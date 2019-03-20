package com.videophotofilter.library.android.com;

import java.io.IOException;
import java.io.InputStream;

import com.ginko.ginko.R;
import com.videophotofilter.android.com.MyVideoLab;
import com.videophotofilter.android.com.VideoFilterCore;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VideoThumbnailView extends View{

	private Context mContext;
	
	private Paint 		mThumbFrameBorderPaint;
	private Paint 		mBitmapPaint;
	
	private String 		videoFilePath = "";
	
	private Bitmap 		bitmapLeftMarker = null;
	private Bitmap 		bitmapRightMarker = null;
	private Bitmap 		bitmapTopBar = null;
	private Bitmap 		bitmapBottomBar = null;
	
	private float    	bitmapScaleRate = 1.0f;
	
	private int			nMarkerWidth = 0;
	private int 		nBarHeight = 0;
	private int 		nLeftMarkerPos = 0;
	private int 		nRightMarkerPos = 0;
	private int			nThumbFrameWidth = 0;
	private int 		nMinThumbFrameWidth = 0;
	private int			nMaxThumbFrameWidth = 0;
	private int 		nCurrentControllerWidth = 0;
	
	private int			nThumbWidth = 0;
	private int 		nThumbHeight = 0;
	private int 		nThumbCount = 0;
	
	
	private int			nScreenHeight = 0 , nScreenWidth = 0;
	
	private boolean 	isVideoLoaded = false;
	private int	   		totalFrameCount = 0;
	private int    		maxFrameCount = 0;
	private int    		minFrameCount = 0;
	
	private float		prevX = 0.0f , prevY = 0.0f;
	private boolean		isLeftMarkerClicked = false;
	private boolean		isRightMarkerClicked = false;
	private boolean 	isPanelClicked = false; 
	
	private int 		startTime = 0;
	private int			endTime = 0;
	
	private MyVideoLab	mVideo = null;
	private Bitmap[]	thumbBitmapArray = null;
	
	private Object lockObj = new Object();
	
	private LoadVideThread loadVideoThread = null;
	private LoadThumbnailThread loadThumbThread = null;
	
	private VideoLengthEditChangeListener videoLengthChangeListneer = null;
	
	public VideoThumbnailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		
		mVideo = null;
		
		isVideoLoaded = false;

		mThumbFrameBorderPaint = new Paint();
		mThumbFrameBorderPaint.setAntiAlias(false);
		mThumbFrameBorderPaint.setStyle(Paint.Style.STROKE);
		mThumbFrameBorderPaint.setColor(
            getResources().getColor(R.color.videothumb_frame_border_color));
		mThumbFrameBorderPaint.setStrokeWidth(20);
		
		mBitmapPaint = new Paint();
		mBitmapPaint.setAntiAlias(true);
		mBitmapPaint.setFilterBitmap(true);
		mBitmapPaint.setDither(true);
		
		//load thumb editor bitmaps
		bitmapLeftMarker = getBitmapFromAsset("left_videolengtheditor.png");
		bitmapRightMarker = getBitmapFromAsset("right_videolengtheditor.png");
		bitmapTopBar = getBitmapFromAsset("topbar_videoeditlength.png");
		bitmapBottomBar = getBitmapFromAsset("bottombar_videoeditlength.png");
	}
	
	private Bitmap getBitmapFromAsset(String strAssetName)
	{
		AssetManager assetManager = mContext.getAssets();
		InputStream istr = null;
		try
		{
			istr = assetManager.open(strAssetName);
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(istr);
		return bitmap;
	}
	
	public VideoInfo getVideoInfo()
	{
		if(mVideo == null)
			return null;
		return this.mVideo.videoInfo;
	}
	
	public void setVideoFilePath(String filePath)
	{
		this.videoFilePath = filePath;
		
	}
	
	public void setOnVideoChangeListener(VideoLengthEditChangeListener listener)
	{
		videoLengthChangeListneer = listener;
	}
	
	public void release()
	{
		if(bitmapLeftMarker != null)
		{
			try{
				bitmapLeftMarker.recycle();
			}catch(Exception e){e.printStackTrace();}finally{bitmapLeftMarker = null;}
		}
		if(bitmapRightMarker !=null)
		{
			try{
				bitmapRightMarker.recycle();
			}catch(Exception e){e.printStackTrace();}finally{bitmapRightMarker= null;}
		
		}
		if(bitmapTopBar !=null)
		{
			try{
				bitmapTopBar .recycle();
			}catch(Exception e){e.printStackTrace();}finally{bitmapTopBar = null;}
		
		}
		if(bitmapBottomBar !=null)
		{
			try{
				bitmapBottomBar .recycle();
			}catch(Exception e){e.printStackTrace();}finally{bitmapBottomBar = null;}
		
		}
		
		if(thumbBitmapArray != null)
		{
			for(int i=0;i<nThumbCount;i++)
			{
				if(thumbBitmapArray[i] != null)
				{
					try
					{
						thumbBitmapArray[i].recycle();
					}catch(Exception e){e.printStackTrace();}
					finally{
						thumbBitmapArray[i] = null;
					}
				}
			}
		}			
		thumbBitmapArray = null;

		if(mVideo != null)
			mVideo.closeVideo();
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		if(isVideoLoaded == false)
			return true;
		
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	if(x>=nLeftMarkerPos-nMarkerWidth && x<=nLeftMarkerPos && y>=0 && y<=nScreenHeight)
        	{
        		isLeftMarkerClicked = true;
        		isRightMarkerClicked = false;
            	isPanelClicked = false;
        	}
        	else if(x>=nRightMarkerPos && x<=nRightMarkerPos+nMarkerWidth && y>=0 && y<=nScreenHeight)
        	{
        		isLeftMarkerClicked = false;
        		isRightMarkerClicked = true;
            	isPanelClicked = false;
        	}
        	else if(x>=nLeftMarkerPos && x<=nRightMarkerPos && y>=0 && y<=nScreenHeight)
        	{
        		isLeftMarkerClicked = false;
        		isRightMarkerClicked = false;
            	isPanelClicked = true; 
            	nCurrentControllerWidth = nRightMarkerPos-nLeftMarkerPos;
        	}
        	
            break;
        case MotionEvent.ACTION_MOVE:
        	
        	if(isLeftMarkerClicked)
        	{
        		nLeftMarkerPos += (x-prevX);
        		if(nRightMarkerPos-nLeftMarkerPos > nMaxThumbFrameWidth)
        			nLeftMarkerPos = nRightMarkerPos - nMaxThumbFrameWidth;
        		else if(nRightMarkerPos-nLeftMarkerPos <nMinThumbFrameWidth)
        			nLeftMarkerPos = nRightMarkerPos - nMinThumbFrameWidth;
        	}
        	else if(isRightMarkerClicked)
        	{
        		nRightMarkerPos += (x-prevX);
       		
        		
        		if(nRightMarkerPos>nMarkerWidth+nThumbFrameWidth)
        		{
        			nRightMarkerPos = nMarkerWidth+nThumbFrameWidth;
        		}
        		if(nRightMarkerPos-nLeftMarkerPos> nMaxThumbFrameWidth)
        		{
        			nRightMarkerPos = nMarkerWidth + nMaxThumbFrameWidth;
        		}
        		else if(nRightMarkerPos-nLeftMarkerPos <nMinThumbFrameWidth)
        			nRightMarkerPos = nLeftMarkerPos + nMinThumbFrameWidth;
        	}
        	else if(isPanelClicked)
        	{
        		nLeftMarkerPos += (x-prevX);
        		nRightMarkerPos += (x-prevX);
        		if(nLeftMarkerPos<nMarkerWidth)
            	{
            		nLeftMarkerPos = nMarkerWidth;
            		nRightMarkerPos = nLeftMarkerPos + nCurrentControllerWidth;
            	}
        		else if(nRightMarkerPos>nMarkerWidth+nThumbFrameWidth)
            	{
            		nRightMarkerPos = nMarkerWidth+nThumbFrameWidth;
            		nLeftMarkerPos = nRightMarkerPos - nCurrentControllerWidth;
            	}
        	}
        	
        	
            break;
        case MotionEvent.ACTION_UP:
        	if(isLeftMarkerClicked || isRightMarkerClicked || isPanelClicked)
        	{
        		float startPos = (float)(nLeftMarkerPos-nMarkerWidth)/nThumbFrameWidth;
        		float duration = (float)(nRightMarkerPos - nLeftMarkerPos)/nThumbFrameWidth;
        		startTime = (int) (mVideo.videoInfo.videoLengthInMills*startPos);
        		endTime = (int) (startTime+mVideo.videoInfo.videoLengthInMills*duration);
        		
        		System.out.println("----Start-EndTime( "+String.valueOf(startTime)+" - "+String.valueOf(endTime)+" )----");
        	}
        	        	
        	if(isLeftMarkerClicked || isPanelClicked)
        		videoLengthChangeListneer.onVideoLengthChanged(startTime, endTime, true);
        	else
        		videoLengthChangeListneer.onVideoLengthChanged(startTime, endTime, false);
        	isLeftMarkerClicked = false;
        	isRightMarkerClicked = false;
        	isPanelClicked = false; 
            break;
        }
        
        prevX = x;
        prevY = y;
        
        invalidate();
        
        return true;
    }
	
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw segment
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        
        nScreenWidth = measuredWidth;
        nScreenHeight = measuredHeight;
        
        if(mVideo == null && loadVideoThread == null)
        {
        	loadVideoThread = new LoadVideThread(this);
        	loadVideoThread.start();
        }
        
        synchronized(lockObj)
        {
        	if(!isVideoLoaded) return;
        }
        bitmapScaleRate = measuredHeight/bitmapLeftMarker.getHeight();
        nMarkerWidth = (int)(bitmapLeftMarker.getWidth()*bitmapScaleRate);
        nBarHeight = (int)(bitmapTopBar.getHeight()*bitmapScaleRate);
        nMinThumbFrameWidth = (int)((measuredWidth-nMarkerWidth*2)*((float)VideoFilterCore.MIN_SEGMENT_TIME/VideoFilterCore.MAX_SEGMENT_TIME));
		
        
        //init pos values when rightmarker pos is infinite
        if(nRightMarkerPos == 0)
        {
        	
        	if(totalFrameCount >= maxFrameCount)
        	{	
        		nLeftMarkerPos = nMarkerWidth;
        		nThumbFrameWidth = measuredWidth-nMarkerWidth*2;
        		float frate = (float)maxFrameCount/totalFrameCount;
        		nRightMarkerPos = nLeftMarkerPos + (int)(nThumbFrameWidth*frate);
        		nMaxThumbFrameWidth = nRightMarkerPos-nLeftMarkerPos;
        		
        		nThumbWidth = (measuredWidth-nMarkerWidth*2)/15;
        		nThumbHeight = measuredHeight-20;
        		nThumbCount = 15;
        		
        		thumbBitmapArray = new Bitmap[nThumbCount];
        	}
        	else
        	{

        		nLeftMarkerPos = nMarkerWidth;
        		float frate = (float)totalFrameCount/maxFrameCount;
        		nThumbFrameWidth = (int)((measuredWidth-nMarkerWidth*2)*frate);
        	
        		nThumbWidth = (measuredWidth-nMarkerWidth*2)/15;
        		nThumbHeight = measuredHeight-20;
        		
        		nThumbCount = nThumbFrameWidth/nThumbWidth;
        		if(nThumbCount < 1) nThumbCount+=1;
        		
        		nThumbFrameWidth = nThumbCount*nThumbWidth;
        		
        		nRightMarkerPos = nLeftMarkerPos + nThumbFrameWidth;
        		nMaxThumbFrameWidth = nRightMarkerPos-nLeftMarkerPos;
        		
        		thumbBitmapArray = new Bitmap[nThumbCount];
        	}
        	
        	//load thumbnail thread
        	if(loadThumbThread == null)
        	{
        		loadThumbThread = new LoadThumbnailThread(this);
        		loadThumbThread.start();		
        	}
        	
        }
        
        //draw video thumbnail frame and thumb bitmaps
        canvas.drawRect(new Rect(nMarkerWidth-10 , 0 , nMarkerWidth+nThumbFrameWidth-10, measuredHeight), mThumbFrameBorderPaint);
        
        for(int i=0;i<nThumbCount;i++)
        {
        	if(thumbBitmapArray[i]!=null)
        		canvas.drawBitmap(thumbBitmapArray[i],new Rect(0, 0 ,nThumbWidth , nThumbHeight),
        			new Rect(nMarkerWidth + i*nThumbWidth , 10 , nMarkerWidth + (i+1)*nThumbWidth , measuredHeight-10)
        			,mThumbFrameBorderPaint);
        }
        
        //draw markers
        Rect source = new Rect();
        Rect dst = new Rect();
        
        source.left = 0; source.top = 0; source.right = bitmapLeftMarker.getWidth(); source.bottom = bitmapLeftMarker.getHeight();
        dst .left = nLeftMarkerPos-nMarkerWidth; dst .top = 0; dst.right = nLeftMarkerPos; dst.bottom = measuredHeight;
        canvas.drawBitmap(bitmapLeftMarker, source, dst, mBitmapPaint);
        
        source.left = 0; source.top = 0; source.right = bitmapRightMarker.getWidth(); source.bottom = bitmapRightMarker.getHeight();
        dst .left = nRightMarkerPos; dst .top = 0; dst.right = nRightMarkerPos+nMarkerWidth; dst.bottom = measuredHeight;
        canvas.drawBitmap(bitmapRightMarker, source, dst, mBitmapPaint);
        
        source.left = 0; source.top = 0; source.right = bitmapTopBar.getWidth(); source.bottom = bitmapTopBar.getHeight();
        dst.left = nLeftMarkerPos; dst .top = 0; dst.right = nRightMarkerPos; dst.bottom = nBarHeight;
        canvas.drawBitmap(bitmapTopBar, source, dst, mBitmapPaint);
        
        source.left = 0; source.top = 0; source.right = bitmapBottomBar.getWidth(); source.bottom = bitmapBottomBar.getHeight();
        dst.left = nLeftMarkerPos; dst .top = measuredHeight-nBarHeight; dst.right = nRightMarkerPos; dst.bottom = measuredHeight;
        canvas.drawBitmap(bitmapBottomBar, source, dst, mBitmapPaint);
        
    }
    
    class LoadVideThread extends Thread
    {
    	private VideoThumbnailView thumbView;
    	public LoadVideThread(VideoThumbnailView view)
    	{
    		thumbView = view;
    		mVideo = new MyVideoLab(mContext , videoFilePath);
    	}
    	
    	@Override
    	public void run()
    	{
    		synchronized(lockObj)
    		{
    			System.out.println("---Open Video File Call---");
    			
    			if(mVideo == null) return;
    			
    			System.out.println("---Open Video File Call Finished---");
    			System.out.println("----Video Info width = "+String.valueOf(mVideo.videoInfo.videoWidth)+" , height = "+String.valueOf(mVideo.videoInfo.videoHeight));
    			System.out.println("----Video Info TotalTimeInMills = "+String.valueOf(mVideo.videoInfo.videoLengthInMills));
    			System.out.println("----Video Info FrameRate = "+String.valueOf(mVideo.videoInfo.videoFrameRate));
    			

    			totalFrameCount = mVideo.videoInfo.videoFrameRate*mVideo.videoInfo.videoLengthInMills/1000;
    			maxFrameCount = mVideo.videoInfo.videoFrameRate*VideoFilterCore.MAX_SEGMENT_TIME;
    			minFrameCount = mVideo.videoInfo.videoFrameRate*VideoFilterCore.MIN_SEGMENT_TIME;
    			//totalFrameCount = mVideo.videoInfo.videoLengthInMills;
    			//maxFrameCount = VideoFilterCore.MAX_SEGMENT_TIME * 1000;
    			//minFrameCount = VideoFilterCore.MIN_SEGMENT_TIME * 1000;
    			
    			
    			startTime = 0;
    			if(mVideo.videoInfo.videoLengthInMills>=mVideo.videoInfo.videoFrameRate*VideoFilterCore.MAX_SEGMENT_TIME)
    			{
    				endTime = VideoFilterCore.MAX_SEGMENT_TIME;
    			}
    			else
    			{
    				endTime = mVideo.videoInfo.videoLengthInMills;
    			}
    			
    			//load thumbnail bitmaps
    				
    			nLeftMarkerPos = 0;
    			nRightMarkerPos = 0;
    			
    			//notify video loaded
				videoLengthChangeListneer.onVideoLoaded();

    			((Activity)mContext).runOnUiThread(new Runnable(){
    				@Override
    				public void run()
    				{
    	    			videoLengthChangeListneer.onVideoLengthChanged(startTime, endTime, true);
    				}
    			});
    			
    			isVideoLoaded = true;
    			thumbView.postInvalidate();
    		}
    	}
    }
    
    //load thumbnail bitmaps
    class LoadThumbnailThread extends Thread
    {
    	private VideoThumbnailView thumbView;
    	
    	public LoadThumbnailThread(VideoThumbnailView view)
    	{
    		this.thumbView = view;
    	}
    	
    	@Override
    	public void run()
    	{
    		Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			
			float reduceRate = (float)nThumbHeight/mVideo.videoInfo.videoHeight;
			int nSrcThumbWidth = (int)(nThumbWidth*reduceRate);
    		for(int i=0;i<nThumbCount;i++)
    		{
    			long timeInMills = (long) (((float)mVideo.videoInfo.videoLengthInMills/nThumbCount)*i);
    			Bitmap bigFrameBitmap = (Bitmap)mVideo.getVideoFrame(timeInMills);
    			if(bigFrameBitmap == null) continue;
    			
    			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    			thumbBitmapArray[i] = Bitmap.createBitmap(nThumbWidth , nThumbHeight , conf);
    			Canvas canvas = new Canvas(thumbBitmapArray[i]);
    			
    			canvas.drawBitmap(bigFrameBitmap, new Rect(0 , 0 , nSrcThumbWidth , mVideo.videoInfo.videoHeight)
    						, new Rect(0 , 0 , nThumbWidth , nThumbHeight), 
    						paint);
    			try
    			{
    				bigFrameBitmap.recycle();
    			}catch(Exception e){e.printStackTrace();}finally{bigFrameBitmap = null;}
    			
    			//call draw
    			thumbView.postInvalidate();
    		}
    	}
    }
    
    public interface VideoLengthEditChangeListener
    {
    	public void onVideoLoaded();
    	public void onFirstFrameLoaded(Bitmap bmp);
    	public void onVideoLengthChanged(int startTime , int endTime , boolean isStartTimeChanged);
    }
}
