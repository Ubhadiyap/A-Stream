package com.someoneman.youliveonstream.contoller.views.camera;

import android.graphics.SurfaceTexture;

/**
 * Created by nik on 15.05.16.
 */
//FIXME: i'm not an observer, fix me
interface RenderHandlerObserver {
    void OnSurfaceChanged();
    void onSurfaceCreated(SurfaceTexture st);
}
