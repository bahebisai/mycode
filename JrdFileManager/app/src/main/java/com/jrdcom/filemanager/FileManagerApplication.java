package com.jrdcom.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.singleton.DataContentObserver;
import com.jrdcom.filemanager.singleton.ResultTaskHandler;
import com.jrdcom.filemanager.singleton.RunningTaskMap;
import com.jrdcom.filemanager.singleton.WaittingTaskList;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileListCache;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.tcl.faext.FAExt;

public class FileManagerApplication extends Application {

    //The path to the current directory.
    public String mCurrentPath;
    // The current location: is the safe or file browser
    public int mCurrentLocation = CommonIdentity.FILE_MANAGER_LOCATIONE;
    // Progress bar mode
    public int mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
    // Task Create Time
    public int mCurrentStatus = CommonIdentity.FILE_STATUS_NORMAL;
    // File view mode
    public String mViewMode = CommonIdentity.LIST_MODE;
    // sort type
    public int mSortType = 1;
    public FileInfoManager mFileInfoManager;
    // running task list
    public RunningTaskMap mRunningTaskMap;
    // watting task list
    public WaittingTaskList mWaittingTaskList;
    public ResultTaskHandler mResultTaskHandler;
    // Application startup time.
    public long mAppStartTime = System.currentTimeMillis();
    // Whether is the portrait mode
    public boolean mPortraitOrientation;
    // Whether to display hidden files mode.
    public boolean isShowHidden = false;
    // Whether it is multi screen mode
    public boolean isInMultiWindowMode = false;
    //Whether the system supports DRM file
    public boolean isSysteSupportDrm = false;
    public AlertDialog mProgressDialog;
    public long cancelTaskTime;
    public boolean isMediaURI = false;
    public boolean isShareMediaURI = false;

    public static boolean isBuiltInStorage = false;

    // cache files, including path mode and category mode.
    public FileListCache mCache = new FileListCache();

    public DataContentObserver mDataContentObserver;

//    private final HashMap<String, FileManagerActivityInfo> mActivityMap =
//            new HashMap<String, FileManagerActivityInfo>();


    public int currentOperation = CommonIdentity.OTHER;
    public ActivityLifecycleCallbacks mAppLife;
    private static FileManagerApplication application;
    public NotificationManager mNotiManager;

    public FAExt mFAExt ;

    public static Application getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        mAppLife = PermissionUtil.getActivityLifecycleCallbacks(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        if (mAppLife != null) {
            registerActivityLifecycleCallbacks(mAppLife);
        }
        if (SharedPreferenceUtils.getPrefsViewBy(this) == null || SharedPreferenceUtils.getPrefsViewBy(this).equals("")) {
            SharedPreferenceUtils.changePrefViewBy(this, CommonIdentity.LIST_MODE);
        } else {
            mViewMode = SharedPreferenceUtils.getPrefsViewBy(this);
        }
        SharedPreferenceUtils.changePrefCurrTag(this, CommonIdentity.CATEGORY_TAG);
        SharedPreferenceUtils.changePrefsStatus(this,CommonIdentity.FILE_STATUS_NORMAL);
        isShowHidden = SharedPreferenceUtils.isShowHidden(this);
        mFileInfoManager = new FileInfoManager(application);
        mRunningTaskMap = RunningTaskMap.getInstance();
        mWaittingTaskList = WaittingTaskList.getInstance();
        mAppStartTime = System.currentTimeMillis() / 1000l;
        mDataContentObserver = DataContentObserver.getInstance();
        isMediaURI = CommonUtils.isMediaURI(getApplicationContext());
        isShareMediaURI = CommonUtils.isShareMediaURI(getApplicationContext());
        mFAExt = FAExt.getInstance(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mAppLife != null) {
            unregisterActivityLifecycleCallbacks(mAppLife);

        }
        if(mCache != null){
            mCache.clearCache();
        }
        RunningTaskMap.clearRunningTask();
        WaittingTaskList.clearWaittingTask();
    }

    public void setDefaultSortBy(int index) {
        mSortType = index;
    }


    public ResultTaskHandler getAppHandler() {
        return mResultTaskHandler;
    }

    public void setAppHandler(ResultTaskHandler resultTaskHandler) {
        mResultTaskHandler = resultTaskHandler;
    }

//    public FileInfoManager initFileInfoManager(Activity a) {
//        FileManagerActivityInfo activityInfo = mActivityMap.get(a.getClass().getName());
//        if (activityInfo == null) {
//            activityInfo = new FileManagerActivityInfo();
//            activityInfo.setFileInfoManager(new FileInfoManager(a));
//            mActivityMap.put(a.getClass().getName(), activityInfo);
//        }
//        return activityInfo.getFileInfoManager();
//    }
//    public class FileManagerActivityInfo {
//        private FileInfoManager mActivityFileInfoManager = null;
//
//        public void setFileInfoManager(FileInfoManager fileInfoManager) {
//            this.mActivityFileInfoManager = fileInfoManager;
//        }
//
//        FileInfoManager getFileInfoManager() {
//            mFileInfoManager = mActivityFileInfoManager;
//            return  mFileInfoManager;
//
//        }
//    }

}