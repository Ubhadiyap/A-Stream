package com.someoneman.youliveonstream.streamer;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Size;

import com.someoneman.youliveonstream.utils.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ultra on 16.04.2016.
 */
public class ACamera {

    private static ACamera instance;

    public enum Facing {FRONT, BACK};

    private int mFrontCameraId = -1, mBackCameraId = -1;
    private Facing mFacing;
    private int mCameraId;
    private Camera mCamera;
    private boolean mFlashlight;
    private Camera.Size mVideoSize;

    public static ACamera GetInstance() {
        if (instance == null)
            instance = new ACamera();

        return instance;
    }

    public void initialize() {
        initializeCamerasInfo();
        initializeStartCameraId();
    }

    private void initializeCamerasInfo() {
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
            }
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mBackCameraId = i;
            }
        }
    }

    private void initializeStartCameraId() {
        if (isBackAvailable())
            mFacing = Facing.BACK;
        else if (isFrontAvailable())
            mFacing = Facing.FRONT;
    }

    /*
    public static void switchCamera() {
        if (mFacing == Facing.FRONT && isBackAvailable())
            open(Facing.BACK);
        else if (mFacing ==Facing.BACK && isFrontAvailable())
            open(Facing.FRONT);
    }*/

    public void open(int width, int height) {
        open(mFacing, width, height);
    }

    public void open(Facing facing, int width, int height) {
        if (facing == Facing.FRONT)
            open(mFrontCameraId, width, height);
        else if (facing == Facing.BACK)
            open(mBackCameraId, width, height);

        mFacing = facing;
    }

    private void open(int cameraId, int width, int height) {
        if (mCamera != null);

        mCamera = Camera.open(cameraId);

        if (mCamera == null);

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(true);
        //parameters.setVideoStabilization(true);

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);


        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

        if (parameters.isAutoWhiteBalanceLockSupported()) {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            parameters.setAutoWhiteBalanceLock(true);
        }

        parameters.setPreviewFormat(ImageFormat.NV21);
       // parameters.set("orientation", "landscape");
        //parameters.setRotation(0);

        mVideoSize = chooseCameraSize(parameters.getSupportedPreviewSizes(), width, height);

       // mCamera.setDisplayOrientation(0);
        parameters.setPreviewSize(mVideoSize.width, mVideoSize.height);

        chooseFixedPreviewFps(parameters, 30000);

        if (mFlashlight && cameraId == mBackCameraId)
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        else
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);


        //FIXME: SAMSUNGS FAILS HERE
        mCamera.setParameters(parameters);

        mCameraId = cameraId;
    }

    public Camera.Size getVideoSize() {
        return mVideoSize;
    }

    public static Camera.Size chooseCameraSize(List<Camera.Size> choices, int width, int height) {
        List<Camera.Size> bigEnough = new ArrayList<>();

        for (Camera.Size option : choices) {
            if (option.height == option.width * height / width && option.width >= width && option.height >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0)
            return Collections.min(bigEnough, new CompareSizeByArea());
        else
            return choices.get(0);
    }

    public void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setCameraOrientation(int degress, int width, int height) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        int result;

        if (mFacing == Facing.FRONT) {
            result = (cameraInfo.orientation + degress) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degress + 360) % 360;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        choosePreviewSize(parameters, width, height);

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(result);
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPreview() {
        mCamera.startPreview();
    }

    public void flashlight(boolean enable) {
        if (mCamera == null);

        Camera.Parameters parameters = mCamera.getParameters();

        if (enable)
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        else
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

        mCamera.setParameters(parameters);
        mFlashlight = enable;
    }

    private static class CompareSizeByArea implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long)lhs.width * (long)lhs.height - (long)rhs.width * (long)rhs.height);
        }
    }

    public static void choosePreviewSize(Camera.Parameters parameters, int width, int height) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size selectedSize = sizes.get(0);

        for (Camera.Size size : sizes) {
            int difference0 =  Math.abs((width + height) - (selectedSize.width + selectedSize.height));
            int difference1 = Math.abs((width + height) - (size.width + size.height));

            if (difference1 < difference0) {
                selectedSize = size;
            }
        }

        parameters.setPreviewSize(selectedSize.width, selectedSize.height);
    }

    public static void chooseFixedPreviewFps(Camera.Parameters parameters, int desiredThousandFps) {
        List<int[]> supported = parameters.getSupportedPreviewFpsRange();
        int[] selectedEntry = supported.get(0);

        for (int[] entry : supported) {
            int difference0 = Math.abs(entry[1] - desiredThousandFps);
            int difference1 = Math.abs(selectedEntry[1] - desiredThousandFps);

            if (difference1 < difference0) {
                selectedEntry = entry;
            } else if (difference1 == difference1 && entry[0] > selectedEntry[0]) {
                selectedEntry = entry;
            }
        }

        parameters.setPreviewFpsRange(selectedEntry[0], selectedEntry[1]);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean isFrontAvailable() {
        return mFrontCameraId != -1;
    }

    public boolean isBackAvailable() {
        return mBackCameraId != -1;
    }

    public boolean isAnyAvailable() {
        return isFrontAvailable() || isBackAvailable();
    }

    public boolean isBoothAvailable() {
        return isFrontAvailable() && isBackAvailable();
    }

}
