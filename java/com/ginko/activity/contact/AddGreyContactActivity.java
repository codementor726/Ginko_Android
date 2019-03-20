package com.ginko.activity.contact;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
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

import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.activity.profiles.CustomSizeMeasureView;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.activity.sync.SyncGreyContactItem;
import com.android.volley.toolbox.ImageLoader;
import com.ginko.api.request.SyncRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.TcImageVO;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddGreyContactActivity extends MyBaseFragmentActivity implements View.OnClickListener,
        CustomSizeMeasureView.OnMeasureListner,
        ImInputEditTExt.OnEditTextKeyDownListener,
        ActionSheet.ActionSheetListener
{
    /*UI Elements */
    private ImageButton btnPrev , btnConfirm;
    private ImageView btnAddInfoItem , btnNote;
    private ImageView imgTypeEntity , imgTypeWork , imgTypeHome, imgFavorite;
    private NetworkImageView imgContactPhoto;
    private EditText edtName;
    private EditText edtMobile , edtEmail , edtAddress;
    private LinearLayout layout_company, layout_mobiles , layout_phones , layout_emails , layout_address , layout_other_fields;

    private ImageView btnAddInfoItemPopupClose , btnAddInfoItemPopupConfirm;
    private ImageButton btnNotePopupClose , btnNotePopupConfirm;

    private PopupWindow addInfoPopupWindow = null, notePopupWindow = null;
    private View addInfoPopupView = null , notePopupView = null;

    private ListView addInfoListView;
    private ImInputEditTExt edtNotes;

    private RelativeLayout rootLayout;
    private LinearLayout popupRootLayout, name_pnl, layout_input_fields;

    private CustomSizeMeasureView sizeMeasureView;

    /*Variables */
    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int FILTER_PHOTO = 6;

    private Uri uri;
    private String tempPhotoUriPath = "";
    private String strPhotoUrl = "" , strTempPhotoPath = "" , strNewUploadPhotoUrl = "";
    private String strPhotoName = "";
    private String strUploadPhotoPath = "";
    private boolean isPhotoChanged = false;

    private ImageLoader imgLoader;
    private int nContactType = ConstValues.GREY_TYPE_NONE;

    private int activityHeight = 0;
    private String strNotes = "";
    private String firstName = "" , middleName = "" , lastName = "";

    private Pattern pattern;

    private ActionSheet takePhotoActionSheet = null;
    private boolean isTakingProfilePhoto = false;

    private boolean isFavoriteSelected = false;
    private boolean isKeyboardVisible = false;

    private int m_orientHeight = 0;

    private CustomDialog mCustomDialog;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;

            if ((strTempPhotoPath.equals("") && strPhotoUrl.equals(""))|| strPhotoUrl.contains("greyblank.png"))
            {
                takePhotoActionSheet = ActionSheet.createBuilder(AddGreyContactActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(AddGreyContactActivity.this)
                        .show();

            } else
            {
                takePhotoActionSheet = ActionSheet.createBuilder(AddGreyContactActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(AddGreyContactActivity.this)
                        .show();
            }

            //takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    private TextWatcher noteTextWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if(btnNotePopupConfirm.getVisibility() == View.GONE)
                btnNotePopupConfirm.setVisibility(View.VISIBLE);
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher edtTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            updateConfirmButton(isProfileDataValid(false));
        }
    };

    private View.OnFocusChangeListener emailFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus == false)
            {
                EditText edtEmail = (EditText)v;
                if(emailCheckerThread == null) {
                    emailCheckerThread = new EmailValidationCheckRunnable(AddGreyContactActivity.this , edtEmail.getText().toString());
                }
                else {
                    mHandler.removeCallbacks(emailCheckerThread);
                }
                emailCheckerThread.setEmailString(edtEmail.getText().toString());
                mHandler.postDelayed(emailCheckerThread , 500);
            }
        }
    };
    private Handler mHandler = new Handler();
    private EmailValidationCheckRunnable emailCheckerThread;

    public final int ITEM_TYPE_NUMBER = 1;
    public final int ITEM_TYPE_EMAIL = 2;
    public final int ITEM_TYPE_URL = 3;
    public final int ITEM_TYPE_TEXT = 4;

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
    private ArrayList<InfoItem> infoItemList;
    private AddInfoListAdapter addInfoListAdapter;

    private void goToFilterScreen()
    {
        Intent intent = new Intent(AddGreyContactActivity.this , PersonalProfilePhotoFilterActivity.class);
        intent.putExtra("imagePath" , strTempPhotoPath);
        File filteredImagePath = null;

            filteredImagePath = new File(RuntimeContext.getAppDataFolder("UserProfile") +
                String.valueOf("greycontact_profile.jpg"));

        try {
            if (filteredImagePath.exists()) {
                filteredImagePath.delete();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        strTempPhotoPath = filteredImagePath.getAbsolutePath();
        intent.putExtra("saveImagePath" , strTempPhotoPath);
        intent.putExtra("isCircleCrop" , true);
        intent.putExtra("groupType" , "grey");
        intent.putExtra("aspect_x", 1);
        intent.putExtra("aspect_y", 1);

        startActivityForResult(intent , FILTER_PHOTO);
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
        mHandler.removeCallbacks(emailCheckerThread);
    }

    @Override
    public void onBackPressed() {
        mHandler.removeCallbacks(emailCheckerThread);
        super.onBackPressed();
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index){
        if(index == 0)//take a photo
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(System.currentTimeMillis()) + ".jpg"));
            tempPhotoUriPath = uri.getPath();
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            AddGreyContactActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            AddGreyContactActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
        }
        else if(index == 2)//remove photo
        {
            if(isTakingProfilePhoto) {
                strPhotoUrl = "";
                strTempPhotoPath = "";
                strNewUploadPhotoUrl = "";
                imgContactPhoto.refreshOriginalBitmap();
                strPhotoName = "";
                imgContactPhoto.setImageUrl(strPhotoUrl, imgLoader);
                imgContactPhoto.invalidate();
            }

        }
    }

    private class InfoItem
    {
        public String strInfoName = "";
        public String strFieldType = "";
        public String strValue = "";
        public EditText edtText;
        public InfoItem(String infoName , String fieldType , EditText edtTxt)
        {
            this.strInfoName = infoName;
            this.strFieldType = fieldType;
            this.edtText = edtTxt;
        }
    }


    private int emailInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grey_contact);

        imgLoader = MyApp.getInstance().getImageLoader();

        getUIObjects();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        name_pnl = (LinearLayout)findViewById(R.id.name_pnl);
        layout_input_fields = (LinearLayout)findViewById(R.id.layout_input_fields);
        /*rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard..
                    Log.d("wang", "keyboard show");
                } else {
                    rootLayout.requestFocus();
                }
            }
        });*/
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.getInstance().hideKeyboard(rootLayout);
            }
        });
        name_pnl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.getInstance().hideKeyboard(rootLayout);
            }
        });
        layout_input_fields.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.getInstance().hideKeyboard(rootLayout);
            }
        });
        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnAddInfoItem = (ImageView)findViewById(R.id.imgBtnAddInfoItem);   btnAddInfoItem.setOnClickListener(this);
        btnNote = (ImageView)findViewById(R.id.imgBtnNote); btnNote.setOnClickListener(this);

        imgTypeEntity = (ImageView)findViewById(R.id.imgEntity); imgTypeEntity.setOnClickListener(this);
        imgTypeWork = (ImageView)findViewById(R.id.imgWork);imgTypeWork.setOnClickListener(this);
        imgTypeHome = (ImageView)findViewById(R.id.imgHome);imgTypeHome.setOnClickListener(this);
        imgFavorite = (ImageView) findViewById(R.id.imgFavorite);imgFavorite.setOnClickListener(this);

        imgContactPhoto = (NetworkImageView)findViewById(R.id.imgContactPhoto); imgContactPhoto.setOnClickListener(snapProfilePhotoClickListener);
        imgContactPhoto.setDefaultImageResId(R.drawable.no_face_grey);

        edtName = (EditText)findViewById(R.id.txtName); edtName.addTextChangedListener(edtTextWatcher);
        edtMobile = (EditText)findViewById(R.id.txtMobile); edtMobile.addTextChangedListener(edtTextWatcher);
        edtMobile.setInputType(InputType.TYPE_CLASS_TEXT);
        edtEmail = (EditText)findViewById(R.id.txtEmail); edtEmail.addTextChangedListener(edtTextWatcher); edtEmail.setOnFocusChangeListener(emailFocusChangeListener);
        edtAddress = (EditText)findViewById(R.id.txtAddress); edtAddress.addTextChangedListener(edtTextWatcher);

        emailInputType = edtEmail.getInputType();

        layout_mobiles = (LinearLayout)findViewById(R.id.layout_mobiles);
        layout_company = (LinearLayout)findViewById(R.id.layout_company_field);
        layout_phones = (LinearLayout)findViewById(R.id.layout_phones);
        layout_emails = (LinearLayout)findViewById(R.id.layout_emails);
        layout_address = (LinearLayout)findViewById(R.id.layout_address);
        layout_other_fields = (LinearLayout)findViewById(R.id.layout_other_fields);

        /* initialize the fields*/
        infoNameMap = new HashMap<String , Integer>();
        infoItemList = new ArrayList<InfoItem>();
        for(int i=0;i<strInfoNames.length;i++)
        {
            infoNameMap.put(strInfoNames[i] , new Integer(i));
            if(i == 1)//default mobile field
            {
                infoItemList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] , edtMobile));
            }
            else if(i == 7)//email default field
            {
                infoItemList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] , edtEmail));
            }
            else if(i == 9)//address default field
            {
                infoItemList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] , edtAddress));
            }
            else
                infoItemList.add(new InfoItem(strInfoNames[i], strFiledTypeNames[i] , null));
        }

        addInfoPopupView = getLayoutInflater().inflate(R.layout.grey_contact_profile_add_info_popup, null);
        btnAddInfoItemPopupClose = (ImageView)addInfoPopupView.findViewById(R.id.btnAddInfoPopupClose);btnAddInfoItemPopupClose.setOnClickListener(this);
        btnAddInfoItemPopupConfirm = (ImageView)addInfoPopupView.findViewById(R.id.btnAddInfoPopupConfirm);btnAddInfoItemPopupConfirm.setOnClickListener(this);
        addInfoListView = (ListView)addInfoPopupView.findViewById(R.id.infoList);


        notePopupView = getLayoutInflater().inflate(R.layout.contact_profile_add_comment_popup, null);
        btnNotePopupClose = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupClose);btnNotePopupClose.setOnClickListener(this);
        btnNotePopupConfirm = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupConfirm);btnNotePopupConfirm.setOnClickListener(this);
        edtNotes = (ImInputEditTExt)notePopupView.findViewById(R.id.edtNotes);
        edtNotes.registerOnBackKeyListener(this);
        edtNotes.addTextChangedListener(noteTextWatcher);
        updateConfirmButton(false);
        chooseContactType(nContactType);
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

    private void chooseContactType(int type)
    {
        if(nContactType == type)
            nContactType = ConstValues.GREY_TYPE_NONE;
        else
            nContactType = type;
        switch(nContactType)
        {
            case ConstValues.GREY_TYPE_NONE:
                imgTypeEntity.setImageResource(R.drawable.btnentity);
                imgTypeWork.setImageResource(R.drawable.btnwork);
                imgTypeHome.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_ENTITY:
                imgTypeEntity.setImageResource(R.drawable.btnentityup);
                imgTypeWork.setImageResource(R.drawable.btnwork);
                imgTypeHome.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_WORK:
                imgTypeEntity.setImageResource(R.drawable.btnentity);
                imgTypeWork.setImageResource(R.drawable.btnworkup);
                imgTypeHome.setImageResource(R.drawable.btnhome);
                break;
            case ConstValues.GREY_TYPE_HOME:
                imgTypeEntity.setImageResource(R.drawable.btnentity);
                imgTypeWork.setImageResource(R.drawable.btnwork);
                imgTypeHome.setImageResource(R.drawable.btnhomeup);
                break;
        }
    }

    private View.OnClickListener btnCloseClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mCustomDialog != null) {
                hideKeyboard(mCustomDialog.getEdtNotes());
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
                //updateContactInfo(true);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                hideKeyboard(mCustomDialog.getEdtNotes());
                mCustomDialog.bIsKeyBoardVisibled = false;
                mCustomDialog.dismiss();
            }
        }
    };

    private boolean isProfileDataValid(boolean bAlert)
    {
        if(nContactType == ConstValues.GREY_TYPE_NONE)
        {
            if(bAlert)
                MyApp.getInstance().showSimpleAlertDiloag(AddGreyContactActivity.this , "Please select category type!" , null);
            return false;
        }
        if(edtName.getText().toString().trim().equals("")) {
            if(bAlert)
                MyApp.getInstance().showSimpleAlertDiloag(AddGreyContactActivity.this , "Please input contact name!" , null);
            return false;
        }
        boolean hasAtleastOneFieldValue = false;
        for(int i=0;i<infoItemList.size();i++)
        {
            if(infoItemList.get(i).edtText!=null && !infoItemList.get(i).edtText.getText().toString().trim().equals(""))
            {
                hasAtleastOneFieldValue = true;
                break;
            }
        }
        if(!hasAtleastOneFieldValue)
        {
            if(bAlert)
                MyApp.getInstance().showSimpleAlertDiloag(AddGreyContactActivity.this , "Please input at least one filed value." , null);
            return false;
        }

        return true;
    }

    private void updateConfirmButton(boolean isProfileDataValid)
    {
        if(isProfileDataValid)
            btnConfirm.setVisibility(View.VISIBLE);
        else
            btnConfirm.setVisibility(View.GONE);
    }

    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }

    private void enableNotePopup()
    {
        // Creating a pop window for emoticons keyboard
        notePopupWindow = new PopupWindow(notePopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) activityHeight, true);
        notePopupWindow.setFocusable(true);
        notePopupWindow.setAnimationStyle(R.style.AnimationPopup);
        notePopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        notePopupWindow.setFocusable(true);
        notePopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        notePopupWindow.setOutsideTouchable(true);
        notePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popupRootLayout.setVisibility(LinearLayout.GONE);
            }
        });
    }

    private void enableAddInfoItemPopup()
    {
        // Creating a pop window for emoticons keyboard
        addInfoPopupWindow = new PopupWindow(addInfoPopupView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) activityHeight, false);
        addInfoPopupWindow.setAnimationStyle(R.style.AnimationPopup);
        addInfoPopupWindow.setFocusable(true);
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

                for(int i=0;i<infoItemList.size();i++)
                {
                    if(infoItemList.get(i).edtText == null)
                        itemList.add(new AddInfoItem(infoItemList.get(i).strInfoName));
                }
                addInfoListAdapter = new AddInfoListAdapter(AddGreyContactActivity.this , itemList);
                addInfoListView.setAdapter(addInfoListAdapter);
                btnAddInfoItemPopupConfirm.setVisibility(View.GONE);
            }
            else if(popupWindow == notePopupWindow)
            {
                btnNotePopupConfirm.setVisibility(View.GONE);
                edtNotes.removeTextChangedListener(noteTextWatcher);
                edtNotes.setText(strNotes);
                edtNotes.addTextChangedListener(noteTextWatcher);
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
            MyApp.getInstance().hideKeyboard(rootLayout);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

    }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                mHandler.removeCallbacks(emailCheckerThread);
                finish();
                break;

            case R.id.btnConfirm:
                if(!isProfileDataValid(true))
                    return;
                String contactName =edtName.getText().toString().trim();

                if(contactName.contains(" "))
                {
                    String[] tokens = contactName.split(" ");
                    if(tokens.length >= 3) {
                        firstName = tokens[0];
                        middleName = tokens[1];
                        lastName = "";
                        for(int j=2;j<tokens.length;j++)
                            lastName+=tokens[j];
                    }
                    else if(tokens.length == 2)
                    {
                        firstName = tokens[0]; middleName = ""; lastName = tokens[1];
                    }
                    else if(tokens.length == 1)
                    {
                        firstName = tokens[0]; middleName = ""; lastName = "";
                    }
                    else
                    {
                        firstName = ""; middleName = ""; lastName = "";
                    }
                }
                else
                {
                    firstName = contactName;
                    middleName = "";
                    lastName = "";
                }

                JSONObject data = new JSONObject();
                try
                {
                    data.put("first_name",  firstName);
                    data.put("middle_name",  middleName);
                    data.put("last_name",  lastName);

                    JSONArray fieldsArray = new JSONArray();
                    for(int i=0;i<infoItemList.size();i++)
                    {
                        InfoItem item = infoItemList.get(i);
                        if(item.edtText!=null && !item.edtText.getText().toString().trim().equals(""))
                        {
                            item.strValue = item.edtText.getText().toString().trim();

                            JSONObject obj = new JSONObject();
                            obj.put("field_name" , item.strInfoName);
                            obj.put("field_type" , item.strFieldType);
                            obj.put("field_value" , item.strValue);

                            if(item.strInfoName.toLowerCase().contains("email") && !isEmailValid(item.strValue))
                            {
                                Toast.makeText(AddGreyContactActivity.this, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            fieldsArray.put(obj);
                        }
                    }
                    if(!strNewUploadPhotoUrl.equals(""))
                        data.put("photo_url" , strNewUploadPhotoUrl);

                    data.put("photo_name" , strPhotoName );
                    data.put("notes" , strNotes);
                    data.put("fields" , fieldsArray);
                    data.put("type" , nContactType);
                    data.put("is_favorite",isFavoriteSelected);
                }catch(Exception e)
                {
                    e.printStackTrace();
                    data = null;
                }

                if(data == null) return;
                final JSONObject finalJsonData = data;
                SyncRequest.addGreyContact(data , new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if(response.isSuccess())
                        {
                            /*try {
                                finalJsonData.put("photo_url" , strPhotoUrl);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JSONObject resObj = response.getData();
                            try
                            {
                                finalJsonData.put("contact_id" , resObj.optInt("contact_id" , 0));
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                                return;
                            }*/
                            JSONObject resObj = response.getData();
                            final String strContactId = String.valueOf(resObj.optInt("contact_id"));
                            if (isPhotoChanged == true && !strUploadPhotoPath.equals("")) {
                                SyncRequest.setGreyContactPhoto(strContactId, new File(strUploadPhotoPath), new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) throws IOException {
                                        final String strValue = strContactId;

                                        if (response.isSuccess()) {
                                            JSONObject data = response.getData();
                                            try {
                                                String photoUrl = data.getString("photo_url");
                                                strPhotoUrl = strTempPhotoPath;
                                                strNewUploadPhotoUrl = photoUrl;
                                                strTempPhotoPath = "";

                                                SyncRequest.getSyncContactDetial(strValue, new ResponseCallBack<JSONObject>() {
                                                    @Override
                                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                                        if (response.isSuccess()) {
                                                            JSONObject jsonRes = response.getData();
                                                            Intent greyContactProfileIntent = new Intent(AddGreyContactActivity.this, GreyContactProfile.class);
                                                            greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                                            startActivity(greyContactProfileIntent);
                                                            AddGreyContactActivity.this.finish();
                                                        }
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                strTempPhotoPath = "";
                                            }
                                        } else {
                                            strTempPhotoPath = "";
                                            imgContactPhoto.refreshOriginalBitmap();
                                            imgContactPhoto.setImageUrl(strPhotoUrl, imgLoader);
                                            imgContactPhoto.invalidate();
                                            SyncRequest.getSyncContactDetial(strValue, new ResponseCallBack<JSONObject>() {
                                                @Override
                                                public void onCompleted(JsonResponse<JSONObject> response) {
                                                    if (response.isSuccess()) {
                                                        JSONObject jsonRes = response.getData();
                                                        Intent greyContactProfileIntent = new Intent(AddGreyContactActivity.this, GreyContactProfile.class);
                                                        greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                                        startActivity(greyContactProfileIntent);
                                                        AddGreyContactActivity.this.finish();
                                                    }
                                                }
                                            });

                                            MyApp.getInstance().showSimpleAlertDiloag(AddGreyContactActivity.this, R.string.str_err_upload_photo, null);

                                        }
                                    }
                                });
                                isPhotoChanged = false;
                            } else{
                                SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                        if (response.isSuccess()) {
                                            JSONObject jsonRes = response.getData();
                                            Intent greyContactProfileIntent = new Intent(AddGreyContactActivity.this, GreyContactProfile.class);
                                            greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                            startActivity(greyContactProfileIntent);
                                            AddGreyContactActivity.this.finish();
                                        }
                                    }
                                });
                            }

                            /*Intent greyContactProfileIntent = new Intent(AddGreyContactActivity.this , GreyContactProfile.class);
                            greyContactProfileIntent.putExtra("jsonvalue" , finalJsonData.toString());
                            startActivity(greyContactProfileIntent);
                            AddGreyContactActivity.this.finish();*/
                        }
                        else
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(AddGreyContactActivity.this , "Failed to add contact." ,null);
                        }
                    }
                } , true);
                break;

            //select contact type entity
            case R.id.imgEntity:
                chooseContactType(ConstValues.GREY_TYPE_ENTITY);
                updateConfirmButton(isProfileDataValid(false));
                break;
            case R.id.imgWork:
                chooseContactType(ConstValues.GREY_TYPE_WORK);
                updateConfirmButton(isProfileDataValid(false));
                break;
            case R.id.imgHome:
                chooseContactType(ConstValues.GREY_TYPE_HOME);
                updateConfirmButton(isProfileDataValid(false));
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
                        if(item.strInfoName.toLowerCase().contains("mobile") ) {

                            EditText edtText = new EditText(AddGreyContactActivity.this);
                            edtText.setInputType(InputType.TYPE_CLASS_TEXT);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.add_grey_contact_input_fileds_margin_top) , 0 , 0);
                            edtText.setBackgroundResource(R.drawable.et);
                            int padding = getResources().getDimensionPixelSize(R.dimen.contact_profile_input_filed_padding);
                            edtText.setPadding(padding , padding , padding , padding);
                            edtText.setHint(item.strInfoName);
                            edtText.setMaxLines(1);
                            edtText.setLines(1);
                            edtText.setSingleLine(true);
                            edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.grey_contact_info_item_textsize));
                            edtText.setLayoutParams(layoutParams);
                            layout_mobiles.addView(edtText);
                            layout_mobiles.requestLayout();
                            infoItemList.get(infoItemIndex).edtText = edtText;
                        } else if(item.strInfoName.toLowerCase().contains("company")) {
                            EditText edtText = new EditText(AddGreyContactActivity.this);
                            edtText.setInputType(InputType.TYPE_CLASS_TEXT);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.add_grey_contact_input_fileds_margin_top) , 0 , 0);
                            edtText.setBackgroundResource(R.drawable.et);
                            int padding = getResources().getDimensionPixelSize(R.dimen.contact_profile_input_filed_padding);
                            edtText.setPadding(padding , padding , padding , padding);
                            edtText.setHint(item.strInfoName);
                            edtText.setMaxLines(1);
                            edtText.setLines(1);
                            edtText.setSingleLine(true);
                            edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.grey_contact_info_item_textsize));
                            edtText.setLayoutParams(layoutParams);
                            layout_company.addView(edtText);
                            layout_company.requestLayout();
                            infoItemList.get(infoItemIndex).edtText = edtText;
                        } else if(item.strInfoName.toLowerCase().contains("email")) {
                            EditText edtText = new EditText(AddGreyContactActivity.this);
                            //edtText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            edtText.setInputType(emailInputType);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.add_grey_contact_input_fileds_margin_top) , 0 , 0);
                            edtText.setBackgroundResource(R.drawable.et);
                            int padding = getResources().getDimensionPixelSize(R.dimen.contact_profile_input_filed_padding);
                            edtText.setPadding(padding , padding , padding , padding);
                            edtText.setHint(item.strInfoName);
                            edtText.setMaxLines(1);
                            edtText.setLines(1);
                            edtText.setSingleLine(true);
                            edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.grey_contact_info_item_textsize));
                            edtText.setLayoutParams(layoutParams);
                            edtText.setOnFocusChangeListener(emailFocusChangeListener);
                            layout_emails.addView(edtText);
                            layout_emails.requestLayout();
                            infoItemList.get(infoItemIndex).edtText = edtText;
                        }
                        else if(item.strInfoName.toLowerCase().contains("address")) {
                            EditText edtText = new EditText(AddGreyContactActivity.this);
                            edtText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , getResources().getDimensionPixelOffset(R.dimen.contact_profile_address_input_filed_height));
                            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.add_grey_contact_input_fileds_margin_top) , 0 , 0);
                            edtText.setBackgroundResource(R.drawable.et);
                            int padding = getResources().getDimensionPixelSize(R.dimen.contact_profile_input_filed_padding);
                            edtText.setPadding(padding , padding , padding , padding);
                            edtText.setHint(item.strInfoName);
                            edtText.setMinLines(2);
                            edtText.setLines(2);
                            edtText.setSingleLine(false);
                            edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.grey_contact_info_item_textsize));
                            edtText.setGravity(Gravity.LEFT|Gravity.TOP);
                            edtText.setLayoutParams(layoutParams);
                            layout_address.addView(edtText);
                            layout_address.requestLayout();
                            infoItemList.get(infoItemIndex).edtText = edtText;
                        }
                        else if(item.strInfoName.toLowerCase().contains("phone")) {
                            EditText edtText = new EditText(AddGreyContactActivity.this);
                            edtText.setInputType(InputType.TYPE_CLASS_TEXT);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.add_grey_contact_input_fileds_margin_top) , 0 , 0);
                            edtText.setBackgroundResource(R.drawable.et);
                            int padding = getResources().getDimensionPixelSize(R.dimen.contact_profile_input_filed_padding);
                            edtText.setPadding(padding , padding , padding , padding);
                            edtText.setHint(item.strInfoName);
                            edtText.setMaxLines(1);
                            edtText.setLines(1);
                            edtText.setSingleLine(true);
                            edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.grey_contact_info_item_textsize));
                            edtText.setLayoutParams(layoutParams);
                            layout_phones.addView(edtText);
                            layout_phones.requestLayout();
                            infoItemList.get(infoItemIndex).edtText = edtText;
                        }
                        else
                        {
                            EditText edtText = new EditText(AddGreyContactActivity.this);
                            int nItemInputType = ITEM_TYPE_NUMBER;

                            if(item.strInfoName.toLowerCase().contains("phone") || item.strInfoName.toLowerCase().contains("mobile") || item.strInfoName.toLowerCase().contains("fax"))
                                nItemInputType = 9090;
                            else if(item.strInfoName.contains("Email")) {
                                nItemInputType = ITEM_TYPE_EMAIL;
                                edtText.setOnFocusChangeListener(emailFocusChangeListener);
                            }
                            else if(item.strInfoName.contains("Website"))
                                nItemInputType = ITEM_TYPE_URL;
                            else
                                nItemInputType = ITEM_TYPE_TEXT;
                            switch(nItemInputType)
                            {
                                case ITEM_TYPE_EMAIL:
                                    //edtText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                                    edtText.setInputType(emailInputType);
                                    break;
                                case ITEM_TYPE_NUMBER:
                                    edtText.setInputType(InputType.TYPE_CLASS_TEXT);
                                    break;
                                case ITEM_TYPE_TEXT:
                                    edtText.setInputType(InputType.TYPE_CLASS_TEXT);
                                    break;
                                case 9090:
                                    edtText.setInputType(InputType.TYPE_CLASS_TEXT);
                                case ITEM_TYPE_URL:
                                    edtText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                                    break;
                            }
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.add_grey_contact_input_fileds_margin_top) , 0 , 0);
                            edtText.setBackgroundResource(R.drawable.et);
                            int padding = getResources().getDimensionPixelSize(R.dimen.contact_profile_input_filed_padding);
                            edtText.setPadding(padding , padding , padding , padding);
                            edtText.setHint(item.strInfoName);
                            edtText.setMaxLines(1);
                            edtText.setLines(1);
                            edtText.setSingleLine(true);
                            edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.grey_contact_info_item_textsize));
                            edtText.setLayoutParams(layoutParams);
                            layout_other_fields.addView(edtText);
                            layout_other_fields.requestLayout();
                            infoItemList.get(infoItemIndex).edtText = edtText;
                        }
                    }
                }
                break;

            //add note
            case R.id.imgBtnNote:
                //showHidePopupView(notePopupWindow , true);
                mCustomDialog = new CustomDialog(this, btnCloseClickListener, btnConfirmClickListener);
                mCustomDialog.show();
                break;
            //note popup close
            case R.id.btnNotePopupClose:
                hideKeyboard(edtNotes);
                showHidePopupView(notePopupWindow, false);
                break;

            //confirm note popup
            case R.id.btnNotePopupConfirm:
                strNotes = edtNotes.getText().toString().trim();
                hideKeyboard(edtNotes);
                showHidePopupView(notePopupWindow, false);
                break;

            //take photo to contact photo
            case R.id.imgContactPhoto:
                {
                    /*final CharSequence[] items = { getResources().getString(R.string.str_grey_contact_take_photo_from_camera),
                            getResources().getString(R.string.str_grey_contact_take_photo_from_gallery),
                            getResources().getString(R.string.str_grey_contact_take_photo_dialog_cancel),};

                    AlertDialog.Builder builder = new AlertDialog.Builder(AddGreyContactActivity.this);
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
                                AddGreyContactActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
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
                    builder.show();*/


                }

                break;
            case R.id.imgFavorite:
                isFavoriteSelected = !isFavoriteSelected;
                if (isFavoriteSelected == true){
                    imgFavorite.setImageResource(R.drawable.img_favorite);
                }else{
                    imgFavorite.setImageResource(R.drawable.img_unfavorite);
                }
                break;
        }
    }

    @Override
    public void onImEditTextBackKeyDown() {
        if (notePopupWindow.isShowing()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            if(notePopupWindow.getHeight() != activityHeight)
                hideKeyboard(edtNotes);
            else {
                showHidePopupView(notePopupWindow, false);
            }
        }
    }

    //when activity is created and the size of activity is measured
    @Override
    public void onViewSizeMeasure(int width, int height) {
        //get acitivty height
        activityHeight = height;
        System.out.println("----Activity Height = " + String.valueOf(height) + "-----");

        if(addInfoPopupWindow == null)
            enableAddInfoItemPopup();

        if(notePopupWindow == null)
            enableNotePopup();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (addInfoPopupWindow.isShowing() && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            showHidePopupView(addInfoPopupWindow, false);
            return false;
        }
        else if(notePopupWindow.isShowing() && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            showHidePopupView(notePopupWindow, false);
            return false;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
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
                    //goToFilterScreen();

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

                        //goToFilterScreen();
                        setGreyContactPhoto();
                    }
                    break;

                case FILTER_PHOTO:
                    setGreyContactPhoto();
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
            if(strEmail.compareTo("") !=0 && !isEmailValid(strEmail) && mContext == AddGreyContactActivity.this)
            {
                Toast.makeText(mContext, getResources().getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show();
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

                    if (bIsControlStarted == true) {
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
            edtNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    btnConfirm.setVisibility(View.GONE);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (btnConfirm.getVisibility() == View.GONE)
                        updateConfirmButton(isProfileDataValid(false));
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
                hideKeyboard(edtNotes);
                mCustomDialog.dismiss();
            }
        }
    }
}
