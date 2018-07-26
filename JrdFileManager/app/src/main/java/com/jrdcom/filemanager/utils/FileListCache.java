package com.jrdcom.filemanager.utils;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.singleton.ResultTaskHandler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used for files cache.
 */
public class FileListCache {

    public static final String TAG = FileListCache.class.getSimpleName();
    private static final int MAX_COUNT = 50;
    private LruCache<String, List<FileInfo>> lruCache;

    public FileListCache() {
        lruCache = new LruCache<>(MAX_COUNT);
    }

    public List<FileInfo> mAllFileList = new ArrayList<FileInfo>();

    public List<FileInfo> getAllFileList() {
        return mAllFileList;
    }

    // Hide and display files in the directory and classification.
    public void setAllFileList(List<FileInfo> allFileList) {
        if (mAllFileList != null) {
            mAllFileList.clear();
        }
        this.mAllFileList.addAll(allFileList);
    }

    /**
     * Insert elements to cache.
     *
     * @param path The local path for saving list.
     * @param list The cached list.
     */
    public void put(String path, List<FileInfo> list) {
        lruCache.put(path, list);
    }

    /**
     * Get the cached list.
     *
     * @param key The key.
     */
    public List<FileInfo> get(String key) {
        if (key == null) {
            return null;
        }
        return lruCache.get(key);
    }

    public int getCacheSize(){
        return lruCache.size();
    }
    /**
     * remove cache.
     */
    public void removeCache(String key) {
        if(hasCachedPath(key)) {
            lruCache.remove(key);
        }
    }

    /**
     * Clear cache.
     */
    public void clearCache() {
        lruCache.evictAll();
    }

    /**
     * To check if path has been cached.
     *
     * @param curPath the path to be checked.
     * @return true if has cached path, otherwise false.
     */
    public boolean hasCachedPath(String curPath) {
        if (TextUtils.isEmpty(curPath)) {
            return false;
        }
        List<FileInfo> cachedList = lruCache.get(curPath);
        return cachedList != null && cachedList.size() > 0;
    }

    /**
     * To check if key has been cached.
     *
     * @param curPath the key to be checked.
     * @return true if has cached key, otherwise false.
     */
    public boolean hasCachedKey(String curPath) {
        if (TextUtils.isEmpty(curPath)) {
            return false;
        }
        List<FileInfo> cachedList = lruCache.get(curPath);
        return cachedList != null;
    }

    /**
     * If files has been deleted, update caches info.
     *
     * @param parentPath The path to be updated.
     * @param mFileInfo  The cached files which to be deleted.
     * @return true if success, otherwise may be not in the cache.
     */
    public boolean deleteCacheFiles(String parentPath, FileInfo mFileInfo) {

        List<FileInfo> cachedList = lruCache.get(parentPath);
        if (cachedList == null || cachedList.size() == 0 || mFileInfo == null) {
            return false;
        }
        if (cachedList.remove(mFileInfo)) {
            //Log.e(TAG, "FileListCache->deleteCacheFiles: " + mFileInfo.getFileName() + " has been removed successfully ");
            return true;
        } else {
            //Log.e(TAG, "FileListCache->deleteCacheFiles: " + "Failed to remove ");
        }
        return false;
    }

    /* If files has been deleted, update caches info.
    *
            * @param parentPath The path to be updated.
            * @param mFileInfo  The cached files which to be deleted.
            * @return true if success, otherwise may be not in the cache.
    */
    public boolean deleteCacheFiles(int category, String parentPath, FileInfo mFileInfo) {
        String key = String.valueOf(category);
        List<FileInfo> cachedList = lruCache.get(key);
        if (cachedList == null || cachedList.size() == 0) {
            //Log.e(TAG, "FileListCache->deleteCacheFiles: " + " No cache info");
            return false;
        }
        if (cachedList.remove(mFileInfo)) {
            //Log.e(TAG, "FileListCache->deleteCacheFiles: " + mFileInfo.getFileName() + " has been removed successfully ");
            return true;
        } else {
            //Log.e(TAG, "FileListCache->deleteCacheFiles: " + "Failed to remove " + mFileInfo.getFileName());
        }
        return false;
    }

    /**
     * If new files has been created(such as create, copy), update caches info.
     *
     * @param parentPath The path to be updated.
     * @param mFileInfo  The new files which to be added.
     * @return true if success, otherwise may be not in the cache.
     */
    public boolean addCacheFiles(String parentPath, FileInfo mFileInfo) {
        List<FileInfo> cachedList = lruCache.get(parentPath);
        if (cachedList == null) {
            if (!TextUtils.isEmpty(parentPath) && mFileInfo != null) {
                cachedList = new ArrayList<FileInfo>();
                lruCache.put(parentPath, cachedList);
                cachedList.add(mFileInfo);
                return true;
            }
            return false;
        }
        // can delete this code for more effective.
        if (cachedList.contains(mFileInfo)) {
            return false;
        }
        cachedList.add(mFileInfo);
        return true;
    }

    /**
     * If new files has been created(such as create, copy), update caches info.
     *
     * @param parentPath The path to be updated.
     * @param mFileInfo  The new files which to be added.
     * @return true if success, otherwise may be not in the cache.
     */
    public boolean addCacheFiles(String parentPath, FileInfo mFileInfo,boolean isObserver) {
        List<FileInfo> cachedList = lruCache.get(parentPath);
        if (cachedList == null) {
            if (!TextUtils.isEmpty(parentPath) && mFileInfo != null) {
                cachedList = new ArrayList<FileInfo>();
                lruCache.put(parentPath, cachedList);
                cachedList.add(mFileInfo);
                return true;
            }
            return false;
        }
        // can delete this code for more effective.
        if (cachedList.contains(mFileInfo)) {
            deleteCacheFiles(parentPath,mFileInfo);
            addCacheFiles(parentPath,mFileInfo);
            return false;
        }
        cachedList.add(mFileInfo);
        return true;
    }

    /**
     * If new files has been created(such as create, copy), delete caches info.
     *
     * @param file     The file to be add.
     * @param mHashMap The new map which to be added.
     * @return true if success, otherwise may be not in the cache.
     */
    public boolean deleteCacheFiles(File file, HashMap<File, FileInfo> mHashMap) {
        String mSouPath = file.getParent();
        if (hasCachedPath(mSouPath)) {
            return deleteCacheFiles(mSouPath, mHashMap.get(file));
        }
        return false;

    }


    /**
     * If new files has been created(such as create, copy), delete caches info.
     *
     * @param file     The file to be add.
     * @param mHashMap The new map which to be added.
     * @param category is category.
     * @return true if success, otherwise may be not in the cache.
     */
    public boolean deleteCacheFiles(int category, File file, HashMap<File, FileInfo> mHashMap) {
        String key = String.valueOf(category);
        if (category >= 0 && category < 10 && hasCachedPath(key)) {
            deleteCacheFiles(key, mHashMap.get(file));
        }
        return false;
    }

    public void observerDataChanage(FileManagerApplication mApplication, ResultTaskHandler mResultHandler, MountManager mMountManager){
        Set<String> mKeySet = lruCache.snapshot().keySet();

        if(mKeySet != null && CommonUtils.isNormalStatus(mApplication)){
            Iterator<String> it = mKeySet.iterator();
            String oldStr;
            while (it.hasNext()) {
                String str = it.next();
                File mKeyFile = new File(str);
                if (!mKeyFile.exists()&&str.length() > 2) {
                    while (!mMountManager.isPhoneRootPath(str)&&!mKeyFile.exists()) {
                        removeCache(str);
                        oldStr=str;
                        str = mKeyFile.getParent();
                        mKeyFile = new File(str);
                        if (mKeyFile.exists()) {
                            if(mApplication.mCurrentPath != null && mApplication.mCurrentPath.startsWith(str)
                                    && !new File(mApplication.mCurrentPath).exists()) {
                                mApplication.mCurrentPath = str;
                                deleteCacheFiles(str,new FileInfo(mApplication, new File(oldStr)));
                                refreshAdapter(mApplication, mResultHandler, CommonIdentity.REFRESH_FILE_PATH_MODE, true);
                            }
                        }
                    }
                    if(!str.equals(mApplication.mCurrentPath)&&!mKeyFile.exists()){
                        removeCache(str);
                    }
                } else {
                    List<FileInfo> mListInfo = get(str);
                    if (mListInfo == null) {
                        continue;
                    }

                    boolean isExist = true;
                    for (int i = 0; i < mListInfo.size(); i++) {
                        final File mFile = mListInfo.get(i).getFile();
                        if (!mFile.exists()) {
                            if(str.length() > 2) {
                                File parentFile = mFile.getParentFile();
                                if (parentFile != null) {
                                    int count = parentFile.listFiles(new FileFilter() {
                                        @Override
                                        public boolean accept(File file) {
                                            return file.equals(mFile);
                                        }
                                    }).length;
                                    if (count == 1) continue;
                                }
                            } else {
                                int category = Integer.parseInt(str);
                                boolean isFileExist = CommonUtils.isDBFileExist(mApplication,mFile,category);
                                if(isFileExist)continue;
                            }
                            isExist = false;
                            CommonUtils.deleteCache(mListInfo.get(i), FileUtils.getFileCategory(mFile.getName()), mApplication.mCache);
                        }
                    }
                    if (!isExist) {
//                        String mOtgRootPath = mMountManager.getUsbOtgPath();
//                        /*
//                          OTG storage will repeat refresh, the inside of the file exists, but as does not exist, OTG storage filtering.
//                         */
//                        if(mOtgRootPath != null && str.startsWith(mOtgRootPath))continue;
                        removeCache(str);
                        isExist = true;
                        if (mApplication == null) continue;
                        if (mApplication.mCurrentPath != null && mApplication.mCurrentPath.equals(str)) {
                            refreshAdapter(mApplication, mResultHandler, CommonIdentity.REFRESH_FILE_PATH_MODE, false);

                        } else if (CommonUtils.isCategoryMode() && str.equals(String.valueOf(CategoryManager.mCurrentCagegory))) {
                            refreshAdapter(mApplication, mResultHandler, CommonIdentity.REFRESH_FILE_CATEGORY_MODE, false);
                        }
                    }

                }
            }
        }
    }

    public void refreshAdapter(FileManagerApplication mApplication, ResultTaskHandler mResultHandler,int refreshmode,boolean isRefresh){
        if(CommonUtils.isNormalStatus(mApplication) || (isRefresh && CommonUtils.isEditStatus(mApplication))) {
            TaskInfo mOberserverInfo = new TaskInfo(mApplication, null, CommonIdentity.OBSERVER_UPDATE_TASK);
            mOberserverInfo.setAdapterMode(refreshmode);
            Message msg = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mOberserverInfo);
            msg.setData(mBundle);
            if (mResultHandler == null) {
                mResultHandler = mApplication.getAppHandler();
            }
            mResultHandler.handleMessage(msg);
        }
    }

}
