package com.ginko.activity.exchange;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ginko.ginko.R;

import java.util.ArrayList;
import java.util.List;

public class ExchangeRequestListAdapter extends BaseAdapter{
    private Context mContext;

    private List<ExchangeItem> items;

    private boolean isSelectable = false;

    public void setIsSelectable(boolean _isSelectable)
    {
        this.isSelectable = _isSelectable;
    }

    public boolean getIsSelectable(){return this.isSelectable;}

    public ExchangeRequestListAdapter(Context context)
    {
        this.mContext = context;
        items = new ArrayList<ExchangeItem>();
    }


    @Override
    public int getCount() {
        return items==null?0:items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getSelectedItemCount()
    {
        if(items == null) return 0;
        int count = 0;
        for(int i=0;i<items.size();i++)
        {
            if(items.get(i).isSelected())
                count++;
        }
        return count;
    }

    public int getVisibleItemCount()
    {
        if (items == null) return 0;
        int count = 0;
        for (int i=0;i<items.size();i++)
        {
            if (items.get(i).getVisibility() == true)
                count++;
        }
        return count;
    }

    public List<ExchangeItem> getSelectedItems()
    {
        List<ExchangeItem> selectedItems = new ArrayList<ExchangeItem>();
        for(int i=0;i<items.size();i++)
        {
            if(items.get(i).isSelected())
                selectedItems.add(items.get(i));
        }
        return selectedItems;
    }


    public void deleteSelectedItems()
    {
        if(items == null) return;
        int index = 0;
        while(index<items.size())
        {
            if(items.get(index).isSelected()) {
                items.remove(index);
                continue;
            }
            index++;
        }
    }

    public void addItem(ExchangeItem item)
    {
        if(items==null)
            items = new ArrayList<ExchangeItem>();
        items.add(item);
    }

    public void deleteItem(int position)
    {
       items.remove(position);
    }

    public void clearAll()
    {
        if(items != null)
            items.clear();
        else
            items = new ArrayList<ExchangeItem>();
    }


    public void unSelectAll()
    {
        if(items == null) return;
        for(int i=0;i<items.size();i++)
        {
            items.get(i).setSelection(false);
        }
    }

    public void triggerSelection(int position)
    {
        if(items!=null && items.size()>position)
        {
            items.get(position).setSelection(!items.get(position).isSelected());
        }
        notifyDataSetChanged();
    }

    public void filterItemsByString(String strQuery)
    {
        if(items != null)
        {
            if(strQuery.compareTo("") != 0) {
                for (int i = 0; i < items.size(); i++) {
                    ExchangeItem item = items.get(i);
                    String contactName = item.contactName.equals("")?item.email:item.contactName;
                    if (contactName.toLowerCase().contains(strQuery))
                        items.get(i).setVisibility(true);
                    else
                        items.get(i).setVisibility(false);
                }
            }
            else//if search with empty string
            {
                for (int i = 0; i < items.size(); i++) {
                    items.get(i).setVisibility(true);
                }
            }
            notifyDataSetChanged();
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExchangeItemView view = null;
        ExchangeItem item = (ExchangeItem) getItem(position);
        if (convertView == null) {
            view = new ExchangeItemView(mContext , item);
        }
        else
        {
            view = (ExchangeItemView)convertView;
        }

        if(item.getVisibility())
        {
            view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        }
        else
        {
            view.findViewById(R.id.rootLayout).setVisibility(View.GONE);
        }

        //check box
        ImageView imgCheck = (ImageView) view.findViewById(R.id.imageSelectionCheck);
        if(isSelectable)
            imgCheck.setVisibility(View.VISIBLE);
        else{
            unSelectAll();
            imgCheck.setVisibility(View.GONE);
        }

        if(item.isSelected())
            imgCheck.setImageResource(R.drawable.contact_info_item_selected);
        else
            imgCheck.setImageResource(R.drawable.contact_info_item_non_selected);

        view.setItem(item);
        view.refreshView();


        getSelectedItemCount();
        return view;
    }
}
