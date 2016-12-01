package com.someoneman.youliveonstream.model.camera;

import android.graphics.SurfaceTexture;

import com.someoneman.youliveonstream.streamer.ACamera;

/**
 * Created by nik on 22.04.2016.
 */
public class CameraManager {

    private static CameraManager instance;

    private CameraManager(){
        ACamera.GetInstance().initialize();
        ACamera.GetInstance().open(1280, 720);
    }

    public static void initCameraManager(){
        //TODO: destroy previous camera manager
        instance = new CameraManager();
    }

    public static CameraManager getInstance() {
        return instance;
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture){
        ACamera.GetInstance().setPreviewTexture(surfaceTexture);
        ACamera.GetInstance().startPreview();
    }

    public void flashlight(boolean enable){
        ACamera.GetInstance().flashlight(enable);
    }

    public void release(){
        ACamera.GetInstance().release();
        instance = null;
    }


}
