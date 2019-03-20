package com.ginko.vo;

/**
 * Created by YongJong on 03/22/17.
 */
import java.io.Serializable;
import java.util.Date;

import com.sz.util.json.Alias;


public class SdpCandidateVO implements Serializable {
    @Alias("sdp")
    private String sdp;

    @Alias("candidates")
    private String candidates;

    @Alias("to")
    private String toUserId;

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String _data) {
        this.sdp = _data;
    }

    public String getCandidates() {
        return candidates;
    }

    public void setCandidates(String _data) {
        this.candidates = _data;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String _data) {
        this.toUserId = _data;
    }
}
