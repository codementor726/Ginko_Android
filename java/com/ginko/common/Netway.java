package com.ginko.common;



import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;


public class Netway {
	private HttpClient httpClient;

	

	private Properties defautlHeaders=new Properties();

	public Netway(){
		 httpClient = new DefaultHttpClient();
	}
	
	public void setJsonHeaders(){
		 this.addHeader("content-type", "application/json");
		 this.addHeader("accept", "application/json");
	}
	
	public void setFormSubmitHeaders(){
		String BOUNDARY = java.util.UUID.randomUUID().toString(); 
//		 this.addHeader("content-type", "multipart/form-data" + ";boundary=" + BOUNDARY);
		 this.addHeader("accept", "application/json");
	}
	
	public Netway(int timeout){
		HttpParams  httpParameters  = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeout * 1000);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.  
	    HttpConnectionParams.setSoTimeout(httpParameters, timeout * 1000);  
	    httpClient = new DefaultHttpClient(httpParameters);
	}
	public void setHeader(Properties head) {
		defautlHeaders = head;
	}
	
	public void addHeader(String key, String value){
		this.defautlHeaders.setProperty(key, value);
	}

	public String postPage(String page) {
		HttpPost postMethod = createPostMethod(page);
		return this.post(postMethod);
	}
	
	public String getPage(String page){
		HttpGet postMethod = new HttpGet(page);
		// postMethod
		// .addRequestHeader(
		// "User-Agent",
		// "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
		// postMethod.addRequestHeader("Referer",
		// "https://ramps.uspto.gov/eram/patentMaintFees.do");
		// postMethod.addRequestHeader("Connection", "keep-alive");
		// postMethod.addRequestHeader("Cookie",
		// "RAMPSsession=370471178.20480.0000");
		// return new String(post(postMethod));
		return getPage(page, new Properties());
	}	
	public String getPage(String page,Charset charSet){
		return getPage(page, new Properties(), charSet);
	}
	
	

	public String getPage(String page, Properties header) {
		return getPage(page, header, null);
	}
	
	public String getPage(String page, Properties header, Charset charSet) {
		HttpGet postMethod = new HttpGet(page);
		// defautlHeaders
		Properties h = new Properties();
		if (defautlHeaders != null) {
			h.putAll(defautlHeaders);
		}
		if (header != null) {
			h.putAll(header);
		}
		addHeadProperties(postMethod, h);
		Logger.debug("add head properties end.");
		HttpResponse httpResp;
		try {
			httpResp = httpClient.execute(postMethod);
			HttpEntity responseEntity = httpResp.getEntity();
			
			String result = this.getResponseFromInputStream(responseEntity.getContent());
			if (result == null) {
				return "";
			}
			Logger.debug("get response . size=" + result.length());
			return result;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		}
		return "";
	}

	private void addHeadProperties(HttpRequestBase postMethod, Properties h) {
		for (Enumeration e = h.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = h.getProperty(key);
			if (value == null || value.equals("")) {
				continue;
			}
			postMethod.addHeader(key, value);
		}
	}

	private HttpPost createPostMethod(String url) {
		HttpPost postMethod = new HttpPost(url);
		addHeadProperties(postMethod, this.defautlHeaders);
		return postMethod;
	}


	
	public String postPage(String page, Properties pdata) {
		HttpPost postMethod = createPostMethod(page);
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		Enumeration keys = pdata.keys();
		for (; keys.hasMoreElements();) {
			String k = (String) keys.nextElement();
			String v = (String) pdata.get(k);
			data.add(new BasicNameValuePair(k, v));
		}
		try {
			HttpEntity	entity = new UrlEncodedFormEntity(data, HTTP.UTF_8);
			postMethod.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Logger.error(e);
		}
		return this.post(postMethod);
	}
	
	public String postPage(String page,  String body) {
		HttpPost postMethod = createPostMethod(page);
		HttpEntity entity =null;
		if (body!=null) {
			try {
				entity = new StringEntity(body , "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Logger.error(e);
			}
		}
		postMethod.setEntity(entity);
		return this.post(postMethod);
	}
	
	public String postPage(String page, Properties pdata, String body) {
		HttpPost postMethod = createPostMethod(page);
		HttpEntity entity =null;
		if (pdata!=null){
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			Enumeration keys = pdata.keys();
			for (; keys.hasMoreElements();) {
				String k = (String) keys.nextElement();
				String v = (String) pdata.get(k);
				data.add(new BasicNameValuePair(k, v));
			}
			try {
				entity = new UrlEncodedFormEntity(data, HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Logger.error(e);
			}
		}
		if (body!=null) {
			try {
				entity = new StringEntity(body);
			} catch (UnsupportedEncodingException e) {
				Logger.error(e);
			}
		}
		postMethod.setEntity(entity);
		return this.post(postMethod);
	}
	


	private String post(HttpPost postMethod) {

		String result = null;
		try {
			Logger.debug("httpclient send out the request.");
			HttpResponse  httpResp = httpClient.execute(postMethod);
			int statusCode = httpResp.getStatusLine().getStatusCode();
			// 301 or 302
			if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
					|| statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
				Header locationHeader = postMethod
						.getFirstHeader("location");
				String location = null;
			}
			Logger.debug("httpclient request complete.");
			HttpEntity responseEntity = httpResp.getEntity();
			
			 result = this.getResponseFromInputStream(responseEntity.getContent());
//			result = postMethod.getResponseBody();
			Logger.debug("httpclient retrieve the response as byte");
		} catch (IllegalArgumentException e) {
			Logger.error(e);
		} catch (IOException e) {
			Logger.error(e);
		} finally {
//			postMethod.releaseConnection();
		}
		Logger.debug("httpclient response the result.");
		return result;

	}

	public String uploadFile(String page, HashMap<String,Object> formdata) {
		if (formdata==null || formdata.size()==0){
			throw new RuntimeException("To upload file, the formdata must be set a File field.");
		}
		String response="";
		try {
			MultipartEntity mpEntity = new MultipartEntity(); 
			HttpPost mPost = createPostMethod(page);

			for (Iterator<Entry<String, Object>> iter = formdata.entrySet().iterator(); iter.hasNext();) {
				Entry<String, Object> entity =  iter.next();
				String k = entity.getKey();
				Object v = entity.getValue();
				if (k == null || v == null){
					continue;
				}
				if (v instanceof String) {
					mpEntity.addPart(k, new StringBody((String)v));

				} else if (v instanceof File) {
					 mpEntity.addPart(k, new FileBody((File)v) );
				} else {
					// Do Nothing;
				}
			}

			mPost.setEntity(mpEntity);

			HttpResponse httpResp = this.httpClient.execute(mPost); 
			HttpEntity responseEntity = httpResp.getEntity();
			response = this.getResponseFromInputStream(responseEntity.getContent());
			// mPost.releaseConnection();
		} catch (FileNotFoundException e) {
			Logger.error(e);
		} catch (IOException e) {
			Logger.error(e);
		}
		return response;
	}



	
	private String getResponseFromInputStream(InputStream is) throws IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line; 
		StringBuffer responseBuff = new StringBuffer();
		
		while ((line = reader.readLine()) != null) {
			
			responseBuff.append(line);
		}
		
		return responseBuff.toString();
	}
}
