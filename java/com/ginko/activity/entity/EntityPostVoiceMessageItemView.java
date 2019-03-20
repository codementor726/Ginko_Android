package com.ginko.activity.entity;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.MessageItemView;
import com.ginko.activity.im.VoiceMessagePlayer;
import com.ginko.activity.im.VoiceMessageProgressView;
import com.ginko.common.MyDataUtils;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.MultimediaMessageVO;

public class EntityPostVoiceMessageItemView extends MessageItemView
                                    implements VoiceMessagePlayer.VoiceMessagePlayerCallback
{
    private LinearLayout rootLayout;
    private RelativeLayout messageContentLayout;
    private TextView txtSendTime ,txtEntityName;
    private NetworkImageView imgEntityPhoto;
    private ImageView btnPlayStop, btnViewOnlyOne;
    private VoiceMessageProgressView progressView;

    private ImageLoader imgLoader;
    private boolean isPlaying = false;

    private EntityMessageVO msgItem;

    public EntityPostVoiceMessageItemView(Context context) {
        super(context);

    }

    public EntityPostVoiceMessageItemView(Context context, EntityMessageVO messageItem) {
        super(context, messageItem);
        inflater.inflate(R.layout.entity_view_message_item_voice, this, true);

        this.msgItem = messageItem;

        txtSendTime = (TextView)findViewById(R.id.txtSendTime);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        messageContentLayout = (RelativeLayout)findViewById(R.id.messageContentLayout);

        txtSendTime = (TextView)findViewById(R.id.txtTime);
        txtEntityName = (TextView)findViewById(R.id.txtEntityName);

        imgEntityPhoto = (NetworkImageView)findViewById(R.id.imgEntityPhoto);
        imgEntityPhoto.setDefaultImageResId(R.drawable.entity_dummy);

        btnPlayStop = (ImageView)findViewById(R.id.imgBtnPlayStop);
        btnViewOnlyOne = (ImageView)findViewById(R.id.viewOnlyOne);

        progressView = (VoiceMessageProgressView)findViewById(R.id.progressBar);
    }

    @Override
    public void setMessageItem(MultimediaMessageVO messageItem)
    {
        super.setMessageItem(messageItem);
        this.msgItem = (EntityMessageVO) getMessageItem();
    }

    @Override
    public void getUIObjects(boolean isSelectable)
    {
        Resources res = mContext.getResources();

        this.msgItem = (EntityMessageVO) getMessageItem();

        messageContentLayout.setPadding(res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding),
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding),
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding),
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding));

        RelativeLayout.LayoutParams messageLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT);
        messageLayoutParam.setMargins(res.getDimensionPixelSize(R.dimen.entity_view_message_content_left_right_margin) ,
                0 ,
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_left_right_margin) ,
                0);
        messageLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT );
        messageLayoutParam.addRule(RelativeLayout.BELOW , R.id.imgEntityPhoto);
        messageContentLayout.setLayoutParams(messageLayoutParam);

        rootLayout.requestLayout();
    }


    @Override
    public void refreshView(boolean isSelectable)
    {
        super.refreshView(isSelectable);
        if (imgLoader == null) {
            imgLoader = MyApp.getInstance().getImageLoader();
        }

        messageContentLayout.setBackgroundResource(R.drawable.entity_post_message_item_bg);

        imgEntityPhoto.setImageUrl(msgItem.strProfilePhoto, imgLoader);
        txtEntityName.setText(msgItem.strEntityName);

        txtSendTime.setText(MyDataUtils.amPmFormat(msgItem.getSendTime()));

        refreshPlayer();
    }

    public void refreshPlayer()
    {
        if(isPlaying)
            btnPlayStop.setImageResource(R.drawable.chat_voice_message_stop);
        else
            btnPlayStop.setImageResource(R.drawable.chat_voice_message_play);

        progressView.invalidate();
    }


    @Override
    public void onStart() {

        isPlaying = true;
        refreshPlayer();
        System.out.println("----onStart()-----");
    }

    @Override
    public void onStop() {
        isPlaying = false;
        refreshPlayer();
        System.out.println("----onStop()-----");
    }

    @Override
    public void onProgress(int progressTime) {
        progressView.setProgressValue(progressTime);
        progressView.invalidate();
        System.out.println("----onProgress("+String.valueOf(progressTime)+")-----");

    }

    @Override
    public void onLoad(int timeDuaration) {
        progressView.setMaximumProgress(timeDuaration);
        progressView.invalidate();
        System.out.println("----onLoad("+String.valueOf(timeDuaration)+")-----");
    }

    @Override
    public void onLoadFailed() {
        isPlaying = false;
        refreshPlayer();
        System.out.println("----onLoadFailed()-----");
    }

    @Override
    public void onCompletion() {
        isPlaying = false;
        progressView.setProgressValue(0);
        progressView.invalidate();
        refreshPlayer();
        System.out.println("----onCompletion()-----");
    }
}
