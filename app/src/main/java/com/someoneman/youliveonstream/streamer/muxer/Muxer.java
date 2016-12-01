package com.someoneman.youliveonstream.streamer.muxer;

import android.media.MediaCodec;

import com.google.api.services.youtube.model.CdnSettings;
import com.someoneman.youliveonstream.streamer.encoders.Encoder;
import com.someoneman.youliveonstream.utils.Log;

import java.nio.ByteBuffer;

/**
 * Created by Aleksander on 17.02.2016.
 */
public class Muxer {
    private WritePacketData mAudioConfig;
    private WritePacketData mVideoConfig;
    private boolean mConfigRequred = true;
    private final Object mConfigFence = new Object();

    private final MuxerThread mMuxerThread;

    public Muxer(CdnSettings settings) {
        mMuxerThread = new MuxerThread(settings);
    }
    public void stop(){
       // mMuxerThread.stop();
    }

    public void start(){
        mMuxerThread.start();
    }

    private void saveConfig(Encoder.Type encoderType, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo){
        if (encoderType == Encoder.Type.VIDEO)
            mVideoConfig = new WritePacketData(encoderType, encodedData, bufferInfo);
        else if(encoderType == Encoder.Type.AUDIO)
            mAudioConfig = new WritePacketData(encoderType, encodedData,bufferInfo);
        else
            Log.D("%s: not an audio, nor video, what are you?", this.getClass().getName());
    }

    private boolean isCodecConfigPacket(MediaCodec.BufferInfo bufferInfo){
        return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
    }

    public void writeSampleData(Encoder.Type encoderType, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        if (encodedData.capacity() <= 0)
            return;
        MuxerHandler muxerHandler = mMuxerThread.getHandler();
        boolean isCodecConfigPacket = isCodecConfigPacket(bufferInfo);


        synchronized (mConfigFence) {
            if (isCodecConfigPacket) {
                ByteBuffer byteBuffer = mMuxerThread.getByteBufferForType(encoderType, encodedData.capacity());
                if(byteBuffer==null)
                    return;
                byteBuffer.put(encodedData);
                byteBuffer.position(0);
                saveConfig(encoderType, byteBuffer, bufferInfo);
                mConfigRequred = true;
            }
            if (mConfigRequred && muxerHandler != null && mAudioConfig != null && mVideoConfig != null) {
                muxerHandler.sendMessage(muxerHandler.obtainMessage(MuxerHandler.MSG_WRITE_DATA, mAudioConfig));
                muxerHandler.sendMessage(muxerHandler.obtainMessage(MuxerHandler.MSG_WRITE_DATA, mVideoConfig));
                mConfigRequred = false;
                if (isCodecConfigPacket)
                    return;
            }
            if (!mConfigRequred && muxerHandler != null) {
                ByteBuffer byteBuffer = mMuxerThread.getByteBufferForType(encoderType, encodedData.capacity());
                if(byteBuffer==null)
                    return;
                byteBuffer.put(encodedData);
                byteBuffer.position(0);
                muxerHandler.sendMessage(muxerHandler.obtainMessage(MuxerHandler.MSG_WRITE_DATA,
                        new WritePacketData(encoderType, byteBuffer, bufferInfo)));
            }
        }
    }
}