package com.jrdcom.filemanager.manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;

import com.jrdcom.filemanager.task.BaseAsyncTask;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import com.jrdcom.filemanager.manager.FavoriteManager;

public final class MediaStoreHelper {

    private static final String TAG = "MediaStoreHelper";
    private final Context mContext;
    private BaseAsyncTask mBaseAsyncTask;

    private String mDstFolder;
    private static final int SCAN_FOLDER_NUM = 20;

    public static final String AUTHORITY = "media";
    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";

    /**
     * Constructor of MediaStoreHelper
     *
     * @param context the Application context
     */
    public MediaStoreHelper(Context context) {
        mContext = context;
    }

    public MediaStoreHelper(Context context, BaseAsyncTask baseAsyncTask) {
        mContext = context;
        mBaseAsyncTask = baseAsyncTask;
    }

    public void updateInMediaStore(String newPath, String oldPath, boolean isFolder) {
        if (mContext != null && !TextUtils.isEmpty(newPath) && !TextUtils.isEmpty(oldPath)) {

            IContentProvider mediaProvider = mContext.getContentResolver().acquireProvider("media");
            Uri uri = Uri.parse(CONTENT_AUTHORITY_SLASH + "external" + "/object");
            uri = uri.buildUpon().appendQueryParameter("need_update_media_values", "true").build();
            String where = FileColumns.DATA + "=?";
            String[] whereArgs = new String[]{
                    oldPath
            };

            ContentValues values = new ContentValues();
            values.put(FileColumns.DATA, newPath);
            if (!isFolder) {
                values.put(FileColumns.DISPLAY_NAME, FileUtils.getFileName(newPath));
            }
            whereArgs = new String[]{oldPath};
            try {
                int i = mediaProvider.update(mContext.getPackageName(), uri, values, where, whereArgs);
                scanPathforMediaStore(newPath);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
                if (mBaseAsyncTask != null) {
                    mBaseAsyncTask.cancel(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void scanMedia(String targetPath) {
        File target = new File(targetPath);
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(target)));
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            for (File file : files) {
                scanMedia(file.getAbsolutePath());
            }
        }
    }


    /**
     * scan Path for new file or folder in MediaStore
     *
     * @param path the scan path
     */
    public void scanPathforMediaStore(String path) {
        if (mContext != null && !TextUtils.isEmpty(path)) {
            String[] paths = {path};
            MediaScannerConnection.scanFile(mContext, paths, null, null);
        }
    }

    public void scanPathforMediaStore(List<String> scanPaths) {
        if (mContext != null && !scanPaths.isEmpty()) {
            String[] paths = new String[scanPaths.size()];
            scanPaths.toArray(paths);
            MediaScannerConnection.scanFile(mContext, paths, null, null);
        }
    }


    public void scanPathforMediaStore(List<String> scanPaths, OnScanCompletedListener listener) {
        if (mContext != null && !scanPaths.isEmpty()) {
            String[] paths = new String[scanPaths.size()];
            scanPaths.toArray(paths);
            MediaScannerConnection.scanFile(mContext, paths, null, listener);
        }
    }

    /**
     * delete the record in MediaStore
     *
     * @param paths the delete file or folder in MediaStore
     */
    public void deleteFileInMediaStore(List<String> paths) {

        Uri uri = MediaStore.Files.getContentUri("external");

        int max = 300;
        for (int i = 0; paths != null && i < Math.ceil(1.0f * paths.size() / max); i++) {
            StringBuilder where = new StringBuilder();
            where.append(FileColumns.DATA);
            where.append(" IN(");
            int index = i * max;
            int length = Math.min((i + 1) * max, paths.size());
            List<String> fileList = new ArrayList<String>();
            for (int j = index; j < length; j++) {
                fileList.add(paths.get(j));
                where.append("?");
                if (j < length - 1) {
                    where.append(",");
                }
            }
            where.append(")");
            String[] whereArgs = new String[length - index];
            fileList.toArray(whereArgs);
            try {
                mContext.getContentResolver().delete(uri, where.toString(), whereArgs);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
                if (mBaseAsyncTask != null) {
                    mBaseAsyncTask.cancel(true);
                }
            } catch (Exception e) { // PR-1246887 Nicky Ni -001 20151229
                e.printStackTrace();
            }
        }
    }

    /**
     * delete the record in MediaStore
     *
     * @param path the delete file or folder in MediaStore
     */
    public void deleteFileInMediaStore(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = FileColumns.DATA + "=?";
        String[] whereArgs = new String[]{
                path
        };
        if (mContext != null) {
            ContentResolver cr = mContext.getContentResolver();
            LogUtils.d(TAG, "deleteFileInMediaStore,delete.");
            try {
                cr.delete(uri, where, whereArgs);
            } catch (UnsupportedOperationException e) {
                if (mBaseAsyncTask != null) {
                    mBaseAsyncTask.cancel(true);
                }
            }
        }
    }


    /**
     * Set dstfolder so when scan files size more than SCAN_FOLDER_NUM use folder
     * path to make scanner scan this folder directly.
     *
     * @param dstFolder
     */
    public void setDstFolder(String dstFolder) {
        mDstFolder = dstFolder;
    }

}
