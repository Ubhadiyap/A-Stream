package com.someoneman.youliveonstream.streamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.someoneman.youliveonstream.activities.broadcast.BroadcastActivity;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.model.datamodel.broadcast.BroadcastInfo;
import com.someoneman.youliveonstream.model.datamodel.broadcast.BroadcastStatus;
import com.someoneman.youliveonstream.model.datamodel.broadcast.IBroadcastObserver;
import com.someoneman.youliveonstream.streamer.encoders.AudioEncoder;
import com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder.TextureMovieEncoder;
import com.someoneman.youliveonstream.streamer.muxer.Muxer;
import com.someoneman.youliveonstream.utils.Log;
import com.someoneman.youliveonstream.utils.Utils;

import java.io.IOException;

import javax.annotation.Nullable;

/**
 * Created by Aleksander on 07.02.2016.
 */
public class StreamerService extends Service {

    private static final int STREAMERSERVICE_NOTIFICAION_ID = 1001;

    private IBinder binder = new LocalBinder();
    private Broadcast mBroadcast;
    private Muxer mMuxer;
    private TextureMovieEncoder mTextureMovieEncoder;
    private AudioEncoder mAudioEncoder;;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public StreamerService getService() {
            return StreamerService.this;
        }
    }

    public void start(Broadcast broadcast) {
        try {
            broadcast.CreateStream();
            broadcast.AddObserver(mBroadcastObserver);
            mMuxer = new Muxer(broadcast.getStream().getCdnSettings());
            mTextureMovieEncoder = new TextureMovieEncoder(mMuxer);
            mAudioEncoder = new AudioEncoder(mMuxer);

            mMuxer.start();
            mAudioEncoder.start();

            showForegroundNotification(Utils.stringFromResourceByName(getApplicationContext(), broadcast.getLiveStatus()));

            broadcast.ActivateBroadcast();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IBroadcastObserver mBroadcastObserver = new IBroadcastObserver() {
        @Override
        public void OnStateChanged(BroadcastStatus status) {
            Log.I("HE:D");
        }

        @Override
        public void OnInfoChanged(BroadcastInfo broadcastInfo) {

        }
    };

    public TextureMovieEncoder getTextureMovieEncoder() {
        return mTextureMovieEncoder;
    }

    /*

    public void initialize(Muxer muxer) {
        try {
            mMuxer = muxer;
            mAudioEncoder = new AudioEncoder(mMuxer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        showForegroundNotification("text");
        //mMuxer.start();
        mAudioEncoder.start();
    }

    public void stop() {
        mAudioEncoder.stop();
        //mMuxer.stop();
        stopForeground(true);
    }*/

    public Muxer getMuxer() {
        return mMuxer;
    }

    public AudioEncoder getAudioEncoder() {
        return mAudioEncoder;
    }

    private void showForegroundNotification(String text) {
        Intent notificationIntent = new Intent(this, BroadcastActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();

        startForeground(STREAMERSERVICE_NOTIFICAION_ID, notification);
    }
}
