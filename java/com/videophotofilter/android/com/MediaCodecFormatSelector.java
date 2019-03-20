package com.videophotofilter.android.com;

import android.os.Build;

public class MediaCodecFormatSelector {
    /*public static MediaCodecFormatSelector forDevice() {
        String deviceName = Device.getDeviceName();
        if (deviceName.equalsIgnoreCase("samsung gt-i9300") 
            && isBadMediaCodecSupport()) {
            return new SamsungGalaxyS3MediaCodecFormatSelector();
        else if (isBadMediaCodecSupport() && isAffectedDevice(deviceName)) {
            return new NoMediaCodecSupportFormatSelector();
        } else {
            return new MediaCodecFormatSelector();
        }
    }

    private static boolean isBadMediaCodecSupport() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;
    }*/
}