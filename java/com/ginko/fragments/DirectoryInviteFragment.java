package com.ginko.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.directory.DirectoryInviteContactItem;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.utils.ViewIdGenerator;

import java.util.List;

public class DirectoryInviteFragment extends android.support.v4.app.Fragment {

    public static final int DIRECTORY_CONTACT_REQUESTED = 0;
    public static final int DIRECTORY_CONTACT_CONFIRMED = 1;
    public static final int DIRECTORY_CONTACT_PENDING = 2;
    public static final int DIRECTORY_CONTACT_NOT_INVITED = 3;

    protected ListView listView;

    private int fragmentType = DIRECTORY_CONTACT_REQUESTED; /*
                                                                   Requested
                                                                   Confirmed
                                                                   Pending
                                                                   Invite contacts(not invted)
                                                                   */

    private DirectoryInviteListAdapter adapter = null;
    private List<DirectoryInviteContactItem> directoryInviteContactItems;

    private boolean isUICreated = false;

    private AdapterView.OnItemClickListener itemClickListener;

    public DirectoryInviteFragment() {
    }

    public DirectoryInviteFragment(int fragment_type , List<DirectoryInviteContactItem> contactItems)
    {
        this.fragmentType = fragment_type;
        this.directoryInviteContactItems = contactItems;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        listView = new ListView(this.getActivity());
        listView.setId(ViewIdGenerator.generateViewId());
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setSelector(android.R.color.transparent);
        listView.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));

        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isUICreated = true;
        initialList(this.fragmentType, this.directoryInviteContactItems);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isUICreated = false;
    }

    public void initialList(int fragment_type , List<DirectoryInviteContactItem> contactItems)
    {
        this.fragmentType = fragment_type;
        this.directoryInviteContactItems = contactItems;
        if(!isUICreated)
            return;

        if(adapter == null)
        {
            adapter = new DirectoryInviteListAdapter(getActivity());
        }

        listView.setAdapter(adapter);

        if(this.itemClickListener != null)
        {
            listView.setOnItemClickListener(this.itemClickListener);
        }

        adapter.notifyDataSetChanged();
    }

    public void setOnListViewItemClickListener(AdapterView.OnItemClickListener _itemClickListener)
    {
        this.itemClickListener =  _itemClickListener;
        if(isUICreated && listView!=null)
            listView.setOnItemClickListener(this.itemClickListener);
    }


    public void refreshListView()
    {
        if(isUICreated && adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
    }

    public void filterItemsByString(String strQuery)
    {
        if(directoryInviteContactItems != null && isUICreated && adapter!= null)
        {
            if(strQuery.compareTo("") != 0) {
                for (int i = 0; i < directoryInviteContactItems.size(); i++) {
                    DirectoryInviteContactItem item = directoryInviteContactItems.get(i);
                    String contactName = item.getFullName();
                    if (contactName.toLowerCase().contains(strQuery))
                        directoryInviteContactItems.get(i).setVisibility(true);
                    else
                        directoryInviteContactItems.get(i).setVisibility(false);
                }
            }
            else//if search with empty string
            {
                for (int i = 0; i < directoryInviteContactItems.size(); i++) {
                    directoryInviteContactItems.get(i).setVisibility(true);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    public int getVisibleCount()
    {
        int totalCnt = 0;
        if(directoryInviteContactItems != null && isUICreated && adapter!= null) {
            for (int i = 0; i < directoryInviteContactItems.size(); i++) {
                DirectoryInviteContactItem item = directoryInviteContactItems.get(i);
                if (item.getVisibility() == true) totalCnt++;
            }
        }

        return totalCnt;
    }

    public int getSelectedItemCounts()
    {
        int totalCnt = 0;
        if(directoryInviteContactItems != null && isUICreated && adapter!= null) {
            for (int i = 0; i < directoryInviteContactItems.size(); i++) {
                DirectoryInviteContactItem item = directoryInviteContactItems.get(i);
                if (item.getVisibility() == true && item.isSelected()) totalCnt++;
            }
        }

        return totalCnt;
    }

    public void selectAll()
    {
        if(directoryInviteContactItems != null && isUICreated && adapter!= null)
        {
            for(int i=0;i<directoryInviteContactItems.size();i++)
            {
                if (directoryInviteContactItems.get(i).getVisibility() == true)
                    directoryInviteContactItems.get(i).setSelected(true);
            }
            adapter.notifyDataSetChanged();
        }
    }


    public void unselectAll()
    {
        if(directoryInviteContactItems != null && isUICreated && adapter!= null)
        {
            for(int i=0;i<directoryInviteContactItems.size();i++)
            {
                if (directoryInviteContactItems.get(i).getVisibility() == true)
                    directoryInviteContactItems.get(i).setSelected(false);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class DirectoryInviteListAdapter extends BaseAdapter {
        private Context mContext;

        public DirectoryInviteListAdapter(Context context)
        {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return directoryInviteContactItems==null?0:directoryInviteContactItems.size();
        }

        @Override
        public Object getItem(int position) {
            return directoryInviteContactItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DirectoryInviteItemView itemView = null;
            DirectoryInviteContactItem item = (DirectoryInviteContactItem) getItem(position);
            if(convertView == null)
            {
                itemView = new DirectoryInviteItemView(mContext,  item);
            }
            else
            {
                itemView = (DirectoryInviteItemView)convertView;
            }

            ImageView checkBox = (ImageView)itemView.findViewById(R.id.imageSelectionCheck);

            if(fragmentType == DIRECTORY_CONTACT_NOT_INVITED)
            {
                if(item.isSelected()) {
                    itemView.findViewById(R.id.rootLayout).setBackgroundColor(0xffd9d9d9);
                }
                else
                {
                    itemView.findViewById(R.id.rootLayout).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                }
                checkBox.setVisibility(View.GONE);
            }
            else
            {
                checkBox.setVisibility(View.VISIBLE);
            }

            if(item.getVisibility())
                itemView.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
            else
                itemView.findViewById(R.id.rootLayout).setVisibility(View.GONE);

            itemView.setItem(item);
            itemView.refreshView();

            return  itemView;
        }
    }

    private class DirectoryInviteItemView extends LinearLayout
    {
        private LayoutInflater inflater = null;
        private DirectoryInviteContactItem item;
        private NetworkImageView profileImage;
        private TextView contactName;
        private ImageView imageSelectionCheck;

        private Context mContext;

        private ImageLoader imgLoader;

        public DirectoryInviteItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.mContext = context;

        }
        public DirectoryInviteItemView(Context context,  DirectoryInviteContactItem _item)
        {
            super(context);
            this.mContext = context;
            item  = _item;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.entity_invite_contact_listitem, this, true);

            profileImage = (NetworkImageView)findViewById(R.id.profileImage);
            contactName = (TextView) findViewById(R.id.contactName);
            imageSelectionCheck = (ImageView)findViewById(R.id.imageSelectionCheck);
        }
        public void setItem(DirectoryInviteContactItem _item)
        {
            this.item = _item;
        }
        public void refreshView()
        {
            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            profileImage.setDefaultImageResId(R.drawable.no_face);
            profileImage.setImageUrl(item.getPhotoUrl(), imgLoader);

            String fullName = item.getFirstName();
            if(!item.getMiddleName().equals(""))
                fullName = fullName + "\n"+ item.getMiddleName();
            if(!item.getLastName().equals(""))
                fullName = fullName + "\n" + item.getLastName();

            contactName.setText(fullName);

            if(item.isSelected())
                imageSelectionCheck.setImageResource(R.drawable.contact_info_item_selected);
            else
                imageSelectionCheck.setImageResource(R.drawable.contact_info_item_non_selected);

        }
    }
}
