package com.ginko.vo;

import java.io.Serializable;
import java.util.Date;

import com.sz.util.json.Alias;


public class EntitySimpleVO implements Serializable {

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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name==null?"":name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription(){return description == null?"":this.description;}
	public void setDescription(String description){this.description = description;}

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


	public String getProfileImage() {
		return profileImage == null?"":profileImage;
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
}
