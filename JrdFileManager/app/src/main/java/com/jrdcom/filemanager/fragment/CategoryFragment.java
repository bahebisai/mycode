
package com.jrdcom.filemanager.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.IActivitytoCategoryListener;
import com.jrdcom.filemanager.adapter.CategoryAdapter;
import com.jrdcom.filemanager.listener.CategoryFragmentListener;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.CategoryManager.CountTextCallback;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.task.ProgressInfo;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.util.ArrayList;
import java.util.List;


@TargetApi(Build.VERSION_CODES.M)
public class CategoryFragment extends FileBrowserFragment implements CategoryAdapter.OnItemClickLitener, AdapterView.OnItemClickListener,
        IActivitytoCategoryListener {

    private static final String TAG = CategoryFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    // layout container of storage info
    private View mPhoneStoContainer;
    private View mSDStoContainer;
    private View mExternalStoContainer;

    // ProgressBar showing using info for storage
    private ProgressBar mPhoneStoProgressBar;
    private ProgressBar mSdStoProgressBar;
    private ProgressBar mExternalStoProgressBar;

    // text content showing using info for storage
    private TextView mSDStoUsedInfo;
    private TextView mPhoneStoUsedInfo;
    private TextView mExternalStoUsedInfo;

    // storage name
    private TextView mPhoneName;
    private TextView mSDName;
    private TextView mExternalName;

    // container for portrait or landscape layout
    private View portraitCategoryLayoutContainer;
    private LinearLayout LandCategoryLayoutContainer;

    // use to adapter progress bar only phone storage display in land
    private View progressBarAdapterView;

    private ArrayList<CategoryItem> mList;
    private CategoryAdapter mAdapter;
    private Resources mResources;
    private Context mContext;
    protected MountManager mMountManager;
    private Activity mActivity;
    private CategoryFragmentListener mCategoryFragmentListener;

    private FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    private boolean isRunCreateActivity = false;

    // space used percent
    private final double SPACE_MEDIUM_USED = 0.35;
    private final double SPACE_HEAVY_USED = 0.75;
    private View rootView;

    @Override
    public void onItemClick(View view, int position) {
        onClickItem(position);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        onClickItem(position);
    }

    private void onClickItem(int position) {
        switch (position) {
            case CommonIdentity.CATEGORY_RECENT:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_RECENT;
                break;
            case CommonIdentity.CATEGORY_APKS:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_APKS;
                break;
            case CommonIdentity.CATEGORY_BLUETOOTH:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_BLUETOOTH;
                break;
            case CommonIdentity.CATEGORY_DOCS:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_DOCS;
                break;
            case CommonIdentity.CATEGORY_DOWNLOAD:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_DOWNLOAD;
                break;
            case CommonIdentity.CATEGORY_MUSIC:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_MUSIC;
                break;
            // hide photos, if value of CATEGORY_* changed, please handle here
            case CommonIdentity.CATEGORY_PICTURES:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_PICTURES;
                break;
            case CommonIdentity.CATEGORY_VEDIOS:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_VEDIOS;
                break;
            case CommonIdentity.CATEGORY_ARCHIVES:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_ARCHIVES;
                break;
            case CommonIdentity.CATEGORY_SAFE:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_SAFE;
                break;
            default:
                return;
        }

        if (CommonUtils.isSupportPrivacyMode(mContext)
                && position == CommonIdentity.CATEGORY_SAFE) {
            Intent intent = new Intent();
            intent.setAction("com.jrdcom.filemanager.action.SAFEBOX");
            intent.putExtra("isFirstEnter",true);
            startActivity(intent);
            CommonUtils.recordCountNumForFA(mContext,CommonIdentity.FILEMANAGER_PRIVATE_MODE_NUMBER,mApplication);
        } else {
            mCategoryFragmentListener.updateCategoryNormalBarView();
            mCategoryFragmentListener.switchContentByViewMode(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(mContext==null) {
            mContext = getActivity();
            mActivity = getActivity();
            mResources = mContext.getResources();
            try {
                if (mActivity instanceof CategoryFragmentListener) {
                    mCategoryFragmentListener = (CategoryFragmentListener) mActivity;
                }
            } catch (Exception e) {
                throw new ClassCastException(mActivity.toString()
                        + "must implement CategoryFragmentListener");
            }
        }
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_category, container, false);
            initView(rootView);
            initData();
        }
        refreshView();
        return rootView;
    }

    private void initView(View view){
        mMountManager = MountManager.getInstance();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.category_recyclerview);
        portraitCategoryLayoutContainer = view.findViewById(R.id.category_storage_layout);
        LandCategoryLayoutContainer = (LinearLayout) view.findViewById(R.id.category_storage_land_layout);
    }

    public void initData(){
        mList = new ArrayList<CategoryItem>();
        mAdapter = new CategoryAdapter(getActivity());
        mList.ensureCapacity(12);
        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_recents, null),
                mResources.getString(R.string.main_recents),
                mResources
                        .getDrawable(R.drawable.main_rencent_corners_bg, null),
                R.color.main_grid_item_color, false));
        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_installers, null),
                mResources.getString(R.string.main_installers),
                mResources
                        .getDrawable(R.drawable.main_install_corners_bg, null),
                R.color.main_grid_item_color, false));
        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_bluetooth, null),
                mResources.getString(R.string.category_bluetooth),
                mResources
                        .getDrawable(R.drawable.main_bluetooth_corners_bg, null),
                R.color.main_grid_item_color, false));

        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_documents, null),
                mResources.getString(R.string.main_document),
                mResources
                        .getDrawable(R.drawable.main_document_corners_bg, null),
                R.color.main_grid_item_color, false));

        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_downloads, null),
                mResources.getString(R.string.category_download),
                mResources
                        .getDrawable(R.drawable.main_download_corners_bg, null),
                R.color.main_grid_item_color, false));

        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_music, null), mResources
                .getString(R.string.category_audio), mResources
                .getDrawable(R.drawable.main_music_corners_bg, null),
                R.color.main_grid_item_color, false));

        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_pictures, null), mResources
                .getString(R.string.category_pictures), mResources
                .getDrawable(R.drawable.main_pic_corners_bg, null),
                R.color.main_grid_item_color, false));

        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_videos, null),
                mResources.getString(R.string.category_vedios),
                mResources
                        .getDrawable(R.drawable.main_video_corners_bg, null),
                R.color.main_grid_item_color, false));

        mList.add(new CategoryItem(mResources
                .getDrawable(R.drawable.ic_cat_archives, null),
                mResources.getString(R.string.category_archives),
                mResources
                        .getDrawable(R.drawable.main_archives_corners_bg, null),
                R.color.main_grid_item_color, false));

        if (CommonUtils.isSupportPrivacyMode(mContext)) {
            mList.add(new CategoryItem(mResources
                    .getDrawable(R.drawable.ic_cat_private, null),
                    mResources.getString(R.string.category_safe),
                    mResources
                            .getDrawable(R.drawable.main_safe_corners_bg, null),
                    R.color.main_grid_item_color, true));
        }

        mAdapter.setList(mList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickLitener(this);
    }
    private void refreshView(){
        onChangeMainlayout();
//        refreshSizeView();
        isRunCreateActivity = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    List<String> mCurrentStoragePath = null;

    private void refreshSizeView() {
        int width = 0;
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        int i = 0;
        List<TaskInfo> mStorageTaskList = new ArrayList<TaskInfo>();
        if (mMountManager == null) {
            mMountManager = MountManager.getInstance();
        }
        mCurrentStoragePath = new ArrayList<String>();

        // Check whether the Phone storage is available
        if (!CommonUtils.isPhoneStorageZero()) {
            if (mPhoneStoContainer != null) {
                mCurrentStoragePath.add(mMountManager.getPhonePath());
                mPhoneStoContainer.setVisibility(View.VISIBLE);
            }

            if (mContext != null && mApplication.mFileInfoManager != null && mMountManager != null && mPhoneStoUsedInfo != null && mPhoneStoProgressBar != null) {
                mStorageTaskList.add(CommonUtils.getBaseTaskInfo(mApplication, new StorageListener(), CommonIdentity.STORAGE_SPACE_TASK, width,
                        CommonIdentity.STORAGE_INFO_CATEGORY, mPhoneStoUsedInfo, mPhoneStoProgressBar, mMountManager.getPhonePath()));
            }
        } else {
            if (mPhoneStoContainer != null) {
                mPhoneStoContainer.setVisibility(View.GONE);
            }
        }

        // Check whether the sd card amounted
        if (mContext != null && mMountManager.isSDCardMounted() && mMountManager != null && mSDStoUsedInfo != null && mSDStoContainer != null && mSdStoProgressBar != null) {
            mCurrentStoragePath.add(mMountManager.getSDCardPath());
            mSDStoContainer.setVisibility(View.VISIBLE);
            if (mApplication.mFileInfoManager != null) {
                mStorageTaskList.add(CommonUtils.getBaseTaskInfo(mApplication, new StorageListener(), CommonIdentity.STORAGE_SPACE_TASK, width,
                        CommonIdentity.STORAGE_INFO_CATEGORY, mSDStoUsedInfo, mSdStoProgressBar, mMountManager.getSDCardPath()));
            }
        } else {
            if (mSDStoContainer != null) {
                mSDStoContainer.setVisibility(View.GONE);
            }
        }

        // Check whether the otg amounted
        if (mMountManager != null && mMountManager.isOtgMounted()) {
            if (mExternalStoContainer != null) {
                mCurrentStoragePath.add(mMountManager.getUsbOtgPath());
                mExternalStoContainer.setVisibility(View.VISIBLE);
            }
            if (mContext != null && mApplication.mFileInfoManager != null && mMountManager != null && mExternalStoUsedInfo != null && mExternalStoProgressBar != null) {
                mStorageTaskList.add(CommonUtils.getBaseTaskInfo(mApplication, new StorageListener(), CommonIdentity.STORAGE_SPACE_TASK, width,
                        CommonIdentity.STORAGE_INFO_CATEGORY, mExternalStoUsedInfo, mExternalStoProgressBar, mMountManager.getUsbOtgPath()));
            }
        } else {
            if (mExternalStoContainer != null) {
                mExternalStoContainer.setVisibility(View.GONE);
            }
        }

        TaskInfo mStorageTaskInfo = new TaskInfo(mApplication, null, CommonIdentity.STORAGE_SPACE_TASK);
        mStorageTaskInfo.setTaskInfoList(mStorageTaskList);
        mApplication.mFileInfoManager.addNewTask(mStorageTaskInfo);

        if (mCategoryFragmentListener != null) {
            mCategoryFragmentListener.refreshStorageInfoUiForLand(progressBarAdapterView);
        }
    }

    private class StorageListener implements OperationEventListener {

        @Override
        public void onTaskResult(TaskInfo info) {
        }

        @Override
        public void onTaskPrepare() {
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            Message msg = new Message();
            Bundle mBundle = new Bundle();
            mBundle.putSerializable(CommonIdentity.RESULT_TASK_KEY, progressInfo);
            msg.setData(mBundle);
            mHandler.sendMessage(msg);
        }
    }

    private void loadCountText(final TextView textview) {
        final Handler mmHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "loadCountText method ==> enter msg.what-->" + msg.what + "obj is -->" + msg.obj);
                if ((CategoryManager.mCategoryItemMap.size() < 10 && CommonUtils.isInPrivacyMode(mContext))||CategoryManager.mCategoryItemMap.size() < 9) {
                    int what = msg.what;
                    if(what < mList.size() ) {
                        mList.get(what).updateCount((String) msg.obj);
                        CategoryManager.mCategoryItemMap.put(what, (String) msg.obj);
                    }
                } else {
                    CategoryManager.mCategoryItemMap.put(msg.what, (String) msg.obj);
                }
                mAdapter.notifyDataSetChanged();

                // Save category count info
                SharedPreferenceUtils.saveCategoryCountInfo(mContext, CategoryManager.mCategoryItemMap);
            }
        };

        CategoryManager.getInstance(mContext).loadCategoryCountText(new CountTextCallback() {
            @Override
            public void countTextCallback(TaskInfo mTaskInfo) {
                Message message = mmHandler.obtainMessage(mTaskInfo.getCategoryIndex(), 1, 1, mTaskInfo.getSearchContent());
                mmHandler.sendMessage(message);
            }
        });
    }


    private void refreshAdapter() {
        if (mAdapter != null) {
            loadCountText(null);
        }
    }

    public class CategoryItem {
        private Drawable icon;

        private String name;
        private int itemColorId;

        private Drawable mainDrawable;
        private String count = "";
        public boolean isSafeBox;

        public CategoryItem(Drawable icon, String name, Drawable main_bg,
                            int itemcolorId, boolean isSafeBox) {
            this.icon = icon;
            this.name = name;

            this.mainDrawable = main_bg;

            this.itemColorId = itemcolorId;
            this.isSafeBox = isSafeBox;
        }


        public Drawable getIcon() {
            return icon;
        }


        public String getName() {
            return name;
        }

        public Drawable getMainDrawable() {
            return mainDrawable;
        }


        public String getCount() {
            return count;
        }

        public void updateCount(String c) {
            count = c;
        }

    }


    @Override
    public void refreshCategory() {
        refreshSizeView();
        // Don't invoke the method refreshAdapter()
        // Because this will be invoked when onScannerFinished()
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            TaskInfo mTaskinfo = null;
            Bundle mBundle = null;
            ProgressInfo mResultInfo = null;
            if (what != CommonIdentity.DATA_UPDATED) {
                mBundle = msg.getData();
                if (mBundle == null) {
                    return;
                }
                mResultInfo = (ProgressInfo) mBundle.getSerializable(CommonIdentity.RESULT_TASK_KEY);
                mTaskinfo = mResultInfo.getTaskInfo();
                if (mTaskinfo == null) {
                    return;
                }
                what = mTaskinfo.getBaseTaskType();
            }
            switch (what) {
                case CommonIdentity.DATA_UPDATED:
                    refreshAdapter();
                    break;
                case CommonIdentity.STORAGE_SPACE_TASK:
                    TextView mTextView = mTaskinfo.getStorageSize();
                    ProgressBar progressBar = mTaskinfo.getStorageProgress();
                    String result = mResultInfo.getUpdateInfo();
                    long totalSpace = mResultInfo.getTotalSize();
                    long availableBlock = mResultInfo.getAvaiableSize();
                    int storageMode = mTaskinfo.getAdapterMode();

                    mTextView.setText(result);
                    if (storageMode == CommonIdentity.STORAGE_INFO_CATEGORY) {
                        double usedSpace = (double) (totalSpace - availableBlock);
                        progressBar.setMax(100);
                        progressBar.setProgress((int) ((usedSpace / ((double) totalSpace)) * 100));
                        if ((usedSpace / ((double) totalSpace)) <= SPACE_MEDIUM_USED) {
                            progressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.space_start_progress_color_horizontal));
                        } else if ((usedSpace / ((double) totalSpace)) > SPACE_MEDIUM_USED && (usedSpace / ((double) totalSpace)) <= SPACE_HEAVY_USED) {
                            progressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.space_center_progress_horizontal));
                        } else {
                            progressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.space_end_progress_color_horizontal));
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onScannerStarted() {
    }

    @Override
    public void onScannerFinished() {
        refreshAdapter();
    }

    @Override
    public void disableCategoryEvent(boolean disable) {
        if (mRecyclerView != null) {
            mRecyclerView.setEnabled(disable);
        }
    }

    public void onResume() {
        if ((mList != null && mList.size() == 10 && !CommonUtils.isInPrivacyMode(mContext))
                || (mList != null && mList.size() == 9 && CommonUtils.isInPrivacyMode(mContext))) {
            initData();
            refreshView();
        } else if (isAdded() && !isScreenScapeConsistent()) {
            refreshView();
        }
        if (!isRunCreateActivity) {
            refreshSizeView();
        }
        isRunCreateActivity = false;
        refreshAdapter();
        super.onResume();
    }

    /**
     The current direction and the direction of the actual screen are consistent
     */
    private boolean isScreenScapeConsistent() {
        // avoid not attached to Activity state
        if (isAdded()) {
            Configuration configuration = getResources().getConfiguration();
            int ori = configuration.orientation;
            if(mApplication.mPortraitOrientation){
                return ori == Configuration.ORIENTATION_PORTRAIT;
            } else {
                return ori == Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return false;
    }

    @Override
    public void dismissSafeDialog() {

    }

    private boolean isScreenLandscape() {
        // avoid not attached to Activity state
        if (isAdded()) {
            Configuration configuration = getResources().getConfiguration();
            int ori = configuration.orientation;
            mApplication.mPortraitOrientation = ori == Configuration.ORIENTATION_PORTRAIT;
            return !mApplication.mPortraitOrientation;
        }
        return false;
    }

    @Override
    public void onChangeMainlayout() {
        if (mRecyclerView == null) {
            return;
        }

        boolean isInMultiWindowMode = false;
        Activity activity = getActivity();
        if (activity != null && Build.VERSION.SDK_INT >= 24) {
            isInMultiWindowMode = activity.isInMultiWindowMode();
        }

        if (isInMultiWindowMode) {
            mRecyclerView.setLayoutManager(new FullyGridLayoutManager(getActivity(), 2));
            initPortView();
            refreshSizeView();
        } else {
            // get current orientation state, mApplication.mPortraitOrientation maybe not right in some special situation
            boolean currentLandState = isScreenLandscape();
            int column = 2;
            if (currentLandState) {
                column = 3;
//                initLandView();
            }
//            else {
                initPortView();
//            }
            mRecyclerView.setLayoutManager(new FullyGridLayoutManager(getActivity(), column));
            if(isAdded()) {
                refreshSizeView();
            }
        }
//        refreshSizeView();
    }

    @TargetApi(24)
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        mApplication.isInMultiWindowMode = isInMultiWindowMode;
        onChangeMainlayout();
    }

    private void initPortView() {
        if (LandCategoryLayoutContainer != null) {
            LandCategoryLayoutContainer.setVisibility(View.GONE);
        }
        if (portraitCategoryLayoutContainer != null) {
            portraitCategoryLayoutContainer.setVisibility(View.VISIBLE);

            mPhoneStoContainer = portraitCategoryLayoutContainer.findViewById(R.id.phone_storage_container);
            mSDStoContainer = portraitCategoryLayoutContainer.findViewById(R.id.sd_storage_container);
            mExternalStoContainer = portraitCategoryLayoutContainer.findViewById(R.id.external_storage_container);

            mPhoneStoProgressBar = (ProgressBar) portraitCategoryLayoutContainer.findViewById(R.id.phone_progressBar);
            mSdStoProgressBar = (ProgressBar) portraitCategoryLayoutContainer.findViewById(R.id.sd_progressBar);
            mExternalStoProgressBar = (ProgressBar) portraitCategoryLayoutContainer.findViewById(R.id.external_progressBar);

            mPhoneStoUsedInfo = (TextView) portraitCategoryLayoutContainer.findViewById(R.id.phone_used_info_tv);
            mSDStoUsedInfo = (TextView) portraitCategoryLayoutContainer.findViewById(R.id.sd_used_info_tv);
            mExternalStoUsedInfo = (TextView) portraitCategoryLayoutContainer.findViewById(R.id.external_used_info_tv);

            mPhoneName = (TextView) portraitCategoryLayoutContainer.findViewById(R.id.phone_name);
            mSDName = (TextView) portraitCategoryLayoutContainer.findViewById(R.id.sd_name);
            mExternalName = (TextView) portraitCategoryLayoutContainer.findViewById(R.id.external_name);

            mPhoneStoContainer.setOnClickListener((OnClickListener) mActivity);
            mSDStoContainer.setOnClickListener((OnClickListener) mActivity);
            mExternalStoContainer.setOnClickListener((OnClickListener) mActivity);
        }
    }

    private void initLandView() {
        if (portraitCategoryLayoutContainer != null) {
            portraitCategoryLayoutContainer.setVisibility(View.GONE);
        }

        if (LandCategoryLayoutContainer != null) {
            LandCategoryLayoutContainer.setVisibility(View.VISIBLE);
            mSDStoContainer = LandCategoryLayoutContainer.findViewById(R.id.sd_storage_container);
            mPhoneStoContainer = LandCategoryLayoutContainer.findViewById(R.id.phone_storage_container);
            mExternalStoContainer = LandCategoryLayoutContainer.findViewById(R.id.external_storage_container);


            mPhoneStoProgressBar = (ProgressBar) LandCategoryLayoutContainer.findViewById(R.id.phone_progressBar);
            mSdStoProgressBar = (ProgressBar) LandCategoryLayoutContainer.findViewById(R.id.sd_progressBar);
            mExternalStoProgressBar = (ProgressBar) LandCategoryLayoutContainer.findViewById(R.id.external_progressBar);

            mSDStoUsedInfo = (TextView) LandCategoryLayoutContainer.findViewById(R.id.sd_used_info_tv);
            mPhoneStoUsedInfo = (TextView) LandCategoryLayoutContainer.findViewById(R.id.phone_used_info_tv);
            mExternalStoUsedInfo = (TextView) LandCategoryLayoutContainer.findViewById(R.id.external_used_info_tv);

            mPhoneName = (TextView) LandCategoryLayoutContainer.findViewById(R.id.phone_name);
            mSDName = (TextView) LandCategoryLayoutContainer.findViewById(R.id.sd_name);
            mExternalName = (TextView) LandCategoryLayoutContainer.findViewById(R.id.external_name);

            mPhoneStoContainer.setOnClickListener((OnClickListener) mActivity);
            mSDStoContainer.setOnClickListener((OnClickListener) mActivity);
            mExternalStoContainer.setOnClickListener((OnClickListener) mActivity);

            progressBarAdapterView = LandCategoryLayoutContainer.findViewById(R.id.progressBar_adapter_view);
        }
    }

    public void onHiddenChanged(boolean hidden) {
        if (!hidden)
            refreshAdapter();
        super.onHiddenChanged(hidden);
    }

}
