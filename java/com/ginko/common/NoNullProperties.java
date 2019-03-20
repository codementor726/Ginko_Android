package com.ginko.common;

import org.json.JSONObject;

import java.util.Properties;


/**
 * @author Stony Zhang 20110121
 *
 */
public class NoNullProperties extends Properties {
	private static final long serialVersionUID = 1L;
	private boolean ignoreNullEntry;
	private String replaceStr;

	public NoNullProperties() {
		ignoreNullEntry = true;
	}

	public NoNullProperties(boolean ignoreNullEntry) {
		this.ignoreNullEntry = ignoreNullEntry;
		replaceStr = "";
	}

	public NoNullProperties(String replaceStr) {
		this.ignoreNullEntry = false;
		this.replaceStr = replaceStr;
	}
	
	@Override
    public synchronized Object setProperty(String key, String value) {
    	if(ignoreNullEntry && value==null){
    		return null;
    	}
    	if(value==null){
    		value=this.replaceStr;
    	}
        return put(key, value);
    }

    public synchronized void setProperty(String key, Integer value) {
    	if (value==null){
    		return;
    	}
    	setProperty(key,String.valueOf(value));
    }
    
    public synchronized void setProperty(String key, Boolean value) {
    	if (value==null){
    		return;
    	}
    	setProperty(key,String.valueOf(value));
    }

	public void setProperty(String key, Float value) {
	   	if (value==null){
    		return;
    	}
    	setProperty(key,String.valueOf(value));
		
	}

    public void setProperty(String key, Double value) {
        if (value==null){
            return;
        }
        setProperty(key,String.valueOf(value));
    }

	public synchronized void setProperty(String key, JSONObject value) {
		if (value==null){
			return;
		}
		setProperty(key,String.valueOf(value));
	}
}
