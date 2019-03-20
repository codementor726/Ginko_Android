package com.ginko.activity.exchange;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.directory.DirMailSelectActivity;
import com.ginko.activity.group.GroupDetailActivity;
import com.ginko.activity.im.ImPreActivity;
import com.ginko.activity.menu.LoginSettingActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.ContactGroupRequest;
import com.ginko.common.Logger;
import com.ginko.customview.InputDialog;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.BaseExchangeFragment;
import com.ginko.fragments.InviteFragment;
import com.ginko.fragments.InviteListFragment;
import com.ginko.fragments.PendingRequestFragment;
import com.ginko.fragments.RequestsFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.GroupVO;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExchangeRequestActivity extends MyBaseFragmentActivity implements View.OnClickListener ,
        BaseExchangeFragment.onBadgeUpdateListener ,
        BaseExchangeFragment.onCheckNotesListener
{

    private final int SHARE_YOUR_LEAF_ACTIVITY = 2;
    private final int VALIDATE_LINK_ACTIVITY = 3;

    private RelativeLayout activityRootView;
    private ImageButton btnPrev;
    private ImageView btnEdit , btnInviteContact , btnClose , btnDelete;
    private TextView txtHeaderTitle , txtPageHint, txtTabNotes;
    private TextView txtPendingBadgeNum , txtInviteBadgeNum , txtRequestsBadgeNum;
    private LinearLayout flag_left , flag_middle , flag_right;
    private LinearLayout bottomNavLayout;
    private RelativeLayout bottomDeleteLayout;
    private EditText edtSearch;
    private ImageView btnCancelSearch;
    private Button btnCancel;
    private String keywordCurr1 = "", keywordCurr2 = "";

	private BaseExchangeFragment currentFragment;
    public static final String PENDING= "Pending";
    public static final String REQUEST= "Request";
    public static final String INVITE= "invite";

    private MyPagerAdapter pageAdapter = null;
    private ViewPager mPager;
    private List<android.support.v4.app.Fragment> fragments = new ArrayList<android.support.v4.app.Fragment>();
    private int currIndex = 1;//default page is invite page
    private int boardId = 0;
	
//	private List<BaseExchangeFragment> framgents = new ArrayList<BaseExchangeFragment>();

    private BaseExchangeFragment pendingRequestFragment = new PendingRequestFragment();
    private BaseExchangeFragment requestsFragment = new RequestsFragment();
    //private BaseExchangeFragment inviteFragment = new InviteListFragment();

    private boolean     isEditable = false;
    private boolean     isKeyboardVisible = false;

    private Pattern pattern;


    private ContactChangeReceiver contactChangeReceiver; private boolean isContactChangeReceiverRegistered = false;
    private ExchangeRequestReceiver exchangeRequestReceiver; private boolean isExchangeRequestReceiverRegistered = false;


    private BaseExchangeFragment getFramgents(String type){
		BaseExchangeFragment fragment = null;
		if (type.equalsIgnoreCase(PENDING)) {
			fragment = pendingRequestFragment;
		} else if (type.equalsIgnoreCase(REQUEST)) {
			fragment = requestsFragment;
		}/* else if (type.equalsIgnoreCase(INVITE)) {
			fragment = inviteFragment;
		}*/
		return fragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exchange_request);

        Intent intent = this.getIntent();
        currIndex = intent.getIntExtra("first_pageindex" , 1);
        boardId = intent.getIntExtra("contactId", 0);

        if (boardId > 0)
        {
            if (MyApp.getInstance().g_contactIDs.contains(boardId))
            {
                Intent newIntent = new Intent(ExchangeRequestActivity.this, ContactMainActivity.class);
                startActivity(newIntent);
                finish();
            }
        }

        getUIObjects();
        this.contactChangeReceiver = new ContactChangeReceiver();
        this.exchangeRequestReceiver = new ExchangeRequestReceiver();

        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        if(this.exchangeRequestReceiver != null)
        {
            IntentFilter exchangeRequestReceiver = new IntentFilter();
            exchangeRequestReceiver.addAction("android.intent.action.EXCHANGE_REQUEST");
            registerReceiver(this.exchangeRequestReceiver, exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = true;
        }
	}

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        activityRootView = (RelativeLayout)findViewById(R.id.rootLayout);
        activityRootView.setFocusable(true);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        edtSearch.setCursorVisible(true);

                        if (edtSearch.getText().toString().length() > 0)
                            btnCancelSearch.setVisibility(View.VISIBLE);
                        else
                            btnCancelSearch.setVisibility(View.GONE);

                        if (btnCancel.getVisibility() == View.GONE)
                            btnCancel.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        edtSearch.setCursorVisible(false);
                        if (btnCancelSearch.getVisibility() == View.VISIBLE)
                            btnCancelSearch.setVisibility(View.GONE);
                        if (btnCancel.getVisibility() == View.VISIBLE)
                            btnCancel.setVisibility(View.GONE);
                    }
                }
            }
        });

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnEdit = (ImageView)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);
        btnInviteContact = (ImageView)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);
        btnInviteContact.setVisibility(View.GONE);//temp
        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);

        txtHeaderTitle = (TextView)findViewById(R.id.txtTitle);
        txtPageHint = (TextView)findViewById(R.id.txtPageHint);
        txtTabNotes = (TextView)findViewById(R.id.txtTabNotes); txtTabNotes.setVisibility(View.GONE);

        txtPendingBadgeNum = (TextView)findViewById(R.id.pending_request_count);
        txtInviteBadgeNum = (TextView)findViewById(R.id.invite_count);
        txtRequestsBadgeNum = (TextView)findViewById(R.id.request_count);

        flag_left = (LinearLayout)findViewById(R.id.flag_left);
        flag_middle = (LinearLayout)findViewById(R.id.flag_middle);
        flag_right = (LinearLayout)findViewById(R.id.flag_right);

        bottomNavLayout = (LinearLayout)findViewById(R.id.contact_navi);
        bottomDeleteLayout = (RelativeLayout)findViewById(R.id.deleteLayout);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
                if (currIndex == 0)
                    keywordCurr1 = "";
                else
                    keywordCurr2 = "";
                searchItems();
                btnCancelSearch.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                isKeyboardVisible = true;
                hideKeyboard();
            }
        });
        edtSearch = (EditText)findViewById(R.id.edtSearch);
        edtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setCursorVisible(true);
                btnCancel.setVisibility(View.VISIBLE);
            }
        });
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currIndex == 0)
                    keywordCurr1 = "";
                else
                    keywordCurr2 = "";
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
                     if(s.length()>0){
                         btnCancelSearch.setVisibility(View.VISIBLE);
                     }
                     else {
                         btnCancelSearch.setVisibility(View.GONE);
                     }
                     if (currIndex == 0)
                         keywordCurr1 = edtSearch.getText().toString().trim();
                     else
                         keywordCurr2 = edtSearch.getText().toString();

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

                    if(edtSearch.getText().toString().length()>0){
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    }
                    else {
                        btnCancelSearch.setVisibility(View.GONE);
                    }

                    activityRootView.requestFocus();
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
                    if(edtSearch.getText().toString().length()>0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                } else {
                    edtSearch.setCursorVisible(false);
                    //btnCancelSearch.setVisibility(View.GONE);
                }
            }
        });


        initViewPager();
    }

    private void hideKeyboard()
    {
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);
    }

    private void initViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        pageAdapter = new MyPagerAdapter(this.getSupportFragmentManager(),
                fragments);

        fragments.add(pendingRequestFragment);
        //fragments.add(inviteFragment);
        fragments.add(requestsFragment);

        mPager.setAdapter(pageAdapter);
        //MyOnPageChangeListener pageChangeListener = new MyOnPageChangeListener();
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        //mPager.setScanScroll(false);

        pendingRequestFragment.setOnBadgeUpdateListener(this);
        pendingRequestFragment.setOnCheckNotesUpdateListener(this);
        requestsFragment.setOnBadgeUpdateListener(this);
        requestsFragment.setOnCheckNotesUpdateListener(this);

        mPager.setCurrentItem(currIndex);
        //pageChangeListener.onPageSelected(currIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //this.setBadge();
        if(pendingRequestFragment!=null)
            pendingRequestFragment.loadData();
        //if(inviteFragment!=null)
        //    inviteFragment.loadData();
        if(requestsFragment!=null)
            requestsFragment.loadData();

        if(contactChangeReceiver == null)
            this.contactChangeReceiver = new ContactChangeReceiver();
        if(exchangeRequestReceiver == null)
            this.exchangeRequestReceiver = new ExchangeRequestReceiver();

        if (this.contactChangeReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactChangeReceiver, msgReceiverIntent);
            isContactChangeReceiverRegistered = true;
        }

        if(this.exchangeRequestReceiver != null)
        {
            IntentFilter exchangeRequestReceiver = new IntentFilter();
            exchangeRequestReceiver.addAction("android.intent.action.EXCHANGE_REQUEST");
            registerReceiver(this.exchangeRequestReceiver, exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        if (this.contactChangeReceiver != null && isContactChangeReceiverRegistered == true) {
            unregisterReceiver(this.contactChangeReceiver);
            isContactChangeReceiverRegistered = false;
        }

        if(this.exchangeRequestReceiver != null && isExchangeRequestReceiverRegistered == true)
        {
            unregisterReceiver(exchangeRequestReceiver);
            isExchangeRequestReceiverRegistered = false;
        }

        super.onPause();
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);

    }

    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();

        if(currentFragment!=null) {
            if (currIndex == 0)
                currentFragment.filter(strEditText.toLowerCase());
            else
                currentFragment.filter(strEditText.toLowerCase());
        }

        checkTabNotes();
    }

    private void checkTabNotes()
    {
        if (currIndex == 0)
        {
            try {

                if (pendingRequestFragment.getAdapter().getVisibleItemCount() > 0) {
                    txtTabNotes.setVisibility(View.GONE);
                    if (isEditable)
                        btnEdit.setImageResource(R.drawable.done_contact);
                    else
                        btnEdit.setImageResource(R.drawable.editcontact);
                }
                else {
                    txtTabNotes.setText("Sorry no pending contacts.");
                    txtTabNotes.setVisibility(View.VISIBLE);
                    if (isEditable)
                        btnEdit.setImageResource(R.drawable.done_contact);
                    else
                        btnEdit.setImageResource(R.drawable.editcontact_disable);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        } else if (currIndex == 1)
        {
            try {
                if (requestsFragment.getAdapter().getVisibleItemCount() > 0) {
                    txtTabNotes.setVisibility(View.GONE);
                    if (isEditable)
                        btnEdit.setImageResource(R.drawable.done_contact);
                    else
                        btnEdit.setImageResource(R.drawable.editcontact);
                }else {
                    txtTabNotes.setText("Sorry no requests.");
                    txtTabNotes.setVisibility(View.VISIBLE);
                    if (isEditable)
                        btnEdit.setImageResource(R.drawable.done_contact);
                    else
                        btnEdit.setImageResource(R.drawable.editcontact_disable);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }


    private void setBadge() {
        CBRequest.getExchangeSummary(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject data = response.getData();
                    int pendingTotal = data.optInt("pending", 0);
                    int inviteTotal = data.optInt("invite", 0);
                    int requestTotal = data.optInt("requests", 0);

                    if(pendingTotal>0) {
                        txtPendingBadgeNum.setVisibility(View.VISIBLE);
                        txtPendingBadgeNum.setText(String.valueOf(pendingTotal));
                    }
                    else
                    {
                        txtPendingBadgeNum.setVisibility(View.INVISIBLE);
                    }
                    if(inviteTotal>0) {
                        txtInviteBadgeNum.setVisibility(View.VISIBLE);
                        txtInviteBadgeNum.setText(String.valueOf(inviteTotal));
                    }
                    else
                    {
                        txtInviteBadgeNum.setVisibility(View.INVISIBLE);
                    }
                    if(requestTotal>0) {
                        txtTabNotes.setVisibility(View.GONE);
                        txtRequestsBadgeNum.setVisibility(View.VISIBLE);
                        txtRequestsBadgeNum.setText(String.valueOf(requestTotal));
                    }
                    else
                    {
                        txtRequestsBadgeNum.setVisibility(View.INVISIBLE);
                    }
                }
                else
                {
                    txtPendingBadgeNum.setVisibility(View.INVISIBLE);
                    txtInviteBadgeNum.setVisibility(View.INVISIBLE);
                    txtRequestsBadgeNum.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

	@Override
	public void onDestroy() {
		this.currentFragment = null;
		this.fragments.clear();
        hideKeyboard();
		super.onDestroy();
	}

	public void onClickPending(View view) { 
		this.onClickTab(R.id.btn_pending_request);
	}
	public void onClickInvite(View view) {
		this.onClickTab(R.id.btn_invite);
	}
	public void onClickRequest(View view) {
		this.onClickTab(R.id.btn_requests);
	}
	
	private void onClickTab(int tabId){
		String type ="";
		if (tabId == R.id.btn_pending_request){
			type = PENDING;
            mPager.setCurrentItem(0);
		} else if (tabId == R.id.btn_invite){
            mPager.setCurrentItem(1);
			type = INVITE;
		} else if (tabId == R.id.btn_requests){
            mPager.setCurrentItem(2);
			type = REQUEST;
		}
	}

	private void changeNarTabToSelectedStyle(View view){
		View[] tabs = {findViewById(R.id.btn_pending_request),findViewById(R.id.btn_invite),findViewById(R.id.btn_requests)};
		for (View tab : tabs) {
			if (tab.getId() == view.getId()){
				tab.setBackgroundResource(R.color.grey_half_transport);
			}else{
				tab.setBackgroundResource(android.R.color.transparent);
			}
		}
	}
	
	private void changeTitle(){
		String description = "";
		String title =  "";
        switch(currIndex)
        {
            case 0://pending
                title= "Pending";
                description = "Waiting for response";
                edtSearch.setText(keywordCurr1);
                break;
            /*case 1://invite
                title= "Invite";
                description = "Invite to share info";
                break;*///temp
            case 1://requests
                title= "Requests";
                description = "Requests to share info";
                edtSearch.setText(keywordCurr2);
                break;
        }

        txtPageHint.setText(description);
		txtHeaderTitle.setText(title);

	}

    private void updateFromEditable()
    {
        if(isEditable)
        {
            btnPrev.setVisibility(View.GONE);
            btnClose.setVisibility(View.VISIBLE);
            bottomNavLayout.setVisibility(View.GONE);
            bottomDeleteLayout.setVisibility(View.VISIBLE);
            btnDelete.setImageResource(R.drawable.img_trash_disable);
            btnInviteContact.setVisibility(View.GONE);

            btnEdit.setImageResource(R.drawable.done_contact);
            btnEdit.setScaleType(ImageView.ScaleType.FIT_XY);

            if(currentFragment!=null)
            {
                currentFragment.setIsSelectable(true);
                currentFragment.updateListView();
            }
        }
        else
        {
            btnPrev.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.GONE);
            bottomNavLayout.setVisibility(View.VISIBLE);
            bottomDeleteLayout.setVisibility(View.GONE);

            /*if(currIndex == 1)//invite
                btnInviteContact.setVisibility(View.VISIBLE);
            else
                btnInviteContact.setVisibility(View.GONE);*/

            edtSearch.setText("");
            if (currIndex == 0)
                keywordCurr1 = "";
            else
                keywordCurr2 = "";
            searchItems();
            btnCancelSearch.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            isKeyboardVisible = true;
            hideKeyboard();

            btnEdit.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (currentFragment!=null)
            {
                currentFragment.setIsSelectable(false);
                currentFragment.updateListView();
            }
        }
    }
    private boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        if(pattern == null)
            pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK && data!=null)
        {
            switch (requestCode)
            {
                case SHARE_YOUR_LEAF_ACTIVITY:
                    int nStatus = data.getIntExtra("status_code", 0);
                    showValuableMessage(nStatus);
                    break;
                case VALIDATE_LINK_ACTIVITY:
                    if (data.getBooleanExtra("isSuccess", false))
                        showValuableMessage(1);
                    break;
            }
        }
    }

    public void showValuableMessage(int nStatus)
    {
        switch (nStatus)
        {
            case 1:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeRequestActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_1));
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                TextView title = new TextView(this);
                title.setText("Congratulations!");
                //title.setBackgroundColor(Color.DKGRAY);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLUE);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                builder.setCancelable(false);
                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 2:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeRequestActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_2));
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                TextView title = new TextView(this);
                title.setText("Thank you!");
                //title.setBackgroundColor(Color.DKGRAY);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLUE);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                builder.setCancelable(false);
                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 3:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeRequestActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_3));
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                TextView title = new TextView(this);
                title.setText("Almost done!");
                //title.setBackgroundColor(Color.DKGRAY);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLUE);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                builder.setCancelable(false);
                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent cbSelectIntent = new Intent(ExchangeRequestActivity.this , DirMailSelectActivity.class);
                        startActivityForResult(cbSelectIntent, VALIDATE_LINK_ACTIVITY);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 4:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeRequestActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_4));
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                TextView title = new TextView(this);
                title.setText("Fantastic!");
                //title.setBackgroundColor(Color.DKGRAY);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLUE);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                builder.setCancelable(false);
                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 5:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeRequestActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_5));
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                TextView title = new TextView(this);
                title.setText("Almost done!");
                //title.setBackgroundColor(Color.DKGRAY);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLUE);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                builder.setCancelable(false);
                builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent cbSelectIntent = new Intent(ExchangeRequestActivity.this , DirMailSelectActivity.class);
                        startActivityForResult(cbSelectIntent, VALIDATE_LINK_ACTIVITY);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
            case 6:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeRequestActivity.this);
                TextView myMsg = new TextView(this);
                myMsg.setText(getResources().getString(R.string.str_confirm_directory_join_status_6));
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(myMsg);

                TextView title = new TextView(this);
                title.setText("Sorry!");
                //title.setBackgroundColor(Color.DKGRAY);
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLUE);
                title.setTextSize(20);
                builder.setCustomTitle(title);

                builder.setCancelable(false);
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int paramInt) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        Intent contentIntent = new Intent(ExchangeRequestActivity.this, LoginSettingActivity.class);
                        startActivity(contentIntent);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            break;
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                MyApp.getInstance().hideKeyboard(activityRootView);


                finish();
                break;

            //invite contact
            /*case R.id.btnInviteContact:
                final String inputErrHint = "Please enter email address.";

                final InputDialog inviteEmailDialog = new InputDialog(this,
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                        getResources().getString(R.string.str_invite_email_dialog_description) , //title
                        false , //show titlebar
                        getResources().getString(R.string.str_cancel) , //left button name
                        new InputDialog.OnButtonClickListener(){
                            @Override
                            public boolean onClick(Dialog dialog , View v, String input) {
                                return  true;
                            }//left button clicklistener
                        },
                        getResources().getString(R.string.str_continue), //right button name
                        new InputDialog.OnButtonClickListener() //right button clicklistener
                        {
                            @Override
                            public boolean onClick(Dialog dialog , View v , String email) {
                                if(email.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(ExchangeRequestActivity.this, inputErrHint, Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                if(!isEmailValid(email.trim()))
                                {
                                    Toast.makeText(ExchangeRequestActivity.this, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
                                    return false;
                                }

                                CBRequest.addInvitation(new String[]{email}, new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                        if (response.isSuccess()){
                                            inviteFragment.loadData();
                                        }
                                    }
                                });
                                return true;
                            }
                        },
                        new InputDialog.OnEditorDoneActionListener() {

                            @Override
                            public void onEditorActionDone(Dialog dialog, String email) {

                                if(email.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(ExchangeRequestActivity.this, inputErrHint, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(!isEmailValid(email.trim()))
                                {
                                    Toast.makeText(ExchangeRequestActivity.this, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                dialog.dismiss();

                                CBRequest.addInvitation(new String[]{email}, new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                        if (response.isSuccess()){
                                            inviteFragment.loadData();
                                        }
                                    }
                                });

                            }
                        }
                );
                inviteEmailDialog.show();
                inviteEmailDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        inviteEmailDialog.showKeyboard();
                    }
                });
                break;*/

            //close editable status
            case R.id.btnClose:
                //if(isEditable)
                //{
                    isEditable = false;
                    updateFromEditable();
                //}
                break;

            case R.id.btnEdit:
                if (currIndex == 0 && pendingRequestFragment.getAdapter().getVisibleItemCount() < 1)
                    return;
                if (currIndex == 1 && requestsFragment.getAdapter().getVisibleItemCount() < 1)
                    return;

                if(!isEditable)
                {
                    isEditable = true;
                    updateFromEditable();
                } else {
                    isEditable = false;
                    updateFromEditable();
                }
                break;

            case R.id.btnDelete:
                if(currentFragment.getSelectedItemsCount()>0) {
                    currentFragment.deleteItems();
                }
                break;
        }
    }

    @Override
    public void onBadgeUpdated(int pageIndex, int badgeCount) {

        switch(pageIndex)
        {
            case 0://pending
                if(badgeCount>0) {
                    txtPendingBadgeNum.setVisibility(View.VISIBLE);
                    txtPendingBadgeNum.setText(String.valueOf(badgeCount));
                }
                else
                {
                    txtPendingBadgeNum.setVisibility(View.INVISIBLE);
                }
                break;
            case 1: //invite
                if(badgeCount>0) {
                    txtInviteBadgeNum.setVisibility(View.VISIBLE);
                    txtInviteBadgeNum.setText(String.valueOf(badgeCount));
                }
                else
                {
                    txtInviteBadgeNum.setVisibility(View.INVISIBLE);
                }
                break;
            case 2://requests
                if(badgeCount>0) {
                    txtRequestsBadgeNum.setVisibility(View.VISIBLE);
                    txtRequestsBadgeNum.setText(String.valueOf(badgeCount));
                }
                else
                {
                    txtRequestsBadgeNum.setVisibility(View.INVISIBLE);
                }
                break;
        }

        //checkTabNotes();
    }

    public void removeEditable()
    {
        //if (currentFragment.getAdapter().getVisibleItemCount() == 0) {
            isEditable = false;
            updateFromEditable();
        //}
    }

    public int getCurrIndex() {return currIndex;}

    @Override
    public void onCheckNotesUpdated(boolean closeEditable) {
        if (closeEditable)
        {
            isEditable = false;
            updateFromEditable();
        } else
            searchItems();
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        private List<android.support.v4.app.Fragment> infos;

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm, List<android.support.v4.app.Fragment> infos) {
            super(fm);
            this.infos = infos;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Logger.debug("position Destroy" + position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public Object instantiateItem(ViewGroup arg0, int position) {
            android.support.v4.app.Fragment ff = (android.support.v4.app.Fragment) super.instantiateItem(arg0, position);
            return ff;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            return infos.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            Log.d("selectpage", ""+position);
            currIndex = position;
            isEditable = false;
            updateFromEditable();
            
            if (currIndex == 0) {
                flag_left.setVisibility(View.VISIBLE);
                flag_middle.setVisibility(View.GONE);
                flag_right.setVisibility(View.INVISIBLE);

                currentFragment = pendingRequestFragment;
                btnInviteContact.setVisibility(View.GONE);

                if(!currentFragment.getIsSelectable()) {
                    btnPrev.setVisibility(View.VISIBLE);
                    btnClose.setVisibility(View.GONE);
                    bottomNavLayout.setVisibility(View.VISIBLE);
                    bottomDeleteLayout.setVisibility(View.GONE);
                }else {
                    btnPrev.setVisibility(View.GONE);
                    btnClose.setVisibility(View.VISIBLE);
                    bottomNavLayout.setVisibility(View.GONE);
                    bottomDeleteLayout.setVisibility(View.VISIBLE);
                }

                /*
                try {
                    if (pendingRequestFragment.getAdapter().getVisibleItemCount() > 0) {
                        txtTabNotes.setVisibility(View.GONE);
                        btnEdit.setImageResource(R.drawable.editcontact);
                    }
                    else {
                        txtTabNotes.setText("Sorry no pending contacts.");
                        txtTabNotes.setVisibility(View.VISIBLE);
                        btnEdit.setImageResource(R.drawable.editcontact_disable);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                */
            } /*else if (currIndex == 1) {
                flag_left.setVisibility(View.INVISIBLE);
                flag_middle.setVisibility(View.VISIBLE);
                flag_right.setVisibility(View.INVISIBLE);

                currentFragment = inviteFragment;
                btnInviteContact.setVisibility(View.VISIBLE);
            } */else {
                flag_left.setVisibility(View.INVISIBLE);
                flag_middle.setVisibility(View.GONE);
                flag_right.setVisibility(View.VISIBLE);

                currentFragment = requestsFragment;
                btnInviteContact.setVisibility(View.GONE);

                if(!currentFragment.getIsSelectable())
                {
                    btnPrev.setVisibility(View.VISIBLE);
                    btnClose.setVisibility(View.GONE);
                    bottomNavLayout.setVisibility(View.VISIBLE);
                    bottomDeleteLayout.setVisibility(View.GONE);
                }else {
                    btnPrev.setVisibility(View.GONE);
                    btnClose.setVisibility(View.VISIBLE);
                    bottomNavLayout.setVisibility(View.GONE);
                    bottomDeleteLayout.setVisibility(View.VISIBLE);
                }
            }

            //currentFragment.loadData();
            changeTitle();
            searchItems();

            edtSearch.setCursorVisible(false);
            btnCancel.setVisibility(View.GONE);
            MyApp.getInstance().hideKeyboard(activityRootView);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }


    public class ContactChangeReceiver extends BroadcastReceiver {
        public ContactChangeReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New Contact Change");

            //setBadge();

            if(pendingRequestFragment!=null)
                pendingRequestFragment.loadData();
            //if(inviteFragment!=null)
            //   inviteFragment.loadData();
            if(requestsFragment!=null)
                requestsFragment.loadData();
        }
    }
    public class ExchangeRequestReceiver extends BroadcastReceiver {
        public ExchangeRequestReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New exchange request");

            //setBadge();
            if(pendingRequestFragment!=null)
                pendingRequestFragment.loadData();
            //if(inviteFragment!=null)
            //    inviteFragment.loadData();
            if(requestsFragment!=null)
                requestsFragment.loadData();
        }
    }
}
