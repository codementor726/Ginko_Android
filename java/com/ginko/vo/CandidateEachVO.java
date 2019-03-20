package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;

/**
 * Created by YongJong on 03/22/17.
 */
public class CandidateEachVO implements Serializable {
    @Alias("sdpMLineIndex")
    private String sdpMLineIndex;

    @Alias("sdpMid")
    private String sdpMid;

    @Alias("candidate")
    private String candidate;

    public String getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(String _data) {
        this.sdpMLineIndex = _data;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String _data) {
        this.sdpMid = _data;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String _data) {
        this.candidate = _data;
    }
}
