package com.someoneman.youliveonstream.model.datamodel.broadcast;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.youtube.model.LiveBroadcast;
import com.someoneman.youliveonstream.model.Connector;
import com.someoneman.youliveonstream.model.datamodel.broadcast.video.Video;
import com.someoneman.youliveonstream.model.datamodel.broadcast.video.VideoStatistic;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.Chat;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.IChatObserver;
import com.someoneman.youliveonstream.model.datamodel.broadcast.stream.Stream;
import com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder.TextureMovieEncoder;
import com.someoneman.youliveonstream.utils.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nik on 22.04.2016.
 */


//Broadcast is not just broadcast, that represents LiveBroadcast,
// but kinda a metaClass, makes all the stuff accessible
public class Broadcast implements IBroadcastObservable, Parcelable {
    public static final String EXTRA_KEY = "BROADCAST" ;
    ArrayList<IBroadcastObserver> _broadcastObservers = new ArrayList<>();

    //Геттер для срима не делать! Для управления - использовать броадкаст, как оболочку.
    private Stream mStream;
    private Video mVideo;
    private Chat mChat;

    private String mId;
    private String mTitle;
    private String mThumbnailUrl;
    private String mPrivacy;
    private String mLiveStatus;
    private BroadcastStatus mStatus;

    private String mBoundStreamId;

    public Broadcast(LiveBroadcast liveBroadcast) throws IOException {
        mId = liveBroadcast.getId();
        mTitle = liveBroadcast.getSnippet().getTitle();
        mThumbnailUrl = liveBroadcast.getSnippet().getThumbnails().getHigh().getUrl();
        mPrivacy = liveBroadcast.getStatus().getPrivacyStatus();
        mLiveStatus = liveBroadcast.getStatus().getLifeCycleStatus();
        mStatus = BroadcastStatus.getStatusFromString(mLiveStatus);
        mVideo = new Video(Connector.GetInstance().getVideo(mId));
        mBoundStreamId = liveBroadcast.getContentDetails().getBoundStreamId();
    }

    protected Broadcast(Parcel in) throws IOException {
        mId = in.readString();
        mTitle =in.readString();
        mThumbnailUrl = in.readString();
        mPrivacy = in.readString();
        mLiveStatus = in.readString();
        mStatus = BroadcastStatus.getStatusFromString(mLiveStatus);
        mStream = in.readParcelable(Stream.class.getClassLoader());
        mVideo = in.readParcelable(Video.class.getClassLoader());
        mBoundStreamId = in.readString();

        mChat = new Chat(mId);

        if (mStatus == BroadcastStatus.Live)
            mChat.start();

        AddObserver(mChat);
    }

    public void CreateStream(){
        Log.I("%s", mBoundStreamId);
        if (mBoundStreamId != null)
            try {
                Log.I("%s", mBoundStreamId);
                mStream = new Stream(Connector.GetInstance().GetStream(mBoundStreamId));
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            try {
                mStream = new Stream(Connector.GetInstance().CreateStream(mTitle,3));
                mStream.BindStream(mId);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public TextureMovieEncoder getVideoEncoder(){
        return mStream.getVideoEncoder();
    }

    public void ActivateBroadcast(){
        new ActivateBroadcastTask().execute();
    }

    private class ActivateBroadcastTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                mStream.ActivateStream();

                String status = Connector.GetInstance().GetBroadcastStatus(mId);

                if (BroadcastStatus.getStatusFromString(status) == BroadcastStatus.Ready) {
                    status = Connector.GetInstance().SetBroadcastStatus(mId, "testing");
                }

                NotifyObserversOnStateChanged(BroadcastStatus.getStatusFromString(status));

                if (BroadcastStatus.getStatusFromString(status) == BroadcastStatus.TestStarting) {
                    while (true) {
                        status = Connector.GetInstance().GetBroadcastStatus(mId);

                        if (BroadcastStatus.getStatusFromString(status) == BroadcastStatus.Testing) {
                            NotifyObserversOnStateChanged(BroadcastStatus.getStatusFromString(status));

                            break;
                        } else {
                            Thread.sleep(5000);
                        }
                    }
                }

                status = Connector.GetInstance().SetBroadcastStatus(mId, "live");

                NotifyObserversOnStateChanged(BroadcastStatus.getStatusFromString(status));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void StopBroadcast(){
        //TODO: notifyObservers Broadcast Stopping
        try {
            Connector.GetInstance().SetBroadcastStatus(mId,"complete");
            mStream.StopStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void AddChatObserver(IChatObserver chatObserver){
        mChat.AddObserver(chatObserver);
    }

    public void RemoveChatObserver(IChatObserver chatObserver){
        mChat.RemoveObserver(chatObserver);
    }

    //region IBroadcastObservable

    @Override
    public void AddObserver(IBroadcastObserver observer) {
        _broadcastObservers.add(observer);
    }

    @Override
    public void RemoveObserver(IBroadcastObserver observer) {
        _broadcastObservers.remove(observer);
    }

    public void NotifyObserversOnStateChanged(BroadcastStatus status) {
        for (IBroadcastObserver observer : _broadcastObservers){
            observer.OnStateChanged(status);
        }
    }

    public void NotifyObserversOnInfoChanged(BroadcastInfo broadcastInfo) {
        for (IBroadcastObserver observer : _broadcastObservers){
            observer.OnInfoChanged(broadcastInfo);
        }
    }

    //endregion

    //region getters

    public String GetBroadcastLink(){
        return String.format("https://www.youtube.com/watch?v=%s", mId);
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getPrivacy(){
        return mPrivacy;
    }

    public String getLiveStatus(){
        return mLiveStatus;
    }

    public Stream getStream() {
        return mStream;
    }

    //endregion

    public static Broadcast createNewBroadcast(String title, int access){
        try {
            return new Broadcast(Connector.GetInstance().CreateBroadcast(title, access));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
    public VideoStatistic getVideoStatistic(){
        return mVideo.getVideoStatistic();
    }


    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mThumbnailUrl);
        dest.writeString(mPrivacy);
        dest.writeString(mLiveStatus);
        dest.writeParcelable(mStream,flags);
        dest.writeParcelable(mVideo,flags);
        dest.writeString(mBoundStreamId);
    }

    public static final Creator<Broadcast> CREATOR = new Creator<Broadcast>() {
        @Override
        public Broadcast createFromParcel(Parcel in) {
            try {
                return new Broadcast(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Broadcast[] newArray(int size) {
            return new Broadcast[size];
        }
    };

    //endregion
}
