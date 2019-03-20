package com.ginko.context;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import com.ginko.api.request.MiscRequest;
import com.ginko.common.Logger;
import com.ginko.common.MyStringUtils;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.utils.ScreenShot;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
/** 
*
* @author Stony Zhang
*  
*/  
public class CrashHandler implements UncaughtExceptionHandler {  

   private Thread.UncaughtExceptionHandler mDefaultHandler;
   private static CrashHandler INSTANCE = new CrashHandler();
   private Context mContext;
   private Map<String, String> infos = new LinkedHashMap<String, String>();
 
   private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
 
   private CrashHandler() {
   }  
 
   public static CrashHandler getInstance() {
       return INSTANCE;  
   }  
 
   /** 
    *
    * @param context 
    */  
   public void init(Context context) {  
       mContext = context;  
       mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
       Thread.setDefaultUncaughtExceptionHandler(this);
   }  
 
   /** 
    */
   @Override  
   public void uncaughtException(Thread thread, Throwable ex) {  
       if (!handleException(ex) && mDefaultHandler != null) {  
           mDefaultHandler.uncaughtException(thread, ex);
           return;
       }
       try {
           Thread.sleep(3000L);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
   }

    private void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    /**
    *
    * @param ex 
    */
   private boolean handleException(final Throwable ex) {
       if (ex == null) {  
           return false;  
       }  
       Logger.error(ex);
       new Thread() {
           @Override  
           public void run() {  
               Looper.prepare();  
               Toast.makeText(mContext, "So sorry, unexpected error occur.", Toast.LENGTH_LONG).show();
               Looper.loop();
           }  
       }.start();

       new Thread() {
           @Override
           public void run() {
               collectDeviceInfo(mContext);
               StringBuffer sb = createCrashReport(ex);
               saveCrashInfo2File(sb);
//       Logger.uploadLog();
               reportCrash(ex.getMessage(), sb);
           }
       }.start();

       return true;  
   }  
     
   /** 
    * @param ctx
    */  
   public void collectDeviceInfo(Context ctx) {  
       try {  
           PackageManager pm = ctx.getPackageManager();  
           PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
           if (pi != null) {  
               String versionName = pi.versionName == null ? "null" : pi.versionName;  
               String versionCode = pi.versionCode + "";  
               infos.put("versionName", versionName);  
               infos.put("versionCode", versionCode);  
           }  
       } catch (NameNotFoundException e) {  
           Logger.error("an error occured when collect package info", e);
       }
       List<Field> allFields = new ArrayList<Field>();
       Field[] fields = Build.class.getDeclaredFields();
       allFields.addAll(Arrays.asList(fields));
       allFields.addAll(Arrays.asList(Build.VERSION.class.getDeclaredFields()));

       for (Field field : allFields) {
           try {  
               field.setAccessible(true);  
               infos.put(field.getName(), field.get(null).toString());  
               Logger.debug(field.getName() + " : " + field.get(null));
           } catch (Exception e) {
               Logger.error("an error occured when collect crash info", e);
           }
       }
       try {
           int maxAppMemory = ((ActivityManager) ctx
                   .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
           long maxMemory2 = Runtime.getRuntime().maxMemory() / (1024 * 1024);
           long totalMemory2 = Runtime.getRuntime().totalMemory() / (1024 * 1024);
           long freeMemory2 = Runtime.getRuntime().freeMemory() / (1024 * 1024);
           Logger.debug("Max memory, ACTIVITY_SERVICE" + " : " + maxAppMemory);
           Logger.debug("Max memory, Runtime.getRuntime().maxMemory()" + " : " + maxMemory2);
           infos.put("Max memory1:", maxAppMemory + "M");
           infos.put("Max memory2:", maxMemory2 + "M");
           infos.put("Total Memory2:", totalMemory2 + "M");
           infos.put("Free Memory2:", freeMemory2 + "M");
       } catch (Exception e) {
           Logger.error("an error occured when collect crash info", e);
       }
   }

   private String saveCrashInfo2File(StringBuffer crashReport) {
       try {
           long timestamp = System.currentTimeMillis();
           String time = formatter.format(new Date());
           String fileName = "crash-" + time + "-" + timestamp + ".log";
           if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
               File dir = RuntimeContext.getCrashLogFolder();
               FileOutputStream fos = new FileOutputStream(new File (dir, fileName));
               fos.write(crashReport.toString().getBytes());
               fos.close();
           }
           return fileName;
       } catch (Exception e) {
           Logger.error("an error occured while writing file...", e);
       }  
       return null;  
   }

    private StringBuffer createCrashReport(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append("* " + key + "=" + value + "\n");
        }

        String result = MyStringUtils.exceptionToString(ex);
        sb.append("#Exception\n");
        sb.append("```\n");
        sb.append("#!java\n");
        sb.append(result);
        sb.append("```\n");
        return sb;
    }



    private void reportCrash(String exMsg, StringBuffer sbContent) {
        if (ConstValues.DEBUG){
            //In debug mode, don't report crash.
            return;
        }
        String title="App crash!";
        if (exMsg != null) {
            title += "--" + exMsg;
        }

        String content=sbContent.toString();
        String priority="blocker";
        reportBug(title, content, priority);
    }

    private void reportBug(String title, String content, String priority) {
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
                killApp();
            }
        });
    }
}