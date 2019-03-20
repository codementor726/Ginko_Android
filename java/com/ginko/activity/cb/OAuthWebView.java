package com.ginko.activity.cb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.api.request.CBRequest;
import com.ginko.api.request.DirectoryRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.CbEmailVO;

import org.json.JSONObject;

import java.io.IOException;

public class OAuthWebView extends MyBaseActivity {
	private static String protocol = "com.ginko.app://";
	
	private String email;
	private String provider;

    private ImageButton btnPrev;
	private TextView txtTitle;

	private boolean isDirectory = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_webview);

		Intent intent = this.getIntent();
		if(intent.hasExtra("isDirectory"))
			isDirectory = intent.getBooleanExtra("isDirectory" , true);
		else
			isDirectory = false;

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

		email = this.getIntent().getExtras().getString("email");
		provider = this.getIntent().getExtras().getString("provider");

		txtTitle = (TextView) findViewById(R.id.txtTitle);
		if (isDirectory)
			txtTitle.setText(R.string.title_activity_cb_directory);
		
		WebView webview = ((WebView) this.findViewById(R.id.wv));
		
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.requestFocus();
		webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		
		String requestUrl = this.getIntent().getExtras().getString("requestUrl");
//		requestUrl = "www.baidu.com";
		if (isDirectory)
		{
			if (provider.equals("google"))
				requestUrl = "https://mail.google.com/mail/#inbox";
			else if (provider.equals("live"))
				requestUrl = "https://outlook.live.com/owa/?realm=hotmail.com";
			else if (provider.equals("yahoo"))
				requestUrl = "https://mg.mail.yahoo.com/neo/launch";
		}

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				Toast.makeText(OAuthWebView.this, "redirect to the url:" + url, Toast.LENGTH_SHORT).show();
				if (isSuccessAuthUrl(url)) {
//					Toast.makeText(OAuthWebView.this, "go to saveView", Toast.LENGTH_SHORT).show();  //FIXME, for testing.
					//go to set permission screen
					if (!isDirectory)
						addCB (url);
					else
						validateEmail(url);

				}else{

					view.loadUrl(url);
				}
				return true;
			}

			@Override
			public void onReceivedSslError(WebView view, final SslErrorHandler handler, android.net.http.SslError error) {
				//handler.proceed();
				final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
				builder.setMessage("Invalid certification.");
				builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handler.proceed();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handler.cancel();
					}
				});
				final AlertDialog dialog = builder.create();
				dialog.show();
			}

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(OAuthWebView.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
			}
		});
		
		webview.loadUrl(requestUrl);
	}

	protected boolean isSuccessAuthUrl(String url) {
		if (url.startsWith(protocol)) {
			return true;
		}
		return false;
	}
	
	private String getOauthToken(String authUrl) {
		int index = authUrl.indexOf("?");
		if (index > 0) {
			return authUrl.substring(index + 1);
		}
		return "";
	}

	private String getKeyValue(String authUrl) {
		int index = authUrl.indexOf("?key=");
		if (index > 0) {
			return authUrl.substring(index + 5);
		}
		return "";
	}
	
	private void addCB(String authUrl){
		
		final CbEmailVO cb = new CbEmailVO();
		
//		cb.setProvider(this.provider);
		cb.setAuthType("oauth");
		cb.setEmail(email);
		String oauthtoken = getOauthToken(authUrl);
		
		cb.setOauthtoken(oauthtoken);
		cb.setProvider(this.provider);
		cb.setSharingStatus(4);
		cb.setActive("no");
		CBRequest.saveCB(cb, new ResponseCallBack<JSONObject>() {
			
			@Override
			public void onCompleted(JsonResponse<JSONObject> response) {
				if (response.isSuccess()){
                    JSONObject resultObj = response.getData();
                    int cbId = resultObj.optInt("id");
					String valid = resultObj.optString("valid");
					cb.setId(cbId);
					cb.setValid(valid);
					
					Intent intent = new Intent();
					intent.setClass(OAuthWebView.this,
							SaveCBActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("cb", cb);
                    bundle.putBoolean("isNewCB" , true);
//					bundle.putInt("cb_id", value);
					intent.putExtras(bundle);
					startActivity(intent);
                    if(AddOAuthCBActivity.getInstatnce() != null)
                        AddOAuthCBActivity.getInstatnce().finish();
					OAuthWebView.this.finish();
				}
                else
                {
                    try {
                        String errorMessage = response.getErrorMessage();

                        MyApp.getInstance().showSimpleAlertDiloag(OAuthWebView.this ,errorMessage , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                OAuthWebView.this.finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
			}
		});
		
	}

	private void validateEmail(String authUrl){

		String oauthKey = getKeyValue(authUrl);

		DirectoryRequest.validateEmail(oauthKey, new ResponseCallBack<JSONObject>() {
			@Override
			public void onCompleted(JsonResponse<JSONObject> response) {
				if (response.isSuccess()) {
					Intent resultIntent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putBoolean("isSuccess", true);
					resultIntent.putExtras(bundle);
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				} else {
					try {
						String errorMessage = response.getErrorMessage();

						MyApp.getInstance().showSimpleAlertDiloag(OAuthWebView.this, errorMessage, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								OAuthWebView.this.finish();
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, true);
	}
}
