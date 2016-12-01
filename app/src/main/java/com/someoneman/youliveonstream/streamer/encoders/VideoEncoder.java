package com.someoneman.youliveonstream.streamer.encoders;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Size;
import android.view.Surface;

import com.someoneman.youliveonstream.streamer.muxer.Muxer;

import java.io.IOException;

/**
 * Created by Aleksander on 17.02.2016.
 */
public class VideoEncoder extends Encoder {

    private Surface mInputSurface;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoEncoder(Muxer muxer, Size mPreviewSize) throws IOException{
        super(muxer, Type.VIDEO);

        mEncodingRequested = true;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                "video/avc",
                //1280,
                //720
                mPreviewSize.getWidth(),
                mPreviewSize.getHeight()
        );
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1500*1000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_WIDTH, 1280);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_HEIGHT, 720);
        mediaFormat.setInteger(MediaFormat.KEY_SLICE_HEIGHT, 720);
        mediaFormat.setInteger(MediaFormat.KEY_PRIORITY, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
            mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
        }

        mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mInputSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
    }

    public VideoEncoder(Muxer muxer, Camera.Size videoSize) throws IOException{
        super(muxer, Type.VIDEO);

        mEncodingRequested = true;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                "video/avc",
                videoSize.width,
                videoSize.height
        );
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1500*1000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, videoSize.width);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, videoSize.height);

   /*     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //5.0+
            mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline); //Без Key_Level вызывает ошибку
                mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
        }*/
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
//        }

        mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
    }

    public void start() {
        super.start("VideoEncoder");
    }

    @Override
    protected void doInThread() {
        while (mEncodingRequested) {
            drainEncoder();
        }
    }

    public boolean isInputSurfaceAvailable() {
        return mInputSurface != null;
    }

    public Surface getInputSurface() {
        if (mInputSurface == null)
            mInputSurface = mMediaCodec.createInputSurface();

        return mInputSurface;
    }
}
