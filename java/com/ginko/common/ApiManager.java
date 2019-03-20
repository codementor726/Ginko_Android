package com.ginko.common;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.ginko.data.JsonResponse;
import com.ginko.ginko.R;
import com.ginko.vo.BaseUserVO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ApiManager {

	
/*************************************************************** API PATH ***************************************************************/

	//urls
	private static final String 	BASE_URL = "http://192.168.254.102:8080/xchangeApi";
	public static final String 		IMAGE_URL = "";
	
	private static final String 	API_SIGNIN 	= "/User/login";
	private static final String 	GET_INVITATIONS = "/ContactBuilder/getInvitations";
	private static final String 	DELETE_INVITATION = "/ContactBuilder/deleteInvitation";
	private static final String 	DELETE_REQUEST = "/ContactBuilder/deleteRequest";
	private static final String 	DELETE_SENTINVITATION = "/ContactBuilder/cancelRequest";
	private static final String		GETS_ENTINVITATIONS = "/ContactBuilder/getSentInvitations";
	private static final String		ANSWER_REQUEST = "/ContactBuilder/confirmRequest";
	private static final String		ADD_INVITATION = "/ContactBuilder/addInvitation";
	private static final String		GET_CONTACTS = "/User/getContacts";
	private static final String		GET_MYPHOTO = "/UserInfo/getMyPhoto";
	private static final String		GET_SUGGESTIONS = "/ContactBuilder/list/suggestion";
	private static final String		SET_NOTIFICATIONS = "/User/notifications/update";
	private static final String		LOG_OUT = "/User/logout";
	private static final String		CHANGE_PWD = "/User/changePassword";
	
	//error
	public static final int ERROR_NONE = 0;
	public static final int ERROR_NO_CONNECTION = 1;
	public static final int ERROR_MALFORMED_URL = 2;
	public static final int ERROR_IO = 3;
	public static final int ERROR_JSON = 4;
	public static final int ERROR_REQUEST = 5;
	public static final int ERROR_XML = 6;
	public static final int ERROR_SESSION_EXPIRED = 7;
	
/*************************************************************** Classes ***************************************************************/
	
	
/*************************************************************** Variables ***************************************************************/

	private static 	ApiManager apiClientInstance = null;
	private 		String responseString;
	private static	String serverError = null;
	@SuppressWarnings("unused")
	private static	int lastError = ERROR_NONE;
	
	
	
/*************************************************************** General ***************************************************************/	

	private ApiManager() {}

	public static ApiManager sharedInstance() {
		
		if (apiClientInstance == null) {
		
			apiClientInstance = new ApiManager();
		}
		
		return apiClientInstance;
	}
	
	public static void showErrorMessage(Context context) {
		
		if (serverError == null)
			return;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(context); 
		alert.setTitle("TipHive").setMessage(serverError).setIcon(R.drawable.ic_launcher).setNeutralButton("Ok", null).show();
		
		serverError = null;

	}
	
	private void setResponse(String response) {
		
		this.responseString = response;
	}

	private String getResponse() {
		
		return responseString;
	}
	
	//POST Method
	private boolean doPost(String apiName, List<NameValuePair> params) {
		
		String sUrl = BASE_URL + apiName;

		lastError = ERROR_NONE;
		serverError = null;

		Logger.log("POST: " + sUrl);
		
		HttpEntity entity = null;
		
		if (params != null) {
			if (Logger.showLog()) {
    			for (NameValuePair param: params) {
    				
    				Logger.log(" +" + param.getName() + ": " + param.getValue());
    			}
			}
			
			try {
				
				entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {}
		}
		
		return doPost(apiName, entity);    		
	}

	private boolean doPost(String apiName, HttpEntity entity) {
		
		String sUrl = BASE_URL + apiName;
		HttpPost postRequest = new HttpPost(sUrl);
		
    	HttpClient client = new DefaultHttpClient();
    	HttpResponse response = null;

		lastError = ERROR_NONE;
		serverError = null;

    	try {

    		if (entity != null) {
    			
    			postRequest.setEntity(entity);
    		}
    		
			response = client.execute(postRequest);
			HttpEntity responseEntity = response.getEntity();
			
			if (responseEntity != null) {
				
				getResponseFromInputStream(responseEntity.getContent());
				return true;
			}
			
		} catch (IOException e) {
			
			lastError = ERROR_IO;
		}

    	return false;
	}
	
	private boolean doPostImage(String apiName, HttpEntity entity) {
		
		String sUrl = BASE_URL + apiName;
		HttpPost postRequest = new HttpPost(sUrl);
		
    	HttpClient client = new DefaultHttpClient();
    	HttpResponse response = null;

		lastError = ERROR_NONE;
		serverError = null;

    	try {

    		if (entity != null) {
    			
    			postRequest.setEntity(entity);
    		}
    		
			response = client.execute(postRequest);
			HttpEntity responseEntity = response.getEntity();
			
			if (responseEntity != null) {
				
				getResponseFromInputStream(responseEntity.getContent());
				return true;
			}
			
		} catch (IOException e) {
			
			lastError = ERROR_IO;
		}

    	return false;
	}
	

	private boolean doGet(String apiName, List<NameValuePair> params) {
		
		lastError = ERROR_NONE;
		serverError = null;
		
		String sUrl = BASE_URL + apiName + encodeParams(params);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(sUrl );

		try {
			
			HttpResponse httpResponse = httpClient.execute(getRequest);
			HttpEntity responseEntity = httpResponse.getEntity();
			
			if (responseEntity != null) {
				
				getResponseFromInputStream(responseEntity.getContent());
				return true;
			}
			
			return false;
			
		} catch (IOException e) {

			lastError = ERROR_IO;
			Logger.error(e);
		}
		
		return false;
	}

	private String encodeParams(List<NameValuePair> params) {
		
		if (params == null) {
			
			return "";
		}
		
		StringBuffer paramBuffer = new StringBuffer();
		boolean f1rst = true;
		
		for (NameValuePair param: params) {
			
			paramBuffer.append(f1rst ? "?" : "&");
			paramBuffer.append(param.getName());
			paramBuffer.append("=");
			String value = param.getValue();
			
			if (value == null) {
				
				value = "null";
			} else{
				
				try {
					
					value = URLEncoder.encode(value, "UTF-8");
					
				} catch (UnsupportedEncodingException e) {
					
				}				
			}
			
			paramBuffer.append(value);
			f1rst = false;
		}
		
		return paramBuffer.toString();
	}
	
	private String getResponseFromInputStream(InputStream is) throws IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line; 
		StringBuffer responseBuff = new StringBuffer();
		
		while ((line = reader.readLine()) != null) {
			
			responseBuff.append(line);
		}
		
		setResponse(responseBuff.toString());
		
		Logger.log("Response: " + getResponse());
		
		return getResponse();
	}

	private boolean doDelete(String apiName, List<NameValuePair> params) {
		
		lastError = ERROR_NONE;
		serverError = null;
		
		String sUrl = BASE_URL + apiName + encodeParams(params);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpDelete getRequest = new HttpDelete(sUrl);

		try {
			
			HttpResponse httpResponse = httpClient.execute(getRequest);
			HttpEntity responseEntity = httpResponse.getEntity();
			
			if (responseEntity != null) {
				
				getResponseFromInputStream(responseEntity.getContent());
				return true;
			}
			
			return false;
			
		} catch (IOException e) {

			lastError = ERROR_IO;
			Logger.error(e);
		}
		return false;
	}

	private boolean sendApiRequestImage(String apiName, MultipartEntity params) {
	
		synchronized (apiClientInstance) {
			
			return doPostImage(apiName, params);
		}
	}
	
	private boolean sendApiRequest(String method, String apiName, List<NameValuePair> params) {
		
		synchronized (apiClientInstance) {
			
			if (HttpGet.METHOD_NAME.equalsIgnoreCase(method)) {
				
				return doGet(apiName, params);
			}
			else if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
				
				return doPost(apiName, params);
			}
			else if (HttpDelete.METHOD_NAME.equalsIgnoreCase(method)) {
				
				return doDelete(apiName, params);				
			}
		}

		return false;
	}

	
/*************************************************************** Loading Images ***************************************************************/	
	
	public void loadImage(ImageView imageView, String url) {
		
        new DownloadImageTask(imageView).execute(url);
	}
	
	class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    
		private ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	    	
	        this.bmImage = bmImage;
	    }

	    @Override
	    protected void onPreExecute() {
	    	
	        super.onPreExecute();
	        
//	        pd.show();
	    }

	    protected Bitmap doInBackground(String... urls) {
	        
	    	String urldisplay = urls[0];
	        Bitmap mIcon = null;
	        
	        try {
	        	
	          InputStream in = new java.net.URL(urldisplay).openStream();
	          mIcon = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	        	
	            Logger.error(e);
	        }
	        return mIcon;
	    }

	    @Override 
	    protected void onPostExecute(Bitmap result) {
	    	
	        super.onPostExecute(result);
//	        pd.dismiss();
	        bmImage.setImageBitmap(result);
	    }
	  }

	
	
/*************************************************************** USER AUTH ***************************************************************/	
	
	public boolean signIn (String emailAddress, String password, String udid, String token) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("email", emailAddress));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("device_uid", udid));
		params.add(new BasicNameValuePair("device_token", token));
		params.add(new BasicNameValuePair("client_type", "3"));
		
		String strRequest = API_SIGNIN + "?email=" + emailAddress + "&password=" + password;
		
		if (udid != null && !udid.isEmpty())
			strRequest = strRequest + "&device_uid=" + udid;
		
		if (token != null && !token.isEmpty())
			strRequest = strRequest + "&device_token=" + token;
		
		strRequest = strRequest + "&client_type=3"; 
		
		if (sendApiRequest(HttpPost.METHOD_NAME, strRequest, params)) {
			
			String strResponse = getResponse();
			
			try {
				
				Logger.log(strResponse);
				
				JsonResponse<BaseUserVO> resp = new JsonResponse<BaseUserVO>(strResponse,BaseUserVO.class);
				if (resp.isSuccess()) {
					return true;
					
				} else {
					serverError = resp.getErrorMessage();
				}

			} catch (Exception e) {
				
				lastError = ERROR_JSON;
			}
		}

		return false;
	}

	
}