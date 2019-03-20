package com.ginko.customview;

/**
 * Created by YongJong on 09/08/16.
 */
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.ginko.ginko.MyApp;

public class TouchableWrapper extends FrameLayout {
    private long lastTouchTime = -1;

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                long thisTime = System.currentTimeMillis();
                if (thisTime - lastTouchTime < ViewConfiguration.getDoubleTapTimeout()) {
                    // Double tap
                    MyApp.getInstance().mMapDoubleTouched = true;
                    lastTouchTime = -1;
                } else {
                    // Too slow :)
                    MyApp.getInstance().mMapDoubleTouched = false;
                    lastTouchTime = thisTime;
                }
                break;

            case MotionEvent.ACTION_UP:
                //MyApp.getInstance().mMapDoubleTouched = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
