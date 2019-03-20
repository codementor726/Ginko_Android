package com.ginko.customview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.ginko.activity.im.DialogMessageAdapter;
import com.ginko.activity.im.EmoticonUtility;
import com.ginko.activity.im.EmoticonsGridAdapter;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.activity.im.PullToRefreshListView;
import com.ginko.api.request.IMRequest;
import com.ginko.common.CirclePageIndicator;
import com.ginko.common.MyDataUtils;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ChatTableModel;
import com.ginko.database.MessageDbConstruct;
import com.ginko.fragments.EntityAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.IMBoardMessage;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImMessageVO;
import com.sz.util.json.JsonConverter;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by YongJong on 05/30/17.
 */
public class TextChatDialog extends Dialog implements
        View.OnClickListener,
        ImInputEditTExt.OnEditTextKeyDownListener,
        EmoticonsGridAdapter.EmoticonKeyClickListener ,
        DialogMessageAdapter.onDeleteMessageListener ,
        PullToRefreshListView.OnSinglePointTouchListener{

    private onCloseChatListener closeListener = null;
    private onReceiveMsgListener messageListener = null;

    private Context mContext;

    private ImageView btnClose;
    private ImageView btnEmoticonPuple1, btnEmoticonPuple2, btnEmoticonPuple3, btnEmoticonPuple4;
    private ImageView imgBtnEmoticon, imgBtnSendMessage;

    private RelativeLayout rootLayout;
    private LinearLayout emoticonTypeLayout;
    private LinearLayout emoticonsCover;
    private ImInputEditTExt edtMessage; //text message input field
    private PullToRefreshListView pullToRefreshView;

    private View popUpView; //emoticon popup view
    private CirclePageIndicator emoticonCirclePageIndicator;
    private PopupWindow popupWindow;

    private ProgressHUD progressDialog;
    private int boardId;
    private int prevBoardId = 0;
    private String contactName = "";
    private ImBoardVO board;

    private DialogMessageAdapter adapter;
    private ArrayList<EventUser> lstUsers;

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
    private int keyboardHeight;
    private int emoticonHeight;

    private EmoticonUtility emoticons;

    private int nEmoticonType = 3;
    private int inputMethodType = 0;
    private String strEditText = "";

    //---------------------------------//
    private boolean isNoMorePrevMessage = false;
    private boolean noMoreMessage = false;
    private boolean fetchByNumber = false;
    private Date earliestMsgSentTime, latestMsgSentTime;
    private String groupName = null;

    //---------------------------------//
    /* new message listener */

    private Object syncMessagesObj = new Object();
    private ChatTableModel chatTableModel;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private View.OnClickListener mCloseClickListener;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    private EmoticonsPagerAdapter[] emoticonAdapters;


    public TextChatDialog(Context context, int boardId, ArrayList<EventUser> users) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        mContext = context;

        this.isReceiverRegistered = false;
        this.isChangedContact = false;
        this.isUILoaded = false;
        this.noMoreMessage = false;
        this.isNoMorePrevMessage = false;
        this.fetchByNumber = false;
        this.earliestMsgSentTime = null;
        this.boardId = boardId;
        this.lstUsers = new ArrayList<EventUser>(users);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.textchat_conference_popup);

        chatTableModel = MyApp.getInstance().getChatDBModel();

        getBoardInfo();

    }

    public void setCloseListener(onCloseChatListener _listener)
    {
        closeListener = _listener;
    }

    public void setMessageListener(onReceiveMsgListener _listener)
    {
        messageListener = _listener;
    }

    private void setLayout() {
        btnClose = (ImageView) findViewById(R.id.imgDimClose);
        btnClose.setClickable(true);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (closeListener != null)
                    closeListener.onCloseChatUpdated();
                hideKeyboard();
            }
        });

        //buttons to select emoticon type
        btnEmoticonPuple1 = (ImageView) findViewById(R.id.emoticonPuple1);
        btnEmoticonPuple1.setOnClickListener(this);
        btnEmoticonPuple2 = (ImageView) findViewById(R.id.emoticonPuple2);
        btnEmoticonPuple2.setOnClickListener(this);
        btnEmoticonPuple3 = (ImageView) findViewById(R.id.emoticonPuple3);
        btnEmoticonPuple3.setOnClickListener(this);
        btnEmoticonPuple4 = (ImageView) findViewById(R.id.emoticonPuple4);
        btnEmoticonPuple4.setOnClickListener(this);

        imgBtnEmoticon = (ImageView) findViewById(R.id.imgBtnEmoticon);
        imgBtnEmoticon.setOnClickListener(this);

        imgBtnSendMessage = (ImageView) findViewById(R.id.imgBtnSendMessage);
        imgBtnSendMessage.setOnClickListener(this);

        /* text Message input field */
        edtMessage = (ImInputEditTExt) findViewById(R.id.textMessage);

        edtMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                edtMessage.setFocusableInTouchMode(true);
                edtMessage.setFocusable(true);
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return false;
                }
                if (!isKeyBoardVisible) {
                    showKeyboard();
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

        emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

        //list view for chat messages
        pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pullToRefreshView);

        pullToRefreshView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String label = DateUtils.formatDateTime(mContext, Calendar.getInstance().getTimeInMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                System.out.println("----Pull to Refresh called---");
                // Do work to refresh the list here.
                fetchPrevHistory();
            }
        });

        this.adapter = new DialogMessageAdapter(mContext, board);

        pullToRefreshView.setAdapter(adapter);
        this.adapter.registerDownloadManager();

        pullToRefreshView.setOnSinglePointTouchListener(this);

        this.emoticons = MyApp.getInstance().getEmoticonUtility();
        this.emoticonAdapters = new EmoticonsPagerAdapter[4];
        for(int i =0 ;i<4;i++)
        {
            ArrayList<Integer> emoticonIndexes = new ArrayList<Integer>();
            for(int j = 0; j<EmoticonUtility.EMOTICON_COUNTS[i];j++)
            {
                emoticonIndexes.add(Integer.valueOf(j+EmoticonUtility.EMOTICON_TYPE_START_INDEX[i]));
            }
            emoticonAdapters[i] = new EmoticonsPagerAdapter(mContext , emoticonIndexes , this);
        }

        popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
        emoticonCirclePageIndicator = (CirclePageIndicator) popUpView.findViewById(R.id.page_indicator);

        // Defining default height of keyboard which is equal to 230 dip
        final float popUpheight = mContext.getResources().getDimension(
                R.dimen.keyboard_height);
        changeKeyboardHeight((int)popUpheight);

        emoticonHeight = mContext.getResources().getDimensionPixelSize(R.dimen.emoticon_height);

        enablePopUpView();
        checkKeyboardHeight(rootLayout);

        this.isUILoaded = true;
    }

    @Override
    public void onImEditTextBackKeyDown() {

    }

    public void getBoardInfo() {
        IMRequest.getGetBoardInfo(boardId, new ResponseCallBack<ImBoardVO>() {

            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    ImBoardVO imBoardVO = response.getData();
                    board = response.getData();
                    board.setVideoChat(true);
                    board.setLstUsers(lstUsers);
                    // check exist contact on list.

                    boolean allowChat = false;
                    if (imBoardVO == null)
                        return;

                    setLayout();
                    fillData();

                } else {
                    if (response.getErrorCode() == 9999) {
                        DialogInterface.OnClickListener dlgListner = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getBoardInfo();
                            }
                        };

                        MyApp.getInstance().showSimpleAlertDiloag(mContext, "Internet connection is missing.", dlgListner);
                    }
                }
            }
        }, true);
    }

    private void fillData() {
        //first try to get messages from local database
        //Integer number = 40;
        Integer number = 10;
        List<ImMessageVO> messages = chatTableModel.getLatestChatsByTime(boardId, null, false, number);

        if (messages != null && messages.size() > 0) {
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
        } else//if there isn't any message stored in the database , then fetch history from server
        {
            this.pullToRefreshView.setRefreshing();

            fetchHistory(null, null);
        }
        MyApp.getInstance().setCurrentBoardId(boardId);
    }

    private synchronized Date getFirstMessageTime(List<ImMessageVO> messages) {
        Date earlyTime = null;
        if (messages == null || messages.size() <= 0)
            return earlyTime;
        synchronized (syncMessagesObj) {
            int index = 0;
            if (earlyTime == null) {
                for (index = 0; index < messages.size(); index++) {
                    if (messages.get(index).utcSendTime != null) {
                        earlyTime = messages.get(index).utcSendTime;
                        break;
                    }
                }
            }
            int size = messages.size();
            for (int i = index + 1; i < size; i++) {
                if (messages.get(i).utcSendTime == null)
                    continue;
                if (earlyTime.getTime() > messages.get(i).utcSendTime.getTime())
                    earlyTime = messages.get(i).utcSendTime;
            }
        }

        return earlyTime;
    }

    private synchronized Date getLastMessageTime(List<ImMessageVO> messages) {
        Date latestTime = null;
        if (messages == null || messages.size() <= 0)
            return latestTime;
        synchronized (syncMessagesObj) {
            int index = 0;
            if (latestTime == null) {
                for (index = 0; index < messages.size(); index++) {
                    if (messages.get(index).utcSendTime != null) {
                        latestTime = messages.get(index).utcSendTime;
                        break;
                    }
                }
            }
            int size = messages.size();
            for (int i = index + 1; i < size; i++) {
                if (messages.get(i).utcSendTime == null)
                    continue;
                if (latestTime.getTime() < messages.get(i).utcSendTime.getTime())
                    latestTime = messages.get(i).utcSendTime;
            }
        }
        return latestTime;
    }

    public boolean  isShownKeyboard() {return isKeyBoardVisible;}

    public void hideKeyboard() {
        //if keyboard is shown, then hide it
        if (isKeyBoardVisible) {
            MyApp.getInstance().hideKeyboard(rootLayout);

            //Add by wang
            btnEmoticonPuple1.setImageResource(R.drawable.emoji_puple1_normal);
            btnEmoticonPuple2.setImageResource(R.drawable.emoji_puple2_normal);
            btnEmoticonPuple3.setImageResource(R.drawable.emoji_puple3_normal);
            btnEmoticonPuple4.setImageResource(R.drawable.emoji_puple4_normal);
            isKeyBoardVisible = false;
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtMessage, InputMethodManager.SHOW_FORCED);
    }

    private void updateViewWithEmoticonType() {
        ViewPager pager = (ViewPager) this.popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(this.emoticonAdapters[this.nEmoticonType]);
        this.emoticonCirclePageIndicator.setViewPager(pager);
        this.emoticonCirclePageIndicator.notifyDataSetChanged();
    }

    public void checkNewMessages()
    {
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
            }
        });
    }

    public void updateListView()
    {
        if (adapter == null) return;
        adapter.updateListView();
    }

    private void displayMessageEvent(List<ImMessageVO> msgs)
    {
        if (msgs == null)
            return;

        for (int i=0; i<msgs.size(); i++)
        {
            if (messageListener != null)
                messageListener.onMessageUpdated(msgs.get(i));
        }
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
                                if (popupWindow.isShowing())
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

    private void changeKeyboardHeight(int height) {

        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }
    }

    @Override
    public void EmoticonKeyClickedIndex(int index) {
        if (index == -1) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            edtMessage.dispatchKeyEvent(event);
            return;
        }
        String strEmoticonCode = this.emoticons.getEmoticonCode(index);
        BitmapDrawable emoticonDrawable = new BitmapDrawable(mContext.getResources(), this.emoticons.getEmoticon(index));
        emoticonDrawable.setBounds(0, 0, this.emoticonHeight * (emoticonDrawable.getIntrinsicHeight() / emoticonDrawable.getIntrinsicWidth()), this.emoticonHeight);
        ImageSpan imgSpan = new ImageSpan(emoticonDrawable, 0);
        int selectionStart = this.edtMessage.getSelectionStart();
        int selectionEnd = this.edtMessage.getSelectionEnd();
        this.edtMessage.getEditableText().replace(selectionStart, selectionEnd, strEmoticonCode);
        this.edtMessage.getEditableText().setSpan(imgSpan, selectionStart, selectionStart + strEmoticonCode.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void onSinglePointTouchEvent() {

    }

    @Override
    public void onDeleteMessage(boolean isDeleted, int deletedMessageCount) {

    }

    @Override
    public void onBoardListItemSelected(int selectedItemPosition, boolean selectedItemsCount) {

    }

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

    private void showMsgInBoard(ImMessageVO msg) {
        this.adapter.addMessageItem(msg);
        this.adapter.notifyDataSetChanged();

        scrollToLastPosition(300);
    }

    private synchronized void callReadMessage() {
        List<Long> msgIds = adapter.getUnreadMessageList();
        String strMsgIds = "";
        if (msgIds.size() > 0) {
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
                    if (response.isSuccess()) {

                    }
                }
            });
        }
    }

    private List<ImMessageVO> storeMessages(List<JSONObject> jsonObjects) {
        List<ImMessageVO> messages = new ArrayList<ImMessageVO>();
        if (simpleDateFormat == null)
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < jsonObjects.size(); i++) {
            try {
                JSONObject jsonObject = jsonObjects.get(i);
                ImMessageVO msg = JsonConverter.json2Object(jsonObject, (Class<ImMessageVO>) ImMessageVO.class);
                String strUtcTime = jsonObject.getString("send_time");
                msg.utcSendTime = simpleDateFormat.parse(strUtcTime);
                if (chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();
                MessageDbConstruct dbStruct = new MessageDbConstruct();
                dbStruct.strMsgTime = strUtcTime;
                dbStruct.msgContent = jsonObject.toString();
                dbStruct.boardId = boardId;
                dbStruct.msgId = msg.getMsgId();
                dbStruct.mediaFilePath = "";
                chatTableModel.add(dbStruct);
                messages.add(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (messages != null && messages.size() > 0) {
            Date date1 = getFirstMessageTime(messages);
            Date date2 = getLastMessageTime(messages);
            synchronized (syncMessagesObj) {

                if (earliestMsgSentTime == null && date1 != null) {
                    earliestMsgSentTime = date1;
                } else if (date1 != null && earliestMsgSentTime != null && earliestMsgSentTime.getTime() > date1.getTime()) {
                    earliestMsgSentTime = date1;
                } else if (earliestMsgSentTime != null && date1 != null && earliestMsgSentTime.getTime() == date1.getTime()) {
                    isNoMorePrevMessage = true;
                }

                if (latestMsgSentTime == null && date2 != null) {
                    latestMsgSentTime = date2;
                } else if (date2 != null && latestMsgSentTime != null && latestMsgSentTime.getTime() < date2.getTime()) {
                    latestMsgSentTime = date2;
                } else if (latestMsgSentTime != null && date2 != null && latestMsgSentTime.getTime() == date2.getTime()) {
                    noMoreMessage = true;
                }
            }
        }
        return messages;
    }

    private void storeListMessages(List<ImMessageVO> messageItems) {
        if (simpleDateFormat == null)
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < messageItems.size(); i++) {
            ImMessageVO msg = messageItems.get(i);
            String strSendTime = MyDataUtils.format(msg.getSendTime());

            try {
                msg.utcSendTime = simpleDateFormat.parse(strSendTime);
                synchronized (syncMessagesObj) {
                    if (earliestMsgSentTime == null || latestMsgSentTime == null) {
                        if (earliestMsgSentTime == null)
                            earliestMsgSentTime = msg.utcSendTime;
                        if (latestMsgSentTime == null)
                            latestMsgSentTime = msg.utcSendTime;
                    } else {
                        if (latestMsgSentTime.getTime() < msg.utcSendTime.getTime())
                            latestMsgSentTime = msg.utcSendTime;
                    }
                }

                if (chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();

                MessageDbConstruct dbStruct = new MessageDbConstruct(boardId, msg, strSendTime);
                chatTableModel.add(dbStruct);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void scrollToLastPosition(int delayTimeMills) {
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

    private synchronized void fetchPrevHistory() {
        if (adapter == null || pullToRefreshView.getAdapter() == null) {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        if (board == null || boardId <= 0) {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        //if there isn't any stored message or messages are not loaded yet
        if (earliestMsgSentTime == null) {
            pullToRefreshView.onRefreshComplete();
            return;
        }
        if (chatTableModel == null)
            chatTableModel = MyApp.getInstance().getChatDBModel();

        if (isFetchingPrevMessage) {
            return;
        }
        if (isNoMorePrevMessage) {
            pullToRefreshView.onRefreshComplete();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                final String strEarliestMsgSentTime = MyDataUtils.format(earliestMsgSentTime);
                isFetchingPrevMessage = true;
                synchronized (syncMessagesObj) {

                    Integer fetchNumber = 100;

                    final List<ImMessageVO> messages = chatTableModel.getLatestChatsByTime(boardId, strEarliestMsgSentTime, true, fetchNumber);
                    boolean isNoStoredPrevMessage = false;
                    if (messages.size() > 0) {
                        Date date1 = getFirstMessageTime(messages);
                        Date date2 = getLastMessageTime(messages);
                        synchronized (syncMessagesObj) {

                            if (earliestMsgSentTime == null && date1 != null) {
                                earliestMsgSentTime = date1;
                            } else if (date1 != null && earliestMsgSentTime != null && earliestMsgSentTime.getTime() > date1.getTime()) {
                                earliestMsgSentTime = date1;
                            } else if (earliestMsgSentTime != null && date1 != null && earliestMsgSentTime.getTime() == date1.getTime()) {
                                isNoStoredPrevMessage = true;
                            }

                            if (latestMsgSentTime == null && date2 != null) {
                                latestMsgSentTime = date2;
                            }
                        }
                    }
                    if (isNoMorePrevMessage) {
                        pullToRefreshView.onRefreshComplete();
                        return;
                    }
                    if (messages.size() > 0) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapter.addMessageItemsToTop(messages);
                                adapter.updateListView();

                                isFetchingPrevMessage = false;

                                pullToRefreshView.onRefreshComplete();
                            }
                        }, 100);
                    }
                    //if prev message is not stored in the database
                    if (!isNoStoredPrevMessage || messages.size() < fetchNumber) {
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
                                                ((GroupVideoChatActivity)mContext).runOnUiThread(new Runnable() {
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

    private synchronized void syncLatestMessages() {
        if (adapter == null || pullToRefreshView.getAdapter() == null) {
            return;
        }
        if (board == null || boardId <= 0) {
            return;
        }
        //if there isn't any stored message or messages are not loaded yet
        if (latestMsgSentTime == null) {
            return;
        }
        if (chatTableModel == null)
            chatTableModel = MyApp.getInstance().getChatDBModel();

        if (isFetchingNewMessage) {
            return;
        }
        if (noMoreMessage) {
            return;
        }

        //new SyncLatestMsgThread(boardId).start();
        SyncLatestMsgThread sycLastestMsg = new SyncLatestMsgThread(boardId);
        sycLastestMsg.start();


    }

    private synchronized void fetchHistory(String strEalierThan, String strLaterThan) {

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

                        ((GroupVideoChatActivity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pullToRefreshView.onRefreshComplete();
                                isFetchingNewMessage = false;
                            }
                        });

                    }
                }, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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


            //get emoticons
            case R.id.imgBtnEmoticon:
                showEmoticonView();
                updateEmoticonTypeButtons();
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
                        hideKeyboard();

                        DialogInterface.OnClickListener newListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                edtMessage.setFocusableInTouchMode(true);
                                edtMessage.requestFocus();
                                showKeyboard();
                            }
                        };

                        MyApp.getInstance().showSimpleAlertDiloag(mContext, strAlert, newListener);
                    }
                }
                break;


        }
    }

    private class SyncLatestMsgThread extends Thread {
        int syncBoardId = 0;

        public SyncLatestMsgThread(int boardId) {
            syncBoardId = boardId;
        }

        @Override
        public void run() {
            isFetchingNewMessage = true;
            final String strLatestMsgSentTime = MyDataUtils.format(latestMsgSentTime);
            ;
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

                                        if (messages.size() == 0 || messages.size() < number) {

                                            noMoreMessage = true;
                                            isFetchingNewMessage = false;
                                        }

                                        synchronized (syncMessagesObj) {
                                            adapter.addMessageItemsToTop(messages);
                                            adapter.updateListView();
                                        }
                                        callReadMessage();//ggg

                                        scrollToLastPosition(150);

                                        if (messages.size() >= number) {
                                            isFetchingNewMessage = false;
                                            if (!noMoreMessage) {
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

    public interface onCloseChatListener
    {
        public void onCloseChatUpdated();
    }

    public interface onReceiveMsgListener
    {
        public void onMessageUpdated(ImMessageVO msg);
    }
}
