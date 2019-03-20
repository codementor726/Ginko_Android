package com.ginko.activity.entity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.ginko.activity.user.PersonalProfilePreviewActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.TradeCard;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.EntityProfileFieldAddOverlayView;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityAddInfoFragment;
import com.ginko.fragments.HomeWorkAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.sz.util.json.JsonConverter;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class EntityEditActivity extends MyBaseFragmentActivity implements View.OnClickListener,
        EntityProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener ,
        EntityAddInfoFragment.OnKeyDownListener,
        ActionSheet.ActionSheetListener{

    private final int TAKE_PHOTO_FROM_CAMERA  = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int TAKE_VIDEO_FROM_CAMERA  = 1133;
    private final int FILTER_PHOTO = 6;

    public static final int PICK_MULTI_REQUEST = 201;

    /* UI Variables */
    private LinearLayout activityRootView;
    private ImageButton btnLockProfile;
    private ImageButton btnMultiLocations;
    private ImageView btnAddProfileField;
    private ImageView imgDimBackground;
    private EntityProfileFieldAddOverlayView addFieldOverlayView;
    private ImageButton btnBack;
    private Button btnDone, btnCancel;

    private CustomNetworkImageView imgEntityLogo;
    private CustomNetworkImageView imgWallpaper , imgProfileVideo;
    private RelativeLayout videoLayout,headerlayout, scrollLayout;
    private ImageView btnPlayVideoButton;
    private EditText edtEntityname , edtDescription;
    private ImageView btnClearSearch;
    private TextView textViewTitle;

    private EntityAddInfoFragment infoListFragment;

    /* Variables */
    private EntityVO entity;
    private EntityVO multiEntity;
    private boolean isNewEntity = false;
    private boolean isCreate = false;
    private boolean isMultiLocations = false;
    private boolean isChanged = false;
    private int currentIndex = 0;

    private ImageLoader imgLoader;

    private ActionSheet takePhotoActionSheet = null;
    private ActionSheet takeVideoFromCameraActionSheet = null;

    private boolean isSharedPrivilege = true;
    private boolean isKeyboardVisible = false;
    private boolean isTakingProfilePhoto = false;
    private boolean isEntityProfileVideo = false;

    private boolean isAddFields = false;
    private boolean isRequestBack = true;

    private boolean isPhotoUploaded = false;
    private boolean isPaperUploaded = false;
    private boolean isVideoUploaded = false;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" ;

    private String strEntityProfilePhotoPath = "";
    private String strEntityWallpaperPhotoPath = "";

    private String strVideoFilePath = "";
    private String strVideoThumbPath = "";

    private Uri uri;
    private String tempPhotoUriPath = "";

    private String strProfilePhotoUrl = "";

    private String mTempDirectory = RuntimeContext.getAppDataFolder("temp");

    private int m_orientHeight = 0;
    private ProgressHUD progressDialog = null;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;
            //if(takePhotoActionSheet == null)

            if (takePhotoActionSheet == null && entity.getProfileImage().equals(""))
            {
                takePhotoActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.entity_edit_take_logo_image) ,
                                getResources().getString(R.string.entity_edit_take_logo_image_from_library))
                        .setCancelableOnTouchOutside(true)
                        .setListener(EntityEditActivity.this)
                        .show();
            }
            else {
                if (entity.getProfileImage().equals(""))
                    takePhotoActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.entity_edit_take_logo_image),
                                    getResources().getString(R.string.entity_edit_take_logo_image_from_library))
                            .setCancelableOnTouchOutside(true)
                            .setListener(EntityEditActivity.this)
                            .show();
                else
                    takePhotoActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.entity_edit_take_logo_image),
                                    getResources().getString(R.string.entity_edit_take_logo_image_from_library),
                                    getResources().getString(R.string.entity_edit_remove_logo_image))
                            .setCancelableOnTouchOutside(true)
                            .setListener(EntityEditActivity.this)
                            .show();
            }
//            else
//                takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    private View.OnClickListener snapWallpaperPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;

            if(takePhotoActionSheet == null && entity.getWallpapaerImage() == null)
                takePhotoActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(EntityEditActivity.this)
                        .show();
            else {
                if(entity.getWallpapaerImage() == null)
                    takePhotoActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                    getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                            .setCancelableOnTouchOutside(true)
                            .setListener(EntityEditActivity.this)
                            .show();
                else
                    takePhotoActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.personal_profile_remove_wallpaper_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(EntityEditActivity.this)
                        .show();
            }
//            else
//                takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    private View.OnClickListener snapProfileVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;

            if(entity.getVideoThumbUrl() == null || entity.getVideoThumbUrl().equals("") || entity.getVideo().equals("") || entity.getVideo() == null){
                deleteAllTempFiles();
                //Toast.makeText(EntityEditActivity.this, "Coming soon....", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EntityEditActivity.this, VideoSetActivity.class);
                intent.putExtra("typeId", "entityInfo");
                intent.putExtra("typeIdVal", entity.getId());
                intent.putExtra("isNewEntity", isNewEntity);
                intent.putExtra("isCreate", isCreate);
                startActivityForResult(intent, TAKE_VIDEO_FROM_CAMERA);
            }else{
                isEntityProfileVideo = true;
                takeVideoFromCameraActionSheet = ActionSheet.createBuilder(EntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_remove_video),
                                getResources().getString(R.string.personal_profile_play_video))
                        .setCancelableOnTouchOutside(true)
                        .setListener(EntityEditActivity.this)
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entity_info);

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity", false);
            this.isCreate = savedInstanceState.getBoolean("isCreate",false);
            this.isMultiLocations = savedInstanceState.getBoolean("isMultiLocations", false);
            this.currentIndex = savedInstanceState.getInt("currentIndex", 0);
        }
        else {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
            this.isNewEntity = getIntent().getBooleanExtra("isNewEntity" , false);
            this.isCreate = getIntent().getBooleanExtra("isCreate" , false);
            this.isMultiLocations = this.getIntent().getBooleanExtra("isMultiLocations",false);
            this.currentIndex = this.getIntent().getIntExtra("currentIndex",0);
        }

        if(this.entity.getPrivilege()!=null)
            this.isSharedPrivilege = this.entity.getPrivilege()>0?true:false;
        else
            this.isSharedPrivilege = false;

        multiEntity = new EntityVO();
        multiEntity.setName("");
        multiEntity.setTags("");
        multiEntity.setId(0);//default 0
        multiEntity.setCategoryId(entity.getCategoryId());
        multiEntity.setPrivilege(1);
        if (this.entity.getProfileImage() != null)
            multiEntity.setProfileImage(this.entity.getProfileImage());
        else
            multiEntity.setProfileImage("");
        if (this.entity.getEntityInfos() != null)
            multiEntity.setEntityInfos(this.entity.getEntityInfos());
        else
            multiEntity.setEntityInfos(new ArrayList<EntityInfoVO>());
        if (this.entity.getEntityImages() != null)
            multiEntity.setEntityImages(this.entity.getEntityImages());
        else
            multiEntity.setEntityImages(new ArrayList<EntityImageVO>());
        if (this.entity.getVideo() != null)
            multiEntity.setVideo(this.entity.getVideo());
        else
            multiEntity.setVideo("");
        if (this.entity.getVideoThumbUrl() != null)
            multiEntity.setVideoThumbUrl(this.entity.getVideoThumbUrl());

        getUIObjects();

        initInfoItemFragment();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity", entity);
        outState.putBoolean("isNewEntity", this.isNewEntity);
        outState.putBoolean("isCreate", this.isCreate);
        outState.putBoolean("isMultiLocations", isMultiLocations);
        outState.putInt("currentIndex", this.currentIndex);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        if(!isNewEntity || isMultiLocations)
            textViewTitle.setText("Edit Entity Profile");

        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        scrollLayout = (RelativeLayout)findViewById(R.id.scrollLayout);
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

        addFieldOverlayView = (EntityProfileFieldAddOverlayView)findViewById(R.id.addFieldOverlayView);
        addFieldOverlayView.setOnProfileFieldItemsChangeListener(this); addFieldOverlayView.setVisibility(View.GONE);

        btnAddProfileField = (ImageView)findViewById(R.id.btnAddFieldInfoItem); btnAddProfileField.setOnClickListener(this);
        btnLockProfile = (ImageButton)findViewById(R.id.btnLockProfile);       btnLockProfile.setOnClickListener(this);
        btnMultiLocations = (ImageButton)findViewById(R.id.btnMultiLocations); btnMultiLocations.setOnClickListener(this);
        if (isMultiLocations == true)
            btnMultiLocations.setVisibility(View.GONE);
        else
            btnMultiLocations.setVisibility(View.VISIBLE);

        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        //btnNext = (Button)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancel); btnCancel.setOnClickListener(this);

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        strEntityProfilePhotoPath = entity.getProfileImage();
        EntityImageVO wallpaperImage = entity.getWallpapaerImage();
        if(wallpaperImage != null)
            strEntityWallpaperPhotoPath = wallpaperImage.getUrl();

        imgEntityLogo = (CustomNetworkImageView)findViewById(R.id.imgEntityLogo);
        imgEntityLogo.setDefaultImageResId(R.drawable.entity_add_logo);
        if(entity.getProfileImage() !=null)
            imgEntityLogo.setImageUrl(strEntityProfilePhotoPath, imgLoader);
        imgEntityLogo.setOnClickListener(snapProfilePhotoClickListener);

        imgWallpaper = (CustomNetworkImageView)findViewById(R.id.imgWallpaper);
        imgWallpaper.setDefaultImageResId(R.drawable.add_wallpaper);
        if((wallpaperImage = entity.getWallpapaerImage()) !=null) {
            imgWallpaper.setImageUrl(strEntityWallpaperPhotoPath , imgLoader);
        }
        imgWallpaper.invalidate();
        imgWallpaper.setOnClickListener(snapWallpaperPhotoClickListener);

        videoLayout = (RelativeLayout)findViewById(R.id.videoLayout); videoLayout.setClickable(true);
        btnPlayVideoButton = (ImageView)findViewById(R.id.imgVideoPlayButton);
        /* modify by lee
        btnPlayVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EntityEditActivity.this , "Entity video is coming." , Toast.LENGTH_LONG).show();
            }
        });
        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entity == null)
                    return;
                if(entity.getVideo() == null || entity.getVideo() == null || entity.getVideo().equals(""))
                    return;
                try {
                    Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
                    videoPlayIntent.setDataAndType(Uri.parse(entity.getVideo()), "video/*");
                    EntityEditActivity.this.startActivity(videoPlayIntent);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        */
        imgProfileVideo = (CustomNetworkImageView)findViewById(R.id.imgProfileVideo);
        imgProfileVideo.setDefaultImageResId(R.drawable.add_profile_video);

        if(entity.getVideoThumbUrl() != null && !entity.getVideoThumbUrl().equals(""))
        {
            imgProfileVideo.setBackground(null);
            imgProfileVideo.setImageUrl(entity.getVideoThumbUrl(), imgLoader);
            //imgProfileVideo.invalidate();

            btnPlayVideoButton.setVisibility(View.VISIBLE);
        }
        else
        {
            btnPlayVideoButton.setVisibility(View.GONE);
            //imgProfileVideo.invalidate();
        }
        imgProfileVideo.setOnClickListener(snapProfileVideoListener);
        imgProfileVideo.invalidate();

        imgDimBackground = (ImageView)findViewById(R.id.imgDimBackground);
        imgDimBackground.setVisibility(View.GONE);

        edtEntityname = (EditText)findViewById(R.id.edtEntityName); edtEntityname.setText(entity.getName());
        edtEntityname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtEntityname.hasFocus() && s.length() > 0)
                    btnClearSearch.setVisibility(View.VISIBLE);
                else
                    btnClearSearch.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtEntityname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtEntityname.getWindowToken(), 0);

                    btnClearSearch.setVisibility(View.GONE);
                    scrollLayout.requestFocus();

                    return true;
                }
                return false;
            }
        });

        edtEntityname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (edtEntityname.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    edtEntityname.setCursorVisible(true);

                } else {
                    btnClearSearch.setVisibility(View.GONE);
                    edtEntityname.setCursorVisible(false);
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtEntityname.setText("");
                btnClearSearch.setVisibility(View.GONE);
            }
        });
        btnClearSearch.setVisibility(View.GONE);

        edtDescription = (EditText)findViewById(R.id.edtDescription); edtDescription.setText(entity.getDescription());

        activityRootView = (LinearLayout) findViewById(R.id.rootLayout);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        if (infoListFragment != null)
                            infoListFragment.setKeyboardVisibilty(true);
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
        //For GAD-1264
        activityRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        if (!isNewEntity){
            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            //btnBack.setBackground(getResources().getDrawable(R.drawable.part_a_btn_back_nav));   For GAD-1161
            btnBack.setImageResource(R.drawable.btn_back_nav_black);
            btnBack.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            textViewTitle.setTextColor(getResources().getColor(R.color.black));
            btnDone.setText("Done");
            btnCancel.setTextColor(getResources().getColor(R.color.black));
            btnDone.setTextColor(getResources().getColor(R.color.black));
        }else{
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            //btnBack.setBackground(getResources().getDrawable(R.drawable.btn_back_nav_white));   For GAD-1161
            textViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            if (isMultiLocations)
            {
                btnBack.setVisibility(View.GONE);
                btnCancel.setTextColor(getResources().getColor(R.color.top_title_text_color));
                btnCancel.setVisibility(View.VISIBLE);
                btnDone.setText("Done");
                btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color));
            } else
            {
                btnBack.setImageResource(R.drawable.btn_back_nav_white);
                btnBack.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.GONE);
                btnDone.setText("Next");
                btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color));
            }
        }
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
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;
        if(entity.getEntityInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
        }
        else
            entityInfo = entity.getEntityInfos().get(currentIndex);

        infoListFragment = EntityAddInfoFragment.newInstance(entityInfo,false);
        infoListFragment.setOnProfileFieldItemsChangeListener(this);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();
    }

    private void resumeInfoFragment()
    {
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;
        if(entity.getEntityInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
        }
        else
            entityInfo = entity.getEntityInfos().get(currentIndex);

        infoListFragment = EntityAddInfoFragment.newInstance(entityInfo,false);
        infoListFragment.setOnProfileFieldItemsChangeListener(this);
        Handler handler_ = new Handler(Looper.getMainLooper());
        handler_.postDelayed(new Runnable() {
            @Override
            public void run() {
                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fieldsLayout, infoListFragment);
                ft.commitAllowingStateLoss();
            }
        }, 0);
    }

    private void updateLockButton()
    {
        if(!isSharedPrivilege)
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
        //if(isKeyboardVisible)
        //    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //initInfoItemFragment();
        //GAD-1792 Not show Address Field always
        /*
        EntityInfoVO entityInfoVO = infoListFragment.getEntityInfo();

        if(entityInfoVO != null) {
            if (entityInfoVO.getLongitude() == null || entityInfoVO.getLatitude() == null){
                if (infoListFragment != null)
                    infoListFragment.addNewInfoItem("Address");
            }
        }
        */
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

    /*
         * Disable back button to go to previsous screen
         */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(addFieldOverlayView.getVisibility() == View.VISIBLE)
        {
            imgDimBackground.setVisibility(View.GONE);
            addFieldOverlayView.hideView();
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            return;
        }
        //if(entity.getId() != 0)
        super.onBackPressed();
    }

    private void goToDone()
    {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", entity);
        resultIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void goToPreviewScreen()
    {
        if(progressDialog != null)
            progressDialog.dismiss();

        //Intent entityProfilePreviewIntent = new Intent(EntityEditActivity.this , EntityProfilePreviewActivity.class);
        Intent entityProfilePreviewIntent = new Intent(EntityEditActivity.this , EntityProfilePreviewAfterEditAcitivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", entity);
        bundle.putBoolean("isMultiLocations",isMultiLocations);
        bundle.putInt("infoID", entity.getEntityInfos().get(currentIndex).getId());
        entityProfilePreviewIntent.putExtras(bundle);
        startActivity(entityProfilePreviewIntent);
        finish();
    }
    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);

        scrollLayout.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnCancel:
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnDone:
            //case R.id.btnNext:
                if (isRequestBack == true)
                    gotToDoneOrNext();
                break;

            //add profile field items
            case R.id.btnAddFieldInfoItem:
                if(infoListFragment == null) return;

                hideKeyboard();
                addFieldOverlayView.setProfileFieldItems(infoListFragment.getCurrentVisibleInfoItems());

                if(addFieldOverlayView.getVisibility() == View.GONE && !isAddFields)
                {
                    imgDimBackground.setVisibility(View.VISIBLE);
                    imgDimBackground.setFocusable(true);
                    addFieldOverlayView.showView();
                    btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                }
                else
                {
                    imgDimBackground.setVisibility(View.GONE);
                    imgDimBackground.setFocusable(false);
                    addFieldOverlayView.hideView();
                    if(isAddFields)
                        btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                    else
                        btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
                }
                break;

            //lock & unlock profile
            case R.id.btnLockProfile:
                if(addFieldOverlayView.getVisibility() == View.VISIBLE)
                    return;

                isSharedPrivilege = !isSharedPrivilege;
                if(!isSharedPrivilege) {
                    final Toast toast = Toast.makeText(EntityEditActivity.this, "Entity is private.", Toast.LENGTH_SHORT);
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
                    final Toast toast = Toast.makeText(EntityEditActivity.this, "Entity is public.", Toast.LENGTH_SHORT);
                    toast.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 300);
                }

                updateLockButton();

                break;
            //Multi Locations
            case R.id.btnMultiLocations:
                if(addFieldOverlayView.getVisibility() == View.VISIBLE)
                    return;
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                MyApp.getInstance().hideKeyboard(activityRootView);
                gotToMultiLocation();

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
                case PICK_MULTI_REQUEST:
                    isChanged = data.getBooleanExtra("isChanged", false);
                    if (isChanged)
                        resumeInfoFragment();
                    break;
                case TAKE_PHOTO_FROM_CAMERA:
                    strTempPhotoPath = tempPhotoUriPath;// uri.getPath();
                    if(!strTempPhotoPath.contains("file://"))
                        strTempPhotoPath = "file://"+strTempPhotoPath;

                    System.out.println("-----Photo Path= " + strTempPhotoPath + "----");

                    goToFilterScreen();

                    /*if(isTakingProfilePhoto)
                        setUserProfilePhoto();
                    else
                        setWallpaperPhoto();*/


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
                        if(strTempPhotoPath == null)
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, "Unsupport image file format.", null);
                            return;
                        }
                        if (!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        goToFilterScreen();

                        /*if(isTakingProfilePhoto)
                            setUserProfilePhoto();
                        else
                            setWallpaperPhoto();*/

                    }
                    break;
                case FILTER_PHOTO:
                    if(isTakingProfilePhoto)
                        setEntityProfilePhoto();
                    else
                        setWallpaperPhoto();
                    break;

                case TAKE_VIDEO_FROM_CAMERA:
                    final String videoFilePath = data.getStringExtra("strMoviePath");
                    final String videoThumbFilePath = data.getStringExtra("strThumbPath");
                    boolean isHistory = data.getBooleanExtra("isHistory", false);
                    if(isHistory)
                    {
                        entity.setVideo(videoFilePath);
                        entity.setVideoThumbUrl(videoThumbFilePath);
                        multiEntity.setVideo(videoFilePath);
                        multiEntity.setVideoThumbUrl(videoThumbFilePath);

                        imgProfileVideo.refreshOriginalBitmap();
                        imgProfileVideo.setImageUrl(videoThumbFilePath, imgLoader);
                        imgProfileVideo.invalidate();

                        btnPlayVideoButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        if (entity.getId() == 0) {
                            strVideoFilePath = videoFilePath;
                            strVideoThumbPath = videoThumbFilePath;

                            entity.setVideo("file://"+strVideoFilePath);
                            entity.setVideoThumbUrl("file://"+strVideoThumbPath);

                            multiEntity.setVideo("file://"+strVideoFilePath);
                            multiEntity.setVideoThumbUrl("file://"+strVideoThumbPath);

                            imgProfileVideo.refreshOriginalBitmap();
                            imgProfileVideo.setImageUrl("file://"+strVideoThumbPath, imgLoader);
                            imgProfileVideo.invalidate();

                            btnPlayVideoButton.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (videoFilePath != null && videoFilePath.equals("") == false) {
                            EntityRequest.uploadVideo(entity.getId(), new File(videoFilePath), new File(videoThumbFilePath), true, new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    if (response.isSuccess()) {
                                        JSONObject jsonObject = response.getData();
                                        String videoUrl = jsonObject.optString("video", "");
                                        String videoThumbUrl = jsonObject.optString("video_thumbnail_url", "");
                                        entity.setVideo(videoUrl);
                                        entity.setVideoThumbUrl(videoThumbUrl);

                                        multiEntity.setVideo(videoUrl);
                                        multiEntity.setVideoThumbUrl(videoThumbUrl);

                                        imgProfileVideo.refreshOriginalBitmap();
                                        imgProfileVideo.setImageUrl(entity.getVideoThumbUrl(), imgLoader);
                                        imgProfileVideo.invalidate();

                                        btnPlayVideoButton.setVisibility(View.VISIBLE);
                                    } else {
                                        MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
                                    }
                                }
                            });
                        }
                    }
            }
        }
        else
        {
            strTempPhotoPath = "";
            strProfileImagePath = "";
        }
    }

    public void gotToDoneOrNext()
    {
        if(edtEntityname.getText().toString().trim().equals(""))
        {
            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_for_input_name_item , null);
            return;
        }

        /*
        if(edtDescription.getText().toString().trim().equals(""))
        {
            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_for_input_entity_description , null);
            return;
        }*/

        entity.setName(edtEntityname.getText().toString().trim());
        entity.setDescription(edtDescription.getText().toString().trim());
        entity.setPrivilege(isSharedPrivilege?1:0);
        EntityInfoVO entityInfoVO = null;
        if(infoListFragment != null)
            entityInfoVO = infoListFragment.saveEntityInfo(EntityEditActivity.this, true);
        if(entityInfoVO == null)
            return;

        if(entityInfoVO.getEntityInfoDetails().size() < 1)
        {
            //if input fields have invalid value
            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, "Oops!", R.string.str_alert_dialog_entity_at_least_one_contact_info, null);
            return;
        }

//        for(EntityInfoVO info:entity.getEntityInfos())
//        {
//            if(info != null) {
//                entityInfoVO.setId(info.getId());
//                break;
//            }
//        }
        if (entity.getEntityInfos().get(currentIndex).getEntityInfoDetails().size()> 0)
            entityInfoVO.setId(entity.getEntityInfos().get(currentIndex).getId());

        //ArrayList<EntityInfoVO> infoList = new ArrayList<EntityInfoVO>();
        //infoList.add(entityInfoVO);
        entity.getEntityInfos().set(currentIndex, entityInfoVO);
        //entity.setEntityInfos(infoList);

        isRequestBack = false;

        if(entity.getId() == 0)//if entity is not created yet,then create the entity
        {
            File entityWallpaperPhotoFile = null;
            if(!strEntityWallpaperPhotoPath.equals(""))
                entityWallpaperPhotoFile = new File(strEntityWallpaperPhotoPath);

            isPhotoUploaded = false;
            isPaperUploaded = false;
            isVideoUploaded = false;

            EntityRequest.createEntity(entity.getCategoryId(), entity.getName(), null, entity.getPrivilege(), entityWallpaperPhotoFile, null,
                    new ResponseCallBack<EntityVO>() {
                        @Override
                        public void onCompleted(JsonResponse<EntityVO> response) {
                            if (response.isSuccess()) {
                                EntityVO newEntity = response.getData();
                                entity.setId(newEntity.getId());
                                if(EntityCategorySelectActivity.getInstance() != null)
                                    EntityCategorySelectActivity.getInstance().finish();
                                EntityRequest.saveEntity(entity, new ResponseCallBack<EntityVO>() {
                                    @Override
                                    public void onCompleted(JsonResponse<EntityVO> response) {
                                        if(response.isSuccess())
                                        {
                                            entity = response.getData();
                                            for (int i = 0; i < entity.getEntityInfos().size(); i++) {
                                                EntityInfoVO location = entity.getEntityInfos().get(i);
                                                if (location.isAddressConfirmed() == false) {
                                                    location.setLatitude(null);
                                                    location.setLongitude(null);
                                                }
                                            }

                                            if (progressDialog == null) {
                                                progressDialog = ProgressHUD.createProgressDialog(EntityEditActivity.this, getResources().getString(R.string.str_uploading_now),
                                                        true, false, null);
                                                progressDialog.show();
                                            } else {
                                                progressDialog.show();
                                            }

                                            if(!strEntityProfilePhotoPath.equals(""))
                                            {
                                                File profilePhotoFile = new File(strEntityProfilePhotoPath);
                                                if(profilePhotoFile.exists()) {
                                                    EntityRequest.uploadProfileImage(entity.getId(), profilePhotoFile, false, new ResponseCallBack<JSONObject>() {
                                                        @Override
                                                        public void onCompleted(JsonResponse<JSONObject> response) {
                                                            isPhotoUploaded = true;
                                                            if (response.isSuccess()) {
                                                                JSONObject data = response.getData();
                                                                try {
                                                                    String photoUrl = data.getString("profile_image");
                                                                    System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                                                    strProfileImagePath = strTempPhotoPath;
                                                                    entity.setProfileImage(photoUrl);
                                                                    strTempPhotoPath = "";
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                    strTempPhotoPath = "";
                                                                }
                                                            } else {
                                                                //Uitils.alert(EntityEditActivity.this, "Failed to upload profile photo.");
                                                            }

                                                            if (isPaperUploaded == true && isVideoUploaded == true)
                                                            {
                                                                isRequestBack = true;
                                                                goToPreviewScreen();
                                                            }
                                                        }
                                                    });
                                                } else
                                                    isPhotoUploaded = true;
                                            } else
                                                isPhotoUploaded = true;

                                            if(!strEntityWallpaperPhotoPath.equals(""))
                                            {
                                                File wallpaperPhotoFile = new File(strEntityWallpaperPhotoPath);
                                                if(wallpaperPhotoFile.exists()) {
                                                    EntityRequest.uploadMultipleImage(entity.getId(), wallpaperPhotoFile, null, false, new ResponseCallBack<List<EntityImageVO>>() {
                                                        @Override
                                                        public void onCompleted(JsonResponse<List<EntityImageVO>> response) {
                                                            isPaperUploaded = true;
                                                            if (response.isSuccess()) {
                                                                List<EntityImageVO> images = response.getData();

                                                                entity.setEntityImages(images);
                                                                multiEntity.setEntityImages(images);
                                                                strProfileImagePath = strTempPhotoPath;
                                                                strProfilePhotoUrl = entity.getWallpapaerImage().getUrl();
                                                                strTempPhotoPath = "";

                                                            } else {
                                                                strTempPhotoPath = "";
                                                                strProfileImagePath = "";
                                                                //MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                                                            }

                                                            if (isPhotoUploaded == true && isVideoUploaded == true)
                                                            {
                                                                isRequestBack = true;
                                                                goToPreviewScreen();
                                                            }
                                                        }
                                                    });
                                                } else
                                                    isPaperUploaded = true;
                                            } else
                                                isPaperUploaded = true;

                                            if (strVideoFilePath != null && strVideoFilePath.equals("") == false) {
                                                EntityRequest.uploadVideo(entity.getId(), new File(strVideoFilePath), new File(strVideoThumbPath), false, new ResponseCallBack<JSONObject>() {
                                                    @Override
                                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                                        isVideoUploaded = true;
                                                        if (response.isSuccess()) {
                                                            JSONObject jsonObject = response.getData();
                                                            String videoUrl = jsonObject.optString("video", "");
                                                            String videoThumbUrl = jsonObject.optString("video_thumbnail_url", "");
                                                            entity.setVideo(videoUrl);
                                                            entity.setVideoThumbUrl(videoThumbUrl);

                                                            imgProfileVideo.refreshOriginalBitmap();
                                                            imgProfileVideo.setImageUrl(entity.getVideoThumbUrl(), imgLoader);
                                                            imgProfileVideo.invalidate();

                                                            btnPlayVideoButton.setVisibility(View.VISIBLE);
                                                        } else {
                                                            //MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
                                                        }

                                                        if (isPhotoUploaded == true && isPaperUploaded == true)
                                                        {
                                                            isRequestBack = true;
                                                            goToPreviewScreen();
                                                        }
                                                    }
                                                });
                                            }
                                            else {
                                                isVideoUploaded = true;
                                            }

                                            if (isVideoUploaded == true && isPhotoUploaded == true && isPaperUploaded == true)
                                            {
                                                isRequestBack = true;
                                                goToPreviewScreen();
                                            }
                                        }
                                    }
                                });

                            } else {
                                isRequestBack = true;
                                if(progressDialog != null)
                                    progressDialog.dismiss();
                                Uitils.alert(EntityEditActivity.this, "Failed to Create Entity.");
                            }
                        }
                    });
        }
        else
        {
            EntityRequest.saveEntity(entity, new ResponseCallBack<EntityVO>() {

                @Override
                public void onCompleted(JsonResponse<EntityVO> response) {
                    isRequestBack = true;
                    if (response.isSuccess()) {
                        entity = response.getData();
                        for (int i = 0; i < entity.getEntityInfos().size(); i++) {
                            EntityInfoVO location = entity.getEntityInfos().get(i);
                            if (location.isAddressConfirmed() == false) {
                                location.setLatitude(null);
                                location.setLongitude(null);
                            }
                        }

                        goToDone();
                    } else {
                        Uitils.alert(EntityEditActivity.this, "Failed to save entity info.");
                    }
                }
            });
        }
    }

    public void gotToMultiLocation(){

//        if(edtEntityname.getText().toString().trim().equals(""))
//        {
//            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_for_input_name_item , null);
//            return;
//        }

        /*
        if(edtDescription.getText().toString().trim().equals(""))
        {
            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_for_input_entity_description , null);
            return;
        }*/

        multiEntity.setName(edtEntityname.getText().toString().trim());
        multiEntity.setDescription(edtDescription.getText().toString().trim());
        multiEntity.setPrivilege(isSharedPrivilege ? 1 : 0);
        if (entity.getId() !=0)
            multiEntity.setId(entity.getId());

        EntityInfoVO entityInfoVO = null;
        if(infoListFragment != null)
            entityInfoVO = infoListFragment.saveEntityInfoForMulti(EntityEditActivity.this, true);
            //entityInfoVO = infoListFragment.getEntityInfo();
        if(entityInfoVO == null)
            return;

//        for(EntityInfoVO info:multiEntity.getEntityInfos())
//        {
//            if(info != null) {
//                entityInfoVO.setId(info.getId());
//                break;
//            }
//        }

        if (entity.getEntityInfos().get(currentIndex).getEntityInfoDetails().size()> 0)
            entityInfoVO.setId(entity.getEntityInfos().get(currentIndex).getId());

        ArrayList<EntityInfoVO> infoList = new ArrayList<EntityInfoVO>();
        infoList.add(entityInfoVO);
        multiEntity.setEntityInfos(infoList);

        Intent intent = new Intent(EntityEditActivity.this, MultiLocationEntityEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity", multiEntity);
        bundle.putSerializable("isNewEntity", isNewEntity);
        bundle.putSerializable("isCreate", isCreate);
        bundle.putSerializable("isMultiLocations", false);
        intent.putExtras(bundle);
        startActivityForResult(intent, PICK_MULTI_REQUEST);
    }
    private void goToFilterScreen()
    {
        Intent intent = new Intent(EntityEditActivity.this , PersonalProfilePhotoFilterActivity.class);
        intent.putExtra("imagePath" , strTempPhotoPath);
        File filteredImagePath = null;
        if(isTakingProfilePhoto) {
            filteredImagePath = new File(RuntimeContext.getAppDataFolder("Entities") +
                    String.valueOf("entity"+".jpg"));
        }
        else
        {
            filteredImagePath = new File(RuntimeContext.getAppDataFolder("Entities") +
                    String.valueOf("entity"+"_wallpaper.jpg"));
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
        intent.putExtra("groupType", "entity");
        intent.putExtra("isNewEntity", isNewEntity);

        if(isTakingProfilePhoto)
        {
            intent.putExtra("aspect_x", 1);
            intent.putExtra("aspect_y", 1);
        }
        else {
            intent.putExtra("aspect_x", 10);
            intent.putExtra("aspect_y", 4);
        }
        startActivityForResult(intent , FILTER_PHOTO);
    }

    private void setWallpaperPhoto()
    {
        if(!strTempPhotoPath.startsWith("file://"))
            strTempPhotoPath = "file://"+strTempPhotoPath;

        List<EntityImageVO> images = new ArrayList<EntityImageVO>();
        EntityImageVO wallImage = new EntityImageVO();


        File photoFile = null;
        try {
            photoFile = new File(new URI(strTempPhotoPath.replaceAll(" ", "%20")));

            if(!photoFile.exists())
                return;

            final String strUploadPhotoPath = photoFile.getAbsolutePath();

            if(entity.getId() == 0)//if entity is not created yet, then save the entity profile photo path
            {
                strEntityWallpaperPhotoPath = photoFile.getAbsolutePath();
                imgWallpaper.refreshOriginalBitmap();
                imgWallpaper.setImageUrl("file://" + strUploadPhotoPath, imgLoader);

                wallImage.setUrl(strUploadPhotoPath);
                images.add(wallImage);
                entity.setEntityImages(images);
                multiEntity.setEntityImages(images);

                imgWallpaper.invalidate();
                return;
            }

            //String zippedPhotoFile = ImageScalingUtilities.decodeFile(photoFile.getAbsolutePath(), 300, 300);
            //if(zippedPhotoFile.equalsIgnoreCase(""))
            //    return;

            //photoFile = new File(zippedPhotoFile);

            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {
                EntityRequest.uploadMultipleImage(entity.getId(), photoFile, null, true, new ResponseCallBack<List<EntityImageVO>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<EntityImageVO>> response) {
                        if (response.isSuccess()) {
                            List<EntityImageVO> images = response.getData();

                            entity.setEntityImages(images);
                            multiEntity.setEntityImages(images);
                            strProfileImagePath = strTempPhotoPath;
                            strProfilePhotoUrl = entity.getWallpapaerImage().getUrl();
                            imgWallpaper.refreshOriginalBitmap();
                            imgWallpaper.setImageUrl(strProfilePhotoUrl, imgLoader);
                            imgWallpaper.invalidate();
                            strTempPhotoPath = "";

                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgWallpaper.refreshOriginalBitmap();
                            imgWallpaper.setImageUrl(strProfileImagePath, imgLoader);
                            imgWallpaper.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                        }
                    }
                });

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void setEntityProfilePhoto()
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

            if(entity.getId() == 0)//if entity is not created yet, then save the entity profile photo path
            {
                strEntityProfilePhotoPath = photoFile.getAbsolutePath();
                imgEntityLogo.refreshOriginalBitmap();
                imgEntityLogo.setImageUrl("file://" + strUploadPhotoPath, imgLoader);
                entity.setProfileImage(strUploadPhotoPath);
                multiEntity.setProfileImage(strUploadPhotoPath);
                imgEntityLogo.invalidate();
                return;
            }


            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {

                EntityRequest.uploadProfileImage(entity.getId(), photoFile, true, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject data = response.getData();
                            try {
                                String photoUrl = data.getString("profile_image");
                                System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                strProfileImagePath = strTempPhotoPath;
                                strProfilePhotoUrl = photoUrl;
                                entity.setProfileImage(photoUrl);
                                multiEntity.setProfileImage(photoUrl);
                                imgEntityLogo.refreshOriginalBitmap();
                                imgEntityLogo.setImageUrl("file://" + strUploadPhotoPath, imgLoader);
                                imgEntityLogo.invalidate();
                                strTempPhotoPath = "";

                            } catch (Exception e) {
                                e.printStackTrace();
                                strTempPhotoPath = "";
                            }
                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgEntityLogo.refreshOriginalBitmap();
                            imgEntityLogo.setImageUrl(strProfileImagePath, imgLoader);
                            imgEntityLogo.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_err_upload_photo, null);
                        }
                    }
                });

            }
        }
        catch(URISyntaxException e)
        {
            e.printStackTrace();

        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//take a photo
        {
            if(isEntityProfileVideo)
            {
                if (entity.getId() == 0) {
                    strVideoFilePath = "";
                    strVideoThumbPath = "";

                    entity.setVideo(strVideoFilePath);
                    entity.setVideoThumbUrl(strVideoThumbPath);

                    multiEntity.setVideo(strVideoFilePath);
                    multiEntity.setVideoThumbUrl(strVideoThumbPath);

                    imgProfileVideo.refreshOriginalBitmap();
                    imgProfileVideo.setImageUrl(strVideoThumbPath, imgLoader);
                    imgProfileVideo.invalidate();

                    btnPlayVideoButton.setVisibility(View.GONE);
                    return;
                }
                else {
                    EntityRequest.removeVideo(entity.getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                /*EntityRequest.deleteArchiveVideo(mItem.archiveID, entity.getId(), new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if(response.isSuccess())
                                            Log.e("Archive video delete", "Sucessfully");
                                    }
                                });*/

                                entity.setVideo("");
                                entity.setVideoThumbUrl("");
                                multiEntity.setVideo("");
                                multiEntity.setVideoThumbUrl("");

                                imgProfileVideo.refreshOriginalBitmap();
                                imgProfileVideo.setBackgroundResource(R.drawable.add_profile_video);
                                imgProfileVideo.setImageUrl("", imgLoader);
                                imgProfileVideo.invalidate();

                                btnPlayVideoButton.setVisibility(View.GONE);
                            } else
                                MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
                        }
                    });
                }

                isEntityProfileVideo = false;
            }else{
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                        String.valueOf(System.currentTimeMillis()) + ".jpg"));
                tempPhotoUriPath = uri.getPath();
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                EntityEditActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
            }
        }
        else if(index == 1) //photo from gallery
        {
            if(isEntityProfileVideo){
                playEntityProfileVideo();
                isEntityProfileVideo = false;
            }
            else {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                EntityEditActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
            }
        }
        else if(index == 2)//remove photo
        {
            if(isTakingProfilePhoto) {
                if (!entity.getProfileImage().equals("")) {
                    if (entity.getId() == 0) {
                        strTempPhotoPath = "";
                        strProfileImagePath = "";
                        strProfilePhotoUrl = "";
                        strEntityProfilePhotoPath = "";
                        imgEntityLogo.refreshOriginalBitmap();
                        imgEntityLogo.setImageUrl(strProfileImagePath, imgLoader);
                        imgEntityLogo.invalidate();
                        entity.setProfileImage("");
                        multiEntity.setProfileImage("");
                    }
                    else {
                        EntityRequest.removeProfileImage(entity.getId(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    strTempPhotoPath = "";
                                    strProfileImagePath = "";
                                    strProfilePhotoUrl = "";
                                    strEntityProfilePhotoPath = "";
                                    imgEntityLogo.refreshOriginalBitmap();
                                    imgEntityLogo.setImageUrl(strProfileImagePath, imgLoader);
                                    imgEntityLogo.invalidate();
                                    entity.setProfileImage("");
                                    multiEntity.setProfileImage("");
                                }
                            }
                        });
                    }
                } else {
                    Uitils.alert(EntityEditActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
                }
            }
            else//remove wallpaper photo
            {
                if(entity.getEntityImages() == null || entity.getEntityImages().size() < 1) return;
                EntityImageVO wallpaperImage = entity.getWallpapaerImage();
                if(wallpaperImage == null) return;

                if (entity.getId() == 0) {
                    entity.setEntityImages(new ArrayList<EntityImageVO>());
                    multiEntity.setEntityImages(new ArrayList<EntityImageVO>());
                    strTempPhotoPath = "";
                    strProfileImagePath = "";
                    strProfilePhotoUrl = "";
                    strEntityWallpaperPhotoPath = "";
                    imgWallpaper.refreshOriginalBitmap();
                    imgWallpaper.setImageUrl("", imgLoader);
                    imgWallpaper.invalidate();
                }
                else {
                    EntityRequest.removeImage(entity.getId(), wallpaperImage.getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                entity.setEntityImages(new ArrayList<EntityImageVO>());
                                multiEntity.setEntityImages(new ArrayList<EntityImageVO>());
                                strTempPhotoPath = "";
                                strProfileImagePath = "";
                                strProfilePhotoUrl = "";
                                strEntityWallpaperPhotoPath = "";
                                imgWallpaper.refreshOriginalBitmap();
                                imgWallpaper.setImageUrl("", imgLoader);
                                imgWallpaper.invalidate();
                            } else {
                                MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_remove_wallpaper_photo, null);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onAddedNewProfileField(String fieldName) {
        if(fieldName.equals("noExistAddFields")){
            imgDimBackground.setVisibility(View.GONE);
            imgDimBackground.setFocusable(false);
            addFieldOverlayView.hideOverlapView();
            //btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            btnAddProfileField.setVisibility(View.GONE);
            isAddFields = true;
            return;
        }
        if(infoListFragment != null)
            infoListFragment.addNewInfoItem(fieldName);
    }

    @Override
    public void onRemovedProfileField(String fieldName) {
        if(infoListFragment != null) {
            isAddFields = false;
            if (btnAddProfileField.getVisibility() == View.GONE)
                btnAddProfileField.setVisibility(View.VISIBLE);
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            if(fieldName.equals("Address")) {
                infoListFragment.getEntityInfo().setLongitude(null);
                infoListFragment.getEntityInfo().setLatitude(null);
                infoListFragment.getEntityInfo().setAddressConfirmed(true);
            }
            infoListFragment.removeInfoItem(fieldName);

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
    private void playEntityProfileVideo(){
        if(entity == null)
            return;
        if(entity.getVideo() == null || entity.getVideo() == null || entity.getVideo().equals(""))
            return;
        try {
            Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
            videoPlayIntent.setDataAndType(Uri.parse(entity.getVideo()), "video/*");
            EntityEditActivity.this.startActivity(videoPlayIntent);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
