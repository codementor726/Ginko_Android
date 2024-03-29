package com.ginko.customview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class DragGridView extends GridView{
	/**
	 * DragGridView��item������Ӧ��ʱ�䣬 Ĭ����1000���룬Ҳ������������
	 */
	private long dragResponseMS = 1000;
	
	/**
	 * �Ƿ������ק��Ĭ�ϲ�����
	 */
	private boolean isDrag = false;
	
	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;

	private int mDragPosition;
	

	private View mStartDragItemView = null;
	

	private ImageView mDragImageView;

	private Vibrator mVibrator;
	
	private WindowManager mWindowManager;

	private WindowManager.LayoutParams mWindowLayoutParams;

	private Bitmap mDragBitmap;

	private int mPoint2ItemTop ; 

	private int mPoint2ItemLeft;

	private int mOffset2Top;

	private int mOffset2Left;

	private int mStatusHeight; 

	private int mDownScrollBorder;

	private int mUpScrollBorder;

	private static final int speed = 80;

	private OnChanageListener onChanageListener;
	
	
	
	public DragGridView(Context context) {
		this(context, null);
	}
	
	public DragGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); //��ȡ״̬���ĸ߶�
	}
	
	private Handler mHandler = new Handler();
	
	private Runnable mLongClickRunnable = new Runnable() {
		
		@Override
		public void run() {
			isDrag = true; //���ÿ�����ק
			try {
				mVibrator.vibrate(50); //��һ��
				if(mStartDragItemView == null)
					return;
				mStartDragItemView.setVisibility(View.INVISIBLE);//���ظ�item

				//�������ǰ��µĵ���ʾitem����
				createDragImage(mDragBitmap, mDownX, mDownY);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	};
	
	/**
	 * ���ûص��ӿ�
	 * @param onChanageListener
	 */
	public void setOnChangeListener(OnChanageListener onChanageListener){
		this.onChanageListener = onChanageListener;
	}
	
	/**
	 * ������Ӧ��ק�ĺ�������Ĭ����1000����
	 * @param dragResponseMS
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			//ʹ��Handler�ӳ�dragResponseMSִ��mLongClickRunnable
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			
			//���ݰ��µ�X,Y�����ȡ�����item��position
			mDragPosition = pointToPosition(mDownX, mDownY);
			
			if(mDragPosition == AdapterView.INVALID_POSITION){
				return super.dispatchTouchEvent(ev);
			}
			
			//����position��ȡ��item����Ӧ��View
			mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());
			
			//�����⼸�������ҿ��Բο��ҵĲ��������ͼ�������
			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
			
			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX() - mDownX);
			
			//��ȡDragGridView�Զ����Ϲ�����ƫ������С�����ֵ��DragGridView���¹���
			mDownScrollBorder = getHeight() /4;
			//��ȡDragGridView�Զ����¹�����ƫ�������������ֵ��DragGridView���Ϲ���
			mUpScrollBorder = getHeight() * 3/4;
			
			
			
			//����mDragItemView��ͼ����
			mStartDragItemView.setDrawingCacheEnabled(true);
			//��ȡmDragItemView�ڻ����е�Bitmap����
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
			//��һ���ܹؼ����ͷŻ�ͼ���棬��������ظ��ľ���
			mStartDragItemView.destroyDrawingCache();
			
			
			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int)ev.getX();
			int moveY = (int) ev.getY();
			
			//��������ڰ��µ�item�����ƶ���ֻҪ������item�ı߽����ǾͲ��Ƴ�mRunnable
			if(!isTouchInItem(mStartDragItemView, moveX, moveY)){
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			break;
		case MotionEvent.ACTION_UP:
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isTouchInItem(View dragView, int x, int y){
		try {
			int leftOffset = dragView.getLeft();
			int topOffset = dragView.getTop();
			if(x < leftOffset || x > leftOffset + dragView.getWidth()){
				return false;
			}

			if(y < topOffset || y > topOffset + dragView.getHeight()){
				return false;
			}
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		
		return true;
	}
	
	

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(isDrag && mDragImageView != null){
			switch(ev.getAction()){
			case MotionEvent.ACTION_MOVE:
				moveX = (int) ev.getX();
				moveY = (int) ev.getY();
				onDragItem(moveX, moveY);
				break;
			case MotionEvent.ACTION_UP:
				onStopDrag();
				isDrag = false;
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	private void createDragImage(Bitmap bitmap, int downX , int downY){
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; //͸����
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  
	                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;
		  
		mDragImageView = new ImageView(getContext());  
		mDragImageView.setImageBitmap(bitmap);  
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);  
	}

	private void removeDragImage(){
		if(mDragImageView != null){
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	private void onDragItem(int moveX, int moveY){
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //���¾����λ��
		onSwapItem(moveX, moveY);
		
		mHandler.post(mScrollRunnable);
	}
	

	private Runnable mScrollRunnable = new Runnable() {
		
		@Override
		public void run() {
			int scrollY;
			if(moveY > mUpScrollBorder){
				 scrollY = -speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else if(moveY < mDownScrollBorder){
				scrollY = speed;
				 mHandler.postDelayed(mScrollRunnable, 25);
			}else{
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}

			onSwapItem(moveX, moveY);
			
			View view = getChildAt(mDragPosition - getFirstVisiblePosition());
			smoothScrollToPositionFromTop(mDragPosition, view.getTop() + scrollY);
		}
	};

	private void onSwapItem(int moveX, int moveY){

		int tempPosition = pointToPosition(moveX, moveY);
		
		if(tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION){
			getChildAt(tempPosition - getFirstVisiblePosition()).setVisibility(View.INVISIBLE);
			getChildAt(mDragPosition - getFirstVisiblePosition()).setVisibility(View.VISIBLE);
			
			if(onChanageListener != null){
				onChanageListener.onChange(mDragPosition, tempPosition);
			}
			
			mDragPosition = tempPosition;
		}
	}

	private void onStopDrag(){
		getChildAt(mDragPosition - getFirstVisiblePosition()).setVisibility(View.VISIBLE);
		removeDragImage();
	}

	private static int getStatusHeight(Context context){
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        return statusHeight;
    }

	public interface OnChanageListener{

		public void onChange(int form, int to);
	}
}
