package com.ginko.activity.im;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.api.request.GeoLibrary;
import com.ginko.common.Uitils;
import com.ginko.customview.ProgressHUD;
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

public class LocationMapViewerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mapView = null;
    private double dLat;
    private double dLong;
    private LatLng currentLoc;
    private String strAddress = "";

    private int mapHeight = 0 , mapWidth = 0;

    private Handler mHandler = new Handler();

    private ProgressHUD progressHUD;

    private TextView txtLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_message_viewer);

        ImageButton btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(this);

        //mapView = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        SupportMapFragment mapFragment = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        mapWidth = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getView().getMeasuredWidth();
        mapHeight = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getView().getMeasuredHeight();

        Intent intent = this.getIntent();
        dLat = intent.getDoubleExtra("lat" , 0.0d);
        dLong = intent.getDoubleExtra("long", 0.0d);

        try {
            strAddress = intent.getStringExtra("address");
        }catch(Exception e)
        {
            strAddress = "";
        }

        txtLocation = (TextView)findViewById(R.id.txtLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        if(strAddress!=null && !strAddress.equals("")) {
            txtLocation.setText(strAddress);
            progressHUD = ProgressHUD.show(LocationMapViewerActivity.this, "Finding location..." , true , false , null);
            new Thread(){
                public void run()
                {
                    currentLoc = GeoLibrary.getLatLongFromAddress(strAddress);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(progressHUD != null)
                                progressHUD.dismiss();
                            if(currentLoc != null)
                                moveToLocation();
                            else
                            {
                                Uitils.alert(LocationMapViewerActivity.this , "Couldn't find the location.");
                            }
                        }
                    });
                }
            }.start();
        }
        else {
            txtLocation.setText(getResources().getString(R.string.str_chat_location));

            System.out.println("----"+String.valueOf(dLat)+" , "+String.valueOf(dLong)+"-----");

            currentLoc = new LatLng(dLat , dLong);

            moveToLocation();
        }
    }

    private void moveToLocation()
    {
        if(currentLoc != null)
        {
            /*mapView.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

             @Override
             public boolean onMarkerClick(Marker clickedMarker) {
                 // TODO Auto-generated method stub
                 mapWidth = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getView().getMeasuredWidth();
                 mapHeight = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getView().getMeasuredHeight();

                 if(clickedMarker!=null && clickedMarker.getSnippet()!= null && clickedMarker.getSnippet().equals("currentposmarker"))
                 {
                     LatLng loc = CheckInFeedsActivity.this.currentPosMarker.getPosition();
                     moveMapToPositon(loc.latitude , loc.longitude , mapView.getCameraPosition().zoom);
                     return true;
                 }

                 LatLng currentMarkerLoc = clickedMarker.getPosition();
                 //convert latlng to pixel point
                 Projection projection = mapView.getProjection();
                 Point ptCurrentMarkerLoc = projection.toScreenLocation(currentMarkerLoc);
                 ptCurrentMarkerLoc.set(ptCurrentMarkerLoc.x, mapHeight/2-(mapHeight-mapHeight/6-ptCurrentMarkerLoc.y));
                 LatLng newMarkerLoc = projection.fromScreenLocation(ptCurrentMarkerLoc);
                 if(CheckInFeedsActivity.this.marker!=null)
                 {
                     CheckInFeedsActivity.this.marker.hideInfoWindow();
                     if(CheckInFeedsActivity.this.marker.equals(clickedMarker))
                     {
                         CheckInFeedsActivity.this.marker = null;
                         return true;
                     }
                 }
                 clickedMarker.showInfoWindow();
                 CheckInFeedsActivity.this.marker = clickedMarker;

                 moveMapToPositon(newMarkerLoc.latitude , newMarkerLoc.longitude , mapView.getCameraPosition().zoom);
                 return true;
             }}
        );*/

            //Move to current pos position
            mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17.0f));

            Bitmap markerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin_marker);
            final Marker pinMarker = mapView.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
            pinMarker.setAnchor(0.5f , 1.0f);
        }
    }

    private void moveMapToPositon(double x, double y , float zoom)
    {
        LatLng loc = new LatLng(x , y);
        CameraPosition cp = new CameraPosition.Builder().target(loc).zoom(zoom).build();
        mapView.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
        }
    }
}
