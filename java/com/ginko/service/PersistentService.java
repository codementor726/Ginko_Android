package com.ginko.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.ginko.api.request.SpoutRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.receiver.RestartService;

public class PersistentService extends Service {
    private static final int MILLISINIFUTURE = 1000 * 1000;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    public PersistentService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        unregisterRestartAlarm();
        Log.i("RestartService", "Persistentservice OnCreate");
        super.onCreate();
        initData();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(1, new Notification());

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification;

        Log.i("RestartService", "Persistentservice OnStartCommand");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("")
                    .setContentText("")
                    .build();

        }else{
            notification = new Notification(0, "", System.currentTimeMillis());
            notification.setLatestEventInfo(getApplicationContext(), "", "", null);
        }

        nm.notify(startId, notification);
        nm.cancel(startId);

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("RestartService", "Persistentservice OnDestroy");
        registerRestartAlarm();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.i("RestartService", "Persistentservice onTaskRemoved");
        registerRestartAlarm();
    }

    private void initData(){
        Log.i("RestartService", "Turn Off Signal Sent");
        SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    Log.i("RestartService", "Turn Off Success");
                }
            }
        });
    }

    private void registerRestartAlarm(){
        Log.i("RestartService", "register Restart Alarm");
        Intent intent = new Intent(PersistentService.this,RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this,0,intent,0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1*1000;

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1 * 1000, sender);

    }
    private void unregisterRestartAlarm(){
        Log.i("RestartService", "Unregister Restart Alarm");
        Intent intent = new Intent(PersistentService.this,RestartService.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this,0,intent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        alarmManager.cancel(sender);
    }
}
