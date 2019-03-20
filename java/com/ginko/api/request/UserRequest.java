package com.ginko.api.request;

import android.content.Context;

import com.ginko.activity.contact.ContactMainActivity.FilterType;
import com.ginko.common.NoNullProperties;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.IGinkoRequest;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.LoginEmailVO;
import com.ginko.vo.PurpleContactWholeProfileVO;
import com.ginko.vo.UserLoginVO;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class UserRequest  extends GinkoRequest {
	private static final String apiGroup = "/User";
	private static final String CHECK_SESSIONID= "/User/checkSession";
	private static final String API_SIGNIN = "/User/login";
	private static final String LOGIN_WITH_FACEBOOK = "/User/loginByOpenId";
	private static final String SIGNUP = "/User/register";
	private static final String SEND_PASSWORD = "/User/sendpassword";
	private static final String LOGOUT = "/User/logout";
	private static final String SET_WIZARD_PAGE = apiGroup + "/setWizardpage";
	private static final String READ_CONTACT = apiGroup + "/read/contact";

    private static final String getNotifications = apiGroup + "/notifications/get";
	private static final String SET_NOTIFICATIONS = "/User/notifications/update";
	private static final String CHANGE_PWD = "/User/changePassword";

    private static final String SEARCH_CONTACTS = "/User/contact/search";
    private static final String GET_CONTACTS = "/User/getContacts";
    private static final String GET_CONTACT_DETAIL = "/User/getContactDetail";
    private static final String GET_LOGIN_EMAILS = apiGroup + "/getLoginSettings";
	private static final String removeLogin = apiGroup + "/removeLogin";
    private static final String removeLogins = apiGroup + "/removeLogins";
	private static final String addLogin = apiGroup + "/addLogin";
	private static final String GET_FRIENDS = apiGroup + "/getfriends";
	private static final String listDeactivatedReason = apiGroup + "/list/deactivatedreason";
	private static final String deactivate = apiGroup + "/deactivate";
	private static final String sendValidationLinks = apiGroup + "/sendValidationLinks";

    private static final String getContactSummary = apiGroup + "/contact/summary";

	private static final String getSyncUpdatedContacts = apiGroup + "/syncUpdatedContacts";
	private static final String receivedUpdatedContacts = apiGroup + "/receivedUpdatedContacts";

	private static final String getVerifyCodeBySMS = apiGroup + "/phone/get_verify_code";
	private static final String verifySMSCode = apiGroup + "/phone/verify";

	private static final String setFavoriteContacts = "/User/favorite/contact";
	private static final String unsetFavoriteContacts = "/User/unfavorite/contact";

	public static void forgotPassword(String email,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		IGinkoRequest request = new QueryParametersRequest(SEND_PASSWORD, false,
				qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void signUp(String email, String password,
			String first_name, String last_name, String deviceUid,
			String deviceToken, ResponseCallBack<UserLoginVO> callback) {
		
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("password", password);
		qPara.setProperty("first_name", first_name);
		qPara.setProperty("last_name", last_name);
		qPara.setProperty("device_uid", deviceUid);
		qPara.setProperty("device_token", deviceToken);
		qPara.setProperty("client_type", "3");
		IGinkoRequest request = new QueryParametersRequest(SIGNUP, false,
				qPara);
		
		_sendRequest(UserLoginVO.class, callback, request , false , true);
	}

	public static void login(String email,
			String password, String deviceUid, String deviceToken,
			final ResponseCallBack<UserLoginVO> callback) {
		
		
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("password", password);
		qPara.setProperty("device_uid", deviceUid);
		qPara.setProperty("device_token", deviceToken);
		qPara.setProperty("client_type", "3");
		IGinkoRequest request = new QueryParametersRequest(API_SIGNIN, false,
				qPara);
		Type type = new TypeToken<UserLoginVO>() {}.getType();
		_sendRequest(type, callback, request , false , true);
	}
	
	public static void loginWithFacebook(String email,
			String accessToken,
			final ResponseCallBack<UserLoginVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		qPara.setProperty("access_token", accessToken);
		qPara.setProperty("device_uid", Uitils.getDeviceUid());
		qPara.setProperty("device_token", Uitils.getDeviceToken());
		qPara.setProperty("client_type", "3");
		IGinkoRequest request = new QueryParametersRequest(LOGIN_WITH_FACEBOOK, false, qPara);
	
		_sendRequest(UserLoginVO.class, callback, request);
	}


	public static void checkSessionId(final Context context, String sessionId,
			ResponseCallBack<UserLoginVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sessionId", sessionId);
		qPara.setProperty("forUser", true);
		QueryParametersRequest request = new QueryParametersRequest(
				CHECK_SESSIONID, get, false, qPara);
		_sendRequest(UserLoginVO.class, callback, request, false, false);

	}

	public static void logout(
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
//		qPara.setProperty("sessionId", sessionId);
		QueryParametersRequest request = new QueryParametersRequest(LOGOUT, qPara);
		_sendRequest(Void.class, callback, request);

	}

	public static void getContacts(Integer filterType, String search, String sortBy, ResponseCallBack<List<JSONObject>> callback , boolean bProgressDialog) {
		Type type = new TypeToken<List<JSONObject>>() {}.getType();
		
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("category", filterType);
		qPara.setProperty("search", search);
		qPara.setProperty("sortby", sortBy);
		QueryParametersRequest request = new QueryParametersRequest(GET_CONTACTS, get, qPara);
		_sendRequest(type, callback, request, true, bProgressDialog);
	}

	public static void getVerifyCodeBySMS(String phone_num , ResponseCallBack<JSONObject> callback , boolean bProgressDialog)
	{
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("phone_num", phone_num);
		QueryParametersRequest request = new QueryParametersRequest(getVerifyCodeBySMS, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}

	public static void verifyPhoneCode(String phone_num , String verify_code , ResponseCallBack<JSONObject> callback , boolean bProgressDialog)
	{
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("phone_num", phone_num);
		qPara.setProperty("verify_code", verify_code);
		QueryParametersRequest request = new QueryParametersRequest(verifySMSCode, qPara);
		_sendRequest(Void.class, callback, request , false , bProgressDialog);
	}

	public static void receivedUpdatedContacts(String syncTimeStamp, ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {

		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sync_timestamp", syncTimeStamp);
		QueryParametersRequest request = new QueryParametersRequest(receivedUpdatedContacts, get, qPara);
		_sendRequest(JSONObject.class, callback, request, true, bProgressDialog);
	}

	public static void getSyncUpdatedContacts(String syncTimeStamp,  ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {

		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sync_timestamp", syncTimeStamp);
		QueryParametersRequest request = new QueryParametersRequest(getSyncUpdatedContacts, get, qPara);
		_sendRequest(JSONObject.class, callback, request, true, bProgressDialog);
	}

	public static void searchContacts(String searchKeyWord , int pageNum , int countPerPage , ResponseCallBack<List<JSONObject>> callback , boolean bProgressDialog)
    {

        Type type = new TypeToken<List<JSONObject>>(){}.getType();
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("q" , searchKeyWord);
        qPara.setProperty("pageNum" , String.valueOf(pageNum));
        qPara.setProperty("countPerPage" , String.valueOf(countPerPage));
        QueryParametersRequest request = new QueryParametersRequest(SEARCH_CONTACTS, get, qPara);
        _sendRequest(type, callback, request,true, bProgressDialog);
    }

    public static void getContactSummary(ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sessionId", RuntimeContext.getSessionId());

        QueryParametersRequest request = new QueryParametersRequest(getContactSummary, get, qPara);
        _sendRequest(JSONObject.class, callback, request,true, false);
    }

    public static void getContactDetail(String contactId , String contactType , ResponseCallBack<PurpleContactWholeProfileVO> callback)
    {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("contact_id" , contactId);
        qPara.setProperty("contact_type" , contactType);

        QueryParametersRequest request = new QueryParametersRequest(GET_CONTACT_DETAIL , get , qPara);
        _sendRequest(PurpleContactWholeProfileVO.class , callback , request , true , true);

    }

	public static void getContactDetail(String contactId , String contactType , ResponseCallBack<PurpleContactWholeProfileVO> callback, boolean showAlert)
	{
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id" , contactId);
		qPara.setProperty("contact_type" , contactType);

		QueryParametersRequest request = new QueryParametersRequest(GET_CONTACT_DETAIL , get , qPara);
		_sendRequest(PurpleContactWholeProfileVO.class , callback , request , showAlert , true);

	}

	public static void getContactDetailJSON(String contactId , String contactType , ResponseCallBack<JSONObject> callback)
	{
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id" , contactId);
		qPara.setProperty("contact_type" , contactType);

		QueryParametersRequest request = new QueryParametersRequest(GET_CONTACT_DETAIL , get , qPara);
		_sendRequest(JSONObject.class , callback , request , false , false);

	}

	public static void setWizardpage(String setupPage,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("setup_page", setupPage);
		QueryParametersRequest request = new QueryParametersRequest(SET_WIZARD_PAGE, qPara);
		_sendRequest(Void.class, callback, request , true , true);
	}
	
	public static void readContact( Integer contact_id,Integer contact_type,
			ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id", contact_id);
		qPara.setProperty("contact_type", contact_type);
		QueryParametersRequest request = new QueryParametersRequest(READ_CONTACT, qPara);
		_sendRequest(Void.class, callback, request , false , bProgressDialog);
	}
	
	public static void changePassword(String password, String newPassword,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("curpwd", password);
		qPara.setProperty("newpwd", newPassword);
		QueryParametersRequest request = new QueryParametersRequest(CHANGE_PWD, qPara);
		_sendRequest(Void.class, callback, request, false);
	}
	
	public static void setNotifications(Boolean exchgReq, Boolean chatMsg, Boolean entityMsg, Boolean sprout,Boolean bProfileUpdate , ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("exchange_request", exchgReq);
		qPara.setProperty("chat_msg", chatMsg);
		qPara.setProperty("entity", entityMsg);
		qPara.setProperty("sprout", sprout);
		qPara.setProperty("profile_change", bProfileUpdate);
		QueryParametersRequest request = new QueryParametersRequest(SET_NOTIFICATIONS, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	public static void addLogin(String email, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		QueryParametersRequest request = new QueryParametersRequest(addLogin, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void getLoginEmails(ResponseCallBack<List<LoginEmailVO>> callback) {
        Type type = new TypeToken<List<LoginEmailVO>>() {}.getType();
		QueryParametersRequest request = new QueryParametersRequest(GET_LOGIN_EMAILS, get);
		_sendRequest(type, callback, request);
	}
	
	public static void removeLogin(String email, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("email", email);
		QueryParametersRequest request = new QueryParametersRequest(removeLogin, qPara);
		_sendRequest(Void.class, callback, request);
	}
	public static void removeLogins(String emails, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("emails", emails);
		QueryParametersRequest request = new QueryParametersRequest(removeLogins, qPara);
		_sendRequest(Void.class, callback, request);
	}

	
	public static void getfriends(String search, String sortBy,ResponseCallBack<JSONObject> callback) {

		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("search", search);
		qPara.setProperty("sortby", sortBy);
		QueryParametersRequest request = new QueryParametersRequest(GET_FRIENDS, get,qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

    public static void listDeactivatedReason( ResponseCallBack<List<JSONObject>> callback) {
        Type type = new TypeToken<List<JSONObject>>() {}.getType();

        QueryParametersRequest request = new QueryParametersRequest(listDeactivatedReason, get);
        _sendRequest(type, callback, request,true, false);
    }

    public static void deactivate(String password, Integer reasonCode, String otherReason, ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("password", password);
        qPara.setProperty("reason_code", reasonCode);
        qPara.setProperty("other_reason", otherReason);
        QueryParametersRequest request = new QueryParametersRequest(deactivate, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void sendValidationLinks(String emails, ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("emails", emails);
        QueryParametersRequest request = new QueryParametersRequest(sendValidationLinks, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void getNotifications( ResponseCallBack<JSONObject> callback) {
        QueryParametersRequest request = new QueryParametersRequest(getNotifications, get);
        _sendRequest(JSONObject.class, callback, request,true, false);
    }

	public static void setFavoriteContacts( Integer contact_id,String contact_type,
									ResponseCallBack<JSONObject> callback ) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id", contact_id);
		qPara.setProperty("contact_type", contact_type);
		QueryParametersRequest request = new QueryParametersRequest(setFavoriteContacts, qPara);
		_sendRequest(JSONObject.class, callback, request , false);
	}

	public static void unsetFavoriteContacts( Integer contact_id,String contact_type,
											ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id", contact_id);
		qPara.setProperty("contact_type", contact_type);
		QueryParametersRequest request = new QueryParametersRequest(unsetFavoriteContacts, qPara);
		_sendRequest(JSONObject.class, callback, request , false);
	}
}
