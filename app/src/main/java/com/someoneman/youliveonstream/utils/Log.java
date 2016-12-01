package com.someoneman.youliveonstream.utils;

/**
 * Created by Aleksander on 06.02.2016.
 */
public class Log {
    private static void Log(int type, String message) {
        if (Utils.IS_DEBUG)
            android.util.Log.println(type, Utils.TAG, message);
    }

    private static void Log(int type, String format, Object... values) {
        Log(type, String.format(format, values));
    }

    public static void V(String message) {
        Log(android.util.Log.VERBOSE, message);
    }

    public static void V(String format, Object... values) {
        Log(android.util.Log.VERBOSE, format, values);
    }

    public static void D(String message) {
        Log(android.util.Log.DEBUG, message);
    }

    public static void D(String format, Object... values) {
        Log(android.util.Log.DEBUG, format, values);
    }

    public static void I(String message) {
        Log(android.util.Log.INFO, message);
    }

    public static void I(String format, Object... values) {
        Log(android.util.Log.INFO, format, values);
    }

    public static void W(String message) {
        Log(android.util.Log.WARN, message);
    }

    public static void W(String format, Object... values) {
        Log(android.util.Log.WARN, format, values);
    }

    public static void E(String message) {
        Log(android.util.Log.ERROR, message);
    }

    public static void E(String format, Object... values) {
        Log(android.util.Log.ERROR, format, values);
    }
}
