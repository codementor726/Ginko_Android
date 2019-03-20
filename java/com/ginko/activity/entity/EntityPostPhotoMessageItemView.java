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
import com.ginko.common.MyDataUtils;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.MultimediaMessageVO;

import java.io.File;

public class EntityPostPhotoMessageItemView extends MessageItemView {
    private LinearLayout rootLayout;
    private RelativeLayout messageContentLayout;
    private TextView txtSendTime, txtEntityName;
    private NetworkImageView imgEntityPhoto;
    private CustomNetworkImageView imgPhotoView;

    private ImageLoader imgLoader;

    private EntityMessageVO msgItem;

    private ImageView btnViewOnlyOne;

    public EntityPostPhotoMessageItemView(Context context) {
        super(context);
    }

    public EntityPostPhotoMessageItemView(Context context, EntityMessageVO messageItem) {
        super(context, messageItem);
        inflater.inflate(R.layout.entity_view_message_item_photo, this, true);

        this.msgItem = messageItem;

        txtSendTime = (TextView) findViewById(R.id.txtSendTime);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        messageContentLayout = (RelativeLayout) findViewById(R.id.messageContentLayout);

        txtSendTime = (TextView) findViewById(R.id.txtTime);
        txtEntityName = (TextView) findViewById(R.id.txtEntityName);

        imgEntityPhoto = (NetworkImageView) findViewById(R.id.imgEntityPhoto);
        imgEntityPhoto.setDefaultImageResId(R.drawable.entity_dummy);

        btnViewOnlyOne = (ImageView) findViewById(R.id.viewOnlyOne);

        imgPhotoView = (CustomNetworkImageView) findViewById(R.id.imgPhotoView);
        imgPhotoView.setDefaultImageResId(R.drawable.im_image);
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

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();


        msgItem = (EntityMessageVO)getMessageItem();

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
        imgEntityPhoto.invalidate();
        txtEntityName.setText(msgItem.strEntityName);

        if(msgItem.getFile().equals("") == false)
        {
            File file = new File(msgItem.getFile());
            if(file.exists())
                imgPhotoView.setImageUrl("file://"+msgItem.getFile() , imgLoader);
            else
                imgPhotoView.setImageUrl(msgItem.getFileUrl(), imgLoader);
        }
        else
        {
            imgPhotoView.setImageUrl(msgItem.getFileUrl() , imgLoader);
        }
        txtSendTime.setText(MyDataUtils.amPmFormat(msgItem.getSendTime()));

    }
}
