package com.ginko.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class QuickstartCustomFontTextView extends TextView {
    public QuickstartCustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public QuickstartCustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public QuickstartCustomFontTextView(Context context) {
        super(context);
    }
    public void setTypeface(Typeface tf, int style) {
        if (style == Typeface.BOLD) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(),    "Helvetica.otf"));
        }
        else if(style == Typeface.ITALIC)
        {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Helvetica.otf"));
        }
        else
        {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Helvetica.otf"));
        }
    }
}
