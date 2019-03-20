package com.ginko.activity.sync;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.context.ConstValues;
import com.ginko.ginko.R;
import com.ginko.view.ext.SelectableListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncGreyContactListAdapter extends BaseAdapter{
    private List<SyncGreyContactItem> items = new ArrayList<SyncGreyContactItem>();
    private Context mContext;

    private boolean isListSelectable = false;
    private OnContactItemTypeSelectedListener itemTypeSelectedListener;

    private Set<Integer> selectedItems = new HashSet<Integer>();

    public void setOnContactItemTypeSeletedListener(OnContactItemTypeSelectedListener listener)
    {
        this.itemTypeSelectedListener = listener;
    }

    public boolean getListSelectable() {return this.isListSelectable;}

    public void setListSelectable(boolean selectable){
        this.isListSelectable = selectable;
        if(selectedItems!=null)
            selectedItems.clear();
        else
            selectedItems = new HashSet<Integer>();
    }

    public SyncGreyContactListAdapter(Context context)
    {
        this.mContext = context;
        if(items == null)
            items = new ArrayList<SyncGreyContactItem>();
    }

    public synchronized void addItem(SyncGreyContactItem newItem)
    {
        if(items == null)
            items = new ArrayList<SyncGreyContactItem>();

        items.add(newItem);
    }

    public synchronized void clearAll()
    {
        if(items == null)
            items = new ArrayList<SyncGreyContactItem>();
        else
        {
            try {
                items.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            selectedItems.clear();
        }

        notifyDataSetChanged();
    }

    public synchronized void addAll(List<SyncGreyContactItem> arrays)
    {
        if(arrays != null)
        {
            items = new ArrayList<SyncGreyContactItem>();
            for(SyncGreyContactItem item : arrays)
            {
                items.add(new SyncGreyContactItem(item));
            }
        }
        else
        {
            items = new ArrayList<SyncGreyContactItem>();
        }
    }

    public synchronized void selectAllType(int type)
    {
        if(items!=null)
        {
            for(int i=0;i<items.size();i++) {
                items.get(i).setGreyContactType(type);
            }
        }
    }

    public void removeItem(int position)
    {
        if(items == null) {
            items = new ArrayList<SyncGreyContactItem>();
        }
        if(position>=items.size())
            return;

        SyncGreyContactItem item = items.get(position);
        if(selectedItems.contains(item.getSyncContactId()))
            selectedItems.remove(item.getSyncContactId());
        items.remove(position);
    }

    public void deleteSelectedItems()
    {
        if(items == null) {
            items = new ArrayList<SyncGreyContactItem>();
            return;
        }
        int index = 0;
        while(index<items.size())
        {
            if(selectedItems.contains(items.get(index).getSyncContactId()))
            {
                selectedItems.remove(items.get(index).getSyncContactId());
                items.remove(index);
                continue;
            }
            index++;
        }
    }

    public void toggleItem(int position)
    {
        if(selectedItems == null)
            selectedItems = new HashSet<Integer>();
        if(position>=items.size()) {
            return;
        }
        SyncGreyContactItem item = items.get(position);
        item.setSelection(!item.getSelection());

        if(item.getSelection())
        {
            selectedItems.add(item.getSyncContactId());
        }
        else
        {
            selectedItems.remove(item.getSyncContactId());
        }
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedContactsIds()
    {
        if(selectedItems == null)
            selectedItems = new HashSet<Integer>();
        return this.selectedItems;
    }

    public int getSelectedItemCount()
    {
        if(items == null) return 0;
        int count = 0;
        for(int i=0;i<items.size();i++)
        {
            if(items.get(i).getSelection() == true)
                count++;
        }
        return count;
    }

    public List<SyncGreyContactItem> getAll()
    {
        return this.items;
    }

    //For unselected
    public void unselectedAll()
    {
        if(items != null)
            for(int i=0; i<items.size(); i++)
            {
                if (items.get(i).getSelection())
                    items.get(i).setSelection(false);
            }
    }

    public synchronized void searchItems(String keyword)
    {
        if(items == null)
            items = new ArrayList<SyncGreyContactItem>();
        else
        {
            for(SyncGreyContactItem item: items)
            {
                if(item.getGreyContactEmail().toLowerCase().contains(keyword) || item.getGreyContactName().toLowerCase().contains(keyword))
                    item.setVisibility(true);
                else
                    item.setVisibility(false);
            }
        }

        notifyDataSetChanged();
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        SyncGreyContactItemView itemView = null;
        final SyncGreyContactItem item = (SyncGreyContactItem) getItem(position);
        if(convertView == null)
        {
            itemView = new SyncGreyContactItemView(mContext , item);
        }
        else
        {
            itemView = (SyncGreyContactItemView)convertView;
        }

        if(item.getVisibility())
        {
            itemView.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
        }
        else
        {
            itemView.findViewById(R.id.rootLayout).setVisibility(View.GONE);
        }

        //check box
        ImageView imgCheck = (ImageView) itemView.findViewById(R.id.imageSelectionCheck);
        imgCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isListSelectable){
                    itemTypeSelectedListener.OnCheckBoxselected(position);
                }
            }
        });

        if(isListSelectable)
            imgCheck.setVisibility(View.VISIBLE);
        else
            imgCheck.setVisibility(View.GONE);

        if(item.getSelection())
            imgCheck.setImageResource(R.drawable.contact_info_item_selected);
        else
            imgCheck.setImageResource(R.drawable.contact_info_item_non_selected);

        ImageView imgEntity = (ImageView)itemView.findViewById(R.id.imgEntity);
        imgEntity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* For GAD-1157
                if(isListSelectable)
                    return;*/
                if(item.getGreyContactType() == ConstValues.GREY_TYPE_ENTITY)
                    item.setGreyContactType(ConstValues.GREY_TYPE_NONE);
                else
                    item.setGreyContactType(ConstValues.GREY_TYPE_ENTITY);
                if(itemTypeSelectedListener != null)
                    itemTypeSelectedListener.OnContactItemTypeChanged(position , ConstValues.GREY_TYPE_ENTITY);
            }
        });
        ImageView imgWork = (ImageView)itemView.findViewById(R.id.imgWork);
        imgWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* For GAD-1157
                if(isListSelectable)
                    return;*/
                if(item.getGreyContactType() == ConstValues.GREY_TYPE_WORK)
                    item.setGreyContactType(ConstValues.GREY_TYPE_NONE);
                else
                    item.setGreyContactType(ConstValues.GREY_TYPE_WORK);
                if(itemTypeSelectedListener != null)
                    itemTypeSelectedListener.OnContactItemTypeChanged(position , ConstValues.GREY_TYPE_WORK);
            }
        });
        ImageView imgHome = (ImageView)itemView.findViewById(R.id.imgHome);
        imgHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* For GAD-1157
                if(isListSelectable)
                    return;*/
                if(item.getGreyContactType() == ConstValues.GREY_TYPE_HOME)
                    item.setGreyContactType(ConstValues.GREY_TYPE_NONE);
                else
                    item.setGreyContactType(ConstValues.GREY_TYPE_HOME);
                if(itemTypeSelectedListener != null)
                    itemTypeSelectedListener.OnContactItemTypeChanged(position , ConstValues.GREY_TYPE_HOME);
            }
        });

        TextView txtContactName = (TextView)itemView.findViewById(R.id.txtGreyContactName);
        txtContactName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isListSelectable)
                {
                    Intent greyContactProfileIntent = new Intent(mContext , GreyContactOne.class);
                    greyContactProfileIntent.putExtra("jsonvalue" , item.getJsonString());
                    mContext.startActivity(greyContactProfileIntent);
                }
            }
        });


        itemView.setItem(item);
        itemView.refreshView();

        return itemView;
    }


    public interface OnContactItemTypeSelectedListener
    {
        public void OnContactItemTypeChanged(int position , int contactType);
        public void OnCheckBoxselected(int position);
    }

}
