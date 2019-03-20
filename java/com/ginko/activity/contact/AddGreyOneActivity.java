package com.ginko.activity.contact;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.facebook.Profile;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.activity.profiles.CustomSizeMeasureView;
import com.ginko.activity.profiles.GreyContactOne;
import com.ginko.activity.profiles.GreyContactProfile;
import com.ginko.api.request.SyncRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.EntityProfileFieldAddOverlayView;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.GreyAddInfoFragment;
import com.ginko.fragments.GreyAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.imagecrop.Util;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.EntityInfoVO;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class AddGreyOneActivity extends MyBaseFragmentActivity implements View.OnClickListener,
        ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener ,
        GreyAddInfoFragment.OnKeyDownListener,
        CustomSizeMeasureView.OnMeasureListner,
        ImInputEditTExt.OnEditTextKeyDownListener,
        ActionSheet.ActionSheetListener{

    /*UI Elements */
    private ImageButton btnPrev , btnConfirm;
    private ImageView btnClearSearch;
    private ImageView btnAddInfoItem , btnNote;
    private ImageView btnAddProfileField;
    private ImageView imgDimBackground;
    private ProfileFieldAddOverlayView addFieldOverlayView;

    private ImageView imgTypeEntity , imgTypeWork , imgTypeHome, imgFavorite;
    private NetworkImageView imgContactPhoto;
    private EditText edtName;
    private LinearLayout layout_company, scrollLayout;

    private ImageButton btnNotePopupClose , btnNotePopupConfirm;

    private PopupWindow notePopupWindow = null;
    private View notePopupView = null;

    private ImInputEditTExt edtNotes;

    private RelativeLayout rootLayout, bodyLayout, addFieldLayout;
    private LinearLayout popupRootLayout, name_pnl, layout_input_fields;

    private CustomSizeMeasureView sizeMeasureView;

    /*Variables */
    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int FILTER_PHOTO = 6;
    final int IMAGE_MAX_SIZE = 1024;

    private ContentResolver mContentResolver;

    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;

    private Uri uri;
    private String tempPhotoUriPath = "";
    private String strPhotoUrl = "" , strTempPhotoPath = "" , strNewUploadPhotoUrl = "";
    private String strPhotoName = "";
    private String strUploadPhotoPath = "";
    private boolean isPhotoChanged = false;
    private boolean isAddFields = false;

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
    private boolean isConfirmed = true;

    private int m_orientHeight = 0;

    private CustomDialog mCustomDialog;
    private GreyAddInfoFragment infoListFragment;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;

            if ((strTempPhotoPath.equals("") && strPhotoUrl.equals(""))|| strPhotoUrl.contains("greyblank.png"))
            {
                takePhotoActionSheet = ActionSheet.createBuilder(AddGreyOneActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(AddGreyOneActivity.this)
                        .show();

            } else
            {
                takePhotoActionSheet = ActionSheet.createBuilder(AddGreyOneActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(AddGreyOneActivity.this)
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
            if (edtName.hasFocus() && s.length() > 0)
                btnClearSearch.setVisibility(View.VISIBLE);
            else
                btnClearSearch.setVisibility(View.GONE);
        }
        @Override
        public void afterTextChanged(Editable s) {
            updateConfirmButton(isProfileDataValid(false));
        }
    };

    private void goToFilterScreen()
    {
        Intent intent = new Intent(AddGreyOneActivity.this , PersonalProfilePhotoFilterActivity.class);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_grey_one);

        imgLoader = MyApp.getInstance().getImageLoader();
        getUIObjects();
        initInfoItemFragment();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;

        mContentResolver = getContentResolver();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        bodyLayout = (RelativeLayout)findViewById(R.id.bodyLayout);
        addFieldLayout = (RelativeLayout)findViewById(R.id.addFiledLayout);

        name_pnl = (LinearLayout)findViewById(R.id.name_pnl);

        bodyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.getInstance().hideKeyboard(rootLayout);
            }
        });

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                        if (infoListFragment != null)
                            infoListFragment.setKeyboardVisibilty(true);
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                        if (infoListFragment != null)
                            infoListFragment.setKeyboardVisibilty(false);

                    }
                }
            }
        });

        name_pnl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.getInstance().hideKeyboard(rootLayout);
            }
        });

        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        addFieldOverlayView = (ProfileFieldAddOverlayView)findViewById(R.id.addFieldOverlayView);
        addFieldOverlayView.setOnProfileFieldItemsChangeListener(this); addFieldOverlayView.setVisibility(View.GONE);

        imgDimBackground = (ImageView)findViewById(R.id.imgDimBackground);
        imgDimBackground.setVisibility(View.GONE);

        btnAddProfileField = (ImageView)findViewById(R.id.btnAddFieldInfoItem); btnAddProfileField.setOnClickListener(this);

        btnNote = (ImageView)findViewById(R.id.imgBtnNote); btnNote.setOnClickListener(this);

        imgTypeEntity = (ImageView)findViewById(R.id.imgEntity); imgTypeEntity.setOnClickListener(this);
        imgTypeWork = (ImageView)findViewById(R.id.imgWork);imgTypeWork.setOnClickListener(this);
        imgTypeHome = (ImageView)findViewById(R.id.imgHome);imgTypeHome.setOnClickListener(this);
        imgFavorite = (ImageView) findViewById(R.id.imgFavorite);imgFavorite.setOnClickListener(this);

        imgContactPhoto = (NetworkImageView)findViewById(R.id.imgContactPhoto); imgContactPhoto.setOnClickListener(snapProfilePhotoClickListener);
        imgContactPhoto.setDefaultImageResId(R.drawable.no_face_grey);

        scrollLayout = (LinearLayout)findViewById(R.id.scrollLayout);
        scrollLayout.requestFocus();
        scrollLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(edtName);
                return false;
            }
        });

        edtName = (EditText)findViewById(R.id.txtName); edtName.addTextChangedListener(edtTextWatcher);
        edtName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtName.getWindowToken(), 0);

                    /*
                    if (txtDirname.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                    */
                    btnClearSearch.setVisibility(View.GONE);
                    scrollLayout.requestFocus();

                    return true;
                }
                return false;
            }
        });

        edtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (edtName.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    edtName.setCursorVisible(true);

                } else {
                    btnClearSearch.setVisibility(View.GONE);
                    edtName.setCursorVisible(false);
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtName.setText("");
                btnClearSearch.setVisibility(View.GONE);
            }
        });

        btnClearSearch.setVisibility(View.GONE);

        notePopupView = getLayoutInflater().inflate(R.layout.contact_profile_add_comment_popup, null);
        btnNotePopupClose = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupClose);btnNotePopupClose.setOnClickListener(this);
        btnNotePopupConfirm = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupConfirm);btnNotePopupConfirm.setOnClickListener(this);
        edtNotes = (ImInputEditTExt)notePopupView.findViewById(R.id.edtNotes);
        edtNotes.registerOnBackKeyListener(this);
        edtNotes.addTextChangedListener(noteTextWatcher);
        updateConfirmButton(false);
        chooseContactType(nContactType);
    }

    private void initInfoItemFragment()
    {
        EntityInfoVO entityInfo = null;
        entityInfo = new EntityInfoVO();

        infoListFragment = GreyAddInfoFragment.newInstance(entityInfo,false);
        infoListFragment.setOnProfileFieldItemsChangeListener(this);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();
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
                MyApp.getInstance().showSimpleAlertDiloag(AddGreyOneActivity.this , "Please select category type!" , null);
            return false;
        }
        if(edtName.getText().toString().trim().equals("")) {
            if(bAlert)
                MyApp.getInstance().showSimpleAlertDiloag(AddGreyOneActivity.this , "Please input contact name!" , null);
            return false;
        }

        boolean bResult = true;
        if(infoListFragment != null)
            bResult = infoListFragment.checkEntityInfo();
        return bResult;
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
            if(popupWindow == notePopupWindow)
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_grey_one, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//take a photo
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("UserProfile") +
                    String.valueOf(System.currentTimeMillis()) + ".jpg"));
            tempPhotoUriPath = uri.getPath();
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            AddGreyOneActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            AddGreyOneActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
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
    public void onBackPressed() {
        if(addFieldOverlayView.getVisibility() == View.VISIBLE)
        {
            imgDimBackground.setVisibility(View.GONE);
            addFieldOverlayView.hideView();
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            return;
        }

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
                if(!isProfileDataValid(true))
                    return;
                EntityInfoVO entityInfoVO = null;
                if(infoListFragment != null)
                    entityInfoVO = infoListFragment.saveEntityInfo(AddGreyOneActivity.this, true);
                if(entityInfoVO == null)
                    return;

                if (isConfirmed == false)
                    return;

                String contactName =edtName.getText().toString().trim();

                if(contactName.contains(" "))
                {
                    String[] tokens = contactName.split(" ");
                    if(tokens.length >= 3) {
                        firstName = tokens[0];
                        middleName = tokens[1];
                        lastName = "";
                        for(int j=2;j<tokens.length;j++) {
                            lastName += tokens[j];
                            if (j < tokens.length -1)
                                lastName += " ";
                        }

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
                try {
                    data.put("first_name", firstName);
                    data.put("middle_name", middleName);
                    data.put("last_name", lastName);

                    JSONArray fieldsArray = new JSONArray();
                    ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
                    for (int i = 0; i < infoList.size(); i++) {
                        GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
                        if (!infoItem.isVisible) continue;
                        if (infoItem != null && !infoItem.strInfoValue.trim().equals("")) {
                            if (infoItem.strFieldType == ConstValues.PROFILE_FIELD_TYPE_MOBILE &&
                                    infoListFragment.getAvailableMobileFieldCount() == 0)
                                infoItem.strInfoName = "Mobile";
                            if (infoItem.strFieldType == ConstValues.PROFILE_FIELD_TYPE_ADDRESS &&
                                    infoListFragment.getAvailableAddressFieldCount() == 0)
                                infoItem.strInfoName = "Address";
                            if (infoItem.strFieldType == ConstValues.PROFILE_FIELD_TYPE_EMAIL &&
                                    infoListFragment.getAvailableEmailFieldCount() == 0)
                                infoItem.strInfoName = "Email";
                            JSONObject obj = new JSONObject();
                            obj.put("field_name" , infoItem.strInfoName);
                            obj.put("field_type" , infoItem.strFieldType);
                            obj.put("field_value" , infoItem.strInfoValue.trim());

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

                } catch (JSONException e) {
                    e.printStackTrace();
                    data = null;
                }

                if(data == null) return;
                final JSONObject finalJsonData = data;

                isConfirmed = false;

                SyncRequest.addGreyContact(data , new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if(response.isSuccess())
                        {
                            JSONObject resObj = response.getData();
                            final String strContactId = String.valueOf(resObj.optInt("contact_id"));
                            if (isPhotoChanged == true && !strUploadPhotoPath.equals("")) {
                                SyncRequest.setGreyContactPhoto(strContactId, new File(strUploadPhotoPath), new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) {
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
                                                        isConfirmed = true;
                                                        if (response.isSuccess()) {
                                                            JSONObject jsonRes = response.getData();
                                                            Intent greyContactProfileIntent = new Intent(AddGreyOneActivity.this, GreyContactOne.class);
                                                            greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                                            startActivity(greyContactProfileIntent);
                                                            AddGreyOneActivity.this.finish();
                                                        }
                                                    }
                                                });
                                            } catch (Exception e) {
                                                isConfirmed = true;
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
                                                    isConfirmed = true;
                                                    if (response.isSuccess()) {
                                                        JSONObject jsonRes = response.getData();
                                                        Intent greyContactProfileIntent = new Intent(AddGreyOneActivity.this, GreyContactOne.class);
                                                        greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                                        startActivity(greyContactProfileIntent);
                                                        AddGreyOneActivity.this.finish();
                                                    }
                                                }
                                            });

                                            MyApp.getInstance().showSimpleAlertDiloag(AddGreyOneActivity.this, R.string.str_err_upload_photo, null);

                                        }
                                    }
                                });
                                isPhotoChanged = false;
                            } else{
                                SyncRequest.getSyncContactDetial(strContactId, new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                        isConfirmed = true;
                                        if (response.isSuccess()) {
                                            JSONObject jsonRes = response.getData();
                                            Intent greyContactProfileIntent = new Intent(AddGreyOneActivity.this, GreyContactOne.class);
                                            greyContactProfileIntent.putExtra("jsonvalue", jsonRes.toString());
                                            startActivity(greyContactProfileIntent);
                                            AddGreyOneActivity.this.finish();
                                        }
                                    }
                                });
                            }
                        }
                        else
                        {
                            isConfirmed = true;
                            MyApp.getInstance().showSimpleAlertDiloag(AddGreyOneActivity.this , "Failed to add contact." ,null);
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

            //add note
            case R.id.imgBtnNote:
                //showHidePopupView(notePopupWindow , true);
                if (addFieldOverlayView.getVisibility() != View.VISIBLE)
                {
                    mCustomDialog = new CustomDialog(this, btnCloseClickListener, btnConfirmClickListener);
                    mCustomDialog.show();
                }
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

            //add profile field items
            case R.id.btnAddFieldInfoItem:
                if(infoListFragment == null) return;

                addFieldOverlayView.setProfileFieldItems("grey", infoListFragment.getCurrentVisibleInfoItems());

                if(addFieldOverlayView.getVisibility() == View.GONE && !isAddFields)
                {
                    MyApp.getInstance().hideKeyboard(rootLayout);
                    imgDimBackground.setVisibility(View.VISIBLE);
                    imgDimBackground.setClickable(true);
                    imgDimBackground.setFocusable(true);
                    imgDimBackground.setFocusableInTouchMode(true);
                    imgDimBackground.requestFocus();
                    addFieldOverlayView.showView();
                    edtName.setCursorVisible(false);
                    btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                }
                else
                {
                    imgDimBackground.setVisibility(View.GONE);
                    imgDimBackground.setClickable(false);
                    imgDimBackground.setFocusableInTouchMode(false);
                    imgDimBackground.setFocusable(false);
                    addFieldOverlayView.hideView();
                    edtName.setCursorVisible(true);
                    //InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

                    if(isAddFields)
                        btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                    else
                        btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
                }
                break;


            //take photo to contact photo
            case R.id.imgContactPhoto:
            {
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

    @Override
    public void onViewSizeMeasure(int width, int height) {
        //get acitivty height
        activityHeight = height;
        System.out.println("----Activity Height = " + String.valueOf(height) + "-----");

        if(notePopupWindow == null)
            enableNotePopup();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(notePopupWindow.isShowing() && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
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
                        File myFile = new File(uri.getPath());
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        String picturePath = "";

                        if (cursor != null && cursor.moveToFirst())
                        {
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            picturePath = cursor.getString(columnIndex);
                            cursor.close();
                        }
                        else
                        {
                            if (myFile.exists())
                                picturePath = myFile.getAbsolutePath();
                        }
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
            imgContactPhoto.setImageUrl(strPhotoUrl, imgLoader);
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
                checkOrientation(strUploadPhotoPath);
                checkOrientation(strTempPhotoPath);
                imgContactPhoto.refreshOriginalBitmap();
                imgContactPhoto.setImageUrl(strTempPhotoPath, imgLoader);
                imgContactPhoto.invalidate();
                isPhotoChanged = true;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    private void checkOrientation(String strPhotoPath)
    {
        String imageFilepath = strPhotoPath;
        if(imageFilepath.startsWith("file:/"))
            imageFilepath = imageFilepath.substring(6);

        int exifOrientation = 0;
        //get Orient Image's ORIENTATION. * Add by lee
        try {
            ExifInterface exif = new ExifInterface(imageFilepath);     //Since API Level 5
            exifOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));

        }catch (IOException e){
            e.printStackTrace();
        }

        Uri saveImageUri = getImageUri(imageFilepath);
        Bitmap mBitmap = getBitmap(imageFilepath);

        if (mBitmap == null) {
            finish();
            return;
        }

        // Rotation for Nexus 9  * Add by lee
        if (exifOrientation == 6)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            saveOutput(mBitmap, saveImageUri);
        }
        else if (exifOrientation == 8)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            saveOutput(mBitmap, saveImageUri);
        }

        else if (exifOrientation == 3)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            saveOutput(mBitmap, saveImageUri);
        }
    }

    private Uri getImageUri(String path) {

        return Uri.fromFile(new File(path));
    }

    private Bitmap getBitmap(String path) {

        Uri uri = getImageUri(path);
        InputStream in = null;
        try {
            in = mContentResolver.openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();

            return b;
        } catch (FileNotFoundException e) {
            Log.e("PhotoFilter", "file " + path + " not found");
        } catch (IOException e) {
            Log.e("PhotoFilter", "file " + path + " not found");
        }
        return null;
    }

    private void saveOutput(Bitmap rotateImage, Uri saveImageUri) {

        if (saveImageUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(saveImageUri);
                if (outputStream != null) {
                    rotateImage.compress(mOutputFormat, 90, outputStream);
                }
            } catch (IOException ex) {

                Log.e("PhotoFilter", "Cannot open file: " + saveImageUri, ex);
                return;
            } finally {
                Util.closeSilently(outputStream);
            }
        } else {

            Log.e("PhotoFilter", "not defined image url");
        }
        rotateImage.recycle();
    }

    @Override
    public void gotToDoneOrNext() {

    }

    @Override
    public void onAddedNewProfileField(String fieldName) {
        if(fieldName.equals("noExistAddFields")){
            imgDimBackground.setVisibility(View.GONE);
            imgDimBackground.setFocusable(false);
            addFieldOverlayView.hideOverlapView();
            btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
            isAddFields = true;
            return;
        }
        if(infoListFragment != null)
            infoListFragment.addNewInfoItem(fieldName);
    }

    @Override
    public void onRemovedProfileField(String fieldName) {
        if(infoListFragment != null) {
            isAddFields = false;
            btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
            if(fieldName.equals("Address")) {
                infoListFragment.getEntityInfo().setLongitude(null);
                infoListFragment.getEntityInfo().setLatitude(null);
                infoListFragment.getEntityInfo().setAddressConfirmed(true);
            }
            infoListFragment.removeInfoItem(fieldName);

        }
    }

    @Override
    public void onEditTextWatcher() {
        updateConfirmButton(isProfileDataValid(false));
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
