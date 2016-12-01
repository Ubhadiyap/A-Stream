package com.someoneman.youliveonstream.streamer.muxer;

import android.media.MediaCodec;

import com.someoneman.youliveonstream.streamer.encoders.Encoder;

import java.nio.ByteBuffer;

/**
 * Created by nik on 10.05.2016.
 */
public class WritePacketData {
    private final Encoder.Type _encoderType;
    private final ByteBuffer mData;
    private final MediaCodec.BufferInfo mBufferInfo;

    public WritePacketData(Encoder.Type encoderType, ByteBuffer data, MediaCodec.BufferInfo bufferInfo) {
        _encoderType = encoderType;
        mData=data;
        mBufferInfo = new MediaCodec.BufferInfo();
        mBufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
    }

    public Encoder.Type getEncoderIndex() {
        return _encoderType;
    }
    public ByteBuffer getData(){
        return mData;
    }
    public MediaCodec.BufferInfo getBufferInfo(){
        return mBufferInfo;
    }
}