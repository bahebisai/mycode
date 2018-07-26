package com.jrdcom.filemanager.listener;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.singleton.ResultTaskHandler;
import com.jrdcom.filemanager.singleton.RunningTaskMap;
import com.jrdcom.filemanager.task.ProgressInfo;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.LogUtils;
import com.jrdcom.filemanager.utils.TaskInfo;
import com.jrdcom.filemanager.view.ToastHelper;

/**
 * Created by user on 16-7-9.
 */
public class HeavyOperationListener implements OperationEventListener {

    private ResultTaskHandler mResultTaskHandler;
    private boolean mPermissionToast = false;
    private boolean mOperationToast = false;
    int result = -1;
    private TaskInfo mResultTask;
    private FileManagerApplication mApplication;
    private ToastHelper mToastHelper;
    private TaskInfo mListenerInfo;
    private boolean isHashMap = true;


    public HeavyOperationListener(TaskInfo listenerInfo) {
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        mListenerInfo = listenerInfo;
        mToastHelper = new ToastHelper(mApplication);


    }

    @Override
    public void onTaskPrepare() {
        mResultTaskHandler = mApplication.getAppHandler();
        isHashMap = true;

        int mTaskType = mListenerInfo.getFileFilter();
        switch (mTaskType) {
            case CommonIdentity.PASTE_COPY_TASK:
            case CommonIdentity.NORMAL_DELETE_TASK:
            case CommonIdentity.PASTE_CUT_TASK:
            case CommonIdentity.PROGRESS_DIALOG_TASK:
            case CommonIdentity.LIST_INFO_TASK:
            case CommonIdentity.FILE_COMPRESSION_TASK:
            case CommonIdentity.FILE_UNCOMPRESSION_TASK:
            case CommonIdentity.ADD_PRIVATE_FILE_TASK:
            case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                Message msg = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mListenerInfo);
                msg.setData(mBundle);
                mResultTaskHandler.handleMessage(msg);
                break;
            default:

                break;
        }

    }

    @Override
    public void onTaskProgress(ProgressInfo progressInfo) {
        if (progressInfo.isFailInfo()) {
            TaskInfo mCompleteTaskInfo = new TaskInfo(CommonIdentity.PROGRESS_COMPLETE_TASK);
            mCompleteTaskInfo.setErrorCode(progressInfo.getErrorCode());
            Message msg = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mCompleteTaskInfo);
            msg.setData(mBundle);
            mResultTaskHandler.handleMessage(msg);
            return;
        } else {
            int mTaskType = progressInfo.getProgressTaskType();
            switch (mTaskType) {
                case CommonIdentity.PASTE_COPY_TASK:
                case CommonIdentity.NORMAL_DELETE_TASK:
                case CommonIdentity.PASTE_CUT_TASK:
                case CommonIdentity.FILE_COMPRESSION_TASK:
                case CommonIdentity.FILE_UNCOMPRESSION_TASK:
                case CommonIdentity.ADD_PRIVATE_FILE_TASK:
                case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                    TaskInfo mTaskInfo = new TaskInfo(CommonIdentity.PROGRESS_SHOW_TASK);
                    mTaskInfo.setCreateTaskTime(progressInfo.getCreateTime());
                    mTaskInfo.setProgressInfo(progressInfo);
                    Message msg = new Message();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mTaskInfo);
                    msg.setData(mBundle);
                    mResultTaskHandler.handleMessage(msg);
                    break;
                default:
                    break;
            }


        }
    }

    @Override
    public void onTaskResult(TaskInfo mTaskInfo) {
        mResultTask = mTaskInfo;
        result = mTaskInfo.getResultCode();
        Message msg = new Message();
        Bundle mBundle = new Bundle();
        mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mTaskInfo);
        msg.setData(mBundle);
        if (mTaskInfo == null) {
            return;
        }
        if (mResultTaskHandler == null) {
            mResultTaskHandler = mApplication.getAppHandler();
            if (mResultTaskHandler == null) {
                return;
            }
        }
        mResultTaskHandler.handleMessage(msg);


    }

}
