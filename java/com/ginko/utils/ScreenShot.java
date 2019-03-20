package com.ginko.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.ginko.common.RuntimeContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenShot {
    // Get the screenshot for the specified activity and same to sd card.
    private static Bitmap takeScreenShot(Activity activity) {
        // View is the screen which you want to capture
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // Get the height of the status bar
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.i("TAG", "" + statusBarHeight);

        // Get the width and height of the screen.
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        // Remove the title bar
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    private static void savePic(Bitmap b, File imageFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String shoot(Activity a) {
        File imageFile = new File(RuntimeContext.getTempFolder(), FileUtils.createTimestampFileName("screenshot.png"));
        ScreenShot.savePic(ScreenShot.takeScreenShot(a), imageFile);
        return imageFile.getAbsolutePath();
    }
}