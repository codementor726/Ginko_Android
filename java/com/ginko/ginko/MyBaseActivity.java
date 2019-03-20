package com.ginko.ginko;

import android.app.ActionBar;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.appevents.AppEventsLogger;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.sprout.GinkoMeActivity;
import com.ginko.api.request.SpoutRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ProgressDialogManage;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.receiver.RestartService;
import com.ginko.service.PersistentService;
import com.ginko.service.SproutService;
import com.ginko.setup.Login;
import com.ginko.setup.Sign;

public class MyBaseActivity extends Activity {
	private boolean showLogoutMenu;
	private boolean displayHomeAsUpEnabled;
	private boolean isPause = false;
//	protected DropdownMenu dropDownMenu;
	private Intent intent;
	//private RestartService restartService;

	protected MyApp mMyApp;

	private SproutService sproutService = null;

	ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder binder) {
			Logger.debug("Bind to sprout service successfully.");
			sproutService = ((SproutService.LocalBinder) binder).getService();
			if(!sproutService.isLocationServiceEnabled())
				return;
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			sproutService = null;
		}
	};

	private void binderLocationService() {
		Intent intent = new Intent(this, SproutService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void unbinderLocationService() {
		unbindService(serviceConnection);
	}

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

		Log.i("RestartService", "MyBaseActivity OnCreate");
		//initData();

		this.binderLocationService();
	}

	protected void getUIObjects()
	{

	}

	@Override
	protected void onResume() {
		super.onResume();
		mMyApp.setCurrentActivity(this);
		AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause() {
		clearReferences();
		AppEventsLogger.deactivateApp(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		clearReferences();
		this.unbinderLocationService();
		super.onDestroy();

		//unregisterReceiver(restartService);
	}

	private void clearReferences() {
		Activity currActivity = mMyApp.getCurrentActivity();
		if (currActivity != null && currActivity.equals(this))
			mMyApp.setCurrentActivity(null);
	}

	/*
	private void initData() {

		restartService = new RestartService();
		intent = new Intent(MyBaseActivity.this, PersistentService.class);

		Log.i("RestartService", "Init data");

		IntentFilter intentFilter = new IntentFilter("com.ginko.service.PersistentService");
		registerReceiver(restartService, intentFilter);
		startService(intent);
	}
	*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// it seems it's useless, when a activity is assign a parent activity in
		// AndroidManifest.xml, the back icon will display automatically.
		if (displayHomeAsUpEnabled){
			ActionBar actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		// Inflate the menu; this adds items to the action bar if it is present.
		if (this.showLogoutMenu) {
//			getMenuInflater().inflate(R.menu.base_with_logout, menu);
		}

        if(ConstValues.TESTING){
            //menu.addSubMenu(0,100000,999,"Report Bug");
        }

//		dropDownMenu = (DropdownMenu)findViewById(R.id.dropdownMenu1);
//		if (dropDownMenu!=null){
//			dropDownMenu.addButtonToActionBar(menu);
//		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_logout) {
			logout();
			return true;
		}

        if (id == 100000) {
            String title = this.getClass().toString();
            Uitils.reportBug("Bug: " + title ,"A bug found");
            return true;
        }
		
//		if (id == R.id.action_more){
//			if (dropDownMenu!=null){
//				dropDownMenu.tiggle();
//			}
//		}
		return super.onOptionsItemSelected(item);
	}

	protected void logout() {
		// String sessionId = Uitils.getStringFromSharedPreferences(this,
		// "sessionId", "");
		/*
		*  Add by lee for turn off gps when logout.
		*/
		if(sproutService != null) {
			SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
				@Override
				public void onCompleted(JsonResponse<Void> response) {
					if (response.isSuccess()) {
						RuntimeContext.getUser().setLocationOn(false);
						Uitils.storeSproutStartTime(MyApp.getContext(), 0);
						sproutService.stopSproutService();
					}
				}
			});
		}
		//////////////////////////////////////////////////
		UserRequest.logout(new ResponseCallBack<Void>() {
			@Override
			public void onCompleted(JsonResponse<Void> response) {
				if (response.isSuccess()) {
					Uitils.LogoutFromFacebook(MyBaseActivity.this);
					Uitils.setStringToSharedPreferences(MyBaseActivity.this,
							"sessionId", "");
					//Uitils.storeLastSyncTime(MyBaseActivity.this, RuntimeContext.getUser().getUserId(), 0);
					RuntimeContext.setSessionId("");
					MyApp.getInstance().clearGlobalVariables();
					if (ContactMainActivity.getInstance() != null)
						ContactMainActivity.getInstance().finish();

					Uitils.toActivity(MyBaseActivity.this, Sign.class, true);
				}

			}

		});
	}

    @Override
    public void finish() {
        ProgressDialogManage.hide(); // make sure the window is dismiss to avoid window leak.
        super.finish();
    }

	public boolean isShowLogoutMenu() {
		return showLogoutMenu;
	}

	public void setShowLogoutMenu(boolean showLogoutMenu) {
		this.showLogoutMenu = showLogoutMenu;
	}

	public boolean isDisplayHomeAsUpEnabled() {
		return displayHomeAsUpEnabled;
	}

	public void setDisplayHomeAsUpEnabled(boolean displayHomeAsUpEnabled) {
		this.displayHomeAsUpEnabled = displayHomeAsUpEnabled;
	}
	
	/**
	 * the method is for customized dropdown menu, will use it, the sub-activity must override it.
	 * @param view
	 */
//	public void onSelectedCustomizedMenu(View view){
//		Logger.error( "the method must be implement when use dropdown menu");
//		if (dropDownMenu!=null){
//			dropDownMenu.tiggle();
//		}
//	}
}