package com.videophotofilter.library.android.com;

import java.util.ArrayList;

public class VideoSegment {
	public final int FRMAE_PER_SECOND = 24;
	public int segmentIndex = 0;
	public long durationInMills = 0;
	public int frameCount = 0;
	public int videoWidth = 0;
	public int videoHeight = 0;
	public boolean selected = false;
	public ArrayList<Long> frameMemAddArray;
	public VideoSegment()
	{
		frameMemAddArray = new ArrayList<Long>();
	}
}