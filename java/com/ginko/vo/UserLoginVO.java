package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;
import java.util.Date;

public class UserLoginVO implements Serializable{
	@Alias("sessionId")
	private String sessionId;

	@Alias("allow_see_free_space")
	private boolean allowSeeFreeSpace;

	@Alias("country")
	private String country;

	@Alias("current_group_id")
	private int currentGroupId;

	@Alias("deactivated")
	private boolean deactivated;

	@Alias("gender")
	private int gender;

	@Alias("lang")
	private String lang = "en";

	@Alias("online")
	private boolean online;

	@Alias("see_contacts_dates")
	private boolean seeContactsDates;

	@Alias("setup_page")
	private String setupPage = "";

	@Alias("share_limit")
	private int shareLimit = 0;

	@Alias("sharing_status")
	private int sharingStatus = 3;

	@Alias("status")
	private String status = "";

	@Alias("timezone")
	private String timezone = "(UTC+00:00) Europe/London";

	@Alias("location_on")
	private boolean locationOn; // Must change to 0 or 1?

	@Alias("chat_msg_notification")
	private boolean imNotifyEnable = true;

	@Alias("exchange_request_notification")
	private boolean exchangeRequestNotifyEnable = true;

	@Alias( "sprout_notification")
	private boolean sproutNotifyEnable = true;

	@Alias( "phone_verified")
	private boolean phoneVerified = false;

	@Alias("sync_timestamp")
	private long syncTimestamp;

	@Alias( "user_id")
	private int userId;

	@Alias("qrcode")
	private String qrCodeUrl;

	@Alias( "registration")
	private Date joinTime;

	@Alias( "email")
	private String loginName;

	@Alias(  "photo_url")
	private String photoUrl;

	@Alias(  "first_name")
	private String firstName;

	@Alias(  "last_name")
	private String lastName;

	@Alias(  "middle_name")
	private String middleName;

	@Alias(  "user_name")
	private String userName;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getQrCodeUrl(){
		return this.qrCodeUrl==null?"":this.qrCodeUrl;}
	public void setQrCodeUrl(String qrcode){
		this.qrCodeUrl = qrcode;}

	public Date getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getFirstName() {
		return firstName==null?"":firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName==null?"":lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName==null?"":middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getUserName() {
		return userName==null?"":userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean getAllowSeeFreeSpace() {
		return allowSeeFreeSpace;
	}

	public void setAllowSeeFreeSpace(boolean allowSeeFreeSpace) {
		this.allowSeeFreeSpace = allowSeeFreeSpace;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getCurrentGroupId() {
		return currentGroupId;
	}

	public void setCurrentGroupId(int currentGroupId) {
		this.currentGroupId = currentGroupId;
	}

	public boolean getDeactivated() {
		return deactivated;
	}

	public void setDeactivated(boolean deactivated) {
		this.deactivated = deactivated;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public boolean getOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean getSeeContactsDates() {
		return seeContactsDates;
	}

	public void setSeeContactsDates(boolean seeContactsDates) {
		this.seeContactsDates = seeContactsDates;
	}

	public String getSetupPage() {
		return setupPage;
	}

	public void setSetupPage(String setupPage) {
		this.setupPage = setupPage;
	}

	public int getShareLimit() {
		return shareLimit;
	}

	public void setShareLimit(int shareLimit) {
		this.shareLimit = shareLimit;
	}

	public int getSharingStatus() {
		return sharingStatus;
	}

	public void setSharingStatus(int sharingStatus) {
		this.sharingStatus = sharingStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public boolean getLocationOn() {
		return locationOn;
	}

	public void setLocationOn(boolean locationOn) {
		this.locationOn = locationOn;
	}

	public boolean getImNotifyEnable() {
		return imNotifyEnable;
	}

	public void setImNotifyEnable(boolean imNotifyEnable) {
		this.imNotifyEnable = imNotifyEnable;
	}

	public boolean getExchangeRequestNotifyEnable() {
		return exchangeRequestNotifyEnable;
	}

	public void setExchangeRequestNotifyEnable(
			boolean exchangeRequestNotifyEnable) {
		this.exchangeRequestNotifyEnable = exchangeRequestNotifyEnable;
	}

	public boolean getSproutNotifyEnable() {
		return sproutNotifyEnable;
	}

	public void setSproutNotifyEnable(boolean sproutNotifyEnable) {
		this.sproutNotifyEnable = sproutNotifyEnable;
	}


	public boolean getPhoneVerified() {
		return phoneVerified;
	}

	public void setPhoneVerified(boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	public long getSyncTimestamp(){return this.syncTimestamp;}
	public void setSyncTimestamp(long sync_timestamp){this.syncTimestamp = sync_timestamp;}
}
