package com.ginko.setup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ginko.activity.contact.ContactMainActivity;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

public class GoToInviteContactScreenConfirmActivity extends MyBaseActivity implements View.OnClickListener{

    /* UI Varaibles */
    private Button btnNo , btnYes;
    private ImageButton btnBack;

    /* Variables */
    private boolean fromMainContactScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_confirm_screen_go_to_invite_screen);

        Intent intent = this.getIntent();
        fromMainContactScreen = intent.getBooleanExtra("isFromMainContactScreen" , false);

        btnBack = (ImageButton)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromMainContactScreen)
                    finish();
                else
                {
                    Intent mainContactIntent = new Intent(GoToInviteContactScreenConfirmActivity.this, ContactMainActivity.class);
                    startActivity(mainContactIntent);
                    finish();
                }
            }
        });

        /*if(fromMainContactScreen)
            btnBack.setVisibility(View.VISIBLE);
        else
            btnBack.setVisibility(View.GONE);*/

        btnNo = (Button)findViewById(R.id.btnNo); btnNo.setOnClickListener(this);
        btnYes = (Button)findViewById(R.id.btnYes); btnYes.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnNo:
                if(fromMainContactScreen)
                {
                    finish();
                }
                else {
                    //Intent mainContactIntent = new Intent(GoToInviteContactScreenConfirmActivity.this, ContactMainActivity.class);
                    Intent mainContactIntent = new Intent(GoToInviteContactScreenConfirmActivity.this, TutorialActivity.class);
                    mainContactIntent.putExtra("isFromSignUp" , true);
                    startActivity(mainContactIntent);
                    finish();
                }
                break;

            case R.id.btnYes:
                Intent registerMobileIntent = new Intent(GoToInviteContactScreenConfirmActivity.this , RegisterConfirmationMobileActivity.class);
                registerMobileIntent.putExtra("isFromMainContactScreen" , fromMainContactScreen);
                startActivity(registerMobileIntent);
                finish();
                break;
        }
    }
}
