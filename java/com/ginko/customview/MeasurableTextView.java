package com.ginko.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class MeasurableTextView extends TextView {

    public MeasurableTextView(Context context) {
        super(context);
    }

    public MeasurableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MeasurableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
