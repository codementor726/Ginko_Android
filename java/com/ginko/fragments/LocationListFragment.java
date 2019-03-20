package com.ginko.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.EmoticonUtility;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.view.ext.SelectableListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by star on 4/22/2016.
 */
public class LocationListFragment extends Fragment {

    private ListView list;
    private SelectableListAdapter<LocationItem> adapter;

    private List<LocationItem> locations= new ArrayList<LocationItem>();
//	private View view;

    private ContactsLoader contactsLoader;

    private List<LocationItem> original = new ArrayList<LocationItem>();

    private ContactItemSelectListener contactItemClickListener;

    private String existingContactItems = "";
    private int groupId = 0;

    private boolean isShowOnlySelect = false;

    public void setExistingContactItems(String items)
    {
        this.existingContactItems = items;
    }

    public void setGroupId(int strGroupId)
    {
        this.groupId = strGroupId;
    }
    public LocationListFragment()
    {}


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        list = new ListView(this.getActivity());
        list.setDivider(null);
        list.setBackgroundResource(R.drawable.leafbgforblank);

        adapter = new SelectableListAdapter<LocationItem>(this.getActivity(),locations) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ItemView view = null;
                LocationItem item = getItem(position);
                if (convertView == null) {
                    view = new ItemView(getActivity() , item);
                }
                else
                {
                    view = (ItemView)convertView;
                }

                if(isItemVisible(position))
                {
                    view.findViewById(R.id.rootLayout).setVisibility(View.VISIBLE);
                }
                else
                {
                    view.findViewById(R.id.rootLayout).setVisibility(View.GONE);
                }


                selectItem(view, position, isSelected(position));

                view.setItem(item);
                view.refreshView();
                return view;
            }
        };

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/*boolean selected = adapter.isSelected(position);
                boolean newStatus = !selected;
                selectItem(view, position, newStatus);
                adapter.tiggel(position);
                if(contactItemClickListener!=null)
                    contactItemClickListener.onItemSelected(position , newStatus);*/

                //Modify
                if(!isShowOnlySelect)
                {
                    boolean selected = adapter.isSelected(position);
                    boolean newStatus = !selected;
                    selectItem(view, position, newStatus);
                    adapter.tiggel(position);
                    if(contactItemClickListener!=null)
                        contactItemClickListener.onItemSelected(position , newStatus);
                }
            }
        });

        list.setAdapter(adapter);

        return list;
    }

    public void setOnContactItemClickListener(ContactItemSelectListener _contactItemClickListener)
    {
        this.contactItemClickListener = _contactItemClickListener;
    }


    private void selectItem(View view, int position, boolean selected) {
        if(view == null){
            view = list.getChildAt(position);
        }
        if(view == null){
            return;
        }

        if (selected) {
            view.setBackgroundColor(0xffdfd1ed);
        } else {
            view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    public void searchSelectedItems(String searchKeyword)
    {
        if(adapter != null)
        {
            adapter.showOnlySelectedItems();
            if(searchKeyword.compareTo("") != 0)
            {
                List<LocationItem> selectedContacts = adapter.getSelectedItems();
                List<LocationItem> allContacts = adapter.getAll();
                for(int i=0;i<selectedContacts.size();i++)
                {
                    LocationItem item = selectedContacts.get(i);
                    for(int j=0;j<allContacts.size();j++)
                    {
                        if(item.user_id == allContacts.get(j).user_id)
                        {
                            if (item.contactName.toLowerCase().contains(searchKeyword)) {
                                adapter.showItem(j, true);
                            } else {
                                adapter.showItem(j, false);
                            }
                            break;
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
    public void onActivityCreated(Bundle savedInstanceState){
        this.loadContacts();
        super.onActivityCreated(savedInstanceState);
    }

    private void loadContacts() {
        ContactsLoader loader = this.getContactsLoader();
        if(loader == null){
            loader = new DefaultContactLoader();
        }
        loader.loadContacts();
    }

    protected boolean isValidContact(JSONObject jsonContact) {
        String firstName = jsonContact.optString("first_name");
        String lastName = jsonContact.optString("last_name");
        if (firstName.isEmpty() && lastName.isEmpty()) {
            return false;
        }
        return true;
    }

    public void updateList(JSONArray contacts) throws JSONException {
        List<JSONObject> jsonObjects = Uitils.toJsonList(contacts);
        updateList(jsonObjects);
    }

    public void updateList(List<JSONObject> contacts) {
        adapter.clear();
        for (int i = 0; i < contacts.size(); i++) {
            JSONObject jsonContact = contacts.get(i);
            if (!isValidContact(jsonContact)) {
                continue;
            }
            LocationItem item = new LocationItem(jsonContact);
            if(item.entity_id == -1) {
                adapter.add(item);
                original.add(item);
            }

//            if(this.existingContactItems.contains(String.valueOf(item.user_id)+","))
//                continue;

            // adapter.add(item);
            // original.add(item);
        }
        adapter.notifyDataSetChanged();
    }

    public void selectAll(boolean isSelectAll){
        if(isSelectAll)
            adapter.selectAll();
        else
            adapter.unSelectAll();
        /*for (int i = 0; i < contacts.size(); i++) {
            selectItem(null, i, isSelectAll);
        }*/
        adapter.notifyDataSetChanged();
    }

    public int getSelectedItemCount()
    {
        if(adapter!=null)
            return adapter.getSelectedItemCount();
        return 0;
    }

    public int getTotalItemCounts()
    {
        if(adapter!=null)
            return adapter.getCount();
        return 0;
    }

    public void showAllItems()
    {
        if(adapter!=null) {
            isShowOnlySelect = false;
            adapter.showAllItems();
            adapter.notifyDataSetChanged();
        }
    }

    public void showOnlySelectedItems()
    {
        if(adapter != null) {
            isShowOnlySelect = true;
            adapter.showOnlySelectedItems();
            adapter.notifyDataSetChanged();
        }
    }

    public ContactsLoader getContactsLoader() {
        return contactsLoader;
    }

    public void setContactsLoader(ContactsLoader contactsLoader) {
        this.contactsLoader = contactsLoader;
    }


    public void refreshList()
    {
        if(adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
    }


    public List<LocationItem> getSelectedContacts(){
        return this.adapter.getSelectedItems();
    }

    public void remove(List<LocationItem> contacts) {
        this.adapter.clearSelection();
        for (Iterator<LocationItem> iter = contacts.iterator(); iter.hasNext(); ) {
            LocationItem item = iter.next();
            this.locations.remove(item);
        }
        adapter.notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        return adapter.isAllselected();
    }

    public static interface ContactsLoader {
        void loadContacts();
    }

    public class DefaultContactLoader implements ContactsLoader {

        @Override
        public void loadContacts() {
//			UserRequest.getfriends(null, null, new ResponseCallBack<JSONObject>() {
//
//				@Override
//				public void onCompleted(JsonResponse<JSONObject> response) {
//					if (response.isSuccess()) {
//						try {
//							JSONArray contacts = response.getData().getJSONArray("contact");
//                            //sortJsonArray(contacts);
//							updateList(sortJsonArray(contacts));
//						} catch (JSONException e) {
//							Logger.error(e);
//						}
//					}
//				}
//			});
            ContactGroupRequest.getDontAddedContacts(groupId, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if (response.isSuccess()) {
                        try {
                            JSONArray contacts = response.getData().getJSONArray("data");
                            //sortJsonArray(contacts);
                            updateList(sortJsonArray(contacts));
                        } catch (JSONException e) {
                            Logger.error(e);
                        }
                    }
                }
            });
        }
    }

    public static JSONArray sortJsonArray(JSONArray jsonArray) {
        ArrayList<JSONObject> array = new ArrayList<JSONObject>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                array.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Collections.sort(array, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                // TODO Auto-generated method stub

                try {
                    return (lhs.getString("first_name").toLowerCase().compareTo(rhs.getString("first_name").toLowerCase()));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        return new JSONArray(array);
    }

    public interface ContactItemSelectListener
    {
        public void onItemSelected(int position ,boolean selected);
    }

    public class LocationItem
    {
        public String contactName;
        public int user_id;
        public int contact_id;
        public int entity_id;
        public int contact_type;
        public String profileImage;
        public int sharing_status;
        public LocationItem(JSONObject jsonObject)
        {
            try
            {
                this.contact_id = jsonObject.optInt("contact_id" , -1);
                this.contact_type = jsonObject.optInt("contact_type" , -1);
                this.user_id = jsonObject.optInt("user_id", -1);
                this.entity_id = jsonObject.optInt("entity_id", -1);
                String firstName = jsonObject.optString("first_name" , "");
                String middleName = jsonObject.optString("middle_name" , "");
                String lastName = jsonObject.optString("last_name" , "");
                this.profileImage = jsonObject.optString("photo_url" , "");
                this.sharing_status = jsonObject.optInt("sharing_status");
                this.contactName = firstName;
                if(middleName.compareTo("")!=0)
                    this.contactName = this.contactName +" "+middleName;
                if(lastName.compareTo("")!=0)
                    this.contactName = this.contactName +" "+lastName;
            }catch(Exception e)
            {

            }
        }
    }

    private class ItemView extends LinearLayout {
        private LayoutInflater inflater = null;
        private LocationItem item;
        private NetworkImageView profileImage;
        private TextView locationInfo;

        private Context mContext;

        private EmoticonUtility emoticons;

        private com.android.volley.toolbox.ImageLoader imgLoader;

        public ItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.mContext = context;

        }
        public ItemView(Context context,  LocationItem _item)
        {
            super(context);
            this.mContext = context;
            item  = _item;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.contact_cell, this, true);

            profileImage = (NetworkImageView)findViewById(R.id.profileImage);
            locationInfo = (TextView) findViewById(R.id.locationInfo);

        }
        public void setItem(LocationItem _item)
        {
            this.item = _item;
        }
        private void refreshView()
        {
            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            if(this.emoticons == null)
                this.emoticons = MyApp.getInstance().getEmoticonUtility();

            profileImage.setDefaultImageResId(R.drawable.no_face);
            profileImage.setImageUrl(item.profileImage , imgLoader);

            locationInfo.setText(item.contactName);
        }
    }

}
