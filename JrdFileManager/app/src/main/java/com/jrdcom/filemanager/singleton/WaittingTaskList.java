package com.jrdcom.filemanager.singleton;


import com.jrdcom.filemanager.task.BaseAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 16-6-27.
 */
public class WaittingTaskList<T> extends ArrayList {

    private WaittingTaskList() {
    }

    private static WaittingTaskList<BaseAsyncTask> waitSingle = new WaittingTaskList<BaseAsyncTask>();

    public static WaittingTaskList<BaseAsyncTask> getInstance() {
        return waitSingle;
    }

    public static void addWaittingningTask(BaseAsyncTask mAyncTask) {
        waitSingle.add(mAyncTask);
    }

    public static void removeWaittingTask(BaseAsyncTask mAyncTask) {
        waitSingle.remove(mAyncTask);
    }

    public static void clearWaittingTask() {
        waitSingle.clear();
    }

    public static int getWaittingTaskSize() {
        return waitSingle.size();
    }

    public static BaseAsyncTask getWaittingTask(int index) {
        return (BaseAsyncTask) waitSingle.get(index);
    }


}
