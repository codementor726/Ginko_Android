package com.ginko.activity.entity;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.api.request.EntityRequest;
import com.ginko.common.RuntimeContext;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityVO;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EntitySetVideoActivity extends MyBaseActivity implements View.OnClickListener{
    private static final int ALLOW_ACCESS_VIDEO = 1337;
    private static final int REQUEST_CODE_TAKE_VIDEO = 2;


    /* UI Elments */
    private ImageButton btnPrev;
    private TextView txtTitle , txtDescription;
    private ImageButton btnCameraVideo , btnCameraRollVideo;
    private ImageView btnSkip;

    /* Variables */
    private String type;
    private EntityVO entity;

    private String strVideoPath = "";
    private String strRecorderPath = "";

    private boolean isSetNewVideoInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_info_set_video);

        if(savedInstanceState!= null)
        {
            this.isSetNewVideoInfo = savedInstanceState.getBoolean("isSetNewVideo");
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            isSetNewVideoInfo = bundle.getBoolean("isSetNewVideo" , false);
            entity = (EntityVO) bundle.getSerializable("entity");
        }

        getUIObjects();
    }

    @Override
    protected void getUIObjects()
    {
        super.getUIObjects();
        txtTitle = (TextView)findViewById(R.id.textViewTitle);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
        btnSkip = (ImageView)findViewById(R.id.btnSkipVideo); btnSkip.setOnClickListener(this);
        txtDescription = (TextView)findViewById(R.id.txtDescription);
        btnCameraVideo = (ImageButton)findViewById(R.id.btnCameraVideo);
        btnCameraRollVideo = (ImageButton)findViewById(R.id.btnCameraRollVideo);

        int txtColor = 0;
        txtTitle.setText(getResources().getString(R.string.work_info));
        txtColor = getResources().getColor(R.color.photo_video_editor_work_txt_color);
        btnCameraVideo.setImageResource(R.drawable.part_a_btn_video_purple);
        btnCameraRollVideo.setImageResource(R.drawable.part_a_btn_cameraroll_purple);

        txtTitle.setText(getResources().getString(R.string.entity_info));
        txtDescription.setTextColor(txtColor);
        txtDescription.setText(getResources().getString(R.string.entity_set_video_description));
//        btnSkip.setTextColor(txtColor);

        if(isSetNewVideoInfo)
        {
            btnPrev.setVisibility(View.GONE);
            btnSkip.setVisibility(View.VISIBLE);
        }
        else
        {
            btnPrev.setVisibility(View.VISIBLE);
            btnSkip.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isSetNewVideo" , isSetNewVideoInfo);
        outState.putSerializable("entity" , this.entity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        isSetNewVideoInfo = savedInstanceState.getBoolean("isSetNewVideo" ,false);
        this.entity = (EntityVO) savedInstanceState.getSerializable("entity");

        getUIObjects();
    }

    @Override
    public void onBackPressed() {
        if(isSetNewVideoInfo) return;
        super.onBackPressed();
    }

    public void onBtnCameraInHomeVideo(View v) {

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);

    }



    public void onBtnCameraRollInHomeVideo(View v) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//		intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
//		intent.putExtra("return-data", true);

        this.startActivityForResult(intent, ALLOW_ACCESS_VIDEO);
    }

    private void goToEntityProfilePreviewScreen()
    {
        Intent entityProfilePreviewIntent = new Intent(EntitySetVideoActivity.this , OldEntityProfilePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("entity" , this.entity);
        bundle.putBoolean("isNewEntity" , true);
        entityProfilePreviewIntent.putExtras(bundle);
        startActivity(entityProfilePreviewIntent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApp.getInstance().setCurrentActivity(this);

        switch (requestCode) {

            case REQUEST_CODE_TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Uri uriVideo = data.getData();
                    Cursor cursor = this.getContentResolver().query(uriVideo, null, null, null, null);
                    if (cursor.moveToNext()) {
                        /*display_name */
                        strVideoPath = cursor.getString(cursor.getColumnIndex("_data"));
                        System.out.println("---- Video Path = "+strVideoPath+"------");
                        File videoFile = new File(strVideoPath);
                        if(videoFile.exists()) {
                            try {
                                if (Build.VERSION.SDK_INT >= 12) {
                                    //AsyncTask.class.getMethod("setDefaultExecutor", Executor.class).invoke(null, AsyncTask.SERIAL_EXECUTOR);
                                    new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, strVideoPath);
                                }
                                else
                                    new LoadVideoFileAsyncTask().execute(strVideoPath);
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case ALLOW_ACCESS_VIDEO:
                if (resultCode == RESULT_OK) {
                    Uri uriRecorder = data.getData();
                    Cursor cursor = this.getContentResolver().query(uriRecorder, null, null, null, null);
                    if (cursor.moveToNext()) {
                        strRecorderPath = cursor.getString(cursor.getColumnIndex("_data"));
                        System.out.println("---- Video Path = " + strRecorderPath + "------");
                        File videoFile = new File(strRecorderPath);
                        if(videoFile.exists()) {
                            if(Build.VERSION.SDK_INT >= 12) {
                                new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, strRecorderPath);
                            }
                            else
                                new LoadVideoFileAsyncTask().execute(strRecorderPath);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnPrev:
                finish();
                break;

            case R.id.btnSkipVideo:
                goToEntityProfilePreviewScreen();
                break;
        }
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
                String thumbPath = RuntimeContext.getAppDataFolder("Temp")+"thumb_"+fileName+".jpg";
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoFile.getAbsolutePath());
                Bitmap bmpFirstFrame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if(bmpFirstFrame == null) return "";
                FileOutputStream fos;
                BufferedOutputStream bos;
                try {

                    fos = new FileOutputStream(thumbPath);
                    bos = new BufferedOutputStream(fos);
                    bmpFirstFrame.compress(Bitmap.CompressFormat.JPEG, 15, bos);

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
                EntityRequest.uploadVideo(entity.getId(), new File(videoFilePath), new File(videoThumbFilePath), true, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            JSONObject result = response.getData();
                            entity.setVideo(result.optString("video" , ""));

                            if (isSetNewVideoInfo) {
                                goToEntityProfilePreviewScreen();
                            } else {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("entity", entity);
                                intent.putExtras(bundle);
                                EntitySetVideoActivity.this.setResult(RESULT_OK, intent);
                                EntitySetVideoActivity.this.finish();
                            }
                        } else {
                            MyApp.getInstance().showSimpleAlertDiloag(EntitySetVideoActivity.this, R.string.str_alert_failed_to_upload_video, null);
                        }
                    }
                });
            }
        }

    }
}
