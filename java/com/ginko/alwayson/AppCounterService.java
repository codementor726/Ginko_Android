package com.ginko.alwayson;

import android.app.ActivityManager;
import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.ginko.api.request.SpoutRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;

import java.util.List;

/**
 * User: huhwook
 * Date: 2014. 1. 27.
 * Time: 오후 6:02
 */
public class AppCounterService extends Service {

    private final String LOG_NAME = AppCounterService.class.getSimpleName();

    public static Thread mThread;

    private ComponentName recentComponentName;
    private ActivityManager mActivityManager;

    private boolean serviceRunning = false;
    private boolean isForceExit = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        serviceRunning = true;
    }

    @Override
    public void onDestroy() {
        serviceRunning = false;
        SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    RuntimeContext.getUser().setLocationOn(false);
                    Uitils.storeSproutStartTime(MyApp.getContext(), 0);
                    //stopSelf();
                    //serviceRunning = false;
                }
            }
        });
        isForceExit = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mThread == null) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (serviceRunning) {
                        List<ActivityManager.RecentTaskInfo> info = mActivityManager.getRecentTasks(1, Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (info != null) {
                            ActivityManager.RecentTaskInfo recent = info.get(0);
                            Intent mIntent = recent.baseIntent;
                            ComponentName name = mIntent.getComponent();

                            if (name.equals(recentComponentName)) {
                                Log.d(LOG_NAME, "== pre App, recent App is same App");
                            } else {
                                recentComponentName = name;
                                Log.d(LOG_NAME, "== Application is catched: " + name);
                            }
                        }

                        if (isForceExit == true)
                        {
                            SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        RuntimeContext.getUser().setLocationOn(false);
                                        Uitils.storeSproutStartTime(MyApp.getContext(), 0);
                                        //stopSelf();
                                        //serviceRunning = false;
                                    }
                                }
                            });
                            isForceExit = false;
                        }
                        SystemClock.sleep(2000);
                    }
                }
            });

            mThread.start();
        } else if (mThread.isAlive() == false) {
            isForceExit = true;
            mThread.start();
        }

        return START_STICKY;
    }

    public void onTaskRemoved(Intent rootIntent) {
        if (isForceExit == true)
        {
            SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if (response.isSuccess()) {
                        RuntimeContext.getUser().setLocationOn(false);
                        Uitils.storeSproutStartTime(MyApp.getContext(), 0);
                        //stopSelf();
                        //serviceRunning = false;
                    }
                }
            });
            isForceExit = false;
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
