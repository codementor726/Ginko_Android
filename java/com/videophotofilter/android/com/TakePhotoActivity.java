package com.videophotofilter.android.com;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.common.Logger;
import com.ginko.customview.ProgressHUD;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.videophotofilter.library.android.com.AspectFrameLayout;
import com.videophotofilter.library.android.com.CameraHelper;
import com.videophotofilter.library.android.com.CameraHelper.CameraInfo2;
import com.videophotofilter.library.android.com.CameraUtils;
import com.videophotofilter.library.android.com.ImageUtil;
import com.videophotofilter.library.android.com.SquarePhotoImageView;

import java.io.File;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;

public class TakePhotoActivity extends Activity implements OnClickListener{

    private TextView txtTitle;

	private ImageButton btnGoPrev , btnApply;
	private ImageView btnDelete;
	
	private ImageView imgButtonGridOnOff , imgButtonCameraFrontOnOff , imgButtonFlashOnOff;
	private ImageButton imgButtonTakePicture;

	private RelativeLayout toolbarLayout , captureButtonLayout;
	private AspectFrameLayout aspectFrameLayout;
	private ImageView gridImageView;
	private SquarePhotoImageView takenPhotoImageView;
	
	private GLSurfaceView cameraSurfaceView;
	
	//variables
	private boolean isGridOn = false , isCameraFrontOn = false , isFlashOn = false;
	private boolean isTakingPicture = false;
	
	private static final int COLOR_DARK = 0xCC000000;
	private static final int COLOR_LIGHT = 0xCCBFBFBF;
	private static final int COLOR_WHITE = 0xFFFFFFFF;
	
	private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;

    private ProgressHUD progress;

    File pictureFile;
   
    Bitmap capturedBitmap = null;

    private boolean isCameraAvailable = false;
    private boolean isFlashAvailable = false;
    private int availableCameraCount = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_takephoto);

        //check camera is available or not
        PackageManager pm = getApplicationContext().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            this.isCameraAvailable = true;
        }

        if(!this.isCameraAvailable)
        {
            finish();
            return;
        }

        this.isFlashAvailable = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        CameraInfo ci = new CameraInfo();
        this.availableCameraCount = Camera.getNumberOfCameras();

        getUIObjects();


		mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(cameraSurfaceView);

        mCameraHelper = new CameraHelper(this);
        mCamera = new CameraLoader(this);
		
	}
	@Override
    protected void onResume() {
        super.onResume();
        isFlashOn = false;
        mCamera.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }
	private void getUIObjects()
	{
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        if(MyApp.currentTakePhotoTitle != null)
        {
            txtTitle.setText(MyApp.currentTakePhotoTitle);
        }
        else
        {
            txtTitle.setText(getResources().getString(R.string.home_info));
        }
		//progress dialog
		cameraSurfaceView = (GLSurfaceView)findViewById(R.id.surfaceView);

        toolbarLayout = (RelativeLayout)findViewById(R.id.toolbarLayout);
        captureButtonLayout = (RelativeLayout)findViewById(R.id.captureButtonLayout);

		btnGoPrev = (ImageButton)findViewById(R.id.btnPrev); btnGoPrev.setOnClickListener(this);
		btnApply = (ImageButton)findViewById(R.id.btnApply); btnApply.setOnClickListener(this);
		btnDelete = (ImageView)findViewById(R.id.btnDelete); btnDelete.setOnClickListener(this);
		imgButtonGridOnOff = (ImageView)findViewById(R.id.imgButtonGirdOnOff);imgButtonGridOnOff.setOnClickListener(this);
		imgButtonCameraFrontOnOff = (ImageView)findViewById(R.id.imgBtnCameraFrontOnOff);
        if(availableCameraCount>1)
            imgButtonCameraFrontOnOff.setOnClickListener(this);
		imgButtonFlashOnOff = (ImageView)findViewById(R.id.imgBtnFlashLightOnOff);
        if(isFlashAvailable)
            imgButtonFlashOnOff.setOnClickListener(this);

		aspectFrameLayout = (AspectFrameLayout)findViewById(R.id.cameraPreview_afl);
		aspectFrameLayout.setAspectRatio(1.0d);
		
		//imageview to show the taken photo
		takenPhotoImageView = (SquarePhotoImageView)findViewById(R.id.imgTakenPhotoView);
		
		//add grid view
		gridImageView = (ImageView)findViewById(R.id.imgGridView);
		/*gridImageView = new ImageView(this);
		gridImageView.setScaleType(ImageView.ScaleType.FIT_XY);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT ,FrameLayout.LayoutParams.MATCH_PARENT );
		gridImageView.setLayoutParams(params);
		gridImageView.setImageResource(R.drawable.grid_background);
		aspectFrameLayout.addView(gridImageView);*/
		gridImageView.setVisibility(View.INVISIBLE);
		
		//Take photo
		imgButtonTakePicture = (ImageButton)findViewById(R.id.imgButtonTakePhoto);imgButtonTakePicture.setOnClickListener(this);
		
		
		refreshButtons();
	}
	
	private void refreshButtons()
	{
		if(isGridOn)
			imgButtonGridOnOff.setImageResource(R.drawable.video_grid_on);
		else
			imgButtonGridOnOff.setImageResource(R.drawable.video_grid_off);
		
		if(isCameraFrontOn)
			imgButtonCameraFrontOnOff.setImageResource(R.drawable.video_camera_front_on);
		else
			imgButtonCameraFrontOnOff.setImageResource(R.drawable.video_camera_front_off);
		
		if(isFlashOn)
			imgButtonFlashOnOff.setImageResource(R.drawable.video_light_on);
		else
			imgButtonFlashOnOff.setImageResource(R.drawable.video_light_off);
		
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btnPrev:
			finish();
			break;
			
		//apply taken photo to filter screen
		case R.id.btnApply:
			/*Intent filterIntent = new Intent(TakePhotoActivity.this , PhotoFilterActivity.class);
			filterIntent.putExtra("isResource", false);
			filterIntent.putExtra("path_or_name", pictureFile.getAbsolutePath());
			startActivity(filterIntent);*/
            Intent intent = new Intent();
            intent.putExtra("photoPath" , pictureFile.getAbsolutePath());
            setResult(RESULT_OK , intent);
            finish();
			break;
		//delete
		case R.id.btnDelete:
			if(pictureFile!=null)
			{
				try{
					pictureFile.delete();
				}catch(Exception e){e.printStackTrace();}
			}
            mCamera.mCameraInstance.startPreview();
			cameraSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            isTakingPicture = false;
            if(progress != null)
                progress.dismiss();
        	takenPhotoImageView.setImageBitmap(null);
        	if(capturedBitmap!=null)
        	{
        		try
        		{
        			capturedBitmap.recycle();
        		}catch(Exception e){e.printStackTrace();}
        		finally{
        			capturedBitmap = null;
        		}
        	}
            btnGoPrev.setVisibility(View.VISIBLE);
            btnApply.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.GONE);
            toolbarLayout.setVisibility(View.VISIBLE);
            captureButtonLayout.setVisibility(View.VISIBLE);
			break;
		//grid turn on/off
		case R.id.imgButtonGirdOnOff:
			isGridOn = !isGridOn;
			refreshButtons();
			if(isGridOn) //show grid
			{
				gridImageView.setVisibility(View.VISIBLE);
			}
			else//hide grid
			{
				gridImageView.setVisibility(View.INVISIBLE);
			}
			break;
		//camera front on/off
		case R.id.imgBtnCameraFrontOnOff:
			if(mCamera.switchCamera())
			{
				isCameraFrontOn = !isCameraFrontOn;
				refreshButtons();
			}
			
			break;
		//flash light on/off
		case R.id.imgBtnFlashLightOnOff:
            if(!isFlashAvailable) return;
			if(isCameraFrontOn) return;
			if(isFlashOn)
			{
				if(mCamera.turnFlashLightOff())
				{
					isFlashOn = !isFlashOn;
					refreshButtons();
				}
			}
			else
			{
				if(mCamera.turnFlashLightOn())
				{
					isFlashOn = !isFlashOn;
					refreshButtons();
				}
			}
			break;
			
		//take photo
		case R.id.imgButtonTakePhoto:
            if(btnDelete.getVisibility() == View.VISIBLE)//if delete button is shown , then it means that already has captured photo
                return;
			takePicture();
			break;
		}
	}

	private void takePicture() {
        // TODO get a size that is about the size of the screen
		if(isTakingPicture == true) return;
		isTakingPicture = true;

        if(progress == null)
        {
            progress = ProgressHUD.show(this , "" , true , true , null);
        }
        else
		    progress.show();
		final Camera.Parameters params = mCamera.mCameraInstance.getParameters();
		int rotation = 90;
		if(mCamera.mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
		{
			params.setRotation(90);
			params.set("rotation", 90);
		}
		else
		{
			params.setRotation(90);
			params.set("rotation", 90);
	    }
        mCamera.mCameraInstance.setParameters(params);
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            Log.i("ASDF", "Supported: " + size.width + "x" + size.height);
        }
        final int captureWidth = params.getPictureSize().width;
        final int captureHeight = params.getPictureSize().height;

        mCamera.mCameraInstance.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {

                        pictureFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                            Log.d("ASDF",
                                    "Error creating media file, check storage permissions");
                            return;
                        }
                                                
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        capturedBitmap = ImageUtil.rotateCropSquareImage(bitmap , params.getInt("rotation"));
                        bitmap.recycle();
                        // mGPUImage.setImage(bitmap);
                        final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceView);
                        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        System.out.println("----Photo Path="+pictureFile.getAbsolutePath()+"------");
                        
                        mGPUImage.saveToPictures(capturedBitmap, MyApp.APP_NAME,
                                pictureFile.getName(),
                                new OnPictureSavedListener() {

                                    @Override
                                    public void onPictureSaved(final Uri
                                            uri) {
                                        //pictureFile.delete();
                                        //camera.startPreview();
                                        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                                        isTakingPicture = false;
                                        if(progress != null)
                                            progress.dismiss();
                                        System.out.println("----Photo Path="+uri.getPath()+"------");
                                        takenPhotoImageView.setImageBitmap(ImageUtil.decodeSampledBitmapFromImageFile(pictureFile.getAbsolutePath() , takenPhotoImageView.getViewWidth() , takenPhotoImageView.getViewHeight()));
                                        btnApply.setVisibility(View.VISIBLE);
                                        btnDelete.setVisibility(View.VISIBLE);
                                        btnGoPrev.setVisibility(View.GONE);
                                        toolbarLayout.setVisibility(View.INVISIBLE);
                                        captureButtonLayout.setVisibility(View.INVISIBLE);
                                    }
                                });
                        
                    }
                });
        
    }
	
	
	
	private class CameraLoader {

        private int mCurrentCameraId = 0;
        private Camera mCameraInstance;
        
        private boolean isCameraOpen = false;
        private Context mContext;
        public CameraLoader(Context context)
        {
        	this.mContext = context;
        }
        private Point getScreenSizeInPixel()
        {
        	Point size = new Point();
        	WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        	Display display = wm.getDefaultDisplay();
        	DisplayMetrics metrics = new DisplayMetrics();
        	getWindowManager().getDefaultDisplay().getMetrics(metrics);
        	size.x = metrics.widthPixels;
        	size.y = metrics.heightPixels;
        	
        	/*if(android.os.Build.VERSION.SDK_INT>=13)
        	{
        		display.getSize(size);//size.x = width , size.y = height
        	}
        	else
        	{
        		size.x = display.getWidth();  // deprecated
        		size.y = display.getHeight();  // deprecated	
        	}*/
        	return size;
        }
        public void onResume() {
        	btnApply.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.GONE);
            btnGoPrev.setVisibility(View.VISIBLE);
        	isTakingPicture = false;
        	takenPhotoImageView.setImageBitmap(null);
        	if(capturedBitmap!=null)
        	{
        		try
        		{
        			capturedBitmap.recycle();
        		}catch(Exception e){e.printStackTrace();}
        		finally{
        			capturedBitmap = null;
        		}
        	}
        	
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public boolean switchCamera() {
        	if(isCameraOpen == false) return false;
            try {
                if (mCameraInstance != null)
                    mCameraInstance.stopPreview();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
            if(mCameraInstance == null)
            	return false;
            return true;
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            if(mCameraInstance == null) return;
            Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            
            //set smallest picture size of camera and bigger than screen width
            List<Camera.Size> supportedPictureSize = CameraUtils.getSupportedPictureSizes(mCameraInstance);

            Point screenSize = getScreenSizeInPixel();
            int minPictureWidth = 99999 , minPictureHeight = 99999;
            for(int i=0;i<supportedPictureSize.size();i++)
            {
            	if(supportedPictureSize.get(i).height<screenSize.x)//ignore the picture size smaller than the screen width
            		continue;
            	if(minPictureHeight>supportedPictureSize.get(i).height)
            	{
            		minPictureHeight = supportedPictureSize.get(i).height;
            		minPictureWidth = supportedPictureSize.get(i).width;
                    break;
            	}
            }
            parameters.setPictureSize(minPictureWidth , minPictureHeight);

        	mCameraInstance.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    TakePhotoActivity.this, mCurrentCameraId);
            CameraInfo2 cameraInfo = new CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /** A safe way to get an instance of the Camera object. */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
                isCameraOpen = true;
            } catch (Exception e) {
                e.printStackTrace();
                isCameraOpen = false;
            }
            
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
            isCameraOpen = false;
        }
        
        //turn on the flash light
        public boolean turnFlashLightOn()
        {
        	if(isCameraOpen == false) return false; 
	        if( mCameraInstance != null ){
		        /*Parameters params = mCameraInstance.getParameters();
		        params.setFlashMode( Parameters.FLASH_MODE_TORCH );
		        mCameraInstance.setParameters( params );*/
                Parameters params = mCameraInstance.getParameters();
                List<String> pList = mCameraInstance.getParameters().getSupportedFlashModes();

                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    /*if (pList.contains(Parameters.FLASH_MODE_TORCH) && (!ManufacturerName.contains("htc"))) {
                        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    }
                    else*/
                    if (pList.contains(Parameters.FLASH_MODE_ON)) {
                        params.setFlashMode(Parameters.FLASH_MODE_ON);
                    }
                    else
                    {
                        params.setFlashMode( Parameters.FLASH_MODE_TORCH );
                    }
                }
                mCameraInstance.setParameters( params );
	        }
	        return true;
        }
        //turn off the flash light
        public boolean turnFlashLightOff()
        {
        	if(isCameraOpen == false) return false; 

	        if( mCameraInstance != null ){
		        Parameters params = mCameraInstance.getParameters();
		        params.setFlashMode( Parameters.FLASH_MODE_OFF );
		        mCameraInstance.setParameters( params );
		        //mCameraInstance.release();
		        //mCameraInstance = null;
		
	        }
	        return true;
        }
        //auto fouc on/off
        public boolean setAutoFocus(boolean isAutoFocusable)
        {
        	Parameters params = mCameraInstance.getParameters();
        	if(isAutoFocusable) //set auto focus on
        	{
        		if (params.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            		params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
        		else
        			return false;
        	}
        	else //set auto foucs off
        	{
        		if (params.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_INFINITY)) {
            		params.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
                }
        		else
        			return false;
        	}
        	return true;
        }
    }
}
