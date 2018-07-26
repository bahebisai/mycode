package com.jrdcom.filemanager.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.SafeManager;

import java.io.File;

public class FileInfo {


    private File mFile;
    private String mParentPath;
    private String mMimeType;
    private String mName;
    private final String mAbsolutePath;
    private String mFileSizeStr;

    private boolean mIsDir;
    private boolean mIsZip;

    private long mLastModifiedTime = -1;
    private long mSize = -1;
    private boolean isDrm;
    private String mDrmType;
    private boolean mDrm_right_type;
    public static boolean mountReceiver;
    public static boolean scanFinishReceiver;
    private boolean isHideFile;
    private int folderCount;
    private FileManagerApplication mApplication = (FileManagerApplication)FileManagerApplication.getInstance();
    private boolean isPrivateFile;


    private Context mContext;

    /**
     * 49
     * only one of	50
     * {@link com.jrdcom.filemanager.utils.CommonIdentity#SAFE_CATEGORY_FILES}
     * {@link com.jrdcom.filemanager.utils.CommonIdentity#SAFE_CATEGORY_MUISC}
     * {@link com.jrdcom.filemanager.utils.CommonIdentity#SAFE_CATEGORY_PICTURES
     * {@link com.jrdcom.filemanager.utils.CommonIdentity#SAFE_CATEGORY_VEDIO}
     */
    private int mFileType;

    /**
     * Constructor of FileInfo, which restore details of a file.
     *
     * @param file the file associate with the instance of FileInfo.
     * @throws IllegalArgumentException when the parameter file is null, will
     *                                  throw the Exception.
     */
    public FileInfo(Context context, File file) {
        mContext = context;
        mFile = file;
        mAbsolutePath = mFile.getAbsolutePath();
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();
        if (!mIsDir) {
            mSize = mFile.length();
        }
    }

    public FileInfo(Context context, File file,boolean isAddCache) {
        mContext = context;
        mFile = file;
        mAbsolutePath = mFile.getAbsolutePath();
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();
        if (!file.isDirectory()) {
            mSize = mFile.length();
        }
    }

    /**
     * Constructor of FileInfo, which restore details of a file.
     *
     * @param absPath the absolute path of a file which associated with the
     *                instance of FileInfo.
     */
    public FileInfo(Context context, String absPath) {

        mContext = context;
        mAbsolutePath = absPath;
        mFile = new File(absPath);
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();

        if (!mIsDir) {
            mSize = mFile.length();
        }
    }

    public FileInfo(Context context, File file, boolean isDir, String parentPath, String absPath,boolean isMDRM) {
        mContext = context;
        mFile = file;
        mAbsolutePath = absPath;
        mIsDir = isDir;
        mParentPath = parentPath;
        isDrm = isMDRM;
        if (!mIsDir) {
            mSize = mFile.length();
        }
    }

    public FileInfo(Context context, boolean isDir, String parentPath, String absPath) {
        mContext = context;
        mAbsolutePath = absPath;
        mIsDir = isDir;
        mParentPath = parentPath;
    }

    public FileInfo(Context context, boolean isDir, String parentPath, String absPath,File file) {
        mFile = file;
        mContext = context;
        mAbsolutePath = absPath;
        mIsDir = isDir;
        mParentPath = parentPath;
    }

    public FileInfo(String name, long size, long LastModifiedTime, String absPath) {
        mName = name;
        mSize = size;
        mLastModifiedTime = LastModifiedTime;
        mAbsolutePath = absPath;
    }

    private void checkFile() {
        if (mFile == null) {
            mFile = new File(mAbsolutePath);
        }
    }


    public void updateSizeAndLastModifiedTime(File file) {
        if (file != null) {
            mIsDir = file.isDirectory();
            mLastModifiedTime = file.lastModified();
            if (!mIsDir) {
                mSize = file.length();
            }
        }
    }

    public void setHideFile(boolean hideFile) {
        isHideFile = hideFile;
    }


    public void updateSizeAndLastModifiedTime(long size, long modifiedTime) {
        mSize = size;
        mLastModifiedTime = modifiedTime;
    }

    public boolean isZip(){
        String name = mFile.getName();
        if(mFile.isDirectory() || name == null || FileUtils.getFileExtension(name) ==null){
            return false;
        }
        String mExtensionName = FileUtils.getFileExtension(name);
        if(mExtensionName == null){
            return false;
        }

        return mExtensionName.equalsIgnoreCase("zip")
                || mExtensionName.equalsIgnoreCase("tar")
                || mExtensionName.equalsIgnoreCase("rar");
    }


    /**
     * This method gets a file's parent path
     *
     * @return file's parent path.
     */
    public String getFileParentPath() {
        if (mParentPath == null) {
            mParentPath = FileUtils.getFilePath(mAbsolutePath);
        }
        return mParentPath;
    }


    /**
     * This method gets a file's description path, which will be shown on the
     * NavigationBar.
     *
     * @return the path's description path.
     */
    private String getShowPath() {
        return MountManager.getInstance().getDescriptionPath(getFileAbsolutePath());
    }

    /**
     * This method gets a file's real name.
     *
     * @return file's name on FileSystem.
     */
    public String getFileName() {
        if (mName == null) {
            mName = FileUtils.getFileName(mAbsolutePath);
        }
        return mName;
    }

    /**
     * This method gets the file's description name.
     *
     * @return file's description name for show.
     */
    public String getShowName() {

       if (mName == null) {
            return FileUtils.getFileName(getShowPath());
        }
        return mName;

    }

    /**
     * This method gets the file's size(including its contains).
     *
     * @return file's size in long format.
     */
    public long getFileSize() {
        return mSize;
    }

    /**
     * This method gets transform the file's size from long to String.
     *
     * @return file's size in String format.
     */
    public String getFileSizeStr() {
        if (mFileSizeStr == null) {
            mFileSizeStr = FileUtils.sizeToString(mContext, mSize);
        }
        return mFileSizeStr;
    }

    /**
     * This method check the file is directory, or not.
     *
     * @return true for directory, false for not directory.
     */
    public boolean isDirectory() {
        return mIsDir;
    }

    public void setFileMime(String mFileMime) {
        mMimeType = mFileMime;
    }

    public String getMime() {
        if (mMimeType == null) {
            mMimeType = FileUtils.getMIME(mContext,mMimeType,mAbsolutePath,isDirectory(),isDrm);
        }
        return mMimeType;
    }

    public String getMimeType() {
        String type="*/*";
        type = FileUtils.getMIMEType(isDrm,mContext,mAbsolutePath,getFileName());
        return type;
    }

    public String getShareMime(){
      return FileUtils.getShareMimeType(mContext,mMimeType,isDirectory(),isDrm,mAbsolutePath);
    }

    public void setFolderCount(int folderCount) {
        this.folderCount = folderCount;
    }

    public int getFolderCount() {
        return folderCount;
    }

    public boolean isDrm() {
        if (mIsDir || TextUtils.isEmpty(mAbsolutePath)) {
            return false;
        }
        if(mApplication.isSysteSupportDrm || isDrm){
            return isDrm;
        }

        isDrm = DrmManager.getInstance(mApplication).isDrm(mAbsolutePath) || DrmManager.isDrmFileExt(mAbsolutePath);

        return isDrm;

    }

    public void setDrm(boolean drm) {
        this.isDrm = drm;
    }

    public String getmDrmType() {
        return mDrmType;
    }

    public void setmDrmType(String mDrmType) {
        this.mDrmType = mDrmType;
    }

    public boolean getmDrm_right_type() {
        return mDrm_right_type;
    }

    public void setmDrm_right_type(boolean mDrm_right_type) {
        this.mDrm_right_type = mDrm_right_type;
    }

    /**
     * The method check the file is DRM file, or not.
     *
     * @return true for DRM file, false for not DRM file.
     */

    public boolean isDrmFile() {
        //isDrm = FileUtils.isDrmFiles(mContext,mAbsolutePath,isDirectory());
        return isDrm;
    }

    /**
     * This method gets last modified time of the file.
     *
     * @return last modified time of the file.
     */
    public long getFileLastModifiedTime() {
        return mLastModifiedTime;
    }


    /**
     * This method gets the file's absolute path.
     *
     * @return the file's absolute path.
     */
    public String getFileAbsolutePath() {
        return mAbsolutePath;
    }

    /**
     * This method gets the file packaged in FileInfo.
     *
     * @return the file packaged in FileInfo.
     */
    public File getFile() {
        checkFile();
        return mFile;
    }

    /**
     * This method gets the file packaged in FileInfo.
     *
     * @return the file packaged in FileInfo.
     */
    public Uri getUri(boolean isFileDrm) {
        checkFile();
        if(CommonUtils.hasBelowN()){
            return Uri.fromFile(mFile);
        } else if(mApplication.isShareMediaURI){
            return FileUtils.getMediaContentUri(mFile,mApplication.mFileInfoManager,getMime());
        }
        return FileUtils.getContentUri(mFile,mApplication.mFileInfoManager,getMime(),isFileDrm);
    }


    public void setFileName(String fileName) {
        mName = fileName;
    }

    @Override
    public int hashCode() {
        return getFileAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        } else {
            if (o instanceof FileInfo) {
                if (((FileInfo) o).getFileAbsolutePath().equals(getFileAbsolutePath())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * This method checks that weather the file is hide file, or not.
     *
     * @return true for hide file, and false for not hide file
     */
    public boolean isHideFile() {
        return isHideFile;
    }

    /**
     * This method check the file is private file, or not.
     *
     * @return true for private file, false for not private file.
     */
    public boolean isPrivateFile() {
        return isPrivateFile;
    }

    public void setPrivateFile(boolean privateFile) {
        isPrivateFile = privateFile;
    }
}
