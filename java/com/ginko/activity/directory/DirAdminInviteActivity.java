package com.ginko.activity.directory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.common.DirectoryComparator;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.entity.EntityInviteContactItem;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.database.ContactTableModel;
import com.ginko.fragments.DirectoryInviteFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DirAdminInviteActivity extends MyBaseFragmentActivity implements View.OnClickListener {

    /* UI Elements */
    private RelativeLayout activityRootView;
    private LinearLayout tabLayout , deleteLayout;
    private ImageButton btnPrev , btnConfirm;
    private ImageView btnDeleteInvites;
    private ImageView btnTabRequested, btnTabConfirmed , btnTabPending , btnTabInviteContacts;
    private ImageView            imgSelectAllCheckBox;
    private TextView txtTitle;
    private EditText edtSearch;
    private Button btnCancel;
    private ImageView   btnCancelSearch;

    private RelativeLayout headerlayout, bodyLayout;

    /* Variables*/

    private MyViewPager mPager;
    private MyPagerAdapter pageAdapter;
    private List<DirectoryInviteFragment> fragments;
    private DirectoryInviteFragment currentFragment;
    private int currIndex = 0;
    private int nLoaded = 0;

    private List<DirectoryInviteContactItem> requestedContacts;
    private List<DirectoryInviteContactItem> confirmedContacts;
    private List<DirectoryInviteContactItem> pendingContacts;

    private ArrayList<DirectoryInviteContactItem> ginkoPurpleContacts = new ArrayList<DirectoryInviteContactItem>();

    private MyOnPageChangeListener pageListener;

    private boolean          isKeyboardVisible = false;
    private String           strSearchKeyword = "";

    private boolean isSelectedAll = false;
    private boolean isCreate = false;
    private boolean isInviteGo = false;

    private int directoryId = 0;
    private int m_orientHeight = 0;

    private AdapterView.OnItemClickListener requestedContactsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();
            btnCancel.setVisibility(View.GONE);
            activityRootView.requestFocus();

            if(requestedContacts!=null && requestedContacts.size()> position)
            {
                DirectoryInviteContactItem item = requestedContacts.get(position);
                item.setSelected(!item.isSelected());
                int selectedItemCount = getSelectedItemsCount(requestedContacts);
                if(selectedItemCount>0) {
                    tabLayout.setVisibility(View.INVISIBLE);
                    deleteLayout.setVisibility(View.VISIBLE);
                }
                else {
                    tabLayout.setVisibility(View.VISIBLE);
                    deleteLayout.setVisibility(View.INVISIBLE);
                }
                if(currentFragment!=null)
                    currentFragment.refreshListView();

                if(selectedItemCount == getVisibleItemsCount(requestedContacts))
                    isSelectedAll = true;
                else
                    isSelectedAll = false;
                updateSelectAllRadioBox();
            }
        }
    };

    private AdapterView.OnItemClickListener confirmedContactsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();
            btnCancel.setVisibility(View.GONE);
            activityRootView.requestFocus();

            if(confirmedContacts!=null && confirmedContacts.size()>position)
            {
                DirectoryInviteContactItem item = confirmedContacts.get(position);
                item.setSelected(!item.isSelected());
                int selectedItemCount = getSelectedItemsCount(confirmedContacts);
                if(selectedItemCount>0) {
                    tabLayout.setVisibility(View.INVISIBLE);
                    deleteLayout.setVisibility(View.VISIBLE);
                }
                else {
                    tabLayout.setVisibility(View.VISIBLE);
                    deleteLayout.setVisibility(View.INVISIBLE);
                }
                if(currentFragment!=null)
                    currentFragment.refreshListView();

                if(selectedItemCount == getVisibleItemsCount(confirmedContacts))
                    isSelectedAll = true;
                else
                    isSelectedAll = false;
                updateSelectAllRadioBox();
            }
        }
    };

    private AdapterView.OnItemClickListener pendingContactsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();
            btnCancel.setVisibility(View.GONE);
            activityRootView.requestFocus();

            if(pendingContacts!=null && pendingContacts.size()>position)
            {
                DirectoryInviteContactItem item = pendingContacts.get(position);
                item.setSelected(!item.isSelected());
                int selectedItemCount = getSelectedItemsCount(pendingContacts);
                if(selectedItemCount>0) {
                    tabLayout.setVisibility(View.INVISIBLE);
                    deleteLayout.setVisibility(View.VISIBLE);
                }
                else {
                    tabLayout.setVisibility(View.VISIBLE);
                    deleteLayout.setVisibility(View.INVISIBLE);
                }
                if(currentFragment!=null)
                    currentFragment.refreshListView();

                if(selectedItemCount == getVisibleItemsCount(pendingContacts))
                    isSelectedAll = true;
                else
                    isSelectedAll = false;
                updateSelectAllRadioBox();
            }


        }
    };

    private AdapterView.OnItemClickListener notInvitedContactsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();
            btnCancel.setVisibility(View.GONE);
            activityRootView.requestFocus();

            if(ginkoPurpleContacts!=null && ginkoPurpleContacts.size()>position)
            {
                DirectoryInviteContactItem item = ginkoPurpleContacts.get(position);
                item.setSelected(!item.isSelected());
                int selectedItemCount = getSelectedItemsCount(ginkoPurpleContacts);
                if(selectedItemCount>0)
                    btnConfirm.setVisibility(View.VISIBLE);
                else
                    btnConfirm.setVisibility(View.GONE);
                if(currentFragment!=null)
                    currentFragment.refreshListView();

                if(selectedItemCount == getVisibleItemsCount(ginkoPurpleContacts))
                    isSelectedAll = true;
                else
                    isSelectedAll = false;
                updateSelectAllRadioBox();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_admin_invite);

        if(savedInstanceState != null)
        {
            this.directoryId = savedInstanceState.getInt("directoryId");
            this.isCreate = savedInstanceState.getBoolean("isCreate", false);
            this.isInviteGo = savedInstanceState.getBoolean("isInviteGo",false);
        }
        else
        {
            this.directoryId = this.getIntent().getIntExtra("directoryId", 0);
            this.isCreate = this.getIntent().getBooleanExtra("isCreate", false);
            this.isInviteGo = this.getIntent().getBooleanExtra("isInviteGo", false);
        }
        if(directoryId == 0) {
            finish();
            return;
        }

        requestedContacts = new ArrayList<DirectoryInviteContactItem>();
        confirmedContacts = new ArrayList<DirectoryInviteContactItem>();
        pendingContacts = new ArrayList<DirectoryInviteContactItem>();

        getUIObjects();

        loadGinkoPurpleContacts();
        listPendingContacts(true);
        listConfirmedContacts(true);
        listRequestContacts(true);

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
    }

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dsafas", "dasfas");
            }
        });
        fragments = new ArrayList<DirectoryInviteFragment>();

        fragments.add(new DirectoryInviteFragment(DirectoryInviteFragment.DIRECTORY_CONTACT_REQUESTED , requestedContacts));
        fragments.add(new DirectoryInviteFragment(DirectoryInviteFragment.DIRECTORY_CONTACT_CONFIRMED , confirmedContacts));
        fragments.add(new DirectoryInviteFragment(DirectoryInviteFragment.DIRECTORY_CONTACT_PENDING , pendingContacts));
        fragments.add(new DirectoryInviteFragment(DirectoryInviteFragment.DIRECTORY_CONTACT_NOT_INVITED , ginkoPurpleContacts));

        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(), fragments);
        pageListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(pageListener);
        mPager.setScanScroll(false);
        mPager.setAdapter(pageAdapter);
        if (isInviteGo)
            currIndex = 0;
        else
            currIndex = 3;
        mPager.setCurrentItem(currIndex);
        pageListener.onPageSelected(currIndex);
    }

    private void loadGinkoPurpleContacts()
    {
        if(ginkoPurpleContacts == null)
            ginkoPurpleContacts = new ArrayList<DirectoryInviteContactItem>();
        else
            ginkoPurpleContacts.clear();

        //import grey_contacts
        ContactTableModel contactTableModel = MyApp.getInstance().getContactsModel();

        if (contactTableModel != null) {
            List<ContactStruct> purpleContacts = contactTableModel.getAllPurpleContactItems();
            for (ContactStruct purpleContactItem : purpleContacts) {
                DirectoryInviteContactItem contactItem = new DirectoryInviteContactItem();
                contactItem.setFirstName(purpleContactItem.getFirstName());
                contactItem.setMiddleName(purpleContactItem.getMiddleName());
                contactItem.setLastName(purpleContactItem.getLastName());
                ContactItem jsonItem = purpleContactItem.getContactItem();

                contactItem.setPhotoUrl(jsonItem.getProfileImage());
                contactItem.setContactId(purpleContactItem.getContactOrEntityId());
                String fullName = contactItem.getFirstName();
                contactItem.setFullName(jsonItem.getFullName());

                //grey contact is not from local address book
                ginkoPurpleContacts.add(contactItem);
            }
        }
    }

    private void listPendingContacts(final boolean isSpecified)
    {
        DirectoryRequest.listInvites(this.directoryId, 1, 200, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                nLoaded++;
                if (response.isSuccess()) {
                    pendingContacts.clear();

                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray contactsArray = resultObj.getJSONArray("data");
                        for (int i = 0; i < contactsArray.length(); i++) {
                            JSONObject contactObj = (JSONObject) contactsArray.get(i);
                            DirectoryInviteContactItem contactItem = new DirectoryInviteContactItem();
                            contactItem.setFirstName(contactObj.optString("fname", ""));
                            contactItem.setMiddleName(contactObj.optString("mname", ""));
                            contactItem.setLastName(contactObj.optString("lname", ""));
                            contactItem.setPhotoUrl(contactObj.optString("photo_url", ""));
                            contactItem.setContactId(contactObj.optInt("user_id"));
                            String fullName = contactItem.getFirstName();
                            fullName = fullName + " " + contactItem.getMiddleName();
                            fullName = fullName.trim();
                            fullName += contactItem.getLastName();

                            contactItem.setFullName(fullName);
                            removefromContacts(contactItem.getContactId());
                            pendingContacts.add(contactItem);

                            edtSearch.setText("");
                            activityRootView.requestFocus();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    DirectoryComparator contactItemComparator = new DirectoryComparator(DirAdminInviteActivity.this);
                    Collections.sort(pendingContacts, contactItemComparator);
                    Collections.sort(ginkoPurpleContacts, contactItemComparator);

                    fragments.get(2).refreshListView();
                    if (isSpecified) {
                        if (nLoaded == 3) {
                            fragments.get(3).refreshListView();
                            nLoaded = 0;
                        }
                    } else
                        fragments.get(3).refreshListView();

                    if (currentFragment != null) {
                        currentFragment.filterItemsByString(strSearchKeyword);
                        if (isSelectedAll)
                            currentFragment.selectAll();
                    }

                    updateSelectAllRadioBox();
                } else {
                    if (isSpecified && nLoaded == 3)
                        nLoaded = 0;
                    Uitils.alert(DirAdminInviteActivity.this, "Failed to load pending Contacts");
                }
            }
        });
    }

    private void listConfirmedContacts(final boolean isSpecified)
    {
        DirectoryRequest.listConfirmed(this.directoryId, 1, 200, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                nLoaded++;
                if (response.isSuccess()) {
                    confirmedContacts.clear();

                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray contactsArray = resultObj.getJSONArray("data");
                        for (int i = 0; i < contactsArray.length(); i++) {
                            JSONObject contactObj = (JSONObject) contactsArray.get(i);
                            DirectoryInviteContactItem contactItem = new DirectoryInviteContactItem();
                            contactItem.setFirstName(contactObj.optString("fname", ""));
                            contactItem.setMiddleName(contactObj.optString("mname", ""));
                            contactItem.setLastName(contactObj.optString("lname", ""));
                            contactItem.setPhotoUrl(contactObj.optString("photo_url", ""));
                            contactItem.setContactId(contactObj.optInt("user_id"));
                            String fullName = contactItem.getFirstName();
                            fullName = fullName + " " + contactItem.getMiddleName();
                            fullName = fullName.trim();
                            fullName += contactItem.getLastName();

                            contactItem.setFullName(fullName);
                            removefromContacts(contactItem.getContactId());
                            confirmedContacts.add(contactItem);

                            edtSearch.setText("");
                            activityRootView.requestFocus();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    DirectoryComparator contactItemComparator = new DirectoryComparator(DirAdminInviteActivity.this);
                    Collections.sort(confirmedContacts, contactItemComparator);
                    Collections.sort(ginkoPurpleContacts, contactItemComparator);

                    fragments.get(1).refreshListView();

                    if (isSpecified) {
                        if (nLoaded == 3) {
                            fragments.get(3).refreshListView();
                            nLoaded = 0;
                        }
                    } else
                        fragments.get(3).refreshListView();

                    if (currentFragment != null) {
                        currentFragment.filterItemsByString(strSearchKeyword);
                        if (isSelectedAll)
                            currentFragment.selectAll();
                    }

                    updateSelectAllRadioBox();
                } else {
                    if (isSpecified && nLoaded == 3)
                        nLoaded = 0;

                    Uitils.alert(DirAdminInviteActivity.this, "Failed to load confirmed Contacts");
                }
            }
        });
    }

    private void listRequestContacts(final boolean isSpecified)
    {
        DirectoryRequest.listRequest(this.directoryId, 1, 200, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                nLoaded++;
                if (response.isSuccess()) {
                    requestedContacts.clear();

                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray contactsArray = resultObj.getJSONArray("data");
                        for (int i = 0; i < contactsArray.length(); i++) {
                            JSONObject contactObj = (JSONObject) contactsArray.get(i);
                            DirectoryInviteContactItem contactItem = new DirectoryInviteContactItem();
                            contactItem.setFirstName(contactObj.optString("fname", ""));
                            contactItem.setMiddleName(contactObj.optString("mname", ""));
                            contactItem.setLastName(contactObj.optString("lname", ""));
                            contactItem.setPhotoUrl(contactObj.optString("photo_url", ""));
                            contactItem.setContactId(contactObj.optInt("user_id"));
                            String fullName = contactItem.getFirstName();
                            fullName = fullName + " " + contactItem.getMiddleName();
                            fullName = fullName.trim();
                            fullName += contactItem.getLastName();

                            contactItem.setFullName(fullName);
                            requestedContacts.add(contactItem);
                            removefromContacts(contactItem.getContactId());

                            edtSearch.setText("");
                            activityRootView.requestFocus();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    DirectoryComparator contactItemComparator = new DirectoryComparator(DirAdminInviteActivity.this);
                    Collections.sort(requestedContacts, contactItemComparator);
                    Collections.sort(ginkoPurpleContacts, contactItemComparator);

                    fragments.get(0).refreshListView();

                    if (isSpecified) {
                        if (nLoaded == 3) {
                            fragments.get(3).refreshListView();
                            nLoaded = 0;
                        }
                    } else
                        fragments.get(3).refreshListView();

                    if(currentFragment != null)
                    {
                        currentFragment.filterItemsByString(strSearchKeyword);
                        if(isSelectedAll)
                            currentFragment.selectAll();
                    }

                    updateSelectAllRadioBox();
                } else {
                    if (isSpecified && nLoaded == 3)
                        nLoaded = 0;

                    Uitils.alert(DirAdminInviteActivity.this, "Failed to load requested Contacts");
                }
            }
        });
    }

    public void removefromContacts(int contactID)
    {
        if (ginkoPurpleContacts == null)
            return;

        for(int j=0;j<ginkoPurpleContacts.size();j++)
        {
            if(ginkoPurpleContacts.get(j).getContactId() == contactID)
            {
                ginkoPurpleContacts.remove(j);
                break;
            }
        }
    }

    public void removefromRequestList(List<DirectoryInviteContactItem> items)
    {
        for (int i=0; i<items.size(); i++) {
            requestedContacts.remove(items.get(i));
            if (MyApp.g_contactIDs.contains(items.get(i).getContactId()))
            {
                DirectoryInviteContactItem addItem = items.get(i);
                addItem.setSelected(false);
                ginkoPurpleContacts.add(addItem);
            }
        }

        DirectoryComparator contactItemComparator = new DirectoryComparator(DirAdminInviteActivity.this);
        Collections.sort(ginkoPurpleContacts, contactItemComparator);

        fragments.get(0).refreshListView();
        fragments.get(3).refreshListView();
    }

    public void removefromPendingList(List<DirectoryInviteContactItem> items)
    {
        for (int i=0; i<items.size(); i++)
        {
            pendingContacts.remove(items.get(i));
            DirectoryInviteContactItem addItem = items.get(i);
            addItem.setSelected(false);
            ginkoPurpleContacts.add(addItem);
        }

        DirectoryComparator contactItemComparator = new DirectoryComparator(DirAdminInviteActivity.this);
        Collections.sort(ginkoPurpleContacts, contactItemComparator);

        fragments.get(2).refreshListView();
        fragments.get(3).refreshListView();

        mPager.setCurrentItem(3);
    }

    public void removefromConfirmList(List<DirectoryInviteContactItem> items)
    {
        for (int i=0; i<items.size(); i++)
        {
            confirmedContacts.remove(items.get(i));
            DirectoryInviteContactItem addItem = items.get(i);
            addItem.setSelected(false);
            ginkoPurpleContacts.add(addItem);
        }

        DirectoryComparator contactItemComparator = new DirectoryComparator(DirAdminInviteActivity.this);
        Collections.sort(ginkoPurpleContacts, contactItemComparator);

        fragments.get(1).refreshListView();
        fragments.get(3).refreshListView();
    }

    @Override
    protected  void getUIObjects()
    {
        super.getUIObjects();
        currIndex = 0;
        currentFragment = null;
        headerlayout = (RelativeLayout)findViewById(R.id.headerlayout);
        bodyLayout = (RelativeLayout)findViewById(R.id.bodyLayout);

        bodyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        activityRootView = (RelativeLayout) findViewById(R.id.rootLayout);
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
                    }
                }
            }
        });

        activityRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);

        btnTabRequested = (ImageView)findViewById(R.id.btnTabRequest); btnTabRequested.setOnClickListener(this);
        btnTabConfirmed = (ImageView)findViewById(R.id.btnTabConfirmed); btnTabConfirmed.setOnClickListener(this);
        btnTabPending = (ImageView)findViewById(R.id.btnTabPending); btnTabPending.setOnClickListener(this);
        btnTabInviteContacts = (ImageView)findViewById(R.id.btnTabInviteContacts); btnTabInviteContacts.setOnClickListener(this);

        imgSelectAllCheckBox =  (ImageView)findViewById(R.id.imgSelectAllCheckBox); imgSelectAllCheckBox.setOnClickListener(this);

        txtTitle = (TextView)findViewById(R.id.txtTitle);

        tabLayout = (LinearLayout)findViewById(R.id.tabLayout);
        deleteLayout = (LinearLayout)findViewById(R.id.deleteLayout);

        btnDeleteInvites = (ImageView)findViewById(R.id.btnDeleteInvites); btnDeleteInvites.setOnClickListener(this);

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                activityRootView.requestFocus();
                btnCancel.setVisibility(View.GONE);
                hideKeyboard();
            }
        });
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch);   btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                btnCancelSearch.setVisibility(View.GONE);
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
                if (s.length() > 0) {
                    btnCancelSearch.setVisibility(View.VISIBLE);
                } else {
                    btnCancelSearch.setVisibility(View.GONE);
                }
                searchItems();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //Hide soft keyboard
                    /*InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);*/
                    activityRootView.requestFocus();
                    hideKeyboard();

                    searchItems();
                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
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
                    if (btnCancel.getVisibility() == View.GONE) {
                        btnCancel.setVisibility(View.VISIBLE);
                    }
                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                } else {
                    edtSearch.setCursorVisible(false);
                    if (btnCancel.getVisibility() == View.VISIBLE) {
                        btnCancel.setVisibility(View.GONE);
                    }
                    btnCancelSearch.setVisibility(View.GONE);
                    if (isKeyboardVisible)
                        hideKeyboard();
                }
            }
        });

        if (isCreate)
        {
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_back_nav_white);
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnConfirm.setImageResource(R.drawable.btn_confirm_white);
        } else
        {
            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_prev);
            txtTitle.setTextColor(getResources().getColor(R.color.photo_video_editor_home_txt_color));
            btnConfirm.setImageResource(R.drawable.checknav);
        }

        initViewPager();
    }


    private void updateTab(int index)
    {
        switch(index)
        {
            case 0: //requested tab
                txtTitle.setText("Requested");

                btnTabRequested.setImageResource(R.drawable.btn_request_filled);
                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_normal);
                btnTabPending.setImageResource(R.drawable.entity_pending_normal);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_normal);

                break;
            case 1: //confirmed tab
                txtTitle.setText("Confirmed");

                btnTabRequested.setImageResource(R.drawable.btn_request_empty);
                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_sel);
                btnTabPending.setImageResource(R.drawable.entity_pending_normal);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_normal);

                break;

            case 2: //pending
                txtTitle.setText("Pending");

                btnTabRequested.setImageResource(R.drawable.btn_request_empty);
                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_normal);
                btnTabPending.setImageResource(R.drawable.entity_pending_sel);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_normal);

                break;

            case 3: //invite contacts(s)
                txtTitle.setText("Invite Contact(s)");

                btnTabRequested.setImageResource(R.drawable.btn_request_empty);
                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_normal);
                btnTabPending.setImageResource(R.drawable.entity_pending_normal);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_sel);

                break;
        }
    }


    private void updateSelectAllRadioBox()
    {
        if (currentFragment != null)
        {
            int count = currentFragment.getSelectedItemCounts();
            if(count > 0 && count == currentFragment.getVisibleCount())
            {
                imgSelectAllCheckBox.setImageResource(R.drawable.share_profile_selected);
                isSelectedAll = true;
            }
            else
            {
                imgSelectAllCheckBox.setImageResource(R.drawable.share_profile_non_selected);
                isSelectedAll = false;
            }

            if ((currIndex == 3 || currIndex == 0) && count > 0 )
                btnConfirm.setVisibility(View.VISIBLE);
            else
                btnConfirm.setVisibility(View.GONE);

            if (currIndex != 3)
            {
                if(count > 0) {
                    deleteLayout.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.INVISIBLE);
                }
                else
                {
                    deleteLayout.setVisibility(View.INVISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();
        if(strEditText.compareTo("")!=0) {
            strSearchKeyword = strEditText.toLowerCase();
        }
        else
        {
            strSearchKeyword = "";

        }
        if(currentFragment!=null)
            currentFragment.filterItemsByString(strSearchKeyword);

        updateSelectAllRadioBox();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isKeyboardVisible) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShownKeyboard();

        if(!isKeyboardVisible)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        else {
            MyApp.getInstance().hideKeyboard(activityRootView);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            case R.id.btnConfirm:
                String contactUids = "";

                if (currIndex == 3)
                {
                    for(int i=0;i<ginkoPurpleContacts.size();i++)
                    {
                        DirectoryInviteContactItem item = ginkoPurpleContacts.get(i);
                        if(item.isSelected() && item.getVisibility() == true)
                            contactUids += String.valueOf(item.getContactId())+",";
                    }
                    if(!contactUids.equals(""))
                    {
                        contactUids = contactUids.substring(0 , contactUids.length()-1);
                        DirectoryRequest.inviteFriends(directoryId, contactUids, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    btnConfirm.setVisibility(View.GONE);
                                    if (isSelectedAll)
                                        isSelectedAll = false;
                                    updateSelectAllRadioBox();
                                    finish();
                                } else {
                                    Uitils.alert(DirAdminInviteActivity.this, "Failed to invite contact(s).");
                                }
                            }
                        });
                    } else
                        finish();
                }
                else if (currIndex == 0)
                {
                    for(int i=0;i<requestedContacts.size();i++)
                    {
                        DirectoryInviteContactItem item = requestedContacts.get(i);
                        if(item.isSelected() && item.getVisibility() == true)
                            contactUids += String.valueOf(item.getContactId())+",";
                    }
                    if(!contactUids.equals(""))
                    {
                        contactUids = contactUids.substring(0 , contactUids.length()-1);
                        DirectoryRequest.approveRequests(directoryId, contactUids, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    listRequestContacts(false);
                                    btnConfirm.setVisibility(View.GONE);
                                    if (isSelectedAll)
                                        isSelectedAll = false;
                                    updateSelectAllRadioBox();
                                } else {
                                    Uitils.alert(DirAdminInviteActivity.this, "Failed to approve request(s).");
                                }
                            }
                        });
                    }
                }

                break;

            case R.id.btnTabRequest:
                mPager.setCurrentItem(0);
                updateTab(0);
                break;
            case R.id.btnTabConfirmed:
                mPager.setCurrentItem(1);
                updateTab(1);
                break;

            case R.id.btnTabPending:
                mPager.setCurrentItem(2);
                updateTab(2);
                break;

            case R.id.btnTabInviteContacts:
                mPager.setCurrentItem(3);
                updateTab(3);
                break;

            case R.id.btnDeleteInvites:
                String inviteContactIds = "";
                final List<DirectoryInviteContactItem> temp = new ArrayList<DirectoryInviteContactItem>();

                if(currIndex == 0) {

                    for (int i = 0; i < requestedContacts.size(); i++) {
                        DirectoryInviteContactItem item = requestedContacts.get(i);
                        if (item.isSelected()) {
                            inviteContactIds += String.valueOf(item.getContactId()) + ",";
                            temp.add(item);
                        }
                    }

                    if(!inviteContactIds.equals(""))
                    {
                        inviteContactIds = inviteContactIds.substring(0 , inviteContactIds.length()-1);
                        final String inviteIds = inviteContactIds;
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Confirm");
                        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_requests));
                        builder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                DirectoryRequest.deleteRequests(directoryId, inviteIds, new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()) {
                                            removefromRequestList(temp);
                                            temp.clear();

                                            updateSelectAllRadioBox();
                                        } else {
                                            Uitils.alert(DirAdminInviteActivity.this, response.getErrorMessage());
                                        }
                                    }
                                });
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }

                }
                else if(currIndex == 1) {
                    for (int i = 0; i < confirmedContacts.size(); i++) {
                        DirectoryInviteContactItem item = confirmedContacts.get(i);
                        if (item.isSelected()) {
                            inviteContactIds += String.valueOf(item.getContactId()) + ",";
                            temp.add(item);
                        }
                    }

                    if(!inviteContactIds.equals(""))
                    {
                        inviteContactIds = inviteContactIds.substring(0 , inviteContactIds.length()-1);
                        final String inviteIds = inviteContactIds;
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Confirm");
                        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_members));
                        builder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                DirectoryRequest.deleteMembers(directoryId, inviteIds, new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()) {
                                            removefromConfirmList(temp);
                                            temp.clear();

                                            updateSelectAllRadioBox();
                                        } else {
                                            Uitils.alert(DirAdminInviteActivity.this, response.getErrorMessage());
                                        }
                                    }
                                });
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                }
                else if(currIndex == 2) {
                    for (int i = 0; i < pendingContacts.size(); i++) {
                        DirectoryInviteContactItem item = pendingContacts.get(i);
                        if (item.isSelected()) {
                            inviteContactIds += String.valueOf(item.getContactId()) + ",";
                            temp.add(item);
                        }
                    }

                    if(!inviteContactIds.equals(""))
                    {
                        inviteContactIds = inviteContactIds.substring(0 , inviteContactIds.length()-1);
                        final String inviteIds = inviteContactIds;
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Confirm");
                        builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_entity_invites));
                        builder.setPositiveButton(R.string.str_confirm_dialog_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                DirectoryRequest.cancelInvite(directoryId, inviteIds, new ResponseCallBack<Void>() {
                                    @Override
                                    public void onCompleted(JsonResponse<Void> response) {
                                        if (response.isSuccess()) {
                                            removefromPendingList(temp);
                                            temp.clear();

                                            updateSelectAllRadioBox();
                                        } else {
                                            Uitils.alert(DirAdminInviteActivity.this, response.getErrorMessage());
                                        }
                                    }
                                });
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.str_confirm_dialog_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //TODO
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                }

                break;

            case R.id.imgSelectAllCheckBox:
                if(currIndex == 0 && requestedContacts.size() == 0)
                    return;
                if(currIndex == 1 && confirmedContacts.size() == 0)
                    return;
                if(currIndex == 2 && pendingContacts.size() == 0)
                    return;
                if(currIndex == 3 && ginkoPurpleContacts.size() == 0)
                    return;
                if (currentFragment.getVisibleCount() == 0)
                    return;

                isSelectedAll = !isSelectedAll;
                if(currIndex == 0)//requested invites
                {
                    for(int i=0; i<requestedContacts.size(); i++)
                    {
                        if (requestedContacts.get(i).getVisibility() == true)
                            requestedContacts.get(i).setSelected(!requestedContacts.get(i).isSelected());
                    }
                    if(isSelectedAll)
                    {
                        tabLayout.setVisibility(View.INVISIBLE);
                        deleteLayout.setVisibility(View.VISIBLE);
                        currentFragment.selectAll();
                    }
                    else {
                        tabLayout.setVisibility(View.VISIBLE);
                        deleteLayout.setVisibility(View.INVISIBLE);
                        currentFragment.unselectAll();
                    }
                }
                else if(currIndex == 1)//confirmed invites
                {
                    for(int i=0; i<confirmedContacts.size(); i++)
                    {
                        if (confirmedContacts.get(i).getVisibility() == true)
                            confirmedContacts.get(i).setSelected(!confirmedContacts.get(i).isSelected());
                    }
                    if(isSelectedAll)
                    {
                        tabLayout.setVisibility(View.INVISIBLE);
                        deleteLayout.setVisibility(View.VISIBLE);
                        currentFragment.selectAll();
                    }
                    else {
                        tabLayout.setVisibility(View.VISIBLE);
                        deleteLayout.setVisibility(View.INVISIBLE);
                        currentFragment.unselectAll();
                    }
                }
                else if(currIndex == 2) //pending invites
                {
                    for(int i=0; i<pendingContacts.size(); i++)
                    {
                        if (pendingContacts.get(i).getVisibility() == true)
                            pendingContacts.get(i).setSelected(!pendingContacts.get(i).isSelected());
                    }
                    if(isSelectedAll)
                    {
                        tabLayout.setVisibility(View.INVISIBLE);
                        deleteLayout.setVisibility(View.VISIBLE);
                        currentFragment.selectAll();
                    }
                    else {
                        tabLayout.setVisibility(View.VISIBLE);
                        deleteLayout.setVisibility(View.INVISIBLE);
                        currentFragment.unselectAll();
                    }
                }
                else if(currIndex == 3)//not invited contacts
                {
                    for(int i=0; i<ginkoPurpleContacts.size(); i++)
                    {
                        if (ginkoPurpleContacts.get(i).getVisibility() == true)
                            ginkoPurpleContacts.get(i).setSelected(!ginkoPurpleContacts.get(i).isSelected());
                    }
                    if(isSelectedAll)
                    {
                        currentFragment.selectAll();
                        if(getSelectedItemsCount(ginkoPurpleContacts)>0)
                            btnConfirm.setVisibility(View.VISIBLE);
                        else
                            btnConfirm.setVisibility(View.GONE);
                    }
                    else {
                        btnConfirm.setVisibility(View.GONE);
                        currentFragment.unselectAll();
                    }
                }

                updateSelectAllRadioBox();

                if(currentFragment!=null)
                    currentFragment.refreshListView();
                break;
        }
    }

    private void hideKeyboard()
    {
        // if(isKeyboardVisible)
        MyApp.getInstance().hideKeyboard(activityRootView);
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

    private int getSelectedItemsCount(List<DirectoryInviteContactItem> inviteItems)
    {
        if(inviteItems == null) return 0;
        int count = 0;
        for(int i=0;i<inviteItems.size(); i++)
        {
            if(inviteItems.get(i).isSelected() && inviteItems.get(i).getVisibility() == true)
                count++;
        }
        return count;
    }

    private int getVisibleItemsCount(List<DirectoryInviteContactItem> inviteItems)
    {
        if(inviteItems == null) return 0;
        int count = 0;
        for(int i=0;i<inviteItems.size(); i++)
        {
            if(inviteItems.get(i).getVisibility() == true)
                count++;
        }
        return count;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        private List<DirectoryInviteFragment> fragmentList;

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm, List<DirectoryInviteFragment> _fragments) {
            super(fm);
            this.fragmentList = _fragments;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Logger.debug("position Destroy" + position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int position) {
            DirectoryInviteFragment ff = (DirectoryInviteFragment) super.instantiateItem(arg0, position);
            if(position == 0) // requested
            {
                ff.setOnListViewItemClickListener(requestedContactsItemClickListener);
                ff.initialList(DirectoryInviteFragment.DIRECTORY_CONTACT_REQUESTED, requestedContacts);
            }

            if(position == 1)//confirmed
            {
                ff.setOnListViewItemClickListener(confirmedContactsItemClickListener);
                ff.initialList(DirectoryInviteFragment.DIRECTORY_CONTACT_CONFIRMED , confirmedContacts);
            }
            else if(position == 2)//pending
            {
                ff.setOnListViewItemClickListener(pendingContactsItemClickListener);
                ff.initialList(DirectoryInviteFragment.DIRECTORY_CONTACT_PENDING , pendingContacts);
            }
            else if(position == 3)//not invited contacts
            {
                ff.setOnListViewItemClickListener(notInvitedContactsItemClickListener);
                ff.initialList(DirectoryInviteFragment.DIRECTORY_CONTACT_NOT_INVITED , ginkoPurpleContacts);
            }
            fragmentList.set(position , ff);
            if(position == currIndex)
                currentFragment = ff;

            if(currentFragment != null)
            {
                currentFragment.filterItemsByString(strSearchKeyword);
                if(isSelectedAll)
                    currentFragment.selectAll();
                //else
                //    currentFragment.unselectAll();
            }
            return ff;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            DirectoryInviteFragment ff = fragmentList.get(position);
            if(position == currIndex)
                currentFragment = ff;

            if(currentFragment != null)
            {
                currentFragment.filterItemsByString(strSearchKeyword);
                if(isSelectedAll)
                    currentFragment.selectAll();
                //else
                //    currentFragment.unselectAll();
            }
            if(position == 0)
            {
                ff.setOnListViewItemClickListener(requestedContactsItemClickListener);
            }
            else if(position == 1)//confirmed
            {
                ff.setOnListViewItemClickListener(confirmedContactsItemClickListener);
            }
            else if(position == 2)//pending
            {
                ff.setOnListViewItemClickListener(pendingContactsItemClickListener);
            }
            else if(position == 3)//not invited contacts
            {
                ff.setOnListViewItemClickListener(notInvitedContactsItemClickListener);
            }

            return ff;
        }

        @Override
        public int getItemPosition(Object object) {return PagerAdapter.POSITION_NONE;}
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            Logger.debug("Selected page:" + position);
            currIndex = position;
            updateTab(currIndex);

            currentFragment = fragments.get(position);

            if(position == 3)//invite contacts
            {
                deleteLayout.setVisibility(View.INVISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                int selectedItemCount = getSelectedItemsCount(ginkoPurpleContacts);
                if (selectedItemCount == getVisibleItemsCount(ginkoPurpleContacts)  && selectedItemCount != 0)
                    isSelectedAll = true;
                else
                    isSelectedAll = false;

                if(selectedItemCount>0)
                    btnConfirm.setVisibility(View.VISIBLE);
                else
                    btnConfirm.setVisibility(View.GONE);

                if(currentFragment != null)
                {
                    currentFragment.filterItemsByString(strSearchKeyword);
                    if(isSelectedAll)
                        currentFragment.selectAll();
                }

                updateSelectAllRadioBox();
            }
            else
            {
                if(position == 0)
                    listRequestContacts(false);
                else if(position == 1) //confirmed invites
                    listConfirmedContacts(false);
                else //pending invites
                    listPendingContacts(false);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}

