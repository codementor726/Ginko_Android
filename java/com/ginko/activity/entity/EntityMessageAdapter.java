package com.ginko.activity.entity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.ginko.activity.im.ImDownloadManager;
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.activity.im.MessageItemView;
import com.ginko.activity.im.VoiceMessagePlayer;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.FileUtils;
import com.ginko.vo.EntityMessageExtVO;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.EntityVO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class EntityMessageAdapter extends BaseAdapter implements ImDownloadManager.ImDownloadListener {

    private Context mContext;
    private EntityVO entity;
    private ArrayList<EntityMessageVO> messageListItems;

    private ArrayList<EntityMessageVO> messageItemsTemp1;
    private ArrayList<EntityMessageVO> messageItemsTemp2;

    private List<Long> messageIDList;

    private VoiceMessagePlayer currentPlayer = null;

    private HashMap<Long , VoiceMessagePlayer> voiceMessagePlayList;
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

    private EntityMessageItemComparator msgComparator;

    private long timeInterval = 1000*60*3;

    private ImDownloadManager downloadManager;

    public void setVoiceRecord(boolean _isVoice) {isVoiceRecord = _isVoice;}
    public boolean getVoiceRecord() {return isVoiceRecord;}

    private ResponseCallBack<Void> deleteMessageCallback = new ResponseCallBack<Void>() {
        @Override
        public void onCompleted(JsonResponse<Void> response) {
            if(response.isSuccess())
            {
                int deletedMessageCount = 0;
                synchronized (lockObj)
                {
                    int k = 0;
                    while(k<messageItemsTemp1.size())
                    {
                        if(messageItemsTemp1.get(k).isSelected())
                        {
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
            final EntityMessageVO item = messageListItems.get(i);
            int type = getItemViewType(i);
            if (type == EntityMessageVO.MSG_TYPE_VOICE)
            {
                itemView = new EntityVoiceMessageItemView(mContext , item);
                ImageView imgBtnPlayStop = (ImageView)itemView.findViewById(R.id.imgBtnPlayStop);
                final MessageItemView finalVoiceMsgView = itemView;

                EntityMessageVO voiceMsg = (EntityMessageVO) finalVoiceMsgView.getMessageItem();
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
            if(messageItemsTemp1.get(i).isSelected())
                strBuffer.append(String.valueOf(messageItemsTemp1.get(i).getMsgId()));
            if(i<size-1)
                strBuffer.append(',');
        }
        String strSelectedMsgIds = strBuffer.toString();
        EntityRequest.deleteMessages(entity.getId(), strSelectedMsgIds, deleteMessageCallback, false);
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

    private EntityMessageActivity         boardActivity;


    public EntityMessageAdapter(Context context, EntityVO entityvo)
    {
        mContext = context;
        boardActivity = (EntityMessageActivity)context;
        this.entity = entityvo;
        if(messageItemsTemp1 == null) messageItemsTemp1 = new ArrayList<EntityMessageVO>();
        if(messageItemsTemp2 == null) messageItemsTemp2 = new ArrayList<EntityMessageVO>();

        messageIDList = new ArrayList<Long>();

        if(isReferenceTemp1)
            messageListItems = messageItemsTemp1; //messageListItems will point the temp1 array
        else
            messageListItems = messageItemsTemp2; //messageListItems will point the temp1 array

        voiceMessagePlayList = new HashMap<Long , VoiceMessagePlayer>();

        mHandler = new Handler(mContext.getMainLooper());
    }

    public void isSelectable( boolean _isSelectable)
    {
        this.isSelectable = _isSelectable;
    }


    public void setListItems(List<EntityMessageVO> items)
    {
        if(messageItemsTemp1 == null) messageItemsTemp1 = new ArrayList<EntityMessageVO>();
        if(messageItemsTemp2 == null) messageItemsTemp2 = new ArrayList<EntityMessageVO>();

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

    public void clearAllMessages()
    {
        if(messageItemsTemp1 == null)
            messageItemsTemp1 = new ArrayList<EntityMessageVO>();
        if(messageItemsTemp2 == null)
            messageItemsTemp2 = new ArrayList<EntityMessageVO>();

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

    public void updateListView()
    {
        isReferenceTemp1 = !isReferenceTemp1;
        if(isReferenceTemp1)
            messageListItems = messageItemsTemp1; //messageListItems will point the temp1 array
        else
            messageListItems = messageItemsTemp2; //messageListItems will point the temp1 array

        notifyDataSetChanged();
    }

    private boolean checkMessageIdExist(long msgId)
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

    private void checkDownloadAndAddQueue(EntityMessageVO msg)
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
        String filePath = RuntimeContext.getAppDataFolder("EntityMessages")+String.valueOf(msg.getMsgId())+"_"+ FileUtils.getFileNameFromUrl(msg.getFileUrl());
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

    public synchronized void addNewMessageIDToMessageIDList(long msgId)
    {
        if(msgId != 0 && messageIDList != null)
        {
            messageIDList.add(msgId);
        }
    }

    public synchronized void addMessageItem(EntityMessageVO message) {
        //messageItemsTemp1.add(new ImMessageVO(message));
        //messageItemsTemp2.add(new ImMessageVO(message));
        synchronized (lockObj) {
            if(!checkMessageIdExist(message.getMsgId()) || message.getMsgId() == 0) {
                messageItemsTemp1.add(message);
                messageItemsTemp2.add(message);
                messageIDList.add(message.getMsgId());
                if(message.getMsgType() == 2)//file message
                    checkDownloadAndAddQueue(message);
            }
        }

        sortMessage();
    }

    public synchronized void addMessageItems(List<EntityMessageVO> messages) {
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


    public synchronized void addMessageItemsToTop(List<EntityMessageVO> messages)
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
            msgComparator = new EntityMessageItemComparator();
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
                EntityMessageVO prevMsg = messageListItems.get(i-1);
                EntityMessageVO currentMsg = messageListItems.get(i);
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MessageItemView itemView = null;
        final EntityMessageVO item = messageListItems.get(position);
        int type = getItemViewType(position);
        if(convertView == null)
        {
            switch(type)
            {
                case EntityMessageVO.MSG_TYPE_TEXT:
                    itemView = new EntityTextMessageItemView(mContext , item);
                    break;

                case EntityMessageExtVO.MSG_TYPE_PHOTO:
                    itemView = new EntityPhotoMessageItemView(mContext , item);
                    ImageView imgPhoto = (ImageView)itemView.findViewById(R.id.imgPhotoView);
                    final MessageItemView finalPhotoMsgView = itemView;
                    imgPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getVoiceRecord() == true) return;

                            EntityMessageVO photoMsg = (EntityMessageVO) finalPhotoMsgView.getMessageItem();
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
                                    photoIntent.setAction(Intent.ACTION_VIEW);
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

                            if (boardActivity != null)
                                boardActivity.shareMessageViaEmail(item);
                            return true;
                        }
                    });
                    break;

                case EntityMessageVO.MSG_TYPE_VIDEO:
                    itemView = new EntityVideoMessageItemView(mContext , item);
                    ImageView imgBtnPlayVideo = (ImageView)itemView.findViewById(R.id.imgBtnPlayVideo);
                    final MessageItemView finalVideoMsgView = itemView;
                    imgBtnPlayVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getVoiceRecord() == true) return;

                            EntityMessageVO videoMsg = (EntityMessageVO) finalVideoMsgView.getMessageItem();
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

                case EntityMessageVO.MSG_TYPE_VOICE:
                    itemView = new EntityVoiceMessageItemView(mContext , item);
                    ImageView imgBtnPlayStop = (ImageView)itemView.findViewById(R.id.imgBtnPlayStop);
                    final MessageItemView finalVoiceMsgView = itemView;
                    imgBtnPlayStop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (boardActivity != null)
                            {
                                boolean isVoiceRecording = boardActivity.getVoiceRecording();
                                if (isVoiceRecording)
                                    return;
                            }
                            //find current voice player of this message item first
                            EntityMessageVO voiceMsg = (EntityMessageVO) finalVoiceMsgView.getMessageItem();
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
                                            player.registerVoicePlayerCallback((EntityVoiceMessageItemView) finalVoiceMsgView);
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

                case EntityMessageVO.MSG_TYPE_LOCATION:
                    itemView = new EntityLocationMessageItemView(mContext , item);
                    ImageView imgLocation = (ImageView)itemView.findViewById(R.id.imgLocation);
                    final MessageItemView finalLocationItemView = itemView;
                    imgLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getVoiceRecord() == true) return;

                            EntityMessageVO msg = (EntityMessageVO) finalLocationItemView.getMessageItem();
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
        }

        switch(type)
        {
            case EntityMessageVO.MSG_TYPE_TEXT:
                break;

            case EntityMessageVO.MSG_TYPE_PHOTO:
                break;

            case EntityMessageVO.MSG_TYPE_VIDEO:
                break;

            case EntityMessageVO.MSG_TYPE_VOICE:
                //if voice message , register callback for voice message player
                if(voiceMessagePlayList.containsKey(item.getMsgId()))//if this voice message is registred in the play list
                {
                    VoiceMessagePlayer player = voiceMessagePlayList.get(item.getMsgId());
                    player.registerVoicePlayerCallback((EntityVoiceMessageItemView)itemView);
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
                            player.registerVoicePlayerCallback((EntityVoiceMessageItemView) itemView);
                            voiceMessagePlayList.put(item.getMsgId() , player);
                        }
                    }
                }
                break;

            case EntityMessageVO.MSG_TYPE_LOCATION:
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

        /*final ImageView imgContactPhoto = (ImageView)itemView.findViewById(R.id.imgContactPhoto);
        imgContactPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strFullName = "";
                int fromId = messageListItems.get(position).getFrom();
                for(int i =0;i<board.getMembers().size();i++)
                {
                    if(board.getMembers().get(i).getUser().getUserId() == fromId) {
                        strFullName = board.getMembers().get(i).getUser().getFullName();
                        break;
                    }
                }

                Intent purpleContactProfileIntent = new Intent(mContext , PurpleContactProfile.class);
                purpleContactProfileIntent.putExtra("fullname" , strFullName);
                purpleContactProfileIntent.putExtra("contactID" , String.valueOf(fromId));
                mContext.startActivity(purpleContactProfileIntent);
            }
        });*/

        itemView.setMessageItem(messageListItems.get(position));
        itemView.refreshView(this.isSelectable);
        return itemView;
    }

    public boolean isCommingMsg(EntityMessageVO msg) {
        return !(msg.getFrom() == this.entity.getId());
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
        public void onDeleteMessage(boolean isDeleted, int deletedMessageCount);
        public void onBoardListItemSelected(int selectedItemPosition , boolean selectedItemsCount);
    }

}
