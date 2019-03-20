package com.videophotofilter.android.com;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.boxes.Container;
import com.ginko.common.RuntimeContext;
import com.ginko.customview.ProgressHUD;
import com.ginko.ginko.MyApp;
import com.ginko.ginko.R;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;
import com.ringdroid.soundfile.CheapSoundFile;

import customviews.library.widget.AbsHListView;
import customviews.library.widget.AdapterView;
import customviews.library.widget.HListView;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import jp.co.cyberagent.android.gpuimage.MediaAudioEncoder;
import jp.co.cyberagent.android.gpuimage.MediaEncoder;
import jp.co.cyberagent.android.gpuimage.MediaMuxerWrapper;
import jp.co.cyberagent.android.gpuimage.MediaVideoEncoder;

import com.videophotofilter.android.videolib.media.RangeSelector;
import com.videophotofilter.android.videolib.org.m4m.AudioFormat;
import com.videophotofilter.android.videolib.org.m4m.IProgressListener;
import com.videophotofilter.android.videolib.org.m4m.IVideoEffect;
import com.videophotofilter.android.videolib.org.m4m.MediaComposer;
import com.videophotofilter.android.videolib.org.m4m.android.AndroidMediaObjectFactory;
import com.videophotofilter.android.videolib.org.m4m.android.AudioFormatAndroid;
import com.videophotofilter.android.videolib.org.m4m.android.VideoFormatAndroid;
import com.videophotofilter.android.videolib.org.m4m.domain.FileSegment;
import com.videophotofilter.android.videolib.org.m4m.effects.BookStoreEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.CityEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.CountryEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.FilmEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.ForestEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.GrayScaleEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.LakeEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.MomentEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.NYCEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.OriginalEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.QEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.TeaEffect;
import com.videophotofilter.android.videolib.org.m4m.effects.VintageEffect;
import com.videophotofilter.library.android.com.AspectFrameLayout;
import com.videophotofilter.library.android.com.CameraHelper;
import com.videophotofilter.library.android.com.CameraUtils;
import com.videophotofilter.library.android.com.Filters;
import com.videophotofilter.library.android.com.MyProgressDialog;
import com.videophotofilter.library.android.com.SquarePhotoImageView;
import com.videophotofilter.library.android.com.VideoSegment;
import com.videophotofilter.library.android.com.VideoSegmentView;
import com.videophotofilter.library.android.com.CameraHelper.CameraInfo2;
import com.videophotofilter.soundtrack.com.SeekTest;
import com.videophotofilter.soundtrack.com.SongMetadataReader;
import com.videophotofilter.soundtrack.com.WaveformView;
import com.videophotofilter.soundtrack.com.WaveformView.WaveformListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer;
import android.media.MediaCodec.BufferInfo;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Lee
 *
 */
public class RecordFilterCameraActivity extends Activity implements
		OnClickListener,
		customviews.library.widget.AdapterView.OnItemClickListener,
		SurfaceTexture.OnFrameAvailableListener, WaveformListener,
		RangeSelector.RangeSelectorEvents{

	private final int SHOW_PROGRESS_DIALOG = 1;
	private final int HIDE_PROGRESS_DIALOG = 2;
	private final int MIX_AUDIO_VIDEO = 3;
	private final int DELAY_ACTION = 4;

	private final int SELECT_AUDIO = 1133;

	private boolean isOverRecordTime = false;
	private boolean isApplied = false;
	private boolean isPaused = true;

	/* UI Variables */
	private ProgressDialog progressDialog;

	private AspectFrameLayout aspectFrameLayout;
	private HListView filterListView;
	private GLSurfaceView cameraSurfaceView = null;
	//private GLSurfaceView cameraSurfaceView;
	private ImageView gridImageView, imgVideoPlay;
	private SquarePhotoImageView takenPhotoImageView;
	private VideoSegmentView segmentView;

	private RelativeLayout headerLayout;

	private String videoFilePathName;
	private RangeSelector mSegmentSelector;

	private long mVideoDuration;
	// buttons
	private ImageView btnPrev, btnApply, btnNext, btnDelete, focusRect;

	// For Autofocus
	private static final float MIN_GRA=0.9f;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private float[] gOld={0f,0f,SensorManager.GRAVITY_EARTH};
	private float[] gNew={0f,0f,SensorManager.GRAVITY_EARTH};
	//animation autofocus
	private Animation anim_focus;
	//TextView
	private TextView textView, txtStartPos, txtEndPos;

	// controller buttons
	private ImageView imgButtonGridOnOff, imgBtnFlashLightOnOff,
			imgBtnCameraFrontOnOff, imgBtnMicOnOff, imgBtnAutoFocusOnOff,
	/*imgBtnGhostImage, */imgButtonScissor;

	//video rotate
	private String rotate = "0";
	/* Variables */
	private ArrayList<VideoSegment> videoSegments;

	private FilterAdapter filterAdapter;
	private CameraHelper mCameraHelper;
	private CameraLoader mCamera;
	private GPUImage mGPUImage;
	private GPUImageToneCurveFilter tone_curve_Filters[];
	//private GPUImageFilter currentImageFilter = null;
	private GPUImageGrayscaleFilter bwFilter;
	private GPUImageFilter currentFilter = null ;

	private boolean isVideoProcessing = false;

	// minimal camera preview width and height
	private int minPreviewWidth = 9999;
	private int minPreviewHeight = 9999;

	private int maxVideoWidth = 0;
	private int maxVideoHeight = 0;

	private int mMaxFPSRange = 24;

	// setting values
	private boolean isGridOn = false;
	private boolean isCameraFrontOn = false;
	private boolean isFlashOn = false;
	private boolean isMicOn = true;
	private boolean isAutoFocusOn = false;
	private boolean isCameraUsed = false;

	private boolean hasMoreThanOneCamera = true;

	// synchronizing objects
	private Object lockObj = new Object();

	// bitmap objects
	private Bitmap bitmapOrigin;

	// variables for touch events
	private long actionDownTime = 0;

	// variables for recording video
	private boolean isRecording = false;
	private boolean isDelayed = false;
	private long totalRecordedTime = 0;

	private String mSaveDirectoryPath = RuntimeContext.getAppDataFolder("temp");
	private MediaMuxerWrapper mMuxer;
	private Thread mDrawingThread;

	private final int REFRESH_VIDEO_SEGMENT = 1;

	private String audioFilePath = "";
	private String videoFilePath = "";
	private String outputFilePath = "";

	private boolean isMuxingUI = false;
	LinearLayout recordingControl;
	LinearLayout muxingControl;
	LinearLayout controllerForRecord;

	private WaveformView waveFormView;
	private String muxAudioFilePath = "";
	private ImageView imgBtnAddAudio;
	private MediaPlayer mMediaPlayer = null;
	private MediaPlayer mPlayer = null;
	private boolean mIsPlaying = false;
	private boolean mLoadingKeepGoing;
	private File mFile;
	private ProgressDialog mProgressDialog;
	long mLoadingStartTime;
	long mLoadingLastUpdateTime;
	private boolean mCanSeekAccurately;
	private CheapSoundFile mSoundFile;
	private float mDensity;
	private int mMaxPos;
	private int mStartPos;
	private int mEndPos;
	private int mLastDisplayedStartPos;
	private int mLastDisplayedEndPos;
	private boolean mTouchDragging;
	private int mOffset = 0;
	private int mOffsetDefault = 0;
	private int mOffsetGoal;
	private int mFlingVelocity;
	private int mPlayStartOffset;
	private int mWidth;
	private float mTouchStart;
	private int mTouchInitialOffset;
	private long mWaveformTouchStartMsec;
	private int mPlayStartMsec = 0;

	private String strPath = null;
	private String strType = null;
	private int mHome = 1;
	private boolean isNewEntity = false;
	private boolean mIsOnlyFilter = false;
	private int mPlayEndMsec = 0;
	private int mPlayingMsec = 0;
	private int mPlayTempMsec = 0;

	private int effectIndex = 0;

	private boolean isAddAudio = false;
	private boolean fromGallery = false;

	// Audio
	protected final String audioMimeType = "audio/mp4a-latm";
	protected final int audioBitRate = 96 * 1024;
	private AudioFormat audioFormat = null;

	private ProgressHUD progressHUD;

	private CountDownTimer checkCounter = null, countDwonForPlayVideo = null, cntr_aCounter;

	private String videoFileName = "";

	private PackageManager pm;
	private String workFolder;
	private Handler mRefreshHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REFRESH_VIDEO_SEGMENT:
					if (segmentView != null)
					{
						segmentView.refresh();
					}
					break;
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case SELECT_AUDIO:
				if (resultCode == RESULT_OK || data != null) {
					//String audioPath = ImageUtil.getRealPathFromURI(this , data.getData() , ImageUtil.MEDIA_TYPE_AUDIO);'
					isAddAudio = true;
					Uri uri = data.getData();
					String audioPath = uri.getPath();

					if(audioPath != null) {
						if (uri.toString().contains("content://") || uri.toString().contains("file://") == false)
							audioPath = getPath(uri);
						if(audioPath == null)
							audioPath = uri.getPath();

						this.audioFilePath = audioPath;
						if (audioFilePath.equals("")) {
							waveFormView.setVisibility(View.GONE);
							imgBtnAddAudio.setImageResource(R.drawable.btn_musicadd);
							initAudioRelatedValues();
						} else {
							waveFormView.setVisibility(View.VISIBLE);
							imgBtnAddAudio.setImageResource(R.drawable.btn_musicdelete);
							loadAudioFromFile();
						}
					}
				} else {
					this.audioFilePath = "";
				}
				break;

			default:
				super.onActivityResult(requestCode, resultCode, data);
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

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_PROGRESS_DIALOG:
					if (progressDialog == null) {
						progressDialog = MyProgressDialog
								.createProgressDialog(
										RecordFilterCameraActivity.this,
										RecordFilterCameraActivity.this
												.getResources()
												.getString(
														R.string.progress_dialog_loading));
					} else
						progressDialog.show();
					break;

				case HIDE_PROGRESS_DIALOG:
					isVideoProcessing = false;
					if (progressDialog != null)
						progressDialog.dismiss();
					break;

				case DELAY_ACTION:
					stopAfterFewMilliseconds();
					break;
				case MIX_AUDIO_VIDEO:
					if (videoSegments.size() == 0) {
						btnApply.setVisibility(View.INVISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor_disable);
						return;
					}

					String outParam = mSaveDirectoryPath + "/output" + msg.arg1 + ".mp4";
					String videoParam = videoFilePath = mSaveDirectoryPath + "/video" + msg.arg1 + ".mp4";
					String audioParam =audioFilePath = mSaveDirectoryPath + "/audio" + msg.arg1 + ".mp3";

					mux(videoParam, audioParam, outParam);

					//if (btnApply.getVisibility() == View.INVISIBLE)
					//	btnApply.setVisibility(View.VISIBLE);
					if(progressHUD.isShowing())
						progressHUD.hide();


					File file = new File(audioParam);
					file.delete();

					file = new File(videoParam);
					file.delete();
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_recordvideo);

		if (android.os.Build.VERSION.SDK_INT < 18) {
			Toast.makeText(
					RecordFilterCameraActivity.this,
					"To use video feature, you are needed to upgrade your system version over Android 4.3",
					Toast.LENGTH_LONG);
			finish();
			return;
		}

		pm = getApplicationContext().getPackageManager();
		Intent intent = getIntent();
		strPath = intent.getStringExtra("strPathFromGallery");
		strType = intent.getStringExtra("typeId");
		fromGallery = intent.getBooleanExtra("fromGallery", false);
		isNewEntity = intent.getBooleanExtra("isNewEntity", isNewEntity);

		if("personalInfo".equals(strType))
			mHome = intent.getIntExtra("isHome", 1);
		//get UI objects
		getUIObjects();


		mGPUImage = new GPUImage(this);

			if(strPath == null || strPath.equals(""))
				mGPUImage.setGLSurfaceView(cameraSurfaceView);
			else {
				mIsOnlyFilter = true;
				controllerForRecord = (LinearLayout) findViewById(R.id.controllerForRecord);
				LinearLayout controllerForPlay = (LinearLayout) findViewById(R.id.controllerForPlay); controllerForPlay.setVisibility(View.VISIBLE);
				imgVideoPlay = (ImageView)findViewById(R.id.imgBtnForPlay);
				imgVideoPlay.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						cameraSurfaceView.setBackground(null);
						if (!mMediaPlayer.isPlaying()) {
							imgVideoPlay.setBackground(getResources().getDrawable(R.drawable.btn_stop));
							mPlayingMsec = getSegmentTo() - getSegmentFrom();
							playVideoFromTo(mPlayingMsec);

						} else {
							mMediaPlayer.pause();
							countDwonForPlayVideo.onFinish();
							countDwonForPlayVideo.cancel();
						}
					}
				});

				mSegmentSelector = (RangeSelector) findViewById(R.id.segment); mSegmentSelector.setVisibility(View.VISIBLE);
				mSegmentSelector.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						mSegmentSelector.setDuration(mGPUImage.onGetDuration());
						return false;
					}
				});
				mSegmentSelector.setEventsListener(this);
				mSegmentSelector.setStartPosition(0);
				mSegmentSelector.setEndPosition(100);

				controllerForRecord.setVisibility(View.GONE);
				segmentView.setVisibility(View.GONE);
				imgButtonScissor.setVisibility(View.GONE);
				textView.setVisibility(View.GONE);
				btnApply.setVisibility(View.VISIBLE);

				if (mMediaPlayer != null) {
					mMediaPlayer.release();
					mMediaPlayer = null;
				}

				mMediaPlayer = new MediaPlayer();

			/*mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					countDwonForPlayVideo.onFinish();
					countDwonForPlayVideo.cancel();
				}
			});*/

				try {
					mMediaPlayer.setDataSource(strPath);

					MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
					metaRetriever.setDataSource(strPath);
					int height = Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
					int width = Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
					rotate = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

					Display display = getWindowManager().getDefaultDisplay();
					int m_width = display.getWidth();

					if(m_width > height || m_width > width) {
						if (width > height) {
							width = width * m_width / width;
							height = height * m_width / width;
						} else if (height > width) {
							width = width * m_width / height;
							height = height * m_width / height;
						}
					} else if(m_width < height || m_width < width) {
						if (width > height) {
							width = width * width / m_width;
							height = height * width / m_width;
						} else if (height > width) {
							width = width * height / m_width;
							height = height * height / m_width;
						}
					}

					if(rotate.equals("0") && width != height) {
						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
						params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
						cameraSurfaceView.setLayoutParams(params);
					}
					else if(rotate.equalsIgnoreCase("90") && width != height){
						FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(height, width);
						params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
						cameraSurfaceView.setLayoutParams(params);
					}

					final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(strPath, MediaStore.Video.Thumbnails.MINI_KIND);
					cameraSurfaceView.setBackground(new BitmapDrawable(getResources(), thumbnail));
					//cameraSurfaceView.setBackground(null);

					mGPUImage.setMediaPlayer(mMediaPlayer);
					mGPUImage.setVideoGLSurfaceView(cameraSurfaceView);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//mVideoDuration = (long) mMediaPlayer.getDuration();
		}
		// init values
		initalizeVariables();
	}


	/**
	 *  Initialize all the variables
	 */
	private void initalizeVariables() {
		this.videoSegments = new ArrayList<VideoSegment>();

		this.progressDialog = MyProgressDialog.createProgressDialog(this,
				getResources().getString(R.string.progress_dialog_loading));

		// init control button setting values
		this.isGridOn = false;
		this.isCameraFrontOn = false;
		this.isFlashOn = false;
		this.isMicOn = true;
		this.isAutoFocusOn = false;
		this.isCameraUsed = false;

		workFolder = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		// create camera instance
		mCameraHelper = new CameraHelper(this);
		mCamera = new CameraLoader(this);

		loadFilterACVFiles();
		if(mIsOnlyFilter)
			btnApply.setVisibility(View.VISIBLE);
		else {
			btnApply.setVisibility(View.INVISIBLE);
			mDrawingThread = new Drawing();
			mDrawingThread.start();
		}
	}

	private void getUIObjects() {
		btnPrev = (ImageView) findViewById(R.id.btnPrev); btnPrev.setImageResource(R.drawable.part_a_btn_back_nav);
		btnPrev.setOnClickListener(this);
		btnApply = (ImageView) findViewById(R.id.btnApply);
		btnApply.setOnClickListener(this);
		btnDelete = (ImageView) findViewById(R.id.btnDelete);
		btnDelete.setOnClickListener(this);
		btnNext = (ImageView) findViewById(R.id.btnNext);
		btnNext.setOnClickListener(this);

		textView = (TextView)findViewById(R.id.textView18);
		textView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/AvenirLight.ttf"));

		focusRect = (ImageView)findViewById(R.id.focus_circle);
		anim_focus= AnimationUtils.loadAnimation(this, R.anim.focus);

		TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
		if("personalInfo".equals(strType))
			if(mHome == 1)
				txtTitle.setText("Personal Info");
			else
				txtTitle.setText("Work Info");
		else if("entityInfo".equals(strType))
			txtTitle.setText("Entity Info");
		else if(fromGallery)
			txtTitle.setText("Choose Video");
		else
			txtTitle.setText("Take Video");

		imgButtonGridOnOff = (ImageView) findViewById(R.id.imgButtonGirdOnOff);
		imgButtonGridOnOff.setOnClickListener(this);
		imgBtnFlashLightOnOff = (ImageView) findViewById(R.id.imgBtnFlashLightOnOff);
		imgBtnFlashLightOnOff.setOnClickListener(this);
		imgBtnCameraFrontOnOff = (ImageView) findViewById(R.id.imgBtnCameraFrontOnOff);
		imgBtnCameraFrontOnOff.setOnClickListener(this);
		imgBtnMicOnOff = (ImageView) findViewById(R.id.imgBtnMicOnOff);
		imgBtnMicOnOff.setOnClickListener(this);
		imgBtnAutoFocusOnOff = (ImageView) findViewById(R.id.imgBtnAutoFocusOnOff);
		imgBtnAutoFocusOnOff.setOnClickListener(this);
		//imgBtnGhostImage = (ImageView) findViewById(R.id.imgBtnTransImageOnOff);
		//imgBtnGhostImage.setOnClickListener(this);
		imgButtonScissor = (ImageView) findViewById(R.id.imgScissor);
		imgButtonScissor.setOnClickListener(this);

		filterListView = (HListView) findViewById(R.id.filterlistview);
		filterAdapter = new FilterAdapter(this); filterAdapter.setSelection(0);
		filterListView.setAdapter(filterAdapter);
		filterListView.setOnItemClickListener(this);

		waveFormView = (WaveformView)findViewById(R.id.waveform);
		waveFormView.setListener(this);
		if (muxAudioFilePath.equals(""))
			waveFormView.setVisibility(View.GONE);

		imgBtnAddAudio = (ImageView)findViewById(R.id.imgBtnAddAudio);
		imgBtnAddAudio.setOnClickListener(this);

		cameraSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
		cameraSurfaceView.setEGLContextClientVersion(2);     // select GLES 2.0

		aspectFrameLayout = (AspectFrameLayout) findViewById(R.id.cameraPreview_afl);
		aspectFrameLayout.setAspectRatio(1.0d);

		recordingControl = (LinearLayout) findViewById(R.id.controll_layout);
		muxingControl = (LinearLayout) findViewById(R.id.controll_layout1);

		// imageview to show the taken photo
		takenPhotoImageView = (SquarePhotoImageView) findViewById(R.id.imgTakenPhotoView);
		takenPhotoImageView.setLongClickable(true);
		takenPhotoImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					checkRecordTime(true);

					if(isRecording && !isDelayed) {
						btnApply.setVisibility(View.VISIBLE);
						btnPrev.setVisibility(View.VISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor);
						try {
							mGPUImage.stopVideoRecording();

							if (isMicOn == true)
								stopRecording();
						} catch (Exception e) {
							e.printStackTrace();
						}
						isRecording = false;

						totalRecordedTime += videoSegments.get(videoSegments.size() - 1).durationInMills;

						outputFilePath = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
						videoFilePath = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
						audioFilePath = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".mp3";

						if (actionDownTime != 0)
							actionDownTime = 0;

						//Log.i("RecordVideo", "setOnTouch:" + outputFilePath);

						if (isMicOn == true) {
							//mHandler.sendEmptyMessageDelayed(MIX_AUDIO_VIDEO, 300);
							Message msg = new Message();
							msg.arg1 = videoSegments.size();
							msg.what = MIX_AUDIO_VIDEO;
							mHandler.sendMessageDelayed(msg, 1000);
						}
						else {
							if (btnApply.getVisibility() == View.INVISIBLE) {
								btnApply.setVisibility(View.VISIBLE);
								imgButtonScissor.setImageResource(R.drawable.scissor);
							}

							if (btnPrev.getVisibility() == View.INVISIBLE)
								btnPrev.setVisibility(View.VISIBLE);;
						}
					}
					/*mCamera.onPause();
					mCamera.onResume();
					mCamera.setAutoFocus(false);
					if(isFlashOn)
					{
						mCamera.turnFlashLightOn();
					}else{
						mCamera.turnFlashLightOff();
					}
					if(isAutoFocusOn)
						mCamera.setAutoFocus(true);
					else
						mCamera.setAutoFocus(false);*/
				}
				return false;
			}
		});
		takenPhotoImageView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if (mIsOnlyFilter) return false;
				if (isDelayed == true) return false;

				if (isMuxingUI == false) {
					if (isOverRecordTime) {
						MyApp.getInstance().showSimpleAlertDiloag(RecordFilterCameraActivity.this, "Oops! You have reached a recording limit of 30 secs.", null);
					} else {
						checkRecordTime(false);
						actionDownTime = System.currentTimeMillis();

						if ((totalRecordedTime + (System.currentTimeMillis() - actionDownTime)) > VideoFilterCore.MAX_SEGMENT_TIME * 1000) {
							mGPUImage.stopVideoRecording();
							stopRecording();
							isRecording = false;
							//GAD-1716
							btnApply.setVisibility(View.VISIBLE);
							btnPrev.setVisibility(View.VISIBLE);
							imgButtonScissor.setImageResource(R.drawable.scissor);

							if (actionDownTime != 0)
								actionDownTime = 0;
						/*
						 * AlertDialog code
						 */
							return true;
						}

						int nSize = videoSegments.size();

						if (nSize > 0 && videoSegments.get(nSize - 1).selected == true) {
							videoSegments.get(nSize - 1).selected = false;
						}

						if (isRecording == false) {
							videoSegments.add(new VideoSegment());
							segmentView.setSegmentList(videoSegments);


							if (isMicOn == true) {
								String tmpStr = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
								mGPUImage.startVideoRecording(tmpStr);

								tmpStr = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".mp3";
								startRecording(tmpStr);
							} else {
								String tmpStr = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
								mGPUImage.startVideoRecording(tmpStr);
							}

							isRecording = true;
							//GAD-1716
							btnApply.setVisibility(View.INVISIBLE);
							btnPrev.setVisibility(View.INVISIBLE);
							imgButtonScissor.setImageResource(R.drawable.scissor_disable);
						}
					}
				}

				return false;
			}
		});

		takenPhotoImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mIsOnlyFilter) return;
				if (isMuxingUI == false )
				{
					if (isRecording && !isDelayed) {
						try {
							mGPUImage.stopVideoRecording();

							if (isMicOn == true)
								stopRecording();
						}catch (Exception e){
							e.printStackTrace();
						}
						isRecording = false;
						//GAD-1716
						btnApply.setVisibility(View.VISIBLE);
						btnPrev.setVisibility(View.VISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor);

						totalRecordedTime += videoSegments.get(videoSegments.size() - 1).durationInMills;

						outputFilePath = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
						videoFilePath = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
						audioFilePath = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".m4a";

						if (actionDownTime != 0)
							actionDownTime = 0;

						if (isMicOn == true) {
							//mHandler.sendEmptyMessageDelayed(MIX_AUDIO_VIDEO, 300);
							Message msg = new Message();
							msg.arg1 = videoSegments.size();
							msg.what = MIX_AUDIO_VIDEO;
							mHandler.sendMessageDelayed(msg, 1000);
						}
						else {
							if (btnApply.getVisibility() == View.INVISIBLE) {
								btnApply.setVisibility(View.VISIBLE);
								imgButtonScissor.setImageResource(R.drawable.scissor);
							}

							if (btnPrev.getVisibility() == View.INVISIBLE)
								btnPrev.setVisibility(View.VISIBLE);
						}
					}
				}
				else
				{
					if(mPlayer != null) {
						if (mGPUImage.getVideoPrepared() == false)
							return;
						if(mPlayer.isPlaying()) {
							mGPUImage.onStop(0);
							cntr_aCounter.onFinish();
							cntr_aCounter.cancel();
							handlePause();
						}else {
							mGPUImage.onPlayNoneMute();
						/*mPlayer.seekTo(0);
						playVideoAndAudio();
						updateDisplay();*/
							onPlay((int) (mOffset));
						}
					}else {
						if (mMediaPlayer != null) {
							if (mGPUImage.getVideoPrepared() == false)
								return;
							if (mMediaPlayer.isPlaying())
								mGPUImage.onStop(0);
							else
								mGPUImage.onPlay();
						}
					}

				}
			}
		});

		// add grid view
		gridImageView = new ImageView(this);
		gridImageView.setScaleType(ImageView.ScaleType.FIT_XY);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		gridImageView.setLayoutParams(params);
		gridImageView.setImageResource(R.drawable.grid_background);
		aspectFrameLayout.addView(gridImageView);
		gridImageView.setVisibility(View.INVISIBLE);

		refreshButtons();

		segmentView = (VideoSegmentView)findViewById(R.id.segmentview);

		//Show process progress dialog
		progressHUD = ProgressHUD.createProgressDialog(RecordFilterCameraActivity.this, "", false, false, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if(progressHUD != null && progressHUD.isShowing())
					progressHUD.dismiss();
			}
		});

		if(isNewEntity)
		{
			headerLayout = (RelativeLayout)findViewById(R.id.header_layout);
			headerLayout.setBackgroundColor(getResources().getColor(R.color.top_titlebar_color));
			txtTitle.setTextColor(getResources().getColor(R.color.top_title_text_color));
			btnApply.setImageResource(R.drawable.part_a_btn_check_nav_wh);
			//btnPrev.setImageResource(R.drawable.btnclose_white);
			btnPrev.setImageResource(R.drawable.btn_back_nav_white);
		}
	}

	private void checkRecordTime(boolean cancel)
	{
		if(!cancel){
			checkCounter = new CountDownTimer(30000 - totalRecordedTime, 1000) {
				public void onTick(long millisUntilFinished) {
				}

				public void onFinish() {
					//code fire after finish
					Context mContext = RecordFilterCameraActivity.this;
					if (mContext != null)
						stopRecordVideoAudio();
				}
			};
			checkCounter.start();
		}
		else {
			if(checkCounter != null)
				checkCounter.cancel();
			if (videoSegments.size() > 0)
			{
				long durations = videoSegments.get(videoSegments.size() - 1).durationInMills;
				//Log.i("RecordVideo", "---CheckTime: " + durations);
				if (isRecording == true && isDelayed == false && durations < 500) {
					isDelayed = true;
					mHandler.sendEmptyMessageDelayed(DELAY_ACTION, Math.max(50, 500 - durations));
				}
			}
		}
	}

	private void stopAfterFewMilliseconds() {
		isRecording = false;
		isDelayed = false;

		mGPUImage.stopVideoRecording();
		if (isMicOn == true)
			stopRecording();

		totalRecordedTime += videoSegments.get(videoSegments.size() - 1).durationInMills;

		outputFilePath = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
		videoFilePath = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
		audioFilePath = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".mp3";

		//Log.i("RecordVideo", "stopAfter:" + outputFilePath);

		if (actionDownTime != 0)
			actionDownTime = 0;
		if (isMicOn == true) {

			//mHandler.sendEmptyMessageDelayed(MIX_AUDIO_VIDEO, 300);
			Message msg = new Message();
			msg.arg1 = videoSegments.size();
			msg.what = MIX_AUDIO_VIDEO;
			mHandler.sendMessageDelayed(msg, 1000);

			if (btnApply.getVisibility() == View.INVISIBLE) {
				btnApply.setVisibility(View.VISIBLE);
				imgButtonScissor.setImageResource(R.drawable.scissor);
			}
			if (btnPrev.getVisibility() == View.INVISIBLE)
				btnPrev.setVisibility(View.VISIBLE);
		}else {

			if (btnApply.getVisibility() == View.INVISIBLE) {
				btnApply.setVisibility(View.VISIBLE);
				imgButtonScissor.setImageResource(R.drawable.scissor);
			}

			if (btnPrev.getVisibility() == View.INVISIBLE)
				btnPrev.setVisibility(View.VISIBLE);
		}
	}

	private synchronized void stopRecordVideoAudio() {
		progressHUD.show();
		isOverRecordTime = true;
		isRecording = false;

		mGPUImage.stopVideoRecording();
		if (isMicOn == true)
			stopRecording();

		totalRecordedTime += videoSegments.get(videoSegments.size() - 1).durationInMills;

		outputFilePath = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
		videoFilePath = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
		audioFilePath = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".mp3";

		if (actionDownTime != 0)
			actionDownTime = 0;

		AlertDialog alertDialog = new AlertDialog.Builder(RecordFilterCameraActivity.this).create();
		alertDialog.setMessage("Oops! You have reached a recording limit of 30 secs.");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog closed
				if (isMicOn == true) {

					//mHandler.sendEmptyMessageDelayed(MIX_AUDIO_VIDEO, 300);
					Message msg = new Message();
					msg.arg1 = videoSegments.size();
					msg.what = MIX_AUDIO_VIDEO;
					mHandler.sendMessageDelayed(msg, 1000);

					if (btnApply.getVisibility() == View.INVISIBLE) {
						btnApply.setVisibility(View.VISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor);
					}
					if (btnPrev.getVisibility() == View.INVISIBLE)
						btnPrev.setVisibility(View.VISIBLE);
				}else {
					if(progressHUD.isShowing())
						progressHUD.hide();
					if (btnApply.getVisibility() == View.INVISIBLE) {
						btnApply.setVisibility(View.VISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor);
					}

					if (btnPrev.getVisibility() == View.INVISIBLE)
						btnPrev.setVisibility(View.VISIBLE);

				}
			}
		});
		alertDialog.show();
	}
	private void playVideoAndAudio(){
		cntr_aCounter = new CountDownTimer(totalRecordedTime, 1000) {
			public void onTick(long millisUntilFinished) {
				if(mPlayer != null)
					mPlayer.start();
			}

			public void onFinish() {
				//code fire after finish
				if(mPlayer != null) {
					handlePause();
				}
			}
		};
		cntr_aCounter.start();
	}

	private void playVideoFromTo(int playTime){
		mIsPlaying = true;
		mGPUImage.onPlay();
		countDwonForPlayVideo = new CountDownTimer(playTime, 1000) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				//code fire after finish
				mIsPlaying = false;
				imgVideoPlay.setBackground(getResources().getDrawable(R.drawable.btn_play));
				mMediaPlayer.seekTo(getSegmentFrom());
			}
		};
		countDwonForPlayVideo.start();
	}

	private void showProgressDialog() {
		mHandler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
	}

	private void hideProgressDialog() {
		mHandler.sendEmptyMessage(HIDE_PROGRESS_DIALOG);
	}

	private void refreshButtons() {
		if (isGridOn)
			imgButtonGridOnOff.setImageResource(R.drawable.video_grid_on);
		else
			imgButtonGridOnOff.setImageResource(R.drawable.video_grid_off);

		if (hasMoreThanOneCamera) {
			if (isCameraFrontOn)
				imgBtnCameraFrontOnOff
						.setImageResource(R.drawable.video_camera_front_on);
			else
				imgBtnCameraFrontOnOff
						.setImageResource(R.drawable.video_camera_front_off);
		} else
			imgBtnCameraFrontOnOff
					.setImageResource(R.drawable.video_camera_front_off);

		if (isFlashOn && !isCameraFrontOn)
			imgBtnFlashLightOnOff.setImageResource(R.drawable.video_light_on);
		else
			imgBtnFlashLightOnOff.setImageResource(R.drawable.video_light_off);

		if (isMicOn)
			imgBtnMicOnOff.setImageResource(R.drawable.video_mic_on);
		else
			imgBtnMicOnOff.setImageResource(R.drawable.video_mic_off);

		if (isAutoFocusOn)
			imgBtnAutoFocusOnOff
					.setImageResource(R.drawable.video_autofocus_on);
		else
			imgBtnAutoFocusOnOff
					.setImageResource(R.drawable.video_autofocus_off);

/*		if (isGhostImageOn)
			imgBtnGhostImage.setImageResource(R.drawable.video_transparent_on);
		else
			imgBtnGhostImage.setImageResource(R.drawable.video_transparent_off);
*/	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//if(!mIsOnlyFilter && !isMuxingUI)
		mCamera.onResume();
		//if(cameraSurfaceView != null)
		//	cameraSurfaceView.onResume();
		isApplied = false;
	}

	@Override
	protected void onPause() {

		if(mCamera != null) {
			mCamera.onPause();
		}

		isPaused = true;

		if(mIsOnlyFilter && mMediaPlayer.isPlaying())
			mMediaPlayer.seekTo(getSegmentFrom());


		//if(cameraSurfaceView != null)
		//	cameraSurfaceView.onPause();

		if (isMuxingUI == true)
		{
			if(mPlayer != null) {
				if(mPlayer.isPlaying()) {
					mGPUImage.onStop(0);
					cntr_aCounter.onFinish();
					cntr_aCounter.cancel();
					handlePause();
				}
			}else
			if(mMediaPlayer.isPlaying())
				mGPUImage.onStop(0);
		}
		else
		{
			checkRecordTime(true);

			if(isRecording && !isDelayed) {
				btnApply.setVisibility(View.VISIBLE);
				btnPrev.setVisibility(View.VISIBLE);
				imgButtonScissor.setImageResource(R.drawable.scissor);
				try {
					mGPUImage.stopVideoRecording();

					if (isMicOn == true)
						stopRecording();
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRecording = false;

				totalRecordedTime += videoSegments.get(videoSegments.size() - 1).durationInMills;

				outputFilePath = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
				videoFilePath = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
				audioFilePath = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".mp3";

				if (actionDownTime != 0)
					actionDownTime = 0;

				//Log.i("RecordVideo", "setOnTouch:" + outputFilePath);

				if (isMicOn == true) {
					//mHandler.sendEmptyMessageDelayed(MIX_AUDIO_VIDEO, 300);
					Message msg = new Message();
					msg.arg1 = videoSegments.size();
					msg.what = MIX_AUDIO_VIDEO;
					mHandler.sendMessageDelayed(msg, 1000);
				} else {
					if (btnApply.getVisibility() == View.INVISIBLE) {
						btnApply.setVisibility(View.VISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor);
					}

					if (btnPrev.getVisibility() == View.INVISIBLE)
						btnPrev.setVisibility(View.VISIBLE);
				}
			}
		}

		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (isMuxingUI == false)
		{
			if (isRecording)
				return;
			if(mIsPlaying)
				mMediaPlayer.stop();
			finish();
		}
		else if (isMuxingUI == true)
		{
			if (isApplied)
				return;
			if((mPlayer != null && mPlayer.isPlaying()) || (mMediaPlayer != null && mMediaPlayer.isPlaying()))
				mMediaPlayer.stop();

			if(isAddAudio) {
				isAddAudio = false;
				audioFilePath = "";
				imgBtnAddAudio.setImageResource(R.drawable.btn_musicadd);
				waveFormView.setVisibility(View.GONE);
				initAudioRelatedValues();
			}

			imgButtonScissor.setVisibility(View.VISIBLE);
			recordingControl.setVisibility(View.VISIBLE);
			textView.setVisibility(View.VISIBLE);
			muxingControl.setVisibility(View.GONE);

			mIsPlaying = false;
			isMuxingUI = false;

			onPause();
			cameraSurfaceView.onPause();

			mGPUImage.onDestroyVideoRender();

			Handler handler = new Handler();
			class RefreshRunnable implements Runnable {
				public void run() {

					aspectFrameLayout.removeView(findViewById(R.id.surfaceView));

					GLSurfaceView surfaceView = new GLSurfaceView(getApplication());
					surfaceView.setId(R.id.surfaceView);
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
					lp.gravity = Gravity.CENTER_HORIZONTAL;
					aspectFrameLayout.addView(surfaceView, 0, lp);

					cameraSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
					cameraSurfaceView.setEGLContextClientVersion(2);

					mGPUImage = new GPUImage(RecordFilterCameraActivity.this);
					mGPUImage.setGLSurfaceView(cameraSurfaceView);

					//set Effect if grid set
					if (effectIndex < tone_curve_Filters.length - 1) {
						currentFilter = tone_curve_Filters[effectIndex];
						mGPUImage.setFilter(tone_curve_Filters[effectIndex]);
					} else if (effectIndex == tone_curve_Filters.length - 1) //b&W filter
					{
						currentFilter = bwFilter;
						mGPUImage.setFilter(currentFilter);
					}

					//set Grid if set grid
					if(isGridOn)
						gridImageView.setVisibility(View.VISIBLE);
					else
						gridImageView.setVisibility(View.INVISIBLE);

					//if set autoFocus, set autofocus.
					if(isAutoFocusOn) {
						focusRect.setVisibility(View.VISIBLE);
						initSensor();
						if(mSensorManager!=null&&mSensor!=null&&sensorListener!=null)
							mSensorManager.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
					}
					onResume();
					//if set camera flashLight, it set.
					if(isFlashOn)
						mCamera.turnFlashLightOn();

				}
			};


			RefreshRunnable r = new RefreshRunnable();
			handler.postDelayed(r, 100);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		//if (mGPUImage != null)
		//	mGPUImage.releaseGPUImageFrameBuffers();
	}

	private void loadFilterACVFiles() {
		// open filter .acv file
		AssetManager as = getAssets();
		tone_curve_Filters = new GPUImageToneCurveFilter[Filters.filterNames.length];

		bwFilter = new GPUImageGrayscaleFilter();
		for (int i = 0; i < Filters.filterNames.length; i++) {
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

	private class CameraLoader {

		private int mCurrentCameraId = 0;
		private Camera mCameraInstance;

		private boolean isCameraOpen = false;
		private Context mContext;

		public CameraLoader(Context context) {
			this.mContext = context;
		}

		public boolean getCameraOpen() {return isCameraOpen;}

		private Point getScreenSizeInPixel() {
			Point size = new Point();
			WindowManager wm = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			size.x = metrics.widthPixels;
			size.y = metrics.heightPixels;

			return size;
		}

		public Camera getCamera()
		{
			return mCameraInstance;
		}

		public void onResume() {
			System.out.println("-----Camera record onresume called---");
			//takenPhotoImageView.setImageBitmap(null);
			if (isPaused == true)
				setUpCamera(mCurrentCameraId);
			//cameraSurfaceView
			//		.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		}

		public void onPause() {
			//mGPUImage.releaseCamera(mCameraInstance);
			releaseCamera();
		}

		public boolean switchCamera() {
			/*if (isCameraOpen == false)
				return false;
			if (mCameraInstance == null)
				return false;*/
			if (mCameraInstance.getNumberOfCameras() < 2)
				return false;
			if(isCameraOpen == false) return false;
			try {
				if (mCameraInstance != null)
					mCameraInstance.stopPreview();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			mGPUImage.releaseGPUImageFrameBuffers();
			releaseCamera();
			mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
			setUpCamera(mCurrentCameraId);
			if (mCameraInstance == null)
				return false;
			return true;
		}

		private void setUpCamera(final int id) {
			mCameraInstance = getCameraInstance(id);
			if (mCameraInstance == null)
				return;
			isPaused = false;
			Parameters parameters = mCameraInstance.getParameters();

			// TODO adjust by getting supportedPreviewSizes and then choosing
			// the best one for screen size (best fill screen)
			if (parameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
				parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}

			// set smallest picture size of camera and bigger than screen width
			List<Camera.Size> supportedPictureSize = getSupportedPictureSizes();

			/*List<Camera.Size> supportedPictureSize = CameraUtils.getSupportedPictureSizes(mCameraInstance);
			Point screenSize = getScreenSizeInPixel();
			int minPictureWidth = 9999, minPictureHeight = 9999;
			for (int i = 0; i < supportedPictureSize.size(); i++) {
				if (supportedPictureSize.get(i).height < screenSize.x)
					continue;
				if (minPictureHeight > supportedPictureSize.get(i).height) {
					minPictureHeight = supportedPictureSize.get(i).height;
					minPictureWidth = supportedPictureSize.get(i).width;
					break;
				}
			}

			parameters.setPictureSize(minPictureWidth, minPictureHeight);*/
			parameters.setPictureSize(supportedPictureSize.get(0).width, supportedPictureSize.get(0).height);

			/*if (minPreviewWidth > 9999 || minPreviewHeight > 9999) {
				List<Camera.Size> supportedPreviewSize = parameters
						.getSupportedPreviewSizes();
				for (int i = 0; i < supportedPreviewSize.size(); i++) {
					System.out
							.println("----Supported preview size width="
									+ String.valueOf(supportedPreviewSize
									.get(i).width)
									+ " , height="
									+ String.valueOf(supportedPreviewSize
									.get(i).height));
					// if(supportedPreviewSize.get(i).height<screenSize.y)//ignore
					// the picture size smaller than the screenwidth
					// continue;
					//find minimum size 320*480
					if (mCurrentCameraId == CameraInfo.CAMERA_FACING_BACK)// back
					// camera
					{
						if (minPreviewHeight > supportedPreviewSize.get(i).height && minPreviewHeight>=480) {
							minPreviewHeight = supportedPreviewSize.get(i).height;
							minPreviewWidth = supportedPreviewSize.get(i).width;
						}
					} else if (mCurrentCameraId == CameraInfo.CAMERA_FACING_FRONT ) {
						if (minPreviewWidth > supportedPreviewSize.get(i).width && minPreviewWidth>=320) {
							minPreviewHeight = supportedPreviewSize.get(i).height;
							minPreviewWidth = supportedPreviewSize.get(i).width;
						}
					} else {
						if (minPreviewHeight > supportedPreviewSize.get(i).height && minPreviewHeight>=480) {
							minPreviewHeight = supportedPreviewSize.get(i).height;
							minPreviewWidth = supportedPreviewSize.get(i).width;
						}
					}
				}
				Log.d("PreviewSize", "w="+minPreviewWidth + "h="+minPreviewHeight);
				//parameters.setPreviewSize(minPreviewWidth, minPreviewHeight);

				System.out.println("----Min Preview Size width="
						+ String.valueOf(minPreviewWidth) + " , height="
						+ String.valueOf(minPreviewHeight));
			}*/

			//parameters.setPreviewSize(minPreviewWidth, minPreviewHeight);
			parameters.setPreviewSize(minPreviewWidth, minPreviewHeight);

			final int[] fpsRanges = getPreviewFPSRanges(mCameraInstance);
			if(fpsRanges == null) return;

			parameters.setPreviewFpsRange(
					fpsRanges[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
					fpsRanges[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);

			mMaxFPSRange = (int)fpsRanges[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]/1000;
			// set auto focus on/off
			setAutoFocus(isAutoFocusOn);
			try {
				mCameraInstance.setParameters(parameters);
			}catch (Exception e){
				e.printStackTrace();
			}
			int orientation = mCameraHelper.getCameraDisplayOrientation(RecordFilterCameraActivity.this, mCurrentCameraId);

			CameraInfo2 cameraInfo = new CameraInfo2();
			mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
			boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
			mGPUImage.setVideoSize(1280, 1280);

			mGPUImage.deleteImage();
			if (isMuxingUI == false)
			{
				mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
			}
			else
			{
				mGPUImage.setUpVideoPlayer(null, orientation, flipHorizontal, false);
			}
		}

		public List<Camera.Size> getSupportedPictureSizes() {
			if (mCamera == null) {
				return null;
			}

			List<Camera.Size> pictureSizes = CameraUtils.getSupportedPictureSizes(mCameraInstance);

			checkSupportedPictureSizeAtPreviewSize(pictureSizes);

			return pictureSizes;
		}

		private void checkSupportedPictureSizeAtPreviewSize(List<Camera.Size> pictureSizes) {
			List<Camera.Size> previewSizes = mCameraInstance.getParameters().getSupportedPreviewSizes();
			Camera.Size pictureSize;
			Camera.Size previewSize;
			double  pictureRatio = 0;
			double  previewRatio = 0;
			final double aspectTolerance = 0.05;
			boolean isUsablePicture = false;

			double mPrevRatio = 0;

			for (int indexOfPicture = pictureSizes.size() - 1; indexOfPicture >= 0; --indexOfPicture) {
				pictureSize = pictureSizes.get(indexOfPicture);
				pictureRatio = (double) pictureSize.width / (double) pictureSize.height;
				isUsablePicture = false;

				for (int indexOfPreview = previewSizes.size() - 1; indexOfPreview >= 0; --indexOfPreview) {
					previewSize = previewSizes.get(indexOfPreview);

					previewRatio = (double) previewSize.width / (double) previewSize.height;

					if (Math.abs(pictureRatio - previewRatio) < aspectTolerance) {
						isUsablePicture = true;
						mPrevRatio = previewRatio;
						break;
					}
				}
				if (isUsablePicture == false) {
					pictureSizes.remove(indexOfPicture);
				}
			}

			for (int indexOfPreview = previewSizes.size() - 1; indexOfPreview >= 0; --indexOfPreview) {
				previewRatio = (double) previewSizes.get(indexOfPreview).width / (double) previewSizes.get(indexOfPreview).height;
				if(mPrevRatio == previewRatio && previewSizes.get(indexOfPreview).width < minPreviewHeight)
				{
					minPreviewHeight = previewSizes.get(indexOfPreview).height;
					minPreviewWidth = previewSizes.get(indexOfPreview).width;
				}
				if(mPrevRatio == previewRatio && previewSizes.get(indexOfPreview).width < 1400 && previewSizes.get(indexOfPreview).width > maxVideoWidth)
				{
					maxVideoHeight = previewSizes.get(indexOfPreview).height;
					maxVideoWidth = previewSizes.get(indexOfPreview).width;
				}
			}
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

		private int[] getPreviewFPSRanges(Camera camera)
		{
			Camera.Parameters cp = camera.getParameters();
			int[] fps = null;
			for (int[] f : cp.getSupportedPreviewFpsRange()) {
				Log.d("VideoFilter", "FpsRange = {"+
						f[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]+","+
						f[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]+"}");
				if (f[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] >= 24*1000)//max fps is 24
				{
					fps = f;
				}
			}
			if (fps == null) {
				return fps;
			}
			return fps;
		}

		private void releaseCamera() {
			if (isPaused == true) return;
			try{
				mCameraInstance.setPreviewCallback(null);
				mCameraInstance.stopPreview();
				mCameraInstance.lock();
				mCameraInstance.release();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			finally{
				mCameraInstance = null;
				isCameraOpen = false;
			}
		}

		// turn on the flash light
		public boolean turnFlashLightOn() {
			if (isCameraOpen == false)
				return false;
			if (mCameraInstance != null) {
				Parameters params = mCameraInstance.getParameters();
				if(isFlashSupported(pm) && !isCameraFrontOn) {
					params.setFlashMode(Parameters.FLASH_MODE_TORCH);
					mCameraInstance.setParameters(params);
				}
				else{
					return false;
				}
			}
			return true;
		}
		//Check Flash light.
		public boolean isFlashSupported(PackageManager packageManager){
			// if device support camera flash?
			if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
				return true;
			}
			return false;
		}

		// turn off the flash light
		public boolean turnFlashLightOff() {
			if (isCameraOpen == false)
				return false;

			if (mCameraInstance == null)
				return false;

			if (mCameraInstance != null) {
				PackageManager pm = getApplicationContext().getPackageManager();
				Parameters params = mCameraInstance.getParameters();
				if(isFlashSupported(pm)) {
					params.setFlashMode(Parameters.FLASH_MODE_OFF);
					mCameraInstance.setParameters(params);
				}
				/*else{
					MyApp.getInstance().showSimpleAlertDiloag(RecordFilterCameraActivity.this, "The device's camera doesn't support flash.", null);
				}*/
				// mCameraInstance.release();
				// mCameraInstance = null;

			}
			return true;
		}

		// auto fouc on/off
		public boolean setAutoFocus(boolean isAutoFocusable) {
			if(mCameraInstance == null) return false;

			Parameters params = mCameraInstance.getParameters();
			if (isAutoFocusable) // set auto focus on
			{
				if (params.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_AUTO)) {
					params.setFocusMode(Parameters.FOCUS_MODE_AUTO);

					Rect meteringRect = new Rect(-150,-150,150,150);
					ArrayList<Camera.Area> meteringAreas=new ArrayList<Camera.Area>();
					meteringAreas.add((new Camera.Area(meteringRect, 1000)));
					params.setMeteringAreas(meteringAreas);

					mCameraInstance.setParameters(params);
				}
				else
					return false;
			} else // set auto foucs off
			{
				if (params.getSupportedFocusModes().contains(
						Camera.Parameters.FOCUS_MODE_INFINITY)) {
					params.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
				} else
					return false;
			}
			return true;
		}
	}

	private Camera.AutoFocusCallback mFocus = new Camera.AutoFocusCallback(){
		@Override
		public void onAutoFocus(boolean success, Camera camera) {


		}
	};


	class FilterAdapter extends BaseAdapter {
		private Context mContext;
		private LayoutInflater inflater;
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

		private int getDrawableIdFromName(String name) {
			Resources resources = mContext.getResources();
			final int resourceId = resources.getIdentifier(name, "drawable",
					getPackageName());
			return resourceId;
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
			return position;
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

			TextView txtFilterName = (TextView) itemView
					.findViewById(R.id.txtFilterName);
			txtFilterName.setText(Filters.filterNames[position]);
			ImageView imgFilterIcon = (ImageView) itemView
					.findViewById(R.id.imgFilter);
			imgFilterIcon.setImageResource(getDrawableIdFromName("filter_image"
					+ String.valueOf(position + 1)));

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

	// ****************************************//
	// ---------- button click events----------//
	// ****************************************//
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.btnPrev:
				if (isMuxingUI == false)
				{
					if(mIsPlaying) {
						mMediaPlayer.stop();
					}
					finish();
				}
				else if (isMuxingUI == true)
				{
					if (isApplied)
						return;
					if(mPlayer != null && mPlayer.isPlaying())
							mPlayer.stop();
					if(mMediaPlayer != null && mMediaPlayer.isPlaying())
						mMediaPlayer.stop();

					if(isAddAudio) {
						isAddAudio = false;
						audioFilePath = "";
						imgBtnAddAudio.setImageResource(R.drawable.btn_musicadd);
						waveFormView.setVisibility(View.GONE);
						initAudioRelatedValues();
					}

					imgButtonScissor.setVisibility(View.VISIBLE);
					recordingControl.setVisibility(View.VISIBLE);
					textView.setVisibility(View.VISIBLE);
					muxingControl.setVisibility(View.GONE);

					mIsPlaying = false;
					isMuxingUI = false;

					onPause();
					cameraSurfaceView.onPause();

					mGPUImage.onDestroyVideoRender();
					Handler handler = new Handler();
					class RefreshRunnable implements Runnable {
						public void run() {

							aspectFrameLayout.removeView(findViewById(R.id.surfaceView));

							GLSurfaceView surfaceView = new GLSurfaceView(getApplication());
							surfaceView.setId(R.id.surfaceView);
							FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
							lp.gravity = Gravity.CENTER_HORIZONTAL;
							aspectFrameLayout.addView(surfaceView, 0, lp);

							cameraSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
							cameraSurfaceView.setEGLContextClientVersion(2);

							mGPUImage = new GPUImage(RecordFilterCameraActivity.this);
							mGPUImage.setGLSurfaceView(cameraSurfaceView);

							//set Effect if grid set
							if (effectIndex < tone_curve_Filters.length - 1) {
								currentFilter = tone_curve_Filters[effectIndex];
								mGPUImage.setFilter(tone_curve_Filters[effectIndex]);
							} else if (effectIndex == tone_curve_Filters.length - 1) //b&W filter
							{
								currentFilter = bwFilter;
								mGPUImage.setFilter(currentFilter);
							}

							//set Grid if set grid
							if(isGridOn)
								gridImageView.setVisibility(View.VISIBLE);
							else
								gridImageView.setVisibility(View.INVISIBLE);

							//if set autoFocus, set autofocus.
							if(isAutoFocusOn) {
								focusRect.setVisibility(View.VISIBLE);
								initSensor();
								if(mSensorManager!=null&&mSensor!=null&&sensorListener!=null)
									mSensorManager.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
							}
							onResume();
							//if set camera flashLight, it set.
							if(isFlashOn)
								mCamera.turnFlashLightOn();
						}
					};

					RefreshRunnable r = new RefreshRunnable();
					handler.postDelayed(r, 100);
				}
				break;

			case R.id.btnApply:

				int tSize = videoSegments.size();

				if (videoSegments.size() > 0) {
					if (videoSegments.get(tSize - 1).selected == true) {
						videoSegments.get(tSize - 1).selected = false;
					}
				}

				if(mIsOnlyFilter)
				{
					if(mMediaPlayer.isPlaying())
						mMediaPlayer.stop();

					new CutVideoBackground(RecordFilterCameraActivity.this).execute();
//					new videoProcessTask().execute();
				}else {
					if (isMuxingUI == false) {
						int count = videoSegments.size();
						int i;
						String paths[] = new String[count];
						Movie[] inMovies = new Movie[count];

						try {
							for (i = 0; i < count; i++) {
								paths[i] = mSaveDirectoryPath + "/" + "output" + String.valueOf(i + 1) + ".mp4";

								File file = new File(paths[i]);
								if (!file.exists()) {
									return;
								}

								inMovies[i] = MovieCreator.build(paths[i]);
							}

							List<Track> videoTracks = new LinkedList<Track>();
							List<Track> audioTracks = new LinkedList<Track>();

							for (Movie m : inMovies) {
								for (Track t : m.getTracks()) {
									if (t.getHandler().equals("soun")) {
										audioTracks.add(t);
									}
									if (t.getHandler().equals("vide")) {
										videoTracks.add(t);
									}
								}
							}

							Movie result = new Movie();

							if (audioTracks.size() > 0) {
								result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
							}
							if (videoTracks.size() > 0) {
								result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
							}

							Container out = new DefaultMp4Builder().build(result);

							FileOutputStream fos = null;

							try {
								fos = new FileOutputStream(mSaveDirectoryPath + "/" + "output0.mp4");
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
							try {
								out.writeContainer(byteBufferByteChannel);
								byteBufferByteChannel.close();
								fos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						LinearLayout recordingControl = (LinearLayout) findViewById(R.id.controll_layout);
						LinearLayout muxingControl = (LinearLayout) findViewById(R.id.controll_layout1);

						imgButtonScissor.setVisibility(View.GONE);
						recordingControl.setVisibility(View.GONE);
						textView.setVisibility(View.GONE);
						muxingControl.setVisibility(View.VISIBLE);


						if (mMediaPlayer != null) {
							mMediaPlayer.release();
							mMediaPlayer = null;
						}

						mMediaPlayer = new MediaPlayer();

						try {
							mMediaPlayer.setDataSource(mSaveDirectoryPath + "/" + "output0.mp4");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						isMuxingUI = true;
						if(isGridOn)
							gridImageView.setVisibility(View.INVISIBLE);

						if(isFlashOn) {
							mCamera.turnFlashLightOff();
						}
						if(isAutoFocusOn) {
							focusRect.setVisibility(View.GONE);
							if(mSensorManager!=null&&sensorListener!=null)
								mSensorManager.unregisterListener(sensorListener);
						}

						onPause();
						cameraSurfaceView.onPause();
						isApplied = true;

						Handler handler = new Handler();
						class RefreshRunnable implements Runnable {
							public void run() {

								aspectFrameLayout.removeView(findViewById(R.id.surfaceView));

								GLSurfaceView surfaceView = new GLSurfaceView(getApplication());
								surfaceView.setId(R.id.surfaceView);

								aspectFrameLayout.addView(surfaceView, 0);

								cameraSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
								cameraSurfaceView.setEGLContextClientVersion(2);

								mGPUImage = new GPUImage(RecordFilterCameraActivity.this);
								mGPUImage.setMediaPlayer(mMediaPlayer);
								mGPUImage.setVideoGLSurfaceView(cameraSurfaceView);

								onResume();
							}
						}

						RefreshRunnable r = new RefreshRunnable();
						handler.postDelayed(r, 100);

					} else {
						if(mMediaPlayer.isPlaying())
							mMediaPlayer.stop();
						if(mPlayer != null) {
							if (mPlayer.isPlaying())
								mPlayer.stop();
						}

						resultVideo();
					}
				}
				break;

			case R.id.btnDelete:
				break;

			// go to next step of this screen
			case R.id.btnNext:
				break;

			// grid line show / hide button
			case R.id.imgButtonGirdOnOff:
				if(isGridOn)
				{
					isGridOn = false;
					gridImageView.setVisibility(View.INVISIBLE);
				}else{
					isGridOn = true;
					gridImageView.setVisibility(View.VISIBLE);
				}
				refreshButtons();
				break;

			// camera flash light enable / disable
			case R.id.imgBtnFlashLightOnOff:
				if(isCameraFrontOn) return;
				if(isFlashOn)
				{
					isFlashOn = false;
					mCamera.turnFlashLightOff();
				}else if(!isFlashOn && !isCameraFrontOn){
					isFlashOn = true;
					mCamera.turnFlashLightOn();
				}
				refreshButtons();
				break;

			// switch front / rear camera
			case R.id.imgBtnCameraFrontOnOff:
				if(isCameraFrontOn)
					isCameraFrontOn = false;
				else {
					isCameraFrontOn = true;
					//isFlashOn = false;
				}

				//isCameraFrontOn = true;
				mCamera.switchCamera();

				if(mCamera.isFlashSupported(pm)) {
					if (isFlashOn)
						mCamera.turnFlashLightOn();
					else
						mCamera.turnFlashLightOff();
				}
				refreshButtons();
				break;

			//mic on/off when record video
			case R.id.imgBtnMicOnOff:
				if (isMicOn)
					isMicOn = false;
				else
					isMicOn = true;

				refreshButtons();
				break;

			// set camera auto focus option on/off
			case R.id.imgBtnAutoFocusOnOff:
				if(isAutoFocusOn) {
					isAutoFocusOn = false;

					focusRect.setVisibility(View.GONE);
					if(mSensorManager!=null&&sensorListener!=null)
						mSensorManager.unregisterListener(sensorListener);
				}
				else {
					focusRect.setVisibility(View.VISIBLE);
					initSensor();
					if(mSensorManager!=null&&mSensor!=null&&sensorListener!=null)
						mSensorManager.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

					isAutoFocusOn = true;
				}

				refreshButtons();
				if(mCamera.setAutoFocus(true))
					Log.d("CameraFocus---", "Focused");
				break;

			// show ghost image
			//case R.id.imgBtnTransImageOnOff:
			//break;

			// cut the latest recorded video segment
			case R.id.imgScissor:
				if (isRecording)
					break;
				int nSize = videoSegments.size();

				if (videoSegments.size() > 0)
				{
					if (videoSegments.get(nSize - 1).selected == false)
					{
						videoSegments.get(nSize - 1).selected = true;
					}
					else
					{
						isOverRecordTime = false;

						totalRecordedTime -= videoSegments.get(nSize - 1).durationInMills;
						videoSegments.remove(videoSegments.get(nSize - 1));
						String delteFile = mSaveDirectoryPath + "/output" + nSize + ".mp4";

						File file = new File(delteFile);
						file.delete();
					}

					if (videoSegments.size() == 0) {
						btnApply.setVisibility(View.INVISIBLE);
						imgButtonScissor.setImageResource(R.drawable.scissor_disable);
					}

					mRefreshHandler.sendEmptyMessage(REFRESH_VIDEO_SEGMENT);
				}

				break;
			case R.id.imgBtnAddAudio:
				//
				if(!isAddAudio)
				{
					//add audio
					/*Intent audioIntent1 = new Intent(Intent.ACTION_GET_CONTENT);
					audioIntent1.setType("audio*//*");
					startActivityForResult(audioIntent1, SELECT_AUDIO);*/
					Intent audioIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(audioIntent, SELECT_AUDIO);
				}
				else//delete audio
				{
					isAddAudio = false;
					audioFilePath = "";
					imgBtnAddAudio.setImageResource(R.drawable.btn_musicadd);
					waveFormView.setVisibility(View.GONE);
					initAudioRelatedValues();
				}
				break;
		}
	}

	public int secondsToFrames(double seconds, int nSampleRate, int nSamplesPerFrame) {
		return (int)(1.0 * seconds * nSampleRate / nSamplesPerFrame + 0.5);
	}

	public void resultVideo(){
		if(mSoundFile != null) {
			progressHUD.show();

			File file = new File(mSaveDirectoryPath + "/audio.mp3");
			try {
				file.createNewFile();

				double startAudio = (double) mPlayStartMsec / 1000;
				double endAudio = (double) (totalRecordedTime) / 1000;

				double totalFrames = (double) mSoundFile.getNumFrames() * mSoundFile.getSamplesPerFrame() / mSoundFile.getSampleRate();
				if(endAudio > totalFrames)
				{
					startAudio = 0;
					endAudio = totalFrames;
				}
				int startFrame = secondsToFrames(startAudio, mSoundFile.getSampleRate(), mSoundFile.getSamplesPerFrame());
				int endFrame = secondsToFrames(endAudio, mSoundFile.getSampleRate(), mSoundFile.getSamplesPerFrame());

				mSoundFile.WriteFile(file, startFrame, endFrame);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}

			//	create video
			try {
				muxing(mSaveDirectoryPath + "/output0.mp4", mSaveDirectoryPath + "/video.mp4", 0, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			new TranscdingBackground(RecordFilterCameraActivity.this).execute();
		}
		else
		{
			String newVideoName = mSaveDirectoryPath+System.currentTimeMillis()+".mp4";
			File old_video = new File(mSaveDirectoryPath+"/output0.mp4");
			File new_video = new File(newVideoName);
			old_video.renameTo(new_video);

			Intent intent = new Intent();
			intent.putExtra("strMoviePath", newVideoName);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	}
	// video filter horizontal listview item click listener
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		// TODO Auto-generated method stub
		if(isRecording)
			return;
		filterAdapter.setSelection(position);
		effectIndex = position;

		if(mIsOnlyFilter) {
			cameraSurfaceView.setBackground(null);
			mGPUImage.setFilterPosition(position);
			return;
		}
		else{
			synchronized (lockObj) {
				if (position == 0)//original bitmap
				{
					currentFilter = null;
					mGPUImage.setImage(bitmapOrigin);
					mGPUImage.setFilter(tone_curve_Filters[position]);
					currentFilter = tone_curve_Filters[position];
				} else if (position < tone_curve_Filters.length - 1) {
					currentFilter = tone_curve_Filters[position];
					//apply filter
					mGPUImage.setFilter(tone_curve_Filters[position]);
				} else if (position == tone_curve_Filters.length - 1) //b&W filter
				{
					currentFilter = bwFilter;
					//apply filter
					mGPUImage.setFilter(currentFilter);
				}
			}
		}
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub

	}

	private void startRecording(String filePath)
	{
		try {
			mMuxer = new MediaMuxerWrapper(filePath);

			if (false) {
				new MediaVideoEncoder(mMuxer, mMediaEncoderListener, minPreviewWidth, minPreviewHeight);
			}

			if (true) {
				new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
			}

			mMuxer.prepare();
			mMuxer.startRecording();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void stopRecording() {
		if (mMuxer != null) {
			mMuxer.stopRecording();
			mMuxer = null;
			// you should not wait here
		}
	}

	private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
		@Override
		public void onPrepared(final MediaEncoder encoder) {
			if (encoder instanceof MediaVideoEncoder)
				mGPUImage.setVideoEncoder((MediaVideoEncoder)encoder);
		}

		@Override
		public void onStopped(final MediaEncoder encoder) {
			if (encoder instanceof MediaVideoEncoder)
				mGPUImage.setVideoEncoder(null);
		}
	};

	public boolean mux(String videoFile, String audioFile, String outputFile)
	{
		//Log.i("RecordVideo", "-------Start Mux-------" + videoFile);
		Movie video;
		try {
			video = new MovieCreator().build(videoFile);
			//Log.i("RecordVideo", "Video Create" + videoFile);
		} catch (RuntimeException e) {
			e.printStackTrace();
			//Log.i("RecordVideo", "Video RuntimeException-------");
			//Log.i("RecordVideo", "-------End Mux-------");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			//Log.i("RecordVideo", "-------Video IOException-------");
			//Log.i("RecordVideo", "-------End Mux-------");
			return false;
		}
		Movie audio;

		try {
			audio = new MovieCreator().build(audioFile);
			//Log.i("RecordVideo", "Audio Create " + audioFile);
		} catch (IOException e) {
			e.printStackTrace();
			//Log.i("RecordVideo", "-------Audio IOException-------");
			//Log.i("RecordVideo", "-------End Mux-------");
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			//Log.i("RecordVideo", "-------Audio NullPointerException-------");
			//Log.i("RecordVideo", "-------End Mux-------");
			return false;
		}
		Track audioTrack = audio.getTracks().get(0);
		video.addTrack(audioTrack);

		Container out = new DefaultMp4Builder().build(video);

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(outputFile);
			//Log.i("RecordVideo", "Output Create " + outputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//Log.i("RecordVideo", "-------Output FileNotFoundException-------");
			//Log.i("RecordVideo", "-------End Mux-------");
			return false;
		}
		BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
		try {
			out.writeContainer(byteBufferByteChannel);
			byteBufferByteChannel.close();
			fos.close();
			//Log.i("RecordVideo", "Output Write Contents " + outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			//Log.i("RecordVideo", "-------Output IOException-------");
			//Log.i("RecordVideo", "-------End Mux-------");
			return false;
		}

		//Log.i("RecordVideo", "-------End Mux-------");
		return true;
	}

	private void muxing(String src, String dst, int startMs, int endMs) throws IOException
	{
		MediaExtractor extractor = new MediaExtractor();
		extractor.setDataSource(src);
		int trackCount = extractor.getTrackCount();
		// Set up MediaMuxer for the destination.
		MediaMuxer muxer;
		muxer = new MediaMuxer(dst, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		// Set up the tracks and retrieve the max buffer size for selected
		// tracks.
		HashMap<Integer, Integer> indexMap = new HashMap<Integer,
				Integer>(trackCount);
		int bufferSize = -1;
		boolean useAudio = false, useVideo = true;

		for (int i = 0; i < trackCount; i++) {
			MediaFormat format = extractor.getTrackFormat(i);
			String mime = format.getString(MediaFormat.KEY_MIME);
			boolean selectCurrentTrack = false;
			if (mime.startsWith("audio/") && useAudio) {
				selectCurrentTrack = true;
			} else if (mime.startsWith("video/") && useVideo) {
				selectCurrentTrack = true;
			}
			if (selectCurrentTrack) {
				extractor.selectTrack(i);
				int dstIndex = muxer.addTrack(format);
				indexMap.put(i, dstIndex);
				if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
					int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
					bufferSize = newSize > bufferSize ? newSize : bufferSize;
				}
			}
		}
		if (bufferSize < 0) {
			bufferSize = 1 * 1024 * 1024;
		}
		// Set up the orientation and starting time for extractor.
		MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
		retrieverSrc.setDataSource(src);
		String degreesString = retrieverSrc.extractMetadata(
				MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
		if (degreesString != null) {
			int degrees = Integer.parseInt(degreesString);
			if (degrees >= 0) {
				muxer.setOrientationHint(degrees);
			}
		}

        /*if (startMs > 0) {
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }*/

		// Copy the samples from MediaExtractor to MediaMuxer. We will loop
		// for copying each sample and stop when we get to the end of the source
		// file or exceed the end time of the trimming.
		int offset = 0;
		int trackIndex = -1;
		ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
		BufferInfo bufferInfo = new BufferInfo();
		try {
			muxer.start();
			while (true) {
				bufferInfo.offset = offset;
				bufferInfo.size = extractor.readSampleData(dstBuf, offset);
				if (bufferInfo.size < 0) {
					bufferInfo.size = 0;
					break;
				} else {
					bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    /*if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000))
                    {
                        break;
                    }
                    else*/
					{
						bufferInfo.flags = extractor.getSampleFlags();
						trackIndex = extractor.getSampleTrackIndex();
						muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
								bufferInfo);
						extractor.advance();
					}
				}
			}
			muxer.stop();
		} catch (IllegalStateException e) {
			// Swallow the exception due to malformed source.
			e.printStackTrace();
		} finally {
			muxer.release();
		}
	}

	private static class BufferedWritableFileByteChannel implements WritableByteChannel {
		private static final int BUFFER_CAPACITY = 1000000;

		private boolean isOpen = true;
		private final OutputStream outputStream;
		private final ByteBuffer byteBuffer;
		private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

		private BufferedWritableFileByteChannel(OutputStream outputStream) {
			this.outputStream = outputStream;
			this.byteBuffer = ByteBuffer.wrap(rawBuffer);
		}

		@Override
		public boolean isOpen() {
			// TODO Auto-generated method stub
			return isOpen;
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			dumpToFile();
			isOpen = false;
		}

		@Override
		public int write(ByteBuffer buffer) throws IOException {
			// TODO Auto-generated method stub
			int inputBytes = buffer.remaining();

			if (inputBytes > byteBuffer.remaining()) {
				dumpToFile();
				byteBuffer.clear();

				if (inputBytes > byteBuffer.remaining()) {
					throw new BufferOverflowException();
				}
			}

			byteBuffer.put(buffer);
			return inputBytes;
		}

		private void dumpToFile() {
			try {
				outputStream.write(rawBuffer, 0, byteBuffer.position());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class Drawing extends Thread {
		public void run() {
			while (true)
			{
				if (isRecording)
				{
					if(totalRecordedTime < 30000) {
						if ((totalRecordedTime + (System.currentTimeMillis() - actionDownTime)) > VideoFilterCore.MAX_SEGMENT_TIME * 1000) {
							mGPUImage.stopVideoRecording();
							stopRecording();
							isRecording = false;

							totalRecordedTime += videoSegments.get(videoSegments.size() - 1).durationInMills;

							outputFilePath = mSaveDirectoryPath + "/output" + videoSegments.size() + ".mp4";
							videoFilePath = mSaveDirectoryPath + "/video" + videoSegments.size() + ".mp4";
							audioFilePath = mSaveDirectoryPath + "/audio" + videoSegments.size() + ".m4a";

							if (actionDownTime != 0)
								actionDownTime = 0;

							//mHandler.sendEmptyMessageDelayed(MIX_AUDIO_VIDEO, 300);
							Message msg = new Message();
							msg.arg1 = videoSegments.size();
							msg.what = MIX_AUDIO_VIDEO;
							mHandler.sendMessageDelayed(msg, 1000);
				/*
				 * AlertDialog code
				 */
							continue;
						}

						VideoSegment newVideoSeg = videoSegments.get(videoSegments.size() - 1);
						newVideoSeg.durationInMills = System.currentTimeMillis() - actionDownTime;
						videoSegments.set(videoSegments.size() - 1, newVideoSeg);

						segmentView.setSegmentList(videoSegments);
					}
				}

				mRefreshHandler.sendEmptyMessage(REFRESH_VIDEO_SEGMENT);

				try {
					Drawing.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initAudioRelatedValues()
	{
		if(mPlayer!=null)
		{
			try
			{
				mPlayer.stop();
				mPlayer.release();
			}catch(Exception e){e.printStackTrace();}
			finally{
				mPlayer = null;
			}
		}
		mIsPlaying = false;
		mFile = null;
		mLoadingKeepGoing = false;
	}

	/**
	 * Return extension including dot, like ".mp3"
	 */
	private String getExtensionFromFilename(String filename) {
		return filename.substring(filename.lastIndexOf('.'),
				filename.length());
	}

	private void loadAudioFromFile() {
		mFile = new File(audioFilePath);
		String mFilename = mFile.getName();
		String mExtension = getExtensionFromFilename(mFilename);

		SongMetadataReader metadataReader = new SongMetadataReader(
				this, mFilename);
		String mTitle = metadataReader.mTitle;
		String mArtist = metadataReader.mArtist;
		String mAlbum = metadataReader.mAlbum;
		int mYear = metadataReader.mYear;
		String mGenre = metadataReader.mGenre;

		String titleLabel = mTitle;
		if (mArtist != null && mArtist.length() > 0) {
			titleLabel += " - " + mArtist;
		}
		setTitle(titleLabel);

		mLoadingStartTime = System.currentTimeMillis();
		mLoadingLastUpdateTime = System.currentTimeMillis();

		mLoadingKeepGoing = true;
		mProgressDialog = new ProgressDialog(RecordFilterCameraActivity.this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setTitle(R.string.progress_dialog_loading);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(
				new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mLoadingKeepGoing = false;
					}
				});
		mProgressDialog.show();

		final CheapSoundFile.ProgressListener listener =
				new CheapSoundFile.ProgressListener() {
					public boolean reportProgress(double fractionComplete) {
						long now = System.currentTimeMillis();
						if (now - mLoadingLastUpdateTime > 100) {
							mProgressDialog.setProgress(
									(int)(mProgressDialog.getMax() *
											fractionComplete));
							mLoadingLastUpdateTime = now;
						}
						return mLoadingKeepGoing;
					}
				};

		// Create the MediaPlayer in a background thread
		mCanSeekAccurately = false;
		new Thread() {
			public void run() {
				mCanSeekAccurately = SeekTest.CanSeekAccurately(
						getPreferences(Context.MODE_PRIVATE));

				System.out.println("Seek test done, creating media player.");
				try {
					MediaPlayer player = new MediaPlayer();
					player.setDataSource(mFile.getAbsolutePath());
					player.setAudioStreamType(AudioManager.STREAM_MUSIC);
					player.prepare();
					mPlayer = player;
				} catch (final java.io.IOException e) {
					Runnable runnable = new Runnable() {
						public void run() {
							handleFatalError("ReadError", getResources().getText(R.string.read_error), e);
						}
					};
					mHandler.post(runnable);
				};
			}
		}.start();

		// Load the sound file in a background thread
		new Thread() {
			public void run() {
				try {
					mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(),
							listener);

					if (mSoundFile == null) {
						mProgressDialog.dismiss();
						String name = mFile.getName().toLowerCase();
						String[] components = name.split("\\.");
						String err;
						if (components.length < 2) {
							err = getResources().getString(
									R.string.no_extension_error);
						} else {
							err = getResources().getString(
									R.string.bad_extension_error) + " " +
									components[components.length - 1];
						}
						final String finalErr = err;
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								handleFatalError(
										"UnsupportedExtension",
										finalErr,
										new Exception());
							}
						};
						mHandler.post(runnable);
						return;
					}
				} catch (final Exception e) {
					mProgressDialog.dismiss();
					e.printStackTrace();
					//mInfo.setText(e.toString());

					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							handleFatalError(
									"ReadError",
									getResources().getText(R.string.read_error),
									e);
						}
					};
					mHandler.post(runnable);
					return;
				}
				mProgressDialog.dismiss();
				if (mLoadingKeepGoing) {
					Runnable runnable = new Runnable() {
						public void run() {
							finishOpeningSoundFile();
						}
					};
					mHandler.post(runnable);
				} else {
					RecordFilterCameraActivity.this.finish();
				}
			}
		}.start();
	}

	private void handleFatalError(
			final CharSequence errorInternalName,
			final CharSequence errorString,
			final Exception exception) {
		new AlertDialog.Builder(RecordFilterCameraActivity.this)
				.setTitle(errorInternalName)
				.setMessage(errorString)
				.setPositiveButton(
						R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
								dialog.dismiss();
								return;
							}
						})
				.setCancelable(true)
				.show();
	}

	private void finishOpeningSoundFile() {
		waveFormView.setSoundFile(mSoundFile, totalRecordedTime);
		waveFormView.recomputeHeights(mDensity);

		int scaled = (int)waveFormView.scaledVal;

		waveFormView.setZoomLevel(0);
		waveFormView.setPlayback(0);

		mMaxPos = (int)waveFormView.maxPos();
		mLastDisplayedStartPos = -1;
		mLastDisplayedEndPos = -1;

		mTouchDragging = false;

		mOffset = 0;
		mOffsetGoal = 0;
		mOffsetDefault = mOffset;
		mFlingVelocity = 0;
		resetPositions();

		if (mEndPos > mMaxPos)
			mEndPos = mMaxPos;

        /*mCaption =
            mSoundFile.getFiletype() + ", " +
            mSoundFile.getSampleRate() + " Hz, " +
            mSoundFile.getAvgBitrateKbps() + " kbps, " +
            formatTime(mMaxPos) + " " +
            getResources().getString(R.string.time_seconds);*/

		//mInfo.setText(mCaption);
		updateDisplay();
	}

	private void setOffsetGoalNoUpdate(int offset) {
		if (mTouchDragging) {
			return;
		}

		mOffsetGoal = offset;
		if (mOffsetGoal + mWidth / 2 > mMaxPos)
			mOffsetGoal = mMaxPos - mWidth / 2;
		if (mOffsetGoal < 0)
			mOffsetGoal = 0;
	}

	private synchronized void handlePause() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.pause();
		}
		waveFormView.setPlayback(-1);
		mIsPlaying = false;
	}

	private synchronized void updateDisplay() {

		if (mIsPlaying) {
			int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
			int frames = waveFormView.millisecsToPixels(now);
			waveFormView.setPlayback(frames);
			//setOffsetGoalNoUpdate(frames - mWidth);//  modify by lee.
			if (now >= mPlayEndMsec) {
				handlePause();
			}
		}

		if (!mTouchDragging) {
			int offsetDelta;

			if (mFlingVelocity != 0) {
				float saveVel = mFlingVelocity;

				offsetDelta = mFlingVelocity / 30;
				if (mFlingVelocity > 80) {
					mFlingVelocity -= 80;
				} else if (mFlingVelocity < -80) {
					mFlingVelocity += 80;
				} else {
					mFlingVelocity = 0;
				}

				mOffset += offsetDelta;

				if (mOffset + mWidth / 2 > mMaxPos) {
					mOffset = mMaxPos - mWidth / 2;
					mFlingVelocity = 0;
				}
				if (mOffset < mOffsetDefault) {
					mOffset = mOffsetDefault;
					mFlingVelocity = 0;
				}
				mOffsetGoal = mOffset;
			} else {
				offsetDelta = mOffsetGoal - mOffset;

				if (offsetDelta > 10)
					offsetDelta = offsetDelta / 10;
				else if (offsetDelta > 0)
					offsetDelta = 1;
				else if (offsetDelta < -10)
					offsetDelta = offsetDelta / 10;
				else if (offsetDelta < 0)
					offsetDelta = -1;
				else
					offsetDelta = 0;

				mOffset += offsetDelta;
			}
			if(waveFormView.pixelsToMillisecs(mOffset) > waveFormView.pixelsToMillisecs(mMaxPos))
				return;
		}

		waveFormView.setParameters(mStartPos, mEndPos, mOffset);
		waveFormView.invalidate();

        /*int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
	    if (!mStartVisible) {
		// Delay this to avoid flicker
		mHandler.postDelayed(new Runnable() {
			public void run() {
			    mStartVisible = true;
			    mStartMarker.setAlpha(255);
			}
		    }, 0);
	    }
		} else {
		    if (mStartVisible) {
		    	mStartMarker.setAlpha(0);
		    	mStartVisible = false;
		    }
	            startX = 0;
	        }

	        //int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
	        //if (endX + mEndMarker.getWidth() >= 0) {
		    if (!mEndVisible) {
			// Delay this to avoid flicker
			mHandler.postDelayed(new Runnable() {
				public void run() {
				    mEndVisible = true;
				    //mEndMarker.setAlpha(255);
				}
			    }, 0);
		    }
		} else {
		    if (mEndVisible) {
		    	mEndMarker.setAlpha(0);
		    	mEndVisible = false;
		    }
	        endX = 0;
	    }

	        mStartMarker.setLayoutParams(
	            new AbsoluteLayout.LayoutParams(
	                AbsoluteLayout.LayoutParams.WRAP_CONTENT,
	                AbsoluteLayout.LayoutParams.WRAP_CONTENT,
	                startX,
	                mMarkerTopOffset));

	        mEndMarker.setLayoutParams(
	            new AbsoluteLayout.LayoutParams(
	                AbsoluteLayout.LayoutParams.WRAP_CONTENT,
	                AbsoluteLayout.LayoutParams.WRAP_CONTENT,
	                endX,
	                mWaveformView.getMeasuredHeight() -
	                mEndMarker.getHeight() - mMarkerBottomOffset));*/
	}

	private void resetPositions() {
		mStartPos = waveFormView.secondsToPixels(0.0);
		mEndPos = waveFormView.secondsToPixels(1.0);
	}

	private int trap(int pos) {
		if (pos < 0)
			return 0;
		if (pos > mMaxPos)
			return mMaxPos;
		return pos;
	}

	@Override
	public void waveformTouchStart(float x) {
		// TODO Auto-generated method stub
		mTouchStart = x;
		mTouchInitialOffset = mOffset;
		mFlingVelocity = 0;
		mWaveformTouchStartMsec = System.currentTimeMillis();
	}

	@Override
	public void waveformTouchMove(float x, float scale) {
		// TODO Auto-generated method stub
		if(mPlayer != null) {
			if (mPlayer.isPlaying()) {
				cntr_aCounter.onFinish();
				cntr_aCounter.cancel();
			}
		}
		mTouchDragging = true;

		mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
		mOffsetDefault = Math.round(scale);
		if((mOffset + mWidth) > mMaxPos) {
			if (mMaxPos > mWidth)
				mOffset = mMaxPos - (int) mWidth;
		}
		updateDisplay();
	}


	@Override
	public void waveformTouchEnd(float x) {
		// TODO Auto-generated method stub
		if(mTouchDragging) {
			mTouchDragging = false;
			mOffsetGoal = mOffset;

			//long elapsedMsec = System.currentTimeMillis() - mWaveformTouchStartMsec;
			//if (elapsedMsec < 300) {
			if (mIsPlaying) {
				onPlay((int) (mOffset));
				/*if (seekMsec >= mPlayStartMsec &&
						seekMsec < mPlayEndMsec) {
					mPlayer.seekTo(seekMsec - mPlayStartOffset);

				} else {
					handlePause();
				}*/
			} else {
				onPlay((int) (mOffset));
			}
			//}
		}
	}


	@Override
	public void waveformFling(float x) {
		// TODO Auto-generated method stub
		mTouchDragging = false;
		mOffsetGoal = mOffset;
		mFlingVelocity = (int) (-x);
		updateDisplay();
	}


	@Override
	public void waveformDraw() {
		// TODO Auto-generated method stub
		mWidth = waveFormView.getMeasuredWidth();
		if (mOffsetGoal != mOffset)
			updateDisplay();
		else if (mIsPlaying) {
			updateDisplay();
		} else if (mFlingVelocity != 0) {
			updateDisplay();
		}
	}


	@Override
	public void waveformZoomIn() {
		// TODO Auto-generated method stub
		waveFormView.zoomIn();
		mStartPos = waveFormView.getStart();
		mEndPos = waveFormView.getEnd();
		mMaxPos = (int)waveFormView.maxPos();
		mOffset = waveFormView.getOffset();
		mOffsetGoal = mOffset;
		updateDisplay();
	}


	@Override
	public void waveformZoomOut() {
		// TODO Auto-generated method stub
		waveFormView.zoomOut();
		mStartPos = waveFormView.getStart();
		mEndPos = waveFormView.getEnd();
		mMaxPos = (int)waveFormView.maxPos();
		mOffset = waveFormView.getOffset();
		mOffsetGoal = mOffset;
		updateDisplay();
	}

	private synchronized void onPlay(int startPosition) {
		waveFormView.setPlayback(-1);

		if (mPlayer == null) {
			return;
		}

		try {
			mPlayStartMsec = waveFormView.pixelsToMillisecs(startPosition);
			if (startPosition < mStartPos) {
				mPlayEndMsec = waveFormView.pixelsToMillisecs(mStartPos);
			} else {//if (startPosition > mEndPos) {
				mPlayEndMsec = waveFormView.pixelsToMillisecs(mMaxPos);
			} /*else {
				mPlayEndMsec = waveFormView.pixelsToMillisecs(mEndPos);
			}*/

			mPlayStartOffset = 0;

			int startFrame = waveFormView.secondsToFrames(
					mPlayStartMsec * 0.001);
			int endFrame = waveFormView.secondsToFrames(
					mPlayEndMsec * 0.001);
			int startByte = mSoundFile.getSeekableFrameOffset(startFrame);
			int endByte = mSoundFile.getSeekableFrameOffset(endFrame);
			if (mCanSeekAccurately && startByte >= 0 && endByte >= 0) {
				try {
					mPlayer.reset();
					mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					FileInputStream subsetInputStream = new FileInputStream(mFile.getAbsolutePath());
					mPlayer.setDataSource(subsetInputStream.getFD(), startByte, endByte - startByte);
					mPlayer.prepare();
					mPlayStartOffset = mPlayStartMsec;
				} catch (Exception e) {
					System.out.println("Exception trying to play file subset");
					mPlayer.reset();
					mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mPlayer.setDataSource(mFile.getAbsolutePath());
					mPlayer.prepare();
					mPlayStartOffset = 0;
				}
			}

			/*mPlayer.setOnCompletionListener(new OnCompletionListener() {
				public synchronized void onCompletion(MediaPlayer arg0) {
					handlePause();
				}
			});*/
			mIsPlaying = true;

			if (mPlayStartOffset == 0) {
				mPlayer.seekTo(mPlayStartMsec);
			}
			//mPlayer.start();
			if(mMediaPlayer != null)
				mGPUImage.reset();
			playVideoAndAudio();
			updateDisplay();
		} catch (Exception e) {
			showFinalAlert(e, R.string.play_error);
			return;
		}
	}

	private void showFinalAlert(Exception e, int messageResourceId) {
		showFinalAlert(e, getResources().getText(messageResourceId));
	}
	/**
	 * Show a "final" alert dialog that will exit the activity
	 * after the user clicks on the OK button.  If an exception
	 * is passed, it's assumed to be an error condition, and the
	 * dialog is presented as an error, and the stack trace is
	 * logged.  If there's no exception, it's a success message.
	 */
	private void showFinalAlert(Exception e, CharSequence message) {
		CharSequence title;
		if (e != null) {
			//Log.e("Ringdroid", "Error: " + message);
			//Log.e("Ringdroid", getStackTrace(e));
			title = getResources().getText(R.string.alert_title_failure);
			setResult(RESULT_CANCELED, new Intent());
		} else {
			//Log.i("Ringdroid", "Success: " + message);
			title = getResources().getText(R.string.alert_title_success);
		}

		new AlertDialog.Builder(RecordFilterCameraActivity.this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(
						R.string.alert_ok_button,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
								finish();
							}
						})
				.setCancelable(false)
				.show();
	}

	private String getStackTrace(Exception e) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(stream, true);
		e.printStackTrace(writer);
		return stream.toString();
	}

	public class TranscdingBackground extends AsyncTask<String, Integer, Integer>
	{
		Activity _act;


		public TranscdingBackground(Activity act) {
			// TODO Auto-generated constructor stub
			_act = act;
		}


		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub

			PowerManager powerManager = (PowerManager)_act.getSystemService(Activity.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");

			wakeLock.acquire();
			videoFileName = System.currentTimeMillis()+".mp4";
			String commandStr = "ffmpeg -i "+mSaveDirectoryPath+"video.mp4 -i "+mSaveDirectoryPath+"audio.mp3 -c:v copy -c:a aac -strict experimental "+mSaveDirectoryPath+videoFileName;//"/final.mp4";
			//String commandStr = "ffmpeg -i "+mSaveDirectoryPath+"/video.mp4 -i "+mSaveDirectoryPath+"/audio.mp3 -c copy -shortest "+mSaveDirectoryPath+videoFileName;//"/final.mp4";
			LoadJNI vk = new LoadJNI();
			try {
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());

			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				if (wakeLock.isHeld())
					wakeLock.release();
			}
			return Integer.valueOf(0);
		}


		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			//deleteAllTempFiles();
			progressHUD.hide();
			Intent intent = new Intent();
			intent.putExtra("strMoviePath", mSaveDirectoryPath+videoFileName);
			setResult(Activity.RESULT_OK, intent);
			finish();
			super.onPostExecute(result);
		}


		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	}

	public class CutVideoBackground extends AsyncTask<String, Integer, Integer>
	{
		Activity _act;


		public CutVideoBackground(Activity act) {
			// TODO Auto-generated constructor stub
			_act = act;
		}


		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressHUD.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub

			PowerManager powerManager = (PowerManager)_act.getSystemService(Activity.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");

			wakeLock.acquire();

			int ss = getSegmentFrom() / 1000;
			int duration = (getSegmentTo() - getSegmentFrom()) / 1000;
			String commandStr = "";
			if(rotate.equals("90")) {
				commandStr = "ffmpeg -ss "+ss+" -i "+strPath + " -to "+
						duration+" -s 640*640 -c:v copy -c:a copy -strict experimental "+mSaveDirectoryPath + "/cutvideo.mp4";
			} else {
				if (effectIndex != 0)
					commandStr = "ffmpeg -ss " + ss + " -i " + strPath + " -to " +
							duration + " -s 640*640 -c:v copy -c:a copy -strict experimental " + mSaveDirectoryPath + "/cutvideo.mp4";
				else {
					videoFilePathName = mSaveDirectoryPath + "/" + System.currentTimeMillis() + ".mp4";
					commandStr = "ffmpeg -ss " + ss + " -i " + strPath + " -to " +
							duration + " -s 640*640 -c:v copy -c:a copy -strict experimental " + videoFilePathName;
				}
			}

			LoadJNI vk = new LoadJNI();
			try {
				vk.run(GeneralUtils.utilConvertToComplex(commandStr), workFolder, getApplicationContext());
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				if (wakeLock.isHeld())
					wakeLock.release();
			}
			return Integer.valueOf(0);
		}


		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//setMediaComposer(mSaveDirectoryPath + "/cutvideo.mp4");
			if(rotate.equals("90"))
				setMediaComposer(mSaveDirectoryPath + "/cutvideo.mp4"); //new VideoBackground(RecordFilterCameraActivity.this).execute();
			else {
				if(effectIndex != 0)
					setMediaComposer(mSaveDirectoryPath + "/cutvideo.mp4");//new RotateVideoBackground(RecordFilterCameraActivity.this).execute();
				else {
					progressHUD.hide();
					Intent intent = new Intent();
					intent.putExtra("strMoviePath", videoFilePathName);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			}
		}


		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	}

	public class VideoBackground extends AsyncTask<String, Integer, Integer>
	{
		Activity _act;


		public VideoBackground(Activity act) {
			// TODO Auto-generated constructor stub
			_act = act;
		}


		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressHUD.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub

			PowerManager powerManager = (PowerManager)_act.getSystemService(Activity.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");

			wakeLock.acquire();

			String commandRotate = "";
			/*if(effectIndex != 0)
				commandRotate = "ffmpeg -i "+mSaveDirectoryPath + "/cutVideo_1.mp4 -c copy -metadata:s:v rotate=90 "+mSaveDirectoryPath + "/cutvideo_1.mp4";
			else */{
				videoFilePathName = mSaveDirectoryPath + "/" + System.currentTimeMillis() + ".mp4";
				commandRotate = "ffmpeg -i "+mSaveDirectoryPath + "/cutVideo_1.mp4 -c copy -metadata:s:v rotate=90 "+videoFilePathName;
			}
			LoadJNI vk = new LoadJNI();
			try {
				vk.run(GeneralUtils.utilConvertToComplex(commandRotate), workFolder, getApplicationContext());
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				if (wakeLock.isHeld())
					wakeLock.release();
			}
			return Integer.valueOf(0);
		}


		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//setMediaComposer(mSaveDirectoryPath + "/cutvideo.mp4");
			/*if(effectIndex != 0)
				setMediaComposer(mSaveDirectoryPath + "/cutvideo_1.mp4");//new RotateVideoBackground(RecordFilterCameraActivity.this).execute();
			else */{
				progressHUD.hide();
				Intent intent = new Intent();
				intent.putExtra("strMoviePath", videoFilePathName);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}

		}


		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	}

	public class RotateVideoBackground extends AsyncTask<String, Integer, Integer>
	{
		Activity _act;


		public RotateVideoBackground(Activity act) {
			// TODO Auto-generated constructor stub
			_act = act;
		}


		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
		}

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub

			PowerManager powerManager = (PowerManager)_act.getSystemService(Activity.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");

			wakeLock.acquire();

			videoFilePathName = mSaveDirectoryPath+"/"+System.currentTimeMillis()+".mp4";

			//String commandRotate = "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -c copy -metadata:s:v rotate=90 "+videoFilePathName;

			String commandFilter = "";
			if(effectIndex != 0) {
				switch (effectIndex) {
					case 1:
						//commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target vcd -vf mp=eq2=2.0:1.0:0.0:0.9:0.85:0.85:1.0:0.6 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						//commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4  -strict experimental -vf curves=psfile=file:///android_asset/City.acv -c copy "+mSaveDirectoryPath+"/convert_tmp.mp4";
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4  -strict experimental -vf hue=s=0 -vcodec mpeg4  -acodec aac "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 2:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4  -vf mp=eq2=2.0:1.0:0.0:0.9:1.0:0.85:0.2:0.4 -c:v mpeg4 -c:a copy -strict experimental "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 3:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:0.6:0.6:0.8:0.1 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 4:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:1.2:1.0:0.9:0.8 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 5:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:0.1:0.8:1.3:0.4 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 6:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:0.6:0.8:1.0:0.9 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 7:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:1.0:0.85:0.95:0.5 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 8:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:1.0:0.95:0.95:0.9 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 9:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:0.93:0.85:0.95:0.2 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 10:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf mp=eq2=2.0:1.0:0.0:0.9:1.3:1.0:0.8:0.9 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 11:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target vcd -vf mp=eq2=2.0:1.0:0.0:0.9:0.93:0.94:0.78:0.9 "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
					case 12:
						commandFilter= "ffmpeg -i "+mSaveDirectoryPath + "/cutvideo.mp4 -target pal-vcd -vf format=gray "+mSaveDirectoryPath+"/convert_tmp.mp4";
						break;
				}
			}

			String commandConvert = "ffmpeg -i "+mSaveDirectoryPath + "/convert_tmp.mp4 -s 640*480 -vcodec libx264 -acodec aac -strict experimental "+videoFilePathName;

			LoadJNI vk = new LoadJNI();
			try {
				if(effectIndex != 0) {
					vk.run(GeneralUtils.utilConvertToComplex(commandFilter), workFolder, getApplicationContext());
					//vk.run(GeneralUtils.utilConvertToComplex(commandConvert), workFolder, getApplicationContext());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				if (wakeLock.isHeld())
					wakeLock.release();
			}
			return Integer.valueOf(0);
		}


		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progressHUD.hide();
			Intent intent = new Intent();
			intent.putExtra("strMoviePath", videoFilePathName);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}


		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	}

	@Override
	public void onStartPositionChanged(int position) {
		cameraSurfaceView.setBackground(null);
		if(!mMediaPlayer.isPlaying()) {
			mIsPlaying = true;
			mGPUImage.onPlay();
		}
		showPreview(position);
	}

	@Override
	public void onEndPositionChanged(int position) {
		cameraSurfaceView.setBackground(null);
		if(!mMediaPlayer.isPlaying()) {
			mIsPlaying = true;
			mGPUImage.onPlay();
		}
		showPreview(position);
	}

	@Override
	public void onMouseUp() {
		if(mMediaPlayer.isPlaying()) {
			mIsPlaying = false;
			mGPUImage.onStop(getSegmentFrom());
		}
	}

	public static String getTime(long milliSeconds, String dateFormat)
	{
		// Create a DateFormatter object for displaying date in specified format.
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

		// Create a calendar object that will convert the date and time value in milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}

	private void showPreview(int position) {
		if(!mIsOnlyFilter) return;
		if (mMediaPlayer == null) {
			return;
		}
		int seekTo = percentToPosition(position);
		//mMediaPlayer.seekTo((int)seekTo/1000);
		mGPUImage.setSeekTo(seekTo);
	}

	public int getSegmentFrom() {
		return percentToPosition(mSegmentSelector.getStartPosition());
	}

	public int getSegmentTo() {
		return percentToPosition(mSegmentSelector.getEndPosition());
	}

	private int percentToPosition(int percent) {
		mVideoDuration = mGPUImage.onGetDuration();
		int position = (int) (mVideoDuration * percent / 100);

		return position;
	}

	private void setCutVideoFunc(String src, String dst, int startMs, int endMs)
	{
		try {
			MediaExtractor extractor = new MediaExtractor();
			extractor.setDataSource(src);
			int trackCount = extractor.getTrackCount();
			// Set up MediaMuxer for the destination.
			MediaMuxer muxer;
			muxer = new MediaMuxer(dst, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			// Set up the tracks and retrieve the max buffer size for selected
			// tracks.
			HashMap<Integer, Integer> indexMap = new HashMap<Integer,
					Integer>(trackCount);
			int bufferSize = -1;
			boolean useAudio = false, useVideo = true;

			for (int i = 0; i < trackCount; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				boolean selectCurrentTrack = false;
				if (mime.startsWith("audio/") && useAudio) {
					selectCurrentTrack = true;
				} else if (mime.startsWith("video/") && useVideo) {
					selectCurrentTrack = true;
				}
				if (selectCurrentTrack) {
					extractor.selectTrack(i);
					int dstIndex = muxer.addTrack(format);
					indexMap.put(i, dstIndex);
					if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
						int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
						bufferSize = newSize > bufferSize ? newSize : bufferSize;
					}
				}
			}
			if (bufferSize < 0) {
				bufferSize = 1 * 1024 * 1024;
			}
			// Set up the orientation and starting time for extractor.
			MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
			retrieverSrc.setDataSource(src);

			String degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

			if (degreesString != null) {
				int degrees = Integer.parseInt(degreesString);
				if (degrees >= 0) {
					muxer.setOrientationHint(degrees);
				}
			}

			if (startMs > 0) {
				extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
			}

			// Copy the samples from MediaExtractor to MediaMuxer. We will loop
			// for copying each sample and stop when we get to the end of the source
			// file or exceed the end time of the trimming.
			int offset = 0;
			int trackIndex = -1;
			ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
			BufferInfo bufferInfo = new BufferInfo();
			try {
				muxer.start();
				while (true) {
					bufferInfo.offset = offset;
					bufferInfo.size = extractor.readSampleData(dstBuf, offset);
					if (bufferInfo.size < 0) {
						bufferInfo.size = 0;
						break;
					} else {
						bufferInfo.presentationTimeUs = extractor.getSampleTime();
						if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
							break;
						} else {
							bufferInfo.flags = extractor.getSampleFlags();
							trackIndex = extractor.getSampleTrackIndex();
							muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
							extractor.advance();
						}
					}
				}
				muxer.stop();
			} catch (IllegalStateException e) {
				// Swallow the exception due to malformed source.
				e.printStackTrace();
			} finally {
				muxer.release();
				extractor.release();
				retrieverSrc.release();
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void setMediaComposer(String url)
	{
		AndroidMediaObjectFactory factory = new AndroidMediaObjectFactory(getApplicationContext());
		MediaComposer mediaComposer = new MediaComposer(factory, progressListener);
		try
		{
			if(rotate.equals("90"))
				videoFilePathName = mSaveDirectoryPath+"/cutVideo_1.mp4";
			else
				videoFilePathName = mSaveDirectoryPath+"/"+System.currentTimeMillis()+".mp4";
			mediaComposer.addSourceFile(url);
			mediaComposer.setTargetFile(videoFilePathName);
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		configureVideoEncoder(mediaComposer, 640, 640);
		configureAudioEncoder(mediaComposer);
		if(effectIndex !=0)
			configureVideoEffect(mediaComposer, factory);
		mediaComposer.start();
	}

	private void configureVideoEffect(MediaComposer mediaComposer, AndroidMediaObjectFactory factory) {
		IVideoEffect effect = null;

		switch (effectIndex) {
			case 0:
				effect = new OriginalEffect(0, factory.getEglUtil()); //SepiaEffect(0, factory.getEglUtil());
				break;
			case 1:
				effect = new BookStoreEffect(0, factory.getEglUtil());
				break;
			case 2:
				effect = new CityEffect(0, factory.getEglUtil());
				break;
			case 3:
				effect = new CountryEffect(0, factory.getEglUtil());
				break;
			case 4:
				effect = new FilmEffect(0, factory.getEglUtil());
				break;
			case 5:
				effect = new ForestEffect(0, factory.getEglUtil());
				break;
			case 6:
				effect = new LakeEffect(0, factory.getEglUtil());
				break;
			case 7:
				effect = new MomentEffect(0, factory.getEglUtil());
				break;
			case 8:
				effect = new NYCEffect(0, factory.getEglUtil());
				break;
			case 9:
				effect = new TeaEffect(0, factory.getEglUtil());
				break;
			case 10:
				effect = new VintageEffect(0, factory.getEglUtil());
				break;
			case 11:
				effect = new QEffect(0, factory.getEglUtil());
				break;
			case 12:
				effect = new GrayScaleEffect(0, factory.getEglUtil());
				break;
		}

		if (effect != null) {
			effect.setSegment(new FileSegment(0l, 0l)); // Apply to the entire stream
			mediaComposer.addVideoEffect(effect);
		}
	}

	//process listener for video edit.
	public IProgressListener progressListener = new IProgressListener() {
		@Override
		public void onMediaStart() {
			try {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

					}
				});
			} catch (Exception e) {
			}
		}

		@Override
		public void onMediaProgress(float progress) {
			final float mediaProgress = progress;

			try {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
					}
				});
			} catch (Exception e) {
			}
		}

		@Override
		public void onMediaDone() {
			if(rotate.equals("90"))
				new VideoBackground(RecordFilterCameraActivity.this).execute(); //new RotateVideoBackground(RecordFilterCameraActivity.this).execute();
			else {
				//progressHUD.hide();
				Intent intent = new Intent();
				intent.putExtra("strMoviePath", videoFilePathName);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}

		@Override
		public void onMediaPause() {
			//progressHUD.hide();
			if(rotate.equals("90"))
				new VideoBackground(RecordFilterCameraActivity.this).execute(); //new RotateVideoBackground(RecordFilterCameraActivity.this).execute();
			else {
				Intent intent = new Intent();
				intent.putExtra("strMoviePath", videoFilePathName);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}

		@Override
		public void onMediaStop() {

			if(rotate.equals("90"))
				new VideoBackground(RecordFilterCameraActivity.this).execute(); //new RotateVideoBackground(RecordFilterCameraActivity.this).execute();
			else {
				//progressHUD.hide();
				Intent intent = new Intent();
				intent.putExtra("strMoviePath", videoFilePathName);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		}

		@Override
		public void onError(Exception exception) {
			try {
				final Exception e = exception;
				String str = e.getMessage();
				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
			}
		}
	};
	//Video param set
	protected void configureVideoEncoder(MediaComposer mediaComposer, int width, int height) {
		VideoFormatAndroid videoFormat;
		/*if(rotate.equals("0")) {
			videoFormat = new VideoFormatAndroid("video/avc", width, height);
		} else*/ {
			videoFormat = new VideoFormatAndroid("video/avc", width, height);
		}

		videoFormat.setVideoBitRateInKBytes(56);
		videoFormat.setVideoFrameRate(12);
		videoFormat.setVideoIFrameInterval(5);

		mediaComposer.setTargetVideoFormat(videoFormat);
	}
	//Audio set( detect audio from video file)
	protected void configureAudioEncoder(MediaComposer mediaComposer) {

		/**
		 * TODO: Audio resampling is unsupported by current m4m release
		 * Output sample rate and channel count are the same as for input.
		 */
		AudioFormatAndroid aFormat = new AudioFormatAndroid(audioMimeType, 44100, 2);

		aFormat.setAudioBitrateInBytes(audioBitRate);
		aFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

		mediaComposer.setTargetAudioFormat(aFormat);
	}

	//Video Edit( cut video and add effect)
	private class videoProcessTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressHUD.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			setCutVideoFunc(strPath, mSaveDirectoryPath + "/cutvideo.mp4", getSegmentFrom(), getSegmentTo());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Set title into TextView
			setMediaComposer(mSaveDirectoryPath + "/cutvideo.mp4");
		}
	}

	//AutoFocus set
	private void initSensor(){
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		/*List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s:deviceSensors)
			System.out.println(s.getType()+" "+s.getName()+" "+s.getVendor());*/
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
			// Use the accelerometer.
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}else if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		}else{
			// Sorry, there are no accelerometers on your device.
			// You can't play this game.
			//focusThread.start();
		}
	}
	private SensorEventListener sensorListener=new SensorEventListener(){

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			System.arraycopy(gNew, 0, gOld, 0, gOld.length);
			System.arraycopy(event.values,0,gNew,0,event.values.length);
			float delta=Math.abs(gNew[0]-gOld[0])+Math.abs(gNew[1]-gOld[1])
					+Math.abs(gNew[2]-gOld[2]);
			if(delta>MIN_GRA) {//&&!isFocus){
				focusRect.startAnimation(anim_focus);
			}
			//doAutoFocus();
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

	};
}