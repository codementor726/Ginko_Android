package com.ginko.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;

public class Sign extends MyBaseActivity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign);
    }
	
	public void onBtnRegister(View v) {		
		Intent intent = new Intent(Sign.this, SignUp.class);
		startActivity(intent);
        finish();
	}
	
	public void onBtnLoginInSign(View v) {		
		Intent intent = new Intent(Sign.this, Login.class);
		startActivity(intent);
        finish();
	}

}
