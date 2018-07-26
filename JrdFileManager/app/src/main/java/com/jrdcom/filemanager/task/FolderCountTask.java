package com.jrdcom.filemanager.task;

import android.content.Context;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.manager.FolderCountManager;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.List;

/**
 * Created by user on 16-9-21.
 */
public class FolderCountTask extends BaseAsyncTask {
    private final Context mContext;
    FileManagerApplication mApplication;
    TaskInfo mBaseTaskInfo;
    FolderCountManager.FolderCountTextCallback mFolderCallback;
    List<FileInfo> mSrcList;
    private static FolderCountTask mFolderCountTask;

    public static BaseAsyncTask getInstance(TaskInfo mTaskInfo, boolean isCreate) {
        if (mTaskInfo == null && mFolderCountTask != null && !isCreate) {
            return mFolderCountTask;
        } else if (isCreate) {
            mFolderCountTask = null;
            mFolderCountTask = new FolderCountTask(mTaskInfo);
        }
        return mFolderCountTask;
    }

    private FolderCountTask(TaskInfo mTaskInfo) {
        super(mTaskInfo);
        mBaseTaskInfo = mTaskInfo;
        mContext = mTaskInfo.getApplication();
        mApplication = mTaskInfo.getApplication();
        mFolderCallback = mTaskInfo.getFolderCountCallback();
        mSrcList = mTaskInfo.getSourceFileList();
    }

    @Override
    protected TaskInfo doInBackground(Void... object) {
        if(mSrcList == null){
            return mBaseTaskInfo;
        }
        for (int i = 0; i < mSrcList.size(); i++) {
            String count = "";
            FileInfo mCurrentInfo = mSrcList.get(i);
            if(mCurrentInfo != null) {
                File mFile = mCurrentInfo.getFile();
                if (mFile.isDirectory()) {
                    File[] mListFile = mFile.listFiles();
                    if (mListFile != null && mListFile.length > 0) {
                        String path = null;
                        try {
                            path = mSrcList.get(i).getFileAbsolutePath();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (mListFile[0].getAbsolutePath() != null && path != null && !mListFile[0].getAbsolutePath().startsWith(path))
                            continue;
                    }
                    int mCount = FileUtils.isShowHideCount(mListFile);
                    if (mCount == 0) {
                        count = "0";
                    } else {
                        count = mCount + "";
                    }
                }
            }
            mBaseTaskInfo.setCategoryIndex(i);
            mBaseTaskInfo.setSearchContent(count);
            mFolderCallback.folderCountTextCallback(mBaseTaskInfo);

        }
        return mBaseTaskInfo;
    }
}