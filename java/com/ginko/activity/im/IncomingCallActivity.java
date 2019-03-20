package com.ginko.activity.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apprtcClient.AppRTCAudioManager;
import com.ginko.api.request.IMRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.HashMapVO;
import com.ginko.vo.VideoMemberVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IncomingCallActivity extends MyBaseFragmentActivity
        implements View.OnClickListener {

    /* Dial Interface Control */
    private RelativeLayout callInterface;
    private TextView txtSenderName;
    private ImageButton btnAccept, btnReject;
    /* */

    /* Call Rings and Vibrator Session */
    private MediaPlayer mPlayer = null;
    private Timer timerForVibrate;
    private CountDownTimer waitingTimer;
    private Vibrator mVibrator;

    // Conference Datas from External
    private int boardId = 0;
    private int callType = 0;
    private String userIds = "";
    private String conferenceName = "";
    private String inviteName = "";
    private ArrayList<EventUser> lstUsers;
    private String invitedIds = "";
    private String userInfo = "";
    private String senderId = "";
    private boolean isIniatedChat = false;

    private VideoRequestReceiver videoReceiver;
    private boolean isDetectReceiver = false;

    private AudioManager audioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        this.boardId = getIntent().getIntExtra("boardId", 0);
        this.callType = getIntent().getIntExtra("callType", 1);
        this.conferenceName = getIntent().getStringExtra("conferenceName");
        this.inviteName = getIntent().getStringExtra("inviteName");
        this.lstUsers = new ArrayList<EventUser>();
        this.lstUsers = (ArrayList<EventUser>) getIntent().getSerializableExtra("userData");
        this.invitedIds = getIntent().getStringExtra("invited_uids");
        this.userInfo = getIntent().getStringExtra("userInfo");
        this.senderId = getIntent().getStringExtra("senderId");

        isIniatedChat = getIntent().getBooleanExtra("isInitial", false);

        getUIObjects();
        initalizeEnvironment();

        this.videoReceiver = new VideoRequestReceiver();

        if (this.videoReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.VIDEO_REQUEST");
            this.registerReceiver(this.videoReceiver, msgReceiverIntent);
            isDetectReceiver = true;
        }

        startTimer();
        PlayRingTones();
    }

    @Override
    protected void getUIObjects() {
        super.getUIObjects();

        callInterface = (RelativeLayout)findViewById(R.id.callInterface);

        txtSenderName = (TextView)findViewById(R.id.txtInviteName);
        txtSenderName.setText(inviteName);

        btnAccept = (ImageButton)findViewById(R.id.btnAcceptCall); btnAccept.setOnClickListener(this);
        btnReject = (ImageButton)findViewById(R.id.btnRejectCall); btnReject.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAcceptCall:
                if (timerForVibrate != null)
                {
                    timerForVibrate.cancel();
                    timerForVibrate = null;
                }
                if (waitingTimer != null)
                {
                    waitingTimer.cancel();
                    waitingTimer = null;
                }
                DestroyTones();

                if (MyApp.getInstance().isJoinedOnConference == true)
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", boardId);
                    bundle.putInt("callType", callType);
                    bundle.putString("conferenceName", conferenceName);
                    bundle.putSerializable("userData", lstUsers);
                    bundle.putBoolean("isInitial", false);
                    bundle.putString("invitedIds", invitedIds);
                    bundle.putString("userInfo", userInfo);
                    bundle.putString("senderId", senderId);
                    bundle.putString("inviteName", inviteName);
                    MyApp.getInstance().initIncomeBundles();
                    MyApp.getInstance().incomingData = bundle;

                } else
                {
                    MyApp.getInstance().isIncomingOnNow = false;
                    VideoMemberVO currOne = new VideoMemberVO();
                    currOne.setUserId(String.valueOf(RuntimeContext.getUser().getUserId()));
                    currOne.setOwner(false);
                    currOne.setMe(true);
                    currOne.setWeight(1);
                    currOne.setInitialized(true);

                    if (Integer.valueOf(callType) == 1)
                        currOne.setVideoStatus(true);
                    else
                        currOne.setVideoStatus(false);
                    currOne.setVoiceStatus(true);
                    MyApp.getInstance().g_currMemberCon = currOne;
                    MyApp.getInstance().g_videoMemberList.add(currOne);
                    MyApp.getInstance().g_videoMemIDs.add(String.valueOf(RuntimeContext.getUser().getUserId()));

                    MyApp.getInstance().addOtherMembers(invitedIds, userInfo, senderId, callType, inviteName);
                    Intent notificationIntent = new Intent(IncomingCallActivity.this, GroupVideoChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", boardId);
                    bundle.putInt("callType", callType);
                    bundle.putString("conferenceName", conferenceName);
                    bundle.putSerializable("userData", lstUsers);
                    bundle.putBoolean("isInitial", false);
                    notificationIntent.putExtras(bundle);
                    startActivity(notificationIntent);
                }
                finish();
                break;
            case R.id.btnRejectCall:
                RemoveActivity(3);
                break;
        }
    }

    private void initalizeEnvironment()
    {
        mPlayer = new MediaPlayer();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    }

    private void startTimer()
    {
        waitingTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                RemoveActivity(2);
            }
        }.start();

    }

    private void RemoveActivity(int _type)
    {
        int endType = 1;
        if (MyApp.getInstance().isJoinedOnConference == true)
            endType = 3;
        else
            endType = _type;

        DestroyTones();
        MyApp.getInstance().isIncomingOnNow = false;
        if (endType != 2) {
            IMRequest.setHangupVideo(boardId, endType, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                    if (response.isSuccess()) {
                        finish();

                    }
                }
            });
        }else{
            finish();
        }
    }

    private void PlayRingTones() {
        try {
            AssetFileDescriptor afd = getAssets().openFd("ring_conference.mp3");
            mPlayer.reset();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            //mPlayer.setVolume(1.0f, 1.0f);
            mPlayer.prepare();

            mPlayer.start();
        } catch (Exception e) {
            System.out.println("Exception trying to play file subset");
        }

        final int[] count = {0};
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (count[0] < 2) {
                    count[0]++;
                    mp.seekTo(0);
                    mp.start();
                }
            }
        });

        timerForVibrate = new Timer();
        timerForVibrate.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Called at every 1000 milliseconds (1 second)
                if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
                    mVibrator.vibrate(1500);
            }
        }, 0, 4000);
    }

    private void DestroyTones()
    {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        if (mVibrator != null)
            mVibrator.cancel();
        if (timerForVibrate != null)
        {
            timerForVibrate.cancel();
            timerForVibrate = null;
        }
        if (waitingTimer != null)
        {
            waitingTimer.cancel();
            waitingTimer = null;
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

    @Override
    public void onPause() {
        super.onPause();

        if (this.videoReceiver != null && isDetectReceiver == true) {
            this.unregisterReceiver(this.videoReceiver);
            isDetectReceiver = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.videoReceiver != null && isDetectReceiver == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.VIDEO_REQUEST");
            this.registerReceiver(this.videoReceiver, msgReceiverIntent);
        }
    }

    @Override
    protected void onDestroy() {
        DestroyTones();
        MyApp.getInstance().isIncomingOnNow = false;
        super.onDestroy();
    }

    /*public class VideoRequestReceiver extends BroadcastReceiver {
        public VideoRequestReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            final String type = bundle.getString("type", "");

            if (type.equals("hangup"))
            {
                String memId = bundle.getString("from");
                int endType = bundle.getInt("endType");
                MyApp.getInstance().isIncomingOnNow = false;
                DestroyTones();
                finish();
            }
        }
    }*/

    public class VideoRequestReceiver extends BroadcastReceiver {
        public VideoRequestReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            final String type = bundle.getString("type", "");

            if (type.equals("hangup"))
            {
                String memId = bundle.getString("from");
                int endType = bundle.getInt("endType");
                if (memId.equals(senderId))
                {
                    MyApp.getInstance().isIncomingOnNow = false;
                    DestroyTones();
                    IMRequest.setHangupVideo(boardId, endType, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                            if (response.isSuccess()) {
                                finish();

                            }
                        }
                    });
                }
            }
        }
    }
}
