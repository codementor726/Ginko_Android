package com.ginko.vo;

import java.util.ArrayList;
import java.util.List;

import com.sz.util.json.Alias;


public class EntityVO extends EntitySimpleVO{

	@Alias(value = "video_url", ignoreGet= true)
	private String video;

	@Alias(value = "video_thumbnail_url", ignoreGet= true)
	private String videoThumbUrl;

	@Alias(value = "infos")
	private List<EntityInfoVO> entityInfos;

	@Alias(value = "delete_info_ids")
	private String deleteIds;

	@Alias(value = "images")
	private List<EntityImageVO> entityImages;

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getVideoThumbUrl(){return videoThumbUrl == null?"": this.videoThumbUrl;}
	public void setVideoThumbUrl(String videoThumbUrl){this.videoThumbUrl = videoThumbUrl;}

	public String getDeleteIds() {
		return deleteIds;
	}

	public void setDeleteIds(String _Ids) {
		this.deleteIds = _Ids;
	}

	@Alias("infos")
	public List<EntityInfoVO> getEntityInfos() {
		if (this.entityInfos == null){
			entityInfos = new ArrayList<EntityInfoVO>();
		}
		return entityInfos;
	}


	public void setEntityInfos(List<EntityInfoVO> entityInfos) {
		this.entityInfos = entityInfos;
	}

	public List<EntityImageVO> getEntityImages() {
		if(entityImages == null)
            entityImages = new ArrayList<EntityImageVO>();
        return entityImages;
	}

	@Alias(ignoreGet =  true , ignoreSet = true)
	public EntityImageVO getWallpapaerImage()
	{
		EntityImageVO wallpaperImage = null;
		if(entityImages == null || entityImages.size() == 0) return wallpaperImage;

		for(int i=0;i<entityImages.size();i++)
		{
			if(entityImages.get(i).getZIndex() == 0)//background image is changed to wallpaper
			{
				wallpaperImage = entityImages.get(i);
				break;
			}
		}

		return wallpaperImage;
	}

	public void setEntityImages(List<EntityImageVO> entityImages) {
		this.entityImages = entityImages;
	}

}
