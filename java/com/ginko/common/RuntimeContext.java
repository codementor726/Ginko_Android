package com.ginko.common;

import android.os.Environment;

import com.ginko.ginko.MyApp;
import com.ginko.vo.UserLoginVO;

import java.io.File;

public class RuntimeContext {
	public static final String WORK_FOLDER = "/ginko";
	public static final String TEMP_FOLDER = WORK_FOLDER + "/temp";
	public static final String CACHE_DIR = WORK_FOLDER + "/imgcache";
	public static final String DATA_CACHE_DIR = WORK_FOLDER + "/datacache";
    public static final String LOG_FOLDER= WORK_FOLDER  + "/logs";
    public static final String CRASH_LOG_FOLDER = LOG_FOLDER  + "/crash";
    public static final String DATA_FOLDER = WORK_FOLDER  + "/_data";

    private static String sessionId;
	
	private static UserLoginVO user;

	public static File getCacheFolder() {
		File f = new File(getSDPath() + CACHE_DIR);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	public static File getTempFolder() {
		File f = new File(getSDPath() + TEMP_FOLDER);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	public static String getSessionId() {
		return sessionId;
	}

	public static void setSessionId(String sessionId) {
		RuntimeContext.sessionId = sessionId;
	}

	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		if (sdDir != null) {
			return sdDir.toString();
		} else {
			return "";
		}
	}

	public static File getCrashLogFolder() {
		File f = new File(getSDPath() +CRASH_LOG_FOLDER);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	public static File getDataCacheForlder() {
		File f = new File(getSDPath() + DATA_CACHE_DIR);
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	public static UserLoginVO getUser() {
		return user;
	}

	public static void setUser(UserLoginVO user) {
		RuntimeContext.user = user;
        if(user!=null)
        {
            MyApp.getInstance().setUserId(new Integer(user.getUserId()));
        }
        else
            MyApp.getInstance().setUserId(null);
	}
	
	public static boolean isLoginUser(int userId){
		if (getUser()==null){
			return false;
		}
		return getUser().getUserId()==userId;
	}

    public static String getLogerFolder(){
        return getSDPath() + LOG_FOLDER;
    }

    public static String getDataFolder(){
        return getSDPath() + DATA_FOLDER;
    }

    public static String getDataFolderForImage(){
        return getDataFolderFor("image");
    }
    public static String getDataFolderForAudio(){
        String dir = getDataFolderFor("audio");
        if (!dir.endsWith(File.separator)){
            dir +=File.separator;
        }
        return dir;
    }
    public static String getDataFolderForVideo(){
        return getDataFolderFor("video");
    }

    public static String getDataFolderFor(String subFolder){
        String dir = getSDPath() + DATA_FOLDER + "/" + subFolder;
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        if (dir.endsWith(File.separator)){
            dir +=File.separator;
        }
        return dir;
    }
    public static String getAppDataFolder(String subFolder){
        String strFilesDirectory =  Environment.getExternalStorageDirectory().getAbsolutePath();

        File appDirectory = null;
        boolean bExternalStorageAvailable = iSExternalStorageAvailable();
        if(bExternalStorageAvailable)
        {
            appDirectory  = new File(strFilesDirectory + "/" + MyApp.APP_NAME);
        }
        else
        {
            appDirectory = new File( MyApp.getContext().getFilesDir() , MyApp.APP_NAME);
        }

        if (!appDirectory.exists()) {
            appDirectory.mkdirs();
        }
        String dir = appDirectory.getAbsolutePath();
        if (!dir.endsWith(File.separator)){
            dir +=File.separator;
        }

        dir+=subFolder;
        File f = new File(dir);
        if(!f.exists())
        {
            f.mkdir();
        }
        if (!dir.endsWith(File.separator)){
            dir +=File.separator;
        }

        return dir;
    }
    public static boolean iSExternalStorageAvailable()
    {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable;
    }
}
