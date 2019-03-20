package com.ginko.activity.im;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;

public class VoiceMessagePlayer implements MediaPlayer.OnErrorListener , MediaPlayer.OnPreparedListener , MediaPlayer.OnCompletionListener{

    private Context mContext;
    public int msg_id;
    public String strVoiceFilePath = "";

    public boolean isPlaying = false;
    public int totalTimeDuration = 0;
    public int currentTimeProgress = 0;

    private VoiceMessagePlayerCallback playerCallback;

    private MediaPlayer mediaPlayer;

    private boolean isPrepared = false;

    private Handler mHandler;

    public VoiceMessagePlayer(Context context , Handler handler , String voiceFilePath)
    {
        this.mContext = context;
        this.mHandler = handler;
        this.strVoiceFilePath = voiceFilePath;
    }

    private void resetValues()
    {
        totalTimeDuration = 0;
        currentTimeProgress = 0;
        isPlaying = false;
        isPrepared = false;
    }

    private void releaseMediaPalyer()
    {
        if(mediaPlayer!=null)
        {
            try
            {
                if(mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            finally {
                mediaPlayer = null;
            }
        }
        resetValues();
    }

    public void registerVoicePlayerCallback(VoiceMessagePlayerCallback _callback)
    {
        playerCallback = _callback;
        //restore the current playing status to the voice player view callback
        if(playerCallback!=null && totalTimeDuration>0)
        {
            if(isPlaying)
                playerCallback.onStart();
            else
                playerCallback.onStop();
            playerCallback.onLoad(totalTimeDuration);
            playerCallback.onProgress(currentTimeProgress);
        } else
        {
            playerCallback.onStop();
            playerCallback.onLoad(0);
            playerCallback.onProgress(0);
        }

    }

    public boolean isPlaying(){return this.isPlaying;}

    public void start()
    {

        isPlaying = true;
        if(mediaPlayer == null)
        {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(this);

            try {
                mediaPlayer.setDataSource(strVoiceFilePath);
                this.mediaPlayer.setOnPreparedListener(this);
                this.mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer = null;
                if(playerCallback!=null)
                    playerCallback.onLoadFailed();
            }
        }
        else
        {
            if(isPrepared)
            {
                mediaPlayer.start();
                if(playerCallback!=null)
                    playerCallback.onStart();
                mHandler.postDelayed(playThread, 1000);
            }
            else
            {
                try {
                    mediaPlayer.setDataSource(strVoiceFilePath);
                    this.mediaPlayer.setOnPreparedListener(this);
                    this.mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.prepare();
                }catch(Exception e)
                {
                    e.printStackTrace();
                    mediaPlayer = null;
                    if(playerCallback!=null)
                        playerCallback.onLoadFailed();
                    releaseMediaPalyer();
                }
            }
        }

    }
    public void stop()
    {
        isPlaying = false;
        if(mediaPlayer!= null) {
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                currentTimeProgress = mediaPlayer.getCurrentPosition();
            }
        }
        if(playerCallback!=null)
            playerCallback.onStop();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(playerCallback!=null)
        {
            mHandler.post(new Runnable(){
                public void run(){
                    playerCallback.onLoadFailed();
                }});
        }
        releaseMediaPalyer();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        totalTimeDuration = mediaPlayer.getDuration();
        if(playerCallback!=null)
            playerCallback.onLoad(totalTimeDuration);

        if(currentTimeProgress>0) {
            mediaPlayer.seekTo(currentTimeProgress);
        }

        mediaPlayer.start();
        isPlaying = true;

        if (playerCallback != null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    playerCallback.onStart();
                }
            });
        }
        mHandler.postDelayed(playThread , 1000);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mHandler.removeCallbacks(playThread);

        if(playerCallback!=null)
            playerCallback.onCompletion();

        releaseMediaPalyer();

    }

    public interface VoiceMessagePlayerCallback
    {
        public void onStart();
        public void onStop();
        public void onProgress(int progressTime);
        public void onLoad(int timeDuaration);
        public void onLoadFailed();
        public void onCompletion();
    }


    Runnable playThread = new Runnable()
    {
        @Override
        public void run() {
            if(isPlaying)
            {
                if(mediaPlayer!=null && mediaPlayer.isPlaying())
                {
                    if(playerCallback!=null)
                    {
                        playerCallback.onProgress(mediaPlayer.getCurrentPosition());
                    }
                    mHandler.postDelayed(playThread , 1000);
                }
            }
        }
    };

}
