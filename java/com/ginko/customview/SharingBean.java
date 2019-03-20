package com.ginko.customview;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class SharingBean implements Serializable{
	private int sharingStatus;
	private String sharedHomeFieldIds="";
	private String sharedWorkFieldIds="";

	public boolean sharedAll() {
		return this.getSharingStatus() == 3
				&& StringUtils.isBlank(this.getSharedHomeFieldIds())
				&& StringUtils.isBlank(this.getSharedWorkFieldIds());
	}

	public int getSharingStatus() {
		return sharingStatus;
	}

	public void setSharingStatus(int sharingStatus) {
		this.sharingStatus = sharingStatus;
	}

	public String getSharedHomeFieldIds() {
		return sharedHomeFieldIds;
	}

	public void setSharedHomeFieldIds(String sharedHomeFieldIds) {
		this.sharedHomeFieldIds = sharedHomeFieldIds;
	}

	public String getSharedWorkFieldIds() {
		return sharedWorkFieldIds;
	}

	public void setSharedWorkFieldIds(String sharedWorkFieldIds) {
		this.sharedWorkFieldIds = sharedWorkFieldIds;
	}
	
	public boolean isShareAllHome(){
		return this.getSharingStatus() == 1
				&& StringUtils.isBlank(this.getSharedHomeFieldIds());
	}
	
	public boolean isSharedAllWork (){
		return this.getSharingStatus() == 2
				&& StringUtils.isBlank(this.getSharedWorkFieldIds());
	}
}
