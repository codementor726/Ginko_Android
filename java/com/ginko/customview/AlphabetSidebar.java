package com.ginko.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ginko.ginko.R;

public class AlphabetSidebar extends View{
    // Touch events
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26 letters
    public static String[] b = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#" };
    private int choose = -1;// Selected
    private Paint paint = new Paint();

    private TextView mTextDialog;

    private int slideBarItemTextSize = 20;

    /**
     * Set the display letters TextView SideBar
     * @param mTextDialog
     */
    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }


    public AlphabetSidebar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getAttributes(attrs);
    }

    public AlphabetSidebar(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
        getAttributes(attrs);
    }

    public AlphabetSidebar(Context context) {
        super(context);
    }

    private void getAttributes(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(attrs , R.styleable.AlphabetSidebar);
        try
        {
            slideBarItemTextSize = a.getDimensionPixelSize(R.styleable.AlphabetSidebar_ScrollItemTextSize, 20);
        }catch(Exception e)
        {
            e.printStackTrace();
            slideBarItemTextSize = 20;
        }

        a.recycle();
    }
    /**
     * Override this method
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Gets the focus changes the background color.
        int height = getHeight();// Access to the corresponding height
        int width = getWidth(); // Access to the corresponding width
        int singleHeight = height / b.length;// Get a letter height

        for (int i = 0; i <b.length; i++) {
            paint.setColor(Color.rgb(100, 100, 100));
            // paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            //paint.setTextSize(20);
            paint.setTextSize(slideBarItemTextSize);
            // The selected state
            if (i == choose) {
                paint.setColor(Color.parseColor("#3399ff"));
                paint.setFakeBoldText(true);
            }
            // The X coordinate is equal to half the width of the middle - string.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();// Reset brushes
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// Click the Y coordinates
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);// Click y to coordinate the proportion of the total height of the *b array length is equal to the number of clicks in B.

        switch (action) {
            case MotionEvent.ACTION_UP:
                //setBackgroundDrawable(new ColorDrawable(0x00000000));
                setBackgroundResource(R.drawable.sidebar_background);
                //setBackgroundResource(R.drawable.sidebar_background);
                choose = -1;//
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                setBackgroundResource(R.drawable.sidebar_background);
                //setBackgroundColor(Color.rgb(255, 0, 0));
                if (oldChoose != c) {
                    if (c >= 0 && c <b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }

                        choose = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    /**
     * Methods open to the public
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * Interface
     *
     * @author coder
     *
     */
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }
}
