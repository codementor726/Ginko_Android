package com.ginko.customview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.ginko.common.Logger;

public class DragableTextView extends TextView {

	int screenWidth;
	int screenHeight;
	int lastX;
	int lastY;

	private boolean moved;

	private int left;
	private int top;

    public ClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private ClickListener clickListener;
	
    public DragableTextView(Context context) {
        this(context, null);
    }

	public DragableTextView(Context context, AttributeSet attribute) {
		this(context, attribute, 0);
	}

	public DragableTextView(Context context, AttributeSet attribute, int style) {
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

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		//Logger.error("changed position." + left + " " + top + " " + right + " "+ bottom);
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		View v = this;
		int action = event.getAction();
		Logger.info( "Touch:" + action);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
            //The following is needed, or else, when put it into a ScrollView, it won't work.
            getParent().requestDisallowInterceptTouchEvent(true);
//			if (this.getContext() instanceof EntityInfoStyleActivity){
//				((EntityInfoStyleActivity)this.getContext()).setSelectedField(this);
//			}
            if (this.getClickListener() != null) {
                this.getClickListener().onClick(this);
            }
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			break;
		/**
		 * layout(l,t,r,b) l Left position, relative to parent t Top position,
		 * relative to parent r Right position, relative to parent b Bottom
		 * position, relative to parent
		 * */
		case MotionEvent.ACTION_MOVE:
			int dx = (int) event.getRawX() - lastX;
			int dy = (int) event.getRawY() - lastY;

			left = v.getLeft() + dx;
			top = v.getTop() + dy;
			int	right = v.getRight() + dx;
			int bottom = v.getBottom() + dy;
			if (left < 0) {
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
			}
			moved = true;
			v.layout(left, top, right, bottom);
			Logger.info( "position:" + left + ", " + top + ", " + right
					+ ", " + bottom);
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_UP:
            getParent().requestDisallowInterceptTouchEvent(false);
			break;
		}
		// Must return true..
		return true;
	}

    public interface ClickListener{
        void onClick(TextView textView);
    }
}
