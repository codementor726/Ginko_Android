package com.ginko.vo;

import java.io.Serializable;
import com.sz.util.json.Alias;

/**
 * Created by YongJong on 03/22/17.
 */
public class SdpShortVO implements Serializable {
    @Alias("sdp")
    private String sdp;

    @Alias("type")
    private String type;

    public String getSdp() {return sdp;}
    public void setSdp(String _data) { this.sdp = _data;}

    public String getType() {return type;}
    public void setType(String _data) { this.type = _data;}
}
