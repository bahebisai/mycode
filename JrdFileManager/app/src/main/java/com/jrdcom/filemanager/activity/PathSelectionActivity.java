package com.jrdcom.filemanager.activity;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.adapter.FileInfoAdapter;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.task.ProgressInfo;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.LogUtils;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathSelectionActivity extends FileBaseActivity implements OnItemClickListener {

    private final static String TAG = "PathSelectionActivity";
    private static String mCurPath;
    private Intent mIntent;
    private int mTop = -1;
    private FileInfo mSelectedFileInfo;
    private Button mCancel;
    private Button mOk;
    private FileInfoAdapter mAdapter;
    private ListView mListView;
    private static final String RESULT_DIR_SEL = "result_dir_sel";
    private static final int ANDROID_PICK = 1;
    private static final int QRD_PICK = 2;
    private int mCurrentPick = -1;
    private TextView mPathText;
    private String mCurFilePath;
    private Toolbar mMainToolbar = null;

    @Override
    public void setMainContentView() {
        setContentView(R.layout.select_path_main);
        mMainToolbar = (Toolbar) this.findViewById(R.id.path_toolbar);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.file_select_actionbar, null);
        if (customActionBarView != null) {
            mMainToolbar.addView(customActionBarView);
            setSupportActionBar(mMainToolbar);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_back); // MODIFIED by caiminjie, 2017-09-20,BUG-5346088
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            android.support.v7.app.ActionBar.LayoutParams layoutParams = new android.support.v7.app.ActionBar.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mPathText = (TextView) customActionBarView.findViewById(R.id.path_text);
        }
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
        checkPermission();
//        if (mFileInfoManager == null) {
//            mFileInfoManager = mApplication.initFileInfoManager(this);
//        }
        mCurPath = mMountPointManager.getRootPath();
        mIntent = getIntent();
        if (mIntent.getAction().equals(Intent.ACTION_PICK)) {
            mCurrentPick = ANDROID_PICK;
        } else if (mIntent.getAction().equals("com.android.fileexplorer.action.DIR_SEL")) {
            mCurrentPick = QRD_PICK;
        }

        mOk = (Button) findViewById(R.id.btn_ok);
        mOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (mCurrentPick == ANDROID_PICK) {
                    intent.putExtra(RESULT_DIR_SEL, mCurPath);
                } else if (mCurrentPick == QRD_PICK) {
                    intent.setData(Uri.fromFile(new File(mCurPath)));
                }
                LogUtils.i(
                        TAG, "mCurPath = " + mCurPath
                                + " ** mCurrentPick = " + mCurrentPick + " ** intent = "
                                + intent.getDataString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mCancel = (Button) findViewById(R.id.btn_cancel);
        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mAdapter = new FileInfoAdapter(mApplication, mApplication.mFileInfoManager, mListView);
        mListView.setAdapter(mAdapter);
        showDirectoryContent(mCurPath);
    }

    private void showDirectoryContent(String path) {
        mCurPath = path;
        onPathChanged();
        if (mMountPointManager.isRootPath(mCurPath)) {
            showRootPathContent();
            return;
        }
        if (mApplication.mFileInfoManager != null) {
            getBaseTaskInfo(mApplication, CommonIdentity.REFRESH_FILE_PATH_MODE, CommonIdentity.FILE_STATUS_FOLDER_SELECT,CommonIdentity.LIST_INFO_TASK, null, path, -1);
        }
    }

    public void getBaseTaskInfo(FileManagerApplication mApplication, int mRefreshMode, int mode,int mBaseType,
                                String mSrcPath, String mDesPath, int mDrmType) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, new ListListener(), mBaseType);
        mTaskInfo.setRefreshMode(mRefreshMode);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setDestPath(mDesPath);
        mTaskInfo.setSrcPath(mSrcPath);
        mTaskInfo.setDrmType(mDrmType);
        mTaskInfo.setAdapterMode(mode);
        mTaskInfo.setShowDir(true);
        mApplication.mFileInfoManager.addNewTask(mTaskInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private Handler mFileHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            mApplication.mFileInfoManager.loadFileInfoList(
                    mCurPath,
                    mApplication.mSortType);
            mAdapter.refresh();
            mAdapter.notifyDataSetChanged();
            int seletedItemPosition = restoreSelectedPosition();
            restoreListPosition(seletedItemPosition);

        }
    };

    private class ListListener implements OperationEventListener {

        @Override
        public void onTaskResult(TaskInfo mTaskInfo) {
            mFileHandler.sendEmptyMessage(12);

        }

        @Override
        public void onTaskPrepare() {
            return;
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            return;
        }
    }

    private int restoreSelectedPosition() {
        if (mSelectedFileInfo == null) {
            return -1;
        } else {
            int curSelectedItemPosition = mAdapter
                    .getPosition(mSelectedFileInfo);
            return curSelectedItemPosition;
        }
    }

    private void restoreListPosition(final int seletedItemPosition) {
        mListView.setAdapter(mAdapter);
        if (seletedItemPosition == -1) {
            mListView.setSelectionAfterHeaderView();
        } else if (seletedItemPosition >= 0
                && seletedItemPosition < mAdapter.getCount()) {
            if (mTop == -1) {
                mListView.setSelection(seletedItemPosition);
            } else {
                mListView.setSelectionFromTop(seletedItemPosition, mTop);
            }
        }
    }

    private void showRootPathContent() {
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        List<FileInfo> mountFileList = mMountPointManager.getMountPointFileInfo();
        if (mountFileList != null) {
            fileInfoList.addAll(mountFileList);
        }
        mApplication.mFileInfoManager.addItemList(fileInfoList);
        mApplication.mFileInfoManager.loadFileInfoList(mCurPath,
                mApplication.mSortType);
        mAdapter.refresh();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        mMountPointManager.init(PathSelectionActivity.this);
        onPathChanged();
        if (mMountPointManager.isRootPath(mCurPath)) {
            List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
            List<FileInfo> mountFileList = mMountPointManager.getMountPointFileInfo();
            if (mountFileList != null) {
                fileInfoList.addAll(mountFileList);
            }
            mApplication.mFileInfoManager.addItemList(fileInfoList);
            mApplication.mFileInfoManager.loadFileInfoList(mCurPath,
                    mApplication.mSortType);
            mAdapter.refresh();
        }
    }

    private void refreshButton() {
        if (mMountPointManager.isRootPath(mCurPath)) {
            mOk.setEnabled(false);
        } else {
            mOk.setEnabled(true);
        }
    }

    private void onPathChanged() {
        refreshButton();
        refreshPath(mCurPath);
    }

    private void refreshPath(String initFileInfo) {
        mCurFilePath = initFileInfo;
        LogUtils.v("wye", "mCurFilePath=" + mCurFilePath);
        if (mCurFilePath != null) {
            if (!mMountPointManager.isRootPath(mCurFilePath)) {
                String path = mMountPointManager
                        .getDescriptionPath(mCurFilePath);
                if (path != null && !path.isEmpty()) {
                    String result = null;
                    if (path.contains(MountManager.SEPARATOR)) {
                        result = path.substring(path.lastIndexOf(MountManager.SEPARATOR) + 1);
                        mPathText.setText(result);
                    } else {
                        mPathText.setText(path);
                    }
                }
            } else {
                mPathText.setText(R.string.app_name_new); // MODIFIED by Chuanzhi.Shao, 2017-10-11,BUG-5395138
            }
        }
    }

    private void backToRootPath() {
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }

        mCurPath = mMountPointManager.getRootPath();
    }

    @Override
    public void onBackPressed() {
        if (mMountPointManager.isRootPath(mCurPath)) {
            finish();
        } else if (mCurPath != null
                && mMountPointManager.isSdOrPhonePath(mCurPath)) {
            backToRootPath();
        } else {
            mCurPath = new File(mCurPath).getParent();
        }
        showDirectoryContent(mCurPath);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileInfo selecteItemFileInfo = (FileInfo) mAdapter.getItem(position);
        mSelectedFileInfo = selecteItemFileInfo;
        if (selecteItemFileInfo.isDirectory()) {
            int top = view.getTop();
            mTop = top;
            mSelectedFileInfo = (FileInfo) mAdapter.getItem(position);
            showDirectoryContent(selecteItemFileInfo.getFileAbsolutePath());
        }
    }

    @Override
    public void onClick(View view) {
        if (mApplication.mFileInfoManager.isBusy(this.getClass().getName())) {
            return;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT == requestCode) {
            for (String permission : permissions) {
                if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (CommonUtils.hasM()) {
                        PermissionUtil.setSecondRequestPermission(this);
                        finish();
                    }
                } else {
                    if (CommonUtils.hasM()) {
                    }
                }
            }
        }
    }

    @Override
    public void onUnmounted(String mountPoint) {
        super.onUnmounted(mountPoint);
        if (mMountPointManager != null) {
            mCurPath = mMountPointManager.getRootPath();
        }
        if (mCurPath != null) {
            showDirectoryContent(mCurPath);
        }
    }

    @Override
    public void onMounted() {
        super.onMounted();
        refreshRootAdapter();
    }

    protected void refreshRootAdapter() {
        if (mCurPath != null && mMountPointManager != null && mCurPath.equals(mMountPointManager.getRootPath())) {
            showDirectoryContent(mCurPath);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        refreshRootAdapter();
        checkPermission();
    }

    @Override
    public void checkCreatePermission() {
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
    protected void onDestroy() {
        super.onDestroy();
        PermissionUtil.closePermissionDialogShowning();
    }
}
