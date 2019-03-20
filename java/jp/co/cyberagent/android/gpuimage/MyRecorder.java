package jp.co.cyberagent.android.gpuimage;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.widget.Toast;

public class MyRecorder {
	//---------------------------------------------------------------------
	// MEMBERS
	//---------------------------------------------------------------------
	//private final VideoParam mVideoParam = VideoParam.getInstance();

	private static int	  	VIDEO_W = 480;
	private static int 	  	VIDEO_H = 320;
	private static final int      	FPS = 12;
	private static final String		MIME = "video/avc";
	private static final int		BPS = 20000; //56*1024; ///*4*1024*1024*/750000;
	private static final int 		IFI	= 5;

	private MediaCodec            mMediaCodec   = null;
	private InputSurface          mInputSurface = null;
	private MediaCodec.BufferInfo mBufferInfo   = null;
	private MediaMuxer            mMediaMuxer   = null;
	private int                   mTrackIndex   = -1;
	private boolean               mMuxerStarted = false;
	private int                   mTotalSize    = 0; //TODO: DEBUG
	private String				  mFilePath     = null;

	//---------------------------------------------------------------------
	// PUBLIC METHODS
	//---------------------------------------------------------------------
	public MyRecorder() {
	}

	public MyRecorder(String filePath, int width, int height) {
		VIDEO_W = width;
		VIDEO_H = height;
		mFilePath = filePath;
	}
	
	public void prepareEncoder() {
		if (mMediaCodec != null || mInputSurface != null) {
			throw new RuntimeException("prepareEncoder called twice?");
		}
		
		String mime = null;
		
		int codec_num = MediaCodecList.getCodecCount();
				
		for (int i = 0; i < codec_num; i ++)
		{
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			if (info.isEncoder())
			{
				final String[] mimes = info.getSupportedTypes();
				for (String m : mimes)
				{
					if (MIME.equals(m)) {
						mime = m;
					}
				}
			}
		}
		if (mime == null)
		{
			throw new UnsupportedOperationException(String.format("Not Support MIME: %s", MIME));
		}
		
		mBufferInfo = new MediaCodec.BufferInfo();
		
		try {
			/*MediaFormat format = MediaFormat.createVideoFormat(
					mime,
					VIDEO_W,
					VIDEO_H);*/
			MediaFormat format = MediaFormat.createVideoFormat(
					MIME,
					VIDEO_W,
					VIDEO_H);
			format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
			format.setInteger(MediaFormat.KEY_BIT_RATE, BPS);
			format.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
			format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFI);

			mMediaCodec = MediaCodec.createEncoderByType(MIME);
			mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

			mMediaMuxer = new MediaMuxer(mFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			mMuxerStarted = false;
		} catch (Exception e) {
			releaseEncoder();
			throw (RuntimeException)e;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////
	////////////----------Add by lee-----------------------///////////////////////////
	/**
	 * Returns the first codec capable of encoding the specified MIME type, or
	 * null if no match was found.
	 */
	private MediaCodecInfo selectCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

			if (!codecInfo.isEncoder()) {
				continue;
			}

			for (String type: codecInfo.getSupportedTypes()) {
				if (type.equalsIgnoreCase(mimeType)) {
					Log.i("selectCodec", "SelectCodec : " + codecInfo.getName());
					return codecInfo;
				}
			}
		}
		return null;
	}

	/**
	 * Retruns a color format that is supported by the codec and by this test
	 * code. If no match is found, this throws a test failure -- the set of
	 * formats known to the test should be expanded for new platforms.
	 */
	protected int selectColorFormat(String mimeType) {
		MediaCodecInfo codecInfo = selectCodec(mimeType);
		if (codecInfo == null) {
			throw new RuntimeException("Unable to find an appropriate codec for " + mimeType);
		}

		MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
		for (int i = 0; i < capabilities.colorFormats.length; i++) {
			int colorFormat = capabilities.colorFormats[i];
			if (isRecognizedFormat(colorFormat)) {
				Log.d("ColorFomar", "Find a good color format for " + codecInfo.getName() + " / " + mimeType);
				return colorFormat;
			}
		}
		return MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
	}

	/**
	 * Returns true if this is a color format that this test code understands
	 * (i.e. we know how to read and generate frames in this format).
	 */
	private boolean isRecognizedFormat(int colorFormat) {
		switch (colorFormat) {
			// these are the formats we know how to handle for this test
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
				return true;
			default:
				return false;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////////
	public boolean firstTimeSetup() {
		if (!isRecording() || mInputSurface != null) {
			return false;
		}
		try {
			mInputSurface = new InputSurface(mMediaCodec.createInputSurface());
			mMediaCodec.start();
			
		} catch (Exception e) {
			releaseEncoder();
			throw (RuntimeException)e;
		}
		return true;
	}

	public boolean isRecording() {
		return mMediaCodec != null;
	}

	public void makeCurrent() {
		if(mInputSurface == null)
			return;
		mInputSurface.makeCurrent();
	}

	synchronized public void swapBuffers() {
		if (!isRecording()) {
			return;
		}
		drainEncoder(false);
		mInputSurface.swapBuffers();
		mInputSurface.setPresentationTime(System.nanoTime());
	}

	synchronized public void stop() {
		drainEncoder(true);
		releaseEncoder();
		//new AsyncTaskForVideoProcess().execute();
	}

	//---------------------------------------------------------------------
	// PRIVATE...
	//---------------------------------------------------------------------
	private void releaseEncoder() {
		try {
			if (mMediaCodec != null) {
				mMediaCodec.stop();
				mMediaCodec.release();
				mMediaCodec = null;
			}
			if (mInputSurface != null) {
				mInputSurface.release();
				mInputSurface = null;
			}
			if (mMediaMuxer != null) {
				mMediaMuxer.stop();
				mMediaMuxer.release();
				mMediaMuxer = null;
			}
		}catch (IllegalStateException e){
			e.printStackTrace();
		}
	}

	private void drainEncoder(boolean endOfStream) {
		if (mMediaCodec == null) return;
		if (endOfStream) {
			try{
				mMediaCodec.signalEndOfInputStream();
			}catch(IllegalStateException e){
				e.printStackTrace();
			}
		}
		try {
			ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();

			while (true) {
				int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
				if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (!endOfStream) {
						break;
					}
				} else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					encoderOutputBuffers = mMediaCodec.getOutputBuffers();
				} else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (mMuxerStarted) {
						throw new RuntimeException("format changed twice");
					}
					MediaFormat newFormat = mMediaCodec.getOutputFormat();
					mTrackIndex = mMediaMuxer.addTrack(newFormat);
					mMediaMuxer.start();
					mMuxerStarted = true;
				} else {
					ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
					if (encodedData == null) {
						throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
					}
					if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						mBufferInfo.size = 0;
					}
					if (mBufferInfo.size != 0) {
						if (!mMuxerStarted) {
							throw new RuntimeException("muxer hasn't started");
						}
						encodedData.position(mBufferInfo.offset);
						encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

						boolean calc_time = true; //TODO: DEBUG
						if (calc_time) {
							long t0 = System.currentTimeMillis();
							mMediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
							mTotalSize += mBufferInfo.size;
							long dt = System.currentTimeMillis() - t0;
							if (dt>50) Log.e("DEBUG", String.format("XXX: dt=%d, size=%.2f",dt,(float)mTotalSize/1024/1024));
						} else {
							mMediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
						}
					}
					mMediaCodec.releaseOutputBuffer(encoderStatus, false);
					if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						break;
					}
				}
			}
		}catch (IllegalStateException e){
			e.printStackTrace();
		}
	}

	public class AsyncTaskForVideoProcess extends AsyncTask<Void, Void, Void> {
		public AsyncTaskForVideoProcess() {
			super();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			drainEncoder(true);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			releaseEncoder();
		}

	}
}
