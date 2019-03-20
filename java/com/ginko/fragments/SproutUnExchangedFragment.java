package com.ginko.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.activity.sprout.GetSproutAddressFromLatlngTask;
import com.ginko.activity.sprout.SproutSearchItem;
import com.ginko.activity.sprout.SproutUnExchangedItemView;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.GeoLibrary;
import com.ginko.common.ProperListView;
import com.ginko.customview.SharingBean;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.imagecrop.Util;
import com.ginko.view.ext.SelectableListAdapter;
import com.ginko.vo.UserEntityProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SproutUnExchangedFragment  extends Fragment {

    private ProperListView listView;

    private SelectableListAdapter<SproutSearchItem> adapter;

    private boolean isListSelectable = false;

    private SelectableListAdapter.ItemSelectedListener<SproutSearchItem> selectionListener;
    private ArrayList<SproutSearchItem> allItems = new ArrayList<SproutSearchItem>();

    private Object lockObj = new Object();

    private HashMap<String , String> addressSearchedItemsMap = new HashMap<String , String>();

    private UserEntityProfileVO entity;

    private int tmp_contactId = 0;
    public void setIsListSelectable(boolean _isListSelectable)
    {
        this.isListSelectable = _isListSelectable;
        if(adapter == null) return;

        if(isListSelectable == false) {
            for (int i = 0; i < adapter.getSelectedItems().size(); i++) {
                adapter.getSelectedItems().get(i).setSelected(false);
            }
            adapter.clearSelection();
        }
        adapter.notifyDataSetChanged();
    }

    public int getSelectedItemCounts()
    {
        if(adapter != null)
        {
            return adapter.getSelectedItems().size();
        }
        return 0;
    }

    public List<SproutSearchItem> getSelectedItems()
    {
        if(adapter != null)
        {
            return adapter.getSelectedItems();
        }
        return null;
    }
    public synchronized void removeSelectedContactItemsFromList()
    {
        if(adapter != null)
        {
            synchronized (lockObj) {
                /*int n = 0;
                while (n < adapter.getCount() && adapter.getItem(n).contactType != 3) {
                    if (adapter.getItem(n).contactOrEntityID == adapter.getselect) {
                        adapter.removeItem(n);
                        continue;
                    }
                    n++;
                }*/
                for (int i = 0; i < adapter.getSelectedItems().size(); i++) {
                    if (allItems != null)
                        allItems.remove(adapter.getSelectedItems().get(i));
                    adapter.remove(adapter.getSelectedItems().get(i));
                }

                adapter.clearSelection();
                adapter.notifyDataSetChanged();
            }
        }
    }
    public synchronized void removeSelectedEntityItemsFromList()
    {
        if(adapter != null)
        {
            synchronized (lockObj) {
                for (int i = 0; i < adapter.getSelectedItems().size(); i++) {
                    if (allItems != null)
                        allItems.remove(adapter.getSelectedItems().get(i));
                    adapter.remove(adapter.getSelectedItems().get(i));
                }
                adapter.clearSelection();

                if (getActivity() == null)
                    return;

                adapter.notifyDataSetChanged();
            }
        }
    }

    public synchronized void removeItem(SproutSearchItem item)
    {
        if(adapter != null)
        {
            synchronized (lockObj) {
                if (getActivity() == null)
                    return;
                adapter.remove(item);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setListViewSelectListener(SelectableListAdapter.ItemSelectedListener<SproutSearchItem> listener)
    {
        this.selectionListener = listener;
        if(adapter != null)
            adapter.setSelectedListener(this.selectionListener);
    }

    /*
    public synchronized void searchItems(String strSearch)
    {
        if(adapter == null)
            return;
        if(strSearch.equals(""))
        {
            for(int i=0;i<adapter.getCount();i++)
            {
                adapter.getItem(i).setVisible(true);
            }
        }
        else
        {
            for(int i=0;i<adapter.getCount();i++)
            {
                SproutSearchItem item = adapter.getItem(i);
                if(item.entityOrContactName.toLowerCase().contains(strSearch))
                    adapter.getItem(i).setVisible(true);
                else
                    adapter.getItem(i).setVisible(false);
            }
        }

        if (getActivity() == null)
            return;

        adapter.notifyDataSetChanged();
    } */

    public synchronized void searchItems(String strSearch)
    {
        if(allItems == null)
            return;

        ArrayList<SproutSearchItem> tempList = new ArrayList<SproutSearchItem>(allItems);
        if(strSearch.equals(""))
        {
        }
        else
        {
            for(int i=0;i<allItems.size();i++)
            {
                SproutSearchItem item = allItems.get(i);
                if(!item.entityOrContactName.toLowerCase().contains(strSearch))
                    tempList.remove(item);
            }
        }

        if (getActivity() == null)
            return;

        adapter.clear();
        adapter.addAll(tempList);
        adapter.notifyDataSetChanged();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SproutExchangedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SproutUnExchangedFragment newInstance(SelectableListAdapter.ItemSelectedListener<SproutSearchItem> listener) {
        SproutUnExchangedFragment fragment = new SproutUnExchangedFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        fragment.setListViewSelectListener(listener);
        return fragment;
    }

    public SproutUnExchangedFragment() {
        // Required empty public constructor
        adapter = new SelectableListAdapter<SproutSearchItem>(this.getActivity()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //int type = getItemViewType(position);
                SproutUnExchangedItemView view = null;
                final SproutSearchItem item = getItem(position);

                if (convertView == null) {
                    view = new SproutUnExchangedItemView(getContext() , item);
                }
                else
                {
                    view =(SproutUnExchangedItemView)convertView;
                }

                view.setIsListSelectable(isListSelectable);

                if(item.strAddress.equals("") && item.lat!=0.0d && item.lng!=0.0d && !item.isGettingAddress) {

                    String key = String.valueOf(item.contactType) + "_" + String.valueOf(item.contactOrEntityID);
                    if(addressSearchedItemsMap.containsKey(key))
                    {
                        item.strAddress = addressSearchedItemsMap.get(key);
                    }
                    else {
                        GetSproutAddressFromLatlngTask getAddressFromLatlngTask = new GetSproutAddressFromLatlngTask(getActivity(), item , adapter);
                        getAddressFromLatlngTask.start();
                    }
                }

                //check box
                ImageView imgCheck = (ImageView) view.findViewById(R.id.imageSelectionCheck);

                if(isListSelectable)
                    imgCheck.setVisibility(View.VISIBLE);
                else
                    imgCheck.setVisibility(View.GONE);

                if(item.isSelected()) {
                    imgCheck.setImageResource(R.drawable.contact_info_item_selected);
                    select(position);
                }
                else {
                    imgCheck.setImageResource(R.drawable.contact_info_item_non_selected);
                    unSelect(position);
                }

                if(item.isVisible())
                {
                    view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
                }
                else
                {
                    view.findViewById(R.id.rootLayout).setVisibility(View.GONE);
                }

                view.setItem(item);
                view.refreshView();
                return view;
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_sprout_exchanged, container, false);

        listView = (ProperListView) view.findViewById(R.id.list);

        adapter.setSelectedListener(this.selectionListener);

        listView.setAdapter(adapter);
        //setListViewHeightBasedOnChildren(listView);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (isListSelectable) {
                        final SproutSearchItem item = adapter.getItem(position);
                        item.setSelected(!item.isSelected());
                        adapter.tiggel(position);
                        adapter.notifyDataSetChanged();
                    } else {
                        final SproutSearchItem item = adapter.getItem(position);
                        if (item.contactType == 3)//entity
                        {
                            final boolean isFollowed = item.isFollowed;
                            final int contactID = item.contactOrEntityID;

                            EntityRequest.viewEntity(item.contactOrEntityID, new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    if (response.isSuccess()) {
                                        JSONObject jsonObj = response.getData();
                                        JSONObject jData = null;
                                        try {

                                            entity = JsonConverter.json2Object(
                                                    (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                        } catch (JsonConvertException e) {
                                            e.printStackTrace();
                                            entity = null;
                                        }
                                        if (entity.getInfos().size() > 1) {
                                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityMultiLocationsProfileActivity.class);
                                            entityProfileIntent.putExtra("entityJson", entity);
                                            entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                            entityProfileIntent.putExtra("contactID", contactID);
                                            getActivity().startActivityForResult(entityProfileIntent, 980);
                                        } else {
                                            Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                                            entityProfileIntent.putExtra("entityJson", entity);
                                            entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                            entityProfileIntent.putExtra("contactID", contactID);
                                            getActivity().startActivityForResult(entityProfileIntent, 980);
                                        }
                                    } else {
                                        if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                            MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactID);
                                            MyApp.getInstance().removefromContacts(contactID);

                                            MyApp.getInstance().getGinkoModel().deleteContactWithContactId(contactID);
                                            removeItem(item);
                                        }
                                    }
                                }
                            }, true);
                        } else {
                            Intent shareLeafIntent = new Intent(getActivity(), ShareYourLeafActivity.class);
                            tmp_contactId = item.contactOrEntityID;
                            shareLeafIntent.putExtra("contactID", String.valueOf(item.contactOrEntityID));
                            shareLeafIntent.putExtra("contactFullname", item.entityOrContactName);
                            shareLeafIntent.putExtra("isUnexchangedContact", true);
                            shareLeafIntent.putExtra("isInviteContact", true);  //For show green color
                            shareLeafIntent.putExtra("isPendingRequest", item.isPending);
                            shareLeafIntent.putExtra("lat", item.lat);
                            shareLeafIntent.putExtra("long", item.lng);
                            shareLeafIntent.putExtra("address", item.strAddress);
                            getActivity().startActivityForResult(shareLeafIntent, 2233);
                        }
                    }
                }
            });

        return view;
    }

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!= Activity.RESULT_OK){
            return;
        }

        if(resultCode == Activity.RESULT_OK && requestCode == 123) {
            int cSharingStatus = -1;
            if (data != null) {
                cSharingStatus = data.getIntExtra("nSharingStatus", 11);
            }

            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).contactOrEntityID == tmp_contactId && cSharingStatus != -1) {
                    int originalStatus = adapter.getItem(i).nSharingStatus;
                    if (cSharingStatus != 11)
                        adapter.getItem(i).isPending = true;
                    else
                        adapter.getItem(i).isPending = false;
                    break;
                }
            }
            notifyChanged();
        }

        if (requestCode == 2000) {
            SharingBean sb = (SharingBean) data
                    .getSerializableExtra("shareBean");
            final Integer contactUid = data.getIntExtra("contact_uid", -1);
            CBRequest.sendRequest(contactUid, null, sb.getSharingStatus(),
                    sb.getSharedHomeFieldIds(), sb.getSharedWorkFieldIds(),
                    new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {

                        }
                    });

        }

    }
    public void addItem(SproutSearchItem item) {
        this.adapter.add(item);
        allItems.add(item);
    }

    public void setListItems(List<SproutSearchItem> items)
    {
        this.adapter.clear();
        this.adapter.addAll(items);
        if (allItems != null)
            allItems.clear();
        allItems.addAll(items);
    }

    public int getItemCount()
    {
        //return adapter==null?0:adapter.getCount();
        return allItems==null?0:allItems.size();
    }

    public SproutSearchItem getItem(int Index)
    {
        return allItems.get(Index);
    }

    public int getVisibleItemCount()
    {
        int count = 0;
        for (int i=0; i<adapter.getCount(); i++)
        {
            SproutSearchItem item = adapter.getItem(i);
            if (item.isVisible())
                count++;
        }

        return count;
    }

    public void notifyChanged() {
        if(listView!=null && listView.getAdapter()!=null)
         this.adapter.notifyDataSetChanged();
    }

    public void clearAll() {
        this.adapter.clear();
        if (allItems != null)
            allItems.clear();
    }

}
