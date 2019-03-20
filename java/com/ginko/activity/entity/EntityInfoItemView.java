package com.ginko.activity.entity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ginko.fragments.HomeWorkAddInfoFragment;
import com.ginko.ginko.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityInfoItemView extends LinearLayout
{
    private Context mContext;
    private EntityInfoItem item;
    private LayoutInflater inflater;

    private LinearLayout rootLayout;
    private ImageView imgSelectionCheck;
    private EditText edtInfo;

    private boolean isEditable = false;

    private EditFocusChangeListener focusChangeListener;

    private Handler mHandler;
    private Pattern pattern;
    private EmailValidationCheckRunnable emailCheckerThread;

    public EntityInfoItemView(Context context , EditFocusChangeListener focuslistener) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.focusChangeListener = focuslistener;
        mHandler = new Handler(this.mContext.getMainLooper());
    }

    public void setFocusChangeListener(EditFocusChangeListener focuslistener)
    {
        this.focusChangeListener = focuslistener;
    }


    public EntityInfoItemView(Context context,  EntityInfoItem _item , EditFocusChangeListener focuslistener , int emailInputType)
    {
        super(context);
        this.mContext = context;
        this.focusChangeListener = focuslistener;
        item  = _item;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.entity_info_list_item, this, true);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        imgSelectionCheck = (ImageView)findViewById(R.id.imgSelectionCheck);

        imgSelectionCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setIsSelected(!item.getIsSelected());
                if(item.getIsSelected())
                {
                    imgSelectionCheck.setImageResource(R.drawable.chatmessage_selected);
                }
                else
                {
                    imgSelectionCheck.setImageResource(R.drawable.chatmessage_nonsel);
                }
            }
        });

        edtInfo = (EditText)findViewById(R.id.edtInfo);
        edtInfo.setText(item.getFieldValue());
        edtInfo.setHint(item.getFieldName());
        edtInfo.setInputType(item.getTextInputType());

        if(item.getFieldName().toLowerCase().contains("email"))
        {
            edtInfo.setInputType(emailInputType);//special email input type in over android 5.0 keyboards
        }

        edtInfo.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(item.getFieldName().toLowerCase().contains("address") && !edtInfo.getText().toString().trim().equals("") && !hasFocus)
                {
                    if(focusChangeListener!=null)
                        focusChangeListener.onEditFocusChangeListener(hasFocus , edtInfo.getText().toString().trim(),  item);
                }
                else if(item.getFieldName().toLowerCase().contains("email") && hasFocus == false)
                {
                    if(emailCheckerThread == null) {
                        emailCheckerThread = new EmailValidationCheckRunnable(mContext , edtInfo.getText().toString());
                    }
                    else {
                        mHandler.removeCallbacks(emailCheckerThread);
                    }
                    emailCheckerThread.setEmailString(edtInfo.getText().toString());
                    if(mHandler == null)
                        mHandler = new Handler(mContext.getMainLooper());
                    if(mHandler!=null)
                        mHandler.postDelayed(emailCheckerThread , 500);
                }
            }
        });

        //get edtInfo's parent layout param
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) edtInfo.getLayoutParams();

        if(item.getFieldName().toLowerCase().contains("address") || item.getFieldName().toLowerCase().contains("hours"))
        {
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.height = mContext.getResources().getDimensionPixelSize(R.dimen.contact_profile_address_input_filed_height);
            edtInfo.setLayoutParams(params);
            edtInfo.setSingleLine(false);
            edtInfo.setMinLines(2);
            edtInfo.setLines(2);
        }
        else
        {
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            edtInfo.setLayoutParams(params);
            edtInfo.setSingleLine(true);
            edtInfo.setMaxLines(1);
            edtInfo.setLines(1);
        }

        rootLayout.requestLayout();

        refreshView();
    }
    public void setItem(EntityInfoItem _item)
    {
        this.item = _item;
    }
    public EntityInfoItem getItem(){return this.item;}

    public EditText getEditText(){return  this.edtInfo;}

    public void getCurrentEditTextString(){
        if(edtInfo == null)
            item.setFieldValue("");
        else
            item.setFieldValue(edtInfo.getText().toString().trim());
    }


    public void setIsEditable(boolean editable){this.isEditable = editable;}

    public void refreshData()
    {
        edtInfo.setText(item.getFieldValue());


        if(item.getFieldName().toLowerCase().equalsIgnoreCase("address"))
            System.out.println("-----edtInfo.setText( address="+item.getFieldValue()+"------");

        if(item.getIsSelected())
        {
            imgSelectionCheck.setImageResource(R.drawable.chatmessage_selected);
        }
        else
        {
            imgSelectionCheck.setImageResource(R.drawable.chatmessage_nonsel);
        }

        if(item.isDefaultField())
        {
            imgSelectionCheck.setVisibility(View.INVISIBLE);
        }
        else
        {
            if(isEditable)
                imgSelectionCheck.setVisibility(View.VISIBLE);
            else
                imgSelectionCheck.setVisibility(View.INVISIBLE);
        }


        rootLayout.requestLayout();

    }

    public void refreshView()
    {

        if(item.getIsSelected())
        {
            imgSelectionCheck.setImageResource(R.drawable.chatmessage_selected);
        }
        else
        {
            imgSelectionCheck.setImageResource(R.drawable.chatmessage_nonsel);
        }

        if(item.isDefaultField())
        {
            imgSelectionCheck.setVisibility(View.INVISIBLE);
        }
        else
        {
            if(isEditable)
                imgSelectionCheck.setVisibility(View.VISIBLE);
            else
                imgSelectionCheck.setVisibility(View.INVISIBLE);
        }


        rootLayout.requestLayout();

    }

    public interface EditFocusChangeListener
    {
        public void onEditFocusChangeListener(boolean hasFocus , String edtContent ,EntityInfoItem InfoItem);
    }

    private boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        if(pattern == null)
            pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private class EmailValidationCheckRunnable implements Runnable {
        private String email = "";
        private Context mContext;
        public  EmailValidationCheckRunnable(Context context, String emailContent)
        {
            this.email = emailContent;
            this.mContext = context;
        }

        public void setEmailString(String emailContent)
        {
            this.email = emailContent;
        }

        @Override
        public void run()
        {
            String strEmail = email.trim();
            if(strEmail.compareTo("") !=0 && !isEmailValid(strEmail))
            {
                Toast.makeText(mContext, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
            }
        }
    }

}