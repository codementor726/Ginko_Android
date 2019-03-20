package com.videophotofilter.library.android.com;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ginko.ginko.MyApp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ImageUtil {
	
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;
    public static final int MEDIA_TYPE_FILE = 4;
    
    
	public static Bitmap rotate(Bitmap bitmap, int degree) {
	    int w = bitmap.getWidth();
	    int h = bitmap.getHeight();

	    Matrix mtx = new Matrix();
	    mtx.postRotate(degree);

	    return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

    public static Bitmap rotateCropSquareImage(Bitmap srcBmp , int degree)
    {
        Matrix mtx = new Matrix();
        mtx.postRotate(degree);
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight() ,
                    mtx ,
                    true
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth(),
                    mtx ,
                    true
            );
        }
        return dstBmp;
    }

	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) 
	{
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	public static Bitmap decodeSampledBitmapFromImageFile(String filePath,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(filePath , options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(filePath , options);
	}
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	
	//output captured or recorded media file
	public static File getOutputMediaFile(final int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = null;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), MyApp.APP_NAME);
		}
		else if(type == MEDIA_TYPE_VIDEO)
		{
			mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), MyApp.APP_NAME);
		}
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("VideoFilterApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    //output captured or recorded media file
    public static File getOutputMediaFile(final int type , String fileName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = null;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), MyApp.APP_NAME);
        }
        else if(type == MEDIA_TYPE_VIDEO)
        {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES), MyApp.APP_NAME);
        }
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("VideoFilterApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + fileName);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + fileName);
        } else {
            return null;
        }

        return mediaFile;
    }

	public static String getRealPathFromURI(Context context, Uri contentUri , int type) {
	  Cursor cursor = null;
	  try { 
	    String[] proj = { MediaStore.Images.Media.DATA };
	    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
	    int column_index = 0;
	    switch(type)
	    {
	    case MEDIA_TYPE_IMAGE:
	    	column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    	break;
	    case MEDIA_TYPE_VIDEO:
	    	column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
	    	break;
	    case MEDIA_TYPE_AUDIO:
	    	column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
	    	break;
	    case MEDIA_TYPE_FILE:
	    	column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
	    	break;
	    	
	    }
	    
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	  } finally {
	    if (cursor != null) {
	      cursor.close();
	    }
	  }
	}

    public static Uri getUriFromPath(Context context ,String path){
        //String fileName= "file:///sdcard/DCIM/Camera/2013_07_07_12345.jpg";
        Uri fileUri = Uri.parse( path );
        String filePath = fileUri.getPath();
        Cursor cursor = context.getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '" + filePath + "'", null, null );
        cursor.moveToNext();
        int id = cursor.getInt( cursor.getColumnIndex( "_id" ) );
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        return uri;
    }
}
