package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;

public class SharedInfoVO implements Serializable {
    @Alias("shared_home_fids")
    private String sharedHomeFIds;

    @Alias("shared_work_fids")
    private String sharedWorkFIds;

    @Alias("share_limit")
    private int shareLimit;

    @Alias("sharing_status")
    private int sharingStatus;

    public void setSharedHomeFIds(String workFIds){this.sharedWorkFIds = workFIds;}
    public String getSharedHomeFIds(){return this.sharedWorkFIds==null?"":this.sharedWorkFIds;}

    public void setSharedWorkFIds(String homeFIds){this.sharedHomeFIds = homeFIds;}
    public String getSharedWorkFIds(){return this.sharedHomeFIds==null?"":this.sharedHomeFIds;}

    public  void setShareLimit(int limit){this.shareLimit = limit;}
    public int getShareLimit(){return  this.shareLimit;}

    public void setSharingStatus(int status){this.sharingStatus = status;}
    public int getSharingStatus(){return this.sharingStatus;}
}
