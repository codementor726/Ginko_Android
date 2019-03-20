package com.ginko.api.request;

import android.os.AsyncTask;
import android.os.Build;

import com.ginko.common.BaseAyncTask;
import com.ginko.common.Logger;
import com.ginko.common.Netway;
import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.data.IGinkoRequest;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class GinkoRequest {
    public static final String formPost = "formPost";
	
	protected static final String get = HttpGet.METHOD_NAME;
	protected static final String post = HttpPost.METHOD_NAME;
	protected static final String postForm = HttpPost.METHOD_NAME;

	public static abstract class AbstractRequest implements IGinkoRequest {
		private boolean needLogin;
		private String httpMethod;
		private Properties queryParameters;
		private String queryStr; //can accept like aa=va&bb=vb
		private String relativeUlr;
		private String requestBody;
		
		private HashMap<String, Object> formData;
		
		public AbstractRequest(String relativeUlr, String httpMethod) {
			this(relativeUlr, httpMethod, true);
		}

		public AbstractRequest(String relativeUlr, String httpMethod,
				boolean needLogin) {
			this.relativeUlr = relativeUlr;
			this.httpMethod = httpMethod;
			this.setNeedLogin(needLogin);
		}

		@Override
		public String getRequestUrl() {
			String url = ConstValues.baseUrl + relativeUlr;
			url = this.createWholeUrl(url);
			return url;
		}

		@Override
		public String getHttpMethod() {
			return this.httpMethod;
		}

		@Override
		public Properties getQueryParameters() {
			if (this.isNeedLogin() && StringUtils.isNotBlank(RuntimeContext.getSessionId())) {
				this.addQueryProperty("sessionId",
						RuntimeContext.getSessionId());
			}
            this.addQueryProperty("version",
                    ConstValues.buildVersion);
			return this.queryParameters;
		}

		@Override
		public String getRequestBody() {
			return this.requestBody;
		}

		public boolean isNeedLogin() {
			return needLogin;
		}

		public void setNeedLogin(boolean needLogin) {
			this.needLogin = needLogin;
		}

		public AbstractRequest addQueryProperty(String name, String value) {
			if (this.queryParameters == null) {
				this.queryParameters = new Properties();
			}
			this.queryParameters.setProperty(name, value);
			return this;
		}

		public void setHttpMethod(String httpMethod) {
			this.httpMethod = httpMethod;
		}

		public void setRequestBody(String requestBody) {
			this.requestBody = requestBody;
		}

		public void setQueryParameters(Properties queryParameters) {
			this.queryParameters = queryParameters;
		}
		
		public	HashMap<String, Object> getFormData(){
			return formData;
		}

		public void setFormData(HashMap<String, Object> formData) {
			this.formData = formData;
		}

		public String getQueryStr() {
			return queryStr;
		}

		public void setQueryStr(String queryStr) {
			this.queryStr = queryStr;
		}
		
		private String createWholeUrl(String url ) {
			StringBuilder sb = new StringBuilder();
			Properties queryParameters = getQueryParameters();
			Set<String> keys = queryParameters.stringPropertyNames();
			sb.append(url);
			
			boolean addSomething= false;
			int i=0;
			for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
				String k  = (String) iterator.next();
				if (i++ == 0){
					sb.append("?");
				}else {
					sb.append("&");
				}
				String v = (String) queryParameters.getProperty(k);
				try {
					sb.append(k).append("=").append(URLEncoder.encode(v, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Logger.error(e);
				}
				addSomething = true;
			}
			
			if (StringUtils.isNotBlank(this.getQueryStr())){
				sb.append(addSomething? "&" : "?");
				sb.append(this.getQueryStr());
			}
			
			return sb.toString();
		}

       public String send(){
           String requestUrl = this.getRequestUrl();
           String method = this.getHttpMethod();
           String requestBody = this.getRequestBody();
//			String wholeUrl= this.createWholeUrl(requestUrl, request.getQueryParameters());
           Logger.debug("requestUrl:" + requestUrl);
           String rawResp="";
           Netway netway = new Netway();
           if (HttpGet.METHOD_NAME.equalsIgnoreCase(method)) {
               netway.setJsonHeaders();
               rawResp= netway.getPage(requestUrl);
           } else if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
               netway.setJsonHeaders();
               rawResp= netway.postPage(requestUrl, requestBody);
           } else if (GinkoRequest.formPost.equalsIgnoreCase(method)) {
               netway.setFormSubmitHeaders();
               HashMap<String, Object> formdata = this.getFormData();
			   if(formdata != null && formData.size() == 0)
			   {
				   formdata.put("random" , System.currentTimeMillis() + "");
			   }
               rawResp = netway.uploadFile(requestUrl, formdata);
           }
           Logger.debug(rawResp);
           return rawResp;
        }
	}
	
	public static class FormSubmitRequest extends QueryParametersRequest {
		public FormSubmitRequest(String relativeUlr, HashMap<String, Object> formData) {
			super(relativeUlr, formPost);
			super.setFormData(formData);
		}

		public FormSubmitRequest(String relativeUlr, Properties queryParameters, HashMap<String, Object> formData) {
			super(relativeUlr, formPost, queryParameters);
			super.setFormData(formData);
		}
	}
	
	public static class QueryParametersRequest extends AbstractRequest{
		public QueryParametersRequest(String relativeUlr) {
			this(relativeUlr, post, new Properties());
		}
		public QueryParametersRequest(String relativeUlr,
                                      Properties queryParameters) {
			this(relativeUlr, post, queryParameters);
		}
		
		public QueryParametersRequest(String relativeUlr, boolean needLogin,
                                      Properties queryParameters) {
			this(relativeUlr, post, needLogin, queryParameters);
		}

		public QueryParametersRequest(String relativeUlr, String httpMethod) {
			this(relativeUlr, httpMethod, true, new Properties());
		}
		
		public QueryParametersRequest(String relativeUlr, String httpMethod,
                                      Properties queryParameters) {
			this(relativeUlr, httpMethod, true, queryParameters);
		}
		
		public QueryParametersRequest(String relativeUlr, String httpMethod,
                                      boolean needLogin, Properties queryParameters){
			super(relativeUlr, httpMethod, needLogin);
			super.setQueryParameters(queryParameters);
		}
	}
	
	public static class QueryParametesWithBodyRequest extends QueryParametersRequest {

		public QueryParametesWithBodyRequest(String relativeUlr, Object bodyObj) {
			super(relativeUlr);
			parseBody(bodyObj);
		}
		
		public QueryParametesWithBodyRequest(String relativeUlr, Properties queryParameters, Object bodyObj) {
			super(relativeUlr,queryParameters);
			parseBody(bodyObj);
		}


		private void parseBody(Object bodyObj) {
			if (bodyObj instanceof String){
				super.setRequestBody((String)bodyObj);
				return;
			}
			if (bodyObj instanceof JSONObject){
				super.setRequestBody(((JSONObject)bodyObj).toString());
				return;
			}
			try {
				String requestBody = JsonConverter.object2JsonString(bodyObj);
				super.setRequestBody(requestBody);
			} catch (JsonConvertException e) {
				Logger.error(e);
				throw new RuntimeException("can't convert the bean to json string. class:" + bodyObj.getClass().getName());
			}
			
		}
		
	}
	
	protected static <T> void _sendRequest(Type clz,
			final ResponseCallBack<T> callback, IGinkoRequest request) {
		_sendRequest(clz,callback,request, false);
	}
	
	protected static <T> void _sendRequest(Type clz,
			final ResponseCallBack<T> callback, IGinkoRequest request, boolean showErrorAlert) {
		_sendRequest(clz,callback,request,showErrorAlert, true);
	}
	
	protected static <T> void _sendRequest(Type clz,
			final ResponseCallBack<T> callback, IGinkoRequest request, boolean showErrorAlert, boolean showProgressDialog) {
        BaseAyncTask<T> requestTask = new BaseAyncTask<T>(clz, showErrorAlert, showProgressDialog) {

			@Override
			protected void _onPostExecute(JsonResponse<T> response) {

				try {
					callback.onCompleted(response);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
        if(Build.VERSION.SDK_INT>=11)
        {
            requestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
        }
        else
            requestTask.execute(request);

	}

}
