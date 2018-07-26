
package com.jrdcom.filemanager.utils;

import android.util.Log;

public final class LogUtils {

    public static final boolean DEBUG = true;

    /**
     * The method prints the log, level error
     *
     * @param msg the message to print
     */
    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    /**
     * The method prints the log, level error
     *
     * @param msg   the message to print
     * @param throw an exception to log
     */
    public static void e(String tag, String msg, Throwable t) {
        Log.e(tag, msg, t);
    }

    /**
     * The method prints the log, level warning
     *
     * @param msg the message to print
     */
    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    /**
     * The method prints the log, level warning
     *
     * @param msg   the message to print
     * @param throw an exception to log
     */
    public static void w(String tag, String msg, Throwable t) {
        Log.w(tag, msg, t);
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg the message to print
     */
    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg   the message to print
     * @param throw an exception to log
     */
    public static void i(String tag, String msg, Throwable t) {
        Log.i(tag, msg, t);
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg the message to print
     */
    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg   the message to print
     * @param throw an exception to log
     */
    public static void d(String tag, String msg, Throwable t) {
        Log.d(tag, msg, t);
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg the message to print
     */
    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg   the message to print
     * @param throw an exception to log
     */
    public static void v(String tag, String msg, Throwable t) {
        Log.v(tag, msg, t);
    }
}