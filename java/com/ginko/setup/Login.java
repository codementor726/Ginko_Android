package com.ginko.setup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.api.request.UserRequest;
import com.ginko.common.ApiManager;
import com.ginko.common.Logger;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.ForgotPasswordActivity;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.UserLoginVO;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends MyBaseFragmentActivity {
  
    //private MainFragment mainFragment;
    private LinearLayout backLayout, forgetLayout;
    private TextView txtLogin;
    private EditText editEmail , editPassword;

    private Pattern pattern;
    private EmailValidationCheckRunnable emailCheckerThread;

    private final int CHECK_EMAIL_VALIDATION = 1;

    private Handler mHandler = new Handler();

    private CallbackManager callbackmanager;

    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

		setContentView(R.layout.login);

        LoginButton facebookAuthButton = (LoginButton)findViewById(R.id.authButton);
        facebookAuthButton.setBackgroundResource(0);
        facebookAuthButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));

        callbackmanager = CallbackManager.Factory.create();

        // Callback registration
        facebookAuthButton.registerCallback(callbackmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                // App code
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());
                                if (response.getError() != null) {
                                    // handle error
                                    System.out.println("ERROR");
                                } else {
                                    System.out.println("Success");
                                    try {

                                        String jsonresult = String.valueOf(object);
                                        System.out.println("JSON Result" + jsonresult);

                                        String str_email = object.getString("email");
                                        String str_id = object.getString("id");
                                        String strName = object.getString("name");

                                        UserRequest.loginWithFacebook(str_email, loginResult.getAccessToken().getToken().toString(),
                                                new ResponseCallBack<UserLoginVO>() {

                                                    @Override
                                                    public void onCompleted(JsonResponse<UserLoginVO> response) {
                                                        if (response.isSuccess()) {
                                                            loginSuccessful(response.getData());
                                                        } else
                                                            ApiManager.showErrorMessage(Login.this);
                                                    }

                                                });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code
                Log.v("LoginActivity", "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });

        forgetLayout = (LinearLayout)findViewById(R.id.blankLayout4);

        final LinearLayout rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (editEmail.isFocused() || editPassword.isFocused()) {
                        Rect outRect = new Rect();
                        EditText mEditText = editEmail;
                        if(editPassword.isFocused())
                            mEditText = editPassword;
                        mEditText.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                            mEditText.clearFocus();
                            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                }
                return false;
            }
        });

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                if(heightDiff > 100)
                    forgetLayout.setVisibility(View.GONE);
                else
                    forgetLayout.setVisibility(View.VISIBLE);

            }
        });
	    editPassword = (EditText) findViewById(R.id.edit_password);
	    editPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				signInUser();
				return true;
			}
		});

        editEmail = (EditText) findViewById(R.id.edit_username);
        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*if(emailCheckerThread == null) {
                    emailCheckerThread = new EmailValidationCheckRunnable();
                }
                else {
                    mHandler.removeCallbacks(emailCheckerThread);
                }
                mHandler.postDelayed(emailCheckerThread , 3000);*/
            }
        });

        editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (emailCheckerThread == null) {
                        emailCheckerThread = new EmailValidationCheckRunnable();
                    } else {
                        mHandler.removeCallbacks(emailCheckerThread);
                    }
                    mHandler.postDelayed(emailCheckerThread, 500);
                }
            }
        });


        backLayout = (LinearLayout)findViewById(R.id.backLayout);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Sign.class);
                startActivity(intent);
                finish();
            }
        });

        txtLogin = (TextView)findViewById(R.id.txtLogin);
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(callbackmanager!=null)
            callbackmanager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(Login.this, Sign.class);
        startActivity(intent);
        finish();
    }

    private void signInUser() {

		final String emailAddress = editEmail.getText().toString();
		String password = editPassword.getText().toString();

		if (emailAddress.isEmpty()) {
			editEmail.requestFocus();
			Uitils.alert("Please enter the email address.");
			return;
		}

		if (password.isEmpty()) {
			editPassword.requestFocus();
			Uitils.alert("Please enter the password.");
			return;
		}
        if(password.length()<6)
        {
            editPassword.requestFocus();
            Uitils.alert(Login.this , getResources().getString(R.string.str_alert_invalid_password_length));
            return;
        }

        String deviceToken = Uitils.getDeviceToken();

		UserRequest.login( emailAddress, password, Uitils.getDeviceUid(), Uitils.getDeviceToken(),
				new ResponseCallBack<UserLoginVO>() {
					@Override
					public void onCompleted(JsonResponse<UserLoginVO> response) {
						if (response.isSuccess()) {
                            UserLoginVO loginVo = response.getData();
                            RuntimeContext.setUser(loginVo);
                            Uitils.storeLoginEmail(Login.this, emailAddress);
                            MyApp.getInstance().initializeGlobalVariables();
                            loginSuccessful(loginVo);  //FIXME
                            // Login.this.finish();
							// Toast.makeText(getApplicationContext(), "Success",
							// Toast.LENGTH_LONG).show();
						} else {
							//ApiManager.showErrorMessage(Login.this);
							//TODO show login error.
                            MyApp.getInstance().showSimpleAlertDiloag(Login.this , response.getErrorMessage() , null);
						}
					}

				});
	}
	
	public void loginSuccessful(UserLoginVO user ) {
        if(user.getSetupPage()==null || user.getSetupPage().equals("") || !user.getSetupPage().equals("2"))
        {
            String sessionId = user.getSessionId();
            this.storeSessionId(sessionId);
            RuntimeContext.setUser(user);
            String fullname = user.getFirstName()+" ";
            fullname += user.getMiddleName();
            fullname = fullname.trim();
            if(user.getLastName()!=null && !user.getLastName().equals(""))
                fullname += " " + user.getLastName();
            fullname = fullname.trim();

            Uitils.storeUserFullname(Login.this, fullname);
            Uitils.storeUserName(Login.this, user.getUserName());

            MyApp.getInstance().fetchAllEntites(2, Login.this);
            MyApp.getInstance().loadContacts(2, Login.this);
            //Intent intent = new Intent(Login.this, GetStart.class);
            //startActivity(intent);
            //Login.this.finish();

        }
        else {
            String sessionId = user.getSessionId();
            this.storeSessionId(sessionId);
            RuntimeContext.setUser(user);
            String fullname = user.getFirstName()+" ";
            fullname += user.getMiddleName();
            fullname = fullname.trim();
            if(user.getLastName()!=null && !user.getLastName().equals(""))
                fullname += " " + user.getLastName();
            fullname = fullname.trim();

            Uitils.storeUserFullname(Login.this, fullname);
            Uitils.storeUserName(Login.this, user.getUserName());

            MyApp.getInstance().fetchAllEntites(3, Login.this);
            MyApp.getInstance().loadContacts(3, Login.this);
            //Intent intent = new Intent();
            //intent.setClass(Login.this, ContactMainActivity.class);
            //Login.this.startActivity(intent);
        }
	}

	public void forgotPwdClicked(View view) {
		Intent intent = new Intent();
		intent.setClass(Login.this, ForgotPasswordActivity.class);
		Login.this.startActivity(intent);
	}
	
	public void storeSessionId(String sessionId){
		Uitils.storeSessionid(this, sessionId);
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

    private class EmailValidationCheckRunnable implements Runnable {
        @Override
        public void run()
        {
            String strEmail = editEmail.getText().toString().trim();
            if(strEmail.compareTo("") !=0 && !isEmailValid(strEmail))
            {
                //Toast.makeText(Login.this , getResources().getString(R.string.invalid_email_address) , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
