package com.someoneman.youliveonstream.activities.broadcast;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ToggleButton;

import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.activities.broadcast.chatmessagelist.ChatMessagesAdapter;
import com.someoneman.youliveonstream.contoller.views.camera.CameraSurfaceView;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.Chat;
import com.someoneman.youliveonstream.streamer.ACamera;
import com.someoneman.youliveonstream.streamer.StreamerService;
import com.someoneman.youliveonstream.streamer.muxer.Muxer;
import com.someoneman.youliveonstream.utils.Utils;

/**
 * Created by ultra on 10.04.2016.
 */
public class BroadcastActivity extends FragmentActivity {
    BroadcastInfoFragment broadcastInfoFragment;
    IntractableFragment intractableFragment;
    private CameraSurfaceView mGLView;
    Muxer mMuxer;
    private StreamerService mStreamerService;
    private final Object glFence = new Object();

    private ToggleButton lockUIButton;

    private Broadcast mBroadcast;
    private Chat mChat;

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mChatRecyclerView;
    private ChatMessagesAdapter mChatMessagesAdapter;

    private Button _buttonShare;
    private ToggleButton _toggleButtonMic;
    private ToggleButton _toggleButtonFlashlight;
    private ToggleButton _toggleButtonSwitchCamera;
    private ToggleButton mToggleButtonStream;

    //region initialisations

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.broadcast_activity);

        mGLView = (CameraSurfaceView) findViewById(R.id.gLView);

        initWindow();
        final Intent i = getIntent();
        mBroadcast = i.getParcelableExtra(Broadcast.EXTRA_KEY);

        initViews();
    }

    private void initViews() {
        mGLView.invalidate();

        mChatRecyclerView = (RecyclerView)findViewById(R.id.recycleView);
        mChatMessagesAdapter = new ChatMessagesAdapter(mChatRecyclerView);

        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(BroadcastActivity.this));
        mChatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mChatRecyclerView.setAdapter(mChatMessagesAdapter);

        mBroadcast.AddChatObserver(mChatMessagesAdapter);

        lockUIButton = (ToggleButton)findViewById(R.id.toggleButtonLockUi);
        lockUIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lockUIButton.isChecked()) {
                    setUiEnable(false);
                }
                else {
                    lockUIButton.setChecked(true);
                }
            }
        });

        lockUIButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setUiEnable(true);
                lockUIButton.setChecked(false);
                return true;
            }
        });

        _buttonShare = (Button)findViewById(R.id.buttonShare);
        _buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareBroadcast(BroadcastActivity.this, mBroadcast);
            }
        });

        _toggleButtonFlashlight = (ToggleButton)findViewById(R.id.toggleButtonFlashlight);
        _toggleButtonFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACamera.GetInstance().flashlight(((ToggleButton)v).isChecked());
            }
        });

        mToggleButtonStream = (ToggleButton)findViewById(R.id.toggleButtonStream);
        mToggleButtonStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (((ToggleButton)v).isChecked()){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mStreamerService.start(mBroadcast);
                            /*
                            mBroadcast.CreateStream();

                            mMuxer = new Muxer(mBroadcast.getStream().getCdnSettings());
                            final TextureMovieEncoder mVideoEncoder = new TextureMovieEncoder(mMuxer);
                            AudioEncoder mAudioEncoder = null;
                            try {
                                mAudioEncoder = new AudioEncoder(mMuxer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mMuxer.start();
                            mAudioEncoder.start();
                            //mBroadcast.ActivateBroadcast();*/
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGLView.onStartTranslation(mStreamerService.getTextureMovieEncoder());
                                }
                            });
                        }
                    }).start();
//                    new AsyncTask<Void,Void,TextureMovieEncoder>(){
//
//                        @Override
//                        protected TextureMovieEncoder doInBackground(Void... params) {
//
////                            return mBroadcast.getVideoEncoder();
//                            return null;
//                        }
//
//                        @Override
//                        protected void onPostExecute(TextureMovieEncoder videoEncoder){
////                            mGLView.onStartTranslation(videoEncoder);
//                            //muxer.start();
//                        }
//                    }.execute();
                }
                else {
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            mBroadcast.StopBroadcast();
                            return null;
                        }
                    }.execute();
                }
            }
        });

        _toggleButtonMic = (ToggleButton)findViewById(R.id.toggleButtonMic);
        _toggleButtonMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        _toggleButtonSwitchCamera = (ToggleButton)findViewById(R.id.toggleButtonSwitchcamera);
        _toggleButtonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //endregion

    private void setUiEnable(boolean enabled){
        mToggleButtonStream.setEnabled(enabled);
        _toggleButtonSwitchCamera.setEnabled(enabled);
        _toggleButtonFlashlight.setEnabled(enabled);
        _toggleButtonMic.setEnabled(enabled);
        _buttonShare.setEnabled(enabled);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mStreamerService = ((StreamerService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mStreamerService == null)
            bindService(new Intent(this, StreamerService.class), mServiceConnection, BIND_AUTO_CREATE);

    }
    protected void onPause() {
        mGLView.onPause();
        super.onPause();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (!lockUIButton.isChecked()){
            super.onBackPressed();
        }
    }
}
