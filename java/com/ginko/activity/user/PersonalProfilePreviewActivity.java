package com.ginko.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.PersonalProfileFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.setup.GoToInviteContactScreenConfirmActivity;
import com.ginko.setup.RegisterConfirmationMobileActivity;
import com.ginko.setup.TutorialActivity;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;

import java.util.ArrayList;
import java.util.List;

public class PersonalProfilePreviewActivity extends MyBaseFragmentActivity implements View.OnClickListener{

    private final int EDIT_PROFILE_INTENT = 1;

    /* UI Variables*/
    private Button btnEdit , btnDone;
    private LinearLayout homeBarLayout , workBarLayout;
    private ImageView imgHomeBar , imgWorkBar;
    private ImageView imgHomeIcon , imgWorkIcon;
    private RelativeLayout headerlayout;
    private TextView textViewTitle;

    /* Variables */
    //2016.9.21 Layout Update for Big Profile Show
    private NetworkImageView tiledProfilePhoto;

    /* Variables */
    private UserWholeProfileVO myInfo, originalInfo;
    private UserUpdateVO currentGroup;
    private int currIndex = 0;

    private MyViewPager mPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();

    private MyPagerAdapter pageAdapter = null;
    private PersonalProfileFragment homeProfileFragment = PersonalProfileFragment.newInstance("home" , null, false);
    private PersonalProfileFragment workProfileFragment = PersonalProfileFragment.newInstance("work" , null, false);

    private ImageLoader imgLoader;

    private boolean isFromCreateProfileScreen = false;

    private boolean isLoaded = false;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);

        if(savedInstanceState != null)
        {
            this.originalInfo = (UserWholeProfileVO) savedInstanceState.getSerializable("myInfo");
            this.myInfo = (UserWholeProfileVO) savedInstanceState.getSerializable("myInfo");
            this.isFromCreateProfileScreen = savedInstanceState.getBoolean("isFromCreateProfileScreen" ,false);
        }
        else
        {
            Intent intent = this.getIntent();
            this.originalInfo = (UserWholeProfileVO) intent.getSerializableExtra("myInfo");
            this.myInfo = (UserWholeProfileVO) intent.getSerializableExtra("userInfo");
            this.isFromCreateProfileScreen = intent.getBooleanExtra("isFromCreateProfileScreen", false);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("isUser", 1);
        homeProfileFragment.setArguments(bundle);
        workProfileFragment.setArguments(bundle);

        getUIObjects();

        initViewPager();

        if(myInfo == null) {
            UserInfoRequest.getInfo(new ResponseCallBack<UserWholeProfileVO>() {
                @Override
                public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                    if (response.isSuccess()) {
                        originalInfo = response.getData();
                        myInfo = response.getData();
                        initView(myInfo);
                    }
                }
            });
        }
        else
        {
            initView(myInfo);
        }


    }

    @Override
    public void onBackPressed() {
        if (myInfo == null) {
            super.onBackPressed();
            return;
        }

        if(!isFromCreateProfileScreen && isLoaded) {
            if(homeProfileFragment.isLoaded() || workProfileFragment.isLoaded())
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("myInfo", this.myInfo);
        outState.putBoolean("isFromCreateProfileScreen", this.isFromCreateProfileScreen);
    }

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(true);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        fragments.add(workProfileFragment);
        fragments.add(homeProfileFragment);

        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
    }

    private void initView(UserWholeProfileVO info) {
        myInfo = info;

        if (imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();
        tiledProfilePhoto = (NetworkImageView)findViewById(R.id.tileProfileImage);
        //2016.9.21 Update
        tiledProfilePhoto.refreshOriginalBitmap();
        tiledProfilePhoto.setImageUrl("", imgLoader);
        tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);

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

        if(currIndex == 0 && info.getWork() != null && info.getWork().getFields() != null && info.getWork().getInputableFieldsCount() > 0)
            currIndex = 0;
        else if(currIndex == 0 && info.getWork().getFields().size() == 0)
            currIndex = 1;
        else if(currIndex == 1 && info.getHome() != null && info.getHome().getFields() != null && info.getHome().getInputableFieldsCount() > 0)
            currIndex = 1;
        else if(currIndex == 1 && info.getHome().getFields().size() == 0)
            currIndex = 0;

        if(currIndex == 0)
            currentGroup = info.getWork();
        else
            currentGroup = info.getHome();
        homeProfileFragment.init(myInfo.getHome());
        workProfileFragment.init(myInfo.getWork());


        mPager.setCurrentItem(currIndex);

        updateBottomNavigation();

        isLoaded = true;
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        headerlayout  = (RelativeLayout) findViewById(R.id.headerlayout);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);

        btnEdit = (Button)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);
        if(isFromCreateProfileScreen)
            btnDone.setText(getResources().getString(R.string.done));
        else {
            btnDone.setText(getResources().getString(R.string.done));

            headerlayout.setBackgroundResource(R.color.green_top_titlebar_color);
            btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
            btnEdit.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
            textViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color_purple));
        }
        imgHomeBar = (ImageView)findViewById(R.id.imgHomeBar);
        imgWorkBar = (ImageView)findViewById(R.id.imgWorkBar);

        imgHomeIcon = (ImageView)findViewById(R.id.imgHomeIcon);
        imgWorkIcon = (ImageView)findViewById(R.id.imgWorkIcon);

        homeBarLayout = (LinearLayout)findViewById(R.id.homeBarLayout); homeBarLayout.setOnClickListener(this);
        workBarLayout = (LinearLayout)findViewById(R.id.workBarLayout); workBarLayout.setOnClickListener(this);

        imgLoader = MyApp.getInstance().getImageLoader();

        updateBottomNavigation();
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
        if(myInfo != null)
        {
            if(myInfo.getHome() != null && myInfo.getHome().getInputableFieldsCount() > 0)
            {
                imgHomeIcon.setImageResource(R.drawable.profile_preview_navi_home_icon);
            }
            else
            {
                imgHomeIcon.setImageResource(R.drawable.profile_preview_navi_add_home_icon);
            }

            if(myInfo.getWork() != null && myInfo.getWork().getInputableFieldsCount() > 0)
            {
                imgWorkIcon.setImageResource(R.drawable.profile_preview_navi_work_icon);
            }
            else
            {
                imgWorkIcon.setImageResource(R.drawable.profile_preview_navi_add_work_icon);
            }
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
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnEdit:
                if(myInfo == null)
                {
                    MyApp.getInstance().showSimpleAlertDiloag(PersonalProfilePreviewActivity.this, "Internet Connection is missing", null);
                    return;
                }

                if(currIndex == 0)//work
                {
                    Intent profileEditIntent = new Intent(PersonalProfilePreviewActivity.this , PersonalProfileEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", myInfo);
                    bundle.putString("type", "work");
                    bundle.putBoolean("isRegister", !isFromCreateProfileScreen);
                    profileEditIntent.putExtras(bundle);
                    startActivityForResult(profileEditIntent, EDIT_PROFILE_INTENT);
                }
                else //home
                {
                    Intent profileEditIntent = new Intent(PersonalProfilePreviewActivity.this , PersonalProfileEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", myInfo);
                    bundle.putString("type", "home");
                    bundle.putBoolean("isRegister", !isFromCreateProfileScreen);
                    profileEditIntent.putExtras(bundle);
                    startActivityForResult(profileEditIntent , EDIT_PROFILE_INTENT);
                }
                break;

            case R.id.btnDone:
                saveUserInfo();
                break;

            case R.id.homeBarLayout:
                if(myInfo == null) return;
                mPager.setCurrentItem(1);
                tiledProfilePhoto.refreshOriginalBitmap();
                tiledProfilePhoto.setImageUrl(myInfo.getHome().getProfileImage(), imgLoader);
                tiledProfilePhoto.invalidate();
                break;

            case R.id.workBarLayout:
                if(myInfo == null) return;
                mPager.setCurrentItem(0);
                tiledProfilePhoto.refreshOriginalBitmap();
                tiledProfilePhoto.setImageUrl(myInfo.getWork().getProfileImage(), imgLoader);
                tiledProfilePhoto.invalidate();
                break;
        }
    }

    private void setWizardPageAndGotoMobileVerificationScreen()
    {
        UserRequest.setWizardpage("2", new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    Intent tutorialIntent = new Intent(PersonalProfilePreviewActivity.this, GoToInviteContactScreenConfirmActivity.class);
                    PersonalProfilePreviewActivity.this.startActivity(tutorialIntent);
                    PersonalProfilePreviewActivity.this.finish();
                }
            }
        });
    }

    private void saveUserInfo()
    {
        if(this.myInfo == null || this.myInfo.equals(originalInfo) || (isFromCreateProfileScreen == false && isChanged == false))
        {
            PersonalProfilePreviewActivity.this.finish();
            return;
        }

        if(myInfo.getHome() != null && myInfo.getHome().getFields()!= null && myInfo.getHome().getInputableFieldsCount() >0) {
            UserInfoRequest.setUserInfo(this.myInfo.getHome(), new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if (response.isSuccess()) {
                        if (myInfo.getWork() != null && myInfo.getWork().getInputableFieldsCount() > 0) {
                            UserInfoRequest.setUserInfo(myInfo.getWork(), new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if(isFromCreateProfileScreen)
                                    {
                                        setWizardPageAndGotoMobileVerificationScreen();
                                    }
                                    else {
                                        PersonalProfilePreviewActivity.this.finish();
                                    }
                                }
                            });
                        } else {
                            if(isFromCreateProfileScreen)
                            {
                                setWizardPageAndGotoMobileVerificationScreen();
                            }
                            else {
                                PersonalProfilePreviewActivity.this.finish();
                            }
                        }
                    }
                }
            });
        }
        else
        {
            if (myInfo.getWork() != null && myInfo.getWork().getInputableFieldsCount() > 0)
            {
                UserInfoRequest.setUserInfo(myInfo.getWork(), new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if(isFromCreateProfileScreen)
                        {
                            setWizardPageAndGotoMobileVerificationScreen();
                        }
                        else {
                            PersonalProfilePreviewActivity.this.finish();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && resultCode == RESULT_OK)
        {
            if(requestCode == EDIT_PROFILE_INTENT)
            {
                UserWholeProfileVO userInfo = (UserWholeProfileVO) data.getSerializableExtra("userInfo");
                String type = data.getStringExtra("type");
                if(type != null) {
                    if (type.equals("home"))
                        currIndex = 1;
                    else
                        currIndex = 0;
                }
                isChanged = data.getBooleanExtra("isChanged", false);

                if(userInfo != null)
                {
                    initView(userInfo);
                }
            }
        }
    }

    class MyPagerAdapter extends FragmentStatePagerAdapter {

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

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            currIndex = position;
            updateBottomNavigation();

            if (currentGroup == null) {
                return;
            }

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

            if (currIndex == 0) {
                if(myInfo.getWork() != null && myInfo.getWork().getInputableFieldsCount() < 1)
                {
                    Intent profileEditIntent = new Intent(PersonalProfilePreviewActivity.this , PersonalProfileEditActivity.class);
                    Bundle bundle = new Bundle();
                    UserUpdateVO newWorkInfo = new UserUpdateVO();
                    newWorkInfo.setGroupName("work");
                    newWorkInfo.setImages(new ArrayList<TcImageVO>());
                    newWorkInfo.setProfileImage("");
                    newWorkInfo.setImages(new ArrayList<TcImageVO>());
                    newWorkInfo.setFields(new ArrayList<UserProfileVO>());
                    newWorkInfo.setVideo(null);
                    myInfo.setWork(newWorkInfo);

                    workProfileFragment.init(myInfo.getWork());

                    bundle.putSerializable("userInfo", myInfo);
                    bundle.putString("type", "work");
                    bundle.putBoolean("isRegister", true);
                    profileEditIntent.putExtras(bundle);
                    startActivityForResult(profileEditIntent, EDIT_PROFILE_INTENT);
                }
            } else {
                if(myInfo.getHome() == null || myInfo.getHome().getInputableFieldsCount() < 1) {
                    Intent profileEditIntent = new Intent(PersonalProfilePreviewActivity.this, PersonalProfileEditActivity.class);
                    Bundle bundle = new Bundle();
                    UserUpdateVO newHomeInfo = new UserUpdateVO();
                    newHomeInfo.setGroupName("home");
                    newHomeInfo.setImages(new ArrayList<TcImageVO>());
                    newHomeInfo.setProfileImage("");
                    newHomeInfo.setImages(new ArrayList<TcImageVO>());
                    newHomeInfo.setFields(new ArrayList<UserProfileVO>());
                    newHomeInfo.setVideo(null);
                    myInfo.setHome(newHomeInfo);

                    homeProfileFragment.init(myInfo.getHome());

                    bundle.putSerializable("userInfo", myInfo);
                    bundle.putString("type", "home");
                    bundle.putBoolean("isRegister", true);
                    profileEditIntent.putExtras(bundle);
                    startActivityForResult(profileEditIntent, EDIT_PROFILE_INTENT);
                }

            }
            currentGroup = currIndex==0? myInfo.getWork(): myInfo.getHome();

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
