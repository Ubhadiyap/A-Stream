package com.someoneman.youliveonstream.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import com.google.api.services.youtube.YouTubeScopes;
import com.someoneman.youliveonstream.App;
import com.someoneman.youliveonstream.activities.error.ErrorActivity;
import com.someoneman.youliveonstream.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Aleksander on 05.02.2016.
 */
public class Utils {

    public static final String TAG = "AStreamTag";
    public static final boolean IS_DEBUG = true;

    public static String tempBannerUri;

    public static long getMaxMemory() { //return max memory size in bytes
        Runtime rt = Runtime.getRuntime();
        return rt.maxMemory();
    }

    public static String arrayToString(String[] array) {
        return arrayToString(array, "");
    }

    public static String arrayToString(String[] array, String separator) {
        StringBuilder builder = new StringBuilder();

        for (String str : array) {
            builder.append(str);
            builder.append(separator);
        }

        return builder.toString();
    }

    public static String stringFromResourceByName(Context context, String name) {
        int resId = context.getResources().getIdentifier(name, "string", context.getPackageName());

        if (resId != 0x0)
            return context.getString(resId);
        else
            return name;
    }

    public static String intToTextFormat(Context context, double value) {
        if (value == 0)
            return context.getString(R.string.empty);
        else if (value < 1000)
            return String.valueOf((int)value);
        else if (value >= 1000 && value < 1000000)
            return new DecimalFormat("#0.0").format(value / 1000.0f) + " " + context.getString(R.string.thousand);
        else if (value >= 1000000 && value < 1000000000)
            return new DecimalFormat("#0.0").format(value / 1000000.0f) + " " + context.getString(R.string.millions);
        else
            return "PSY?";
    }

    public static String strToMD5(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder stringBuffer = new StringBuilder();
            for (byte b : digest) {
                stringBuffer.append(String.format("%02x", b & 0xff));
            }

            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int dpToPx(int dp) {
        float density = App.getContext().getResources().getDisplayMetrics().density;

        return Math.round((float)dp * density);
    }

    public static void shareBroadcast(Activity activity, com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast broadcast) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", "Стримчанский", broadcast.GetBroadcastLink()));

        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)));
    }

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void startErrorActivity(Activity activity, String errorAction) {
        Intent intent = new Intent(activity, ErrorActivity.class);
        intent.setAction(errorAction);

        activity.startActivity(intent);
        activity.finish();
    }

    public static void alertQuestionDialog(Context context, String question, String positive, String negative) {
        new AlertDialog.Builder(context)
                .setTitle(question)
                .create()
                .show();
    }

    public static String loadHttp(String url, BasicNameValuePair... values) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> httpParams = new ArrayList<>();

            if (values.length > 0) {
                Collections.addAll(httpParams, values);
            }

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(httpParams));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            InputStream inputStream = httpEntity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            inputStream.close();

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}