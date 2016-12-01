package com.someoneman.youliveonstream.streamer;

import java.nio.ByteBuffer;

/**
 * Created by Aleksander on 17.02.2016.
 */
public class FFMpegBridge {

    static {
        System.loadLibrary("ffmpeg-jni");
    }

    public native boolean init(String jRtmpUrl, int jWidth, int jHeight, int jBitrate);
    public native void setVideoCodecExtraData(byte[] jData, int jSize);
    public native void setAudioCodecExtraData(byte[] jData, int jSize);
    public native void writePacket(ByteBuffer jData, int jSize, long jPts, boolean jIsVideo, boolean jIsVideoKeyFrame);
    public native void writeHeader();
    public native void finallize();
    public static native Object allocNative(long size);
    public static native void freeNative(Object object);

}
