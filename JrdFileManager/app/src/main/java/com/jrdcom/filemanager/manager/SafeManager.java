/* Copyright (C) 2016 Tcl Corporation Limited */
package com.jrdcom.filemanager.manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FileSafeBrowserActivity;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.SafeInfo;
import com.jrdcom.filemanager.utils.SafeUtils;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by user on 16-3-9.
 */
public class SafeManager {

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(3);
    private HashMap<String, Long> mSizeMap = new HashMap<String, Long>();
    private static SafeManager sInstance = new SafeManager();
    public static long mSafeCurrentmode = CommonIdentity.CATEGORY_MODE;
    public static int mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
    public FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    public static boolean notQuitSafe = false;
    public static int mCurrentSafeCategory = -1;
    public static int mCurrentmode = CommonIdentity.FILE_STATUS_NORMAL;
    public static boolean isFileTypeInterface = false;
    public static boolean isPrivateFileTypeInterface = false;

    private SafeManager() {
    }

    public static SafeManager getInstance(Context context) {
        return sInstance;
    }

    public void loadCategoryCountText(final CountTextCallback callback) {

        TaskInfo mCategoryCountTaskInfo = new TaskInfo(mApplication, null, CommonIdentity.SAFE_CATEGORY_COUNT_TASK);
        mCategoryCountTaskInfo.setSafeCountCallback(callback);
        mApplication.mFileInfoManager.addNewTask(mCategoryCountTaskInfo);

    }

    public interface CountTextCallback {
        public void countTextCallback(TaskInfo mTaskInfo);
    }


    public void putMap(String key, long value) {
        synchronized (mSizeMap) {
            mSizeMap.put(key, value);
        }
    }

    public void clearMap() {
        synchronized (mSizeMap) {
            mSizeMap.clear();
        }
    }


    public static void setCurrentMode(long mode) {
        mSafeCurrentmode = mode;
    }

    public static boolean needDecrypt(String mimeType) {
        boolean noNeedDecrpyt = mimeType.startsWith("application/zip")
                || mimeType.startsWith("application/x-rar-compressed")
                || mimeType.startsWith("application/x-tar")
                || mimeType.startsWith("application/x-7z-compressed")
                || mimeType.startsWith("application/vnd.android.package-archive");
        return !noNeedDecrpyt;
    }

    public static int getPrivateFileCount(Context mContext) {
        if(!CommonUtils.isInPrivacyMode(mContext)){
            return 0;
        }
        Cursor cursor = null;
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {"count(*)"};
        StringBuilder sb = new StringBuilder();
        sb.append(CommonIdentity.TCT_IS_PRIVATE + " = ");
        DatabaseUtils.appendEscapedSQLString(sb, "1");
        String selection = sb.toString();
        try{
            cursor = mContext.getContentResolver().query(uri, null, selection, null, null);
            if(cursor != null){
                return cursor.getCount();
            }
        } catch(Exception e){
        }

        return 0;
    }

}
