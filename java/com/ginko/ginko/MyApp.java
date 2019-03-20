package com.ginko.ginko;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.activity.im.EmoticonUtility;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImDownloadManager;
import com.ginko.activity.sprout.MyGinkoMeActivity;
import com.ginko.activity.sprout.SproutSearchItem;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.SpoutRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.context.CrashHandler;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ChatTableModel;
import com.ginko.database.ContactStruct;
import com.ginko.database.ContactTableModel;
import com.ginko.database.GinkoMeStruct;
import com.ginko.database.GinkoTableModel;
import com.ginko.service.SproutService;
import com.ginko.setup.GetStart;
import com.ginko.vo.CandidateEachVO;
import com.ginko.vo.EventUser;
import com.ginko.vo.HashMapVO;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.SdpMainVO;
import com.ginko.vo.UserLoginVO;
import com.ginko.vo.VideoMemberVO;

import org.apache.log4j.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class MyApp extends Application {
    public static final String APP_NAME = "Ginko";
    public static Context appContext;
    private static EmoticonUtility emoticons;
    private static MyApp me;
    private ImDownloadManager downloadManager;
    private Activity mCurrentActivity = null;
    private ImageLoader mImageLoader;
    private LruBitmapCache mLruBitmapCache;
    private RequestQueue mRequestQueue;

    private List<Typeface> faces ;

    public static String currentTakePhotoTitle = "Home Info";

    public static MyApp getInstance() {
		return me;
	}
    public static Context getContext()
    {
        return MyApp.appContext;
    }

    public static ContactTableModel g_contactDbModel = null;
    public static ChatTableModel g_chatDbModel = null;
    public static GinkoTableModel g_ginkoDbModel = null;

    public static List<ContactItem> g_contactItems = null;
    public static Set<Integer> g_contactIDs = null;

    public static List<String> userIdsForSDP = null;
    public static List<String> userIdsForCandidate = null;
    public static List<String> userIdsForOlder = null;

    public static List<JSONObject> tempObjects = null;

    public boolean isOwnerForConfernece = false;
    public boolean isJoinedOnConference = false;
    public boolean isReceiverForConferenceSDP = false;
    public boolean isReceiverForConferenceCandidate = false;
    public boolean isIncomingOnNow = false;

    public static List<VideoMemberVO> g_videoMemberList = null;
    public static List<String> g_videoMemIDs = null;
    public static VideoMemberVO g_currMemberCon = null;
    public static Bundle incomingData = null;

    public static String g_strPackageName = "com.ginko.ginko";

    public Integer g_userId;
    public boolean isBackground = false;
    public boolean isSortableName = false;
    public boolean mMapDoubleTouched = false;

    public boolean isSearched = false;
    public int g_StartActivity = 0;
    public int g_currentBoardId = 0;

    //private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;
    private SproutService sproutService = null;
    private Handler mHandler = new Handler();

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

    Foreground.Listener myListener = new Foreground.Listener() {
        @Override
        public void onBecameForeground() {
            if(isBackground == true)
            {
                SpoutRequest.switchLocationStatus(true, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            isBackground = false;
                        }
                    }
                });
            }
        }

        @Override
        public void onBecameBackground() {
            if(RuntimeContext.getUser() != null)
            {
                if (RuntimeContext.getUser().getLocationOn() == true)
                {
                    SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                //RuntimeContext.getUser().setLocationOn(false);
                                Uitils.storeSproutStartTime(getContext(), 0);
                                isBackground = true;
                            }
                        }
                    });
                }
            }
        }
    };

    @Override
	public void onCreate() {
		super.onCreate();

        //mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        //Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());

        initLog4j();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

        isBackground = false;
        Foreground.get(this).addListener(myListener);

        g_strPackageName = getApplicationContext().getPackageName();

		if (ConstValues.DEBUG) {
			String sessionId = ConstValues.sessionId;
			int userId = ConstValues.userId;

			Uitils.storeSessionid(this, sessionId);
			RuntimeContext.setSessionId(sessionId);

			UserLoginVO user = new UserLoginVO();
			user.setUserId(userId);
            user.setPhotoUrl("http://www.xchangewith.me/api/v2/files/tc/images/user_507/profile_1405527466487-4J2F8.");
			RuntimeContext.setUser(user);
		}


//		new Thread(){
//			public void run(){
//				DataCache.refreshCacheData();
//			}
//		}.start();

		me = this;
        MyApp.appContext = getApplicationContext();

        this.emoticons= new EmoticonUtility(getApplicationContext());

        initializeGlobalVariables();
        initializeVideoVariables();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
	}

    @Override
    public void onLowMemory() {
        Log.i("MyApp", "Low Memory");
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        Log.i("MyApp", "Teriminate");
        Foreground.get(this).removeListener(myListener);
        super.onTerminate();
    }

    public List<Typeface> getFontFaces()
    {
        if(faces == null) {
            AssetManager assetManager = getApplicationContext().getAssets();
            faces = new ArrayList<Typeface>();
            for(int i=0;i<ConstValues.fontNamesArray.length;i++)
            {
                Typeface face=Typeface.createFromAsset(assetManager ,
                        "fonts/"+ConstValues.fontNamesArray[i]+".ttf");
                faces.add(face);
            }
        }
        return  faces;
    }

    public void startSproutService()
    {
        if(!isMyServiceRunning(SproutService.class))
        {
            Intent sproutServiceIntent = new Intent(getApplicationContext() , SproutService.class);
            getApplicationContext().startService(sproutServiceIntent);
        }
    }
    public void stopSproutService()
    {
        if(isMyServiceRunning(SproutService.class))
        {
            Intent sproutServiceIntent = new Intent(getApplicationContext() , SproutService.class);
            getApplicationContext().stopService(sproutServiceIntent);
        }
    }
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

    }

    /*@Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }*/

    private void initLog4j(){
        String logFolder = RuntimeContext.getLogerFolder();
        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(logFolder + "/log.txt");
        // Set the root log level
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setMaxBackupSize(5);
        logConfigurator.setFilePattern("%d - [%p] - %m%n");
//        logConfigurator.setMaxFileSize(10);
        // Set log level of a specific logger
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.configure();
    }

    public int getCurrentBoardId() {
        int id = Uitils.getIntFromSharedPreferences(getApplicationContext(), "board_id", 0);
        if(id>0)
            return id;
        else
            return 0;
    }
    public void setCurrentBoardId(int currentId) {
        this.g_currentBoardId = currentId;
        Uitils.setIntToSharedPreferences(getApplicationContext(), "board_id", currentId);
    }

	public Activity getCurrentActivity() {
		return mCurrentActivity;
	}

	public void setCurrentActivity(Activity mCurrentActivity) {
		this.mCurrentActivity = mCurrentActivity;
	}

    public void hideKeyboard(ViewGroup rootLayout)
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootLayout.getApplicationWindowToken(), 0);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public Integer getUserId()
    {
        int id = Uitils.getIntFromSharedPreferences(getApplicationContext(), "user_id", 0);
        if(id>0)
            return new Integer(id);
        else
            return null;
    }
    public void setUserId(Integer id)
    {
        this.g_userId = id;
        if(id == null)
            Uitils.setIntToSharedPreferences(getApplicationContext() , "user_id" , 0);
        else
            Uitils.setIntToSharedPreferences(getApplicationContext() , "user_id" , id.intValue());
    }

    public ContactTableModel getContactsModel()
    {
        if(this.g_contactDbModel == null)
        {
            if(this.g_userId == null)
                this.g_userId = getUserId();
            if(this.g_userId != null && this.g_userId>0)
            {
                this.g_contactDbModel = ContactTableModel.getInstance(getApplicationContext() , "ContactsDB"+String.valueOf(g_userId));
            }
            else
                this.g_contactDbModel = null;
        }

        return this.g_contactDbModel;
    }

    public synchronized  ChatTableModel getChatDBModel()
    {
        if(this.g_chatDbModel == null)
        {
            if(this.g_userId == null)
                this.g_userId = getUserId();
            if(this.g_userId>0)
            {
                this.g_chatDbModel = ChatTableModel.getInstance(getApplicationContext() , "MessageBoardDB"+String.valueOf(g_userId));
            }
            else
                this.g_chatDbModel = null;
        }

        return this.g_chatDbModel;
    }

    public GinkoTableModel getGinkoModel()
    {
        if(this.g_ginkoDbModel == null)
        {
            if(this.g_userId == null)
                this.g_userId = getUserId();
            if(this.g_userId>0)
            {
                this.g_ginkoDbModel = GinkoTableModel.getInstance(getApplicationContext() , "GinkoDB"+String.valueOf(g_userId));
            }
            else
                this.g_ginkoDbModel = null;
        }

        return this.g_ginkoDbModel;
    }

    public synchronized void initializeGlobalVariables()
    {
        this.g_userId = getUserId();
        if(g_userId == null) return;

        if(g_contactDbModel == null)
            g_contactDbModel = new ContactTableModel(this , "ContactsDB"+String.valueOf(g_userId));

        if(g_chatDbModel== null)
            this.g_chatDbModel = ChatTableModel.getInstance(getApplicationContext() , "MessageBoardDB"+String.valueOf(g_userId));

        if(g_ginkoDbModel == null)
            g_ginkoDbModel = new GinkoTableModel(this, "GinkoDB"+String.valueOf(g_userId));

        if(g_contactItems == null) {
            g_contactItems = new ArrayList<ContactItem>();
            g_contactIDs = new HashSet<Integer>();
        }

        if (tempObjects == null)
            tempObjects = new ArrayList<JSONObject>();
    }

    public void setGroupListData(List<JSONObject> data)
    {
        this.tempObjects = data;
    }

    public synchronized void clearGlobalVariables()
    {
        setUserId(null);

        //close all database
        if(this.g_contactDbModel != null) {
            this.g_contactDbModel.closeDB();
            ContactTableModel.clearInstance();
            this.g_contactDbModel = null;
        }

        if(this.g_chatDbModel != null) {
            this.g_chatDbModel.closeDB();
            ChatTableModel.clearInstance();
            this.g_chatDbModel = null;
        }

        if(this.g_ginkoDbModel != null) {
            this.g_ginkoDbModel.closeDB();
            GinkoTableModel.clearInstance();
            this.g_ginkoDbModel = null;
        }

        this.tempObjects = null;
        this.g_contactItems = null;
        this.g_contactIDs = null;
     }

    public synchronized void initializeVideoVariables()
    {
        if (g_videoMemberList == null)
            g_videoMemberList = new ArrayList<VideoMemberVO>();
        else
            g_videoMemberList.clear();

        if (g_videoMemIDs == null)
            g_videoMemIDs = new ArrayList<String>();
        else
            g_videoMemIDs.clear();

        if (userIdsForSDP == null)
            userIdsForSDP = new ArrayList<String>();
        else
            userIdsForSDP.clear();

        if (userIdsForCandidate == null)
            userIdsForCandidate = new ArrayList<String>();
        else
            userIdsForCandidate.clear();

        if (userIdsForOlder == null)
            userIdsForOlder = new ArrayList<String>();
        else
            userIdsForCandidate.clear();

        incomingData = new Bundle();
    }

    public void initIncomeBundles()
    {
        if (incomingData == null)
            incomingData = new Bundle();
    }

    public void clearVideoVariables() {
        this.g_videoMemberList.clear();
        this.g_videoMemIDs.clear();

        g_videoMemberList = null;
        g_videoMemIDs = null;

        this.g_currMemberCon = null;

        userIdsForSDP.clear();
        userIdsForCandidate.clear();
        userIdsForOlder.clear();

        userIdsForSDP = null;
        userIdsForCandidate = null;
        userIdsForOlder = null;

        isOwnerForConfernece = false;
        isJoinedOnConference = false;
        isReceiverForConferenceSDP = false;
        isReceiverForConferenceCandidate = false;
    }

    public void removeVideoMembersByid(String memberId)
    {
        if (g_videoMemIDs == null || g_videoMemIDs.size() < 1)
            return;

        for (int i=0; i<g_videoMemberList.size(); i++)
        {
            VideoMemberVO currOne = g_videoMemberList.get(i);
            if (currOne.getUserId().equals(memberId)) {
                g_videoMemberList.remove(i);
                g_videoMemIDs.remove(memberId);
                break;
            }
        }

        if (userIdsForCandidate != null && userIdsForCandidate.contains(memberId))
            userIdsForCandidate.remove(memberId);
        if (userIdsForSDP != null && userIdsForSDP.contains(memberId))
            userIdsForSDP.remove(memberId);
        if (userIdsForOlder != null && userIdsForOlder.contains(memberId))
            userIdsForOlder.remove(memberId);
    }

    public String getBoardName()
    {
        if (g_videoMemberList == null)
            return  "";

        int count = 0;
        String boardName = "";
        for (int i = 0; i < g_videoMemberList.size(); i++) {
            VideoMemberVO member = g_videoMemberList.get(i);
            if (member.getUserId().equals(String.valueOf(RuntimeContext.getUser().getUserId())))
                continue;
            if (boardName.equals(""))
                boardName = member.getName();
            count++;
        }

        if (!boardName.equals(""))
        {
            if (count > 1) {
                count--;
                boardName = boardName + "+" + String.valueOf(count);
            }
        }

        return boardName;
    }

    public void addOtherMembers(String invitedIds, String userInfo, String senderId, int callType, String inviteName)
    {
        List<String> idsOfConference =  Arrays.asList(invitedIds.split(","));
        List<HashMapVO> userArray = new ArrayList<>();
        userArray = parseUserInfo(userInfo);

        VideoMemberVO memSender = new VideoMemberVO();
        memSender.setUserId(senderId);
        memSender.setOwner(true);
        memSender.setMe(false);
        memSender.setWeight(0);
        memSender.setInitialized(true);
        memSender.setOlder(true);
        memSender.setInvitedByMe(false);

        if (Integer.valueOf(callType) == 1)
            memSender.setVideoStatus(true);
        else
            memSender.setVideoStatus(false);
        memSender.setVoiceStatus(true);
        memSender.setName(inviteName);
        g_videoMemberList.add(memSender);
        g_videoMemIDs.add(memSender.getUserId());

        int userId = RuntimeContext.getUser().getUserId();
        for (int k = 0; k < userArray.size(); k++) {

            Integer memberId = userArray.get(k).getId();
            String imageUrl = userArray.get(k).getPhotoUrl();

            if (memberId.equals(Integer.valueOf(senderId))) {
                memSender.setImageUrl(imageUrl);
                continue;
            }

            if (memberId.equals(RuntimeContext.getUser().getUserId()))
                continue;

            VideoMemberVO memUsers = new VideoMemberVO();
            memUsers.setUserId(String.valueOf(memberId));
            memUsers.setImageUrl(imageUrl);
            memUsers.setOwner(false);
            memUsers.setMe(false);
            memUsers.setWeight(k + 1);
            memUsers.setInitialized(true);
            memUsers.setInvitedByMe(false);

            if (!idsOfConference.contains(String.valueOf(memberId)))
                memUsers.setOlder(true);
            else {
                if (memberId >= userId)
                    memUsers.setYounger(true);
                else
                    memUsers.setOlder(true);
            }

            if (Integer.valueOf(callType) == 1)
                memUsers.setVideoStatus(true);
            else
                memUsers.setVideoStatus(false);
            memUsers.setVoiceStatus(true);

            memUsers.setName(userArray.get(k).getName());
            g_videoMemberList.add(memUsers);
            g_videoMemIDs.add(memUsers.getUserId());
        }
    }

    public EmoticonUtility getEmoticonUtility()
    {
        if(this.emoticons == null)
        {
            this.emoticons = new EmoticonUtility(getApplicationContext());
        }
        this.emoticons.waitForLoad();
        return this.emoticons;
    }
    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();
        return this.mLruBitmapCache;
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public ImDownloadManager getDownloadManager()
    {
        if(this.downloadManager == null)
        {
            this.downloadManager = new ImDownloadManager();
        }
        return this.downloadManager;
    }


    public void showSimpleAlertDiloag(Context context ,int paramInt , DialogInterface.OnClickListener clickListener)
    {
        final DialogInterface.OnClickListener okButtonClickListener = clickListener;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(paramInt)).setCancelable(false);
        DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int paramAnonymousInt)
            {
                if(okButtonClickListener!=null)
                    okButtonClickListener.onClick(dialog , paramAnonymousInt);
                dialog.dismiss();
            }
        };
        builder.setPositiveButton("OK", local1);
        builder.create().show();
    }
    public void showSimpleAlertDiloag(Context context ,String title , int paramInt , final DialogInterface.OnClickListener clickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(context.getResources().getString(paramInt)).setCancelable(false);
        DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int paramAnonymousInt)
            {
                if(clickListener!=null)
                    clickListener.onClick(dialog , paramAnonymousInt);
                dialog.dismiss();
            }
        };
        builder.setPositiveButton("OK", local1);
        builder.create().show();
    }
    public void showSimpleAlertDiloag(Context context ,String strAlertMessage, final DialogInterface.OnClickListener clickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(strAlertMessage).setCancelable(false);
        DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int paramAnonymousInt)
            {
                if(clickListener!=null)
                    clickListener.onClick(dialog , paramAnonymousInt);
                dialog.dismiss();
            }
        };
        builder.setPositiveButton("OK", local1);
        builder.create().show();
    }
    public boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void getAllContactItemsFromDatabase()
    {
        g_contactItems = new ArrayList<ContactItem>();
        g_contactIDs = new HashSet<Integer>();

        //open database
        this.g_contactDbModel = getContactsModel();

        List<ContactStruct> contactStructs = this.g_contactDbModel.getAllContactItems();
        for(ContactStruct contactStruct : contactStructs)
        {
            if(contactStruct.getContactItem() == null) continue;
            g_contactItems.add(contactStruct.getContactItem());
            g_contactIDs.add(contactStruct.getContactOrEntityId());
        }
    }

    public ContactItem getfromContacts(int contactID)
    {
        if (g_contactItems == null)
            return null;

        for(int j=0;j<g_contactItems.size();j++)
            if (g_contactItems.get(j).getContactId() == contactID) {
                return g_contactItems.get(j);
            }

        return null;
    }

    public void removefromContacts(int contactID)
    {
        if (g_contactItems == null)
            return;

        for(int j=0;j<g_contactItems.size();j++)
        {
            if(g_contactItems.get(j).getContactId() == contactID)
            {
                g_contactItems.remove(j);
                g_contactIDs.remove(contactID);
                break;
            }
        }
    }

    private List<HashMapVO> parseUserInfo(String parseData)
    {
        List<HashMapVO> arrList = new ArrayList<>();
        if (parseData.equals(""))
            return null;

        try {
            JSONArray newJson = new JSONArray(parseData);
            if (newJson.length() < 1)
                return null;

            for (int i = 0; i < newJson.length(); i++) {
                JSONObject jsonOne = (JSONObject) newJson.get(i);
                HashMapVO eachOne = new HashMapVO();

                eachOne.setId(jsonOne.getInt("id"));
                eachOne.setName(jsonOne.getString("name"));
                eachOne.setPhotoUrl(jsonOne.getString("photo_url"));
                arrList.add(eachOne);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrList;
    }

    public synchronized void getSyncUpdatedContacts(long lastSyncTime)
    {
        g_contactDbModel = getContactsModel();
        if (g_contactDbModel == null) return;


        UserRequest.getSyncUpdatedContacts(String.valueOf(lastSyncTime), new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject resultObj = response.getData();
                    try {
                        //store updated contact into contacts table
                        //remove the deleted contacts
                        JSONArray removedContactsArray = resultObj.getJSONArray("removed_contacts");
                        String strRemovedId = "";

                        ArrayList<ContactStruct> removecContacts = new ArrayList<ContactStruct>();
                        for (int i = 0; i < removedContactsArray.length(); i++) {
                            JSONObject removedContact = removedContactsArray.getJSONObject(i);
                            int contactType = removedContact.optInt("contact_type");
                            int contactOrEntityId = removedContact.optInt("contact_id");
                            g_contactDbModel.deleteContactWithContactId(contactOrEntityId);
                            ContactStruct removedContactItem = new ContactStruct();
                            removedContactItem.setContactOrEntityId(contactOrEntityId);
                            removecContacts.add(removedContactItem);

                            strRemovedId = strRemovedId + String.valueOf(contactOrEntityId) + ",";
                        }


                        //add or udpate new contacts
                        ArrayList<ContactStruct> newContacts = new ArrayList<ContactStruct>();
                        JSONArray newContactsArray = resultObj.getJSONArray("contacts");
                        for (int i = 0; i < newContactsArray.length(); i++) {
                            JSONObject jsonObject = newContactsArray.getJSONObject(i);
                            try {
                                int contactType = jsonObject.optInt("contact_type", 1);
                                if (contactType == 1 || contactType == 2)//purple contact or grey contact
                                {
                                    ContactStruct contactStruct = new ContactStruct();
                                    contactStruct.setFirstName(jsonObject.optString("first_name", ""));
                                    contactStruct.setLastName(jsonObject.optString("last_name", ""));
                                    contactStruct.setContactType(contactType);
                                    contactStruct.setContactOrEntityId(jsonObject.optInt("contact_id"));
                                    contactStruct.setJsonValue(jsonObject.toString());
                                    JSONObject obj = new JSONObject(contactStruct.getJsonValue());
                                    if (contactType == 1)
                                        contactStruct.setContactItem(ContactTableModel.parsePurpleContact(obj));
                                    else if (contactType == 2)
                                        contactStruct.setContactItem(ContactTableModel.parseGreyContact(obj));
                                    if (contactStruct.getContactItem() != null) {
                                        newContacts.add(contactStruct);
                                        g_contactDbModel.add(contactStruct);
                                    }
                                } else//entity
                                {
                                    ContactStruct contactStruct = new ContactStruct();
                                    contactStruct.setFirstName(jsonObject.optString("name", ""));
                                    contactStruct.setLastName("");
                                    contactStruct.setContactType(contactType);
                                    contactStruct.setContactOrEntityId(jsonObject.optInt("entity_id"));
                                    contactStruct.setJsonValue(jsonObject.toString());
                                    JSONObject obj = new JSONObject(contactStruct.getJsonValue());
                                    contactStruct.setContactItem(ContactTableModel.parseEntityContact(obj));
                                    if (contactStruct.getContactItem() != null) {
                                        newContacts.add(contactStruct);
                                        g_contactDbModel.add(contactStruct);
                                    }
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (g_contactItems == null) {
                            g_contactItems = new ArrayList<ContactItem>();
                            g_contactIDs = new HashSet<Integer>();
                            getAllContactItemsFromDatabase();
                        } else {
                            boolean hasNullElementInArray = false;
                            for (int i = 0; i < g_contactItems.size(); i++) {
                                if (g_contactItems.get(i) == null) {
                                    hasNullElementInArray = true;
                                    break;
                                }
                            }

                            if (hasNullElementInArray)
                                getAllContactItemsFromDatabase();

                            //remove contacts from global variable of contacts array
                            for (int i = 0; i < removecContacts.size(); i++) {
                                if (g_contactIDs.contains(removecContacts.get(i).getContactOrEntityId())) {
                                    ContactStruct removedContactItem = removecContacts.get(i);
                                    for (int j = 0; j < g_contactItems.size(); j++) {
                                        if (g_contactItems.get(j).getContactId() == removedContactItem.getContactOrEntityId()) {
                                            g_contactItems.remove(j);
                                            g_contactIDs.remove(new Integer(removedContactItem.getContactOrEntityId()));
                                            break;
                                        }
                                    }
                                }
                            }

                            //add new contacts to global variable
                            for (int i = 0; i < newContacts.size(); i++) {
                                //if new contact is already exist then update
                                if (g_contactIDs.contains(newContacts.get(i).getContactOrEntityId())) {
                                    for (int j = 0; j < g_contactItems.size(); j++) {
                                        if (g_contactItems.get(j).getContactId() == newContacts.get(i).getContactOrEntityId()) {
                                            g_contactItems.remove(j);
                                            break;
                                        }
                                    }
                                    g_contactItems.add(newContacts.get(i).getContactItem());
                                    g_contactIDs.add(newContacts.get(i).getContactOrEntityId());
                                } else {
                                    g_contactItems.add(newContacts.get(i).getContactItem());
                                    g_contactIDs.add(newContacts.get(i).getContactOrEntityId());
                                }
                            }
                        }

                        //store new sync timestamp
                        long newSyncTimeStamp = resultObj.optLong("new_sync_timestamp");
                        g_userId = getUserId();
                        if (g_userId != null) {
                            Uitils.storeLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId(), newSyncTimeStamp);
                            UserRequest.receivedUpdatedContacts(String.valueOf(newSyncTimeStamp), new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                }
                            }, false);
                        }

                        //if some contacts were changed

                        if (newContacts.size() > 0 || removecContacts.size() > 0) {
                            //send message to notify main contact screen
                            Intent intent1 = new Intent();
                            intent1.putExtra("removed_id", strRemovedId);
                            intent1.setAction("android.intent.action.CONTACT_CHANGED");
                            sendBroadcast(intent1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }, false);

    }

    public void SyncUpdateEntities(final int newSprout, final Context mContext) {
        EntityRequest.syncEntityUpdated(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject resultObj = response.getData();
                    try {
                        g_ginkoDbModel = getGinkoModel();
                        if (g_ginkoDbModel == null) return;

                        JSONArray removedContactsArray = resultObj.getJSONArray("removed_entities");

                        for (int i = 0; i < removedContactsArray.length(); i++) {
                            int contactOrEntityId = Integer.valueOf(removedContactsArray.getString(i));
                            g_ginkoDbModel.deleteContactWithContactId(contactOrEntityId);
                        }

                        //add or udpate new contacts
                        ArrayList<GinkoMeStruct> newContacts = new ArrayList<GinkoMeStruct>();
                        JSONArray newContactsArray = resultObj.getJSONArray("entities");
                        for (int i = 0; i < newContactsArray.length(); i++) {
                            try {
                                JSONObject jsonObject = newContactsArray.getJSONObject(i);
                                JSONArray locationArray = jsonObject.getJSONArray("locations");
                                g_ginkoDbModel.deleteContactWithContactId(jsonObject.optInt("entity_id"));

                                for (int j = 0; j < locationArray.length(); i++) {
                                    GinkoMeStruct ginkoMeStruct = new GinkoMeStruct();
                                    ginkoMeStruct.setContactOrEntityID(jsonObject.optInt("entity_id"));
                                    ginkoMeStruct.setEntityOrContactName(jsonObject.optString("name", ""));

                                    JSONObject location = locationArray.getJSONObject(i);
                                    ginkoMeStruct.setLat(location.optDouble("latitude", 0.0d));
                                    ginkoMeStruct.setLng(location.optDouble("longitude", 0.0d));
                                    g_ginkoDbModel.add(ginkoMeStruct);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Intent sproutIntent = new Intent(mContext, MyGinkoMeActivity.class);
                sproutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (newSprout > 0)
                    sproutIntent.putExtra("isDetectedContacts", true);
                else
                    sproutIntent.putExtra("isDetectedContacts", false);
                sproutIntent.putExtra("isNewDetection", false);
                startActivity(sproutIntent);
            }
        });
    }

    public void loadContacts(final int method, final Context context)
    {
        long lastSyncTimeStamp = Uitils.getLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId());

        if (lastSyncTimeStamp == 0)
        {
            UserRequest.getContacts(null, null, null, new ResponseCallBack<List<JSONObject>>() {

                @Override
                public void onCompleted(JsonResponse<List<JSONObject>> response) {
                    if (response.isSuccess()) {
                        List<JSONObject> contacts = response.getData();
                        List<ContactItem> contactList = new ArrayList<ContactItem>();

                        ArrayList<ContactStruct> contactStructs = new ArrayList<ContactStruct>();

                        for (JSONObject jsonObject : contacts) {
                            try {
                                int contactType = jsonObject.optInt("contact_type", 1);
                                ContactItem item = null;
                                if (contactType == 1)//purple contact
                                {
                                    item = ContactTableModel.parsePurpleContact(jsonObject);
                                    if(item != null)
                                        contactList.add(item);

                                } else if (contactType == 2)//grey contact
                                {
                                    item = ContactTableModel.parseGreyContact(jsonObject);
                                    if(item != null)
                                        contactList.add(item);

                                } else//entity
                                {
                                    item = ContactTableModel.parseEntityContact(jsonObject);
                                    if(item != null)
                                        contactList.add(item);
                                }
                                if(item != null) {
                                    ContactStruct contactStruct = new ContactStruct();
                                    contactStruct.setFirstName(item.getFirstName());
                                    contactStruct.setLastName(item.getLastName());
                                    contactStruct.setContactType(contactType);
                                    contactStruct.setContactOrEntityId(item.getContactId());
                                    contactStruct.setJsonValue(jsonObject.toString());
                                    contactStructs.add(contactStruct);
                                    MyApp.getInstance().getContactsModel().add(contactStruct);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //store all contacts to the database
                        //store login sync stamp time
                    }

                    Uitils.storeLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId(), System.currentTimeMillis());
                    UserRequest.receivedUpdatedContacts(String.valueOf(RuntimeContext.getUser().getSyncTimestamp()), new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                        }
                    }, false);

                    MyApp.getInstance().getAllContactItemsFromDatabase();
                    g_StartActivity++;
                    if (g_StartActivity == 2)
                    {
                        if (method == 1)
                            Uitils.toActivity(context, ContactMainActivity.class, true);
                        else if (method == 2)
                            Uitils.toActivity(context, GetStart.class, true);
                        else if (method == 3)
                            Uitils.toActivity(context, ContactMainActivity.class, true);
                        g_StartActivity = 0;
                    }
                }
            }, true);
        } else
        {
            new Thread(){
                @Override
                public void run()
                {
                    ContactTableModel contactsTableModel = MyApp.getInstance().getContactsModel();
                    if (contactsTableModel != null) {
                        boolean isExist = contactsTableModel.isTableExists();
                        if (isExist)
                        {
                            getAllContactItemsFromDatabase();
                            if (g_contactItems != null && g_contactIDs.size() != 0)
                            {
                                g_StartActivity++;
                                if (g_StartActivity == 2)
                                {
                                    if (method == 1)
                                        Uitils.toActivity(context, ContactMainActivity.class, true);
                                    else if (method == 2)
                                        Uitils.toActivity(context, GetStart.class, true);
                                    else if (method == 3)
                                        Uitils.toActivity(context, ContactMainActivity.class, true);
                                    g_StartActivity = 0;
                                }
                            } else
                            {
                                //if sometimes db data is lost or can't get data , then refersh syncTime
                                Uitils.storeLastSyncTime(getApplicationContext() , RuntimeContext.getUser().getUserId() , 0);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadContacts(method, context);
                                    }
                                });
                            }
                        } else {
                            Uitils.storeLastSyncTime(getApplicationContext() , RuntimeContext.getUser().getUserId() , 0);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadContacts(method, context);
                                }
                            });
                        }
                    }
                }
            }.start();

        }
    }

    public void fetchAllEntites(final int method, final Context context)
    {
        final GinkoTableModel ginkoTableModel = getGinkoModel();

        if (ginkoTableModel != null) {
            boolean isExist = ginkoTableModel.isTableExists();
            if (isExist)
            {
                List<GinkoMeStruct> ginkoList = new ArrayList<GinkoMeStruct>();
                ginkoList = ginkoTableModel.getAll();
                if (ginkoList != null && ginkoList.size() != 0)
                {
                    g_StartActivity++;
                    if (g_StartActivity == 2)
                    {
                        if (method == 1)
                            Uitils.toActivity(context, ContactMainActivity.class, true);
                        else if (method == 2)
                            Uitils.toActivity(context, GetStart.class, true);
                        else if (method == 3)
                            Uitils.toActivity(context, ContactMainActivity.class, true);
                        g_StartActivity = 0;
                    }
                    return;
                }
            }
        }

        //get all contacts
        EntityRequest.fetchAllEntities(new ResponseCallBack<List<JSONObject>>() {
            @Override
            public void onCompleted(JsonResponse<List<JSONObject>> response) {
                if (response.isSuccess()) {
                    List<JSONObject> data = response.getData();

                    ArrayList<GinkoMeStruct> ginkoMeStructs = new ArrayList<GinkoMeStruct>();
                    for (int index = 0; index < data.size(); index++) {
                        try {
                            JSONObject object = data.get(index);
                            JSONArray jsonArray = object.getJSONArray("locations");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                SproutSearchItem sproutSearchItem_child = new SproutSearchItem();
                                sproutSearchItem_child.contactType = 3;
                                sproutSearchItem_child.isOnlyEntity = true;
                                sproutSearchItem_child.jsonObject = object;

                                sproutSearchItem_child.contactOrEntityID = object.optInt("entity_id", -1);
                                sproutSearchItem_child.entityOrContactName = object.optString("name", "");
                                sproutSearchItem_child.nEnityFollowerCount = 0;
                                sproutSearchItem_child.isFollowed = false;

                                sproutSearchItem_child.profile_image = object.optString("profile_image", "");
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                sproutSearchItem_child.lat = jsonObject.optDouble("latitude", 0.0d);
                                sproutSearchItem_child.lng = jsonObject.optDouble("longitude", 0.0d);

                                if (sproutSearchItem_child != null) {
                                    GinkoMeStruct ginkoStruct = new GinkoMeStruct();
                                    ginkoStruct.setContactOrEntityID(sproutSearchItem_child.contactOrEntityID);
                                    ginkoStruct.setEntityOrContactName(sproutSearchItem_child.entityOrContactName);
                                    ginkoStruct.setProfileImage(sproutSearchItem_child.profile_image);
                                    ginkoStruct.setLat(sproutSearchItem_child.lat);
                                    ginkoStruct.setLng(sproutSearchItem_child.lng);
                                    ginkoMeStructs.add(ginkoStruct);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    MyApp.getInstance().getGinkoModel().addAll(ginkoMeStructs);
                } else {
                    MyApp.getInstance().showSimpleAlertDiloag(getCurrentActivity(), "Failed fetch All info of Entities.", null);
                }

                g_StartActivity++;
                if (g_StartActivity == 2) {
                    if (method == 1)
                        Uitils.toActivity(context, ContactMainActivity.class, true);
                    else if (method == 2)
                        Uitils.toActivity(context, GetStart.class, true);
                    else if (method == 3)
                        Uitils.toActivity(context, ContactMainActivity.class, true);
                    g_StartActivity = 0;
                }
            }
        });
    }

    /*
    private String getStackTrace(Throwable th) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
    */

    /*
    class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler{

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {

            //Working on Unexpected Exception

            SpoutRequest.switchLocationStatus(false, new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if (response.isSuccess()) {
                        RuntimeContext.getUser().setLocationOn(false);
                        Uitils.storeSproutStartTime(MyApp.getContext(), 0);
                    }
                }
            });
            mUncaughtExceptionHandler.uncaughtException(thread, ex);
        }
    }
    */
}