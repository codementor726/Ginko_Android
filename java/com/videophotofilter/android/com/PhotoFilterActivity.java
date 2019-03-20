package com.videophotofilter.android.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.common.RuntimeContext;
import com.ginko.context.ConstValues;
import com.ginko.customview.DragableImage;
import com.ginko.customview.ProgressHUD;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.videophotofilter.library.android.com.AspectFrameLayout;
import com.videophotofilter.library.android.com.Filters;
import com.videophotofilter.library.android.com.ImageUtil;
import com.videophotofilter.library.android.com.SquarePhotoImageView.OnViewDrawListener;
import com.videophotofilter.library.android.com.VerticalSeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import customviews.library.widget.AdapterView;
import customviews.library.widget.AdapterView.OnItemClickListener;
import customviews.library.widget.HListView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;

public class PhotoFilterActivity extends Activity 
									implements OnClickListener ,
											   OnItemClickListener , 
       										   OnViewDrawListener{
	
	private ImageButton btnPrev;
	private TextView btnDone;
    private TextView txtTitle;
	private HListView filterListView;
	private ImageView imgButtonPhotoLayer , imgBtnHalfLightness , imgBtnLightness , imgBtnCrop;
	
	private AspectFrameLayout aspectFrameLayout;
	//private SquarePhotoImageView backgroundPhotoView;
    private DragableImage backgroundPhotoView , foregroundPhotoView;

	private ProgressHUD progressDialog;


	private VerticalSeekBar lightnessSeekBar;

	//variables
	private FilterAdapter filterAdapter;

	private boolean isSavingPicture = false;
	private boolean isColorResource = false;
	private String photoPath = "";
	private int 	archiveId = 0;//default is 0 , this means this is not archive photo

	private GPUImage mBackgroundGPUImage , mForegroundGPUImage;
	private GPUImageBrightnessFilter brightnessFilter;
	private GPUImageToneCurveFilter tone_curve_Filters[];
    private GPUImageGrayscaleFilter bwFilter;
	private GPUImageFilter currentBackgroundFilter = null ;
    private GPUImageFilter currentForegroundFilter = null;


	private Bitmap bitmapBackground;
    private Bitmap bitmapForeground;

    private boolean isForegroundVisible = false;

    private String strBackgroundPhotoPath = "";
    private String strForegroundPhotoPath = "";


    private String strTmpPhotoFilePath = "";
    private String strTmpCropPhotoFilePath = "";

	int imageBackgroundViewWidth = 0 , imageBackgroundViewHeight = 0;
	int imageForegroundViewWidth = 0 , imageForegroundViewHeight =0;

    private final int REQUEST_TAKE_PHOTO = 1;
	private final int REQUEST_PICK_IMAGE = 2;
    private final int PHOTO_REQUEST_CUT = 3;

	private Object lockObj = new Object();

    private boolean isFromCamera = false;
    private int nTradeCardType = ConstValues.HOME_PHOTO_EDITOR;

    private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photofilter);

		Intent intent = this.getIntent();
		isColorResource = intent.getBooleanExtra("isResource", true);
		photoPath = intent.getStringExtra("path_or_name");
		archiveId = intent.getIntExtra("archiveID", 0);

        this.nTradeCardType = intent.getIntExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);

        this.isFromCamera = intent.getBooleanExtra("isFromCamera" , false);

        strBackgroundPhotoPath = photoPath;

		getUIObjects();

        mBackgroundGPUImage = new GPUImage(this);
        mForegroundGPUImage = new GPUImage(this);

		brightnessFilter = new GPUImageBrightnessFilter();
		loadFilterACVFiles();
	}

	private int getDrawableIdFromName(String name)
    {
    	Resources resources = getResources();
    	final int resourceId = resources.getIdentifier(name, "drawable",
    			getPackageName());
    	return resourceId;
    }

    private void updateImageLayerIcon()
    {
        if(strForegroundPhotoPath.equalsIgnoreCase(""))
        {
            imgButtonPhotoLayer.setImageResource(R.drawable.btn_layer);
            foregroundPhotoView.setVisibility(View.INVISIBLE);
            isForegroundVisible = false;
        }
        else
        {
            if(isForegroundVisible)
            {
                foregroundPhotoView.setVisibility(View.VISIBLE);
                imgButtonPhotoLayer.setImageResource(R.drawable.btn_layer_order);
                System.out.println("---Foreground Photo Available----");
            }
            else
            {
                foregroundPhotoView.setVisibility(View.INVISIBLE);
                imgButtonPhotoLayer.setImageResource(R.drawable.btn_layer);
                System.out.println("---Foreground Photo Unavailable----");
            }
        }
    }


	private void getUIObjects()
	{
		aspectFrameLayout = (AspectFrameLayout)findViewById(R.id.cameraPreview_afl);
		aspectFrameLayout.setAspectRatio(1.0d);
		
		//imageview to show the taken photo
        //backgroundPhotoView = (SquarePhotoImageView)findViewById(R.id.imgBackgroundPhotoView);
        backgroundPhotoView = (DragableImage)findViewById(R.id.imgBackgroundPhotoView);
        backgroundPhotoView.setViewListener(this);

        foregroundPhotoView = (DragableImage)findViewById(R.id.imgForegroundPhotoView);
        foregroundPhotoView.setViewListener(this);

		btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
		btnDone = (TextView)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);
        imgButtonPhotoLayer = (ImageView)findViewById(R.id.imgButtonPhotoLayer);imgButtonPhotoLayer.setOnClickListener(this);
		imgBtnHalfLightness = (ImageView)findViewById(R.id.imgButtonHalfLightness);imgBtnHalfLightness.setOnClickListener(this);
		imgBtnLightness = (ImageView)findViewById(R.id.imgButtonLightness);imgBtnLightness.setOnClickListener(this);
		imgBtnCrop = (ImageView)findViewById(R.id.imgButtonPhotoCrop);imgBtnCrop.setOnClickListener(this);

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
        {
            txtTitle.setText(getResources().getString(R.string.home_info));
        }
        else if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
        {
            txtTitle.setText(getResources().getString(R.string.work_info));
        }
        else if(nTradeCardType == ConstValues.ENTITY_PHOTO_EDITOR)
        {
            txtTitle.setText(getResources().getString(R.string.entity_info));
        }

		filterListView = (HListView)findViewById(R.id.filterlistview);
		filterAdapter = new FilterAdapter(this); filterAdapter.setSelection(0);
		filterListView.setAdapter(filterAdapter);
		filterListView.setOnItemClickListener(this);
		
		lightnessSeekBar = (VerticalSeekBar)findViewById(R.id.lightnessSeekBar);
		lightnessSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				synchronized(lockObj)
				{
                    if(isForegroundVisible)
                    {
                        if (currentForegroundFilter != null && currentForegroundFilter != brightnessFilter) {
                            mForegroundGPUImage.setImage(mForegroundGPUImage.getBitmapWithFilterApplied());
                            currentForegroundFilter = brightnessFilter;
                        }
                        brightnessFilter.setBrightness(range(progress, -1.0f, 1.0f));
                        mForegroundGPUImage.setFilter(brightnessFilter);
                        foregroundPhotoView.setAdjustImageRespectRate(mForegroundGPUImage.getBitmapWithFilterApplied() , imageForegroundViewWidth , imageForegroundViewHeight);
                    }
                    else {
                        if (currentBackgroundFilter != null && currentBackgroundFilter != brightnessFilter) {
                            mBackgroundGPUImage.setImage(mBackgroundGPUImage.getBitmapWithFilterApplied());
                            currentBackgroundFilter = brightnessFilter;
                        }
                        brightnessFilter.setBrightness(range(progress, -1.0f, 1.0f));
                        mBackgroundGPUImage.setFilter(brightnessFilter);
                        backgroundPhotoView.setImageBitmap(mBackgroundGPUImage.getBitmapWithFilterApplied());
                    }
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}}
		);
		lightnessSeekBar.setVisibility(View.INVISIBLE);

        updateImageLayerIcon();

	}
	private float range(final int percentage, final float start, final float end) {
        return (end - start) * percentage / 100.0f + start;
    }
	private void loadFilterACVFiles()
	{
		//open filter .acv file
		AssetManager as = getAssets();
		tone_curve_Filters = new GPUImageToneCurveFilter[Filters.filterNames.length];

        bwFilter = new GPUImageGrayscaleFilter();
        /*InputStream is = null;
        try {
            is = as.open(Filters.filterNames[Filters.filterNames.length-1] + ".acv");

            //bwFilter.setFromCurveFileInputStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("MainActivity", "Error");
        }*/
		for(int i=0;i<Filters.filterNames.length;i++)
		{
            tone_curve_Filters[i] = new GPUImageToneCurveFilter();
            InputStream is = null;
            try {
                is = as.open(Filters.filterNames[i] + ".acv");

                tone_curve_Filters[i].setFromCurveFileInputStream(is);
                is.close();
            } catch (IOException e) {
                Log.e("MainActivity", "Error");
            }

		}
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

        updateImageLayerIcon();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btnPrev:
            //this intent will notify that the filtered photo was from camera or gallery , if photo was from camera , then this takes the screen to the camera screen
            Intent intent = new Intent();
            intent.putExtra("isFromCamera" , isFromCamera);

            this.setResult(RESULT_CANCELED , intent);
			finish();
			break;
		case R.id.btnDone:
			if(isSavingPicture) return;
			isSavingPicture = true;
			final File backgroundPhotoFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE , "filteredBackgroundPhoto.jpg");
			if(backgroundPhotoFile == null)
			{
				isSavingPicture = false;
				return;
			}
            strBackgroundPhotoPath = backgroundPhotoFile.getAbsolutePath();

            final File foregroundPhotoFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE , "filteredForegroundPhoto.jpg");

            if(isForegroundVisible)
            {
                strForegroundPhotoPath = foregroundPhotoFile.getAbsolutePath();
            }
            else
            {
                strForegroundPhotoPath = "";
            }

            progressDialog = ProgressHUD.show(PhotoFilterActivity.this, "Saving...", true, false, null);


            final int backgroundLayoutWidth = aspectFrameLayout.getWidth();
            final int backgroundLayoutHeight = aspectFrameLayout.getHeight();
            float ratio = (float)640/backgroundLayoutWidth;

            final int bgPhotoLeft = backgroundPhotoView.getLeft();
            final int bgPhotoTop = backgroundPhotoView.getTop();

            final int bgPhotoRight = backgroundPhotoView.getRight();
            final int bgPhotoBottom = backgroundPhotoView.getBottom();

            final int bgPhotoWidth = backgroundPhotoView.getWidth();
            final int bgPhotoHeight = backgroundPhotoView.getHeight();

            int orgDrawLeft = 0 , orgDrawRight = 0 , orgDrawTop = 0 , orgDrawBottom = 0;
            int targetDrawLeft = 0 , targetDrawRight = 0 , targetDrawTop =0 , targetDrawBottom = 0;

            if(bgPhotoLeft>=backgroundLayoutHeight || bgPhotoTop >=backgroundLayoutHeight || bgPhotoRight<=0 || bgPhotoBottom<=0)
            {
                targetDrawLeft = 0; targetDrawTop = 0; targetDrawRight = 0; targetDrawBottom = 0;
            }
            else
            {
                if(bgPhotoLeft < 0)
                    orgDrawLeft = bgPhotoLeft*(-1);
                else
                    orgDrawLeft = 0;
                if(bgPhotoTop < 0)
                    orgDrawTop = bgPhotoTop*(-1);
                else
                    orgDrawTop = 0;

                if(bgPhotoRight > backgroundLayoutWidth)
                    orgDrawRight = backgroundLayoutWidth - bgPhotoLeft;
                else
                    orgDrawRight = bgPhotoRight - bgPhotoLeft;
                if(bgPhotoBottom > backgroundLayoutHeight)
                    orgDrawBottom = backgroundLayoutHeight - bgPhotoTop;
                else
                    orgDrawBottom = bgPhotoBottom - bgPhotoTop;


                targetDrawLeft = (int) (Math.max(bgPhotoLeft, 0) * ratio);
                targetDrawRight = (int) (Math.min(bgPhotoRight, backgroundLayoutWidth) * ratio);
                targetDrawTop = (int) (Math.max(bgPhotoTop, 0) * ratio);
                targetDrawBottom = (int) (Math.min(bgPhotoBottom, backgroundLayoutHeight) * ratio);
            }

            final float orgBitmapDrawLeft = (float)orgDrawLeft/bgPhotoWidth;
            final float orgBitmapDrawRight = (float)orgDrawRight/bgPhotoWidth;
            final float orgBitmapDrawTop = (float)orgDrawTop/bgPhotoHeight;
            final float orgBitmapDrawBottom = (float)orgDrawBottom/bgPhotoHeight;


            final Rect desRect = new Rect(targetDrawLeft , targetDrawTop , targetDrawRight , targetDrawBottom);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //save background photo to a file
                    Bitmap filteredBackgroundBitmap = mBackgroundGPUImage.getBitmapWithFilterApplied();

                    int bitmapWidth = filteredBackgroundBitmap.getWidth();
                    int bitmapHeight = filteredBackgroundBitmap.getHeight();

                    Rect srcRect = new Rect((int)(orgBitmapDrawLeft*bitmapWidth) ,
                                            (int)(orgBitmapDrawTop*bitmapHeight) ,
                                            (int)(orgBitmapDrawRight*bitmapWidth) ,
                                            (int)(orgBitmapDrawBottom*bitmapHeight));


                    int w = 640, h = 640;

                    Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                    Bitmap bitmapNewBackground = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
                    Canvas canvas = new Canvas(bitmapNewBackground);
                    Paint bgPaint = new Paint();
                    bgPaint.setStyle(Paint.Style.FILL);
                    bgPaint.setColor(Color.WHITE);
                    canvas.drawRect(new Rect(0, 0, 640, 640), bgPaint);
                    if(desRect.left != desRect.right && desRect.top != desRect.bottom)
                        canvas.drawBitmap(filteredBackgroundBitmap , srcRect , desRect , null);

                    SaveBitmapToFileCache(bitmapNewBackground , strBackgroundPhotoPath);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            isSavingPicture = false;
                            if(progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            if(archiveId>0 && photoPath.compareTo("")!=0)//if file was already exist
                            {
                                //delete original file and save new
                                File originalPhotoFile = new File(photoPath);
                                try{
                                    if(originalPhotoFile != null && originalPhotoFile.exists())
                                        originalPhotoFile.delete();
                                }catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            if(isForegroundVisible)
                            {
                                mForegroundGPUImage.saveToPictures(null, MyApp.APP_NAME,
                                        foregroundPhotoFile.getName(),
                                        new OnPictureSavedListener() {
                                            @Override
                                            public void onPictureSaved(final Uri uri) {
                                                if(progressDialog != null)
                                                    progressDialog.dismiss();
                                                isSavingPicture = false;

                                                Intent resultIntent = new Intent();
                                                resultIntent.putExtra("backgroundPhotoPath", strBackgroundPhotoPath);
                                                resultIntent.putExtra("foregroundPhotoPath", strForegroundPhotoPath);
                                                resultIntent.putExtra("foregroundLeft" , foregroundPhotoView.getLeft());
                                                resultIntent.putExtra("foregroundTop" , foregroundPhotoView.getTop());
                                                resultIntent.putExtra("foregroundWidth" , foregroundPhotoView.getWidth());
                                                resultIntent.putExtra("foregroundHeight" , foregroundPhotoView.getHeight());
                                                PhotoFilterActivity.this.setResult(RESULT_OK, resultIntent);
                                                PhotoFilterActivity.this.finish();
                                            }
                                        });
                            }
                            else {
                                if(progressDialog != null)
                                    progressDialog.dismiss();
                                isSavingPicture = false;
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("backgroundPhotoPath", strBackgroundPhotoPath);
                                resultIntent.putExtra("foregroundPhotoPath", strForegroundPhotoPath);
                                resultIntent.putExtra("foregroundLeft" , foregroundPhotoView.getLeft());
                                resultIntent.putExtra("foregroundTop" , foregroundPhotoView.getTop());
                                resultIntent.putExtra("foregroundWidth" , foregroundPhotoView.getWidth());
                                resultIntent.putExtra("foregroundHeight" , foregroundPhotoView.getHeight());

                                PhotoFilterActivity.this.setResult(RESULT_OK, resultIntent);
                                PhotoFilterActivity.this.finish();
                            }
                        }
                    });
                }
            }).start();

			/*mBackgroundGPUImage.saveToPictures(null, MyApp.APP_NAME,
                    backgroundPhotoFile.getName(),
                new OnPictureSavedListener() {
                    @Override
                    public void onPictureSaved(final Uri uri) {

                        if(archiveId>0 && photoPath.compareTo("")!=0)//if file was already exist
                        {
                            //delete original file and save new
                            File originalPhotoFile = new File(photoPath);
                            try{
                                if(originalPhotoFile != null && originalPhotoFile.exists())
                                    originalPhotoFile.delete();
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                        if(isForegroundVisible)
                        {
                            mForegroundGPUImage.saveToPictures(null, MyApp.APP_NAME,
                                    foregroundPhotoFile.getName(),
                                    new OnPictureSavedListener() {
                                        @Override
                                        public void onPictureSaved(final Uri uri) {
                                            if(progressDialog != null)
                                                progressDialog.dismiss();
                                            isSavingPicture = false;

                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("backgroundPhotoPath", strBackgroundPhotoPath);
                                            resultIntent.putExtra("foregroundPhotoPath", strForegroundPhotoPath);
                                            resultIntent.putExtra("foregroundLeft" , foregroundPhotoView.getLeft());
                                            resultIntent.putExtra("foregroundTop" , foregroundPhotoView.getTop());
                                            resultIntent.putExtra("foregroundWidth" , foregroundPhotoView.getWidth());
                                            resultIntent.putExtra("foregroundHeight" , foregroundPhotoView.getHeight());
                                            PhotoFilterActivity.this.setResult(RESULT_OK, resultIntent);
                                            PhotoFilterActivity.this.finish();
                                        }
                                    });
                        }
                        else {
                            if(progressDialog != null)
                                progressDialog.dismiss();
                            isSavingPicture = false;
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("backgroundPhotoPath", strBackgroundPhotoPath);
                            resultIntent.putExtra("foregroundPhotoPath", strForegroundPhotoPath);
                            resultIntent.putExtra("foregroundLeft" , foregroundPhotoView.getLeft());
                            resultIntent.putExtra("foregroundTop" , foregroundPhotoView.getTop());
                            resultIntent.putExtra("foregroundWidth" , foregroundPhotoView.getWidth());
                            resultIntent.putExtra("foregroundHeight" , foregroundPhotoView.getHeight());

                            PhotoFilterActivity.this.setResult(RESULT_OK, resultIntent);
                            PhotoFilterActivity.this.finish();
                        }
                    }
                });*/
			break;
			
		case R.id.imgButtonPhotoLayer:
            if(strForegroundPhotoPath.equals("")) {
                showDialog(this);
            }
            else
            {
                isForegroundVisible = !isForegroundVisible;
                updateImageLayerIcon();
            }

			break;
		case R.id.imgButtonHalfLightness:
			if(lightnessSeekBar.getVisibility() == View.INVISIBLE)
			{
				lightnessSeekBar.setProgress(50);
				lightnessSeekBar.setVisibility(View.VISIBLE);

			}
			else if(lightnessSeekBar.getProgress()!=50)
			{
				lightnessSeekBar.setProgress(50);
			}
			else
			{
				lightnessSeekBar.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.imgButtonLightness:
			if(lightnessSeekBar.getVisibility() == View.INVISIBLE)
			{
				lightnessSeekBar.setVisibility(View.VISIBLE);
			}
			else
			{
				lightnessSeekBar.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.imgButtonPhotoCrop:
            strTmpPhotoFilePath = RuntimeContext.getAppDataFolder("Temp")+"TempPhoto.jpg";
            final File tempPhotoFile = new File(strTmpPhotoFilePath);
            if(tempPhotoFile.exists())
                tempPhotoFile.delete();
            Bitmap currentBitmap = null;
            if(isForegroundVisible)
                currentBitmap = mForegroundGPUImage.getBitmapWithFilterApplied();
            else
                currentBitmap = mBackgroundGPUImage.getBitmapWithFilterApplied();

            progressDialog = ProgressHUD.show(PhotoFilterActivity.this , "Loading..." , true , false , null);

            if(isForegroundVisible)
            {
                mForegroundGPUImage.saveToPictures(currentBitmap, MyApp.APP_NAME,
                        strTmpPhotoFilePath,
                        new OnPictureSavedListener() {
                            @Override
                            public void onPictureSaved(final Uri uri) {
                                if(progressDialog != null)
                                    progressDialog.dismiss();
                                startPhotoZoom(Uri.fromFile(tempPhotoFile));
                            }
                        });
            }
            else {
                mBackgroundGPUImage.saveToPictures(currentBitmap, MyApp.APP_NAME,
                        strTmpPhotoFilePath,
                        new OnPictureSavedListener() {
                            @Override
                            public void onPictureSaved(final Uri uri) {
                                if(progressDialog != null)
                                    progressDialog.dismiss();
                                startPhotoZoom(Uri.fromFile(tempPhotoFile));
                            }
                        });
            }
			break;
		}
	}
    private Uri getCropPhotoUrl() {
        if(strTmpCropPhotoFilePath.equals(""))
            strTmpCropPhotoFilePath = RuntimeContext.getAppDataFolder("Temp")+"TempPhoto_crop.jpg";
        File tempPhotoCropFile = new File(strTmpCropPhotoFilePath);
        if(tempPhotoCropFile.exists())
            tempPhotoCropFile.delete();

        return Uri.fromFile(tempPhotoCropFile);
    }
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY
        // intent.putExtra("outputX", 500);
        // intent.putExtra("outputY", 500);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("scale", true);
        Uri outputUri = getCropPhotoUrl();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("output", outputUri);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    //Filter Item Click
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
        filterAdapter.setSelection(position);

        synchronized(lockObj)
		{
            if(isForegroundVisible)
            {
                if(bitmapForeground != null) {
                    if (position == 0)//origin
                    {
                        currentForegroundFilter = null;
                        mForegroundGPUImage.setImage(bitmapForeground);
                        mForegroundGPUImage.setFilter(tone_curve_Filters[position]);
                        foregroundPhotoView.setImageBitmap(bitmapForeground);
                        currentForegroundFilter = tone_curve_Filters[position];
                    } else if(position < tone_curve_Filters.length-1){
                        currentForegroundFilter = tone_curve_Filters[position];
                        //apply filter
                        mForegroundGPUImage.setFilter(tone_curve_Filters[position]);
                        foregroundPhotoView.setImageBitmap(mForegroundGPUImage.getBitmapWithFilterApplied());
                    }
                    else if(position == tone_curve_Filters.length-1)
                    {
                        currentForegroundFilter = bwFilter;
                        mForegroundGPUImage.setFilter(currentForegroundFilter);
                        foregroundPhotoView.setImageBitmap(mForegroundGPUImage.getBitmapWithFilterApplied());
                    }
                }
            }
            else
            {
                if(bitmapBackground != null) {
                    if (position == 0)//origin
                    {
                        currentBackgroundFilter = null;
                        mBackgroundGPUImage.setImage(bitmapBackground);
                        mBackgroundGPUImage.setFilter(tone_curve_Filters[position]);
                        backgroundPhotoView.setImageBitmap(bitmapBackground);
                        currentBackgroundFilter = tone_curve_Filters[position];
                    }  else if(position < tone_curve_Filters.length-1){
                        currentBackgroundFilter = tone_curve_Filters[position];
                        //apply filter
                        mBackgroundGPUImage.setFilter(tone_curve_Filters[position]);
                        backgroundPhotoView.setImageBitmap(mBackgroundGPUImage.getBitmapWithFilterApplied());
                    }
                    else if(position == tone_curve_Filters.length-1 ) //b&W filter
                    {
                        currentBackgroundFilter = bwFilter;
                        //apply filter
                        mBackgroundGPUImage.setFilter(currentBackgroundFilter);
                        backgroundPhotoView.setImageBitmap(mBackgroundGPUImage.getBitmapWithFilterApplied());
                    }
                }
			}

		}

	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK)
                {
                    String photofilePath = data.getStringExtra("photoPath");
                    if(PhotoFilterActivity.this.photoPath.compareTo(photofilePath)!=0)//if load different photo file, then archiveID is = 0
                    {
                        PhotoFilterActivity.this.archiveId = 0;
                    }
                    strForegroundPhotoPath = photofilePath;
                    isForegroundVisible = true;
                    if(bitmapForeground!=null)
                    {
                        try
                        {
                            bitmapForeground.recycle();
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            bitmapForeground = null;
                            mForegroundGPUImage.setImage(bitmapForeground);
                        }
                    }
                    onDrawnView(foregroundPhotoView , imageForegroundViewWidth , imageForegroundViewHeight);

                }
                break;
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                	
                    String photofilePath = ImageUtil.getRealPathFromURI(this , data.getData() , ImageUtil.MEDIA_TYPE_IMAGE);
                    if(PhotoFilterActivity.this.photoPath.compareTo(photofilePath)!=0)//if load different photo file, then archiveID is = 0
                    {
                    	PhotoFilterActivity.this.archiveId = 0;
                    }
                    System.out.println("---photo path= "+photofilePath+"-------");
                    //photofilePath = photofilePath.replaceAll(" " , "%20");
                    strForegroundPhotoPath = photofilePath;
                    isForegroundVisible = true;
                    if(bitmapForeground!=null)
                    {
                    	try
                    	{
                            bitmapForeground.recycle();
                    	}catch(Exception e)
                    	{
                    		e.printStackTrace();
                    	}
                    	finally
                    	{
                            bitmapForeground = null;
                    		mForegroundGPUImage.setImage(bitmapForeground);
                    	}
                    }
                    onDrawnView(foregroundPhotoView , imageForegroundViewWidth , imageForegroundViewHeight);

                } else {
                    //finish();
                }
                break;
            case PHOTO_REQUEST_CUT:
                if (resultCode == RESULT_OK) {
                    isColorResource = false;
                    if(isForegroundVisible)
                    {
                        strForegroundPhotoPath = strTmpCropPhotoFilePath;
                        if(bitmapForeground!=null)
                        {
                            try
                            {
                                bitmapForeground.recycle();
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                bitmapForeground = null;
                                mForegroundGPUImage.setImage(bitmapForeground);
                            }
                        }
                        onDrawnView(foregroundPhotoView , imageForegroundViewWidth , imageForegroundViewHeight);

                    }
                    else {
                        PhotoFilterActivity.this.photoPath = strTmpCropPhotoFilePath;
                        if (bitmapBackground != null) {
                            try {
                                bitmapBackground.recycle();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                bitmapBackground = null;
                                mBackgroundGPUImage.setImage(bitmapBackground);
                            }
                        }
                        onDrawnView(backgroundPhotoView , imageBackgroundViewWidth, imageBackgroundViewHeight);

                    }

                } else {
                    //finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

        updateImageLayerIcon();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    public void showDialog(Context context) {
        final String options[] = {"Take a Picture",
        						"Select from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a picture");
        builder.setItems(options,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int item) {
                        if(item == 0)//take new photo
                        {
                        	Intent takePhotoActivity = new Intent(PhotoFilterActivity.this , TakePhotoActivity.class);
                            startActivityForResult(takePhotoActivity, REQUEST_TAKE_PHOTO);
                        }
                        else//go to gallery
                        {
                        	Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                	        photoPickerIntent.setType("image/*");
                	        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
                        }
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
	
	
	class FilterAdapter extends BaseAdapter{
	    private Context mContext;
	    private LayoutInflater inflater ;
        private int selectedItemIndex = 0;
	    public FilterAdapter(Context c) {
	          mContext = c;
	          
	          inflater = (LayoutInflater) mContext
	    		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }

        public void setSelection(int index)
        {
            this.selectedItemIndex = index;
            notifyDataSetChanged();
        }

	    @Override
	    public int getCount() {
	      // TODO Auto-generated method stub
	      return Filters.filterNames.length;
	    }
	    @Override
	    public Object getItem(int position) {
	      // TODO Auto-generated method stub
	      return Filters.filterNames[position];
	    }
	    @Override
	    public long getItemId(int position) {
	      // TODO Auto-generated method stub
	      return 0;
	    }
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	      // TODO Auto-generated method stub
	        View itemView = null;
            if (convertView == null) {
    	  
	    	    itemView = inflater.inflate(R.layout.filter_item, null);
	 	      
	
            } else {
                itemView = convertView;

            }

            TextView txtFilterName = (TextView)itemView.findViewById(R.id.txtFilterName);
            txtFilterName.setText(Filters.filterNames[position]);
            ImageView imgFilterIcon = (ImageView)itemView.findViewById(R.id.imgFilter);
            imgFilterIcon.setImageResource(getDrawableIdFromName("filter_image"+String.valueOf(position+1)));

            LinearLayout itemLayout = (LinearLayout)itemView.findViewById(R.id.rootLayout);
            if(position == selectedItemIndex)
            {
                itemLayout.setBackgroundResource(R.drawable.photo_selected_filter_item_background);
            }
            else
            {
                itemLayout.setBackgroundResource(R.drawable.photo_nonselected_filter_item_background);
            }

	        return itemView;
	    }
	    
	    
	}

	@Override
	public void onDrawnView(ImageView view , int width, int height) {
		// TODO Auto-generated method stub
        if(width == 0 || height == 0)
            return;

        if(view == backgroundPhotoView) {
            imageBackgroundViewWidth = width;
            imageBackgroundViewHeight = height;
            imageForegroundViewWidth= width - (int)(width * 0.2);
            imageForegroundViewHeight = height - (int)(height * 0.2);

            if (bitmapBackground == null) {
                if (isColorResource) {
                    bitmapBackground = ImageUtil.decodeSampledBitmapFromResource(PhotoFilterActivity.this.getResources(), getDrawableIdFromName(photoPath), width, height);
                } else {
                    bitmapBackground = ImageUtil.decodeSampledBitmapFromImageFile(photoPath, width, height);
                }
                if (bitmapBackground != null) {
                    mBackgroundGPUImage.setImage(bitmapBackground);
                    backgroundPhotoView.setImageBitmap(bitmapBackground);
                } else {
                    Toast.makeText(PhotoFilterActivity.this, "Can't Open image", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        else if(view == foregroundPhotoView)
        {
            if (bitmapForeground == null && !strForegroundPhotoPath.equals("")) {
                bitmapForeground = ImageUtil.decodeSampledBitmapFromImageFile(strForegroundPhotoPath, width, height);

                if (bitmapForeground != null) {
                    mForegroundGPUImage.setImage(bitmapForeground);
                    foregroundPhotoView.setAdjustImageRespectRate(bitmapForeground , imageForegroundViewWidth , imageForegroundViewWidth);
                } else {
                    Toast.makeText(PhotoFilterActivity.this, "Can't Open image", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
	}

    private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        if(fileCacheItem.exists())
            fileCacheItem.delete();
        OutputStream out = null;

        try
        {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
