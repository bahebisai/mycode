/* Copyright (C) 2016 Tcl Corporation Limited */
package com.jrdcom.filemanager.activity;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.IActivityListener;
import com.jrdcom.filemanager.ISafeCategoryListener;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.dialog.CommonDialogFragment;
import com.jrdcom.filemanager.dialog.ProgressPopupWindow;
import com.jrdcom.filemanager.fragment.CategoryFragment;
import com.jrdcom.filemanager.fragment.FileBrowserFragment;
import com.jrdcom.filemanager.fragment.FileBrowserFragment.AbsListViewFragmentListener;
import com.jrdcom.filemanager.fragment.ListsFragment;
import com.jrdcom.filemanager.fragment.PrivateCategoryFragment;

import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.IconManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.singleton.ResultTaskHandler;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.view.CustomPopupWindowBasedAnchor;

import java.io.File;


/**
 * Created by user on 16-3-3.
 */
public class FilePrivateModeActivity extends FileBaseActivity implements PrivateCategoryFragment.PrivateFragmentListener, AbsListViewFragmentListener, View.OnClickListener , DialogInterface.OnClickListener {

    private PowerManager.WakeLock wakeLock;
    private String currentSafePath;
    public PrivateCategoryFragment mPrivateCategoryFragment = null;
    FragmentTransaction fragmentTransaction;
    FragmentManager mFragmentManager;
    protected ListsFragment mListFragment;
    private ISafeCategoryListener mActivitytoCategoryListener;
    private Toolbar mMainToolbar = null;
    protected RelativeLayout Mainframe;
    private ImageView mEditImg;
    private ImageView mSortImg;
    public ImageView mMoreImg;
    public ImageView mOKImg;

    public static TextView mActibarText;
    public static Fragment mCurrentFragment;
    private String mSDCardRootPath;
    private String mOtgRootPath;
    private String mSafeRootPath;
    private String mSafeTagMode = CommonIdentity.CATEGORY_TAG;
    private TextView mPrivateCountText;
    private static boolean isEnterSaveStateMethod = false;


    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            if (fragment instanceof IActivityListener) {
                mActivityListener = (IActivityListener) fragment;
            }
            // set value for ISafeCategoryListener with attached fragment.
            if (fragment instanceof ISafeCategoryListener) {
                mActivitytoCategoryListener = (ISafeCategoryListener) fragment;
            }
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
            if (mApplication != null && mActivityListener != null) {
                mApplication.mResultTaskHandler = ResultTaskHandler.getInstance(mActivityListener);
                mApplication.setAppHandler(mApplication.mResultTaskHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mApplication != null) {
            mApplication.mCurrentLocation = CommonIdentity.FILE_PRIVATE_LOCATION;
        }
    }

    @Override
    public void initActionBar() {
        mMainToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        mlayout = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Mainframe = (RelativeLayout) findViewById(R.id.content_frame);
        View customActionBarView = mlayout.inflate(R.layout.private_actionbar, null);
        setSupportActionBar(mMainToolbar);
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        android.support.v7.app.ActionBar.LayoutParams layoutParams = new android.support.v7.app.ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mMainToolbar.addView(customActionBarView, layoutParams);
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_back); // MODIFIED by caiminjie, 2017-09-20,BUG-5346088
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMoreImg = (ImageView) customActionBarView.findViewById(R.id.private_more_btn);
        mEditImg = (ImageView) customActionBarView.findViewById(R.id.private_edit_back);
        mSortImg = (ImageView) customActionBarView.findViewById(R.id.private_sort_btn);
        mActibarText = (TextView) customActionBarView.findViewById(R.id.private_path_text);
        mOKImg = (ImageView) customActionBarView.findViewById(R.id.private_ok_btn);
        mEditImg.setVisibility(View.GONE);
        mSortImg.setVisibility(View.GONE);
        mOKImg.setVisibility(View.GONE);
        mEditImg.setOnClickListener(this);
        mSortImg.setOnClickListener(this);
        mMoreImg.setOnClickListener(this);
        mOKImg.setOnClickListener(this);
        mActibarText.setText(getSafeTitle());
    }

    @Override
    public void setMainContentView() {
        setContentView(R.layout.private_main);
        snackbarLayout = (LinearLayout) findViewById(R.id.snackbarlayout);
        snackTextView = (TextView) findViewById(R.id.snackbarlayout_text);
        mainLayout = (RelativeLayout) findViewById(R.id.content_frame);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mListFragment = new ListsFragment();
        mCategoryFragment = new CategoryFragment();
        //get AppbarLayout params for setScrollFlags change Scroll status
        mParams = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
        initActionBar();
        initFloatMenu();
        Intent intent = getIntent();
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        if (mApplication != null && mActivityListener != null && mApplication.mResultTaskHandler == null) {
            mApplication.mResultTaskHandler = ResultTaskHandler.getInstance(mActivityListener);
            mApplication.setAppHandler(mApplication.mResultTaskHandler);
        }
        if (intent != null) {
            currentSafePath = intent.getStringExtra("currentsafepath");
        }
        if (SharedPreferenceUtils.getPrefsViewBy(this) == null || SharedPreferenceUtils.getPrefsViewBy(this).equals("")) {
            SharedPreferenceUtils.changePrefViewBy(this, CommonIdentity.LIST_MODE);
        } else {
            mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(this);
        }
        mSafeTagMode = CommonIdentity.CATEGORY_TAG;
        mPrivateCountText = (TextView) findViewById(R.id.private_count_text);
        SharedPreferenceUtils.changeSafePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
        if (SharedPreferenceUtils.getCurrentSafeRoot(this) != null) {
            mSafeRootPath = new File(SharedPreferenceUtils.getCurrentSafeRoot(this)).getParent();
        }
        if (mMountPointManager != null) {
            if (mMountPointManager.isSDCardMounted()) {
                mSDCardRootPath = mMountPointManager.getSDCardPath();
            }
            if (mMountPointManager.isOtgMounted()) {
                mOtgRootPath = mMountPointManager.getUsbOtgPath();
            }
        }
        if (mListFragment == null) {
            mListFragment = new ListsFragment();
        }
        if (mPrivateCategoryFragment == null) {
            mPrivateCategoryFragment = new PrivateCategoryFragment();
            mPrivateCategoryFragment.setMode(mSafeTagMode);
        }
        checkPermission();
        if (mSaveStatus == CommonIdentity.FILE_STATUS_NORMAL) {
            if (isEnterSaveStateMethod && isshowFileTypeInterface && SafeManager.getPrivateFileCount(this) > 0) {
                isEnterSaveStateMethod = false;
                changeFileTypeFragment();
            } else {
                switchFragment();
            }
        } else if (mSaveStatus == CommonIdentity.FILE_STATUS_EDIT && mSaveOperation == CommonIdentity.FILE_SAFE_VIEW_MODE) {
            setFileActionMode(CommonIdentity.FILE_STATUS_EDIT);
            mSaveStatus = CommonIdentity.FILE_STATUS_NORMAL;
        } else if (mSaveStatus == CommonIdentity.FILE_STATUS_EDIT && mSaveOperation == CommonIdentity.FILE_MOVE_IN_MODE) {
            setFileActionMode(CommonIdentity.FILE_STATUS_EDIT);
            mSaveStatus = CommonIdentity.FILE_STATUS_NORMAL;
            SafeManager.mSafeCurrentOperration = mSaveOperation;
            mSaveOperation = CommonIdentity.FILE_SAFE_VIEW_MODE;
        }
        isshowFileTypeInterface = false;
        updateViewByTag();
        updateViewByTag();
    }

    public void changeFileTypeFragment() {
        SafeManager.isPrivateFileTypeInterface = true;
        mMoreImg.setVisibility(View.GONE);
        mSortImg.setVisibility(View.GONE);
        mFragmentManager = getFragmentManager();
        if (mFragmentManager != null) {
            fragmentTransaction = mFragmentManager.beginTransaction();
        }
        if (fragmentTransaction != null) {
            mCurrentFragment = mPrivateCategoryFragment;
//            fragmentTransaction.replace(R.id.layout_content, mPrivateCategoryFragment).commitAllowingStateLoss();
            CommonUtils.commitFragment(fragmentTransaction, mPrivateCategoryFragment);
            mActibarText.setText(R.string.safe_activity_title);
        }
    }

    @Override
    public void checkPermission() {
        final String className = this.getClass().getName();
        if (PermissionUtil.isAllowPermission(this) && PermissionUtil.isUerGrant(this)) {
            PermissionUtil.popPermissionDialog(className, this);
        } else {
            if (permissionMap.get(className) != null) {
                String[] permissions = permissionMap.get(className);
                PermissionUtil.checkAndRequestPermissions(this, permissions,
                        PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isRefreshFilesCategory = false;
        if (!PermissionUtil.isAllowPermission(this) && requestCode == PermissionUtil.JUMPTOSETTINGFORSTORAGE) {
            switchFragment();
        } else {
            checkPermission();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.private_more_btn:
                int moreItemPopX = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_xoff);
                int moreItemPopY = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_yoff);
                int morePopWidth = getResources().getDimensionPixelSize(R.dimen.sort_menu_width);
                morePop = new CustomPopupWindowBasedAnchor(
                        initMorePopWindow(), morePopWidth, LayoutParams.WRAP_CONTENT, FilePrivateModeActivity.this);
                morePop.showAtLocationBasedAnchor(mMoreImg, moreItemPopX, moreItemPopY);
                break;
            case R.id.selectall_item_normal:
            case R.id.unselectall_item_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.clickSelectAllBtn();
                }
                break;
            case R.id.add_file_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mMoreImg != null) {
                    mMoreImg.setVisibility(View.GONE);
                }
                clearAdapter();
                if (mActivitytoCategoryListener != null && mCurrentFragment == mPrivateCategoryFragment) {
                    mActivitytoCategoryListener.clickAddFileBtn(false);
                } else {
                    changeFileTypeFragment();
                }
                break;
            case R.id.private_ok_btn:
                if (mActivityListener != null) {
                    mActivityListener.clickAddPrivateMode();
                }
                break;

            case R.id.private_sort_btn:
                showChoiceResourceDialog(CommonIdentity.SORT_MODE, mMoreImg);
                break;
            case R.id.remove_item_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.clickRemovePrivateMode();
                }
                break;
            case R.id.select_item_normal:
                if (morePop != null) {
                    morePop.dismiss();
                }
                if (mActivityListener != null) {
                    mActivityListener.clickEditBtn();
                }
                break;
            case R.id.private_edit_back:
                mEditImg.setVisibility(View.GONE);
                setActionBarDisplayHomeAsUpEnabled(true);
                if (mCurrentFragment == mListFragment && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
                     switchFileTypeInterface();
                     return;
                }
                SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
                setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                updateBarView();
                if (mActivityListener != null) {
                    mActivityListener.clearChecked();
                    if (SafeManager.getPrivateFileCount(this) <= 0) {
                        switchFragment();
                    } else {
                        switchSafeCategoryList();
                    }
                }
                break;
        }
        super.onClick(v);
    }

    @Override
    public void updateCategoryNormalBarView() {
        updateCategoryNormalBar();
    }

    protected void updateCategoryNormalBar() {
        // TODO
    }

    public void switchContentByViewMode(boolean isCategory) {
        if (mListFragment == null) {
            mListFragment = new ListsFragment();
        }
        switchContent(mListFragment);
    }


    public void switchContent(Fragment to) {
        if (mCurrentFragment != mPrivateCategoryFragment) {
            CategoryManager.getInstance(this).clearMap();
        }
        mFragmentManager = getFragmentManager();
        if (mCurrentFragment != to) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_content, to);
            fragmentTransaction.commitAllowingStateLoss();
            mCurrentFragment = to;
            if (to == mListFragment) {
                switchViewContent(mListFragment);
            } else if (to == mPrivateCategoryFragment) {
                switchCategoryViewContent(mPrivateCategoryFragment);
            }
        } else if (to == mPrivateCategoryFragment) {
            switchCategoryViewContent(mPrivateCategoryFragment);
        } else if (to == mListFragment) {
            switchViewContent(mListFragment);
        }
    }

    private void switchViewContent(FileBrowserFragment mViewFragment) {
        mActivityListener = (IActivityListener) mViewFragment;
        if (mActivityListener != null) {
            mApplication.mResultTaskHandler = ResultTaskHandler.getInstance(mActivityListener);
            mApplication.setAppHandler(mApplication.mResultTaskHandler);
        }
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                updateViewByTag();
                updateFragmentView(getFileMode());
            }

        });
        mCurrentFragment = mViewFragment;
    }

    private void switchCategoryViewContent(Fragment mViewFragment) {
        SafeManager.mCurrentSafeCategory = -1;
        mActivitytoCategoryListener = (ISafeCategoryListener) mViewFragment;
        if (mActivityListener != null) {
            mActivityListener.clearAdapter();
        }
        mApplication.mFileInfoManager.getCategoryFileList().clear();
        updateViewByTag();
        updateCategoryNormalBar();
        if (mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.refreshSafeCategory();
        }
        mCurrentFragment = mViewFragment;
    }

    public void updateViewByTag() {
        if (mCurrentFragment == mPrivateCategoryFragment) {
            if(mActivitytoCategoryListener != null && mActivitytoCategoryListener.isShowFileTypeInterface()){
                mMoreImg.setVisibility(View.GONE);
            } else {
                mMoreImg.setVisibility(View.VISIBLE);
            }

            mSortImg.setVisibility(View.GONE);
            mOKImg.setVisibility(View.GONE);
        } else if (mCurrentFragment == mListFragment) {
            switch (getFileActionMode()) {
                case CommonIdentity.FILE_STATUS_NORMAL:
                    mMoreImg.setVisibility(View.VISIBLE);
                    mSortImg.setVisibility(View.VISIBLE);
                    mEditImg.setVisibility(View.GONE);
                    mOKImg.setVisibility(View.GONE);
                    break;
                case CommonIdentity.FILE_STATUS_EDIT:
                    if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
                        mMoreImg.setVisibility(View.GONE);
                        mSortImg.setVisibility(View.GONE);
                        mOKImg.setVisibility(View.VISIBLE);
                        mEditImg.setVisibility(View.VISIBLE);
                    } else {
                        mOKImg.setVisibility(View.GONE);
                        mMoreImg.setVisibility(View.VISIBLE);
                        mSortImg.setVisibility(View.GONE);
                        mEditImg.setVisibility(View.VISIBLE);
                    }

                    break;
            }
        }
    }

    private void updateFragmentView(int mode) {
        invalidateOptionsMenu();
        switch (mode) {
            case CommonIdentity.FILE_STATUS_NORMAL:
                refreshPathAdapter("");
                selectCount = 0;
                break;
            case CommonIdentity.FILE_STATUS_EDIT:
                refreshPathAdapter("");
                break;
            default:
                break;
        }
    }

    // update actionbar UI
    private void updateView(int mode) {
        switch (mode) {
            case CommonIdentity.FILE_STATUS_NORMAL:
                setActionBarDisplayHomeAsUpEnabled(true);
                if (mEditImg != null) {
                    mEditImg.setVisibility(View.GONE);
                }
                if (mMoreImg != null) {
                    if(mActivitytoCategoryListener != null && mActivitytoCategoryListener.isShowFileTypeInterface()){
                        mMoreImg.setVisibility(View.GONE);
                    } else {
                        mMoreImg.setVisibility(View.VISIBLE);
                    }
                }
                if (mCurrentFragment == mPrivateCategoryFragment && mSortImg != null) {
                    mSortImg.setVisibility(View.GONE);
                }
                setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.main_bar_private_color));
                selectCount = 0;
                break;
            case CommonIdentity.FILE_STATUS_EDIT:
                if (mNormalBar != null) {
                    mNormalBar.setVisibility(View.VISIBLE);
                }
                setActionBarDisplayHomeAsUpEnabled(false);
                if (mEditImg != null) {
                    mEditImg.setVisibility(View.VISIBLE);
                }
                mSortImg.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void refreshPathAdapter(String path) {
        int mode = CommonIdentity.REFRESH_SAFE_CATEGORY_MODE;
        if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE) {
            mActivityListener.refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_PRIVATE_CATEGORY_MODE,
                    CommonIdentity.FILE_PRIVATE_LOCATION, false,false);
        } else {
            mActivityListener.refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_CATEGORY_MODE,
                    CommonIdentity.FILE_PRIVATE_LOCATION, false,false);
        }

    }

    @Override
    public void updateEditBar(int count, boolean isHasDir, boolean isHasZip, boolean isHasDrm, boolean canShare,boolean hasAllPrivate,boolean isFLorSDDrm) {
        if (CommonUtils.isNormalStatus(mApplication) && mBtnMore != null) {
            mBtnMore.setEnabled(true);
            return;
        }
        setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.edit_bar_bg));
        setActionBarDisplayHomeAsUpEnabled(false);
        if (mBtnEditBack != null) {
            mBtnEditBack.setVisibility(View.VISIBLE);
        }
        selectCount = count;
        mIsHasDir = isHasDir;
        mIsHasDrm = isHasDrm;
        mCanShare = canShare;
        mHasAllPrivate = hasAllPrivate;
        mIsFLorSDDrm = isFLorSDDrm;
        updateEditBarWidgetState(count);
        invalidateOptionsMenu();
        updateBarTitle(CommonIdentity.UNKNOWN_TASK);
    }

    @Override
    public void reSearch() {
    }

    @Override
    public void setFileActionMode(int mode) {
        mApplication.mCurrentStatus = mode;
        SharedPreferenceUtils.changePrefsStatus(this, mode);
    }

    @Override
    public int getFileActionMode() {
        return getFileMode();
    }

    @Override
    public void updateBarView() {
        updateView(getFileMode());
        updateViewByTag();
    }

    public void updateActionbar() {
        updateBarTitle(CommonIdentity.UNKNOWN_TASK);
        invalidateOptionsMenu();
    }

    public void updateBarTitle(int mTaskType) {
        if (mApplication == null)
            return;
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE && mCurrentFragment != mPrivateCategoryFragment) {
            mPrivateCountText.setVisibility(View.VISIBLE);
            mPrivateCountText.setText(SafeManager.getPrivateFileCount(this) + "    " + getString(R.string.safe_activity_title));
        } else {
            mPrivateCountText.setVisibility(View.GONE);
        }
        if (SafeManager.mSafeCurrentmode == CommonIdentity.CATEGORY_MODE) {

            if (getFileActionMode() == CommonIdentity.FILE_STATUS_EDIT) {
                mActibarText.setText("" + selectCount);
            } else {
                mActibarText.setText(getSafeTitle());
            }
        } else {
            mActibarText.setText(getSafeTitle());
        }
    }

    @Override
    public void updateNormalBarView() {
        updateNormalBar();
    }

    @Override
    public void changeSearchMode(boolean flag) {
    }

    @Override
    public void toShowForbiddenDialog() {
    }

    @Override
    public void pasteBtnUpdated() {
    }

    @Override
    public void toReleaseWakeLock() {
        releaseWakeLock();
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
    public void HideActionbar(boolean flag) {
    }

    @Override
    public void isDeleteFlag(boolean flag) {
    }

    @Override
    public int getSlideLimite() {
        return 0;
    }

    @Override
    public void refreashSafeFilesCategory() {
        switchFragment();
    }

    @Override
    public void showBottomView(String message) {

    }

    @Override
    public void onBackPressed() {
        IconManager.getInstance().clearAll();
        try {
            if (mActivityListener != null) {
                mActivityListener.showNoSearchResults(false, null);
                mActivityListener.showNoFolderResultView(false);
            }
            if ((mCurrentFragment == mPrivateCategoryFragment && !mActivitytoCategoryListener.isShowFileTypeInterface())) {
                isEnterSaveStateMethod = false;
                SafeManager.isPrivateFileTypeInterface = false;
                mSaveCategory = -1;
                mSaveSafeCategory = -1;
                mSaveOperation = -1;
                mSaveStatus = CommonIdentity.FILE_STATUS_NORMAL;
                setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                mSaveCheckedList.clear();
                mSaveSelectedList.clear();
                finish();
                return;
            }
            if (mCurrentFragment == mPrivateCategoryFragment && mActivitytoCategoryListener.isShowFileTypeInterface()) {
                if(SafeManager.getPrivateFileCount(this) <= 0) {
                    mActivitytoCategoryListener.clickAddFileBtn(true);
                    if(mMoreImg != null && !mActivitytoCategoryListener.isShowFileTypeInterface()){
                        mMoreImg.setVisibility(View.VISIBLE);
                    }
                } else {
                    switchFragment();
                }
                return;
            }
            if (mCurrentFragment == mListFragment && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
                switchFileTypeInterface();
                return;
            }

            if (mCurrentFragment == mListFragment) {
                if (SafeManager.mCurrentSafeCategory == CommonIdentity.SAFE_CATEGORY_PRIVATE && getFileActionMode() == CommonIdentity.FILE_STATUS_NORMAL) {
                    finish();
                }
                if (SafeManager.mSafeCurrentmode == CommonIdentity.CATEGORY_MODE
                        && getFileActionMode() == CommonIdentity.FILE_STATUS_NORMAL || getFileActionMode() == CommonIdentity.FILE_STATUS_EDIT) {
                    SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
                    if (SafeManager.getPrivateFileCount(this) > 0) {
                        SafeManager.mCurrentmode = CommonIdentity.FILE_STATUS_NORMAL;
                        setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mActivityListener.clearChecked();
                                switchSafeCategoryList();
                                updateViewByTag();
                                updateView(getFileActionMode());
                            }
                        });
                        return;
                    } else {
                        ShowCategoryContent();
                        SharedPreferenceUtils.changeSafePrefCurrTag(mApplication, CommonIdentity.CATEGORY_TAG);
                        SafeManager.mCurrentmode = CommonIdentity.FILE_STATUS_NORMAL;
                        setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
                        updateView(getFileActionMode());
                    }
                } else {
                    if (mActivityListener != null) {
                        mActivityListener.onBackPressed();
                    }
                }
                updateBarTitle(CommonIdentity.UNKNOWN_TASK);
                updateViewByTag();
                return;
            }
            mActibarText.setText(getSafeTitle());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    private void switchSafeCategoryList() {
        if (CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_DOCS ||
                CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_MUSIC ||
                CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_VEDIOS ||
                CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_APKS ||
                CategoryManager.mCurrentCagegory == CommonIdentity.CATEGORY_ARCHIVES) {
            mActibarText.setText(R.string.safe_activity_title);
            SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_PRIVATE;
        }
        updateCategoryNormalBarView();
        switchContentByViewMode(false);
    }

    private void ShowCategoryContent() {
        switchContent(mPrivateCategoryFragment);
    }

    private void updateFragment(String tag, boolean... isRootClicked) {
        mSafeTagMode = tag;
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mApplication.mCurrentPath = null;
        if (CommonUtils.isCategoryTag(tag)) {

            SafeManager.setCurrentMode(CommonIdentity.CATEGORY_MODE);
            mApplication.mCurrentPath = null;
            ShowCategoryContent();
            return;
        }
        switchContentByViewMode(false);
    }


    private LayoutInflater mlayout;

    public boolean isRefreshFilesCategory = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public View initMorePopWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customMoreView = inflater.inflate(R.layout.private_more_menu, null);
        TextView mTexSelectAll = (TextView) customMoreView.findViewById(R.id.selectall_item_normal);
        TextView mTexSelect = (TextView) customMoreView.findViewById(R.id.select_item_normal);
        TextView mTexAddFile = (TextView) customMoreView.findViewById(R.id.add_file_normal);
        TextView mTexUnSelectAll = (TextView) customMoreView.findViewById(R.id.unselectall_item_normal);
        TextView mTexPublic = (TextView) customMoreView.findViewById(R.id.remove_item_normal);
        mTexSelectAll.setOnClickListener(this);
        mTexUnSelectAll.setOnClickListener(this);
        mTexPublic.setOnClickListener(this);
        mTexAddFile.setOnClickListener(this);
        mTexSelect.setOnClickListener(this);
        if (mCurrentFragment == mPrivateCategoryFragment) {
            mTexAddFile.setVisibility(View.VISIBLE);
            mTexSelectAll.setVisibility(View.GONE);
            mTexUnSelectAll.setVisibility(View.GONE);
            mTexPublic.setVisibility(View.GONE);
            mTexSelect.setVisibility(View.GONE);
        } else {
            if (selectCount != 0) {
                if (mActivityListener.checkIsSelectAll()) {
                    mTexSelectAll.setVisibility(View.GONE);
                    mTexUnSelectAll.setVisibility(View.VISIBLE);
                } else {
                    mTexSelectAll.setVisibility(View.VISIBLE);
                    mTexUnSelectAll.setVisibility(View.GONE);
                }
            }
            if (getFileActionMode() == CommonIdentity.FILE_STATUS_NORMAL) {
                mTexAddFile.setVisibility(View.VISIBLE);
                mTexPublic.setVisibility(View.GONE);
                mTexSelect.setVisibility(View.VISIBLE);
                mTexSelectAll.setVisibility(View.GONE);
            } else {
                if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
                    mTexPublic.setVisibility(View.GONE);
                    mTexAddFile.setVisibility(View.GONE);
                } else {
                    mTexAddFile.setVisibility(View.GONE);
                    mTexPublic.setVisibility(View.VISIBLE);
                }
                mTexSelect.setVisibility(View.GONE);
            }
            if (selectCount == 0) {
                if (getFileActionMode() == CommonIdentity.FILE_STATUS_NORMAL) {
                    mTexSelectAll.setVisibility(View.GONE);
                } else {
                    mTexSelectAll.setVisibility(View.VISIBLE);
                    mTexPublic.setVisibility(View.GONE);
                }
                mTexUnSelectAll.setVisibility(View.GONE);
            }

        }
        return customMoreView;
    }

    private void updateEditBarWidgetState(int selectedCount) {
        updateViewByTag();
    }

    private String getSafeTitle() {
        return getString(R.string.safe_activity_title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SafeManager.mCurrentmode = CommonIdentity.FILE_STATUS_NORMAL;
        setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
        SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
        mPrivateCountText.setVisibility(View.GONE);
        if (mApplication != null) {
            mApplication.mCurrentLocation = CommonIdentity.FILE_MANAGER_LOCATIONE;
        }
        SafeManager.notQuitSafe = false;
        CategoryManager.mCurrentCagegory = -1;
        SafeManager.mCurrentSafeCategory = -1;
        mCurrentFragment = null;
        isRefreshFilesCategory = false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
            for (String permission : permissions) {
                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        PermissionUtil.setSecondRequestPermission(this);
                        finish();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (enterPermissionResultCount == 2) {
                            enterPermissionResultCount = 1;
                            switchFragment();
                        } else {
                            enterPermissionResultCount++;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (SafeManager.notQuitSafe) {
//            SafeManager.notQuitSafe = false;
//        } else if (RunningTaskMap.getRunningTaskSize() > 0) {
//
//        } else {
//            SafeManager.notQuitSafe = false;
//            finish();
//        }
    }

    @Override
    public void onUnmounted(String mountPoint) {
        super.onUnmounted(mountPoint);
        if ((mSafeRootPath != null && mMountPointManager != null)
                && ((mSDCardRootPath != null && mSDCardRootPath.equals(mSafeRootPath) && !mMountPointManager.isSDCardMounted()) ||
                (mOtgRootPath != null && mOtgRootPath.equals(mSafeRootPath) && !mMountPointManager.isOtgMounted()))) {
            finish();
        }
    }

    public void switchSafeCateogryFragment() {
        mFragmentManager = getFragmentManager();
        if (mFragmentManager != null) {
            fragmentTransaction = mFragmentManager.beginTransaction();
        }
        if (fragmentTransaction != null) {
            mCurrentFragment = mPrivateCategoryFragment;
            fragmentTransaction.replace(R.id.layout_content, mPrivateCategoryFragment).commitAllowingStateLoss();
            mActibarText.setText(R.string.safe_activity_title);
            if (mPrivateCountText != null) {
                mPrivateCountText.setVisibility(View.GONE);
            }
            if (mSortImg != null) {
                mSortImg.setVisibility(View.GONE);
            }
        }
    }

    public void switchFileTypeInterface(){
        if(mActivityListener != null){
            mActivityListener.clearAdapter();
        }
        SafeManager.isPrivateFileTypeInterface = true;
        switchSafeCateogryFragment();
        SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
        setFileActionMode(CommonIdentity.FILE_STATUS_NORMAL);
        updateBarView();
        if (mMoreImg != null) {
            mMoreImg.setVisibility(View.GONE);
        }
        return;
    }

    public void switchFragment() {
        int mPrivateCount = SafeManager.getPrivateFileCount(this);
        if (mPrivateCount > 0) {
            if (!PermissionUtil.isAllowPermission(this)) {
                mActivityListener = (IActivityListener) mListFragment;
                mFragmentManager = getFragmentManager();
                fragmentTransaction = mFragmentManager.beginTransaction();
                if (fragmentTransaction != null) {
                    mCurrentFragment = mListFragment;
                    fragmentTransaction.replace(R.id.layout_content, mListFragment).commitAllowingStateLoss();
                }
                SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
                SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_PRIVATE;

                mPrivateCountText.setVisibility(View.VISIBLE);
                mPrivateCountText.setText(mPrivateCount + "    " + getString(R.string.safe_activity_title));

                switchSafeCategoryList();
            }
        } else {
           switchSafeCateogryFragment();
        }

    }

    @Override
    public void cancelScrollActionbar() {

    }

    @Override
    public void enableScrollActionbar() {

    }

    @Override
    public void updateSearch(String mSearchContent) {

    }

    public void clearAdapter() {
        if (mActivityListener != null) {
            mActivityListener.clearAdapter();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isEnterSaveStateMethod = true;
        mSaveCategory = CategoryManager.mCurrentCagegory;
        mSaveSafeCategory = SafeManager.mCurrentSafeCategory;
        mSaveOperation = SafeManager.mSafeCurrentOperration;
        if (mActivitytoCategoryListener != null) {
            isshowFileTypeInterface = mActivitytoCategoryListener.isShowFileTypeInterface();
        }
        mSaveQueryText = mQueryText;
        if (mCurrentFragment == mListFragment) {
            mSaveStatus = getFileActionMode();
        }
        if (mActivityListener != null) {
            mSaveCheckedList.clear();
            mSaveSelectedList.clear();
            if (mCurrentFragment == mListFragment) {
                mSaveCheckedList.addAll(mActivityListener.getCheckedList());
                mSaveSelectedList.addAll(mActivityListener.saveSelectedList());
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (getFileActionMode() == CommonIdentity.FILE_STATUS_EDIT && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE) {
            updateEditFragment();
        } else if (getFileActionMode() == CommonIdentity.FILE_STATUS_EDIT && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
            updateEditFragment();
        }
        updateBarView();
    }

    private void updateEditFragment() {
        changePrivateEditMode();
        switchContent(mListFragment);
        updatePrivateEditMode();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        CommonDialogFragment mDeleteDialog = CommonDialogFragment.getInstance();
        switch (i) {
            case ProgressDialog.BUTTON_NEGATIVE:
                if (mActivityListener != null) {
                    mApplication.cancelTaskTime = ProgressPopupWindow.getCreateTaskTime();
                    mActivityListener.clickProgressBtn(i);
                }
                break;
            case ProgressDialog.BUTTON_POSITIVE:
                if (mActivityListener != null) {
                    if (mDeleteDialog != null) {
                        String mTag = mDeleteDialog.getDailogTag();
                        if(!mTag.isEmpty() && mTag.equals(CommonIdentity.DELETE_DIALOG_TAG)) {
                            mActivityListener.deleteFileResponse();
                        } else if(!mTag.isEmpty() && mTag.equals(CommonIdentity.REMOVE_PRIVATE_DIALOG_TAG)){
                            mActivityListener.removePrivateMode();
                        }
                        CommonDialogFragment.mShowMessageDialogFragment = null;
                    } else {
                        mActivityListener.clickProgressBtn(i);
                    }
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (morePop != null) {
            reshowMorePop(mMoreImg);
        }
        if (mCurrentFragment == mListFragment && mApplication.mViewMode.equals(CommonIdentity.GRID_MODE)) {
            mActivityListener.changeViewMode(CommonIdentity.GRID_MODE);
        }
    }
}
