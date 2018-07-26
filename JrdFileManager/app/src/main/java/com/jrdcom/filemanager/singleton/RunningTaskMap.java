package com.jrdcom.filemanager.singleton;


import com.jrdcom.filemanager.task.BaseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 16-6-27.
 */
public class RunningTaskMap<K, T> extends HashMap {

    private RunningTaskMap() {
    }

    private static RunningTaskMap<Long, BaseAsyncTask> runSingle = new RunningTaskMap<Long, BaseAsyncTask>();

    public static RunningTaskMap<Long, BaseAsyncTask> getInstance() {
        return runSingle;
    }

    public static void addRunningTask(long createTime, BaseAsyncTask mAyncTask) {
        runSingle.put(createTime, mAyncTask);
    }

    public static void removeRunningTask(long createTime) {
        runSingle.remove(createTime);
    }

    public static void clearRunningTask() {
        runSingle.clear();
    }

    public static BaseAsyncTask getRunningTask(long createTime) {
        return (BaseAsyncTask)runSingle.get(createTime);
    }

    public static int getRunningTaskSize() {
        return runSingle.size();
    }

}
