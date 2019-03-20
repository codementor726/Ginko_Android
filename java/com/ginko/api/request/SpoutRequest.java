package com.ginko.api.request;

import com.ginko.common.NoNullProperties;
import com.ginko.data.ResponseCallBack;

import org.json.JSONObject;

public class SpoutRequest extends GinkoRequest {
	private static final String apiGroup = "/gps";
	private static final String detectNearUsers = apiGroup + "/friend/found";
	private static final String updateLocation = apiGroup + "/location/update";
	private static final String switchLocationStatus = apiGroup + "/location/status";
    private static final String deleteFoundFriends = apiGroup + "/friends/remove";
    private static final String deleteFoundEntities = apiGroup + "/entities/remove";
	private static final String listAllContacts = apiGroup + "/contact/list";
	private static final String detectUnExchangedContacts = apiGroup + "/unexchanged/list";
	private static final String detectContactFilter = apiGroup + "/contact/filter";
	private static final String getFilterContact = apiGroup + "/contact/filter/get";


	public static void detectNearUsers(Integer type, String q, ResponseCallBack<JSONObject> callback , boolean bProgressShow) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);
		qPara.setProperty("q", q);
		QueryParametersRequest request = new QueryParametersRequest(detectNearUsers, get, qPara);
		_sendRequest(JSONObject.class, callback, request,false,bProgressShow);
	}

	public static void listAllContacts(String query,ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("pageNum", 1);
		qPara.setProperty("countPerPage", 40);
		qPara.setProperty("q", query);
		QueryParametersRequest request = new QueryParametersRequest(listAllContacts, get, qPara);
		_sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
	}

	public static void detectContactFilter(Integer type ,String user_ids,boolean isRemoveExisted ,ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();

		qPara.setProperty("type", type);
		qPara.setProperty("user_ids", user_ids);
		qPara.setProperty("remove_existed", isRemoveExisted);
		QueryParametersRequest request = new QueryParametersRequest(detectContactFilter, qPara);
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}
	public static void detectUnExchangedContacts(String query,ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("pageNum", 1);
		qPara.setProperty("countPerPage", 40);
		qPara.setProperty("q", query);
		QueryParametersRequest request = new QueryParametersRequest(detectUnExchangedContacts, get,  qPara);
		_sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
	}

	public static void updateLocation(Double latitude, Double longitude, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("latitude", latitude);
		qPara.setProperty("longitude", longitude);
		QueryParametersRequest request = new QueryParametersRequest(updateLocation, qPara);
		_sendRequest(Void.class, callback, request,false,false);
	}

	public static void switchLocationStatus(boolean turnedOn, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("turn_on", turnedOn);
		QueryParametersRequest request = new QueryParametersRequest(switchLocationStatus, qPara);
		_sendRequest(Void.class, callback, request,false,false);
	}

	public static void deleteFoundFriends(String userIds, int removeType, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("user_ids", userIds);
		qPara.setProperty("remove_type", removeType);
		QueryParametersRequest request = new QueryParametersRequest(deleteFoundFriends, qPara);
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}


    public static void deleteFoundEntities(String userIds, int removeType, ResponseCallBack<Void> callback , boolean bProgressDialog) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("entity_ids", userIds);
        qPara.setProperty("remove_type", removeType);
        QueryParametersRequest request = new QueryParametersRequest(deleteFoundEntities, qPara);
        _sendRequest(Void.class, callback, request , true , bProgressDialog);
    }

	public static void getFilterContact(ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		QueryParametersRequest request = new QueryParametersRequest(getFilterContact, get , qPara);
		_sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
	}
}
