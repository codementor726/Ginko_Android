package com.ginko.activity.entity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.menu.MenuActivity;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.Uitils;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.EntityProfileFieldAddOverlayView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;


import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddInfosForEachLocation extends MyBaseFragmentActivity implements View.OnClickListener,
        EntityProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener ,
        EntityAddInfoFragment.OnKeyDownListener,
        ActionSheet.ActionSheetListener{


    /* UI Variables */
    private LinearLayout activityRootView;
    private ImageButton btnDeleteLocation;
    private ImageView btnAddProfileField;
    private ImageView imgDimBackground;
    private EntityProfileFieldAddOverlayView addFieldOverlayView;
    private Button btnDone;
    private ImageButton btnBack;
    RelativeLayout headerlayout;

    private TextView textViewTitle;

    private EntityAddInfoFragment infoListFragment;

    /* Variables */
    private EntityVO entity;
    private EntityVO multiEntity;
    private boolean isNewEntity = false;
    private boolean isCreate = false;
    private int currentIndex;


    private boolean isSharedPrivilege = true;
    private boolean isKeyboardVisible = false;
    private boolean isTakingProfilePhoto = false;
    private boolean isEntityProfileVideo = false;

    private boolean isAddFields = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entity_multi_location_infos);


        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity", false);
            this.isCreate = savedInstanceState.getBoolean("isCreate", false);
            this.currentIndex = 0;
        }
        else {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
            this.isNewEntity = getIntent().getBooleanExtra("isNewEntity" , false);
            this.isCreate = getIntent().getBooleanExtra("isCreate", false);
            this.currentIndex = getIntent().getIntExtra("currentIndex",0);
        }

        if(this.entity.getPrivilege()!=null)
            this.isSharedPrivilege = this.entity.getPrivilege()>0?true:false;
        else
            this.isSharedPrivilege = false;

        multiEntity = new EntityVO();
        getUIObjects();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity", entity);
        outState.putBoolean("isNewEntity", this.isNewEntity);
        outState.putBoolean("isCreate", this.isCreate);
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        textViewTitle = (TextView)findViewById(R.id.textViewTitle);

        if (!isNewEntity)
            textViewTitle.setText("Edit Entity Profile");
        else
            textViewTitle.setText("Create Entity Profile");

        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);

        addFieldOverlayView = (EntityProfileFieldAddOverlayView)findViewById(R.id.addFieldOverlayView);
        addFieldOverlayView.setOnProfileFieldItemsChangeListener(this); addFieldOverlayView.setVisibility(View.GONE);

        btnAddProfileField = (ImageView)findViewById(R.id.btnAddFieldInfoItem); btnAddProfileField.setOnClickListener(this);
        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);

        btnDeleteLocation = (ImageButton) findViewById(R.id.btnDeleteLocation); btnDeleteLocation.setOnClickListener(this);
        if (entity.getEntityInfos().size() > 1)
            btnDeleteLocation.setVisibility(View.VISIBLE);
        else
            btnDeleteLocation.setVisibility(View.INVISIBLE);

        imgDimBackground = (ImageView)findViewById(R.id.imgDimBackground);
        imgDimBackground.setVisibility(View.GONE);

        activityRootView = (LinearLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        if (infoListFragment != null)
                            infoListFragment.setKeyboardVisibilty(true);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        if (infoListFragment != null)
                            infoListFragment.setKeyboardVisibilty(false);
                    }
                }
            }
        });

        activityRootView.setClickable(true);
        activityRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MyApp.getInstance().hideKeyboard(activityRootView);
                return false;
            }
        });

        if (!isNewEntity){
            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            btnBack.setImageResource(R.drawable.btn_back_nav_black);
            textViewTitle.setTextColor(getResources().getColor(R.color.black));
            btnDone.setTextColor(getResources().getColor(R.color.black));
        }else{
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnBack.setImageResource(R.drawable.btn_back_nav_white);
            textViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color));
        }

        initInfoItemFragment();
    }

    private void initInfoItemFragment()
    {
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;
        if(entity.getEntityInfos().size() < currentIndex + 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
        }
        else
            entityInfo = entity.getEntityInfos().get(currentIndex);

        infoListFragment = EntityAddInfoFragment.newInstance(entityInfo,true);
        infoListFragment.setOnProfileFieldItemsChangeListener(this);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
         * Disable back button to go to previsous screen
         */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(addFieldOverlayView.getVisibility() == View.VISIBLE)
        {
            imgDimBackground.setVisibility(View.GONE);
            addFieldOverlayView.hideView();
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            return;
        }
        else
            //if(entity.getId() != 0)
            super.onBackPressed();
    }

    private void goToMultiLocationEntityEditActivity(boolean isNewEntity)
    {
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity" , entity);
        bundle.putInt("currentIndex", currentIndex);
        bundle.putBoolean("isDeleted",false);
        resultIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void hideKeyboard()
    {
        MyApp.getInstance().hideKeyboard(activityRootView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
                finish();
                break;

            case R.id.btnDone:
                gotToDoneOrNext();
                break;

            //add profile field items
            case R.id.btnAddFieldInfoItem:
                if(infoListFragment == null) return;

                MyApp.getInstance().hideKeyboard(activityRootView);
                addFieldOverlayView.setProfileFieldItems(infoListFragment.getCurrentVisibleInfoItems());

                if(addFieldOverlayView.getVisibility() == View.GONE && !isAddFields)
                {
                    MyApp.getInstance().hideKeyboard(activityRootView);
                    imgDimBackground.setVisibility(View.VISIBLE);
                    imgDimBackground.setFocusable(true);
                    addFieldOverlayView.showView();
                    btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                }
                else
                {
                    imgDimBackground.setVisibility(View.GONE);
                    imgDimBackground.setFocusable(false);
                    addFieldOverlayView.hideView();
                    if(isAddFields)
                        btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                    else
                        btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
                }
                break;
            case R.id.btnDeleteLocation:
                if(addFieldOverlayView.getVisibility() == View.VISIBLE)
                    return;
                AlertDialog.Builder alertDeleteItems = new AlertDialog.Builder(this);
                alertDeleteItems.setTitle("Confirm");
                alertDeleteItems.setMessage("Do you want to delete this location?");
                alertDeleteItems.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("entity", entity);
                        bundle.putInt("currentIndex", currentIndex);
                        bundle.putBoolean("isDeleted", true);
                        resultIntent.putExtras(bundle);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                        /*
                        if(entity.getId() == 0) {

                        } else {
                            EntityRequest.deleteInfo(entity.getId(), entity.getEntityInfos().get(currentIndex).getId(), new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        EntityRequest.getEntity(entity.getId(), new ResponseCallBack<EntityVO>() {
                                            @Override
                                            public void onCompleted(JsonResponse<EntityVO> response) {
                                                if (response.isSuccess()) {
                                                    Intent resultIntent = new Intent();
                                                    Bundle bundle = new Bundle();
                                                    entity = response.getData();
                                                    for (int i = 0; i < entity.getEntityInfos().size(); i++) {
                                                        EntityInfoVO location = entity.getEntityInfos().get(i);
                                                        if (location.isAddressConfirmed() == false) {
                                                            location.setLatitude(null);
                                                            location.setLongitude(null);
                                                        }
                                                    }
                                                    bundle.putSerializable("entity", entity);
                                                    bundle.putInt("currentIndex", currentIndex);
                                                    bundle.putBoolean("isDeleted", true);
                                                    resultIntent.putExtras(bundle);
                                                    setResult(Activity.RESULT_OK, resultIntent);
                                                    finish();
                                                }
                                            }
                                        });
                                    } else {
                                        Intent resultIntent = new Intent();
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("entity", entity);
                                        bundle.putInt("currentIndex", currentIndex);
                                        bundle.putBoolean("isDeleted", true);
                                        resultIntent.putExtras(bundle);
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();
                                    }
                                }
                            });
                        }
                        */
                    }
                });
                alertDeleteItems.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int paramInt) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });
                alertDeleteItems.show();
                break;
        }
    }

    public void gotToDoneOrNext()
    {
        EntityInfoVO entityInfoVO = null;
        if(infoListFragment != null)
            entityInfoVO = infoListFragment.saveEntityInfo(AddInfosForEachLocation.this, true);
        if(entityInfoVO == null)
            return;

        if(entityInfoVO.getEntityInfoDetails().size() < 1)
        {
            //if input fields have invalid value
            MyApp.getInstance().showSimpleAlertDiloag(AddInfosForEachLocation.this, "Oops!", R.string.str_alert_dialog_entity_at_least_one_contact_info, null);
            return;
        }

//        for(EntityInfoVO info:entity.getEntityInfos())
//        {
//            if(entity.getEntityInfos().get(currentIndex) != null) {
//                entityInfoVO.setId(info.getId());
//                break;
//            }
//        }
        if (entity.getEntityInfos().get(currentIndex).getEntityInfoDetails().size()> 0)
            entityInfoVO.setId(entity.getEntityInfos().get(currentIndex).getId());

        entity.getEntityInfos().set(currentIndex, entityInfoVO);
        hideKeyboard();
        goToMultiLocationEntityEditActivity(isNewEntity);

    }

    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {

    }

    @Override
    public void onAddedNewProfileField(String fieldName) {
        if(fieldName.equals("noExistAddFields")){
            imgDimBackground.setVisibility(View.GONE);
            imgDimBackground.setFocusable(false);
            addFieldOverlayView.hideOverlapView();
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            isAddFields = true;
            return;
        }
        if(infoListFragment != null)
            infoListFragment.addNewInfoItem(fieldName);
    }

    @Override
    public void onRemovedProfileField(String fieldName) {
        if(infoListFragment != null) {
            isAddFields = false;
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);

            infoListFragment.removeInfoItem(fieldName);
        }
    }
}
