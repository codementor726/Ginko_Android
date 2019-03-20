/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ginko.ginko;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.directory.DirAdminPreviewActivity;
import com.ginko.activity.entity.ViewEntityPostsActivity;
import com.ginko.activity.exchange.ExchangeRequestActivity;
import com.ginko.activity.group.GroupMainActivity;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.im.IncomingCallActivity;
import com.ginko.activity.sprout.GinkoMeActivity;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.setup.Splash;
import com.ginko.vo.DirectoryVO;
import com.ginko.vo.EventUser;
import com.ginko.vo.HashMapVO;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.VideoMemberVO;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    private KeyguardManager km = null;
    private KeyguardManager.KeyguardLock keyLock = null;

    public int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    private Uri chatMessageSoundUri = null ;
    private Uri ginkomeSoundUri = null;
    private PendingIntent contentIntent;

    private String Type = "";
    public GcmIntentService() {

        super("GcmIntentService");

        chatMessageSoundUri = Uri.parse("android.resource://" + MyApp.g_strPackageName + "/" + R.raw.chatmessage);
        ginkomeSoundUri = Uri.parse("android.resource://" + MyApp.g_strPackageName+ "/" + R.raw.ginkome);
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        String session_Id = Uitils.getStringFromSharedPreferences(this, "sessionId", "");
        if(session_Id.equals("") || session_Id.isEmpty())
            return;
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        Type = extras.getString("type");
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString() , null , 0);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString() , null , 0);
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
            	broadcast(extras);

                // Post notification of received message.
                String alert = extras.getString("alert");
                String sound = extras.getString("sound" , "");
                int boardId = 0;
                try {
                    if("entity_msg".equals(Type))  //Entity
                        boardId = Integer.valueOf(extras.getString("entity_id", ""));
                    else if ("video_call".equals(Type)) //VideoCall
                        boardId = Integer.valueOf(extras.getString("board_id", ""));
                    else if("gps_contact".equals(Type))  //GinkoMe
                        boardId = Integer.valueOf(extras.getString("contact_id", ""));
                    else if("im".equals(Type) || "video_call".equals(Type))  //Chat board
                        boardId = Integer.valueOf(extras.getString("board_id", ""));
                    else if ("directory".equals(Type))
                        boardId = Integer.valueOf(extras.getString("id", ""));
                    else if ("recieved_exchange_request".equals(Type)) {
                        boardId = Integer.valueOf(extras.getString("contact_uid", "0"));
                        if (boardId == 0)
                            boardId = Integer.valueOf(extras.getString("request_id", "0"));
                    }
                    else
                        boardId = Integer.valueOf(extras.getString("contact_id", ""));

                    if ("recieved_exchange_request".equals(Type))
                        NOTIFICATION_ID = 1;
                    else
                        NOTIFICATION_ID = 2;
                }catch(Exception e)
                {
                    e.printStackTrace();
                    boardId = 0;
                }
                if (StringUtils.isNotBlank(alert) && "video_call".equals(Type) == false && MyApp.getInstance().isJoinedOnConference == false){
                    sendNotification(alert , sound , boardId);
                } /// Add by lee for notification of GinkoMe.
                else if(boardId > 0 && "gps_contact".equals(Type)){
                    //sendNotification("Detected new contact", "ginkome.wav", boardId);
                }
                ///////////////////////////////////////////
                Logger.info("tokeninfo: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

	private void broadcast(Bundle extras) {
		Intent intent1=new Intent();
		String type = extras.getString("type");
        Log.d("tokeninfo", "type=" + type);
		if (StringUtils.equalsIgnoreCase(type, "im")){
//			intent1.putExtra("i", "");
            //sample
            //Bundle[{from=187384323103, type=im, alert=Asa bb: f, sound=chatmessage.wav, board_id=695, android.support.content.wakelockid=1, collapse_key=do_not_collapse}]
			intent1.setAction("android.intent.action.IM_NEW_MSG");//action���������ͬ
            String msg = extras.getString("alert", "");

            if (msg.equals("")) return;

            intent1.putExtra("message", msg.split(":")[1]);
            intent1.putExtra("uid", extras.getString("uid", "0"));
			sendBroadcast(intent1);
		}
        else if(StringUtils.equalsIgnoreCase(type, "entity_msg"))
        {
            intent1.setAction("android.intent.action.ENTITY_NEW_MSG");//action���������ͬ
            intent1.putExtra("entity_id" , extras.getString("entity_id" , "0"));
            sendBroadcast(intent1);
        }
        else if (StringUtils.equalsIgnoreCase(type, "DETECTED_NEW_USER")){
//			intent1.putExtra("i", "");
            int contact_id = extras.getInt("contact_id");
            intent1.putExtra("contact_id", extras.getString("contact_id", "0"));
			intent1.setAction("android.intent.action.DETECTED_NEW_USER");
			sendBroadcast(intent1);
		}
        else if(StringUtils.equalsIgnoreCase(type , "CONTACT_CHANGED"))
        {
            if(RuntimeContext.getUser()!=null) {
                MyApp.getInstance().getSyncUpdatedContacts(Uitils.getLastSyncTime(getApplicationContext(), RuntimeContext.getUser().getUserId()));
            }
        }
        else if(StringUtils.equalsIgnoreCase(type , "recieved_exchange_request"))
        {
            intent1.setAction("android.intent.action.EXCHANGE_REQUEST");
            intent1.putExtra("contact_id", extras.getString("contact_uid", "0"));
            sendBroadcast(intent1);
        }
        else if (StringUtils.equalsIgnoreCase(type, "gps_contact")){
			intent1.putExtra("contact_id", extras.getString("contact_id", "0"));
			intent1.putExtra("visible", extras.getBoolean("visible", false));
            intent1.setAction("android.intent.action.DETECTED_GPS_CONTACT");
            sendBroadcast(intent1);
        }else if (StringUtils.equalsIgnoreCase(type, "entity_removed")){
            intent1.putExtra("entity_id", extras.getString("entity_id", "0"));
            intent1.setAction("android.intent.action.ENTITY_REMOVED");
            sendBroadcast(intent1);
        }else if (StringUtils.equalsIgnoreCase(type, "directory")) {
            String msg = extras.getString("alert");
            if (msg.contains("invited you to join the directory"))
            {
                intent1.setAction("android.intent.action.EXCHANGE_REQUEST");
                intent1.putExtra("contact_id", extras.getString("id", "0"));
                sendBroadcast(intent1);
            }
        }else if (StringUtils.equalsIgnoreCase(type, "video_call")) {
            String action = extras.getString("action", "");
            intent1.setAction("android.intent.action.VIDEO_REQUEST");
            intent1.putExtra("type", action);
            /* Incoming call Notification */
            if (action.equals("initial")) {
                openCallingVideoConference(extras);
            }
            /* Accept call Notification from Recipient */
            else if (action.equals("accept")) {
                intent1.putExtra("from", extras.getString("uid", ""));
                String memId = extras.getString("uid", "");
                /*
                if (MyApp.getInstance().isJoinedOnConference == false)
                {
                    if (MyApp.getInstance().userIdsForOlder != null)
                        MyApp.getInstance().userIdsForOlder.add(memId);
                }
                */
                sendBroadcast(intent1);
            } else if (action.equals("hangup")) {
                intent1.putExtra("from", extras.getString("uid", ""));
                intent1.putExtra("boardId", Integer.valueOf(extras.getString("board_id")));

                sendBroadcast(intent1);
            } else if (action.equals("inviting")) {
                if (MyApp.getInstance().g_videoMemberList == null) return;

                List<String> idsOfConference =  Arrays.asList(extras.getString("invited_uids").split(","));
                List<HashMapVO> userInfo = new ArrayList<>();
                userInfo = parseUserInfo(extras.getString("userInfo"));

                for (int i=0; i<idsOfConference.size(); i++) {
                    VideoMemberVO memUsers = new VideoMemberVO();
                    memUsers.setUserId(idsOfConference.get(i));
                    memUsers.setOwner(false);
                    memUsers.setMe(false);
                    memUsers.setYounger(true);
                    memUsers.setInitialized(true);
                    memUsers.setInvitedByMe(false);

                    if (extras.getString("callType").equals("1"))
                        memUsers.setVideoStatus(true);
                    else
                        memUsers.setVideoStatus(false);
                    memUsers.setVoiceStatus(true);

                    if (userInfo != null)
                    {
                        for (int j=0; j<userInfo.size(); j++)
                        {
                            HashMapVO userData = userInfo.get(j);
                            if (Integer.valueOf(idsOfConference.get(i)).equals(userData.getId()))
                                memUsers.setName(userData.getName());
                        }
                    }
                    else
                        memUsers.setName("XXX");

                    MyApp.getInstance().g_videoMemberList.add(memUsers);
                    MyApp.getInstance().g_videoMemIDs.add(memUsers.getUserId());
                }
                sendBroadcast(intent1);
            } else if (action.equals("sdp_available")) {
                intent1.putExtra("dataType", "sdp");
                intent1.putExtra("from", extras.getString("uid", ""));
                MyApp.getInstance().isReceiverForConferenceSDP = true;
                String userId = extras.getString("uid");
                if (MyApp.getInstance().userIdsForSDP != null)
                    MyApp.getInstance().userIdsForSDP.add(userId);
                sendBroadcast(intent1);
            } else if (action.equals("candidates_available")) {
                intent1.putExtra("dataType", "candidates");
                intent1.putExtra("from", extras.getString("uid", ""));
                String userId = extras.getString("uid");
                MyApp.getInstance().isReceiverForConferenceCandidate = true;
                if (MyApp.getInstance().userIdsForCandidate != null)
                    MyApp.getInstance().userIdsForCandidate.add(extras.getString("uid"));
                sendBroadcast(intent1);
            }  else if (action.equals("videooff")) {
                intent1.putExtra("from", extras.getString("uid", ""));
                intent1.putExtra("value", 2);
                sendBroadcast(intent1);
            } else if (action.equals("videoon")) {
                intent1.putExtra("from", extras.getString("uid", ""));
                intent1.putExtra("value", 1);
                sendBroadcast(intent1);
            } else if (action.equals("audiooff")) {
                intent1.putExtra("from", extras.getString("uid", ""));
                intent1.putExtra("value", 4);
                sendBroadcast(intent1);
            } else if (action.equals("audioon")) {
                intent1.putExtra("from", extras.getString("uid", ""));
                intent1.putExtra("value", 3);
                sendBroadcast(intent1);
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

    public void openCallingVideoConference(final Bundle Extras)
    {
        String strIds = "";
        strIds = strIds.format("%s,%s", Extras.getString("invited_uids"), Extras.getString("uid"));
        Integer boardId = Integer.valueOf(Extras.getString("board_id"));

        if (MyApp.getInstance().isJoinedOnConference == true)
        {
            IMRequest.setHangupVideo(boardId, 3, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                    if (response.isSuccess()) {
                    }
                }
            });
        }
        else
        {
            MyApp.getInstance().initializeVideoVariables();
            IMRequest.checkVideoCallDetail(boardId, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                    if (response.isSuccess()) {
                        try {
                            JSONObject data = response.getData();
                            int newId = data.getInt("board_id");
                            int newCallType = data.getInt("callType");
                            createMessageBoard(Extras, newId, newCallType);
                        } catch (JSONException e) {

                        }

                    } else {
                    }
                }
            });
        }
    }

    public String getBoardName(List<EventUser> lstUsers)
    {
        if (lstUsers == null)
            return  "";

        int count = 0;
        String boardName = "";
        for (int i = 0; i < lstUsers.size(); i++) {
            EventUser member = lstUsers.get(i);
            if (member.getUserId() == RuntimeContext.getUser().getUserId())
                continue;
            if (boardName.equals(""))
                boardName = member.getFirstName() + " " + member.getLastName();
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

    public void createMessageBoard(Bundle _infoCalling, int newBoardId, int newCallType)
    {
        final Integer boardId = newBoardId;
        final Integer callType = newCallType;
        final String inviteName = _infoCalling.getString("uname", "");
        final String invitedIds = _infoCalling.getString("invited_uids");
        final String userInfo = _infoCalling.getString("userInfo");
        final String senderId = _infoCalling.getString("uid");

        if (MyApp.getInstance().isIncomingOnNow == true)
        {
            IMRequest.setHangupVideo(boardId, 3, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                    if (response.isSuccess()) {
                    }
                }
            });
        } else
        {
            IMRequest.getGetBoardInfo(boardId, new ResponseCallBack<ImBoardVO>() {
                @Override
                public void onCompleted(JsonResponse<ImBoardVO> response) throws IOException {
                    if (response.isSuccess()) {
                        MyApp.getInstance().isOwnerForConfernece = false;
                        List<ImBoardMemeberVO> imBoardVO = response.getData().getMembers();
                        String boardName = response.getData().getBoardName();
                        ArrayList<EventUser> lstTemp = new ArrayList<EventUser>();

                        for (int i = 0; i < imBoardVO.size(); i++) {
                            lstTemp.add(imBoardVO.get(i).getUser());
                        }

                        if (boardName == null || boardName.equals(""))
                            boardName = getBoardName(lstTemp);

                        Intent notificationIntent = new Intent(getApplicationContext(), IncomingCallActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("boardId", boardId);
                        bundle.putInt("callType", callType);
                        bundle.putString("senderId", senderId);
                        bundle.putString("conferenceName", boardName);
                        bundle.putString("invited_uids", invitedIds);
                        bundle.putString("userInfo", userInfo);
                        bundle.putString("inviteName", inviteName);
                        bundle.putSerializable("userData", lstTemp);
                        bundle.putBoolean("isInitial", false);
                        notificationIntent.putExtras(bundle);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getApplicationContext().startActivity(notificationIntent);
                    }
                }
            }, false);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg , String sound , int boardId) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        //Add by lee for only current activity.  GAD-1089 and GAD-1051
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        // get the info from the currently running task
        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);
        /*ComponentName componentInfo = taskInfo.get(0).topActivity;
        componentInfo.getPackageName();*/

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        contentIntent = null;
        if(boardId > 0)//if chat message
        {
            if("entity_msg".equals(Type)){
                Intent notificationIntent = new Intent(getApplicationContext(), ViewEntityPostsActivity.class);
                notificationIntent.putExtra("entityName", msg.split(":")[0]);
                notificationIntent.putExtra("entityId", boardId);
                notificationIntent.putExtra("isfollowing_entity", true);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            else if("gps_contact".equals(Type)){
                Intent notificationIntent = new Intent(getApplicationContext(), GinkoMeActivity.class);
                notificationIntent.putExtra("isNewDetection", false);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            else if ("directory".equals(Type)) {
                if (msg.contains("invited you to join the directory"))
                {
                    Intent notificationIntent = new Intent(getApplicationContext(), ExchangeRequestActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
                else if (msg.contains("grants you the permission to access directory"))
                {
                    Intent notificationIntent = new Intent(getApplicationContext(), GroupMainActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
                else if (msg.contains("want to join the directory"))
                {
                    Intent notificationIntent = new Intent(getApplicationContext(), DirAdminPreviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("directory", null);
                    notificationIntent.putExtras(bundle);
                    notificationIntent.putExtra("directoryId", boardId);
                    notificationIntent.putExtra("isInviteGo", true);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);;
                }
            }
            else if("recieved_exchange_request".equals(Type) && !msg.equals("accepted") && !msg.contains("cancelled the request")) {
                String contactId = (boardId == 0) ? "" : String.valueOf(boardId);
                if (!contactId.equals(""))
                {
                    if (MyApp.getInstance().g_contactIDs.contains(boardId))
                    {
                        Intent notificationIntent = new Intent(getApplicationContext(), ContactMainActivity.class);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    } else
                    {
                        Intent notificationIntent = new Intent(getApplicationContext(), ExchangeRequestActivity.class);
                        notificationIntent.putExtra("contactId", boardId);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                }

            }
            else {
                String sessionId = Uitils.getStringFromSharedPreferences(this, "sessionId", "");
                if(!sessionId.isEmpty()) {
                    //ImBoardActivity.getInstance().finish();
                    Intent notificationIntent = new Intent(getApplicationContext(), ImBoardActivity.class);

                    notificationIntent.putExtra("contact_name", msg.split(":")[0]);
                    notificationIntent.putExtra("contact_ids", "");
                    notificationIntent.putExtra("board_id", boardId);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    //notification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationMessage, pendingNotificationIntent);
                }
            }
        }
        else {
            if("gps_contact".equals(Type)) {
                //contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, GinkoMeActivity.class), 0);
                Intent notificationIntent = new Intent(getApplicationContext(), GinkoMeActivity.class);
                notificationIntent.putExtra("isNewDetection", false);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            else {
                //GAD-1799
                ActivityManager manager = (ActivityManager) this .getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> myInfo = manager.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;

                Intent notificationIntent = new Intent(getApplicationContext(), componentInfo.getClass());
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

        }

        //GAD-1089 and GAD-1051, GAD-1224,
        if(taskInfo.get(0).topActivity.getClassName().contains("ImBoardActivity") && "im".equals(Type) && boardId == MyApp.getInstance().getCurrentBoardId()
                || taskInfo.get(0).topActivity.getClassName().contains("ViewEntityPostsActivity") && "entity_msg".equals(Type)
                || taskInfo.get(0).topActivity.getClassName().contains("ExchangeRequestActivity") && "recieved_exchange_request".equals(Type))
        {
            // To do for.....
            Log.d("tokeninfo", boardId+"="+MyApp.getInstance().getCurrentBoardId());
        }else if("contact_changed".equals(Type) && msg.contains("removed")) {
            // To do for.....
        }
        else {
            mBuilder.setSmallIcon(R.drawable.icon);
            mBuilder.setContentTitle(MyApp.APP_NAME);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
            mBuilder.setContentText(msg);

            mBuilder.setAutoCancel(true);
        }

        /*mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setContentTitle(MyApp.APP_NAME);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        mBuilder.setContentText(msg);

        mBuilder.setAutoCancel(true);*/

        if(sound != null && sound.equals("chatmessage.wav"))
        {
            if(chatMessageSoundUri == null)
                chatMessageSoundUri = Uri.parse("android.resource://" + MyApp.g_strPackageName + "/" + R.raw.chatmessage);
            mBuilder.setSound(chatMessageSoundUri);
        }
        else if(sound != null && sound.equals("ginkome.wav"))
        {
            if(ginkomeSoundUri == null)
                ginkomeSoundUri = Uri.parse("android.resource://" + MyApp.g_strPackageName + "/" + R.raw.ginkome);

            mBuilder.setSound(ginkomeSoundUri);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}