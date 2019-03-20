package com.ginko.customview;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.UserProfileVO;

import java.util.Locale;

public class PersonalProfilePreviewFieldItemView extends LinearLayout{
    private Context mContext;
    private UserProfileVO itemInfo;
    private ImageView imgFieldIcon;
    private TextView txtFieldValue;

    public PersonalProfilePreviewFieldItemView(UserProfileVO itemInfo , Context context) {
        this(context);
        this.itemInfo = itemInfo;
        init(context);
    }

    public PersonalProfilePreviewFieldItemView(Context context) {
        this(context, null);
    }

    public PersonalProfilePreviewFieldItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersonalProfilePreviewFieldItemView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.personal_profile_preview_field_item, this);

        imgFieldIcon = (ImageView)findViewById(R.id.imgFieldIcon);
        txtFieldValue = (TextView)findViewById(R.id.txtFieldValue);

        txtFieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activityName = mContext.getClass().getSimpleName();
                if(activityName.equals("PersonalProfilePreviewActivity"))
                    return;

                if(itemInfo == null) return;
                String fieldValue = itemInfo.getValue();
                String fieldType = itemInfo.getFieldType();
                String fieldName = itemInfo.getFieldName();
                if(fieldValue.trim().equals("")) return;
                if(fieldType.equalsIgnoreCase("phone") || fieldType.equalsIgnoreCase("mobile"))
                {
                    try
                    {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + fieldValue.trim()));
                        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                            mContext.startActivity(intent);
                        }
                    }catch(Exception e){e.printStackTrace();}
                }
                else if(fieldType.equalsIgnoreCase("email"))
                {
                    try {
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.putExtra(Intent.EXTRA_EMAIL, new String[]{fieldValue.trim()});
                        email.putExtra(Intent.EXTRA_SUBJECT, "");
                        email.putExtra(Intent.EXTRA_TEXT, "");

                        // need this to prompts email client only
                        email.setType("message/rfc822");

                        mContext.startActivity(Intent.createChooser(email, "Choose an Email client"));
                    }catch (Exception e){e.printStackTrace();}
                }
                else if(fieldType.equalsIgnoreCase("address"))
                {
                    try
                    {
                        String uri = String.format(Locale.ENGLISH, "geo:0,0?q=%s", fieldValue.trim());
                        Intent goolgeMapIntent = new Intent(Intent.ACTION_VIEW , Uri.parse(uri));
                        //goolgeMapIntent.setData(geoLocation);
                        // check if Google Maps is supported on given device
                        if (goolgeMapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                            mContext.startActivity(goolgeMapIntent);
                        }
                        else
                        {
                            //open my ginko lcoation viewer activity
                            Intent locationMessageIntent = new Intent(mContext, LocationMapViewerActivity.class);
                            locationMessageIntent.putExtra("address" , fieldValue.trim());

                            mContext.startActivity(locationMessageIntent);
                        }
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PersonalProfilePreviewFieldItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context)
    {
        if(this.itemInfo == null) return;

        String strFieldName = itemInfo.getFieldName();

        if(strFieldName.toLowerCase().contains("name"))
        {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_title);
        }
        else if (strFieldName.toLowerCase().contains("title")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_title);
        }
        else if (strFieldName.toLowerCase().contains("company")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_company);
        }
        else if (strFieldName.toLowerCase().contains("mobile")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_mobile);
        }
        else if (strFieldName.toLowerCase().contains("phone")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_phone);
        }
        else if (strFieldName.toLowerCase().contains("email")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_email);
        }
        else if (strFieldName.toLowerCase().contains("address")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_address);
        }
        else if (strFieldName.toLowerCase().contains("fax")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_fax);
        }
        else if (strFieldName.toLowerCase().contains("birthday")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_birthday);
        }
        else if (strFieldName.toLowerCase().contains("facebook")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_facebook);
        }
        else if (strFieldName.toLowerCase().contains("twitter")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_twitter);
        }
        else if (strFieldName.toLowerCase().contains("linkedin")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_linkedin);
        }
        else if (strFieldName.toLowerCase().contains("website")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_website);
        }
        else if (strFieldName.toLowerCase().contains("custom")) {
            imgFieldIcon.setImageResource(R.drawable.field_icon_grey_custom);
        }

        txtFieldValue.setText(this.itemInfo.getValue());

        if(strFieldName.toLowerCase().trim().contains("website"))
        {
            txtFieldValue.setLinksClickable(true);
            Linkify.addLinks(txtFieldValue, Linkify.WEB_URLS);

                        /*try {
                            String url = fieldValue.trim();
                            if(!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://"+fieldValue.trim();
                            Uri webpage = Uri.parse(url);
                            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                mContext.startActivity(intent);
                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }*/
        }
        else if(strFieldName.toLowerCase().trim().contains("facebook"))
        {
            txtFieldValue.setLinksClickable(true);
            Linkify.addLinks(txtFieldValue, Linkify.WEB_URLS);
        }
        else if(strFieldName.toLowerCase().trim().contains("twitter"))
        {
            txtFieldValue.setLinksClickable(true);
            Linkify.addLinks(txtFieldValue, Linkify.WEB_URLS);
        }
        else if(strFieldName.toLowerCase().trim().contains("linkedin"))
        {
            txtFieldValue.setLinksClickable(true);
            Linkify.addLinks(txtFieldValue, Linkify.WEB_URLS);
        }
        else if(strFieldName.toLowerCase().trim().contains("custom"))
        {
            if(itemInfo.getValue().contains("http://") || itemInfo.getValue().contains("https://") || itemInfo.getValue().contains("www."))
            {
                txtFieldValue.setLinksClickable(true);
                Linkify.addLinks(txtFieldValue, Linkify.WEB_URLS);
            }
        }
    }
}
