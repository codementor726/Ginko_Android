package com.ginko.activity.directory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ginko.activity.entity.EntityProfilePreviewAfterEditAcitivity;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.DirectoryVO;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class CreateDirNamesActivity extends MyBaseFragmentActivity implements View.OnClickListener
{
    private EditText edtDirName;
    private ImageButton btnBack;
    private Button btnDone;
    private TextView txtAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dir_names);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        getUIObjects();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnNext:
                checkNamesAndNext();
        }
    }

    @Override
    protected void getUIObjects() {
        super.getUIObjects();

        edtDirName = (EditText)findViewById(R.id.edtNameCreate);
        edtDirName.addTextChangedListener(new TextWatcher() {
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
                 // TODO Auto-generated method stub
                txtAlert.setVisibility(View.GONE);
             }
         }
        );

        btnBack = (ImageButton)findViewById(R.id.btnBack); btnBack.setOnClickListener(this);
        btnDone = (Button)findViewById(R.id.btnNext); btnDone.setOnClickListener(this);
        txtAlert = (TextView)findViewById(R.id.txtAlert);
        txtAlert.setVisibility(View.GONE);

    }

    public void checkNamesAndNext()
    {
        if(edtDirName.getText().toString().trim().equals(""))
        {
            MyApp.getInstance().showSimpleAlertDiloag(CreateDirNamesActivity.this, R.string.str_alert_for_input_directory_name , null);
            return;
        }

        final String txtName = edtDirName.getText().toString();

        DirectoryRequest.checkNameAvailable(txtName, new ResponseCallBack<Void>() {

            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (response.isSuccess()) {
                    if (response.isTrueData())
                    {
                        txtAlert.setVisibility(View.GONE);
                        DirectoryVO newDirInfo = new DirectoryVO();
                        newDirInfo.setDomains("");
                        newDirInfo.setName(txtName);
                        newDirInfo.setPrivilege(0);
                        newDirInfo.setApproveMode(1);
                        newDirInfo.setProfileImage("");
                        Intent dirSettingsIntent = new Intent(CreateDirNamesActivity.this, DirSettingsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("directory", newDirInfo);
                        bundle.putBoolean("isCreate", true);
                        dirSettingsIntent.putExtras(bundle);
                        startActivity(dirSettingsIntent);
                        finish();
                    } else
                    {
                        edtDirName.setText("");
                        txtAlert.setVisibility(View.VISIBLE);
                        String msgText = getResources().getString(R.string.str_alert_directory_mistaken_name);
                        msgText = msgText.replace("This name", txtName);
                        txtAlert.setText(msgText);

                    }
                } else
                {
                    txtAlert.setVisibility(View.VISIBLE);
//                    Uitils.alert(CreateDirNamesActivity.this, getResources().getString(R.string.str_alert_directory_mistaken_name));
                    String msgText = getResources().getString(R.string.str_alert_directory_mistaken_name);
                    msgText = msgText.replace("This name", txtName);
                    Uitils.alert(CreateDirNamesActivity.this, msgText);
                    edtDirName.setText("");
                }
            }
        });
    }
}
