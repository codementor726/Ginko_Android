package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;
import java.util.Date;

public class BaseUserVO implements Serializable {

	@Alias( "user_id")
	private int userId;

	@Alias( "registration")
	private Date joinTime;

	@Alias( "email")
	private String loginName;

	@Alias(  "photo_url")
	private String photoUrl;
	
	@Alias(  "fname")
	private String firstName;

	@Alias(  "lname")
	private String lastName;

	@Alias(  "mname")
	private String middleName;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

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

	public String getFullName()
	{
		String strFullName = getFirstName();
		if(getMiddleName() != null && !getMiddleName().equals(""))
			strFullName = strFullName + " "+ getMiddleName();
		if(getLastName() != null && !getLastName().equals(""))
			strFullName = strFullName + " "+ getLastName();

		return strFullName;
	}

}
