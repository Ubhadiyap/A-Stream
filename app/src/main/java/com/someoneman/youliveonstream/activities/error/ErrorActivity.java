package com.someoneman.youliveonstream.activities.error;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.someoneman.youliveonstream.R;

/**
 * Created by Aleksander on 13.03.2016.
 */
public class ErrorActivity extends AppCompatActivity {

    public static final String ACTION_ERROR_NO_INTERNET = "action_error_no_internet";
    public static final String ACTION_ERROR_NO_PERMISSIONS = "action_error_no_permissions";
    public static final String ACTION_ERROR_NO_CAMERA = "action_error_no_camera";
    public static final String ACTION_ERROR_AUTH = "action_error_auth";

    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_activity);

        Intent intent = getIntent();
        mAction = intent.getAction();

        initViews();
    }

    public void initViews() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.colorLightStatusBar));
        }

        TextView textViewErrorMessage = (TextView)findViewById(R.id.textViewErrorMessage);

        if (mAction.equals(ACTION_ERROR_NO_INTERNET))
            textViewErrorMessage.setText(getResources().getString(R.string.error_no_internet));
        else if (mAction.equals(ACTION_ERROR_NO_PERMISSIONS))
            textViewErrorMessage.setText(getResources().getString(R.string.error_no_permissions));
        else if (mAction.contentEquals(ACTION_ERROR_NO_CAMERA))
            textViewErrorMessage.setText(getResources().getString(R.string.error_no_camera));
        else if (mAction.contentEquals(ACTION_ERROR_AUTH))
            textViewErrorMessage.setText(getResources().getString(R.string.error_auth));
    }
}
