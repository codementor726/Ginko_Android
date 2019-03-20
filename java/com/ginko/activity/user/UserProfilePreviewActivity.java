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
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ginko.activity.im.VideoViewerActivity;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.UserProfileFragment;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserProfilePreviewActivity extends MyBaseFragmentActivity implements View.OnClickListener {
    private MyViewPager mPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private int currIndex = 0;

    private final int NEW_WORK_INFO = 999;

    private ImageButton btnPrev;
    private ImageView btnHome;
    private ImageView btnEdit;
    private ImageView btnWork;
    private ImageView btnPrivilege;
    private ImageView btnPlayVideo;

    private UserWholeProfileVO myInfo;

    private UserUpdateVO currentGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_preview);

        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnHome = (ImageView) findViewById(R.id.btn_home);
        btnEdit = (ImageView)findViewById(R.id.btnEdit);
        btnWork = (ImageView) findViewById(R.id.btn_work);
        btnPrivilege = (ImageView) findViewById(R.id.btn_privilege);
        btnPlayVideo = (ImageView) findViewById(R.id.btn_play_video);

        btnHome.setOnClickListener(this);
        btnWork.setOnClickListener(this);
        btnPrivilege.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnPlayVideo.setOnClickListener(this);

        initViewPager();

        UserInfoRequest.getInfo(new ResponseCallBack<UserWholeProfileVO>() {
            @Override
            public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                if (response.isSuccess()) {
                    myInfo = response.getData();
                    initView(myInfo);
                }
            }
        });
    }

    private MyPagerAdapter pageAdapter = null;
    private UserProfileFragment homeProfileFragment = UserProfileFragment.newInstance("home");
    private UserProfileFragment workProfileFragment = UserProfileFragment.newInstance("work");

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(true);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        fragments.add(homeProfileFragment);
        fragments.add(workProfileFragment);
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView(UserWholeProfileVO info) {
        myInfo = info;
        currentGroup = info.getHome();
        setActionButtonStyle();
        homeProfileFragment.init(info.getHome());
        if(info.getWork() == null || info.getWork().getInputableFieldsCount() <= 0)
        {
            currIndex = 0;
            mPager.setCurrentItem(currIndex);
        }
        else {
            workProfileFragment.init(info.getWork());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1000 && resultCode == RESULT_OK){
            UserWholeProfileVO info = (UserWholeProfileVO) data.getSerializableExtra("myinfo");
            initView(info);
        }
        else if(requestCode == NEW_WORK_INFO && resultCode == RESULT_OK && data != null)
        {
            UserWholeProfileVO info = (UserWholeProfileVO) data.getSerializableExtra("myInfo");
            initView(info);
            //work profile is added
            currIndex = 1;
            mPager.setCurrentItem(currIndex);

        }
        else if(requestCode == NEW_WORK_INFO && resultCode == RESULT_CANCELED)
        {
            currIndex = 0;
            mPager.setCurrentItem(0);
        }

    }

    private void goToWorkNewCreateProfile()
    {
        Intent tradeCardPhotoEditorForWorkIntent = new Intent(UserProfilePreviewActivity.this , TradeCardPhotoEditorSetActivity.class);
        tradeCardPhotoEditorForWorkIntent.putExtra("isSetNewPhotoInfo" , true);
        tradeCardPhotoEditorForWorkIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
        tradeCardPhotoEditorForWorkIntent.putExtra("isAddNewWorkProfile" , true);
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("userInfo", myInfo);
        tradeCardPhotoEditorForWorkIntent.putExtras(bundle1);
        startActivityForResult(tradeCardPhotoEditorForWorkIntent, NEW_WORK_INFO);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btn_home:
                mPager.setCurrentItem(0);
                break;
            case R.id.btnEdit:
                Intent intent = new Intent(this,UserProfileEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("myinfo",myInfo);
                bundle.putInt("currentPageIndex" , currIndex);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1000);
                break;
            case R.id.btn_work:
                if(myInfo.getWork() == null || myInfo.getWork().getInputableFieldsCount() <= 0) {
                    //go to create your profile screen of "work"
                    goToWorkNewCreateProfile();
                }
                else
                {
                    mPager.setCurrentItem(1);
                }
                break;
            case R.id.btn_privilege:
                int home_privilege = myInfo.getHome().isPublic()?1:0;
                int work_privilege = myInfo.getWork().isPublic()?1:0;

                if (currentGroup == null) {
                    return;
                }
                if(currIndex == 0)
                    home_privilege = (home_privilege+1)%2;
                else
                    work_privilege = (work_privilege+1)%2;

                UserInfoRequest.updatePrivilege(home_privilege, work_privilege, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if(response.isSuccess())
                        {
                            currentGroup.setPublic(!currentGroup.isPublic());
                            setActionButtonStyle();
                        }
                        else
                        {
                            Uitils.alert(UserProfilePreviewActivity.this , response.getErrorMessage());
                        }
                    }
                });
                break;

            case R.id.btn_play_video:
                if(currentGroup != null && currentGroup.getVideoUrl() != null) {
                    Intent videoPlayIntent = new Intent(UserProfilePreviewActivity.this, VideoViewerActivity.class);
                    videoPlayIntent.putExtra("video_uri", currentGroup.getVideoUrl());
                    startActivity(videoPlayIntent);
                }
                break;
        }
    }

    private void setActionButtonStyle() {
        if (currentGroup.isPublic()){
            btnPrivilege.setImageResource(R.drawable.btnunlockedit);
        }else{
            btnPrivilege.setImageResource(R.drawable.btnlockedit);
        }

        if(StringUtils.isBlank(currentGroup.getVideoUrl())){
            btnPlayVideo.setVisibility(View.INVISIBLE);
        }else{
            btnPlayVideo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btnhomeedit);
                btnWork.setImageResource(R.drawable.img_icon_work);
            } else {
                if(myInfo.getWork() == null || myInfo.getWork().getInputableFieldsCount() <= 0) {
                    goToWorkNewCreateProfile();
                    return;
                }
                btnHome.setImageResource(R.drawable.img_home);
                btnWork.setImageResource(R.drawable.btnworkedit);
            }
            currentGroup = currIndex==0? myInfo.getHome(): myInfo.getWork();
            setActionButtonStyle();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
