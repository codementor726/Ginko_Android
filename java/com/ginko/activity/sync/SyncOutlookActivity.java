package com.ginko.activity.sync;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ginko.api.request.SyncRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

public class SyncOutlookActivity extends MyBaseActivity implements View.OnClickListener{

    private ImageButton btnPrev;
    private RelativeLayout activityRootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_outlook);

        activityRootView = (RelativeLayout)findViewById(R.id.rootLayout);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);

        final EditText txtEmail = (EditText) this.findViewById(R.id.txtEmail);
        final EditText txtWebmailLink = (EditText) findViewById(R.id.txtWebmailLink);
        //txtWebmailLink.setVisibility(View.GONE);

        txtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String email = txtEmail.getText().toString().trim();
                if (!txtEmail.hasFocus() && Uitils.isEmail(email)) {
                    descoverWebMailLink(txtWebmailLink, email);
                }

            }
        });

        findViewById(R.id.btn_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToImportContacts();
            }
        });

    }

    private void descoverWebMailLink(final EditText txtWebmailLink,
                                     String email) {
        EditText txtUserName = (EditText) findViewById(R.id.txtUserName);

        txtUserName.setText(email);
        // discovery weblink, TODO need correct userName/password
        SyncRequest.discoverOutlookServer(
                email, "", "", new ResponseCallBack<JSONObject>() {

                    @Override
                    public void onCompleted(
                            JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            String webmailLink = response.getData()
                                    .optString("webmail_link", "");
                            if (!webmailLink.isEmpty()) {
                                txtWebmailLink.setText(webmailLink);
                                return;
                            }
                        }
                        txtWebmailLink.setVisibility(View.VISIBLE);
                    }
                });
    }


    private void redirectToImportContacts(){
        EditText txtEmail = (EditText) this.findViewById(R.id.txtEmail);
        EditText txtWebmailLink = (EditText) findViewById(R.id.txtWebmailLink);
        EditText txtUserName = (EditText) findViewById(R.id.txtUserName);
        EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);

        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String userName = txtUserName.getText().toString().trim();
        String webMailLink = txtWebmailLink.getText().toString().trim();

        if (email.isEmpty() || !Uitils.isEmail(email)) {
            Uitils.alert( "Please input the correct Email Address.");
            return;
        }
        if (password.isEmpty()) {
            Uitils.alert("Please input the password.");
            return;
        }
        if (webMailLink.isEmpty()) {
            Uitils.alert("Please input the webmail_link.");
            return;
        }

        Intent intent = new Intent(this,SyncImportAddressBookActivity.class);
        intent.putExtra("import_type","ows");
        intent.putExtra("email",email);
        intent.putExtra("userName",userName);
        intent.putExtra("password",password);
        intent.putExtra("webmail_link",webMailLink);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.getInstance().hideKeyboard(activityRootView);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;
        }
    }
}
