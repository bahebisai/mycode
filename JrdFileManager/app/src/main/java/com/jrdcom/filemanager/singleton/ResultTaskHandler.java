package com.jrdcom.filemanager.singleton;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jrdcom.filemanager.IActivityListener;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.TaskInfo;

/**
 * Created by user on 16-7-15.
 */
public class ResultTaskHandler extends Handler {
    private static ResultTaskHandler mResultHandler = null;
    private static IActivityListener mActivytListener;

    private ResultTaskHandler() {

    }

   private ResultTaskHandler(IActivityListener activytListener) {
        mActivytListener = activytListener;
    }

    public static ResultTaskHandler getInstance(IActivityListener activytListener) {

        mActivytListener = activytListener;
        if (mResultHandler == null) {
            mResultHandler = new ResultTaskHandler(mActivytListener);
        }

        return mResultHandler;

    }

    public IActivityListener getActivytListener() {
        return mActivytListener;
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle mBundle = msg.getData();
        TaskInfo mResultInfo = (TaskInfo) mBundle.getSerializable(CommonIdentity.RESULT_TASK_KEY);
        switch (mResultInfo.getBaseTaskType()) {
            case CommonIdentity.PROGRESS_SHOW_TASK:
                mActivytListener.managerTaskResult(mResultInfo);
                break;
            case CommonIdentity.OBSERVER_UPDATE_TASK:
                mActivytListener.managerTaskResult(mResultInfo);
                break;
            case CommonIdentity.CREATE_FOLDER_TASK:
            case CommonIdentity.RENAME_FILE_TASK:
            case CommonIdentity.SEARCH_INFO_TASK:
            case CommonIdentity.STORAGE_SPACE_TASK:
            case CommonIdentity.DETAIL_FILE_TASK:
            case CommonIdentity.UPDATE_PERCENTAGEBAR_TASK:
            case CommonIdentity.LIST_INFO_TASK:
            case CommonIdentity.PROGRESS_DIALOG_TASK:
            case CommonIdentity.PROGRESS_COMPLETE_TASK:
                mActivytListener.managerTaskResult(mResultInfo);
                break;
            case CommonIdentity.PASTE_COPY_TASK:
            case CommonIdentity.NORMAL_DELETE_TASK:
            case CommonIdentity.PASTE_CUT_TASK:
            case CommonIdentity.FILE_COMPRESSION_TASK:
            case CommonIdentity.FILE_UNCOMPRESSION_TASK:
            case CommonIdentity.ADD_PRIVATE_FILE_TASK:
            case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                if(mResultInfo.getApplication() == null){
                    return;
                }
                mResultInfo.getApplication().mFileInfoManager.wakeWaittingTask(mResultInfo);
                mActivytListener.managerTaskResult(mResultInfo);
            default:
                break;
        }
    }
}
