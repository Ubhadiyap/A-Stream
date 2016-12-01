package com.someoneman.youliveonstream.activities.auth;

/**
 * Created by nik on 19.04.2016.
 */
public interface ITokenGetCallback {
    void OnTokenGetSucceed();
    void OnTokenGetFailed();
    void OnTokenGetStarted();
}
