package com.ginko.activity.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.activity.profiles.UserEntityMultiLocationsProfileActivity;
import com.ginko.activity.profiles.UserEntityProfileActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserEntityProfileVO;
import com.google.maps.android.MarkerManager;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchContactActivity extends MyBaseActivity implements View.OnClickListener {

    /*UI Elements */
    private final int PURPLE_ACTIVITY_START = 100;
    private final int GREY_ACTIVITY_START = 101;

    private ImageView btnClose, btnCancelSearch;
    private Button btnCancel;
    private LinearLayout activityRootView;
    private EditText edtSearch;
    private ListView contactsListView;

    /* Variables */
    private boolean isKeyboardVisible = false;
    private boolean isSearchingContacts = false;

    private int nPageNum = 0;
    private final int COUNT_PER_PAGE = 50;
    private String strSearchKeyword = "";

    private UserEntityProfileVO entity;

    private SearchContactsListAdapter mAdpater;
    //private ArrayList<FragmentContactItem> localContactsList;

    private int tmp_contactId = 0;
    private int tmp_type = 0;

    private boolean isLoadingEnded = true;
    private ContactChangeReceiver contactChangeReceiver; private boolean isContactChangeReceiverRegistered = false;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);

        //loadLocalContactsFromDB();
        this.contactChangeReceiver = new ContactChangeReceiver();
        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        getUIObjects();
    }

    /*
    private void loadLocalContactsFromDB()
    {
        localContactsList = new ArrayList<FragmentContactItem>();
        if(MyApp.g_contactItems != null)
        {
            for(int i=0; i< MyApp.g_contactItems.size(); i++) {
                final FragmentContactItem item = MyApp.g_contactItems.get(i);
                final FragmentContactItem searchItem = new FragmentContactItem();

                if (item.getContactType() == 3) //entity
                {
                    searchItem.contactType = 3;
                    searchItem.entityOrContactId = item.getContactId();
                    searchItem.entityName = item.getFullName();
                    if(searchItem.entityName.trim().equals(""))
                        continue;
                    searchItem.photoUrl = item.getProfileImage();
                    EntityRequest.getFollowerTotal(searchItem.entityOrContactId, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                JSONObject json = response.getData();
                                searchItem.isFollowed = json.optBoolean("is_followed", false);
                                searchItem.nFollowerCount = json.optInt("follower_total", 0);
                                localContactsList.add(searchItem);
                            }
                        }
                    }, false);
                } else//user
                {
                    searchItem.contactType = item.getContactType();
                    searchItem.entityOrContactId = item.getContactId();
                    searchItem.contactFirstName = item.getFirstName();
                    searchItem.contactLastName = item.getLastName();
                    if(searchItem.contactFirstName.trim().equals("") && searchItem.contactLastName.trim().equals(""))
                        continue;
                    if(searchItem.contactType == 1)//purple contact
                    {
                        searchItem.nSharingStatus = item.getSharingStatus();//5 is unkown status , not exchanged
                    }
                    else//grey contact
                    {
                        //continue;
                        searchItem.nSharingStatus = item.getSharingStatus();
                    }
                    searchItem.photoUrl = item.getProfileImage();
                    localContactsList.add(searchItem);
                }
            }
        }

    }
    */

    @Override
    protected void getUIObjects() {
        super.getUIObjects();

        activityRootView = (LinearLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        edtSearch.setCursorVisible(true);
                        btnCancel.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        edtSearch.setCursorVisible(false);
                        btnCancel.setVisibility(View.GONE);
                    }
                }
            }
        });

        activityRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.getInstance().hideKeyboard(activityRootView);
            }
        });

        btnClose = (ImageView) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch);   btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                btnCancelSearch.setVisibility(View.GONE);
            }
        });

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                edtSearch.setFocusable(false);
                edtSearch.setFocusableInTouchMode(true);
                btnCancel.setVisibility(View.GONE);
                MyApp.getInstance().hideKeyboard(activityRootView);
            }
        });
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {

                                             @Override
                                             public void beforeTextChanged(CharSequence s, int start, int count,
                                                                           int after) {
                                             }

                                             @Override
                                             public void onTextChanged(CharSequence s, int start, int before,
                                                                       int count) {
                                                 if (isSearchingContacts) return;

                                                 if (s.length() > 0) {
                                                     btnCancelSearch.setVisibility(View.VISIBLE);
                                                 } else {
                                                     btnCancelSearch.setVisibility(View.GONE);
                                                 }
                                                 searchInLocalContacts(250);
                                                 MyApp.getInstance().isSearched = false;
                                             }

                                             @Override
                                             public void afterTextChanged(Editable s) {
                                                 // TODO Auto-generated method stub

                                             }
                                         }
        );
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
                    String strEditText = edtSearch.getText().toString().trim();
                    MyApp.getInstance().isSearched = true;
                    if (strEditText.compareTo("") != 0) {
                        strSearchKeyword = strEditText;
                        //mAdpater.clearAll();
                        searchInLocalContacts(250);
                        searchContacts(strSearchKeyword, nPageNum, COUNT_PER_PAGE, true, true, false);
                    } else {
                        try {
                            mAdpater.clearAll();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mAdpater.notifyDataSetChanged();
                    }

                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);

                    edtSearch.setFocusable(false);
                    edtSearch.setFocusableInTouchMode(true);
                    btnCancel.setVisibility(View.GONE);

                    return true;
                }
                return false;
            }
        });

        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtSearch.setCursorVisible(true);
                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.VISIBLE);
                } else {
                    edtSearch.setCursorVisible(false);
                    btnCancelSearch.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                }
            }
        });

        contactsListView = (ListView) findViewById(R.id.contactList);
        mAdpater = new SearchContactsListAdapter(this);
        contactsListView.setAdapter(mAdpater);

        contactsListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MyApp.getInstance().hideKeyboard(activityRootView);
                return false;
            }
        });

        contactsListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                final ContactItem item = (ContactItem) mAdpater.getItem(position);
                if (!isLoadingEnded) return;

                tmp_contactId = item.getContactId();
                tmp_type = item.getContactType();

                if(item.getContactType() == 1)//purple contact
                {
                    if (item.getPending() == true) {
                        Intent shareLeafIntent = new Intent(SearchContactActivity.this , ShareYourLeafActivity.class);
                        shareLeafIntent.putExtra("contactID" , String.valueOf(item.getContactId()));
                        shareLeafIntent.putExtra("contactFullname" , item.getFirstName() + " " + item.getLastName());
                        shareLeafIntent.putExtra("shared_home_fids", item.getSharedHomeFields());
                        shareLeafIntent.putExtra("shared_work_fids", item.getSharedWorkFields());
                        shareLeafIntent.putExtra("sharing_status", item.getSharingStatus());
                        shareLeafIntent.putExtra("isInviteContact", true);
                        shareLeafIntent.putExtra("isUnexchangedContact", true);
                        shareLeafIntent.putExtra("isPendingRequest", true);
                        startActivity(shareLeafIntent);
                    }
                    else {
                        if(item.getSharingStatus() > 0 && item.getSharingStatus()<=3)//1:home , 2:work , 3: both
                        {
                            final String strContactId = String.valueOf(item.getContactId());
                            final int intContactId = item.getContactId();
                            final String strFullName =  item.getFirstName() + " " + item.getLastName();
                            final Intent purpleContactProfileIntent = new Intent(SearchContactActivity.this, PurpleContactProfile.class);
                            final Bundle bundle = new Bundle();
                            bundle.putString("fullname", strFullName);
                            bundle.putString("contactID", strContactId);
                            isLoadingEnded = false;
                            UserRequest.getContactDetail(String.valueOf(strContactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                                @Override
                                public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                    if (response.isSuccess()) {
                                        PurpleContactWholeProfileVO responseData = response.getData();
                                        bundle.putSerializable("responseData", responseData);
                                        bundle.putString("StartActivity", "ContactMain");
                                        purpleContactProfileIntent.putExtras(bundle);
                                        startActivityForResult(purpleContactProfileIntent, PURPLE_ACTIVITY_START);
                                    } else {
                                        if (response.getErrorCode() == 350)//The contact can't be found.
                                        {
                                        /*
                                        Intent contactSharingSettingIntent = new Intent(SearchContactActivity.this, ShareYourLeafActivity.class);
                                        contactSharingSettingIntent.putExtra("contactID", strContactId);
                                        contactSharingSettingIntent.putExtra("contactFullname", strFullName);
                                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                        startActivity(contactSharingSettingIntent); */
                                            MyApp.getInstance().getContactsModel().deleteContactWithContactId(intContactId);
                                            MyApp.getInstance().removefromContacts(intContactId);
                                            searchInLocalContacts(250);

                                        } else {
                                        }
                                    }

                                    isLoadingEnded = true;
                                }
                            });
                        }
                        else if(item.getSharingStatus() == 4)//chat only
                        {
                            Intent intent = new Intent(SearchContactActivity.this,ImBoardActivity.class);
                            intent.putExtra("contact_name", item.getFirstName() + " " + item.getLastName());
                            intent.putExtra("contact_ids", String.valueOf(item.getContactId()) + "");
                            startActivity(intent);
                        }
                        else//unexchanged contact
                        {
                            Intent shareLeafIntent = new Intent(SearchContactActivity.this , ShareYourLeafActivity.class);
                            shareLeafIntent.putExtra("contactID" , String.valueOf(item.getContactId()));
                            shareLeafIntent.putExtra("contactFullname" , item.getFirstName() + " " + item.getLastName());
                            shareLeafIntent.putExtra("shared_home_fids", item.getSharedHomeFields());
                            shareLeafIntent.putExtra("shared_work_fids", item.getSharedWorkFields());
                            shareLeafIntent.putExtra("sharing_status", item.getSharingStatus());
                            shareLeafIntent.putExtra("isUnexchangedContact" , true);
                            shareLeafIntent.putExtra("isPendingRequest", false);
                            shareLeafIntent.putExtra("isInviteContact", true);
                            startActivity(shareLeafIntent);
                        }
                    }

                }
                else if(item.getContactType() == 2)//grey contact
                {
                    String strContactId = String.valueOf(item.getContactId());
                    isLoadingEnded = false;
                    SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {

                                JSONObject jsonRes = response.getData();
                                Intent greyContactProfileIntent = new Intent(SearchContactActivity.this, GreyContactOne.class);
                                greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                startActivityForResult(greyContactProfileIntent, GREY_ACTIVITY_START);
                            }
                            isLoadingEnded = true;
                        }
                    });
                }
                else if(item.getContactType() == 3)//entity
                {
                    final boolean isFollowed = item.getFollowed();
                    final int contactID = item.getContactId();

                    isLoadingEnded = false;
                    EntityRequest.viewEntity(item.getContactId(), new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                try{
                                    JSONObject jsonObj = response.getData();
                                    JSONObject jData = null;
                                    try {
                                        entity = JsonConverter.json2Object(
                                                (JSONObject) jsonObj, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);
                                    }
                                    catch (JsonConvertException e) {
                                        e.printStackTrace();
                                        entity = null;
                                    }
                                    if (entity.getInfos().size() > 1){
                                        Intent entityProfileIntent = new Intent(SearchContactActivity.this, UserEntityMultiLocationsProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entity);
                                        entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                        entityProfileIntent.putExtra("isFavorite", jsonObj.getBoolean("is_favorite"));
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        startActivity(entityProfileIntent);
                                    }else{
                                        Intent entityProfileIntent = new Intent(SearchContactActivity.this, UserEntityProfileActivity.class);
                                        entityProfileIntent.putExtra("entityJson", entity);
                                        entityProfileIntent.putExtra("contactID", jsonObj.getInt("entity_id"));
                                        entityProfileIntent.putExtra("isFavorite", jsonObj.getBoolean("is_favorite"));
                                        entityProfileIntent.putExtra("isfollowing_entity", isFollowed);
                                        startActivity(entityProfileIntent);
                                    }
                                }catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }

                            } else if (response.getErrorCode() == 700)//The Entity can't be found.
                            {
                                MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactID);
                                MyApp.getInstance().removefromContacts(contactID);
                                mAdpater.removeItem(item);
                                mAdpater.notifyDataSetChanged();
                                //searchInLocalContacts(250);

                            }

                            isLoadingEnded = true;
                        }
                    }, true);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK && data!=null)
        {
        }
        /*if(resultCode == Activity.RESULT_OK && requestCode == 123) {
            for (int i = 0; i < mAdpater.getCount(); i++) {
                FragmentContactItem FragmentContactItem = (FragmentContactItem) mAdpater.getItem(i);
                if (FragmentContactItem.entityOrContactId == tmp_contactId) {
                    if (FragmentContactItem.isPending == true)
                        FragmentContactItem.isPending = false;
                    else
                        FragmentContactItem.isPending = true;
                }
            }
            mAdpater.notifyDataSetChanged();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(contactChangeReceiver == null)
            this.contactChangeReceiver = new ContactChangeReceiver();
        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        String strEditText = edtSearch.getText().toString().trim();
        if (strEditText.compareTo("") != 0) {
            strSearchKeyword = strEditText;
            if (MyApp.getInstance().isSearched == true)
            {
                mAdpater.clearAll();
                searchInLocalContacts(250);
                searchContacts(strSearchKeyword, nPageNum, COUNT_PER_PAGE, true, true, false);
            } else
            {
                searchInLocalContacts(250);
            }
        } else {
            try {
                mAdpater.clearAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAdpater.notifyDataSetChanged();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        //when keyboard is open , hide keyboard
        //MyApp.getInstance().hideKeyboard(activityRootView);
        if (this.contactChangeReceiver != null && isContactChangeReceiverRegistered == true) {
            unregisterReceiver(this.contactChangeReceiver);
            isContactChangeReceiverRegistered = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //close button
            case R.id.btnClose:
                hideKeyboard();
                SearchContactActivity.this.finish();
                break;
        }
    }

    private synchronized  void searchInLocalContacts(final float durationMs)
    {
        //final float oldScale = getScale();
        final long startTime = System.currentTimeMillis();

        mHandler.post(new Runnable() {
            public void run() {

                long now = System.currentTimeMillis();
                float currentMs = Math.min(durationMs, now - startTime);
                //float target = oldScale + (incrementPerMs * currentMs);

                String strSearchKeyword = edtSearch.getText().toString().toLowerCase().trim();

                try {
                    mAdpater.clearAll();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }

                List<ContactItem> localContactsList = MyApp.getInstance().g_contactItems;
                if(!strSearchKeyword.equals("") && localContactsList != null)
                {
                    //sortGroupByName(localContactsList);

                    for(ContactItem item : localContactsList)
                    {
                        if(item.getContactType() == 3)//entity
                        {
                            if(item.getFullName().toLowerCase().contains(strSearchKeyword)) {
                                mAdpater.addItem(item);
                            }
                        }
                        else
                        {
                            if(item.getFirstName().toLowerCase().contains(strSearchKeyword) || item.getLastName().toLowerCase().contains(strSearchKeyword) )
                            {
                                mAdpater.addItem(item);
                            }
                        }
                    }
                }

                List<ContactItem> sortItems = mAdpater.getAllItems();
                if (sortItems != null && sortItems.size() > 0)
                {
                    ContactItemComparator contactItemComparator = new ContactItemComparator(SearchContactActivity.this, true);
                    try {
                        Collections.sort(sortItems, contactItemComparator);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mAdpater.notifyDataSetChanged();
                if (currentMs < durationMs) {
                    mHandler.post(this);
                }
            }
        });
    }

    private void searchContacts(String searchKeyword, int pageNum, int countPerPage, boolean bProgressDialog , boolean isClearAllList, final boolean isClear) {
        //if (isSearchingContacts) return;

        /* Modify by lee
        if(isClearAllList)
        {
            try {
                mAdpater.clearAll();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            mAdpater.notifyDataSetChanged();
        }*/

        //isSearchingContacts = false;

        UserRequest.searchContacts(searchKeyword, pageNum, countPerPage, new ResponseCallBack<List<JSONObject>>() {
            @Override
            public void onCompleted(JsonResponse<List<JSONObject>> response) {
                if(response.isSuccess())
                {
                    if (isClear)
                        mAdpater.clearAll();
                    List<JSONObject> jsonArray = response.getData();
                    List<ContactItem> searchedContacts = new ArrayList<ContactItem>();
                    int length = jsonArray.size();
                    for(int i=0;i<length;i++)
                    {
                        JSONObject obj = jsonArray.get(i);
                        try {
                            if (obj.has("entity_id")) //entity
                            {
                                ContactItem searchItem = new ContactItem(3);
                                searchItem.setContactId(obj.getInt("entity_id"));
                                searchItem.setContactType(3);
                                searchItem.setFollowed(obj.optInt("invite_status", 0) == 0 ? false : true);
                                searchItem.setnFollowerCount(obj.optInt("follower_total", 0));
                                searchItem.setEntityName(obj.optString("name"));
                                if(searchItem.getEntityName().trim().equals(""))
                                    continue;
                                searchItem.setProfileImage(obj.optString("profile_image" , ""));

                                if (!MyApp.g_contactIDs.contains(searchItem.getContactId()))
                                    searchedContacts.add(searchItem);
                            } else//user
                            {
                                //Add by lee for GAD1046
                                if(MyApp.getInstance().getUserId() == obj.getInt("contact_id"))
                                    continue;
                                ////////////////////////
                                ContactItem searchItem = new ContactItem(obj.optInt("contact_type" , 1));
                                searchItem.setContactType(obj.optInt("contact_type" , 1));
                                searchItem.setContactId(obj.getInt("contact_id"));
                                searchItem.setFirstName(obj.optString("first_name", ""));
                                searchItem.setLastName(obj.optString("last_name" , ""));

                                if(searchItem.getFirstName().trim().equals("") && searchItem.getLastName().trim().equals(""))
                                    continue;
                                if(searchItem.getContactType() == 1)//purple contact
                                {
                                    if (obj.has("share"))
                                    {
                                        JSONObject objShare = obj.getJSONObject("share");
                                        searchItem.setSharingStatus(objShare.optInt("sharing_status", 5));//5 is unkown status , not exchanged
                                        searchItem.setPending(objShare.optBoolean("is_pending", false));
                                        searchItem.setSharedHomeFields(objShare.optString("shared_home_fids"));
                                        searchItem.setSharedWorkFields(objShare.optString("shared_work_fids"));
                                    }
                                    else
                                    {
                                        searchItem.setSharingStatus(5);
                                        searchItem.setPending(false);
                                    }
                                }
                                else//grey contact
                                {

                                }
                                searchItem.setProfileImage(obj.optString("profile_image" , ""));
                                if (!MyApp.g_contactIDs.contains(searchItem.getContactId()))
                                    searchedContacts.add(searchItem);
                            }
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    mAdpater.addItems(searchedContacts);
                }
                isSearchingContacts = false;

                List<ContactItem> sortItems = mAdpater.getAllItems();
                if (sortItems != null && sortItems.size() > 0)
                {
                    ContactItemComparator contactItemComparator = new ContactItemComparator(SearchContactActivity.this, true);
                    try {
                        Collections.sort(sortItems, contactItemComparator);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mAdpater.notifyDataSetChanged();
            }
        }, bProgressDialog);
    }

    private class SearchContactsListAdapter extends BaseAdapter {
        private List<ContactItem> contactsList;
        private Context mContext = null;

        public SearchContactsListAdapter(Context context) {
            contactsList = new ArrayList<ContactItem>();
            this.mContext = context;
        }

        public List<ContactItem> getAllItems() {return contactsList;}

        public void setListItems(List<ContactItem> list) {
            this.contactsList = list;
        }

        public void addItem(ContactItem contactItem)
        {
            contactsList.add(contactItem);
        }

        public void removeItem(ContactItem contactItem)
        {
            contactsList.remove(contactItem);
        }

        public void addItems(List<ContactItem> contactItems)
        {
            for(int i=0;i<contactItems.size();i++)
            {
                contactsList.add(contactItems.get(i));
            }
        }

        public void clearAll() {
            if (contactsList != null)
            {
                try
                {
                    contactsList.clear();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                contactsList = new ArrayList<ContactItem>();
            }
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return contactsList == null ? 0 : contactsList.size();
        }

        @Override
        public Object getItem(int position) {
            return contactsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactItemView itemView = null;
            ContactItem item = contactsList.get(position);
            if(itemView == null)
            {
                itemView = new ContactItemView(mContext , item);
            }
            else
            {
                itemView = (ContactItemView)convertView;
            }

            itemView.setItem(item);
            itemView.refreshView();

            return itemView;
        }
    }

    private class ContactItemView extends LinearLayout {
        private Context mContext = null;
        private ContactItem item;
        private LayoutInflater inflater;
        private ImageLoader imgLoader;

        private NetworkImageView imgPhoto;
        private TextView txtContactName;
        private TextView txtFollowers;
        private ImageView imgContactStatus, imgContactStatus1, imgEntityStatus;

        private List<String> phones = new ArrayList<String>();

        public ContactItemView(Context context, ContactItem contactItem) {
            super(context);
            this.mContext = context;
            this.item = contactItem;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.search_contact_item , this , true);

            imgPhoto = (NetworkImageView)findViewById(R.id.photo);
            txtContactName = (TextView)findViewById(R.id.txtContactName);
            imgContactStatus = (ImageView)findViewById(R.id.imgContactStatus);
            imgContactStatus1 = (ImageView)findViewById(R.id.imgContactStatus1);
            imgEntityStatus = (ImageView)findViewById(R.id.imgEntityStatus);
            txtFollowers = (TextView)findViewById(R.id.txtFollowers);

            /*imgContactStatus1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeyboard();
                    if(item.contactType != 3 || phones == null)
                    {
                        ContactStruct struct = MyApp.getInstance().getContactsModel().getContactById(item.entityOrContactId);
                        String jsonValue = struct.getJsonValue();
                        if(jsonValue !=null && jsonValue.compareTo("") != 0)
                        {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonValue);
                                JSONArray jsonArray = jsonObject.getJSONArray("phones");
                                final List<String> m_phones = new ArrayList<String>();
                                if(jsonArray != null) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        if(jsonArray.optString(i).contains("@"))
                                            continue;
                                        m_phones.add(jsonArray.optString(i));

                                    }
                                }
                                phones = m_phones;
                                phones.add("Cancel");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    /////////////////////////////////////////////////////////////////
                    if (phones.size() == 1){
                        Uitils.alert("Oops! No registered phone numbers.");
                        return;
                    }
                    if(phones.size()>2) {
                        if (!checkSimCard(getContext()))
                            return;
                        final List<String> buttons = phones;
                        final BottomPopupWindow popupWindow = new BottomPopupWindow(mContext, buttons);
                        popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                            @Override
                            public void onClick(View button, int position) {
                                String text = buttons.get(position);
                                if (text == "Cancel") {
                                    popupWindow.dismiss();
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + text));
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                        popupWindow.show(v);
                    }
                    else
                    {
                        if (!checkSimCard(getContext()))
                            return;
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phones.get(0)));
                        mContext.startActivity(intent);
                    }
                }
            });

            imgContactStatus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.contactType == 1) {
                        if(item.nSharingStatus > 0 && item.nSharingStatus < 5) {
                            int contact_id = item.entityOrContactId;
                            String fullName = item.contactFirstName + item.contactLastName;
                            Intent intent = new Intent(mContext, ImBoardActivity.class);
                            intent.putExtra("contact_name", fullName);
                            intent.putExtra("contact_ids", contact_id+"");
                            mContext.startActivity(intent);
                        }
                    } else {
                        ContactStruct struct = MyApp.getInstance().getContactsModel().getContactById(item.entityOrContactId);
                        String jsonValue = struct.getJsonValue();
                        if(jsonValue !=null && jsonValue.compareTo("") != 0)
                        {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonValue);
                                String m_email = "";
                                m_email = jsonObject.getString("email");

                                if(!m_email.equals("")) {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_EMAIL, m_email);
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "");//subject
                                    intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n" + getResources().getString(R.string.str_send_email_bottom_suffix));


                                    mContext.startActivity(Intent.createChooser(intent, "Send Email"));
                                } else {
                                    Uitils.alert("Oops! No registered emails!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            */
        }

        public void setItem(ContactItem contactItem)
        {
            this.item = contactItem;
        }
        public ContactItem getItem(){return this.item;}

        public void refreshView()
        {
            if(imgLoader == null)
                this.imgLoader = MyApp.getInstance().getImageLoader();

            if(item.getContactType() == 3)//entity
            {
                imgPhoto.setDefaultImageResId(R.drawable.entity_dummy);
                imgPhoto.setImageUrl(item.getProfileImage() , imgLoader );

                //entity name
                txtContactName.setText(item.getEntityName());

                //show follower count
                txtFollowers.setVisibility(View.VISIBLE);
                txtFollowers.setText(String.valueOf(item.getnFollowerCount()) + " followers");

                //show entity status icon and hide contact status icon
                imgEntityStatus.setVisibility(View.VISIBLE);
                imgContactStatus.setVisibility(View.INVISIBLE);
                imgContactStatus1.setVisibility(View.INVISIBLE);

                if(item.getFollowed()) {
                    imgEntityStatus.setImageResource(R.drawable.leaf_solid);
                }
                else
                {
                    imgEntityStatus.setImageResource(R.drawable.leaf_line);
                }
            }
            else
            {

                //contact name
                txtContactName.setText(item.getFirstName() + "\n" + item.getLastName());

                //hide follower textview
                txtFollowers.setVisibility(View.INVISIBLE);

                //show contact status icon and hide entity status icon
                imgEntityStatus.setVisibility(View.INVISIBLE);
                imgContactStatus.setVisibility(View.VISIBLE);
                imgContactStatus1.setVisibility(View.VISIBLE);

                /*
                 Sharing Status
                 1 : home
                 2 : work
                 3 : both
                 4 : chat only
                 5 : not shared anything yet
                */
                if(item.getContactType() == 1)//purple contact
                {
                    imgPhoto.setDefaultImageResId(R.drawable.no_face);
                    imgPhoto.setImageUrl(item.getProfileImage(), imgLoader);

                    if (item.getPending()){
                        imgContactStatus1.setVisibility(INVISIBLE);
                        imgContactStatus.setImageResource(R.drawable.time_normal);
                    } else {
                        if (item.getSharingStatus() > 0 && item.getSharingStatus() < 4) {
                            //imgContactStatus.setImageResource(R.drawable.contact_exchanged);
                            imgContactStatus1.setImageResource(R.drawable.btnphone);
                            imgContactStatus.setImageResource(R.drawable.btnchat);
                        } else if (item.getSharingStatus() == 4) {
                            imgContactStatus1.setVisibility(INVISIBLE);
                            imgContactStatus.setImageResource(R.drawable.btnchat);
                        } else {
                            imgContactStatus1.setVisibility(INVISIBLE);
                            imgContactStatus.setImageResource(R.drawable.contact_exchanged);
                        }
                    }
                }
                else//grey contact
                {
                    imgPhoto.setDefaultImageResId(R.drawable.no_face_grey);
                    imgPhoto.setImageUrl(item.getProfileImage(), imgLoader);

                    imgPhoto.setBorderColor(mContext.getResources().getColor(R.color.grey_contact_color));

                    imgContactStatus1.setImageResource(R.drawable.btnphonegrey);
                    imgContactStatus.setImageResource(R.drawable.btnmailgrey);
                }
            }

        }
    }
    public boolean checkSimCard(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if(telephonyManager.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE) {
            MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
            return false;
        } else {
            int SIM_STATE = telephonyManager.getSimState();

            if (SIM_STATE == TelephonyManager.SIM_STATE_READY)
                return true;
            else {
                switch (SIM_STATE) {
                    case TelephonyManager.SIM_STATE_ABSENT: //SimState = "No Sim Found!";
                        MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
                        break;
                    case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = "Network Locked!";
                        break;
                    case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = "PIN Required to access SIM!";
                        break;
                    case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = "PUK Required to access SIM!"; // Personal Unblocking Code
                        break;
                    case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = "Unknown SIM State!";
                        MyApp.getInstance().showSimpleAlertDiloag(context, "No Sim-card", null);
                        break;
                }
                return false;
            }
        }
    }

    private final static Comparator<ContactItem> contactsNameComparator = new Comparator<ContactItem>()
    {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(ContactItem lhs, ContactItem rhs) {
            return collator.compare(lhs.getFirstName(), rhs.getLastName());
        }
    };

    /*
    public void sortGroupByName(ArrayList<FragmentContactItem> contacts){
        try {
            Collections.sort(contacts, contactsNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public void sortGroupByName(List<ContactItem> contacts){
        try {
            Collections.sort(contacts, contactsNameComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard()
    {
        //if(isKeyboardVisible)
        MyApp.getInstance().hideKeyboard(activityRootView);
    }

    public class ContactChangeReceiver extends BroadcastReceiver {
        public ContactChangeReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New Contact Change");

            String strEditText = edtSearch.getText().toString().trim();
            if (strEditText.compareTo("") != 0) {
                strSearchKeyword = strEditText;
                if (MyApp.getInstance().isSearched == true)
                {
                    mAdpater.clearAll();
                    searchInLocalContacts(250);
                    searchContacts(strSearchKeyword, nPageNum, COUNT_PER_PAGE, true, true, false);
                } else
                {
                    searchInLocalContacts(250);
                }
            } else {
                try {
                    mAdpater.clearAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAdpater.notifyDataSetChanged();
            }

        }
    }
}