package com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder;

import android.opengl.EGLContext;
import android.os.Looper;

import com.someoneman.youliveonstream.streamer.ACamera;
import com.someoneman.youliveonstream.streamer.encoders.VideoEncoder;
import com.someoneman.youliveonstream.streamer.gles.EglCore;
import com.someoneman.youliveonstream.streamer.gles.FullFrameRect;
import com.someoneman.youliveonstream.streamer.gles.Texture2dProgram;
import com.someoneman.youliveonstream.streamer.gles.WindowSurface;
import com.someoneman.youliveonstream.streamer.muxer.Muxer;
import com.someoneman.youliveonstream.utils.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nik on 6/12/16.
 */
public class TextureMovieEncoderThread implements Runnable {
    // ----- accessed exclusively by encoder thread -----
    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private FullFrameRect mFullScreen;
    private int mTextureId;
    private VideoEncoder mVideoEncoder;
    private Muxer mMuxer;

    private AtomicBoolean mReadyToHandle = new AtomicBoolean();
    private EncoderHandler mHandler;

    public TextureMovieEncoderThread(Muxer muxer){
        mMuxer = muxer;
    }

    public void start(){
        new Thread(this, "TextureMovieEncoder").start();
    }

    /**
     * Encoder thread entry point.  Establishes Looper/Handler and waits for messages.
     * <p/>
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        // Establish a Looper for this thread, and define a Handler for it.
        Looper.prepare();
        mHandler = new EncoderHandler(this);
        mReadyToHandle.set(true);
        Looper.loop();
        mReadyToHandle.set(false);
    }

    /**
     * Starts recording.
     */
    void handleStartRecording(EGLContext sharingContext) {
        prepareEncoder(sharingContext);
    }

    /**
     * Handles notification of an available frame.
     * <p/>
     * The texture is rendered onto the encoder's input surface, along with a moving
     * box (just because we can).
     * <p/>
     *
     * @param transform      The texture transform, from SurfaceTexture.
     * @param timestampNanos The frame's timestamp, from SurfaceTexture.
     */
    public void handleFrameAvailable(float[] transform, long timestampNanos) {
        mVideoEncoder.drainEncoder();
        mFullScreen.drawFrame(mTextureId, transform);
        mInputWindowSurface.setPresentationTime(timestampNanos);
        mInputWindowSurface.swapBuffers();
    }

    /**
     * Handles a request to stop encoding.
     */
    void handleStopRecording() {
        Log.D("handleStopRecording");
        mVideoEncoder.stop();
        mVideoEncoder.drainEncoder();
        releaseEncoder();
    }

    /**
     * Sets the texture name that SurfaceTexture will use when frames are received.
     */
    void handleSetTexture(int id) {
        mTextureId = id;
    }

    /**
     * Tears down the EGL surface and context we've been using to feed the MediaCodec input
     * surface, and replaces it with a new one that shares with the new context.
     * <p/>
     * This is useful if the old context we were sharing with went away (maybe a GLSurfaceView
     * that got torn down) and we need to hook up with the new one.
     */
    void handleUpdateSharedContext(EGLContext newSharedContext) {
        Log.D("handleUpdatedSharedContext " + newSharedContext);

        mInputWindowSurface.releaseEglSurface();
        mFullScreen.release(false);
        mEglCore.release();

        // Create a new EGLContext and recreate the window surface.
        mEglCore = new EglCore(newSharedContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface.recreate(mEglCore);
        mInputWindowSurface.makeCurrent();

        // Create new programs and such for the new context.
        mFullScreen = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
    }

    private void prepareEncoder(EGLContext sharedContext) {
        try {
            mVideoEncoder = new VideoEncoder(mMuxer, ACamera.GetInstance().getVideoSize());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        mEglCore = new EglCore(sharedContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface = new WindowSurface(mEglCore, mVideoEncoder.getInputSurface(), true);
        mInputWindowSurface.makeCurrent();

        mFullScreen = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
    }

    private void releaseEncoder() {

        //mVideoEncoder.release();
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);
            mFullScreen = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    //We need to access the encoderHandler, for instance to capture frames
    public EncoderHandler getHandler(){
        if(mReadyToHandle.get()) {
            return mHandler;
        }
        return null;
    }
}
