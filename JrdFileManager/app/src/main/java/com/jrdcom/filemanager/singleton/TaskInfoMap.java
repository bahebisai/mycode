package com.jrdcom.filemanager.singleton;

import android.app.Notification;

import com.jrdcom.filemanager.utils.TaskInfo;

import java.util.HashMap;

/**
 * Created by user on 16-11-1.
 */
public class TaskInfoMap<K,T> extends HashMap {

    private TaskInfoMap() {
    }

    private static TaskInfoMap<Long, TaskInfo> runSingle = new TaskInfoMap<Long, TaskInfo>();

    public static TaskInfoMap<Long, TaskInfo> getInstance() {
        return runSingle;
    }

    public static void addtaskInfo(long createTime, TaskInfo taskInfo) {
        runSingle.put(createTime, taskInfo);
    }

    public static void removeTaskInfo(long createTime) {
        runSingle.remove(createTime);
    }

    public static void clearAllTaskInfo() {
        runSingle.clear();
    }

    public static TaskInfo getTaskInfo(long createTime) {
        return (TaskInfo)runSingle.get(createTime);
    }

    public static int getTaskInfoSize() {
        return runSingle.size();
    }
}
