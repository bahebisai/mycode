package com.jrdcom.filemanager.utils;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.manager.FolderCountManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.task.BaseAsyncTask;
import com.jrdcom.filemanager.task.ProgressInfo; // MODIFIED by wenjing.ni, 2016-07-23,BUG-2521972

import java.io.Serializable;
import java.util.List;

/**
 * Created by user on 16-7-13.
 */
public class TaskInfo implements Serializable {

    private OperationEventListener mListener;
    private BaseAsyncTask mTask;
    private AsyncTask mStorageTask;
    private int mBaseTaskType;
    private int mBaseTaskHashcode;
    private FileManagerApplication mApplication;
    private String mDestPath;
    private int mFileFilter;
    private List<FileInfo> mSourceFileList;
    private String mSrcPath;
    private int mResultCode;
    private FileInfo mDstFile;
    private int mAdapterMode;
    private FileInfo mSrcFile;
    private String mSearchContent;
    private boolean isShowDir;
    private int mDrmType;
    private long mFileSize;
    private long mAllSize;
    private String mTitleStr;


    private long createTaskTime;
    private int mCategoryIndex = -1;
    private TextView mStorageSize;

    private TextView mStoragePercent;
    private ProgressBar mStorageProgress;
    private int mRefreshMode;
    private ProgressInfo mProgressInfo;
    private int errorCode;
    private CategoryManager.CountTextCallback countCallback;
    private SafeManager.CountTextCallback safeCountCallback;
    private FolderCountManager.FolderCountTextCallback folderCountCallback;

    public SafeManager.CountTextCallback getSafeCountCallback() {
        return safeCountCallback;
    }

    public void setSafeCountCallback(SafeManager.CountTextCallback safeCountCallback) {
        this.safeCountCallback = safeCountCallback;
    }

    public List<TaskInfo> getTaskInfoList() {
        return taskInfoList;
    }

    public void setTaskInfoList(List<TaskInfo> taskInfoList) {
        this.taskInfoList = taskInfoList;
    }

    private List<TaskInfo> taskInfoList;

    public CategoryManager.CountTextCallback getCountCallback() {
        return countCallback;
    }

    public void setCountCallback(CategoryManager.CountTextCallback countCallback) {
        this.countCallback = countCallback;
    }

    public void setFolderCountCallback(FolderCountManager.FolderCountTextCallback countCallback) {
        this.folderCountCallback = countCallback;
    }

    public FolderCountManager.FolderCountTextCallback getFolderCountCallback() {
        return folderCountCallback;
    }


    public TaskInfo(int baseTaskType) {
        mBaseTaskType = baseTaskType;
    }

    public TaskInfo(List<FileInfo> fileInfo) {
        mSourceFileList = fileInfo;
    }

    public TaskInfo(FileManagerApplication application) {
        mApplication = application;
    }

    public TaskInfo(FileManagerApplication application, OperationEventListener listener, int baseTaskType) {
        mApplication = application;
        mListener = listener;
        mBaseTaskType = baseTaskType;
    }

    public AsyncTask getStorageTask() {
        return mStorageTask;
    }

    public void setStorageTask(AsyncTask mStorageTask) {
        this.mStorageTask = mStorageTask;
    }

    public BaseAsyncTask getTask() {
        return mTask;
    }

    public void setTask(BaseAsyncTask mTask) {
        this.mTask = mTask;
    }

    public long getCreateTaskTime() {
        return createTaskTime;
    }

    public void setCreateTaskTime(long createTaskTime) {
        this.createTaskTime = createTaskTime;
    }

    public String getTitleStr() {
        return mTitleStr;
    }

    public void setTitleStr(String titleStr) {
        this.mTitleStr = titleStr;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long mFileSize) {
        this.mFileSize = mFileSize;
    }

    public long getAllSize() {
        return mAllSize;
    }

    public void setAllSize(long mAllSize) {
        this.mAllSize = mAllSize;
    }

    public int getRefreshMode() {
        return mRefreshMode;
    }

    public void setRefreshMode(int mRefreshMode) {
        this.mRefreshMode = mRefreshMode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public ProgressInfo getProgressInfo() {
        return mProgressInfo;
    }

    public void setProgressInfo(ProgressInfo mProgressInfo) {
        this.mProgressInfo = mProgressInfo;
    }

    public TextView getStorageSize() {
        return mStorageSize;
    }

    public void setStorageSize(TextView mStorageSize) {
        this.mStorageSize = mStorageSize;
    }

    public TextView getStoragePercent() {
        return mStoragePercent;
    }

    public void setStoragePercent(TextView mStoragePercent) {
        this.mStoragePercent = mStoragePercent;
    }

    public ProgressBar getStorageProgress() {
        return mStorageProgress;
    }

    public void setStorageProgress(ProgressBar mStorageProgress) {
        this.mStorageProgress = mStorageProgress;
    }

    public List<String> getDesPathList() {
        return mDesPathList;
    }

    public void setDesPathList(List<String> mDesPathList) {
        this.mDesPathList = mDesPathList;
    }

    private List<String> mDesPathList;

    public int getCategoryIndex() {
        return mCategoryIndex;
    }

    public void setCategoryIndex(int mCategoryIndex) {
        this.mCategoryIndex = mCategoryIndex;
    }

    public int getDrmType() {
        return mDrmType;
    }

    public void setDrmType(int mDrmType) {
        this.mDrmType = mDrmType;
    }

    public boolean isShowDir() {
        return isShowDir;
    }

    public void setShowDir(boolean showDir) {
        isShowDir = showDir;
    }

    public String getSearchContent() {
        return mSearchContent;
    }

    public void setSearchContent(String mSearchContent) {
        this.mSearchContent = mSearchContent;
    }

    public FileInfo getSrcFile() {
        return mSrcFile;
    }

    public void setSrcFile(FileInfo mSrcFile) {
        this.mSrcFile = mSrcFile;
    }

    public FileInfo getDstFile() {
        return mDstFile;
    }

    public void setDstFile(FileInfo mDstFile) {
        this.mDstFile = mDstFile;
    }

    public int getAdapterMode() {
        return mAdapterMode;
    }

    public void setAdapterMode(int mAdapterMode) {
        this.mAdapterMode = mAdapterMode;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public void setResultCode(int mResultCode) {
        this.mResultCode = mResultCode;
    }

    public FileInfoManager getFileInfoManager() {
        return mApplication.mFileInfoManager;
    }

    public OperationEventListener getListener() {
        return mListener;
    }

    public void setListener(OperationEventListener listener) {
        mListener = listener;
    }

    public int getBaseTaskHashcode() {
        return mBaseTaskHashcode;
    }

    public void setBaseTaskHashcode(int mBaseTaskHashcode) {
        this.mBaseTaskHashcode = mBaseTaskHashcode;
    }

    public FileManagerApplication getApplication() {
        return mApplication;
    }

    public void setApplication(FileManagerApplication mApplication) {
        this.mApplication = mApplication;
    }

    public int getBaseTaskType() {
        return mBaseTaskType;
    }

    public void setBaseTaskType(int baseTaskType) {
        mBaseTaskType = baseTaskType;
    }

    public String getDestPath() {
        return mDestPath;
    }

    public void setDestPath(String destPath) {
        mDestPath = destPath;
    }

    public List<FileInfo> getSourceFileList() {
        return mSourceFileList;
    }

    public void setSourceFileList(List<FileInfo> sourceFileList) {
        mSourceFileList = sourceFileList;
    }

    public String getSrcPath() {
        return mSrcPath;
    }

    public void setSrcPath(String srcPath) {
        mSrcPath = srcPath;
    }

    public int getFileFilter() {
        return mFileFilter;
    }

    public void setFileFilter(int mFileFilter) {
        this.mFileFilter = mFileFilter;
    }



}
