package com.jrdcom.filemanager.compress;

import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by user on 16-11-29.
 * To unRar files, it's better to run this code in a thread.
 */

public class RarType extends CommonCompress {
    public static final String TAG = RarType.class.getSimpleName();

    @Override
    public boolean doArchive() {
        return false;
    }

    @Override
    public boolean doUnArchive(String srcPath, String mDestPath) {
        sInterrupt = false;
        boolean isSuccess = true;
        if (!srcPath.toLowerCase().endsWith(".rar")) {
            return false;
        }
        File dstDiretory = new File(mDestPath);
        if (!dstDiretory.exists()) {
            dstDiretory.mkdirs();
        }
        Archive a = null;
        try {
            a = new Archive(new File(srcPath));

            if (a != null) {
                a.getMainHeader().print();
                FileHeader fh = a.nextFileHeader();

                int pos = 1;
                int total = a.getFileHeaders().size();
                while (fh != null) {
                    String fileName = fh.getFileNameW().isEmpty()?fh.getFileNameString():fh.getFileNameW();
                    if (fh.isDirectory()) {
                        fileName = fileName.replace("\\",File.separator);
                        File fol = new File(dstDiretory + File.separator
                                + fileName);
                        if(!fol.exists()) {
                            fol.mkdirs();
                        }
                        if (mObserver != null) {
                            mObserver.onUnArchiveComplete(fol, pos++, total);
                        }

                    } else {
                        fileName = fileName.trim().replace("\\",File.separator);
                        File out = new File(dstDiretory + File.separator
                                + fileName);
                        File dirFile = out.getParentFile();
                        if (!dirFile.exists()) {
                            dirFile.mkdirs();
                        }
                        try {
                            out.createNewFile();
                            FileOutputStream os = new FileOutputStream(out);
                            a.extractFile(fh, os);
                            os.close();
                        } catch (OutOfMemoryError e){
                            isSuccess = false;
                            e.printStackTrace();
                        } catch (Exception ex) {
                            isSuccess = false;
                            ex.printStackTrace();
                        }
                        if (mObserver != null) {
                            mObserver.onUnArchiveComplete(out, pos++, total);
                        }
                    }
                    fh = a.nextFileHeader();
                }
                a.close();
            }
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        if (sInterrupt) {
            sInterrupt = false;
        }
        return isSuccess;
    }
}
