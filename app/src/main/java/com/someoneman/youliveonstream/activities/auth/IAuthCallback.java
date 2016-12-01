package com.someoneman.youliveonstream.activities.auth;

/**
 * Created by nik on 19.04.2016.
 */
public interface IAuthCallback {
    void OnAuthReady();
    void OnAuthSucceed(String authCode);
    void OnAuthFailed();
}
