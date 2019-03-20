package com.ginko.customview;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.ginko.R;
import com.ginko.vo.UserProfileVO;

import java.util.ArrayList;

public class PersonalProfilePreviewFieldGroupView extends LinearLayout{
    private Context mContext;
    private ArrayList<UserProfileVO> groupItems;
    private String strGroupName = "";

    private TextView txtFieldsGroupName;
    private LinearLayout fieldItemsLayout;
    private boolean isShowMore = false;
    private int Index = 0;

    private ArrayList<PersonalProfilePreviewFieldItemView> itemViewList;

    public PersonalProfilePreviewFieldGroupView(ArrayList<UserProfileVO> groupItems , String groupName , Context context, int Index, boolean isShowMore)
    {
        this(context);
        this.mContext = context;
        this.groupItems = groupItems;
        this.strGroupName = groupName;
        this.Index = Index;
        this.isShowMore = isShowMore;

        init(context);
    }


    public PersonalProfilePreviewFieldGroupView(Context context) {
        this(context, null);
    }

    public PersonalProfilePreviewFieldGroupView(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public PersonalProfilePreviewFieldGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    public PersonalProfilePreviewFieldGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    private void init(Context context)
    {
        if(this.groupItems == null) return;
        if(this.Index >= 2 && !isShowMore) return;

        fieldItemsLayout.removeAllViews();

        itemViewList = new ArrayList<PersonalProfilePreviewFieldItemView>();

        if(strGroupName.toLowerCase().contains("name"))
        {
            txtFieldsGroupName.setText("NAME");
        }
        else if(strGroupName.toLowerCase().contains("title"))
        {
            txtFieldsGroupName.setText("TITLE");
        }
        else if(strGroupName.toLowerCase().contains("company"))
        {
            txtFieldsGroupName.setText("COMPANY");
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
        else if(strGroupName.toLowerCase().contains("birthday"))
        {
            txtFieldsGroupName.setText("BIRTHDAY");
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

        boolean bExist = false;

        for(int i=0;i<this.groupItems.size();i++)
        {
            Index++;
            if (Index > 2 && !isShowMore) break;

            bExist = true;
            PersonalProfilePreviewFieldItemView itemView = new PersonalProfilePreviewFieldItemView(this.groupItems.get(i) , context);
            itemViewList.add(itemView);
            fieldItemsLayout.addView(itemView);
        }

        if (!bExist) txtFieldsGroupName.setVisibility(View.GONE);
    }
}
