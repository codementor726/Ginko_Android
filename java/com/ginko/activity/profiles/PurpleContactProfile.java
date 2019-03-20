package com.ginko.activity.profiles;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.activity.im.VideoViewerActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.fragments.PersonalProfileFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityVO;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.VideoMemberVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PurpleContactProfile extends MyBaseFragmentActivity implements View.OnClickListener,CustomSizeMeasureView.OnMeasureListner,
        ImInputEditTExt.OnEditTextKeyDownListener{

    private final int SAHRE_YOUR_LEAF_ACTIVITY = 2;
    private final int INT_EXTRA_PURPLE = 1133;

    private ImageButton btnPrev;
    private ImageView btnContactSharing , btnChatNav, btnDetails, btnGinkoCall;
    private MyViewPager mPager;
    private ImageButton btnNotePopupClose , btnNotePopupConfirm;
    private LinearLayout homeBarLayout , workBarLayout;
    private ImageView imgHomeBar , imgWorkBar;
    private ImageView imgHomeIcon , imgWorkIcon;
    private TextView txtTitle;

    private CustomSizeMeasureView sizeMeasureView;
    private PopupWindow  notePopupWindow = null;
    private View notePopupView = null;
    private LinearLayout popupRootLayout;

    private ImInputEditTExt edtNotes;

    /* Variables */
    //2016.9.21 Layout Update for Big Profile Show
    private NetworkImageView tiledProfilePhoto;
    private ImageLoader imgLoader;

    private List<Fragment> fragments = new ArrayList<Fragment>();

    private PersonalProfileFragment homeProfileFragment = null;
    private PersonalProfileFragment workProfileFragment = null;
    private int currIndex = 0;
    private MyPagerAdapter pageAdapter = null;
    private MyOnPageChangeListener pageChangeListener;

    private PurpleContactWholeProfileVO myInfo;

    private UserUpdateVO currentGroup;

    private int contactId;

    private int activityHeight = 0;

    private String strNotes = "";

    private String strFullName = "";

    private boolean isFavorite = false;

    private boolean isChat = false;

    private boolean isDirectory = false;

    private boolean isLoading = false;

    private String strResult = "";

    private CustomDialog mCustomDialog;

    private double lattitude = 0.0d , longitude = 0.0d;

    private List<String> phones = new ArrayList<String>();
    private ContactItem myData;

    TextWatcher noteTextWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if(btnNotePopupConfirm.getVisibility() == View.GONE)
                btnNotePopupConfirm.setVisibility(View.VISIBLE);
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purple_contact_profile);

        Intent intent = this.getIntent();

        contactId = Integer.valueOf(intent.getStringExtra("contactID"));
        strFullName = intent.getStringExtra("fullname");
        isFavorite = intent.getBooleanExtra("isFavorite", false);
        isChat = intent.getBooleanExtra("isChatting", false);
        isDirectory = intent.getBooleanExtra("isDirectory", false);
        myInfo = (PurpleContactWholeProfileVO)intent.getSerializableExtra("responseData");
        strResult = intent.getStringExtra("StartActivity");

        homeProfileFragment = PersonalProfileFragment.newInstance("home" , null, isDirectory);
        //myInfo.setIsFavorite(isFavorite);
        getUIObjects();

        if (myInfo != null) {
            strNotes = myInfo.getNotes();
            if (myInfo.getDetectedLocation() != null)
                parseLocation(myInfo.getDetectedLocation());
        }

        if (isDirectory)
            myInfo.setSharingStatus(3);

        if (pageAdapter.getCount() < 2 && myInfo != null & (myInfo.getSharingStatus() == 2 || myInfo.getSharingStatus() == 3))//if work or both shared, then add work profilefragment
        {
            if (myInfo.getWork().getFields().size() > 0) {
                workProfileFragment = PersonalProfileFragment.newInstance("work", myInfo.getWork(), isDirectory);
                workProfileFragment.setFavouriteValue(myInfo.isFavorite());

                sendValueToFrag(false);
                fragments.remove(homeProfileFragment);
                fragments.add(workProfileFragment);
                fragments.add(homeProfileFragment);
                pageAdapter.notifyDataSetChanged();

                if (myInfo.getSharingStatus() == 2)//if only work is shared , then go to work profile
                {
                    currIndex = 0;
                    mPager.setCurrentItem(currIndex);
                    pageChangeListener.onPageSelected(currIndex);
                    updateBottomNavigation();
                }
                if (myInfo.getSharingStatus() == 3)//sharing all
                {
                    mPager.setScanScroll(true);
                }
            } else{
                currIndex = 1;
                mPager.setCurrentItem(currIndex);
                pageChangeListener.onPageSelected(currIndex);
                updateBottomNavigation();
            }

        }
        initView(myInfo);

        if (myInfo != null) {
            if (currIndex == 1) {
                if (myInfo.getHome() == null || myInfo.getHome().getFields().size() < 1) {
                    tiledProfilePhoto.refreshOriginalBitmap();
                    tiledProfilePhoto.setImageUrl("", imgLoader);
                    tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);
                    tiledProfilePhoto.invalidate();
                }

                tiledProfilePhoto.refreshOriginalBitmap();
                tiledProfilePhoto.setImageUrl(myInfo.getHome().getProfileImage(), imgLoader);
                tiledProfilePhoto.invalidate();
            } else
            {
                if (myInfo.getWork() == null || myInfo.getWork().getFields().size() < 1) {
                    tiledProfilePhoto.refreshOriginalBitmap();
                    tiledProfilePhoto.setImageUrl("", imgLoader);
                    tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);
                    tiledProfilePhoto.invalidate();
                }

                tiledProfilePhoto.refreshOriginalBitmap();
                tiledProfilePhoto.setImageUrl(myInfo.getWork().getProfileImage(), imgLoader);
                tiledProfilePhoto.invalidate();
            }
        }

        sendValueToFrag(true);
    }
    private void enableNotePopup()
    {
        // Creating a pop window for emoticons keyboard
        notePopupWindow = new PopupWindow(notePopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) activityHeight, true);
        notePopupView.setFocusable(true);
        notePopupWindow.setAnimationStyle(R.style.AnimationPopup);
        notePopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        notePopupWindow.setFocusable(true);
        notePopupWindow.setOutsideTouchable(true);
        notePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }

    private void parseLocation(String location)
    {
        List<String> parseList = Arrays.asList(location.split(","));
        if (parseList != null && parseList.size() > 1) {
            lattitude = Double.valueOf(parseList.get(0));
            longitude = Double.valueOf(parseList.get(1));
        }
    }

    private void recreateFragment(PurpleContactWholeProfileVO responsedata)
    {

        myInfo = (PurpleContactWholeProfileVO)responsedata;

        if (myInfo != null)
            strNotes = myInfo.getNotes();

        if (workProfileFragment != null && myInfo.getWork().getFields().size() > 0)
            workProfileFragment.setFavorite(myInfo.isFavorite());
        if (homeProfileFragment != null && myInfo.getHome().getFields().size() > 0)
            homeProfileFragment.setFavorite(myInfo.isFavorite());
    }

    private void showHidePopupView(PopupWindow popupWindow , boolean bShown)
    {
        if(popupWindow == null) return;
        try {
            if (!popupWindow.isShowing()) {
                popupWindow.setHeight((int) (activityHeight));
                if (bShown) {
                    popupRootLayout.setVisibility(LinearLayout.INVISIBLE);
                } else {
                    popupRootLayout.setVisibility(LinearLayout.VISIBLE);
                }
                popupWindow.showAtLocation(popupRootLayout, Gravity.BOTTOM, 0, 0);

                {
                    btnNotePopupConfirm.setVisibility(View.GONE);
                    edtNotes.removeTextChangedListener(noteTextWatcher);
                    edtNotes.setText(strNotes);
                    edtNotes.addTextChangedListener(noteTextWatcher);
                }
            } else {
                if (bShown) {
                    popupRootLayout.setVisibility(LinearLayout.INVISIBLE);
                } else {
                    popupRootLayout.setVisibility(LinearLayout.VISIBLE);
                }
                popupWindow.dismiss();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        ////有焦点的时候，让你的PopupWindow显示出来
        if(hasFocus){
            notePopupWindow.showAtLocation(popupRootLayout, Gravity.BOTTOM|Gravity.FILL, 0, 0);
        }
    }*/

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);
        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);

        imgHomeBar = (ImageView)findViewById(R.id.imgHomeBar);
        imgWorkBar = (ImageView)findViewById(R.id.imgWorkBar);

        imgHomeBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myInfo.getHome() != null && myInfo.getHome().getInputableFieldsCount() > 0)
                {
                    currIndex = 1;
                    tiledProfilePhoto.refreshOriginalBitmap();
                    tiledProfilePhoto.setImageUrl(myInfo.getHome().getProfileImage(), imgLoader);
                    tiledProfilePhoto.invalidate();

                    mPager.setCurrentItem(currIndex);
                }
            }
        });

        imgWorkBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myInfo.getWork() != null && myInfo.getWork().getInputableFieldsCount() > 0)
                {
                    currIndex = 0;
                    tiledProfilePhoto.refreshOriginalBitmap();
                    tiledProfilePhoto.setImageUrl(myInfo.getWork().getProfileImage(), imgLoader);
                    tiledProfilePhoto.invalidate();
                    mPager.setCurrentItem(currIndex);
                }
            }
        });



        imgHomeIcon = (ImageView)findViewById(R.id.imgHomeIcon);
        imgWorkIcon = (ImageView)findViewById(R.id.imgWorkIcon);

        homeBarLayout = (LinearLayout)findViewById(R.id.homeBarLayout); homeBarLayout.setOnClickListener(this);
        workBarLayout = (LinearLayout)findViewById(R.id.workBarLayout); workBarLayout.setOnClickListener(this);

        btnGinkoCall = (ImageView)findViewById(R.id.btnGinkoCall); btnGinkoCall.setOnClickListener(this);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnContactSharing = (ImageView)findViewById(R.id.btnContactSharing); btnContactSharing.setOnClickListener(this);
        btnChatNav = (ImageView)findViewById(R.id.btnChatNav); btnChatNav.setOnClickListener(this);
        btnDetails = (ImageView)findViewById(R.id.imgBtnNote); btnDetails.setOnClickListener(this);
        if (isDirectory)
            btnDetails.setVisibility(View.GONE);

        txtTitle = (TextView)findViewById(R.id.textViewTitle);

        if(isChat)

        {
            btnChatNav.setVisibility(View.INVISIBLE);
            btnGinkoCall.setVisibility(View.INVISIBLE);
        }
        else {
            btnChatNav.setVisibility(View.VISIBLE);
            btnGinkoCall.setVisibility(View.VISIBLE);
        }

        notePopupView = getLayoutInflater().inflate(R.layout.contact_profile_add_comment_popup, null);
        btnNotePopupClose = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupClose);btnNotePopupClose.setOnClickListener(this);
        btnNotePopupConfirm = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupConfirm);btnNotePopupConfirm.setOnClickListener(this);
        edtNotes = (ImInputEditTExt)notePopupView.findViewById(R.id.edtNotes);
        edtNotes.registerOnBackKeyListener(this);
        edtNotes.addTextChangedListener(noteTextWatcher);

        //2016.9.21 Update
        if(this.imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        tiledProfilePhoto = (NetworkImageView)findViewById(R.id.tileProfileImage);
        //2016.9.21 Update
        tiledProfilePhoto.refreshOriginalBitmap();
        tiledProfilePhoto.setImageUrl("", imgLoader);
        tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);

        initViewPager();

        updateBottomNavigation();
        updateControlsByDirectory();
    }
    private void sendValueToFrag(boolean isHome)
    {
        Bundle bundle = new Bundle();
        bundle.putInt("contactID", contactId);
        bundle.putBoolean("isDirectory", isDirectory);
        if(isHome) {
            bundle.putString("type", "home");
            homeProfileFragment.setArguments(bundle);
            if (!isDirectory)
                homeProfileFragment.setFavouriteValue(myInfo.isFavorite());
        }
        else {
            bundle.putString("type", "work");
            workProfileFragment.setArguments(bundle);
            if (!isDirectory)
                workProfileFragment.setFavouriteValue(myInfo.isFavorite());
        }
    }

    public void sendFavoriteValue(boolean isFavorite, String type)
    {
        if (isDirectory)
            return;
        if (type == "home") {
            if (myInfo.getWork() != null && myInfo.getWork().getFields().size() > 0)
                workProfileFragment.setFavorite(isFavorite);
        }
        else
        {
            if (myInfo.getHome() != null && myInfo.getHome().getFields().size() > 0)
                homeProfileFragment.setFavorite(isFavorite);
        }
    }

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(false);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        pageChangeListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(pageChangeListener);
        fragments.add(homeProfileFragment);
        //fragments.add(workProfileFragment);
        mPager.setAdapter(pageAdapter);
        currIndex = 0;

        mPager.setCurrentItem(currIndex);

    }
    private void initView(PurpleContactWholeProfileVO info) {
        myInfo = info;
        currIndex = 1;
        currentGroup = info.getHome();
        homeProfileFragment.init(info.getHome());
        if(pageAdapter.getCount()>1){
            currIndex = 0;
            workProfileFragment.init(info.getWork());
        }

        if (myInfo.getHome() == null || myInfo.getHome().getFields().size() == 0)
            imgHomeIcon.setImageResource(R.drawable.btnhome_disabled);
        else
            imgHomeIcon.setImageResource(R.drawable.profile_preview_navi_home_icon);

        if (myInfo.getWork() == null || myInfo.getWork().getFields().size() == 0)
            imgWorkIcon.setImageResource(R.drawable.btnwork_disabled);
        else
            imgWorkIcon.setImageResource(R.drawable.profile_preview_navi_work_icon);

        if (MyApp.getInstance().g_contactIDs != null && MyApp.getInstance().g_contactIDs.contains(contactId))
        {
            myData = MyApp.getInstance().getfromContacts(contactId);
            if (myData != null)
                phones = myData.getPhones();
        }
        updateBottomNavigation();

        final Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                isLoading = true;
            }
        }, 1000);
    }

    private void updateControlsByDirectory()
    {
        if (isDirectory)
        {
            if (MyApp.g_contactIDs.contains(contactId)) {
                btnContactSharing.setVisibility(View.GONE);
                txtTitle.setVisibility(View.VISIBLE);
            }
            else
            {
                btnContactSharing.setVisibility(View.VISIBLE);
                txtTitle.setVisibility(View.GONE);
            }
        }
    }

    private void updateBottomNavigation()
    {
        if(currIndex == 0)
        {
            imgWorkBar.setVisibility(View.VISIBLE);
            imgHomeBar.setVisibility(View.INVISIBLE);
        }
        else
        {
            imgWorkBar.setVisibility(View.INVISIBLE);
            imgHomeBar.setVisibility(View.VISIBLE);
        }

        if (myInfo != null) {
            if (myInfo.getHome() == null || myInfo.getHome().getFields().size() == 0)
                imgHomeIcon.setImageResource(R.drawable.btnhome_disabled);
            else
                imgHomeIcon.setImageResource(R.drawable.profile_preview_navi_home_icon);

            if (myInfo.getWork() == null || myInfo.getWork().getFields().size() == 0)
                imgWorkIcon.setImageResource(R.drawable.btnwork_disabled);
            else
                imgWorkIcon.setImageResource(R.drawable.profile_preview_navi_work_icon);
        } else{
            imgWorkIcon.setImageResource(R.drawable.btnwork_disabled);
            imgHomeIcon.setImageResource(R.drawable.btnhome_disabled);
        }
    }

    private View.OnClickListener btnCloseClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mCustomDialog != null) {
                hideKeyboard(mCustomDialog.getEdtNotes());
                mCustomDialog.bIsKeyBoardVisibled = false;
                mCustomDialog.dismiss();

            }
        }
    };
    private View.OnClickListener btnConfirmClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mCustomDialog != null)
            {
                strNotes = mCustomDialog.getEdtNotes().getText().toString().trim();
                updatePurpleConctactNotes();
                hideKeyboard(mCustomDialog.getEdtNotes());
                mCustomDialog.bIsKeyBoardVisibled = false;
                mCustomDialog.dismiss();
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (!isChat || isDirectory)
            super.onBackPressed();
        else
        {
            Intent returnIntent = new Intent();
            if (workProfileFragment != null && myInfo.getWork().getFields().size() > 0)
                returnIntent.putExtra("isFavorite" , workProfileFragment.getFavorite());
            else if (homeProfileFragment != null && myInfo.getHome().getFields().size() > 0)
                returnIntent.putExtra("isFavorite", homeProfileFragment.getFavorite());
            else
                returnIntent.putExtra("isFavorite", false);
            PurpleContactProfile.this.setResult(Activity.RESULT_OK , returnIntent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnGinkoCall:
                showBottomCallWindow(v);
                break;
            case R.id.btnPrev:
                //if(isLoading)
                if (!isChat)
                    finish();
                else
                {
                    Intent returnIntent = new Intent();
                    if (workProfileFragment != null && myInfo.getWork().getFields().size() > 0)
                        returnIntent.putExtra("isFavorite" , workProfileFragment.getFavorite());
                    else if (homeProfileFragment != null && myInfo.getHome().getFields().size() > 0)
                        returnIntent.putExtra("isFavorite", homeProfileFragment.getFavorite());
                    else
                        returnIntent.putExtra("isFavorite", false);
                    PurpleContactProfile.this.setResult(Activity.RESULT_OK , returnIntent);
                    finish();
                }
                break;

            case R.id.btnContactSharing:
                Intent contactSharingSettingIntent = new Intent(PurpleContactProfile.this , ShareYourLeafActivity.class);
                contactSharingSettingIntent.putExtra("contactID" , String.valueOf(contactId));
                contactSharingSettingIntent.putExtra("contactFullname" , strFullName);
                if (MyApp.g_contactIDs.contains(contactId))
                    contactSharingSettingIntent.putExtra("isUnexchangedContact", false);
                else
                    contactSharingSettingIntent.putExtra("isUnexchangedContact" , true);
                contactSharingSettingIntent.putExtra("isInviteContact", true);
                contactSharingSettingIntent.putExtra("isPendingRequest", true);
                contactSharingSettingIntent.putExtra("isPendingRequest", true);
                contactSharingSettingIntent.putExtra("StartActivity", strResult);
                contactSharingSettingIntent.putExtra("lat", lattitude);
                contactSharingSettingIntent.putExtra("long", longitude);

                startActivityForResult(contactSharingSettingIntent, SAHRE_YOUR_LEAF_ACTIVITY );
                break;

            //go to chat screen
            case R.id.btnChatNav:
                Intent intent = new Intent(PurpleContactProfile.this , ImBoardActivity.class);
                intent.putExtra("contact_name", strFullName);
                intent.putExtra("contact_ids", contactId + "");
                intent.putExtra("PurpleContact", true);
                if (isDirectory)
                    intent.putExtra("isDirectory", true);
                startActivityForResult(intent, INT_EXTRA_PURPLE);
                break;

            //case R.id.imgHome:
            case R.id.workBarLayout:
                if(myInfo != null && myInfo.getHome()!=null && myInfo.getHome().getFields().size()>0 && myInfo.getSharingStatus()!=2 )
                    mPager.setCurrentItem(0);

                break;
            //case R.id.imgWork:
            case R.id.homeBarLayout:
                if(pageAdapter.getCount() > 1 && myInfo.getHome().getFields().size()>0) {
                    mPager.setCurrentItem(1);
                }
                break;

            //set notes
            case R.id.imgBtnNote:
                /*edtNotes.removeTextChangedListener(noteTextWatcher);
                edtNotes.setText(strNotes);
                edtNotes.addTextChangedListener(noteTextWatcher);
                showKeyboard();

                showHidePopupView(notePopupWindow, true);*/
                mCustomDialog = new CustomDialog(this, btnCloseClickListener, btnConfirmClickListener);
                mCustomDialog.show();
                break;

            //play video
            case R.id.imgBtnPlayVideo:
                if(currentGroup == null) return;
                if(currentGroup.hasVideo() && !currentGroup.getVideoUrl().equals("")) {
                    Intent videoPlayIntent = new Intent(PurpleContactProfile.this, VideoViewerActivity.class);
                    videoPlayIntent.putExtra("video_uri", currentGroup.getVideoUrl());
                    startActivity(videoPlayIntent);


                }
                break;

            case R.id.btnNotePopupClose:
                showHidePopupView(notePopupWindow, false);
                break;

            case R.id.btnNotePopupConfirm:
                updatePurpleConctactNotes();
                showHidePopupView(notePopupWindow, false);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SAHRE_YOUR_LEAF_ACTIVITY:
                    if (data != null) {
                        if (data.getBooleanExtra("isContactDeleted", false)) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("isContactDeleted", true);
                            PurpleContactProfile.this.setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                    break;
                case INT_EXTRA_PURPLE:
                    /*
                    if (data != null) {
                        boolean isFavorite = data.getBooleanExtra("isFavorite", false);
                        if (workProfileFragment != null && myInfo.getWork().getFields().size() > 0)
                            workProfileFragment.setFavorite(isFavorite);
                        if (homeProfileFragment != null && myInfo.getHome().getFields().size() > 0)
                            homeProfileFragment.setFavorite(isFavorite);
                    }
                    */
                    if (!isDirectory) {
                        UserRequest.getContactDetail(String.valueOf(contactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                            @Override
                            public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                if (response.isSuccess()) {
                                    PurpleContactWholeProfileVO responsedata = response.getData();
                                    recreateFragment(responsedata);
                                }
                            }
                        });
                    }
                    break;
            }
        }
    }

    private void showBottomCallWindow(View v)
    {
        if (myData == null)
            return;

        if (!phones.contains("Ginko Video Call"))
            phones.add(0, "Ginko Video Call");
        if (!phones.contains("Ginko Voice Call"))
            phones.add(0, "Ginko Voice Call");

        /////////////////////////////////////////////////////////////////
        if (phones.size() == 1) {
            Uitils.alert("Oops! No registered phone numbers.");
            return;
        }
        if (phones.size() > 2) {
            final List<String> buttons = phones;
            final BottomPopupWindow popupWindow = new BottomPopupWindow(PurpleContactProfile.this, buttons);
            popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                @Override
                public void onClick(View button, int position) {
                    String text = buttons.get(position);
                    if (text == "Cancel") {
                        popupWindow.dismiss();
                    } else if (text == "Ginko Video Call") {
                        EventUser newUser = new EventUser();
                        newUser.setFirstName(myData.getFirstName());
                        newUser.setLastName(myData.getLastName());
                        newUser.setPhotoUrl(myData.getProfileImage());
                        newUser.setUserId(myData.getContactId());
                        CreateVideoVoiceConferenceBoard(String.valueOf(myData.getContactId()), newUser, 1);
                    } else if (text == "Ginko Voice Call") {
                        EventUser newUser = new EventUser();
                        newUser.setFirstName(myData.getFirstName());
                        newUser.setLastName(myData.getLastName());
                        newUser.setPhotoUrl(myData.getProfileImage());
                        newUser.setUserId(myData.getContactId());
                        CreateVideoVoiceConferenceBoard(String.valueOf(myData.getContactId()), newUser, 2);
                    } else {
                        if (!checkSimCard(PurpleContactProfile.this))
                            return;
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + text));
                        startActivity(intent);
                    }
                }
            });
            popupWindow.show(v);
        } else {
            if (!checkSimCard(PurpleContactProfile.this))
                return;
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phones.get(0)));
            startActivity(intent);
        }
    }

    public void CreateVideoVoiceConferenceBoard(String userIds, final EventUser candidate, final int callType) {
        IMRequest.createBoard(String.valueOf(userIds), new ResponseCallBack<ImBoardVO>() {
            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    ImBoardVO board = response.getData();
                    int boardId = board.getBoardId();

                    EventUser ownUser = new EventUser();
                    ownUser.setFirstName(RuntimeContext.getUser().getFirstName());
                    ownUser.setLastName(RuntimeContext.getUser().getLastName());
                    ownUser.setPhotoUrl(RuntimeContext.getUser().getPhotoUrl());
                    ownUser.setUserId(RuntimeContext.getUser().getUserId());

                    ArrayList<EventUser> listTemp = new ArrayList<EventUser>();
                    listTemp.add(candidate);
                    listTemp.add(ownUser);

                    MyApp.getInstance().isOwnerForConfernece = true;

                    MyApp.getInstance().initializeVideoVariables();
                    VideoMemberVO currMember = new VideoMemberVO();
                    currMember.setUserId(String.valueOf(RuntimeContext.getUser().getUserId()));
                    currMember.setName(RuntimeContext.getUser().getFirstName());
                    currMember.setOwner(true);
                    currMember.setMe(true);
                    currMember.setWeight(0);
                    currMember.setInitialized(true);
                    if (callType == 1)
                        currMember.setVideoStatus(true);
                    else
                        currMember.setVideoStatus(false);
                    currMember.setVoiceStatus(true);

                    MyApp.getInstance().g_currMemberCon = currMember;

                    VideoMemberVO otherMember = new VideoMemberVO();
                    otherMember.setUserId(String.valueOf(candidate.getUserId()));
                    otherMember.setName(candidate.getFirstName());
                    otherMember.setImageUrl(candidate.getPhotoUrl());
                    otherMember.setOwner(false);
                    otherMember.setMe(false);
                    otherMember.setWeight(1);
                    otherMember.setYounger(true);
                    otherMember.setInitialized(true);
                    if (callType == 1)
                        otherMember.setVideoStatus(true);
                    else
                        otherMember.setVideoStatus(false);
                    otherMember.setVoiceStatus(true);

                    MyApp.getInstance().g_videoMemberList.add(currMember);
                    MyApp.getInstance().g_videoMemberList.add(otherMember);
                    MyApp.getInstance().g_videoMemIDs.add(currMember.getUserId());
                    MyApp.getInstance().g_videoMemIDs.add(otherMember.getUserId());

                    Intent groupVideoIntent = new Intent(PurpleContactProfile.this, GroupVideoChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", boardId);
                    bundle.putInt("callType", callType);
                    bundle.putString("conferenceName", otherMember.getName());
                    bundle.putSerializable("userData", listTemp);
                    bundle.putBoolean("isInitial", true);
                    groupVideoIntent.putExtras(bundle);
                    startActivity(groupVideoIntent);
                }
            }
        });
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

    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        if(edtText!=null) {
            InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void refreshFragments(String type){

    }
    //press back key on note edittext to hide the keyboard and note popup window
    @Override
    public void onImEditTextBackKeyDown() {
        if (notePopupWindow.isShowing()) {
            hideKeyboard(edtNotes);
            showHidePopupView(notePopupWindow, false);
        }
    }

    @Override
    public void onViewSizeMeasure(int width, int height) {
        //get acitivty height
        activityHeight = height;
        System.out.println("----Activity Height = " + String.valueOf(height) + "-----");

        if(notePopupWindow == null)
            enableNotePopup();
    }

    private void updatePurpleConctactNotes()
    {
        CBRequest.addNotes(contactId , strNotes , new ResponseCallBack<Void>(){
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if(response.isSuccess())
                {
                    strNotes = mCustomDialog.getEdtNotes().getText().toString().trim();
                }
            }
        });
    }
    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> infos;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> infos) {
            super(fm);
            this.infos = infos;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
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
            if (currentGroup == null) {
                return;
            }
            pageAdapter.getItem(currIndex).onResume();

            if (myInfo != null) {
                if (currIndex == 0) {
                    if (myInfo.getWork() == null || myInfo.getWork().getFields().size() < 1) {
                        tiledProfilePhoto.refreshOriginalBitmap();
                        tiledProfilePhoto.setImageUrl("", imgLoader);
                        tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);
                        tiledProfilePhoto.invalidate();
                    }

                    tiledProfilePhoto.refreshOriginalBitmap();
                    tiledProfilePhoto.setImageUrl(myInfo.getWork().getProfileImage(), imgLoader);
                    tiledProfilePhoto.invalidate();
                } else
                {
                    if (myInfo.getHome() == null || myInfo.getHome().getFields().size() < 1) {
                        tiledProfilePhoto.refreshOriginalBitmap();
                        tiledProfilePhoto.setImageUrl("", imgLoader);
                        tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);
                        tiledProfilePhoto.invalidate();
                    }

                    tiledProfilePhoto.refreshOriginalBitmap();
                    tiledProfilePhoto.setImageUrl(myInfo.getHome().getProfileImage(), imgLoader);
                    tiledProfilePhoto.invalidate();
                }
            }

            updateBottomNavigation();
            currentGroup = currIndex==0? myInfo.getHome(): myInfo.getWork();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
    public class CustomDialog extends Dialog implements ImInputEditTExt.OnEditTextKeyDownListener {
        private ImInputEditTExt edtNotes;
        private ImageButton btnClose;
        private ImageButton btnConfirm;
        private View.OnClickListener mCloseClickListener;
        private View.OnClickListener mConfirmClickListener;
        private RelativeLayout rootLayout;
        private RelativeLayout rlHeaderLayout;
        private RelativeLayout rlBodyLayout;
        public boolean bIsKeyBoardVisibled;
        private int screenHeight;
        private int rlHeaderLayoutHeight;
        private int nEdtNoteHeight;
        private int nBodyLaoytHeight;
        private boolean bIsControlStarted;  // If user started the Edit to the EdtNotes
        private Context mContext;

        public CustomDialog(Context context, View.OnClickListener close, View.OnClickListener confirm) {
            //set notes for following entity
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
            mContext = context;
            mCloseClickListener = close;
            mConfirmClickListener = confirm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.8f;
            getWindow().setAttributes(lpWindow);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            setContentView(R.layout.contact_profile_add_comment_popup);
            setLayout();
            setClickListener(mCloseClickListener, mConfirmClickListener);
            //set entity notes
            rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

            bIsKeyBoardVisibled = false;
            rlHeaderLayout = (RelativeLayout) findViewById(R.id.headerlayout);
            rlBodyLayout = (RelativeLayout) findViewById(R.id.bodyLayout);


            rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    rootLayout.getWindowVisibleDisplayFrame(r);
                    screenHeight = rootLayout.getRootView().getHeight();
                    rlHeaderLayoutHeight = rlHeaderLayout.getHeight();

                    if (nEdtNoteHeight == 0)
                        nEdtNoteHeight = screenHeight - (int) (rlHeaderLayoutHeight * 1.5);
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.15 && bIsKeyBoardVisibled == false) { // 0.15 ratio is perhaps enough to determine keypad height.
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        edtNotes.setHeight(screenHeight - keypadHeight - (int) (rlHeaderLayoutHeight * 1.5));
                        edtNotes.setMaxHeight(screenHeight - keypadHeight - (int) (rlHeaderLayoutHeight * 1.5));
                        bIsKeyBoardVisibled = true;
                    } else if (keypadHeight == 0) {
                        if (nEdtNoteHeight > 0) {
                            edtNotes.setHeight(nEdtNoteHeight);
                            edtNotes.setMaxHeight(nEdtNoteHeight);
                        }
                    }
                }
            });
        }

        private void setLayout() {
            edtNotes = (ImInputEditTExt) findViewById(R.id.edtNotes);
            btnClose = (ImageButton) findViewById(R.id.btnNotePopupClose);
            btnConfirm = (ImageButton) findViewById(R.id.btnNotePopupConfirm);
            btnConfirm.setVisibility(View.GONE);
            rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
            nEdtNoteHeight = 0;
            bIsControlStarted = false;

            edtNotes.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        bIsControlStarted = true;
                    return false;
                }
            });
            edtNotes.setText(strNotes);
            edtNotes.registerOnBackKeyListener(this);
            edtNotes.setSelection(edtNotes.length());

            bIsKeyBoardVisibled = false;

            edtNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    btnConfirm.setVisibility(View.GONE);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    bIsControlStarted = true;
                    if (btnConfirm.getVisibility() == View.GONE)
                        btnConfirm.setVisibility(View.VISIBLE);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        private void setClickListener(View.OnClickListener close, View.OnClickListener confirm) {
            if (close != null && confirm != null) {
                btnClose.setOnClickListener(close);
                btnConfirm.setOnClickListener(confirm);
            }
        }

        public ImInputEditTExt getEdtNotes() {
            return edtNotes;
        }

        @Override
        public void onImEditTextBackKeyDown() {
            if(bIsKeyBoardVisibled) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                hideKeyboard(edtNotes);
                bIsKeyBoardVisibled = false;
            } else {
                //hideKeyboard(edtNotes);
                //mCustomDialog.dismiss();
            }
        }
    }
}
