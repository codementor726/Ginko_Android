package com.ginko.api.request;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import com.ginko.common.NoNullProperties;
import com.ginko.data.ResponseCallBack;
import com.ginko.vo.EntityVO;
import com.ginko.vo.GreyContactVO;

public class GreyContactRequest extends GinkoRequest {
	private static final String apiGroup = "/grey/contact";
	private static final String setPhoto = apiGroup + "/setPhoto";
	private static final String add = apiGroup + "/add";
    private static final String removePhoto = apiGroup + "/removePhoto";

	public static void setPhoto(Integer contact_id, File file,
			ResponseCallBack<JSONObject> callback) {
		NoNullProperties qPara = new NoNullProperties();
		qPara.setProperty("contact_id", contact_id);
		HashMap<String, Object> formData = new HashMap<String, Object>();
		formData.put("photo", file);
		AbstractRequest request = new FormSubmitRequest(setPhoto, qPara,
				formData);
		request.setFormData(formData);
		_sendRequest(JSONObject.class, callback, request);
	}
	
	public static void addGreyContact(GreyContactVO data, ResponseCallBack<GreyContactVO> callback) {
		QueryParametersRequest request = new QueryParametesWithBodyRequest(add, data);
		_sendRequest(GreyContactVO.class, callback, request);
	}

    public static void removePhoto(Integer contactId , ResponseCallBack<Void> callback)
    {
        NoNullProperties qPara = new NoNullProperties();
        qPara.setProperty("contact_id", contactId);
        QueryParametersRequest request = new QueryParametersRequest(removePhoto, post, qPara);
        _sendRequest(Void.class, callback, request , false , true);
    }

}
