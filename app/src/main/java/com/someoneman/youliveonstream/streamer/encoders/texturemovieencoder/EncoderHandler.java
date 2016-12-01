package com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder;

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Handles encoder state change requests.  The handler is created on the encoder thread.
 */
public class EncoderHandler extends Handler {

    public static final int MSG_START_RECORDING = 0;
    public static final int MSG_STOP_RECORDING = 1;
    public static final int MSG_FRAME_AVAILABLE = 2;
    public static final int MSG_SET_TEXTURE_ID = 3;
    public static final int MSG_UPDATE_SHARED_CONTEXT = 4;
    public static final int MSG_QUIT = 5;
    private static final String TAG = "TextureMovieEncoder";

    private WeakReference<TextureMovieEncoderThread> mWeakEncoder;

    public EncoderHandler(TextureMovieEncoderThread encoder) {
        mWeakEncoder = new WeakReference<>(encoder);
    }

    @Override  // runs on encoder thread
    public void handleMessage(Message inputMessage) {
        int what = inputMessage.what;
        Object obj = inputMessage.obj;

        TextureMovieEncoderThread encoder = mWeakEncoder.get();
        if (encoder == null) {
            Log.w(TAG, "EncoderHandler.handleMessage: encoder is null");
            return;
        }

        switch (what) {
            case MSG_START_RECORDING:
                encoder.handleStartRecording((EGLContext) obj);
                break;
            case MSG_STOP_RECORDING:
                encoder.handleStopRecording();
                break;
            case MSG_FRAME_AVAILABLE:
                long timestamp = (((long) inputMessage.arg1) << 32) |
                        (((long) inputMessage.arg2) & 0xffffffffL);
                encoder.handleFrameAvailable((float[]) obj, timestamp);
                break;
            case MSG_SET_TEXTURE_ID:
                encoder.handleSetTexture(inputMessage.arg1);
                break;
            case MSG_UPDATE_SHARED_CONTEXT:
                encoder.handleUpdateSharedContext((EGLContext) inputMessage.obj);
                break;
            case MSG_QUIT:
                Looper.myLooper().quit();
                break;
            default:
                throw new RuntimeException("Unhandled msg what=" + what);
        }
    }
}
