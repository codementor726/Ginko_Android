package com.videophotofilter.android.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ginko.activity.entity.EntityInfoInputActivity;
import com.ginko.api.request.EntityRequest;
import com.ginko.api.request.TradeCard;
import com.ginko.common.Uitils;
import com.ginko.context.ConstValues;
import com.ginko.data.JsonResponse;
import com.ginko.data.ResponseCallBack;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.MyBaseActivity;
import com.ginko.ginko.R;
import com.ginko.setup.HomeWorkAddInfoActivity;
import com.ginko.setup.HomeWorkEditProfileActivity;
import com.ginko.setup.HomeWorkInfoPreviewActivity;
import com.ginko.vo.EntityImageVO;
import com.ginko.vo.EntityMessageVO;
import com.ginko.vo.EntityVO;
import com.ginko.vo.TcImageVO;
import com.ginko.vo.UserUpdateVO;
import com.ginko.vo.UserWholeProfileVO;
import com.sz.util.json.JsonConvertException;
import com.sz.util.json.JsonConverter;
import com.videophotofilter.library.android.com.ImageUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import customviews.library.widget.AdapterView;
import customviews.library.widget.AdapterView.OnItemClickListener;
import customviews.library.widget.HListView;


public class TradeCardPhotoEditorSetActivity extends MyBaseActivity implements OnClickListener,
        MediaArchiveListAdapter.ArchiveItemClickListener
{
    private final int NEW_WORK_INFO = 1000;

    private RelativeLayout bodyLayout;
    private TextView txtTitle , btnSkip, txtContentDiscription , txtChooseOrTakePhoto , txtSelectBackgroundColor,
                                txtBackgroundImageArchive , txtSkipForTextOnly;
	private ImageView btnClose , btnSkipWorkPhoto;
	private ImageView btnFromCamera , btnFromCameraRoll;
	
	private ImageView imgBlack , imgGray , imgSilver , imgWhite , imgMore;

	private HListView archiveListView;


    /* Variables */

    private com.videophotofilter.android.com.MediaArchiveListAdapter mAdapter;

    private boolean isSetNewPhotoInfo = false;//if true , then add new enitity or home photo info ,
                                              // else its false then add new photo into already existing entity or home info

    private boolean isAddNewWorkProfile = false;

    private int nTradeCardType = ConstValues.ENTITY_PHOTO_EDITOR;//trade card type , entity , Home or Work

    private EntityVO entity = null;

    private UserWholeProfileVO userInfo = null;


    private final int REQUEST_TAKE_PHOTO_FROM_CAMERA = 1;
    private final int REQUEST_PICK_PHOTO_FROM_GALLERY = 2;
    private final int UPDATE_ARCHIVE_PHOTO = 3;
    private final int FILTERED_PHOTO = 4;

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

    private float ratio = 1.0f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tradecard_set_photoeditor);

        if(savedInstanceState != null)
        {
            isSetNewPhotoInfo = savedInstanceState.getBoolean("isSetNewPhotoInfo", true);
            isAddNewWorkProfile = savedInstanceState.getBoolean("isAddNewWorkProfile" , false);
            nTradeCardType = savedInstanceState.getInt("tradecardType", ConstValues.HOME_PHOTO_EDITOR);
            try {
                this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
            }catch(Exception e)
            {
                this.entity = null;
                e.printStackTrace();
            }

            try {
                userInfo = (UserWholeProfileVO)savedInstanceState.getSerializable("userInfo");
            }catch(Exception e)
            {
                this.userInfo = null;
                e.printStackTrace();
            }
        }
        else
        {
            Intent intent = getIntent();

            isAddNewWorkProfile = intent.getBooleanExtra("isAddNewWorkProfile" , false);
            isSetNewPhotoInfo = intent.getBooleanExtra("isSetNewPhotoInfo" , true);
            nTradeCardType = intent.getIntExtra("tradecardType" , ConstValues.HOME_PHOTO_EDITOR);
            try {
                this.entity = (EntityVO) intent.getExtras().getSerializable("entity");
            }catch(Exception e)
            {
                this.entity = null;
                e.printStackTrace();
            }

            try {
                userInfo = (UserWholeProfileVO)intent.getExtras().getSerializable("userInfo");
            }catch(Exception e)
            {
                this.userInfo = null;
                e.printStackTrace();
            }
        }
        ratio = Uitils.getScreenRatioViaIPhone(this);

		getUIObjects();
	}

    @Override
	protected void getUIObjects()
	{
        super.getUIObjects();
        bodyLayout = (RelativeLayout)findViewById(R.id.body_layout);
        if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
        {
            bodyLayout.setBackgroundResource(R.drawable.img_leaf);
        }
        else
        {
            bodyLayout.setBackgroundResource(R.drawable.leafbgforblank);
        }

        txtTitle = (TextView)findViewById(R.id.txtTitle);
        btnSkip = (TextView)findViewById(R.id.txtBtnSkip); btnSkip.setOnClickListener(this);
        btnClose = (ImageView)findViewById(R.id.btnClose); btnClose.setOnClickListener(this);
        btnSkipWorkPhoto = (ImageView)findViewById(R.id.btnSkipWorkPhoto); btnSkipWorkPhoto.setOnClickListener(this);
        if(isSetNewPhotoInfo)
        {
            btnSkip.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.GONE);
            if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
            {
                btnSkipWorkPhoto.setVisibility(View.VISIBLE);
            }
            else
            {
                btnSkipWorkPhoto.setVisibility(View.GONE);
            }
        }
        else
        {
            btnSkip.setVisibility(View.GONE);
            btnClose.setVisibility(View.VISIBLE);
            btnSkipWorkPhoto.setVisibility(View.GONE);
        }

		btnFromCamera = (ImageView)findViewById(R.id.btnFromCamera);btnFromCamera.setOnClickListener(this);
		btnFromCameraRoll = (ImageView)findViewById(R.id.btnFromCameraroll);btnFromCameraRoll.setOnClickListener(this);

        //textviews
        txtContentDiscription = (TextView)findViewById(R.id.txtContentDiscription);
        txtChooseOrTakePhoto = (TextView)findViewById(R.id.txtChooseOrTakePhoto);
        txtSelectBackgroundColor = (TextView)findViewById(R.id.txtSelectBackgroundColor);
        txtBackgroundImageArchive = (TextView)findViewById(R.id.txtBackgroundImageArchive);
        txtSkipForTextOnly = (TextView)findViewById(R.id.txtSkipForTextOnly);

        if(isSetNewPhotoInfo)
        {
            txtBackgroundImageArchive.setVisibility(View.GONE);
            txtSkipForTextOnly.setVisibility(View.VISIBLE);
        }
        else
        {
            txtBackgroundImageArchive.setVisibility(View.VISIBLE);
            txtSkipForTextOnly.setVisibility(View.GONE);
        }

        int txtColor = 0;
        if(nTradeCardType == ConstValues.ENTITY_PHOTO_EDITOR)
        {
            txtTitle.setText(getResources().getString(R.string.entity_info));
            if(isSetNewPhotoInfo)
                txtContentDiscription.setText(getResources().getString(R.string.entity_content_description_photo));
            else
                txtContentDiscription.setText(getResources().getString(R.string.entity_content_description_photo_edit));

            txtColor = getResources().getColor(R.color.photo_video_editor_entity_txt_color);
            btnFromCamera.setImageResource(R.drawable.part_a_btn_camera_purple);
            btnFromCameraRoll.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
        }
        else if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
        {
            txtTitle.setText(getResources().getString(R.string.home_info));
            if(isSetNewPhotoInfo)
                txtContentDiscription.setText(getResources().getString(R.string.home_content_description_photo));
            else
                txtContentDiscription.setText(getResources().getString(R.string.home_content_description_photo_edit));

            txtColor = getResources().getColor(R.color.photo_video_editor_home_txt_color);
            btnFromCamera.setImageResource(R.drawable.part_a_btn_camera_green);
            btnFromCameraRoll.setImageResource(R.drawable.part_a_btn_cameraroll_green);
        }
        else if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
        {
            txtTitle.setText(getResources().getString(R.string.work_info));
            if(isSetNewPhotoInfo)
                txtContentDiscription.setText(getResources().getString(R.string.work_content_description_photo));
            else
                txtContentDiscription.setText(getResources().getString(R.string.work_content_description_photo_edit));

            txtColor = getResources().getColor(R.color.photo_video_editor_work_txt_color);
            btnFromCamera.setImageResource(R.drawable.part_a_btn_camera_purple);
            btnFromCameraRoll.setImageResource(R.drawable.part_a_btn_cameraroll_purple);
        }

        txtContentDiscription.setTextColor(txtColor);
        txtChooseOrTakePhoto.setTextColor(txtColor);
        txtSelectBackgroundColor.setTextColor(txtColor);
        txtBackgroundImageArchive.setTextColor(txtColor);
        txtSkipForTextOnly.setTextColor(txtColor);

		//color buttons
		imgBlack = (ImageView)findViewById(R.id.imgBlack);imgBlack.setOnClickListener(this);
		imgGray = (ImageView)findViewById(R.id.imgGray);imgGray.setOnClickListener(this);
		imgSilver = (ImageView)findViewById(R.id.imgSilver);imgSilver.setOnClickListener(this);
		imgWhite = (ImageView)findViewById(R.id.imgWhite);imgWhite.setOnClickListener(this);
		imgMore = (ImageView)findViewById(R.id.imgMore);imgMore.setOnClickListener(this);

        if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
        {
            imgMore.setImageResource(R.drawable.bc_more_green);
        }
        else
        {
            imgMore.setImageResource(R.drawable.bc_more_purple);
        }


        if(!isSetNewPhotoInfo) {
            archiveListView = (HListView) findViewById(R.id.photoArchiveListView);
            mAdapter = new com.videophotofilter.android.com.MediaArchiveListAdapter(this ,ratio);
            mAdapter.setOnArchiveItemClickListener(this);
            //mAdapter.setListItems(VideoFilterApplication.photoArchiveArray);
            archiveListView.setAdapter(mAdapter);
        }
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isSetNewPhotoInfo", isSetNewPhotoInfo);
        outState.putInt("tradecardType", nTradeCardType);
        outState.putBoolean("isAddNewWorkProfile" , this.isAddNewWorkProfile);
        if (this.entity != null) {
            outState.putSerializable("entity", entity);
        }
        if (this.userInfo != null) {
            outState.putSerializable("userInfo", userInfo);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        isAddNewWorkProfile = savedInstanceState.getBoolean("isAddNewWorkProfile" , false);
        isSetNewPhotoInfo = savedInstanceState.getBoolean("isSetNewPhotoInfo", true);
        nTradeCardType = savedInstanceState.getInt("tradecardType", ConstValues.HOME_PHOTO_EDITOR);
        try {
            this.entity = (EntityVO) savedInstanceState.getSerializable("entity");
        }catch(Exception e)
        {
            this.entity = null;
            e.printStackTrace();
        }

        try {
            userInfo = (UserWholeProfileVO)savedInstanceState.getSerializable("userInfo");
        }catch(Exception e)
        {
            this.userInfo = null;
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if(isSetNewPhotoInfo) return;
        super.onBackPressed();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
        //skip work photo
        case R.id.btnSkipWorkPhoto:
            if(isAddNewWorkProfile) {
                finish();
            }
            else
            {
                Intent homeWorkProfileEditIntent = new Intent(TradeCardPhotoEditorSetActivity.this, HomeWorkInfoPreviewActivity.class);
                Bundle bundle2 = new Bundle();
                //bundle2.putBoolean("fromPreviewActivity", false);
                bundle2.putBoolean("isWorkSkipped", true);
                bundle2.putString("type", "home");
                bundle2.putSerializable("userInfo", userInfo);
                homeWorkProfileEditIntent.putExtras(bundle2);
                startActivity(homeWorkProfileEditIntent);
                finish();
                /*Intent homeWorkProfileEditIntent = new Intent(TradeCardPhotoEditorSetActivity.this, HomeWorkEditProfileActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putBoolean("fromPreviewActivity", false);
                bundle2.putBoolean("isWorkSkipped", true);
                bundle2.putString("type", "home");
                bundle2.putSerializable("userInfo", userInfo);
                homeWorkProfileEditIntent.putExtras(bundle2);
                startActivity(homeWorkProfileEditIntent);
                finish();*/

            }
            break;
        //skip button
        case R.id.txtBtnSkip:
            if(nTradeCardType == ConstValues.ENTITY_PHOTO_EDITOR) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("entity", this.entity);
                bundle.putSerializable("isNewEntity" , true);
                Intent intent = new Intent(this, EntityInfoInputActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
            else if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
            {
                //if press skip , go to video editor
                if(userInfo == null) return;

                Intent homeInfoAddIntent = new Intent(this , HomeWorkAddInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "home");
                bundle.putSerializable("userInfo", userInfo);
                homeInfoAddIntent.putExtras(bundle);
                startActivity(homeInfoAddIntent);
                finish();
            }
            else if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
            {
                if(userInfo == null) return;

                Intent homeInfoAddIntent = new Intent(this , HomeWorkAddInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "work");
                bundle.putSerializable("userInfo", userInfo);
                if(this.isAddNewWorkProfile) {
                    bundle.putBoolean("isAddNewWorkProfile", this.isAddNewWorkProfile);
                    homeInfoAddIntent.putExtras(bundle);
                    startActivityForResult(homeInfoAddIntent, NEW_WORK_INFO);
                }
                else {
                    homeInfoAddIntent.putExtras(bundle);
                    startActivity(homeInfoAddIntent);
                    finish();
                }
            }
            break;

		case R.id.btnClose:
			finish();
			break;
		case R.id.btnFromCamera:
            if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
                MyApp.currentTakePhotoTitle = getResources().getString(R.string.home_info);
            else if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
                MyApp.currentTakePhotoTitle = getResources().getString(R.string.work_info);
            else
                MyApp.currentTakePhotoTitle = getResources().getString(R.string.entity_info);
			Intent takePhotoIntent = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.TakePhotoActivity.class);
			startActivityForResult(takePhotoIntent , REQUEST_TAKE_PHOTO_FROM_CAMERA);
			break;
		case R.id.btnFromCameraroll:
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
	        photoPickerIntent.setType("image/*");
	        startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO_FROM_GALLERY);
			break;
			
		//color buttons
		case R.id.imgBlack:
			Intent intent1 = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.PhotoFilterActivity.class);
			intent1.putExtra("isResource", true);
			intent1.putExtra("path_or_name", "bc_black");
            intent1.putExtra("tradecardType", this.nTradeCardType);
			startActivityForResult(intent1, FILTERED_PHOTO);
			
			break;
		case R.id.imgGray:
			Intent intent2 = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.PhotoFilterActivity.class);
			intent2.putExtra("isResource", true);
			intent2.putExtra("path_or_name", "bc_grey");
            intent2.putExtra("tradecardType", this.nTradeCardType);
            startActivityForResult(intent2, FILTERED_PHOTO);
			
			break;
		case R.id.imgSilver:
			Intent intent3 = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.PhotoFilterActivity.class);
			intent3.putExtra("isResource", true);
			intent3.putExtra("path_or_name", "bc_silver");
            intent3.putExtra("tradecardType", this.nTradeCardType);
            startActivityForResult(intent3, FILTERED_PHOTO);
			break;
		case R.id.imgWhite:
			Intent intent4 = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.PhotoFilterActivity.class);
			intent4.putExtra("isResource", true);
			intent4.putExtra("path_or_name", "bc_white");
            intent4.putExtra("tradecardType", this.nTradeCardType);
            startActivityForResult(intent4, FILTERED_PHOTO);
			break;
		case R.id.imgMore:
            if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
                MyApp.currentTakePhotoTitle = getResources().getString(R.string.home_info);
            else if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
                MyApp.currentTakePhotoTitle = getResources().getString(R.string.work_info);
            else
                MyApp.currentTakePhotoTitle = getResources().getString(R.string.entity_info);
			Intent selectMoreColorIntent = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.SelectMoreColorActivity.class);
            selectMoreColorIntent.putExtra("tradecardType" , this.nTradeCardType);
            startActivityForResult(selectMoreColorIntent, FILTERED_PHOTO);
			break;
		}
	}
	
	@Override
	public void onResume()
	{
		//if archive photo media is updated ,then refresh listview
		/*if(VideoFilterApplication.isMediaUpdated)
		{
			mAdapter.setListItems(VideoFilterApplication.photoArchiveArray);
			mAdapter.notifyDataSetChanged();
			
			VideoFilterApplication.isMediaUpdated = false;
		}*/
		super.onResume();
        if(!isSetNewPhotoInfo)
        {
            getArchiveItems();
        }
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
        if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR || nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
        {
            int type = 1;//home
            if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
                type = 1;//home
            else
                type = 2;//work
            TradeCard.getArchiveImages(type ,
                    1 , //default page num
                    20 , //conut per page
                    new ResponseCallBack<JSONObject>() {
                @Override
                public void onCompleted(JsonResponse<JSONObject> response) {
                    if(response.isSuccess())
                    {
                        JSONObject resObj = response.getData();
                        try {
                            JSONArray dataArray = resObj.getJSONArray("data");
                            for(int i=0;i<dataArray.length();i++) {
                                JSONObject obj = dataArray.getJSONObject(i);
                                ArchiveMediaItem archiveItem = new ArchiveMediaItem(nTradeCardType);
                                archiveItem.archiveID = obj.optInt("archive_id");

                                JSONArray images = obj.getJSONArray("images");
                                for(int j=0;j<images.length();j++)
                                {
                                    try{
                                        TcImageVO img = JsonConverter.json2Object(images.getJSONObject(j) ,  (Class<TcImageVO>) TcImageVO.class);
                                        archiveItem.userImages.add(img);
                                    } catch (JsonConvertException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                mAdapter.getListItems().add(archiveItem);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(mAdapter.getCount() <= 0)
                    {
                        //if there isn't any archive iamge, then add default image
                        ArchiveMediaItem defaultItem = new ArchiveMediaItem(nTradeCardType);
                        defaultItem.isDefaultItem = true;
                        mAdapter.getListItems().add(defaultItem);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
        else if(nTradeCardType == ConstValues.ENTITY_PHOTO_EDITOR && this.entity != null)
        {
            EntityRequest.getArchiveImages(this.entity.getId() ,
                1 , //default page num
                20 , //conut per page
                new ResponseCallBack<JSONObject>() {
                    @Override
                    public void onCompleted(JsonResponse<JSONObject> response) {
                        if(response.isSuccess())
                        {
                            JSONObject resObj = response.getData();
                            try {
                                JSONArray dataArray = resObj.getJSONArray("data");
                                for(int i=0;i<dataArray.length();i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);
                                    ArchiveMediaItem archiveItem = new ArchiveMediaItem(ConstValues.ENTITY_PHOTO_EDITOR);
                                    archiveItem.archiveID = obj.optInt("archive_id");

                                    JSONArray images = obj.getJSONArray("images");
                                    for(int j=0;j<images.length();j++)
                                    {
                                        try{
                                            EntityImageVO img = JsonConverter.json2Object(images.getJSONObject(j) ,  (Class<EntityImageVO>) EntityImageVO.class);
                                            archiveItem.entityImages.add(img);
                                        } catch (JsonConvertException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    mAdapter.getListItems().add(archiveItem);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(mAdapter.getCount() <= 0)
                        {
                            //if there isn't any archive iamge, then add default image
                            ArchiveMediaItem defaultItem = new ArchiveMediaItem(nTradeCardType);
                            defaultItem.isDefaultItem = true;
                            mAdapter.getListItems().add(defaultItem);
                        }
                        mAdapter.notifyDataSetChanged();

                    }
            });
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        MyApp.getInstance().setCurrentActivity(this);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO_FROM_CAMERA:
                if(resultCode == RESULT_OK)
                {
                    String photoPath = data.getExtras().getString("photoPath");
                    System.out.println("---photo path = "+photoPath+"---");
                    System.out.println("---photo path= " + photoPath + "-------");
                    Intent photoFilterIntent = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.PhotoFilterActivity.class);
                    photoFilterIntent.putExtra("isResource", false);
                    photoFilterIntent.putExtra("path_or_name", photoPath);
                    photoFilterIntent.putExtra("isFromCamera" , true);
                    photoFilterIntent.putExtra("tradecardType" , this.nTradeCardType);
                    startActivityForResult(photoFilterIntent, FILTERED_PHOTO);
                }

                break;
            case REQUEST_PICK_PHOTO_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    String photoPath = ImageUtil.getRealPathFromURI(this, data.getData(), ImageUtil.MEDIA_TYPE_IMAGE);
                    System.out.println("---photo path= "+photoPath+"-------");
                    Intent photoFilterIntent = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.PhotoFilterActivity.class);
                    photoFilterIntent.putExtra("isResource", false);
                    photoFilterIntent.putExtra("path_or_name", photoPath);
                    photoFilterIntent.putExtra("tradecardType" , this.nTradeCardType);
                    startActivityForResult(photoFilterIntent, FILTERED_PHOTO);
                } else {
                    //finish();
                }
                break;

            case UPDATE_ARCHIVE_PHOTO:
                if(resultCode == RESULT_OK)
                {
                    getArchiveItems();
                }
                break;

            case FILTERED_PHOTO:
                if(resultCode == RESULT_OK && data!=null)
                {
                    String backgroundPhotoPath = data.getStringExtra("backgroundPhotoPath");
                    String foregroundPhotoPath = data.getStringExtra("foregroundPhotoPath");
                    float ratio = Uitils.getScreenRatioViaIPhone(TradeCardPhotoEditorSetActivity.this);

                    int foregroundLeft = (int)(data.getIntExtra("foregroundLeft" , 0)  / (ratio * 1.0));
                    int foregroundTop = (int)(data.getIntExtra("foregroundTop" , 0) / (ratio * 1.0));
                    int foregroundWidth = (int)(data.getIntExtra("foregroundWidth" , 0) / (ratio * 1.0));
                    int foregroundHeight = (int)(data.getIntExtra("foregroundHeight" , 0) / (ratio * 1.0));

                    System.out.println("----BackgroundPhotoPath---" + backgroundPhotoPath);
                    System.out.println("----ForegroundPhotoPath---" + foregroundPhotoPath);
                    System.out.println("----(" + foregroundLeft+","+foregroundTop+") - ("+foregroundWidth+","+foregroundHeight+")----");

                    if (nTradeCardType == ConstValues.HOME_PHOTO_EDITOR) {
                        //go to home info add screen
                        File backgroundPhotoFile = new File(backgroundPhotoPath);
                        File foregroundPhotoFile = new File(foregroundPhotoPath);
                        if(backgroundPhotoFile.exists() && foregroundPhotoFile.exists() ) {
                            uploadBackAndForegroundPhotoFile(1, backgroundPhotoPath, foregroundPhotoPath, foregroundLeft, foregroundTop, foregroundWidth, foregroundHeight);
                        }
                        else if(backgroundPhotoFile.exists() && !foregroundPhotoFile.exists())
                        {
                            uploadBackgroundPhotoFile(1 , backgroundPhotoFile);
                        }
                    }
                    else if (nTradeCardType == ConstValues.WORK_PHOTO_EDITOR) {
                        //go to work info
                        File backgroundPhotoFile = new File(backgroundPhotoPath);
                        File foregroundPhotoFile = new File(foregroundPhotoPath);
                        if(backgroundPhotoFile.exists() && foregroundPhotoFile.exists() ) {
                            uploadBackAndForegroundPhotoFile(2, backgroundPhotoPath, foregroundPhotoPath, foregroundLeft, foregroundTop, foregroundWidth, foregroundHeight);
                        }
                        else if(backgroundPhotoFile.exists() && !foregroundPhotoFile.exists())
                        {
                            uploadBackgroundPhotoFile(2 , backgroundPhotoFile);
                        }
                    }
                    else if(nTradeCardType == ConstValues.ENTITY_PHOTO_EDITOR && entity!=null)
                    {
                        uploadEntityBackAndForegroundPhotoFile(entity.getId() , backgroundPhotoPath , foregroundPhotoPath , foregroundLeft , foregroundTop, foregroundWidth , foregroundHeight);
                    }

                }
                else
                {
                    if(data!=null)
                    {
                        //this will notify that the filtered photo was from camera or gallery , if photo was from camera , then this takes the screen to the camera screen
                        boolean isFilterPhotoFromCameraCapture = data.getBooleanExtra("isFromCamera" , false);
                        if(isFilterPhotoFromCameraCapture)
                        {
                            if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
                                MyApp.currentTakePhotoTitle = getResources().getString(R.string.home_info);
                            else if(nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
                                MyApp.currentTakePhotoTitle = getResources().getString(R.string.work_info);
                            else
                                MyApp.currentTakePhotoTitle = getResources().getString(R.string.entity_info);

                            Intent takePhotoIntent = new Intent(TradeCardPhotoEditorSetActivity.this , com.videophotofilter.android.com.TakePhotoActivity.class);
                            startActivityForResult(takePhotoIntent , REQUEST_TAKE_PHOTO_FROM_CAMERA);
                        }
                    }
                }
                break;

            case NEW_WORK_INFO:
                if(resultCode == RESULT_OK && data!=null)
                {
                    userInfo = (UserWholeProfileVO)data.getSerializableExtra("myInfo");
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("myInfo" , userInfo);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK , intent);
                    finish();
                }
                else
                {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void uploadBackAndForegroundPhotoFile(int groupType , String backgroundPhotoPath , String foregroundPhotoPath
                                                  , int _foregroundLeft , int _foregroundTop , int _foregroundWidth , int _foregroundHeight)

    {
        final int type = groupType;
        final int foregroundLeft = _foregroundLeft;
        final int foregroundTop = _foregroundTop;
        final int foregroundWidth = _foregroundWidth;
        final int foregroundHeight = _foregroundHeight;

        userInfo.getGroupInfoByGroupType(groupType).setImages(new ArrayList<TcImageVO>());
        File backgroundPhotoFile = new File(backgroundPhotoPath);
        File foregroundPhotoFile = new File(foregroundPhotoPath);
        if(!foregroundPhotoFile.exists())
            foregroundPhotoFile = null;
        if(backgroundPhotoFile.exists()) {
            TradeCard.putMultipleImages(groupType, foregroundPhotoFile, backgroundPhotoFile, new ResponseCallBack<List<JSONObject>>(){
                @Override
                public void onCompleted(JsonResponse<List<JSONObject>> response) {
                    if (response.isSuccess()) {

                        List<JSONObject> imageArray = response.getData();
                        List<TcImageVO> images = new ArrayList<TcImageVO>();
                        for(int i=0;i<imageArray.size();i++)
                        {
                            try {
                                images.add(JsonConverter.json2Object(imageArray.get(i) , TcImageVO.class));
                            } catch (JsonConvertException e) {
                                e.printStackTrace();
                            }
                        }
                        for(int i=0;i<images.size();i++)
                        {
                            if(images.get(i).getZIndex() == 1)//foreground photo
                            {
                                //set position
                                images.get(i).setWidth(Float.valueOf((float) foregroundWidth));
                                images.get(i).setHeight(Float.valueOf((float) foregroundHeight));
                                images.get(i).setTop(Float.valueOf((float) foregroundTop));
                                images.get(i).setLeft(Float.valueOf((float) foregroundLeft));
                                break;
                            }
                        }
                        userInfo.getGroupInfoByGroupType(type).setImages(images);

                        if (isSetNewPhotoInfo) {
                            Intent homeInfoAddIntent = new Intent(TradeCardPhotoEditorSetActivity.this, HomeWorkAddInfoActivity.class);
                            Bundle bundle = new Bundle();
                            if (nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
                                bundle.putString("type", "home");
                            else if (nTradeCardType == ConstValues.WORK_PHOTO_EDITOR)
                                bundle.putString("type", "work");
                            bundle.putSerializable("userInfo", userInfo);
                            if(isAddNewWorkProfile) {
                                bundle.putBoolean("isAddNewWorkProfile", true);
                                homeInfoAddIntent.putExtras(bundle);
                                startActivityForResult(homeInfoAddIntent, NEW_WORK_INFO);
                            }
                            else {
                                homeInfoAddIntent.putExtras(bundle);
                                TradeCardPhotoEditorSetActivity.this.startActivity(homeInfoAddIntent);
                                TradeCardPhotoEditorSetActivity.this.finish();
                            }


                        } else {
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("userInfo", userInfo);
                            intent.putExtras(bundle);

                            TradeCardPhotoEditorSetActivity.this.setResult(RESULT_OK, intent);
                            TradeCardPhotoEditorSetActivity.this.finish();
                        }

                    } else {
                        MyApp.getInstance().showSimpleAlertDiloag(TradeCardPhotoEditorSetActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                    }

                }
            });
        }
    }

    private void uploadBackgroundPhotoFile(int groupType , File backgroundPhotoFile)
    {
        final int type = groupType;
        //upload foreground photo file

        TradeCard.putMultipleImages(type
                , null
                , backgroundPhotoFile ,//top
                new ResponseCallBack<List<JSONObject>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<JSONObject>> response) {
                        if (response.isSuccess()) {

                            List<JSONObject> imageArray = response.getData();
                            List<TcImageVO> images = new ArrayList<TcImageVO>();
                            for(int i=0;i<imageArray.size();i++)
                            {
                                try {
                                    images.add(JsonConverter.json2Object(imageArray.get(i) , TcImageVO.class));
                                } catch (JsonConvertException e) {
                                    e.printStackTrace();
                                }
                            }
                            userInfo.getGroupInfoByGroupType(type).setImages(images);

                            if (isSetNewPhotoInfo) {
                                Intent homeInfoAddIntent = new Intent(TradeCardPhotoEditorSetActivity.this, HomeWorkAddInfoActivity.class);
                                Bundle bundle = new Bundle();
                                if (nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)
                                    bundle.putString("type", "home");
                                else
                                    bundle.putString("type", "work");
                                bundle.putSerializable("userInfo", userInfo);
                                if (isAddNewWorkProfile) {
                                    bundle.putBoolean("isAddNewWorkProfile", isAddNewWorkProfile);
                                    homeInfoAddIntent.putExtras(bundle);
                                    startActivityForResult(homeInfoAddIntent, NEW_WORK_INFO);
                                } else {
                                    homeInfoAddIntent.putExtras(bundle);
                                    TradeCardPhotoEditorSetActivity.this.startActivity(homeInfoAddIntent);
                                    TradeCardPhotoEditorSetActivity.this.finish();
                                }

                            } else {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("userInfo", userInfo);
                                intent.putExtras(bundle);
                                TradeCardPhotoEditorSetActivity.this.setResult(RESULT_OK, intent);
                                TradeCardPhotoEditorSetActivity.this.finish();
                            }
                        } else {
                            MyApp.getInstance().showSimpleAlertDiloag(TradeCardPhotoEditorSetActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                        }
                    }
                });
    }

    private void uploadEntityBackAndForegroundPhotoFile(int entityId , String backgroundPhotoPath , String foregroundPhotoPath
            , int _foregroundLeft , int _foregroundTop , int _foregroundWidth , int _foregroundHeight)

    {
        final int foregroundLeft = _foregroundLeft;
        final int foregroundTop = _foregroundTop;
        final int foregroundWidth = _foregroundWidth;
        final int foregroundHeight = _foregroundHeight;

        entity.setEntityImages(new ArrayList<EntityImageVO>());
        File backgroundPhotoFile = new File(backgroundPhotoPath);
        File foregroundPhotoFile = new File(foregroundPhotoPath);
        if(!foregroundPhotoFile.exists())
            foregroundPhotoFile = null;
        if(backgroundPhotoFile.exists()) {
            EntityRequest.uploadMultipleImage(entityId,backgroundPhotoFile, foregroundPhotoFile, true, new ResponseCallBack<List<EntityImageVO>>() {
                @Override
                public void onCompleted(JsonResponse<List<EntityImageVO>> response) {
                    if (response.isSuccess()) {
                        List<EntityImageVO> images = response.getData();

                        for (int i = 0; i < images.size(); i++) {
                            if (images.get(i).getZIndex() == 1)//foreground photo
                            {
                                //set position
                                images.get(i).setWidth(Float.valueOf((float) foregroundWidth));
                                images.get(i).setHeight(Float.valueOf((float) foregroundHeight));
                                images.get(i).setTop(Float.valueOf((float) foregroundTop));
                                images.get(i).setLeft(Float.valueOf((float) foregroundLeft));
                                break;
                            }
                        }
                        entity.setEntityImages(images);

                        if (isSetNewPhotoInfo) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("entity", entity);
                            bundle.putSerializable("isNewEntity" , true);
                            Intent intent = new Intent(TradeCardPhotoEditorSetActivity.this, EntityInfoInputActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            TradeCardPhotoEditorSetActivity.this.finish();
                        } else {
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("entity", entity);
                            intent.putExtras(bundle);

                            TradeCardPhotoEditorSetActivity.this.setResult(RESULT_OK, intent);
                            TradeCardPhotoEditorSetActivity.this.finish();
                        }

                    } else {
                        MyApp.getInstance().showSimpleAlertDiloag(TradeCardPhotoEditorSetActivity.this, R.string.str_alert_failed_to_upload_photo, null);
                    }

                }
            });
        }
    }

    public void createAndShowAlertDialog(final ArchiveMediaItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        if(item.mediaType == ArchiveMediaItem.PHOTO_MEDIAT_TYPE)
        {
        	builder.setMessage(getResources().getString(R.string.confirm_message_delete_photo));
        }
        else
        {
        	builder.setMessage(getResources().getString(R.string.confirm_message_delete_video));
        }
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 //TODO
                mAdapter.removeItem(item);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 //TODO
                 dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onArchiveItemClick(ArchiveMediaItem item, int position) {
        final ArchiveMediaItem mediaItem = item;
        final int itemPos = position;
        if(mAdapter != null)
        {
            int type = 1;
            if(mediaItem.archiveType == ConstValues.WORK_PHOTO_EDITOR)
                type = 2;
            if(mediaItem.archiveType == ConstValues.HOME_PHOTO_EDITOR || mediaItem.archiveType == ConstValues.WORK_PHOTO_EDITOR)
            {
                TradeCard.pickupArchiveImage(mediaItem.archiveID , type , new ResponseCallBack<List<TcImageVO>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<TcImageVO>> response) {
                        if(response.isSuccess())
                        {
                            List<TcImageVO> images = response.getData();
                            if(images == null) return;
                            if(nTradeCardType == ConstValues.HOME_PHOTO_EDITOR)//home
                            {
                                userInfo.getHome().setImages(images);
                            }
                            else
                            {
                                userInfo.getWork().setImages(images);
                            }
                            {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("userInfo", userInfo);
                                intent.putExtras(bundle);

                                TradeCardPhotoEditorSetActivity.this.setResult(RESULT_OK, intent);
                                TradeCardPhotoEditorSetActivity.this.finish();
                            }
                        }
                        else {
                            MyApp.getInstance().showSimpleAlertDiloag(TradeCardPhotoEditorSetActivity.this , response.getErrorMessage() , null);
                        }
                    }
                });
            }
            else
            {
                EntityRequest.pickupArchiveImage(entity.getId() , mediaItem.archiveID , new ResponseCallBack<List<EntityImageVO>>() {
                    @Override
                    public void onCompleted(JsonResponse<List<EntityImageVO>> response) {
                        if(response.isSuccess())
                        {
                            List<EntityImageVO> images = response.getData();
                            entity.setEntityImages(images);
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("entity", entity);
                            intent.putExtras(bundle);

                            TradeCardPhotoEditorSetActivity.this.setResult(RESULT_OK, intent);
                            TradeCardPhotoEditorSetActivity.this.finish();
                        }
                        else
                        {
                            MyApp.getInstance().showSimpleAlertDiloag(TradeCardPhotoEditorSetActivity.this , response.getErrorMessage() , null);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onArchiveDeleteClick(ArchiveMediaItem item, int position) {
        final ArchiveMediaItem mediaItem = item;
        final int itemPos = position;
        if(mAdapter != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(TradeCardPhotoEditorSetActivity.this);
            builder.setMessage(getResources().getString(R.string.str_confirm_dialog_delete_archive_images));
            builder.setPositiveButton(getResources().getString(R.string.str_confirm_dialog_yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int paramInt) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    if(mediaItem.archiveType == ConstValues.HOME_PHOTO_EDITOR || mediaItem.archiveType == ConstValues.WORK_PHOTO_EDITOR)
                    {
                        int type = 1;
                        if(mediaItem.archiveType == ConstValues.WORK_PHOTO_EDITOR)
                            type = 2;
                        TradeCard.deleteArchiveImage(mediaItem.archiveID , type , new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if(response.isSuccess())
                                {
                                    mAdapter.removeItem(itemPos);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    else if(mediaItem.archiveType == ConstValues.ENTITY_PHOTO_EDITOR)
                    {
                        EntityRequest.deleteArchiveImage(entity.getId(), mediaItem.archiveID, new ResponseCallBack<Void>() {
                            @Override
                            public void onCompleted(JsonResponse<Void> response) {
                                if (response.isSuccess()) {
                                    mAdapter.removeItem(itemPos);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_confirm_dialog_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int paramInt) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
}
