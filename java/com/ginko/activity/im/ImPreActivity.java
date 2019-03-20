package com.ginko.activity.im;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.entity.EntityViewPostMessageAdapter;
import com.ginko.api.request.IMRequest;
import com.ginko.common.ImageButtonTab;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.MyViewPager;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.EntityMsgWallFragment;
import com.ginko.fragments.ImBoardListFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.view.ext.SelectableListAdapter;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.IMBoardMessage;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.ImMessageVO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ginko.ginko.R.drawable.leafbgforblank;

public class ImPreActivity extends MyBaseFragmentActivity implements
		OnClickListener ,
        ImPreGetMessageCallbackListener,
        ActionSheet.ActionSheetListener,
        ImBoardListFragment.OnBoardListItemSelectListener
{

    private RelativeLayout activityRootView;
    private ImageView btnClose , btnDelete , btnDeleteAllBoards , btnChatSelection , btnEdit;
    private ImageButton btnBack;
    private LinearLayout deleteLayout;
    private ImageButtonTab imgButtonTab = new ImageButtonTab();
    private ImageButton btnChatHistory;
    private ImageButton btnMsgWall;
    private TextView txtTabNotes;

    private Button btnCancel;
    private EditText edtSearch;
    private ImageView btnCancelSearch;

	private MyViewPager mPager;
	private List<Fragment> fragments = new ArrayList<Fragment>();
	private int currIndex = 0;
    private MyPagerAdapter pageAdater = null;

    private boolean isKeyboardVisible = false;
    private boolean isEditable = false;
    private String strSearchKeyword = "";

    private ImBoardListFragment imBoardListFragment;
    private EntityMsgWallFragment entityMsgWallFragment;

    private boolean isImReceiverRegistered = false;
    private boolean isEntityReceiverRegistered = false;
    private boolean isContactReceiverRegistered = false;
    //---------------------------------//
    /* new message listener */
    private ImMsgReceiver imReceiver;
    private EntityMsgReceiver entityMsgReceiver;
    private ContactChangedReceiver contactReceiver;


    //sharing messages
    private ActionSheet shareViaEmailActionSheet = null;
    public static EntityMessageVO sharingMessage = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_im_pre);

        getUIObjects();
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
                        edtSearch.setCursorVisible(true);
                        btnCancel.setVisibility(View.VISIBLE);
                        deleteLayout.setVisibility(View.GONE);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        edtSearch.setCursorVisible(false);
                        btnCancel.setVisibility(View.GONE);
                        if(imBoardListFragment != null && imBoardListFragment.getSelectedItemsCount() > 0) {
                            deleteLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
        btnDeleteAllBoards = (ImageView)findViewById(R.id.btnDeleteAllBoards); btnDeleteAllBoards.setOnClickListener(this);
        btnBack = (ImageButton)findViewById(R.id.btnPrev); btnBack.setOnClickListener(this);
        btnChatSelection = (ImageView)findViewById(R.id.btnChatSelection); btnChatSelection.setOnClickListener(this);
        btnEdit = (ImageView)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);

        txtTabNotes = (TextView)findViewById(R.id.txtTabNotes);

        deleteLayout = (LinearLayout)findViewById(R.id.deleteLayout);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                btnCancelSearch.setVisibility(View.GONE);
                hideKeyboard();
            }
        });

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
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
                                                 if (s.length() > 0)
                                                     btnCancelSearch.setVisibility(View.VISIBLE);
                                                 else
                                                     btnCancelSearch.setVisibility(View.GONE);
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

        btnChatHistory = (ImageButton) this.findViewById(R.id.btn_chat_history);
        btnMsgWall = (ImageButton) this.findViewById(R.id.btn_msg_wall);
        imgButtonTab.addButton(btnChatHistory, R.drawable.chat1_1,
                R.drawable.chat1_2);
        imgButtonTab.addButton(btnMsgWall, R.drawable.chat2_1,
                R.drawable.chat2_2);

        btnChatHistory.setOnClickListener(this);
        btnMsgWall.setOnClickListener(this);

        InitViewPager();

        imReceiver = new ImMsgReceiver();
        entityMsgReceiver = new EntityMsgReceiver();
        contactReceiver = new ContactChangedReceiver();

        if (this.imReceiver != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.imReceiver, msgReceiverIntent);
            isImReceiverRegistered = true;
        }
        if(this.entityMsgReceiver != null)
        {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }
        if (this.contactReceiver != null) {
            IntentFilter contactChangeIntent = new IntentFilter();
            contactChangeIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactReceiver, contactChangeIntent);
            isContactReceiverRegistered = true;
        }
    }

    private void searchItems()
    {
        //GAD-1644 Space Trim Search
        String strEditText = edtSearch.getText().toString();
        if(strEditText.compareTo("")!=0) { // && strEditText.compareTo(strSearchKeyword) != 0) {
            strSearchKeyword = strEditText.toLowerCase();
            if(currIndex == 0)
            {
                imBoardListFragment.searchItems(strSearchKeyword);
            }
            else
            {
                entityMsgWallFragment.searchItems(strSearchKeyword);
            }
        }
        else
        {
            strSearchKeyword = "";
            if(currIndex == 0)
            {
                imBoardListFragment.searchItems("");
            }
            else
            {
                entityMsgWallFragment.searchItems("");
            }
        }

        updateUIFromEditable();
    }

    private void updateUIFromEditable()
    {
        if(isEditable)
        {
            btnDeleteAllBoards.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.VISIBLE);
            if(imBoardListFragment != null && imBoardListFragment.getSelectedItemsCount() > 0)
            {
                deleteLayout.setVisibility(View.VISIBLE);
            }
            else
                deleteLayout.setVisibility(View.GONE);

            btnBack.setVisibility(View.GONE);
            btnChatSelection.setVisibility(View.GONE);

            if(currIndex == 0)
            {
                imBoardListFragment.setIsListSelectable(true);
            }
//            if(mPager!=null)
//                mPager.setScanScroll(false);
            btnEdit.setImageResource(R.drawable.done_contact);

        }
        else
        {
            btnDeleteAllBoards.setVisibility(View.GONE);
            btnClose.setVisibility(View.GONE);
            deleteLayout.setVisibility(View.GONE);

            btnBack.setVisibility(View.VISIBLE);

            if(currIndex == 0)
            {
                imBoardListFragment.setIsListSelectable(false);
                btnChatSelection.setVisibility(View.VISIBLE);
            }
            else
                btnChatSelection.setVisibility(View.GONE);
            
            if (!imBoardListFragment.noChatHistory())
                btnEdit.setImageResource(R.drawable.editcontact);
            else
                btnEdit.setImageResource(R.drawable.editcontact_disable);

//            if(mPager!=null)
//                mPager.setScanScroll(true);
        }
    }


	private void InitViewPager() {
		mPager = (MyViewPager) findViewById(R.id.vPager);
		pageAdater = new MyPagerAdapter(this.getSupportFragmentManager(),
				fragments);
        imBoardListFragment = new ImBoardListFragment();
        entityMsgWallFragment = new EntityMsgWallFragment();

        imBoardListFragment.setOnBoardListItemSelectListener(this);

        imBoardListFragment.setOnImPreGetMessageCallbackListener(ImPreActivity.this);
        entityMsgWallFragment.setOnImPreGetMessageCallbackListener(ImPreActivity.this);

        currIndex = 0;

		fragments.add(imBoardListFragment);
		fragments.add(entityMsgWallFragment);
		mPager.setAdapter(pageAdater);
		mPager.setCurrentItem(currIndex);
        mPager.setScanScroll(false);
        MyOnPageChangeListener pageChangeListener = new MyOnPageChangeListener();
		mPager.setOnPageChangeListener(pageChangeListener);
        pageChangeListener.onPageSelected(currIndex);
	}

    public void shareMessageViaEmail(EntityMessageVO msg)
    {
        sharingMessage = msg;
        setTheme(R.style.ActionSheetStyleIOS7);

        if(shareViaEmailActionSheet == null)
            shareViaEmailActionSheet = ActionSheet.createBuilder(ImPreActivity.this, getSupportFragmentManager())
                    .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                    .setOtherButtonTitles(
                            getResources().getString(R.string.str_share_via_email))
                    .setCancelableOnTouchOutside(true)
                    .setListener(this)
                    .show();
        else
            shareViaEmailActionSheet.show(getSupportFragmentManager(), "actionSheet");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(imReceiver == null)
            imReceiver = new ImMsgReceiver();

        if (this.imReceiver != null && isImReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
            registerReceiver(this.imReceiver, msgReceiverIntent);
            isImReceiverRegistered = true;
        }

        if(entityMsgReceiver == null)
            entityMsgReceiver = new EntityMsgReceiver();
        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == false) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.ENTITY_NEW_MSG");
            registerReceiver(this.entityMsgReceiver, msgReceiverIntent);
            isEntityReceiverRegistered = true;
        }

        if(contactReceiver == null)
            contactReceiver = new ContactChangedReceiver();
        if (this.contactReceiver != null && isContactReceiverRegistered == false) {
            IntentFilter contactReceiverIntent = new IntentFilter();
            contactReceiverIntent.addAction("android.intent.action.CONTACT_CHANGED");
            registerReceiver(this.contactReceiver, contactReceiverIntent);
            isContactReceiverRegistered = true;
        }
        String strEditText = edtSearch.getText().toString().trim();
        if(strEditText.compareTo("")!=0)
            strSearchKeyword = strEditText.toLowerCase();
        imBoardListFragment.searchKeyword = strSearchKeyword;
        entityMsgWallFragment.searchKeyword = strSearchKeyword;

        if(imBoardListFragment!=null)
            imBoardListFragment.loadBoardList();
        if(entityMsgWallFragment!=null)
            entityMsgWallFragment.loadEntityMsg();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);

        if (this.imReceiver != null && isImReceiverRegistered == true) {
            unregisterReceiver(this.imReceiver);
            isImReceiverRegistered = false;
        }

        if (this.entityMsgReceiver != null && isEntityReceiverRegistered == true) {
            unregisterReceiver(this.entityMsgReceiver);
            isEntityReceiverRegistered = false;
        }

        if (this.contactReceiver != null && isContactReceiverRegistered == true) {
            unregisterReceiver(this.contactReceiver);
            isContactReceiverRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        this.fragments.clear();
        super.onDestroy();
    }

    @Override
	public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btn_chat_history:
                mPager.setCurrentItem(0);
                entityMsgWallFragment.setStopAllRecords();
                break;

            case R.id.btn_msg_wall:
                mPager.setCurrentItem(1);
                break;

            case R.id.btnPrev:
                ImPreActivity.this.finish();
                break;
            case R.id.btnChatSelection:
                Uitils.toActivity(ImMainActivity.class, false);
                break;

            //delete selected messages
            case R.id.btnDeleteAllBoards:
                AlertDialog.Builder builder = new AlertDialog.Builder(ImPreActivity.this);
                builder.setMessage(getResources().getString(R.string.str_confirm_delete_all_im_boards));
                builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        if(currIndex == 0) {
                            imBoardListFragment.deleteAllImBoards();
                            SelectableListAdapter<ImBoardVO> adapter = (SelectableListAdapter<ImBoardVO>) imBoardListFragment.getListViewAdapter();

                            int cnt = adapter.getCount() - adapter.getSelectedItemCount();
                            showNotesIfNoChats(cnt, false);
                            if(isEditable)
                            {
                                isEditable = false;
                                updateUIFromEditable();
                            }
                        }
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int paramInt) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
            case R.id.btnDelete:
                if(currIndex == 0)
                {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ImPreActivity.this);
                    builder1.setMessage(getResources().getString(R.string.str_confirm_delete_im_boards));
                    builder1.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            imBoardListFragment.deleteBoards();

                            SelectableListAdapter<ImBoardVO> adapter = (SelectableListAdapter<ImBoardVO>) imBoardListFragment.getListViewAdapter();

                            int cnt = adapter.getCount() - adapter.getSelectedItemCount();
                            showNotesIfNoChats(cnt, false);
                            if(isEditable)
                            {
                                isEditable = false;
                                updateUIFromEditable();
                            }
                        }
                    });
                    builder1.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int paramInt) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                        }
                    });
                    builder1.show();

                }
                break;

            case R.id.btnEdit:
                if(!imBoardListFragment.noChatHistory())
                {
                    if(isEditable)
                        isEditable = false;
                    else
                        isEditable = true;
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
        }
	}

    private void hideKeyboard()
    {
        if(isKeyboardVisible){
            MyApp.getInstance().hideKeyboard(activityRootView);
            deleteLayout.setVisibility(View.GONE);
        }else if(imBoardListFragment != null && imBoardListFragment.getSelectedItemsCount() > 0) {
            deleteLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showNotesIfNoChats(int nChatRooms, boolean isEntity)
    {
        if(isEntity && currIndex != 1) return;
        if(!isEntity && currIndex != 0) return;

        if(nChatRooms>0)
        {
            txtTabNotes.setVisibility(View.INVISIBLE);
            mPager.setBackgroundColor(Color.WHITE);
        }
        else {
            txtTabNotes.setVisibility(View.VISIBLE);
            mPager.setBackgroundDrawable(null);
            if (isEntity) {
                txtTabNotes.setText(getResources().getString(R.string.str_im_pre_walltab_notes));
            } else {
                txtTabNotes.setText(getResources().getString(R.string.str_im_pre_messagetab_notes));
            }
        }
    }

    @Override
    public void onGetRecentChats(int nChatRooms, boolean isEntity) {
        if(txtTabNotes!=null)
        {
            showNotesIfNoChats(nChatRooms , isEntity);
        }
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0) //share photo or video file via email
        {
            if(sharingMessage != null)
            {
                if(sharingMessage.getFile() != null && sharingMessage.getFile().equals(""))
                {
                    Toast.makeText(ImPreActivity.this, "File Downloading... Please wait for a while", Toast.LENGTH_LONG).show();
                    return;
                }
                if(sharingMessage.getFile() != null)
                {
                    File file = new File(sharingMessage.getFile());
                    if(file.exists())
                    {
                        try {

                            final Intent emailIntent = new Intent(
                                    android.content.Intent.ACTION_SEND);

                            emailIntent.setType("plain/text");
                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                                    new String[] { "" });//email
                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                                    "");//subject

                            Uri attachFileUri = Uri.fromFile(file);

                            if (attachFileUri != null) {
                                emailIntent.putExtra(Intent.EXTRA_STREAM, attachFileUri);
                            }

                            emailIntent
                                    .putExtra(android.content.Intent.EXTRA_TEXT, "Sent from Ginko Android...");
                            this.startActivity(Intent.createChooser(emailIntent,
                                    "Send email via..."));
                        } catch (Throwable t) {
                            Toast.makeText(this,
                                    "Request failed try again: " + t.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ImPreActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                        return;
                    }

                }
                else
                {
                    Toast.makeText(ImPreActivity.this ,  "File Downloading... Please wait for a while" , Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    @Override
    public void onBoardListItemSelected(int selectedItemPosition, int selectedItemsCount) {
        if(selectedItemsCount > 0)
        {
            deleteLayout.setVisibility(View.VISIBLE);
        }
        else {
            deleteLayout.setVisibility(View.GONE);
        }
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

		private List<Fragment> infos;

		public MyPagerAdapter(FragmentManager fm, List<Fragment> infos) {
			super(fm);
			this.infos = infos;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Logger.debug("position Destory" + position);
			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {
			return infos.size();
		}

		@Override
		public Object instantiateItem(ViewGroup arg0, int position) {
			Fragment ff = (Fragment) super.instantiateItem(arg0, position);
			return ff;
		}

		@Override
		public Fragment getItem(int position) {

			return infos.get(position);
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
	}


	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
            currIndex = position;
			if (currIndex == 0) {
				imgButtonTab.selectButton(btnChatHistory);
                btnChatSelection.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                updateUIFromEditable();

                SelectableListAdapter<ImBoardVO> adapter = (SelectableListAdapter<ImBoardVO>) imBoardListFragment.getListViewAdapter();
                showNotesIfNoChats(adapter==null?0:adapter.getCount(), false);
			} else {
				imgButtonTab.selectButton(btnMsgWall);
                //updateUIFromEditable();
                /*
                if(isEditable)
                {
                    btnDeleteAllBoards.setVisibility(View.GONE);
                    btnClose.setVisibility(View.GONE);
                    deleteLayout.setVisibility(View.GONE);

                    btnBack.setVisibility(View.VISIBLE);
                    btnChatSelection.setVisibility(View.VISIBLE);
                }
                */
                //GAD-1643 Change
                if(isEditable)
                {
                    isEditable = false;
                    updateUIFromEditable();
                }


                btnChatSelection.setVisibility(View.GONE);
                btnEdit.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.GONE);
                entityMsgWallFragment.setOnImPreGetMessageCallbackListener(ImPreActivity.this);

                EntityViewPostMessageAdapter adapter = (EntityViewPostMessageAdapter) entityMsgWallFragment.getListViewAdapter();
                showNotesIfNoChats(adapter==null?0:adapter.getCount() , true);
			}

            searchItems();
            /* For GAD-928
            if(isKeyboardVisible)
                hideKeyboard();*/
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

    public class ContactChangedReceiver extends BroadcastReceiver {
        public ContactChangedReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String strEditText = edtSearch.getText().toString().trim();
            if(strEditText.compareTo("")!=0)
                strSearchKeyword = strEditText.toLowerCase();
            imBoardListFragment.searchKeyword = strSearchKeyword;
            if(imBoardListFragment!=null)
                imBoardListFragment.loadBoardList();
        }
    }

    public class ImMsgReceiver extends BroadcastReceiver {
        public ImMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            String strEditText = edtSearch.getText().toString().trim();
            if(strEditText.compareTo("")!=0)
                strSearchKeyword = strEditText.toLowerCase();
            imBoardListFragment.searchKeyword = strSearchKeyword;
            if(imBoardListFragment!=null)
                imBoardListFragment.loadBoardList();

        }
    }
    public class EntityMsgReceiver extends BroadcastReceiver {
        public EntityMsgReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Logger.debug("Received New message");

            String strEditText = edtSearch.getText().toString().trim();
            if(strEditText.compareTo("")!=0)  // && strEditText.compareTo(strSearchKeyword) != 0) {
                strSearchKeyword = strEditText.toLowerCase();
            entityMsgWallFragment.searchKeyword = strSearchKeyword;
            if(entityMsgWallFragment!=null)
                entityMsgWallFragment.loadEntityMsg();

        }
    }
}
