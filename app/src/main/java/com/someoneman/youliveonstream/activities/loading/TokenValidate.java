package com.someoneman.youliveonstream.activities.loading;

import android.os.AsyncTask;

import com.someoneman.youliveonstream.AppClientInfo;
import com.someoneman.youliveonstream.activities.loading.LoadingActivity;
import com.someoneman.youliveonstream.utils.Utils;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nik on 17.04.2016.
 */
public class TokenValidate extends AsyncTask<Void, Void, String> {

    private String mRefreshToken;
    LoadingActivity loadingActivity;

    private static final String TOKEN_REFRESH_URL = "https://www.googleapis.com/oauth2/v3/token";
    private static final String TOKEN_VALIDATE_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo";

    public TokenValidate(LoadingActivity loadingActivity, String refreshToken) {
        mRefreshToken = refreshToken;
        this.loadingActivity = loadingActivity;
    }

    private String refreshAccessToken(){
        String accessToken = null;
        try {
            JSONObject refreshTokenJson = new JSONObject(
                    Utils.loadHttp(
                            TOKEN_REFRESH_URL,
                            new BasicNameValuePair("refresh_token", mRefreshToken),
                            new BasicNameValuePair("client_id", AppClientInfo.getCID()),
                            new BasicNameValuePair("client_secret", AppClientInfo.getCS()),
                            new BasicNameValuePair("grant_type", "refresh_token")
                    )
            );

            accessToken = refreshTokenJson.getString("access_token");
            return accessToken;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean validateAccessToken(String accessToken){
        JSONObject accessTokenJson=null;

        try {
            accessTokenJson = new JSONObject(
                    Utils.loadHttp(
                            TOKEN_VALIDATE_URL,
                            new BasicNameValuePair("access_token", accessToken)
                    )
            );
            if (accessTokenJson.has("error"))
                return false;
            else{
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected String doInBackground(Void... params) {
        String accessToken = refreshAccessToken();

        if (accessToken!=null&&validateAccessToken(accessToken))
            return accessToken;
        else{
            return null;
        }
    }

    @Override
    protected void onPostExecute(String accessToken) {
        if (accessToken==null)
            loadingActivity.OnValidationFailed();
        else
            loadingActivity.OnValidationSucceed(accessToken);
    }
}
