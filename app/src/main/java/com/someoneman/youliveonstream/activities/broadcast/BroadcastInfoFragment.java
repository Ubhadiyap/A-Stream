package com.someoneman.youliveonstream.activities.broadcast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.model.datamodel.broadcast.BroadcastInfo;
import com.someoneman.youliveonstream.model.datamodel.broadcast.BroadcastStatus;
import com.someoneman.youliveonstream.model.datamodel.broadcast.IBroadcastObserver;
import com.someoneman.youliveonstream.R;

import java.util.Timer;
import java.util.TimerTask;

public class BroadcastInfoFragment extends Fragment implements IBroadcastObserver {
    private TextView mTextViewBroadcastStatus;
    private TextView mTextViewTimer;
    private TextView mTextViewCurrentViewers;
    private TextView mTextViewLikes;
    private TextView mTextViewDislikes;
    private View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        //TODO: create broadcast info listener
        fragmentView = inflater.inflate(R.layout.broadcast_activity_info, container, false);
        mTextViewBroadcastStatus = (TextView)fragmentView.findViewById(R.id.textViewBroadcastStatus);
        mTextViewTimer = (TextView)fragmentView.findViewById(R.id.textViewTimer);
        mTextViewCurrentViewers = (TextView)fragmentView.findViewById(R.id.textViewViewers);
        mTextViewLikes = (TextView)fragmentView.findViewById(R.id.textViewLikes);
        mTextViewDislikes = (TextView)fragmentView.findViewById(R.id.textViewDislikes);
        //TODO: Change Visibility by default.
        fragmentView.setVisibility(View.INVISIBLE);
        return fragmentView;
    }

    @Override
    public void OnStateChanged(final BroadcastStatus status) {
        fragmentView.post(new Runnable() {
            @Override
            public void run() {
            switch(status){

                case TestStarting:
                case Testing:
                    mTextViewBroadcastStatus.setText(getResources().getString(R.string.status_broadcast_testing));

                    break;

                case Starting:
                case Live:
                    mTextViewBroadcastStatus.setText(getResources().getString(R.string.status_broadcast_live));

                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTimerTask(), 1000, 1000);

                    break;
            }
            }
        });


    }

    @Override
    public void OnInfoChanged(BroadcastInfo broadcastInfo) {
        //TODO: runOnUIThread
        mTextViewCurrentViewers.setText(String.valueOf(broadcastInfo.getViewers()));
        mTextViewLikes.setText(String.valueOf(broadcastInfo.getLikes()));
        mTextViewDislikes.setText(String.valueOf(broadcastInfo.getDislikes()));
    }

    public void setBroadcast(Broadcast broadcast){
        broadcast.AddObserver(this);
        fragmentView.setVisibility(View.VISIBLE);
    }

    private class TimerTimerTask extends TimerTask {

        private long timeSeconds=0;

        @Override
        public void run() {
            timeSeconds++;

            final long seconds = timeSeconds % 60;
            final long minutes = (timeSeconds / 60) % 60;
            final long hours = timeSeconds / (60*60);

            fragmentView.post(new Runnable() {
                @Override
                public void run() {
                    mTextViewTimer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            });
        }
    }
}
