package com.jrdcom.filemanager.manager;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class CategoryManager {
    public static int mCurrentMode=CommonIdentity.CATEGORY_MODE;
    public static int mCurrentCagegory = -1;
    public static int mLastCagegory = -1;
    private HashMap<String, Long> mSizeMap = new HashMap<String, Long>();
    private static CategoryManager sInstance;
    private static BlockingQueue<Runnable> sPoolWorkQueue = null;
    public static FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    public static String mSizeStr;
    private static MountManager mountManager;
    public static Map<Integer,String> mCategoryItemMap;

    private CategoryManager() {
        mountManager = MountManager.getInstance();
    }

    public static CategoryManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CategoryManager();
        }
        if (mCategoryItemMap == null || mCategoryItemMap.size() == 0) {
            mCategoryItemMap = SharedPreferenceUtils.getCategoryCountInfo(context);
        }

        return sInstance;
    }

    //this is used a new thread pool to excute task, this can make the task run right now.
    private static final Executor EXECUTOR;

    static {
        int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        int CORE_POOL_SIZE = CPU_COUNT + 1;
        int MAXIMUM_POOL_SIZE = 128;
        ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                return new Thread(r, "AsyncTask " + this.mCount.getAndIncrement());
            }
        };
        sPoolWorkQueue = new LinkedBlockingQueue(128);

        EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1L, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    }

    public void clearTaskQueue() {
        sPoolWorkQueue.clear();
    }


    public void loadCategoryCountText(final CountTextCallback callback) {

        TaskInfo mCategoryCountTaskInfo = new TaskInfo(mApplication, null, CommonIdentity.CATEGORY_COUNT_TASK);
        mCategoryCountTaskInfo.setCountCallback(callback);
        mApplication.mFileInfoManager.addNewTask(mCategoryCountTaskInfo);

    }


    public void putMap(String key, long value) {
        synchronized (mSizeMap) {
            mSizeMap.put(key, value);
        }
    }

    public void clearMap() {
        synchronized (mSizeMap) {
            if(mSizeMap != null) {
                mSizeMap.clear();
            }
        }
    }

    public static int getCountFromPath(Context context, String path, int category) {
        File dir = new File(path);
        File[] files = null;
        int count = 0;


        //this can return listFiles very fast.
        File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
        if (dir.exists()) {
            files = dir0.listFiles();
            if (files != null) {
                int len = files.length;


                for (int i = 0; i < len; i++) {
                    if (!files[i].isDirectory() && (mApplication.isShowHidden || !files[i].getName().startsWith("."))) {
                        count++;
                    }
                }
            }
        }


        return count;
    }

    public static int getCountFromFiles(Context context, int category) {

        String[] rootPaths = new String[2];
        rootPaths[0] = mountManager.getPhonePath();
        rootPaths[1] = mountManager.getSDCardPath();
        int count = 0;
        for (String rootPath : rootPaths) {
            if (!TextUtils.isEmpty(rootPath)) {
                count += getCountFromPath(context, CategoryManager.getCategoryPath(rootPath, category), category);
            }
        }
        long size = 0;
        if (rootPaths[0] != null) {
            size = getFolderSize(new File(CategoryManager.getCategoryPath(rootPaths[0], category)));
        }
        if (rootPaths[1] != null) {
            size += getFolderSize(new File(CategoryManager.getCategoryPath(rootPaths[1], category)));
        }
        mSizeStr = FileUtils.sizeToString(context, size);
        return count;
    }


    public interface CountTextCallback {
        public void countTextCallback(TaskInfo mTaskInfo);
    }

    public static long getFolderSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (int i = 0; i < fileList.length; i++) {
                    if (!fileList[i].isDirectory()) {
                        size = size + fileList[i].length();
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }

    public static void setCurrentMode(int mode) {
        mCurrentMode = mode;
    }

    public static String getCategoryPath(String rootPath, int category) {
        String path = null;
        if (CommonIdentity.CATEGORY_DOWNLOAD == category) {
            if (rootPath != null) {
                path = rootPath + "/Download";
            }
        } else if (CommonIdentity.CATEGORY_BLUETOOTH == category) {
            if (rootPath != null) {
                path = rootPath + "/bluetooth";
            }
        }

        return path;
    }

    public static int getCategoryString(int category) {
        int str;
        switch (category) {
            case CommonIdentity.CATEGORY_RECENT:
                str = R.string.main_recents;
                break;
            case CommonIdentity.CATEGORY_APKS:
                str = R.string.main_installers;
                break;
            case CommonIdentity.CATEGORY_BLUETOOTH:
                str = R.string.category_bluetooth;
                break;
            case CommonIdentity.CATEGORY_DOCS:
                str = R.string.main_document;
                break;
            case CommonIdentity.CATEGORY_DOWNLOAD:
                str = R.string.category_download;
                break;
            case CommonIdentity.CATEGORY_MUSIC:
                str = R.string.category_audio;
                break;
            case CommonIdentity.CATEGORY_PICTURES:
                str = R.string.category_pictures;
                break;
            case CommonIdentity.CATEGORY_VEDIOS:
                str = R.string.category_vedios;
                break;
            case CommonIdentity.CATEGORY_ARCHIVES:
                str = R.string.category_archives;
                break;
            default:
                str = 0;
                break;

        }
        return str;
    }
}
