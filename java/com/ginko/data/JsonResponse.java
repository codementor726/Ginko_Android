package com.ginko.data;

import com.ginko.common.Logger;
import com.ginko.vo.CandidateMainVO;
import com.ginko.vo.SdpMainVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public  class  JsonResponse<T extends Object> extends JsonResponseBase<T >{
	private T data;
	
	private JSONObject jsonData;
	
	private boolean isSuccess;
	private int errorCode  = 0;
	private String errorMsg;
	
	@SuppressWarnings("rawtypes")
	public static JsonResponse NullResponse= new JsonResponse(){
		public int getErrorCode (){
			return 9999;
		}
		public String getErrorMessage() {
			return "Internet connection is missing."; //""Unknown Exception, can't get response from backend. It may be caused by a network issue.";
		}
		public boolean isSuccess(){
			return false;
		}
	};
	
	private JsonResponse (){
		
	}
	
	@SuppressWarnings("unchecked")
	public JsonResponse(String jsonDataStr, Type type){
		try {
			this.jsonData = new JSONObject(jsonDataStr);
			this.isSuccess = isSuccess(jsonData);
			if (this.isSuccess){
				if(type!=null && type != Void.class){
					try {
						Type rawType = type;
						if (type instanceof ParameterizedType){
							rawType = ((ParameterizedType) type).getRawType();
						}
						
						Object jData = jsonData.get("data");
						
						if (jData instanceof JSONObject){
							T json2Object = JsonConverter.json2Object(
									(JSONObject)jData, (Class<T>) rawType);
							this.setData(json2Object);
						}else if (jData instanceof JSONArray){
							if (type.equals(SdpMainVO.class) || type.equals(CandidateMainVO.class))
							{
								JSONObject jIndex = ((JSONArray) jData).getJSONObject(0);
								T json2Object = JsonConverter.json2Object(
										(JSONObject) jIndex, (Class<T>) rawType);
								this.setData(json2Object);
							} else
							{
								List<T> resultData= new ArrayList<T>();
								JSONArray jsonArray = (JSONArray)jData;

								boolean isFlag = false;
								if (type instanceof ParameterizedType)
									isFlag = true;

								Class<T> voClass = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];

								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject jo = jsonArray.getJSONObject(i);
									Object json2Object = JsonConverter.json2Object(jo,
											voClass);
									resultData.add((T)json2Object);
								}
								this.setData((T)resultData);
							}
						}
					} catch (JsonConvertException e) {
						this.errorCode = 9998;
						this.errorMsg = "The response from server side is illegal.";
						e.printStackTrace();
						//Logger.error(e);
					}
				}
			}else{
				this.errorCode = getErrorCode(jsonData);
				this.errorMsg = getErrorMessage(jsonData);
			}

		} catch (JSONException e) {
			Logger.error(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Class getGenericClass(Method cls) {
		int index = 0; // In the case, you only have a generic type, so index is
						// 0 to get the first one.
		Type genType = cls.getGenericReturnType();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			throw new RuntimeException("Index outof bounds");
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class) params[index];
	}
	
	public int getErrorCode (){
		return this.errorCode;
	}
	
	public String getErrorMessage() {
		return this.errorMsg;
	}
	
	public boolean isSuccess(){
		return isSuccess(jsonData);
	}

	public boolean isTrueData() {
		return isTrueData(jsonData);
	}
	
	protected int getErrorCode(JSONObject response)  {
		JSONObject err = response.optJSONObject("err");
		if (err==null){
			err = response;
		}
		return err.optInt("errCode");
	}
	
	public String getErrorMessage(JSONObject response) {
		JSONObject err = response.optJSONObject("err");
		if (err==null){
			err = jsonData;
		}
		return err.optString("errMsg");
	}

	protected static JSONObject getResponseData(JSONObject json)
			throws JSONException {
		return json.getJSONObject("data");
	}

	protected JSONArray getResponseArray(String response) throws JSONException {
		JSONObject json = new JSONObject(response);
		return json.optJSONArray("data");
	}
	
	protected JSONArray getResponseArray(JSONObject json) throws JSONException {
		return json.optJSONArray("data");
	}

	protected JSONObject getResponseData(String response) throws JSONException {

		JSONObject json = new JSONObject(response);
		return json.optJSONObject("data");
	}

	protected int getErrorCode(String jsonString) throws JSONException {
		JSONObject json = new JSONObject(jsonString);
		return json.getJSONObject("err").getInt("errCode");
	}

	protected static boolean isSuccess(String jsonString) throws JSONException {
		JSONObject json = new JSONObject(jsonString);
		return json.getBoolean("success");
	}

	protected static boolean isSuccess(JSONObject json) {
        if (json==null){
            return false;
        }
		return json.optBoolean("success");
	}

	protected static boolean isTrueData(JSONObject json) {
		if (json==null){
			return false;
		}
		return json.optBoolean("data");
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}


}
