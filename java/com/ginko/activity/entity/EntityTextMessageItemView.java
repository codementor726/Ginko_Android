package com.ginko.activity.entity;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.EmoticonUtility;
import com.ginko.activity.im.MessageItemView;
import com.ginko.common.MyDataUtils;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImMessageVO;

public class EntityTextMessageItemView extends MessageItemView{

    private LinearLayout rootLayout , contactInfoLayout;
    private RelativeLayout messageLayout , messageContentLayout;
    private ImageView imgSelectionCheck;
    private TextView txtSendTime ,contactUserName , txtMessage;
    private NetworkImageView imgContactPhoto;

    private ImageLoader imgLoader;

    private EmoticonUtility emoticons;

    private EntityMessageVO msgItem;

    public EntityTextMessageItemView(Context context) {
        super(context);
    }

    public EntityTextMessageItemView(Context context, EntityMessageVO messageItem) {
        super(context, messageItem);

        this.msgItem = messageItem;

        inflater.inflate(R.layout.chat_text_message_item, this, true);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        contactInfoLayout = (LinearLayout)findViewById(R.id.contactInfoLayout);

        messageLayout = (RelativeLayout)findViewById(R.id.messageLayout);
        messageContentLayout = (RelativeLayout)findViewById(R.id.messageContentLayout);

        imgSelectionCheck = (ImageView)findViewById(R.id.imgSelectionCheck);

        txtSendTime = (TextView)findViewById(R.id.txtSendTime);
        contactUserName = (TextView)findViewById(R.id.contactUserName);
        txtMessage = (TextView)findViewById(R.id.txtMessage);

        imgContactPhoto = (NetworkImageView)findViewById(R.id.imgContactPhoto);
        imgContactPhoto.setDefaultImageResId(R.drawable.img_placeholder);
    }

    @Override
    public void getUIObjects(boolean isSelectable)
    {
        Resources res = mContext.getResources();

        this.msgItem = (EntityMessageVO) getMessageItem();
        isCommingMsg = false;

        if(isCommingMsg) //received message from others , show at leftside
        {
            messageContentLayout.setPadding(res.getDimensionPixelSize(R.dimen.chat_checkbox_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_limit_left_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding));

            RelativeLayout.LayoutParams txtMessageLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT);
            txtMessageLayoutParam.addRule(RelativeLayout.RIGHT_OF , contactInfoLayout.getId());
            txtMessageLayoutParam.setMargins(res.getDimensionPixelSize(R.dimen.chat_message_content_left_margin) , 0 , 0 , 0);
            txtMessage.setLayoutParams(txtMessageLayoutParam);

            if(msgItem.isUserPhotoShowable()) {
                contactInfoLayout.setVisibility(View.VISIBLE);
                contactUserName.setVisibility(View.VISIBLE);
            }
            else {
                contactInfoLayout.setVisibility(View.INVISIBLE);
                contactUserName.setVisibility(View.GONE);
            }
        }
        else//sent message , show at rightside
        {
            messageContentLayout.setPadding(res.getDimensionPixelSize(R.dimen.chat_message_item_limit_left_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding) ,
                    res.getDimensionPixelSize(R.dimen.chat_checkbox_right_margin) ,
                    res.getDimensionPixelSize(R.dimen.chat_message_item_top_bottom_padding));

            RelativeLayout.LayoutParams txtMessageLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT);
            txtMessageLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.TRUE);
            txtMessage.setLayoutParams(txtMessageLayoutParam);

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

        if(this.emoticons == null)
            this.emoticons = MyApp.getInstance().getEmoticonUtility();

        if(isSelectable) {
            if (msgItem.isSelected())
                imgSelectionCheck.setImageResource(R.drawable.chatmessage_selected);
            else
                imgSelectionCheck.setImageResource(R.drawable.chatmessage_nonsel);
        }

        isCommingMsg = false;
        if(isCommingMsg) //received message from others
        {
            txtMessage.setBackgroundResource(R.drawable.chat_received_message_bg);

            if(msgItem.isUserPhotoShowable()) {
                if (imgLoader == null) {
                    imgLoader = MyApp.getInstance().getImageLoader();
                }

                imgContactPhoto.setImageUrl(getUserPhoto(msgItem.getFrom()), imgLoader);
                contactUserName.setText(getContactShortName(msgItem.getFrom()));
            }
        }
        else//sent message
        {
            txtMessage.setBackgroundResource(R.drawable.chat_sent_message_bg);
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

        txtMessage.setText(this.emoticons.addSmileySpans(msgItem.getContent()));
        txtMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }
}
