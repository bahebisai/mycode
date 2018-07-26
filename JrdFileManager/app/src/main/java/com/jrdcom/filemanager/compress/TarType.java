package com.jrdcom.filemanager.compress;

import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by yyang on 8/23/16.
 *
 */
public final class TarType extends CommonCompress {


    @Override
    public boolean doArchive() {
        boolean isSuccessed = true;
        if (!mPrepare) {
            Log.e("FileManager_YY", "Please call prepare() before doArchive().");
        }

        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(mDestPath);
            bos = new BufferedOutputStream(fos);
            TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

            byte[] buf = new byte[1024];

            int total = mFileList.size();

            for (int i = 0; i < total; i++) {
//            for (CompressOptions option : mFileList) {
                CompressOptions option = mFileList.get(i);
                File child = option.zipFile;
                if (child.isFile()) { // 文件
                    FileInputStream fis = new FileInputStream(child);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    TarArchiveEntry tae = new TarArchiveEntry(option.relPath);
                    tae.setSize(child.length());
                    taos.putArchiveEntry(tae);
                    int len;
                    while ((len = bis.read(buf)) > 0) {
                        if (sInterrupt) {
                            bis.close();
                            throw new Exception("User cancelled.");
                        }
                        taos.write(buf, 0, len);
                    }
                    bis.close();
                    taos.closeArchiveEntry();

                } else {
                    File[] fs = child.listFiles();
                    if (fs.length <= 0) {
                        taos.putArchiveEntry(new TarArchiveEntry(option.relPath + "/"));
                        taos.closeArchiveEntry();
                    }
                }

                if (mObserver != null) {
                    mObserver.onArchiveComplete(child, i + 1, total);
                }
                Log.e("FileManager_YY", child.getName() + " has compress sucessfully");
            }
            taos.flush();

        } catch (Exception e) {
            Log.e("FileManager_YY", "TarCompress->doArchive: " + " compress failed." + e);
            isSuccessed = false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                    if (sInterrupt) {
                        File file = new File(mDestPath);
                        if (file.exists()) {
                            boolean result = file.delete();
                            Log.e("FileManager_YY", "Delete file: " + file.getName() + ", result: " + result);
                        }
                        isSuccessed = false;
                    }
                } catch (Exception e) {
                    Log.e("FileManager_YY", "bos close failed." + e);
                }
            }
        }
        return isSuccessed;
    }


    public boolean doUnArchive(String srcFile, String destPath) {
        try {
            byte[] buf = new byte[1024];
            TarArchiveInputStream tais = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile)));
            int pos = 1;
            int total = getTotal(tais);

            tais = new TarArchiveInputStream(new BufferedInputStream(new FileInputStream(srcFile)));
            TarArchiveEntry tae;

            while ((tae = tais.getNextTarEntry()) != null) {
                File f = new File(destPath + "/" + tae.getName());
                if (tae.isDirectory()) {
                    f.mkdirs();
                } else {
                    File parent = f.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(f);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int len;
                    while ((len = tais.read(buf)) != -1) {
                        if (sInterrupt) {
                            break;
                        }
                        bos.write(buf, 0, len);
                    }
                    bos.flush();
                    bos.close();
                }


                if (mObserver != null) {
                    mObserver.onUnArchiveComplete(f, pos++, total);
                }
                Log.e("FileManager_YY", f.getName() + " onUnArchive success");
            }
            tais.close();
        } catch (Exception e) {
            Log.e("FileManager_YY", "TarType->doUnArchive: " + " failed to UnArchive. " + e);
            return false;
        } finally {
            if (sInterrupt) {
                sInterrupt = false;
            }
        }

        return true;
    }

    private int getTotal(TarArchiveInputStream tais) {
        int total = 0;

        try {
            while (tais.getNextTarEntry() != null) {

                total++;
            }
        } catch (IOException e) {
            Log.e("FileManager_YY", "TarType->getTotal: " + " failed to getTotal. " + e);
        }

        return total;
    }
}