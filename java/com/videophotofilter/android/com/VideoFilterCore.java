package com.videophotofilter.android.com;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.videophotofilter.library.android.com.VideoInfo;

public class VideoFilterCore {
	public static int FPS = 14;
	public static final int MAX_SEGMENT_TIME = 30;
	public static final int MIN_SEGMENT_TIME = 2;
	
	public static final int AUDIO_SAMPLERATE_IN_HZ = 44100; 
	
	public static final int VIDEO_WIDTH = 360;
	public static final int VIDEO_HEIGHT = 480;
	

	
	public static VideoInfo getVideoInfoFromFile(Context context , String filePath)
	{
		VideoInfo videoInfo = new VideoInfo();
		File videoFile = new File(filePath);
		if(!videoFile.exists())
			return null;
		
		videoInfo.videoPath = filePath;
		
		MediaPlayer mp = MediaPlayer.create(context, Uri.fromFile(videoFile));
		videoInfo.videoLengthInMills = mp.getDuration();
		videoInfo.videoWidth = mp.getVideoWidth();
		videoInfo.videoHeight = mp.getVideoHeight();
		videoInfo.videoFrameRate = 1;
		mp.release();
		
		return videoInfo;
	}
	
	public Bitmap getVideoFrame(long timeMills)
	{
		Bitmap bitmap = null;
		
		return bitmap;
	}
	
	
	 private String getOutputMediaFileName() {
	    String state = Environment.getExternalStorageState();
	    // Check if external storage is mounted
	    if (!Environment.MEDIA_MOUNTED.equals(state)) {
	        Log.e("VideoFilter", "External storage is not mounted!");
	        return null;
	    }
	
	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DCIM), "TestingCamera2");
	    // Create the storage directory if it does not exist
	    if (!mediaStorageDir.exists()) {
	        if (!mediaStorageDir.mkdirs()) {
	            Log.e("VideoFilter", "Failed to create directory " + mediaStorageDir.getPath()
	                    + " for pictures/video!");
	            return null;
	        }
	    }
	
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String mediaFileName = mediaStorageDir.getPath() + File.separator +
	            "VID_" + timeStamp + ".mp4";
	
	    return mediaFileName;
	}

}
