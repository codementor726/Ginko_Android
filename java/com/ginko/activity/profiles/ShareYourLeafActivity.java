package com.ginko.activity.profiles;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.exchange.ExchangeItem;
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.activity.menu.MenuActivity;
import com.ginko.activity.sprout.MyGinkoMeActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.api.request.GeoLibrary;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.ContactUserInfoVo;
import com.ginko.vo.DirectoryVO;
import com.ginko.vo.SharedInfoVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserWholeProfileVO;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class ShareYourLeafActivity extends MyBaseActivity implements View.OnClickListener ,
        ProfileFieldItemView.onProfileFieldItemClickListener
{

    private ArrayList<UserProfileVO> nameFields = null ,
            companyFields = null ,
            mobileFields = null,
            phoneFields = null,
            emailFields = null ,
            addressFields = null ,
            faxFields = null,
            birthdayFields = null ,
            socialNetworkFields = null ,
            websiteFields = null,
            customFields = null;
    /* UI objects */
    private ImageButton btnPrev , btnConfirm;
    private ImageView imgShareAll , imgChatOnly, imgLeaf;
    private ImageView btnDelete;
    private LinearLayout homeFieldHeaderLayout , workFieldHeaderLayout;
    private LinearLayout homeHeaderDivider , workHeaderDivider;
    private LinearLayout homeFieldsLayout , workFieldsLayout;
    private RelativeLayout locationInfoLayout;
    private TextView txtAddress, txtTitle;
    private ImageView imgMapIcon;
    private TextView txtFullName;
    private RelativeLayout headerlayout;

    /* Variables */
    public String contactId;

    private boolean isChanged = false;
    private boolean isShareAllSelected = false , isChatOnlySelected = false;
    private String strResult = "";

    private UserWholeProfileVO myProfileInfo;
    private int sharingStatus , originalSharingStatus;
    private List<UserProfileVO> workFields , homeFields;
    private List<Boolean> orgWorkFieldsSelections , orgHomeFieldsSelections;

    private HashMap<Integer , UserProfileVO> sharedWorkFieldMap , sharedHomeFieldMap;

    private List<ProfileFieldItemView> homeFieldViews , workFieldViews;



    private final String TAG = "ContactProfileSharing";

    private String strEmail = null;
    private String strFullName = "";

    private boolean isUnexchangedContact = false;
    private String strAddress = "";
    private double lattitude = 0.0d , longitude = 0.0d;

    private boolean isHomeHeaderSelected = false , isWorkHeaderSelected = false;
    private boolean isPendingRequest = false;
    private boolean isInviteContact  = false;
    private boolean isDirectory = false;
    private boolean isJoin = false;
    private int directoryId = 0;
    private String directoryName;
    public static String menuName = "";
    public static String domainName = "";
    public static int menuCode = 0;

    private GetAddressFromLatlngTask getAddressFromLatlngTask = null;

    private String originalSelectedHomeFids = "" , originalSelectedWorkFids = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_sharing_setting);

        Intent intent = this.getIntent();
        contactId = intent.getStringExtra("contactID");
        strFullName = intent.getStringExtra("contactFullname");
        isUnexchangedContact = intent.getBooleanExtra("isUnexchangedContact", false);
        isInviteContact = intent.getBooleanExtra("isInviteContact", false);
        strResult = intent.getStringExtra("StartActivity");
        isDirectory = intent.getBooleanExtra("isDirectory", false);
        isJoin = intent.getBooleanExtra("isJoinDirectory", false);

        if(intent.hasExtra("isPendingRequest"))
            isPendingRequest = intent.getBooleanExtra("isPendingRequest" , false);

        if(intent.hasExtra("shared_home_fids"))
            originalSelectedHomeFids = intent.getStringExtra("shared_home_fids");

        if(intent.hasExtra("shared_work_fids"))
            originalSelectedWorkFids = intent.getStringExtra("shared_work_fids");

        if(intent.hasExtra("sharing_status"))
            originalSharingStatus = intent.getIntExtra("sharing_status" , 0);

        if(intent.hasExtra("email"))
            strEmail = intent.getStringExtra("email");

        if(isUnexchangedContact) {
            strAddress = intent.getStringExtra("address");
        }

        lattitude = intent.getDoubleExtra("lat" , 0.0d);
        longitude = intent.getDoubleExtra("long" , 0.0d);

        directoryName = intent.getStringExtra("contactFullname");

        this.isShareAllSelected = false;
        this.isChatOnlySelected = false;
        this.isChanged = false;
        workFields = new ArrayList<UserProfileVO>();
        homeFields = new ArrayList<UserProfileVO>();
        homeFieldViews = new ArrayList<ProfileFieldItemView>();
        workFieldViews = new ArrayList<ProfileFieldItemView>();

        sharedWorkFieldMap = new HashMap<Integer , UserProfileVO>();
        sharedHomeFieldMap = new HashMap<Integer , UserProfileVO>();

        getUIObjects();

        Integer contactID = null;
        if(contactId.compareTo("0")!=0 )
            contactID = Integer.valueOf(contactId);

        if (!isDirectory)
        {
            UserInfoRequest.getInfo(contactID, new ResponseCallBack<UserWholeProfileVO>() {
                @Override
                public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                    if (response.isSuccess()) {
                        initDatas(response.getData());
                    }
                }
            }, true);
        } else
        {
            directoryId = intent.getIntExtra("directoryID" , 0);
            if (isUnexchangedContact && !isPendingRequest)
            {
                UserInfoRequest.getInfo(new ResponseCallBack<UserWholeProfileVO>() {
                    @Override
                    public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                        if (response.isSuccess()) {
                            initDatas(response.getData());
                        }
                    }
                });
            } else
            {
                DirectoryRequest.getPermissionShared(directoryId, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject jsonData = response.getData();
                            try {
                                originalSelectedHomeFids = jsonData.getString("shared_home_fids");
                                originalSelectedWorkFids = jsonData.getString("shared_work_fids");
                                originalSharingStatus = jsonData.getInt("sharing");
                                UserInfoRequest.getInfo(new ResponseCallBack<UserWholeProfileVO>() {
                                    @Override
                                    public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                                        if (response.isSuccess()) {
                                            initDatas(response.getData());
                                        }
                                    }
                                });
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }, true);
            }
        }

    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.imgBtnDelete); btnDelete.setOnClickListener(this);
        imgLeaf = (ImageView)findViewById(R.id.imgLeaf);
        imgShareAll = (ImageView)findViewById(R.id.imgShareAll); imgShareAll.setOnClickListener(this);
        imgChatOnly = (ImageView)findViewById(R.id.imgChatOnly); imgChatOnly.setOnClickListener(this);

        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtFullName = (TextView)findViewById(R.id.txtFullName); txtFullName.setText("");

        if(isInviteContact){
            headerlayout.setBackgroundResource(R.color.green_top_titlebar_color);
            /*
            btnPrev.setImageDrawable(getResources().getDrawable(R.drawable.part_a_btn_back_nav));
            imgLeaf.setImageDrawable(getResources().getDrawable(R.drawable.leaf_line));
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
            txtFullName.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
            btnConfirm.setImageDrawable(getResources().getDrawable(R.drawable.part_a_btn_check_nav));
            */
            headerlayout.setBackgroundResource(R.color.green_top_titlebar_color);
            btnPrev.setImageDrawable(getResources().getDrawable(R.drawable.btn_back_nav_black));
            imgLeaf.setImageDrawable(getResources().getDrawable(R.drawable.leaf_line_black));
            txtTitle.setTextColor(getResources().getColor(R.color.black));
            txtFullName.setTextColor(getResources().getColor(R.color.black));
            btnConfirm.setImageDrawable(getResources().getDrawable(R.drawable.part_a_btn_check_nav_bl));
        }
        else {
            imgLeaf.setImageDrawable(getResources().getDrawable(R.drawable.leaf_line_white));
        }

        homeHeaderDivider = (LinearLayout)findViewById(R.id.homeHeaderDivider);homeHeaderDivider.setVisibility(View.GONE);
        workHeaderDivider = (LinearLayout)findViewById(R.id.workHeaderDivider);workHeaderDivider.setVisibility(View.GONE);

        homeFieldHeaderLayout = (LinearLayout)findViewById(R.id.homeFieldHeaderLayout); homeFieldHeaderLayout.setVisibility(View.GONE);
        homeFieldHeaderLayout.setOnClickListener(this);
        workFieldHeaderLayout = (LinearLayout)findViewById(R.id.workFieldHeaderLayout); workFieldHeaderLayout.setVisibility(View.GONE);
        workFieldHeaderLayout.setOnClickListener(this);

        homeFieldsLayout = (LinearLayout)findViewById(R.id.homeFieldsLayout);
        workFieldsLayout = (LinearLayout)findViewById(R.id.workFieldsLayout);

        locationInfoLayout = (RelativeLayout)findViewById(R.id.locationInfoLayout);
        imgMapIcon = (ImageView)findViewById(R.id.imgMapIcon); imgMapIcon.setOnClickListener(this);
        txtAddress = (TextView)findViewById(R.id.txtAddress);

        RelativeLayout bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
        if(isUnexchangedContact)
        {
            bottomLayout.setVisibility(View.GONE);//hide the bottom delete button layout
            if(strAddress != null && !isDirectory) {
                locationInfoLayout.setVisibility(View.VISIBLE);
                strAddress = strAddress.replace(",", ", ");
                txtAddress.setText(strAddress);
                if (strAddress.equals("") && lattitude != 0.0d && longitude != 0.0d) {
                    getAddressFromLatlngTask = new GetAddressFromLatlngTask(lattitude, longitude);
                    try {
                        if (Build.VERSION.SDK_INT >= 12)
                            getAddressFromLatlngTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            getAddressFromLatlngTask.execute();
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
                locationInfoLayout.setVisibility(View.GONE);
        }
        else
        {
            //locationInfoLayout.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
            if (lattitude != 0.0d && longitude != 0.0d) {
                getAddressFromLatlngTask = new GetAddressFromLatlngTask(lattitude, longitude);
                try {
                    if (Build.VERSION.SDK_INT >= 12)
                        getAddressFromLatlngTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        getAddressFromLatlngTask.execute();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }

                locationInfoLayout.setVisibility(View.VISIBLE);
            } else
                locationInfoLayout.setVisibility(View.GONE);
        }

        if(contactId.equals("0") && !isDirectory)
            bottomLayout.setVisibility(View.GONE);

        updateButtonIfChanged();
    }

    private void initDatas(UserWholeProfileVO _data)
    {
        myProfileInfo = _data;
        if (myProfileInfo == null) {
            finish();
            return;
        }
        if (strFullName.compareTo("") == 0 && myProfileInfo.getContactUserInfo() != null) {
            ContactUserInfoVo contactUserInfoVo = myProfileInfo.getContactUserInfo();
            strFullName = contactUserInfoVo.getFirstName() + " " + contactUserInfoVo.getMiddleName();
            strFullName = strFullName.trim();
            strFullName = strFullName + " " + contactUserInfoVo.getLastName();
            strFullName = strFullName.trim();
        }
        SharedInfoVO shareObject = (SharedInfoVO) myProfileInfo.getShare();
        String strSharedWorkFids = "", strSharedHomeFids = "";
        if (shareObject == null) {
            if (workFields == null)
                workFields = new ArrayList<UserProfileVO>();
            if (homeFields == null)
                homeFields = new ArrayList<UserProfileVO>();

            if (originalSharingStatus > 0) {
                strSharedHomeFids = originalSelectedHomeFids;
                strSharedWorkFids = originalSelectedWorkFids;
                sharingStatus = originalSharingStatus;
            } else {
                sharingStatus = ConstValues.SHARE_NONE;
                originalSharingStatus = sharingStatus;
            }
            try {
                if (strSharedHomeFids.compareTo("") != 0) {
                    strSharedHomeFids = strSharedHomeFids.replaceAll(",", " ");
                    strSharedHomeFids += " ";
                }
            } catch (Exception e) {
                e.printStackTrace();
                strSharedHomeFids = "";
            }
            try {
                if (strSharedWorkFids.compareTo("") != 0) {
                    strSharedWorkFids = strSharedWorkFids.replaceAll(",", " ");
                    strSharedWorkFids += " ";
                }
            } catch (Exception e) {
                e.printStackTrace();
                strSharedWorkFids = "";
            }
        } else {
            try {
                sharingStatus = shareObject.getSharingStatus();
                originalSharingStatus = sharingStatus;
            } catch (Exception e) {
                e.printStackTrace();
                sharingStatus = ConstValues.SHARE_CHAT_ONLY;
                originalSharingStatus = sharingStatus;
            }
            try {
                strSharedHomeFids = shareObject.getSharedHomeFIds();
                if (strSharedHomeFids.compareTo("") != 0) {
                    strSharedHomeFids = strSharedHomeFids.replaceAll(",", " ");
                    strSharedHomeFids += " ";
                }
            } catch (Exception e) {
                e.printStackTrace();
                strSharedHomeFids = "";
            }
            try {
                strSharedWorkFids = shareObject.getSharedWorkFIds();
                if (strSharedWorkFids.compareTo("") != 0) {
                    strSharedWorkFids = strSharedWorkFids.replaceAll(",", " ");
                    strSharedWorkFids += " ";
                }
            } catch (Exception e) {
                e.printStackTrace();
                strSharedWorkFids = "";
            }
        }
        if (homeFields == null)
            homeFields = new ArrayList<UserProfileVO>();
        if (orgHomeFieldsSelections == null)
            orgHomeFieldsSelections = new ArrayList<Boolean>();

        if (myProfileInfo.getHome().getFields() != null) {
            ListIterator it = myProfileInfo.getHome().getFields().listIterator();
            while (it.hasNext()) {
                String[] dontShowFields = {"foreground", "background",
                        "privilege", "abbr", "video", "name", "company", "title"};
                UserProfileVO item = (UserProfileVO) it.next();
                if (item.getValue().equals("") ||
                        ArrayUtils.contains(dontShowFields, item.getFieldType().toLowerCase())) {
                    continue;
                }

                boolean isSelected = false;
                if (strSharedHomeFids.contains(String.valueOf(item.getId()) + " ")) {
                    item.setIsShared(true);
                    isSelected = true;
                } else
                    item.setIsShared(false);
                homeFields.add(item);
                orgHomeFieldsSelections.add(Boolean.valueOf(isSelected));

            }
        }

        if (workFields == null)
            workFields = new ArrayList<UserProfileVO>();
        if (orgWorkFieldsSelections == null)
            orgWorkFieldsSelections = new ArrayList<Boolean>();

        if (myProfileInfo.getWork().getFields() != null) {
            ListIterator it = myProfileInfo.getWork().getFields().listIterator();
            while (it.hasNext()) {
                String[] dontShowFields = {"foreground", "background",
                        "privilege", "abbr", "video", "name", "company", "title"};
                UserProfileVO item = (UserProfileVO) it.next();
                if (item.getValue().equals("") ||
                        ArrayUtils.contains(dontShowFields, item.getFieldType().toLowerCase())) {
                    continue;
                }
                boolean isSelected = false;
                if (strSharedWorkFids.contains(String.valueOf(item.getId()) + " ")) {
                    isSelected = true;
                    item.setIsShared(true);
                } else
                    item.setIsShared(false);
                workFields.add(item);
                orgWorkFieldsSelections.add(isSelected);
            }
        }

        switch (sharingStatus) {
            case ConstValues.SHARE_NONE:
                isShareAllSelected = false;
                isChatOnlySelected = false;

                break;
            case ConstValues.SHARE_HOME:
                if (strSharedHomeFids.equals(""))//if home fields are shared but shared fid is empty , then select all home fields as shared
                {
                    for (int i = 0; i < homeFields.size(); i++) {
                        homeFields.get(i).setIsShared(true);
                    }
                }

                isShareAllSelected = false;
                isChatOnlySelected = false;
                break;
            case ConstValues.SHARE_WORK:
                if (strSharedWorkFids.equals(""))//if work fields are shared but shared fid is empty , then select all work fields as shared
                {
                    for (int i = 0; i < workFields.size(); i++) {
                        workFields.get(i).setIsShared(true);
                    }
                }
                isShareAllSelected = false;
                isChatOnlySelected = false;
                break;
            case ConstValues.SHARE_BOTH:
                if (strSharedHomeFids.equals(""))//if home fields are shared but shared fid is empty , then select all home fields as shared
                {
                    for (int i = 0; i < homeFields.size(); i++) {
                        homeFields.get(i).setIsShared(true);
                    }
                }
                if (strSharedWorkFids.equals(""))//if work fields are shared but shared fid is empty , then select all work fields as shared
                {
                    for (int i = 0; i < workFields.size(); i++) {
                        workFields.get(i).setIsShared(true);
                    }
                }
                if (strSharedHomeFids.equals("") && strSharedWorkFids.equals(""))
                    isShareAllSelected = true;
                else {
                    isShareAllSelected = false;
                }
                isChatOnlySelected = false;
                break;
            case ConstValues.SHARE_CHAT_ONLY:
                isChatOnlySelected = true;
                isShareAllSelected = false;
                break;
        }
        initScreen();
    }

    private void setOriginalStatus()
    {
        String strSharedWorkFids = "", strSharedHomeFids = "";

        if (originalSharingStatus > 0) {
            strSharedHomeFids = originalSelectedHomeFids;
            strSharedWorkFids = originalSelectedWorkFids;
            sharingStatus = originalSharingStatus;
        }
        else {
            sharingStatus = ConstValues.SHARE_NONE;
            originalSharingStatus = sharingStatus;
        }

        try {
            if (strSharedHomeFids.compareTo("") != 0) {
                strSharedHomeFids = strSharedHomeFids.replaceAll(",", " ");
                strSharedHomeFids += " ";
            }
        } catch (Exception e) {
            e.printStackTrace();
            strSharedHomeFids = "";
        }

        try {
            if (strSharedWorkFids.compareTo("") != 0) {
                strSharedWorkFids = strSharedWorkFids.replaceAll(",", " ");
                strSharedWorkFids += " ";
            }
        } catch (Exception e) {
            e.printStackTrace();
            strSharedWorkFids = "";
        }

        for(int i = 0; i<homeFields.size();i++)
        {
            boolean isShared = homeFields.get(i).isShared();
            homeFieldViews.get(i).setSelected(isShared);
        }

        for(int i = 0; i<workFields.size();i++)
        {
            boolean isShared = workFields.get(i).isShared();
            workFieldViews.get(i).setSelected(isShared);
        }

        switch (sharingStatus) {
            case ConstValues.SHARE_NONE:
                isShareAllSelected = false;
                isChatOnlySelected = false;

                selectHomeHeader(false);
                selectWorkHeader(false);
                break;
            case ConstValues.SHARE_HOME:
                if (workFields.size() == 0)
                    isShareAllSelected = true;
                else
                    isShareAllSelected = false;
                isChatOnlySelected = false;

                selectHomeHeader(true);
                selectWorkHeader(false);
                break;
            case ConstValues.SHARE_WORK:
                if (homeFields.size() == 0)
                    isShareAllSelected = true;
                else
                    isShareAllSelected = false;
                isChatOnlySelected = false;
                break;
            case ConstValues.SHARE_BOTH:
                if (!strSharedHomeFids.equals("") && !strSharedWorkFids.equals(""))
                    isShareAllSelected = true;
                else
                    isShareAllSelected = false;

                isChatOnlySelected = false;
                break;
            case ConstValues.SHARE_CHAT_ONLY:
                isChatOnlySelected = true;
                isShareAllSelected = false;
                break;
        }

        updateCheckBox();
        refreshFieldViews(2);
    }

    private void initScreen()
    {
        txtFullName.setText(strFullName);

        //add home fields
        if(homeFields.size()>0) {
            homeFields = addEachFieldInfo(homeFields);
            homeFieldHeaderLayout.setVisibility(View.VISIBLE);
        }
        else
            homeFieldHeaderLayout.setVisibility(View.GONE);

        for(int i = 0; i<homeFields.size();i++)
        {
            ProfileFieldItemView itemView = new ProfileFieldItemView(this , true , homeFields.get(i) , this);
            homeFieldViews.add(itemView);
            homeFieldsLayout.addView(itemView);
        }

        if(workFields.size()>0) {
            workFields = addEachFieldInfo(workFields);
            workFieldHeaderLayout.setVisibility(View.VISIBLE);
        }
        else
            workFieldHeaderLayout.setVisibility(View.GONE);

        for(int i = 0; i<workFields.size();i++)
        {
            ProfileFieldItemView itemView = new ProfileFieldItemView(this , false , workFields.get(i) , this);
            workFieldViews.add(itemView);
            workFieldsLayout.addView(itemView);
        }

        boolean isAllHomeFiledSelected = isAllFieldSelected(homeFieldViews);
        boolean isAllWorkFiledSelected = isAllFieldSelected(workFieldViews);
        if((isAllHomeFiledSelected && isAllWorkFiledSelected) ||
            (isAllHomeFiledSelected && homeFields.size()>0)  && (workFields.size() == 0) ||
                (isAllWorkFiledSelected && workFields.size()>0)  && (homeFields.size() == 0))
            isShareAllSelected = true;

        updateCheckBox();
        refreshFieldViews(2);
    }

    private void selectHomeHeader(boolean isSelected)
    {
        isHomeHeaderSelected = isSelected;
        if(isSelected)
            homeFieldHeaderLayout.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_selected_color));
        else
            homeFieldHeaderLayout.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_unselected_color));
    }
    private void selectWorkHeader(boolean isSelected)
    {
        isWorkHeaderSelected = isSelected;
        if(isSelected)
            workFieldHeaderLayout.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_selected_color));
        else
            workFieldHeaderLayout.setBackgroundColor(getResources().getColor(R.color.profile_share_field_item_unselected_color));
    }
    private boolean isAllFieldSelected(List<ProfileFieldItemView> fieldsItemViews)
    {
        if(fieldsItemViews.size() == 0) return false;
        int nCount = 0;
        for(int i=0;i<fieldsItemViews.size();i++)
        {
            if(fieldsItemViews.get(i).isSelected())
                nCount ++;
        }
        return fieldsItemViews.size()==nCount?true:false;
    }

    private boolean isShareAllSelected()
    {
        boolean isHomeSelected = isAllFieldSelected(homeFieldViews);
        boolean isWorkSelected = isAllFieldSelected(workFieldViews);

        if(!isHomeSelected && homeFieldViews.size()>0){
            return  false;}
        if(!isWorkSelected && workFieldViews.size()>0){
            return  false;}

        return true;
    }

    private boolean isAllFieldUnselected(List<ProfileFieldItemView> fieldsItemViews)
    {
        if(fieldsItemViews.size() == 0) return true;
        boolean isAllUnselected = true;
        for(int i=0;i<fieldsItemViews.size();i++)
        {
            if(fieldsItemViews.get(i).isSelected()) {
                isAllUnselected = false;
                break;
            }
        }
        return isAllUnselected;
    }


    private void checkIsChanged()
    {
        if((!isShareAllSelected && !isChatOnlySelected) && isAllFieldUnselected(homeFieldViews) && isAllFieldUnselected(workFieldViews))
        {
            isChanged = false;
            return;
        }
        if(homeFieldViews.size() > 0) {
            for (int i = 0; i < homeFieldViews.size(); i++) {
                if (homeFieldViews.get(i).isSelected() != orgHomeFieldsSelections.get(i).booleanValue()) {
                    isChanged = true;
                    return;
                }
            }
        }
        if(workFieldViews.size() > 0) {
            for (int i = 0; i < workFieldViews.size(); i++) {
                if (workFieldViews.get(i).isSelected() != orgWorkFieldsSelections.get(i).booleanValue()) {
                    isChanged = true;
                    return;
                }
            }
        }

        if(isChatOnlySelected && originalSharingStatus!=ConstValues.SHARE_CHAT_ONLY) {
            isChanged = true;
            return;
        }

        if (isShareAllSelected && originalSharingStatus != ConstValues.SHARE_BOTH) {
            isChanged = true;
            return;
        }

        isChanged = false;
    }


    private void updateButtonIfChanged()
    {
        if(isChanged) {

            if(isAllFieldUnselected(homeFieldViews) &&
               isAllFieldUnselected(workFieldViews) &&
               !isShareAllSelected && !isChatOnlySelected)//if nothing is selected , then hide apply(confirm) button
            {
                isChanged = false;
                btnConfirm.setVisibility(View.INVISIBLE);
            }
            else
            {
                btnConfirm.setVisibility(View.VISIBLE);
            }
        }
        else
            btnConfirm.setVisibility(View.INVISIBLE);
    }


    private void updateCheckBox()
    {
        if(isShareAllSelected)
            imgShareAll.setImageResource(R.drawable.share_profile_selected);
        else
            imgShareAll.setImageResource(R.drawable.share_profile_non_selected);

        if(isChatOnlySelected)
            imgChatOnly.setImageResource(R.drawable.share_profile_selected);
        else
            imgChatOnly.setImageResource(R.drawable.share_profile_non_selected);
    }

    private void checkCancelPendingRequest()
    {
        if(isAllFieldUnselected(homeFieldViews) && isAllFieldUnselected(workFieldViews)
                && !isChatOnlySelected && !isShareAllSelected && isPendingRequest)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShareYourLeafActivity.this);
            builder.setTitle("Confirm");
            builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_pending_contact));
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO
                    dialog.dismiss();
                    if (!isDirectory) {
                        if (contactId.equals("0")) {
                            StringBuffer emails = new StringBuffer();
                            emails.append(strEmail).append(",");
                            if (emails.length() > 0) {
                                emails.deleteCharAt(emails.length() - 1);
                                CBRequest.cancelRequestByEmail(emails.toString(), new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()) {
                                            if (strResult != null && strResult.equalsIgnoreCase("GinkoMe")) {
                                                Intent returnIntent = new Intent();
                                                returnIntent.putExtra("isContactDeleted", true);
                                                ShareYourLeafActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                                                finish();
                                            } else {
                                                Intent returnIntent = new Intent(ShareYourLeafActivity.this, ContactMainActivity.class);
                                                startActivity(returnIntent);
                                                finish();
                                            }
                                        } else {
                                            MyApp.getInstance().showSimpleAlertDiloag(ShareYourLeafActivity.this, response.getErrorMessage(), null);
                                        }
                                    }
                                });
                            }
                        } else {
                            // Remove Contact from List
                        /*
                        CBRequest.removeFriend(contactId, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("isContactDeleted" , true);
                                    returnIntent.putExtra("contactID" , contactId);
                                    returnIntent.putExtra("nSharingStatus" , 11);
                                    ShareYourLeafActivity.this.setResult(RESULT_OK , returnIntent);
                                    finish();
                                } else {
                                    MyApp.getInstance().showSimpleAlertDiloag(ShareYourLeafActivity.this , R.string.str_err_fail_to_delete_grey_contact , null);
                                }
                            }
                        },true);
                        */

                            StringBuffer contactUids = new StringBuffer();
                            contactUids.append(contactId).append(",");
                            if (contactUids.length() > 0) {
                                CBRequest.cancelRequest(contactUids.toString(), new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()) {
                                            if (strResult != null && strResult.equalsIgnoreCase("GinkoMe")) {
                                                Intent returnIntent = new Intent();
                                                returnIntent.putExtra("isContactDeleted", true);
                                                returnIntent.putExtra("nSharingStatus", 11);
                                                ShareYourLeafActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                                                finish();
                                            } else {
                                                Intent returnIntent = new Intent(ShareYourLeafActivity.this, ContactMainActivity.class);
                                                startActivity(returnIntent);
                                                finish();
                                            }
                                        } else {
                                            MyApp.getInstance().showSimpleAlertDiloag(ShareYourLeafActivity.this, response.getErrorMessage(), null);
                                        }
                                    }
                                });
                            }

                        }
                    } else {
                        DirectoryRequest.quitMember(directoryId, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    Intent returnIntent = new Intent(ShareYourLeafActivity.this, ContactMainActivity.class);
                                    startActivity(returnIntent);
                                    finish();
                                } else {
                                    MyApp.getInstance().showSimpleAlertDiloag(ShareYourLeafActivity.this, R.string.str_err_fail_to_quit_directory, null);
                                }
                            }
                        }, true);
                    }

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO
                    setOriginalStatus();
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private int getCurrentSharingStatus()
    {
        String strShareHomeFids = "" , strShareWorkFids = "";
        for(int i=0;i<homeFieldViews.size();i++)
        {
            if(homeFieldViews.get(i).isSelected())
            {
                strShareHomeFids += String.valueOf(homeFieldViews.get(i).getFieldItem().getId()) +",";
            }
        }
        for(int i=0;i<workFieldViews.size();i++)
        {
            if(workFieldViews.get(i).isSelected())
            {
                strShareWorkFids += String.valueOf(workFieldViews.get(i).getFieldItem().getId()) +",";
            }
        }
        if(!strShareHomeFids.equals(""))
            strShareHomeFids = strShareHomeFids.substring(0 , strShareHomeFids.length()-1);//remove the last deliminator ','
        if(!strShareWorkFids.equals(""))
            strShareWorkFids = strShareWorkFids.substring(0 , strShareWorkFids.length()-1);//remove the last deliminator ','

        int nSharingStatus = ConstValues.SHARE_CHAT_ONLY;
        if(strShareHomeFids.equals("") && strShareWorkFids.equals(""))
            nSharingStatus = ConstValues.SHARE_CHAT_ONLY;
        else if(strShareHomeFids.equals("") && !strShareWorkFids.equals(""))
            nSharingStatus = ConstValues.SHARE_WORK;
        else if(!strShareHomeFids.equals("") && strShareWorkFids.equals(""))
            nSharingStatus = ConstValues.SHARE_HOME;
        else
            nSharingStatus = ConstValues.SHARE_BOTH;

        return nSharingStatus;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            //confirm changes of sharing setting
            case R.id.btnConfirm:

                String strShareHomeFids = "" , strShareWorkFids = "";
                int workFieldsCount = 0;
                for(int i=0;i<homeFieldViews.size();i++)
                {
                    if(homeFieldViews.get(i).isSelected())
                    {
                        strShareHomeFids += String.valueOf(homeFieldViews.get(i).getFieldItem().getId()) +",";
                    }
                }

                for(int i=0;i<workFieldViews.size();i++)
                {
                    if(workFieldViews.get(i).isSelected())
                    {
                        strShareWorkFids += String.valueOf(workFieldViews.get(i).getFieldItem().getId()) +",";
                        workFieldsCount ++;
                    }
                }
                //if work has more than one shared field , then share default field too
                if(workFieldsCount > 0)
                {
                    List<UserProfileVO> workFields = myProfileInfo.getWork().getFields();
                    String[] defaultWorkShareFields = { "name" , "company" , "title"};
                    for(int i=0;i<workFields.size();i++)
                    {
                        if (ArrayUtils.contains(defaultWorkShareFields, workFields.get(i).getFieldType().toLowerCase())) {
                            strShareWorkFids += String.valueOf(workFields.get(i).getId()) +",";
                        }
                    }
                }
                if(!strShareHomeFids.equals(""))
                    strShareHomeFids = strShareHomeFids.substring(0 , strShareHomeFids.length()-1);//remove the last deliminator ','
                if(!strShareWorkFids.equals(""))
                    strShareWorkFids = strShareWorkFids.substring(0 , strShareWorkFids.length()-1);//remove the last deliminator ','

                int nSharingStatus = ConstValues.SHARE_CHAT_ONLY;
                if(strShareHomeFids.equals("") && strShareWorkFids.equals(""))
                    nSharingStatus = ConstValues.SHARE_CHAT_ONLY;
                else if(strShareHomeFids.equals("") && !strShareWorkFids.equals(""))
                    nSharingStatus = ConstValues.SHARE_WORK;
                else if(!strShareHomeFids.equals("") && strShareWorkFids.equals(""))
                    nSharingStatus = ConstValues.SHARE_HOME;
                else
                    nSharingStatus = ConstValues.SHARE_BOTH;

                final int sharingSTATUS = nSharingStatus;
                if(isUnexchangedContact)
                {
                    if (!isDirectory)
                    {
                        if(contactId.compareTo("0") != 0) {
                            CBRequest.contactRequestSend(Integer.valueOf(contactId), new Integer(nSharingStatus), strShareHomeFids, strShareWorkFids,
                                    new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                isChanged = false;
                                                updateButtonIfChanged();
                                                Intent intent = new Intent();
                                                intent.putExtra("contactID" , contactId);
                                                intent.putExtra("nSharingStatus" , sharingSTATUS);
                                                setResult(Activity.RESULT_OK, intent);
                                                finish();
                                            } else {
                                                //isChanged = false;
                                                //updateButtonIfChanged();
                                            }
                                        }
                                    }, true);
                        }
                        else
                        {
                            CBRequest.contactRequestSend(strEmail, new Integer(nSharingStatus), strShareHomeFids, strShareWorkFids,
                                    new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                isChanged = false;
                                                updateButtonIfChanged();
                                                Intent intent = new Intent();
                                                intent.putExtra("contactID" , contactId);
                                                intent.putExtra("nSharingStatus" , sharingSTATUS);
                                                setResult(Activity.RESULT_OK, intent);
                                                finish();
                                            } else {
                                                //isChanged = false;
                                                //updateButtonIfChanged();
                                            }
                                        }
                                    }, true);
                        }
                    } else
                    {
                        if (directoryId != 0)
                        {
                            DirectoryRequest.joinDirectory(directoryId, new Integer(nSharingStatus), strShareHomeFids, strShareWorkFids,
                                new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    if (response.isSuccess())
                                    {
                                        isChanged = false;
                                        updateButtonIfChanged();
                                        JSONObject object = response.getData();
                                        Integer status = 0;
                                        try {
                                            status = object.getInt("status_code");
                                            domainName = object.getString("domain");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        if (isJoin) {
                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("status_code",status);
                                            setResult(Activity.RESULT_OK,returnIntent);
                                            finish();
                                        } else {
                                            if (directoryName != null) {
                                                menuCode = status;
                                                menuName = directoryName;

                                                Intent intent = new Intent(ShareYourLeafActivity.this, MenuActivity.class);
                                                intent.putExtra("isShowDialog", true);
                                                intent.putExtra("directoryName", directoryName);
                                                intent.putExtra("directoryID", directoryId);
                                                intent.putExtra("status_code",status);
                                                startActivity(intent);
                                                finish();
                                            } else {

                                            }
                                        }
//                                        Intent intent = new Intent(ShareYourLeafActivity.this, MenuActivity.class);
//                                        intent.putExtra("isShowDialog", true);
//                                        startActivity(intent);
//                                        finish();
                                    } else
                                    {

                                    }
                                }
                            }, true);
                        }
                    }
                }
                else {
                    if (!isDirectory)
                    {
                        CBRequest.updatePermission(Integer.valueOf(contactId), new Integer(nSharingStatus), strShareHomeFids, strShareWorkFids,
                            new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        isChanged = false;
                                        updateButtonIfChanged();
                                        Intent intent = new Intent();
                                        intent.putExtra("contactID" , contactId);
                                        intent.putExtra("nSharingStatus" , sharingSTATUS);
                                        setResult(Activity.RESULT_OK, intent);
                                        finish();
                                    } else {
                                        //isChanged = false;
                                        //updateButtonIfChanged();
                                    }
                                }
                            }, true);
                    } else if (directoryId != 0)
                    {
                        DirectoryRequest.updatePermission(directoryId, new Integer(nSharingStatus), strShareHomeFids, strShareWorkFids,
                            new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        finish();
                                    } else {

                                    }
                                }
                        }, true);
                    }

                }
                break;

            //select share all
            case R.id.imgShareAll:
                if(isShareAllSelected) {
                    isShareAllSelected = !isShareAllSelected;
                    if(isChatOnlySelected)
                        isChatOnlySelected = false;
                    updateCheckBox();
                    selectHomeHeader(false);
                    selectWorkHeader(false);
                    for(int i =0 ;i<homeFieldViews.size();i++)
                    {
                        homeFieldViews.get(i).setSelected(false);
                    }
                    for(int i =0 ;i<workFieldViews.size();i++)
                    {
                        workFieldViews.get(i).setSelected(false);
                    }
                }else {
                    isShareAllSelected = !isShareAllSelected;
                    if (isChatOnlySelected)
                        isChatOnlySelected = false;
                    updateCheckBox();
                    selectHomeHeader(true);
                    selectWorkHeader(true);
                    for (int i = 0; i < homeFieldViews.size(); i++) {
                        homeFieldViews.get(i).setSelected(true);
                    }
                    for (int i = 0; i < workFieldViews.size(); i++) {
                        workFieldViews.get(i).setSelected(true);
                    }
                }
                refreshFieldViews(2);
                checkIsChanged();
                updateButtonIfChanged();

                if(isPendingRequest && isUnexchangedContact || isInviteContact)
                {
                    checkCancelPendingRequest();
                }
                break;

            //select "Im shy chat only"
            case R.id.imgChatOnly:
                //if(isChatOnlySelected) return;
                isChatOnlySelected = !isChatOnlySelected;

                if(isShareAllSelected)
                    isShareAllSelected = false;

                updateCheckBox();
                selectHomeHeader(false);
                selectWorkHeader(false);
                for(int i =0 ;i<homeFieldViews.size();i++)
                {
                    homeFieldViews.get(i).setSelected(false);
                }
                for(int i =0 ;i<workFieldViews.size();i++)
                {
                    workFieldViews.get(i).setSelected(false);
                }

                isShareAllSelected = isShareAllSelected();

                refreshFieldViews(2);
                updateCheckBox();
                checkIsChanged();
                updateButtonIfChanged();

                if(isPendingRequest && isUnexchangedContact || isInviteContact)
                {
                    checkCancelPendingRequest();
                }
                break;

            //select or unselect all home fileds
            case R.id.homeFieldHeaderLayout:
                if(isHomeHeaderSelected) //if home header was already selected,  then deselect all home fields
                {
                    for(int i=0;i<homeFieldViews.size();i++)
                    {
                        homeFieldViews.get(i).setSelected(false);
                    }
                }
                else
                {
                    for(int i=0;i<homeFieldViews.size();i++)
                    {
                        homeFieldViews.get(i).setSelected(true);
                    }
                }

                isShareAllSelected = isShareAllSelected();
                refreshFieldViews(0);

                if(isHomeHeaderSelected && isWorkHeaderSelected) {
                    isShareAllSelected = true;
                    isChatOnlySelected = false;
                }
                else if(isHomeHeaderSelected && workFieldViews.size() == 0) {
                    isShareAllSelected = true;
                    isChatOnlySelected = false;
                }
                else
                {
                    isShareAllSelected = false;
                    isChatOnlySelected = false;
                }
                updateCheckBox();

                checkIsChanged();
                updateButtonIfChanged();

                if(isPendingRequest && isUnexchangedContact)
                {
                    checkCancelPendingRequest();
                }
                else if(!isHomeHeaderSelected && isInviteContact)
                {
                    checkCancelPendingRequest();
                }

                break;

            //select or unselect all work fileds
            case R.id.workFieldHeaderLayout:
                if(isWorkHeaderSelected) //if home header was already selected,  then deselect all home fields
                {
                    for(int i=0;i<workFieldViews.size();i++)
                    {
                        workFieldViews.get(i).setSelected(false);
                    }
                }
                else
                {
                    for(int i=0;i<workFieldViews.size();i++)
                    {
                        workFieldViews.get(i).setSelected(true);
                    }
                }
                isShareAllSelected = isShareAllSelected();
                refreshFieldViews(1);

                if(isHomeHeaderSelected && isWorkHeaderSelected) {
                    isShareAllSelected = true;
                    isChatOnlySelected = false;
                }
                else if(isWorkHeaderSelected && homeFieldViews.size() == 0) {
                    isShareAllSelected = true;
                    isChatOnlySelected = false;
                }
                else
                {
                    isShareAllSelected = false;
                    isChatOnlySelected = false;
                }
                updateCheckBox();

                checkIsChanged();
                updateButtonIfChanged();

                if(isPendingRequest && isUnexchangedContact)
                {
                    checkCancelPendingRequest();
                }
                else if(!isWorkHeaderSelected && isInviteContact)
                {
                    checkCancelPendingRequest();
                }

                break;

            //delete contact
            case R.id.imgBtnDelete:
                //confirm dialog to delete this contact forever
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                dialog.dismiss();

                                if (!isDirectory)
                                {
                                    CBRequest.removeFriend(contactId, new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                if (strResult != null && strResult.equalsIgnoreCase("GinkoMe"))
                                                {
                                                    Intent returnIntent = new Intent();
                                                    returnIntent.putExtra("isContactDeleted" , true);
                                                    ShareYourLeafActivity.this.setResult(Activity.RESULT_OK , returnIntent);
                                                    finish();
                                                } else
                                                {
                                                    Intent returnIntent = new Intent(ShareYourLeafActivity.this, ContactMainActivity.class);
                                                    startActivity(returnIntent);
                                                    finish();
                                                }
                                            } else {
                                                MyApp.getInstance().showSimpleAlertDiloag(ShareYourLeafActivity.this , R.string.str_err_fail_to_delete_grey_contact , null);
                                            }
                                        }
                                    },true);
                                } else
                                {
                                    DirectoryRequest.quitMember(directoryId, new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                Intent returnIntent = new Intent(ShareYourLeafActivity.this, ContactMainActivity.class);
                                                startActivity(returnIntent);
                                                finish();
                                            } else {
                                                MyApp.getInstance().showSimpleAlertDiloag(ShareYourLeafActivity.this , R.string.str_err_fail_to_quit_directory , null);
                                            }
                                        }
                                    },true);
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ShareYourLeafActivity.this);
                CharSequence txtMessage = null;
                if (isDirectory)
                    txtMessage = getResources().getString(R.string.str_quit_directory_forever);
                else
                    txtMessage = getResources().getString(R.string.str_delete_contact_forever);

                TextView myMsg = new TextView(this);
                myMsg.setText(txtMessage);
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), dialogClickListener)
                        .setCancelable(false)
                        .show();
                break;

            //if click map icon , then go to map view screen to show the location of unexchanged contact
            case R.id.imgMapIcon:
                Intent mapViewerIntent = new Intent(ShareYourLeafActivity.this , LocationMapViewerActivity.class);
                mapViewerIntent.putExtra("lat" , lattitude);
                mapViewerIntent.putExtra("long" , longitude);
                mapViewerIntent.putExtra("address" , strAddress);
                startActivity(mapViewerIntent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getAddressFromLatlngTask != null)
        {
            try
            {
                getAddressFromLatlngTask.cancel(true);
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            finally {
                getAddressFromLatlngTask = null;
            }
        }
    }

    private void refreshFieldViews(int whereUpdate)
    {
        /*
        whereUpdate 0 : update home fields
              1 : update work fields
              2 : update all fields
         */
        if(whereUpdate == 2 || whereUpdate==0) {
            if(isAllFieldSelected(homeFieldViews))
                selectHomeHeader(true);
            else
                selectHomeHeader(false);
            for (int i = 0; i < homeFieldViews.size(); i++) {
                if (i == 0) {
                    if(homeFieldViews.get(i).isSelected() && isHomeHeaderSelected)
                    {
                        homeHeaderDivider.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        homeHeaderDivider.setVisibility(View.GONE);
                    }
                }
                if (i < homeFieldViews.size() - 1) {
                    if (homeFieldViews.get(i).isSelected() && homeFieldViews.get(i + 1).isSelected())
                        homeFieldViews.get(i).setDividerVisibility(true);
                    else
                        homeFieldViews.get(i).setDividerVisibility(false);
                } else if (i == homeFieldViews.size() - 1) {
                    if (homeFieldViews.get(i).isSelected() && isWorkHeaderSelected)
                        homeFieldViews.get(i).setDividerVisibility(true);
                    else
                        homeFieldViews.get(i).setDividerVisibility(false);
                }
                homeFieldViews.get(i).refreshView();
            }
        }
        if(whereUpdate == 2 || whereUpdate ==1) {
            if(isAllFieldSelected(workFieldViews))
                selectWorkHeader(true);
            else
                selectWorkHeader(false);
            for (int i = 0; i < workFieldViews.size(); i++) {
                if (i == 0) {
                    if(workFieldViews.get(i).isSelected() && isWorkHeaderSelected)
                    {
                        workHeaderDivider.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        workHeaderDivider.setVisibility(View.GONE);
                    }
                }
                if (i < workFieldViews.size() - 1) {
                    if (workFieldViews.get(i).isSelected() && workFieldViews.get(i + 1).isSelected())
                        workFieldViews.get(i).setDividerVisibility(true);
                    else
                        workFieldViews.get(i).setDividerVisibility(false);
                } else if (i == workFieldViews.size() - 1) {
                        workFieldViews.get(i).setDividerVisibility(false);
                }
                workFieldViews.get(i).refreshView();
            }
        }
    }

    @Override
    public void onFieldClicked(boolean bHomeOrWork) {


        updateCheckBox();

        if(bHomeOrWork)
        {
            refreshFieldViews(0);
        }
        else
        {
            refreshFieldViews(1);
        }
        if(isHomeHeaderSelected && isWorkHeaderSelected) {
            isShareAllSelected = true;
            isChatOnlySelected = false;
        }
        else if(isHomeHeaderSelected && workFieldViews.size() == 0)
        {
            isShareAllSelected = true;
            isChatOnlySelected = false;
        }
        else if(isWorkHeaderSelected && homeFieldViews.size() == 0)
        {
            isShareAllSelected = true;
            isChatOnlySelected = false;
        }
        else
        {
            isShareAllSelected = false;
            isChatOnlySelected = false;
        }
        updateCheckBox();

        checkIsChanged();
        updateButtonIfChanged();

        /*
        if(isPendingRequest && isUnexchangedContact)
        {
            checkCancelPendingRequest();
        }
        */
        if(isAllFieldUnselected(homeFieldViews) && isAllFieldUnselected(workFieldViews))
            checkCancelPendingRequest();
    }

    private class GetAddressFromLatlngTask extends AsyncTask<Void ,Void , Void>
    {
        private double lat , lng;

        public GetAddressFromLatlngTask(double _lat , double _lng)
        {
            this.lat = _lat;
            this.lng = _lng;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            ArrayList<String> addresses = GeoLibrary.getAddressListFromLatLng(lat, lng, 20);
            for(int i=0;i<addresses.size();i++)
            {
                String address = addresses.get(i);
                if(address.equals("")) continue;

                strAddress = address;
                break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            txtAddress.setText(strAddress);
            getAddressFromLatlngTask = null;
        }

        @Override
        protected void onCancelled(Void result) {
            // TODO Auto-generated method stub
            super.onCancelled(result);
            strAddress = "";
        }
    }

    //For Sort
    private List<UserProfileVO> addEachFieldInfo(List<UserProfileVO> fieldsInfo){
        nameFields = null;
        companyFields = null;
        mobileFields = null;
        phoneFields = null;
        emailFields = null;
        addressFields = null;
        faxFields = null;
        birthdayFields = null;
        socialNetworkFields = null;
        websiteFields = null;
        customFields = null;
        List<UserProfileVO> fields = fieldsInfo;

        for(UserProfileVO field:fields)
        {
            String strFieldName = field.getFieldName();
            String fieldType = field.getFieldType();
            if(fieldType.equals(""))
                continue;
            if (fieldType.equalsIgnoreCase("abbr"))
                continue;
            if (fieldType.equalsIgnoreCase("privilege"))
                continue;
            if (fieldType.equalsIgnoreCase("video"))
                continue;
            if (fieldType.equalsIgnoreCase("foreground"))
                continue;
            if (fieldType.equalsIgnoreCase("background"))
                continue;
            if (strFieldName.toLowerCase().contains("company")) {
                if(companyFields == null)
                    companyFields = new ArrayList<UserProfileVO>();
                companyFields.add(field);
            }else if(strFieldName.toLowerCase().contains("title"))
            {
                if(nameFields == null)
                    nameFields = new ArrayList<UserProfileVO>();
                nameFields.add(field);
            }else if (strFieldName.toLowerCase().contains("mobile") || strFieldName.toLowerCase().contains("phone")) {
                if(mobileFields == null)
                    mobileFields = new ArrayList<UserProfileVO>();
                mobileFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("email")) {
                if(emailFields == null)
                    emailFields = new ArrayList<UserProfileVO>();
                emailFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("address")) {
                if(addressFields == null)
                    addressFields = new ArrayList<UserProfileVO>();
                addressFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("fax")) {
                if(faxFields == null)
                    faxFields = new ArrayList<UserProfileVO>();
                faxFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("birthday")) {
                if(birthdayFields == null)
                    birthdayFields = new ArrayList<UserProfileVO>();
                birthdayFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("facebook") || strFieldName.toLowerCase().contains("twitter") || strFieldName.toLowerCase().contains("linkedin")) {
                if(socialNetworkFields == null)
                    socialNetworkFields = new ArrayList<UserProfileVO>();
                socialNetworkFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("website")) {
                if(websiteFields == null)
                    websiteFields = new ArrayList<UserProfileVO>();
                websiteFields.add(field);
            }
            else if (strFieldName.toLowerCase().contains("custom")) {
                if(customFields == null)
                    customFields = new ArrayList<UserProfileVO>();
                customFields.add(field);
            }
        }
        //Sort mobile, phone, address, ......
        FieldNameComparator fieldNameComparator = new FieldNameComparator();

        try {
            Collections.sort(mobileFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(emailFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(addressFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(socialNetworkFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Collections.sort(customFields, fieldNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<UserProfileVO> returnFields = new ArrayList<UserProfileVO>();
        if(mobileFields != null) {
            for (UserProfileVO field : mobileFields) {
                returnFields.add(field);
            }
        }
        if(emailFields != null) {
            for (UserProfileVO field : emailFields) {
                returnFields.add(field);
            }
        }
        if(addressFields != null) {
            for (UserProfileVO field : addressFields) {
                returnFields.add(field);
            }
        }
        if(faxFields != null) {
            for (UserProfileVO field : faxFields) {
                returnFields.add(field);
            }
        }
        if(birthdayFields != null) {
            for (UserProfileVO field : birthdayFields) {
                returnFields.add(field);
            }
        }
        if(socialNetworkFields != null) {
            for (UserProfileVO field : socialNetworkFields) {
                returnFields.add(field);
            }
        }
        if(websiteFields != null) {
            for (UserProfileVO field : websiteFields) {
                returnFields.add(field);
            }
        }
        if(customFields != null) {
            for (UserProfileVO field : customFields) {
                returnFields.add(field);
            }
        }

        return returnFields;
    }

    class FieldNameComparator implements Comparator<UserProfileVO> {
        public FieldNameComparator()
        {
        }

        @Override
        public int compare(UserProfileVO lhs, UserProfileVO rhs) {
            return compareFieldName(lhs.getFieldName() , rhs.getFieldName());
        }

        private int compareFieldName(String leftFieldName ,String rightFieldName)
        {
            int result = 0;
            if(leftFieldName.equalsIgnoreCase(rightFieldName))
            {
                result = 0;
            }
            else
            {
                String leftFieldNamePrefix = "";
                String rightFieldNamePrefix = "";
                if(leftFieldName.contains("#"))
                    leftFieldNamePrefix = leftFieldName.substring(0, leftFieldName.indexOf("#"));
                else
                    leftFieldNamePrefix = leftFieldName;

                if(rightFieldName.contains("#"))
                    rightFieldNamePrefix = rightFieldName.substring(0 , rightFieldName.indexOf("#"));
                else
                    rightFieldNamePrefix = rightFieldName;

                if(!leftFieldNamePrefix.equalsIgnoreCase(rightFieldNamePrefix))
                {
                    result = leftFieldNamePrefix.compareTo(rightFieldNamePrefix);
                }
                else {

                    int leftFieldIndex = 1;
                    int rightFieldIndex = 1;
                    if (leftFieldName.contains("#")) {
                        try {
                            leftFieldIndex = Integer.valueOf(leftFieldName.substring(leftFieldName.length() - 1, leftFieldName.length() ));
                        } catch (Exception e) {
                            e.printStackTrace();
                            leftFieldIndex = 2;
                        }
                    } else {
                        leftFieldIndex = 0;
                    }

                    if (rightFieldName.contains("#")) {
                        try {
                            rightFieldIndex = Integer.valueOf(rightFieldName.substring(rightFieldName.length() - 1, rightFieldName.length()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            rightFieldIndex = 2;
                        }
                    } else {
                        rightFieldIndex = 0;
                    }
                    if (leftFieldIndex < rightFieldIndex)
                        result = -1;
                    else if (leftFieldIndex == rightFieldIndex)
                        result = 0;
                    else if (leftFieldIndex > rightFieldIndex)
                        result = 1;
                }
            }
            return result;
        }
    }
}
