package com.ginko.activity.sprout;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.ginko.Markers.AbstractMarker;
import com.ginko.common.MyDataUtils;
import com.ginko.map.TypedClusterItem;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONException;
import org.json.JSONObject;

public class SproutSearchItem extends AbstractMarker implements Parcelable, TypedClusterItem {
    public JSONObject jsonObject;
    public int contactType = -1;
    public String distance;
    public int contactOrEntityID = -1;
    public String profile_image = null;
    public String entityOrContactName = "";
    public String foundTime = "";
    public boolean isExchanged = false;
    public boolean isPending = false;
    public double lat = 0.0;
    public double lng = 0.0;
    public int nEnityFollowerCount = 0;
    public String strAddress = "";
    public int nSharingStatus = -1;
    public boolean isFollowed = false;

    private boolean isVisible = true;

    public boolean isGettingAddress = false;

    public boolean isFocused = false;

    public boolean isOnlyEntity = false;

    private boolean isSelected = false;

    public SproutSearchItem(){
        super();
    }
    public SproutSearchItem(Parcel in) {
        super();
        readFromParcel(in);
    }

    public SproutSearchItem(JSONObject jsonObj)
    {
        super();
        this.jsonObject = jsonObj;
        this.contactType = jsonObject.optInt("contact_type" , -1);
        this.distance = jsonObject.optString("distance" , "");
        this.strAddress = "";
        if(contactType == 3)//entity
        {
            this.contactOrEntityID = jsonObject.optInt("entity_id" , -1);
            this.entityOrContactName = jsonObject.optString("name" , "");
            this.nEnityFollowerCount = jsonObject.optInt("follower_total" , 0);
            this.isFollowed = jsonObject.optInt("invite_status" , 0)==0?false:true;
        }
        else//contact
        {
            this.entityOrContactName = jsonObject.optString("first_name" , "") + " "+" "+jsonObject.optString("middle_name" , "")+" "+jsonObject.optString("last_name" , "");
            this.contactOrEntityID = jsonObject.optInt("contact_id" , -1);
            this.isExchanged = jsonObject.optBoolean("exchanged" , false);
            this.isPending = jsonObject.optBoolean("is_pending" , false);
            if(this.isExchanged)
            {
                this.nSharingStatus = jsonObject.optInt("sharing_status", -1);
            }
        }

        this.profile_image = jsonObject.optString("profile_image" , "");
        this.foundTime = MyDataUtils.convertFullMonthString(jsonObject.optString("found_time", ""));
        this.lat = jsonObject.optDouble("latitude" , 0.0d);
        this.lng = jsonObject.optDouble("longitude" , 0.0d);

        this.isVisible = true;
    }

    public boolean isVisible(){return this.isVisible;}
    public void setVisible(boolean visible){this.isVisible = visible;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jsonObject.toString());
        dest.writeInt(contactType);
        dest.writeInt(contactOrEntityID);
        dest.writeString(profile_image);
        dest.writeString(distance);
        dest.writeString(entityOrContactName);
        dest.writeString(foundTime);
        dest.writeByte((byte) (isExchanged ? 1 : 0));
        dest.writeByte((byte) (isPending ? 1 : 0));
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeInt(nEnityFollowerCount);
        dest.writeString(strAddress);
        dest.writeInt(nSharingStatus);
        dest.writeByte((byte) (isFollowed ? 1 : 0));
        dest.writeByte((byte) (isVisible ? 1 : 0));
        dest.writeByte((byte) (isGettingAddress ? 1 : 0));
    }

    public void readFromParcel(Parcel in) {
        try {
            this.jsonObject = new JSONObject(in.readString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.contactType = in.readInt();
        this.contactOrEntityID = in.readInt();
        this.profile_image = in.readString();
        this.distance = in.readString();
        this.entityOrContactName = in.readString();
        this.foundTime = in.readString();
        this.isExchanged = in.readByte() != 0;
        this.isPending = in.readByte() != 0;
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.nEnityFollowerCount = in.readInt();
        this.strAddress = in.readString();
        this.nSharingStatus = in.readInt();
        this.isFollowed = in.readByte() != 0;
        this.isVisible = in.readByte() != 0;
        this.isGettingAddress = in.readByte() != 0;
    }

    //this is parceable object creator.
    //this is just needed to be implemented
    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public SproutSearchItem createFromParcel(Parcel in) {
            return new SproutSearchItem(in);
        }

        @Override
        public SproutSearchItem[] newArray(int size) {
            // TODO Auto-generated method stub
            return new SproutSearchItem[size];
        }
    };

    @Override
    public LatLng getPosition() {
        return new LatLng(lat , lng);
    }

    @Override
    public void setZoom(float zoom) {

    }

    @Override
    public BitmapDescriptor getIcon() {
        return null;
    }

    @Override
    public boolean isOnlyEntity() {
        return isOnlyEntity;
    }

    @Override
    public int getType() {
        return contactType;
    }

    public void setSelected(boolean selected){this.isSelected = selected;}
    public boolean isSelected(){return this.isSelected;}
}
