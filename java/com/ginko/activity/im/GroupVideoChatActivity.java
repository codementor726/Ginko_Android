package com.ginko.activity.im;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.apprtcClient.AppRTCAudioManager;
import com.apprtcClient.AppRTCClient;
import com.apprtcClient.MainPeerClient;
import com.apprtcClient.serverCommClient;
import com.ginko.api.request.IMRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.customview.AutoResizeTextButton;
import com.ginko.customview.AutoResizeTextView;
import com.ginko.customview.DragGridView;
import com.ginko.customview.TextChatDialog;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImMessageVO;
import com.ginko.vo.PeerConsVO;
import com.ginko.vo.RemoteViewVO;
import com.ginko.vo.VideoMemberVO;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.VideoTrack;
import org.webrtc.AudioTrack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GroupVideoChatActivity extends MyBaseFragmentActivity
        implements View.OnClickListener,
        MainPeerClient.PeerConnectionEvents,
        TextChatDialog.onCloseChatListener,
        TextChatDialog.onReceiveMsgListener,
        ToolTipView.OnToolTipViewClickedListener,
        AppRTCClient.SignalingEvents {

    public static final int ADD_USER_ACTIVITY = 1001;
    private List<PeerConnection.IceServer> iceServers;
    private MediaStream mediastream = null;
    private PeerConnectionFactory mainFactory = null;
    private MainPeerClient.PeerConnectionParameters peerConnectionParameters;
    private MainPeerClient peerClient;
    private List<PeerConsVO> arrARDAppClients;
    private List<RemoteViewVO> arrMembersOfConference;
    private AppRTCAudioManager audioManager = null;

    private VideoCapturer videoCapturer;
    private boolean videoCapturerStopped;

    private EglBase rootEglBase;
    private SurfaceViewRenderer localRender;

    private ScalingType scalingType;
    private int iceConnected = 0;

    // Conference Datas from External
    private int boardId = 0;
    private int callType = 0;
    private String userIds = "";
    private String conferenceName = "";
    private ArrayList<EventUser> lstUsers;

    private PercentRelativeLayout localLayout;
    private ImageView btnChatView, btnGroupAddView, btnTelView, btnCameraType, btnCameraStatus, btnMicView;
    private ImageView btnClose, btnTest;
    private ImageView imgOnlyVoice, imgVideoMute, imgVoiceMute, imgNavCover;

    private ImageLoader imgLoader;
    private BaseAdapter adapter;
    private DragGridView remoteList;
    private Handler myHandler;

    private VideoRequestReceiver videoReceiver;
    private MsgReceiver receiver;

    private NetworkImageView imgTipProfile;
    private ToolTipView mGreenToolTipView;
    private ToolTipRelativeLayout mToolTipFrameLayout;

    // Boolean Variable
    private boolean isError;
    private boolean isVideoEnable = true;
    private boolean isAudioEnable = false;
    private boolean isSpeakerEnabled;
    private boolean isIniatedChat;
    private boolean isDisconnected;
    private boolean conferenceBuild = false;
    private boolean activityRunning = false;

    private boolean isDetectReceiver = false;
    private boolean isReceiverRegistered = false;

    private int mainWidth = 0;
    private int mainHeight = 0;

    Timer timer;
    TimerTask timerTask;
    private long arrivedTick = 0;

    private TextChatDialog mCustomDialog;

    public GroupVideoChatActivity() {
        remoteList = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_video_chat);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.boardId = getIntent().getIntExtra("boardId", 0);
        this.callType = getIntent().getIntExtra("callType", 1);
        this.conferenceName = getIntent().getStringExtra("conferenceName");
        this.lstUsers = new ArrayList<EventUser>();
        this.lstUsers = (ArrayList<EventUser>) getIntent().getSerializableExtra("userData");

        isIniatedChat = getIntent().getBooleanExtra("isInitial", false);
        getUIObjects();
        initEnvironments();

        this.videoReceiver = new VideoRequestReceiver();
        this.receiver = new MsgReceiver();

        if (this.videoReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.VIDEO_REQUEST");
            this.registerReceiver(this.videoReceiver, msgReceiverIntent);
            isDetectReceiver = true;
        }

        if (this.receiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.receiver, msgReceiverIntent);
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        /* Conference Title */
        /* Leave Buttons from Call */
        localLayout = (PercentRelativeLayout)findViewById(R.id.localViewLayout);
        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);

        /* Features button (Chatting Room, Reject, Switching Camera, Video Turn on/off, Voice Turn on/off, Invitation members */
        btnChatView = (ImageView)findViewById(R.id.imgBtnChatView); btnChatView.setOnClickListener(this);
        btnGroupAddView = (ImageView)findViewById(R.id.imgBtnGroupAddView); btnGroupAddView.setOnClickListener(this);
        btnCameraType = (ImageView)findViewById(R.id.imgBtnCameraFrontOnOff); btnCameraType.setOnClickListener(this);
        btnCameraStatus = (ImageView)findViewById(R.id.imgBtnPlayVideo); btnCameraStatus.setOnClickListener(this);
        btnMicView = (ImageView)findViewById(R.id.imgBtnMicOnOff); btnMicView.setOnClickListener(this);
        btnTelView = (ImageView)findViewById(R.id.imgBtnTelView); btnTelView.setOnClickListener(this);
        btnTelView.setImageResource(R.drawable.speak_phone_img);

        imgNavCover = (ImageView)findViewById(R.id.imgCover);
        imgNavCover.setClickable(true);
        imgNavCover.setFocusable(true);

        imgNavCover.setVisibility(View.VISIBLE);

        /* ImageView for Screen Status */
        imgVoiceMute = (ImageView)findViewById(R.id.imgVoiceMute);
        imgVideoMute = (ImageView)findViewById(R.id.imgVideoMute);
        imgOnlyVoice = (ImageView)findViewById(R.id.imgOnlyVoice);
        imgVoiceMute.setVisibility(View.GONE);
        imgVideoMute.setVisibility(View.GONE);
        imgOnlyVoice.setVisibility(View.GONE);

        /* local Video Render */
        localRender = (SurfaceViewRenderer) findViewById(R.id.glview_call);
        scalingType = ScalingType.SCALE_ASPECT_FIT;

        /* Remote Video Group View */
        remoteList = (DragGridView) findViewById(R.id.list);
        remoteList.requestFocus();

        if (callType == 2)
        {
            btnCameraStatus.setSelected(true);
            btnCameraStatus.setImageResource(R.drawable.camera_conference_off);
        }
        if (imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        mToolTipFrameLayout = (ToolTipRelativeLayout) findViewById(R.id.bodyLayout);
        imgTipProfile = (NetworkImageView) findViewById(R.id.imgTipProfile);

        imgTipProfile.refreshOriginalBitmap();
        imgTipProfile.setDefaultImageResId(R.drawable.profile_preview_default_icon);
        imgTipProfile.setVisibility(View.GONE);

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 2000); //
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                if (arrARDAppClients == null)
                    return;

                long currMills = System.currentTimeMillis();
                boolean isFind = false;

                for (int i=0; i<arrMembersOfConference.size(); i++)
                {
                    RemoteViewVO peerOne = arrMembersOfConference.get(i);
                    if ((peerOne.getIceConnected() == ConstValues.ICE_NEW || peerOne.getIceConnected() == ConstValues.ICE_FAILED) && (currMills > peerOne.getCreateTime() + 30000) && peerOne.getCreateTime() != 0) {
                        peerOne.setIceConnected(ConstValues.ICE_FAILED);
                        isFind = true;
                        if (!peerOne.isInvitedByMe()) {
                            hangupConference(arrMembersOfConference.get(i).getUser_id(), 2);
                            isFind = false;
                            break;
                        }
                    }
                }

                if (isFind == true)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
    }
    /*
     Init Environments
    */
    private void initEnvironments()
    {
        /* Flag for Room Elements */
        activityRunning = true;
        iceConnected = 0;
        isDisconnected = false;
        isSpeakerEnabled = true;
        isAudioEnable = false;
        isError = false;

        /* Arrays for PeerCon Clients and Remote Renders Infos */
        arrARDAppClients = new ArrayList<PeerConsVO>();
        arrMembersOfConference = new ArrayList<RemoteViewVO>();
        iceServers = new ArrayList<PeerConnection.IceServer>();

        /* Camera Capturer */
        videoCapturerStopped = false;
        videoCapturer = null;


        // Video Renderer Setting
        rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setZOrderMediaOverlay(true);
        localRender.setEnableHardwareScaler(true /* enabled */);
        updateLocalVideoView();

        peerConnectionParameters =
                new MainPeerClient.PeerConnectionParameters(true, 0, 0, 0, false);
        peerClient = new MainPeerClient();
        peerClient.createPeerConnectionFactory(
                GroupVideoChatActivity.this, peerConnectionParameters, GroupVideoChatActivity.this);

        audioManager = AppRTCAudioManager.create(this, new Runnable() {
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });

        audioManager.init();

        MyApp.getInstance().isJoinedOnConference = true;
        putConferenceMembers();

        adapter = new ArrayAdapter<RemoteViewVO>(this,R.layout.list_item_conference, arrMembersOfConference) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = convertView;

                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.list_item_conference, parent, false);
                }
                int realWidth = 0;
                int realHeight = 0;

                int arrSize = arrMembersOfConference.size();
                if (arrSize > 1)
                    realWidth = Math.round(mainWidth / 2);
                else
                    realWidth = mainWidth;

                int divideCnt = (arrSize == 1) ? 2 : (arrSize / 2 + 1);
                realHeight = Math.round(mainHeight / divideCnt);

                PercentRelativeLayout parentLayout = (PercentRelativeLayout)view.findViewById(R.id.bodyLayout);

                final LinearLayout retryLayout = (LinearLayout)view.findViewById(R.id.retryLayout);
                ImageView imgVideoMute = (ImageView)view.findViewById(R.id.imgVideoMute);
                final ImageView imgInitCall = (ImageView)view.findViewById(R.id.imgInitCall);
                imgInitCall.post(new Runnable() {
                    @Override
                    public void run() {
                        AnimationDrawable anim = (AnimationDrawable) imgInitCall.getDrawable();
                        anim.setOneShot(false);//repeat animation
                        anim.start();
                    }
                });

                AutoResizeTextButton btnRetryNo = (AutoResizeTextButton)view.findViewById(R.id.btnRetryNo);
                AutoResizeTextButton btnRetryYes = (AutoResizeTextButton)view.findViewById(R.id.btnRetryYes);
                NetworkImageView tiledProfilePhoto = (NetworkImageView)view.findViewById(R.id.tileProfileImage);
                AutoResizeTextView txtStatus = (AutoResizeTextView)view.findViewById(R.id.txtStatus);

                final RemoteViewVO remoteData = getItem(position);

                tiledProfilePhoto.getLayoutParams().width = Math.round(realHeight / 4 * 3);
                tiledProfilePhoto.getLayoutParams().height = Math.round(realHeight / 4 * 3);
                tiledProfilePhoto.setCircleRadiusInPixels(Math.round(realHeight / 4 * 3));
                tiledProfilePhoto.requestLayout();

                tiledProfilePhoto.refreshOriginalBitmap();
                String imageUrl = remoteData.getPhotoUrl();
                tiledProfilePhoto.setImageUrl(imageUrl, imgLoader);
                tiledProfilePhoto.setDefaultImageResId(R.drawable.profile_preview_default_icon);

                SurfaceViewRenderer remoteRenderScreen = (SurfaceViewRenderer)view.findViewById(R.id.glview_cell);
                if (remoteData.isViewRemoted() == false)
                {
                    remoteRenderScreen.init(rootEglBase.getEglBaseContext(), null);
                    remoteRenderScreen.setEnableHardwareScaler(true /* enabled */);
                    remoteData.setViewRemoted(true);
                }

                if (remoteData.isVideoStatus() == true) {
                    remoteRenderScreen.setEnabled(true);
                    remoteRenderScreen.setVisibility(View.VISIBLE);
                    imgVideoMute.setVisibility(view.GONE);
                    tiledProfilePhoto.setVisibility(View.GONE);
                }
                else if (remoteData.isVideoStatus() == false){
                    remoteRenderScreen.setEnabled(false);
                    remoteRenderScreen.setVisibility(View.GONE);
                    imgVideoMute.setVisibility(View.GONE);
                    tiledProfilePhoto.setVisibility(View.VISIBLE);
                }

                parentLayout.getLayoutParams().width = realWidth;
                parentLayout.getLayoutParams().height = realHeight;
                parentLayout.requestLayout();

                btnRetryYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        retryLayout.setVisibility(View.GONE);
                        reOpenConference(remoteData.getUser_id());
                    }
                });

                btnRetryNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        retryLayout.setVisibility(View.GONE);
                        hangupConference(remoteData.getUser_id(), 2);
                    }
                });

                if (isDisconnected == true) {
                    txtStatus.setBackgroundColor(Color.RED);
                    txtStatus.setTextColor(Color.WHITE);
                    txtStatus.setText("Disconnected!");

                    if (remoteData.getRemoteTrack() != null) {
                        VideoTrack videoTrack = remoteData.getRemoteTrack();
                        videoTrack.removeRenderer(new VideoRenderer(remoteRenderScreen));
                        videoTrack.setEnabled(false);
                        videoTrack = null;
                    }

                    if (remoteData.getRemoteAudioTrack() != null) {
                        AudioTrack audioTrack = remoteData.getRemoteAudioTrack();
                        audioTrack = null;
                    }

                    if (remoteRenderScreen != null) {
                        remoteRenderScreen.release();
                        remoteRenderScreen = null;
                    }
                    tiledProfilePhoto.setVisibility(View.VISIBLE);

                } else
                {
                    switch (remoteData.getIceConnected())
                    {
                        case ConstValues.ICE_NEW:
                            imgInitCall.setVisibility(View.VISIBLE);
                            retryLayout.setVisibility(View.GONE);
                            tiledProfilePhoto.setVisibility(View.VISIBLE);
                            txtStatus.setBackgroundColor(Color.TRANSPARENT);
                            txtStatus.setText(remoteData.getName());
                            remoteRenderScreen.setScalingType(scalingType);
                            remoteRenderScreen.setMirror(false);
                            remoteRenderScreen.requestLayout();
                            break;
                        case ConstValues.ICE_FAILED:
                            imgInitCall.setVisibility(View.GONE);
                            retryLayout.setVisibility(View.VISIBLE);
                            tiledProfilePhoto.setVisibility(View.VISIBLE);
                            txtStatus.setBackgroundColor(Color.rgb(128, 0, 0));
                            txtStatus.setTextColor(Color.WHITE);
                            txtStatus.setText("No answer!");
                            remoteRenderScreen.setScalingType(scalingType);
                            remoteRenderScreen.setMirror(false);
                            remoteRenderScreen.requestLayout();
                            break;
                        case ConstValues.ICE_CONNECTED: {
                            if (remoteData.isRendered() == false)
                            {
                                remoteData.setRendered(true);
                                VideoTrack videoTrack = remoteData.getRemoteTrack();
                                videoTrack.addRenderer(new VideoRenderer(remoteRenderScreen));
                                remoteRenderScreen.setScalingType(scalingType);
                                remoteRenderScreen.setMirror(false);
                                remoteRenderScreen.requestLayout();

                                txtStatus.setText(remoteData.getName());
                                txtStatus.setBackgroundColor(Color.TRANSPARENT);
                                txtStatus.setTextColor(Color.WHITE);

                                imgInitCall.setVisibility(View.GONE);
                            }

                        }
                            break;
                        case ConstValues.ICE_CLOSED:
                        {
                            String strClose = "";
                            int endType = remoteData.getEndType();
                            if (endType == 1)
                                strClose = "Left.";
                            else if (endType == 2)
                                strClose = "No answer.";
                            else if (endType == 3)
                                strClose = "Busy.";
                            else if (endType == 4)
                                strClose = "Missing.";

                            txtStatus.setBackgroundColor(Color.RED);
                            txtStatus.setTextColor(Color.WHITE);
                            txtStatus.setText(strClose);

                            VideoTrack videoTrack = remoteData.getRemoteTrack();
                            if (videoTrack != null)
                            {
                                videoTrack.removeRenderer(new VideoRenderer(remoteRenderScreen));
                                videoTrack.setEnabled(false);
                                videoTrack = null;
                            }

                            AudioTrack audioTrack = remoteData.getRemoteAudioTrack();
                            if (audioTrack != null)
                            {
                                audioTrack = null;
                            }


                            if (remoteRenderScreen != null) {
                                remoteRenderScreen.release();
                                remoteRenderScreen = null;
                            }


                            tiledProfilePhoto.setVisibility(View.VISIBLE);
                        }

                        break;
                    }
                }

                return view;
            }
        };

        remoteList.post(new Runnable() {
            @Override
            public void run() {
                mainWidth = remoteList.getMeasuredWidth();
                mainHeight = remoteList.getMeasuredHeight();

                if (arrMembersOfConference.size() > 1)
                    remoteList.setNumColumns(2);
                else
                    remoteList.setNumColumns(1);

                remoteList.setAdapter(adapter);
                updateLocalVideoView();
                adapter.notifyDataSetChanged();
            }
        });

        if (isIniatedChat) {
            createConference();
        } else
        {
            joinConference();
        }

    }

    @Override
    public void onBackPressed()
    {
        disconnect(1);
        //super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();

        activityRunning = false;
        if (peerClient != null)
            peerClient.stopVideoSource();

        if (this.videoReceiver != null && isDetectReceiver == true) {
            this.unregisterReceiver(this.videoReceiver);
            isDetectReceiver = false;
        }

        if (this.receiver != null && isReceiverRegistered == true) {
            unregisterReceiver(this.receiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MyApp.getInstance().isIncomingOnNow == true)
        {
            MyApp.getInstance().initializeVideoVariables();
            Bundle infoCalling = MyApp.getInstance().incomingData;
            String invitedIdS = infoCalling.getString("invitedIds");
            String userInfo = infoCalling.getString("userInfo");
            String senderId = infoCalling.getString("senderId");
            String inviteName = infoCalling.getString("inviteName");

            MyApp.getInstance().addOtherMembers(invitedIdS, userInfo, senderId, callType, inviteName);
            MyApp.getInstance().isIncomingOnNow = false;
            MyApp.getInstance().incomingData = null;

            Intent intent = new Intent();
            intent.putExtras(infoCalling);
            startActivity(intent);
            finish();
        }

        if (this.videoReceiver != null && isDetectReceiver == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.VIDEO_REQUEST");
            this.registerReceiver(this.videoReceiver, msgReceiverIntent);
        }

        if (this.receiver != null && isReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            this.registerReceiver(this.receiver, msgReceiverIntent);
        }

        activityRunning = true;
        if (peerClient != null)
            peerClient.startVideoSource();

    }

    @Override
    protected void onDestroy() {
        disconnect(1);
        activityRunning = false;
        rootEglBase.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                disconnect(1);
                break;
            case R.id.imgBtnChatView:
                /*Intent intent = new Intent(GroupVideoChatActivity.this , ImBoardActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("board_id", boardId);
                bundle.putBoolean("isVideoChat", true);
                bundle.putSerializable("userData", lstUsers);
                intent.putExtras(bundle);
                startActivity(intent); */

                mCustomDialog = new TextChatDialog(this, boardId, lstUsers);
                mCustomDialog.show();
                mCustomDialog.updateListView();
                mCustomDialog.setCloseListener(this);
                mCustomDialog.setMessageListener(this);

                mCustomDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == event.KEYCODE_BACK) {
                            if (mCustomDialog.isShownKeyboard())
                                mCustomDialog.hideKeyboard();
                            else {
                                mCustomDialog.hide();
                                btnClose.setVisibility(View.VISIBLE);
                            }

                            return true;
                        }
                        return false;
                    }
                });


                btnClose.setVisibility(View.GONE);
                break;
            case R.id.imgBtnGroupAddView:
                //btnGroupAddView.setImageResource(R.drawable.selconference);
                Intent addContactIntent = new Intent(GroupVideoChatActivity.this , VideoChatAddUserActivity.class);
                addContactIntent.putExtra("boardId" , boardId);
                addContactIntent.putExtra("isReturnFromConference", true);
                String contactKeys="";
                for(int i=0;i<arrMembersOfConference.size();i++)
                {
                    String userId = arrMembersOfConference.get(i).getUser_id();
                    contactKeys = contactKeys + userId +",";
                }
                addContactIntent.putExtra("existContactIds" , contactKeys);

                startActivityForResult(addContactIntent, ADD_USER_ACTIVITY);
                break;
            case R.id.imgBtnCameraFrontOnOff:
                if (isVideoEnable)
                {
                    btnCameraType.setSelected(!btnCameraType.isSelected());
                    if (!btnCameraType.isSelected())
                        btnCameraType.setImageResource(R.drawable.camera_conference_back);
                    else
                        btnCameraType.setImageResource(R.drawable.camera_conference_front);

                    if (peerClient != null)
                        peerClient.switchCamera();
                }
                break;
            case R.id.imgBtnPlayVideo:
                switchVideoEnable();
                break;
            case R.id.imgBtnMicOnOff:
                btnMicView.setSelected(!btnMicView.isSelected());
                if (!btnMicView.isSelected())
                {
                    if (peerClient != null)
                        peerClient.setAudioEnabled(true);
                    btnMicView.setImageResource(R.drawable.mic_conference_on);

                    sendingTurnStatusOfAudio("on");
                    isAudioEnable = true;

                    if (btnCameraStatus.isSelected()) {
                        imgVoiceMute.setVisibility(View.GONE);
                        imgVideoMute.setVisibility(View.GONE);
                        imgOnlyVoice.setVisibility(View.VISIBLE);
                    } else
                    {
                        imgVoiceMute.setVisibility(View.GONE);
                        imgVideoMute.setVisibility(View.GONE);
                        imgOnlyVoice.setVisibility(View.GONE);
                    }
                } else
                {
                    if (peerClient != null)
                        peerClient.setAudioEnabled(false);
                    sendingTurnStatusOfAudio("off");
                    btnMicView.setImageResource(R.drawable.mic_conference_off);

                    isAudioEnable = false;

                    if (btnCameraStatus.isSelected()) {
                        imgVoiceMute.setVisibility(View.VISIBLE);
                        imgVideoMute.setVisibility(View.VISIBLE);
                        imgOnlyVoice.setVisibility(View.GONE);
                    } else
                    {
                        imgVoiceMute.setVisibility(View.VISIBLE);
                        imgVideoMute.setVisibility(View.GONE);
                        imgOnlyVoice.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.imgBtnTelView:
                btnTelView.setSelected(!btnTelView.isSelected());
                if (!btnTelView.isSelected())
                {
                    btnTelView.setImageResource(R.drawable.speaker_off_movie);
                    btnTelView.post(new Runnable() {
                        @Override
                        public void run() {
                            AnimationDrawable anim = (AnimationDrawable) btnTelView.getDrawable();
                            anim.setOneShot(false);//repeat animation
                            anim.start();
                        }
                    });
                    enableSpeaker();
                } else
                {
                    btnTelView.setImageResource(R.drawable.speak_phone_img);
                    disableSpeaker();
                }
                break;
        }
    }

    private void switchVideoEnable()
    {
        btnCameraStatus.setSelected(isVideoEnable);
        isVideoEnable =! isVideoEnable;

        if (!btnCameraStatus.isSelected())
        {
            sendingTurnStatusOfVideo("on");
            if (peerClient != null)
                peerClient.setVideoEnabled(true);

            btnCameraStatus.setImageResource(R.drawable.camera_conference_on);

            if (btnMicView.isSelected())
            {
                imgVoiceMute.setVisibility(View.VISIBLE);
                imgVideoMute.setVisibility(View.GONE);
                imgOnlyVoice.setVisibility(View.GONE);
            } else
            {
                imgVoiceMute.setVisibility(View.GONE);
                imgVideoMute.setVisibility(View.GONE);
                imgOnlyVoice.setVisibility(View.GONE);
            }
        } else
        {
            if (peerClient != null)
                peerClient.setVideoEnabled(false);
            sendingTurnStatusOfVideo("off");

            btnCameraStatus.setImageResource(R.drawable.camera_conference_off);

            if (btnMicView.isSelected()) {
                imgVoiceMute.setVisibility(View.VISIBLE);
                imgVideoMute.setVisibility(View.VISIBLE);
                imgOnlyVoice.setVisibility(View.GONE);
            } else
            {
                imgVoiceMute.setVisibility(View.GONE);
                imgVideoMute.setVisibility(View.GONE);
                imgOnlyVoice.setVisibility(View.VISIBLE);
            }
        }

    }

    private void putConferenceMembers()
    {
        for (int i=0; i<MyApp.getInstance().g_videoMemberList.size(); i++)
        {
            VideoMemberVO memberCon = MyApp.getInstance().g_videoMemberList.get(i);
            if (Integer.valueOf(memberCon.getUserId()) != RuntimeContext.getUser().getUserId())
            {
                RemoteViewVO oneMem = new RemoteViewVO();
                oneMem.setUser_id(memberCon.getUserId());
                oneMem.setName(memberCon.getName());
                oneMem.setPhotoUrl(memberCon.getImageUrl());
                oneMem.setVideoStatus(memberCon.isVideoStatus());
                oneMem.setVoiceStatus(memberCon.isVoiceStatus());
                oneMem.setCreateTime(System.currentTimeMillis());
                oneMem.setIceConnected(ConstValues.ICE_NEW);
                oneMem.setInvitedByMe(memberCon.isInvitedByMe());
                arrMembersOfConference.add(oneMem);
            }
        }
    }


    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = null;

        videoCapturer = createCameraCapturer(new Camera2Enumerator(this), true);

        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()), true);
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private boolean captureToTexture() {
        return true;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator, boolean isFrontCam) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName) && isFrontCam) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
            else if (enumerator.isBackFacing(deviceName) && !isFrontCam) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }

        }
        return null;
    }

    private void sendingTurnStatusOfVideo(String status) {
        IMRequest.turnStatusOfVideoConference(boardId, status, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                if (response.isSuccess()) {

                }
            }
        });
    }

    private void sendingTurnStatusOfAudio(String status) {
        IMRequest.turnStatusOfVoiceConference(boardId, status, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                if (response.isSuccess()) {

                }
            }
        });
    }


    private void enableSpeaker()
    {
        if (audioManager != null)
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        isSpeakerEnabled = true;
    }

    private void disableSpeaker()
    {
        if (audioManager != null)
            audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        isSpeakerEnabled = false;
    }
    /**
     * Create Video Conference By Owner
     */
    private void createConference() {
        IMRequest.setInitalVideo(boardId, callType, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                if (response.isSuccess()) {
                    conferenceBuild = true;

                    try {
                        iceServers.clear();
                        iceServers = iceServersFromPCConfigJSON(response.getData());
                    } catch (JSONException e) {
                    }

                    VideoCapturer videoCapturer = null;
                    //if (peerConnectionParameters.videoCallEnabled) {
                    videoCapturer = createVideoCapturer();
                    //}
                    peerClient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender,
                            videoCapturer, iceServers);
                    if (callType == 2)
                        switchVideoEnable();
                    else
                        disableSpeaker();
                } else {
                    if (response.getErrorCode() == 650) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                disconnect(5);
                            }
                        }, 1500);
                    }
                }
            }
        });
    }

    /**
     * Join Video Conference
     */
    private void joinConference() {
        IMRequest.setAcceptVideo(boardId, userIds, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                if (response.isSuccess()) {
                    try {
                        iceServers.clear();
                        iceServers = iceServersFromPCConfigJSON(response.getData());
                    } catch (JSONException e) {
                    }

                    VideoCapturer videoCapturer = null;
                    //if (peerConnectionParameters.videoCallEnabled) {
                    videoCapturer = createVideoCapturer();
                    //}
                    peerClient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender,
                            videoCapturer, iceServers);

                    peerClient.setVideoEnabled(false);
                    disableSpeaker();
                    switchVideoEnable();
                } else {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            disconnect(5);
                        }
                    }, 1500);
                }
            }
        });
    }

    private void reOpenConference(final String memberId) {
        IMRequest.setInviteNewVideoMember(boardId, memberId, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                if (response.isSuccess()) {
                    if (arrARDAppClients.size() > 0) {
                        for (int i = 0; i < arrARDAppClients.size(); i++) {
                            PeerConsVO peerCon = arrARDAppClients.get(i);
                            if (!peerCon.getUser_id().equals(memberId))
                                continue;

                            AppRTCClient client = peerCon.getPeerCons();
                            client.setIsInitiator(true);
                            client.sendOffer();

                            arrMembersOfConference.get(i).setIceConnected(ConstValues.ICE_NEW);
                            arrMembersOfConference.get(i).setCreateTime(System.currentTimeMillis());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        }
                    }
                } else {
                }
            }
        });
    }


    private void updateLocalVideoView() {
        if (isVideoEnable)
        {
            imgVideoMute.setVisibility(View.GONE);
            imgOnlyVoice.setVisibility(View.GONE);
        }
        else
        {
            imgOnlyVoice.setVisibility(View.VISIBLE);
        }

        PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) localLayout.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();

        if (arrMembersOfConference == null || arrMembersOfConference.size() == 0)
        {
            info.heightPercent = 1.00f;
            info.widthPercent = 1.00f;
        } else
        {
            int memberSize = arrMembersOfConference.size();
            if (memberSize % 2 != 0 && memberSize > 1)
                info.widthPercent = 0.50f;
            else
                info.widthPercent = 1.00f;

            int divideCnt = (memberSize == 1) ? 2 : (memberSize / 2 + 1);
            info.heightPercent = Math.round(1 / divideCnt);
        }

        if (iceConnected == 1) {
            localRender.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        } else {
            localRender.setScalingType(ScalingType.SCALE_ASPECT_FILL);
        }
        localRender.setMirror(true);
        localRender.requestLayout();
    }


    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnectFromRoom() {
        for (int i=0; i<arrARDAppClients.size(); i++)
        {
            PeerConsVO peerOne = arrARDAppClients.get(i);
            peerOne.getPeerCons().disconnectFromRoom();
        }

        arrARDAppClients.clear();
        arrARDAppClients = null;

        if (peerClient != null) {
            peerClient.close();
            peerClient = null;
        }

        if (localRender != null) {
            localRender.release();
            localRender = null;
        }

        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }

        if (mediastream != null) {
            mediastream.audioTracks.get(0).dispose();
            mediastream.videoTracks.get(0).dispose();
            mediastream = null;
        }

        if (mainFactory != null) {
            mainFactory = null;
        }

        MyApp.getInstance().clearVideoVariables();
    }

    private void disconnect(final int endType)
    {
        if (activityRunning == false)
            return;

        activityRunning = false;
        isDisconnected = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (mCustomDialog != null)
            mCustomDialog.dismiss();

        if (this.videoReceiver != null && isDetectReceiver == true) {
            this.unregisterReceiver(this.videoReceiver);
            isDetectReceiver = false;
        }

        myHandler = new Handler(Looper.getMainLooper());

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String boardIDForConference = "";
                if (boardId > 0) {
                    boardIDForConference = String.valueOf(boardId);
                    IMRequest.setHangupVideo(Integer.valueOf(boardIDForConference), endType, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                            if (response.isSuccess()) {
                                arrMembersOfConference.clear();
                                arrMembersOfConference = null;
                                disconnectFromRoom();
                                finish();

                            }
                        }
                    });
                } else {
                    arrMembersOfConference.clear();
                    arrMembersOfConference = null;
                    disconnectFromRoom();
                    finish();

                }
            }
        }, 1500);
    }

    private void updateButtonStatus(boolean flag)
    {
        if (!flag)
        {
            btnChatView.setAlpha(.5f);
            btnChatView.setClickable(false);
            btnCameraStatus.setAlpha(.5f);;
            btnCameraStatus.setClickable(false);
            btnCameraType.setAlpha(.5f);
            btnCameraType.setClickable(false);
            btnMicView.setAlpha(.5f);
            btnMicView.setClickable(false);
            btnTelView.setAlpha(.5f);
            btnTelView.setClickable(false);

        } else
        {
            btnChatView.setAlpha(1f);
            btnChatView.setClickable(true);
            btnCameraStatus.setAlpha(1f);;
            btnCameraStatus.setClickable(true);
            btnCameraType.setAlpha(1f);
            btnCameraType.setClickable(true);
            btnMicView.setAlpha(1f);
            btnMicView.setClickable(true);
            btnTelView.setAlpha(1f);
            btnTelView.setClickable(true);
        }
    }

    private String getActiveConnectionStats(StatsReport report) {
        StringBuilder activeConnectionbuilder = new StringBuilder();
        // googCandidatePair to show information about the active
        // connection.
        for (StatsReport.Value value : report.values) {
            if (value.name.equals("googActiveConnection")
                    && value.value.equals("false")) {
                return null;
            }
            String name = value.name.replace("goog", "");
            activeConnectionbuilder.append(name).append("=")
                    .append(value.value).append("\n");
        }
        return activeConnectionbuilder.toString();
    }

    private void initInvitationMembers() {
        for (int i=0; i<MyApp.getInstance().g_videoMemberList.size(); i++)
        {
            VideoMemberVO inviteMem = MyApp.getInstance().g_videoMemberList.get(i);
            boolean isJoinedMem = false;
            for (int j=0; j<arrMembersOfConference.size(); j++)
            {
                RemoteViewVO isExistMem = arrMembersOfConference.get(j);
                if (inviteMem.getUserId().equals(isExistMem.getUser_id()))
                    isJoinedMem = true;
            }

            if (!isJoinedMem && (!inviteMem.getUserId().equals(String.valueOf(RuntimeContext.getUser().getUserId()))))
            {
                RemoteViewVO oneMem = new RemoteViewVO();
                oneMem.setUser_id(inviteMem.getUserId());
                oneMem.setName(inviteMem.getName());
                oneMem.setPhotoUrl(inviteMem.getImageUrl());
                oneMem.setVideoStatus(inviteMem.isVideoStatus());
                oneMem.setVoiceStatus(inviteMem.isVoiceStatus());
                oneMem.setIceConnected(ConstValues.ICE_NEW);
                oneMem.setCreateTime(System.currentTimeMillis());
                arrMembersOfConference.add(oneMem);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int numColumn = remoteList.getNumColumns();

                if (numColumn != 2 && arrMembersOfConference.size() >= 2) {
                    releaseAllRenders();
                    remoteList.setNumColumns(2);
                } else if (numColumn != 1 && arrMembersOfConference.size() == 1) {
                    releaseAllRenders();
                    remoteList.setNumColumns(1);
                }

                adapter.notifyDataSetChanged();
                updateLocalVideoView();
            }
        });
    }

    private void sendingOfferToInvitationMembers() {

        for (int i=0; i<MyApp.getInstance().g_videoMemberList.size(); i++)
        {
            VideoMemberVO inviteMem = MyApp.getInstance().g_videoMemberList.get(i);
            boolean isJoinedMem = false;
            for (int j=0; j<arrMembersOfConference.size(); j++)
            {
                RemoteViewVO isExistMem = arrMembersOfConference.get(j);
                if (inviteMem.getUserId().equals(isExistMem.getUser_id()))
                    isJoinedMem = true;
            }

            if (!isJoinedMem && (!inviteMem.getUserId().equals(String.valueOf(RuntimeContext.getUser().getUserId()))))
            {
                AppRTCClient newClient = new serverCommClient(GroupVideoChatActivity.this, boardId, callType, iceServers, inviteMem.getUserId());
                newClient.setIsInitiator(true);
                newClient.connectToRoomWithStream(mediastream);
                newClient.sendOffer();

                PeerConsVO onePeerCon = new PeerConsVO();
                onePeerCon.setPeerCons(newClient);
                onePeerCon.setUser_id(inviteMem.getUserId());
                onePeerCon.setType("sender");
                onePeerCon.setCreateTime(System.currentTimeMillis());
                arrARDAppClients.add(onePeerCon);

                RemoteViewVO oneMem = new RemoteViewVO();
                oneMem.setUser_id(inviteMem.getUserId());
                oneMem.setName(inviteMem.getName());
                oneMem.setPhotoUrl(inviteMem.getImageUrl());
                oneMem.setVideoStatus(inviteMem.isVideoStatus());
                oneMem.setVoiceStatus(inviteMem.isVoiceStatus());
                oneMem.setCreateTime(System.currentTimeMillis());
                oneMem.setIceConnected(ConstValues.ICE_NEW);
                arrMembersOfConference.add(oneMem);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int numColumn = remoteList.getNumColumns();

                if (numColumn != 2 && arrMembersOfConference.size() >= 2) {
                    releaseAllRenders();
                    remoteList.setNumColumns(2);
                } else if (numColumn != 1 && arrMembersOfConference.size() == 1) {
                    releaseAllRenders();
                    remoteList.setNumColumns(1);
                }

                adapter.notifyDataSetChanged();
                updateLocalVideoView();
            }
        });
    }

    private void releaseAllRenders()
    {
        if (arrMembersOfConference == null)
            return;;
        for (int i=0; i<arrMembersOfConference.size(); i++)
        {
            RemoteViewVO remoteOne = arrMembersOfConference.get(i);
            remoteOne.setRendered(false);
        }
    }

    private void checkIsMyAlone()
    {
        if (arrMembersOfConference == null || arrMembersOfConference.size() == 0)
            disconnect(1);
    }

    void reloadMembersWithStatus(String memId, int valueId)
    {
        int idx = -1;

        for (int j=0; j<arrMembersOfConference.size(); j++)
        {
            RemoteViewVO changeOne = arrMembersOfConference.get(j);
            if (changeOne.getUser_id().equals(memId)){
                if (valueId == 1)
                    changeOne.setVideoStatus(true);
                else if (valueId == 2)
                    changeOne.setVideoStatus(false);
                else if (valueId == 3)
                    changeOne.setVoiceStatus(true);
                else if (valueId == 4)
                    changeOne.setVoiceStatus(false);
                idx = j;
                break;
            }
        }

        final int finalIdx = idx;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void hangupConference(final String memberId, int endType)
    {
        if (arrARDAppClients == null)
            return;
        if (arrMembersOfConference == null)
            return;

        int position = -1;
        for (int i=0; i<arrMembersOfConference.size(); i++)
        {
            RemoteViewVO oneMem = arrMembersOfConference.get(i);
            if (oneMem.getUser_id().equals(memberId)) {
                oneMem.setIceConnected(ConstValues.ICE_CLOSED);
                if (iceConnected == 1)
                    oneMem.setEndType(endType);
                else if (iceConnected == 0)
                    oneMem.setEndType(3);
                position = i;
                break;
            }
        }

        for (int j=0; j<arrARDAppClients.size(); j++)
        {
            PeerConsVO peerOne = arrARDAppClients.get(j);
            if (peerOne.getUser_id().equals(memberId)) {
                peerOne.getPeerCons().disconnectFromRoom();
                arrARDAppClients.remove(j);
                break;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

        myHandler = new Handler(Looper.getMainLooper());

        final int finalPosition = position;
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (iceConnected != 0)
                    MyApp.getInstance().removeVideoMembersByid(memberId);

                if (finalPosition != -1 && arrMembersOfConference.size() != 0)
                    arrMembersOfConference.remove(finalPosition);
                int numColumn = remoteList.getNumColumns();

                if (arrMembersOfConference != null)
                {
                    if (numColumn != 2 && arrMembersOfConference.size() >= 2) {
                        releaseAllRenders();
                        remoteList.setNumColumns(2);
                    } else if (numColumn != 1 && arrMembersOfConference.size() == 1) {
                        releaseAllRenders();
                        remoteList.setNumColumns(1);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    updateLocalVideoView();
                    checkIsMyAlone();
                }
            }
        }, 1500);
    }

    // Update the heads-up display with information from |reports|.
    private void updateHUD(StatsReport[] reports) {
        StringBuilder builder = new StringBuilder();
        for (StatsReport report : reports) {
            // bweforvideo to show statistics for video Bandwidth Estimation,
            // which is global per-session.
            if (report.id.equals("bweforvideo")) {
                for (StatsReport.Value value : report.values) {
                    String name = value.name.replace("goog", "")
                            .replace("Available", "").replace("Bandwidth", "")
                            .replace("Bitrate", "").replace("Enc", "");

                    builder.append(name).append("=").append(value.value)
                            .append(" ");
                }
                builder.append("\n");
            } else if (report.type.equals("googCandidatePair")) {
                String activeConnectionStats = getActiveConnectionStats(report);
                if (activeConnectionStats == null) {
                    continue;
                }
                builder.append(activeConnectionStats);
            } else {
                continue;
            }
            builder.append("\n");
        }
    }

    private void reportError(final String description) {
        if (!isError) {
            isError = true;
            disconnectWithErrorMessage(description);
        }
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (!activityRunning) {
            disconnect(1);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.str_error_play_video))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.alert_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect(1);
                                }
                            })
                    .create()
                    .show();
        }
    }

    @Override
    public void onChangeState(AppRTCClient.ConnectionState roomState, String memberId) {

    }

    @Override
    public void onReceiveRemoteVideoTrack(VideoTrack remoteTrack, org.webrtc.AudioTrack remoteAudio, String memberId) {
        iceConnected = 1;

        for (int i=0; i<arrMembersOfConference.size(); i++) {
            RemoteViewVO remoteOne = arrMembersOfConference.get(i);
            if (remoteOne.getUser_id().equals(memberId)) {
                remoteOne.setRemoteTrack(remoteTrack);
                remoteOne.setRemoteAudioTrack(remoteAudio);
                remoteOne.setIceConnected(ConstValues.ICE_CONNECTED);
                break;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgNavCover.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onReceiveRemoteAudioTrack(AudioTrack remoteTrack, String memberId) {

    }

    @Override
    public void onChannelError(String description, String memberId) {
        if (activityRunning)
            MyApp.getInstance().showSimpleAlertDiloag(GroupVideoChatActivity.this, description, null);
    }

    @Override
    public void onConnectedOnConference(String memberId) {
        /*
        if (arrMembersOfConference == null)
            return;

        iceConnected = 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgNavCover.setVisibility(View.GONE);
            }
        });

        for (int i=0; i<arrMembersOfConference.size(); i++) {
            RemoteViewVO peerConsVO = arrMembersOfConference.get(i);
            if (peerConsVO.getUser_id().equals(memberId)) {
                peerConsVO.setIceConnected(ConstValues.ICE_CONNECTED);
                break;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        */
    }

    @Override
    public void onClosedOnConference(String memberId, int closeType) {
        /* if (arrMembersOfConference == null)
            return;

        for (int i=0; i<arrMembersOfConference.size(); i++) {
            RemoteViewVO peerConsVO = arrMembersOfConference.get(i);
            if (peerConsVO.getUser_id().equals(memberId) && peerConsVO.getIceConnected() != ConstValues.ICE_CLOSED) {
                hangupConference(memberId, closeType);
                break;
            }
        } */
    }

    private LinkedList<PeerConnection.IceServer> iceServersFromPCConfigJSON(
            JSONObject responseData) throws JSONException {
        JSONArray servers = responseData.getJSONArray("iceServers");
        LinkedList<PeerConnection.IceServer> ret =
                new LinkedList<PeerConnection.IceServer>();
        for (int i = 0; i < servers.length(); ++i) {
            JSONObject server = servers.getJSONObject(i);
            String url = server.getString("url");
            if (url.startsWith("turn:")) {
                String username = "";
                if (!server.getString("username").equals(""))
                    username = server.getString("username");

                String password = "";
                if (!server.getString("credential").equals(""))
                    password = server.getString("credential");

                ret.add(new PeerConnection.IceServer(url, username, password));
            } else
            {
                String credential =
                        server.has("credential") ? server.getString("credential") : "";
                ret.add(new PeerConnection.IceServer(url, "", credential));
            }
        }
        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_USER_ACTIVITY:
                    if (data != null)
                    {
                        sendingOfferToInvitationMembers();
                    }
                    break;
            }
        }
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    @Override
    public void onPeerLocalStreamRender(MediaStream localStream, PeerConnectionFactory factory, boolean isInstance) {
        mediastream = localStream;
        mainFactory = factory;

        if (isInstance)
        {
            startTimer();

            if (isIniatedChat)
            {
                VideoMemberVO currCon = MyApp.getInstance().g_currMemberCon;

                for (int i = 0; i < MyApp.getInstance().g_videoMemberList.size(); i++) {
                    final VideoMemberVO memberCon = MyApp.getInstance().g_videoMemberList.get(i);

                    if (memberCon.isYounger() == true && memberCon.getUserId() != currCon.getUserId()) {
                        AppRTCClient newClient = new serverCommClient(GroupVideoChatActivity.this, boardId, callType, iceServers, memberCon.getUserId());
                        newClient.setIsInitiator(true);
                        newClient.connectToRoomWithStream(localStream);
                        newClient.sendOffer();

                        PeerConsVO onePeerCon = new PeerConsVO();
                        onePeerCon.setPeerCons(newClient);
                        onePeerCon.setUser_id(memberCon.getUserId());
                        onePeerCon.setCreateTime(System.currentTimeMillis());
                        onePeerCon.setType("sender");

                        arrARDAppClients.add(onePeerCon);
                    }
                }
            }
            else
            {
                VideoMemberVO currCon = MyApp.getInstance().g_currMemberCon;

                for (int i = 0; i < MyApp.getInstance().g_videoMemberList.size(); i++) {
                    VideoMemberVO memberCon = MyApp.getInstance().g_videoMemberList.get(i);

                    if (memberCon.isYounger() == true && memberCon.getUserId() != currCon.getUserId()) {
                        AppRTCClient newClient = new serverCommClient(GroupVideoChatActivity.this, boardId, callType, iceServers, memberCon.getUserId());
                        newClient.setIsInitiator(true);
                        newClient.connectToRoomWithStream(localStream);
                        newClient.sendOffer();

                        PeerConsVO onePeerCon = new PeerConsVO();
                        onePeerCon.setPeerCons(newClient);
                        onePeerCon.setUser_id(memberCon.getUserId());
                        onePeerCon.setCreateTime(System.currentTimeMillis());
                        onePeerCon.setType("sender");
                        arrARDAppClients.add(onePeerCon);
                    } else if (memberCon.isOlder() == true) {
                        if (memberCon.getUserId() != currCon.getUserId()) {
                            AppRTCClient newClient = new serverCommClient(GroupVideoChatActivity.this, boardId, callType, iceServers, memberCon.getUserId());
                            newClient.setIsInitiator(false);
                            newClient.connectToRoomWithStream(localStream);

                            if (MyApp.getInstance().userIdsForSDP != null && MyApp.getInstance().userIdsForSDP.contains(memberCon.getUserId())) {
                                newClient.getRemoteSDPData(memberCon.getUserId());
                                MyApp.getInstance().userIdsForSDP.remove(memberCon.getUserId());
                            }
                            if (MyApp.getInstance().userIdsForCandidate != null && MyApp.getInstance().userIdsForCandidate.contains(memberCon.getUserId())) {
                                newClient.getRemoteIceCandidate(memberCon.getUserId());
                                MyApp.getInstance().userIdsForCandidate.remove(memberCon.getUserId());
                            }

                            PeerConsVO onePeerCon = new PeerConsVO();
                            onePeerCon.setPeerCons(newClient);
                            onePeerCon.setUser_id(memberCon.getUserId());
                            onePeerCon.setType("answer");
                            onePeerCon.setCreateTime(System.currentTimeMillis());
                            arrARDAppClients.add(onePeerCon);
                        }
                    } else
                    {
                        if (memberCon.getUserId() != currCon.getUserId())
                        {
                            AppRTCClient newClient = new serverCommClient(GroupVideoChatActivity.this, boardId, callType, iceServers, memberCon.getUserId());
                            newClient.connectToRoomWithStream(localStream);

                            PeerConsVO onePeerCon = new PeerConsVO();
                            onePeerCon.setPeerCons(newClient);
                            onePeerCon.setUser_id(memberCon.getUserId());
                            onePeerCon.setType("sender");
                            onePeerCon.setCreateTime(System.currentTimeMillis());
                            arrARDAppClients.add(onePeerCon);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = 1;
                callConnected();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = 2;
                disconnect(1);
            }
        });
    }

    // Should be called from UI thread
    private void callConnected() {
        if (peerClient == null || isError) {
            return;
        }

        // Update video view.
        updateLocalVideoView();
        // Enable statistics callback.
        peerClient.enableStatsEvents(true, 1000);
    }

    @Override
    public void onCloseChatUpdated() {
        if (mCustomDialog != null) {
            mCustomDialog.hide();
            mCustomDialog.hideKeyboard();
            btnClose.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        if (mGreenToolTipView == toolTipView)
            mGreenToolTipView = null;
    }

    @Override
    public void onMessageUpdated(ImMessageVO msg) {
        arrivedTick = System.currentTimeMillis();

        imgTipProfile.setImageUrl(getUserPhoto(msg.getFrom()), imgLoader);
        imgTipProfile.invalidate();

        imgTipProfile.setVisibility(View.VISIBLE);
        if (mGreenToolTipView == null)
            addGreenToolTipView(msg.getContent());
        else {
            mGreenToolTipView.remove();
            mGreenToolTipView = null;

            addGreenToolTipView(msg.getContent());
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long currTime = System.currentTimeMillis();
                if (currTime > arrivedTick + 1400) {
                    mGreenToolTipView.remove();
                    mGreenToolTipView = null;
                    imgTipProfile.setVisibility(View.GONE);
                }
            }
        }, 1500);

    }

    public String getUserPhoto(int userId)
    {
        if (this.lstUsers == null || this.lstUsers.size() == 0)
            return "";

        for (int i = 0; i < this.lstUsers.size(); i++) {
            EventUser member = this.lstUsers.get(i);
            if (member.getUserId() == userId) {
                return member.getPhotoUrl();
            }
        }

        return "";
    }

    public class MsgReceiver extends BroadcastReceiver {
        public MsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");
            String message = bundle.getString("message");
            String uid = bundle.getString("uid");

            if (mCustomDialog != null)
                mCustomDialog.checkNewMessages();

            displayMessageEvent(message, uid);
        }
    }

    private void displayMessageEvent(String message, String uid)
    {
        arrivedTick = System.currentTimeMillis();

        imgTipProfile.setImageUrl(getUserPhoto(Integer.valueOf(uid)), imgLoader);
        imgTipProfile.invalidate();

        imgTipProfile.setVisibility(View.VISIBLE);
        if (mGreenToolTipView == null)
            addGreenToolTipView(message);
        else {
            mGreenToolTipView.remove();
            mGreenToolTipView = null;

            addGreenToolTipView(message);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long currTime = System.currentTimeMillis();
                if (currTime > arrivedTick + 1400) {
                    if (mGreenToolTipView != null)
                    {
                        mGreenToolTipView.remove();
                        mGreenToolTipView = null;
                    }
                    imgTipProfile.setVisibility(View.GONE);
                }
            }
        }, 1500);
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate){
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {

        }

        @Override
        public void onSignalingChange(
                PeerConnection.SignalingState newState) {

        }

        @Override
        public void onIceConnectionChange(
                final PeerConnection.IceConnectionState newState) {
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
        }

        @Override
        public void onIceGatheringChange(final PeerConnection.IceGatheringState newState) {
        }

        @Override
        public void onAddStream(final MediaStream stream){
        }

        @Override
        public void onRemoveStream(final MediaStream stream){
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
        }

        @Override
        public void onRenegotiationNeeded() {
        }
    }
    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(final String error) {
        }

        @Override
        public void onSetFailure(final String error) {

        }
    }

    public class VideoRequestReceiver extends BroadcastReceiver {
        public VideoRequestReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            final String type = bundle.getString("type", "");

            if (type.equals("sdp_available"))
            {
                String uid = bundle.getString("from");

                for (int j=0; j< arrARDAppClients.size(); j++)
                {
                    PeerConsVO peerOne = arrARDAppClients.get(j);
                    if (uid.equals(peerOne.getUser_id())) {
                        peerOne.getPeerCons().getRemoteSDPData(uid);
                    }
                }

            } else if (type.equals("candidates_available"))
            {
                String uid = bundle.getString("from");

                for (int j=0; j< arrARDAppClients.size(); j++)
                {
                    PeerConsVO peerOne = arrARDAppClients.get(j);
                    if (uid.equals(peerOne.getUser_id())) {
                        peerOne.getPeerCons().getRemoteIceCandidate(uid);
                    }
                }
            } else if (type.equals("hangup"))
            {
                String memId = bundle.getString("from");

                hangupConference(memId, 1);

            } else if (type.equals("inviting"))
            {
                sendingOfferToInvitationMembers();
            } else if (type.equals("videoon") || type.equals("videooff") || type.equals("audiooff") || type.equals("audioon")) {
                String memId = bundle.getString("from");
                int valueId = bundle.getInt("value");
                reloadMembersWithStatus(memId, valueId);
            }
        }
    }

    private void addGreenToolTipView(String text) {
        ToolTip toolTip = new ToolTip()
                .withText(text)
                .withTextColor(Color.WHITE)
                .withColor(getResources().getColor(R.color.font_selector_color17));

        mGreenToolTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.imgBtnChatView));
        mGreenToolTipView.setOnToolTipViewClickedListener(this);
    }
}
