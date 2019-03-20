package com.ginko.vo;

import com.sz.util.json.Alias;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongJong on 03/22/17.
 */
public class CandidateMainVO implements Serializable {
    @Alias("from")
    private String from;

    @Alias("to")
    private String to;

    @Alias("candidates")
    private String candidates;

    public String getFrom() {return from;}
    public void setFrom(String _data) { this.from = _data;}

    public String getTo() {return to;}
    public void setTo(String _data) {this.to = _data;}

    public String getCandidates() {return candidates;}

    public void setCandidates(String _list) {
        this.candidates = _list;
    }
}
