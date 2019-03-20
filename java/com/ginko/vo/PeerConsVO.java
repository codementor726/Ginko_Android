package com.ginko.vo;

import com.apprtcClient.AppRTCClient;
import com.sz.util.json.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by YongJong on 03/29/17.
 */
public class PeerConsVO implements Serializable{
    @Alias("peerCons")
    private AppRTCClient peerCons;

    @Alias("user_id")
    private String user_id;

    @Alias("type")
    private String type;

    @Alias( "create_time")
    private long createTime;

    private Integer iceConnected = 0;

    public AppRTCClient getPeerCons() {return peerCons;}
    public void setPeerCons(AppRTCClient _data) {this.peerCons = _data;}

    public String getUser_id() {return user_id;}
    public void setUser_id(String _data) {this.user_id = _data;}

    public String getType() {return type;}
    public void setType(String _data) {this.type = _data;}

    public long getCreateTime() {return createTime;}
    public void setCreateTime(long _data) {this.createTime = _data;}

    public Integer getIceConnected() {return iceConnected;}
    public void setIceConnected(Integer _data) {this.iceConnected = _data;}

}
