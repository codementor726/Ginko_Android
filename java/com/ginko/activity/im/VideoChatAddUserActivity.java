package com.ginko.activity.im;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
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

import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.group.GroupDetailActivity;
import com.ginko.api.request.IMRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.ContactListFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EventUser;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.VideoMemberVO;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoChatAddUserActivity extends MyBaseActivity implements View.OnClickListener,
        ContactListFragment.ContactItemSelectListener
{
    private LinearLayout activityRootView;
    private ImageButton btnPrev , btnConfirm;
    private EditText edtSearch;
    private ImageView btnCancelSearch;
    private Button btnCancel;
    private ContactListFragment contactList;
    private TextView txtTitle;

    /* Variables */
    private boolean isKeyboardVisible = false;
    private String strSearchKeyword = "";
    private String existingContactIds = "";

    private int boardId = 0;
    private boolean isReturnFromConference = false;
    private boolean isGroupFrom = false;
    private boolean isImBoard = false;
    private int conferenceType = 1;
    private int existCount = 0;
    private ArrayList<ContactItem> lstUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_add_user);

        Intent intent = this.getIntent();
        this.existingContactIds = intent.getStringExtra("existContactIds");
        this.boardId = intent.getIntExtra("boardId", 0);
        this.isReturnFromConference = intent.getBooleanExtra("isReturnFromConference", false);
        this.isGroupFrom = intent.getBooleanExtra("isGroupFrom", false);
        this.isImBoard = intent.getBooleanExtra("isImBoard", false);
        this.lstUsers = new ArrayList<ContactItem>();

        ArrayList<ContactItem> tempListUsers = new ArrayList<ContactItem>();
        List<ContactItem> tempImBoard = new ArrayList<>();

        if (isImBoard) {
            tempImBoard = GroupDetailActivity.sltContactList;
            ContactItem ImItem = null;
            if (tempImBoard != null && tempImBoard.size() > 0) {
                for (int i = 0; i < tempImBoard.size(); i++) {
                    ImItem = tempImBoard.get(i);
                    tempListUsers.add(ImItem);
                }
            }
        } else {
            tempListUsers = (ArrayList<ContactItem>) getIntent().getSerializableExtra("lstUsers");
        }

        if(tempListUsers != null)
        {
            for(int i=0; i<tempListUsers.size(); i++)
            {
                if(tempListUsers.get(i).getSharingStatus() == 4)
                    continue;
                this.lstUsers.add(tempListUsers.get(i));
            }
        }
        if (this.existingContactIds != null && !this.existingContactIds.equals(""))
            existCount = Arrays.asList(existingContactIds.split(",")).size();

        getUIObjects();
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
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        String strTitle = "Select up to " + String.valueOf(7-existCount);
        txtTitle.setText(strTitle);
        contactList = (ContactListFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_contact_list);
        contactList.setOnContactItemClickListener(this);

        contactList.setExistingContactItems(existingContactIds);
        contactList.setAddVideoFrom(true);
        if (isGroupFrom) {
            contactList.setGroupFrom(true);
            contactList.setContactsOriginal(lstUsers);
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
        });

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
            btnConfirm.setVisibility(View.GONE);
        }
        else {
            if (contactList.getSelectedItemCount() > 0) {
                btnConfirm.setVisibility(View.VISIBLE);
            } else {
                btnConfirm.setVisibility(View.GONE);
            }
        }
    }

    public void CreateVideoVoiceConferenceBoard(final String userIds, final List<ContactListFragment.FragmentContactItem> fragmentContactItems, final int callType) {
        IMRequest.createBoard(String.valueOf(userIds), new ResponseCallBack<ImBoardVO>() {
            @Override
            public void onCompleted(JsonResponse<ImBoardVO> response) {
                if (response.isSuccess()) {
                    ImBoardVO board = response.getData();
                    boardId = board.getBoardId();
                    RunningVideoScreen(board, callType);
                }
            }
        });
    }

    private void RunningVideoScreen(ImBoardVO board, int callType)
    {
        if (board == null)
            return;

        MyApp.getInstance().isOwnerForConfernece = true;
        MyApp.getInstance().initializeVideoVariables();

        ArrayList<EventUser> userData = new ArrayList<EventUser>();
        EventUser ownUser = new EventUser();
        ownUser.setFirstName(RuntimeContext.getUser().getFirstName());
        ownUser.setLastName(RuntimeContext.getUser().getLastName());
        ownUser.setPhotoUrl(RuntimeContext.getUser().getPhotoUrl());
        ownUser.setUserId(RuntimeContext.getUser().getUserId());
        userData.add(ownUser);

        VideoMemberVO currMember = new VideoMemberVO();
        currMember.setUserId(String.valueOf(ownUser.getUserId()));
        currMember.setName(ownUser.getFullName());
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
        MyApp.getInstance().g_videoMemberList.add(currMember);
        MyApp.getInstance().g_videoMemIDs.add(currMember.getUserId());

        for (int i=0; i<board.getMembers().size(); i++)
        {
            VideoMemberVO memberOne = new VideoMemberVO();
            EventUser indexOne = board.getMembers().get(i).getUser();
            if (indexOne.getUserId() == RuntimeContext.getUser().getUserId())
                continue;

            memberOne.setUserId(String.valueOf(indexOne.getUserId()));
            memberOne.setName(indexOne.getFullName());
            memberOne.setImageUrl(indexOne.getPhotoUrl());
            memberOne.setOwner(false);
            memberOne.setMe(false);
            memberOne.setWeight(i + 1);
            memberOne.setInitialized(true);
            memberOne.setYounger(true);

            if (callType == 1)
                memberOne.setVideoStatus(true);
            else
                memberOne.setVideoStatus(false);

            memberOne.setVoiceStatus(true);

            userData.add(indexOne);
            MyApp.getInstance().g_videoMemberList.add(memberOne);
            MyApp.getInstance().g_videoMemIDs.add(memberOne.getUserId());
        }

        Intent groupVideoIntent = new Intent(VideoChatAddUserActivity.this, GroupVideoChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("boardId", boardId);
        bundle.putInt("callType", callType);
        bundle.putString("conferenceName", board.getBoardName());
        bundle.putSerializable("userData", userData);
        bundle.putBoolean("isInitial", true);
        groupVideoIntent.putExtras(bundle);
        startActivity(groupVideoIntent);
        finish();
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
                    VideoChatAddUserActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    protected boolean isValidContact(String contactId) {
        boolean isExist = false;

        if (MyApp.getInstance().g_videoMemberList == null)
            return true;

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

    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btnConfirm:
                if (isReturnFromConference == false) {
                    hideKeyboard();
                    showBottomCallWindow(v);
                } else
                    GotoVideoChat(1);
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

    private void showBottomCallWindow(View v)
    {
        List<String> phones = new ArrayList<String>();
        phones.add(0, "Ginko Voice Call");
        phones.add(1, "Ginko Video Call");
        phones.add(2, "Cancel");

        final List<String> buttons = phones;
        final BottomPopupWindow popupWindow = new BottomPopupWindow(VideoChatAddUserActivity.this, buttons);
        popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
            @Override
            public void onClick(View button, int position) {
                String text = buttons.get(position);
                if (text == "Cancel") {
                    popupWindow.dismiss();
                    if (edtSearch.hasFocus()) {
                        showKeyboard();
                    } else {
                        hideKeyboard();
                    }
                } else if (text == "Ginko Video Call") {
                    GotoVideoChat(1);
                } else if (text == "Ginko Voice Call") {
                    GotoVideoChat(2);
                }
            }
        });
        popupWindow.show(v);
    }

    public void GotoVideoChat(int callType)
    {
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
            if (!isValidContact(String.valueOf(contact.user_id)))
                continue;
            contactIDs = contactIDs + String.valueOf(contact.user_id);
            videoLists.add(contact);
            if(i < contacts.size() -1)
                contactIDs = contactIDs + ",";
        }


        if (boardId == 0)
            CreateVideoVoiceConferenceBoard(contactIDs, videoLists, callType);
        else
            AddMemberOnConference(contactIDs, videoLists);
    }

    @Override
    public void onItemSelected(int position, boolean selected) {
        hideKeyboard();
        int count = contactList.getSelectedItemCount();
        int totalCnt = 7 - existCount;

        if (count > totalCnt) {
            String alertMsg = "Oops! you selected more than " + String.valueOf(totalCnt) + " contacts";
            MyApp.getInstance().showSimpleAlertDiloag(VideoChatAddUserActivity.this, alertMsg, null);
            contactList.setSelected(position, false);
        }
        else
        {
            if(count > 0) {
                //if all items are selected
                btnConfirm.setVisibility(View.VISIBLE);
            }
            else {
                btnConfirm.setVisibility(View.GONE);
            }
        }
    }
}