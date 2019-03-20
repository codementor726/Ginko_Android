package com.ginko.activity.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ChatTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.FileUtils;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImMessageVO;
import com.ginko.vo.PurpleContactWholeProfileVO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class MessageAdapter extends BaseAdapter implements ImDownloadManager.ImDownloadListener {
    private final int INT_EXTRA_PURPLE  = 1133;

    private ImBoardActivity boardActivity;
    private Context mContext;
    private ImBoardVO board;
    private ArrayList<ImMessageVO> messageListItems;

    private ArrayList<ImMessageVO> messageItemsTemp1;
    private ArrayList<ImMessageVO> messageItemsTemp2;

    private List<Long> messageIDList;

    private HashMap<Long , VoiceMessagePlayer> voiceMessagePlayList;
    private HashMap<Integer, ImBoardMemeberVO> entireMemberList;
    /*
        This is the nice knowhow of android listview.
        Android listview is updated by calling notifyByDataChanged() function.
        This function has a observable reference check function, if some data or new meomry reference is detected , that item will be redrawn
        but if we don't add or remove any item , and replace some data value , then the observer can't detect as its changed
        So we think a sly method to avoid this.
        That is just to make two item arrays of listview.
        And if we need to udpate the listview ,we just change the memory reference from tmp1 to tmp2  , tmp2 to tmp1.
        Even though these two same list array will waste some memory , this is a really nice way.
        I couldn't find another best way.
        I leave this task to anyone who will get this source.
        Hope to make a more new and efficient way
     */
    private boolean isReferenceTemp1 = true;

    private boolean isSelectable = false;

    private boolean isVoiceRecord = false;

    private Handler mHandler;

    private Object lockObj = new Object();

    private onDeleteMessageListener deleteMessagListener;

    private MessageItemComparator msgComparator;

    private long timeInterval = 1000*60*3;

    private ImDownloadManager downloadManager;
    private ChatTableModel chatTableModel;
    private VoiceMessagePlayer currentPlayer = null;
    private final int PURPLE_ACTIVITY_START = 100;

    private String msgType = "";
    private int endType = 1;

    public void setVoiceRecord(boolean _isVoice) {isVoiceRecord = _isVoice;}
    public boolean getVoiceRecord() {return isVoiceRecord;}

    private ResponseCallBack<Void> deleteMessageCallback = new ResponseCallBack<Void>() {
        @Override
        public void onCompleted(JsonResponse<Void> response) {
            if(response.isSuccess())
            {
                if(chatTableModel == null)
                    chatTableModel = MyApp.getInstance().getChatDBModel();
                int deletedMessageCount = 0;
                synchronized (lockObj)
                {
                    int k = 0;
                    while(k<messageItemsTemp1.size())
                    {
                        if(messageItemsTemp1.get(k).isSelected())
                        {
                            chatTableModel.deleteMessage(messageItemsTemp1.get(k));
                            messageItemsTemp1.remove(k);
                            messageItemsTemp2.remove(k);
                            deletedMessageCount++;
                            continue;
                        }
                        k++;
                    }
                }
                sortMessage();
                notifyDataSetChanged();
                if(deleteMessagListener!=null)
                    deleteMessagListener.onDeleteMessage(true , deletedMessageCount);
            }
            else
            {
                if(deleteMessagListener!=null)
                    deleteMessagListener.onDeleteMessage(false , 0);
            }
        }
    };

    public void setOnDeleteMessageListener(onDeleteMessageListener listener)
    {
        this.deleteMessagListener = listener;
    }

    public void stopAllRecords()
    {
        if (messageListItems.size() < 1)
            return;

        MessageItemView itemView = null;
        for (int i = 0; i < messageListItems.size(); i++)
        {
            final ImMessageVO item = messageListItems.get(i);
            int type = getItemViewType(i);
            if (type == ImMessageVO.MSG_TYPE_VOICE)
            {
                itemView = new VoiceMessageItemView(mContext , board , item, entireMemberList);
                ImageView imgBtnPlayStop = (ImageView)itemView.findViewById(R.id.imgBtnPlayStop);
                final MessageItemView finalVoiceMsgView = itemView;

                ImMessageVO voiceMsg = (ImMessageVO) finalVoiceMsgView.getMessageItem();
                VoiceMessagePlayer player = voiceMessagePlayList.get(item.getMsgId());
                if (player != null && player.isPlaying())
                    player.stop();
            }
        }

        if (currentPlayer != null)
            currentPlayer = null;
    }

    public synchronized void deleteSelectedMessages()
    {
        int size = messageItemsTemp1.size();

        StringBuffer strBuffer = new StringBuffer("");
        for(int i=0;i<size;i++)
        {
            if(messageItemsTemp1.get(i).isSelected() && messageItemsTemp1.get(i).getMsgId()>0)
                strBuffer.append(String.valueOf(messageItemsTemp1.get(i).getMsgId()));
            if(i<size-1)
                strBuffer.append(',');
        }
        String strSelectedMsgIds = strBuffer.toString();
        IMRequest.deleteMessages(board.getBoardId() , strSelectedMsgIds , deleteMessageCallback , false);
    }

    public synchronized boolean hasSelectedMessageCount()
    {
        int size = messageItemsTemp1.size();

        boolean isSelected = false;
        for(int i=0;i<size;i++)
        {
            if(messageItemsTemp1.get(i).isSelected()) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }


    public void registerDownloadManager()
    {
        if(this.downloadManager == null)
            this.downloadManager = MyApp.getInstance().getDownloadManager();

        this.downloadManager.registerDownloadListener(this);

    }

    public void unregisterDownloadManager(){
        if(this.downloadManager == null)
            this.downloadManager = MyApp.getInstance().getDownloadManager();

        this.downloadManager.registerDownloadListener(this);
    }


    public MessageAdapter(Context context , ImBoardVO boardInfo)
    {
        mContext = context;
        boardActivity = (ImBoardActivity)context;
        this.board = boardInfo;
        if(messageItemsTemp1 == null) messageItemsTemp1 = new ArrayList<ImMessageVO>();
        if(messageItemsTemp2 == null) messageItemsTemp2 = new ArrayList<ImMessageVO>();

        messageIDList = new ArrayList<Long>();

        if(isReferenceTemp1)
            messageListItems = messageItemsTemp1; //messageListItems will point the temp1 array
        else
            messageListItems = messageItemsTemp2; //messageListItems will point the temp1 array

        voiceMessagePlayList = new HashMap<Long , VoiceMessagePlayer>();
        entireMemberList = new HashMap<Integer, ImBoardMemeberVO>();

        mHandler = new Handler(mContext.getMainLooper());
    }

    public void updateBoard(ImBoardVO boardInfo)
    {
        this.board = boardInfo;
        notifyDataSetChanged();

    }

    public HashMap<Integer, ImBoardMemeberVO> getMemberInfo() { return entireMemberList; }

    public void isSelectable( boolean _isSelectable)
    {
        this.isSelectable = _isSelectable;
    }


    public List<ImMessageVO> getListItems()
    {
        if(messageItemsTemp1 == null)
            messageItemsTemp1 = new ArrayList<ImMessageVO>();
        return messageItemsTemp1;
    }

    public synchronized void setListItems(List<ImMessageVO> items)
    {
        if(messageItemsTemp1 == null) messageItemsTemp1 = new ArrayList<ImMessageVO>();
        if(messageItemsTemp2 == null) messageItemsTemp2 = new ArrayList<ImMessageVO>();

        try
        {
            messageItemsTemp1.clear();
            messageItemsTemp2.clear();
        }catch(Exception e){e.printStackTrace();}

        int itemSize = items.size();
        for(int i=0; i<itemSize; i++)
        {
            this.messageItemsTemp1.add(items.get(i));
            this.messageItemsTemp2.add(items.get(i));
        }

        if(isReferenceTemp1)
            messageListItems = messageItemsTemp1; //messageListItems will point the temp1 array
        else
            messageListItems = messageItemsTemp2; //messageListItems will point the temp1 array


    }

    public synchronized void clearAllMessages()
    {
        if(messageItemsTemp1 == null)
            messageItemsTemp1 = new ArrayList<ImMessageVO>();
        if(messageItemsTemp2 == null)
            messageItemsTemp2 = new ArrayList<ImMessageVO>();

        try
        {
            messageItemsTemp1.clear();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            messageItemsTemp2.clear();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void updateListView()
    {
        isReferenceTemp1 = !isReferenceTemp1;
        if(isReferenceTemp1)
            messageListItems = messageItemsTemp1; //messageListItems will point the temp1 array
        else
            messageListItems = messageItemsTemp2; //messageListItems will point the temp1 array

        boolean isApiNeeds = false;
        int userid = RuntimeContext.getUser().getUserId();

        List<String> memberIdList = new ArrayList<String>();

        if (board.isGroup())
        {
            for (int i=0; i<messageListItems.size(); i++)
            {
                int detectId = messageListItems.get(i).getFrom();
                String strDetectId = String.valueOf(detectId);
                if (!memberIdList.contains(strDetectId) && detectId != userid)
                    memberIdList.add(strDetectId);

                if (messageListItems.get(i).isCommingMsg() && detectId != userid && !entireMemberList.containsKey(detectId))
                    isApiNeeds = true;
            }
        }

        if (isApiNeeds)
        {
            String memberIds = TextUtils.join(", ", memberIdList);
            IMRequest.getMemberInfo(board.getBoardId(), memberIds, new ResponseCallBack<List<ImBoardMemeberVO>>() {
                @Override
                public void onCompleted(JsonResponse<List<ImBoardMemeberVO>> response) {
                    if (response.isSuccess())
                    {
                        entireMemberList.clear();
                        List<ImBoardMemeberVO> dirMemArray = response.getData();
                        for (int i=0; i< dirMemArray.size(); i++)
                            entireMemberList.put(dirMemArray.get(i).getMemberId(), dirMemArray.get(i));
                        notifyDataSetChanged();
                    }
                }
            }, false);

        } else
        {
            memberIdList = null;
            notifyDataSetChanged();
        }
    }

    private synchronized boolean checkMessageIdExist(long msgId)
    {
        if(messageIDList != null)
        {
            Iterator<Long> it = this.messageIDList.iterator();
            while(it.hasNext())
            {
                if(it.next().intValue() == msgId)
                    return true;
            }
        }

        return false;
    }

    private void checkDownloadAndAddQueue(ImMessageVO msg)
    {
        if(!msg.getFile().equals(""))
        {
            File file = new File(msg.getFile());
            if(file.exists())
            {
                return;
            }
            msg.setFile("");
        }
        if(msg.getFileUrl().equals(""))
            return;
        String filePath = RuntimeContext.getAppDataFolder("Messages")+String.valueOf(msg.getMsgId())+"_"+ FileUtils.getFileNameFromUrl(msg.getFileUrl());
        File file = new File(filePath);
        if(this.downloadManager == null)
            this.downloadManager = MyApp.getInstance().getDownloadManager();

        if(file.exists())
        {
            if(this.downloadManager.isMessageInDownloadQueue(msg))
                return;
            msg.setFile(filePath);
            return;
        }

        if(!this.downloadManager.isMessageInDownloadQueue(msg))
            this.downloadManager.addMessageToDownloadQueue(msg , filePath);
    }

    public synchronized void addMessageItem(ImMessageVO message) {
        //messageItemsTemp1.add(new ImMessageVO(message));
        //messageItemsTemp2.add(new ImMessageVO(message));
        synchronized (lockObj) {
            if(!checkMessageIdExist(message.getMsgId()) || message.getMsgId() == 0) {
                messageItemsTemp1.add(message);
                messageItemsTemp2.add(message);
                if(message.getMsgId() != 0)
                    messageIDList.add(message.getMsgId());
                if(message.getMsgType() == 2)//file message
                    checkDownloadAndAddQueue(message);
            }
        }

        sortMessage();
    }

    public synchronized void addNewMessageIDToMessageIDList(long msgId)
    {
        if(msgId != 0 && messageIDList != null)
        {
            messageIDList.add(msgId);
        }


    }

    public synchronized void addMessageItems(List<ImMessageVO> messages) {
        //messageItemsTemp1.add(new ImMessageVO(message));
        //messageItemsTemp2.add(new ImMessageVO(message));
        synchronized (lockObj) {
            for (int i = 0; i < messages.size(); i++) {
                if(!checkMessageIdExist(messages.get(i).getMsgId())) {

                    messageItemsTemp1.add(messages.get(i));
                    messageItemsTemp2.add(messages.get(i));
                    messageIDList.add(messages.get(i).getMsgId());
                    if(messages.get(i).getMsgType() == 2)//file message
                        checkDownloadAndAddQueue(messages.get(i));

                }
            }
        }

        sortMessage();
    }


    public synchronized void addMessageItemsToTop(List<ImMessageVO> messages)
    {
        synchronized (lockObj) {
            int size = messages.size();
            for(int i=0;i<size;i++) {
                if (checkMessageIdExist(messages.get(i).getMsgId()))
                {
                    continue;
                }

                this.messageItemsTemp1.add(0 , messages.get(i));
                this.messageItemsTemp2.add(0 , messages.get(i));
                this.messageIDList.add(messages.get(i).getMsgId());

                if(messages.get(i).getMsgType() == 2)
                    checkDownloadAndAddQueue(messages.get(i));
            }
        }

        sortMessage();
    }

    public synchronized List<Long> getUnreadMessageList()
    {
        List<Long> unreadMessageList = new ArrayList<Long>();
        if(RuntimeContext.getUser() == null)
            return unreadMessageList;
        int userid = RuntimeContext.getUser().getUserId();

        for(int i = 0; i<messageListItems.size(); i++)
        {
            ImMessageVO msg = messageListItems.get(i);
            boolean isWebRTC = isWebRtcCall(msg.content);
            if(!msg.isRead() && (msg.getFrom() != userid || isWebRTC == true))
            {
                unreadMessageList.add(msg.getMsgId());
            }
        }

        return unreadMessageList;
    }

    public synchronized void refreshMessageSelection()
    {
        int size = messageListItems.size();
        for(int i=0;i<size;i++)
        {
            messageListItems.get(i).setIsSelected(false);
        }
    }
    private synchronized void sortMessage()
    {
        if(msgComparator == null)
            msgComparator = new MessageItemComparator();
        synchronized (lockObj) {
            try {
                Collections.sort(messageItemsTemp1, msgComparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Collections.sort(messageItemsTemp2, msgComparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isReferenceTemp1 = !isReferenceTemp1;
        if(isReferenceTemp1)
            messageListItems = messageItemsTemp1; //messageListItems will point the temp1 array
        else
            messageListItems = messageItemsTemp2; //messageListItems will point the temp1 array

        int size = messageListItems.size();

        if(size>=1)
        {
            messageListItems.get(0).setIsTimeShowable(true);
            messageListItems.get(0).setIsUserPhotoShowable(true);

            for(int i = 1;i<size;i++)
            {
                ImMessageVO prevMsg = messageListItems.get(i-1);
                ImMessageVO currentMsg = messageListItems.get(i);
                if(prevMsg.getFrom() != currentMsg.getFrom())
                {
                    currentMsg.setIsTimeShowable(true);
                    if(currentMsg.isCommingMsg())
                        currentMsg.setIsUserPhotoShowable(true);
                    else
                        currentMsg.setIsUserPhotoShowable(false);
                }

                {
                    if(currentMsg.getSendTime().getTime()-prevMsg.getSendTime().getTime() > timeInterval)
                    {
                        currentMsg.setIsTimeShowable(true);
                        if(currentMsg.isCommingMsg())
                            currentMsg.setIsUserPhotoShowable(true);
                    }
                    else
                    {
                        currentMsg.setIsTimeShowable(false);
                    }
                }
            }
        }


    }

    @Override
    public int getCount() {
        return messageListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return messageListItems.get(position);
    }

    @Override
    public int getViewTypeCount()
    {
        return 5;
    }


    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return messageListItems.get(position).getMessageType();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void parseContent(String content)
    {
        if (content == null || !content.contains("endType"))
        {
            msgType = null;
            endType = 0;
            return;
        }

        try {
            JSONObject json = new JSONObject(content);
            msgType = json.getString("msgType");
            endType = json.getInt("endType");
        } catch (JSONException e)
        {

        }
    }

    private boolean isWebRtcCall(String content)
    {
        if (content == null || !content.contains("endType"))
        {
            msgType = null;
            endType = 0;
            return false;
        }

        return true;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MessageItemView itemView = null;
        final ImMessageVO item = messageListItems.get(position);

        int type = getItemViewType(position);
        String content = item.getContent();
        parseContent(content);
        if (msgType != null && msgType.equals("videoCall"))
            type = 5;
        else if (msgType != null && msgType.equals("audioCall"))
            type = 6;

        if(convertView == null)
        {
            switch(type)
            {
                case ImMessageVO.MSG_TYPE_TEXT:
                case ImMessageVO.MSG_TYPE_VIDEOCALL:
                case ImMessageVO.MSG_TYPE_AUDIOCALL:
                    itemView = new TextMessageItemView(mContext , board , item, entireMemberList);
                    break;
                case ImMessageVO.MSG_TYPE_PHOTO:
                    itemView = new PhotoMessageItemView(mContext , board , item, entireMemberList);
                    ImageView imgPhoto = (ImageView)itemView.findViewById(R.id.imgPhotoView);
                    final MessageItemView finalPhotoMsgView = itemView;
                    imgPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getVoiceRecord() == true) return;

                            ImMessageVO photoMsg = (ImMessageVO) finalPhotoMsgView.getMessageItem();
                            if(photoMsg.getFile().equals(""))
                            {
                                Toast.makeText(mContext , "Downloading photo file..." , Toast.LENGTH_LONG).show();
                                checkDownloadAndAddQueue(photoMsg);
                            }
                            else
                            {
                                File file = new File(photoMsg.getFile());
                                if(file.exists()) {
                                    Intent photoIntent = new Intent();
                                    photoIntent.setAction(android.content.Intent.ACTION_VIEW);
                                    photoIntent.setDataAndType(Uri.fromFile(new File(photoMsg.getFile())), "image/*");
                                    mContext.startActivity(photoIntent);
                                }
                                else
                                {
                                    //redownload the photo file
                                    checkDownloadAndAddQueue(photoMsg);
                                }
                            }
                        }
                    });

                    imgPhoto.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (getVoiceRecord() == true) return true;

                            if(boardActivity != null)
                                boardActivity.shareMessageViaEmail(item);
                            return true;
                        }
                    });
                    break;

                case ImMessageVO.MSG_TYPE_VIDEO:
                    itemView = new VideoMessageItemView(mContext , board , item, entireMemberList);
                    ImageView imgBtnPlayVideo = (ImageView)itemView.findViewById(R.id.imgBtnPlayVideo);
                    final MessageItemView finalVideoMsgView = itemView;
                    imgBtnPlayVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getVoiceRecord() == true) return;

                            ImMessageVO videoMsg = (ImMessageVO) finalVideoMsgView.getMessageItem();
                            if(videoMsg.getFile().equals(""))
                            {
                                Toast.makeText(mContext , "Downloading video file..." , Toast.LENGTH_LONG).show();
                                checkDownloadAndAddQueue(videoMsg);
                            }
                            else {
                                File file = new File(videoMsg.getFile());
                                if(file.exists()) {
                                    Intent videoPlayIntent = new Intent(Intent.ACTION_VIEW);
                                    videoPlayIntent.setDataAndType(Uri.parse("file://"+videoMsg.getFile()), "video/*");
                                    mContext.startActivity(videoPlayIntent);
                                }
                                else
                                {
                                    //redownload the video file
                                    checkDownloadAndAddQueue(videoMsg);
                                }
                            }
                        }
                    });

                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (getVoiceRecord() == true) return true;
                            if (boardActivity != null)
                                boardActivity.shareMessageViaEmail(item);
                            return true;
                        }
                    });
                    break;

                case ImMessageVO.MSG_TYPE_VOICE:
                    itemView = new VoiceMessageItemView(mContext , board , item, entireMemberList);
                    ImageView imgBtnPlayStop = (ImageView)itemView.findViewById(R.id.imgBtnPlayStop);
                    final MessageItemView finalVoiceMsgView = itemView;

                    imgBtnPlayStop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //find current voice player of this message item first
                            if (boardActivity != null)
                            {
                                boolean isVoiceRecording = boardActivity.getVoiceRecording();
                                if (isVoiceRecording)
                                    return;
                            }

                            ImMessageVO voiceMsg = (ImMessageVO) finalVoiceMsgView.getMessageItem();
                            if(item.getFile().equals(""))
                            {
                                Toast.makeText(mContext , "Downloading voice message..." , Toast.LENGTH_LONG).show();
                                checkDownloadAndAddQueue(voiceMsg);
                            }
                            else {
                                if(!voiceMsg.isPending) {
                                    VoiceMessagePlayer player = voiceMessagePlayList.get(voiceMsg.getMsgId());
                                    if (player == null) {
                                        File file = new File(item.getFile());
                                        if (file.exists()) {
                                            player = new VoiceMessagePlayer(mContext, mHandler, file.getAbsolutePath());
                                            player.registerVoicePlayerCallback((VoiceMessageItemView) finalVoiceMsgView);
                                            voiceMessagePlayList.put(item.getMsgId(), player);
                                        } else {
                                            //redownload the voice file
                                            checkDownloadAndAddQueue(voiceMsg);
                                        }
                                    } else {
                                        if (player.isPlaying()) {
                                            currentPlayer = null;
                                            player.stop();
                                        }
                                        else {
                                            stopAllRecords();
                                            if (currentPlayer != null && currentPlayer.isPlaying())
                                                currentPlayer.stop();
                                            player.start();
                                            currentPlayer = player;
                                        }
                                    }
                                }
                            }
                        }
                    });
                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (boardActivity != null)
                                boardActivity.shareMessageViaEmail(item);
                            return true;
                        }
                    });
                    break;

                case ImMessageVO.MSG_TYPE_LOCATION:
                    itemView = new LocationMessageItemView(mContext , board , item, entireMemberList);
                    ImageView imgLocation = (ImageView)itemView.findViewById(R.id.imgLocation);
                    final MessageItemView finalLocationItemView = itemView;

                    imgLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getVoiceRecord() == true) return;

                            ImMessageVO msg = (ImMessageVO) finalLocationItemView.getMessageItem();
                            try
                            {
                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=16", msg.getLattitude(), msg.getLongitude());
                                Intent goolgeMapIntent = new Intent(Intent.ACTION_VIEW , Uri.parse(uri));
                                //goolgeMapIntent.setData(geoLocation);
                                // check if Google Maps is supported on given device
                                if (goolgeMapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                    mContext.startActivity(goolgeMapIntent);
                                }
                                else
                                {
                                    Intent locationMessageIntent = new Intent(mContext , LocationMapViewerActivity.class);
                                    locationMessageIntent.putExtra("lat" , msg.getLattitude());
                                    locationMessageIntent.putExtra("long" , msg.getLongitude());
                                    mContext.startActivity(locationMessageIntent);
                                }
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                                Intent locationMessageIntent = new Intent(mContext , LocationMapViewerActivity.class);
                                locationMessageIntent.putExtra("lat" , msg.getLattitude());
                                locationMessageIntent.putExtra("long" , msg.getLongitude());
                                mContext.startActivity(locationMessageIntent);
                            }

                        }
                    });
                    break;
            }
        }
        else
        {
            itemView = (MessageItemView)convertView;
            itemView.updateBoard(board, entireMemberList);
        }

        switch(type)
        {
            case ImMessageVO.MSG_TYPE_TEXT:
                break;

            case ImMessageVO.MSG_TYPE_PHOTO:
                break;

            case ImMessageVO.MSG_TYPE_VIDEO:
                break;

            case ImMessageVO.MSG_TYPE_VOICE:
                if(voiceMessagePlayList.containsKey(item.getMsgId()))//if this voice message is registred in the play list
                {
                    VoiceMessagePlayer player = voiceMessagePlayList.get(item.getMsgId());
                    player.registerVoicePlayerCallback((VoiceMessageItemView)itemView);
                }
                else
                {
                    //add this voice message to the play list
                    if(item.getFile().equals(""))//if file is not downloaded
                    {
                        //add this voice message to the download list
                        checkDownloadAndAddQueue(item);
                    }
                    else {
                        File file = new File(item.getFile());
                        if(file.exists()) {
                            VoiceMessagePlayer player = new VoiceMessagePlayer(mContext, mHandler , file.getAbsolutePath());
                            player.registerVoicePlayerCallback((VoiceMessageItemView)itemView);
                            voiceMessagePlayList.put(item.getMsgId(), player);
                        }
                    }

                }
                break;

            case ImMessageVO.MSG_TYPE_LOCATION:
                break;
        }

        final ImageView imgSelectionCheck = (ImageView)itemView.findViewById(R.id.imgSelectionCheck);
        imgSelectionCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSlected = !messageItemsTemp1.get(position).isSelected();
                messageItemsTemp1.get(position).setIsSelected(isSlected);
                messageItemsTemp2.get(position).setIsSelected(isSlected);

                if(deleteMessagListener!=null)
                    deleteMessagListener.onBoardListItemSelected(position , hasSelectedMessageCount());

                if(isSlected)
                    imgSelectionCheck.setImageResource(R.drawable.chatmessage_selected);
                else
                    imgSelectionCheck.setImageResource(R.drawable.chatmessage_nonsel);
            }
        });

        final ImageView imgContactPhoto = (ImageView)itemView.findViewById(R.id.imgContactPhoto);
        imgContactPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getVoiceRecord() == true) return;

                String strFullName = "";
                int fromId = messageListItems.get(position).getFrom();
                if (board.isGroup()) return;

                boolean inSameDirectory = false;

                for(int i =0;i<board.getMembers().size();i++)
                {
                    if(board.getMembers().get(i).getUser().getUserId() == fromId) {
                        if (board.getMembers().get(i).isInDirectory())
                            inSameDirectory = true;
                        if (board.getMembers().get(i).isFriend())
                            strFullName = board.getMembers().get(i).getUser().getFullName();
                        break;
                    }
                }

                if (inSameDirectory)
                    return;

                final String strContactId = String.valueOf(fromId);
                final String strName = strFullName;

                final Intent purpleContactProfileIntent = new Intent(mContext, PurpleContactProfile.class);
                final Bundle bundle = new Bundle();
                bundle.putString("fullname", strName);
                bundle.putString("contactID", strContactId);
                bundle.putBoolean("isChatting", true);
                UserRequest.getContactDetail(String.valueOf(strContactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                    @Override
                    public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                        if (response.isSuccess()) {
                            PurpleContactWholeProfileVO responseData = response.getData();
                            if (responseData.getSharingStatus() == 4) {
                                MyApp.getInstance().showSimpleAlertDiloag(mContext, "Oops! Contact would like to chat only", null);
                            } else {
                                bundle.putSerializable("responseData", responseData);
                                purpleContactProfileIntent.putExtras(bundle);
                                ImBoardActivity parentActivity = (ImBoardActivity) mContext;
                                parentActivity.startActivityForResult(purpleContactProfileIntent, INT_EXTRA_PURPLE);
                            }
                        } else {
                            //String errMsg = response.getErrorMessage();
                            //MyApp.getInstance().showSimpleAlertDiloag(mContext, errMsg, null);
                            if (response.getErrorCode() == 350)//The contact can't be found.
                                //MyApp.getInstance().showSimpleAlertDiloag(mContext, errMsg, null);
                                Uitils.alert("Sorry, the selection is no longer a contact.");
                        }
                    }
                }, false);
            }
        });

        itemView.setMessageItem(messageListItems.get(position));
        itemView.refreshView(this.isSelectable);
        return itemView;
    }

    public boolean isCommingMsg(ImMessageVO msg) {
        return !RuntimeContext.isLoginUser(msg.getFrom());
    }

    @Override
    public void onImMessageFileDownloaded() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateListView();
            }
        });
    }

    public interface onDeleteMessageListener
    {
        public void onDeleteMessage(boolean isDeleted , int deletedMessageCount);
        public void onBoardListItemSelected(int selectedItemPosition , boolean selectedItemsCount);
    }
}
