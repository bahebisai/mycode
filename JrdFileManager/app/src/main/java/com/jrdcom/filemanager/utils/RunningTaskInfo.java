package com.jrdcom.filemanager.utils;

/**
 * Created by user on 16-8-2.
 */
public class RunningTaskInfo {

    private Long prgressBarIndex;
    private int max;
    private int progress;
    private String dialogTitle;

    public String getDialogTitle() {
        return dialogTitle;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }
    public RunningTaskInfo(long mTaskCreateTime){
        prgressBarIndex = mTaskCreateTime;
    }

    public RunningTaskInfo(long mPrgressBarIndex,int mMax){
        prgressBarIndex = mPrgressBarIndex;
        max = mMax;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public long getPrgressBarIndex() {
        return prgressBarIndex;
    }

    public void setPrgressBarIndex(long prgressBarIndex) {
        this.prgressBarIndex = prgressBarIndex;
    }
}
