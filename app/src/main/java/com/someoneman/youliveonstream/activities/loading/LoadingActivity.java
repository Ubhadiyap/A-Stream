package com.someoneman.youliveonstream.activities.loading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.someoneman.youliveonstream.activities.error.ErrorActivity;
import com.someoneman.youliveonstream.activities.main.MainActivity;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.SettingsManager;
import com.someoneman.youliveonstream.activities.auth.AuthActivity;
import com.someoneman.youliveonstream.utils.Utils;

public class LoadingActivity extends Activity {
    //TODO: Сделать красивенькую анимашечку или красивенький сплеш
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        String refreshToken = SettingsManager.getInstance().GetRefreshToken();

        if (Utils.isInternetConnected(getApplicationContext())) {
            if (SettingsManager.getInstance().IsSignedIn()) {
                new TokenValidate(this, refreshToken).execute();
            }
            else{
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            }
        } else {
            Utils.startErrorActivity(this, ErrorActivity.ACTION_ERROR_NO_INTERNET);
        }
    }

    public void OnValidationSucceed(String accessToken) {
        SettingsManager.getInstance().SetAccessToken(accessToken);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void OnValidationFailed() {
        Intent intent = new Intent(this, AuthActivity.class);

        Bundle b = new Bundle();
        b.putBoolean("changeChannel", false);
        intent.putExtras(b);

        startActivity(intent);
        finish();
    }


}
