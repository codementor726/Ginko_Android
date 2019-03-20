package com.ginko.vo;

import com.sz.util.json.Alias;

import java.io.Serializable;
import java.security.Timestamp;

public class TcVideoVO implements Serializable {
    @Alias("id")
	private int id;

	@Alias("current_used")
	private boolean currentUse;

    @Alias("type")
	private byte type;

	@Alias("uploaded_at")
	private Timestamp uploadedAt;

    @Alias("video_url")
	private String video_url;

    @Alias("thumbnail_url")
    private String thumbUrl;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte getType() {
		return this.type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public Timestamp getUploadedAt() {
		return this.uploadedAt;
	}

	public void setUploadedAt(Timestamp uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public String getVideo_url() {
		return this.video_url==null?"":this.video_url;
	}

	public void setVideo_url(String url) {
		this.video_url = url;
	}

    public String getThumbUrl(){return this.thumbUrl==null?"":this.thumbUrl;}
    public void setThumbUrl(String url){this.thumbUrl = url;}

	public boolean isCurrentUse() {
		return currentUse;
	}

	public void setCurrentUse(boolean currentUse) {
		this.currentUse = currentUse;
	}


}
