package com.videophotofilter.library.android.com;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.List;

import com.ginko.ginko.R;
import com.videophotofilter.android.com.VideoFilterCore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.InvalidDisplayException;

public class VideoSegmentView extends View{

	private Paint mBackgroundPaint;
	private Paint mSegmentPaint;
	private Paint mSegmentDividerPaint;
	private Paint mSegmentSelectorPaint;
	
	private List<VideoSegment> videoSegments;
	
	private int totalFrameCount = 0;
	private int totalDurationTime = 0;
	
	public VideoSegmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		totalFrameCount = VideoFilterCore.FPS * VideoFilterCore.MAX_SEGMENT_TIME;
		totalDurationTime = 1000 * VideoFilterCore.MAX_SEGMENT_TIME;

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setAntiAlias(false);
		mBackgroundPaint.setColor(getResources().getColor(R.color.segment_canvas_background));
		mBackgroundPaint.setStyle(Paint.Style.FILL);
		
		mSegmentPaint = new Paint();
		mSegmentPaint.setAntiAlias(false);
		mSegmentPaint.setColor(getResources().getColor(R.color.segment_color));
		mSegmentPaint.setStyle(Paint.Style.FILL);
		
		mSegmentSelectorPaint = new Paint();
		mSegmentSelectorPaint.setAntiAlias(false);
		mSegmentSelectorPaint.setColor(getResources().getColor(R.color.segment_selector_color));
		mSegmentSelectorPaint.setStyle(Paint.Style.FILL);
		
		mSegmentDividerPaint = new Paint();
		mSegmentDividerPaint.setAntiAlias(false);
		mSegmentDividerPaint.setColor(getResources().getColor(R.color.segment_divider_color));
		mSegmentDividerPaint.setStyle(Paint.Style.FILL);
		mSegmentDividerPaint.setStrokeWidth(8);
	}
	
	public void setSegmentList(List<VideoSegment> segments)
	{
		videoSegments = segments;
	}
	
	public void refresh()
	{
		invalidate();
	}
	
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	
        // Draw segment
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        
        //float linewidthPerFrame = (float)measuredWidth/totalFrameCount;
        float linewidthPerFrame = (float)measuredWidth/totalDurationTime;
        canvas.drawRect(new Rect(0, 0, measuredWidth , measuredHeight - 1), mBackgroundPaint);

        int currentFrameCount = 0;
        
        if(videoSegments == null) return;
        
        synchronized(videoSegments)
        {
        	for(int i =0 ;i<videoSegments.size();i++)
	       	{
	       		//synchronized(videoSegments.get(i))
	       		{
	           		
	       			int left = (int)(currentFrameCount*linewidthPerFrame);
	       			//currentFrameCount += videoSegments.get(i).frameCount;
	       			currentFrameCount += videoSegments.get(i).durationInMills;
	       			int right = (int)(currentFrameCount*linewidthPerFrame);
	       			
	       			if (videoSegments.get(i).selected == false)
	       				canvas.drawRect(new Rect(left,1 , right , measuredHeight-1), mSegmentPaint);
	       			else
	       				canvas.drawRect(new Rect(left,1 , right , measuredHeight-1), mSegmentSelectorPaint);
	       			
	       			if(i<videoSegments.size()-1)
	           		{
	           			canvas.drawLine(right, 1, right, measuredHeight-1,  mSegmentDividerPaint);
	           		}
	        	}
	        }
        }   
    }
}
