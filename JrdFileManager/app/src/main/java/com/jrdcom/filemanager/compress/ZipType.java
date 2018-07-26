package com.jrdcom.filemanager.compress;

/**
 * To zip and unzip files, it's better to run this code in a thread.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipType extends CommonCompress {
    public static final String TAG = ZipType.class.getSimpleName();

    @Override
    public boolean doUnArchive(String srcFile, String outputDirectory) {

        Log.e("FileManager_YY", "unzip begin");

        ZipFile zipFile = null;
        sInterrupt = false;
        boolean isSuccess = true;
        try {
            zipFile = new ZipFile(srcFile, Charset.forName("GBK"));
            Enumeration e = zipFile.entries();
            File dest = new File(outputDirectory);
            if (!dest.mkdirs()) {
                Log.e(TAG, "ZipType->unzip: " + " failed to create dir0: " + e);
            }

            int pos = 1;
            int total = zipFile.size();
            while (e.hasMoreElements()) {
                if (sInterrupt) {
                    break;
                }



                BufferedInputStream buffIn = null;
                BufferedOutputStream bos = null;

                byte[] buf = new byte[1024];

                try {
                    ZipEntry zipEntry = (ZipEntry) e.nextElement();
                    File f = new File(outputDirectory + File.separator + zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        if (!f.mkdirs()) {
                            Log.i(TAG, "ZipType->unzip: " + "dir1: " + f.getName());
                        }
                    } else {
                        File parent = f.getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }

                        FileOutputStream fos = new FileOutputStream(f);
                        bos = new BufferedOutputStream(fos);

                        buffIn = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                        int len;
                        while ((len = buffIn.read(buf)) != -1) {
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
                    Log.e(TAG, f.getName() + " onUnArchive success");

                } catch (IllegalArgumentException ex){
                    isSuccess = false;
                    Log.e(TAG, "ZipType->unzip: unzip failed, please check.");
                } catch (Exception ex) {
                    isSuccess = false;
                    Log.e(TAG, "ZipType->unzip: unzip failed, please check.");
                } finally {
                    if (buffIn != null) {
                        try {
                            buffIn.close();
                        } catch (IOException ex) {
                            Log.e(TAG, "ZipType->unzip: failed to close input stream");
                        }
                    }
                    if (bos != null) {
                        try {
                            bos.close();
                        } catch (IOException ex) {
                            Log.e(TAG, "ZipType->unzip: failed to close output stream");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            isSuccess = false;
            Log.e(TAG, "ZipType->unzip: unzip failed, may be not valid zip file." + ex);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                    Log.e(TAG, "ZipType->unzip: close zipFile failed.");
                }
            }
        }

        if (sInterrupt) {
            Log.e(TAG, "ZipType->doUnArchive: unzip cancelled");
            sInterrupt = false;
        } else {
            Log.e(TAG, "unzip end");
        }

        return isSuccess;
    }

    @Override
    public boolean doArchive() {
        boolean isSuccessed = true;

        if (!mPrepare) {
            Log.e(TAG, "Please call prepare() before doArchive().");
        }

        sInterrupt = false;
        ZipOutputStream out = null;
        BufferedOutputStream buffOut = null;
        FileOutputStream fileOutPut = null;
        File outFile = null;

        try {
            outFile = new File(mDestPath);
            fileOutPut = new FileOutputStream(outFile);
            buffOut = new BufferedOutputStream(fileOutPut);
            out = new ZipOutputStream(buffOut);

            byte[] buffer = new byte[4096];
            int total = mFileList.size();

            for (int i = 0; i < total; i++) {

                int bytes_read;
                CompressOptions option = mFileList.get(i);
                File child = option.zipFile;
                long time = System.currentTimeMillis();
                if (child.isFile()) {
                    FileInputStream in = null;
                    BufferedInputStream buffIn = null;
                    try {
                        in = new FileInputStream(option.zipFile);
                        buffIn = new BufferedInputStream(in);
                        ZipEntry entry = new ZipEntry(mFileList.get(i).relPath);

                        out.putNextEntry(entry);

                        while ((bytes_read = buffIn.read(buffer)) != -1) {
                            if (sInterrupt) {
                                buffIn.close();
                                throw new Exception("User cancelled.");
                            }
                            out.write(buffer, 0, bytes_read);
                        }
                        out.closeEntry();
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        if(buffIn!=null)buffIn.close();
                        if(in!=null)in.close();
                    }
                } else {
                    File[] fs = child.listFiles();
                    if (fs.length <= 0) {
                        out.putNextEntry(new ZipEntry(mFileList.get(i).relPath + "/"));
                        out.closeEntry();
                    }
                }

                if (mObserver != null) {
                    mObserver.onArchiveComplete(option.zipFile, i + 1, total);
                }
                Log.e(TAG, "zip " + option.zipFile.getName() + " complete. Size =  " + option.zipFile.length() + ", time: " + (System.currentTimeMillis() - time));
            }


        } catch (Exception e) {
            isSuccessed = false;
            Log.d(TAG, "ZipType->zip: " + " failed to zip files" + e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                    Log.e(TAG, "ZipType->zip: " + " close output stream success");
                } catch (IOException ex) {
                    Log.e(TAG, "ZipType->zip: " + " failed to close stream" + ex);
                }
                try {
                    if(fileOutPut != null){
                        fileOutPut.flush();
                        fileOutPut.close();
                        if(buffOut!=null)buffOut.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (sInterrupt) {

            Log.e(TAG, "ZipType->zip: Zip cancelled");
            // if outFile exists, delete
            if (outFile != null && outFile.exists()) {
                if (outFile.delete()) {
                    Log.e(TAG, "ZipType->zip: " + outFile.getName() + " has been deleted.");
                }
            }
            sInterrupt = false;
            isSuccessed = false;
        } else {
            Log.e(TAG, "Zip end");
        }
        return isSuccessed;
    }
}
