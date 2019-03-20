package com.ginko.activity.entity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

import com.ginko.activity.im.EmoticonUtility;
import com.ginko.activity.im.EmoticonsGridAdapter;
import com.ginko.activity.im.ImAddLocationMessageActivity;
import com.ginko.activity.im.ImAddPhotoVideoMessageActivity;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.activity.im.PullToRefreshListView;
import com.ginko.activity.im.SoundMeter;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.CirclePageIndicator;
import com.ginko.common.Logger;
import com.ginko.common.MyDataUtils;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.ImMessageVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class EntityMessageActivity extends MyBaseFragmentActivity implements View.OnClickListener,
                                                        EmoticonsGridAdapter.EmoticonKeyClickListener ,
                                                        EntityMessageAdapter.onDeleteMessageListener ,
                                                        PullToRefreshListView.OnSinglePointTouchListener ,
                                                        ImInputEditTExt.OnEditTextKeyDownListener,
                                                        ActionSheet.ActionSheetListener
{

    /* UI Elements */
    private ImageButton btnPrev;
    private ImageView btnClose, btnDeleteAllMessage, btnDeleteChatHistory;
    private ImageView btnEmoticonPuple1, btnEmoticonPuple2, btnEmoticonPuple3, btnEmoticonPuple4;
    private ImageView imgBtnGetLocation, imgBtnGetPhoto, imgBtnGetVideo, imgBtnEmoticon;
    private ImageView imgBtnMic, imgMicSel, imgBtnSendMessage, imgBtnEditChat, imgBtnGinkoCall;

    private RelativeLayout rootLayout;
    private LinearLayout emoticonTypeLayout, chatInputMethodLayout;
    private RelativeLayout chatInputLayout, trashlayout;
    private RelativeLayout messageLayout;
    private LinearLayout emoticonsCover;

    private RelativeLayout headerlayout;

    private ImInputEditTExt edtMessage; //text message input field
    private PullToRefreshListView pullToRefreshView;
    private ListView messageListView;

    private View popUpView; //emoticon popup view
    private CirclePageIndicator emoticonCirclePageIndicator;
    private PopupWindow popupWindow;

    private ProgressHUD progressDialog;

    /* Variables */
    private EntityVO entity;
    private EntityMessageAdapter adapter;

    private boolean isGettingCurrentLocation = false;
    private boolean isDeletingMessage = false;
    private boolean isUILoaded = false;
    private boolean isFetchingMessage = false;
    private Date lastMsgSentTime;

    private boolean isCreate = false;

    /*Chat related variables */
    private boolean isKeyBoardVisible = false;
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
    private SoundMeter mSensor;
    private final int REFRESH_INTERVAL = 1000;
    private String strEditText = "";

    private long lastClickedTime = 0;//to check the double click event on the message listview to hide keyboard
    private float x = 0;
    private float y = 0;

    //---------------------------------//
    private boolean noMoreMessage = false;
    private boolean fetchByNumber = false;
    private final int MESSAGECOUNT_PER_PAGE = 40;
    private int currentPageNum = 0;

    //---------------------------------//

    /* Location Manager to get current location info */
    private LocationManager lm;
    private Location latestLocation = null;
    private Handler locationHandler;
    private Looper locationLooper;
    private Object lockObj = new Object();

    private final int GET_CURRENT_LOCATION_TIME_OUT = 10000;

    private Runnable locationTimeoutRunnable = new Runnable() {
        public void run() {
            synchronized (lockObj) {
                lm.removeUpdates(locationListener);
                if (progressDialog != null)
                    progressDialog.dismiss();
                isGettingCurrentLocation = false;
                MyApp.getInstance().showSimpleAlertDiloag(EntityMessageActivity.this ,R.string.str_failed_to_get_current_location , null);
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


    private boolean isEntityReceiverRegistered = false;

    //---------------------------------//
    /* new message listener */
    private EntityMsgReceiver entityMsgReceiver;

    private Set<Long> msgIdSet = new HashSet<Long>();

    //sharing messages
    private ActionSheet shareViaEmailActionSheet = null;
    public static EntityMessageVO sharingMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_board);

        this.isUILoaded = false;
        this.noMoreMessage = false;
        this.fetchByNumber = false;
        this.currentPageNum = 0;

        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            this.isCreate = savedInstanceState.getBoolean("isCreate");
        }
        else
        {
            this.entity = (EntityVO) getIntent().getSerializableExtra("entity");
            this.isCreate = getIntent().getBooleanExtra("isCreate", false);
        }

        if(this.entity == null)
        {
            finish();
            return;
        }

        getUIObjects();

        this.pullToRefreshView.setRefreshing();

        if(this.entityMsgReceiver != null)
        {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        TextView txtEntityName = (TextView)findViewById(R.id.txtContactName);
        txtEntityName.setText(this.entity.getName());

        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);

        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(this);
        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
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
        imgBtnGinkoCall = (ImageView) findViewById(R.id.btnGinkoCall);
        imgBtnGinkoCall.setVisibility(View.GONE);

        /* text Message input field */
        edtMessage = (ImInputEditTExt) findViewById(R.id.textMessage);
        edtMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isVoiceRecording) {
                    edtMessage.setFocusableInTouchMode(false);
                    edtMessage.setFocusable(false);
                    hideKeyboard();
                }
                else {
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
                      if(count > 0)
                          imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage);
                      else
                          imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage_disable);
                  }

                  @Override
                  public void afterTextChanged(Editable s) {
                      // TODO Auto-generated method stub
                      if(s.length() > 0)
                          imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage);
                      else
                          imgBtnSendMessage.setImageResource(R.drawable.btnchat_sendmessage_disable);
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
                fetchHistory();
            }
        });

        this.adapter = new EntityMessageAdapter(this, this.entity);
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
            for(int j = 0; j< EmoticonUtility.EMOTICON_COUNTS[i];j++)
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
        changeKeyboardHeight((int) popUpheight);

        emoticonHeight = getResources().getDimensionPixelSize(R.dimen.emoticon_height);

        enablePopUpView();
        checkKeyboardHeight(rootLayout);

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
                            strEditText = edtMessage.getText().toString();
                            startVoiceRecord(strVoiceRecordFilePath);
                             adapter.stopAllRecords();
                            imgBtnGetLocation.setAlpha(0.5f);
                            imgBtnGetPhoto.setAlpha(0.5f);
                            imgBtnGetVideo.setAlpha(0.5f);
                            imgBtnEmoticon.setAlpha(0.5f);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        imgBtnGetLocation.setAlpha(1.0f);
                        imgBtnGetPhoto.setAlpha(1.0f);
                        imgBtnGetVideo.setAlpha(1.0f);
                        imgBtnEmoticon.setAlpha(1.0f);
                        if (isVoiceRecording && System.currentTimeMillis() - voiceRecordStartTime < 1000) {
                            stopVoiceRecording();
                            adapter.setVoiceRecord(false);
                            isVoiceRecording = false;
                            edtMessage.setText(strEditText);
                            File file = new File(strVoiceRecordFilePath);
                            if (file.exists())
                                file.delete();
                            return true;

                        }

                        if (isVoiceRecording) {
                            stopVoiceRecording();
                            adapter.setVoiceRecord(false);
                            //send recorded voice message
                            if (event.getX() + imgBtnMic.getLeft() < chatInputLayout.getWidth() / 2)//slide to cancel
                            {
                                //delete recorded voice file
                                isVoiceRecording = false;
                                File file = new File(strVoiceRecordFilePath);
                                edtMessage.setText(strEditText);
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
                                                    isVoiceRecording = false;
                                                    Properties param = new Properties();
                                                    param.setProperty("voice_length", String.valueOf(EntityMessageActivity.this.recordedVoiceTime) + "");
                                                    sendMediaMessage(EntityMessageVO.MSG_VOICE, EntityMessageActivity.this.strVoiceRecordFilePath);
                                                    edtMessage.setText(strEditText);
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    //No button clicked
                                                    //delete recorded voice file
                                                    isVoiceRecording = false;
                                                    File file = new File(strVoiceRecordFilePath);
                                                    if (file.exists())
                                                        file.delete();
                                                    dialog.dismiss();
                                                    edtMessage.setText(strEditText);
                                                    break;
                                            }
                                        }
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(EntityMessageActivity.this);
                                    builder.setMessage("Do you want to send your voice message?").setPositiveButton("Yes", dialogClickListener)
                                            .setNegativeButton("No", dialogClickListener).show();
                                } else {
                                    isVoiceRecording = false;
                                    Toast.makeText(EntityMessageActivity.this, "Failed to send voice message", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        break;

                }
                return true;
            }
        });

        if (!isCreate){
            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            btnPrev.setImageResource(R.drawable.part_a_btn_back_nav);
            txtEntityName.setTextColor(getResources().getColor(R.color.photo_video_editor_home_txt_color));
        }
        else {
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_back_nav_white);
            txtEntityName.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnClose.setImageResource(R.drawable.close_white);
            btnDeleteAllMessage.setImageResource(R.drawable.img_car_white);
        }

        updateChatControllerButtons();

        if(mSensor == null)
            mSensor = new SoundMeter();

        this.isUILoaded = true;

        enableOrDisableEditBTN();
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
        if(isKeyBoardVisible)
            MyApp.getInstance().hideKeyboard(rootLayout);
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

    public void shareMessageViaEmail(EntityMessageVO msg)
    {
        sharingMessage = msg;
        setTheme(R.style.ActionSheetStyleIOS7);

        if(shareViaEmailActionSheet == null)
            shareViaEmailActionSheet = ActionSheet.createBuilder(EntityMessageActivity.this, getSupportFragmentManager())
                    .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                    .setOtherButtonTitles(
                            getResources().getString(R.string.str_share_via_email))
                    .setCancelableOnTouchOutside(true)
                    .setListener(this)
                    .show();
        else
            shareViaEmailActionSheet.show(getSupportFragmentManager(), "actionSheet");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.adapter != null)
            this.adapter.registerDownloadManager();

        //update the controller UI buttons
        if (this.isUILoaded)
            updateChatControllerButtons();

        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }

        fetchHistory();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null)
        {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        }
        if(entity == null)
        {
            finish();
            return;
        }

        getUIObjects();

        isUILoaded = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entity", this.entity);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.adapter != null)
            this.adapter.unregisterDownloadManager();

        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == true) {
            unregisterReceiver(this.entityMsgReceiver);
            isEntityReceiverRegistered = false;
        }
        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }
        if (isVoiceRecording){
            imgBtnGetLocation.setAlpha(1.0f);
            imgBtnGetPhoto.setAlpha(1.0f);
            imgBtnGetVideo.setAlpha(1.0f);
            imgBtnEmoticon.setAlpha(1.0f);
            if (isVoiceRecording && System.currentTimeMillis() - voiceRecordStartTime < 1000) {
                isVoiceRecording = false;
                stopVoiceRecording();
                adapter.setVoiceRecord(isVoiceRecording);
                edtMessage.setText(strEditText);
                File file = new File(strVoiceRecordFilePath);
                if (file.exists())
                    file.delete();           }

            if (isVoiceRecording) {
                stopVoiceRecording();
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
                                        isVoiceRecording = false;
                                        Properties param = new Properties();
                                        param.setProperty("voice_length", String.valueOf(EntityMessageActivity.this.recordedVoiceTime) + "");
                                        sendMediaMessage(EntityMessageVO.MSG_VOICE, EntityMessageActivity.this.strVoiceRecordFilePath);
                                        edtMessage.setText(strEditText);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        //delete recorded voice file
                                        isVoiceRecording = false;
                                        File file = new File(strVoiceRecordFilePath);
                                        if (file.exists())
                                            file.delete();
                                        dialog.dismiss();
                                        edtMessage.setText(strEditText);
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(EntityMessageActivity.this);
                        builder.setMessage("Do you want to send your voice message?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    } else {
                        Toast.makeText(EntityMessageActivity.this, "Failed to send voice message", Toast.LENGTH_LONG).show();
                    }
            }
        }
        //hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.debug("Received the image");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case ADD_LOCATION_MESSAGE:
                    double lat = data.getDoubleExtra("lat" , 0.0d);
                    double lng = data.getDoubleExtra("long" , 0.0d);
                    sendLocationMessage(lat , lng);
                    break;

                case ADD_PHOTO_MESSAGE:
                    String strPhotoPath = data.getStringExtra("photoPath");
                    if(strPhotoPath.contains("file://"))
                        strPhotoPath = strPhotoPath.replaceAll(" " , "%20");
                    sendMediaMessage(EntityMessageVO.MSG_PHOTO , strPhotoPath);
                    break;

                case ADD_VIDEO_MESSAGE:
                    String strVideoPath = data.getStringExtra("videoPath");
                    if(strVideoPath.contains("file://"))
                        strVideoPath = strVideoPath.replaceAll(" " , "%20");
                    String strThumbPath = data.getStringExtra("thumbPath");
                    if(strThumbPath.contains("file://"))
                        strThumbPath = strThumbPath.replaceAll(" " , "%20");
                    sendVideoMessage(strThumbPath , strVideoPath );
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                hideKeyboard();
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                finish();
                break;

            //delete selected message
            case R.id.btnDeleteAllMessage:
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                AlertDialog.Builder deleteAllDialogBuilder = new AlertDialog.Builder(this);
                deleteAllDialogBuilder.setTitle("Confirm");
                deleteAllDialogBuilder.setMessage(getResources().getString(R.string.str_delete_all_chat_messages));
                deleteAllDialogBuilder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        EntityRequest.clearAllMessages(entity.getId(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    inputMethodType = 0;
                                    btnDeleteAllMessage.setVisibility(View.GONE);
                                    btnClose.setVisibility(View.GONE);
                                    btnPrev.setVisibility(View.VISIBLE);

                                    noMoreMessage = true;
                                    isFetchingMessage = false;
                                    adapter.isSelectable(false);
                                    adapter.clearAllMessages();
                                    adapter.refreshMessageSelection();
                                    adapter.updateListView();
                                    imgBtnEditChat.setImageResource(R.drawable.btnchatedit_disable);
                                    imgBtnEditChat.setEnabled(false);
                                    updateChatControllerButtons();

                                    enableOrDisableEditBTN();
                                } else {
                                    MyApp.getInstance().showSimpleAlertDiloag(EntityMessageActivity.this, "Failed to delete all messages.", null);
                                }
                            }
                        } , true);
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
                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                if (adapter != null) {
                    AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this);
                    deleteDialogBuilder.setTitle("Confirm");
                    deleteDialogBuilder.setMessage("Do you want to delete selected chat history?");
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

                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                if (lm == null)
                    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (isGettingCurrentLocation)
                    return;
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    if(latestLocation != null)
                    {
                        Intent addLocationIntent = new Intent(EntityMessageActivity.this, ImAddLocationMessageActivity.class);
                        addLocationIntent.putExtra("lat", latestLocation.getLatitude());
                        addLocationIntent.putExtra("long", latestLocation.getLongitude());
                        addLocationIntent.putExtra("isCreate", isCreate);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(EntityMessageActivity.this);
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

                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                hideKeyboard();
                Intent photoIntent = new Intent(EntityMessageActivity.this, ImAddPhotoVideoMessageActivity.class);
                photoIntent.putExtra("isPhotoIntent", true);
                photoIntent.putExtra("isEntityMessage", true);
                photoIntent.putExtra("isCreate", isCreate);
                startActivityForResult(photoIntent, ADD_PHOTO_MESSAGE);
                break;

            //get video from camera or gallery to send video
            case R.id.imgBtnVideo:
                if(isVoiceRecording)
                    return;

                if (this.adapter != null) {
                    this.adapter.stopAllRecords();
                    this.adapter.unregisterDownloadManager();
                }
                hideKeyboard();
                Intent videoIntent = new Intent(EntityMessageActivity.this, ImAddPhotoVideoMessageActivity.class);
                videoIntent.putExtra("isPhotoIntent", false);
                videoIntent.putExtra("isEntityMessage" , true);
                videoIntent.putExtra("isCreate", isCreate);
                startActivityForResult(videoIntent, ADD_VIDEO_MESSAGE);
                break;

            //get emoticons
            case R.id.imgBtnEmoticon:
                if(isVoiceRecording)
                    return;
                inputMethodType = 1;
                updateChatControllerButtons();
                showEmoticonView();
                nEmoticonType = 3;
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
                } else {
                    inputMethodType = 2;
                    /*
                    btnDeleteAllMessage.setVisibility(View.VISIBLE);
                    btnClose.setVisibility(View.VISIBLE);
                    btnPrev.setVisibility(View.GONE);
                    adapter.isSelectable(true);
                    adapter.updateListView();
                    */
                    if(pullToRefreshView.getCount() > 1)
                    {
                        inputMethodType = 2;
                        btnDeleteAllMessage.setVisibility(View.VISIBLE);
                        btnClose.setVisibility(View.VISIBLE);
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

                        MyApp.getInstance().showSimpleAlertDiloag(EntityMessageActivity.this, strAlert, newListener);
                    }
                }
                break;
        }
    }

    /*
    Add by wang
     */
    private void enableOrDisableEditBTN(){
        //Integer number = 40;
        if (adapter != null && adapter.getCount() > 0) {
            imgBtnEditChat.setImageResource(R.drawable.btnchatedit);
            imgBtnEditChat.setEnabled(true);
        } else {
            imgBtnEditChat.setImageResource(R.drawable.btnchatedit_disable);
            imgBtnEditChat.setEnabled(false);
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
            } else {
                emoticonsCover.setVisibility(LinearLayout.VISIBLE);
            }
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
        if (popupWindow.isShowing() && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
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
            finish();
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
                    Toast.makeText(EntityMessageActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
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
                        Toast.makeText(EntityMessageActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                else
                {
                    Toast.makeText(EntityMessageActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
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
                        Intent addLocationIntent = new Intent(EntityMessageActivity.this, ImAddLocationMessageActivity.class);
                        addLocationIntent.putExtra("lat", latestLocation.getLatitude());
                        addLocationIntent.putExtra("long", latestLocation.getLongitude());
                        addLocationIntent.putExtra("isCreate", isCreate);
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


    /* start voice recording */
    private void startVoiceRecord(String name) {
        if (this.adapter != null) {
            this.adapter.stopAllRecords();
            this.adapter.unregisterDownloadManager();
        }
        mSensor.start(name);
        imgMicSel.setVisibility(View.VISIBLE);
        imgBtnEditChat.setVisibility(View.INVISIBLE);
        edtMessage.setText("00:00 Slide to Cancel <");

        mHandler.postDelayed(mRecordRefreshTask, REFRESH_INTERVAL);
    }

    //-------------------------------------------------------------------------------------------------------//
    //                     Vocie Recording Functions                                                          //
    //-------------------------------------------------------------------------------------------------------//
    /* stop voice recording*/
    private void stopVoiceRecording() {
        mHandler.removeCallbacks(mRecordRefreshTask);
        mSensor.stop();
        edtMessage.setText("");
        this.recordedVoiceTime = ((int)((System.currentTimeMillis() - this.voiceRecordStartTime) / 1000L));
        imgMicSel.setVisibility(View.INVISIBLE);
        imgBtnEditChat.setVisibility(View.VISIBLE);
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

    //-------------------------------------------------------------------------------------------------------//
    //                      Entity Messsage Functions                                                       //
    //-------------------------------------------------------------------------------------------------------//

    private void sendMessage() {
        final String strMsg = this.edtMessage.getText().toString();
        this.edtMessage.setText("");
        EntityRequest.sendMessage(this.entity.getId(), strMsg, new ResponseCallBack<EntityMessageVO>() {
            @Override
            public void onCompleted(JsonResponse<EntityMessageVO> response) {
                if (response.isSuccess()) {
                    edtMessage.setText("");
                    EntityMessageVO newMsg = (EntityMessageVO) response.getData();
                    newMsg.setMsgType(1);
                    newMsg.setFrom(RuntimeContext.getUser().getUserId());
                    newMsg.setContent(strMsg);
                    showMsgInBoard(newMsg);
                }
            }
        },
        false);
    }

    private void sendLocationMessage(double lattitude, double longitude) {
        final String str = ConstValues.IM_LOCATION_PREFIX + String.valueOf(lattitude) + "," + String.valueOf(longitude);
        EntityMessageVO newMsg = new EntityMessageVO();
        newMsg.setMsgId(0);
        newMsg.isPending = true;
        newMsg.setMsgType(Integer.valueOf(1));
        newMsg.setMessageType(ImMessageVO.MSG_TYPE_LOCATION);
        newMsg.setFrom(RuntimeContext.getUser().getUserId());
        newMsg.setSendTime(Calendar.getInstance().getTime());
        newMsg.setContent(str);

        showMsgInBoard(newMsg);

        final EntityMessageVO sentMsg = newMsg;
        EntityRequest.sendMessage(this.entity.getId(), str, new ResponseCallBack<EntityMessageVO>() {
            @Override
            public void onCompleted(JsonResponse<EntityMessageVO> response) {
                if (response.isSuccess()) {
                    EntityMessageVO msg = (EntityMessageVO) response.getData();
                    sentMsg.isPending = false;
                    sentMsg.setMsgId(msg.getMsgId());
                    sentMsg.setSendTime(msg.getSendTime());

                    adapter.addNewMessageIDToMessageIDList(msg.getMsgId());
                    adapter.notifyDataSetChanged();
                }
                //Add by wang
                enableOrDisableEditBTN();
            }
        }
        ,false);
    }

    private void sendMediaMessage(final String mediaType , final String filePath)
    {
        final File file = new File(filePath);
        if(!file.exists())
            return;
        EntityMessageVO mediaMsg = new EntityMessageVO();
        mediaMsg.isPending = true;
        mediaMsg.setMsgId(0);
        mediaMsg.setMsgType(2);
        if(mediaType.equals(ImMessageVO.MSG_VOICE))
            mediaMsg.setMessageType(ImMessageVO.MSG_TYPE_VOICE);
        else if(mediaType.equals(ImMessageVO.MSG_PHOTO))
            mediaMsg.setMessageType(ImMessageVO.MSG_TYPE_PHOTO);
        mediaMsg.setFile(file.getAbsolutePath());
        mediaMsg.setSendTime(Calendar.getInstance().getTime());
        mediaMsg.setFrom(RuntimeContext.getUser().getUserId());

        showMsgInBoard(mediaMsg);

        final EntityMessageVO sentMsg = mediaMsg;

        EntityRequest.sendFile(this.entity.getId(), mediaType, null, file,
                new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject jsonRes = response.getData();
                            sentMsg.isPending = false;
                            int msgId = jsonRes.optInt("msg_id");
                            sentMsg.setMsgId(msgId);
                            sentMsg.setSendTime((MyDataUtils.convertUTCTimeToLocalTime(jsonRes.optString("send_time"))));

                            adapter.addNewMessageIDToMessageIDList(msgId);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                , false);
    }
    private void sendVideoMessage(final String thumbFilePath , final String videoFilePath)
    {
        final File videoFile = new File(videoFilePath);
        if(!videoFile.exists())
            return;
        final File thuumbFile = new File(thumbFilePath);
        if(!thuumbFile.exists())
            return;

        EntityMessageVO mediaMsg = new EntityMessageVO();
        mediaMsg.isPending = true;
        mediaMsg.setMsgId(0);
        mediaMsg.setMsgType(2);
        mediaMsg.setMessageType(ImMessageVO.MSG_TYPE_VIDEO);
        mediaMsg.setFile(videoFilePath);
        mediaMsg.setThumnail(thumbFilePath);
        mediaMsg.setSendTime(Calendar.getInstance().getTime());
        mediaMsg.setFrom(RuntimeContext.getUser().getUserId());

        showMsgInBoard(mediaMsg);

        final EntityMessageVO sentMsg = mediaMsg;

        EntityRequest.sendFile(this.entity.getId(), EntityMessageVO.MSG_VIDEO, thuumbFile, videoFile,
                new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject jsonRes = response.getData();
                            sentMsg.isPending = false;
                            int msgId = jsonRes.optInt("msg_id");
                            sentMsg.setMsgId(msgId);
                            sentMsg.setSendTime((MyDataUtils.convertUTCTimeToLocalTime(jsonRes.optString("send_time"))));

                            adapter.addNewMessageIDToMessageIDList(msgId);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                , false);
    }
    private void showMsgInBoard(EntityMessageVO msg) {
        this.adapter.addMessageItem(msg);
        this.adapter.notifyDataSetChanged();

        /*
        if (pullToRefreshView.getCount() > 0) {
            this.pullToRefreshView.setSelection(this.pullToRefreshView.getCount() - 1);
            this.pullToRefreshView.smoothScrollToPosition(this.pullToRefreshView.getCount() - 1);
            imgBtnEditChat.setImageResource(R.drawable.btnchatedit);
            imgBtnEditChat.setEnabled(true);
        }
        */
        scrollToLastPosition(300);

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

    private void fetchHistory() {

        if (this.isFetchingMessage) {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        this.isFetchingMessage = true;

        if (noMoreMessage) {
            this.isFetchingMessage = false;
            pullToRefreshView.onRefreshComplete();
            return;
        }

        EntityRequest.listMessages(this.entity.getId(), currentPageNum, MESSAGECOUNT_PER_PAGE,
                new ResponseCallBack<JSONObject>() {

                    @Override
                    public void onCompleted(
                            JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject messagesObj = response.getData();
                            int messageCount = messagesObj.optInt("total", 0);
                            if (messageCount == 0) {

                                noMoreMessage = true;
                                isFetchingMessage = false;
                                pullToRefreshView.onRefreshComplete();

                                //imgBtnEditChat.setImageResource(R.drawable.btnchatedit_disable);
                                //imgBtnEditChat.setEnabled(false);

                                return;
                            }

                            imgBtnEditChat.setImageResource(R.drawable.btnchatedit);
                            imgBtnEditChat.setEnabled(true);

                            if (messageCount < MESSAGECOUNT_PER_PAGE) {
                                noMoreMessage = true;
                            }

                            currentPageNum++;

                            List<EntityMessageVO> messages = new ArrayList<EntityMessageVO>();
                            try {
                                JSONArray messagesArray = messagesObj.getJSONArray("data");
                                for (int i = 0; i < messagesArray.length(); i++) {
                                    JSONObject msgObj = messagesArray.getJSONObject(i);
                                    EntityMessageVO msg = JsonConverter.json2Object(
                                            msgObj, (Class<EntityMessageVO>) EntityMessageVO.class);

                                    if (msgIdSet == null)
                                        msgIdSet = new HashSet<Long>();

                                    if (!msgIdSet.contains(msg.getMsgId())) {
                                        messages.add(msg);
                                        msgIdSet.add(msg.getMsgId());
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (JsonConvertException e) {
                                e.printStackTrace();
                            }

                            boolean isFirstRequest = lastMsgSentTime == null;
                            if (lastMsgSentTime == null) {
                                lastMsgSentTime = messages.get(0).getSendTime();
                            }
                            int size = messages.size();
                            for (int i = 0; i < size; i++) {
                                if (lastMsgSentTime.getTime() > messages.get(i).getSendTime().getTime())
                                    lastMsgSentTime = messages.get(i).getSendTime();
                            }


                            if (messages.size() > 0)
                                adapter.addMessageItemsToTop(messages);
                            adapter.updateListView();

                            if (isFirstRequest) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (adapter.getCount() > 0) {
                                            pullToRefreshView.setSelection(adapter.getCount());
                                            pullToRefreshView.smoothScrollToPosition(pullToRefreshView
                                                    .getCount());
                                        }
                                    }
                                }, 300);
                            }

                            pullToRefreshView.onRefreshComplete();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pullToRefreshView.onRefreshComplete();
                                isFetchingMessage = false;
                            }
                        });
                        //Add by wang
                        enableOrDisableEditBTN();
                    }
                }, false);

    }

    public class EntityMsgReceiver extends BroadcastReceiver {
        public EntityMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            fetchHistory();
        }
    }
}
