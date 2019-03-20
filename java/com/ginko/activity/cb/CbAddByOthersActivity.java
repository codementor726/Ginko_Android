package com.ginko.activity.cb;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ginko.api.request.CBRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.CbEmailVO;

import org.json.JSONObject;

public class CbAddByOthersActivity extends MyBaseActivity implements View.OnClickListener{

    private ImageButton btnPrev , btnConfirm;
	private RelativeLayout bodyLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cb_add_by_others);

		bodyLayout = (RelativeLayout)findViewById(R.id.bodyLayout);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnConfirm = (ImageButton)findViewById(R.id.btnConfirm); btnConfirm.setVisibility(View.GONE); btnConfirm.setOnClickListener(this);
		
		final EditText txtEmail = (EditText)this.findViewById(R.id.txtEmail);
		final EditText txtPassword = (EditText)this.findViewById(R.id.txtPassword);
		EditText txtMailServer = (EditText)this.findViewById(R.id.txtMailServer);
		EditText txtSererType = (EditText)this.findViewById(R.id.txtSererType);
		EditText txtServerPort = (EditText)this.findViewById(R.id.txtServerPort);

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
					txtPassword.requestFocus();
				}
				return false;
			}
		});
		txtServerPort.addTextChangedListener(new TextWatcher() {
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
		txtServerPort.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				addCB();
				return true;
			}
		});

	}

	private void addCB(){
		EditText txtEmail = (EditText)this.findViewById(R.id.txtEmail);
		EditText txtPassword = (EditText)this.findViewById(R.id.txtPassword);
		EditText txtMailServer = (EditText)this.findViewById(R.id.txtMailServer);
		EditText txtSererType = (EditText)this.findViewById(R.id.txtSererType);
		EditText txtServerPort = (EditText)this.findViewById(R.id.txtServerPort);
		
		String email = txtEmail.getText().toString().trim();
		String password = txtPassword.getText().toString().trim();
		String server =txtMailServer.getText().toString().trim();
		String serverType = txtSererType.getText().toString().trim();
		String serverPort = txtServerPort.getText().toString().trim();
		
		if (email.isEmpty() || !Uitils.isEmail(email)){
			Uitils.alert("Please input the correct Email Address.");
			return;
		}
		if (password.isEmpty() ){
			Uitils.alert("Please input the password.");
			return;
		}
		if (server.isEmpty() ){
			Uitils.alert("Please input the server name or IP address.");
			return;
		}
		if (serverType.isEmpty() ){
			Uitils.alert("Please input the server Type.");
			return;
		}
		if (serverPort.isEmpty() ){
			Uitils.alert("Please input the server Port.");
			return;
		}
		
		
		final CbEmailVO cb = new CbEmailVO();
		
//		cb.setProvider(this.provider);
		cb.setAuthType("password");
		cb.setPassword(password);
		cb.setEmail(email);
		cb.setServerAddr(server);
		try {
			cb.setServerPort(Integer.valueOf(serverPort));
		}catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(CbAddByOthersActivity.this , "Invalid server port value..." , Toast.LENGTH_LONG).show();
			return;
		}
		cb.setType(serverType);

		cb.setSharingStatus(4);
		cb.setActive("no");
		CBRequest.saveCB(cb, new ResponseCallBack<JSONObject>() {
			
			@Override
			public void onCompleted(JsonResponse<JSONObject> response) {
				if (response.isSuccess()){
					int cbId = response.getData().optInt("id");
					String valid = response.getData().optString("valid");
					cb.setId(cbId);
					cb.setValid(valid);
					
					Intent intent = new Intent();
					intent.setClass(CbAddByOthersActivity.this,
							SaveCBActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("cb", cb);
                    bundle.putBoolean("isNewCB" , true);
//					bundle.putInt("cb_id", value);
					intent.putExtras(bundle);
					startActivity(intent);
					CbAddByOthersActivity.this.finish();
				}else {
					Toast.makeText(CbAddByOthersActivity.this, "Oops, the CB cant be verified, the email or password is incorrect.", Toast.LENGTH_SHORT).show();
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
