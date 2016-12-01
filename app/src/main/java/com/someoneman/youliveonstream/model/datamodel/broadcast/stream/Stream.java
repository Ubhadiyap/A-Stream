package com.someoneman.youliveonstream.model.datamodel.broadcast.stream;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveStream;
import com.someoneman.youliveonstream.model.Connector;
import com.someoneman.youliveonstream.streamer.encoders.AudioEncoder;
import com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder.TextureMovieEncoder;
import com.someoneman.youliveonstream.streamer.muxer.Muxer;

import java.io.IOException;

/**
 * Created by nik on 22.04.2016.
 */
public class Stream implements Parcelable {
    private String _id;
    public CdnSettings mCdnSettings;
    private Muxer mMuxer;
    private TextureMovieEncoder mVideoEncoder;
    private AudioEncoder mAudioEncoder;

    public Stream(LiveStream liveStream) throws IOException {
        _id = liveStream.getId();
        mCdnSettings = liveStream.getCdn();


        /*
        mMuxer = new Muxer(mCdnSettings);
        //TODO: not the best place to start muxer, i guess
        mVideoEncoder = new TextureMovieEncoder(mMuxer);
        mAudioEncoder = new AudioEncoder(mMuxer);
        mMuxer.start();
        mAudioEncoder.start();*/
    }

    public CdnSettings getCdnSettings() {
        return mCdnSettings;
    }

    public void enableSound(boolean mute){

    }

    public TextureMovieEncoder getVideoEncoder(){
        return mVideoEncoder;
    }



    public String GetStatus() throws IOException {
        return Connector.GetInstance().GetStreamStatus(_id);
    }

    public void BindStream(String broadcastId) throws IOException {
        Connector.GetInstance().BindStream(broadcastId,_id);
    }

    public void ActivateStream() throws IOException, InterruptedException {
        int WAIT_DELAY_MS = 5000;
        String status;

        while (true) {
            status = GetStatus();

            if (status.equals("active")) {

                break;
            } else
                Thread.sleep(WAIT_DELAY_MS);
        }
    }

    public void StopStream() throws IOException {
        mAudioEncoder.stop();
        mMuxer.stop();
        Connector.GetInstance().DeleteStream(_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
    }

    protected Stream(Parcel in) {
        _id = in.readString();
    }

    public static final Creator<Stream> CREATOR = new Creator<Stream>() {
        @Override
        public Stream createFromParcel(Parcel in) {
            return new Stream(in);
        }

        @Override
        public Stream[] newArray(int size) {
            return new Stream[size];
        }
    };
}
