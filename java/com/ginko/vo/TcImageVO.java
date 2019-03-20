package com.ginko.vo;

import java.io.Serializable;
import java.sql.Timestamp;

import com.sz.util.json.Alias;

public class TcImageVO implements Serializable{

	private Integer id;

	private Float height;

	private Float left;

	private Float top;

	private byte type;

	@Alias("updated_at")
	private Timestamp updatedAt;

	private Float width;

	@Alias("z_index")
	private byte zIndex;

	@Alias("image_url")
	private String url;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Float getHeight() {
		return this.height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Float getLeft() {
		return this.left;
	}

	public void setLeft(Float left) {
		this.left = left;
	}

	public Float getTop() {
		return this.top;
	}

	public void setTop(Float top) {
		this.top = top;
	}

	public byte getType() {
		return this.type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public Timestamp getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUrl() {
		return this.url==null?"":this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Float getWidth() {
		return this.width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}

	public byte getZIndex() {
		return this.zIndex;
	}

	public void setZIndex(byte zIndex) {
		this.zIndex = zIndex;
	}

}
