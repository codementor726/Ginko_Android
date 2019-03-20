package com.ginko.activity.directory;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.api.request.CBRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.customview.ActionSheet;
import com.ginko.customview.CustomNetworkImageView;
import com.ginko.customview.InputDialog;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.utils.ImageScalingUtilities;
import com.ginko.vo.DirectoryVO;
import com.google.maps.android.MarkerManager;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirSettingsActivity extends MyBaseFragmentActivity implements
        View.OnClickListener,
        ActionSheet.ActionSheetListener
{
    private final int TAKE_PHOTO_FROM_CAMERA = 4;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int FILTER_PHOTO = 6;

    private LinearLayout activityRootView, domainLayout, methodTypeLayout, scrollLayout;
    private RelativeLayout headerLayout;
    private String strDirname;
    private ImageButton btnBack;
    private Button btnDone, btnCancel;
    private TextView txtViewTitle;
    private EditText txtDirname;
    private ListView domainListView;
    private ImageView btnAddDomain;
    private CheckBox chkPrivate, chkPublic, chkAuto, chkManual;
    private CustomNetworkImageView imgDirProfile;
    private ImageView btnClearSearch;
    private ImageLoader imgLoader;

    private String strProfileImagePath = "";
    private String strTempPhotoPath = "" ;

    private Uri uri;
    private String tempPhotoUriPath = "";
    private String strProfilePhotoUrl = "";
    private String strDirectoryProfilePath = "";

    private ActionSheet takeProfilePhotoActionSheet = null;
    private boolean isTakingProfilePhoto = true;

    private DirectoryVO dirInfo;
    private DirectoryVO newDirInfo = null;
    private Pattern pattern;

    private DomainListAdapter mAdapter;
    private ArrayList<String> domainList = new ArrayList<String>();

    private boolean isKeyboardVisible = false;
    private boolean isCreate = false;
    private boolean isNewDirectory = false;
    private boolean isSharedPrivilege = true;
    private boolean isAutoApproved = true;
    private boolean isRequestBack = true;
    private boolean isPhotoUploaded = false;

    private int m_orientHeight = 0;
    private ProgressHUD progressDialog = null;

    private View.OnClickListener snapProfilePhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setTheme(R.style.ActionSheetStyleIOS7);
            //if(takeProfilePhotoActionSheet == null)
            if(dirInfo.getProfileImage().equals("")) {
                takeProfilePhotoActionSheet = ActionSheet.createBuilder(DirSettingsActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery))
                        .setCancelableOnTouchOutside(true)
                        .setListener(DirSettingsActivity.this)
                        .show();
            }
            else
            {
                takeProfilePhotoActionSheet = ActionSheet.createBuilder(DirSettingsActivity.this, getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.str_cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.home_work_add_info_take_photo),
                                getResources().getString(R.string.home_work_add_info_photo_from_gallery),
                                getResources().getString(R.string.home_work_add_info_remove_photo))
                        .setCancelableOnTouchOutside(true)
                        .setListener(DirSettingsActivity.this)
                        .show();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_settings);

        if(savedInstanceState != null)
        {
            this.dirInfo = (DirectoryVO) savedInstanceState.getSerializable("directory");
            this.isCreate = savedInstanceState.getBoolean("isCreate", false);
            this.isNewDirectory = savedInstanceState.getBoolean("isNewDirectory", false);
        }
        else {
            this.dirInfo = (DirectoryVO) getIntent().getSerializableExtra("directory");
            this.isCreate = getIntent().getBooleanExtra("isCreate", false);
            this.isNewDirectory = getIntent().getBooleanExtra("isNewDirectory", false);
        }

        if(this.dirInfo.getPrivilege()!=null)
            this.isSharedPrivilege = this.dirInfo.getPrivilege()>0?true:false;
        else
            this.isSharedPrivilege = false;

        if(this.dirInfo.getApproveMode()!=null)
            this.isAutoApproved = this.dirInfo.getApproveMode()>0?true:false;
        else
            this.isAutoApproved = false;

        getUIObjects();

        initValues();
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        m_orientHeight = rectgle.bottom;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnNext:
                if (isRequestBack == true)
                    saveAndNext();
                break;
            case R.id.imgDirDomain:
                final String inputErrHint = "Please enter Domain Name";

                final InputDialog inviteEmailDialog = new InputDialog(this,
                        -1,//not sure , default is email input
                        getResources().getString(R.string.str_domain_email_dialog_description) , //title
                        getResources().getString(R.string.str_domain_email_dialog_hint) , //Hint
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
                                    Toast.makeText(DirSettingsActivity.this, inputErrHint, Toast.LENGTH_LONG).show();
                                    hideKeyboard();
                                    return false;
                                }
                                if(!isEmailValid(email))
                                {
                                    Toast.makeText(DirSettingsActivity.this, getResources().getString(R.string.invalid_domain), Toast.LENGTH_SHORT).show();
                                    hideKeyboard();
                                    return false;
                                }

                                addEmailDomain(email);
                                hideKeyboard();

                                return true;
                            }
                        },
                        new InputDialog.OnEditorDoneActionListener() {

                            @Override
                            public void onEditorActionDone(Dialog dialog, String email) {

                                if(email.trim().equalsIgnoreCase(""))
                                {
                                    Toast.makeText(DirSettingsActivity.this, inputErrHint, Toast.LENGTH_LONG).show();
                                    hideKeyboard();
                                    return;
                                }
                                if(!isEmailValid(email))
                                {
                                    Toast.makeText(DirSettingsActivity.this, getResources().getString(R.string.invalid_domain), Toast.LENGTH_SHORT).show();
                                    hideKeyboard();
                                    return;
                                }
                                dialog.dismiss();

                                addEmailDomain(email);
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
            case R.id.chkbox_private:
                chkPrivate.setChecked(true);
                chkPublic.setChecked(false);
                isSharedPrivilege = false;
                updateUIFromMethod(false, isAutoApproved);
                break;
            case R.id.chkbox_public:
                chkPrivate.setChecked(false);
                chkPublic.setChecked(true);
                isSharedPrivilege = true;
                updateUIFromMethod(true, false);
                break;
            case R.id.chkbox_auto:
                chkAuto.setChecked(true);
                chkManual.setChecked(false);
                isAutoApproved = true;
                updateUIFromMethod(false, true);
                break;
            case R.id.chkbox_manual:
                chkManual.setChecked(true);
                chkAuto.setChecked(false);
                isAutoApproved = false;
                updateUIFromMethod(false, false);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("directory", dirInfo);
        outState.putBoolean("isCreate", this.isCreate);
    }

    @Override
    protected void getUIObjects() {
        super.getUIObjects();
        activityRootView = (LinearLayout)findViewById(R.id.rootLayout);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                    }
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                    }
                }
            }
        });
        //For GAD-1264
        activityRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });


        domainLayout = (LinearLayout)findViewById(R.id.domainLayout);
        methodTypeLayout = (LinearLayout)findViewById(R.id.methodTypeLayout);

        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnNext); btnDone.setOnClickListener(this);
        btnCancel = (Button)findViewById(R.id.btnCancel); btnCancel.setOnClickListener(this);

        txtDirname = (EditText)findViewById(R.id.txtName);
        txtDirname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtDirname.hasFocus() && s.length() > 0)
                    btnClearSearch.setVisibility(View.VISIBLE);
                else
                    btnClearSearch.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtDirname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //if enter search keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtDirname.getWindowToken(), 0);

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

        txtDirname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (txtDirname.getText().toString().length() > 0)
                        btnClearSearch.setVisibility(View.VISIBLE);
                    else
                        btnClearSearch.setVisibility(View.GONE);
                    txtDirname.setCursorVisible(true);

                } else {
                    btnClearSearch.setVisibility(View.GONE);
                    txtDirname.setCursorVisible(false);
                }
            }
        });

        btnClearSearch = (ImageView)findViewById(R.id.imgClearSearch); btnClearSearch.setVisibility(View.GONE);
        btnClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDirname.setText("");
                btnClearSearch.setVisibility(View.GONE);
            }
        });

        headerLayout = (RelativeLayout)findViewById(R.id.headerlayout);
        scrollLayout = (LinearLayout)findViewById(R.id.scrollLayout);
        scrollLayout.requestFocus();
        btnClearSearch.setVisibility(View.GONE);

        //For GAD-2077
        scrollLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        txtViewTitle = (TextView)findViewById(R.id.textViewTitle);
        if (!isCreate)
            txtViewTitle.setText("Edit");
        else
            txtViewTitle.setText("Create");

        if (isNewDirectory || isCreate)
        {
            headerLayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            txtViewTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnBack.setImageResource(R.drawable.btn_back_nav_black);
            btnBack.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            btnDone.setTextColor(getResources().getColor(R.color.top_title_text_color));
        }
        else
        {
            headerLayout.setBackgroundColor(getResources().getColor(R.color.green_top_titlebar_color));
            btnBack.setImageResource(R.drawable.btn_back_nav_black);
            btnBack.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setTextColor(getResources().getColor(R.color.black));
            btnDone.setTextColor(getResources().getColor(R.color.black));
        }

        btnAddDomain = (ImageView)findViewById(R.id.imgDirDomain);
        btnAddDomain.setOnClickListener(this);

        if(imgLoader == null)
            imgLoader = MyApp.getInstance().getImageLoader();

        imgDirProfile = (CustomNetworkImageView)findViewById(R.id.imgDirLogo);
        imgDirProfile.setDefaultImageResId(R.drawable.entity_add_logo);
        if(dirInfo.getProfileImage() !=null) {
            strDirectoryProfilePath = dirInfo.getProfileImage();
            imgDirProfile.setImageUrl(strDirectoryProfilePath, imgLoader);
        }
        imgDirProfile.setOnClickListener(snapProfilePhotoClickListener);

        chkPrivate = (CheckBox)findViewById(R.id.chkbox_private); chkPrivate.setOnClickListener(this);
        chkPublic = (CheckBox)findViewById(R.id.chkbox_public); chkPublic.setOnClickListener(this);
        chkAuto = (CheckBox)findViewById(R.id.chkbox_auto); chkAuto.setOnClickListener(this);
        chkManual = (CheckBox)findViewById(R.id.chkbox_manual); chkManual.setOnClickListener(this);

        chkPrivate.setChecked(true);
        chkAuto.setChecked(true);

        domainListView = (ListView) findViewById(R.id.domainList);
        mAdapter = new DomainListAdapter(this);
        domainListView.setAdapter(mAdapter);

        domainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eText = (String)mAdapter.getItem(position);
                changeDomainName(position, eText);
            }
        });

        if(domainList == null) {
            domainList = new ArrayList<String>();
        }
        mAdapter.setListItems(domainList);
    }

    private void changeDomainName(final int position, String domainName)
    {
        final String inputErrHint = "Please enter domain name";

        final InputDialog inviteEmailDialog = new InputDialog(this,
                -1,//not sure , default is email input
                getResources().getString(R.string.str_domain_email_dialog_description) , //title
                getResources().getString(R.string.str_domain_email_dialog_hint) , //Hint
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
                            Toast.makeText(DirSettingsActivity.this, inputErrHint, Toast.LENGTH_LONG).show();
                            hideKeyboard();
                            return false;
                        }
                        if(!isEmailValid(email))
                        {
                            Toast.makeText(DirSettingsActivity.this, getResources().getString(R.string.invalid_domain), Toast.LENGTH_SHORT).show();
                            hideKeyboard();
                            return false;
                        }

                        mAdapter.replaceItem(email, position);
                        hideKeyboard();

                        return true;
                    }
                },
                new InputDialog.OnEditorDoneActionListener() {

                    @Override
                    public void onEditorActionDone(Dialog dialog, String email) {

                        if(email.trim().equalsIgnoreCase(""))
                        {
                            Toast.makeText(DirSettingsActivity.this, inputErrHint, Toast.LENGTH_LONG).show();
                            hideKeyboard();
                            return;
                        }
                        if(!isEmailValid(email))
                        {
                            Toast.makeText(DirSettingsActivity.this, getResources().getString(R.string.invalid_domain), Toast.LENGTH_SHORT).show();
                            hideKeyboard();
                            return;
                        }
                        dialog.dismiss();

                        mAdapter.replaceItem(email, position);
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
        inviteEmailDialog.setTextOnEditBox(domainName);
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

    private void initValues()
    {
        if (dirInfo == null) return;

        if (isCreate)
        {
            txtDirname.setText(dirInfo.getName());
            updateUIFromMethod(isSharedPrivilege, isAutoApproved);


        } else
        {
            txtDirname.setText(dirInfo.getName());
            updateUIFromMethod(isSharedPrivilege, isAutoApproved);
            if (dirInfo.getDomains() != null && !dirInfo.getDomains().equals(""))
            {
                domainList.clear();
                List<String> parseList = Arrays.asList(dirInfo.getDomains().split(","));
                domainList.addAll(parseList);
                mAdapter.notifyDataSetChanged();
            }

        }
    }

    public void hideKeyboard()
    {
        //if(isKeyboardVisible)
        activityRootView.requestFocus();
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activityRootView.getApplicationWindowToken(), 0);
    }

    private void addEmailDomain(String email) {
        String emailAddress = null;
        if (email.contains(".")) {
            emailAddress = email;
            for(int i = 0; i < domainList.size(); i++){
                if(domainList.get(i).equals(emailAddress))
                {
                    MyApp.getInstance().showSimpleAlertDiloag(DirSettingsActivity.this, "This email already exists in the Domain list", null);
                    return;
                }
            }

            addDomainToList(email);
        }

    }

    private void updateUIFromMethod(boolean isPublic, boolean isAutoApproved)
    {
        if (isPublic)
        {
            chkPrivate.setChecked(false);
            chkPublic.setChecked(true);
            chkAuto.setVisibility(View.GONE);
            chkManual.setVisibility(View.GONE);
            domainLayout.setVisibility(View.GONE);
            domainListView.setVisibility(View.GONE);
            methodTypeLayout.setVisibility(View.INVISIBLE);

        } else
        {
            chkPrivate.setChecked(true);
            chkPublic.setChecked(false);
            chkAuto.setVisibility(View.VISIBLE);
            chkManual.setVisibility(View.VISIBLE);
            methodTypeLayout.setVisibility(View.VISIBLE);

            if (isAutoApproved == true)
            {
                chkAuto.setChecked(true);
                chkManual.setChecked(false);
                domainLayout.setVisibility(View.VISIBLE);
                domainListView.setVisibility(View.VISIBLE);
            } else
            {
                chkAuto.setChecked(false);
                chkManual.setChecked(true);
                domainLayout.setVisibility(View.GONE);
                domainListView.setVisibility(View.GONE);
            }
        }
    }

    private void addDomainToList(String item)
    {
        if(domainList == null) {
            domainList = new ArrayList<String>();
            mAdapter.setListItems(domainList);
        }

        domainList.add(item);
        Comparator<String> newComparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int result = 0;
                if(lhs.compareTo(rhs)<0)
                {
                    result = -1;
                }
                else if(lhs.compareTo(rhs) == 0)
                {
                    result = 0;
                }
                else if(lhs.compareTo(rhs)>0)
                {
                    result = 1;
                }
                return result;
            }
        };

        try {
            Collections.sort(domainList, newComparator);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        mAdapter.notifyDataSetChanged();
    }

    private boolean isEmailValid(String email)
    {
        String regExpn = "^[a-zA-Z0-9.]+\\.+[a-z]+";

        CharSequence inputStr = email;

        if (!email.contains("."))
            return false;

        if(pattern == null)
            pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if(index == 0)//take a photo
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder("Directory") +
                    String.valueOf(System.currentTimeMillis()) + ".jpg"));
            tempPhotoUriPath = uri.getPath();
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            DirSettingsActivity.this.startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
        else if(index == 1) //photo from gallery
        {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            DirSettingsActivity.this.startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
        }
        else if(index == 2)//remove photo
        {
            if (!dirInfo.getProfileImage().equals("")) {
                if (isCreate) {
                    strTempPhotoPath = "";
                    strProfileImagePath = "";
                    strProfilePhotoUrl = "";
                    strDirectoryProfilePath = "";
                    imgDirProfile.refreshOriginalBitmap();
                    imgDirProfile.setImageUrl(strProfileImagePath, imgLoader);
                    imgDirProfile.invalidate();
                    dirInfo.setProfileImage("");

                }
                else {
                    DirectoryRequest.removeProfileImage(dirInfo.getId(), new ResponseCallBack<Void>() {
                        @Override
                        public void onCompleted(JsonResponse<Void> response) {
                            if (response.isSuccess()) {
                                strTempPhotoPath = "";
                                strProfileImagePath = "";
                                strProfilePhotoUrl = "";
                                strDirectoryProfilePath = "";

                                imgDirProfile.refreshOriginalBitmap();
                                imgDirProfile.setImageUrl(strProfileImagePath, imgLoader);
                                imgDirProfile.invalidate();
                                dirInfo.setProfileImage("");
                            }
                        }
                    });
                }
            } else {
                Uitils.alert(DirSettingsActivity.this, getResources().getString(R.string.str_grey_contact_remove_photo_alert));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMyApp.setCurrentActivity(this);

        if (resultCode == RESULT_OK) {
            switch(requestCode)
            {
                case TAKE_PHOTO_FROM_CAMERA:
                    strTempPhotoPath = tempPhotoUriPath;// uri.getPath();
                    if(!strTempPhotoPath.contains("file://"))
                        strTempPhotoPath = "file://"+strTempPhotoPath;

                    goToFilterScreen();
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
                        if(strTempPhotoPath == null)
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(DirSettingsActivity.this, "Unsupport image file format.", null);
                            return;
                        }
                        if (!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        goToFilterScreen();

                    }
                    break;
                case FILTER_PHOTO:
                    setDirectoryProfilePhoto();
                    break;
            }
        }
        else
        {
            strTempPhotoPath = "";
            strProfileImagePath = "";
        }
    }

    public String method(String str) {
        if (str != null && str.length() > 0 && str.charAt(str.length()-1)==',') {
            str = str.substring(0, str.length()-1);
        }
        return str;
    }

    private void saveAndNext()
    {
        if (txtDirname.getText().toString().trim().equals("")) {
            Uitils.alert(DirSettingsActivity.this, "Name must be set when create a new directory!");
            return;
        }

        dirInfo.setName(txtDirname.getText().toString());
        Integer nApprovMode = isAutoApproved ? 1 : 0;
        final Integer nPrivilege = isSharedPrivilege ? 1 : 0;
        dirInfo.setApproveMode(nApprovMode);
        dirInfo.setPrivilege(nPrivilege);

        if (isAutoApproved == true && isSharedPrivilege == false)
        {
            if (domainList.size() < 1)
            {
                MyApp.getInstance().showSimpleAlertDiloag(DirSettingsActivity.this, "Oops!", R.string.str_alert_dialog_at_least_domain, null);
                return;
            }
            else
            {
                String domainIDs = "";
                for (int i = 0; i < domainList.size(); i++) {
                    domainIDs += domainList.get(i) + ",";
                }

                domainIDs = method(domainIDs);
                dirInfo.setDomains(domainIDs);
            }
        } else
        {
            dirInfo.setDomains("");
        }

        isRequestBack = false;

        if(dirInfo.getId() == null)//if entity is not created yet,then create the entity
        {

            isPhotoUploaded = false;
            DirectoryRequest.createDirectory(dirInfo, new ResponseCallBack<JSONObject>()
            {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response)  {
                    if (response.isSuccess()) {
                        JSONObject object = response.getData();
                        Integer newId = 0;
                        try
                        {
                            newId = object.getInt("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dirInfo.setId(newId);

                        File dirProfilePhotoFile = null;
                        if(!strDirectoryProfilePath.equals("")) {
                            dirProfilePhotoFile = new File(strDirectoryProfilePath);
                            if (dirProfilePhotoFile.exists())
                            {
                                if (progressDialog == null) {
                                    progressDialog = ProgressHUD.createProgressDialog(DirSettingsActivity.this, getResources().getString(R.string.str_uploading_now),
                                            true, false, null);
                                    progressDialog.show();
                                } else {
                                    progressDialog.show();
                                }

                                DirectoryRequest.uploadProfileImage(dirInfo.getId(), dirProfilePhotoFile, true, new ResponseCallBack<JSONObject>() {
                                    @Override
                                    public void onCompleted(JsonResponse<JSONObject> response) {
                                        isPhotoUploaded = true;
                                        if (response.isSuccess()) {
                                            JSONObject data = response.getData();
                                            try {
                                                String photoUrl = data.getString("image_url");
                                                System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                                strProfileImagePath = strTempPhotoPath;
                                                dirInfo.setProfileImage(photoUrl);
                                                strTempPhotoPath = "";

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                strTempPhotoPath = "";
                                            }
                                        } else {
                                        }

                                        isRequestBack = true;
                                        Intent dirProfilePreviewIntent = new Intent(DirSettingsActivity.this , DirAdminPreviewActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("directory", dirInfo);
                                        if (isNewDirectory || isCreate)
                                            bundle.putBoolean("isCreate", true);
                                        else
                                            bundle.putBoolean("isCreate", false);
                                        bundle.putInt("privilege", nPrivilege);

                                        dirProfilePreviewIntent.putExtras(bundle);
                                        startActivity(dirProfilePreviewIntent);
                                        //finish();

                                    }
                                });
                            } else {
                                isPhotoUploaded = true;
                                isRequestBack = true;
                            }
                        } else
                            isPhotoUploaded = true;
                            if (isPhotoUploaded == true) {
                                isRequestBack = true;
                                Intent dirProfilePreviewIntent = new Intent(DirSettingsActivity.this, DirAdminPreviewActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("directory", dirInfo);
                                bundle.putBoolean("isCreate", isCreate);
                                bundle.putInt("privilege", nPrivilege);
                                dirProfilePreviewIntent.putExtras(bundle);
                                startActivity(dirProfilePreviewIntent);
                            }
                    } else {
                        isRequestBack = true;
                        if(progressDialog != null)
                            progressDialog.dismiss();
                    }
                }
            });
        } else
        {
            final String profileUrl = dirInfo.getProfileImage();
            dirInfo.setProfileImage("");
            DirectoryRequest.updateDirectory(dirInfo, new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            isRequestBack = true;
                            if (response.isSuccess()) {
                                if (progressDialog != null)
                                    progressDialog.dismiss();

                                //Intent entityProfilePreviewIntent = new Intent(EntityEditActivity.this , EntityProfilePreviewActivity.class);
                                Intent dirProfilePreviewIntent = new Intent(DirSettingsActivity.this, DirAdminPreviewActivity.class);
                                Bundle bundle = new Bundle();
                                dirInfo.setProfileImage(profileUrl);
                                bundle.putSerializable("directory", dirInfo);
                                bundle.putBoolean("isCreate", isCreate);
                                bundle.putInt("privilege", nPrivilege);
                                dirProfilePreviewIntent.putExtras(bundle);
                                startActivity(dirProfilePreviewIntent);
                            } else {
                                if (progressDialog != null)
                                    progressDialog.dismiss();
                                Uitils.alert(DirSettingsActivity.this, response.getErrorMessage());
                            }
                        }
                    });

        }
    }

    private void goToFilterScreen()
    {
        Intent intent = new Intent(DirSettingsActivity.this , PersonalProfilePhotoFilterActivity.class);
        intent.putExtra("imagePath" , strTempPhotoPath);
        File filteredImagePath = null;
        filteredImagePath = new File(RuntimeContext.getAppDataFolder("Directory") +
                    String.valueOf("directory"+".jpg"));
        try {
            if (filteredImagePath.exists()) {
                filteredImagePath.delete();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        strTempPhotoPath = filteredImagePath.getAbsolutePath();
        intent.putExtra("saveImagePath", strTempPhotoPath);
        intent.putExtra("groupType", "Directory");

        intent.putExtra("aspect_x", 1);
        intent.putExtra("aspect_y", 1);
        startActivityForResult(intent , FILTER_PHOTO);
    }

    private void setDirectoryProfilePhoto()
    {
        if(!strTempPhotoPath.startsWith("file://"))
            strTempPhotoPath = "file://"+strTempPhotoPath;

        File photoFile = null;
        try {
            photoFile = new File(new URI(strTempPhotoPath.replaceAll(" ", "%20")));

            if(!photoFile.exists())
                return;

            String zippedPhotoFile = ImageScalingUtilities.decodeFile(photoFile.getAbsolutePath(), 300, 300);
            if(zippedPhotoFile.equalsIgnoreCase(""))
                return;

            photoFile = new File(zippedPhotoFile);
            final String strUploadPhotoPath = photoFile.getAbsolutePath();

            if(dirInfo.getId() == null)//if entity is not created yet, then save the entity profile photo path
            {
                strDirectoryProfilePath = photoFile.getAbsolutePath();
                imgDirProfile.refreshOriginalBitmap();
                imgDirProfile.setImageUrl("file://" + strUploadPhotoPath, imgLoader);
                dirInfo.setProfileImage(strUploadPhotoPath);
                imgDirProfile.invalidate();

                return;
            }


            if(!strUploadPhotoPath.equals("") && photoFile.exists())
            {
                DirectoryRequest.uploadProfileImage(dirInfo.getId(), photoFile, true, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject data = response.getData();
                            try {
                                String photoUrl = data.getString("image_url");
                                System.out.println("---Uploaded photo url = " + photoUrl + " ----");
                                strProfileImagePath = strTempPhotoPath;
                                strProfilePhotoUrl = photoUrl;
                                dirInfo.setProfileImage(photoUrl);
                                imgDirProfile.refreshOriginalBitmap();
                                imgDirProfile.setImageUrl("file://" + strUploadPhotoPath, imgLoader);
                                imgDirProfile.invalidate();
                                strTempPhotoPath = "";

                            } catch (Exception e) {
                                e.printStackTrace();
                                strTempPhotoPath = "";
                            }
                        } else {
                            strTempPhotoPath = "";
                            strProfileImagePath = "";
                            imgDirProfile.refreshOriginalBitmap();
                            imgDirProfile.setImageUrl(strProfileImagePath, imgLoader);
                            imgDirProfile.invalidate();
                            MyApp.getInstance().showSimpleAlertDiloag(DirSettingsActivity.this, R.string.str_err_upload_photo, null);
                        }
                    }
                });

            }
        }
        catch(URISyntaxException e)
        {
            e.printStackTrace();

        }
    }
}
