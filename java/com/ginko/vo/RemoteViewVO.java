package com.ginko.vo;

import org.webrtc.AudioTrack;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoTrack;

/**
 * Created by YongJong on 03/29/17.
 */
public class RemoteViewVO {
    private VideoTrack remoteTrack;
    private AudioTrack remoteAudio;
    private String name;
    private String user_id;
    private String photoUrl = "";
    private boolean videoStatus;
    private boolean voiceStatus;
    private boolean viewRemoted;
    private boolean isRendered = false;
    private boolean isMe;
    private boolean invitedByMe;

    private Integer endType = 1;
    private int iceConnected;
    private long createTime;

    public VideoTrack getRemoteTrack() {return remoteTrack;}
    public void setRemoteTrack(VideoTrack _data) {this.remoteTrack = _data;}

    public AudioTrack getRemoteAudioTrack() {return remoteAudio;}
    public void setRemoteAudioTrack(AudioTrack _data) {this.remoteAudio = _data;}

    public String getName() {return name;}
    public void setName(String _data) {this.name = _data;}

    public String getUser_id() {return user_id;}
    public void setUser_id(String _data) {this.user_id = _data;}

    public String getPhotoUrl() {return photoUrl;}
    public void setPhotoUrl(String _data) {this.photoUrl = _data;}

    public boolean isVideoStatus() {return videoStatus;}
    public void setVideoStatus(boolean _isVideo) {this.videoStatus = _isVideo;}

    public boolean isVoiceStatus() {return voiceStatus;}
    public void setVoiceStatus(boolean _isVoice) {this.voiceStatus = _isVoice;}

    public boolean isViewRemoted() {return viewRemoted;}
    public void setViewRemoted(boolean _data) {this.viewRemoted = _data;}

    public boolean isRendered() {return isRendered;}
    public void setRendered(boolean _data) {this.isRendered = _data;}

    public boolean isMe() {return isMe;}
    public void setMe(boolean _data) {this.isMe = _data;}

    public int getIceConnected() {return iceConnected;}
    public void setIceConnected(int _ice) {this.iceConnected = _ice;}

    public Integer getEndType() {return endType;}
    public void setEndType(Integer _data) { this.endType = _data;}

    public long getCreateTime() {return createTime;}
    public void setCreateTime(long _data) {this.createTime = _data;}

    public boolean isInvitedByMe(){return invitedByMe;}
    public void setInvitedByMe(boolean _invitedByMe){ this.invitedByMe = _invitedByMe; }
}
