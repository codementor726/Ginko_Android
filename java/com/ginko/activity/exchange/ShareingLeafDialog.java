package com.ginko.activity.exchange;

import com.ginko.customview.LeafShareChooser;
import com.ginko.customview.SharingBean;
import com.ginko.ginko.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ShareingLeafDialog extends Activity implements OnClickListener {
	private LeafShareChooser leafChooser;
	
	private String email;
	private Integer contactUid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_sharing_choose);
		 contactUid = this.getIntent().getIntExtra("contact_uid", 0);
		 email = this.getIntent().getStringExtra("email");
		if (contactUid == 0  ) {
			contactUid = null;
		}
		SharingBean sb = (SharingBean)this.getIntent().getSerializableExtra("sharebean");
		leafChooser = (LeafShareChooser)findViewById(R.id.leafShareChooser1);
		leafChooser.setShareBean(sb);
		leafChooser.retrieveUserProfile(contactUid);
		
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_ok){
			saveShareInfo();
		}
		this.finish();
	}
	private void saveShareInfo() {
		Intent intent = new Intent(this, ExchangeRequestActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("shareBean", leafChooser.getSelectedAsShareBean());
		if (email !=null){
			bundle.putString("email", email);
		}
		if (contactUid!=null){
			bundle.putInt("contact_uid", contactUid);
		}
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		
	}
}
