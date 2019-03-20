package com.ginko.activity.exchange;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ExchangeItemView extends LinearLayout {
    private LayoutInflater inflater = null;
    private ExchangeItem item;
    private NetworkImageView profileImage;
    private TextView contactName;
    private TextView txtTime;
    private ImageView imgRequestStatus;

    private Context mContext;

    private ImageLoader imgLoader;

    public ExchangeItemView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;

    }
    public ExchangeItemView(Context context,  ExchangeItem _item)
    {
        super(context);
        this.mContext = context;
        item  = _item;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.exchange_request_listitem, this, true);

        profileImage = (NetworkImageView)findViewById(R.id.profileImage);
        contactName = (TextView) findViewById(R.id.contactName);

        txtTime = (TextView)findViewById(R.id.txtTime);

        imgRequestStatus = (ImageView)findViewById(R.id.imgRequestStatus);
    }
    public void setItem(ExchangeItem _item)
    {
        this.item = _item;
    }
    public void refreshView()
    {
        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        if(item.contactType == 3)
        {
            profileImage.setDefaultImageResId(R.drawable.entity_dummy);
            profileImage.setImageUrl(item.photoUrl, imgLoader);
        }
        else
        {
            profileImage.setDefaultImageResId(R.drawable.no_face);
            profileImage.setImageUrl(item.photoUrl, imgLoader);
        }



        if(item.contactType == 3)
        {
            //if(item.nFollowerCount>0) {
                txtTime.setText(String.valueOf(item.nFollowerCount)+" followers");
                txtTime.setVisibility(View.VISIBLE);
            //}
            /*else
            {
                txtTime.setVisibility(View.INVISIBLE);
            }*/
        }
        else
        {
            if(item.getTime()!=null) {
                Calendar today = Calendar.getInstance();
                Calendar lastMsgTime = Calendar.getInstance();
                lastMsgTime.setTime(item.getTime());
                //if today
                if (today.get(Calendar.YEAR) == lastMsgTime.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) == lastMsgTime.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) == lastMsgTime.get(Calendar.DAY_OF_MONTH)) {
                    //SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                    txtTime.setText(dateFormat.format(lastMsgTime.getTime()));
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                    txtTime.setText(dateFormat.format(lastMsgTime.getTime()));
                }
                txtTime.setVisibility(View.VISIBLE);
            }
            else
            {
                txtTime.setVisibility(View.INVISIBLE);
            }
        }

        if(item.isInviteExchange())
        {
            contactName.setText(item.email);
        }
        else {
            if (item.contactName.equals(""))
                contactName.setText(item.email);
            else
                contactName.setText(item.contactName);
        }
        if(item.isPendingExchange())
            imgRequestStatus.setImageResource(R.drawable.time_normal);
        else if(item.isInviteExchange())
        {
            imgRequestStatus.setImageResource(R.drawable.contact_exchanged);
        }
        else if(item.isRequestsExchange())
        {
            if(item.contactType == 3)//entity
            {
                imgRequestStatus.setImageResource(R.drawable.leaf_line);
            }
            else
                imgRequestStatus.setImageResource(R.drawable.qsymbolactive);
        }
    }
}
