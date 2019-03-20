package com.ginko.activity.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.Logger;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.UserProfileEditFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.setup.HomeWorkSetVideoActivity;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class UserProfileEditActivity extends MyBaseFragmentActivity implements View.OnClickListener ,
        ActionSheet.ActionSheetListener
{
    private final int RETAKE_PHOTO = 1993;
    private final int RETAKE_VIDEO = 1995;
    private final int EDIT_USER_PROFILE_INFO = 1996;
    private final int NEW_WORK_INFO = 899;

    private ImageView btnHome , btnWork , btnDeletePhoto , btnTag , btnVideo , btnPhoto , btnEditProfile;
    private ImageButton btnConfirm;
    private ImageView btnSkipWorkInfo;

    private MyViewPager mPager;
    private MyOnPageChangeListener myOnPageChangeListener;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private int currIndex = 0;


    private UserWholeProfileVO myInfo;

    private UserUpdateVO currentGroup;

    private ActionSheet removeBackgroundsActionSheet = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);


        btnHome = (ImageView) findViewById(R.id.btn_home);        btnHome.setOnClickListener(this);
        btnWork = (ImageView) findViewById(R.id.btn_work);        btnWork.setOnClickListener(this);
        btnDeletePhoto = (ImageView) findViewById(R.id.btn_delete_photo);        btnDeletePhoto.setOnClickListener(this);
        btnTag = (ImageView) findViewById(R.id.btn_tag);        btnTag.setOnClickListener(this);
        btnVideo = (ImageView) findViewById(R.id.btn_video);        btnVideo.setOnClickListener(this);
        btnPhoto = (ImageView) findViewById(R.id.btn_photo);        btnPhoto.setOnClickListener(this);
        btnEditProfile = (ImageView) findViewById(R.id.btn_edit_profile);        btnEditProfile.setOnClickListener(this);

        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnSkipWorkInfo = (ImageView)findViewById(R.id.btnSkipWorkInfo); btnSkipWorkInfo.setOnClickListener(this);

        if(getIntent().hasExtra("myinfo")){
            myInfo = (UserWholeProfileVO) getIntent().getSerializableExtra("myinfo");
            int startPageIndex = getIntent().getIntExtra("currentPageIndex" , 0);
            currIndex = startPageIndex;

            if(myInfo==null)
            {
                finish();
                return;
            }

            if(currIndex == 0)//home
                currentGroup = myInfo.getHome();
            else
                currentGroup = myInfo.getWork();

        }

        if(currentGroup!=null)
        {
            boolean isAbbr = currentGroup.getAbbr();
            btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
        }

        initField();
        initViewPager();
    }

    private void initField() {
        homeProfileFragment = UserProfileEditFragment.newInstance("home", myInfo.getHome());
        workProfileFragment = UserProfileEditFragment.newInstance("work",myInfo.getWork());
    }

    private MyPagerAdapter pageAdapter = null;
    private UserProfileEditFragment homeProfileFragment = null;
    private UserProfileEditFragment workProfileFragment = null;

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(false);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        myOnPageChangeListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(myOnPageChangeListener);
        fragments.add(homeProfileFragment);
        fragments.add(workProfileFragment);
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
        myOnPageChangeListener.onPageSelected(currIndex);

    }

    private void saveChange() {
        myInfo.setHome(homeProfileFragment.save());
        myInfo.setWork(workProfileFragment.save());

        saveUserInfo();

    }

    private void goToWorkNewCreateProfile()
    {
        Intent tradeCardPhotoEditorForWorkIntent = new Intent(UserProfileEditActivity.this , TradeCardPhotoEditorSetActivity.class);
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
        switch (id) {
            case R.id.btnConfirm:
                saveChange();
                break;

            //delete work info
            case R.id.btnSkipWorkInfo:
                if(currIndex == 0)//home
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                builder.setMessage(getResources().getString(R.string.str_confirm_delete_profile));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        UserInfoRequest.removeProfile("work" , new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                {
                                    //reset the work info
                                    UserUpdateVO workInfo = myInfo.getWork();
                                    workInfo.setFields(new ArrayList<UserProfileVO>());
                                    workInfo.setProfileImage("");
                                    workInfo.setVideo(null);
                                    workInfo.setImages(new ArrayList<TcImageVO>());
                                }
                                //select home info
                                workProfileFragment.init(currentGroup);
                                mPager.setCurrentItem(0);

                            }
                        });
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

            case R.id.btn_home:
                mPager.setCurrentItem(0);
                break;
            case R.id.btn_work:
                if(myInfo.getWork() == null || myInfo.getWork().getInputableFieldsCount() <= 0)
                {
                    goToWorkNewCreateProfile();
                }
                else {
                    mPager.setCurrentItem(1);
                }
                break;
            case R.id.btn_delete_photo:
                setTheme(R.style.ActionSheetStyleIOS7);
                if(removeBackgroundsActionSheet == null)
                    removeBackgroundsActionSheet = ActionSheet.createBuilder(UserProfileEditActivity.this , getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(
                                    getResources().getString(R.string.str_remove_video),
                                    getResources().getString(R.string.str_remove_foreground_photo),
                                    getResources().getString(R.string.str_remove_background_photo))
                            .setCancelableOnTouchOutside(true)
                            .setListener(this)
                            .show();
                else
                    removeBackgroundsActionSheet.show(getSupportFragmentManager() , "actionSheet");
                break;
            case R.id.btn_tag:
                if (this.currentGroup == null) {
                    return;
                }

                boolean isAbbr = this.currentGroup.getAbbr();
                isAbbr = !isAbbr;
                btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
                this.currentGroup.setAbbr(isAbbr);
                ((UserProfileEditFragment)pageAdapter.getItem(currIndex)).reShowForAbbr(isAbbr);
                break;
            case R.id.btn_video:
                myInfo.setHome(homeProfileFragment.save());
                myInfo.setWork(workProfileFragment.save());

                Intent videoIntent = new Intent(UserProfileEditActivity.this,HomeWorkSetVideoActivity.class);
                Bundle videoBundle = new Bundle();
                videoBundle.putBoolean("isSetNewVideo", false);
                if(currIndex == 0)
                    videoBundle.putString("type" , "home");
                else
                    videoBundle.putString("type" , "work");

                videoBundle.putSerializable("userInfo" , myInfo);
                videoIntent.putExtras(videoBundle);
                startActivityForResult(videoIntent, RETAKE_VIDEO);
                break;
            case R.id.btn_photo:
                myInfo.setHome(homeProfileFragment.save());
                myInfo.setWork(workProfileFragment.save());

                Intent photoEditorIntent = new Intent(UserProfileEditActivity.this,TradeCardPhotoEditorSetActivity.class);
                photoEditorIntent.putExtra("isSetNewPhotoInfo" , false);
                if(currIndex == 0)
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);
                else
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
                Bundle photoBundle = new Bundle();
                photoBundle.putSerializable("userInfo", myInfo);
                photoEditorIntent.putExtras(photoBundle);
                startActivityForResult(photoEditorIntent, RETAKE_PHOTO);
                break;
            case R.id.btn_edit_profile:
                //TODO go to edit screen.
                myInfo.setHome(homeProfileFragment.save());
                myInfo.setWork(workProfileFragment.save());

                Intent intent = new Intent(UserProfileEditActivity.this , UserProfileEditInfoActivity.class);
                Bundle bundle = new Bundle();
                if(currIndex == 0)
                    bundle.putString("type", "home");
                else
                    bundle.putString("type" , "work");
                bundle.putBoolean("isFromSignup" , false);
                bundle.putSerializable("userInfo" , myInfo);
                intent.putExtras(bundle);
                startActivityForResult(intent , EDIT_USER_PROFILE_INFO);
                break;
            default:
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
                myInfo = (UserWholeProfileVO) bundle.getSerializable("userInfo");
                currentGroup = myInfo.getGroupInfoByGroupType(currIndex+1);
                homeProfileFragment.init(myInfo.getHome());
                workProfileFragment.init(myInfo.getWork());
            }

            else if(requestCode == NEW_WORK_INFO)
            {
                myInfo = (UserWholeProfileVO) data.getSerializableExtra("myInfo");
                //work profile is added
                currIndex = 1;
                mPager.setCurrentItem(currIndex);
                homeProfileFragment.init(myInfo.getHome());
                workProfileFragment.init(myInfo.getWork());
            }
        }
        else if(requestCode == NEW_WORK_INFO && resultCode == RESULT_CANCELED)
        {
            currIndex = 0;
            mPager.setCurrentItem(0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //saveUserInfo();
    }

    private void saveUserInfo()
    {
        if(this.myInfo == null) return;

        UserInfoRequest.setUserInfo(this.myInfo.getHome(),new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if(response.isSuccess()) {
                    if(myInfo.getWork()!=null && myInfo.getWork().getInputableFieldsCount() > 0)
                    {
                        UserInfoRequest.setUserInfo(myInfo.getWork(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("myinfo", myInfo);
                                intent.putExtras(bundle);
                                setResult(RESULT_OK, intent);
                                UserProfileEditActivity.this.finish();
                            }
                        });
                    }
                    else
                    {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("myinfo", myInfo);
                        intent.putExtras(bundle);
                        setResult(RESULT_OK, intent);
                        UserProfileEditActivity.this.finish();
                    }
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
            if (currIndex == 0) {
                btnHome.setImageResource(R.drawable.btnhomeedit);
                btnWork.setImageResource(R.drawable.img_icon_work);
                btnSkipWorkInfo.setVisibility(View.GONE);
            } else {
                btnHome.setImageResource(R.drawable.img_home);
                btnWork.setImageResource(R.drawable.btnworkedit);

                if(myInfo.getWork().getInputableFieldsCount() >0)
                    btnSkipWorkInfo.setVisibility(View.VISIBLE);
                else
                    btnSkipWorkInfo.setVisibility(View.GONE);
            }
            currentGroup = currIndex==0? myInfo.getHome(): myInfo.getWork();
            if(currentGroup!=null)
            {
                boolean isAbbr = currentGroup.getAbbr();
                btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
            }
            
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
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
                            MyApp.getInstance().showSimpleAlertDiloag(UserProfileEditActivity.this, R.string.str_alert_failed_to_delete_video , null);
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
                                    homeProfileFragment.removeForeGround();
                                }
                                else
                                {
                                    workProfileFragment.removeForeGround();
                                }
                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(UserProfileEditActivity.this, R.string.str_alert_failed_to_remove_foreground_photo, null);
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
                                    homeProfileFragment.removeBackGround();
                                }
                                else
                                {
                                    workProfileFragment.removeBackGround();
                                }
                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(UserProfileEditActivity.this , R.string.str_alert_failed_to_remove_background_photo, null);
                            }
                        }
                    });
                }
            }
        }
    }
}
