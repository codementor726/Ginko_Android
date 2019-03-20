package com.ginko.activity.im;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.ginko.common.Logger;

import java.io.IOException;

public  class SoundMeter {
	static final private double EMA_FILTER = 0.6;

	private MediaRecorder mRecorder = null;
	private double mEMA = 0.0;

	public void start(String name) {
		if (!Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return;
		}
        Logger.debug("Start to record:file:" + name);
        if (mRecorder == null) {
			mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            boolean isSuperiorModel = false;

            if(Build.VERSION.SDK_INT >= 16) {
                isSuperiorModel = true;
            }

            if (isSuperiorModel == true) {
                try {
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                } catch (Exception e) {
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                }
            }
            else {
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            }

            //mRecorder.setAudioChannels(1);
            if (isSuperiorModel == true) {
                try {
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                } catch (Exception e) {
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                }
            }
            else {
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            }

            if (isSuperiorModel == true) {
                try {
                    mRecorder.setAudioEncodingBitRate(96000);
                } catch (Exception e) {
                }
            }

            if (isSuperiorModel == true) {
                boolean setAudioSamplingRateSucceed = false;
                try {
                    mRecorder.setAudioSamplingRate(96000);
                    setAudioSamplingRateSucceed = true;
                } catch (Exception e) {
                }
                if (setAudioSamplingRateSucceed == false) {
                    try {
                        mRecorder.setAudioSamplingRate(44100);
                    } catch (Exception e) {
                    }
                }
            }
			mRecorder.setOutputFile(name);
			try {
				mRecorder.prepare();
				mRecorder.start();
				
				mEMA = 0.0;
			} catch (IllegalStateException e) {
			    Logger.error(e);
			} catch (IOException e) {
                Logger.error(e);
			}

		}
	}

    /**
     * If record time is too short (<1s), the audio file wasn't created, so when stop(), throw exception.
     */
    public void stop() {
		if (mRecorder != null) {
            try{
                mRecorder.stop();
            }catch(RuntimeException stopException){
                Logger.error(stopException);
            }finally {
                mRecorder.release();
            }
			mRecorder = null;
		}
	}

	public void pause() {
		if (mRecorder != null) {
			mRecorder.stop();
		}
	}

	public void start() {
		if (mRecorder != null) {
			mRecorder.start();
		}
	}

	public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude() / 2700.0);
		else
			return 0;

	}

	public double getAmplitudeEMA() {
		double amp = getAmplitude();
		mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
		return mEMA;
	}
}
