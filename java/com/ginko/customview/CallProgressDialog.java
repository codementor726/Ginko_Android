package com.ginko.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ginko.ginko.R;

/**
 * Created by YongJong on 03/21/17.
 */
public class CallProgressDialog extends Dialog {
    public CallProgressDialog(Context context) {
        super(context);
    }

    public CallProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){

    }

    public static CallProgressDialog show(Context context , boolean indeterminate, boolean cancelable,
                                            DialogInterface.OnCancelListener cancelListener) {
        CallProgressDialog dialog = new CallProgressDialog(context, R.style.ProgressHUD);
        dialog.setTitle("");
        dialog.setContentView(R.layout.call_animation_dialog);
        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        final ImageView animationView = (ImageView)dialog.findViewById(R.id.imgAnimationView);
        animationView.setBackgroundResource(R.drawable.call_animation_movie);
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
        dialog.getWindow().getAttributes().gravity= Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount=0.2f;
        dialog.getWindow().setAttributes(lp);
        //dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.show();
        return dialog;
    }
    public static CallProgressDialog createProgressDialog(Context context, boolean indeterminate, boolean cancelable,
                                                            DialogInterface.OnCancelListener cancelListener) {
        CallProgressDialog dialog = new CallProgressDialog(context,R.style.ProgressHUD);
        dialog.setTitle("");
        dialog.setContentView(R.layout.call_animation_dialog);

        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

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
