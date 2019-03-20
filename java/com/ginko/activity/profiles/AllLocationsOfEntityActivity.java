package com.ginko.activity.profiles;

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
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.lee.pullrefresh.ui.PullToRefreshListView;
import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.entity.GPSTracker;
import com.ginko.api.request.EntityRequest;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityVO;
import com.ginko.vo.UserEntityProfileVO;
import com.hb.views.PinnedSectionListView;
import com.hb.views.PullToRefreshPinnedSectionListView;
import com.lee.pullrefresh.ui.PullToRefreshBase;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AllLocationsOfEntityActivity extends MyBaseFragmentActivity implements View.OnClickListener{

    /* Variables */
    private UserEntityProfileVO entity;
    private UserEntityProfileVO newEntity = null;
    private List<UserEntityProfileVO> entityList;
    private boolean isFollowedThisEntity = false;
    private boolean isFavorite = false;

    private int contactID = 0;
    private int mStartPos = 0;

    private ImageButton btnBack;

    private ListView mListView;
    private PullToRefreshListView mPullListView;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private ALLLocationsListAdapter mAdpater;
    private ArrayList<ALLLocationsItem> locationItems;

    private Location latestLocation = null;
    private GPSTracker gps;
    private double latitude;
    private double longitude;
    private boolean isLoadingEnd = true;

    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_locations_preview);

        Intent intent = this.getIntent();
        contactID = intent.getIntExtra("contactID", 0);
        isFavorite = intent.getBooleanExtra("isFavorite", false);
        entity = (UserEntityProfileVO)intent.getSerializableExtra("entity");
        isFollowedThisEntity = intent.getBooleanExtra("isfollowing_entity" , false);
        entityList = new ArrayList<>();
        entityList.add(entity);
        //parseEntityInfo(strEntityInfo);

        if(entity == null)
        {
            return;
        }

        latitude = 0;
        longitude = 0;

        gps = new GPSTracker(AllLocationsOfEntityActivity.this);
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
    protected void getUIObjects() {
        super.getUIObjects();

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        mPullListView = (PullToRefreshListView) findViewById(R.id.pullToRefreshView);
        mPullListView.setPullLoadEnabled(true);

        if (entity.getInfoTotal() <= 20)
        {
            mPullListView.setScrollLoadEnabled(false);
        }
        else
        {
            mPullListView.setScrollLoadEnabled(true);
        }


        mListView = mPullListView.getRefreshableView();

        locationItems = new ArrayList<ALLLocationsItem>();
        for (int i = 0; i < this.entity.getInfos().size(); i++) {

            ALLLocationsItem searchItem = new ALLLocationsItem();

            String tmpLocationName = "";
            for (int j = 0; j < entity.getInfos().get(i).getEntityInfoDetails().size(); j++) {
                if (entity.getInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")) {
                    tmpLocationName = entity.getInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                }
            }
            int count = this.entity.getInfos().size();
            if (tmpLocationName == "") {
                searchItem.locationName = "Location #" + count;
            } else {
                searchItem.locationName = tmpLocationName;
            }
            float latitudeOfEntity;
            float longitudeOfEntity;
            if (entity.getInfos().get(i).isAddressConfirmed() == false) {
                latitudeOfEntity = 0;
                longitudeOfEntity = 0;
            } else {
                String str_Latitude = entity.getInfos().get(i).getLatitude();
                String str_Longitude = entity.getInfos().get(i).getLongitude();

                if (str_Latitude == null || str_Longitude == null || str_Latitude.equals("null") || str_Longitude.equals("null")) {
                    latitudeOfEntity = 0.0f;
                    longitudeOfEntity = 0.0f;
                } else {
                    latitudeOfEntity = Float.parseFloat(entity.getInfos().get(i).getLatitude());
                    longitudeOfEntity = Float.parseFloat(entity.getInfos().get(i).getLongitude());
                }
            }
            Location currentOfEntity = new Location("");
            currentOfEntity.setLatitude(latitudeOfEntity);
            currentOfEntity.setLongitude(longitudeOfEntity);
            searchItem.currentOfEntity = currentOfEntity;

            if (latestLocation == null) {
                searchItem.distanceValue = "No Address";
                searchItem.isAddress = false;
            } else {

                float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                String tmp_distance = String.format("%.2f", distanceInMeters / 1609);
                String distanceVal = tmp_distance.replace(",", ".");
                searchItem.distanceValue = distanceVal;
                searchItem.isAddress = true;
            }
            searchItem.infoId = entity.getInfos().get(i).getId();
            locationItems.add(searchItem);
        }

        mAdpater = new ALLLocationsListAdapter(AllLocationsOfEntityActivity.this);
        mAdpater.addItems(locationItems);
        mListView.setAdapter(mAdpater);
        mListView.setFastScrollEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);

        //UtiltyDynamicOfHeight.setListViewHeightBasedOnChildren(allLocationListView);
        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, final int position, long id) {
                final int idx = (int) Math.floor(position / 20);
                EntityRequest.getFollowerTotal(contactID, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject json = response.getData();
                            isFollowedThisEntity = json.optBoolean("is_followed", false);

                            Intent entityProfileIntent = new Intent(AllLocationsOfEntityActivity.this, UserEntityProfileActivity.class);
                            entityProfileIntent.putExtra("entityJson", entityList.get(idx));
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("infoID", locationItems.get(position).infoId);
                            entityProfileIntent.putExtra("isfollowing_entity", isFollowedThisEntity);
                            entityProfileIntent.putExtra("isMultiLocations", true);
                            entityProfileIntent.putExtra("isNoLetter", true);
                            startActivityForResult(entityProfileIntent, 1234);
                        } else {
                            if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                MyApp.getInstance().showSimpleAlertDiloag(AllLocationsOfEntityActivity.this, "The entity Can't be found.", null);
                                MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactID);
                                MyApp.getInstance().removefromContacts(contactID);

                                MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contactID);
                            }
                        }
                    }
                }, true, false);
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
                if (entity.getInfoTotal() <= 20 || !isLoadingEnd)
                {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // when fail, also let the header/footer hide.
                            mPullListView.onPullDownRefreshComplete();
                            mPullListView.onPullUpRefreshComplete();
                        }
                    }, 50);
                    return;
                }
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (mStartPos <= entity.getInfoTotal()) {
                        reloadEntityInfos();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // when fail, also let the header/footer hide.
                                mPullListView.onPullDownRefreshComplete();
                                mPullListView.onPullUpRefreshComplete();
                            }
                        }, 500);
                    }
                }
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
        setLastUpdateTime();
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

    @Override
    public void onBackPressed() {
        if (!isLoadingEnd) return;
        Intent returnIntent = new Intent();
        returnIntent.putExtra("isFollowEntity",isFollowedThisEntity);
        returnIntent.putExtra("isFavorite",isFavorite);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        return;
        //super.onBackPressed();
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
                        if (!isLoadingEnd) return;

                        if (gps.canGetLocation()){
                            latestLocation = gps.getLocation();
                        }else{
                            gps.showSettingAlert();
                        }

                        if (latestLocation != null){
                            for(int i=0; i< locationItems.size(); i++) {

                                ALLLocationsItem searchItem = locationItems.get(i);

                                if (latestLocation == null){
                                    searchItem.distanceValue = "No Address";
                                    searchItem.isAddress = false;
                                }else
                                {
                                    float distanceInMeters = searchItem.currentOfEntity.distanceTo(latestLocation);
                                    String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                                    String distanceVal = tmp_distance.replace(",", ".");
                                    searchItem.distanceValue = distanceVal;
                                    searchItem.isAddress = true;
                                }
                            }
                            mAdpater.notifyDataSetChanged();
                        }
                    }
                });
            }
        };
    }

    private void reloadEntityInfos()
    {
        isLoadingEnd = false;
        mStartPos += 21;
        if (mStartPos >= entity.getInfoTotal())
        {
            mListView.setFastScrollEnabled(false);
            mListView.setFastScrollAlwaysVisible(false);
        }

        double latitude = 0;
        double longitude = 0;
        if (latestLocation != null) {
            latitude = latestLocation.getLatitude();
            longitude = latestLocation.getLongitude();
        }

        newEntity = null;
        EntityRequest.viewEntity(contactID, mStartPos, 20, latitude, longitude, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject jsonObj = response.getData();
                    try {
                        newEntity = JsonConverter.json2Object(
                                (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                        entityList.add(newEntity);

                        for(int i=0; i< newEntity.getInfos().size(); i++) {

                            ALLLocationsItem searchItem = new ALLLocationsItem();

                            String tmpLocationName = "";
                            for(int j=0;j<newEntity.getInfos().get(i).getEntityInfoDetails().size();j++)
                            {
                                if (newEntity.getInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                                    tmpLocationName = newEntity.getInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                                }
                            }
                            int count =  newEntity.getInfos().size();
                            if (tmpLocationName == "") {
                                searchItem.locationName = "Location #" + count;
                            }
                            else {
                                searchItem.locationName = tmpLocationName;
                            }
                            float latitudeOfEntity;
                            float longitudeOfEntity;
                            if (newEntity.getInfos().get(i).isAddressConfirmed() == false){
                                latitudeOfEntity = 0;
                                longitudeOfEntity = 0;
                            }else
                            {
                                String str_Latitude = newEntity.getInfos().get(i).getLatitude();
                                String str_Longitude = newEntity.getInfos().get(i).getLongitude();

                                if(str_Latitude == null || str_Longitude == null || str_Latitude.equals("null") || str_Longitude.equals("null")) {
                                    latitudeOfEntity = 0.0f;
                                    longitudeOfEntity = 0.0f;
                                }
                                else {
                                    latitudeOfEntity = Float.parseFloat(newEntity.getInfos().get(i).getLatitude());
                                    longitudeOfEntity = Float.parseFloat(newEntity.getInfos().get(i).getLongitude());
                                }
                            }
                            Location currentOfEntity = new Location("");
                            currentOfEntity.setLatitude(latitudeOfEntity);
                            currentOfEntity.setLongitude(longitudeOfEntity);
                            searchItem.currentOfEntity = currentOfEntity;

                            if (latestLocation == null){
                                searchItem.distanceValue = "No Address";
                                searchItem.isAddress = false;
                            }else {

                                float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                                String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                                String distanceVal = tmp_distance.replace(",", ".");
                                searchItem.distanceValue = distanceVal;
                                searchItem.isAddress = true;
                            }
                            searchItem.infoId = newEntity.getInfos().get(i).getId();
                            locationItems.add(searchItem);
                        }

                        //Collections.sort(locationItems, new SortPlaces());

                        boolean hasMoreData = locationItems.size() != 0;
                        mAdpater.clearAll();
                        mAdpater.addItems(locationItems);
                        mAdpater.notifyDataSetChanged();

                        if (mAdpater.getCount() == 0)
                            mPullListView.setPullRefreshEnabled(false);
                        else
                            mPullListView.setPullRefreshEnabled(true);

                        mPullListView.setHasMoreData(hasMoreData);
                        setLastUpdateTime();
                    } catch (JsonConvertException e) {
                        e.printStackTrace();
                        newEntity = null;
                    }
                }
                // when fail, also let the header/footer hide.
                mPullListView.onPullDownRefreshComplete();
                mPullListView.onPullUpRefreshComplete();

                isLoadingEnd = true;
            }
        }, true);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && requestCode == 1234) {
            isFollowedThisEntity = data.getBooleanExtra("isFollowEntity", false);
            isFavorite = data.getBooleanExtra("isFavorite", false);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
                if (!isLoadingEnd) return;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("isFollowEntity",isFollowedThisEntity);
                returnIntent.putExtra("isFavorite",isFavorite);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }
    }

    private class ALLLocationsItem {
        public String locationName = "";
        public Location currentOfEntity = new Location("");
        public String distanceValue = "No address";
        public int infoId = 0;
        public boolean isAddress = true;

        public ALLLocationsItem() {

        }

    }
    private class ALLLocationsListAdapter extends BaseAdapter{
        private List<ALLLocationsItem> locationsList;
        private Context mContext = null;

        public ALLLocationsListAdapter(Context context) {
            locationsList = new ArrayList<ALLLocationsItem>();
            this.mContext = context;
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

        public void setItem(ALLLocationsItem contactItem)
        {
            this.item = contactItem;
        }
        public ALLLocationsItem getItem(){return this.item;}

        public void refreshView()
        {
            locationInfo.setText(item.locationName);
            distanceInfo.setText(item.distanceValue  + " mi");
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
