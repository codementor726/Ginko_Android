package com.ginko.vo;

import java.io.Serializable;
import com.sz.util.json.Alias;

import org.json.JSONObject;

/**
 * Created by YongJong on 03/22/17.
 */
public class SdpMainVO implements Serializable {
    @Alias("from")
    private String from;

    @Alias("to")
    private String to;

    @Alias("sdp")
    private String sdp;

    public String getFrom() {return from;}
    public void setFrom(String _data) { this.from = _data;}

    public String getTo() {return to;}
    public void setTo(String _data) {this.to = _data;}

    public String getSdp() {return sdp;}

    public void setSdp(String data) {this.sdp = data;}
}