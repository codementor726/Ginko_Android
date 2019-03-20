package com.ginko.activity.im;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lee on 4/23/2015.
 */
public class VoiceMessageProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;

    private int nProgress;
    private int nMaxProgress;

    public VoiceMessageProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(0xffb8b8b8);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setColor(0xff8064a2);

        nProgress = 0;
        nMaxProgress = 100;
    }

    public void setProgressValue(int value)
    {
        this.nProgress = value;
    }
    public void setMaximumProgress(int value)
    {
        this.nMaxProgress = value;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        float progressRate = (float)nProgress/nMaxProgress;
        System.out.println("----Progress Rate = "+String.valueOf(progressRate));
        int progressWidth = (int) (measuredWidth*progressRate);

        canvas.drawRect( 0 , 0 , measuredWidth , measuredHeight , backgroundPaint);
        canvas.drawRect( 0 , 0 , progressWidth , measuredHeight , progressPaint);

    }
}
