package com.someoneman.youliveonstream;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.someoneman.youliveonstream.model.SettingsManager;
import com.someoneman.youliveonstream.streamer.ACamera;

/**
 * Created by Aleksander on 05.02.2016.
 */
public class App extends MultiDexApplication {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(getApplicationContext());

        SettingsManager.CreateNew(getApplicationContext());

        ACamera.GetInstance().initialize();
        mContext = this;

    }

    public static Context getContext() {
        return mContext;
    }
}
