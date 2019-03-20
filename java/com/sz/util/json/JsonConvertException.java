package com.sz.util.json;

public class JsonConvertException extends Exception {

	public JsonConvertException(String msg) {
		super(msg);
	}

	public JsonConvertException(Exception e) {
		super(e);
	}

}
