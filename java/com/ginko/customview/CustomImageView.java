package com.ginko.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.ginko.ginko.R;

/**
 *
 * @author zhy
 * 
 */
public class CustomImageView extends View {

	/**
	 * TYPE_CIRCLE / TYPE_ROUND
	 */
	private int type;
	private static final int TYPE_CIRCLE = 0;
	private static final int TYPE_ROUND = 1;

	private Bitmap mSrc;

    private int srcHeight=0;
    private int srcWidth=0;

	private int mRadius;

	/**
	 */
	private int mWidth;
	/**
	 */
	private int mHeight;

	
	/**
	 * When draw a cycle image with a border
	 */
	private int boderColor = Color.BLACK;
	private int borderSize = 5;
	
	public int getBoderColor() {
		return boderColor;
	}

	public void setBoderColor(int boderColor) {
		this.boderColor = boderColor;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}


	
	public CustomImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomImageView(Context context) {
		this(context, null);
	}

	/**
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.CustomImageView, defStyle, 0);

		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.CustomImageView_src:
				mSrc = BitmapFactory.decodeResource(getResources(),
						a.getResourceId(attr, 0));
                this.srcHeight = mSrc.getHeight();
                this.srcWidth = mSrc.getWidth();
				break;
			case R.styleable.CustomImageView_type:
				type = a.getInt(attr, 0);
				break;
			case R.styleable.CustomImageView_borderRadius:
				mRadius = a.getDimensionPixelSize(attr, (int) TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
								getResources().getDisplayMetrics()));
				break;
			case R.styleable.CustomImageView_border_size:
				this.borderSize = a.getDimensionPixelSize(attr, (int) TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f,
								getResources().getDisplayMetrics()));
				break;
			case R.styleable.CustomImageView_border_color:
				this.boderColor = a.getColor(attr, Color.BLACK);
				break;
			}
		}
		a.recycle();
	}
	
    public void setImageBitmap(Bitmap bm) {
//    	this.mSrc= Bitmap.createBitmap(bm);
        this.mSrc = bm;
    	 invalidate();
    }
    
    public Bitmap getImageBitmap(){
    	return this.mSrc;
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);

		if (specMode == MeasureSpec.EXACTLY)// match_parent , accurate
		{
			mWidth = specSize;
		} else {
			if (this.srcHeight==0){
				mWidth = specSize;
			}else {
				int desireByImg = getPaddingLeft() + getPaddingRight()
						+ this.srcHeight;
				if (specMode == MeasureSpec.AT_MOST)// wrap_content
				{
					mWidth = Math.min(desireByImg, specSize);
				} else

					mWidth = desireByImg;
			}
	
		}


		specMode = MeasureSpec.getMode(heightMeasureSpec);
		specSize = MeasureSpec.getSize(heightMeasureSpec);
		if (specMode == MeasureSpec.EXACTLY)// match_parent , accurate
		{
			mHeight = specSize;
		} else {
			if (this.srcWidth==0) {
				mHeight = specSize;
			} else {
				int desire = getPaddingTop() + getPaddingBottom()
						+ this.srcWidth;

				if (specMode == MeasureSpec.AT_MOST)// wrap_content
				{
					mHeight = Math.min(desire, specSize);
				} else
					mHeight = desire;
			}
		}

		setMeasuredDimension(mWidth, mHeight);

	}

	/**
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		if (this.mSrc == null){
			return;
		}
		switch (type) {
		case TYPE_CIRCLE:
			int min = Math.min(mWidth, mHeight);
			
			mSrc = Bitmap.createScaledBitmap(mSrc, min, min, false);
			canvas.drawBitmap(createCircleImage(mSrc, min), 0, 0, null);
			break;
		case TYPE_ROUND:
			canvas.drawBitmap(createRoundConerImage(mSrc), 0, 0, null);
			break;

		}
	}

    /**
	 *
	 * @param source
	 * @param min
	 * @return
	 */
	private Bitmap createCircleImage(Bitmap source, int min) {
		final Paint paint = new Paint();
        paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);

		int smallCycle= min -this.getBorderSize();
		Bitmap target = Bitmap.createBitmap(smallCycle, smallCycle, Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		canvas.drawCircle(smallCycle / 2, smallCycle / 2, (smallCycle / 2), paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		
		float leftTop = -1 * this.getBorderSize()/2;
		canvas.drawBitmap(source, leftTop, leftTop, paint);
        if (hasBorder()) {
            //draw a stroke circle
            Paint paintCircle = new Paint();
            paintCircle.setAntiAlias(true);
            paintCircle.setColor(this.getBoderColor());
            paintCircle.setStyle(Paint.Style.STROKE);
            paintCircle.setStrokeWidth(this.getBorderSize() / 2);

            float offset = (float) (this.getBorderSize() / 2.0);
            canvas.drawCircle(smallCycle / 2, smallCycle / 2, (smallCycle / 2 - offset / 2), paintCircle);
        }
		return target;
	}

	private boolean hasBorder(){
		return this.getBorderSize()>0;
	}

	/**
	 *
	 * @param source
	 * @return
	 */
	private Bitmap createRoundConerImage(Bitmap source) {
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		RectF rect = new RectF(0, 0, source.getWidth(), source.getHeight());
		canvas.drawRoundRect(rect, mRadius, mRadius, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(source, 0, 0, paint);
		return target;
	}
}
