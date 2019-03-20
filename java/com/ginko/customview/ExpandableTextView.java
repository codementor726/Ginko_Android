package com.ginko.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ginko.ginko.R;

public class ExpandableTextView extends TextView {
    private static final int DEFAULT_TRIM_LENGTH = 200;
    private static final String ELLIPSIS = "... ";
    private static final String SHOW_MORE = "show more";

    private CharSequence originalText;
    private CharSequence trimmedText;
    private BufferType bufferType;
    private boolean trim = true;
    private int trimLength;
    private boolean isShowMore = false;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isShowMore && !trim)
                setOnClickListener(null);
            trim = !trim;
            setText();
            requestFocusFromTouch();

        }
    };

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH);
        this.isShowMore = typedArray.getBoolean(R.styleable.ExpandableTextView_showMoreClick, false);
        typedArray.recycle();

        if(isShowMore) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }

        if(!isShowMore)
        {
            setOnClickListener(clickListener);
        }
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
    }

    private CharSequence getDisplayableText() {
        return trim ? trimmedText : originalText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        trimmedText = getTrimmedText(text);
        bufferType = type;
        setText();
    }

    private CharSequence getTrimmedText(CharSequence text) {
        if (originalText != null && originalText.length() > (trimLength +9)) {
            if(isShowMore)
            {
                final SpannableStringBuilder sb = new SpannableStringBuilder(originalText, 0, trimLength + 1);
                sb.append(ELLIPSIS);
                sb.append(SHOW_MORE);

                final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(127, 91, 134));

                ClickableSpan clickSpan = getClickableSpan();
                // to cater last/only word
                sb.setSpan(clickSpan, trimLength+4+1, trimLength+4+10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                // Set the text color for first 4 characters
                sb.setSpan(fcs, trimLength+4+1, trimLength+4+10, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                return sb;
            }
            else
                return new SpannableStringBuilder(originalText, 0, trimLength + 1).append(ELLIPSIS);
        } else {
            return originalText;
        }
    }
    private ClickableSpan getClickableSpan() {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                //trim = !trim;
                //setText();
                //requestFocusFromTouch();
                if(trim)
                    setOnClickListener(clickListener);
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
    }
    public CharSequence getOriginalText() {
        return originalText;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        trimmedText = getTrimmedText(originalText);
        setText();
    }

    public int getTrimLength() {
        return trimLength;
    }
}