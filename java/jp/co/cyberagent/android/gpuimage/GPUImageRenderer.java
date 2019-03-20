/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.view.Surface;

import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

@TargetApi(11)
public class GPUImageRenderer implements Renderer, PreviewCallback, SurfaceTexture.OnFrameAvailableListener {
    public static final int NO_IMAGE = -1;
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private GPUImageFilter mFilter;

    public final Object mSurfaceChangedWaiter = new Object();

    private int mGLTextureId = NO_IMAGE;
    private SurfaceTexture mSurfaceTexture = null;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private IntBuffer mGLRgbBuffer;

    private int mOutputWidth;
    private int mOutputHeight;
    private int mImageWidth;
    private int mImageHeight;
    private int mAddedPadding;

    private final Queue<Runnable> mRunOnDraw;
    private final Queue<Runnable> mRunOnDrawEnd;
    private Rotation mRotation;
    private boolean mFlipHorizontal;
    private boolean mFlipVertical;
    private GPUImage.ScaleType mScaleType = GPUImage.ScaleType.CENTER_CROP;
    private boolean isRecording = false;
	private int            mFboTexId     = -1;
	private boolean   mRecording = false;
	private MediaVideoEncoder mVideoEncoder;
	
	private boolean isShowingCamera = true;
    private boolean isRecordOneFrame = false;
    private Object recordLockObj = new Object();
    private FrameRecordListener frameRecordListener;
    
    private long glFrameBufferMemAddress = 0;
    private long frameTimeStamp = 0;
    private int recordedFrameCount = 0;
    private int cameraPreviewFPS = 0;
	private MediaPlayer mMediaPlayer = null;
	private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    public void setRecorder(MyRecorder recorder, int width, int height)
    {
    	/*synchronized (this) {
    		if (recorder != null)
    			mRenderSrfTex = new RenderSrfTex(
    					width, height, 
    					mFboTexId, 
    					recorder, 
    					mFilter.mVertexShader, 
    					mFilter.mFragmentShader);
    		else
    			mRenderSrfTex = null;
		}*/
    	synchronized (this) {
        	if (recorder != null){
        		mFilter.setVideoRecorder(width, height, recorder, true);
        		mRecording = true;
        	}
        	else{
        		mRecording = false;
        		mFilter.setVideoRecorder(width, height, recorder, false);
        	}
		}
    }
	    
	public void setVideoEncoder(final MediaVideoEncoder encorder)
    {
    	runOnDraw(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				synchronized(this) {
					if (encorder != null) {
						encorder.setEglContext(EGL14.eglGetCurrentContext(), mFboTexId);
					}
					
					mVideoEncoder = encorder;
				}
			}
		});
    }
	
    public GPUImageRenderer(final GPUImageFilter filter) {
        mFilter = filter;
        mRunOnDraw = new LinkedList<Runnable>();
        mRunOnDrawEnd = new LinkedList<Runnable>();

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
    }
    public void releaseGLFrameBuffer()
    {
    	GPUImageNativeLibrary.ReleaseCapturedFrame();
    	glFrameBufferMemAddress = 0;
    }
    public void setRecording(boolean _isRecording)
    {
    	synchronized(recordLockObj)
    	{
    		this.isRecording = _isRecording;
    	}
    }
    public void setOnFrameRecordListener(FrameRecordListener listener)
    {
    	frameRecordListener = listener;
    }
    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
        adjustImageScaling();
        synchronized (mSurfaceChangedWaiter) {
            mSurfaceChangedWaiter.notifyAll();
        }
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        synchronized (mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.poll().run();
            }
        }
        mFilter.onPreDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        
        if (mRecording)
        	mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        	//mRenderSrfTex.OnDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);

        /*if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }*/
    }
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        final Size previewSize = camera.getParameters().getPreviewSize();
        
        if (mGLRgbBuffer == null) {
            mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
            System.out.println("----Preview size("+String.valueOf(previewSize.width)+" , "+String.valueOf(previewSize.height));
            int fpsRange[] = {0 , 0};
            
            camera.getParameters().getPreviewFpsRange(fpsRange);
            cameraPreviewFPS = fpsRange[0];//minimum frame rate
            System.out.println("----Preview Frame Rate between " +fpsRange[0]+"  , "+fpsRange[1]);
        }
        if (mRunOnDraw.isEmpty()) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    GPUImageNativeLibrary.YUVtoRBGA(data, previewSize.width, previewSize.height,
                            mGLRgbBuffer.array());
                    mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, previewSize, mGLTextureId);
                    camera.addCallbackBuffer(data);

                    if (mImageWidth != previewSize.width) {
                        mImageWidth = previewSize.width;
                        mImageHeight = previewSize.height;
                        adjustImageScaling();
                    }
                }
            });
        }
        //if(isRecording)
        //{
        //	frameRecordListener.onFrameRecorded(data, previewSize.width, previewSize.height);
        //}
        /*synchronized(recordLockObj)
    	{
    		if(isRecording)
    		{
    			isRecordOneFrame = true;
    		}
    		else
    		{
    			isRecordOneFrame = false;
    		}
    	}*/
    }

    public void setUpSurfaceTexture(final Camera camera) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
            	isShowingCamera = true;
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                mFboTexId     = textures[0];
                mSurfaceTexture = new SurfaceTexture(mFboTexId);
                try {
                    if(camera != null) {
                        camera.setPreviewTexture(mSurfaceTexture);
                        camera.setPreviewCallback(GPUImageRenderer.this);
                        camera.startPreview();
                    }
                } catch (Exception e) {
                    camera.release();
                }
            }
        });
    }
    
    public void setUpSurfaceTexture(final MediaPlayer mp) {
    	runOnDraw(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				isShowingCamera = false;
				
				mMediaPlayer = mp;
				
				int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                mFboTexId     = textures[0];
                GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mFboTexId);
                GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                        GLES20.GL_NEAREST);
                GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                        GLES20.GL_LINEAR);
 
                mSurfaceTexture = new SurfaceTexture(mFboTexId);
				mSurfaceTexture.setOnFrameAvailableListener(GPUImageRenderer.this);
                Surface surface = new Surface(mSurfaceTexture);
                mMediaPlayer.setSurface(surface);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setVolume(0, 0);
                surface.release();

                try {
                    mMediaPlayer.prepare();
                } catch (IOException t) {
                    t.printStackTrace();
                }
            }
        });
    }

    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final GPUImageFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES20.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }

    public void deleteImage() {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glDeleteTextures(1, new int[]{
                        mGLTextureId
                }, 0);
                mGLTextureId = NO_IMAGE;
            }
        });
    }

    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap == null) {
            return;
        }

        runOnDraw(new Runnable() {

            @Override
            public void run() {
                Bitmap resizedBitmap = null;
                if (bitmap.getWidth() % 2 == 1) {
                    resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas(resizedBitmap);
                    can.drawARGB(0x00, 0x00, 0x00, 0x00);
                    can.drawBitmap(bitmap, 0, 0, null);
                    mAddedPadding = 1;
                } else {
                    mAddedPadding = 0;
                }

                mGLTextureId = OpenGlUtils.loadTexture(
                        resizedBitmap != null ? resizedBitmap : bitmap, mGLTextureId, recycle);
                if (resizedBitmap != null) {
                    resizedBitmap.recycle();
                }
                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();
                adjustImageScaling();
            }
        });
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return mOutputWidth;
    }

    protected int getFrameHeight() {
        return mOutputHeight;
    }

    private void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);
        if (mScaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void setRotationCamera(final Rotation rotation, final boolean flipHorizontal,
            final boolean flipVertical) {
        setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setRotation(final Rotation rotation) {
        mRotation = rotation;
        adjustImageScaling();
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        mFlipHorizontal = flipHorizontal;
        mFlipVertical = flipVertical;
        setRotation(rotation);
    }

    public Rotation getRotation() {
        return mRotation;
    }

    public boolean isFlippedHorizontally() {
        return mFlipHorizontal;
    }

    public boolean isFlippedVertically() {
        return mFlipVertical;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (mRunOnDrawEnd) {
            mRunOnDrawEnd.add(runnable);
        }
    }
	public void onPlay() {
		// TODO Auto-generated method stub
		
		if (mMediaPlayer.isPlaying())
			return;
		
		mMediaPlayer.seekTo(0);
		mMediaPlayer.start();
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		
	}
}
