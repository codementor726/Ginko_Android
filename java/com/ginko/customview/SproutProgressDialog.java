package com.ginko.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.ginko.R;

public class SproutProgressDialog extends Dialog {
	public SproutProgressDialog(Context context) {
		super(context);
	}

	public SproutProgressDialog(Context context, int theme) {
		super(context, theme);
	}

    @Override
	public void onWindowFocusChanged(boolean hasFocus){
		/*final ImageView animationView = (ImageView)findViewById(R.id.imgAnimationView);
		animationView.setBackgroundResource(R.drawable.sprout_animation_movie);
		animationView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) animationView.getBackground();
				anim.setOneShot(false);//repeat animation
				anim.start();
			}
		});*/
    }

	public static SproutProgressDialog show(Context context , boolean indeterminate, boolean cancelable,
			OnCancelListener cancelListener) {
		SproutProgressDialog dialog = new SproutProgressDialog(context,R.style.ProgressHUD);
		dialog.setTitle("");
		dialog.setContentView(R.layout.sprout_animation_dialog);
		final ImageView animationView = (ImageView)dialog.findViewById(R.id.imgAnimationView);
		animationView.setBackgroundResource(R.drawable.sprout_animation_movie);
		animationView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) animationView.getBackground();
				anim.setOneShot(false);//repeat animation
				anim.start();
			}
		});
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.getWindow().getAttributes().gravity=Gravity.CENTER;
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
		lp.dimAmount=0.2f;
		dialog.getWindow().setAttributes(lp); 
		//dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		dialog.show();
		return dialog;
	}
    public static SproutProgressDialog createProgressDialog(Context context, boolean indeterminate, boolean cancelable,
                                   OnCancelListener cancelListener) {
        SproutProgressDialog dialog = new SproutProgressDialog(context,R.style.ProgressHUD);
        dialog.setTitle("");
        dialog.setContentView(R.layout.sprout_animation_dialog);
		final ImageView animationView = (ImageView)dialog.findViewById(R.id.imgAnimationView);
		animationView.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable anim = (AnimationDrawable) animationView.getBackground();
				anim.setOneShot(false);//repeat animation
				anim.start();
			}
		});
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.getWindow().getAttributes().gravity=Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount=0.2f;
        dialog.getWindow().setAttributes(lp);
        //dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        return dialog;
    }
}
