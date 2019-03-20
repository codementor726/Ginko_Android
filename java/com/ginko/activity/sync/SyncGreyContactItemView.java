package com.ginko.activity.sync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.context.ConstValues;
import com.ginko.ginko.R;
import com.ginko.vo.UserProfileVO;

public class SyncGreyContactItemView extends LinearLayout{

    private SyncGreyContactItem item;
    private Context mContext;
    private LayoutInflater inflater;

    private TextView txtGreyContactName;
    private ImageView imgTypeEntity , imgTypeWork , imgTypeHome;

    public SyncGreyContactItemView(Context context) {
        super(context);
        this.mContext = context;
    }

    public SyncGreyContactItemView(Context context, SyncGreyContactItem _item ) {
        super(context);
        this.mContext = context;
        this.item = _item;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_item_import_contact, this, true);

        txtGreyContactName = (TextView)findViewById(R.id.txtGreyContactName);

        imgTypeEntity = (ImageView)findViewById(R.id.imgEntity);
        imgTypeWork = (ImageView)findViewById(R.id.imgWork);
        imgTypeHome = (ImageView)findViewById(R.id.imgHome);


        refreshView();
    }

    public void setItem(SyncGreyContactItem _item)
    {
        this.item = _item;
    }

    public void refreshView()
    {
        if(item.getGreyContactName().trim().equals(""))
        {
            txtGreyContactName.setText(item.getGreyContactEmail());
        }
        else
            txtGreyContactName.setText(item.getGreyContactName());

        switch(item.getGreyContactType())
        {
            case ConstValues.GREY_TYPE_NONE:
                imgTypeEntity.setImageResource(R.drawable.btnentity);
                imgTypeWork.setImageResource(R.drawable.btnwork);
                imgTypeHome.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_ENTITY:
                imgTypeEntity.setImageResource(R.drawable.btnentityup);
                imgTypeWork.setImageResource(R.drawable.btnwork);
                imgTypeHome.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_WORK:
                imgTypeEntity.setImageResource(R.drawable.btnentity);
                imgTypeWork.setImageResource(R.drawable.btnworkup);
                imgTypeHome.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_HOME:
                imgTypeEntity.setImageResource(R.drawable.btnentity);
                imgTypeWork.setImageResource(R.drawable.btnwork);
                imgTypeHome.setImageResource(R.drawable.btnhomeup);
                break;
        }
    }
}
