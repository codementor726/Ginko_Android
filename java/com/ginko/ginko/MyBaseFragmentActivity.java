package com.ginko.ginko;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ProgressDialogManage;

public class MyBaseFragmentActivity extends FragmentActivity {
	protected MyApp mMyApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		mMyApp = (MyApp) this.getApplicationContext();
        mMyApp.setCurrentActivity(this);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // some devices needs waking up screen first before disable keyguard
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock screenLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, getLocalClassName());
        screenLock.acquire();

        if (screenLock.isHeld()) {
            screenLock.release();
        }

        // On most other devices, using the KeyguardManager + the permission in
        // AndroidManifest.xml will do the trick
        KeyguardManager mKeyGuardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (mKeyGuardManager.inKeyguardRestrictedInputMode()) {
            KeyguardManager.KeyguardLock keyguardLock = mKeyGuardManager.newKeyguardLock(getLocalClassName());
            keyguardLock.disableKeyguard();
        }
    }

    protected  void getUIObjects()
    {

    }

	@Override
	protected void onResume() {
		super.onResume();
		mMyApp.setCurrentActivity(this);
	}

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(ConstValues.TESTING){
            menu.addSubMenu(0,100000,999,"Report Bug");
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 100000) {
            String title = this.getClass().toString();
            Uitils.reportBug("Bug: " + title, "A bug found");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
	@Override
	protected void onPause() {
		clearReferences();
		super.onPause();
    }

	@Override
	protected void onDestroy() {
		clearReferences();
		super.onDestroy();
	}

    @Override
    public void finish() {
        ProgressDialogManage.hide(); // make sure the window is dismiss to avoid window leak.
        super.finish();
    }

	private void clearReferences() {
		Activity currActivity = mMyApp.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			mMyApp.setCurrentActivity(null);
	}
}
