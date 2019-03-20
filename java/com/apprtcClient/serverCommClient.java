package com.apprtcClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import com.apprtcClient.util.*;
import com.ginko.api.request.IMRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.vo.CandidateMainVO;
import com.ginko.vo.SdpMainVO;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class serverCommClient implements AppRTCClient{
  private static final boolean PREFER_ISAC = false;
  public static final String VIDEO_TRACK_ID = "ARDAMSv0";
  public static final String AUDIO_TRACK_ID = "ARDAMSa0";
  public static final String VIDEO_TRACK_TYPE = "video";
  private static final String TAG = "PCRTCClient";
  private static final String VIDEO_CODEC_VP8 = "VP8";
  private static final String VIDEO_CODEC_VP9 = "VP9";
  private static final String VIDEO_CODEC_H264 = "H264";
  private static final String AUDIO_CODEC_OPUS = "opus";
  private static final String AUDIO_CODEC_ISAC = "ISAC";
  private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
  private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
  private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
  private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
  private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
  private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
  private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
  private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
  private static final int HD_VIDEO_WIDTH = 1280;
  private static final int HD_VIDEO_HEIGHT = 720;
  private static final int BPS_IN_KBPS = 1000;

  private final LooperExecutor executor;
  private SignalingEvents events;
  private ConnectionState roomState = ConnectionState.NEW;

  private PeerConnectionFactory factory = null;
  private PeerConnection pc = null;
  PeerConnectionFactory.Options options = null;
  private AudioSource audioSource;
  private VideoSource videoSource;
  private boolean preferIsac;
  private String preferredVideoCodec;
  private boolean videoCapturerStopped;
  private List<VideoRenderer.Callbacks> remoteRenders;
  private MediaConstraints pcConstraints;
  private MediaConstraints audioConstraints;
  private MediaConstraints sdpMediaConstraints;
  private SessionDescription localSdp;

  private int videoWidth;
  private int videoHeight;
  private int videoFps;

  private MediaStream mediaStream, remoteStream;
  private VideoCapturer videoCapturer;
  // enableVideo is set to true if video should be rendered and sent.
  private VideoTrack remoteVideoTrack;
  // enableAudio is set to true if audio should be sent.
  private boolean enableAudio;

  private boolean isInitiator = false;
  private boolean isSpeakerEnabled = false;
  private boolean videoCallEnabled = false;
  private boolean noAudioProcessing = false;
  private boolean isError = false;
  private boolean renderVideo = false;

  private List<PeerConnection.IceServer> iceServers;
  private PCObserver pcObserver = new PCObserver();
  private SDPObserver sdpObserver = new SDPObserver();
  private LinkedList<IceCandidate> queuedRemoteCandidates = null;

  private int boardId = 0;
  private int callType = 1;
  private String memberId = "";
  private ArrayList<IceCandidate> allCandidates;
  public boolean iceConnected = false;
  private boolean quit = false;

  public serverCommClient(SignalingEvents events, int boardId, int callType, List<PeerConnection.IceServer> iceServers, String memberId) {
    this.events = events;
    executor = new LooperExecutor();

    this.boardId = boardId;
    this.callType = callType;
    if (this.iceServers == null)
      this.iceServers = new ArrayList<PeerConnection.IceServer>();
    this.iceServers = iceServers;
    this.memberId = memberId;
    this.allCandidates = new ArrayList<IceCandidate>();

    isSpeakerEnabled = true;

    factory = null;
    pc = null;
    preferIsac = false;
    videoCapturerStopped = false;
    isError = false;
    queuedRemoteCandidates = null;
    mediaStream = null;
    remoteStream = null;
    videoCapturer = null;
    renderVideo = true;
    remoteVideoTrack = null;
    enableAudio = true;

    isError = false;

    // Check preferred video codec.
    preferredVideoCodec = VIDEO_CODEC_VP8;
    //preferredVideoCodec = VIDEO_CODEC_VP9;
    //preferredVideoCodec = VIDEO_CODEC_H264;

    preferIsac = false;
    factory = new PeerConnectionFactory(options);
  }

  // --------------------------------------------------------------------
  // AppRTCClient interface implementation.
  // Asynchronously connect to an AppRTC room URL, e.g.
  // https://apprtc.appspot.com/register/<room>, retrieve room parameters
  // and connect to WebSocket server.

  @Override
  public void connectToRoomWithStream(MediaStream localStream) {
    executor.requestStart();
    roomState = ConnectionState.NEW;
    mediaStream = localStream;
    startSignalingIfReady(localStream);
  }

  @Override
  public void disconnectFromRoom() {
    if (roomState == ConnectionState.CLOSED)
      return;

    roomState = ConnectionState.CLOSED;
    closeInternal();
  }

  @Override
  public void sendOffer() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        pc.createOffer(sdpObserver, defaultOfferConstraints());
      }
    });
  }

  @Override
  public void getRemoteSDPData(final String memberId) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        String dataType = "sdp";

        IMRequest.setGetSDPdata(boardId, dataType, memberId, new ResponseCallBack<SdpMainVO>() {
          @Override
          public void onCompleted(JsonResponse<SdpMainVO> response) throws IOException {
            if (response.isSuccess()) {
              SdpMainVO data = response.getData();
              if (data == null) return;
              try {
                String strSdp = data.getSdp();
                JSONObject json = new JSONObject(strSdp);
                String type = json.getString("type");
                String sdpStr = json.getString("sdp");

                SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(type),
                        sdpStr);
                setRemoteDescription(sdp);
              } catch (JSONException e) {
                throw new RuntimeException(e);
              }
            }
          }
        });
      }
    });
  }

  @Override
  public void getRemoteIceCandidate(final String memberId) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        String dataType = "candidates";
        IMRequest.setGetCandidateData(boardId, dataType, memberId, new ResponseCallBack<CandidateMainVO>() {
          @Override
          public void onCompleted(JsonResponse<CandidateMainVO> response) throws IOException {
            if (response.isSuccess()) {
              CandidateMainVO data = response.getData();
              try {
                CandidateMainVO rMainVO = (CandidateMainVO) data;
                if (data != null) {
                  String thisList = rMainVO.getCandidates();
                  JSONArray newJson = new JSONArray(thisList);

                  if (newJson.length() < 1)
                    return;
                  for (int i = 0; i < newJson.length(); i++) {
                    JSONObject eachOne = (JSONObject) newJson.get(i);
                    String strMid = eachOne.getString("sdpMid");
                    String strMLine = eachOne.getString("sdpMLineIndex");
                    String strCandidate = eachOne.getString("candidate");

                    IceCandidate candidate = new IceCandidate(
                            strMid,
                            Integer.valueOf(strMLine),
                            strCandidate);

                    if (pc != null && !isError) {
                      if (queuedRemoteCandidates != null) {
                        queuedRemoteCandidates.add(candidate);
                      } else {
                        pc.addIceCandidate(candidate);
                      }
                    }
                  }
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
          }
        });
      }
    });

  }

  @Override
  public void setStreamPeerConnection(final MediaStream stream) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        pc.removeStream(stream);
        pc.addStream(stream);

        mediaStream = stream;
      }
    });
  }

  @Override
  public void setIsInitiator(boolean _data) {
    this.isInitiator = _data;
  }

  @Override
  public boolean isIsInitiator() {
    return isInitiator;
  }

  @Override
  public void setIsSpeakerEnabled(boolean _data) {
  this.isSpeakerEnabled = _data;
  }

  @Override
  public boolean isIsSpeakerEnabled() {
    return isSpeakerEnabled;
  }

  private void createMediaConstraintsInternal() {
    // Create peer connection constraints.
    pcConstraints = new MediaConstraints();
    // Enable DTLS for normal calls and disable for loopback calls.
    pcConstraints.optional.add(new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));

    // Create SDP constraints.
    sdpMediaConstraints = new MediaConstraints();
    sdpMediaConstraints.mandatory.add(
            new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
    sdpMediaConstraints.mandatory.add(
            new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
  }

  private void closeInternal() {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        if (quit) {
          return;
        }
        quit = true;

        if (pc != null) {
          pc.close();
          pc = null;
        }

        if (remoteStream != null) {
          remoteStream.audioTracks.get(0).dispose();
          remoteStream.videoTracks.get(0).dispose();
          //remoteStream.dispose();
          //remoteStream = null;
        }


        if (factory != null) {
          factory = null;
        }

        pcObserver = null;
        sdpObserver = null;
        sdpMediaConstraints = null;

        options = null;
      }
    });

  }

  // Connects to room - function runs on a local looper thread.
  private void startSignalingIfReady(final MediaStream localStream) {
    roomState = ConnectionState.CONNECTED;
    executor.execute(new Runnable() {
      @Override
      public void run() {
        createMediaConstraintsInternal();
        pc = factory.createPeerConnection(iceServers,
                pcConstraints, pcObserver);
        pc.addStream(localStream);
      }
    });
  }

  // Send local SDP to the other participant.
  @Override
  public void sendSignalingSDpToIceServer(final SessionDescription sdp, final String memberId) {
    if (roomState != ConnectionState.CONNECTED) {
      reportError("Sending offer SDP in non connected state.");
      return;
    }
    JSONObject json = new JSONObject();
    jsonPut(json, "sdp", sdp.description);
    if (sdp.type.equals(SessionDescription.Type.OFFER))
      jsonPut(json, "type", "offer");
    else if (sdp.type.equals(SessionDescription.Type.ANSWER))
      jsonPut(json, "type", "answer");

    JSONObject finalData = new JSONObject();
    jsonPut(finalData, "sdp", json);
    jsonPut(finalData, "to", memberId);

    IMRequest.setSendSDPdata(boardId, finalData, new ResponseCallBack<Void>() {
      @Override
      public void onCompleted(JsonResponse<Void> response) throws IOException {
        if (response.isSuccess()) {
          boolean isData = true;
        }
      }
    });
  }

  // Send All Ice candidates to the other participant.
  @Override
  public void sendAllIceCandidates(final ArrayList<IceCandidate> candidates, final String toUser) {
    JSONArray canArray = new JSONArray();

    for(int i = 0; i < candidates.size(); i++){
      IceCandidate candidate = candidates.get(i);
      JSONObject json = new JSONObject();
      jsonPut(json, "sdpMLineIndex", candidate.sdpMLineIndex);
      jsonPut(json, "sdpMid", candidate.sdpMid);
      jsonPut(json, "candidate", candidate.sdp);
      jsonPut(canArray, json);
    }

    JSONObject finalData = new JSONObject();
    jsonPut(finalData, "to", toUser);
    jsonPut(finalData, "candidates", canArray);

    IMRequest.setSendSDPdata(boardId, finalData, new ResponseCallBack<Void>() {
      @Override
      public void onCompleted(JsonResponse<Void> response) throws IOException {
        if (response.isSuccess()) {

        }
      }
    });
  }

  // --------------------------------------------------------------------
  // Helper functions.
  private void reportError(final String errorMessage) {

    if (roomState != ConnectionState.ERROR) {
      roomState = ConnectionState.ERROR;
      events.onChannelError(errorMessage, memberId);
    }
  }

  // Put a |key|->|value| mapping in |json|.
  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  // Put a |key|->|value| mapping in |json|.
  private static void jsonPut(JSONArray json, JSONObject value) {
    try {
      json.put(value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setRemoteDescription(final SessionDescription sdp) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        if (pc == null || isError) {
          return;
        }
        String sdpDescription = sdp.description;
        if (PREFER_ISAC) {
          sdpDescription = preferISAC(sdpDescription);
        }

        SessionDescription sdpRemote = new SessionDescription(
                sdp.type, sdpDescription);
        pc.setRemoteDescription(sdpObserver, sdpRemote);
      }
    });
  }

  private MediaConstraints defaultAnswerConstraints() {
    return this.defaultOfferConstraints();
  }

  private MediaConstraints defaultOfferConstraints() {
    MediaConstraints sdpMediaConstraints = new MediaConstraints();
    sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
            "OfferToReceiveAudio", "true"));
    sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
            "OfferToReceiveVideo", "true"));

    return sdpMediaConstraints;
  }

  // Mangle SDP to prefer ISAC/16000 over any other audio codec.
  private static String preferISAC(String sdpDescription) {
    String[] lines = sdpDescription.split("\r\n");
    int mLineIndex = -1;
    String isac16kRtpMap = null;
    Pattern isac16kPattern =
            Pattern.compile("^a=rtpmap:(\\d+) ISAC/16000[\r]?$");
    for (int i = 0;
         (i < lines.length) && (mLineIndex == -1 || isac16kRtpMap == null);
         ++i) {
      if (lines[i].startsWith("m=audio ")) {
        mLineIndex = i;
        continue;
      }
      Matcher isac16kMatcher = isac16kPattern.matcher(lines[i]);
      if (isac16kMatcher.matches()) {
        isac16kRtpMap = isac16kMatcher.group(1);
        continue;
      }
    }
    if (mLineIndex == -1) {
      return sdpDescription;
    }
    if (isac16kRtpMap == null) {
      return sdpDescription;
    }
    String[] origMLineParts = lines[mLineIndex].split(" ");
    StringBuilder newMLine = new StringBuilder();
    int origPartIndex = 0;
    // Format is: m=<media> <port> <proto> <fmt> ...
    newMLine.append(origMLineParts[origPartIndex++]).append(" ");
    newMLine.append(origMLineParts[origPartIndex++]).append(" ");
    newMLine.append(origMLineParts[origPartIndex++]).append(" ");
    newMLine.append(isac16kRtpMap);
    for (; origPartIndex < origMLineParts.length; ++origPartIndex) {
      if (!origMLineParts[origPartIndex].equals(isac16kRtpMap)) {
        newMLine.append(" ").append(origMLineParts[origPartIndex]);
      }
    }
    lines[mLineIndex] = newMLine.toString();
    StringBuilder newSdpDescription = new StringBuilder();
    for (String line : lines) {
      newSdpDescription.append(line).append("\r\n");
    }
    return newSdpDescription.toString();
  }

  // Implementation detail: observe ICE & stream changes and react accordingly.
  private class PCObserver implements PeerConnection.Observer {
    @Override
    public void onIceCandidate(final IceCandidate candidate){
      allCandidates.add(candidate);
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          if (pc == null || isError) {
            return;
          }

          pc.removeIceCandidates(candidates);
        }
      });
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState newState) {

    }

    @Override
    public void onIceConnectionChange(
            final PeerConnection.IceConnectionState newState) {

      executor.execute(new Runnable() {
        @Override
        public void run() {
          if (newState == PeerConnection.IceConnectionState.CONNECTED) {
            iceConnected = true;
            //events.onConnectedOnConference(memberId);
          } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
            if (iceConnected == true)
            {
              iceConnected = false;
              //events.onClosedOnConference(memberId, 1);
            }
          } else if (newState == PeerConnection.IceConnectionState.CLOSED) {
            if (iceConnected == true)
            {
              iceConnected = false;
            }
            //events.onClosedOnConference(memberId, 1);
          } else if (newState == PeerConnection.IceConnectionState.FAILED) {
            reportError("ICE connection failed.");
          }
        }
      });
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
      executor.execute(new Runnable() {
        @Override
        public void run() {

        }
      });
    }

    @Override
    public void onIceGatheringChange(final PeerConnection.IceGatheringState newState) {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          ArrayList<IceCandidate> myList = new ArrayList<IceCandidate>(allCandidates);
          if (newState.equals(PeerConnection.IceGatheringState.COMPLETE) && myList.size() > 0) {
            sendAllIceCandidates(myList, memberId);
            allCandidates.clear();
          }
        }
      });
    }

    @Override
    public void onAddStream(final MediaStream stream) {

      executor.execute(new Runnable() {
        @Override
        public void run() {
          if (pc == null || isError) {
            return;
          }
          if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
            reportError("Weird-looking stream: " + stream);
            return;
          }

          remoteStream = stream;

          if (stream.videoTracks.size() == 1 && stream.audioTracks.size() == 1) {
            VideoTrack videoTrack = stream.videoTracks.get(0);
            AudioTrack audioTrack = stream.audioTracks.get(0);
            events.onReceiveRemoteVideoTrack(videoTrack, audioTrack, memberId);
          } else if (stream.videoTracks.size() == 1 && stream.audioTracks.size() == 0) {
            VideoTrack videoTrack = stream.videoTracks.get(0);
            AudioTrack audioTrack = null;
            events.onReceiveRemoteVideoTrack(videoTrack, audioTrack, memberId);
          } else if (stream.videoTracks.size() == 0 && stream.audioTracks.size() == 1) {
            VideoTrack videoTrack = null;
            AudioTrack audioTrack = stream.audioTracks.get(0);
            events.onReceiveRemoteVideoTrack(videoTrack, audioTrack, memberId);
          }
        }
      });

    }

    @Override
    public void onRemoveStream(final MediaStream stream){
      executor.execute(new Runnable() {
        @Override
        public void run() {
          if (pc == null || isError) {
            return;
          }
          /*
          if (stream.videoTracks.size() == 1)
            stream.videoTracks.get(0).dispose();
          if (stream.audioTracks.size() == 1)
            stream.audioTracks.get(0).dispose();
            */
          if (mediaStream != null) {
            mediaStream.dispose();
            mediaStream = null;
          }

          if (remoteStream != null) {
            remoteStream.dispose();
            remoteStream = null;
          }
        }
      });


    }

    @Override
    public void onDataChannel(final DataChannel dc) {
      reportError("AppRTC doesn't use data channels, but got: " + dc.label()
              + " anyway!");
    }

    @Override
    public void onRenegotiationNeeded() {
      // No need to do anything; AppRTC follows a pre-agreed-upon
      // signaling/negotiation protocol.
    }
  }
  // Implementation detail: handle offer creation/signaling and answer setting,
  // as well as adding remote ICE candidates once the answer SDP is set.
  private class SDPObserver implements SdpObserver {
    @Override
    public void onCreateSuccess(final SessionDescription origSdp) {
      /*
      if (localSdp != null) {
        reportError("Multiple SDP create.");
        return;
      }
      */
      String sdpDescription = origSdp.description;
      if (PREFER_ISAC) {
        sdpDescription = preferISAC(sdpDescription);
      }
      final SessionDescription sdp = new SessionDescription(
              origSdp.type, sdpDescription);
      localSdp = sdp;
      executor.execute(new Runnable() {
        @Override
        public void run() {
          if (pc != null && !isError) {
            pc.setLocalDescription(sdpObserver, sdp);
            sendSignalingSDpToIceServer(sdp, memberId);
          }
        }
      });
    }

    @Override
    public void onSetSuccess() {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          if (pc == null || isError) {
            events.onChannelError("Failed to set session descripton", memberId);
            return;
          }

          if (isInitiator)
          {
            MediaConstraints constraints = defaultAnswerConstraints();
          } else
          {
            MediaConstraints constraints = defaultAnswerConstraints();
            if (pc.getLocalDescription() == null) {
              pc.createAnswer(sdpObserver, constraints);
            }
          }
        }
      });
    }

    @Override
    public void onCreateFailure(final String error) {
      reportError("createSDP error: " + error);
    }

    @Override
    public void onSetFailure(final String error) {
      reportError("setSDP error: " + error);
    }
  }
}
