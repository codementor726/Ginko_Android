package com.ginko.activity.group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.ContactItemComparator;
import com.ginko.activity.im.GroupVideoChatActivity;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.activity.im.VideoChatAddUserActivity;
import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.PurpleContactProfile;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.AlphabetSidebar;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.GroupVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserLoginVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.VideoMemberVO;
import com.hb.views.PinnedSectionListView;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupDetailActivity extends MyBaseActivity  implements View.OnClickListener,
        AlphabetSidebar.OnTouchingLetterChangedListener {

    private final int PURPLE_ACTIVITY_START = 100;
    private final int GREY_ACTIVITY_START = 101;
    private final int SHARE_YOUR_LEAF_ACTIVITY = 2;

    private RelativeLayout activityRootView, layoutBottom;
    private ImageButton btnPrev;
    private ImageView btnDeleteGroup , btnChatNav , btnClose , btnEdit, btnPermission, btnGinkoCall;
    private ImageButton btnDeleteItems , btnAddContact;
    private TextView txtTitle;
    private PinnedSectionListView listView;
    private EditText edtSearch;
    private ImageView btnCancelSearch;
    private Button btnCancel;

    private AlphabetSidebar alphabetScrollbar;

    private GroupContactListAdapter adapter;

    private GroupVO group;
    private int groupType = 0;
    private ArrayList<ContactItem> lstItems = new ArrayList<>();
    public static List<ContactItem> sltContactList = new ArrayList<>();

    private boolean isEditable = false;
    private boolean isGinkoCallAvaiable = false;
    private boolean isKeyboardVisible = false;
    private boolean isChangedContact = false;

    private String strSearchKeyword = "";

    private int m_orientHeight = 0;

    private ContactChangedReceiver contactChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        if (getIntent().getExtras()!=null){
            group = (GroupVO)getIntent().getSerializableExtra("group");
            this.groupType = group.getType();
        }

        this.isChangedContact = false;
        getUIObjects();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if(adapter!=null)//if tile style is changed
        {
            try
            {
                adapter.forceNewlyClear();
                adapter.notifyDataSetChanged();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }*/

        if(isKeyboardVisible) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }
        loadContactGroup();
        //isEditable = false;
        updateUIFromEditable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShownKeyboard();

        if(!isKeyboardVisible) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            btnCancel.setVisibility(View.GONE);
        }
        else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }

        if (this.contactChangedReceiver != null && isChangedContact == true) {
            unregisterReceiver(this.contactChangedReceiver);
            isChangedContact = false;
        }
    }

    private void hideKeyboard()
    {
        //if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);
    }

    private void showKeyboard()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void isShownKeyboard() {
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int curheight= rectgle.bottom;
        if(m_orientHeight == curheight)
            isKeyboardVisible = false;
        else
            isKeyboardVisible = true;
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        activityRootView = (RelativeLayout) findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        btnCancel.setVisibility(View.VISIBLE);
                        edtSearch.setCursorVisible(true);
                        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        btnCancel.setVisibility(View.GONE);
                        edtSearch.setCursorVisible(false);
                        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                }
            }
        });

        layoutBottom = (RelativeLayout)findViewById(R.id.layout_bottom);

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnCancelSearch.setVisibility(View.GONE);
                // For GAD-1231
                // hideKeyboard();
                showKeyboard();
            }
        });
        edtSearch.addTextChangedListener(new TextWatcher() {

                                             @Override
                                             public void beforeTextChanged(CharSequence s, int start, int count,
                                                                           int after) {
                                             }

                                             @Override
                                             public void onTextChanged(CharSequence s, int start, int before,
                                                                       int count) {
                                                 if (s.length() > 0)
                                                     btnCancelSearch.setVisibility(View.VISIBLE);
                                                 else
                                                     btnCancelSearch.setVisibility(View.GONE);
                                                 btnCancel.setVisibility(View.VISIBLE);
                                                 searchItems();
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
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                    //searchItems();
                    btnCancel.setVisibility(View.GONE);
                    activityRootView.setFocusable(true);
                    activityRootView.requestFocus();
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
                    btnCancel.setVisibility(View.VISIBLE);
                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                } else {
                    edtSearch.setCursorVisible(false);
                    btnCancel.setVisibility(View.GONE);
//                    btnCancelSearch.setVisibility(View.GONE);
                }
            }
        });

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtTitle.setText(group.getName());

        findViewById(R.id.btnAddContact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this,GroupAddUserActivity.class);
                intent.putExtra("group_id",group.getId());
                startActivity(intent);
            }
        });

        listView = (PinnedSectionListView) findViewById(R.id.list);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setFastScrollEnabled(false);
        listView.setFastScrollAlwaysVisible(false);

        alphabetScrollbar = (AlphabetSidebar)findViewById(R.id.alphabetScrollbar);
        alphabetScrollbar.setOnTouchingLetterChangedListener(this);

        adapter = new GroupContactListAdapter(this);
        if (groupType == 2)
            adapter.setIsDirectory(true);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO go to the group list detail view.
                if (adapter.isSectionView(position))
                    return;
                if (isEditable) {
                    adapter.tiggel(position);
                    adapter.notifyDataSetChanged();
                    showTrashIsSelectedContact();
                } else {
                    final ContactItem contactItem = (ContactItem) adapter.getItem(position);
                    if (contactItem.isSection()) return;
                    if (groupType != 2) {
                        if (!contactItem.getIsRead() && contactItem.getContactType() != 3) {
                            contactItem.setIsRead(true);
                            final ContactItem item = contactItem;
                            adapter.notifyDataSetChanged();
                            UserRequest.readContact(new Integer(contactItem.getContactId()), contactItem.getContactType(), new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        //MyApp.getInstance().getContactsModel().update(item);
                                    }
                                }
                            }, false);

                        }
                        if (contactItem.getContactType() == 1)//purple contact
                        {
                            if (contactItem.getSharingStatus() == 4) {
                        /*Intent purpleContactProfileIntent = new Intent(ContactMainActivity.this, ShareYourLeafActivity.class);
                        purpleContactProfileIntent.putExtra("contactFullname", contactItem.getFullName());
                        purpleContactProfileIntent.putExtra("contactID", String.valueOf(contactItem.getContactId()));
                        purpleContactProfileIntent.putExtra("isUnexchangedContact" , false);
                        startActivity(purpleContactProfileIntent);*/
                                MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, "Oops! Contact would like to chat only", null);
                            } else {
                                final String strContactId = String.valueOf(adapter.getItem(position).getContactId());
                                final int ContactID = adapter.getItem(position).getContactId();
                                final String strFullName = adapter.getItem(position).getFullName();

                                final Intent purpleContactProfileIntent = new Intent(GroupDetailActivity.this, PurpleContactProfile.class);
                                final Bundle bundle = new Bundle();
                                bundle.putString("fullname", strFullName);
                                bundle.putString("contactID", strContactId);
                                UserRequest.getContactDetail(String.valueOf(strContactId), "1", new ResponseCallBack<PurpleContactWholeProfileVO>() {
                                    @Override
                                    public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                        if (response.isSuccess()) {
                                            PurpleContactWholeProfileVO responseData = response.getData();
                                            bundle.putSerializable("responseData", responseData);
                                            purpleContactProfileIntent.putExtras(bundle);
                                            startActivityForResult(purpleContactProfileIntent, PURPLE_ACTIVITY_START);
                                        } else {
                                            if (response.getErrorCode() == 350)//The contact can't be found.
                                            {
                                                MyApp.getInstance().getContactsModel().deleteContactWithContactId(ContactID);
                                                MyApp.getInstance().removefromContacts(ContactID);
                                                adapter.remove(contactItem);
                                                adapter.notifyDataSetChanged();
                                            /*
                                            Intent contactSharingSettingIntent = new Intent(GroupDetailActivity.this, ShareYourLeafActivity.class);
                                            contactSharingSettingIntent.putExtra("contactID", strContactId);
                                            contactSharingSettingIntent.putExtra("contactFullname", strFullName);
                                            contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                                            startActivity(contactSharingSettingIntent); */

                                            } else {
                                            }
                                        }
                                    }
                                });
                            }
                        } else if (contactItem.getContactType() == 2)//grey contact
                        {
                            String strContactId = String.valueOf(contactItem.getContactId());
                            SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                                @Override
                                public void onCompleted(JsonResponse<JSONObject> response) {
                                    if (response.isSuccess()) {
                                        JSONObject jsonRes = response.getData();
                                        Intent greyContactProfileIntent = new Intent(GroupDetailActivity.this, GreyContactOne.class);
                                        greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                        startActivityForResult(greyContactProfileIntent, GREY_ACTIVITY_START);
                                    }
                                }
                            });
                        }
                    } else {
                        final String strContactId = String.valueOf(adapter.getItem(position).getContactId());
                        final int ContactID = adapter.getItem(position).getContactId();
                        final String strFullName = adapter.getItem(position).getFullName();

                        if (ContactID == MyApp.getInstance().getUserId()) {
                            Intent contactSharingSettingIntent = new Intent(GroupDetailActivity.this, ShareYourLeafActivity.class);
                            contactSharingSettingIntent.putExtra("contactID", "0");
                            contactSharingSettingIntent.putExtra("contactFullname", group.getName());
                            contactSharingSettingIntent.putExtra("isDirectory", true);
                            contactSharingSettingIntent.putExtra("directoryID", group.getGroup_id());
                            contactSharingSettingIntent.putExtra("isUnexchangedContact", false);
                            contactSharingSettingIntent.putExtra("isInviteContact", true);
                            contactSharingSettingIntent.putExtra("isPendingRequest", true);
                            contactSharingSettingIntent.putExtra("StartActivity", "ContactMain");

                            startActivityForResult(contactSharingSettingIntent, SHARE_YOUR_LEAF_ACTIVITY);
                        } else {
                            final Intent purpleContactProfileIntent = new Intent(GroupDetailActivity.this, PurpleContactProfile.class);
                            final Bundle bundle = new Bundle();
                            bundle.putString("fullname", strFullName);
                            bundle.putString("contactID", strContactId);
                            DirectoryRequest.getMemberDetail(group.getGroup_id(), ContactID, new ResponseCallBack<PurpleContactWholeProfileVO>() {
                                @Override
                                public void onCompleted(JsonResponse<PurpleContactWholeProfileVO> response) {
                                    if (response.isSuccess()) {
                                        PurpleContactWholeProfileVO responseData = response.getData();
                                        bundle.putSerializable("responseData", responseData);
                                        bundle.putBoolean("isDirectory", true);
                                        purpleContactProfileIntent.putExtras(bundle);
                                        startActivityForResult(purpleContactProfileIntent, PURPLE_ACTIVITY_START);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnDeleteGroup = (ImageView)findViewById(R.id.btnDeleteGroup); btnDeleteGroup.setOnClickListener(this);
        btnChatNav = (ImageView)findViewById(R.id.btnChatNav); btnChatNav.setOnClickListener(this);
        btnPermission = (ImageView)findViewById(R.id.btnEditPermission); btnPermission.setOnClickListener(this);
        btnGinkoCall = (ImageView)findViewById(R.id.btnGinkoCall); btnGinkoCall.setOnClickListener(this);
        if (groupType == 2)
            btnPermission.setVisibility(View.VISIBLE);
        else
            btnPermission.setVisibility(View.GONE);

        btnGinkoCall.setVisibility(View.VISIBLE);

        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnEdit = (ImageView)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);

        btnCancel = (Button)findViewById(R.id.btnCancel); btnCancel.setOnClickListener(this); btnCancel.setVisibility(View.GONE);

        btnDeleteItems = (ImageButton)findViewById(R.id.btnDeleteItems); btnDeleteItems.setOnClickListener(this);
        btnAddContact = (ImageButton)findViewById(R.id.btnAddContact); btnAddContact.setOnClickListener(this);

        this.contactChangedReceiver = new ContactChangedReceiver();

        if (this.contactChangedReceiver != null) {
            IntentFilter contactReceiverIntent = new IntentFilter();
            contactReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangedReceiver, contactReceiverIntent);
            isChangedContact = true;
        }
    }

    private void loadContactGroup() {
        if (group == null) {
            return;
        }

        int groupId = this.group.getGroup_id();

        if (groupType != 2)
        {
            ContactGroupRequest.getUsers(groupId,new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if (response.isSuccess()){
                        adapter.clearAdapter();
                        adapter.notifyDataSetChanged();
                        listView.initView();
                        String currentSectionName = "";
                        List<JSONObject> jsonContacts = Uitils.toJsonList(response.getData().optJSONArray("data"));
                        List<ContactItem> contactList = new ArrayList<ContactItem>();

                        for(int i=0;i<jsonContacts.size();i++)
                        {
                            JSONObject jsonContact = jsonContacts.get(i);
                            //String sectionName = createSectionAsNeeded(jsonContact , currentSectionName ,contactList);
                            //if(!sectionName.equals(""))
                            //    currentSectionName = sectionName;
                            int contactType = jsonContact.optInt("contact_type", 1);
                            if (contactType == 1) {
                                // purple contact
                                showPurpleContact(jsonContact , contactList);
                            } else if (contactType == 2) {
                                // grey contact
                                showGreyContact(jsonContact , contactList);
                            } else if (contactType == 3) {
                                // entity followed
                                showEntity(jsonContact , contactList);
                            }
                        }

                        sortContactsAndMakeSectionHeaders(contactList);

                        if (contactList.size() > 0) {
                            getSelectedContacts(contactList);
                        }

                        adapter.addAll(contactList);
                        adapter.notifyDataSetChanged();

                        if(strSearchKeyword != null && !strSearchKeyword.equals("")) {
                            strSearchKeyword = "";
                            searchItems();
                        }
                    }
                    updateControlsByCount();

                }
            });
        } else
        {
            DirectoryRequest.getAllMembers(groupId, 1, 200, new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if (response.isSuccess()) {
                        adapter.clearAdapter();
                        adapter.notifyDataSetChanged();
                        listView.initView();
                        String currentSectionName = "";
                        List<JSONObject> jsonContacts = Uitils.toJsonList(response.getData().optJSONArray("data"));
                        List<ContactItem> contactList = new ArrayList<ContactItem>();

                        for (int i = 0; i < jsonContacts.size(); i++) {
                            JSONObject jsonContact = jsonContacts.get(i);
                            // Directory contact
                            showDirectoryMemberContact(jsonContact, contactList);
                        }

                        sortContactsAndMakeSectionHeaders(contactList);

                        if (contactList.size() > 0) {
                            getSelectedContacts(contactList);
                        }

                        adapter.addAll(contactList);
                        adapter.notifyDataSetChanged();

                        if (strSearchKeyword != null && !strSearchKeyword.equals("")) {
                            strSearchKeyword = "";
                            searchItems();
                        }
                    }
                    updateControlsByCount();
                }
            });
        }
    }

    private void getSelectedContacts(List<ContactItem> contactList) {
        ContactItem lItem = null;
        ContactItem SItem = null;
        int contactLId, contactSId = 0;

        sltContactList = new ArrayList<>();

        if(MyApp.g_contactItems != null) {
            if (MyApp.g_contactItems.size() > 0) {
                for (int i = 0; i < MyApp.g_contactItems.size(); i++) {
                    lItem = MyApp.g_contactItems.get(i);
                    contactLId = lItem.getContactId();
                    for (int j = 0; j < contactList.size(); j++) {
                        SItem = contactList.get(j);
                        contactSId = SItem.getContactId();
                        if (contactLId == contactSId) {
                            sltContactList.add(SItem);
                            break;
                        }
                    }
                }
            } else {

            }
        } else {

        }
    }

    private void sortContactsAndMakeSectionHeaders(List<ContactItem> contactList)
    {
        int index = 0;
        String currentSectionName = "";
        boolean isSortByFName = Uitils.getIsSortByFName(GroupDetailActivity.this);

        ContactItemComparator contactItemComparator = new ContactItemComparator(GroupDetailActivity.this, isSortByFName);

        Collections.sort(contactList, contactItemComparator);

        while (index < contactList.size()) {
            String sectionName = createSectionAsNeeded(contactList.get(index), currentSectionName, isSortByFName);
            if (!sectionName.equals("")) {
                currentSectionName = sectionName;
                contactList.add(index, ContactItem.createSection(sectionName));
                index += 2;
                continue;
            }
            index++;
        }
    }

    @SuppressLint("DefaultLocale")
    protected String createSectionAsNeeded(JSONObject jsonContact , String currentSectionName , List<ContactItem> contacts) {
        int contactType = jsonContact.optInt("contact_type", 1);
        String checkName = "";
        if (contactType == 1) {
            // purple contact
            String firstName = jsonContact.optString("first_name");
            String lastName = jsonContact.optString("last_name");
            checkName = (firstName + lastName).trim();
        } else if (contactType == 2) {
            // grey contact
            String firstName = jsonContact.optString("first_name");
            String lastName = jsonContact.optString("last_name");
            checkName = (firstName + lastName).trim();
        } else if (contactType == 3) {
            // entity followed
            String name = jsonContact.optString("name");
            checkName = name.trim();
        }
        if (checkName.length() > 0
                && (StringUtils.isEmpty(currentSectionName) || !checkName
                .toUpperCase().startsWith(currentSectionName))) {
            // New section;
            String newSectionName = checkName.substring(0, 1).toUpperCase();
            contacts.add(ContactItem.createSection(newSectionName));
            return newSectionName;
        }

        return "";
    }

    @SuppressLint("DefaultLocale")
    protected String createSectionAsNeeded(ContactItem item , String currentSectionName , boolean isSortByFName) {
        int contactType = item.getContactType();
        String checkName = null;
        if(isSortByFName)
        {
            checkName = item.getFirstName() + item.getLastName();
        }else
        {
            checkName = item.getLastName() + item.getFirstName();
        }
        checkName = checkName.trim();

        if (checkName.length() > 0
                && (StringUtils.isEmpty(currentSectionName) || !checkName
                .toUpperCase().startsWith(currentSectionName))) {
            // New section;
            char firstLetter = checkName.charAt(0);
            if(!((firstLetter >= 'a' && firstLetter <= 'z') || (firstLetter >= 'A' && firstLetter <= 'Z')))
            {
                return "#";
            }
            String newSectionName = checkName.substring(0, 1).toUpperCase();

            return newSectionName;
        }

        return "";
    }

    protected void showEntity(JSONObject jsonContact ,  List<ContactItem> contacts) {
        String name = jsonContact.optString("name");
        String profileImage = jsonContact.optString("profile_image");

        ContactItem item = ContactItem.createItem(name, "");
        item.setProfileImage(profileImage);
        item.setContactId(jsonContact.optInt("entity_id"));
        item.setContactType(3);
        item.setIsRead(jsonContact.optBoolean("is_read" , false));
        item.setId(jsonContact.optInt("id" , 0));

        contacts.add(item);
    }

    protected void showGreyContact(JSONObject jsonContact ,  List<ContactItem> contacts) {
        String firstName = jsonContact.optString("first_name");
        String lastName = jsonContact.optString("last_name");
        String middleName = jsonContact.optString("middle_name");
        String profileImage = jsonContact.optString("photo_url");


        JSONArray phonesJsonArray = jsonContact.optJSONArray("phones");
        JSONArray emailsJsonArray = jsonContact.optJSONArray("emails");
        final List<String> phones  = new ArrayList<String>();
        if (phonesJsonArray != null) {
            for(int i = 0 ;i<phonesJsonArray.length();i++){
                phones.add(phonesJsonArray.optString(i));
            }
        }

        phones.add("Cancel");
        final List<String> emails  = new ArrayList<String>();
        if (emailsJsonArray != null) {
            for(int i = 0 ;i<emailsJsonArray.length();i++){
                emails.add(emailsJsonArray.optString(i));
            }
        }

        ContactItem item = ContactItem.createItem(firstName, lastName);
        item.setMiddleName(middleName);
        item.setProfileImage(profileImage);
        item.setContactId(jsonContact.optInt("contact_id"));
        item.setContactType(2);
        item.setPhones(phones);
        item.setEmails(emails);
        item.setIsRead(jsonContact.optBoolean("is_read", true));
        item.setId(jsonContact.optInt("id", 0));
        contacts.add(item);
    }

    protected void showPurpleContact(JSONObject jsonContact ,  List<ContactItem> contacts) {
        String firstName = jsonContact.optString("first_name");
        String middleName = jsonContact.optString("middle_name");
        String lastName = jsonContact.optString("last_name");
        String profileImage = jsonContact.optString("profile_image");
        if (firstName.isEmpty() && lastName.isEmpty()){
            return;
        }

        JSONArray phonesJsonArray = jsonContact.optJSONArray("phones");

        final List<String> phones  = new ArrayList<String>();
        if (phonesJsonArray != null) {
            for(int i = 0 ;i<phonesJsonArray.length();i++){
                phones.add(phonesJsonArray.optString(i));
            }
        }
        phones.add("Cancel");
        if (!phones.contains("Ginko Video Call"))
            phones.add(0, "Ginko Video Call");
        if (!phones.contains("Ginko Voice Call"))
            phones.add(0, "Ginko Voice Call");

        ContactItem item = ContactItem.createItem(firstName, lastName);
        item.setMiddleName(middleName);
        item.setProfileImage(profileImage);
        item.setContactType(1);
        item.setContactId(jsonContact.optInt("contact_id"));
        item.setPhones(phones);
        item.setIsRead(jsonContact.optBoolean("is_read", false));
        item.setId(jsonContact.optInt("id", 0));
        item.setSharingStatus(jsonContact.optInt("sharing_status", -1));
        contacts.add(item);

    }

    protected void showDirectoryMemberContact(JSONObject jsonContact ,  List<ContactItem> contacts) {
        String firstName = jsonContact.optString("fname");
        String middleName = jsonContact.optString("mname");
        String lastName = jsonContact.optString("lname");
        String profileImage = jsonContact.optString("profile_image");
        if (firstName.isEmpty() && lastName.isEmpty()){
            return;
        }

        JSONArray phonesJsonArray = jsonContact.optJSONArray("phones");

        final List<String> phones  = new ArrayList<String>();
        if (phonesJsonArray != null) {
            for(int i = 0 ;i<phonesJsonArray.length();i++){
                phones.add(phonesJsonArray.optString(i));
            }
        }
        phones.add("Cancel");
        if (!phones.contains("Ginko Video Call"))
            phones.add(0, "Ginko Video Call");
        if (!phones.contains("Ginko Voice Call"))
            phones.add(0, "Ginko Voice Call");

        ContactItem item = ContactItem.createItem(firstName, lastName);
        item.setMiddleName(middleName);
        item.setProfileImage(profileImage);
        item.setContactType(1);
        item.setContactId(jsonContact.optInt("user_id"));
        item.setPhones(phones);
        item.setIsRead(jsonContact.optBoolean("is_read", false));
        item.setId(jsonContact.optInt("id", 0));
        item.setSharingStatus(jsonContact.optInt("sharing_status", -1));
        contacts.add(item);

    }

    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();
        if(strEditText.compareTo("")!=0) { // && strEditText.compareTo(strSearchKeyword) != 0) {
            strSearchKeyword = strEditText.toLowerCase();
            try {
                adapter.searchItems(strSearchKeyword);
                adapter.notifyDataSetChanged();
            }catch(Exception e){e.printStackTrace();}
        }
        else
        {
            strSearchKeyword = "";
            try {
                adapter.searchItems("");
                adapter.notifyDataSetChanged();
            }catch(Exception e){e.printStackTrace();}
        }

        updateControlsByCount();
    }

    private void updateControlsByCount()
    {
        lstItems.clear();

        if (adapter == null)
        {
            if (groupType != 2)
                btnEdit.setImageResource(R.drawable.editpen_disable);
            btnGinkoCall.setImageResource(R.drawable.conferencemutliusers_disabled);
            isGinkoCallAvaiable = false;
            listView.setBackgroundResource(R.drawable.leaf_bg_for_blank);
        }

        int callCnt = 0;

        if (adapter.getVisibleItems() != null && adapter.getVisibleItems().size() > 0)
        {
            if (groupType != 2)
                btnEdit.setImageResource(R.drawable.editpen);
            listView.setBackgroundColor(Color.TRANSPARENT);

            for (int i = 0; i < adapter.getVisibleItems().size(); i++) {
                ContactItem contact = adapter.getVisibleItems().get(i);

                if (contact.getContactId() == 0) continue;
                if (contact.getContactType() != 1) continue;
                if (contact.getSharingStatus() == 4) continue;
                if (contact.getContactId() == RuntimeContext.getUser().getUserId())
                    continue;
                lstItems.add(contact);
                callCnt++;
            }
        }
        else
        {
            if (groupType != 2)
                btnEdit.setImageResource(R.drawable.editpen_disable);
            listView.setBackgroundResource(R.drawable.leaf_bg_for_blank);
        }

        if (callCnt > 0)
        {
            btnGinkoCall.setImageResource(R.drawable.conferencemutliusers);
            isGinkoCallAvaiable = true;
        }
        else
        {
            btnGinkoCall.setImageResource(R.drawable.conferencemutliusers_disabled);
            isGinkoCallAvaiable = false;
        }
    }

    private void updateUIFromEditable()
    {
        if(isEditable)
        {
            btnPrev.setVisibility(View.GONE);
            btnChatNav.setVisibility(View.GONE);
            //btnAddContact.setVisibility(View.GONE);
            btnPermission.setVisibility(View.GONE);
            btnGinkoCall.setVisibility(View.GONE);

            btnDeleteGroup.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.VISIBLE);
            //btnDeleteItems.setVisibility(View.VISIBLE);

            try {
                adapter.setListIsSeletable(true);
                adapter.notifyDataSetChanged();
            }catch(Exception e){e.printStackTrace();}
        }
        else
        {
            if (groupType == 2)
            {
                btnPrev.setVisibility(View.VISIBLE);
                btnChatNav.setVisibility(View.VISIBLE);
                btnGinkoCall.setVisibility(View.VISIBLE);
                layoutBottom.setVisibility(View.GONE);
                btnPermission.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                btnDeleteGroup.setVisibility(View.GONE);
                btnClose.setVisibility(View.GONE);
            } else {
                btnPrev.setVisibility(View.VISIBLE);
                btnChatNav.setVisibility(View.VISIBLE);
                btnGinkoCall.setVisibility(View.VISIBLE);
                layoutBottom.setVisibility(View.VISIBLE);
                btnAddContact.setVisibility(View.VISIBLE);
                btnPermission.setVisibility(View.GONE);

                btnDeleteGroup.setVisibility(View.GONE);
                btnClose.setVisibility(View.GONE);
                btnDeleteItems.setVisibility(View.GONE);
            }
            try {
                adapter.setListIsSeletable(false);
                adapter.notifyDataSetChanged();
            }catch(Exception e){e.printStackTrace();}
        }
    }
    private void showTrashIsSelectedContact(){
        List<ContactItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.size() > 0){
            btnAddContact.setVisibility(View.GONE);
            btnDeleteItems.setVisibility(View.VISIBLE);
        }else {
            btnAddContact.setVisibility(View.VISIBLE);
            btnDeleteItems.setVisibility(View.GONE);
        }
    }

    private void showBottomPopup(View v)
    {
        final List<String> buttons = new ArrayList<String>();
        buttons.add("Ginko Video Call");
        buttons.add("Ginko Voice Call");

        String contactKeys = "";
        final ArrayList<EventUser> lstUsers = new ArrayList<EventUser>();

        if (isEditable) {
            for (int i = 0; i < adapter.getSelectedItems().size(); i++) {
                ContactItem contact = adapter.getSelectedItems().get(i);
                if (contact.getId() == 0) continue;
                if (contact.getContactType() != 1) continue;

                EventUser newUser = new EventUser();
                newUser.setFirstName(contact.getFirstName());
                newUser.setLastName(contact.getLastName());
                newUser.setPhotoUrl(contact.getProfileImage());
                newUser.setUserId(contact.getContactId());

                lstUsers.add(newUser);
                contactKeys = contactKeys + String.valueOf(contact.getId());
                if (i < adapter.getCount() - 1)
                    contactKeys = contactKeys + ",";
            }
        } else
        {
            for (int i = 0; i < adapter.getCount(); i++) {
                ContactItem contact = adapter.getItem(i);
                if (contact.getId() == 0) continue;
                if (contact.getContactType() != 1) continue;

                EventUser newUser = new EventUser();
                newUser.setFirstName(contact.getFirstName());
                newUser.setLastName(contact.getLastName());
                newUser.setPhotoUrl(contact.getProfileImage());
                newUser.setUserId(contact.getContactId());

                lstUsers.add(newUser);
                contactKeys = contactKeys + String.valueOf(contact.getId());
                if (i < adapter.getCount() - 1)
                    contactKeys = contactKeys + ",";
            }
        }

        if (contactKeys.length() > 0)
            contactKeys.substring(0, contactKeys.length()-1);
        else {
            MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_no_available_ginkocall_in_group, null);
            return;
        }

        final BottomPopupWindow popupWindow = new BottomPopupWindow(GroupDetailActivity.this, buttons);
        final String finalContactKeys = contactKeys;
        popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
            @Override
            public void onClick(View button, int position) {
                String text = buttons.get(position);
                if (text == "Cancel") {
                    popupWindow.dismiss();
                } else if (text == "Ginko Video Call") {
                    CreateVideoVoiceConferenceBoard(finalContactKeys, lstUsers, 1);
                    popupWindow.dismiss();
                } else if (text == "Ginko Voice Call") {
                    CreateVideoVoiceConferenceBoard(finalContactKeys, lstUsers, 2);
                    popupWindow.dismiss();
                }
            }
        });
        popupWindow.show(v);
    }

    public void CreateVideoVoiceConferenceBoard(final String userIds, final ArrayList<EventUser> lstUsers, final int callType) {
        IMRequest.createBoard(String.valueOf(userIds), new ResponseCallBack<ImBoardVO>() {
            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    int boardId = response.getData().getBoardId();

                    EventUser ownUser = new EventUser();
                    ownUser.setFirstName(RuntimeContext.getUser().getFirstName());
                    ownUser.setLastName(RuntimeContext.getUser().getLastName());
                    ownUser.setPhotoUrl(RuntimeContext.getUser().getPhotoUrl());
                    ownUser.setUserId(RuntimeContext.getUser().getUserId());

                    lstUsers.add(ownUser);
                    MyApp.getInstance().isOwnerForConfernece = true;

                    MyApp.getInstance().initializeVideoVariables();

                    int index = 1;
                    for (int i=0; i<lstUsers.size(); i++)
                    {
                        VideoMemberVO currMember = new VideoMemberVO();
                        EventUser indexOne = lstUsers.get(i);

                        currMember.setUserId(String.valueOf(indexOne.getUserId()));
                        currMember.setName(indexOne.getFirstName() + " " + indexOne.getLastName());
                        currMember.setImageUrl(indexOne.getPhotoUrl());

                        if (indexOne.getUserId() == RuntimeContext.getUser().getUserId())
                        {
                            currMember.setOwner(true);
                            currMember.setMe(true);
                            currMember.setWeight(0);
                            currMember.setInitialized(true);
                            if (callType == 1)
                                currMember.setVideoStatus(true);
                            else
                                currMember.setVideoStatus(false);

                            currMember.setVoiceStatus(true);
                            MyApp.getInstance().g_currMemberCon = currMember;
                        } else
                        {
                            currMember.setOwner(false);
                            currMember.setMe(false);
                            currMember.setWeight(index);
                            currMember.setInitialized(true);
                            currMember.setYounger(true);

                            if (callType == 1)
                                currMember.setVideoStatus(true);
                            else
                                currMember.setVideoStatus(false);

                            currMember.setVoiceStatus(true);
                            index++;
                        }

                        MyApp.getInstance().g_videoMemberList.add(currMember);
                        MyApp.getInstance().g_videoMemIDs.add(currMember.getUserId());
                    }

                    Intent groupVideoIntent = new Intent(GroupDetailActivity.this, GroupVideoChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", boardId);
                    bundle.putInt("callType", callType);
                    bundle.putString("conferenceName", group.getName());
                    bundle.putSerializable("userData", lstUsers);
                    bundle.putBoolean("isInitial", true);
                    groupVideoIntent.putExtras(bundle);
                    startActivity(groupVideoIntent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK && data!=null)
        {
            switch (requestCode)
            {
                case PURPLE_ACTIVITY_START:
                case GREY_ACTIVITY_START:
                    if(data.getBooleanExtra("isContactDeleted" , false)) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("isContactDeleted" , true);
                        GroupDetailActivity.this.setResult(Activity.RESULT_OK , returnIntent);
                        finish();
                    }
                    break;
                case SHARE_YOUR_LEAF_ACTIVITY:
                    //loadContactGroup();
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btnDeleteGroup:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm");
                //GAD-1554 Change logic from delete object (group to all contacts)
                builder.setMessage(getResources().getString(R.string.str_delete_all_contact_dialog));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        /*
                        ContactGroupRequest.delete(group.getId(), new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    GroupDetailActivity.this.finish();
                                }
                            }
                        });*/
                        String contactKeys = "";
                        for (int i = 0; i < adapter.getCount(); i++) {
                            ContactItem contact = adapter.getItem(i);
                            if (contact.getId() == 0) continue;
                            contactKeys = contactKeys + String.valueOf(contact.getId()) + "_" + String.valueOf(contact.getContactType());
                            if (i < adapter.getCount() - 1)
                                contactKeys = contactKeys + ",";
                        }
                        ContactGroupRequest.removeUser(group.getGroup_id(), contactKeys, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    loadContactGroup();
                                    isEditable = false;
                                    updateUIFromEditable();
                                } else {
                                    MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_failed_to_remove_group_users, null);
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                break;

            case R.id.btnChatNav:
                isShownKeyboard();
                if(isKeyboardVisible)
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                else
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if(adapter.getCount() > 0) {
                    if (groupType != 2)
                    {
                        ContactGroupRequest.getUsers(group.getGroup_id(), new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    List<JSONObject> jsonContacts = Uitils.toJsonList(response.getData().optJSONArray("data"));
                                    if (CollectionUtils.isEmpty(jsonContacts)) {
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_no_purple_contacts_in_group, null);
                                        if (isKeyboardVisible)
                                            btnCancel.setVisibility(View.VISIBLE);
                                        return;
                                    }

                                    if (jsonContacts.size() < 2)
                                    {
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_no_available_contacts_in_group, null);
                                        if (isKeyboardVisible)
                                            btnCancel.setVisibility(View.VISIBLE);
                                        return;
                                    }

                                    String userIds = "";
                                    for (int i = 0; i < jsonContacts.size(); i++) {
                                        try {
                                            JSONObject contact = jsonContacts.get(i);
                                            if (contact.has("id") && contact.getString("contact_type").equals("1")) {
                                                if (i != (jsonContacts.size() - 1))
                                                    userIds += contact.optInt("id") + ",";
                                                else
                                                    userIds += contact.optInt("id");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (userIds.compareTo("") == 0) {
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_no_purple_contacts_in_group, null);
                                        if (isKeyboardVisible)
                                            btnCancel.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    IMRequest.createBoard(userIds, new ResponseCallBack<ImBoardVO>() {

                                        @Override
                                        public void onCompleted(JsonResponse<ImBoardVO> response) {
                                            if (response.isSuccess()) {
                                                ImBoardVO board = response.getData();

                                                Intent intent = new Intent(GroupDetailActivity.this, ImBoardActivity.class);
                                                intent.putExtra("board_id", board.getBoardId());
                                                intent.putExtra("group_id", group.getGroup_id());
                                                intent.putExtra("isChatOnly", true);
                                                intent.putExtra("groupname", group.getName());
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("board", board);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }
                                        }
                                    });

                                }
                            }
                        });
                    } else
                    {
                        IMRequest.createDirectoryBoard(group.getGroup_id(), new ResponseCallBack<ImBoardVO>() {

                            @Override
                            public void onCompleted(JsonResponse<ImBoardVO> response) {
                                if (response.isSuccess()) {
                                    ImBoardVO board = response.getData();

                                    Intent intent = new Intent(GroupDetailActivity.this, ImBoardActivity.class);
                                    intent.putExtra("board_id", board.getBoardId());
                                    intent.putExtra("groupname", board.getBoardName());
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("board", board);
                                    intent.putExtras(bundle);
                                    startActivity(intent);

                                }
                            }
                        });

                    }
                } else
                {
                    MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_no_purple_contacts_in_group, null);
                    if (isKeyboardVisible)
                        btnCancel.setVisibility(View.VISIBLE);
                    return;
                }
                /*List<FragmentContactItem> contacts = adapter.getAll();
                if(CollectionUtils.isEmpty(contacts)){
                    return;
                }
                String userIds="";
                for (FragmentContactItem contact : contacts) {
                    userIds += contact.getId() + ",";
                }
                final String groupName = group.getName();
                IMRequest.createBoard(userIds, new ResponseCallBack<ImBoardVO>() {

                    @Override
                    public void onCompleted(JsonResponse<ImBoardVO> response) {
                        if (response.isSuccess()) {
                            ImBoardVO board = response.getData();

                            Intent intent = new Intent(GroupDetailActivity.this, ImBoardActivity.class);
                            intent.putExtra("board_id", board.getBoardId());
                            intent.putExtra("groupname" , groupName);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("board", board);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                });*/
                break;

            // Edit Permission
            case R.id.btnEditPermission:
                Intent contactSharingSettingIntent = new Intent(GroupDetailActivity.this , ShareYourLeafActivity.class);
                contactSharingSettingIntent.putExtra("contactID" , "0");
                contactSharingSettingIntent.putExtra("contactFullname" , group.getName());
                contactSharingSettingIntent.putExtra("isDirectory" , true);
                contactSharingSettingIntent.putExtra("directoryID", group.getGroup_id());
                contactSharingSettingIntent.putExtra("isUnexchangedContact", false);
                contactSharingSettingIntent.putExtra("isInviteContact", true);
                contactSharingSettingIntent.putExtra("isPendingRequest", true);
                contactSharingSettingIntent.putExtra("StartActivity", "ContactMain");

                startActivityForResult(contactSharingSettingIntent, SHARE_YOUR_LEAF_ACTIVITY);
                break;
            case R.id.btnGinkoCall:
                if (isGinkoCallAvaiable)
                {
                    Intent groupVideoIntent = new Intent(GroupDetailActivity.this, VideoChatAddUserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", 0);
                    bundle.putBoolean("isReturnFromConference", false);
                    bundle.putBoolean("isGroupFrom", true);
                    bundle.putString("existContactIds", "");
                    bundle.putSerializable("lstUsers", lstItems);

                    groupVideoIntent.putExtras(bundle);
                    startActivity(groupVideoIntent);
                }
                break;
            //edit mode
            case R.id.btnEdit:
                if(!isEditable && adapter.getVisibleCount() > 0)
                {
                    isEditable = true;
                    updateUIFromEditable();
                }
                else {
                    isEditable = false;
                    updateUIFromEditable();
                }
                break;

            case R.id.btnClose:
                if(isEditable)
                {
                    isEditable = false;
                    updateUIFromEditable();
                }
                break;

            case R.id.btnDeleteItems:
                //delete select items
                if(adapter.getSelectedItemCount()>0)
                {
                    AlertDialog.Builder alertDeleteItems = new AlertDialog.Builder(GroupDetailActivity.this);
                    alertDeleteItems.setMessage(getResources().getString(R.string.str_delete_group_contact_confirm_dialog));
                    alertDeleteItems.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            List<ContactItem> selectedItems = adapter.getSelectedItems();
                            if (CollectionUtils.isEmpty(selectedItems)) {
                                return;
                            }
                            String contactKeys = "";
                            for (int i = 0; i < selectedItems.size(); i++) {
                                ContactItem contact = selectedItems.get(i);
                                if (contact.getId() == 0) continue;
                                contactKeys = contactKeys + String.valueOf(contact.getId()) + "_" + String.valueOf(contact.getContactType());
                                if (i < selectedItems.size() - 1)
                                    contactKeys = contactKeys + ",";
                            }
                            ContactGroupRequest.removeUser(group.getGroup_id(), contactKeys, new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        loadContactGroup();
                                        isEditable = false;
                                        updateUIFromEditable();
                                    } else {
                                        MyApp.getInstance().showSimpleAlertDiloag(GroupDetailActivity.this, R.string.str_alert_failed_to_remove_group_users, null);
                                    }
                                }
                            });
                        }
                    });

                    alertDeleteItems.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int paramInt) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    });
                    alertDeleteItems.show();
                }
                break;

            case R.id.btnAddContact:
                Intent addContactIntent = new Intent(GroupDetailActivity.this , GroupAddUserActivity.class);
                addContactIntent.putExtra("groupId" , group.getGroup_id());
                //List<FragmentContactItem> selectedItems = adapter.getAll();
                String contactKeys="";
                for(int i=0;i<adapter.getCount();i++)
                {
                    long userId = adapter.getItem(i).getId();
                    if(userId == 0) continue;
                    contactKeys = contactKeys + String.valueOf(userId)+",";
                }
                addContactIntent.putExtra("existContactIds" , contactKeys);
                startActivity(addContactIntent);
                break;

            //Click when Cancel button
            case R.id.btnCancel:
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                activityRootView.requestFocus();
                btnCancel.setVisibility(View.GONE);
                btnCancelSearch.setVisibility(View.GONE);
                hideKeyboard();
                break;
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        if(listView != null && adapter != null)
        {
            int index =adapter.getSectionItemIndex(s);
            if(index >= 0) {
                listView.setSelection(index);
            }
        }
    }

    public class ContactChangedReceiver extends BroadcastReceiver {
        public ContactChangedReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            loadContactGroup();
            isEditable = false;
            updateUIFromEditable();
        }
    }

}
