package com.ginko.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.fragments.UserProfileEditFragment;
import com.ginko.api.request.TradeCard;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.util.List;

public class HomeWorkEditInfoActivity extends MyBaseFragmentActivity implements
        View.OnClickListener,
        ActionSheet.ActionSheetListener
{

    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";
    private String type = GROUP_TYPE_HOME;

    private final int HOME_GROUP = 1;
    private final int WORK_GROUP = 2;

    private final int RETAKE_PHOTO = 1991;

    private int groupType = HOME_GROUP;

    /* UI Elements */
    private TextView textViewTitle;
    private ImageView btnNext;
    private ImageView btnFieldTag , btnSharePublic , btnDeleteBackgrounds , btnBackgroundPhoto , btnEditInfo;

    private UserProfileEditFragment profileEditFragment;

    private ActionSheet removeBackgroundsActionSheet = null;

    /* Variables */

    private UserWholeProfileVO userInfo;
    private UserUpdateVO groupInfo;

    private boolean isPublicLocked = true;
    private boolean isAbbr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_work_editinfo);

        if(savedInstanceState != null)
        {
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
            type = savedInstanceState.getString(TYPE_PARAM);
        }
        else {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
        }

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        groupInfo = userInfo.getGroupInfoByGroupType(groupType);
        isPublicLocked = !groupInfo.isPublic();
        isAbbr = groupInfo.getAbbr();

        getUIObjects();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        textViewTitle = (TextView)findViewById(R.id.textViewTitle);

        btnNext = (ImageView)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);
        btnFieldTag =(ImageView)findViewById(R.id.btnFieldTag); btnFieldTag.setOnClickListener(this);
        btnSharePublic = (ImageView)findViewById(R.id.btnSharePublic); btnSharePublic.setOnClickListener(this);
        btnDeleteBackgrounds = (ImageView)findViewById(R.id.btnDeleteBackgrounds); btnDeleteBackgrounds.setOnClickListener(this);
        btnBackgroundPhoto = (ImageView)findViewById(R.id.btnBackgroundPhoto); btnBackgroundPhoto.setOnClickListener(this);
        btnEditInfo = (ImageView)findViewById(R.id.btnEditInfo); btnEditInfo.setOnClickListener(this);

        if(groupType == HOME_GROUP) {
            textViewTitle.setText(getResources().getString(R.string.home_info));
            btnFieldTag.setImageResource(R.drawable.part_a_btn_tag_green);
            btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_green);
            btnDeleteBackgrounds.setImageResource(R.drawable.home_trash);
            btnBackgroundPhoto.setImageResource(R.drawable.part_a_btn_cameraroll_green);
            btnEditInfo.setImageResource(R.drawable.part_a_btn_info_green);
        }
        else {
            textViewTitle.setText(getResources().getString(R.string.work_info));
            btnFieldTag.setImageResource(R.drawable.part_a_btn_tag_purple);
            btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_purple);
            btnDeleteBackgrounds.setImageResource(R.drawable.btntrashedit);
            btnBackgroundPhoto.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
            btnEditInfo.setImageResource(R.drawable.part_a_btn_info_purple);
        }

        initEditProfileFragment();

        updatePublicIcon();
        updateTagIcon();
    }

    private void initEditProfileFragment()
    {
        profileEditFragment = UserProfileEditFragment.newInstance(type, groupInfo);

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.editFragmentLayout , profileEditFragment);
        ft.commit();
    }

    private void updateTagIcon()
    {
        if(groupType == HOME_GROUP)
            btnFieldTag.setImageResource(isAbbr ? R.drawable.img_bt_tag_home_sel : R.drawable.part_a_btn_tag_green);
        else
            btnFieldTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
    }

    private void updatePublicIcon()
    {
        if(isPublicLocked)
        {
            switch(groupType) {
                case HOME_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_green);
                    break;
                case WORK_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_lock_purple);
                    break;
            }
        }
        else
        {
            switch(groupType) {
                case HOME_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_unlock_green);
                    break;
                case WORK_GROUP:
                    btnSharePublic.setImageResource(R.drawable.part_a_btn_unlock_purple);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TYPE_PARAM , type);
        outState.putSerializable(USER_INFO_PARAM , userInfo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        type = savedInstanceState.getString(TYPE_PARAM);

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        groupInfo = userInfo.getGroupInfoByGroupType(groupType);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            //go to set video activity
            case R.id.btnNext:
                if(groupType == HOME_GROUP) {
                    userInfo.setHome(profileEditFragment.save());
                    userInfo.getHome().setPublic(!isPublicLocked);
                    Intent photoEditorIntent = new Intent(HomeWorkEditInfoActivity.this,TradeCardPhotoEditorSetActivity.class);
                    photoEditorIntent.putExtra("isSetNewPhotoInfo" , true);
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("userInfo" , userInfo);
                    photoEditorIntent.putExtras(bundle1);
                    startActivity(photoEditorIntent);
                    finish();
                    return;
                }
                else if(groupType == WORK_GROUP) {
                    userInfo.setWork(profileEditFragment.save());
                    userInfo.getWork().setPublic(!isPublicLocked);
                }
                //remove video
                /*Intent homeWorkSetVideoIntent = new Intent(HomeWorkEditInfoActivity.this , HomeWorkSetVideoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isSetNewVideo" , true);
                bundle.putString(TYPE_PARAM , type);
                bundle.putSerializable(USER_INFO_PARAM , userInfo);
                homeWorkSetVideoIntent.putExtras(bundle);
                startActivity(homeWorkSetVideoIntent);
                finish();*/

                //go to preview activity instead go to set video screen
                Intent homeWorkInfoPreviewIntent = new Intent(HomeWorkEditInfoActivity.this , HomeWorkInfoPreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(TYPE_PARAM , type);
                bundle.putSerializable(USER_INFO_PARAM , userInfo);
                homeWorkInfoPreviewIntent.putExtras(bundle);
                startActivity(homeWorkInfoPreviewIntent);
                finish();
                break;

            //set tag for field values
            case R.id.btnFieldTag:
                boolean isAbbr = groupInfo.getAbbr();
                isAbbr = !isAbbr;
                if(groupType == HOME_GROUP)
                    btnFieldTag.setImageResource(isAbbr ? R.drawable.img_bt_tag_home_sel : R.drawable.part_a_btn_tag_green);
                else
                    btnFieldTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
                groupInfo.setAbbr(isAbbr);
                profileEditFragment.reShowForAbbr(isAbbr);
                break;

            //share public or not
            case R.id.btnSharePublic:
                isPublicLocked = !isPublicLocked;
                updatePublicIcon();
                break;

            //delete background or foreground images
            case R.id.btnDeleteBackgrounds:
                setTheme(R.style.ActionSheetStyleIOS7);
                if(removeBackgroundsActionSheet == null)
                    removeBackgroundsActionSheet = ActionSheet.createBuilder(HomeWorkEditInfoActivity.this , getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.str_remove_foreground_photo) ,
                                    getResources().getString(R.string.str_remove_background_photo))
                            .setCancelableOnTouchOutside(true)
                            .setListener(this)
                            .show();
                else
                    removeBackgroundsActionSheet.show(getSupportFragmentManager() , "actionSheet");
                break;

            //select background or foreground photo
            case R.id.btnBackgroundPhoto:
                if(groupType == HOME_GROUP) {
                    userInfo.setHome(profileEditFragment.save());
                    userInfo.getHome().setPublic(!isPublicLocked);
                }
                else if(groupType == WORK_GROUP) {
                    userInfo.setWork(profileEditFragment.save());
                    userInfo.getWork().setPublic(!isPublicLocked);
                }

                Intent photoEditorIntent = new Intent(HomeWorkEditInfoActivity.this,TradeCardPhotoEditorSetActivity.class);
                photoEditorIntent.putExtra("isSetNewPhotoInfo" , false);
                if(groupType == HOME_GROUP)
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);
                else
                    photoEditorIntent.putExtra("tradecardType" , ConstValues.WORK_PHOTO_EDITOR);
                Bundle photoBundle = new Bundle();
                photoBundle.putSerializable("userInfo", userInfo);
                photoEditorIntent.putExtras(photoBundle);
                startActivityForResult(photoEditorIntent, RETAKE_PHOTO);
                break;

            //go to screen of adding or editing info
            case R.id.btnEditInfo:

                if(groupType == HOME_GROUP) {
                    userInfo.setHome(profileEditFragment.save());
                    userInfo.getHome().setPublic(!isPublicLocked);
                }
                else if(groupType == WORK_GROUP) {
                    userInfo.setWork(profileEditFragment.save());
                    userInfo.getWork().setPublic(!isPublicLocked);
                }

                Intent addEditInfoIntent = new Intent(HomeWorkEditInfoActivity.this , HomeWorkAddInfoActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("type" , type);
                bundle1.putSerializable("userInfo", userInfo);
                addEditInfoIntent.putExtras(bundle1);
                startActivity(addEditInfoIntent);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RETAKE_PHOTO && resultCode == RESULT_OK)
        {
            System.out.println("-----Retake photo----");
            if(data!=null)
            {
                Bundle bundle = data.getExtras();
                userInfo = (UserWholeProfileVO) bundle.getSerializable("userInfo");
                groupInfo = userInfo.getGroupInfoByGroupType(groupType);

            }

            profileEditFragment.init(groupInfo);
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//remove foreground photo
        {
            final List<TcImageVO> images = groupInfo.getImages();
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
                    TradeCard.removeImage(groupType, images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                images.remove(imgIndex);
                                profileEditFragment.removeForeGround();

                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(HomeWorkEditInfoActivity.this, R.string.str_alert_failed_to_remove_foreground_photo, null);
                            }
                        }
                    });
                }
            }
        }
        else if(index == 1)//remove background photo
        {
            final List<TcImageVO> images = groupInfo.getImages();
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
                    TradeCard.removeImage(groupType, images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                images.remove(imgIndex);
                                profileEditFragment.removeBackGround();
                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(HomeWorkEditInfoActivity.this, R.string.str_alert_failed_to_remove_background_photo, null);
                            }
                        }
                    });
                }
            }
        }
    }
}