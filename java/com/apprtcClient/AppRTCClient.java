/*
 * libjingle
 * Copyright 2013 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.apprtcClient;

import com.ginko.vo.SdpMainVO;
import com.videophotofilter.android.videolib.org.m4m.MediaStreamer;

import org.json.JSONArray;
import org.webrtc.AudioTrack;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * AppRTCClient is the interface representing an AppRTC client.
 */
public interface AppRTCClient {
  enum ConnectionState {
    NEW, CONNECTED, CLOSED, ERROR
  };
  enum MessageType {
    MESSAGE, BYE
  };

  /**
   * Asynchronously connect to an AppRTC room URL, e.g.
   * onConnectedToRoom() callback with room parameters is invoked.
   */
  public void connectToRoomWithStream(MediaStream localStream);

  /**
   *  Send General Session Description
   */
  public void sendOffer();
  /**
   * Send SDP to server for other participants
   */
  public void sendSignalingSDpToIceServer(final SessionDescription sdp, final String memberId);

  /**
   * Send Ice candidate to the other participant.
   */
  public void sendAllIceCandidates(final ArrayList<IceCandidate> candidates, final String toUser);

  /**
   * Disconnect from room.
   */
  public void disconnectFromRoom();

  /**
   * parse Candidate SDP Data
   */
  public void getRemoteSDPData(String memberId);
  public void getRemoteIceCandidate(String memberId);

  /**
   *  set Stream Peer Connection
   */
  public void setStreamPeerConnection(MediaStream stream);


  public void setIsInitiator(boolean _data);

  public boolean isIsInitiator();

  public void setIsSpeakerEnabled(boolean _data);

  public boolean isIsSpeakerEnabled();

  /**
   * Callback interface for messages delivered on signaling channel.
   *
   * <p>Methods are guaranteed to be invoked on the UI thread of |activity|.
   */
  public static interface SignalingEvents {
    /**
     * Callback fired once Connection State Changed.
     */
    public void onChangeState(final ConnectionState roomState, final String memberId);

    /**
     * Callback fired once Video Track Receive.
     */
    public void onReceiveRemoteVideoTrack(final VideoTrack remoteTrack, final AudioTrack remoteAudio, final String memberId);

    /**
     * Callback fired once Audio  Track Receive
     */
    public void onReceiveRemoteAudioTrack(final AudioTrack remoteTrack, final String memberId);


    /**
     * Callback fired once channel error happened.
     */
    public void onChannelError(final String description, final String memberId);

    /**
     * Callback fired once channel is opened.
     */
    public void onConnectedOnConference(String memberId);

    /**
     * Callback fired once channel is closed.
     */
    public void onClosedOnConference(String memberId, int disconnectType);
  }
}
