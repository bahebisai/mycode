package com.jrdcom.filemanager.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StatFs;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.List;


public class SetRootSizeTextTask extends BaseAsyncTask {


    private String mFilePath;
    private List<TaskInfo> mStorageTaskInfoList;
    private FileManagerApplication mApplication;

    private int mStroageMode = 0;

    public SetRootSizeTextTask(TaskInfo mTaskInfo) {
        super(mTaskInfo);
        mApplication = mTaskInfo.getApplication();
        mStorageTaskInfoList = mTaskInfo.getTaskInfoList();
    }

    @Override
    protected TaskInfo doInBackground(Void... object) {
        TaskInfo mStorageTaskInfo = null;
        for (int i = 0; i < mStorageTaskInfoList.size(); i++) {
            long totalSpace = 0;
            long availaBlock = 0;
            String sizeString;
            String totalSpaceString;
            String usedSpaceString;
            mStorageTaskInfo = mStorageTaskInfoList.get(i);
            mFilePath = mStorageTaskInfo.getSrcPath();
            mStroageMode = mStorageTaskInfo.getAdapterMode();
            StringBuilder sb = new StringBuilder();
            long blocSize = 0;
            long blockCount = 0;
            try {
                while (totalSpace <= 0 && MountManager.getInstance().isMountPoint(mFilePath)) {
                    StatFs statfs = new StatFs(mFilePath);
                    try {
                        blocSize = statfs.getBlockSizeLong();
                        availaBlock = new File(mFilePath).getFreeSpace();
                        blockCount = statfs.getBlockCountLong();
                    } catch (NoSuchMethodError e) {
                        blocSize = statfs.getBlockSizeLong();
                        availaBlock = new File(mFilePath).getFreeSpace();
                        blockCount = statfs.getBlockCountLong();
                    }
                    totalSpace = blocSize * blockCount;
                }
            } catch (Exception e) {
                totalSpace = 0;
                e.printStackTrace();
            }
            totalSpaceString = FileUtils.sizeToString(mContext, totalSpace);
            usedSpaceString = FileUtils.sizeToString(mContext, (totalSpace - availaBlock));
            if (mStroageMode == CommonIdentity.STORAGE_INFO_SAFEBOX) {
                sb.append(mContext.getResources().getString(R.string.freeof_m) + usedSpaceString).append("/ ");
                sb.append(usedSpaceString).append("/ ");
//            } else if ( MountManager.getInstance().isSDCardMounted() && MountManager.getInstance().isOtgMounted()
//                    && !mApplication.mPortraitOrientation) {
//                // if all storage displayed, use "/ " instead of "used of"
//                sb.append(usedSpaceString).append("/ ");
            } else {
                sb.append(usedSpaceString).append("/");
                //sb.append(usedSpaceString).append(" "+mApplication.getResources().getString(R.string.used_of)+" ");
            }
            sb.append(totalSpaceString);
            sizeString = sb.toString();
            if(mStroageMode == CommonIdentity.STORAGE_INFO_CATEGORY) {
                mStorageTaskInfo.getListener().onTaskProgress(CommonUtils.getProgressInfo(sizeString, availaBlock, totalSpace, mStorageTaskInfo));
            } else if(mStroageMode == CommonIdentity.STORAGE_INFO_SAFEBOX){
                mStorageTaskInfo.getListener().onTaskProgress(CommonUtils.getProgressInfo(sizeString, 0, 0, mStorageTaskInfo));
            }
        }
        return mStorageTaskInfo;
    }

}
