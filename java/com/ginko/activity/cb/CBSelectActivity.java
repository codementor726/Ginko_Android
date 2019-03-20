package com.ginko.activity.cb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.common.Uitils;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.setup.TutorialActivity;

public class CBSelectActivity extends MyBaseActivity {
    private RelativeLayout bottomLayout;
    private Button btnSkip;
    private ImageButton btnPrev;

    private boolean isNewGinkoUserStartup = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cbsplash);

        Intent intent = this.getIntent();
        if(intent.hasExtra("isNewGinkoStartUp"))
            isNewGinkoUserStartup = intent.getBooleanExtra("isNewGinkoStartUp" , true);
        else
            isNewGinkoUserStartup = true;

        btnPrev = (ImageButton)findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
        if(!isNewGinkoUserStartup) {
            btnPrev.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.GONE);
        }
        else
        {
            btnPrev.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
        }


        btnSkip = (Button)findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uitils.toActivity(CBSelectActivity.this,
                //       ContactMainActivity.class, true); // FIXME
                Intent tutorialIntent = new Intent(CBSelectActivity.this , TutorialActivity.class);
                tutorialIntent.putExtra("isFromSignUp" , true);
                CBSelectActivity.this.startActivity(tutorialIntent);
                CBSelectActivity.this.finish();
            }
        });
	}

    @Override
    public void onBackPressed() {
        if(isNewGinkoUserStartup)
            return;
        super.onBackPressed();
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
        Intent intent = new Intent(CBSelectActivity.this , CBAddByOWSctivity.class);
        startActivity(intent);
	}

	public void addOthers(View view) {
        Intent intent = new Intent(CBSelectActivity.this , CbAddByOthersActivity.class);
        startActivity(intent);
	}

	private void openAddCbViewByOAuth(String provider) {
        Intent intent = new Intent(CBSelectActivity.this , AddOAuthCBActivity.class);
        intent.putExtra("provider", provider);
        startActivity(intent);
	}
}
