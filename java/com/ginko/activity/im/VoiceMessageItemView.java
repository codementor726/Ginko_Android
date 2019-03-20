package com.ginko.activity.im;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.common.MyDataUtils;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImContactVO;
import com.ginko.vo.ImMessageVO;

import java.util.HashMap;


/**
 * Created by lee on 4/21/2015.
 */
public class VoiceMessageItemView extends MessageItemView
                                    implements VoiceMessagePlayer.VoiceMessagePlayerCallback
{
    private LinearLayout rootLayout , contactInfoLayout;
    private RelativeLayout messageLayout , messageContentLayout , voicePlayerLayout;
    private ImageView imgSelectionCheck;
    private TextView txtSendTime ,contactUserName;
    private NetworkImageView imgContactPhoto;
    private ImageView btnPlayStop;
    private VoiceMessageProgressView progressView;
    private ProgressBar pendingProgress;

    private ImageLoader imgLoader;
    private boolean isPlaying = false;

    private ImMessageVO msgItem;

    public VoiceMessageItemView(Context context) {
        super(context);

    }

    public VoiceMessageItemView(Context context, ImBoardVO boardInfo , ImMessageVO messageItem, HashMap<Integer, ImBoardMemeberVO> entireMemberList) {
        super(context, boardInfo ,messageItem, entireMemberList);

        this.msgItem = messageItem;

        inflater.inflate(R.layout.chat_voice_message_item, this, true);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        contactInfoLayout = (LinearLayout)findViewById(R.id.contactInfoLayout);

        messageLayout = (RelativeLayout)findViewById(R.id.messageLayout);
        messageContentLayout = (RelativeLayout)findViewById(R.id.messageContentLayout);
        voicePlayerLayout = (RelativeLayout)findViewById(R.id.voicePlayerLayout);

        imgSelectionCheck = (ImageView)findViewById(R.id.imgSelectionCheck);

        txtSendTime = (TextView)findViewById(R.id.txtSendTime);
        contactUserName = (TextView)findViewById(R.id.contactUserName);
        imgContactPhoto = (NetworkImageView)findViewById(R.id.imgContactPhoto);
        imgContactPhoto.setDefaultImageResId(R.drawable.img_placeholder);

        btnPlayStop = (ImageView)findViewById(R.id.imgBtnPlayStop);

        progressView = (VoiceMessageProgressView)findViewById(R.id.progressBar);

        pendingProgress = (ProgressBar)findViewById(R.id.pendingProgress);

    }


    @Override
    public void getUIObjects(boolean isSelectable)
    {
        Resources res = mContext.getResources();
        this.msgItem = (ImMessageVO) getMessageItem();

        if(isCommingMsg) //received message from others , show at leftside
        {
            messageContentLayout.setPadding(res.getDimensionPixelSize(R.dimen.chat_checkbox_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_limit_left_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding));

            RelativeLayout.LayoutParams voicePlayerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT);
            voicePlayerLayoutParams.addRule(RelativeLayout.RIGHT_OF , contactInfoLayout.getId());
            voicePlayerLayoutParams.setMargins(res.getDimensionPixelSize(R.dimen.chat_message_content_left_margin) , 0 , 0 , 0);
            voicePlayerLayout.setLayoutParams(voicePlayerLayoutParams);

            if(msgItem.isUserPhotoShowable()) {
                contactInfoLayout.setVisibility(View.VISIBLE);
                contactUserName.setVisibility(View.VISIBLE);
            }
            else {
                contactInfoLayout.setVisibility(View.VISIBLE);
                contactUserName.setVisibility(View.VISIBLE);
            }

            pendingProgress.setVisibility(View.INVISIBLE);
        }
        else//sent message , show at rightside
        {
            messageContentLayout.setPadding(res.getDimensionPixelSize(R.dimen.chat_message_item_limit_left_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding) ,
                    res.getDimensionPixelSize(R.dimen.chat_checkbox_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding));

            RelativeLayout.LayoutParams voicePlayerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT);
            voicePlayerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.TRUE);
            voicePlayerLayout.setLayoutParams(voicePlayerLayoutParams);

            contactInfoLayout.setVisibility(View.GONE);

        }

        if(isSelectable)
        {
            imgSelectionCheck.setVisibility(View.VISIBLE);
            if(isCommingMsg)
                messageContentLayout.setX(res.getDimensionPixelSize(R.dimen.chat_selectable_shift_to_right));
            else
                messageContentLayout.setX(0.0f);
        }
        else {
            imgSelectionCheck.setVisibility(View.GONE);
            messageContentLayout.setX(0.0f);
        }
        rootLayout.requestLayout();
    }


    @Override
    public void refreshView(boolean isSelectable)
    {
        super.refreshView(isSelectable);
        if(isSelectable) {
            if (msgItem.isSelected())
                imgSelectionCheck.setImageResource(R.drawable.chatmessage_selected);
            else
                imgSelectionCheck.setImageResource(R.drawable.chatmessage_nonsel);
        }

        if(isCommingMsg) //received message from others
        {
            voicePlayerLayout.setBackgroundResource(R.drawable.chat_received_message_bg);

            //if(msgItem.isUserPhotoShowable()) {
                if (imgLoader == null) {
                    imgLoader = MyApp.getInstance().getImageLoader();
                }

                imgContactPhoto.setImageUrl(getUserPhoto(msgItem.getFrom()), imgLoader);
                contactUserName.setText(getContactShortName(msgItem.getFrom()));
            //}
        }
        else//sent message
        {
            voicePlayerLayout.setBackgroundResource(R.drawable.chat_sent_message_bg);

        }

        if(msgItem.isTimeShowable())
        {
            txtSendTime.setVisibility(View.VISIBLE);
            txtSendTime.setText(MyDataUtils.chatTimeFormat(msgItem.getSendTime()));
        }
        else
        {
            txtSendTime.setVisibility(View.GONE);
        }

        if(msgItem.isPending && !isCommingMsg)
        {
            pendingProgress.setVisibility(View.VISIBLE);
        }
        else
            pendingProgress.setVisibility(View.INVISIBLE);

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
    }

    @Override
    public void onStop() {
        isPlaying = false;
        refreshPlayer();
    }

    @Override
    public void onProgress(int progressTime) {
        progressView.setProgressValue(progressTime);
        progressView.invalidate();
    }

    @Override
    public void onLoad(int timeDuaration) {
        progressView.setMaximumProgress(timeDuaration);
        progressView.invalidate();
    }

    @Override
    public void onLoadFailed() {
        isPlaying = false;
        refreshPlayer();

    }

    @Override
    public void onCompletion() {
        isPlaying = false;
        progressView.setProgressValue(0);
        progressView.invalidate();
        refreshPlayer();

    }
}
