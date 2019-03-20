package com.ginko.api.request;

import com.ginko.common.Logger;
import com.ginko.common.MyDataUtils;
import com.ginko.common.NoNullProperties;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.CandidateEachVO;
import com.ginko.vo.CandidateMainVO;
import com.ginko.vo.IMBoardMessage;
import com.ginko.vo.ImBoardMemeberVO;
import com.ginko.vo.ImBoardVO;
import com.ginko.vo.SdpCandidateVO;
import com.ginko.vo.SdpMainVO;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class IMRequest extends GinkoRequest {
	private static final String apiGroup = "/im";

	private static final String checkNew = apiGroup + "/checkNew";
	private static final String getMessageHistory = apiGroup + "/message/history";
	private static final String sendMessage = apiGroup + "/send/message";
	private static final String getBoardInfo = apiGroup + "/board";
	private static final String getMemberInfo = apiGroup + "/getMemberInfo";
	private static final String createBoard = apiGroup + "/board/create";
	private static final String createDirectoryBoard = apiGroup + "/board/directory/create";
	private static final String addMember = apiGroup + "/board/addmember";
	private static final String leaveGroup = apiGroup + "/board/leave";
	private static final String leaveGroups = apiGroup + "/boards/leave";
	private static final String listBoards = apiGroup + "/board/list";
	private static final String deleteMessage = apiGroup + "/delete/message";
	private static final String deleteMessages = apiGroup + "/delete/messages";
	private static final String clearMessage = apiGroup + "/message/clear";
	private static final String sendFile = apiGroup + "/file/send";

	private static final String initalVideo = apiGroup + "/video/start";
	private static final String acceptVideo = apiGroup + "/video/accept";
	private static final String hangupVideo = apiGroup + "/video/hangup";
	private static final String sendSDPdata = apiGroup + "/video/data";
	private static final String getSDPdata = apiGroup + "/video/data/get";
	private static final String inviteNewVideoMember = apiGroup + "/video/addmember";
	private static final String turnVideoStatus = apiGroup + "/video/turnvideo";
	private static final String turnAudioStatus = apiGroup + "/video/turnaudio";
	private static final String callDetail = apiGroup + "/video/detail";


	public static void checkNewMessage(ResponseCallBack<List<IMBoardMessage>> callback) {
		Type type = new TypeToken<List<IMBoardMessage>>() {}.getType();
		AbstractRequest request = new QueryParametersRequest(checkNew, get);
		_sendRequest(type, callback, request , false , false);
	}

	/**
	 * 
	 * @param boardId
	 * @param date , baseDate, such as 2014-12-23 12:23:46
	 * @param number
	 * @param lastDays
	 * @param callback
	 */
	public static void getMessageHistory(int boardId, Date date, Integer number, Integer lastDays, String earlierThan , String laterThan ,
										 ResponseCallBack<List<JSONObject>> callback ,boolean bProgressDailog) {
		Type type = new TypeToken<List<JSONObject>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		if (date!=null){
			qPara.setProperty("date", MyDataUtils.format(date));
		}
		qPara.setProperty("number", number);
		qPara.setProperty("lastDays", lastDays);
		qPara.setProperty("earlier_than", earlierThan);
		qPara.setProperty("later_than", laterThan);

		AbstractRequest request = new QueryParametersRequest(getMessageHistory + "/" + boardId, get, qPara);
		_sendRequest(type, callback, request , true , bProgressDailog);
	}
	
	public static void sendMessage(int boardId, String content, ResponseCallBack<JSONObject> callback) {
		JSONObject json = new JSONObject();
		try {
			json.put("content", content);
		} catch (JSONException e) {
			Logger.error(e);
		}
		AbstractRequest request = new QueryParametesWithBodyRequest(sendMessage + "/" + boardId, json);
		_sendRequest(JSONObject.class, callback, request , false , false);
	}
	
	public static void createBoard(String user_ids, ResponseCallBack<ImBoardVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("user_ids", user_ids);
		qPara.setProperty("return_board", true);
		AbstractRequest request = new QueryParametersRequest(createBoard, qPara);
		_sendRequest(ImBoardVO.class, callback, request);
	}

	public static void createDirectoryBoard(Integer groupId, ResponseCallBack<ImBoardVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("group_id", groupId);
		AbstractRequest request = new QueryParametersRequest(createDirectoryBoard, qPara);
		_sendRequest(ImBoardVO.class, callback, request);
	}

	public static void getGetBoardInfo(int boardId , ResponseCallBack<ImBoardVO> callback, boolean progress)
	{
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("board_id", boardId);
		AbstractRequest request = new QueryParametersRequest(getBoardInfo+"/"+String.valueOf(boardId), get , qPara);
		_sendRequest(ImBoardVO.class, callback, request , false , progress);
	}

	public static void getMemberInfo(int boardId, String userIDs, ResponseCallBack<List<ImBoardMemeberVO>> callback, boolean progress)
	{
		Type type = new TypeToken<List<ImBoardMemeberVO>>() {}.getType();

		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("board_id", boardId);
		qPara.setProperty("user_ids", userIDs);
		AbstractRequest request = new QueryParametersRequest(getMemberInfo+"/"+String.valueOf(boardId), get , qPara);
		_sendRequest(type, callback, request , false , progress);
	}
	
	public static void addMember(int boardId, String user_ids, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("user_ids", user_ids);
		AbstractRequest request = new QueryParametersRequest(addMember + "/" + boardId, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void leaveGroup(int boardId,  ResponseCallBack<Void> callback) {
		AbstractRequest request = new QueryParametersRequest(leaveGroup + "/" + boardId);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void leaveGroups(String board_ids,  ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("board_ids", board_ids);
		AbstractRequest request = new QueryParametersRequest(leaveGroups ,qPara);
		_sendRequest(Void.class, callback, request , false , bProgressDialog);
	}
	
	public static void listBoards(Integer number,  ResponseCallBack<List<ImBoardVO>> callback) {
		Type type = new TypeToken<List<ImBoardVO>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("number", number);
		AbstractRequest request = new QueryParametersRequest(listBoards, get,qPara );
		_sendRequest(type, callback, request);
	}
	
	public static void deleteMessage(int boardId, int msg_id, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("board_id", boardId);
		AbstractRequest request = new QueryParametersRequest(deleteMessage + "/" + msg_id,qPara );
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}
	
	public static void clearAllMessage(int boardId, ResponseCallBack<Void> callback) {
		AbstractRequest request = new QueryParametersRequest(clearMessage + "/" + boardId );
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	public static void deleteMessages(int boardId, String msg_ids, ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("msg_ids", msg_ids);
		AbstractRequest request = new QueryParametersRequest(deleteMessages + "/" + boardId,qPara );
		_sendRequest(Void.class, callback, request , true , bProgressDialog);
	}
	
	public static void setReadStatus(int boardId, String msg_ids, boolean isRead,  ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("board_id", boardId);
		qPara.setProperty("msg_ids", msg_ids);
		String readOrUnread =  isRead?"read":"unread";
		AbstractRequest request = new QueryParametersRequest(apiGroup + "/" + readOrUnread + "/messages/" + boardId,qPara );
		_sendRequest(Void.class, callback, request ,false , false);
	}
	
	public static void sendFile(Integer boardId, String filetype, File  thumbnail, File file, Properties attachParameters,
			ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("file_type", filetype);
        if (attachParameters!=null){
            qPara.putAll(attachParameters);
        }
		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("thumbnail", thumbnail);
		formData.put("file", file);
		AbstractRequest request = new FormSubmitRequest(sendFile + "/" + boardId, qPara,
				formData);
		request.setFormData(formData);
		_sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
	}

	public static void setInitalVideo(int boardId, int callType, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("callType", callType);
		AbstractRequest request = new QueryParametersRequest(initalVideo + "/" + boardId, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void setAcceptVideo(int boardId, String user_ids, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("user_ids", user_ids);
		AbstractRequest request = new QueryParametersRequest(acceptVideo + "/" + boardId, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void setInviteNewVideoMember(int boardId, String user_ids, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("user_ids", user_ids);
		AbstractRequest request = new QueryParametersRequest(inviteNewVideoMember + "/" + boardId, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void setHangupVideo(int boardId, int endType, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		AbstractRequest request = new QueryParametersRequest(hangupVideo + "/" + boardId, qPara);
		qPara.setProperty("endType", endType);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void setSendSDPdata(int boardId, boolean isCandidate, String data1, String data2, ResponseCallBack<JSONObject> callback) {
		SdpCandidateVO data = new SdpCandidateVO();
		if (isCandidate)
			data.setCandidates(data1);
		else
			data.setSdp(data1);

		data.setToUserId(data2);
		QueryParametersRequest request = new QueryParametesWithBodyRequest(sendSDPdata + "/" + boardId, data);
		_sendRequest(JSONObject.class, callback, request, false, true);
	}

	public static void setSendSDPdata(int boardId, JSONObject finalData, ResponseCallBack<Void> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(sendSDPdata + "/" + boardId, finalData);
		_sendRequest(Void.class, callback, request);
	}

	public static void setGetSDPdata(int boardId, String dataType, String from, ResponseCallBack<SdpMainVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("dataType", dataType);
		qPara.setProperty("from", from);

		QueryParametersRequest request = new QueryParametersRequest(getSDPdata + "/" + boardId, get, qPara);
		_sendRequest(SdpMainVO.class, callback, request , false , false);
	}

	public static void setGetCandidateData(int boardId, String dataType, String from, ResponseCallBack<CandidateMainVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("dataType", dataType);
		qPara.setProperty("from", from);

		QueryParametersRequest request = new QueryParametersRequest(getSDPdata + "/" + boardId, get, qPara);
		_sendRequest(CandidateMainVO.class, callback, request , false , false);
	}

	public static void turnStatusOfVideoConference(int boardId, String status, ResponseCallBack<JSONObject> callBack) {
		NoNullProperties qPara = new NoNullProperties();

		AbstractRequest request = new QueryParametersRequest(turnVideoStatus + "/" + status + "/" + boardId, qPara);
		_sendRequest(Void.class, callBack, request);
	}

	public static void turnStatusOfVoiceConference(int boardId, String status, ResponseCallBack<JSONObject> callBack) {
		NoNullProperties qPara = new NoNullProperties();

		AbstractRequest request = new QueryParametersRequest(turnAudioStatus + "/" + status + "/" + boardId, qPara);
		_sendRequest(Void.class, callBack, request);
	}

	public static void checkVideoCallDetail(int boardId, ResponseCallBack<JSONObject> callBack) {
		NoNullProperties qPara = new NoNullProperties();

		QueryParametersRequest request = new QueryParametersRequest(callDetail + "/" + boardId, get, qPara);
		_sendRequest(JSONObject.class, callBack, request , false , false);
	}
}
