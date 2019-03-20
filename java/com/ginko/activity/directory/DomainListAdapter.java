package com.ginko.activity.directory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.ginko.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongJong on 01/13/17.
 */
public class DomainListAdapter extends BaseAdapter {
    private List<String> domainList;
    private Context mContext = null;

    public DomainListAdapter(Context context) {
        domainList = new ArrayList<String>();
        this.mContext = context;
    }

    public void setListItems(List<String> list) {
        this.domainList = list;
    }

    public void addItem(String domain)
    {
        domainList.add(domain);
    }

    public void removeItem(String domain)
    {
        domainList.remove(domain);
    }

    public void addItems(List<String> domains)
    {
        for(int i=0;i<domains.size();i++)
        {
            domainList.add(domains.get(i));
        }
    }

    public void clearAll() {
        if (domainList != null)
        {
            try
            {
                domainList.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            domainList = new ArrayList<String>();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return domainList == null ? 0 : domainList.size();
    }

    @Override
    public Object getItem(int position) {
        return domainList.get(position);
    }

    public void replaceItem(final String newItem, final int position) {
        domainList.set(position, newItem);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        DomainItemView itemView = null;
        final String domain = domainList.get(position);
        if(itemView == null)
        {
            itemView = new DomainItemView(mContext , domain);
        }
        else
        {
            itemView = (DomainItemView)convertView;
        }

        itemView.setItem(domain);
        itemView.refreshView();

        ImageView btnRemove = (ImageView)itemView.findViewById(R.id.imgRemove);

        if (btnRemove != null)
        {
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    domainList.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
        return itemView;
    }
}

class DomainItemView extends LinearLayout {
    private Context mContext = null;
    private String domain;
    private LayoutInflater inflater;

    private TextView txtDomainName;
    private ImageView imgRemove;

    private List<String> phones = new ArrayList<String>();

    public DomainItemView(Context context, String domain) {
        super(context);
        this.mContext = context;
        this.domain = domain;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.domain_address_item , this , true);

        txtDomainName = (TextView)findViewById(R.id.txtDomainName);
        imgRemove = (ImageView)findViewById(R.id.imgRemove);
    }

    public void setItem(String item)
    {
        this.domain = item;
    }
    public String getItem(){return this.domain;}

    public void refreshView()
    {
        txtDomainName.setText(domain);
        if (mContext != null && mContext.getClass() == DirAdminPreviewActivity.class)
            imgRemove.setVisibility(View.GONE);


    }
}
