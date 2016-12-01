package com.someoneman.youliveonstream.streamer.encoders;

import android.media.MediaCodec;

import com.someoneman.youliveonstream.streamer.muxer.Muxer;

import java.nio.ByteBuffer;

/**
 * Created by Aleksander on 17.02.2016.
 */
public abstract class Encoder implements Runnable {

    public enum Type {VIDEO, AUDIO, ENDED};
    protected MediaCodec mMediaCodec;
    protected boolean mEncodingRequested = false, mEndRequest = false;

    protected final Muxer mMuxer;
    protected final MediaCodec.BufferInfo mBufferInfo;
    protected final Object mEncodingFence = new Object();
    protected final Type mType;

    public Encoder(Muxer muxer, Type type) {
        mMuxer = muxer;
        mType = type;
        mBufferInfo = new MediaCodec.BufferInfo();
    }

    @Override
    public void run() {
        synchronized (mEncodingFence) {
            while (!mEncodingRequested) {
                try {
                    mEncodingFence.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        doInThread();
    }

    protected void startThread(String threadName) {
        Thread encoderThread = new Thread(this, threadName);

        encoderThread.setPriority(Thread.MAX_PRIORITY);
        encoderThread.start();
    }
    public void drainEncoder() {

        final int TIMEOUT_USEC = 1000;

        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();

        while (true) {
            int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!mEndRequest)
                    break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

            } else if (encoderStatus < 0) {

            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null)
                    throw new RuntimeException("encodedOutputBuffer " + encoderStatus + " was null");
                sendEncodedDataToMuxer(encodedData);
                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
                System.gc();
                System.gc();
            }

            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }
        }
    }
    private void sendEncodedDataToMuxer(ByteBuffer encodedData){

        if (mBufferInfo.size >= 0) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

            if (mEndRequest) {
                mEncodingRequested = false;

                mBufferInfo.flags = mBufferInfo.flags | MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }
            synchronized (mMuxer) {
                mMuxer.writeSampleData(mType, encodedData, mBufferInfo);
            }
        }
    }


    public Type getType() {
        return mType;
    }

    public void start(String threadName) {
        startThread(threadName);
        mEncodingRequested = true;
    }

    public void stop() {
        if (mEncodingRequested && !mEndRequest) {
            mEndRequest = true;
        }
    }

    protected abstract void doInThread();
}