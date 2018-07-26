package com.jrdcom.filemanager.task;

import android.content.Context;
import android.os.AsyncTask;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.manager.MediaStoreHelper;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.MultiMediaStoreHelper;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.FileListCache;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tct.util.privacymode.TctPrivacyModeHelper;

public abstract class BaseAsyncTask extends AsyncTask<Void, ProgressInfo, Object> {

    protected OperationEventListener mListener;
    protected FileInfoManager mFileInfoManager;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 5 + 1; // MODIFIED by Chuanzhi.Shao, 2017-07-20,BUG-5080045
    private static final int KEEP_ALIVE = 1;
    protected long mStartOperationTime;
    protected Context mContext;
    protected FileManagerApplication mApplication;
    protected MediaStoreHelper mMediaProviderHelper;
    protected MultiMediaStoreHelper.DeleteMediaStoreHelper deleteMediaStoreHelper;
    protected MultiMediaStoreHelper.CopyMediaStoreHelper copyMediaStoreHelper;
    protected MountManager mMountManager;
    protected boolean mCancelled;
    protected PrivateModeManager mPrivateModeManager;
    protected TctPrivacyModeHelper mPrivateModeHelper;
    protected long taskTime;

    public long getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(long taskTime) {
        this.taskTime = taskTime;
    }

    public void setCancel(boolean cancel) {
        mCancelled = cancel;
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    public static final Executor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                    TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory, new ThreadPoolExecutor.DiscardOldestPolicy());
    private TaskInfo mResultTaskInfo;
    private BaseAsyncTask task;

    public BaseAsyncTask getTask() {
        return task;
    }

    public void setTask(BaseAsyncTask task) {
        this.task = task;
    }

    public TaskInfo getmResultTaskInfo() {
        return mResultTaskInfo;
    }
    /**
     * Constructor of BaseAsyncTask
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *                        information of files in FileManager.
     * @param listener        a instance of OperationEventListener, which is a
     *                        interface doing things before/in/after the task.
     */
    public BaseAsyncTask(TaskInfo mTaskInfo) {
        if (mTaskInfo.getFileInfoManager() == null) {
            throw new IllegalArgumentException();
        }
        mResultTaskInfo = mTaskInfo;
        mContext = mTaskInfo.getApplication();
        mApplication = mTaskInfo.getApplication();
        mFileInfoManager = mApplication.mFileInfoManager;
        mListener = mTaskInfo.getListener();
        mMediaProviderHelper = new MediaStoreHelper(mContext);
        deleteMediaStoreHelper = new MultiMediaStoreHelper.DeleteMediaStoreHelper(mMediaProviderHelper);
        copyMediaStoreHelper = new MultiMediaStoreHelper.CopyMediaStoreHelper(mMediaProviderHelper);
        mPrivateModeManager = new PrivateModeManager(mApplication);
        mPrivateModeHelper = mPrivateModeManager.getInstance();
        mMountManager = MountManager.getInstance();
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            mListener.onTaskPrepare();
        }
    }


    protected void onPostExecute(TaskInfo mTaskResultInfo) {
        if (mListener != null) {
            mListener.onTaskResult(mTaskResultInfo);
            mListener = null;
        }
    }

    @Override
    protected void onCancelled() {
        if (mListener != null) {
            mResultTaskInfo.setResultCode(OperationEventListener.ERROR_CODE_USER_CANCEL);
            mListener.onTaskResult(mResultTaskInfo);
            mListener = null;
        }
    }

    @Override
    protected void onProgressUpdate(ProgressInfo... values) {
        if (mListener != null && values != null && values[0] != null) {
            mListener.onTaskProgress(values[0]);
        }
    }

    /**
     * This method remove listener from task. Set listener associate with task
     * to be null.
     */
    protected void removeListener() {
        if (mListener != null) {
            mListener = null;
        }
    }

    /**
     * This method set mListener with certain listener.
     *
     * @param listener the certain listener, which will be set to be mListener.
     */
    public void setListener(OperationEventListener listener) {
        mListener = listener;
    }


    public boolean needUpdate() {
        long operationTime = System.currentTimeMillis() - mStartOperationTime;
        if (operationTime > CommonIdentity.NEED_UPDATE_TIME) {
            mStartOperationTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void cancel() {
        setCancel(true);
        cancel(true);
    }


}
