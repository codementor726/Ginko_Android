package com.ginko.customview;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class TouchableFrameLayout extends RelativeLayout{

    public View childView;
    public TouchableFrameLayout(Context context) {
        super(context);
    }

    public TouchableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setChildViewToDelegateTouchEvent(View v)
    {
        this.childView = v;
    }

    private boolean isInArea(View view , MotionEvent event)
    {
        if(event.getX()>=view.getLeft() && event.getX()<=view.getRight() && event.getY()>=view.getTop() && event.getY()<=view.getBottom())
            return true;
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me)
    {
        /**/

        if(childView != null && isInArea(childView , me)) {

            childView.onTouchEvent(me);
            return true;
        }

        return  super.dispatchTouchEvent(me);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return super.onInterceptTouchEvent(arg0);
        //return true;
    }
}
