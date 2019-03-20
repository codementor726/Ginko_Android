package com.ginko.vo;

import java.io.Serializable;
import java.util.Date;

import com.sz.util.json.Alias;

public class CbEmailVO implements Serializable {
	private Integer id;
	
	private String password;

	private String oauthtoken;
	
	private String active;

	private String email;

	@Alias("last_update")
	private Date lastUpdate;

	@Alias("share_limit")
	private int shareLimit;

	@Alias("sharing_status")
	private int sharingStatus;

	@Alias("username")
	private String username;

	@Alias("shared_home_fids")
	private String sharedHomeFieldIds = "";

	@Alias("shared_work_fids")
	private String sharedWorkFieldIds = "";

	private String valid;

	@Alias("provider")
	private String provider;

	@Alias("auth_type")
	private String authType;

	@Alias("is_ssl")
	private boolean isSsl = false;

	@Alias("server_addr")
	private String serverAddr;

	@Alias("server_port")
	private Integer serverPort;

	@Alias("type")
	private String type;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getValid() {
		return valid;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public Integer getServerPort() {
		return serverPort;
	}

	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOauthtoken() {
		return oauthtoken;
	}

	public void setOauthtoken(String oauthtoken) {
		this.oauthtoken = oauthtoken;
	}

	public boolean isSsl() {
		return isSsl;
	}

	public void setSsl(boolean isSsl) {
		this.isSsl = isSsl;
	}
}
