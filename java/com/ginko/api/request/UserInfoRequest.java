package com.ginko.api.request;

import android.content.Context;

import com.ginko.common.NoNullProperties;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;

import org.json.JSONObject;

public class UserInfoRequest extends GinkoRequest {
	private static final String apiGroup = "/UserInfo";
	private static final String setInfo = apiGroup + "/setInfo";
	private static final String removeProfile = apiGroup + "/removeProfile";
	private static final String updatePrivilege = apiGroup + "/updatePrivilege";
	private static final String getInfo = apiGroup + "/getInfo";
	private static final String getMyPhoto = apiGroup + "/getMyPhoto";
	private static final String getContactInfo = apiGroup + "/getContactInfo";

	public static void setUserInfo(UserUpdateVO userInfo, ResponseCallBack<Void> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(setInfo, userInfo);
		_sendRequest(JSONObject.class, callback, request , true , true);
	}
	
	public static void removeProfile(String group, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group", group);
		QueryParametersRequest request = new QueryParametersRequest(removeProfile, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	public static void updatePrivilege(Integer homePrivilege,Integer workPrivilege, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("home_privilege", homePrivilege);
		qPara.setProperty("work_privilege", workPrivilege);
		QueryParametersRequest request = new QueryParametersRequest(updatePrivilege, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
    public static void getInfo(ResponseCallBack<UserWholeProfileVO> callback) {
        getInfo(null,callback,true);
    }
    public static void getInfo(Integer contact_uid ,ResponseCallBack<UserWholeProfileVO> callback) {
        getInfo(contact_uid,callback,false);
    }
	public static void getInfo(Integer contact_uid ,ResponseCallBack<UserWholeProfileVO> callback,boolean showProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sessionId", RuntimeContext.getSessionId());

		if (contact_uid !=null){
			qPara.setProperty("contact_uid", contact_uid);
		}

		QueryParametersRequest request = new QueryParametersRequest(getInfo, get, qPara);
		_sendRequest(UserWholeProfileVO.class, callback, request, true,showProgressDialog);
	}
	
	public static void getMyPhoto(Context context ,ResponseCallBack<JSONObject> callback) {
		QueryParametersRequest request = new QueryParametersRequest(getMyPhoto, get);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void getContactInfo(Integer contact_uid, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_uid", contact_uid);
		QueryParametersRequest request = new QueryParametersRequest(getContactInfo, get);
		_sendRequest(JSONObject.class, callback, request);
	}
}
