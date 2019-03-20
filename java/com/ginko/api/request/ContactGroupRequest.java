package com.ginko.api.request;

import com.ginko.common.NoNullProperties;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.GroupVO;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

public class ContactGroupRequest extends GinkoRequest {
	private static final String apiGroup = "/contact/group";
	private static final String listGroups = apiGroup + "/list";
	private static final String add_group = apiGroup + "/add";
	private static final String delete_group = apiGroup + "/delete";
	private static final String rename_group = apiGroup + "/rename";
	private static final String addUser = apiGroup + "/addUser";
	private static final String removeUser = apiGroup + "/removeUser";
	private static final String getUsers = apiGroup + "/getusers";
	private static final String getDontAddedContacts = apiGroup + "/getDontAddedContacts";
	private static final String changeGroupOrder = apiGroup + "/order/move";
	private static final String changeAllGroupOrder = apiGroup + "/order/batch/move";

	public static void listGroups(ResponseCallBack<List<GroupVO>> callback) {
		Type type = new TypeToken<List<GroupVO>>() {
		}.getType();
		QueryParametersRequest request = new QueryParametersRequest(listGroups, get);
		_sendRequest(type, callback, request , false , true);
	}

	public static void add(String name, ResponseCallBack<GroupVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("name", name);
		QueryParametersRequest request = new QueryParametersRequest(add_group, qPara);
		_sendRequest(GroupVO.class, callback, request , false , true);
	}

	public static void delete(int groupId, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		QueryParametersRequest request = new QueryParametersRequest(delete_group, qPara);
		_sendRequest(Void.class, callback, request , false , false);
	}

	public static void rename(int groupId, String name, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		qPara.setProperty("name", name);
		QueryParametersRequest request = new QueryParametersRequest(rename_group, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	/*
	 * contactKeys's format is 12_1,3_5 ($contactId_$contact_type)
	 */
	public static void addUser(int groupId, String contactKeys, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		qPara.setProperty("contact_keys", contactKeys);
		QueryParametersRequest request = new QueryParametersRequest(addUser, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void removeUser(int groupId, String contactKeys, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		qPara.setProperty("contact_keys", contactKeys);
		QueryParametersRequest request = new QueryParametersRequest(removeUser, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	public static void getUsers(int groupId, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		QueryParametersRequest request = new QueryParametersRequest(getUsers, get,qPara);
		_sendRequest(JSONObject.class, callback, request , false , true);
	}
	
	public static void getDontAddedContacts(int groupId, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		QueryParametersRequest request = new QueryParametersRequest(getDontAddedContacts, get,qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void setChangeGroupOrder(int group_id, int oldOrder, int newOrder, ResponseCallBack<JSONObject> callBack)
	{
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("id", group_id);
		qPara.setProperty("order_num", newOrder);
		qPara.setProperty("old_order_num", oldOrder);
		QueryParametersRequest request = new QueryParametersRequest(changeGroupOrder, post, qPara);
		_sendRequest(JSONObject.class, callBack, request, false , false);
	}

	public static void setAllChangeGroupOrder(JSONObject groupOrderObj , ResponseCallBack<JSONObject> callBack)
	{
		NoNullProperties qPara = new NoNullProperties();
		QueryParametersRequest request = new QueryParametersRequest(changeAllGroupOrder, post, qPara);
		request.setRequestBody(groupOrderObj.toString());
		_sendRequest(JSONObject.class, callBack, request, false, true);
	}
//	public static void setAllChangeGroupOrder(GroupVO groups, ResponseCallBack<Void> callback) {
//		QueryParametersRequest request = new QueryParametesWithBodyRequest(changeAllGroupOrder, groups);
//		_sendRequest(JSONObject.class, callback, request , true , true);
//	}
}
