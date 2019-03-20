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

public class EntityInfoProfileEditActivity extends MyBaseFragmentActivity implements OnClickListener ,
                                                                            ActionSheet.ActionSheetListener
{
    private final int EDIT_OR_ADD_INFO = 999;

    /* UI Elements*/
    private ImageButton btnConfirm;
    private ImageView btnDeleteEntity;
    private ImageView btnRemoveLocation , btnRemoveBackground , btnTag , btnGetVideo , btnGetPhoto , btnEditInfo;

    private TouchableFrameLayout editorLayout;
    private MyViewPager mPager;
    private CustomNetworkImageView backgroundPhotoView ;
    private DragableCustomNetworkImageView foregroundPhotoView;
    private FontSelector font_selector;

    private TextView textViewTitle;
    private ActionSheet removeBackgroundsActionSheet = null;

    /* Variables */
    private final int RETAKE_PHOTO = 1991;
    private final int RETAKE_VIDEO = 2000;

    private EntityVO entity;
    private boolean isNewEntity = false;
    private boolean isProfileLocked = true;

    private MyOnPageChangeListener pageListener;
    private MyPagerAdapter pageAdapter = null;
    private HashMap<Integer , EntityInfoProfileEditFragment> fragmentMap;
    private EntityInfoProfileEditFragment currentFragment;
    private int currIndex = 0;
    private float ratio = 1.0f;


    private ImageLoader imgLoader;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_edit_profile);

        if(savedInstanceState!=null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity" , false);
        }
        else {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
            this.isNewEntity = getIntent().getBooleanExtra("isNewEntity", false);
        }
        if (this.entity!=null){
            this.isProfileLocked = this.entity.getPrivilege()==0;
        }

        ratio = Uitils.getScreenRatioViaIPhone(EntityInfoProfileEditActivity.this);

        getUIObjects();
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

        btnDeleteEntity = (ImageView)findViewById(R.id.btnDeleteEntity); btnDeleteEntity.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnRemoveLocation = (ImageView)findViewById(R.id.btnRemoveLocation); btnRemoveLocation.setOnClickListener(this);
        btnRemoveBackground = (ImageView)findViewById(R.id.btnRemoveBackground); btnRemoveBackground.setOnClickListener(this);
        btnTag = (ImageView)findViewById(R.id.btnTag); btnTag.setOnClickListener(this);
        btnGetVideo = (ImageView)findViewById(R.id.btnGetVideo); btnGetVideo.setOnClickListener(this);
        btnGetPhoto = (ImageView)findViewById(R.id.btnGetPhoto); btnGetPhoto.setOnClickListener(this);
        btnEditInfo = (ImageView)findViewById(R.id.btnEditInfo); btnEditInfo.setOnClickListener(this);

        if(entity.getEntityInfos().size()>1)
            btnRemoveLocation.setVisibility(View.VISIBLE);
        else
            btnRemoveLocation.setVisibility(View.INVISIBLE);

        //set background and foreground picture
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

                foregroundPhotoView = new DragableCustomNetworkImageView(EntityInfoProfileEditActivity.this);
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
                    DisplayMetrics dm = Uitils.getResolution(EntityInfoProfileEditActivity.this);

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

    }

    private void updateTagIcon(boolean isAbbr)
    {
        btnTag.setImageResource(isAbbr ? R.drawable.tag_selected : R.drawable.tag_none );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity", entity);
        outState.putBoolean("isNewEntity", this.isNewEntity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        this.isNewEntity = savedInstanceState.getBoolean("isNewEntity" , false);

        if(this.entity.getPrivilege()!=null)
            this.isProfileLocked = this.entity.getPrivilege()==0;
        else
            this.isProfileLocked = false;

        getUIObjects();
    }

    private void saveCurrentEntityProfile()
    {
        ratio = Uitils.getScreenRatioViaIPhone(EntityInfoProfileEditActivity.this);

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
                                    //return empty entity to the prview screen , then the preview screen closes automatically
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    entity.getEntityInfos().clear();
                                    bundle.putSerializable("entity" , entity);
                                    intent.putExtras(bundle);
                                    setResult(RESULT_OK , intent);
                                    finish();
                                }
                                else
                                {
                                    MyApp.getInstance().showSimpleAlertDiloag(EntityInfoProfileEditActivity.this ,  R.string.str_alert_dialog_failed_to_delete_entity , null);
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
                        if(entity.getEntityInfos().size()>1)
                            btnRemoveLocation.setVisibility(View.VISIBLE);
                        else
                            btnRemoveLocation.setVisibility(View.INVISIBLE);

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

            case R.id.btnConfirm:

                //save current entity profile
                saveCurrentEntityProfile();

                if(isNewEntity) {
                    Intent entityProfilePreviewIntent = new Intent(EntityInfoProfileEditActivity.this, OldEntityProfilePreviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", this.entity);
                    bundle.putBoolean("isNewEntity", true);
                    entityProfilePreviewIntent.putExtras(bundle);
                    startActivity(entityProfilePreviewIntent);
                    finish();
                }else {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", this.entity);
                    intent.putExtras(bundle);
                    this.setResult(RESULT_OK, intent);
                    finish();
                }
                break;

            case R.id.btnRemoveBackground:
                setTheme(R.style.ActionSheetStyleIOS7);
                if(removeBackgroundsActionSheet == null)
                    removeBackgroundsActionSheet = ActionSheet.createBuilder(EntityInfoProfileEditActivity.this, getSupportFragmentManager())
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

            case R.id.btnTag:
                boolean isAbbr = this.entity.getEntityInfos().get(currIndex).getAbbr();
                isAbbr = !isAbbr;
                this.entity.getEntityInfos().get(currIndex).setAbbr(isAbbr);
                updateTagIcon(isAbbr);
                if(currentFragment!=null)
                    currentFragment.updateTag(isAbbr);

                break;

            case R.id.btnGetPhoto:

                //save current entity profile
                saveCurrentEntityProfile();

                Intent photoEditorIntent = new Intent(EntityInfoProfileEditActivity.this,TradeCardPhotoEditorSetActivity.class);
                photoEditorIntent.putExtra("isSetNewPhotoInfo" , false);
                photoEditorIntent.putExtra("tradecardType" , ConstValues.ENTITY_PHOTO_EDITOR);
                Bundle photoBundle = new Bundle();
                photoBundle.putSerializable("entity", this.entity);
                photoEditorIntent.putExtras(photoBundle);
                startActivityForResult(photoEditorIntent, RETAKE_PHOTO);

                break;

            case R.id.btnGetVideo:
                //save current entity profile
                saveCurrentEntityProfile();

                Intent entityVideoSetIntent=  new Intent(EntityInfoProfileEditActivity.this , EntitySetVideoActivity.class);
                Bundle bunlde = new Bundle();
                bunlde.putBoolean("isSetNewVideo" , false);
                bunlde.putSerializable("entity" , this.entity);
                entityVideoSetIntent.putExtras(bunlde);
                startActivityForResult(entityVideoSetIntent, RETAKE_VIDEO);

                break;

            case R.id.btnEditInfo:
                /*//save current edited profile
                Iterator<Integer> fragmentsKeys = fragmentMap.keySet().iterator();
                while(fragmentsKeys.hasNext())
                {
                    Integer key = fragmentsKeys.next();
                    EntityInfoProfileEditFragment ff = fragmentMap.get(key);
                    if(ff!=null)
                        ff.save();
                }
                this.entity.setPrivilege(isProfileLocked?0:1);*/


                //save current entity profile
                saveCurrentEntityProfile();

                Bundle edtInfoBundle = new Bundle();
                edtInfoBundle.putSerializable("entity", entity);
                edtInfoBundle.putSerializable("isNewEntity", false);
                Intent intent = new Intent(EntityInfoProfileEditActivity.this, EntityInfoInputActivity.class);
                intent.putExtras(edtInfoBundle);
                startActivityForResult(intent, EDIT_OR_ADD_INFO);

                break;
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
                                foregroundPhotoView.setImageUrl("", imgLoader);
                                foregroundPhotoView.invalidate();

                                editorLayout.setChildViewToDelegateTouchEvent(null);
                                AbsoluteLayout foregroundPhotoLayout = (AbsoluteLayout)findViewById(R.id.foregroundPhotoLayout);
                                foregroundPhotoLayout.removeAllViews();
                                foregroundPhotoView = null;
                                foregroundPhotoLayout.setVisibility(View.INVISIBLE);
                            } else {
                                MyApp.getInstance().showSimpleAlertDiloag(EntityInfoProfileEditActivity.this, R.string.str_alert_failed_to_remove_foreground_photo, null);
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
                    EntityRequest.removeImage( this.entity.getId(), images.get(imgIndex).getId(), new ResponseCallBack<Void>() {
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
                                MyApp.getInstance().showSimpleAlertDiloag(EntityInfoProfileEditActivity.this, R.string.str_alert_failed_to_remove_background_photo, null);
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
        if(resultCode == RESULT_OK)
        {
            if(requestCode == RETAKE_PHOTO || requestCode == RETAKE_VIDEO)
            {
                if(data!=null)
                {
                    Bundle bundle = data.getExtras();
                    this.entity = (EntityVO) bundle.getSerializable("entity");
                    getUIObjects();
                }
            }
            else if(requestCode == EDIT_OR_ADD_INFO && data!=null)
            {
                this.entity = (EntityVO) data.getSerializableExtra("entity");
                if(entity.getEntityInfos().size() <= 0)
                    finish();
                else
                    getUIObjects();
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
