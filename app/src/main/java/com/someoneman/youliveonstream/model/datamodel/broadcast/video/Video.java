package com.someoneman.youliveonstream.model.datamodel.broadcast.video;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.youtube.model.VideoStatistics;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.Chat;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.IChatObserver;

/**
 * Created by nik on 02.05.2016.
 */
public class Video implements Parcelable {
    private VideoStatistic _videoStatistic;

    public Video(com.google.api.services.youtube.model.Video video){
        VideoStatistics videoStatistics = video.getStatistics();
        _videoStatistic = new VideoStatistic(videoStatistics);

    }

    /*
    public void AddChatObserver(IChatObserver chatObserver){
        _chat.AddObserver(chatObserver);
    }

    public void RemoveChatObserver(IChatObserver chatObserver){
        _chat.RemoveObserver(chatObserver);
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected Video(Parcel in) {
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public VideoStatistic getVideoStatistic() {
        return _videoStatistic;
    }
}
