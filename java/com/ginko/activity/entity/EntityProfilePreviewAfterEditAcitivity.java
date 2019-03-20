package com.ginko.activity.entity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.menu.MenuActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.SpoutRequest;
import com.ginko.common.Logger;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityAddInfoFragment;
import com.ginko.fragments.EntityProfilePreviewFragment;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.security.PrivateKey;

public class EntityProfilePreviewAfterEditAcitivity extends MyBaseFragmentActivity implements View.OnClickListener{

    public static final int ENTITY_EDIT_NUM = 101;
    /* UI Variables*/
    private ImageButton btnChat;
    private Button btnDelete , btnEdit, btnDone;
    private ImageButton btnInviteContact;
    private TextView txtTitle;
    private RelativeLayout headerlayout;

    private EntityProfilePreviewFragment infoListFragment;

    /* Variables */
    private EntityVO entity;
    private boolean isProfileLocked = false;
    private boolean isMultiLocations = false;
    private int infoId = 0;
    private int currentIndex= 0;

    /* Location Manager to get current location info */
    private LocationManager lm;
    private boolean isGettingCurrentLocation = false;
    private Location latestLocation = null;
    private Handler locationHandler;
    private LocationListener locationListener = new MyLocationListener();
    private Looper locationLooper;
    private Marker currentPosMarker = null;
    private LatLng currentLoc =  null;
    private final int GET_CURRENT_LOCATION_TIME_OUT = 10000;

    private Runnable locationTimeoutRunnable = new Runnable() {
        public void run() {
            synchronized (lockObj) {
                lm.removeUpdates(locationListener);
                //if (progressDialog != null)
                //    progressDialog.dismiss();
                isGettingCurrentLocation = false;
            }
        }
    };
    private Object lockObj = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_profile_preview_after_edit_acitivity);

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.infoId = 0;
        }
        else
        {
            this.entity = (EntityVO) this.getIntent().getSerializableExtra("entity");
            this.infoId = getIntent().getIntExtra("infoID", 0);
            this.isMultiLocations = getIntent().getBooleanExtra("isMultiLocations", false);
        }

        this.isProfileLocked = this.entity.getPrivilege()==0;
        this.currentIndex = 0;

        getUIObjects();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnDelete = (Button)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);

        btnDelete.setVisibility(View.GONE);

        if (isMultiLocations == true)
            btnDelete.setText("Remove Location");
        else
            btnDelete.setText("Remove Entity");

        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        txtTitle = (TextView)findViewById(R.id.textViewTitle);

        btnEdit = (Button)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);
        btnChat = (ImageButton)findViewById(R.id.btnChat); btnChat.setOnClickListener(this);

        btnInviteContact = (ImageButton)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);

        headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
        btnEdit.setTextColor(getResources().getColor(R.color.top_title_text_color));
        btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color));

        txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
        btnChat.setImageResource(R.drawable.btnchatnav_white);
        btnInviteContact.setImageResource(R.drawable.entity_invite_white);

        initInfoItemFragment();
    }

    private void initInfoItemFragment()
    {
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;

        currentIndex = 0;
        if(entity.getEntityInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
            currentIndex = 0;
        }
        else{
            if (infoId == 0) {
                currentIndex = 0;
                entityInfo = entity.getEntityInfos().get(0);
            }
            else{
                for (int i = 0; i < entity.getEntityInfos().size();i ++){
                    if (entity.getEntityInfos().get(i).getId() == infoId){
                        entityInfo = entity.getEntityInfos().get(i);
                        break;
                    }
                    currentIndex++;
                }
            }
        }

        infoListFragment = EntityProfilePreviewFragment.newInstance(entity , entityInfo);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();
    }

    private void resumeInfoItemFragment() {
        EntityInfoVO entityInfo = null;
        if(entity.getEntityInfos() == null) return;
        currentIndex = 0;
        if(entity.getEntityInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getEntityInfos().add(entityInfo);
            currentIndex = 0;
        }
        else{
            if (infoId == 0)
            {
                currentIndex = 0;
                entityInfo = entity.getEntityInfos().get(0);
            }
            else{
                for (int i = 0; i < entity.getEntityInfos().size();i ++){
                    if (entity.getEntityInfos().get(i).getId() == infoId){
                        entityInfo = entity.getEntityInfos().get(i);
                        break;
                    }
                    currentIndex++;
                }
            }
        }

        infoListFragment = EntityProfilePreviewFragment.newInstance(entity , entityInfo);
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
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity" , this.entity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnDone:

                if (isMultiLocations == true){
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("entity", entity);
                    resultIntent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }else {

                    Intent backIntent = new Intent(EntityProfilePreviewAfterEditAcitivity.this, MenuActivity.class);
                    startActivity(backIntent);
                    finish();
                }
                break;

            case R.id.btnEdit:
                Bundle bundle = new Bundle();
                bundle.putSerializable("entity", entity);
                bundle.putBoolean("isMultiLocations", isMultiLocations);
                bundle.putBoolean("isNewEntity", true);
                bundle.putBoolean("isCreate", true);
                bundle.putInt("currentIndex", currentIndex);
                Intent intent = new Intent(EntityProfilePreviewAfterEditAcitivity.this,EntityEditActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, ENTITY_EDIT_NUM);
                //finish();
                break;

            case R.id.btnDelete:
                if (isMultiLocations == true){
                    AlertDialog.Builder builder = new AlertDialog.Builder(EntityProfilePreviewAfterEditAcitivity.this);
                    builder.setTitle("GINKO");
                    builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_delete_location));
                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            /*
                            EntityRequest.deleteInfo(entity.getId(),infoId, new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        //EntityProfilePreviewActivity.this.finish();
                                        EntityRequest.getEntity(entity.getId(), new ResponseCallBack<EntityVO>() {
                                            @Override
                                            public void onCompleted(JsonResponse<EntityVO> response) {
                                                if (response.isSuccess()){
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
                                                    bundle.putSerializable("entity" , entity);
                                                    resultIntent.putExtras(bundle);
                                                    setResult(Activity.RESULT_OK, resultIntent);
                                                    finish();
                                                }
                                            }
                                        });


                                    } else {

                                    }
                                }
                            });
                            */
                            EntityRequest.deleteEntity(entity.getId(), new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        Intent intent = new Intent(EntityProfilePreviewAfterEditAcitivity.this, MenuActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {

                                    }
                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EntityProfilePreviewAfterEditAcitivity.this);
                    builder.setTitle("GINKO");
                    builder.setMessage(getResources().getString(R.string.str_confirm_dialog_confirm_delete_entity));
                    builder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            EntityRequest.deleteEntity(entity.getId(), new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        EntityProfilePreviewAfterEditAcitivity.this.finish();
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
                }

                break;

            case R.id.btnInviteContact:
                Intent inviteContactIntent = new Intent(EntityProfilePreviewAfterEditAcitivity.this , EntityInviteContactActivity.class);
                inviteContactIntent.putExtra("entityId" , this.entity.getId());
                inviteContactIntent.putExtra("isCreate", true);
                startActivity(inviteContactIntent);
                break;

            case R.id.btnChat:
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("entity", this.entity);
                bundle1.putBoolean("isCreate", true);
                Intent entityMessageIntent = new Intent(EntityProfilePreviewAfterEditAcitivity.this , EntityMessageActivity.class);
                entityMessageIntent.putExtras(bundle1);
                startActivity(entityMessageIntent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ENTITY_EDIT_NUM:
                    entity = (EntityVO) data.getSerializableExtra("entity");
                    resumeInfoItemFragment();
                    break;
            }
        }
    }

    class MyLocationListener implements LocationListener {

        @Override
        public synchronized void onLocationChanged(Location location) {
            if (location != null) {
                System.out.println("----Location changed valueable----");
                lm.removeUpdates(locationListener);
                //if(progressDialog!=null && progressDialog.isShowing())
                //    progressDialog.dismiss();
                locationHandler.removeCallbacks(locationTimeoutRunnable);
                synchronized (lockObj) {
                    if (isGettingCurrentLocation) {
                        isGettingCurrentLocation = false;

                        //if(latestLocation != null)
                        //showCurrentLocationMarker(new LatLng(latestLocation.getLatitude() , latestLocation.getLongitude()));
                    }
                }
            }
            else
            {
                System.out.println("----Location changed null----");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("----Provider Disabled --"+provider);
        }
    }
}
