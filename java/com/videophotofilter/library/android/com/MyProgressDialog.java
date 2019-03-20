package com.videophotofilter.library.android.com;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.WindowManager.BadTokenException;
import android.widget.TextView;

import com.ginko.ginko.R;

public class MyProgressDialog {
	
	 public static ProgressDialog createProgressDialog(Context mContext , String message) {
	        ProgressDialog dialog = new ProgressDialog(mContext);
	        try {
	                dialog.show();
	        } catch (BadTokenException e) {

	        }
	        dialog.setCancelable(false);
	        dialog.setContentView(R.layout.custom_progress_dialog);
	        TextView txtMessage = (TextView)dialog.findViewById(R.id.txtMessage);
	        {
	        	txtMessage.setText(message);
	        }
	        // dialog.setMessage(Message);
	        try
	        {
	        	dialog.dismiss();
	        }catch(Exception e){e.printStackTrace();}
	        return dialog;
	    }
}
