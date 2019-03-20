package com.ginko.activity.user;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.profiles.CustomSizeMeasureView;
import com.ginko.api.request.TradeCard;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.MyViewPager;
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

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfileEditInfoActivity extends MyBaseFragmentActivity implements View.OnClickListener,
        ActionSheet.ActionSheetListener,
        CustomSizeMeasureView.OnMeasureListner
{
    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";
    private final int HOME_GROUP = 1;
    private final int WORK_GROUP = 2;

    private int groupType = HOME_GROUP;

    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;

    /* UI elements */
    private TextView textBoundMeasureView;
    private ImageButton btnConfirm;
    private ImageView btnHome , btnWork , btnAddInfoItem , btnSharePublic , btnDelete;
    private TextView textViewPlusProfile , textViewSnapshot;
    private NetworkImageView imgHomeProfilePhoto , imgWorkProfilePhoto;
    private LinearLayout homeProfilePhotoLayout , workProfilePhotoLayout;
    private HomeWorkAddInfoFragment homeInfoListFragment , workInfoListFragment , currentFragment;
    private LinearLayout profileSnapshotLayout;

    /* Variables */
    private String type;
    private UserWholeProfileVO userInfo;

    private UserUpdateVO currentGroup;

    private ImageLoader imgLoader;

    private AddInfoListAdapter addInfoListAdapter;

    private MyPagerAdapter pageAdapter = null;
    private MyViewPager mPager;
    private MyOnPageChangeListener myPageListener;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private int currIndex = 0;


    private PopupWindow addInfoPopupWindow = null;
    private View addInfoPopupView = null;
    private ImageView btnAddInfoItemPopupClose , btnAddInfoItemPopupConfirm;
    private ListView addInfoListView;
    private RelativeLayout rootLayout;
    private LinearLayout popupRootLayout;
    private LinearLayout samePictureAsHomeLayout;
    private ImageView imgCheckBoxSamePicAsHome;

    private CustomSizeMeasureView sizeMeasureView;
    private int activityHeight = 0 , activityWidth = 0;

    private boolean isHomePublicLocked = false, isWorkPublicLocked = false;
    private boolean isSamePictureAsHome = false;

    private float ratio = 1.0f;

    private ActionSheet takePhotoActionSheet = null;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" , strNewUploadPhotoUrl = "";

    private String strHomeProfilePhotoUrl = "" , strWorkProfilePhotoUrl = "";

    private Uri uri;
    private String tempPhotoUriPath = "";

    private boolean wasWorkHavingProfile = true;
    private boolean isFromSingup = false;


    private View.OnClickListener snapPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            if(takePhotoActionSheet == null)
                takePhotoActionSheet = ActionSheet.createBuilder(UserProfileEditInfoActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo) ,
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo)
                                )
                        .setCancelableOnTouchOutside(true)
                        .setListener(UserProfileEditInfoActivity.this)
                        .show();
            else
                takePhotoActionSheet.show(getSupportFragmentManager(), "actionSheet");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_editinfo);
        this.wasWorkHavingProfile = false; //default value , mark as work has profile set

        if(savedInstanceState!= null)
        {
            this.isFromSingup = savedInstanceState.getBoolean("isFromSignup", false);
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            this.isFromSingup = bundle.getBoolean("isFromSignup" ,false);
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
        }

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        currentGroup = userInfo.getGroupInfoByGroupType(groupType);
        if(userInfo.getHome().getProfileImage().equals(userInfo.getWork().getProfileImage()))
            isSamePictureAsHome = true;
        else
            isSamePictureAsHome = false;

        UserUpdateVO workGroup = userInfo.getWork();
        if(workGroup == null || workGroup.getFields().size() == 0)
            wasWorkHavingProfile = false;
        else
        {
            List<UserProfileVO> fields = workGroup.getFields();
            String[] dontShowFields = { "foreground", "background",
                    "privilege", "abbr", "video" };
            for(int i=0;i<fields.size();i++)
            {
                UserProfileVO field = fields.get(i);
                String fieldPosition = field.getPosition();
                String fieldType = field.getFieldType();

                if(fieldPosition == null || fieldPosition.equalsIgnoreCase(""))
                    continue;

                if (ArrayUtils.contains(dontShowFields,
                        fieldType.toLowerCase())) {
                    continue;
                }
                String fieldValue = field.getValue();
                if(fieldValue != null && fieldValue.compareTo("") != 0)
                {
                    wasWorkHavingProfile = true;
                    break;
                }
            }
        }


        currIndex = groupType-1;

        imgLoader = MyApp.getInstance().getImageLoader();

        getUIObjects();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TYPE_PARAM , type);
        outState.putSerializable(USER_INFO_PARAM , userInfo);
        outState.putBoolean("isFromSignup", this.isFromSingup);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        type = savedInstanceState.getString(TYPE_PARAM);
        this.isFromSingup = savedInstanceState.getBoolean("isFromSignup", false);

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        currentGroup = userInfo.getGroupInfoByGroupType(groupType);
        currIndex = groupType - 1;

        if(userInfo.getHome().getProfileImage().equals(userInfo.getWork().getProfileImage()))
            isSamePictureAsHome = true;
        else
            isSamePictureAsHome = false;
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
        btnSharePublic = (ImageView)findViewById(R.id.btnSharePublic); btnSharePublic.setOnClickListener(this);
        addInfoListView = (ListView)addInfoPopupView.findViewById(R.id.infoList);

        textViewPlusProfile = (TextView)findViewById(R.id.textViewPlusProfile);
        textViewSnapshot = (TextView)findViewById(R.id.textViewSnapshot);

        //profile snapshot layout to take photo for user home
        profileSnapshotLayout = (LinearLayout)findViewById(R.id.profileSnapshotLayout); profileSnapshotLayout.setOnClickListener(snapPhotoClickListener);

        //check box layout to select the same picture as home
        samePictureAsHomeLayout = (LinearLayout)findViewById(R.id.samePictureAsHomeLayout); samePictureAsHomeLayout.setOnClickListener(this);
        imgCheckBoxSamePicAsHome = (ImageView)findViewById(R.id.imgCheckBoxSamePictureAsHome);


        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnHome = (ImageView)findViewById(R.id.btnHome); btnHome.setOnClickListener(this);
        btnWork = (ImageView)findViewById(R.id.btnWork); btnWork.setOnClickListener(this);
        btnAddInfoItem = (ImageView)findViewById(R.id.btnAddInfoItem); btnAddInfoItem.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);

        imgHomeProfilePhoto = (NetworkImageView)findViewById(R.id.imgHomeProfilePhoto);imgHomeProfilePhoto.setOnClickListener(snapPhotoClickListener);
        imgWorkProfilePhoto = (NetworkImageView)findViewById(R.id.imgWorkProfilePhoto);imgWorkProfilePhoto.setOnClickListener(snapPhotoClickListener);

        homeProfilePhotoLayout = (LinearLayout)findViewById(R.id.homeProfilePhotoLayout);
        workProfilePhotoLayout = (LinearLayout)findViewById(R.id.workProfilePhotoLayout);

        strHomeProfilePhotoUrl = userInfo.getHome().getProfileImage();
        imgHomeProfilePhoto.setDefaultImageResId(R.drawable.part_a_user_photo_placeholder_purple);
        imgHomeProfilePhoto.setImageUrl(strHomeProfilePhotoUrl, imgLoader);


        strWorkProfilePhotoUrl = userInfo.getWork().getProfileImage();
        imgWorkProfilePhoto.setDefaultImageResId(R.drawable.part_a_user_photo_placeholder_purple);
        imgWorkProfilePhoto.setImageUrl(strWorkProfilePhotoUrl, imgLoader);

        System.out.println("----Profile Edit Screen Home ProfilePhoto = " + userInfo.getHome().getProfileImage());
        System.out.println("----Profile Edit Screen Work ProfilePhoto = " + userInfo.getWork().getProfileImage());

        homeInfoListFragment = HomeWorkAddInfoFragment.newInstance("home" , userInfo.getHome());
        workInfoListFragment = HomeWorkAddInfoFragment.newInstance("work" , userInfo.getWork());

        //homeInfoListFragment.setIsEditable(true);
        //workInfoListFragment.setIsEditable(true);

        isHomePublicLocked = !userInfo.getHome().isPublic();
        isWorkPublicLocked = !userInfo.getWork().isPublic();

        updatePublicIcon();

        initViewPager();

        updateBottomButtons();

        udpateCheckbox();

        //check home info has other fields except the default fields , if has extra fields , then show delete button
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
        if(validFieldsCount > 4)//if it has added fields except the default 4 fields, then show edit button
        {
            btnDelete.setVisibility(View.VISIBLE);
        }
        else
        {
            btnDelete.setVisibility(View.INVISIBLE);
        }
    }

    private void updateDeleteButtonVisibility()
    {
        if(currentGroup == null) return;
        UserUpdateVO groupInfo = currentGroup;
        if(groupInfo == null) return;

        //default is to hide btnEdit
        if(currIndex == 0 && currentFragment.getCurrentInputableFieldsCount() > 4)//if it has added fields except the default 4 fields, then show edit button
        {
            btnDelete.setVisibility(View.VISIBLE);
        }
        else if(currIndex == 1 && currentFragment.getCurrentInputableFieldsCount()> 6)//if it has added fields except the default 6 fields, then show edit button
        {
            btnDelete.setVisibility(View.VISIBLE);
        }
        else
            btnDelete.setVisibility(View.GONE);
    }

    private void updateBottomButtons()
    {
        if(groupType == HOME_GROUP)
        {
            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btnhomeedit);
                btnWork.setImageResource(R.drawable.img_icon_work);
            } else {
                btnHome.setImageResource(R.drawable.img_home);
                btnWork.setImageResource(R.drawable.btnworkedit);
            }
            btnAddInfoItem.setImageResource(R.drawable.part_a_btn_add_info_purple);
        }
        else {
            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btnhomeedit);
                btnWork.setImageResource(R.drawable.img_icon_work);
            } else {
                btnHome.setImageResource(R.drawable.img_home);
                btnWork.setImageResource(R.drawable.btnworkedit);
            }
            btnAddInfoItem.setImageResource(R.drawable.part_a_btn_add_info_purple);
        }
    }

    private void udpateCheckbox()
    {
        if(currIndex == 0)
        {
            samePictureAsHomeLayout.setVisibility(View.GONE);
        }
        else
        {
            samePictureAsHomeLayout.setVisibility(View.VISIBLE);
            if(isSamePictureAsHome)
            {
                imgCheckBoxSamePicAsHome.setImageResource(R.drawable.share_profile_selected);
            }
            else
            {
                imgCheckBoxSamePicAsHome.setImageResource(R.drawable.share_profile_non_selected);
            }
        }
    }

    private void updatePublicIcon()
    {
        boolean isPublicLocked = currIndex == 0?isHomePublicLocked:isWorkPublicLocked;
        if(isPublicLocked)
        {
            switch(groupType) {
                case HOME_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_purple);
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
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_unlock_purple);
                    break;
                case WORK_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_unlock_purple);
                    break;
            }
        }
    }

    private void initViewPager() {
        if(groupType == HOME_GROUP)
            currIndex = 0;
        else
            currIndex = 1;
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(false);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        myPageListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(myPageListener);
        fragments.add(homeInfoListFragment);
        if((groupType == HOME_GROUP && wasWorkHavingProfile) || (groupType == WORK_GROUP))
            fragments.add(workInfoListFragment);

        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
        myPageListener.onPageSelected(currIndex);

        if(currIndex == 0)
        {
            textViewPlusProfile.setTextColor(0xff83d570);
            textViewSnapshot.setTextColor(0xff83d570);
        }
        else
        {
            textViewPlusProfile.setTextColor(0xff7e5785);
            textViewSnapshot.setTextColor(0xff7e5785);
        }
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
        addInfoPopupWindow.setFocusable(true);
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
                ArrayList<HomeWorkAddInfoFragment.InfoItem> infoList;
                if(currIndex == 0) {
                    infoList = homeInfoListFragment.getInfoList();
                }
                else
                    infoList = workInfoListFragment.getInfoList();


                ArrayList<AddInfoItem> itemList = new ArrayList<AddInfoItem>();
                for(int i=0;i<infoList.size();i++)
                {
                    if(!infoList.get(i).isVisible) {
                        if(currIndex == 0)//home
                        {
                            if(infoList.get(i).strInfoName.toLowerCase().equals("title") ||
                                infoList.get(i).strInfoName.toLowerCase().equals("company"))
                                continue;
                        }
                        itemList.add(new AddInfoItem(infoList.get(i).strInfoName));
                    }
                }
                addInfoListAdapter = new AddInfoListAdapter(UserProfileEditInfoActivity.this , itemList);
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
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnConfirm:
                hideKeyboard();

                UserUpdateVO homeInfo = null , workInfo = null;
                //homeInfo = homeInfoListFragment.saveGroupInfo(UserProfileEditInfoActivity.this , isHomePublicLocked , activityWidth , activityHeight , ratio , textBoundMeasureView , true);
                if(pageAdapter.getCount()>1) {
                    if (wasWorkHavingProfile) //if work profile was already exist and some essential fields are missed ,then show alert
                    {
                        //workInfo = workInfoListFragment.saveGroupInfo(UserProfileEditInfoActivity.this, isWorkPublicLocked, activityWidth, activityHeight, ratio, textBoundMeasureView, true);
                        if(workInfo == null || workInfo.getFields().size() <= 0)
                            return;
                    }
                    else {
                        if(workInfoListFragment.hasMoreThanOneInputedValues()) {
                            //workInfo = workInfoListFragment.saveGroupInfo(UserProfileEditInfoActivity.this, isWorkPublicLocked, activityWidth, activityHeight, ratio, textBoundMeasureView, true);
                            if(workInfo == null)
                                return;
                        }
                    }
                }
                if(homeInfo == null)
                    return;


                userInfo.setHome(homeInfo);
                userInfo.getHome().setPublic(!isHomePublicLocked);
                userInfo.getHome().setProfileImage(strHomeProfilePhotoUrl);

                if(wasWorkHavingProfile && workInfo == null )
                    return;


                if(workInfo!=null) {
                    userInfo.setWork(workInfo);
                }

                userInfo.getWork().setPublic(!isWorkPublicLocked);
                userInfo.getWork().setProfileImage(strWorkProfilePhotoUrl);

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("userInfo" , userInfo);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                UserProfileEditInfoActivity.this.finish();
                break;

            case R.id.btnHome:
                mPager.setCurrentItem(0);
                break;

            case R.id.btnWork:
                if(!isFromSingup || groupType != HOME_GROUP)
                    if(pageAdapter.getCount()>0)
                        mPager.setCurrentItem(1);
                break;

            case R.id.btnSharePublic:
                if(currIndex == 0)
                {
                    isHomePublicLocked = !isHomePublicLocked;
                }
                else
                {
                    isWorkPublicLocked = !isWorkPublicLocked;
                }
                updatePublicIcon();
                break;

            case R.id.btnAddInfoItem:
                showHidePopupView(addInfoPopupWindow, true);
                break;

            case R.id.btnDelete:
                if(currentFragment!= null) {
                    //currentFragment.deleteSelectedInfoItems();
                    updateDeleteButtonVisibility();
                }
                break;

            //add info item listview popup close
            case R.id.btnAddInfoPopupClose:
                showHidePopupView(addInfoPopupWindow, false);
                break;

            //add info item listview popup confirm
            case R.id.btnAddInfoPopupConfirm:
                HomeWorkAddInfoFragment fragment;
                if(currIndex == 0)
                    fragment = homeInfoListFragment;
                else
                    fragment = workInfoListFragment;

                for(int i=0;i<addInfoListAdapter.getCount();i++)
                {
                    AddInfoItem item = (AddInfoItem) addInfoListAdapter.getItem(i);
                    if(item.isSelected)
                    {
                        fragment.addNewInfoItem(item.strInfoName);
                    }
                }
                fragment.updateInfoListViews(false);
                updateDeleteButtonVisibility();
                showHidePopupView(addInfoPopupWindow, false);
                break;

            //select if use the same profile picture as home
            case R.id.samePictureAsHomeLayout:
                if(isSamePictureAsHome == true)
                {
                    if(!userInfo.getWork().getProfileImage().equals("") && userInfo.getWork().getProfileImage().compareTo(userInfo.getHome().getProfileImage()) != 0) {
                        //remove old work profile photo
                        TradeCard.removeProfileImage(2, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    userInfo.getWork().setProfileImage("");
                                    strWorkProfilePhotoUrl = "";
                                    isSamePictureAsHome = false;
                                    udpateCheckbox();
                                    imgWorkProfilePhoto.refreshOriginalBitmap();
                                    imgWorkProfilePhoto.setImageUrl(strWorkProfilePhotoUrl, imgLoader);
                                    imgWorkProfilePhoto.invalidate();
                                }
                            }
                        });
                    }
                    else
                    {
                        userInfo.getWork().setProfileImage("");
                        strWorkProfilePhotoUrl = "";
                        isSamePictureAsHome = false;
                        udpateCheckbox();
                        imgWorkProfilePhoto.refreshOriginalBitmap();
                        imgWorkProfilePhoto.setImageUrl(strWorkProfilePhotoUrl, imgLoader);
                        imgWorkProfilePhoto.invalidate();
                    }
                    return;
                }
                else
                {
                    userInfo.getWork().setProfileImage(userInfo.getHome().getProfileImage());//set the work's profile image with the home's profile image
                    strWorkProfilePhotoUrl = strHomeProfilePhotoUrl;
                    imgWorkProfilePhoto.refreshOriginalBitmap();
                    imgWorkProfilePhoto.setImageUrl(strWorkProfilePhotoUrl, imgLoader);
                    imgWorkProfilePhoto.invalidate();
                }
                isSamePictureAsHome = !isSamePictureAsHome;
                udpateCheckbox();
                break;
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
                        if(uri != null) {
                            Cursor cursor = null;
                            File myFile = new File(uri.getPath());

                            try {
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                                cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
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
                                if (strTempPhotoPath == null) {
                                    strTempPhotoPath = "";
                                    return;
                                }
                                if (!strTempPhotoPath.contains("file://"))
                                    strTempPhotoPath = "file://" + strTempPhotoPath;

                                System.out.println("-----Photo Path= " + strTempPhotoPath + "----");
                                setUserProfilePhoto();
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                                if(cursor != null)
                                    cursor.close();
                                strTempPhotoPath = "";
                            }
                        }

                    }
                    break;
            }
        }
        else
        {
            strTempPhotoPath = "";
            strProfileImagePath = "";
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

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//take a photo
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(System.currentTimeMillis()) + ".jpg"));
            tempPhotoUriPath = uri.getPath();
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            UserProfileEditInfoActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            UserProfileEditInfoActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
        }
        else if(index == 2) //remove photo
        {
            if(!currentGroup.getProfileImage().equals(""))
            {
                int type = 1;//home
                if(currIndex == 0)
                    type = 1; ///home
                else
                    type = 2; //work
                final int imageGroupType = type;
                TradeCard.removeProfileImage(imageGroupType, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";

                            if(imageGroupType == 1) {
                                strHomeProfilePhotoUrl = "";
                                imgHomeProfilePhoto.refreshOriginalBitmap();
                                imgHomeProfilePhoto.setImageUrl(strProfileImagePath, imgLoader);
                                imgHomeProfilePhoto.invalidate();
                            }
                            else {
                                strWorkProfilePhotoUrl = "";
                                imgWorkProfilePhoto.refreshOriginalBitmap();
                                imgWorkProfilePhoto.setImageUrl(strProfileImagePath, imgLoader);
                                imgWorkProfilePhoto.invalidate();
                            }
                        }
                    }
                });
            }
            else
            {
                Uitils.alert(UserProfileEditInfoActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
            }
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
                int type = 1;//home
                if(currIndex == 0)
                    type = 1; ///home
                else
                    type = 2; //work
                TradeCard.uploadProfileImage(type, new File(strUploadPhotoPath), new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {

                            JSONObject data = response.getData();
                            try {
                                String photoUrl = data.getString("profile_image");
                                System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                strProfileImagePath = strTempPhotoPath;
                                strNewUploadPhotoUrl = photoUrl;
                                if(currIndex == 0)//home
                                {
                                    userInfo.getHome().setProfileImage(photoUrl);
                                    strHomeProfilePhotoUrl = photoUrl;

                                    imgHomeProfilePhoto.refreshOriginalBitmap();
                                    imgHomeProfilePhoto.setImageUrl("file://" + strUploadPhotoPath, imgLoader);
                                    imgHomeProfilePhoto.invalidate();

                                }
                                else //work
                                {
                                    userInfo.getWork().setProfileImage(photoUrl);
                                    strWorkProfilePhotoUrl = photoUrl;
                                    imgWorkProfilePhoto.refreshOriginalBitmap();
                                    imgWorkProfilePhoto.setImageUrl("file://"+strUploadPhotoPath, imgLoader);
                                    imgWorkProfilePhoto.invalidate();
                                }

                                strTempPhotoPath = "";

                            } catch (Exception e) {
                                e.printStackTrace();
                                strTempPhotoPath = "";
                            }
                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            //imgViewAvatar.refreshOriginalBitmap();
                            //imgViewAvatar.setImageUrl(strProfileImagePath, imgLoader);
                            //imgViewAvatar.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(UserProfileEditInfoActivity.this, R.string.str_err_upload_photo, null);
                        }
                    }
                }, true);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }


    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> infos;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> infos) {
            super(fm);
            this.infos = infos;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Logger.debug("position Destroy" + position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int position) {
            Fragment ff = (Fragment) super.instantiateItem(arg0, position);
            return ff;
        }

        @Override
        public Fragment getItem(int position) {

            return infos.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            currIndex = position;
            if(groupType == HOME_GROUP)
            {
                if (currIndex == 0) {
                    btnHome.setImageResource(R.drawable.btnhomeedit);
                    btnWork.setImageResource(R.drawable.img_icon_work);
                    homeProfilePhotoLayout.setVisibility(View.VISIBLE);
                    workProfilePhotoLayout.setVisibility(View.INVISIBLE);
                } else {
                    btnHome.setImageResource(R.drawable.img_home);
                    btnWork.setImageResource(R.drawable.btnworkedit);
                    homeProfilePhotoLayout.setVisibility(View.INVISIBLE);
                    workProfilePhotoLayout.setVisibility(View.VISIBLE);
                }

            }
            else {
                if (currIndex == 0) {
                    btnHome.setImageResource(R.drawable.btnhomeedit);
                    btnWork.setImageResource(R.drawable.img_icon_work);
                    homeProfilePhotoLayout.setVisibility(View.VISIBLE);
                    workProfilePhotoLayout.setVisibility(View.INVISIBLE);
                } else {
                    btnHome.setImageResource(R.drawable.img_home);
                    btnWork.setImageResource(R.drawable.btnworkedit);
                    homeProfilePhotoLayout.setVisibility(View.INVISIBLE);
                    workProfilePhotoLayout.setVisibility(View.VISIBLE);
                }
            }

            if(currIndex == 0)
            {
                textViewPlusProfile.setTextColor(0xff83d570);
                textViewSnapshot.setTextColor(0xff83d570);
            }
            else
            {
                textViewPlusProfile.setTextColor(0xff7e5785);
                textViewSnapshot.setTextColor(0xff7e5785);
            }

            currentGroup = currIndex==0? userInfo.getHome(): userInfo.getWork();

            currentFragment = currIndex==0?homeInfoListFragment:workInfoListFragment;

            updateDeleteButtonVisibility();
            updatePublicIcon();
            udpateCheckbox();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
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
