package com.ginko.activity.entity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.im.ImAddLocationMessageActivity;
import com.ginko.activity.menu.MenuActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.videophotofilter.android.videolib.org.m4m.StreamingParameters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AllEntityPreviewListActivity extends MyBaseFragmentActivity implements View.OnClickListener{

    public static final int STATIC_ALL_ITEM_INTEGER_VALUE = 111;
    /* Variables */
    private EntityVO entity;
    private boolean isNewEntity = false;
    private boolean isMultiLocations = false;

    private ImageButton btnBack;
    private TextView txtTitle;
    private RelativeLayout headerlayout;

    private ListView mListView;
    private PullToRefreshListView mPullListView;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private ALLLocationsListAdapter mAdpater;
    private ArrayList<ALLLocationsItem> locationItems;
    private boolean isLoadingEnd = true;

    private Integer locationTotal = 0;

    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    private Location latestLocation = null;
    private GPSTracker gps;
    private double latitude;
    private double longitude;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_locations_preview);

        if (savedInstanceState != null) {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isNewEntity = savedInstanceState.getBoolean("isNewEntity", false);
            this.locationTotal = savedInstanceState.getInt("locationTotal");
        } else {
            this.entity = (EntityVO) this.getIntent().getSerializableExtra("entity");
            this.isNewEntity = this.getIntent().getBooleanExtra("isNewEntity", false);
            this.isMultiLocations = this.getIntent().getBooleanExtra("isMultiLocations", false);
            this.locationTotal = this.getIntent().getIntExtra("locationTotal",0);
        }
        latitude = 0;
        longitude = 0;

        gps = new GPSTracker(AllEntityPreviewListActivity.this);
        latestLocation = new Location("");

        if (gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            latestLocation.setLongitude(longitude);
            latestLocation.setLatitude(latitude);
        }else{
            gps.showSettingAlert();
        }
        getUIObjects();
    }


    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        txtTitle = (TextView)findViewById(R.id.textViewTitle);

        if(isNewEntity) {
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnBack.setImageResource(R.drawable.btn_back_nav_white);
        }

        mPullListView = (PullToRefreshListView) findViewById(R.id.pullToRefreshView);
        mPullListView.setPullLoadEnabled(true);
        if (locationTotal <= 20)
        {
            mPullListView.setScrollLoadEnabled(false);
        }
        else
        {
            mPullListView.setScrollLoadEnabled(true);
        }
        mListView = mPullListView.getRefreshableView();

        locationItems = new ArrayList<ALLLocationsItem>();

        for(int i=0; i< this.entity.getEntityInfos().size(); i++) {

            ALLLocationsItem searchItem = new ALLLocationsItem();

            String tmpLocationName = "";
            for(int j=0;j<entity.getEntityInfos().get(i).getEntityInfoDetails().size();j++)
            {
                if (entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                    tmpLocationName = entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                }
            }
            int count =  this.entity.getEntityInfos().size();
            if (tmpLocationName == "") {
                searchItem.locationName = "Location #" + count;
            }
            else {
                searchItem.locationName = tmpLocationName;
            }

            float latitudeOfEntity;
            float longitudeOfEntity;
            if (entity.getEntityInfos().get(i).isAddressConfirmed() == false){
                latitudeOfEntity = 0;
                longitudeOfEntity = 0;
            }else
            {
                latitudeOfEntity = Float.parseFloat(entity.getEntityInfos().get(i).getLatitude());
                longitudeOfEntity = Float.parseFloat(entity.getEntityInfos().get(i).getLongitude());
            }
            Location currentOfEntity = new Location("");
            currentOfEntity.setLatitude(latitudeOfEntity);
            currentOfEntity.setLongitude(longitudeOfEntity);
            /*if (latestLocation == null){
                latestLocation.setLatitude(0);
                latestLocation.setLongitude(0);
            }*/
            if (latestLocation == null){
                searchItem.distanceValue = "No Address";
                searchItem.isAddress = false;
            }else
            {
                float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                String distanceVal = tmp_distance.replace(",", ".");
                searchItem.distanceValue = distanceVal;
                searchItem.isAddress = true;
            }
            searchItem.infoId = entity.getEntityInfos().get(i).getId();

            locationItems.add(searchItem);
        }

        Collections.sort(locationItems, new SortPlaces());

        mAdpater = new ALLLocationsListAdapter(AllEntityPreviewListActivity.this, locationItems);
        //mAdpater.addItems(locationItems);
        mListView.setAdapter(mAdpater);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);

        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AllEntityPreviewListActivity.this, EntityProfilePreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("entity", entity);
                bundle.putSerializable("isNewEntity", isNewEntity);
                bundle.putInt("infoID", locationItems.get(position).infoId);
                bundle.putBoolean("isMultiLocations", true);
                intent.putExtras(bundle);
                startActivityForResult(intent, STATIC_ALL_ITEM_INTEGER_VALUE);
            }
        });
        mPullListView.getHeaderLoadingLayout().setVisibility(View.INVISIBLE);
        mPullListView.getFooterLoadingLayout().setVisibility(View.GONE);

        mPullListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Algorithm to check if the last item is visible or not
//                if (locationTotal <= 20 || !isLoadingEnd) {
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            // when fail, also let the header/footer hide.
//                            mPullListView.onPullDownRefreshComplete();
//                            mPullListView.onPullUpRefreshComplete();
//                        }
//                    }, 50);
//                    return;
//                }
//                final int lastItem = firstVisibleItem + visibleItemCount;
//                if (lastItem == totalItemCount) {
//                    if (mStartPos <= locationTotal) {
//                        reloadEntityInfos();
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                // when fail, also let the header/footer hide.
//                                mPullListView.onPullDownRefreshComplete();
//                                mPullListView.onPullUpRefreshComplete();
//                            }
//                        }, 500);
//                    }
//                }
            }
        });
        mPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // when fail, also let the header/footer hide.
                        mPullListView.onPullDownRefreshComplete();
                        mPullListView.onPullUpRefreshComplete();
                    }
                }, 50);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
        //setLastUpdateTime();
    }

    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        mPullListView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }
        return mDateFormat.format(new Date(time));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startTimer();
    }
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 3000); //
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        if (gps.canGetLocation()){
                            latestLocation = gps.getLocation();
                        }else{
                            gps.showSettingAlert();
                        }
                        locationItems.clear();

                        if (latestLocation != null){
                            for(int i=0; i< entity.getEntityInfos().size(); i++) {

                                ALLLocationsItem searchItem = new ALLLocationsItem();

                                String tmpLocationName = "";
                                for(int j=0;j<entity.getEntityInfos().get(i).getEntityInfoDetails().size();j++)
                                {
                                    if (entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                                        tmpLocationName = entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                                    }
                                }
                                int count =  entity.getEntityInfos().size();
                                if (tmpLocationName == "") {
                                    searchItem.locationName = "Location #" + count;
                                }
                                else {
                                    searchItem.locationName = tmpLocationName;
                                }
                                float latitudeOfEntity;
                                float longitudeOfEntity;
                                if (entity.getEntityInfos().get(i).isAddressConfirmed() == false){
                                    latitudeOfEntity = 0;
                                    longitudeOfEntity = 0;
                                }else
                                {
                                    latitudeOfEntity = Float.parseFloat(entity.getEntityInfos().get(i).getLatitude());
                                    longitudeOfEntity = Float.parseFloat(entity.getEntityInfos().get(i).getLongitude());
                                }
                                Location currentOfEntity = new Location("");
                                currentOfEntity.setLatitude(latitudeOfEntity);
                                currentOfEntity.setLongitude(longitudeOfEntity);
                                if (latestLocation == null){
                                    searchItem.distanceValue = "No Address";
                                    searchItem.isAddress = false;
                                }else
                                {
                                    float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                                    String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                                    String distanceVal = tmp_distance.replace(",", ".");
                                    searchItem.distanceValue = distanceVal;
                                    searchItem.isAddress = true;
                                }

                                searchItem.infoId = entity.getEntityInfos().get(i).getId();
                                locationItems.add(searchItem);
                            }

                            Collections.sort(locationItems, new SortPlaces());

                            mAdpater.clearAll();
                            mAdpater.addItems(locationItems);
                            mAdpater.notifyDataSetChanged();
                        }
                    }
                });
            }
        };
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity" , this.entity);
        outState.putBoolean("isNewEntity", this.isNewEntity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
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
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case STATIC_ALL_ITEM_INTEGER_VALUE:
                    entity = (EntityVO) data.getSerializableExtra("entity");
                    locationItems.clear();
                    for(int i=0; i< this.entity.getEntityInfos().size(); i++) {

                        ALLLocationsItem searchItem = new ALLLocationsItem();

                        String tmpLocationName = "";
                        for(int j=0;j<entity.getEntityInfos().get(i).getEntityInfoDetails().size();j++)
                        {
                            if (entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                                tmpLocationName = entity.getEntityInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                            }
                        }
                        int count =  this.entity.getEntityInfos().size();
                        if (tmpLocationName == "") {
                            searchItem.locationName = "Location #" + count;
                        }
                        else {
                            searchItem.locationName = tmpLocationName;
                        }

                        float latitudeOfEntity;
                        float longitudeOfEntity;
                        if (entity.getEntityInfos().get(i).isAddressConfirmed() == false){
                            latitudeOfEntity = 0;
                            longitudeOfEntity = 0;
                        }else
                        {
                            latitudeOfEntity = Float.parseFloat(entity.getEntityInfos().get(i).getLatitude());
                            longitudeOfEntity = Float.parseFloat(entity.getEntityInfos().get(i).getLongitude());
                        }
                        Location currentOfEntity = new Location("");
                        currentOfEntity.setLatitude(latitudeOfEntity);
                        currentOfEntity.setLongitude(longitudeOfEntity);

                        if (latestLocation == null){
                            searchItem.distanceValue = "No Address";
                            searchItem.isAddress = false;
                        }else
                        {
                            float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                            String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                            String distanceVal = tmp_distance.replace(",", ".");
                            searchItem.distanceValue = distanceVal;
                            searchItem.isAddress = true;
                        }
                        searchItem.infoId = entity.getEntityInfos().get(i).getId();
                        locationItems.add(searchItem);
                    }
                    mAdpater.clearAll();
                    mAdpater.addItems(locationItems);
                    mAdpater.notifyDataSetChanged();
                    break;
            }
        }
    }

    private class ALLLocationsItem {
        public String locationName = "";
        public String distanceValue = "No Address";
        public int infoId = 0;
        public boolean isAddress = true;
        public ALLLocationsItem() {

        }

    }
    private class ALLLocationsListAdapter extends BaseAdapter {
        private List<ALLLocationsItem> locationsList;
        private Context mContext = null;

        public ALLLocationsListAdapter(Context context, ArrayList<ALLLocationsItem> items) {
            locationsList = new ArrayList<ALLLocationsItem>();
            this.mContext = context;
            if(items != null) {
                for (int i = 0; i<items.size(); i++)
                    this.locationsList.add(items.get(i));
            }
        }

        public void setListItems(List<ALLLocationsItem> list) {
            this.locationsList = list;
        }

        public void addItem(ALLLocationsItem contactItem)
        {
            locationsList.add(contactItem);
        }

        public void addItems(List<ALLLocationsItem> contactItems)
        {
            for(int i=0;i<contactItems.size();i++)
            {
                locationsList.add(contactItems.get(i));
            }
        }

        public void clearAll() {
            if (locationsList != null)
            {
                try
                {
                    locationsList.clear();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                locationsList = new ArrayList<ALLLocationsItem>();
            }
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return locationsList == null ? 0 : locationsList.size();
        }

        @Override
        public Object getItem(int position) {
            return locationsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ALLLocationsItemView itemView = null;
            ALLLocationsItem item = locationsList.get(position);
            if(itemView == null)
            {
                itemView = new ALLLocationsItemView(mContext , item);
            }
            else
            {
                itemView = (ALLLocationsItemView)convertView;
            }

            itemView.setItem(item);
            itemView.refreshView();

            return itemView;
        }
    }
    private class ALLLocationsItemView extends LinearLayout {
        private Context mContext = null;
        private ALLLocationsItem item;
        private LayoutInflater inflater;

        private CustomNetworkImageView imgEntityProfilePhoto;
        private TextView locationInfo;
        private TextView distanceInfo;

        private ImageLoader imgLoader;

        public ALLLocationsItemView(Context context, ALLLocationsItem contactItem) {
            super(context);
            this.mContext = context;
            this.item = contactItem;

            if(this.imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.all_locations_item , this , true);

            imgEntityProfilePhoto = (CustomNetworkImageView)findViewById(R.id.imgEntityProfilePhoto);
            imgEntityProfilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);

            imgEntityProfilePhoto.refreshOriginalBitmap();
            imgEntityProfilePhoto.setImageUrl(entity.getProfileImage(), imgLoader);

            imgEntityProfilePhoto.invalidate();

            locationInfo = (TextView)findViewById(R.id.locationInfo);
            distanceInfo = (TextView)findViewById(R.id.txtDistance);

        }

        public void setItem(ALLLocationsItem contactItem) {
            this.item = contactItem;
        }
        public ALLLocationsItem getItem(){return this.item;}

        public void refreshView()
        {
            locationInfo.setText(item.locationName);
            distanceInfo.setText(item.distanceValue);
        }
    }

    public class SortPlaces implements Comparator<ALLLocationsItem> {
        @Override
        public int compare(final ALLLocationsItem place1, final ALLLocationsItem place2) {
            if (place1.isAddress == false || place2.isAddress == false)
                return 1;
            else
                return (Float.parseFloat(place1.distanceValue) < Float.parseFloat(place2.distanceValue)) ? -1: (Float.parseFloat(place1.distanceValue) > Float.parseFloat(place2.distanceValue)) ? 1:0;
        }
    }

}
