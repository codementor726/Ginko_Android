package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;


public class PurpleContactWholeProfileVO implements Serializable {

	private UserUpdateVO profile = new UserUpdateVO();

	private UserUpdateVO home = new UserUpdateVO();

	private UserUpdateVO work = new UserUpdateVO();

	@Alias("notes")
    private String notes = "";

	//It works, it's a JSONObject
	//@Alias( "share")  //Can be a instance of ContactVO or ExchangeRequestVO
	//private Object share;

	@Alias( "profile")
	public UserUpdateVO getProfile() {
		return profile;
	}

	public void setProfile(UserUpdateVO profile) {
		this.profile = profile;
	}

    public void setNotes(String str){
        this.notes = str;
    }
    public String getNotes(){return this.notes;}


	@Alias( "home")
	public UserUpdateVO getHome() {
		return home;
	}

	public void setHome(UserUpdateVO home) {
		this.home = home;
        home.setGroupName("home");
	}
	
	@Alias( "work")
	public UserUpdateVO getWork() {
		return work;
	}

	public void setWork(UserUpdateVO work) {
		this.work = work;
        work.setGroupName("work");
	}

	/*public Object getShare() {
		return share;
	}

	public void setShare(Object share) {
		this.share = share;
	}*/

	@Alias("first_name")
	private String firstName = "";


	@Alias("last_name")
	private String lastName = "";

	@Alias("middle_name")
	private String middleName = "";

	@Alias("sharing_status")
	private int sharingStatus = 0;

	@Alias("shared_home_fids")
	private String sharedHomeFids = "";

	@Alias("shared_work_fids")
	private String sharedWorkFids = "";

	@Alias("contact_id")
	private int contactId;

	@Alias("id")
	private int id;

	@Alias("is_read")
	private boolean isRead;

	@Alias("is_favorite")
	private boolean isFavorite;

	@Alias("share_limit")
	private int shareLimit;

	@Alias("detected_location")
	private String detectedLocation;


	public void setFirstName(String name){this.firstName = name;}
	public String getFirstName(){return  this.firstName;}

	public void setLastName(String name){this.lastName = name;}
	public String getLastName(){return  this.lastName;}

	public void setMiddleName(String name){this.middleName = name;}
	public String getMiddleName(){return  this.middleName;}

	public void setSharingStatus(int n){this.sharingStatus = n;}
	public int getSharingStatus(){return this.sharingStatus;}

	public void setSharedHomeFids(String homeFids){this.sharedHomeFids = homeFids;}
	public String getSharedHomeFids(){return this.sharedHomeFids;}

	public void setSharedWorkFids(String workFids){this.sharedWorkFids = workFids;}
	public String getSharedWorkFids(){return this.sharedWorkFids;}

	public void setContactId(int id){this.contactId = id;}
	public int getContactId(){return  this.contactId;}

	public void setId(int _id){this.id = _id;}
	public int getId(){return this.id;}

	public void setRead(boolean _isRead) {this.isRead = _isRead;}
	public boolean isRead() {return this.isRead;}

	public void setFavorite(boolean _isFavorite) {this.isFavorite = _isFavorite;}
	public boolean isFavorite() {return this.isFavorite;}

	public void setShareLimit(int limit){this.shareLimit = limit;}
	public int getShareLimit(){return this.shareLimit;}

	public void setDetectedLocation(String _detect) {this.detectedLocation = _detect;}
	public String getDetectedLocation() {return this.detectedLocation;}
}
