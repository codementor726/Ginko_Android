package com.ginko.setup;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import com.facebook.appevents.AppEventsLogger;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.entity.EntityInfoInputActivity;
import com.ginko.alwayson.ServiceMonitor;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.UserLoginVO;
import com.ginko.vo.UserWholeProfileVO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class Splash extends MyBaseActivity {

	static final String TAG = ConstValues.LOG_TAG;

	AtomicInteger msgId = new AtomicInteger();
	Context context;

	GoogleCloudMessaging gcm;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = ConstValues.SENDER_ID;

	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		/*
		ServiceMonitor serviceMonitor = ServiceMonitor.getInstance();

		if (serviceMonitor.isMonitoring() == false) {
			serviceMonitor.startMonitoring(getApplicationContext());
		}
		*/

		/*Intent intent = new Intent(Splash.this , GetStart.class);
		startActivity(intent);
		return;

		Intent intent = new Intent(Splash.this , RegisterConfirmationMobileActivity.class);
		startActivity(intent);
		return;*/

		context = getApplicationContext();
		String arch = System.getProperty("os.arch");
		System.out.println("---------CPU Architecture = "+arch+"----------");
		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		//regid = "APA91bEJFlQkvRmIj2HABN1Xt0JKFk8HXT2DLGVHUioAiLvLiHw-hcIXVacwat4NJFoM4yeGEtcvpMfYb4wx0HZHDOiL4jTiPZcuZtJxhACOKSM8zpzApWQ9hSppxBe6i4jFGJlERmol";
		//storeRegistrationId(Splash.this , regid);
		//startService(new Intent(getBaseContext(), AlwaysOnService.class));

		if (!ConstValues.DEBUG && checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

            System.out.println("Registered ID = "+regid);
			if (regid.isEmpty()) {
                Logger.debug("Try to get registration Id from google play service ");
				registerInBackground();
			}else{
				this.checkUserAlreadyLogin();
				return;
			}
		} else {
			MyApp.getInstance().showSimpleAlertDiloag(Splash.this, "No valid Google Play", null);
			Logger.info("No valid Google Play Services APK found.");
            this.checkUserAlreadyLogin();
			return;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Check device for Play Services APK.
		checkPlayServices();
		// Logs 'install' and 'app activate' App Events.
		AppEventsLogger.activateApp(this);
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
            Logger.debug("Play service is not available.");
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						ConstValues.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Logger.info("This device is not supported.");
				finish();
			}
			return false;
		}
        Logger.debug("Play service is available.");
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		int appVersion = Uitils.getAppVersion(context);
		Logger.info("Saving regId on app version " + appVersion);

		Uitils.setStringToSharedPreferences(context,
				ConstValues.PROPERTY_REG_ID, regId);
		Uitils.setStringToSharedPreferences(context,
				ConstValues.PROPERTY_APP_VERSION, appVersion + "");
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {

		String registrationId = Uitils.getStringFromSharedPreferences(context,
				ConstValues.PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Logger.info("Registration not found.");
			return "";
		}
		Logger.info("Registration id:" + registrationId);
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.

		String temp = Uitils.getStringFromSharedPreferences(context,
				ConstValues.PROPERTY_APP_VERSION, Integer.MIN_VALUE + "");
		int registeredVersion = Integer.valueOf(temp);
		int currentVersion = Uitils.getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Logger.info("App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
                int noOfAttemptsAllowed = 3;   // Number of Retries allowed
                int noOfAttempts = 0;          // Number of tries done

                while (true) {
                    try {
                        noOfAttempts ++;
                        if (gcm == null) {
                            gcm = GoogleCloudMessaging.getInstance(context);
                        }
                        regid = gcm.register(SENDER_ID);
                        msg = "Device registered, registration ID=" + regid;
//                        Uitils.alert(Splash.this,msg);
                        if (!regid.isEmpty())
                        {
                            // If registration ID obtained
                            storeRegistrationId(context, regid);
                            checkUserAlreadyLogin();
                           	break;
                        }
                            // Persist the regID - no need to register again.

                    } catch (Exception ex) {
                        msg = "Error :" + ex.getMessage();
                        Logger.error(msg);
                        // If there is an error, don't just keep trying to register.
                        // Require the user to click a button again, or perform
                        // exponential back-off.
                    }
                    // No Of tries exceeded, stop fetching
                    if( noOfAttempts > noOfAttemptsAllowed){
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Logger.error(e);
                    }
                }
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				// mDisplay.append(msg + "\n");
                Logger.debug(msg);
				DialogInterface.OnClickListener dlgListner = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						registerInBackground();
					}
				};

				if (msg.startsWith("Error")){
					//Uitils.alert("Opps...Can't register your device into google play." + msg);
					//Uitils.alert("Internet connection is missing.");
					MyApp.getInstance().showSimpleAlertDiloag(Splash.this, "Internet connection is missing.", dlgListner);
				}

				Logger.info(msg);
			}
		}.execute(null, null, null);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Logs 'app deactivate' App Event.
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void checkUserAlreadyLogin() {
		final String sessionId = Uitils.getStringFromSharedPreferences(this,
				"sessionId", "");
		if (sessionId.isEmpty()) {
            Logger.debug("sessionId is empty, go to Sign screen.");
			Uitils.toActivity(this, Sign.class, true);
			return;
		}

		UserRequest.checkSessionId(this, sessionId,
				new ResponseCallBack<UserLoginVO>() {

					@Override
					public void onCompleted(JsonResponse<UserLoginVO> response) {
						if (response.isSuccess()) {
							RuntimeContext.setSessionId(sessionId);
                            UserLoginVO user = response.getData();

							RuntimeContext.setUser(user);
							String fullname = user.getFirstName()+" ";

							fullname += user.getMiddleName();
							fullname = fullname.trim();
							if(user.getLastName()!=null && !user.getLastName().equals(""))
								fullname += " " + user.getLastName();
							fullname = fullname.trim();
							Uitils.storeUserFullname(Splash.this , fullname);
							Uitils.storeUserName(Splash.this, user.getUserName());

							MyApp.getInstance().initializeGlobalVariables();

							//long oldSyncTime = Uitils.getLastSyncTime(getApplicationContext());
							long lastSyncTime = Uitils.getLastSyncTime(getApplicationContext() , user.getUserId());
							//MyApp.getInstance().getAllContactItemsFromDatabase();
							//if(lastSyncTime  != 0)
							//	MyApp.getInstance().getSyncUpdatedContacts(lastSyncTime);
                            /*if(true) {
                                Uitils.toActivity(Splash.this,
                                        GetStart.class, true); // FIXME
                                return;
                            }*/

                            if(user.getSetupPage()==null || user.getSetupPage().equals("") || !user.getSetupPage().equals("2"))
                            {
                                Uitils.toActivity(Splash.this,
                                        Sign.class, true); // FIXME
                                return;
                            }

							//Uitils.alert(Splash.this , Settings.Secure.getString(Splash.this.getContentResolver(), Settings.Secure.ANDROID_ID));
							//if(true)
							//	return;
                            //start sprout service
                            MyApp.getInstance().startSproutService();

                            //Uitils.toActivity(Splash.this,
                            //       EntityInfoInputActivity.class, true); // FIXME
                            /*Intent intent = new Intent(Splash.this , TradeCardPhotoEditorSetActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isSetNewPhotoInfo" , false);
                            bundle.putInt("tradecardType", ConstValues.HOME_PHOTO_EDITOR)  ;
                            bundle.putSerializable("userInfo" , new UserWholeProfileVO());
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();*/
							MyApp.getInstance().fetchAllEntites(1, Splash.this);
							MyApp.getInstance().loadContacts(1, Splash.this);

							//Uitils.toActivity(Splash.this,
							//		ContactMainActivity.class, true); // FIXME
						} else {
							Uitils.toActivity(Splash.this, Sign.class, true);
						}
					}
				});
	}
}
