package com.ginko.activity.im;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;

public class VideoViewerActivity extends Activity implements View.OnClickListener{
    private String strVideoUri = "";
    private WebView videoWebView;
    private VideoWebChromeClient chromeClient;

    private VideoView videoView;
    private ImageButton btnPrev;
    private LinearLayout progressLayout;
    private TextView txtProgressState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.strVideoUri = this.getIntent().getStringExtra("video_uri");
        System.out.println("---Video URL "+this.strVideoUri+" -----");

        setContentView(R.layout.activity_video_message_viewer);

        /*chromeClient = new VideoWebChromeClient();

        this.videoWebView = (WebView)findViewById(R.id.videoWebview);
        videoWebView.setWebChromeClient(chromeClient);
        //videoWebView.setWebViewClient(wvClient);
        videoWebView.getSettings().setJavaScriptEnabled(true);*/
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        videoView = (VideoView)findViewById(R.id.videoView);
        progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
        txtProgressState = (TextView)findViewById(R.id.txtProgressState);
        try
        {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            Uri video = Uri.parse(strVideoUri);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            showProgressDialog("Loading....");
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            videoView.start();
                            hideProgressDialog();
                        }
                    });

                }
            });
        }catch(Exception e)
        {
            e.printStackTrace();
            MyApp.getInstance().showSimpleAlertDiloag(VideoViewerActivity.this , R.string.str_error_play_video , null);
            hideProgressDialog();
            finish();
        }
    }

    private void showProgressDialog(String progressText)
    {
        txtProgressState.setText(progressText);
        progressLayout.setVisibility(View.VISIBLE);
    }
    private void hideProgressDialog()
    {
        progressLayout.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    public class VideoWebChromeClient extends WebChromeClient
    {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }
    }

}
