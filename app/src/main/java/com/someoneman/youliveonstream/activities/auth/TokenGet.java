package com.someoneman.youliveonstream.activities.auth;

import android.os.AsyncTask;

import com.someoneman.youliveonstream.AppClientInfo;
import com.someoneman.youliveonstream.model.SettingsManager;
import com.someoneman.youliveonstream.utils.Log;
import com.someoneman.youliveonstream.utils.Utils;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nik on 17.04.2016.
 */
public class TokenGet extends AsyncTask<String, String, JSONObject> {

    private String mCode;

    private static final String REDIRECT_URI = "http://localhost";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";

    ITokenGetCallback authActivity;

    public TokenGet(ITokenGetCallback authActivity, String code) {
        this.authActivity = authActivity;
        mCode = code;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        authActivity.OnTokenGetStarted();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {

            return new JSONObject(
                    Utils.loadHttp(
                            TOKEN_URL,
                            new BasicNameValuePair("code", mCode),
                            new BasicNameValuePair("client_id", AppClientInfo.getCID()),
                            new BasicNameValuePair("client_secret",AppClientInfo.getCS()),
                            new BasicNameValuePair("redirect_uri", REDIRECT_URI),
                            new BasicNameValuePair("grant_type", GRANT_TYPE)
                    )
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                String accessToken = jsonObject.getString("access_token");
                SettingsManager.getInstance().SetAccessToken(accessToken);
                if (jsonObject.has("refresh_token"))
                    SettingsManager.getInstance().SetRefreshToken(jsonObject.getString("refresh_token"));

                Log.D(jsonObject.toString());

                authActivity.OnTokenGetSucceed();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        authActivity.OnTokenGetFailed();
    }
}