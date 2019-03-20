package com.ginko.data;

import java.io.IOException;

public interface ResponseCallBack<T> {
	public void onCompleted(JsonResponse<T> response) throws IOException;
	
	public static final ResponseCallBack<Void> DoNothing = new   ResponseCallBack<Void>(){

		@Override
		public void onCompleted(JsonResponse<Void> response) {
			
		}
		
	};
}
