// started from https://github.com/google/grafika/blob/f3c8c3dee60153f471312e21acac8b3a3cddd7dc/src/com/android/grafika/TextureMovieEncoder.java

/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;

import com.someoneman.youliveonstream.streamer.muxer.Muxer;
import com.someoneman.youliveonstream.utils.Log;

/**
 * Encode a movie from frames rendered from an external texture image.
 * <p/>
 * The object wraps an encoder running on a dedicated thread.  The various control messages
 * may be sent from arbitrary threads (typically the app UI thread).  The encoder thread
 * manages both sides of the encoder (feeding and draining); the only external input is
 * the GL texture.
 * <p/>
 * The design is complicated slightly by the need to create an EGL context that shares state
 * with a view that gets restarted if (say) the device orientation changes.  When the view
 * in question is a GLSurfaceView, we don't have full control over the EGL context creation
 * on that side, so we have to bend a bit backwards here.
 * <p/>
 * To use:
 * <ul>
 * <li>create TextureMovieEncoder object
 * <li>create an EncoderConfig
 * <li>call TextureMovieEncoder#startRecording() with the config
 * <li>call TextureMovieEncoder#setTextureId() with the texture object that receives frames
 * <li>for each frame, after latching it with SurfaceTexture#updateTexImage(),
 * call TextureMovieEncoder#frameAvailable().
 * </ul>
 * <p/>
 * TODO: tweak the API (esp. textureId) so it's less awkward for simple use cases.
 */
public class TextureMovieEncoder {

    private EGLContext _sharingContext;
    private Boolean mSharedContextMessageSended = false;
    private boolean mRunning;
    private final TextureMovieEncoderThread mTextureMovieEncoderThread;

    //That calls from anywhere initialization is, not really important since we can't use object was't initialized
    public TextureMovieEncoder(Muxer muxer) {
        mTextureMovieEncoderThread = new TextureMovieEncoderThread(muxer);
    }

    //region calls from render thread
    /**
     * Sends sharing context to an encoder thread, if it's hasn't had one
     */
    void sendSharedContextIfNeeded(){
        if(mSharedContextMessageSended){
            return;
        }
        EncoderHandler handler = mTextureMovieEncoderThread.getHandler();
        if(handler==null){
            return;
        }
        handler.sendMessage(handler.obtainMessage(EncoderHandler.MSG_START_RECORDING, _sharingContext));
        mSharedContextMessageSended=true;

    }

    public void startRecording(EGLContext sharingContext) {
        mTextureMovieEncoderThread.start();
        _sharingContext=sharingContext;
        sendSharedContextIfNeeded();
        mRunning=true;
    }



    /**
     * Tells the video recorder to stop recording.  (Call from non-encoder thread.)
     * <p/>
     * Returns immediately; the encoder/muxer may not yet be finished creating the movie.
     * <p/>
     * TODO: have the encoder thread invoke a callback on the UI thread just before it shuts down
     * so we can provide reasonable status UI (and let the caller know that movie encoding
     * has completed).
     */
    public void stopRecording() {
        EncoderHandler handler = mTextureMovieEncoderThread.getHandler();
        //That's prety bad, since TextureMovieEncoder Won't Stop
        if(handler==null){
            return;
        }
        handler.sendMessage(handler.obtainMessage(EncoderHandler.MSG_STOP_RECORDING));
        handler.sendMessage(handler.obtainMessage(EncoderHandler.MSG_QUIT));
        // We don't know when these will actually finish (or even start).  We don't want to
        // delay the UI thread though, so we return immediately.
    }

    /**
     * Returns true if recording has been started.
     */
    public boolean isRecording() {
        return mRunning;
    }

    /**
     * Tells the video recorder to refresh its EGL surface.  (Call from non-encoder thread.)
     */
    public void updateSharedContext(EGLContext sharedContext) {
        EncoderHandler handler = mTextureMovieEncoderThread.getHandler();
        if(handler==null) {
            _sharingContext=sharedContext;
        }
        else {
            sendSharedContextIfNeeded();
            handler.sendMessage(handler.obtainMessage(EncoderHandler.MSG_UPDATE_SHARED_CONTEXT, sharedContext));
        }
    }

    /**
     * Tells the video recorder that a new frame is available.  (Call from non-encoder thread.)
     * <p/>
     * This function sends a message and returns immediately.  This isn't sufficient -- we
     * don't want the caller to latch a new frame until we're done with this one -- but we
     * can get away with it so long as the input frame rate is reasonable and the encoder
     * thread doesn't stall.
     * <p/>
     * TODO: either block here until the texture has been rendered onto the encoder surface,
     * or have a separate "block if still busy" method that the caller can execute immediately
     * before it calls updateTexImage().  The latter is preferred because we don't want to
     * stall the caller while this thread does work.
     */
    public void frameAvailable(int id,SurfaceTexture st) {
        //If handler is not ready somehow, just skip the frame;
        EncoderHandler handler = mTextureMovieEncoderThread.getHandler();
        if(handler==null){
            return;
        }
        sendSharedContextIfNeeded();
        handler.sendMessage(handler.obtainMessage(EncoderHandler.MSG_SET_TEXTURE_ID, id, 0, null));

        float[] transform = new float[16];      // TODO - avoid alloc every frame
        st.getTransformMatrix(transform);
        long timestamp = st.getTimestamp();
        if (timestamp == 0) {
            // Seeing this after device is toggled off/on with power button.  The
            // first frame back has a zero timestamp.
            //
            // MPEG4Writer thinks this is cause to abort() in native code, so it's very
            // important that we just ignore the frame.
            Log.W("HEY: got SurfaceTexture with timestamp of zero");
            return;
        }

        handler.sendMessage(handler.obtainMessage(EncoderHandler.MSG_FRAME_AVAILABLE,
                (int) (timestamp >> 32), (int) timestamp, transform));
    }
    //endregion

}