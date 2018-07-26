package com.jrdcom.filemanager.manager;

import android.content.Context;
import android.os.Environment;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MountManager {
    private static final String TAG = "MountManager";

    public static final String SEPARATOR = File.separator;
    public static final String HOME = "Home";
    public static final String ROOT_PATH = "Root Path";

    private String mRootPath = "Root Path";
    private static MountManager sInstance;

    private StorageManager mStorageManager;
    private final List<MountPoint> mMountPathList = new CopyOnWriteArrayList<MountPoint>();
    private Context mContext;

    private MountManager() {
    }

    /**
     * This method initializes MountPointManager.
     *
     * @param context Context to use
     */
    public void init(Context context) {
        mContext = context;
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        if (!TextUtils.isEmpty(getDefaultPath())) {
            mRootPath = FileUtils.getFilePath(getDefaultPath());
        }
        mMountPathList.clear();
        // check media availability to init mMountPathList
        StorageVolume[] storageVolumeList = mStorageManager.getVolumeList();
        if (storageVolumeList != null) {
            for (StorageVolume volume : storageVolumeList) {
                MountPoint mountPoint = new MountPoint();
                mountPoint.mPath = volume.getPath();
                mountPoint.mIsMounted = isMounted(volume.getPath());
                mountPoint.mIsExternal = volume.isRemovable();
                if (mountPoint.mIsExternal && mountPoint.mPath.equals("/storage/usbotg")) {
                    mountPoint.isOTG = true;
                } else if (mountPoint.mIsExternal) {
                    mountPoint.isSdCard = true;
                }
                getMountPointDescription(mountPoint, context);
                if (mountPoint.mIsMounted) {
                    mMountPathList.add(mountPoint);
                }
            }
        }
        List<VolumeInfo> volumeInfoList = null;
        if(CommonUtils.hasN()){
             volumeInfoList = mStorageManager.getVolumes();
        }
        if (volumeInfoList != null && CommonUtils.hasN()) {
            for (VolumeInfo volume : volumeInfoList) {
                MountPoint mountPoint = new MountPoint();
                DiskInfo disk = volume.getDisk();
                if (disk == null || (!disk.isUsb() && !disk.isSd()) || volume.getPath() == null) {
                    continue;
                }
                boolean isUSB = disk.isUsb();
                boolean isSD = disk.isSd();
                mountPoint.mPath = volume.getPath().getAbsolutePath();
                /*
                If the List have an SD card and OTG relevant information will remove the original information, add again.
                 */
                if(mMountPathList != null) {
                    for (int i = 0; i < mMountPathList.size(); i++) {
                      MountPoint mMountpoint = mMountPathList.get(i);
                        if(mMountpoint.mPath != null  && mountPoint.mPath != null &&
                                mMountpoint.mPath.equals(mountPoint.mPath)){
                            mMountPathList.remove(i);
                        }
                    }
                }
                if(isUSB && volume.getState() == VolumeInfo.STATE_MOUNTED){
                    mountPoint.mIsMounted = true;
                } else {
                    mountPoint.mIsMounted = isMounted(volume.getPath().getAbsolutePath());
                }
                if(disk.isUsb()) {
                    mountPoint.mIsExternal = isUSB;
                    mountPoint.isOTG = isUSB;
                } else{
                    mountPoint.mIsExternal = isSD;
                    mountPoint.isSdCard = isSD;
                }
                getMountPointDescription(mountPoint, context);
                //Only show the mounted volume
                mMountPathList.add(mountPoint);

            }
        }

        IconManager.getInstance().init(context, getDefaultPath() + SEPARATOR);
    }

    /**
     * This method gets instance of MountPointManager. Before calling this
     * method, must call init().
     *
     * @return instance of MountPointManager
     */
    public static MountManager getInstance() {
        if (sInstance == null) {
            sInstance = new MountManager();
        }

        return sInstance;
    }

    private static class MountPoint {
        String mDescription;
        String mPath;
        boolean mIsExternal;
        boolean mIsMounted;
        boolean isSdCard;
        boolean isOTG;
    }

    /**
     * This method checks weather certain path is root path.
     *
     * @param path certain path to be checked
     * @return true for root path, and false for not root path
     */
    public boolean isRootPath(String path) {
        return mRootPath.equals(path);
    }

    public boolean isPhoneRootPath(String path) {
        if (path != null) {
            if (path.equals(getPhonePath())) {
                return true;
            } else if (path.equals(getSDCardPath())) {
                return true;
            } else if (path.equals("/storage/usbotg")) {
                return true;
            } else if (path.equals(getUsbOtgPath())) {
                return true;
            }

        }
        return false;
    }

    /**
     * This method gets root path
     *
     * @return root path
     */
    public String getRootPath() {
        return mRootPath;
    }

    /**
     * This method gets informations of file of mount point path
     *
     * @return fileInfos of mount point path
     */
    public List<FileInfo> getMountPointFileInfo() {
        List<FileInfo> fileInfos = new ArrayList<FileInfo>(0);
        FileInfo internal = null, sdcard = null, external = null;
        for (MountPoint mp : mMountPathList) {
            if (!mp.mIsExternal) {
                internal = new FileInfo(mContext, mp.mPath);
            } else if (mp.mIsExternal && mp.mIsMounted && mp.isSdCard) {
                sdcard = new FileInfo(mContext, mp.mPath);
            } else if (mp.mIsExternal && mp.mIsMounted && mp.isOTG) {
                external = new FileInfo(mContext, mp.mPath);
            }
        }
        if (internal != null) {
            fileInfos.add(internal);
        }
        if (sdcard != null) {
            fileInfos.add(sdcard);
        }
        if (external != null) {
            fileInfos.add(external);
        }
        return fileInfos;
    }

    public boolean isOTGMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)
                    && mountPoint.isOTG) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets the description of mountPoints.
     *
     * @param mp the mount point that should be checked
     */
    public void getMountPointDescription(MountPoint mp, Context context) {
        if (!mp.mIsExternal) {
            mp.mDescription = context.getResources().getString(R.string.draw_left_phone_storage_n);
        } else {
            if (mp.isOTG) {
                mp.mDescription = context.getResources().getString(R.string.usbotg_n);
            } else {
                mp.mDescription = context.getResources().getString(R.string.sd_card);
            }
        }
    }

    /**
     * This method gets count of mount, number of mount point(s)
     *
     * @return number of mount point(s)
     */
    public int getMountCount() {
        int count = 0;
        for (MountPoint mPoint : mMountPathList) {
            if (mPoint.mIsMounted) {
                count++;
            }
        }
        return count;
    }

    /**
     * This method gets default path from StorageManager
     *
     * @return default path from StorageManager
     */
    public String getDefaultPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * This method checks whether mountPoint is mounted or not
     *
     * @param mountPoint the mount point that should be checked
     * @return true if mountPhont is mounted, false otherwise
     */
    public boolean isMounted(String mountPoint) {
        if (TextUtils.isEmpty(mountPoint)) {
            return false;
        }
        String state = null;
        if(CommonUtils.hasN() && getUsbOtgPath() != null && mountPoint.startsWith(getUsbOtgPath())){
           return isOtgMounted();
        }
        state = mStorageManager.getVolumeState(mountPoint);
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * This method checks whether SDCard is mounted or not
     *
     * @return true if SDCard is mounted, false otherwise
     */
    public boolean isSDCardMounted() {
        try {
            for (MountPoint mp : mMountPathList) {
                if (mp.mIsExternal && mp.mIsMounted && mp.isSdCard) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtils.e("MountManager", "Exception occured when isSDCardMounted():", e);
            return false;
        }
        return false;
    }

    public boolean isOtgMounted() {
        try {
            for (MountPoint mp : mMountPathList) {
                if (mp.mIsExternal && mp.mIsMounted && mp.isOTG) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtils.e("MountManager", "Exception occured when isOtgMounted():", e);
            return false;
        }
        return false;
    }


    /**
     * This method checks whether each of SDCard is mounted or not
     *
     * @return true if SDCard is mounted, false otherwise
     */
    public boolean isSignalSDCardMounted(int i) {
        MountPoint mp = mMountPathList.get(i);
        if (mp.mIsExternal && mp.mIsMounted) {
            return true;
        }
        return false;
    }


    /**
     * This method checks whether the current path is mount path.
     *
     * @param path
     * @return
     */
    public boolean isSdOrPhonePath(String path) {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets real mount point path for certain path.
     *
     * @param path certain path to be checked
     * @return real mount point path for certain path, "" for path is not
     * mounted
     */
    public String getRealMountPointPath(String path) {
        for (MountPoint mountPoint : mMountPathList) {
            if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                return mountPoint.mPath;
            }
        }
        return "";
    }

    /**
     * This method changes mount state of mount point, if parameter path is
     * mount point.
     *
     * @param path      certain path to be checked
     * @param isMounted flag to mark weather certain mount point is under
     *                  mounted state
     * @return true for change success, and false for fail
     */
    public boolean changeMountState(String path, Boolean isMounted) {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mPath.equals(path)) {
                if (mountPoint.mIsMounted == isMounted) {
                    return false;
                } else {
                    mountPoint.mIsMounted = isMounted;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is mount point.
     *
     * @param path certain path, which needs to be checked
     * @return true for mount point, and false for not mount piont
     */
    public boolean isMountPoint(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (path.equals(mountPoint.mPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is internal mount path.
     *
     * @param path path which needs to be checked
     * @return true for internal mount path, and false for not internal mount
     * path
     */
    public boolean isInternalMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal && mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks weather certain path is external mount path.
     *
     * @param path path which needs to be checked
     * @return true for external mount path, and false for not external mount
     * path
     */
    public boolean isExternalMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)) {
                return true;
            }
        }
        return false;
    }


    public boolean isSDMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)
                    && mountPoint.isSdCard) {
                return true;
            }
        }
        return false;
    }

    public boolean isUSBMountPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.mPath.equals(path)
                    && mountPoint.isOTG) {
                return true;
            }
        }
        return false;
    }


    /**
     * This method return the file of phone.
     *
     * @return
     */
    public File getPhoneFile() {
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal) {
                return new File(mountPoint.mPath);
            }
        }
        return null;
    }

    /**
     * This method return the file of sd card.
     *
     * @return
     */
    public File getSDCardFile() {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal) {
                return new File(mountPoint.mPath);
            }
        }
        return null;
    }

    /**
     * This method return the path of phone.
     *
     * @return
     */
    public String getPhonePath() {
        for (MountPoint mountPoint : mMountPathList) {
            if (!mountPoint.mIsExternal) {
                return mountPoint.mPath;
            }
        }
        return null;
    }

    /**
     * This method return the path of sd card.
     *
     * @return
     */
    public String getSDCardPath() {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.isSdCard) {
                return mountPoint.mPath;
            }
        }
        return null;
    }


    public String getUsbOtgPath() {
        for (MountPoint mountPoint : mMountPathList) {
            if (mountPoint.mIsExternal && mountPoint.isOTG) {
                return mountPoint.mPath;
            }
        }
        return null;
    }

    public boolean isUsbOtg(int i) {
        MountPoint mountPoint = mMountPathList.get(i);
        if (mountPoint.mIsExternal && mountPoint.isOTG) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSdOrOtg() {
        if (getMountCount() == 1) {
            if (mMountPathList.get(0).mIsExternal && mMountPathList.get(0).isOTG) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }


    /**
     * This method checks weather certain file is External File.
     *
     * @param fileInfo certain file needs to be checked
     * @return true for external file, and false for not external file
     */
    public boolean isExternalFile(FileInfo fileInfo) {
        if (fileInfo != null) {
            String mountPath = getRealMountPointPath(fileInfo.getFileAbsolutePath());
            if (mountPath.equals(fileInfo.getFileAbsolutePath())) {
                return false;
            }
            if (isExternalMountPath(mountPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets description of certain path
     *
     * @param path certain path
     * @return description of the path
     */
    public String getDescriptionPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            for (MountPoint mountPoint : mMountPathList) {
                if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR)) {
                    return path.length() > mountPoint.mPath.length() + 1 ? mountPoint.mDescription
                            + SEPARATOR + path.substring(mountPoint.mPath.length() + 1)
                            : mountPoint.mDescription;
                }
            }
        }
        return path;
    }
}
