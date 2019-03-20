package com.ginko.activity.sprout;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;

import com.ginko.api.request.GeoLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GetSproutAddressFromLatlngTask extends Thread
{
    private double lat , lng;
    private SproutSearchItem sproutDetectedItem;
    private Context mContext;
    private BaseAdapter adapter;
    private Geocoder geocoder;

    public GetSproutAddressFromLatlngTask(Context context, SproutSearchItem item, BaseAdapter adapter)
    {
        mContext = context;
        sproutDetectedItem = item;
        this.adapter = adapter;
        this.lat = sproutDetectedItem.lat;
        this.lng = sproutDetectedItem.lng;
        sproutDetectedItem.isGettingAddress = true;
    }

    @Override
    public void run() {
        /*ArrayList<String> addresses = GeoLibrary.getAddressListFromLatLngLimitLineIndex(lat, lng, 20, 3);
        for(int i=0;i<addresses.size();i++)
        {
            String address = addresses.get(i);
            if(address.equals("")) continue;

            sproutDetectedItem.strAddress = address;
            break;
        }*/
        List<Address> addresses;
        String fullAddress = "";
        if (mContext != null)
            geocoder = new Geocoder(mContext, Locale.getDefault());
        if (geocoder != null) {
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String address = addresses.get(0).getAddressLine(0);
                fullAddress = String.format("%s, %s, %s", city, state, address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sproutDetectedItem.strAddress = fullAddress;

        sproutDetectedItem.isGettingAddress = false;

        if (mContext != null)
        {
            Handler handler = new Handler(mContext.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    try
                    {
                        if(adapter != null)
                            adapter.notifyDataSetChanged();
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            handler.sendEmptyMessage(0);
        }
    }
}