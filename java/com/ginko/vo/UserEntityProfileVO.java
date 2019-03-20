package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class UserEntityProfileVO implements Serializable {

	@Alias("entity_id")
	private Integer id;

	@Alias("name")
	private String name;

	@Alias("description")
	private String description;

	@Alias("category_id")
	private Integer categoryId;

	@Alias(value="create_time", ignoreGet= true)
	private Date createTime;
	
	@Alias("privilege")
	private Integer privilege;

	@Alias("search_words")
	private String tags;

	
	@Alias(value ="profile_image" , ignoreGet= true)
	private String profileImage;
	
	@Alias(value = "follower_total", ignoreGet= true)
	private long followerNum;

	@Alias("notes")
	private String notes = "";

    @Alias("images")
    private List<EntityImageVO> images;

    @Alias("infos")
    private List<EntityInfoVO> infos;

    @Alias("video_url")
    private String videoUrl;

	@Alias(value = "video_thumbnail_url", ignoreGet= true)
	private String videoThumbUrl;

    @Alias("longitude")
    private double longitude;

    @Alias("latitude")
    private double latitude;

	@Alias("info_total")
	private Integer infoTotal;

	@Alias("invite_status")
	private Integer inviteStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNotes(String str){
		this.notes = str;
	}

	public String getNotes(){return this.notes;}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getPrivilege() {
		return privilege;
	}

	public void setPrivilege(Integer privilege) {
		this.privilege = privilege;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getDescription(){return description == null?"":this.description;}
	public void setDescription(String description){this.description = description;}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
	

	public long getFollowerNum() {
		return followerNum;
	}

	public void setFollowerNum(long followerNum) {
		this.followerNum = followerNum;
	}

    public void setImages(List<EntityImageVO> _images){this.images = _images;}
    public List<EntityImageVO> getImages(){
        if(this.images == null)
            this.images = new ArrayList<EntityImageVO>();
        return this.images;
    }

    public void setInfos(List<EntityInfoVO> _infos){this.infos = _infos;}
    public List<EntityInfoVO> getInfos(){
        if(this.infos == null)
            this.infos = new ArrayList<EntityInfoVO>();
        return this.infos;
    }

    public void setVideoUrl(String _url){this.videoUrl = _url;}
    public String getVideoUrl(){return this.videoUrl;}

    public void setLongitude(double _lng){this.longitude = _lng;}
    public double getLongitude(){return this.longitude;}

    public void setLatitude(double _lat){this.latitude = _lat;}
    public double getLatitude(){return this.latitude;}

	public String getVideoThumbUrl(){return videoThumbUrl == null?"": this.videoThumbUrl;}
	public void setVideoThumbUrl(String videoThumbUrl){this.videoThumbUrl = videoThumbUrl;}

	@Alias(ignoreGet =  true , ignoreSet = true)
	public EntityImageVO getWallpapaerImage()
	{
		EntityImageVO wallpaperImage = null;
		if(images == null || images.size() == 0) return wallpaperImage;

		for(int i=0;i<images.size();i++)
		{
			if(images.get(i).getZIndex() == 0)//background image is changed to wallpaper
			{
				wallpaperImage = images.get(i);
				break;
			}
		}

		return wallpaperImage;
	}

	public Integer getInfoTotal() {return infoTotal;}
	public void setInfoTotal(Integer _total) {this.infoTotal = _total;}

	public Integer getInviteStatus() {return inviteStatus;}
	public void setInviteStatus(Integer _status) {this.inviteStatus = _status;}
}
