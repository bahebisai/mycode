package com.jrdcom.filemanager.manager;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 16-9-19.
 */
public class FolderCountManager {

    private FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();

    private FolderCountManager.FolderCountTextCallback countCallback;
    private static FolderCountManager sInstance = new FolderCountManager();

    public static FolderCountManager getInstance() {
        return sInstance;
    }


    public FolderCountManager.FolderCountTextCallback getCountCallback() {
        return countCallback;
    }
    public static Map<Integer,String> mFolderItemMap = new HashMap<Integer,String>();

    public void setCountCallback(FolderCountManager.FolderCountTextCallback countCallback) {
        this.countCallback = countCallback;
    }

    public void loadFolderCountText(final FolderCountManager.FolderCountTextCallback callback, List<FileInfo> mSrcList) {

        TaskInfo mFolderCountTaskInfo = new TaskInfo(mApplication, null, CommonIdentity.FOLDER_COUNT_TASK);
        mFolderCountTaskInfo.setFolderCountCallback(callback);
        mFolderCountTaskInfo.setSourceFileList(mSrcList);
        mApplication.mFileInfoManager.addNewTask(mFolderCountTaskInfo);

    }

    public interface FolderCountTextCallback {
        public void folderCountTextCallback(TaskInfo mTaskInfo);
    }
}
