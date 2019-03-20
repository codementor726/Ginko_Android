package com.ginko.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.ginko.Markers.AbstractMarker;

/**
 * Created by lexap on 18.10.2016.
 */

public class MapRenderer extends CustomDefaultClusterRenderer<AbstractMarker> {


    public MapRenderer(Context context, GoogleMap map, ClusterManager<AbstractMarker> clusterManager) {
        super(context, map, clusterManager);
    }

	@Override
	protected void onBeforeClusterItemRendered(AbstractMarker item, MarkerOptions markerOptions)
	{
		//markerOptions.icon(item.getIcon());

	}
}
