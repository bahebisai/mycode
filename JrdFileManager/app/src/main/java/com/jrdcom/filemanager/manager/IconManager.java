package com.jrdcom.filemanager.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class IconManager {
    public static final String TAG = "IconManager";

    private static IconManager sInstance = new IconManager();
    public static int GRID_ITEM = 0;
    public static int LIST_ITEM = 1;

    private Resources mRes;
    protected Bitmap mIconsHead;

    private Map<String, Boolean> rightCaches;
    private LruCache<String, Drawable> mLrucache;
    private ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final int maxMemory = (int) Runtime.getRuntime().maxMemory();

    private IconManager() {

        mLrucache = new LruCache<String, Drawable>(maxMemory / 8) {
            @Override
            protected int sizeOf(String key, Drawable bitmap) {
                int size = bitmap.getIntrinsicWidth() * bitmap.getIntrinsicHeight();
                return size;
            }
        };
        rightCaches = new HashMap<String, Boolean>(100);

    }

    public void clearAll() {
        rightCaches.clear();
        mLrucache.evictAll();
    }


    public void removeCache(String path) {
        mLrucache.remove(path);
        rightCaches.remove(path);
    }


    /**
     * This method gets instance of IconManager
     *
     * @return instance of IconManager
     */
    public static IconManager getInstance() {
        return sInstance;
    }

    /**
     * This method gets the drawable id based on the mimetype
     *
     * @param mimeType the mimeType of a file/folder
     * @return the drawable icon id based on the mimetype
     */
    public static int getDrawableId(String mimeType, int mode) {
        if (TextUtils.isEmpty(mimeType)) {
            return R.drawable.ic_type_misc;
        }  else if (mimeType.startsWith("audio/") || mimeType.startsWith("application/ogg")) {
            return R.drawable.ic_type_audio;
        } else if (mimeType.startsWith("image/")) {
            return R.drawable.ic_type_image;
        } else if (mimeType.startsWith("video/") ||
                mimeType.startsWith("application/sdp")) {
            return R.drawable.ic_type_video;
        } else if (mimeType.startsWith("text/html") ||
                mimeType.startsWith("text/htm") ||
                mimeType.startsWith("application/vnd.wap.xhtml+xml")) {
            return R.drawable.ic_type_web;
        } else if (mimeType.startsWith("application/vnd.android.package-archive")) {
            return R.drawable.ic_type_installer;
        } else if (mimeType.startsWith("application/zip") ||
                mimeType.startsWith("application/x-rar-compressed") ||
                mimeType.startsWith("application/x-tar") ||
                mimeType.startsWith("application/x-7z-compressed")) {
            return R.drawable.ic_type_archive;
        } else if (mimeType.startsWith("application/vnd.ms-powerpoint") ||
                mimeType.startsWith("application/mspowerpoint") ||
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                mimeType.startsWith("application/vnd.ms-excel") ||
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return R.drawable.ic_type_chart;
        } else if (mimeType.startsWith("text/") ||
                mimeType.startsWith("application/msword") ||
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                mimeType.startsWith("application/pdf")) {
            return R.drawable.ic_type_doc;
        } else {
            return R.drawable.ic_type_misc;
        }
    }

    /**
     * This method gets icon from resources according to file's information.
     *
     * @param res      Resources to use
     * @param fileInfo information of file
     * @param service  FileManagerService, which will provide function to get
     *                 file's Mimetype
     * @return bitmap(icon), which responds the file
     */
    public int getIcon(FileInfo fileInfo, int mode) {
        int iconId = -1;
        if (fileInfo.isDirectory()) {
            iconId = getFolderIcon(fileInfo, mode);
        } else {
            String mimeType = fileInfo.getMime();
            iconId = getDrawableId(mimeType, mode);
        }
        return iconId;
    }

    private int getFolderIcon(FileInfo fileInfo, int mode) {
        if (fileInfo != null &&fileInfo.getFolderCount()==0){
            return R.drawable.ic_type_folder_empty;
        }

        return R.drawable.ic_type_folder;
    }

    /**
     * This method initializes variable mExt of IIconExtension type, and create
     * system folder.
     *
     * @param context Context to use
     * @param path    create system folder under this path
     */
    public void init(Context context, String path) {
        mRes = context.getResources();
    }

    /**
     * Get the external icon . icon.
     *
     * @param path  for cache key
     * @param resId resource ID for external icon
     * @return external icon for certain item
     */
    public Bitmap getExternalIcon(String path, int resId) {
        Bitmap icon = getDefaultIcon(resId);
        return icon;
    }

    /**
     * Get the default bitmap and cache it in memory.
     *
     * @param resId resource ID for default icon
     * @return default icon
     */
    public Bitmap getDefaultIcon(int resId) {
        return readBitmap(resId);
    }

    public Bitmap readBitmap(int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = mRes.openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public Drawable getImageCacheDrawable(String path) {
        return mLrucache.get(path);
    }

    public Drawable loadImage(final Context context, FileInfo fileInfo, final IconCallback callback) {
        final FileInfo mFileInfo = fileInfo;
        final String filePath = fileInfo.getFileAbsolutePath();
        executorService.execute(new Runnable() {
            public void run() {
                final DrmManager mDrmManager = DrmManager.getInstance(context.getApplicationContext());
                boolean isDrm = false;
                boolean newRight = false;
                String drmOriginalType = null;
                if(!mFileInfo.isDirectory()) {
                     isDrm =mFileInfo.isDrmFile() || mFileInfo.isDrm();
                    if(isDrm) {
                        newRight = mDrmManager.isRightsStatus(filePath);
                        drmOriginalType = mDrmManager.getOriginalMimeType(filePath);
                    }
                }
                if ((mLrucache.get(filePath) != null)) {
                    Drawable icon = mLrucache.get(filePath);
                    if (icon != null) {
                        if (isDrm && (rightCaches.get(filePath) != null)) {
                            boolean oldRight = rightCaches.get(filePath);
                            if (oldRight == newRight) {
                                callback.iconLoaded(icon);
                                return;
                            }
                        } else {
                            callback.iconLoaded(icon);
                            return;
                        }
                    }
                }
                try {
                    Drawable icon = FileUtils.queryThumbnail(context, mFileInfo, mFileInfo.getMime(), isDrm,newRight,drmOriginalType,mDrmManager);
                    if (icon != null) {
                        mLrucache.put(filePath, icon);
                        if (isDrm) {
                            rightCaches.put(filePath, newRight);
                        }
                        callback.iconLoaded(icon);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }


    public interface IconCallback {
        public void iconLoaded(Drawable iconDrawable);
    }
}
