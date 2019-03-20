package com.ginko.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ginko.service.PersistentService;

public class RestartService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RestartService", "Restart Service Called");
        if (intent.getAction().equals("ACTION.RESTART.PersistentService")) {
            Log.i("RestartService", "System Quit");
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("RestartService", "System Rebooted");
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
            Log.i("RestartService", "System Fully  Removed");
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            Log.i("RestartService", "System Removed");
            Intent i = new Intent(context, PersistentService.class);
            context.startService(i);
        }


    }

}
