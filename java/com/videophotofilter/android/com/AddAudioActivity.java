package com.videophotofilter.android.com;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import com.ginko.ginko.R;
import com.ringdroid.soundfile.CheapSoundFile;

import com.videophotofilter.library.android.com.ImageUtil;
import com.videophotofilter.soundtrack.com.SeekTest;
import com.videophotofilter.soundtrack.com.SongMetadataReader;
import com.videophotofilter.soundtrack.com.WaveformView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddAudioActivity extends Activity implements OnClickListener ,
									WaveformView.WaveformListener{

	private ImageView btnPrev , btnApply;
	private ImageView imgBtnAddAudio;
	private RelativeLayout mAudioTrackLayout;
	private WaveformView waveFormView;
    private ProgressDialog mProgressDialog;
    private long mLoadingStartTime;
    private long mLoadingLastUpdateTime;
	
	//Variables
	private String audioFilePath = "";
	
	private final int SELECT_AUDIO = 1;
	private static final String TAG = "AudioTrack";

    private boolean mLoadingKeepGoing;
    private boolean mIsPlaying = false;
    private CheapSoundFile mSoundFile;
    private MediaPlayer mPlayer;
    private boolean mCanSeekAccurately;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private File mFile;
    private String mFilename;
    private String mDstFilename;
    private String mArtist;
    private String mAlbum;
    private String mGenre;
    private String mTitle;
    private int mYear;
    private String mExtension;
    private String mRecordingFilename;
    private int mNewFileKind;
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayStartOffset;
    private int mPlayEndMsec;
    
    private Handler mHandler;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addaudio);
		
		//init the audio-related variables
		initAudioRelatedValues();
        
		getUIObjects();
		
		mHandler = new Handler();
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
	private void getUIObjects()
	{
		btnPrev = (ImageView)findViewById(R.id.btnPrev); btnPrev.setOnClickListener(this);
		btnApply = (ImageView)findViewById(R.id.btnApply); btnApply.setOnClickListener(this);
		imgBtnAddAudio = (ImageView)findViewById(R.id.imgBtnAddAudio);imgBtnAddAudio.setOnClickListener(this);
		
		
		mAudioTrackLayout = (RelativeLayout)findViewById(R.id.audioTrackLayout);
		waveFormView = (WaveformView)findViewById(R.id.waveform);
		waveFormView.setListener(this);
		if(audioFilePath.equals(""))
			waveFormView.setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btnPrev:
			finish();
			break;
		case R.id.btnApply:
			
			break;
			
		case R.id.imgBtnAddAudio:
			//
			if(audioFilePath.equals(""))
			{
				//add audio
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("audio/*");
				Intent c = Intent.createChooser(i, "Select audiofile");
		        startActivityForResult(i, SELECT_AUDIO);  
			}
			else//delete audio
			{
				audioFilePath = "";
				imgBtnAddAudio.setImageResource(R.drawable.btn_musicadd);
				waveFormView.setVisibility(View.GONE);
    			initAudioRelatedValues();
			}
			break;
			
		}
	}
	
	@Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case SELECT_AUDIO:
                if (resultCode == RESULT_OK) {
                    String audioPath = ImageUtil.getRealPathFromURI(this , data.getData() , ImageUtil.MEDIA_TYPE_AUDIO);
                    System.out.println("---audio path= "+audioPath+"-------");
                    this.audioFilePath = audioPath;
                    if(audioFilePath.equals(""))
                    {
            			waveFormView.setVisibility(View.GONE);
            			imgBtnAddAudio.setImageResource(R.drawable.btn_musicadd);
            			initAudioRelatedValues();
                    }
                    else
                    {
                    	waveFormView.setVisibility(View.VISIBLE);
                    	imgBtnAddAudio.setImageResource(R.drawable.btn_musicdelete);
                    	loadAudioFromFile();
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
	

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
    protected void onPause() {
        super.onPause();
        
  
	}
	@Override
	protected void onDestroy() {
        Log.i("Ringdroid", "EditActivity OnDestroy");

        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        initAudioRelatedValues();
        
        super.onDestroy();
        
	 }
	
	private void loadAudioFromFile() {
        mFile = new File(audioFilePath);
        mFilename = mFile.getName();
        mExtension = getExtensionFromFilename(mFilename);

        SongMetadataReader metadataReader = new SongMetadataReader(
            this, mFilename);
        mTitle = metadataReader.mTitle;
        mArtist = metadataReader.mArtist;
        mAlbum = metadataReader.mAlbum;
        mYear = metadataReader.mYear;
        mGenre = metadataReader.mGenre;

        String titleLabel = mTitle;
        if (mArtist != null && mArtist.length() > 0) {
            titleLabel += " - " + mArtist;
        }
        setTitle(titleLabel);

        mLoadingStartTime = System.currentTimeMillis();
        mLoadingLastUpdateTime = System.currentTimeMillis();
        mLoadingKeepGoing = true;
        mProgressDialog = new ProgressDialog(AddAudioActivity.this);
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
                            handleFatalError(
                                "ReadError",
                                getResources().getText(R.string.read_error),
                                e);
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
                    AddAudioActivity.this.finish();
                }
            } 
        }.start();
    }
	private void handleFatalError(
            final CharSequence errorInternalName,
            final CharSequence errorString,
            final Exception exception) {
		    new AlertDialog.Builder(AddAudioActivity.this)
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
	private void resetPositions() {
        mStartPos = waveFormView.secondsToPixels(0.0);
        mEndPos = waveFormView.secondsToPixels(1.0);
    }
	private void finishOpeningSoundFile() {
        waveFormView.setSoundFile(mSoundFile, mWaveformTouchStartMsec);
        waveFormView.recomputeHeights(mDensity);

        mMaxPos = (int)waveFormView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
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
	private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
            int frames = waveFormView.millisecsToPixels(now);
            waveFormView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
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
                if (mOffset < 0) {
                    mOffset = 0;
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
	
	private String formatTime(int pixels) {
	    if (waveFormView != null && waveFormView.isInitialized()) {
	        return formatDecimal(waveFormView.pixelsToSeconds(pixels));
	    } else {
	        return "";
	    }
	}
	
	private String formatDecimal(double x) {
	    int xWhole = (int)x;
	    int xFrac = (int)(100 * (x - xWhole) + 0.5);
	
	    if (xFrac >= 100) {
	        xWhole++; //Round up
	        xFrac -= 100; //Now we need the remainder after the round up
	        if (xFrac < 10) {
	            xFrac *= 10; //we need a fraction that is 2 digits long
	        }
	    }
	
	    if (xFrac < 10)
	        return xWhole + ".0" + xFrac;
	    else
	        return xWhole + "." + xFrac;
	}
	
	private synchronized void handlePause() {
	    if (mPlayer != null && mPlayer.isPlaying()) {
	        mPlayer.pause();
	    }
	    waveFormView.setPlayback(-1);
	    mIsPlaying = false;
	}
	/**
     * Return extension including dot, like ".mp3"
     */
    private String getExtensionFromFilename(String filename) {
        return filename.substring(filename.lastIndexOf('.'),
                                  filename.length());
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
		mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = System.currentTimeMillis();
	}

	@Override
	public void waveformTouchMove(float x, float scale) {
		// TODO Auto-generated method stub
		mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
	}

	@Override
	public void waveformTouchEnd(float x) {
		// TODO Auto-generated method stub
		mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = System.currentTimeMillis() -
            mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = waveFormView.pixelsToMillisecs(
                    (int)(mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                    seekMsec < mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec - mPlayStartOffset);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int)(mTouchStart + mOffset));
            }
        }
	}

	@Override
	public void waveformFling(float vx) {
		// TODO Auto-generated method stub
		mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int)(-vx);
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
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
            mPlayStartMsec = waveFormView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = waveFormView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = waveFormView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = waveFormView.pixelsToMillisecs(mEndPos);
            }

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
                    FileInputStream subsetInputStream = new FileInputStream(
                        mFile.getAbsolutePath());
                    mPlayer.setDataSource(subsetInputStream.getFD(),
                                          startByte, endByte - startByte);
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

            mPlayer.setOnCompletionListener(new OnCompletionListener() {
                    public synchronized void onCompletion(MediaPlayer arg0) {
                        handlePause();
                    }
                });
            mIsPlaying = true;

            if (mPlayStartOffset == 0) {
                mPlayer.seekTo(mPlayStartMsec);
            }
            mPlayer.start();
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
            Log.e("Ringdroid", "Error: " + message);
            Log.e("Ringdroid", getStackTrace(e));
            title = getResources().getText(R.string.alert_title_failure);
            setResult(RESULT_CANCELED, new Intent());
        } else {
            Log.i("Ringdroid", "Success: " + message);
            title = getResources().getText(R.string.alert_title_success);
        }

        new AlertDialog.Builder(AddAudioActivity.this)
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
}
