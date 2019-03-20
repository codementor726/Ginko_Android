package com.ginko.activity.sprout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.ContactItemComparator;
import com.ginko.activity.exchange.ShareingLeafDialog;
import com.ginko.activity.im.ImBoardActivity;
import com.ginko.api.request.SpoutRequest;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.SharingBean;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.imagecrop.Util;
import com.ginko.view.ext.SelectableListAdapter;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GinkoMeFilterActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI Variables */
    private RelativeLayout activityRootView;
    private ImageButton btnClose , btnConfirm;
    private LinearLayout filterSelectAllLayout , filterSelectHomeLayout , filterSelectWorkLayout , filterSelectDontShareLayout;
    private View dividerLayout1 ,  dividerLayout2 , dividerLayout3 , dividerLayout4;
    private ImageView imgFilterIcon1 , imgFilterIcon2 , imgFilterIcon3;
    private TextView txtFilter1 , txtFilter2 , txtFilter3 , txtFilter4;
    private EditText edtSearch;
    private ImageView btnClearSearch;
    private Button btnCancelSearch;
    private ListView contactList;

    /* Variables */
    private int filterModeIndex = 0;
    private MyContactListAdapter listAdapter;
    private ArrayList<ContactItem> myContactList;

    private boolean isKeyboardVisible = false;
    private String strSearchKeyword = "";

    private int m_orientHeight = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ginkome_filter);

        myContactList = new ArrayList<ContactItem>();

        getUIObjects();

        loadContacts();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
    }

    private void loadContacts()
    {

        ContactTableModel contactsTableModel = MyApp.getInstance().getContactsModel();
        if(contactsTableModel != null) {
            MyApp.getInstance().getAllContactItemsFromDatabase();
            if(MyApp.g_contactItems != null || MyApp.g_contactItems.size() > 0)
            {
                for(ContactItem item : MyApp.g_contactItems)
                {
                    if(item.getContactType() == 1)//purple contact
                    {
                        myContactList.add(new ContactItem(item));
                    }
                }
                sortContacts(myContactList);
            }
        }
        listAdapter.notifyDataSetChanged();
        SpoutRequest.getFilterContact(new ResponseCallBack<JSONObject>() {
            @Override
            public void onCompleted(JsonResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    JSONObject result = response.getData();
                    try {
                        filterModeIndex = result.optInt("type", 0);
                        JSONArray userIds = result.getJSONArray("user_ids");
                        for (int i = 0; i < userIds.length(); i++) {
                            int userId = userIds.getInt(i);
                            for (int j = 0; j < myContactList.size(); j++) {
                                if (myContactList.get(j).getContactId() == userId) {
                                    listAdapter.select(j);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                listAdapter.notifyDataSetChanged();
                updateSelectedFilterMode();
            }
        }, true);
    }

    @Override
    protected void getUIObjects() {
        super.getUIObjects();

        btnClose = (ImageButton)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);

        filterSelectAllLayout = (LinearLayout)findViewById(R.id.filterSelectAllLayout); filterSelectAllLayout.setOnClickListener(this);
        filterSelectHomeLayout = (LinearLayout)findViewById(R.id.filterSelectHomeLayout); filterSelectHomeLayout.setOnClickListener(this);
        filterSelectWorkLayout = (LinearLayout)findViewById(R.id.filterSelectWorkLayout); filterSelectWorkLayout.setOnClickListener(this);
        filterSelectDontShareLayout = (LinearLayout)findViewById(R.id.filterSelectDontShareLayout); filterSelectDontShareLayout.setOnClickListener(this);

        dividerLayout1 = (View)findViewById(R.id.dividerLayout1);
        dividerLayout2 = (View)findViewById(R.id.dividerLayout2);
        dividerLayout3 = (View)findViewById(R.id.dividerLayout3);
        dividerLayout4 = (View)findViewById(R.id.dividerLayout4);

        imgFilterIcon1 = (ImageView)findViewById(R.id.imgFilterIcon1);
        imgFilterIcon2 = (ImageView)findViewById(R.id.imgFilterIcon2);
        imgFilterIcon3 = (ImageView)findViewById(R.id.imgFilterIcon3);

        txtFilter1 = (TextView)findViewById(R.id.txtFilter1);
        txtFilter2 = (TextView)findViewById(R.id.txtFilter2);
        txtFilter3 = (TextView)findViewById(R.id.txtFilter3);
        txtFilter4 = (TextView)findViewById(R.id.txtFilter4);

        edtSearch = (EditText)findViewById(R.id.edtSearch);
        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch);
        btnCancelSearch = (Button)findViewById(R.id.btnCancelSearch);

        activityRootView = (RelativeLayout)findViewById(R.id.rootLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        edtSearch.setCursorVisible(true);
                        updateEdtSearchButtons(true);
                        if (edtSearch.getText().toString().length() > 0) {
                            btnClearSearch.setVisibility(View.VISIBLE);
                            btnCancelSearch.setVisibility(View.VISIBLE);
                        } else {
                            btnClearSearch.setVisibility(View.GONE);
                            btnCancelSearch.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        updateEdtSearchButtons(false);
                        edtSearch.setCursorVisible(false);
                        /*if (btnClearSearch.getVisibility() == View.VISIBLE)
                            btnClearSearch.setVisibility(View.GONE);*/
                        if (btnCancelSearch.getVisibility() == View.VISIBLE)
                            btnCancelSearch.setVisibility(View.GONE);
                    }
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnClearSearch.setVisibility(View.GONE);
            }
        });

        btnCancelSearch = (Button)findViewById(R.id.btnCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strSearchKeyword = "";
                edtSearch.setText("");
                searchItems();
                btnClearSearch.setVisibility(View.GONE);

                hideKeyboard();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {

             @Override
             public void beforeTextChanged(CharSequence s, int start, int count,
                                           int after) {
                 // TODO Auto-generated method stub
             }

             @Override
             public void onTextChanged(CharSequence s, int start, int before,
                                       int count) {
                 // TODO Auto-generated method stub
                 if (s.length() > 0)
                     btnClearSearch.setVisibility(View.VISIBLE);
                 else
                     btnClearSearch.setVisibility(View.GONE);

                 strSearchKeyword = s.toString().trim();
             }

             @Override
             public void afterTextChanged(Editable s) {
                 // TODO Auto-generated method stub
                 searchItems();
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
                    hideKeyboard();
                    searchItems();
                    return true;
                }
                return false;
            }
        });

        /*
        edtSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(btnCancelSearch.getVisibility() != View.VISIBLE) {
                    edtSearch.setCursorVisible(true);
                    btnCancelSearch.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        */

        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (btnCancelSearch.getVisibility() == View.GONE) {
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    }
                    edtSearch.setCursorVisible(true);
                    if (edtSearch.getText().toString().length() > 0) {
                        btnClearSearch.setVisibility(View.VISIBLE);
                    } else {
                        btnClearSearch.setVisibility(View.GONE);
                    }
                } else {
                    edtSearch.setCursorVisible(false);
                    if (btnCancelSearch.getVisibility() == View.VISIBLE) {
                        btnCancelSearch.setVisibility(View.GONE);
                    }
                    btnClearSearch.setVisibility(View.GONE);
                }
            }
        });

        listAdapter = new MyContactListAdapter(GinkoMeFilterActivity.this , myContactList);

        contactList = (ListView)findViewById(R.id.contactList);
        contactList.setAdapter(listAdapter);
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listAdapter.isSelected(position))
                    listAdapter.unSelect(position);
                else
                    listAdapter.select(position);

                if(listAdapter.getSelectedItemCount() > 0 && filterModeIndex != 3)
                {
                    filterModeIndex = 3;
                }

                listAdapter.notifyDataSetChanged();

                updateSelectedFilterMode();
            }
        });
    }

    private void searchItems()
    {

        listAdapter.searchItems(strSearchKeyword);
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

    public void hideKeyboard()
    {
        //if(isKeyboardVisible)
        MyApp.getInstance().hideKeyboard(activityRootView);
        updateEdtSearchButtons(false);
    }

    private void updateEdtSearchButtons(boolean bShowButtons)
    {
        if(bShowButtons)
        {
            edtSearch.setCursorVisible(true);
            btnClearSearch.setVisibility(View.VISIBLE);
            btnCancelSearch.setVisibility(View.VISIBLE);
        }
        else {
            edtSearch.setCursorVisible(false);
            if(edtSearch.getText().toString().equals(""))
                btnClearSearch.setVisibility(View.INVISIBLE);
            else
                btnClearSearch.setVisibility(View.VISIBLE);
            btnCancelSearch.setVisibility(View.GONE);
        }
    }

    private void updateSelectedFilterMode()
    {
        switch (filterModeIndex)
        {
            case 0://select all
                imgFilterIcon1.setImageResource(R.drawable.ginkome_filter_all_white); imgFilterIcon1.invalidate();
                imgFilterIcon2.setImageResource(R.drawable.ginkome_filter_home_white); imgFilterIcon2.invalidate();
                imgFilterIcon3.setImageResource(R.drawable.ginkome_filter_work_white); imgFilterIcon3.invalidate();

                filterSelectAllLayout.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                filterSelectHomeLayout.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                filterSelectWorkLayout.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                filterSelectDontShareLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                txtFilter1.setTextColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                txtFilter2.setTextColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                txtFilter3.setTextColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                txtFilter4.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));

                dividerLayout1.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                dividerLayout2.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                dividerLayout3.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                dividerLayout4.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));

                break;
            case 1://select home
                imgFilterIcon1.setImageResource(R.drawable.ginkome_filter_all_grey); imgFilterIcon1.invalidate();
                imgFilterIcon2.setImageResource(R.drawable.ginkome_filter_home_white); imgFilterIcon2.invalidate();
                imgFilterIcon3.setImageResource(R.drawable.ginkome_filter_work_grey); imgFilterIcon3.invalidate();

                filterSelectAllLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                filterSelectHomeLayout.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                filterSelectWorkLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                filterSelectDontShareLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                txtFilter1.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                txtFilter2.setTextColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                txtFilter3.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                txtFilter4.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout1.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout2.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                dividerLayout3.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout4.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                break;
            case 2://select work
                imgFilterIcon1.setImageResource(R.drawable.ginkome_filter_all_grey); imgFilterIcon1.invalidate();
                imgFilterIcon2.setImageResource(R.drawable.ginkome_filter_home_grey); imgFilterIcon2.invalidate();
                imgFilterIcon3.setImageResource(R.drawable.ginkome_filter_work_white); imgFilterIcon3.invalidate();

                filterSelectAllLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                filterSelectHomeLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                filterSelectWorkLayout.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                filterSelectDontShareLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                txtFilter1.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                txtFilter2.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                txtFilter3.setTextColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                txtFilter4.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout1.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout2.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout3.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                dividerLayout4.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                break;
            case 3://select contact
                if(listAdapter.getSelectedItemCount() == 0)
                {
                    imgFilterIcon1.setImageResource(R.drawable.ginkome_filter_all_grey); imgFilterIcon1.invalidate();
                    imgFilterIcon2.setImageResource(R.drawable.ginkome_filter_home_grey); imgFilterIcon2.invalidate();
                    imgFilterIcon3.setImageResource(R.drawable.ginkome_filter_work_grey); imgFilterIcon3.invalidate();

                    filterSelectAllLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    filterSelectHomeLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    filterSelectWorkLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    filterSelectDontShareLayout.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                    txtFilter1.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    txtFilter2.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    txtFilter3.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    txtFilter4.setTextColor(getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                    dividerLayout1.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout2.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout3.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout4.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_selected_item_color));
                }
                else {
                    imgFilterIcon1.setImageResource(R.drawable.ginkome_filter_all_grey); imgFilterIcon1.invalidate();
                    imgFilterIcon2.setImageResource(R.drawable.ginkome_filter_home_grey); imgFilterIcon2.invalidate();
                    imgFilterIcon3.setImageResource(R.drawable.ginkome_filter_work_grey); imgFilterIcon3.invalidate();

                    filterSelectAllLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    filterSelectHomeLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    filterSelectWorkLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    filterSelectDontShareLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    txtFilter1.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    txtFilter2.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    txtFilter3.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    txtFilter4.setTextColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout1.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout2.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout3.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                    dividerLayout4.setBackgroundColor(getResources().getColor(R.color.ginkome_filter_item_text_color));
                }
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isKeyboardVisible) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShownKeyboard();
        //hideKeyboard();
        if(!isKeyboardVisible)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        else {
            //MyApp.getInstance().hideKeyboard(activityRootView);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnClose:
                hideKeyboard();
                finish();
                break;

            case R.id.btnConfirm:
                List<ContactItem> selectedItems = listAdapter.getSelectedItems();
                String strSelectedContactIds = null;

                if(filterModeIndex == 3 && selectedItems.size() > 0)//if select contact type
                {
                    strSelectedContactIds = "";
                    for(ContactItem item : selectedItems)
                    {
                        strSelectedContactIds += String.valueOf(item.getContactId())+ ",";
                    }
                    strSelectedContactIds = strSelectedContactIds.substring(0 , strSelectedContactIds.length()-1);
                }

                boolean isRemoveExist = strSelectedContactIds == null?false:true;

                if(filterModeIndex == 3 && selectedItems.size() < 1) {
                    strSelectedContactIds = "";Uitils.getUserFullname(GinkoMeFilterActivity.this);
                    isRemoveExist = true;
                }

                SpoutRequest.detectContactFilter(filterModeIndex, strSelectedContactIds, isRemoveExist, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if(response.isSuccess())
                        {
                            finish();
                        }
                        else {
                            Uitils.alert(GinkoMeFilterActivity.this , "Failed to set filter");
                        }
                    }
                } , true);
                break;

            //select all
            case R.id.filterSelectAllLayout:
                listAdapter.unSelectAll();
                listAdapter.notifyDataSetChanged();
                filterModeIndex = 0;
                updateSelectedFilterMode();
                break;

            //select home
            case R.id.filterSelectHomeLayout:
                listAdapter.unSelectAll();
                listAdapter.notifyDataSetChanged();
                filterModeIndex = 1;
                updateSelectedFilterMode();
                break;

            //select work
            case R.id.filterSelectWorkLayout:
                listAdapter.unSelectAll();
                listAdapter.notifyDataSetChanged();
                filterModeIndex = 2;
                updateSelectedFilterMode();
                break;

            //select contact
            case R.id.filterSelectDontShareLayout:
                listAdapter.unSelectAll();
                listAdapter.notifyDataSetChanged();
                filterModeIndex = 3;
                updateSelectedFilterMode();
                break;
        }
    }

    class MyContactListAdapter extends SelectableListAdapter<ContactItem>
    {
        private Context mContext;
        private String searchKeyword;

        public MyContactListAdapter(Context context , ArrayList<ContactItem> listItems)
        {
            super(context , listItems);
            this.mContext = context;
        }

        public void searchItems(String searchKeyword)
        {
            this.searchKeyword = searchKeyword;
            List<ContactItem> items = getListItems();
            if(searchKeyword.equals(""))
            {
                showAllItems();
            }
            for(int i =0 ; i<items.size(); i++)
            {
                ContactItem item = items.get(i);
                String fullName = item.getFullName().toLowerCase().trim();
                if(fullName.contains(searchKeyword.toLowerCase()) || item.getFirstName().toLowerCase().contains(searchKeyword.toLowerCase()) || item.getLastName().toLowerCase().contains(searchKeyword.toLowerCase()))
                    showItem(i, true);
                else
                    showItem(i , false);
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ContactItem item = getItem(position);

            ItemView view = null;
            if (convertView == null) {
                view = new ItemView(mContext , item);
            }
            else
            {
                view = (ItemView)convertView;
            }

            view.setAsSelected(isSelected(position));


            view.setItem(item);
            view.refreshView();
            if(isItemVisible(position))
                view.findViewById(R.id.itemRootLayout).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.itemRootLayout).setVisibility(View.GONE);
            return view;
        }
    }
    private class ItemView extends LinearLayout {
        private LayoutInflater inflater = null;
        private TextView txtContactName;
        //private ImageView imgCheckIcon;
        private View dividerLayout;
        private RelativeLayout itemLayout;

        private ContactItem item;
        private Context mContext;

        public ItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.mContext = context;

        }
        public ItemView(Context context,  ContactItem _item)
        {
            super(context);
            this.mContext = context;
            item  = _item;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.ginkome_filter_contact_list_item, this, true);

            itemLayout = (RelativeLayout)findViewById(R.id.itemLayout);
            //imgCheckIcon = (ImageView)findViewById(R.id.imgCheckIcon);
            dividerLayout = (View)findViewById(R.id.dividerLayout);

            txtContactName = (TextView)findViewById(R.id.txtContactName);

        }
        public void setItem(ContactItem _item)
        {
            this.item = _item;
        }
        public void setAsSelected(boolean selected)
        {
            if(selected) {
                itemLayout.setBackgroundColor(mContext.getResources().getColor(R.color.ginkome_filter_selected_item_color));
                txtContactName.setTextColor(mContext.getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                dividerLayout.setBackgroundColor(mContext.getResources().getColor(R.color.ginkome_filter_item_selected_text_color));
                //imgCheckIcon.setVisibility(View.VISIBLE);
            }
            else {
                itemLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                txtContactName.setTextColor(mContext.getResources().getColor(R.color.ginkome_filter_item_text_color));
                dividerLayout.setBackgroundColor(mContext.getResources().getColor(R.color.ginkome_filter_item_text_color));
                //imgCheckIcon.setVisibility(View.INVISIBLE);
            }

        }

        public void refreshView()
        {
            String name = item.getFirstName();

            if (StringUtils.isNotBlank(item.getMiddleName())) {
                name += " " + item.getMiddleName();
            }
            if (StringUtils.isNotBlank(item.getLastName())) {
                name += " " + item.getLastName();
            }

            txtContactName.setText(name);
        }
    }

    private void sortContacts(List<ContactItem> contactList)
    {
        boolean isSortByFName = Uitils.getIsSortByFName(GinkoMeFilterActivity.this);
        ContactItemComparator contactItemComparator = new ContactItemComparator(GinkoMeFilterActivity.this, isSortByFName);

        try {
            Collections.sort(contactList, contactItemComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
