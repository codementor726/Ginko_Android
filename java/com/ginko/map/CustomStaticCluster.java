package com.ginko.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.algo.StaticCluster;

/**
 * Created by Alexey Rogovoy (lexapublic@gmail.com) on 20.10.2016.
 */

public class CustomStaticCluster<T extends ClusterItem> extends StaticCluster<T> implements TypedClusterItem
{
	private int type = -1;

	public CustomStaticCluster(int type, LatLng center)
	{
		super(center);
		this.type = type;
	}

	@Override
	public int getType()
	{
		return type;
	}
}
