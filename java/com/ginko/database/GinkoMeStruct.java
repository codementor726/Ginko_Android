package com.ginko.database;

import com.ginko.activity.sprout.SproutSearchItem;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by YongJong on 10/25/16.
 */
public class GinkoMeStruct implements Serializable{
    private int Id = 0;
    private int contactOrEntityID = 0;
    private double lat = 0.0d;
    private double lng = 0.0d;
    private String profileImage = "";
    private String entityOrContactName = "";

    private SproutSearchItem sproutItem;

    public GinkoMeStruct()
    {

    }

    public GinkoMeStruct(int id , int contactOrEntityID , double latitude , double longitude , String entityName, String profileImage)
    {
        this.Id = id;
        this.contactOrEntityID = contactOrEntityID;
        this.lat = latitude;
        this.lng = longitude;
        this.entityOrContactName = entityName;
        this.profileImage = profileImage;
    }

    public void update(GinkoMeStruct st)
    {
        this.Id = st.Id;
        this.contactOrEntityID = st.contactOrEntityID;
        this.lat = st.lat;
        this.lng = st.lng;
        this.entityOrContactName = "";
        this.profileImage = st.profileImage;
    }

    public void setId(int id){this.Id = id;}
    public int getId(){return this.Id;}

    public void setContactOrEntityID(int entityId){this.contactOrEntityID = entityId;}
    public int getContactOrEntityID(){return this.contactOrEntityID;}

    public void setEntityOrContactName(String contactName){this.entityOrContactName = contactName;}
    public String getEntityOrContactName(){return  this.entityOrContactName;}

    public void setLat(double latitude) {this.lat = latitude;}
    public double getLat() {return this.lat;}

    public void setLng(double longitude) {this.lng = longitude;}
    public double getLng() {return this.lng;}

    public void setProfileImage(String profile) {this.profileImage = profile;}
    public String getProfileImage() {return this.profileImage;}
}

