package com.someoneman.youliveonstream.streamer.muxer;

import android.content.Intent;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.IngestionInfo;
import com.someoneman.youliveonstream.App;
import com.someoneman.youliveonstream.streamer.FFMpegBridge;
import com.someoneman.youliveonstream.streamer.encoders.Encoder;
import com.someoneman.youliveonstream.utils.Log;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nik on 6/5/16.
 */
public class MuxerThread implements Runnable {
    public static final String BROADCAST_ACTION = "muxer_broadcast_action";
    public static final String BROADCAST_MESSAGE = "muxer_broadcast_message";
    public static final String BROADCAST_STATE = "muxer-broadcast-state";

    public static final int STATE_NOT_INIT = 0;
    public static final int STATE_INIT = 1;
    public static final int STATE_RTMP_CONNECTED = 2;
    public static final int STATE_RTMP_CLOSED = 3;
    public static final int STATE_ERROR = -1;

    private MuxerHandler mHandler;
    private FFMpegBridge mFFMpegBridge;
    private CdnSettings mSettings;
    private int mH264MetaSize;
    private ByteBuffer mH264KeyFrame;
    private byte[] mVideoConfig;
    private byte[] mAudioConfig;

    private final int mDropLimit=60;

    private int atomicFFMPEGVideoQueueCount=0;
    private int atomicFFMPEGAudioQueueCount=0;

    private final AtomicBoolean mReadyToHandle = new AtomicBoolean(false);
    private final ArrayDeque<ByteBuffer> sharedByteBufferVideoArray = new ArrayDeque<>();
    private final ArrayDeque<ByteBuffer> sharedByteBufferAudioArray = new ArrayDeque<>();

    public MuxerThread(CdnSettings settings){
        mSettings = settings;
    }

    private void startThread() {
        Thread thread = new Thread(this, "Muxer");
        thread.start();
    }
    public ByteBuffer getByteBufferForType(Encoder.Type encoderType, int encoderDataCapacity){
        if(encoderType== Encoder.Type.AUDIO){
            synchronized (sharedByteBufferAudioArray){
                atomicFFMPEGAudioQueueCount++;
                if (!sharedByteBufferAudioArray.isEmpty()) {
                    return sharedByteBufferAudioArray.remove();
                } else {
                    return ByteBuffer.allocateDirect(encoderDataCapacity);
                }
            }
        }
        else if(encoderType == Encoder.Type.VIDEO){
            synchronized (sharedByteBufferVideoArray){
                atomicFFMPEGVideoQueueCount++;
                if (!sharedByteBufferVideoArray.isEmpty()) {
                    return sharedByteBufferVideoArray.remove();
                } else {
                    return ByteBuffer.allocateDirect(encoderDataCapacity);
                }
            }
        }
        return null;
    }


    private boolean isCodecConfigPacket(MediaCodec.BufferInfo bufferInfo){
        return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
    }
    private boolean isKeyFramePacket(MediaCodec.BufferInfo bufferInfo){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
        }
        else{
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
        }
    }

    public void start() {
        startThread();
    }

    @Override
    public void run() {
        Looper.prepare();
        IngestionInfo info = mSettings.getIngestionInfo();
        //String rtmp = String.format("%s/%s", info.getBackupIngestionAddress(), info.getStreamName());
        String rtmp = String.format("%s/%s", info.getIngestionAddress(), info.getStreamName());
        mFFMpegBridge = new FFMpegBridge();
        mFFMpegBridge.init(
                rtmp,
                1280,
                720,
                2500
        );
        mHandler = new MuxerHandler(this);
        mReadyToHandle.set(true);

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(BROADCAST_MESSAGE, BROADCAST_STATE);
        intent.putExtra(BROADCAST_STATE, STATE_RTMP_CONNECTED);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
        Looper.loop();
    }

    private void sendKeyFrame(Encoder.Type encoderType, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo){
        mH264KeyFrame.position(mH264MetaSize);
        mH264KeyFrame.put(encodedData);

        mFFMpegBridge.writePacket(
                mH264KeyFrame,
                bufferInfo.size + mH264MetaSize,
                bufferInfo.presentationTimeUs,
                (encoderType == Encoder.Type.VIDEO),
                true
        );
    }
    private void sendOrdinaryFrame(Encoder.Type encoderType, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo){
        mFFMpegBridge.writePacket(
                encodedData,
                bufferInfo.size,
                bufferInfo.presentationTimeUs,
                (encoderType == Encoder.Type.VIDEO),
                false
        );
    }

    private void releaseBufferForType(ByteBuffer packet, Encoder.Type packetType){
        packet.clear();
        if(packetType == Encoder.Type.AUDIO){
            synchronized (sharedByteBufferAudioArray){
                atomicFFMPEGAudioQueueCount--;
                sharedByteBufferAudioArray.add(packet);
            }
        }
        else if(packetType == Encoder.Type.VIDEO){
            synchronized (sharedByteBufferVideoArray){
                atomicFFMPEGVideoQueueCount--;
                sharedByteBufferVideoArray.add(packet);
            }
        }
    }

    void handleWriteSampleData(WritePacketData packetData) {
        ByteBuffer encodedData = packetData.getData();
        MediaCodec.BufferInfo bufferInfo = packetData.getBufferInfo();
        Encoder.Type encoderType = packetData.getEncoderIndex();

        if(isCodecConfigPacket(bufferInfo)){
            if (encoderType == Encoder.Type.VIDEO) {
                mH264MetaSize = bufferInfo.size;
                mH264KeyFrame = ByteBuffer.allocateDirect(encodedData.capacity());
                mVideoConfig = new byte[bufferInfo.size];
                encodedData.get(mVideoConfig, bufferInfo.offset, bufferInfo.size);
                mFFMpegBridge.setVideoCodecExtraData(mVideoConfig,mVideoConfig.length);
            } else {
                mAudioConfig = new byte[bufferInfo.size];
                encodedData.get(mAudioConfig, bufferInfo.offset, bufferInfo.size);
                mFFMpegBridge.setAudioCodecExtraData(mAudioConfig,mAudioConfig.length);
            }
            if(mVideoConfig!=null&&mAudioConfig!=null){
                mFFMpegBridge.writeHeader();
            }
            releaseBufferForType(encodedData,encoderType);
            return;
        }
        encodedData.position(bufferInfo.offset);
        encodedData.limit(bufferInfo.offset + bufferInfo.size);

        if ((encoderType == Encoder.Type.VIDEO) && isKeyFramePacket(bufferInfo)) {
            sendKeyFrame(encoderType,encodedData,bufferInfo);
        } else {
            boolean dropframe=false;
            if(encoderType == Encoder.Type.AUDIO){
                synchronized (sharedByteBufferAudioArray){
                    if(atomicFFMPEGAudioQueueCount>mDropLimit)
                        dropframe=true;
                }
            }
            else if(encoderType == Encoder.Type.VIDEO){
                synchronized (sharedByteBufferVideoArray){
                    if(atomicFFMPEGVideoQueueCount>mDropLimit)
                        dropframe=true;
                }
            }
            if(!dropframe)
                sendOrdinaryFrame(encoderType,encodedData,bufferInfo);
        }
        releaseBufferForType(encodedData,encoderType);
    }
    private void finish() {
        Log.D("%s finish()", this.getClass().getName());
        mFFMpegBridge.finallize();

        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(BROADCAST_MESSAGE, BROADCAST_STATE);
        intent.putExtra(BROADCAST_STATE, STATE_RTMP_CLOSED);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    public MuxerHandler getHandler() {
        return mReadyToHandle.get() ? mHandler : null;
    }
}
