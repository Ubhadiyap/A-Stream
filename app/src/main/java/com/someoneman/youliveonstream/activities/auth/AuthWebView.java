package com.someoneman.youliveonstream.activities.auth;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by nik on 17.04.2016.
 */
public class AuthWebView extends WebViewClient {
    IAuthCallback authActivity;
    public AuthWebView(IAuthCallback authActivity){
        this.authActivity=authActivity;
    }
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon){
        if (url.contains("?code=")) {
            view.setVisibility(View.GONE);
            Uri uri = Uri.parse(url);
            String authCode = uri.getQueryParameter("code");
            authActivity.OnAuthSucceed(authCode);
        }
        else if (url.contains("error=access_denied")){
            view.setVisibility(View.GONE);
            authActivity.OnAuthFailed();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.contains("?code=")||url.contains("error=access_denied")) {
        }
        else{
            view.setVisibility(View.VISIBLE);
            authActivity.OnAuthReady();
        }

    }
}
