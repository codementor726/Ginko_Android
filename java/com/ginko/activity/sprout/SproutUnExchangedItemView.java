package com.ginko.activity.sprout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

public class SproutUnExchangedItemView extends SproutListItemView {
    private ImageView imageSelectionCheck;
    private RelativeLayout contactInfoLayout , entityInfoLayout;
    private NetworkImageView imageView;
    private TextView txtEntityName , txtFollowerCount , txtContactName , txtTime , txtAddress;
    private ImageView statusIcon, imgLeaf;

    private boolean isListSelectable = false;
    private ImageLoader imgLoader;

    public void setIsListSelectable(boolean isListSelectable)
    {
        this.isListSelectable = isListSelectable;
    }

    public SproutUnExchangedItemView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub

    }
    public SproutUnExchangedItemView(Context context,  SproutSearchItem _item)
    {
        super(context , _item);

        inflater.inflate(R.layout.list_item_sprout_unexchanged, this, true);

        imageSelectionCheck = (ImageView)findViewById(R.id.imageSelectionCheck);
        contactInfoLayout = (RelativeLayout)findViewById(R.id.contactInfoLayout);
        entityInfoLayout = (RelativeLayout)findViewById(R.id.entityInfoLayout);
        imageView = (NetworkImageView)findViewById(R.id.photo);
        txtEntityName = (TextView)findViewById(R.id.txtEntityName);
        txtFollowerCount = (TextView)findViewById(R.id.txtFollowerCount);
        txtContactName = (TextView)findViewById(R.id.txtContactName);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtAddress = (TextView)findViewById(R.id.txtAddress);
        statusIcon = (ImageView)findViewById(R.id.imgStatusIcon);
        imgLeaf = (ImageView)findViewById(R.id.imgLeaf);
    }


    @Override
    public void refreshView()
    {
        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(item.contactType == 3)//entity
        {
            entityInfoLayout.setVisibility(View.VISIBLE);
            if(item.isFollowed)
                imgLeaf.setImageResource(R.drawable.leaf_solid);
            else
                imgLeaf.setImageResource(R.drawable.leaf_line);

            contactInfoLayout.setVisibility(View.GONE);
            imageView.setDefaultImageResId(R.drawable.entity_dummy);
            imageView.setImageUrl(item.profile_image , imgLoader);

            txtEntityName.setText(item.entityOrContactName);
            txtFollowerCount.setText(String.valueOf(item.nEnityFollowerCount) + " followers");
        }
        else //unexchanged contact
        {
            entityInfoLayout.setVisibility(View.GONE);
            contactInfoLayout.setVisibility(View.VISIBLE);
            imageView.setDefaultImageResId(R.drawable.no_face);
            imageView.setImageUrl(item.profile_image, imgLoader);

            txtContactName.setText(item.entityOrContactName);
            txtTime.setText(item.foundTime);
            txtAddress.setText(item.strAddress);

            if (isListSelectable)
                imageSelectionCheck.setVisibility(View.VISIBLE);
            else
                imageSelectionCheck.setVisibility(View.GONE);

            if(item.isPending)
                statusIcon.setImageResource(R.drawable.time_normal);
            else
                statusIcon.setImageResource(R.drawable.qsymboldeactive);
        }
    }
}
