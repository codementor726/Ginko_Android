package com.ginko.vo;

import java.io.Serializable;

public class GroupVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;

	private int group_id;

	private String name;

	private int userId;

	private int user_count;

	private long order_weight;

	private int type;

	private String profile_image;

	private int position;

    public GroupVO() {
    }

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGroup_id() { return this.group_id; }

	public void setGroup_id(int _gId) { this.group_id = _gId;}

	public void setUser_count(int count){
		this.user_count = count;
	}

	public void setOrder_weight(long order)
	{
		this.order_weight = order;
	}

	public void setType(int typeId) {
		this.type = typeId;
	}

	public int getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUserId() {
		return this.userId;
	}

	public int getUserCount() {
		return this.user_count;
	}

	public long getOrder_weight()
	{
		return this.order_weight;
	}

	public void setUserId(int userRef) {
		this.userId = userRef;
	}

	public String getProfile_image() {return this.profile_image;}

	public void setProfile_image(String _image) {this.profile_image = _image;}

	public int getPosition() { return this.position; }

	public void setPosition(int Pos) {this.position = Pos;}
}