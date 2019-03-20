package com.ginko.api.request;

import com.ginko.common.NoNullProperties;
import com.ginko.data.ResponseCallBack;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class SyncRequest extends GinkoRequest {
	private static final String apiGroup = "/sync/contact";
	private static final String DISCOVER_SERVER = apiGroup + "/server/discover";
	private static final String FETCH_REDIRECT = apiGroup + "/fetch_redirect";;
	private static final String importByOAuth = apiGroup + "/import";
	private static final String importByOWA = apiGroup + "/import/owa";
	private static final String importByMisc = apiGroup + "/import/misc";
	private static final String saveGreyContact = apiGroup + "/greyContact/save";
	private static final String saveSyncContacts = apiGroup + "/multiple/add";
    private static final String addGreyContact = "/grey/contact/add";
	private static final String removeGreyContact = apiGroup + "/greyContact/remove";
	private static final String deleteSyncContact = apiGroup + "/delete";
	private static final String getSyncContactDetial = apiGroup + "/getDetail";
	private static final String updateDetail = apiGroup + "/updateDetail";
	private static final String search = apiGroup + "/search";
    private static final String getImportedContactHistory = apiGroup + "/getHistory";

    private static final String setGreyContactPhoto = "/grey/contact/setPhoto";

	public static void discoverOutlookServer(String email, String username, String password, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("username", username);
		qPara.setProperty("password", password);
		QueryParametersRequest request = new QueryParametersRequest(DISCOVER_SERVER, get, qPara);
		_sendRequest(JSONObject.class, callback, request, false, false);

	}

	public static void getOAuthUrl(String email, String provider, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("provider", provider);
		QueryParametersRequest request = new QueryParametersRequest(FETCH_REDIRECT, get, qPara);
		_sendRequest(JSONObject.class, callback, request);

	}

	public static void importByOAuth(String email, String provider, String authStr, ResponseCallBack<List<JSONObject>> callback) {
        Type type = new TypeToken<List<JSONObject>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("provider", provider);
		QueryParametersRequest request = new QueryParametersRequest(importByOAuth, qPara);
		request.setQueryStr(authStr);
		_sendRequest(type, callback, request);
	}

	public static void importByOWA(String email, String username, String password, String webmailLink, ResponseCallBack<List<JSONObject>> callback) {
        Type type = new TypeToken<List<JSONObject>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("username", username);
		qPara.setProperty("password", password);
		qPara.setProperty("webmailLink", webmailLink);
		QueryParametersRequest request = new QueryParametersRequest(importByOWA, qPara);
		_sendRequest(type, callback, request);
	}

	public static void miscImport(String username, String password, String site, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("username", username);
		qPara.setProperty("password", password);
		qPara.setProperty("site", site);
		QueryParametersRequest request = new QueryParametersRequest(importByMisc, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void saveGreyContact(JSONObject data, ResponseCallBack<Void> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(saveGreyContact, data);
		_sendRequest(Void.class, callback, request);
	}
	
	// {data:[{},{}]}
	public static void saveSyncContacts(JSONObject data, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(saveSyncContacts, data);
		_sendRequest(Void.class, callback, request , false , bProgressDialog);
	}

    public static void addGreyContact(JSONObject data, ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
        QueryParametersRequest request = new QueryParametesWithBodyRequest(addGreyContact, data);
        _sendRequest(JSONObject.class, callback, request , false , bProgressDialog);
    }
	
	public static void removeGreyContact(String syncContactIds, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sync_contact_ids", syncContactIds);
		QueryParametersRequest request = new QueryParametersRequest(removeGreyContact, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void deleteSyncContact(String syncContactIds, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sync_contact_ids", syncContactIds);
		QueryParametersRequest request = new QueryParametersRequest(deleteSyncContact, qPara);
		_sendRequest(Void.class, callback, request , false ,true);
	}
	
	public static void getSyncContactDetial(String syncContactId,  ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sync_contact_id", syncContactId);
		QueryParametersRequest request = new QueryParametersRequest(getSyncContactDetial,get, qPara);
		_sendRequest(JSONObject.class, callback, request , true , true);
	}

    public static void getImportedContactHistory( ResponseCallBack<JSONObject> callback) {
        //Type type = new TypeToken<List<JSONObject>>() {}.getType();
        NoNullProperties qPara = new NoNullProperties();
        QueryParametersRequest request = new QueryParametersRequest(getImportedContactHistory,get, qPara);
        _sendRequest(JSONObject.class, callback, request , true , true);
    }

	public static void updateDetail(JSONObject data, ResponseCallBack<Void> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(updateDetail, data);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void search(String scope,String keyword, Integer pageNum,Integer countPerPage,  ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("scope", scope);
		qPara.setProperty("q", keyword);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(search, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

    public static void setGreyContactPhoto(String contact_id, File file,
                                ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("contact_id", contact_id);
        HashMap<String, Object> formData = new HashMap<String, Object>();
        formData.put("photo", file);
        AbstractRequest request = new FormSubmitRequest(setGreyContactPhoto, qPara,
                formData);
        request.setFormData(formData);
        _sendRequest(JSONObject.class, callback, request , true , true);
    }
}
