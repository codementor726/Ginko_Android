package com.ginko.common;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ginko.ginko.MyApp;

/**
 * Created by YongJong on 09/29/16.
 */
public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectionDetector cd = new ConnectionDetector(context);
        Boolean isInternetConnected = cd.isConnectingToInternet();
    }
}