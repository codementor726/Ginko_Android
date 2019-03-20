package com.ginko.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.Manifest;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.contact.ContactItem;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.activity.profiles.ShareYourLeafActivity;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.UserInfoRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.SmsSendObserver;
import com.ginko.common.Trie;
import com.ginko.common.Uitils;
import com.ginko.customview.BottomPopupWindow;
import com.ginko.customview.InputDialog;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.database.ContactTableModel;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserWholeProfileVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v4.app.ActivityCompat.requestPermissions;

public class InviteGinkoConnects extends MyBaseActivity implements View.OnClickListener ,
        ListView.OnScrollListener{

    /* UI Variables */
    private LinearLayout activityRootView;
    private Button btnDone;
    private ImageButton btnInviteContact;
    private ListView listViewConnects;
    private ProgressHUD progressHud;
    private EditText edtSearch;
    private ImageView btnClearSearch;
    private Button btnCancelSearch;

    private String finalEmailAddress = null;
    private String finalPhoneNumber = null;

    /* Variables */
    private GinkoConnectListAdapter listAdapter;
    private ArrayList<GinkoConnectItem> ginkoConnectItemsList = new ArrayList<GinkoConnectItem>();
    private ArrayList<GinkoConnectItem> inGinkoConnectList = new ArrayList<GinkoConnectItem>();
    private ArrayList<GinkoConnectItem> notInGinkoConnectList = new ArrayList<GinkoConnectItem>();

    private LoadPhoneContactsThread loadContactThread;

    private ArrayList<PhoneContactItem> phoneContactList = new ArrayList<PhoneContactItem>();
    private ArrayList<PhoneContactItem> localAddressContactList = new ArrayList<PhoneContactItem>();
    private ArrayList<PhoneContactItem> ginkoGreyContactList = new ArrayList<PhoneContactItem>();

    private boolean isThreadStopped = true;
    private boolean isProgressLeaf = true;

    private Pattern pattern;

    private int nPageNum = 0;
    private final int COUNT_PER_PAGE = 20;
    private int SEND_MAIL = 213;

    private boolean hasMoreInvitations = true;
    private boolean isLoadingInvitations = true;

    private final int SHARE_YOUR_LEAF = 1;

    private boolean isKeyboardVisible = false;

    private boolean fromMainContactScreen = false;
    private ContactChanged received_exchange_request;

    private long old_ID = 0;
    private boolean findContact_Counter = false;
    private int header_id = 0;

    private int m_orientHeight = 0;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    public InviteGinkoConnects() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_ginko_connect);

        Intent intent = this.getIntent();
        fromMainContactScreen = intent.getBooleanExtra("isFromMainContactScreen", false);

        getUIObjects();

        //loadContactsFromLocalAddressBook(getApplicationContext());
        //loadGinkoConnectAndInvitations((Integer) nPageNum);
        new initialize().execute();

        this.received_exchange_request = new ContactChanged();

        if (this.received_exchange_request != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.EXCHANGE_REQUEST");
            this.registerReceiver(this.received_exchange_request, msgReceiverIntent);
        }
    }

    private void loadGinkoConnectAndInvitations(final Integer pageNum)
    {
        isLoadingInvitations = true;
        String searchKeyword = null;
        if(!edtSearch.getText().toString().equals(""))
            searchKeyword = edtSearch.getText().toString().toLowerCase();
        final String strKeyword = searchKeyword;

        //if load first page , then should clear all contact values
        if(pageNum == 0){
            ginkoConnectItemsList = new ArrayList<GinkoConnectItem>();
            listAdapter.setItemList(ginkoConnectItemsList);
            inGinkoConnectList = new ArrayList<GinkoConnectItem>();
            notInGinkoConnectList = new ArrayList<GinkoConnectItem>();

            listAdapter.notifyDataSetChanged();
        }

        CBRequest.getExchangeInvitations(strKeyword, pageNum, COUNT_PER_PAGE, new ResponseCallBack<List<JSONObject>>() {
            @Override
            public void onCompleted(JsonResponse<List<JSONObject>> response) {
                if (response.isSuccess()) {
                    List<JSONObject> invitationList = response.getData();
                    if (invitationList.size() < COUNT_PER_PAGE)
                        hasMoreInvitations = false;

                    for (JSONObject obj : invitationList) {
                        int userId = obj.optInt("user_id", 0);
                        String email = obj.optString("email", "");
                        String phone = obj.optString("phone", "");
                        String name = obj.optString("name", "");
                        String profileImage = obj.optString("profile_image", "");
                        boolean isInGinko = obj.optBoolean("in_ginko", false);
                        boolean isSend = obj.optBoolean("is_send", false);
                        if (strKeyword != null && !(name.toLowerCase().contains(strKeyword) || phone.contains(strKeyword) || email.toLowerCase().contains(strKeyword))) {
                            continue;
                        }

                        GinkoConnectItem item = new GinkoConnectItem();
                        item.isSentInvite = isSend;
                        item.isNotInGinko = !isInGinko;
                        item.strEmail = email;
                        item.strMobileNumber = phone;
                        item.userId = String.valueOf(userId);
                        item.strConnectName = name;
                        item.strProfileImage = profileImage;
                        if (isInGinko)
                            inGinkoConnectList.add(item);
                        else
                            notInGinkoConnectList.add(item);
                    }
                }
                if (pageNum == 0) //if load first page , then should load the local address book contacts and grey contacts of ginko
                {
                    loadContactThread = null;
                    importAddressBook(strKeyword);
                } else {
                    isLoadingInvitations = false;
                    refreshListViewFromGinkoContacts();
                }
            }
        }, false);
    }

    public void hasDuplicates(ArrayList<GinkoConnectItem> pList) {
        final List<String> usedNames = new ArrayList<String>();
        final List<String> usedPhones = new ArrayList<String>();
        final List<String> usedEmails = new ArrayList<String>();

        Iterator<GinkoConnectItem> it = pList.iterator();
        while (it.hasNext()) {
            GinkoConnectItem nextItem = it.next();
            final String name = nextItem.strConnectName;
            final String phone = nextItem.strMobileNumber;
            final String email = nextItem.strEmail;

            if (name.trim().equals("") && phone.trim().equals("") && !email.trim().equals("")) {
                if (usedEmails.contains(email))
                    it.remove();
                else
                {
                    usedNames.add(name);
                    usedEmails.add(email);
                    usedPhones.add(phone);
                }
            } else if (name.trim().equals("") && !phone.trim().equals("")) {
                if (usedPhones.contains(phone))
                    it.remove();
                else
                {
                    usedNames.add(name);
                    usedEmails.add(email);
                    usedPhones.add(phone);
                }
            } else if (!name.trim().equals("")) {
                if (usedNames.contains(name))
                    it.remove();
                else
                {
                    usedNames.add(name);
                    usedEmails.add(email);
                    usedPhones.add(phone);
                }
            }
        }
    }

    private void loadContactsFromLocalAddressBook(Context context)
    {
        // Check the SDK version and whether the permission is already granted or not.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            if(localAddressContactList == null)
                localAddressContactList = new ArrayList<PhoneContactItem>();
            else
                localAddressContactList.clear();

            ContentResolver resolver = getContentResolver();
            Cursor c = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    "(" + ContactsContract.Data.HAS_PHONE_NUMBER + "==0 OR " + ContactsContract.Data.HAS_PHONE_NUMBER + "!=0) And (" + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?)",
                    new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                    ContactsContract.Data.CONTACT_ID);
            if(c.isBeforeFirst()){
                while (c.moveToNext()) {
                    //Get first field value.
                    long id = c.getLong(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    if(id == old_ID)
                        continue;
                    String contactName = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String data1 = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1)).trim();
                    String email = "";
                    String phonenumber = "";

                    if(c.moveToNext()) {
                        //Get second field value.
                        long id_after = c.getLong(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                        String contactName_after = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        String data1_after = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1)).trim();

                        boolean flag = false;
                        if (contactName == "Alx56")
                            flag = true;

                        old_ID = id_after;

                        if (id == id_after) {
                            if (data1.contains(".com") || data1.contains(".net"))
                                email = data1;
                            else {
                                String alphaAndDigits = data1.replaceAll("[^\\p{Alpha}\\p{Digit}]+","");
                                if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?") || alphaAndDigits.matches("[0-9]+") && alphaAndDigits.length() > 2)
                                    phonenumber = alphaAndDigits;
                            }

                            if (data1_after.contains(".com") || data1.contains(".net"))
                                email = data1_after;
                            else {
                                String alphaAndDigits = data1_after.replaceAll("[^\\p{Alpha}\\p{Digit}]+","");
                                if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?"))if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?") || alphaAndDigits.matches("[0-9]+") && alphaAndDigits.length() > 2)
                                    phonenumber = alphaAndDigits;

                            }

                            contactName = contactName.trim();
                            email = email.trim();
                            phonenumber = phonenumber.trim();

                            if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                                localAddressContactList.add(new PhoneContactItem(contactName, email, phonenumber, true));
                            }
                        } else {
                            if (data1.contains(".com") || data1.contains(".net"))
                                email = data1;
                            else {
                                String alphaAndDigits = data1.replaceAll("[^\\p{Alpha}\\p{Digit}]+","");
                                if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?") || alphaAndDigits.matches("[0-9]+") && alphaAndDigits.length() > 2)
                                    phonenumber = alphaAndDigits;
                            }

                            contactName = contactName.trim();
                            email = email.trim();
                            phonenumber = phonenumber.trim();

                            if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                                localAddressContactList.add(new PhoneContactItem(contactName, email, phonenumber, true));
                            }

                            phonenumber = "";
                            email = "";


                            if (data1_after.contains(".com") || data1_after.contains(".net"))
                                email = data1_after;
                            else {
                                String alphaAndDigits = data1_after.replaceAll("[^\\p{Alpha}\\p{Digit}]+","");
                                if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?"))if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?") || alphaAndDigits.matches("[0-9]+") && alphaAndDigits.length() > 2)
                                    phonenumber = alphaAndDigits;

                            }

                            contactName = contactName_after.trim();
                            email = email.trim();
                            phonenumber = phonenumber.trim();

                            if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                                localAddressContactList.add(new PhoneContactItem(contactName, email, phonenumber, true));
                            }
                        }
                    }
                    else {
                        if (data1.contains(".com") || data1.contains(".net"))
                            email = data1;
                        else {
                            String alphaAndDigits = data1.replaceAll("[^\\p{Alpha}\\p{Digit}]+","");
                            if (alphaAndDigits.matches("\\d+(?:\\.\\d+)?") || alphaAndDigits.matches("[0-9]+") && alphaAndDigits.length() > 2)
                                phonenumber = alphaAndDigits;
                        }

                        contactName = contactName.trim();
                        email = email.trim();
                        phonenumber = phonenumber.trim();

                        if (!(contactName.equals("") || (email.equals("") && phonenumber.equals("")))) {
                            localAddressContactList.add(new PhoneContactItem(contactName, email, phonenumber, true));
                        }
                    }
                }
            }
        }

        /*PhoneContactItem item1 = new PhoneContactItem("Sam" , "ron@atrient.com" , "222-222-2222" , true); localAddressContactList.add(item1);
        PhoneContactItem item2 = new PhoneContactItem("Abram" , "" , "227556" , true); localAddressContactList.add(item2);
        PhoneContactItem item3 = new PhoneContactItem("Boy" , "" , "12345678" , true); localAddressContactList.add(item3);
        PhoneContactItem item4 = new PhoneContactItem("Zam" , "" , "13456778" , true); localAddressContactList.add(item4);*/
    }

    private void loadGinkoGreyContacts()
    {
        if(ginkoGreyContactList == null)
            ginkoGreyContactList = new ArrayList<PhoneContactItem>();
        else
            ginkoGreyContactList.clear();

        //import grey_contacts
        ContactTableModel contactTableModel = MyApp.getInstance().getContactsModel();

        if (contactTableModel != null) {
            List<ContactStruct> greyContacts = contactTableModel.getAllGreyContactItems();
            for (ContactStruct greyContactItem : greyContacts) {
                String name = greyContactItem.getFirstName() + " " + greyContactItem.getMiddleName();
                name = name.trim();
                name = name + greyContactItem.getLastName();
                ContactItem contactItem = greyContactItem.getContactItem();
                String strEmail = "", strMobileNumber = "";
                if (contactItem.getPhones() != null && contactItem.getPhones().size() > 0)
                    strMobileNumber = contactItem.getPhones().get(0);
                if (contactItem.getEmails() != null && contactItem.getEmails().size() > 0)
                    strEmail = contactItem.getEmails().get(0);
                if (strEmail.equals("") && strMobileNumber.equals(""))
                    continue;

                //grey contact is not from local address book
                ginkoGreyContactList.add(new PhoneContactItem(name, strEmail, strMobileNumber, false));
            }
        }
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnDone = (Button)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);
        btnInviteContact = (ImageButton)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);

        activityRootView = (LinearLayout)findViewById(R.id.rootLayout);
        activityRootView.requestFocus();
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        edtSearch.setCursorVisible(true);
                        if (edtSearch.getText().toString().length() > 0) {
                            btnClearSearch.setVisibility(View.VISIBLE);
                        } else {
                            btnClearSearch.setVisibility(View.GONE);
                        }
                        btnCancelSearch.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isKeyboardVisible) {
                        //isKeyboardVisible = false;
                        //edtSearch.setCursorVisible(false);
                        if (edtSearch.getText().toString().length() > 0)
                            btnClearSearch.setVisibility(View.VISIBLE);
                        else
                            btnClearSearch.setVisibility(View.GONE);
                        /*if(btnCancelSearch.getVisibility() == View.VISIBLE)
                            btnCancelSearch.setVisibility(View.GONE);*/
                    }
                }
            }
        });

        listViewConnects = (ListView)findViewById(R.id.listViewConnects);

        listViewConnects.setOnScrollListener(this);

        listAdapter = new GinkoConnectListAdapter(this);
        if(ginkoConnectItemsList == null) {
            ginkoConnectItemsList = new ArrayList<GinkoConnectItem>();
        }
        listAdapter.setItemList(ginkoConnectItemsList);

        listViewConnects.setAdapter(listAdapter);

        listViewConnects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
                //Add by lee for GAD-1359
                /*if (listAdapter == null) return;
                if (listAdapter.getCount() < 1) return;
                GinkoConnectItem item = (GinkoConnectItem) listAdapter.getItem(position);
                if (item.isNotInGinko || item.isHeaderView) return;
                if (item.userId.equals("")) return;
                long userId = 0;
                try {
                    userId = Long.valueOf(item.userId);
                } catch (Exception e) {
                    e.printStackTrace();
                    userId = 0;
                }
                if (userId <= 0) return;

                gotoSharingScreen(item);*/
            }
        });

        progressHud = ProgressHUD.createProgressDialog(InviteGinkoConnects.this, "", false, false, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(loadContactThread != null && loadContactThread.isAlive())
                {
                    isThreadStopped = true;
                    try
                    {
                        loadContactThread.stop();
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        loadContactThread = null;
                        isThreadStopped = true;
                        if(progressHud != null && progressHud.isShowing())
                            progressHud.dismiss();
                    }
                }
            }
        });

        edtSearch = (EditText)findViewById(R.id.edtSearch);

        btnCancelSearch = (Button)findViewById(R.id.btnCancelSearch); btnCancelSearch.setVisibility(View.GONE);
        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);//default is gone

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
                                                 btnCancelSearch.setVisibility(View.VISIBLE);
                                             }

                                             @Override
                                             public void afterTextChanged(Editable s) {
                                                 // TODO Auto-generated method stub
                                                 searchItems(edtSearch.getText().toString().toLowerCase());
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
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    edtSearch.clearFocus();
                    btnCancelSearch.setVisibility(View.GONE);
                    /* modify by lee for GAD-1022
                    searchItems(null);
                    nPageNum = 0;
                    hasMoreInvitations = true;
                    loadGinkoConnectAndInvitations((Integer) nPageNum);*/
                    return true;
                }
                return false;
            }
        });

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
                    if (edtSearch.getText().toString().length() > 0) {
                        btnClearSearch.setVisibility(View.VISIBLE);
                    } else {
                        btnClearSearch.setVisibility(View.GONE);
                    }
                    btnCancelSearch.setVisibility(View.GONE);
                }
            }
        });

        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
                activityRootView.requestFocus();
                btnCancelSearch.setVisibility(View.GONE);
                searchItems(null);
                //nPageNum = 0;
                //hasMoreInvitations = true;
                //loadGinkoConnectAndInvitations((Integer) nPageNum);
                hideKeyboard();
            }
        });

        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
                searchItems("");
                edtSearch.requestFocus();
                showKeyboard();
            }
        });
    }

    private void showKeyboard()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void searchItems(String searchKeyword)
    {
        //listAdapter.searchItems(searchKeyword);

        if(ginkoConnectItemsList == null) return;
        ArrayList<GinkoConnectItem> tempList = new ArrayList<GinkoConnectItem>(ginkoConnectItemsList);

        boolean findInGinko = false;
        boolean findNotInGinko = false;
        GinkoConnectItem item_header = null;
        GinkoConnectItem item_headernot = null;

        if(searchKeyword == null || searchKeyword.equals(""))
        {

        }
        else {
            //for (GinkoConnectItem item : itemList) {

            for(int i = 0; i < ginkoConnectItemsList.size(); i++) {
                GinkoConnectItem item = ginkoConnectItemsList.get(i);
                if (item.isHeaderView)
                {
                    if (item.isNotInGinko == true)
                        item_headernot = item;
                    else
                        item_header = item;
                    continue;
                }

                if (item.strConnectName.toLowerCase().contains(searchKeyword) || item.strMobileNumber.contains(searchKeyword) || item.strEmail.contains(searchKeyword))
                {
                    if (item.isNotInGinko == true)
                        findNotInGinko = true;
                    else
                        findInGinko = true;
                }
                else
                {
                    tempList.remove(item);
                }
            }

            if (item_header != null && findInGinko == false)
                tempList.remove(item_header);
            if (item_headernot != null && findNotInGinko == false)
                tempList.remove(item_headernot);
        }

        listAdapter.setItemList(tempList);
        listAdapter.notifyDataSetChanged();
    }

    private void isShownKeyboard() {
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int curheight= rectgle.bottom;
        if(m_orientHeight == curheight)
            isKeyboardVisible = false;
        else if (m_orientHeight > 0)
            isKeyboardVisible = true;
    }

    private synchronized void refreshListViewFromGinkoContacts()
    {
        //sort ginko connect itmes by contact name
        GinkoConnectItemComparator ginkoConnectItemComparator = new GinkoConnectItemComparator();
        try
        {
            Collections.sort(inGinkoConnectList, ginkoConnectItemComparator);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try {
            Collections.sort(notInGinkoConnectList, ginkoConnectItemComparator);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        if(ginkoConnectItemsList == null) {
            ginkoConnectItemsList = new ArrayList<GinkoConnectItem>();
            listAdapter.setItemList(ginkoConnectItemsList);
        }
        ginkoConnectItemsList.clear();

        //add header item ("in Ginko")
        if(!inGinkoConnectList.isEmpty()) {
            GinkoConnectItem inGinkoHeaderItem = new GinkoConnectItem();
            inGinkoHeaderItem.isHeaderView = true;
            inGinkoHeaderItem.isNotInGinko = false;
            inGinkoHeaderItem.strConnectName = "";
            inGinkoHeaderItem.strMobileNumber = "";
            inGinkoHeaderItem.strEmail = "";
            ginkoConnectItemsList.add(inGinkoHeaderItem);

            for (GinkoConnectItem item : inGinkoConnectList) {
                if(item.strEmail.equals("Cancel") || item.strMobileNumber.equals("Cancel"))
                    continue;
                ginkoConnectItemsList.add(item);
            }
        }
        //add header item ("Not in Ginko")
        GinkoConnectItem notInGinkoHeaderItem = new GinkoConnectItem();
        notInGinkoHeaderItem.isHeaderView = true;
        notInGinkoHeaderItem.isNotInGinko = true;
        notInGinkoHeaderItem.strConnectName = "";
        notInGinkoHeaderItem.strMobileNumber = "";
        notInGinkoHeaderItem.strEmail = "";
        ginkoConnectItemsList.add(notInGinkoHeaderItem);

        for(GinkoConnectItem item : notInGinkoConnectList) {
            if(item.strEmail.equals("Cancel") || item.strMobileNumber.equals("Cancel"))
                continue;
            ginkoConnectItemsList.add(item);
        }

        listAdapter.notifyDataSetChanged();
    }


    public void importAddressBook(String strKeyword) {
        if(loadContactThread != null)
        {
            return;
        }
        else
        {
            isThreadStopped = false;
            /*if(progressHud != null)
                progressHud.show();*/
            loadContactThread = new LoadPhoneContactsThread(getApplicationContext() , strKeyword);
            loadContactThread.start();
        }

    }


    //check the bottom scroll event of listview
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(listAdapter == null) return;
        if(!hasMoreInvitations) return;

        int count = totalItemCount - visibleItemCount;

        if(firstVisibleItem >= count && totalItemCount != 0
                && isLoadingInvitations == false)
        {
            //load more invitations
            nPageNum++;
            loadGinkoConnectAndInvitations((Integer) nPageNum);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(listAdapter != null)
            listAdapter.notifyDataSetChanged();

        if (this.received_exchange_request != null) {
            IntentFilter msgReceiverIntent = new IntentFilter();
            msgReceiverIntent.addAction("android.intent.action.EXCHANGE_REQUEST");
            this.registerReceiver(this.received_exchange_request, msgReceiverIntent);
        }
        /*activityRootView.requestFocus();
        MyApp.getInstance().hideKeyboard(activityRootView);*/

        if(isKeyboardVisible) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
            edtSearch.requestFocus();
        }else {
            //edtSearch.setText("");
            //activityRootView.requestFocus();
            btnCancelSearch.setVisibility(View.GONE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            activityRootView.requestFocus();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SHARE_YOUR_LEAF && data != null)
        {
            String contactId = data.getStringExtra("contactID");
            int nSharingStatus = data.getIntExtra("nSharingStatus" , 0);
            //if(nSharingStatus != ConstValues.SHARE_NONE)
            if(nSharingStatus != 11)
            {
                for(GinkoConnectItem connectItem : inGinkoConnectList)
                {
                    if(connectItem.userId.equals("")) continue;
                    if(connectItem.userId.equals(contactId)) {
                        //inGinkoConnectList.remove(connectItem);
                        connectItem.isSentInvite = true;
                        break;
                    }
                }
                for(GinkoConnectItem connectItem : ginkoConnectItemsList)
                {
                    if(connectItem.userId.equals("")) continue;
                    if(connectItem.userId.equals(contactId)) {
                        //ginkoConnectItemsList.remove(connectItem);
                        connectItem.isSentInvite = true;
                        break;
                    }
                }
            }
            else{
                for(GinkoConnectItem connectItem : inGinkoConnectList)
                {
                    if(connectItem.userId.equals("")) continue;
                    if(connectItem.userId.equals(contactId)) {
                        //inGinkoConnectList.remove(connectItem);
                        connectItem.isSentInvite = false;
                        break;
                    }
                }
                for(GinkoConnectItem connectItem : ginkoConnectItemsList)
                {
                    if(connectItem.userId.equals("")) continue;
                    if(connectItem.userId.equals(contactId)) {
                        //ginkoConnectItemsList.remove(connectItem);
                        connectItem.isSentInvite = false;
                        break;
                    }
                }
            }
        }
        if(requestCode == SEND_MAIL) {
            switch(resultCode)
            {
                case RESULT_OK:
                    edtSearch.requestFocus();
                    btnCancelSearch.setVisibility(View.GONE);
                    hideKeyboard();
                    break;
                case RESULT_CANCELED:
                    edtSearch.requestFocus();
                    btnCancelSearch.setVisibility(View.GONE);
                    hideKeyboard();
                    break;
            }
        }
        if(listAdapter != null)
            listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.received_exchange_request != null) {
            this.unregisterReceiver(this.received_exchange_request);
        }
        isShownKeyboard();
        //hideKeyboard();
        if(!isKeyboardVisible)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        else {
            MyApp.getInstance().hideKeyboard(activityRootView);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void hideKeyboard()
    {
        //if(isKeyboardVisible)
        activityRootView.requestFocus();
        MyApp.getInstance().hideKeyboard(activityRootView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        if(fromMainContactScreen)
        {
            super.onBackPressed();
        }
        //super.onBackPressed();
    }

    private void addInviteContact(String emailOrPhoneNnumber) {
        String emailAddress = null;
        String phoneNumber = null;
        if (emailOrPhoneNnumber.contains("@")) {
            emailAddress = emailOrPhoneNnumber;
            for(int i = 0; i < ginkoConnectItemsList.size(); i++){
                if(ginkoConnectItemsList.get(i).strEmail.equals(emailAddress))
                {
                    MyApp.getInstance().showSimpleAlertDiloag(InviteGinkoConnects.this, "The invite already exists in the Ginko Connect list", null);
                    return;
                }
            }
        }
        else {
            phoneNumber = emailOrPhoneNnumber;
            for(int i = 0; i < ginkoConnectItemsList.size(); i++){
                if(ginkoConnectItemsList.get(i).strMobileNumber.equals(phoneNumber))
                {
                    MyApp.getInstance().showSimpleAlertDiloag(InviteGinkoConnects.this, "The invite already exists in the Ginko Connect list", null);
                    return;
                }
            }
        }

        finalEmailAddress = emailAddress;
        finalPhoneNumber = phoneNumber;

        CBRequest.addInvitation(emailAddress, phoneNumber, new ResponseCallBack<List<JSONObject>>() {
            @Override
            public void onCompleted(JsonResponse<List<JSONObject>> response) {
                if (response.isSuccess()) {
                    if (finalEmailAddress == null || finalEmailAddress.equals("")) //if invited user is invited by phone number
                    {
                        String content = getString(R.string.str_ginko_invites_text_prefix) +
                                "user name: " + Uitils.getUserName(InviteGinkoConnects.this) + //Uitils.getUserFullname(InviteGinkoConnects.this) +
                                getString(R.string.str_ginko_invites_text_suffix);
                        GinkoConnectItem connectItem = new GinkoConnectItem();
                        connectItem.isHeaderView = false;
                        connectItem.isNotInGinko = true;
                        connectItem.isSentInvite = true;
                        connectItem.strConnectName = "";
                        connectItem.strProfileImage = "";
                        connectItem.strMobileNumber = finalPhoneNumber;
                        connectItem.userId = String.valueOf(connectItem.userId);
                        addInvitedItemToList(connectItem);
                        sendSMS(false, finalPhoneNumber, content);
                    } else {
                        List<JSONObject> addedInvites = response.getData();
                        for (JSONObject obj : addedInvites) {
                            int userId = obj.optInt("user_id", 0);
                            String email = obj.optString("email", "");
                            String profileImage = obj.optString("profile_image", "");
                            String name = obj.optString("name", "");

                            GinkoConnectItem connectItem = new GinkoConnectItem();
                            connectItem.isHeaderView = false;
                            if (userId > 0)
                                connectItem.isNotInGinko = false;
                            else
                                connectItem.isNotInGinko = true;
                            connectItem.strConnectName = name;
                            connectItem.strProfileImage = profileImage;
                            connectItem.strMobileNumber = "";
                            connectItem.strEmail = email;
                            connectItem.userId = String.valueOf(userId);
                            if (userId > 0) {
                                connectItem.isSentInvite = false;
                                addInvitedItemToList(connectItem);
                                if (isProgressLeaf == true)
                                    gotoSharingScreen(connectItem);
                            } else if (userId == 0 && !email.equals("")) {
                                connectItem.isSentInvite = true;
                                addInvitedItemToList(connectItem);
                                String content = getString(R.string.str_ginko_invites_text_prefix) +
                                        "user name: " + Uitils.getUserName(InviteGinkoConnects.this) + //Uitils.getUserFullname(InviteGinkoConnects.this) +
                                        getString(R.string.str_ginko_invites_text_suffix);
                                sendEmail(false, email, content);
                            }
                            break;
                        }
                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InviteGinkoConnects.this);
                    builder.setMessage("Sorry, " + response.getErrorMessage());
                    builder.setPositiveButton(getResources().getString(R.string.str_okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            hideKeyboard();
                            paramDialogInterface.dismiss();
                        }
                    });
                    builder.show();
                    //Uitils.alert(InviteGinkoConnects.this, "Sorry, " + response.getErrorMessage());

                }
            }
        });
    }

    private void gotoSharingScreen(final GinkoConnectItem item)
    {
        isProgressLeaf = false;
        //GAD-1623 Update
        UserInfoRequest.getInfo(Integer.valueOf(item.userId), new ResponseCallBack<UserWholeProfileVO>() {
            @Override
            public void onCompleted(JsonResponse<UserWholeProfileVO> response) throws IOException {
                isProgressLeaf = true;
                if (response.isSuccess()) {
                    UserWholeProfileVO myProfileInfo = response.getData();
                    //if (myProfileInfo.getShare() == null)//non-exchanged info
                    //{
                        Intent contactSharingSettingIntent = new Intent(InviteGinkoConnects.this, ShareYourLeafActivity.class);
                        contactSharingSettingIntent.putExtra("contactID", item.userId);
                        contactSharingSettingIntent.putExtra("contactFullname", item.strConnectName);
                        contactSharingSettingIntent.putExtra("isUnexchangedContact", true);
                        contactSharingSettingIntent.putExtra("isInviteContact", item.isSentInvite);
                        contactSharingSettingIntent.putExtra("isPendingRequest", item.isSentInvite);
                        startActivityForResult(contactSharingSettingIntent, SHARE_YOUR_LEAF);
                    /*
                    } else {
                        for (GinkoConnectItem connectItem : inGinkoConnectList) {
                            if (connectItem.userId.equals("")) continue;
                            if (connectItem.userId.equals(item.userId)) {
                                inGinkoConnectList.remove(connectItem);
                                break;
                            }
                        }
                        for (GinkoConnectItem connectItem : ginkoConnectItemsList) {
                            if (connectItem.userId.equals("")) continue;
                            if (connectItem.userId.equals(item.userId)) {
                                ginkoConnectItemsList.remove(connectItem);
                                break;
                            }
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                    */
                }
            }
        }) ;
    }

    private void addInvitedItemToList(GinkoConnectItem item)
    {
        if(ginkoConnectItemsList == null) {
            ginkoConnectItemsList = new ArrayList<GinkoConnectItem>();
            listAdapter.setItemList(ginkoConnectItemsList);
        }
        boolean added = false;
        if(item.isNotInGinko)
            notInGinkoConnectList.add(item);
        else
            inGinkoConnectList.add(item);

        //sort ginko connect itmes by contact name
        GinkoConnectItemComparator ginkoConnectItemComparator = new GinkoConnectItemComparator();
        try
        {
            Collections.sort(inGinkoConnectList, ginkoConnectItemComparator);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            Collections.sort(notInGinkoConnectList, ginkoConnectItemComparator);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        for(int i=0;i<ginkoConnectItemsList.size();i++)
        {
            if(ginkoConnectItemsList.get(i).isHeaderView) continue;
            if(item.isNotInGinko == ginkoConnectItemsList.get(i).isNotInGinko && item.strConnectName.compareTo(ginkoConnectItemsList.get(i).strConnectName)>=0)
            {
                added = true;
                ginkoConnectItemsList.add(i,item);
                break;
            }
        }
        if(!added)
        {
            if(item.isNotInGinko)
                ginkoConnectItemsList.add(item);
            else
                ginkoConnectItemsList.add(0 , item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnDone:
                /*Intent intent = new Intent(InviteGinkoConnects.this , GetStart.class);
                intent.putExtra("fullname" , Uitils.getUserFullname(getApplicationContext()));
                startActivity(intent);*/
                hideKeyboard();
                if(fromMainContactScreen) {
                    Intent intent = new Intent();
                    intent.setClass(InviteGinkoConnects.this, ContactMainActivity.class);
                    InviteGinkoConnects.this.startActivity(intent);
                    InviteGinkoConnects.this.finish();
                }
                else {
                    Intent tutorialIntent = new Intent(InviteGinkoConnects.this , TutorialActivity.class);
                    tutorialIntent.putExtra("isFromSignUp" , true);
                    InviteGinkoConnects.this.startActivity(tutorialIntent);
                    InviteGinkoConnects.this.finish();
                }
                break;

            case R.id.btnInviteContact:
                final String inputErrHint = "Please enter email address or mobile number.";

                final InputDialog inviteEmailDialog = new InputDialog(this,
                        -1,//not sure , default is email input
                        getResources().getString(R.string.str_invite_email_dialog_description) , //title
                        "", //txtHint
                        false , //show titlebar
                        getResources().getString(R.string.str_cancel) , //left button name
                        new InputDialog.OnButtonClickListener(){
                            @Override
                            public boolean onClick(Dialog dialog , View v, String input) {
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                hideKeyboard();
                                return  true;
                            }//left button clicklistener
                        },
                        getResources().getString(R.string.str_okay), //right button name
                        new InputDialog.OnButtonClickListener() //right button clicklistener
                        {
                            @Override
                            public boolean onClick(Dialog dialog , View v , String email) {
                                if(email.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(InviteGinkoConnects.this, inputErrHint, Toast.LENGTH_LONG).show();
                                    hideKeyboard();
                                    return false;
                                }
                                if(email.contains("@") && !isEmailValid(email.trim()))
                                {
                                    Toast.makeText(InviteGinkoConnects.this, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
                                    hideKeyboard();
                                    return false;
                                }

                                addInviteContact(email);
                                hideKeyboard();

                                return true;
                            }
                        },
                        new InputDialog.OnEditorDoneActionListener() {

                            @Override
                            public void onEditorActionDone(Dialog dialog, String email) {

                                if(email.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(InviteGinkoConnects.this, inputErrHint, Toast.LENGTH_LONG).show();
                                    hideKeyboard();
                                    return;
                                }
                                if(email.contains("@") && !isEmailValid(email.trim()))
                                {
                                    Toast.makeText(InviteGinkoConnects.this, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
                                    hideKeyboard();
                                    return;
                                }
                                dialog.dismiss();

                                addInviteContact(email);
                                hideKeyboard();

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
                break;
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

    private void sendSMS(final boolean fromLocalAddressBook , final String reciver , final String content){
        CBRequest.sendInviteStatus(null, reciver, fromLocalAddressBook , new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    Uri uri = Uri.parse("smsto:" + reciver);
                    Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                    it.putExtra("sms_body", content);
                    it.putExtra("exit_on_sent", true);
                    startActivityForResult(it, SEND_MAIL);
                } else {
                    Uitils.alert(InviteGinkoConnects.this , response.getErrorMessage());
                }
            }
        });

    }

    private void sendEmail(boolean fromLocalAddressBook , final String receiver , final String content)
    {
        CBRequest.sendInviteStatus(receiver, null, fromLocalAddressBook , new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{receiver});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Exchange contact info with me via Ginko");//subject
                    intent.putExtra(Intent.EXTRA_TEXT, content + "\n\n\n" + getResources().getString(R.string.str_send_email_bottom_suffix));

                    startActivityForResult(Intent.createChooser(intent, "Send Email"), SEND_MAIL);
                } else {
                    Uitils.alert(InviteGinkoConnects.this, "Sorry, " + response.getErrorMessage());
                }
            }
        });

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    class PhoneContactItem
    {
        public String contactName;
        public String email;
        public String phoneNumber;
        public boolean isFromLocalAddressBook = false;
        public PhoneContactItem(String contactName , String email , String phoneNumber , boolean isFromLocalAddressBook)
        {
            this.contactName = contactName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.isFromLocalAddressBook = isFromLocalAddressBook;
        }
    }

    private JSONObject makePhoneContactJSONObject(PhoneContactItem item)
    {
        JSONObject object = new JSONObject();
        try {
            if (item.email != null && !item.email.equals(""))
                object.putOpt("email", item.email);
            if (item.phoneNumber != null && !item.phoneNumber.equals(""))
                object.putOpt("phone", item.phoneNumber);
            if (object.toString().equals(""))
                return null;
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return object;
    }


    private class LoadPhoneContactsThread extends Thread{
        private Context mContext;
        private String searchKeyword = null;
        private long recordStartTime = 0;

        public LoadPhoneContactsThread(Context context , String strKeyword)
        {
            this.mContext = context;
            this.searchKeyword = strKeyword;
        }

        @Override
        public void run()
        {
            recordStartTime = System.currentTimeMillis();

            phoneContactList = new ArrayList<PhoneContactItem>();

            for(PhoneContactItem item : localAddressContactList)
            {
                if(searchKeyword != null && !(item.contactName.toLowerCase().contains(searchKeyword) || item.phoneNumber.contains(searchKeyword) || item.email.toLowerCase().contains(searchKeyword)))
                {
                    continue;
                }

                phoneContactList.add(item);
            }

            //check the phone contacts whether they are in ginko or not
            //show listview
            final Handler mHandler = new Handler(mContext.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressHud.isShowing())
                        progressHud.dismiss();
                    //if(phoneContactList.size() > 0)
                    {
                        JSONObject data = new JSONObject();
                        JSONArray dataArray = new JSONArray();

                        //sort phone contact list by name
                        PhoneContactItemComparator phoneContactItemComparator = new PhoneContactItemComparator();
                        try {
                            Collections.sort(phoneContactList, phoneContactItemComparator);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            for (int i = 0; i < phoneContactList.size(); i++) {
                                PhoneContactItem item = phoneContactList.get(i);
                                JSONObject object = makePhoneContactJSONObject(item);
                                if (object != null)
                                    dataArray.put(object);

                            }
                            data.put("data", dataArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //check lcoal phone's contacts whether they are existing users of ginko
                        CBRequest.checkUsers(data, new ResponseCallBack<List<JSONObject>>() {
                            @Override
                            public void onCompleted(JsonResponse<List<JSONObject>> response) {
                                if (response.isSuccess()) {
                                    List<JSONObject> objects = response.getData();
                                    for (int i = 0; i < objects.size(); i++) {
                                        JSONObject obj = objects.get(i);
                                        String email = obj.optString("email", "");
                                        String phone = obj.optString("phone", "");
                                        String strUser_id = obj.optString("user_id", "");
                                        String strUserName = obj.optString("name", "");
                                        String strProfileImage = obj.optString("profile_image", "");
                                        boolean isSend = obj.optBoolean("is_send", false);
                                        boolean inGinko = obj.optBoolean("in_ginko", false);
                                        boolean isFriend = obj.optBoolean("is_friend", false);

                                        if(searchKeyword != null && !(strUserName.toLowerCase().contains(searchKeyword) || phone.contains(searchKeyword) || email.toLowerCase().contains(searchKeyword)))
                                        {
                                            continue;
                                        }

                                        if (email.equals("") && phone.equals("")) continue;

                                        /*
                                        for (int j = 0; j < phoneContactList.size(); j++) {
                                            PhoneContactItem item = phoneContactList.get(j);
                                            if (!email.equals("") && item.email.equalsIgnoreCase(email)) {
                                                phoneContactList.remove(j);
                                                break;
                                            }
                                            if (!phone.equals("") && item.phoneNumber.equalsIgnoreCase(phone)) {
                                                phoneContactList.remove(j);
                                                break;
                                            }
                                        }
                                        */

                                        GinkoConnectItem connectItem = new GinkoConnectItem();
                                        connectItem.isHeaderView = false;
                                        connectItem.isNotInGinko = !inGinko;
                                        connectItem.strConnectName = strUserName;
                                        connectItem.strProfileImage = strProfileImage;
                                        connectItem.isFromLocalAddressBook = true;
                                        if (!phone.equals(""))
                                            connectItem.strMobileNumber = phone;
                                        else
                                            connectItem.strEmail = email;
                                        connectItem.isSentInvite = isSend;
                                        connectItem.userId = strUser_id;

                                        if (inGinko && !isFriend)
                                            inGinkoConnectList.add(connectItem);
                                        else if (!inGinko)
                                            notInGinkoConnectList.add(connectItem);
                                    }
                                }

                                loadGinkoGreyContacts();
                                //add not in ginko connects from phone address book
                                //GAD-1568 Fast Remove Duplicates (Ginko Connect List Speed up)
                                Set<String> hashNames = new HashSet<>();
                                Set<String> hashPhones = new HashSet<>();
                                Set<String> hashEmails = new HashSet<>();

                                for (int i = 0; i < ginkoGreyContactList.size(); i++) {
                                    PhoneContactItem phoneContactItem = ginkoGreyContactList.get(i);
                                    if(searchKeyword != null && !(phoneContactItem.contactName.toLowerCase().contains(searchKeyword) || phoneContactItem.phoneNumber.contains(searchKeyword) || phoneContactItem.email.toLowerCase().contains(searchKeyword)))
                                    {
                                        continue;
                                    }
                                    GinkoConnectItem connectItem = new GinkoConnectItem();
                                    connectItem.isHeaderView = false;
                                    connectItem.isNotInGinko = true;
                                    connectItem.strConnectName = phoneContactItem.contactName;
                                    connectItem.isFromLocalAddressBook = phoneContactItem.isFromLocalAddressBook;
                                    if (!phoneContactItem.phoneNumber.equals(""))
                                        connectItem.strMobileNumber = phoneContactItem.phoneNumber;
                                    if (!phoneContactItem.email.equals(""))
                                        connectItem.strEmail = phoneContactItem.email;

                                    int nPreviousCnt = hashNames.size();
                                    hashNames.add(connectItem.strConnectName);
                                    if (nPreviousCnt != hashNames.size()) {
                                        notInGinkoConnectList.add(connectItem);
                                    } else if (connectItem.strConnectName.equals("")) {
                                        nPreviousCnt = hashPhones.size();
                                        hashPhones.add(connectItem.strMobileNumber);
                                        if (nPreviousCnt != hashPhones.size()) {
                                            notInGinkoConnectList.add(connectItem);
                                        } else if (connectItem.strMobileNumber.equals("")) {
                                            nPreviousCnt = hashEmails.size();
                                            hashEmails.add(connectItem.strMobileNumber);
                                            if (nPreviousCnt != hashEmails.size())
                                                notInGinkoConnectList.add(connectItem);
                                        }
                                    }
                                }

                                for (int i = 0; i < phoneContactList.size(); i++) {
                                    PhoneContactItem phoneContactItem = phoneContactList.get(i);
                                    GinkoConnectItem connectItem = new GinkoConnectItem();
                                    connectItem.isHeaderView = false;
                                    connectItem.isNotInGinko = true;
                                    connectItem.strConnectName = phoneContactItem.contactName;
                                    connectItem.isFromLocalAddressBook = phoneContactItem.isFromLocalAddressBook;
                                    if (!phoneContactItem.phoneNumber.equals(""))
                                        connectItem.strMobileNumber = phoneContactItem.phoneNumber;
                                    if (!phoneContactItem.email.equals(""))
                                        connectItem.strEmail = phoneContactItem.email;

                                    int nPreviousCnt = hashNames.size();
                                    hashNames.add(connectItem.strConnectName);
                                    if (nPreviousCnt != hashNames.size()) {
                                        notInGinkoConnectList.add(connectItem);
                                    } else if (connectItem.strConnectName.equals("")) {
                                        nPreviousCnt = hashPhones.size();
                                        hashPhones.add(connectItem.strMobileNumber);
                                        if (nPreviousCnt != hashPhones.size()) {
                                            notInGinkoConnectList.add(connectItem);
                                        } else if (connectItem.strMobileNumber.equals("")) {
                                            nPreviousCnt = hashEmails.size();
                                            hashEmails.add(connectItem.strMobileNumber);
                                            if (nPreviousCnt != hashEmails.size())
                                                notInGinkoConnectList.add(connectItem);
                                        }
                                    }
                                }

                                if (hashNames != null)
                                    hashNames.clear();
                                if (hashEmails != null)
                                    hashEmails.clear();
                                if (hashPhones != null)
                                    hashPhones.clear();
                                //hasDuplicates(inGinkoConnectList);
                                //hasDuplicates(notInGinkoConnectList);
                                refreshListViewFromGinkoContacts();

                                isLoadingInvitations = false;
                            }
                        });
                    }
                }
            });

        }
    }
    class PhoneContactItemComparator implements Comparator<PhoneContactItem> {
        public PhoneContactItemComparator()
        {
        }

        public int compare(PhoneContactItem left, PhoneContactItem right) {
            //an integer < 0 if lhs is less than rhs, 0 if they are equal, and > 0 if lhs is greater than rhs.
            int result = 0;
            if(left.contactName.compareTo(right.contactName)<0)
            {
                result = -1;
            }
            else if(left.contactName.compareTo(right.contactName) == 0)
            {
                result = 0;
            }
            else if(left.contactName.compareTo(right.contactName)>0)
            {
                result = 1;
            }
            return result;
        }
    }
    class GinkoConnectItemComparator implements Comparator<GinkoConnectItem> {
        public GinkoConnectItemComparator()
        {
        }

        public int compare(GinkoConnectItem left, GinkoConnectItem right) {
            //an integer < 0 if lhs is less than rhs, 0 if they are equal, and > 0 if lhs is greater than rhs.
            int result = 0;
            if(left.strConnectName.compareTo(right.strConnectName)<0)
            {
                result = -1;
            }
            else if(left.strConnectName.compareTo(right.strConnectName) == 0)
            {
                result = 0;
            }
            else if(left.strConnectName.compareTo(right.strConnectName)>0)
            {
                result = 1;
            }
            return result;
        }
    }

    class GinkoConnectItem
    {
        private String strConnectName = "";
        private String strMobileNumber = "";
        private String strEmail = "";
        private String strProfileImage = "";
        private boolean isNotInGinko = false;
        private boolean isHeaderView = false;
        private boolean isSentInvite = false;
        private String userId = "";//ginko userid
        private boolean isFromLocalAddressBook = false;

        private boolean isVisible = true;

        public GinkoConnectItem()
        {
            this.isVisible = true;
        }
    }


    class GinkoConnectItemView extends LinearLayout {
        private LayoutInflater inflater = null;
        private LinearLayout ginkoContactLayout;
        private GinkoConnectItem item;
        private NetworkImageView imgProfilePhoto;
        private ImageView imgGinkoContactIcon;
        private ImageView btnInviteContact;
        private TextView txtContactName , txtMobileNumber;

        private Context mContext;

        private ImageLoader imgLoader;

        public GinkoConnectItemView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.mContext = context;

        }
        public GinkoConnectItemView(Context context,  GinkoConnectItem _item)
        {
            super(context);
            this.mContext = context;
            item  = _item;

            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.invite_ginko_connect_item, this, true);

            ginkoContactLayout = (LinearLayout)findViewById(R.id.ginkoContactLayout);
            imgProfilePhoto = (NetworkImageView)findViewById(R.id.imgProfilePhoto);
            imgGinkoContactIcon = (ImageView)findViewById(R.id.imgGinkoContactIcon);
            btnInviteContact = (ImageView)findViewById(R.id.btnInviteContact);
            txtContactName = (TextView)findViewById(R.id.txtContactName);
            txtMobileNumber = (TextView)findViewById(R.id.txtMobileNumber);

            //Add by lee for GAD-1359
            imgGinkoContactIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isNotInGinko || item.isHeaderView) return;
                    if (item.userId.equals("")) return;
                    long userId = 0;
                    try {
                        userId = Long.valueOf(item.userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        userId = 0;
                    }
                    if (userId <= 0) return;

                    if (isProgressLeaf ==  true)
                        gotoSharingScreen(item);
                }
            });
            ///////////////////////
            btnInviteContact.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = null, phone = null;
                    if (!item.strEmail.equals(""))
                        email = item.strEmail;
                    if (!item.strMobileNumber.equals(""))
                        phone = item.strMobileNumber;

                    if(!item.strEmail.equals("") && !item.strMobileNumber.equals("")) {
                        final List<String> buttons = new ArrayList<String>();
                        buttons.add(phone);
                        buttons.add(email);
                        buttons.add("Cancel");
                        final BottomPopupWindow popupWindow = new BottomPopupWindow(mContext, buttons);
                        popupWindow.setClickListener(new BottomPopupWindow.OnButtonClickListener() {
                            @Override
                            public void onClick(View button, int position) {
                                String text = buttons.get(position);
                                if (text == "Cancel") {
                                    popupWindow.dismiss();
                                } else {
                                    item.isSentInvite = true;

                                    String content = getString(R.string.str_ginko_invites_text_prefix) +
                                            "user name: " + Uitils.getUserName(InviteGinkoConnects.this) + //Uitils.getUserFullname(InviteGinkoConnects.this) +
                                            getString(R.string.str_ginko_invites_text_suffix);
                                    if (position == 0) {
                                        sendSMS(item.isFromLocalAddressBook, text, content);
                                    } else {
                                        sendEmail(item.isFromLocalAddressBook, text, content);
                                    }
                                }
                            }
                        });
                        popupWindow.show(v);
                    } else {
                        item.isSentInvite = true;

                        String content = getString(R.string.str_ginko_invites_text_prefix) +
                                "user name: " + Uitils.getUserName(InviteGinkoConnects.this) + //Uitils.getUserFullname(InviteGinkoConnects.this) +
                                getString(R.string.str_ginko_invites_text_suffix);
                        if (phone != null && !phone.equals("")) {
                            sendSMS(item.isFromLocalAddressBook, phone, content);
                        } else {
                            sendEmail(item.isFromLocalAddressBook, email, content);
                        }
                    }
                    /*CBRequest.inviteGinkoUser(email, phone, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if(response.isSuccess())
                            {
                                Toast.makeText(mContext , item.strConnectName+" is invited to Ginko by you." , Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(mContext , "Failed to invite "+item.strConnectName+" to Ginko." , Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/

                }
            });
        }
        public void setItem(GinkoConnectItem _item)
        {
            this.item = _item;
        }
        public void refreshView()
        {
            if(imgLoader == null)
                imgLoader = MyApp.getInstance().getImageLoader();

            if(!item.isNotInGinko) {
                imgProfilePhoto.setDefaultImageResId(R.drawable.no_face);
                imgProfilePhoto.setImageUrl(item.strProfileImage, imgLoader);
            }

            if(item.isNotInGinko)
            {
                imgGinkoContactIcon.setVisibility(View.GONE);
                imgProfilePhoto.setVisibility(View.GONE);
                btnInviteContact.setVisibility(View.VISIBLE);
                if(item.isSentInvite)
                    btnInviteContact.setImageResource(R.drawable.ginko_resend_invite_button);
                else
                    btnInviteContact.setImageResource(R.drawable.ginko_invite_button);
                btnInviteContact.invalidate();
                if(item.isHeaderView && item.isNotInGinko)
                {
                    ginkoContactLayout.setBackgroundColor(getResources().getColor(R.color.headergreycolor));

                    txtContactName.setVisibility(View.GONE);
                    txtMobileNumber.setText("NOT IN GINKO");
                    btnInviteContact.setVisibility(View.GONE);
                }
                else
                {
                    ginkoContactLayout.setBackgroundColor(getResources().getColor(R.color.white));

                    txtContactName.setVisibility(View.VISIBLE);
                    if(!item.strMobileNumber.equals("") && !item.strEmail.equals(""))
                        txtMobileNumber.setText(item.strMobileNumber + "\n" + item.strEmail);
                    else if (!item.strMobileNumber.equals(""))
                        txtMobileNumber.setText(item.strMobileNumber);
                    else if (!item.strEmail.equals(""))
                        txtMobileNumber.setText(item.strEmail);

                    txtContactName.setText(item.strConnectName);
                }
            }
            else
            {
                if(item.isHeaderView)
                {
                    ginkoContactLayout.setBackgroundColor(getResources().getColor(R.color.headergreycolor));

                    imgGinkoContactIcon.setVisibility(View.GONE);
                    imgProfilePhoto.setVisibility(View.GONE);
                    txtContactName.setVisibility(View.GONE);
                    txtMobileNumber.setText("IN GINKO");
                    btnInviteContact.setVisibility(View.GONE);

                    if (item.isSentInvite)
                        imgGinkoContactIcon.setImageResource(R.drawable.ginko_connect_time);
                    else
                        imgGinkoContactIcon.setImageResource(R.drawable.ginko_connect_icon);
                }
                else {
                    ginkoContactLayout.setBackgroundColor(getResources().getColor(R.color.white));

                    if (item.isSentInvite)
                        imgGinkoContactIcon.setImageResource(R.drawable.ginko_connect_time);
                    else
                        imgGinkoContactIcon.setImageResource(R.drawable.ginko_connect_icon);

                    imgGinkoContactIcon.setVisibility(View.VISIBLE);
                    imgProfilePhoto.setVisibility(View.VISIBLE);
                    btnInviteContact.setVisibility(View.GONE);

                    txtContactName.setVisibility(View.VISIBLE);

                    if (!item.strConnectName.equals(""))
                        txtContactName.setText(item.strConnectName);
                    else
                        txtContactName.setText("Null");
                    if (!item.strEmail.equals(""))
                        txtMobileNumber.setText(item.strEmail);
                    else
                        txtMobileNumber.setText(item.strMobileNumber);
                }
            }
        }
    }

    private class GinkoConnectListAdapter extends BaseAdapter{
        private ArrayList<GinkoConnectItem> itemList;
        private Context mContext;

        public GinkoConnectListAdapter(Context context)
        {
            this.mContext = context;
            itemList = new ArrayList<GinkoConnectItem>();
        }

        public void setItemList(ArrayList<GinkoConnectItem> itemList)
        {
            this.itemList = itemList;
        }

        public void searchItems(String searchKeyword)
        {
            if(itemList == null) return;

            if(searchKeyword == null || searchKeyword.equals(""))
            {
                for(GinkoConnectItem item: itemList)
                {
                    item.isVisible = true;
                }
            }
            else {
                //for (GinkoConnectItem item : itemList) {
                for(int i = 0; i < itemList.size(); i++) {
                    GinkoConnectItem item = itemList.get(i);
                    if (item.isHeaderView) {
                        //continue;   by lee For GAD-960
                        if(findContact_Counter) {
                            itemList.get(header_id).isVisible = true;
                            findContact_Counter = false;
                            header_id = 0;
                        }
                        header_id = i;
                    }
                    if (item.strConnectName.toLowerCase().contains(searchKeyword) || item.strMobileNumber.contains(searchKeyword) || item.strEmail.contains(searchKeyword))
                    {
                        findContact_Counter = true;
                        item.isVisible = true;
                    }
                    else
                    {
                        item.isVisible = false;
                    }
                    // end of List by lee For GAD-960
                    if(i == (itemList.size() - 1) && findContact_Counter)
                    {
                        itemList.get(header_id).isVisible = true;
                        findContact_Counter = false;
                        header_id = 0;
                    }
                }
            }


            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return itemList == null?0:itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList==null?null:itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GinkoConnectItemView view = null;
            GinkoConnectItem item = (GinkoConnectItem) getItem(position);

            if (convertView == null) {
                view = new GinkoConnectItemView(mContext , item);
            } else
            {
                view = (GinkoConnectItemView)convertView;
            }

            if(item.isVisible)
            {
                view.findViewById(R.id.itemRootLayout).setVisibility(View.VISIBLE);
            }
            else
            {
                view.findViewById(R.id.itemRootLayout).setVisibility(View.GONE);
            }

            view.setItem(item);
            view.refreshView();

            return view;
        }
    }

    public class ContactChanged extends BroadcastReceiver {
        public ContactChanged() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            /*loadContactsFromLocalAddressBook(getApplicationContext());

            loadGinkoConnectAndInvitations((Integer) nPageNum);*/
            new initialize().execute();
        }
    }

    private class initialize extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressHud.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            loadContactsFromLocalAddressBook(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set title into TextView
            Log.d("post", "");
            loadGinkoConnectAndInvitations((Integer) nPageNum);
        }
    }
}
