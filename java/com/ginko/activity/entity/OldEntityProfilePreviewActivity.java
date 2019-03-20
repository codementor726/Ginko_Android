package com.ginko.activity.entity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.im.VideoViewerActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.Uitils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.UserEntityProfileFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;

import java.util.HashMap;
import java.util.List;

public class OldEntityProfilePreviewActivity extends MyBaseFragmentActivity implements View.OnClickListener{

    private final int EDIT_ENTITY_INFO = 1000;

    /* UI Elements */
    private ImageButton btnPrev;
    private ImageView btnHome , btnChatNav;
    ImageView btnEditProfile , btnInviteContact , btnLock , btnVideo;
    TextView txtTile;
    private ViewPager mPager;
    private CustomNetworkImageView backgroundPhotoView , foregroundPhotoView;

    /* Variables */

    private EntityVO entity;
    private boolean isNewEntity = false;
    private boolean isProfileLocked = true;

    private ImageLoader imgLoader;

    private MyOnPageChangeListener pageListener;
    private MyPagerAdapter pageAdapter = null;
    private HashMap<Integer , UserEntityProfileFragment> fragmentMap;
    private UserEntityProfileFragment currentFragment;
    private int currIndex = 0;
    private float ratio = 1.0f;

    private boolean isForceClose = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_activity_entity_profile_preview);

        isForceClose = true;

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity" , true);
        }
        else
        {
            this.entity = (EntityVO) this.getIntent().getSerializableExtra("entity");
            this.isNewEntity = this.getIntent().getBooleanExtra("isNewEntity" , true);
        }

        this.isProfileLocked = this.entity.getPrivilege()==0;
        ratio = Uitils.getScreenRatioViaIPhone(OldEntityProfilePreviewActivity.this);

        getUIObjects();
    }
    private void initViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),  this.entity.getEntityInfos());
        pageListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(pageListener);
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        fragmentMap = new HashMap<Integer , UserEntityProfileFragment>();

        imgLoader = MyApp.getInstance().getImageLoader();

        txtTile = (TextView)findViewById(R.id.txtTitle);
        if(this.entity.getEntityInfos().size()>1)//if has more than one locations
        {
            txtTile.setText(String.valueOf(this.entity.getEntityInfos().size())+" "+ getResources().getString(R.string.str_entity_locations));
        }
        else
        {
            txtTile.setText(getResources().getString(R.string.str_entity_location));
        }

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnChatNav = (ImageView)findViewById(R.id.btnChatNav); btnChatNav.setOnClickListener(this);
        btnHome = (ImageView)findViewById(R.id.btnHome); btnHome.setOnClickListener(this);
        btnEditProfile = (ImageView)findViewById(R.id.btnEditProfile); btnEditProfile.setOnClickListener(this);
        btnInviteContact = (ImageView)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);
        btnLock = (ImageView)findViewById(R.id.btnLock); btnLock.setOnClickListener(this);
        btnVideo = (ImageView)findViewById(R.id.btnVideo); btnVideo.setOnClickListener(this);

        if(isNewEntity)
        {
            btnHome.setVisibility(View.VISIBLE);
            btnPrev.setVisibility(View.GONE);
            btnChatNav.setVisibility(View.GONE);
        }
        else
        {
            btnHome.setVisibility(View.GONE);
            btnPrev.setVisibility(View.VISIBLE);
            btnChatNav.setVisibility(View.VISIBLE);
        }

        if(this.entity.getVideo() == null || this.entity.getVideo().equals(""))
            btnVideo.setVisibility(View.GONE);
        else
            btnVideo.setVisibility(View.VISIBLE);

        backgroundPhotoView = (CustomNetworkImageView)findViewById(R.id.imgBackgroundPhoto);

        LinearLayout backgroundPhotoLayout = (LinearLayout)findViewById(R.id.backgroundPhotoLayout);
        AbsoluteLayout foregroundPhotoLayout = (AbsoluteLayout)findViewById(R.id.foregroundPhotoLayout);

        List<EntityImageVO> images = this.entity.getEntityImages();
        if(images.size() >= 1)
        {
            String backgroundPhotoUrl = null;
            String foregroundPhotoPurl = null;

            EntityImageVO foregroundImage = null;
            for(int i=0;i<images.size();i++)
            {
                if(images.get(i).getZIndex() == 0 && backgroundPhotoUrl == null)
                {
                    backgroundPhotoUrl = images.get(i).getUrl();
                }
                else if(images.get(i).getZIndex() == 1 && foregroundPhotoPurl == null)
                {
                    foregroundPhotoPurl = images.get(i).getUrl();
                    foregroundImage = images.get(i);
                }
            }


            if(backgroundPhotoUrl == null)
                backgroundPhotoUrl = "";

            if(!backgroundPhotoUrl.equals("")) {
                backgroundPhotoLayout.setVisibility(View.VISIBLE);
                backgroundPhotoView.refreshOriginalBitmap();
                backgroundPhotoView.setImageUrl(backgroundPhotoUrl, imgLoader);
                backgroundPhotoView.invalidate();
            }
            else
            {
                backgroundPhotoLayout.setVisibility(View.INVISIBLE);
            }

            if(foregroundImage != null && !foregroundPhotoPurl.equals("")) {
                foregroundPhotoLayout.removeAllViews();

                foregroundPhotoView = new CustomNetworkImageView(OldEntityProfilePreviewActivity.this);
                foregroundPhotoView.setAdustImageAspect(false);
                foregroundPhotoView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);

                Float width = foregroundImage.getWidth();
                Float height = foregroundImage.getHeight();
                Float x = foregroundImage.getLeft();
                Float y = foregroundImage.getTop();
                //if width , height or x,y are not specified , then it means full layout with some padding from background photo
                if (width != null && height != null && x != null && y != null) {
                    int nWidth = Float.valueOf(width * ratio).intValue();
                    int nHeight = Float.valueOf(height * ratio).intValue();
                    int nX = Float.valueOf(x * ratio).intValue();
                    int nY = Float.valueOf(y * ratio).intValue();
                    System.out.println("----(" + nX + "," + nY + ") - (" + nWidth + "," + nHeight + ")----");

                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    //layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    foregroundPhotoLayout.addView(foregroundPhotoView, layoutParams);

                } else {
                    DisplayMetrics dm = Uitils.getResolution(OldEntityProfilePreviewActivity.this);

                    int nXPadding = (int) (dm.widthPixels * 0.10);
                    int nYPadding = (int) (dm.heightPixels * 0.10);

                    int nWidth = dm.widthPixels - nXPadding * 2;
                    int nHeight = dm.heightPixels - nYPadding * 2;
                    int nX = nXPadding;
                    int nY = nYPadding;
                    AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(nWidth, nHeight, nX, nY);
                    foregroundPhotoLayout.addView(foregroundPhotoView, layoutParams);
                }
            }
            if(foregroundPhotoPurl == null || foregroundPhotoPurl.equals(""))
            {
                foregroundPhotoLayout.setVisibility(View.INVISIBLE);
            }
            else
            {
                foregroundPhotoLayout.setVisibility(View.VISIBLE);
                if(foregroundPhotoView != null)
                    foregroundPhotoView.setImageUrl(foregroundPhotoPurl , imgLoader);
            }
        }
        else
        {
            backgroundPhotoLayout.setVisibility(View.INVISIBLE);
            foregroundPhotoLayout.setVisibility(View.INVISIBLE);
            foregroundPhotoLayout.removeAllViews();
        }

        initViewPager();

        updateLockButton();
    }

    private void updateLockButton()
    {
        if(isProfileLocked)
            btnLock.setImageResource(R.drawable.img_bt_lock);
        else
            btnLock.setImageResource(R.drawable.img_bt_unlock);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_ENTITY_INFO && resultCode == RESULT_OK && data!=null)
        {
            this.entity = (EntityVO) data.getSerializableExtra("entity");
            if(entity.getEntityInfos().size() <= 0)
                finish();
            else
                getUIObjects();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity" , this.entity);
        outState.putBoolean("isNewEntity", this.isNewEntity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        this.isNewEntity = savedInstanceState.getBoolean("isNewEntity" , true);

        if(this.entity.getPrivilege()!=null)
            this.isProfileLocked = this.entity.getPrivilege()==0;
        else
            this.isProfileLocked = false;
    }

    private void saveEntity()
    {
        EntityRequest.saveEntity(this.entity , new ResponseCallBack<EntityVO>() {
            @Override
            public void onCompleted(JsonResponse<EntityVO> response) {
                if(response.isSuccess())
                {
                    OldEntityProfilePreviewActivity.this.finish();
                }
                else
                {
                    //if(!isForceClose)
                    MyApp.getInstance().showSimpleAlertDiloag(OldEntityProfilePreviewActivity.this , R.string.str_alert_dialog_failed_to_save_entity , null);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if(isForceClose)
        //    saveEntity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            //click back button
            case R.id.btnPrev:
                //finish();
                saveEntity();
                break;

            //chat button
            case R.id.btnChatNav:
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("entity" , this.entity);
                Intent entityMessageIntent = new Intent(OldEntityProfilePreviewActivity.this , EntityMessageActivity.class);
                entityMessageIntent.putExtras(bundle1);
                startActivity(entityMessageIntent);
                break;

            //save entity
            case R.id.btnHome:
                isForceClose = false;
                saveEntity();
                break;

            //go to entity profile edit screen
            case R.id.btnEditProfile:
                if(isNewEntity)
                {
                    Intent newEntityEditProfileIntent = new Intent(OldEntityProfilePreviewActivity.this , EntityInfoNewProfileAddActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity" , this.entity);
                    newEntityEditProfileIntent.putExtras(bundle);
                    startActivity(newEntityEditProfileIntent);
                    finish();
                }
                else
                {
                    Intent entityProfileIntent = new Intent(OldEntityProfilePreviewActivity.this , EntityInfoProfileEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", this.entity);
                    bundle.putBoolean("isNewEntity", this.isNewEntity);
                    entityProfileIntent.putExtras(bundle);
                    startActivityForResult(entityProfileIntent, EDIT_ENTITY_INFO);
                }
                break;

            case R.id.btnInviteContact:
                Intent inviteContactIntent = new Intent(OldEntityProfilePreviewActivity.this , EntityInviteContactActivity.class);
                inviteContactIntent.putExtra("entityId" , this.entity.getId());
                startActivity(inviteContactIntent);
                break;

            case R.id.btnLock:
                isProfileLocked = !isProfileLocked;
                updateLockButton();
                this.entity.setPrivilege(isProfileLocked?0:1);
                break;
            case R.id.btnVideo:
                Intent videoPlayIntent = new Intent(OldEntityProfilePreviewActivity.this, VideoViewerActivity.class);
                videoPlayIntent.putExtra("video_uri", this.entity.getVideo());
                startActivity(videoPlayIntent);
                break;
        }
    }


    /**
     */
    public class MyPagerAdapter extends FragmentPagerAdapter {

        private List<EntityInfoVO> infos;

        public MyPagerAdapter(FragmentManager fm, List<EntityInfoVO> infos) {
            super(fm);
            this.infos = infos;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragmentMap.remove(position);
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int position) {
            UserEntityProfileFragment ff = (UserEntityProfileFragment) super
                    .instantiateItem(arg0, position);
            //ff.setRatio(ratio);
            //ff.setEntityInfo(infos.get(position));

            if(position == currIndex)
                currentFragment = ff;
            fragmentMap.put(position, ff);
            return ff;
        }

        @Override
        public Fragment getItem(int position) {
            UserEntityProfileFragment ff = null;//new UserEntityProfileFragment(infos.get(position) , ratio);

            if(position == currIndex)
                currentFragment = ff;

            fragmentMap.put(position , ff);

            return ff;
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

            currentFragment = fragmentMap.get(position);
            if(currentFragment!=null)
            {
                //currentFragment.updateListView();
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
