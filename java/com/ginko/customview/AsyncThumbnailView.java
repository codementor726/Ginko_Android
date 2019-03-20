package com.ginko.customview;

import com.ginko.common.AppUtility;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

/**
 * Custom image view that shows a progress indicator until a bitmap is attached to it.
 */

public class AsyncThumbnailView extends FrameLayout {

	protected FrameLayout thumbFrame;
	private ProgressBar backProgress;
	protected ImageView thumbnailImage;
	
	protected int 	thumbSizeX = 0;
	protected int	thumbSizeY = 0;
	protected int	thumbMargin = 0;
	protected boolean 	rounded;
	protected boolean 	hasImage;
	
	protected String photoSource;
	
	public AsyncThumbnailView(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		
		this.thumbSizeX = LayoutParams.FILL_PARENT;
		this.thumbSizeY = LayoutParams.FILL_PARENT;
		this.thumbMargin = 0;
		this.hasImage = false;
		this.rounded = false;
		
		create();
	}

	public AsyncThumbnailView(Context context) {
		
		super(context);
		
		this.thumbSizeX = LayoutParams.FILL_PARENT;
		this.thumbSizeY = LayoutParams.FILL_PARENT;
		this.thumbMargin = 0;
		this.hasImage = false;
		this.rounded = false;
		
		create();
	}

	public AsyncThumbnailView(Context context, int thumbSize, int thumbMargin, boolean rounded) {
		
		super(context);
		
		this.thumbSizeX = thumbSize;
		this.thumbSizeY = thumbSize;
		this.thumbMargin = thumbMargin;
		this.hasImage = false;
		this.rounded = rounded;
		
		create();
	}

	public AsyncThumbnailView(Context context, int thumbSizeX, int thumbSizeY, int thumbMargin, boolean rounded) {
		
		super(context);
		
		this.thumbSizeX = thumbSizeX;
		this.thumbSizeY = thumbSizeY;
		this.thumbMargin = thumbMargin;
		this.hasImage = false;
		this.rounded = rounded;
		
		create();
	}

	protected void create() {
		
		thumbFrame = new FrameLayout(getContext());
		
		FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(thumbSizeX, thumbSizeY);
		frameParams.setMargins(thumbMargin, thumbMargin, thumbMargin, thumbMargin);
		frameParams.gravity = Gravity.CENTER;
		
		backProgress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
		backProgress.setIndeterminate(true);
		
		FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progressParams.gravity = Gravity.CENTER;
		
		thumbnailImage = new ImageView(getContext());
		thumbnailImage.setScaleType(ScaleType.FIT_CENTER);
		thumbnailImage.setVisibility(View.GONE);
		FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		imageParams.gravity = Gravity.CENTER;
		
		thumbFrame.addView(backProgress, progressParams);
		thumbFrame.addView(thumbnailImage, imageParams);
		
		addView(thumbFrame, frameParams);
	}
	
	public void setLoadingIndicator(boolean loading) {
		
		backProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
		thumbnailImage.setVisibility(loading ? View.GONE : View.VISIBLE);
	}
	
	public void setImage(Bitmap image) {
		
		hasImage = image != null;
		
		if (rounded) {
			
			Bitmap roundedBitmap = AppUtility.getRoundedBitmap(image);
			thumbnailImage.setImageBitmap(roundedBitmap);
		} else {
			
			thumbnailImage.setImageBitmap(image);
		}
		
		setLoadingIndicator(false);
	}

	public void setImage(int resource) {
		
		hasImage = true;
		thumbnailImage.setImageResource(resource);
		setLoadingIndicator(false);
	}

	public void setScaleType(ScaleType st) {
		
		thumbnailImage.setScaleType(st);
	}
	
	public boolean hasImage() {
		
		return hasImage;
	}
	
	public void setRound(boolean value) {
		
		rounded = value;
	}

}