package com.jrdcom.filemanager.manager;

import android.media.MediaScannerConnection.OnScanCompletedListener;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiMediaStoreHelper {
    protected final List<String> mPathList = new ArrayList<String>();

    protected final MediaStoreHelper mMediaStoreHelper;

    public MultiMediaStoreHelper(MediaStoreHelper mediaStoreHelper) {
        if (mediaStoreHelper == null) {
            throw new IllegalArgumentException("mediaStoreHelper has not been initialized.");
        }
        mMediaStoreHelper = mediaStoreHelper;
    }

    public void addRecord(String path) {
        mPathList.add(path);

    }

    public void updateRecords() {
        mPathList.clear();
    }


    /**
     * Set dstfolder to scan with folder.
     *
     * @param dstFolder
     */
    public void setDstFolder(String dstFolder) {
        mMediaStoreHelper.setDstFolder(dstFolder);
    }

    public void updateRecords(OnScanCompletedListener listener) {
        mPathList.clear();
    }

    public static class CopyMediaStoreHelper extends MultiMediaStoreHelper {
        public CopyMediaStoreHelper(MediaStoreHelper mediaStoreHelper) {
            super(mediaStoreHelper);
        }

        @Override
        public void updateRecords() {
            mMediaStoreHelper.scanPathforMediaStore(mPathList);
            super.updateRecords();
        }

        @Override
        public void updateRecords(OnScanCompletedListener listener) {
            mMediaStoreHelper.scanPathforMediaStore(mPathList, listener);
            super.updateRecords(listener);
        }
    }

    public static class DeleteMediaStoreHelper extends MultiMediaStoreHelper {
        public DeleteMediaStoreHelper(MediaStoreHelper mediaStoreHelper) {
            super(mediaStoreHelper);
        }

        @Override
        public void updateRecords() {
            mMediaStoreHelper.deleteFileInMediaStore(mPathList);
            super.updateRecords();
        }
    }

}
