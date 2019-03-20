package com.ginko.activity.profiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.ginko.R;
import com.ginko.vo.UserProfileVO;

public class ProfileFieldItemView extends LinearLayout{
    private UserProfileVO fieldItem;
    private Context mContext;
    private LayoutInflater inflater;

    private LinearLayout bottomDivider;
    private RelativeLayout itemLayout;
    private TextView txtFieldName , txtFieldValue;

    private boolean isHomeOrWork = false; // true : Home , false: Work
    private boolean isSelected = false;
    private boolean isDividerVisible = false;

    private onProfileFieldItemClickListener itemClickListener;



    public interface onProfileFieldItemClickListener
    {
        public void onFieldClicked(boolean bHomeOrWork);
    }


    public ProfileFieldItemView(Context context) {
        super(context);
        this.mContext = context;
    }

    public ProfileFieldItemView(Context context, boolean _isHomeOrWork, UserProfileVO field , onProfileFieldItemClickListener _itemClickListener ) {
        super(context);
        this.mContext = context;
        this.fieldItem = field;
        this.isHomeOrWork = _isHomeOrWork;
        this.itemClickListener = _itemClickListener;

        if(fieldItem.isShared())
            this.isSelected = true;
        else
            this.isSelected = false;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.profile_sharing_field_item_layout, this, true);

        bottomDivider = (LinearLayout)findViewById(R.id.bottom_divider);
        itemLayout = (RelativeLayout)findViewById(R.id.itemLayout);
        itemLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelected = !isSelected;
                if(itemClickListener != null)
                    itemClickListener.onFieldClicked(isHomeOrWork);
            }
        });
        txtFieldName = (TextView)findViewById(R.id.txtFieldName);
        txtFieldValue = (TextView)findViewById(R.id.txtFieldValue);

        refreshView();
    }

    public UserProfileVO getFieldItem(){return this.fieldItem;}

    public void setSelected(boolean selected){this.isSelected = selected;}
    public boolean isSelected(){return this.isSelected;}

    public void setDividerVisibility(boolean visible){this.isDividerVisible = visible;}
    public boolean getDividerVisibility(){return this.isDividerVisible;}

    public void refreshView()
    {
        if(isSelected)
            itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.profile_share_field_item_selected_color));
        else
            itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.profile_share_field_item_unselected_color));

       if(isDividerVisible)
            bottomDivider.setVisibility(View.VISIBLE);
        else
            bottomDivider.setVisibility(View.GONE);

        if(fieldItem != null)
        {
            txtFieldName.setText(fieldItem.getFieldName() + ".");
            txtFieldValue.setText(fieldItem.getValue());
        }
        else
        {
            txtFieldName.setText("");
            txtFieldValue.setText("");
        }
    }
}
