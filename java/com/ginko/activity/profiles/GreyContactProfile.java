package com.ginko.activity.profiles;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.api.request.GreyContactRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreyContactProfile extends MyBaseActivity implements View.OnClickListener , CustomSizeMeasureView.OnMeasureListner,
                                                                ImInputEditTExt.OnEditTextKeyDownListener
{

    private ImageButton btnPrev , btnConfirm;
    private ImageView btnClose;
    private ImageView btnAddInfoPopupClose , btnAddInfoConfirm;
    private NetworkImageView imgContactPhoto;
    private LinearLayout infoListLayout;
    private EditText edtFullName;
    private TextView txtFirstName , txtLastName;
    private ImageView imgEntity , imgWork , imgHome, imgFavorite;
    private ImageView imgBtnDelete , imgBtnNote , imgBtnAddInfoItem , imgEditContact;

    private ImageButton btnAddInfoItemPopupClose , btnAddInfoItemPopupConfirm;
    private ImageButton btnNotePopupClose , btnNotePopupConfirm;

    private PopupWindow addInfoPopupWindow = null, notePopupWindow = null;
    private View addInfoPopupView = null , notePopupView = null;

    private ListView addInfoListView;
    private RelativeLayout rootLayout;
    private LinearLayout popupRootLayout;

    private CustomSizeMeasureView sizeMeasureView;

    /* Variables */
    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;

    private Uri uri;
    private String tempPhotoUriPath = "";
    private String strUploadPhotoPath = "";

    private ImageLoader imgLoader;

    private boolean isEditable = false;
    private boolean isChanged = false;
    private boolean isPhotoChanged = false;
    private boolean isSelected = false;

    private final String[] strInfoNames = {
            "Company",
            "Mobile",
            "Mobile#2",
            "Mobile#3",
            "Phone",
            "Phone#2",
            "Phone#3",
            "Email",
            "Email#2",
            "Address",
            "Address#2",
            "Fax",
            "Birthday",
            "Facebook",
            "Twitter",
            "LinkedIn",
            "Website",
            "Custom",
            "Custom#2",
            "Custom#3",
    };
    private final String[] strFiledTypeNames = {
            ConstValues.PROFILE_FIELD_TYPE_COMPANY,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_MOBILE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_PHONE,
            ConstValues.PROFILE_FIELD_TYPE_EMAIL,
            ConstValues.PROFILE_FIELD_TYPE_EMAIL,
            ConstValues.PROFILE_FIELD_TYPE_ADDRESS,
            ConstValues.PROFILE_FIELD_TYPE_ADDRESS,
            ConstValues.PROFILE_FIELD_TYPE_FAX,
            ConstValues.PROFILE_FIELD_TYPE_DATE,
            ConstValues.PROFILE_FIELD_TYPE_FACEBOOK,
            ConstValues.PROFILE_FIELD_TYPE_TWITTER,
            ConstValues.PROFILE_FIELD_TYPE_LINKEDIN,
            ConstValues.PROFILE_FIELD_TYPE_WEBSITE,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
            ConstValues.PROFILE_FIELD_TYPE_CUSTOM,
    };

    private HashMap<String , Integer> infoNameMap;

    private ArrayList<InfoItem> infoList;
    private ArrayList<InfoItemView> infoItemViews;
    private AddInfoListAdapter addInfoListAdapter;

    private final int TYPE_ENTITY = 0;
    private final int TYPE_HOME = 1;
    private final int TYPE_WORK = 2;
    private final int TYPE_FAVORITE = 3;

    private int nType = TYPE_ENTITY;/* entity , work , home */
    private int nSelectingType = TYPE_ENTITY;/* entity , work , home */

    private int activityHeight = 0;
    private CustomDialog mCustomDialog;
    /* Contact Item Variables */
    int contactId;
    private String strFirstName = "" , strLastName = "";
    private String strNotes = "";
    private String strPhotoUrl = "" , strTempPhotoPath = "" , strNewUploadPhotoUrl = "";
    private String strEmail = "";
    private List<String> phones;

    TextWatcher edtTextWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            if(!isChanged)
            {
                isChanged = true;
                refreshUiTopMenuButtons();
            }
        }
    };

    private EmailValidationCheckRunnable emailCheckerThread;
    private Pattern pattern;
    private Handler mHandler = new Handler();

    private View.OnFocusChangeListener emailFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus == false)
            {
                EditText edtEmail = (EditText)v;
                if(emailCheckerThread == null) {
                    emailCheckerThread = new EmailValidationCheckRunnable(GreyContactProfile.this , edtEmail.getText().toString());
                }
                else {
                    mHandler.removeCallbacks(emailCheckerThread);
                }
                emailCheckerThread.setEmailString(edtEmail.getText().toString());
                mHandler.postDelayed(emailCheckerThread, 500);
            }
        }
    };

    private int emailInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grey_contact_profile);

        this.isEditable = false;
        this.isChanged = false;
        this.isPhotoChanged = false;

        phones = new ArrayList<String>();

        imgLoader = MyApp.getInstance().getImageLoader();

        infoNameMap = new HashMap<String , Integer>();
        infoList = new ArrayList<InfoItem>();
        infoItemViews = new ArrayList<InfoItemView>();
        for(int i=0;i<strInfoNames.length;i++)
        {
            infoNameMap.put(strInfoNames[i] , new Integer(i));
            infoList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] ,""));
        }
        getUIObjects();
        Intent intent = this.getIntent();
        parseJSONObject(intent.getStringExtra("jsonvalue"));
    }

    private View.OnClickListener btnCloseClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mCustomDialog != null) {
                hideKeyboard(mCustomDialog.getEdtNotes());
                mCustomDialog.bIsKeyBoardVisibled = false;
                mCustomDialog.dismiss();

            }
        }
    };
    private View.OnClickListener btnConfirmClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mCustomDialog != null)
            {
                strNotes = mCustomDialog.getEdtNotes().getText().toString().trim();
                updateContactInfo(true);
                hideKeyboard(mCustomDialog.getEdtNotes());
                mCustomDialog.bIsKeyBoardVisibled = false;
                mCustomDialog.dismiss();
            }
        }
    };
    private void parseJSONObject(String object)
    {
        Log.e("GreyContactProfile" , object);
        try {
            JSONObject jsonObject = new JSONObject(object);
            try
            {
                contactId = jsonObject.getInt("contact_id");
            }catch(Exception e){
                e.printStackTrace();
                contactId = 0;
            }

            try {
                strPhotoUrl = jsonObject.getString("photo_url");
            }catch(Exception e){strPhotoUrl = "";}
            try {
                strFirstName = jsonObject.getString("first_name");
            }catch(Exception e){strFirstName = "";}

            try {
                strLastName = jsonObject.getString("last_name");
            }catch(Exception e){strLastName = "";}
            try
            {
                strEmail = jsonObject.getString("email");
            }catch(Exception e){strEmail = "";}
            try
            {
                JSONArray phoneArray = jsonObject.optJSONArray("phones");
                phones.clear();
                for(int i=0;i<phoneArray.length();i++) {
                    phones.add(phoneArray.get(i).toString());
                }
            }catch(Exception e){phones.clear();}

            try
            {
                nType = jsonObject.getInt("type");
                isSelected = jsonObject.getBoolean("is_favorite");
                nSelectingType = nType;
            }catch(Exception e)
            {
                nType = TYPE_ENTITY;
                nSelectingType = TYPE_ENTITY;
            }
            try
            {
                JSONArray fieldsArray = jsonObject.optJSONArray("fields");
                for(int i=0;i<fieldsArray.length();i++)
                {
                    JSONObject fieldObject = fieldsArray.getJSONObject(i);
                    int infoNameIndex = -1;
                    try
                    {
                        int fieldId = fieldObject.optInt("id" , -1);

                        String strFieldName = fieldObject.getString("field_name");
                        infoNameIndex = infoNameMap.get(strFieldName);
                        String strFieldValue = fieldObject.getString("field_value");
                        infoList.get(infoNameIndex).strInfoValue = strFieldValue;
                        infoList.get(infoNameIndex).nFieldId = fieldId;
                        if(infoList.get(infoNameIndex).strInfoValue.equals(""))
                            infoList.get(infoNameIndex).setVisibility(false);
                        else
                            infoList.get(infoNameIndex).setVisibility(true);
                    }catch(Exception e){
                        e.printStackTrace();
                        if(infoNameIndex>=0)
                        {
                            infoList.get(infoNameIndex).strInfoValue = "";
                            infoList.get(infoNameIndex).setVisibility(false);
                        }
                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                strNotes = jsonObject.getString("notes");
            }catch(Exception e)
            {
                strNotes = "";
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);

        btnPrev =  (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm =  (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnClose  =  (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);

        edtFullName = (EditText)findViewById(R.id.edtFullName);
        txtFirstName = (TextView)findViewById(R.id.txtFirstName);
        txtLastName = (TextView)findViewById(R.id.txtLastName);

        edtFullName.addTextChangedListener(edtTextWatcher);

        EditText edtEmail = (EditText)findViewById(R.id.txtEmail);

        emailInputType = edtEmail.getInputType();

        imgEntity = (ImageView)findViewById(R.id.imgEntity); imgEntity.setOnClickListener(this);
        imgWork = (ImageView)findViewById(R.id.imgWork); imgWork.setOnClickListener(this);
        imgHome = (ImageView)findViewById(R.id.imgHome); imgHome.setOnClickListener(this);
        imgFavorite = (ImageView)findViewById(R.id.imgFavorite); imgFavorite.setOnClickListener(this);
        imgBtnDelete = (ImageView)findViewById(R.id.imgBtnDelete); imgBtnDelete.setOnClickListener(this);
        imgBtnNote = (ImageView)findViewById(R.id.imgBtnNote); imgBtnNote.setOnClickListener(this);
        imgBtnAddInfoItem = (ImageView)findViewById(R.id.imgBtnAddInfoItem); imgBtnAddInfoItem.setOnClickListener(this);

        imgEditContact = (ImageView)findViewById(R.id.imgEditContact); imgEditContact.setOnClickListener(this);


        imgContactPhoto = (NetworkImageView)findViewById(R.id.imgContactPhoto); imgContactPhoto.setOnClickListener(this);
        imgContactPhoto.setDefaultImageResId(R.drawable.im_default_contact_photo);

        infoListLayout = (LinearLayout)findViewById(R.id.infoListLayout);
        for(int i=0;i<strInfoNames.length;i++)
        {
            infoItemViews.add(new InfoItemView(this , infoList.get(i)));
            infoListLayout.addView(infoItemViews.get(i));
        }

        addInfoPopupView = getLayoutInflater().inflate(R.layout.grey_contact_profile_add_info_popup, null);
        btnAddInfoItemPopupClose = (ImageButton)addInfoPopupView.findViewById(R.id.btnAddInfoPopupClose);btnAddInfoItemPopupClose.setOnClickListener(this);
        btnAddInfoItemPopupConfirm = (ImageButton)addInfoPopupView.findViewById(R.id.btnAddInfoPopupConfirm);btnAddInfoItemPopupConfirm.setOnClickListener(this);
        addInfoListView = (ListView)addInfoPopupView.findViewById(R.id.infoList);


        notePopupView = getLayoutInflater().inflate(R.layout.contact_profile_add_comment_popup, null);
        btnNotePopupClose = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupClose);btnNotePopupClose.setOnClickListener(this);
        btnNotePopupConfirm = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupConfirm);btnNotePopupConfirm.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        showContactInfo();
        updateUIFromEditable();
        refreshUiTopMenuButtons();
        updateInfoListViews(false);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void enableAddInfoItemPopup()
    {
        // Creating a pop window for emoticons keyboard
        addInfoPopupWindow = new PopupWindow(addInfoPopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) activityHeight, false);
        addInfoPopupWindow.setFocusable(true);
        addInfoPopupWindow.setAnimationStyle(R.style.AnimationPopup);
        addInfoPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }
    private void showHidePopupView(PopupWindow popupWindow , boolean bShown)
    {
        if(popupWindow == null) return;
        if (!popupWindow.isShowing()) {
            popupWindow.setHeight((int) (activityHeight));
            if (bShown) {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            } else {
                popupRootLayout.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.showAtLocation(popupRootLayout, Gravity.BOTTOM, 0, 0);
            if(popupWindow == addInfoPopupWindow)
            {
                //list to show for adding empty item infos
                ArrayList<AddInfoItem> itemList = new ArrayList<AddInfoItem>();
                for(int i=0;i<infoList.size();i++)
                {
                    if(infoList.get(i).strInfoValue.equals("") && !infoList.get(i).isVisible)
                        itemList.add(new AddInfoItem(infoList.get(i).strInfoName));
                }
                addInfoListAdapter = new AddInfoListAdapter(GreyContactProfile.this , itemList);
                addInfoListView.setAdapter(addInfoListAdapter);
                btnAddInfoItemPopupConfirm.setVisibility(View.GONE);
            }
        }
        else
        {
            if (bShown) {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            } else {
                popupRootLayout.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.dismiss();
        }
    }

    private void showContactInfo()
    {
        if(!strPhotoUrl.equals("") && strTempPhotoPath.equals(""))
            imgContactPhoto.setImageUrl(strPhotoUrl , imgLoader);

        String fullname = strFirstName;
        txtFirstName.setText(fullname);
        txtLastName.setText(strLastName);
        fullname += " "+ strLastName;
        edtFullName.removeTextChangedListener(edtTextWatcher);
        edtFullName.setText(fullname);
        edtFullName.addTextChangedListener(edtTextWatcher);
    }

    private void updateUIFromEditable() {

        if (isEditable) {
            edtFullName.setVisibility(View.VISIBLE);
            txtFirstName.setVisibility(View.GONE);
            txtLastName.setVisibility(View.GONE);

            imgEditContact.setVisibility(View.GONE);

            imgBtnNote.setVisibility(View.GONE);
            imgBtnAddInfoItem.setVisibility(View.VISIBLE);
            imgBtnDelete.setVisibility(View.INVISIBLE);

            updateTypeIcons(nSelectingType);
            updateFavoriteButton(isSelected);
        }
        else {
            edtFullName.setVisibility(View.GONE);
            txtFirstName.setVisibility(View.VISIBLE);
            txtLastName.setVisibility(View.VISIBLE);

            imgEditContact.setVisibility(View.VISIBLE);

            imgBtnNote.setVisibility(View.VISIBLE);
            imgBtnAddInfoItem.setVisibility(View.GONE);
            imgBtnDelete.setVisibility(View.VISIBLE);

            updateTypeIcons(nType);
            updateFavoriteButton(isSelected);
        }
    }

    private void updateFavoriteButton(boolean selected)
    {
        if(selected) {
            UserRequest.setFavoriteContacts(contactId, "2", new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    imgFavorite.setImageResource(R.drawable.img_contact_favorite);
                }
            });
        }
        else {
            UserRequest.unsetFavoriteContacts(contactId, "2", new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    imgFavorite.setImageResource(R.drawable.img_unfavorite);
                }
            });
        }
    }

    private void updateTypeIcons(int type)
    {
        switch(type)
        {
            case TYPE_ENTITY:
                imgEntity.setImageResource(R.drawable.btnentityup);
                imgWork.setImageResource(R.drawable.btnwork);
                imgHome.setImageResource(R.drawable.btnhome);
                break;
            case TYPE_WORK:
                imgEntity.setImageResource(R.drawable.btnentity);
                imgWork.setImageResource(R.drawable.btnworkup);
                imgHome.setImageResource(R.drawable.btnhome);
                break;
            case TYPE_HOME:
                imgEntity.setImageResource(R.drawable.btnentity);
                imgWork.setImageResource(R.drawable.btnwork);
                imgHome.setImageResource(R.drawable.btnhomeup);
                break;
        }
    }


    private void updateInfoListViews(boolean isReset)
    {
        for(int i=0;i<strInfoNames.length;i++)
        {
            if(!infoList.get(i).isVisible) {
                infoItemViews.get(i).setVisibility(View.GONE);
                continue;
            }
            else
                infoItemViews.get(i).setVisibility(View.VISIBLE);
            if(isReset)
                infoItemViews.get(i).resetValues();
            infoItemViews.get(i).refreshView();

        }
    }


    private void refreshUiTopMenuButtons()
    {
        if(isEditable)
        {
            btnPrev.setVisibility(View.GONE);
            btnClose.setVisibility(View.VISIBLE);
            if(isChanged)
                btnConfirm.setVisibility(View.VISIBLE);
            else
                btnConfirm.setVisibility(View.VISIBLE);
        }
        else
        {
            btnPrev.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
        }
    }

    private void cancelEditing()
    {
        isEditable = false;
        isChanged = false;

        String strfullname = strFirstName;
        txtFirstName.setText(strfullname);
        txtLastName.setText(strLastName);
        strfullname += " "+ strLastName;
        edtFullName.removeTextChangedListener(edtTextWatcher);
        edtFullName.setText(strfullname);
        edtFullName.addTextChangedListener(edtTextWatcher);

        if (isPhotoChanged == true)
        {
            imgContactPhoto.refreshOriginalBitmap();
            imgContactPhoto.setImageUrl(strPhotoUrl, imgLoader);
            imgContactPhoto.invalidate();
            strTempPhotoPath = "";
            isPhotoChanged = false;
        }

        nSelectingType = nType;

        for(int i=0;i<strInfoNames.length;i++)
        {
            infoList.get(i).isSelected = false;
            if(infoList.get(i).strInfoValue.equals("")) {
                infoList.get(i).isVisible = false;
                infoItemViews.get(i).setVisibility(View.GONE);
            }
            else
            {
                infoList.get(i).isVisible = true;
                infoItemViews.get(i).setVisibility(View.VISIBLE);
            }
        }

        updateUIFromEditable();
        refreshUiTopMenuButtons();
        updateInfoListViews(true);
        showContactInfo();

        MyApp.getInstance().hideKeyboard(rootLayout);
    }

    private int getSelectedItemCounts()
    {
        int selectedItemCounts = 0;
        int size = infoItemViews.size();
        for(int i=0;i<size;i++)
        {
            InfoItem item = infoItemViews.get(i).getItem();
            if(item.isSelected)
                selectedItemCounts++;
        }
        return selectedItemCounts;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            //close the contact editable state
            case R.id.btnClose:
                cancelEditing();
                break;

            //confirm to save the changed contact info
            case R.id.btnConfirm:
                //confirm dialog to update this contact info
                DialogInterface.OnClickListener updateConfirmDialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                dialog.dismiss();
                                updateContactInfo(false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder updateConfirmDialogBuilder = new AlertDialog.Builder(GreyContactProfile.this);
                updateConfirmDialogBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_make_changes_to_this_contact_info))
                        .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), updateConfirmDialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), updateConfirmDialogClickListener).show();

                break;

            //edit contact info
            case R.id.imgEditContact:
                isEditable = true;
                isChanged = false;
                nSelectingType = nType;
                for(int i=0;i<infoList.size();i++)
                {
                    infoList.get(i).isSelected = false;
                }
                String fullname = strFirstName;
                txtFirstName.setText(fullname);
                txtLastName.setText(strLastName);
                fullname += " "+ strLastName;
                edtFullName.removeTextChangedListener(edtTextWatcher);
                edtFullName.setText(fullname);
                edtFullName.addTextChangedListener(edtTextWatcher);

                updateUIFromEditable();
                refreshUiTopMenuButtons();
                updateInfoListViews(true);
                break;

            //select entity type
            case R.id.imgEntity:
                if(!isEditable) return;
                nSelectingType = TYPE_ENTITY;
                if(nSelectingType != nType)//if new selected type is different from original type, then update changed flag
                {
                    isChanged = true;
                    refreshUiTopMenuButtons();
                }
                updateTypeIcons(nSelectingType);
                break;
            //select entity work
            case R.id.imgWork:
                if(!isEditable) return;
                nSelectingType = TYPE_WORK;
                if(nSelectingType != nType)//if new selected type is different from original type, then update changed flag
                {
                    isChanged = true;
                    refreshUiTopMenuButtons();
                }
                updateTypeIcons(nSelectingType);

                break;
            //select entity home
            case R.id.imgHome:
                if(!isEditable) return;
                nSelectingType = TYPE_HOME;
                if(nSelectingType != nType)//if new selected type is different from original type, then update changed flag
                {
                    isChanged = true;
                    refreshUiTopMenuButtons();
                }
                updateTypeIcons(nSelectingType);
                break;
            //select favorite
            case R.id.imgFavorite:
                //if(!isEditable) return;
                /*nSelectingType = TYPE_FAVORITE;
                if(nSelectingType != nType)
                {
                    isChanged = true;
                    refreshUiTopMenuButtons();
                }*/
                if(!isSelected) {
                    isSelected = true;
                    updateFavoriteButton(isSelected);
                }
                else {
                    isSelected = false;
                    updateFavoriteButton(isSelected);
                }
                break;
            //delete
            case R.id.imgBtnDelete:
                if(isEditable)
                {
                    int selectedItemCounts = getSelectedItemCounts();
                    if(selectedItemCounts == 0)
                    {
                        //alert dialog to notify should input at least one item info
                        MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this , R.string.str_alert_for_grey_contact_select_item_to_delete , null);
                        return;
                    }
                    else
                    {
                        //confirm dialog to delete this contact forever
                        DialogInterface.OnClickListener deleteInfoConfirmDialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        dialog.dismiss();
                                        //deleteSelectedInfo();
                                        removeInfoListView();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder deleteInfoConfrimDialogBuilder = new AlertDialog.Builder(GreyContactProfile.this);
                        deleteInfoConfrimDialogBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_grey_contact_delete_selected_fields))
                                .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), deleteInfoConfirmDialogClickListener)
                                .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), deleteInfoConfirmDialogClickListener).show();
                    }
                }
                else
                {
                    //confirm dialog to delete this contact forever
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    dialog.dismiss();
                                    SyncRequest.removeGreyContact(String.valueOf(contactId) , new ResponseCallBack<Void>(){
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if(response.isSuccess())
                                            {
                                                finish();
                                            }
                                            else
                                            {
                                                MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this , R.string.str_err_fail_to_delete_grey_contact , null);
                                            }
                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(GreyContactProfile.this);
                    builder.setMessage(getResources().getString(R.string.str_delete_grey_contact_forever_confirm_dialog_title))
                            .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), dialogClickListener)
                            .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), dialogClickListener).show();
                }
                break;

            //take photo to contact photo
            case R.id.imgContactPhoto:
                if(isEditable)
                {
                    if(strPhotoUrl.equals("") || strPhotoUrl.contains("greyblank.png"))    //there isn't user's photo.
                    {
                        final CharSequence[] items = { getResources().getString(R.string.str_grey_contact_take_photo_from_camera),
                                getResources().getString(R.string.str_grey_contact_take_photo_from_gallery),
                                getResources().getString(R.string.str_grey_contact_take_photo_dialog_cancel),};

                        AlertDialog.Builder builder = new AlertDialog.Builder(GreyContactProfile.this);
                        builder.setTitle(getResources().getString(R.string.str_grey_contact_take_photo_dialog_title));
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {//take photo from camera
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("Contacts") +
                                            String.valueOf(System.currentTimeMillis()) + ".jpg"));
                                    tempPhotoUriPath = uri.getPath();
                                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                                    GreyContactProfile.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
                                    //dialog.dismiss();
                                } else if (item == 1) {//take photo from gallery
                                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    i.setType("image/*");
                                    startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                                    dialog.dismiss();
                                }
                                else//cancel
                                {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                    }
                    else  //there is an user's photo
                    {
                        final CharSequence[] items = {getResources().getString(R.string.str_grey_contact_take_photo_from_camera),
                                getResources().getString(R.string.str_grey_contact_take_photo_from_gallery),
                                getResources().getString(R.string.str_grey_contact_remove_photo),
                                getResources().getString(R.string.str_grey_contact_take_photo_dialog_cancel),};

                        AlertDialog.Builder builder = new AlertDialog.Builder(GreyContactProfile.this);
                        builder.setTitle(getResources().getString(R.string.str_grey_contact_take_photo_dialog_title));
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {//take photo from camera
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("Contacts") +
                                            String.valueOf(System.currentTimeMillis()) + ".jpg"));
                                    tempPhotoUriPath = uri.getPath();
                                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                                    GreyContactProfile.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
                                    //dialog.dismiss();
                                } else if (item == 1) {//take photo from gallery
                                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    i.setType("image/*");
                                    startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                                    dialog.dismiss();
                                } else if (item == 2)//remove photo
                                {
                                    dialog.dismiss();
                                    if (strPhotoUrl.equals("")) {
                                        MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this, R.string.str_grey_contact_remove_photo_alert, null);
                                    } else {
                                        GreyContactRequest.removePhoto(contactId, new ResponseCallBack<Void>() {
                                            @Override
                                            public void onCompleted(JsonResponse<Void> response) {
                                                if (response.isSuccess()) {
                                                    strPhotoUrl = "";
                                                    imgContactPhoto.refreshOriginalBitmap();
                                                    imgContactPhoto.setImageUrl("", imgLoader);
                                                    imgContactPhoto.invalidate();
                                                } else {
                                                    MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this, "Failed to remove photo", null);
                                                }
                                            }
                                        });
                                    }
                                } else//cancel
                                {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builder.show();
                    }
                }

                break;
            //add info item
            case R.id.imgBtnAddInfoItem:
                showHidePopupView(addInfoPopupWindow, true);
                break;
            //close add info popup window
            case R.id.btnAddInfoPopupClose:
                showHidePopupView(addInfoPopupWindow, false);
                break;
            //add info item
            case R.id.btnAddInfoPopupConfirm:
                showHidePopupView(addInfoPopupWindow, false);
                for(int i=0;i<addInfoListAdapter.getCount();i++)
                {
                    AddInfoItem item = (AddInfoItem) addInfoListAdapter.getItem(i);
                    if(item.isSelected)
                    {
                        int infoItemIndex = infoNameMap.get(item.strInfoName);
                        infoList.get(infoItemIndex).setVisibility(true);
                        infoList.get(infoItemIndex).isSelected = false;
                    }
                }
                updateInfoListViews(false);
                break;

            //add note
            case R.id.imgBtnNote:
                //showHidePopupView(notePopupWindow , true);
                mCustomDialog = new CustomDialog(this, btnCloseClickListener, btnConfirmClickListener);
                mCustomDialog.show();
                break;

        }
    }

    private void removeInfoListView()
    {
        if(strInfoNames.length < 0 || infoList.isEmpty())
            return;

        for(int i = 0;i<strInfoNames.length;i++)
        {
            if(!infoList.get(i).isSelected) continue;

            //infoList.get(i).setInfoValue("");
            infoList.get(i).setVisibility(false);
            infoList.get(i).isSelected = false;
            infoItemViews.get(i).setVisibility(View.GONE);
            infoItemViews.get(i).resetValues();
            infoItemViews.get(i).refreshView();
        }
    }

    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    //press back key on note edittext to hide the keyboard and note popup window
    @Override
    public void onImEditTextBackKeyDown() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (addInfoPopupWindow.isShowing() && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            showHidePopupView(addInfoPopupWindow, false);
            return false;
        }
        else if(isEditable && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            cancelEditing();
            return false;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
    //when activity is created and the size of activity is measured
    @Override
    public void onViewSizeMeasure(int width, int height) {
        //get acitivty height
        activityHeight = height;
        System.out.println("----Activity Height = "+String.valueOf(height)+"-----");

        if(addInfoPopupWindow == null)
            enableAddInfoItemPopup();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApp.getInstance().setCurrentActivity(this);
        if (resultCode == RESULT_OK) {
            switch(requestCode)
            {
                case TAKE_PHOTO_FROM_CAMERA:
                    strTempPhotoPath = tempPhotoUriPath;// uri.getPath();
                    if(!strTempPhotoPath.contains("file://"))
                        strTempPhotoPath = "file://"+strTempPhotoPath;

                    System.out.println("-----Photo Path= " + strTempPhotoPath + "----");
                    setGreyContactPhoto();
                    break;
                case TAKE_PHOTO_FROM_GALLERY:
                    if(data!=null)
                    {
                        uri = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();
                        strTempPhotoPath = picturePath;
                        if(!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        System.out.println("-----Photo Path= " + strTempPhotoPath + "----");
                        setGreyContactPhoto();
                        isChanged = true;
                    }
                    break;
            }
        }
        else
        {
            strTempPhotoPath = "";
            imgContactPhoto.setImageUrl(strPhotoUrl , imgLoader);
            imgContactPhoto.invalidate();
        }
    }
    //upload photo for grey contact
    private void setGreyContactPhoto()
    {
        File photoFile = null;
        try {
            photoFile = new File(new URI(strTempPhotoPath.replaceAll(" ", "%20")));

            if(!photoFile.exists())
                return;

            String zippedPhotoFile = ImageScalingUtilities.decodeFile(photoFile.getAbsolutePath(), 300, 300);
            if(zippedPhotoFile.equalsIgnoreCase(""))
                return;

            photoFile = new File(zippedPhotoFile);
            strUploadPhotoPath = photoFile.getAbsolutePath();

            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {
                imgContactPhoto.refreshOriginalBitmap();
                imgContactPhoto.setImageUrl(strTempPhotoPath, imgLoader);
                imgContactPhoto.invalidate();
                isPhotoChanged = true;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    ResponseCallBack<Void> updateContactInfoCallback = new ResponseCallBack<Void>() {
        @Override
        public void onCompleted(JsonResponse<Void> response) {
            if(response.isSuccess())
            {
                isChanged = false;
                if(isEditable) {
                    String fullname = edtFullName.getText().toString().trim();
                    if(fullname.contains(" "))
                    {
                        int spaceIndex = fullname.lastIndexOf(" ");
                        strFirstName = fullname.substring(0 , spaceIndex);
                        strLastName = fullname.substring(spaceIndex+1);
                    }
                    else {
                        strFirstName =  fullname;
                        strLastName = "";
                    }
                    int size = infoItemViews.size();
                    String email = null;

                    phones.clear();
                    for(int i=0;i<size; i++)
                    {
                        InfoItem item = infoItemViews.get(i).getItem();
                        item.strInfoValue = infoItemViews.get(i).edtInfoItem.getText().toString().trim();
                        if(item.strInfoValue.equals("")) {
                            item.isVisible = false;
                        }
                        else {
                            item.isVisible = true;
                            if(email==null && item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL))
                                email = item.strInfoValue;
                            if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_PHONE))
                                phones.add(item.strInfoValue);
                        }
                    }
                    strEmail = email==null?"":email;;
                    nType = nSelectingType;
                }
                else
                {
                    strNotes = mCustomDialog.getEdtNotes().getText().toString();
                }

                isEditable = false;
                for(int i=0;i<infoList.size();i++)
                {
                    infoList.get(i).isSelected = false;
                }

                showContactInfo();
                updateUIFromEditable();
                updateInfoListViews(true);
                refreshUiTopMenuButtons();

            }
        }
    };
    private void updateContactInfo(boolean updateNotes)
    {

        final JSONObject jsonData= new JSONObject();
        JSONArray filedsArray = new JSONArray();
        try {
            jsonData.put("contact_id", contactId);
            if(updateNotes)
            {
                jsonData.put("notes", mCustomDialog.getEdtNotes().getText().toString());
                jsonData.put("type" , nType);
                jsonData.put("first_name" , strFirstName);
                jsonData.put("last_name" , strLastName);
                jsonData.put("email" , strEmail);

                int size = infoList.size();
                for(int i=0;i<size; i++)
                {
                    InfoItem item = infoList.get(i);
                    JSONObject filedObj = new JSONObject();
                    if(item.nFieldId >= 0)
                    {
                        filedObj.put("id", item.nFieldId);
                    }
                    filedObj.put("field_name" , item.strInfoName);
                    filedObj.put("field_value" , item.strInfoValue);
                    filedObj.put("field_type" , item.strFieldType);
                    filedsArray.put(filedObj);
                }

                ContactStruct contactStruct = MyApp.getInstance().getContactsModel().getContactById(contactId);
                contactStruct.setFirstName(strFirstName);
                contactStruct.setLastName(strLastName);
                contactStruct.setJsonValue(jsonData.toString());

                // Update Db Data with Updated Contact Item and refresh Item List
                MyApp.getInstance().getContactsModel().update(contactStruct);
                MyApp.getInstance().getAllContactItemsFromDatabase();

                //call request for update contact info
                SyncRequest.updateDetail(jsonData, updateContactInfoCallback);

            }
            else//if this is not updaing the "Notes" , then update the contact info from editTexts
            {
                //if name is empty then show alert
                if(edtFullName.getText().toString().trim().equals("")) {
                    //alert dialog to notify should input at least one item info
                    MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this , R.string.str_alert_for_input_name_item , null);
                    return;
                }
                int validValueItemCounts= 0;
                String email = "";
                for(int i=0;i<infoItemViews.size();i++)
                {
                    if(!infoItemViews.get(i).edtInfoItem.getText().toString().trim().equals("")) {
                        validValueItemCounts++;

                        if(infoItemViews.get(i).getItem().strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL) && !isEmailValid(infoItemViews.get(i).edtInfoItem.getText().toString().trim()))
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this , R.string.invalid_email_address , null);
                            return;
                        }
                        //find the first email address
                        if(email.equals("") &&
                                infoItemViews.get(i).getItem().strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL))
                            email = infoItemViews.get(i).edtInfoItem.getText().toString().trim();
                    }

                }
                if(validValueItemCounts == 0)//if all values are empty ,then show alert so that input at least one more item
                {
                    //alert dialog to notify should input at least one item info
                    MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this , R.string.str_alert_for_grey_contact_empty_inputs , null);
                    return;
                }
                jsonData.put("notes" , strNotes);
                jsonData.put("type" , nSelectingType);
                String fullname = edtFullName.getText().toString().trim();
                if(fullname.contains(" "))
                {
                    int spaceIndex = fullname.lastIndexOf(" ");
                    jsonData.put("first_name" , fullname.substring(0 , spaceIndex));
                    jsonData.put("last_name" , fullname.substring(spaceIndex+1));
                }
                else {
                    jsonData.put("first_name", fullname);
                    jsonData.put("last_name", "");
                }
                jsonData.put("email" , email);

                int size = infoItemViews.size();
                for(int i=0;i<size; i++)
                {
                    JSONObject filedObj = new JSONObject();
                    InfoItem item = infoItemViews.get(i).getItem();
                    if(item.nFieldId >= 0)
                    {
                        filedObj.put("id" , item.nFieldId);
                    }
                    filedObj.put("field_name" , item.strInfoName);
                    //filedObj.put("field_value" , infoItemViews.get(i).edtInfoItem.getText().toString().trim());
                    if(item.isVisible)
                        filedObj.put("field_value" , infoItemViews.get(i).edtInfoItem.getText().toString().trim());
                    else {
                        filedObj.put("field_value", "");
                        infoItemViews.get(i).edtInfoItem.setText("");
                    }
                    filedObj.put("field_type" , item.strFieldType);
                    filedsArray.put(filedObj);
                }

                jsonData.put("fields", filedsArray);

                if (isPhotoChanged == true && !strUploadPhotoPath.equals(""))
                {
                    SyncRequest.setGreyContactPhoto(String.valueOf(contactId) ,new File(strUploadPhotoPath) , new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if(response.isSuccess())
                            {
                                JSONObject data = response.getData();
                                try {
                                    String photoUrl = data.getString("photo_url");
                                    strPhotoUrl = strTempPhotoPath;
                                    strNewUploadPhotoUrl = photoUrl;

                                    jsonData.put("photo_url", strNewUploadPhotoUrl);
                                    if (MyApp.getInstance().getContactsModel().getContactById(contactId) == null){
                                        ContactStruct contactStruct = new ContactStruct();
                                        contactStruct.setFirstName(strFirstName);
                                        contactStruct.setLastName(strLastName);
                                        contactStruct.setContactType(2);
                                        contactStruct.setContactOrEntityId(contactId);
                                        contactStruct.setJsonValue(jsonData.toString());
                                        MyApp.getInstance().getContactsModel().add(contactStruct);
                                    } else
                                    {
                                        ContactStruct contactStruct = MyApp.getInstance().getContactsModel().getContactById(contactId);
                                        contactStruct.setFirstName(strFirstName);
                                        contactStruct.setLastName(strLastName);
                                        contactStruct.setJsonValue(jsonData.toString());

                                        // Update Db Data with Updated Contact Item and refresh Item List
                                        MyApp.getInstance().getContactsModel().update(contactStruct);
                                    }

                                    //call request for update contact info
                                    MyApp.getInstance().getAllContactItemsFromDatabase();
                                    SyncRequest.updateDetail(jsonData, updateContactInfoCallback);
                                    strTempPhotoPath = "";


                                }catch(Exception e)
                                {
                                    e.printStackTrace();
                                    strTempPhotoPath = "";
                                }
                            }
                            else {
                                strTempPhotoPath = "";
                                imgContactPhoto.refreshOriginalBitmap();
                                imgContactPhoto.setImageUrl(strPhotoUrl , imgLoader);
                                imgContactPhoto.invalidate();
                                MyApp.getInstance().showSimpleAlertDiloag(GreyContactProfile.this , R.string.str_err_upload_photo , null);
                            }
                        }
                    });
                    isPhotoChanged = false;

                } else{
                    //call request for update contact info
                    if(MyApp.getInstance().getContactsModel().getContactById(contactId) == null){
                        ContactStruct contactStruct = new ContactStruct();
                        contactStruct.setFirstName(strFirstName);
                        contactStruct.setLastName(strLastName);
                        contactStruct.setContactType(2);
                        contactStruct.setContactOrEntityId(contactId);
                        contactStruct.setJsonValue(jsonData.toString());
                        MyApp.getInstance().getContactsModel().add(contactStruct);
                    } else
                    {
                        ContactStruct contactStruct = MyApp.getInstance().getContactsModel().getContactById(contactId);
                        contactStruct.setFirstName(strFirstName);
                        contactStruct.setLastName(strLastName);
                        contactStruct.setJsonValue(jsonData.toString());

                        // Update Db Data with Updated Contact Item and refresh Item List
                        MyApp.getInstance().getContactsModel().update(contactStruct);
                    }
                    MyApp.getInstance().getAllContactItemsFromDatabase();

                    SyncRequest.updateDetail(jsonData , updateContactInfoCallback);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    ResponseCallBack<Void> deleteSelectedContactInfoCallback = new ResponseCallBack<Void>() {
        @Override
        public void onCompleted(JsonResponse<Void> response) {
            if(response.isSuccess())
            {
                isChanged = false;
                if(isEditable) {

                    int size = infoItemViews.size();
                    String email = null;
                    phones.clear();
                    for(int i=0;i<size; i++)
                    {
                        InfoItem item = infoItemViews.get(i).getItem();
                        if(item.isSelected) {
                            item.strInfoValue = "";
                            infoItemViews.get(i).edtInfoItem.setText("");
                        }
                        item.isSelected = false;
                        if(item.strInfoValue.equals("")) {
                            item.isVisible = false;
                        }
                        else {
                            item.isVisible = true;
                            if(email==null && item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL))
                                email = item.strInfoValue;
                            if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_PHONE))
                                phones.add(item.strInfoValue);
                        }
                    }
                    strEmail = email==null?"":email;
                }

                updateInfoListViews(false);
                refreshUiTopMenuButtons();

            }
        }
    };
    private void deleteSelectedInfo()
    {
        JSONObject jsonData= new JSONObject();
        JSONArray filedsArray = new JSONArray();
        try {
            jsonData.put("contact_id" , contactId);
            {
                jsonData.put("notes" , strNotes);
                jsonData.put("type" , nSelectingType);
                jsonData.put("first_name" , strFirstName);
                jsonData.put("last_name" , strLastName);
                jsonData.put("email" , strEmail);

                int size = infoItemViews.size();
                for(int i=0;i<size; i++)
                {
                    JSONObject filedObj = new JSONObject();
                    InfoItem item = infoItemViews.get(i).getItem();
                    filedObj.put("field_name" , item.strInfoName);
                    if(item.isSelected)//if selected to delete , then replace with empty string value
                    {
                        filedObj.put("field_value", "");
                    }
                    else
                        filedObj.put("field_value" , item.strInfoValue);
                    filedObj.put("field_type" , item.strFieldType);
                    if(!item.isSelected)//if selected to delete , then replace with empty string value
                        filedsArray.put(filedObj);
                }
            }
            jsonData.put("fields" , filedsArray);

            //call request for update contact info
            SyncRequest.updateDetail(jsonData, deleteSelectedContactInfoCallback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private class InfoItem
    {
        public static final int ITEM_TYPE_NUMBER = 1;
        public static final int ITEM_TYPE_EMAIL = 2;
        public static final int ITEM_TYPE_URL = 3;
        public static final int ITEM_TYPE_TEXT = 4;

        public int nFieldId = -1;
        public String strInfoValue;
        public String strFieldType;
        public String strInfoName = "Phone";
        public int nItemInputType;
        public int nMaxLines = 1;
        public boolean isSelected = false;
        public boolean isVisible = true;

        public InfoItem(String infoName , String infoTypeFiled , String infoValue)
        {
            this.strInfoValue = infoValue;
            this.strFieldType = infoTypeFiled;
            this.strInfoName = infoName;
            this.nItemInputType = ITEM_TYPE_NUMBER;
            this.nMaxLines = 1;
            this.isSelected = false;
            this.isVisible = false;

            if(this.strInfoName.toLowerCase().contains("address"))
                this.nMaxLines = 2;

            if(this.strInfoName.contains("Phone") || this.strInfoName.contains("Mobile"))
                this.nItemInputType = ITEM_TYPE_TEXT;
            else if(this.strInfoName.contains("Email"))
                this.nItemInputType = ITEM_TYPE_EMAIL;
            else if(this.strInfoName.contains("Website"))
                this.nItemInputType = ITEM_TYPE_TEXT;
            else
                this.nItemInputType = ITEM_TYPE_TEXT;
        }
        public void setInfoValue(String infoValue)
        {
            this.strInfoValue = infoValue;
        }
        public void setVisibility(boolean visible)
        {
            this.isVisible = visible;
        }

    }

    public class InfoItemView extends LinearLayout {
        protected Context mContext;
        protected LayoutInflater inflater;

        protected InfoItem item;

        private ImageView imgCheckbox;
        private TextView txtInfoItemValue , txtEmailHint;
        private EditText edtInfoItem;
        private LinearLayout itemRootLayout;

        public InfoItemView(Context context) {
            super(context);
            this.mContext = context;
        }
        public InfoItemView(Context context ,InfoItem _item)
        {
            super(context);
            this.mContext = context;
            this.item = _item;

            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.grey_contact_profile_info_item, this, true);

            itemRootLayout = (LinearLayout)findViewById(R.id.rootLayout);
            imgCheckbox = (ImageView)findViewById(R.id.imgCheckbox);
            edtInfoItem = (EditText)findViewById(R.id.edtInfoItem);
            txtInfoItemValue = (TextView)findViewById(R.id.txtInfoItemValue);
            txtEmailHint = (TextView)findViewById(R.id.txtEmailHint);

            if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL)) //if email input filed
            {
                edtInfoItem.addTextChangedListener(edtTextWatcher); edtInfoItem.setOnFocusChangeListener(emailFocusChangeListener);
            }

            imgCheckbox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.isSelected = !item.isSelected;
                    if(item.isSelected) {
                        imgCheckbox.setImageResource(R.drawable.contact_info_item_selected);
                        //imgBtnDelete.setVisibility(View.VISIBLE);
                    }
                    else {
                        imgCheckbox.setImageResource(R.drawable.contact_info_item_non_selected);
                        //imgBtnDelete.setVisibility(View.INVISIBLE);
                    }
                    if(getSelectedItemCounts() > 0)
                        imgBtnDelete.setVisibility(View.VISIBLE);
                    else
                        imgBtnDelete.setVisibility(View.INVISIBLE);
                }
            });
            txtInfoItemValue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL)) //call send email intent
                    {
                        try {
                            Intent email = new Intent(Intent.ACTION_SEND);
                            email.putExtra(Intent.EXTRA_EMAIL, new String[]{item.strInfoValue});
                            email.putExtra(Intent.EXTRA_SUBJECT, "");
                            email.putExtra(Intent.EXTRA_TEXT, "");

                            // need this to prompts email client only
                            email.setType("message/rfc822");

                            GreyContactProfile.this.startActivity(Intent.createChooser(email, "Choose an Email client"));
                        }catch (Exception e){e.printStackTrace();}
                    }
                    else if(item.strInfoName.toLowerCase().contains("mobile") || item.strInfoName.toLowerCase().contains("phone")) //call dial intent
                    {
                        try {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + item.strInfoValue));
                            startActivity(intent);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if(item.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_WEBSITE))//call android browser intent with url
                    {
                        try {
                            String url = item.strInfoValue;
                            if(!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://"+url;
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.strInfoValue));
                            startActivity(browserIntent);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            if(this.item.strInfoName.contains("Email"))
                txtEmailHint.setVisibility(View.VISIBLE);
            else
                txtEmailHint.setVisibility(View.INVISIBLE);

            if(this.item.nMaxLines>1)
            {
                edtInfoItem.setLines(2);
                edtInfoItem.setSingleLine(false);
            }
            else
            {
                edtInfoItem.setMaxLines(1);
                edtInfoItem.setSingleLine(true);
            }

            switch(this.item.nItemInputType)
            {
                case InfoItem.ITEM_TYPE_EMAIL:
                    //edtInfoItem.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    edtInfoItem.setInputType(emailInputType);
                    break;
                case InfoItem.ITEM_TYPE_NUMBER:
                    edtInfoItem.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case InfoItem.ITEM_TYPE_TEXT:
                    edtInfoItem.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case InfoItem.ITEM_TYPE_URL:
                    edtInfoItem.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                    break;
            }

            //get edtInfo's parent layout param
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) edtInfoItem.getLayoutParams();

            if(item.strInfoName.toLowerCase().contains("address") || item.strInfoName.toLowerCase().contains("hours"))
            {
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = mContext.getResources().getDimensionPixelSize(R.dimen.contact_profile_address_input_filed_height);
                edtInfoItem.setLayoutParams(params);
                edtInfoItem.setSingleLine(false);
                edtInfoItem.setMinLines(3);
                edtInfoItem.setLines(3);
            }
            else
            {
                params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                edtInfoItem.setLayoutParams(params);
                edtInfoItem.setSingleLine(true);
                edtInfoItem.setMaxLines(1);
                edtInfoItem.setLines(1);
            }



            edtInfoItem.setHint(item.strInfoName);

            edtInfoItem.setText(item.strInfoValue);

            edtInfoItem.addTextChangedListener(edtTextWatcher);

            itemRootLayout.requestLayout();

        }

        public InfoItem getItem(){return this.item;}

        public void resetValues()
        {
            edtInfoItem.removeTextChangedListener(edtTextWatcher);
            edtInfoItem.setText(item.strInfoValue);
            edtInfoItem.addTextChangedListener(edtTextWatcher);

        }

        public void refreshView()
        {
            if(item.strInfoName.contains("Custom"))
                txtInfoItemValue.setText(item.strInfoValue);
            else
                txtInfoItemValue.setText(item.strInfoName.substring(0,1).toLowerCase()+". "+item.strInfoValue);

            if(isEditable)
            {
                txtInfoItemValue.setVisibility(View.GONE);
                edtInfoItem.setVisibility(View.VISIBLE);
                if(this.item.strInfoName.contains("Email"))
                    txtEmailHint.setVisibility(View.VISIBLE);
                else
                    txtEmailHint.setVisibility(View.INVISIBLE);
                imgCheckbox.setVisibility(View.VISIBLE);
                if(this.item.isSelected)
                    imgCheckbox.setImageResource(R.drawable.contact_info_item_selected);
                else
                    imgCheckbox.setImageResource(R.drawable.contact_info_item_non_selected);
            }
            else
            {
                txtInfoItemValue.setVisibility(View.VISIBLE);
                edtInfoItem.setVisibility(View.GONE);
                txtEmailHint.setVisibility(View.GONE);
                imgCheckbox.setVisibility(View.INVISIBLE);
            }
        }
    }

    class AddInfoItem
    {
        public String strInfoName = "";
        public boolean isSelected;
        public AddInfoItem(String infoName )
        {
            this.strInfoName = infoName;
        }
    }

    class AddInfoListAdapter extends BaseAdapter
    {
        private Context mContext;
        private ArrayList<AddInfoItem> listItems;
        private Resources res;
        public AddInfoListAdapter(Context context , ArrayList<AddInfoItem> items)
        {
            this.mContext = context;
            this.listItems = new ArrayList<AddInfoItem>();
            if(items != null) {
                for (int i = 0; i<items.size(); i++)
                    this.listItems.add(items.get(i));
            }
            this.res = mContext.getResources();
        }

        @Override
        public int getCount() {
            return listItems == null?0:listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public boolean hasSelectedItems()
        {
            if(listItems == null)
                return false;

            int count = 0;
            for(int i=0;i<listItems.size();i++)
            {
                if(listItems.get(i).isSelected)
                    count++;
            }

            if(count>0)
                return true;

            return false;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder(); // our view holder of the row
            if (convertView == null) {

                holder.txtInfoItem = new TextView(mContext);
                holder.txtInfoItem.setPadding(this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_left_right_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_top_bottom_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_left_right_padding),
                        this.res.getDimensionPixelSize(R.dimen.grey_contact_add_info_text_top_bottom_padding)
                        );
                holder.txtInfoItem.setTextColor(Color.BLACK);
                holder.txtInfoItem.setTextSize(this.res.getDimension(R.dimen.grey_contact_add_info_text_size));
                holder.txtInfoItem.setBackgroundColor(Color.WHITE);
                holder.txtInfoItem.setSingleLine(true);
                holder.txtInfoItem.setLinksClickable(false);
                holder.txtInfoItem.setAutoLinkMask(0);
                convertView = holder.txtInfoItem;

                convertView.setTag(holder);

            }
            holder = (ViewHolder) convertView.getTag();
            holder.txtInfoItem.setText(listItems.get(position).strInfoName);
            if(listItems.get(position).isSelected)
                holder.txtInfoItem.setBackgroundColor(0xffd9d9d9);
            else
                holder.txtInfoItem.setBackgroundColor(Color.WHITE);

            holder.txtInfoItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(addInfoListAdapter!=null)
                    {
                        AddInfoItem item = (AddInfoItem) getItem(position);
                        item.isSelected = !item.isSelected;
                        if(addInfoListAdapter.hasSelectedItems())
                            btnAddInfoItemPopupConfirm.setVisibility(View.VISIBLE);
                        else
                            btnAddInfoItemPopupConfirm.setVisibility(View.GONE);

                        notifyDataSetChanged();
                    }
                }
            });
            return convertView;
        }
    }
    class ViewHolder
    {
        TextView txtInfoItem;
    }

    private class EmailValidationCheckRunnable implements Runnable {
        private String email = "";
        private Context mContext;
        public  EmailValidationCheckRunnable(Context context, String emailContent)
        {
            this.email = emailContent;
            this.mContext = context;
        }

        public void setEmailString(String emailContent)
        {
            this.email = emailContent;
        }

        @Override
        public void run()
        {
            String strEmail = email.trim();
            if(strEmail.compareTo("") !=0 && !isEmailValid(strEmail))
            {
                //Toast.makeText(mContext, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
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


    public class CustomDialog extends Dialog implements ImInputEditTExt.OnEditTextKeyDownListener {
        private ImInputEditTExt edtNotes;
        private ImageButton btnClose;
        private ImageButton btnConfirm;
        private View.OnClickListener mCloseClickListener;
        private View.OnClickListener mConfirmClickListener;
        private RelativeLayout rootLayout;
        private RelativeLayout rlHeaderLayout;
        private RelativeLayout rlBodyLayout;
        public boolean bIsKeyBoardVisibled;
        private int screenHeight;
        private int rlHeaderLayoutHeight;
        private int nEdtNoteHeight;
        private int nBodyLaoytHeight;
        private boolean bIsControlStarted;  // If user started the Edit to the EdtNotes
        private Context mContext;

        public CustomDialog(Context context, View.OnClickListener close, View.OnClickListener confirm) {
            //set notes for following entity
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
            mContext = context;
            mCloseClickListener = close;
            mConfirmClickListener = confirm;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.8f;
            getWindow().setAttributes(lpWindow);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            setContentView(R.layout.contact_profile_add_comment_popup);
            setLayout();
            setClickListener(mCloseClickListener, mConfirmClickListener);
            //set entity notes
            rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
            bIsKeyBoardVisibled = false;
            rlHeaderLayout = (RelativeLayout) findViewById(R.id.headerlayout);
            rlBodyLayout = (RelativeLayout) findViewById(R.id.bodyLayout);


            rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    rootLayout.getWindowVisibleDisplayFrame(r);
                    screenHeight = rootLayout.getRootView().getHeight();
                    rlHeaderLayoutHeight = rlHeaderLayout.getHeight();

                    if (nEdtNoteHeight == 0)
                        nEdtNoteHeight = screenHeight - (int) (rlHeaderLayoutHeight * 1.5);
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.15 && bIsKeyBoardVisibled == false) { // 0.15 ratio is perhaps enough to determine keypad height.
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        edtNotes.setHeight(screenHeight - keypadHeight - (int) (rlHeaderLayoutHeight * 1.5));
                        edtNotes.setMaxHeight(screenHeight - keypadHeight - (int) (rlHeaderLayoutHeight * 1.5));
                        bIsKeyBoardVisibled = true;
                    } else if (keypadHeight == 0) {
                        if (nEdtNoteHeight > 0) {
                            edtNotes.setHeight(nEdtNoteHeight);
                            edtNotes.setMaxHeight(nEdtNoteHeight);
                        }
                    }
                }
            });
        }

        private void setLayout() {
            edtNotes = (ImInputEditTExt) findViewById(R.id.edtNotes);
            btnClose = (ImageButton) findViewById(R.id.btnNotePopupClose);
            btnConfirm = (ImageButton) findViewById(R.id.btnNotePopupConfirm);
            btnConfirm.setVisibility(View.GONE);
            rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
            nEdtNoteHeight = 0;
            bIsControlStarted = false;

            edtNotes.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        bIsControlStarted = true;
                    return false;
                }
            });
            edtNotes.setText(strNotes);
            edtNotes.registerOnBackKeyListener(this);
            edtNotes.setSelection(edtNotes.length());
            bIsKeyBoardVisibled = false;

            edtNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    btnConfirm.setVisibility(View.GONE);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    bIsControlStarted = true;
                    if (btnConfirm.getVisibility() == View.GONE)
                        btnConfirm.setVisibility(View.VISIBLE);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        private void setClickListener(View.OnClickListener close, View.OnClickListener confirm) {
            if (close != null && confirm != null) {
                btnClose.setOnClickListener(close);
                btnConfirm.setOnClickListener(confirm);
            }
        }

        public ImInputEditTExt getEdtNotes() {
            return edtNotes;
        }

        @Override
        public void onImEditTextBackKeyDown() {
            if(bIsKeyBoardVisibled) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                hideKeyboard(edtNotes);
                bIsKeyBoardVisibled = false;
            } else {
                //edtNotes.setSelection(edtNotes.length());
                //mCustomDialog.dismiss();
            }
        }
    }

}
