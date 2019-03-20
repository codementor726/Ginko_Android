package com.ginko.data;

import java.util.HashMap;
import java.util.Properties;

public interface IGinkoRequest {
	String getRequestUrl();

	String getHttpMethod();

	Properties getQueryParameters();

	String getRequestBody();
	
	HashMap<String, Object> getFormData();

    String send();
}
