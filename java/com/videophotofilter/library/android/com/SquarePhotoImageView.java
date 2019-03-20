package com.videophotofilter.library.android.com;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquarePhotoImageView extends ImageView{

	private int width = 0;
	private int height = 0;
	
	private OnViewDrawListener viewListener;
	public void setViewListener(OnViewDrawListener listener)
	{
		this.viewListener = listener;
	}
	public SquarePhotoImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SquarePhotoImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public SquarePhotoImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = MeasureSpec.getSize(widthMeasureSpec);
        this.height = MeasureSpec.getSize(heightMeasureSpec);
        if(this.viewListener != null)
        	this.viewListener.onDrawnView(this , width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public int getViewWidth()
	{
		return this.width;
	}
	public int getViewHeight()
	{
		return this.height;
	}
	
	public interface OnViewDrawListener{
		void onDrawnView(ImageView view , int width , int height);
	}
}
