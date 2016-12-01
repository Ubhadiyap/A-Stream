package com.someoneman.youliveonstream.activities.broadcast;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.streamer.ACamera2;
import com.someoneman.youliveonstream.streamer.encoders.AudioEncoder;
import com.someoneman.youliveonstream.streamer.encoders.VideoEncoder;
import com.someoneman.youliveonstream.streamer.muxer.Muxer;
import com.someoneman.youliveonstream.utils.Log;
import com.someoneman.youliveonstream.views.autofittextureview.AutoFitTextureView;

import java.io.IOException;

/**
 * Created by ultra on 24.06.2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BroadcastActivityV21 extends AppCompatActivity {

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private AutoFitTextureView mPreviewTextureView;

    private Surface mPreviewSurface;
    private ACamera2 mAcamera2;
    private Size mPreviewSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broadcast_activity_v21);

        final Intent i = getIntent();
        final Broadcast broadcast = i.getParcelableExtra(Broadcast.EXTRA_KEY);

        mPreviewTextureView = (AutoFitTextureView) findViewById(R.id.cameraView);
        Log.I("%dx%d", mPreviewTextureView.getWidth(), mPreviewTextureView.getHeight());

        if (mPreviewTextureView.isAvailable()) {
            initSurface(mPreviewTextureView.getSurfaceTexture());
        } else {
            mPreviewTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        ToggleButton tb = (ToggleButton)findViewById(R.id.toggleStream);

        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Void>() {

                    private Muxer muxer;
                    private VideoEncoder videoEncoder;
                    private AudioEncoder audioEncoder;

                    @Override
                    protected Void doInBackground(Void... params) {
                        broadcast.CreateStream();
                        muxer = new Muxer(broadcast.getStream().mCdnSettings);

                        try {
                            audioEncoder = new AudioEncoder(muxer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        muxer.start();
                        audioEncoder.start();
                        //videoEncoder.start();
                        //mAcamera2.createCapture();

                        SurfaceTexture pre = mPreviewTextureView.getSurfaceTexture();
                        mPreviewSize = mAcamera2.getPreviewSize();
                        pre.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                        mPreviewSurface = new Surface(pre);
                        configureTransform(mPreviewTextureView.getWidth(), mPreviewTextureView.getHeight());

                        try {
                            videoEncoder = new VideoEncoder(muxer, mPreviewSize);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        videoEncoder.start();

                        mAcamera2.startRecording(mPreviewSurface, videoEncoder.getInputSurface());

                    }
                }.execute();
            }
        });

        mAcamera2 = new ACamera2(this);
        mAcamera2.open(CameraCharacteristics.LENS_FACING_BACK, 1280, 720);
    }

    private void initSurface(SurfaceTexture surfaceTexture) {
        mPreviewSize = mAcamera2.getPreviewSize();
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            mPreviewTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        else
            mPreviewTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getHeight());

        configureTransform(mPreviewTextureView.getWidth(), mPreviewTextureView.getHeight());
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mPreviewSurface = new Surface(surfaceTexture);
        mAcamera2.startPreview(mPreviewSurface);
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mPreviewTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mPreviewTextureView.setTransform(matrix);
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            initSurface(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.I("");
        }
    };

}
