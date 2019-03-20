package com.ginko.activity.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.api.request.UserRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

public class ChangePasswordActivity extends MyBaseActivity implements View.OnClickListener{

    private ImageButton btnPrev;
    private EditText et_password, et_new_password, et_new_password1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        findViewById(R.id.btn_changepwd).setOnClickListener(this);

        et_password      = (EditText)findViewById(R.id.et_password);
        et_new_password  =(EditText)findViewById(R.id.et_new_password);
        et_new_password1 = (EditText)findViewById(R.id.et_new_password1);
        et_new_password1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                    changePassword();
                return false;
            }
        });
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
    }

   @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPrev:
                finish();
                break;
            case R.id.btn_changepwd:
                changePassword();
                break;

        }
    }

    private void changePassword()
    {
        String password = ((EditText) findViewById(R.id.et_password)).getText().toString().trim();
        String newPassword = ((EditText) findViewById(R.id.et_new_password)).getText().toString().trim();
        String newPassword1 = ((EditText) findViewById(R.id.et_new_password1)).getText().toString().trim();
        if (password.isEmpty() || newPassword.isEmpty() || newPassword1.isEmpty()) {
            Uitils.alert("Oops! All fields must be completed.");
            return;
        }
        if (!newPassword.equals(newPassword1)) {
            Uitils.alert("Oops! New passwords do not match. Please re-enter");
            return;
        }

        if(password.length()<6 || newPassword.length()<6 || newPassword1.length()<6)
        {
            Uitils.alert(ChangePasswordActivity.this , getResources().getString(R.string.str_alert_invalid_password_length));
            return;
        }

        UserRequest.changePassword(password, newPassword, new ResponseCallBack<Void>() {
            @Override
            public void onCompleted(JsonResponse<Void> response) {
                if (!response.isSuccess()) {
                    if (response.getErrorCode() == 111) {
                        Uitils.alert("Oops! Current password does not match our records. Please re-enter.");
                    } else {
                        Uitils.alert(response.getErrorMessage());
                    }
                    return;
                }
                Uitils.alert("Password successfully changed!", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChangePasswordActivity.this.finish();
                    }
                });

            }
        });
    }
}
