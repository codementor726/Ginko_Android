package com.ginko.customview;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.UserProfileVO;

import java.util.ArrayList;

public class EntityProfilePreviewFieldGroupView extends LinearLayout{
    private Context mContext;
    private ArrayList<EntityInfoDetailVO> groupItems;
    private String strGroupName = "";

    private TextView txtFieldsGroupName;
    private LinearLayout fieldItemsLayout;

    private ArrayList<EntityProfilePreviewFieldItemView> itemViewList;

    public EntityProfilePreviewFieldGroupView(ArrayList<EntityInfoDetailVO> groupItems, String groupName, Context context)
    {
        this(context);
        this.groupItems = groupItems;
        this.strGroupName = groupName;

        init(context);
    }


    public EntityProfilePreviewFieldGroupView(Context context) {
        this(context, null);
    }

    public EntityProfilePreviewFieldGroupView(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public EntityProfilePreviewFieldGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.personal_profile_preview_field_group_item, this);

        txtFieldsGroupName = (TextView)findViewById(R.id.txtFieldsGroupName);
        fieldItemsLayout = (LinearLayout)findViewById(R.id.fieldItemsLayout);

        txtFieldsGroupName.setVisibility(View.GONE);//for temporary

        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EntityProfilePreviewFieldGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context)
    {
        if(this.groupItems == null) return;
        fieldItemsLayout.removeAllViews();

        itemViewList = new ArrayList<EntityProfilePreviewFieldItemView>();

        if(strGroupName.toLowerCase().contains("hours"))
        {
            txtFieldsGroupName.setText("HOURS");
        }
        else if(strGroupName.toLowerCase().contains("mobile") || strGroupName.toLowerCase().contains("phone"))
        {
            txtFieldsGroupName.setText("PHONES");
        }
        else if(strGroupName.toLowerCase().contains("email"))
        {
            txtFieldsGroupName.setText("E-MAILS");
        }
        else if(strGroupName.toLowerCase().contains("address"))
        {
            txtFieldsGroupName.setText("ADDRESS");
        }
        else if(strGroupName.toLowerCase().contains("fax"))
        {
            txtFieldsGroupName.setText("FAX");
        }
        else if(strGroupName.toLowerCase().contains("facebook") || strGroupName.toLowerCase().contains("twitter") || strGroupName.toLowerCase().contains("linkedin"))
        {
            txtFieldsGroupName.setText("SOCIAL NETWORKS");
        }
        else if(strGroupName.toLowerCase().contains("website"))
        {
            txtFieldsGroupName.setText("WEBSITES");
        }
        else if(strGroupName.toLowerCase().contains("custom"))
        {
            txtFieldsGroupName.setText("CUSTOMS");
        }

        for(int i=0;i<this.groupItems.size();i++)
        {
            EntityProfilePreviewFieldItemView itemView = new EntityProfilePreviewFieldItemView(this.groupItems.get(i) , mContext);
            itemViewList.add(itemView);
            fieldItemsLayout.addView(itemView);
        }
    }
}
