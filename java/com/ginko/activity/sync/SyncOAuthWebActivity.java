package com.ginko.activity.sync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

public class SyncOAuthWebActivity extends MyBaseActivity {
    private static String protocol = "com.ginko.app://";

    private String email;
    private String provider;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_webview);

        title = this.getIntent().getExtras().getString("title");
        email = this.getIntent().getExtras().getString("email");
        provider = this.getIntent().getExtras().getString("provider");

        ImageButton btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtTitle.setText(title);

        WebView webview = ((WebView) this.findViewById(R.id.wv));

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.requestFocus();
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        String requestUrl = this.getIntent().getExtras().getString("requestUrl");
//		requestUrl = "www.baidu.com";

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				Toast.makeText(OAuthWebView.this, "redirect to the url:" + url, Toast.LENGTH_SHORT).show();
                if (isSuccessAuthUrl(url)) {
                    //go to set permission screen
                    redirectToImportContacts(url);
                }else{

                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                //handler.proceed();
                final SslErrorHandler m_handler= handler;
                final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage("Invalid certification.");
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_handler.proceed();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(SyncOAuthWebActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
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

    private void redirectToImportContacts(String authUrl){
        String oauthToken = getOauthToken(authUrl);
        Intent intent = new Intent(this,SyncImportAddressBookActivity.class);
        intent.putExtra("oauth_token",oauthToken);
        intent.putExtra("email",email);
        intent.putExtra("provider",provider);
        startActivity(intent);
        this.finish();
    }
}
