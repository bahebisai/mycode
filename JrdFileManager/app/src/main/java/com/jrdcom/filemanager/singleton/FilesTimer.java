package com.jrdcom.filemanager.singleton;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.Timer;

import tct.util.privacymode.TctPrivacyModeHelper;

/**
 * Created by user on 16-8-13.
 */
public class FilesTimer extends Timer{
    private static final String TAG = FilesTimer.class.getSimpleName();
    private static FilesTimer mFileTimer;
    private static FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    private static ResultTaskHandler mResultHandler = mApplication.getAppHandler();;
    private static long nextQueryStartTime = 0;
    private static Uri uri;
    private static TctPrivacyModeHelper mPrivateModeHelper;
    private static MountManager mMountManager;

    public FilesTimer() {
//        Log.d(TAG, "FilesTimer()");
        mPrivateModeHelper = new PrivateModeManager(mApplication).getInstance();
        uri = MediaStore.Files.getContentUri("external");
        mMountManager = MountManager.getInstance();
    }

    private static FilesTimer getInstance() {
        if(mFileTimer == null){
            mApplication.mAppStartTime = mApplication.mAppStartTime / 1000l;
//            nextQueryStartTime = System.currentTimeMillis()/1000l;
            mFileTimer = new FilesTimer();
        }
        mPrivateModeHelper = new PrivateModeManager(mApplication).getInstance();
        uri = MediaStore.Files.getContentUri("external");
        mMountManager = MountManager.getInstance();
        return mFileTimer;
    }

    public void changeFileInfo() {

                Cursor cursor = null;
                boolean isStoragePermision = PermissionUtil.checkAppPermission(mApplication);
                boolean isSupportPrivateMode = CommonUtils.isInPrivacyMode(mApplication);
                try {
                    String[] projection;
                    if (mApplication.isSysteSupportDrm && isSupportPrivateMode) {
                        projection = new String[]{MediaStore.Files.FileColumns.DATA,
                                MediaStore.Files.FileColumns.MIME_TYPE,
                                MediaStore.Files.FileColumns.SIZE,
                                MediaStore.Files.FileColumns.DATE_MODIFIED, "is_drm",CommonIdentity.TCT_IS_PRIVATE};
                    } else if (mApplication.isSysteSupportDrm) {                        projection = new String[]{MediaStore.Files.FileColumns.DATA,
                                MediaStore.Files.FileColumns.MIME_TYPE,
                                MediaStore.Files.FileColumns.SIZE,
                                MediaStore.Files.FileColumns.DATE_MODIFIED, "is_drm"};
                    } else if (isSupportPrivateMode) {
                        projection = new String[]{MediaStore.Files.FileColumns.DATA,
                                MediaStore.Files.FileColumns.MIME_TYPE,
                                MediaStore.Files.FileColumns.SIZE,
                                MediaStore.Files.FileColumns.DATE_MODIFIED, CommonIdentity.TCT_IS_PRIVATE};
                    } else {
                        projection = new String[]{MediaStore.Files.FileColumns.DATA,
                                MediaStore.Files.FileColumns.MIME_TYPE,
                                MediaStore.Files.FileColumns.SIZE,
                                MediaStore.Files.FileColumns.DATE_MODIFIED};
                    }
                    long mCurrentTime = System.currentTimeMillis()/1000l;
                    if (mApplication.mAppStartTime > mCurrentTime) {
                        mApplication.mAppStartTime = mCurrentTime;
                    }
                    StringBuilder sb = new StringBuilder();

                    sb.append(MediaStore.Files.FileColumns.DATE_MODIFIED + " >= " + mApplication.mAppStartTime);
                    sb.append(" and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " <= " + mCurrentTime);
                    //mApplication.mAppStartTime = nextQueryStartTime;
                    String selection = sb.toString();
                    if(isStoragePermision) {
                        mApplication.mCache.observerDataChanage(mApplication, mResultHandler, mMountManager);
//                        Log.d(TAG,"query sql ==>"+selection);
                        cursor = mApplication.getContentResolver().query(uri, projection, selection,
                                null, null);
                    }
                    if (cursor == null || cursor.getCount() == 0 || !isStoragePermision) {
//                        nextQueryStartTime += 10l;
                        return;
                    }
                    if (cursor != null) {
                        long lastModifyTime = 0l;
                        while (cursor.moveToNext()) {
                            String name = (String) cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
//                            Log.d(TAG,"query result name ==>"+name);
                            String mMimeType = (String) cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                            int isDrm = 0;

                            long size = (long) cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                            lastModifyTime = (long) cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));

                            String parentPath = name.substring(0, name.lastIndexOf("/"));
                            File file = new File(name);
                            FileInfo mFileInfo = new FileInfo(mApplication, false, parentPath, name,file);
                            mFileInfo.setHideFile(file.isHidden());
                            mFileInfo.updateSizeAndLastModifiedTime(size, lastModifyTime * 1000);
                            mFileInfo.setFileMime(mMimeType);
                            boolean isDir = mFileInfo.isDirectory();
                            if (mApplication.isSysteSupportDrm) {
                                isDrm = cursor.getInt(cursor.getColumnIndex("is_drm"));
                                if (isDrm == 1) {
                                    mFileInfo.setDrm(true);
                                }
                            } else{
                                if(!isDir) {
                                    mFileInfo.setDrm((DrmManager.getInstance(mApplication).isDrm(name)) || DrmManager.isDrmFileExt(file.getName()));
                                } else {
                                    mFileInfo.setDrm(false);
                                }
                            }
                            if(isSupportPrivateMode && !isDir &&
                                    !CommonUtils.isExternalStorage(name,mMountManager,mApplication.isBuiltInStorage) &&
                                    PrivateModeManager.isPrivateFile(mPrivateModeHelper,name)){
                                mFileInfo.setPrivateFile(true);
                            }
                            CommonUtils.addCache(mApplication,mFileInfo,mApplication.mCache,mMountManager,true);
                        }
                        if(lastModifyTime >0) {
                            mApplication.mAppStartTime = lastModifyTime+1l;
//                            nextQueryStartTime = mApplication.mAppStartTime;
                        }
                    }


                    TaskInfo mOberserverInfo = new TaskInfo(mApplication, null, CommonIdentity.OBSERVER_UPDATE_TASK);
                    if(CommonUtils.isCategoryMode() && !mApplication.mCache.hasCachedPath(CategoryManager.mCurrentCagegory+"")){
                       mOberserverInfo.setAdapterMode(CommonIdentity.REFRESH_FILE_CATEGORY_MODE);
                    } else if(CommonUtils.isPathMode(mApplication.mCurrentPath) && !mApplication.mCache.hasCachedPath(mApplication.mCurrentPath)){
                        mOberserverInfo.setAdapterMode(CommonIdentity.REFRESH_FILE_PATH_MODE);
                    }
                    Message msg = new Message();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mOberserverInfo);
                    msg.setData(mBundle);
                    if (mResultHandler == null) {
                        mResultHandler = mApplication.getAppHandler();
                    }
                    if(mResultHandler != null) {
                        mResultHandler.handleMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }



}
