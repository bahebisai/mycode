package com.jrdcom.filemanager.utils;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.PlfUtils;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.listener.HeavyOperationListener;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.singleton.TaskInfoMap;
import com.jrdcom.filemanager.task.ProgressInfo;
import com.tcl.faext.FAExt;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    private static final String TAG = CommonUtils.class.getSimpleName();

    public static Typeface getRobotoMedium() {
        Typeface tf = Typeface.createFromFile(CommonIdentity.FONT_ROBOTO_MEDIUM_FILE);
        return tf;
    }

    public static int getTotalWidthofListView(AdapterView<ListAdapter> listView) {
        if(listView == null){
            return 0;
        }
        ListAdapter mAdapter = listView.getAdapter();
        if (mAdapter == null) {
            return 0;
        }
        int totalWidth = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            totalWidth += mView.getMeasuredWidth();

        }
        return totalWidth;
    }

    // get screen wid and hei
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        return width;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    public static long getYesterdayTime() {
        return (System.currentTimeMillis() - 86400000 * 2) / 1000;
    }


    public static boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= 23;
    }

    public static boolean hasN() {
        return Build.VERSION.SDK_INT >= 24;
    }

    public static boolean hasBelowN() {
        return Build.VERSION.SDK_INT < 24;
    }

    public static boolean hasHighN() {
        return Build.VERSION.SDK_INT > 24;
    }

    public static boolean isPhoneStorageZero() {
        MountManager mMountManager = MountManager.getInstance();
        long blocSize = 0;
        long blockCount = 0;
        try {
            String filePath = mMountManager.getPhonePath();
            if (filePath != null) {
                StatFs statfs = new StatFs(filePath);
                try {
                    blocSize = statfs.getBlockSizeLong();
                    blockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    blocSize = statfs.getBlockSizeLong();
                    blockCount = statfs.getBlockCountLong();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (blocSize * blockCount == 0) {
            return true;
        }
        return false;
    }


    public static boolean isMemory512(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        if (info.totalMem <= 536870912) {
            return true;
        } else {
            return false;
        }
    }

    public static int getRefreshMode(String path, int category) {
        int mRefreshMode = -1;
        if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE
                && 0 <= category && category <= 11) {
            mRefreshMode = CommonIdentity.REFRESH_FILE_CATEGORY_MODE;
        } else if (CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE
                && path != null) {
            mRefreshMode = CommonIdentity.REFRESH_FILE_PATH_MODE;
        } else {
            mRefreshMode = CommonIdentity.REFRESH_FILE_PATH_MODE;
        }
        return mRefreshMode;
    }

    public static int getCategoryRefreshMode(int locationMode) {

        if (locationMode == CommonIdentity.FILE_MANAGER_LOCATIONE) {
            return CommonIdentity.REFRESH_FILE_CATEGORY_MODE;
        } else {
            return CommonIdentity.REFRESH_SAFE_CATEGORY_MODE;
        }
    }


    public static void returnTaskResult(TaskInfo mTaskInfo, int errorCode) {
        mTaskInfo.setResultCode(errorCode);
        mTaskInfo.getListener().onTaskResult(mTaskInfo);

    }

    public static String getStorageRootPath(String tag, MountManager mMountManager) {
        if (tag.equals(CommonIdentity.PHONE_TAG)) {
            return mMountManager.getPhonePath();
        } else if (tag.equals(CommonIdentity.SDCARD_TAG)) {
            return mMountManager.getSDCardPath();
        } else if (tag.equals(CommonIdentity.USBOTG_TAG)) {
            return mMountManager.getUsbOtgPath();
        }
        return mMountManager.getPhonePath();
    }

    public static boolean isSafeFileView(FileManagerApplication mApplication) {
        return (mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION || mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION)
                && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE;
    }


    public static ProgressInfo getProgressInfo(FileInfo info, int mTaskType, long mCreateTime, int progress, int total) {
        ProgressInfo mProgressInfo = new ProgressInfo(info, progress, total);
        mProgressInfo.setProgressTaskType(mTaskType);
        mProgressInfo.setCreateTime(mCreateTime);
        return mProgressInfo;
    }

    public static ProgressInfo getProgressInfo(String name, int mTaskType, long mCreateTime, int progress, int total) {
        ProgressInfo mProgressInfo = new ProgressInfo(name, progress, total);
        mProgressInfo.setProgressTaskType(mTaskType);
        mProgressInfo.setCreateTime(mCreateTime);
        return mProgressInfo;
    }

    public static ProgressInfo getProgressInfo(String name, int mTaskType, long mCreateTime, int progress, int total, int mode) {
        ProgressInfo mProgressInfo = new ProgressInfo(name, progress, total);
        mProgressInfo.setProgressTaskType(mTaskType);
        mProgressInfo.setCreateTime(mCreateTime);
        mProgressInfo.setUnitStyle(mode);
        return mProgressInfo;
    }

    public static ProgressInfo getProgressInfo(String name, long progress, long total, TaskInfo info) {
        ProgressInfo mProgressInfo = new ProgressInfo(name, progress, total);
        mProgressInfo.setTaskInfo(info);
        return mProgressInfo;
    }

    public static TaskInfo getListenerInfo(String title, long mCreateTime, int mTaskType, int mode) {
        TaskInfo mListenerInfo = new TaskInfo(CommonIdentity.PROGRESS_DIALOG_TASK);
        mListenerInfo.setFileFilter(mTaskType);
        mListenerInfo.setTitleStr(title);
        mListenerInfo.setAdapterMode(mode);
        mListenerInfo.setCreateTaskTime(mCreateTime);
        return mListenerInfo;
    }

    public static TaskInfo getListenerInfo(String title, long mCreateTime, int mTaskType, int mode, int category) {
        TaskInfo mListenerInfo = new TaskInfo(CommonIdentity.PROGRESS_DIALOG_TASK);
        mListenerInfo.setFileFilter(mTaskType);
        mListenerInfo.setTitleStr(title);
        mListenerInfo.setAdapterMode(mode);
        mListenerInfo.setCreateTaskTime(mCreateTime);
        mListenerInfo.setCategoryIndex(category);
        return mListenerInfo;
    }


    public static boolean isPathNormalMode(int mFileMode) {
        return CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE
                && mFileMode == CommonIdentity.FILE_STATUS_NORMAL || mFileMode == CommonIdentity.FILE_COPY_NORMAL;
    }

    public static boolean isCategoryNormalMode(int mFileMode) {
        return CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE
                && mFileMode == CommonIdentity.FILE_STATUS_NORMAL;
    }

    public static Drawable getActionbarItemTheme(Context context) {
        int[] attrs = new int[]{android.R.attr.actionBarItemBackground};
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs);
        Drawable d = a.getDrawable(0);
        return d;
    }

    public static void getBaseTaskInfo(FileManagerApplication mApplication, int mRefreshMode, int listenerMode, int mode, String title, int mBaseType, int category,
                                       String mSrcPath, String mDesPath, int mDrmType, boolean isDir, FileInfo mSrcInfo) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, new HeavyOperationListener(CommonUtils.getListenerInfo(title, mCreateTime,
                mBaseType, listenerMode, category)), mBaseType);
        mTaskInfo.setCategoryIndex(category);
        mTaskInfo.setRefreshMode(mRefreshMode);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setAdapterMode(mode);
        mTaskInfo.setDestPath(mDesPath);
        mTaskInfo.setSrcPath(mSrcPath);
        mTaskInfo.setDrmType(mDrmType);
        mTaskInfo.setShowDir(isDir);
        mTaskInfo.setSrcFile(mSrcInfo);
        TaskInfoMap.addtaskInfo(mCreateTime,mTaskInfo);
        mApplication.mFileInfoManager.addNewTask(mTaskInfo);
    }

    public static void getBaseTaskInfo(FileManagerApplication mApplication, int listenerMode, String title, int mBaseType, int category, String mQuery, int mFilter, List<FileInfo> mSourceList, String mDesPath, List<String> mSourcePath) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, new HeavyOperationListener(CommonUtils.getListenerInfo(title, mCreateTime, mBaseType, listenerMode)), mBaseType);
        mTaskInfo.setCategoryIndex(category);
        mTaskInfo.setSearchContent(mQuery);
        mTaskInfo.setSourceFileList(mSourceList);
        mTaskInfo.setFileFilter(mFilter);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setDestPath(mDesPath);
        mTaskInfo.setDesPathList(mSourcePath);
        TaskInfoMap.addtaskInfo(mCreateTime,mTaskInfo);
        mApplication.mFileInfoManager.addNewTask(mTaskInfo);
    }

    public static void getBaseTaskInfo(FileManagerApplication mApplication, int listenerMode, String title, int mBaseType, int category, String mQuery, int mFilter, List<FileInfo> mSourceList, String mDesPath, List<String> mSourcePath, boolean isNoExpireLimited) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, new HeavyOperationListener(CommonUtils.getListenerInfo(title, mCreateTime, mBaseType, listenerMode)), mBaseType);
        mTaskInfo.setCategoryIndex(category);
        mTaskInfo.setSearchContent(mQuery);
        mTaskInfo.setSourceFileList(mSourceList);
        mTaskInfo.setFileFilter(mFilter);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setDestPath(mDesPath);
        mTaskInfo.setDesPathList(mSourcePath);
        mTaskInfo.setShowDir(isNoExpireLimited);
        TaskInfoMap.addtaskInfo(mCreateTime,mTaskInfo);
        mApplication.mFileInfoManager.addNewTask(mTaskInfo);
    }

    public static void getBaseTaskInfo(FileManagerApplication mApplication, int listenerMode, String title, int mBaseType, int mFilter, int mode, FileInfo mSrcInfo, FileInfo mDesInfo, String mDesPath, String mquery, List<FileInfo> mSrcList, boolean isDir, int category) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, new HeavyOperationListener(CommonUtils.getListenerInfo(title, mCreateTime,
                mBaseType, listenerMode)), mBaseType);
        mTaskInfo.setFileFilter(mFilter);
        mTaskInfo.setSrcFile(mSrcInfo);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setDstFile(mDesInfo);
        mTaskInfo.setAdapterMode(mode);
        mTaskInfo.setDestPath(mApplication.mCurrentPath);
        mTaskInfo.setSearchContent(mquery);
        mTaskInfo.setDestPath(mDesPath);
        mTaskInfo.setSourceFileList(mSrcList);
        mTaskInfo.setShowDir(isDir);
        mTaskInfo.setCategoryIndex(category);
        ProgressInfo progressInfo = new ProgressInfo("",0,0);
        progressInfo.setCreateTime(mCreateTime);
        mTaskInfo.setProgressInfo(progressInfo);
        TaskInfoMap.addtaskInfo(mCreateTime,mTaskInfo);
        mApplication.mFileInfoManager.addNewTask(mTaskInfo);
    }

    public static TaskInfo getBaseTaskInfo(FileManagerApplication mApplication, OperationEventListener listener, int mBaseType, int mFilter, int mode, TextView mTextSize, ProgressBar mGressBar, String mSrcPath) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, listener, mBaseType);
        mTaskInfo.setFileFilter(mFilter);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setAdapterMode(CommonIdentity.STORAGE_INFO_CATEGORY);
        mTaskInfo.setStorageSize(mTextSize);
        mTaskInfo.setStorageProgress(mGressBar);
        mTaskInfo.setSrcPath(mSrcPath);
        mTaskInfo.setFileFilter(mFilter);
        return mTaskInfo;
    }

    public static boolean isDRMColumn(Context mContext) {
        Cursor cursor = null;
        try {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{"is_drm"};
            cursor = mContext.getContentResolver().query(uri, projection, null,
                    null, null);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if(Build.HARDWARE.startsWith("qcom")){
            return true;
        }
        return false;
    }

    public static boolean isTctPrivateColumn(Context mContext) {
        Cursor cursor = null;
        try {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{CommonIdentity.TCT_IS_PRIVATE};
            cursor = mContext.getContentResolver().query(uri, projection, null,
                    null, null);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    /**
     * Media queries the database if the file exists
     */
    public static boolean isDBFileExist(Context mContext,File mFile,int mCategory) {
        Cursor cursor = null;
        Uri uri = MediaStore.Files.getContentUri("external");
        try {
            if (CommonIdentity.CATEGORY_PICTURES == mCategory) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (CommonIdentity.CATEGORY_VEDIOS == mCategory) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (CommonIdentity.CATEGORY_MUSIC == mCategory) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String where = MediaStore.Files.FileColumns.DATA +"=?";
            cursor = mContext.getContentResolver().query(uri, null, where,
                    new String[]{mFile.getAbsolutePath()}, null);
            if(cursor != null && cursor.getCount() == 1){
                return true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public static int getGridColumn(FileManagerApplication mApplication) {
        if (mApplication.mPortraitOrientation/*||mApplication.isInMultiWindowMode*/) {
            return 3;
        } else {
            return 5;
        }
    }

    public static boolean isActivityMultiWindowMode(String mActivity) {
        try {
            Class cls = Class.forName(mActivity);
            Constructor assetMagCt = cls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            Method med = cls.getMethod("isInMultiWindowMode", (Class[]) null);
            boolean isActivityMultiWindow = (boolean) med.invoke(assetMag, (Object[]) null);
            Log.d(TAG, "is Activity multi window--" + isActivityMultiWindow);
            return isActivityMultiWindow;
        } catch (Exception e) {
            Log.d(TAG, "is Activity multi window--exception--" + e);
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFragmentMultiWindowMode(String mFragment) {
        try {
            Class cls = Class.forName(mFragment);
            Constructor assetMagCt = cls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            Method med = cls.getMethod("isInMultiWindowMode", (Class[]) null);
            boolean isFragmentMultiWindow = (boolean) med.invoke(assetMag, (Object[]) null);
            Log.d(TAG, "is fragment multi window--" + isFragmentMultiWindow);
            return isFragmentMultiWindow;
        } catch (Exception e) {
            Log.d(TAG, "is fragment multi window--exception--" + e);
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isMultiWindowMode(FileManagerApplication mApplication, String mActivity, String mFragment) {
        boolean isFragmentMultiWindow = CommonUtils.isFragmentMultiWindowMode(mFragment);
        boolean isActivityMultiWindow = CommonUtils.isActivityMultiWindowMode(mActivity);
        if (isActivityMultiWindow || isFragmentMultiWindow) {
            return true;
        } else if (mApplication.isInMultiWindowMode) {
            return true;
        }
        return false;
    }

    public static boolean isCategoryMode() {
        return CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && CategoryManager.mCurrentCagegory >= 0 && CategoryManager.mCurrentCagegory < 9;
    }

    public static boolean isRecentCategoryMode() {
        return CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && CategoryManager.mCurrentCagegory == 0;
    }

    public static boolean isPathMode(String path) {
        return CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE && !TextUtils.isEmpty(path);
    }

    public static void changeLight(ImageView imageView, int brightness) {
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
        imageView.setColorFilter(new ColorMatrixColorFilter(cMatrix));
    }

    public static void addCache(Context mContext, String mSrcPath, String mDstPath, FileListCache mListCache, MountManager mMountManager,int category) {
        FileInfo mDstFileInfo = new FileInfo(mContext, mSrcPath);
        File mFile = mDstFileInfo.getFile();
        mDstFileInfo.updateSizeAndLastModifiedTime(mFile);
        mDstFileInfo.setHideFile(mFile.isHidden());
        mDstPath = mFile.getParent();
        mListCache.addCacheFiles(mDstPath, mDstFileInfo);
        boolean isDir = mFile.isDirectory();
        if (!isDir && category >= 0 && mListCache.hasCachedPath(String.valueOf(category))) {
            mListCache.addCacheFiles(String.valueOf(category), mDstFileInfo);
        }
        if (!isDir && CommonUtils.isDownLoadFile(mDstPath,mMountManager)) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD), mDstFileInfo);
        }
        if (!isDir && CommonUtils.isBluetoothFile(mDstPath,mMountManager)) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH), mDstFileInfo);
        }
        if (!isDir && !mDstPath.endsWith("/.thumbnails") && mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_RECENT))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_RECENT), mDstFileInfo);
        }
    }

    public static void deleteCache(FileInfo sourceFile, int category, FileListCache mListCache) {
        if (sourceFile == null || sourceFile.getFile() == null) {
            return;
        }
        File mSrcFile = sourceFile.getFile();
        mListCache.deleteCacheFiles(mSrcFile.getParent(), sourceFile);
        if (category >= 0) {
            mListCache.deleteCacheFiles(category, mSrcFile.getParent(), sourceFile);
        }
        mListCache.deleteCacheFiles(CommonIdentity.CATEGORY_DOWNLOAD, mSrcFile.getParent(), sourceFile);
        mListCache.deleteCacheFiles(CommonIdentity.CATEGORY_BLUETOOTH, mSrcFile.getParent(), sourceFile);
        mListCache.deleteCacheFiles(CommonIdentity.CATEGORY_RECENT, mSrcFile.getParent(), sourceFile);
    }

    public static void addCache(Context mContext, FileInfo info, FileListCache mListCache, int category,MountManager mMountManager,boolean isCopyCut) {
        File mFile = info.getFile();
        String mDstPath = mFile.getParent();
        info.updateSizeAndLastModifiedTime(mFile);
        if (!isCopyCut || mListCache.getCacheSize() < 49 || mListCache.hasCachedPath(mDstPath)) {
            mListCache.addCacheFiles(mDstPath, info);
        }
        if (mFile.isDirectory()) return;
        if (category >= 0 && mListCache.hasCachedPath(String.valueOf(category))) {
            mListCache.addCacheFiles(String.valueOf(category), info);
        }
        if (CommonUtils.isDownLoadFile(mDstPath, mMountManager) && (!isCopyCut || mListCache.getCacheSize() < 49) && mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD), info, true);
        } else if (CommonUtils.isBluetoothFile(mDstPath, mMountManager) && (!isCopyCut || mListCache.getCacheSize() < 49) && mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH), info, true);
        }
        if (!mDstPath.endsWith("/.thumbnails") && mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_RECENT))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_RECENT), info);
        }
    }

    public static void addCache(Context mContext, FileInfo info, FileListCache mListCache, MountManager mMountManager,boolean isObserver) {
        File mFile = info.getFile();
        String mDstPath = mFile.getParent();
        if (!mFile.exists()) {
            mListCache.deleteCacheFiles(mDstPath, info);
            return;
        }
        info.updateSizeAndLastModifiedTime(mFile);
        if(mListCache.hasCachedPath(mDstPath)){
            mListCache.addCacheFiles(mDstPath, info, isObserver);
        }
        int category = -1;
        boolean isDir = mFile.isDirectory();
        if (!isDir) {
            category = isObserver?FileUtils.getFileCategory(mFile.getName()):getFileCategory(info.getMime());
        }
        if (!isDir && mListCache.hasCachedPath(String.valueOf(category))) {
            mListCache.addCacheFiles(String.valueOf(category), info, true);
        }
        if (!isDir && CommonUtils.isDownLoadFile(mDstPath,mMountManager)&&mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD), info, true);
        }
        if (!isDir && CommonUtils.isBluetoothFile(mDstPath,mMountManager)&&mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH), info, true);
        }
        if (!isDir && !mDstPath.endsWith("/.thumbnails") && mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_RECENT))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_RECENT), info, true);
        }
    }


    public static void addCache(Context mContext, FileInfo info, FileListCache mListCache, MountManager mMountManager) {
        File mFile = info.getFile();
        String mDstPath = mFile.getParent();
        if (!mFile.exists()) {
            mListCache.deleteCacheFiles(mDstPath, info);
            return;
        }
        info.updateSizeAndLastModifiedTime(mFile);
        if(mListCache.hasCachedPath(mDstPath)){
            mListCache.addCacheFiles(mDstPath, info, false);
        }
        int category = -1;
        boolean isDir = mFile.isDirectory();
        if (!isDir) {
            category = getFileCategory(info.getMime());
        }
        if (!isDir && mListCache.hasCachedPath(String.valueOf(category))) {
            mListCache.addCacheFiles(String.valueOf(category), info, true);
        }
        if (!isDir && CommonUtils.isDownLoadFile(mDstPath,mMountManager)&&mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD), info, true);
        }
        if (!isDir && CommonUtils.isBluetoothFile(mDstPath,mMountManager)&&mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH))) {
            mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH), info, true);
        }
        if (!isDir && !mDstPath.endsWith("/.thumbnails") && mListCache.hasCachedPath(String.valueOf(CommonIdentity.CATEGORY_RECENT))
                && mListCache.get(String.valueOf(CommonIdentity.CATEGORY_RECENT)).contains(info)) {
                mListCache.addCacheFiles(String.valueOf(CommonIdentity.CATEGORY_RECENT), info, true);
            }
    }

    /**
     * Gets the default date format for the system
     *
     * @param context
     * @return
     */
    public static SimpleDateFormat getSystemDateFormat(Context context) {
        String strDateFormat = "yyyy-MM-dd HH:mm";
        try {
            //read data format from System Setting
            ContentResolver cv = context.getContentResolver();
            strDateFormat = Settings.System.getString(cv, android.provider.Settings.System.DATE_FORMAT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(strDateFormat)) {
            strDateFormat = "yyyy-MM-dd";
        }

        strDateFormat = getDateFormatPattern(context, strDateFormat);

        return new SimpleDateFormat(strDateFormat + " HH:mm");
    }

    /**
     * get date format pattern string from settings
     *
     * @param mContext
     * @param strDateFormat
     * @return
     */
    private static String getDateFormatPattern(Context mContext, String strDateFormat) {
        String pattern = null;
        try {
            Class clazz = Class.forName("android.text.format.DateFormat");
            Method method = clazz.getDeclaredMethod("getDateFormatForSetting", Context.class, String.class);
            SimpleDateFormat dateFormat = (SimpleDateFormat) method.invoke(null, mContext, strDateFormat);
            pattern = dateFormat.toPattern();
        } catch (Exception e) {
//            e.printStackTrace();
        }

        // if got pattern if empty, return the value before
        if (TextUtils.isEmpty(pattern)) {
            pattern = strDateFormat;
        }
        return pattern;
    }

    public static boolean isListMode(FileManagerApplication mApplication) {
        return mApplication.mViewMode.equals(CommonIdentity.LIST_MODE);
    }

    public static boolean isGridMode(FileManagerApplication mApplication) {
        return mApplication.mViewMode.equals(CommonIdentity.GRID_MODE);
    }

    public static boolean isPathMode() {
        return CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE;
    }

    public static boolean isPhoneTag(String tag) {
        return tag.equals(CommonIdentity.PHONE_TAG);
    }

    public static boolean isSDCARDTag(String tag) {
        return tag.equals(CommonIdentity.SDCARD_TAG);
    }

    public static boolean isCategoryTag(String tag) {
        return tag.equals(CommonIdentity.CATEGORY_TAG);
    }

    public static boolean isPermissionTag(String tag) {
        return tag.equals(CommonIdentity.PERMISSION_TAG);
    }

    public static boolean isOTGUSBTag(String tag) {
        return tag.equals(CommonIdentity.USBOTG_TAG);
    }

    public static boolean isGlobalSearchTag(String tag) {
        return tag.equals(CommonIdentity.GLOBAL_SEARCH);
    }

    public static boolean isShowProgressDialog(int mTaskType) {
        if (mTaskType == CommonIdentity.SEARCH_INFO_TASK) {
            return CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && CategoryManager.mCurrentCagegory < 0;
        }
        return true;
    }

    public static boolean isShowCircularProgressBar(int mTaskType) {
        return mTaskType == CommonIdentity.LIST_INFO_TASK;
    }

    public static boolean isShowHorizontalProgressBar(int mTaskType) {
        return mTaskType != CommonIdentity.LIST_INFO_TASK;
    }

    public static String getCurrentTag(MountManager mountManager, String mCurrentPath) {
        try {
            if (mountManager == null || TextUtils.isEmpty(mCurrentPath)) return null;

            String startString = mountManager.getPhonePath();
            if (startString != null && mCurrentPath != null && mCurrentPath.startsWith(startString)) {
                return CommonIdentity.PHONE_TAG;
            }

            startString = mountManager.getSDCardPath();
            if (startString != null && mCurrentPath != null && mCurrentPath.startsWith(startString)) {
                return CommonIdentity.SDCARD_TAG;
            }

            startString = mountManager.getUsbOtgPath();
            if (startString != null && mCurrentPath != null && mCurrentPath.startsWith(startString)) {
                return CommonIdentity.USBOTG_TAG;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean isPathMultiScreenChanage(String mTagMode, FileManagerApplication mApplication) {
        return (CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE || CommonUtils.isPhoneTag(mTagMode) ||
                CommonUtils.isSDCARDTag(mTagMode) || CommonUtils.isOTGUSBTag(mTagMode)) && !TextUtils.isEmpty(mApplication.mCurrentPath);
    }

    public static boolean isNormalStatus(FileManagerApplication mApplication) {
        return mApplication.mCurrentStatus == CommonIdentity.FILE_STATUS_NORMAL;
    }

    public static boolean isEditStatus(FileManagerApplication mApplication) {
        return mApplication.mCurrentStatus == CommonIdentity.FILE_STATUS_EDIT;
    }

    public static boolean isGlobalSearchStatus(FileManagerApplication mApplication) {
        return mApplication.mCurrentStatus == CommonIdentity.FILE_STATUS_GLOBALSEARCH;
    }

    public static boolean isSearchStatus(FileManagerApplication mApplication) {
        return mApplication.mCurrentStatus == CommonIdentity.FILE_STATUS_SEARCH;
    }

    public static boolean isCopyNormalStatus(FileManagerApplication mApplication) {
        return mApplication.mCurrentStatus == CommonIdentity.FILE_COPY_NORMAL;
    }

    public static void hideSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null && activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static boolean isInMultiWindowMode(Activity activity) {
        if(activity == null) return false;
        return Build.VERSION.SDK_INT >= 24 && activity.isInMultiWindowMode();
    }

    // get file category
    public static int getFileCategory(String mMime) {
        if (mMime == null) {
            return CommonIdentity.UNKNOWN_TASK;
        }
        if (CommonIdentity.videoMimeTypeList.contains(mMime)) {
            return CommonIdentity.CATEGORY_VEDIOS;
        } else if (CommonIdentity.audioMimeTypeList.contains(mMime)) {
            return CommonIdentity.CATEGORY_MUSIC;
        } else if (CommonIdentity.imageMimeTypeList.contains(mMime)) {
            return CommonIdentity.CATEGORY_PICTURES;
        } else if (mMime.equals("application/vnd.android.package-archive")) {
            return CommonIdentity.CATEGORY_APKS;
        } else if (CommonIdentity.docMimeTypeList.contains(mMime)) {
            return CommonIdentity.CATEGORY_DOCS;
        } else if (CommonIdentity.achiveMimeTypeList.contains(mMime)) {
            return CommonIdentity.CATEGORY_ARCHIVES;
        }
        return CommonIdentity.UNKNOWN_TASK;
    }

    public static boolean isDownLoadFile(String path,MountManager mMountManager) {
        if (path != null && mMountManager != null) {
            if (path.equals(mMountManager.getPhonePath()+File.separator+"Download")) {
                return true;
            } else if (path.equals(mMountManager.getSDCardPath()+File.separator+"Download")) {
                return true;
            } else if (path.equals(mMountManager.getUsbOtgPath()+File.separator+"Download")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBluetoothFile(String path,MountManager mMountManager) {
        if (path != null && mMountManager != null) {
            if (path.equals(mMountManager.getPhonePath()+File.separator+"bluetooth")) {
                return true;
            } else if (path.equals(mMountManager.getSDCardPath()+File.separator+"bluetooth")) {
                return true;
            } else if (path.equals(mMountManager.getUsbOtgPath()+File.separator+"bluetooth")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInPrivacyMode(Context mContext) {
        return new PrivateModeManager(mContext).getInstance().isInPrivacyMode();
    }

    public static boolean isSupportPrivacyMode(Context mContext) {
        return new PrivateModeManager(mContext).getInstance().isInPrivacyMode() && isSupportPrivateModePlf(mContext);
    }

    public static boolean isPrivateLocation(FileManagerApplication mApplication) {
        return mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION ||
                mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION;
    }

    public static boolean isFilePathLocation(FileManagerApplication mApplication) {
        return mApplication.mCurrentLocation == CommonIdentity.FILE_MANAGER_LOCATIONE;
    }

    public static boolean isAddPrivateFileMode(FileManagerApplication mApplication) {
        return (mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION
                || mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION)
                && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE;
    }

    public static boolean isExternalStorage(String path,MountManager mMountManager,boolean isBuiltInStorage) {
        if (path != null) {
            String mSdCarePath = mMountManager.getSDCardPath();
            String mOtgCarePath = mMountManager.getUsbOtgPath();
            if (mSdCarePath != null && path.startsWith(mSdCarePath) && !isBuiltInStorage) {
                return true;
            } else if (mOtgCarePath !=null && path.startsWith(mOtgCarePath)) {
                return true;
            }
        }
        return false;
    }

    /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-07-04,BUG-4974642*/
    public static boolean isExternalStorage(String path,MountManager mMountManager) {
        if (path != null) {
            String mSdCarePath = mMountManager.getSDCardPath();
            String mOtgCarePath = mMountManager.getUsbOtgPath();
            if (mSdCarePath != null && path.startsWith(mSdCarePath)) {
                return true;
            } else if (mOtgCarePath !=null && path.startsWith(mOtgCarePath)) {
                return true;
            }
        }
        return false;
    }
    /* MODIFIED-END by Chuanzhi.Shao,BUG-4974642*/

    /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-06-30,BUG-4953713*/
    public static boolean isOTGStorage(String path,MountManager mMountManager){
        if (path != null) {
            String mOtgCarePath = mMountManager.getUsbOtgPath();
            if (mOtgCarePath != null && path.startsWith(mOtgCarePath)) {
                return true;
            }
        }
        return false;
    }
    /* MODIFIED-END by Chuanzhi.Shao,BUG-4953713*/

    public static boolean isSupportPrivateModePlf(Context mContext){
        return PlfUtils.getBoolean(mContext, "def_privatemode_included");
    }

    public static boolean isMediaURI(Context mContext){
        return PlfUtils.getBoolean(mContext, "def_openfile_default_uri_type");
    }

    public static boolean isShareMediaURI(Context mContext){
        return PlfUtils.getBoolean(mContext, "def_sharefile_default_uri_type");
    }


    public static int getNotificationIconId(int progressType){
        int iconId;
        if(progressType == CommonIdentity.PASTE_COPY_TASK){
            iconId = R.drawable.ic_action_copy_black;
            return iconId;
        }else if (progressType == CommonIdentity.PASTE_CUT_TASK){
            iconId = R.drawable.ic_action_cut_black;
            return iconId;
        }else if (progressType == CommonIdentity.NORMAL_DELETE_TASK){
            iconId = R.drawable.ic_action_delete_black;
            return iconId;
        }else if (progressType == CommonIdentity.FILE_COMPRESSION_TASK){
            iconId = R.drawable.ic_action_archive_black;
            return iconId;
        }else if (progressType == CommonIdentity.FILE_UNCOMPRESSION_TASK){
            iconId = R.drawable.ic_action_extract_black;
            return iconId;
        }else if (progressType == CommonIdentity.ADD_PRIVATE_FILE_TASK){
            iconId = R.drawable.ic_action_private_black;
            return iconId;
        }else if (progressType == CommonIdentity.REMOVE_PRIVATE_FILE_TASK){
            iconId = R.drawable.ic_public;
            return iconId;
        }
        return R.drawable.ic_launcher_shortcut;
    }

    public static String getNotificationTitle(Context context,int progressType){
        String actionTitle = "";
        if(progressType == CommonIdentity.PASTE_COPY_TASK){
            actionTitle = context.getResources().getString(R.string.copy);
        }else if (progressType == CommonIdentity.PASTE_CUT_TASK){
            actionTitle = context.getResources().getString(R.string.cut);
        }else if (progressType == CommonIdentity.NORMAL_DELETE_TASK){
            actionTitle = context.getResources().getString(R.string.delete);
        }else if (progressType == CommonIdentity.FILE_COMPRESSION_TASK){
            actionTitle = context.getResources().getString(R.string.compressing);
        }else if (progressType == CommonIdentity.FILE_UNCOMPRESSION_TASK){
            actionTitle = context.getResources().getString(R.string.extracting);
        } else if (progressType == CommonIdentity.ADD_PRIVATE_FILE_TASK){
            actionTitle = context.getResources().getString(R.string.move_safe);
        } else if (progressType == CommonIdentity.REMOVE_PRIVATE_FILE_TASK){
            actionTitle = context.getResources().getString(R.string.set_public);
        }
        return actionTitle;
    }

    public static boolean isDialogProgressTask(int taskType){
        return taskType == CommonIdentity.PASTE_COPY_TASK
                || taskType == CommonIdentity.PASTE_CUT_TASK
                || taskType == CommonIdentity.NORMAL_DELETE_TASK
                || taskType == CommonIdentity.FILE_COMPRESSION_TASK
                || taskType == CommonIdentity.FILE_UNCOMPRESSION_TASK
                || taskType == CommonIdentity.ADD_PRIVATE_FILE_TASK
                || taskType == CommonIdentity.REMOVE_PRIVATE_FILE_TASK;
    }

    public static void recordStatusEventForFA(Context context, String title, String status, FileManagerApplication appl) {
        recordStatusEventForFA(context, title, "status", status, appl);
    }

    private static void recordStatusEventForFA(Context context, String title, String type, String status, FileManagerApplication appl) {
        if (appl != null) {
            if (appl != null) {
                Bundle bundle = new Bundle();
                bundle.putString(FAExt.Param.ITEM_ID, title);
                bundle.putString(FAExt.Param.ITEM_NAME, status);
                bundle.putString(FAExt.Param.CONTENT_TYPE, type);
                appl.mFAExt.logEvent(title, bundle);
            }
        }
    }

    public static void recordCountNumForFA(Context mContext, String title, FileManagerApplication mApplication) {
        if (mApplication != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FAExt.Param.CONTENT_TYPE, "count");
            mApplication.mFAExt.logEvent(title, bundle);
        }
    }

    public static void hawkeyeTimeEvent(Context mContext, String eventType, long timeStatus, FileManagerApplication appl) {
        if (timeStatus > 0) {
            //hawkeyeStatusUpdateEvent(mContext, eventType, String.valueOf(timeStatus));
            recordStatusEventForFA(mContext, eventType, "eventTime", String.valueOf(timeStatus), appl);
        }
    }

    public static void fieldDialog(AlertDialog dialog){
        try {
            Field field = dialog.getClass().getDeclaredField("mAlert");
            field.setAccessible(true);
            Object obj = field.get(dialog);
            field = obj.getClass().getDeclaredField("mHandler");
            field.setAccessible(true);
            field.set(obj, new ButtonHandler(dialog));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDiagnosticExist(Context context) {
        boolean isDiagnostic = false;
        if (context != null) {
            isDiagnostic = Settings.Global.getInt(context.getContentResolver(),
                    "def.diagnostic.on", -1) != -1 ? true : false;
        }
        return isDiagnostic;
    }

    public static boolean isDiagnosticOn(Context context) {
        boolean isDiagnostic = false;
        if (context != null) {
            isDiagnostic = Settings.Global.getInt(context.getContentResolver(),
                    "def.diagnostic.on", -1) == 1 ? true : false;
        }
        return isDiagnostic;
    }

    public static boolean matcherFolderName(String mFolderName){
        if(mFolderName != null){
            String regx = ".*[/\\\\:*?\"<>|].*";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(mFolderName.toString());
            return m.find();
        }
        return false;
    }
    public static long getAvailMemory(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            return mi.availMem;
        } catch (Exception e){
            e.printStackTrace();
            return 0l;
        }
    }

    public static void commitFragment(FragmentTransaction fragmentTransaction, Fragment mCommitFragment) {
        if (mCommitFragment.isAdded()) {
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            fragmentTransaction.replace(R.id.layout_content, mCommitFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }


}
