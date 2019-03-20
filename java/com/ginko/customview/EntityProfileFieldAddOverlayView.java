package com.ginko.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ginko.context.ConstValues;
import com.ginko.ginko.R;

import java.util.ArrayList;

public class EntityProfileFieldAddOverlayView extends LinearLayout implements View.OnClickListener{

    private final String[] strInfoNames = {
            //"Name",
            //"Keysearch",
            "Address",
            "Address#2",
            "Mobile",
            "Mobile#2",
            "Mobile#3",
            "Phone",
            "Phone#2",
            "Phone#3",
            "Email",
            "Email#2",
            "Fax",
            "Hours",
            "Facebook",
            "Twitter",
            "LinkedIn",
            "Website",
            "Custom",
            "Custom#2",
            "Custom#3",
    };

    private final String[] strFiledTypeNames = {
            //"name",
            //"keysearch",
            "address",
            "address",
            "phone",
            "phone",
            "phone",
            "phone",
            "phone",
            "phone",
            "email",
            "email",
            "fax",
            "hours",
            "facebook",
            "twitter",
            "linkedin",
            "url",
            "custom",
            "custom",
            "custom",
    };
    private RelativeLayout hoursFieldLayout;      private ImageView imgFieldHours;
    private RelativeLayout mobileFieldLayout;       private ImageView imgFieldMobile;       private ImageView imgFieldMobileBadgeNumber;
    private RelativeLayout phoneFieldLayout;        private ImageView imgFieldPhone;        private ImageView imgFieldPhoneBadgeNumber;
    private RelativeLayout emailFieldLayout;        private ImageView imgFieldEmail;        private ImageView imgFieldEmailBadgeNumber;
    private RelativeLayout addressFieldLayout;      private ImageView imgFieldAddress;      private ImageView imgFieldAddressBadgeNumber;
    private RelativeLayout faxFieldLayout;           private ImageView imgFieldFax;
    private RelativeLayout facebookFieldLayout;     private ImageView imgFieldFacebook;
    private RelativeLayout twitterFieldLayout;      private ImageView imgFieldTwitter;
    private RelativeLayout linkedinFieldLayout;     private ImageView imgFieldLinkedIn;
    private RelativeLayout websiteFieldLayout;      private ImageView imgFieldWebsite;
    private RelativeLayout customFieldLayout;       private ImageView imgFieldCustom;       private ImageView imgFieldCustomBadgeNumber;


    private Context mContext;
    private OnProfileFieldItemsChangeListener profileChangedListener;

    private int mobileCount = 0;
    private int phoneCount = 0;
    private int emailCount = 0;
    private int addressCount = 0;
    private int customCount = 0;

    private ArrayList<String> profileFieldItems;

    public EntityProfileFieldAddOverlayView(Context context) {
        this(context, null);
    }

    public EntityProfileFieldAddOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EntityProfileFieldAddOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.add_entity_profile_field_items_overlayview, this);

        init(context);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EntityProfileFieldAddOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    //set the Home or Work profile group info
    public void setProfileFieldItems(ArrayList<String> fieldItems)
    {
        this.profileFieldItems = fieldItems;

        refreshView();
    }

    public void setOnProfileFieldItemsChangeListener(OnProfileFieldItemsChangeListener changedListener)
    {
        this.profileChangedListener = changedListener;
    }

    private void init(Context context)
    {
        //field item layouts
        hoursFieldLayout = (RelativeLayout)findViewById(R.id.hoursFieldLayout);
        mobileFieldLayout = (RelativeLayout)findViewById(R.id.mobileFieldLayout);
        phoneFieldLayout = (RelativeLayout)findViewById(R.id.phoneFieldLayout);
        emailFieldLayout = (RelativeLayout)findViewById(R.id.emailFieldLayout);
        addressFieldLayout = (RelativeLayout)findViewById(R.id.addressFieldLayout);
        faxFieldLayout = (RelativeLayout)findViewById(R.id.faxFieldLayout);
        facebookFieldLayout = (RelativeLayout)findViewById(R.id.facebookFieldLayout);
        twitterFieldLayout = (RelativeLayout)findViewById(R.id.twitterFieldLayout);
        linkedinFieldLayout = (RelativeLayout)findViewById(R.id.linkedinFieldLayout);
        websiteFieldLayout = (RelativeLayout)findViewById(R.id.websiteFieldLayout);
        customFieldLayout = (RelativeLayout)findViewById(R.id.customFieldLayout);

        imgFieldHours = (ImageView)findViewById(R.id.imgFieldHours);        imgFieldHours.setOnClickListener(this);
        imgFieldMobile = (ImageView)findViewById(R.id.imgFieldMobile);      imgFieldMobile.setOnClickListener(this);
        imgFieldPhone = (ImageView)findViewById(R.id.imgFieldPhone);        imgFieldPhone.setOnClickListener(this);
        imgFieldEmail = (ImageView)findViewById(R.id.imgFieldEmail);        imgFieldEmail.setOnClickListener(this);
        imgFieldAddress = (ImageView)findViewById(R.id.imgFieldAddress);    imgFieldAddress.setOnClickListener(this);
        imgFieldFax = (ImageView)findViewById(R.id.imgFieldFax);              imgFieldFax.setOnClickListener(this);
        imgFieldFacebook = (ImageView)findViewById(R.id.imgFieldFacebook);  imgFieldFacebook.setOnClickListener(this);
        imgFieldTwitter = (ImageView)findViewById(R.id.imgFieldTwitter);    imgFieldTwitter.setOnClickListener(this);
        imgFieldLinkedIn = (ImageView)findViewById(R.id.imgFieldLinkedIn);  imgFieldLinkedIn.setOnClickListener(this);
        imgFieldWebsite = (ImageView)findViewById(R.id.imgFieldWebsite);    imgFieldWebsite.setOnClickListener(this);
        imgFieldCustom = (ImageView)findViewById(R.id.imgFieldCustom);      imgFieldCustom.setOnClickListener(this);

        imgFieldMobileBadgeNumber = (ImageView)findViewById(R.id.imgFieldMobileBadgeNumber);
        imgFieldPhoneBadgeNumber = (ImageView)findViewById(R.id.imgFieldPhoneBadgeNumber);
        imgFieldEmailBadgeNumber = (ImageView)findViewById(R.id.imgFieldEmailBadgeNumber);
        imgFieldAddressBadgeNumber = (ImageView)findViewById(R.id.imgFieldAddressBadgeNumber);
        imgFieldCustomBadgeNumber = (ImageView)findViewById(R.id.imgFieldCustomBadgeNumber);
    }

    public void showView()
    {
        refreshView();
        setVisibility(View.VISIBLE);
    }

    public void hideView()
    {
        refreshView();
        setVisibility(View.GONE);
    }

    public void hideOverlapView()
    {
        setVisibility(View.GONE);
    }

    public void refreshView()
    {
        boolean bIsExistAddField = false;
        if(profileFieldItems == null) return;

        hoursFieldLayout.setVisibility(View.VISIBLE);
        mobileFieldLayout.setVisibility(View.VISIBLE);
        phoneFieldLayout.setVisibility(View.VISIBLE);
        emailFieldLayout.setVisibility(View.VISIBLE);
        addressFieldLayout.setVisibility(View.VISIBLE);
        imgFieldAddressBadgeNumber.setVisibility(View.GONE);
        faxFieldLayout.setVisibility(View.VISIBLE);
        facebookFieldLayout.setVisibility(View.VISIBLE);
        twitterFieldLayout.setVisibility(View.VISIBLE);
        linkedinFieldLayout.setVisibility(View.VISIBLE);
        websiteFieldLayout.setVisibility(View.VISIBLE);
        customFieldLayout.setVisibility(View.VISIBLE);

        mobileCount = 0;
        phoneCount = 0;
        emailCount = 0;
        addressCount = 0;
        customCount = 0;

        for(String fieldName : profileFieldItems)
        {
            if(fieldName.toLowerCase().contains("description"))
                continue;
            if(fieldName.toLowerCase().contains("hours"))
                hoursFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("mobile")) {
                mobileCount++;
            }
            else if(fieldName.toLowerCase().contains("phone")) {
                phoneCount++;
            }
            else if(fieldName.toLowerCase().contains("email")) {
                emailCount++;
            }
            else if(fieldName.toLowerCase().contains("address")) {
                //addressCount++;
                addressFieldLayout.setVisibility(GONE);
            }
            else if(fieldName.toLowerCase().contains("fax"))
                faxFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("facebook"))
                facebookFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("twitter"))
                twitterFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("linkedin"))
                linkedinFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("website"))
                websiteFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("custom")) {
                customCount++;
            }
        }


        if(mobileCount>0) {
            imgFieldMobileBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldMobileBadgeNumber.setImageResource(getBadgeNumberResId(mobileCount));
            bIsExistAddField = true;
        }
        else {
            imgFieldMobileBadgeNumber.setVisibility(View.GONE);
        }

        if(mobileCount<3) {
            mobileFieldLayout.setVisibility(View.VISIBLE);
            bIsExistAddField = true;
        }
        else {
            mobileFieldLayout.setVisibility(View.GONE);
        }

        if(phoneCount>0) {
            imgFieldPhoneBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldPhoneBadgeNumber.setImageResource(getBadgeNumberResId(phoneCount));
            bIsExistAddField = true;
        }
        else {
            imgFieldPhoneBadgeNumber.setVisibility(View.GONE);
        }

        if(phoneCount<3) {
            phoneFieldLayout.setVisibility(View.VISIBLE);
            bIsExistAddField = true;
        }
        else {
            phoneFieldLayout.setVisibility(View.GONE);
        }

        if(emailCount>0) {
            imgFieldEmailBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldEmailBadgeNumber.setImageResource(getBadgeNumberResId(emailCount));
            bIsExistAddField = true;
        }
        else {
            imgFieldEmailBadgeNumber.setVisibility(View.GONE);
        }

        if(emailCount<2) {
            emailFieldLayout.setVisibility(View.VISIBLE);
            bIsExistAddField = true;
        }
        else {
            emailFieldLayout.setVisibility(View.GONE);
        }

        /*if(addressCount>0) {
            imgFieldAddressBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldAddressBadgeNumber.setImageResource(getBadgeNumberResId(addressCount));
            bIsExistAddField = true;
        }
        else {
            imgFieldAddressBadgeNumber.setVisibility(View.GONE);
        }

        if(addressCount<2) {
            addressFieldLayout.setVisibility(View.VISIBLE);
            bIsExistAddField = true;
        }
        else {
            addressFieldLayout.setVisibility(View.GONE);
        }*/

        if(customCount>0) {
            imgFieldCustomBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldCustomBadgeNumber.setImageResource(getBadgeNumberResId(customCount));
            bIsExistAddField = true;
        }
        else {
            imgFieldCustomBadgeNumber.setVisibility(View.GONE);
        }

        if(customCount<3) {
            customFieldLayout.setVisibility(View.VISIBLE);
            bIsExistAddField = true;
        }
        else {
            customFieldLayout.setVisibility(View.GONE);
        }

        if (hoursFieldLayout.getVisibility() == GONE  && mobileFieldLayout.getVisibility() == GONE &&
                phoneFieldLayout.getVisibility() == GONE && emailFieldLayout.getVisibility() == GONE &&
                addressFieldLayout.getVisibility() == GONE && imgFieldAddressBadgeNumber.getVisibility() == GONE &&
                twitterFieldLayout.getVisibility() == GONE && linkedinFieldLayout.getVisibility() == GONE &&
                websiteFieldLayout.getVisibility() == GONE && customFieldLayout.getVisibility() == GONE &&
                faxFieldLayout.getVisibility() == GONE && facebookFieldLayout.getVisibility() == GONE) {
            profileChangedListener.onAddedNewProfileField("noExistAddFields");
        }
    }

    private int getBadgeNumberResId(int badgeNumber)
    {
        switch (badgeNumber)
        {
            case 1:
                return R.drawable.field_badge_number1;
            case 2:
                return R.drawable.field_badge_number2;
            case 3:
                return R.drawable.field_badge_number3;
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        setClickable(true);  // for get Focus only this window.
        if(profileFieldItems == null)
            return;
        switch(v.getId())
        {

            case R.id.imgFieldTitle:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Title");
                profileFieldItems.add("Title");
                break;

            case R.id.imgFieldHours:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Hours");
                profileFieldItems.add("Hours");
                break;

            case R.id.imgFieldMobile:
                mobileCount++;
                if(mobileCount > 3) return;

                if(imgFieldMobileBadgeNumber.getVisibility() == View.GONE)
                    imgFieldMobileBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldMobileBadgeNumber.setImageResource(getBadgeNumberResId(mobileCount));

                if(profileChangedListener != null) {
                    if(mobileCount < 2) {
                        profileChangedListener.onAddedNewProfileField("Mobile");
                        profileFieldItems.add("Mobile");
                    }
                    else {
                        if(!profileFieldItems.contains("Mobile"))
                        {
                            profileChangedListener.onAddedNewProfileField("Mobile");
                            profileFieldItems.add("Mobile");
                        }
                        else {
                            for (int i = 2; i <= mobileCount; i++) {
                                if (!profileFieldItems.contains("Mobile#" + String.valueOf(i))) {
                                    profileChangedListener.onAddedNewProfileField("Mobile#" + String.valueOf(i));
                                    profileFieldItems.add("Mobile#" + String.valueOf(i));
                                    break;
                                }
                            }
                        }
                    }
                }

                break;

            case R.id.imgFieldPhone:
                phoneCount++;
                if(phoneCount > 3) return;

                if(imgFieldPhoneBadgeNumber.getVisibility() == View.GONE)
                    imgFieldPhoneBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldPhoneBadgeNumber.setImageResource(getBadgeNumberResId(phoneCount));

                if(profileChangedListener != null) {
                    if(phoneCount < 2) {
                        profileChangedListener.onAddedNewProfileField("Phone");
                        profileFieldItems.add("Phone");
                    }
                    else {
                        if(!profileFieldItems.contains("Phone"))
                        {
                            profileChangedListener.onAddedNewProfileField("Phone");
                            profileFieldItems.add("Phone");
                        }
                        else {
                            for (int i = 2; i <= phoneCount; i++) {
                                if (!profileFieldItems.contains("Phone#" + String.valueOf(i))) {
                                    profileChangedListener.onAddedNewProfileField("Phone#" + String.valueOf(i));
                                    profileFieldItems.add("Phone#" + String.valueOf(i));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;

            case R.id.imgFieldEmail:
                emailCount++;
                if(emailCount > 2) return;
                if(imgFieldEmailBadgeNumber.getVisibility() == View.GONE)
                    imgFieldEmailBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldEmailBadgeNumber.setImageResource(getBadgeNumberResId(emailCount));

                if(profileChangedListener != null) {
                    if(emailCount < 2) {
                        profileChangedListener.onAddedNewProfileField("Email");
                        profileFieldItems.add("Email");
                    }
                    else {
                        if(!profileFieldItems.contains("Email"))
                        {
                            profileChangedListener.onAddedNewProfileField("Email");
                            profileFieldItems.add("Email");
                        }
                        else {
                            for (int i = 2; i <= emailCount; i++) {
                                if (!profileFieldItems.contains("Email#" + String.valueOf(i))) {
                                    profileChangedListener.onAddedNewProfileField("Email#" + String.valueOf(i));
                                    profileFieldItems.add("Email#" + String.valueOf(i));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;

            case R.id.imgFieldAddress:
                addressCount++;
                if(addressCount > 2) return;
                if(imgFieldAddressBadgeNumber.getVisibility() == View.GONE)
                    imgFieldAddressBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldAddressBadgeNumber.setImageResource(getBadgeNumberResId(addressCount));

                if(profileChangedListener != null) {
                    if(addressCount < 2) {
                        profileChangedListener.onAddedNewProfileField("Address");
                        profileFieldItems.add("Address");
                    }
                    else {
                        if(!profileFieldItems.contains("Address"))
                        {
                            profileChangedListener.onAddedNewProfileField("Address");
                            profileFieldItems.add("Address");
                        }
                        else {
                            for (int i = 2; i <= addressCount; i++) {
                                if (!profileFieldItems.contains("Address#" + String.valueOf(i))) {
                                    profileChangedListener.onAddedNewProfileField("Address#" + String.valueOf(i));
                                    profileFieldItems.add("Address#" + String.valueOf(i));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;

            case R.id.imgFieldFax:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Fax");
                profileFieldItems.add("Fax");

                break;

            case R.id.imgFieldFacebook:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Facebook");
                profileFieldItems.add("Facebook");
                break;

            case R.id.imgFieldTwitter:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Twitter");
                profileFieldItems.add("Twitter");
                break;

            case R.id.imgFieldLinkedIn:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("LinkedIn");
                profileFieldItems.add("LinkedIn");
                break;

            case R.id.imgFieldWebsite:
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Website");
                profileFieldItems.add("Website");
                break;

            case R.id.imgFieldCustom:
                customCount++;
                if(customCount > 3) return;
                if(imgFieldCustomBadgeNumber.getVisibility() == View.GONE)
                    imgFieldCustomBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldCustomBadgeNumber.setImageResource(getBadgeNumberResId(customCount));

                if(profileChangedListener != null) {
                    if(customCount < 2) {
                        profileChangedListener.onAddedNewProfileField("Custom");
                        profileFieldItems.add("Custom");
                    }
                    else {
                        if(!profileFieldItems.contains("Custom"))
                        {
                            profileChangedListener.onAddedNewProfileField("Custom");
                            profileFieldItems.add("Custom");
                        }
                        else {
                            for (int i = 2; i <= customCount; i++) {
                                if (!profileFieldItems.contains("Custom#" + String.valueOf(i))) {
                                    profileChangedListener.onAddedNewProfileField("Custom#" + String.valueOf(i));
                                    profileFieldItems.add("Custom#" + String.valueOf(i));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
        }
        refreshView();
    }

    public interface OnProfileFieldItemsChangeListener
    {
        public void onAddedNewProfileField(String fieldName);
        public void onRemovedProfileField(String fieldName);
    }

}
