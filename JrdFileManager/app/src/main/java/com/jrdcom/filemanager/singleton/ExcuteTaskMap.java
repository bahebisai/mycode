package com.jrdcom.filemanager.singleton;


import com.jrdcom.filemanager.utils.RunningTaskInfo;

import java.util.HashMap;

/**
 * Created by user on 16-6-28.
 */
public class ExcuteTaskMap<K,T> extends HashMap {

    private ExcuteTaskMap() {
    }

    private static ExcuteTaskMap<Long, RunningTaskInfo> finishSingle = new ExcuteTaskMap<Long, RunningTaskInfo>();

    public static ExcuteTaskMap<Long,RunningTaskInfo> getInstance() {
        return finishSingle;
    }

    public static void addFinishTask(long createtime, RunningTaskInfo info) {
        finishSingle.put(createtime,info);
    }

    public static void removeFinishTask(long createtime) {
        finishSingle.remove(createtime);
    }

    public static void clearFinishTask() {
        finishSingle.clear();
    }

    public static int getFinishTaskSize() {
        return finishSingle.size();
    }

    public static RunningTaskInfo getFinishTask(long createTime) {
        RunningTaskInfo mRunningInfo = (RunningTaskInfo)finishSingle.get(createTime);
        return mRunningInfo;
    }
    public static boolean isExist(long createTime) {
        return finishSingle.containsKey(createTime);
    }
}
