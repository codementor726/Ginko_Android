package com.ginko.activity.entity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.common.VideoSetActivity;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.SearchContactActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.activity.user.PersonalProfilePreviewActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.SyncRequest;
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
import com.ginko.fragments.LocationListFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.GroupVO;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MultiLocationEntityEditActivity extends MyBaseFragmentActivity implements View.OnClickListener,
        ActionSheet.ActionSheetListener{
    public static final int STATIC_INTEGER_VALUE = 101;

    private final int TAKE_PHOTO_FROM_CAMERA  = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int TAKE_VIDEO_FROM_CAMERA  = 1133;
    private final int FILTER_PHOTO = 6;

    /* UI Variables */
    private LinearLayout activityRootView;
    private ImageButton btnLockProfile;
    private ImageButton btnSingleLocation;
    private ImageButton btnAddLocation;
    private Button btnDone, btnNext;
    private Button btnBack;

    private CustomNetworkImageView imgEntityLogo;
    private CustomNetworkImageView imgWallpaper , imgProfileVideo;
    private RelativeLayout videoLayout,headerlayout;
    private ImageView btnPlayVideoButton;
    private EditText edtEntityname , edtDescription;
    private ImageView btnClearSearch;
    private TextView textViewTitle;

    private ListView locationListView;
    private LocationsListAdapter mAdpater;
    private ArrayList<LocationsItem> locationItems;
    private ArrayList<LocationsItem> items_temp;

    /* Variables */
    private EntityVO entity;
    private EntityInfoVO entity_tmp;
    private boolean isNewEntity = false;
    private boolean isCreate = false;

    private ImageLoader imgLoader;

    private ActionSheet takePhotoActionSheet = null;
    private ActionSheet takeVideoFromCameraActionSheet = null;

    private boolean isSharedPrivilege = true;
    private boolean isKeyboardVisible = false;
    private boolean isTakingProfilePhoto = false;
    private boolean isEntityProfileVideo = false;
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

    private String strDeletedID = "";

    private int currentIndedOfItem = 0;
    private Boolean isDeleted = false;
    private Boolean isChanged = false;

    private boolean isMultiLocations = false;
    private ProgressHUD progressDialog = null;

    private Uri uri;
    private String tempPhotoUriPath = "";

    private String strProfilePhotoUrl = "";

    private String mTempDirectory = RuntimeContext.getAppDataFolder("temp");

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;
            //if(takePhotoActionSheet == null)
            if (takePhotoActionSheet == null && entity.getProfileImage().equals(""))
            {
                takePhotoActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.entity_edit_take_logo_image) ,
                                getResources().getString(R.string.entity_edit_take_logo_image_from_library))
                        .setCancelableOnTouchOutside(true)
                        .setListener(MultiLocationEntityEditActivity.this)
                        .show();
            }
            else {
                if (entity.getProfileImage().equals(""))
                    takePhotoActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.entity_edit_take_logo_image),
                                    getResources().getString(R.string.entity_edit_take_logo_image_from_library))
                            .setCancelableOnTouchOutside(true)
                            .setListener(MultiLocationEntityEditActivity.this)
                            .show();
                else
                    takePhotoActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.entity_edit_take_logo_image),
                                    getResources().getString(R.string.entity_edit_take_logo_image_from_library),
                                    getResources().getString(R.string.entity_edit_remove_logo_image))
                            .setCancelableOnTouchOutside(true)
                            .setListener(MultiLocationEntityEditActivity.this)
                            .show();
            }
        }
    };

    private View.OnClickListener snapWallpaperPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;
            if(takePhotoActionSheet == null && entity.getWallpapaerImage() == null)
                takePhotoActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(MultiLocationEntityEditActivity.this)
                        .show();
            else {
                if(entity.getWallpapaerImage() == null)
                    takePhotoActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                    getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                            .setCancelableOnTouchOutside(true)
                            .setListener(MultiLocationEntityEditActivity.this)
                            .show();
                else
                    takePhotoActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                    getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                    getResources().getString(R.string.personal_profile_remove_wallpaper_photo))
                            .setCancelableOnTouchOutside(true)
                            .setListener(MultiLocationEntityEditActivity.this)
                            .show();
            }
        }
    };

    private View.OnClickListener snapProfileVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;

            if(entity.getVideoThumbUrl() == null || entity.getVideoThumbUrl().equals("") || entity.getVideo().equals("") || entity.getVideo() == null){
                deleteAllTempFiles();
                //Toast.makeText(MultiLocationEntityEditActivity.this, "Coming soon....", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MultiLocationEntityEditActivity.this, VideoSetActivity.class);
                intent.putExtra("typeId", "entityInfo");
                intent.putExtra("isNewEntity", isNewEntity);
                intent.putExtra("isCreate", isCreate);
                startActivityForResult(intent, TAKE_VIDEO_FROM_CAMERA);
            }else{
                isEntityProfileVideo = true;
                takeVideoFromCameraActionSheet = ActionSheet.createBuilder(MultiLocationEntityEditActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_remove_video),
                                getResources().getString(R.string.personal_profile_play_video))
                        .setCancelableOnTouchOutside(true)
                        .setListener(MultiLocationEntityEditActivity.this)
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entity_multi_location);

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity", false);
            this.isCreate = savedInstanceState.getBoolean("isCreate", false);
        }
        else {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
            this.isNewEntity = getIntent().getBooleanExtra("isNewEntity" , false);
            this.isCreate = getIntent().getBooleanExtra("isCreate", false);
            this.isMultiLocations = this.getIntent().getBooleanExtra("isMultiLocations",false);
        }

        if(this.entity != null && this.entity.getPrivilege()!=null)
            this.isSharedPrivilege = this.entity.getPrivilege()>0?true:false;
        else
            this.isSharedPrivilege = false;

        getUIObjects();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity", entity);
        outState.putBoolean("isNewEntity", this.isNewEntity);
        outState.putBoolean("isCreate", this.isCreate);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        if(!isNewEntity)
            textViewTitle.setText("Edit Entity Profile");

        activityRootView = (LinearLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    /*if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                    }*/
                } else {
                    /*if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                    }*/
                }
            }
        });

        //For GAD-1669
        activityRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);

        btnLockProfile = (ImageButton)findViewById(R.id.btnLockProfile);       btnLockProfile.setOnClickListener(this);
        btnSingleLocation = (ImageButton)findViewById(R.id.btnSingleLocation);       btnSingleLocation.setOnClickListener(this);
        btnAddLocation = (ImageButton)findViewById(R.id.btnAddLocation);       btnAddLocation.setOnClickListener(this);

        if (isMultiLocations == false){
            btnSingleLocation.setVisibility(View.VISIBLE);
        }else{
            btnSingleLocation.setVisibility(View.GONE);
        }
        if (entity != null && entity.getEntityInfos().size() > 1){
            btnSingleLocation.setEnabled(true);
            btnSingleLocation.setImageResource(R.drawable.single_location);
        }
        else{
            btnSingleLocation.setEnabled(false);
            btnSingleLocation.setImageResource(R.drawable.single_location_disable);
        }

        btnBack = (Button)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        btnNext = (Button)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        strEntityProfilePhotoPath = entity.getProfileImage();
        EntityImageVO wallpaperImage = entity.getWallpapaerImage();
        if(wallpaperImage != null)
            strEntityWallpaperPhotoPath = wallpaperImage.getUrl();

        imgEntityLogo = (CustomNetworkImageView)findViewById(R.id.imgEntityLogo);
        imgEntityLogo.setDefaultImageResId(R.drawable.entity_add_logo);
        if(entity.getProfileImage() !=null) {
            if (entity.getId() == 0)
                imgEntityLogo.setImageUrl("file://" + strEntityProfilePhotoPath, imgLoader);
            else
                imgEntityLogo.setImageUrl(strEntityProfilePhotoPath, imgLoader);
        }

        imgEntityLogo.setOnClickListener(snapProfilePhotoClickListener);

        imgWallpaper = (CustomNetworkImageView)findViewById(R.id.imgWallpaper);
        imgWallpaper.setDefaultImageResId(R.drawable.add_wallpaper);
        if((wallpaperImage = entity.getWallpapaerImage()) !=null) {
            if (entity.getId() == 0)
                imgWallpaper.setImageUrl("file://" + strEntityWallpaperPhotoPath , imgLoader);
            else
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
        imgProfileVideo.setOnClickListener(snapProfileVideoListener);

        if(entity.getVideoThumbUrl() != null && !entity.getVideoThumbUrl().equals(""))
        {
            imgProfileVideo.setBackground(null);
            imgProfileVideo.setImageUrl(entity.getVideoThumbUrl(), imgLoader);
            imgProfileVideo.invalidate();

            strVideoFilePath = entity.getVideo();
            strVideoFilePath = strVideoFilePath.substring(7, strVideoFilePath.length());
            strVideoThumbPath = entity.getVideoThumbUrl();
            strVideoThumbPath = strVideoThumbPath.substring(7, strVideoThumbPath.length());

            btnPlayVideoButton.setVisibility(View.VISIBLE);
        }
        else
        {
            btnPlayVideoButton.setVisibility(View.GONE);
            imgProfileVideo.invalidate();
        }

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

        if (!isNewEntity){
            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            btnBack.setTextColor(getResources().getColor(R.color.black));
            //btnBack.setBackground(getResources().getDrawable(R.drawable.part_a_btn_back_nav));   For GAD-1160
            textViewTitle.setTextColor(getResources().getColor(R.color.black));
            btnNext.setVisibility(View.INVISIBLE);
            btnDone.setVisibility(View.VISIBLE);
            btnDone.setTextColor(getResources().getColor(R.color.black));
        }else{
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnBack.setTextColor(getResources().getColor(R.color.top_title_text_color));
            //btnBack.setBackground(getResources().getDrawable(R.drawable.btn_back_nav_white));   For GAD-1160
            textViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnNext.setVisibility(View.VISIBLE);
            btnDone.setVisibility(View.INVISIBLE);
            btnNext.setTextColor(getResources().getColor(R.color.top_title_text_color));
        }

        locationListView = (ListView) findViewById(R.id.locationListView);

        locationItems = new ArrayList<LocationsItem>();
        for(int i=0; i< this.entity.getEntityInfos().size(); i++) {

            LocationsItem searchItem = new LocationsItem();

            String tmpLocationName = "";
            for(int j=0;j<entity.getEntityInfos().get(i).getEntityInfoDetails().size();j++)
            {
                if (entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                    tmpLocationName = entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                }
            }
            int count =  this.entity.getEntityInfos().size();
            if (tmpLocationName.toLowerCase().equals("")) {
                searchItem.locationName = "Location #" + count;
                searchItem.isLocation = false;
            }
            else {
                searchItem.locationName = tmpLocationName;
                searchItem.isLocation = true;
            }

            locationItems.add(searchItem);
        }

        //sortItems(locationItems);
        mAdpater = new LocationsListAdapter(MultiLocationEntityEditActivity.this, locationItems);
        locationListView.setAdapter(mAdpater);
        UtiltyDynamicOfHeight.setListViewHeightBasedOnChildren(locationListView);
        locationListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MultiLocationEntityEditActivity.this, AddInfosForEachLocation.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("entity", entity);
                bundle.putSerializable("isNewEntity", isNewEntity);
                bundle.putSerializable("isCreate", isCreate);
                bundle.putInt("currentIndex", position);
                intent.putExtras(bundle);
                startActivityForResult(intent, STATIC_INTEGER_VALUE);
            }
        });

        updateLockButton();

    }

    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if(entity.getId() != 0)
            super.onBackPressed();
    }

    private void goToNext()
    {
        if(progressDialog != null)
            progressDialog.dismiss();

        if (entity.getEntityInfos().size() > 1){
            //Intent entityMultiLocationsPreviewIntent = new Intent(MultiLocationEntityEditActivity.this , EntityMultiLocationsPreviewActivity.class);
            Intent entityMultiLocationsPreviewIntent = new Intent(MultiLocationEntityEditActivity.this , EntityMultiLocationsPreviewAfterEditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("entity", entity);
            entityMultiLocationsPreviewIntent.putExtras(bundle);
            startActivity(entityMultiLocationsPreviewIntent);
            finish();
        }else
        {
            Intent entityProfilePreviewIntent = new Intent(MultiLocationEntityEditActivity.this , EntityProfilePreviewAfterEditAcitivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("entity", entity);
            bundle.putBoolean("isMultiLocations",false);
            entityProfilePreviewIntent.putExtras(bundle);
            startActivity(entityProfilePreviewIntent);
            finish();
        }
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

    private void goToExist()
    {
        if (entity.getEntityInfos().size() > 1){
            //Intent entityMultiLocationsPreviewIntent = new Intent(MultiLocationEntityEditActivity.this , EntityMultiLocationsPreviewActivity.class);
            Intent entityMultiLocationsPreviewIntent = new Intent(MultiLocationEntityEditActivity.this , EntityMultiLocationsPreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("entity", entity);
            entityMultiLocationsPreviewIntent.putExtras(bundle);
            startActivity(entityMultiLocationsPreviewIntent);
            finish();
        }else
        {
            Intent entityProfilePreviewIntent = new Intent(MultiLocationEntityEditActivity.this , EntityProfilePreviewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("entity" , entity);
            bundle.putBoolean("isMultiLocations",false);
            entityProfilePreviewIntent.putExtras(bundle);
            startActivity(entityProfilePreviewIntent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
                if(mAdpater != null) {
                    mAdpater.clearAll();
                    mAdpater.addItems(locationItems);
                }

                finish();
                break;
            case R.id.btnNext:
            case R.id.btnDone:
                if (isRequestBack == true)
                    gotToDoneOrNext();
                break;

            //add profile field items

            //lock & unlock profile
            case R.id.btnLockProfile:
                isSharedPrivilege = !isSharedPrivilege;
                updateLockButton();
                if(!isSharedPrivilege) {
                    final Toast toast = Toast.makeText(MultiLocationEntityEditActivity.this, "Entity is private.", Toast.LENGTH_SHORT);
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
                    final Toast toast = Toast.makeText(MultiLocationEntityEditActivity.this, "Entity is public.", Toast.LENGTH_SHORT);
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
            // button to be back multilocations
            case R.id.btnSingleLocation:
                AlertDialog.Builder builder = new AlertDialog.Builder(MultiLocationEntityEditActivity.this);
                builder.setTitle("");
                //builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_location_address));
                builder.setMessage("Do you want to revert back to single location?");
                builder.setNegativeButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        finish();
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO

                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            //Add location
            case R.id.btnAddLocation:
                LocationsItem item = new LocationsItem();

                int count = mAdpater.getCount() + 1;
                item.locationName = "Location #" + count;
                mAdpater.addItem(item);
                UtiltyDynamicOfHeight.setListViewHeightBasedOnChildren(locationListView);

                mAdpater.notifyDataSetChanged();

                EntityInfoVO entityInfoVO = new EntityInfoVO();
                entity.getEntityInfos().add(entityInfoVO);

                Intent intent = new Intent(MultiLocationEntityEditActivity.this, AddInfosForEachLocation.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("entity", entity);
                bundle.putSerializable("isNewEntity", isNewEntity);
                bundle.putSerializable("isCreate", isCreate);
                bundle.putInt("currentIndex", mAdpater.getCount()-1);
                intent.putExtras(bundle);
                startActivityForResult(intent, STATIC_INTEGER_VALUE);

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
                    String videoFilePath = data.getStringExtra("strMoviePath");
                    final String videoThumbFilePath = data.getStringExtra("strThumbPath");
                    boolean isHistory = data.getBooleanExtra("isHistory", false);
                    if(isHistory)
                    {
                        entity.setVideo(videoFilePath);
                        entity.setVideoThumbUrl(videoThumbFilePath);

                        imgProfileVideo.refreshOriginalBitmap();
                        imgProfileVideo.setImageUrl(videoThumbFilePath, imgLoader);
                        imgProfileVideo.invalidate();

                        btnPlayVideoButton.setVisibility(View.VISIBLE);
                    } else {
                        if (entity.getId() == 0) {
                            strVideoFilePath = videoFilePath;
                            strVideoThumbPath = videoThumbFilePath;

                            entity.setVideo("file://"+videoFilePath);
                            entity.setVideoThumbUrl("file://"+videoThumbFilePath);

                            imgProfileVideo.refreshOriginalBitmap();
                            imgProfileVideo.setImageUrl("file://"+videoThumbFilePath, imgLoader);
                            imgProfileVideo.invalidate();

                            btnPlayVideoButton.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (videoFilePath != null || videoFilePath.equals("") == false) {
                            EntityRequest.uploadVideo(entity.getId(), new File(videoFilePath), new File(videoThumbFilePath), true, new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
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
                                        MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
                                    }
                                }
                            });
                        }
                    }
                    break;
                case STATIC_INTEGER_VALUE:
                    if(data!=null) {

                        entity = (EntityVO) data.getSerializableExtra("entity");
                        currentIndedOfItem = data.getIntExtra("currentIndex",0);
                        isDeleted = data.getBooleanExtra("isDeleted", false);
                        isChanged = true;

                        ArrayList<LocationsItem> items =  new ArrayList<LocationsItem>();

                        for (int count = 0 ; count < mAdpater.getCount() ; count ++){
                            items.add((LocationsItem)mAdpater.getItem(count));
                        }

                        if (isDeleted == true){
                            items.remove(currentIndedOfItem);
                            if (currentIndedOfItem < entity.getEntityInfos().size())
                            {
                                if (entity.getEntityInfos().get(currentIndedOfItem).getId() != null &&
                                        entity.getEntityInfos().get(currentIndedOfItem).getId() != 0)
                                {
                                    strDeletedID += String.valueOf(entity.getEntityInfos().get(currentIndedOfItem).getId());
                                    strDeletedID += ",";
                                }
                                entity.getEntityInfos().remove(currentIndedOfItem);
                            }
                        }else{

                            LocationsItem currentItem = new LocationsItem();

                            String tmpLocationName = "";
                            for(int j=0;j<entity.getEntityInfos().get(currentIndedOfItem).getEntityInfoDetails().size();j++)
                            {
                                if (entity.getEntityInfos().get(currentIndedOfItem).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                                    tmpLocationName = entity.getEntityInfos().get(currentIndedOfItem).getEntityInfoDetails().get(j).getValue().toString();
                                }
                            }
                            int count =  this.entity.getEntityInfos().size();
                            if (tmpLocationName == "") {
                                currentItem.locationName = "Location #" + count;
                                currentItem.isLocation = false;
                            }
                            else {
                                currentItem.locationName = tmpLocationName;
                                currentItem.isLocation = true;
                            }

                            items.set(currentIndedOfItem,currentItem);

                            //sortItems(items);
                        }

                        mAdpater.clearAll();
                        mAdpater.addItems(items);

                        if (mAdpater.getCount() > 1){
                            btnSingleLocation.setEnabled(true);
                            btnSingleLocation.setImageResource(R.drawable.single_location);
                        }
                        else{
                            btnSingleLocation.setEnabled(false);
                            btnSingleLocation.setImageResource(R.drawable.single_location_disable);
                        }
                            mAdpater.notifyDataSetChanged();
                            UtiltyDynamicOfHeight.setListViewHeightBasedOnChildren(locationListView);

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

    public void gotToDoneOrNext()  {
        if(edtEntityname.getText().toString().trim().equals(""))
        {
            MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_for_input_name_item , null);
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

        boolean isCompleted = true;

        for (int i = 0 ; i < entity.getEntityInfos().size(); i ++){
            if (entity.getEntityInfos().get(i) == null || entity.getEntityInfos().get(i).isAddressConfirmed() == false)
                isCompleted = false;
        }

        boolean isAllFilled = true;
        boolean isEmailValid = true;

        for (int i = 0 ; i < entity.getEntityInfos().size(); i ++){
            for(int j=0;j<entity.getEntityInfos().get(i).getEntityInfoDetails().size();j++)
            {
                EntityInfoDetailVO infoItem = entity.getEntityInfos().get(i).getEntityInfoDetails().get(j);

                if(infoItem.getValue().equals("")) {
                    isAllFilled = false;
                    break;
                }
                else
                {
                    //check email type
                    if(infoItem.getFieldName().contains("Email"))
                    {
                        if(!isEmailValid(infoItem.getValue()))
                        {
                            isEmailValid = false;
                            break;
                        }
                    }

                }
            }

            if (isEmailValid == false || isAllFilled == false)
                break;
        }

        if (isEmailValid == false)
        {
            MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_invalid_email_address, null);
            return;
        }

        if (isAllFilled == false)
        {
            MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_please_fill_all_fields_location , null);
            return;
        }


        if (isCompleted == false){
            MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_for_is_completed_items , null);
            return;
        }

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
                                if (EntityCategorySelectActivity.getInstance() != null)
                                    EntityCategorySelectActivity.getInstance().finish();
                                EntityRequest.saveEntity(entity, new ResponseCallBack<EntityVO>() {
                                    @Override
                                    public void onCompleted(JsonResponse<EntityVO> response) {
                                        if (response.isSuccess()) {
                                            entity = response.getData();
                                            for (int i = 0; i < entity.getEntityInfos().size(); i++) {
                                                EntityInfoVO location = entity.getEntityInfos().get(i);
                                                if (location.isAddressConfirmed() == false) {
                                                    location.setLatitude(null);
                                                    location.setLongitude(null);
                                                }
                                            }

                                            if (progressDialog == null) {
                                                progressDialog = ProgressHUD.createProgressDialog(MultiLocationEntityEditActivity.this, getResources().getString(R.string.str_uploading_now),
                                                        true, false, null);
                                                progressDialog.show();
                                            } else {
                                                progressDialog.show();
                                            }

                                            if (!strEntityProfilePhotoPath.equals("")) {
                                                File profilePhotoFile = new File(strEntityProfilePhotoPath);
                                                if (profilePhotoFile.exists()) {
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
                                                                //Uitils.alert(MultiLocationEntityEditActivity.this, "Failed to upload profile photo.");
                                                            }

                                                            if (isPaperUploaded == true && isVideoUploaded == true) {
                                                                isRequestBack = true;
                                                                goToNext();
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
                                                                strProfileImagePath = strTempPhotoPath;
                                                                strProfilePhotoUrl = entity.getWallpapaerImage().getUrl();
                                                                strTempPhotoPath = "";

                                                            } else {
                                                                strTempPhotoPath = "";
                                                                strProfileImagePath = "";
                                                                //MyApp.getInstance().showSimpleAlertDiloag(EntityEditActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                                                            }

                                                            if (isPhotoUploaded == true && isVideoUploaded == true) {
                                                                isRequestBack = true;
                                                                goToNext();
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
                                                             //MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
                                                         }

                                                         if (isPhotoUploaded == true && isPaperUploaded == true) {
                                                             isRequestBack = true;
                                                             goToNext();
                                                         }
                                                     }
                                                 });
                                             } else {
                                                 isVideoUploaded = true;
                                             }

                                            if (isVideoUploaded == true && isPhotoUploaded == true && isPaperUploaded == true)
                                            {
                                                isRequestBack = true;
                                                goToNext();
                                            }
                                        }
                                    }
                                });

                            } else {
                                isRequestBack = true;
                                if(progressDialog != null)
                                    progressDialog.dismiss();
                                Uitils.alert(MultiLocationEntityEditActivity.this, "Failed to Create Entity.");
                            }
                        }
                    });
        }
        else
        {
            /*
            JSONArray fieldArray = new JSONArray();

            for (int i = 0; i < entity.getEntityInfos().size(); i++) {
                EntityInfoVO infoItem = entity.getEntityInfos().get(i);
                if (infoItem != null){
                    try {
                        JSONObject jsonData = new JSONObject();
                        jsonData = JsonConverter.object2Json(infoItem);
                        fieldArray.put(jsonData);
                    } catch (JsonConvertException e) {
                        throw new RuntimeException("can't convert the bean to json string. class:");
                    }

                }
            }

            EntityRequest.updateEntity(entity.getId(), entity.getName(), entity.getDescription(), fieldArray.toString(), strDeletedID,
                    null, entity.getPrivilege(), new ResponseCallBack<EntityVO>() {
                        @Override
                        public void onCompleted(JsonResponse<EntityVO> response) throws IOException {
                            if (response.isSuccess()) {
                                entity = response.getData();
                                for (int i = 0; i < entity.getEntityInfos().size(); i++) {
                                    EntityInfoVO location = entity.getEntityInfos().get(i);
                                    if (location.isAddressConfirmed() == false) {
                                        location.setLatitude(null);
                                        location.setLongitude(null);
                                    }
                                }
                                if (isMultiLocations)
                                    goToDone();
                                else {
                                    if (isNewEntity)
                                        goToNext();
                                    else
                                        goToExist();
                                }
                            } else {
                                //Uitils.alert(MultiLocationEntityEditActivity.this, "Failed to save entity info.");
                                Uitils.alert(MultiLocationEntityEditActivity.this, response.getErrorMessage());
                            }
                        }
                    });
            */
            entity.setDeleteIds(strDeletedID);

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
                        if (isMultiLocations)
                            goToDone();
                        else {
                            if (isNewEntity)
                                goToNext();
                            else
                                goToExist();
                        }
                    } else {
                        //Uitils.alert(MultiLocationEntityEditActivity.this, "Failed to save entity info.");
                        Uitils.alert(MultiLocationEntityEditActivity.this, response.getErrorMessage());
                    }
                }
            });

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
        Pattern pattern = null;

        if (pattern == null)
            pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private void goToFilterScreen()
    {
        Intent intent = new Intent(MultiLocationEntityEditActivity.this , PersonalProfilePhotoFilterActivity.class);
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
                            MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_failed_to_upload_photo, null);
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
                            MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_err_upload_photo, null);
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

                    imgProfileVideo.refreshOriginalBitmap();
                    imgProfileVideo.setImageUrl(strVideoThumbPath, imgLoader);
                    imgProfileVideo.invalidate();

                    btnPlayVideoButton.setVisibility(View.GONE);
                    return;
                } else {
                    EntityRequest.removeVideo(entity.getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                entity.setVideo("");
                                entity.setVideoThumbUrl("");

                                imgProfileVideo.refreshOriginalBitmap();
                                imgProfileVideo.setBackgroundResource(R.drawable.add_profile_video);
                                imgProfileVideo.setImageUrl("", imgLoader);
                                imgProfileVideo.invalidate();

                                btnPlayVideoButton.setVisibility(View.GONE);
                            } else
                                MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_failed_to_upload_video, null);
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
                MultiLocationEntityEditActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
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
                MultiLocationEntityEditActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
            }
        }
        else if(index == 2)//remove photo
        {
            if(isTakingProfilePhoto) {
                if (!entity.getProfileImage().equals("")) {
                    if(entity.getId() == 0) {
                        strTempPhotoPath = "";
                        strProfileImagePath = "";
                        strProfilePhotoUrl = "";
                        strEntityProfilePhotoPath = "";
                        imgEntityLogo.refreshOriginalBitmap();
                        imgEntityLogo.setImageUrl(strProfileImagePath, imgLoader);
                        imgEntityLogo.invalidate();
                        entity.setProfileImage("");
                    }else {
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
                                }
                            }
                        });
                    }
                } else {
                    Uitils.alert(MultiLocationEntityEditActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
                }
            }
            else//remove wallpaper photo
            {
                if(entity.getEntityImages() == null || entity.getEntityImages().size() < 1) return;
                EntityImageVO wallpaperImage = entity.getWallpapaerImage();
                if(wallpaperImage == null) return;
                if(entity.getId() == 0) {
                    strTempPhotoPath = "";
                    strProfileImagePath = "";
                    strProfilePhotoUrl = "";
                    strEntityWallpaperPhotoPath = "";
                    entity.setEntityImages(new ArrayList<EntityImageVO>());
                    imgWallpaper.refreshOriginalBitmap();
                    imgWallpaper.setImageUrl("", imgLoader);
                    imgWallpaper.invalidate();
                } else {
                    EntityRequest.removeImage(entity.getId(), wallpaperImage.getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                strTempPhotoPath = "";
                                strProfileImagePath = "";
                                strProfilePhotoUrl = "";
                                strEntityWallpaperPhotoPath = "";
                                entity.setEntityImages(new ArrayList<EntityImageVO>());
                                imgWallpaper.refreshOriginalBitmap();
                                imgWallpaper.setImageUrl("", imgLoader);
                                imgWallpaper.invalidate();
                            } else {
                                MyApp.getInstance().showSimpleAlertDiloag(MultiLocationEntityEditActivity.this, R.string.str_alert_failed_to_remove_wallpaper_photo, null);
                            }
                        }
                    });
                }
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
    private void playEntityProfileVideo(){
        if(entity == null)
            return;
        if(entity.getVideo() == null || entity.getVideo() == null || entity.getVideo().equals(""))
            return;
        try {
            Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
            videoPlayIntent.setDataAndType(Uri.parse(entity.getVideo()), "video/*");
            MultiLocationEntityEditActivity.this.startActivity(videoPlayIntent);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private class LocationsItem {
        public String locationName = "";
        public Boolean isLocation = false;

        public LocationsItem() {

        }

    }
    private class LocationsListAdapter extends BaseAdapter {
        private List<LocationsItem> locationsList;
        private Context mContext = null;

        public LocationsListAdapter(Context context, ArrayList<LocationsItem> items) {
            locationsList = new ArrayList<LocationsItem>();
            this.mContext = context;
            if(items != null) {
                for (int i = 0; i<items.size(); i++)
                    this.locationsList.add(items.get(i));
            }
        }

        public void setListItems(List<LocationsItem> list) {
            this.locationsList = list;
        }

        public void addItem(LocationsItem contactItem)
        {
            locationsList.add(contactItem);
        }

        public void addItems(List<LocationsItem> contactItems)
        {
            for(int i=0;i<contactItems.size();i++)
            {
                locationsList.add(contactItems.get(i));
            }
        }

        public void clearAll() {
            if (locationsList != null)
            {
                try
                {
                    locationsList.clear();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                locationsList = new ArrayList<LocationsItem>();
            }
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return locationsList == null ? 0 : locationsList.size();
        }

        @Override
        public Object getItem(int position) {
            return locationsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LocationsItemView itemView = null;
            LocationsItem item = locationsList.get(position);
            if(itemView == null)
            {
                itemView = new LocationsItemView(mContext , item);
            }
            else
            {
                itemView = (LocationsItemView)convertView;
            }

            itemView.setItem(item);
            itemView.refreshView();

            return itemView;
        }
    }
    private class LocationsItemView extends LinearLayout {
        private Context mContext = null;
        private LocationsItem item;
        private LayoutInflater inflater;

        private TextView locationInfo, txtUncompleted;
        private ImageView imgArrow;

        public LocationsItemView(Context context, LocationsItem contactItem) {
            super(context);
            this.mContext = context;
            this.item = contactItem;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.location_item , this , true);

            locationInfo = (TextView)findViewById(R.id.locationInfo);
            txtUncompleted = (TextView)findViewById(R.id.txtUncompleted);
            imgArrow = (ImageView)findViewById(R.id.ImgArrow);
        }

        public void setItem(LocationsItem contactItem)
        {
            this.item = contactItem;
        }
        public LocationsItem getItem(){return this.item;}

        public void refreshView()
        {
            if (item.isLocation){
                locationInfo.setTextColor(Color.BLACK);
                imgArrow.setImageResource(R.drawable.location_arrow_black);
                txtUncompleted.setVisibility(View.GONE);
            }else{
                locationInfo.setTextColor(Color.LTGRAY);
                imgArrow.setImageResource(R.drawable.location_arrow_grey);
                txtUncompleted.setVisibility(View.GONE);
            }
            locationInfo.setText(item.locationName);


        }
    }

    private final static Comparator<LocationsItem> itemsComparator = new Comparator<LocationsItem>()
    {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(LocationsItem lhs, LocationsItem rhs) {
            return collator.compare(lhs.locationName, rhs.locationName);
        }
    };

    public void sortItems(ArrayList<LocationsItem> items){
        try {
            Collections.sort(items, itemsComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
