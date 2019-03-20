package com.ginko.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.common.NamesComparator;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.im.EmoticonUtility;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.api.request.UserRequest;
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
import java.util.Iterator;
import java.util.List;

public class ContactListFragment extends Fragment {

	private ListView list;
	private SelectableListAdapter<FragmentContactItem> adapter;
	
	private List<FragmentContactItem> contacts= new ArrayList<FragmentContactItem>();
    private List<FragmentContactItem> searchedContacts = new ArrayList<FragmentContactItem>();
//	private View view;
	
	private ContactsLoader contactsLoader;

    private ArrayList<FragmentContactItem> original = new ArrayList<FragmentContactItem>();

    private ContactItemSelectListener contactItemClickListener;

    private String existingContactItems = "";
    private int groupId = 0;

    private boolean isShowOnlySelect = false;
    private boolean isGroupFrom = false;
    private boolean isAddVideoFrom = false;

    public void setContactsOriginal(ArrayList<ContactItem> data) {
        for (int i = 0; i < data.size(); i++)
        {
            ContactItem item = data.get(i);
            FragmentContactItem contact = new FragmentContactItem();
            contact.user_id = item.getContactId();
            contact.contact_type = item.getContactType();
            contact.profileImage = item.getProfileImage();
            contact.sharing_status = item.getSharingStatus();
            contact.contactName = item.getFullName();
            original.add(contact);
        }
    }

    public void setExistingContactItems(String items)
    {
        this.existingContactItems = items;
    }

    public void setGroupFrom(boolean _data) { this.isGroupFrom = _data; }

    public void setAddVideoFrom(boolean _data) { this.isAddVideoFrom = _data; }

    public void setGroupId(int strGroupId)
    {
        this.groupId = strGroupId;
    }

    public ContactListFragment()
    {}


	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            Bundle savedInstanceState) {

		list = new ListView(this.getActivity());
        list.setDivider(null);
        list.setBackgroundResource(R.drawable.leafbgforblank);

		adapter = new SelectableListAdapter<FragmentContactItem>(this.getActivity(),contacts) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                ItemView view = null;
                FragmentContactItem item = getItem(position);
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

		list.setOnItemClickListener(new OnItemClickListener() {

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
                List<FragmentContactItem> selectedContacts = adapter.getSelectedItems();
                List<FragmentContactItem> allContacts = adapter.getAll();
                for(int i=0;i<selectedContacts.size();i++)
                {
                    FragmentContactItem item = selectedContacts.get(i);
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

    public void searchItems(String searchKeyword)
    {
        List<FragmentContactItem> searchContacts = new ArrayList<FragmentContactItem>();

        if(adapter != null)
        {
            if(searchKeyword.compareTo("") == 0) {
                adapter.showAllItems();
                searchContacts = adapter.getAll();
            }
            else
            {
                List<FragmentContactItem> contacts =  adapter.getAll();
                for(int i=0;i<contacts.size();i++)
                {
                    FragmentContactItem item = contacts.get(i);

                    if(item.contactName.toLowerCase().contains(searchKeyword))
                    {
                        adapter.showItem(i, true);
                        searchContacts.add(item);
                    }
                    else{
                        adapter.showItem(i, false);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
        searchedContacts = searchContacts;
    }

    public int getSearchedItemCount(){
        if (adapter != null)
            return searchedContacts.size();
        else return 0;
    }
    public void onActivityCreated(Bundle savedInstanceState){
        if (isGroupFrom == false)
		    this.loadContacts();
        else {
            adapter.clear();
            adapter.addAll(original);
            adapter.notifyDataSetChanged();
        }
        super.onActivityCreated(savedInstanceState);
	}

	private void loadContacts() {
		ContactsLoader loader = this.getContactsLoader();
		if(loader == null){
			loader = new DefaultContactLoader();
		}
		loader.loadContacts();
    }


	public void updateList(JSONArray contacts) throws JSONException {
        List<JSONObject> jsonObjects = Uitils.toJsonList(contacts);
        updateList(jsonObjects);
	}
	
	public void updateList(List<JSONObject> contacts) {
		adapter.clear();
		for (int i = 0; i < contacts.size(); i++) {
			JSONObject jsonContact = contacts.get(i);
            FragmentContactItem item = new FragmentContactItem(jsonContact);
            if(this.existingContactItems.contains(String.valueOf(item.user_id)+","))
                continue;
            if(item.entity_id == -1) {
                if(isAddVideoFrom)
                {
                    if (item.sharing_status != 4)
                    {
                        adapter.add(item);
                        original.add(item);
                    }
                }else{
                    adapter.add(item);
                    original.add(item);
                }

            }
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

    public void setSelected(int position, boolean isSelect)
    {
        if (isSelect)
            adapter.select(position);
        else
            adapter.unSelect(position);
        adapter.notifyDataSetChanged();
    }

    public int getSelectedItemCount()
    {
        if(adapter!=null)
            return adapter.getSelectedVisibleItemCount();
        return 0;
    }

    public int getTotalItemCounts()
    {
        if(adapter!=null)
            return adapter.getCount();
        return 0;
    }

    public int getVisibleItemCount()
    {
        if (adapter != null)
            return adapter.getVisibleCount();
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


	public List<FragmentContactItem> getSelectedContacts(){
		return this.adapter.getSelectedItems();
	}

    public List<FragmentContactItem> getSelectedVisibleContacts(){
        return this.adapter.getSelectedVisibleItems();
    }

    public void remove(List<FragmentContactItem> contacts) {
        this.adapter.clearSelection();
        for (Iterator<FragmentContactItem> iter = contacts.iterator(); iter.hasNext(); ) {
            FragmentContactItem item = iter.next();
            this.contacts.remove(item);
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
            if(groupId != 0) {
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
            else {
                UserRequest.getfriends(null, null, new ResponseCallBack<JSONObject>() {

                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            try {
                                JSONArray contacts = response.getData().getJSONArray("contact");
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
	}

    public JSONArray sortJsonArray(JSONArray jsonArray) {
        ArrayList<JSONObject> array = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                array.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        NamesComparator jsonComparator = new NamesComparator(this.getActivity());
        Collections.sort(array, jsonComparator);
        return new JSONArray(array);
    }

    /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
    private static final String getChunk(String s, int slength, int marker)
    {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (Character.isDigit(c))
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (!Character.isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (Character.isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }

    public interface ContactItemSelectListener
    {
        public void onItemSelected(int position ,boolean selected);
    }


    public class FragmentContactItem
    {
        public String contactName;
        public int user_id;
        public int contact_id;
        public int entity_id;
        public int contact_type;
        public String profileImage;
        public int sharing_status;

        public FragmentContactItem()
        {

        }
        public FragmentContactItem(JSONObject jsonObject)
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
        private FragmentContactItem item;
        private NetworkImageView profileImage;
        private TextView contactName;

        private Context mContext;

        private EmoticonUtility emoticons;

        private com.android.volley.toolbox.ImageLoader imgLoader;

        public ItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.mContext = context;

        }
        public ItemView(Context context,  FragmentContactItem _item)
        {
            super(context);
            this.mContext = context;
            item  = _item;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.contact_cell, this, true);

            profileImage = (NetworkImageView)findViewById(R.id.profileImage);
            contactName = (TextView) findViewById(R.id.contactName);

            //Purple Contact Border
            if (item.contact_type == 1)
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.purple_contact_color));
                // Grey Contact Border
            else if (item.contact_type == 2)
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.grey_contact_color));

        }
        public void setItem(FragmentContactItem _item)
        {
            this.item = _item;
        }
        private void refreshView()
        {
            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            if(this.emoticons == null)
                this.emoticons = MyApp.getInstance().getEmoticonUtility();

            //Purple Contact Border
            if (item.contact_type == 1)
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.purple_contact_color));
            // Grey Contact Border
            else if (item.contact_type == 2)
                profileImage.setBorderColor(mContext.getResources().getColor(R.color.grey_contact_color));

            profileImage.setDefaultImageResId(R.drawable.no_face);
            profileImage.setImageUrl(item.profileImage , imgLoader);


            contactName.setText(item.contactName);
        }
    }

}
