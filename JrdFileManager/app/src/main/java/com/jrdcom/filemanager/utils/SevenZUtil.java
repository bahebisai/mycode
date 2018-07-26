package com.jrdcom.filemanager.utils;

import android.util.Log;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * @author yang
 */
public final class SevenZUtil {

    public static final String TAG = SevenZUtil.class.getSimpleName();
    private void dfs(File[] files, TarArchiveOutputStream taos, String fpath)
            throws IOException {
        byte[] buf = new byte[1024];
        for (File child : files) {
            if (child.isFile()) { // 文件
                FileInputStream fis = new FileInputStream(child);
                BufferedInputStream bis = new BufferedInputStream(fis);
                TarArchiveEntry tae = new TarArchiveEntry(fpath + child.getName());
                tae.setSize(child.length());
                taos.putArchiveEntry(tae);
                int len;
                while ((len = bis.read(buf)) > 0) {
                    taos.write(buf, 0, len);
                }
                bis.close();
                taos.closeArchiveEntry();

                Log.e(TAG, child.getName() + " has compress sucessfully");
                continue;
            }
            File[] fs = child.listFiles();
            String nfpath = fpath + child.getName() + "/";
            if (fs.length <= 0) { // 空目录
                taos.putArchiveEntry(new TarArchiveEntry(nfpath));
                taos.closeArchiveEntry();
            } else { // 目录非空，递归处理
                dfs(fs, taos, nfpath);
            }
        }
    }

    public static final void doArchiver(String src, String dest) throws IOException {

        byte[] buf = new byte[1024];

        File inputFile = new File("/storage/emulated/0/9.apk");

        SevenZOutputFile outputFile = new SevenZOutputFile(new File("/storage/emulated/0/hahaha.7z"));
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);

        SevenZArchiveEntry entry = outputFile.createArchiveEntry(inputFile,inputFile.getName());
//        SevenZArchiveEntry entry = new SevenZArchiveEntry();
//        entry.setName(inputFile.getName());
        entry.setSize(inputFile.length());
        outputFile.putArchiveEntry(entry);
        int len;
        while ((len = bis.read(buf)) > 0) {
            outputFile.write(buf, 0, len);
        }
        bis.close();
        outputFile.closeArchiveEntry();

        Log.e(TAG, inputFile.getName() + " has compress successfully");

    }

    public void doUnArchiver(File srcfile, String destpath, String password) throws IOException {

    }
}