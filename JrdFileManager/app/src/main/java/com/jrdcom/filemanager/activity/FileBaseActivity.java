package com.jrdcom.filemanager.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.IActivityListener;
import com.jrdcom.filemanager.IActivitytoCategoryListener;
import com.jrdcom.filemanager.MountReceiver;
import com.jrdcom.filemanager.MountReceiver.MountListener;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.adapter.PathBarAdapter;
import com.jrdcom.filemanager.adapter.SimPickerAdapter;
import com.jrdcom.filemanager.dialog.CommonDialogFragment;
import com.jrdcom.filemanager.fragment.CategoryFragment;
import com.jrdcom.filemanager.fragment.ListsFragment;
import com.jrdcom.filemanager.fragment.PermissionFragment;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.FileInfoComparator;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.PermissionUtil;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.ViewUtil;
import com.jrdcom.filemanager.view.CustomPopupWindowBasedAnchor;
import com.jrdcom.filemanager.view.Rotate3dAnimation;
import com.jrdcom.filemanager.view.ToastHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileBaseActivity extends AppCompatActivity implements
        MountListener, View.OnClickListener, OnQueryTextListener,
        OnCloseListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = FileBaseActivity.class.getSimpleName();

    protected static CustomPopupWindowBasedAnchor morePop = null;
    protected PrivateModeManager mPrivateModeManager;
    protected ToastHelper mToastHelper;
    protected RelativeLayout mNormalBar;
    protected LinearLayout mSearchBar;
    protected LinearLayout mGlobalSearchBar;
    protected ImageView mGlobalSearchBack;
    protected EditText mGlobalSearchView;
    protected ImageView mGlobalSearchCancel;
    protected SearchView mSearchView;
    protected String mSearchPath;
    protected boolean isSearching;
    protected boolean isSearchingDone;
    protected ImageView mBtnSort;
    protected ImageView mBtnShare;
    protected ImageView mBtnDelete;
    protected ImageView mBtnMore;
    protected ImageView mBtnSearch;
    protected View floatingActionButtonContainer;
    protected ImageButton floatingActionButton;
    protected TextView mActionBarPathText;
    protected ImageView mBtnEditBack;
    protected ImageView mSearchBack;
    protected ImageView mPrivacyInfoBtn;
    protected ListsFragment mListFragment;
    protected CategoryFragment mCategoryFragment;
    protected static PermissionFragment mPermissionFragment;
    protected Fragment mCurrentFragment;
    protected FileManagerApplication mApplication;
    protected MountManager mMountPointManager;
    protected int selectCount = 0;
    protected IActivityListener mActivityListener;
    protected boolean mIsHasDir;
    protected boolean mIsHasDrm;
    protected boolean mCanShare;
    protected boolean mIsHasZip;
    protected boolean mIsFLorSDDrm;
    protected boolean mHasAllPrivate;
    private MountReceiver mMountReceiver;
    protected int mWindowWidth = -1;
    protected TextView mTexSelect;
    protected TextView mTexGrid;
    protected TextView mTexList;
    protected TextView mTexCreateFolder;
    protected TextView mTexShow;
    protected TextView mTexHide;
    protected TextView mTexPaste;
    protected TextView mTexCut;
    protected TextView mTexCopy;
    protected TextView mTexDelete;
    protected TextView mTexDetail;
    protected TextView mTexRename;
    protected TextView mTextExtract;
    protected TextView mTextCompress;
    protected TextView mTexShare;
    protected TextView mTexSelectAll;
    protected TextView mTexUnSelectAll;
    protected TextView mTexShortcut;
    protected TextView mTexAddSafe;
    protected TextView mTexRemoveSafe;
    protected int morePopWidth;
    protected LinearLayout snackbarLayout;
    protected TextView snackTextView;
    protected ImageView mGlobalView;
    protected RelativeLayout mainLayout;
    protected String laucherFolderName;
    private Toolbar mMainToolbar = null;
    protected RelativeLayout Mainframe;
    protected RecyclerView fileBrowerList;
    protected LinearLayout filePathBrower;
    protected ImageView home_item;
    protected ImageView home_path_arrow;
    protected ImageView home_path_icon;
    protected AppBarLayout appBarLayout;
    protected AppBarLayout.LayoutParams mParams;
    protected static HashMap<String, String[]> permissionMap = new HashMap<String, String[]>();
    protected static CustomPopupWindowBasedAnchor sortPop = null;
    protected static String mSaveQueryText = "";
    protected boolean isSearchMode;
    protected String mTagMode = CommonIdentity.CATEGORY_TAG;
    protected IActivitytoCategoryListener mActivitytoCategoryListener;
    protected static List<FileInfo> mSaveCheckedList = new ArrayList<FileInfo>();
    protected static List<FileInfo> mSavePastedList = new ArrayList<FileInfo>();
    protected static List<FileInfo> mSaveSelectedList = new ArrayList<FileInfo>();
    protected static int mPasteOperation = CommonIdentity.FILE_FILTER_TYPE_UNKOWN;
    protected ImageView searchcloseBtn;

    protected static int mSaveSafeCategory = -1;
    protected static int mSaveCategory = -1;
    protected static int mSaveStatus = CommonIdentity.FILE_STATUS_NORMAL;
    protected static int mSaveOperation = CommonIdentity.FILE_SAFE_VIEW_MODE;
    protected static boolean isshowFileTypeInterface = false;
    protected static int enterPermissionResultCount = 1;
    protected StorageManager mStorageManager;
    protected PathBarAdapter pathBarAdapter;

    protected Animation floatBtnShowAnim;
    protected Animation floatBtnHideAnim;
    protected CommonDialogFragment mExitDialog;
    protected CommonDialogFragment mNoStorageDialog;
    protected static int runCreateCount=0;
    //No storage dialog is displayed
    protected static boolean isShowNoStorageDialog = false;

    protected boolean isClosePermission = false;
    protected boolean isMounted = false;
    private long launchTime=0;

    private Animation rotationAnim;
    private AnimatorSet animatorSet;
    private ObjectAnimator scaleX;
    private ObjectAnimator scaleY;
    private ObjectAnimator alpha;

    private byte proPrivateMode=0;
    protected static boolean isChangeMultiScreen = false;

    static {
        //TODO:Define Activity class and permission here
        permissionMap.put(FileBrowserActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        permissionMap.put(FileSelectionActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        permissionMap.put(PathSelectionActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        permissionMap.put(FilePrivateModeActivity.class.getName(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
    }

    protected Handler mBaseHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case CommonIdentity.INIT_ACTIVITY_ONCREATE:
                    mMountReceiver = MountReceiver.registerMountReceiver(FileBaseActivity.this);
                    mMountReceiver.registerMountListener(FileBaseActivity.this);
                    mPrivateModeManager = new PrivateModeManager(FileBaseActivity.this);
                    mToastHelper = new ToastHelper(FileBaseActivity.this);
                    mApplication.isSysteSupportDrm = CommonUtils.isDRMColumn(FileBaseActivity.this);
                    mApplication.isInMultiWindowMode = CommonUtils.isInMultiWindowMode(FileBaseActivity.this);
//                    mApplication.isBuiltInStorage = CommonUtils.isPhoneStorageZero();
//                    Intent intent = getIntent();
//                    if (intent != null) {
//                        laucherFolderName = intent.getStringExtra("foldername");
//                        intent.removeExtra("foldername");
//                        if (laucherFolderName != null && !PermissionUtil.isAllowPermission(FileBaseActivity.this)) {
//                            File launcherFile = new File(laucherFolderName);
//                            if (!launcherFile.exists() || (!mApplication.isShowHidden && launcherFile.isHidden())
//                                    || isPathInvalid(laucherFolderName)) {
//                                laucherFolderName = null;
//                                mToastHelper.showToast(R.string.shortcut_no_exist);
//                            }
//                        }
//                    }
                    break;
                case CommonIdentity.INIT_ACTIVITY_MAINCONTENTVIEW:
                    fileBrowerList = (RecyclerView) findViewById(R.id.listview);
                    home_item = (ImageView) findViewById(R.id.main_horizontallist_path);
                    home_path_arrow = (ImageView) findViewById(R.id.home_arrow);
                    home_path_icon = (ImageView) findViewById(R.id.main_horizontallist_icon);
                    snackbarLayout = (LinearLayout) findViewById(R.id.snackbarlayout);
                    snackTextView = (TextView) findViewById(R.id.snackbarlayout_text);
                    initFloatMenu();
                    if(CommonUtils.hasN()) {
                        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                        mStorageManager.registerListener(mStorageListener);
                    }
                    //get AppbarLayout params for setScrollFlags change Scroll status
                    mParams = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
                    break;
                case CommonIdentity.MSG_HAWKEYE_RESUME:
                    if(mApplication != null) {
                        mApplication.isBuiltInStorage = CommonUtils.isPhoneStorageZero();
                    }
                    if(launchTime>=0 && mApplication != null){
                        launchTime=System.currentTimeMillis()-launchTime;
                        CommonUtils.hawkeyeTimeEvent(getApplicationContext(),
                                CommonIdentity.FILEMANAGER_LAUNCH_TIME,
                                launchTime,mApplication);
                    }
                    launchTime=-1;
                    break;
                case CommonIdentity.MSG_BASE_ACTIVITY_RESTART:
                    if(CommonUtils.isInPrivacyMode(mApplication) && proPrivateMode==-1) {
                        mApplication.mCache.clearCache();
                        if ((CommonUtils.isPathNormalMode(getFileMode()) || CommonUtils.isCategoryNormalMode(getFileMode()) && mCurrentFragment == mListFragment )){
                            refreshPathAdapter(mApplication.mCurrentPath);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchTime=System.currentTimeMillis();
        runCreateCount++;
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        if (CommonUtils.isEditStatus(mApplication)) {
            changeStatusBarColor(true);
        }
        mApplication.mPortraitOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        mBaseHandler.sendEmptyMessage(CommonIdentity.INIT_ACTIVITY_ONCREATE);
        mTagMode = SharedPreferenceUtils.getPrefCurrTag(mApplication);
        mMountPointManager = MountManager.getInstance();
        mMountPointManager.init(mApplication);
        mApplication.mCurrentStatus = SharedPreferenceUtils.getPrefsStatus(mApplication);
        mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(this);
        mApplication.mSortType = SharedPreferenceUtils.getPrefsSortBy(mApplication);
        setMainContentView();
        if (!this.getClass().getName().equals(FileSelectionActivity.class.getName()) &&
                !this.getClass().getName().equals(PathSelectionActivity.class.getName()) &&
                !this.getClass().getName().equals(FilePrivateModeActivity.class.getName())) {
            checkCreatePermission();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        proPrivateMode = CommonUtils.isInPrivacyMode(mApplication)?(byte)1:(byte)-1;
    }

    public void setMainContentView() {
        setContentView(R.layout.main);
        mBaseHandler.sendEmptyMessage(CommonIdentity.INIT_ACTIVITY_MAINCONTENTVIEW);
        mainLayout = (RelativeLayout) findViewById(R.id.content_frame);
        filePathBrower = (LinearLayout) findViewById(R.id.main_filebrower);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mListFragment = new ListsFragment();
        mCategoryFragment = new CategoryFragment();
        if(mPermissionFragment==null) {
            mPermissionFragment = new PermissionFragment();
        }
        initActionBar();

    }

    public void initActionBar() {
        mMainToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        if (CommonUtils.isEditStatus(mApplication)) {
            mMainToolbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.edit_bar_bg));
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.actionbar, null);
        Mainframe = (RelativeLayout) findViewById(R.id.content_frame);
        setSupportActionBar(mMainToolbar);
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        mMainToolbar.addView(customActionBarView, layoutParams);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_back); // MODIFIED by caiminjie, 2017-09-20,BUG-5346088
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        if (customActionBarView != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mNormalBar = (RelativeLayout) customActionBarView
                    .findViewById(R.id.normal_bar);
            mSearchBar = (LinearLayout) customActionBarView
                    .findViewById(R.id.search_bar);
            mSearchView = (SearchView) customActionBarView
                    .findViewById(R.id.search_view);
            mGlobalSearchBar = (LinearLayout) customActionBarView
                    .findViewById(R.id.global_search_view);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mBtnEditBack = (ImageView) customActionBarView
                    .findViewById(R.id.edit_back);
            mBtnEditBack.setOnClickListener(this);
            mSearchBack = (ImageView) customActionBarView
                    .findViewById(R.id.search_back);
            mSearchBack.setOnClickListener(this);
            initSearchViewStyle();
            mBtnSort = (ImageView) customActionBarView
                    .findViewById(R.id.sort_btn);
            mBtnShare = (ImageView) customActionBarView
                    .findViewById(R.id.share_btn);
            mBtnShare.setOnClickListener(this);
            mBtnDelete = (ImageView) customActionBarView
                    .findViewById(R.id.delete_btn);
            mBtnDelete.setOnClickListener(this);
            mBtnSort.setOnClickListener(this);
            mBtnMore = (ImageView) customActionBarView
                    .findViewById(R.id.more_btn);
            mBtnMore.setOnClickListener(this);
            mBtnSearch = (ImageView) customActionBarView
                    .findViewById(R.id.search_btn);
            mBtnSearch.setOnClickListener(this);
            mGlobalSearchBack = (ImageView) customActionBarView
                    .findViewById(R.id.global_search_back);
            mGlobalSearchBack.setOnClickListener(this);
            mGlobalSearchView = (EditText) customActionBarView
                    .findViewById(R.id.global_search_text);
            mGlobalSearchView.addTextChangedListener(tbxSearch_TextChanged);
            mGlobalSearchView.setOnEditorActionListener(lEditorActionListener);
            mGlobalSearchCancel = (ImageView) customActionBarView
                    .findViewById(R.id.global_search_cancel);
            mGlobalSearchCancel.setOnClickListener(this);
            mGlobalView = (ImageView) customActionBarView.findViewById(R.id.global_search_btn);
            mGlobalView.setOnClickListener(this);
            mPrivacyInfoBtn = (ImageView) customActionBarView.findViewById(R.id.privacy_info);
            mPrivacyInfoBtn.setOnClickListener(this);
        }
        mActionBarPathText = (TextView) customActionBarView
                .findViewById(R.id.path_text);
        if (PermissionUtil.isAllowPermission(FileBaseActivity.this) && CommonUtils.hasM()) {
            getSupportActionBar().hide();
        } else {
            if (!CommonUtils.isEditStatus(mApplication)) {
                mBtnEditBack.setVisibility(View.GONE);
                mBtnSort.setVisibility(View.GONE);
                if (CommonUtils.isSearchStatus(mApplication)) {
                    mSearchBar.setVisibility(View.VISIBLE);
                    mNormalBar.setVisibility(View.GONE);
                } else {
                    updateNormalBar();
                    mSearchBar.setVisibility(View.GONE);
                    mNormalBar.setVisibility(View.VISIBLE);
                }
            } else {
                setActionBarDisplayHomeAsUpEnabled(false);
                if (CommonUtils.isCategoryMode()) {
                    setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.edit_bar_bg));
                } else {
                    setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.edit_bar_bg));
                    filePathBrower.setBackgroundColor(getResources().getColor(R.color.actionbar_edit_bg));
                }
            }
        }

    }



    public void checkCreatePermission() {
        final String className = this.getClass().getName();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (permissionMap.get(className) != null) {
                    String[] permissions;
                    // If use mie upgrade sdk, need READ_PHONE_STATE permission
//                    if (FileBrowserActivity.class.getName().equals(className)
//                            && CommonUtils.isSupportUpgrade(getApplicationContext())) {
//                        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                Manifest.permission.READ_PHONE_STATE};
//                        PermissionUtil.checkAndRequestPermissions(FileBaseActivity.this, permissions,
//                                PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
//                    } else {
                        permissions = permissionMap.get(className);
                        PermissionUtil.checkAndRequestPermissions(FileBaseActivity.this, permissions,
                                PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
                    }
//                }
            }
        }, 1000);
    }


    @Override
    public void onRestart() {
        super.onRestart();
        checkPermission();
        mBaseHandler.sendEmptyMessage(CommonIdentity.MSG_BASE_ACTIVITY_RESTART);
//        initFileInfoManager();
    }

    public void checkPermission() {
        final String className = this.getClass().getName();
        if (className != null && !PermissionUtil.isShowPermissionDialog && permissionMap.get(className) != null &&
                ((mPermissionFragment != null && mCurrentFragment != mPermissionFragment) || (laucherFolderName != null &&
                        PermissionUtil.isAllowPermission(this)))) {
            String[] permissions;
            // If use mie upgrade sdk, need READ_PHONE_STATE permission
//            if (FileBrowserActivity.class.getName().equals(className)
//                    && CommonUtils.isSupportUpgrade(getApplicationContext())) {
//                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_PHONE_STATE};
//                PermissionUtil.checkAndRequestPermissions(FileBaseActivity.this, permissions,
//                        PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
//            } else {
                permissions = permissionMap.get(className);
                PermissionUtil.checkAndRequestPermissions(FileBaseActivity.this, permissions,
                        PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
//            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runCreateCount=0;
        FileBrowserActivity.comparepath = "";
        if(mMountReceiver != null) {
            unregisterReceiver(mMountReceiver);
        }
        if(mStorageManager != null && mStorageListener != null && CommonUtils.hasN()) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        if(mApplication.mProgressDialog != null){
            mApplication.mProgressDialog.dismiss();
        }
        mPermissionFragment=null;
    }

    @Override
    public void onMounted() {
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        isMounted = true;
        mMountPointManager.init(this);
    }

    @Override
    public void onUnmounted(String mountPoint) {
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        mMountPointManager.init(this);
        if (mApplication.mFileInfoManager != null
                && mApplication.mFileInfoManager.getPasteCount() > 0) {
            FileInfo fileInfo = mApplication.mFileInfoManager.getPasteList()
                    .get(0);
            if (fileInfo.getFileAbsolutePath().startsWith(
                    mountPoint + MountManager.SEPARATOR)) {
                mApplication.mFileInfoManager.clearPasteList();
            }
        }
    }

    protected void removeCategoryCache(boolean isHotPlug, boolean isClearCurrShowList){
        // remove category cache
        if(isClearCurrShowList && mApplication.mFileInfoManager!=null){
            mApplication.mFileInfoManager.clearShowFiles();
        }
        if(mApplication.mCache != null){
            mApplication.mCache.removeCache("0");
            mApplication.mCache.removeCache("1");
            mApplication.mCache.removeCache("2");
            mApplication.mCache.removeCache("3");
            mApplication.mCache.removeCache("4");
            mApplication.mCache.removeCache("5");
            mApplication.mCache.removeCache("6");
            mApplication.mCache.removeCache("7");
            mApplication.mCache.removeCache("8");
            if(isHotPlug && CommonUtils.isCategoryMode() && mActivityListener != null){
                mActivityListener.refreshAdapter(null, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_CATEGORY_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
            }
        }
    }

    @Override
    public void onEject() {
    }

    protected void showToastForUnmountCurrentSDCard(String path) {
        if (mMountPointManager == null) {
            mMountPointManager = MountManager.getInstance();
        }
        String mountPointDescription = mMountPointManager.getDescriptionPath(path);
        if (MountManager.getInstance().isExternalMountPath(path)) {// add for
            mToastHelper.showToast(getString(R.string.unmounted,
                    mountPointDescription));
        }
    }

    public View initMorePopWindow() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customMoreView = inflater.inflate(R.layout.more_menu, null);
        morePopWidth = getResources().getDimensionPixelSize(R.dimen.sort_menu_width);
        mTexSelect = (TextView) customMoreView.findViewById(R.id.select_item);
        mTexGrid = (TextView) customMoreView.findViewById(R.id.grid_item);
        mTexList = (TextView) customMoreView.findViewById(R.id.list_item);
        mTexCreateFolder = (TextView) customMoreView.findViewById(R.id.createfolder_item);
        mTexShow = (TextView) customMoreView.findViewById(R.id.show_item);
        mTexHide = (TextView) customMoreView.findViewById(R.id.hide_item);
        mTexPaste = (TextView) customMoreView.findViewById(R.id.paste_item_normal);
        mTexCut = (TextView) customMoreView.findViewById(R.id.cut_item_normal);
        mTexCopy = (TextView) customMoreView.findViewById(R.id.copy_item_normal);
        mTexDelete = (TextView) customMoreView.findViewById(R.id.delete_item_normal);
        mTexDetail = (TextView) customMoreView.findViewById(R.id.details_item_normal);
        mTexRename = (TextView) customMoreView.findViewById(R.id.rename_item_normal);
        mTextExtract = (TextView) customMoreView.findViewById(R.id.extract_item_normal);
        mTextCompress = (TextView) customMoreView.findViewById(R.id.compress_item_normal);
        mTexShare = (TextView) customMoreView.findViewById(R.id.share_item_normal);
        mTexSelectAll = (TextView) customMoreView.findViewById(R.id.selectall_item_normal);
        mTexUnSelectAll = (TextView) customMoreView.findViewById(R.id.unselectall_item_normal);
        mTexShortcut = (TextView) customMoreView.findViewById(R.id.shortcut_item_normal);
        mTexAddSafe = (TextView) customMoreView.findViewById(R.id.move_to_safe_item_normal);
        mTexRemoveSafe =(TextView) customMoreView.findViewById(R.id.remove_to_safe_item_normal);

        mTexSelect.setOnClickListener(this);
        mTexGrid.setOnClickListener(this);
        mTexList.setOnClickListener(this);
        mTexCreateFolder.setOnClickListener(this);
        mTexShow.setOnClickListener(this);
        mTexHide.setOnClickListener(this);
        mTexCut.setOnClickListener(this);
        mTexCopy.setOnClickListener(this);
        mTexDelete.setOnClickListener(this);
        mTexDetail.setOnClickListener(this);
        mTexRename.setOnClickListener(this);
        mTextExtract.setOnClickListener(this);
        mTextCompress.setOnClickListener(this);
        mTexShare.setOnClickListener(this);
        mTexSelectAll.setOnClickListener(this);
        mTexUnSelectAll.setOnClickListener(this);
        mTexPaste.setOnClickListener(this);
        mTexShortcut.setOnClickListener(this);
        mTexAddSafe.setOnClickListener(this);
        mTexRemoveSafe.setOnClickListener(this);
        mTexRemoveSafe.setVisibility(View.GONE);
        mTextCompress.setVisibility(View.GONE);
        mTextExtract.setVisibility(View.GONE);
        if (CommonUtils.isNormalStatus(mApplication) || CommonUtils.isCopyNormalStatus(mApplication)) {
            mTexSelectAll.setVisibility(View.GONE);
            mTexUnSelectAll.setVisibility(View.GONE);
            mTexPaste.setVisibility(View.GONE);
            mTexCut.setVisibility(View.GONE);
            mTexCopy.setVisibility(View.GONE);
            mTexDelete.setVisibility(View.GONE);
            mTexDetail.setVisibility(View.GONE);
            mTexRename.setVisibility(View.GONE);
            mTextExtract.setVisibility(View.GONE);
            mTextCompress.setVisibility(View.GONE);
            mTexShare.setVisibility(View.GONE);
            mTexShortcut.setVisibility(View.GONE);
            mTexAddSafe.setVisibility(View.GONE);
            if (CommonIdentity.LIST_MODE.equals(SharedPreferenceUtils.getPrefsViewBy(mApplication))) {
                mTexGrid.setVisibility(View.VISIBLE);
                mTexList.setVisibility(View.GONE);
            } else {
                mTexGrid.setVisibility(View.GONE);
                mTexList.setVisibility(View.VISIBLE);
            }
            if (mActivityListener != null) {
                mTexSelect.setVisibility(View.VISIBLE);
                if (mActivityListener.getAdapterSize() == 0 || mApplication.mFileInfoManager.getPasteCount() > 0) {
                    //mTexSelect.setVisibility(View.GONE);
                    mTexSelect.setTextColor(getResources().getColor(R.color.actionbar_search_text_color));
                    mTexSelect.setEnabled(false);
                } else {
                    mTexSelect.setTextColor(getResources().getColor(R.color.grid_item_name_color));
                    mTexSelect.setEnabled(true);
                }
            }

            mTexCreateFolder.setVisibility(View.VISIBLE);
            if (mApplication.isShowHidden) {
                mTexShow.setVisibility(View.GONE);
                mTexHide.setVisibility(View.VISIBLE);
            } else {
                mTexShow.setVisibility(View.VISIBLE);
                mTexHide.setVisibility(View.GONE);
            }
            if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                mTexShow.setVisibility(View.GONE);
                mTexHide.setVisibility(View.GONE);
                mTexCreateFolder.setVisibility(View.GONE);
            }
            if (mCurrentFragment != mCategoryFragment) {
                if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                    mTexPaste.setVisibility(View.GONE);
                    mTexCreateFolder.setVisibility(View.GONE);
                } else {
                    if (mApplication.mFileInfoManager.getPasteCount() > 0) {
                        mTexPaste.setVisibility(View.VISIBLE);
                        mTexCreateFolder.setVisibility(View.VISIBLE);
                    } else {
                        mTexCreateFolder.setVisibility(View.VISIBLE);
                    }
                }
            } else if (mCurrentFragment == mCategoryFragment) {
                mTexPaste.setVisibility(View.GONE);
                mTexCreateFolder.setVisibility(View.GONE);
                mTexSelect.setVisibility(View.GONE);
                mTexGrid.setVisibility(View.GONE);
                mTexList.setVisibility(View.GONE);
            }
        } else if (CommonUtils.isEditStatus(mApplication)) {
            mTexAddSafe.setVisibility(View.GONE);
            if (selectCount == 1) {
                if (!mIsHasZip) {
                    mTextExtract.setVisibility(View.GONE);
                    mTextCompress.setVisibility(View.VISIBLE);
                } else if (mIsHasZip) {
                    mTextExtract.setVisibility(View.VISIBLE);
                    mTextCompress.setVisibility(View.GONE);
                }
                mTexRename.setVisibility(View.VISIBLE);
                mTexDetail.setVisibility(View.VISIBLE);
            } else {
                mTextExtract.setVisibility(View.GONE);
                mTextCompress.setVisibility(View.VISIBLE);
                mTexRename.setVisibility(View.GONE);
                mTexDetail.setVisibility(View.GONE);
                mTexCopy.setVisibility(View.GONE);
                if (selectCount != 0) {
                    mTexShare.setVisibility(View.VISIBLE);
                    if (CommonUtils.isSupportPrivacyMode(this) && !CommonUtils.isExternalStorage(mApplication.mCurrentPath,mMountPointManager,mApplication.isBuiltInStorage)) {
                        mTexAddSafe.setVisibility(View.VISIBLE);
                    }
                }
            }
            if (selectCount > 0) {
                mTexDelete.setVisibility(View.VISIBLE);
                mTexCut.setVisibility(View.VISIBLE);
                if (mIsHasDrm) {
                    mTexCopy.setVisibility(View.GONE);
                    mTextExtract.setVisibility(View.GONE);
                    mTextCompress.setVisibility(View.GONE);
                } else {
                    mTexCopy.setVisibility(View.VISIBLE);
                }
                if (mCanShare || mIsFLorSDDrm) {
                    if (selectCount != 0) {
                        mTexShare.setVisibility(View.VISIBLE);
                        if (mCanShare && CommonUtils.isSupportPrivacyMode(this) && !CommonUtils.isExternalStorage(mApplication.mCurrentPath,mMountPointManager,mApplication.isBuiltInStorage)) {
                            mTexAddSafe.setVisibility(View.VISIBLE);
                        }
                        mTexShortcut.setVisibility(View.GONE);
                    }
                } else {
                    if (selectCount > 1) {
                        mTexShortcut.setVisibility(View.GONE);
                    } else {
                        if (selectCount == 1 && mIsHasDir) {
                            mTexShortcut.setVisibility(View.VISIBLE);
                        } else {
                            mTexShortcut.setVisibility(View.GONE);
                        }
                    }
                    if (selectCount != 0) {
                        mTexShare.setVisibility(View.GONE);
                        mTexAddSafe.setVisibility(View.GONE);
                    }
                }

            }
            if (selectCount != 0) {
                if (mActivityListener.checkIsSelectAll()) {
                    mTexSelectAll.setVisibility(View.GONE);
                    mTexUnSelectAll.setVisibility(View.VISIBLE);
                } else {
                    mTexSelectAll.setVisibility(View.VISIBLE);
                    mTexUnSelectAll.setVisibility(View.GONE);
                }
            }

            if(mCanShare && mHasAllPrivate && CommonUtils.isSupportPrivacyMode(this)){
                mTexRemoveSafe.setVisibility(View.VISIBLE);
                mTexAddSafe.setVisibility(View.GONE);
            } else {
                mTexRemoveSafe.setVisibility(View.GONE);
            }

            if (selectCount == 0) {
                mTexSelectAll.setVisibility(View.VISIBLE);
                mTexCopy.setVisibility(View.GONE);
                mTexDelete.setVisibility(View.GONE);
                mTexCut.setVisibility(View.GONE);
                mTexShare.setVisibility(View.GONE);
                mTexAddSafe.setVisibility(View.GONE);
                mTextCompress.setVisibility(View.GONE);
                mTexUnSelectAll.setVisibility(View.GONE);
                mTexShortcut.setVisibility(View.GONE);
                mTextExtract.setVisibility(View.GONE);
                mTexRemoveSafe.setVisibility(View.GONE);
            }


            if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                mTextExtract.setVisibility(View.GONE);
                mTextCompress.setVisibility(View.GONE);
            }
            mTexGrid.setVisibility(View.GONE);
            mTexList.setVisibility(View.GONE);
            mTexShow.setVisibility(View.GONE);
            mTexHide.setVisibility(View.GONE);
            mTexSelect.setVisibility(View.GONE);
            mTexCreateFolder.setVisibility(View.GONE);
            mTexPaste.setVisibility(View.GONE);
        }
        return customMoreView;
    }


    public void initSearchViewStyle() {
        try {
            int searchPlate = getResources().getIdentifier(
                    "android:id/search_plate", null, null);
            mSearchView.findViewById(searchPlate).setBackground(null);
            int queryTextViewId = getResources().getIdentifier(
                    "android:id/search_src_text", null, null);
            View autoComplete = mSearchView.findViewById(queryTextViewId);
            Class<?> clazz = Class
                    .forName("android.widget.SearchView$SearchAutoComplete");
            SpannableStringBuilder stopHint = new SpannableStringBuilder("");
            stopHint.append(getString(R.string.default_search_text));
            // Add the icon as an spannable
            Method textSizeMethod = clazz.getMethod("getTextSize");
            textSizeMethod.invoke(autoComplete);
            Method textColor = clazz.getMethod("setTextColor", Integer.TYPE);
            textColor.invoke(autoComplete, 0xdeffffff);
            // Set the new hint text
            Method setHintMethod = clazz.getMethod("setHint",
                    CharSequence.class);
            setHintMethod.invoke(autoComplete, stopHint);
            Method setHintColorMethod = clazz.getMethod("setHintTextColor",
                    Integer.TYPE);
            setHintColorMethod.invoke(autoComplete, 0x7fffffff);
            Field mQueryTextView = null;
            if (Build.VERSION.SDK_INT < 23) {
                mQueryTextView = mSearchView.getClass().getDeclaredField(
                        "mQueryTextView");
            } else {
                mQueryTextView = mSearchView.getClass().getDeclaredField(
                        "mSearchSrcTextView");
            }
            mQueryTextView.setAccessible(true);
            Class<?> mTextViewClass = mQueryTextView.get(mSearchView)
                    .getClass().getSuperclass().getSuperclass().getSuperclass();
            Field mCursorDrawableRes = mTextViewClass
                    .getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(mQueryTextView.get(mSearchView),
                    R.drawable.text_cursor_material);
            int closeBtnViewId = getResources().getIdentifier(
                    "android:id/search_close_btn", null, null);
            searchcloseBtn = (ImageView) mSearchView
                    .findViewById(closeBtnViewId);
            searchcloseBtn.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_close));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void initFloatMenu() {
        floatingActionButtonContainer = findViewById(R.id.floating_action_button_container);
        ViewUtil.setupFloatingActionButton(
                floatingActionButtonContainer, getResources());
        floatingActionButton = (ImageButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(this);
        floatingActionButtonContainer.setVisibility(View.GONE);
        floatingActionButton.setVisibility(View.GONE);
    }

    protected void updateCategoryNormalBar() {
        if (mCurrentFragment == mCategoryFragment) { // update home page actionbar title
            if(mApplication.mCurrentStatus == CommonIdentity.FILE_COPY_NORMAL){
                mBtnEditBack.setVisibility(View.VISIBLE);
                mGlobalView.setVisibility(View.GONE);
                setActionBarTitle(R.string.choice_file);
                changeStatusBarColor(true);
                setActionBarBackgroundDrawable(getResources().getDrawable(R.drawable.edit_bar_bg));
            }else {
                setActionBarTitle(R.string.app_name_new); // MODIFIED by Chuanzhi.Shao, 2017-10-11,BUG-5395138
            }
        } else if (CategoryManager.mCurrentCagegory != -1) {
            // update category actionbar title
            setActionBarDisplayHomeAsUpEnabled(true);
            int s = CategoryManager.getCategoryString(CategoryManager.mCurrentCagegory);
            if (s != 0) {
                setActionBarTitle(s);
            }
        }
    }

    public void updateCategoryNavigation(int id) {
    }

    protected void refreshPathAdapter(String path) {
    }

    protected int getFileMode() {
        return SharedPreferenceUtils.getPrefsStatus(this);
    }

    protected void updateNormalBar() {
        if (mCurrentFragment == mCategoryFragment) {
            setActionBarTitle(R.string.app_name_new); // MODIFIED by Chuanzhi.Shao, 2017-10-11,BUG-5395138
        } else {
            if (mApplication == null) return;
            if (mMountPointManager == null) {
                mMountPointManager = MountManager.getInstance();
            }
            String path = mMountPointManager
                    .getDescriptionPath(mApplication.mCurrentPath);
            if (path != null && !path.isEmpty()) {
                String result = null;
                if (mApplication.mFileInfoManager.getPasteCount() == 0
                        || mBtnEditBack.getVisibility() == View.GONE) {
                    if (path.contains(MountManager.SEPARATOR)) {
                        result = path.substring(path
                                .lastIndexOf(MountManager.SEPARATOR) + 1);
                        setActionBarTitle(result);
                    } else {
                        setActionBarTitle(path);
                    }
                }
            } else if ((CommonUtils.isNormalStatus(mApplication) || CommonUtils.isCopyNormalStatus(mApplication)) &&
                    mApplication.mFileInfoManager != null && mApplication.mFileInfoManager.getPasteCount() > 0) {
            } else {
                setActionBarTitle(R.string.app_name_new); // MODIFIED by Chuanzhi.Shao, 2017-10-11,BUG-5395138
            }
        }
    }

    public void setActionBarTitle(String text) {
        if (mActionBarPathText != null) {
            mActionBarPathText.setText(text);
        }
    }

    public void setActionBarTitle(int strId) {
        if (mActionBarPathText != null && strId > 0) {
            mActionBarPathText.setText(strId);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return true;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    protected String mQueryText;

    public String getQueryText() {
        return mQueryText;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    public void onClick(View v) {
    }

    public void onScannerFinished() {
    }

    public void onScannerStarted() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mApplication.mPortraitOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        switchFloatButtonDirection();
    }

    private void switchFloatButtonDirection(){
        if(mListFragment == null || mListFragment.menuMultipleActions == null || mListFragment.menuMultipleActionsLeft == null){
            return;
        }
        if(mCurrentFragment == mListFragment) {
            if (mApplication.mPortraitOrientation) {
                if(mListFragment.menuMultipleActionsLeft.getVisibility() == View.VISIBLE){
                    mListFragment.menuMultipleActions.setVisibility(View.VISIBLE);
                    mListFragment.menuMultipleActionsLeft.setVisibility(View.GONE);
                    if(mListFragment.menuMultipleActionsLeft.isExpanded()){
                        mListFragment.menuMultipleActions.expand();
                    }else {
                        mListFragment.menuMultipleActions.collapse();
                    }
                }
            } else {
                if(mListFragment.menuMultipleActions.getVisibility() == View.VISIBLE){
                    mListFragment.menuMultipleActions.setVisibility(View.GONE);
                    mListFragment.menuMultipleActionsLeft.setVisibility(View.VISIBLE);
                    if(mListFragment.menuMultipleActions.isExpanded()){
                        mListFragment.menuMultipleActionsLeft.expand();
                    }else {
                        mListFragment.menuMultipleActionsLeft.collapse();
                    }
                }
            }
        }
    }

    protected boolean isPathInvalid(String path) {
        if (path != null) {
            FileInfo fileInfo = new FileInfo(this, path);
            if (!mMountPointManager.isExternalFile(fileInfo)) { //The path is internal file. Judge the phone storage.
                return CommonUtils.isPhoneStorageZero();
            } else {
                return false;
            }
        } else {
            return (CommonUtils.isPhoneStorageZero() && !mMountPointManager.isSDCardMounted() && !mMountPointManager.isOtgMounted());
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        laucherFolderName = intent.getStringExtra("foldername");
    }


    protected void setActionBarDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
        }
    }

    @TargetApi(24)
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (mApplication == null) {
            mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        }
        mApplication.isInMultiWindowMode = isInMultiWindowMode;

        if (mActivityListener != null) {
            mActivityListener.closeItemMorePop();
            mActivityListener.closeFloatMenu(!isInMultiWindowMode);
//            if(isInMultiWindowMode) {
                mActivityListener.cancelProgressDialog();
//            }
        }
        if (sortPop != null) {
            if(isChangeMultiScreen) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sortPop.dismiss();
                    }
                }, 200);
            } else {
                sortPop.dismiss();
            }
        }
        if (morePop != null) {
            if(isChangeMultiScreen) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        morePop.dismiss();
                    }
                }, 200);
            } else {
                morePop.dismiss();
            }
        }
        isChangeMultiScreen = false;
    }

    /**
     * Dynamic search
     */
    protected TextWatcher tbxSearch_TextChanged = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                if (mActivityListener != null) {
                    mActivityListener.clearAdapter();
                }
            }
            mQueryText = s.toString();

            if (TextUtils.isEmpty(s.toString())) {
                if (isSearchMode) {
                    if (mActivityListener != null) {
                        mActivityListener.showNoSearchResults(false, null);
                    }
                }
                return;
            }
            if (mActivityListener != null) {
                mActivityListener.clearAdapter();
            }
            String regx = ".*[/\\\\:*?\"<>|].*";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(s.toString());
            if (m.find()) {
                try {
                    String mToastStr = getString(R.string.invalid_char_prompt);
                    if (mToastHelper != null && mToastStr != null) {
                        mToastHelper.showToast(mToastStr);
                    }
                } catch (Exception e){

                }
                return;
            }
            requestGlobalSearch(s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            mApplication.mFileInfoManager.getSearchFileList().clear();
            isSearching = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }
    };

    protected void requestGlobalSearch(String query) {
        if (mGlobalSearchView != null && !PermissionUtil.isAllowPermission(this)) {
            List<String> mStorageList = new ArrayList<String>();
            mStorageList.add(mMountPointManager.getPhonePath());
            mStorageList.add(mMountPointManager.getSDCardPath());
            mStorageList.add(mMountPointManager.getUsbOtgPath());
            CommonUtils.getBaseTaskInfo(mApplication, CommonIdentity.FILE_STATUS_GLOBALSEARCH, this.getResources().getString(R.string.search).toString(),
                    CommonIdentity.SEARCH_INFO_TASK, -1, query, CommonIdentity.FILE_STATUS_GLOBALSEARCH, null, null, mStorageList);
        }
    }

    protected TextView.OnEditorActionListener lEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (mGlobalSearchView != null) {
                    CommonUtils.hideSoftInput(FileBaseActivity.this);
                    mGlobalSearchView.clearFocus();
                }
            }
            return false;
        }
    };

    protected void setActionBarBackgroundDrawable(Drawable d) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(d);
        }
    }

    @SuppressLint("InlinedApi")
    protected void changeStatusBarColor(boolean flag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            if (CommonUtils.isMemory512(mApplication)) {
                window.setStatusBarColor(Color.BLACK);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                if (flag) {
                    window.setStatusBarColor(Color.BLACK);
                } else {
                    window.setStatusBarColor(getResources().getColor(R.color.filemanager_theme_color_dark));
                }
            }
        }
    }

    protected void commitFragment(Fragment mFragemnt, boolean isShowPadding, int NavTag, boolean isShortcut) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        filePathBrower.setVisibility(NavTag);
        fragmentTransaction.replace(R.id.layout_content, mFragemnt);
        fragmentTransaction.commitAllowingStateLoss();
        mCurrentFragment = mFragemnt;
        if (mCurrentFragment == mCategoryFragment && mActivitytoCategoryListener != null) {
            mActivitytoCategoryListener.onChangeMainlayout();
        }
    }


    protected void searchStatusChanageScreen() {
        if(mSearchView != null && mSearchView.hasFocus()) {
            mSearchView.onActionViewExpanded();
            mSearchView.setIconifiedByDefault(true);
        }
        setActionBarDisplayHomeAsUpEnabled(false);
        isSearchMode = true;
        mQueryText = mSaveQueryText;
        mSaveQueryText = "";
        if (mQueryText == null || mQueryText.equals("")) {
            if (searchcloseBtn != null) {
                searchcloseBtn.setVisibility(View.GONE);
            }
            return;
        }
        mSearchView.setQuery(mQueryText, false);
        requestSearch(mQueryText);
        return;
    }

    protected void editStatusChanageScreen() {
        if (mActivityListener != null && mSaveCheckedList != null && mSaveCheckedList.size() > 0) {
            mActivityListener.restoreCheckedList(mSaveCheckedList);
        }
        mSaveCheckedList.clear();
        mActivityListener.updateEditBarState();
        changeStatusBarColor(true);
        if(!mApplication.isInMultiWindowMode){
            if(mApplication.mPortraitOrientation && mListFragment.menuMultipleActions != null){
                mListFragment.menuMultipleActions.setVisibility(View.VISIBLE);
            }else if(!mApplication.mPortraitOrientation && mListFragment.menuMultipleActionsLeft != null){
                mListFragment.menuMultipleActionsLeft.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void normalStatusChanageScreen() {
        mGlobalView.setVisibility(View.GONE);
        if(mCurrentFragment!=mCategoryFragment) {
            mBtnSearch.setVisibility(View.VISIBLE);
        }
        if (mActivityListener != null && mSaveSelectedList.size() > 0) {
            mActivityListener.restoreCheckedList(mSaveSelectedList);
        }
        mSaveSelectedList.clear();
        if (mApplication.mFileInfoManager != null && mSavePastedList != null && mSavePastedList.size() > 0) {
            mApplication.mFileInfoManager.savePasteList(-1, mPasteOperation, mSavePastedList);
        }
        mPasteOperation = CommonIdentity.FILE_FILTER_TYPE_UNKOWN;
        mSavePastedList.clear();
    }

    protected void requestSearch(String query) {
        mSearchPath = mApplication.mCurrentPath;
        if (mSearchView != null && query != null) {
            if (mApplication.mFileInfoManager != null)
                if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                    List<FileInfo> mCategoryList = null;
                    if (mApplication.mCache.hasCachedPath(String.valueOf(CategoryManager.mCurrentCagegory))) {
                        mCategoryList = mApplication.mCache.get(String.valueOf(CategoryManager.mCurrentCagegory));
                    } else {
                        mCategoryList = mApplication.mFileInfoManager.getCategoryFileList();
                    }
                    CommonUtils.getBaseTaskInfo(mApplication, CommonIdentity.FILE_STATUS_CATEGORY_SEARCH, this.getResources().getString(R.string.search).toString(),
                            CommonIdentity.SEARCH_INFO_TASK, CategoryManager.mCurrentCagegory, query, CommonIdentity.FILE_STATUS_CATEGORY_SEARCH, mCategoryList, null, null);
                } else {
                    CommonUtils.getBaseTaskInfo(mApplication, CommonIdentity.FILE_STATUS_SEARCH, this.getResources().getString(R.string.search).toString(),
                            CommonIdentity.SEARCH_INFO_TASK, -1, query, CommonIdentity.FILE_STATUS_SEARCH, null, mSearchPath, null);
                }
        }
    }

    protected void requestSearch(String query, boolean isNoExpireLimited) {
        mSearchPath = mApplication.mCurrentPath;
        if (mSearchView != null && query != null) {
            if (mApplication.mFileInfoManager != null)
                if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
                    List<FileInfo> mCategoryList = null;
                    if (mApplication.mCache.hasCachedPath(String.valueOf(CategoryManager.mCurrentCagegory))) {
                        mCategoryList = mApplication.mCache.get(String.valueOf(CategoryManager.mCurrentCagegory));
                    } else {
                        mCategoryList = mApplication.mFileInfoManager.getCategoryFileList();
                    }
                    CommonUtils.getBaseTaskInfo(mApplication, CommonIdentity.FILE_STATUS_CATEGORY_SEARCH, this.getResources().getString(R.string.search).toString(),
                            CommonIdentity.SEARCH_INFO_TASK, CategoryManager.mCurrentCagegory, query, CommonIdentity.FILE_STATUS_CATEGORY_SEARCH, mCategoryList, null, null, isNoExpireLimited);
                }
        }
    }


    protected void showChoiceResourceDialog(int type, View mView) {
        //String title = null;
        int offX = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_xoff);
        int offY = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_yoff);
        int sortpopwidth = getResources().getDimensionPixelSize(R.dimen.sort_menu_width);
        int sortPopheight = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_height);
        int sortPopXoff = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_xoff);
        int sortPopYoff = getResources().getDimensionPixelSize(R.dimen.sort_menu_pop_yoff);
        int choiceItem = 0;
        LayoutInflater inflater;
        ListView sortList;
        LinearLayout sortmenu;

        TextView sortTitle;
        List<String> textAdapter;
        textAdapter = new ArrayList<String>();
        textAdapter.add(getString(R.string.sort_by_name));

        textAdapter.add(getString(R.string.sort_by_time));
        textAdapter.add(getString(R.string.sort_by_size));
        textAdapter.add(getString(R.string.sort_by_type));

        choiceItem = SharedPreferenceUtils.getPrefsSortBy(mApplication);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sortmenu = (LinearLayout) inflater.inflate(R.layout.sort_menu, null);
        sortList = (ListView) sortmenu.findViewById(R.id.sort_list);
        sortList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        SimPickerAdapter simAdapter = new SimPickerAdapter(this, textAdapter, choiceItem);
        simAdapter.setSingleChoice(true);
        simAdapter.setSingleChoiceIndex(choiceItem);
        sortList.setAdapter(simAdapter);
        sortPop = new CustomPopupWindowBasedAnchor(
                sortmenu, sortpopwidth, LayoutParams.WRAP_CONTENT, FileBaseActivity.this);
        if (mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION) {
            sortPop.showAtLocationBasedAnchor(mView, sortPopXoff, sortPopYoff);
        } else {
            sortPop.showAtLocationBasedAnchor(mBtnMore, sortPopXoff, sortPopYoff);
        }

        sortList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                sortPop.dismiss();
                refreshData(position);
            }

        });
    }

    protected void refreshData(int which) {
        if (sortPop != null && sortPop.isShowing()) {
            sortPop.dismiss();
        }
        switch (which) {
            case CommonIdentity.SORT_TYPE:
                updateSortView(FileInfoComparator.SORT_BY_TYPE);
                break;
            case CommonIdentity.SORT_NAME:
                updateSortView(FileInfoComparator.SORT_BY_NAME);
                break;
            case CommonIdentity.SORT_SIZE:
                updateSortView(FileInfoComparator.SORT_BY_SIZE);
                break;
            case CommonIdentity.SORT_TIME:
                updateSortView(FileInfoComparator.SORT_BY_TIME);
                break;
            default:
                break;
        }
    }


    protected void updateSortView(int sort) {
        SharedPreferenceUtils.changePrefsSortBy(mApplication, sort);
        mApplication.mFileInfoManager.sort(mApplication.mSortType);
        if (mActivityListener != null) {
            mActivityListener.refreshAdapter("", CommonIdentity.SORT_UPDATE_ADAPTER_NOTIFICATION, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE, CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
        }
    }

    protected void setFocusOnSearchView() {
        mSearchView.setIconified(false);
    }

    protected void setSearchMode(boolean flag) {
        if (mSearchView == null) {
            return;
        }
        final CharSequence queryText = mSearchView.getQuery();
        mSearchView.onActionViewExpanded();
        isSearchMode = flag;
        if (mSaveQueryText != null && !mSaveQueryText.equals("")) {
            mSearchView.setQuery(mSaveQueryText, false);
            mQueryText = mSaveQueryText;
            mSaveQueryText = "";
        } else if (!TextUtils.isEmpty(queryText) && CommonUtils.isSearchStatus(mApplication)) {
            mSearchView.setQuery(queryText, false);
        }
        if (flag) {
            setFocusOnSearchView();
            if (mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION) {
                requestSearch(mQueryText);
            }
        } else {
            if (CommonUtils.isSearchStatus(mApplication)) {
                setFocusOnSearchView();
            } else {
                mSearchView.setQuery("", false);
            }
        }
        if (mSearchBack != null) {
            setActionBarDisplayHomeAsUpEnabled(!flag);
            mSearchBack.setVisibility(flag ? View.VISIBLE : View.GONE);
        }
    }

    protected boolean queryTextChange(String queryString) {
        if (TextUtils.isEmpty(mQueryText) && TextUtils.isEmpty(queryString)) {
            return false;
        }

        if (!TextUtils.isEmpty(mQueryText)
                && !TextUtils.isEmpty(queryString)
                && TextUtils.equals(mQueryText.toLowerCase(),
                queryString.toLowerCase())) {
            return false;
        }
        String regx = ".*[/\\\\:*?\"<>|].*";
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(queryString);
        if (m.find()) {
            mToastHelper.showToast(R.string.invalid_char_prompt);
            return false;
        }

        mQueryText = queryString;

        if (TextUtils.isEmpty(queryString)) {
            if (isSearchMode) {
                if (mActivityListener != null) {
                    if(SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE && mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION
                            && SafeManager.mCurrentSafeCategory == CommonIdentity.SAFE_CATEGORY_PRIVATE){
                        mActivityListener.refreshAdapter("", CommonIdentity.SAFE_CATEGORY_PRIVATE, CommonIdentity.REFRESH_SAFE_CATEGORY_MODE, CommonIdentity.FILE_SAFEBOX_LOCATION, false,false);
                    } else {
                        mActivityListener.refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonUtils.getRefreshMode(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory),
                                CommonIdentity.FILE_MANAGER_LOCATIONE, false,false);
                    }
                    mActivityListener.showBeforeSearchList();
                    mActivityListener.showNoSearchResults(false, null);
                }
            }
            return false;
        }
        if (mActivityListener != null&&CommonUtils.isSearchStatus(mApplication)) {
            mActivityListener.clearAdapter();
        }
        requestSearch(queryString);
        return true;
    }

    protected void queryTextSubmit(String query) {
        if (mSearchView != null) {
            InputMethodManager imm = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                imm.focusOut(mSearchView);
            }
            mSearchView.clearFocus();
        }
    }

    protected void changePrivateEditMode() {
        if (mListFragment == null) {
            mListFragment = new ListsFragment();
        }
        mActivityListener = (IActivityListener) mListFragment;
        mApplication.mCurrentStatus = CommonIdentity.FILE_STATUS_EDIT;

        if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE) {
            SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_SAFE_VIEW_MODE;
            SafeManager.mCurrentSafeCategory = mSaveSafeCategory;
            mSaveSafeCategory = -1;
            changeStatusBarColor(true);

        } else if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
            CategoryManager.mCurrentCagegory = mSaveCategory;
            mSaveCategory = -1;
            changeStatusBarColor(true);
        }
    }

    protected void updatePrivateEditMode() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mActivityListener != null && mSaveCheckedList != null && mSaveCheckedList.size() > 0) {
                    mActivityListener.restoreCheckedList(mSaveCheckedList);
                }
                mSaveCheckedList.clear();
                mActivityListener.updateEditBarState();
                if (SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_SAFE_VIEW_MODE) {
                    mActivityListener.refreshAdapter(mApplication.mCurrentPath, SafeManager.mCurrentSafeCategory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                            CommonIdentity.FILE_SAFEBOX_LOCATION, false, false);
                } else {
                    mActivityListener.refreshAdapter(mApplication.mCurrentPath, CategoryManager.mCurrentCagegory, CommonIdentity.REFRESH_FILE_NOTIFICATION_MODE,
                            CommonIdentity.FILE_SAFEBOX_LOCATION, false, false);
                }
            }
        }, 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBaseHandler.sendEmptyMessage(CommonIdentity.MSG_HAWKEYE_RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private final StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if(vol == null)
                return;
            DiskInfo mDisk = vol.getDisk();
            if(mDisk == null){
                return;
            }
            if(newState == VolumeInfo.STATE_UNMOUNTED){
                if(mDisk.isUsb()) {
                    onUnmounted(mMountPointManager.getUsbOtgPath());
                }
                if(mDisk.isSd() || mDisk.isUsb()){
                    removeCategoryCache(true, true);
                    if(mCurrentFragment == mCategoryFragment && mActivitytoCategoryListener != null){
                        mActivitytoCategoryListener.onScannerFinished();
                    }
                    if(mApplication != null) {
                        mApplication.isBuiltInStorage = CommonUtils.isPhoneStorageZero();
                    }
                }
            }else if(newState == VolumeInfo.STATE_MOUNTED){
                if(mDisk.isUsb()) {
                    onMounted();
                }
                if((mDisk.isSd() || mDisk.isUsb()) && mCurrentFragment == mCategoryFragment && mActivitytoCategoryListener != null){
                    mActivitytoCategoryListener.onScannerFinished();
                    if(mApplication != null) {
                        mApplication.isBuiltInStorage = CommonUtils.isPhoneStorageZero();
                    }
                }
            }else if(newState == VolumeInfo.STATE_EJECTING){
                if(mDisk.isUsb()) {
                    onEject();
                }
            }

        }
        /// M: ALPS02316229 refresh UI when plug in or out SD card. @{
        @Override
        public void onDiskScanned(DiskInfo disk, int volumeCount) {

        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {

        }

    };

    protected void reshowMorePop(final View mView) {
        if (morePop.isShowing()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    morePop.dismiss();
                    int offX = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_xoff);
                    int offY = getResources().getDimensionPixelSize(R.dimen.more_menu_pop_yoff);
                    morePop.update(initMorePopWindow(), FileBaseActivity.this);
                    morePop.showAtLocationBasedAnchor(mView, offX, offY);
                }
            }, 200);
        }
    }

    public Animation setFloatBtnShowAnim(){
        if(floatBtnShowAnim == null){
            floatBtnShowAnim = AnimationUtils.loadAnimation(this, R.anim.float_btn_show);
        }
        return floatBtnShowAnim;
    }

    public Animation setFloatBtnHideAnim(){
        if(floatBtnHideAnim == null) {
            floatBtnHideAnim = AnimationUtils.loadAnimation(this, R.anim.float_btn_hide);
        }
        return floatBtnHideAnim;
    }
    protected void refreshNavBar(){
        if (mActivityListener != null&&!CommonUtils.isCategoryMode()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (pathBarAdapter != null) {
                        pathBarAdapter.notifyAndScroll(fileBrowerList);
                    }
                }
            }, 50);
        }
    }

    protected void showNoAvailableStorageDialog(){
        /**
         * When a user will pull out of built-in SD/is built-in process, and mobile phone no insert OTG, will pop up the dialog prompt.
         */
        if(CommonUtils.isPhoneStorageZero() && !mMountPointManager.isSDCardMounted() && !mMountPointManager.isOtgMounted()
                && !PermissionUtil.isAllowPermission(this)){
            mNoStorageDialog = CommonDialogFragment.getInstance(getFragmentManager(),
                    getString(R.string.no_available_storage_dialog_content), CommonIdentity.NO_AVAILABLE_STORAGE_DIALOG_TAG);
            mNoStorageDialog.showDialog();
        }
    }

    //add animation for actionbar
    protected void applyRotation(ImageView imageView) {
        if (imageView.getVisibility() == View.VISIBLE) {
            return;
        }
        animatorSet = new AnimatorSet();
        scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 0f, 0.5f);
        scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 0f, 0.5f);
        alpha = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 0.5f);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.play(scaleX).with(scaleY).with(alpha);

//        final float centerX = getResources().getDimension(R.dimen.actionbar_img_size) / 2.0f;
//        final float centerY = getResources().getDimension(R.dimen.actionbar_img_size) / 2.0f;

//        rotationAnim = new Rotate3dAnimation(270, 360,
//                centerX, centerY, 360.0f, true);
//        rotationAnim.setDuration(200);
//        rotationAnim.setInterpolator(new AccelerateInterpolator());
//        rotationAnim.setAnimationListener(new DisplayNextView(imageView));
//        imageView.startAnimation(rotationAnim);
        animatorSet.start();
    }

    private final class DisplayNextView implements Animation.AnimationListener {

        private ImageView imageView;

        public DisplayNextView(ImageView imageView) {
            this.imageView = imageView;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            imageView.post(new SwapViews(imageView));
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private final class SwapViews implements Runnable {
        private ImageView imageView;

        public SwapViews(ImageView imageView) {
            this.imageView = imageView;
        }

        public void run() {

            animatorSet = new AnimatorSet();
            scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.5f, 1f);
            scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.5f, 1f);
            alpha = ObjectAnimator.ofFloat(imageView, "alpha", 0.5f, 1f);
            animatorSet.setDuration(350);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.play(scaleX).with(scaleY).with(alpha);
            animatorSet.start();

//            final float centerX = getResources().getDimension(R.dimen.actionbar_img_size) / 2.0f;
//            final float centerY = getResources().getDimension(R.dimen.actionbar_img_size) / 2.0f;
//            imageView.requestFocus();
//            rotationAnim = new Rotate3dAnimation(270, 360, centerX, centerY, 360.0f,
//                    false);
//            rotationAnim.setDuration(350);
//            rotationAnim.setInterpolator(new DecelerateInterpolator());
//            imageView.startAnimation(rotationAnim);
        }
    }
}
