package com.ginko.imagecrop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;

public class CropImageView extends ImageViewTouchBase {

    public ArrayList<HighlightView> mHighlightViews      = new ArrayList<HighlightView>();
    public HighlightView            mMotionHighlightView = null;
    float mLastX, mLastY;
    int mMotionEdge;
    private boolean isSaving = false;

    private Context mContext;

    @Override
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {

        super.onLayout(changed, left, top, right, bottom);
        if (mBitmapDisplayed.getBitmap() != null) {
            for (HighlightView hv : mHighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();

                //if (hv.mIsFocused) {
                //    centerBasedOnHighlightView(hv);
                //}

            }
        }
    }

    public CropImageView(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.mContext = context;

        startX = 0.0f;
        startY = 0.0f;

        translateX = 0.0f;
        translateY = 0.0f;
    }

    @Override
    protected void zoomTo(float scale, float centerX, float centerY) {

        super.zoomTo(scale, centerX, centerY);
        /*for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }*/
    }

    @Override
    protected void zoomIn() {

        super.zoomIn();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void zoomOut() {

        super.zoomOut();
        for (HighlightView hv : mHighlightViews) {
            hv.mMatrix.set(getImageMatrix());
            hv.invalidate();
        }
    }

    @Override
    protected void postTranslate(float deltaX, float deltaY) {

        super.postTranslate(deltaX, deltaY);
        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.mMatrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    // According to the event's position, change the focus to the first
    // hitting cropping rectangle.
    private void recomputeFocus(MotionEvent event) {

        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            hv.setFocus(false);
            hv.invalidate();
        }

        for (int i = 0; i < mHighlightViews.size(); i++) {
            HighlightView hv = mHighlightViews.get(i);
            int edge = hv.getHit(event.getX(), event.getY());
            if (edge != HighlightView.GROW_NONE) {
                if (!hv.hasFocus()) {
                    hv.setFocus(true);
                    hv.invalidate();
                }
                break;
            }
        }
        invalidate();
    }

    public synchronized void setIsSaving(boolean saving)
    {
        this.isSaving = saving;
    }

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0.0f;
    private float startY = 0.0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0.0f;
    private float translateY = 0.0f;



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (this.isSaving) {
            return false;
        }

        /*switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                //if (mMotionHighlightView != null)
                {
                    boolean isTouchingCropArea = false;
                    for (int i = 0; i < mHighlightViews.size(); i++) {
                        HighlightView hv = mHighlightViews.get(i);
                        int edge = hv.getHit(event.getX(), event.getY());
                        if (edge != HighlightView.GROW_NONE) {
                            mMotionEdge = edge;
                            mMotionHighlightView = hv;
                            mLastX = event.getX();
                            mLastY = event.getY();
                            if(edge != HighlightView.GROW_NONE)
                                edge = HighlightView.MOVE;
                            mMotionHighlightView.setMode(
                                    (edge == HighlightView.MOVE)
                                            ? HighlightView.ModifyMode.Move
                                            : HighlightView.ModifyMode.Grow);
                            isTouchingCropArea = true;
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mMotionHighlightView != null) {
                    centerBasedOnHighlightView(mMotionHighlightView);
                    mMotionHighlightView.setMode(
                            HighlightView.ModifyMode.None);
                }
                mMotionHighlightView = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMotionHighlightView != null) {
                    mMotionHighlightView.handleMotion(mMotionEdge,
                            event.getX() - mLastX,
                            event.getY() - mLastY);
                    mLastX = event.getX();
                    mLastY = event.getY();

                    if (true) {
                        // This section of code is optional. It has some user
                        // benefit in that moving the crop rectangle against
                        // the edge of the screen causes scrolling but it means
                        // that the crop rectangle is no longer fixed under
                        // the user's finger.
                        //ensureVisible(mMotionHighlightView);
                    }
                }

                break;
        }*/


        float touchX = event.getX();
        float touchY = event.getY();

        float a = 0;
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:

                mode = DRAG;

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                //amount for each coordinates This works even when we are translating the first time because the initial
                //values for these two variables is zero.
                startX = touchX ;
                startY = touchY ;

                mLastX = touchX;
                mLastY = touchY;
                break;

            case MotionEvent.ACTION_MOVE:
                translateX = event.getX() - mLastX;   //Modify by lee.
                translateY = event.getY() - mLastY;
                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.
                //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.
                double distance = Math.sqrt(Math.pow(event.getX()-startX, 2) +
                        Math.pow(event.getY() - startY, 2));

                if(distance > 0)
                {
                    dragged = true;
                }
                mLastX = event.getX();    //Modify by lee.
                mLastY = event.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_UP:
                mode = NONE;
                dragged = false;
                translateX = 0.0f ; translateY = 0.0f;
                for (HighlightView hv : mHighlightViews) {
                    adjustImageSizeToCropViewBound(hv , 300f);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                translateX = 0.0f ; translateY = 0.0f;
                for (HighlightView hv : mHighlightViews) {
                    adjustImageSizeToCropViewBound(hv , 300f);
                }
                break;
        }

        detector.onTouchEvent(event);


        /*switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                center(true, true);
                break;
            case MotionEvent.ACTION_MOVE:
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around.  This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (getScale() == 1F) {
                    center(true, true);
                }
                break;
        }*/
        //We redraw the canvas only in the following cases:
        //
        // o The mode is ZOOM
        // OR
        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
        // set to true (meaning the finger has actually moved)
        /*if(mHighlightViews != null) {
            for (HighlightView hv : mHighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                hv.invalidate();
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }*/

        //if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM)
        {
            invalidate();
        }

        return true;
    }

    private void adjustImageSizeToCropViewBound(final HighlightView hv , final float durationMs)
    {
        //final float oldScale = getScale();
        final long startTime = System.currentTimeMillis();

        mHandler.post(new Runnable() {
            public void run() {

                long now = System.currentTimeMillis();
                float currentMs = Math.min(durationMs, now - startTime);
                //float target = oldScale + (incrementPerMs * currentMs);
                Rect drawbleBoundRect = getDrawable().getBounds();

                RectF realDrawingRect = new RectF((float)drawbleBoundRect.left ,
                        (float)drawbleBoundRect.top,
                        (float)drawbleBoundRect.right,
                        (float)drawbleBoundRect.bottom);
                Matrix drawMatrix = getImageViewMatrix();
                drawMatrix.mapRect(realDrawingRect);

                RectF drawHighlightRectF = new RectF(hv.mDrawRect.left ,
                        hv.mDrawRect.top,
                        hv.mDrawRect.right,
                        hv.mDrawRect.bottom);

                float deltaX = 0.0f;
                float deltaY = 0.0f;

                if(realDrawingRect.top>drawHighlightRectF.top)
                {
                    deltaY = drawHighlightRectF.top - realDrawingRect.top;
                }
                else if(realDrawingRect.bottom  < drawHighlightRectF.bottom)
                {
                    deltaY = drawHighlightRectF.bottom - realDrawingRect.bottom;
                }

                if(realDrawingRect.left>drawHighlightRectF.left)
                {
                    deltaX = drawHighlightRectF.left - realDrawingRect.left;
                }
                else if(realDrawingRect.right  < drawHighlightRectF.right)
                {
                    deltaX = drawHighlightRectF.right - realDrawingRect.right;
                }

                if(deltaX != 0 || deltaY != 0) {
                    panBy(deltaX, deltaY);
                    invalidate();

                    /* modif by lee
                    if (currentMs < durationMs) {
                        mHandler.post(this);
                    }*/
                }
            }
        });


    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private void ensureVisible(HighlightView hv) {

        Rect r = hv.mDrawRect;

        int panDeltaX1 = Math.max(0, mLeft - r.left);
        int panDeltaX2 = Math.min(0, mRight - r.right);

        int panDeltaY1 = Math.max(0, mTop - r.top);
        int panDeltaY2 = Math.min(0, mBottom - r.bottom);

        int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
        int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY);
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private void centerBasedOnHighlightView(HighlightView hv) {

        Rect drawRect = hv.mDrawRect;

        float width = drawRect.width();
        float height = drawRect.height();

        float thisWidth = getWidth();
        float thisHeight = getHeight();

        float z1 = thisWidth / width ;
        float z2 = thisHeight / height ;

        float zoom = Math.min(z1, z2);
        zoom = zoom * this.getScale();
        zoom = Math.max(1F, zoom);
        if ((Math.abs(zoom - getScale()) / zoom) > .1) {
            float[] coordinates = new float[]{hv.mCropRect.centerX(),
                    hv.mCropRect.centerY()};
            getImageMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300F);
        }

        ensureVisible(hv);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if(mode == DRAG)
        {
            panBy(translateX , translateY);
            /*for (int i = 0; i < mHighlightViews.size(); i++) {
                mHighlightViews.get(i).moveBy((-1)*translateX , (-1)*translateY);
            }*/
        }
        if(mode == ZOOM)
        {
            zoomTo(scaleFactor, startX, startY);
            System.out.println("------Zooming------");
        }
        /*if(mHighlightViews != null) {
            for (HighlightView hv : mHighlightViews) {
                hv.mMatrix.set(getImageMatrix());
                //keepOriginalHighlightView(hv);
                centerBasedOnHighlightView(hv);
            }
        }*/
        /*Rect drawbleBoundRect = getDrawable().getBounds();

        RectF realDrawingRect = new RectF((float)drawbleBoundRect.left ,
                        (float)drawbleBoundRect.top,
                        (float)drawbleBoundRect.right,
                         (float)drawbleBoundRect.bottom);
        Matrix drawMatrix = getImageViewMatrix();
        drawMatrix.mapRect(realDrawingRect);
        System.out.println("---image (" + realDrawingRect.left + " , " +
                realDrawingRect.top + " , " +
                realDrawingRect.right + " , " +
                realDrawingRect.bottom + " ) --- ");*/

        for (int i = 0; i < mHighlightViews.size(); i++) {
            mHighlightViews.get(i).draw(canvas);
            /*RectF drawHighlightRectF = new RectF(mHighlightViews.get(i).mDrawRect.left ,
                    mHighlightViews.get(i).mDrawRect.top,
                    mHighlightViews.get(i).mDrawRect.right,
                    mHighlightViews.get(i).mDrawRect.bottom);
            //drawMatrix.mapRect(drawHighlightRectF);
            System.out.println("---CropRect = ("+drawHighlightRectF.left + " , " +
                    drawHighlightRectF.top + " , "+
                    drawHighlightRectF.right + " , "+
                    drawHighlightRectF.bottom + " )--- ");*/
        }
    }

    public Rect getCroppedRect(Rect originalBitmapRect)
    {
        Rect croppedRect = new Rect();

        int originalBitmapWidth = originalBitmapRect.right - originalBitmapRect.left;
        int originalBitmapHeight = originalBitmapRect.bottom - originalBitmapRect.top;


        Rect drawbleBoundRect = getDrawable().getBounds();

        RectF realDrawingRect = new RectF((float)drawbleBoundRect.left ,
                (float)drawbleBoundRect.top,
                (float)drawbleBoundRect.right,
                (float)drawbleBoundRect.bottom);
        Matrix drawMatrix = getImageViewMatrix();
        drawMatrix.mapRect(realDrawingRect);
        float leftOffset = (-1)*(float)realDrawingRect.left;
        float topOffset = (-1)*(float)realDrawingRect.top;

        //realDrawingRect.offset( leftOffset , topOffset);

        int drawingBitmapWidth = (int)(realDrawingRect.right - realDrawingRect.left);
        int drawingBitmapHeight = (int)(realDrawingRect.bottom - realDrawingRect.top);

        float scaleWidth = ((float)originalBitmapWidth)/drawingBitmapWidth;
        float scaleHeight = ((float)originalBitmapHeight)/drawingBitmapHeight;

        for (int i = 0; i < mHighlightViews.size(); i++) {

            RectF drawHighlightRectF = new RectF(mHighlightViews.get(i).mDrawRect.left ,
                    mHighlightViews.get(i).mDrawRect.top,
                    mHighlightViews.get(i).mDrawRect.right,
                    mHighlightViews.get(i).mDrawRect.bottom);
            //drawMatrix.mapRect(drawHighlightRectF);
            drawHighlightRectF.offset(leftOffset , topOffset);

            croppedRect.left = (int) drawHighlightRectF.left;
            croppedRect.top = (int) drawHighlightRectF.top;
            croppedRect.right = (int) drawHighlightRectF.right;
            croppedRect.bottom = (int) drawHighlightRectF.bottom;

        }
        croppedRect.left = (int)(croppedRect.left*scaleWidth);
        croppedRect.top = (int)(croppedRect.top*scaleHeight);
        croppedRect.right = (int)(croppedRect.right*scaleWidth);
        croppedRect.bottom = (int)(croppedRect.bottom*scaleWidth);

        return croppedRect;
    }

    public void add(HighlightView hv) {

        mHighlightViews.add(hv);
        invalidate();
    }
}