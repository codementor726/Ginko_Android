package com.videophotofilter.android.com;

import android.app.Activity;
import android.content.Context;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

import com.ginko.vo.EntityImageVO;
import com.ginko.vo.TcImageVO;
import com.videophotofilter.library.android.com.AspectFrameLayout;
import com.videophotofilter.library.android.com.ImageUtil;

public class ArchiveItemView extends LinearLayout {

	private Context mContext;
	private ArchiveMediaItem mItem;
	private LayoutInflater inflater;

    private AbsoluteLayout thumbLayout;

	private CustomNetworkImageView imgThumb;//background view
    private CustomNetworkImageView frontView;

    private ImageLoader imgLoader;

    private float ratio = 1.0f;

    private int archiveWidth = 100 , archiveHeight = 100;

	public ArchiveItemView(Context context , ArchiveMediaItem item , float _ratio)
	{	
		super(context);
		
		this.mContext = context;
		this.mItem = item;
        this.ratio = _ratio;

        archiveWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.archive_image_width);
        archiveHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.archive_image_height);

        float r1 = (float)archiveWidth/480;
        float r2 = (float)archiveHeight/320;

        if(r2>r1)
            ratio = r2;
        else
            ratio = r1;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.archive_item, this , true);

        thumbLayout = (AbsoluteLayout)findViewById(R.id.thumbLayout);
		imgThumb = (CustomNetworkImageView)findViewById(R.id.imgThumb);

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        addFrontView();

        refreshView();
	}

    private void addFrontView() {
        if (frontView != null) return;
        //foreground photo
        if (mItem.archiveType == ConstValues.HOME_PHOTO_EDITOR || mItem.archiveType == ConstValues.WORK_PHOTO_EDITOR)
        {
            if (mItem.userImages != null && mItem.userImages.size() > 1)
            {
                TcImageVO imgInfo = null;
                for (int i = 0; i < mItem.userImages.size(); i++) {
                    if (mItem.userImages.get(i).getZIndex() > 0) {
                        imgInfo = mItem.userImages.get(i);
                        break;
                    }
                }
                if (imgInfo == null)
                    return;
                String foregroundPhotoUrl = imgInfo.getUrl();
                CustomNetworkImageView _frontView = new CustomNetworkImageView(mContext);
                _frontView.setAdustImageAspect(false);
                _frontView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
                _frontView.setImageUrl(foregroundPhotoUrl, imgLoader);

                Float width = imgInfo.getWidth();
                Float height = imgInfo.getHeight();
                Float x = imgInfo.getLeft();
                Float y = imgInfo.getTop();

                //if width , height or x,y are not specified , then it means full layout with some padding from background photo
                if (width != null && height != null && x != null && y != null) {
                    int nWidth = Float.valueOf(width * ratio).intValue();
                    int nHeight = Float.valueOf(height * ratio).intValue();
                    int nX = Float.valueOf(x * ratio).intValue();
                    int nY = Float.valueOf(y * ratio).intValue();
                    System.out.println("----(" + nX + "," + nY + ") - (" + nWidth + "," + nHeight + ")----");

                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    layoutParams.height = AbsoluteLayout.LayoutParams.WRAP_CONTENT;
                    thumbLayout.addView(_frontView, layoutParams);
                } else
                {
                    DisplayMetrics dm = Uitils.getResolution((Activity) mContext);

                    int nXPadding = (int) (archiveWidth * 0.30);
                    int nYPadding = (int) (archiveHeight * 0.30);

                    int nWidth = archiveWidth - nXPadding * 2;
                    int nHeight = archiveHeight - nYPadding * 2;
                    int nX = nXPadding;
                    int nY = nYPadding;
                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    thumbLayout.addView(_frontView, layoutParams);
                }

                this.frontView = _frontView;
            }
        }
        else if(mItem.archiveType == ConstValues.ENTITY_PHOTO_EDITOR)
        {
            if (mItem.entityImages != null && mItem.entityImages.size() > 1)
            {
                EntityImageVO imgInfo = mItem.entityImages.get(1);
                for(int i=0;i<mItem.entityImages.size();i++)
                {
                    if(mItem.entityImages.get(i).getZIndex() >0)
                    {
                        imgInfo = mItem.entityImages.get(i);
                        break;
                    }
                }
                if (imgInfo == null)
                    return;
                String foregroundPhotoUrl = imgInfo.getUrl();
                CustomNetworkImageView _frontView = new CustomNetworkImageView(mContext);
                _frontView.setAdustImageAspect(false);
                _frontView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
                _frontView.setImageUrl(foregroundPhotoUrl, imgLoader);

                Float width = imgInfo.getWidth();
                Float height = imgInfo.getHeight();
                Float x = imgInfo.getLeft();
                Float y = imgInfo.getTop();

                //if width , height or x,y are not specified , then it means full layout with some padding from background photo
                if (width != null && height != null && x != null && y != null) {
                    int nWidth = Float.valueOf(width * ratio).intValue();
                    int nHeight = Float.valueOf(height * ratio).intValue();
                    int nX = Float.valueOf(x * ratio).intValue();
                    int nY = Float.valueOf(y * ratio).intValue();
                    System.out.println("----(" + nX + "," + nY + ") - (" + nWidth + "," + nHeight + ")----");

                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    layoutParams.height = AbsoluteLayout.LayoutParams.WRAP_CONTENT;
                    thumbLayout.addView(_frontView, layoutParams);
                } else {
                    DisplayMetrics dm = Uitils.getResolution((Activity) mContext);

                    int nXPadding = (int) (archiveWidth * 0.30);
                    int nYPadding = (int) (archiveHeight * 0.30);

                    int nWidth = archiveWidth - nXPadding * 2;
                    int nHeight = archiveHeight - nYPadding * 2;
                    int nX = nXPadding;
                    int nY = nYPadding;
                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    thumbLayout.addView(_frontView, layoutParams);
                }

                this.frontView = _frontView;
            }
        }
    }

	public void setItem(ArchiveMediaItem item)
	{
		this.mItem = item;
	}
	
	public void refreshView()
	{
        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(mItem.archiveType == 1 || mItem.archiveType == 2) {
            if(mItem.isDefaultItem)
            {
                if(mItem.archiveType ==  1)
                {
                    if(mItem.isNewItem)
                        imgThumb.setDefaultImageResId(R.drawable.img_novideohome);
                    else
                        imgThumb.setDefaultImageResId(R.drawable.img_novideohome_green);
                    imgThumb.setImageUrl("" , imgLoader);
                }
                else
                {
                    if(mItem.isNewItem)
                        imgThumb.setDefaultImageResId(R.drawable.img_novideohome);
                    else
                        imgThumb.setDefaultImageResId(R.drawable.img_novideohome_green);
                    imgThumb.setImageUrl("" , imgLoader);
                }
            }
            else
            {
                if(mItem.userImages.size()>0 && mItem.mediaType == 1) {
                    imgThumb.setImageUrl(mItem.userImages.get(0).getUrl(), imgLoader);
                }
                if(mItem.thumbImages.size()>0 && mItem.mediaType == 0) {
                    imgThumb.setImageUrl(mItem.thumbImages.get(0).getThumbUrl(), imgLoader);
                }
            }

        }
        else if(mItem.archiveType == 3)
        {
            if(mItem.isDefaultItem)
            {
                if(mItem.isNewItem)
                    imgThumb.setDefaultImageResId(R.drawable.img_novideohome);
                else
                    imgThumb.setDefaultImageResId(R.drawable.img_novideohome_green);
                imgThumb.setImageUrl("" , imgLoader);
            }
            else {
                if (mItem.entityImages.size() > 0) {
                    imgThumb.setImageUrl(mItem.entityImages.get(0).getUrl(), imgLoader);
                }
            }
        }

        addFrontView();
	}

	private class ThumbLoadThread extends Thread
	{
		public ThumbLoadThread()
		{
			
		}
		@Override
		public void run()
		{
			//load photo
			if(mItem.mediaType == ArchiveMediaItem.PHOTO_MEDIAT_TYPE)
			{
				try
				{
					// ImageUtil.decodeSampledBitmapFromImageFile(mItem.filePath, 100,  100);
					((TradeCardPhotoEditorSetActivity)mContext).mHandler.sendEmptyMessage(0);
				}catch(Exception e){e.printStackTrace();}
				finally
				{
				}
			}
			//load video thumb
			else
			{
				try{
					//ThumbnailUtils.createVideoThumbnail(mItem.filePath, Thumbnails.MICRO_KIND);
				}catch(Exception e){e.printStackTrace();}
				finally
				{
				}
			}
			
		}
	}

}
