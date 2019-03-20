package com.ginko.setup;

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

import com.ginko.activity.cb.CBSelectActivity;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.im.VideoViewerActivity;
import com.ginko.fragments.UserProfileFragment;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.context.ConstValues;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeWorkInfoPreviewActivity extends MyBaseFragmentActivity implements View.OnClickListener{

    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";

    private final int HOME_GROUP = 1;
    private final int WORK_GROUP = 2;

    private int groupType = HOME_GROUP;

    private final int EDIT_PROFILE = 1992;

    /* UI elements */
    private ImageView btnEdit;
    private Button btnNext;
    private ImageView btnHome , btnWork , btnPrivilege , btnPlayVideo;

    /* Variables */
    private String type;
    private UserWholeProfileVO userInfo;

    private UserUpdateVO currentGroup;


    private MyPagerAdapter pageAdapter = null;
    private UserProfileFragment homeProfileFragment;
    private UserProfileFragment workProfileFragment;

    private int currIndex = 0;
    private MyViewPager mPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();

    public static HomeWorkInfoPreviewActivity currentInstatnce = null;

    private boolean isHomePublicLocked = false, isWorkPublicLocked = false;
    private boolean isWorkSkipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_info_preview);

        HomeWorkInfoPreviewActivity.currentInstatnce = this;

        if(savedInstanceState!= null)
        {
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
            this.isWorkSkipped = savedInstanceState.getBoolean("isWorkSkipped" , false);
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
            this.isWorkSkipped = bundle.getBoolean("isWorkSkipped" , false);
        }

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        currentGroup = userInfo.getGroupInfoByGroupType(groupType);

        currIndex = groupType - 1;

        isHomePublicLocked = !userInfo.getHome().isPublic();
        if(userInfo.getWork()!=null)
            isWorkPublicLocked = !userInfo.getWork().isPublic();

        getUIObjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHomeWorkIcon();
        updatePrivilegeIcon();
        updateVideoIcon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HomeWorkInfoPreviewActivity.currentInstatnce = null;
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnEdit = (ImageView) findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnNext = (Button)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);

        btnHome = (ImageView) findViewById(R.id.btn_home); btnHome.setOnClickListener(this);
        btnWork = (ImageView) findViewById(R.id.btn_work);btnWork.setOnClickListener(this);
        btnPrivilege = (ImageView) findViewById(R.id.btn_privilege); btnPrivilege.setOnClickListener(this);
        btnPlayVideo = (ImageView) findViewById(R.id.btn_play_video);btnPlayVideo.setOnClickListener(this);

        initViewPager();

    }

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(true);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        homeProfileFragment = UserProfileFragment.newInstance("home" , userInfo.getHome());
        fragments.add(homeProfileFragment);
        if(groupType == WORK_GROUP) {
            workProfileFragment = UserProfileFragment.newInstance("work" , userInfo.getWork());
            fragments.add(workProfileFragment);
        }
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
    }

    private void updateHomeWorkIcon()
    {
        if(groupType == HOME_GROUP)
        {
            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btn_green_home_full);
                btnWork.setImageResource(R.drawable.green_work_line);
            } else {
                btnHome.setImageResource(R.drawable.green_home_line);
                btnWork.setImageResource(R.drawable.btn_green_work_full);
            }
            btnWork.setVisibility(View.GONE);
        }
        else {
            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btnhomeedit);
                btnWork.setImageResource(R.drawable.img_icon_work);
            } else {
                btnHome.setImageResource(R.drawable.img_home);
                btnWork.setImageResource(R.drawable.btnworkedit);
            }
            btnWork.setVisibility(View.VISIBLE);
        }
    }

    private void updatePrivilegeIcon()
    {
        if(currentGroup == null)
            return;
        boolean isPublicLocked = false;
        if(currIndex == 0) {
            isPublicLocked = isHomePublicLocked;
        }
        else {
            isPublicLocked = isWorkPublicLocked;
        }

        if(isPublicLocked)
        {
            if(groupType == HOME_GROUP)
                btnPrivilege.setImageResource(R.drawable.part_a_btn_lock_green);
            else
                btnPrivilege.setImageResource(R.drawable.part_a_btn_lock_purple);
        }
        else
        {
            if(groupType == HOME_GROUP)
                btnPrivilege.setImageResource(R.drawable.part_a_btn_unlock_green);
            else
                btnPrivilege.setImageResource(R.drawable.part_a_btn_unlock_purple);
        }

    }

    private void updateVideoIcon()
    {
        if(currentGroup == null) return;

        if(currentGroup.hasVideo()){
            btnPlayVideo.setVisibility(View.VISIBLE);
        }else{
            btnPlayVideo.setVisibility(View.INVISIBLE);
        }

        if(groupType == HOME_GROUP)
        {
            btnPlayVideo.setImageResource(R.drawable.btnhomeplayvideo);
        }
        else
        {
            btnPlayVideo.setImageResource(R.drawable.btnplayedit);
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
        outState.putBoolean("isWorkSkipped" , this.isWorkSkipped);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        this.type = savedInstanceState.getString(TYPE_PARAM);
        this.isWorkSkipped = savedInstanceState.getBoolean("isWorkSkipped" ,false);

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        currentGroup = userInfo.getGroupInfoByGroupType(groupType);
        currIndex = groupType - 1;

        getUIObjects();
    }

    private void saveUserInfoAndSetupWizardPage()
    {
        UserInfoRequest.setUserInfo(this.userInfo.getHome(),new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if(response.isSuccess()) {
                    if(userInfo.getWork().getFields()!=null && userInfo.getWork().getInputableFieldsCount() > 0) {
                        UserInfoRequest.setUserInfo(userInfo.getWork(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    UserRequest.setWizardpage("2", new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                //Intent contactMainIntent = new Intent(HomeWorkInfoPreviewActivity.this, ContactMainActivity.class);
                                                //Intent cbSelectIntent = new Intent(HomeWorkInfoPreviewActivity.this, CBSelectActivity.class);
                                                Intent tutorialIntent = new Intent(HomeWorkInfoPreviewActivity.this , TutorialActivity.class);
                                                tutorialIntent.putExtra("isFromSignUp" , true);
                                                HomeWorkInfoPreviewActivity.this.startActivity(tutorialIntent);
                                                HomeWorkInfoPreviewActivity.this.finish();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else
                    {
                        UserRequest.setWizardpage("2", new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    //Intent contactMainIntent = new Intent(HomeWorkInfoPreviewActivity.this, ContactMainActivity.class);
                                    //Intent cbSelectIntent = new Intent(HomeWorkInfoPreviewActivity.this, CBSelectActivity.class);
                                    Intent tutorialIntent = new Intent(HomeWorkInfoPreviewActivity.this , TutorialActivity.class);
                                    tutorialIntent.putExtra("isFromSignUp" , true);
                                    HomeWorkInfoPreviewActivity.this.startActivity(tutorialIntent);
                                    HomeWorkInfoPreviewActivity.this.finish();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            //go to edit info screen again
            case R.id.btnEdit:
                Intent homeWorkProfileEditIntent = new Intent(HomeWorkInfoPreviewActivity.this , HomeWorkEditProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("fromPreviewActivity" , true);
                bundle.putString(TYPE_PARAM, type);
                bundle.putSerializable(USER_INFO_PARAM, userInfo);
                homeWorkProfileEditIntent.putExtras(bundle);
                startActivityForResult(homeWorkProfileEditIntent, EDIT_PROFILE);
                break;

            //go to next info screen or fully created screen
            case R.id.btnNext:
                if(groupType == HOME_GROUP)
                {
                    if(this.isWorkSkipped)//save profile and goto contact main screen directly
                    {
                        userInfo.getHome().setPublic(!isHomePublicLocked);
                        saveUserInfoAndSetupWizardPage();
                    }
                    else {
                        userInfo.getHome().setPublic(!isHomePublicLocked);
                        Intent photoEditorIntent = new Intent(HomeWorkInfoPreviewActivity.this, TradeCardPhotoEditorSetActivity.class);
                        photoEditorIntent.putExtra("isSetNewPhotoInfo", true);
                        photoEditorIntent.putExtra("tradecardType", ConstValues.WORK_PHOTO_EDITOR);
                        Bundle bundle1 = new Bundle();
                        bundle1.putSerializable("userInfo", userInfo);
                        photoEditorIntent.putExtras(bundle1);
                        startActivity(photoEditorIntent);
                        finish();
                    }
                }
                else
                {
                    userInfo.getHome().setPublic(!isHomePublicLocked);
                    userInfo.getWork().setPublic(!isWorkPublicLocked);

                    saveUserInfoAndSetupWizardPage();
                }
                break;

            //click home
            case R.id.btn_home:
                mPager.setCurrentItem(0);
                break;

            //click work
            case R.id.btn_work:
                if(groupType == WORK_GROUP)
                    mPager.setCurrentItem(1);
                break;

            //click video to view
            case R.id.btn_play_video:
                Intent videoPlayIntent = new Intent(HomeWorkInfoPreviewActivity.this, VideoViewerActivity.class);
                videoPlayIntent.putExtra("video_uri", currentGroup.getVideoUrl());
                startActivity(videoPlayIntent);
                break;

            case R.id.btn_privilege:
                boolean isPublicLocked = false;
                if(currIndex == 0) {
                    isHomePublicLocked = !isHomePublicLocked;
                    isPublicLocked = isHomePublicLocked;
                }
                else {
                    isWorkPublicLocked = !isWorkPublicLocked;
                    isPublicLocked = isWorkPublicLocked;
                }

                if(isPublicLocked)
                {
                    if(groupType == HOME_GROUP)
                        btnPrivilege.setImageResource(R.drawable.part_a_btn_lock_green);
                    else
                        btnPrivilege.setImageResource(R.drawable.part_a_btn_lock_purple);
                }
                else
                {
                    if(groupType == HOME_GROUP)
                        btnPrivilege.setImageResource(R.drawable.part_a_btn_unlock_green);
                    else
                        btnPrivilege.setImageResource(R.drawable.part_a_btn_unlock_purple);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_PROFILE && resultCode == RESULT_OK)
        {
            Bundle bundle = data.getExtras();
            userInfo = (UserWholeProfileVO) bundle.getSerializable("userInfo");

            currentGroup = userInfo.getGroupInfoByGroupType(groupType);

            currIndex = groupType - 1;

            if(groupType == HOME_GROUP)
            {
                homeProfileFragment.init(userInfo.getHome());
            }
            else
            {
                homeProfileFragment.init(userInfo.getHome());
                workProfileFragment.init(userInfo.getWork());
            }

            mPager.setCurrentItem(currIndex);

            updateHomeWorkIcon();
            updatePrivilegeIcon();
            updateVideoIcon();
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
            if (currentGroup == null) {
                return;
            }

            currentGroup = currIndex==0? userInfo.getHome(): userInfo.getWork();
            updateHomeWorkIcon();
            updatePrivilegeIcon();
            updateVideoIcon();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
