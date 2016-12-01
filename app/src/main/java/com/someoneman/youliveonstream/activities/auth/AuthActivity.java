package com.someoneman.youliveonstream.activities.auth;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.someoneman.youliveonstream.AppClientInfo;
import com.someoneman.youliveonstream.activities.error.ErrorActivity;
import com.someoneman.youliveonstream.activities.main.MainActivity;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Aleksander on 28.02.2016.
 */
public class AuthActivity extends AppCompatActivity implements IAuthCallback, ITokenGetCallback {

    private static final String REDIRECT_URI = "http://localhost";
    private static final String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String OAUTH_SCOPE = "https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.force-ssl https://www.googleapis.com/auth/youtubepartner https://www.googleapis.com/auth/youtubepartner-channel-audit https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";

    private static final int PERMISSIONS_REQUEST = 1;

    private ProgressDialog mProgressDialog;
    private ProgressBar progressBar;

    public static final String[] PERMISSIONS = {
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);

        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        initViews();

        Bundle b = getIntent().getExtras();
        if (b!=null){
            boolean changeChannel = b.getBoolean("changeChannel");
            if(changeChannel)
                changeChannel();
            else
                signIn();
        }
        else
            signIn();


        if (!Utils.isInternetConnected(getApplicationContext())) {
            Utils.startErrorActivity(this, ErrorActivity.ACTION_ERROR_NO_INTERNET);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (!Utils.isInternetConnected(getApplicationContext())) {
            Utils.startErrorActivity(this, ErrorActivity.ACTION_ERROR_NO_INTERNET);

            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            Utils.startErrorActivity(this, ErrorActivity.ACTION_ERROR_NO_PERMISSIONS);

                            return;
                        }
                    }
                    initWebView();
                }

                break;
        }
    }

    private String[] isPermissionsRequested() {
        ArrayList<String> permissions = new ArrayList<>();

        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }

        return permissions.toArray(new String[permissions.size()]);
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
    }

    //region init

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.authorization));
    }

    private void initWebView() {
        progressBar = (ProgressBar) findViewById(R.id.loadingSpinner);

        final WebView webView = (WebView) findViewById(R.id.webViewAuth);
        if(webView!=null){
            webView.setVisibility(View.GONE);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + AppClientInfo.getCID() + "&access_type=offline&scope=" + OAUTH_SCOPE);
            webView.clearCache(true);
            webView.clearHistory();
            webView.setWebViewClient(new AuthWebView(this));
        }
    }

    //endregion

    private void clearCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.removeSessionCookie();
    }

    private void startApp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void signIn() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = isPermissionsRequested();
            if (permissions.length > 0) {
                requestPermissions(permissions);
            } else {
                clearCookies();
                initWebView();
            }
        } else {
            clearCookies();
            initWebView();
        }
    }

    public void changeChannel(){
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = isPermissionsRequested();
            if (permissions.length > 0) {
                requestPermissions(permissions);
            } else {
                initWebView();
            }
        } else {
            initWebView();
        }

    }

    //region IAuthCallback

    public void OnAuthReady(){
        progressBar.setVisibility(View.GONE);
    }

    public void OnAuthSucceed(String authCode){
        new TokenGet(this, authCode).execute();
    }

    public  void OnAuthFailed(){
        Utils.startErrorActivity(AuthActivity.this, ErrorActivity.ACTION_ERROR_AUTH);
        finish();
    }

    //endregion

    //region ITokenGetCallback

    public void OnTokenGetSucceed(){
        startApp();
    }

    public void OnTokenGetFailed(){
        mProgressDialog.dismiss();
    }

    public void OnTokenGetStarted(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.authorization));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
    }

    //endregion

}