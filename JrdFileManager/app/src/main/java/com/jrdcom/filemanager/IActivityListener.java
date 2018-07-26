package com.jrdcom.filemanager;

import android.app.FragmentManager;

import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.util.List;

public interface IActivityListener {


    public void clickEditBtn();

    public void clickNewFolderBtn();

    public void clickSearchBtn();

    public void clickGlobalSearchBtn();

    public void clickPasteBtn();

    public void clickDelteBtn(int mode);

    public void clickCopyBtn();

    public void clickCutBtn();

    public void clickShareBtn();

    public void clickRenameBtn(String searchMessage);

    public void clickDetailsBtn();

    public boolean refreshAdapter(String path, int category, int refreshMode, int locationMode, boolean isShow,boolean isCreateFolder);

    public void onBackPressed();

    public void onGlobalSearchBackPressed();

    public void clearAdapter();

    public void showNoSearchResults(boolean isShow, String args);

    public void showNoFolderResults(boolean isShow);

    public boolean checkIsSelectAll();

    public void unMountUpdate();

    public void showBeforeSearchList();

    public void clickSelectAllBtn();

    public int getAdapterSize();

    public void onConfiguarationChanged();

    public void onScannerStarted();

    public void onScannerFinished();

    public void closeItemMorePop();

    public void closeFloatMenu(boolean isCollapse);

    public boolean isItemMorePop();

    public void clearChecked();

    public void showNoFolderResultView(boolean b);

    public void switchToCopyView();

    public void clickMigrateBtn();

    public void clickShortcutBtn();

    public void clickShortcutToNormal();

    public void updateActionMode(int mode);

    public List<FileInfo> saveSelectedList();

    public void managerTaskResult(TaskInfo mTaskInfo);

    public void clickProgressBtn(int id);

    public void changeViewMode(String mode);

    public void clickNotificationBtn();

    public void showAnim(FragmentManager fragmentManager, int dirAnimLeft);

    public void clickCompressBtn(int mode,String mAchiveName);

    public void clickExtractBtn(int mode,String mFolderName);

    public void registerDataContentObserver();

    public void unRegisterDataContentObserver();

    public boolean isShowNoFolderView();

    public void showFloatMenu(int selectCount, boolean hasDirctory, boolean isHasZip);

    public void updateEditBarState();

    public List<FileInfo> getCheckedList();

    public void restoreCheckedList(List<FileInfo> mRestoreCheckedList);

    public void deleteFileResponse();

    public void clickAddPrivateMode();

    public void clickRemovePrivateMode();

    public void removePrivateMode();

    public void cancelProgressDialog();

    public void afreshItemMorePop(); // MODIFIED by Chuanzhi.Shao, 2017-07-04,BUG-4974642
}
