package com.apprtcClient;

/**
 * Created by YongJong on 03/28/17.
 */
public class AppRTCManager {
    private AppRTCClient peerCon;
    private String type;
    private String userId;

    public AppRTCClient getPeerCon() {return peerCon;}
    public void setPeerCon(AppRTCClient _data) {peerCon = _data;}

    public String getType() {return type;}
    public void setType(String _data) {this.type = _data;}

    public String getUserId() {return userId;}
    public void setUserId(String _data) {this.userId = _data;}
}
