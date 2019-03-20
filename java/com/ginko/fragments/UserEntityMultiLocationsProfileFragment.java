package com.ginko.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.entity.AllEntityPreviewListActivity;
import com.ginko.activity.entity.EntityProfilePreviewActivity;
import com.ginko.activity.entity.GPSTracker;
import com.ginko.activity.profiles.AllLocationsOfEntityActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.TradeCard;
import com.ginko.api.request.UserRequest;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.EntityProfilePreviewFieldGroupView;
import com.ginko.customview.EntityProfilePreviewLocationMapView;
import com.ginko.customview.ExpandableTextView;
import com.ginko.customview.PersonalProfilePreviewFieldGroupView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.UserEntityProfileVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UserEntityMultiLocationsProfileFragment extends Fragment{

    private View view;
    private LinearLayout containerLayout, spaceLayout, moreContainerLayout;
    private RelativeLayout containerItemLayout, containerItemLayout1;
    private CustomNetworkImageView profilePhoto, imgEntityProfilePhoto, imgEntityProfilePhoto1;
    private TextView txtEntityName;
    private CustomNetworkImageView imgWallpaper;
    private ImageView imgProfileLock, imgProfileFavorite;
    private TextView locationInfo, locationInfo1, countLocations, distanceEntity, distanceEntity1;
    private ImageView imgArrow, imgArrow1;
    private ExpandableTextView txtEntityDescription;
    private TextView txtEntityCategory;

    private UserEntityProfileVO entity;
    private EntityInfoVO entityInfo;
    private ImageLoader imgLoader;

    //2016.9.21 Layout Update for Big Profile Show
    private CustomNetworkImageView tiledProfilePhoto;
    private RelativeLayout hiddenLayout;
    private ImageView imgDimClose;

    private boolean isUICreated = false;
    private boolean isFavorite = false;
    private boolean isFollowThisEntity = false;

    private int userId = 0;
    private int contactID = 0;

    private Location latestLocation = null;

    private ArrayList<ALLLocationsItem> locationItems;

    private GPSTracker gps;
    private double latitude;
    private double longitude;


    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    Handler handler;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserProfileFragment.
     */
    public static UserEntityMultiLocationsProfileFragment newInstance(UserEntityProfileVO _entity, EntityInfoVO _entityInfo) {
        UserEntityMultiLocationsProfileFragment fragment = new UserEntityMultiLocationsProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("entity", _entity);
        args.putSerializable("entityInfo", _entityInfo);
        fragment.setArguments(args);
        return fragment;
    }


    public UserEntityMultiLocationsProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if(this.entity == null)
                this.entity = (UserEntityProfileVO)getArguments().getSerializable("entity");
            if(this.entityInfo == null)
                this.entityInfo = (EntityInfoVO)getArguments().getSerializable("entityInfo");
        }
        Bundle extra = getArguments();
        userId = extra.getInt("isUser");
        contactID = extra.getInt("contactID");
        isFavorite = extra.getBoolean("isFavorite");
        isFollowThisEntity = extra.getBoolean("isFollowedThisEntity");
        isUICreated = false;

        latitude = 0;
        longitude = 0;

        gps = new GPSTracker(getActivity());
        latestLocation = new Location("");

        if (gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            latestLocation.setLongitude(longitude);
            latestLocation.setLatitude(latitude);
        }else{
            gps.showSettingAlert();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        handler = new Handler();

        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 100, 3000); //

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_multilocations_profile_preview, container, false);
        containerLayout = (LinearLayout) view.findViewById(R.id.container);
        spaceLayout = (LinearLayout) view.findViewById(R.id.spaceLayout);
        moreContainerLayout = (LinearLayout) view.findViewById(R.id.moreContainer);
        moreContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EntityRequest.getFollowerTotal(contactID, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject json = response.getData();
                            isFollowThisEntity = json.optBoolean("is_followed", false);

                            Intent entityProfileIntent = new Intent(getActivity(), AllLocationsOfEntityActivity.class);
                            entityProfileIntent.putExtra("entity", entity);
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("isfollowing_entity", isFollowThisEntity);
                            getActivity().startActivityForResult(entityProfileIntent, 1234);
                        } else {
                            Intent entityProfileIntent = new Intent(getActivity(), AllLocationsOfEntityActivity.class);
                            entityProfileIntent.putExtra("entity", entity);
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("isfollowing_entity", false);
                            getActivity().startActivityForResult(entityProfileIntent, 1234);
                        }
                    }
                }, true, false);
            }
        });

        containerItemLayout = (RelativeLayout) view.findViewById(R.id.containerItem);
        containerItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EntityRequest.getFollowerTotal(contactID, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {

                        if (response.isSuccess()) {
                            JSONObject json = response.getData();
                            isFollowThisEntity = json.optBoolean("is_followed", false);
                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                            entityProfileIntent.putExtra("entityJson", entity);
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("infoID", locationItems.get(0).infoId);
                            entityProfileIntent.putExtra("isfollowing_entity", isFollowThisEntity);
                            entityProfileIntent.putExtra("isMultiLocations", true);
                            entityProfileIntent.putExtra("isNoLetter", true);
                            getActivity().startActivityForResult(entityProfileIntent, 1234);
                        } else {
                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                            entityProfileIntent.putExtra("entityJson", entity);
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("infoID", locationItems.get(0).infoId);
                            entityProfileIntent.putExtra("isfollowing_entity", false);
                            entityProfileIntent.putExtra("isMultiLocations", true);
                            entityProfileIntent.putExtra("isNoLetter", true);
                            getActivity().startActivityForResult(entityProfileIntent, 1234);
                        }
                    }
                }, true, false);
            }
        });
        containerItemLayout1 = (RelativeLayout) view.findViewById(R.id.containerItem1);
        containerItemLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EntityRequest.getFollowerTotal(contactID, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject json = response.getData();
                            isFollowThisEntity = json.optBoolean("is_followed", false);
                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                            entityProfileIntent.putExtra("entityJson", entity);
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("infoID", locationItems.get(1).infoId);
                            entityProfileIntent.putExtra("isfollowing_entity", isFollowThisEntity);
                            entityProfileIntent.putExtra("isMultiLocations", true);
                            entityProfileIntent.putExtra("isNoLetter", true);
                            getActivity().startActivityForResult(entityProfileIntent, 1234);
                        } else {
                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                            entityProfileIntent.putExtra("entityJson", entity);
                            entityProfileIntent.putExtra("contactID", contactID);
                            entityProfileIntent.putExtra("isFavorite", isFavorite);
                            entityProfileIntent.putExtra("infoID", locationItems.get(1).infoId);
                            entityProfileIntent.putExtra("isfollowing_entity", false);
                            entityProfileIntent.putExtra("isMultiLocations", true);
                            entityProfileIntent.putExtra("isNoLetter", true);
                            getActivity().startActivityForResult(entityProfileIntent, 1234);
                        }
                    }
                }, true, false);
            }
        });

        //2016.9.21 Update
        tiledProfilePhoto = (CustomNetworkImageView)getActivity().findViewById(R.id.tileProfileImage);
        hiddenLayout = (RelativeLayout)getActivity().findViewById(R.id.hiddenLayout);
        imgDimClose = (ImageView)getActivity().findViewById(R.id.imgDimClose);
        tiledProfilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);

        hiddenLayout.setVisibility(View.GONE);

        profilePhoto =(CustomNetworkImageView)view.findViewById(R.id.imgProfilePhoto);
        profilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);
        imgEntityProfilePhoto = (CustomNetworkImageView) view.findViewById(R.id.imgEntityProfilePhoto);
        imgEntityProfilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);
        imgEntityProfilePhoto1 = (CustomNetworkImageView) view.findViewById(R.id.imgEntityProfilePhoto1);
        imgEntityProfilePhoto1.setDefaultImageResId(R.drawable.entity_profile_preview);

        imgWallpaper = (CustomNetworkImageView)view.findViewById(R.id.imgWallpaper);
        imgWallpaper.setDefaultImageResId(R.drawable.default_wallpaper);

        txtEntityName = (TextView)view.findViewById(R.id.txtEntityName);
        txtEntityName.setText("");

        imgProfileLock = (ImageView)view.findViewById(R.id.imgProfileLock);
        imgProfileFavorite = (ImageView)view.findViewById(R.id.imgProfileFavorite);
        imgProfileFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFavorite) {
                    isFavorite = true;
                    UserRequest.setFavoriteContacts(contactID, "3", new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            imgProfileFavorite.setImageResource(R.drawable.img_favorite);
                        }
                    });
                } else {
                    isFavorite = false;
                    UserRequest.unsetFavoriteContacts(contactID, "3", new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
                        }
                    });
                }
            }
        });

        imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);

        if(userId != 11){
            imgProfileLock.setVisibility(View.VISIBLE);
            imgProfileFavorite.setVisibility(View.GONE);
        }
        else if(userId == 11 && isFollowThisEntity){
            imgProfileLock.setVisibility(View.GONE);
            imgProfileFavorite.setVisibility(View.VISIBLE);
            if(isFavorite)
                imgProfileFavorite.setImageResource(R.drawable.img_favorite);
            else
                imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
        }
        else{
            imgProfileFavorite.setVisibility(View.GONE);
            imgProfileLock.setVisibility(View.GONE);
        }

        txtEntityDescription = (ExpandableTextView)view.findViewById(R.id.txtEntityDescription);
        txtEntityCategory = (TextView)view.findViewById(R.id.txtEntityCategory);
        locationInfo = (TextView) view.findViewById(R.id.locationInfo);
        locationInfo1 = (TextView) view.findViewById(R.id.locationInfo1);


        distanceEntity = (TextView)view.findViewById(R.id.distanceEntity);
        distanceEntity1 = (TextView)view.findViewById(R.id.distanceEntity1);

        imgArrow = (ImageView) view.findViewById(R.id.ImgArrow);
        imgArrow1 = (ImageView) view.findViewById(R.id.ImgArrow1);

        countLocations = (TextView) view.findViewById(R.id.countLocations);


        isUICreated = true;


        init(entity, entityInfo);
        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        if(!gps.canGetLocation())
            timerTask.cancel();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(!gps.canGetLocation())
            timerTask.run();
    }

    private void updateLockButton()
    {
        if(this.entity == null || this.entityInfo == null) return;
        EntityImageVO wallPaperImage = entity.getWallpapaerImage();
        if (this.entity.getPrivilege()>0){
            if(wallPaperImage != null && !wallPaperImage.getUrl().equals(""))
                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
            else
                imgProfileLock.setImageResource(R.drawable.personal_profile_unlocked_white);
        }else{
            if(wallPaperImage != null && !wallPaperImage.getUrl().equals(""))
                imgProfileLock.setImageResource(R.drawable.personal_profile_preview_locked);
            else
                imgProfileLock.setImageResource(R.drawable.personal_profile_locked);
        }
        imgProfileLock.invalidate();
    }

    public void init(UserEntityProfileVO _entity , EntityInfoVO _entityInfo)
    {
        this.entity = _entity;
        this.entityInfo = _entityInfo;

        if(isUICreated == false) return;

        if(this.imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(this.entityInfo == null || this.entityInfo.getEntityInfoDetails().size() < 1) {
            profilePhoto.refreshOriginalBitmap();
            profilePhoto.setImageUrl("", imgLoader);
            profilePhoto.invalidate();

            tiledProfilePhoto.refreshOriginalBitmap();
            tiledProfilePhoto.setImageUrl("", imgLoader);
            tiledProfilePhoto.invalidate();

            imgEntityProfilePhoto.refreshOriginalBitmap();
            imgEntityProfilePhoto.setImageUrl("", imgLoader);
            imgEntityProfilePhoto.invalidate();

            imgEntityProfilePhoto1.refreshOriginalBitmap();
            imgEntityProfilePhoto1.setImageUrl("", imgLoader);
            imgEntityProfilePhoto1.invalidate();

            imgWallpaper.refreshOriginalBitmap();
            imgWallpaper.setImageUrl("", imgLoader);
            imgWallpaper.invalidate();

            txtEntityName.setText("");

            updateLockButton();

            return;
        }

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenLayout.setVisibility(View.VISIBLE);
                fadeInAndShowImage(tiledProfilePhoto);
            }
        });

        imgDimClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenLayout.setVisibility(View.GONE);
                tiledProfilePhoto.setVisibility(View.INVISIBLE);
            }
        });

        hiddenLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (tiledProfilePhoto.getAnimation() != null && !tiledProfilePhoto.getAnimation().hasEnded())
                        return false;

                    hiddenLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });

        profilePhoto.refreshOriginalBitmap();
        profilePhoto.setImageUrl(this.entity.getProfileImage(), imgLoader);
        profilePhoto.invalidate();

        tiledProfilePhoto.refreshOriginalBitmap();
        tiledProfilePhoto.setImageUrl(this.entity.getProfileImage(), imgLoader);
        tiledProfilePhoto.invalidate();

        imgEntityProfilePhoto.refreshOriginalBitmap();
        imgEntityProfilePhoto.setImageUrl(this.entity.getProfileImage(), imgLoader);

        imgEntityProfilePhoto.invalidate();
        imgEntityProfilePhoto1.refreshOriginalBitmap();
        imgEntityProfilePhoto1.setImageUrl(this.entity.getProfileImage(), imgLoader);

        imgEntityProfilePhoto1.invalidate();

        EntityImageVO wallpaperImage = null;
        if((wallpaperImage = entity.getWallpapaerImage()) !=null) {
            imgWallpaper.setImageUrl(wallpaperImage.getUrl() , imgLoader);
        }

        txtEntityName.setText(entity.getName());


        locationItems = new ArrayList<ALLLocationsItem>();
        for(int i=0; i< this.entity.getInfos().size(); i++) {

            ALLLocationsItem searchItem = new ALLLocationsItem();

            String tmpLocationName = "";
            for(int j=0;j<entity.getInfos().get(i).getEntityInfoDetails().size();j++)
            {
                if (entity.getInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                    tmpLocationName = entity.getInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                }
            }
            int count =  this.entity.getInfos().size();
            if (tmpLocationName == "") {
                searchItem.locationName = "Location #" + count;
            }
            else {
                searchItem.locationName = tmpLocationName;
            }
            float latitudeOfEntity;
            float longitudeOfEntity;
            if (entity.getInfos().get(i).isAddressConfirmed() == false){
                latitudeOfEntity = 0.0f;
                longitudeOfEntity = 0.0f;
            }else
            {
                String str_Latitude = entity.getInfos().get(i).getLatitude();
                String str_Longitude = entity.getInfos().get(i).getLongitude();

                if(str_Latitude == null || str_Longitude == null || str_Latitude.equals("null") || str_Longitude.equals("null")) {
                    latitudeOfEntity = 0.0f;
                    longitudeOfEntity = 0.0f;
                }
                else {
                    latitudeOfEntity = Float.parseFloat(entity.getInfos().get(i).getLatitude());
                    longitudeOfEntity = Float.parseFloat(entity.getInfos().get(i).getLongitude());
                }
            }
            Location currentOfEntity = new Location("");
            currentOfEntity.setLatitude(latitudeOfEntity);
            currentOfEntity.setLongitude(longitudeOfEntity);

            if (latestLocation == null){
                searchItem.distanceValue = "No address";
                searchItem.isAddress = false;
            } else if (tmpLocationName == "") {
                searchItem.locationName = "Can't find Location!";
                searchItem.isAddress = false;
                searchItem.distanceValue = "No Address";
            }else{
                float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                String distanceVal = tmp_distance.replace(",", ".");
                searchItem.distanceValue = distanceVal;
                searchItem.isAddress = true;
            }
            searchItem.infoId = entity.getInfos().get(i).getId();
            locationItems.add(searchItem);
        }

        Collections.sort(locationItems, new SortPlaces());


        locationInfo.setText(locationItems.get(0).locationName);
        locationInfo.setTextColor(Color.BLACK);
        imgArrow.setImageResource(R.drawable.location_arrow_black);
        distanceEntity.setTextColor(Color.BLACK);

        if (locationItems.get(0).distanceValue != "No Address")
            distanceEntity.setText(locationItems.get(0).distanceValue + " mi");

        if (locationItems.size() > 1){
            containerItemLayout1.setVisibility(View.VISIBLE);
            locationInfo1.setText(locationItems.get(1).locationName);
            locationInfo1.setTextColor(Color.BLACK);
            imgArrow1.setImageResource(R.drawable.location_arrow_black);
            distanceEntity1.setTextColor(Color.BLACK);

            if (locationItems.get(1).distanceValue != "No Address")
                distanceEntity1.setText(locationItems.get(1).distanceValue + "vmi");
        }else {
            containerItemLayout1.setVisibility(View.GONE);
        }


        if (entity.getInfos().size() > 2){
            spaceLayout.setVisibility(View.VISIBLE);
            moreContainerLayout.setVisibility(View.VISIBLE);
            countLocations.setText("+" + String.valueOf(entity.getInfoTotal() - 2));
        }

        switch (entity.getCategoryId())
        {
            case 0:
                txtEntityCategory.setText("Local Business or Place");
                break;
            case 1:
                txtEntityCategory.setText("Company, Organization or Institution");
                break;
            case 2:
                txtEntityCategory.setText("Brand or Product");
                break;
            case 3:
                txtEntityCategory.setText("Entertainment");
                break;
            case 4:
                txtEntityCategory.setText("Artist, Brand or Public Figure");
                break;

            case 5:
                txtEntityCategory.setText("Cause or Community");
                break;


        }
        if("".equals(entity.getDescription()) || entity.getDescription() == null)
            txtEntityDescription.setVisibility(View.GONE);
        else
            txtEntityDescription.setText(entity.getDescription());

        updateLockButton();
    }

    private void fadeInAndShowImage(final CustomNetworkImageView img)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(600);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }

    public void setIsFollow(boolean set){
        isFollowThisEntity = set;

        if (userId == 11 && isFollowThisEntity)
        {
            imgProfileLock.setVisibility(View.GONE);
            imgProfileFavorite.setVisibility(View.VISIBLE);
            if(isFavorite)
                imgProfileFavorite.setImageResource(R.drawable.img_favorite);
            else
                imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
        } else
        {
            isFavorite = false;
            imgProfileFavorite.setVisibility(View.GONE);
            imgProfileLock.setVisibility(View.GONE);
        }

    }

    public void stopTimer() {
        timerTask.cancel();
    }

    public void setIsFavorite(boolean set){
        isFavorite = set;
        if(isFavorite)
            imgProfileFavorite.setImageResource(R.drawable.img_favorite);
        else
            imgProfileFavorite.setImageResource(R.drawable.img_unfavorite);
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
                        for(int i=0; i< entity.getInfos().size(); i++) {

                            ALLLocationsItem searchItem = new ALLLocationsItem();

                            String tmpLocationName = "";
                            for(int j=0;j<entity.getInfos().get(i).getEntityInfoDetails().size();j++)
                            {
                                if (entity.getInfos().get(i).getEntityInfoDetails().get(j).getFieldName().toString().toLowerCase().contains("address")){
                                    tmpLocationName = entity.getInfos().get(i).getEntityInfoDetails().get(j).getValue().toString();
                                }
                            }
                            int count =  entity.getInfos().size();
                            if (tmpLocationName == "") {
                                searchItem.locationName = "Location #" + count;
                            }
                            else {
                                searchItem.locationName = tmpLocationName;
                            }
                            float latitudeOfEntity;
                            float longitudeOfEntity;
                            if (entity.getInfos().get(i).isAddressConfirmed() == false) {
                                latitudeOfEntity = 0.0f;
                                longitudeOfEntity = 0.0f;
                            }
                            else {
                                String str_Latitude = entity.getInfos().get(i).getLatitude();
                                String str_Longitude = entity.getInfos().get(i).getLongitude();

                                if(str_Latitude == null || str_Longitude == null || str_Latitude.equals("null") || str_Longitude.equals("null")) {
                                    latitudeOfEntity = 0.0f;
                                    longitudeOfEntity = 0.0f;
                                }
                                else {
                                    latitudeOfEntity = Float.parseFloat(entity.getInfos().get(i).getLatitude());
                                    longitudeOfEntity = Float.parseFloat(entity.getInfos().get(i).getLongitude());
                                }
                            }
                            Location currentOfEntity = new Location("");
                            currentOfEntity.setLatitude(latitudeOfEntity);
                            currentOfEntity.setLongitude(longitudeOfEntity);
                            if (latestLocation == null){
                                searchItem.distanceValue = "No Address";
                                searchItem.isAddress = false;
                            }else if (tmpLocationName == "") {
                                searchItem.locationName = "Can't find Location!";
                                searchItem.isAddress = false;
                                searchItem.distanceValue = "No Address";
                            } else
                            {
                                float distanceInMeters = currentOfEntity.distanceTo(latestLocation);
                                String tmp_distance = String.format("%.2f", distanceInMeters/1609);
                                String distanceVal = tmp_distance.replace(",", ".");
                                searchItem.distanceValue = distanceVal;
                                searchItem.isAddress = true;
                            }
                            searchItem.infoId = entity.getInfos().get(i).getId();
                            locationItems.add(searchItem);
                        }

                        Collections.sort(locationItems, new SortPlaces());

                        if (locationItems.get(0).distanceValue != "No Address")
                            distanceEntity.setText(locationItems.get(0).distanceValue + " mi");

                        if(locationItems.size() > 1) {
                            locationInfo1.setText(locationItems.get(1).locationName);
                            locationInfo1.setTextColor(Color.BLACK);
                            imgArrow1.setImageResource(R.drawable.location_arrow_black);

                            if (locationItems.get(1).distanceValue != "No Address")
                                distanceEntity1.setText(locationItems.get(1).distanceValue + " mi");
                        }
                    }
                });
            }
        };
    }

    private class ALLLocationsItem {
        public String locationName = "";
        public String distanceValue = "";
        public int infoId = 0;
        public boolean isAddress = true;
        public ALLLocationsItem() {

        }
    }
    public class SortPlaces implements Comparator<ALLLocationsItem> {
        @Override
        public int compare(final ALLLocationsItem place1, final ALLLocationsItem place2) {
            if (place1.isAddress == false || place2.isAddress == false)
                return 1;
            else {
                try {
                    if (place1.distanceValue.contains(","))
                        place1.distanceValue.replaceAll(",", ".");
                    else if (place2.distanceValue.contains(","))
                        place2.distanceValue.replaceAll(",", ".");

                    return (Float.parseFloat(place1.distanceValue) < Float.parseFloat(place2.distanceValue)) ? -1 : (Float.parseFloat(place1.distanceValue) > Float.parseFloat(place2.distanceValue)) ? 1 : 0;
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                    return 0;
                }
            }
        }
    }
}
