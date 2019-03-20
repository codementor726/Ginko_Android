package jp.co.cyberagent.android.gpuimage;

public interface FrameRecordListener {
	void onFrameRecorded(byte[] data , int width , int height);
	void onGLFrameRecorded(long memAddr , int width , int height);
}
