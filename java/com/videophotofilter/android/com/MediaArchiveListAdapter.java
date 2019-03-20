package com.videophotofilter.android.com;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ginko.customview.CustomNetworkImageView;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

import java.util.ArrayList;


public class MediaArchiveListAdapter extends BaseAdapter{

	private ArrayList<ArchiveMediaItem> listItems;
	private Context mContext;

    private ArchiveItemClickListener archiveItemClickListener;

    private float ratio = 1.0f;

    public void setOnArchiveItemClickListener(ArchiveItemClickListener listenr)
    {
        this.archiveItemClickListener = listenr;
    }

	public MediaArchiveListAdapter(Context context , float _ratio)
	{
		listItems = new ArrayList<ArchiveMediaItem>();
		mContext = context;
        this.ratio = _ratio;
	}
	public void setListItems(ArrayList<ArchiveMediaItem> itemsLists)
	{
        this.listItems = itemsLists;
	}
    public ArrayList<ArchiveMediaItem> getListItems(){
        if(listItems == null)
            listItems = new ArrayList<ArchiveMediaItem>();

        return listItems;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
        System.out.println("--------size = "+listItems.size()+"--------");
		return listItems==null?0:listItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listItems.get(position);
	}

    @Override
    public long getItemId(int position) {
        return position;
    }

	public void removeItem(ArchiveMediaItem item)
	{
		if(listItems != null)
		{
			for(int i=0;i<listItems.size();i++)
			{
				if(item.archiveID == listItems.get(i).archiveID)
				{
					listItems.remove(i);
					break;
				}
			}
		}
	}

    public void removeItem(int pos)
    {
        if(listItems != null)
        {
            if(listItems.size()>pos)
            {
                listItems.remove(pos);
            }
        }

    }
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ArchiveItemView itemView = null;
		final ArchiveMediaItem item = listItems.get(position);
        final int pos = position;
		if(convertView == null)
		{
			itemView = new ArchiveItemView(this.mContext , item , ratio);
            System.out.println("--------"+position+"--------");
		}
		else
		{
			itemView = (ArchiveItemView)convertView;
		}

        CustomNetworkImageView imgThumb = (CustomNetworkImageView)itemView.findViewById(R.id.imgThumb);

        imgThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.isDefaultItem) return;
                if(archiveItemClickListener != null)
                {
                    archiveItemClickListener.onArchiveItemClick(item , pos);
                }
            }
        });

        ImageView imgClose = (ImageView)itemView.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(archiveItemClickListener != null)
                {
                    archiveItemClickListener.onArchiveDeleteClick(item , pos);
                }
            }
        });

        if(item.isDefaultItem)
            imgClose.setVisibility(View.INVISIBLE);
        else
            imgClose.setVisibility(View.VISIBLE);

		
		itemView.setItem(item);
		itemView.refreshView();
		
		return itemView;
	}

    public interface ArchiveItemClickListener
    {
        public void onArchiveItemClick(ArchiveMediaItem item , int position);
        public void onArchiveDeleteClick(ArchiveMediaItem item , int position);
    }
}
