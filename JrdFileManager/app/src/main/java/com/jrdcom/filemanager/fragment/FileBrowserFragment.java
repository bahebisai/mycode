package com.jrdcom.filemanager.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.WifiDisplayStatus;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.IActivityListener;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FileBaseActivity;
import com.jrdcom.filemanager.adapter.FileShowAdapter;
import com.jrdcom.filemanager.dialog.AlertDialogFragment;
import com.jrdcom.filemanager.dialog.AlertDialogFragment.AlertDialogFragmentBuilder;
import com.jrdcom.filemanager.dialog.AlertDialogFragment.EditDialogFragmentBuilder;
import com.jrdcom.filemanager.dialog.AlertDialogFragment.EditTextDialogFragment;
import com.jrdcom.filemanager.dialog.AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener;
import com.jrdcom.filemanager.dialog.CommonDialogFragment;
import com.jrdcom.filemanager.dialog.DetailDialogFragment;
import com.jrdcom.filemanager.dialog.ProgressNotification;
import com.jrdcom.filemanager.dialog.ProgressPopupWindow;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.listener.HeavyOperationListener;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.FileInfoComparator;
import com.jrdcom.filemanager.manager.IconManager;
import com.jrdcom.filemanager.manager.MediaStoreHelper;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.MultiMediaStoreHelper;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.singleton.ExcuteTaskMap;
import com.jrdcom.filemanager.singleton.NotificationMap;
import com.jrdcom.filemanager.singleton.RunningTaskMap;
import com.jrdcom.filemanager.singleton.TaskInfoMap;
import com.jrdcom.filemanager.singleton.WaittingTaskList;
import com.jrdcom.filemanager.task.BaseAsyncTask;
import com.jrdcom.filemanager.task.ProgressInfo;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.RunningTaskInfo;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;
import com.jrdcom.filemanager.view.FloatingActionButton;
import com.jrdcom.filemanager.view.FloatingActionsMenu;
import com.jrdcom.filemanager.view.ToastHelper;
/* MODIFIED-BEGIN by caiminjie, 2017-09-12,BUG-5325137*/
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.app.PendingIntent;
/* MODIFIED-END by caiminjie,BUG-5325137*/

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class FileBrowserFragment extends Fragment implements FileShowAdapter.OnItemClickLitener, IActivityListener {

    private static final String TAG = FileBrowserFragment.class.getSimpleName();
    private boolean mSavedState;
    private boolean mSearchMode;
    protected LinearLayout mNoSearchView;
    protected LinearLayout mNoFolderView;
    protected TextView mNo_messageView;
    protected ImageView mNo_ImageView;
    protected TextView noSearchText;
    private ToastHelper mToastHelper;
    protected Activity mActivity;
    protected FileManagerApplication mApplication;
    private boolean isDataChanged = false;
    private DisplayManager mDisplayManager;
    private int clickcount;
    private long fir_time;
    private long sec_time;
    public static boolean isOpenSafeFile = false;
    private Notification mNotificationProgress;
    protected RecyclerView mRecyclerView;
    protected FileShowAdapter mAdapter;
    protected boolean mSelectAll = false;
    MediaStoreHelper mMediaProviderHelper;
    MultiMediaStoreHelper.DeleteMediaStoreHelper deleteMediaStoreHelper;
    public FloatingActionsMenu menuMultipleActions;
    protected FloatingActionButton setPrivate_btn;
    protected FloatingActionButton archives_btn;
    protected FloatingActionButton share_btn;
    protected FloatingActionButton delete_btn;
    protected FloatingActionButton copy_btn;
    protected FloatingActionButton cut_btn;
    protected FloatingActionButton extract_btn;
    protected FloatingActionButton paste_btn;

    public FloatingActionsMenu menuMultipleActionsLeft;
    protected FloatingActionButton setPrivate_btnLeft;
    protected FloatingActionButton archives_btnLeft;
    protected FloatingActionButton share_btnLeft;
    protected FloatingActionButton delete_btnLeft;
    protected FloatingActionButton copy_btnLeft;
    protected FloatingActionButton cut_btnLeft;
    protected FloatingActionButton extract_btnLeft;
    protected FloatingActionButton paste_btnLeft;
    //protected DataContentObserver mDataContentObserver;

    protected CommonDialogFragment mRemovePrivateDialog;
    protected CommonDialogFragment mDeleteDialog;
    protected CommonDialogFragment mExtractDialog;
    protected CommonDialogFragment mComPressDialog;
    protected EditTextDialogFragment renameDialogFragment;
    protected AlertDialogFragment renameExtensionDialogFragment;
    EditTextDialogFragment createFolderDialogFragment;
    private static Activity mRenameActivity;
    private String mCurrentSearchText;
    private String mLastSearchText;
    public TaskInfo mResultInfo;
    // Locate the folder information
    private static FileInfo mPositionInfo;
    //Click to return to whether the need for positioning.
    private static boolean isBackPosition;


    @Override
    public void clickProgressBtn(int id) {
        Iterator it = ExcuteTaskMap.getInstance().entrySet().iterator();
        Iterator runningit = RunningTaskMap.getInstance().entrySet().iterator();
        switch (id) {

            case CommonIdentity.CANCEL_ALL_TASK:
                try {
                    if (mApplication.mNotiManager == null) {
                        mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
                    }
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        long mTaskCreateTime = (long) entry.getKey();
                        RunningTaskInfo info = (RunningTaskInfo) entry.getValue();
                        BaseAsyncTask task = (BaseAsyncTask) RunningTaskMap.getRunningTask(mTaskCreateTime);
                        mApplication.mFileInfoManager.cancel(task);
                        mApplication.mNotiManager.cancel((int) mTaskCreateTime);
                        RunningTaskMap.removeRunningTask(mTaskCreateTime);
                        ExcuteTaskMap.removeFinishTask(mTaskCreateTime);
                    }
                    RunningTaskMap.clearRunningTask();
                    ExcuteTaskMap.clearFinishTask();
                    NotificationMap.clearAllNotification();
                    TaskInfoMap.clearAllTaskInfo();
                    mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                } finally {
                    try {
                        mApplication.mNotiManager.cancelAll();
                    } catch (Exception e){

                    }
                }
                break;

            case AlertDialog.BUTTON_NEGATIVE:
                cancelTask();
                break;
            case AlertDialog.BUTTON_POSITIVE:
                if (mApplication.mProgressDialog != null) {
                    mApplication.mProgressDialog.hide();
                    mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_NOTIFICATION_MODE;
                    createNotificationProgress();
                }
                break;
        }
    }

    private void cancelTask() {
        BaseAsyncTask task = (BaseAsyncTask) RunningTaskMap.getRunningTask(mApplication.cancelTaskTime);
        mApplication.mFileInfoManager.cancel(task);
        if (mApplication.mNotiManager == null) {
            mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mApplication.mNotiManager.cancel((int) mApplication.cancelTaskTime);
        RunningTaskMap.removeRunningTask(mApplication.cancelTaskTime);
        ExcuteTaskMap.removeFinishTask(mApplication.cancelTaskTime);
        NotificationMap.removeNotification(mApplication.cancelTaskTime);
        TaskInfoMap.removeTaskInfo(mApplication.cancelTaskTime);
    }

    @Override
    public void changeViewMode(String mode) {
        if (mApplication == null) {
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        }
        int column = CommonUtils.getGridColumn(mApplication);
        if (mode.equals(CommonIdentity.LIST_MODE)) {
            if (mRecyclerView != null) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                if (mAdapter != null) {
                    mRecyclerView.setAdapter(mAdapter);
                }
            }

        } else if (mode.equals(CommonIdentity.GRID_MODE)) {
            if (mRecyclerView != null) {
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), column));
                if (mAdapter != null) {
                    mAdapter.setViewMode(CommonIdentity.GRID_MODE);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        FileInfo selecteItemFileInfo = mAdapter.getItem(position);
        if (selecteItemFileInfo == null) {
            return;
        }
        File mSelectFile = selecteItemFileInfo.getFile();
        if (mSelectFile != null && !mSelectFile.exists()) {
            String error = getResources().getString(R.string.path_not_exists, selecteItemFileInfo.getFileName());
            CommonUtils.deleteCache(selecteItemFileInfo, CategoryManager.mCurrentCagegory, mApplication.mCache);
            if(mMediaProviderHelper!=null) {
                mMediaProviderHelper.scanPathforMediaStore(mSelectFile.getAbsolutePath());
            }
            if(mAdapter.getItemCount()==1 && CommonUtils.isCategoryMode()){
                mApplication.mFileInfoManager.clearShowFiles();
            }
            refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory,
                    CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
            mToastHelper.showToast(error);
            return;
        }
        if (mAdapter.isMode(CommonIdentity.FILE_STATUS_NORMAL) || mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)
                || mAdapter.isMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH) || mAdapter.isMode(CommonIdentity.FILE_COPY_NORMAL)) {
            if (mSelectFile != null && mSelectFile.isDirectory()) {
                if (mAdapter.isMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH) || mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)) {
                    CommonUtils.hideSoftInput(mActivity);
                    updateActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                }
                if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                    CategoryManager.mCurrentMode = CommonIdentity.PATH_MODE;
                    mApplication.mCurrentPath = selecteItemFileInfo.getFileAbsolutePath();
                    refreshPathBar();
                    mAdapter.clearList();
                    // load content when click in search mode
                    refreshAdapter(selecteItemFileInfo.getFileAbsolutePath(), -1, CommonIdentity.REFRESH_FILE_PATH_MODE,
                            CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                } else {
                    refreshPathBar();
                    mAdapter.clearList();
                    // Save location file information and status
                    isBackPosition = true;
                    refreshAdapter(selecteItemFileInfo.getFileAbsolutePath(), -1, CommonIdentity.REFRESH_FILE_PATH_MODE,
                            CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                }
                if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.updateBarView();
                }
            } else {
                clickcount++;
                if (clickcount == 1) {
                    fir_time = System.currentTimeMillis();
                } else {
                    sec_time = System.currentTimeMillis();
                    if ((sec_time - fir_time) < 600) {
                        return;
                    }
                }
                fir_time = sec_time;
                openFile(selecteItemFileInfo);
            }
        } else {
            mAdapter.setSelect(position);
            updateEditBarByThread();
        }
    }

    public void showAnim(FragmentManager fm, int anim) {
        final FragmentTransaction ft = fm.beginTransaction();
        switch (anim) {
            case CommonIdentity.DIR_ANIM_LEFT:
                ft.setCustomAnimations(R.animator.card_flip_right_out, R.animator.card_flip_left_in);
                break;
            case CommonIdentity.DIR_ANIM_RIGHT:
                ft.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_left_out);
                break;
        }
        final ListsFragment fragment = new ListsFragment();
        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory,
                CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory)
                , mApplication.mCurrentLocation, false,false);
        ft.replace(R.id.layout_content, fragment);
        ft.commitAllowingStateLoss();
        fragment.clearAdapter();
    }

    @Override
    public void closeFloatMenu(boolean isCollapse) {
        if (menuMultipleActions != null) {
            if (menuMultipleActions.isExpanded()) {
                menuMultipleActions.collapse();
            }
            if (!isCollapse) {
                menuMultipleActions.setVisibility(View.GONE);
            }

        }
        if (menuMultipleActionsLeft != null) {
            if (menuMultipleActionsLeft.isExpanded()) {
                menuMultipleActionsLeft.collapse();
            }
            if (!isCollapse) {
                menuMultipleActionsLeft.setVisibility(View.GONE);
            }

        }
    }

    public void showFloatMenu(int selectCount, boolean hasDirctory, boolean isHasZip) {
        if(mApplication == null){
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        }
        if(CommonUtils.isEditStatus(mApplication) && menuMultipleActions != null && menuMultipleActionsLeft != null) {
            if (mApplication.mPortraitOrientation) {
                menuMultipleActions.setVisibility(View.VISIBLE);
                menuMultipleActionsLeft.setVisibility(View.GONE);
            } else {
                menuMultipleActions.setVisibility(View.GONE);
                menuMultipleActionsLeft.setVisibility(View.VISIBLE);
            }
        }
        if (menuMultipleActions != null && menuMultipleActionsLeft != null && (CommonUtils.isInMultiWindowMode(getActivity()) || selectCount <= 0)) {
            closeFloatMenu(false);
            menuMultipleActions.setVisibility(View.GONE);
            menuMultipleActionsLeft.setVisibility(View.GONE);
            return;
        }
        if ((menuMultipleActions!= null && menuMultipleActions.getVisibility() == View.VISIBLE) || (menuMultipleActionsLeft != null && menuMultipleActionsLeft.getVisibility() == View.VISIBLE)) {
            if (!hasDirctory && (mCanShare || mIsFLorSDDrm)) {
                share_btn.setVisibility(View.VISIBLE);
                share_btnLeft.setVisibility(View.VISIBLE);
            } else {
                share_btn.setVisibility(View.GONE);
                share_btnLeft.setVisibility(View.GONE);
            }
            if(mIsHasDrm){
                copy_btn.setVisibility(View.GONE);
                copy_btnLeft.setVisibility(View.GONE);
            } else {
                copy_btn.setVisibility(View.VISIBLE);
                copy_btnLeft.setVisibility(View.VISIBLE);
            }
            if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                archives_btn.setVisibility(View.GONE);
                extract_btn.setVisibility(View.GONE);
                archives_btnLeft.setVisibility(View.GONE);
                extract_btnLeft.setVisibility(View.GONE);
            } else {
                if (selectCount == 1 && isHasZip) {
                    archives_btn.setVisibility(View.GONE);
                    extract_btn.setVisibility(View.VISIBLE);
                    archives_btnLeft.setVisibility(View.GONE);
                    extract_btnLeft.setVisibility(View.VISIBLE);
                } else {
                    /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-06-30,BUG-4967152*/
                    if (mIsHasDrm) {
                        archives_btn.setVisibility(View.GONE);
                        archives_btnLeft.setVisibility(View.GONE);
                    }else{
                        archives_btn.setVisibility(View.VISIBLE);
                        archives_btnLeft.setVisibility(View.VISIBLE);
                    }
                    //archives_btn.setVisibility(View.VISIBLE);
                    extract_btn.setVisibility(View.GONE);
                    //archives_btnLeft.setVisibility(View.VISIBLE);
                    /* MODIFIED-END by Chuanzhi.Shao,BUG-4967152*/
                    extract_btnLeft.setVisibility(View.GONE);
                }
            }
            return;
        }
        if (menuMultipleActions != null && menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapse();
        }
        if (menuMultipleActionsLeft != null && menuMultipleActionsLeft.isExpanded()) {
            menuMultipleActionsLeft.collapse();
        }
        if (paste_btn != null) {
            paste_btn.setVisibility(View.GONE);
        }
        if (paste_btnLeft != null) {
            paste_btnLeft.setVisibility(View.GONE);
        }
        if (mAdapter != null) {
            List<FileInfo> infoList = mAdapter.getItemEditFileInfoList();
            if (infoList != null && infoList.size() == 1) {
                String mFileName = infoList.get(0).getFileName();
                String mExtensionName = null;
                if (mFileName != null) {
                    mExtensionName = FileUtils.getFileExtension(mFileName);
                }
                if (infoList.get(0).isDrm() && archives_btn != null && extract_btn != null
                        && archives_btnLeft != null && extract_btnLeft != null) {
                    archives_btn.setVisibility(View.GONE);
                    extract_btn.setVisibility(View.GONE);
                    copy_btn.setVisibility(View.GONE);
                    archives_btnLeft.setVisibility(View.GONE);
                    extract_btnLeft.setVisibility(View.GONE);
                    copy_btnLeft.setVisibility(View.GONE);
                } else if (infoList.get(0).getFile().isDirectory() && archives_btn != null && extract_btn != null
                        && archives_btnLeft != null && extract_btnLeft != null) {
                    archives_btn.setVisibility(View.VISIBLE);
                    extract_btn.setVisibility(View.GONE);
                    archives_btnLeft.setVisibility(View.VISIBLE);
                    extract_btnLeft.setVisibility(View.GONE);
                } else if (archives_btn != null && extract_btn != null && mExtensionName != null
                        && archives_btnLeft != null && extract_btnLeft != null && mExtensionName != null
                        && mExtensionName.equalsIgnoreCase("zip")) {
                    archives_btn.setVisibility(View.GONE);
                    extract_btn.setVisibility(View.VISIBLE);
                    archives_btnLeft.setVisibility(View.GONE);
                    extract_btnLeft.setVisibility(View.VISIBLE);
                } else {
                    archives_btn.setVisibility(View.VISIBLE);
                    extract_btn.setVisibility(View.GONE);
                    archives_btnLeft.setVisibility(View.VISIBLE);
                    extract_btnLeft.setVisibility(View.GONE);
                }
            } else if (copy_btn != null && cut_btn != null && paste_btn != null && extract_btn != null && mApplication != null &&
                    copy_btnLeft != null && cut_btnLeft != null && paste_btnLeft != null && extract_btnLeft != null &&
                    mApplication.mFileInfoManager != null && mApplication.mFileInfoManager.getPasteCount() > 0) {
                copy_btn.setVisibility(View.GONE);
                cut_btn.setVisibility(View.GONE);
                paste_btn.setVisibility(View.VISIBLE);
                extract_btn.setVisibility(View.GONE);
                copy_btnLeft.setVisibility(View.GONE);
                cut_btnLeft.setVisibility(View.GONE);
                paste_btnLeft.setVisibility(View.VISIBLE);
                extract_btnLeft.setVisibility(View.GONE);
            } else if (archives_btnLeft != null && copy_btnLeft != null && cut_btnLeft != null &&
                    archives_btn != null && copy_btn != null && cut_btn != null && mIsHasDrm) {
                copy_btn.setVisibility(View.GONE);
                cut_btn.setVisibility(View.VISIBLE);
                archives_btn.setVisibility(View.GONE);
                extract_btn.setVisibility(View.GONE);
                copy_btnLeft.setVisibility(View.GONE);
                cut_btnLeft.setVisibility(View.VISIBLE);
                archives_btnLeft.setVisibility(View.GONE);
                extract_btnLeft.setVisibility(View.GONE);
            } else if (archives_btn != null && copy_btn != null && cut_btn != null
                    && archives_btnLeft != null && copy_btnLeft != null && cut_btnLeft != null) {
                copy_btn.setVisibility(View.VISIBLE);
                cut_btn.setVisibility(View.VISIBLE);
                archives_btn.setVisibility(View.VISIBLE);
                extract_btn.setVisibility(View.GONE);
                copy_btnLeft.setVisibility(View.VISIBLE);
                cut_btnLeft.setVisibility(View.VISIBLE);
                archives_btnLeft.setVisibility(View.VISIBLE);
                extract_btnLeft.setVisibility(View.GONE);
            }
            if (mCanShare) {
                share_btn.setVisibility(View.VISIBLE);
                share_btnLeft.setVisibility(View.VISIBLE);
            } else {
                share_btn.setVisibility(View.GONE);
                share_btnLeft.setVisibility(View.GONE);
            }
            if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && archives_btn != null && extract_btn != null
                    && archives_btnLeft != null && extract_btnLeft != null) {
                archives_btn.setVisibility(View.GONE);
                extract_btn.setVisibility(View.GONE);
                archives_btnLeft.setVisibility(View.GONE);
                extract_btnLeft.setVisibility(View.GONE);
            }
            menuMultipleActions.setVisibility(View.VISIBLE);
            menuMultipleActionsLeft.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        if(mApplication.mCurrentStatus == CommonIdentity.FILE_COPY_NORMAL){
            return true;
        }
        FileInfo selecteItemFileInfo = mAdapter.getItem(position);
        if (!selecteItemFileInfo.getFile().exists()) {
            String error = getResources().getString(R.string.path_not_exists, selecteItemFileInfo.getFileName());
            CommonUtils.deleteCache(selecteItemFileInfo, CategoryManager.mCurrentCagegory, mApplication.mCache);
            if(mMediaProviderHelper!=null) {
                mMediaProviderHelper.scanPathforMediaStore(selecteItemFileInfo.getFile().getAbsolutePath());
            }
            if(mAdapter.getItemCount()==1 && CommonUtils.isCategoryMode()){
                mApplication.mFileInfoManager.clearShowFiles();
            }
            refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory,
                    CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
            mToastHelper.showToast(error);
            return false;
        }
        if (mAdapter.isMode(CommonIdentity.FILE_STATUS_NORMAL) || mAdapter.isMode(CommonIdentity.FILE_COPY_NORMAL)) {
            if (!MountManager.getInstance().isRootPath(mApplication.mCurrentPath)) {
                if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.HideActionbar(false);
                }
                if (CommonUtils.isSafeFileView(mApplication)) {
                    isOpenSafeFile = false;
                }
                if (position < mAdapter.getItemCount()) {
                    int top = view.getTop();
                    switchToEditView(position, top, true);
                    return true;
                }
            }
        } else if (mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)) {
            int top = view.getTop();
            switchToEditView(position, top, true);
            //showFloatMenu();
            if (CategoryManager.mCurrentMode != CommonIdentity.CATEGORY_MODE) {
                mApplication.mCurrentPath = mAdapter.getItem(position).getFileParentPath();
                refreshPathBar();
                refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_PATH_MODE,
                        CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
            }
            return true;
        } else if (mAdapter.isMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH)) {
            CommonUtils.hideSoftInput(getActivity());
            CategoryManager.mCurrentMode = CommonIdentity.PATH_MODE;
            mApplication.mCurrentPath = selecteItemFileInfo.getFileParentPath();
            int top = view.getTop();
            switchToEditView(position, top, true);
            updateActionMode(CommonIdentity.FILE_STATUS_EDIT);
            refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory),
                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
            return true;
        }
        return false;
    }

    protected class Pos {
        int index = 0;
        int top = 0;
    }

    protected Stack<Pos> mPosStack = new Stack<Pos>();
    protected boolean mIsBack;
    private WindowManager mWindowManager;
    private int resumeCount;
    private String mSearchMessage;
    List<FileInfo> infos;

    private FileInfo fileInfoEdit = null;
    private AlertDialog mAlertDialog;
    private ProgressPopupWindow mAlertFragmentDialog;
    private DetailDialogFragment mDetailDialog;
    private AlertDialog mDrmDialog;
    static int deleteMode = 0;

    private void setShowAlertDialog(AlertDialog dialog) {
        mAlertDialog = dialog;
    }

    /**
     * when select all, there will be a progress Fragment, this is used to record this fragment.
     *
     * @param dialog
     */
    private void setShowFragmentDialog(ProgressPopupWindow dialog) {
        mAlertFragmentDialog = dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
        mRenameActivity = mActivity;
        try {
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
            if (activity instanceof AbsListViewFragmentListener) {
                mAbsListViewFragmentListener = (AbsListViewFragmentListener) activity;
            }
            boolean forceRefresh = false;
            if (mApplication == null) {
                forceRefresh = true;
            }
            if (forceRefresh) {
                refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory,
                        CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory)
                        , mApplication.mCurrentLocation, false,false);
            }
            mMediaProviderHelper = new MediaStoreHelper(mActivity);
            deleteMediaStoreHelper = new MultiMediaStoreHelper.DeleteMediaStoreHelper(mMediaProviderHelper);
        } catch (Exception e) {
            throw new ClassCastException(activity.toString() + "must implement AbsListViewFragmentListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        // registerDataContentObserver();
        mToastHelper = new ToastHelper(mActivity);

    }

    private void showNoSearchResultView(boolean isShow, String args) {
        if (!isAdded()) {
            return;
        }
        if (isShow) {
            if (mNoFolderView != null && mNoFolderView.getVisibility() == View.VISIBLE) {
                mNoFolderView.setVisibility(View.GONE);
            }
            if (mNoSearchView != null) {
                mNoSearchView.setVisibility(View.VISIBLE);
                String noResultText = getResources().getString(R.string.no_search_result_m);
                noResultText = String.format(noResultText, args);
                noSearchText.setText(mAdapter.setHighLight(noResultText, "\"" + args + "\""));
            }
        } else {
            if (mNoSearchView != null) {
                mNoSearchView.setVisibility(View.GONE);
            }
        }
    }

    public void onGlobalSearchBackPressed() {
        showNoSearchResultView(false, null);
        if (mAdapter != null) {
            mAdapter.changeModeFromSearchToNormal();
        }
        updateActionMode(CommonIdentity.FILE_STATUS_NORMAL);
    }

    public void onBackPress() {
        showNoSearchResultView(false, null);
        showNoFolderResultView(false);
        isOpenCDDrmFile = false;
        if (menuMultipleActions != null) {
            menuMultipleActions.setVisibility(View.GONE);
            if (menuMultipleActions.isExpanded()) {
                menuMultipleActions.collapse();
            }
        }
        if (menuMultipleActionsLeft != null) {
            menuMultipleActionsLeft.setVisibility(View.GONE);
            if (menuMultipleActionsLeft.isExpanded()) {
                menuMultipleActionsLeft.collapse();
            }
        }
        if (mAdapter == null) {
            return;
        }
        if (mAdapter != null && mAdapter.isMode(CommonIdentity.FILE_STATUS_EDIT) || mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)) {
            if (mAdapter.isMode(CommonIdentity.FILE_STATUS_EDIT)) {
                if (mFromSearchToEdit) {
                    if (mApplication.mFileInfoManager.getSearchItemsCount() > 0) {
                        showSearchResultView();
                    } else {
                        exitSearchResultView();
                    }
                } else {
                    switchToNormalView();
//                    if (CategoryManager.mCurrentMode != CommonIdentity.CATEGORY_MODE&&!isShowNoFolderView()) {
//                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory),
//                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
//                    }
                }
                mFromSearchToEdit = false;
            } else {
                exitSearchResultView();
            }
            return;
        }
        mIsBack = true;
        if (CommonUtils.isPathNormalMode(mApplication.mCurrentStatus)) {
            if (mApplication.mCurrentPath != null && !MountManager.getInstance().isSdOrPhonePath(mApplication.mCurrentPath)) {
                File mCurrentFile = new File(mApplication.mCurrentPath);
                // Save location file information and status
                mPositionInfo = new FileInfo(mApplication,mCurrentFile);
                String parentDir = mCurrentFile.getParent();
                refreshAdapter(parentDir, -1, CommonIdentity.REFRESH_FILE_PATH_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,true);
            } else {
                mActivity.finish();
            }
        }
    }

    public boolean refreshAdapter(String path, int category, int refreshMode, int locationMode, boolean showDialog, final boolean isPosition) {
        if (mApplication == null) {
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        }
        // If has cached list, then no need to create new task.
        boolean isPathCacheExist = false;
        String key = null;
        if (CommonUtils.isPathMode(path) && !CommonUtils.isPrivateLocation(mApplication)) {
            isPathCacheExist = mApplication.mCache.hasCachedPath(path);
            if (isPathCacheExist) {
                key = path;
            }
        } else if (CommonUtils.isCategoryMode() && category == CategoryManager.mCurrentCagegory && !CommonUtils.isPrivateLocation(mApplication)) {
            isPathCacheExist = mApplication.mCache.hasCachedPath(String.valueOf(category));
            if (isPathCacheExist) {
                key = String.valueOf(category);
            }
        }
        List<FileInfo> mPathCache = null;
        if (!CommonUtils.isPrivateLocation(mApplication) && refreshMode == CommonIdentity.REFRESH_FILE_PATH_MODE && isPathCacheExist) {
            refreshMode = CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE;
        } else if (!CommonUtils.isPrivateLocation(mApplication) && refreshMode == CommonIdentity.REFRESH_FILE_CATEGORY_MODE && isPathCacheExist) {
            refreshMode = CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE;
        }
        if ((CommonUtils.isPrivateLocation(mApplication) || locationMode == CommonIdentity.FILE_MANAGER_LOCATIONE) && refreshMode == CommonIdentity.REFRESH_FILE_CATEGORY_MODE
                && 0 <= category && category < 10 && PermissionUtil.checkAppPermission(mApplication)) {
            showDialog = true;
            CommonUtils.getBaseTaskInfo(mApplication, refreshMode, CommonIdentity.REFRESH_FILE_PATH_MODE, showDialog ? CommonIdentity.LOADING_REFRESH_MODE : CommonIdentity.NOTIFICATION_REFRESH_MODE,
                    mApplication.getResources().getString(R.string.loading).toString(), CommonIdentity.LIST_INFO_TASK, category, null, null, -1, false, null);
            return true;

        } else if (CommonUtils.isPrivateLocation(mApplication) &&
                (refreshMode == CommonIdentity.REFRESH_PRIVATE_CATEGORY_MODE ||
                        refreshMode == CommonIdentity.REFRESH_SAFE_CATEGORY_MODE) && 12 == category) {
            showDialog = true;
            CommonUtils.getBaseTaskInfo(mApplication, refreshMode, CommonIdentity.REFRESH_PRIVATE_CATEGORY_MODE, CommonIdentity.LOADING_REFRESH_MODE,
                    mApplication.getResources().getString(R.string.loading).toString(), CommonIdentity.LIST_INFO_TASK, category, null, null, -1, false, null);
            return true;
        } else if (locationMode == CommonIdentity.FILE_MANAGER_LOCATIONE && refreshMode == CommonIdentity.REFRESH_FILE_PATH_MODE) {
            showDialog = false;
            CommonUtils.getBaseTaskInfo(mApplication, refreshMode, CommonIdentity.REFRESH_SAFE_CATEGORY_MODE, CommonIdentity.LOADING_REFRESH_MODE,
                    mApplication.getResources().getString(R.string.loading).toString(), CommonIdentity.LIST_INFO_TASK, category, null, path, -1, false, null);
            return true;
        } else if (refreshMode == CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE) {
            if (isPathCacheExist && !TextUtils.isEmpty(path) && CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE) {
                mApplication.mCurrentPath = key;
            }
            if (isPathCacheExist && !TextUtils.isEmpty(key) && category != CommonIdentity.SORT_UPDATE_ADAPTER_NOTIFICATION) {
                 mPathCache = getPathCacheExist(key);
            // change sort type,get current show list
            } else if(category == CommonIdentity.SORT_UPDATE_ADAPTER_NOTIFICATION && mAdapter != null) {
                mPathCache = mAdapter.getList();
            }

            if (isPathCacheExist || category == CommonIdentity.SORT_UPDATE_ADAPTER_NOTIFICATION) {
                // Cache files to sort
                if (CommonUtils.isPathMode(mApplication.mCurrentPath)) {
                    mApplication.mFileInfoManager.loadFileInfoList(mApplication.mCurrentPath, mPathCache,
                            mApplication.mSortType);
                } else if (CommonUtils.isRecentCategoryMode()) {
                    mApplication.mFileInfoManager.loadFileInfoList(category, mPathCache,
                            FileInfoComparator.SORT_BY_TIME);
                } else if (CommonUtils.isCategoryMode() && SafeManager.mCurrentSafeCategory != 12) {
                    mApplication.mFileInfoManager.loadFileInfoList(category, mPathCache,
                            mApplication.mSortType);
                }
            }
            if (mAdapter == null) {
                return false;
            }
            if(category == CommonIdentity.SORT_UPDATE_ADAPTER_NOTIFICATION){
                mAdapter.refreshSortAdapter();
            } else {
                mAdapter.refresh();
            }
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (!CommonUtils.isGlobalSearchStatus(mApplication) && !CommonUtils.isSearchStatus(mApplication)) {
                        showNoFolderResultView(mAdapter.getItemCount() <= 0);
                    }
                    if ((mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION || mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION) &&
                            CategoryManager.mCurrentCagegory > 0 && SafeManager.mCurrentSafeCategory != 12) {
                        switchToEditView();
                    } else if (!CommonUtils.isEditStatus(mApplication)) {
                        // refresh action title and navigation
                        refreshPathBar();
                        if(isPosition){
                            if(mPositionInfo != null) {
                                int postion = mAdapter.getPosition(mPositionInfo);
                                setViewPostion(postion,isBackPosition);
                                mPositionInfo = null;
                                isBackPosition = false;
                            }
                        }

                    }
                }
            }, 50);

            return true;
        }
        return false;
    }


    public List<FileInfo> getPathCacheExist(String path) {
        if (mApplication == null || mApplication.mCache == null) {
            return null;
        }
        return mApplication.mCache.get(path);
    }

    private void showDrmWifidisplyDiaog(final Context context) {
        mDrmDialog = new AlertDialog.Builder(mActivity).setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setTitle(R.string.drm_wifidisplay_title).setMessage(R.string.drm_wifidisplay_message).setPositiveButton(R.string.drm_wifidisplay_cancel_btn, new OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {

                        DisplayManagerGlobal.getInstance().disconnectWifiDisplay();

                        Toast.makeText(context, R.string.tv_link_close_toast, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.ok, null).show();
    }


    private void exitSearchResultView() {
        switchToNormalView();
        if (mApplication.mFileInfoManager != null) {
//            if (CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory) == -1) {
//                showBeforeSearchContent();
//            }
            /**
             * when eixtSearchview back normal status list
             */
            refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory),
                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
        }
        mFromSearchToEdit = false;
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.changeSearchMode(false);
        }
    }


    private void showBeforeSearchContent() {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        if (mApplication != null && mApplication.mFileInfoManager != null) {
//            mApplication.mFileInfoManager.listBeforeSearchFiles(mActivity.getClass().getName(),
//                    new HeavyOperationListener(), mApplication.mFileInfoManager.getBeforeSearchList());
        }
    }


    public Handler mTaskResultHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            Bundle mBundle = msg.getData();
            mResultInfo = (TaskInfo) mBundle.getSerializable(CommonIdentity.RESULT_TASK_KEY);
            if (mResultInfo == null) {
                return;
            }
            int errorType = mResultInfo.getResultCode();
            int errorCode = mResultInfo.getErrorCode();
            if (!isAdded()) {
                // if Fragment not attached with activity, just return
                switch (mResultInfo.getBaseTaskType()) {
                    case CommonIdentity.PASTE_COPY_TASK:
                    case CommonIdentity.NORMAL_DELETE_TASK:
                    case CommonIdentity.PASTE_CUT_TASK:
                    case CommonIdentity.FILE_COMPRESSION_TASK:
                    case CommonIdentity.FILE_UNCOMPRESSION_TASK:
                    case CommonIdentity.ADD_PRIVATE_FILE_TASK:
                    case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                        if(errorType < 0) showErrorToast(errorType, mResultInfo);
                        break;
                }
                return;
            }
            String mDstPath = null;
            int category = -1;
            int mRefreshMode = -1;
            switch (mResultInfo.getBaseTaskType()) {
                case CommonIdentity.PROGRESS_SHOW_TASK:
                    ProgressInfo mProgressInfo = mResultInfo.getProgressInfo();
                    int mTaskType = mProgressInfo.getProgressTaskType();
                    int progress = mProgressInfo.getProgeress();
                    int max = (int) mProgressInfo.getTotal();
                    float mTotal = (float) progress / (float) max;
                    int mTotals = (int) (mTotal * 100f);
                    TaskInfoMap.addtaskInfo(mResultInfo.getCreateTaskTime(), mResultInfo);
                    if (mApplication.mProgressDialog != null && mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_DIALOG_MODE) {
                        updateDialogProgress(mProgressInfo, mResultInfo.getCreateTaskTime(), mTotals, mTaskType, max, progress);
                        if(mApplication.mNotiManager==null)mApplication.mNotiManager=(NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
                        if(mApplication.mNotiManager.getActiveNotifications().length>0) {
                            updateNotificationProgress(mProgressInfo, mTotals, max, progress, false);
                        }
                    } else if (mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_NOTIFICATION_MODE) {
                        updateNotificationProgress(mProgressInfo, mTotals, max, progress,true);
                    } else if (mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_ALL_SHOW_MODE) {
                        if (mApplication.mProgressDialog != null && mApplication.mProgressDialog.isShowing()) {
                            updateDialogProgress(mProgressInfo, mResultInfo.getCreateTaskTime(), mTotals, mTaskType, max, progress);
                        }
                        updateNotificationProgress(mProgressInfo, mTotals, max, progress,true);
                    }
                    break;
                case CommonIdentity.OBSERVER_UPDATE_TASK:
                    if (CommonUtils.isPrivateLocation(mApplication)) {
                        return;
                    }
                    int mObserverMode = mResultInfo.getAdapterMode();
                    if (mObserverMode == CommonIdentity.REFRESH_FILE_PATH_MODE) {
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_PATH_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    } else if (mObserverMode == CommonIdentity.REFRESH_FILE_CATEGORY_MODE) {
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_CATEGORY_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    } else if (CommonUtils.isCategoryMode()) {
                        refreshAdapter(null, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    } else if (CommonUtils.isPathMode(mApplication.mCurrentPath)) {
                        refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    }
                    return;
                case CommonIdentity.LIST_INFO_TASK:
                    if (mResultInfo.getRefreshMode() == CommonIdentity.REFRESH_FILE_PATH_MODE) {
                        mApplication.mCurrentPath = mResultInfo.getDestPath();
                        if (CommonUtils.isPrivateLocation(mApplication) &&
                                CategoryManager.mCurrentCagegory > 0 && SafeManager.mCurrentSafeCategory != 12) {
                            switchToEditView();
                        } else if (!CommonUtils.isEditStatus(mApplication)) {
                            refreshPathBar();
                        }
                        if (mApplication != null && mApplication.mFileInfoManager != null) {
                            mApplication.mFileInfoManager.loadFileInfoList(mResultInfo.getDestPath(), mApplication.mSortType);
                        }
                    } else if (mResultInfo.getRefreshMode() == CommonIdentity.REFRESH_FILE_CATEGORY_MODE
                            || mResultInfo.getRefreshMode() == CommonIdentity.REFRESH_SAFE_CATEGORY_MODE) {
                        if (CategoryManager.mCurrentCagegory >= 0 || SafeManager.mCurrentSafeCategory == 12) {

                            if (mApplication != null && mApplication.mFileInfoManager != null &&
                                    mApplication.mFileInfoManager.getShowFileList().size() == 0 &&
                                    mAdapter != null && !mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)) {
                                try {
                                    if (mNo_messageView != null) {
                                        mNo_messageView.setText(mActivity.getResources().getString(R.string.no_category));
                                    }
                                    showNoFolderResultView(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showNoFolderResultView(false);
                            }
                        }
                        FileInfo.mountReceiver = false;
                        FileInfo.scanFinishReceiver = false;
                        if (mResultInfo.getRefreshMode() == CommonIdentity.REFRESH_FILE_CATEGORY_MODE) {
                            CategoryManager.mLastCagegory = CategoryManager.mCurrentCagegory;
                        } else {
                            CategoryManager.mLastCagegory = SafeManager.mCurrentSafeCategory;
                        }
                    }
                    onTaskComplete(mResultInfo);
                    if (SafeManager.mCurrentSafeCategory == 12) {
                        refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                mApplication.mCurrentLocation, false,false);
                    } else {
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    }
                    if(mApplication != null && mApplication.mFileInfoManager != null&&mApplication.mFileInfoManager.getShowFileList().size()>CommonIdentity.HAWKEYE_LISTFILE_COUNT) {
                        CommonUtils.hawkeyeTimeEvent(mApplication, CommonIdentity.FILEMANAGER_LISTFILE_TIME, System.currentTimeMillis() - mResultInfo.getCreateTaskTime(),mApplication);
                    }
                    if (mAdapter != null && mAdapter.isMode(CommonIdentity.FILE_STATUS_EDIT)) {
                        updateEditBarState();
                    }
                    if (CommonUtils.isPrivateLocation(mApplication)) {
                        return;
                    }
                    String key = null;
                    if (mResultInfo.getRefreshMode() == CommonIdentity.REFRESH_FILE_CATEGORY_MODE && CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && CategoryManager.mCurrentCagegory >= 0 && CategoryManager.mCurrentCagegory < 9) {
                        key = String.valueOf(CategoryManager.mCurrentCagegory);
                    } else if (mResultInfo.getRefreshMode() == CommonIdentity.REFRESH_FILE_PATH_MODE && mApplication.mCurrentPath != null) {
                        key = String.valueOf(mApplication.mCurrentPath);
                    }
                    if (key != null) {
                        List<FileInfo> list = new ArrayList<>();
                        if (!mApplication.mCache.hasCachedPath(key)) {
                            // Cache directory and the files that are hidden and displayed in the classification.
                            list.addAll(mApplication.mCache.getAllFileList());
                            if (list != null && list.size() > 0) {
                                mApplication.mCache.put(key, list);
                            }
                        }
                    }
                    break;
                case CommonIdentity.DETAIL_FILE_TASK:
                    showFileDetailInfo(mResultInfo);
                    break;
                case CommonIdentity.SEARCH_INFO_TASK:
                    String mSearchText = mResultInfo.getSearchContent();
                    if (mResultInfo.getResultCode() != OperationEventListener.ERROR_CODE_SUCCESS) {
                        return;
                    }
                    mCurrentSearchText = ((FileBaseActivity) mActivity).getQueryText();
                    if (mAdapter == null) return;
                    if (mAdapter.getItemCount() > 0 && !mSearchText.equalsIgnoreCase(mCurrentSearchText)) {
                        return;
                    }
                    if (mCurrentSearchText.equalsIgnoreCase(mLastSearchText)) return;
                    mLastSearchText = mSearchText;
                    if (mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)
                            || mAdapter.isMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH) || mApplication.mCurrentStatus == CommonIdentity.FILE_STATUS_GLOBALSEARCH) {
                        mApplication.mFileInfoManager
                                .updateSearchList(mApplication.mSortType);
                        refreshAdapter("", CommonIdentity.UPDATE_ADAPTER_SORT_NOTIFICATION, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        onTaskComplete(mResultInfo);
                        updateSearchResultItem(mSearchText);
                    }
                    break;
                case CommonIdentity.PASTE_COPY_TASK:
                    if (!isAdded()) {
                        return;
                    }
                    if(errorType!=OperationEventListener.ERROR_CODE_PASTE_TO_SUB) {
                        onTaskComplete(mResultInfo);
                    }
                    mDstPath = mResultInfo.getDestPath();
                    if (mDstPath != null && mApplication.mCurrentPath != null && mApplication.mCurrentPath.equals(mDstPath)) {
                        if (mAbsListViewFragmentListener != null && mAdapter != null && mAdapter.isMode(CommonIdentity.FILE_STATUS_NORMAL)) {
                            mAbsListViewFragmentListener.enableScrollActionbar();
                        }
                        if (mApplication.mCache.hasCachedPath(mDstPath)) {
                            refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        } else {
                            refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_PATH_MODE,
                                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        }
                    }
                    if (errorType >= 0) {
                        mAbsListViewFragmentListener.showBottomView(getResources().getString(R.string.pasted));
                    } else {
                        showErrorToast(errorType, mResultInfo);
                    }
                    releaseWakeLock();
                    break;
                case CommonIdentity.PASTE_CUT_TASK:
                    if (!isAdded()) {
                        return;
                    }
                    if(errorType!=OperationEventListener.ERROR_CODE_PASTE_TO_SUB) {
                        onTaskComplete(mResultInfo);
                    }
                    mDstPath = mResultInfo.getDestPath();
                    if (mDstPath != null && mApplication.mCurrentPath != null && mApplication.mCurrentPath.equals(mDstPath)) {
                        if (mAbsListViewFragmentListener != null && mAdapter != null && mAdapter.isMode(CommonIdentity.FILE_STATUS_NORMAL)) {
                            mAbsListViewFragmentListener.enableScrollActionbar();
                        }
                        if (mApplication.mCache.hasCachedPath(mDstPath)) {
                            refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        } else {
                            refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_PATH_MODE,
                                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        }
                    }
                    if (errorType >= 0) {
                        mAbsListViewFragmentListener.showBottomView(getResources().getString(R.string.pasted));
                    } else {
                        showErrorToast(errorType, mResultInfo);
                    }
                    releaseWakeLock();
                    break;
                case CommonIdentity.PROGRESS_DIALOG_TASK:
                    if (!isAdded()) {
                        return;
                    }
                    long createTime = mResultInfo.getCreateTaskTime();
                    if (mApplication.mProgressDialog != null) {
                        mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
                        try {
                            mApplication.mProgressDialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        mApplication.mProgressDialog = null;
                    }
                    if (mApplication.mProgressDialog == null && (mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_DIALOG_MODE
                            || mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_ALL_SHOW_MODE
                            ||getContext().getString(R.string.loading).equals(mResultInfo.getTitleStr()))) {
                        mApplication.mProgressDialog = ProgressPopupWindow.newInstance(mActivity, mResultInfo);
                        mApplication.mProgressDialog.setCancelable(false);
                        if (!mApplication.mProgressDialog.isShowing()) {
                            mApplication.mProgressDialog.show();
                        }
                    }
                    if (mResultInfo.getFileFilter() == CommonIdentity.PROGRESS_NOTIFICATION_TASK) {
                        return;
                    }
                    RunningTaskInfo mSaveRunningInfo = null;
                    if (mApplication.mProgressDialog != null && CommonUtils.isShowHorizontalProgressBar(mResultInfo.getFileFilter())) {
                        if (!ExcuteTaskMap.isExist(createTime)) {
                            mSaveRunningInfo = new RunningTaskInfo(mResultInfo.getCreateTaskTime());
                            mSaveRunningInfo.setDialogTitle(mResultInfo.getTitleStr());
                            ExcuteTaskMap.addFinishTask(createTime, mSaveRunningInfo);
                        }
                    }

                    break;
                case CommonIdentity.PROGRESS_COMPLETE_TASK:
                    showErrorToast(errorCode, mResultInfo);
                    return;
                case CommonIdentity.NORMAL_DELETE_TASK:
                    onTaskComplete(mResultInfo);
                    if(mApplication!=null&&CommonUtils.getAvailMemory(mApplication)<CommonIdentity.LOW_MEMORY_TAG) {
                        CommonUtils.hawkeyeTimeEvent(mApplication, CommonIdentity.FILEMANAGER_DELETE_TIME, System.currentTimeMillis() - mResultInfo.getCreateTaskTime(),mApplication);
                    }
                    category = mResultInfo.getCategoryIndex();

                    if (category < 0 && CommonUtils.isPathMode()) {
                        mDstPath = mResultInfo.getDestPath();
                    } else {
                        mDstPath = mApplication.mCurrentPath;
                    }
                    mRefreshMode = CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE;
                    if (CommonUtils.isPathMode(mDstPath)) {
                        mRefreshMode = CommonIdentity.REFRESH_FILE_PATH_MODE;
                    } else if (CommonUtils.isFilePathLocation(mApplication) && CommonUtils.isCategoryMode() && category == CategoryManager.mCurrentCagegory) {
                        mRefreshMode = CommonIdentity.REFRESH_FILE_CATEGORY_MODE;
                    }

                    if ((CommonUtils.isFilePathLocation(mApplication) && CommonUtils.isCategoryMode() && category == CategoryManager.mCurrentCagegory) || (mDstPath != null && mApplication.mCurrentPath != null && mApplication.mCurrentPath.equals(mDstPath))) {
                        refreshAdapter(mDstPath, category, mRefreshMode,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    } else if (mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION
                            && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE &&
                            SafeManager.mCurrentSafeCategory == CommonIdentity.SAFE_CATEGORY_PRIVATE) {
                        if (SafeManager.getPrivateFileCount(mApplication) > 0) {
                            refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_SAFE_CATEGORY_MODE,
                                    CommonIdentity.FILE_SAFEBOX_LOCATION, false,false);
                        } else {
                            if (mAbsListViewFragmentListener != null) {
                                mAbsListViewFragmentListener.refreashSafeFilesCategory();
                            }
                        }
                    }
                    if (errorType >= 0) {
                        mAbsListViewFragmentListener.showBottomView(getResources().getString(R.string.deleted));
                    }
                    releaseWakeLock();
                    break;
                case CommonIdentity.RENAME_FILE_TASK:
                    String mRenameSearchText = mResultInfo.getSearchContent();
                    if (mRenameSearchText != null && !mRenameSearchText.equals("")
                            && mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)) {
                        if (mAbsListViewFragmentListener != null) {
                            mAbsListViewFragmentListener.updateSearch(mRenameSearchText);
                        }
                    } else if (mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION
                            && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE &&
                            SafeManager.mCurrentSafeCategory == CommonIdentity.SAFE_CATEGORY_PRIVATE) {
                        refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_SAFE_CATEGORY_MODE,
                                CommonIdentity.FILE_SAFEBOX_LOCATION, false,false);
                    } else {
                        mRefreshMode = CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE;
                        category = mResultInfo.getCategoryIndex();
                        if (CommonUtils.isPathMode(mApplication.mCurrentPath)) {
                            mRefreshMode = CommonIdentity.REFRESH_FILE_PATH_MODE;
                        } else if (CommonUtils.isCategoryMode() && category == CategoryManager.mCurrentCagegory) {
                            mRefreshMode = CommonIdentity.REFRESH_FILE_CATEGORY_MODE;
                        }
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, mRefreshMode,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    }
                    break;
                case CommonIdentity.CREATE_FOLDER_TASK:
                    mPositionInfo = mResultInfo.getSrcFile();
                    refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_PATH_MODE,
                            CommonIdentity.FILE_MANAGER_LOCATIONE, false,true);
                    break;
                case CommonIdentity.FILE_COMPRESSION_TASK:
                    refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_PATH_MODE,
                            CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    onTaskComplete(mResultInfo);
                    if (errorType >= 0) {
                        mAbsListViewFragmentListener.showBottomView(getResources().getString(R.string.compress_susscces));
                    } else {
                        showErrorToast(errorType, mResultInfo);
                    }
                    releaseWakeLock();
                    break;
                case CommonIdentity.FILE_UNCOMPRESSION_TASK:
                    refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_PATH_MODE,
                            CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    onTaskComplete(mResultInfo);
                    if (errorType >= 0) {
                        mAbsListViewFragmentListener.showBottomView(getResources().getString(R.string.extract_susscces));
                    } else {
                        showErrorToast(errorType, mResultInfo);
                    }
                    releaseWakeLock();
                    break;
                case CommonIdentity.ADD_PRIVATE_FILE_TASK:
                    if (CommonUtils.isPrivateLocation(mApplication)) {
                        SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_PRIVATE;
                        SafeManager.mCurrentmode = CommonIdentity.FILE_STATUS_NORMAL;
                        mAdapter.changeMode(CommonIdentity.FILE_STATUS_NORMAL);
                        clearChecked();
                        switchToNormalView();
                        SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
                        if (SafeManager.getPrivateFileCount(mApplication) > 0) {
                            refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_PRIVATE_CATEGORY_MODE,
                                    mApplication.mCurrentLocation, false, false);
                        } else {
                            if (mAbsListViewFragmentListener != null) {
                                mAbsListViewFragmentListener.refreashSafeFilesCategory();
                            }
                        }
                    } else {
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    }
                    onTaskComplete(mResultInfo);
                    releaseWakeLock();
                    break;

                case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                    if (CommonUtils.isPrivateLocation(mApplication)) {
                        SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_PRIVATE;
                        SafeManager.mCurrentmode = CommonIdentity.FILE_STATUS_NORMAL;
                        mAdapter.changeMode(CommonIdentity.FILE_STATUS_NORMAL);
                        clearChecked();
                        switchToNormalView();
                        SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
                        if (SafeManager.getPrivateFileCount(mApplication) > 0) {
                            refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_PRIVATE_CATEGORY_MODE,
                                    mApplication.mCurrentLocation, false,false);
                        } else {
                            if (mAbsListViewFragmentListener != null) {
                                mAbsListViewFragmentListener.refreashSafeFilesCategory();
                            }
                        }
                    } else {
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    }
                    onTaskComplete(mResultInfo);
                    releaseWakeLock();
                    break;

                default:
                    refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                            CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    return;

            }
            showErrorToast(errorType, mResultInfo);


        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    public void createNotificationProgress() {
        if (mApplication.mNotiManager == null) {
            mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        TaskInfo taskInfo = null;
        long createTime = ProgressPopupWindow.getCreateTaskTime();
        if (TaskInfoMap.getTaskInfo(createTime) != null) {
            taskInfo = TaskInfoMap.getTaskInfo(createTime);
        } else {
            taskInfo = mResultInfo;
        }
        if (taskInfo == null || taskInfo.getProgressInfo() == null) {
            return;
        }
        int taskType = taskInfo.getProgressInfo().getProgressTaskType();
        Notification.Builder builder;
        Notification notification;
        if (NotificationMap.getNotification(taskInfo.getCreateTaskTime()) != null) {
            builder = NotificationMap.getNotification(taskInfo.getCreateTaskTime());
        } else {
            builder = ProgressNotification.newInstance(getActivity(), createTime);
        }
        notification = builder.build();
        NotificationMap.addNotification(taskInfo.getCreateTaskTime(), builder);
        notification.contentView.setTextViewText(R.id.title, CommonUtils.getNotificationTitle(getActivity(), taskType));
        if (Build.VERSION.SDK_INT < 24) {
            notification.contentView.setImageViewResource(R.id.noti_icon, CommonUtils.getNotificationIconId(taskType));
        }
        notification.setSmallIcon(Icon.createWithResource(getActivity(), CommonUtils.getNotificationIconId(taskType)));
        mApplication.mNotiManager.notify((int) createTime, notification);
        Message msg1 = new Message();
        Bundle mBundle1 = new Bundle();
        mBundle1.putSerializable(CommonIdentity.RESULT_TASK_KEY, TaskInfoMap.getTaskInfo(createTime));
        msg1.setData(mBundle1);
        mTaskResultHandler.sendMessage(msg1);
    }

    private void updateDialogProgress(ProgressInfo mProgressInfo, long mTaskCreateTime, int mTotals, int mTaskType, int max, int progress) {
        String mProgressNumberFormat = null;
        if (mProgressInfo.getUnitStyle() == ProgressInfo.M_MODE) {
            mProgressNumberFormat = FileUtils.safeFileSizeToString(mApplication, progress, max);
        } else if (max > 0) {
            mProgressNumberFormat = progress + "/" + max;
        }
        if (CommonUtils.isShowHorizontalProgressBar(mTaskType)) {
            if (ProgressPopupWindow.getCreateTaskTime() == mTaskCreateTime) {
                ProgressPopupWindow.mFirstProgressbar.setMax(max);
                ProgressPopupWindow.mTotalProgress.setText(mProgressInfo.getUpdateInfo());
                ProgressPopupWindow.mFirstProgressbar.setProgress(progress);
                ProgressPopupWindow.mFirstTaskProgress.setText(mProgressNumberFormat);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void updateNotificationProgress(ProgressInfo mProgressInfo, int mTotals, int max, int progress, boolean isCreateNew) {
        if (mApplication.mNotiManager == null) {
            mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Notification.Builder builder = NotificationMap.getNotification(mResultInfo.getCreateTaskTime());
        Notification mNotificationProgress;
        int taskType = mProgressInfo.getProgressTaskType();
        if (builder == null) {
            if(RunningTaskMap.getRunningTask(mProgressInfo.getCreateTime())==null||!isCreateNew){
                return;
            }
            long createTime = mProgressInfo.getCreateTime();
            builder = ProgressNotification.newInstance(getActivity(), createTime);
            NotificationMap.addNotification(createTime, builder);
            mNotificationProgress = builder.build();
            mNotificationProgress.contentView.setTextViewText(R.id.title, CommonUtils.getNotificationTitle(getActivity(), taskType));
            if (Build.VERSION.SDK_INT < 24) {
                mNotificationProgress.contentView.setImageViewResource(R.id.noti_icon, CommonUtils.getNotificationIconId(taskType));
            }
            mNotificationProgress.setSmallIcon(Icon.createWithResource(getActivity(), CommonUtils.getNotificationIconId(taskType)));
        } else {
            mNotificationProgress = builder.build();
        }
        String mProgressNumberFormat = null;
        if (mProgressInfo.getUnitStyle() == ProgressInfo.M_MODE) {
            mProgressNumberFormat = FileUtils.safeFileSizeToString(mApplication, progress, max);
        } else if (max > 0) {
            mProgressNumberFormat = progress + "/" + max;
        }
        mNotificationProgress.contentView.setTextViewText(R.id.noti_total_title, mResultInfo.getProgressInfo().getUpdateInfo().toString());
        if (mTotals <= 100) {
            if (Build.VERSION.SDK_INT >= 24) {
                builder.setSubText(mTotals + "%");
            }
            mNotificationProgress.contentView.setTextViewText(R.id.noti_progress_text, mProgressNumberFormat);
            mNotificationProgress.contentView.setProgressBar(R.id.noti_total_progress, 100, mTotals, false);
            mApplication.mNotiManager.notify((int) mResultInfo.getCreateTaskTime(), mNotificationProgress);
        }
    }

    public void showErrorToast(int errorType, TaskInfo mTaskInfo) {
        switch (errorType) {
            case OperationEventListener.ERROR_CODE_FILE_EXIST:
                String mDstName = mTaskInfo.getTitleStr();
                if (mDstName != null) {
                    mToastHelper.showToast(getResources().getString(R.string.already_exists, mDstName));
                }
                break;
            case OperationEventListener.ERROR_CODE_NAME_EMPTY:
                mToastHelper.showToast(R.string.invalid_empty_name);
                break;
            case OperationEventListener.ERROR_CODE_NAME_TOO_LONG:
                mToastHelper.showToast(R.string.file_name_too_long);
                break;
            case OperationEventListener.ERROR_COMPRESS_FILE_NAME_TOO_LONG:
                mToastHelper.showToast(R.string.compress_file_name_too_long);
                break;
            case OperationEventListener.ERROR_INVALID_CHAR:
                mToastHelper.showToast(R.string.invalid_char_prompt);
                break;
            case OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION:
                mToastHelper.showToast(R.string.copy_deny);
                break;
            case OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION:
                mToastHelper.showToast(R.string.delete_deny);
                break;
            case OperationEventListener.ERROR_CODE_DELETE_UNSUCCESS:
                mToastHelper.showToast(R.string.some_delete_fail);
                break;
            case OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS:
                mToastHelper.showToast(R.string.some_paste_fail);
                break;
            case OperationEventListener.ERROR_CODE_PASTE_TO_SUB:
                mToastHelper.showToast(R.string.paste_sub_folder);
                break;
            case OperationEventListener.ERROR_CODE_CUT_SAME_PATH:
                mToastHelper.showToast(R.string.paste_same_folder);
                break;
            case OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE:
                mToastHelper.showToast(R.string.insufficient_memory);
                break;
            case OperationEventListener.ERROR_CODE_DELETE_FAILS:
                mToastHelper.showToast(R.string.delete_fail);
                break;
            case OperationEventListener.ERROR_CODE_EXCEEDED_MAX_TASK:
                mToastHelper.showToast(R.string.exceeded_max_task);
                break;
            case OperationEventListener.ERROR_CODE_ADD_WAITING_TASK:
                mToastHelper.showToast(R.string.add_waiting_task);
                break;
            case OperationEventListener.ERROR_CODE_EXTRACT_FAIL_TASK:
            case OperationEventListener.ERROR_CODE_OPERATION_FAILS:
                mToastHelper.showToast(R.string.operation_fail);
                break;
            case OperationEventListener.ERROR_SET_PRIVATE_FAILS:
                mToastHelper.showToast(R.string.set_private_failed);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onTaskComplete(TaskInfo mResultInfo) {
        if (RunningTaskMap.getRunningTaskSize() == 0 && WaittingTaskList.getWaittingTaskSize() == 0) {
            if (mApplication.mNotiManager == null) {
                mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            mApplication.mNotiManager.cancelAll();
            NotificationMap.clearAllNotification();
            TaskInfoMap.clearAllTaskInfo();
            if (mApplication.mProgressDialog != null) {
                if (isAdded() && mApplication.mProgressDialog.isShowing()) {
                    try {
                        mApplication.mProgressDialog.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                mApplication.mProgressDialog = null;
            }
            mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
        } else if (RunningTaskMap.getRunningTaskSize() > 0) {
            if (mApplication.mNotiManager != null) {
                mApplication.mNotiManager.cancel((int) mResultInfo.getCreateTaskTime());
                RunningTaskMap.removeRunningTask(mResultInfo.getCreateTaskTime());
                NotificationMap.removeNotification(mResultInfo.getCreateTaskTime());
                TaskInfoMap.removeTaskInfo(mResultInfo.getCreateTaskTime());
                if (mApplication.mNotiManager.getActiveNotifications().length > 0) {
                    mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_ALL_SHOW_MODE;
                } else {
                    mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
                }
            }
        }
        if (CommonUtils.isShowHorizontalProgressBar(mResultInfo.getBaseTaskType())) {
            long mCreateTime = mResultInfo.getCreateTaskTime();
            if (ExcuteTaskMap.getFinishTaskSize() <= 0) {
                return;
            }
            RunningTaskInfo mRunningInfo = ExcuteTaskMap.getFinishTask(mCreateTime);
            if (mRunningInfo == null) {
                return;
            }
            if (CommonUtils.isShowHorizontalProgressBar(mResultInfo.getBaseTaskType())) {
                if (mApplication.mProgressDialog != null) {
                    if (ProgressPopupWindow.getCreateTaskTime() == mResultInfo.getCreateTaskTime()) {
                        mApplication.mProgressDialog.hide();
                    }
                    RunningTaskMap.removeRunningTask(mCreateTime);
                    NotificationMap.removeNotification(mCreateTime);
                    TaskInfoMap.removeTaskInfo(mCreateTime);
                    if (mApplication.mNotiManager != null && mApplication.mNotiManager.getActiveNotifications().length > 0) {
                        mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_ALL_SHOW_MODE;
                    } else {
                        mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
                    }
                }
                ExcuteTaskMap.removeFinishTask(mCreateTime);
            }
        } else {
            if (mApplication.mProgressDialog != null) {
                mApplication.mProgressDialog.hide();
            }
        }
    }

    private void updateSearchResultItem(String query) {
        int resultCount = mApplication.mFileInfoManager.getSearchItemsCount();
        if (resultCount == 0) {
            showNoSearchResults(true, query);
        } else {
            showNoSearchResults(false, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(CommonIdentity.CATEGROY_DELETE_NOT_EXIT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mPosStack.empty()) {
            mPosStack.clear();
        }
        if (fileInfoEdit != null) {
            fileInfoEdit = null;
        }

        // close pop menu
        closeItemMorePop();
    }

    protected boolean mIsHasDirctory;
    protected boolean mIsHasDrm;
    protected boolean mCanShare;
    protected boolean mHasAllPrivate;
    protected boolean mIsFLorSDDrm;

    private void updateSelectedFilter(List<FileInfo> files) {
        mIsHasDirctory = false;
        mIsHasDrm = false;
        mCanShare = true;
        mHasAllPrivate = true;
        mIsFLorSDDrm = true;
        for (FileInfo info : files) {
            boolean isFLorSD = false;
            boolean isDrm = info.isDrmFile() || info.isDrm();
            if(isDrm) {
                isFLorSD = DrmManager.getInstance(mActivity).isDrmSDFile(info.getFileAbsolutePath());
            }
            if (!mIsHasDirctory && info.isDirectory()) {
                mIsHasDirctory = true;
                mCanShare = false;
                mHasAllPrivate = false;
                mIsFLorSDDrm = false;
            }
            if (mHasAllPrivate) {
                mHasAllPrivate = info.isPrivateFile();
            }
            if ((!mIsHasDrm || mCanShare) && isDrm) {
                mIsHasDrm = true;
                mCanShare = false;
            }
            if(mIsFLorSDDrm && isDrm && !isFLorSD){
                mIsFLorSDDrm = false;
            }
            if (mIsHasDirctory && mIsHasDrm && !mCanShare) {
                break;
            }

        }
    }

    private void storeLastListPos() {
        mIsBack = false;
        int index = mAbsListView.getFirstVisiblePosition();
        View v = mAbsListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        Pos lastPos = new Pos();
        lastPos.index = index;
        lastPos.top = top;
        mPosStack.push(lastPos);
    }

    private void refreshPathBar() {
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateNormalBarView();
            mAbsListViewFragmentListener.updateBarTitle(CommonIdentity.UNKNOWN_TASK);
        }
    }

    public void clearAll() {
        mPosStack.clear();
    }

    private void showSearchResultView() {
        switchToSearchView();

        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.reSearch();
        }
        mAdapter.clearChecked();
    }

    private void switchToSearchView() {
        mSearchMode = true;
        mLastSearchText = "";
        updateActionMode(CommonIdentity.FILE_STATUS_SEARCH);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateBarView();
        }
    }

    private void switchToGlobalSearchView() {
        mSearchMode = true;
        mLastSearchText = "";
        updateActionMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH);
//        if (mAbsListViewFragmentListener != null) {
//            mAbsListViewFragmentListener.updateBarView();
//        }
    }

    private void switchToNormalView() {
        Log.i(TAG, "switchToNormalView");
        mSearchMode = false;
        if (mAdapter != null) {
            mAdapter.changeModeFromSearchToNormal();
        }
        updateActionMode(CommonIdentity.FILE_STATUS_NORMAL);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateBarView();
        }
        if(menuMultipleActions != null) {
            menuMultipleActions.setVisibility(View.GONE);
        }
        if(menuMultipleActionsLeft != null) {
            menuMultipleActionsLeft.setVisibility(View.GONE);
        }
    }


    protected void restoreListPosition() {

    }

    public void setViewPostion(int position,boolean isBackPosition) {

    }

    protected void switchToEditView() {
        mSearchMode = false;
        if (mAdapter != null) {
            if (mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)) {
                mFromSearchToEdit = true;
            }
            int count = mAdapter.getCheckedItemsCount();
            updateActionMode(CommonIdentity.FILE_STATUS_EDIT);
        }
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateBarView();
        }
        updateEditBarByThread();
    }

    public void updateActionMode(int mode) {
        mApplication.mCurrentStatus = SharedPreferenceUtils.getPrefsStatus(mApplication);
        SharedPreferenceUtils.changePrefsStatus(mApplication, mode);
        if (mAdapter != null) {
            mAdapter.changeMode(mode);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mSavedState = true;
        super.onSaveInstanceState(outState);
    }


    private void showCreateFolderDialog() {
        EditDialogFragmentBuilder builder = new EditDialogFragmentBuilder();
        String defaultName = getResources().getString(R.string.default_folder_name);
        builder.setDefault(defaultName, 0, true).setDoneTitle(R.string.create_folder)
                .setCancelTitle(R.string.cancel)
                .setTitle(R.string.create_new_folder);
        createFolderDialogFragment = builder.create();
        createFolderDialogFragment.setOnEditTextDoneListener(new CreateFolderListener());
        createFolderDialogFragment.show(getFragmentManager(), CommonIdentity.CREATE_FOLDER_DIALOG_TAG);
    }

    private final class CreateFolderListener implements EditTextDoneListener {

        public void onClick(String text) {
            if (mApplication.mFileInfoManager != null) {
                String dstPath = mApplication.mCurrentPath + MountManager.SEPARATOR + text;
                long mCreateTime = System.currentTimeMillis();
                TaskInfo mCreateFolderTaskinfo = new TaskInfo(mApplication, new HeavyOperationListener(CommonUtils.getListenerInfo(mApplication.getResources().getString(R.string.create_folder).toString(), mCreateTime,
                        CommonIdentity.CREATE_FOLDER_TASK, -1)), CommonIdentity.CREATE_FOLDER_TASK);
                mCreateFolderTaskinfo.setDestPath(dstPath);
                mCreateFolderTaskinfo.setCreateTaskTime(mCreateTime);
                mApplication.mFileInfoManager.addNewTask(mCreateFolderTaskinfo);
            }
        }
    }

    private void showRenameDialog() {
        if(mAdapter == null) {return;}
        FileInfo fileInfo = mAdapter.getFirstCheckedFileInfoItem();
        if (fileInfo == null && mAdapter.getItemEditSelect().size() > 0) {
            fileInfo = mAdapter.getItemEditSelect().get(0);
        }
        int selection = 0;
        if (fileInfo != null) {
            String name = fileInfo.getFileName();
            String fileExtension = FileUtils.getFileExtension(name);
            selection = name.length();
            if (!fileInfo.isDirectory() && fileExtension != null) {
                selection = selection - fileExtension.length() - 1;
            }
            EditDialogFragmentBuilder builder = new EditDialogFragmentBuilder();
            builder.setDefault(name, selection, false).setDoneTitle(R.string.rename)
                    .setCancelTitle(R.string.cancel)
                    .setTitle(R.string.rename);
            renameDialogFragment = builder.create();
            renameDialogFragment.setOnEditTextDoneListener(new RenameDoneListener(fileInfo));
            renameDialogFragment.show(getActivity().getFragmentManager(), CommonIdentity.CREATE_FOLDER_DIALOG_TAG);
        }
    }

    private class RenameDoneListener implements EditTextDoneListener {
        FileInfo mSrcfileInfo;

        public RenameDoneListener(FileInfo srcFile) {
            mSrcfileInfo = srcFile;
        }

        @Override
        public void onClick(String text) {
            String newFilePath = null;
            if (mApplication.mCurrentPath == null || mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH)
                    || mAdapter.isMode(CommonIdentity.FILE_STATUS_EDIT)) {
                String path = mSrcfileInfo.getFileParentPath();
                newFilePath = path + MountManager.SEPARATOR + text;
            } else {
                newFilePath = mApplication.mCurrentPath + MountManager.SEPARATOR + text;
            }
            if (null == mSrcfileInfo) {
                return;
            }

            mIsBack = true;

            if (FileUtils.isStartWithDot(newFilePath,
                    mSrcfileInfo.getFileAbsolutePath())) {
                showCheckFileStartDialog(mSrcfileInfo, newFilePath);
            } else {
                if (FileUtils.isExtensionChange(newFilePath,
                        mSrcfileInfo.getFileAbsolutePath())) {
                    showRenameExtensionDialog(mSrcfileInfo, newFilePath);
                } else {
                    mRenameActivity = null;
                    if (mApplication.mFileInfoManager != null) {
                        if (mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH) || mFromSearchToEdit) {
                            showSearchResultView();
                        } else {
                            mSearchMessage = null;
                            switchToNormalView();
                        }
                        CommonUtils.getBaseTaskInfo(mApplication, CommonIdentity.MODE_NORMAL_RENAME, mApplication.getResources().getString(R.string.rename).toString(), CommonIdentity.RENAME_FILE_TASK, -1, CommonIdentity.MODE_NORMAL_RENAME, mSrcfileInfo,
                                new FileInfo(mApplication, newFilePath), mApplication.mCurrentPath, mSearchMessage, null, false, CategoryManager.mCurrentCagegory);
                    }
                }
            }
        }
    }

    private class RenameExtensionListener implements OnClickListener {

        private final String mNewFilePath;
        private final FileInfo mSrcFile;

        public RenameExtensionListener(FileInfo fileInfo, String newFilePath) {
            mNewFilePath = newFilePath;
            mSrcFile = fileInfo;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mApplication.mFileInfoManager != null) {
                mRenameActivity = null;
                switchToNormalView();
                CommonUtils.getBaseTaskInfo(mApplication, CommonIdentity.MODE_EXTEND_RENAME, mApplication.getResources().getString(R.string.rename).toString(), CommonIdentity.RENAME_FILE_TASK, -1, CommonIdentity.MODE_EXTEND_RENAME, mSrcFile,
                        new FileInfo(getActivity(), mNewFilePath), mApplication.mCurrentPath, mSearchMessage, null, false, CategoryManager.mCurrentCagegory);
                updatePasteBtn();
            }
        }

    }

    private void showCheckFileStartDialog(FileInfo srcfileInfo, final String newFilePath) {
        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        AlertDialogFragment checkFileStartDialogFragment = builder
                .setTitle(R.string.confirm_rename)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(R.string.create_hidden_file)
                .setCancelTitle(R.string.cancel)
                .setDoneTitle(R.string.ok).create();
        checkFileStartDialogFragment.getArguments().putString(CommonIdentity.NEW_FILE_PATH_KEY, newFilePath);
        checkFileStartDialogFragment.setOnDoneListener(new RenameExtensionListener(srcfileInfo, newFilePath));
        checkFileStartDialogFragment.show(getFragmentManager(), CommonIdentity.RENAME_EXTENSION_DIALOG_TAG);
    }

    private void showRenameExtensionDialog(FileInfo srcfileInfo, final String newFilePath) {
        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        renameExtensionDialogFragment = builder
                .setTitle(R.string.confirm_rename)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(R.string.msg_rename_ext)
                .setCancelTitle(R.string.cancel)
                .setDoneTitle(R.string.confirm).create();
        renameExtensionDialogFragment.getArguments().putString(CommonIdentity.NEW_FILE_PATH_KEY, newFilePath);
        renameExtensionDialogFragment.setOnDoneListener(new RenameExtensionListener(srcfileInfo, newFilePath));
        FragmentManager mFragmentManager = getFragmentManager();
        if (mFragmentManager == null) {
            mFragmentManager = mRenameActivity.getFragmentManager();
            mRenameActivity = null;
        }
        renameExtensionDialogFragment.show(mFragmentManager, CommonIdentity.RENAME_EXTENSION_DIALOG_TAG);
    }

    private void share() {
        Intent intent;
        List<FileInfo> files = null;
        ArrayList<Parcelable> sendList = new ArrayList<Parcelable>();
        try {
            if (mAdapter == null) {
                return;
            }
            files = mAdapter.getItemEditFileInfoList();
            if (files.size() > 100) {
                isShareSizeExceed = true;
                showNoShareDialog();
                return;
            } else {
                isShareSizeExceed = false;
            }

            if (files.size() > 1) {
                for (FileInfo info : files) {
                    if ((info.isDrmFile() || info.isDrm()) && !DrmManager.getInstance(mActivity).isDrmSDFile(info.getFileAbsolutePath())) {
                        forbidden = true;
                        break;
                    }
                    sendList.add(info.getUri(false));
                }
                if (!forbidden) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("*/*");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sendList);
                    startActivity(Intent.createChooser(intent, getString(R.string.send_file)));
                }
            } else {
                // send single file
                FileInfo fileInfo = files.get(0);
                File file = new File(fileInfo.getFileAbsolutePath());
                String mimeType = fileInfo.getShareMime();
                if ((fileInfo.isDrmFile() || fileInfo.isDrm()) && !DrmManager.getInstance(mActivity).isDrmSDFile(fileInfo.getFileAbsolutePath())) {
                    forbidden = true;
                }
                if (TextUtils.isEmpty(mimeType) || mimeType.startsWith("unknown")) {
                    mimeType = CommonIdentity.MIMETYPE_UNRECOGNIZED;
                } else if (!TextUtils.isEmpty(mimeType)) {
                    mimeType = FileUtils.getAudioMimeType(mimeType);
                }
                if (!forbidden) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setType(mimeType);
                    Uri uri = fileInfo.getUri(false);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, getString(R.string.send_file)));
                }
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (!isShareSizeExceed) {
            if (forbidden) {
                if (mAbsListViewFragmentListener != null) {
                    mAbsListViewFragmentListener.toShowForbiddenDialog();
                }
            } else {
                if (mFromSearchToEdit) {
                    showSearchResultView();
                } else {
                    switchToNormalView();
                    if (CommonUtils.isCategoryMode()) {
                        refreshAdapter(null, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false, false);
                    } else {
                        refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false, false);
                    }
                }
            }
        }
    }

    private String toDateTimeString(Long sec) {
        Date date = new Date(sec.longValue() * 1000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String str = dateFormat.format(date);
        return str;
    }

    private void onUnmounted() {
        dismissDialog();
        if (mAdapter != null
                && (mAdapter.getMode() == CommonIdentity.FILE_STATUS_EDIT || mAdapter.getMode() == CommonIdentity.FILE_STATUS_SEARCH)) {
            showNoSearchResultView(false, null);
            switchToNormalView();
        }
    }

    private void dismissDialog(){
        if (renameDialogFragment != null && renameDialogFragment.getDialog() != null && renameDialogFragment.getDialog().isShowing()) {
            renameDialogFragment.getDialog().dismiss();
        }
        if (createFolderDialogFragment != null && createFolderDialogFragment.getDialog() != null && createFolderDialogFragment.getDialog().isShowing()) {
            createFolderDialogFragment.getDialog().dismiss();
        }

        if (mDetailDialog != null && mDetailDialog.getDialog() != null && mDetailDialog.getDialog().isShowing()) {
            mDetailDialog.getDialog().dismiss();
        }

        if (mDeleteDialog != null && mDeleteDialog.getDialog() != null && mDeleteDialog.getDialog().isShowing()) {
            mDeleteDialog.getDialog().dismiss();
        }

        if (mComPressDialog != null && mComPressDialog.getDialog() != null && mComPressDialog.getDialog().isShowing()) {
            mComPressDialog.getDialog().dismiss();
        }

        if (mExtractDialog != null && mExtractDialog.getDialog() != null && mExtractDialog.getDialog().isShowing()) {
            mExtractDialog.getDialog().dismiss();
        }

        if(renameExtensionDialogFragment != null && renameExtensionDialogFragment.getDialog() != null && renameExtensionDialogFragment.getDialog().isShowing()) {
            renameExtensionDialogFragment.getDialog().dismiss();
        }
    }

    public void updateEditBarState() {
        updateEditBarByThread();
    }

    private int getAdapterCount() {
        if (mAdapter != null) {
            return mAdapter.getItemCount();
        }
        return 0;
    }

    private void selectAllBtnClicked() {
        if (!mAdapter.isAllItemChecked()) {
            mApplication.currentOperation = CommonIdentity.SELECT_ALL;
            mAdapter.setAllItemChecked(true);
        } else {
            mApplication.currentOperation = CommonIdentity.OTHER;
            mAdapter.setAllItemChecked(false);
        }
    }

    private void selectDoneBtnClicked() {
        mAdapter.setAllItemChecked(false);
        if (mFromSearchToEdit) {
            showSearchResultView();
        } else {
            switchToNormalView();
        }
    }

    private void detailsBtnClicked() {
        if (mAdapter.getItemEditFileInfoList().size() == 0) return;
        FileInfo info = mAdapter.getItemEditFileInfoList().get(0);
        if (info.isDrmFile() || info.isDrm()) {
            CommonUtils.getBaseTaskInfo(mApplication, -1, -1, CommonIdentity.MODE_DRM_DETAIL, mApplication.getResources().getString(R.string.detail).toString(),
                    CommonIdentity.DETAIL_FILE_TASK, -1, null, info.getFileAbsolutePath(), -1, false, info);
        } else {
            CommonUtils.getBaseTaskInfo(mApplication, -1, -1, CommonIdentity.MODE_NORMAL_DETAIL, mApplication.getResources().getString(R.string.detail).toString(),
                    CommonIdentity.DETAIL_FILE_TASK, -1, null, null, -1, false, info);
        }
    }

    private void copyBtnClicked() {
        mApplication.mFileInfoManager.savePasteList(CategoryManager.mCurrentCagegory, CommonIdentity.PASTE_COPY_TASK,
                mAdapter.getItemEditFileInfoList());
        if (mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH) && mApplication.mFileInfoManager.getPasteCount() == 0) {
            showSearchResultView();
        } else {
            switchToCopyView();
        }
    }

    private void shortcutBtnClicked() {

        try {

            /* MODIFIED-BEGIN by caiminjie, 2017-09-12,BUG-5325137*/
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.setClass(mActivity, mActivity.getClass());
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launcherIntent.putExtra("foldername", mAdapter.getItemEditFileInfoList().get(0)
                    .getFileAbsolutePath());
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {

                ShortcutManager mShortcutManager = mActivity.getSystemService(ShortcutManager.class);

                if (mShortcutManager.isRequestPinShortcutSupported()) {
                    ShortcutInfo pinShortInfo = new ShortcutInfo.Builder(mActivity, mAdapter.getItemEditFileInfoList().get(0).getFileAbsolutePath())
                            .setShortLabel(mAdapter.getItemEditFileInfoList().get(0).getFileName())
                            .setIcon(Icon.createWithResource(mActivity, R.drawable.ic_launcher_shortcut))
                            .setIntent(launcherIntent)
                            .build();
                    Intent intent = mShortcutManager.createShortcutResultIntent(pinShortInfo);

                    PendingIntent successCallback = PendingIntent.getBroadcast(mActivity, 0, intent, 0);
                    mShortcutManager.requestPinShortcut(pinShortInfo, successCallback.getIntentSender());
                }

            } else {
            /* MODIFIED-END by caiminjie,BUG-5325137*/
            Intent addShortcutIntent = new Intent(CommonIdentity.ACTION_ADD_SHORTCUT);
            addShortcutIntent.putExtra("duplicate", false);
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mAdapter.getItemEditFileInfoList()
                    .get(0).getFileName());
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(mActivity,
                            R.drawable.ic_launcher_shortcut));

            addShortcutIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

            mActivity.sendBroadcast(addShortcutIntent);
            } // MODIFIED by caiminjie, 2017-09-12,BUG-5325137
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void switchToCopyView() {
        mSearchMode = false;
        if (mAdapter != null) {
            int count = mAdapter.getCheckedItemsCount();
            updateActionMode(CommonIdentity.FILE_COPY_NORMAL);
        }
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateBarView();
        }
    }

    @Override
    public boolean checkIsSelectAll() {
        return isSelectAll();
    }

    private boolean isSelectAll() {
        if (mAdapter.isAllItemChecked()) {
            return true;
        }
        return false;
    }

    private void cutBtnClicked() {
        mApplication.mFileInfoManager.savePasteList(CategoryManager.mCurrentCagegory, CommonIdentity.PASTE_CUT_TASK,
                mAdapter.getItemEditFileInfoList());
        if (mAdapter.isMode(CommonIdentity.FILE_STATUS_SEARCH) && mApplication.mFileInfoManager.getPasteCount() == 0) {
            showSearchResultView();
        } else {
            switchToCopyView();
        }
    }

    private void pasteBtnClicked() {
        if (mApplication.mFileInfoManager != null) {
            CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.paste).toString(), mApplication.mFileInfoManager.getPasteType(), -1, -1, null, null, mApplication.mCurrentPath, null, mApplication.mFileInfoManager.getPasteList(), false, mApplication.mFileInfoManager.getCategory());
            switchToNormalWhenCreateTask(mApplication.mFileInfoManager.getPasteType(), mApplication.mFileInfoManager.getPasteList());
            // TODO
            updateActionMode(CommonIdentity.FILE_STATUS_NORMAL);
            if (mApplication.mProgressDialog != null && !mApplication.mProgressDialog.isShowing()) {
                mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_ALL_SHOW_MODE;
                mApplication.mProgressDialog.show();
            }
        }

    }

    private void updatePasteBtn() {
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.pasteBtnUpdated();
        }
    }

    private void showNoShareDialog() {
        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        AlertDialogFragment deleteDialogFragment = builder.setMessage(
                R.string.share_warning).setDoneTitle(R.string.ok)
                .setTitle(R.string.warning).create();
        deleteDialogFragment.show(getFragmentManager(), CommonIdentity.DELETE_DIALOG_TAG);
    }

    private OnClickListener mCancelListner = new OnClickListener() {

        public void onClick(DialogInterface dlg, int sumthin) {
            if (menuMultipleActions != null && mAdapter != null && mAdapter.getCheckedItemsCount() > 0 &&
                    (Build.VERSION.SDK_INT < 23 || (Build.VERSION.SDK_INT >= 24 && !getActivity().isInMultiWindowMode()))) {
                menuMultipleActions.setVisibility(View.VISIBLE);
            }
        }
    };

    private void showDeleteDialog(int dMode) {
        if (!isAdded() || mAdapter.getItemEditFileInfoList().size() == 0) {
            return;
        }
        deleteMode = dMode;
        FileInfo fileInfo = mAdapter.getFirstCheckedFileInfoItem();
        String name = null;
        String deleteMessage = null;
        boolean isNormal = false;
        if (fileInfo == null && mAdapter.getItemEditSelect().size() > 0) {
            isNormal = true;
            fileInfo = mAdapter.getItemEditSelect().get(0);
        }
        if (fileInfo != null) {
            name = fileInfo.getFileName();
        }

        SpannableString spannableString = new SpannableString(name);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int alertMsgId = R.string.alert_delete_multiple;
        if (mAdapter.getCheckedItemsCount() == 1 || isNormal) {
            deleteMessage = getResources().getString(R.string.delete) + " " + spannableString + "?";
        } else {
            deleteMessage = getResources().getString(R.string.multi_alert_delete_message);
        }
//        mDeleteDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.delete).setMessage(
//                deleteMessage).setPositiveButton(R.string.delete, new DeleteListener()).setNegativeButton(
//                R.string.cancel, mCancelListner).show();
        mDeleteDialog = CommonDialogFragment.getInstance(getFragmentManager(), deleteMessage, CommonIdentity.DELETE_DIALOG_TAG);
//        WindowManager.LayoutParams lp = mDeleteDialog.getWindow().getAttributes();
//        lp.width = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.delete_dialog_width);
//        mDeleteDialog.getWindow().setAttributes(lp);
        //setShowAlertDialog(mDeleteDialog);
        mDeleteDialog.showDialog();
    }

    public class DeleteListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (mApplication.mFileInfoManager != null) {
                CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.delete).toString(),
                        deleteMode, CategoryManager.mCurrentCagegory, null, -1, mAdapter.getItemEditFileInfoList(), mApplication.mCurrentPath, null);
                switchToNormalWhenCreateTask(CommonIdentity.DETAIL_FILE_TASK, mAdapter.getItemEditFileInfoList());
            }
        }
    }

    private void releaseWakeLock() {
        if (RunningTaskMap.getRunningTaskSize() == 0 && WaittingTaskList.getWaittingTaskSize() == 0
                && mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.toReleaseWakeLock();
        }
    }

    /**
     * Get the WindowManager
     */
    private WindowManager getWindowManager() {
        if (null == mWindowManager) {
            mWindowManager = (WindowManager) mActivity.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }


    private int dpToPx(float dp) {
        float scale = mActivity.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected class SelectedItemInfo {
        int count;
        boolean hasDirectory;
        boolean hasDrm;
        boolean canShare;
        boolean hasZip;
        boolean hasPrivate;
        boolean isFLorSDDrm;
    }

    private boolean isShareSizeExceed;
    private boolean forbidden;
    private boolean mFromSearchToEdit;
    private int firstIndex, lastIndex;
    private AlertDialog mLoadingDialog;
    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonIdentity.MSG_DIALOG_SHOW:
                    if (mLoadingDialog == null) {
                        TaskInfo mResultInfo = CommonUtils.getListenerInfo(getString(R.string.wait), System.currentTimeMillis(), CommonIdentity.LIST_INFO_TASK, CommonIdentity.FILE_STATUS_SELECT);
                        mLoadingDialog = ProgressPopupWindow.newInstance(mActivity, mResultInfo);
                        mLoadingDialog.setCancelable(false);
                    }
                    if (!mLoadingDialog.isShowing()) {
                        mLoadingDialog.show();
                    }
                    break;
                case CommonIdentity.MSG_REFRESH_UI:
                    if (!isAdded()) {
                        // if Fragment not attached with activity, just return
                        return;
                    }
                    SelectedItemInfo info = (SelectedItemInfo) msg.obj;
                    if (mAbsListViewFragmentListener != null) {
                        mAbsListViewFragmentListener.updateEditBar(info.count, info.hasDirectory, info.hasZip, info.hasDrm, info.canShare, info.hasPrivate,info.isFLorSDDrm);
                        if (!CommonUtils.isPrivateLocation(mApplication)) {
                            showFloatMenu(info.count, info.hasDirectory, info.hasZip);
                        }
                    }
                    if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
            }
        }
    };

    protected AbsListView mAbsListView;
    protected AbsListViewFragmentListener mAbsListViewFragmentListener;
    private int i = 0;

    public interface AbsListViewFragmentListener {
        public void updateEditBar(int count, boolean isHasDir, boolean isHasZip, boolean isHasDrm, boolean canShare, boolean isHasAllPrivate, boolean isFLorSDDrm);

        public void reSearch();

        public void setFileActionMode(int mode);

        public int getFileActionMode();

        public void updateBarView();

        public void updateNormalBarView();

        public void changeSearchMode(boolean flag);

        public void toShowForbiddenDialog();

        public void pasteBtnUpdated();

        public void toReleaseWakeLock();

        public void HideActionbar(boolean flag);

        public void isDeleteFlag(boolean flag);

        public int getSlideLimite();

        public void refreashSafeFilesCategory();

        public void showBottomView(String message);

        public void updateBarTitle(int mTaskType);

        public void cancelScrollActionbar();

        public void enableScrollActionbar();

        public void updateSearch(String mSearchContent);

    }

    protected void switchToEditView(int position, int top, boolean flag) {
        mAdapter.setChecked(position, flag);
    }

    protected void updateEditBarByThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mAdapter == null) {
                    return;
                }
                if (mSelectAll) {
                    mUpdateHandler.removeMessages(CommonIdentity.MSG_DIALOG_SHOW);
                    mUpdateHandler.sendEmptyMessage(CommonIdentity.MSG_DIALOG_SHOW);
                }
                infos = mAdapter.getCheckedFileInfoItemsList();
                updateSelectedFilter(infos);
                setShowFragmentDialog(null);
                mSelectAll = false;
                SelectedItemInfo info = new SelectedItemInfo();
                info.count = infos.size();
                info.hasDirectory = mIsHasDirctory;
                info.hasDrm = mIsHasDrm;
                info.canShare = mCanShare;
                info.hasPrivate = mHasAllPrivate;
                info.isFLorSDDrm = mIsFLorSDDrm;
                if (info.count == 1) {
                    info.hasZip = infos.get(0).isZip();
                }
                mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(CommonIdentity.MSG_REFRESH_UI, info));
            }
        }).start();
    }

    @Override
    public void clickEditBtn() {
        switchToEditView(1, 0, false);
    }

    @Override
    public void clickNewFolderBtn() {
        showCreateFolderDialog();
    }

    @Override
    public void clickSearchBtn() {
        switchToSearchView();
    }

    @Override
    public void clickGlobalSearchBtn() {
        switchToGlobalSearchView();
    }

    @Override
    public void clickPasteBtn() {
        pasteBtnClicked();
    }

    @Override
    public void clickDelteBtn(int mode) {
        showDeleteDialog(mode);
    }

    @Override
    public void clickCopyBtn() {
        copyBtnClicked();
    }

    @Override
    public void clickMigrateBtn() {
    }

    @Override
    public void clickShortcutBtn() {
        shortcutBtnClicked();
    }

    @Override
    public void clickCutBtn() {
        cutBtnClicked();
    }

    @Override
    public void clickShareBtn() {
        share();
        if (menuMultipleActions != null) {
            menuMultipleActions.setVisibility(View.GONE);
        }
        if (menuMultipleActionsLeft != null) {
            menuMultipleActionsLeft.setVisibility(View.GONE);
        }
    }

    @Override
    public void clickRenameBtn(String mSearchtext) {
        mSearchMessage = mSearchtext;
        showRenameDialog();
    }

    @Override
    public void clickDetailsBtn() {
        detailsBtnClicked();
    }

    @Override
    public void clickSelectAllBtn() {
        mSelectAll = true;
        selectAllBtnClicked();
        updateEditBarByThread();
    }


    @Override
    public void onBackPressed() {
        if(mApplication == null){
            mApplication = (FileManagerApplication)FileManagerApplication.getInstance();
        }
        if (mApplication != null && mApplication.mProgressDialog != null && mApplication.mProgressDialog.isShowing()) {
            mApplication.mProgressDialog.hide();
        }
        onBackPress();
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.updateBarView();
        }
    }

    @Override
    public void clearAdapter() {
        if (mAdapter != null) {
            mLastSearchText = "";
            mAdapter.clearList();
        }
    }

    @Override
    public void showNoSearchResults(boolean isShow, String args) {
        showNoSearchResultView(isShow, args);
    }

    @Override
    public void unMountUpdate() {
        onUnmounted();
    }

    @Override
    public int getAdapterSize() {
        return getAdapterCount();
    }

    @Override
    public void showBeforeSearchList() {
        showBeforeSearchContent();
    }

    @Override
    public void onConfiguarationChanged() {
    }

    @Override
    public void onScannerStarted() {

    }

    @Override
    public void onScannerFinished() {
        if (mApplication != null && mApplication.mFileInfoManager != null && isDataChanged) {
            CategoryManager.mLastCagegory = -2;
            if (CategoryManager.mCurrentCagegory != CommonIdentity.CATEGORY_PICTURES
                    && CategoryManager.mCurrentCagegory != CommonIdentity.CATEGORY_MUSIC
                    && CategoryManager.mCurrentCagegory != CommonIdentity.CATEGORY_VEDIOS) {
                return;
            }
            refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, mApplication.mCurrentLocation,
                    CommonUtils.getCategoryRefreshMode(mApplication.mCurrentLocation), false,false);
            mApplication.currentOperation = CommonIdentity.OTHER;
        }
    }

    private void startActivityOpenFile(FileInfo fileInfo,boolean isDrm) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String path = fileInfo.getFileAbsolutePath();
        String mimeType = fileInfo.getMime();
        Uri uri = null;
        /**
          Control to open the file generated by the default URI
         */
        if(mApplication.isMediaURI){
            uri = FileUtils.getMediaContentUri(fileInfo.getFile(), mApplication.mFileInfoManager, mimeType);
            intent.putExtra("isFiles",true);
        } else {
            uri = FileUtils.getContentUri(fileInfo.getFile(), mApplication.mFileInfoManager, mimeType, isDrm);
        }
        mimeType = mimeType.toLowerCase();
        LogUtils.d(TAG, "Open uri file: " + uri + " mimeType=" + mimeType);
        intent.setDataAndType(uri, FileUtils.getAudioMimeType(mimeType));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            SafeManager.notQuitSafe = true;
            startActivity(intent);
            fileInfoEdit = fileInfo;
        } catch (NullPointerException e) {
            e.printStackTrace();
            SafeManager.notQuitSafe = false;
            Message message = mHandler
                    .obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
            mHandler.sendMessage(message);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "Open uri file: " + uri + " mimeType=" + mimeType + "==exception e==>" + e);
            SafeManager.notQuitSafe = false;
            Message message = mHandler
                    .obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            SafeManager.notQuitSafe = false;

            Message message = mHandler
                    .obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
            mHandler.sendMessage(message);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isAdded()) {
                // if Fragment not attached with activity, just return
                return;
            }

            switch (msg.what) {
                case 0:
                    showDrmWifidisplyDiaog(mActivity);
                    break;

                case 1:
                    startActivityOpenFile((FileInfo) msg.obj,false);
                    break;
                case 5:
                    startActivityOpenFile((FileInfo) msg.obj,true);
                    break;
                case 2:
                    mToastHelper.showToast((Integer) msg.obj);
                    break;

                case 3:
                    try {
                        final String drmRefreshPath = (String) msg.obj;
                        Dialog drmRefreshDialog = DrmManager.getInstance(getActivity().getApplicationContext()).showRefreshLicenseDialog(mActivity, drmRefreshPath);
                        if (drmRefreshDialog != null) {
                            drmRefreshDialog.setOnDismissListener(new OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    if (!TextUtils.isEmpty(drmRefreshPath)) {
                                        File file = new File(drmRefreshPath);
                                        if (!file.exists()) {
                                            reloadContent();
                                        }
                                    }
                                }
                            });
                        } else {
                            mToastHelper.showToast(getActivity().getString(R.string.drm_toast_license_expired));
                            if (!TextUtils.isEmpty(drmRefreshPath)) {
                                File file = new File(drmRefreshPath);
                                if (!file.exists()) {
                                    reloadContent();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CommonIdentity.CATEGROY_DELETE_NOT_EXIT:
                    if (fileInfoEdit != null && CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && !fileInfoEdit.getFile().exists()) {
                        deleteNotExistFiles(fileInfoEdit.getFileAbsolutePath());
                    }
                    fileInfoEdit = null;
                    if (isOpenCDDrmFile && mApplication != null && CommonUtils.isNormalStatus(mApplication) && CommonUtils.isFilePathLocation(mApplication)) {
                        refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false, false);
                    }
                   isOpenCDDrmFile = false;
                default:
                    break;
            }
        }

    };


    private void reloadContent() {
        if (mApplication.mFileInfoManager != null && mApplication.mFileInfoManager.isPathModified(mApplication.mCurrentPath)) {
            if (!mSearchMode) {
                refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory),
                        CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
            }

        } else if (mApplication.mFileInfoManager != null && mAdapter != null && mApplication.mCurrentPath != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        int len = mAdapter.getItemCount();
                        for (int i = 0; i < len; i++) {
                            if (mAdapter.getItem(i).isDrm()) {

                                mTaskResultHandler.sendMessage(mTaskResultHandler.obtainMessage(0));
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(runnable).start();
        }

    }

    boolean isOpenCDDrmFile = false;
    private class OpenFileThread extends Thread {

        FileInfo mFileInfo;

        public OpenFileThread(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            super.run();
            boolean canOpen = true;
            mDisplayManager = (DisplayManager) mActivity.getSystemService(Context.DISPLAY_SERVICE);
            WifiDisplayStatus mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
            String mimeType = mFileInfo.getMime();
            ContentResolver resolver = mActivity.getContentResolver();
            int HDCP_ENABLE = 0;
            try {
                HDCP_ENABLE = Settings.Global.getInt(resolver,
                        CommonIdentity.TCT_HDCP_DRM_NOTIFY, 0);
            } catch (Exception e) {
                HDCP_ENABLE = 0;
            }
            if (CommonIdentity.MIMETYPE_EXTENSION_UNKONW.equals(mimeType)) {
                Message message = mHandler.obtainMessage(2, 1, 11, R.string.msg_unable_open_file_in_app);
                mHandler.sendMessage(message);
                return;
            }
            boolean isDrmFile = mFileInfo.isDrmFile() || mFileInfo.isDrm();
            if (isDrmFile) {
                String drmPath = mFileInfo.getFileAbsolutePath();
                if (!DrmManager.getInstance(getActivity().getApplicationContext()).isRightsStatus(drmPath)) {
                    canOpen = false;
                    if (DrmManager.mCurrentDrm == 10) {
                        Message message = mHandler.obtainMessage(3, 1, 11, drmPath);
                        mHandler.sendMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage(2, 1, 11, R.string.drm_no_valid_right);
                        mHandler.sendMessage(message);
                    }

                    if (mWifiDisplayStatus.getActiveDisplayState() == 2 && (0 == HDCP_ENABLE)) {
                        mHandler.sendEmptyMessage(0);
                    }
                } else if (DrmManager.getInstance(getActivity().getApplicationContext()).isDrmCDFile(drmPath)) {
                    isOpenCDDrmFile = true;
                    IconManager.getInstance().removeCache(drmPath);
                }
            }

            if (canOpen) {
                if ((mWifiDisplayStatus.getActiveDisplayState() == 2) && (0 == HDCP_ENABLE) && isDrmFile) {
                    mHandler.sendEmptyMessage(0);
                } else if(isDrmFile){
                    Message message = mHandler.obtainMessage(5, 1, 11, mFileInfo);
                    mHandler.sendMessage(message);
                } else {
                    Message message = mHandler.obtainMessage(1, 1, 11, mFileInfo);
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    protected void openFile(FileInfo mFileInfo) {
        new OpenFileThread(mFileInfo).start();
    }

    public void showNoFolderResultView(boolean isShow) {
        // If the fragment is visible, just return
        if (!isVisible()) return;
        if (isShow) {
            if (CommonUtils.isCategoryMode()) {
                if (mNo_messageView != null) {
                    mNo_messageView.setText(mActivity.getResources().getString(R.string.no_category));
                }
                if (mNoFolderView != null) {
                    mNoFolderView.setVisibility(View.VISIBLE);
                    if (CommonUtils.isInPrivacyMode(mActivity) && mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION) {
                        mNo_ImageView.setImageResource(R.drawable.ic_gray_empty);
                    } else {
                        mNo_ImageView.setImageResource(R.drawable.ic_empty);
                    }
                }
            } else {
                if (mNo_messageView != null) {
                    mNo_messageView.setText(mApplication.getResources().getString(R.string.no_folder));
                }
                if (mNoFolderView != null) {
                    mNoFolderView.setVisibility(View.VISIBLE);
                    if (CommonUtils.isInPrivacyMode(mActivity) && mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION) {
                        mNo_ImageView.setImageResource(R.drawable.ic_gray_empty);
                    } else {
                        mNo_ImageView.setImageResource(R.drawable.ic_empty);
                    }
                    // When entering the empty directory, the prohibition of sliding hidden Actionbar
                    if (mAbsListViewFragmentListener != null) {
                        mAbsListViewFragmentListener.cancelScrollActionbar();
                    }
                }
            }
        } else {
            if (mNoFolderView != null) {
                mNoFolderView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showNoFolderResults(boolean isShow) {
        showNoFolderResultView(isShow);
    }

    @Override
    public void closeItemMorePop() {
        if (mAdapter != null && mAdapter.mItemMorePop != null) {
            mAdapter.mItemMorePop.dismiss();
        }
    }

    @Override
    public boolean isItemMorePop() {
        if (mAdapter != null && mAdapter.mItemMorePop != null) {
            return mAdapter.mItemMorePop.isShowing();
        }
        return false;
    }

    @Override
    public void clearChecked() {
        if(mAdapter != null) {
            mAdapter.clearChecked();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void clickShortcutToNormal() {
        showNoSearchResultView(false, null);
        /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-06-28,BUG-4967133*/
        if(mApplication != null && mApplication.mFileInfoManager != null){
            mApplication.mFileInfoManager.clearPasteList();
        }
        /* MODIFIED-END by Chuanzhi.Shao,BUG-4967133*/
        switchToNormalView();
    }

    private void deleteNotExistFiles(String file) {
        deleteMediaStoreHelper.addRecord(file);
        deleteMediaStoreHelper.updateRecords();
    }


    @Override
    public List<FileInfo> saveSelectedList() {
        if (mAdapter == null) return new ArrayList<>();
        return mAdapter.getItemEditFileInfoList();
    }


    @Override
    public void managerTaskResult(TaskInfo mTaskInfo) {
        int mTaskType = mTaskInfo.getBaseTaskType();
        switch (mTaskType) {
            case CommonIdentity.PROGRESS_SHOW_TASK:
                mTaskType = mTaskInfo.getProgressInfo().getProgressTaskType();
                switch (mTaskType) {
                    case CommonIdentity.NORMAL_DELETE_TASK:
                    case CommonIdentity.PASTE_COPY_TASK:
                    case CommonIdentity.PASTE_CUT_TASK:
                    case CommonIdentity.FILE_COMPRESSION_TASK:
                    case CommonIdentity.FILE_UNCOMPRESSION_TASK:
                    case CommonIdentity.ADD_PRIVATE_FILE_TASK:
                    case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                        Message msg1 = new Message();
                        Bundle mBundle1 = new Bundle();
                        mBundle1.putSerializable(CommonIdentity.RESULT_TASK_KEY, mTaskInfo);
                        msg1.setData(mBundle1);
                        mTaskResultHandler.sendMessage(msg1);
                        break;
                }

                break;
            case CommonIdentity.CREATE_FOLDER_TASK:
            case CommonIdentity.RENAME_FILE_TASK:
            case CommonIdentity.SEARCH_INFO_TASK:
            case CommonIdentity.STORAGE_SPACE_TASK:
            case CommonIdentity.DETAIL_FILE_TASK:
            case CommonIdentity.UPDATE_PERCENTAGEBAR_TASK:
            case CommonIdentity.LIST_INFO_TASK:
            case CommonIdentity.PROGRESS_DIALOG_TASK:
            case CommonIdentity.OBSERVER_UPDATE_TASK:
                Message msg = new Message();
                Bundle mBundle = new Bundle();
                mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mTaskInfo);
                msg.setData(mBundle);
                mTaskResultHandler.sendMessage(msg);
                break;
            case CommonIdentity.NORMAL_DELETE_TASK:
            case CommonIdentity.PASTE_COPY_TASK:
            case CommonIdentity.PASTE_CUT_TASK:
            case CommonIdentity.FILE_COMPRESSION_TASK:
            case CommonIdentity.FILE_UNCOMPRESSION_TASK:
            case CommonIdentity.ADD_PRIVATE_FILE_TASK:
            case CommonIdentity.REMOVE_PRIVATE_FILE_TASK:
                Message queuemsg = new Message();
                Bundle mQueueBundle = new Bundle();
                mQueueBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, mTaskInfo);
                queuemsg.setData(mQueueBundle);
                mTaskResultHandler.sendMessage(queuemsg);
                break;
        }

    }


    public void showFileDetailInfo(TaskInfo info) {
        TaskInfo mDetailTaskInfo = info;
        FileInfo mFileInfo = info.getSrcFile();
        int fileType = info.getAdapterMode();
        TextView mDetailsText;
        String mName;
        String mSize;
        String mModifiedTime;
        String mPermission;
        long time;
        StringBuilder mStringBuilder = new StringBuilder();

        boolean isDrm = false;
        String mLicesesIssuerURL = "";
        String mCuttentConstraint = "";
        String mCurrentCount = "";
        String mDetailName;
        String mDetailSize;
        String mDetailPath;
        String mDetailModifyTime;
        String mDetailPermission = "";
        String mDrmCurrentCount = "";
        String mDrmCurrentConstraint = "";
        String mDrmLicesesIssuerURL = "";
        String mProtectionStatus = "";
        String mLicenseStartTime = "";
        String mLicenseEndTime = "";
        String mRemains = "";
        boolean isRightValid = false;
        int schema = 0;
        String path = info.getDestPath();
        int mAction = -1;

        mStringBuilder.setLength(0);
        mName = mStringBuilder.append(getString(R.string.detail_name_m))
                .append(mFileInfo.getFileName()).append("\n")
                .toString();
        mStringBuilder.setLength(0);
        mSize = mStringBuilder.append(getString(R.string.detail_size_m))
                .append(FileUtils.sizeToString(getActivity(), 0)).append(
                        " \n").toString();

        time = mFileInfo.getFileLastModifiedTime();

        mStringBuilder.setLength(0);
        mModifiedTime = mStringBuilder.append(
                getString(R.string.modified_time)).append(
                DateFormat.getDateInstance().format(new Date(time)))
                .append("\n").toString();
        mStringBuilder.setLength(0);
        mPermission = getPermission(mStringBuilder, mFileInfo.getFile());
        mDetailName = mFileInfo.getFileName();
        mDetailPath = getDetailPath(mFileInfo.getFileAbsolutePath());
        mDetailModifyTime = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(new Date(time));
        File mSourceFile = mFileInfo.getFile();
        if (mSourceFile.canRead() && mSourceFile.canWrite()) {
            mDetailPermission = getString(R.string.readable_m) + " & " + getString(R.string.writable);
        } else if (!mSourceFile.canRead() && !mSourceFile.canWrite()) {
            mDetailPermission = getString(R.string.no);
        } else if (mSourceFile.canRead()) {
            mDetailPermission = getString(R.string.readable_m);
        } else if (mSourceFile.canWrite()) {
            mDetailPermission = getString(R.string.writable_m);
        }

        isDrm = mFileInfo.isDrmFile() || mFileInfo.isDrm();
        if (fileType == CommonIdentity.MODE_DRM_DETAIL) {
            String mMimeType = mFileInfo.getMime();
            isRightValid = DrmManager.getInstance(getActivity().getApplicationContext()).isRightsStatus(path);
            schema = DrmManager.getInstance(getActivity().getApplicationContext()).getDrmScheme(path);
            mAction = DrmManager.getAction(mMimeType);
        }


        if (DrmManager.mCurrentDrm == 20) {
            if (schema == DrmManager.DRM_SCHEME_OMA1_FL) {
                if (isRightValid) {
                    mCurrentCount = mStringBuilder
                            .append(getString(R.string.drm_current_count))
                            .append(": ")
                            .append(getString(R.string.unlimited_usage))
                            .append("\n").toString();
                    mDrmCurrentCount = getString(R.string.unlimited_usage);
                    mStringBuilder.setLength(0);
                    mCuttentConstraint = mStringBuilder
                            .append(getString(R.string.drm_current_right))
                            .append(": ")
                            .append(getString(R.string.unlimited_usage))
                            .append("\n").toString();
                    mDrmCurrentConstraint = getString(R.string.unlimited_usage);
                } else {
                    mCurrentCount = mStringBuilder
                            .append(getString(R.string.drm_current_count))
                            .append(": ")
                            .append(getString(R.string.not_available))
                            .append("\n").toString();
                    mDrmCurrentCount = getString(R.string.not_available);
                    mStringBuilder.setLength(0);
                    mCuttentConstraint = mStringBuilder
                            .append(getString(R.string.drm_current_right))
                            .append(": ")
                            .append(getString(R.string.not_available))
                            .append("\n").toString();
                    mDrmCurrentConstraint = getString(R.string.not_available);
                }
                mStringBuilder.setLength(0);
            } else if (schema == DrmManager.DRM_SCHEME_OMA1_CD
                    || schema == DrmManager.DRM_SCHEME_OMA1_SD
                    || schema == DrmManager.METHOD_SD) {// for CD && SD type
                if (schema == DrmManager.DRM_SCHEME_OMA1_SD
                        || schema == DrmManager.METHOD_SD) {// for SD only
                    ContentValues contentValue = DrmManager.getInstance(getActivity().getApplicationContext())
                            .getMetadata(path);
                    if (contentValue != null) {
                        mLicesesIssuerURL = mStringBuilder
                                .append(getString(R.string.right_url))
                                .append(": ")
                                .append(contentValue
                                        .getAsString(DrmManager.RIGHTS_ISSUER))
                                .append("\n").toString();
                        mDrmLicesesIssuerURL = contentValue
                                .getAsString(DrmManager.RIGHTS_ISSUER);
                    }
                }
                mStringBuilder.setLength(0);
                if (isRightValid) {
                    ContentValues cv = DrmManager.getInstance(getActivity().getApplicationContext())
                            .getConstraints(path, mAction);
                    if (null != cv) {
                        String constrainType = cv.getAsString(DrmManager.CONSTRAINT_TYPE);
                        if (null == constrainType) {
                            mCuttentConstraint = mStringBuilder
                                    .append(getString(R.string.drm_current_right))
                                    .append(": ")
                                    .append(getString(R.string.not_available))
                                    .append("\n").toString();
                            mStringBuilder.setLength(0);
                            mDrmCurrentConstraint = getString(R.string.not_available);
                        }
                        if (!TextUtils.isEmpty(cv.getAsString(CommonIdentity.REMAINING_REPEAT_COUNT))) {
                            String useTime = null;
                            try {
                                int times = Integer.parseInt(cv.getAsString(CommonIdentity.REMAINING_REPEAT_COUNT));
                                useTime = String.format(getString(R.string.use_times), "" + times);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (null != useTime) {
                                mCurrentCount = mStringBuilder
                                        .append(getString(R.string.drm_current_count))
                                        .append(": ").append(useTime)
                                        .append("\n").toString();
                                mDrmCurrentCount = useTime;
                                mStringBuilder.setLength(0);
                            } else {
                                mCurrentCount = mStringBuilder
                                        .append(getString(R.string.drm_current_count))
                                        .append(": ")
                                        .append(getString(R.string.not_available))
                                        .append("\n").toString();
                                mDrmCurrentCount = getString(R.string.not_available);
                                mStringBuilder.setLength(0);
                            }
                        } else {
                            mCurrentCount = mStringBuilder
                                    .append(getString(R.string.drm_current_count))
                                    .append(": ")
                                    .append(getString(R.string.not_available))
                                    .append("\n").toString();
                            mDrmCurrentCount = getString(R.string.not_available);
                            mStringBuilder.setLength(0);
                        }
                        if ("count".equalsIgnoreCase(constrainType)) {
                            String useTime = null;
                            try {
                                int times = Integer.parseInt(cv.getAsString(CommonIdentity.REMAINING_REPEAT_COUNT));
                                useTime = String.format(getString(R.string.use_times), "" + times);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (null != useTime) {
                                mCuttentConstraint = mStringBuilder
                                        .append(getString(R.string.drm_current_right))
                                        .append(": ").append(useTime)
                                        .append("\n").toString();
                                mStringBuilder.setLength(0);
                                mDrmCurrentConstraint = useTime;
                            } else {
                                mCuttentConstraint = mStringBuilder
                                        .append(getString(R.string.drm_current_right))
                                        .append(": ")
                                        .append(getString(R.string.not_available))
                                        .append("\n").toString();
                                mDrmCurrentConstraint = getString(R.string.not_available);
                                mStringBuilder.setLength(0);
                            }
                        } else if ("datetime".equalsIgnoreCase(constrainType)) {
                            String startTime = getString(R.string.valid_after) + " " + cv.getAsString(CommonIdentity.LICENSE_START_TIME);
                            String endTime = getString(R.string.valid_until) + " " + cv.getAsString(CommonIdentity.LICENSE_EXPIRY_TIME);
                            if (TextUtils.isEmpty(startTime)) {
                                startTime = getString(R.string.unlimited_usage);
                            }
                            if (TextUtils.isEmpty(endTime)) {
                                startTime += "\n" + getString(R.string.unlimited_usage);
                            } else {
                                startTime += "\n" + endTime;
                            }
                            mCuttentConstraint = mStringBuilder
                                    .append(getString(R.string.drm_current_right))
                                    .append(": ").append(startTime)
                                    .append("\n").toString();
                            mDrmCurrentConstraint = startTime;
                            mStringBuilder.setLength(0);
                        } else if ("interval".equalsIgnoreCase(constrainType)) {
                            String interval = cv.getAsString(CommonIdentity.LICENSE_AVAILABLE_TIME);
                            int line_index = interval.indexOf("-");
                            String year = interval.substring(0, line_index);
                            line_index = interval.indexOf("-", line_index + 1);
                            String month = interval.substring(line_index - 2, line_index);
                            String day = interval.substring(line_index + 1, line_index + 3);
                            int colon_index = interval.indexOf(":");
                            String hour = interval.substring(colon_index - 2, colon_index);
                            colon_index = interval.indexOf(":", colon_index + 1);
                            String minute = interval.substring(colon_index - 2, colon_index);
                            String second = interval.substring(colon_index + 1, colon_index + 3);
                            year = (year.equalsIgnoreCase("0000") ? "" : ("" + Integer.parseInt(year) + "Year-"));
                            month = (month.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(month) + "Month-"));
                            day = (day.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(day) + "Day "));
                            hour = (hour.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(hour) + "Hour "));
                            minute = (minute.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(minute) + "Minute "));
                            second = (second.equalsIgnoreCase("00") ? "" : ("" + Integer.parseInt(second) + "Second"));
                            interval = getString(R.string.drm_interval, year + month + day + hour + minute + second);
                            mCuttentConstraint = mStringBuilder
                                    .append(getString(R.string.drm_current_right))
                                    .append(": ").append(interval)
                                    .append("\n").toString();
                            mDrmCurrentConstraint = interval;
                            mStringBuilder.setLength(0);
                        }
                    }
                } else {
                    mCurrentCount = mStringBuilder
                            .append(getString(R.string.drm_current_count))
                            .append(": ")
                            .append(getString(R.string.not_available))
                            .append("\n").toString();
                    mDrmCurrentCount = getString(R.string.not_available);
                    mStringBuilder.setLength(0);
                    mCuttentConstraint = mStringBuilder
                            .append(getString(R.string.drm_current_right))
                            .append(": ")
                            .append(getString(R.string.not_available))
                            .append("\n").toString();
                    mDrmCurrentConstraint = getString(R.string.not_available);
                }
            } else {
                mCurrentCount = mStringBuilder
                        .append(getString(R.string.drm_current_count))
                        .append(": ")
                        .append(getString(R.string.not_available))
                        .append("\n").toString();
                mDrmCurrentCount = getString(R.string.not_available);
                mStringBuilder.setLength(0);
                mCuttentConstraint = mStringBuilder
                        .append(getString(R.string.drm_current_right))
                        .append(": ")
                        .append(getString(R.string.not_available))
                        .append("\n").toString();
                mDrmCurrentConstraint = getString(R.string.not_available);
            }

        } else if (DrmManager.mCurrentDrm == 10) {
            mCuttentConstraint = "";
            try {
                boolean rightsStatus = DrmManager.getInstance(getActivity().getApplicationContext()).canTransfer(path);
                if (rightsStatus == true) {
                    mProtectionStatus = getString(R.string.drm_can_forward);
                } else if (rightsStatus == false) {
                    mProtectionStatus = getString(R.string.drm_can_not_forward);
                }

                ContentValues values = DrmManager.getInstance(getActivity().getApplicationContext()).getConstraints(path, mAction);
                if (values == null || values.size() == 0 || !isRightValid) {
                    mLicenseStartTime = getString(R.string.drm_no_license);
                    mLicenseEndTime = getString(R.string.drm_no_license);
                    mRemains = getString(R.string.drm_no_license);
                } else {
                    if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_START_TIME)) {
                        Long startL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_START_TIME);
                        if (startL != null) {
                            if (startL == -1) {
                                mLicenseStartTime = getString(R.string.drm_no_limitation);
                            } else {
                                mLicenseStartTime = toDateTimeString(startL);
                            }
                        }
                    } else {
                        mLicenseStartTime = getString(R.string.drm_no_limitation);
                    }

                    if (values.containsKey(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME)) {
                        Long endL = values.getAsLong(DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME);
                        if (endL != null) {
                            if (endL == -1) {
                                mLicenseEndTime = getString(R.string.drm_no_limitation);
                            } else {
                                mLicenseEndTime = toDateTimeString(endL);
                            }
                        } else {
                            LogUtils.e(TAG, "fengke DetailInfoListener: endL is null");
                        }
                    } else {
                        mLicenseEndTime = getString(R.string.drm_no_limitation);
                    }

                    if (values.containsKey(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT)
                            && values.containsKey(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT)) {
                        Long remainCount = values.getAsLong(DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT);
                        Long maxCount = values.getAsLong(DrmStore.ConstraintsColumns.MAX_REPEAT_COUNT);
                        if (remainCount != null && maxCount != null) {
                            if (remainCount == -1 || maxCount == -1) {
                                mRemains = getString(R.string.drm_no_limitation);
                            } else {
                                mRemains = remainCount.toString() + "/" + maxCount.toString();
                            }
                        }
                    } else {
                        mRemains = getString(R.string.drm_no_limitation);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        mStringBuilder.setLength(0);

        mDetailSize = FileUtils.sizeToString(getActivity(), info.getFileSize());
        List<String> titleAdapter = new ArrayList<String>();
        List<String> valueAdapter = new ArrayList<String>();
        titleAdapter.add(getString(R.string.detail_name_m));
        valueAdapter.add(mDetailName);
        titleAdapter.add(getString(R.string.detail_size_m));
        valueAdapter.add(mDetailSize);
        titleAdapter.add(getString(R.string.detail_path_m));
        valueAdapter.add(mDetailPath);
        titleAdapter.add(getString(R.string.detail_time_m));
        valueAdapter.add(mDetailModifyTime);
        titleAdapter.add(getString(R.string.permission_m));
        valueAdapter.add(mDetailPermission);
        if (isDrm && DrmManager.mCurrentDrm == CommonIdentity.QCOM_DRM) {
            titleAdapter.add(getString(R.string.drm_current_count));
            valueAdapter.add(mDrmCurrentCount);
            titleAdapter.add(getString(R.string.drm_current_right));
            valueAdapter.add(mDrmCurrentConstraint);
            titleAdapter.add(getString(R.string.right_url));
            valueAdapter.add(mDrmLicesesIssuerURL);

        } else if (isDrm && DrmManager.mCurrentDrm == CommonIdentity.MTK_DRM) {
            titleAdapter.add(getString(R.string.drm_protection_status));
            valueAdapter.add(mProtectionStatus);
            titleAdapter.add(getString(R.string.drm_begin));
            valueAdapter.add(mLicenseStartTime);
            titleAdapter.add(getString(R.string.drm_end));
            valueAdapter.add(mLicenseEndTime);
            titleAdapter.add(getString(R.string.drm_use_left));
            valueAdapter.add(mRemains);
        }
        mDetailDialog = DetailDialogFragment.createDetailDialog(getActivity(), getFragmentManager(),getString(R.string.detail), titleAdapter, valueAdapter);
        mDetailDialog.showDialog();
    }

    private String getPermission(StringBuilder mStringBuilder, File file) {
        appendPermission(mStringBuilder, file.canRead(), R.string.readable_m);
        mStringBuilder.append("\n");
        appendPermission(mStringBuilder, file.canWrite(), R.string.writable_m);
        mStringBuilder.append("\n");
        appendPermission(mStringBuilder, file.canExecute(), R.string.executable_m);

        return mStringBuilder.toString();
    }

    private void appendPermission(StringBuilder mStringBuilder, boolean hasPermission, int title) {
        mStringBuilder.append(getString(title) + ": ");
        if (hasPermission) {
            mStringBuilder.append(getString(R.string.yes));
        } else {
            mStringBuilder.append(getString(R.string.no));
        }
    }

    private String getDetailPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            path = path.substring(0, path.lastIndexOf("/"));
        }
        return path;
    }

    @Override
    public void clickNotificationBtn() {
        if (mApplication.mProgressDialog == null && RunningTaskMap.getRunningTaskSize() > 0) {
            mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_ALL_SHOW_MODE;
            TaskInfo mProgressTaskInfo = CommonUtils.getListenerInfo("Progress", 0, CommonIdentity.PASTE_COPY_TASK, 0);
            mApplication.mProgressDialog = ProgressPopupWindow.newInstance(mActivity, mProgressTaskInfo);
            mApplication.mProgressDialog.setCancelable(false);
            mApplication.mProgressDialog.show();
            for (Object key : RunningTaskMap.getInstance().keySet()) {
                long mKey = (long) key;
                RunningTaskInfo info = (RunningTaskInfo) ExcuteTaskMap.getFinishTask(mKey);
                if (info.getPrgressBarIndex() == 1) {
                    ProgressPopupWindow.mFirstTaskLayout.setVisibility(View.VISIBLE);
                    if (info.getDialogTitle() != null && !info.getDialogTitle().equals("")) {
                        mApplication.mProgressDialog.setTitle(info.getDialogTitle());
                    }
                }
            }
        } else if (mApplication.mProgressDialog != null && RunningTaskMap.getRunningTaskSize() > 0 && !mApplication.mProgressDialog.isShowing()) {
            mApplication.mProgressDialog.show();
        }
    }

    public void switchToNormalWhenCreateTask(int mTaskType, List<FileInfo> mOperInfo) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.clearSelected(mTaskType);
        if (mTaskType != CommonIdentity.PASTE_COPY_TASK && mTaskType != CommonIdentity.PASTE_CUT_TASK) {
            switchToNormalView();
        } else {
            mAbsListViewFragmentListener.updateBarTitle(mTaskType);
        }
    }

    @Override
    public void clickCompressBtn(int mode,String mArchiveName) {
        if (!isAdded() || mAdapter == null ||  mAdapter.getItemEditFileInfoList().size() == 0) {
            return;
        }
        if (mode == CommonIdentity.COMPRESS_NORMAL_MODE) {
            if(mAdapter.getItemEditFileInfoList().size() == 1 && mAdapter.getItemEditFileInfoList().get(0).isDirectory()) {
                showCompressRenameDialog(mAdapter.getItemEditFileInfoList().get(0).getFileName());
            } else {
                showCompressRenameDialog(null);
            }
        } else {
            List<FileInfo> fileInfoList = mAdapter.getItemEditFileInfoList();
            boolean isNull = mArchiveName == null || mArchiveName.equals("");
            String mParentPath = fileInfoList.get(0).getFileParentPath();
            if (!isNull && CommonUtils.matcherFolderName(mArchiveName)) {
                mToastHelper.showToast(R.string.invalid_char_prompt);
            } else if (!isNull && !mArchiveName.equals(CommonIdentity.COMPRESS_DEFAUT_NAME) && new File(mParentPath + File.separator + mArchiveName+".zip").exists()) {
                mToastHelper.showToast(getResources().getString(R.string.file_already_exists, mArchiveName+".zip"));
            } else {
                if(isNull){
                    mArchiveName = CommonIdentity.COMPRESS_DEFAUT_NAME;
                }
                CommonDialogFragment df = (CommonDialogFragment) getFragmentManager().findFragmentByTag(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG);
                if (df != null) {
                    df.dismissAllowingStateLoss();
                }
                CommonDialogFragment.mShowMessageDialogFragment = null;
                CommonDialogFragment.clearDailogTag();
                CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.compressing).toString(), CommonIdentity.FILE_COMPRESSION_TASK, -1, -1, null,
                        null, mApplication.mCurrentPath, mArchiveName, fileInfoList, false, CategoryManager.mCurrentCagegory);
                switchToNormalWhenCreateTask(CommonIdentity.FILE_COMPRESSION_TASK, fileInfoList);
            }
        }
    }

    private void showCompressRenameDialog(String mFileName) {
        mComPressDialog = CommonDialogFragment.getInstance(getFragmentManager(),mFileName,CommonIdentity.COMPRESS_RENAME_DIALOG_TAG);
        mComPressDialog.showDialog();
    }

    @Override
    public void clickExtractBtn(int mode,String folderName) {
        if (!isAdded() || mAdapter == null || mAdapter.getItemEditFileInfoList().size() == 0) {
            return;
        }
        List<FileInfo> fileInfoList = mAdapter.getItemEditFileInfoList();
        FileInfo fileInfo = fileInfoList.get(0);
        if(fileInfo == null){
            return;
        }
        String name = fileInfo.getFileName();
        if(mode == CommonIdentity.EXTRACT_NORMAL_MODE) {
              showExtractDialog(name);
         } else if (mode == CommonIdentity.EXTRACT_REPEAT_NAME_MODE){
            String mParentPath = fileInfo.getFileParentPath();
            String mFolderName = FileUtils.getOriginalFileName(fileInfo.getFileName());
            if(new File(mParentPath+File.separator+mFolderName).exists()){
                showExtractExistDialog(name);
            } else {
                CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.extracting).toString(), CommonIdentity.FILE_UNCOMPRESSION_TASK, -1, -1, null,
                        null, mApplication.mCurrentPath, null, fileInfoList, false, CategoryManager.mCurrentCagegory);
                switchToNormalWhenCreateTask(CommonIdentity.FILE_UNCOMPRESSION_TASK, fileInfoList);
            }

         } else if (mode == CommonIdentity.EXTRACT_RUNNING_MODE){
            CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.extracting).toString(), CommonIdentity.FILE_UNCOMPRESSION_TASK, -1, -1, null,
                    null, mApplication.mCurrentPath, null, fileInfoList, false, CategoryManager.mCurrentCagegory);
            switchToNormalWhenCreateTask(CommonIdentity.FILE_UNCOMPRESSION_TASK, fileInfoList);
        } else if (mode == CommonIdentity.EXTRACT_RENAME_MODE){
            showExtractRenameDialog(name);
        } else if (mode == CommonIdentity.EXTRACT_RENAME_RUNNING_MODE){
            if(folderName == null || folderName.equals("")){
                mToastHelper.showToast(R.string.invalid_empty_name);
            } else {
                String mParentPath = fileInfo.getFileParentPath();
                if(CommonUtils.matcherFolderName(folderName)){
                    mToastHelper.showToast(R.string.invalid_char_prompt);
                } else if (new File(mParentPath+File.separator+folderName).exists()){
                    mToastHelper.showToast(R.string.folder_already_exists);
                } else {
                    CommonDialogFragment df = (CommonDialogFragment) getFragmentManager().findFragmentByTag(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG);
                    if(df != null){
                        df.dismissAllowingStateLoss();
                    }
                    CommonDialogFragment.mShowMessageDialogFragment = null;
                    CommonDialogFragment.clearDailogTag();
                    CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.extracting).toString(), CommonIdentity.FILE_UNCOMPRESSION_TASK, -1, -1, null,
                            null, mApplication.mCurrentPath, folderName, fileInfoList, false, CategoryManager.mCurrentCagegory);
                    switchToNormalWhenCreateTask(CommonIdentity.FILE_UNCOMPRESSION_TASK, fileInfoList);
                }
            }
        }

    }

    private void showExtractDialog(String name) {
        SpannableString spannableString = new SpannableString(name);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        String extractMessage = getResources().getString(R.string.extract) + " " + spannableString + "?";
        mExtractDialog = CommonDialogFragment.getInstance(getFragmentManager(), extractMessage, CommonIdentity.EXTRACT_DIALOG_TAG);
        mExtractDialog.showDialog();
    }

    private void showExtractExistDialog(String name) {
        mExtractDialog = CommonDialogFragment.getInstance(getFragmentManager(), getResources().getString(R.string.file_already_exists, name),
                CommonIdentity.EXTRACT_NAME_EXIST_DIALOG_TAG);
        mExtractDialog.showDialog();
    }

    private void showExtractRenameDialog(String name) {
        mExtractDialog = CommonDialogFragment.getInstance(getFragmentManager(), getResources().getString(R.string.file_already_exists, name),
                CommonIdentity.EXTRACT_RENAME_DIALOG_TAG);
        mExtractDialog.showDialog();
    }

    @Override
    public void registerDataContentObserver() {
        if (mApplication != null && mApplication.mDataContentObserver != null) {
            mApplication.mDataContentObserver.startFileTimerWatcher();
        }
    }

    @Override
    public void unRegisterDataContentObserver() {
        if (mApplication != null && mApplication.mDataContentObserver != null) {
            mApplication.mDataContentObserver.cancelFileTimerWatcher();
        }
    }

    @Override
    public boolean isShowNoFolderView() {
        if (mNoFolderView != null) {
            return mNoFolderView.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    @Override
    public List<FileInfo> getCheckedList() {
        if (mAdapter != null) {
            return mAdapter.getCheckedFileInfoItemsList();
        }
        return null;
    }

    @Override
    public void restoreCheckedList(List<FileInfo> mRestoreCheckedList) {
        if (mApplication == null) {
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        }
        if (mAdapter != null && CommonUtils.isNormalStatus(mApplication)) {
            mAdapter.setChecked(mRestoreCheckedList, true);
        } else if (mAdapter != null && CommonUtils.isEditStatus(mApplication)) {
            mAdapter.setChecked(mRestoreCheckedList, false);
        }
    }

    @TargetApi(24)
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        mApplication.isInMultiWindowMode = isInMultiWindowMode;
        if (mApplication.mProgressDialog != null && mApplication.mProgressDialog.isShowing()) {
            mApplication.mProgressDialog.hide();
            mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_NOTIFICATION_MODE;
        }
    }

    @Override
    public void deleteFileResponse() {
        if (mApplication != null && mAdapter != null) {
            CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.delete).toString(),
                    deleteMode, CategoryManager.mCurrentCagegory, null, -1, mAdapter.getItemEditFileInfoList(), mApplication.mCurrentPath, null);
            switchToNormalWhenCreateTask(CommonIdentity.DETAIL_FILE_TASK, mAdapter.getItemEditFileInfoList());
        }
    }

    @Override
    public void clickAddPrivateMode() {
        if (mApplication != null) {
            List<FileInfo> fileInfoList = mAdapter.getItemEditFileInfoList();
            CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.move_safe).toString(), CommonIdentity.ADD_PRIVATE_FILE_TASK, -1, -1, null,
                    null, mApplication.mCurrentPath, null, fileInfoList, CommonUtils.isSearchStatus(mApplication), CategoryManager.mCurrentCagegory);
            switchToNormalWhenCreateTask(CommonIdentity.ADD_PRIVATE_FILE_TASK, fileInfoList);
        }
    }

    @Override
    public void clickRemovePrivateMode() {
        showRemovePrivateDialog();
    }

    private void showRemovePrivateDialog() {
        if (!isAdded() || mAdapter.getItemEditFileInfoList().size() == 0) {
            return;
        }
        String mDialogContent;
        if (mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION ||
                mApplication.mCurrentLocation == CommonIdentity.FILE_MANAGER_LOCATIONE) {
            mDialogContent = getString(R.string.remove_safe_multi);
            if (mAdapter.getItemEditFileInfoList().size() == 1) {
                String filename = mAdapter.getItemEditFileInfoList().get(0).getFileName();
                String noResultText = getResources().getString(R.string.remove_safe_single);
                mDialogContent = String.format(noResultText, filename);
            }
        } else {
            mDialogContent = getString(R.string.remove_private_tip);
        }

        mRemovePrivateDialog = CommonDialogFragment.getInstance(getFragmentManager(),
                mDialogContent, CommonIdentity.REMOVE_PRIVATE_DIALOG_TAG);
        mRemovePrivateDialog.showDialog();
    }

    @Override
    public void removePrivateMode() {
        if (mApplication != null) {
            List<FileInfo> fileInfoList = mAdapter.getItemEditFileInfoList();
            CommonUtils.getBaseTaskInfo(mApplication, -1, mApplication.getResources().getString(R.string.set_public).toString(), CommonIdentity.REMOVE_PRIVATE_FILE_TASK, -1, -1, null,
                    null, mApplication.mCurrentPath, null, fileInfoList, CommonUtils.isSearchStatus(mApplication), CategoryManager.mCurrentCagegory);
            switchToNormalWhenCreateTask(CommonIdentity.REMOVE_PRIVATE_FILE_TASK, fileInfoList);
        }
    }

    @Override
    public void cancelProgressDialog() {
        if(RunningTaskMap.getRunningTaskSize() > 0) {
            mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_NOTIFICATION_MODE;
            createNotificationProgress();
        }
    }

    /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-07-04,BUG-4974642*/
    @Override
    public void afreshItemMorePop() {
        if (mAdapter != null && mAdapter.mItemMorePop != null) {
            mAdapter.afreshPopWindow(mRecyclerView);
        }
    }
    /* MODIFIED-END by Chuanzhi.Shao,BUG-4974642*/
}
