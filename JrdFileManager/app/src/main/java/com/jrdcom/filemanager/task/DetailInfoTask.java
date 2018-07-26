package com.jrdcom.filemanager.task;


import android.os.Environment;
import android.util.Log;

import com.jrdcom.filemanager.listener.OperationEventListener;


import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;

public class DetailInfoTask extends BaseAsyncTask {

    private final FileInfo mDetailfileInfo;
    private int mDetailFileType;
    private TaskInfo mDetailTask;

    /**
     * Constructor of DetailInfoTask
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *                        information of files in FileManager.
     * @param operationEvent  a instance of OperationEventListener, which is a
     *                        interface doing things before/in/after the task.
     * @param file            a instance of FileInfo, which contains all data about a file.
     */
    public DetailInfoTask(TaskInfo mTaskInfo) {
        super(mTaskInfo);
        mDetailfileInfo = mTaskInfo.getSrcFile();
        mDetailFileType = mTaskInfo.getAdapterMode();
        mDetailTask = mTaskInfo;
    }

    @Override
    protected TaskInfo doInBackground(Void... object) {
        mDetailTask.setTask(getTask());
        mDetailTask.setBaseTaskHashcode(getTask().hashCode());
        if (mDetailfileInfo.isDirectory()) {
            mDetailTask.setFileSize(getSize(mDetailfileInfo.getFile()));
        } else {
            mDetailTask.setFileSize(mDetailfileInfo.getFileSize());

        }
        CommonUtils.returnTaskResult(mDetailTask, OperationEventListener.ERROR_CODE_SUCCESS);
        return mDetailTask;
    }

    private static long getSize(File file) {
        long folderSize = 0;
        try {
            if (!file.exists()) {
                String message = file + " does not exist";
                throw new IllegalArgumentException(message);
            }
            if (file.isDirectory()) {
                folderSize = sizeOfDirectory(file);
            } else
                folderSize = file.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderSize;
    }

    private static long sizeOfDirectory(File directory) {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File dir0 = Environment.maybeTranslateEmulatedPathToInternal(directory);
        File[] files = dir0.listFiles();
        if (files == null) {
            return 0L;
        }
        //add by long.tang@tcl.com
        int len = files.length;
        for (int i = 0; i < len; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                size += sizeOfDirectory(file);
            } else {
                size += file.length();
            }
        }
        return size;
    }
}
