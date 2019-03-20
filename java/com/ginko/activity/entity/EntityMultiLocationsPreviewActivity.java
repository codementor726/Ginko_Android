package com.ginko.activity.entity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ginko.activity.menu.MenuActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityAddInfoFragment;
import com.ginko.fragments.EntityMultiLocationsPreviewFragment;
import com.ginko.fragments.EntityProfilePreviewFragment;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class EntityMultiLocationsPreviewActivity extends MyBaseFragmentActivity implements View.OnClickListener{

    public static final int STATIC_INTEGER_VALUE = 101;
    public static final int STATIC_MORE_INTEGER_VALUE = 65638;

    public static final int PICK_MULTI_REQUEST = 202;

    /* UI Variables*/
    private ImageButton btnBack , btnChat;
    private Button btnDelete , btnEdit;
    private ImageButton btnInviteContact;

    private EntityMultiLocationsPreviewFragment infoListFragment;

    /* Variables */
    private EntityVO entity;
    private boolean isProfileLocked = false;
    private boolean isChanged = false;


    private Object lockObj = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multilocations_entity_profile_preview);

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        }
        else
        {
            this.entity = (EntityVO) this.getIntent().getSerializableExtra("entity");
        }

        this.isProfileLocked = this.entity.getPrivilege()==0;


        getUIObjects();
    }

    private void getEntityInfo() {
        EntityRequest.getEntity(entity.getId(), new ResponseCallBack<EntityVO>() {
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
                    resumeInfoItemFragment();
                }
            }
        });
    }
    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnDelete = (Button)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
        btnEdit = (Button)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        btnChat = (ImageButton)findViewById(R.id.btnChat); btnChat.setOnClickListener(this);

        btnInviteContact = (ImageButton)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);

        initInfoItemFragment();
    }

    private void initInfoItemFragment()
    {
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;
        if(entity.getEntityInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
        }
        else
            entityInfo = entity.getEntityInfos().get(0);

        infoListFragment = EntityMultiLocationsPreviewFragment.newInstance(entity, entityInfo, false, false);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();
    }

    private void resumeInfoItemFragment()
    {
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;
        if(entity.getEntityInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
        }
        else
            entityInfo = entity.getEntityInfos().get(0);

        infoListFragment = EntityMultiLocationsPreviewFragment.newInstance(entity, entityInfo, false, false);
        Handler handler_ = new Handler(Looper.getMainLooper());
        handler_.postDelayed(new Runnable() {
            @Override
            public void run() {
                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fieldsLayout, infoListFragment);
                ft.commitAllowingStateLoss();
            }
        }, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(entity != null) {
            getEntityInfo();
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("entity" , this.entity);
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case STATIC_INTEGER_VALUE:
                    entity = (EntityVO) data.getSerializableExtra("entity");
                    resumeInfoItemFragment();
                    break;
                case STATIC_MORE_INTEGER_VALUE:
                    entity = (EntityVO) data.getSerializableExtra("entity");
                    resumeInfoItemFragment();
                    break;
                case PICK_MULTI_REQUEST:
                    if (data != null)
                    {
                        //Crash Error GAD-1689
                        EntityVO tempVO = (EntityVO) data.getSerializableExtra("entity");
                        if (tempVO != null)
                            entity = tempVO;
                        resumeInfoItemFragment();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EntityMultiLocationsPreviewActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack: {
                Intent intent = new Intent(EntityMultiLocationsPreviewActivity.this, MenuActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.btnEdit:
                Bundle bundle = new Bundle();
                bundle.putSerializable("entity", entity);
                bundle.putBoolean("isMultiLocations", true);
                bundle.putBoolean("isCreate", false);
                bundle.putBoolean("isNewEntity", false);
                Intent intent = new Intent(EntityMultiLocationsPreviewActivity.this,MultiLocationEntityEditActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, PICK_MULTI_REQUEST);
                //finish();
                break;

            case R.id.btnDelete:
                AlertDialog.Builder builder = new AlertDialog.Builder(EntityMultiLocationsPreviewActivity.this);
                builder.setTitle("GINKO");
                builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_delete_entity));
                builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        EntityRequest.deleteEntity(entity.getId(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    Intent intent = new Intent(EntityMultiLocationsPreviewActivity.this, MenuActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {

                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.alert_no_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;

            case R.id.btnInviteContact:
                Intent inviteContactIntent = new Intent(EntityMultiLocationsPreviewActivity.this , EntityInviteContactActivity.class);
                inviteContactIntent.putExtra("entityId" , this.entity.getId());
                startActivity(inviteContactIntent);
                break;

            case R.id.btnChat:
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("entity" , this.entity);
                Intent entityMessageIntent = new Intent(EntityMultiLocationsPreviewActivity.this , EntityMessageActivity.class);
                entityMessageIntent.putExtras(bundle1);
                startActivity(entityMessageIntent);
                break;
        }
    }
}
