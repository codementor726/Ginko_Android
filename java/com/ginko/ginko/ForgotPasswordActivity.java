package com.ginko.ginko;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ginko.api.request.UserRequest;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;

public class ForgotPasswordActivity extends MyBaseActivity {
    private EditText etxtEmail;
    private LinearLayout backLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		etxtEmail = (EditText)this.findViewById(R.id.etxtEmail);
		etxtEmail.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				ForgotPasswordActivity.this.sendPassword();
				return true;
			}
		});

        backLayout = (LinearLayout)findViewById(R.id.backLayout);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				hideKeyboard();
                finish();
            }
        });
	}

	private void validateEmailInput(EditText etxtEmail) {
		int ecolor = R.color.back; // whatever color you want  
		String estring = "Email format is invalid.";  
//		ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);  
		BackgroundColorSpan bgcspan = new BackgroundColorSpan(ecolor);
		SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);  
		ssbuilder.setSpan(bgcspan, 0, estring.length(), 0);  
		etxtEmail.requestFocus();  
		etxtEmail.setError(ssbuilder);
	}

	public void btnSendPwdClicked(View view) {
		this.sendPassword();
	}

	private AlertDialog alert;
	private void sendPassword() {
		final String emailAddress = etxtEmail.getText().toString().trim();
		if(emailAddress.compareTo("") == 0) {
            Uitils.alert(this , "Please input your Email Address.");
            return;
        }
		if(!Uitils.isEmail(emailAddress)){
			this.validateEmailInput(etxtEmail);
			return;
		}
		
		UserRequest.forgotPassword(emailAddress, new ResponseCallBack<Void>() {
			@Override
			public void onCompleted(JsonResponse<Void> response) {
				if (response.isSuccess()) {
					String msg = getString(R.string.send_pwd_success, emailAddress);
					alert = Uitils.alert(msg, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
    	            	}
					});
                    alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ForgotPasswordActivity.this.finish();
                        }
                    });
				}
				else
				{
					int getErrCode = response.getErrorCode();
					if(getErrCode == 101)
						MyApp.getInstance().showSimpleAlertDiloag(ForgotPasswordActivity.this, emailAddress+" is not in our login records.", null);
				}
			}
		});
	}
	
	@Override
	protected void onPause()
	{
	  super.onPause();
	  if (alert!=null){
		  alert.dismiss();
	  }
	}
	private void hideKeyboard()
	{
		//Hide soft keyboard
		InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etxtEmail.getWindowToken(), 0);
	}

}
