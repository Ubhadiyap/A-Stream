package com.someoneman.youliveonstream.model.datamodel.broadcast.chat;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.someoneman.youliveonstream.model.Connector;
import com.someoneman.youliveonstream.model.datamodel.broadcast.BroadcastInfo;
import com.someoneman.youliveonstream.model.datamodel.broadcast.BroadcastStatus;
import com.someoneman.youliveonstream.model.datamodel.broadcast.IBroadcastObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by nik on 22.04.2016.
 */
public class Chat implements IChatObservable, IBroadcastObserver, Parcelable {

    private String mId;
    private String mBroadcastId;
    private String mPageToken = null;
    private boolean mIsStared = false;
    private final Object mLooper = new Object();
    private ArrayList<IChatObserver> mChatObservers = new ArrayList<>();

    public Chat(String broadcastId) {
        mBroadcastId = broadcastId;
    }

    public void start() {
        synchronized (mLooper) {
            mIsStared = true;
        }

        new Thread(() -> {
            try {
                mId = Connector.GetInstance().GetLiveChatId(mBroadcastId);

                while(mIsStared) {
                    List<ChatMessage> chatMessageList = Connector.GetInstance().GetChatMessages(Chat.this);

                    if (chatMessageList.size() > 0)
                        NotifyObservesOnChatMessagesReceived(chatMessageList);

                    Thread.sleep(5000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void stop() {
        synchronized (mLooper) {
            mIsStared = false;
        }
    }

    private void NotifyObservesOnChatMessagesReceived(List<ChatMessage> chatMessageList) {
        for (IChatObserver observer : mChatObservers)
            observer.OnChatMessagesReceived(chatMessageList);
    }

    protected Chat(Parcel in) {
        mId = in.readString();
    }

    public String getId() {
        return mId;
    }

    public String getPageToken() {
        return mPageToken;
    }

    public void setPageToken(String pageToken) {
        mPageToken = pageToken;
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    @Override
    public void AddObserver(IChatObserver observer) {
        mChatObservers.add(observer);
    }

    @Override
    public void RemoveObserver(IChatObserver observer) {
        mChatObservers.remove(observer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
    }

    @Override
    public void OnStateChanged(BroadcastStatus status) {
        if (status == BroadcastStatus.Live && !mIsStared)
            start();
        else if (status != BroadcastStatus.Live && mIsStared)
            stop();
    }

    @Override
    public void OnInfoChanged(BroadcastInfo broadcastInfo) {

    }
}
