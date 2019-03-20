package com.ginko.activity.cb;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ginko.api.request.CBRequest;
import com.ginko.api.request.SyncRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.CbEmailVO;

import org.json.JSONObject;

public class CBAddByOWSctivity extends MyBaseActivity implements View.OnClickListener{

    private ImageButton btnPrev , btnConfirm;
	private EditText txtUserName;
	private RelativeLayout bodyLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cb__add__ows);

		bodyLayout = (RelativeLayout)findViewById(R.id.bodyLayout);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setVisibility(View.GONE); btnConfirm.setOnClickListener(this);

		final EditText txtEmail = (EditText) this.findViewById(R.id.txtEmail);
		final EditText txtWebmailLink = (EditText) findViewById(R.id.txtWebmailLink);
		txtUserName = (EditText) findViewById(R.id.txtUserName);
		//txtWebmailLink.setVisibility(View.INVISIBLE);

		txtEmail.requestFocus();
		txtEmail.setCursorVisible(false);
		txtEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				txtEmail.setCursorVisible(true);
			}
		});
		txtEmail.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					txtUserName.requestFocus();
				}
				return false;
			}
		});
		txtEmail.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				String email = txtEmail.getText().toString().trim();
				if (!txtEmail.hasFocus() && Uitils.isEmail(email)) {
					descoverWebMailLink(txtWebmailLink, email);
				}

			}
		});
		txtEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length()>0)
					btnConfirm.setVisibility(View.VISIBLE);
				else
					btnConfirm.setVisibility(View.GONE);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		
		EditText txtPassword = (EditText) this.findViewById(R.id.txtPassword);
		OnEditorActionListener l = new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE){
					addCB();
				}
				else if (actionId == EditorInfo.IME_ACTION_NEXT){
					txtWebmailLink.requestFocus();
				}
				return true;
			}
		};
		txtPassword.setOnEditorActionListener(l);
		txtWebmailLink.setOnEditorActionListener(l);
	}

	private void descoverWebMailLink(final EditText txtWebmailLink,
			String email) {


		txtUserName.setText(email);
		// discovery weblink, TODO need correct userName/password
		SyncRequest.discoverOutlookServer(
				email,"","", new ResponseCallBack<JSONObject>() {

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

	private void addCB() {
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
			Uitils.alert("Please input the WebMail link.");
			return;
		}

		final CbEmailVO cb = new CbEmailVO();

		// cb.setProvider(this.provider);
		cb.setAuthType("password");
		cb.setPassword(password);
		cb.setEmail(email);
		cb.setServerAddr(webMailLink);
		cb.setType("OWA"); // TODO server don't support OWA...
		cb.setUsername(userName);
		cb.setSharingStatus(4);
		cb.setActive("no");
		CBRequest.saveCB(cb, new ResponseCallBack<JSONObject>() {

			@Override
			public void onCompleted(JsonResponse<JSONObject> response) {
				if (response.isSuccess()) {
					int cbId = response.getData().optInt("id");
					String valid = response.getData().optString("valid");
					cb.setId(cbId);
					cb.setValid(valid);

					Intent intent = new Intent();
					intent.setClass(CBAddByOWSctivity.this,
							SaveCBActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("cb", cb);
                    bundle.putBoolean("isNewCB" , true);
                    // bundle.putInt("cb_id", value);
					intent.putExtras(bundle);
					startActivity(intent);
					CBAddByOWSctivity.this.finish();
				}

			}
		});

	}

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnPrev:
				MyApp.getInstance().hideKeyboard(bodyLayout);
				finish();
                break;

            case R.id.btnConfirm:
                addCB();
                break;
        }
    }
}
