package com.ginko.vo;

import java.io.Serializable;
import java.util.Date;

import com.sz.util.json.Alias;

public class ImBoardMemeberVO implements Serializable{

	private Date joinTime;
	@Alias("memberinfo")
	private EventUser user;

	@Alias("is_left")
	private boolean leave;

	@Alias("left_time")
	private Date leaveTime;

	@Alias("is_friend")
	private boolean friend;

	@Alias("in_same_directory")
	private boolean inDirectory;

	@Alias("id")
	public Integer memberId;

	@Alias("name")
	public String name;

	@Alias("profile_image")
	public String profileImage;

	public boolean chatOnly;

	public Date getJoinTime() {
		return joinTime;
	}


	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}


	public EventUser getUser() {
		return user;
	}

	public boolean isFriend() {return friend; }

	public void setUser(EventUser user) {
		this.user = user;
	}


	public boolean isLeave() {
		return leave;
	}


	public void setLeave(boolean leave) {
		this.leave = leave;
	}

	public void setFriend(boolean friend) { this.friend = friend; }

	public boolean isInDirectory() {return inDirectory; }

	public void setInDirectory(boolean _inDirec) {this.inDirectory = _inDirec; }

	public Date getLeaveTime() {
		return leaveTime;
	}


	public void setLeaveTime(Date leaveTime) {
		this.leaveTime = leaveTime;
	}

	public Integer getMemberId() { return memberId; }
	public String getName() { return this.name; }
	public String getProfileImage() {return this.profileImage; }

	public void setMemberId(Integer _id) { this.memberId = _id; }
	public void setName(String _name) { this.name = _name; }
	public void setProfileImage(String _image) { this.profileImage = _image; }

	public Boolean isChatOnly(){ return chatOnly;}
	public void setChatOnly(Boolean _chatOnly){ this.chatOnly = _chatOnly;}
}
