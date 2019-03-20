package com.ginko.setup;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.profiles.CustomSizeMeasureView;
import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.HomeWorkAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HomeWorkAddInfoActivity extends MyBaseFragmentActivity implements View.OnClickListener ,
                                                                        CustomSizeMeasureView.OnMeasureListner,
                                                                        ActionSheet.ActionSheetListener
{
    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";

    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;

    private final int HOME_GROUP = 1;
    private final int WORK_GROUP = 2;

    private int groupType = HOME_GROUP;

    /* UI Elements */
    private NetworkImageView imgViewAvatar;
    private LinearLayout blankLayoutProfileComment;
    private TextView textBoundMeasureView;
    private ImageView btnSkipWorkAddInfo , btnClose , btnDelete;
    private ImageButton btnConfirm;
    private ImageView btnEdit , btnSharePublic , btnAddInfoItem;
    private TextView txtViewTitle , textViewPlusProfile , textViewSnapshot;
    private LinearLayout profileSnapshotLayout;
    private LinearLayout samePictureAsHomeLayout;
    private ImageView imgCheckBoxSamePicAsHome;

    private PopupWindow addInfoPopupWindow = null;
    private View addInfoPopupView = null;
    private ImageView btnAddInfoItemPopupClose , btnAddInfoItemPopupConfirm;
    private ListView addInfoListView;
    private RelativeLayout rootLayout;
    private LinearLayout popupRootLayout;


    private CustomSizeMeasureView sizeMeasureView;

    /* Variables */
    private String type;
    private UserWholeProfileVO userInfo;

    private UserUpdateVO groupInfo;

    private ImageLoader imgLoader;

    private HomeWorkAddInfoFragment infoListFragment;

    private int activityHeight = 0 , activityWidth = 0;

    private boolean isEditable = false;
    private boolean isPublicLocked = true;
    private boolean isSamePictureAsHome = false;

    private AddInfoListAdapter addInfoListAdapter;


    private ActionSheet takePhotoActionSheet = null;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" ;

    private Uri uri;
    private String tempPhotoUriPath = "";

    private String strProfilePhotoUrl = "";

    private float ratio = 1.0f;

    private boolean isAddNewWorkProfile = false;

    private View.OnClickListener snapPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            if(takePhotoActionSheet == null)
                takePhotoActionSheet = ActionSheet.createBuilder(HomeWorkAddInfoActivity.this , getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo) ,
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery) ,
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(HomeWorkAddInfoActivity.this)
                        .show();
            else
                takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_work_addinfo);

        if(savedInstanceState!= null)
        {
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
            this.isAddNewWorkProfile = savedInstanceState.getBoolean("isAddNewWorkProfile" , false);
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
            this.isAddNewWorkProfile = bundle.getBoolean("isAddNewWorkProfile" , false);
        }

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        groupInfo = userInfo.getGroupInfoByGroupType(groupType);

        imgLoader = MyApp.getInstance().getImageLoader();


        isPublicLocked = !groupInfo.isPublic();

        getUIObjects();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        textBoundMeasureView = (TextView)findViewById(R.id.textBoundMeasureView);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);

        addInfoPopupView = getLayoutInflater().inflate(R.layout.grey_contact_profile_add_info_popup, null);
        btnAddInfoItemPopupClose = (ImageView)addInfoPopupView.findViewById(R.id.btnAddInfoPopupClose);btnAddInfoItemPopupClose.setOnClickListener(this);
        btnAddInfoItemPopupConfirm = (ImageView)addInfoPopupView.findViewById(R.id.btnAddInfoPopupConfirm);btnAddInfoItemPopupConfirm.setOnClickListener(this);
        addInfoListView = (ListView)addInfoPopupView.findViewById(R.id.infoList);

        imgViewAvatar = (NetworkImageView)findViewById(R.id.imgViewAvatar);imgViewAvatar.setOnClickListener(snapPhotoClickListener);
        imgViewAvatar.setDefaultImageResId(R.drawable.part_a_user_photo_placeholder_purple);
        strProfilePhotoUrl = groupInfo.getProfileImage();
        imgViewAvatar.setImageUrl(strProfilePhotoUrl , imgLoader);

        txtViewTitle = (TextView)findViewById(R.id.textViewTitle);
        textViewPlusProfile = (TextView)findViewById(R.id.textViewPlusProfile);
        textViewSnapshot = (TextView)findViewById(R.id.textViewSnapshot);
        //btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnSkipWorkAddInfo = (ImageView)findViewById(R.id.btnSkipWorkAddInfo);btnSkipWorkAddInfo.setOnClickListener(this);

        btnEdit = (ImageView)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnSharePublic = (ImageView)findViewById(R.id.btnSharePublic); btnSharePublic.setOnClickListener(this);
        btnAddInfoItem = (ImageView)findViewById(R.id.btnAddInfoItem); btnAddInfoItem.setOnClickListener(this);

        //profile snapshot layout to take photo for user home
        profileSnapshotLayout = (LinearLayout)findViewById(R.id.profileSnapshotLayout); profileSnapshotLayout.setOnClickListener(snapPhotoClickListener);

        //check box layout to select the same picture as home
        samePictureAsHomeLayout = (LinearLayout)findViewById(R.id.samePictureAsHomeLayout); samePictureAsHomeLayout.setOnClickListener(this);
        imgCheckBoxSamePicAsHome = (ImageView)findViewById(R.id.imgCheckBoxSamePictureAsHome);

        //init UI values according to the home or work status
        switch(groupType)
        {
            case HOME_GROUP:
                txtViewTitle.setText(getResources().getString(R.string.home_info));
                textViewPlusProfile.setTextColor(0xff83d570);
                textViewSnapshot.setTextColor(0xff83d570);

                btnEdit.setImageResource(R.drawable.part_a_btn_edit_setup_green);
                btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_green);
                btnAddInfoItem.setImageResource(R.drawable.part_a_btn_add_info_green);

                btnSkipWorkAddInfo.setVisibility(View.GONE);
                //btnPrev.setVisibility(View.VISIBLE);

                samePictureAsHomeLayout.setVisibility(View.GONE);
                isSamePictureAsHome = false;
                break;
            case WORK_GROUP:
                txtViewTitle.setText(getResources().getString(R.string.work_info));
                textViewPlusProfile.setTextColor(0xff7e5785);
                textViewSnapshot.setTextColor(0xff7e5785);

                btnSkipWorkAddInfo.setVisibility(View.VISIBLE);
                //btnPrev.setVisibility(View.GONE);

                btnEdit.setImageResource(R.drawable.part_a_btn_edit_setup_purple);
                btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_purple);
                btnAddInfoItem.setImageResource(R.drawable.part_a_btn_add_info_purple);

                samePictureAsHomeLayout.setVisibility(View.VISIBLE);
                if(userInfo.getWork().getProfileImage().equals(userInfo.getHome().getProfileImage()))
                    isSamePictureAsHome = true;
                else
                    isSamePictureAsHome = false;

                //groupInfo.setProfileImage(userInfo.getHome().getProfileImage());//set the work's profile image with the home's profile image
                //strProfilePhotoUrl = userInfo.getHome().getProfileImage();

                updateSamePictureRaidoBox();
                break;
        }

        initInfoItemFragment();

        isEditable = false;
        updateUIFromEditable();

        updatePublicIcon();

        checkEditButtonVisibility();

        //default is to hide btnEdit
        if(groupType == HOME_GROUP && groupInfo.getInputableFieldsCount() > 4)//if it has added fields except the default 4 fields, then show edit button
        {
            btnEdit.setVisibility(View.VISIBLE);
        }
        else if(groupType == WORK_GROUP && groupInfo.getInputableFieldsCount() > 6)//if it has added fields except the default 6 fields, then show edit button
        {
            btnEdit.setVisibility(View.VISIBLE);
        }
        else
            btnEdit.setVisibility(View.GONE);


        /*
        //check home/work info has other fields except the default fields , if has extra fields , then show delete button
        List<UserProfileVO> fields = userInfo.getHome().getFields();
        int validFieldsCount = 0;
        String[] dontShowFields = { "foreground", "background",
                "privilege", "abbr", "video" };
        for (UserProfileVO field: fields) {
            if (ArrayUtils.contains(dontShowFields,
                    field.getFieldType().toLowerCase()))
                continue;
            if(field.getValue().compareTo("") == 0) continue;

            validFieldsCount++;
        }
        if(groupType == HOME_GROUP)
        {
            if(validFieldsCount > 4)//if it has added fields except the default 4 fields, then show edit button
            {
                btnDelete.setVisibility(View.VISIBLE);
            }
            else
            {
                btnDelete.setVisibility(View.INVISIBLE);
            }
        }
        else//work has 6 default fields
        {
            if(validFieldsCount > 6)//if it has added fields except the default 4 fields, then show edit button
            {
                btnDelete.setVisibility(View.VISIBLE);
            }
            else
            {
                btnDelete.setVisibility(View.INVISIBLE);
            }
        }*/

    }

    private void initInfoItemFragment()
    {
        String strFullName = userInfo.getFirstName()==null?"": userInfo.getFirstName();

        infoListFragment = HomeWorkAddInfoFragment.newInstance(type , groupInfo , strFullName);

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.blankLayoutInfos , infoListFragment);
        ft.commit();
    }

    private void updateSamePictureRaidoBox()
    {
        if(isSamePictureAsHome)
        {
            imgCheckBoxSamePicAsHome.setImageResource(R.drawable.share_profile_selected);
        }
        else
        {
            imgCheckBoxSamePicAsHome.setImageResource(R.drawable.share_profile_non_selected);
        }

    }

    private void updatePublicIcon()
    {
        if(isPublicLocked)
        {
            switch(groupType) {
                case HOME_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_green);
                    break;
                case WORK_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_purple);
                    break;
            }
        }
        else
        {
            switch(groupType) {
                case HOME_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_unlock_green);
                    break;
                case WORK_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_unlock_purple);
                    break;
            }
        }
    }

    private void checkEditButtonVisibility()
    {
        if((groupType == HOME_GROUP && infoListFragment.getCurrentInfoItemCounts()>4) ||
                (groupType == WORK_GROUP && infoListFragment.getCurrentInfoItemCounts()>6))
                //there are 4 default items, name , mobile ,email , and address
        {
            if(isEditable)
            {
                btnEdit.setVisibility(View.GONE);
            }
            else
            {
                //if there is new item added
                btnEdit.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            btnEdit.setVisibility(View.GONE);
        }
    }

    private void deleteFieldItems(List<String> deleteFieldNames)
    {
        List<UserProfileVO> fields = groupInfo.getFields();
        if(fields == null || deleteFieldNames == null || deleteFieldNames.size()<1)
            return;

        int index = 0;

        while(index < fields.size())
        {
            UserProfileVO fieldItem = fields.get(index);
            String fieldType = fieldItem.getFieldType();
            String fieldName = fieldItem.getFieldName();

            if(fieldType.equals("")) {
                index++;
                continue;
            }
            if (fieldType.equalsIgnoreCase("abbr")) {
                index++;
                continue;
            }
            if (fieldType.equalsIgnoreCase("privilege")) {
                index++;
                continue;
            }
            if (fieldType.equalsIgnoreCase("video")) {
                index++;
                continue;
            }
            if (fieldType.equalsIgnoreCase("foreground")) {
                index++;
                continue;
            }
            if (fieldType.equalsIgnoreCase("background")) {
                index++;
                continue;
            }

            if(deleteFieldNames.contains(fieldName.toLowerCase()))
            {
                fields.remove(index);
                continue;
            }

            index++;
        }
    }


    private void updateUIFromEditable()
    {
        if(isEditable)
        {
            btnConfirm.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.VISIBLE);

            btnEdit.setVisibility(View.GONE);

            switch(groupType) {
                case HOME_GROUP:
                    btnSkipWorkAddInfo.setVisibility(View.GONE);
                    //btnPrev.setVisibility(View.GONE);
                    break;
                case WORK_GROUP:
                    btnSkipWorkAddInfo.setVisibility(View.GONE);
                    //btnPrev.setVisibility(View.GONE);
                    break;
            }
        }
        else
        {
            btnConfirm.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);

            checkEditButtonVisibility();

            switch(groupType) {
                case HOME_GROUP:
                    btnSkipWorkAddInfo.setVisibility(View.GONE);
                    //btnPrev.setVisibility(View.VISIBLE);
                    break;
                case WORK_GROUP:
                    btnSkipWorkAddInfo.setVisibility(View.VISIBLE);
                    //btnPrev.setVisibility(View.GONE);
                    break;
            }
        }

        //infoListFragment.refreshListFragment(isEditable);
    }

    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }
    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootLayout.getApplicationWindowToken(), 0);
    }
    private void enableAddInfoItemPopup()
    {
        // Creating a pop window for emoticons keyboard
        addInfoPopupWindow = new PopupWindow(addInfoPopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) activityHeight, false);
        addInfoPopupView.setFocusable(true);
        addInfoPopupWindow.setAnimationStyle(R.style.AnimationPopup);
        addInfoPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }
    private void showHidePopupView(PopupWindow popupWindow , boolean bShown)
    {
        if(popupWindow == null) return;
        if (!popupWindow.isShowing()) {
            popupWindow.setHeight((int) (activityHeight));
            if (bShown) {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            } else {
                popupRootLayout.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.showAtLocation(popupRootLayout, Gravity.BOTTOM, 0, 0);
            if(popupWindow == addInfoPopupWindow)
            {
                //list to show for adding empty item infos
                ArrayList<HomeWorkAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
                ArrayList<AddInfoItem> itemList = new ArrayList<AddInfoItem>();
                for(int i=0;i<infoList.size();i++)
                {
                    if(!infoList.get(i).isVisible) {
                        if(groupType == HOME_GROUP)//home
                        {
                            if(infoList.get(i).strInfoName.toLowerCase().equals("title") ||
                                    infoList.get(i).strInfoName.toLowerCase().equals("company"))
                                continue;
                        }
                        itemList.add(new AddInfoItem(infoList.get(i).strInfoName));
                    }
                }
                addInfoListAdapter = new AddInfoListAdapter(HomeWorkAddInfoActivity.this , itemList);
                addInfoListView.setAdapter(addInfoListAdapter);
                btnAddInfoItemPopupConfirm.setVisibility(View.GONE);
            }
        }
        else
        {
            if (bShown) {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            } else {
                popupRootLayout.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TYPE_PARAM, type);
        outState.putSerializable(USER_INFO_PARAM, userInfo);
        outState.putBoolean("isAddNewWorkProfile" , this.isAddNewWorkProfile);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        type = savedInstanceState.getString(TYPE_PARAM);
        this.isAddNewWorkProfile = savedInstanceState.getBoolean("isAddNewWorkProfile" ,false);
        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        groupInfo = userInfo.getGroupInfoByGroupType(groupType);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            //back
            case R.id.btnPrev:
                switch(groupType)
                {
                    case HOME_GROUP:
                        Intent tradeCardPhotoEditorForHomeIntent = new Intent(HomeWorkAddInfoActivity.this , TradeCardPhotoEditorSetActivity.class);
                        tradeCardPhotoEditorForHomeIntent.putExtra("isSetNewPhotoInfo" , true);
                        tradeCardPhotoEditorForHomeIntent.putExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);
                        startActivity(tradeCardPhotoEditorForHomeIntent);
                        finish();
                        break;
                    case WORK_GROUP:
                        Intent tradeCardPhotoEditorForWorkIntent = new Intent(HomeWorkAddInfoActivity.this , TradeCardPhotoEditorSetActivity.class);
                        tradeCardPhotoEditorForWorkIntent.putExtra("isSetNewPhotoInfo" , true);
                        tradeCardPhotoEditorForWorkIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
                        startActivity(tradeCardPhotoEditorForWorkIntent);
                        finish();
                        break;
                }
                finish();
                break;

            //done add info
            case R.id.btnConfirm:
                hideKeyboard();

                if(groupType == HOME_GROUP) {
                    UserUpdateVO homeInfo = null;// = infoListFragment.saveGroupInfo(HomeWorkAddInfoActivity.this , isPublicLocked, activityWidth, activityHeight, ratio, textBoundMeasureView , true);
                    if(homeInfo == null)
                        return;

                    homeInfo.setPublic(!isPublicLocked);
                    homeInfo.setProfileImage(strProfilePhotoUrl);
                    userInfo.setHome(homeInfo);

                }
                else {
                    UserUpdateVO workInfo = null;//= infoListFragment.saveGroupInfo(HomeWorkAddInfoActivity.this , isPublicLocked, activityWidth, activityHeight, ratio, textBoundMeasureView, true);
                    if(workInfo == null)
                        return;

                    workInfo.setPublic(!isPublicLocked);
                    workInfo.setProfileImage(strProfilePhotoUrl);
                    userInfo.setWork(workInfo);
                    if(isAddNewWorkProfile)
                    {
                        UserInfoRequest.setUserInfo(userInfo.getWork(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                {
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("myInfo" , userInfo);
                                    intent.putExtras(bundle);
                                    setResult(RESULT_OK , intent);
                                    finish();
                                    return;
                                }
                            }
                        });
                        return;
                    }
                }

                /*if(groupType == WORK_GROUP && isSamePictureAsHome)
                {
                    String profileImageUrl = userInfo.getHome().getProfileImage();
                    if(profileImageUrl!=null)
                        groupInfo.setProfileImage(profileImageUrl);
                    else
                        groupInfo.setProfileImage("");
                }*/

                /*UserInfoRequest.setUserInfo(this.groupInfo, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if(response.isSuccess())
                        {
                            //succeed to set home info
                        }
                        else
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(HomeWorkAddInfo.this , R.string.str_aler_failed_to_set_home_info);
                        }
                    }
                });*/

                Intent homeWorkProfileEditIntent = new Intent(HomeWorkAddInfoActivity.this , HomeWorkEditInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(TYPE_PARAM , type);
                bundle.putSerializable(USER_INFO_PARAM , userInfo);
                homeWorkProfileEditIntent.putExtras(bundle);
                startActivity(homeWorkProfileEditIntent);
                finish();

                break;

            //delete selected info items
            case R.id.btnDelete:
                //List<String> deleteFieldNames = infoListFragment.deleteSelectedInfoItems();
                //deleteFieldItems(deleteFieldNames);
                isEditable = false;
                updateUIFromEditable();
                checkEditButtonVisibility();

                break;

            //close editing state
            case R.id.btnClose:
                isEditable = false;
                updateUIFromEditable();
                break;

            //skip add work info item
            case R.id.btnSkipWorkAddInfo:
                if(isAddNewWorkProfile)
                {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                else {
                    Intent homeWorkInfoPreviewIntent = new Intent(HomeWorkAddInfoActivity.this, HomeWorkInfoPreviewActivity.class);
                    Bundle bundle2 = new Bundle();
                    bundle2.putBoolean("isWorkSkipped" , true);
                    bundle2.putString(TYPE_PARAM, "home");
                    bundle2.putSerializable(USER_INFO_PARAM, userInfo);
                    homeWorkInfoPreviewIntent.putExtras(bundle2);
                    startActivity(homeWorkInfoPreviewIntent);
                    finish();
                }
                break;

            //edit info
            case R.id.btnEdit:
                isEditable = !isEditable;
                updateUIFromEditable();
                break;

            //share public or not
            case R.id.btnSharePublic:
                isPublicLocked = !isPublicLocked;
                updatePublicIcon();

                break;

            //add info items
            case R.id.btnAddInfoItem:
                showHidePopupView(addInfoPopupWindow, true);
                break;

            //select if use the same profile picture as home
            case R.id.samePictureAsHomeLayout:
                if(isSamePictureAsHome == true)
                {
                    if(!groupInfo.getProfileImage().equals("") && groupInfo.getProfileImage().compareTo(userInfo.getHome().getProfileImage())!=0) {
                        TradeCard.removeProfileImage(groupType, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    groupInfo.setProfileImage("");
                                    strProfilePhotoUrl = "";
                                    isSamePictureAsHome = false;
                                    updateSamePictureRaidoBox();
                                    imgViewAvatar.refreshOriginalBitmap();
                                    imgViewAvatar.setImageUrl(strProfilePhotoUrl, imgLoader);
                                    imgViewAvatar.invalidate();
                                }
                            }
                        });
                    }
                    else
                    {
                        groupInfo.setProfileImage("");
                        strProfilePhotoUrl = "";
                        isSamePictureAsHome = false;
                        updateSamePictureRaidoBox();
                        imgViewAvatar.refreshOriginalBitmap();
                        imgViewAvatar.setImageUrl(strProfilePhotoUrl, imgLoader);
                        imgViewAvatar.invalidate();
                    }
                    return;
                }
                else
                {
                    groupInfo.setProfileImage(userInfo.getHome().getProfileImage());//set the work's profile image with the home's profile image
                    strProfilePhotoUrl = userInfo.getHome().getProfileImage();
                    imgViewAvatar.refreshOriginalBitmap();
                    imgViewAvatar.setImageUrl(strProfilePhotoUrl, imgLoader);
                    imgViewAvatar.invalidate();
                }
                isSamePictureAsHome = !isSamePictureAsHome;
                updateSamePictureRaidoBox();
                break;

            //add info item listview popup close
            case R.id.btnAddInfoPopupClose:
                showHidePopupView(addInfoPopupWindow, false);
                break;

            //add info item listview popup confirm
            case R.id.btnAddInfoPopupConfirm:
                for(int i=0;i<addInfoListAdapter.getCount();i++)
                {
                    AddInfoItem item = (AddInfoItem) addInfoListAdapter.getItem(i);
                    if(item.isSelected)
                    {
                        infoListFragment.addNewInfoItem(item.strInfoName);
                    }
                }
                infoListFragment.updateInfoListViews(false);
                checkEditButtonVisibility();
                showHidePopupView(addInfoPopupWindow, false);
                break;

        }
    }

    private void setUserProfilePhoto()
    {
        File photoFile = null;
        try {
            photoFile = new File(new URI(strTempPhotoPath.replaceAll(" ", "%20")));

            if(!photoFile.exists())
                return;

            String zippedPhotoFile = ImageScalingUtilities.decodeFile(photoFile.getAbsolutePath(), 300, 300);
            if(zippedPhotoFile.equalsIgnoreCase(""))
                return;

            photoFile = new File(zippedPhotoFile);

            final String strUploadPhotoPath = photoFile.getAbsolutePath();

            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {
                TradeCard.uploadProfileImage(groupType, new File(strUploadPhotoPath), new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject data = response.getData();
                            try {
                                String photoUrl = data.getString("profile_image");
                                System.out.println("---Uploaded photo url = "+photoUrl+" ----");
                                strProfileImagePath = strTempPhotoPath;
                                strProfilePhotoUrl = photoUrl;
                                groupInfo.setProfileImage(photoUrl);
                                imgViewAvatar.refreshOriginalBitmap();
                                imgViewAvatar.setImageUrl("file://"+strUploadPhotoPath, imgLoader);
                                imgViewAvatar.invalidate();
                                strTempPhotoPath = "";

                                if(groupType == WORK_GROUP && isSamePictureAsHome)
                                {
                                    isSamePictureAsHome = false;
                                    updateSamePictureRaidoBox();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                strTempPhotoPath = "";
                            }
                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgViewAvatar.refreshOriginalBitmap();
                            imgViewAvatar.setImageUrl(strProfileImagePath, imgLoader);
                            imgViewAvatar.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(HomeWorkAddInfoActivity.this, R.string.str_err_upload_photo, null);
                        }
                    }
                } , true);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch(requestCode)
            {
                case TAKE_PHOTO_FROM_CAMERA:
                    strTempPhotoPath = tempPhotoUriPath;// uri.getPath();
                    if(!strTempPhotoPath.contains("file://"))
                        strTempPhotoPath = "file://"+strTempPhotoPath;

                    System.out.println("-----Photo Path= "+strTempPhotoPath+"----");
                    setUserProfilePhoto();

                    break;
                case TAKE_PHOTO_FROM_GALLERY:
                    if(data!=null)
                    {
                        uri = data.getData();
                        File myFile = new File(uri.getPath());
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        String picturePath = "";

                        if (cursor != null && cursor.moveToFirst())
                        {
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            picturePath = cursor.getString(columnIndex);
                            cursor.close();
                        }
                        else
                        {
                            if (myFile.exists())
                                picturePath = myFile.getAbsolutePath();
                        }
                        strTempPhotoPath = picturePath;
                        if(!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        System.out.println("-----Photo Path= "+strTempPhotoPath+"----");
                        setUserProfilePhoto();
                    }
                    break;
            }
        }
        else
        {
            strTempPhotoPath = "";
            strProfileImagePath = "";
            //imgViewAvatar.setImageUrl(strProfileImagePath , imgLoader);
            //imgViewAvatar.invalidate();
        }
    }

    @Override
    public void onViewSizeMeasure(int width, int height) {
        //get acitivty height
        activityHeight = height;
        activityWidth = width;

        float r1 = (float)height/480;
        float r2 = (float)width/320;

        if(r2>r1)
            ratio = r2;
        else
            ratio = r1;

        System.out.println("----Activity Height = " + String.valueOf(height) + "-----");

        if(addInfoPopupWindow == null)
            enableAddInfoItemPopup();
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    //delete contact (delete permanently , delete for 24 hours)
    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//take a photo
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(System.currentTimeMillis()) + ".jpg"));
            tempPhotoUriPath = uri.getPath();
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            HomeWorkAddInfoActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            HomeWorkAddInfoActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
        }
        else if(index == 2)//remove photo
        {
            if(!groupInfo.getProfileImage().equals("")) {
                TradeCard.removeProfileImage(groupType, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            strProfilePhotoUrl = "";
                            imgViewAvatar.refreshOriginalBitmap();
                            imgViewAvatar.setImageUrl(strProfileImagePath, imgLoader);
                            imgViewAvatar.invalidate();
                            groupInfo.setProfileImage("");
                        }
                    }
                });
            }
            else
            {
                Uitils.alert(HomeWorkAddInfoActivity.this , getResources().getString(R.string.str_grey_contact_remove_photo_alert));
            }
        }
    }

    class AddInfoItem
    {
        public String strInfoName = "";
        public boolean isSelected;
        public AddInfoItem(String infoName )
        {
            this.strInfoName = infoName;
        }
    }

    class AddInfoListAdapter extends BaseAdapter
    {
        private Context mContext;
        private ArrayList<AddInfoItem> listItems;
        private Resources res;
        public AddInfoListAdapter(Context context , ArrayList<AddInfoItem> items)
        {
            this.mContext = context;
            this.listItems = new ArrayList<AddInfoItem>();
            if(items != null) {
                for (int i = 0; i<items.size(); i++)
                    this.listItems.add(items.get(i));
            }
            this.res = mContext.getResources();
        }

        @Override
        public int getCount() {
            return listItems == null?0:listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public boolean hasSelectedItems()
        {
            if(listItems == null)
                return false;

            int count = 0;
            for(int i=0;i<listItems.size();i++)
            {
                if(listItems.get(i).isSelected)
                    count++;
            }

            if(count>0)
                return true;

            return false;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder(); // our view holder of the row
            if (convertView == null) {

                holder.txtInfoItem = new TextView(mContext);
                holder.txtInfoItem.setPadding(this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_left_right_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_top_bottom_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_left_right_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_top_bottom_padding)
                );
                holder.txtInfoItem.setTextColor(Color.BLACK);
                holder.txtInfoItem.setTextSize(this.res.getDimension(R.dimen.grey_contact_add_info_text_size));
                holder.txtInfoItem.setBackgroundColor(Color.WHITE);
                holder.txtInfoItem.setSingleLine(true);
                holder.txtInfoItem.setLinksClickable(false);
                holder.txtInfoItem.setAutoLinkMask(0);
                convertView = holder.txtInfoItem;

                convertView.setTag(holder);

            }
            holder = (ViewHolder) convertView.getTag();
            holder.txtInfoItem.setText(listItems.get(position).strInfoName);
            if(listItems.get(position).isSelected)
                holder.txtInfoItem.setBackgroundColor(0xffd9d9d9);
            else
                holder.txtInfoItem.setBackgroundColor(Color.WHITE);

            holder.txtInfoItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(addInfoListAdapter!=null)
                    {
                        AddInfoItem item = (AddInfoItem) getItem(position);
                        item.isSelected = !item.isSelected;
                        if(addInfoListAdapter.hasSelectedItems())
                            btnAddInfoItemPopupConfirm.setVisibility(View.VISIBLE);
                        else
                            btnAddInfoItemPopupConfirm.setVisibility(View.GONE);

                        notifyDataSetChanged();
                    }
                }
            });
            return convertView;
        }
    }
    class ViewHolder
    {
        TextView txtInfoItem;
    }
}
