package com.someoneman.youliveonstream.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by nik on 26.04.2016.
 */
public class SettingsManager {
    private static final String ACCESS_TOKEN = "AccessToken";
    private static final String REFRESH_TOKEN = "RefreshToken";

    private static SettingsManager _instance;
    private SharedPreferences _settings;

    private SettingsManager(Context context){
        _settings= PreferenceManager.getDefaultSharedPreferences(context);

    }

    public static void CreateNew(Context context){
        _instance=new SettingsManager(context);

    }

    public static SettingsManager getInstance() {
        return _instance;
    }

    public void RemoveAuthSettings(){

        SharedPreferences.Editor editor = _settings.edit();
        editor.remove(ACCESS_TOKEN);
        editor.remove(REFRESH_TOKEN);
        editor.apply();
    }
    public String GetAccessToken(){
        return _settings.getString(ACCESS_TOKEN, null);
    }

    public String GetRefreshToken(){
        return _settings.getString(REFRESH_TOKEN, null);
    }

    public void SetAccessToken(String accessToken){
        SharedPreferences.Editor editor = _settings.edit();
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public void SetRefreshToken(String refreshToken){
        SharedPreferences.Editor editor = _settings.edit();
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.commit();
    }

    public boolean IsSignedIn() {
        return _settings.getString(ACCESS_TOKEN, null) != null
                && _settings.getString(REFRESH_TOKEN, null) != null;

    }
}
