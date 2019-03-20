package com.ginko.api.request;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.ginko.ginko.MyApp;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeoLibrary {

    public static String getAddressFromLatLng(double latitude , double longitude , int addressMaxResultCount)
    {
        Context context = MyApp.getContext();
        Locale currentLocale = context.getResources().getConfiguration().locale;
        Geocoder geoCoder = new Geocoder(context , currentLocale);
        String strAdress = "";

        try
        {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, addressMaxResultCount);
            if(addresses!=null && addresses.size()>0)
            {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnAddress = new StringBuilder();
                for(int i=0;i<returnedAddress.getMaxAddressLineIndex();i++)
                {
                    if(returnedAddress.getAddressLine(i).equals("") == false)
                        strReturnAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                strAdress = strReturnAddress.toString();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            strAdress = "";
        }
        return strAdress;
    }
    public static ArrayList<String> getAddressListFromLatLng(double latitude , double longitude , int addressMaxResultCount)
    {
        ArrayList<String> addressList = new ArrayList<String>();
        Context context = MyApp.getContext();
        Locale currentLocale = context.getResources().getConfiguration().locale;
        Geocoder geoCoder = new Geocoder(context , currentLocale);

        try
        {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, addressMaxResultCount);
            if(addresses!=null && addresses.size()>0)
            {
                for(int j=0;j<addresses.size();j++)
                {
                    String strAdress = "";
                    Address returnedAddress = addresses.get(j);
                    StringBuilder strReturnAddress = new StringBuilder();
                    for(int i=0;i<returnedAddress.getMaxAddressLineIndex();i++)
                    {
                        if(returnedAddress.getAddressLine(i).equals("") == false)
                            strReturnAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                    }
                    strAdress = strReturnAddress.toString();
                    addressList.add(strAdress);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return addressList;
    }
    public static ArrayList<String> getAddressListFromLatLngLimitLineIndex(double latitude , double longitude , int addressMaxResultCount , int maxLineIndex)
    {
        ArrayList<String> addressList = new ArrayList<String>();
        Context context = MyApp.getContext();
        Locale currentLocale = context.getResources().getConfiguration().locale;
        Geocoder geoCoder = new Geocoder(context , currentLocale);

        try
        {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, addressMaxResultCount);
            if(addresses!=null && addresses.size()>0)
            {
                for(int j=0;j<addresses.size();j++)
                {
                    String strAdress = "";
                    Address returnedAddress = addresses.get(j);

                    StringBuilder strReturnAddress = new StringBuilder();
                    /*int startIndex = 0;
                    if(returnedAddress.getMaxAddressLineIndex() >= maxLineIndex) {
                        startIndex = returnedAddress.getMaxAddressLineIndex() - maxLineIndex;
                    }

                    //get address from the last line
                    for(int i=startIndex;i<returnedAddress.getMaxAddressLineIndex();i++)
                    {
                        if(returnedAddress.getAddressLine(i).equals("") == false)
                            strReturnAddress.append(returnedAddress.getAddressLine(i)).append(",");
                    }*/
                    if(returnedAddress.getLocality() != null)
                        strReturnAddress.append(returnedAddress.getLocality()+", ");//city
                    String state = returnedAddress.getAdminArea();
                    if(state!=null) {
                        if (state.length() > 2)
                            state = state.substring(0, 2);
                        state = state.toUpperCase();
                        strReturnAddress.append(state + ", ");//state
                    }
                    strReturnAddress.append(returnedAddress.getCountryName());//state
                    strAdress = strReturnAddress.toString();
                    if(strAdress.endsWith(", "))
                        strAdress = strAdress.substring(0 , strAdress.length()-1);
                    addressList.add(strAdress);
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return addressList;
    }
    public static LatLng getLatLongFromAddress(String youraddress) {
        try{
            String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                    URLEncoder.encode(youraddress,"utf-8") + "&sensor=false";
            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());

                double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);
                return new LatLng(lat , lng);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return null;
    }


}
