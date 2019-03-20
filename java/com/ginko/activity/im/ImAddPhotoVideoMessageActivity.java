package com.ginko.activity.im;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.common.RuntimeContext;
import com.ginko.ginko.R;
import com.videophotofilter.android.com.PersonalProfilePhotoFilterActivity;
import com.videophotofilter.android.com.RecordFilterCameraActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImAddPhotoVideoMessageActivity extends Activity implements View.OnClickListener{

    private TextView txtTitle , txtBottomDescription;
    private ImageView btnClose;
    private ImageView btnFromCamera , btnFromGallery;
    private RelativeLayout headerLayout;

    private boolean isRequestForPhoto = false;
    private boolean isEntityMessage = false; //from entity message screen
    private boolean isCreate = false;

    private String strTempPhotoPath = "" ;

    private final int TAKE_PHOTO_FROM_CAMERA = 1;
    private final int TAKE_PHOTO_FROM_GALLERY = 2;
    private final int TAKE_VIDEO_FROM_CAMERA = 3;
    private final int TAKE_VIDEO_FROM_GALLERY = 4;
    private final int TAKE_VIDEO_FILTER_GALLERY = 44;
    private final int FILTER_PHOTO = 22;

    private boolean isResult = false;

    private File tempFile = null;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo_video_message);

        Intent intent = this.getIntent();
        this.isRequestForPhoto = intent.getBooleanExtra("isPhotoIntent" , false);
        this.isEntityMessage = intent.getBooleanExtra("isEntityMessage" , false);
        this.isCreate = intent.getBooleanExtra("isCreate", false);

        getUIObjects();
    }

    private void getUIObjects()
    {

        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnFromCamera = (ImageView)findViewById(R.id.imgFromCamera); btnFromCamera.setOnClickListener(this);
        btnFromGallery = (ImageView)findViewById(R.id.imgFromGallery); btnFromGallery.setOnClickListener(this);

        txtTitle = (TextView)findViewById(R.id.txtTittle);
        txtBottomDescription = (TextView)findViewById(R.id.txtDescription); txtBottomDescription.setVisibility(View.GONE);

        headerLayout = (RelativeLayout)findViewById(R.id.headerlayout);

        if(this.isRequestForPhoto) //activity to get photo file
        {
            txtTitle.setText(getResources().getString(R.string.str_choose_photo_title));
            //txtBottomDescription.setVisibility(View.VISIBLE);
            txtBottomDescription.setVisibility(View.GONE);
            btnFromCamera.setImageResource(R.drawable.part_a_btn_camera_purple);
        }
        else//activity to get video file
        {
            txtTitle.setText(getResources().getString(R.string.str_choose_video_title));
            txtBottomDescription.setVisibility(View.GONE);
            btnFromCamera.setImageResource(R.drawable.part_a_btn_video_purple);
        }

        if (this.isCreate)
        {
            headerLayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            btnClose.setImageResource(R.drawable.close_white);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            switch(requestCode)
            {
                case TAKE_PHOTO_FROM_CAMERA:
                    System.out.println("-----Captured Photo = "+uri.getPath()+"----");
                    /*Intent resultIntenet1 = new Intent();
                    resultIntenet1.putExtra("photoPath" , uri.getPath());
                    this.setResult(RESULT_OK , resultIntenet1);
                    finish();*/

                    strTempPhotoPath = uri.getPath();
                    if (!strTempPhotoPath.contains("file://"))
                        strTempPhotoPath = "file://"+strTempPhotoPath;

                    goToFilterScreen();
                    break;
                case TAKE_PHOTO_FROM_GALLERY:
                    if(data!=null)
                    {
                        uri = data.getData();
                        File myFile = new File(uri.getPath());
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        String picturePath = "";

                        if (cursor != null && cursor.moveToFirst())
                        {
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            picturePath = cursor.getString(columnIndex);
                            cursor.close();
                        }
                        else
                        {
                            if (myFile.exists())
                                picturePath = myFile.getAbsolutePath();
                        }
                        System.out.println("-----Photo Path= "+picturePath+"----");
                        /*Intent resultIntenet2 = new Intent();
                        resultIntenet2.putExtra("photoPath" , picturePath);
                        this.setResult(RESULT_OK , resultIntenet2);
                        finish();
                        */
                        strTempPhotoPath = picturePath;
                        if (!strTempPhotoPath.contains("file://"))
                            strTempPhotoPath = "file://"+strTempPhotoPath;

                        goToFilterScreen();
                    }
                    break;
                case TAKE_VIDEO_FROM_CAMERA:
                    isResult = true;
                    String strRecordedVideoPath = data.getStringExtra("strMoviePath");
                    if(Build.VERSION.SDK_INT >= 12)
                        new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR , strRecordedVideoPath);
                    else
                        new LoadVideoFileAsyncTask().execute(strRecordedVideoPath);


                    break;
                case TAKE_VIDEO_FROM_GALLERY:
                    if(data!=null)
                    {
                        uri = data.getData();

                        String path = uri.getPath();
                        if(uri.toString().contains("content://"))
                            path = getPath(uri);

                        Intent intent = new Intent(ImAddPhotoVideoMessageActivity.this, RecordFilterCameraActivity.class);
                        intent.putExtra("strPathFromGallery", path);
                        intent.putExtra("fromGallery", true);
                        intent.putExtra("isNewEntity", isCreate);
                        startActivityForResult(intent, TAKE_VIDEO_FILTER_GALLERY);
//                        startActivity(intent);
//                        finish();
                    }
                    break;
                case FILTER_PHOTO:
                    Intent resultIntenet2 = new Intent();
                    resultIntenet2.putExtra("photoPath" , strTempPhotoPath);
                    this.setResult(RESULT_OK , resultIntenet2);
                    finish();
                    break;
                case TAKE_VIDEO_FILTER_GALLERY:
                    isResult = true;
                    String videoPath = data.getStringExtra("strMoviePath");

                    if(Build.VERSION.SDK_INT >= 12)
                        new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR , videoPath);
                    else
                        new LoadVideoFileAsyncTask().execute(videoPath);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnClose:
                this.setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.imgFromCamera:
                if(isResult) return;
                if(this.isRequestForPhoto) //take photo from camera
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String FolderName = "Messages";
                    if(isEntityMessage)
                        FolderName = "EntityMessages";
                    uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder(FolderName)+
                            String.valueOf(System.currentTimeMillis()) + ".jpg"));
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
        }
                else//take video from camera
                {
                    /* modify by lee for video filter function.
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    String FolderName = "Messages";
                    if(isEntityMessage)
                        FolderName = "EntityMessages";
                    uri = Uri.fromFile(new File(RuntimeContext.getAppDataFolder(FolderName)+
                            String.valueOf(System.currentTimeMillis()) + ".mp4"));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, TAKE_VIDEO_FROM_CAMERA);*/
                    Intent getVideoIntent = new Intent(ImAddPhotoVideoMessageActivity.this, RecordFilterCameraActivity.class);
                    getVideoIntent.putExtra("isNewEntity", isCreate);
                    startActivityForResult(getVideoIntent, TAKE_VIDEO_FROM_CAMERA);
                }
                break;

            case R.id.imgFromGallery:
                if(isResult) return;
                if(this.isRequestForPhoto) //take photo from gallery
                {
                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                }
                else//take video from gallery
                {
                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("video/*");
                    startActivityForResult(i, TAKE_VIDEO_FROM_GALLERY);
                }
                break;
        }
    }

    private void goToFilterScreen()
    {
        Intent intent = new Intent(ImAddPhotoVideoMessageActivity.this , PersonalProfilePhotoFilterActivity.class);
        intent.putExtra("imagePath" , strTempPhotoPath);
        File filteredImagePath = null;
        tempFile = new File(strTempPhotoPath);
        filteredImagePath = new File(RuntimeContext.getAppDataFolder("Messages") +
                    String.valueOf(System.currentTimeMillis()+".jpg"));
        /*
        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }*/
        strTempPhotoPath = filteredImagePath.getAbsolutePath();
        intent.putExtra("saveImagePath", strTempPhotoPath);
        intent.putExtra("groupType" , "message");
        intent.putExtra("aspect_x", 1);
        intent.putExtra("aspect_y", 1);
        intent.putExtra("isNewEntity", isCreate);

        startActivityForResult(intent , FILTER_PHOTO);
    }

    private class LoadVideoFileAsyncTask extends AsyncTask<String, Void, String> {
        private String videoFilePath = "";
        private String videoThumbFilePath = "";

        @Override
        protected String doInBackground(String... params) {

            videoFilePath = params[0];
            System.out.println("-----Video Path= "+videoFilePath+"----");
            File videoFile = new File(videoFilePath);
            if(videoFile.exists()) {
                String fileName = videoFile.getName();
                if(fileName.contains("."))
                    fileName = fileName.substring(0 , fileName.lastIndexOf("."));
                String FolderName = "Messages";
                if(isEntityMessage)
                    FolderName = "EntityMessages";
                String thumbPath = RuntimeContext.getAppDataFolder(FolderName)+"thumb_"+fileName+".jpg";
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoFile.getAbsolutePath());
                Bitmap bmpFirstFrame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if(bmpFirstFrame == null) return "";
                FileOutputStream fos;
                BufferedOutputStream bos;
                try {

                    fos = new FileOutputStream(thumbPath);
                    bos = new BufferedOutputStream(fos);
                    bmpFirstFrame.compress(Bitmap.CompressFormat.JPEG, 60, bos);

                    bos.flush();
                    bos.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
                finally {
                    bmpFirstFrame.recycle();
                }

                return thumbPath;
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            this.videoThumbFilePath = result;
            System.out.println("-----Thumb Video = "+videoThumbFilePath+"----");

            if(this.videoThumbFilePath.equals("") == false)
            {
                Intent videoFileResultIntent = new Intent();
                videoFileResultIntent.putExtra("videoPath" , this.videoFilePath);
                videoFileResultIntent.putExtra("thumbPath" , this.videoThumbFilePath);
                ImAddPhotoVideoMessageActivity.this.setResult(RESULT_OK, videoFileResultIntent);
                ImAddPhotoVideoMessageActivity.this.finish();
            }
        }

    }
}
