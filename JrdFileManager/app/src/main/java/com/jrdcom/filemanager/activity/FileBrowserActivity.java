package com.jrdcom.filemanager.activity;

import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.IActivityListener;
import com.jrdcom.filemanager.IActivitytoCategoryListener;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.adapter.PathBarAdapter;
import com.jrdcom.filemanager.manager.ScrollSpeedLinearLayoutManger;
import com.jrdcom.filemanager.dialog.AlertDialogFragment;
import com.jrdcom.filemanager.dialog.AlertDialogFragment.AlertDialogFragmentBuilder;
import com.jrdcom.filemanager.dialog.CommonDialogFragment;
import com.jrdcom.filemanager.dialog.ProgressPopupWindow;
import com.jrdcom.filemanager.fragment.CategoryFragment;
import com.jrdcom.filemanager.listener.CategoryFragmentListener;
import com.jrdcom.filemanager.fragment.FileBrowserFragment;
import com.jrdcom.filemanager.fragment.FileBrowserFragment.AbsListViewFragmentListener;
import com.jrdcom.filemanager.fragment.ListsFragment;
import com.jrdcom.filemanager.fragment.PermissionFragment;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.IconManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.singleton.ExcuteTaskMap;
import com.jrdcom.filemanager.singleton.ResultTaskHandler;
import com.jrdcom.filemanager.singleton.RunningTaskMap;
import com.jrdcom.filemanager.singleton.TaskInfoMap;
import com.jrdcom.filemanager.singleton.WaittingTaskList;
import com.jrdcom.filemanager.task.BaseAsyncTask;
import com.jrdcom.filemanager.task.FileOperationTask;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;
import com.jrdcom.filemanager.view.CustomPopupWindowBasedAnchor;
import com.jrdcom.filemanager.view.ToastHelper;
import com.tcl.faext.FAExt;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class FileBrowserActivity extends FileBaseActivity implements
        AbsListViewFragmentListener, CategoryFragmentListener,
        PathBarAdapter.OnItemClickLitener, DialogInterface.OnClickListener {

    private static final String TAG = FileBrowserActivity.class.getSimpleName();

    private PowerManager.WakeLock wakeLock;
    private Builder mBuilderTemp;
    private boolean deleteFlag = false;
    private boolean mShareFlag = false;
    private boolean mSearchFromEdit = false;
    private boolean isShowDialog = false;
    private boolean isCancelTask = false;
    private boolean isClickNotification = false;
    private static boolean isEnterSaveInstanceState = false;


    /**
     * /* Whether to click on the navigation bar of the ICON HOME
     */
    private boolean isClickHomeIcon = false;
    /**
     * this param is used to check whether this action is from shortcut
     */
    private boolean isFromShortcut = false;

    public boolean getFrom() {
        return isFromShortcut;
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            if (fragment instanceof IActivityListener) {
                mActivityListener = (IActivityListener) fragment;
                if (mApplication == null) {
                    mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
                }
                if (mActivityListener != null && mApplication != null) {
                    mApplication.mResultTaskHandler = ResultTaskHandler.getInstance(mActivityListener);
                    mApplication.setAppHandler(mApplication.mResultTaskHandler);
                }
            }
            // set value for mActivitytoCategoryListener with attached fragment.
            if (fragment instanceof IActivitytoCategoryListener) {
                mActivitytoCategoryListener = (IActivitytoCategoryListener) fragment;
            }
        } catch (Exception e) {
            Log.e(TAG, "onAttachFragment happen exception --" + e);
            e.printStackTrace();
        }
        super.onAttachFragment(fragment);
    }

    Handler mBrowserHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CommonIdentity.HAWKEYE_FEEDBACK_INFO:
                    if(mApplication == null){
                        return;
                    }
                    int mSortType = SharedPreferenceUtils.getPrefsSortBy(FileBrowserActivity.this);
//                    String mFileViewMode = SharedPreferenceUtils.getPrefsViewBy(FileBrowserActivity.this);
                    if (mSortType == CommonIdentity.SORT_NAME) {
                        CommonUtils.recordStatusEventForFA(FileBrowserActivity.this, CommonIdentity.FILEMANAGER_SORT_KEY, CommonIdentity.FILEMANAGER_NAME_SORT_STATUS,mApplication);
                    } else if (mSortType == CommonIdentity.SORT_TIME) {
                        CommonUtils.recordStatusEventForFA(FileBrowserActivity.this, CommonIdentity.FILEMANAGER_SORT_KEY, CommonIdentity.FILEMANAGER_TIME_SORT_STATUS,mApplication);
                    } else if (mSortType == CommonIdentity.SORT_SIZE) {
                        CommonUtils.recordStatusEventForFA(FileBrowserActivity.this, CommonIdentity.FILEMANAGER_SORT_KEY, CommonIdentity.FILEMANAGER_SIZE_SORT_STATUS,mApplication);
                    } else if (mSortType == CommonIdentity.SORT_TYPE) {
                        CommonUtils.recordStatusEventForFA(FileBrowserActivity.this, CommonIdentity.FILEMANAGER_SORT_KEY, CommonIdentity.FILEMANAGER_TYPE_SORT_STATUS,mApplication);
                    }

                    String mFileViewMode = SharedPreferenceUtils.getCurrentViewMode(FileBrowserActivity.this);
                    if (mFileViewMode != null && mFileViewMode.equals(CommonIdentity.GRID_MODE)) {
                        CommonUtils.recordStatusEventForFA(FileBrowserActivity.this, CommonIdentity.FILEMANAGER_VIEW_KEY, CommonIdentity.FILEMANAGER_GRID_VIEW_STATUS,mApplication);
                    } else {
                        CommonUtils.recordStatusEventForFA(FileBrowserActivity.this, CommonIdentity.FILEMANAGER_VIEW_KEY, CommonIdentity.FILEMANAGER_LIST_VIEW_STATUS,mApplication);
                    }
                    break;
                case CommonIdentity.INIT_ACTIVITY_MAINCONTENTVIEW:
                    home_item.setOnClickListener(FileBrowserActivity.this);
                    home_path_icon.setOnClickListener(FileBrowserActivity.this);
                    if (!isShowNoStorageDialog) {
                        isShowNoStorageDialog = false;
                        showNoAvailableStorageDialog();
                    }
                    break;
                case CommonIdentity.NO_AVAILABLE_STORAGE:
                    showNoAvailableStorageDialog();
                    break;
                case CommonIdentity.RESTORE_INSTANCE_STATE:
                    if (CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication)) {
                        CategoryManager.mCurrentMode = CommonIdentity.PATH_MODE;
                        mActivityListener.refreshAdapter(mApplication.mCurrentPath, -1, CommonIdentity.REFRESH_FILE_PATH_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        if (CommonUtils.isSearchStatus(mApplication)) {
                            searchStatusChanageScreen();
                        } else if (CommonUtils.isEditStatus(mApplication)) {
                            editStatusChanageScreen();
                        } else if (CommonUtils.isNormalStatus(mApplication) || CommonUtils.isCopyNormalStatus(mApplication)) {
                            normalStatusChanageScreen();
                        }
                        updateBarView();
                    } else if (CategoryManager.mCurrentCagegory >= 0) {
                        CategoryManager.mCurrentMode = CommonIdentity.CATEGORY_MODE;
                        mActivityListener.refreshAdapter("", CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_CATEGORY_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                        if (CommonUtils.isSearchStatus(mApplication)) {
                            searchStatusChanageScreen();
                        } else if (CommonUtils.isEditStatus(mApplication)) {
                            editStatusChanageScreen();
                        } else if (CommonUtils.isNormalStatus(mApplication) || CommonUtils.isCopyNormalStatus(mApplication)) {
                            normalStatusChanageScreen();
                        }
                        updateBarView();
                    } else if(mCurrentFragment==mCategoryFragment&&CommonUtils.isCopyNormalStatus(mApplication)){
                        normalStatusChanageScreen();
                        updateCategoryNormalBar();
                    }
                    break;
            }
        }
    };

    @Override
    public void setMainContentView() {
        super.setMainContentView();
        if (mActivityListener != null && mApplication != null) {
            mApplication.mResultTaskHandler = ResultTaskHandler.getInstance(mActivityListener);
            mApplication.setAppHandler(mApplication.mResultTaskHandler);
        }
        Intent intent = getIntent();
        if (intent != null) {
            laucherFolderName = intent.getStringExtra("foldername");
            intent.removeExtra("foldername");
            if ((CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication) || isEnterSaveInstanceState) && !PermissionUtil.isAllowPermission(this)) {
                laucherFolderName = null;
            }
            isEnterSaveInstanceState = false;
        }
        if (laucherFolderName != null && !laucherFolderName.equals("") && !PermissionUtil.isAllowPermission(this)) {
            File launcherFile = new File(laucherFolderName);
            if (!launcherFile.exists() || (!mApplication.isShowHidden && launcherFile.isHidden())
                    || isPathInvalid(laucherFolderName)) {
                laucherFolderName = null;
                if(mToastHelper == null){
                    mToastHelper = new ToastHelper(this);
                }
                mToastHelper.showToast(R.string.shortcut_no_exist);
                mTagMode = CommonIdentity.CATEGORY_TAG;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
                filePathBrower.setVisibility(View.GONE);
            } else {
                mTagMode = "";
            }
        } else if (PermissionUtil.isAllowPermission(this) && CommonUtils.hasM()) {
            mTagMode = CommonIdentity.PERMISSION_TAG;
        } else if (CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication)) {
        } else if (CommonUtils.isCategoryMode()) {
            mTagMode = CommonIdentity.CATEGORY_TAG;
            SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
            filePathBrower.setVisibility(View.GONE);
        } else {
            String path = null;
            if (getIntent() != null) {
                path = getIntent().getStringExtra("foldername");
            }
            if ((path != null && !new File(path).exists()) || CommonUtils.isPermissionTag(SharedPreferenceUtils.getPermissionPrefCurrTag(mApplication))
                    || SharedPreferenceUtils.getPrefCurrTag(mApplication).equals(CommonIdentity.FILE_STATUS_SEARCH)
                    || CommonUtils.isPermissionTag(mTagMode) || isPathInvalid(path)) {
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
            }
            // if mApplication.mCurrentPath is null, the path info may invalid, change mTagMode to CommonIdentity.CATEGORY_TAG
            if (!CommonUtils.isCategoryTag(mTagMode)) {
                mTagMode = CommonIdentity.CATEGORY_TAG;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
            }
        }
        // reset viewstatus avoid exception when app forced exit
        if (!CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication) && !CommonUtils.isCategoryTag(mTagMode)) {
            SharedPreferenceUtils.resetPrefsStatus(this);
        }
        //LogUtils.d("SHO", "this is screen width" + CommonUtils.getScreenWidth(this) + "this is screen height" + CommonUtils.getScreenHeight(this));
        try {
            if (CommonUtils.isPermissionTag(mTagMode)) {
                if (mPermissionFragment == null) {
                    mPermissionFragment = new PermissionFragment();
                }
                commitFragment(mPermissionFragment, false, View.GONE, false);
            } else if (CommonUtils.isCategoryTag(mTagMode)) {
                if (CategoryManager.mCurrentCagegory >= 0) {
                    updateCategoryNormalBarView();
                    if (mListFragment == null) {
                        mListFragment = new ListsFragment();
                    }
                    commitFragment(mListFragment, false, View.GONE, false);
                    mActivityListener = (IActivityListener) mListFragment;
                } else if (CommonUtils.isGlobalSearchStatus(mApplication)) {
                    if (mListFragment == null) {
                        mListFragment = new ListsFragment();
                    }
                    commitFragment(mListFragment, false, View.GONE, false);
                    mActivityListener = (IActivityListener) mListFragment;
                    setActionBarDisplayHomeAsUpEnabled(false);
                    if (filePathBrower != null) {
                        filePathBrower.setVisibility(View.GONE);
                    }
                    mQueryText = mSaveQueryText;
                    mSaveQueryText = "";
                    updateViewByTag();
                    updateView(getFileMode());
                    if (mQueryText == null || mQueryText.equals("")) {
                        return;
                    }
                    return;
                } else {
                    if (mCategoryFragment == null) {
                        mCategoryFragment = new CategoryFragment();
                    }
                    commitFragment(mCategoryFragment, false, View.GONE, false);
                    mActivityListener = (IActivityListener) mCategoryFragment;
                    setActionBarTitle(R.string.app_name_new); // MODIFIED by Chuanzhi.Shao, 2017-10-11,BUG-5395138
                    updateViewByTag();
                }
                CategoryManager.setCurrentMode(CommonIdentity.CATEGORY_MODE);
            } else if (laucherFolderName != null && !laucherFolderName.equals("")) {
                switchShortcut(laucherFolderName, false);
                mApplication.mCurrentPath = laucherFolderName;
                if (mListFragment == null) {
                    mListFragment = new ListsFragment();
                }
                commitFragment(mListFragment, true, View.VISIBLE, true);
                mActivityListener = (IActivityListener) mListFragment;
                //commitPathFragment(true);
            } else if (CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication)) {
                if (mListFragment == null) {
                    mListFragment = new ListsFragment();
                }
                commitFragment(mListFragment, false, View.VISIBLE, false);
                mActivityListener = (IActivityListener) mListFragment;
            } else {
                commitPathFragment(false);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGlobalSearchBack.setOnClickListener(this);
        mGlobalSearchCancel.setOnClickListener(this);
        mBrowserHandler.sendEmptyMessage(CommonIdentity.HAWKEYE_FEEDBACK_INFO);
        mBrowserHandler.sendEmptyMessage(CommonIdentity.INIT_ACTIVITY_MAINCONTENTVIEW);
    }

    @Override
    public void onBackPressed() {
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, false);//show Actionbar
        }
        if (mActivityListener != null) {
            mActivityListener.showNoSearchResults(false, null);
            mActivityListener.showNoFolderResultView(false);
            //When exiting Files, cancel all running Task.
            if (mCurrentFragment == mCategoryFragment) {
                if (RunningTaskMap.getRunningTaskSize() > 0) {
                    mExitDialog = CommonDialogFragment.getInstance(getFragmentManager(),
                            getString(R.string.exit_dialog_content), CommonIdentity.EXIT_DIALOG_TAG);
                    mExitDialog.showDialog();
                    return;
                } else {
                    mApplication.mCurrentStatus = CommonIdentity.FILE_STATUS_NORMAL;
                    SharedPreferenceUtils.changePrefsStatus(mApplication, CommonIdentity.FILE_STATUS_NORMAL);
                    isEnterSaveInstanceState = false;
                    isShowNoStorageDialog = false;
                    ExcuteTaskMap.clearFinishTask();
                    if (mApplication.mNotiManager != null
                            && mApplication.mNotiManager.getActiveNotifications().length > 0) {
                        mApplication.mNotiManager.cancelAll();
                    }
                    mApplication.mCache.clearCache();
                }
            }
        }
        isSearchingDone = false;
        if (CommonUtils.isEditStatus(mApplication) || CommonUtils.isSearchStatus(mApplication)) {
            if (mApplication != null && mApplication.mFileInfoManager.getPasteCount() == 0) {
                updateBarTitle(CommonIdentity.UNKNOWN_TASK);
            } else {
                mActivityListener.switchToCopyView();
                mActivityListener.clearChecked();
                return;
            }
        }
        if (CommonUtils.isPathNormalMode(mApplication.mCurrentStatus) || isClickHomeIcon) {
            if (mBtnEditBack != null && !isClickHomeIcon) {
                setActionBarDisplayHomeAsUpEnabled(true);
                mBtnEditBack.setVisibility(View.GONE);
            }
            if (mMountPointManager
                    .isSdOrPhonePath(mApplication.mCurrentPath) || (mApplication.mFileInfoManager.getPasteCount() > 0
                    && CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) || isClickHomeIcon) {
                isClickHomeIcon = false;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
                hideFloatButton();
                mTagMode = CommonIdentity.CATEGORY_TAG;
                CategoryManager.setCurrentMode(CommonIdentity.CATEGORY_MODE);
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
                if (mActivityListener != null) {
                    mActivityListener.clearAdapter();
                }
                updateFragment(CommonIdentity.CATEGORY_TAG);
                return;
            }
        } else {
            if (morePop != null && morePop.isShowing()) {
                morePop.dismiss();
            }
            if (PermissionUtil.isAllowPermission(this) && CommonUtils.hasM()) {
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.PERMISSION_TAG);
                mTagMode = CommonIdentity.PERMISSION_TAG;
            } else {
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
                mTagMode = CommonIdentity.CATEGORY_TAG;
            }
        }
        isClickHomeIcon = false;
        if (mBtnSearch != null) {
            mBtnSearch.setBackground(CommonUtils.getActionbarItemTheme(this));
        }
        if (mCurrentFragment == mListFragment) {
            if (CommonUtils.isCategoryNormalMode(mApplication.mCurrentStatus)) {
                hideFloatButton();
                switchContentByViewMode(true);
            } else {
                if (mActivityListener != null) {
                    mActivityListener.onBackPressed();
                }
            }
            return;
        }
        super.onBackPressed();
    }

    private void commitPathFragment(boolean isShortcut) {
        if (isShortcut) {
            switchShortcut(laucherFolderName, false);
        }
        mApplication.mCurrentPath = laucherFolderName;
        if (mListFragment == null) {
            mListFragment = new ListsFragment();
        }
        commitFragment(mListFragment, true, View.VISIBLE, isShortcut);
        if (isShortcut) {
            switchShortcut(laucherFolderName, true);
        }
        CategoryManager.setCurrentMode(CommonIdentity.PATH_MODE);
    }


    int listcount;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        listcount = 0;
        isChangeMultiScreen = true;
        if (mGlobalSearchView != null) {
            mGlobalSearchView.clearFocus();
        }
        if (mSearchView != null) {
            mSearchView.clearFocus();
        }
        if (mApplication.mPortraitOrientation) {
            listcount = getListPortLimita();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            listcount = getListLandLimita();
        }
        HideActionbar(false);
        if (mCurrentFragment == mListFragment && mApplication.mViewMode.equals(CommonIdentity.GRID_MODE)) {
            mActivityListener.changeViewMode(CommonIdentity.GRID_MODE);
        }
        if (sortPop != null && sortPop.isShowing()) {
            new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sortPop.dismiss();
                        int offX = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_xoff);
                        int offY = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_yoff);
                        sortPop.update(FileBrowserActivity.this);
                        sortPop.showAtLocationBasedAnchor(mBtnMore, offX, offY);
                    }
                }, 200);
            }

        if (mActivityListener != null) {
            /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-07-04,BUG-4974642*/
            //mActivityListener.closeItemMorePop();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mActivityListener.afreshItemMorePop();
                }
            }, 200);
            /* MODIFIED-END by Chuanzhi.Shao,BUG-4974642*/
        }
        if (morePop != null) {
            reshowMorePop(mBtnMore);
        }
        if (mCurrentFragment == mCategoryFragment && mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.onChangeMainlayout();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(fileBrowerList!=null&&paths!=null) {
                    fileBrowerList.scrollToPosition(paths.length - 1);
                }
            }
        }, 150);
    }

    @Override
    public void updateCategoryNavigation(int id) {
        super.updateCategoryNavigation(id);
        if ((id == 0)
                && (mCurrentFragment != mCategoryFragment)) {
            switchContentByViewMode(true);
        }
    }

    public void switchContent(Fragment to) {
        Log.d(TAG, "switchContent(), to" + to);
        if ((mCurrentFragment == mCategoryFragment)
                || (to == mCategoryFragment)) {
        } else {
            CategoryManager.getInstance(this).clearMap();
        }
        if (mCurrentFragment != to) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.layout_content, to);
            fragmentTransaction.commitAllowingStateLoss();
            mCurrentFragment = to;
            if (to == mListFragment) {
                switchViewContent(mListFragment);
            } else if (to == mCategoryFragment) {
                switchCategoryViewContent(mCategoryFragment);
            }
        } else if (to == mCategoryFragment) {
            switchCategoryViewContent(mCategoryFragment);
        } else if (to == mListFragment) {
            switchViewContent(mListFragment);
            if (mActivityListener != null && !mActivityListener.isShowNoFolderView()
                    && (CategoryManager.mCurrentCagegory > 9 || CategoryManager.mCurrentCagegory < 0)) {
                enableScrollActionbar();//support scroll actionbar
            }
        }
    }

    private void switchViewContent(FileBrowserFragment mViewFragment) {
        mActivityListener = (IActivityListener) mViewFragment;
        if (mActivityListener != null && mApplication != null) {
            mApplication.mResultTaskHandler = ResultTaskHandler.getInstance(mActivityListener);
            mApplication.setAppHandler(mApplication.mResultTaskHandler);
        }
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                if (mTagMode != CommonIdentity.GLOBAL_SEARCH) {
                    updateViewByTag();
                    updateView(getFileMode());
                }
            }

        });
        if (mTagMode != CommonIdentity.GLOBAL_SEARCH) {
            refreshPathAdapter(mApplication.mCurrentPath);
        }
    }

    private void switchCategoryViewContent(Fragment mViewFragment) {
        if (mApplication != null && mApplication.mFileInfoManager != null) {
            mApplication.mFileInfoManager.getCategoryFileList().clear();
        }
        CategoryManager.mCurrentCagegory = -1;
        mActivitytoCategoryListener = (IActivitytoCategoryListener) mViewFragment;
        if (mActivityListener != null) {
            mActivityListener.clearAdapter();
        }
        updateViewByTag();
        updateMoreButtonMenu();
        updateCategoryNormalBar();
        showActionBar();
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.onChangeMainlayout();
        }

        //cancel scroll actionbar in CategoryFragment
        if (mViewFragment == mCategoryFragment) {
            cancelScrollActionbar();
        }
    }

    @Override
    protected void refreshPathAdapter(String path) {
        super.refreshPathAdapter(path);
        if (mActivityListener != null) {
            mActivityListener.refreshAdapter(path, CategoryManager.mCurrentCagegory, CommonUtils.getRefreshMode(path, CategoryManager.mCurrentCagegory),
                    CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
        }
    }

    private void updateFragment(String tag, boolean... isRootClicked) {
        mTagMode = tag;
        if (CommonUtils.isPhoneTag(tag)) {
            updatePathFragment(tag);
            if(home_path_icon != null){
                home_path_icon.setContentDescription(getString(R.string.phone_storage));
            }
        } else if (CommonUtils.isSDCARDTag(tag)) {
            updatePathFragment(tag);
            if(home_path_icon != null){
                home_path_icon.setContentDescription(getString(R.string.sd_card));
            }
        } else if (CommonUtils.isCategoryTag(tag)) {
            updateCategoryFragment(tag, false);
            return;
        } else if (CommonUtils.isOTGUSBTag(tag)) {
            updatePathFragment(tag);
            if(home_path_icon != null){
                home_path_icon.setContentDescription(getString(R.string.usbotg));
            }
        } else if (CommonUtils.isGlobalSearchTag(tag)) {
            updateCategoryFragment(tag, true);
        }

    }


    private void updateCategoryFragment(String tag, boolean isGlobal) {
        filePathBrower.setVisibility(View.GONE);
        //cancelScrollActionbar();//cancel scroll actionbar
        if (isGlobal) {
            mApplication.mFileInfoManager.clearAll();
        }
        CategoryManager.setCurrentMode(CommonIdentity.CATEGORY_MODE);
        if (mCurrentFragment != mPermissionFragment) {
            mApplication.mCurrentPath = null;
        }
        if (!isGlobal) {
            switchContentByViewMode(true);
            if (isGlobal) {
                hideFloatButton();
            } else {
                showActionBar();
            }
        } else {
            switchContentByViewMode(false);
        }
    }

    private void updatePathFragment(String tag) {
        //enableScrollActionbar();//support scroll actionbar
        CategoryManager.setCurrentMode(CommonIdentity.PATH_MODE);
        if (laucherFolderName == null && mApplication.mCurrentPath == CommonUtils.getStorageRootPath(tag, mMountPointManager)) {
            return;
        }
        if (laucherFolderName == null) {
            mApplication.mCurrentPath = CommonUtils.getStorageRootPath(tag, mMountPointManager);
        }
        if (laucherFolderName != null && mCurrentFragment != mPermissionFragment) {
            mApplication.mCurrentPath = laucherFolderName;
            laucherFolderName = null;
        }
        CategoryManager.mLastCagegory = -2;
        CategoryManager.mCurrentCagegory = -1;
        switchContentByViewMode(false);
    }

    private void showFloatButton() {
        if (floatingActionButtonContainer != null && floatingActionButtonContainer.getVisibility() == View.GONE) {
            floatingActionButtonContainer.startAnimation(setFloatBtnShowAnim());
            floatingActionButtonContainer.setVisibility(View.VISIBLE);
        }
        if (floatingActionButton != null && floatingActionButton.getVisibility() == View.GONE) {
            floatingActionButton.startAnimation(setFloatBtnShowAnim());
            floatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    private void hideFloatButton() {
        if (floatingActionButtonContainer != null && floatingActionButtonContainer.getVisibility() == View.VISIBLE) {
            floatingActionButtonContainer.startAnimation(setFloatBtnHideAnim());
            floatingActionButtonContainer.setVisibility(View.GONE);
        }
        if (floatingActionButton != null && floatingActionButton.getVisibility() == View.VISIBLE) {
            floatingActionButton.startAnimation(setFloatBtnHideAnim());
            floatingActionButton.setVisibility(View.GONE);
        }
    }

    public void switchContentByViewMode(boolean isCategory) {
        Log.d(TAG, "switchContentByViewMode(), isCategory=" + isCategory);
        if (isCategory) {
            switchContent(mCategoryFragment);
        } else {
            switchContent(mListFragment);
            mActivityListener.changeViewMode(mApplication.mViewMode);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mApplication != null && mApplication.mDataContentObserver != null) {
            mApplication.mDataContentObserver.cancelFileTimerWatcher();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mApplication == null) {
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        }
        if (mListFragment != null) {
            mListFragment.clearAll();
        }
        mQueryText = null;
//        if (fileBrowerList != null) {
//            fileBrowerList.onDestroy();
//        }
        IconManager.getInstance().clearAll();
        if (CategoryManager.mCategoryItemMap != null) {
            CategoryManager.mCategoryItemMap.clear();
        }
        if (mApplication != null) {
            // clear cache
            //mApplication.mCache.clearCache();
            if (mApplication.mFileInfoManager != null) {
                mApplication.mFileInfoManager.clearAll();
            }
        }

        //if (mActivityListener != null) {
        //    mActivityListener.unRegisterDataContentObserver();
        //}
        CategoryManager.getInstance(this).clearMap();
        laucherFolderName = null;
        if (mApplication.mProgressDialog != null) {
            mApplication.mProgressDialog.dismiss();
        }
    }


    @Override
    public void onEject() {
        Iterator it = ExcuteTaskMap.getInstance().entrySet().iterator();
        try {
            if (mApplication.mNotiManager == null) {
                mApplication.mNotiManager = (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                long mTaskCreateTime = (long) entry.getKey();
                BaseAsyncTask task = RunningTaskMap.getRunningTask(mTaskCreateTime);
                if(task.getmResultTaskInfo()!=null) {
                    int taskType = task.getmResultTaskInfo().getBaseTaskType();
                    if ((taskType==CommonIdentity.FILE_UNCOMPRESSION_TASK && !((FileOperationTask.ExtractFileTask)task).isSourceExists()) ||
                            (taskType==CommonIdentity.FILE_COMPRESSION_TASK && !((FileOperationTask.CompressFileTask)task).isSourceExists())) {
                        mApplication.mFileInfoManager.cancel(task);
                        mApplication.mNotiManager.cancel((int) mTaskCreateTime);
                        RunningTaskMap.removeRunningTask(mTaskCreateTime);
                        ExcuteTaskMap.removeFinishTask(mTaskCreateTime);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.disableCategoryEvent(false);
        }
    }

    @Override
    public void onMounted() {
        super.onMounted();
        mounted(null, false);
    }

    private void mounted(String mountPoint, boolean isUnmounted) {
        try {
            if (CommonUtils.isEditStatus(mApplication) || CommonUtils.isSearchStatus(mApplication)) {
                if (mActivityListener != null) {
                    mActivityListener.onBackPressed();
                }
            }
            if (CommonUtils.isGlobalSearchStatus(mApplication)) {
                onBackGlobalSearch();
            }
            if (mCurrentFragment == mCategoryFragment) {
                updateCategoryContent();
                // set value for mActivitytoCategoryListener if it is null
                if (mActivitytoCategoryListener == null) {
                    if (mCurrentFragment == mCategoryFragment) {
                        mActivitytoCategoryListener = mCategoryFragment;
                        mActivityListener = (IActivityListener) mCategoryFragment;
                    }
                }
                if (mActivitytoCategoryListener != null) {
                    mActivitytoCategoryListener.refreshCategory();
                }
                if (isUnmounted && mActivityListener != null) {
                    mActivityListener.unMountUpdate();
                }
            } else {
                if (isUnmounted && mountPoint != null && mApplication.mCurrentPath != null && mApplication.mCurrentPath.startsWith(mountPoint)) {
                    if (sortPop != null && sortPop.isShowing()) {
                        sortPop.dismiss();
                    }
                    if (morePop != null && morePop.isShowing()) {
                        morePop.dismiss();
                    }
                    CategoryManager.setCurrentMode(CommonIdentity.CATEGORY_MODE);
                    if (mApplication.currentOperation != CommonIdentity.PASTE) {
                        mActivityListener.unMountUpdate();
                    }
                    updateFragment(CommonIdentity.CATEGORY_TAG);
                } else if (isUnmounted && CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE && CategoryManager.mCurrentCagegory < 0) {
                    updateFragment(CommonIdentity.CATEGORY_TAG);
                } else if (!isUnmounted && mApplication.mCurrentPath == null) {
                    FileInfo.mountReceiver = true;
                    updateCategoryItems();
                }

                // if turn to category fragment from list fragment by mount state changing,
                // setExpanded to true with no animation for appBarLayout
                if (appBarLayout != null && mApplication.mCurrentPath == null
                        && CategoryManager.mCurrentCagegory < 0) {
                    appBarLayout.setExpanded(true, false);
                }
            }
            if (mActivitytoCategoryListener == null && isUnmounted) {
                mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
                mActivityListener = (IActivityListener) mCategoryFragment;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isUnmounted) {
            showToastForUnmountCurrentSDCard(mountPoint);
        }
    }

    @Override
    public void onUnmounted(String mountPoint) {
        super.onUnmounted(mountPoint);
        mounted(mountPoint, true);
    }

    private void acquireWakeLock(Context context) {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) (context
                    .getSystemService(Context.POWER_SERVICE));
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "Paste Task");
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }


    @Override
    /* MODIFIED-BEGIN by caiminjie, 2017-09-12,BUG-5325137*/
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.ActivityTheme_appCompat);
        super.onCreate(savedInstanceState);
    }

    @Override
    /* MODIFIED-END by caiminjie,BUG-5325137*/
    public void onClick(View v) {
        mApplication.currentOperation = CommonIdentity.OTHER;
        switch (v.getId()) {
            //float menu click event
            case R.id.extract_item:
            case R.id.extract_item_normal:
            case R.id.float_action_extract:
            case R.id.float_action_extract_left:
                mActivityListener.clickExtractBtn(CommonIdentity.EXTRACT_NORMAL_MODE,null);
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.closeFloatMenu(false);
                }
                break;
            case R.id.compress_item:
            case R.id.float_action_archive:
            case R.id.float_action_archive_left:
            case R.id.compress_item_normal:
                mActivityListener.clickCompressBtn(CommonIdentity.COMPRESS_NORMAL_MODE,null);
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.closeFloatMenu(false);
                }
                break;
            case R.id.global_search_back:
                mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
                changeStatusBarColor(false);
                setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                updateFragment(CommonIdentity.CATEGORY_TAG);
                setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.clearFocus();
                }
                if (mActivityListener != null) {
                    mActivityListener.onGlobalSearchBackPressed();
                }
                mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(mApplication);
                break;
            case R.id.global_search_cancel:
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.setText("");
                }
                if (mActivityListener != null) {
                    mActivityListener.showNoSearchResults(false, "");
                }
                break;
            case R.id.sort_btn:
                showChoiceResourceDialog(CommonIdentity.SORT_MODE, null);
                break;
            case R.id.float_action_share:
            case R.id.float_action_share_left:
            case R.id.share_btn:
                if (mActivityListener != null) {
                    mActivityListener.clickShareBtn();
                    mActivityListener.closeFloatMenu(false);
                }
                break;
            case R.id.delete_btn:
                deleteFiles();
                break;
            case R.id.more_btn:
                if (mActivityListener != null && !mActivityListener.isItemMorePop() || !(CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE)) {
                    int morePopX = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_xoff);
                    int morePopY = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_yoff);
                    if (morePop != null && morePop.isShowing()) {
                        morePop.dismiss();
                        morePop = null;
                    }
                    mActivityListener.closeFloatMenu(true);
                    morePop = new CustomPopupWindowBasedAnchor(
                            initMorePopWindow(), morePopWidth, LayoutParams.WRAP_CONTENT, FileBrowserActivity.this);

                    morePop.showAtLocationBasedAnchor(mBtnMore, morePopX, morePopY);
                }
                break;
            case R.id.global_search_btn:
                mNormalBar.setVisibility(View.GONE);
                mSearchBar.setVisibility(View.GONE);
                mGlobalSearchBar.setVisibility(View.VISIBLE);
                //setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.search_bar_color));
                SharedPreferenceUtils.changePrefCurrTag(this, CommonIdentity.GLOBAL_SEARCH);
                mTagMode = CommonIdentity.GLOBAL_SEARCH;
                updateFragment(CommonIdentity.GLOBAL_SEARCH);
                CategoryManager.mCurrentCagegory = -1;
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.requestFocus();
                }
                InputMethodManager immshow = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (immshow != null) {
                    immshow.showSoftInput(mGlobalSearchView, 0);
                    immshow.focusOut(mGlobalSearchView);
                }
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.setText("");
                }
                isSearchMode = true;
                if (mActivityListener != null) {
                    mActivityListener.clickGlobalSearchBtn();
                    mActivityListener.clearAdapter();
                }
                if(mApplication != null) {
                    CommonUtils.recordCountNumForFA(this, CommonIdentity.FILEMANAGER_GLOBAL_SEARCH_NUMBER, mApplication);
                }
                break;
            case R.id.search_btn:
                if (mCurrentFragment == mCategoryFragment) {
                    SharedPreferenceUtils.changePrefCurrTag(this, CommonIdentity.GLOBAL_SEARCH);
                    mTagMode = CommonIdentity.GLOBAL_SEARCH;
                    updateFragment(CommonIdentity.GLOBAL_SEARCH);
                    CategoryManager.mCurrentCagegory = -1;
                    setFileActionMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH);

                    if (mGlobalSearchView != null) {
                        mGlobalSearchView.setText("");
                    }
                    mApplication.mFileInfoManager.saveListBeforeSearch();
                    if (mActivityListener != null) {
                        mActivityListener.clickGlobalSearchBtn();
                    }
                } else {
                    if (mActivityListener != null) {
                        mActivityListener.clickSearchBtn();
                    }
                    mSaveQueryText = "";
                    setFocusOnSearchView();
                    if (!TextUtils.isEmpty(mSearchView.getQuery()) || mQueryText != null) {
                        mSearchView.setQuery("", false);
                        mQueryText = "";
                    }
                    setSearchMode(true);
                    mApplication.mFileInfoManager.saveListBeforeSearch();
                }
                CommonUtils.recordCountNumForFA(this, CommonIdentity.FILEMANAGER_SEARCH_NUMBER,mApplication);
                break;
            case R.id.floating_action_button:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeFloatMenu(false);
                }
                pasteFiles();
                break;
            case R.id.search_back:
            case R.id.edit_back:
                if (CommonUtils.isCopyNormalStatus(mApplication)) {
                    mApplication.mFileInfoManager.clearPasteList();
                    mBtnEditBack.setVisibility(View.GONE);
                    setActionBarDisplayHomeAsUpEnabled(true);
                    setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                    hideFloatButton();
                    updateBarView();
                    if (mActivityListener != null) {
                        mActivityListener.clearChecked();
                        mActivityListener.updateActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                    }
//                    if (mApplication != null && !TextUtils.isEmpty(mApplication.mCurrentPath)) {
//                        refreshPathAdapter(mApplication.mCurrentPath);
//                    }
                    if (mCurrentFragment == mCategoryFragment) {
                        setActionBarDisplayHomeAsUpEnabled(false);
                        mBtnSort.setVisibility(View.GONE);
                        mBtnSearch.setVisibility(View.GONE);
                        mGlobalView.setVisibility(View.VISIBLE);
                    }
                } else {
                    onBackPressed();
                }
//                refreshNavBar();
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                break;
            case R.id.main_horizontallist_icon:
                if (CommonUtils.isEditStatus(mApplication)) {
                    setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                    if (mBtnEditBack != null) {
                        mBtnEditBack.setVisibility(View.GONE);
                    }
                    if (mBtnDelete != null) {
                        mBtnDelete.setVisibility(View.GONE);
                    }
                    if (mBtnShare != null) {
                        mBtnShare.setVisibility(View.GONE);
                    }
                    if (mBtnSort != null) {
                        mBtnSort.setVisibility(View.VISIBLE);
                    }
                    if (mBtnSearch != null) {
                        mBtnSearch.setVisibility(View.VISIBLE);
                    }
                    if (mActivityListener != null) {
                        mActivityListener.clearChecked();
                        mActivityListener.closeFloatMenu(false);
                    }
                    setActionBarDisplayHomeAsUpEnabled(true);
                    enableScrollActionbar();//support scroll actionbar
                } else if (CommonUtils.isSearchStatus(mApplication)) {
                    mNormalBar.setVisibility(View.VISIBLE);
                    mSearchBar.setVisibility(View.GONE);
                    setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                    if (mGlobalView != null) {
                        mGlobalView.setVisibility(View.GONE);
                    }
                    if (mBtnSort != null) {
                        mBtnSort.setVisibility(View.VISIBLE);
                    }
                    if (mActivityListener != null) {
                        mQueryText = null;
                        mActivityListener.showNoSearchResults(false, mQueryText);
                    }
                    setActionBarDisplayHomeAsUpEnabled(true);
                    enableScrollActionbar();//support scroll actionbar
                }
                if (mMountPointManager == null) {
                    mMountPointManager = MountManager.getInstance();
                }
                String tagMode = null;
                if (mApplication.mCurrentPath != null) {
                    tagMode = CommonUtils.getCurrentTag(mMountPointManager, mApplication.mCurrentPath);
                }
                if (tagMode != null) {
                    mTagMode = tagMode;
                }
                if (CommonUtils.isPhoneTag(mTagMode)) {
                    refreshPathAdapter(mMountPointManager.getPhonePath());
                } else if (CommonUtils.isSDCARDTag(mTagMode)) {
                    refreshPathAdapter(mMountPointManager.getSDCardPath());
                } else if (CommonUtils.isOTGUSBTag(mTagMode)) {
                    refreshPathAdapter(mMountPointManager.getUsbOtgPath());
                }
                break;
            case R.id.phone_storage_container:
                enterRootDir(CommonIdentity.PHONE_TAG, R.drawable.ic_storage_phone_white);
                break;
            case R.id.sd_storage_container:
                enterRootDir(CommonIdentity.SDCARD_TAG, R.drawable.ic_storage_sd_white);
                break;
            case R.id.external_storage_container:
                enterRootDir(CommonIdentity.USBOTG_TAG, R.drawable.ic_storage_usb_white);
                break;
            case R.id.main_horizontallist_path:
                mTagMode = CommonIdentity.CATEGORY_TAG;
                mApplication.mCurrentPath = null;
                isClickHomeIcon = true;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
                if (CommonUtils.isEditStatus(mApplication)) {
                    setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
                }
                if(mApplication.mCurrentStatus==CommonIdentity.FILE_STATUS_EDIT) {
                    setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                    if (mActivityListener != null) {
                        mActivityListener.closeFloatMenu(false);
                    }
                }
                mBtnEditBack.setVisibility(View.GONE);
                mBtnSort.setVisibility(View.GONE);
                mSearchBar.setVisibility(View.GONE);
                mNormalBar.setVisibility(View.VISIBLE);
                onBackPressed();
                break;
            case R.id.select_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.clickEditBtn();
                }
                break;
            case R.id.list_item:
                viewModeChanage(CommonIdentity.LIST_MODE);
                if(mApplication != null) {
                    CommonUtils.recordStatusEventForFA(getApplicationContext(),
                            CommonIdentity.FILEMANAGER_VIEW_KEY,
                            CommonIdentity.FILEMANAGER_LIST_VIEW_STATUS, mApplication);
                }
                break;
            case R.id.grid_item:
                viewModeChanage(CommonIdentity.GRID_MODE);
                if(mApplication != null) {
                    CommonUtils.recordStatusEventForFA(getApplicationContext(),
                            CommonIdentity.FILEMANAGER_VIEW_KEY,
                            CommonIdentity.FILEMANAGER_GRID_VIEW_STATUS, mApplication);
                }
                break;
            case R.id.show_item:
            case R.id.hide_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mApplication != null) {
                    SharedPreferenceUtils.changePrefsShowHidenFile(mApplication);
                    mApplication.isShowHidden = SharedPreferenceUtils.isShowHidden(this);
                    refreshPathAdapter(mApplication.mCurrentPath);
                }
                break;
            case R.id.float_action_delete:
            case R.id.float_action_delete_left:
            case R.id.delete_item_normal:
            case R.id.delete_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeFloatMenu(true);
                }
                deleteFiles();
                break;
            case R.id.float_action_copy:
            case R.id.float_action_copy_left:
            case R.id.copy_item_normal:
            case R.id.copy_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                setFileActionMode(CommonIdentity.FILE_COPY_NORMAL);
                if (pathBarAdapter != null) {
                    pathBarAdapter.notifyAndScroll(fileBrowerList);
                }
                if (CommonUtils.isSearchStatus(mApplication)) {
                    refreshPathAdapter(mApplication.mCurrentPath);
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.clickCopyBtn();
                    mActivityListener.refreshAdapter("", CommonIdentity.UPDATE_ADAPTER_SORT_NOTIFICATION, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    mActivityListener.clearChecked();
                    mActivityListener.closeFloatMenu(false);
                }
//                refreshNavBar();
                setActionBarTitle(getResources().getString(R.string.choice_file));
                break;
            case R.id.float_action_cut:
            case R.id.float_action_cut_left:
            case R.id.cut_item_normal:
            case R.id.cut_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                setFileActionMode(CommonIdentity.FILE_COPY_NORMAL);
                if (pathBarAdapter != null) {
                    pathBarAdapter.notifyAndScroll(fileBrowerList);
                }
                if (CommonUtils.isSearchStatus(mApplication)) {
                    refreshPathAdapter(mApplication.mCurrentPath);
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.clickCutBtn();
                    mActivityListener.refreshAdapter("", CommonIdentity.UPDATE_ADAPTER_SORT_NOTIFICATION, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    mActivityListener.clearChecked();
                    mActivityListener.closeFloatMenu(false);
                }
//                refreshNavBar();
                setActionBarTitle(getResources().getString(R.string.choice_file));
                break;
            case R.id.paste_item:
            case R.id.float_action_paste:
            case R.id.float_action_paste_left:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeFloatMenu(false);
                }
                pasteFiles();
                hideFloatButton();
                break;
            case R.id.paste_item_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeFloatMenu(false);
                }
                pasteFiles();
                break;
            case R.id.share_item_normal:
            case R.id.share_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                mShareFlag = true;

                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.clickShareBtn();
                    mActivityListener.closeFloatMenu(false);
                }
                if(mApplication != null) {
                    CommonUtils.recordCountNumForFA(this, CommonIdentity.FILEMANAGER_SHARE_NUMBER,mApplication);
                }
                break;
            case R.id.rename_item_normal:
            case R.id.rename_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mApplication.currentOperation = CommonIdentity.RENAME;
                    mActivityListener.clickRenameBtn(mQueryText);
                }
                break;
            case R.id.details_item_normal:
            case R.id.detail_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.clickDetailsBtn();
                }
                break;
            case R.id.createfolder_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.clickNewFolderBtn();
                }
                break;
            case R.id.selectall_item_normal:
            case R.id.unselectall_item_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                mTexSelect.setEnabled(false);
                mBtnMore.setEnabled(true);
                if (mActivityListener != null) {
                    mActivityListener.clickSelectAllBtn();
                }
                break;
            case R.id.shortcut_item:
            case R.id.shortcut_item_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                    mActivityListener.clickShortcutBtn();
                }
                break;
            case R.id.move_to_safe_item_normal:
            case R.id.move_to_safe_item:
            case R.id.float_action_private:
            case R.id.float_action_private_left:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mMountPointManager == null) {
                    mMountPointManager = MountManager.getInstance();
                }

                if (CommonUtils.isInPrivacyMode(this)) {
                    if (mActivityListener != null) {
                        mActivityListener.closeItemMorePop();
                        mActivityListener.closeFloatMenu(false);
                        mActivityListener.clickAddPrivateMode();
                    }
                    return;
                }
                break;
            case R.id.permission_setting_layout:
                boolean isEnterPermission = false;
                Intent intent;
                try {
                    // Goto setting application permission
                    intent = new Intent(CommonIdentity.MANAGE_PERMISSIONS);
                    intent.putExtra(CommonIdentity.PACKAGE_NAME, getPackageName());
                    startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
                } catch (Exception e) {
                    isEnterPermission = true;
                }
                if (isEnterPermission) {
                    // Goto settings details
                    Uri packageURI = Uri.parse("package:" + getPackageName());
                    intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
                }

                break;

            case R.id.remove_to_safe_item_normal:
            case R.id.set_public_safe_item:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (CommonUtils.isInPrivacyMode(this)) {
                    if (mActivityListener != null) {
                        mActivityListener.closeItemMorePop();
                        mActivityListener.closeFloatMenu(false);
                        mActivityListener.clickRemovePrivateMode();
                    }
                    return;
                }
                break;
            case R.id.extract_dialog_keep:
                CommonDialogFragment keepdf = (CommonDialogFragment) getFragmentManager().findFragmentByTag(CommonIdentity.EXTRACT_NAME_EXIST_DIALOG_TAG);
                if(keepdf != null){
                    keepdf.dismissAllowingStateLoss();
                }
                CommonDialogFragment.mShowMessageDialogFragment = null;
                if(mActivityListener != null){
                    mActivityListener.clickExtractBtn(CommonIdentity.EXTRACT_RUNNING_MODE,null);
                }
                break;
            case R.id.extract_dialog_rename:
                CommonDialogFragment df = (CommonDialogFragment) getFragmentManager().findFragmentByTag(CommonIdentity.EXTRACT_NAME_EXIST_DIALOG_TAG);
                if(df != null){
                    df.dismissAllowingStateLoss();
                }
                CommonDialogFragment.mShowMessageDialogFragment = null;
                if(mActivityListener != null){
                    mActivityListener.clickExtractBtn(CommonIdentity.EXTRACT_RENAME_MODE,null);
                }
                break;
            case R.id.privacy_info:
                FAExt.showInformedConsentActivity(getApplicationContext());
                break;

            default:
                break;
        }
    }

    private void enterRootDir(String mTag, int mDrawableID) {
        laucherFolderName = null;
        CategoryManager.mCurrentCagegory = -1;
        if (CommonUtils.isEditStatus(mApplication) || CommonUtils.isSearchStatus(mApplication)) {
            setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
        }
        if (mApplication.mCurrentStatus == CommonIdentity.FILE_COPY_NORMAL) {
            setActionBarDisplayHomeAsUpEnabled(false);
        } else {
            setActionBarDisplayHomeAsUpEnabled(true);
        }
        //CategoryManager.setCurrentMode(CommonIdentity.PATH_MODE);
        SharedPreferenceUtils.changePrefCurrTag(mApplication, mTag);
        updateFragment(mTag);
        mTagMode = mTag;
        home_path_icon.setImageResource(mDrawableID);
//        home_path_icon.setAlpha(1.0f);
    }

    public void viewModeChanage(String viewMode) {
        if (morePop != null) {
            morePop.dismiss();
        }
//        if (mActivityListener != null) {
//            mActivityListener.clearAdapter();
//        }
        mApplication.mViewMode = viewMode;
        SharedPreferenceUtils.changePrefViewBy(mApplication, viewMode);
        //switchContent(mListFragment);
        mActivityListener.changeViewMode(viewMode);
    }

    public void showBottomView(String message) {
        Snackbar snackbar = Snackbar.make(snackTextView, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void updateEditBarWidgetState(int selectedCount) {
        if (selectedCount == 0) {
            mBtnShare.setVisibility(View.GONE);
            mBtnDelete.setVisibility(View.GONE);
        } else {
            if (mCanShare || mIsFLorSDDrm) {
                mBtnShare.setVisibility(View.VISIBLE);
            } else {
                mBtnShare.setVisibility(View.GONE);
            }
            mBtnDelete.setVisibility(View.VISIBLE);
        }
        mBtnMore.setEnabled(false);
    }

    public void notifyCategoryDone(boolean isDone) {
    }

    @Override
    public boolean onClose() {
        setSearchMode(false);
        super.onClose();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        queryTextSubmit(query);
        super.onQueryTextSubmit(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String queryString) {
        return queryTextChange(queryString);
    }

    private void updateViewByTag() {
        if (mCurrentFragment == mCategoryFragment) {
            setActionBarElevation(2);
            if (mNormalBar != null) {
                mNormalBar.setVisibility(View.VISIBLE);
            }
            if (mGlobalSearchBar != null) {
                mGlobalSearchBar.setVisibility(View.GONE);
            }

            if (mSearchBar != null) {
                mSearchBar.setVisibility(View.GONE);
            }

            if (mBtnSort != null) {
                mBtnSort.setVisibility(View.GONE);
            }
            if (mBtnShare != null) {
                mBtnShare.setVisibility(View.GONE);
            }
            if (mBtnDelete != null) {
                mBtnDelete.setVisibility(View.GONE);
            }
            if (mBtnSearch != null) {
                mBtnSearch.setVisibility(View.GONE);
            }
            if (mGlobalView != null) {
                mGlobalView.setVisibility(View.VISIBLE);
            }
            setActionBarDisplayHomeAsUpEnabled(false);
        } else if (CommonUtils.isGlobalSearchStatus(mApplication)) {
            setActionBarElevation(2);
            mGlobalSearchBar.setVisibility(View.GONE);
            mBtnShare.setVisibility(View.GONE);
            mBtnDelete.setVisibility(View.VISIBLE);
            mBtnSort.setVisibility(View.VISIBLE);
            mBtnSearch.setVisibility(View.VISIBLE);
            mGlobalView.setVisibility(View.GONE);
        } else {
            setActionBarElevation(0);
            mGlobalSearchBar.setVisibility(View.GONE);
            mBtnShare.setVisibility(View.GONE);
//            if(CategoryManager.mCurrentCagegory != CommonIdentity.CATEGORY_RECENT) {
//                applyRotation(mBtnSort);
//            }
            mBtnSort.setVisibility(View.VISIBLE);
//            applyRotation(mBtnSearch);
            mBtnSearch.setVisibility(View.VISIBLE);
            mGlobalView.setVisibility(View.GONE);
            mBtnDelete.setVisibility(View.GONE);
        }
        updateMoreButtonMenu();
    }


    // update actionbar UI
    private void updateView(int mode) {
        Log.d(TAG, "updateview(), mode=" + mode);
        switch (mode) {
            case CommonIdentity.FILE_STATUS_NORMAL:
                changeStatusBarColor(false);
                setActionBarElevation(0);
                if (mainLayout != null) {
                    mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
                }
                if (mNormalBar != null) {
                    mNormalBar.setVisibility(View.VISIBLE);
                }
                if (mBtnSearch != null) {
                    mBtnSearch.setVisibility(View.VISIBLE);
                }
                if (mGlobalSearchBar != null) {
                    mGlobalSearchBar.setVisibility(View.GONE);
                }
                if (mBtnShare != null) {
                    mBtnShare.setVisibility(View.GONE);
                }
                if (mBtnDelete != null) {
                    mBtnDelete.setVisibility(View.GONE);
                }
                if (mBtnSort != null && CategoryManager.mCurrentCagegory != CommonIdentity.CATEGORY_RECENT) {
                    mBtnSort.setVisibility(View.VISIBLE);
                } else if (mBtnSort != null) {
                    mBtnSort.setVisibility(View.GONE);
                }

                if (pathBarAdapter != null) {
                    pathBarAdapter.notifyAndScroll(fileBrowerList);
                }
                isSearchMode = false;
                setActionBarDisplayHomeAsUpEnabled(true);
                if (mBtnEditBack != null) {
                    mBtnEditBack.setVisibility(View.GONE);
                }
                if (mSearchBar != null) {
                    mSearchBar.setVisibility(View.GONE);
                }
                setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
                if (mApplication.currentOperation == CommonIdentity.RENAME) {
                    mApplication.mFileInfoManager.clearPasteList();
                }
                if (mApplication.mFileInfoManager.getPasteCount() > 0
                        && CategoryManager.mCurrentMode != CommonIdentity.CATEGORY_MODE) {
                    showFloatButton();
                } else {
                    hideFloatButton();
                }
                if (CommonUtils.isPathMode() && filePathBrower != null && mApplication.mCurrentPath != null
                        && filePathBrower.getVisibility() != View.VISIBLE) {
                    filePathBrower.setVisibility(View.VISIBLE);
                    mBtnSearch.setVisibility(View.VISIBLE);
                    mGlobalView.setVisibility(View.GONE);
                }
                if (CategoryManager.mCurrentMode != CommonIdentity.CATEGORY_MODE) {
                    if (mActivityListener != null && !mActivityListener.isShowNoFolderView()) {
                        enableScrollActionbar();//support scroll actionbar
                    }
                }
                selectCount = 0;
                String path = mMountPointManager
                        .getDescriptionPath(mApplication.mCurrentPath);
                if (mApplication.mFileInfoManager.getPasteCount() > 0) {

                } else if (path != null && !path.isEmpty()) {
                    String result = null;
                    if (path.contains(MountManager.SEPARATOR)) {
                        result = path.substring(path
                                .lastIndexOf(MountManager.SEPARATOR) + 1);
                        setActionBarTitle(result);
                    } else {
                        setActionBarTitle(path);
                    }
                }
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.clearFocus();
                }
                break;
            case CommonIdentity.FILE_STATUS_EDIT:
                cancelScrollActionbar();//cancel scroll actionbar
                appBarLayout.setExpanded(true);
                mNormalBar.setVisibility(View.VISIBLE);
                mGlobalSearchBar.setVisibility(View.GONE);
                mBtnSearch.setVisibility(View.GONE);
                mBtnShare.setVisibility(View.GONE);
                mBtnDelete.setVisibility(View.GONE);
                setActionBarDisplayHomeAsUpEnabled(false);
                mBtnEditBack.setVisibility(View.VISIBLE);
                mBtnSort.setVisibility(View.GONE);
                mSearchBar.setVisibility(View.GONE);
                mGlobalView.setVisibility(View.GONE);
                mGlobalSearchBar.setVisibility(View.GONE);
                hideFloatButton();
                //setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.clearFocus();
                }

                if (CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication)) {
                    if (filePathBrower != null && filePathBrower.getVisibility() != View.VISIBLE) {
                        filePathBrower.setVisibility(View.VISIBLE);
                    }
                    getPathBrowerText();
                }
                if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                    setActionBarElevation(2);
                } else {
                    setActionBarElevation(0);
                }
                break;
            case CommonIdentity.FILE_STATUS_SEARCH:
                cancelScrollActionbar();//cancel scroll actionbar
                mNormalBar.setVisibility(View.GONE);
                mSearchBar.setVisibility(View.VISIBLE);
                mGlobalSearchBar.setVisibility(View.GONE);
                hideFloatButton();
                if (pathBarAdapter != null) {
                    pathBarAdapter.notifyAndScroll(fileBrowerList);
                }
                break;
            case CommonIdentity.FILE_STATUS_GLOBALSEARCH:
                cancelScrollActionbar();//cancel scroll actionbar
                changeStatusBarColor(false);
                mNormalBar.setVisibility(View.GONE);
                mSearchBar.setVisibility(View.GONE);
                mGlobalSearchBar.setVisibility(View.VISIBLE);
                InputMethodManager immshow = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (immshow != null) {
                    immshow.showSoftInput(mGlobalSearchView, 0);
                    immshow.focusOut(mGlobalSearchView);
                }
                if (mGlobalSearchView != null) {
                    mGlobalSearchView.clearFocus();
                }
                break;
            case CommonIdentity.FILE_COPY_NORMAL:
                cancelScrollActionbar();//cancel scroll actionbar
                changeStatusBarColor(false);
                mNormalBar.setVisibility(View.VISIBLE);
                mGlobalSearchBar.setVisibility(View.GONE);
                mBtnShare.setVisibility(View.GONE);
                mBtnDelete.setVisibility(View.GONE);
                if (mCurrentFragment != mCategoryFragment) {
                    mBtnSearch.setVisibility(View.VISIBLE);
                    mBtnSort.setVisibility(View.VISIBLE);
                }
                if (CommonUtils.isPathMultiScreenChanage(mTagMode, mApplication)) {
                    if (filePathBrower != null && filePathBrower.getVisibility() != View.VISIBLE) {
                        filePathBrower.setVisibility(View.VISIBLE);
                    }
                    getPathBrowerText();
                }
                mSearchBar.setVisibility(View.GONE);
                if (mApplication.mFileInfoManager.getPasteCount() > 0 && CategoryManager.mCurrentMode != CommonIdentity.CATEGORY_MODE) {
                    showFloatButton();
                } else {
                    hideFloatButton();
                }
                if (mApplication.mFileInfoManager.getPasteCount() != 0) {
                    if (mBtnEditBack != null) {
                        setActionBarDisplayHomeAsUpEnabled(false);
                        mBtnEditBack.setVisibility(View.VISIBLE);
                    }
                    setActionBarTitle(getResources().getString(R.string.choice_file));
                }
                selectCount = 0;
                setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
                break;
            default:
                break;
        }
        updateMoreButtonMenu();
    }

    protected void showForbiddenDialog() {
        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        AlertDialogFragment forbiddenDialogFragment = builder
                .setTitle(R.string.drm_forwardforbidden_title)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(R.string.drm_forwardforbidden_message)
                .setCancelable(false).setCancelTitle(R.string.ok).create();
        forbiddenDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
    }

    @Override
    public void updateEditBar(int count, boolean isHasDir, boolean isHasZip, boolean isHasDrm,
                              boolean canShare, boolean isHasAllPrivate,boolean isFLorSDDrm) {
        if (CommonUtils.isNormalStatus(mApplication)) {
            mBtnMore.setEnabled(true);
            return;
        }
        changeStatusBarColor(true);
        setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.edit_bar_bg));
        filePathBrower.setBackgroundColor(getResources().getColor(R.color.actionbar_edit_bg));
        if (pathBarAdapter != null) {
            pathBarAdapter.notifyAndScroll(fileBrowerList);
        }
        setActionBarDisplayHomeAsUpEnabled(false);
        mBtnEditBack.setVisibility(View.VISIBLE);
        setActionBarTitle("" + count);
        selectCount = count;
        mIsHasDir = isHasDir;
        mIsHasDrm = isHasDrm;
        mCanShare = canShare;
        mIsHasZip = isHasZip;
        mIsFLorSDDrm = isFLorSDDrm;
        mHasAllPrivate = isHasAllPrivate;
        if (mActivityListener != null) {
            mActivityListener.showFloatMenu(selectCount, isHasDir, isHasZip);
        }
        updateEditBarWidgetState(count);
        updateMoreButtonMenu();
    }

    @Override
    public void setFileActionMode(int mode) {
        mApplication.mCurrentStatus = mode;
        SharedPreferenceUtils.changePrefsStatus(this, mode);
    }

    @Override
    public void updateBarView() {
        updateView(getFileMode());
        updateBarTitle(CommonIdentity.UNKNOWN_TASK);
    }

    public void updateBarTitle(int mTaskType) {
        if (mApplication == null) return;

        if (mTaskType == CommonIdentity.PASTE_COPY_TASK || mTaskType == CommonIdentity.PASTE_CUT_TASK) {
            setActionBarDisplayHomeAsUpEnabled(true);
            mBtnEditBack.setVisibility(View.GONE);
        }
        if (!CommonUtils.isEditStatus(mApplication)) {
            setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
        }
        if (filePathBrower != null) {
            filePathBrower.setBackgroundColor(getResources().getColor(R.color.filemanager_theme_color));
        }
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        String path = mMountPointManager
                .getDescriptionPath(mApplication.mCurrentPath);
        if (mCurrentFragment == mListFragment && mApplication.mFileInfoManager.getPasteCount() > 0) {
            if (mBtnEditBack != null) {
                setActionBarDisplayHomeAsUpEnabled(false);
                mBtnEditBack.setVisibility(View.VISIBLE);
//                setFileActionMode(CommonIdentity.FILE_COPY_NORMAL);
            }
            setActionBarTitle(R.string.choice_file);
        } else if (!TextUtils.isEmpty(path)) {
            String result;
            if (mApplication.mFileInfoManager.getPasteCount() == 0) {
                if (path.contains(MountManager.SEPARATOR)) {
                    result = path.substring(path
                            .lastIndexOf(MountManager.SEPARATOR) + 1);
                    setActionBarTitle(result);
                } else {
                    setActionBarTitle(path);
                }
            }
        } else if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
            if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_RECENT) {
                setActionBarTitle(R.string.main_recents);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_APKS) {
                setActionBarTitle(R.string.main_installers);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_BLUETOOTH) {
                setActionBarTitle(R.string.category_bluetooth);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_DOCS) {
                setActionBarTitle(R.string.main_document);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_DOWNLOAD) {
                setActionBarTitle(R.string.category_download);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_MUSIC) {
                setActionBarTitle(R.string.category_audio);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_PICTURES) {
                setActionBarTitle(R.string.category_pictures);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_VEDIOS) {
                setActionBarTitle(R.string.category_vedios);
            } else if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_ARCHIVES) {
                setActionBarTitle(R.string.category_archives);
            } else {
                /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-10-11,BUG-5395138*/
                setActionBarTitle(R.string.app_name_new);
            }
        } else {
            setActionBarTitle(R.string.app_name_new);
            /* MODIFIED-END by Chuanzhi.Shao,BUG-5395138*/
        }
    }

    @Override
    public int getFileActionMode() {
        return getFileMode();
    }

    @Override
    public void updateNormalBarView() {
        if (filePathBrower != null && mCurrentFragment != mCategoryFragment && CategoryManager.mCurrentCagegory < 0
                && !CommonUtils.isGlobalSearchStatus(mApplication)) {
            filePathBrower.setVisibility(View.VISIBLE);
            mGlobalView.setVisibility(View.GONE);
        }
        getPathBrowerText();
        updateNormalBar();
    }

    @Override
    public void changeSearchMode(boolean flag) {
        setSearchMode(flag);
    }

    @Override
    public void toShowForbiddenDialog() {
        showForbiddenDialog();
    }

    @Override
    public void pasteBtnUpdated() {

    }

    @Override
    public void updateCategoryNormalBarView() {
        updateCategoryNormalBar();
    }

    @Override
    public void toReleaseWakeLock() {
        releaseWakeLock();
    }

    private void updateCategoryItems() {
        if (FileInfo.mountReceiver && FileInfo.scanFinishReceiver) {
            switchContentByViewMode(false);
        }
    }

    @Override
    public void onScannerFinished() {
        if (mActivitytoCategoryListener == null) {
            if (mCategoryFragment == null) {
                mCategoryFragment = new CategoryFragment();
            }
            mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
            mActivityListener = (IActivityListener) mCategoryFragment;
        }
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.disableCategoryEvent(true);// add for

            mActivitytoCategoryListener.onScannerFinished();
//            if (mActivityListener != null) {
//                mActivityListener.onScannerFinished();
//            }
        }
        if(isMounted) {
            isMounted = false;
            removeCategoryCache(true, false);
        }
    }

    @Override
    public void onScannerStarted() {
        if (mActivitytoCategoryListener == null) {
            if (mCategoryFragment == null) {
                mCategoryFragment = (CategoryFragment) new CategoryFragment();
            }
            mActivitytoCategoryListener = (IActivitytoCategoryListener) mCategoryFragment;
            mActivityListener = (IActivityListener) mCategoryFragment;
        }
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.onScannerStarted();
            if (mActivityListener != null) {
                mActivityListener.onScannerStarted();
            }
        }
    }

    private void updateCategoryContent() {
        mCurrentFragment = mCategoryFragment;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMoreButtonMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                if(mApplication != null) {
                    CommonUtils.recordCountNumForFA(this, CommonIdentity.FILEMANAGER_ACTIONBAR_HOME_CLICK_NUMBER,mApplication);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMoreButtonMenu() {
        if (mBtnMore == null)
            return;
        if (mCurrentFragment == mCategoryFragment) {
            mBtnMore.setVisibility(View.GONE);
            if (mPrivacyInfoBtn != null) {
                mPrivacyInfoBtn.setVisibility(View.VISIBLE);
            }
        } else {
//            applyRotation(mBtnMore);
            mBtnMore.setVisibility(View.VISIBLE);
            if (CommonUtils.isEditStatus(mApplication)) {
                mBtnMore.setEnabled(true);
            }
            if(mPrivacyInfoBtn != null){
                mPrivacyInfoBtn.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CommonUtils.isGlobalSearchStatus(mApplication)) {
                onBackGlobalSearch();
            } else if (mCurrentFragment == mCategoryFragment) {
                // TODO, in order to faster the launch speed, do not finish this activity
                mApplication.mFileInfoManager.clearAll();
                onBackPressed();
            } else {
                onBackPressed();
            }
        }
        // use Toolbar as actionBar, after pressing MenuKey in some projects,
        // the BackKey and VolumeKey can not work. As we have no menu item, just return.
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return false;
    }

    public void getPathBrowerText() {
        path = mMountPointManager
                .getDescriptionPath(mApplication.mCurrentPath);
        // If it is root path, highlight the storage icon
        setPhonePathRootIcon(mApplication.mCurrentPath);
        if (home_path_icon != null && mApplication.mCurrentPath != null && (mMountPointManager.isPhoneRootPath(mApplication.mCurrentPath))) {
            home_path_icon.setAlpha(1.0f);
        } else if (home_path_icon != null) {
            home_path_icon.setAlpha(0.5f);
        }
        if (fileBrowerList != null && path != null && !path.equals(comparepath)) {
            paths = path.split(File.separator);
            comparepath = path;
            if(pathBarAdapter!=null){
                pathBarAdapter.setList(paths);
                pathBarAdapter.notifyAndScroll(fileBrowerList);
            }else {
                pathBarAdapter = new PathBarAdapter(this, paths);
                fileBrowerList.setAdapter(pathBarAdapter);
                pathBarAdapter.setOnItemClickLitener(this);
                ScrollSpeedLinearLayoutManger linearLayoutManager = new ScrollSpeedLinearLayoutManger(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                fileBrowerList.setLayoutManager(linearLayoutManager);
                fileBrowerList.scrollToPosition(paths.length-1);
            }
        }
    }

    private String[] paths;
    private String path;
    public static String comparepath = "";
    private StringBuilder absolutePath;

    @Override
    public void onItemClick(int position) {
        absolutePath = getAbsolutePath(position);
        if (CommonUtils.isEditStatus(mApplication) || CommonUtils.isSearchStatus(mApplication)) {
            if (mActivityListener != null) {
                mActivityListener.onBackPressed();
            }
        }
        if (position != paths.length - 1) {
            mApplication.mCurrentPath = absolutePath.toString();
            refreshPathAdapter(mApplication.mCurrentPath);
        }
    }

    private StringBuilder getAbsolutePath(int position) {
        StringBuilder absolutePath = new StringBuilder();
        String rootPath = null;
        try {
            if (CommonUtils.isPhoneTag(mTagMode)) {
                rootPath = mMountPointManager.getPhonePath();
            } else if (CommonUtils.isSDCARDTag(mTagMode)) {
                rootPath = mMountPointManager.getSDCardPath();
            } else if (CommonUtils.isOTGUSBTag(mTagMode)) {
                rootPath = mMountPointManager.getUsbOtgPath();
            } else if (CommonUtils.isGlobalSearchTag(mTagMode)) {
                String[] temp = mMountPointManager.getDescriptionPath(mApplication.mCurrentPath).split(File.separator);
                if (temp[0].equals(getResources().getString(R.string.draw_left_phone_storage_n))) {
                    rootPath = mMountPointManager.getPhonePath();
                } else if (temp[0].equals(getResources().getString(R.string.usbotg_n))) {
                    rootPath = mMountPointManager.getUsbOtgPath();
                } else if (temp[0].equals(getResources().getString(R.string.sd_card))) {
                    rootPath = mMountPointManager.getSDCardPath();
                }
            }
            for (int i = 0; i <= position; i++) {
                if (i == position && position != 0) {
                    absolutePath.append(paths[i]);
                } else if (i == 0 && i != position) {
                    absolutePath.append(rootPath + File.separator);
                } else if (i == position && position == 0) {
                    absolutePath.append(rootPath);
                } else {
                    absolutePath.append(paths[i] + File.separator);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return absolutePath;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isChangeMultiScreen = false;
        if (mApplication != null) {
            mApplication.mCurrentLocation = CommonIdentity.FILE_MANAGER_LOCATIONE;
        }
        if (laucherFolderName != null && !laucherFolderName.equals("") && !PermissionUtil.isAllowPermission(this)) {
            switchShortcut(laucherFolderName, true);
        }

        if (mApplication != null && mApplication.mDataContentObserver != null) {
            mApplication.mDataContentObserver.startFileTimerWatcher();
        }
    }

    public static boolean isMorePopShow() {
        if (morePop != null) {
            return morePop.isShowing();
        }
        return false;
    }

    @Override
    public void isDeleteFlag(boolean flag) {
        if (flag) {
            deleteFlag = false;
        } else {
            deleteFlag = true;
        }
    }

    @Override
    public void reSearch() {
        if (mQueryText == null) {
            return;
        }
        if (CommonUtils.isSearchStatus(mApplication)
                && CategoryManager.mCurrentMode != CommonIdentity.CATEGORY_MODE
                && mApplication.currentOperation !=
                CommonIdentity.RENAME) {
            mSearchView.setQuery(mQueryText, false);
            requestSearch(mQueryText);
        } else if (mApplication.currentOperation != CommonIdentity.RENAME) {
            refreshPathAdapter(mApplication.mCurrentPath);
            mSearchFromEdit = true;
            mSearchView.setQuery(mQueryText, false);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: handle exception
            }
            requestSearch(mQueryText);
        }
        if (pathBarAdapter != null && mApplication.currentOperation == CommonIdentity.RENAME
                && filePathBrower.isShown()) {
            pathBarAdapter.notifyAndScroll(fileBrowerList);
        }
    }

    boolean isFromPermission = false;

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
            PermissionUtil.isShowPermissionDialog = false;
            for (String permission : permissions) {
                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                    if (CommonUtils.hasM() && mPermissionFragment != null
                            && mCurrentFragment == mPermissionFragment) {

                        if (!PermissionUtil.isSecondRequestPermission(this)) {
                            mPermissionFragment.updateView(1);
                        }
                        PermissionUtil.setSecondRequestPermission(this);
                    } else {
                        laucherFolderName = null;
                        mApplication.mCurrentPath = null;
                        finish();
                    }
                } else if (CommonUtils.hasM()) {
                    if(CommonUtils.isInMultiWindowMode(this)&& runCreateCount>1) {
                        recreate();
                    }else {
                        isFromPermission = true;
                        onPemissinRequestResult(false);
                        if (enterPermissionResultCount == 2) {
                            enterPermissionResultCount = 1;
                            mBrowserHandler.sendEmptyMessage(CommonIdentity.NO_AVAILABLE_STORAGE);
                        } else {
                            enterPermissionResultCount++;
                        }
                    }
                }
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onNewIntent(Intent intent) {
        laucherFolderName = intent.getStringExtra("foldername");
        intent.removeExtra("foldername");
        isClickNotification = intent.getBooleanExtra("notification", false);
        long turnTime = intent.getLongExtra("turnTime", 0);
        isShowDialog = intent.getBooleanExtra("showDialog", false);
        long createTime = intent.getLongExtra("createTime", 0);
        isCancelTask = intent.getBooleanExtra("cancel", false);
        long cancelTime = intent.getLongExtra("cancelTime", 0);
        if (isShowDialog) {
            createProgressDialog(createTime);
        }
        if (isCancelTask && mActivityListener != null) {
            mApplication.cancelTaskTime = cancelTime;
            mActivityListener.clickProgressBtn(CommonIdentity.CANCEL_TASK);
        }
        if (isClickNotification) {
            createProgressDialog(turnTime);
        }
        if (laucherFolderName == null || laucherFolderName.equals("")) {
            isFromShortcut = false;
            return;
        }
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.dismissSafeDialog();
        }
        if (laucherFolderName != null && !PermissionUtil.isAllowPermission(this)) {
            onPemissinRequestResult(true);
        }
    }

    private void createProgressDialog(long createTime) {
        if (mApplication.mProgressDialog != null) {
            if (mApplication.mNotiManager != null
                    && mApplication.mNotiManager.getActiveNotifications().length > 0) {
                mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_ALL_SHOW_MODE;
            } else {
                mApplication.mCurrentProgressMode = CommonIdentity.PROGRESS_DIALOG_MODE;
            }
            try {
                mApplication.mProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mApplication.mProgressDialog = null;
        }
        if (mApplication.mProgressDialog == null && (mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_DIALOG_MODE
                || mApplication.mCurrentProgressMode == CommonIdentity.PROGRESS_ALL_SHOW_MODE)) {
            TaskInfo currTask = TaskInfoMap.getTaskInfo(createTime);
            if (currTask == null) return;
            mApplication.mProgressDialog = ProgressPopupWindow.newInstance(
                    this, currTask);
            mApplication.mProgressDialog.setTitle(CommonUtils.getNotificationTitle(
                    this, currTask.getProgressInfo().getProgressTaskType()));
            mApplication.mProgressDialog.setCancelable(false);
            if (SafeManager.mCurrentSafeCategory != 12 && !mApplication.mProgressDialog.isShowing()) {
                mApplication.mProgressDialog.show();
            }
        }
        Message msg1 = new Message();
        Bundle mBundle1 = new Bundle();
        mBundle1.putSerializable(CommonIdentity.RESULT_TASK_KEY, TaskInfoMap.getTaskInfo(createTime));
        msg1.setData(mBundle1);
        if (mCurrentFragment == mListFragment) {
            mListFragment.mTaskResultHandler.sendMessage(msg1);
        } else if (mCurrentFragment == mCategoryFragment) {
            mCategoryFragment.mTaskResultHandler.sendMessage(msg1);
        }
    }

    public void onPemissinRequestResult(boolean isShortcut) {
        if (mCurrentFragment == mPermissionFragment && !isShortcut) {
            showActionBar();
        }
        if (laucherFolderName != null && !PermissionUtil.isAllowPermission(this)) {
            isFromShortcut = true;
            if (mToastHelper == null) {
                mToastHelper = new ToastHelper(this);
            }
            File launcherFile = new File(laucherFolderName);
            if (!launcherFile.exists() || (!mApplication.isShowHidden && launcherFile.isHidden())) {
                laucherFolderName = null;
                mToastHelper.showToast(R.string.shortcut_no_exist);
                if (mApplication != null && mApplication.mCurrentPath != null) {
                    //mApplication.mCurrentPath = null;
                    return;
                }
            } else if (isPathInvalid(laucherFolderName)) {
                laucherFolderName = null;
                mToastHelper.showToast(R.string.shortcut_no_exist);
                if (mApplication != null && mApplication.mCurrentPath != null) {
                    //mApplication.mCurrentPath = null;
                    return;
                }
            } else if (!isShortcut && mApplication != null) {
                CategoryManager.setCurrentMode(CommonIdentity.PATH_MODE);
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.closeItemMorePop();
                }
                if (filePathBrower != null && filePathBrower.getVisibility() == View.GONE) {
                    filePathBrower.setVisibility(View.VISIBLE);
                }
                mApplication.mCurrentPath = laucherFolderName;
                laucherFolderName = null;
                switchContentByViewMode(false);

                return;
            } else if (isShortcut && mActivityListener != null) {
                String viewMode = SharedPreferenceUtils.getPrefsViewBy(mApplication);
                hideFloatButton();
                CategoryManager.setCurrentMode(CommonIdentity.PATH_MODE);
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                mActivityListener.closeItemMorePop();
                if (mApplication.mCurrentPath != null && !mApplication.mCurrentPath.equals(laucherFolderName)) {
                    if (mListFragment != null) {
                        mListFragment.clearAdapter();
                    }
                }
                if (mActivityListener != null) {
                    mActivityListener.clickShortcutToNormal();
                }
                if (filePathBrower != null && filePathBrower.getVisibility() == View.GONE) {
                    filePathBrower.setVisibility(View.VISIBLE);
                }
                mApplication.mCurrentPath = laucherFolderName;
                laucherFolderName = null;
                switchContentByViewMode(false);
            } else if (isShortcut && mApplication.mCurrentPath == null) {
                if (morePop != null && morePop.isShowing()) {
                    morePop.dismiss();
                }
                mActivityListener.closeItemMorePop();
                if (filePathBrower != null && filePathBrower.getVisibility() == View.GONE) {
                    filePathBrower.setVisibility(View.VISIBLE);
                }
                CategoryManager.setCurrentMode(CommonIdentity.PATH_MODE);
                mApplication.mCurrentPath = laucherFolderName;
                laucherFolderName = null;
                switchContentByViewMode(false);
            }
        }
        if (isShortcut || !isFromPermission) {
            return;
        }
        if (mCurrentFragment != mPermissionFragment) {
            laucherFolderName = null;
        }
        if (mApplication != null && mApplication.mCurrentPath != null) {
            return;
        }
        isFromPermission = false;
        switchContent(mCategoryFragment);
        CategoryManager.setCurrentMode(CommonIdentity.CATEGORY_MODE);
        mApplication.mFAExt.checkCollectOnWhenLaunch(mApplication);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!PermissionUtil.isAllowPermission(this)) {
            if (CommonUtils.hasM()) {
                if(requestCode==PermissionUtil.JUMPTOSETTINGFORSTORAGE&&CommonUtils.isInMultiWindowMode(this)&& runCreateCount>1){
                    recreate();
                }else {
                    isFromPermission = true;
                    onPemissinRequestResult(false);
                    mBrowserHandler.sendEmptyMessage(CommonIdentity.NO_AVAILABLE_STORAGE);
                }
            }
        }
    }

    public void setPhonePathRootIcon(String path) {
        if (TextUtils.isEmpty(path) || mMountPointManager == null) return;

        try {
            String startString = mMountPointManager.getPhonePath();
            if (startString != null
                    && path.startsWith(mMountPointManager.getPhonePath())) {
                home_path_icon.setImageResource(R.drawable.ic_storage_phone_white);
                mTagMode = CommonIdentity.PHONE_TAG;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.PHONE_TAG);
                return;
            }
            startString = mMountPointManager.getSDCardPath();
            if (startString != null
                    && path.startsWith(startString)) {
                home_path_icon.setImageResource(R.drawable.ic_storage_sd_white);
                mTagMode = CommonIdentity.SDCARD_TAG;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.SDCARD_TAG);
                return;
            }
            startString = mMountPointManager.getUsbOtgPath();
            if (startString != null
                    && path.startsWith(startString)) {
                home_path_icon.setImageResource(R.drawable.ic_storage_usb_white);
                mTagMode = CommonIdentity.USBOTG_TAG;
                SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.USBOTG_TAG);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchShortcut(String path, boolean isFragment) {
        try {
            if (path != null && mMountPointManager != null) {
                if (mMountPointManager.getPhonePath() != null
                        && path.startsWith(mMountPointManager.getPhonePath())) {
                    if (isFragment) {
                        updateFragment(CommonIdentity.PHONE_TAG);
                    } else {
                        home_path_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_storage_phone_white, null));
                        mTagMode = CommonIdentity.PHONE_TAG;
                        SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.PHONE_TAG);
                    }
                } else if (mMountPointManager.getSDCardPath() != null
                        && path.startsWith(mMountPointManager.getSDCardPath())) {
                    if (isFragment) {
                        updateFragment(CommonIdentity.SDCARD_TAG);
                    } else {
                        home_path_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_storage_sd_white, null));
                        mTagMode = CommonIdentity.SDCARD_TAG;
                        SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.SDCARD_TAG);
                    }
                } else if (mMountPointManager.getUsbOtgPath() != null
                        && path.startsWith(mMountPointManager.getUsbOtgPath())) {
                    if (isFragment) {
                        updateFragment(CommonIdentity.USBOTG_TAG);
                    } else {
                        home_path_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_storage_usb_white, null));
                        mTagMode = CommonIdentity.USBOTG_TAG;
                        SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.USBOTG_TAG);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshStorageInfoUiForLand(View view) {
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        // Use view to adapter phone storage progress bar width, avoid too wide only  phone storage display in land
        if (!mApplication.mPortraitOrientation && view != null) {
            if (!mMountPointManager.isOtgMounted() && !mMountPointManager.isSDCardMounted()) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    public int getSlideLimite() {
        if (mApplication.mPortraitOrientation) {
            if (SharedPreferenceUtils.getPrefsViewBy(mApplication).equals(CommonIdentity.GRID_MODE)) {
                return 12;
            } else {
                return getListPortLimita();
            }
        } else {
            if (SharedPreferenceUtils.getPrefsViewBy(mApplication).equals(CommonIdentity.GRID_MODE)) {
                return 10;
            } else {
                return getListLandLimita();
            }
        }
    }

    private int getListLandLimita() {
        int count = 0;
        if (CommonUtils.getScreenWidth(this) <= 800) {
            count = 3;
        } else {
            count = 4;
        }
        return count;
    }

    private int getListPortLimita() {
        int count = 0;
        if (CommonUtils.getScreenWidth(this) <= 480) {
            count = 6;
        } else {
            count = 7;
        }
        return count;
    }

    private void setActionBarElevation(float elevation) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(elevation);
        }
    }

    public void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && (mCurrentFragment != mCategoryFragment)) {
            actionBar.hide();
        }
    }

    public void HideActionbar(boolean flag) {
        if (!mApplication.mPortraitOrientation && CommonUtils.isEditStatus(mApplication)) {
            if (flag && (mCurrentFragment != mCategoryFragment)) {
                hideActionBar();
            } else {
                if (mCurrentFragment != mPermissionFragment) {
                    showActionBar();
                }
            }
        } else {
            if (mCurrentFragment != mPermissionFragment) {
                showActionBar();
            }
        }
    }

    private void onBackGlobalSearch() {
        mainLayout.setBackgroundColor(getResources().getColor(R.color.white));
        changeStatusBarColor(false);
        setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
        SharedPreferenceUtils.changePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
        mTagMode = CommonIdentity.CATEGORY_TAG;
        updateFragment(CommonIdentity.CATEGORY_TAG);
        setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_color));
        if (mGlobalSearchView != null) {
            mGlobalSearchView.clearFocus();
        }
        if (mActivityListener != null) {
            mActivityListener.onGlobalSearchBackPressed();
        }
        mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(mApplication);
    }

    /**
     * this method is used called when click "paste"
     */
    private void pasteFiles() {
        if (mActivityListener != null) {
            mActivityListener.closeItemMorePop();
            mApplication.currentOperation = CommonIdentity.PASTE;
            acquireWakeLock(this.getApplicationContext());
            mActivityListener.clickPasteBtn();
        }
        hideFloatButton();
    }

    /**
     * this method is used called when click "delete"
     */
    private void deleteFiles() {
        if (mActivityListener != null) {
            mActivityListener.closeItemMorePop();
            mApplication.currentOperation = CommonIdentity.DETETE;
            mActivityListener.clickDelteBtn(CommonIdentity.NORMAL_DELETE_TASK);
        }
    }

    @Override
    public void refreashSafeFilesCategory() {

    }

    @Override
    public void cancelScrollActionbar() {
        Log.d(TAG, "cancelScrollActionbar()");
        if (appBarLayout == null) return;

        mParams = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
        if (mParams == null) return;

        if (mParams.getScrollFlags() != CommonIdentity.SCROLL_FLAG_SCROLL_CANCEL) {
            mParams.setScrollFlags(CommonIdentity.SCROLL_FLAG_SCROLL_CANCEL);
            if (mCurrentFragment != mCategoryFragment) {
                appBarLayout.setExpanded(true);
            }
        }
    }

    @Override
    public void enableScrollActionbar() {
        Log.d(TAG, "enableScrollActionbar()");
        if (appBarLayout == null) return;

        mParams = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
        if (mParams == null) return;

        if (mParams.getScrollFlags() != CommonIdentity.SCROLL_FLAG_SCROLL_START) {
            mParams.setScrollFlags(CommonIdentity.SCROLL_FLAG_SCROLL_START);
        }
        appBarLayout.setExpanded(true);
        refreshNavBar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            mSaveQueryText = mQueryText;
            isEnterSaveInstanceState = true;
            if (mActivityListener != null) {
                mSaveCheckedList.clear();
                if (mActivityListener.getCheckedList() != null) {
                    mSaveCheckedList.addAll(mActivityListener.getCheckedList());
                }
                mSaveSelectedList.clear();
                if (mActivityListener.saveSelectedList() != null) {
                    mSaveSelectedList.addAll(mActivityListener.saveSelectedList());
                }
            }
            if (mApplication.mFileInfoManager != null) {
                mSavePastedList.clear();
                if (mApplication.mFileInfoManager.getPasteList() != null) {
                    mSavePastedList.addAll(mApplication.mFileInfoManager.getPasteList());
                }
                mPasteOperation = mApplication.mFileInfoManager.getPasteType();
            }
            isShowNoStorageDialog = false;
            // To determine whether a no storage dialog displays
            if (mNoStorageDialog != null && mNoStorageDialog.isAdded()) {
                isShowNoStorageDialog = true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isEnterSaveInstanceState = false;
        mBrowserHandler.sendEmptyMessage(CommonIdentity.RESTORE_INSTANCE_STATE);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        CommonDialogFragment mCommonDialog = CommonDialogFragment.getInstance();
        switch (i) {
            case ProgressDialog.BUTTON_NEGATIVE:
                if (mCommonDialog != null) {
                    String mTag = mCommonDialog.getDailogTag();
                    if (mTag != null && !mTag.isEmpty() && mTag.equals(CommonIdentity.EXIT_DIALOG_TAG)) {
                        try {
                            WaittingTaskList.clearWaittingTask();
                            mActivityListener.clickProgressBtn(CommonIdentity.CANCEL_ALL_TASK);
                            mApplication.mCurrentStatus = CommonIdentity.FILE_STATUS_NORMAL;
                            SharedPreferenceUtils.changePrefsStatus(mApplication, CommonIdentity.FILE_STATUS_NORMAL);
                            isEnterSaveInstanceState = false;
                            mApplication.mCache.clearCache();
                            CommonDialogFragment.mShowMessageDialogFragment = null;
                            finish();
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    }

                } else if (mActivityListener != null) {
                    mApplication.cancelTaskTime = ProgressPopupWindow.getCreateTaskTime();
                    mActivityListener.clickProgressBtn(i);
                }
                break;
            case ProgressDialog.BUTTON_POSITIVE:
                if (mActivityListener != null) {
                    if (mCommonDialog != null) {
                        String mTag = mCommonDialog.getDailogTag();
                        if (mTag != null && !mTag.isEmpty() && mTag.equals(CommonIdentity.DELETE_DIALOG_TAG)) {
                            mActivityListener.deleteFileResponse();
                        } else if (mTag != null && !mTag.isEmpty() && mTag.equals(CommonIdentity.REMOVE_PRIVATE_DIALOG_TAG)) {
                            mActivityListener.removePrivateMode();
                        } else if (mTag != null && !mTag.isEmpty() && mTag.equals(CommonIdentity.EXTRACT_DIALOG_TAG)) {
                            mActivityListener.clickExtractBtn(CommonIdentity.EXTRACT_REPEAT_NAME_MODE,null);
                        } else if (mTag != null && !mTag.isEmpty() && mTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG)) {
                            mActivityListener.clickExtractBtn(CommonIdentity.EXTRACT_RENAME_RUNNING_MODE,mCommonDialog.getFolderName());
                        } else if (mTag != null && !mTag.isEmpty() && mTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG)) {
                            mActivityListener.clickCompressBtn(CommonIdentity.COMPRESS_RENAME_RUNNING_MODE,mCommonDialog.getFolderName());
                        }
                        CommonDialogFragment.mShowMessageDialogFragment = null;
                    } else {
                        String mTag = CommonDialogFragment.getDailogTag();
                        if(mTag != null && mTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG)){
                            mActivityListener.clickExtractBtn(CommonIdentity.EXTRACT_RENAME_RUNNING_MODE,CommonDialogFragment.getFolderName());
                        } else if(mTag != null && mTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG)){
                            mActivityListener.clickCompressBtn(CommonIdentity.COMPRESS_RENAME_RUNNING_MODE,CommonDialogFragment.getFolderName());
                        } else {
                            mActivityListener.clickProgressBtn(i);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void updateSearch(String mSearchContent) {
        if (mSearchView != null) {
            mSearchView.setQuery(mSearchContent, false);
            if (CommonUtils.isCategoryMode()) {
                requestSearch(mSearchContent, true);
            } else {
                requestSearch(mSearchContent);
            }
        }
    }
}
