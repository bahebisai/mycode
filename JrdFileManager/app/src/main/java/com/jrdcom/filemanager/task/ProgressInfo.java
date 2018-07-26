package com.jrdcom.filemanager.task;

import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.Serializable;

public class ProgressInfo implements Serializable {

    private String mUpdateInfo;
    private final int mProgress;
    private int mErrorCode;
    private final long mTotal;
    private final boolean mIsFailInfo;
    private FileInfo mFileInfo;
    private int mProgressTaskType;
    private long mCreateTime;
    private long avaiableSize;
    private long totalSize;
    public static final int M_MODE = 1;
    private int mUnitStyle = 0;
    private boolean isSaveMap;
    private TaskInfo taskInfo;


    public long getAvaiableSize() {
        return avaiableSize;
    }


    public long getTotalSize() {
        return totalSize;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;

    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }


    public boolean isSaveMap() {
        return isSaveMap;
    }

    public void setSaveMap(boolean saveMap) {
        isSaveMap = saveMap;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long CreateTime) {
        this.mCreateTime = CreateTime;
    }

    public int getProgressTaskType() {
        return mProgressTaskType;
    }

    public void setProgressTaskType(int progressTask) {
        this.mProgressTaskType = progressTask;
    }


    public int getUnitStyle() {
        return mUnitStyle;
    }

    public void setUnitStyle(int style) {
        this.mUnitStyle = style;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param update    the string which will be shown on ProgressPopupWindow
     * @param progeress current progress number
     * @param total     total number
     */
    public ProgressInfo(String update, int progeress, long total) {
        this.mUpdateInfo = update;
        this.mProgress = progeress;
        this.mTotal = total;
        this.mIsFailInfo = false;
    }

    public ProgressInfo(String update, long progeress, long total) {
        this.mUpdateInfo = update;
        this.avaiableSize = progeress;
        this.mProgress = 0;
        this.mTotal = 0;
        this.totalSize = total;
        this.mIsFailInfo = false;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param fileInfo  the fileInfo which will be associated with Dialog
     * @param progeress current progress number
     * @param total     total number
     */
    public ProgressInfo(FileInfo fileInfo, int progeress, long total) {
        this.mFileInfo = fileInfo;
        this.mProgress = progeress;
        this.mTotal = total;
        this.mIsFailInfo = false;
    }

    /**
     * Constructor to construct a ProgressInfo
     *
     * @param errorCode  An int represents ERROR_CODE
     * @param isFailInfo status of task associated with certain progressDialog
     */
    public ProgressInfo(int errorCode, boolean isFailInfo) {
        this.mErrorCode = errorCode;
        this.mProgress = 0;
        this.mTotal = 0;
        this.mIsFailInfo = isFailInfo;
    }

    /**
     * This method gets status of task doing in background
     *
     * @return true for failed, false for no fail occurs in task
     */
    public boolean isFailInfo() {
        return mIsFailInfo;
    }

    /**
     * This method gets fileInfo, which will be updated on DetaiDialog
     *
     * @return fileInfo, which contains file's information(name, size, and so
     * on)
     */
    public FileInfo getFileInfo() {
        return mFileInfo;
    }

    /**
     * This method gets ERROR_CODE for certain task, which is doing in
     * background.
     * //[FEATURE]-Add-END by TSNJ
     *
     * @return ERROR_CODE for certain task
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * This method gets the content, which will be updated on ProgressDialog
     *
     * @return content, which need update
     */
    public String getUpdateInfo() {
        return mUpdateInfo;
    }

    /**
     * This method gets current progress number
     *
     * @return current progress number of progressDialog
     */
    public int getProgeress() {
        return mProgress;
    }

    /**
     * This method gets total number of progressDialog
     *
     * @return total number
     */
    public long getTotal() {
        return mTotal;
    }
}
