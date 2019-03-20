package com.ginko.activity.im;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.ImageSpan;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.group.GroupChatMembersActivity;
import com.ginko.activity.group.GroupDetailActivity;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.api.request.IMRequest;
import com.ginko.common.CirclePageIndicator;
import com.ginko.common.Logger;
import com.ginko.common.MyDataUtils;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ChatTableModel;
import com.ginko.database.ContactStruct;
import com.ginko.database.MessageDbConstruct;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.IMBoardMessage;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImMessageVO;
import com.ginko.vo.VideoMemberVO;
import com.sz.util.json.JsonConverter;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class ImBoardActivity extends MyBaseFragmentActivity implements OnClickListener ,
        EmoticonsGridAdapter.EmoticonKeyClickListener ,
        MessageAdapter.onDeleteMessageListener ,
        PullToRefreshListView.OnSinglePointTouchListener ,
        ImInputEditTExt.OnEditTextKeyDownListener,
        ActionSheet.ActionSheetListener
{

    /* Layout variables */
    private final int PURPLE_ACTIVITY_START = 100;
    private final int INT_EXTRA_PURPLE  = 1133;

    private TextView txtContactName;
    private ImageButton btnPrev;
    private ImageView btnClose, btnDeleteAllMessage, btnDeleteChatHistory;
    private ImageView btnGinkoCall;
    private ImageView btnEmoticonPuple1, btnEmoticonPuple2, btnEmoticonPuple3, btnEmoticonPuple4;
    private ImageView imgBtnGetLocation, imgBtnGetPhoto, imgBtnGetVideo, imgBtnEmoticon;
    private ImageView imgBtnMic, imgMicSel, imgBtnSendMessage, imgBtnEditChat;

    private RelativeLayout rootLayout;
    private LinearLayout emoticonTypeLayout, chatInputMethodLayout;
    private RelativeLayout chatInputLayout, trashlayout;
    private RelativeLayout messageLayout;
    private LinearLayout emoticonsCover;

    private static ImBoardActivity mInstance;

    private ImInputEditTExt edtMessage; //text message input field
    private PullToRefreshListView pullToRefreshView;
    private ListView messageListView;

    private View popUpView; //emoticon popup view
    private CirclePageIndicator emoticonCirclePageIndicator;
    private PopupWindow popupWindow;

    private ProgressHUD progressDialog;

    /* Valuables */
    private int boardId;
    private int prevBoardId = 0;
    private String contactName = "";
    private ImBoardVO board;

    private MessageAdapter adapter;
    private ArrayList<EventUser> lstUsers;

    private boolean isGettingCurrentLocation = false;
    private boolean isDeletingMessage = false;
    private boolean isReceiverRegistered = false;
    private boolean isChangedContact = false;
    private boolean isUILoaded = false;
    private boolean isFetchingNewMessage = false;
    private boolean isFetchingPrevMessage = false;
    /*Chat related variables */
    private boolean isKeyBoardVisible = false;
    private boolean isInterfaceDisabled = false;
    private boolean isFavorite = false;
    private boolean isPurpleClass = false;
    private boolean isVideoChat = false;
    private boolean isChatOnly = false;
    private int mChatEnableCnt;

    private int keyboardHeight;
    private int emoticonHeight;

    private EmoticonUtility emoticons;

    private int nEmoticonType = 3; /*   default is 3
                                        0: puple1 , 1: puple2 , 2: puple3 , 3: pupule4*/
    private int inputMethodType = 0; /* default is 0
                                        0: record voice , and ready to input status , keyboard is not shown , emoticon is not shown
                                        1: input message/emoticon , keyboard/emoticon is shown
                                        2: editable state */

    /* voice record variables */
    private boolean isVoiceRecording = false;
    private int recordedVoiceTime = 0;
    private long voiceRecordStartTime = 0;
    private String strVoiceRecordFilePath = "";
    private String strEditText = "";
    private SoundMeter mSensor;
    private final int REFRESH_INTERVAL = 1000;

    private long lastClickedTime = 0;//to check the double click event on the message listview to hide keyboard
    private float x = 0;
    private float y = 0;

    //---------------------------------//
    private boolean isNoMorePrevMessage = false;
    private boolean noMoreMessage = false;
    private boolean fetchByNumber = false;
    private Date earliestMsgSentTime , latestMsgSentTime;
    private String groupName = null;

    //---------------------------------//
    /* new message listener */
    private MsgReceiver receiver;
    private ContactChangedReceiver contactChangedReceiver;

    /* Location Manager to get current location info */
    private LocationManager lm;
    private Location latestLocation = null;
    private Handler locationHandler;
    private Looper locationLooper;
    private Object lockObj = new Object();
    private Object syncMessagesObj = new Object();

    private ChatTableModel chatTableModel;

    private ActionSheet shareViaEmailActionSheet = null;
    public static ImMessageVO sharingMessage = null;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final int GET_CURRENT_LOCATION_TIME_OUT = 10000;

    private String mTempDirectory = RuntimeContext.getAppDataFolder("temp");

    private Runnable locationTimeoutRunnable = new Runnable() {
        public void run() {
            synchronized (lockObj) {
                lm.removeUpdates(locationListener);
                if (progressDialog != null)
                    progressDialog.dismiss();
                isGettingCurrentLocation = false;
                MyApp.getInstance().showSimpleAlertDiloag(ImBoardActivity.this ,R.string.str_failed_to_get_current_location , null);
            }
        }
    };


    private final int UPDATE_VOICE_RECORDING_STATUS = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VOICE_RECORDING_STATUS:

                    break;
            }
        }
    };

    private final int ADD_LOCATION_MESSAGE = 1;
    private final int ADD_PHOTO_MESSAGE = 5;
    private final int ADD_VIDEO_MESSAGE = 3;

    private EmoticonsPagerAdapter[] emoticonAdapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_board);

        chatTableModel = MyApp.getInstance().getChatDBModel();
        mInstance = this;

        this.isReceiverRegistered = false;
        this.isChangedContact = false;
        this.isUILoaded = false;
        this.noMoreMessage = false;
        this.isNoMorePrevMessage = false;
        this.fetchByNumber = false;
        this.earliestMsgSentTime = null;
        this.latestMsgSentTime = null;

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            groupName = getIntent().getStringExtra("groupname");
        }catch (Exception e){e.printStackTrace();
            groupName = null;
        }
        boardId = getIntent().getIntExtra("board_id", 0);
        contactName = getIntent().getStringExtra("contact_name");
        isPurpleClass = getIntent().getBooleanExtra("PurpleContact", false);
        this.lstUsers = new ArrayList<EventUser>();
        this.lstUsers = (ArrayList<EventUser>) getIntent().getSerializableExtra("userData");
        this.isVideoChat = getIntent().getBooleanExtra("isVideoChat", false);
        this.isChatOnly = getIntent().getBooleanExtra("isChatOnly", false);

        board = (ImBoardVO) getIntent().getSerializableExtra("board");

        //board = new ImBoardVO();
        /*String strJson = "{\"data\":{\"created_by\":567,\"created_time\":\"2014-10-08 16:35:30\",\"board_id\":97,\"members\":[{\"memberinfo\":{\"mname\":\"\",\"lname\":\"Stefan\",\"user_id\":531,\"photo_url\":\"http:\\/\\/www.xchangewith.me\\/api\\/v2\\/Photos\\/no-face.png\",\"fname\":\"RJ\"},\"joinTime\":\"2014-10-08 16:35:30\",\"is_left\":false},{\"memberinfo\":{\"mname\":\"\",\"lname\":\"Max\",\"user_id\":567,\"photo_url\":\"http:\\/\\/www.xchangewith.me\\/api\\/v2\\/Photos\\/no-face.png\",\"fname\":\"Mad\"},\"joinTime\":\"2014-10-08 16:35:30\",\"is_left\":false}],\"last_active_time\":\"2015-04-24 02:26:46\",\"recent_messages\":[]},\"success\":true,\"request_id\":\"20150424145914-JXZR608805181U88\"}";
        JsonResponse<ImBoardVO> response = new JsonResponse<ImBoardVO>(strJson, ImBoardVO.class);
        board = response.getData();
        boardId = board.getBoardId();*/
        btnGinkoCall = (ImageView) findViewById(R.id.btnGinkoCall);
        btnGinkoCall.setOnClickListener(this);

        if (board == null) {
            String contactIds = getIntent().getStringExtra("contact_ids");
            if (contactIds != null && !contactIds.equals("")) {
                createBoard(contactIds);
            }
            else if(boardId > 0)//if boardId has value , but contactIds is empty string , then its from gcm service intent
            {
                getBoardInfo();
            }
        } else {
            getBoardInfo();
        }

    }

    public void createBoard(final String contactIds) {
        IMRequest.createBoard(contactIds, new ResponseCallBack<ImBoardVO>() {

            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    board = response.getData();
                    if (isVideoChat) {
                        board.setVideoChat(true);
                        board.setLstUsers(lstUsers);
                    }

                    boardId = board.getBoardId();
                    getUIObjects();
                    fillData();

                    IMRequest.getGetBoardInfo(boardId, new ResponseCallBack<ImBoardVO>() {
                        @Override
                        public void onCompleted(JsonResponse<ImBoardVO> response) {
                            if (response.isSuccess()) {
                                boolean allowChat = false;
                                ImBoardVO imBoardVO = response.getData();
                                if (imBoardVO == null)
                                    return;
                                for (int i = 0; i < imBoardVO.getMembers().size(); i++) {
                                    boolean isDelected = true;

                                    for (int j = 0; j < MyApp.g_contactItems.size(); j++) {
                                        ContactItem item = MyApp.g_contactItems.get(j);
                                        if (item.getContactType() == 1)//purple contact
                                        {
                                            if (item.getContactId() == imBoardVO.getMembers().get(i).getUser().getUserId()) {
                                                isDelected = false;
                                                continue;
                                            }
                                        }
                                    }
                                    if (imBoardVO.getMembers().get(i).isFriend() && !isDelected)
                                        allowChat = true;
                                    else if (imBoardVO.getMembers().get(i).isInDirectory())
                                        allowChat = true;
                                }
                                if (!allowChat) {
                                    disabledForChat();
                                } else {
                                    enableForChat();
                                }
                            }
                            //add by wang
                            enableOrDisableEditBTN();
                        }
                    }, false);
                } else {
                    if (response.getErrorCode() == 9999) {
                        DialogInterface.OnClickListener dlgListner = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createBoard(contactIds);
                            }
                        };

                        MyApp.getInstance().showSimpleAlertDiloag(ImBoardActivity.this, "Internet connection is missing.", dlgListner);
                    }
                }
            }
        });
    }
    public void getBoardInfo()
    {
        IMRequest.getGetBoardInfo(boardId, new ResponseCallBack<ImBoardVO>() {

            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    ImBoardVO imBoardVO = response.getData();
                    board = response.getData();
                    if (isVideoChat) {
                        board.setVideoChat(true);
                        board.setLstUsers(lstUsers);
                    }
                    // check exist contact on list.

                    boolean allowChat = false;
                    if (imBoardVO == null)
                        return;
                    if (!imBoardVO.isGroup()) {
                        mChatEnableCnt = 0;
                        for (int i = 0; i < imBoardVO.getMembers().size(); i++) {
                            ContactStruct itemStruct = MyApp.getInstance().getContactsModel().getContactById(imBoardVO.getMembers().get(i).getUser().getUserId());
                            if (itemStruct != null)
                            {
                                ContactItem item = itemStruct.getContactItem();
                                if (item != null && imBoardVO.getMembers().get(i).isFriend())
                                    allowChat = true;
                                if (item != null && item.getSharingStatus() != 4)
                                    mChatEnableCnt++;
                                else
                                    board.getMembers().get(i).setChatOnly(true);
                            }
                            else if (imBoardVO.getMembers().get(i).isInDirectory())
                                allowChat = true;
                        }
                        if (mChatEnableCnt >0 )
                        {
                            btnGinkoCall.setVisibility(View.VISIBLE);
                        }else{
                            btnGinkoCall.setVisibility(View.INVISIBLE);
                        }
                    } else
                        allowChat = true;

                    getUIObjects();
                    fillData();
                    if (!allowChat)
                        disabledForChat();
                    else
                        enableForChat();

                } else {
                    if (response.getErrorCode() == 9999) {
                        DialogInterface.OnClickListener dlgListner = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getBoardInfo();
                            }
                        };

                        MyApp.getInstance().showSimpleAlertDiloag(ImBoardActivity.this, "Internet connection is missing.", dlgListner);
                    }
                }
            }
        }, true);
    }

    public static ImBoardActivity getInstance()
    {
        return ImBoardActivity.mInstance;
    }

    public void shareMessageViaEmail(ImMessageVO msg)
    {
        sharingMessage = msg;
        setTheme(R.style.ActionSheetStyleIOS7);

        if(shareViaEmailActionSheet == null)
            shareViaEmailActionSheet = ActionSheet.createBuilder(ImBoardActivity.this , getSupportFragmentManager())
                    .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                    .setOtherButtonTitles(
                            getResources().getString(R.string.str_share_via_email))
                    .setCancelableOnTouchOutside(true)
                    .setListener(this)
                    .show();
        else
            shareViaEmailActionSheet.show(getSupportFragmentManager() , "actionSheet");
    }

    private void fillData() {
        /*set title of screen*/
        if(groupName!=null)
            txtContactName.setText(groupName);
        else if (this.board != null) {
            if (this.board.isGroup())
                txtContactName.setText(board.getBoardName());
            else
            {
                List<ImBoardMemeberVO> memberArry = new ArrayList<ImBoardMemeberVO>();
                memberArry = this.board.getMembers();
                try {
                    Collections.sort(memberArry, memberArryComparator);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String title = "";
                int chatMemberCount = 0;

                if (contactName != null && !contactName.equals(""))
                    title = contactName;

                for (int i = 0; i < memberArry.size(); i++) {
                    ImBoardMemeberVO member = memberArry.get(i);
                    if (RuntimeContext.isLoginUser(member.getUser().getUserId())) {
                        continue;
                    }
                    if(title.compareTo("") == 0 && (contactName == null || contactName.equals("")) )
                        title = member.getUser().getFullName();
                    chatMemberCount++;
                }

                if(chatMemberCount > 1)
                    txtContactName.setText(title+"+"+String.valueOf(chatMemberCount-1));
                else
                    txtContactName.setText(title);
            }
        }

        //first try to get messages from local database
        //Integer number = 40;
        Integer number = 10;
        List<ImMessageVO> messages = chatTableModel.getLatestChatsByTime(boardId , null , false , number);

        if(messages!= null && messages.size() > 0)
        {
            earliestMsgSentTime = getFirstMessageTime(messages);
            latestMsgSentTime = getLastMessageTime(messages);

            adapter.addMessageItemsToTop(messages);
            adapter.updateListView();
            this.isFetchingNewMessage = false;
            this.isFetchingPrevMessage = false;
            scrollToLastPosition(300);

            //check the latest message from server , because even though the message is stored in the database ,
            // it could be not latest messages if user didn't open the app for a few days
            //so just check it again
            syncLatestMessages();
        }
        else//if there isn't any message stored in the database , then fetch history from server
        {
            this.pullToRefreshView.setRefreshing();

            fetchHistory(null, null);
        }
        MyApp.getInstance().setCurrentBoardId(boardId);
    }

    private final static Comparator<ImBoardMemeberVO> memberArryComparator = new Comparator<ImBoardMemeberVO>()
    {

        @Override
        public int compare(ImBoardMemeberVO lhs, ImBoardMemeberVO rhs) {
            int result = 0;
            String leftName = null , rightName = null;

            leftName = lhs.getUser().getFullName().toLowerCase();
            rightName = rhs.getUser().getFullName().toLowerCase();

            char leftFirstLetter = leftName.charAt(0);
            char rightFirstLetter = rightName.charAt(0);

            if(!((leftFirstLetter >= 'a' && leftFirstLetter <= 'z') || (leftFirstLetter >= 'A' && leftFirstLetter <= 'Z')))
                return 1;
            if(!((rightFirstLetter >= 'a' && rightFirstLetter <= 'z') || (rightFirstLetter >= 'A' && rightFirstLetter <= 'Z')))
                return -1;

            if(leftName.compareTo(rightName)<0)
                result = -1;
            else if(leftName.compareTo(rightName) == 0)
                result = 0;
            else if(leftName.compareTo(rightName)>0)
                result = 1;

            return result;
        }
    };

    private synchronized Date getFirstMessageTime(List<ImMessageVO> messages)
    {
        Date earlyTime = null;
        if(messages == null || messages.size() <= 0)
            return earlyTime;
        synchronized (syncMessagesObj)
        {
            int index = 0;
            if (earlyTime == null) {
                for(index=0;index<messages.size();index++)
                {
                    if(messages.get(index).utcSendTime != null) {
                        earlyTime = messages.get(index).utcSendTime;
                        break;
                    }
                }
            }
            int size = messages.size();
            for (int i = index+1; i < size; i++) {
                if(messages.get(i).utcSendTime == null)
                    continue;
                if (earlyTime.getTime() > messages.get(i).utcSendTime.getTime())
                    earlyTime = messages.get(i).utcSendTime;
            }
        }

        return earlyTime;
    }

    private synchronized Date getLastMessageTime(List<ImMessageVO> messages)
    {
        Date latestTime = null;
        if(messages == null || messages.size() <= 0)
            return latestTime;
        synchronized (syncMessagesObj)
        {
            int index = 0;
            if (latestTime == null) {
                for(index=0;index<messages.size();index++)
                {
                    if(messages.get(index).utcSendTime != null) {
                        latestTime = messages.get(index).utcSendTime;
                        break;
                    }
                }
            }
            int size = messages.size();
            for (int i = index+1; i < size; i++) {
                if(messages.get(i).utcSendTime == null)
                    continue;
                if(latestTime.getTime() < messages.get(i).utcSendTime.getTime())
                    latestTime = messages.get(i).utcSendTime;
            }
        }
        return latestTime;
    }

    @Override
    protected void getUIObjects() {
        super.getUIObjects();
        txtContactName = (TextView) findViewById(R.id.txtContactName);
        txtContactName.setText(contactName);

        txtContactName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(board != null && board.getMembers().size() > 2)
                {
                    if (!isVoiceRecording)
                    {
                        Intent groupChatMemberActivity = new Intent(ImBoardActivity.this , GroupChatMembersActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("board" , board);
                        groupChatMemberActivity.putExtras(bundle);
                        startActivity(groupChatMemberActivity);
                    }
                } else if (board.isGroup() && !isVoiceRecording)
                {
                    if (adapter != null && adapter.getMemberInfo() != null)
                    {
                        board.setMemberList(adapter.getMemberInfo());
                        Intent groupChatMemberActivity = new Intent(ImBoardActivity.this , GroupChatMembersActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("board" , board);
                        groupChatMemberActivity.putExtras(bundle);
                        startActivity(groupChatMemberActivity);
                    }
                }
            }
        });

        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(this);
        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);

//        btnGinkoCall = (ImageView) findViewById(R.id.btnGinkoCall);
//        btnGinkoCall.setOnClickListener(this);

        if (isVideoChat || isChatOnly)
        {
            btnGinkoCall.setVisibility(View.GONE);
        }

        btnDeleteAllMessage = (ImageView) findViewById(R.id.btnDeleteAllMessage);
        btnDeleteAllMessage.setOnClickListener(this);
        btnDeleteChatHistory = (ImageView) findViewById(R.id.imgDeleteChatHistory);
        btnDeleteChatHistory.setOnClickListener(this);

        //buttons to select emoticon type
        btnEmoticonPuple1 = (ImageView) findViewById(R.id.emoticonPuple1);
        btnEmoticonPuple1.setOnClickListener(this);
        btnEmoticonPuple2 = (ImageView) findViewById(R.id.emoticonPuple2);
        btnEmoticonPuple2.setOnClickListener(this);
        btnEmoticonPuple3 = (ImageView) findViewById(R.id.emoticonPuple3);
        btnEmoticonPuple3.setOnClickListener(this);
        btnEmoticonPuple4 = (ImageView) findViewById(R.id.emoticonPuple4);
        btnEmoticonPuple4.setOnClickListener(this);

        /* chat input method buttons */
        imgBtnGetLocation = (ImageView) findViewById(R.id.imgBtnLocation);
        imgBtnGetLocation.setOnClickListener(this);
        imgBtnGetPhoto = (ImageView) findViewById(R.id.imgBtnPhoto);
        imgBtnGetPhoto.setOnClickListener(this);
        imgBtnGetVideo = (ImageView) findViewById(R.id.imgBtnVideo);
        imgBtnGetVideo.setOnClickListener(this);
        imgBtnEmoticon = (ImageView) findViewById(R.id.imgBtnEmoticon);
        imgBtnEmoticon.setOnClickListener(this);

        /* chat related buttons */
        imgBtnMic = (ImageView) findViewById(R.id.imgBtnMic);
        imgBtnMic.setOnClickListener(this);
        imgMicSel = (ImageView) findViewById(R.id.imgRecordingMic);
        imgBtnSendMessage = (ImageView) findViewById(R.id.imgBtnSendMessage);
        imgBtnSendMessage.setOnClickListener(this);
        imgBtnEditChat = (ImageView) findViewById(R.id.imgBtnEditChat);
        imgBtnEditChat.setOnClickListener(this);

        /* text Message input field */
        edtMessage = (ImInputEditTExt) findViewById(R.id.textMessage);

        edtMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isVoiceRecording) {
                    edtMessage.setFocusableInTouchMode(false);
                    edtMessage.setFocusable(false);
                    hideKeyboard();
                } else {
                    edtMessage.setFocusableInTouchMode(true);
                    edtMessage.setFocusable(true);
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                        //if (!isKeyBoardVisible) {
                        inputMethodType = 3;
                        updateChatControllerButtons();
                        //}
                        return false;
                    }
                    if (!isKeyBoardVisible) {
                        inputMethodType = 3;
                        updateChatControllerButtons();
                        showKeyboard();
                        btnGinkoCall.setVisibility(View.GONE);
                        btnClose.setVisibility(View.VISIBLE);
                    }

                }
                return false;
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {

                                              @Override
                                              public void beforeTextChanged(CharSequence s, int start, int count,
                                                                            int after) {
                                                  // TODO Auto-generated method stub
                                              }

                                              @Override
                                              public void onTextChanged(CharSequence s, int start, int before,
                                                                        int count) {
                                                  // TODO Auto-generated method stub
                                                  if (count > 0) {
                                                      imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage);
                                                      imgBtnSendMessage.setTag(R.drawable.btnchat_sendmessage);
                                                  } else {
                                                      imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage_disable);
                                                      imgBtnSendMessage.setTag(R.drawable.btnchat_sendmessage_disable);
                                                  }
                                              }

                                              @Override
                                              public void afterTextChanged(Editable s) {
                                                  // TODO Auto-generated method stub
                                                  if (s.length() > 0) {
                                                      imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage);
                                                      imgBtnSendMessage.setTag(R.drawable.btnchat_sendmessage);
                                                  } else {
                                                      imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage_disable);
                                                      imgBtnSendMessage.setTag(R.drawable.btnchat_sendmessage_disable);
                                                  }
                                              }
                                          }
        );
        edtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        edtMessage.registerOnBackKeyListener(this);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        emoticonTypeLayout = (LinearLayout) findViewById(R.id.emoticonTypeLayout);
        chatInputMethodLayout = (LinearLayout) findViewById(R.id.chatInputMethodLayout);
        chatInputLayout = (RelativeLayout) findViewById(R.id.chatInputLayout);
        trashlayout = (RelativeLayout) findViewById(R.id.trashlayout);
        emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

        //list view for chat messages
        pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pullToRefreshView);
        messageListView = (ListView) findViewById(R.id.messageListView);

        pullToRefreshView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String label = DateUtils.formatDateTime(getApplicationContext(), Calendar.getInstance().getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                System.out.println("----Pull to Refresh called---");
                // Do work to refresh the list here.
                fetchPrevHistory();
            }
        });

        this.adapter = new MessageAdapter(this, board);
        this.adapter.setOnDeleteMessageListener(this);

        pullToRefreshView.setAdapter(adapter);
        this.adapter.registerDownloadManager();

        pullToRefreshView.setOnSinglePointTouchListener(this);

        messageLayout = (RelativeLayout) findViewById(R.id.messageLayout);

        this.emoticons = MyApp.getInstance().getEmoticonUtility();
        this.emoticonAdapters = new EmoticonsPagerAdapter[4];
        for(int i =0 ;i<4;i++)
        {
            ArrayList<Integer> emoticonIndexes = new ArrayList<Integer>();
            for(int j = 0; j<EmoticonUtility.EMOTICON_COUNTS[i];j++)
            {
                emoticonIndexes.add(Integer.valueOf(j+EmoticonUtility.EMOTICON_TYPE_START_INDEX[i]));
            }
            emoticonAdapters[i] = new EmoticonsPagerAdapter(this , emoticonIndexes , this);
        }

        popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
        emoticonCirclePageIndicator = (CirclePageIndicator) popUpView.findViewById(R.id.page_indicator);

        // Defining default height of keyboard which is equal to 230 dip
        final float popUpheight = getResources().getDimension(
                R.dimen.keyboard_height);
        changeKeyboardHeight((int)popUpheight);

        emoticonHeight = getResources().getDimensionPixelSize(R.dimen.emoticon_height);

        enablePopUpView();
        checkKeyboardHeight(rootLayout);

        imgBtnMic.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENDED:
                        break;
                    case DragEvent.ACTION_DROP:
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:

                        break;
                }
                return false;
            }
        });

        imgBtnMic.setOnTouchListener(new View.OnTouchListener() {
            @Override

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isVoiceRecording) {
                            isVoiceRecording = true;
                            adapter.setVoiceRecord(isVoiceRecording);
                            voiceRecordStartTime = System.currentTimeMillis();
                            strVoiceRecordFilePath = RuntimeContext.getAppDataFolder("Messages_Temp") + "voice_" + String.valueOf(voiceRecordStartTime) + ".aac";
                            adapter.stopAllRecords();
                            imgBtnGetLocation.setAlpha(0.5f);
                            imgBtnGetPhoto.setAlpha(0.5f);
                            imgBtnGetVideo.setAlpha(0.5f);
                            imgBtnEmoticon.setAlpha(0.5f);
                            strEditText = edtMessage.getText().toString();
                            startVoiceRecord(strVoiceRecordFilePath);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (!isVoiceRecording){

                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        long diff = System.currentTimeMillis() - voiceRecordStartTime;
                        imgBtnGetLocation.setAlpha(1.0f);
                        imgBtnGetPhoto.setAlpha(1.0f);
                        imgBtnGetVideo.setAlpha(1.0f);
                        imgBtnEmoticon.setAlpha(1.0f);

                        if (isVoiceRecording && diff < 1000) {
                            adapter.setVoiceRecord(false);
                            stopVoiceRecording();
                            isVoiceRecording = false;

                            edtMessage.setText(strEditText);
                            imgMicSel.setVisibility(View.INVISIBLE);
                            imgBtnEditChat.setVisibility(View.VISIBLE);
                            File file = new File(strVoiceRecordFilePath);
                            if (file.exists())
                                file.delete();
                            return true;
                        }

                        if (isVoiceRecording) {
                            adapter.setVoiceRecord(false);
                            stopVoiceRecording();

                            //send recorded voice message
                            if (event.getX() + imgBtnMic.getLeft() < chatInputLayout.getWidth() / 2)//slide to cancel
                            {
                                //delete recorded voice file
                                isVoiceRecording = false;
                                edtMessage.setText(strEditText);
                                imgMicSel.setVisibility(View.INVISIBLE);
                                imgBtnEditChat.setVisibility(View.VISIBLE);
                                File file = new File(strVoiceRecordFilePath);
                                if (file.exists())
                                    file.delete();
                            } else {
                                File file = new File(strVoiceRecordFilePath);
                                if (file.exists()) {
                                    //confirm dialog to send voice message or not
                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    //Yes button clicked
                                                    //send recorded voice message
                                                    Properties param = new Properties();
                                                    param.setProperty("voice_length", String.valueOf(ImBoardActivity.this.recordedVoiceTime) + "");
                                                    sendMediaMessage(ImMessageVO.MSG_VOICE, ImBoardActivity.this.strVoiceRecordFilePath, param);
                                                    isVoiceRecording = false;
                                                    edtMessage.setText(strEditText);
                                                    imgMicSel.setVisibility(View.INVISIBLE);
                                                    imgBtnEditChat.setVisibility(View.VISIBLE);
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    //No button clicked
                                                    //delete recorded voice file
                                                    File file = new File(strVoiceRecordFilePath);
                                                    if (file.exists())
                                                        file.delete();
                                                    dialog.dismiss();
                                                    isVoiceRecording = false;
                                                    edtMessage.setText(strEditText);
                                                    imgMicSel.setVisibility(View.INVISIBLE);
                                                    imgBtnEditChat.setVisibility(View.VISIBLE);
                                                    break;
                                            }
                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(ImBoardActivity.this);
                                    builder.setCancelable(false).setMessage("Do you want to send your voice message?").setPositiveButton("Yes", dialogClickListener)
                                            .setNegativeButton("No", dialogClickListener).show();
                                } else {
                                    Toast.makeText(ImBoardActivity.this, "Failed to send voice message", Toast.LENGTH_LONG).show();
                                    isVoiceRecording = false;
                                }
                            }
                        }
                        break;

                }
                return true;
            }
        });

        updateChatControllerButtons();
        this.receiver = new MsgReceiver();
        this.contactChangedReceiver = new ContactChangedReceiver();

        if(mSensor == null)
            mSensor = new SoundMeter();

        if (this.receiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.receiver, msgReceiverIntent);
            isReceiverRegistered = true;
        }

        if (this.contactChangedReceiver != null) {
            IntentFilter contactReceiverIntent = new IntentFilter();
            contactReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangedReceiver, contactReceiverIntent);
            isChangedContact = true;
        }
        //Add by wang
        enableOrDisableEditBTN();

        this.isUILoaded = true;
    }

    private void updateChatControllerButtons() {
        switch (inputMethodType) {
            case 0: /*ready to input status , voice record available , keyboard is not shown , emoticon is not shown*/
                emoticonTypeLayout.setVisibility(View.GONE);
                chatInputMethodLayout.setVisibility(View.VISIBLE);
                chatInputLayout.setVisibility(View.VISIBLE);
                trashlayout.setVisibility(View.GONE);

                imgBtnSendMessage.setVisibility(View.INVISIBLE);
                imgMicSel.setVisibility(View.INVISIBLE);
                imgBtnMic.setVisibility(View.VISIBLE);

                if (popupWindow.isShowing())
                    popupWindow.dismiss();
                hideKeyboard();

                btnClose.setVisibility(View.GONE);
                btnGinkoCall.setVisibility(View.VISIBLE);

                break;

            case 1:
                emoticonTypeLayout.setVisibility(View.VISIBLE);
                chatInputMethodLayout.setVisibility(View.GONE);
                chatInputLayout.setVisibility(View.VISIBLE);
                trashlayout.setVisibility(View.GONE);

                imgBtnSendMessage.setVisibility(View.VISIBLE);
                imgMicSel.setVisibility(View.INVISIBLE);
                imgBtnMic.setVisibility(View.INVISIBLE);

                break;

            case 2:
                emoticonTypeLayout.setVisibility(View.GONE);
                chatInputMethodLayout.setVisibility(View.GONE);
                chatInputLayout.setVisibility(View.GONE);
                if(!this.adapter.hasSelectedMessageCount())
                    btnDeleteChatHistory.setImageResource(R.drawable.btn_chat_message_delete_dis);
                else
                    btnDeleteChatHistory.setImageResource(R.drawable.btn_chat_message_delete);
                trashlayout.setVisibility(View.VISIBLE);

                if (popupWindow.isShowing())
                    popupWindow.dismiss();
                hideKeyboard();
                break;

            case 3: /*ready to input status , voice record available , keyboard is shown , emoticon is not shown*/
                emoticonTypeLayout.setVisibility(View.GONE);
                chatInputMethodLayout.setVisibility(View.VISIBLE);
                chatInputLayout.setVisibility(View.VISIBLE);
                trashlayout.setVisibility(View.GONE);

                imgBtnSendMessage.setVisibility(View.VISIBLE);
                imgMicSel.setVisibility(View.INVISIBLE);
                imgBtnMic.setVisibility(View.INVISIBLE);

                if (popupWindow.isShowing())
                    popupWindow.dismiss();
                break;
        }
    }

    public boolean getVoiceRecording() {return isVoiceRecording;}
    private void hideKeyboard() {
        //if keyboard is shown, then hide it
        if(isKeyBoardVisible) {
            MyApp.getInstance().hideKeyboard(rootLayout);

            //Add by wang
            btnEmoticonPuple1.setImageResource(R.drawable.emoji_puple1_normal);
            btnEmoticonPuple2.setImageResource(R.drawable.emoji_puple2_normal);
            btnEmoticonPuple3.setImageResource(R.drawable.emoji_puple3_normal);
            btnEmoticonPuple4.setImageResource(R.drawable.emoji_puple4_normal);
        }
    }

    private void showKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtMessage, InputMethodManager.SHOW_FORCED);
    }

    private void updateViewWithEmoticonType()
    {
        ViewPager pager = (ViewPager)this.popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(this.emoticonAdapters[this.nEmoticonType]);
        this.emoticonCirclePageIndicator.setViewPager(pager);
        this.emoticonCirclePageIndicator.notifyDataSetChanged();
    }
    //update the emoticon types
    private void updateEmoticonTypeButtons() {
        switch (nEmoticonType) {
            case 0:
                btnEmoticonPuple1.setImageResource(R.drawable.emoji_puple1_sel);
                btnEmoticonPuple2.setImageResource(R.drawable.emoji_puple2_normal);
                btnEmoticonPuple3.setImageResource(R.drawable.emoji_puple3_normal);
                btnEmoticonPuple4.setImageResource(R.drawable.emoji_puple4_normal);
                break;
            case 1:
                btnEmoticonPuple1.setImageResource(R.drawable.emoji_puple1_normal);
                btnEmoticonPuple2.setImageResource(R.drawable.emoji_puple2_sel);
                btnEmoticonPuple3.setImageResource(R.drawable.emoji_puple3_normal);
                btnEmoticonPuple4.setImageResource(R.drawable.emoji_puple4_normal);
                break;
            case 2:
                btnEmoticonPuple1.setImageResource(R.drawable.emoji_puple1_normal);
                btnEmoticonPuple2.setImageResource(R.drawable.emoji_puple2_normal);
                btnEmoticonPuple3.setImageResource(R.drawable.emoji_puple3_sel);
                btnEmoticonPuple4.setImageResource(R.drawable.emoji_puple4_normal);
                break;
            case 3:
                btnEmoticonPuple1.setImageResource(R.drawable.emoji_puple1_normal);
                btnEmoticonPuple2.setImageResource(R.drawable.emoji_puple2_normal);
                btnEmoticonPuple3.setImageResource(R.drawable.emoji_puple3_normal);
                btnEmoticonPuple4.setImageResource(R.drawable.emoji_puple4_sel);
                break;
        }
    }

    private void GotoVideoChat(int callType)
    {
        if (board == null)
            return;

        MyApp.getInstance().isOwnerForConfernece = true;
        MyApp.getInstance().initializeVideoVariables();

        ArrayList<EventUser> userData = new ArrayList<EventUser>();
        EventUser ownUser = new EventUser();
        ownUser.setFirstName(RuntimeContext.getUser().getFirstName());
        ownUser.setLastName(RuntimeContext.getUser().getLastName());
        ownUser.setPhotoUrl(RuntimeContext.getUser().getPhotoUrl());
        ownUser.setUserId(RuntimeContext.getUser().getUserId());
        userData.add(ownUser);

        VideoMemberVO currMember = new VideoMemberVO();
        currMember.setUserId(String.valueOf(ownUser.getUserId()));
        currMember.setName(ownUser.getFullName());
        currMember.setOwner(true);
        currMember.setMe(true);
        currMember.setWeight(0);
        currMember.setInitialized(true);
        if (callType == 1)
            currMember.setVideoStatus(true);
        else
            currMember.setVideoStatus(false);

        currMember.setVoiceStatus(true);
        MyApp.getInstance().g_currMemberCon = currMember;
        MyApp.getInstance().g_videoMemberList.add(currMember);
        MyApp.getInstance().g_videoMemIDs.add(currMember.getUserId());

        for (int i=0; i<board.getMembers().size(); i++)
        {
            VideoMemberVO memberOne = new VideoMemberVO();
            EventUser indexOne = board.getMembers().get(i).getUser();
            if (indexOne.getUserId() == RuntimeContext.getUser().getUserId())
                continue;
            if (board.getMembers().get(i).isChatOnly())
                continue;

            memberOne.setUserId(String.valueOf(indexOne.getUserId()));
            memberOne.setName(indexOne.getFullName());
            memberOne.setImageUrl(indexOne.getPhotoUrl());
            memberOne.setOwner(false);
            memberOne.setMe(false);
            memberOne.setWeight(i + 1);
            memberOne.setInitialized(true);
            memberOne.setYounger(true);

            if (callType == 1)
                memberOne.setVideoStatus(true);
            else
                memberOne.setVideoStatus(false);

            memberOne.setVoiceStatus(true);

            userData.add(indexOne);
            MyApp.getInstance().g_videoMemberList.add(memberOne);
            MyApp.getInstance().g_videoMemIDs.add(memberOne.getUserId());
        }

        String conferenceName = txtContactName.getText().toString();

        Intent groupVideoIntent = new Intent(ImBoardActivity.this, GroupVideoChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("boardId", boardId);
        bundle.putInt("callType", callType);
        bundle.putString("conferenceName", conferenceName);
        bundle.putSerializable("userData", userData);
        bundle.putBoolean("isInitial", true);
        groupVideoIntent.putExtras(bundle);
        startActivity(groupVideoIntent);
    }

    private void showBottomCallWindow(View v)
    {
        List<String> phones = new ArrayList<String>();
        phones.add(0, "Ginko Voice Call");
        phones.add(1, "Ginko Video Call");
        phones.add(2, "Cancel");

        final List<String> buttons = phones;
        final BottomPopupWindow popupWindow = new BottomPopupWindow(ImBoardActivity.this, buttons);
        popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
            @Override
            public void onClick(View button, int position) {
                String text = buttons.get(position);
                if (text == "Cancel") {
                    popupWindow.dismiss();
                } else if (text == "Ginko Video Call") {
                    GotoVideoChat(1);
                } else if (text == "Ginko Voice Call") {
                    GotoVideoChat(2);
                }
            }
        });
        popupWindow.show(v);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGinkoCall:
                if(isVoiceRecording)
                    return;
//                showBottomCallWindow(view);

                Intent groupVideoIntent = new Intent(ImBoardActivity.this, VideoChatAddUserActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("boardId", boardId);
                bundle.putBoolean("isImBoard", true);
                bundle.putBoolean("isReturnFromConference", false);
                bundle.putBoolean("isGroupFrom", true);
                bundle.putString("existContactIds", "");

                groupVideoIntent.putExtras(bundle);
                startActivity(groupVideoIntent);

                break;
            case R.id.btnPrev:
                //GAD-1682
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }

                if (this.receiver != null && isReceiverRegistered == true) {
                    unregisterReceiver(this.receiver);
                    isReceiverRegistered = false;
                }

                if (this.contactChangedReceiver != null && isChangedContact == true) {
                    unregisterReceiver(this.contactChangedReceiver);
                    isChangedContact = false;
                }
                hideKeyboard();
                if (!isPurpleClass)
                    finish();
                else
                {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isFavorite" , isFavorite);
                    ImBoardActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                break;

            //delete selected message
            case R.id.btnDeleteAllMessage:
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                AlertDialog.Builder deleteAllDialogBuilder = new AlertDialog.Builder(this);
                deleteAllDialogBuilder.setTitle("Confirm");
                deleteAllDialogBuilder.setCancelable(false);
                deleteAllDialogBuilder.setMessage(getResources().getString(R.string.str_delete_all_chat_messages));
                deleteAllDialogBuilder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        IMRequest.clearAllMessage(boardId, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    inputMethodType = 0;
                                    btnDeleteAllMessage.setVisibility(View.GONE);
                                    btnClose.setVisibility(View.GONE);
                                    btnPrev.setVisibility(View.VISIBLE);

                                    noMoreMessage = true;
                                    isFetchingNewMessage = false;

                                    synchronized (syncMessagesObj)
                                    {
                                        if(chatTableModel == null)
                                            chatTableModel = MyApp.getInstance().getChatDBModel();
                                        chatTableModel.deleteWholeBoardMessage(boardId);
                                        synchronized (syncMessagesObj) {
                                            earliestMsgSentTime = null;
                                            latestMsgSentTime = null;
                                        }
                                    }

                                    adapter.isSelectable(false);
                                    adapter.clearAllMessages();
                                    adapter.refreshMessageSelection();
                                    adapter.updateListView();
                                    //Add by wang
                                    enableOrDisableEditBTN();

                                    updateChatControllerButtons();
                                }
                                else
                                {
                                    MyApp.getInstance().showSimpleAlertDiloag(ImBoardActivity.this , "Failed to delete all messages." , null);
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                deleteAllDialogBuilder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog deleteAllMessageDialog = deleteAllDialogBuilder.create();
                deleteAllMessageDialog.show();
                break;
            case R.id.imgDeleteChatHistory:

                if (this.isDeletingMessage) return;
                if(!this.adapter.hasSelectedMessageCount()) return;

                this.isDeletingMessage = true;
                /*if (adapter != null) {
                    this.adapter.deleteSelectedMessages();
                }*/
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                if (adapter != null) {
                    AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
                    deleteDialogBuilder.setTitle("Confirm");
                    deleteDialogBuilder.setMessage("Do you want to delete selected messages?");
                    deleteDialogBuilder.setCancelable(false);
                    deleteDialogBuilder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            adapter.deleteSelectedMessages();
                            btnDeleteChatHistory.setImageResource(R.drawable.btn_chat_message_delete_dis);
                            dialog.dismiss();
                        }
                    });
                    deleteDialogBuilder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            isDeletingMessage = false;
                            dialog.dismiss();
                        }
                    });
                    AlertDialog deleteMessageDialog = deleteDialogBuilder.create();
                    deleteMessageDialog.show();
                }
                break;

            //close the edit message status
            case R.id.btnClose:
                inputMethodType = 0;
                btnDeleteAllMessage.setVisibility(View.GONE);
                btnGinkoCall.setVisibility(View.VISIBLE);
                btnClose.setVisibility(View.GONE);
                btnPrev.setVisibility(View.VISIBLE);
                adapter.isSelectable(false);
                adapter.refreshMessageSelection();
                adapter.updateListView();

                updateChatControllerButtons();
                break;

            //select emoticon type as puple 1
            case R.id.emoticonPuple1:
                nEmoticonType = 0;
                updateEmoticonTypeButtons();
                showEmoticonView();
                updateViewWithEmoticonType();
                break;

            case R.id.emoticonPuple2:
                nEmoticonType = 1;
                updateEmoticonTypeButtons();
                showEmoticonView();
                updateViewWithEmoticonType();
                break;

            case R.id.emoticonPuple3:
                nEmoticonType = 2;
                updateEmoticonTypeButtons();
                showEmoticonView();
                updateViewWithEmoticonType();
                break;

            case R.id.emoticonPuple4:
                nEmoticonType = 3;
                updateEmoticonTypeButtons();
                showEmoticonView();
                updateViewWithEmoticonType();
                break;

            //get location to send my location info
            case R.id.imgBtnLocation:
                if(isVoiceRecording)
                    return;
                if (lm == null)
                    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (isGettingCurrentLocation)
                    return;
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    if(latestLocation != null)
                    {
                        Intent addLocationIntent = new Intent(ImBoardActivity.this, ImAddLocationMessageActivity.class);
                        addLocationIntent.putExtra("lat", latestLocation.getLatitude());
                        addLocationIntent.putExtra("long", latestLocation.getLongitude());
                        startActivityForResult(addLocationIntent, ADD_LOCATION_MESSAGE);
                    }
                    else {
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_LOW);
                        criteria.setPowerRequirement(Criteria.POWER_LOW);
                        isGettingCurrentLocation = true;
                        if(locationLooper == null) {
                            locationLooper = Looper.myLooper();
                        }
                        if(progressDialog == null)
                            progressDialog = ProgressHUD.createProgressDialog(this , getResources().getString(R.string.str_get_current_location),
                                    true , true , new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            synchronized (lockObj) {
                                                isGettingCurrentLocation = false;
                                                lm.removeUpdates(locationListener);
                                                if (progressDialog != null && progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                                locationHandler.removeCallbacks(locationTimeoutRunnable);
                                            }
                                        }
                                    });

                        lm.requestSingleUpdate(criteria, locationListener, locationLooper);
                        if(locationHandler == null)
                            locationHandler = new Handler(locationLooper);
                        locationHandler.postDelayed(locationTimeoutRunnable, GET_CURRENT_LOCATION_TIME_OUT);

                        progressDialog.show();
                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ImBoardActivity.this);
                    builder.setMessage(getResources().getString(R.string.gps_network_not_enabled));
                    builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int paramInt) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                break;

            //get photo from camera or gallery to send photo
            case R.id.imgBtnPhoto:
                if(isVoiceRecording)
                    return;
                Intent photoIntent = new Intent(ImBoardActivity.this, ImAddPhotoVideoMessageActivity.class);
                photoIntent.putExtra("isPhotoIntent", true);
                startActivityForResult(photoIntent, ADD_PHOTO_MESSAGE);
                break;

            //get video from camera or gallery to send video
            case R.id.imgBtnVideo:
                if(isVoiceRecording)
                    return;
                deleteAllTempFiles();
                //Toast.makeText(ImBoardActivity.this, "Coming soon....", Toast.LENGTH_LONG).show();
                Intent videoIntent = new Intent(ImBoardActivity.this, ImAddPhotoVideoMessageActivity.class);
                videoIntent.putExtra("isPhotoIntent", false);
                startActivityForResult(videoIntent, ADD_VIDEO_MESSAGE);
                break;

            //get emoticons
            case R.id.imgBtnEmoticon:
                if(isVoiceRecording)
                    return;
                inputMethodType = 1;
                updateChatControllerButtons();
                showEmoticonView();
                updateEmoticonTypeButtons();
                break;

            //edit chat message
            case R.id.imgBtnEditChat:
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                if (inputMethodType == 2) {
                    inputMethodType = 0;
                    btnDeleteAllMessage.setVisibility(View.GONE);
                    btnClose.setVisibility(View.GONE);
                    btnPrev.setVisibility(View.VISIBLE);
                    btnGinkoCall.setVisibility(View.VISIBLE);
                } else {
                    /*inputMethodType = 2;
                    btnDeleteAllMessage.setVisibility(View.VISIBLE);
                    btnClose.setVisibility(View.VISIBLE);
                    btnPrev.setVisibility(View.GONE);
                    adapter.isSelectable(true);
                    adapter.updateListView();*/
                    if(pullToRefreshView.getCount() > 1)
                    {
                        inputMethodType = 2;
                        btnDeleteAllMessage.setVisibility(View.VISIBLE);
                        btnClose.setVisibility(View.VISIBLE);
                        btnGinkoCall.setVisibility(View.GONE);
                        btnPrev.setVisibility(View.GONE);
                        adapter.isSelectable(true);
                        adapter.updateListView();
                    }
                    else{
                        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(this);
                        editDialogBuilder.setTitle("Confirm");
                        editDialogBuilder.setMessage("There is no chat history.");
                        editDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                dialog.dismiss();
                            }
                        });
                        AlertDialog editMessageDialog = editDialogBuilder.create();
                        editMessageDialog.show();
                    }
                }

                updateChatControllerButtons();

                break;

            //send chat message
            case R.id.imgBtnSendMessage:
                if (this.edtMessage.getText().toString().trim().compareTo("") != 0)
                    sendMessage();
                else
                {
                    int resourceId = R.drawable.btnchat_sendmessage;
                    Integer integer = (Integer)imgBtnSendMessage.getTag();
                    integer = integer == null ? 0 : integer;
                    if (integer == resourceId){
                        //Uitils.alert("Can't sent the empty message!");
                        this.edtMessage.setText("");
                        String strAlert = "Can't send the empty message!";
                        inputMethodType = 0;
                        hideKeyboard();
                        updateChatControllerButtons();

                        DialogInterface.OnClickListener newListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                edtMessage.setFocusableInTouchMode(true);
                                edtMessage.requestFocus();
                                showKeyboard();
                                inputMethodType = 3;
                                updateChatControllerButtons();
                            }
                        };

                        MyApp.getInstance().showSimpleAlertDiloag(ImBoardActivity.this, strAlert, newListener);
                    }
                }
                break;


        }
    }
    /*
    private void initValues() {
        groupName = null;
        board = null;
        chatTableModel = null;
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        int new_boardId = intent.getIntExtra("board_id", 0);
        MyApp.getInstance().setCurrentBoardId(new_boardId);

        Intent newIntent =  new Intent(this, ImBoardActivity.class);
        newIntent.putExtra("board_id", new_boardId);
        startActivity(newIntent);
        //this.finish();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(boardId != 0 )
            MyApp.getInstance().setCurrentBoardId(boardId);
        if(chatTableModel == null)
            chatTableModel = MyApp.getInstance().getChatDBModel();

        if (this.receiver != null && isReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.receiver, msgReceiverIntent);
            isReceiverRegistered = true;
        }

        if (this.contactChangedReceiver != null && isChangedContact == false) {
            IntentFilter contactReceiverIntent = new IntentFilter();
            contactReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangedReceiver, contactReceiverIntent);
            isChangedContact = true;
        }
        if (this.adapter != null) {
            this.adapter.registerDownloadManager();
            adapter.updateListView();
        }

        //update the controller UI buttons
        if (this.isUILoaded)
            updateChatControllerButtons();


        //Add by wang
        if(this.inputMethodType == 2)
            return;
        if(this.inputMethodType != 0)
        {
            this.inputMethodType = 0;
            updateChatControllerButtons();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }

        /*if (this.receiver != null && isReceiverRegistered == true) {
            //unregisterReceiver(this.receiver);
            //isReceiverRegistered = false;
        }

        if (this.contactChangedReceiver != null && isChangedContact == true) {
            unregisterReceiver(this.contactChangedReceiver);
            isChangedContact = false;
        }
        */

        /*if (this.receiver != null && isReceiverRegistered == true) {
            unregisterReceiver(this.receiver);
            isReceiverRegistered = false;
        }*/
        hideKeyboard();

        long diff = System.currentTimeMillis() - voiceRecordStartTime;
        if (imgBtnGetLocation != null)
            imgBtnGetLocation.setAlpha(1.0f);
        if (imgBtnGetPhoto != null)
            imgBtnGetPhoto.setAlpha(1.0f);
        if (imgBtnGetVideo != null)
            imgBtnGetVideo.setAlpha(1.0f);
        if (imgBtnEmoticon != null)
            imgBtnEmoticon.setAlpha(1.0f);

        if (isVoiceRecording && diff < 1000) {
            isVoiceRecording = false;
            if (adapter != null)
                adapter.setVoiceRecord(isVoiceRecording);
            stopVoiceRecording();
            edtMessage.setText("");
            imgMicSel.setVisibility(View.INVISIBLE);
            imgBtnEditChat.setVisibility(View.VISIBLE);
            File file = new File(strVoiceRecordFilePath);
            if (file.exists())
                file.delete();
            return;
        }

        if (isVoiceRecording) {
            stopVoiceRecording();
            if (adapter != null)
                adapter.setVoiceRecord(false);

            //send recorded voice message

            File file = new File(strVoiceRecordFilePath);
            if (file.exists()) {
                //confirm dialog to send voice message or not
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                //send recorded voice message
                                Properties param = new Properties();
                                param.setProperty("voice_length", String.valueOf(ImBoardActivity.this.recordedVoiceTime) + "");
                                sendMediaMessage(ImMessageVO.MSG_VOICE, ImBoardActivity.this.strVoiceRecordFilePath, param);
                                isVoiceRecording = false;
                                edtMessage.setText("");
                                imgMicSel.setVisibility(View.INVISIBLE);
                                imgBtnEditChat.setVisibility(View.VISIBLE);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                //delete recorded voice file
                                File file = new File(strVoiceRecordFilePath);
                                if (file.exists())
                                    file.delete();
                                dialog.dismiss();
                                isVoiceRecording = false;
                                edtMessage.setText("");
                                imgMicSel.setVisibility(View.INVISIBLE);
                                imgBtnEditChat.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ImBoardActivity.this);
                builder.setCancelable(false).setMessage("Do you want to send your voice message?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else {
                Toast.makeText(ImBoardActivity.this, "Failed to send voice message", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public  void onBackPressed() {
        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }

        if (this.receiver != null && isReceiverRegistered == true) {
            unregisterReceiver(this.receiver);
            isReceiverRegistered = false;
        }

        if (this.contactChangedReceiver != null && isChangedContact == true) {
            unregisterReceiver(this.contactChangedReceiver);
            isChangedContact = false;
        }
        /*if (this.receiver != null && isReceiverRegistered == true) {
            unregisterReceiver(this.receiver);
            isReceiverRegistered = false;
        }*/
        hideKeyboard();

        if (!isPurpleClass)
            //GAD-1682
            super.onBackPressed();
        else
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("isFavorite" , isFavorite);
            ImBoardActivity.this.setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MyApp.getInstance().setCurrentBoardId(0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.debug("Received the image");
        if(chatTableModel == null)
            chatTableModel = MyApp.getInstance().getChatDBModel();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PURPLE_ACTIVITY_START:
                    if(data != null && data.getBooleanExtra("isContactDeleted" , false)) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isContactDeleted" , true);
                        ImBoardActivity.this.setResult(Activity.RESULT_OK , returnIntent);
                        finish();
                    }
                    break;
                case ADD_LOCATION_MESSAGE:
                    edtMessage.setText("");
                    double lat = data.getDoubleExtra("lat" , 0.0d);
                    double lng = data.getDoubleExtra("long" , 0.0d);
                    sendLocationMessage(lat , lng);
                    break;

                case ADD_PHOTO_MESSAGE:
                    String strPhotoPath = data.getStringExtra("photoPath");
                    if(strPhotoPath.contains("file://"))
                        strPhotoPath = strPhotoPath.replaceAll(" " , "%20");
                    sendMediaMessage(ImMessageVO.MSG_PHOTO , strPhotoPath , null);
                    break;

                case ADD_VIDEO_MESSAGE:
                    String strVideoPath = data.getStringExtra("videoPath");
                    if(strVideoPath.contains("file://"))
                        strVideoPath = strVideoPath.replaceAll(" " , "%20");
                    String strThumbPath = data.getStringExtra("thumbPath");
                    if(strThumbPath.contains("file://"))
                        strThumbPath = strThumbPath.replaceAll(" " , "%20");
                    sendVideoMessage(strThumbPath , strVideoPath , null);
                    break;
                case INT_EXTRA_PURPLE:
                    if(data != null)
                        isFavorite = data.getBooleanExtra("isFavorite", false);
                    break;

            }
        }
    }

    /*
    Add by wang
     */
    private void enableOrDisableEditBTN(){
        //Integer number = 40;
        Integer number = 10;
        List<ImMessageVO> messages = chatTableModel.getLatestChatsByTime(boardId , null , false , number);

        if(messages != null && messages.size() > 0 && boardId != 0) {
            imgBtnEditChat.setImageResource(R.drawable.btnchatedit);
            imgBtnEditChat.setEnabled(true);
        }
        else {
            if (boardId != 0 && adapter.getCount() > 0) {
                imgBtnEditChat.setImageResource(R.drawable.btnchatedit);
                imgBtnEditChat.setEnabled(true);
            } else
            {
                imgBtnEditChat.setImageResource(R.drawable.btnchatedit_disable);
                imgBtnEditChat.setEnabled(false);
            }
        }
    }
    /**
     * Defining all components of emoticons keyboard
     */
    private void enablePopUpView() {

        ViewPager pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(this.emoticonAdapters[this.nEmoticonType]);

        emoticonCirclePageIndicator.setViewPager(pager);
        // Creating a pop window for emoticons keyboard
        popupWindow = new PopupWindow(popUpView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) keyboardHeight, false);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            }
        });
    }

    /* show emoticons */
    private void showEmoticonView() {
        if (!popupWindow.isShowing()) {
            //popupWindow.setHeight((int) (keyboardHeight));
            if (isKeyBoardVisible) {
                emoticonsCover.setVisibility(LinearLayout.GONE);
                //hideKeyboard();
            } else {
                emoticonsCover.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.setHeight(keyboardHeight);
            popupWindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);

        }
    }

    /* keyboard related functions */
    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;

    private void checkKeyboardHeight(final View parentLayout) {

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);

                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }

                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {
                            if (isKeyBoardVisible == false) {
                                changeKeyboardHeight(heightDifference);
                                //inputMethodType = 1;
                                inputMethodType = 3;
                                updateChatControllerButtons();
                                if(popupWindow.isShowing())
                                    popupWindow.dismiss();

                                isKeyBoardVisible = true;
                                edtMessage.setCursorVisible(true);
                            }

                        } else {
                            isKeyBoardVisible = false;
                            //inputMethodType = 0;
                            //updateChatControllerButtons();
                            edtMessage.setCursorVisible(false);
                        }

                    }
                });

    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height minimum height by which we can make sure actual keyboard is
     *               open or not
     */
    private void changeKeyboardHeight(int height) {

        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }
    }

    /**
     * Overriding onKeyDown for dismissing keyboard on key down
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (popupWindow != null && popupWindow.isShowing() && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            popupWindow.dismiss();
            inputMethodType = 0;
            updateChatControllerButtons();
            return false;
        }
        else if (inputMethodType == 2 && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            inputMethodType = 0;
            updateChatControllerButtons();
            btnDeleteAllMessage.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);
            btnPrev.setVisibility(View.VISIBLE);

            adapter.refreshMessageSelection();
            adapter.isSelectable(false);
            adapter.updateListView();
            return false;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public void EmoticonKeyClickedIndex(int index) {
        if(index == -1)
        {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            edtMessage.dispatchKeyEvent(event);
            return;
        }
        String strEmoticonCode = this.emoticons.getEmoticonCode(index);
        BitmapDrawable emoticonDrawable = new BitmapDrawable(getResources() , this.emoticons.getEmoticon(index));
        emoticonDrawable.setBounds(0 , 0 , this.emoticonHeight * (emoticonDrawable.getIntrinsicHeight()/emoticonDrawable.getIntrinsicWidth()) , this.emoticonHeight);
        ImageSpan imgSpan = new ImageSpan(emoticonDrawable , 0);
        int selectionStart = this.edtMessage.getSelectionStart();
        int selectionEnd = this.edtMessage.getSelectionEnd();
        this.edtMessage.getEditableText().replace(selectionStart , selectionEnd , strEmoticonCode);
        this.edtMessage.getEditableText().setSpan(imgSpan, selectionStart, selectionStart + strEmoticonCode.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void onBoardListItemSelected(int selectedItemPosition, boolean selectedItemsCount) {
        if(selectedItemsCount)
            btnDeleteChatHistory.setImageResource(R.drawable.btn_chat_message_delete);
        else
            btnDeleteChatHistory.setImageResource(R.drawable.btn_chat_message_delete_dis);
    }

    @Override
    public void onDeleteMessage(boolean isDeleted, int deletedMessageCount) {
        this.isDeletingMessage = false;
        if (isDeleted == true)//if delete is success
        {
            //add by wang
            enableOrDisableEditBTN();
            if(pullToRefreshView.getCount() > 1)
            {

            }else {
                adapter.refreshMessageSelection();
                adapter.isSelectable(false);
                adapter.updateListView();

                inputMethodType = 0;

                updateChatControllerButtons();
                btnDeleteAllMessage.setVisibility(View.GONE);
                btnClose.setVisibility(View.GONE);
                btnPrev.setVisibility(View.VISIBLE);
            }

            synchronized (syncMessagesObj) {
                try {
                    earliestMsgSentTime = getFirstMessageTime(adapter.getListItems());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                try {
                    latestMsgSentTime = getLastMessageTime(adapter.getListItems());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    //when single touch the pulltorefreshview , then hide the keyboard
    @Override
    public void onSinglePointTouchEvent() {
        if(this.inputMethodType == 2)
            return;
        if(this.inputMethodType != 0)
        {
            this.inputMethodType = 0;
            updateChatControllerButtons();
        }
    }

    //when press back key on edt message
    @Override
    public void onImEditTextBackKeyDown() {
        if(this.popupWindow.isShowing())
        {
            this.popupWindow.dismiss();
            if(!isKeyBoardVisible) {
                this.inputMethodType = 0;
                updateChatControllerButtons();
            }
            return;
        }
        if(!this.isKeyBoardVisible)
        {
            if (!isPurpleClass)
                finish();
            else
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("isFavorite" , isFavorite);
                ImBoardActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            return;
        }
        this.inputMethodType = 0;
        updateChatControllerButtons();
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0) //share photo or video file via email
        {
            if(sharingMessage != null)
            {
                if(sharingMessage.getFile() != null && sharingMessage.getFile().equals(""))
                {
                    Toast.makeText(ImBoardActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                    return;
                }
                if(sharingMessage.getFile() != null)
                {
                    File file = new File(sharingMessage.getFile());
                    if(file.exists())
                    {
                        try {

                            final Intent emailIntent = new Intent(
                                    android.content.Intent.ACTION_SEND);

                            emailIntent.setType("plain/text");
                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                                    new String[] { "" });//email
                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                    "");//subject

                            Uri attachFileUri = Uri.fromFile(file);

                            if (attachFileUri != null) {
                                emailIntent.putExtra(Intent.EXTRA_STREAM, attachFileUri);
                            }

                            emailIntent
                                    .putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Ginko Android...");
                            this.startActivity(Intent.createChooser(emailIntent,
                                    "Send email via..."));
                        } catch (Throwable t) {
                            Toast.makeText(this,
                                    "Request failed try again: " + t.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ImBoardActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                else
                {
                    Toast.makeText(ImBoardActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    public void startActivityForResult() {

    }


    public class EmoticonsPagerAdapter extends PagerAdapter {

        private static final int NO_OF_EMOTICONS_PER_PAGE = 20;
        private Context mContext;
        private EmoticonsGridAdapter.EmoticonKeyClickListener mListener;
        private ArrayList<Integer> emoticonList;
        private LayoutInflater inflater;

        public EmoticonsPagerAdapter(Context context,
                                     ArrayList<Integer> _emoticonList, EmoticonsGridAdapter.EmoticonKeyClickListener listener) {
            this.emoticonList = _emoticonList;
            this.mContext = context;
            this.mListener = listener;
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return (int) Math.ceil((double) emoticonList.size()
                    / (double) NO_OF_EMOTICONS_PER_PAGE);
        }

        @Override
        public Object instantiateItem(View collection, int position) {

            View layout = inflater.inflate(
                    R.layout.emoticons_grid, null);

            int initialPosition = position * NO_OF_EMOTICONS_PER_PAGE;
            ArrayList<Integer> emoticonsInAPage = new ArrayList<Integer>();

            for (int i = initialPosition; i < initialPosition
                    + NO_OF_EMOTICONS_PER_PAGE
                    && i < emoticonList.size(); i++) {
                emoticonsInAPage.add(emoticonList.get(i));
            }

            GridView grid = (GridView) layout.findViewById(R.id.emoticons_grid);
            EmoticonsGridAdapter adapter = new EmoticonsGridAdapter(
                    mContext, emoticons , emoticonsInAPage, position, mListener);
            grid.setAdapter(adapter);

            ((ViewPager) collection).addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    /* start voice recording */
    private void startVoiceRecord(String name) {
        mSensor.start(name);
        imgMicSel.setVisibility(View.VISIBLE);
        imgBtnEditChat.setVisibility(View.INVISIBLE);
        edtMessage.setText("00:00 Slide to Cancel <");

        imgBtnGetLocation.setImageResource(R.drawable.btnchat_location);
        mHandler.postDelayed(mRecordRefreshTask, REFRESH_INTERVAL);
    }

    /* stop voice recording*/
    private void stopVoiceRecording() {
        mHandler.removeCallbacks(mRecordRefreshTask);
        mSensor.stop();
        //edtMessage.setText("");
        this.recordedVoiceTime = ((int)((System.currentTimeMillis() - this.voiceRecordStartTime) / 1000L));
        //imgMicSel.setVisibility(View.INVISIBLE);
        //imgBtnEditChat.setVisibility(View.VISIBLE);
    }

    private Runnable mRecordRefreshTask = new Runnable() {
        public void run() {
            //double amp = mSensor.getAmplitude();
            long duration = (System.currentTimeMillis() - voiceRecordStartTime) / 1000;
            int min = (int) duration / 60;
            int seconds = (int) duration % 60;
            String strTimeFormat = String.format("%02d:%02d Slide to Cancel <", min, seconds);
            edtMessage.setText(strTimeFormat);
            mHandler.postDelayed(mRecordRefreshTask, REFRESH_INTERVAL);

        }
    };

    private void sendMessage() {
        final String strMsg = this.edtMessage.getText().toString();
        this.edtMessage.setText("");

        ImMessageVO sendMsg = new ImMessageVO();
        sendMsg.setMsgId(0);
        sendMsg.setMsgType(1);//text message
        sendMsg.setMessageType(ImMessageVO.MSG_TYPE_TEXT);
        sendMsg.setSendTime(Calendar.getInstance().getTime());
        sendMsg.setFrom(RuntimeContext.getUser().getUserId());
        sendMsg.setContent(strMsg);

        final ImMessageVO newMsg = sendMsg;
        IMRequest.sendMessage(this.boardId, strMsg, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                //sample respone
                //{"board_id":695,"msg_id":6151,"send_time":"2015-11-17 11:55:48"}

                if (response.isSuccess()) {
                    edtMessage.setText("");
                    try
                    {
                        JSONObject jsonObject = response.getData();

                        if(simpleDateFormat == null)
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        String strUtcTime = jsonObject.getString("send_time");
                        newMsg.utcSendTime = simpleDateFormat.parse(strUtcTime);
                        newMsg.setMsgId(jsonObject.optLong("msg_id"));
                        newMsg.setSendTime(MyDataUtils.convertUTCTimeToLocalTime(strUtcTime));

                        synchronized (syncMessagesObj)
                        {
                            if(earliestMsgSentTime == null || latestMsgSentTime == null) {
                                if (earliestMsgSentTime == null)
                                    earliestMsgSentTime = newMsg.utcSendTime;
                                if (latestMsgSentTime == null)
                                    latestMsgSentTime = newMsg.utcSendTime;
                            }
                            else
                            {
                                if(latestMsgSentTime.getTime() < newMsg.utcSendTime.getTime())
                                    latestMsgSentTime = newMsg.utcSendTime;
                            }
                        }

                        if(chatTableModel == null)
                            chatTableModel = MyApp.getInstance().getChatDBModel();
                        MessageDbConstruct dbStruct = new MessageDbConstruct(boardId , newMsg , strUtcTime);
                        chatTableModel.add(dbStruct);

                        showMsgInBoard(newMsg);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendLocationMessage(double lattitude, double longitude) {
        final String str = ConstValues.IM_LOCATION_PREFIX + String.valueOf(lattitude) + "," + String.valueOf(longitude);

        ImMessageVO sendMsg = new ImMessageVO();
        sendMsg.setMsgId(0);
        sendMsg.setMsgType(1);//text message
        sendMsg.setMessageType(ImMessageVO.MSG_TYPE_LOCATION);
        sendMsg.setSendTime(Calendar.getInstance().getTime());
        sendMsg.setFrom(RuntimeContext.getUser().getUserId());
        sendMsg.setContent(str);

        final ImMessageVO  newMsg = sendMsg;

        IMRequest.sendMessage(this.boardId, str, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    try {
                        JSONObject jsonObject = response.getData();

                        if (simpleDateFormat == null)
                            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        String strUtcTime = jsonObject.getString("send_time");
                        newMsg.utcSendTime = simpleDateFormat.parse(strUtcTime);
                        newMsg.setMsgId(jsonObject.optLong("msg_id"));
                        newMsg.setSendTime(MyDataUtils.convertUTCTimeToLocalTime(strUtcTime));

                        synchronized (syncMessagesObj) {
                            if (earliestMsgSentTime == null || latestMsgSentTime == null) {
                                if (earliestMsgSentTime == null)
                                    earliestMsgSentTime = newMsg.utcSendTime;
                                if (latestMsgSentTime == null)
                                    latestMsgSentTime = newMsg.utcSendTime;
                            } else {
                                if (latestMsgSentTime.getTime() < newMsg.utcSendTime.getTime())
                                    latestMsgSentTime = newMsg.utcSendTime;
                            }
                        }

                        if (chatTableModel == null)
                            chatTableModel = MyApp.getInstance().getChatDBModel();
                        MessageDbConstruct dbStruct = new MessageDbConstruct(boardId, newMsg, strUtcTime);
                        chatTableModel.add(dbStruct);

                        showMsgInBoard(newMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendMediaMessage(final String mediaType , final String filePath , Properties paramProperties)
    {
        final File file = new File(filePath);
        if(!file.exists())
            return;
        final ImMessageVO mediaMsg = new ImMessageVO();
        mediaMsg.setMsgId(0);
        mediaMsg.isPending = true;
        mediaMsg.setMsgType(2);//media message
        if(mediaType.equals(ImMessageVO.MSG_VOICE))
            mediaMsg.setMessageType(ImMessageVO.MSG_TYPE_VOICE);
        else if(mediaType.equals(ImMessageVO.MSG_PHOTO))
            mediaMsg.setMessageType(ImMessageVO.MSG_TYPE_PHOTO);
        mediaMsg.setFile(file.getAbsolutePath());
        mediaMsg.setSendTime(Calendar.getInstance().getTime());
        mediaMsg.setFrom(RuntimeContext.getUser().getUserId());

        //showMsgInBoard(mediaMsg);

        final ImMessageVO sentMsg = mediaMsg;
        IMRequest.sendFile(this.boardId, mediaType, null, file, paramProperties,
                new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            //sample respone
                            //{"msg_id":6152,"file_name":"tmp.png","file_type":"photo","url":"http:\/\/image.ginko.mobi\/im_upload\/tmp.png","send_time":"2015-11-17 11:56:43","status":"Send file successfully."}
                            //voice:{"file_name" = "2015-11-17-08-13-031493.aac";"file_type" = voice;"msg_id" = 6154;"send_time" = "2015-11-17 12:13:09";status = "Send file successfully.";url = "http://image.ginko.mobi/im_upload/2015-11-17-08-13-031493.aac";};
                            if (simpleDateFormat == null)
                                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                JSONObject jsonObject = response.getData();

                                if (simpleDateFormat == null)
                                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                String strUtcTime = jsonObject.getString("send_time");

                                sentMsg.isPending = false;
                                JSONObject contentObject = new JSONObject();
                                String strUrl = jsonObject.optString("url");
                                if (sentMsg.getMessageType() == ImMessageVO.MSG_TYPE_PHOTO)//photo
                                {
                                    contentObject.put("file_type", "photo");
                                    contentObject.put("url", strUrl);
                                } else//voice
                                {
                                    contentObject.put("file_type", "voice");
                                    contentObject.put("url", strUrl);
                                    contentObject.put("voice_length", "1");//temporary setting
                                }
                                sentMsg.setContent(contentObject.toString());
                                sentMsg.setMsgId(jsonObject.optLong("msg_id"));
                                sentMsg.setFileUrl(strUrl);
                                sentMsg.setSendTime(MyDataUtils.convertUTCTimeToLocalTime(strUtcTime));
                                sentMsg.utcSendTime = simpleDateFormat.parse(strUtcTime);

                                if (chatTableModel == null)
                                    chatTableModel = MyApp.getInstance().getChatDBModel();

                                synchronized (syncMessagesObj) {
                                    if (earliestMsgSentTime == null || latestMsgSentTime == null) {
                                        if (earliestMsgSentTime == null)
                                            earliestMsgSentTime = sentMsg.utcSendTime;
                                        if (latestMsgSentTime == null)
                                            latestMsgSentTime = sentMsg.utcSendTime;
                                    } else {
                                        if (latestMsgSentTime.getTime() < sentMsg.utcSendTime.getTime())
                                            latestMsgSentTime = sentMsg.utcSendTime;
                                    }
                                }

                                MessageDbConstruct dbStruct = new MessageDbConstruct(boardId, sentMsg, strUtcTime);
                                dbStruct.mediaFilePath = sentMsg.getFile() == null ? "" : sentMsg.getFile();
                                chatTableModel.add(dbStruct);

                                showMsgInBoard(mediaMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            adapter.addNewMessageIDToMessageIDList(sentMsg.getMsgId());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                , false);
    }
    private synchronized void sendVideoMessage(final String thumbFilePath , final String videoFilePath , Properties paramProperties)
    {
        final File videoFile = new File(videoFilePath);
        if(!videoFile.exists())
            return;
        final File thuumbFile = new File(thumbFilePath);
        if(!thuumbFile.exists())
            return;

        final ImMessageVO mediaMsg = new ImMessageVO();
        mediaMsg.isPending = true;
        mediaMsg.setMsgId(0);
        mediaMsg.setMsgType(2);//media message
        mediaMsg.setMessageType(ImMessageVO.MSG_TYPE_VIDEO);
        mediaMsg.setFile(videoFilePath);
        mediaMsg.setThumnail(thumbFilePath);
        mediaMsg.setSendTime(Calendar.getInstance().getTime());
        mediaMsg.setFrom(RuntimeContext.getUser().getUserId());

        showMsgInBoard(mediaMsg);

        final ImMessageVO sentMsg = mediaMsg;

        IMRequest.sendFile(this.boardId, ImMessageVO.MSG_VIDEO, thuumbFile, videoFile, paramProperties,
                new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            //sample response
                            //video:{"file_name" = "2015-11-17-08-11-401493.mp4";"file_type" = video;"msg_id" = 6153;"send_time" = "2015-11-17 12:11:49";status = "Send file successfully.";"thumnail_url" = "http://image.ginko.mobi/im_upload/1447762309040T013WR3215.jpg";url = "http://image.ginko.mobi/im_upload/2015-11-17-08-11-401493.mp4";};
                            if (simpleDateFormat == null)
                                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                JSONObject jsonObject = response.getData();

                                if (simpleDateFormat == null)
                                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                String strUtcTime = jsonObject.getString("send_time");

                                sentMsg.isPending = false;
                                JSONObject contentObject = new JSONObject();
                                String strUrl = jsonObject.optString("url");
                                String strThumbUrl = jsonObject.optString("thumnail_url");
                                contentObject.put("file_type", "video");
                                contentObject.put("url", strUrl);
                                contentObject.put("thumnail_url", strThumbUrl);
                                sentMsg.setContent(contentObject.toString());
                                sentMsg.setMsgId(jsonObject.optLong("msg_id"));
                                sentMsg.setFileUrl(strUrl);
                                sentMsg.setThumnail(strThumbUrl);
                                sentMsg.setSendTime(MyDataUtils.convertUTCTimeToLocalTime(strUtcTime));
                                sentMsg.utcSendTime = simpleDateFormat.parse(strUtcTime);

                                if (chatTableModel == null)
                                    chatTableModel = MyApp.getInstance().getChatDBModel();

                                synchronized (syncMessagesObj) {
                                    if (earliestMsgSentTime == null || latestMsgSentTime == null) {
                                        if (earliestMsgSentTime == null)
                                            earliestMsgSentTime = sentMsg.utcSendTime;
                                        if (latestMsgSentTime == null)
                                            latestMsgSentTime = sentMsg.utcSendTime;
                                    } else {
                                        if (latestMsgSentTime.getTime() < sentMsg.utcSendTime.getTime())
                                            latestMsgSentTime = sentMsg.utcSendTime;
                                    }
                                }

                                MessageDbConstruct dbStruct = new MessageDbConstruct(boardId, sentMsg, strUtcTime);
                                dbStruct.mediaFilePath = sentMsg.getFile() == null ? "" : sentMsg.getFile();
                                chatTableModel.add(dbStruct);

                                //showMsgInBoard(mediaMsg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            adapter.addNewMessageIDToMessageIDList(sentMsg.getMsgId());
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                , false);
    }
    private void showMsgInBoard(ImMessageVO msg) {
        this.adapter.addMessageItem(msg);
        this.adapter.notifyDataSetChanged();

        scrollToLastPosition(300);
        /*if (pullToRefreshView.getCount() > 0) {
            this.pullToRefreshView.setSelection(this.pullToRefreshView.getCount() - 1);
            this.pullToRefreshView.smoothScrollToPosition(this.pullToRefreshView.getCount() - 1);
        }*/
        //add by wang
        enableOrDisableEditBTN();
    }

    private synchronized void callReadMessage()
    {
        List<Long> msgIds = adapter.getUnreadMessageList();
        String strMsgIds = "";
        if(msgIds.size() > 0) {
            int len = msgIds.size();
            for (int i = 0; i < len; i++) {
                strMsgIds += msgIds.get(i);
                if (i < len - 1) {
                    strMsgIds += ",";
                }
            }

            int tempId = 0;
            if (boardId == 0)
                tempId = prevBoardId;
            else
                tempId = boardId;

            IMRequest.setReadStatus(tempId, strMsgIds, true, new ResponseCallBack<Void>() {
                @Override
                public void onCompleted(JsonResponse<Void> response) {
                    if (response.isSuccess())
                    {

                    }
                }
            });
        }
    }

    private List<ImMessageVO> storeMessages(List<JSONObject> jsonObjects)
    {
        List<ImMessageVO> messages = new ArrayList<ImMessageVO>();
        if(simpleDateFormat == null)
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for(int i=0;i<jsonObjects.size();i++)
        {
            try {
                JSONObject jsonObject = jsonObjects.get(i);
                ImMessageVO msg = JsonConverter.json2Object(jsonObject, (Class<ImMessageVO>) ImMessageVO.class);
                String strUtcTime = jsonObject.getString("send_time");
                msg.utcSendTime = simpleDateFormat.parse(strUtcTime);
                if(chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();
                MessageDbConstruct dbStruct = new MessageDbConstruct();
                dbStruct.strMsgTime = strUtcTime;
                dbStruct.msgContent = jsonObject.toString();
                dbStruct.boardId = boardId;
                dbStruct.msgId = msg.getMsgId();
                dbStruct.mediaFilePath = "";
                chatTableModel.add(dbStruct);
                messages.add(msg);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if(messages != null && messages.size()>0)
        {
            Date date1 = getFirstMessageTime(messages);
            Date date2 = getLastMessageTime(messages);
            synchronized (syncMessagesObj) {

                if(earliestMsgSentTime == null && date1 != null) {
                    earliestMsgSentTime = date1;
                }
                else if (date1 != null && earliestMsgSentTime != null && earliestMsgSentTime.getTime() > date1.getTime()) {
                    earliestMsgSentTime = date1;
                }
                else if(earliestMsgSentTime != null && date1 != null && earliestMsgSentTime.getTime() == date1.getTime())
                {
                    isNoMorePrevMessage = true;
                }

                if(latestMsgSentTime == null && date2 != null) {
                    latestMsgSentTime = date2;
                }
                else if (date2 != null && latestMsgSentTime != null && latestMsgSentTime.getTime() < date2.getTime()) {
                    latestMsgSentTime = date2;
                }
                else if(latestMsgSentTime != null && date2 != null && latestMsgSentTime.getTime() == date2.getTime())
                {
                    noMoreMessage = true;
                }
            }
        }
        //add by wang
        enableOrDisableEditBTN();

        return messages;
    }

    private void storeListMessages(List<ImMessageVO> messageItems)
    {
        if(simpleDateFormat == null)
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for(int i=0;i<messageItems.size();i++)
        {
            ImMessageVO msg = messageItems.get(i);
            String strSendTime = MyDataUtils.format(msg.getSendTime());

            try {
                msg.utcSendTime = simpleDateFormat.parse(strSendTime);
                synchronized (syncMessagesObj)
                {
                    if(earliestMsgSentTime == null || latestMsgSentTime == null) {
                        if (earliestMsgSentTime == null)
                            earliestMsgSentTime = msg.utcSendTime;
                        if (latestMsgSentTime == null)
                            latestMsgSentTime = msg.utcSendTime;
                    }
                    else
                    {
                        if(latestMsgSentTime.getTime() < msg.utcSendTime.getTime())
                            latestMsgSentTime = msg.utcSendTime;
                    }
                }

                if(chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();

                MessageDbConstruct dbStruct = new MessageDbConstruct(boardId , msg , strSendTime);
                chatTableModel.add(dbStruct);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        //add by wang
        enableOrDisableEditBTN();
    }

    private void scrollToLastPosition(int delayTimeMills)
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getCount() > 0) {
                    /* Modify by lee for scroll
                    pullToRefreshView.setSelection(adapter.getCount() - 1);
                    pullToRefreshView.smoothScrollToPosition(pullToRefreshView
                            .getCount() - 1);*/
                    pullToRefreshView.setSelection(adapter.getCount());
                    pullToRefreshView.smoothScrollToPosition(pullToRefreshView.getCount());
                }
            }
        }, delayTimeMills);
    }

    private synchronized void fetchPrevHistory()
    {
        if(adapter == null || pullToRefreshView.getAdapter() == null) {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        if(board == null || boardId<=0) {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        //if there isn't any stored message or messages are not loaded yet
        if(earliestMsgSentTime == null)
        {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        if(chatTableModel == null)
            chatTableModel = MyApp.getInstance().getChatDBModel();

        if(isFetchingPrevMessage) {
            return;
        }
        if(isNoMorePrevMessage)
        {
            pullToRefreshView.onRefreshComplete();
            return;
        }

        new Thread(){
            @Override
            public void run()
            {
                final String strEarliestMsgSentTime = MyDataUtils.format(earliestMsgSentTime);
                isFetchingPrevMessage = true;
                synchronized (syncMessagesObj) {

                    Integer fetchNumber = 100;

                    final List<ImMessageVO> messages = chatTableModel.getLatestChatsByTime(boardId, strEarliestMsgSentTime, true , fetchNumber);
                    boolean isNoStoredPrevMessage = false;
                    if (messages.size() > 0) {
                        Date date1 = getFirstMessageTime(messages);
                        Date date2 = getLastMessageTime(messages);
                        synchronized (syncMessagesObj) {

                            if(earliestMsgSentTime == null && date1 != null) {
                                earliestMsgSentTime = date1;
                            }
                            else if (date1 != null && earliestMsgSentTime != null && earliestMsgSentTime.getTime() > date1.getTime()) {
                                earliestMsgSentTime = date1;
                            }
                            else if(earliestMsgSentTime != null && date1 != null && earliestMsgSentTime.getTime() == date1.getTime())
                            {
                                isNoStoredPrevMessage = true;
                            }

                            if(latestMsgSentTime == null && date2 != null) {
                                latestMsgSentTime = date2;
                            }
                        }
                    }
                    if(isNoMorePrevMessage)
                    {
                        pullToRefreshView.onRefreshComplete();
                        return;
                    }
                    if(messages.size() > 0)
                    {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapter.addMessageItemsToTop(messages);
                                adapter.updateListView();

                                isFetchingPrevMessage = false;

                                pullToRefreshView.onRefreshComplete();
                            }
                        },100);
                    }
                    //if prev message is not stored in the database
                    if(!isNoStoredPrevMessage || messages.size()<fetchNumber) {
                        //check the prev message from server
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Integer number = 40;
                                Integer number = 10;
                                final String ealierThan = strEarliestMsgSentTime;
                                IMRequest.getMessageHistory(boardId, null, number, null, ealierThan, null,
                                        new ResponseCallBack<List<JSONObject>>() {

                                            @Override
                                            public void onCompleted(
                                                    JsonResponse<List<JSONObject>> response) {
                                                if (response.isSuccess()) {
                                                    List<JSONObject> jsonObjects = response.getData();
                                                    List<ImMessageVO> messages = storeMessages(jsonObjects);

                                                    if (messages.size() == 0) {

                                                        isNoMorePrevMessage = true;
                                                        isFetchingPrevMessage = false;
                                                    }

                                                    synchronized (syncMessagesObj) {
                                                        adapter.addMessageItemsToTop(messages);
                                                        adapter.updateListView();
                                                    }
                                                    callReadMessage();

                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pullToRefreshView.onRefreshComplete();
                                                        isFetchingPrevMessage = false;
                                                    }
                                                });
                                            }
                                        }, false);
                            }
                        });

                    }
                }
            }
        }.start();

    }

    private synchronized void syncLatestMessages()
    {
        if(adapter == null || pullToRefreshView.getAdapter() == null) {
            return;
        }
        if(board == null || boardId<=0) {
            return;
        }
        //if there isn't any stored message or messages are not loaded yet
        if(latestMsgSentTime == null)
        {
            return;
        }
        if(chatTableModel == null)
            chatTableModel = MyApp.getInstance().getChatDBModel();

        if(isFetchingNewMessage) {
            return;
        }
        if(noMoreMessage)
        {
            return;
        }

        //new SyncLatestMsgThread(boardId).start();
        SyncLatestMsgThread sycLastestMsg = new SyncLatestMsgThread(boardId);
        sycLastestMsg.start();


    }

    private synchronized void fetchHistory(String strEalierThan , String strLaterThan) {

        int TempId = 0;
        if (this.isFetchingNewMessage) return;
        synchronized (syncMessagesObj) {
            this.isFetchingNewMessage = true;

            if (noMoreMessage) {
                this.isFetchingNewMessage = false;
                pullToRefreshView.onRefreshComplete();
                return;
            }
        }

        Integer number = null;
        Integer lastDays = null;
        if (fetchByNumber) {
            number = 10; //number = 40;

        } else {
            lastDays = 7;
        }

        IMRequest.getMessageHistory(boardId, earliestMsgSentTime, number, lastDays, strEalierThan, strLaterThan,
                new ResponseCallBack<List<JSONObject>>() {

                    @Override
                    public void onCompleted(
                            JsonResponse<List<JSONObject>> response) {
                        if (response.isSuccess()) {
                            List<JSONObject> jsonObjects = response.getData();
                            List<ImMessageVO> messages = storeMessages(jsonObjects);
                            boolean isFirstRequest = earliestMsgSentTime == null;

                            if (messages.size() == 0) {

                                if (fetchByNumber) {
                                    noMoreMessage = true;
                                    isFetchingNewMessage = false;
                                    pullToRefreshView.onRefreshComplete();
                                    return;
                                }

                                fetchByNumber = true;
                                isFetchingNewMessage = false;
                                fetchHistory(null, null); // user number mode fetch again.
                                return;
                            }
                            fetchByNumber = true;

                            synchronized (syncMessagesObj) {
                                adapter.addMessageItemsToTop(messages);
                            }
                            callReadMessage();

                            if (isFirstRequest) {
                                scrollToLastPosition(300);
                            } else {
                                adapter.updateListView();
                            }

                            pullToRefreshView.onRefreshComplete();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pullToRefreshView.onRefreshComplete();
                                isFetchingNewMessage = false;
                            }
                        });

                        //Add by wang
                        enableOrDisableEditBTN();
                    }
                }, false);
    }

    private class SyncLatestMsgThread extends  Thread
    {
        int syncBoardId = 0;
        public SyncLatestMsgThread( int boardId)
        {
            syncBoardId = boardId;
        }
        @Override
        public void run()
        {
            isFetchingNewMessage = true;
            final String strLatestMsgSentTime = MyDataUtils.format(latestMsgSentTime);;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //final Integer number = 40;
                    final Integer number = 10;
                    final String laterThan = strLatestMsgSentTime;
                    IMRequest.getMessageHistory(syncBoardId, null, number, null, null, laterThan,
                            new ResponseCallBack<List<JSONObject>>() {

                                @Override
                                public void onCompleted(
                                        JsonResponse<List<JSONObject>> response) {
                                    if (response.isSuccess()) {
                                        List<JSONObject> jsonObjects = response.getData();
                                        List<ImMessageVO> messages = storeMessages(jsonObjects);

                                        if (messages.size() == 0 || messages.size()<number) {

                                            noMoreMessage = true;
                                            isFetchingNewMessage = false;
                                        }

                                        synchronized (syncMessagesObj) {
                                            adapter.addMessageItemsToTop(messages);
                                            adapter.updateListView();
                                        }
                                        callReadMessage();//ggg

                                        scrollToLastPosition(150);

                                        if(messages.size()>=number)
                                        {
                                            isFetchingNewMessage = false;
                                            if(!noMoreMessage) {
                                                boardId = syncBoardId;
                                                syncLatestMessages();
                                            }
                                        }
                                    }
                                }
                            }, false);
                }
            });
        }
    }


    public class MsgReceiver extends BroadcastReceiver {
        public MsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            IMRequest.checkNewMessage(new ResponseCallBack<List<IMBoardMessage>>() {

                @Override
                public void onCompleted(
                        JsonResponse<List<IMBoardMessage>> response) {
                    if (response.isSuccess()) {
                        if (adapter == null) return;
                        List<IMBoardMessage> msgs = response.getData();
                        for (IMBoardMessage msg : msgs) {
                            int board_id = msg.getBoard_id();
                            if (board_id == boardId) {
                                List<ImMessageVO> newMsgs = msg.getMessages();

                                adapter.addMessageItemsToTop(newMsgs);
                                adapter.updateListView();
                                callReadMessage();

                                storeListMessages(newMsgs);
                            }
                        }

                    }
                    enableOrDisableEditBTN();
                }
            });
        }
    }

    public class ContactChangedReceiver extends BroadcastReceiver {
        public ContactChangedReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int tempId = 0;
            if (boardId == 0)
                tempId = prevBoardId;
            else
                tempId = boardId;


            IMRequest.getGetBoardInfo(tempId, new ResponseCallBack<ImBoardVO>() {
                @Override
                public void onCompleted(JsonResponse<ImBoardVO> response) {
                    if (response.isSuccess()) {
                        boolean allowChat = false;
                        ImBoardVO imBoardVO = response.getData();
                        board = response.getData();

                        if (imBoardVO == null)
                            return;

                        if (imBoardVO.isGroup())
                            allowChat = true;
                        else
                        {
                            for (int i = 0; i < imBoardVO.getMembers().size(); i++) {
                                boolean isDelected = true;

                                for (int j = 0; j < MyApp.g_contactItems.size(); j++) {
                                    ContactItem item = MyApp.g_contactItems.get(j);
                                    if (item.getContactType() == 1)//purple contact
                                    {
                                        if (item.getContactId() == imBoardVO.getMembers().get(i).getUser().getUserId()) {
                                            isDelected = false;
                                            continue;
                                        }
                                    }
                                }
                                if (imBoardVO.getMembers().get(i).isFriend() && !isDelected)
                                    allowChat = true;
                                else if (imBoardVO.getMembers().get(i).isInDirectory())
                                    allowChat = true;
                            }
                        }

                        if (!allowChat) {
                            disabledForChat();
                        } else {
                            enableForChat();
                        }

                        adapter.updateBoard(board);
                    }
                    //add by wang
                    enableOrDisableEditBTN();
                }
            }, false);
        }
    }

    private LocationListener locationListener = new MyLocationListener();

    class MyLocationListener implements LocationListener {

        @Override
        public synchronized void onLocationChanged(Location location) {
            if (location != null) {
                latestLocation = location;
                System.out.println("----Location changed valueable----");
                lm.removeUpdates(locationListener);
                if(progressDialog!=null && progressDialog.isShowing())
                    progressDialog.dismiss();
                locationHandler.removeCallbacks(locationTimeoutRunnable);
                synchronized (lockObj) {
                    if (isGettingCurrentLocation) {
                        isGettingCurrentLocation = false;
                        Intent addLocationIntent = new Intent(ImBoardActivity.this, ImAddLocationMessageActivity.class);
                        addLocationIntent.putExtra("lat", latestLocation.getLatitude());
                        addLocationIntent.putExtra("long", latestLocation.getLongitude());
                        startActivityForResult(addLocationIntent, ADD_LOCATION_MESSAGE);
                    }
                }
            }
            else
            {
                System.out.println("----Location changed null----");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("----Provider Disabled --"+provider);
        }
    }
    private void deleteAllTempFiles()
    {
        File dir = new File(mTempDirectory);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                if(children[i].contains(".zip"))
                    continue;
                new File(dir, children[i]).delete();
            }
        }
    }
    private void disabledForChat()
    {
        if(popupWindow.isShowing())
            popupWindow.dismiss();

        inputMethodType = 0;
        updateChatControllerButtons();

        if (receiver != null && isReceiverRegistered == true) {
            unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }

        prevBoardId = boardId;
        boardId = 0;

        chatInputLayout.setBackgroundColor(getResources().getColor(R.color.purple_contact_color));
        chatInputMethodLayout.setBackgroundColor(getResources().getColor(R.color.purple_contact_color));
        imgBtnGetLocation.setEnabled(false);
        imgBtnGetLocation.setClickable(false);
        imgBtnEditChat.setEnabled(false);
        imgBtnEditChat.setClickable(false);
        imgBtnEmoticon.setEnabled(false);
        imgBtnEmoticon.setClickable(false);
        imgBtnGetPhoto.setEnabled(false);
        imgBtnGetPhoto.setClickable(false);
        imgBtnGetVideo.setEnabled(false);
        imgBtnGetVideo.setClickable(false);
        imgBtnMic.setEnabled(false);
        imgBtnMic.setClickable(false);
        edtMessage.setEnabled(false);
        edtMessage.setClickable(false);

        isInterfaceDisabled = true;
    }

    private void enableForChat()
    {
        //inputMethodType = 0;
        //updateChatControllerButtons();
        if (isInterfaceDisabled == false)
            return;

        if (this.receiver != null && isReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.receiver, msgReceiverIntent);
            isReceiverRegistered = true;
        }

        boardId = prevBoardId;

        chatInputLayout.setBackgroundColor(Color.parseColor("#ff72426e"));
        chatInputMethodLayout.setBackgroundColor(Color.parseColor("#ff72426e"));
        imgBtnGetLocation.setEnabled(true);
        imgBtnGetLocation.setClickable(true);
        imgBtnEditChat.setEnabled(true);
        imgBtnEditChat.setClickable(true);
        imgBtnEmoticon.setEnabled(true);
        imgBtnEmoticon.setClickable(true);
        imgBtnGetPhoto.setEnabled(true);
        imgBtnGetPhoto.setClickable(true);
        imgBtnGetVideo.setEnabled(true);
        imgBtnGetVideo.setClickable(true);
        imgBtnMic.setEnabled(true);
        imgBtnMic.setClickable(true);
        edtMessage.setEnabled(true);
        edtMessage.setClickable(true);

        isInterfaceDisabled = false;
    }
}
