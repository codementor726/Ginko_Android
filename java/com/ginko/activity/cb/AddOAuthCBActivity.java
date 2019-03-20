package com.ginko.activity.cb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ginko.api.request.CBRequest;
import com.ginko.common.ImageButtonTab;
import com.ginko.common.Logger;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddOAuthCBActivity extends MyBaseActivity {
    private final int VALIDATE_LINK_ACTIVITY = 3;

	private String provider;
    private ImageButton btnPrev , btnConfirm;
    private EditText edtMail;

    private Pattern pattern;
    private TextView txtMainTitle;

    private boolean isDirectory = false;

    public static AddOAuthCBActivity instance;

    public static AddOAuthCBActivity getInstatnce()
    {
        return AddOAuthCBActivity.instance;
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_cb);

        Intent intent = this.getIntent();
        if(intent.hasExtra("isDirectory"))
            isDirectory = intent.getBooleanExtra("isDirectory" , true);
        else
            isDirectory = false;

        AddOAuthCBActivity.instance = this;

        txtMainTitle = (TextView)findViewById(R.id.txtTitle);
        if (isDirectory)
            txtMainTitle.setText(R.string.title_activity_cb_directory);

		provider = this.getIntent().getExtras().getString("provider");

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String email = edtMail.getText().toString().trim();
                authIt(email);
            }
        });

		ImageView logo = (ImageView) this
				.findViewById(R.id.img_cb_provider_logo);
		TextView txtTitle = (TextView) this
				.findViewById(R.id.cb_add_title);

		if (provider.equalsIgnoreCase("google")) {
			logo.setImageResource(R.drawable.cb_import_gmail);
			txtTitle.setText(R.string.title_add_gmail_cb);

		} else if (provider.equalsIgnoreCase("yahoo")) {
			logo.setImageResource(R.drawable.cb_import_yahoo);
			txtTitle.setText(R.string.title_add_yahoo_cb);

		} else if (provider.equalsIgnoreCase("live")) {
			logo.setImageResource(R.drawable.cb_import_msn);
			txtTitle.setText(R.string.title_add_live_cb);
		} else if (provider.equalsIgnoreCase("outlook")) {
            logo.setImageResource(R.drawable.cb_import_outlook);
            txtTitle.setText(R.string.title_add_outlook_cb);
        }


        edtMail = (EditText) findViewById(R.id.txtEmail);
        edtMail.setCursorVisible(false);

        edtMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtMail.setCursorVisible(true);
            }
        });

        edtMail.addTextChangedListener(new TextWatcher() {

             @Override
             public void beforeTextChanged(CharSequence s, int start, int count,
                                           int after) {
             }

             @Override
             public void onTextChanged(CharSequence s, int start, int before,
                                       int count) {
                 if(s.length()>0)
                     btnConfirm.setVisibility(View.VISIBLE);
                 else
                     btnConfirm.setVisibility(View.GONE);

             }

             @Override
             public void afterTextChanged(Editable s) {
                 // TODO Auto-generated method stub
             }
         }
        );

        edtMail.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
                hideKeyboard();
				String email = edtMail.getText().toString().trim();
				authIt(email);
				return true;
			}
		});
	}

    private void hideKeyboard()
    {
        //Hide soft keyboard
        InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtMail.getWindowToken(), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AddOAuthCBActivity.instance = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK && data!=null)
        {
            switch (requestCode)
            {
                case VALIDATE_LINK_ACTIVITY:
                    if (data.getBooleanExtra("isSuccess", false))
                    {
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isSuccess", true);
                        resultIntent.putExtras(bundle);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                    break;
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

    protected void authIt(final String email) {
        if(email.compareTo("") !=0 && !isEmailValid(email))
        {
            MyApp.getInstance().showSimpleAlertDiloag(AddOAuthCBActivity.this, getResources().getString(R.string.invalid_email_address), null);
            return;
        }
        if (isDirectory)
        {
            Intent intent = new Intent();
            intent.setClass(AddOAuthCBActivity.this,
                    OAuthWebView.class);
            intent.putExtra("email", email);
            intent.putExtra("provider", provider);
            intent.putExtra("isDirectory", true);
            startActivityForResult(intent, VALIDATE_LINK_ACTIVITY);
        }
        else
        {
            CBRequest.getOAuthUrl(email, this.provider,
                    new ResponseCallBack<JSONObject>() {

                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                String authUrl = response.getData().optString(
                                        "requestUrl");
                                Logger.error(authUrl);

                                Intent intent = new Intent();
                                intent.setClass(AddOAuthCBActivity.this,
                                        OAuthWebView.class);

                                intent.putExtra("requestUrl", authUrl);
                                intent.putExtra("email", email);
                                intent.putExtra("provider", provider);
                                startActivity(intent);
                                finish();
                            }

                        }
                    });
        }

	}

}
