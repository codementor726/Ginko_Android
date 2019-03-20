package com.ginko.setup;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ginko.api.request.TradeCard;
import com.ginko.common.RuntimeContext;
import com.ginko.common.Uitils;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.vo.TcVideoVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HomeWorkSetVideoActivity extends MyBaseActivity implements View.OnClickListener{
	private static final int ALLOW_ACCESS_VIDEO = 1337;
	private static final int REQUEST_CODE_TAKE_VIDEO = 2;

    private final String TYPE_PARAM = "type";
    private final String USER_INFO_PARAM = "userInfo";

    private final String GROUP_TYPE_HOME = "home";
    private final String GROUP_TYPE_WORK = "work";

    private final int HOME_GROUP = 1;
    private final int WORK_GROUP = 2;

    private int groupType = HOME_GROUP;

    /* UI Elments */
    private ImageButton btnPrev;
    private TextView txtTitle , txtDescription;
    private ImageButton btnCameraVideo , btnCameraRollVideo;
    private ImageView btnSkip;

    /* Variables */
    private String type;
    private UserWholeProfileVO userInfo;

    private UserUpdateVO groupInfo;

	private String strVideoPath = "";
	private String strRecorderPath = "";

    private boolean isSetNewVideoInfo = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homework_info_set_video);

        if(savedInstanceState!= null)
        {
            isSetNewVideoInfo = savedInstanceState.getBoolean("isSetNewVideo");
            type = savedInstanceState.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        }
        else {
            //get intent
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            isSetNewVideoInfo = bundle.getBoolean("isSetNewVideo" , false);
            type = bundle.getString(TYPE_PARAM, GROUP_TYPE_HOME);
            userInfo = (UserWholeProfileVO) bundle.getSerializable(USER_INFO_PARAM);
        }

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        groupInfo = userInfo.getGroupInfoByGroupType(groupType);

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
        if(groupType == HOME_GROUP)
        {
            txtTitle.setText(getResources().getString(R.string.home_info));
            txtColor = getResources().getColor(R.color.photo_video_editor_home_txt_color);
            btnCameraVideo.setImageResource(R.drawable.part_a_btn_video_green);
            btnCameraRollVideo.setImageResource(R.drawable.part_a_btn_cameraroll_green);
            txtDescription.setText(getResources().getString(R.string.home_content_description_video));
        }
        else if(groupType == WORK_GROUP)
        {
            txtTitle.setText(getResources().getString(R.string.work_info));
            txtColor = getResources().getColor(R.color.photo_video_editor_work_txt_color);
            btnCameraVideo.setImageResource(R.drawable.part_a_btn_video_purple);
            btnCameraRollVideo.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
            txtDescription.setText(getResources().getString(R.string.work_content_description_video));
        }

        txtTitle.setTextColor(txtColor);
        txtDescription.setTextColor(txtColor);
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
        outState.putString(TYPE_PARAM , type);
        outState.putSerializable(USER_INFO_PARAM , userInfo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        isSetNewVideoInfo = savedInstanceState.getBoolean("isSetNewVideo" ,false);
        userInfo = (UserWholeProfileVO) savedInstanceState.getSerializable(USER_INFO_PARAM);
        type = savedInstanceState.getString(TYPE_PARAM);

        if(type.equalsIgnoreCase(GROUP_TYPE_HOME))
            groupType = HOME_GROUP;
        else
            groupType = WORK_GROUP;

        groupInfo = userInfo.getGroupInfoByGroupType(groupType);

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

    private void goToGroupInfoPreviewScreen()
    {
        Intent homeWorkInfoPreviewIntent = new Intent(HomeWorkSetVideoActivity.this , HomeWorkInfoPreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(TYPE_PARAM , type);
        bundle.putSerializable(USER_INFO_PARAM , userInfo);
        homeWorkInfoPreviewIntent.putExtras(bundle);
        startActivity(homeWorkInfoPreviewIntent);
        finish();
    }


	@TargetApi(Build.VERSION_CODES.KITKAT)
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
                            if (Build.VERSION.SDK_INT >= 12)
                                new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strVideoPath);
                            else
                                new LoadVideoFileAsyncTask().execute(strVideoPath);
                        }catch (Exception e)
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
                if(Uitils.getOsVersion() >=19) {
                    try {
                        String wholeID = DocumentsContract.getDocumentId(uriRecorder);

                        // Split at colon, use second item in the array
                        String id = wholeID.split(":")[1];

                        String[] column = {MediaStore.Images.Media.DATA};

                        // where id is equal to
                        String sel = MediaStore.Images.Media._ID + "=?";

                        Cursor cursor = getContentResolver().
                                query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                        column, sel, new String[]{id}, null);

                        String strRecorderPath = "";

                        int columnIndex = cursor.getColumnIndex(column[0]);

                        if (cursor.moveToFirst()) {
                            strRecorderPath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    File videoFile = new File(strRecorderPath);
                    if (videoFile.exists()) {
                        try {
                            if (Build.VERSION.SDK_INT >= 12)
                                new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strRecorderPath);
                            else
                                new LoadVideoFileAsyncTask().execute(strRecorderPath);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Cursor cursor = this.getContentResolver().query(uriRecorder, null, null, null, null);
                    if (cursor.moveToNext()) {
                        strRecorderPath = cursor.getString(cursor.getColumnIndex("_data"));

                        System.out.println("---- Video Path = " + strRecorderPath + "------");
                        File videoFile = new File(strRecorderPath);
                        if (videoFile.exists()) {
                            try {
                                if (Build.VERSION.SDK_INT >= 12)
                                    new LoadVideoFileAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strRecorderPath);
                                else
                                    new LoadVideoFileAsyncTask().execute(strRecorderPath);
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
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
                goToGroupInfoPreviewScreen();
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
                TradeCard.uploadVideo(groupType , new File(videoFilePath) , new File(videoThumbFilePath) , new ResponseCallBack<TcVideoVO>() {
                    @Override
                    public void onCompleted(JsonResponse<TcVideoVO> response) {
                        if(response.isSuccess())
                        {
                            TcVideoVO video = response.getData();
                            groupInfo.setVideo(video);
                            if(isSetNewVideoInfo) {
                                goToGroupInfoPreviewScreen();
                            }
                            else
                            {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("userInfo" , userInfo);
                                intent.putExtras(bundle);
                                HomeWorkSetVideoActivity.this.setResult(RESULT_OK , intent);
                                HomeWorkSetVideoActivity.this.finish();
                            }
                        }
                        else
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(HomeWorkSetVideoActivity.this , R.string.str_alert_failed_to_upload_video, null);
                        }
                    }
                });
            }
        }

    }
}
