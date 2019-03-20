package com.ginko.setup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.ginko.api.request.UserRequest;
import com.ginko.common.ApiManager;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseFragmentActivity;
import com.ginko.ginko.R;
import com.ginko.vo.UserLoginVO;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends MyBaseFragmentActivity {

    private LinearLayout backLayout, hiddenLayout;
    private TextView txtTitle;
    private TextView txtSignUp;
    private EditText editName , editPassword , editEmail;
    private TextView txtTermsPolicy;
    private WebView innerWebPage;
    private Pattern pattern;

    private ScrollView mScrollView;
    private CallbackManager callbackmanager;
    private boolean isKeyboardVisible = false;
    private int m_orientHeight = 0;
    private int originalSize = 0;
    private LinearLayout rootLayout;
    private RelativeLayout bodyLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.signup);

        txtTitle = (TextView)findViewById(R.id.textViewTitle);
        innerWebPage = (WebView)findViewById(R.id.innerWebPage);
        innerWebPage.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                innerWebPage.loadData(url , "text/html", "UTF-8");
                return false;
            }
        });

        innerWebPage.getSettings().setPluginState(WebSettings.PluginState.ON);
        innerWebPage.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        innerWebPage.getSettings().setAppCacheEnabled(false);
        innerWebPage.getSettings().setJavaScriptEnabled(true);
        innerWebPage.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        txtTermsPolicy = (TextView)findViewById(R.id.txtTermsPolicy);
        //txtTermsPolicy.setText(Html.fromHtml(getString(R.string.str_signup_terms_policy_text)));

        SpannableString ss = new SpannableString(getResources().getString(R.string.str_signup_terms_policy_text));
        ClickableSpan termsOfUseSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // do some thing
                if(innerWebPage.getVisibility() != View.VISIBLE)
                {
                    txtTitle.setText(getResources().getString(R.string.signup_temrs_of_use));
                    innerWebPage.setVisibility(View.VISIBLE);
                    //innerWebPage.loadUrl(getResources().getString(R.string.ginko_privacy_policy_file_path));
                    innerWebPage.loadUrl("http://www.ginko.mobi/terms");
                    txtSignUp.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                TextPaint textpaint = ds;
                //ds.setColor(ds.linkColor);

                textpaint.bgColor = Color.TRANSPARENT;
                textpaint.setARGB(255, 255, 255, 255);
                //Remove default underline associated with spans
                ds.setUnderlineText(true);

            }

        };

        ClickableSpan privacyPolicySpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // do another thing
                if(innerWebPage.getVisibility() != View.VISIBLE)
                {
                    txtTitle.setText(getResources().getString(R.string.signup_privacy_policy));
                    innerWebPage.setVisibility(View.VISIBLE);
                    //innerWebPage.loadUrl(getResources().getString(R.string.ginko_privacy_policy_file_path));
                    innerWebPage.loadUrl("http://www.ginko.mobi/privacypolicy");
                    txtSignUp.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                TextPaint textpaint = ds;
                //ds.setColor(ds.linkColor);

                textpaint.bgColor = Color.TRANSPARENT;
                textpaint.setARGB(255, 255, 255, 255);
                //Remove default underline associated with spans
                ds.setUnderlineText(true);

            }
        };


        ss.setSpan(termsOfUseSpan, 28, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacyPolicySpan, 38, 52, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtTermsPolicy.setText(ss);
        txtTermsPolicy.setHighlightColor(Color.TRANSPARENT);
        txtTermsPolicy.setMovementMethod(LinkMovementMethod.getInstance());

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
                                                            signUpSuccessful(response.getData());
                                                        } else
                                                            ApiManager.showErrorMessage(SignUp.this);
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

        backLayout = (LinearLayout)findViewById(R.id.backLayout);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (innerWebPage.getVisibility() == View.VISIBLE) {
                    txtTitle.setText(getResources().getString(R.string.signup));
                    innerWebPage.setVisibility(View.GONE);
                    txtSignUp.setVisibility(View.VISIBLE);
                } else {
                    Intent intent = new Intent(SignUp.this, Sign.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        hiddenLayout = (LinearLayout)findViewById(R.id.blankLayout4);
        mScrollView = (ScrollView)findViewById(R.id.scrollBody);
        originalSize = mScrollView.getLayoutParams().height;

        txtSignUp = (TextView)findViewById(R.id.txtSignUp);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

        editName = (EditText) findViewById(R.id.edit_name);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editPassword = (EditText) findViewById(R.id.edit_password);
        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signInUser();
                    return true;
                }
                return false;
            }
        });

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
                if (heightDiff > 100) {
                    if (!isKeyboardVisible) {
                        isKeyboardVisible = true;
                    }
                    hiddenLayout.setVisibility(View.GONE);
                } else {
                    if (isKeyboardVisible) {
                        isKeyboardVisible = false;
                    }
                    hiddenLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        bodyLayout = (RelativeLayout) findViewById(R.id.bodyLayout);
        bodyLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (editName.isFocused() || editEmail.isFocused() || editPassword.isFocused()) {
                        Rect outRect = new Rect();
                        EditText mEditText = editName;
                        if (!editName.isFocused()) {
                            if (editEmail.isFocused()) {
                                mEditText = editEmail;
                            } else if (editPassword.isFocused()) {
                                mEditText = editPassword;
                            }
                        }
                        mEditText.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            mEditText.clearFocus();
                            MyApp.getInstance().hideKeyboard(rootLayout);
                        }
                    }
                }
                return false;
            }
        });

	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(callbackmanager!=null)
            callbackmanager.onActivityResult(requestCode, resultCode, data);
    }

	protected void signUpSuccessful(UserLoginVO user) {
		String sessionId = user.getSessionId();
		Uitils.storeSessionid(this, sessionId);
		RuntimeContext.setUser(user);
        MyApp.getInstance().initializeGlobalVariables();
        Intent intent = new Intent(SignUp.this , GetStart.class);
        intent.putExtra("fullname" , editName.getText().toString().trim());
        Uitils.storeUserFullname(SignUp.this, editName.getText().toString().trim());
        Uitils.storeUserName(SignUp.this, user.getUserName());
        startActivity(intent);
        SignUp.this.finish();
	}

	public void signUpWithFacebook (View view){
		
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT < 18) {
            innerWebPage.clearView();
        } else {
            innerWebPage.loadUrl("about:blank");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        isShownKeyboard();
        //hideKeyboard();
        if(!isKeyboardVisible)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        else {
            MyApp.getInstance().hideKeyboard(rootLayout);
            rootLayout.requestFocus();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void isShownKeyboard() {
        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int curheight= rectgle.bottom;
        if(m_orientHeight == curheight)
            isKeyboardVisible = false;
        else if (m_orientHeight > 0)
            isKeyboardVisible = true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(innerWebPage.getVisibility() == View.VISIBLE) {
            txtTitle.setText(getResources().getString(R.string.signup));
            innerWebPage.setVisibility(View.GONE);
            txtSignUp.setVisibility(View.VISIBLE);
        }
        else
        {
            Intent intent = new Intent(SignUp.this, Sign.class);
            startActivity(intent);
            finish();
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

	private void signInUser() {

		String name = editName.getText().toString().trim();
		final String emailAddress = editEmail.getText().toString().trim();
		String password = editPassword.getText().toString().trim();

		if (name.isEmpty()) {
			editName.requestFocus();
			Uitils.alert("Please enter the name.");
			return;
		}

		if (emailAddress.isEmpty()) {
			editEmail.requestFocus();
			Uitils.alert("Please enter the email address.");
			return;
		}
        else if(!emailAddress.isEmpty() && isEmailValid(emailAddress) == false)
        {
            editEmail.requestFocus();
            Uitils.alert("Please enter a valid email address.");
            return;
        }

		if (password.isEmpty()) {
			editPassword.requestFocus();
			Uitils.alert("Please enter the password.");
			return;
		}

        if(password.length()<6) {
            editPassword.requestFocus();
            Uitils.alert(SignUp.this, getResources().getString(R.string.str_alert_invalid_password_length));
            return;
        }
		String firstName=name;
		String lastName="";
		int index = name.indexOf(" ");
		if (index>0){
			firstName = name.substring(0, index);
			lastName= name.substring(index+1);
		}


        final String finalFirstName = firstName;
        final String finalLastName = lastName;
        UserRequest.signUp(emailAddress,
				password,firstName, lastName,Uitils.getDeviceUid(), Uitils.getDeviceToken(), new ResponseCallBack<UserLoginVO>() {
					
					@Override
					public void onCompleted(JsonResponse<UserLoginVO> response) {
						if (response.isSuccess()) {
                            UserLoginVO user = response.getData();
                            if(user == null)
                                user = new UserLoginVO();
                            user.setFirstName(finalFirstName);
                            user.setLastName(finalLastName);
                            String fullName = finalFirstName+" "+finalLastName;
                            fullName = fullName.trim();
                            Uitils.storeLoginEmail(SignUp.this , emailAddress);
                            Uitils.storeUserFullname(SignUp.this, fullName);
                            Uitils.storeUserName(SignUp.this, user.getUserName());
							signUpSuccessful(response.getData());

                        }
						else
                        {
                            AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this).create();
                            alertDialog.setTitle("Sign Up");
                            //alertDialog.setMessage(response.getErrorMessage());
                            alertDialog.setMessage("Email already registered, please login.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            onBackPressed();
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
					}
				});
	}
}
