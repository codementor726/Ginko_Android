package com.ginko.activity.im;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class ImInputEditTExt extends EditText {

    private OnEditTextKeyDownListener backKeyListener;

    public ImInputEditTExt(Context a_context) {

        super(a_context);

    }

    public ImInputEditTExt(Context a_context, AttributeSet a_attributeSet) {

        super(a_context, a_attributeSet);

    }

    public void registerOnBackKeyListener(OnEditTextKeyDownListener _backKeyListener)
    {
        this.backKeyListener = _backKeyListener;
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {

            if(keyCode == KeyEvent.KEYCODE_BACK) {

                if(backKeyListener != null)
                    backKeyListener.onImEditTextBackKeyDown();
                return true; // use the override function by user
            }
        }

        return super.onKeyPreIme(keyCode, event); // use default system function
    }

    public interface OnEditTextKeyDownListener
    {
        public void onImEditTextBackKeyDown();
    }


}