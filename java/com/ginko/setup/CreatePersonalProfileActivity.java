package com.ginko.setup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
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
import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.HomeWorkAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.TcVideoVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePersonalProfileActivity extends MyBaseFragmentActivity implements
                                    View.OnClickListener ,
                                    ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener ,
                                    ActionSheet.ActionSheetListener{

    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int FILTER_PHOTO = 6;

    /*  UI Objects */
    private LinearLayout activityRootView;
    private Button btnNext;
    private ProfileFieldAddOverlayView addFieldOverlayView;
    private TextView textViewTitle;
    private ImageView imgDimBackground;
    private ImageView btnAddProfileField;
    private ImageButton btnAddHomeWorkProfile ,btnRemoveHomeWorkProfile , btnLockProfile;

    private NetworkImageView imgPersonalProfilePhoto;
    private CustomNetworkImageView imgWallpaper , imgProfileVideo;
    private RelativeLayout videoLayout;
    private ImageView btnPlayVideoButton;
    private EditText edtFullName;

    /* Variables */
    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";

    private final int GROUP_HOME = 1;
    private final int GROUP_WORK = 2;

    private final int TAKE_VIDEO_FROM_CAMERA = 2211;

    private int groupType = GROUP_HOME;
    private String type;
    private String createProfileMode = "home";
    private UserWholeProfileVO userInfo; //whole user profile info
    private UserUpdateVO groupInfo;//home or work profile

    private ImageLoader imgLoader;

    private boolean isPublicLocked = true;
    private boolean isKeyboardVisible = false;
    private boolean isNewEntity = false;

    private HomeWorkAddInfoFragment infoListFragment;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" ;

    private Uri uri;
    private String tempPhotoUriPath = "";

    private String strProfilePhotoUrl = "";

    private ActionSheet takePhotoActionSheet = null;
    private ActionSheet takeVideoActionSheet = null;

    private boolean isTakingProfilePhoto = true;

    private UserUpdateVO newGroupInfo = null;

    private String mTempDirectory = RuntimeContext.getAppDataFolder("temp");
    private boolean isProfileVideo = false;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;
            if(groupInfo == null) return;
            //if(takePhotoActionSheet == null)
            if(groupInfo.getProfileImage() == null || groupInfo.getProfileImage().equals("")) {
                takePhotoActionSheet = ActionSheet.createBuilder(CreatePersonalProfileActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(CreatePersonalProfileActivity.this)
                        .show();
            }
            else
            {
                takePhotoActionSheet = ActionSheet.createBuilder(CreatePersonalProfileActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(CreatePersonalProfileActivity.this)
                        .show();
            }
            //else
            //    takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    private View.OnClickListener snapWallpaperPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = false;
            if(groupInfo == null) return;
            //if(takePhotoActionSheet == null)
            if(groupInfo.getWallpapaerImage() == null) {
                takePhotoActionSheet = ActionSheet.createBuilder(CreatePersonalProfileActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(CreatePersonalProfileActivity.this)
                        .show();
            }
            else
            {
                takePhotoActionSheet = ActionSheet.createBuilder(CreatePersonalProfileActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_take_wallpaper_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.personal_profile_remove_wallpaper_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(CreatePersonalProfileActivity.this)
                        .show();
            }
            //else
            //    takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
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

                Intent getFilteredVideo = new Intent(CreatePersonalProfileActivity.this, VideoSetActivity.class);
                getFilteredVideo.putExtra("typeId", "personalInfo");
                getFilteredVideo.putExtra("isHome", groupType);
                getFilteredVideo.putExtra("isNewEntity", true);
                startActivityForResult(getFilteredVideo, TAKE_VIDEO_FROM_CAMERA);
            }
            else
            {
                isProfileVideo = true;
                takeVideoActionSheet = ActionSheet.createBuilder(CreatePersonalProfileActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.personal_profile_remove_video),
                                getResources().getString(R.string.personal_profile_play_video))
                        .setCancelableOnTouchOutside(true)
                        .setListener(CreatePersonalProfileActivity.this)
                        .show();
            }
            //else
            //    takeWallpaperPhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_personal_profile);

        if(savedInstanceState!= null)
        {
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
            this.createProfileMode = savedInstanceState.getString("createProfileMode", "home");
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            isNewEntity = bundle.getBoolean("isNewEntity");
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
            this.createProfileMode = bundle.getString("createProfileMode" , "home");
        }

        if(type.equals(GROUP_TYPE_HOME)) {
            groupType = GROUP_HOME;
            groupInfo = userInfo.getHome();
        }
        else {
            groupType = GROUP_WORK;
            groupInfo = userInfo.getWork();
        }

        isPublicLocked = !groupInfo.isPublic();

        getUIObjects();
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
            CreatePersonalProfileActivity.this.startActivity(videoPlayIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TYPE_PARAM, type);
        outState.putSerializable(USER_INFO_PARAM, userInfo);
        outState.putString("createProfileMode", this.createProfileMode);
    }
    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnAddHomeWorkProfile = (ImageButton)findViewById(R.id.btnAddHomeWorkProfile); btnAddHomeWorkProfile.setOnClickListener(this);
        btnRemoveHomeWorkProfile = (ImageButton)findViewById(R.id.btnRemoveHomeWorkProfile); btnRemoveHomeWorkProfile.setOnClickListener(this);

        btnNext = (Button)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);

        addFieldOverlayView = (ProfileFieldAddOverlayView)findViewById(R.id.addFieldOverlayView);
        addFieldOverlayView.setOnProfileFieldItemsChangeListener(this); addFieldOverlayView.setVisibility(View.GONE);

        btnAddProfileField = (ImageView)findViewById(R.id.btnAddFieldInfoItem); btnAddProfileField.setOnClickListener(this);
        btnLockProfile = (ImageButton)findViewById(R.id.btnLockProfile);       btnLockProfile.setOnClickListener(this);

        imgDimBackground = (ImageView)findViewById(R.id.imgDimBackground);
        imgDimBackground.setVisibility(View.GONE);

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

        activityRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);
            }
        });
        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        if(groupType == GROUP_HOME)
        {
            textViewTitle.setText(getResources().getString(R.string.str_personal));
        }
        else
        {
            textViewTitle.setText(getResources().getString(R.string.str_profile_work));
        }

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
        /*btnPlayVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupInfo == null)
                    return;
                if(groupInfo.getVideo() == null || groupInfo.getVideo() == null || groupInfo.getVideo().getVideo_url().equals(""))
                    return;
                try {
                    Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
                    videoPlayIntent.setDataAndType(Uri.parse(groupInfo.getVideo().getVideo_url()), "video*//*");
                    CreatePersonalProfileActivity.this.startActivity(videoPlayIntent);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreatePersonalProfileActivity.this , "Profile video coming soon." , Toast.LENGTH_LONG).show();
            }
        });*/

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

        initInfoItemFragment();

        updateLockButton();

        updateAddRemoveHomeWorkButton();
    }

    private void updateAddRemoveHomeWorkButton()
    {
        if(this.createProfileMode.equalsIgnoreCase("both"))
        {
            //if create both of home and work ,then hide the add home/work button
            btnRemoveHomeWorkProfile.setVisibility(View.VISIBLE);
            btnAddHomeWorkProfile.setVisibility(View.GONE);
            if(groupType == GROUP_HOME)
            {
                btnRemoveHomeWorkProfile.setVisibility(View.VISIBLE);
                btnRemoveHomeWorkProfile.setImageResource(R.drawable.remove_home_profile_button);
            }
            else
            {
                btnRemoveHomeWorkProfile.setVisibility(View.VISIBLE);
                btnRemoveHomeWorkProfile.setImageResource(R.drawable.remove_work_profile_button);
            }
        }
        else if(this.createProfileMode.equalsIgnoreCase("home"))
        {
            btnAddHomeWorkProfile.setVisibility(View.VISIBLE);
            btnAddHomeWorkProfile.setImageResource(R.drawable.add_work_profile_button);

            btnRemoveHomeWorkProfile.setVisibility(View.GONE);
        }
        else if(this.createProfileMode.equalsIgnoreCase("work"))
        {
            btnAddHomeWorkProfile.setVisibility(View.VISIBLE);
            btnAddHomeWorkProfile.setImageResource(R.drawable.add_home_profile_button);

            btnRemoveHomeWorkProfile.setVisibility(View.GONE);
        }
    }

    private void initInfoItemFragment()
    {
        String strFullName = userInfo.getFirstName()==null?"": userInfo.getFirstName();

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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnNext:
                if(edtFullName.getText().toString().trim().equals(""))
                {
                    MyApp.getInstance().showSimpleAlertDiloag(CreatePersonalProfileActivity.this, R.string.str_alert_for_input_name_item , null);
                    return;
                }
                newGroupInfo = null;
                if(infoListFragment != null)
                    newGroupInfo = infoListFragment.saveGroupInfo(CreatePersonalProfileActivity.this , isPublicLocked , true);

                if(newGroupInfo == null)
                {
                    return;
                }

                if(newGroupInfo.getFields() == null)
                    newGroupInfo.setFields(new ArrayList<UserProfileVO>());
                UserProfileVO nameField = new UserProfileVO();
                nameField.setFieldName("Name");
                nameField.setFieldType("name");
                //nameField.setFont("");
                //nameField.setColor("");
                nameField.setPosition("");
                nameField.setValue(edtFullName.getText().toString().trim());
                newGroupInfo.getFields().add(nameField);

                groupInfo.setFields(newGroupInfo.getFields());

                Bundle bundle = new Bundle();
                bundle.putSerializable("userInfo", userInfo);
                bundle.putBoolean("isNewEntity", isNewEntity);
                if(this.createProfileMode.equalsIgnoreCase("both"))
                {
                    if(groupType == GROUP_HOME) //on the home create screen
                    {
                        if(userInfo.getWork() == null || userInfo.getWork().getFields() == null || userInfo.getWork().getFields().size() < 1)
                        {
                            bundle.putString("type", "work");
                            bundle.putString("createProfileMode", "both");
                            Intent intent = new Intent(CreatePersonalProfileActivity.this , CreatePersonalProfileActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            CreatePersonalProfileActivity.this.finish();
                        }
                        else
                        {
                            bundle.putBoolean("isFromCreateProfileScreen", true);
                            Intent intent = new Intent(CreatePersonalProfileActivity.this , PersonalProfilePreviewActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            CreatePersonalProfileActivity.this.finish();
                        }
                    }
                    else//on the work create screen
                    {
                        if(userInfo.getHome() == null || userInfo.getHome().getFields() == null || userInfo.getHome().getFields().size() < 1)
                        {
                            bundle.putString("type", "home");
                            bundle.putString("createProfileMode", "both");
                            Intent intent = new Intent(CreatePersonalProfileActivity.this , CreatePersonalProfileActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            CreatePersonalProfileActivity.this.finish();
                        }
                        else
                        {
                            bundle.putBoolean("isFromCreateProfileScreen", true);
                            Intent intent = new Intent(CreatePersonalProfileActivity.this , PersonalProfilePreviewActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            CreatePersonalProfileActivity.this.finish();
                        }
                    }
                }
                else if(this.createProfileMode.equalsIgnoreCase("home") || this.createProfileMode.equalsIgnoreCase("work"))
                {
                    bundle.putBoolean("isFromCreateProfileScreen", true);
                    Intent intent = new Intent(CreatePersonalProfileActivity.this , PersonalProfilePreviewActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    CreatePersonalProfileActivity.this.finish();
                }
                break;

            //add profile field items
            case R.id.btnAddFieldInfoItem:
                if(infoListFragment == null) return;
                if(isKeyboardVisible)
                    MyApp.getInstance().hideKeyboard(activityRootView);

                addFieldOverlayView.setProfileFieldItems(type, infoListFragment.getCurrentVisibleInfoItems());

                if(addFieldOverlayView.getVisibility() == View.GONE)
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
                    btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
                }
                break;

            //lock & unlock profile
            case R.id.btnLockProfile:
                isPublicLocked = !isPublicLocked;
                updateLockButton();
                if(isPublicLocked) {
                    final Toast toast = Toast.makeText(CreatePersonalProfileActivity.this, "Profile is private.", Toast.LENGTH_SHORT);
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
                    final Toast toast = Toast.makeText(CreatePersonalProfileActivity.this, "Profile is public.", Toast.LENGTH_SHORT);
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

            case R.id.btnAddHomeWorkProfile:
                if(this.createProfileMode.equalsIgnoreCase("both"))
                {
                }
                else if(this.createProfileMode.equalsIgnoreCase("home") || this.createProfileMode.equalsIgnoreCase("work"))
                {
                    DialogInterface.OnClickListener confirmDialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    CreatePersonalProfileActivity.this.createProfileMode = "both";
                                    updateAddRemoveHomeWorkButton();
                                    dialog.dismiss();
                                break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };
                    if(this.createProfileMode.equalsIgnoreCase("home")) {
                        AlertDialog.Builder updateConfirmDialogBuilder = new AlertDialog.Builder(CreatePersonalProfileActivity.this);
                        updateConfirmDialogBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_add_work_profile))
                                .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), confirmDialogClickListener)
                                .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), confirmDialogClickListener).show();
                    }
                    else
                    {
                        AlertDialog.Builder updateConfirmDialogBuilder = new AlertDialog.Builder(CreatePersonalProfileActivity.this);
                        updateConfirmDialogBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_add_personal_profile))
                                .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), confirmDialogClickListener)
                                .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), confirmDialogClickListener).show();
                    }
                }


                break;

            case R.id.btnRemoveHomeWorkProfile:
                String strConfirmMsg = getResources().getString(R.string.str_confirm_delete_profile);
                if(groupType == GROUP_HOME)
                    strConfirmMsg = getResources().getString(R.string.str_confirm_delete_personal_profile);

                final String strCreateProfileMode = createProfileMode;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                builder.setMessage(strConfirmMsg);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        if(strCreateProfileMode.equalsIgnoreCase("both"))
                        {
                            if(groupType == GROUP_HOME)
                            {
                                userInfo.getHome().setFields(new ArrayList<UserProfileVO>());
                                userInfo.getHome().setProfileImage("");
                                userInfo.getHome().setVideo(null);
                                userInfo.getHome().setImages(new ArrayList<TcImageVO>());
                                Bundle bundle1 = new Bundle();
                                bundle1.putSerializable("userInfo", userInfo);
                                bundle1.putString("type", "work");
                                bundle1.putString("createProfileMode", "work");
                                Intent intent1 = new Intent(CreatePersonalProfileActivity.this , CreatePersonalProfileActivity.class);
                                intent1.putExtras(bundle1);
                                startActivity(intent1);
                                CreatePersonalProfileActivity.this.finish();
                            }
                            else
                            {
                                userInfo.getWork().setFields(new ArrayList<UserProfileVO>());
                                userInfo.getWork().setProfileImage("");
                                userInfo.getWork().setVideo(null);
                                userInfo.getWork().setImages(new ArrayList<TcImageVO>());
                                Bundle bundle1 = new Bundle();
                                bundle1.putSerializable("userInfo", userInfo);
                                bundle1.putString("type", "home");
                                bundle1.putString("createProfileMode", "home");
                                Intent intent1 = new Intent(CreatePersonalProfileActivity.this , CreatePersonalProfileActivity.class);
                                intent1.putExtras(bundle1);
                                startActivity(intent1);
                                CreatePersonalProfileActivity.this.finish();
                            }
                        }
                        else if(strCreateProfileMode.equalsIgnoreCase("home"))
                        {
                        }
                        else if(strCreateProfileMode.equalsIgnoreCase("work"))
                        {
                        }
                        updateAddRemoveHomeWorkButton();
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
        }
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

                            imgWallpaper.invalidate();
                            strTempPhotoPath = "";

                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgWallpaper.refreshOriginalBitmap();
                            imgWallpaper.setImageUrl(strProfileImagePath, imgLoader);
                            imgWallpaper.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(CreatePersonalProfileActivity.this, R.string.str_alert_failed_to_upload_photo, null);
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
                                if(strProfileImagePath.startsWith("file://"))
                                    imgPersonalProfilePhoto.setImageUrl(strProfileImagePath, imgLoader);
                                else
                                    imgPersonalProfilePhoto.setImageUrl("file://"+strProfileImagePath, imgLoader);

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
                            MyApp.getInstance().showSimpleAlertDiloag(CreatePersonalProfileActivity.this, R.string.str_err_upload_photo, null);
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
        Intent intent = new Intent(CreatePersonalProfileActivity.this , PersonalProfilePhotoFilterActivity.class);
        intent.putExtra("imagePath" , strTempPhotoPath);
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
        intent.putExtra("saveImagePath" , strTempPhotoPath);
        intent.putExtra("groupType" , type);
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
        startActivityForResult(intent , FILTER_PHOTO);
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
                        String picturePath = "";
                        Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null, null);
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

                        System.out.println("-----Photo Path= "+strTempPhotoPath+"----");

                        goToFilterScreen();

                        /*if(isTakingProfilePhoto)
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
                                        MyApp.getInstance().showSimpleAlertDiloag(CreatePersonalProfileActivity.this, R.string.str_alert_failed_to_upload_video, null);
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
        if(index == 0)//take a photo
        {
            if(isProfileVideo){
                if(groupInfo.getVideo() != null)
                {
                    TradeCard.deleteVideo(groupType, groupInfo.getVideo().getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                TradeCard.deleteArchiveVideo(groupType, groupInfo.getVideo().getId(), new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if(response.isSuccess())
                                            Log.e("ArchiveVideo delete", "Successfully");
                                    }
                                });
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
                CreatePersonalProfileActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
            }
        }
        else if(index == 1) //photo from gallery
        {
            if(isProfileVideo) {
                playProfileVideo();
                isProfileVideo = false;
            }
            else {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                CreatePersonalProfileActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
            }
        }
        else if(index == 2)//remove photo
        {
            if(isTakingProfilePhoto) {
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
                    Uitils.alert(CreatePersonalProfileActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
                }
            }
            else//remove wallpaper photo
            {
                TcImageVO wallpaperImage = null;
                if(groupInfo.getImages() == null || groupInfo.getImages().size() < 1) return;
                for(TcImageVO image : groupInfo.getImages())
                {
                    if(image != null && image.getId() > 0)
                    {
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
                        }
                        else
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(CreatePersonalProfileActivity.this, R.string.str_alert_failed_to_remove_wallpaper_photo, null);
                        }
                    }
                });
            }
        }
    }
}
