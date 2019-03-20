package com.ginko.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ginko.common.Logger;
import com.videophotofilter.library.android.com.SquarePhotoImageView;

public class DragableImage extends ImageView {
    int screenWidth;
    int screenHeight;
    int lastX;
    int lastY;

    private boolean moved;

    private int left;
    private int top;

    private int width = 0;
    private int height = 0;

    private SquarePhotoImageView.OnViewDrawListener viewListener;
    public void setViewListener(SquarePhotoImageView.OnViewDrawListener listener)
    {
        this.viewListener = listener;
    }

    public DragableImage(Context context) {
        this(context, null);
    }

    public DragableImage(Context context, AttributeSet attribute) {
        this(context, attribute, 0);
    }

    public DragableImage(Context context, AttributeSet attribute, int style) {
        super(context, attribute, style);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - 50;
        // this.setOnTouchListener(this);
        this.setClickable(true);// to be movable, clickable must be set to true.
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();

        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    screenWidth = ((View)getParent()).getWidth();
                    screenHeight = ((View)getParent()).getHeight();
                }
            });
        }
    }

    public void setAdjustImageRespectRate(Bitmap bmp ,  int limitWidth , int limitHeight)
    {
        int bWidth = 0;
        int bHeight = 0;
        setImageBitmap(bmp);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();

        bWidth = bmp.getWidth();
        bHeight = bmp.getHeight();

        if (bWidth == 0 || bHeight == 0 || limitWidth == 0)
            return;


        int swidth = bWidth>limitWidth?limitWidth:bWidth;
        int new_height = 0;
        new_height = swidth * bHeight / bWidth;
        params.width = swidth;
        params.height = new_height;
        setLayoutParams(params);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = MeasureSpec.getSize(widthMeasureSpec);
        this.height = MeasureSpec.getSize(heightMeasureSpec);
        if(this.viewListener != null)
            this.viewListener.onDrawnView(this , width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        Logger.error("changed position." + left + " " + top + " " + right + " "
                + bottom);

        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    public void layout(int l, int t, int r, int b) {
        if (!moved) {
            super.layout(l, t, r, b);
        } else {
            int right = this.left + r-l;
            int bottom = this.top + b-t;
            super.layout(this.left, this.top, right, bottom);
        }
    }

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    float newDist = 1f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ImageView v = this;
        int action = event.getAction();
        Logger.info( "Touch:" + action);

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                //The following is needed, or else, when put it into a ScrollView, it won't work.
                System.out.println("-----Action Down Start Drag----");
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();

                start.set(event.getX(), event.getY());
                mode = DRAG;

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                System.out.println("-----Action Pointer Down Start Zoom----");
                getParent().requestDisallowInterceptTouchEvent(true);
                mode = ZOOM;
                newDist = spacing(event);
                oldDist = spacing(event);

                left = v.getLeft();  top = v.getTop();
                width = v.getWidth(); height = v.getHeight();

                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == DRAG) {
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;

                    left = v.getLeft() + dx;
                    top = v.getTop() + dy;
                    int right = v.getRight() + dx;
                    int bottom = v.getBottom() + dy;
                     /*if (left < 0) {
                        left = 0;
                        right = left + v.getWidth();
                    }
                    if (right > screenWidth) {
                        right = screenWidth;
                        left = right - v.getWidth();
                    }
                    if (top < 0) {
                        top = 0;
                        bottom = top + v.getHeight();
                    }
                    if (bottom > screenHeight) {
                        bottom = screenHeight;
                        top = bottom - v.getHeight();
                    }*/
                    moved = true;
                    v.layout(left, top, right, bottom);
                    Logger.info("position:" + left + ", " + top + ", " + right
                            + ", " + bottom);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                }
                else if(mode == ZOOM){
                    float newDist = spacing(event);
                    if (newDist > 5f)
                    {
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        int newWidth = (int) (width * scale);
                        int newHeight = (int) (height * scale);
                        moved = true;
                        int dx = (int)((width - newWidth)/2);
                        int dy = (int)((height - newHeight)/2);

                        System.out.println("-----Action  Zoom (dx , dy) = ("+String.valueOf(dx)+ " , "+String.valueOf(dy)+") ------");
                        System.out.println("-----Action  Zoom (nLeft , nTop) = ("+String.valueOf(left-dx)+ " , "+String.valueOf(top-dy)+") ------");
                        v.layout(left-dx, top - dy, left-dx+newWidth, top-dy+newHeight);
                        //v.layout(left, top, left+width, top + height);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return true;
    }
    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        if(event.getPointerCount()>1) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return FloatMath.sqrt(x * x + y * y);
        }
        return 0.0f;
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        if(event.getPointerCount()>1) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }
}
