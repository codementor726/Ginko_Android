package com.ginko.activity.entity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.DragableCustomNetworkImageView;
import com.ginko.customview.FontSelector;
import com.ginko.customview.MyViewPager;
import com.ginko.customview.TouchableFrameLayout;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityInfoProfileEditFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EntityInfoNewProfileAddActivity extends MyBaseFragmentActivity implements OnClickListener,
                                                                                            ActionSheet.ActionSheetListener
{

    /* UI Elements*/
    private TouchableFrameLayout editorLayout;
    private ImageButton btnNext;
    private ImageView   btnRemoveLocation , btnDeleteEntity;
    private TextView    textViewTitle;
    private ImageView   btnTag , btnRemoveBackground , btnLock , btnGetPhoto , btnEditInfo;
    private MyViewPager   mPager;
    private CustomNetworkImageView backgroundPhotoView;
    private DragableCustomNetworkImageView foregroundPhotoView;
    private FontSelector font_selector;

    /* Variables */
    private final int RETAKE_PHOTO = 1991;

    private EntityVO entity;
    private boolean isProfileLocked = false;

    private MyOnPageChangeListener pageListener;
    private MyPagerAdapter pageAdapter = null;
    private HashMap<Integer , EntityInfoProfileEditFragment> fragmentMap;
    private EntityInfoProfileEditFragment currentFragment;
    private int currIndex = 0;
    private float ratio = 1.0f;

    private ActionSheet removeBackgroundsActionSheet = null;

    private ImageLoader imgLoader;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_add_new_profile);

        if(savedInstanceState!=null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        }
        else {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
        }
		if (this.entity!=null){
			this.isProfileLocked = this.entity.getPrivilege()==0;
		}

        ratio = Uitils.getScreenRatioViaIPhone(EntityInfoNewProfileAddActivity.this);

        getUIObjects();
   }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity", entity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.entity = (EntityVO) savedInstanceState.getSerializable("entity");

        if(this.entity.getPrivilege()!=null)
            this.isProfileLocked = this.entity.getPrivilege()==0;
        else
            this.isProfileLocked = false;

        getUIObjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //disable back key
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setScanScroll(true);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),  this.entity.getEntityInfos());
        pageListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(pageListener);
        mPager.setAdapter(pageAdapter);
        mPager.setCurrentItem(currIndex);
        pageListener.onPageSelected(currIndex);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        fragmentMap = new HashMap<Integer , EntityInfoProfileEditFragment>();

        imgLoader = MyApp.getInstance().getImageLoader();

        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        if(this.entity.getEntityInfos().size()>1)
        {
            textViewTitle.setText(getResources().getString(R.string.entity_info)+" "+String.valueOf(entity.getEntityInfos().size()-1));
        }
        else
        {
            textViewTitle.setText(getResources().getString(R.string.entity_info));
        }

        font_selector = (FontSelector)findViewById(R.id.font_selector);

        btnRemoveLocation = (ImageView)findViewById(R.id.btnRemoveLocation); btnRemoveLocation.setOnClickListener(this);
        btnDeleteEntity = (ImageView)findViewById(R.id.btnDeleteEntity); btnDeleteEntity.setOnClickListener(this);


        btnNext = (ImageButton)findViewById(R.id.btnNext); btnNext.setOnClickListener(this);
        btnTag = (ImageView)findViewById(R.id.btnTag); btnTag.setOnClickListener(this);
        btnRemoveBackground = (ImageView)findViewById(R.id.btnRemoveBackground); btnRemoveBackground.setOnClickListener(this);
        btnLock = (ImageView)findViewById(R.id.btnLock); btnLock.setOnClickListener(this);
        btnGetPhoto = (ImageView)findViewById(R.id.btnGetPhoto); btnGetPhoto.setOnClickListener(this);
        btnEditInfo = (ImageView)findViewById(R.id.btnEditInfo); btnEditInfo.setOnClickListener(this);

        if(this.entity.getEntityInfos().size()>1)
            btnRemoveLocation.setVisibility(View.VISIBLE);
        else
            btnRemoveLocation.setVisibility(View.GONE);

        backgroundPhotoView = (CustomNetworkImageView)findViewById(R.id.imgBackgroundPhoto);

        LinearLayout backgroundPhotoLayout = (LinearLayout)findViewById(R.id.backgroundPhotoLayout);
        AbsoluteLayout foregroundPhotoLayout = (AbsoluteLayout)findViewById(R.id.foregroundPhotoLayout);

        editorLayout = (TouchableFrameLayout)findViewById(R.id.editorLayout);

        List<EntityImageVO> images = this.entity.getEntityImages();
        if(images.size() > 0)
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

                foregroundPhotoView = new DragableCustomNetworkImageView(EntityInfoNewProfileAddActivity.this);
                foregroundPhotoView.setAdustImageAspect(false);
                foregroundPhotoView.setImageScaleType(ImageView.ScaleType.FIT_CENTER);
                foregroundPhotoView.setImageUrl(foregroundImage.getUrl(), imgLoader);

                editorLayout.setChildViewToDelegateTouchEvent((View) foregroundPhotoView);


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
                    DisplayMetrics dm = Uitils.getResolution(EntityInfoNewProfileAddActivity.this);

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

        updateLockIcon();
        updateDeleteRemoveButtons();
    }

    private void updateDeleteRemoveButtons()
    {

        if(this.entity.getEntityInfos().size()>1)//if has more than one entity info
        {
            btnRemoveLocation.setVisibility(View.VISIBLE);
            btnDeleteEntity.setVisibility(View.GONE);
        }
        else
        {
            btnRemoveLocation.setVisibility(View.GONE);
            btnDeleteEntity.setVisibility(View.VISIBLE);
        }
    }

    private void updateLockIcon()
    {
        if(isProfileLocked)
            btnLock.setImageResource(R.drawable.img_bt_lock);
        else
            btnLock.setImageResource(R.drawable.img_bt_unlock);
    }

    private void updateTagIcon(boolean isAbbr)
    {
        btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
    }


    private void saveCurrentProfile()
    {
        ratio = Uitils.getScreenRatioViaIPhone(EntityInfoNewProfileAddActivity.this);

        List<EntityImageVO>  images = entity.getEntityImages();
        int foregroundPhotoIndex = -1;
        for(int i= 0;i<images.size(); i++)
        {
            if(images.get(i).getZIndex() == 1)
            {
                foregroundPhotoIndex = i;
                break;
            }
        }
        if(foregroundPhotoView != null && foregroundPhotoIndex > 0)
        {
            EntityImageVO image = images.get(foregroundPhotoIndex);

            int foregroundLeft = (int)(foregroundPhotoView.getLeft()  / (ratio * 1.0));
            int foregroundTop = (int)(foregroundPhotoView.getTop() / (ratio * 1.0));
            int foregroundWidth = (int)(foregroundPhotoView.getWidth() / (ratio * 1.0));
            int foregroundHeight = (int)(foregroundPhotoView.getHeight() / (ratio * 1.0));

            image.setWidth(Float.valueOf((float) foregroundWidth));
            image.setHeight(Float.valueOf((float) foregroundHeight));
            image.setTop(Float.valueOf((float) foregroundTop));
            image.setLeft(Float.valueOf((float) foregroundLeft));
        }
        //get all field values from edit boxes
        Iterator<Integer> fragmentKeys = fragmentMap.keySet().iterator();
        while(fragmentKeys.hasNext())
        {
            Integer key = fragmentKeys.next();
            EntityInfoProfileEditFragment ff = fragmentMap.get(key);
            if(ff!=null)
                ff.save();
        }
        this.entity.setPrivilege(isProfileLocked?0:1);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnDeleteEntity:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_entity));
                builder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        EntityRequest.deleteEntity(entity.getId() , new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                {
                                    finish();
                                }
                                else
                                {
                                    MyApp.getInstance().showSimpleAlertDiloag(EntityInfoNewProfileAddActivity.this ,  R.string.str_alert_dialog_failed_to_delete_entity , null);
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.btnRemoveLocation:
                AlertDialog.Builder removeLocationAlertBuilder = new AlertDialog.Builder(this);
                removeLocationAlertBuilder.setTitle("Confirm");
                removeLocationAlertBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_entity_location));
                removeLocationAlertBuilder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        if (currIndex < entity.getEntityInfos().size())
                            entity.getEntityInfos().remove(currIndex);
                        currIndex--;
                        if (currIndex < 0)
                            currIndex = 0;
                        pageAdapter.notifyDataSetChanged();
                        mPager.setCurrentItem(currIndex);
                        pageListener.onPageSelected(currIndex);
                        updateDeleteRemoveButtons();

                        dialog.dismiss();
                    }
                });
                removeLocationAlertBuilder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                removeLocationAlertBuilder.create().show();
                break;

            case R.id.btnNext:
                //save entity
                saveCurrentProfile();

                //remove video feature
                /*Intent entityVideoSetIntent=  new Intent(EntityInfoNewProfileAddActivity.this , EntitySetVideoActivity.class);
                Bundle bunlde = new Bundle();
                bunlde.putBoolean("isSetNewVideo" , true);
                bunlde.putSerializable("entity" , this.entity);
                entityVideoSetIntent.putExtras(bunlde);
                startActivity(entityVideoSetIntent);
                finish();*/

                EntityRequest.saveEntity(this.entity, new ResponseCallBack<EntityVO>() {
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
                            Intent entityProfilePreviewIntent = new Intent(EntityInfoNewProfileAddActivity.this , OldEntityProfilePreviewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("entity" , entity);
                            bundle.putBoolean("isNewEntity" , true);
                            entityProfilePreviewIntent.putExtras(bundle);
                            startActivity(entityProfilePreviewIntent);
                            EntityInfoNewProfileAddActivity.this.finish();
                        } else {
                            //if(!isForceClose)
                            MyApp.getInstance().showSimpleAlertDiloag(EntityInfoNewProfileAddActivity.this, R.string.str_alert_dialog_failed_to_save_entity, null);
                        }
                    }
                });

                break;

            case R.id.btnTag:
                boolean isAbbr = this.entity.getEntityInfos().get(currIndex).getAbbr();
                isAbbr = !isAbbr;
                this.entity.getEntityInfos().get(currIndex).setAbbr(isAbbr);
                updateTagIcon(isAbbr);
                if(currentFragment!=null)
                    currentFragment.updateTag(isAbbr);

                break;

            case R.id.btnRemoveBackground:
                setTheme(R.style.ActionSheetStyleIOS7);
                if(removeBackgroundsActionSheet == null)
                    removeBackgroundsActionSheet = ActionSheet.createBuilder(EntityInfoNewProfileAddActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(
                                    getResources().getString(R.string.str_remove_foreground_photo) ,
                                    getResources().getString(R.string.str_remove_background_photo))
                            .setCancelableOnTouchOutside(true)
                            .setListener(this)
                            .show();
                else
                    removeBackgroundsActionSheet.show(getSupportFragmentManager() , "actionSheet");
                break;

            case R.id.btnLock:
                isProfileLocked = !isProfileLocked;
                this.entity.setPrivilege(isProfileLocked?0:1);
                updateLockIcon();
                break;

            case R.id.btnGetPhoto:
                //save entity
                saveCurrentProfile();

                Intent photoEditorIntent = new Intent(EntityInfoNewProfileAddActivity.this,TradeCardPhotoEditorSetActivity.class);
                photoEditorIntent.putExtra("isSetNewPhotoInfo" , false);
                photoEditorIntent.putExtra("tradecardType" , ConstValues.ENTITY_PHOTO_EDITOR);
                Bundle photoBundle = new Bundle();
                photoBundle.putSerializable("entity", this.entity);
                photoEditorIntent.putExtras(photoBundle);
                startActivityForResult(photoEditorIntent, RETAKE_PHOTO);
                break;

            case R.id.btnEditInfo:
                //save entity
                saveCurrentProfile();

                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("entity", entity);
                bundle1.putSerializable("isNewEntity" , true);
                Intent intent = new Intent(EntityInfoNewProfileAddActivity.this, EntityInfoInputActivity.class);
                intent.putExtras(bundle1);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RETAKE_PHOTO && resultCode == RESULT_OK)
        {
            if(data!=null)
            {
                Bundle bundle = data.getExtras();
                this.entity = (EntityVO) bundle.getSerializable("entity");
                getUIObjects();
            }

        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//remove foreground photo
        {
            final List<EntityImageVO> images = this.entity.getEntityImages();
            if(images == null || images.size()<1)
            {
                MyApp.getInstance().showSimpleAlertDiloag(this, R.string.str_alert_no_foreground_photo_to_remove, null);
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
                if(images.get(imgIndex).getId() !=  null) {
                    EntityRequest.removeImage(this.entity.getId(), images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                images.remove(imgIndex);
                                foregroundPhotoView.refreshOriginalBitmap();
                                foregroundPhotoView.setImageUrl("" , imgLoader);
                                foregroundPhotoView.invalidate();

                                editorLayout.setChildViewToDelegateTouchEvent(null);
                                AbsoluteLayout foregroundPhotoLayout = (AbsoluteLayout)findViewById(R.id.foregroundPhotoLayout);
                                foregroundPhotoLayout.removeAllViews();
                                foregroundPhotoView = null;
                                foregroundPhotoLayout.setVisibility(View.INVISIBLE);
                            } else {
                                MyApp.getInstance().showSimpleAlertDiloag(EntityInfoNewProfileAddActivity.this, R.string.str_alert_failed_to_remove_foreground_photo, null);
                            }
                        }
                    });
                }
            }
        }
        else if(index == 1)//remove background photo
        {
            final List<EntityImageVO> images = this.entity.getEntityImages();
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
                final int imgIndex = i;

                if(images.get(imgIndex).getId() != null) {
                    EntityRequest.removeImage(this.entity.getId(), images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                images.remove(imgIndex);
                                backgroundPhotoView.refreshOriginalBitmap();
                                backgroundPhotoView.setImageUrl("" , imgLoader);
                                backgroundPhotoView.invalidate();
                                LinearLayout backgroundPhotoLayout = (LinearLayout)findViewById(R.id.backgroundPhotoLayout);
                                backgroundPhotoLayout.setVisibility(View.INVISIBLE);

                            }
                            else
                            {
                                MyApp.getInstance().showSimpleAlertDiloag(EntityInfoNewProfileAddActivity.this, R.string.str_alert_failed_to_remove_background_photo, null);
                            }
                        }
                    });
                }
            }
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
            EntityInfoProfileEditFragment ff = (EntityInfoProfileEditFragment) super
                    .instantiateItem(arg0, position);
            ff.setFontSelector(font_selector);
            ff.setRatio(ratio);
            ff.setEntityInfoItemList(infos.get(position));
            ff.position = position;
            if(position == currIndex)
                currentFragment = ff;
            fragmentMap.put(position, ff);
            return ff;
        }

        @Override
        public Fragment getItem(int position) {
            EntityInfoProfileEditFragment ff = new EntityInfoProfileEditFragment(infos.get(position) , font_selector , ratio);
            ff.refreshFieldsData();

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
            boolean isAbbr = entity.getEntityInfos().get(currIndex).getAbbr();
            updateTagIcon(isAbbr);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
