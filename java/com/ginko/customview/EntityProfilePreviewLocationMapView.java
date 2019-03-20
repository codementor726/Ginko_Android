package com.ginko.customview;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoDetailVO;

import java.util.ArrayList;
import java.util.Locale;

public class EntityProfilePreviewLocationMapView extends LinearLayout{
    private Context mContext;

    private CustomNetworkImageView imgLocationMap;

    private Double lat , lng;

    private ArrayList<EntityProfilePreviewFieldItemView> itemViewList;

    public EntityProfilePreviewLocationMapView(Context context ,Double lat , Double lng)
    {
        this(context);
        this.lat = lat;
        this.lng = lng;

        init(context);
    }


    public EntityProfilePreviewLocationMapView(Context context) {
        this(context, null);
    }

    public EntityProfilePreviewLocationMapView(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public EntityProfilePreviewLocationMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.entity_profile_preview_location_mapview, this);

        imgLocationMap = (CustomNetworkImageView)findViewById(R.id.imgLocationMap);

        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EntityProfilePreviewLocationMapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context)
    {
        int width = 640; int height = 241;
        ImageLoader imgLoader = MyApp.getInstance().getImageLoader();
        if(lat != null && lng != null) {
            imgLocationMap.setVisibility(View.VISIBLE);
            imgLocationMap.setImageUrl(getMapUrl(lat, lng, width, height), imgLoader);
            imgLocationMap.invalidate();
        }
    }
    public static String getMapUrl(Double lat, Double lon, int width, int height) {
        final String coordPair = lat + "," + lon;
        return "http://maps.googleapis.com/maps/api/staticmap?"
                + "&zoom=12"
                + "&size=" + width + "x" + height
                + "&maptype=roadmap&sensor=true"
                + "&center=" + coordPair
                + "&markers=color:red|" + coordPair;
    }
}
