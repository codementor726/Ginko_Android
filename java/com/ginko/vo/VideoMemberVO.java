package com.ginko.vo;


/**
 * Created by YongJong on 03/23/17.
 */
public class VideoMemberVO {
    private String userId;
    private String name;
    private String imageUrl = "";
    private boolean isOwner = false;
    private boolean isMe;
    private Integer weight;
    private boolean isInitialized = false;
    private boolean videoStatus;
    private boolean voiceStatus;
    private boolean isOlder = false;
    private boolean isYounger = false;
    private boolean invitedByMe = true;

    public String getUserId() {return userId;}
    public void setUserId(String _data) {this.userId = _data;}

    public String getName() {return name;}
    public void setName(String _data) {this.name = _data;}

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String _data) {this.imageUrl = _data;}

    public boolean isOwner() {return isOwner;}
    public void setOwner(boolean _isData) { this.isOwner = _isData;}

    public boolean isMe() {return isMe;}
    public void setMe(boolean _isData) { this.isMe = _isData;}

    public boolean isInitialized() {return isInitialized;}
    public void setInitialized(boolean _isData) {this.isInitialized = _isData;}

    public Integer getWeight() {return weight;}
    public void setWeight(Integer _data) {this.weight = _data;}

    public boolean isVideoStatus() {return videoStatus;}
    public void setVideoStatus(boolean _isData) {this.videoStatus = _isData;}

    public boolean isVoiceStatus() {return voiceStatus;}
    public void setVoiceStatus(boolean _isData) {this.voiceStatus = _isData;}

    public boolean isOlder() {return isOlder;}
    public void setOlder(boolean _isData) {this.isOlder = _isData;}

    public boolean isYounger() {return isYounger;}
    public void setYounger(boolean _isData) {this.isYounger = _isData;}

    public boolean isInvitedByMe(){return invitedByMe;}
    public void setInvitedByMe(boolean _invitedByMe){ this.invitedByMe = _invitedByMe; }
}
