package com.ginko.activity.profiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;
import com.ginko.activity.entity.EntityInviteContactActivity;
import com.ginko.activity.entity.ViewEntityPostsActivity;
import com.ginko.activity.im.ImInputEditTExt;
import com.ginko.api.request.EntityRequest;
import com.ginko.common.Logger;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.fragments.UserEntityProfileFragment;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.UserEntityProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONException;
import org.json.JSONObject;


public class UserEntityProfileActivity extends MyBaseFragmentActivity implements
                                        View.OnClickListener ,
                                        CustomSizeMeasureView.OnMeasureListner
{

    //UI objects
    private ImageButton btnPrev;
    private ImageView btnViewEntityPosts , btnFollowEntity , btnInviteContact, btnNote;
    private CustomSizeMeasureView sizeMeasureView;

    private AlertDialog followEntityDialog , unfollowEntityDialog;
    //Variables
    private UserEntityProfileFragment infoListFragment;

    private UserEntityProfileVO entity;

    private String strEntityNotes = "";

    private int errorCode  = 0;
    private String errorMsg;

    private boolean isFollowedThisEntity = false;
    private boolean isFavorite = false;
    private boolean isNoLetter = false;
    private boolean isMultiLocations = false;

    private int currIndex = 0;
    private int contactID = 0;
    private ImageLoader imgLoader;
    private int infoId = 0;

    private CustomDialog mCustomDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entity_profile);

        Intent intent = this.getIntent();
        contactID = intent.getIntExtra("contactID", 0);
        isFavorite = intent.getBooleanExtra("isFavorite", false);
        isNoLetter = intent.getBooleanExtra("isNoLetter", false);
        //String strEntityInfo = intent.getStringExtra("entityJson");
        entity = (UserEntityProfileVO)intent.getSerializableExtra("entityJson");
        isFollowedThisEntity = intent.getBooleanExtra("isfollowing_entity" , false);
        isMultiLocations = intent.getBooleanExtra("isMultiLocations", false);
        infoId = intent.getIntExtra("infoID",0);
        //parseEntityInfo(strEntityInfo);

        if(entity == null)
        {
            return;
        }
        strEntityNotes = entity.getNotes();
        if (strEntityNotes.equalsIgnoreCase("null"))
            strEntityNotes = "";
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
                EntityRequest.updateNotes(entity.getId(), mCustomDialog.getEdtNotes().getText().toString().trim(), new ResponseCallBack<Void>() {
                    @Override
                    public void onCompleted(JsonResponse<Void> response) {
                        if (response.isSuccess()) {
                            strEntityNotes = mCustomDialog.getEdtNotes().getText().toString().trim();
                            hideKeyboard(mCustomDialog.getEdtNotes());
                            mCustomDialog.bIsKeyBoardVisibled = false;
                            mCustomDialog.dismiss();
                        }
                    }
                });
            }
        }
    };

    private void parseEntityInfo(String strJson)
    {
        JSONObject jData = null;
        try {
            jData = new JSONObject(strJson);
            strEntityNotes = jData.optString("notes" , "");
            if (strEntityNotes.equalsIgnoreCase("null"))
                strEntityNotes = "";

            entity = JsonConverter.json2Object(
                        (JSONObject) jData, (Class<UserEntityProfileVO>) UserEntityProfileVO.class);

        }
        catch (JsonConvertException e) {
            e.printStackTrace();
            entity = null;
        }catch (JSONException e) {
            e.printStackTrace();
            this.errorCode = 9998;
            this.errorMsg = "Illegal entity info.";
            Logger.error(e);
            entity = null;
        }
    }


    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();

        imgLoader = MyApp.getInstance().getImageLoader();

        sizeMeasureView = (CustomSizeMeasureView)findViewById(R.id.sizeMeasureView);
        sizeMeasureView.setOnMeasureListener(this);

        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnViewEntityPosts = (ImageView)findViewById(R.id.btnEntityPosts); btnViewEntityPosts.setOnClickListener(this);
        btnInviteContact = (ImageView)findViewById(R.id.btnInviteContact); btnInviteContact.setOnClickListener(this);
        if(isNoLetter)
            btnInviteContact.setVisibility(View.INVISIBLE);
        btnFollowEntity = (ImageView)findViewById(R.id.imgBtnFollowEntity); btnFollowEntity.setOnClickListener(this);
        btnNote = (ImageView)findViewById(R.id.imgBtnNote); btnNote.setOnClickListener(this);

        if (isMultiLocations)
            btnNote.setVisibility(View.GONE);
        refreshUIFromFollowingStatus();

        initInfoItemFragment();
    }

    private void initInfoItemFragment()
    {
        EntityInfoVO entityInfo = null;

        if(entity.getInfos() == null) return;
        if(entity.getInfos().size() < 1)
        {
            entityInfo = new EntityInfoVO();
            entity.getInfos().add(entityInfo);
        }
        else
        {
            if (infoId == 0)
                entityInfo = entity.getInfos().get(0);
            else {
                for (int i = 0; i < entity.getInfos().size();i++){
                    if (entity.getInfos().get(i).getId() == infoId)
                        entityInfo = entity.getInfos().get(i);
                }
            }
        }

        infoListFragment = UserEntityProfileFragment.newInstance(entity , entityInfo);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fieldsLayout, infoListFragment);
        ft.commit();

        //send parameter
        Bundle bundle = new Bundle();
        bundle.putInt("isUser", 11);
        bundle.putInt("contactID", contactID);
        bundle.putBoolean("isFavorite", isFavorite);
        bundle.putBoolean("isFollowedThisEntity", isFollowedThisEntity);
        bundle.putSerializable("entity", entity);
        bundle.putSerializable("entityInfo", entityInfo);
        infoListFragment.setArguments(bundle);
        ////////////////////////////////////////////
    }

    private void hideKeyboard(EditText edtText)
    {
        //if keyboard is shown, then hide it
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }

        // Creating a pop window for emoticons keyboard

    private void refreshUIFromFollowingStatus()
    {
        if(isFollowedThisEntity)
        {
            btnFollowEntity.setImageResource(R.drawable.leaf_solid); //unfollow entity
            if (!isMultiLocations)
                btnNote.setVisibility(View.VISIBLE);
        }
        else
        {
            btnFollowEntity.setImageResource(R.drawable.leaf_line); //unfollow entity
            if (!isMultiLocations)
                btnNote.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mCustomDialog == null)
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("isFollowEntity",isFollowedThisEntity);
            returnIntent.putExtra("isFavorite",infoListFragment.getisFavorite());
            returnIntent.putExtra("contactID", contactID);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else if(mCustomDialog.isShowing())
            mCustomDialog.dismiss();
        return;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("isFollowEntity",isFollowedThisEntity);
                returnIntent.putExtra("isFavorite",infoListFragment.getisFavorite());
                returnIntent.putExtra("contactID", contactID);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;

            //view entity posts
            case R.id.btnEntityPosts:
                Intent viewEntityPost = new Intent(UserEntityProfileActivity.this , ViewEntityPostsActivity.class);
                viewEntityPost.putExtra("entityName" , entity.getName());
                viewEntityPost.putExtra("entityId" , entity.getId());
                viewEntityPost.putExtra("profileImage" , entity.getProfileImage());
                viewEntityPost.putExtra("isfollowing_entity" , false);
                startActivity(viewEntityPost);
                break;

            //set notes when followed this entity
            case R.id.imgBtnNote:
                mCustomDialog = new CustomDialog(this, btnCloseClickListener, btnConfirmClickListener);
                mCustomDialog.show();
                break;

            //follow this entity
            case R.id.imgBtnFollowEntity:
                if(isFollowedThisEntity)//unfollow entity
                {
                    //confirm dialog to unfollow entity
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    dialog.dismiss();
                                    EntityRequest.unFollowEntity(entity.getId(), new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if (response.isSuccess()) {
                                                isFollowedThisEntity = false;
                                                infoListFragment.setNullFavorite();
                                                refreshUIFromFollowingStatus();
                                            }
                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    if(unfollowEntityDialog == null) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(UserEntityProfileActivity.this);
                        builder.setMessage(getResources().getString(R.string.str_confirm_message_to_unfollow_this_entity)).setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).setCancelable(false);
                        unfollowEntityDialog = builder.show();
                    }
                    else
                        unfollowEntityDialog.show();
                }
                else//follow entity
                {
                    //confirm dialog to follow entity
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    dialog.dismiss();
                                    EntityRequest.followEntity(entity.getId() , new ResponseCallBack<Void>() {
                                        @Override
                                        public void onCompleted(JsonResponse<Void> response) {
                                            if(response.isSuccess())
                                            {
                                                isFollowedThisEntity = true;
                                                infoListFragment.setShowFavorite();
                                                refreshUIFromFollowingStatus();
                                            }
                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    if(followEntityDialog == null) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(UserEntityProfileActivity.this);
                        builder.setMessage(getResources().getString(R.string.str_confirm_message_to_follow_this_entity)).setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).setCancelable(false);
                        followEntityDialog = builder.show();
                    }
                    else
                        followEntityDialog.show();
                }

                break;


            case R.id.btnInviteContact:
                Intent inviteContactIntent = new Intent(UserEntityProfileActivity.this , EntityInviteContactActivity.class);
                inviteContactIntent.putExtra("entityId" , this.entity.getId());
                startActivity(inviteContactIntent);
                break;

        }
    }
            //close set notes popup window
    @Override
    public void onViewSizeMeasure(int width, int height) {
    }


    public class CustomDialog extends Dialog implements ImInputEditTExt.OnEditTextKeyDownListener{
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

        public CustomDialog(Context context,  View.OnClickListener close , View.OnClickListener confirm) {
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
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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

        private void setLayout(){
            edtNotes = (ImInputEditTExt) findViewById(R.id.edtNotes);
            btnClose = (ImageButton)findViewById(R.id.btnNotePopupClose);
            btnConfirm = (ImageButton)findViewById(R.id.btnNotePopupConfirm);
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
            edtNotes.setText(strEntityNotes);
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

                    if(btnConfirm.getVisibility() == View.GONE)
                        btnConfirm.setVisibility(View.VISIBLE);
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        private void setClickListener(View.OnClickListener close , View.OnClickListener confirm){
            if(close!=null && confirm!=null){
                btnClose.setOnClickListener(close);
                btnConfirm.setOnClickListener(confirm);
            }
        }

        public ImInputEditTExt getEdtNotes(){
            return edtNotes;
    }

        @Override
        public void onImEditTextBackKeyDown() {
            if(bIsKeyBoardVisibled) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                hideKeyboard(edtNotes);
                bIsKeyBoardVisibled = false;
            } else {
                //hideKeyboard(edtNotes);
                //mCustomDialog.dismiss();
            }
        }

        @Override
        public void onBackPressed() {
            mCustomDialog.dismiss();
        }
    }
}
