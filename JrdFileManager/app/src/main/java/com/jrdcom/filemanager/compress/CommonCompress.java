package com.jrdcom.filemanager.compress;

import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.filemanager.utils.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yyang on 8/23/16.
 * This is a parent class for compress files.
 */
public abstract class CommonCompress {

    /**
     * Compress Type.
     */
    public static final int ZIP = 1;
    public static final int TAR = 2;
    public static final int RAR = 3;
//    public static final int SEVENZ = 3;

    // flag to cancel progress.
    protected boolean sInterrupt = false;

    // list to store compress files
    protected ArrayList<CompressOptions> mFileList = new ArrayList<>();

    // dest files to generate
    protected String mDestPath;

    // total size for files.
    protected long mTotalSize;

    // observer for progress process.
    protected CompressObserver mObserver;

    // observer for progress process.
    protected boolean mPrepare = false;

    // abstract method for compress.
    public abstract boolean doArchive();

    public abstract boolean doUnArchive(String srcPath, String mDestPath);

    /**
     * observer for monitoring process.
     */
    public interface CompressObserver {
        void onArchiveComplete(File file, int pos, int total);

        void onUnArchiveComplete(File file, int pos, int total);

    }


    /**
     * set observer.
     */
    public void setObserver(CompressObserver observer) {
        mObserver = observer;
    }

    /**
     * Create type entry.
     *
     * @param type compress type, such as zip, rar, 7z, tar
     * @return return abstract class CommonCompress.
     */
    public static CommonCompress createCompressType(int type) {
        CommonCompress compress = null;

        switch (type) {
            case ZIP:
                compress = new ZipType();
                break;
            case TAR:
                compress = new TarType();
                break;
//            case SEVENZ:
//                break;
            case RAR:
                compress = new RarType();
                break;
        }
        return compress;
    }

    /**
     * @param zipFiles specified files to compress
     * @param dest     dest files to generate
     * @return false if not ok, otherwise ok.
     */
    public boolean prepare(File[] zipFiles, String dest) {

        if (TextUtils.isEmpty(dest) || zipFiles == null) {
            Log.e("FileManager_YY", "CommonCompress->prepare: " + " failed to prepare. Please check.");
            mPrepare = false;
            return false;
        }

        sInterrupt = false;
        mDestPath = dest;
        mTotalSize = 0;

        for (File fileOrDirectory : zipFiles) {
            if (fileOrDirectory.isFile()) {
                mFileList.add(new CompressOptions(fileOrDirectory, fileOrDirectory.getName()));
                mTotalSize += fileOrDirectory.length();
            } else {
                File[] entries = fileOrDirectory.listFiles();
                if (entries != null) {
                    if (entries.length <= 0) {
                        mFileList.add(new CompressOptions(fileOrDirectory, fileOrDirectory.getName()));
                    } else {
                        for (File file : entries) {
                            zipFileOrDirectory(file, fileOrDirectory.getName() + "/");
                        }
                    }
                }
            }
        }

        mPrepare = mFileList.size() != 0;

        return mPrepare;
    }

    /**
     * Special for files info.
     *
     * @param infoList specified files to compress
     * @param dest     dest files to generate
     * @return false if not ok, otherwise ok.
     */
    public boolean prepare(List<FileInfo> infoList, String dest) {

        if (infoList == null || infoList.size() == 0) {
            Log.e("FileManager_YY", "CommonCompress->prepare(filesInfo): " + " No need to compress.");
            return false;
        }

        int total = infoList.size();
        File[] zipFiles = new File[infoList.size()];

        for (int i = 0; i < total; i++) {
            File file = infoList.get(i).getFile();
            if (file == null) {
                return false;
            }
            zipFiles[i] = file;
        }
        return prepare(zipFiles, dest);
    }

    /**
     * scan file by recurse.
     *
     * @param fileOrDirectory file or directory
     * @param curPath         current path
     */
    private void zipFileOrDirectory(File fileOrDirectory, String curPath) {
        if (!fileOrDirectory.isDirectory()) {
            mFileList.add(new CompressOptions(fileOrDirectory, curPath + fileOrDirectory.getName()));
            mTotalSize += fileOrDirectory.length();
        } else {
            // continue recurse
            File[] entries = fileOrDirectory.listFiles();
            if (entries != null) {
                if (entries.length <= 0) {
                    mFileList.add(new CompressOptions(fileOrDirectory, curPath + fileOrDirectory.getName()));
                } else {
                    for (File file : entries) {
                        zipFileOrDirectory(file, curPath + fileOrDirectory.getName() + "/");
                    }
                }
            }
        }
    }


    /**
     * cancel compress process.
     */
    public void cancel() {
        sInterrupt = true;
        Log.e("FileManager_YY", "CommonCompress->cancel: " + " User cancel process.");
    }

    /**
     * return total size for compress files.
     */
    public long getTotalSize() {
        return mTotalSize;
    }

    /**
     * return total size for compress files.
     */
    public int getAllFilesCount() {
        return mFileList.size();
    }

    /**
     * Inner class for compress options.
     */
    protected static class CompressOptions {
        public File zipFile;
        public String relPath;

        public CompressOptions(File file, String path) {
            zipFile = file;
            relPath = path;
        }
    }
}
