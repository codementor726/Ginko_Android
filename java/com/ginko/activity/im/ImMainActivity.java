package com.ginko.activity.im;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.IMRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.ContactListFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.ImBoardVO;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class ImMainActivity extends MyBaseActivity implements View.OnClickListener,
                                                              ContactListFragment.ContactItemSelectListener
{

    /* UI Objects */
    private LinearLayout activityRootView, selectAllLayout;
    private ImageButton btnPrev , btnConfirm;
    private ImageView imgSelectAllCheckBox;
    private ImageView imgChatableSelect , imgGroupSelect;
    private EditText edtSearch;
    private ImageView btnCancelSearch;
    private ContactListFragment contactList;
    private RelativeLayout bottomLayout;
    private TextView txtSelectAll;

    private Button btnCancel;
    /* Variables */
    private boolean isKeyboardVisible = false;
    private boolean isSelectedAll = false;
    private boolean isShowAll = true;

    private String strSearchKeyword = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_im_main);

        getUIObjects();
	}

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        activityRootView = (LinearLayout)findViewById(R.id.rootLayout);
        selectAllLayout = (LinearLayout)findViewById(R.id.selectAllLayout);
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

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        btnCancelSearch = (ImageView)findViewById(R.id.imgCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnCancelSearch.setVisibility(View.GONE);
                hideKeyboard();
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
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);

                    if (edtSearch.getText().toString().length() > 0)
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    else
                        btnCancelSearch.setVisibility(View.GONE);
                    searchItems();
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
                    btnCancelSearch.setVisibility(View.GONE);
                }
            }
        });

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this); btnConfirm.setVisibility(View.GONE);
        imgSelectAllCheckBox = (ImageView)findViewById(R.id.imgSelectAllCheckBox); imgSelectAllCheckBox.setOnClickListener(this);

        bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
        imgChatableSelect = (ImageView)findViewById(R.id.chatableOnOff); imgChatableSelect.setOnClickListener(this);
        imgGroupSelect = (ImageView)findViewById(R.id.groupOnOff); imgGroupSelect.setOnClickListener(this);

        btnCancel = (Button)findViewById(R.id.btnCancel); btnCancel.setOnClickListener(this); btnCancel.setVisibility(View.GONE);
        txtSelectAll = (TextView)findViewById(R.id.txtSelectAll);

        isShowAll = true;
        updateBottomMenuButtons();
    }

    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();
        if(strEditText.compareTo("")!=0) {
            strSearchKeyword = strEditText.toLowerCase();
            //contactList.searchItems(strSearchKeyword);
            if(isShowAll)
                contactList.searchItems(strSearchKeyword);
            else
                contactList.searchSelectedItems(strSearchKeyword);
        }
        else
        {
            strSearchKeyword = "";
            //contactList.searchItems(strSearchKeyword);
            if(isShowAll)
                contactList.searchItems(strSearchKeyword);
            else
                contactList.searchSelectedItems(strSearchKeyword);
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
                bottomLayout.setVisibility(View.VISIBLE);
            } else {
                btnConfirm.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.GONE);
            }
        }
    }


	private void startNewChatBoard() {
		List<ContactListFragment.FragmentContactItem> contacts = contactList.getSelectedVisibleContacts();
		if(CollectionUtils.isEmpty(contacts)){
			return;
		}
		String userIds="";
		for (ContactListFragment.FragmentContactItem contact : contacts) {
			userIds += contact.user_id + ",";
		}

        userIds = userIds.substring(0, userIds.length()-1);

		IMRequest.createBoard(userIds, new ResponseCallBack<ImBoardVO>() {
			
			@Override
			public void onCompleted(JsonResponse<ImBoardVO> response) {
				if (response.isSuccess()){
					ImBoardVO board = response.getData();

                    //Add by lee for GAD-651
                    for(int i = 0; i < board.getMembers().size() -1; i++)
                    {
                        //for(int j = 0; j < contactList.getSelectedItemCount(); j++) {
                        //   if (board.getMembers().get(i).getUser().getUserId() == contactList.getSelectedContacts().get(j).user_id)
                        board.getMembers().get(i).getUser().setPhotoUrl(contactList.getSelectedContacts().get(i).profileImage);
                        //}
                    }
                    ////////////////////////////////////////////////////////

					Intent intent = new Intent(ImMainActivity.this,ImBoardActivity.class);
					intent.putExtra("board_id", board.getBoardId());
					Bundle bundle = new Bundle();
					bundle.putSerializable("board", board);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}

    private void updateBottomMenuButtons()
    {
        if(isShowAll)
        {
            imgChatableSelect.setImageResource(R.drawable.chat_off);
            imgGroupSelect.setImageResource(R.drawable.group_on);
        }
        else
        {
            imgChatableSelect.setImageResource(R.drawable.chat_on);
            imgGroupSelect.setImageResource(R.drawable.group_off);
        }
    }

    private void updateSelectAll()
    {
        int count = contactList.getSelectedItemCount();
        if(count > 0 && count == contactList.getVisibleItemCount())
        {
            imgSelectAllCheckBox.setImageResource(R.drawable.checkbox_on);
            isSelectedAll = true;
        }
        else
        {
            imgSelectAllCheckBox.setImageResource(R.drawable.checkbox_off);
            isSelectedAll = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btnConfirm:
                startNewChatBoard();
                finish();
                break;

            //select all
            case R.id.imgSelectAllCheckBox:
                hideKeyboard();

                isSelectedAll = !isSelectedAll;
                contactList.selectAll(isSelectedAll);
                updateSelectAll();
                if(contactList.getSelectedItemCount()>0) {
                    btnConfirm.setVisibility(View.VISIBLE);
                    bottomLayout.setVisibility(View.VISIBLE);
                }
                else {
                    btnConfirm.setVisibility(View.GONE);
                    bottomLayout.setVisibility(View.GONE);
                }
                break;

            //show only selected chats
            case R.id.chatableOnOff:
                if(isShowAll) {
                    imgSelectAllCheckBox.setVisibility(View.INVISIBLE);
                    txtSelectAll.setVisibility(View.INVISIBLE);
                    //selectAllLayout.setVisibility(View.INVISIBLE);
                    isShowAll = false;
                    updateBottomMenuButtons();
                    contactList.showOnlySelectedItems();
                }

                searchItems();
                hideKeyboard();
                break;

            //select all of selected and unselected contact
            case R.id.groupOnOff:
                if(!isShowAll) {
                    isShowAll = true;
                    imgSelectAllCheckBox.setVisibility(View.VISIBLE);
                    txtSelectAll.setVisibility(View.VISIBLE);
                    //selectAllLayout.setVisibility(View.VISIBLE);
                    updateBottomMenuButtons();
                    contactList.showAllItems();
                }
                searchItems();
                hideKeyboard();
                break;

            //Click Cancel button
            case R.id.btnCancel:
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnCancelSearch.setVisibility(View.GONE);
                hideKeyboard();
                break;
        }
    }

    private void hideKeyboard()
    {
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);
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
            bottomLayout.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
        }
        else {
            bottomLayout.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
        }
    }
}
