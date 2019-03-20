package com.ginko.activity.sync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.CBRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.common.Logger;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

public class SyncOAuthActivity extends MyBaseActivity {

    private String provider;
    private EditText textEmail;
    private RelativeLayout activityRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_oauth);

        activityRootView = (RelativeLayout)findViewById(R.id.rootLayout);

        provider = this.getIntent().getExtras().getString("provider");

        ImageButton btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView logo = (ImageView) this
                .findViewById(R.id.img_cb_provider_logo);
        TextView txtTitle = (TextView) this
                .findViewById(R.id.cb_add_title);

        if (provider.equalsIgnoreCase("google")) {
            logo.setImageResource(R.drawable.cb_import_gmail);
            txtTitle.setText(R.string.title_sync_gmail);

        } else if (provider.equalsIgnoreCase("yahoo")) {
            logo.setImageResource(R.drawable.cb_import_yahoo);
            txtTitle.setText(R.string.title_sync_yahoo);

        } else if (provider.equalsIgnoreCase("live")) {
            logo.setImageResource(R.drawable.cb_import_msn);
            txtTitle.setText(R.string.title_sync_live);
        }

        findViewById(R.id.btn_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textEmail = (EditText) findViewById(R.id.txtEmail);
                String email = textEmail.getText().toString().trim();
                if(email.isEmpty()){
                    Uitils.alert("Please enter a correct email address.");
                    return;
                }
                authIt(email);
            }
        });
    }

    protected void authIt(final String email) {

        SyncRequest.getOAuthUrl(email, this.provider,
                new ResponseCallBack<JSONObject>() {

                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            String authUrl = response.getData().optString(
                                    "requestUrl");
                            Logger.error(authUrl);
                            Intent intent = new Intent();
                            intent.setClass(SyncOAuthActivity.this,
                                    SyncOAuthWebActivity.class);

                            intent.putExtra("title", getResources().getString(R.string.title_activity_sync_home));
                            intent.putExtra("requestUrl", authUrl);
                            intent.putExtra("email", email);
                            intent.putExtra("provider", provider);
                            startActivity(intent);
                            SyncOAuthActivity.this.finish();
                        }

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.getInstance().hideKeyboard(activityRootView);
    }
}
