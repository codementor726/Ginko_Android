package jp.co.cyberagent.android.gpuimage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

public class VideoRender implements Renderer, OnFrameAvailableListener {

	private static String TAG = "VideoRender";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
        // X, Y, Z, U, V
        -1.0f, -1.0f, 0, 0.f, 0.f,
        1.0f, -1.0f, 0, 1.f, 0.f,
        -1.0f,  1.0f, 0, 0.f, 1.f,
        1.0f,  1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;

    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uSTMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "  gl_Position = uMVPMatrix * aPosition;\n" +
            "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
            "}\n";

    private String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    int[] textures = new int[1];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private SurfaceTexture mSurface;
    private boolean updateSurface = false;
    public boolean isPrepared = false;
    private static int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private MediaPlayer mMediaPlayer = null;
    private int mFilterPos = 0;

    public static final int NO_IMAGE = -1;
    private int mGLTextureId = NO_IMAGE;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public VideoRender(Context context, final int position) {
        mFilterPos = position;
        setShader(position);
        mTriangleVertices = ByteBuffer.allocateDirect(
            mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public void setShader(int pos)
    {
        switch (pos)
        {
            case 0:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";
                break;
            case 1:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r * 0.85;\n" +
                        "  float colorG = color.g * 0.85;\n" +
                        "  float colorB = color.b;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.6);\n" +
                        "}\n";
                break;
            case 2:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r;\n" +
                        "  float colorG = color.g * 0.85;\n" +
                        "  float colorB = color.b * 0.2;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.4);\n" +
                        "}\n";
                break;
            case 3:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r * 0.6;\n" +
                        "  float colorG = color.g * 0.6;\n" +
                        "  float colorB = color.b * 0.8;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.1);\n" +
                        "}\n";
                break;
            case 4:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r * 1.2;\n" +
                        "  float colorG = color.g ;\n" +
                        "  float colorB = color.b * 0.9;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.9);\n" +
                        "}\n";
                break;
            case 5:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = (color.r + color.g + color.b) / 3.0;\n" +
                        "  float colorG = (color.r + color.g + color.b) / 2.0;\n" +
                        "  float colorB = (color.r + color.g + color.b) / 2.0;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
                        "}\n";
                break;
            case 6:  //Lake
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r * 0.6;\n" +
                        "  float colorG = color.g * 0.8;\n" +
                        "  float colorB = color.b;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.9);\n" +
                        "}\n";
                break;
            case 7:  //Moment
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r;\n" +
                        "  float colorG = color.g * 0.85;\n" +
                        "  float colorB = color.b * 0.95;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.5);\n" +
                        "}\n";
                break;
            case 8:  //NYC
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r;\n" +
                        "  float colorG = color.g * 0.95;\n" +
                        "  float colorB = color.b * 0.95;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.9);\n" +
                        "}\n";
                break;
            case 9:  //Tea
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r * 0.93;\n" +
                        "  float colorG = color.g * 0.85;\n" +
                        "  float colorB = color.b * 0.95;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, 0.2);\n" +
                        "}\n";
                break;
            case 10:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r*1.3;\n" +
                        "  float colorG = color.g;\n" +
                        "  float colorB = color.b*0.8;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
                        "}\n";
                break;
            case 11:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = color.r*0.93;\n" +
                        "  float colorG = color.g*0.94;\n" +
                        "  float colorB = color.b*0.78;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
                        "}\n";
                break;
            case 12:
                mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                        "  float colorR = (color.r + color.g + color.b) / 3.0;\n" +
                        "  float colorG = (color.r + color.g + color.b) / 3.0;\n" +
                        "  float colorB = (color.r + color.g + color.b) / 3.0;\n" +
                        "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
                        "}\n";
                break;

        }
    }
    public void setMediaPlayer(MediaPlayer player) {
        mMediaPlayer = player;
    }
    
    @Override
    public void onDrawFrame(GL10 glUnused) {
        synchronized(this) {
            if (updateSurface) {
                mSurface.updateTexImage();
                mSurface.getTransformMatrix(mSTMatrix);
                updateSurface = false;
            }
        }
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        //mFilter.onPreDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
        GLES20.glFinish();

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    }
    
    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                               GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                               GLES20.GL_LINEAR);

        /*
         * Create the SurfaceTexture that will feed this textureID,
         * and pass it to the MediaPlayer
         */
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);

        Surface surface = new Surface(mSurface);
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        surface.release();

        try {
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.seekTo(0);
        } catch (IOException t) {
            Log.e(TAG, "media player prepare failed");
        } catch (IllegalStateException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        synchronized(this) {
            updateSurface = false;
        }
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                if(mMediaPlayer.isPlaying())
                    mMediaPlayer.pause();
                isPrepared = true;
            }
        });

    }

    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurface = true;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public void onPlay(boolean bMute)
    {
    	/*if (mMediaPlayer.isPlaying())
    		return;*/
    	if (mMediaPlayer != null)
        {
            if (bMute)
                mMediaPlayer.setVolume(0, 0);
            else
                mMediaPlayer.setVolume(1, 1);

            mMediaPlayer.start();
        }
    }

    public long getDurationMedia()
    {
        long time = mMediaPlayer.getDuration();
        return time;
    }

    public void onSeekTo(int num)
    {
        /*mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);

        Surface surface = new Surface(mSurface);
        mMediaPlayer.setSurface(surface);
        //mMediaPlayer.setScreenOnWhilePlaying(true);
        surface.release();

        try {
            mMediaPlayer.prepare();
        } catch (IOException t) {
            Log.e(TAG, "media player prepare failed");
        } catch (IllegalStateException e){
            e.printStackTrace();
        }

        synchronized(this) {
            updateSurface = false;
        }*/

        mMediaPlayer.seekTo(num);
    }
    
    public void onStop(int reset)
    {
    	if (mMediaPlayer == null)
    		return;

        //mMediaPlayer.seekTo(reset);
        mMediaPlayer.pause();
        //mMediaPlayer.reset();
    }

    public void reset()
    {
        if (mMediaPlayer == null)
            return;
        mMediaPlayer.setVolume(0.0f, 0.0f);
        mMediaPlayer.seekTo(0);
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
    }
    
    public void destroy()
    {
    	GLES20.glDeleteTextures(1, textures, 0);
    	GLES20.glDeleteProgram(mProgram);

        mSurface.release();
        mSurface.setOnFrameAvailableListener(null);
    }
}
