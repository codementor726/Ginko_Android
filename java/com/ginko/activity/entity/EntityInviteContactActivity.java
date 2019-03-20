package com.ginko.activity.entity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.common.InviteComparator;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityInviteFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityInviteContactActivity extends MyBaseFragmentActivity implements View.OnClickListener {

    /* UI Elements */
    private RelativeLayout       activityRootView;
    private LinearLayout         tabLayout , deleteLayout;
    private ImageButton          btnPrev , btnConfirm;
    private ImageView            btnDeleteInvites;
    private ImageView            btnTabConfirmed , btnTabPending , btnTabInviteContacts;
    private ImageView            imgSelectAllCheckBox;
    private TextView             txtTitle;
    private EditText             edtSearch;
    private Button               btnCancel;
    private ImageView   btnCancelSearch;

    private RelativeLayout headerlayout, bodyLayout;

    /* Variables*/

    private MyViewPager mPager;
    private MyPagerAdapter pageAdapter;
    private List<EntityInviteFragment> fragments;
    private EntityInviteFragment currentFragment;
    private int currIndex = 0;

    private List<EntityInviteContactItem> confirmedContacts;
    private List<EntityInviteContactItem> pendingContacts;
    private List<EntityInviteContactItem> notInvitedContacts;

    private MyOnPageChangeListener pageListener;

    private boolean         isKeyboardVisible = false;
    private String           strSearchKeyword = "";

    private boolean isSelectedAll = false;
    private boolean isCreate = false;

    private int entityId = 0;
    private int m_orientHeight = 0;

    private AdapterView.OnItemClickListener confirmedContactsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();
            btnCancel.setVisibility(View.GONE);
            activityRootView.requestFocus();

            if(confirmedContacts!=null && confirmedContacts.size()>position)
             {
                 EntityInviteContactItem item = confirmedContacts.get(position);
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
                 udpateSelectAllRadioBox();
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
                EntityInviteContactItem item = pendingContacts.get(position);
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
                udpateSelectAllRadioBox();
            }
        }
    };

    private AdapterView.OnItemClickListener notInvitedContactsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideKeyboard();
            btnCancel.setVisibility(View.GONE);
            activityRootView.requestFocus();

            if(notInvitedContacts!=null && notInvitedContacts.size()>position)
            {
                EntityInviteContactItem item = notInvitedContacts.get(position);
                item.setSelected(!item.isSelected());
                int selectedItemCount = getSelectedItemsCount(notInvitedContacts);
                if(selectedItemCount>0)
                     btnConfirm.setVisibility(View.VISIBLE);
                else
                    btnConfirm.setVisibility(View.GONE);
                if(currentFragment!=null)
                    currentFragment.refreshListView();

                if(selectedItemCount == getVisibleItemsCount(notInvitedContacts))
                    isSelectedAll = true;
                else
                    isSelectedAll = false;
                udpateSelectAllRadioBox();
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_invite_contact);

        if(savedInstanceState != null)
        {
            this.entityId = savedInstanceState.getInt("entityId");
            this.isCreate = savedInstanceState.getBoolean("isCreate",false);
        }
        else
        {
            this.entityId = this.getIntent().getIntExtra("entityId", 0);
            this.isCreate = this.getIntent().getBooleanExtra("isCreate", false);
        }
        if(entityId == 0) {
            finish();
            return;
        }

        confirmedContacts = new ArrayList<EntityInviteContactItem>();
        pendingContacts = new ArrayList<EntityInviteContactItem>();
        notInvitedContacts = new ArrayList<EntityInviteContactItem>();

        getUIObjects();

        listEntityInviteContacts(false);

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("entityId", this.entityId);
        outState.putBoolean("isCreate", this.isCreate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.entityId = savedInstanceState.getInt("entityId");
        this.isCreate = savedInstanceState.getBoolean("isCreate");

        confirmedContacts = new ArrayList<EntityInviteContactItem>();
        pendingContacts = new ArrayList<EntityInviteContactItem>();
        notInvitedContacts = new ArrayList<EntityInviteContactItem>();

        getUIObjects();

        listEntityInviteContacts(false);
    }

    private void initViewPager() {
        mPager = (MyViewPager) findViewById(R.id.vPager);
        mPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dsafas","dasfas");
            }
        });
        fragments = new ArrayList<EntityInviteFragment>();

        fragments.add(new EntityInviteFragment(EntityInviteFragment.ENTITY_CONTACT_CONFIRMED , confirmedContacts));
        fragments.add(new EntityInviteFragment(EntityInviteFragment.ENTITY_CONTACT_PENDING , pendingContacts));
        fragments.add(new EntityInviteFragment(EntityInviteFragment.ENTITY_CONTACT_NOT_INVITED , notInvitedContacts));

        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);
        pageListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(pageListener);
        mPager.setScanScroll(false);
        mPager.setAdapter(pageAdapter);
        currIndex = 2;
        mPager.setCurrentItem(currIndex);
        pageListener.onPageSelected(currIndex);

    }

    private void listEntityInviteContacts(final boolean isSpecified)
    {
        EntityRequest.listContacts(this.entityId , false , 1 , 200 , new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if(response.isSuccess())
                {
                    confirmedContacts.clear();
                    pendingContacts.clear();
                    notInvitedContacts.clear();

                    JSONObject resultObj = response.getData();
                    try {
                        JSONArray contactsArray = resultObj.getJSONArray("data");
                        for(int i=0;i<contactsArray.length();i++)
                        {
                            JSONObject contactObj = (JSONObject) contactsArray.get(i);
                            EntityInviteContactItem contactItem = new EntityInviteContactItem();
                            contactItem.setFirstName(contactObj.optString("fname" , ""));
                            contactItem.setMiddleName(contactObj.optString("mname" , ""));
                            contactItem.setLastName(contactObj.optString("lname" , ""));
                            contactItem.setPhotoUrl(contactObj.optString("photo_url" , ""));
                            contactItem.setContactId(contactObj.optInt("user_id"));
                            String fullName = contactItem.getFirstName();
                            fullName = fullName + " " + contactItem.getMiddleName();
                            fullName = fullName.trim();
                            fullName += contactItem.getLastName();

                            contactItem.setFullName(fullName);
                            int invite_status = contactObj.optInt("invite_status" , 0);
                            if(invite_status == 0)//not invited
                            {
                                notInvitedContacts.add(contactItem);
                            }
                            else if(invite_status == 1)//pending
                            {
                                pendingContacts.add(contactItem);
                            }
                            else //confirmed
                            {
                                confirmedContacts.add(contactItem);
                            }

                            edtSearch.setText("");
                            activityRootView.requestFocus();

                            if (isSpecified)
                            {
                                if(isSelectedAll) {
                                    isSelectedAll = false;
                                    if (currIndex == 0){
                                        mPager.setCurrentItem(2);
                                        updateTab(2);
                                    } else if (currIndex == 1) {
                                        mPager.setCurrentItem(2);
                                        updateTab(2);
                                    }
                                }
                                btnConfirm.setVisibility(View.GONE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    InviteComparator contactItemComparator = new InviteComparator(EntityInviteContactActivity.this);
                    Collections.sort(notInvitedContacts, contactItemComparator);
                    Collections.sort(pendingContacts, contactItemComparator);
                    Collections.sort(confirmedContacts, contactItemComparator);

                    for(int i=0;i<fragments.size();i++)
                    {
                        fragments.get(i).refreshListView();
                    }

                    udpateSelectAllRadioBox();
                }
                else
                {
                    Uitils.alert(EntityInviteContactActivity.this , "Failed to load contacts to invite");
                }
            }
        });
    }
    @Override
    protected void getUIObjects()
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
                        //isKeyboardVisible = false;
                        //edtSearch.setCursorVisible(false);

                        /*if(btnCancel.getVisibility() == View.VISIBLE)
                            btnCancel.setVisibility(View.GONE);*/
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
                     }
                     else {
                         btnCancelSearch.setVisibility(View.GONE);
                     }
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
                    if(isKeyboardVisible)
                        hideKeyboard();
                }
            }
        });


        if (!isCreate){
            headerlayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_prev);
            txtTitle.setTextColor(getResources().getColor(R.color.photo_video_editor_home_txt_color));
            btnConfirm.setImageResource(R.drawable.checknav);
        }
        else {
            headerlayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_back_nav_white);
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnConfirm.setImageResource(R.drawable.btn_confirm_white);
        }
        initViewPager();
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

    private void updateTab(int index)
    {
        switch(index)
        {
            case 0: //confirmed tab
                txtTitle.setText("Confirmed");

                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_sel);
                btnTabPending.setImageResource(R.drawable.entity_pending_normal);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_normal);

                break;

            case 1: //pending
                txtTitle.setText("Pending");

                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_normal);
                btnTabPending.setImageResource(R.drawable.entity_pending_sel);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_normal);

                break;

            case 2: //invite contacts(s)
                txtTitle.setText("Invite Contact(s)");

                btnTabConfirmed.setImageResource(R.drawable.entity_confirm_normal);
                btnTabPending.setImageResource(R.drawable.entity_pending_normal);
                btnTabInviteContacts.setImageResource(R.drawable.entity_contact_sel);

                break;
        }
    }

    private void udpateSelectAllRadioBox()
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

            if (currIndex == 2 && count > 0)
                btnConfirm.setVisibility(View.VISIBLE);
            else
                btnConfirm.setVisibility(View.GONE);
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

        udpateSelectAllRadioBox();
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

                for(int i=0;i<notInvitedContacts.size();i++)
                {
                    EntityInviteContactItem item = notInvitedContacts.get(i);
                    if(item.isSelected() && item.getVisibility() == true)
                        contactUids += String.valueOf(item.getContactId())+",";
                }
                if(!contactUids.equals(""))
                {
                    contactUids = contactUids.substring(0 , contactUids.length()-1);
                    EntityRequest.inviteFriends(entityId , contactUids , new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if(response.isSuccess())
                            {
                                listEntityInviteContacts(false);
                                btnConfirm.setVisibility(View.GONE);
                                if(isSelectedAll)
                                    isSelectedAll = false;
                                udpateSelectAllRadioBox();
                                finish();
                            }
                            else
                            {
                                Uitils.alert(EntityInviteContactActivity.this , "Failed to invite contact(s).");
                            }
                        }
                    });
                } else
                    finish();
                break;

            case R.id.btnTabConfirmed:
                mPager.setCurrentItem(0);
                updateTab(0);
                break;

            case R.id.btnTabPending:
                mPager.setCurrentItem(1);
                updateTab(1);
                break;

            case R.id.btnTabInviteContacts:
                mPager.setCurrentItem(2);
                updateTab(2);
                break;

            case R.id.btnDeleteInvites:
                String inviteContactIds = "";
                if(currIndex == 0) {
                    for (int i = 0; i < confirmedContacts.size(); i++) {
                        EntityInviteContactItem item = confirmedContacts.get(i);
                        if (item.isSelected())
                            inviteContactIds += String.valueOf(item.getContactId()) + ",";
                    }
                }
                else if(currIndex == 1) {
                    for (int i = 0; i < pendingContacts.size(); i++) {
                        EntityInviteContactItem item = pendingContacts.get(i);
                        if (item.isSelected())
                            inviteContactIds += String.valueOf(item.getContactId()) + ",";
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
                            EntityRequest.deleteFollowers(entityId, inviteIds, new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if (response.isSuccess()) {
                                        listEntityInviteContacts(true);
                                        tabLayout.setVisibility(View.VISIBLE);
                                        deleteLayout.setVisibility(View.INVISIBLE);
                                    } else {
                                        Uitils.alert(EntityInviteContactActivity.this, response.getErrorMessage());
                                        //Uitils.alert(EntityInviteContactActivity.this, "Failed to delete invite(s).");
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
                break;

            case R.id.imgSelectAllCheckBox:
                if(currIndex == 0 && confirmedContacts.size() == 0)
                    return;
                if(currIndex == 1 && pendingContacts.size() == 0)
                    return;
                if(currIndex == 2 && notInvitedContacts.size() == 0)
                    return;
                if (currentFragment.getVisibleCount() == 0)
                    return;

                isSelectedAll = !isSelectedAll;
                if(currIndex == 0)//confirmed invites
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
                else if(currIndex == 1) //pending invites
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
                else if(currIndex == 2)//not invited contacts
                {
                    for(int i=0; i<notInvitedContacts.size(); i++)
                    {
                        if (notInvitedContacts.get(i).getVisibility() == true)
                            notInvitedContacts.get(i).setSelected(!notInvitedContacts.get(i).isSelected());
                    }
                    if(isSelectedAll)
                    {
                        currentFragment.selectAll();
                        if(getSelectedItemsCount(notInvitedContacts)>0)
                            btnConfirm.setVisibility(View.VISIBLE);
                        else
                            btnConfirm.setVisibility(View.GONE);
                    }
                    else {
                        btnConfirm.setVisibility(View.GONE);
                        currentFragment.unselectAll();
                    }
                }

                udpateSelectAllRadioBox();

                if(currentFragment!=null)
                    currentFragment.refreshListView();
                break;
        }
    }

    private int getSelectedItemsCount(List<EntityInviteContactItem> inviteItems)
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

    private int getVisibleItemsCount(List<EntityInviteContactItem> inviteItems)
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

        private List<EntityInviteFragment> fragmentList;

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm, List<EntityInviteFragment> _fragments) {
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
            EntityInviteFragment ff = (EntityInviteFragment) super.instantiateItem(arg0, position);
            if(position == 0)//confirmed
            {
                ff.setOnListViewItemClickListener(confirmedContactsItemClickListener);
                ff.initialList(EntityInviteFragment.ENTITY_CONTACT_CONFIRMED , confirmedContacts);
            }
            else if(position == 1)//pending
            {
                ff.setOnListViewItemClickListener(pendingContactsItemClickListener);
                ff.initialList(EntityInviteFragment.ENTITY_CONTACT_PENDING , pendingContacts);
            }
            else if(position == 2)//not invited contacts
            {
                ff.setOnListViewItemClickListener(notInvitedContactsItemClickListener);
                ff.initialList(EntityInviteFragment.ENTITY_CONTACT_NOT_INVITED , notInvitedContacts);
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
            EntityInviteFragment ff = fragmentList.get(position);
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
            if(position == 0)//confirmed
            {
                ff.setOnListViewItemClickListener(confirmedContactsItemClickListener);
            }
            else if(position == 1)//pending
            {
                ff.setOnListViewItemClickListener(pendingContactsItemClickListener);
            }
            else if(position == 2)//not invited contacts
            {
                ff.setOnListViewItemClickListener(notInvitedContactsItemClickListener);
            }

            return ff;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            Logger.debug("Selected page:" + position);
            currIndex = position;
            updateTab(currIndex);

            currentFragment = fragments.get(position);

            if(position == 2)//invite contacts
            {
                deleteLayout.setVisibility(View.INVISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                int selectedItemCount = getSelectedItemsCount(notInvitedContacts);
                if (selectedItemCount == getVisibleItemsCount(notInvitedContacts)  && selectedItemCount != 0)
                    isSelectedAll = true;
                else
                    isSelectedAll = false;

                if(selectedItemCount>0)
                    btnConfirm.setVisibility(View.VISIBLE);
                else
                    btnConfirm.setVisibility(View.GONE);
            }
            else
            {
                int selectedItemCount = 0;
                if(position == 0) //confirmed invites
                {
                    selectedItemCount = getSelectedItemsCount(confirmedContacts);
                    if (selectedItemCount == getVisibleItemsCount(confirmedContacts) && selectedItemCount != 0)
                        isSelectedAll = true;
                    else
                        isSelectedAll = false;
                }
                else //pending invites
                {
                    selectedItemCount = getSelectedItemsCount(pendingContacts);
                    if (selectedItemCount == getVisibleItemsCount(pendingContacts) && selectedItemCount != 0)
                        isSelectedAll = true;
                    else
                        isSelectedAll = false;
                }

                if(selectedItemCount>0) {
                    deleteLayout.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.INVISIBLE);
                }
                else
                {
                    deleteLayout.setVisibility(View.INVISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                }

                btnConfirm.setVisibility(View.GONE);
            }

            if(currentFragment != null)
            {
                currentFragment.filterItemsByString(strSearchKeyword);
                if(isSelectedAll)
                    currentFragment.selectAll();
//                else
//                    currentFragment.unselectAll();
            }

            udpateSelectAllRadioBox();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
