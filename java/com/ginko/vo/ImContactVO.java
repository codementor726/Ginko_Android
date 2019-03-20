package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;

/**
 * Created by YongJong on 01/23/17.
 */
public class ImContactVO implements Serializable {
    @Alias("id")
    public Integer memberId;

    @Alias("name")
    public String name;

    @Alias("profile_image")
    public String profileImage;

    public Integer getMemberId() { return memberId; }
    public String getName() { return this.name; }
    public String getProfileImage() {return this.profileImage; }

    public void setMemberId(Integer _id) { this.memberId = _id; }
    public void setName(String _name) { this.name = _name; }
    public void setProfileImage(String _image) { this.profileImage = _image; }

    public ImContactVO()
    {
        this.name = "";
        this.profileImage = "";
    }

}
