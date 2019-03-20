package com.ginko.activity.profiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.api.request.GreyContactRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.api.request.UserRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.EntityProfileFieldAddOverlayView;
import com.ginko.customview.ProfileFieldAddOverlayView;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.database.ContactStruct;
import com.ginko.fragments.GreyAddInfoFragment;
import com.ginko.fragments.GreyAddInfoFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.EntityInfoDetailVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GreyContactOne extends MyBaseFragmentActivity implements View.OnClickListener ,
        ProfileFieldAddOverlayView.OnProfileFieldItemsChangeListener ,
        GreyAddInfoFragment.OnKeyDownListener,
        CustomSizeMeasureView.OnMeasureListner,
        ImInputEditTExt.OnEditTextKeyDownListener,
        ActionSheet.ActionSheetListener{

    private ImageButton btnPrev , btnConfirm;
    private ImageView btnClose, btnClearSearch;
    private NetworkImageView imgContactPhoto;
    private EditText edtFullName;
    private ImageView btnAddProfileField;
    private ImageView imgDimBackground;
    private ProfileFieldAddOverlayView addFieldOverlayView;

    private TextView txtFirstName , txtMiddleName, txtLastName;
    private ImageView imgEntity , imgWork , imgHome, imgFavorite;
    private ImageView imgBtnDelete , imgBtnNote , imgEditContact;

    private ImageButton btnNotePopupClose , btnNotePopupConfirm;

    private PopupWindow notePopupWindow = null;
    private View notePopupView = null;

    private RelativeLayout rootLayout, bodyLayout, bottomLayout;
    private ScrollView mScrollView;
    private LinearLayout popupRootLayout, scrollLayout;
    private CustomSizeMeasureView sizeMeasureView;

    //2016.9.21 Layout Update for Big Profile Show
    private NetworkImageView tiledProfilePhoto;
    private RelativeLayout hiddenLayout;
    private ImageView imgDimClose;

    /* Variables */
    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;

    private Uri uri;
    private String tempPhotoUriPath = "";
    private String strUploadPhotoPath = "";

    private ImageLoader imgLoader;

    private boolean isEditable = false;
    private boolean isChanged = false;
    private boolean isFavouriteChanged = false;
    private boolean isPhotoChanged = false;
    private boolean isSelected = false;
    private boolean isAddFields = false;

    private boolean isPreFavorite = false;

    private final int TYPE_ENTITY = 0;
    private final int TYPE_HOME = 1;
    private final int TYPE_WORK = 2;
    private final int TYPE_FAVORITE = 3;

    private ActionSheet takePhotoActionSheet = null;
    private boolean isTakingProfilePhoto = false;

    private int nType = TYPE_ENTITY;/* entity , work , home */
    private int nSelectingType = TYPE_ENTITY;/* entity , work , home */

    private int activityHeight = 0;
    private CustomDialog mCustomDialog;
    private GreyAddInfoFragment infoListFragment;
    /* Contact Item Variables */
    int contactId;
    private String strFirstName = "" , strLastName = "";
    private String strNotes = "";
    private String strPhotoUrl = "" , strTempPhotoPath = "" , strNewUploadPhotoUrl = "";
    private String strEmail = "";
    private List<String> phones;

    /*Private Variables */
    private EntityInfoVO entityInfo;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isEditable) {
                hiddenLayout.setVisibility(View.VISIBLE);
                fadeInAndShowImage(tiledProfilePhoto);
                return;
            }
            if (imgDimBackground.getVisibility() == View.VISIBLE)
                return;

            setTheme(R.style.ActionSheetStyleIOS7);
            isTakingProfilePhoto = true;

            if ((strPhotoUrl.equals("") || strPhotoUrl.contains("greyblank.png")) && strTempPhotoPath.equals(""))
            {
                takePhotoActionSheet = ActionSheet.createBuilder(GreyContactOne.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(GreyContactOne.this)
                        .show();

            } else
            {
                takePhotoActionSheet = ActionSheet.createBuilder(GreyContactOne.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(GreyContactOne.this)
                        .show();
            }

            //takePhotoActionSheet.show(getSupportFragmentManager() , "actionSheet");
        }
    };

    TextWatcher edtTextWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (edtFullName.hasFocus() && s.length() > 0)
                btnClearSearch.setVisibility(View.VISIBLE);
            else
                btnClearSearch.setVisibility(View.GONE);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grey_contact_one);

        this.isEditable = false;
        this.isChanged = false;
        this.isPhotoChanged = false;

        phones = new ArrayList<String>();

        imgLoader = MyApp.getInstance().getImageLoader();

        Intent intent = this.getIntent();
        parseJSONObject(intent.getStringExtra("jsonvalue"));
        initInfoItemFragment(intent.getStringExtra("jsonvalue"));

        getUIObjects();
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

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        bottomLayout = (RelativeLayout) findViewById(R.id.bottomLayout);

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (infoListFragment != null)
                        infoListFragment.setKeyboardVisibilty(true);

                } else {
                    if (infoListFragment != null)
                        infoListFragment.setKeyboardVisibilty(false);
                }
            }
        });

        mScrollView = (ScrollView)findViewById(R.id.mainScrollView);
        rootLayout.setClickable(true);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MyApp.getInstance().hideKeyboard(rootLayout);
                return false;
            }
        });

        mScrollView.setClickable(true);
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MyApp.getInstance().hideKeyboard(rootLayout);
                return false;
            }
        });

        popupRootLayout = (LinearLayout)findViewById(R.id.popupRootLayout);
        bodyLayout = (RelativeLayout)findViewById(R.id.bodyLayout);
        bodyLayout.setClickable(true);
        bodyLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MyApp.getInstance().hideKeyboard(rootLayout);
                return false;
            }
        });

        btnPrev =  (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm =  (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setOnClickListener(this);
        btnClose  =  (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);

        scrollLayout = (LinearLayout)findViewById(R.id.scrollLayout);
        scrollLayout.requestFocus();
        scrollLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(edtFullName);
                return false;
            }
        });

        edtFullName = (EditText)findViewById(R.id.edtFullName);
        txtFirstName = (TextView)findViewById(R.id.txtFirstName);
        txtLastName = (TextView)findViewById(R.id.txtLastName);

        edtFullName.addTextChangedListener(edtTextWatcher);
        edtFullName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtFullName.getWindowToken(), 0);
                    btnClearSearch.setVisibility(View.GONE);
                    scrollLayout.requestFocus();

                    return true;
                }
                return false;
            }
        });

        edtFullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (edtFullName.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    edtFullName.setCursorVisible(true);

                } else {
                    btnClearSearch.setVisibility(View.GONE);
                    edtFullName.setCursorVisible(false);
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtFullName.setText("");
                btnClearSearch.setVisibility(View.GONE);
            }
        });

        btnClearSearch.setVisibility(View.GONE);

        EditText edtEmail = (EditText)findViewById(R.id.txtEmail);

        imgEntity = (ImageView)findViewById(R.id.imgEntity); imgEntity.setOnClickListener(this);
        imgWork = (ImageView)findViewById(R.id.imgWork); imgWork.setOnClickListener(this);
        imgHome = (ImageView)findViewById(R.id.imgHome); imgHome.setOnClickListener(this);
        imgFavorite = (ImageView)findViewById(R.id.imgFavorite); imgFavorite.setOnClickListener(this);
        imgBtnDelete = (ImageView)findViewById(R.id.imgBtnDelete); imgBtnDelete.setOnClickListener(this);
        imgBtnNote = (ImageView)findViewById(R.id.imgBtnNote); imgBtnNote.setOnClickListener(this);

        imgEditContact = (ImageView)findViewById(R.id.imgEditContact); imgEditContact.setOnClickListener(this);

        imgContactPhoto = (NetworkImageView)findViewById(R.id.imgContactPhoto); imgContactPhoto.setOnClickListener(snapProfilePhotoClickListener);
        imgContactPhoto.setDefaultImageResId(R.drawable.im_default_contact_photo);

        notePopupView = getLayoutInflater().inflate(R.layout.contact_profile_add_comment_popup, null);
        btnNotePopupClose = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupClose);btnNotePopupClose.setOnClickListener(this);
        btnNotePopupConfirm = (ImageButton)notePopupView.findViewById(R.id.btnNotePopupConfirm);btnNotePopupConfirm.setOnClickListener(this);

        addFieldOverlayView = (ProfileFieldAddOverlayView)findViewById(R.id.addFieldOverlayView);
        addFieldOverlayView.setOnProfileFieldItemsChangeListener(this); addFieldOverlayView.setVisibility(View.GONE);

        imgDimBackground = (ImageView)findViewById(R.id.imgDimBackground);
        imgDimBackground.setVisibility(View.GONE);

        btnAddProfileField = (ImageView)findViewById(R.id.btnAddFieldInfoItem); btnAddProfileField.setOnClickListener(this);
        if (isPreFavorite == false)
            imgFavorite.setImageResource(R.drawable.img_unfavorite);
        else
            imgFavorite.setImageResource(R.drawable.img_contact_favorite);

        //2016.9.21 Update
        tiledProfilePhoto = (NetworkImageView)findViewById(R.id.tileProfileImage);
        hiddenLayout = (RelativeLayout)findViewById(R.id.hiddenLayout);
        imgDimClose = (ImageView)findViewById(R.id.imgDimClose);
        tiledProfilePhoto.setDefaultImageResId(R.drawable.entity_profile_preview);

        hiddenLayout.setVisibility(View.GONE);

        imgDimClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hiddenLayout.setVisibility(View.GONE);
                tiledProfilePhoto.setVisibility(View.INVISIBLE);
            }
        });

        hiddenLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (tiledProfilePhoto.getAnimation() != null && !tiledProfilePhoto.getAnimation().hasEnded())
                        return false;

                    hiddenLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                //GAD-1613 Change to GAD-1630
                if (imgDimBackground.getVisibility() == View.VISIBLE) {
                    imgDimBackground.setVisibility(View.GONE);
                    imgDimBackground.setFocusable(false);
                    edtFullName.setCursorVisible(true);
                    edtFullName.setFocusableInTouchMode(true);
                    edtFullName.setFocusable(true);
                }

                if(isAddFields)
                    btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                else
                    btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);
                addFieldOverlayView.hideView();

                DialogInterface.OnClickListener updateConfirmDialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                updateContactInfo(false);
                                dialog.dismiss();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder updateConfirmDialogBuilder = new AlertDialog.Builder(GreyContactOne.this);
                updateConfirmDialogBuilder.setMessage(getResources().getString(R.string.str_confirm_dialog_make_changes_to_this_contact_info))
                        .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), updateConfirmDialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), updateConfirmDialogClickListener)
                        .setCancelable(false).show();

                break;

            //edit contact info
            case R.id.imgEditContact:
                isEditable = true;
                isChanged = false;
                nSelectingType = nType;
                String fullname = strFirstName;
                txtFirstName.setText(fullname);
                txtLastName.setText(strLastName);
                fullname += " "+ strLastName;
                edtFullName.removeTextChangedListener(edtTextWatcher);
                edtFullName.setText(fullname);
                edtFullName.addTextChangedListener(edtTextWatcher);

                updateUIFromEditable();
                refreshUiTopMenuButtons();
                infoListFragment.updateInfoView(true);
                infoListFragment.setAllPending(true);
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
                if (!isEditable) {
                    isSelected = !isSelected;
                    updateFavoriteButton(isSelected);
                    isPreFavorite = isSelected;
                } else {
                    if (isSelected == true)
                        imgFavorite.setImageResource(R.drawable.img_unfavorite);
                    else
                        imgFavorite.setImageResource(R.drawable.img_contact_favorite);
                    isSelected = !isSelected;
                    isFavouriteChanged = !isFavouriteChanged;
                }

                break;
            //delete
            case R.id.imgBtnDelete: {
                //confirm dialog to delete this contact forever
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    dialog.dismiss();
                                    SyncRequest.removeGreyContact(String.valueOf(contactId), new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                MyApp.getInstance().getContactsModel().deleteContactWithContactId(contactId);
                                                MyApp.getInstance().removefromContacts(contactId);

                                                Intent returnIntent = new Intent();
                                                returnIntent.putExtra("isContactDeleted" , true);
                                                GreyContactOne.this.setResult(Activity.RESULT_OK , returnIntent);
                                                finish();
                                            } else {
                                                MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this, R.string.str_err_fail_to_delete_grey_contact, null);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(GreyContactOne.this);
                    builder.setMessage(getResources().getString(R.string.str_delete_grey_contact_forever_confirm_dialog_title))
                            .setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), dialogClickListener)
                            .setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), dialogClickListener)
                            .setCancelable(false)
                            .show();
                }
                break;

            //add profile field items
            case R.id.btnAddFieldInfoItem:
                if(infoListFragment == null) return;

                MyApp.getInstance().hideKeyboard(rootLayout);
                addFieldOverlayView.setProfileFieldItems("grey", infoListFragment.getCurrentVisibleInfoItems());

                if(addFieldOverlayView.getVisibility() == View.GONE && !isAddFields)
                {
                    MyApp.getInstance().hideKeyboard(rootLayout);
                    imgDimBackground.setVisibility(View.VISIBLE);
                    imgDimBackground.setFocusable(true);
                    addFieldOverlayView.showView();
                    edtFullName.setCursorVisible(false);
                    edtFullName.setFocusable(false);
                    btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
                }
                else
                {
                    imgDimBackground.setVisibility(View.GONE);
                    imgDimBackground.setFocusable(false);
                    addFieldOverlayView.hideView();
                    edtFullName.setCursorVisible(true);
                    edtFullName.setFocusableInTouchMode(true);
                    edtFullName.setFocusable(true);
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
                /*
                if(isEditable)
                {
                    if(strPhotoUrl.equals("") || strPhotoUrl.contains("greyblank.png"))    //there isn't user's photo.
                    {
                        final CharSequence[] items = { getResources().getString(R.string.str_grey_contact_take_photo_from_camera),
                                getResources().getString(R.string.str_grey_contact_take_photo_from_gallery),
                                getResources().getString(R.string.str_grey_contact_take_photo_dialog_cancel),};

                        AlertDialog.Builder builder = new AlertDialog.Builder(GreyContactOne.this);
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
                                    GreyContactOne.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(GreyContactOne.this);
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
                                    GreyContactOne.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
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
                                        MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this, R.string.str_grey_contact_remove_photo_alert, null);
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
                                                    MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this, "Failed to remove photo", null);
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
                */
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

        }
    }

    @Override
    public void onImEditTextBackKeyDown() {

    }

    @Override
    public void onViewSizeMeasure(int width, int height) {
        //get acitivty height
        activityHeight = height;
        System.out.println("----Activity Height = " + String.valueOf(height) + "-----");
    }

    private void fadeInAndShowImage(final NetworkImageView img)
    {
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(600);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }

    private void parseJSONObject(String object) {
        Log.e("GreyContactProfile", object);
        try {
            JSONObject jsonObject = new JSONObject(object);
            try {
                contactId = jsonObject.getInt("contact_id");
            } catch (Exception e) {
                e.printStackTrace();
                contactId = 0;
            }

            try {
                strPhotoUrl = jsonObject.getString("photo_url");
            } catch (Exception e) {
                strPhotoUrl = "";
            }
            try {
                strFirstName = jsonObject.getString("first_name");
            } catch (Exception e) {
                strFirstName = "";
            }

            try {
                strLastName = jsonObject.getString("middle_name") + " " + jsonObject.getString("last_name");
            } catch (Exception e) {
                strLastName = "";
            }
            try {
                strEmail = jsonObject.getString("email");
            } catch (Exception e) {
                strEmail = "";
            }
            try {
                JSONArray phoneArray = jsonObject.optJSONArray("phones");
                phones.clear();
                for (int i = 0; i < phoneArray.length(); i++) {
                    phones.add(phoneArray.get(i).toString());
                }
            } catch (Exception e) {
                phones.clear();
            }

            try {
                nType = jsonObject.getInt("type");
                isPreFavorite = jsonObject.getBoolean("is_favorite");
                isSelected = jsonObject.getBoolean("is_favorite");
                nSelectingType = nType;
            } catch (Exception e) {
                nType = TYPE_ENTITY;
                nSelectingType = TYPE_ENTITY;
            }

            try {
                JSONArray fieldsArray = jsonObject.optJSONArray("fields");
                for (int i = 0; i < fieldsArray.length(); i++) {
                    JSONObject fieldObject = fieldsArray.getJSONObject(i);
                    try {
                        int fieldId = fieldObject.optInt("id", -1);

                        String strFieldName = fieldObject.getString("field_name");
                        String strFieldValue = fieldObject.getString("field_value");

                        ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
                        for (int j = 0; j < infoList.size(); j++) {
                            GreyAddInfoFragment.InfoItem infoItem = infoList.get(j);
                            if (infoItem.strInfoName.equals(strFieldName))
                                infoItem.nFieldId = fieldId;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    strNotes = jsonObject.getString("notes");
                } catch (Exception e) {
                    strNotes = "";
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initInfoItemFragment(String object)
    {
        EntityInfoVO entityInfo = null;
        entityInfo = new EntityInfoVO();

        ArrayList<EntityInfoDetailVO> entityDetails = null;
        entityDetails = new ArrayList<EntityInfoDetailVO>();

        try {
            JSONObject jsonObject = new JSONObject(object);
            try {
                JSONArray fieldsArray = jsonObject.optJSONArray("fields");
                for (int i = 0; i < fieldsArray.length(); i++) {
                    JSONObject fieldObject = fieldsArray.getJSONObject(i);
                    try {
                        int fieldId = fieldObject.optInt("id", -1);

                        String strFieldName = fieldObject.getString("field_name");
                        String strFieldValue = fieldObject.getString("field_value");
                        String strFieldType = fieldObject.getString("field_type");

                        EntityInfoDetailVO entityDetail = null;
                        entityDetail = new EntityInfoDetailVO();
                        entityDetail.setFieldName(strFieldName);
                        entityDetail.setId(fieldId);
                        entityDetail.setType(strFieldType);
                        entityDetail.setValue(strFieldValue);
                        entityDetails.add(entityDetail);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    strNotes = jsonObject.getString("notes");
                } catch (Exception e) {
                    strNotes = "";
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        entityInfo.setEntityInfoDetails(entityDetails);
        entityInfo.setAddressConfirmed(true);
        entityInfo.setId(1);
        entityInfo.setLongitude("36.25");
        entityInfo.setLatitude("40.35");
        infoListFragment = GreyAddInfoFragment.newInstance(entityInfo, false);
        infoListFragment.setOnProfileFieldItemsChangeListener(this);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();

        /*
        ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
        for (int j = 0; j < infoList.size(); j++) {
            GreyAddInfoFragment.InfoItem infoItem = infoList.get(j);
            if (infoItem.strInfoName.trim().equals(""))
                infoItem.setVisibility(false);
            else
                infoItem.setVisibility(true);
        }
        */

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
        isFavouriteChanged = false;

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

            tiledProfilePhoto.refreshOriginalBitmap();
            tiledProfilePhoto.setImageUrl(strPhotoUrl, imgLoader);
            tiledProfilePhoto.invalidate();

            strTempPhotoPath = "";
            isPhotoChanged = false;
        }

        nSelectingType = nType;

        updateUIFromEditable();
        refreshUiTopMenuButtons();

        ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
        for (int j = 0; j < infoList.size(); j++) {
            GreyAddInfoFragment.InfoItem infoItem = infoList.get(j);
            if (infoItem.isPending == true)
                infoListFragment.resetOriginalItem(infoItem.strInfoName);
            else
                infoListFragment.removeInfoItem(infoItem.strInfoName);
        }

        infoListFragment.updateInfoView(false);
        infoListFragment.setAllPending(false);
        showContactInfo();

        MyApp.getInstance().hideKeyboard(rootLayout);

        isSelected = isPreFavorite;
        if (isPreFavorite == false)
            imgFavorite.setImageResource(R.drawable.img_unfavorite);
        else
            imgFavorite.setImageResource(R.drawable.img_contact_favorite);
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(isEditable && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            if(addFieldOverlayView.getVisibility() == View.VISIBLE)
            {
                imgDimBackground.setVisibility(View.GONE);
                addFieldOverlayView.hideView();
                btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);

                edtFullName.setCursorVisible(true);
                edtFullName.setFocusableInTouchMode(true);
                edtFullName.setFocusable(true);
            } else
                cancelEditing();
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

            tiledProfilePhoto.setImageUrl(strPhotoUrl, imgLoader);
            tiledProfilePhoto.invalidate();
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

                tiledProfilePhoto.refreshOriginalBitmap();
                tiledProfilePhoto.setImageUrl(strTempPhotoPath, imgLoader);
                tiledProfilePhoto.invalidate();
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
                    String email = null;

                    phones.clear();

                    ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
                    for (int i = 0; i < infoList.size(); i++) {
                        GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
                        if (infoItem == null) return;
                        if (!infoItem.isVisible)
                            infoListFragment.removeInfoItem(infoItem.strInfoName);
                        else
                        {
                            if(email==null && infoItem.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL))
                                email = infoItem.strInfoValue;
                            if(infoItem.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_PHONE))
                                phones.add(infoItem.strInfoValue);
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

                showContactInfo();
                updateUIFromEditable();
                infoListFragment.updateInfoView(false);
                infoListFragment.setAllPending(false);
                refreshUiTopMenuButtons();

            }
        }
    };
    private void updateContactInfo(boolean updateNotes)
    {
        isPreFavorite = isSelected;
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

                ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
                for (int i = 0; i < infoList.size(); i++) {
                    GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
                    JSONObject obj = new JSONObject();

                    obj.put("field_name" , infoItem.strInfoName);
                    obj.put("field_type" , infoItem.strFieldType);
                    if (infoItem.isVisible)
                        obj.put("field_value" , infoItem.strInfoValue.trim());
                    else
                        obj.put("field_value" , "");
                    filedsArray.put(obj);
                }

                jsonData.put("fields", filedsArray);

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

                MyApp.getInstance().getAllContactItemsFromDatabase();

                //call request for update contact info
                SyncRequest.updateDetail(jsonData, updateContactInfoCallback);

            }
            else//if this is not updaing the "Notes" , then update the contact info from editTexts
            {
                //if name is empty then show alert
                if(edtFullName.getText().toString().trim().equals("")) {
                    //alert dialog to notify should input at least one item info
                    MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this , R.string.str_alert_for_input_name_item , null);
                    return;
                }
                EntityInfoVO entityInfoVO = null;
                if(infoListFragment != null)
                    entityInfoVO = infoListFragment.saveEntityInfo(GreyContactOne.this, true);
                if(entityInfoVO == null)
                    return;

                infoListFragment.getEditingInfoItemValues();

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

                //find the first email address
                String email = "";
                ArrayList<GreyAddInfoFragment.InfoItem> infoList = infoListFragment.getInfoList();
                for (int i = 0; i < infoList.size(); i++) {
                    GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
                    if (infoItem != null && !infoItem.strInfoValue.trim().equals("")) {
                        if(email.equals("") &&
                                infoItem.strFieldType.equals(ConstValues.PROFILE_FIELD_TYPE_EMAIL)) {
                            if (infoItem.isVisible)
                                email = infoItem.strInfoValue.toString().trim();
                            else
                                email = "";
                        }
                        jsonData.put("email" , email);
                    }
                }

                JSONArray fieldsArray = new JSONArray();
                for (int i = 0; i < infoList.size(); i++) {
                    GreyAddInfoFragment.InfoItem infoItem = infoList.get(i);
                    if (infoItem != null && !infoItem.strInfoValue.trim().equals("")) {
                        JSONObject obj = new JSONObject();

                        obj.put("field_name" , infoItem.strInfoName);
                        obj.put("field_type" , infoItem.strFieldType);
                        if (infoItem.isVisible)
                            obj.put("field_value" , infoItem.strInfoValue.trim());
                        else
                            obj.put("field_value" , "");
                        fieldsArray.put(obj);
                    }
                }

                jsonData.put("fields", fieldsArray);

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

                                tiledProfilePhoto.refreshOriginalBitmap();
                                tiledProfilePhoto.setImageUrl(strPhotoUrl , imgLoader);
                                tiledProfilePhoto.invalidate();
                                MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this , R.string.str_err_upload_photo , null);
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
            edtFullName.setCursorVisible(true);
            edtFullName.setFocusableInTouchMode(true);
            edtFullName.setFocusable(true);
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

            infoListFragment.hiddenInfoItem(fieldName);
        }
    }

    @Override
    public void onEditTextWatcher() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        showContactInfo();
        updateUIFromEditable();
        refreshUiTopMenuButtons();
        if (infoListFragment != null){
            infoListFragment.updateInfoView(isEditable);
        }
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
        if(!strPhotoUrl.equals("") && strTempPhotoPath.equals("")) {
            imgContactPhoto.setImageUrl(strPhotoUrl, imgLoader);
            tiledProfilePhoto.setImageUrl(strPhotoUrl, imgLoader);
        }

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

            imgEditContact.setVisibility(View.INVISIBLE);

            imgBtnNote.setVisibility(View.GONE);
            btnAddProfileField.setVisibility(View.VISIBLE);
            imgBtnDelete.setVisibility(View.INVISIBLE);
            //bottomLayout.setVisibility(View.GONE);

            updateTypeIcons(nSelectingType);
            updateFavoriteButton(isSelected);
        }
        else {
            edtFullName.setVisibility(View.GONE);
            txtFirstName.setVisibility(View.VISIBLE);
            txtLastName.setVisibility(View.VISIBLE);

            imgEditContact.setVisibility(View.VISIBLE);
            //bottomLayout.setVisibility(View.VISIBLE);
            imgBtnNote.setVisibility(View.VISIBLE);
            btnAddProfileField.setVisibility(View.GONE);
            imgBtnDelete.setVisibility(View.VISIBLE);

            if (imgDimBackground.getVisibility() == View.VISIBLE) {
                imgDimBackground.setVisibility(View.GONE);
                imgDimBackground.setFocusable(false);
                edtFullName.setCursorVisible(true);
                edtFullName.setFocusableInTouchMode(true);
                edtFullName.setFocusable(true);
            }
            addFieldOverlayView.hideView();
            if(isAddFields)
                btnAddProfileField.setImageResource(R.drawable.remove_profile_info_item_button);
            else
                btnAddProfileField.setImageResource(R.drawable.add_profile_info_item_button);

            updateTypeIcons(nType);
            if (isFavouriteChanged == true)
            {
                updateFavoriteButton(isSelected);
                isFavouriteChanged = false;
            }
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
            GreyContactOne.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            GreyContactOne.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
        }
        else if(index == 2)//remove photo
        {
            if (strPhotoUrl.equals("")) {
                MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this, R.string.str_grey_contact_remove_photo_alert, null);
            } else {
                GreyContactRequest.removePhoto(contactId, new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            strTempPhotoPath = "";
                            isPhotoChanged = true;
                            imgContactPhoto.refreshOriginalBitmap();
                            imgContactPhoto.setDefaultImageResId(R.drawable.no_face_grey);
                            imgContactPhoto.setImageUrl("", imgLoader);
                            imgContactPhoto.invalidate();

                            tiledProfilePhoto.refreshOriginalBitmap();
                            tiledProfilePhoto.setDefaultImageResId(R.drawable.no_face_grey);
                            tiledProfilePhoto.setImageUrl("", imgLoader);
                            tiledProfilePhoto.invalidate();
                        } else {
                            MyApp.getInstance().showSimpleAlertDiloag(GreyContactOne.this, "Failed to remove photo", null);
                        }
                    }
                });
            }

        }
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
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                hideKeyboard(edtNotes);
                bIsKeyBoardVisibled = false;
            } else {
                hideKeyboard(edtNotes);
                mCustomDialog.dismiss();
            }
        }
    }

}
