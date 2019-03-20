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
import com.ginko.fragments.UserProfileEditFragment;
import com.ginko.activity.user.UserProfileEditInfoActivity;
import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeWorkEditProfileActivity extends MyBaseFragmentActivity implements View.OnClickListener,
        ActionSheet.ActionSheetListener
{
    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";

    private final int HOME_GROUP = 1;
    private final int WORK_GROUP = 2;

    private int groupType = HOME_GROUP;

    private final int RETAKE_PHOTO = 1993;
    private final int RETAKE_VIDEO = 1995;
    private final int EDIT_USER_PROFILE_INFO = 1996;


    /* UI Elements*/
    private Button btnDone;
    private ImageView btnHome , btnWork , btnDelete , btnTag , btnVideo , btnPhoto , btnEditProfile;

    private ActionSheet removeBackgroundsActionSheet = null;

    /* Variables */
    private String type;
    private UserWholeProfileVO userInfo;

    private UserUpdateVO currentGroup;

    private boolean isFromPreviewActivity = false;
    private boolean isWorkSkipped = false;

    private MyPagerAdapter pageAdapter = null;
    private UserProfileEditFragment homeProfileEditFragment;
    private UserProfileEditFragment workProfileEditFragment;

    private int currIndex = 0;
    private MyViewPager mPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_profile_edit);

        if(savedInstanceState!= null)
        {
            isFromPreviewActivity = savedInstanceState.getBoolean("fromPreviewActivity" , false);
            isWorkSkipped = savedInstanceState.getBoolean("isWorkSkipped" , false);
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            isFromPreviewActivity = bundle.getBoolean("fromPreviewActivity" , false);
            isWorkSkipped = bundle.getBoolean("isWorkSkipped" , false);
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
        }

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        currentGroup = userInfo.getGroupInfoByGroupType(groupType);

        currIndex = groupType - 1;

        getUIObjects();
    }

    private void initGroupInfo()
    {
        homeProfileEditFragment.init(userInfo.getHome());
        workProfileEditFragment.init(userInfo.getWork());
    }

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(false);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        fragments.add(homeProfileEditFragment);
        fragments.add(workProfileEditFragment);
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);
        btnHome = (ImageView)findViewById(R.id.btn_home); btnHome.setOnClickListener(this);
        btnWork = (ImageView)findViewById(R.id.btn_work); btnWork.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btn_delete_photo); btnDelete.setOnClickListener(this);
        btnTag = (ImageView)findViewById(R.id.btn_tag); btnTag.setOnClickListener(this);
        btnVideo = (ImageView)findViewById(R.id.btn_video); btnVideo.setOnClickListener(this);
        btnPhoto = (ImageView)findViewById(R.id.btn_photo); btnPhoto.setOnClickListener(this);
        btnEditProfile = (ImageView)findViewById(R.id.btn_edit_profile); btnEditProfile.setOnClickListener(this);

        homeProfileEditFragment = UserProfileEditFragment.newInstance("home" , userInfo.getHome()) ;
        workProfileEditFragment = UserProfileEditFragment.newInstance("work" , userInfo.getWork()) ;

        initViewPager();

        updateHomeWorkIcon();
        updateTagIcon();

        if(groupType == HOME_GROUP)
        {
            btnDelete.setImageResource(R.drawable.home_trash);
            btnVideo.setImageResource(R.drawable.part_a_btn_video_green);
            btnPhoto.setImageResource(R.drawable.part_a_btn_cameraroll_green);
            btnEditProfile.setImageResource(R.drawable.part_a_btn_info_green);
        }
        else
        {
            btnDelete.setImageResource(R.drawable.btntrashedit);
            btnVideo.setImageResource(R.drawable.part_a_btn_video_purple);
            btnPhoto.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
            btnEditProfile.setImageResource(R.drawable.part_a_btn_info_purple);
        }
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
        }
        else {
            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btnhomeedit);
                btnWork.setImageResource(R.drawable.img_icon_work);
            } else {
                btnHome.setImageResource(R.drawable.img_home);
                btnWork.setImageResource(R.drawable.btnworkedit);
            }
        }
    }
    private void updateTagIcon()
    {
        if(currentGroup == null)
            return;
        boolean isAbbr = currentGroup.getAbbr();
        if(groupType == HOME_GROUP)
            btnTag.setImageResource(isAbbr ? R.drawable.img_bt_tag_home_sel : R.drawable.part_a_btn_tag_green);
        else
            btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("fromPreviewActivity", this.isFromPreviewActivity);
        outState.putBoolean("isWorkSkipped" , this.isWorkSkipped);
        outState.putString(TYPE_PARAM, type);
        outState.putSerializable(USER_INFO_PARAM, userInfo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.isFromPreviewActivity = savedInstanceState.getBoolean("fromPreviewActivity" , false);
        this.isWorkSkipped = savedInstanceState.getBoolean("isWorkSkipped" , false);
        this.userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        this.type = savedInstanceState.getString(TYPE_PARAM);

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        currentGroup = userInfo.getGroupInfoByGroupType(groupType);

        currIndex = groupType - 1;

        getUIObjects();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            //done button
            case R.id.btnDone:
                userInfo.setHome(homeProfileEditFragment.save());
                userInfo.setWork(workProfileEditFragment.save());

                if(groupType == HOME_GROUP) {
                    if (isFromPreviewActivity) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userInfo" , userInfo);
                        intent.putExtras(bundle);
                        HomeWorkEditProfileActivity.this.setResult(RESULT_OK, intent);
                        HomeWorkEditProfileActivity.this.finish();
                    } else {
                        //save userinfo and go to select CB page
                        Intent homeWorkInfoPreviewIntent = new Intent(HomeWorkEditProfileActivity.this , HomeWorkInfoPreviewActivity.class);
                        Bundle bundle2 = new Bundle();
                        bundle2.putString(TYPE_PARAM, "home");
                        bundle2.putSerializable(USER_INFO_PARAM, userInfo);
                        bundle2.putBoolean("isWorkSkipped" , this.isWorkSkipped);
                        homeWorkInfoPreviewIntent.putExtras(bundle2);
                        startActivity(homeWorkInfoPreviewIntent);
                        finish();
                    }
                }
                else
                {
                    if (isFromPreviewActivity) {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userInfo" , userInfo);
                        intent.putExtras(bundle);
                        HomeWorkEditProfileActivity.this.setResult(RESULT_OK, intent);
                        HomeWorkEditProfileActivity.this.finish();
                    } else {
                        //save userinfo and go to select CB page
                        Intent homeWorkInfoPreviewIntent = new Intent(HomeWorkEditProfileActivity.this , HomeWorkInfoPreviewActivity.class);
                        Bundle bundle2 = new Bundle();
                        bundle2.putString(TYPE_PARAM , "work");
                        bundle2.putSerializable(USER_INFO_PARAM , userInfo);
                        bundle2.putBoolean("isWorkSkipped" , this.isWorkSkipped);
                        homeWorkInfoPreviewIntent.putExtras(bundle2);
                        startActivity(homeWorkInfoPreviewIntent);
                        finish();
                    }
                }
                break;

            //home button
            case R.id.btn_home:
                mPager.setCurrentItem(0);
                break;

            //work button
            case R.id.btn_work:
                if(groupType == HOME_GROUP)
                {
                    Intent photoEditorIntent = new Intent(HomeWorkEditProfileActivity.this,TradeCardPhotoEditorSetActivity.class);
                    photoEditorIntent.putExtra("isSetNewPhotoInfo" , true);
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("userInfo" , userInfo);
                    photoEditorIntent.putExtras(bundle1);
                    startActivity(photoEditorIntent);
                    finish();
                    if(HomeWorkInfoPreviewActivity.currentInstatnce != null)
                        HomeWorkInfoPreviewActivity.currentInstatnce.finish();
                }
                else
                {
                    mPager.setCurrentItem(1);
                }
                break;

            //delete photo & video
            case R.id.btn_delete_photo:
                setTheme(R.style.ActionSheetStyleIOS7);
                if(removeBackgroundsActionSheet == null)
                    removeBackgroundsActionSheet = ActionSheet.createBuilder(HomeWorkEditProfileActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.str_remove_video) ,
                                    getResources().getString(R.string.str_remove_foreground_photo) ,
                                    getResources().getString(R.string.str_remove_background_photo))
                            .setCancelableOnTouchOutside(true)
                            .setListener(this)
                            .show();
                else
                    removeBackgroundsActionSheet.show(getSupportFragmentManager() , "actionSheet");
                break;

            //tag button
            case R.id.btn_tag:
                boolean isAbbr = currentGroup.getAbbr();
                isAbbr = !isAbbr;
                currentGroup.setAbbr(isAbbr);
                if(currIndex == 0) //Home
                    homeProfileEditFragment.reShowForAbbr(isAbbr);
                else //Work
                    workProfileEditFragment.reShowForAbbr(isAbbr);
                if(groupType == HOME_GROUP) //Home
                    btnTag.setImageResource(isAbbr ? R.drawable.img_bt_tag_home_sel : R.drawable.part_a_btn_tag_green);
                else //Work
                    btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );

                break;

            //video button
            case R.id.btn_video:
                userInfo.setHome(homeProfileEditFragment.save());
                userInfo.setWork(workProfileEditFragment.save());

                Intent videoIntent = new Intent(HomeWorkEditProfileActivity.this,HomeWorkSetVideoActivity.class);
                Bundle videoBundle = new Bundle();
                videoBundle.putBoolean("isSetNewVideo", false);
                if(currIndex == 0)
                    videoBundle.putString(TYPE_PARAM, GROUP_TYPE_HOME);
                else
                    videoBundle.putString(TYPE_PARAM, GROUP_TYPE_WORK);

                videoBundle.putSerializable(USER_INFO_PARAM , userInfo);
                videoIntent.putExtras(videoBundle);
                startActivityForResult(videoIntent, RETAKE_VIDEO);
                break;

            //photo button
            case R.id.btn_photo:
                userInfo.setHome(homeProfileEditFragment.save());
                userInfo.setWork(workProfileEditFragment.save());

                Intent photoEditorIntent = new Intent(HomeWorkEditProfileActivity.this,TradeCardPhotoEditorSetActivity.class);
                photoEditorIntent.putExtra("isSetNewPhotoInfo" , false);
                if(currIndex == 0)
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);
                else
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
                Bundle photoBundle = new Bundle();
                photoBundle.putSerializable(USER_INFO_PARAM, userInfo);
                photoEditorIntent.putExtras(photoBundle);
                startActivityForResult(photoEditorIntent, RETAKE_PHOTO);
                break;

            //edit profile
            case R.id.btn_edit_profile:
                userInfo.setHome(homeProfileEditFragment.save());
                userInfo.setWork(workProfileEditFragment.save());

                Intent intent = new Intent(HomeWorkEditProfileActivity.this , UserProfileEditInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFromSignup" , true);
                bundle.putString(TYPE_PARAM , type);
                bundle.putSerializable(USER_INFO_PARAM , userInfo);
                intent.putExtras(bundle);
                startActivityForResult(intent , EDIT_USER_PROFILE_INFO);
                break;

        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//remove video
        {
            if(currentGroup == null) return;
            if(currentGroup.hasVideo() && currentGroup.getVideo() != null)
            {
                TradeCard.deleteVideo(currIndex + 1, currentGroup.getVideo().getId(), new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            currentGroup.setVideo(null);
                        } else {
                            MyApp.getInstance().showSimpleAlertDiloag(HomeWorkEditProfileActivity.this, R.string.str_alert_failed_to_delete_video, null);
                        }
                    }
                });
            }
            else
            {
                MyApp.getInstance().showSimpleAlertDiloag(this , R.string.str_alert_no_video_to_remove, null);
            }
        }
        else if(index == 1)//remove foreground photo
        {
            final List<TcImageVO> images = currentGroup.getImages();
            if(images == null || images.size()<1)
            {
                MyApp.getInstance().showSimpleAlertDiloag(this , R.string.str_alert_no_foreground_photo_to_remove, null);
            }
            else
            {
                int i = 0;
                boolean bHasForeground = false;
                for(i=0;i<images.size();i++)
                {
                    if(images.get(i).getZIndex() == 1)//foreground
                    {
                        bHasForeground = true;
                        break;
                    }
                }
                if(!bHasForeground)
                {
                    MyApp.getInstance().showSimpleAlertDiloag(this , R.string.str_alert_no_foreground_photo_to_remove, null);
                    return;
                }
                final int imgIndex=  i;

                if(images.get(imgIndex).getId()!= null) {
                    TradeCard.removeImage(currIndex+1, images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                images.remove(imgIndex);
                                if(currIndex == 0) {
                                    homeProfileEditFragment.removeForeGround();
                                }
                                else
                                {
                                    workProfileEditFragment.removeForeGround();
                                }
                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(HomeWorkEditProfileActivity.this, R.string.str_alert_failed_to_remove_foreground_photo, null);
                            }
                        }
                    });
                }
            }
        }
        else if(index == 2)//remove background photo
        {
            final List<TcImageVO> images = currentGroup.getImages();
            if(images == null || images.size()<1)
            {
                MyApp.getInstance().showSimpleAlertDiloag(this , R.string.str_alert_no_background_photo_to_remove, null);
            }
            else
            {
                int i = 0;
                boolean bHasBackground = false;
                for(i=0;i<images.size();i++)
                {
                    if(images.get(i).getZIndex() == 0)//background
                    {
                        bHasBackground = true;
                        break;
                    }
                }
                if(!bHasBackground)
                {
                    MyApp.getInstance().showSimpleAlertDiloag(this , R.string.str_alert_no_background_photo_to_remove, null);
                    return;
                }
                final int imgIndex=  i;
                if(images.get(imgIndex).getId()!= null) {
                    TradeCard.removeImage(currIndex+1, images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                images.remove(imgIndex);
                                if(currIndex == 0) {
                                    homeProfileEditFragment.removeBackGround();
                                }
                                else
                                {
                                    workProfileEditFragment.removeBackGround();
                                }
                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(HomeWorkEditProfileActivity.this, R.string.str_alert_failed_to_remove_background_photo, null);
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null)
        {

            if(requestCode == RETAKE_PHOTO
                    || requestCode == RETAKE_VIDEO
                    || requestCode == EDIT_USER_PROFILE_INFO)
            {
                Bundle bundle = data.getExtras();
                userInfo = (UserWholeProfileVO) bundle.getSerializable("userInfo");
                currentGroup = userInfo.getGroupInfoByGroupType(currIndex+1);

                System.out.println("-------Profile Info updated-------");
                initGroupInfo();
            }
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
            updateTagIcon();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
