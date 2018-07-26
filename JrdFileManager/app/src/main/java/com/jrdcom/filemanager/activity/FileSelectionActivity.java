package com.jrdcom.filemanager.activity;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.adapter.FileInfoAdapter;
import com.jrdcom.filemanager.dialog.ProgressPopupWindow;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.MountManager;

import com.jrdcom.filemanager.task.ListFileTask;
import com.jrdcom.filemanager.task.ProgressInfo;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.SafeUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;


public class FileSelectionActivity extends FileBaseActivity implements OnItemClickListener {

    private static final String TAG = "FileSelectionActivity";
    private static final String mUnSupportedFormat = "bad mime type";
    private static String mCurPath;
    private Intent mIntent;
    private int mTop = -1;
    private FileInfo mSelectedFileInfo;
    private Button mCancel;
    private FileInfoAdapter mAdapter;
    private ListView mListView;
    private String mFileCategory;
    private TextView mPathText;
    private String mCurFilePath;
    public static final String EXTRA_DRM_LEVEL = "android.intent.extra.drm_level";
    private int mDrmLevel = ListFileTask.LEVEL_ALL;
    private Toolbar mMainToolbar = null;
    private AlertDialog mProgressDialog;
    private static final int SHOW_DIALOG = 11;
    private static final int HIDE_DIALOG = 12;

    @Override
    public void setMainContentView() {
        setContentView(R.layout.select_file_main);
        mMainToolbar = (Toolbar) this.findViewById(R.id.select_toolbar);
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
        mCancel = (Button) findViewById(R.id.select_cancel);
        mIntent = getIntent();
        mFileCategory = mIntent.getType();
        mDrmLevel = mIntent.getIntExtra(EXTRA_DRM_LEVEL, ListFileTask.LEVEL_ALL);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mCancel.setOnClickListener(this);
        mAdapter = new FileInfoAdapter(this, mApplication.mFileInfoManager, mListView);
        mListView.setAdapter(mAdapter);
        showDirectoryContent(mCurPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getItemId() == android.R.id.home && !PermissionUtil.isAllowPermission(this)) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void showDirectoryContent(String path) {
        mCurPath = path;
        onPathChanged();
        if (mMountPointManager.isRootPath(mCurPath)) {
            showRootPathContent();
            return;
        }
        if (mApplication.mFileInfoManager != null) {
            getBaseTaskInfo(mApplication, CommonIdentity.REFRESH_FILE_PATH_MODE, CommonIdentity.FILE_STATUS_SELECT,CommonIdentity.LIST_INFO_TASK, mFileCategory, path, mDrmLevel);
        }
    }

    public void getBaseTaskInfo(FileManagerApplication mApplication, int mRefreshMode, int mode,int mBaseType,
                                String mSrcPath, String mDesPath, int mDrmType) {
        long mCreateTime = System.currentTimeMillis();
        TaskInfo mTaskInfo = new TaskInfo(mApplication, new ListListener(), mBaseType);
        mTaskInfo.setRefreshMode(mRefreshMode);
        mTaskInfo.setCreateTaskTime(mCreateTime);
        mTaskInfo.setDestPath(mDesPath);
        mTaskInfo.setAdapterMode(mode);
        mTaskInfo.setSrcPath(mSrcPath);
        mTaskInfo.setDrmType(mDrmLevel);
        mApplication.mFileInfoManager.addNewTask(mTaskInfo);
    }

    private Handler mFileHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_DIALOG:
                    TaskInfo mResultInfo = CommonUtils.getListenerInfo(getString(R.string.loading),System.currentTimeMillis(),CommonIdentity.LIST_INFO_TASK,CommonIdentity.FILE_STATUS_SELECT);
                    mProgressDialog = ProgressPopupWindow.newInstance(FileSelectionActivity.this, mResultInfo);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    break;
                case HIDE_DIALOG:
                    mApplication.mFileInfoManager.loadFileInfoList(
                            mCurPath,
                            mApplication.mSortType);
                    mAdapter.refresh();
                    mAdapter.notifyDataSetChanged();
                    int seletedItemPosition = restoreSelectedPosition();
                    restoreListPosition(seletedItemPosition);
                    mProgressDialog.hide();
                    break;
                default:break;
            }
        }
    };

    private class ListListener implements OperationEventListener {

        @Override
        public void onTaskResult(TaskInfo info) {
            Message message = mFileHandler.obtainMessage();
            message.what = HIDE_DIALOG;
            mFileHandler.sendMessage(message);
        }

        @Override
        public void onTaskPrepare() {
            Message message = mFileHandler.obtainMessage();
            message.what = SHOW_DIALOG;
            mFileHandler.sendMessage(message);
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {

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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        mMountPointManager.init(FileSelectionActivity.this);
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

    private void onPathChanged() {
        refreshPath(mCurPath);
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
        String mMimeTypeFromIntent = mIntent.getType();
        FileInfo selecteItemFileInfo = (FileInfo) mAdapter.getItem(position);
        mSelectedFileInfo = selecteItemFileInfo;
        if (selecteItemFileInfo.isDirectory()) {
            int top = view.getTop();
            mTop = top;
            mSelectedFileInfo = (FileInfo) mAdapter.getItem(position);
            showDirectoryContent(selecteItemFileInfo.getFileAbsolutePath());
        } else {
            String mFileType = selecteItemFileInfo.getMime();
            if (mIntent.getAction().equals(Intent.ACTION_PICK)
                    && MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.equals(mIntent.getData())) {
                if (!mFileType.contains(CommonIdentity.MIME_HEAD_AUDIO)&&!mFileType.endsWith("/ogg")) {
                    mToastHelper.showToast(R.string.msg_unable_open_file);
                    return;
                } else {
                    Intent intent = new Intent();
                    Uri uri = null;
                    if(mApplication.isShareMediaURI){
                       uri = FileUtils.getMediaContentUri(selecteItemFileInfo.getFile(), mApplication.mFileInfoManager, mFileType);
                    } else {
                        uri = FileUtils.getContentUri(selecteItemFileInfo.getFile(), mApplication.mFileInfoManager, mFileType, selecteItemFileInfo.isDrm());
                    }
                    intent.setData(uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            if (!compareMimeType(mMimeTypeFromIntent, mFileType)) {
                mToastHelper.showToast(R.string.msg_unable_open_file);
                return;
            } else {
                Intent intent = new Intent();
                Uri uri = null;
                if(mApplication.isShareMediaURI){
                    uri = FileUtils.getMediaContentUri(selecteItemFileInfo.getFile(), mApplication.mFileInfoManager, mFileType);
                } else {
                    uri = FileUtils.getContentUri(selecteItemFileInfo.getFile(), mApplication.mFileInfoManager, mFileType, selecteItemFileInfo.isDrm());
                }
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private boolean compareMimeType(String mimetypefromintent, String mimetypefromfile) {
        String startWithIntent = null;
        String startWithFileMimetype = null;
        if (mimetypefromintent == null || mimetypefromintent.startsWith("*")) {
            return true;
        }
        if (mimetypefromintent.equals("application/x-ogg")) {
            mimetypefromintent = CommonIdentity.MIME_HEAD_AUDIO;
        }
        if (mimetypefromfile.equals(CommonIdentity.MIMETYPE_EXTENSION_UNKONW)
                || mimetypefromfile.equals(CommonIdentity.MIMETYPE_EXTENSION_NULL) || mimetypefromfile.equals(mUnSupportedFormat)) {
            return false;
        }
        if (mimetypefromfile.equals(CommonIdentity.MIMETYPE_3GPP_UNKONW)) {
            mimetypefromfile = CommonIdentity.MIME_HEAD_VIDEO;
        }
        try {
            startWithIntent = mimetypefromintent.substring(0, mimetypefromintent.lastIndexOf("/"));
            startWithFileMimetype = mimetypefromfile.substring(0, mimetypefromfile.lastIndexOf("/"));
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (startWithIntent.equalsIgnoreCase(startWithFileMimetype)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.select_cancel:
                finish();
                break;
        }
    }

    private void refreshPath(String initFileInfo) {
        mCurFilePath = initFileInfo;
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
        if(mApplication.mProgressDialog != null){
            mApplication.mProgressDialog.dismiss();
        }
    }
}

