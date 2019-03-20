package com.ginko.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.exchange.ShareingLeafDialog;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.activity.sprout.GetSproutAddressFromLatlngTask;
import com.ginko.activity.sprout.SproutExchangedItemView;
import com.ginko.activity.sprout.SproutSearchItem;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.GeoLibrary;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.ProperListView;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.SharingBean;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.view.ext.SelectableListAdapter;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserEntityProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SproutExchangedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SproutExchangedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SproutExchangedFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ProperListView listView;

    private SelectableListAdapter<SproutSearchItem> adapter;
    private ArrayList<SproutSearchItem> allItems = new ArrayList<SproutSearchItem>();
    private ImageLoader imgLoader;

    private boolean isListSelectable = false;
    private SelectableListAdapter.ItemSelectedListener<SproutSearchItem> selectionListener;

    private boolean isUILoaded = false;

    private UserEntityProfileVO entity;

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
    public void removeSelectedItemsFromList()
    {
        if(adapter != null)
        {
            for (int i = 0; i < adapter.getSelectedItems().size(); i++) {
                if (allItems != null)
                    allItems.remove(adapter.getSelectedItems().get(i));
                adapter.remove(adapter.getSelectedItems().get(i));
            }
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
        }
    }

    public synchronized void removeItem(SproutSearchItem item)
    {
        if(adapter != null)
        {
            if (getActivity() == null)
                return;
            adapter.remove(item);
            adapter.notifyDataSetChanged();
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
                    item.setVisible(true);
                else
                    item.setVisible(false);
            }
        }

        adapter.notifyDataSetChanged();
    }
    */

    // Logic for Quick Search
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
    public static SproutExchangedFragment newInstance(SelectableListAdapter.ItemSelectedListener<SproutSearchItem> listener) {
        SproutExchangedFragment fragment = new SproutExchangedFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        fragment.setListViewSelectListener(listener);
        return fragment;
    }

    public SproutExchangedFragment() {
        // Required empty public constructor
        adapter = new SelectableListAdapter<SproutSearchItem>(this.getActivity()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SproutSearchItem item = getItem(position);

                SproutExchangedItemView view = null;
                if (convertView == null) {
                    view = new SproutExchangedItemView(getContext() , item);
                }
                else
                {
                    view = (SproutExchangedItemView)convertView;
                }

                view.setIsListSelectable(isListSelectable);

                if(item.strAddress.equals("") && item.lat!=0.0d && item.lng!=0.0d && !item.isGettingAddress) {
                    GetSproutAddressFromLatlngTask getAddressFromLatlngTask = new GetSproutAddressFromLatlngTask(getActivity() , item , adapter);
                    getAddressFromLatlngTask.start();
                }

                //check box
                ImageView imgCheck = (ImageView) view.findViewById(R.id.imageSelectionCheck);

                if(isListSelectable)
                    imgCheck.setVisibility(View.VISIBLE);
                else
                    imgCheck.setVisibility(View.GONE);

                if(item.isSelected())
                {
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
        isUILoaded = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sprout_exchanged, container, false);

        listView = (ProperListView) view.findViewById(R.id.list);

        adapter.setSelectedListener(this.selectionListener);

        listView.setAdapter(adapter);
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
                    if (item.contactType == 1)//purple contact
                    {
                        if (item.nSharingStatus > 0 && item.nSharingStatus < 4)//1:home , 2:work , 3: both
                        {
                            /*
                            Intent purpleContactProfileIntent = new Intent(getActivity(), PurpleContactProfile.class);
                            purpleContactProfileIntent.putExtra("fullname", item.entityOrContactName);
                            purpleContactProfileIntent.putExtra("contactID", String.valueOf(item.contactOrEntityID));
                            startActivity(purpleContactProfileIntent);
                            */

                            final String strContactId = String.valueOf(item.contactOrEntityID);
                            final String strFullName = item.entityOrContactName;

                            final Intent purpleContactProfileIntent = new Intent(getActivity(), PurpleContactProfile.class);
                            final Bundle bundle = new Bundle();
                            bundle.putString("fullname", strFullName);
                            bundle.putString("contactID", strContactId);
                            UserRequest.getContactDetail(String.valueOf(strContactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                                @Override
                                public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                    if (response.isSuccess()) {
                                        PurpleContactWholeProfileVO responseData = response.getData();
                                        bundle.putSerializable("responseData", responseData);
                                        bundle.putString("StartActivity", "GinkoMe");
                                        purpleContactProfileIntent.putExtras(bundle);
                                        getActivity().startActivityForResult(purpleContactProfileIntent, 3322);
                                    } else {
                                        if (response.getErrorCode() == 350)//The contact can't be found.
                                        {
                                            /*
                                            Intent contactSharingSettingIntent = new Intent(getActivity(), ShareYourLeafActivity.class);
                                            contactSharingSettingIntent.putExtra("contactID", String.valueOf(strContactId));
                                            contactSharingSettingIntent.putExtra("contactFullname", strFullName);
                                            contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                            startActivity(contactSharingSettingIntent);*/

                                        } else {
                                            MyApp.getInstance().getContactsModel().deleteContactWithContactId(item.contactOrEntityID);
                                            MyApp.getInstance().removefromContacts(item.contactOrEntityID);
                                            adapter.remove(item);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        } else if (item.nSharingStatus == 4) {
                            MyApp.getInstance().showSimpleAlertDiloag(getActivity(), "Oops! Contact would like to chat only", null);
                        } else//unexchanged contact
                        {
                            Intent shareLeafIntent = new Intent(getActivity(), ShareYourLeafActivity.class);
                            shareLeafIntent.putExtra("contactID", String.valueOf(item.contactOrEntityID));
                            shareLeafIntent.putExtra("contactFullname", item.entityOrContactName);
                            shareLeafIntent.putExtra("isUnexchangedContact", true);
                            getActivity().startActivityForResult(shareLeafIntent, 2233);
                        }
                    } else if (item.contactType == 2)//grey contact
                    {
                        String strContactId = String.valueOf(item.contactOrEntityID);
                        SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject jsonRes = response.getData();
                                    Intent greyContactProfileIntent = new Intent(getActivity(), GreyContactOne.class);
                                    greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                    startActivity(greyContactProfileIntent);
                                }
                            }
                        });
                    } else if (item.contactType == 3)//entity
                    {
                        final boolean isFollowed = item.isFollowed;
                        final int contactID = item.contactOrEntityID;
                        final SproutSearchItem finalItem = item;

                        EntityRequest.viewEntity(item.contactOrEntityID, new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject jsonObj = response.getData();
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
                                        getActivity().startActivityForResult(entityProfileIntent, 980);
                                    } else {
                                        Intent entityProfileIntent = new Intent(getActivity(), UserEntityProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entity);
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        getActivity().startActivityForResult(entityProfileIntent, 980);
                                    }
                                }
                                {
                                    if (response.getErrorCode() == 700 && response.getErrorMessage().equals("The entity can't be found.")) {
                                        MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactID);
                                        MyApp.getInstance().removefromContacts(contactID);
                                        adapter.remove(finalItem);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }, true);
                    }
                }
            }
        });

        isUILoaded = true;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUILoaded = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isUILoaded = false;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!= Activity.RESULT_OK){
            return;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }



}
