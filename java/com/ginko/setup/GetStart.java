package com.ginko.setup;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ginko.api.request.UserInfoRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserProfileVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.videophotofilter.android.com.TradeCardPhotoEditorSetActivity;

import java.util.ArrayList;

public class GetStart extends MyBaseFragmentActivity implements View.OnClickListener{

    private String strFullName = "";

    private LinearLayout createHomeProfileLayout;
    private LinearLayout createWorkProfileLayout;
    private LinearLayout createBothProfileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getstart);

        this.strFullName = this.getIntent().getStringExtra("fullname");

        createHomeProfileLayout = (LinearLayout)findViewById(R.id.create_home_profile_layout); createHomeProfileLayout.setOnClickListener(this);
        createWorkProfileLayout = (LinearLayout)findViewById(R.id.create_work_profile_layout); createWorkProfileLayout.setOnClickListener(this);
        createBothProfileLayout = (LinearLayout)findViewById(R.id.create_both_profile_layout); createBothProfileLayout.setOnClickListener(this);

        createHintDialog();
    }

    private void createHintDialog()
    {
        CustomHintDialog dialog = new CustomHintDialog(this);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void onBtnGetStart() {
        Intent intent = new Intent(GetStart.this,TradeCardPhotoEditorSetActivity.class);
        intent.putExtra("isSetNewPhotoInfo" , true);
        intent.putExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);
        Bundle bundle = new Bundle();
        UserWholeProfileVO userInfo = new UserWholeProfileVO();
        //init group info of null elements
        UserUpdateVO homeInfo = new UserUpdateVO();
        homeInfo.setGroupName("home");
        homeInfo.setImages(new ArrayList<TcImageVO>());
        homeInfo.setProfileImage("");
        homeInfo.setImages(new ArrayList<TcImageVO>());
        homeInfo.setFields(new ArrayList<UserProfileVO>());
        homeInfo.setVideo(null);
        userInfo.setHome(homeInfo);

        UserUpdateVO workInfo = new UserUpdateVO();
        workInfo.setGroupName("work");
        workInfo.setImages(new ArrayList<TcImageVO>());
        workInfo.setProfileImage("");
        workInfo.setImages(new ArrayList<TcImageVO>());
        workInfo.setFields(new ArrayList<UserProfileVO>());
        workInfo.setVideo(null);
        userInfo.setWork(workInfo);

        userInfo.setFirstName(strFullName);

        userInfo.setUserId(RuntimeContext.getUser().getUserId());
        userInfo.setPhotoUrl(RuntimeContext.getUser().getPhotoUrl());

        bundle.putSerializable("userInfo" , userInfo);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
	}

    private void updateProfileTypeSelection(final int selectedIndex)
    {
        //if select the same profile type again , then goes to create profile screen
        UserInfoRequest.getInfo(new ResponseCallBack<UserWholeProfileVO>() {
            @Override
            public void onCompleted(JsonResponse<UserWholeProfileVO> response) {
                if (response.isSuccess()) {
                    UserWholeProfileVO userInfo = response.getData();
                    if (userInfo != null) {
                        Intent intent = new Intent(GetStart.this, CreatePersonalProfileActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userInfo", userInfo);
                        switch (selectedIndex) {
                            case 1: //home
                                bundle.putString("type", "home");
                                bundle.putBoolean("isNewEntity", true);
                                bundle.putString("createProfileMode", "home");
                                break;

                            case 2: //work
                                bundle.putString("type", "work");
                                bundle.putBoolean("isNewEntity", true);
                                bundle.putString("createProfileMode", "work");
                                break;

                            case 3: //home and work both
                                bundle.putString("type", "home");
                                bundle.putBoolean("isNewEntity", true);
                                bundle.putString("createProfileMode", "both");
                                break;
                        }
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.create_home_profile_layout:
                updateProfileTypeSelection(1);
                break;

            case R.id.create_work_profile_layout:
                updateProfileTypeSelection(2);
                break;

            case R.id.create_both_profile_layout:
                updateProfileTypeSelection(3);
                break;

        }
    }

    class CustomHintDialog extends  Dialog implements View.OnClickListener{
        public CustomHintDialog(Context context)
        {
            super(context);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setCancelable(false);
            setContentView(R.layout.signup_getstart_hint_dialog);

            getWindow().getAttributes().gravity= Gravity.CENTER;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            findViewById(R.id.btnLetsStart).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btnLetsStart)
            {
                dismiss();
            }
        }
    }
}
