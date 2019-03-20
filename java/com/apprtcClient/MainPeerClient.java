package com.apprtcClient;

import android.content.Context;
import android.os.Environment;

import com.ginko.activity.im.GroupVideoChatActivity;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpSender;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YongJong on 04/07/17.
 */
public class MainPeerClient {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_TRACK_TYPE = "video";
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


    private static final MainPeerClient instance = new MainPeerClient();
    private PCObserver pcObserver = new PCObserver();
    private SDPObserver sdpObserver = new SDPObserver();
    private final ScheduledExecutorService executor;

    private Context context;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    PeerConnectionFactory.Options options = null;
    private AudioSource audioSource;
    private VideoSource videoSource;
    private boolean videoCallEnabled;
    private boolean preferIsac;
    private String preferredVideoCodec;
    private boolean videoCapturerStopped;
    private boolean isError;
    private Timer statsTimer;
    private VideoRenderer.Callbacks localRender;
    private MediaConstraints pcConstraints;
    private int videoWidth;
    private int videoHeight;
    private int videoFps;
    private MediaConstraints audioConstraints;
    private PeerConnectionParameters peerConnectionParameters;
    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private PeerConnectionEvents events;
    private MediaStream mediaStream;
    private VideoCapturer videoCapturer;
    // enableVideo is set to true if video should be rendered and sent.
    private boolean renderVideo;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio;

    private final Boolean[] quit = new Boolean[] { false };
    /**
     * Peer connection parameters.
     */
    public static class PeerConnectionParameters {
        public final boolean videoCallEnabled;
        public final int videoWidth;
        public final int videoHeight;
        public final int videoFps;
        public final boolean noAudioProcessing;

        public PeerConnectionParameters(boolean videoCallEnabled, int videoWidth, int videoHeight, int videoFps,
                                        boolean noAudioProcessing) {
            this.videoCallEnabled = videoCallEnabled;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.videoFps = videoFps;
            this.noAudioProcessing = noAudioProcessing;
        }
    }

    /**
     * Peer connection events.
     */
    public interface PeerConnectionEvents {
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);

        /**
         * Callback fired once local stream changed
         */
        void onPeerLocalStreamRender(final MediaStream localStream, PeerConnectionFactory factory, boolean isInstance);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is closed (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();
    }

    public MainPeerClient() {
        // Executor thread is started once in private ctor and is used for all
        // peer connection API calls to ensure new peer connection factory is
        // created on the same thread as previously destroyed factory.
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public static MainPeerClient getInstance() {
        return instance;
    }

    public void setPeerConnectionFactoryOptions(PeerConnectionFactory.Options options) {
        this.options = options;
    }

    public void createPeerConnectionFactory(final Context context,
                                            final PeerConnectionParameters peerConnectionParameters, final PeerConnectionEvents events) {
        this.peerConnectionParameters = peerConnectionParameters;
        this.events = events;
        videoCallEnabled = peerConnectionParameters.videoCallEnabled;
        // Reset variables to initial states.
        this.context = null;
        factory = null;
        peerConnection = null;
        preferIsac = false;
        videoCapturerStopped = false;
        isError = false;
        queuedRemoteCandidates = null;
        mediaStream = null;
        videoCapturer = null;
        renderVideo = true;
        localVideoTrack = null;
        localAudioTrack = null;

        enableAudio = true;
        statsTimer = new Timer();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactoryInternal(context);
            }
        });
    }

    public void createPeerConnection(final EglBase.Context renderEGLContext,
                                     final VideoRenderer.Callbacks localRender,
                                     final VideoCapturer videoCapturer,
                                     final List<PeerConnection.IceServer> iceServers) {
        if (peerConnectionParameters == null) {
            return;
        }
        this.localRender = localRender;
        this.videoCapturer = videoCapturer;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    createMediaConstraintsInternal();
                    createPeerConnectionInternal(renderEGLContext, iceServers);
                } catch (Exception e) {
                    reportError("Failed to create peer connection: " + e.getMessage());
                    throw e;
                }
            }
        });
    }

    public void close() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                closeInternal();
            }
        });
    }

    public boolean isVideoCallEnabled() {
        return videoCallEnabled;
    }

    private void createPeerConnectionFactoryInternal(Context context) {
        PeerConnectionFactory.initializeInternalTracer();

        isError = false;

        // Initialize field trials.
        PeerConnectionFactory.initializeFieldTrials("");

        // Check preferred video codec.
        preferredVideoCodec = VIDEO_CODEC_VP8;

        // Check if ISAC is used by default.
        preferIsac = false;

        /*
        WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);
        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        */

        // Create peer connection factory.
        if (!PeerConnectionFactory.initializeAndroidGlobals(
                context, true, true, false)) {
            events.onPeerConnectionError("Failed to initializeAndroidGlobals");
        }

        this.context = context;
        factory = new PeerConnectionFactory(options);
    }

    private void createMediaConstraintsInternal() {
        // Create peer connection constraints.
        pcConstraints = new MediaConstraints();
        // Enable DTLS for normal calls and disable for loopback calls.
        pcConstraints.optional.add(
                new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));

        // Check if there is a camera on device and disable video call if not.
        if (videoCapturer == null) {
            videoCallEnabled = false;
        }
        // Create video constraints if video call is enabled.
        if (videoCallEnabled) {
            videoWidth = peerConnectionParameters.videoWidth;
            videoHeight = peerConnectionParameters.videoHeight;
            videoFps = peerConnectionParameters.videoFps;

            // If video resolution is not specified, default to HD.
            if (videoWidth == 0 || videoHeight == 0) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }

            // If fps is not specified, default to 30.
            if (videoFps == 0) {
                videoFps = 30;
            }
        }

        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        } else
        {
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
    }

    private void createPeerConnectionInternal(EglBase.Context renderEGLContext, List<PeerConnection.IceServer> iceServers) {
        if (factory == null || isError) {
            return;
        }
        queuedRemoteCandidates = new LinkedList<IceCandidate>();

        if (videoCallEnabled) {
            factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
        }

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);

        // Set default WebRTC tracing and INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        if (videoCallEnabled) {
            mediaStream.addTrack(createVideoTrack(videoCapturer));
        }

        mediaStream.addTrack(createAudioTrack());
        peerConnection.addStream(mediaStream);

        events.onPeerLocalStreamRender(mediaStream, factory, true);
    }

    private void getStats() {
        if (peerConnection == null || isError) {
            return;
        }
        boolean success = peerConnection.getStats(new StatsObserver() {
            @Override
            public void onComplete(final StatsReport[] reports) {
                events.onPeerConnectionStatsReady(reports);
            }
        }, null);
        if (!success) {
        }
    }

    public void enableStatsEvents(boolean enable, int periodMs) {
        if (enable) {
            try {
                statsTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                getStats();
                            }
                        });
                    }
                }, 0, periodMs);
            } catch (Exception e) {
            }
        } else {
            statsTimer.cancel();
        }
    }

    public void setAudioEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                enableAudio = enable;
                if (localAudioTrack != null) {
                    localAudioTrack.setEnabled(enableAudio);
                    //events.onPeerLocalStreamRender(mediaStream, false);
                }
            }
        });
    }

    public void setVideoEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                renderVideo = enable;
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(renderVideo);
                    //events.onPeerLocalStreamRender(mediaStream, false);
                }
            }
        });
    }

    private void closeInternal() {
        statsTimer.cancel();

        if (peerConnection != null) {
            //peerConnection.dispose();
            peerConnection.close();
            peerConnection = null;
        }

        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturerStopped = true;
            videoCapturer.dispose();
            videoCapturer = null;
        }

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        if (localAudioTrack != null) {
            localAudioTrack.dispose();
            localAudioTrack = null;
        }

        if (localVideoTrack != null) {
            localVideoTrack.dispose();
            localVideoTrack = null;
        }

        if (mediaStream != null) {
            mediaStream.audioTracks.get(0).dispose();
            mediaStream.videoTracks.get(0).dispose();
            mediaStream = null;
        }

        if (factory != null) {
            factory = null;
        }
        options = null;

        pcConstraints = null;
        audioConstraints = null;

        sdpObserver = null;
        pcObserver = null;

        //events.onPeerConnectionClosed();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
    }

    public void stopVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
            if (videoCapturer != null && !videoCapturerStopped) {
                try {
                    videoCapturer.stopCapture();
                } catch (InterruptedException e) {
                }
                videoCapturerStopped = true;
            }
            }
        });
    }

    public void startVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
            if (videoCapturer != null && videoCapturerStopped) {
                videoCapturer.startCapture(videoWidth, videoHeight, videoFps);
                videoCapturerStopped = false;
            }
            }
        });
    }

    private void reportError(final String errorMessage) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    events.onPeerConnectionError(errorMessage);
                    isError = true;
                }
            }
        });
    }

    private AudioTrack createAudioTrack() {
        createAudioConstraints(enableAudio);
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        videoSource = factory.createVideoSource(videoCapturer);
        capturer.startCapture(videoWidth, videoHeight, videoFps);

        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(videoCallEnabled);
        localVideoTrack.addRenderer(new VideoRenderer(localRender));
        return localVideoTrack;
    }

    private void createAudioConstraints(boolean isAudioEnable)
    {
        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (!isAudioEnable) {
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        } else
        {
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String codecRtpMap = null;
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length) && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
            }
        }
        if (mLineIndex == -1) {
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            return sdpDescription;
        }

        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            // Format is: m=<media> <port> <proto> <fmt> ...
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);
            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }
            lines[mLineIndex] = newMLine.toString();
        } else {
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }

    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            for (IceCandidate candidate : queuedRemoteCandidates) {
                peerConnection.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    private void switchCameraInternal() {
        if (videoCapturer instanceof CameraVideoCapturer) {
            if (!videoCallEnabled || isError || videoCapturer == null) {
                return; // No video is sent or only one camera is available or error happened.
            }
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(null);
        } else {
        }

        //events.onPeerLocalStreamRender(mediaStream, false);
    }

    public void switchCamera() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                switchCameraInternal();
            }
        });
    }
    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate){
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {

        }

        @Override
        public void onSignalingChange(
                PeerConnection.SignalingState newState) {

        }

        @Override
        public void onIceConnectionChange(
                final PeerConnection.IceConnectionState newState) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    //events.onIceConnected();
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    //events.onIceDisconnected();
                } else if (newState == PeerConnection.IceConnectionState.CLOSED) {
                    //events.onIceDisconnected();
                }else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    reportError("ICE connection failed.");
                }
                }
            });
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
        }

        @Override
        public void onIceGatheringChange(final PeerConnection.IceGatheringState newState) {
        }

        @Override
        public void onAddStream(final MediaStream stream){
        }

        @Override
        public void onRemoveStream(final MediaStream stream){
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {
        }
    }
    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(final String error) {
        }

        @Override
        public void onSetFailure(final String error) {

        }
    }
}
