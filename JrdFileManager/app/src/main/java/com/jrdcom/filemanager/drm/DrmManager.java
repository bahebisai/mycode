package com.jrdcom.filemanager.drm;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jrdcom.filemanager.PlfUtils;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;
import com.tct.drm.api.TctDrmManager;

public class DrmManager {

    private static final String TAG = DrmManager.class.getSimpleName();


    public static int DRM_SCHEME_OMA1_FL;
    public static int DRM_SCHEME_OMA1_CD;
    public static int DRM_SCHEME_OMA1_SD;
    public static String RIGHTS_ISSUER;
    public static String CONSTRAINT_TYPE;
    public static String CONTENT_VENDOR;

    public static int mCurrentDrm = CommonIdentity.NO_DRM;
    private static boolean isDrmEnable;
    private static DrmManager sInstance;

    private TctDrmManager mTctDrmManager;
    private com.tct.omadrm.MtkDrmManager mtkDrmManagerForN;
    private com.mtk.drm.frameworks.MtkDrmManager mtkDrmManagerForM;
    private DrmManagerClient mDrmManagerClient;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    public static int METHOD_FL = 1;
    public static int METHOD_SD = 3;
    private Context mContext;

    public static void setScheme() {
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    DRM_SCHEME_OMA1_FL = CommonIdentity.DRM_SCHEME_OMA1_FL;
                    DRM_SCHEME_OMA1_CD = CommonIdentity.DRM_SCHEME_OMA1_CD;
                    DRM_SCHEME_OMA1_SD = CommonIdentity.DRM_SCHEME_OMA1_SD;
                    RIGHTS_ISSUER = CommonIdentity.RIGHTS_ISSUER;
                    CONSTRAINT_TYPE = CommonIdentity.CONSTRAINT_TYPE;
                    CONTENT_VENDOR = CommonIdentity.CONTENT_VENDOR;
                    if (CommonUtils.hasN()) {
                        CommonIdentity.TCT_IS_DRM = com.mtk.drm.frameworks.MtkDrmManager.TCT_IS_DRM;
                    } else {
                        CommonIdentity.TCT_IS_DRM = com.tct.omadrm.MtkDrmManager.TCT_IS_DRM;
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    DRM_SCHEME_OMA1_FL = TctDrmManager.DRM_SCHEME_OMA1_FL;
                    DRM_SCHEME_OMA1_CD = TctDrmManager.DRM_SCHEME_OMA1_CD;
                    DRM_SCHEME_OMA1_SD = TctDrmManager.DRM_SCHEME_OMA1_SD;
                    RIGHTS_ISSUER = TctDrmManager.RIGHTS_ISSUER;
                    CONSTRAINT_TYPE = TctDrmManager.CONSTRAINT_TYPE;
                    CONTENT_VENDOR = TctDrmManager.CONTENT_VENDOR;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Constructor for DrmManager.
     */
    public DrmManager(Context context) {
        mContext = context;
        mCurrentDrm = getDrmPlatform();
        setScheme();
        if (isDrmEnable) {
            mDrmManagerClient = new DrmManagerClient(mContext);
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasBelowN()) {
                        mtkDrmManagerForM = com.mtk.drm.frameworks.MtkDrmManager.getInstance(mContext);
                    } else {
                        try {
                            mtkDrmManagerForN = com.tct.omadrm.MtkDrmManager.getInstance(context);
                        } catch (NoClassDefFoundError e) {
                            //LogUtils.e(Logging.LOG_TAG, e, "happened error when init MtkDrmManager in M platform.");
                        }
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    mTctDrmManager = new TctDrmManager(mContext);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Get a DrmManager Object.
     *
     * @return a instance of DrmManager.
     */
    public static DrmManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DrmManager(context);
        }
        return sInstance;
    }

    private boolean isMTKDrm() {
        try {
            //whether the mtk platform
            if (!Build.HARDWARE.startsWith("mt")) {
                return false;
            }
            Class<?> managerClass = null;
            if (CommonUtils.hasN()) {
                managerClass = Class.forName("com.tct.omadrm.MtkDrmManager");
            } else {
                managerClass = Class.forName("com.mtk.drm.frameworks.MtkDrmManager");
            }
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isQcomDrm() {
        try {
            //whether the qualcom platform
            if (!Build.HARDWARE.startsWith("qcom")) {
                return false;
            }
            Class<?> managerClass = Class.forName("com.tct.drm.TctDrmManagerClient");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getDrmPlatform() {
        LogUtils.d(TAG, "def_DRM_included" + PlfUtils.getBoolean(mContext, "def_DRM_included"));
        if (PlfUtils.getBoolean(mContext, "def_DRM_included")) {
            if (isQcomDrm()) {
                isDrmEnable = TctDrmManager.isDrmEnabled();
                return CommonIdentity.QCOM_DRM;
            } else if (isMTKDrm()) {
                if (CommonUtils.hasN()) {
                    isDrmEnable = com.tct.omadrm.MtkDrmManager.isDrmEnabled();
                } else {
                    isDrmEnable = com.mtk.drm.frameworks.MtkDrmManager.isDrmEnabled();
                }
                return CommonIdentity.MTK_DRM;
            }
        }
        isDrmEnable = false;
        return CommonIdentity.NO_DRM;
    }

    /**
     * Get original mimeType of a file.
     *
     * @param path The file's path.
     * @return original mimeType of the file.
     */
    public String getOriginalMimeType(String path) {
        if (isDrmEnable) {
            return mDrmManagerClient.getOriginalMimeType(path);
        }
        return "";
    }

    /**
     * This method check weather the rights-protected content has valid right to
     * transfer.
     *
     * @param path path to the rights-protected content.
     * @return true for having right to transfer, false for not having the
     * right.
     */
    public boolean canTransfer(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        flag = mtkDrmManagerForN.checkRightsStatus(path, DrmStore.Action.TRANSFER) == DrmStore.RightsStatus.RIGHTS_VALID;
                    } else {
                        flag = mtkDrmManagerForM.checkRightsStatus(path, DrmStore.Action.TRANSFER) == DrmStore.RightsStatus.RIGHTS_VALID;
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    flag = TctDrmManager.checkRightsStatus(path, DrmStore.Action.TRANSFER)
                            != DrmStore.RightsStatus.RIGHTS_VALID;
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    /**
     * check weather the rights-protected content has valid right or not
     *
     * @param path path to the rights-protected content.
     * @return true for having valid right, false for invalid right
     */
    public boolean isRightsStatus(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        flag = mtkDrmManagerForN.isRightValid(path);
                    } else {
                        flag = mtkDrmManagerForM.isRightValid(path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    flag = mTctDrmManager.isRightValid(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public static int getAction(String mime) {
        if (mime.startsWith(CommonIdentity.MIME_HAED_IMAGE)) {
            return DrmStore.Action.DISPLAY;
        } else if (mime.startsWith(CommonIdentity.MIME_HEAD_AUDIO)
                || mime.startsWith(CommonIdentity.MIME_HEAD_VIDEO)) {
            return DrmStore.Action.PLAY;
        }

        return DrmStore.Action.PLAY; // otherwise PLAY is returned.
    }

    /**
     * This static method check a file is DRM file, or not.
     *
     * @param fileName the file which need to be checked.
     * @return true for DRM file, false for not DRM file.
     */
    public static boolean isDrmFileExt(String fileName) {
        if (isDrmEnable) {
            String extension = FileUtils.getFileExtension(fileName);
            if (!TextUtils.isEmpty(extension) && FileUtils.drmTypeMap.containsKey(extension)) {
                return true; // all drm files cannot be copied
            }
        }
        return false;
    }

    public boolean isDrm(String path) {
        try {
            return executorService.submit(new newThread(path)).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private class newThread implements Callable<Boolean> {
        private String path = null;

        public newThread(String path) {
            this.path = path;
        }

        public Boolean call() {
            boolean flag = Boolean.valueOf(false);
            if (isDrmEnable) {
                switch (mCurrentDrm) {
                    case CommonIdentity.MTK_DRM:
                        if (CommonUtils.hasN()) {
                            flag = mtkDrmManagerForN.isDrm(path);
                        } else {
                            flag = mtkDrmManagerForM.isDrm(path);
                        }
                        break;
                    case CommonIdentity.QCOM_DRM:
                        flag = Boolean.valueOf(mTctDrmManager.isDrm(path));
                        break;
                    default:
                        break;
                }
            }
            return flag;
        }
    }

    public Bitmap getDrmVideoThumbnail(Bitmap bitmap, String filePath, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    //there is no getDrmVideoThumbnail fucntion in mtk platform
                    break;
                case CommonIdentity.QCOM_DRM:
                    b = TctDrmManager.getDrmVideoThumbnail(bitmap, filePath, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public Bitmap getDrmThumbnail(String filePath, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        b = mtkDrmManagerForN.getDrmThumbnail(filePath, size);
                    } else {
                        b = mtkDrmManagerForM.getDrmThumbnail(filePath, size);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    b = TctDrmManager.getDrmThumbnail(filePath, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public Bitmap getDrmRealThumbnail(String filePath, BitmapFactory.Options options, int size) {
        Bitmap b = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN() && mtkDrmManagerForN != null) {
                        b = mtkDrmManagerForN.getDrmThumbnail(filePath, size);
                    } else if (mtkDrmManagerForM != null) {
                        b = mtkDrmManagerForM.getDrmThumbnail(filePath, size);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    b = TctDrmManager.getDrmRealThumbnail(filePath, options, size);
                    break;
                default:
                    break;
            }
        }
        return b;
    }

    public boolean isDrmCDFile(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if(mtkDrmManagerForN != null) {
                        flag = mtkDrmManagerForN.isCDType(path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    flag = mTctDrmManager.isCDType(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public boolean isDrmSDFile(String path) {
        boolean flag = false;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if(mtkDrmManagerForN != null) {
                        flag = mtkDrmManagerForN.isSdType(path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    flag = mTctDrmManager.isSdType(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public int getDrmScheme(String path) {
        int flag = TctDrmManager.DRM_SCHEME_OMA1_FL;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        flag = mtkDrmManagerForN.getDrmScheme(path);
                    } else {
                        flag = mtkDrmManagerForM.getDrmScheme(path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    flag = TctDrmManager.getDrmScheme(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public ContentValues getMetadata(String path) {
        ContentValues c = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        c = mtkDrmManagerForN.getMetadata(path);
                    } else {
                        c = mtkDrmManagerForM.getMetadata(path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    c = TctDrmManager.getMetadata(path);
                    break;
                default:
                    break;
            }
        }
        return c;
    }

    public ContentValues getConstraints(String path, int action) {
        ContentValues c = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        c = mtkDrmManagerForN.getConstraints(path, action);
                    } else {
                        c = mtkDrmManagerForM.getConstraints(path, action);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    c = TctDrmManager.getConstraints(path, action);
                    break;
                default:
                    break;
            }
        }
        return c;
    }

    public boolean isAllowForward(String path) {
        boolean flag = true;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasN()) {
                        flag = mtkDrmManagerForN.isAllowForward(path);
                    } else {
                        flag = mtkDrmManagerForM.isAllowForward(path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    flag = TctDrmManager.isAllowForward(path);
                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public Dialog showRefreshLicenseDialog(Context context, String path) {
        Dialog result = null;
        if (isDrmEnable) {
            switch (mCurrentDrm) {
                case CommonIdentity.MTK_DRM:
                    if (CommonUtils.hasBelowN()) {
                        result = com.mtk.drm.frameworks.MtkDrmManager.showRefreshLicenseDialog(context, path);
                    }
                    break;
                case CommonIdentity.QCOM_DRM:
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public void restoreWallpaper() {
        if (isDrmEnable) {
            if (mCurrentDrm == CommonIdentity.MTK_DRM) {
                String filePath = Settings.System.getString(mContext.getContentResolver(), CommonIdentity.WALLPAPER_FILEPATH);
                if (!TextUtils.isEmpty(filePath) && !(new File(filePath)).exists()) {
                    if (CommonUtils.hasBelowN()) {
                        mtkDrmManagerForM.checkDrmWallpaperStatus(mContext, filePath);
                    }
                }
            }
        }
    }

}
