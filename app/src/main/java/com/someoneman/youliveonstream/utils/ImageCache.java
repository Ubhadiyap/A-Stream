package com.someoneman.youliveonstream.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.someoneman.youliveonstream.App;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by Aleksander on 05.02.2016.
 */

public class ImageCache {

    //FIXME: НЕ ЗАБЫТЬ ПЕРЕДЕЛАТЬ НООРМАЛЬНОООО!

    private static final int DISKCACHESIZE = 1024 * 1024 * 10;

    private static ImageCache mInstance;

    private DiskLruCache mDiskLruCache;
    private String mCachePath;

    public ImageCache() {
        initCache(App.getContext());
    }

    public static ImageCache getInstance() {
        if (mInstance == null)
            mInstance = new ImageCache();

        return mInstance;
    }

    private void initCache(Context context) {
        try {
            File cacheDir = getDiskCacheDir(context, "/images");
            mCachePath = cacheDir.getAbsolutePath();
            mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISKCACHESIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap get(String key) {
        try {
            key = Utils.strToMD5(key);
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);

            if (snapshot == null)
                return  null;

            InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
                return BitmapFactory.decodeStream(bufferedInputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void put(String key, Bitmap bitmap) {
        try {
            if (bitmap == null)
                return;

            key = Utils.strToMD5(key);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);

            if (editor == null) {
                return;
            }

            OutputStream out = new BufferedOutputStream(editor.newOutputStream(0));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            mDiskLruCache.flush();
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    private Bitmap downloadBitmapUrl(String url) {
        Bitmap bitmap = null;

        try {
            InputStream in = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        put(url, bitmap);

        return bitmap;
    }

    public void getImageView(String url, ImageView imageView) {
        if (url != null && !url.isEmpty())
            new GetImageViewTask(imageView).execute(url);
    }

    public String getCachePath() {
        return getInstance().mCachePath;
    }

    //region async tasks

    private class GetImageViewTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView mImageView;

        public GetImageViewTask(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            ImageCache imageCache = getInstance();

            String url = params[0];
            Bitmap bitmap;

            if (!url.isEmpty()) {
                if ((bitmap = imageCache.get(url)) == null) {
                    bitmap = imageCache.downloadBitmapUrl(url);
                }
            } else {
                return null;
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null)
                mImageView.setImageBitmap(bitmap);
        }
    }

    //endregion
}
