package com.jrdcom.filemanager.task;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.CategoryManager;

import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.PrivateHelper;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListFileTask extends BaseAsyncTask {

    private final String mPath;
    private final int mFilterType;
    private final boolean mOnlyShowDir;
    private String mFileCategory = "all";
    public static final int LEVEL_NONE = -2;
    public static final int LEVEL_FL = 1;
    public static final int LEVEL_SD = 2;
    public static final int LEVEL_ALL = 4;
    private int mDrmLevel = LEVEL_ALL;
    private int mListMode;
    private TaskInfo mBaseTaskInfo;
    private int mCategory;
    private PrivateHelper mPrivateHelper;
    private SQLiteDatabase db;
    private String dbPath;
    private Context mContext;
    private int dirprogress = 0;
    private BaseAsyncTask mTask;
    private MountManager mountManager;
    private boolean isThirdAppSelect = false;
    private int mRefreshMode = 0;
    private boolean isSupportPrivateMode = false;

    /**
     * Constructor for ListFileTask, construct a ListFileTask with certain
     * parameters
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *                        information of files in FileManager.
     * @param operationEvent  a instance of OperationEventListener, which is a
     *                        interface doing things before/in/after the task.
     * @param path            ListView will list files included in this path.
     * @param filterType      to determine which files will be listed.
     */
    public ListFileTask(TaskInfo mTaskInfo) {
        super(mTaskInfo);
        mBaseTaskInfo = mTaskInfo;
        mContext = mBaseTaskInfo.getApplication();
        mPath = mTaskInfo.getDestPath();
        mFilterType = mTaskInfo.getFileFilter();
        mOnlyShowDir = mTaskInfo.isShowDir();
        String fileCategory = mTaskInfo.getSrcPath();
        if (fileCategory != null && !fileCategory.equals("*/*")) {
            mFileCategory = fileCategory;
        }

        mDrmLevel = mTaskInfo.getDrmType();
        mCategory = mTaskInfo.getCategoryIndex();
        mListMode = mTaskInfo.getAdapterMode();
        mountManager = MountManager.getInstance();
        isThirdAppSelect = (mListMode == CommonIdentity.FILE_STATUS_FOLDER_SELECT || mListMode == CommonIdentity.FILE_STATUS_SELECT);
    }

    @Override
    protected TaskInfo doInBackground(Void... object) {
        mBaseTaskInfo.setTask(getTask());
        mBaseTaskInfo.setBaseTaskHashcode(getTask().hashCode());
        mStartOperationTime = System.currentTimeMillis();
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        mRefreshMode = mBaseTaskInfo.getRefreshMode();
        if (mRefreshMode == CommonIdentity.REFRESH_FILE_PATH_MODE) {
            if (isThirdAppSelect && mOnlyShowDir) {
                refreshOnlyShowDirPath();
            } else if (isThirdAppSelect && mFileCategory != null) {
                refreshSelectPath();
            } else {
                if (!CommonUtils.isExternalStorage(mPath,mMountManager,mApplication.isBuiltInStorage) && CommonUtils.isInPrivacyMode(mApplication)) {
                    isSupportPrivateMode = true;
                }
                ret = refreshPath();
            }
        } else if (mRefreshMode == CommonIdentity.REFRESH_FILE_CATEGORY_MODE) {
            if (CommonUtils.isInPrivacyMode(mApplication) && !CommonUtils.isAddPrivateFileMode(mApplication)) {
                isSupportPrivateMode = true;
            }
            ret = refreshCategory();
        } else if (mRefreshMode == CommonIdentity.REFRESH_SAFE_CATEGORY_MODE ||
                mRefreshMode == CommonIdentity.REFRESH_PRIVATE_CATEGORY_MODE) {
            ret = refreshSafeCategory();
        }
        mBaseTaskInfo.setResultCode(ret);
        mBaseTaskInfo.getListener().onTaskResult(mBaseTaskInfo);
        return mBaseTaskInfo;
    }

    public int refreshOnlyShowDirPath() {

        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        File[] files = null;
//        int progress = 0;
        int total = 0;
        int mTaskType = mBaseTaskInfo.getBaseTaskType();
        long mTaskCreateTime = mBaseTaskInfo.getCreateTaskTime();
        if (TextUtils.isEmpty(mPath)) {
            ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
            return ret;
        }
        publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, -1, -1));

        File dir = new File(mPath);
        final String originPath = dir.getAbsolutePath();
        String internalPath = null;
        if (dir.exists()) {
            File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
            internalPath = dir0.getAbsolutePath();
            files = dir0.listFiles();
            if (files == null || files.length == 0) {
                files = dir.listFiles();
            }
        }
        List<FileInfo> addList = new ArrayList<FileInfo>();
        if (files != null) {
            total = files.length;
            Map<String, String> mimeMap = FileUtils.getMimeTypeMap(mContext, mPath);

            for (int i = 0; i < total; i++) {
//                if (progress < total) {
//                    progress++;
//                }
                //publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, progress, total));
                if (isCancelled()) {
                    ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                    return ret;
                }
                String mAbsolutePath = files[i].getAbsolutePath();
                String name = files[i].getName();
                String mFileMime = mimeMap.get(mAbsolutePath);
                if (isThirdAppSelect && name != null && name.startsWith(".")) {
                    continue;
                }
                if (TextUtils.isEmpty(mFileMime)) {
                    mFileMime = FileUtils.getMimeTypeByExt(files[i].getAbsolutePath());
                    if (TextUtils.isEmpty(mFileMime)) {
                        mFileMime = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
                    }
                }
                if (isThirdAppSelect && name != null && name.startsWith(".")) {
                    continue;
                }
                boolean directory = files[i].isDirectory();
                boolean hide = files[i].isHidden();

                if (directory) {
                    addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, hide, false));
                    continue;
                }
            }
        }

        mFileInfoManager.removeAllItem();
        mFileInfoManager.clearHideItem();
        mFileInfoManager.addAllItem(addList);

        return ret;


    }

    public int refreshSelectPath() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        File[] files = null;
//        int progress = 0;
        int total = 0;
        int mTaskType = mBaseTaskInfo.getBaseTaskType();
        long mTaskCreateTime = mBaseTaskInfo.getCreateTaskTime();
        if (TextUtils.isEmpty(mPath)) {
            ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
            return ret;
        }
        publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, -1, -1));

        File dir = new File(mPath);
        final String originPath = dir.getAbsolutePath();
        String internalPath = null;
        if (dir.exists()) {
            File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
            internalPath = dir0.getAbsolutePath();
            files = dir0.listFiles();
            if (files == null || files.length == 0) {
                files = dir.listFiles();
            }
        }

        List<FileInfo> addList = new ArrayList<FileInfo>();
//        boolean isAll = mDrmLevel != LEVEL_FL && mDrmLevel != LEVEL_SD;
        boolean isImageAll = mFileCategory.startsWith("image/*");
        boolean isAudioAll = mFileCategory.startsWith("audio/*");
        boolean isVideoAll = mFileCategory.startsWith("video/*");
        if (files != null) {
            total = files.length;
            Map<String, String> mimeMap = FileUtils.getMimeTypeMap(mContext, mPath);
            for (int i = 0; i < total; i++) {
//                if (progress < total) {
//                    progress++;
//                }
                //publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, progress, total));
                if (isCancelled()) {
                    ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                    return ret;
                }
                String name = files[i].getName();
                boolean directory = files[i].isDirectory();
                String mFileMime = mimeMap.get(files[i].getAbsolutePath());

                if (TextUtils.isEmpty(mFileMime) && !directory) {
                    mFileMime = FileUtils.getSelectMimeTypeByExt(files[i].getAbsolutePath());
                    if (TextUtils.isEmpty(mFileMime)) {
                        mFileMime = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
                    }
                }
                if (isThirdAppSelect && name != null && name.startsWith(".")) {
                    continue;
                }
                if (!"all".equals(mFileCategory) && !directory) {
                    String extensionName = FileUtils.getFileExtension(name);
                    //String mimeType = FileUtils.getMimeTypeByExtension(extensionName);
                    if (extensionName != null && !"".equals(extensionName)) {
                        if (mFileCategory.equals("audio/*") && (extensionName.equals("3gp") || extensionName.equals("ogg") || extensionName.equals("mp3") || extensionName.equals("mp4")) &&
                                mFileMime != null && mFileMime.startsWith("audio/")) {
                            addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                            continue;
                        } else if (mFileCategory.equals("video/*") && (extensionName.equals("3gp") || extensionName.equals("ogg") || extensionName.equals("mp3") || extensionName.equals("mp4")) &&
                                mFileMime != null && mFileMime.startsWith("video/")) {
                            addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                            continue;
                        }
                    }
                    try {
                        String path = files[i].getAbsolutePath();
                        String drmMimetype = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                        int schema = DrmManager.getInstance(mContext.getApplicationContext()).getDrmScheme(path);
                        if (!TextUtils.isEmpty(drmMimetype)) {
                            if (((mDrmLevel == LEVEL_SD && schema == DrmManager.METHOD_SD)
                                    || (mDrmLevel == LEVEL_FL  && schema == DrmManager.METHOD_FL)
                                    || mDrmLevel == LEVEL_ALL)
                                    && ((mFileCategory.startsWith("audio/") && drmMimetype.startsWith("audio/"))
                                    || (mFileCategory.startsWith("video/") && drmMimetype.startsWith("video/"))
                                    || (mFileCategory.startsWith("image/") && drmMimetype.startsWith("image/")))) {
                                addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, drmMimetype, false, true));
                            }
                            continue;
                        } else if(mFileMime != null && isImageAll && mFileMime.startsWith("image/")){
                            addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                            continue;
                        } else if(mFileMime != null && isAudioAll && mFileMime.startsWith("audio/")){
                            addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                            continue;
                        } else if(mFileMime != null && isVideoAll && mFileMime.startsWith("video/")){
                            addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                            continue;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                } else if ("all".equals(mFileCategory)) {
                    addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                } else if (directory) {
                    addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, false, false));
                }
            }
        }

        mFileInfoManager.removeAllItem();
        mFileInfoManager.clearHideItem();
        mFileInfoManager.addAllItem(addList);

        return ret;

    }

    private int refreshPath() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        File[] files = null;
        //int progress = 0;
        int total = 0;
        int mTaskType = mBaseTaskInfo.getBaseTaskType();
        long mTaskCreateTime = mBaseTaskInfo.getCreateTaskTime();
        if (TextUtils.isEmpty(mPath)) {
            ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
            return ret;
        }
        publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, -1, -1));

        File dir = new File(mPath);
        final String originPath = dir.getAbsolutePath();
        String internalPath = null;
        if (dir.exists()) {
            File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
            internalPath = dir0.getAbsolutePath();
            files = dir0.listFiles();
            if (files == null || files.length == 0) {
                files = dir.listFiles();
            }
        }

        List<FileInfo> addList = new ArrayList<FileInfo>();
        if (files != null) {
            total = files.length;
            Map<String, String> mimeMap = FileUtils.getMimeTypeMap(mContext, mPath);

            for (int i = 0; i < total; i++) {
//                if (progress < total) {
//                    progress++;
//                }
                //publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, progress, total));
                if (isCancelled()) {
                    ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
                    return ret;
                }
                String mAbsolutePath = files[i].getAbsolutePath();
                String name = files[i].getName();
                String mFileMime = mimeMap.get(mAbsolutePath);
                if (TextUtils.isEmpty(mFileMime)) {
                    mFileMime=null;
//                    mFileMime = FileUtils.getMimeTypeByExt(files[i].getAbsolutePath());
//                    if (TextUtils.isEmpty(mFileMime)) {
//                        mFileMime = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
//                    }
                }
                boolean directory = files[i].isDirectory();
                boolean hide = files[i].isHidden();
                if (!directory && isSupportPrivateMode) {
                    boolean isPrivate = PrivateModeManager.isPrivateFile(mPrivateModeHelper, mAbsolutePath);
                    addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, hide, false, isPrivate));
                    continue;
                }
                addList.add(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, hide, false));
            }

        }

        mFileInfoManager.removeAllItem();
        mFileInfoManager.clearHideItem();
        mFileInfoManager.addAllItem(addList);

        return ret;


    }

    private int refreshCategory() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        int progress = 0;
        int total = 0;
//        int mTaskType = mBaseTaskInfo.getBaseTaskType();
//        long mTaskCreateTime = mBaseTaskInfo.getCreateTaskTime();
        if (CommonIdentity.CATEGORY_DOWNLOAD == mCategory
                || CommonIdentity.CATEGORY_BLUETOOTH == mCategory) {
            String path1 = mountManager.getPhonePath();
            String path2 = mountManager.getSDCardPath();
            total = CategoryManager.getCountFromFiles(mContext, mCategory);
            if (path1 != null) {
                dirprogress = 0;
                path1 = CategoryManager.getCategoryPath(path1, mCategory);
                ret = categoryFromFolder(path1, total, progress);
            } else {
                dirprogress = 0;
            }
            if (path2 != null) {
                path2 = CategoryManager.getCategoryPath(path2, mCategory);
                ret = categoryFromFolder(path2, total, progress);
            }
        } else {
            Uri uri = MediaStore.Files.getContentUri("external");
            String[] projection = null;
            if (mApplication.isSysteSupportDrm && isSupportPrivateMode) {
                projection = new String[]{MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.DATE_MODIFIED, "is_drm", CommonIdentity.TCT_IS_PRIVATE};
            } else if (mApplication.isSysteSupportDrm) {
                projection = new String[]{MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.DATE_MODIFIED, "is_drm"};
            } else if (isSupportPrivateMode) {
                projection = new String[]{MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.DATE_MODIFIED, CommonIdentity.TCT_IS_PRIVATE};
            } else {
                projection = new String[]{MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.DATE_MODIFIED};
            }
            if (CommonIdentity.CATEGORY_PICTURES == mCategory) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (CommonIdentity.CATEGORY_VEDIOS == mCategory) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (CommonIdentity.CATEGORY_MUSIC == mCategory) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            StringBuilder sb = new StringBuilder();

            sb.append(MediaStore.Files.FileColumns.DATA + " not like ");
            DatabaseUtils.appendEscapedSQLString(sb, "null");

            if (CommonUtils.isTctPrivateColumn(mContext) && (!CommonUtils.isInPrivacyMode(mContext) || CommonUtils.isAddPrivateFileMode(mApplication))) {
                sb.append(" and ");
                sb.append(CommonIdentity.TCT_IS_PRIVATE + " != ");
                DatabaseUtils.appendEscapedSQLString(sb, "1");
                if (CommonUtils.isInPrivacyMode(mContext) && CommonUtils.isAddPrivateFileMode(mApplication)) {
                    sb.append(" and ");
                    sb.append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "/storage/emulated/0/%");
                }

            }

            String selection0 = sb.toString();
            if (CommonIdentity.CATEGORY_DOCS == mCategory) {

                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");

                if (CommonUtils.isInPrivacyMode(mContext) && SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
                    sb.append(" and (").append(
                            MediaStore.Files.FileColumns.MIME_TYPE + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "text/%");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
                    sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
                    sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
                    sb.append(")");
                } else {
                    sb.append(" and (").append(

                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
                    sb.append(" or ").append(
                            MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
                    sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
                    sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                    DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
                    sb.append(")");
                }

            } else if (CommonIdentity.CATEGORY_APKS == mCategory) {
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
                sb.append(" and (").append(
                        MediaStore.Files.FileColumns.MIME_TYPE + " like ");
                DatabaseUtils.appendEscapedSQLString(sb,
                        "application/vnd.android.package-archive");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
                sb.append(")");
            } else if (CommonIdentity.CATEGORY_RECENT == mCategory) {

                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                sb.append(" and ");
                sb.append(MediaStore.Files.FileColumns.FORMAT + "!=");
                DatabaseUtils.appendEscapedSQLString(sb, MtpConstants.FORMAT_ASSOCIATION + "");
                sb.append(" and ").append("bucket_display_name not like ");
                DatabaseUtils.appendEscapedSQLString(sb, ".thumbnails");
                sb.append(" and (").append(
                        MediaStore.Files.FileColumns.MIME_TYPE + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "text/%");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.doc");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.xls");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.ppt");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.docx");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.xlsx");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.xlsm");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.pptx");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.pdf");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.jpg");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.jpeg");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.png");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.bmp");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.mp3");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.wav");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.mp4");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.avi");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.mov");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.apk");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.vcf");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.vcs");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.m4a");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.3gp");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.dcf");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.3gpp");
                sb.append(" or ").append(MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.gif");
                sb.append(") and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " > " + CommonUtils.getYesterdayTime());
                sb.append(" and ").append(MediaStore.Files.FileColumns.DATE_MODIFIED + " < " + CommonUtils.getCurrentTime()); //MODIFIED by jian.xu, 2016-04-08,BUG-1921733

            } else if (CommonIdentity.CATEGORY_ARCHIVES == mCategory) {
                sb.append(" and (").append(
                        MediaStore.Files.FileColumns.MIME_TYPE + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "application/zip");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.rar");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.zip");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.tar");
                sb.append(" or ").append(
                        MediaStore.Files.FileColumns.DATA + " like ");
                DatabaseUtils.appendEscapedSQLString(sb, "%.7z");
                sb.append(")");
            }


            String selection = sb.toString();
            Cursor cursor = null;
            if (CommonIdentity.CATEGORY_PICTURES == mCategory
                    || CommonIdentity.CATEGORY_VEDIOS == mCategory
                    || CommonIdentity.CATEGORY_MUSIC == mCategory) {

                //select * from video where _data not in (select _data from files where  format == 12289) ;
                //avoid to get folders like xx.mp4,xx.mp3, xx.apk
                StringBuilder sb1 = new StringBuilder();
                sb1.append(selection0);
                sb.append(" and (" + MediaStore.Files.FileColumns.DATA + " not in (select " + MediaStore.Files.FileColumns.DATA + " from files where "
                        + MediaStore.Files.FileColumns.FORMAT + "==" + MtpConstants.FORMAT_ASSOCIATION + "))");
                selection0 = sb.toString();

                cursor = mContext.getContentResolver().query(uri, projection, selection0,
                        null, null);
            } else if (CommonIdentity.CATEGORY_DOCS == mCategory
                    || CommonIdentity.CATEGORY_APKS == mCategory
                    || CommonIdentity.CATEGORY_ARCHIVES == mCategory
                    || CommonIdentity.CATEGORY_RECENT == mCategory
                    ) {
                cursor = mContext.getContentResolver().query(uri, projection, selection,
                        null, null);
            }

            if (cursor == null) {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            //total = cursor.getCount();
            boolean firstItem = true;
            try {
                while (cursor.moveToNext()) {
//                    if (progress < total) {
//                        progress++;
//                    }
                    //publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, progress, total));

                    if (mCancelled) {
                        cancel(true);
                        onCancelled();
                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
                    }
                    if (isCancelled()) {
                        ret = OperationEventListener.ERROR_CODE_USER_CANCEL;
                        break;
                    }
                    String name = (String) cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    String mMimeType = (String) cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                    long size = (long) cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                    long lastModifyTime = (long) cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));

                    String parentPath = name.substring(0, name.lastIndexOf("/"));
                    File file = new File(name);
                    FileInfo mFileInfo = new FileInfo(mContext, false, parentPath, name, file);
                    mFileInfo.updateSizeAndLastModifiedTime(size, lastModifyTime * 1000);
                    mFileInfo.setFileMime(mMimeType);
                    mFileInfo.setHideFile(file.isHidden());
                    int isDrm = 0;
                    if (mApplication.isSysteSupportDrm) {
                        isDrm = cursor.getInt(cursor.getColumnIndex("is_drm"));
                        if (isDrm == 1) {
                            mFileInfo.setDrm(true);
                        }
                    }
                    int isPrivate = 0;
                    if (isSupportPrivateMode) {
                        isPrivate = cursor.getInt(cursor.getColumnIndex(CommonIdentity.TCT_IS_PRIVATE));
                        if (isPrivate == 1) {
                            mFileInfo.setPrivateFile(true);
                        }
                    }
                    mFileInfoManager.addItem(mFileInfo, firstItem);
                    firstItem = false;

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ret = OperationEventListener.ERROR_CODE_SUCCESS;
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (ret != OperationEventListener.ERROR_CODE_USER_CANCEL) {
            FileManagerApplication application = (FileManagerApplication) mContext
                    .getApplicationContext();
            if (CommonIdentity.CATEGORY_RECENT != mCategory) {
                application.mFileInfoManager
                        .updateCategoryList(application.mSortType);
            } else {
                application.mFileInfoManager
                        .updateCategoryList(1);
            }
        }

        return ret;
    }

    private int refreshSafeCategory() {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        //int progress = 0;
        //int total = 0;
//        int mTaskType = mBaseTaskInfo.getBaseTaskType();
//        long mTaskCreateTime = mBaseTaskInfo.getCreateTaskTime();

        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = null;
        if (mApplication.isSysteSupportDrm) {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED, "is_drm"};
        } else {
            projection = new String[]{MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_MODIFIED};
        }

        StringBuilder sb = new StringBuilder();
        sb.append(CommonIdentity.TCT_IS_PRIVATE + " = ");
        DatabaseUtils.appendEscapedSQLString(sb, "1");
        String selection = sb.toString();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, projection, selection, null, null);
            if (cursor == null) {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            //total = cursor.getCount();
            boolean firstItem = true;
            while (cursor.moveToNext()) {
//                if (progress < total) {
//                    progress++;
//                }
                //publishProgress(CommonUtils.getProgressInfo("", mTaskType, mTaskCreateTime, progress, total));

                if (mCancelled) {
                    cancel(true);
                    onCancelled();
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (isCancelled()) {
                    ret = OperationEventListener.ERROR_CODE_USER_CANCEL;
                    break;
                }
                String name = (String) cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String mMimeType = (String) cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                int isDrm = 0;
                if (mApplication.isSysteSupportDrm) {
                    isDrm = cursor.getInt(cursor.getColumnIndex("is_drm"));
                }
                long size = (long) cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                long lastModifyTime = (long) cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));

                String parentPath = name.substring(0, name.lastIndexOf("/"));
                File file = new File(name);
                FileInfo mFileInfo = new FileInfo(mContext, false, parentPath, name, file);
                mFileInfo.updateSizeAndLastModifiedTime(size, lastModifyTime * 1000);
                mFileInfo.setFileMime(mMimeType);
                mFileInfo.setHideFile(file.isHidden());

                if (mApplication.isSysteSupportDrm) {
                    if (isDrm == 0) {
                        mFileInfo.setDrm(false);
                    } else if (isDrm == 1) {
                        mFileInfo.setDrm(true);
                    }
                }
                mFileInfoManager.addItem(mFileInfo, firstItem);
                firstItem = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ret = OperationEventListener.ERROR_CODE_SUCCESS;
            if (cursor != null) {
                cursor.close();
            }
        }

        if (ret != OperationEventListener.ERROR_CODE_USER_CANCEL) {
            FileManagerApplication application = (FileManagerApplication) mContext
                    .getApplicationContext();
            application.mFileInfoManager
                    .updateCategoryList(1);
        }


        return ret;

    }


    private int categoryFromFolder(String path, int total, int progress) {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        File dir = new File(path);
        File[] files = null;

        //this can return listFiles very fast.
        final String originPath = dir.getAbsolutePath();
        File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
        String internalPath = dir0.getAbsolutePath();
        if (dir.exists()) {
            files = dir0.listFiles();
            if (files == null || files.length == 0) {
                files = dir.listFiles();
            }
        }
        if (files != null) {
            int len = files.length;
            //Map<String, String> mimeMap = FileUtils.getMimeTypeMap(mApplication, path);
            for (int i = 0; i < len; i++) {
                dirprogress++;
                //publishProgress(CommonUtils.getProgressInfo("", mBaseTaskInfo.getBaseTaskType(), mBaseTaskInfo.getCreateTaskTime(), dirprogress, total));
                if (isCancelled()) {
                    return OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                File mFile = files[i];
                String mFileName = mFile.getName();
                String mAbsolutePath = mFile.getAbsolutePath();
                boolean isDrm = DrmManager.getInstance(mApplication).isDrm(originPath) || DrmManager.isDrmFileExt(mFileName);
                boolean hide = mFileName.startsWith(".");
                String mFileMime = FileUtils.getCategoryMIME(mApplication,null,mAbsolutePath.replace(internalPath, originPath),mFile.isDirectory(),isDrm);
                if (!mFile.isDirectory()) {
                    if(isSupportPrivateMode) {
                        boolean isPrivate = PrivateModeManager.isPrivateFile(mPrivateModeHelper, mAbsolutePath);
                        mFileInfoManager.addItem(FileUtils.createFileInfo(mContext, mFile, internalPath, originPath, mFileMime, hide, false, isPrivate));
                    } else {
                        mFileInfoManager.addItem(FileUtils.createFileInfo(mContext, mFile, internalPath, originPath, mFileMime, hide,isDrm));
                    }
                }
            }

        } else {
            ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
        }

        return ret;
    }

    public int categoryFromPhotoFolder(String path, int total, int progress) {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        File dir = new File(path);
        File[] files;

        //this can return listFiles very fast.
        final String originPath = dir.getAbsolutePath();
        File dir0 = Environment.maybeTranslateEmulatedPathToInternal(dir);
        String internalPath = dir0.getAbsolutePath();

        if (dir.exists()) {
            files = dir0.listFiles();
            if (files == null) {
                ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
            } else {
                int len = files.length;
                for (int i = 0; i < len; i++) {

                    dirprogress++;
                    //publishProgress(CommonUtils.getProgressInfo("", mBaseTaskInfo.getBaseTaskType(), mBaseTaskInfo.getCreateTaskTime(), dirprogress, total));
                    if (isCancelled()) {
                        return OperationEventListener.ERROR_CODE_UNSUCCESS;
                    }
                    String mFileName = files[i].getName();
                    String mFileMime = new FileInfo(mContext, mFileName).getMime();
                    boolean hide = mFileName.startsWith(".");

                    if (!files[i].isDirectory() && (mApplication.isShowHidden || !mFileName.startsWith(".")) && mFileName.endsWith(".jpg")) {
                        mFileInfoManager.addItem(FileUtils.createFileInfo(mContext, files[i], internalPath, originPath, mFileMime, hide, DrmManager.getInstance(mApplication).isDrm(originPath) || DrmManager.isDrmFileExt(mFileName)));
                    }
                }
            }
        } else {
            ret = OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        return ret;
    }


}
