package com.jrdcom.filemanager.singleton;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;

import java.util.TimerTask;


/**
 * Created by user on 16-7-11.
 */
public class DataContentObserver extends ContentObserver {
    private static final String TAG = DataContentObserver.class.getSimpleName();
    private static FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    public static ResultTaskHandler mResultHandler = mApplication.getAppHandler();
    private static DataContentObserver mDataContentObserver;
    private boolean isTimerStarted = false;
    private FilesTimer mFileTimer = null;

    private DataContentObserver(Handler handler, Context context) {
        super(handler);

        /* mFileTimer = new FilesTimer();
        try {
            Log.d(TAG, "Timer schedule start ==>"+isTimerStarted);
            if (!isTimerStarted) {
                isTimerStarted = true;
                mFileTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Log.d(TAG, "***** task  start run");
                        if(mFileTimer != null) {
                            mFileTimer.changeFileInfo();
                        }
                    }
                }, 5000, 5000);
            }
        } catch (Exception e) {
            Log.d(TAG, "Timer schedule start exception ==>"+e);
        } */
    }

    public static DataContentObserver getInstance() {
        if(mDataContentObserver == null){
            mDataContentObserver = new DataContentObserver(mResultHandler, mApplication);
        }
        return mDataContentObserver;
    }

    public void startFileTimerWatcher() {
        if (!isTimerStarted) {
            Log.d(TAG, "startFileTimerWatcher()");
            mFileTimer = new FilesTimer();
            try {
                isTimerStarted = true;
                mFileTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if(mFileTimer != null) {
                            mFileTimer.changeFileInfo();
                        }
                    }
                }, 120, 5000);
            } catch (Exception e) {
                Log.d(TAG, "Exception occurred when startFileTimerWatcher:", e);
            }
        }
    }

    public void cancelFileTimerWatcher() {
        if(mFileTimer != null) {
            Log.d(TAG, "cancelFileTimerWatcher()");
            mFileTimer.cancel();
            mFileTimer = null;
            isTimerStarted = false;
        }
    }


    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "onchange name ==>");
//        try {
//            if (isTimerStarted) {
//                isTimerStarted = false;
//                mFileTimer.schedule(task,5000, 5000);
//            }
//
//        } catch (Exception e) {
//            Log.d(TAG, "Timer schedule start exception ==>"+e);
//        }
    }

    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            if(mFileTimer != null) {
                mFileTimer.changeFileInfo();
            }
        }
    };
}
