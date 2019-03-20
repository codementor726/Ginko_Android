package com.ginko.activity.sprout;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.SpoutRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.MySupportMapFragment;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.database.ContactTableModel;
import com.ginko.database.GinkoMeStruct;
import com.ginko.fragments.SproutExchangedFragment;
import com.ginko.fragments.SproutUnExchangedFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.map.CustomAlgo;
import com.ginko.map.CustomDefaultClusterRenderer;
import com.ginko.service.SproutService;
import com.ginko.view.ext.SelectableListAdapter;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserEntityProfileVO;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyGinkoMeActivity extends MyBaseFragmentActivity implements OnMapReadyCallback,View.OnClickListener
        ,SproutService.SproutServiceActionListener,
        SproutExchangedFragment.OnFragmentInteractionListener,
        SelectableListAdapter.ItemSelectedListener<SproutSearchItem>,
        ActionSheet.ActionSheetListener ,
        ClusterManager.OnClusterItemClickListener<SproutSearchItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<SproutSearchItem>
{
    /* UI Variables*/
    private RelativeLayout activityRootView;
    private RelativeLayout headerLayout;
    private RelativeLayout listContentLayout;
    private LinearLayout mapViewLayout;
    private ImageButton btnBack , btnClose , btnContactFilter , imgGPSOnOff;
    private LinearLayout contactNaviPointerLeftLayout , contactNaviPointerRightLayout;
    private ImageView imgTabMap ,imgTabList;
    private ImageView btnDeleteContact, btnEdit;
    private ImageButton btnTabContactsAroundTown , btnTabUnExchanged;
    private EditText edtSearch;
    private ImageView btnClearSearch;
    private Button btnCancelSearch;

    private TextView txtSproutOneHour , txtNoneDetectionNotification , txtHint;

    private GoogleMap mapView = null;
    private MapRenderer mRenderer;

    private MyViewPager listModeViewPager;

    private RelativeLayout mapitemListLayout;
    private ListView mapItemListView;

    /* Variables */
    private SproutSearchItem firstSproutItem = null;
    private ActionSheet deleteContactActionSheet = null;

    private boolean bMapMode = true; //map mode ? list mode
    private boolean isPageScrolling = false;

    private long sproutStartedTime = 0;//the time when the one hour auto off setting is on.
    private boolean isGpsOn;
    private boolean isEditable = false;
    private boolean isMarkerClicked = false;

    private Handler mHandler = new Handler();

    private MyPagerAdapter pageAdater = null;
    private int currIndex = 0;
    private SproutExchangedFragment sproutExchangedFragment = null;
    private SproutUnExchangedFragment sproutUnExchangedFragment = null;
    private boolean isKeyboardVisible = false;
    private boolean tmp_isKeyVisible = false;
    private String strSearchKeyword = "";

    private final int DETECTED_REMOVE_TYPE_PERMANENTLY = 1;
    private final int DETECTED_REMOVE_TYPE_FOR_24_HOURS = 2;

    private boolean isChangingSproutSetting = false;
    private String strExchangedNoneDetectionMessage = "" , strUnExchangedNoneDetectionMessage = "";

    private ArrayList<SproutSearchItem> detectedContactsAroundTown , detectedUnexchangedContacts;
    private List<Fragment> fragments = new ArrayList<Fragment>();

    private SproutService sproutService = null;

    /* Location Manager to get current location info */
    private LocationManager lm;
    private boolean isGettingCurrentLocation = false;
    private Location latestLocation = null;
    private Handler locationHandler;
    private LocationListener locationListener = new MyLocationListener();
    private Looper locationLooper;
    private Marker currentPosMarker = null, entityMarker = null;
    private LatLng currentLoc =  null;

    private MapItemListAdapter mapItemListAdapter;

    private DetectUserReceiver detectUserReceiver;
    private ReceiveExchangeRequest receiveExchangeRequest;
    private ReceiveEntityRemove receiveEntityRemove;
    private ExchangedContact exchangedContact;

    private UserEntityProfileVO entity;
    private UserEntityProfileVO entityOfContact;

    private boolean isDetectNewUser = false;
    private boolean isContactChange = false;
    private boolean isNewDetectUser = false;
    private boolean isLoad = false;
    private boolean isPurpleLoad = false;
    private boolean isUnexchangeLoad = false;

    private int m_tempZoom = 0;
    private IconGenerator mIconGenerator;
    private ImageView mImageView;
    private int mDimension;

    private BitmapDescriptor iconExchangedDefault , iconExchangedFocused , iconUnexchangedDefault , iconUnexchangedFocused;

    private ClusterManager<SproutSearchItem> mClusterManager;
    private ClusterManager<SproutSearchItem> mNormalManager;
    private MarkerManager markerManager;

    private boolean isClusteringStarted = true;
    private boolean isSproutNewDetection = false;
    private boolean isDetectedContacts = false;
    private boolean isPending_temp = false;

    private int m_orientHeight = 0;
    private int m_tmpId = 0;
    private boolean posted;

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

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Logger.debug("Bind to sprout service successfully.");
            sproutService = ((SproutService.LocalBinder) binder).getService();
            if(!sproutService.isLocationServiceEnabled())
                showLocationServiceSettingAlertDialog();

            sproutService.registerSproutActionListener(MyGinkoMeActivity.this);
            if (RuntimeContext.getUser() != null)
                isGpsOn = RuntimeContext.getUser().getLocationOn();
            sproutStartedTime = Uitils.getSproutStartedTime(MyApp.getContext());
            refreshGPSStatusUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sproutService = null;
            //瓦숅뇤??빳?먪ㅊ?ⓩ댎
        }
    };
    private void showLocationServiceSettingAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyGinkoMeActivity.this);
        builder.setMessage(getResources().getString(R.string.gps_network_not_enabled));
        builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private void binderLocationService() {
        Intent intent = new Intent(this, SproutService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbinderLocationService() {
        unbindService(serviceConnection);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ginko_me);

        this.strExchangedNoneDetectionMessage = getResources().getString(R.string.str_sprout_message_around_contact_no_dectection);
        this.strUnExchangedNoneDetectionMessage = getResources().getString(R.string.str_sprout_message_unexchanged_none_dectection);

        Intent intent = this.getIntent();
        isSproutNewDetection = intent.getBooleanExtra("isNewDetection", false);
        isDetectedContacts = intent.getBooleanExtra("isDetectedContacts", false);

        ///Add by wang for auto turn on.
        boolean turnOn = intent.getBooleanExtra("turnOn", false);
        if(turnOn)
        {
            turnOnOff(true);
        }
        //////////////////////////////
        if (RuntimeContext.getUser() != null) {
            isGpsOn = RuntimeContext.getUser().getLocationOn();
        } else {
            isGpsOn = true;
        }
        sproutStartedTime = Uitils.getSproutStartedTime(MyApp.getContext());

        this.binderLocationService();

        this.detectUserReceiver = new DetectUserReceiver();
        this.receiveExchangeRequest = new ReceiveExchangeRequest();
        this.receiveEntityRemove = new ReceiveEntityRemove();
        this.exchangedContact = new ExchangedContact();

        if (this.detectUserReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.DETECTED_GPS_CONTACT");
            this.registerReceiver(this.detectUserReceiver, msgReceiverIntent);
            isDetectNewUser = true;
        }
        if (this.receiveExchangeRequest != null) {
            IntentFilter receiveChangeContact = new IntentFilter();
            receiveChangeContact.addAction("android.intent.action.CONTACT_CHANGED");
            this.registerReceiver(this.receiveExchangeRequest, receiveChangeContact);
            isContactChange = true;
        }
        if (this.exchangedContact != null) {
            IntentFilter changed = new IntentFilter();
            changed.addAction("android.intent.action.EXCHANGE_REQUEST");
            this.registerReceiver(this.exchangedContact, changed);
            isContactChange = true;
        }
        if (this.receiveEntityRemove != null) {
            IntentFilter receiveChange = new IntentFilter();
            receiveChange.addAction("android.intent.action.ENTITY_REMOVED");
            this.registerReceiver(this.receiveEntityRemove, receiveChange);
            isContactChange = true;
        }
        getUIObjects();

        getCurrentLocation();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
        detectAllContact(null, false, true);
        detectUnExchangedContact(null, false, true);
    }
    @Override
    protected void getUIObjects() {
        super.getUIObjects();

        mIconGenerator = new IconGenerator(getApplicationContext());
        mImageView = new ImageView(getApplicationContext());
        mDimension = (int) getResources().getDimension(R.dimen.ginkome_custom_map_cluster_marker_iamgeview_size);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        mIconGenerator.setContentView(mImageView);

        mapitemListLayout = (RelativeLayout)findViewById(R.id.mapitemListLayout); mapitemListLayout.setVisibility(View.GONE);
        mapItemListView = (ListView)findViewById(R.id.mapItemListView);
        mapItemListAdapter = new MapItemListAdapter(this);
        mapItemListView.setAdapter(mapItemListAdapter);
        mapItemListView.setSelection(0);

        mapItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SproutSearchItem item = (SproutSearchItem) mapItemListAdapter.getItem(position);
                if (item == null) return;
                if (item.isExchanged) {
                    if (item.contactType == 1)//purple contact
                    {
                        if (item.nSharingStatus > 0 && item.nSharingStatus < 4)//1:home , 2:work , 3: both
                        {
                            final String strContactId = String.valueOf(item.contactOrEntityID);
                            final String strFullName = item.entityOrContactName;
                            final Intent purpleContactProfileIntent = new Intent(MyGinkoMeActivity.this, PurpleContactProfile.class);
                            final Bundle bundle = new Bundle();
                            bundle.putString("fullname", strFullName);
                            bundle.putString("contactID", strContactId);
                            UserRequest.getContactDetail(String.valueOf(strContactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                                @Override
                                public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                    if (response.isSuccess()) {
                                        hideKeyboard();
                                        PurpleContactWholeProfileVO responseData = response.getData();
                                        bundle.putSerializable("responseData", responseData);
                                        bundle.putString("StartActivity", "GinkoMe");
                                        purpleContactProfileIntent.putExtras(bundle);
                                        startActivityForResult(purpleContactProfileIntent, 3322);
                                    } else {
                                        if (response.getErrorCode() == 350)//The contact can't be found.
                                        {
                                            MyApp.getInstance().getContactsModel().deleteContactWithContactId(item.contactOrEntityID);
                                            MyApp.getInstance().removefromContacts(item.contactOrEntityID);
                                            sproutExchangedFragment.removeItem(item);
                                            detectedContactsAroundTown.remove(item);
                                            updateMarkers();
                                        } else {
                                        }
                                    }
                                }
                            });
                        } else if (item.nSharingStatus == 4) {
                            MyApp.getInstance().showSimpleAlertDiloag(MyGinkoMeActivity.this, "Oops! Contact would like to chat only", null);
                        } else//unexchanged contact
                        {
                            m_tmpId = item.contactOrEntityID;
                            hideKeyboard();
                            Intent shareLeafIntent = new Intent(MyGinkoMeActivity.this, ShareYourLeafActivity.class);
                            shareLeafIntent.putExtra("contactID", String.valueOf(item.contactOrEntityID));
                            shareLeafIntent.putExtra("contactFullname", item.entityOrContactName);
                            shareLeafIntent.putExtra("isUnexchangedContact", true);
                            shareLeafIntent.putExtra("isInviteContact", true);
                            shareLeafIntent.putExtra("isPendingRequest", item.isPending);
                            startActivityForResult(shareLeafIntent, 2233);
                        }
                    } else if (item.contactType == 2)//grey contact
                    {
                        String strContactId = String.valueOf(item.contactOrEntityID);
                        SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject jsonRes = response.getData();
                                    hideKeyboard();
                                    Intent greyContactProfileIntent = new Intent(MyGinkoMeActivity.this, GreyContactOne.class);
                                    greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                    startActivity(greyContactProfileIntent);
                                }
                            }
                        });
                    } else if (item.contactType == 3)//entity
                    {
                        final boolean isFollowed = item.isFollowed;
                        final int contactID = item.contactOrEntityID;
                        final SproutSearchItem finalItem = item;

                        EntityRequest.viewEntity(item.contactOrEntityID, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject jsonObj = response.getData();
                                    JSONObject jData = null;
                                    try {

                                        entity = JsonConverter.json2Object(
                                                (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                    } catch (JsonConvertException e) {
                                        e.printStackTrace();
                                        entity = null;
                                    }
                                    if (entity.getInfos().size() > 1) {
                                        hideKeyboard();
                                        Intent entityProfileIntent = new Intent(MyGinkoMeActivity.this, UserEntityMultiLocationsProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entity);
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        entityProfileIntent.putExtra("contactID", contactID);
                                        startActivityForResult(entityProfileIntent, 980);
                                    } else {
                                        hideKeyboard();
                                        Intent entityProfileIntent = new Intent(MyGinkoMeActivity.this, UserEntityProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entity);
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        entityProfileIntent.putExtra("contactID", contactID);
                                        startActivityForResult(entityProfileIntent, 980);
                                    }
                                } else {
                                    if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                        MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactID);
                                        MyApp.getInstance().removefromContacts(contactID);

                                        MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contactID);
                                        updateMarkers();
                                        sproutExchangedFragment.removeItem(finalItem);
                                    }
                                }
                            }
                        }, true);
                    }
                } else {
                    if (item.contactType == 3)//entity
                    {
                        final boolean isFollowed = item.isFollowed;
                        final int contactID = item.contactOrEntityID;

                        EntityRequest.viewEntity(item.contactOrEntityID, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject jsonObj = response.getData();
                                    JSONObject jData = null;
                                    try {

                                        entityOfContact = JsonConverter.json2Object(
                                                (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                    } catch (JsonConvertException e) {
                                        e.printStackTrace();
                                        entity = null;
                                    }
                                    if (entityOfContact.getInfos().size() > 1) {
                                        hideKeyboard();
                                        Intent entityProfileIntent = new Intent(MyGinkoMeActivity.this, UserEntityMultiLocationsProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entityOfContact);
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        entityProfileIntent.putExtra("contactID", contactID);
                                        entityProfileIntent.putExtra("isNoLetter", jsonObj.optInt("privilege", 0));
                                        startActivityForResult(entityProfileIntent, 980);
                                    } else {
                                        hideKeyboard();
                                        Intent entityProfileIntent = new Intent(MyGinkoMeActivity.this, UserEntityProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entityOfContact);
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        entityProfileIntent.putExtra("contactID", contactID);
                                        startActivityForResult(entityProfileIntent, 980);
                                    }
                                } else {
                                    if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                        MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactID);
                                        MyApp.getInstance().removefromContacts(contactID);

                                        MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contactID);
                                        updateMarkers();
                                        sproutUnExchangedFragment.removeItem(item);
                                    }
                                }
                            }
                        }, true);
                        //mapitemListLayout.setVisibility(View.GONE);
                    } else {
                        m_tmpId = item.contactOrEntityID;
                        hideKeyboard();
                        Intent shareLeafIntent = new Intent(MyGinkoMeActivity.this, ShareYourLeafActivity.class);
                        shareLeafIntent.putExtra("contactID", String.valueOf(item.contactOrEntityID));
                        shareLeafIntent.putExtra("contactFullname", item.entityOrContactName);
                        shareLeafIntent.putExtra("isUnexchangedContact", true);
                        shareLeafIntent.putExtra("isPendingRequest", item.isPending);
                        shareLeafIntent.putExtra("lat", item.lat);
                        shareLeafIntent.putExtra("long", item.lng);
                        shareLeafIntent.putExtra("address", item.strAddress);
                        if (item.contactType == 1)
                            shareLeafIntent.putExtra("isInviteContact", true);
                        startActivityForResult(shareLeafIntent, 2233);
                    }
                }


            }
        });

        headerLayout = (RelativeLayout)findViewById(R.id.headerLayout);
        listContentLayout = (RelativeLayout)findViewById(R.id.listContentLayout);
        mapViewLayout = (LinearLayout)findViewById(R.id.mapViewLayout);

        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        btnClose = (ImageButton)findViewById(R.id.btnClose);btnClose.setOnClickListener(this);
        btnEdit = (ImageView)findViewById(R.id.btnEdit);btnEdit.setOnClickListener(this);
        btnContactFilter = (ImageButton)findViewById(R.id.btnContactFilter); btnContactFilter.setOnClickListener(this);
        imgGPSOnOff = (ImageButton)findViewById(R.id.imgGPSOnOff); imgGPSOnOff.setOnClickListener(this);

        btnDeleteContact = (ImageView)findViewById(R.id.btnDeleteContact);btnDeleteContact.setOnClickListener(this);

        btnTabContactsAroundTown = (ImageButton)findViewById(R.id.btnTabContactsAroundTown); btnTabContactsAroundTown.setOnClickListener(this);
        btnTabUnExchanged = (ImageButton)findViewById(R.id.btnTabUnExchanged); btnTabUnExchanged.setOnClickListener(this);


        imgTabMap = (ImageView)findViewById(R.id.imgTabMap); imgTabMap.setOnClickListener(this);
        imgTabList = (ImageView)findViewById(R.id.imgTabList); imgTabList.setOnClickListener(this);

        txtSproutOneHour = (TextView)findViewById(R.id.txtSproutOneHour);
        edtSearch = (EditText)findViewById(R.id.edtSearch);
        edtSearch.setFocusable(true);

        txtNoneDetectionNotification = (TextView)findViewById(R.id.txtNoneDetectionNotification); txtNoneDetectionNotification.setVisibility(View.INVISIBLE);
        txtHint = (TextView)findViewById(R.id.txtHint);

        activityRootView = (RelativeLayout)findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();

                isShownKeyboard();
                if (isKeyboardVisible) {
                    edtSearch.setCursorVisible(true);
                    btnCancelSearch.setVisibility(View.VISIBLE);
                    listModeViewPager.setScanScroll(false);
                } else
                {

                    edtSearch.setCursorVisible(false);
                    btnCancelSearch.setVisibility(View.GONE);
                    if (!isEditable)
                        listModeViewPager.setScanScroll(true);
                    else {
                        if (isPageScrolling == false)
                            listModeViewPager.setScanScroll(false);
                    }
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnClearSearch.setVisibility(View.GONE);
            }
        });


        btnCancelSearch = (Button)findViewById(R.id.btnCancelSearch); //btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnClearSearch.setVisibility(View.GONE);
                hideKeyboard();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence s, int start, int count,
                                               int after) {
                     // TODO Auto-generated method stub
                 }

                 @Override
                 public void onTextChanged(CharSequence s, int start, int before,
                                           int count) {
                     // TODO Auto-generated method stub
                     if (s.length() > 0)
                         btnClearSearch.setVisibility(View.VISIBLE);
                     else
                         btnClearSearch.setVisibility(View.GONE);
                 }

                 @Override
                 public void afterTextChanged(Editable s) {
                     // TODO Auto-generated method stub
                     searchItems();
                 }
             }
        );
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

                    if (edtSearch.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    //searchItems();
                    return true;
                }
                return false;
            }
        });

        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (edtSearch.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);

                    btnCancelSearch.setVisibility(View.VISIBLE);
                    //showKeyboard();
                    listModeViewPager.setScanScroll(false);
                } else {
                    btnCancelSearch.setVisibility(View.GONE);
                    btnClearSearch.setVisibility(View.GONE);
                    if (!isEditable)
                        listModeViewPager.setScanScroll(true);
                    else {
                        if (!isPageScrolling)
                            listModeViewPager.setScanScroll(false);
                    }
                }
            }
        });

        if(currIndex == 0)
        {
            txtHint.setText(getResources().getString(R.string.str_contacts_around_town));
        }
        else
        {
            txtHint.setText(getResources().getString(R.string.str_exchange_info_with_these_contacts));
        }

        SupportMapFragment mapFragment = ((MySupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        sproutExchangedFragment = SproutExchangedFragment.newInstance(this);
        sproutUnExchangedFragment = SproutUnExchangedFragment.newInstance(this);

        detectedContactsAroundTown = new ArrayList<SproutSearchItem>();
        detectedUnexchangedContacts = new ArrayList<SproutSearchItem>();

        bMapMode = !isSproutNewDetection;//map mode ? list mode
        updateTabMode();
        updateEditableUI();

        contactNaviPointerLeftLayout = (LinearLayout) findViewById(R.id.flag_left);
        contactNaviPointerRightLayout = (LinearLayout)findViewById(R.id.flag_right);

        initViewPager();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;

        mapView.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapitemListLayout.setVisibility(View.GONE);
            }
        });

        /*
        mapView.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final SproutSearchItem item = (SproutSearchItem) marker.getTag();
                isMarkerClicked = true;

                if (item == null) return false;

                if (item.contactType == 3) {
                    EntityRequest.getFollowerTotal(item.contactOrEntityID, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                JSONObject json = response.getData();
                                item.isFollowed = json.optBoolean("is_followed", false);
                                item.nEnityFollowerCount = json.optInt("follower_total", 0);
                                ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                                items.add(item);
                                mapitemListLayout.setVisibility(View.VISIBLE);
                                mapItemListAdapter.setListItems(items);
                                mapItemListAdapter.notifyDataSetChanged();
                            }
                        }
                    }, true);

                } else {
                    ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                    items.add(item);
                    mapitemListLayout.setVisibility(View.VISIBLE);
                    mapItemListAdapter.setListItems(items);
                    mapItemListAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        */
        markerManager = new MarkerManager(mapView);
        mClusterManager = new ClusterManager<SproutSearchItem>(MyGinkoMeActivity.this, mapView, markerManager);
        mNormalManager = new ClusterManager<SproutSearchItem>(MyGinkoMeActivity.this, mapView, markerManager);


        mapView.setOnMarkerClickListener(markerManager);
        mRenderer = new MapRenderer(MyGinkoMeActivity.this, mapView, mClusterManager);
        mClusterManager.setRenderer(new MapRenderer(MyGinkoMeActivity.this, googleMap, mClusterManager));
        mNormalManager.setRenderer(new MapRenderer(MyGinkoMeActivity.this, googleMap, mNormalManager));

        mClusterManager.setAlgorithm(new CustomAlgo<SproutSearchItem>());
        mNormalManager.setAlgorithm(new CustomAlgo<SproutSearchItem>());

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<SproutSearchItem>() {
            @Override
            public boolean onClusterItemClick(final SproutSearchItem item) {
                isMarkerClicked = true;

                final int contactId = item.contactOrEntityID;
                EntityRequest.getFollowerTotal(item.contactOrEntityID, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject json = response.getData();
                            item.isFollowed = json.optBoolean("is_followed", false);
                            item.nEnityFollowerCount = json.optInt("follower_total", 0);
                            ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                            items.add(item);
                            mapitemListLayout.setVisibility(View.VISIBLE);
                            mapItemListAdapter.setListItems(items);
                            mapItemListAdapter.notifyDataSetChanged();
                        } else {
                            if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                MyApp.getInstance().showSimpleAlertDiloag(MyGinkoMeActivity.this, "The entity Can't be found.", null);
                                MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactId);
                                MyApp.getInstance().removefromContacts(contactId);

                                MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contactId);
                                updateMarkers();
                                sproutUnExchangedFragment.removeItem(item);
                            } else
                            {
                                ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                                items.add(item);
                                mapitemListLayout.setVisibility(View.VISIBLE);
                                mapItemListAdapter.setListItems(items);
                                mapItemListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, false, false);

                return false;
            }
        });

        mNormalManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<SproutSearchItem>() {
            @Override
            public boolean onClusterItemClick(final SproutSearchItem item) {
                isMarkerClicked = true;

                int contactId = item.contactOrEntityID;
                ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                items.add(item);
                mapitemListLayout.setVisibility(View.VISIBLE);
                mapItemListAdapter.setListItems(items);
                mapItemListAdapter.notifyDataSetChanged();

                return false;
            }
        });

        mapView.setInfoWindowAdapter(markerManager);

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<SproutSearchItem>() {

            @Override
            public void onClusterItemInfoWindowClick(SproutSearchItem item) {

            }
        });

        mNormalManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<SproutSearchItem>() {

            @Override
            public void onClusterItemInfoWindowClick(SproutSearchItem item) {

            }
        });

        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());
        mNormalManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());

        /*
        mapView.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                // Empty info window.
                return new View(getApplicationContext());
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        */

        mapView.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override

            public void onCameraChange(CameraPosition cameraPosition) {
                if (MyApp.getInstance().mMapDoubleTouched == true)
                {
                    mapitemListLayout.setVisibility(View.GONE);
                    MyApp.getInstance().mMapDoubleTouched = false;
                }

                if (!isMarkerClicked)
                    updateMarkers();
                else
                    isMarkerClicked = !isMarkerClicked;
            }
        });
        createMarkers();
        updateMarkers();
    }

    private UpdateTask task;
    private void updateMarkers() {
        if (isFinishing())
            return;
        if (isPurpleLoad == true || isUnexchangeLoad == true)
            return;
        if (task!=null)
            task.cancel(true);
        task = new UpdateTask();
        task.execute();
    }

    @Override
    public boolean onClusterItemClick(final SproutSearchItem item) {

        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(SproutSearchItem item) {

    }

    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        MyCustomAdapterForItems() {
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub

            return new View(getApplicationContext());
        }
    }

    public class Wrapper
    {
        public List<SproutSearchItem> groupMarkers;
        public List<SproutSearchItem> nonMarkers;
    }

    private class UpdateTask extends AsyncTask<Void,Void,Wrapper>{

        private LatLngBounds visible;
        private boolean isVisibleValid;
        private float zoom;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            visible = mapView.getProjection().getVisibleRegion().latLngBounds;
            zoom = mapView.getCameraPosition().zoom;
            isVisibleValid = visible.northeast.latitude!=0||visible.northeast.longitude!=0||visible.southwest.longitude!=0||visible.southwest.latitude!=0;
        }

        @Override
        protected Wrapper doInBackground(Void... params) {
            List<SproutSearchItem> clusterMarkers = new ArrayList<>();
            List<SproutSearchItem> nonMarkers = new ArrayList<>();
            for (SproutSearchItem marker:detectedContactsAroundTown)
            {
                if (!isVisibleValid||visible.contains(marker.getPosition())) {
                    marker.setZoom(zoom);
                    nonMarkers.add(marker);
                }
            }
            for (SproutSearchItem marker:detectedUnexchangedContacts)
            {
                if (!isVisibleValid||visible.contains(marker.getPosition())) {
                    marker.setZoom(zoom);
                    nonMarkers.add(marker);
                }
            }

            for (SproutSearchItem marker:groupMarkers){

                if (!isVisibleValid||visible.contains(marker.getPosition())) {
                    marker.setZoom(zoom);
                    clusterMarkers.add(marker);
                }
            }

            Wrapper w = new Wrapper();
            w.groupMarkers = clusterMarkers;
            w.nonMarkers = nonMarkers;

            return w;
        }

        @Override
        protected void onPostExecute(Wrapper w) {
            super.onPostExecute(w);
            if (this==task) {
                mClusterManager.clearItems();
                mClusterManager.addItems(w.groupMarkers);
                mClusterManager.cluster();

                mNormalManager.clearItems();
                mNormalManager.addItems(w.nonMarkers);
                mNormalManager.cluster();
                task = null;
            }

        }
    }

    List<SproutSearchItem> groupMarkers = new ArrayList<>();
    List<SproutSearchItem> sepMarkers = new ArrayList<>();

    private void createMarkers() {
        List<GinkoMeStruct> ginkoStructs = null;
        ginkoStructs = MyApp.getInstance().getGinkoModel().getAll();

        for (SproutSearchItem marker:detectedContactsAroundTown)
            sepMarkers.add(marker);
        for (SproutSearchItem marker:detectedUnexchangedContacts)
            sepMarkers.add(marker);

        if (ginkoStructs != null) {
            for (GinkoMeStruct p : ginkoStructs) {
                SproutSearchItem newItem = new SproutSearchItem();
                newItem.contactType = 3;
                newItem.contactOrEntityID = p.getContactOrEntityID();
                newItem.entityOrContactName = p.getEntityOrContactName();
                newItem.profile_image = p.getProfileImage();
                newItem.lat = p.getLat();
                newItem.lng = p.getLng();
                groupMarkers.add(newItem);
            }
        }
    }

    private void recreateMarker() {
        List<GinkoMeStruct> ginkoStructs = null;
        ginkoStructs = MyApp.getInstance().getGinkoModel().getAll();
        groupMarkers.clear();
        if (ginkoStructs != null) {
            for (GinkoMeStruct p : ginkoStructs) {
                SproutSearchItem newItem = new SproutSearchItem();
                newItem.contactType = 3;
                newItem.contactOrEntityID = p.getContactOrEntityID();
                newItem.entityOrContactName = p.getEntityOrContactName();
                newItem.profile_image = p.getProfileImage();
                newItem.lat = p.getLat();
                newItem.lng = p.getLng();
                groupMarkers.add(newItem);
            }
        }
    }

    private void loadMarkerIcon(SproutSearchItem item, float zoom) {
        float m_tempZoom = zoom;

        if (m_tempZoom < 5.0)
        {
            if (item.isExchanged) {
                if (item.isFocused) {
                    mImageView.setImageResource(R.drawable.ginkome_map_marker_purple_selected);
                } else {
                    mImageView.setImageResource(R.drawable.ginkome_map_marker_purple);
                }
            } else {
                if (item.isFocused) {
                    mImageView.setImageResource(R.drawable.ginkome_map_marker_grey_selected);
                } else {
                    mImageView.setImageResource(R.drawable.ginkome_map_marker_grey);
                }
            }
        } else {
            if (item.isExchanged) {
                if (item.isFocused) {
                    mImageView.setImageResource(R.drawable.purple_photo);
                    //mImageView.setImageUrl("", imgLoader);
                } else {
                    mImageView.setImageResource(R.drawable.purple_photo);
                    //mImageView.setImageUrl("", imgLoader);
                }
                if(item.profile_image == null || "".equals(item.profile_image))
                    mImageView.setImageResource(R.drawable.purple_photo);
                else
                    Picasso.with(MyGinkoMeActivity.this).load(item.profile_image).transform(new CircleTransform()).into(mImageView);
            } else {
                if (item.isFocused) {
                    mImageView.setImageResource(R.drawable.ginkome_map_marker_grey_selected);
                } else {
                    mImageView.setImageResource(R.drawable.ginkome_map_marker_grey);
                }
            }
        }

        int m_dpBonus = (int)m_tempZoom * 5 - 20;
        mImageView.setLayoutParams(new ViewGroup.LayoutParams((int) (mDimension / 2) + m_dpBonus, (int) (mDimension / 2) + m_dpBonus));
        mIconGenerator.setContentView(mImageView);
        Bitmap icon = mIconGenerator.makeIcon();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(item.getPosition());

        Marker newMarker = mapView.addMarker(markerOptions);
        newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        newMarker.setTag(item);
    }


    private void showKeyboard()
    {
        isKeyboardVisible = true;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    void hideKeyboard()
    {
        //if keyboard is shown, then hide it
        isKeyboardVisible = false;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
    }

    /*
    private void showKeyboard() {

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtSearch, InputMethodManager.SHOW_FORCED);
    }
    */

    private void moveMapToPositon(double x, double y , float zoom)
    {
        LatLng loc = new LatLng(x , y);
        CameraPosition cp = new CameraPosition.Builder().target(loc).zoom(zoom).build();
        mapView.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    private void showCurrentLocationMarker(LatLng latlng)
    {
        final Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_marker_current);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);

        currentLoc = latlng;

        //mapView.clear();

        mapView.animateCamera(CameraUpdateFactory.newLatLng(latlng));

        currentPosMarker = mapView.addMarker(markerOptions);
        currentPosMarker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
        currentPosMarker.setAnchor(0.5f, 1.0f);
        //moveMapToPositon(latlng.latitude, latlng.longitude, mapView.getCameraPosition().zoom);
        if(!isNewDetectUser) {
            moveMapToPositon(latlng.latitude, latlng.longitude, 13);
        }
    }

    private void addCurrentMarker(LatLng latlng)
    {
        final Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_marker_current);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);

        currentLoc = latlng;

        //mapView.clear();

        currentPosMarker = mapView.addMarker(markerOptions);
        currentPosMarker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
    }

    private void getCurrentLocation()
    {
        if (lm == null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (isGettingCurrentLocation)
            return;

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            if(latestLocation != null)
            {
                showCurrentLocationMarker(new LatLng(latestLocation.getLatitude() , latestLocation.getLongitude()));
            }
            else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_LOW);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                isGettingCurrentLocation = true;
                if(locationLooper == null) {
                    locationLooper = Looper.myLooper();
                }

                //GAD-1454
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null)
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (location != null) {
                    updateLocation(location);
                    return;
                }

                lm.requestSingleUpdate(criteria, locationListener, locationLooper);

                if(locationHandler == null)
                    locationHandler = new Handler(locationLooper);
                locationHandler.postDelayed(locationTimeoutRunnable, GET_CURRENT_LOCATION_TIME_OUT);
                //progressDialog.show();
            }

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyGinkoMeActivity.this);
            builder.setMessage(getResources().getString(R.string.gps_network_not_enabled));
            builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int paramInt) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private void updateLocation(Location location) {
        if (location == null) {
            return;
        }

        latestLocation = location;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                synchronized (lockObj) {
                    if (isGettingCurrentLocation) {
                        isGettingCurrentLocation = false;

                        if (latestLocation != null)
                            showCurrentLocationMarker(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
                    }
                }
            }
        }, 1500);

        SpoutRequest.updateLocation(location.getLatitude(), location.getLongitude(), new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    Logger.debug("Upload location successfully!");
                    detectUnExchangedContact(null, false, true);
                    detectAllContact(null, false, true);
                }
            }
        });
    }

    private boolean getSelectableFromExchanged(int contactId)
    {
        boolean isSelected = false;
        for (int i = 0; i < sproutExchangedFragment.getItemCount(); i++)
        {
            SproutSearchItem item = sproutExchangedFragment.getItem(i);
            if (item.contactOrEntityID == contactId)
                isSelected = item.isSelected();
        }

        return isSelected;
    }

    private boolean getSelectableFromUnExchanged(int contactId)
    {
        boolean isSelected = false;
        for (int i = 0; i < sproutUnExchangedFragment.getItemCount(); i++)
        {
            SproutSearchItem item = sproutUnExchangedFragment.getItem(i);
            if (item.contactOrEntityID == contactId)
                isSelected = item.isSelected();
        }

        return isSelected;
    }

    private void detectUnExchangedContact(final String searchString, boolean bProgressShow, final boolean bShowMap) {
        isUnexchangeLoad = true;

        SpoutRequest.detectUnExchangedContacts(searchString, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject data = response.getData();
                    ArrayList<SproutSearchItem> newLists = new ArrayList<SproutSearchItem>();
                    try {
                        JSONArray dataArray = data.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            SproutSearchItem sproutSearchItem = new SproutSearchItem(obj);
                            boolean isSelected = getSelectableFromUnExchanged(sproutSearchItem.contactOrEntityID);
                            sproutSearchItem.setSelected(isSelected);
                            newLists.add(sproutSearchItem);

                            if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                                for (int k = 0; k < mapItemListAdapter.getCount(); k++) {
                                    SproutSearchItem thisItem = (SproutSearchItem) mapItemListAdapter.getItem(k);
                                    if (thisItem.contactOrEntityID == sproutSearchItem.contactOrEntityID) {
                                        mapItemListAdapter.setItem(k, sproutSearchItem);
                                    }
                                }
                            }
                        }

                        if (detectedUnexchangedContacts == null)
                            detectedUnexchangedContacts = new ArrayList<SproutSearchItem>();
                        else
                            detectedUnexchangedContacts.clear();

                        detectedUnexchangedContacts.addAll(newLists);

                        if (detectedUnexchangedContacts.size() > 0 || detectedContactsAroundTown.size() > 0)
                            isDetectedContacts = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    isUnexchangeLoad = false;
                    updateMarkers();

                    sproutUnExchangedFragment.clearAll();
                    sproutUnExchangedFragment.setListItems(detectedUnexchangedContacts);

                    String strEditText = edtSearch.getText().toString().trim();
                    if (!strEditText.equals(""))
                        sproutUnExchangedFragment.searchItems(edtSearch.getText().toString().toLowerCase());
                    else
                        sproutUnExchangedFragment.notifyChanged();

                    if (mapItemListAdapter.getCount() == 0)
                        mapitemListLayout.setVisibility(View.GONE);
                    mapItemListAdapter.notifyDataSetChanged();
                    checkSearchContacts();

                }
            }
        }, bProgressShow);
    }

    private void detectAllContact(final String searchString, boolean bProgressShow, final boolean bShowMap) {
        boolean bProgress = bProgressShow;
        isPurpleLoad = true;

        SpoutRequest.listAllContacts(searchString, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject data = response.getData();
                    ContactTableModel contactTableModel = MyApp.getInstance().getContactsModel();

                    if (contactTableModel == null) return;

                    boolean isViewChanged = false;

                    ArrayList<SproutSearchItem> newLists = new ArrayList<SproutSearchItem>();

                    try {
                        JSONArray dataArray = data.getJSONArray("data");
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            int contactId = obj.getInt("contact_id");
                            int contactType = obj.getInt("contact_type");
                            double lat = obj.getDouble("latitude");
                            double lng = obj.getDouble("longitude");
                            SproutSearchItem sproutSearchItem = new SproutSearchItem();
                            ContactStruct struct = contactTableModel.getContactById(contactId);
                            if (struct == null)
                                continue;
                            ContactItem contactItem = struct.getContactItem();
                            if (contactItem == null) {
                                continue;
                            }
                            if (contactType == 3)
                                continue;
                            sproutSearchItem.setVisible(true);
                            sproutSearchItem.isExchanged = true;
                            sproutSearchItem.contactType = contactType;
                            sproutSearchItem.contactOrEntityID = contactId;
                            sproutSearchItem.lat = lat;
                            sproutSearchItem.lng = lng;
                            sproutSearchItem.isPending = false;
                            sproutSearchItem.profile_image = contactItem.getProfileImage();
                            sproutSearchItem.nSharingStatus = contactItem.getSharingStatus();
                            sproutSearchItem.entityOrContactName = contactItem.getFullName();
                            boolean isSelected = getSelectableFromExchanged(contactId);
                            sproutSearchItem.setSelected(isSelected);
                            sproutSearchItem.isPending = false;

                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            sproutSearchItem.foundTime = format.format(Calendar.getInstance().getTime());
                            try {
                                sproutSearchItem.jsonObject = new JSONObject(struct.getJsonValue());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            newLists.add(sproutSearchItem);

                            if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                                for(int k = 0; k <mapItemListAdapter.getCount(); k++) {
                                    SproutSearchItem thisItem = (SproutSearchItem) mapItemListAdapter.getItem(k);
                                    if(thisItem.contactOrEntityID == contactId) {
                                        mapItemListAdapter.setItem(k, sproutSearchItem);
                                    }
                                }
                            }
                        }

                        if (detectedContactsAroundTown == null)
                            detectedContactsAroundTown = new ArrayList<SproutSearchItem>();
                        else
                            detectedContactsAroundTown.clear();
                        sproutExchangedFragment.clearAll();

                        detectedContactsAroundTown.addAll(newLists);
                        sproutExchangedFragment.setListItems(newLists);

                        if (detectedUnexchangedContacts.size() > 0 || detectedContactsAroundTown.size() > 0)
                            isDetectedContacts = true;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    isPurpleLoad = false;
                    updateMarkers();

                    String strEditText = edtSearch.getText().toString().trim();
                    if (!strEditText.equals(""))
                        sproutExchangedFragment.searchItems(edtSearch.getText().toString().toLowerCase());
                    else
                        sproutExchangedFragment.notifyChanged();

                    if (mapItemListAdapter.getCount() == 0)
                        mapitemListLayout.setVisibility(View.GONE);
                    mapItemListAdapter.notifyDataSetChanged();

                    //checkHasDetectedContacts();
                    checkSearchContacts();
                } else {
                    Log.w("panda", "errMsg----" + response.getErrorMessage());
                }
            }
        }, bProgressShow);
    }

    private void delayFunc() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.detectUserReceiver != null && isDetectNewUser == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.DETECTED_GPS_CONTACT");
            this.registerReceiver(this.detectUserReceiver, msgReceiverIntent);
        }
        if (this.receiveExchangeRequest != null && isContactChange == false) {
            IntentFilter receiveChangeContact = new IntentFilter();
            receiveChangeContact.addAction("android.intent.action.CONTACT_CHANGED");
            this.registerReceiver(this.receiveExchangeRequest, receiveChangeContact);
        }
        if (this.receiveEntityRemove != null && isContactChange == false) {
            IntentFilter receiveChange = new IntentFilter();
            receiveChange.addAction("android.intent.action.ENTITY_REMOVED");
            this.registerReceiver(this.receiveEntityRemove, receiveChange);
        }
        if (this.exchangedContact != null) {
            IntentFilter changed = new IntentFilter();
            changed.addAction("android.intent.action.EXCHANGE_REQUEST");
            this.registerReceiver(this.exchangedContact, changed);
            isContactChange = true;
        }

        detectUnExchangedContact(null, false, true);
        detectAllContact(null, false, true);

        if (tmp_isKeyVisible)
        {
            showKeyboard();
        }
    }

    /*
    public void hideKeyboard()
    {
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);
    }
    */

    private void isShownKeyboard() {
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int curheight= rectgle.bottom;
        if(m_orientHeight == curheight && m_orientHeight != 0)
            isKeyboardVisible = false;
        else
            isKeyboardVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.detectUserReceiver != null && isDetectNewUser == true) {
            this.unregisterReceiver(this.detectUserReceiver);
            isDetectNewUser = false;
        }
        if (this.receiveExchangeRequest != null && isContactChange == true) {
            this.unregisterReceiver(this.receiveExchangeRequest);
            isContactChange = false;
        }
        if (this.receiveEntityRemove != null && isContactChange == true) {
            this.unregisterReceiver(this.receiveEntityRemove);
            isContactChange = false;
        }
        if (this.exchangedContact != null && isContactChange == true) {
            this.unregisterReceiver(this.exchangedContact);
            isContactChange = false;
        }

        if (edtSearch.isCursorVisible())
            tmp_isKeyVisible = true;
        else {
            tmp_isKeyVisible = false;
        }
    }

    @Override
    protected void onDestroy() {
        this.fragments.clear();
        hideKeyboard();
        unbinderLocationService();
        this.unregisterReceiver(this.receiveEntityRemove);
        super.onDestroy();

        if (task!=null)
            task.cancel(true);
        task = null;
    }

    private void initViewPager() {
        currIndex = 1;//default show exchange info with these contacts
        listModeViewPager = (MyViewPager) findViewById(R.id.vPager);
        pageAdater = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        fragments.add(sproutExchangedFragment);
        fragments.add(sproutUnExchangedFragment);
        listModeViewPager.setAdapter(pageAdater);
        listModeViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        listModeViewPager.setCurrentItem(currIndex);
        listModeViewPager.setScanScroll(true);

        updateBottomNaviPointer();
    }

    private void updateBottomNaviPointer()
    {
        if(currIndex == 0)
        {
            contactNaviPointerLeftLayout.setVisibility(View.VISIBLE);
            contactNaviPointerRightLayout.setVisibility(View.INVISIBLE);
        }
        else {
            contactNaviPointerLeftLayout.setVisibility(View.INVISIBLE);
            contactNaviPointerRightLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateTabMode() {
        if(bMapMode)
        {
            headerLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mapViewLayout.setVisibility(View.VISIBLE);
            listContentLayout.setVisibility(View.GONE);
            imgTabMap.setImageResource(R.drawable.sprout_left_tab_selected);
            imgTabList.setImageResource(R.drawable.sprout_right_tab_unselected);
        }
        else
        {
            headerLayout.setBackgroundColor(getResources().getColor(R.color.top_title_text_color));
            mapViewLayout.setVisibility(View.GONE);
            listContentLayout.setVisibility(View.VISIBLE);
            imgTabMap.setImageResource(R.drawable.sprout_left_tab_unselected);
            imgTabList.setImageResource(R.drawable.sprout_right_tab_selected);
        }
    }

    private void updateEditableUI()
    {
        if(isEditable)
        {
            btnBack.setVisibility(View.GONE);
            imgGPSOnOff.setVisibility(View.GONE);
            txtSproutOneHour.setVisibility(View.GONE);
            btnClose.setVisibility(View.VISIBLE);
            btnDeleteContact.setVisibility(View.VISIBLE);
            btnContactFilter.setVisibility(View.GONE);
        }
        else
        {
            btnBack.setVisibility(View.VISIBLE);
            imgGPSOnOff.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.GONE);
            btnDeleteContact.setVisibility(View.INVISIBLE);
            btnContactFilter.setVisibility(View.VISIBLE);
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (sproutStartedTime == 0 || currentTime - sproutStartedTime > SproutService.AUTO_TURN_OFF_TIME_LIMIT)//if sprout is not set to turn off after one hour automatically or
            //or the sprout time is exceed one hour , then hide one hour number textview
            {
                txtSproutOneHour.setVisibility(View.INVISIBLE);
            } else {
                txtSproutOneHour.setVisibility(View.VISIBLE);
            }
            if(isKeyboardVisible)
                hideKeyboard();
        }
    }

    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();
        if(!strEditText.equals("")) {
            strSearchKeyword = strEditText.toLowerCase();
            if(currIndex == 0)
            {
                sproutExchangedFragment.searchItems(edtSearch.getText().toString().toLowerCase());
            }
            else
            {
                sproutUnExchangedFragment.searchItems(edtSearch.getText().toString().toLowerCase());
            }
        }
        else
        {
            strSearchKeyword = "";
            if(currIndex == 0)
            {
                sproutExchangedFragment.searchItems("");
            }
            else
            {
                sproutUnExchangedFragment.searchItems("");
            }
        }

        sproutExchangedFragment.getItemCount();
        checkSearchContacts();
    }

    private void refreshGPSStatusUI() {
        if (isGpsOn) {
            imgGPSOnOff.setImageResource(R.drawable.gpson);
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (sproutStartedTime == 0 || currentTime - sproutStartedTime > SproutService.AUTO_TURN_OFF_TIME_LIMIT)//if sprout is not set to turn off after one hour automatically or
            //or the sprout time is exceed one hour , then hide one hour number textview
            {
                txtSproutOneHour.setVisibility(View.INVISIBLE);
            } else {
                txtSproutOneHour.setVisibility(View.VISIBLE);
            }
        } else {
            imgGPSOnOff.setImageResource(R.drawable.gpsoff);
            txtSproutOneHour.setVisibility(View.INVISIBLE);
        }
    }

    //turn on/off sprout settings
    private void turnOnOff(final boolean on) {
        isChangingSproutSetting = true;
        SpoutRequest.switchLocationStatus(on, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    if (on) {
                        MyGinkoMeActivity.this.isGpsOn = true;
                        if (RuntimeContext.getUser() != null)
                            RuntimeContext.getUser().setLocationOn(true);
                        sproutService.startSproutService();
                        if (!isDetectedContacts) {
                            getCurrentLocation();
                            detectUnExchangedContact(null, false, true);
                            detectAllContact(null, false, true);
                        }
                    } else {
                        MyGinkoMeActivity.this.isGpsOn = false;
                        if (RuntimeContext.getUser() != null)
                            RuntimeContext.getUser().setLocationOn(false);
                        sproutStartedTime = 0;
                        Uitils.storeSproutStartTime(MyApp.getContext(), sproutStartedTime);
                        sproutService.stopSproutService();
                    }
                }
                isChangingSproutSetting = false;
                refreshGPSStatusUI();
            }
        });
    }

    private void checkHasDetectedContacts()
    {
        if(currIndex == 0)
        {
            txtNoneDetectionNotification.setText(strExchangedNoneDetectionMessage);
            if(sproutExchangedFragment != null)
            {
                if(sproutExchangedFragment.getItemCount()>0)
                    txtNoneDetectionNotification.setVisibility(View.INVISIBLE);
                else
                    txtNoneDetectionNotification.setVisibility(View.VISIBLE);
            }
            else {
                txtNoneDetectionNotification.setVisibility(View.VISIBLE);
            }
        }
        else {
            txtNoneDetectionNotification.setText(strUnExchangedNoneDetectionMessage);
            if(sproutUnExchangedFragment != null)
            {
                if(sproutUnExchangedFragment.getItemCount()>0)
                    txtNoneDetectionNotification.setVisibility(View.INVISIBLE);
                else
                    txtNoneDetectionNotification.setVisibility(View.VISIBLE);
            }
            else {
                txtNoneDetectionNotification.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkSearchContacts()
    {
        if(currIndex == 0)
        {
            txtNoneDetectionNotification.setText(strExchangedNoneDetectionMessage);
            if(sproutExchangedFragment != null)
            {
                if(sproutExchangedFragment.getVisibleItemCount()>0) {
                    txtNoneDetectionNotification.setVisibility(View.INVISIBLE);
                    listModeViewPager.setBackgroundColor(Color.TRANSPARENT);

                    btnEdit.setImageResource(R.drawable.editcontact);
                }
                else {
                    txtNoneDetectionNotification.setVisibility(View.VISIBLE);
                    listModeViewPager.setBackgroundResource(R.drawable.leafbgforblank);

                    btnEdit.setImageResource(R.drawable.editcontact_disable);
                }

                if (sproutExchangedFragment.getItemCount() == 0)
                {
                    if (isEditable) {
                        isEditable = false;
                        updateEditableUI();
                        if (!isEditable)
                            listModeViewPager.setScanScroll(true);
                        else {
                            if (!isPageScrolling)
                                listModeViewPager.setScanScroll(false);
                        }
                        sproutExchangedFragment.setIsListSelectable(false);
                        sproutUnExchangedFragment.setIsListSelectable(false);
                   }

                }
            }
            else {
                txtNoneDetectionNotification.setVisibility(View.VISIBLE);
                btnEdit.setImageResource(R.drawable.editcontact_disable);
                listModeViewPager.setBackgroundResource(R.drawable.leafbgforblank);

                if (sproutExchangedFragment.getItemCount() == 0)
                {
                    if (isEditable) {
                        isEditable = false;
                        updateEditableUI();
                        if (!isEditable)
                            listModeViewPager.setScanScroll(true);
                        else {
                            if (!isPageScrolling)
                                listModeViewPager.setScanScroll(false);
                        }
                        sproutExchangedFragment.setIsListSelectable(false);
                        sproutUnExchangedFragment.setIsListSelectable(false);
                    }

                }
            }
        }
        else {
            txtNoneDetectionNotification.setText(strUnExchangedNoneDetectionMessage);

            if(sproutUnExchangedFragment != null)
            {
                if(sproutUnExchangedFragment.getVisibleItemCount() > 0) {
                    txtNoneDetectionNotification.setVisibility(View.INVISIBLE);
                    listModeViewPager.setBackgroundColor(Color.TRANSPARENT);

                    btnEdit.setImageResource(R.drawable.editcontact);
                }
                else {
                    txtNoneDetectionNotification.setVisibility(View.VISIBLE);
                    listModeViewPager.setBackgroundResource(R.drawable.leafbgforblank);

                    btnEdit.setImageResource(R.drawable.editcontact_disable);
                }

                if (sproutUnExchangedFragment.getItemCount() == 0)
                {
                    if (isEditable) {
                        isEditable = false;
                        updateEditableUI();
                        if (!isEditable)
                            listModeViewPager.setScanScroll(true);
                        else {
                            if (!isPageScrolling)
                                listModeViewPager.setScanScroll(false);
                        }
                        sproutExchangedFragment.setIsListSelectable(false);
                        sproutUnExchangedFragment.setIsListSelectable(false);
                    }
                }
            }
            else {
                txtNoneDetectionNotification.setVisibility(View.VISIBLE);
                btnEdit.setImageResource(R.drawable.editcontact_disable);
                listModeViewPager.setBackgroundResource(R.drawable.leafbgforblank);

                if (sproutUnExchangedFragment.getItemCount() == 0)
                {
                    if (isEditable) {
                        isEditable = false;
                        updateEditableUI();
                        if (!isEditable)
                            listModeViewPager.setScanScroll(true);
                        else {
                            if (!isPageScrolling)
                                listModeViewPager.setScanScroll(false);
                        }
                        sproutExchangedFragment.setIsListSelectable(false);
                        sproutUnExchangedFragment.setIsListSelectable(false);
                    }
                }
            }
        }
    }


    private void DeleteDetectedContactOrEntity(int removeType)
    {
        if(currIndex == 0)//exchanged contact fragment
        {
            final List<SproutSearchItem> selectedItems = sproutExchangedFragment.getSelectedItems();
            final int selectedCount = selectedItems.size();
            String deleteContactIDs = "";
            for(int i= 0;i<selectedCount;i++)
            {
                SproutSearchItem item = selectedItems.get(i);
                deleteContactIDs = deleteContactIDs + String.valueOf(item.contactOrEntityID) + ",";
            }
            if(deleteContactIDs.length()>0)
                deleteContactIDs = deleteContactIDs.substring(0 , deleteContactIDs.length()-1);
            //SpoutRequest.deleteFoundFriends(deleteContactIDs , removeType , new ResponseCallBack<Void>() {
            CBRequest.removeFriends(deleteContactIDs, new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if (response.isSuccess()) {
                        for (int i = 0; i < selectedCount; i++) {
                            SproutSearchItem item = selectedItems.get(i);
                            detectedContactsAroundTown.remove(item);
                        }
                        sproutExchangedFragment.removeSelectedItemsFromList();
                    }
                    //checkHasDetectedContacts();
                    checkSearchContacts();
                }
            }, true);
        }
        else//unexchanged contact fragment
        {
            final List<SproutSearchItem> selectedItems = sproutUnExchangedFragment.getSelectedItems();
            final int selectedCount = selectedItems.size();
            String deleteContactIDs = "" , deleteEntityIDs = "";

            for(int i= 0;i<selectedCount;i++)
            {
                SproutSearchItem item = selectedItems.get(i);
                if(item.contactType == 3)//entity
                {
                    deleteEntityIDs = deleteEntityIDs + String.valueOf(item.contactOrEntityID) + ",";
                }
                else
                {
                    deleteContactIDs = deleteContactIDs + String.valueOf(item.contactOrEntityID) + ",";
                }
            }
            //delete selected contact items
            if(deleteContactIDs.length()>0) {
                deleteContactIDs = deleteContactIDs.substring(0, deleteContactIDs.length() - 1);

                SpoutRequest.deleteFoundFriends(deleteContactIDs , removeType , new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if(response.isSuccess())
                        {
                            sproutUnExchangedFragment.removeSelectedContactItemsFromList();
                            for(int i= 0;i<selectedCount;i++)
                            {
                                SproutSearchItem item = selectedItems.get(i);
                                detectedUnexchangedContacts.remove(item);
                            }
                        }
                        //checkHasDetectedContacts();
                        checkSearchContacts();
                    }
                } , true);
            }
            //delete selected entity items
            if(deleteEntityIDs.length()>0) {
                deleteEntityIDs = deleteEntityIDs.substring(0, deleteEntityIDs.length() - 1);
                SpoutRequest.deleteFoundEntities(deleteEntityIDs , removeType , new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if(response.isSuccess())
                        {
                            sproutUnExchangedFragment.removeSelectedEntityItemsFromList();
                        }
                        checkSearchContacts();
                    }
                } , true);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 3322) {
            detectedContactsAroundTown.clear();
            detectedUnexchangedContacts.clear();

            mapitemListLayout.setVisibility(View.GONE);
            isNewDetectUser = true;
            isLoad = false;

            detectAllContact(null, false, true);
            detectUnExchangedContact(null, false, true);

            final Handler handler = new Handler();
            updateMarkers();
            mapitemListLayout.setVisibility(View.GONE);
        }else if(requestCode == 123) {
            /*
            if(latestLocation != null)
            {
                LatLng latlng = new LatLng(latestLocation.getLatitude() , latestLocation.getLongitude());
                moveMapToPositon(latlng.latitude, latlng.longitude, 13);
            }*/
            updateMarkers();
        } else if (resultCode == Activity.RESULT_OK && requestCode == 2233) {
            detectedContactsAroundTown.clear();
            detectedUnexchangedContacts.clear();

            isNewDetectUser = true;
            isLoad = false;

            mapitemListLayout.setVisibility(View.GONE);
            detectAllContact(null, false, true);
            detectUnExchangedContact(null, false, true);

            final Handler handler = new Handler();
            updateMarkers();

            int cSharingStatus = -1;
            if (data != null) {
                cSharingStatus = data.getIntExtra("nSharingStatus", 11);
            }

            if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                for(int i = 0; i <mapItemListAdapter.getCount(); i++) {
                    SproutSearchItem sproutSearchItem = (SproutSearchItem) mapItemListAdapter.getItem(i);
                    if(sproutSearchItem.contactOrEntityID == m_tmpId && cSharingStatus != -1)
                    {
                        int originalStatus = ((SproutSearchItem) mapItemListAdapter.getItem(i)).nSharingStatus;
                        if (cSharingStatus != 11)
                            ((SproutSearchItem) mapItemListAdapter.getItem(i)).isPending = true;
                        else if (cSharingStatus == 11)
                            ((SproutSearchItem) mapItemListAdapter.getItem(i)).isPending = false;
                        break;
                    }
                }
                mapItemListAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == 980) {
            detectedContactsAroundTown.clear();
            detectedUnexchangedContacts.clear();

            //mapitemListLayout.setVisibility(View.GONE);
            isNewDetectUser = true;
            isLoad = false;

            detectAllContact(null, false, true);
            detectUnExchangedContact(null, false, true);
            mapitemListLayout.setVisibility(View.GONE);

            if (data != null)
            {
                int contact_ID = data.getIntExtra("contactID", 0);
                boolean isFollow = data.getBooleanExtra("isFollowEntity", false);

                if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                    for(int i = 0; i <mapItemListAdapter.getCount(); i++) {
                        SproutSearchItem sproutSearchItem = (SproutSearchItem) mapItemListAdapter.getItem(i);
                        if(sproutSearchItem.contactOrEntityID == contact_ID) {
                            if(((SproutSearchItem) mapItemListAdapter.getItem(i)).isFollowed == true && isFollow == false) {
                                ((SproutSearchItem) mapItemListAdapter.getItem(i)).isFollowed = false;
                                if (sproutSearchItem.nEnityFollowerCount > 0)
                                    ((SproutSearchItem) mapItemListAdapter.getItem(i)).nEnityFollowerCount -= 1;
                            }
                            else if (((SproutSearchItem) mapItemListAdapter.getItem(i)).isFollowed == false && isFollow == true) {
                                ((SproutSearchItem) mapItemListAdapter.getItem(i)).isFollowed = true;
                                ((SproutSearchItem) mapItemListAdapter.getItem(i)).nEnityFollowerCount += 1;
                            }
                            break;
                        }
                    }
                    mapItemListAdapter.notifyDataSetChanged();
                }
            }

            final Handler handler = new Handler();
            updateMarkers();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
                hideKeyboard();
                finish();
                break;

            case R.id.btnContactFilter:
                hideKeyboard();
                Intent intent = new Intent(MyGinkoMeActivity.this, GinkoMeFilterActivity.class);
                startActivityForResult(intent, 123);

                break;

            case R.id.imgGPSOnOff:
                if(sproutService == null)
                {
                    return;
                }

                if(isChangingSproutSetting) return;//if its waiting setting response from server , then return;
                if(isGpsOn)
                {
                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (sproutStartedTime == 0)//if sprout is not set to turn off after one hour automatically or
                    //or the sprout time is exceed one hour , then turn off sprout
                    {
                        if(!sproutService.isLocationServiceEnabled()) {
                            showLocationServiceSettingAlertDialog();
                            return;
                        }
                        sproutStartedTime = currentTime;
                        sproutService.setAutoOffStartTime(sproutStartedTime);
                        sproutService.startSproutService();
                        refreshGPSStatusUI();
                    } else {
                        turnOnOff(false);
                    }
                }
                else
                {
                    if(!sproutService.isLocationServiceEnabled()) {
                        showLocationServiceSettingAlertDialog();
                        return;
                    }
                    turnOnOff(true);
                }
                break;

            case R.id.btnEdit:
                if(isEditable) return;
                if (currIndex == 0)
                {
                    if(sproutExchangedFragment == null || sproutExchangedFragment.getVisibleItemCount() == 0)
                        return;
                } else
                {
                    if(sproutUnExchangedFragment == null || sproutUnExchangedFragment.getVisibleItemCount() == 0)
                        return;
                }

                isEditable = true;
                updateEditableUI();
                //listModeViewPager.setScanScroll(false);
                sproutExchangedFragment.setIsListSelectable(false);
                sproutUnExchangedFragment.setIsListSelectable(false);

                sproutExchangedFragment.setIsListSelectable(true);
                sproutUnExchangedFragment.setIsListSelectable(true);
                break;

            case R.id.btnClose:
                isEditable = false;
                updateEditableUI();
                if (!isEditable)
                    listModeViewPager.setScanScroll(true);
                else {
                    if (!isPageScrolling)
                        listModeViewPager.setScanScroll(false);
                }
                sproutExchangedFragment.setIsListSelectable(false);
                sproutUnExchangedFragment.setIsListSelectable(false);
                break;

            case R.id.imgTabMap:
                if(bMapMode)
                    return;
                bMapMode = true;
                isEditable = false;
                if(latestLocation != null)
                {
                    updateMarkers();
                    LatLng latlng = new LatLng(latestLocation.getLatitude() , latestLocation.getLongitude());
                    moveMapToPositon(latlng.latitude, latlng.longitude, mapView.getCameraPosition().zoom);
                } else {
                    moveMapToPositon(firstSproutItem.lat , firstSproutItem.lng , mapView.getCameraPosition().zoom);
                }
                isShownKeyboard();
                tmp_isKeyVisible = isKeyboardVisible;

                hideKeyboard();
                updateEditableUI();
                updateTabMode();
                // Add by wang GAD-867
                listModeViewPager.setScanScroll(true);
                sproutExchangedFragment.setIsListSelectable(false);
                sproutUnExchangedFragment.setIsListSelectable(false);
                break;

            case R.id.imgTabList:
                bMapMode = false;
                if(tmp_isKeyVisible) {
                    edtSearch.requestFocus();
                    showKeyboard();
                }
                updateTabMode();
                break;

            //delete contact button
            case R.id.btnDeleteContact:
                int nSelectedCounts = 0;
                List<SproutSearchItem> selectedSearchItems = null;
                if(currIndex == 0) //exchanged fragment
                {
                    selectedSearchItems = sproutExchangedFragment.getSelectedItems();
                }
                else//unexchanged fragment
                {
                    selectedSearchItems = sproutUnExchangedFragment.getSelectedItems();
                }
                if(selectedSearchItems ==null ) return;
                nSelectedCounts = selectedSearchItems.size();
                if(nSelectedCounts == 0) return;

                //check the selected items are visible or not
                int visibleSelectedItemCounts = 0;
                for(int i=0;i<selectedSearchItems.size(); i++)
                {
                    if(selectedSearchItems.get(i).isVisible())
                        visibleSelectedItemCounts++;
                }
                if(visibleSelectedItemCounts == 0) return;

                setTheme(R.style.ActionSheetStyleIOS7);
                if(deleteContactActionSheet == null)
                    deleteContactActionSheet = ActionSheet.createBuilder(MyGinkoMeActivity.this, getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.str_sprout_delete_contact_permanently) ,
                                    getResources().getString(R.string.str_sprout_delete_contact_for_24hours))
                            .setCancelableOnTouchOutside(true)
                            .setListener(this).show();
                else
                    deleteContactActionSheet.show(getSupportFragmentManager() , "actionSheet");
                break;

            //show contacts around the town
            case R.id.btnTabContactsAroundTown:
                if(!isEditable) {
                    currIndex = 0;
                    if (listModeViewPager != null)
                        listModeViewPager.setCurrentItem(currIndex);
                }
                break;

            //unexchanged contact
            case R.id.btnTabUnExchanged:
                if(!isEditable) {
                    currIndex = 1;
                    if (listModeViewPager != null)
                        listModeViewPager.setCurrentItem(currIndex);
                }
                break;
        }
    }

    @Override
    public void onSproutAutoStopped() {
        System.out.println("---- Time over! Turned off the service automatically!---");
        this.sproutStartedTime = 0;
        Uitils.storeSproutStartTime(MyApp.getContext() , 0);
        this.isGpsOn = false;
        refreshGPSStatusUI();
    }

    @Override
    public void singleLocationUpdateStarted() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("----Single Location update started----");
                imgGPSOnOff.setImageResource(R.drawable.gpson);
            }
        }, 100);
    }

    @Override
    public void singleLocationUpdateEnded() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("----Single Location update ended----");
                imgGPSOnOff.setImageResource(R.drawable.gpsoff);
            }
        } , 100);
    }

    //called when background sproutService is started and get some location data
    @Override
    public void singleLocationChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(isGpsOn) {
                    firstSproutItem = null;
                    currentLoc = null;
                    currentPosMarker = null;
                    getCurrentLocation();

                    detectAllContact(null, false, true);
                    detectUnExchangedContact(null, false, true);

                    final Handler handler = new Handler();
                    updateMarkers();
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSelectedStausChanged(SproutSearchItem sproutSearchItem, boolean isSelected) {
        int nSelectedCounts = 0;
        if(currIndex == 0) //exchanged fragment
        {
            nSelectedCounts = sproutExchangedFragment.getSelectedItemCounts();
        }
        else//unexchanged fragment
        {
            nSelectedCounts = sproutUnExchangedFragment.getSelectedItemCounts();
        }
        if(nSelectedCounts > 0)
            btnDeleteContact.setImageResource(R.drawable.btn_delete_enabled);
        else
            btnDeleteContact.setImageResource(R.drawable.btn_delete_disabled);
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    //click actionsheet listener
    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//Delete Contact Permanently
        {
            DeleteDetectedContactOrEntity(DETECTED_REMOVE_TYPE_PERMANENTLY);
        }
        else if(index == 1) //Delete contact for 24 hours
        {
            DeleteDetectedContactOrEntity(DETECTED_REMOVE_TYPE_FOR_24_HOURS);
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
            isPageScrolling = false;
            if (currIndex == 0) {
                txtHint.setText(getResources().getString(R.string.str_contacts_around_town));
            } else {
                txtHint.setText(getResources().getString(R.string.str_exchange_info_with_these_contacts));
            }
            if (isEditable) {
                int nSelectedCounts = 0;
                if (currIndex == 0) //exchanged fragment
                {
                    nSelectedCounts = sproutExchangedFragment.getSelectedItemCounts();
                } else//unexchanged fragment
                {
                    nSelectedCounts = sproutUnExchangedFragment.getSelectedItemCounts();
                }
                if (nSelectedCounts > 0)
                    btnDeleteContact.setImageResource(R.drawable.btn_delete_disabled);
                else
                    btnDeleteContact.setImageResource(R.drawable.btn_delete_disabled);
            }

            if (currIndex == 0) {

                sproutExchangedFragment.searchItems(edtSearch.getText().toString().toLowerCase());

            } else {

                sproutUnExchangedFragment.searchItems(edtSearch.getText().toString().toLowerCase());
            }

            //checkHasDetectedContacts();
            checkSearchContacts();

            updateBottomNaviPointer();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            isPageScrolling = true;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    }

    class MyLocationListener implements LocationListener {

        @Override
        public synchronized void onLocationChanged(Location location) {
            if (location != null) {
                latestLocation = location;
                System.out.println("----Location changed valueable----");
                lm.removeUpdates(locationListener);
                //if(progressDialog!=null && progressDialog.isShowing())
                //    progressDialog.dismiss();
                locationHandler.removeCallbacks(locationTimeoutRunnable);
                synchronized (lockObj) {
                    if (isGettingCurrentLocation) {
                        isGettingCurrentLocation = false;

                        if(latestLocation != null)
                            showCurrentLocationMarker(new LatLng(latestLocation.getLatitude() , latestLocation.getLongitude()));
                    }
                }

                SpoutRequest.updateLocation(location.getLatitude(), location.getLongitude(), new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            Logger.debug("Upload location successfully!");
                            detectUnExchangedContact(null, false, true);
                            detectAllContact(null, false, true);
                        }
                    }
                });
            }
            else
            {
                System.out.println("----Location changed null----");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("----Location changed null----");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("----Provider Disabled --"+provider);
        }
    }

    class MapItemListAdapter extends BaseAdapter
    {
        private Context mContext;
        private ArrayList<SproutSearchItem> items;
        public MapItemListAdapter(Context context)
        {
            this.mContext = context;
            items = new ArrayList<SproutSearchItem>();
        }

        public void setListItems(ArrayList<SproutSearchItem> listItems)
        {
            items = listItems;
        }

        public void setItem(int position, SproutSearchItem item) {
            items.set(position, item);
        }

        public void removeItem(int position)
        {
            items.remove(position);
        }

        @Override
        public int getCount() {
            return items==null?0:items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            SproutSearchItem item = (SproutSearchItem) getItem(position);
            if(item == null) return 0;
            return item.isExchanged?1:0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SproutSearchItem item = items.get(position);
            if(item == null) return null;

            int type = getItemViewType(position);
            SproutListItemView view = null;
            if (convertView == null) {

                if(type == 1)
                    view = new SproutExchangedItemView(mContext , item);
                else
                    view = new SproutUnExchangedItemView(mContext , item);
            }
            else
            {
                view = (SproutListItemView)convertView;
            }
            view.setPadding(30,0,30,0);

            if(item.strAddress.equals("") && item.lat!=0.0d && item.lng!=0.0d && !item.isGettingAddress) {

                GetSproutAddressFromLatlngTask getAddressFromLatlngTask = new GetSproutAddressFromLatlngTask(MyGinkoMeActivity.this, item , this);
                getAddressFromLatlngTask.start();
            }

            //check box
            ImageView imgCheck = (ImageView) view.findViewById(R.id.imageSelectionCheck);
            imgCheck.setVisibility(View.GONE);

            view.setItem(item);
            view.refreshView();
            return view;

        }
    }

    public class ReceiveEntityRemove extends BroadcastReceiver {
        public ReceiveEntityRemove() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String str_id = bundle.getString("entity_id", "0");
            int contact_id = Integer.parseInt(str_id);

            MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contact_id);
            MyApp.getInstance().getContactsModel().deleteContactWithContactId(contact_id);

            if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                for(int i = 0; i <mapItemListAdapter.getCount(); i++) {
                    SproutSearchItem sproutSearchItem = (SproutSearchItem) mapItemListAdapter.getItem(i);
                    if(sproutSearchItem.contactOrEntityID != contact_id)
                        items.add(sproutSearchItem);
                }

                if (mapItemListAdapter.getCount() == 0)
                    mapitemListLayout.setVisibility(View.GONE);
                mapItemListAdapter.setListItems(items);
                mapItemListAdapter.notifyDataSetChanged();
            }

            //mapitemListLayout.setVisibility(View.GONE);
            isNewDetectUser = true;
            isLoad = false;


            //if (latestLocation != null)
            //    showCurrentLocationMarker(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
            recreateMarker();
            updateMarkers();
        }
    }

    public class DetectUserReceiver extends BroadcastReceiver {
        public DetectUserReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String str_id = bundle.getString("contact_id", "0");

            detectedContactsAroundTown.clear();
            detectedUnexchangedContacts.clear();

            //mapitemListLayout.setVisibility(View.GONE);
            isNewDetectUser = true;
            isLoad = false;

            detectAllContact(null, false, true);
            detectUnExchangedContact(null, false, true);

            int contact_id = Integer.parseInt(str_id);

            if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                ArrayList<SproutSearchItem> items = new ArrayList<SproutSearchItem>();
                for(int i = 0; i <mapItemListAdapter.getCount(); i++) {
                    SproutSearchItem sproutSearchItem = (SproutSearchItem) mapItemListAdapter.getItem(i);
                    if(sproutSearchItem.contactOrEntityID != contact_id)
                        items.add(sproutSearchItem);
                }

                if (mapItemListAdapter.getCount() == 0)
                    mapitemListLayout.setVisibility(View.GONE);
                mapItemListAdapter.setListItems(items);
                mapItemListAdapter.notifyDataSetChanged();
            }
        }
    }

    public class ReceiveExchangeRequest extends BroadcastReceiver {
        public ReceiveExchangeRequest() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            String str_id = bundle.getString("removed_id", "");
            String[] seperated = str_id.split(",");

            detectedContactsAroundTown.clear();
            detectedUnexchangedContacts.clear();

            //mapitemListLayout.setVisibility(View.GONE);
            isNewDetectUser = true;
            isLoad = false;

            detectAllContact(null, false, true);
            detectUnExchangedContact(null, false, true);

            if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                for(int i = 0; i <mapItemListAdapter.getCount(); i++) {
                    SproutSearchItem sproutSearchItem = (SproutSearchItem) mapItemListAdapter.getItem(i);
                    for (int j = 0; j < seperated.length; j++)
                    {
                        if (seperated[j].equals("")) continue;
                        if (sproutSearchItem.contactOrEntityID == Integer.valueOf(seperated[j])) {
                            mapItemListAdapter.removeItem(i);
                            break;
                        }
                    }
                }

                if (mapItemListAdapter.getCount() == 0)
                    mapitemListLayout.setVisibility(View.GONE);
                mapItemListAdapter.notifyDataSetChanged();
            }
        }
    }

    public class ExchangedContact extends BroadcastReceiver {
        public ExchangedContact() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            String str_id = bundle.getString("contact_id", "0");
            final int contact_id = Integer.parseInt(str_id);

            detectedContactsAroundTown.clear();
            detectedUnexchangedContacts.clear();

            detectAllContact(null, false, true);
            detectUnExchangedContact(null, false, true);
            /*

            UserRequest.getContactDetail(String.valueOf(contact_id), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                @Override
                public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                    if (response.isSuccess()) {
                        PurpleContactWholeProfileVO myInfo = response.getData();

                        if (mapitemListLayout.getVisibility() == View.VISIBLE) {
                            for(int k = 0; k <mapItemListAdapter.getCount(); k++) {
                                SproutSearchItem thisItem = (SproutSearchItem) mapItemListAdapter.getItem(k);
                                if(thisItem.contactOrEntityID == contact_id) {

                                    ((SproutSearchItem) mapItemListAdapter.getItem(k)).nSharingStatus = myInfo.getSharingStatus();
                                    ((SproutSearchItem) mapItemListAdapter.getItem(k)).isExchanged = true;
                                }
                            }
                            mapItemListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
            */

            //if (latestLocation != null)
              //  showCurrentLocationMarker(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
        }
    }

    public class MapRenderer extends CustomDefaultClusterRenderer<SproutSearchItem> {
        public MapRenderer(Context context, GoogleMap map, ClusterManager<SproutSearchItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(final SproutSearchItem person, final MarkerOptions markerOptions)
        {
            final MarkerOptions markerOption = markerOptions;
            float m_tempZoom = mapView.getCameraPosition().zoom;

            if (person.contactType == 3) {
                if(m_tempZoom < 5.0)
                    mImageView.setImageResource(R.drawable.green_pageindicator_fill);
                else {
                    if(person.profile_image == null || "".equals(person.profile_image)) {
                        mImageView.setImageResource(R.drawable.green_pageindicator_fill);
                    }
                    else {
                        Picasso.with(MyGinkoMeActivity.this).load(person.profile_image).transform(new CircleTransform()).into(mImageView);
                    }
                }
            }
            else {
                if (m_tempZoom < 5.0)
                {
                    if (person.isExchanged) {
                        if (person.isFocused) {
                            mImageView.setImageResource(R.drawable.ginkome_map_marker_purple_selected);
                        } else {
                            mImageView.setImageResource(R.drawable.ginkome_map_marker_purple);
                        }
                    } else {
                        if (person.isFocused) {
                            mImageView.setImageResource(R.drawable.ginkome_map_marker_grey_selected);
                        } else {
                            mImageView.setImageResource(R.drawable.ginkome_map_marker_grey);
                        }
                    }
                } else {
                    if (person.isExchanged) {
                        if (person.isFocused) {
                            mImageView.setImageResource(R.drawable.purple_photo);
                            //mImageView.setImageUrl("", imgLoader);
                        } else {
                            mImageView.setImageResource(R.drawable.purple_photo);
                            //mImageView.setImageUrl("", imgLoader);
                        }
                        if(person.profile_image == null || "".equals(person.profile_image))
                            mImageView.setImageResource(R.drawable.purple_photo);
                        else
                            Picasso.with(MyGinkoMeActivity.this).load(person.profile_image).transform(new CircleTransform()).into(mImageView);
                    } else {
                        if (person.isFocused) {
                            mImageView.setImageResource(R.drawable.ginkome_map_marker_grey_selected);
                        } else {
                            mImageView.setImageResource(R.drawable.ginkome_map_marker_grey);
                        }
                    }
                }
            }

            int m_dpBonus = (int)m_tempZoom * 5 - 20;
            mImageView.setLayoutParams(new ViewGroup.LayoutParams((int) (mDimension / 2) + m_dpBonus, (int) (mDimension / 2) + m_dpBonus));
            mIconGenerator.setContentView(mImageView);

            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)); //.title(String.valueOf(person.contactOrEntityID));

        }

        @Override
        protected void onClusterItemRendered(SproutSearchItem clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
            if(!isLoad) {
                isLoad = true;
            }
        }
    }

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}
