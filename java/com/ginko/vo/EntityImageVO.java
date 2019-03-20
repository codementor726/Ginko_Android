package com.ginko.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.sz.util.json.Alias;

public class EntityImageVO implements Serializable {
	@Alias("image_id")
	private Integer id;

	@Alias("height")
	private Float height;

	@Alias("left")
	private Float left;

	@Alias("top")
	private Float top;

	@Alias ( "updated_at")
	private Timestamp updatedAt;

	@Alias("image_url")
	private String url;

	@Alias("width")
	private Float width;

	@Alias("z_index")
	private int zIndex;



	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Float getHeight() {
		return height;
	}

	public void setHeight(Float height) {
		this.height = height;
	}

	public Float getLeft() {
		return left;
	}

	public void setLeft(Float left) {
		this.left = left;
	}

	public Float getTop() {
		return top;
	}

	public void setTop(Float top) {
		this.top = top;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUrl() {
		return url==null?"":url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}

	public int getZIndex() {
		return zIndex;
	}

	public void setZIndex(int zIndex) {
		this.zIndex = zIndex;
	}
}
