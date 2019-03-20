package com.ginko.customview;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.ginko.common.Logger;

public class AsyncImageLoader extends AsyncTask<Object, Void, Bitmap> {

	private AsyncThumbnailView imv;
	private String path;
	private Integer tag;

	public AsyncImageLoader(String url, AsyncThumbnailView asyncThumbnailView) {
		
		this.imv = asyncThumbnailView;
		this.path = url;
		this.tag = (Integer) asyncThumbnailView.getTag();
		this.imv.setLoadingIndicator(true);
	}

	@Override
	protected Bitmap doInBackground(Object... params) {
		
		Bitmap bitmap = null;
		
//		Bitmap bitmap = ApiClient.instance().getBitmap(path);

        try {
              InputStream in = new java.net.URL(path).openStream();
	          bitmap = BitmapFactory.decodeStream(in);
	          
	        } catch (Exception e) {
	        	
	            Logger.error(e);
	        }

		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		
		Integer t = (Integer) imv.getTag();
		if (t != tag) {
			/*
			 * The path is not same. This means that this image view is
			 * handled by some other async task. We don't do anything and
			 * return.
			 */
			return;
		}

		if (imv != null) {
			imv.setImage(result);
		}
	}
}
