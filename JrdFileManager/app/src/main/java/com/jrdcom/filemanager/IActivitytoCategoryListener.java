package com.jrdcom.filemanager;

public interface IActivitytoCategoryListener {
    public void refreshCategory();
    public void onScannerStarted();
    public void onScannerFinished();
    public void disableCategoryEvent(boolean disable);
    public void dismissSafeDialog();
    public void onChangeMainlayout();
}
