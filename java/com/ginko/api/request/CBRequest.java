package com.ginko.api.request;

import android.content.Context;

import com.ginko.common.MyStringUtils;
import com.ginko.common.NoNullProperties;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.CbEmailVO;
import com.ginko.vo.EntityVO;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class CBRequest extends GinkoRequest{
	private static final String apiGroup = "/ContactBuilder";
	private static final String FETCH_REDIRECT= apiGroup + "/fetch_redirect";
	private static final String getCBemails= apiGroup + "/getCBemails";
	private static final String deleteEmail= apiGroup + "/deleteEmail";
	private static final String getCBEmail= apiGroup + "/getCBEmail";
	private static final String getCBEmailByEmail= apiGroup + "/getCBEmailByEmail";
	private static final String clear_all= apiGroup + "/clear";
	private static final String SAVE_UPDATE_CB= apiGroup + "/updateEmailSetting";
	private static final String GET_EXCHANGE_REQUEST= apiGroup + "/getRequests";
	private static final String SEND_EXCHANGE_REQUEST= apiGroup + "/request/send";
	private static final String UPDATE_PERMISSION= apiGroup + "/permission/update";
	private static final String CANCEL_REQUEST= apiGroup + "/request/cancel";
	private static final String LIST_SUGGESION= apiGroup + "/list/suggestion";
	private static final String UPDATE_CONTACT_NOTE= apiGroup + "/contact/notes/update";
	private static final String CHECK_ALL_CB_VALID= apiGroup + "/check/cbemailvalid";
	private static final String ADD_INVITATION= apiGroup + "/addInvitation";
	private static final String ANSWER_EXCHANGE_REQUEST= apiGroup + "/confirmRequest";
	private static final String REMOVE_FRIENDS = apiGroup + "/removeFriend";
	private static final String REMOVE_MULTIFRIENDS = apiGroup + "/removeFriends";
	private static final String DELETE_INVITATION = apiGroup + "/deleteInvitation";
	private static final String CANCEL_REQUEST_BY_EMAIL = apiGroup + "/cancelRequest";
	private static final String GET_PENDING_REQUESTS = apiGroup + "/getSentInvitations";
	private static final String GET_INVITATIONS= apiGroup + "/getInvitations";
	private static final String DELETE_REQUEST= apiGroup + "/deleteRequest";
	private static final String GET_EXCHANGE_SUMMARY= apiGroup + "/getExchangeSummary";
    private static final String SEND_REQUEST = apiGroup + "/request/send";
	private static final String CHECK_USERS = apiGroup + "/checkUsers";
	private static final String INVITE = apiGroup + "/invite";
	private static final String SEND_INVITE_STATUS = apiGroup + "/invite/send";

	private static final String EXCHANGE_INVITES = apiGroup + "/getExchangeInvites";

	public static void getOAuthUrl(String email, String provider,
			ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("provider", provider);
		QueryParametersRequest request = new QueryParametersRequest(FETCH_REDIRECT, get, qPara);
		_sendRequest(JSONObject.class, callback, request , false , true);
	}
	
	public static void getCBemails(Context context,	ResponseCallBack<List<CbEmailVO>> callback , boolean bProgressDialog){
		Type type = new TypeToken<List<CbEmailVO>>() {}.getType();
		QueryParametersRequest request = new QueryParametersRequest(getCBemails, get);
		_sendRequest(type, callback, request,true,bProgressDialog);
	}
	
	public static void deleteEmail(Integer cbId, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("emailId", cbId);
		QueryParametersRequest request = new QueryParametersRequest(deleteEmail,qPara);
		_sendRequest(Void.class, callback, request, false, true);
	}
	
	public static void getCBEmail(Integer cbId,ResponseCallBack<CbEmailVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("emailId", cbId);
		QueryParametersRequest request = new QueryParametersRequest(getCBEmail, get, qPara);
		_sendRequest(CbEmailVO.class, callback, request);
	}
	
	public static void getCBEmailByEmail(String email,ResponseCallBack<CbEmailVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		QueryParametersRequest request = new QueryParametersRequest(getCBEmailByEmail, get, qPara);
		_sendRequest(CbEmailVO.class, callback, request);
	}
	
	public static void clear(ResponseCallBack<Void> callback) {
		QueryParametersRequest request = new QueryParametersRequest(clear_all);
		_sendRequest(Void.class, callback, request);
	}
	
	//cbId only should be set when update
	public static void saveCB(CbEmailVO cb,  ResponseCallBack<JSONObject> callback) {
		/*Integer cbId, String email, String username, String password, String active, String sharedHomeFieldIds,
		String sharedWorkFieldIds, String authType, String provider, String oauthtoken,
		Integer sharing, String serverAddr, Integer serverPort, String serverType, Boolean isSsl,*/
		
		
		NoNullProperties qPara = new NoNullProperties();
		if (cb.getId()!=null){
			qPara.setProperty("id", cb.getId());
		}
		qPara.setProperty("email", cb.getEmail());
		qPara.setProperty("username", cb.getUsername());
		qPara.setProperty("password", cb.getPassword());
//		qPara.setProperty("phone_only", phoneOnly);
//		qPara.setProperty("email_only", emailOnly);

		qPara.setProperty("active", cb.getActive());
		qPara.setProperty("shared_home_fids", cb.getSharedHomeFieldIds());
		qPara.setProperty("shared_work_fids", cb.getSharedWorkFieldIds());
		
		qPara.setProperty("auth_type", cb.getAuthType());
		qPara.setProperty("provider", cb.getProvider());
		qPara.setProperty("oauth_token", cb.getOauthtoken());

		qPara.setProperty("sharing", cb.getSharingStatus());
		qPara.setProperty("inserver", cb.getServerAddr());
		if (cb.getServerPort()!=null){
			qPara.setProperty("inserverport", cb.getServerPort());
		}
		qPara.setProperty("inservertype", cb.getType());
		qPara.setProperty("is_ssl", cb.isSsl());
		QueryParametersRequest request = new QueryParametersRequest(SAVE_UPDATE_CB, qPara);
		_sendRequest(JSONObject.class, callback, request, false, true);
	}
	
	public static void getExchangeRequestsNew(String searchWord, Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("q", searchWord);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(GET_EXCHANGE_REQUEST, get, qPara);
		
		Type type = new TypeToken<JSONObject>() {}.getType();
		_sendRequest(type, callback, request);
	}

	public static void sendInviteStatus(String email, String phone, boolean fromLocalAddressBook , ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("phone", phone);
		qPara.setProperty("from_local_address_book", fromLocalAddressBook);
		QueryParametersRequest request = new QueryParametersRequest(SEND_INVITE_STATUS, qPara);

		_sendRequest(Void.class, callback, request , false , true);
	}

	public static void sendRequest(Integer contact_uid, String email, Integer sharing, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_uid", contact_uid);
		qPara.setProperty("email", email);
		qPara.setProperty("sharing", sharing);
		qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
		qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
		QueryParametersRequest request = new QueryParametersRequest(SEND_EXCHANGE_REQUEST, qPara);
		_sendRequest(Void.class, callback, request);

	}
	
	/*
	 * For the man invited
	 */
	public static void deleteRequest( String contact_uids, String entityIds , ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_ids", contact_uids);
        qPara.setProperty("entity_ids", entityIds);
        QueryParametersRequest request = new QueryParametersRequest(DELETE_REQUEST, qPara);
		_sendRequest(Void.class, callback, request , false , true);

	}

	public static void updatePermission(Integer contact_uid, Integer sharing, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_uid", contact_uid);
		qPara.setProperty("sharing", sharing);
		qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
		qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
		QueryParametersRequest request = new QueryParametersRequest(UPDATE_PERMISSION, qPara);
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}

    public static void contactRequestSend(Integer contact_uid, Integer sharing, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<Void> callback , boolean bProgressDialog)
    {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("contact_uid", contact_uid);
        qPara.setProperty("sharing", sharing);
        qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
        qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
        QueryParametersRequest request = new QueryParametersRequest(SEND_REQUEST, qPara);
        _sendRequest(Void.class, callback, request , true , bProgressDialog);
    }

    public static void contactRequestSend(String email, Integer sharing, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<Void> callback , boolean bProgressDialog)
    {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("email", email);
        qPara.setProperty("sharing", sharing);
        qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
        qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
        QueryParametersRequest request = new QueryParametersRequest(SEND_REQUEST, qPara);
        _sendRequest(Void.class, callback, request , true , bProgressDialog);
    }
	
	public static void cancelRequest(String contact_uids, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_uids", contact_uids);
		QueryParametersRequest request = new QueryParametersRequest(CANCEL_REQUEST, qPara);
		_sendRequest(Void.class, callback, request);
	}
	

	public static void getSuggestions(Integer pageNum, Integer countPerPage, String searchWord, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("q", searchWord);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(LIST_SUGGESION, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void addNotes(Integer contact_uid, String notes, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_uid", contact_uid);
		qPara.setProperty("notes", notes);
		QueryParametersRequest request = new QueryParametersRequest(UPDATE_CONTACT_NOTE, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void checkCbEmailValid(ResponseCallBack<JSONObject> callback) {
		QueryParametersRequest request = new QueryParametersRequest(CHECK_ALL_CB_VALID, get);
		_sendRequest(JSONObject.class, callback, request);
	}
	

	public static void  addInvitation(String email, String phone ,  ResponseCallBack<List<JSONObject>> callback) {
		Type type = new TypeToken<List<JSONObject>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("phone", phone);
		QueryParametersRequest request = new QueryParametersRequest(ADD_INVITATION, qPara);
		_sendRequest(type, callback, request);
	}
	
	
	public static void answerExchangeRequest(Integer contact_uid, Integer answer, String sharedHomeFieldIds, String sharedWorkFieldIds, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id", contact_uid);
		qPara.setProperty("answer", answer);
		qPara.setProperty("shared_home_fids", sharedHomeFieldIds);
		qPara.setProperty("shared_work_fids", sharedWorkFieldIds);
		QueryParametersRequest request = new QueryParametersRequest(ANSWER_EXCHANGE_REQUEST, qPara);
		_sendRequest(Void.class, callback, request);

	}
	
	
	public static void removeFriend( String contact_uid, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id", contact_uid);
		QueryParametersRequest request = new QueryParametersRequest(REMOVE_FRIENDS, qPara);
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}

	public static void removeFriends( String contact_uids, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_ids", contact_uids);
		QueryParametersRequest request = new QueryParametersRequest(REMOVE_MULTIFRIENDS, qPara);
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}

	public static void deleteInvitation( String emails, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("emails", emails);
		QueryParametersRequest request = new QueryParametersRequest(DELETE_INVITATION, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	
	public static void cancelRequestByEmail( String emails, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("emails", emails);
		QueryParametersRequest request = new QueryParametersRequest(CANCEL_REQUEST_BY_EMAIL, qPara);
		_sendRequest(Void.class, callback, request, false , true);
	}
	
	public static void getPendingExhchangeRequest(Integer pageNum, Integer countPerPage, String searchWord, ResponseCallBack<JSONObject> callback){
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("q", searchWord);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(GET_PENDING_REQUESTS, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void getInvitations(Integer pageNum, Integer countPerPage, String searchWord, ResponseCallBack<JSONObject> callback){
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("q", searchWord);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(GET_INVITATIONS, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void getExchangeInvitations(String searchWord, Integer pageNum, Integer countPerPage, ResponseCallBack<List<JSONObject>> callback , boolean bProgressDialog){
		Type type = new TypeToken<List<JSONObject>>() {}.getType();

		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("q", searchWord);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(EXCHANGE_INVITES, get, qPara);
		_sendRequest(type, callback, request , false , bProgressDialog);

	}

	public static void getExchangeSummary(ResponseCallBack<JSONObject> callback){

		QueryParametersRequest request = new QueryParametersRequest(GET_EXCHANGE_SUMMARY, get);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void checkUsers(JSONObject data, ResponseCallBack<List<JSONObject>> callback) {
		Type type = new TypeToken<List<JSONObject>>() {}.getType();
		QueryParametersRequest request = new QueryParametesWithBodyRequest(CHECK_USERS, data);
		_sendRequest(type, callback, request , false , false);
	}

	public static void inviteGinkoUser(String emails , String phones ,ResponseCallBack<JSONObject> callback){
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", emails);
		qPara.setProperty("phones", phones);
		QueryParametersRequest request = new QueryParametersRequest(INVITE, qPara);
		_sendRequest(JSONObject.class, callback, request , false ,true);
	}
}
