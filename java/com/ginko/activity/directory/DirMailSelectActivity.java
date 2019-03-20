package com.ginko.activity.directory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.cb.AddOAuthCBActivity;
import com.ginko.activity.cb.CBAddByOWSctivity;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

public class DirMailSelectActivity extends MyBaseActivity {

    private final int VALIDATE_LINK_ACTIVITY = 3;

    private RelativeLayout bottomLayout;
    private Button btnSkip;
    private ImageButton btnPrev;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_mail_select);Intent intent = this.getIntent();
        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    public void addGmail(View view) {
        this.openAddCbViewByOAuth("google");
    }

    public void addYahoo(View view) {
        this.openAddCbViewByOAuth("yahoo");
    }

    public void addLive(View view) {
        this.openAddCbViewByOAuth("live");
    }

    public void addOutlook(View view) {
        this.openAddCbViewByOAuth("outlook");
    }

    private void openAddCbViewByOAuth(String provider) {
        Intent intent = new Intent(DirMailSelectActivity.this , AddOAuthCBActivity.class);
        intent.putExtra("isDirectory", true);
        intent.putExtra("provider", provider);
        startActivityForResult(intent, VALIDATE_LINK_ACTIVITY);
    }


}
