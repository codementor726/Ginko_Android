package com.ginko.activity.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ginko.api.request.ContactGroupRequest;
import com.ginko.api.request.IMRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.ContactListFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.VideoMemberVO;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupAddUserActivity extends MyBaseActivity implements View.OnClickListener,
                                                                    ContactListFragment.ContactItemSelectListener
{
    /* UI Objects */
    private LinearLayout activityRootView;
    private ImageButton btnPrev , btnConfirm;
    private ImageView imgSelectAllCheckBox;
    private EditText edtSearch;
    private ImageView btnCancelSearch;
    private Button btnCancel;
    private ContactListFragment contactList;

    /* Variables */
    private boolean isKeyboardVisible = false;
    private boolean isSelectedAll = false;

    private String strSearchKeyword = "";

    private int groupID = 0;
    private String existingContactIds = "";

    private int boardId = 0;
    private boolean isReturnFromConference = false;
    private boolean isFromMenu = false;
    private int conferenceType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select_contact);

        Intent intent = this.getIntent();
        this.groupID = intent.getIntExtra("groupId" , 0);
        this.existingContactIds = intent.getStringExtra("existContactIds");

        this.boardId = intent.getIntExtra("boardId", 0);
        this.isReturnFromConference = intent.getBooleanExtra("isReturnFromConference", false);
        this.isFromMenu = intent.getBooleanExtra("isFromMenu", false);
        this.conferenceType = intent.getIntExtra("conferenceType", 1);

        if(groupID == 0 && !isReturnFromConference) {
            finish();
            return;
        }

        if (boardId == 0 && isReturnFromConference) {
            if (!isFromMenu)
            {
                finish();
                return;
            } else
            {
                getUIObjects();
            }
        } else
        {
            getUIObjects();
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        activityRootView = (LinearLayout)findViewById(R.id.rootLayout);
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

        contactList = (ContactListFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_contact_list);
        contactList.setOnContactItemClickListener(this);
        if (this.isReturnFromConference == false)
        {
            contactList.setExistingContactItems(existingContactIds);
            contactList.setGroupId(groupID);
        } else
        {
            contactList.setExistingContactItems(existingContactIds);
        }

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        btnCancel = (Button)findViewById(R.id.btnCancel); btnCancel.setVisibility(View.GONE);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnCancelSearch.setVisibility(View.GONE);
                hideKeyboard();
            }
        });
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                //searchItems();
                btnCancelSearch.setVisibility(View.GONE);
                //hideKeyboard();
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
                     if(s.length()>0)
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
                    btnCancel.setVisibility(View.GONE);
                    //searchItems();
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
                    btnCancel.setVisibility(View.VISIBLE);
                } else {
                    edtSearch.setCursorVisible(false);
                    btnCancel.setVisibility(View.GONE);
                    btnCancelSearch.setVisibility(View.GONE);
                }
            }
        });

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this); btnConfirm.setVisibility(View.GONE);
        imgSelectAllCheckBox = (ImageView)findViewById(R.id.imgSelectAllCheckBox); imgSelectAllCheckBox.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancel); btnCancel.setOnClickListener(this); btnCancel.setVisibility(View.GONE);

    }

    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();
        if(strEditText.compareTo("")!=0) {
            strSearchKeyword = strEditText.toLowerCase();
            contactList.searchItems(strSearchKeyword);
        }
        else
        {
            strSearchKeyword = "";
            contactList.searchItems(strSearchKeyword);
        }

        if (contactList.getSearchedItemCount() == 0){
            imgSelectAllCheckBox.setEnabled(false);
            imgSelectAllCheckBox.setImageResource(R.drawable.share_profile_non_selected);
            btnConfirm.setVisibility(View.GONE);
        }
        else {
            imgSelectAllCheckBox.setEnabled(true);
            updateSelectAll();
            if (contactList.getSelectedItemCount() > 0) {
                btnConfirm.setVisibility(View.VISIBLE);
            } else {
                btnConfirm.setVisibility(View.GONE);
            }
        }
    }


    private void updateSelectAll()
    {
        int count = contactList.getSelectedItemCount();
        if(count > 0 && count == contactList.getVisibleItemCount())
        {
            imgSelectAllCheckBox.setImageResource(R.drawable.share_profile_selected);
            isSelectedAll = true;
        }
        else
        {
            imgSelectAllCheckBox.setImageResource(R.drawable.share_profile_non_selected);
            isSelectedAll = false;
        }
    }

    public void CreateVideoVoiceConferenceBoard(final String userIds, final List<ContactListFragment.FragmentContactItem> fragmentContactItems) {
        IMRequest.createBoard(String.valueOf(userIds), new ResponseCallBack<ImBoardVO>() {
            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    ImBoardVO board = response.getData();
                    boardId = board.getBoardId();
                    AddMemberOnConference(userIds, fragmentContactItems);
                }
            }
        });
    }

    private void AddMemberOnConference(String contactIds, final List<ContactListFragment.FragmentContactItem> fragmentContactItems)
    {
        IMRequest.setInviteNewVideoMember(boardId, contactIds, new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                if (response.isSuccess()) {
                    for (int i = 0; i < fragmentContactItems.size(); i++) {
                        ContactListFragment.FragmentContactItem oneItem = fragmentContactItems.get(i);
                        VideoMemberVO newOne = new VideoMemberVO();

                        newOne.setUserId(String.valueOf(oneItem.user_id));
                        newOne.setName(oneItem.contactName);
                        newOne.setImageUrl(oneItem.profileImage);
                        newOne.setOwner(false);
                        newOne.setYounger(true);
                        newOne.setMe(false);
                        newOne.setWeight(i + 1);
                        newOne.setInitialized(true);
                        if (conferenceType == 1)
                            newOne.setVideoStatus(true);
                        else
                            newOne.setVideoStatus(false);
                        newOne.setVoiceStatus(true);
                        MyApp.getInstance().g_videoMemberList.add(newOne);
                        MyApp.getInstance().g_videoMemIDs.add(newOne.getUserId());
                    }

                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("boardId", boardId);
                    resultIntent.putExtras(bundle);
                    GroupAddUserActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    protected boolean isValidContact(String contactId) {
        boolean isExist = false;
        if (MyApp.getInstance().isJoinedOnConference == false)
            return false;

        for (int i=0; i<MyApp.getInstance().g_videoMemberList.size(); i++)
        {
            VideoMemberVO memOne = MyApp.getInstance().g_videoMemberList.get(i);
            if (memOne.getUserId().equals(contactId))
            {
                isExist = true;
                break;
            }

        }

        if (isExist == true)
            return false;

        return true;
    }

    @Override
    public void onBackPressed()
    {
        hideKeyboard();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btnConfirm:
                List<ContactListFragment.FragmentContactItem> contacts = contactList.getSelectedVisibleContacts();
                List<ContactListFragment.FragmentContactItem> videoLists = new ArrayList<ContactListFragment.FragmentContactItem>();
                if(CollectionUtils.isEmpty(contacts)){
                    return;
                }
                String contactKeys="";
                String contactIDs="";

                for(int i=0;i<contacts.size();i++)
                {
                    ContactListFragment.FragmentContactItem contact = contacts.get(i);
                    if (isReturnFromConference == true && !isValidContact(String.valueOf(contact.user_id)))
                        continue;
                    contactKeys = contactKeys + String.valueOf(contact.contact_id)+"_"+String.valueOf(contact.contact_type);
                    contactIDs = contactIDs + String.valueOf(contact.user_id);
                    videoLists.add(contact);
                    if(i < contacts.size() -1) {
                        contactKeys = contactKeys + ",";
                        contactIDs = contactIDs + ",";
                    }
                }

                if (!isReturnFromConference)
                {
                    ContactGroupRequest.addUser(groupID , contactKeys , new ResponseCallBack<Void>() {
                                @Override
                                public void onCompleted(JsonResponse<Void> response) {
                                    if(response.isSuccess())
                                    {
                                        GroupAddUserActivity.this.finish();
                                    }
                                }
                            }
                    );
                } else {
                    if (boardId == 0)
                        CreateVideoVoiceConferenceBoard(contactIDs, videoLists);
                    else
                        AddMemberOnConference(contactIDs, videoLists);
                }
                break;

            //select all
            case R.id.imgSelectAllCheckBox:
                if(contactList.getTotalItemCounts() > 0) {
                    //hideKeyboard();
                    isSelectedAll = !isSelectedAll;
                    contactList.selectAll(isSelectedAll);
                    updateSelectAll();

                    if (contactList.getSelectedItemCount() > 0) {
                        btnConfirm.setVisibility(View.VISIBLE);
                    } else {
                        btnConfirm.setVisibility(View.GONE);
                    }
                }
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

    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);
    }


    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    public void onItemSelected(int position, boolean selected) {
        hideKeyboard();
        int count = contactList.getSelectedItemCount();
        if(count > 0) {
            //if all items are selected
            updateSelectAll();
            btnConfirm.setVisibility(View.VISIBLE);
        }
        else {
            btnConfirm.setVisibility(View.GONE);
        }
    }
}
