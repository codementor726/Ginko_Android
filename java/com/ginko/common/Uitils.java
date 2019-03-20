package com.ginko.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.ginko.api.request.MiscRequest;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.ScreenShot;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uitils {
	private static final String PREFS_DEVICE_ID = "DEVICE_ID";

    public static int getOsVersion() {
        try {
            java.lang.reflect.Field osField = android.os.Build.VERSION.class.getDeclaredField("SDK_INT");
            osField.setAccessible(true);
            int myBuild = osField.getInt(android.os.Build.VERSION.class);
            return myBuild;
        } catch (Exception e) {
            return 3;
        }
    }


    public static String getDeviceUid() {
		return getUDID();
	}

	// This is for GCM
	public static String getDeviceToken() {
		String registrationId = Uitils.getStringFromSharedPreferences(MyApp.getInstance().getApplicationContext(), ConstValues.PROPERTY_REG_ID, "");
		return registrationId;
	}

	public static void storeSessionid(Context context, String sessionId) {
		RuntimeContext.setSessionId(sessionId);
		Uitils.setStringToSharedPreferences(context, "sessionId", sessionId);
	}

	public static Object getSessionId(Context context) {
		context = MyApp.getInstance().getApplicationContext();
		return getStringFromSharedPreferences(context, "sessionId", "");
	}

    public static void storeUserFullname(Context context, String userFullanme) {
        Uitils.setStringToSharedPreferences(context, "userfullname", userFullanme);
    }

    public static String getUserFullname(Context context) {
        context = MyApp.getInstance().getApplicationContext();
        return getStringFromSharedPreferences(context, "userfullname", "");
    }

    public static void storeUserName(Context context, String userFullanme) {
        Uitils.setStringToSharedPreferences(context, "user_name", userFullanme);
    }

    public static String getUserName(Context context) {
        context = MyApp.getInstance().getApplicationContext();
        return getStringFromSharedPreferences(context, "user_name", "");
    }

    public static void storeLoginEmail(Context context, String loginEmail) {
        Uitils.setStringToSharedPreferences(context, "login_email", loginEmail);
    }

    public static String getLoginEmail(Context context) {
        context = MyApp.getInstance().getApplicationContext();
        return getStringFromSharedPreferences(context, "login_email", "");
    }

    public static void storeIsSortByFName(Context context, boolean isSortByFirstName) {
        Uitils.setBooleanToSharedPreferences(context, "isSortByFirstName", isSortByFirstName);
    }

    public static boolean getIsSortByFName(Context context) {
        context = MyApp.getInstance().getApplicationContext();
        return getBooleanFromSharedPreferences(context, "isSortByFirstName", true);
    }

    public static void storeIsContactTileStyle(Context context, boolean isSortByFirstName) {
        Uitils.setBooleanToSharedPreferences(context, "isContactTileStyle", isSortByFirstName);
    }

    public static boolean getIsContactTileStyle(Context context) {
        context = MyApp.getInstance().getApplicationContext();
        return getBooleanFromSharedPreferences(context, "isContactTileStyle", false);
    }
    public static long getSproutStartedTime(Context context)
    {
        return Uitils.getLongFromSharedPreferences(context, "sprout_started_time", 0);
    }

    public static void storeSproutStartTime(Context context, long currentTime)
    {
        Uitils.setLongToSharedPreferences(context, "sprout_started_time" , currentTime);
    }
    public static void storeLastSyncTime(Context context, int userId, long currentTime)
    {
        //store sync_time per every user
        Uitils.setLongToSharedPreferences(context, "sync_time"+String.valueOf(userId) , currentTime);
    }
    public static long getLastSyncTime(Context context , int userId)
    {
        return Uitils.getLongFromSharedPreferences(context , "sync_time"+String.valueOf(userId) , 0);
    }


	public static void LogoutFromFacebook(Context context) {

        LoginManager.getInstance().logOut();
        FacebookSdk.sdkInitialize(context);
        /*
        Session session = Session.getActiveSession();
        if (session != null) {

			if (!session.isClosed()) {
				session.closeAndClearTokenInformation();
				// clear your preferences if saved
			}
		} else {

			session = new Session(context);
			Session.setActiveSession(session);

			session.closeAndClearTokenInformation();
			// clear your preferences if saved

		}
         */

	}

	public static String getStringFromSharedPreferences(Context context,
			String key, String defaultValue) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences(
                ConstValues.preferenceName, 0);
		String value = mySharedPreferences.getString(key, defaultValue);
		return value;
	}

	public static void setStringToSharedPreferences(Context context,
			String key, String value) {
		SharedPreferences settings = context.getSharedPreferences(ConstValues.preferenceName,
				0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

    public static long getLongFromSharedPreferences(Context context,
                                                        String key, long defaultValue) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                ConstValues.preferenceName, 0);
        long value = mySharedPreferences.getLong(key, defaultValue);
        return value;
    }

    public static void setLongToSharedPreferences(Context context,
                                                    String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(ConstValues.preferenceName,
                0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static int getIntFromSharedPreferences(Context context,
                                                    String key, int defaultValue) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                ConstValues.preferenceName, 0);
        int value = mySharedPreferences.getInt(key, defaultValue);
        return value;
    }

    public static void setIntToSharedPreferences(Context context,
                                                  String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(ConstValues.preferenceName,
                0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static boolean getBooleanFromSharedPreferences(Context context,
                                                    String key, boolean defaultValue) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(
                ConstValues.preferenceName, 0);
        boolean value = mySharedPreferences.getBoolean(key, defaultValue);
        return value;
    }

    public static void setBooleanToSharedPreferences(Context context,
                                                  String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(ConstValues.preferenceName,
                0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

	public static void toActivity(Context context,
			Class<? extends Activity> targetActivity, boolean finishCurrent) {
		Intent intent = new Intent();
		intent.setClass(context, targetActivity);
		context.startActivity(intent);
		if (finishCurrent) {
			((Activity) context).finish();
		}
	}

	public static void toActivity(Class<? extends Activity> targetActivity,
			boolean finishCurrent) {
		Context context = MyApp.getInstance().getCurrentActivity();

		Intent intent = new Intent();
		intent.setClass(context, targetActivity);
		context.startActivity(intent);
		if (finishCurrent) {
			((Activity) context).finish();
		}
	}

	public static AlertDialog alert(String msg, OnClickListener okAction) {
		// new AlertDialog.Builder(context).setTitle(R.string.app_name)
		// .setMessage(msg)
		// .setIcon(R.drawable.ginko_iconx16)
		// .setNeutralButton("Ok", null).show();
		Context context = MyApp.getInstance().getCurrentActivity();
		if (context == null) {
			Logger.error("can't get the context, don't show the alert!");
			// if (okAction!=null){
			// okAction.onClick(dialog, which);
			// }
			return null;
		}
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.alert, null);
		TextView textView = (TextView) layout.findViewById(R.id.text);
		textView.setText(msg);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		AlertDialog alertDialog = builder.setNeutralButton("Ok", okAction)
				.create();
		alertDialog.show();
		return alertDialog;
	}

    public static AlertDialog alert( Context context,String msg) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.alert, null);
        TextView textView = (TextView) layout.findViewById(R.id.text);
        textView.setText(msg);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        AlertDialog alertDialog = builder.setNeutralButton("Ok", null)
                .create();
        alertDialog.show();
        return alertDialog;
    }

	public static AlertDialog alert(String msg) {

		return alert(msg, null);
	}

	public static boolean isEmail(String strEmail) {
		String strPattern = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}

	public static List<JSONObject> toJsonList(JSONArray arr) {
		if (arr == null) {
			return Collections.EMPTY_LIST;
		}
		List<JSONObject> results = new ArrayList<JSONObject>();

		for (int i = 0; i < arr.length(); i++) {
			results.add(arr.optJSONObject(i));
		}
		return results;
	}

	public static synchronized String getUDID() {

		 String uuid = getStringFromSharedPreferences(MyApp
				.getInstance().getApplicationContext(), PREFS_DEVICE_ID, null);

		if (uuid != null) {
			// Use the ids previously computed and stored in the prefs file
			return uuid;
		}

		final String androidId = Secure.getString(MyApp.getInstance()
				.getContentResolver(), Secure.ANDROID_ID);

		// Use the Android ID unless it's broken, in which case fallback
		// on deviceId,
		// unless it's not available, then fallback on a random number
		// which we store
		// to a prefs file
		try {
			if (!"9774d56d682e549c".equals(androidId)) {
				uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"))
						.toString();
			} else {
				 String deviceId = ((TelephonyManager) MyApp.getInstance()
						.getSystemService(Context.TELEPHONY_SERVICE))
						.getDeviceId();
				uuid = deviceId != null ? UUID.nameUUIDFromBytes(
						deviceId.getBytes("utf8")).toString() : UUID
						.randomUUID().toString();
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		setStringToSharedPreferences(MyApp.getInstance()
				.getApplicationContext(), PREFS_DEVICE_ID, uuid);
		return uuid;
	}

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    public static void downloadFile(final String url, final String path) {
        downloadFile(url, path, null);
    }
    public static void downloadFile(final String url, final String path, final DownloadCompletedCallback callback) {
        File file = new File(path);
        if(file.exists()){
            //if already downloaded, don't download again.
            if (callback!=null){
                callback.onCompleted(path);
            }
            return;
        }
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... params) {
                Logger.debug("Download file from: " + url);
                URL myFileUrl = null;
                Bitmap bitmap = null;
                try {
                    Logger.debug(url);
                    myFileUrl = new URL(url);
                } catch (MalformedURLException e) {
                    Logger.error("url is invalid:" + url,e);
                }
                if(myFileUrl == null){
                    return null;
                }
                InputStream is = null;
                FileOutputStream output = null;
                try {
                    HttpURLConnection conn = (HttpURLConnection) myFileUrl
                            .openConnection();
                    conn.setConnectTimeout(0);
                    conn.setDoInput(true);
                    conn.connect();
                    is = conn.getInputStream();
                    output = new FileOutputStream(path);
                    IOUtils.copy(is, output);
                } catch (IOException e) {
                    Logger.error(e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (callback!=null){
                    callback.onCompleted(path);
                }
            }
        }.execute(url);
    }
    public static void writeFile(File file , boolean bAppend , byte[] bytesData)
    {
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
    public static void dwonlaodImageSaveToFile(String strUrl , String saveFilePath )
    {
        try
        {
            File file = new File(saveFilePath);
            if(file.exists())
                file.delete();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        try {
            File file = new File(saveFilePath);
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(is);

            ByteArrayOutputStream outstream = new ByteArrayOutputStream();

            bm.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            byte[] byteArray = outstream.toByteArray();

            writeFile( file ,false , byteArray);

        } catch(Exception e) {
            e.printStackTrace();
            File file = new File(saveFilePath);
            if(file.exists())
                file.delete();
        }
    }
    public static interface DownloadCompletedCallback{
        void onCompleted(String filePath);
    }

    public static DisplayMetrics  getResolution(Activity activity){
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric;
    }

    public static float getScreenRatioViaIPhone(Activity activity){
        DisplayMetrics dm = Uitils.getResolution(activity);
        //320 is iOS width
        float ratio = 1.0f;
        float r1 = (float)dm.heightPixels/480;
        float r2 = (float)dm.widthPixels/320;

        if(r2>r1)
            ratio = r2;
        else
            ratio = r1;
        return ratio;
    }

    public static void reportBug(String title, String content){
        reportBug(title,content,"major",false);
    }

    public static void reportBug(String title, String content, String priority, final boolean killApp) {
        File zipLog = Logger.zipLog();
        final String logfilePath = zipLog.getAbsolutePath();
        File screenShootFile = null;
        try{
            String screenShot = ScreenShot.shoot(MyApp.getInstance().getCurrentActivity());
            if (StringUtils.isNotBlank(screenShot)){
                screenShootFile = new File(screenShot);
            }
        }catch (Exception e){
            Logger.error(e);
        }
        MiscRequest.reportBug(screenShootFile, zipLog, title, content, priority, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse response) {
                Logger.info("Report bug successfully!");
                if (!response.isSuccess()) {
                    Logger.error("Can't upload log file to server, you can get it in " + logfilePath);
                }
                if (killApp) {
                    killApp();
                }
            }
        });
    }

    public static void reportBug( Throwable e){
        reportBug(null, e);
    }

    public static void reportBug(String title, Throwable e){
        if(StringUtils.isBlank(title)){
            title = e.getMessage();
        }
        if(StringUtils.isBlank(title)){
            title = e.getClass().toString();
        }
        StringBuffer sb = new StringBuffer();
        String result = MyStringUtils.exceptionToString(e);
        sb.append("#Exception\n");
        sb.append("```\n");
        sb.append("#!java\n");
        sb.append(result);
        sb.append("```\n");
        reportBug(title, sb.toString());
    }




    public static void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
