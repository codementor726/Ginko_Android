package com.ginko.Markers;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by lexap on 18.10.2016.
 */


public abstract class AbstractMarker implements ClusterItem  {
    public static final int ZOOM0_SIZE = 30;
    public static final int ZOOM3_SIZE = 40;
    public static final int ZOOM5_SIZE = 55;
    public static final int ZOOM7_SIZE = 70;
    public static final int ZOOM10_SIZE = 80;
    public static final int ZOOM13_SIZE = 90;
    public static final int ZOOM15_SIZE = 100;
    public static final int TYPE_BLURED = 1;
    public static final int TYPE_STARBUCKS = 2;

    protected double latitude;
    protected double longitude;
    protected String profileImage;
    protected Bitmap myBitmap = null;
    protected String contactName = "";
    protected int contactId = -1;

    protected MarkerOptions marker;

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    protected AbstractMarker() {

    }

    public MarkerOptions getMarker() {
        return marker;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }

    public void setProfileImage(String profileImage) {this.profileImage = profileImage;}
    public String getProfileImage() {return profileImage;}

    public void setMyBitmap(Bitmap bitmap) {this.myBitmap = bitmap;}
    public Bitmap getMyBitmap() {return myBitmap;}

    public abstract void setZoom(float zoom);
    public abstract BitmapDescriptor getIcon();

}