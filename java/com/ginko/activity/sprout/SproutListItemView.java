package com.ginko.activity.sprout;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ginko.ginko.R;

public class SproutListItemView extends LinearLayout{
    public LayoutInflater inflater = null;
    public SproutSearchItem item;

    public Context mContext;

    public SproutListItemView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;

    }
    public SproutListItemView(Context context,  SproutSearchItem _item) {
        super(context);
        this.mContext = context;
        item = _item;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItem(SproutSearchItem _item)
    {
        this.item = _item;
    }

    public void refreshView()
    {

    }

}
