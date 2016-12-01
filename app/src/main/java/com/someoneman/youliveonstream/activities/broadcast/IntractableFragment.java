package com.someoneman.youliveonstream.activities.broadcast;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.streamer.ACamera;
import com.someoneman.youliveonstream.utils.Utils;

public class IntractableFragment extends Fragment {
    private View fragmentView;
    private ToggleButton lockUIButton;

    private Broadcast broadcast;

    private Button _buttonShare;
    private ToggleButton _toggleButtonMic;
    private ToggleButton _toggleButtonFlashlight;
    private ToggleButton _toggleButtonSwitchCamera;
    private ToggleButton _toggleButtonStream;

    private void setUiEnable(boolean enable){
        _toggleButtonStream.setEnabled(enable);
        _toggleButtonSwitchCamera.setEnabled(enable);
        _toggleButtonFlashlight.setEnabled(enable);
        _toggleButtonMic.setEnabled(enable);
        _buttonShare.setEnabled(enable);
    }

    private void initButtons(View fragmentView){

        lockUIButton = (ToggleButton)fragmentView.findViewById(R.id.toggleButtonLockUi);
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

        _buttonShare = (Button)fragmentView.findViewById(R.id.buttonShare);
        _buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareBroadcast(getActivity(), broadcast);
            }
        });

        _toggleButtonFlashlight = (ToggleButton)fragmentView.findViewById(R.id.toggleButtonFlashlight);
        _toggleButtonFlashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ACamera.GetInstance().flashlight(((ToggleButton)v).isChecked());
            }
        });

        _toggleButtonStream = (ToggleButton)fragmentView.findViewById(R.id.toggleButtonStream);
        _toggleButtonStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

            }
        });

        _toggleButtonMic = (ToggleButton)fragmentView.findViewById(R.id.toggleButtonMic);
        _toggleButtonMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        _toggleButtonSwitchCamera = (ToggleButton)fragmentView.findViewById(R.id.toggleButtonSwitchcamera);
        _toggleButtonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        //TODO: create broadcast info listener
        fragmentView = inflater.inflate(R.layout.broadcast_activity_interractable, container, false);

        initButtons(fragmentView);
        fragmentView.setVisibility(View.INVISIBLE);


        return fragmentView;
    }
}
