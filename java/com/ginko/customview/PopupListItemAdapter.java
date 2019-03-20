package com.ginko.customview;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public abstract class PopupListItemAdapter<T> extends ArrayAdapter<T> {
    private List<T> listItems;

    public PopupListItemAdapter(Context context, List<T> objects) {
        super(context, android.R.layout.activity_list_item, objects);
        this.listItems = objects;
    }

    public T getItem(int position)
    {
        if(listItems!=null && listItems.size()>position)
        {
            return listItems.get(position);
        }
        return null;
    }

    @Override
    public int getCount()
    {
        return listItems==null?0:listItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = newView();
            viewHolder = new ViewHolder();
            viewHolder.tvText = (TextView) convertView
                    .findViewById(android.R.id.text1);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        refreshView();
        convertView.setTag(viewHolder);

        return convertView;
    }

    public void refreshView()
    {

    }

    private class ViewHolder {
        TextView tvText;
    }

    private View newView() {
        LinearLayout ll_parent = new LinearLayout(getContext());
        ll_parent.setGravity(Gravity.CENTER_VERTICAL);
        ll_parent.setOrientation(LinearLayout.HORIZONTAL);

        ImageView ivIcon = new ImageView(getContext());
        ivIcon.setId(android.R.id.icon);
        ll_parent.addView(ivIcon);

        TextView tvText = new TextView(getContext());
        //tvText.setTextColor(textColor);
        //tvText.setTextSize(textSize);
        tvText.setMaxLines(1);
        tvText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvText.setMarqueeRepeatLimit(-1);
        tvText.setId(android.R.id.text1);
        ll_parent.addView(tvText);
        //ll_parent.setPadding(leftPadding, topPadding, rightPadding,
        //        bottomPadding);
        //ll_parent.setBackgroundColor(backgroundColor);

        return ll_parent;
    }
}