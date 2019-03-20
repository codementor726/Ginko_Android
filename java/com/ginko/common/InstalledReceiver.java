package com.ginko.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ginko.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Stony Zhang on 2/2/2015.
 */
public class InstalledReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {     // install
            String packageName = intent.getDataString();
            clearLogs(context, packageName);
            Logger.debug("installed :" + packageName);
        }

        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {   // uninstall
            String packageName = intent.getDataString();
            clearLogs(context, packageName);
            Logger.debug("uninstalled :" + packageName);
        }
    }

    private void clearLogs(Context context, String packageName) {
        String myPackageName = context.getPackageName();
        Logger.debug("myPackageName :" + myPackageName);
        if (packageName.endsWith(myPackageName)) {
            Logger.debug("uninstalled :" + packageName);
            try {
                FileUtils.cleanDirectory(new File(RuntimeContext.getLogerFolder()));
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }


}