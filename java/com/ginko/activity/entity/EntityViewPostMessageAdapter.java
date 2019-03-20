package com.ginko.activity.entity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ginko.activity.im.ImDownloadManager;
import com.ginko.activity.im.ImPreActivity;
import com.ginko.activity.im.LocationMapViewerActivity;
import com.ginko.activity.im.MessageItemView;
import com.ginko.activity.im.VoiceMessagePlayer;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.FileUtils;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.UserEntityProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EntityViewPostMessageAdapter extends BaseAdapter implements ImDownloadManager.ImDownloadListener {

    private Context mContext;
    private int entityID = -1;
    private boolean isFollowingEntity = false;

    private ArrayList<EntityMessageVO> messageListItems;

    private List<Long> messageIDList;

    private HashMap<Long , VoiceMessagePlayer> voiceMessagePlayList;

    private boolean isSelectable = false;

    private Handler mHandler;

    private Object lockObj = new Object();

    private onDeleteMessageListener deleteMessagListener;

    private EntityMessageItemComparator msgComparator;

    private long timeInterval = 1000*60*3;

    private ImDownloadManager downloadManager;

    private Set<Integer> hiddenItems = new HashSet<Integer>();

    private UserEntityProfileVO entity;

    private boolean isVisible = false;

    private VoiceMessagePlayer currentPlayer = null;

    public class EntityMessageItemComparator implements Comparator<EntityMessageVO> {
        public EntityMessageItemComparator()
        {}
        @Override
        public int compare(EntityMessageVO left, EntityMessageVO right) {
            //an integer < 0 if lhs is less than rhs, 0 if they are equal, and > 0 if lhs is greater than rhs.
            int result = 0;
            if(left.getSendTime().getTime()<right.getSendTime().getTime())
            {
                result = 1;
            }
            else if(left.getSendTime().getTime()==right.getSendTime().getTime())
            {
                result = 0;
            }
            else if(left.getSendTime().getTime()>right.getSendTime().getTime())
            {
                result = -1;
            }
            return result;
        }
    }
    private ResponseCallBack<Void> deleteMessageCallback = new ResponseCallBack<Void>() {
        @Override
        public void onCompleted(JsonResponse<Void> response) {
            if(response.isSuccess())
            {
                int deletedMessageCount = 0;
                synchronized (lockObj)
                {
                    int k = 0;
                    while(k<messageListItems.size())
                    {
                        if(messageListItems.get(k).isSelected())
                        {
                            messageListItems.remove(k);
                            deletedMessageCount++;
                            continue;
                        }
                        k++;
                    }
                }
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

    public synchronized void deleteSelectedMessages()
    {
        int size = messageListItems.size();

        StringBuffer strBuffer = new StringBuffer("");
        for(int i=0;i<size;i++)
        {
            if(messageListItems.get(i).isSelected())
                strBuffer.append(String.valueOf(messageListItems.get(i).getMsgId()));
            if(i<size-1)
                strBuffer.append(',');
        }
        String strSelectedMsgIds = strBuffer.toString();
    }

    public void registerDownloadManager()
    {
        if(this.downloadManager == null)
            this.downloadManager = MyApp.getInstance().getDownloadManager();

        this.downloadManager.registerDownloadListener(this);

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
    public void unregisterDownloadManager(){
        if(this.downloadManager == null)
            this.downloadManager = MyApp.getInstance().getDownloadManager();

        this.downloadManager.registerDownloadListener(this);
    }

    private Activity boardActivity;

    public EntityViewPostMessageAdapter(Context context , int _entityID , boolean _isFollowingEntity)
    {
        this.mContext = context;
        boardActivity = (Activity)mContext;

        this.entityID = _entityID;
        this.isFollowingEntity = _isFollowingEntity;

        if(messageListItems == null) messageListItems = new ArrayList<EntityMessageVO>();

        messageIDList = new ArrayList<Long>();


        voiceMessagePlayList = new HashMap<Long , VoiceMessagePlayer>();

        mHandler = new Handler(mContext.getMainLooper());
    }

    public EntityViewPostMessageAdapter(Context context , int _entityID , boolean _isFollowingEntity, boolean _isVisible)
    {
        this.mContext = context;
        boardActivity = (Activity)mContext;

        this.entityID = _entityID;
        this.isFollowingEntity = _isFollowingEntity;
        this.isVisible = _isVisible;

        if(messageListItems == null) messageListItems = new ArrayList<EntityMessageVO>();

        messageIDList = new ArrayList<Long>();


        voiceMessagePlayList = new HashMap<Long , VoiceMessagePlayer>();

        mHandler = new Handler(mContext.getMainLooper());
    }

    public EntityViewPostMessageAdapter(Context context)
    {
        this.mContext = context;
        boardActivity = (Activity)mContext;

        this.entityID = -1;
        this.isFollowingEntity = true;
        this.isVisible = true;

        if(messageListItems == null) messageListItems = new ArrayList<EntityMessageVO>();

        messageIDList = new ArrayList<Long>();


        voiceMessagePlayList = new HashMap<Long , VoiceMessagePlayer>();

        mHandler = new Handler(mContext.getMainLooper());
    }

    public synchronized void clear()
    {
        if(messageListItems != null)
        {
            try {
                messageListItems.clear();
            }catch(Exception e){e.printStackTrace();}
            try
            {
                messageIDList.clear();
            }catch (Exception e){e.printStackTrace();}
        }
    }

    public void isSelectable( boolean _isSelectable)
    {
        this.isSelectable = _isSelectable;
    }


    public void setListItems(List<EntityMessageVO> items)
    {
        if(messageListItems == null) messageListItems = new ArrayList<EntityMessageVO>();

        try
        {
            messageListItems.clear();
        }catch(Exception e){e.printStackTrace();}

        int itemSize = items.size();
        for(int i=0; i<itemSize; i++)
        {
            this.messageListItems.add(items.get(i));
        }
    }

    public void showItem(int position , boolean visibility){
        //if(visibility && hiddenItems.contains(position))
        if(visibility)
        {
            this.hiddenItems.remove(position);
        }
        else {
            this.hiddenItems.add(position);
        }
    }

    public void showAllItems(){
        if(this.hiddenItems != null) {
            try {
                this.hiddenItems.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            this.hiddenItems = new HashSet<Integer>();
        }
    }

    public boolean isItemVisible(int position){return this.hiddenItems.contains(position)?false:true;}

    public void updateListView()
    {
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

    public List<EntityMessageVO> getAll()
    {
        if(messageListItems == null)
            messageListItems = new ArrayList<EntityMessageVO>();

        return messageListItems;
    }


    public synchronized void addMessageItem(EntityMessageVO message) {
        //messageItemsTemp1.add(new EntityMessageVO(message));
        //messageItemsTemp2.add(new EntityMessageVO(message));
        synchronized (lockObj) {
            //if(!checkMessageIdExist(message.getMsgId())) {
            messageListItems.add(message);
            messageIDList.add(message.getMsgId());
            if(message.getMsgType() == 2)//file message
                checkDownloadAndAddQueue(message);
            //}
        }


        sortMessage();
    }

    public synchronized void addMessageItems(List<EntityMessageVO> messages) {
        //messageItemsTemp1.add(new EntityMessageVO(message));
        //messageItemsTemp2.add(new EntityMessageVO(message));
        synchronized (lockObj) {
            for (int i = 0; i < messages.size(); i++) {
                //if(!checkMessageIdExist(messages.get(i).getMsgId())) {
                    messageListItems.add(messages.get(i));
                    messageIDList.add(messages.get(i).getMsgId());
                    if(messages.get(i).getMsgType() == 2)//file message
                        checkDownloadAndAddQueue(messages.get(i));
                //}
            }

            sortMessage();
            notifyDataSetChanged();
        }
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

                this.messageListItems.add(0 , messages.get(i));
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
                Collections.sort(messageListItems, msgComparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCount() {
        return messageListItems==null?0:messageListItems.size();
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
                    itemView = new EntityPostTextMessageItemView(mContext , item);
                    final ImageView btnViewOnlyOne = (ImageView)itemView.findViewById(R.id.viewOnlyOne);
                    final MessageItemView finalTextMsgView = itemView;

                    if(!isVisible) btnViewOnlyOne.setVisibility(View.GONE);
                    btnViewOnlyOne.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EntityMessageVO textMsg = (EntityMessageVO) finalTextMsgView.getMessageItem();
                            Intent viewEntityPost = new Intent(mContext , ViewEntityPostsActivity.class);
                            viewEntityPost.putExtra("entityName" , textMsg.strEntityName);
                            if (entityID == -1)
                                viewEntityPost.putExtra("entityId" , textMsg.entityId);
                            else
                                viewEntityPost.putExtra("entityId" , entityID);
                            viewEntityPost.putExtra("profileImage" , textMsg.strProfilePhoto);
                            viewEntityPost.putExtra("msgId", textMsg.getMsgId());
                            viewEntityPost.putExtra("isfollowing_entity" , true);
                            mContext.startActivity(viewEntityPost);
                        }
                    });
                    break;

                case EntityMessageVO.MSG_TYPE_PHOTO:
                    itemView = new EntityPostPhotoMessageItemView(mContext , item);
                    ImageView imgPhoto = (ImageView)itemView.findViewById(R.id.imgPhotoView);
                    final MessageItemView finalPhotoMsgView = itemView;
                    imgPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                            if (boardActivity != null)
                            {
                                if (boardActivity instanceof ViewEntityPostsActivity) {
                                    ((ViewEntityPostsActivity) boardActivity).shareMessageViaEmail(item);
                                } else if (boardActivity instanceof ImPreActivity) {
                                    ((ImPreActivity) boardActivity).shareMessageViaEmail(item);
                                }
                            }
                            return true;
                        }
                    });

                    final ImageView btnViewPhotoOnlyOne = (ImageView)itemView.findViewById(R.id.viewOnlyOne);
                    if(!isVisible) btnViewPhotoOnlyOne.setVisibility(View.GONE);
                    btnViewPhotoOnlyOne.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EntityMessageVO textMsg = (EntityMessageVO) finalPhotoMsgView.getMessageItem();
                            Intent viewEntityPost = new Intent(mContext, ViewEntityPostsActivity.class);
                            viewEntityPost.putExtra("entityName", textMsg.strEntityName);
                            if (entityID == -1)
                                viewEntityPost.putExtra("entityId" , textMsg.entityId);
                            else
                                viewEntityPost.putExtra("entityId" , entityID);
                            viewEntityPost.putExtra("profileImage", textMsg.strProfilePhoto);
                            viewEntityPost.putExtra("msgId", textMsg.getMsgId());
                            viewEntityPost.putExtra("isfollowing_entity", true);
                            mContext.startActivity(viewEntityPost);
                        }
                    });
                    break;

                case EntityMessageVO.MSG_TYPE_VIDEO:
                    itemView = new EntityPostVideoMessageItemView(mContext , item);
                    ImageView imgBtnPlayVideo = (ImageView)itemView.findViewById(R.id.imgBtnPlayVideo);
                    RelativeLayout videoContentLayout = (RelativeLayout)itemView.findViewById(R.id.messageContentLayout);
                    final MessageItemView finalVideoMsgView = itemView;
                    imgBtnPlayVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                    videoContentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (boardActivity != null) {
                                if (boardActivity instanceof ViewEntityPostsActivity) {
                                    ((ViewEntityPostsActivity) boardActivity).shareMessageViaEmail(item);
                                } else if (boardActivity instanceof ImPreActivity) {
                                    ((ImPreActivity) boardActivity).shareMessageViaEmail(item);
                                }
                            }
                            return true;
                        }
                    });

                    final ImageView btnViewVideoOnlyOne = (ImageView)itemView.findViewById(R.id.viewOnlyOne);
                    if(!isVisible) btnViewVideoOnlyOne.setVisibility(View.GONE);
                    btnViewVideoOnlyOne.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EntityMessageVO textMsg = (EntityMessageVO) finalVideoMsgView.getMessageItem();
                            Intent viewEntityPost = new Intent(mContext, ViewEntityPostsActivity.class);
                            viewEntityPost.putExtra("entityName", textMsg.strEntityName);
                            if (entityID == -1)
                                viewEntityPost.putExtra("entityId" , textMsg.entityId);
                            else
                                viewEntityPost.putExtra("entityId" , entityID);
                            viewEntityPost.putExtra("profileImage", textMsg.strProfilePhoto);
                            viewEntityPost.putExtra("msgId", textMsg.getMsgId());
                            viewEntityPost.putExtra("isfollowing_entity", true);
                            mContext.startActivity(viewEntityPost);
                        }
                    });
                    break;

                case EntityMessageVO.MSG_TYPE_VOICE:
                    itemView = new EntityPostVoiceMessageItemView(mContext , item);
                    ImageView imgBtnPlayStop = (ImageView)itemView.findViewById(R.id.imgBtnPlayStop);
                    RelativeLayout voiceContentLayout = (RelativeLayout)itemView.findViewById(R.id.messageContentLayout);
                    final MessageItemView finalVoiceMsgView = itemView;
                    imgBtnPlayStop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //find current voice player of this message item first
                            EntityMessageVO voiceMsg = (EntityMessageVO) finalVoiceMsgView.getMessageItem();
                            if(item.getFile().equals(""))
                            {
                                Toast.makeText(mContext , "Downloading voice message..." , Toast.LENGTH_LONG).show();
                                checkDownloadAndAddQueue(voiceMsg);
                            }
                            else {
                                VoiceMessagePlayer player = voiceMessagePlayList.get(voiceMsg.getMsgId());
                                if (player == null) {
                                    File file = new File(item.getFile());
                                    if (file.exists()) {
                                        player = new VoiceMessagePlayer(mContext, mHandler , file.getAbsolutePath());
                                        player.registerVoicePlayerCallback((EntityPostVoiceMessageItemView) finalVoiceMsgView);
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
                    });
                    voiceContentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (boardActivity != null) {
                                if (boardActivity instanceof ViewEntityPostsActivity) {
                                    ((ViewEntityPostsActivity) boardActivity).shareMessageViaEmail(item);
                                } else if (boardActivity instanceof ImPreActivity) {
                                    ((ImPreActivity) boardActivity).shareMessageViaEmail(item);
                                }
                            }
                            return true;
                        }
                    });

                    final ImageView btnViewVoiceOnlyOne = (ImageView)itemView.findViewById(R.id.viewOnlyOne);
                    if(!isVisible) btnViewVoiceOnlyOne.setVisibility(View.GONE);
                    btnViewVoiceOnlyOne.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EntityMessageVO textMsg = (EntityMessageVO) finalVoiceMsgView.getMessageItem();
                            Intent viewEntityPost = new Intent(mContext, ViewEntityPostsActivity.class);
                            viewEntityPost.putExtra("entityName", textMsg.strEntityName);
                            if (entityID == -1)
                                viewEntityPost.putExtra("entityId" , textMsg.entityId);
                            else
                                viewEntityPost.putExtra("entityId" , entityID);
                            viewEntityPost.putExtra("profileImage", textMsg.strProfilePhoto);
                            viewEntityPost.putExtra("msgId", textMsg.getMsgId());
                            viewEntityPost.putExtra("isfollowing_entity", true);
                            mContext.startActivity(viewEntityPost);
                        }
                    });
                    break;

                case EntityMessageVO.MSG_TYPE_LOCATION:
                    itemView = new EntityPostLocationMessageItemView(mContext , item);
                    ImageView imgLocation = (ImageView)itemView.findViewById(R.id.imgLocation);
                    final MessageItemView finalLocationItemView = itemView;
                    imgLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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

                    final ImageView btnViewLocationOnlyOne = (ImageView)itemView.findViewById(R.id.viewOnlyOne);
                    if(!isVisible) btnViewLocationOnlyOne.setVisibility(View.GONE);
                    btnViewLocationOnlyOne.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EntityMessageVO textMsg = (EntityMessageVO) finalLocationItemView.getMessageItem();
                            Intent viewEntityPost = new Intent(mContext, ViewEntityPostsActivity.class);
                            viewEntityPost.putExtra("entityName", textMsg.strEntityName);
                            if (entityID == -1)
                                viewEntityPost.putExtra("entityId" , textMsg.entityId);
                            else
                                viewEntityPost.putExtra("entityId" , entityID);
                            viewEntityPost.putExtra("profileImage", textMsg.strProfilePhoto);
                            viewEntityPost.putExtra("msgId", textMsg.getMsgId());
                            viewEntityPost.putExtra("isfollowing_entity", true);
                            mContext.startActivity(viewEntityPost);
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
                    player.registerVoicePlayerCallback((EntityPostVoiceMessageItemView)itemView);
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
                            player.registerVoicePlayerCallback((EntityPostVoiceMessageItemView) itemView);
                            voiceMessagePlayList.put(item.getMsgId() , player);
                        }
                    }
                }
                break;

            case EntityMessageVO.MSG_TYPE_LOCATION:
                break;
        }

        final ImageView imgEntityPhoto = (ImageView)itemView.findViewById(R.id.imgEntityPhoto);
        imgEntityPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFollowingEntity == false) return;
                if (!isVisible) return;

                stopAllRecords();
                int itemEntityID = entityID;
                if(entityID == -1)
                {
                    itemEntityID = item.entityId;
                }

                final int finalItemEntityID = itemEntityID;
                EntityRequest.viewEntity(new Integer(itemEntityID), new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            try {
                                JSONObject jsonObj = response.getData();
                                try {
                                    entity = JsonConverter.json2Object(
                                            (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                } catch (JsonConvertException e) {
                                    e.printStackTrace();
                                    entity = null;
                                }

                                if (entity.getInfos().size() > 1){
                                    Intent entityProfileIntent = new Intent(mContext, UserEntityMultiLocationsProfileActivity.class);
                                    entityProfileIntent.putExtra("entityJson", entity);
                                    entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                    entityProfileIntent.putExtra("isFavorite", jsonObj.getBoolean("is_favorite"));
                                    entityProfileIntent.putExtra("isfollowing_entity", true);
                                    //entityProfileIntent.putExtra("isNoLetter", true);
                                    mContext.startActivity(entityProfileIntent);
                                }else {
                                    Intent entityProfileIntent = new Intent(mContext, UserEntityProfileActivity.class);
                                    entityProfileIntent.putExtra("entityJson", entity);
                                    //entityProfileIntent.putExtra("isNoLetter", true);
                                    entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                    entityProfileIntent.putExtra("isFavorite", jsonObj.getBoolean("is_favorite"));
                                    entityProfileIntent.putExtra("isfollowing_entity", true);
                                    mContext.startActivity(entityProfileIntent);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JSONObject jsonObj = response.getData();
                            JSONObject jData = null;
                            try {

                                entity = JsonConverter.json2Object(
                                        (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                            }
                            catch (JsonConvertException e) {
                                e.printStackTrace();
                                entity = null;
                            }
                        }
                        else
                        {
                            if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found."))
                            {
                                MyApp.getInstance().getContactsModel().deleteContactWithContactId(finalItemEntityID);
                                MyApp.getInstance().removefromContacts(finalItemEntityID);
                            }
                        }
                    }
                }, true);

            }
        });

        if(isItemVisible(position))
        {
            itemView.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        }
        else
        {
            itemView.findViewById(R.id.rootLayout).setVisibility(View.GONE);
        }

        itemView.setMessageItem(messageListItems.get(position));
        itemView.refreshView(this.isSelectable);
        return itemView;
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
    }

}
