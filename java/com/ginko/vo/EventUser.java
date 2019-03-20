package com.ginko.vo;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.sz.util.json.Alias;

public class EventUser implements Serializable {
	@Alias("user_id")
	private int userId;

	@Alias("fname")
	private String firstName;

	@Alias("lname")
	private String lastName;

	@Alias("mname")
	private String middleName;

	@Alias("photo_url")
	private String photoUrl;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getFullName(){
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotBlank(this.getFirstName())){
			sb.append(this.getFirstName());
		}
		if (StringUtils.isNotBlank(this.getMiddleName())){
			sb.append(" ").append(this.getMiddleName());
		}
		if (StringUtils.isNotBlank(this.getLastName())){
			sb.append(" ").append(this.getLastName());
		}
		return sb.toString();
	}
}
