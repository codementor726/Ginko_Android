package com.ginko.activity.user;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.common.VideoSetActivity;
import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.HomeWorkAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.setup.HomeWorkAddInfoActivity;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.TcVideoVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;
import com.videophotofilter.android.com.RecordFilterCameraActivity;

import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class PersonalProfileEditActivity extends MyBaseFragmentActivity implements
        View.OnClickListener ,
        ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener,
        ActionSheet.ActionSheetListener{


    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int TAKE_VIDEO_FROM_CAMERA = 11;
    private final int FILTER_VIDEO = 22;
    private final int FILTER_PHOTO = 6;

    /*  UI Objects */
    private LinearLayout activityRootView, scrollLayout;
    private TextView textViewTitle;
    private Button btnNext;
    private ProfileFieldAddOverlayView addFieldOverlayView;
    private ImageView imgDimBackground;
    private ImageView btnAddProfileField;
    private ImageButton btnRemoveProfile , btnLockProfile;
    private NetworkImageView imgPersonalProfilePhoto;
    private CustomNetworkImageView imgWallpaper , imgProfileVideo;
    private RelativeLayout videoLayout;
    private ImageView btnPlayVideoButton;
    private EditText edtFullName;
    private ImageView btnClearSearch;
    private RelativeLayout headerlayout;

    /* Variables */
    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";

    private final int GROUP_HOME = 1;
    private final int GROUP_WORK = 2;

    private int groupType = GROUP_HOME;
    private String type;
    private UserWholeProfileVO userInfo; //whole user profile info
    private UserUpdateVO groupInfo;//home or work profile

    private boolean isPublicLocked = true;
    private boolean isKeyboardVisible = false;
    private boolean isFromGallery = false;

    private HomeWorkAddInfoFragment infoListFragment;

    private ImageLoader imgLoader;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" ;

    private Uri uri;
    private String tempPhotoUriPath = "";

    private String strProfilePhotoUrl = "";

    private ActionSheet takeProfilePhotoActionSheet = null;
    private ActionSheet takeWallpaperPhotoActionSheet = null;
    private ActionSheet takeVideoActionSheet = null;

    private boolean isTakingProfilePhoto = true;
    private boolean isProfileVideo = false;

    private UserUpdateVO newGroupInfo = null;

    private boolean isCreatingNewProfile = false;
    private boolean isRegistered         = false;

    private String mTempDirectory = RuntimeContext.getAppDataFolder("temp");
    private int m_orientHeight = 0;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;
            if(groupInfo == null) return;
            //if(takeProfilePhotoActionSheet == null)
            if((groupInfo.getProfileImage().contains("no-face") || groupInfo.getProfileImage().equals(""))) {
                takeProfilePhotoActionSheet = ActionSheet.createBuilder(PersonalProfileEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(PersonalProfileEditActivity.this)
                        .show();
            }
            else
            {
                takeProfilePhotoActionSheet = ActionSheet.createBuilder(PersonalProfileEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(PersonalProfileEditActivity.this)
                        .show();
            }
           // else
            //    takeProfilePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    private View.OnClickListener snapWallpaperPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;
            if(groupInfo == null) return;
            //if(takeWallpaperPhotoActionSheet == null)
            if(groupInfo.getWallpapaerImage() == null) {
                takeWallpaperPhotoActionSheet = ActionSheet.createBuilder(PersonalProfileEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.personal_profile_take_wallpaper_from_library))
                        .setCancelableOnTouchOutside(true)
                        .setListener(PersonalProfileEditActivity.this)
                        .show();
            }
            else
            {
                takeWallpaperPhotoActionSheet = ActionSheet.createBuilder(PersonalProfileEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.personal_profile_take_wallpaper_from_library),
                                getResources().getString(R.string.personal_profile_remove_wallpaper_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(PersonalProfileEditActivity.this)
                        .show();
            }
            //else
            //    takeWallpaperPhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    private View.OnClickListener snapVideoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;
            if(groupInfo == null) return;
            //if(takeWallpaperPhotoActionSheet == null)
            if(groupInfo.getVideo() == null) {
                deleteAllTempFiles();  //delte all temp video and audio files.
                //Toast.makeText(PersonalProfileEditActivity.this, "Coming soon....", Toast.LENGTH_LONG).show();

                Intent getFilteredVideo = new Intent(PersonalProfileEditActivity.this, VideoSetActivity.class);
                getFilteredVideo.putExtra("typeId", "personalInfo");
                getFilteredVideo.putExtra("isHome", groupType);
                getFilteredVideo.putExtra("isNewEntity", !isRegistered);
                startActivityForResult(getFilteredVideo, TAKE_VIDEO_FROM_CAMERA);
            }
            else
            {
                isProfileVideo = true;
                takeVideoActionSheet = ActionSheet.createBuilder(PersonalProfileEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_remove_video),
                                getResources().getString(R.string.personal_profile_play_video))
                        .setCancelableOnTouchOutside(true)
                        .setListener(PersonalProfileEditActivity.this)
                        .show();
            }
            //else
            //    takeWallpaperPhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_profile);

        if(savedInstanceState!= null)
        {
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            isRegistered = bundle.getBoolean("isRegister");
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
        }

        if(type.equals(GROUP_TYPE_HOME)) {
            groupType = GROUP_HOME;
            groupInfo = userInfo.getHome();
        }
        else {
            groupType = GROUP_WORK;
            groupInfo = userInfo.getWork();
        }

        if(groupInfo == null)
        {
            finish();
            return;
        }

        isPublicLocked = !groupInfo.isPublic();

        getUIObjects();

        //initialize keyboard
        //hideKeyboard();
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TYPE_PARAM, type);
        outState.putSerializable(USER_INFO_PARAM, userInfo);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnRemoveProfile = (ImageButton)findViewById(R.id.btnRemoveProfile); btnRemoveProfile.setOnClickListener(this);

        //if user only has one profile home or work, then hide remove profile button
        if((groupType == GROUP_HOME && (userInfo.getWork() == null || userInfo.getWork().getFields() == null || userInfo.getWork().getInputableFieldsCount() < 1)) ||
                (groupType == GROUP_WORK && (userInfo.getHome() == null || userInfo.getHome().getFields() == null ||userInfo.getHome().getInputableFieldsCount() < 1))     )
        {
            btnRemoveProfile.setVisibility(View.GONE);
            isCreatingNewProfile = true;
        }
        else
        {
            if(groupInfo == null || groupInfo.getFields() == null  || groupInfo.getInputableFieldsCount() <1)
            {
                isCreatingNewProfile = true;
            }
            else {
                btnRemoveProfile.setVisibility(View.VISIBLE);
                isCreatingNewProfile = false;
            }
        }
        /*Add by lee GAD-823

        if (groupType == GROUP_HOME)
            btnRemoveProfile.setImageResource(R.drawable.remove_home_profile_button);
        else
            btnRemoveProfile.setImageResource(R.drawable.remove_work_profile_button);
        btnRemoveProfile.invalidate();
        */
        btnNext = (Button)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);

        addFieldOverlayView = (ProfileFieldAddOverlayView)findViewById(R.id.addFieldOverlayView); addFieldOverlayView.setOnClickListener(this);
        addFieldOverlayView.setOnProfileFieldItemsChangeListener(this); addFieldOverlayView.setVisibility(View.GONE);

        btnAddProfileField = (ImageView)findViewById(R.id.btnAddFieldInfoItem); btnAddProfileField.setOnClickListener(this);
        btnLockProfile = (ImageButton)findViewById(R.id.btnLockProfile);       btnLockProfile.setOnClickListener(this);

        imgDimBackground = (ImageView)findViewById(R.id.imgDimBackground);
        imgDimBackground.setVisibility(View.GONE);

        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        if(groupType == GROUP_HOME)
        {
            textViewTitle.setText(getResources().getString(R.string.str_personal));
        }
        else
        {
            textViewTitle.setText(getResources().getString(R.string.str_profile_work));
        }

        activityRootView = (LinearLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        if (infoListFragment != null) {
                            infoListFragment.setKeyboardVisibilty(true);
                            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        if (infoListFragment != null)
                            infoListFragment.setKeyboardVisibilty(false);
                    }
                }
            }
        });

        scrollLayout = (LinearLayout)findViewById(R.id.scrollLayout);
        scrollLayout.setFocusable(true);
        scrollLayout.setFocusableInTouchMode(true);
        scrollLayout.requestFocus();

        scrollLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        imgPersonalProfilePhoto = (NetworkImageView)findViewById(R.id.imgPersonalProfilePhoto);
        imgPersonalProfilePhoto.setDefaultImageResId(R.drawable.add_personal_profile_photo_bg);
        if(groupInfo != null) {
            if(!groupInfo.getProfileImage().equals("http://image.ginko.mobi/Photos/no-face.png")) {
                //don't show default profile image from server
                imgPersonalProfilePhoto.setImageUrl(groupInfo.getProfileImage(), imgLoader);
            }
        }
        imgPersonalProfilePhoto.setOnClickListener(snapProfilePhotoClickListener);

        imgWallpaper = (CustomNetworkImageView)findViewById(R.id.imgWallpaper);
        imgWallpaper.setDefaultImageResId(R.drawable.add_wallpaper);
        TcImageVO wallpaperImage = null;
        if((wallpaperImage = groupInfo.getWallpapaerImage()) !=null) {
            imgWallpaper.setImageUrl(wallpaperImage.getUrl() , imgLoader);
        }
        imgWallpaper.invalidate();
        imgWallpaper.setOnClickListener(snapWallpaperPhotoClickListener);

        videoLayout = (RelativeLayout)findViewById(R.id.videoLayout); videoLayout.setClickable(true);
        btnPlayVideoButton = (ImageView)findViewById(R.id.imgVideoPlayButton);
        /* modify by lee.
        btnPlayVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupInfo == null)
                    return;
                if (groupInfo.getVideo() == null || groupInfo.getVideo() == null || groupInfo.getVideo().getVideo_url().equals(""))
                    return;
                try {
                    Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
                    videoPlayIntent.setDataAndType(Uri.parse(groupInfo.getVideo().getVideo_url()), "video*//*");
                    PersonalProfileEditActivity.this.startActivity(videoPlayIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(PersonalProfileEditActivity.this , "Profile video coming soon." , Toast.LENGTH_LONG).show();

            }
        });
        */
        imgProfileVideo = (CustomNetworkImageView)findViewById(R.id.imgProfileVideo);
        imgProfileVideo.setDefaultImageResId(R.drawable.add_profile_video);
        imgProfileVideo.setOnClickListener(snapVideoClickListener);

        if(groupInfo != null && groupInfo.getVideo()!= null && !groupInfo.getVideo().getThumbUrl().equals(""))
        {
            imgProfileVideo.setBackground(null);
            imgProfileVideo.setImageUrl(groupInfo.getVideo().getThumbUrl(), imgLoader);
            imgProfileVideo.invalidate();

            btnPlayVideoButton.setVisibility(View.VISIBLE);
        }
        else
        {
            btnPlayVideoButton.setVisibility(View.GONE);
            imgProfileVideo.invalidate();
        }

        edtFullName = (EditText)findViewById(R.id.edtFullName);
        String fullName = groupInfo.getProfileUserName();
        if(fullName.trim().equals(""))
        {
            edtFullName.setText(Uitils.getUserFullname(getApplicationContext()));
        }
        else
            edtFullName.setText(fullName);

        edtFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtFullName.hasFocus() && s.length() > 0)
                    btnClearSearch.setVisibility(View.VISIBLE);
                else
                    btnClearSearch.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtFullName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtFullName.getWindowToken(), 0);

                    btnClearSearch.setVisibility(View.GONE);
                    scrollLayout.requestFocus();

                    return true;
                }
                return false;
            }
        });

        edtFullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (edtFullName.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    edtFullName.setCursorVisible(true);

                } else {
                    btnClearSearch.setVisibility(View.GONE);
                    edtFullName.setCursorVisible(false);
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtFullName.setText("");
                btnClearSearch.setVisibility(View.GONE);
            }
        });
        btnClearSearch.setVisibility(View.GONE);

        /**
         * Add by Lee GAD-823
         */
        headerlayout = (RelativeLayout) findViewById(R.id.headerlayout);
        if(isRegistered)
        {
            if (groupType == GROUP_HOME)
                btnRemoveProfile.setImageResource(R.drawable.remove_home_profile_button_purple);
            else
                btnRemoveProfile.setImageResource(R.drawable.remove_work_profile_button_purple);
            btnRemoveProfile.invalidate();

            headerlayout.setBackgroundResource(R.color.green_top_titlebar_color);
            btnNext.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
            textViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
        }
        else {
            if (groupType == GROUP_HOME)
                btnRemoveProfile.setImageResource(R.drawable.remove_home_profile_button);
            else
                btnRemoveProfile.setImageResource(R.drawable.remove_work_profile_button);
            btnRemoveProfile.invalidate();
        }
        ///
        initInfoItemFragment();

        updateLockButton();
    }

    private void isShownKeyboard() {
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int curheight= rectgle.bottom;
        if(m_orientHeight == curheight)
            isKeyboardVisible = false;
        else
            isKeyboardVisible = true;
    }

    private void initInfoItemFragment()
    {
        String strFullName = userInfo.getFirstName()==null?"": userInfo.getFirstName();

        if(userInfo.getMiddleName()!= null && !userInfo.getMiddleName().equals(""))
            strFullName = strFullName +" " +userInfo.getMiddleName();

        if(userInfo.getLastName()!= null && !userInfo.getLastName().equals(""))
            strFullName = strFullName + " " + userInfo.getLastName();

        infoListFragment = HomeWorkAddInfoFragment.newInstance(type , groupInfo , strFullName);
        infoListFragment.setOnProfileFieldItemsChangeListener(this);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();
    }

    private void updateLockButton()
    {
        if(isPublicLocked)
        {
            btnLockProfile.setImageResource(R.drawable.personal_profile_locked); btnLockProfile.invalidate();
        }
        else
        {
            btnLockProfile.setImageResource(R.drawable.personal_profile_unlocked); btnLockProfile.invalidate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isKeyboardVisible)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShownKeyboard();

        if(!isKeyboardVisible)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        MyApp.getInstance().hideKeyboard(activityRootView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(addFieldOverlayView.getVisibility() == View.VISIBLE)
        {
            imgDimBackground.setVisibility(View.GONE);
            addFieldOverlayView.hideView();
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            return;
        }
        if(isRegistered && groupInfo.getFields().size() == 0)
            return;
        else
        {
            //saveProfile();
            super.onBackPressed();
        }
    }

    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);

        scrollLayout.requestFocus();
    }

    private boolean checkSameGroupInfo(UserUpdateVO leftInfo, UserUpdateVO rightInfo)
    {
        if (leftInfo.getFields().size() != rightInfo.getFields().size())
            return true;

        String strLeftValue, strRightValue;
        String strLeftName, strRightName;
        boolean isChanged = false;

        for (int i=0; i<leftInfo.getFields().size(); i++)
        {
            strLeftValue = leftInfo.getFields().get(i).getValue();
            strRightValue = rightInfo.getFields().get(i).getValue();
            strLeftName = leftInfo.getFields().get(i).getFieldName();
            strRightName = rightInfo.getFields().get(i).getFieldName();

            if (strLeftName.equals(strRightName) && strLeftValue.equals(strRightValue))
                continue;
            isChanged = true;
            break;
        }

        return isChanged;
    }
    private void saveProfile()
    {
        if(edtFullName.getText().toString().trim().equals(""))
        {
            MyApp.getInstance().showSimpleAlertDiloag(PersonalProfileEditActivity.this, R.string.str_alert_for_input_name_item , null);
            return;
        }
        newGroupInfo = null;
        if(infoListFragment != null)
            newGroupInfo = infoListFragment.saveGroupInfo(PersonalProfileEditActivity.this , isPublicLocked , true);

        if(newGroupInfo == null)
        {
            return;
        }

        {
            if(newGroupInfo.getFields() == null)
                newGroupInfo.setFields(new ArrayList<UserProfileVO>());
            UserProfileVO nameField = new UserProfileVO();

            for (int j = 0; j< groupInfo.getFields().size(); j++)
            {
                String strName = groupInfo.getFields().get(j).getFieldName().trim();
                String strType = groupInfo.getFields().get(j).getFieldType().trim();
                if (strName.equalsIgnoreCase("name"))
                    nameField.setId(groupInfo.getFields().get(j).getId());
            }

            nameField.setFieldName("Name");
            nameField.setFieldType("name");
            //nameField.setFont("");
            //nameField.setColor("");
            nameField.setPosition("");
            nameField.setValue(edtFullName.getText().toString().trim());
            newGroupInfo.getFields().add(nameField);
        }

        boolean isChanged = checkSameGroupInfo(groupInfo, newGroupInfo);
        if (isChanged)
            groupInfo.setFields(newGroupInfo.getFields());

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("userInfo" , userInfo);
        bundle.putSerializable("type", type);
        intent.putExtras(bundle);
        intent.putExtra("isChanged", isChanged);
        setResult(RESULT_OK, intent);
        PersonalProfileEditActivity.this.finish();

        /*DialogInterface.OnClickListener updateConfirmDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

                        dialog.dismiss();
                    {
                        if(newGroupInfo.getFields() == null)
                            newGroupInfo.setFields(new ArrayList<UserProfileVO>());
                        UserProfileVO nameField = new UserProfileVO();
                        nameField.setFieldName("Name");
                        nameField.setFieldType("name");
                        nameField.setFont("");
                        nameField.setColor("");
                        nameField.setPosition("");
                        nameField.setValue(edtFullName.getText().toString().trim());
                        newGroupInfo.getFields().add(nameField);
                    }

                    groupInfo.setFields(newGroupInfo.getFields());

                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo" , userInfo);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    PersonalProfileEditActivity.this.finish();
                    break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.dismiss();
                        setResult(RESULT_CANCELED);
                        PersonalProfileEditActivity.this.finish();
                        break;
                }
            }
        };
        AlertDialog.Builder updateConfirmDialogBuilder = new AlertDialog.Builder(PersonalProfileEditActivity.this);
        updateConfirmDialogBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_make_changes_to_this_personal_profile))
                .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), updateConfirmDialogClickListener)
                .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), updateConfirmDialogClickListener).show();*/
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnNext:
                saveProfile();
                break;

            //add profile field items
            case R.id.btnAddFieldInfoItem:
                if(infoListFragment == null) return;

                hideKeyboard();
                addFieldOverlayView.setProfileFieldItems(type, infoListFragment.getCurrentVisibleInfoItems());

                if(addFieldOverlayView.getVisibility() == View.GONE)
                {
                    imgDimBackground.setVisibility(View.VISIBLE);
                    addFieldOverlayView.showView();
                    btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                }
                else
                {
                    imgDimBackground.setVisibility(View.GONE);
                    addFieldOverlayView.hideView();
                    btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
                }
                break;

            //remove profile
            case R.id.btnRemoveProfile:
                String strConfirmMsg = getResources().getString(R.string.str_confirm_delete_profile);
                if(groupType == GROUP_HOME)
                    strConfirmMsg = getResources().getString(R.string.str_confirm_delete_personal_profile);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                builder.setMessage(strConfirmMsg);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        UserInfoRequest.removeProfile(type, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    //reset the work info

                                    groupInfo.setFields(new ArrayList<UserProfileVO>());
                                    groupInfo.setProfileImage("");
                                    groupInfo.setVideo(null);
                                    groupInfo.setImages(new ArrayList<TcImageVO>());
                                }
                                //select home info
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("userInfo" , userInfo);
                                bundle.putSerializable("type", type);
                                intent.putExtras(bundle);
                                setResult(RESULT_OK, intent);
                                PersonalProfileEditActivity.this.finish();

                            }
                        });
                        /*groupInfo.setFields(new ArrayList<UserProfileVO>());
                        groupInfo.setProfileImage("");
                        groupInfo.setVideo(null);
                        groupInfo.setImages(new ArrayList<TcImageVO>());
                        UserInfoRequest.setUserInfo(groupInfo, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                {
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("userInfo" , userInfo);
                                    intent.putExtras(bundle);
                                    setResult(RESULT_OK, intent);
                                    PersonalProfileEditActivity.this.finish();
                                }
                            }
                        });*/
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            //lock & unlock profile
            case R.id.btnLockProfile:
                isPublicLocked = !isPublicLocked;
                updateLockButton();
                if(isPublicLocked) {
                    final Toast toast = Toast.makeText(PersonalProfileEditActivity.this, "Profile is private.", Toast.LENGTH_SHORT);
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 300);
                }
                else
                {
                    final Toast toast = Toast.makeText(PersonalProfileEditActivity.this, "Profile is public.", Toast.LENGTH_SHORT);
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 300);
                }
                break;
        }
    }

    private void setWallpaperPhoto()
    {
        if(!strTempPhotoPath.startsWith("file://"))
            strTempPhotoPath = "file://"+strTempPhotoPath;
        File photoFile = null;
        try {
            photoFile = new File(new URI(strTempPhotoPath.replaceAll(" ", "%20")));

            if(!photoFile.exists())
                return;

            //String zippedPhotoFile = ImageScalingUtilities.decodeFile(photoFile.getAbsolutePath(), 300, 300);
            //if(zippedPhotoFile.equalsIgnoreCase(""))
            //    return;

            //photoFile = new File(zippedPhotoFile);

            final String strUploadPhotoPath = photoFile.getAbsolutePath();

            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {
                TradeCard.putMultipleImages(groupType, null, photoFile, new ResponseCallBack<List<JSONObject>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<JSONObject>> response) {
                        if (response.isSuccess()) {

                            List<JSONObject> imageArray = response.getData();
                            List<TcImageVO> images = new ArrayList<TcImageVO>();
                            for (int i = 0; i < imageArray.size(); i++) {
                                try {
                                    images.add(JsonConverter.json2Object(imageArray.get(i), TcImageVO.class));
                                } catch (JsonConvertException e) {
                                    e.printStackTrace();
                                }
                            }

                            groupInfo.setImages(images);

                            strProfileImagePath = strTempPhotoPath;
                            strProfilePhotoUrl = groupInfo.getWallpapaerImage().getUrl();
                            imgWallpaper.refreshOriginalBitmap();
                            if(strProfileImagePath.startsWith("file://"))
                                imgWallpaper.setImageUrl(strProfileImagePath, imgLoader);
                            else
                                imgWallpaper.setImageUrl("file://"+strProfileImagePath, imgLoader);
                            imgWallpaper.setAdustImageAspect(true);
                            imgWallpaper.invalidate();
                            strTempPhotoPath = "";

                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgWallpaper.refreshOriginalBitmap();
                            imgWallpaper.setImageUrl(strProfileImagePath, imgLoader);
                            imgWallpaper.setAdustImageAspect(true);
                            imgWallpaper.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(PersonalProfileEditActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                        }
                    }
                });
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void setUserProfilePhoto()
    {
        if(!strTempPhotoPath.startsWith("file://"))
            strTempPhotoPath = "file://"+strTempPhotoPath;
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
                                System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                strProfileImagePath = strTempPhotoPath;
                                strProfilePhotoUrl = photoUrl;
                                groupInfo.setProfileImage(photoUrl);
                                imgPersonalProfilePhoto.refreshOriginalBitmap();
                                if (strProfileImagePath.startsWith("file://"))
                                    imgPersonalProfilePhoto.setImageUrl(strProfileImagePath, imgLoader);
                                else
                                    imgPersonalProfilePhoto.setImageUrl("file://" + strProfileImagePath, imgLoader);

                                imgPersonalProfilePhoto.invalidate();
                                strTempPhotoPath = "";

                            } catch (Exception e) {
                                e.printStackTrace();
                                strTempPhotoPath = "";
                            }
                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgPersonalProfilePhoto.refreshOriginalBitmap();
                            imgPersonalProfilePhoto.setImageUrl(strProfileImagePath, imgLoader);
                            imgPersonalProfilePhoto.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(PersonalProfileEditActivity.this, R.string.str_err_upload_photo, null);
                        }
                    }
                }, true);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    private void goToFilterScreen()
    {
        Intent intent = new Intent(PersonalProfileEditActivity.this , PersonalProfilePhotoFilterActivity.class);
        intent.putExtra("imagePath", strTempPhotoPath);
        File filteredImagePath = null;
        if(isTakingProfilePhoto) {
            filteredImagePath = new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(type+".jpg"));
        }
        else
        {
            filteredImagePath = new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(type+"_wallpaper.jpg"));
        }
        try {
            if (filteredImagePath.exists()) {
                filteredImagePath.delete();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        strTempPhotoPath = filteredImagePath.getAbsolutePath();
        intent.putExtra("saveImagePath", strTempPhotoPath);
        intent.putExtra("groupType", type);
        if(isTakingProfilePhoto)
        {
            intent.putExtra("isCircleCrop" , true);
            intent.putExtra("aspect_x", 1);
            intent.putExtra("aspect_y", 1);
        }
        else {
            intent.putExtra("aspect_x", 10);
            intent.putExtra("aspect_y", 4);
        }
        startActivityForResult(intent, FILTER_PHOTO);
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

                    System.out.println("-----Photo Path= " + strTempPhotoPath + "----");

                    goToFilterScreen();

                    break;
                case TAKE_PHOTO_FROM_GALLERY:
                    if(data!=null)
                    {
                        uri = data.getData();
                        File myFile = new File(uri.getPath());
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null, null);
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
                        if (!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        System.out.println("-----Photo Path= " + strTempPhotoPath + "----");

                        goToFilterScreen();

                        /*
                        if(isTakingProfilePhoto)
                            setUserProfilePhoto();
                        else
                            setWallpaperPhoto();*/
                    }
                    break;

                case FILTER_PHOTO:
                    if(isTakingProfilePhoto)
                        setUserProfilePhoto();
                    else
                        setWallpaperPhoto();
                    break;

                case TAKE_VIDEO_FROM_CAMERA:
                    final String videoFilePath = data.getStringExtra("strMoviePath");
                    final String videoThumbFilePath = data.getStringExtra("strThumbPath");
                    isFromGallery = data.getBooleanExtra("isFromGallery", false);
                    boolean isHistory = data.getBooleanExtra("isHistory", false);

                    if(isHistory)
                    {
                        TcVideoVO thumb_rul = new TcVideoVO();
                        thumb_rul.setVideo_url(videoFilePath);
                        thumb_rul.setThumbUrl(videoThumbFilePath);

                        groupInfo.setVideo(thumb_rul);

                        imgProfileVideo.setBackground(null);
                        imgProfileVideo.setImageUrl(thumb_rul.getThumbUrl(), imgLoader);
                        imgProfileVideo.invalidate();

                        btnPlayVideoButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        if (videoFilePath != null || videoFilePath.equals("") == false) {
                            TradeCard.uploadVideo(groupType, new File(videoFilePath), new File(videoThumbFilePath), new ResponseCallBack<TcVideoVO>() {
                                @Override
                                public void onCompleted(JsonResponse<TcVideoVO> response) {
                                    if (response.isSuccess()) {
                                        groupInfo.setVideo(response.getData());

                                        imgProfileVideo.setBackground(null);
                                        imgProfileVideo.setImageUrl(groupInfo.getVideo().getThumbUrl(), imgLoader);
                                        imgProfileVideo.invalidate();

                                        btnPlayVideoButton.setVisibility(View.VISIBLE);
                                    } else {
                                        MyApp.getInstance().showSimpleAlertDiloag(PersonalProfileEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
                                    }
                                }
                            });
                        }
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
    public void onAddedNewProfileField(String fieldName) {
        if(infoListFragment != null) {
            infoListFragment.addNewInfoItem(fieldName);
            addFieldOverlayView.refreshView();
            boolean isAllFieldAdded = addFieldOverlayView.isAllFieldAdded();
            if(isAllFieldAdded)
            {
                imgDimBackground.setVisibility(View.GONE);
                addFieldOverlayView.hideView();
                btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            }
        }
    }

    @Override
    public void onRemovedProfileField(String fieldName) {
        if(infoListFragment != null)
            infoListFragment.removeInfoItem(fieldName);
    }

    @Override
    public void onEditTextWatcher() {

    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {

        if (index == 0)//take a photo
        {
            if(isProfileVideo){
                if(groupInfo.getVideo() != null)
                {
                    TradeCard.deleteVideo(groupType, groupInfo.getVideo().getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                //FOR GAD-1201 by wang  For GAD-1209 duplicate videoArchive.
                                if (isFromGallery) {
                                    TradeCard.deleteArchiveVideo(groupType, groupInfo.getVideo().getId(), new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess())
                                                Log.e("ArchiveVideo delete", "Successfully");
                                        }
                                    });
                                }
                                groupInfo.setVideo(null);

                                btnPlayVideoButton.setVisibility(View.GONE);

                                imgProfileVideo.refreshOriginalBitmap();
                                imgProfileVideo.setBackgroundResource(R.drawable.add_profile_video);
                                imgProfileVideo.setImageUrl("", imgLoader);
                                imgProfileVideo.invalidate();
                            }
                        }
                    });
                }
                isProfileVideo = false;
            }
            else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                        String.valueOf(System.currentTimeMillis()) + ".jpg"));
                tempPhotoUriPath = uri.getPath();
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                PersonalProfileEditActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
            }
        } else if (index == 1) //photo from gallery
        {
            if(isProfileVideo) {
                playProfileVideo();
                isProfileVideo = false;
            }
            else {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                PersonalProfileEditActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
            }
        } else if (index == 2)//remove photo
        {
            if (isTakingProfilePhoto) {
                if (!groupInfo.getProfileImage().equals("")) {
                    TradeCard.removeProfileImage(groupType, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                strTempPhotoPath = "";
                                strProfileImagePath = "";
                                strProfilePhotoUrl = "";
                                imgPersonalProfilePhoto.refreshOriginalBitmap();
                                imgPersonalProfilePhoto.setImageUrl(strProfileImagePath, imgLoader);
                                imgPersonalProfilePhoto.invalidate();
                                groupInfo.setProfileImage("");
                            }
                        }
                    });
                } else {
                    Uitils.alert(PersonalProfileEditActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
                }
            } else//remove wallpaper photo
            {
                TcImageVO wallpaperImage = null;
                if (groupInfo.getImages() == null || groupInfo.getImages().size() < 1) return;
                for (TcImageVO image : groupInfo.getImages()) {
                    if (image != null && image.getId() > 0) {
                        wallpaperImage = image;
                        break;
                    }
                }
                TradeCard.removeImage(groupType, wallpaperImage.getId(), new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            groupInfo.setImages(new ArrayList<TcImageVO>());
                            imgWallpaper.refreshOriginalBitmap();
                            imgWallpaper.setImageUrl("", imgLoader);
                            imgWallpaper.invalidate();
                        } else {
                            MyApp.getInstance().showSimpleAlertDiloag(PersonalProfileEditActivity.this, R.string.str_alert_failed_to_remove_wallpaper_photo, null);
                        }
                    }
                });
            }
        }
    }
    private void deleteAllTempFiles()
    {
        File dir = new File(mTempDirectory);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                if(children[i].contains(".zip"))
                    continue;
                new File(dir, children[i]).delete();
            }
        }
    }
    private void playProfileVideo(){
        if (groupInfo == null)
            return;
        if (groupInfo.getVideo() == null || groupInfo.getVideo() == null || groupInfo.getVideo().getVideo_url().equals(""))
            return;
        try {
            Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
            videoPlayIntent.setDataAndType(Uri.parse(groupInfo.getVideo().getVideo_url()), "video/*");
            PersonalProfileEditActivity.this.startActivity(videoPlayIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
