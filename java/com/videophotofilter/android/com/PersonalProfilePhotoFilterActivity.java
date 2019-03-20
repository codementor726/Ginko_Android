package com.videophotofilter.android.com;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.media.ExifInterface;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.ginko.activity.user.PersonalProfileEditActivity;
import com.ginko.common.RuntimeContext;
import com.ginko.customview.ProgressHUD;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.ginko.imagecrop.BitmapManager;
import com.ginko.imagecrop.CropImageView;
import com.ginko.imagecrop.HighlightView;
import com.ginko.imagecrop.MonitoredActivity;
import com.ginko.imagecrop.Util;
import com.videophotofilter.library.android.com.Filters;
import com.videophotofilter.library.android.com.ImageUtil;
import com.videophotofilter.library.android.com.SquarePhotoImageView;
import com.videophotofilter.library.android.com.VerticalSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import customviews.library.widget.AdapterView;
import customviews.library.widget.HListView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class PersonalProfilePhotoFilterActivity extends MonitoredActivity
        implements View.OnClickListener , AdapterView.OnItemClickListener, SquarePhotoImageView.OnViewDrawListener {

    final int IMAGE_MAX_SIZE = 1024;
    private final int PHOTO_REQUEST_CUT = 3;

    /* UI Variables */

    private ImageButton btnPrev;
    private ImageButton btnDone;
    private TextView txtTitle;
    private HListView filterListView;
    private ImageView imgBtnHalfLightness , imgBtnContrast, imgBtnCrop;
    private CropImageView imgPhotoView;

    private HighlightView mCrop;
    private ProgressHUD progressDialog;
    private VerticalSeekBar lightnessSeekBar;

    /* Layout variables */
    private RelativeLayout headerLayout;
    private LinearLayout toolLayout;

    /* Variables */
    private String originalImageFilePath = "" , saveImageFilePath = "";
    private String strTmpPhotoFilePath = "";
    private String strTmpCropPhotoFilePath = "";

    private Bitmap bitmapBackground;
    int imageBackgroundViewWidth = 0 , imageBackgroundViewHeight = 0;
    int imageForegroundViewWidth = 0 , imageForegroundViewHeight =0;

    private Uri saveImageUri = null;
    private Bitmap mBitmap = null;
    private int             mAspectX;
    private int             mAspectY;
    private int             mOutputX;
    private int             mOutputY;
    private boolean         mScale;
    private boolean         mDoFaceDetection = false;
    private int             currIndex = 0;

    private int             mBrightLevel = 50;
    private int             mContrastLevel = 100;

    private String groupType = "home" ;// home or work
    private Object lockObj = new Object();

    private ContentResolver mContentResolver;

    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;

    private boolean mScaleUp = true;
    private boolean mSaving;  // Whether the "save" button is already clicked.
    private boolean mCircleCrop      = false;
    private boolean isFilterNeeded = true;
    private boolean isNewEntity = false;

    private final BitmapManager.ThreadSet mDecodingThreads =
            new BitmapManager.ThreadSet();

    private final Handler mHandler = new Handler();

    // filter values
    private GPUImage mGPUImage;
    private GPUImageBrightnessFilter brightnessFilter;
    private GPUImageContrastFilter contrastFilter;
    private GPUImageToneCurveFilter tone_curve_Filters[];
    private GPUImageGrayscaleFilter bwFilter;
    private GPUImageFilter currentFilter = null ;
    private FilterAdapter filterAdapter = null;

    private int exifOrientation = 0;

    private int scaledWidth = 0;
    private int scaledHeight = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_photofilter);

        Intent intent = this.getIntent();
        originalImageFilePath = intent.getStringExtra("imagePath");
        saveImageFilePath = intent.getStringExtra("saveImagePath");
        this.groupType = intent.getStringExtra("groupType");
        mCircleCrop = intent.getBooleanExtra("isCircleCrop" , false);
        this.isNewEntity = intent.getBooleanExtra("isNewEntity", false);
        mAspectX = intent.getIntExtra("aspect_x", 0);
        mAspectY = intent.getIntExtra("aspect_y", 0);
        mOutputX = intent.getIntExtra("output_x" , 0);
        mOutputY = intent.getIntExtra("output_y", 0);
        mScale = intent.getBooleanExtra("scale", true);

        mContentResolver = getContentResolver();

        /*groupType = "work";
        originalImageFilePath = "file:///storage/emulated/0/ginko/UserProfile/work_wallpaper.jpg";
        originalImageFilePath = "file:///storage/emulated/0/ginko/UserProfile/sample.jpg";
        saveImageFilePath = "file:///storage/emulated/0/ginko/UserProfile/1.jpg";
        mAspectX = 10; mAspectY = 4;
        mOutputX = 640; mOutputY = 260;*/

        if(originalImageFilePath.startsWith("file:/"))
            originalImageFilePath = originalImageFilePath.substring(6);

        //get Orient Image's ORIENTATION. * Add by lee
        try {
            ExifInterface exif = new ExifInterface(originalImageFilePath);     //Since API Level 5
            exifOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));

        }catch (IOException e){
            e.printStackTrace();
        }

        if(saveImageFilePath.startsWith("file:/"))
            saveImageFilePath = saveImageFilePath.substring(6);
        saveImageUri = getImageUri(saveImageFilePath);
        mBitmap = getBitmap(originalImageFilePath);

        if (mBitmap == null) {
            finish();
            return;
        }

        // Rotation for Nexus 9  * Add by lee
        if (exifOrientation == 6)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        }
        else if (exifOrientation == 8)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        }

        else if (exifOrientation == 3)
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        }
        //load filters
        mGPUImage = new GPUImage(this);
        mGPUImage.setImage(mBitmap);
        brightnessFilter = new GPUImageBrightnessFilter();
        contrastFilter = new GPUImageContrastFilter();

        loadFilterACVFiles();

        getUIObjects();
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
    protected void getUIObjects()
    {
        super.getUIObjects();
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnDone = (ImageButton)findViewById(R.id.btnDone); btnDone.setOnClickListener(this);

        headerLayout = (RelativeLayout)findViewById(R.id.header_layout);
        toolLayout = (LinearLayout)findViewById(R.id.toolLayout);

        imgBtnHalfLightness = (ImageView)findViewById(R.id.imgButtonHalfLightness);imgBtnHalfLightness.setOnClickListener(this);
        imgBtnContrast = (ImageView)findViewById(R.id.imgButtonContrast);
        imgBtnContrast.setOnClickListener(this);
        imgBtnCrop = (ImageView)findViewById(R.id.imgButtonPhotoCrop);imgBtnCrop.setOnClickListener(this);

        imgPhotoView = (com.ginko.imagecrop.CropImageView)findViewById(R.id.imgBackgroundPhotoView);

        if (Build.VERSION.SDK_INT > 11)//Build.VERSION_CODES.HONEYCOMB)
        {
            imgPhotoView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        if(groupType.equals("home"))
        {
            toolLayout.setVisibility(View.GONE);
            txtTitle.setText(getResources().getString(R.string.str_personal));
        }
        else if(groupType.equals("work"))//work
        {
            txtTitle.setText(getResources().getString(R.string.str_profile_work));
        }
        else if(groupType.equals("grey"))
        {
            txtTitle.setText("Contact");
        }
        else if(groupType.equals("entity"))//entity
        {
            txtTitle.setText(getResources().getString(R.string.str_profile_entity));
        }
        else if (groupType.equals("directory"))
        {
            txtTitle.setText("Directory");
        }
        else
        {
            txtTitle.setText("Choose Photo");
        }

       if (isNewEntity) {
            headerLayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            btnPrev.setImageResource(R.drawable.btn_back_nav_white);
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnDone.setImageResource(R.drawable.part_a_btn_check_nav_wh);
        }
//imgPhotoView.getHeight()+"="+imgPhotoView.getWidth()
        /*Rect r = new Rect(imgPhotoView.mMotionHighlightView);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, 480, 320, true);*/
        imgPhotoView.setImageBitmapResetBase(mBitmap, true);

        Util.startBackgroundJob(this, null,
                "Please wait\u2026",
                new Runnable() {
                    public void run() {
                        final CountDownLatch latch = new CountDownLatch(1);
                        final Bitmap b = mBitmap;
                        mHandler.post(new Runnable() {
                            public void run() {

                                if (b != mBitmap && b != null) {
                                    imgPhotoView.setImageBitmapResetBase(b, true);
                                    mBitmap.recycle();
                                    mBitmap = b;
                                }
                                if (imgPhotoView.getScale() == 1F) {
                                    //mBitmap = Bitmap.createScaledBitmap(mBitmap, imgPhotoView.getWidth(), imgPhotoView.getWidth(), true);
                                    //imgPhotoView.setImageBitmapResetBase(mBitmap, true);
                                    imgPhotoView.center(true, true);
                                }
                                latch.countDown();
                            }
                        });
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        mRunFaceDetection.run();
                    }
                }, mHandler);

        filterListView = (HListView)findViewById(R.id.filterlistview);
        filterAdapter = new FilterAdapter(this); filterAdapter.setSelection(0);
        filterListView.setAdapter(filterAdapter);
        filterListView.setOnItemClickListener(this);

        currIndex = 0;

        lightnessSeekBar = (VerticalSeekBar)findViewById(R.id.lightnessSeekBar);
        lightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                synchronized (lockObj) {
                    if (currIndex == 0)
                    {
                        //brightnessFilter.setBrightness(progress);
                        if (currentFilter != null && currentFilter != brightnessFilter) {
                            currentFilter = brightnessFilter;
                            mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
                        }

                        brightnessFilter.setBrightness(range(progress, -1.0f, 1.0f));
                        mGPUImage.setFilter(brightnessFilter);
                        imgPhotoView.setImageBitmap(mGPUImage.getBitmapWithFilterApplied());
                        mBrightLevel = progress;
                        /*
                        mGPUImage.setImage(mBitmap);
                        brightnessFilter.setBrightness(range(progress, -1.0f, 1.0f));
                        mGPUImage.setFilter(brightnessFilter);
                        Bitmap bitmap1 = mGPUImage.getBitmapWithFilterApplied();

                        GPUImage mGPUImage2 = new GPUImage(PersonalProfilePhotoFilterActivity.this);
                        mGPUImage2.setImage(bitmap1);
                        contrastFilter.setContrast(range(mContrastLevel, -1.0f, 1.0f));
                        mGPUImage2.setFilter(contrastFilter);
                        imgPhotoView.setImageBitmap(mGPUImage2.getBitmapWithFilterApplied());
                        mBrightLevel = progress;
                        */
                        /*
                        mGPUImage.setImage(mBitmap);
                        GPUImageFilterGroup filterGroup;
                        filterGroup = new GPUImageFilterGroup();
                        brightnessFilter.setBrightness(range(progress, -1.0f, 1.0f));
                        contrastFilter.setContrast(range(mContrastLevel, -1.0f, 1.0f));
                        filterGroup.addFilter(brightnessFilter);
                        filterGroup.addFilter(contrastFilter);

                        List<GPUImageFilter> filters = filterGroup.getMergedFilters();
                        Iterator<GPUImageFilter> iterator = filters.iterator();

                        while(iterator.hasNext()){
                            GPUImageFilter currentFilter = iterator.next();
                            mGPUImage.setFilter(currentFilter);
                        }
                        */

                    } else if (currIndex == 1)
                    {
                        //contrastFilter.setContrast(progress);
                        if (currentFilter != null && currentFilter != contrastFilter) {
                            currentFilter = contrastFilter;
                            mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
                        }
                        contrastFilter.setContrast(range(progress, -1.0f, 1.0f));
                        mGPUImage.setFilter(contrastFilter);
                        imgPhotoView.setImageBitmap(mGPUImage.getBitmapWithFilterApplied());
                        mContrastLevel = progress;

                        /*
                        mGPUImage.setImage(mBitmap);
                        brightnessFilter.setBrightness(range(mBrightLevel, -1.0f, 1.0f));
                        mGPUImage.setFilter(brightnessFilter);
                        Bitmap bitmap1 = mGPUImage.getBitmapWithFilterApplied();

                        GPUImage mGPUImage2 = new GPUImage(PersonalProfilePhotoFilterActivity.this);
                        mGPUImage2.setImage(bitmap1);
                        contrastFilter.setContrast(range(progress, -1.0f, 1.0f));
                        mGPUImage2.setFilter(contrastFilter);
                        imgPhotoView.setImageBitmap(mGPUImage2.getBitmapWithFilterApplied());
                        mContrastLevel = progress;
                        */
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

            }
        }
        );
        lightnessSeekBar.setVisibility(View.INVISIBLE);
    }

    private float range(final int percentage, final float start, final float end) {
        return (end - start) * percentage / 100.0f + start;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BitmapManager.instance().cancelThreadDecoding(mDecodingThreads);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PHOTO_REQUEST_CUT:
                if (resultCode == RESULT_OK) {
                    PersonalProfilePhotoFilterActivity.this.originalImageFilePath = strTmpCropPhotoFilePath;
                    Uri cropUri = getImageUri(strTmpCropPhotoFilePath);
                    getDropboxIMGSize(cropUri);

                    if (bitmapBackground != null) {
                        try {
                            bitmapBackground.recycle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            bitmapBackground = null;
                            mGPUImage.setImage(bitmapBackground);
                        }
                    }

                    onDrawnView(imgPhotoView , imageBackgroundViewWidth, imageBackgroundViewHeight);


                } else {
                    //finish();
                }
                break;
        }
    }

    @Override
    public void onDrawnView(ImageView view , int width, int height) {
        // TODO Auto-generated method stub
        if(width == 0 || height == 0)
            return;

        if (bitmapBackground == null) {
            //    bitmapBackground = ImageUtil.decodeSampledBitmapFromResource(PersonalProfilePhotoFilterActivity.this.getResources(), getDrawableIdFromName(photoPath), width, height);
            bitmapBackground = ImageUtil.decodeSampledBitmapFromImageFile(originalImageFilePath, width, height);
            if (bitmapBackground != null) {
                mGPUImage.setImage(bitmapBackground);
                imgPhotoView.setImageBitmap(bitmapBackground);
            } else {
                Toast.makeText(PersonalProfilePhotoFilterActivity.this, "Can't Open image", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {

            mBitmap.recycle();
        }
    }
    //Filter Item Click
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        filterAdapter.setSelection(position);

        synchronized(lockObj)
        {
            if(mBitmap != null) {
                if (position == 0)//original bitmap
                {
                    currentFilter = null;
                    mGPUImage.setImage(mBitmap);
                    mGPUImage.setFilter(tone_curve_Filters[position]);
                    imgPhotoView.setImageBitmap(mBitmap);
                    currentFilter = tone_curve_Filters[position];
                }  else if(position < tone_curve_Filters.length-1){
                    currentFilter = tone_curve_Filters[position];
                    //apply filter
                    mGPUImage.setFilter(tone_curve_Filters[position]);
                    imgPhotoView.setImageBitmap(mGPUImage.getBitmapWithFilterApplied());
                }
                else if(position == tone_curve_Filters.length-1 ) //b&W filter
                {
                    currentFilter = bwFilter;
                    //apply filter
                    mGPUImage.setFilter(currentFilter);
                    imgPhotoView.setImageBitmap(mGPUImage.getBitmapWithFilterApplied());
                }
            }

        }

    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPrev:
                //this intent will notify that the filtered photo was from camera or gallery , if photo was from camera , then this takes the screen to the camera screen
                Intent intent = new Intent();
                this.setResult(RESULT_CANCELED, intent);
                finish();
                break;
            case R.id.btnDone:
                if (mSaving) return;
                try {
                    onSaveClicked();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.imgButtonHalfLightness:
                if (lightnessSeekBar.getVisibility() == View.INVISIBLE) {
                    lightnessSeekBar.setVisibility(View.VISIBLE);
                    lightnessSeekBar.setProgress(mBrightLevel);
                    currIndex = 0;
                    setControlStyle(0);
                } else {
                    if (currIndex == 0)
                    {
                        lightnessSeekBar.setVisibility(View.INVISIBLE);
                        imgBtnHalfLightness.setImageResource(R.drawable.photo_half_contrast);
                    } else {
                        currIndex = 0;
                        lightnessSeekBar.setProgress(mBrightLevel);
                        setControlStyle(0);
                    }
                }
                break;
            case R.id.imgButtonContrast:
                if (lightnessSeekBar.getVisibility() == View.INVISIBLE) {
                    lightnessSeekBar.setVisibility(View.VISIBLE);
                    lightnessSeekBar.setProgress(mContrastLevel);
                    currIndex = 1;
                    setControlStyle(1);
                } else {
                    if (currIndex == 1)
                    {
                        lightnessSeekBar.setVisibility(View.INVISIBLE);
                        imgBtnContrast.setImageResource(R.drawable.photo_contrast);
                    } else {
                        currIndex = 1;
                        lightnessSeekBar.setProgress(mContrastLevel);
                        setControlStyle(1);
                    }
                }
                break;
            case R.id.imgButtonPhotoCrop:
                setControlStyle(2);
                strTmpPhotoFilePath = RuntimeContext.getAppDataFolder("Temp") + "TempPhoto.jpg";
                final File tempPhotoFile = new File(strTmpPhotoFilePath);
                if (tempPhotoFile.exists())
                    tempPhotoFile.delete();
                Bitmap currentBitmap = null;
                currentBitmap = mGPUImage.getBitmapWithFilterApplied();

                progressDialog = ProgressHUD.show(PersonalProfilePhotoFilterActivity.this, "Loading...", true, false, null);

                mGPUImage.saveToPictures(currentBitmap, MyApp.APP_NAME,
                        strTmpPhotoFilePath,
                        new GPUImage.OnPictureSavedListener() {
                            @Override
                            public void onPictureSaved(final Uri uri) {
                                if (progressDialog != null)
                                    progressDialog.dismiss();
                                startPhotoZoom(Uri.fromFile(tempPhotoFile));
                            }
                        });
                break;
        }
    }

    private Uri getImageUri(String path) {

        return Uri.fromFile(new File(path));
    }

    private void getDropboxIMGSize(Uri uri){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        imageBackgroundViewHeight = options.outHeight;
        imageBackgroundViewWidth = options.outWidth;
    }

    private void setControlStyle(int param)
    {
        if (param == 0)
        {
            imgBtnHalfLightness.setImageResource(R.drawable.photo_half_contrast_white);
            imgBtnContrast.setImageResource(R.drawable.photo_contrast);
            imgBtnCrop.setImageResource(R.drawable.photo_crop);
        }
        else if (param == 1)
        {
            imgBtnHalfLightness.setImageResource(R.drawable.photo_half_contrast);
            imgBtnContrast.setImageResource(R.drawable.photo_contrast_white);
            imgBtnCrop.setImageResource(R.drawable.photo_crop);
        }
        else if (param == 2)
        {
            imgBtnHalfLightness.setImageResource(R.drawable.photo_half_contrast);
            imgBtnContrast.setImageResource(R.drawable.photo_contrast);
            imgBtnCrop.setImageResource(R.drawable.photo_crop_white);
        }
    }

    private Bitmap getBitmap(String path) {

        Uri uri = getImageUri(path);
        InputStream in = null;
        try {
            in = mContentResolver.openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();

            return b;
        } catch (FileNotFoundException e) {
            Log.e("PhotoFilter", "file " + path + " not found");
        } catch (IOException e) {
            Log.e("PhotoFilter", "file " + path + " not found");
        }
        return null;
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

    private void onSaveClicked() throws Exception {
        // TODO this code needs to change to use the decode/crop/encode single
        // step api so that we don't require that the whole (possibly large)
        // bitmap doesn't have to be read into memory
        if (mSaving) return;

        if (mCrop == null) {

            return;
        }

        mSaving = true;
        if(imgPhotoView != null)
            imgPhotoView.setIsSaving(true);


        if(mGPUImage != null)
            mBitmap = mGPUImage.getBitmapWithFilterApplied();

        // If we are circle cropping, we want alpha channel, which is the
        // third param here.
        Rect originalBitmapRect = new Rect(0 , 0 , mBitmap.getWidth() , mBitmap.getHeight());
        Rect r = imgPhotoView.getCroppedRect(originalBitmapRect);

        int width = r.width();
        int height = r.height();

        Bitmap croppedImage;

        try {

            croppedImage = Bitmap.createBitmap(width, height,
                    mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        } catch (Exception e) {
            throw e;
        }
        if (croppedImage == null) {

            return;
        }

        {
            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(mBitmap, r, dstRect, null);
            if (mCircleCrop) {

                // OK, so what's all this about?
                // Bitmaps are inherently rectangular but we want to return
                // something that's basically a circle.  So we fill in the
                // area around the circle with alpha.  Note the all important
                // PortDuff.Mode.CLEAR.
                Path p = new Path();
                p.addCircle(width / 2F, height / 2F, width / 2F,
                        Path.Direction.CW);
                canvas.clipPath(p, Region.Op.DIFFERENCE);
                canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
            }
        }


		/* If the output is required to a specific size then scale or fill */
        if (mOutputX != 0 && mOutputY != 0) {
            if (mScale) {

                /* Scale the image to the required dimensions */
                Bitmap old = croppedImage;
                croppedImage = Util.transform(new Matrix(),
                        croppedImage, mOutputX, mOutputY, mScaleUp);
                if (old != croppedImage) {

                    old.recycle();
                }
            } else {

				/* Don't scale the image crop it to the size requested.
                 * Create an new image with the cropped image in the center and
				 * the extra space filled.
				 */

                // Don't scale the image but instead fill it so it's the
                // required dimension
                Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY,
                        Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(b);

                Rect srcRect = imgPhotoView.getCroppedRect(originalBitmapRect);
                Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

                int dx = (srcRect.width() - dstRect.width()) / 2;
                int dy = (srcRect.height() - dstRect.height()) / 2;

				/* If the srcRect is too big, use the center part of it. */
                srcRect.inset(Math.max(0, dx), Math.max(0, dy));

				/* If the dstRect is too big, use the center part of it. */
                dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

				/* Draw the cropped bitmap in the center */
                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

				/* Set the cropped bitmap as the new bitmap */
                croppedImage.recycle();
                croppedImage = b;
            }
        }

        // Return the cropped image directly or save it to the specified URI.
        /*Bundle myExtras = getIntent().getExtras();
        if (myExtras != null && (myExtras.getParcelable("data") != null
                || myExtras.getBoolean(RETURN_DATA))) {

            Bundle extras = new Bundle();
            extras.putParcelable(RETURN_DATA_AS_BITMAP, croppedImage);
            setResult(RESULT_OK,
                    (new Intent()).setAction(ACTION_INLINE_DATA).putExtras(extras));
            finish();
        } else*/ {
            final Bitmap b = croppedImage;
            Util.startBackgroundJob(this, null, "Saving image...",
                    new Runnable() {
                        public void run() {

                            saveOutput(b);
                        }
                    }, mHandler);
        }
    }

    private void saveOutput(Bitmap croppedImage) {

        if (saveImageUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(saveImageUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, 90, outputStream);
                }
            } catch (IOException ex) {

                Log.e("PhotoFilter", "Cannot open file: " + saveImageUri, ex);
                setResult(RESULT_CANCELED);
                finish();
                return;
            } finally {

                Util.closeSilently(outputStream);
            }

            Bundle extras = new Bundle();
            Intent intent = new Intent();
            intent.putExtras(extras);
            //intent.putExtra(ORIENTATION_IN_DEGREES, Util.getOrientationInDegree(this));
            setResult(RESULT_OK, intent);
        } else {

            Log.e("PhotoFilter", "not defined image url");
        }
        croppedImage.recycle();
        finish();
    }

    Runnable mRunFaceDetection = new Runnable() {
        @SuppressWarnings("hiding")
        float mScale = 1F;
        Matrix mImageMatrix;
        FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
        int mNumFaces;

        // For each face, we create a HightlightView for it.
        private void handleFace(FaceDetector.Face f) {

            PointF midPoint = new PointF();

            int r = ((int) (f.eyesDistance() * mScale)) * 2;
            f.getMidPoint(midPoint);
            midPoint.x *= mScale;
            midPoint.y *= mScale;

            int midX = (int) midPoint.x;
            int midY = (int) midPoint.y;

            HighlightView hv = new HighlightView(imgPhotoView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            RectF faceRect = new RectF(midX, midY, midX, midY);
            faceRect.inset(-r, -r);
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left);
            }

            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top);
            }

            if (faceRect.right > imageRect.right) {
                faceRect.inset(faceRect.right - imageRect.right,
                        faceRect.right - imageRect.right);
            }

            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(faceRect.bottom - imageRect.bottom,
                        faceRect.bottom - imageRect.bottom);
            }

            hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop,
                    mAspectX != 0 && mAspectY != 0);

            imgPhotoView.add(hv);
            imgPhotoView.center(true, true);
        }

        // Create a default HightlightView if we found no face in the picture.
        private void makeDefault() {

            HighlightView hv = new HighlightView(imgPhotoView);
            int viewWidth = imgPhotoView.getWidth();
            int viewHeight = imgPhotoView.getHeight();

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            // calculate the scale - in this case = 0.4f
            int minWidthHeight = Math.min(viewWidth , viewHeight);
            int minBitmapWidthHeight = Math.min(width , height);
            float scaleWidth = 1.0f, scaleHeight = 1.0f;
            if(minWidthHeight < minBitmapWidthHeight) {
                scaleWidth = ((float) minWidthHeight) / width;
                scaleHeight = ((float) minWidthHeight) / height;
            }
            else
            {
                scaleWidth = ((float) minBitmapWidthHeight) / width;
                scaleHeight = ((float) minBitmapWidthHeight) / height;
            }
            // createa matrix for the manipulation
            Matrix matrix = mImageMatrix;
            // resize the bit map
            float scale = (scaleWidth <= scaleHeight) ? scaleWidth : scaleHeight;
            //matrix.postScale(scaleWidth, scaleHeight);

            scale = scaleWidth;

            matrix.postScale(scale, scale);
            imgPhotoView.setImageMatrix(matrix);

            // rotate the Bitmap
            //matrix.postRotate(45);

            //Add by lee for get width and height after scaled.
            int widthAfterScaled = imgPhotoView.getMeasuredWidth();
            int heightAfterScaled = imgPhotoView.getMeasuredHeight();
            int originWidth = mBitmap.getWidth();
            int originHeight = mBitmap.getHeight();

            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                    width, height, matrix, true);
            mBitmap = resizedBitmap;
            imgPhotoView.setImageBitmap(mBitmap);
            /* //Add by lee for PHOTO resize
            if(mGPUImage != null)
                mGPUImage.setImage(mBitmap);*/
            mImageMatrix = imgPhotoView.getImageMatrix();

            width = mBitmap.getWidth();
            height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            //Add by leeW
            if(originHeight > originWidth) {
                float tmp_scale = (float)imgPhotoView.getMeasuredHeight() / (float)originHeight;
                //widthAfterScaled = (int)(originWidth);
                widthAfterScaled = (int) (originWidth * tmp_scale);
            }
            else if(originHeight < originWidth) {
                float tmp_scale = (float)imgPhotoView.getMeasuredWidth() / (float)originWidth;
                //heightAfterScaled = (int)(originHeight);
                heightAfterScaled = (int) (originHeight * tmp_scale);
            }
            //////////////////////////////////////////
            //modify by leeW
            Rect drawRect = new Rect(0, 0, widthAfterScaled, heightAfterScaled);
            //Rect drawRect = new Rect(0, 0, width, height);
            //imgPhotoView.getDrawingRect(drawRect);
            int drawWidth = drawRect.right-drawRect.left;
            int drawHeight = drawRect.bottom-drawRect.top;
            // make the default size about 4/5 of the width or height
            int cropWidth = (int)(drawWidth);
            int cropHeight = (int)(drawHeight);

            if (mAspectX != 0 && mAspectY != 0) {
                if(imgPhotoView.getWidth() <= imgPhotoView.getHeight()) {
                    if (mAspectX >= mAspectY) {
                        //modify by leeW
                        if(cropHeight > cropWidth)
                            cropHeight = cropWidth * mAspectY / mAspectX;
                        else
                            cropHeight = cropHeight * mAspectY / mAspectX;
                    } else {
                        //modify by leeW
                        if(cropHeight < cropWidth)
                            cropWidth = cropHeight * mAspectX / mAspectY;
                        else
                            cropWidth = cropWidth * mAspectX / mAspectY;
                    }
                }
                else{
                    if (mAspectX >= mAspectY) {
                        //modify by leeW
                        if(cropHeight > cropWidth)
                            cropHeight = cropWidth * mAspectY / mAspectX;
                        else
                            cropHeight = cropHeight * mAspectY / mAspectX;
                    } else {
                        //modify by leeW
                        if(cropHeight < cropWidth)
                            cropWidth = cropHeight * mAspectX / mAspectY;
                        else
                            cropWidth = cropWidth * mAspectX / mAspectY;
                    }
                }

                /*if(mAspectX > mAspectY)
                    cropHeight = cropWidth * mAspectY / mAspectX;
                else
                    cropWidth = cropHeight * mAspectX / mAspectY;

                //Add by lee for photo hv
                if(mCircleCrop && imageRect.width() > imageRect.height())
                    cropWidth = cropHeight * mAspectX / mAspectY;*/
            }

            int x = (viewWidth- cropWidth) / 2;
            int y = (viewHeight - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);

            imgPhotoView.mHighlightViews.clear(); // Thong added for rotate

            //Add by lee for PHOTO resize
            /*mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)cropRect.width(), (int)cropRect.height(), true);
            if(mGPUImage != null)
                mGPUImage.setImage(mBitmap);
            imgPhotoView.setImageBitmapResetBase(mBitmap, true);
            imgPhotoView.center(true, true);*/
            ///////////////////////////////
            imgPhotoView.add(hv);
        }

        // Scale the image down for faster face detection.
        private Bitmap prepareBitmap() {

            if (mBitmap == null) {

                return null;
            }

            // 256 pixels wide is enough.
            if (mBitmap.getWidth() > 256) {

                mScale = 256.0F / mBitmap.getWidth();
            }
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            return Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        }

        public void run() {
            mImageMatrix = imgPhotoView.getImageMatrix();
            Bitmap faceBitmap = prepareBitmap();

            mScale = 1.0F;// / mScale;
            if (faceBitmap != null && mDoFaceDetection) {
                FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
                        faceBitmap.getHeight(), mFaces.length);
                mNumFaces = detector.findFaces(faceBitmap, mFaces);
            }

            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle();
            }

            mHandler.post(new Runnable() {
                public void run() {

                    //mWaitingToPick = mNumFaces > 1;
                    if (mNumFaces > 0) {
                        for (int i = 0; i < mNumFaces; i++) {
                            handleFace(mFaces[i]);
                        }
                    } else {
                        makeDefault();
                    }
                    imgPhotoView.invalidate();
                    if (imgPhotoView.mHighlightViews.size() == 1) {
                        mCrop = imgPhotoView.mHighlightViews.get(0);
                        mCrop.setFocus(true);
                    }

                    if (mNumFaces > 1) {
                        Toast.makeText(PersonalProfilePhotoFilterActivity.this,
                                "Multi face crop help",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    private int getDrawableIdFromName(String name)
    {
        Resources resources = getResources();
        final int resourceId = resources.getIdentifier(name, "drawable",
                getPackageName());
        return resourceId;
    }
    class FilterAdapter extends BaseAdapter {
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
}