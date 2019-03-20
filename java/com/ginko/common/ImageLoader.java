package com.ginko.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ginko.cache.ImageFileCache;
import com.ginko.cache.ImageMemoryCache;
import com.ginko.ginko.MyApp;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoader {
	private static ImageMemoryCache memoryCache;  
    private static ImageFileCache fileCache; 

    static {
    	 memoryCache=new ImageMemoryCache(MyApp.getInstance().getApplicationContext());  
         fileCache=new ImageFileCache();  
    }
	  
	public static Bitmap getBitmapFromCache(String url) {
		Bitmap result = memoryCache.getBitmapFromCache(url);
		if (result != null) {
			return result;
		}
		result = fileCache.getImage(url);
		if (result != null) {
			memoryCache.addBitmapToCache(url, result);
		}
		return result;
	}
	
	public static interface ImageDisplay<T>{
		void show(T imageView, Bitmap bitmap);
	}

	public  static <T extends View> void setImage(final T imageView, final String imageUrl, final ImageDisplay<T> display) {
		if (StringUtils.isBlank(imageUrl)) {
			return;
		}
		
		Bitmap bitmap = getBitmapFromCache(imageUrl);
		if (bitmap!=null){
			if (display!=null){
				display.show(imageView, bitmap);
			}else {
				((ImageView)imageView).setImageBitmap(bitmap);
			}
			return;
		}
		new AsyncTask<String, Void, Bitmap>() {

			protected void onPreExecute(){
				Logger.error( "start to download image");
			}
			
			protected void 	onCancelled(){
				Logger.error( "cancel to download image");
			}
			
			@Override
			protected Bitmap doInBackground(String... params) {
				
				Bitmap bits = getBitmapFromCache(imageUrl);;
				if (bits!=null){
					return bits;
				}
				Bitmap bitmap = getHttpBitmap(params[0]);
				
				return bitmap;
			}

			protected void onPostExecute(Bitmap bitmap) {
				if (bitmap != null) {
//					Bitmap clone = bitmap.copy(Config.ARGB_8888, true);
//					caches.put(imageUrl, bitmap);
					fileCache.saveBitmap(bitmap, imageUrl);
					memoryCache.addBitmapToCache(imageUrl, bitmap);
					if (display!=null){
						display.show(imageView, bitmap);
//						display.show(imageView, memoryCache.getBitmapFromCache(imageUrl));
					}else {
						((ImageView)imageView).setImageBitmap(bitmap);
					}
					
				}
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUrl);
	}
    private static final int MAX_IMAGE_SIZE = 300;
	public static Bitmap getHttpBitmap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			Logger.debug( url);
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			Logger.error(e);
		}
        InputStream is = null;
        OutputStream os = null;
        try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();
            File temFile = new File(RuntimeContext.getTempFolder(), "" + System.currentTimeMillis());
            os = new FileOutputStream(temFile);

            IOUtils.copy(is,os);
            bitmap = decodeThumbBitmapForFile(temFile.getAbsolutePath(),MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
//			bitmap = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			Logger.error(e);
		}finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		return bitmap;
	}


    public static Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);

        options.inJustDecodeBounds = false;
        Logger.debug("change image size:" +  options.inSampleSize);
        return BitmapFactory.decodeFile(path, options);
    }


    /**
     * @param options
     * @param viewWidth
     * @param viewHeight
     */
    public static int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight){
        int inSampleSize = 1;
        if(viewWidth == 0 || viewWidth == 0){
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        if(bitmapWidth > viewWidth || bitmapHeight > viewWidth){
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }
}
