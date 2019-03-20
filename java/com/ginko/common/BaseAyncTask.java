package com.ginko.common;

import java.lang.reflect.Type;
import java.util.HashMap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.content.Context;
import android.os.AsyncTask;

import com.ginko.api.request.GinkoRequest;
import com.ginko.customview.ProgressDialogManage;
import com.ginko.data.IGinkoRequest;
import com.ginko.data.JsonResponse;
import com.ginko.ginko.MyApp;

public abstract class BaseAyncTask<T> extends AsyncTask<IGinkoRequest, Void, JsonResponse<T>> {
	private Context context;
	private Type clazz ;
	private boolean showProgressDialog = true;
	private boolean showErrorAlert= true;
	public BaseAyncTask ( Type cls){
		this(cls,true);
	}
	
	public BaseAyncTask (Type cls,  boolean showErrorAlert){
		this(cls,true, true);
	}
	
	public BaseAyncTask (Type cls,  boolean showErrorAlert, boolean showProgressDialog){
		this.context = MyApp.getInstance().getCurrentActivity();
		this.clazz = cls;
		this.showErrorAlert = showErrorAlert;
		this.showProgressDialog= showProgressDialog;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (showProgressDialog){
			ProgressDialogManage.show(context);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected JsonResponse<T> doInBackground(IGinkoRequest... requests) {
		String rawResp="";
		for (IGinkoRequest request : requests) {
            rawResp = request.send();
		}
		if (rawResp==null || rawResp.isEmpty()){
			return  JsonResponse.NullResponse;
		}
		return new JsonResponse(rawResp,this.clazz);

	}

	@Override
	protected  void onPostExecute(JsonResponse<T> response){
		super.onPostExecute(response);
		if (showProgressDialog){
			ProgressDialogManage.hide();
		}
		
		if (!response.isSuccess() && showErrorAlert){
			Uitils.alert(response.getErrorMessage());
		}
		this._onPostExecute(response);
	}
	
	protected abstract void _onPostExecute(JsonResponse<T> response);

}
