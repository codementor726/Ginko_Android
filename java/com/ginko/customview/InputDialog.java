package com.ginko.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.ginko.R;

public class InputDialog  extends Dialog implements View.OnClickListener{

    private Context mContext;

    private Button leftBtn , rightBtn;
    private EditText edtInput;
    private TextView txtTitle;
    private LinearLayout title_bar;

    private OnButtonClickListener mLeftClickListener = null;
    private OnButtonClickListener mRightClickListener = null;
    private OnEditorDoneActionListener editTextActionListener;

    private String title = "";
    private boolean bShowTitleBar = true;
    private String leftButtonName = "" , rightButtonName = "";
    private String txtHint = "";

    private int textInputType = 0;


    public InputDialog(Context context,
                       int inputType,
                       String _title,
                       String _txtHint,
                       boolean _bShowTitleBar ,
                       String leftBtnName,
                       OnButtonClickListener leftClickListener ,
                       String rightBtnName ,
                       OnButtonClickListener rightClickListener ,
                       OnEditorDoneActionListener edtBoxActionListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.mContext = context;

        this.textInputType = inputType;

        this.bShowTitleBar = _bShowTitleBar;
        this.title = _title;
        this.leftButtonName = leftBtnName;
        this.rightButtonName = rightBtnName;

        this.mLeftClickListener = leftClickListener;
        this.mRightClickListener = rightClickListener;
        this.editTextActionListener = edtBoxActionListener;
        this.txtHint = _txtHint;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the background of extra dialog as dim color
        /*WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.input_dialog);


        leftBtn = (Button)findViewById(R.id.leftBtn);
        leftBtn.setOnClickListener(this);
        leftBtn.setText(leftButtonName);

        rightBtn = (Button)findViewById(R.id.rightBtn);
        rightBtn.setOnClickListener(this);
        rightBtn.setText(rightButtonName);

        edtInput = (EditText)findViewById(R.id.edtInput);
        if(textInputType != -1) {
            edtInput.setInputType(textInputType);
            if (textInputType == InputType.TYPE_TEXT_VARIATION_PASSWORD)
                edtInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        edtInput.setHint(txtHint);

        txtTitle = (TextView) findViewById(R.id.txtTitle); txtTitle.setText(title);
        title_bar = (LinearLayout) findViewById(R.id.title_bar);

        if(bShowTitleBar)
            title_bar.setVisibility(View.VISIBLE);
        else
            title_bar.setVisibility(View.GONE);

        //set focus and show keyboard when dialog is open
        edtInput.requestFocus();
        edtInput.postDelayed(new Runnable(){
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);
        edtInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)
                {
                    if(editTextActionListener!=null)
                        editTextActionListener.onEditorActionDone(InputDialog.this ,edtInput.getText().toString().trim() );
                    return true;
                }
                return false;
            }

        });


        /*edtInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });*/
        /*edtInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager inputMgr = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMgr.toggleSoftInput(0, 0);
                }
            }
        });*/

    }
    public void showKeyboard()
    {
        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtInput, InputMethodManager.SHOW_IMPLICIT);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //edtInput.requestLayout();
        //InputDialog.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);

    }
    public void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtInput.getWindowToken(), 0);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void setTextOnEditBox(String text)
    {
        edtInput.setText(text);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.leftBtn:
                if(mLeftClickListener != null)
                {
                    if(mLeftClickListener.onClick(this , (View)leftBtn , edtInput.getText().toString().trim()))
                    {
                        hideKeyboard();
                        dismiss();
                    }
                }
                else
                    hideKeyboard();
                break;
            case R.id.rightBtn:
                if(mRightClickListener != null)
                {
                    if(mRightClickListener.onClick(this , (View)rightBtn , edtInput.getText().toString().trim()))
                    {
                        hideKeyboard();
                        dismiss();
                    }
                }
                else
                    hideKeyboard();
                break;
        }
    }


    public interface OnButtonClickListener
    {
        public boolean onClick(Dialog dialog , View v , String input);
    }

    public interface OnEditorDoneActionListener
    {
        public void onEditorActionDone(Dialog dialog , String input);
    }


}
