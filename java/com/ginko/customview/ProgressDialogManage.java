package com.ginko.customview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import com.ginko.common.Logger;

public class ProgressDialogManage {
	
	static 	ProgressHUD mProgressHUD = null;

	public static synchronized void show(Context context)
	{
        if (mProgressHUD!=null && mProgressHUD.isShowing()){
            return;
        }
		if (context==null){
			Logger.error("context is null, can't show the progress dialog.");
			return;
		}
    	mProgressHUD = ProgressHUD.show(context,"", true,false,null);
	}
	

    public static synchronized void hide() {
		if (mProgressHUD == null){
			Logger.error("context is null, can't show the progress dialog.");
			return;
		}

        if (mProgressHUD.isShowing() && !isParentDestroyed()){
            mProgressHUD.dismiss();
            mProgressHUD = null;
        }
	}


    private static boolean isParentDestroyed(){
        Context context = mProgressHUD.getContext();

        if (context == null || !(context instanceof Activity || context instanceof ContextWrapper)) {
            return true;
        }
        Activity activity= null;
        if (context instanceof Activity){
            activity = (Activity) context;
        }else{
            Context baseContext = ((ContextWrapper) context).getBaseContext();
            if (!(baseContext instanceof Activity)) {
                return false;
            }
            activity = (Activity) baseContext;
        }
        return activity.isFinishing();
    }
}
