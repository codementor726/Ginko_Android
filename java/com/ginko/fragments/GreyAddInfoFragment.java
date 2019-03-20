package com.ginko.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.ginko.activity.contact.AddGreyOneActivity;
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.context.ConstValues;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.EntityProfileFieldAddOverlayView;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.customview.ProgressHUD;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreyAddInfoFragment extends Fragment implements TextView.OnEditorActionListener {

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

    private HashMap<String , Integer> infoNameMap;

    private ArrayList<InfoItem> infoList;
    private ArrayList<InfoItemView> infoItemViews;

    private Pattern pattern;

    private boolean isUICreated = false;
    private LinearLayout infoListLayout;

    private EntityInfoVO entityInfo;

    private boolean isMultiLcation;


    private boolean isKeyboardVisible = false;

    private ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener onProfileFieldItemsChangeListener = null;

    private EditText edtTextEmailInputType;
    private int emailInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

    private ProgressHUD progressHUD;

    private String latitude = "";
    private String longtitude = "";
    InputFilter smileyFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                //System.out.println("Type : " + type);
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };


    public static GreyAddInfoFragment newInstance(EntityInfoVO groupInfo, boolean isMultiLcation) {
        GreyAddInfoFragment fragment = new GreyAddInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("entityInfo", groupInfo);
        args.putBoolean("isMultiLcation", isMultiLcation);
        fragment.setArguments(args);
        return fragment;
    }

    public GreyAddInfoFragment(){}

    public EntityInfoVO getEntityInfo()
    {
        return this.entityInfo;
    }

    public void setOnProfileFieldItemsChangeListener(ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener listener)
    {
        this.onProfileFieldItemsChangeListener = listener;
    }

    public ArrayList<InfoItem> getInfoList(){
        return  this.infoList;
    }

    public void setKeyboardVisibilty(boolean visibilty)
    {
        this.isKeyboardVisible = visibilty;
        if(!isUICreated) return;
        for(InfoItemView itemView:infoItemViews)
        {
            if(itemView.getVisibility() == View.VISIBLE)
                itemView.refreshView();
        }
    }

    public void updateInfoView(boolean isEditable)
    {
        if(!isUICreated) return;
        for(InfoItemView itemView:infoItemViews)
        {
            if(itemView.getVisibility() == View.VISIBLE) {
                itemView.UpdateEditable(isEditable);
            }
        }
    }

    public void setAllPending(boolean isPending)
    {
        for(int i = 0;i<strInfoNames.length;i++)
        {
            if (isPending == true)
            {
                if (infoList.get(i).isVisible && !infoList.get(i).isPending)
                    infoList.get(i).setPending(true);
                else
                    infoList.get(i).setPending(false);
            }
            else
                infoList.get(i).setPending(false);
        }
    }

    public void changeFieldname(String resetName, String fieldName, String values)
    {
        if (infoList != null) {
            int infoItemIndex = infoNameMap.get(resetName);
            infoList.get(infoItemIndex).setVisibility(true);

            if(infoItemViews!=null) {
                infoItemViews.get(infoItemIndex).setValues(values);
            }

            removeInfoItem(fieldName);
        }
    }

    public void addNewInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(true);
        infoList.get(infoItemIndex).setInfoValue("");
        if(infoItemViews!=null) {
            infoItemViews.get(infoItemIndex).resetValues();
        }
        updateInfoListViews(false);
    }

    public void removeInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(false);
        infoList.get(infoItemIndex).setInfoValue("");
        if(infoItemViews!=null) {
            infoItemViews.get(infoItemIndex).resetValues();
        }
        updateInfoListViews(false);
    }

    public void hiddenInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(false);

        updateInfoListViews(false);
    }

    public void restoreInfoItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(true);

        if(infoItemViews!=null) {
            infoItemViews.get(infoItemIndex).resetValues();
        }

        updateInfoListViews(false);
    }

    public void resetOriginalItem(String fieldName)
    {
        int infoItemIndex = infoNameMap.get(fieldName);
        infoList.get(infoItemIndex).setVisibility(true);

        if(infoItemViews!=null) {
            infoItemViews.get(infoItemIndex).resetOriginal();
        }

        updateInfoListViews(false);
    }

    public void getEditingInfoItemValues()
    {
        for(int i = 0;i<strInfoNames.length;i++)
        {
            if(infoList.get(i).isVisible)
            {
                infoList.get(i).strInfoValue = infoItemViews.get(i).getEdtTextValue();
            }
        }
    }

    public boolean hasMoreThanOneInputedValues()
    {
        getEditingInfoItemValues();

        {
            int nValidItemCount = 0;
            String[] dontShowFields = { "foreground", "background",
                    "privilege", "abbr", "video" };
            for(int i=0; i<infoList.size(); i++)
            {
                GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
                if(!infoItem.isVisible) continue;
                if (ArrayUtils.contains(dontShowFields,
                        infoItem.strFieldType.toLowerCase())) {
                    continue;
                }
                if(!infoItem.strInfoValue.equals(""))
                    nValidItemCount++;
            }

            if(nValidItemCount > 0) return true;
        }
        return false;
    }


    public EntityInfoVO saveMultiEntityInfo(Context context , boolean bShowAlert)
    {
        EntityInfoVO newEntityInfo = new EntityInfoVO();

        getEditingInfoItemValues();

        List<EntityInfoDetailVO> fields = new ArrayList<EntityInfoDetailVO>();;

        newEntityInfo.setEntityInfoDetails(fields);
        String[] dontShowFields = { "name" , "foreground", "background",
                "privilege", "abbr", "video" };

        //check inputs validation
        for(int i=0;i<infoList.size();i++)
        {
            GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
            if(!infoItem.isVisible) continue;
            if(infoItem.strInfoValue.equals(""))
            {
                if(infoItem.strInfoName.equalsIgnoreCase("mobile")) //mobile
                {
                    //infoItem.setInfoValue("112-123-122");
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else if(infoItem.strInfoName.equalsIgnoreCase("phone")) //phone
                {
                    //infoItem.setInfoValue("112-123-122");
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else if(infoItem.strInfoName.equalsIgnoreCase("email")) //email
                {
                    //infoItem.setInfoValue("sample@gmail.com");
                    MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else if(infoItem.strInfoName.equalsIgnoreCase("address")) //address
                {
                    //infoItem.setInfoValue("China-ShenYang");
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else
                {
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
            }
            else
            {
                //check email type
                if(infoItem.strInfoName.contains("Email"))
                {
                    if(!isEmailValid(infoItem.strInfoValue))
                    {
                        if(bShowAlert) {
                            MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_invalid_email_address, null);
                        }
                        return null;
                    }
                }

                EntityInfoDetailVO field = new EntityInfoDetailVO();
                field.setFieldName(infoItem.getFieldName());
                field.setType(infoItem.getFieldType());
                field.setValue(infoItem.getFieldValue());
                //field.setColor("ff000000"); //default color
                //field.setFont("Arial" + ":" + "17" + ":" + "Normal"); //default font and font size

                field.setPosition(0, 0, 0, 0, 1);

                if(infoItem.getFieldName().toLowerCase().contains("address"))
                {
                    if(isValidAddressNew(infoItem.strInfoValue)){
                        entityInfo.setLatitude(latitude);
                        entityInfo.setLongitude(longtitude);
                        entityInfo.setAddressConfirmed(true);
                        infoItem.setAddressSkipped(true);

                        //return EntityVo.  Add by lee
                        newEntityInfo.setAddressConfirmed(true);
                        newEntityInfo.setLatitude(latitude);
                        newEntityInfo.setLongitude(longtitude);
                    }
                    /* Modify by lee.
                    if(infoItem.getLatitude() != null && infoItem.getLongitude() != null) {
                        entityInfo.setLatitude(String.valueOf(infoItem.getLatitude()));
                        entityInfo.setLongitude(String.valueOf(infoItem.getLongitude()));
                        entityInfo.setAddressConfirmed(true);
                        infoItem.setAddressSkipped(true);
                    }*/
                    else
                    {

                        entityInfo.setLatitude(null);
                        entityInfo.setLongitude(null);
                        //Add by lee
                        newEntityInfo.setLatitude(null);
                        newEntityInfo.setLongitude(null);
                        newEntityInfo.setAddressConfirmed(false);
                        /////////////
                        if(infoItem.isAddressSkipped())
                        {
                            entityInfo.setAddressConfirmed(true);
                        }
                        else
                        {
                            final InfoItem addressItem = infoItem;
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirm");
                            //builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                            builder.setMessage("Oh no! Cannot find the location from the "+addressItem.strInfoName+"! Please retype the "+addressItem.strInfoName+" field");
                            builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //TODO
                                    addressItem.setIsAddressConfirmed(false);
                                    addressItem.setAddressSkipped(false);
                                    dialog.dismiss();
                                }
                            });
                            builder.setPositiveButton(R.string.alert_button_skip, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //TODO
                                    addressItem.setIsAddressConfirmed(false);
                                    addressItem.setAddressSkipped(true);
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }
                    }
                }

                fields.add(field);

            }
        }

        return newEntityInfo;
    }

    public EntityInfoVO saveEntityInfo(Context context , boolean bShowAlert)
    {
        EntityInfoVO newEntityInfo = new EntityInfoVO();

        List<EntityInfoDetailVO> fields = new ArrayList<EntityInfoDetailVO>();;

        newEntityInfo.setEntityInfoDetails(fields);
        String[] dontShowFields = { "name" , "foreground", "background",
                "privilege", "abbr", "video" };

        //check inputs validation
        for(int i=0;i<infoList.size();i++)
        {
            GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
            String infoValue = infoItemViews.get(i).getEdtTextValue();

            if(!infoItem.isVisible)
                continue;
            if(infoValue.equals("")) {
                if(infoItem.strInfoName.equalsIgnoreCase("mobile")) //mobile
                {
                    //infoItem.setInfoValue("112-123-122");
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else if(infoItem.strInfoName.equalsIgnoreCase("phone")) //phone
                {
                    //infoItem.setInfoValue("112-123-122");
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else if(infoItem.strInfoName.equalsIgnoreCase("email")) //email
                {
                    //infoItem.setInfoValue("sample@gmail.com");
                    MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else if(infoItem.strInfoName.equalsIgnoreCase("address")) //address
                {
                    //infoItem.setInfoValue("China-ShenYang");
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
                else
                {
                    if(bShowAlert)
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
                    return null;
                }
            }
            else
            {
                //check email type
                if(infoItem.strInfoName.contains("Email"))
                {
                    if(!isEmailValid(infoValue))
                    {
                        if(bShowAlert) {
                            MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_invalid_email_address, null);
                        }
                        return null;
                    }
                }

                EntityInfoDetailVO field = new EntityInfoDetailVO();
                field.setFieldName(infoItem.getFieldName());
                field.setType(infoItem.getFieldType());
                field.setValue(infoItem.getFieldValue());
                //field.setColor("ff000000"); //default color
                //field.setFont("Arial" + ":" + "17" + ":" + "Normal"); //default font and font size

                field.setPosition(0, 0, 0, 0, 1);

                if(infoItem.getFieldName().toLowerCase().contains("address"))
                {
                    if(isValidAddressNew(infoValue)){
                        entityInfo.setLatitude(latitude);
                        entityInfo.setLongitude(longtitude);
                        entityInfo.setAddressConfirmed(true);
                        infoItem.setAddressSkipped(true);

                        //return EntityVo.  Add by lee
                        newEntityInfo.setAddressConfirmed(true);
                        newEntityInfo.setLatitude(latitude);
                        newEntityInfo.setLongitude(longtitude);
                    }
                    /* Modify by lee.
                    if(infoItem.getLatitude() != null && infoItem.getLongitude() != null) {
                        entityInfo.setLatitude(String.valueOf(infoItem.getLatitude()));
                        entityInfo.setLongitude(String.valueOf(infoItem.getLongitude()));
                        entityInfo.setAddressConfirmed(true);
                        infoItem.setAddressSkipped(true);
                    }*/
                    else
                    {

                        entityInfo.setLatitude(null);
                        entityInfo.setLongitude(null);
                        //Add by lee for set location.
                        newEntityInfo.setLatitude(null);
                        newEntityInfo.setLongitude(null);
                        newEntityInfo.setAddressConfirmed(false);
                        /////////////
                        if(infoItem.isAddressSkipped())
                        {
                            entityInfo.setAddressConfirmed(true);
                        }
                        else
                        {
                            final InfoItem addressItem = infoItem;
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirm");
                            //builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                            builder.setMessage("Oh no! Cannot find the location from the "+addressItem.strInfoName+"! Please retype the "+addressItem.strInfoName+" field");
                            builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //TODO
                                    addressItem.setIsAddressConfirmed(false);
                                    addressItem.setAddressSkipped(false);
                                    dialog.dismiss();
                                }
                            });
                            builder.setPositiveButton(R.string.alert_button_skip, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //TODO
                                    addressItem.setIsAddressConfirmed(false);
                                    addressItem.setAddressSkipped(true);
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }
                    }
                }

                fields.add(field);

            }
        }

        getEditingInfoItemValues();
        return newEntityInfo;
    }

    public boolean checkEntityInfo()
    {
        //check inputs validation
        for(int i=0;i<infoList.size();i++)
        {
            GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
            String infoValue = infoItemViews.get(i).getEdtTextValue();

            if(!infoItem.isVisible)
                continue;
            if(infoValue.equals(""))
                return false;
        }

        return true;
    }

    public EntityInfoVO saveEntityInfoForMulti(Context context , boolean bShowAlert)
    {
        EntityInfoVO newEntityInfo = new EntityInfoVO();

        getEditingInfoItemValues();

        List<EntityInfoDetailVO> fields = new ArrayList<EntityInfoDetailVO>();;

        newEntityInfo.setEntityInfoDetails(fields);
        String[] dontShowFields = { "name" , "foreground", "background",
                "privilege", "abbr", "video" };

        //check inputs validation
        for(int i=0;i<infoList.size();i++)
        {
            GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
            if(infoItem.strInfoName.equals("address") || infoItem.getFieldName().toLowerCase().equals("address"))
                infoItem.isVisible = true;
            if(!infoItem.isVisible) continue;
//            if(infoItem.strInfoValue.equals(""))
//            {
//                if(infoItem.strInfoName.equalsIgnoreCase("mobile")) //mobile
//                {
//                    //infoItem.setInfoValue("112-123-122");
//                    if(bShowAlert)
//                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
//                    return null;
//                }
//                else if(infoItem.strInfoName.equalsIgnoreCase("phone")) //phone
//                {
//                    //infoItem.setInfoValue("112-123-122");
//                    if(bShowAlert)
//                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
//                    return null;
//                }
//                else if(infoItem.strInfoName.equalsIgnoreCase("email")) //email
//                {
//                    //infoItem.setInfoValue("sample@gmail.com");
//                    MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
//                    return null;
//                }
//                else if(infoItem.strInfoName.equalsIgnoreCase("address")) //address
//                {
//                    //infoItem.setInfoValue("China-ShenYang");
//                    if(bShowAlert)
//                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
//                    return null;
//                }
//                else
//                {
//                    if(bShowAlert)
//                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_please_input_all_fields , null);
//                    return null;
//                }
//            }
//            else
//            {
            //check email type
            if(!(infoItem.strInfoValue.equals("")) && infoItem.strInfoName.contains("Email"))
            {
                if(!isEmailValid(infoItem.strInfoValue))
                {
                    if(bShowAlert) {
                        MyApp.getInstance().showSimpleAlertDiloag(context, R.string.str_alert_invalid_email_address, null);
                    }
                    return null;
                }
            }

            EntityInfoDetailVO field = new EntityInfoDetailVO();
            field.setFieldName(infoItem.getFieldName());
            field.setType(infoItem.getFieldType());
            field.setValue(infoItem.getFieldValue());
            //field.setColor("ff000000"); //default color
            //field.setFont("Arial" + ":" + "17" + ":" + "Normal"); //default font and font size

            field.setPosition(0, 0, 0, 0, 1);

            if(!(infoItem.strInfoValue.equals("")) && infoItem.getFieldName().toLowerCase().contains("address"))
            {
                if(isValidAddressNew(infoItem.strInfoValue)){
                    entityInfo.setLatitude(latitude);
                    entityInfo.setLongitude(longtitude);
                    entityInfo.setAddressConfirmed(true);
                    infoItem.setAddressSkipped(true);

                    //return EntityVo.  Add by lee
                    newEntityInfo.setAddressConfirmed(true);
                    newEntityInfo.setLatitude(latitude);
                    newEntityInfo.setLongitude(longtitude);
                }
                    /* Modify by lee.
                    if(infoItem.getLatitude() != null && infoItem.getLongitude() != null) {
                        entityInfo.setLatitude(String.valueOf(infoItem.getLatitude()));
                        entityInfo.setLongitude(String.valueOf(infoItem.getLongitude()));
                        entityInfo.setAddressConfirmed(true);
                        infoItem.setAddressSkipped(true);
                    }*/
                else
                {

                    entityInfo.setLatitude(null);
                    entityInfo.setLongitude(null);
                    //Add by lee
                    newEntityInfo.setLatitude(null);
                    newEntityInfo.setLongitude(null);
                    newEntityInfo.setAddressConfirmed(false);
                    /////////////
                    if(infoItem.isAddressSkipped())
                    {
                        entityInfo.setAddressConfirmed(true);
                    }
                    else
                    {
                        final InfoItem addressItem = infoItem;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Confirm");
                        //builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                        builder.setMessage("Oh no! Cannot find the location from the "+addressItem.strInfoName+"! Please retype the "+addressItem.strInfoName+" field");
                        builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                addressItem.setIsAddressConfirmed(false);
                                addressItem.setAddressSkipped(false);
                                dialog.dismiss();
                            }
                        });
//                            builder.setPositiveButton(R.string.alert_button_skip, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    //TODO
//                                    addressItem.setIsAddressConfirmed(false);
//                                    addressItem.setAddressSkipped(true);
//                                    dialog.dismiss();
//                                }
//                            });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return null;
                    }
                }
            }

            fields.add(field);

            //}
        }

        return newEntityInfo;
    }


    public int getCurrentInfoItemCounts()
    {
        if(infoList == null) return 4;
        int nCount = 0;
        for(int i=0;i<strInfoNames.length;i++)
        {
            if(i == 3 || //mobile
                    i == 10) //email

            {
                nCount++;
                continue;
            }

            if(infoList.get(i).isVisible)
                nCount++;
        }

        return nCount;
    }

    public int getTotalInfoItemCounts()
    {
        if(entityInfo == null || infoList == null) return 0;
        return strInfoNames.length;
    }


    public int getCurrentInputableFieldsCount()
    {
        if(entityInfo == null || infoList == null) return 0;
        List<EntityInfoDetailVO> fields = entityInfo.getEntityInfoDetails();
        int count = 0;
        for (int i =0 ;i < infoList.size(); i++) {
            if(infoList.get(i).isVisible)
                count++;
        }
        return  count;
    }

    public int getAvailableEmailFieldCount()
    {
        int count = 0;
        if(infoList == null || infoList.size() < 1) return 0;
        for(int i =0;i<infoList.size();i++) {
            if (infoList.get(i).isVisible && infoList.get(i).strInfoName.toLowerCase().equalsIgnoreCase("email"))
                count++;
        }
        return count;
    }

    public int getAvailableMobileFieldCount()
    {
        int count = 0;
        if(infoList == null || infoList.size() < 1) return 0;
        for(int i =0;i<infoList.size();i++) {
            if (infoList.get(i).isVisible && infoList.get(i).strInfoName.toLowerCase().equalsIgnoreCase("mobile"))
                count++;
        }
        return count;
    }

    public int getAvailableAddressFieldCount()
    {
        int count = 0;
        if(infoList == null || infoList.size() < 1) return 0;
        for(int i =0;i<infoList.size();i++) {
            if (infoList.get(i).isVisible && infoList.get(i).strInfoName.toLowerCase().equalsIgnoreCase("address"))
                count++;
        }
        return count;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            entityInfo = (EntityInfoVO)getArguments().getSerializable("entityInfo");
            isMultiLcation = getArguments().getBoolean("isMultiLcation");

            infoNameMap = new HashMap<String , Integer>();
            infoList = new ArrayList<InfoItem>();
            infoItemViews = new ArrayList<InfoItemView>();
            for(int i=0;i<strInfoNames.length;i++)
            {
                infoNameMap.put(strInfoNames[i] , new Integer(i));
                //show default items
                //if(i == 0)//hide name field
                //{
                //    infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i], "", true, true));
                //    continue;
                //}
                if(entityInfo == null && i == 3 || i == 10 || i == 12)
                    infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] ,"" , false , true));
                else
                    infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] ,"" , false , true));
            }

            //if(strFullName.compareTo("") != 0) {
            //    infoList.get(0).strInfoValue = strFullName;
            ///}
            String[] dontShowFields = { "foreground", "background",
                    "privilege", "abbr", "video" };

            List <EntityInfoDetailVO> fields = entityInfo.getEntityInfoDetails();
            if(fields!=null) {
                int fieldCount = 0;
                for (EntityInfoDetailVO field : fields) {
                    String fieldName = field.getFieldName();
                    if(fieldName.equals(""))
                        continue;
                    if (ArrayUtils.contains(dontShowFields,
                            fieldName.toLowerCase()))
                        continue;
                    fieldCount++;
                    for(int i =0;i<infoList.size();i++)
                    {
                        //infoList.get(i).setVisibility(false);
                        if(infoList.get(i).strInfoName.equalsIgnoreCase(fieldName))
                        {
                            infoList.get(i).setInfoValue(field.getValue());
                            //if(i == 0)
                            //    infoList.get(i).setVisibility(false);//hide name field as default
                            //else
                            infoList.get(i).setVisibility(true);
                        }
                    }
                }

                //if there isn't any field info, then set the default fields as visible
                if(fieldCount == 0)
                {
                    for(int i=0; i<infoList.size(); i++)
                    {
                        if(i == 3 || //mobile
                                i == 10 || //email
                                i == 12
                                )
                            infoList.get(i).setVisibility(true);
                        /*if(i == 9)//email . if there isn't any info ,then make sample email with user's registered name
                        {
                            String userName = Uitils.getUserFullname(getActivity());
                            if(userName.trim().contains(" "))
                                userName = userName.substring(0 , userName.indexOf(" ") -1 );
                            String email = userName+"@"+userName+".com";
                            infoList.get(i).strInfoValue = email;
                        }*/
                    }
                }
            }
            //Show process progress dialog
            progressHUD = ProgressHUD.createProgressDialog(getActivity(), "", false, false, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if(progressHUD != null && progressHUD.isShowing())
                        progressHUD.dismiss();
                }
            });
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_work_add_info , container , false);
        infoListLayout = (LinearLayout)view.findViewById(R.id.infoListLayout);

        edtTextEmailInputType = (EditText)view.findViewById(R.id.edtTextEmailInputType);
        emailInputType = edtTextEmailInputType.getInputType();//this is used for special google keyboard inputtypes


        for(int i=0;i<strInfoNames.length;i++)
        {
            infoItemViews.add(new InfoItemView(getActivity() , infoList.get(i)));
            infoListLayout.addView(infoItemViews.get(i));
        }

        isUICreated = true;

        updateInfoListViews(true);



        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        isUICreated = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        //isUICreated = false;
    }


    public ArrayList<String> getCurrentVisibleInfoItems()
    {
        ArrayList<String> infoItems = new ArrayList<String>();
        if(infoList!=null)
        {
            for(InfoItem item:infoList)
            {
                if(item.isVisible)
                    infoItems.add(item.strInfoName);
            }
        }
        return infoItems;
    }



    public void updateInfoListViews(boolean isReset)
    {
        if(!isUICreated ) return;
        if(infoList == null || infoList.size() < 1) return;

        int emailCount = getAvailableEmailFieldCount();
        int mobileCount = getAvailableMobileFieldCount();
        int addressCount = getAvailableAddressFieldCount();

        for(int i=0;i<strInfoNames.length;i++)
        {
            infoList.get(i).isRemovable = true;

            InfoItem item = infoList.get(i);

            if (isMultiLcation == true){
                if(item.strInfoName.toLowerCase().contains("address")) {
                    item.isRemovable = false;
                }
            }
        }
        if (isMultiLcation == true)
        {
            for(int i=0;i<infoList.size();i++)
            {
                InfoItem item = infoList.get(i);
                if(item.isVisible && item.strInfoName.toLowerCase().contains("address")) {
                    item.isRemovable = false;
                    break;
                }
            }
        }
        /*
        //if there is only one mobile or email field item , then its not removable
        if((emailCount < 1 && mobileCount == 1) || (emailCount == 1 && mobileCount < 1)){
            if(mobileCount == 1)
            {
                for(int i=0;i<infoList.size();i++)
                {
                    InfoItem item = infoList.get(i);
                    if(item.isVisible && item.strInfoName.toLowerCase().contains("mobile")) {
                        item.isRemovable = false;
                        break;
                    }
                }
            }
            else if(emailCount == 1)
            {
                for(int i=0;i<infoList.size();i++)
                {
                    InfoItem item = infoList.get(i);
                    if(item.isVisible && item.strInfoName.toLowerCase().contains("email")) {
                        item.isRemovable = false;
                        break;
                    }
                }
            }
        }
        */
        // Add by lee for hidden delete button where there is only one field.
        if(mobileCount == 1 && emailCount < 1 && addressCount < 1)
        {
            for(int i=0;i<infoList.size();i++)
            {
                InfoItem item = infoList.get(i);
                if(item.isVisible && item.strInfoName.toLowerCase().equalsIgnoreCase("mobile")) {
                    item.isRemovable = false;
                    break;
                }
            }
        }
        else if(emailCount == 1 && mobileCount < 1 && addressCount < 1)
        {
            for(int i=0;i<infoList.size();i++)
            {
                InfoItem item = infoList.get(i);
                if(item.isVisible && item.strInfoName.toLowerCase().equalsIgnoreCase("email")) {
                    item.isRemovable = false;
                    break;
                }
            }
        }
        else if(emailCount < 1 && mobileCount < 1 && addressCount == 1)
        {
            for(int i=0;i<infoList.size();i++)
            {
                InfoItem item = infoList.get(i);
                if(item.isVisible && item.strInfoName.toLowerCase().equalsIgnoreCase("address")) {
                    item.isRemovable = false;
                    break;
                }
            }
        }

        for(int i=0;i<strInfoNames.length;i++)
        {
            if(!infoList.get(i).isVisible) {
                infoItemViews.get(i).setVisibility(View.GONE);
            }
            else {
                infoItemViews.get(i).setVisibility(View.VISIBLE);
            }
            if(isReset)
                infoItemViews.get(i).resetValues();
            infoItemViews.get(i).refreshView();
        }
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

    private boolean isValidAddress(String strAddress)
    {
        progressHUD.show();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addresses = null;

        try {
            // Getting a maximum of 3 Address that matches the input text
            addresses = geocoder.getFromLocationName(strAddress, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressHUD.cancel();

        if(addresses == null || addresses.size() == 0)
            return false;
        else {
            latitude = String.valueOf(addresses.get(0).getLatitude());
            longtitude = String.valueOf(addresses.get(0).getLongitude());
            return true;
        }
    }

    private boolean isValidAddressNew(String strAddress) {
        JSONObject result = null;
        try {
            result = new NetworkClass().execute(strAddress).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (result != null)
        {
            try {
                longtitude = String.valueOf(((JSONArray)result.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng"));

                latitude = String.valueOf(((JSONArray)result.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat"));

            } catch (JSONException e) {
                return false;

            }
            return true;
        } else
            return false;
    }


    public class NetworkClass extends AsyncTask<String, String, JSONObject>
    {
        @Override
        protected JSONObject doInBackground(String... params) {
            String address = params[0];
            StringBuilder stringBuilder = new StringBuilder();
            try {

                address = address.replaceAll(" ","%20");

                HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
                HttpClient client = new DefaultHttpClient();
                HttpResponse response;
                stringBuilder = new StringBuilder();

                response = client.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            progressHUD.cancel();
        }
    }

    public boolean checkSimCard(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if(telephonyManager.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE) {
            MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
            return false;
        } else {
            int SIM_STATE = telephonyManager.getSimState();

            if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
                return true;
            else {
                switch (SIM_STATE) {
                    case TelephonyManager.SIM_STATE_ABSENT: //SimState = "No Sim Found!";
                        MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
                        break;
                    case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                        break;
                    case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                        break;
                    case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                        break;
                    case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                        MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
                        break;
                }
                return false;
            }
        }
    }

    public class InfoItem
    {
        public String strInfoValue;
        public String strFieldType;
        public String strInfoName = "Phone";
        public int nItemInputType;
        public int nFieldId = -1;
        public int nMaxLines = 1;
        public boolean isVisible = true;
        public boolean isRemovable = true;
        public boolean isPending = false;
        public String strOriginValue = "";

        private boolean isDefaultField = false;
        private boolean isAddressConfirmed = false;

        private boolean isAddressSkipped = false;

        private Double lat = null , lng = null;

        public InfoItem(String infoName , String infoTypeFiled , String infoValue , boolean _isVisible , boolean isRemovable)
        {
            this.strInfoValue = infoValue;
            this.strFieldType = infoTypeFiled;
            this.strInfoName = infoName;
            this.nItemInputType = InputType.TYPE_CLASS_TEXT;
            this.nMaxLines = 1;
            this.isVisible = _isVisible;
            this.isRemovable = isRemovable;

            if(this.strInfoName.contains("Address")) {
                this.nMaxLines = 2;
            }

            if(this.strInfoName.toLowerCase().contains("phone") || this.strInfoName.toLowerCase().contains("mobile") || this.strInfoName.toLowerCase().contains("fax")) {
                //this.nItemInputType = InputType.TYPE_CLASS_PHONE;
                this.nItemInputType = InputType.TYPE_CLASS_TEXT;
            }
            else if(this.strInfoName.toLowerCase().contains("email")) {
                this.nItemInputType = emailInputType;
                //this.nItemInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            }

            //else if(this.strInfoName.equals("Birthday")) {
            //    this.nItemInputType = InputType.TYPE_CLASS_DATETIME;
            //}
            else if(this.strInfoName.toLowerCase().contains("website") || this.strInfoName.toLowerCase().contains("facebook")
                    || this.strInfoName.toLowerCase().contains("twitter") || this.strInfoName.toLowerCase().contains("linkedin"))
                this.nItemInputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
            else
                this.nItemInputType = InputType.TYPE_CLASS_TEXT;
        }

        public void setOriginvalue(String infoValue)
        {
            this.strOriginValue = infoValue;
        }
        public void setInfoValue(String infoValue)
        {
            this.strInfoValue = infoValue;
        }
        public void setRemovable(boolean removable)
        {
            this.isRemovable = removable;
        }

        public void setFieldValue(String infoValue)
        {
            this.strInfoValue = infoValue;
        }
        public String getFieldValue(){return  this.strInfoValue;}

        public void setFieldType(String fieldType)
        {
            this.strFieldType = fieldType;
        }
        public String getFieldType(){return  this.strFieldType;}

        public void setFieldName(String name)
        {
            this.strInfoName = name;
        }
        public String getFieldName(){return  this.strInfoName;}

        public void setVisibility(boolean visible)
        {
            this.isVisible = visible;
        }

        public void setPending(boolean pending)
        {
            this.isPending = pending;
            if (pending)
                this.setOriginvalue(this.strInfoValue);
            else
                this.setOriginvalue("");
        }

        public boolean getVisibility(){return  this.isVisible;}

        public int getTextInputType(){return this.nItemInputType;}

        public boolean isDefaultField(){return  this.isDefaultField;}

        public void setIsAddressConfirmed(boolean bconfirmed){this.isAddressConfirmed = bconfirmed;}
        public boolean isAddressConfirmed(){return this.isAddressConfirmed;}

        public void setAddressSkipped(boolean isSkipped){this.isAddressSkipped = isSkipped;}
        public boolean isAddressSkipped(){return this.isAddressSkipped;}

        public void setLatitude(Double d){this.lat = d;}
        public Double getLatitude(){return this.lat;}

        public void setLongitude(Double d){this.lng = d;}
        public Double getLongitude(){return this.lng;}

    }
    public class InfoItemView extends LinearLayout {
        protected Context mContext;
        protected LayoutInflater inflater;

        protected InfoItem item;
        private LinearLayout rootLayout;

        private ImageView imgFieldIcon;
        private ImageButton btnDeleteField , btnBackspace;
        private EditText edtInfoItem;
        private TextView txtInfoItemView;

        private EmailValidationCheckRunnable emailCheckerThread = null;

        private Handler mHandler;

        private GetLatLngFromAddress confirmAddressValidationTask = null;
        private double latit = 0.0d, longi = 0.0d;

        public InfoItemView(Context context) {
            super(context);
            this.mContext = context;
        }
        public InfoItemView(Context context ,InfoItem _item)
        {
            super(context);
            this.mContext = context;
            this.item = _item;

            mHandler = new Handler(this.mContext.getMainLooper());

            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.home_work_add_info_item, this, true);

            rootLayout = (LinearLayout)findViewById(R.id.rootLayout);

            txtInfoItemView = (TextView)findViewById(R.id.txtInfoItemValue);

            edtInfoItem = (EditText)findViewById(R.id.edtInfoItem);
            edtInfoItem.setClickable(true);
            //Add by lee for GAD-984
            edtInfoItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        mCallback.gotToDoneOrNext();
                    }
                    return false;
                }
            });

            edtInfoItem.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(onProfileFieldItemsChangeListener != null)
                        onProfileFieldItemsChangeListener.onEditTextWatcher();
                }
            });

            imgFieldIcon = (ImageView)findViewById(R.id.imgFieldIcon);
            btnBackspace = (ImageButton)findViewById(R.id.btnBackspace); btnBackspace.setVisibility(View.GONE);
            btnDeleteField = (ImageButton) findViewById(R.id.btnDeleteField);
            edtInfoItem.setFilters(new InputFilter[]{smileyFilter});

            edtInfoItem.setCursorVisible(false);//hide cursor as default
            edtInfoItem.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        edtInfoItem.setCursorVisible(true);
                    } else {
                        edtInfoItem.setCursorVisible(false);
                    }
                    if (item.strInfoName.toLowerCase().contains("email") && hasFocus == false) {
                        if (emailCheckerThread == null) {
                            emailCheckerThread = new EmailValidationCheckRunnable(mContext, edtInfoItem.getText().toString());
                        } else {
                            if (mHandler != null)
                                mHandler.removeCallbacks(emailCheckerThread);
                        }
                        if (isUICreated) {
                            emailCheckerThread.setEmailString(edtInfoItem.getText().toString());
                            mHandler.postDelayed(emailCheckerThread, 100);
                        }
                    }
                    else if (item.strInfoName.toLowerCase().contains("address") && hasFocus == false) {
                        String address = edtInfoItem.getText().toString().trim();
                        if(address.equals("")) return;
                        try {
                            if (confirmAddressValidationTask == null) {
                                confirmAddressValidationTask = new GetLatLngFromAddress(getActivity(), address, item, true);
                                if (Build.VERSION.SDK_INT >= 12)
                                    confirmAddressValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                else
                                    confirmAddressValidationTask.execute();
                            } else {
                                if (confirmAddressValidationTask.getStatus() == AsyncTask.Status.PENDING) {
                                    confirmAddressValidationTask.cancel(true);
                                    confirmAddressValidationTask = new GetLatLngFromAddress(getActivity(), address, item, true);
                                    if (Build.VERSION.SDK_INT >= 12)
                                        confirmAddressValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    else
                                        confirmAddressValidationTask.execute();

                                } else if (confirmAddressValidationTask.getStatus() == AsyncTask.Status.RUNNING) {
                                    return;
                                } else if (confirmAddressValidationTask.getStatus() == AsyncTask.Status.FINISHED) {
                                    confirmAddressValidationTask = new GetLatLngFromAddress(getActivity(), address, item, true);
                                    if (Build.VERSION.SDK_INT >= 12)
                                        confirmAddressValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    else
                                        confirmAddressValidationTask.execute();
                                }
                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    refreshView();
                }
            });
            //this enables scrollable in scrollview
            /*edtInfoItem.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (v.getId() == R.id.edtInfoItem) {
                        v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_UP:
                                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
                                break;
                        }
                    }
                    return false;
                }
            });*/
            /*btnBackspace.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtInfoItem.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }
            });*/
            txtInfoItemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL)) //call send email intent
                    {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:"));
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{item.strInfoValue});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Exchange contact info with me via Ginko");//subject
                        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.str_send_email_bottom_suffix));
                        // need this to prompts email client only
                        intent.setType("message/rfc822");
                        getActivity().startActivity(Intent.createChooser(intent, "Choose an Email client"));
                    }
                    else if(item.strInfoName.toLowerCase().contains("mobile") || item.strInfoName.toLowerCase().contains("phone")) //call dial intent
                    {
                        if (!checkSimCard(getContext()))
                            return;
                        final List<String> buttons = new ArrayList<String>();
                        buttons.add(item.strInfoValue);
                        buttons.add("Cancel");

                        final BottomPopupWindow popupWindow = new BottomPopupWindow(mContext, buttons);
                        popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                            @Override
                            public void onClick(View button, int position) {
                                String text = buttons.get(position);
                                if (text == "Cancel") {
                                    popupWindow.dismiss();
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + text));
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                        popupWindow.show(v);
                    }
                    else if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_WEBSITE))//call android browser intent with url
                    {
                        try {
                            String url = item.strInfoValue;
                            if(!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://"+url;
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.strInfoValue));
                            getActivity().startActivity(browserIntent);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_ADDRESS))
                    {
                        String address = edtInfoItem.getText().toString().trim();
                        if(address.equals("")) return;
                        getPositionNew(address);
                    }
                }
            });


            btnDeleteField.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    if(onProfileFieldItemsChangeListener != null)
                        onProfileFieldItemsChangeListener.onRemovedProfileField(item.strInfoName);
                }
            });

            /*if(groupType == GROUP_HOME)
            {
                edtInfoItem.setTextColor(0xffa3a3a3);
                edtInfoItem.setHintTextColor(0xff000000);
                //edtInfoItem.setBackgroundResource(R.drawable.home_info_item_background);
            }
            else
            {
                edtInfoItem.setTextColor(0xff8064a1);
                edtInfoItem.setHintTextColor(0xff8064a1);
                //edtInfoItem.setBackgroundResource(R.drawable.work_info_item_background);
            }*/

            edtInfoItem.setHintTextColor(0xffa3a3a3);
            edtInfoItem.setTextColor(0xff000000);

            edtInfoItem.setHint(item.strInfoName);

            edtInfoItem.setText(item.strInfoValue);

            if(item.strInfoName.toLowerCase().contains("email"))
                edtInfoItem.setInputType(emailInputType);
            else
                edtInfoItem.setInputType(this.item.nItemInputType);

            //get edtInfo's parent layout param
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) edtInfoItem.getLayoutParams();

            if(item.strInfoName.toLowerCase().contains("address") || item.strInfoName.toLowerCase().contains("hours"))
            {
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                //params.height = mContext.getResources().getDimensionPixelSize(R.dimen.contact_profile_address_input_filed_height);
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                edtInfoItem.setLayoutParams(params);
                edtInfoItem.setSingleLine(false);
                //edtInfoItem.setMinLines(2);
                //edtInfoItem.setMaxLines(2);
            }
            else
            {
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                edtInfoItem.setLayoutParams(params);
                edtInfoItem.setSingleLine(true);
                edtInfoItem.setMaxLines(1);
                edtInfoItem.setLines(1);
            }

            if(item.strInfoName.toLowerCase().contains("hours"))
            {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_hours);
            }
            else if (item.strInfoName.toLowerCase().contains("mobile")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_mobile);
                //Add by wang for GAD-878
                edtInfoItem.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            else if (item.strInfoName.toLowerCase().contains("phone")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_phone);
                //Add by wang for GAD-878
                edtInfoItem.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            else if (item.strInfoName.toLowerCase().contains("email")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_email);
            }
            else if (item.strInfoName.toLowerCase().contains("address")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_address);
            }
            else if (item.strInfoName.toLowerCase().contains("fax")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_fax);
            }
            else if (item.strInfoName.toLowerCase().contains("birthday")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_birthday);
            }
            else if (item.strInfoName.toLowerCase().contains("facebook")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_facebook);
            }
            else if (item.strInfoName.toLowerCase().contains("twitter")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_twitter);
            }
            else if (item.strInfoName.toLowerCase().contains("linkedin")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_linkedin);
            }
            else if (item.strInfoName.toLowerCase().contains("website")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_website);
            }
            else if (item.strInfoName.toLowerCase().contains("custom")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_custom);
            }
            else if (item.strInfoName.toLowerCase().contains("company")) {
                imgFieldIcon.setImageResource(R.drawable.field_icon_grey_company);
            }

            /*if(this.item.nMaxLines>1)
            {
                edtInfoItem.setSingleLine(false);
                edtInfoItem.setLines(2);
                edtInfoItem.setMaxLines(2);
                edtInfoItem.setMinLines(2);
                edtInfoItem.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                edtInfoItem.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                edtInfoItem.setMinHeight(mContext.getResources().getDimensionPixelOffset(R.dimen.home_work_add_info_address_edtbox_min_height));

            }
            else
            {
                edtInfoItem.setMaxLines(1);
                edtInfoItem.setSingleLine(true);
            }*/



            refreshView();

            //edtInfoItem.addTextChangedListener(edtTextWatcher);
        }

        public InfoItem getItem(){return this.item;}

        public void setValues(String value)
        {
            edtInfoItem.setText(value);
        }

        public void resetValues()
        {
            //edtInfoItem.removeTextChangedListener(edtTextWatcher);
            edtInfoItem.setText(item.strInfoValue);
            //edtInfoItem.addTextChangedListener(edtTextWatcher);
        }

        public void resetOriginal()
        {
            edtInfoItem.setText(item.strOriginValue);
            item.strInfoValue = item.strOriginValue;
        }

        public String getEdtTextValue()
        {
            return edtInfoItem.getText().toString().trim();
        }

        private boolean getPosition(String strAddress) {
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses = null;

            if (geocoder.isPresent()) {
                progressHUD.show();
                try {
                    // Getting a maximum of 3 Address that matches the input text
                    addresses = geocoder.getFromLocationName(strAddress, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressHUD.cancel();

                if(addresses == null || addresses.size() == 0)
                    return false;
                else {
                    latit = addresses.get(0).getLatitude();
                    longi = addresses.get(0).getLongitude();
                    if (latit != 0.0d && longi != 0.0d)
                    {
                        Intent mapViewerIntent = new Intent(getActivity() , LocationMapViewerActivity.class);
                        mapViewerIntent.putExtra("lat" , latit);
                        mapViewerIntent.putExtra("long" , longi);
                        mapViewerIntent.putExtra("address" , strAddress);
                        startActivity(mapViewerIntent);
                    }
                    return true;
                }
            }

            return false;
        }

        private boolean getPositionNew(String strAddress) {
            JSONObject result = null;
            try {
                result = new NetworkClass().execute(strAddress).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (result != null)
            {
                try {
                    longi = ((JSONArray) result.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");

                    latit = ((JSONArray) result.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");
                    if (latit != 0.0d && longi != 0.0d)
                    {
                        Intent mapViewerIntent = new Intent(getActivity() , LocationMapViewerActivity.class);
                        mapViewerIntent.putExtra("lat" , latit);
                        mapViewerIntent.putExtra("long" , longi);
                        mapViewerIntent.putExtra("address" , strAddress);
                        startActivity(mapViewerIntent);
                    }
                } catch (JSONException e) {
                    return false;

                }
                return true;
            } else
                return false;
        }

        public Handler getmHandler() {return mHandler;}

        public void refreshView()
        {
            if(item.strInfoName.contains("Custom"))
                txtInfoItemView.setText(item.strInfoValue);
            else
                txtInfoItemView.setText(/*item.strInfoName.substring(0,1).toLowerCase()+". "+*/item.strInfoValue);

            if(!edtInfoItem.isFocused())
            {
                //btnBackspace.setVisibility(View.GONE);
                btnDeleteField.setVisibility(View.VISIBLE);
            }
            else {
                if (isKeyboardVisible) {
                    //btnBackspace.setVisibility(View.VISIBLE);
                    btnDeleteField.setVisibility(View.VISIBLE);
                } else {
                    //if (ArrayUtils.contains(defaultHomeInfoFields,item.strInfoName.toLowerCase()) && groupType == GROUP_HOME)
                    //btnBackspace.setVisibility(View.GONE);
                    btnDeleteField.setVisibility(View.VISIBLE);
                }
            }

            if(!item.isRemovable && btnDeleteField.getVisibility() == View.VISIBLE)
                btnDeleteField.setVisibility(View.INVISIBLE);
            if (edtInfoItem.isEnabled() == false)
                btnDeleteField.setVisibility(View.INVISIBLE);
            rootLayout.requestLayout();
        }

        public void UpdateEditable(boolean isFocus)
        {
            if (isFocus)
            {
                btnDeleteField.setVisibility(View.VISIBLE);
                edtInfoItem.setVisibility(View.VISIBLE);
                edtInfoItem.setEnabled(true);
                txtInfoItemView.setVisibility(View.GONE);
            } else {
                btnDeleteField.setVisibility(View.INVISIBLE);
                edtInfoItem.setVisibility(View.GONE);
                edtInfoItem.setEnabled(false);
                txtInfoItemView.setVisibility(View.VISIBLE);
            }

            if(!item.isRemovable && btnDeleteField.getVisibility() == View.VISIBLE)
                btnDeleteField.setVisibility(View.INVISIBLE);

            rootLayout.requestLayout();
        }
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
            if((strEmail.compareTo("") != 0) && !isEmailValid(strEmail) && mContext == getActivity())
            {
                Toast.makeText(mContext, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class GetLatLngFromAddress extends AsyncTask<Void ,Void , Void>
    {
        private Context mContext;
        private String address;
        private InfoItem infoItem;
        private boolean isEditable = false;
        private Geocoder gc;
        private double latit = 0.0d;
        private double longi = 0.0d;


        public GetLatLngFromAddress(Context context , String address, InfoItem item, boolean isEditable)
        {
            this.mContext = context;
            this.address = address;
            this.infoItem = item;
            this.isEditable = isEditable;
            gc = new Geocoder(context);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (latit != 0.0d && longi != 0.0d)
            {
                if (isEditable)
                {
                    infoItem.setIsAddressConfirmed(true);
                    infoItem.setLatitude(latit);
                    infoItem.setLongitude(longi);
                    entityInfo.setLatitude(String.valueOf(latit));
                    entityInfo.setLongitude(String.valueOf(longi));
                    entityInfo.setAddressConfirmed(true);
                }
            }
            else
            {
                if (isEditable)
                {
                    infoItem.setIsAddressConfirmed(false);
                    infoItem.setLatitude(latit);
                    infoItem.setLongitude(longi);
                    entityInfo.setAddressConfirmed(false);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(!isUICreated) return;

            /* modify by wang
            if(latitude == null && longitude == null && !infoItem.isAddressSkipped()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirm");
                builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        infoItem.setIsAddressConfirmed(false);
                        infoItem.setAddressSkipped(false);
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.alert_button_skip, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        infoItem.setIsAddressConfirmed(false);
                        infoItem.setAddressSkipped(true);
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }*/

        }

        @Override
        protected void onCancelled(Void result) {
            // TODO Auto-generated method stub
            super.onCancelled(result);
            infoItem.setIsAddressConfirmed(false);
        }


    }

    //Add by lee for GAD-984
    OnKeyDownListener mCallback;
    public interface OnKeyDownListener{
        public void gotToDoneOrNext();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnKeyDownListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }
}
