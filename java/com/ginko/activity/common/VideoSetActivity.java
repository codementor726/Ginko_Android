package com.ginko.activity.common;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.TradeCard;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.customview.ProgressHUD;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.TcVideoVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;
import com.videophotofilter.android.com.ArchiveMediaItem;
import com.videophotofilter.android.com.MediaArchiveListAdapter;
import com.videophotofilter.android.com.RecordFilterCameraActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import customviews.library.widget.HListView;

public class VideoSetActivity extends MyBaseActivity implements View.OnClickListener,
        MediaArchiveListAdapter.ArchiveItemClickListener{
    private static final int ALLOW_ACCESS_VIDEO_FROM_DIRECTORY = 1337;
    private static final int TAKE_VIDEO_FROM_CAMERA = 2;
    private static final int TAKE_VIDEO_FROM_ARCHIVE = 321;
    private static final int TAKE_VIDEO_FILTER_GALLERY = 1231;

    /* UI Elments */
    private ImageButton btnPrev;
    private ImageView archiveImg;
    private TextView txtTitle , txtDescription, archiveTxt;
    private ImageButton btnCameraVideo , btnCameraRollVideo;
    private ImageView btnSkip;
    private RelativeLayout headerLayout;

    /* Variables */
    private String type;
    private int group_type = 1;
    private int entity_Id;
    private EntityVO entity;

    private String strVideoPath = "";
    private String strRecorderPath = "";

    private boolean isSetNewVideoInfo = false;
    private boolean isNewEntity = false;
    private boolean isFromGallery = false;
    private boolean isResult = false;

    private HListView archiveListView;
    private com.videophotofilter.android.com.MediaArchiveListAdapter mAdapter;

    private float ratio = 1.0f;

    private ProgressHUD progressHUD;
    private String mSaveDirectoryPath = RuntimeContext.getAppDataFolder("temp");

    private OutputStream output;
    public Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 0:

                    if(mAdapter != null)
                    {
                        System.out.println("----load finished and refresh called----");
                        mAdapter.notifyDataSetChanged();
                    }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_info_set_video);

        //get intent
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getString("typeId");
        if(type.equals("personalInfo"))
            group_type = bundle.getInt("isHome");
        entity_Id = bundle.getInt("typeIdVal");
        isNewEntity = bundle.getBoolean("isNewEntity");

        ratio = Uitils.getScreenRatioViaIPhone(this);
        getUIObjects();
    }

    protected void getUIObjects()
    {
        headerLayout = (RelativeLayout)findViewById(R.id.headerlayout);
        txtTitle = (TextView)findViewById(R.id.textViewTitle);
        btnPrev = (ImageButton)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this); btnPrev.setVisibility(View.GONE);
        btnSkip = (ImageView)findViewById(R.id.btnSkipVideo); btnSkip.setOnClickListener(this); btnSkip.setVisibility(View.VISIBLE);

        archiveImg = (ImageView)findViewById(R.id.archive);
        archiveTxt = (TextView)findViewById(R.id.archiveTxt);

        txtDescription = (TextView)findViewById(R.id.txtDescription);
        btnCameraVideo = (ImageButton)findViewById(R.id.btnCameraVideo);
        btnCameraRollVideo = (ImageButton)findViewById(R.id.btnCameraRollVideo);

        if(type.equals("personalInfo"))  //when Personal
        {
            if(group_type == 1) {
                txtTitle.setText("Personal Info");
                txtDescription.setText(getResources().getString(R.string.Personal_set_video_description));
            }
            else{
                txtTitle.setText("Work Info");
                txtDescription.setText(getResources().getString(R.string.work_content_description_video));
            }
        }
        else if(type.equals("entityInfo"))  //when Entity
        {
            txtTitle.setText("Entity Info");
            txtDescription.setText(getResources().getString(R.string.entity_set_video_description));
        }


        if(isNewEntity){
            headerLayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
            txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
            txtDescription.setTextColor(getResources().getColor(R.color.photo_video_editor_work_txt_color));
            btnSkip.setImageResource(R.drawable.bs_ic_clear);
            btnCameraVideo.setImageResource(R.drawable.part_a_btn_video_purple);
            btnCameraRollVideo.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
            archiveImg.setImageResource(R.drawable.archive_img);
            archiveTxt.setTextColor(getResources().getColor(R.color.photo_video_editor_work_txt_color));
        }
        /*btnCameraVideo.setImageResource(R.drawable.part_a_btn_video_purple);
        btnCameraRollVideo.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
        txtDescription.setTextColor(getResources().getColor(R.color.photo_video_editor_work_txt_color));
        btnSkip.setTextColor(getResources().getColor(R.color.photo_video_editor_work_txt_color));*/

        archiveListView = (HListView)findViewById(R.id.photoArchiveListView);
        mAdapter = new com.videophotofilter.android.com.MediaArchiveListAdapter(this ,ratio);
        mAdapter.setOnArchiveItemClickListener(this);
        //mAdapter.setListItems(VideoFilterApplication.photoArchiveArray);
        archiveListView.setAdapter(mAdapter);

        progressHUD = ProgressHUD.createProgressDialog(VideoSetActivity.this, "Downloading...", false, false, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(progressHUD != null && progressHUD.isShowing())
                    progressHUD.dismiss();
            }
        });
    }

    private void getArchiveItems()
    {
        ArrayList<ArchiveMediaItem> archiveList = new ArrayList<ArchiveMediaItem>();
        if(mAdapter == null) {
            mAdapter = new com.videophotofilter.android.com.MediaArchiveListAdapter(this , ratio);
            mAdapter.setOnArchiveItemClickListener(this);
            archiveListView.setAdapter(mAdapter);
        }
        mAdapter.setListItems(archiveList);
        mAdapter.notifyDataSetChanged();
        if(type.equals("personalInfo")) {
            if (group_type == 1 || group_type == 2) {
                TradeCard.getArchiveVideoHistory(group_type,
                        1, //default page num
                        20, //conut per page
                        new ResponseCallBack<JSONObject>() {
                            @Override
                            public void onCompleted(JsonResponse<JSONObject> response) {
                                if (response.isSuccess()) {
                                    JSONObject resObj = response.getData();
                                    try {
                                        JSONArray dataArray = resObj.getJSONArray("data");
                                        for (int i = 0; i < dataArray.length(); i++) {
                                            JSONObject obj = dataArray.getJSONObject(i);
                                            ArchiveMediaItem archiveItem = new ArchiveMediaItem(group_type);
                                            archiveItem.archiveID = obj.optInt("id");
                                            archiveItem.mediaType = 0;

                                            String thumbImg = obj.getString("thumbnail_url");
                                            TcVideoVO videoInfo = new TcVideoVO();
                                            videoInfo.setThumbUrl(thumbImg);
                                            videoInfo.setVideo_url(obj.getString("video_url"));
                                            archiveItem.thumbImages.add(videoInfo);

                                            mAdapter.getListItems().add(archiveItem);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (mAdapter.getCount() <= 0) {
                                    //if there isn't any archive iamge, then add default image
                                    ArchiveMediaItem defaultItem = new ArchiveMediaItem(group_type);
                                    defaultItem.isDefaultItem = true;
                                    defaultItem.isNewItem = isNewEntity;
                                    mAdapter.getListItems().add(defaultItem);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        });
            }
        }
        else{
            EntityRequest.getArchiveVideoHistory(entity_Id,
                    1, //default page num
                    20, //conut per page
                    new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                JSONObject resObj = response.getData();
                                try {
                                    JSONArray dataArray = resObj.getJSONArray("data");
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        JSONObject obj = dataArray.getJSONObject(i);
                                        ArchiveMediaItem archiveItem = new ArchiveMediaItem(3);
                                        archiveItem.archiveID = obj.optInt("video_id");
                                        archiveItem.mediaType = 0;

                                        String thumbImg = obj.getString("thumbnail_url");
                                        TcVideoVO videoInfo = new TcVideoVO();
                                        videoInfo.setThumbUrl(thumbImg);
                                        videoInfo.setVideo_url(obj.getString("video_url"));
                                        archiveItem.thumbImages.add(videoInfo);
                                        mAdapter.getListItems().add(archiveItem);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (mAdapter.getCount() <= 0) {
                                //if there isn't any archive iamge, then add default image
                                ArchiveMediaItem defaultItem = new ArchiveMediaItem(3);
                                defaultItem.isDefaultItem = true;
                                defaultItem.isNewItem = isNewEntity;
                                mAdapter.getListItems().add(defaultItem);
                            }
                            mAdapter.notifyDataSetChanged();

                        }
                    });
            }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getArchiveItems();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onBtnCameraInHomeVideo(View v) {
        if(isResult) return;
        Intent intent = new Intent(VideoSetActivity.this, RecordFilterCameraActivity.class);
        intent.putExtra("typeId", type);
        intent.putExtra("isHome", group_type);
        intent.putExtra("isNewEntity", isNewEntity);
        startActivityForResult(intent, TAKE_VIDEO_FROM_CAMERA);

    }

    public void onBtnCameraRollInHomeVideo(View v) {
        if(isResult) return;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
//		intent.putExtra("return-data", true);

        this.startActivityForResult(intent, ALLOW_ACCESS_VIDEO_FROM_DIRECTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApp.getInstance().setCurrentActivity(this);

        switch (requestCode) {

            case TAKE_VIDEO_FROM_CAMERA:
                if (resultCode == RESULT_OK) {
                    isResult = true;
                    strVideoPath = data.getStringExtra("strMoviePath");

                    if (strVideoPath != null) {
                        /*display_name */
                        File videoFile = new File(strVideoPath);
                        if(videoFile.exists()) {
                            try {
                                isFromGallery = false;
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
            case ALLOW_ACCESS_VIDEO_FROM_DIRECTORY:
                if (resultCode == RESULT_OK) {
                    Uri uriRecorder = data.getData();

                    strRecorderPath = uriRecorder.getPath();
                    if(uriRecorder.toString().contains("content://") || uriRecorder.toString().contains("file://") == false)
                        strRecorderPath = getPath(uriRecorder);

                    Intent intent = new Intent(VideoSetActivity.this, RecordFilterCameraActivity.class);
                    intent.putExtra("strPathFromGallery", strRecorderPath);
                    intent.putExtra("typeId", type);
                    intent.putExtra("isHome", group_type);
                    intent.putExtra("isNewEntity", isNewEntity);
                    startActivityForResult(intent, TAKE_VIDEO_FILTER_GALLERY);
                }
                break;
            case TAKE_VIDEO_FILTER_GALLERY:
                if (resultCode == RESULT_OK) {
                    isResult = true;
                    strVideoPath = data.getStringExtra("strMoviePath");

                    if (strVideoPath != null) {
                        File videoFile = new File(strVideoPath);
                        if (videoFile.exists()) {
                            try {
                                if (Build.VERSION.SDK_INT >= 12) {
                                    //AsyncTask.class.getMethod("setDefaultExecutor", Executor.class).invoke(null, AsyncTask.SERIAL_EXECUTOR);
                                    new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, strVideoPath);
                                } else
                                    new LoadVideoFileAsyncTask().execute(strVideoPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            case TAKE_VIDEO_FROM_ARCHIVE:
                if (resultCode == RESULT_OK) {
                    isResult = true;
                    strVideoPath = data.getStringExtra("strMoviePath");

                    if (strVideoPath != null) {
                        File videoFile = new File(strVideoPath);
                        if (videoFile.exists()) {
                            try {
                                isFromGallery = true;
                                if (Build.VERSION.SDK_INT >= 12) {
                                    //AsyncTask.class.getMethod("setDefaultExecutor", Executor.class).invoke(null, AsyncTask.SERIAL_EXECUTOR);
                                    new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, strVideoPath);
                                } else
                                    new LoadVideoFileAsyncTask().execute(strVideoPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
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
            case R.id.btnSkipVideo:
            case R.id.btnPrev:
                finish();
                break;
            /*
            case R.id.btnSkipVideo:
                //goToEntityProfilePreviewScreen();
                break;
            */
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
            Intent returnIntent = new Intent();
            returnIntent.putExtra("strMoviePath", videoFilePath);
            returnIntent.putExtra("strThumbPath", result);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            /*if(type.equals("personalInfo"))
            {
                TradeCard.uploadVideo(group_type, new File(videoFilePath), new File(videoThumbFilePath), new ResponseCallBack<TcVideoVO>() {
                    @Override
                    public void onCompleted(JsonResponse<TcVideoVO> response) {
                        finish();
                    }
                });
            }
            else if(type.equals("entityInfo"))
            {
                if(this.videoThumbFilePath.equals("") == false)
                {
                    EntityRequest.uploadVideo(entity.getId(), new File(videoFilePath), new File(videoThumbFilePath), new ResponseCallBack<JSONObject>() {
                        @Override
                        public void onCompleted(JsonResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                JSONObject result = response.getData();
                                entity.setVideo(result.optString("video" , ""));

                                *//*if (isSetNewVideoInfo) {
                                    //goToEntityProfilePreviewScreen();
                                } else {
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("entity", entity);
                                    intent.putExtras(bundle);
                                    EntitySetVideoActivity.this.setResult(RESULT_OK, intent);
                                    EntitySetVideoActivity.this.finish();
                                }*//*
                            } else {
                                MyApp.getInstance().showSimpleAlertDiloag(VideoSetActivity.this, R.string.str_alert_failed_to_upload_video, null);
                            }
                        }
                    });
                }
            }*/
        }
    }

    @Override
    public void onArchiveItemClick(ArchiveMediaItem item, int position) {
        if(item != null) {
            final ArchiveMediaItem mItem = item;
            if (type.equals("personalInfo")) {
                String thumb_url = mItem.thumbImages.get(0).getThumbUrl();
                String videoFilePath = mItem.thumbImages.get(0).getVideo_url();
                new DownloadFileFromURL().execute(videoFilePath);
                /*TradeCard.selectArchiveVideoFromHistory(item.archiveID, group_type, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if(response.isSuccess()) {
                            String thumb_url = mItem.thumbImages.get(0).getThumbUrl();
                            String videoFilePath = mItem.thumbImages.get(0).getVideo_url();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("isHistory", true);
                            returnIntent.putExtra("strThumbPath", thumb_url);
                            returnIntent.putExtra("strMoviePath", videoFilePath);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                });*/
            }
            else {
                String thumb_url = mItem.thumbImages.get(0).getThumbUrl();
                String videoFilePath = mItem.thumbImages.get(0).getVideo_url();
                new DownloadFileFromURL().execute(videoFilePath);
                /*EntityRequest.selectArchiveVideo(item.archiveID, entity_Id, new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if(response.isSuccess()) {
                            String thumb_url = mItem.thumbImages.get(0).getThumbUrl();
                            String videoFilePath = mItem.thumbImages.get(0).getVideo_url();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("isHistory", true);
                            returnIntent.putExtra("strThumbPath", thumb_url);
                            returnIntent.putExtra("strMoviePath", videoFilePath);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                });*/
            }
        }
    }

    @Override
    public void onArchiveDeleteClick(ArchiveMediaItem item, int position) {
        if(item != null)
        {
            final ArchiveMediaItem mItem = item;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm");
            builder.setMessage("Do you want to delete video from the archive?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO
                    if(type.equals("personalInfo")) {
                        TradeCard.deleteArchiveVideo(group_type, mItem.archiveID, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                    getArchiveItems();
                            }
                        });
                    }
                    else {
                        EntityRequest.deleteArchiveVideo(mItem.archiveID, entity_Id, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                    getArchiveItems();
                            }
                        });
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                output = new FileOutputStream(mSaveDirectoryPath + "/download.mp4");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            progressHUD.cancel();
            Intent intent = new Intent(VideoSetActivity.this, RecordFilterCameraActivity.class);
            intent.putExtra("strPathFromGallery", mSaveDirectoryPath +"/download.mp4");
            intent.putExtra("typeId", type);
            intent.putExtra("isHome", group_type);
            intent.putExtra("isNewEntity", isNewEntity);
            startActivityForResult(intent, TAKE_VIDEO_FROM_ARCHIVE);
        }

    }
}
