package com.ginko.api.request;

import com.ginko.common.NoNullProperties;
import com.ginko.data.IGinkoRequest;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.TcVideoVO;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class TradeCard extends GinkoRequest {
	private static final String apiGroup = "/tradecard";
	private static final String DELETE_IMAGE_FROM_HISTORY = apiGroup + "/image/history/delete";
	private static final String listHistroyImages = apiGroup + "/image/history/list";
	private static final String UPLOAD_IMAGE = apiGroup + "/image/upload";
	private static final String PUT_IMAGE = apiGroup + "/image/put";
	private static final String UPLOAD_TWO_IMAGE = apiGroup + "/image/multipleUpload";
	private static final String MOVE_IMAGE = apiGroup + "/image/move";
	private static final String upload_Profile_Image = apiGroup + "/profile/image/upload";
	private static final String remove_Profile_Image = apiGroup + "/profile/image/remove";
	private static final String remove_Image = apiGroup + "/image/remove";
	private static final String upload_video = apiGroup + "/video/upload";
	private static final String remove_video = apiGroup + "/video/delete";

    private static final String get_archivePhotos = apiGroup + "/image/archive/list";
    private static final String deleteArchivePhoto = apiGroup + "/image/archive/delete";
    private static final String archiveImagePicker = apiGroup + "/image/archive/pickup";

    private static final String archiveVideoHistory = apiGroup + "/video/history/list";
    private static final String archiveDeleteVideoHistory = apiGroup + "/video/history/delete";
    private static final String archiveSeleteVideoFromHistory = apiGroup + "/video/history/select";


    public static void deleteImageFromHistory(Integer imageId, ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("image_id", imageId);
        IGinkoRequest request = new QueryParametersRequest(DELETE_IMAGE_FROM_HISTORY, qPara);
        _sendRequest(Void.class, callback, request);
    }


    public static void pickupArchiveImage(Integer archive_id, Integer type ,  ResponseCallBack<List<TcImageVO>> callback) {
		Type clstype = new TypeToken<List<TcImageVO>>() {}.getType();
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("archive_id", archive_id);
        qPara.setProperty("type", type);
        IGinkoRequest request = new QueryParametersRequest(archiveImagePicker, qPara);
        _sendRequest(clstype , callback, request , false , true);
    }

    public static void deleteArchiveImage(Integer archive_id, Integer type ,  ResponseCallBack<Void> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("archive_id", archive_id);
        qPara.setProperty("type", type);
        IGinkoRequest request = new QueryParametersRequest(deleteArchivePhoto, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

	public static void listHistroyImages(Integer pageNum, Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		IGinkoRequest request = new QueryParametersRequest(listHistroyImages, get, qPara);
		_sendRequest(JSONObject.class, callback, request);
	}

    public static void getArchiveImages(Integer type, Integer pageNum , Integer countPerPage, ResponseCallBack<JSONObject> callback) {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("type", type);
        qPara.setProperty("pageNum", pageNum);
        qPara.setProperty("countPerPage", countPerPage);
        IGinkoRequest request = new QueryParametersRequest(get_archivePhotos, get, qPara);
        _sendRequest(JSONObject.class, callback, request,  false  , false);
    }

	public static void uploadImage(File file, ResponseCallBack<JSONObject> callback) {
		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("image", file);
		AbstractRequest request = new FormSubmitRequest(UPLOAD_IMAGE, formData);
		request.setFormData(formData);
		_sendRequest(JSONObject.class, callback, request);
	}

	public static void putImage(Integer type, Float width, Float height, Float top, Float left, Integer zindex, File file, ResponseCallBack<TcImageVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);
		qPara.setProperty("width", width);
		qPara.setProperty("height", height);
		qPara.setProperty("top", top);
		qPara.setProperty("left", left);
		qPara.setProperty("z_index", zindex);

		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("image", file);
		AbstractRequest request = new FormSubmitRequest(PUT_IMAGE, qPara, formData);
		request.setFormData(formData);
		_sendRequest(TcImageVO.class, callback, request , false ,true);
	}

	public static void putMultipleImages(Integer type, File frontGroudImage, File backGroundImage, ResponseCallBack<List<JSONObject>> callback) {
        Type clstype = new TypeToken<List<JSONObject>>() {}.getType();
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);

		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("frontground", frontGroudImage);
		formData.put("background", backGroundImage);
		AbstractRequest request = new FormSubmitRequest(UPLOAD_TWO_IMAGE, qPara, formData);
		request.setFormData(formData);
		_sendRequest(clstype , callback, request , false , true);
	}

	public static void moveImage(TcImageVO tcivo, ResponseCallBack<TcImageVO> callback) {
		IGinkoRequest request = new QueryParametesWithBodyRequest(MOVE_IMAGE, tcivo);
		_sendRequest(TcImageVO.class, callback, request);
	}

	public static void uploadProfileImage(Integer type, File profileImage, ResponseCallBack<JSONObject> callback , boolean bProgressDialog) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);

		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("image", profileImage);
		AbstractRequest request = new FormSubmitRequest(upload_Profile_Image, qPara, formData);
		request.setFormData(formData);
		_sendRequest(JSONObject.class, callback, request , false, bProgressDialog);
	}

	public static void removeProfileImage(Integer type, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);
		IGinkoRequest request = new QueryParametersRequest(remove_Profile_Image, qPara);
		_sendRequest(JSONObject.class, callback, request , true , true);
	}

	public static void removeImage(Integer type, Integer imageId, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("image_id", imageId);
		qPara.setProperty("type", type);
		IGinkoRequest request = new QueryParametersRequest(remove_Image, qPara);
		_sendRequest(Void.class, callback, request);
	}

	public static void uploadVideo(Integer type, File video, File thumbnail , ResponseCallBack<TcVideoVO> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);

		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("video", video);
        formData.put("thumbnail", thumbnail);
		AbstractRequest request = new FormSubmitRequest(upload_video, qPara, formData);
		request.setFormData(formData);
		_sendRequest(TcVideoVO.class, callback, request , false , true);
	}

	public static void deleteVideo(Integer type, Integer videoId, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("video_id", videoId);
		qPara.setProperty("type", type);
		IGinkoRequest request = new QueryParametersRequest(remove_video, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}

	public static void getArchiveVideoHistory(Integer type, Integer pageNum , Integer countPerPage, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("type", type);
		qPara.setProperty("pageNum", pageNum);
		qPara.setProperty("countPerPage", countPerPage);
		IGinkoRequest request = new QueryParametersRequest(archiveVideoHistory, get, qPara);
		_sendRequest(JSONObject.class, callback, request,  false  , false);
	}

	public static void deleteArchiveVideo(Integer type, Integer videoId, ResponseCallBack<Void> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("video_id", videoId);
		qPara.setProperty("type", type);
		IGinkoRequest request = new QueryParametersRequest(archiveDeleteVideoHistory, qPara);
		_sendRequest(Void.class, callback, request , false , true);
	}

	public static void selectArchiveVideoFromHistory(Integer videoId, Integer type, ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("video_id", videoId);
		qPara.setProperty("type", type);
		IGinkoRequest request = new QueryParametersRequest(archiveSeleteVideoFromHistory, qPara);
		_sendRequest(JSONObject.class, callback, request , false , true);
	}
}
