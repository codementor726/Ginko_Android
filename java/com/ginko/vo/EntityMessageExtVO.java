package com.ginko.vo;

import com.sz.util.json.Alias;

public class EntityMessageExtVO extends EntityMessageVO {
	@Alias("entity_id")
	private int entityId;
	@Alias("entity_name")
	private String entityName;
	@Alias("profile_image")
	private String profileImage;

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
}
