package com.someoneman.youliveonstream.streamer.encoders;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.someoneman.youliveonstream.App;
import com.someoneman.youliveonstream.streamer.muxer.Muxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Aleksander on 17.02.2016.
 */
public class AudioEncoder extends Encoder {

    private long mAudioAbsolitePtsUs = 0;
    private long mStartPts = 0;
    private long mTotalSamplesNum = 0;
    private boolean mMuteMic = false;

    public AudioEncoder(Muxer muxer) throws IOException {
        super(muxer, Type.AUDIO);

        MediaFormat mediaFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 1);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

        mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
    }

    public void start() {
        super.start("AudioEncoder");
    }

    @Override
    protected void doInThread() {
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int channels = AudioFormat.CHANNEL_IN_MONO;
        int bufferSize = AudioRecord.getMinBufferSize(44100, channels, audioEncoding);

        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, 44100, channels, audioEncoding, bufferSize * 4);
        audioRecord.startRecording();

        AudioManager audioManager = (AudioManager) App.getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(mMuteMic);

        bufferSize /= 2;
        byte[] buffer = new byte[bufferSize];

        while (mEncodingRequested) {
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);

            ByteBuffer[] encoderInputBuffers = mMediaCodec.getInputBuffers();

            if (inputBufferIndex >= 0) {
                //ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                ByteBuffer inputBuffer = encoderInputBuffers[inputBufferIndex];
                if (inputBuffer == null)
                    throw new RuntimeException("encodedInputBuffer " + inputBufferIndex + " was null");
                inputBuffer.clear();
                int audioInputLength = audioRecord.read(inputBuffer, 2048);
                mAudioAbsolitePtsUs = System.nanoTime() / 1000L;
                mAudioAbsolitePtsUs = getJitterFreePts(mAudioAbsolitePtsUs, audioInputLength / 2);

                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, audioInputLength, mAudioAbsolitePtsUs, 0);

                drainEncoder();
            }
        }

        audioRecord.stop();
    }

    public void setMuteMic(boolean muteMic) {
        mMuteMic = muteMic;

        AudioManager audioManager = (AudioManager) App.getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(mMuteMic);
    }

    private long getJitterFreePts(long bufferPts, long bufferSamplesNum) {
        long correctedPts = 0;
        long bufferDuration = (1000000 * bufferSamplesNum) / (44100);
        bufferPts -= bufferDuration;
        if (mTotalSamplesNum == 0) {
            // reset
            mStartPts = bufferPts;
            mTotalSamplesNum = 0;
        }
        correctedPts = mStartPts + (1000000 * mTotalSamplesNum) / (44100);
        if (bufferPts - correctedPts >= 2 * bufferDuration) {
            mStartPts = bufferPts;
            mTotalSamplesNum = 0;
            correctedPts = mStartPts;
        }
        mTotalSamplesNum += bufferSamplesNum;

        return correctedPts;
    }
}