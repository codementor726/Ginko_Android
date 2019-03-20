package com.videophotofilter.library.android.com;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

public class FileManager {

	public static final String APP_NAME = "VideoFilter";
	
	// check if external storage is available for read and write
	public static boolean isExternalStorageWritable()
	{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			return true;
		}
		return false;
	}
	public static boolean isExternalStorageReadable()
	{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
		   Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState()))
		{
			return true;
		}
		return false;
	}
	
	public static boolean  isExternalStorageAvailable()
	{
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		
		if(Environment.MEDIA_MOUNTED.equals(state))
		{
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else
		{
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		return mExternalStorageAvailable;
	}
	
	//internal storage
	public static String getAppPrivateFilePath(String fileName , Context context)
	{
		ContextWrapper contextWrapper = new ContextWrapper(context);
		File directory = contextWrapper.getDir(APP_NAME, Context.MODE_PRIVATE);
		File privateFile = new File(directory , fileName);
		
		return privateFile.getAbsolutePath();
	}
	
	public static void writeFile(Context ctx, String fileName, boolean bAppend , byte[] bytesData)
	{
		File file = new File(getAppPrivateFilePath(fileName , ctx));
		try
		{
			FileOutputStream fos = new FileOutputStream(file , bAppend);
			fos.write(bytesData);
			fos.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
