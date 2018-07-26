package com.jrdcom.filemanager.manager;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.singleton.RunningTaskMap;
import com.jrdcom.filemanager.singleton.WaittingTaskList;
import com.jrdcom.filemanager.task.BaseAsyncTask;
import com.jrdcom.filemanager.task.DetailInfoTask;
import com.jrdcom.filemanager.task.FileOperationTask;
import com.jrdcom.filemanager.task.FileOperationTask.CopyPasteFilesTask;
import com.jrdcom.filemanager.task.FileOperationTask.CreateFolderTask;
import com.jrdcom.filemanager.task.FileOperationTask.CutPasteFilesTask;
import com.jrdcom.filemanager.task.FileOperationTask.DeleteFilesTask;
import com.jrdcom.filemanager.task.FileOperationTask.RenameTask;
import com.jrdcom.filemanager.task.FolderCountTask;
import com.jrdcom.filemanager.task.ListFileTask;
import com.jrdcom.filemanager.task.SearchTask;
import com.jrdcom.filemanager.task.SetRootSizeTextTask;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileInfoManager {


    private final List<FileInfo> mAddFilesInfoList = new CopyOnWriteArrayList<FileInfo>();
    private final List<FileInfo> mRemoveFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mPasteFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mShowFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mSearchFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mSearchFilesInfoListTS = new ArrayList<FileInfo>();
    private final List<FileInfo> mCategoryFilesInfoList = new ArrayList<FileInfo>();
    private final List<FileInfo> mAddHideFilesInfoList = new ArrayList<FileInfo>();

    private FileInfo mSafeFileInfo;


    public List<FileInfo> mBeforeSearchList = new ArrayList<FileInfo>();
    private int mPasteOperation = CommonIdentity.FILE_FILTER_TYPE_UNKOWN;
    private String mLastAccessPath;
    protected long mModifiedTime = -1;
    protected int mCategory = -1;

    public FileManagerApplication mApplication;
    public Context mContext;

    public FileInfoManager(Context context) {
        mContext = context;
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    }

    public void clearAll() {
        mAddFilesInfoList.clear();
        mRemoveFilesInfoList.clear();
        mPasteFilesInfoList.clear();
        mShowFilesInfoList.clear();
        mSearchFilesInfoList.clear();
        mCategoryFilesInfoList.clear();
        mAddHideFilesInfoList.clear();
        mBeforeSearchList.clear();
        mSafeFileInfo = null;
    }

    public void clearShowFiles() {
        mShowFilesInfoList.clear();
        mCategoryFilesInfoList.clear();
    }

    /**
     * mContext method updates mPasteFilesInfoList.
     *
     * @param pasteType previous operation before paste, copy or cut
     * @param fileInfos list of copied (or cut) files
     */
    public void savePasteList(int category, int pasteType, List<FileInfo> fileInfos) {
        mCategory = category;
        mPasteOperation = pasteType;
        mPasteFilesInfoList.clear();
        mPasteFilesInfoList.addAll(fileInfos);
    }

    public int getCategory() {
        return mCategory;
    }

    /**
     * mContext method checks weather current path is modified.
     *
     * @param path certain path to be checked
     * @return true for modified, and false for not modified
     */
    public boolean isPathModified(String path) {
        if (!TextUtils.isEmpty(path) && !path.equals(mLastAccessPath)) {
            return true;
        }
        if (mLastAccessPath != null
                && mModifiedTime != (new File(mLastAccessPath)).lastModified()) {
            return true;
        }
        return false;
    }

    /**
     * mContext method gets a ArrayList of FileInfo with content of
     * mPasteFilesInfoList.
     *
     * @return list of files, which paste operation involve
     */
    public List<FileInfo> getPasteList() {
        return new ArrayList<FileInfo>(mPasteFilesInfoList);
    }

    /**
     * mContext method gets previous operation before paste, copy or cut
     *
     * @return copy or cut
     */
    public int getPasteType() {
        return mPasteOperation;
    }

    /**
     * mContext method add file to mAddFilesInfoList
     *
     * @param fileInfo information of certain file
     */
    public void addItem(FileInfo dest) {
        mAddFilesInfoList.add(dest);
    }


    /**
     * mContext method add file list to mAddFilesInfoList
     *
     * @param destList information of file list
     */
    public void addAllItem(List<FileInfo> destList) {
        mAddFilesInfoList.addAll(destList);
    }


    public void addItem(FileInfo dest, boolean firstItem) {
        if (firstItem && mAddFilesInfoList.size() > 0) {
            mAddFilesInfoList.clear();
            mAddFilesInfoList.add(dest);
        } else {
            mAddFilesInfoList.add(dest);
        }
    }

    public List<FileInfo> getAddFilesInfoList() {
        return mAddFilesInfoList;
    }


    public void addHideItem(FileInfo fileInfo) {
        mAddHideFilesInfoList.add(fileInfo);
    }


    public void addAllHideItem(List<FileInfo> fileList) {
        mAddHideFilesInfoList.addAll(fileList);
    }


    public void clearHideItem() {
        mAddHideFilesInfoList.clear();
    }

    public void removeHideItem(FileInfo fileInfo) {
        mAddHideFilesInfoList.remove(fileInfo);
    }

    public void removeHideItemList(List<FileInfo> fileInfoList) {
        mAddFilesInfoList.removeAll(fileInfoList);
    }

    public List<FileInfo> getHideItemList() {
        return mAddHideFilesInfoList;
    }

    /**
     * mContext method adds file to mRemoveFilesInfoList
     *
     * @param fileInfo information of certain file
     */
    public void removeItem(FileInfo fileInfo) {
        mRemoveFilesInfoList.add(fileInfo);
        mSearchFilesInfoList.remove(fileInfo);
        mCategoryFilesInfoList.remove(fileInfo);
        mBeforeSearchList.remove(fileInfo);
        mPasteFilesInfoList.remove(fileInfo);
    }

    /**
     * mContext method updates all file lists according to parameter path and
     * sortType, and called in onTaskResult() of HeavyOperationListener, which
     * corresponds to operations like delete, copyPaste, cutPaste and so on.
     *
     * @param currentPath current path
     * @param sortType    sort type, which determine files' list sequence
     */
    public void updateFileInfoList(String currentPath, int sortType) {
        try {
            mLastAccessPath = currentPath;
            if (mLastAccessPath != null) {
                mModifiedTime = (new File(mLastAccessPath)).lastModified();
            }
            int len = mAddFilesInfoList.size();
            for (int i = 0; i < len; i++) {
                FileInfo fileInfo = mAddFilesInfoList.get(i);
                if (fileInfo.getFileParentPath().equals(mLastAccessPath)) {
                    mShowFilesInfoList.add(fileInfo);
                }
            }


            for (int i = 0; i < mRemoveFilesInfoList.size(); i++) {
                mShowFilesInfoList.remove(mRemoveFilesInfoList.get(i));
            }


            mPasteFilesInfoList.removeAll(mRemoveFilesInfoList);
            mAddFilesInfoList.clear();
            mRemoveFilesInfoList.clear();
            sort(sortType);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mContext method adds one file to mShowFilesInfoList, and called in
     * onTaskResult() of HeavyOperationListener, which corresponds to operations
     * like rename, createFolder and so on.
     *
     * @param path     current path
     * @param sortType sort type, which determine files' list sequence
     * @return information of file, which will be set selected after UI updated.
     * null if size of mAddFilesInfoList is zero
     */
    public FileInfo updateOneFileInfoList(String path, int sortType) {
        FileInfo fileInfo = null;
        mLastAccessPath = path;
        mModifiedTime = (new File(mLastAccessPath)).lastModified();
        if (mAddFilesInfoList.size() > 0) {
            fileInfo = mAddFilesInfoList.get(0);
            if (fileInfo.getFileParentPath().equals(mLastAccessPath)) {
                mShowFilesInfoList.add(fileInfo);
            }
        }
        mShowFilesInfoList.removeAll(mRemoveFilesInfoList);
        mPasteFilesInfoList.removeAll(mRemoveFilesInfoList);
        mAddFilesInfoList.clear();
        mRemoveFilesInfoList.clear();
        sort(sortType);

        return fileInfo;
    }

    /**
     * mContext method adds one file to mShowFilesInfoList in the category list, and called in
     * onTaskResult() of HeavyOperationListener, which corresponds to operations
     * like rename.
     *
     * @param sortType sort type, which determine files' list sequence
     * @return information of file, which will be set selected after UI updated.
     * null if size of mAddFilesInfoList is zero
     */
    public FileInfo updateOneCategoryFileInfoList(int sortType) {
        FileInfo fileInfo = null;
        if (mAddFilesInfoList.size() > 0) {
            fileInfo = mAddFilesInfoList.get(0);
            mShowFilesInfoList.add(fileInfo);
        }
        mShowFilesInfoList.removeAll(mRemoveFilesInfoList);
        mPasteFilesInfoList.removeAll(mRemoveFilesInfoList);
        mAddFilesInfoList.clear();
        mRemoveFilesInfoList.clear();
        sort(sortType);

        return fileInfo;
    }

    /**
     * mContext method adds mAddFilesInfoList to loadFileInfoList
     *
     * @param path     the current path to list files
     * @param sortType sort type, which determine files' sequence
     */
    public void loadFileInfoList(String path, int sortType) {
        if (!TextUtils.isEmpty(path)) {
            mShowFilesInfoList.clear();
            mLastAccessPath = path;
            mModifiedTime = (new File(mLastAccessPath)).lastModified();
            for (FileInfo fileInfo : mAddFilesInfoList) {
                if (mLastAccessPath.equals(fileInfo.getFileParentPath())
                        || MountManager.getInstance().isMountPoint(
                        fileInfo.getFileAbsolutePath())) {
                    mShowFilesInfoList.add(fileInfo);
                }
            }
            mAddFilesInfoList.clear();
            if (!MountManager.getInstance().isRootPath(path)) {
                sort(sortType);
            }
        }
    }

    /**
     * mContext method adds mAddFilesInfoList to loadFileInfoList
     *
     * @param path     the current path to list files
     * @param sortType sort type, which determine files' sequence
     */
    public void loadFileInfoList(String path, List<FileInfo> mInfoList, int sortType) {
        if (mInfoList != null && !TextUtils.isEmpty(path)) {
            mShowFilesInfoList.clear();
            mLastAccessPath = path;
            mModifiedTime = (new File(mLastAccessPath)).lastModified();
            mShowFilesInfoList.addAll(mInfoList);
            if (!MountManager.getInstance().isRootPath(path)) {
                sort(sortType);
            }
        }
    }

    /**
     * mContext method adds mAddFilesInfoList to loadFileInfoList
     *
     * @param path     the current path to list files
     * @param sortType sort type, which determine files' sequence
     */
    public void loadFileInfoList(int category, List<FileInfo> mInfoList, int sortType) {
        if (mInfoList != null) {
            mShowFilesInfoList.clear();
            mShowFilesInfoList.addAll(mInfoList);
            sort(sortType);
        }
    }

    /**
     * mContext method adds list to mAddFilesInfoList
     *
     * @param fileInfoList list of files
     */
    public void addItemList(List<FileInfo> fileInfoList) {
        mAddFilesInfoList.clear();
        mAddFilesInfoList.addAll(fileInfoList);
    }

    /**
     * mContext method removes all item in mAddFilesInfoList
     */
    public void removeAllItem() {
        mAddFilesInfoList.clear();
    }

    /**
     * mContext method checks weather certain item is included in paste list
     *
     * @param currentItem certain item, which needs to be checked
     * @return status of weather the item is included in paste list
     */
    public boolean isPasteItem(FileInfo currentItem) {
        return mPasteFilesInfoList.contains(currentItem);
    }

    /**
     * mContext method gets count of files in PasteFileInfoList, which need to paste
     *
     * @return number of files, which need to be pasted
     */
    public int getPasteCount() {
        return mPasteFilesInfoList.size();
    }

    /**
     * mContext method clears pasteList, which stores files need to paste(after copy
     * , or cut)
     */
    public void clearPasteList() {
        mPasteFilesInfoList.clear();
        mCategory = -1;
        mPasteOperation = CommonIdentity.FILE_FILTER_TYPE_UNKOWN;
    }

    /**
     * mContext method gets file list for show
     *
     * @return file list for show
     */
    public List<FileInfo> getShowFileList() {
        return mShowFilesInfoList;
    }

    /**
     * mContext method saves file list before search operation
     */
    public void saveListBeforeSearch() {
        mBeforeSearchList.clear();
        mBeforeSearchList.addAll(mShowFilesInfoList);
    }

    /**
     * mContext method gets file list that saved before search
     *
     * @return file list for show
     */
    public List<FileInfo> getBeforeSearchList() {
        return mBeforeSearchList;
    }

    /**
     * mContext method sorts files with given sort type
     *
     * @param sortType sort type
     */
    public void sort(int sortType) {
        try {
            Collections.sort(mShowFilesInfoList, FileInfoComparator.getInstance(sortType));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mContext method updates search list, which stores search result
     *
     * @param sortType sort type
     */
    public void updateSearchList(int sortType) {
        mSearchFilesInfoList.clear();
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mSearchFilesInfoListTS);
        mSearchFilesInfoList.addAll(mSearchFilesInfoListTS);
        mSearchFilesInfoListTS.clear();
        sort(sortType);
    }

    /**
     * mContext method gets the the list of search list
     *
     * @return
     */
    public List<FileInfo> getSearchFileList() {
        return mSearchFilesInfoList;
    }

    /**
     * add the search result list to TS(temporary storage) list
     * @param fileInfoList
     */
    public void addToSearchResultList(List<FileInfo> fileInfoList) {
        mSearchFilesInfoListTS.clear();
        mSearchFilesInfoListTS.addAll(fileInfoList);
    }

    /**
     * mContext method gets the number of the search result
     *
     * @return the number of the search result
     */
    public int getSearchItemsCount() {
        return mSearchFilesInfoList.size();
    }

    /**
     * mContext method updates category list
     *
     * @param sortType
     */
    public void updateCategoryList(int sortType) {
        mLastAccessPath = null;
        mCategoryFilesInfoList.clear();
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mAddFilesInfoList);
        mCategoryFilesInfoList.addAll(mAddFilesInfoList);
        mAddFilesInfoList.clear();
        sort(sortType);
    }

    public void updatingCategoryList(int sortType) {
        mShowFilesInfoList.addAll(mAddFilesInfoList);
        mCategoryFilesInfoList.addAll(mAddFilesInfoList);
        mAddFilesInfoList.clear();
        sort(sortType);
    }

    /**
     * mContext method gets the list of category list
     *
     * @return
     */
    public List<FileInfo> getCategoryFileList() {
        return mCategoryFilesInfoList;
    }

    /**
     * mContext method show the category result list
     *
     * @param sortType sort type
     */
    public void showCategoryResultView(int sortType) {
        mShowFilesInfoList.clear();
        mShowFilesInfoList.addAll(mCategoryFilesInfoList);
        sort(sortType);
    }

    /**
     * mContext method gets the number of the category list
     *
     * @return
     */
    public int getCategoryItemsCount() {
        return mCategoryFilesInfoList.size();
    }

    public FileInfo getSafeFileInfo() {
        return mSafeFileInfo;
    }

    public void setSafeFileInfo(FileInfo safeFileInfo) {
        mSafeFileInfo = safeFileInfo;
    }

//    private final HashMap<String, FileManagerActivityInfo> mActivityMap =
//            new HashMap<String, FileManagerActivityInfo>();

    /**
     * mContext method checks that weather the service is busy or not, which means
     * id any task exist for certain activity
     *
     * @param activityName name of activity, which will be checked
     * @return true for busy, false for not busy
     */
    public boolean isBusy(String activityName) {
//        FileManagerActivityInfo activityInfo = mActivityMap.get(mContext.getClass().getName());
//        if (activityInfo == null) {
//            return false;
//        }
//        BaseAsyncTask task = activityInfo.getTask();
//        //Log.i(TAG, "Task->" + task + "            is busy");
//        if (task != null
//                && !task.isCancelled()
//                && (task.getStatus() == AsyncTask.Status.PENDING || task
//                .getStatus() == AsyncTask.Status.RUNNING)) {
//            return true;
//        }
        return false;
    }

//    private FileManagerActivityInfo getActivityInfo(String activityName) {
//        FileManagerActivityInfo activityInfo = mActivityMap.get(activityName);
//        if (activityInfo == null) {
//            //Log.d(TAG, "mContext activity not init in Service");
//            return null;
//        }
//        return null;
//    }

    /**
     * mContext method sets list filter, which which type of items will be listed in
     * listView
     *
     * @param type         type of list filter
     * @param activityName name of activity, which operations attached to
     */
//    public void setListType(int type, String activityName) {
//        if (getActivityInfo(activityName) != null) {
//            getActivityInfo(activityName).setFilterType(type);
//        }
//    }
    public void addNewTask(TaskInfo mBaseTaskInfo) {
        try{ // MODIFIED by Chuanzhi.Shao, 2017-07-20,BUG-5080045
        FileInfoManager mFileInfoManager = null;
        BaseAsyncTask task = null;
        SetRootSizeTextTask mStorageTask = null;
        int taskType = mBaseTaskInfo.getBaseTaskType();
        int filterType = 0;
        if(CommonUtils.isDialogProgressTask(taskType)){
            if (RunningTaskMap.getRunningTaskSize() >= CommonIdentity.RUNNING_TASK_MAX_LIMITE
                    && WaittingTaskList.getWaittingTaskSize() >= CommonIdentity.WAITTING_TASK_MAX_LIMITE) {
                mBaseTaskInfo.setResultCode(OperationEventListener.ERROR_CODE_EXCEEDED_MAX_TASK);
                if (mBaseTaskInfo != null && mBaseTaskInfo.getListener() != null) {
                    mBaseTaskInfo.getListener().onTaskResult(mBaseTaskInfo);
                }
                return;
            }
        }
        switch (taskType) {
            case CommonIdentity.CREATE_FOLDER_TASK:
                task = new CreateFolderTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.PASTE_COPY_TASK:
            case CommonIdentity.PASTE_CUT_TASK:
                List<FileInfo> mSourceList = mBaseTaskInfo.getSourceFileList();
                String mDestPath = mBaseTaskInfo.getDestPath();
                OperationEventListener listener = mBaseTaskInfo.getListener();

                if (filterPasteList(mSourceList, mDestPath) > 0) {
                    mBaseTaskInfo.setResultCode(OperationEventListener.ERROR_CODE_PASTE_TO_SUB);
                    listener.onTaskResult(mBaseTaskInfo);
                }
                if (mSourceList.size() > 0) {
                    switch (mBaseTaskInfo.getBaseTaskType()) {
                        case CommonIdentity.PASTE_CUT_TASK:
                            if (isCutSamePath(mSourceList, mDestPath)) {
                                mBaseTaskInfo.setResultCode(OperationEventListener.ERROR_CODE_CUT_SAME_PATH);
                                listener.onTaskResult(mBaseTaskInfo);
                                return;
                            }
                            task = new CutPasteFilesTask(mBaseTaskInfo);
                            task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                            task.setTask(task);
                            break;
                        case CommonIdentity.PASTE_COPY_TASK:
                            task = new CopyPasteFilesTask(mBaseTaskInfo);
                            task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                            task.setTask(task);
                            break;
                        default:
                            mBaseTaskInfo.setResultCode(OperationEventListener.ERROR_CODE_UNKOWN);
                            listener.onTaskResult(mBaseTaskInfo);
                            return;
                    }
                }
                break;
            case CommonIdentity.NORMAL_DELETE_TASK:
                task = new DeleteFilesTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                break;
            case CommonIdentity.RENAME_FILE_TASK:
                filterType = SharedPreferenceUtils.getPrefsShowHidenFile(mApplication);
                mBaseTaskInfo.setFileFilter(filterType);
                task = new RenameTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.LIST_INFO_TASK:
                mBaseTaskInfo.setFileFilter(SharedPreferenceUtils.getPrefsShowHidenFile(mApplication));
                task = new ListFileTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.SEARCH_INFO_TASK:
                task = new SearchTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.DETAIL_FILE_TASK:
                task = new DetailInfoTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.STORAGE_SPACE_TASK:
                mStorageTask = new SetRootSizeTextTask(mBaseTaskInfo);
                mStorageTask.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                mStorageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.FILE_COMPRESSION_TASK:
                task = new FileOperationTask.CompressFileTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                break;
            case CommonIdentity.FILE_UNCOMPRESSION_TASK:
                task = new FileOperationTask.ExtractFileTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                break;
            case CommonIdentity.CATEGORY_COUNT_TASK:
                task = new FileOperationTask.CategoryCountTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.FOLDER_COUNT_TASK:
                task = FolderCountTask.getInstance(mBaseTaskInfo,true);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.SAFE_CATEGORY_COUNT_TASK:
                task = new FileOperationTask.SafeCategoryCountTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            case CommonIdentity.ADD_PRIVATE_FILE_TASK:
                task = new FileOperationTask.AddPrivateModeFileTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                break;
            case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                task = new FileOperationTask.RemovePrivateModeFileTask(mBaseTaskInfo);
                task.setTaskTime(mBaseTaskInfo.getCreateTaskTime());
                task.setTask(task);
                break;
        }
        if(task == null){
            return;
        }
        if (RunningTaskMap.getRunningTaskSize() >= CommonIdentity.RUNNING_TASK_MAX_LIMITE) {
            WaittingTaskList.addWaittingningTask(task);
            mBaseTaskInfo.setResultCode(OperationEventListener.ERROR_CODE_ADD_WAITING_TASK);
            if (mBaseTaskInfo != null && mBaseTaskInfo.getListener() != null) {
                mBaseTaskInfo.getListener().onTaskResult(mBaseTaskInfo);
            }
        } else {
            RunningTaskMap.addRunningTask(mBaseTaskInfo.getCreateTaskTime(), task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-07-20,BUG-5080045*/
        }catch(Exception e){
            android.util.Log.e("AsyncTask","maximum pool size is exceed",e);
        }
        /* MODIFIED-END by Chuanzhi.Shao,BUG-5080045*/

    }

    public void wakeWaittingTask(TaskInfo mTaskInfo) {
        if (mTaskInfo != null) {
            RunningTaskMap.removeRunningTask(mTaskInfo.getCreateTaskTime());
            if (mApplication == null) {
                mApplication = mTaskInfo.getApplication();
            }
            if (mApplication.mNotiManager == null) {
                mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            mApplication.mNotiManager.cancel((int) mTaskInfo.getCreateTaskTime());
        }
        int runningSize = RunningTaskMap.getRunningTaskSize();
        int waittingSize = WaittingTaskList.getWaittingTaskSize();
        int addTaskCount = 0;
        if (waittingSize > 0) {
            try {
                if (runningSize < CommonIdentity.RUNNING_TASK_MAX_LIMITE) {
                    if (CommonIdentity.RUNNING_TASK_MAX_LIMITE - runningSize < waittingSize) {
                        addTaskCount = CommonIdentity.RUNNING_TASK_MAX_LIMITE - runningSize;
                    } else {
                        addTaskCount = waittingSize;
                    }
                    for (int i = 0; i < addTaskCount; i++) {
                        BaseAsyncTask task = WaittingTaskList.getWaittingTask(i);
                        RunningTaskMap.addRunningTask(task.getTaskTime(), task);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        WaittingTaskList.removeWaittingTask(task);
                    }
                }
            } catch(IllegalStateException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private int filterPasteList(List<FileInfo> fileInfoList, String destFolder) {

        int remove = 0;
        Iterator<FileInfo> iterator = fileInfoList.iterator();
        while (iterator.hasNext()) {
            FileInfo fileInfo = iterator.next();
            if (fileInfo.isDirectory()) {
                if ((destFolder + MountManager.SEPARATOR)
                        .startsWith(fileInfo.getFileAbsolutePath()
                                + MountManager.SEPARATOR)) {
                    iterator.remove();
                    remove++;
                }
            }
        }
        return remove;
    }

    private boolean isCutSamePath(List<FileInfo> fileInfoList, String dstFolder) {
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getFileParentPath().equals(dstFolder)) {
                return true;
            }
        }
        return false;
    }


    /**
     * This method cancel certain task
     *
     * @param activityName name of activity, which the task attached to
     */
    public void cancel(BaseAsyncTask task) {
        if (task != null) {
            task.cancel();
        }
    }
}
