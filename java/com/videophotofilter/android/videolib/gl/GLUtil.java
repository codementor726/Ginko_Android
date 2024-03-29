package com.videophotofilter.android.videolib.gl;

import android.annotation.SuppressLint;
import android.opengl.EGL14;
import android.util.Log;

@SuppressLint("NewApi")
public class GLUtil {	
    /**
     * Checks for EGL errors.
     */
    public static void checkEglError(String msg) {
        boolean failed = false;
        int error;
        while ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            Log.e("TAG", msg + ": EGL error: 0x" + Integer.toHexString(error));
            failed = true;
        }
        if (failed) {
            throw new RuntimeException("EGL error encountered (see log)");
        }
    }	
}
