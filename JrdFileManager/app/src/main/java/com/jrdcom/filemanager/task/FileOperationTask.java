package com.jrdcom.filemanager.task;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.PlfUtils; // MODIFIED by Chuanzhi.Shao, 2017-06-30,BUG-4953713
import com.jrdcom.filemanager.compress.CommonCompress;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.manager.IconManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.singleton.RunningTaskMap;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class FileOperationTask extends BaseAsyncTask {


    public FileOperationTask(TaskInfo mTaskInfo) {
        super(mTaskInfo);
        if (mTaskInfo.getApplication() == null) {
            throw new IllegalArgumentException();
        }
    }

    protected File getDstFile(HashMap<String, String> pathMap, File file, String defPath) {

        String curPath = pathMap.get(file.getParent());
        if (curPath == null) {
            curPath = defPath;
        }
        File dstFile = new File(curPath, file.getName());

        return checkFileNameAndRename(dstFile);
    }

    protected boolean deleteFile(File file) {
        if (file == null) {
            publishProgress(new ProgressInfo(
                    OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
        } else {

            if (file.canWrite() && file.getName().equals("DCIM")) {
                final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
                if (file.renameTo(to)) {
                    return to.delete();
                } else {
                    publishProgress(new ProgressInfo(
                            OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
                }
            } else if (file.canWrite() && file.delete()) {

                return true;

            } else if (!file.exists()) {
                return true;

            } else {
                publishProgress(new ProgressInfo(
                        OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
            }
        }

        return false;
    }

    protected boolean mkdir(HashMap<String, String> pathMap, File srcFile, File dstFile) {
        if (srcFile.exists() && srcFile.canRead() && dstFile.mkdirs()) {
            pathMap.put(srcFile.getAbsolutePath(), dstFile.getAbsolutePath());
            return true;
        } else {
            publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS,
                    true));
            return false;
        }
    }

    private long calcNeedSpace(List<File> fileList) {
        long need = 0;
        for (File file : fileList) {
            need += file.length();
        }
        return need;
    }

    protected boolean isEnoughSpace(List<File> fileList, String dstFolder) {
        try {
            long needSpace = calcNeedSpace(fileList);
            File file = new File(dstFolder);
            long freeSpace = file.getUsableSpace();//PR 1851024 zibin.wang 2016/04/01

            if (needSpace > freeSpace) {
                return false;
            }
        } catch (Exception e) {
            LogUtils.d("FileOperationTask", "Exception occured when isEnoughSpace():", e);
            return false;
        }
        return true;
    }

    protected int getAllDeleteFiles(List<FileInfo> fileInfoList, List<File> deleteList) {

        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        for (FileInfo fileInfo : fileInfoList) {
            ret = getAllDeleteFile(fileInfo.getFile(), deleteList);
            if (ret < 0) {
                break;
            }
        }
        return ret;
    }

    protected int getAllDeleteFile(File deleteFile, List<File> deleteList) {
        if (isCancelled()) {
            return OperationEventListener.ERROR_CODE_USER_CANCEL;
        }
        if (deleteFile.isDirectory()) {
            deleteList.add(0, deleteFile);
            if (deleteFile.canWrite()) {
                File[] files = deleteFile.listFiles();
                if (files == null) {
                    return OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                for (File file : files) {
                    getAllDeleteFile(file, deleteList);
                }
            }
        } else {
            deleteList.add(0, deleteFile);
        }
        return OperationEventListener.ERROR_CODE_SUCCESS;
    }

    protected int getAllFileList(List<FileInfo> srcList, List<File> resultList,
                                 UpdateInfo updateInfo) {

        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        for (FileInfo fileInfo : srcList) {
            ret = getAllFile(fileInfo.getFile(), resultList, updateInfo);
            if (ret < 0) {
                break;
            }
        }
        return ret;
    }

    protected int getAllFile(File srcFile, List<File> fileList, UpdateInfo updateInfo) {
        if (isCancelled()) {
            return OperationEventListener.ERROR_CODE_USER_CANCEL;
        }
        fileList.add(srcFile);
        updateInfo.updateTotal(srcFile.length());
        if (srcFile.isDirectory() && srcFile.canRead()) {
            File[] files = srcFile.listFiles();
            if (files == null) {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            for (File file : files) {
                int ret = getAllFile(file, fileList, updateInfo);
                if (ret < 0) {
                    return ret;
                }
            }
        }
        return OperationEventListener.ERROR_CODE_SUCCESS;
    }

    protected int copyFile(byte[] buffer, File srcFile, File dstFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        try {
            if (!dstFile.createNewFile()) {
                return OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
            if (!srcFile.exists()) {
                return OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                // Copy data from in stream to out stream
                if (isCancelled()) {
                    if (!dstFile.delete()) {
                        LogUtils.w(this.getClass().getName(), "delete fail in copyFile()");
                    }
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public File checkFileNameAndRename(File conflictFile) {
        File retFile = conflictFile;
        while (true) {
            if (isCancelled()) {
                return null;
            }
            if (!retFile.exists()) {
                return retFile;
            }
            retFile = FileUtils.genrateNextNewName(retFile);
            if (retFile == null) {
                return null;
            }
        }
    }

    protected void updateProgressWithTime(UpdateInfo updateInfo, File file) {
        if (updateInfo.needUpdate()) {
            int progress = (int) (updateInfo.getProgress() * CommonIdentity.TOTAL / updateInfo.getTotal());
            publishProgress(new ProgressInfo(file.getName(), progress, CommonIdentity.TOTAL));
        }
    }

    protected void addItem(HashMap<File, FileInfo> fileInfoMap, File file, File addFile) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(mContext, addFile);
            mFileInfoManager.addItem(fileInfo);
        }
    }

    protected void addItemWithMimeType(HashMap<File, FileInfo> fileInfoMap, File file,
                                       File addFile, FileInfoManager mFileInfoManager) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(mContext, addFile);
            fileInfo.setFileMime(fileInfoMap.get(file).getMime());
            fileInfo.updateSizeAndLastModifiedTime(file);
            mFileInfoManager.addItem(fileInfo);
        }
    }

    protected void removeItem(HashMap<File, FileInfo> fileInfoMap, File file, File removeFile) {
        if (fileInfoMap.containsKey(file)) {
            mFileInfoManager.removeItem(fileInfoMap.get(removeFile));
        }
    }

    public static class DeleteFilesTask extends FileOperationTask {
        private List<FileInfo> mDeletedFilesInfo;
        private int mDeleteMode = 0;
        private Context mContext;
        private TaskInfo mResultTask;
        private int mCategory = -1;

        public DeleteFilesTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;
            mDeletedFilesInfo = mTaskInfo.getSourceFileList();
            mDeleteMode = mTaskInfo.getBaseTaskType();
            mContext = mTaskInfo.getApplication();
            mCategory = mTaskInfo.getCategoryIndex();

        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            int mTaskType = mResultTask.getBaseTaskType();
            long mTaskCreateTime = mResultTask.getCreateTaskTime();
            publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, 0, 0));
            List<File> deletefileList = new ArrayList<File>();
            boolean isSafeBoxSupported = false;

            int ret = getAllDeleteFiles(mDeletedFilesInfo, deletefileList);
            if (ret < 0) {
                CommonUtils.returnTaskResult(mResultTask, ret);
                return mResultTask;
            }
//            HashMap<File, FileInfo> deleteFileInfoMap = new HashMap<File, FileInfo>();
//            for (FileInfo fileInfo : mDeletedFilesInfo) {
//                deleteFileInfoMap.put(fileInfo.getFile(), fileInfo);
//            }
            LogUtils.i(this.getClass().getName(), "delete files : deletefileList.size()" + deletefileList.size());
            int total = deletefileList.size();
            int progress = 0;
            String DstFileName = UUID.randomUUID().toString();
            if (deletefileList != null && deletefileList.size() > 0) {
                publishProgress(CommonUtils.getProgressInfo(deletefileList.get(0).getName(), mTaskType, mTaskCreateTime, progress, total));
            }
            int condition = getConditionCount(total, mTaskType);
            try {
                for (File file : deletefileList) {
                    if (progress % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, progress, total));
                    }
                    String mAbsolutePath = file.getAbsolutePath();
                    if (isCancelled()) {
                        CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_USER_CANCEL);
                        return mResultTask;
                    }
                    if (deleteFile(file)) {
                        deleteMediaStoreHelper.addRecord(mAbsolutePath);
                        CommonUtils.deleteCache(new FileInfo(mContext, file), FileUtils.getFileCategory(file.getName()), mApplication.mCache);
                        IconManager.getInstance().removeCache(mAbsolutePath);
                    } else {
                        ret = OperationEventListener.ERROR_CODE_DELETE_UNSUCCESS;
                    }
                    if (progress < total - 1) {
                        progress++;
                    }
                    //publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, progress, total));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                deleteMediaStoreHelper.updateRecords();
            }

            DrmManager.getInstance(mContext.getApplicationContext()).restoreWallpaper();

            CommonUtils.returnTaskResult(mResultTask, ret);
            return mResultTask;
        }
    }

    static class UpdateInfo {
        protected static final int NEED_UPDATE_TIME = 200;
        private long mStartOperationTime = 0;
        private long mProgressSize = 0;
        private long mTotalSize = 0;

        public UpdateInfo() {
            mStartOperationTime = System.currentTimeMillis();
        }

        public long getProgress() {
            return mProgressSize;
        }

        public long getTotal() {
            return mTotalSize;
        }

        public void updateProgress(long addSize) {
            mProgressSize += addSize;
        }

        public void updateTotal(long addSize) {
            mTotalSize += addSize;
        }

        public boolean needUpdate() {
            long operationTime = System.currentTimeMillis() - mStartOperationTime;
            if (operationTime > NEED_UPDATE_TIME) {
                mStartOperationTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

    }

    public static class CutPasteFilesTask extends FileOperationTask {
        private final List<FileInfo> mSrcList;
        private final String mDstFolder;
        private TaskInfo mResultTask;
        private int mCategory = -1;
        boolean isExteranalStorage = false;
        boolean isOTGStorage = false; // MODIFIED by Chuanzhi.Shao, 2017-09-11,BUG-5189986
        ArrayList<String> mPrivateFileList = new ArrayList<String>();

        public CutPasteFilesTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;
            mSrcList = mTaskInfo.getSourceFileList();
            mDstFolder = mTaskInfo.getDestPath();
            mCategory = mTaskInfo.getCategoryIndex();

        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            if (mSrcList.isEmpty()) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS);
                return mResultTask;
            }
            int ret = cutPasteInDiffCard();
            CommonUtils.returnTaskResult(mResultTask, ret);
            return mResultTask;
        }

        private Integer cutPasteInDiffCard() {
            List<File> fileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            int mTaskType = mResultTask.getBaseTaskType();
            long mTaskCreateTime = mResultTask.getCreateTaskTime();
            publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, 0, 0));
            int ret = getAllFileList(mSrcList, fileList, updateInfo);
            if (ret < 0) {
                return ret;
            }
            if (!isEnoughSpace(fileList, mDstFolder)) {
                return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }

            if (CommonUtils.isExternalStorage(mDstFolder, mMountManager,mApplication.isBuiltInStorage) || !CommonUtils.isInPrivacyMode(mApplication)) {
                isExteranalStorage = true;
            }
            isOTGStorage = CommonUtils.isOTGStorage(mDstFolder, mMountManager); // MODIFIED by Chuanzhi.Shao, 2017-09-11,BUG-5189986
            List<File> removeFolderFiles = new LinkedList<File>();
            List<File> removeFiles = new LinkedList<File>();
            //updateInfo.updateTotal(fileList.size());
            int total = fileList.size();
            int progress = 0;
            publishProgress(CommonUtils.getProgressInfo(fileList.get(0).getName(), mTaskType, mTaskCreateTime, 0, total));
            byte[] buffer = new byte[CommonIdentity.BUFFER_SIZE];
            HashMap<String, String> pathMap = new HashMap<String, String>();
            if (!fileList.isEmpty()) {
                pathMap.put(fileList.get(0).getParent(), mDstFolder);
            }
            // Set dstFolder so we can scan folder instead of scanning each file one by one.
            copyMediaStoreHelper.setDstFolder(mDstFolder);
            HashMap<File, FileInfo> cutFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mSrcList) {
                cutFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }
            int condition = getConditionCount(total, mTaskType);
            try {
                for (File file : fileList) {
                    if (progress % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, progress, total));
                    }
                    File dstFile = getDstFile(pathMap, file, mDstFolder);
                    if (isCancelled()) {
                        ret = OperationEventListener.ERROR_CODE_USER_CANCEL;
                        break;
                    }
                    if (dstFile == null || file == null) {
                        publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                        continue;
                    }

                    if (mMountManager != null) {
                        String dst = mMountManager.getRealMountPointPath(dstFile.getAbsolutePath());
                        String sour = mMountManager.getRealMountPointPath(file.getAbsolutePath());
                        if (!mMountManager.isMounted(dst) || !mMountManager.isMounted(sour)) {
                            copyMediaStoreHelper.updateRecords();
                            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
                            break;
                        }
                    }

                    FileInfo mDstFileInfo = new FileInfo(mContext, dstFile, true);
                    mDstFileInfo.setFileMime(FileUtils.getMimeTypeByExt(dstFile.getAbsolutePath()));
                    if (file.isDirectory()) {
                        if (mkdir(pathMap, file, dstFile)) {
                            if (mCategory >= 0 && mCategory < 10) {
                                mApplication.mCache.deleteCacheFiles(mCategory, file, cutFileInfoMap);
                            }
                            if (dstFile.isHidden()) {
                                mDstFileInfo.setHideFile(true);
                            }
                            CommonUtils.addCache(mApplication, mDstFileInfo, mApplication.mCache, -1,mMountManager, true);
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                            removeFolderFiles.add(0, file);
                        } else {
                            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
                            continue;
                        }
                    } else {
                        ret = copyFile(buffer, file, dstFile);
                        if (ret == OperationEventListener.ERROR_CODE_USER_CANCEL) {
                            break;
                        } else if (ret < 0) {
                            publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                        } else {
                            if (mCategory >= 0 && mCategory < 10) {
                                mApplication.mCache.deleteCacheFiles(mCategory, file, cutFileInfoMap);
                            }
                            if (dstFile.isHidden()) {
                                mDstFileInfo.setHideFile(true);
                            }
                            String mSrcAbsolutePath = file.getAbsolutePath();
                            if (!isExteranalStorage && !CommonUtils.isExternalStorage(mSrcAbsolutePath, mMountManager,mApplication.isBuiltInStorage) &&
                                    PrivateModeManager.isPrivateFile(mPrivateModeHelper, mSrcAbsolutePath)) {
                                mDstFileInfo.setPrivateFile(true);
                                mPrivateFileList.add(dstFile.getAbsolutePath());
                            } else {
                                mDstFileInfo.setPrivateFile(false);
                            }
                            CommonUtils.addCache(mApplication, mDstFileInfo, mApplication.mCache, FileUtils.getFileCategory(dstFile.getName()), mMountManager,true);
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                            if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                                addItemWithMimeType(cutFileInfoMap, file, dstFile, mFileInfoManager);
                            }
                            removeFiles.add(file);
                        }
                    }
                    if (progress < total - 1) {
                        progress++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                for (File file : removeFiles) {
                    if (deleteFile(file)) {
                        CommonUtils.deleteCache(new FileInfo(mContext, file), FileUtils.getFileCategory(file.getName()), mApplication.mCache);
                        deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                    }
                }
                for (File file : removeFolderFiles) {
                    if (file.listFiles() != null && file.listFiles().length <= 0) {
                        if (deleteFile(file)) {
                            mApplication.mCache.removeCache(file.getParent());
                            deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                        }
                    }
                }
                if (mSrcList != null && mSrcList.size() > 0 && mSrcList.get(0) != null) {
                    mApplication.mCache.removeCache(mSrcList.get(0).getFileParentPath());
                }
                /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-09-11,BUG-5189986*/
                //copyMediaStoreHelper.updateRecords();
                if (PlfUtils.getBoolean(mContext, "def_OTG_paste_mediascanner")){
                    copyMediaStoreHelper.updateRecords();
                }else if (!isOTGStorage){
                    copyMediaStoreHelper.updateRecords();
                }
                /* MODIFIED-END by Chuanzhi.Shao,BUG-5189986*/
                deleteMediaStoreHelper.updateRecords();
                if (!isExteranalStorage) {
                    mPrivateModeManager.addPrivateModeFile(mPrivateFileList);
                }
            }
            return ret;
        }
    }

    public static class CopyPasteFilesTask extends FileOperationTask {

        List<FileInfo> mSrcList = null;
        String mDstFolder = null;
        boolean isExteranalStorage = false;
        boolean isOTGStorage = false; // MODIFIED by Chuanzhi.Shao, 2017-06-30,BUG-4953713
        ArrayList<String> mPrivateFileList = new ArrayList<String>();

        /**
         * Buffer size for data read and write.
         */
        public static final int BUFFER_SIZE = 16 * 1024;
        private TaskInfo mResultTask;
        private int mCategory = -1;

        public CopyPasteFilesTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mSrcList = mTaskInfo.getSourceFileList();
            mDstFolder = mTaskInfo.getDestPath();
            mResultTask = mTaskInfo;
            mCategory = mResultTask.getCategoryIndex();

        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            int mTaskType = mResultTask.getBaseTaskType();
            long mTaskCreateTime = mResultTask.getCreateTaskTime();
            publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, 0, 0));
            List<File> fileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            int ret = getAllFileList(mSrcList, fileList, updateInfo);
            if (ret < 0) {
                CommonUtils.returnTaskResult(mResultTask, ret);
                return mResultTask;
            }
            if (CommonUtils.isExternalStorage(mDstFolder, mMountManager,mApplication.isBuiltInStorage) || !CommonUtils.isInPrivacyMode(mApplication)) {
                isExteranalStorage = true;
            }
            isOTGStorage = CommonUtils.isOTGStorage(mDstFolder, mMountManager); // MODIFIED by Chuanzhi.Shao, 2017-06-30,BUG-4953713
            copyMediaStoreHelper.setDstFolder(mDstFolder);
//            HashMap<File, FileInfo> copyFileInfoMap = new HashMap<File, FileInfo>();
//            for (FileInfo fileInfo : mSrcList) {
//                copyFileInfoMap.put(fileInfo.getFile(), fileInfo);
//            }
            int total = fileList.size();
            int progress = 0;
            publishProgress(CommonUtils.getProgressInfo(fileList.get(0).getName(), mTaskType, mTaskCreateTime, 0, total));
            if (!isEnoughSpace(fileList, mDstFolder)) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE);
                return mResultTask;
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            HashMap<String, String> pathMap = new HashMap<String, String>();
            if (!fileList.isEmpty()) {
                pathMap.put(fileList.get(0).getParent(), mDstFolder);
            }
            MountManager mount = MountManager.getInstance();

            int condition = getConditionCount(total, mTaskType);
            try {
                for (File file : fileList) {
                    if (progress % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, progress, total));
                    }
                    File dstFile = getDstFile(pathMap, file, mDstFolder);
                    if (isCancelled()) {
                        CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_USER_CANCEL);
                        return mResultTask;
                    }
                    if (dstFile == null || file == null) {
                        //publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                        continue;
                    }
                    String mAbsolutePath = file.getAbsolutePath();
                    if (mount != null) {
                        String dst = mount.getRealMountPointPath(dstFile.getAbsolutePath());
                        String sour = mount.getRealMountPointPath(mAbsolutePath);
                        if (!mount.isMounted(dst) || !mount.isMounted(sour)) {
                            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
                            CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS);
                            return mResultTask;
                        }
                    }
                    FileInfo mDstFileInfo = new FileInfo(mContext, dstFile, true);
                    mDstFileInfo.setFileMime(FileUtils.getMimeTypeByExt(dstFile.getAbsolutePath()));
                    if (file.isDirectory()) {
                        if (mkdir(pathMap, file, dstFile)) {
                            if (dstFile.isHidden()) {
                                mDstFileInfo.setHideFile(true);
                            }
                            CommonUtils.addCache(mApplication, mDstFileInfo, mApplication.mCache, -1, mMountManager,true);
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                        } else {
                            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
                            continue;
                        }
                    } else {
                        if (DrmManager.isDrmFileExt(file.getName()) || !file.canRead()) {
                            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
                            //publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION, true));
                            continue;
                        }
                        ret = copyFile(buffer, file, dstFile);
                        if (ret == OperationEventListener.ERROR_CODE_USER_CANCEL) {
                            CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_USER_CANCEL);
                            return mResultTask;
                        } else if (ret < 0) {
                            publishProgress(new ProgressInfo(ret, true));
                        } else {
                            if (dstFile.isHidden()) {
                                mDstFileInfo.setHideFile(true);
                            }
                            if (!isExteranalStorage && !dstFile.isDirectory() &&
                                    !CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage) &&
                                    PrivateModeManager.isPrivateFile(mPrivateModeHelper, mAbsolutePath)) {
                                mDstFileInfo.setPrivateFile(true);
                                mPrivateFileList.add(dstFile.getAbsolutePath());
                            } else {
                                mDstFileInfo.setPrivateFile(false);
                            }
                            CommonUtils.addCache(mApplication, mDstFileInfo, mApplication.mCache, FileUtils.getFileCategory(dstFile.getName()),mMountManager, true);
                            copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                        }
                    }

                    if (progress < total - 1) {
                        progress++;
                    }
                    //publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, progress, total));
                }
            } finally {
                /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-06-30,BUG-4953713*/
                if (PlfUtils.getBoolean(mContext, "def_OTG_paste_mediascanner")){
                    copyMediaStoreHelper.updateRecords();
                }else if (!isOTGStorage){
                    copyMediaStoreHelper.updateRecords();
                }
                /* MODIFIED-END by Chuanzhi.Shao,BUG-4953713*/
                if (!isExteranalStorage) {
                    mPrivateModeManager.addPrivateModeFile(mPrivateFileList);
                }
            }
            CommonUtils.returnTaskResult(mResultTask, ret);
            return mResultTask;
        }
    }

    public int getConditionCount(int total, int taskType) {
        int condition = 1;
        if (total > 1 && total <= 10) {
            condition = 1;
        } else if (total > 10 && total <= 100) {
            condition = 2;
        } else if (total > 100 && total <= 400) {
            condition = 4;
        } else if (total > 400 && total <= 1000) {
            condition = 8;
        } else if (total > 1000) {
            if (taskType == CommonIdentity.NORMAL_DELETE_TASK
                    || taskType == CommonIdentity.FILE_COMPRESSION_TASK
                    || taskType == CommonIdentity.FILE_UNCOMPRESSION_TASK
                    || taskType == CommonIdentity.REMOVE_PRIVATE_FILE_TASK) {
                condition = 50;
            } else {
                condition = 10;
            }
        }
        return condition;
    }

    public static class CreateFolderTask extends FileOperationTask {
        public static final String TAG = "CreateFolderTask";
        private final String mDstFolder;
        int mFilterType;
        TaskInfo mResultTask;

        public CreateFolderTask(TaskInfo mCreateFolderTask) {
            super(mCreateFolderTask);
            mResultTask = mCreateFolderTask;
            mDstFolder = mCreateFolderTask.getDestPath();
            mFilterType = mCreateFolderTask.getFileFilter();
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;
            ret = FileUtils.checkFileName(FileUtils.getFileName(mDstFolder));
            if (ret < 0) {
                CommonUtils.returnTaskResult(mResultTask, ret);
                return mResultTask;
            }

            File dir = new File(mDstFolder.trim());
            if (dir.exists()) {
                mResultTask.setTitleStr(dir.getName());
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_FILE_EXIST);
                return mResultTask;
            }
            File path = new File(FileUtils.getFilePath(mDstFolder));
            if (path.getFreeSpace() <= 0) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE);
                return mResultTask;
            }
            if (dir.mkdirs()) {
                FileInfo fileInfo = new FileInfo(mContext, dir);
                boolean hide = dir.isHidden();

                fileInfo.setHideFile(hide);
                mMediaProviderHelper.scanPathforMediaStore(fileInfo.getFileAbsolutePath());
                mApplication.mCache.addCacheFiles(dir.getParent(), fileInfo);
                mResultTask.setSrcFile(fileInfo);
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_SUCCESS);
                return mResultTask;
            } else {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_OPERATION_FAILS);
                return mResultTask;
            }
        }
    }

    public static class RenameTask extends FileOperationTask {
        public static final String TAG = "RenameTask";
        private final FileInfo mDstFileInfo;
        private final FileInfo mSrcFileInfo;
        private String mSearchTextString;
        int mFilterType = 0;
        private int mCategory = -1;

        private String mCurrentPath;
        private int mCurrentMode;
        private TaskInfo mResultTask;
        boolean isExteranalStorage = false;

        public RenameTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;

            mDstFileInfo = mTaskInfo.getDstFile();
            mSrcFileInfo = mTaskInfo.getSrcFile();
            mFilterType = mTaskInfo.getFileFilter();
            mSearchTextString = mTaskInfo.getSearchContent();
            mCurrentPath = mTaskInfo.getDestPath();
            mCurrentMode = mTaskInfo.getAdapterMode();
            mCategory = mTaskInfo.getCategoryIndex();
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;

            String dstFile = mDstFileInfo.getFileAbsolutePath();
            boolean isFolder = true;
            dstFile = dstFile.trim();
            ret = FileUtils.checkFileName(FileUtils.getFileName(dstFile));
            if (ret < 0) {
                CommonUtils.returnTaskResult(mResultTask, ret);
                return mResultTask;
            }

            File newFile = mDstFileInfo.getFile();
            File oldFile = mSrcFileInfo.getFile();

            if (newFile.exists()) {
                mResultTask.setTitleStr(newFile.getName());
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_FILE_EXIST);
                return mResultTask;
            }
            if (CommonUtils.isExternalStorage(dstFile, mMountManager,mApplication.isBuiltInStorage) || !CommonUtils.isInPrivacyMode(mApplication)) {
                isExteranalStorage = true;
            }
            if (oldFile.renameTo(newFile)) {

                IconManager.getInstance().removeCache(oldFile.getAbsolutePath());

                FileInfo newFileInfo = new FileInfo(mContext, newFile);
                boolean hide = newFile.getName().startsWith(".");
                if (hide) {
                    newFileInfo.setHideFile(true);
                }

                if (newFile.isDirectory()) {
                    isFolder = true;
                } else {
                    isFolder = false;
                }
                String mAbsolutePath = newFileInfo.getFileAbsolutePath();
                boolean isNormalReName = mCurrentMode == CommonIdentity.EXTRACT_NORMAL_MODE;
                if(isNormalReName) {
                    mMediaProviderHelper.updateInMediaStore(newFileInfo.getFileAbsolutePath(),
                            mSrcFileInfo.getFileAbsolutePath(), isFolder);
                } else {
                    mMediaProviderHelper.scanMedia(newFile.getAbsolutePath());
                    mMediaProviderHelper.scanMedia(oldFile.getAbsolutePath());
                }
                newFileInfo.setFileMime(FileUtils.getSingleFileMIME(mContext, newFileInfo,isNormalReName));
                if (!isFolder && !isExteranalStorage && PrivateModeManager.isPrivateFile(mPrivateModeHelper, mAbsolutePath)) {
                    newFileInfo.setPrivateFile(true);
                }
                CommonUtils.deleteCache(mSrcFileInfo, CommonUtils.getFileCategory(mSrcFileInfo.getMime()), mApplication.mCache);
                CommonUtils.addCache(mApplication, newFileInfo, mApplication.mCache,mMountManager,false);

                if(mApplication != null && mApplication.mCache != null) {
                    if(mSrcFileInfo.isDirectory()) {
                        mApplication.mCache.removeCache(mSrcFileInfo.getFileAbsolutePath());
                    }
                    if (CommonUtils.isDownLoadFile(mSrcFileInfo.getFileAbsolutePath(),mMountManager)) {
                        mApplication.mCache.removeCache(String.valueOf(CommonIdentity.CATEGORY_DOWNLOAD));
                    }
                    if (CommonUtils.isBluetoothFile(mSrcFileInfo.getFileAbsolutePath(),mMountManager)) {
                        mApplication.mCache.removeCache(String.valueOf(CommonIdentity.CATEGORY_BLUETOOTH));
                    }
                }
                if (SafeManager.mCurrentSafeCategory == 12 && CommonUtils.isSearchStatus(mApplication)) {
                    mFileInfoManager.removeItem(mSrcFileInfo);
                    mFileInfoManager.addItem(newFileInfo);
                    mFileInfoManager.getCategoryFileList().add(newFileInfo);
                }
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_SUCCESS);
                return mResultTask;
            } else {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_OPERATION_FAILS);
                return mResultTask;
            }
        }
    }

    /**
     * check dstFolder space size
     *
     * @param dstFolder
     * @param fileInfoList
     * @return
     */
    protected Integer canOperate(String dstFolder, List<FileInfo> fileInfoList) {
        List<File> fileList = new ArrayList<>();
        UpdateInfo updateInfo = new UpdateInfo();
        int ret = getAllFileList(fileInfoList, fileList, updateInfo);
        if (ret < 0) {
            return ret;
        }

        if (!isEnoughSpace(fileList, dstFolder)) {
            return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
        }
        return ret;
    }

    public static class AddPrivateModeFileTask extends FileOperationTask {

        protected final List<FileInfo> mSrcList;

        protected Context context;
        protected TaskInfo mResultTask;
        private int resultCode = 0;
        private FileManagerApplication mApplication;
        private boolean isExternalFile = false;
        ArrayList<String> mPrivateFileList = new ArrayList<String>();

        public AddPrivateModeFileTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;
            context = mTaskInfo.getApplication();
            if (context == null) {
                throw new IllegalArgumentException();
            }
            mSrcList = mTaskInfo.getSourceFileList();
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            mApplication = mResultTask.getApplication();
            if (mSrcList.isEmpty()) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_ENCRYPT_UNSUCCESS);
                return mResultTask;
            }
            resultCode = insertPrivateFile();
            CommonUtils.returnTaskResult(mResultTask, resultCode);
            return mResultTask;
        }

        private Integer insertPrivateFile() {
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;
            int total = mSrcList.size();
            int progress = 0;
            int mTaskType = mResultTask.getBaseTaskType();
            long mTaskCreateTime = mResultTask.getCreateTaskTime();
            int condition = getConditionCount(total, mTaskType);
            if (mSrcList != null && mSrcList.size() > 0) {
                publishProgress(CommonUtils.getProgressInfo(mSrcList.get(0).getFileName(), mTaskType, mTaskCreateTime, 0, total));
            }
            try {
                for (FileInfo fileInfo : mSrcList) {
                    if (isCancelled()) {
                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (progress % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(fileInfo.getFileName(), mTaskType, mTaskCreateTime, progress, total));
                    }
                    String mPath = fileInfo.getFileAbsolutePath();
                    if (!CommonUtils.isExternalStorage(mPath, mMountManager,mApplication.isBuiltInStorage)) {
                        mPrivateFileList.add(mPath);
                        fileInfo.setPrivateFile(true);
                        CommonUtils.addCache(mApplication, fileInfo, mApplication.mCache, mMountManager);
                        mPrivateModeManager.addPrivateModeFile(mPrivateFileList);
                        mPrivateFileList.clear();
                    } else {
                        isExternalFile = true;
                    }

                    if (progress < total) {
                        progress++;
                    }
                    //CommonUtils.deleteCache(fileInfo, FileUtils.getFileCategory(file.getName()), mApplication.mCache);
                    //publishProgress(CommonUtils.getProgressInfo(fileInfo, mTaskType, mTaskCreateTime, progress, total));
                }
                if (CommonUtils.isCategoryMode() && mResultTask.isShowDir() && SearchTask.searchResultCacheMap != null) {
                    SearchTask.searchResultCacheMap.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ret = OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
            } finally {
                //mPrivateModeManager.addPrivateModeFile(mPrivateFileList);
            }
            if(isExternalFile){
                ret = OperationEventListener.ERROR_SET_PRIVATE_FAILS;
            }
            return ret;

        }

    }

    public static class RemovePrivateModeFileTask extends FileOperationTask {

        protected final List<FileInfo> mSrcList;

        protected Context context;
        protected TaskInfo mResultTask;
        private int resultCode = 0;
        private FileManagerApplication mApplication;
        ArrayList<String> mPrivateFileList = new ArrayList<String>();

        public RemovePrivateModeFileTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;
            context = mTaskInfo.getApplication();
            if (context == null) {
                throw new IllegalArgumentException();
            }
            mSrcList = mTaskInfo.getSourceFileList();
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            mApplication = mResultTask.getApplication();
            if (mSrcList.isEmpty()) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_ENCRYPT_UNSUCCESS);
                return mResultTask;
            }
            resultCode = removePrivateFile();
            CommonUtils.returnTaskResult(mResultTask, resultCode);
            return mResultTask;
        }

        private Integer removePrivateFile() {
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;
            int total = mSrcList.size();
            int progress = 0;
            int mTaskType = mResultTask.getBaseTaskType();
            long mTaskCreateTime = mResultTask.getCreateTaskTime();
            int condition = getConditionCount(total, mTaskType);
            if (mSrcList != null && mSrcList.size() > 0) {
                publishProgress(CommonUtils.getProgressInfo(mSrcList.get(0).getFileName(), mTaskType, mTaskCreateTime, 0, total));
            }
            try {
                for (FileInfo fileInfo : mSrcList) {
                    if (isCancelled()) {
                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (progress % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(fileInfo.getFileName(), mTaskType, mTaskCreateTime, progress, total));
                    }
                    String mPath = fileInfo.getFileAbsolutePath();
                    mPrivateFileList.add(mPath);
                    fileInfo.setPrivateFile(false);
                    CommonUtils.addCache(mApplication, fileInfo, mApplication.mCache, mMountManager);
                    mPrivateModeManager.removePrivateModeFile(mPrivateFileList);
                    mPrivateFileList.clear();
                    if (progress < total) {
                        progress++;
                    }
                    //CommonUtils.deleteCache(fileInfo, FileUtils.getFileCategory(file.getName()), mApplication.mCache);
                    // publishProgress(CommonUtils.getProgressInfo(fileInfo, mTaskType, mTaskCreateTime, progress, total));
                }

                if (CommonUtils.isCategoryMode() && mResultTask.isShowDir() && SearchTask.searchResultCacheMap != null) {
                    SearchTask.searchResultCacheMap.clear();
                }

            } catch (Exception e) {
                e.printStackTrace();
                ret = OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
            } finally {
                //mPrivateModeManager.removePrivateModeFile(mPrivateFileList);
            }

            return ret;

        }

    }


    public static class CompressFileTask extends FileOperationTask {

        protected final List<FileInfo> mSrcList;
        protected String dstFolder;
        protected TaskInfo mResultTask;
        private String mZipFileName;
        private Context mContext;
        private String mzipName = null;
        private String mArchiveName = null;

        private CommonCompress mCompress;
        private int condition = 0;

        @Override
        public void cancel() {
            super.cancel();
            mCompress.cancel();
        }

        public CompressFileTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;
            mContext = mTaskInfo.getApplication();

            mSrcList = mTaskInfo.getSourceFileList();
            dstFolder = mResultTask.getDestPath();
            mArchiveName = mResultTask.getSearchContent();
        }

        public boolean isSourceExists(){
            if (mSrcList != null || mSrcList.size() > 0) {
                return MountManager.getInstance().isMountPoint(mSrcList.get(0).getFileAbsolutePath());
            }
            return false;
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {

            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            if (mSrcList.isEmpty()) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_ENCRYPT_UNSUCCESS);
                return mResultTask;
            }
            if (mSrcList.size() >= 0) {
                //if (mSrcList.get(0).isDirectory()) {
                    mzipName = mArchiveName;
//                } else {
//                    mzipName = FileUtils.getOriginalFileName(mSrcList.get(0).getFileName());
//                }
            }
//            else {
//                mzipName = System.currentTimeMillis() + "";
//            }
            mZipFileName = dstFolder + File.separator + mzipName + ".zip";
            mCompress = CommonCompress.createCompressType(CommonCompress.ZIP);
            int i = 0;
            while (new File(mZipFileName).exists()) {
                mZipFileName = dstFolder + File.separator + mzipName + "(" + i + ")" + ".zip";
                i++;
            }
            int resultCode = compressFile();
            if (resultCode < 0) {
                File mZipFile = new File(mZipFileName);
                if (mZipFile.exists()) {
                    mZipFile.delete();
                }
            }
            CommonUtils.returnTaskResult(mResultTask, resultCode);
            return mResultTask;
        }

        public int compressFile() {
            final int mTaskType = mResultTask.getBaseTaskType();
            final long mTaskCreateTime = mResultTask.getCreateTaskTime();
            publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, 0, 0));
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;
            if (!mCompress.prepare(mSrcList, mZipFileName)) {
                ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
            }

            if ((mArchiveName+".zip").length() > 255) {
                return OperationEventListener.ERROR_COMPRESS_FILE_NAME_TOO_LONG;
            }
            int total = mCompress.getAllFilesCount();
            publishProgress(CommonUtils.getProgressInfo(mSrcList.get(0).getFileName(), mTaskType, mTaskCreateTime, 0, total));
            mCompress.setObserver(new CommonCompress.CompressObserver() {
                @Override
                public void onArchiveComplete(File file, int pos, int total) {
                    if(condition <= 0){
                        condition = getConditionCount(total, mTaskType);
                    }
                    if (pos % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, pos, total));
                    }
                }
                @Override
                public void onUnArchiveComplete(File file, int pos, int total) {

                }
            });
            if (!mCompress.doArchive()) {
                ret = OperationEventListener.ERROR_CODE_EXTRACT_FAIL_TASK;
            }
            // add new file to already exist cache
            if (ret >= 0) {
                copyMediaStoreHelper.addRecord(mZipFileName);
                CommonUtils.addCache(mContext, mZipFileName, dstFolder, mApplication.mCache, mMountManager,CommonIdentity.CATEGORY_ARCHIVES);
            }
            copyMediaStoreHelper.updateRecords();
            return ret;
        }
    }

    public static class ExtractFileTask extends FileOperationTask {

        protected final List<FileInfo> mSrcList;

        protected String dstFolder;
        protected TaskInfo mResultTask;

        private CommonCompress mCompress;
        private FileManagerApplication mApplication;
        private int condition = 0;
        private String folderName;
        @Override
        public void cancel() {
            super.cancel();
            mCompress.cancel();
        }

        public ExtractFileTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mResultTask = mTaskInfo;
            mSrcList = mTaskInfo.getSourceFileList();
            dstFolder = mResultTask.getDestPath();
            mApplication = mResultTask.getApplication();
        }

        public boolean isSourceExists(){
            if (mSrcList != null || mSrcList.size() > 0) {
                return MountManager.getInstance().isMountPoint(mSrcList.get(0).getFileAbsolutePath());
            }
            return false;
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            mResultTask.setTask(getTask());
            mResultTask.setBaseTaskHashcode(getTask().hashCode());
            folderName = mResultTask.getSearchContent();
            if (mSrcList.isEmpty()) {
                CommonUtils.returnTaskResult(mResultTask, OperationEventListener.ERROR_CODE_ENCRYPT_UNSUCCESS);
                return mResultTask;
            }
            int resultCode = ExtractFile();
            CommonUtils.returnTaskResult(mResultTask, resultCode);

            return mResultTask;
        }

        public int ExtractFile() {
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;
            if (mSrcList.get(0) == null) {
                ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                return ret;
            }
            String src = mSrcList.get(0).getFileAbsolutePath();
            String mFolderName = FileUtils.getOriginalFileName(mSrcList.get(0).getFileName());
            String output = null;
            if(folderName != null && !folderName.equals("")){
                output = dstFolder + File.separator + folderName;
            } else {
                output = dstFolder + File.separator + mFolderName;
            }

            int i = 0;
            File mExtractFile = new File(output);
            String mExtractName = output;
            while (mExtractFile.exists()) {
                mExtractName = output + "(" + i + ")";
                mExtractFile = new File(mExtractName);
                i++;
            }
            output = mExtractName;
            if (mExtractFile != null && !mExtractFile.mkdirs()) {
                ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                return ret;
            }
            String extension = FileUtils.getFileExtension(mSrcList.get(0).getFileName());
            if (TextUtils.isEmpty(extension)) {
                ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                return ret;
            } else if (extension.equalsIgnoreCase("zip")) {
                mCompress = CommonCompress.createCompressType(CommonCompress.ZIP);
            } else if (extension.equalsIgnoreCase("tar")) {
                mCompress = CommonCompress.createCompressType(CommonCompress.TAR);
            } else if (extension.equalsIgnoreCase("rar")) {
                mCompress = CommonCompress.createCompressType(CommonCompress.RAR);
            } else {
                ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                return ret;
            }

            final int mTaskType = mResultTask.getBaseTaskType();
            final long mTaskCreateTime = mResultTask.getCreateTaskTime();
            if (mSrcList != null && mSrcList.size() > 0) {
                publishProgress(CommonUtils.getProgressInfo(mSrcList.get(0).getFileName(), mTaskType, mTaskCreateTime, 0, 0));
            }

            mCompress.setObserver(new CommonCompress.CompressObserver() {
                @Override
                public void onArchiveComplete(File file, int pos, int total) {
                }

                @Override
                public void onUnArchiveComplete(File file, int pos, int total) {
                    if(condition <= 0) {
                        condition = getConditionCount(total, mTaskType);
                    }
                    if (file != null) {
                        copyMediaStoreHelper.addRecord(file.getAbsolutePath());
                    }
                    if (pos % condition == 0) {
                        publishProgress(CommonUtils.getProgressInfo(file.getName(), mTaskType, mTaskCreateTime, pos, total));
                    }
                }
            });

            if (mCompress.doUnArchive(src, output + File.separator)) {
                ret = OperationEventListener.ERROR_CODE_SUCCESS;
            } else {
                ret = OperationEventListener.ERROR_CODE_EXTRACT_FAIL_TASK;
            }
            // add new file to already exist cache
            if (ret >= 0) {
                CommonUtils.addCache(mContext, output, dstFolder, mApplication.mCache,mMountManager, -1);
                copyMediaStoreHelper.updateRecords();
            }

            return ret;
        }

    }


    public static class CategoryCountTask extends FileOperationTask {
        private final Context mContext;
        String mSizeStr = "";
        FileManagerApplication mApplication;
        TaskInfo mBaseTaskInfo;
        CategoryManager.CountTextCallback mCallback;
        boolean isTctPrivateColumn;

        public CategoryCountTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mBaseTaskInfo = mTaskInfo;
            mContext = mTaskInfo.getApplication();
            mApplication = mTaskInfo.getApplication();
            mCallback = mTaskInfo.getCountCallback();
            isTctPrivateColumn = CommonUtils.isTctPrivateColumn(mContext);
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            int total = 9;
            if (CommonUtils.isInPrivacyMode(mContext)) {
                total = 10;
            }
            for (int i = 0; i < total; i++) {
                String count;
                mSizeStr = "";
                if (i == 2 || i == 4) {
                    int mPathCount = CategoryManager.getCountFromFiles(mContext, i);
                    mSizeStr = CategoryManager.mSizeStr;
                    if (TextUtils.isEmpty(mSizeStr)) {
                        mSizeStr = "0 B";
                    }
                    if (mPathCount >= 1000) {
                        count = "999+ (" + mSizeStr + ")";
                    } else {
                        count = mPathCount + " (" + mSizeStr + ")";
                    }
                } else {
                    count = getCountFromMedia(i, mContext,isTctPrivateColumn);
                    if(count == null || count.equals("")){
                        count = "0"+ " (0 B)";
                    }
                }
                mBaseTaskInfo.setCategoryIndex(i);
                mBaseTaskInfo.setSearchContent(count);
                mCallback.countTextCallback(mBaseTaskInfo);

            }
            return mBaseTaskInfo;
        }
    }


    public static class SafeCategoryCountTask extends FileOperationTask {
        private final Context mContext;
        FileManagerApplication mApplication;
        TaskInfo mBaseTaskInfo;
        SafeManager.CountTextCallback mCallback;
        boolean isTctPrivateColumn;

        public SafeCategoryCountTask(TaskInfo mTaskInfo) {
            super(mTaskInfo);
            mBaseTaskInfo = mTaskInfo;
            mContext = mTaskInfo.getApplication();
            mApplication = mTaskInfo.getApplication();
            mCallback = mTaskInfo.getSafeCountCallback();
            isTctPrivateColumn = CommonUtils.isTctPrivateColumn(mContext);
        }

        @Override
        protected TaskInfo doInBackground(Void... object) {
            int total = 6;

            for (int i = 0; i < total; i++) {
                String count;

                count = getSafeCountFromMedia(i, mContext,isTctPrivateColumn);

                mBaseTaskInfo.setCategoryIndex(i);
                mBaseTaskInfo.setSearchContent(count);
                mCallback.countTextCallback(mBaseTaskInfo);

            }
            return mBaseTaskInfo;
        }
    }

    public String getCountFromMedia(int position, Context context,boolean isTctPrivateColumn) {
        Uri uri = null;
        String mQuerySizeStr;

        String[] projection = {"count(*)", "+sum(" + MediaStore.Files.FileColumns.SIZE + ")"};
        String count = "";

        if (CommonIdentity.CATEGORY_PICTURES == position) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (CommonIdentity.CATEGORY_VEDIOS == position) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (CommonIdentity.CATEGORY_MUSIC == position) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = MediaStore.Files.getContentUri("external");
        }
        StringBuilder sb = new StringBuilder();

        if (!mApplication.isShowHidden) {
            sb.append(MediaStore.Files.FileColumns.TITLE + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, ".%");
            sb.append(" and ");
        }

        sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, "null");

        if (isTctPrivateColumn && !CommonUtils.isInPrivacyMode(mContext) && position != CommonIdentity.CATEGORY_SAFE) {
            sb.append(" and ");
            sb.append(CommonIdentity.TCT_IS_PRIVATE + " != ");
            DatabaseUtils.appendEscapedSQLString(sb, "1");
        }
        String selection0 = sb.toString();
        if (CommonIdentity.CATEGORY_DOCS == position) {

            //avoid to get folders like xx.mp4,xx.mp3, xx.apk
            sb.append(" and ");
            sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
            DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");


            sb.append(" and (").append(

                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            sb.append(")");
        } else if (CommonIdentity.CATEGORY_APKS == position) {

            //avoid to get folders like xx.mp4,xx.mp3, xx.apk
            sb.append(" and ");
            sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
            DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");

            sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "application/vnd.android.package-archive");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
            sb.append(")");
        } else if (CommonIdentity.CATEGORY_RECENT == position) {
            //avoid to get folders like xx.mp4,xx.mp3, xx.apk
            sb.append(" and ");
            sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
            DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
            sb.append(" and ").append("bucket_display_name not like ");
            DatabaseUtils.appendEscapedSQLString(sb, ".thumbnails");
            sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "text/%");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.jpg");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.jpeg");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.png");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.bmp");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mp3");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.wav");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mp4");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.avi");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.mov");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.m4a");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.3gp");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.dcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.3gpp");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.gif");
            sb.append(") and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " > " + CommonUtils.getYesterdayTime());
            sb.append(" and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " < " + CommonUtils.getCurrentTime());

        } else if (CommonIdentity.CATEGORY_ARCHIVES == position) {
            sb.append(" and (").append(
                    MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "application/zip");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.tar");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.7z");
            sb.append(")");
        } else if (CommonIdentity.CATEGORY_SAFE == position) {
            sb.append(" and ").append(CommonIdentity.TCT_IS_PRIVATE + " = ");
            DatabaseUtils.appendEscapedSQLString(sb, "1");
        }

        String selection = sb.toString();

        Cursor cursor = null;
        try {
            if (CommonIdentity.CATEGORY_PICTURES == position ||
                    CommonIdentity.CATEGORY_VEDIOS == position ||
                    CommonIdentity.CATEGORY_MUSIC == position) {

                //select * from video where _data not in (select _data from files where  format == 12289) ;
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                StringBuilder sb1 = new StringBuilder();
                sb1.append(selection0);
                sb.append(" and (_data not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                selection0 = sb.toString();

                cursor = context.getContentResolver().query(uri, projection, selection0, null, null);
            } else if (CommonIdentity.CATEGORY_DOCS == position ||
                    CommonIdentity.CATEGORY_APKS == position
                    || CommonIdentity.CATEGORY_RECENT == position
                    || CommonIdentity.CATEGORY_ARCHIVES == position
                    || CommonIdentity.CATEGORY_SAFE == position
                    ) {
                cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            }
            //Filter is a folder type file.
            if (cursor != null) {

                if (cursor.moveToNext()) {
                    long size = cursor.getLong(1);
                    mQuerySizeStr = FileUtils.sizeToString(context, size);
                    if (TextUtils.isEmpty(mQuerySizeStr)) {
                        mQuerySizeStr = "0 B";
                    }
                    if (cursor.getInt(0) >= 1000) {
                        count = "" + "999+" + " (" + mQuerySizeStr + ")";
                    } else {
                        count = "" + cursor.getInt(0) + " (" + mQuerySizeStr + ")";
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public String getSafeCountFromMedia(int position, Context context,boolean isTctPrivateColumn) {
        Uri uri = null;

        String[] projection = {"count(*)"};
        String count = "";

        if (CommonIdentity.SAFE_CATEGORY_PICTURES == position) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (CommonIdentity.SAFE_CATEGORY_VEDIO == position) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (CommonIdentity.SAFE_CATEGORY_MUISC == position) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            uri = MediaStore.Files.getContentUri("external");
        }
        StringBuilder sb = new StringBuilder();

        if (!mApplication.isShowHidden) {
            sb.append(MediaStore.Files.FileColumns.TITLE + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, ".%");
            sb.append(" and ");
        }

        sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
        DatabaseUtils.appendEscapedSQLString(sb, "null");

        if (isTctPrivateColumn && CommonUtils.isInPrivacyMode(mContext)) {
            sb.append(" and ");
            sb.append(CommonIdentity.TCT_IS_PRIVATE + " != ");
            DatabaseUtils.appendEscapedSQLString(sb, "1");
            sb.append(" and ");
            sb.append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "/storage/emulated/0/%");
        }
        String selection0 = sb.toString();
        if (CommonIdentity.SAFE_CATEGORY_DOCS == position) {
            //avoid to get folders like xx.mp4,xx.mp3, xx.apk
            sb.append(" and (").append(
                    MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "text/%");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
            sb.append(")");
        } else if (CommonIdentity.SAFE_CATEGORY_INSTALLERS == position) {

            //avoid to get folders like xx.mp4,xx.mp3, xx.apk
            sb.append(" and ");
            sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
            DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");

            sb.append(" and (").append(MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "application/vnd.android.package-archive");
            sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
            sb.append(")");
        } else if (CommonIdentity.SAFE_CATEGORY_ARCHIVES == position) {
            sb.append(" and (").append(
                    MediaStore.Files.FileColumns.MIME_TYPE + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "application/zip");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.tar");
            sb.append(" or ").append(
                    MediaStore.Files.FileColumns.DATA + " like ");
            DatabaseUtils.appendEscapedSQLString(sb, "%.7z");
            sb.append(")");
        }

        String selection = sb.toString();

        Cursor cursor = null;
        try {
            if (CommonIdentity.SAFE_CATEGORY_PICTURES == position ||
                    CommonIdentity.SAFE_CATEGORY_VEDIO == position ||
                    CommonIdentity.SAFE_CATEGORY_MUISC == position) {

                //select * from video where _data not in (select _data from files where  format == 12289) ;
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                StringBuilder sb1 = new StringBuilder();
                sb1.append(selection0);
                sb.append(" and (_data not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                selection0 = sb.toString();

                cursor = context.getContentResolver().query(uri, projection, selection0, null, null);
            } else if (CommonIdentity.SAFE_CATEGORY_DOCS == position ||
                    CommonIdentity.SAFE_CATEGORY_INSTALLERS == position
                    || CommonIdentity.SAFE_CATEGORY_ARCHIVES == position
                    ) {
                cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            }
            //Filter is a folder type file.
            if (cursor != null) {

                if (cursor.moveToNext()) {

                    if (cursor.getInt(0) >= 1000) {
                        count = "" + "999+";
                    } else {
                        count = "" + cursor.getInt(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {

                cursor.close();
            }
        }
        return count;
    }
}
