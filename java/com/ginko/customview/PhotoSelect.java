package com.ginko.customview;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ginko.common.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PhotoSelect {
	
	private String 				userPhotoPath = null;
	private Context 			contextActivity = null;
	
	
	public PhotoSelect(Context context) {
		
		contextActivity = context;
	}
	
	public String onPhotoCaptured ( Intent intent )	{
		
		if (intent != null)	{
			
			Uri data = intent.getData ();
			if (data != null) {
				
				Cursor imageCursor = contextActivity.getContentResolver().query(intent.getData(), null, null, null, null );
				if (imageCursor != null && imageCursor.moveToFirst ()) {
					
					int dataIdx = imageCursor.getColumnIndex ( MediaStore.Images.Media.DATA );

					userPhotoPath = imageCursor.getString ( dataIdx );

					setImageOrientationFromPath ( userPhotoPath );

					return userPhotoPath;
				}
				
			} else if (intent.getAction ().equalsIgnoreCase ( "inline-data" )) {
				
				Bitmap photoBitmap = intent.getParcelableExtra ( "data" );

				if (photoBitmap != null) {
					
					try	{
						
						userPhotoPath = getOutputMediaFile ();
						
						if (userPhotoPath == null)
							return null;

						String identity = android.os.Build.MANUFACTURER;
						if (identity.equalsIgnoreCase ( "samsung" )) {
							
							String inpath = getInputMediaFile ();
							if (inpath != null)
								setImageOrientationFromPath ( inpath );
							else {
								
								FileOutputStream out = new FileOutputStream ( userPhotoPath );
								photoBitmap.compress ( CompressFormat.JPEG, 100, out );
								out.flush ();
								out.close ();
							}

						} else {
							
							FileOutputStream out = new FileOutputStream ( userPhotoPath );
							photoBitmap.compress ( CompressFormat.JPEG, 100, out );
							out.flush ();
							out.close ();
						}

						return userPhotoPath;
						
					} catch (FileNotFoundException e)	{
						
					} catch (IOException e) {
						
					}
				}
			}
		}
		
		return null;
	}

	public String onPhotoPicked ( Intent intent ) {
		
		if (intent != null) {
			
			if (intent.getData() == null)
				return null;
			
			Cursor cursor = contextActivity.getContentResolver().query(intent.getData(), null, null, null, null );
			
			if (cursor != null)	{
				
				cursor.moveToFirst ();
				
				int dataIdx = cursor.getColumnIndex ( MediaStore.Images.Media.DATA );
				userPhotoPath = cursor.getString ( dataIdx );

				setImageOrientationFromPath ( userPhotoPath );

				return userPhotoPath;
			}
		}
		
		return null;
	}
	
	private void setImageOrientationFromPath ( String path ) {
		
		ExifInterface exif;
		
		try	{
			
			exif = new ExifInterface ( path );
			int exifOrientation = exif.getAttributeInt ( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );
			
			int rotate = 0;
			switch (exifOrientation) {
			
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;

			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;

			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			if (rotate != 0) {
				
				BitmapFactory.Options opts = new BitmapFactory.Options ();
				opts.inSampleSize = 4;
				Bitmap image = BitmapFactory.decodeFile ( path, opts );

				int w = image.getWidth ();
				int h = image.getHeight ();

				// Setting pre rotate
				Matrix mtx = new Matrix ();
				mtx.preRotate ( rotate );

				// Rotating Bitmap & convert to ARGB_8888, required by tess
				image = Bitmap.createBitmap ( image, 0, 0, w, h, mtx, false );

				// image = image.copy(Bitmap.Config.ARGB_8888, true);

				FileOutputStream fOut;

				try {
					
					fOut = new FileOutputStream ( userPhotoPath );
					image.compress ( Bitmap.CompressFormat.JPEG, 90, fOut );
					fOut.flush ();
					fOut.close ();

				} catch (FileNotFoundException e1) {
					
					// TODO Auto-generated catch block
					e1.printStackTrace ();
				} catch (IOException e) {
					
					// TODO Auto-generated catch block
					e.printStackTrace ();
				}
			}
		} catch (IOException e) {
			
		}

	}

	public static String getOutputMediaFile ()	{
		
		File mediaStorageDir = new File ( Environment.getExternalStoragePublicDirectory ( Environment.DIRECTORY_PICTURES ), "TipHive" );
		
		if (!mediaStorageDir.exists ()) {
			
			if (!mediaStorageDir.mkdirs ())	{
				
				Logger.debug("failed to create directory");
				return null;
			}
		}

		String timeStamp = new SimpleDateFormat ( "yyyyMMdd_HHmmss", Locale.US ).format ( new Date () );
		String path = mediaStorageDir.getPath () + File.separator + "IMG_" + timeStamp + ".jpg";

		return path;
	}

	private String getInputMediaFile () {

		Uri mImageCaptureUri_samsung = null;

		// Final Code As Below
		try	{
			Logger.info( "inside Samsung Phones" );
			String[] projection = { MediaStore.Images.Thumbnails._ID, // The
																		// columns
																		// we
																		// want
					MediaStore.Images.Thumbnails.IMAGE_ID, MediaStore.Images.Thumbnails.KIND, MediaStore.Images.Thumbnails.DATA };

			String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select
																			// only
																			// mini's
					MediaStore.Images.Thumbnails.MINI_KIND;

			String sort = MediaStore.Images.Thumbnails._ID + " DESC";

			Cursor myCursor = contextActivity.getContentResolver().query( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort );

			long imageId = 0l;
			long thumbnailImageId = 0l;
			String thumbnailPath = "";

			try	{

				myCursor.moveToFirst ();
				imageId = myCursor.getLong ( myCursor.getColumnIndexOrThrow ( MediaStore.Images.Thumbnails.IMAGE_ID ) );
				thumbnailImageId = myCursor.getLong ( myCursor.getColumnIndexOrThrow ( MediaStore.Images.Thumbnails._ID ) );
				thumbnailPath = myCursor.getString ( myCursor.getColumnIndexOrThrow ( MediaStore.Images.Thumbnails.DATA ) );

			} finally {

				// myCursor.close();
			}

			// Create new Cursor to obtain the file Path for the large image

			String[] largeFileProjection = { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA };

			String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";

//			myCursor = this.managedQuery ( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort );
			myCursor = contextActivity.getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort );

			String largeImagePath = "";

			try {

				myCursor.moveToFirst ();

				// This will actually give yo uthe file path location of the image.

				largeImagePath = myCursor.getString ( myCursor.getColumnIndexOrThrow ( MediaStore.Images.ImageColumns.DATA ) );
				mImageCaptureUri_samsung = Uri.fromFile ( new File ( largeImagePath ) );
				// mImageCaptureUri = null;
			}
			finally {

				// myCursor.close();
			}

			// These are the two URI's you'll be interested in. They give you a
			// handle to the actual images
			Uri uriLargeImage = Uri.withAppendedPath ( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf ( imageId ) );
			Uri uriThumbnailImage = Uri.withAppendedPath ( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf ( thumbnailImageId ) );

			// I've left out the remaining code, as all I do is assign the URI's
			// to my own objects anyways...
		} catch (Exception e) {

			mImageCaptureUri_samsung = null;
			Logger.info( "inside catch Samsung Phones exception " + e.toString () );
		}

		try {

			Logger.info( "URI Samsung:" + mImageCaptureUri_samsung.getPath () );

		} catch (Exception e)	{

			Logger.info( "Excfeption inside Samsung URI :" + e.toString () );
		}

		return mImageCaptureUri_samsung.getPath ();
	}
}
