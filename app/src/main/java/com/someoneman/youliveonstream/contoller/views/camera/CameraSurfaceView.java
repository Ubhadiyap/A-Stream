package com.someoneman.youliveonstream.contoller.views.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;

import com.someoneman.youliveonstream.streamer.ACamera;
import com.someoneman.youliveonstream.streamer.encoders.texturemovieencoder.TextureMovieEncoder;

;

/**
 * Created by nik on 10.05.2016.
 */
public class CameraSurfaceView extends GLSurfaceView implements RenderHandlerObserver {
    private final CameraSurfaceRender render;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);

        render = new CameraSurfaceRender(new RenderHandler(this));
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onPause(){
        ACamera.GetInstance().release();
        super.onPause();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                render.notifyPausing();
            }
        });

    }
    @Override
    public void onResume(){
        super.onResume();
        ACamera.GetInstance().open(1280, 720);
    }

    public void onStartTranslation(final TextureMovieEncoder targetVideoEncoder){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                render.onStart(targetVideoEncoder);
            }
        });
    }
    public void onStopTranslation(){
//        //queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                render.onStop();
//            }
//        });

    }
    private int getDeviceRotationDegress() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                return 0;

            case Surface.ROTATION_90:
                return 90;

            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
                return 270;
        }

        return 0;
    }


    @Override
    public void OnSurfaceChanged() {
        int degress = getDeviceRotationDegress();

    }

    @Override
    public void onSurfaceCreated(final SurfaceTexture st) {
        st.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });

        ACamera.GetInstance().setPreviewTexture(st);
        ACamera.GetInstance().startPreview();
    }
}
