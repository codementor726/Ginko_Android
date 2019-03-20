package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongJong on 01/11/17.
 */
public class DirectoryVO implements Serializable {
    @Alias("id")
    private Integer id;

    @Alias("name")
    private String name;

    @Alias("privilege")
    private Integer privilege;

    @Alias("approve_mode")
    private Integer approveMode;

    @Alias("profile_image")
    private String profileImage;

    @Alias("domain")
    private String domains;

    public Integer getId() {return id;}
    public void setId(Integer _id) {this.id = _id;}

    public String getName() {return name;}
    public void setName(String _name) {this.name = _name;}

    public String getProfileImage() {return profileImage;}
    public void setProfileImage(String _profileImage) {this.profileImage = _profileImage;}


    public Integer getPrivilege() {
        return privilege;
    }
    public void setPrivilege(Integer privilege) {
        this.privilege = privilege;
    }

    public Integer getApproveMode() {return approveMode;}
    public void setApproveMode(Integer _appMode) {this.approveMode = _appMode;}

    public String getDomains() {
        return domains;
    }
    public void setDomains(String _domains) {
        this.domains = _domains;
    }
}
