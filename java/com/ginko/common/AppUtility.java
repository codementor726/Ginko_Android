package com.ginko.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.util.Log;

public class AppUtility {

	public static final String GCM_SENDER_ID = "778617728982";
	private static final String EMAIL_REGEXP = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";

	private static String remoteAppUri;
	private static String remoteAppVer;
	
	public static String byteArrayToHexString(byte in[]) {
	    if (in == null || in.length <= 0) {
	        return null;
	    }
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < in.length; i++) {
        	String s = Integer.toHexString(0xFF & in[i]);
        	if (s.length() == 1) {
        		s = "0" + s;
        	}
            hexString.append(s);
        }
        return hexString.toString();
	}    
	
	public static String encryptMD5(String what) {
		if (what == null) {
			return null;
		}
		try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(what.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        return byteArrayToHexString(messageDigest);

	    } catch (NoSuchAlgorithmException e) {
	        Logger.error(e);
	    }
	    return null;		
	}

	public static int dpiToPixels(Context c, int dpi) {
		return (int)(dpi * c.getResources().getDisplayMetrics().density);
	}
	
	public static void activityExitWithMessage(final Activity activity, String message) {
		new AlertDialog.Builder(activity)
			.setMessage(message)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.finish();
				}
			})
			.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					activity.finish();
				}
			})
			.show();
	}

	public static boolean isUpdateAvailable(Context context, String remoteAppVer) {
		if (remoteAppVer == null || context == null) {
			return false;
		}
		try {
			PackageInfo packInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			int installedAppBuild = packInfo.versionCode;
			int remoteAppBuild = 0;
			try {
				remoteAppBuild = Integer.parseInt(remoteAppVer);
			} catch (NumberFormatException e) {}
			return installedAppBuild < remoteAppBuild;
		} catch (NameNotFoundException e) {}
		return false;
	}

	public static boolean installApplication(Context context, String fileName) {
	
		try {
			String fullPath = "file://" + context.getFilesDir().getAbsolutePath() + "/" + fileName;
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(fullPath), "application/vnd.android.package-archive");
			context.startActivity(intent);
		}
		catch (Exception e) {
			Logger.error(e);
			return false;
		}
		return true;
	}

	public static String getRemoteAppUri() {
		return remoteAppUri;
	}

	public static void setRemoteAppUri(String remoteAppUri) {
		AppUtility.remoteAppUri = remoteAppUri;
	}

	public static String getRemoteAppVer() {
		return AppUtility.remoteAppVer;
	}
	
	public static void setRemoteAppVer(String appBuild) {
		AppUtility.remoteAppVer = appBuild;	
	}
		
	public static boolean isOver18(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		int months = (cal.get(Calendar.YEAR) - year) * 12;
		months += cal.get(Calendar.MONTH) - month;
		int age = months / 12;

		if (age < 18 || (age == 18 && day > cal.get(Calendar.DAY_OF_MONTH))) {
			return false;
		}

		return true;
	}
	
	public static boolean isValidEmailAddress(String email) {
		return email.matches(EMAIL_REGEXP);
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		if (bitmap == null) {
			return bitmap;
		}
		
		int maxSize = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
		
        Bitmap output = Bitmap.createBitmap(maxSize, maxSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int xOffset = (bitmap.getWidth() - maxSize) / 2;
        int yOffset = (bitmap.getHeight() - maxSize) / 2;
        
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect srcRect = new Rect(xOffset, yOffset, xOffset + maxSize, yOffset + maxSize);
        final Rect destRect = new Rect(0, 0, maxSize, maxSize);
        final RectF rectF = new RectF(destRect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, destRect, paint);

        return output;
    }
	
	public static Bitmap getRoundedBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return bitmap;
		}
		
		int maxSize = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
		
	    Bitmap output = Bitmap.createBitmap(maxSize, maxSize, Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);
	
	    int xOffset = (bitmap.getWidth() - maxSize) / 2;
	    int yOffset = (bitmap.getHeight() - maxSize) / 2;
	    
	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect srcRect = new Rect(xOffset, yOffset, xOffset + maxSize, yOffset + maxSize);
	    final Rect destRect = new Rect(0, 0, maxSize, maxSize);

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawCircle(destRect.centerX(), destRect.centerY(), maxSize / 2, paint);
	
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, srcRect, destRect, paint);
	
	    return output;
	}

	public static void hideNotification(Context context, String notificationId) {
		int nId = 0;
		try {
			nId = Integer.valueOf(notificationId);
		} catch (NumberFormatException e) {}
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(nId);
	}
	
	public static int dp2px(Context context, int dp) {
		if (context == null) {
			Logger.error( "Context is null, can't display the image.");
			return dp;
		}
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}
}
