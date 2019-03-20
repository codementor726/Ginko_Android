package com.ginko.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ginko.api.request.SpoutRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class SproutService extends Service {
    private IBinder binder = new SproutService.LocalBinder();
    private LocationManager lm;
    private Location latestLocation;
    private Looper locationLooper = null;


    private boolean addedListener;

    private long lastServiceStartedTime = 0;
    public boolean isGPSOn = false;
    private SproutServiceActionListener sproutActionListener;

    private long locationListenerUpdateInterval = 1000*60;
    private long networkListenerUpdateInterval = 1000*60;

    public final static long AUTO_TURN_OFF_TIME_LIMIT = 60*1000*60;

    private boolean isSingleLocationUpdate = false;

    private Object lockObj = new Object();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Logger.debug("Renew Location ");
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateLocation(location);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void stopSproutService()
    {
        if(lm == null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mHandler.removeCallbacks(runnableAutoSproutTurnOff);
        isGPSOn = false;

        if(addedListener)
        {
            addedListener = false;
            lm.removeUpdates(locationListener);
            lm.removeUpdates(networkListener);
        }
        lastServiceStartedTime = 0;
        Uitils.storeSproutStartTime(MyApp.getContext() , lastServiceStartedTime);

        //if(sproutActionListener!=null)
        //    sproutActionListener.onSproutLocationServiceStopped();
    }
    public void startSproutService()
    {
        if(lm == null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isGPSOn = true;
        if(addedListener)
        {
            addedListener = false;
            lm.removeUpdates(locationListener);
            lm.removeUpdates(networkListener);
        }
        locationListenerUpdateInterval = 1000;
        networkListenerUpdateInterval = 1000;

        addLocationListener();

        //if(sproutActionListener!=null)
        //   sproutActionListener.onSproutLocationServiceStarted();
    }


    @Override
    public void onCreate() {
        System.out.println("SproutService.onCreate");
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListenerUpdateInterval = 1000;
        networkListenerUpdateInterval = 1000;
        this.init();

        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        System.out.println("SproutService.onStart");
        super.onStart(intent, startId);
    }


    public void onTaskRemoved(Intent rootIntent) {
        //Code here
     //   if(RuntimeContext.getUser() == null)
      //      return;

        SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    RuntimeContext.getUser().setLocationOn(false);
                    Uitils.storeSproutStartTime(MyApp.getContext(), 0);
                    stopSproutService();
                }
            }
        });

        isGPSOn = false;

        stopSelf();
    }


    public void registerSproutActionListener(SproutServiceActionListener listener)
    {
        synchronized (lockObj) {
            this.sproutActionListener = listener;
        }
    }
    public void unregisterSproutActionListener(SproutServiceActionListener listener){
        synchronized (lockObj) {
            if (this.sproutActionListener == listener)
                this.sproutActionListener = null;
        }
    }

    public synchronized void setAutoOffStartTime(long time)
    {
        this.lastServiceStartedTime = time;
        Uitils.storeSproutStartTime(MyApp.getContext(), lastServiceStartedTime);
        if(this.lastServiceStartedTime>0)
        {
            mHandler.postDelayed(runnableAutoSproutTurnOff , AUTO_TURN_OFF_TIME_LIMIT);
        }

    }
    private void init() {
        if(RuntimeContext.getUser() == null)
            return;
        if(RuntimeContext.getUser().getLocationOn()) {
            lastServiceStartedTime = Uitils.getSproutStartedTime(MyApp.getContext());
            long currentTimeInMills = Calendar.getInstance().getTimeInMillis();
            if(lastServiceStartedTime != 0 && (currentTimeInMills-lastServiceStartedTime)>AUTO_TURN_OFF_TIME_LIMIT)//one hour auto off is set and if one hour is passed
            {
                //reset the time
                lastServiceStartedTime = 0;
                Uitils.storeSproutStartTime(MyApp.getContext() , 0);
                RuntimeContext.getUser().setLocationOn(false);
                SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            isGPSOn = false;
                            RuntimeContext.getUser().setLocationOn(false);
                        }
                    }
                });
                return;
            }
            if(lastServiceStartedTime != 0 && (currentTimeInMills-lastServiceStartedTime)<AUTO_TURN_OFF_TIME_LIMIT)
            {
                mHandler.postDelayed(runnableAutoSproutTurnOff, currentTimeInMills - lastServiceStartedTime);
            }

            this.addLocationListener();
            //if(sproutActionListener!=null)
            //    sproutActionListener.onSproutLocationServiceStarted();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("SproutService.onStartCommand");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        System.out.println("SproutService.onDestroy");
        super.onDestroy();
    }

    public boolean isLocationEnable() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public Location getLatestLocation() {
        return latestLocation;
    }

    public void setLatestLocation(Location latestLocation) {
        this.latestLocation = latestLocation;
    }


    public boolean isLocationServiceEnabled()
    {
        if(lm == null)
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            return true;
        return false;
    }


    public class LocalBinder extends Binder {
        public SproutService getService() {
            return SproutService.this;
        }
    }


    public void removeLocationListeners()
    {
        if(addedListener)
        {
            lm.removeUpdates(locationListener);
            lm.removeUpdates(networkListener);
            locationListenerUpdateInterval = 1000;
            networkListenerUpdateInterval = 1000;
        }
    }

    public void addLocationListener() {
        if (!this.isLocationEnable()) {
            Logger.error("GPS PROVIDER is not enable.");
//            return;
        }
        if (addedListener) {
            return;
        }

        try {
            List<String> allProviders = lm.getAllProviders();
            Logger.debug("-----------GPS provide as followingr------------------");
            for (String provider : allProviders) {
                Logger.debug(provider);
            }
            Logger.debug("----------------------------------------");
            //为获取地理位置信息时设置查询条件
            String bestProvider = lm.getBestProvider(getCriteria(), true);
            Logger.debug("best Provider:" + bestProvider);
            //获取位置信息
            //如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location==null){
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location!=null){
                updateLocation(location);
            }
            //监听状态
            lm.addGpsStatusListener(listener);

            System.out.println("----Location Listeners Registered-----");
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationListenerUpdateInterval, 50, locationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, networkListenerUpdateInterval, 50, networkListener);
            addedListener = true;
        }catch (Exception e) {
            Logger.error(e);
            Uitils.reportBug(e);
        }
    }

    public synchronized void startSingleLocationUpdate()
    {
        if(isSingleLocationUpdate == false) {
            if (sproutActionListener != null)
                sproutActionListener.singleLocationUpdateStarted();

            SpoutRequest.switchLocationStatus(true , new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if(response.isSuccess())
                    {
                        //System.out.println("startSingleLocationUpdate()");
                        //if(RuntimeContext.getUser()!=null)
                            //RuntimeContext.getUser().setLocationOn(true);

                        /*if (sproutActionListener != null)
                            sproutActionListener.singleLocationUpdateStarted();*/
                        isGPSOn = true;

                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_LOW);
                        criteria.setPowerRequirement(Criteria.POWER_LOW);

                        if(locationLooper == null) {
                            locationLooper = Looper.myLooper();
                        }

                        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location == null) {
                                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            }
                            if (location != null) {
                                updateLocation(location);
                                return;
                            }
                        }
                        lm.requestSingleUpdate(criteria, locationListener, locationLooper);


                        /*if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                        }
                        else
                        {
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, networkListener);
                        }*/
                    }
                }
            });
            isSingleLocationUpdate = true;
        }
    }
    public synchronized void stopSingleLocationUpdate()
    {
        if(isSingleLocationUpdate) {
            System.out.println("stopSingleLocationUpdate()");
            if(sproutActionListener!=null)
                sproutActionListener.singleLocationUpdateEnded();

            SpoutRequest.switchLocationStatus(false , new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        //if(RuntimeContext.getUser()!=null)
                        //    RuntimeContext.getUser().setLocationOn(false);

                        /*if(sproutActionListener!=null)
                            sproutActionListener.singleLocationUpdateEnded();*/
                        isGPSOn = false;
                    }
                }
            );
            isSingleLocationUpdate = false;
            lm.removeUpdates(locationListener);

        }

    }
    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        //设置是否需要方位信息
        criteria.setBearingRequired(false);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    //状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    System.out.println("The first location set.");
                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    System.out.println("Satellite status changed");
                    //获取当前状态
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
//                    System.out.println("Searched: " + count + " satellites");
                    break;
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    System.out.println("GPS location start.");
                    break;
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    System.out.println("GPS location end");
                    break;
            }
        }

        ;
    };

    private LocationListener locationListener = new MyLocationListener();
    private LocationListener networkListener = new MyLocationListener();

    public class MyLocationListener implements LocationListener {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            System.out.println("-----Got New Location of provider:" + location.getProvider() + "-----");
            /*if (latestLocation != null) {
                if (!isBetterLocation(location, latestLocation)) {
                    System.out.println("Not very good!");
                    return;
                }
                System.out.println("It's a better location");
            } else {
                System.out.println("It's first location");
            }*/

            updateLocation(location);
            System.out.println("Time:" + location.getTime());
            System.out.println("Longitude:" + location.getLongitude());
            System.out.println("Latitude:" + location.getLatitude());
            System.out.println("Altitude:" + location.getAltitude());

            if(isSingleLocationUpdate)
            {
                System.out.println("----Single Location Update : (" + String.valueOf(location.getLatitude()) + " , " + String.valueOf(location.getLongitude()) + ")-----");
                lm.removeUpdates(this);
                return;
            }

            if (LocationManager.GPS_PROVIDER.equals(location.getProvider()) && locationListenerUpdateInterval == 1000) {
                lm.removeUpdates(this);
                locationListenerUpdateInterval = 60*1000; //update location every minute
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationListenerUpdateInterval, 1, locationListener);
            }


            if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider()) && networkListenerUpdateInterval == 1000) {
                lm.removeUpdates(this);
                networkListenerUpdateInterval = 60*1000;
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, networkListenerUpdateInterval, 1, networkListener);
            }
        }

        /**
         * Triggered when GPS status changes
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    System.out.println("GPS is available now.");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    System.out.println("GPS is out of service.");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    System.out.println("GPS is unavailable temporarily.");
                    break;
            }
        }

        /**
         * Trigger when GPS change to enable
         */
        public void onProviderEnabled(String provider) {
            Location location = lm.getLastKnownLocation(provider);
            updateLocation(location);
        }

        public void onProviderDisabled(String provider) {
            Logger.debug("Disable provider:" + provider);
            //updateLocation(null);
        }
    };

    private void updateLocation(Location location) {
        if (location == null) {
            return;
        }
        this.latestLocation = location;
        reportLocation(null);
    }

    public void reportLocation(final ReportLocationListener listener) {
        Location location = this.latestLocation;
        if (location == null) {
            Logger.error("Location is null, can't update location to the server.");
            //return;
        }
        final boolean isSingleUpdatedLocation = isSingleLocationUpdate;
        SpoutRequest.updateLocation(location.getLatitude(), location.getLongitude(), new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    Logger.debug("Upload location successfully!");
                    if (listener != null) {
                        listener.success();
                    }
                    if(sproutActionListener!=null && isSingleLocationUpdate) {
                        sproutActionListener.singleLocationChanged();
                    }

                    //notify SproutActivity to detect new friends
                    //if(!isSingleUpdatedLocation)
                    {
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.DETECTED_NEW_USER");
                        sendBroadcast(intent1);
                    }
                }
            }
        });
    }

    public interface  ReportLocationListener{
        void success();
    }

    private static final int CHECK_INTERVAL = 1000 * 30;

    protected boolean isBetterLocation(Location location,
                                       Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > CHECK_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -CHECK_INTERVAL;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location,
        // use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must
            // be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private Runnable runnableAutoSproutTurnOff = new Runnable() {
        @Override
        public void run() {
            long currentTimeInMills = Calendar.getInstance().getTimeInMillis();
            if(lastServiceStartedTime != 0 && (currentTimeInMills-lastServiceStartedTime)>AUTO_TURN_OFF_TIME_LIMIT)//one hour auto off is set and if one hour is passed
            {
                //reset the time
                System.out.println("One hour is passed , Turn off the sprout automatically");
                lastServiceStartedTime = 0;
                Uitils.storeSproutStartTime(MyApp.getContext() , 0);
                SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            RuntimeContext.getUser().setLocationOn(false);
                        }
                    }
                });
                RuntimeContext.getUser().setLocationOn(false);
                if(addedListener)
                    removeLocationListeners();
                locationListenerUpdateInterval = 1000;
                networkListenerUpdateInterval = 1000;

                if(sproutActionListener != null)
                    sproutActionListener.onSproutAutoStopped();
                return;
            }
        }
    };

    public interface SproutServiceActionListener{
        //public void onSproutLocationServiceStarted();
        //public void onSproutLocationServiceStopped();
        public void onSproutAutoStopped();
        public void singleLocationUpdateStarted();
        public void singleLocationUpdateEnded();
        public void singleLocationChanged();

    }

}
