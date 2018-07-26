package com.jrdcom.filemanager.listener;

import com.jrdcom.filemanager.task.ProgressInfo;
import com.jrdcom.filemanager.utils.TaskInfo;

/**
 * Created by user on 16-7-9.
 */


public interface OperationEventListener {

    int ERROR_CODE_NAME_VALID = 100;
    int ERROR_CODE_SUCCESS = 0;
    int ERROR_CODE_DECRYPT_SUCCESS = 1;
    int ERROR_CODE_UNSUCCESS = -1;
    int ERROR_CODE_NAME_EMPTY = -2;
    int ERROR_CODE_NAME_TOO_LONG = -3;
    int ERROR_CODE_FILE_EXIST = -4;
    int ERROR_CODE_NOT_ENOUGH_SPACE = -5;
    int ERROR_CODE_DELETE_FAILS = -6;
    int ERROR_CODE_USER_CANCEL = -7;
    int ERROR_CODE_PASTE_TO_SUB = -8;
    int ERROR_CODE_UNKOWN = -9;
    int ERROR_CODE_COPY_NO_PERMISSION = -10;
    int ERROR_CODE_MKDIR_UNSUCCESS = -11;
    int ERROR_CODE_CUT_SAME_PATH = -12;
    int ERROR_CODE_BUSY = -100;
    int ERROR_CODE_DELETE_UNSUCCESS = -13;
    int ERROR_CODE_PASTE_UNSUCCESS = -14;
    int ERROR_CODE_DELETE_NO_PERMISSION = -15;
    int ERROR_CODE_FAVORITE_UNSUCESS = -16;
    int ERROR_CODE_ENCRYPT_UNSUCCESS = -17;
    int ERROR_INVALID_CHAR = -20;
    int ERROR_COMPRESS_FILE_NAME_TOO_LONG = -21;
    int ERROR_CODE_EXCEEDED_MAX_TASK = -22;
    int ERROR_CODE_ADD_WAITING_TASK = -23;
    int ERROR_CODE_EXTRACT_FAIL_TASK = -24;
    int ERROR_CODE_OPERATION_FAILS = -25;
    int ERROR_SET_PRIVATE_FAILS = -26;

    int ERROR_SAFE_SIZE_LIMTED = -18;
    int ERROR_SAFE_DRM_LIMTED = -19;

    /**
     * This method will be implemented, and called in onPreExecute of
     * asynctask
     */
    void onTaskPrepare();

    /**
     * This method will be implemented, and called in onProgressUpdate
     * function of asynctask
     *
     * @param progressInfo information of ProgressInfo, which will be
     *                     updated on UI
     */
    void onTaskProgress(ProgressInfo progressInfo);

    /**
     * This method will be implemented, and called in onPostExecute of
     * asynctask
     *
     * @param result the result of asynctask's doInBackground()
     */
    void onTaskResult(TaskInfo mTaskInfo);

}
