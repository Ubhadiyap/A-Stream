package com.someoneman.youliveonstream.contoller.views.camera;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by nik on 15.05.16.
 */
class RenderHandler extends Handler {
    public static final int MSG_SET_SURFACE_TEXTURE = 0;
    public static final int MSG_SURFACE_CHANGED = 1;

    private WeakReference<RenderHandlerObserver> mWeakRenderObserver;

    public RenderHandler(RenderHandlerObserver renderObserver) {
        mWeakRenderObserver = new WeakReference<>(renderObserver);
    }

    public void invalidateHandler() {
        mWeakRenderObserver.clear();
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;

        RenderHandlerObserver renderHandlerObserver = mWeakRenderObserver.get();

        if (renderHandlerObserver == null) {
            return;
        }

        switch (what) {
            case MSG_SURFACE_CHANGED:
                renderHandlerObserver.OnSurfaceChanged();
                break;
            case MSG_SET_SURFACE_TEXTURE:
                renderHandlerObserver.onSurfaceCreated((SurfaceTexture) msg.obj);
                break;
            default:
                throw new RuntimeException("unknown msg " + what);
        }
    }
}