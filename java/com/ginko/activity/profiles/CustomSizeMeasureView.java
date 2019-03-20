package com.ginko.activity.profiles;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CustomSizeMeasureView extends View
{

    public interface OnMeasureListner{
        public void onViewSizeMeasure(int width , int height);
    }
    OnMeasureListner measureListener;
    public CustomSizeMeasureView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setOnMeasureListener(OnMeasureListner listener)
    {
        this.measureListener = listener;
    }


    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        if(measureListener!= null)
            measureListener.onViewSizeMeasure(width , height);
    }
}