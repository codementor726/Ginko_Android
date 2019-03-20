package com.ginko.activity.sync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.api.request.SyncRequest;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SyncImportAddressBookActivity extends MyBaseActivity implements View.OnClickListener,
                                                                            SyncGreyContactListAdapter.OnContactItemTypeSelectedListener
{
    private RelativeLayout activityRootView;
    private ImageButton btnPrev , btnConfirm;
    private ImageView btnEdit , btnClose , btnDelete;
    private ImageView imgEntityAll , imgWorkAll , imgHomeAll;
    private EditText edtSearch;
    private ImageView btnCancelSearch;
    private TextView txtTitle;
    private Button btnCancel;

    private String email;
    private String provider;
    private String oauthToken;

    private List<SyncGreyContactItem> originalContacts = new ArrayList<SyncGreyContactItem>();
    private SyncGreyContactListAdapter adapter;

    private boolean selectSomething;
    private String importType;
    private String userName;
    private String password;
    private String webmailLink;

    private int nCurrentSelectAllType = ConstValues.GREY_TYPE_NONE;

    private boolean isImportedContactHistory = true;
    private boolean isEditable = false;

    private boolean isKeyboardVisible = false;
    private String strSearchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_import_address_book);


        if (this.getIntent()!= null && this.getIntent().getExtras()!=null){
            email = this.getIntent().getExtras().getString("email");
            provider = this.getIntent().getExtras().getString("provider");
            oauthToken = this.getIntent().getExtras().getString("oauth_token");
            importType = this.getIntent().getExtras().getString("import_type");
            userName = this.getIntent().getExtras().getString("userName");
            password = this.getIntent().getExtras().getString("password");
            webmailLink = this.getIntent().getExtras().getString("webmail_link");

            isImportedContactHistory = false;
        }

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        //if(isImportedContactHistory)
        //    txtTitle.setText(getResources().getString(R.string.title_activity_import_history));
       // else
            txtTitle.setText(getResources().getString(R.string.title_activity_sync_home));

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
                        isKeyboardVisible = false;
                        edtSearch.setCursorVisible(false);
                        if(edtSearch.length() > 0)
                            btnCancelSearch.setVisibility(View.VISIBLE);
                        else
                            btnCancelSearch.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                    }
                }
            }
        });

        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnEdit = (ImageView)findViewById(R.id.btnEdit); btnEdit.setOnClickListener(this);

        imgEntityAll = (ImageView)findViewById(R.id.imgEntity_all); imgEntityAll.setOnClickListener(this);
        imgWorkAll = (ImageView)findViewById(R.id.imgWork_all); imgWorkAll.setOnClickListener(this);
        imgHomeAll = (ImageView)findViewById(R.id.imgHome_all); imgHomeAll.setOnClickListener(this);

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

        ListView list = (ListView)findViewById(R.id.list);
        adapter = new SyncGreyContactListAdapter(this);
        adapter.setOnContactItemTypeSeletedListener(this);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getListSelectable())
                {
                    adapter.toggleItem(position);

                    if (adapter.getSelectedItemCount() > 0)
                        btnDelete.setVisibility(View.VISIBLE);
                    else
                        btnDelete.setVisibility(View.GONE);
                }

            }
        });

        edtSearch = (EditText)findViewById(R.id.edtSearch);
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

        updateSelectAllButtons();

        this.retrieveContacts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //modify by lee GAD-1112
        //
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.getInstance().hideKeyboard(activityRootView);
    }

    private void hideKeyboard()
    {
        if(isKeyboardVisible)
            MyApp.getInstance().hideKeyboard(activityRootView);
    }

    private void searchItems()
    {
        String strEditText = edtSearch.getText().toString().trim();
        if(strEditText.compareTo("")!=0 && strEditText.compareTo(strSearchKeyword) != 0) {
            strSearchKeyword = strEditText.toLowerCase();
        }
        else
        {
            strSearchKeyword = "";
        }
        adapter.searchItems(strSearchKeyword);
        adapter.notifyDataSetChanged();
    }

    private void updateSelectAllButtons()
    {
        switch(nCurrentSelectAllType)
        {
            case ConstValues.GREY_TYPE_NONE:
                imgEntityAll.setImageResource(R.drawable.btnentity);
                imgWorkAll.setImageResource(R.drawable.btnwork);
                imgHomeAll.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_ENTITY:
                imgEntityAll.setImageResource(R.drawable.btnentityup);
                imgWorkAll.setImageResource(R.drawable.btnwork);
                imgHomeAll.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_WORK:
                imgEntityAll.setImageResource(R.drawable.btnentity);
                imgWorkAll.setImageResource(R.drawable.btnworkup);
                imgHomeAll.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_HOME:
                imgEntityAll.setImageResource(R.drawable.btnentity);
                imgWorkAll.setImageResource(R.drawable.btnwork);
                imgHomeAll.setImageResource(R.drawable.btnhomeup);
                break;
        }
    }

    private void checkAllSelectedType()
    {
        List<SyncGreyContactItem> items = adapter.getAll();
        int itemCount = items.size();
        if(itemCount>1)
        {
            int i = 0;
            for(i=0;i<itemCount-1;i++)
            {
                if(items.get(i).getGreyContactType() != items.get(i+1).getGreyContactType())
                {
                    nCurrentSelectAllType = ConstValues.GREY_TYPE_NONE;
                    break;
                }
            }
            if(i>=itemCount-1)
            {
                nCurrentSelectAllType = items.get(0).getGreyContactType();
            }
        }
        else if( itemCount == 1){
            nCurrentSelectAllType = items.get(0).getGreyContactType();
        }
        else if (itemCount ==0){
            nCurrentSelectAllType = ConstValues.GREY_TYPE_NONE;
        }

        updateSelectAllButtons();
    }

    private void checkIsUpdated()
    {
        if(isEditable)
            return;
        boolean isChanged = false;
        if(originalContacts.size()!=adapter.getCount())
            isChanged = true;

        for(int i=0;i<originalContacts.size();i++)
        {
            if(originalContacts.get(i).getGreyContactType() != ((SyncGreyContactItem)adapter.getItem(i)).getGreyContactType())
            {
                isChanged = true;
                break;
            }else if(originalContacts.get(i).getGreyContactType() != nCurrentSelectAllType)
            {
                isChanged = true;
                break;
            }
        }

        if(isChanged)
            btnConfirm.setVisibility(View.VISIBLE);
        else
            btnConfirm.setVisibility(View.GONE);
    }

    private void updateUIFromEditable()
    {
        if(isEditable)
        {
            btnPrev.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
            btnEdit.setVisibility(View.INVISIBLE);

            btnClose.setVisibility(View.VISIBLE);

            adapter.setListSelectable(true);
            adapter.notifyDataSetChanged();
        }
        else
        {
            btnClose.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            btnPrev.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);

            checkIsUpdated();
            adapter.setListSelectable(false);
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteSelectedContacts(String sync_contactids)
    {
        SyncRequest.deleteSyncContact(sync_contactids , new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if(response.isSuccess())
                {
                    int index = 0;
                    Set<Integer> selectedContactIds = adapter.getSelectedContactsIds();
                    while(index<originalContacts.size()) {
                        if(selectedContactIds.contains(originalContacts.get(index).getSyncContactId()))
                        {
                            originalContacts.remove(index);
                            continue;
                        }
                        index++;
                    }
                    adapter.deleteSelectedItems();
                    //Auto close edit mode
                    if(isEditable)
                    {
                        isEditable = false;
                        if(adapter != null)
                            adapter.unselectedAll();
                        updateUIFromEditable();
                    }
                }
            }
        });
    }

    private void retrieveContacts() {
        if(isImportedContactHistory)
        {
            //import grey contacts history
            SyncRequest.getImportedContactHistory(new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if(response.isSuccess())
                    {
                        originalContacts.clear();
                        JSONObject rootObj = response.getData();
                        JSONArray jsonObjectList = rootObj.optJSONArray("contacts");
                        for (int i=0;i<jsonObjectList.length();i++) {
                            JSONObject obj = null;
                            try {
                                obj = (JSONObject) jsonObjectList.get(i);

                                SyncGreyContactItem item = new SyncGreyContactItem();
                                item.setJsonString(obj.toString());

                                StringBuffer sb = new StringBuffer();
                                sb.append(obj.optString("first_name"));
                                String middleName = obj.optString("middle_name");
                                if (middleName != null && !middleName.isEmpty()) {
                                    sb.append(" ").append(middleName);
                                }
                                String lastName = obj.optString("last_name");
                                if (lastName != null && !lastName.isEmpty()) {
                                    sb.append(" ").append(lastName);
                                }
                                item.setGreyContactName(sb.toString());
                                item.setSyncContactId(obj.optInt("contact_id"));

                                item.setGreyContactType(obj.optInt("type", ConstValues.GREY_TYPE_NONE));
                                item.setGreyContactEmail(obj.optString("email", ""));
                                originalContacts.add(item);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        adapter.addAll(originalContacts);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
        else {
            isImportedContactHistory = true;
            if (StringUtils.equalsIgnoreCase(importType, "ows")) {
                SyncRequest.importByOWA(email, userName, password, webmailLink, new ResponseCallBack<List<JSONObject>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<JSONObject>> response) {
                        if (response.isSuccess()) {
                            originalContacts.clear();
                            List<JSONObject> jsonObjectList = response.getData();
                            for (JSONObject obj : jsonObjectList) {
                                SyncGreyContactItem item = new SyncGreyContactItem();
                                item.setJsonString(obj.toString());

                                StringBuffer sb = new StringBuffer();
                                sb.append(obj.optString("first_name"));
                                String middleName = obj.optString("middle_name");
                                if (middleName != null && !middleName.isEmpty()) {
                                    sb.append(" ").append(middleName);
                                }
                                String lastName = obj.optString("last_name");
                                if (lastName != null && !lastName.isEmpty()) {
                                    sb.append(" ").append(lastName);
                                }
                                item.setGreyContactName(sb.toString());
                                item.setSyncContactId(obj.optInt("contact_id"));
                                originalContacts.add(item);
                            }
                            adapter.addAll(originalContacts);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            } else {
                SyncRequest.importByOAuth(email, provider, oauthToken, new ResponseCallBack<List<JSONObject>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<JSONObject>> response) {
                        if (response.isSuccess()) {
                            originalContacts.clear();
                            List<JSONObject> jsonObjectList = response.getData();
                            for (JSONObject obj : jsonObjectList) {
                                SyncGreyContactItem item = new SyncGreyContactItem();
                                item.setJsonString(obj.toString());
                                StringBuffer sb = new StringBuffer();
                                sb.append(obj.optString("first_name"));
                                String middleName = obj.optString("middle_name");
                                if (middleName != null && !middleName.isEmpty()) {
                                    sb.append(" ").append(middleName);
                                }
                                String lastName = obj.optString("last_name");
                                if (lastName != null && !lastName.isEmpty()) {
                                    sb.append(" ").append(lastName);
                                }
                                item.setGreyContactName(sb.toString());
                                item.setSyncContactId(obj.optInt("contact_id"));
                                originalContacts.add(item);
                            }
                            adapter.addAll(originalContacts);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    private void selectAll(int contactType){
        if(nCurrentSelectAllType>ConstValues.GREY_TYPE_NONE && nCurrentSelectAllType == contactType)//if entity , home or work was already selected
        {
            nCurrentSelectAllType = ConstValues.GREY_TYPE_NONE;
            adapter.selectAllType(nCurrentSelectAllType);
            adapter.notifyDataSetChanged();
            updateSelectAllButtons();
            checkIsUpdated();
            return;
        }
        else
        {
            nCurrentSelectAllType = contactType;

        }

        /*//Add by lee for select category(for none dialog)
        adapter.selectAllType(nCurrentSelectAllType);
        adapter.notifyDataSetChanged();
        updateSelectAllButtons();*/

        showYesNoDialogForSelectALl(nCurrentSelectAllType);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard();
    }



    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                MyApp.getInstance().hideKeyboard(activityRootView);
                finish();
                break;

            case R.id.btnEdit:
                if(!isEditable && adapter.getCount() > 0) {
                    isEditable = true;
                    updateUIFromEditable();
                }
                break;

            case R.id.btnClose:
                if(isEditable)
                {
                    isEditable = false;
                    if(adapter != null)
                        adapter.unselectedAll();
                    updateUIFromEditable();
                }
                break;

            case R.id.btnDelete:
                final Set<Integer> selectedContactIds = adapter.getSelectedContactsIds();
                if(selectedContactIds.size()>0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Confirm");
                    builder.setMessage(getResources().getString(R.string.str_confirm_dialog_import_contact_delete_selected_fields));
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO
                            String strSelectedSyncContactIds = "";
                            Iterator<Integer> iterator = selectedContactIds.iterator();
                            while(iterator.hasNext())
                            {
                                strSelectedSyncContactIds += String.valueOf(iterator.next()) +",";
                            }
                            if(strSelectedSyncContactIds.length() > 1) {
                                strSelectedSyncContactIds = strSelectedSyncContactIds.substring(0 , strSelectedSyncContactIds.length()-1);
                                deleteSelectedContacts(strSelectedSyncContactIds);
                            }

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
                }
                break;

            case R.id.btnConfirm:
                JSONObject data = new JSONObject();
                JSONArray arr = new JSONArray();
                try {
                    List<SyncGreyContactItem> items = adapter.getAll();
                    for(int i = 0; i<items.size();i++){
                        SyncGreyContactItem item = items.get(i);
                        if(item.getGreyContactType() != ConstValues.GREY_TYPE_NONE) {
                            JSONObject t = new JSONObject();
                            t.put("sync_contact_id", item.getSyncContactId());
                            t.put("type", String.valueOf(item.getGreyContactType()));

                            arr.put(t);
                        }
                    }

                    data.put("data",arr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SyncRequest.saveGreyContact(data, new ResponseCallBack<Void>(){

                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        /*if (response.isSuccess()){
                            SyncImportAddressBookActivity.this.finish();
                        }else{
                            MyApp.getInstance().showSimpleAlertDiloag(SyncImportAddressBookActivity.this, "Failed save Grey Contacts.", null);
                        }*/
                        SyncImportAddressBookActivity.this.finish();
                    }
                });
                break;

            case R.id.imgEntity_all:
                selectAll(ConstValues.GREY_TYPE_ENTITY);
                //checkIsUpdated();
                break;

            case R.id.imgWork_all:
                selectAll(ConstValues.GREY_TYPE_WORK);
                //checkIsUpdated();
                break;

            case R.id.imgHome_all:
                selectAll(ConstValues.GREY_TYPE_HOME);
                //checkIsUpdated();
                break;

        }
    }

    private void showYesNoDialogForSelectALl(int selectAllType)
    {
        final int nSelectType = selectAllType;
        String message = null;
        switch(nSelectType)
        {
            case ConstValues.GREY_TYPE_ENTITY:
                message = getResources().getString(R.string.str_confirm_dialog_grey_contact_select_entity);
                break;
            case ConstValues.GREY_TYPE_WORK:
                message = getResources().getString(R.string.str_confirm_dialog_grey_contact_select_work);
                break;
            case ConstValues.GREY_TYPE_HOME:
                message = getResources().getString(R.string.str_confirm_dialog_grey_contact_select_home);
                break;
            case ConstValues.GREY_TYPE_NONE:
                return;

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                adapter.selectAllType(nCurrentSelectAllType);
                adapter.notifyDataSetChanged();
                updateSelectAllButtons();
                checkIsUpdated();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
                nCurrentSelectAllType = ConstValues.GREY_TYPE_NONE;
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void OnContactItemTypeChanged(int position, int contactType) {
        if(adapter != null)
        {
            adapter.notifyDataSetChanged();
            checkIsUpdated();
            checkAllSelectedType();
        }
    }

    @Override
    public void OnCheckBoxselected(int position) {
        if (adapter != null && isEditable) {
            adapter.toggleItem(position);
            if (adapter.getSelectedItemCount() > 0)
                btnDelete.setVisibility(View.VISIBLE);
            else
                btnDelete.setVisibility(View.GONE);
        }
    }
}
