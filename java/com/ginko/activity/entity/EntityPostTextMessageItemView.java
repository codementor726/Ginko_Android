package com.ginko.activity.entity;

import android.content.Context;
import android.content.res.Resources;
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
import com.ginko.vo.MultimediaMessageVO;


public class EntityPostTextMessageItemView extends MessageItemView{

    private LinearLayout rootLayout;
    private RelativeLayout messageContentLayout;
    private TextView txtSendTime ,txtEntityName , txtMessage;
    private NetworkImageView imgEntityPhoto;

    private ImageLoader imgLoader;

    private EmoticonUtility emoticons;

    private EntityMessageVO msgItem;

    private ImageView btnViewOnlyOne;

    public EntityPostTextMessageItemView(Context context) {
        super(context);
    }

    public EntityPostTextMessageItemView(Context context, EntityMessageVO messageItem) {
        super(context, messageItem);

        this.msgItem = messageItem;

        inflater.inflate(R.layout.entity_view_message_item_text, this, true);

        rootLayout = (LinearLayout)findViewById(R.id.rootLayout);
        messageContentLayout = (RelativeLayout)findViewById(R.id.messageContentLayout);

        txtSendTime = (TextView)findViewById(R.id.txtTime);
        txtEntityName = (TextView)findViewById(R.id.txtEntityName);
        txtMessage = (TextView)findViewById(R.id.txtMessage);

        imgEntityPhoto = (NetworkImageView)findViewById(R.id.imgEntityPhoto);

        btnViewOnlyOne = (ImageView)findViewById(R.id.viewOnlyOne);

        imgEntityPhoto.setDefaultImageResId(R.drawable.entity_dummy);
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

        msgItem = (EntityMessageVO) getMessageItem();

        messageContentLayout.setPadding(res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding),
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding),
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding),
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_layout_padding));

        RelativeLayout.LayoutParams messageLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT);
        messageLayoutParam.setMargins(res.getDimensionPixelSize(R.dimen.entity_view_message_content_left_right_margin) ,
                0 ,
                res.getDimensionPixelSize(R.dimen.entity_view_message_content_left_right_margin) ,
                0);
        messageLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        messageLayoutParam.addRule(RelativeLayout.BELOW , R.id.imgEntityPhoto);
        messageContentLayout.setLayoutParams(messageLayoutParam);

        rootLayout.requestLayout();
    }


    @Override
    public void refreshView(boolean isSelectable)
    {
        super.refreshView(isSelectable);

        if(this.emoticons == null)
            this.emoticons = MyApp.getInstance().getEmoticonUtility();

        if (imgLoader == null) {
            imgLoader = MyApp.getInstance().getImageLoader();
        }

        //txtMessage.setBackgroundResource(R.drawable.entity_post_message_item_bg);

        imgEntityPhoto.setImageUrl(msgItem.strProfilePhoto, imgLoader);
        txtEntityName.setText(msgItem.strEntityName);

        txtSendTime.setText(MyDataUtils.amPmFormat(msgItem.getSendTime()));

        txtMessage.setText(this.emoticons.addSmileySpans(msgItem.getContent()));
        txtMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }
}
