package com.someoneman.youliveonstream.streamer.muxer;

import android.os.Handler;
import android.os.Message;

import com.someoneman.youliveonstream.utils.Log;

import java.lang.ref.WeakReference;

/**
 * Created by nik on 10.05.2016.
 */
public class MuxerHandler extends Handler {

    static final int MSG_WRITE_DATA = 0;

    private WeakReference<MuxerThread> mWeakMuxer;

    public MuxerHandler(MuxerThread muxer) {
        mWeakMuxer = new WeakReference<>(muxer);
    }

    @Override
    public void handleMessage(Message msg) {
        MuxerThread muxerThread = mWeakMuxer.get();

        switch (msg.what) {
            case MSG_WRITE_DATA:
                WritePacketData data = (WritePacketData)msg.obj;
                muxerThread.handleWriteSampleData(data);
                System.gc();
                System.gc();
                break;
            default:
                Log.D("%s unknown message received", this.getClass().getName());
                break;
        }
    }
}
