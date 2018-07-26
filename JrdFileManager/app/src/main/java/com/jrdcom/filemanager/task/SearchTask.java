package com.jrdcom.filemanager.task;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.jrdcom.filemanager.listener.OperationEventListener;

import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.PrivateHelper;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class SearchTask extends BaseAsyncTask {
    private static final String TAG = SearchTask.class.getSimpleName();
    private String mSearchName;
    private String mPath;
    private ContentResolver mContentResolver;
    private List<FileInfo> mSearchResult;
    private List<FileInfo> mFilesInfoList;
    private List<String> pathList;
    public static Map<String, List<FileInfo>> searchResultCacheMap = new WeakHashMap<String, List<FileInfo>>(CommonIdentity.MAX_CACHE_SIZE);  //cache MAX_CACHE_SIZE search result
    private static Map<String, Long> searchResultExpireMap = new HashMap<String, Long>(CommonIdentity.MAX_CACHE_SIZE);     //search result expire time


    private int mCategory;
    private int mSearchType;
    private TaskInfo mSearchTask;
    private boolean isNoExpireLimited = false;
    private boolean isSupportPrivateMode = false;


    public SearchTask(TaskInfo mTaskInfo) {
        super(mTaskInfo);
        mSearchTask = mTaskInfo;
        mContext = mTaskInfo.getApplication();
        mContentResolver = mContext.getContentResolver();
        mFilesInfoList = mTaskInfo.getSourceFileList();
        mSearchName = mTaskInfo.getSearchContent();
        mCategory = mTaskInfo.getCategoryIndex();
        pathList = mTaskInfo.getDesPathList();
        mSearchType = mTaskInfo.getFileFilter();
        mPath = mTaskInfo.getDestPath();
        mSearchResult = new ArrayList<FileInfo>(1000);
        if (mSearchType == CommonIdentity.FILE_STATUS_CATEGORY_SEARCH) {
            if (isExpire()) {
                searchResultCacheMap.clear();
                searchResultExpireMap.clear();
            }
        }
    }


    @Override
    protected TaskInfo doInBackground(Void... object) {
        mSearchTask.setTask(getTask());
        mSearchTask.setBaseTaskHashcode(getTask().hashCode());
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        if (mSearchType == CommonIdentity.FILE_STATUS_CATEGORY_SEARCH) {
            isNoExpireLimited = mSearchTask.isShowDir();
            ret = categorySearch();
        } else if (mSearchType == CommonIdentity.FILE_STATUS_SEARCH) {
            if (!CommonUtils.isExternalStorage(mPath,mMountManager,mApplication.isBuiltInStorage) && CommonUtils.isInPrivacyMode(mApplication)) {
                isSupportPrivateMode = true;
            }
            ret = search();
        } else if (mSearchType == CommonIdentity.FILE_STATUS_GLOBALSEARCH) {
            if (CommonUtils.isInPrivacyMode(mApplication)) {
                isSupportPrivateMode = true;
            }
            ret = globalSearch();
        }

        CommonUtils.returnTaskResult(mSearchTask, ret);
        return mSearchTask;

    }

    public int search() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        Uri uri = MediaStore.Files.getContentUri("external");
        boolean isSupportDrm = CommonUtils.isDRMColumn(mContext);
        String[] projection = null;
        if(isSupportDrm && isSupportPrivateMode) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE ,"is_drm",CommonIdentity.TCT_IS_PRIVATE};
        } else if (mApplication.isSysteSupportDrm) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE ,"is_drm"};
        }  else if (isSupportPrivateMode) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE,CommonIdentity.TCT_IS_PRIVATE};
        } else {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE};
        }

        StringBuilder sb = new StringBuilder();

        StringBuilder searchText = new StringBuilder();
        int len = mSearchName.length();
        for (int i = 0; i < len; i++) {
            char c = mSearchName.charAt(i);
            if ((c == '_') || (c == '%')) {
                searchText.append('/');
                searchText.append(c);
            } else {
                searchText.append(c);
            }
        }

        String path = mPath;
        String separator = File.separator;

        if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
            path = mPath.replace("/", "//");
            separator = "//";
        }

        String data = path + separator + "%" + searchText.toString() + "%";

        sb.append(MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, data);

        if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
            sb.append(" escape '/'");
        }
        if (!mApplication.isShowHidden) {
            sb.append(" and ").append(MediaStore.Files.FileColumns.DATA + " not like ");
            String hideString = mPath + "%" + File.separator + ".%";
            DatabaseUtils.appendEscapedSQLString(sb, hideString);
        }

        String selection = sb.toString();

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(uri, projection, selection, null, null);
        } catch (Exception e) {
        }

        if (cursor == null) {
            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }

        int total = cursor.getCount();
        publishProgress(new ProgressInfo("", 0, total));
        //int progress = 0;
        try {
            int dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int mimeTypeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);

            List<FileInfo> searchList = new ArrayList<FileInfo>();

            while (cursor.moveToNext()) {
                if (mCancelled) {
                    cancel(true);
                    onCancelled();
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (isCancelled()) {
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                String filePath = cursor.getString(dataIdx);
                String mMimeType = cursor.getString(mimeTypeIdx);
                int drm =0;
                if(isSupportDrm) {
                    drm = cursor.getInt(cursor.getColumnIndex("is_drm"));
                }
                int isPrivate = 0;
                if(isSupportPrivateMode){
                    isPrivate = cursor.getInt(cursor.getColumnIndex(CommonIdentity.TCT_IS_PRIVATE));
                }
                FileInfo info = new FileInfo(mContext, filePath);
                String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

                if (fileName.toLowerCase().contains(mSearchName.toLowerCase())) {
                    info.setFileMime(mMimeType);
                    info.updateSizeAndLastModifiedTime(info.getFile());
                    if(drm == 1){
                        info.setDrm(true);
                    }
                    if(isPrivate == 1){
                       info.setPrivateFile(true);
                    }
                    info.setHideFile(info.getFile().isHidden());
                    searchList.add(info);
                }
//                if(progress <total){
//                    progress++;
//                }
//                publishProgress(new ProgressInfo(info, progress, total));
            }

            mSearchResult.addAll(searchList);
            mFileInfoManager.addToSearchResultList(mSearchResult);
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }

    public int globalSearch() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        boolean isSupportDrm = CommonUtils.isDRMColumn(mContext);
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = null;
        if(CommonUtils.isDRMColumn(mContext) && isSupportPrivateMode) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE ,"is_drm",CommonIdentity.TCT_IS_PRIVATE};
        } else if (mApplication.isSysteSupportDrm) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE ,"is_drm"};
        }  else if (isSupportPrivateMode) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE ,CommonIdentity.TCT_IS_PRIVATE};
        }  else {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE};
        }
        StringBuilder searchText = new StringBuilder();
        int len = mSearchName.length();
        for (int i = 0; i < len; i++) {
            char c = mSearchName.charAt(i);
            if ((c == '_') || (c == '%')) {
                searchText.append('/');
                searchText.append(c);
            } else {
                searchText.append(c);
            }
        }
        for (int i = 0; i < pathList.size(); i++) {
            StringBuilder sb = new StringBuilder();
            String path = pathList.get(i);
            String separator = File.separator;
            if (path != null && !(searchText.toString().trim()).equals(mSearchName.trim())) {
                path = path.replace("/", "//");
                separator = "//";
            }
            String data = path + separator + "%" + searchText.toString() + "%";
            sb.append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, data);
            if (!(searchText.toString().trim()).equals(mSearchName.trim())) {
                sb.append(" escape '/'");
            }
            if (!mApplication.isShowHidden) {
                sb.append(" and ").append(MediaStore.Files.FileColumns.DATA + " not like ");
                String hideString = path + "%" + File.separator + ".%";
                DatabaseUtils.appendEscapedSQLString(sb, hideString);
            }

            String selection = sb.toString();
            Cursor cursor = mContentResolver.query(uri, projection, selection, null, null);
            if (cursor == null) {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            //int total = cursor.getCount();
            //publishProgress(new ProgressInfo("", 0, total));
            //int progress = 0;
            try {
                int dataIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int mimeTypeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                List<FileInfo> searchList = new ArrayList<FileInfo>();
                while (cursor.moveToNext()) {
                    if (mCancelled) {
                        cancel(true);
                        onCancelled();
                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (isCancelled()) {
                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    String filePath = cursor.getString(dataIdx);
                    String mMimeType = cursor.getString(mimeTypeIdx);
                    int drm = 0;
                    if(isSupportDrm) {
                        drm = cursor.getInt(cursor.getColumnIndex("is_drm"));
                    }
                    int isPrivate = 0;
                    if(isSupportPrivateMode){
                        isPrivate = cursor.getInt(cursor.getColumnIndex(CommonIdentity.TCT_IS_PRIVATE));
                    }

                    FileInfo info = new FileInfo(mContext, filePath);
                    String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

                    if (fileName.toLowerCase().contains(mSearchName.toLowerCase())) {
                        info.setFileMime(mMimeType);
                        info.updateSizeAndLastModifiedTime(info.getFile());

                        if(drm == 1){
                            info.setDrm(true);
                        }

                        if(isPrivate ==1){
                            info.setPrivateFile(true);
                        }
                        info.setHideFile(info.getFile().isHidden());

                        searchList.add(info);
                    }
//                    if(progress <total){
//                        progress++;
//                    }
                    //publishProgress(new ProgressInfo(info, progress, total));
                }
                mSearchResult.addAll(searchList);
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        mFileInfoManager.addToSearchResultList(mSearchResult);
        return ret;
    }


    public int categorySearch() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        int total = mFilesInfoList.size();
//        int progress = 0;
        Log.d(TAG,"Category search Source list size ==>"+total);
        String tag = mCategory + mSearchName;
        List<FileInfo> cacheList = searchResultCacheMap.get(tag);
        if (!isNoExpireLimited && cacheList != null
                && (System.currentTimeMillis() - searchResultExpireMap.get(tag)) < CommonIdentity.EXPIRE_TIME && SafeManager.mCurrentSafeCategory != 12) {
            for (FileInfo file : cacheList) {
                if (file.getFile().exists()) {
                    String mMime = file.getMime();
                    if(mMime != null) {
                        file.setFileMime(mMime);
                    } else {
                        file.setFileMime(file.getMimeType());
                    }
                    file.updateSizeAndLastModifiedTime(file.getFile());
                    file.setDrm(file.isDrmFile());
                    file.setHideFile(file.getFile().isHidden());
                    file.setPrivateFile(file.isPrivateFile());
                    mSearchResult.add(file);
                }
            }
            mFileInfoManager.addToSearchResultList(mSearchResult);
        } else {
            int len = mFilesInfoList.size();

            for (int i = 0; i < len; i++) {
                if (mCancelled) {
                    cancel(true);
                    onCancelled();
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (mApplication.mCurrentStatus != CommonIdentity.FILE_STATUS_SEARCH) {
                    return OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                FileInfo info = mFilesInfoList.get(i);
                String name = info.getFileName();
                String path = info.getFileAbsolutePath();
                String mMime = info.getMime();
                if (name.toLowerCase().contains(mSearchName.toLowerCase())) {
                    FileInfo info1 = new FileInfo(mContext, path);

                    if(mMime != null) {
                        info1.setFileMime(mMime);
                    } else {
                        info1.setFileMime(info1.getMimeType());
                    }
                    info1.setPrivateFile(info.isPrivateFile());
                    info1.setHideFile(info1.getFile().isHidden());
                    info1.updateSizeAndLastModifiedTime(info.getFile());
                    mSearchResult.add(info1);
//                    if(progress <total){
//                        progress++;
//                    }
//                    publishProgress(new ProgressInfo(info, progress, total));
                }
            }

            if (mSearchResult != null && mSearchResult.size() > 0) {
                mFileInfoManager.addToSearchResultList(mSearchResult);
                if (searchResultCacheMap.entrySet().size() < CommonIdentity.MAX_CACHE_SIZE) {
                    searchResultCacheMap.put(tag, mSearchResult);
                    searchResultExpireMap.put(tag, System.currentTimeMillis());
                }

            }
        }

        return ret;
    }


    private boolean isExpire() {
        long currentTime = System.currentTimeMillis();
        long lastTime = 1;
        for (Iterator<String> it = searchResultExpireMap.keySet().iterator(); it.hasNext(); ) {
            String searchKey = it.next();
            if (lastTime < searchResultExpireMap.get(searchKey)) {
                lastTime = searchResultExpireMap.get(searchKey);
            }
        }

        if ((currentTime - lastTime) > CommonIdentity.EXPIRE_TIME) {
            return true;
        }
        return false;
    }


}
