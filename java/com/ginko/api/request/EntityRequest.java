package com.ginko.api.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.ginko.common.NoNullProperties;
import com.ginko.common.RuntimeContext;
import com.ginko.data.IGinkoRequest;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityInfoVO;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.PageCategory;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.TcVideoVO;
import com.google.gson.reflect.TypeToken;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class EntityRequest extends GinkoRequest{
	private static final String apiGroup = "/entity";
	private static final String getAllCategories= apiGroup + "/category/listall";
	private static final String getCategoryById= apiGroup + "/category/get";
	private static final String listEntities= apiGroup + "/list";
	private static final String getEntity= apiGroup + "/get";
	private static final String CREATE_WITH_PHOTO= apiGroup + "/create";
	private static final String saveEntity= apiGroup + "/save";
	private static final String deleteEntity= apiGroup + "/delete";
	private static final String saveInfo= apiGroup + "/info/save";
	private static final String deleteInfo= apiGroup + "/info/delete";
	private static final String reOrderInfo= apiGroup + "/info/reorder";
	private static final String sendMessage= apiGroup + "/message/send";
	private static final String sendFile= apiGroup + "/message/sendfile";
	private static final String listMessageBoard= apiGroup + "/message/board/list";
	private static final String listMessageWall= apiGroup + "/follower/messagewall";
	private static final String listMessages= apiGroup + "/message/list";
	private static final String deleteMessages= apiGroup + "/message/delete";
    private static final String clearAllMessages = apiGroup + "/message/clear";
	private static final String inviteFriends= apiGroup + "/invite";
	private static final String deleteFollowers= apiGroup + "/follower/delete";
	private static final String PUT_IMAGE= apiGroup + "/image/upload";
	private static final String uploadMultipleImage= apiGroup + "/image/multiple/upload";
	private static final String uploadVideo= apiGroup + "/video/upload";
	private static final String removeImage= apiGroup + "/image/remove";
	private static final String removeProfileImage= apiGroup + "/profile/image/remove";
	private static final String removeVideo= apiGroup + "/video/remove";
	private static final String uploadProfileImage= apiGroup + "/profile/image/upload";
	private static final String listContacts= apiGroup + "/listContacts";
	private static final String listFollowers= apiGroup + "/follower/list";
	private static final String followEntity= apiGroup + "/follower/follow";
	private static final String unFollowEntity= apiGroup + "/follower/unfollow";
	private static final String updateNotes= apiGroup + "/follower/notes/update";
	private static final String viewEntity= apiGroup + "/follower/view_new";
	private static final String follower_total= apiGroup + "/follower/view/summary";
	private static final String entityArchiveImages = apiGroup + "/image/archive/list";
    private static final String deleteArchiveImage = apiGroup + "/image/archive/delete";
    private static final String pickupArchiveImage = apiGroup + "/image/archive/pickup";

    private static final String pickupArchiveVideo = apiGroup + "/video/history/list";
    private static final String deleteArchiveVideo = apiGroup + "/video/history/delete";
    private static final String selectArchiveVideo = apiGroup + "/video/history/select";

    private static final String fetchAllEntity = apiGroup + "/fetch_all";
    private static final String syncEntityUpdated = apiGroup + "/sync_updated";

	private static final String fetchAllNew = apiGroup + "/fetch_all_new";
	private static final String syncEntityNew = apiGroup + "/sync_updated_new";

	public static void fetchAllEntities( ResponseCallBack<List<JSONObject>> callback) {
		Type type = new TypeToken<List<JSONObject>>() {}.getType();
		QueryParametersRequest request = new QueryParametersRequest(fetchAllNew, get);
		_sendRequest(type, callback, request,true, true);
	}

	public static void syncEntityUpdated( ResponseCallBack<JSONObject> callback) {
		QueryParametersRequest request = new QueryParametersRequest(syncEntityNew, get);
		_sendRequest(JSONObject.class, callback, request,true, true);
	}

    public static void getArchiveImages(Integer entityId, Integer pageNum , Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("entity_id", entityId);
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        IGinkoRequest request = new QueryParametersRequest(entityArchiveImages, get, qPara);
        _sendRequest(JSONObject.class, callback, request ,false , true);
    }

    public static void deleteArchiveImage(Integer entityId,Integer archive_id,
                                    ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("archive_id", archive_id);
        qPara.setProperty("entity_id", entityId);
        QueryParametersRequest request = new QueryParametersRequest(deleteArchiveImage, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

    public static void pickupArchiveImage( Integer entityId , Integer archive_id,  ResponseCallBack<List<EntityImageVO>> callback) {
		Type clstype = new TypeToken<List<EntityImageVO>>() {}.getType();

		NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("archive_id", archive_id);
        qPara.setProperty("entity_id", entityId);
        IGinkoRequest request = new QueryParametersRequest(pickupArchiveImage, qPara);
        _sendRequest(clstype, callback, request , false , true);
    }

	public static void getAllCategories(
			ResponseCallBack<List<PageCategory>> callback) {
		Type type = new TypeToken<List<PageCategory>>() {}.getType();
		QueryParametersRequest request = new QueryParametersRequest(getAllCategories, get);
		_sendRequest(type, callback, request);

	}
	
	public static void getCategoryById(Integer id,
			ResponseCallBack<PageCategory> callback) {
		QueryParametersRequest request = new QueryParametersRequest(getCategoryById + "/" + id, get);
		_sendRequest(PageCategory.class, callback, request);
	}
	
	public static void listEntities(ResponseCallBack<List<EntityVO>> callback) {
		Type type = new TypeToken<List<EntityVO>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("sessionId", RuntimeContext.getSessionId());

		QueryParametersRequest request = new QueryParametersRequest(listEntities, get);
		_sendRequest(type, callback, request);
	}
	
	public static void getEntity(Integer entityId,
			ResponseCallBack<EntityVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(getEntity, get, qPara);
		_sendRequest(EntityVO.class, callback, request);
	}
	
	public static void createEntity(Integer categoryId,String name,String searchWords,Integer privilege,File background, File frontground,
			ResponseCallBack<EntityVO> callback) {
		NoNullProperties pros = new NoNullProperties();
		pros.setProperty("category_id", categoryId);
		pros.setProperty("name", name);
		pros.setProperty("search_words", searchWords);
		pros.setProperty("privilege", privilege);
		HashMap<String, Object> formData = new HashMap<String, Object>();
		if(background != null) {
			formData.put("background", background);
		}
		if(frontground != null)
			formData.put("frontground", frontground);
		AbstractRequest request = new FormSubmitRequest(CREATE_WITH_PHOTO, pros,
				formData);
		request.setFormData(formData);
		_sendRequest(EntityVO.class, callback, request, false, true);
	}
	
	public static void saveEntity(EntityVO data, ResponseCallBack<EntityVO> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(saveEntity, data);
		_sendRequest(EntityVO.class, callback, request, false , true);
	}

	public static void updateEntity(Integer entityId,String name, String description, String info, String deleteIDs, String searchWords, Integer privilege, ResponseCallBack<EntityVO> callback)  {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("name", name);
		qPara.setProperty("description", description);
		qPara.setProperty("infos", info);
		qPara.setProperty("delete_info_ids", deleteIDs);
		qPara.setProperty("search_words", searchWords);
		qPara.setProperty("privilege", privilege);

		QueryParametersRequest request = new QueryParametersRequest(saveEntity, qPara);
		_sendRequest(EntityVO.class, callback, request , false , true);
	}
	
	public static void deleteEntity(Integer entityId,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(deleteEntity, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	public static void saveInfo(Integer entityId, EntityInfoVO info, ResponseCallBack<EntityInfoVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametesWithBodyRequest(saveInfo, qPara ,info);
		_sendRequest(EntityInfoVO.class, callback, request , false , true);
	}
	
	public static void deleteInfo(Integer entityId, Integer infoId,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("info_id", infoId);
		QueryParametersRequest request = new QueryParametersRequest(deleteInfo, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void reOrderInfo(EntityVO data, ResponseCallBack<Void> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(reOrderInfo, data);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void sendMessage(Integer entityId, String content,
			ResponseCallBack<EntityMessageVO> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("content", content);
		QueryParametersRequest request = new QueryParametersRequest(sendMessage, qPara);
		_sendRequest(EntityMessageVO.class, callback, request , false , bProgressDialog);
	}
	
	public static void sendFile(Integer entityId,String filetype, File thumbnail, File file,
			ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties pros = new NoNullProperties();
		pros.setProperty("entity_id", entityId);
		pros.setProperty("file_type", filetype);
		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("file", file);
		formData.put("thumbnail", thumbnail);
		AbstractRequest request = new FormSubmitRequest(sendFile, pros,
				formData);
		request.setFormData(formData);
		_sendRequest(JSONObject.class, callback, request , false , bProgressDialog);
	}
	
	public static void listMessageBoard( Integer pageNum,Integer countPerPage, Integer messageNum, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("message_num", messageNum);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(listMessageBoard, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void listMessageWall( Integer pageNum,Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(listMessageWall, get, qPara);
		_sendRequest(JSONObject.class, callback, request, false, false);
	}
	
	public static void listMessages(Integer entityId, Integer pageNum,Integer countPerPage, ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(listMessages, get, qPara);
		_sendRequest(JSONObject.class, callback, request ,  true , bProgressDialog);
	}
	
	public static void deleteMessages(Integer entityId, String messageIds,
			ResponseCallBack<Void> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("msg_ids", messageIds);
		QueryParametersRequest request = new QueryParametersRequest(deleteMessages, qPara);
		_sendRequest(Void.class, callback, request , false , bProgressDialog);
	}

    public static void clearAllMessages(Integer entityId,
                                      ResponseCallBack<Void> callback , boolean bProgressDialog) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("entity_id", entityId);
        QueryParametersRequest request = new QueryParametersRequest(clearAllMessages, qPara);
        _sendRequest(Void.class, callback, request , false , bProgressDialog);
    }


	public static void inviteFriends(Integer entityId, String contact_uids,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("contact_uids", contact_uids);
		QueryParametersRequest request = new QueryParametersRequest(inviteFriends, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void deleteFollowers(Integer entityId, String contact_uids,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("contact_uids", contact_uids);
		QueryParametersRequest request = new QueryParametersRequest(deleteFollowers, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}
	
	public static void putImage(Integer entityId, Float width, Float height, Float top, Float left, Integer zindex, Integer imageId, File file, ResponseCallBack<EntityImageVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("width", width);
		qPara.setProperty("height", height);
		qPara.setProperty("top", top);
		qPara.setProperty("left", left);
		qPara.setProperty("z_index", zindex);
		qPara.setProperty("image_id", imageId);

		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("image", file);
		AbstractRequest request = new FormSubmitRequest(PUT_IMAGE, qPara, formData);
		request.setFormData(formData);
		_sendRequest(EntityImageVO.class, callback, request);
	}

	public static void uploadMultipleImage(Integer entityId,File background, File frontground, boolean bShowProgress,
			ResponseCallBack<List<EntityImageVO> > callback) {
		Type type = new TypeToken<List<EntityImageVO>>() {}.getType();
		NoNullProperties pros = new NoNullProperties();
		pros.setProperty("entity_id", entityId);
		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("frontground", frontground);
		formData.put("background", background);
		AbstractRequest request = new FormSubmitRequest(uploadMultipleImage, pros,
				formData);
		request.setFormData(formData);
		_sendRequest(type, callback, request, false, bShowProgress);
	}
	
	public static void uploadVideo(Integer entityId,File video, File thumbnail , boolean bShowProgress, ResponseCallBack<JSONObject> callback) {
		NoNullProperties pros = new NoNullProperties();
		pros.setProperty("entity_id", entityId);

        HashMap<String, Object> formData = new HashMap<String, Object>();
        formData.put("video", video);
        formData.put("thumbnail", thumbnail);
        AbstractRequest request = new FormSubmitRequest(uploadVideo, pros, formData);
        request.setFormData(formData);

		_sendRequest(JSONObject.class, callback, request , false , bShowProgress);
	}
	
	public static void removeImage(Integer entityId, Integer imageId,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("image_id", imageId);
		QueryParametersRequest request = new QueryParametersRequest(removeImage, qPara);
		_sendRequest(Void.class, callback, request);
	}

	public static void removeProfileImage(Integer entityId , ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(removeProfileImage, qPara);
		_sendRequest(Void.class, callback, request , true , true);
	}
	
	public static void removeVideo(Integer entityId,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(removeVideo, qPara);
		_sendRequest(Void.class, callback, request);
	}
	
	public static void uploadProfileImage(Integer entityId,File image, boolean showProgress,
			ResponseCallBack<JSONObject > callback) {
		NoNullProperties pros = new NoNullProperties();
		pros.setProperty("entity_id", entityId);
		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("image", image);
		AbstractRequest request = new FormSubmitRequest(uploadProfileImage, pros,
				formData);
		request.setFormData(formData);
		_sendRequest(JSONObject.class, callback, request , false , showProgress);
	}
	
	public static void listContacts(Integer entityId,Boolean onlyInvited,Integer pageNum,Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("only_invited", onlyInvited);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(listContacts, get, qPara);
		_sendRequest(JSONObject.class, callback, request , false  , true);
	}
	
	public static void listFollowers(Integer entityId, Integer pageNum,Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		QueryParametersRequest request = new QueryParametersRequest(listFollowers, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void followEntity(Integer entityId,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(followEntity, qPara);
		_sendRequest(Void.class, callback, request , true , true);
	}
	
	public static void unFollowEntity(Integer entityId,
												   ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(unFollowEntity, qPara);
		_sendRequest(Void.class, callback, request , true , true);
	}
	
	public static void updateNotes(Integer entityId, String notes,
			ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("notes", notes);
		QueryParametersRequest request = new QueryParametersRequest(updateNotes, qPara);
		_sendRequest(Void.class, callback, request , true , true);
	}
	
	public static void viewEntity(Integer entityId, 
			ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(viewEntity, get, qPara);
		_sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
	}

	public static void getFollowerTotal(Integer entityId,
								  ResponseCallBack<JSONObject> callback , boolean bProgressDialog, boolean isShowAlert) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		QueryParametersRequest request = new QueryParametersRequest(follower_total, get, qPara);
		_sendRequest(JSONObject.class, callback, request , isShowAlert , bProgressDialog);
	}

	public static void viewEntity(Integer entityId, Integer infoFrom, Integer infoCount, double latitude, double longitude,
								  ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("info_from", infoFrom);
		qPara.setProperty("info_count", infoCount);
		qPara.setProperty("latitude", latitude);
		qPara.setProperty("longitude", longitude);
		QueryParametersRequest request = new QueryParametersRequest(viewEntity, get, qPara);
		_sendRequest(JSONObject.class, callback, request , true , bProgressDialog);
	}

	public static void getArchiveVideoHistory(Integer entityId, Integer pageNum , Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		IGinkoRequest request = new QueryParametersRequest(pickupArchiveVideo, get, qPara);
		_sendRequest(JSONObject.class, callback, request ,false , false);
	}

	public static void deleteArchiveVideo(Integer videoId, Integer entityId,
								   ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("video_id", videoId);
		QueryParametersRequest request = new QueryParametersRequest(deleteArchiveVideo, qPara);
		_sendRequest(Void.class, callback, request);
	}

	public static void selectArchiveVideo(Integer videoId, Integer entityId,
										  ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("entity_id", entityId);
		qPara.setProperty("video_id", videoId);
		QueryParametersRequest request = new QueryParametersRequest(selectArchiveVideo, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}
}
