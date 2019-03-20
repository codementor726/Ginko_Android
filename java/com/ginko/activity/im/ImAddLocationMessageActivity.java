package com.ginko.activity.im;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.GeoLibrary;
import com.ginko.api.request.Transfer;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ImAddLocationMessageActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private LinearLayout activityRootView;
    private RelativeLayout headerlayout;
    private ImageButton btnPrev ,  btnConfirm;
    private EditText edtSearchLocation;
    private ImageView btnCancelSearch;
    private ListView searchedLocationListView;
    private TextView txtLocation;
    private Button btnCancel;

    private ProgressBar progressBar;
    private GoogleMap mapView = null;


    private ArrayList<LocationInfo> searchedLocationItems = new ArrayList<LocationInfo>();
    private AddressListAdapter addressAdapter;

    private SearchLocationTask searchLocationTask = null;
    private GetAddressFromLatlngTask getAddressFromLatlngTask = null;


    private double dCurrentLat;
    private double dCurrentLong;
    private LatLng currentLoc;
    private Marker currentPosMarker = null;

    private boolean isKeyboardVisible = false;
    private boolean isCreate = false;

    public class LocationInfo
    {
        public LatLng loc;
        public String strRealAddress = "";
        public String strPlaceName = "";
        public String strVicinity = "";
        public boolean bSelected = false;
        public LocationInfo()
        {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_message);

        Intent intent = this.getIntent();
        dCurrentLat = intent.getDoubleExtra("lat" , 0.0d);
        dCurrentLong = intent.getDoubleExtra("long" , 0.0d);
        this.isCreate = intent.getBooleanExtra("isCreate", false);

        System.out.println("---Current Location(" + String.valueOf(dCurrentLat) + " , " + String.valueOf(dCurrentLong) + ")----");

        getUIObjects();
    }

    private void getUIObjects()
    {
        activityRootView = (LinearLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        edtSearchLocation.setCursorVisible(true);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        edtSearchLocation.setCursorVisible(false);
                    }
                }
            }
        });


        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancelSearch); btnCancel.setVisibility(View.GONE);
        txtLocation = (TextView)findViewById(R.id.txtLocation);

        progressBar = (ProgressBar)findViewById(R.id.progress);

        edtSearchLocation = (EditText)findViewById(R.id.edtSearchLocation);
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearchLocation.setText("");
                btnCancelSearch.setVisibility(View.GONE);

                //clear list when cancel
                /*try {
                    searchedLocationItems.clear();
                    addressAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                //Hide soft keyboard
                //InputMethodManager imm = (InputMethodManager)MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(edtSearchLocation.getWindowToken(), 0);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearchLocation.setText("");
                btnCancelSearch.setVisibility(View.GONE);
                activityRootView.setFocusableInTouchMode(true);
                activityRootView.requestFocus();
                //clear list when cancel
                /*
                try {
                    searchedLocationItems.clear();
                    addressAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                //Hide soft keyboard
                InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtSearchLocation.getWindowToken(), 0);
            }
        });
        edtSearchLocation.addTextChangedListener(new TextWatcher() {

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
                                                             btnCancelSearch.setVisibility(View.VISIBLE);
                                                         else
                                                             btnCancelSearch.setVisibility(View.GONE);
                                                     }

                                                     @Override
                                                     public void afterTextChanged(Editable s) {
                                                         // TODO Auto-generated method stub
                                                     }
                                                 }
        );

        edtSearchLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (btnCancel.getVisibility() == View.GONE) {
                        btnCancel.setVisibility(View.VISIBLE);
                    }
                    edtSearchLocation.setCursorVisible(true);
                } else {
                    if (btnCancel.getVisibility() == View.VISIBLE) {
                        btnCancel.setVisibility(View.GONE);
                    }
                    edtSearchLocation.setCursorVisible(false);
                    addressAdapter.notifyDataSetChanged();
                }
            }
        });
        searchedLocationListView = (ListView)findViewById(R.id.searchedLocationListview);

        addressAdapter = new AddressListAdapter(this);

        searchedLocationListView.setAdapter(addressAdapter);
        searchedLocationListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {
                // TODO Auto-generated method stub
                for(int i =0;i<searchedLocationItems.size();i++)
                {
                    searchedLocationItems.get(i).bSelected = false;
                }
                searchedLocationItems.get(index).bSelected = true;
                currentLoc = searchedLocationItems.get(index).loc;
                setMarkerOnMap(currentLoc , false);
                try
                {
                    addressAdapter.notifyDataSetChanged();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }}
        );

        edtSearchLocation = (EditText)findViewById(R.id.edtSearchLocation);
        edtSearchLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearchLocation.getWindowToken(), 0);
                    edtSearchLocation.setCursorVisible(false);

                    //perform search function
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("location", String.valueOf(currentLoc.latitude) + "," + String.valueOf(currentLoc.longitude));
                    params.put("radius", "10000");
                    //params.put("types",type);
                    params.put("sensor", "true");
                    params.put("key", ImAddLocationMessageActivity.this.getResources().getString(R.string.google_map_server_key));
                    try {
                        if (searchLocationTask == null) {
                            searchLocationTask = new SearchLocationTask(ImAddLocationMessageActivity.this, edtSearchLocation.getText().toString().trim());
                            if (Build.VERSION.SDK_INT >= 11)
                                searchLocationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            else
                                searchLocationTask.execute();
                        } else {
                            if (searchLocationTask.getStatus() == AsyncTask.Status.PENDING) {
                                searchLocationTask.cancel(true);
                                searchLocationTask = new SearchLocationTask(ImAddLocationMessageActivity.this, edtSearchLocation.getText().toString().trim());
                                if (Build.VERSION.SDK_INT >= 11)
                                    searchLocationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                else
                                    searchLocationTask.execute();
                            } else if (searchLocationTask.getStatus() == AsyncTask.Status.RUNNING) {
                                return true;
                            } else if (searchLocationTask.getStatus() == AsyncTask.Status.FINISHED) {
                                searchLocationTask = new SearchLocationTask(ImAddLocationMessageActivity.this, edtSearchLocation.getText().toString().trim());
                                if (Build.VERSION.SDK_INT >= 11)
                                    searchLocationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                else
                                    searchLocationTask.execute();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });

        currentLoc = new LatLng(dCurrentLat , dCurrentLong);

        if (this.isCreate)
        {
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_back_nav_white);
            btnConfirm.setImageResource(R.drawable.part_a_btn_check_nav_wh);
            txtLocation.setTextColor(getResources().getColor(R.color.top_title_text_color));
        }

        //---------Show Map----------------
        //mapView = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        SupportMapFragment mapFragment = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
    }

    private void setMarkerOnMap(LatLng latlng , boolean bGetNewPlaces)
    {
        final Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_marker);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        currentLoc = latlng;

        mapView.clear();

        mapView.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        currentPosMarker = mapView.addMarker(markerOptions);
        currentPosMarker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
        currentPosMarker.setAnchor(0.5f , 1.0f);

        moveMapToPositon(latlng , mapView.getCameraPosition().zoom);

        if(bGetNewPlaces) {
            try {
                if (getAddressFromLatlngTask == null) {
                    getAddressFromLatlngTask = new GetAddressFromLatlngTask(ImAddLocationMessageActivity.this, latlng.latitude, latlng.longitude);
                    if (Build.VERSION.SDK_INT >= 11)
                        getAddressFromLatlngTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        getAddressFromLatlngTask.execute();
                } else {
                    if (getAddressFromLatlngTask.getStatus() == AsyncTask.Status.PENDING) {
                        getAddressFromLatlngTask.cancel(true);
                        getAddressFromLatlngTask = new GetAddressFromLatlngTask(ImAddLocationMessageActivity.this, latlng.latitude, latlng.longitude);
                        if (Build.VERSION.SDK_INT >= 11)
                            getAddressFromLatlngTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            getAddressFromLatlngTask.execute();

                    } else if (getAddressFromLatlngTask.getStatus() == AsyncTask.Status.RUNNING) {
                        return;
                    } else if (getAddressFromLatlngTask.getStatus() == AsyncTask.Status.FINISHED) {
                        getAddressFromLatlngTask = new GetAddressFromLatlngTask(ImAddLocationMessageActivity.this, latlng.latitude, latlng.longitude);
                        if (Build.VERSION.SDK_INT >= 11)
                            getAddressFromLatlngTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            getAddressFromLatlngTask.execute();

                    }
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    }
    private void moveMapToPositon(LatLng loc , float zoom)
    {
        CameraPosition cp = new CameraPosition.Builder().target(loc).zoom(zoom).build();
        mapView.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        mapView.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                setMarkerOnMap(latLng , true);
            }
        });
        setMarkerOnMap(currentLoc , true);
        mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15.0f));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btnConfirm:
                Intent intent = new Intent();
                intent.putExtra("lat" , currentLoc.latitude);
                intent.putExtra("long" , currentLoc.longitude);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private class SearchLocationTask extends AsyncTask<Void , Void , Void>
    {
        private Context mContext;
        //private HashMap<String , String> params;
        private String searchAddr;
        public SearchLocationTask(Context context , String  strSearchAddr)
        {
            this.mContext = context;
            this.searchAddr = strSearchAddr;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            try
            {
                while(searchedLocationItems.size()>0)
                {
                    searchedLocationItems.remove(0);
                }
                addressAdapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... param) {
            // TODO Auto-generated method stub
            /*String strResult = Transfer.GetHttpSendResponse("https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                    , params);
            if(strResult!=null && strResult.compareTo("")!=0)
            {
                System.out.println(strResult);
                try{
                    JSONObject jj = new JSONObject(strResult);
                    JSONArray jPlaces = jj.getJSONArray("results");
                    double lat, lon;
                    for (int i = 0; i < jPlaces.length(); i++) {
                        try{
                            LocationInfo location = new LocationInfo();
                            JSONObject placeObject = jPlaces.getJSONObject(i);
                            if(!placeObject.isNull("name"))
                            {
                                location.strPlaceName = placeObject.getString("name");
                            }
                            if(!placeObject.isNull("formatted_address"))
                            {
                                location.strRealAddress = placeObject.getString("formatted_address");
                                System.out.println("Real Addres = "+location.strRealAddress);
                            }
                            if(!placeObject.isNull("vicinity"))
                            {
                                location.strVicinity = placeObject.getString("vicinity");
                                location.strRealAddress = placeObject.getString("vicinity");
                                System.out.println("---Real Addres = "+location.strRealAddress);
                            }
                            try
                            {
                                lat = Double.valueOf(placeObject.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                            }catch(Exception e){e.printStackTrace(); lat = 0;}
                            try
                            {
                                lon = Double.valueOf(placeObject.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                            }catch(Exception e){e.printStackTrace();lon = 0;}

                            if(location.strRealAddress.equals("") || location.strPlaceName.equals("")) continue;
                            location.loc = new LatLng(lat , lon);
                            if(i == 0)//select the first item as default
                                location.bSelected = true;
                            else
                                location.bSelected = false;
                            searchedLocationItems.add(location);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }*/
            /* get latitude and longitude from the adderress */

            Geocoder geoCoder = new Geocoder(ImAddLocationMessageActivity.this , Locale.ENGLISH);
            try {
                List<Address> addresses = geoCoder.getFromLocationName(searchAddr, 10);
                if (addresses != null && addresses.size() > 0) {
                    boolean isFirstItem = true;
                    for(Address addr : addresses)
                    {
                        LocationInfo location = new LocationInfo();
                        location.strPlaceName = "";
                        for(int i=0;i<addr.getMaxAddressLineIndex();i++)
                        {
                            location.strPlaceName += addr.getAddressLine(i)+" ";
                        }

                        location.loc =   new LatLng(addr.getLatitude() , addr.getLongitude());
                        if(isFirstItem)//select the first item as default
                        {
                            location.bSelected = true;
                            isFirstItem = false;
                        }
                        else
                            location.bSelected = false;
                        searchedLocationItems.add(location);
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            try
            {
                addressAdapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }

            if(searchedLocationItems.size()>0) {
                searchedLocationItems.get(0).bSelected = true;
                currentLoc = searchedLocationItems.get(0).loc;
                setMarkerOnMap(currentLoc, false);
                try
                {
                    addressAdapter.notifyDataSetChanged();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }


        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            progressBar.setVisibility(View.GONE);
        }

    }
    private class GetAddressFromLatlngTask extends AsyncTask<Void ,Void , Void>
    {
        private Context mContext;
        private double lat , lng;
        public GetAddressFromLatlngTask(Context context , double lat , double lng)
        {
            this.mContext = context;
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            try
            {
                while(searchedLocationItems.size()>0)
                {
                    searchedLocationItems.remove(0);
                }
            }catch(Exception e){e.printStackTrace();}
            try
            {
                addressAdapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            ArrayList<String> addresses = GeoLibrary.getAddressListFromLatLng(lat, lng, 20);
            for(int i=0;i<addresses.size();i++)
            {
                LocationInfo item = new LocationInfo();
                item.strRealAddress = addresses.get(i);
                item.strPlaceName = addresses.get(i);
                item.loc = new LatLng(lat , lng);
                if(item.strRealAddress.equals("")) continue;
                if(i==0)
                    item.bSelected = true;
                else
                    item.bSelected = false;
                searchedLocationItems.add(item);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try
            {
                addressAdapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled(Void result) {
            // TODO Auto-generated method stub
            super.onCancelled(result);
            try
            {
                addressAdapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private class AddressListAdapter extends BaseAdapter
    {
        private Context mContext;
        private LayoutInflater inflater;
        public AddressListAdapter(Context context)
        {
            this.mContext = context;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return searchedLocationItems == null ? 0 : searchedLocationItems.size();
        }
        @Override
        public Object getItem(int index) {
            // TODO Auto-generated method stub
            return searchedLocationItems.get(index);
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            LocationInfo item = searchedLocationItems.get(position);
            if(convertView == null)
            {
                convertView = inflater.inflate(R.layout.add_location_list_item, null);
                holder = new ViewHolder();
                holder.txtAddress = (TextView)convertView.findViewById(R.id.txtAddress);
                holder.imgCheck = (ImageView)convertView.findViewById(R.id.imgCheck);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.txtAddress.setText(item.strPlaceName);
            if(item.bSelected)
            {
                holder.imgCheck.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.imgCheck.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }
    }
    private class ViewHolder
    {
        TextView txtAddress;
        ImageView imgCheck;
    }
}
