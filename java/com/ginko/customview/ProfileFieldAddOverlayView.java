package com.ginko.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ginko.context.ConstValues;
import com.ginko.ginko.R;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;

import java.util.ArrayList;

public class ProfileFieldAddOverlayView extends LinearLayout implements View.OnClickListener{

    private final String[] strInfoNames = {
            "Name",
            "Company",
            "Title",
            "Mobile",
            "Mobile#2",
            "Mobile#3",
            "Phone",
            "Phone#2",
            "Phone#3",
            "Fax",
            "Email",
            "Email#2",
            "Address",
            "Address#2",
            "Hours",
            "Birthday",
            "Facebook",
            "Twitter",
            "LinkedIn",
            "Website",
            "Custom",
            "Custom#2",
            "Custom#3",
    };
    private final String[] strFiledTypeNames = {
            ConstValues.PROFILE_FIELD_TYPE_NAME,
            ConstValues.PROFILE_FIELD_TYPE_COMPANY,
            ConstValues.PROFILE_FIELD_TYPE_TITLE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_FAX,
            ConstValues.PROFILE_FIELD_TYPE_EMAIL,
            ConstValues.PROFILE_FIELD_TYPE_EMAIL,
            ConstValues.PROFILE_FIELD_TYPE_ADDRESS,
            ConstValues.PROFILE_FIELD_TYPE_ADDRESS,
            ConstValues.PROFILE_FIELD_TYPE_HOURS,
            ConstValues.PROFILE_FIELD_TYPE_DATE,
            ConstValues.PROFILE_FIELD_TYPE_FACEBOOK,
            ConstValues.PROFILE_FIELD_TYPE_TWITTER,
            ConstValues.PROFILE_FIELD_TYPE_LINKEDIN,
            ConstValues.PROFILE_FIELD_TYPE_WEBSITE,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
    };


    private RelativeLayout titleFieldLayout;        private ImageView imgFieldTitle;
    private RelativeLayout companyFieldLayout;      private ImageView imgFieldCompany;
    private RelativeLayout mobileFieldLayout;       private ImageView imgFieldMobile;       private ImageView imgFieldMobileBadgeNumber;
    private RelativeLayout phoneFieldLayout;        private ImageView imgFieldPhone;        private ImageView imgFieldPhoneBadgeNumber;
    private RelativeLayout emailFieldLayout;        private ImageView imgFieldEmail;        private ImageView imgFieldEmailBadgeNumber;
    private RelativeLayout addressFieldLayout;      private ImageView imgFieldAddress;      private ImageView imgFieldAddressBadgeNumber;
    private RelativeLayout faxFieldLayout;          private ImageView imgFieldFax;
    private RelativeLayout hoursFieldLayout;        private ImageView imgFieldHour;
    private RelativeLayout birthdayFieldLayout;     private ImageView imgFieldBirthday;
    private RelativeLayout facebookFieldLayout;     private ImageView imgFieldFacebook;
    private RelativeLayout twitterFieldLayout;      private ImageView imgFieldTwitter;
    private RelativeLayout linkedinFieldLayout;     private ImageView imgFieldLinkedIn;
    private RelativeLayout websiteFieldLayout;      private ImageView imgFieldWebsite;
    private RelativeLayout customFieldLayout;       private ImageView imgFieldCustom;       private ImageView imgFieldCustomBadgeNumber;


    private Context mContext;
    private OnProfileFieldItemsChangeListener profileChangedListener;

    private final int GROUP_HOME = 1;
    private final int GROUP_WORK = 2;
    private final int GROUP_GREY = 3;
    private int group_type = 1; //

    //user profile info
    private String type;

    private int mobileCount = 0;
    private int phoneCount = 0;
    private int emailCount = 0;
    private int addressCount = 0;
    private int customCount = 0;

    private ArrayList<String> profileFieldItems;

    public ProfileFieldAddOverlayView(Context context) {
        this(context, null);
    }

    public ProfileFieldAddOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfileFieldAddOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.add_profile_field_items_overlayview, this);

        init(context);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProfileFieldAddOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    //set the Home or Work profile group info
    public void setProfileFieldItems(String groupType , ArrayList<String> fieldItems)
    {
        this.type = groupType;
        if(groupType.equalsIgnoreCase("home"))
            group_type = GROUP_HOME;
        else if(groupType.equalsIgnoreCase("work"))
            group_type = GROUP_WORK;
        else
            group_type = GROUP_GREY;

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
        companyFieldLayout = (RelativeLayout)findViewById(R.id.companyFieldLayout);
        titleFieldLayout = (RelativeLayout)findViewById(R.id.titleFieldLayout);

        mobileFieldLayout = (RelativeLayout)findViewById(R.id.mobileFieldLayout);
        phoneFieldLayout = (RelativeLayout)findViewById(R.id.phoneFieldLayout);
        emailFieldLayout = (RelativeLayout)findViewById(R.id.emailFieldLayout);
        addressFieldLayout = (RelativeLayout)findViewById(R.id.addressFieldLayout);
        faxFieldLayout = (RelativeLayout)findViewById(R.id.faxFieldLayout);
        hoursFieldLayout = (RelativeLayout)findViewById(R.id.hoursFieldLayout);
        birthdayFieldLayout = (RelativeLayout)findViewById(R.id.birthdayFieldLayout);
        facebookFieldLayout = (RelativeLayout)findViewById(R.id.facebookFieldLayout);
        twitterFieldLayout = (RelativeLayout)findViewById(R.id.twitterFieldLayout);
        linkedinFieldLayout = (RelativeLayout)findViewById(R.id.linkedinFieldLayout);
        websiteFieldLayout = (RelativeLayout)findViewById(R.id.websiteFieldLayout);
        customFieldLayout = (RelativeLayout)findViewById(R.id.customFieldLayout);

        imgFieldTitle = (ImageView)findViewById(R.id.imgFieldTitle);        imgFieldTitle.setOnClickListener(this);
        imgFieldCompany = (ImageView)findViewById(R.id.imgFieldCompany);    imgFieldCompany.setOnClickListener(this);
        imgFieldMobile = (ImageView)findViewById(R.id.imgFieldMobile);      imgFieldMobile.setOnClickListener(this);
        imgFieldPhone = (ImageView)findViewById(R.id.imgFieldPhone);        imgFieldPhone.setOnClickListener(this);
        imgFieldEmail = (ImageView)findViewById(R.id.imgFieldEmail);        imgFieldEmail.setOnClickListener(this);
        imgFieldAddress = (ImageView)findViewById(R.id.imgFieldAddress);    imgFieldAddress.setOnClickListener(this);
        imgFieldFax = (ImageView)findViewById(R.id.imgFieldFax);            imgFieldFax.setOnClickListener(this);
        imgFieldHour = (ImageView)findViewById(R.id.imgFieldHours);         imgFieldHour.setOnClickListener(this);
        imgFieldBirthday = (ImageView)findViewById(R.id.imgFieldBirthday);  imgFieldBirthday.setOnClickListener(this);
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
        if(profileFieldItems == null) return;

        titleFieldLayout.setVisibility(View.VISIBLE);
        companyFieldLayout.setVisibility(View.VISIBLE);
        mobileFieldLayout.setVisibility(View.VISIBLE);
        phoneFieldLayout.setVisibility(View.VISIBLE);
        emailFieldLayout.setVisibility(View.VISIBLE);
        addressFieldLayout.setVisibility(View.VISIBLE);
        faxFieldLayout.setVisibility(View.VISIBLE);
        hoursFieldLayout.setVisibility(View.VISIBLE);
        birthdayFieldLayout.setVisibility(View.VISIBLE);
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
            if(fieldName.toLowerCase().contains("name"))
                continue;
            else if(fieldName.toLowerCase().contains("title"))
                titleFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("company"))
                companyFieldLayout.setVisibility(View.GONE);
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
                addressCount++;
            }
            else if(fieldName.toLowerCase().contains("fax"))
                faxFieldLayout.setVisibility(View.GONE);
            else if (fieldName.toLowerCase().contains("hours"))
                hoursFieldLayout.setVisibility(View.GONE);
            else if(fieldName.toLowerCase().contains("birthday"))
                birthdayFieldLayout.setVisibility(View.GONE);
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

        //home porifle
        if(group_type == GROUP_HOME)
        {
            titleFieldLayout.setVisibility(View.GONE);
            companyFieldLayout.setVisibility(View.GONE);
        }
        else if (group_type == GROUP_GREY)
            titleFieldLayout.setVisibility(View.GONE);


        if(mobileCount>0) {
            imgFieldMobileBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldMobileBadgeNumber.setImageResource(getBadgeNumberResId(mobileCount));
        }
        else
            imgFieldMobileBadgeNumber.setVisibility(View.GONE);

        if(mobileCount<3) {
            mobileFieldLayout.setVisibility(View.VISIBLE);
        }
        else
            mobileFieldLayout.setVisibility(View.GONE);

        if(phoneCount>0) {
            imgFieldPhoneBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldPhoneBadgeNumber.setImageResource(getBadgeNumberResId(phoneCount));
        }
        else
            imgFieldPhoneBadgeNumber.setVisibility(View.GONE);

        if(phoneCount<3)
            phoneFieldLayout.setVisibility(View.VISIBLE);
        else
            phoneFieldLayout.setVisibility(View.GONE);

        if(emailCount>0) {
            imgFieldEmailBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldEmailBadgeNumber.setImageResource(getBadgeNumberResId(emailCount));
        }
        else
            imgFieldEmailBadgeNumber.setVisibility(View.GONE);

        if(emailCount<2)
            emailFieldLayout.setVisibility(View.VISIBLE);
        else
            emailFieldLayout.setVisibility(View.GONE);

        if(addressCount>0) {
            imgFieldAddressBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldAddressBadgeNumber.setImageResource(getBadgeNumberResId(addressCount));
        }
        else
            imgFieldAddressBadgeNumber.setVisibility(View.GONE);

        if(addressCount<2)
            addressFieldLayout.setVisibility(View.VISIBLE);
        else
            addressFieldLayout.setVisibility(View.GONE);

        if(customCount>0) {
            imgFieldCustomBadgeNumber.setVisibility(View.VISIBLE);
            imgFieldCustomBadgeNumber.setImageResource(getBadgeNumberResId(customCount));
        }
        else
            imgFieldCustomBadgeNumber.setVisibility(View.GONE);

        if(customCount<3)
            customFieldLayout.setVisibility(View.VISIBLE);
        else
            customFieldLayout.setVisibility(View.GONE);
    }

    public boolean isAllFieldAdded()
    {
        boolean result = true;
        if(group_type == GROUP_WORK)
        {
            result = result & titleFieldLayout.getVisibility()==View.GONE?true:false;
            result = result & companyFieldLayout.getVisibility()==View.GONE?true:false;
        }
        else if (group_type == GROUP_GREY)
        {
            result = result & companyFieldLayout.getVisibility()==View.GONE?true:false;
        }
        result = result & mobileFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & phoneFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & emailFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & addressFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & faxFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & hoursFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & birthdayFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & facebookFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & twitterFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & linkedinFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & websiteFieldLayout.getVisibility()==View.GONE?true:false;
        result = result & customFieldLayout.getVisibility()==View.GONE?true:false;

        return result;
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
        if(profileFieldItems == null)
            return;
        switch(v.getId())
        {
            case R.id.imgFieldTitle:
                profileFieldItems.add("Title");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Title");
                break;

            case R.id.imgFieldCompany:
                profileFieldItems.add("Company");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Company");
                break;

            case R.id.imgFieldMobile:
                mobileCount++;
                if(mobileCount > 3) return;

                if(imgFieldMobileBadgeNumber.getVisibility() == View.GONE)
                    imgFieldMobileBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldMobileBadgeNumber.setImageResource(getBadgeNumberResId(mobileCount));

                if(profileChangedListener != null) {
                    if(mobileCount < 2) {
                        profileFieldItems.add("Mobile");
                        profileChangedListener.onAddedNewProfileField("Mobile");
                    }
                    else {
                        if(!profileFieldItems.contains("Mobile"))
                        {
                            profileFieldItems.add("Mobile");
                            profileChangedListener.onAddedNewProfileField("Mobile");
                        }
                        else {
                            for (int i = 2; i <= mobileCount; i++) {
                                if (!profileFieldItems.contains("Mobile#" + String.valueOf(i))) {
                                    profileFieldItems.add("Mobile#" + String.valueOf(i));
                                    profileChangedListener.onAddedNewProfileField("Mobile#" + String.valueOf(i));
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
                        profileFieldItems.add("Phone");
                        profileChangedListener.onAddedNewProfileField("Phone");
                    }
                    else {
                        if(!profileFieldItems.contains("Phone"))
                        {
                            profileFieldItems.add("Phone");
                            profileChangedListener.onAddedNewProfileField("Phone");
                        }
                        else {
                            for (int i = 2; i <= phoneCount; i++) {
                                if (!profileFieldItems.contains("Phone#" + String.valueOf(i))) {
                                    profileFieldItems.add("Phone#" + String.valueOf(i));
                                    profileChangedListener.onAddedNewProfileField("Phone#" + String.valueOf(i));
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
                        profileFieldItems.add("Email");
                        profileChangedListener.onAddedNewProfileField("Email");
                    }
                    else {
                        if(!profileFieldItems.contains("Email"))
                        {
                            profileFieldItems.add("Email");
                            profileChangedListener.onAddedNewProfileField("Email");
                        }
                        else {
                            for (int i = 2; i <= emailCount; i++) {
                                if (!profileFieldItems.contains("Email#" + String.valueOf(i))) {
                                    profileFieldItems.add("Email#" + String.valueOf(i));
                                    profileChangedListener.onAddedNewProfileField("Email#" + String.valueOf(i));
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
                        profileFieldItems.add("Address");
                        profileChangedListener.onAddedNewProfileField("Address");
                    }
                    else {
                        if(!profileFieldItems.contains("Address"))
                        {
                            profileFieldItems.add("Address");
                            profileChangedListener.onAddedNewProfileField("Address");
                        }
                        else {
                            for (int i = 2; i <= addressCount; i++) {
                                if (!profileFieldItems.contains("Address#" + String.valueOf(i))) {
                                    profileFieldItems.add("Address#" + String.valueOf(i));
                                    profileChangedListener.onAddedNewProfileField("Address#" + String.valueOf(i));
                                    break;
                                }
                            }
                        }
                    }
                }
                break;

            case R.id.imgFieldHours:
                profileFieldItems.add("Hours");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Hours");

                break;

            case R.id.imgFieldFax:
                profileFieldItems.add("Fax");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Fax");

                break;

            case R.id.imgFieldBirthday:
                profileFieldItems.add("Birthday");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Birthday");
                break;

            case R.id.imgFieldFacebook:
                profileFieldItems.add("Facebook");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Facebook");
                break;

            case R.id.imgFieldTwitter:
                profileFieldItems.add("Twitter");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Twitter");
                break;

            case R.id.imgFieldLinkedIn:
                profileFieldItems.add("LinkedIn");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("LinkedIn");
                break;

            case R.id.imgFieldWebsite:
                profileFieldItems.add("Website");
                if(profileChangedListener != null)
                    profileChangedListener.onAddedNewProfileField("Website");
                break;

            case R.id.imgFieldCustom:
                customCount++;
                if(customCount > 3) return;
                if(imgFieldCustomBadgeNumber.getVisibility() == View.GONE)
                    imgFieldCustomBadgeNumber.setVisibility(View.VISIBLE);
                imgFieldCustomBadgeNumber.setImageResource(getBadgeNumberResId(customCount));

                if(profileChangedListener != null) {
                    if(customCount < 2) {
                        profileFieldItems.add("Custom");
                        profileChangedListener.onAddedNewProfileField("Custom");
                    }
                    else {
                        if(!profileFieldItems.contains("Custom"))
                        {
                            profileFieldItems.add("Custom");
                            profileChangedListener.onAddedNewProfileField("Custom");
                        }
                        else {
                            for (int i = 2; i <= customCount; i++) {
                                if (!profileFieldItems.contains("Custom#" + String.valueOf(i))) {
                                    profileFieldItems.add("Custom#" + String.valueOf(i));
                                    profileChangedListener.onAddedNewProfileField("Custom#" + String.valueOf(i));
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
        public void onEditTextWatcher();
    }

}
