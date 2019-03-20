package com.ginko.vo;

import com.sz.util.json.Alias;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ImBoardVO implements Serializable {
	@Alias("board_id")
	private int boardId;

	@Alias("board_name")
	private String boardName;

	@Alias("created_by")
	private int createdBy;
	
//	@XmlJavaTypeAdapter(type = java.util.Date.class, value = DateAdapter.class)
	@Alias("created_time")
	private Date createdTime;
//	@XmlJavaTypeAdapter(type = java.util.Date.class, value = DateAdapter.class)
	@Alias("last_active_time")
	private Date lastActiveTime;
	
	private List<ImBoardMemeberVO> members = new ArrayList<ImBoardMemeberVO>();
	@Alias("recent_messages")
	private List<ImMessageVO> recentMessages =  new ArrayList<ImMessageVO>();

	@Alias("profile_image")
	private String profileImage;

	@Alias("is_group")
	private boolean isGroup;

	private boolean isVideoChat;
	private List<EventUser> lstUsers = new ArrayList<>();

	public int getBoardId() {
		return boardId;
	}

	public void setBoardId(int boardId) {
		this.boardId = boardId;
	}

	public String getBoardName() {return boardName;}

	public void setBoardName(String _boardName) {this.boardName = _boardName;}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(Date lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public List<ImBoardMemeberVO> getMembers() {
		return members;
	}

	public void setMembers(List<ImBoardMemeberVO> members) {
		this.members = members;
	}

	public List<ImMessageVO> getRecentMessages() {
		return recentMessages;
	}

	public void setRecentMessages(List<ImMessageVO> recentMessages) {
		this.recentMessages = recentMessages;
	}

	public String getProfileImage() { return profileImage; }

	public void setProfileImage(String _image) { this.profileImage = _image; }

	public boolean isGroup() {return isGroup;}

	public void setGroup(boolean _group) {this.isGroup = _group; }

	public void setMemberList(HashMap<Integer, ImBoardMemeberVO> members)
	{
		this.members = new ArrayList<ImBoardMemeberVO>(members.values());
	}

	public boolean isVideoChat() {return isVideoChat;}
	public void setVideoChat(boolean _data) {this.isVideoChat = _data;}

	public List<EventUser> getLstUsers() {return lstUsers;}
	public void setLstUsers(ArrayList<EventUser> _data) {lstUsers = _data;}


}
