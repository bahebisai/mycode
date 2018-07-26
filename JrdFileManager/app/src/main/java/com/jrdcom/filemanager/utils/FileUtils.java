package com.jrdcom.filemanager.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.listener.OperationEventListener;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.manager.MountManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    private static FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();

    private static final int UNIT_INTERVAL = 1024;

    private static final double ROUNDING_OFF = 0.05;
    private static final int DECIMAL_NUMBER = 10;


    private static Map<String, String> mimeTypeMap;
    private static Map<String, String> audioMimeTypeMap;
    private static Map<String, String> imageMimeTypeMap;
    private static Map<String, String> videoMimeTypeMap;
    private static Map<String, String> achiveMimeTypeMap;
    private static Map<String, String> docMimeTypeMap;

    public static Map<String, String> drmTypeMap;

    public static List<String> multiMIMETypeList = new ArrayList<String>();
    public static List<String> multiSelectMIMETypeList = new ArrayList<String>();


    static {
        mimeTypeMap = new HashMap<String, String>();
        drmTypeMap = new HashMap<String, String>();
        drmTypeMap.put("dcf", "*/*");
        drmTypeMap.put("dm", "application/vnd.oma.drm.message");
        audioMimeTypeMap = new HashMap<String, String>();
        audioMimeTypeMap.put("mp3", "audio/mp3");
        audioMimeTypeMap.put("wav", "audio/x-wav");
        audioMimeTypeMap.put("ogg", "audio/ogg");
        audioMimeTypeMap.put("mid", "audio/midi");
        audioMimeTypeMap.put("spm", "audio/*");
        audioMimeTypeMap.put("wma", "audio/x-ms-wma");
        audioMimeTypeMap.put("amr", "audio/amr");
        audioMimeTypeMap.put("aac", "audio/aac");
        audioMimeTypeMap.put("m4a", "audio/mp4a-latm");
        audioMimeTypeMap.put("midi", "audio/midi");
        audioMimeTypeMap.put("awb", "audio/amr-wb");
        audioMimeTypeMap.put("mpga", "audio/mpeg");
        audioMimeTypeMap.put("xmf", "audio/xmf");
        audioMimeTypeMap.put("flac", "audio/flac");
        audioMimeTypeMap.put("imy", "audio/melody");
        audioMimeTypeMap.put("diff", "audio/*");
        audioMimeTypeMap.put("gsm", "audio/x-gsm");
        audioMimeTypeMap.put("ape", "audio/x-ape");
        audioMimeTypeMap.put("rm", "audio/mpeg");
        audioMimeTypeMap.put("qcp", "audio/qcelp");
        mimeTypeMap.putAll(audioMimeTypeMap);

        // for video
        videoMimeTypeMap = new HashMap<String, String>();
        videoMimeTypeMap.put("avi", "video/avi");
        videoMimeTypeMap.put("wmv", "video/x-ms-wmv");
        videoMimeTypeMap.put("mov", "video/quicktime");
        videoMimeTypeMap.put("rmvb", "video/*");
        videoMimeTypeMap.put("mp4", "video/mp4");
        videoMimeTypeMap.put("mpeg", "video/mpeg");
        videoMimeTypeMap.put("3gp", "video/3gpp");
        videoMimeTypeMap.put("3g2", "video/3gpp2");
        videoMimeTypeMap.put("flv", "video/x-flv");
        videoMimeTypeMap.put("m4v", "video/x-m4v");
        videoMimeTypeMap.put("mkv", "video/x-matroska");
        videoMimeTypeMap.put("mpg", "video/mpeg");
        videoMimeTypeMap.put("3gpp", "video/3gpp");
        videoMimeTypeMap.put("asf", "video/x-ms-asf");
        videoMimeTypeMap.put("webm", "video/x-matroska");
        videoMimeTypeMap.put("divx", "video/flv");
        mimeTypeMap.putAll(videoMimeTypeMap);

        // for image
        imageMimeTypeMap = new HashMap<String, String>();
        imageMimeTypeMap.put("png", "image/png");
        imageMimeTypeMap.put("jpg", "image/jpeg");
        imageMimeTypeMap.put("gif", "image/gif");
        imageMimeTypeMap.put("bmp", "image/x-ms-bmp");
        imageMimeTypeMap.put("jpeg", "image/jpeg");
        imageMimeTypeMap.put("dm", "application/vnd.oma.drm.message");
        imageMimeTypeMap.put("dcf", "*/*");
        imageMimeTypeMap.put("wbmp", "image/vnd.wap.wbmp");
        imageMimeTypeMap.put("webp", "image/webp");
        mimeTypeMap.putAll(imageMimeTypeMap);

        // for achive
        achiveMimeTypeMap = new HashMap<String, String>();
        achiveMimeTypeMap.put("sdp", "application/sdp");
        achiveMimeTypeMap.put("jar", "application/java-archive");
        achiveMimeTypeMap.put("jad", "application/java-archive");
        achiveMimeTypeMap.put("zip", "application/zip");
        achiveMimeTypeMap.put("rar", "application/x-rar-compressed");
        achiveMimeTypeMap.put("tar", "application/x-tar");
        achiveMimeTypeMap.put("7z", "application/x-7z-compressed");
        achiveMimeTypeMap.put("gz", "application/x-gzip");
        mimeTypeMap.putAll(achiveMimeTypeMap);

        // for application
        mimeTypeMap.put("apk", "application/vnd.android.package-archive");

        // for doc
        docMimeTypeMap = new HashMap<String, String>();
        docMimeTypeMap.put("pdf", "application/pdf");
        docMimeTypeMap.put("doc", "application/msword");
        docMimeTypeMap.put("xls", "application/vnd.ms-excel");
        docMimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
        docMimeTypeMap.put("docx", "application/msword");
        docMimeTypeMap.put("xlsx", "application/vnd.ms-excel");
        docMimeTypeMap.put("pptx", "application/vnd.ms-powerpoint");
        docMimeTypeMap.put("xlsm", "application/vnd.ms-excel");
        docMimeTypeMap.put("vcs", "text/x-vcalendar");
        docMimeTypeMap.put("ics", "text/calendar");
        mimeTypeMap.putAll(docMimeTypeMap);

        // for webfile
        mimeTypeMap.put("htm", "text/html");
        mimeTypeMap.put("html", "text/html");
        mimeTypeMap.put("xml", "text/html");
        mimeTypeMap.put("php", "application/vnd.wap.xhtml+xml");
        mimeTypeMap.put("url", "text/html");

        // for text
        mimeTypeMap.put("rc", "text/plain");
        mimeTypeMap.put("sh", "text/plain");
        mimeTypeMap.put("vcf", "text/x-vcard");
        mimeTypeMap.put("ICZ", "text/calendar");
        mimeTypeMap.put("txt", "text/plain");
        mimeTypeMap.put("log", "text/plain");
        mimeTypeMap.put("dat", "text/plain");
        mimeTypeMap.put("ini", "application/octet-stream");
        mimeTypeMap.put("eml", "application/eml");
        mimeTypeMap.put("rtf", "application/msword");
        mimeTypeMap.put("keynote", "application/vnd.ms-powerpoint");
        mimeTypeMap.put("numbers", "application/vnd.ms-powerpoint");
        mimeTypeMap.put("xmind","application/xmind");
        mimeTypeMap.put("tif","image/tiff");


        multiMIMETypeList.add("3gp");
        multiMIMETypeList.add("dcf");
        multiMIMETypeList.add("dm");
        multiMIMETypeList.add("ogg");
        multiMIMETypeList.add("3gpp");

        multiSelectMIMETypeList.add("3gp");
        multiSelectMIMETypeList.add("dcf");
        multiSelectMIMETypeList.add("dm");
        multiSelectMIMETypeList.add("ogg");
        multiSelectMIMETypeList.add("3gpp");
        multiSelectMIMETypeList.add("mp4");

    }

    // get file category
    public static int getFileCategory(String mFileName) {
        String mFileExtendName = getFileExtension(mFileName);
        if (mFileExtendName == null) {
            return CommonIdentity.UNKNOWN_TASK;
        }
        if (videoMimeTypeMap.containsKey(mFileExtendName)) {
            return CommonIdentity.CATEGORY_VEDIOS;
        } else if (audioMimeTypeMap.containsKey(mFileExtendName)) {
            return CommonIdentity.CATEGORY_MUSIC;
        } else if (imageMimeTypeMap.containsKey(mFileExtendName)) {
            return CommonIdentity.CATEGORY_PICTURES;
        } else if (mFileExtendName.equals("apk")) {
            return CommonIdentity.CATEGORY_APKS;
        } else if (docMimeTypeMap.containsKey(mFileExtendName)) {
            return CommonIdentity.CATEGORY_DOCS;
        } else if (achiveMimeTypeMap.containsKey(mFileExtendName)) {
            return CommonIdentity.CATEGORY_ARCHIVES;
        }
        return CommonIdentity.UNKNOWN_TASK;
    }

    /**
     * This method check the file name is valid.
     *
     * @param fileName the input file name
     * @return valid or the invalid type
     */
    public static int checkFileName(String fileName) {
        if (TextUtils.isEmpty(fileName) || fileName.trim().length() == 0) {
            return OperationEventListener.ERROR_CODE_NAME_EMPTY;
        } else {
            try {
                int length = fileName.getBytes("UTF-8").length;
                if (length > CommonIdentity.FILENAME_MAX_LENGTH) {
                    return OperationEventListener.ERROR_CODE_NAME_TOO_LONG;
                } else {
                    return OperationEventListener.ERROR_CODE_NAME_VALID;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return OperationEventListener.ERROR_CODE_NAME_EMPTY;
            }
        }
    }

    /**
     * This method gets extension of certain file.
     *
     * @param fileName name of a file
     * @return Extension of the file's name
     */
    public static String getFileExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String extension = null;
        final int lastDot = fileName.lastIndexOf('.');
        if ((lastDot > 0)) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        return extension;
    }

    /**
     * This method gets name of certain file from its path.
     *
     * @param absolutePath the file's absolute path
     * @return name of the file
     */
    public static String getFileName(String absolutePath) {
        int sepIndex = absolutePath.lastIndexOf(MountManager.SEPARATOR);
        if (sepIndex >= 0) {
            return absolutePath.substring(sepIndex + 1);
        }
        return absolutePath;

    }

    /**
     * This method gets path to directory of certain file(or folder).
     *
     * @param filePath path to certain file
     * @return path to directory of the file
     */
    public static String getFilePath(String filePath) {
        int sepIndex = filePath.lastIndexOf(MountManager.SEPARATOR);
        if (sepIndex >= 0) {
            return filePath.substring(0, sepIndex);
        }
        return "";

    }

    /**
     * This method generates a new suffix if a name conflict occurs, ex: paste a
     * file named "stars.txt", the target file name would be "stars(1).txt"
     *
     * @param file the conflict file
     * @return a new name for the conflict file
     */
    public static File genrateNextNewName(File file) {
        String parentDir = file.getParent();
        String fileName = file.getName();
        String ext = "";
        int newNumber = 0;
        if (file.isFile()) {
            int extIndex = fileName.lastIndexOf(".");
            if (extIndex != -1) {
                ext = fileName.substring(extIndex);
                fileName = fileName.substring(0, extIndex);
            }
        }

        if (fileName.endsWith(")")) {
            int leftBracketIndex = fileName.lastIndexOf("(");
            if (leftBracketIndex != -1) {
                String numeric = fileName.substring(leftBracketIndex + 1, fileName.length() - 1);
                if (numeric.matches("[0-9]+")) {
                    try {
                        newNumber = Integer.parseInt(numeric);
                        newNumber++;
                        fileName = fileName.substring(0, leftBracketIndex);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        String suffix = "(" + newNumber + ")";
        sb.append(fileName).append(suffix).append(ext);
        int ret = FileUtils.checkFileName(sb.toString());
        if (ret < 0) {
            if(ret == OperationEventListener.ERROR_CODE_NAME_TOO_LONG){
                // If the name is too long, replace the several lastest characters
                int length =  fileName.length();
                int suffixLength = suffix.length();
                if (length > suffixLength) {
                    StringBuffer fileN = new StringBuffer(fileName.substring(0, length - suffixLength));
                    return new File(parentDir, fileN.append(suffix).append(ext).toString());
                }
                return null;
            }
            return null;
        }
        return new File(parentDir, sb.toString());
    }

    /**
     * This method converts a size to a string
     *
     * @param size the size of a file
     * @return the string represents the size
     */
    public static String sizeToString(Context context, long size) {
        String UNIT_B = context.getString(R.string.unit_B);
        String UNIT_KB = context.getString(R.string.unit_KB);
        String UNIT_MB = context.getString(R.string.unit_MB);
        String UNIT_GB = context.getString(R.string.unit_GB);
        String UNIT_TB = context.getString(R.string.unit_TB);

        String unit = UNIT_B;
        if (size < DECIMAL_NUMBER) {
            return Long.toString(size) + " " + unit;
        }
        unit = UNIT_KB;
        double sizeDouble = (double) size / (double) UNIT_INTERVAL;
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_MB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_GB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_TB;
        }

        // Add 0.005 for rounding-off.
        long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);

        double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

        if (formatedSize <= 0) {
            return "0" + " " + unit;
        } else {
            return String.format("%.1f",formatedSize) + " " + unit;
        }
    }

    /**
     * This method checks weather extension of certain file(not folder) is
     * changed.
     *
     * @param newFilePath path to file before modified.(Here modify means
     *                    rename).
     * @param oldFilePath path to file after modified.
     * @return true for extension changed, false for not changed.
     */
    public static boolean isExtensionChange(String newFilePath, String oldFilePath) {
        File oldFile = new File(oldFilePath);
        if (oldFile.isDirectory()) {
            return false;
        }
        String origFileExtension = FileUtils.getFileExtension(oldFilePath);
        String newFileExtension = FileUtils.getFileExtension(newFilePath);
        if (((origFileExtension != null) && (!origFileExtension.equals(newFileExtension)))
                || ((newFileExtension != null) && (!newFileExtension.equals(origFileExtension)))) {
            return true;
        }
        return false;
    }

    /**
     * This method checks weather file name start with ".".
     *
     * @param newFilePath path to file before modified.(Here modify means
     *                    rename).
     * @param oldFilePath path to file after modified.
     * @return true for extension changed, false for not changed.
     */
    public static boolean isStartWithDot(String newFilePath, String oldFilePath) {
        boolean oldStartWithDot = FileUtils.getFileName(oldFilePath).startsWith(".");
        boolean newStartWithDot = FileUtils.getFileName(newFilePath).startsWith(".");

        if (!oldStartWithDot && newStartWithDot) {
            return true;
        } else {
            return false;
        }
    }

    private static final String[] THUMB_PROJECTION = new String[]{
            Thumbnails._ID,
            Thumbnails.DATA,
    };

    public static Drawable queryThumbnail(Context context, FileInfo fileInfo, String mimeType, boolean isDrm,boolean isRightsStatus,String drmOriginalType,DrmManager drmManager) {
        if (fileInfo.getFile().isDirectory()) {
            return null;
        }
        Bitmap bitmap = null;
        if (isDrm) {
            String path = fileInfo.getFileAbsolutePath();
            if (drmOriginalType != null && !drmOriginalType.isEmpty()) {
                mimeType = drmOriginalType;
            }
            if (mimeType.contains(CommonIdentity.MIME_HAED_IMAGE)) {
                Options op = new Options();
                bitmap = drmManager.getDrmRealThumbnail(path, op,
                        context.getResources().getDimensionPixelSize(R.dimen.drm_icon_width));
            } else if (mimeType.contains(CommonIdentity.MIME_HEAD_VIDEO)) {
                bitmap = drmManager.getDrmVideoThumbnail(FileUtils.getVideoThumbnail(fileInfo),
                        path, context.getResources().getDimensionPixelSize(R.dimen.drm_icon_width));
            } else if (mimeType.contains(CommonIdentity.MIME_HEAD_AUDIO)) {
                bitmap = drmManager.getDrmThumbnail(path,
                        context.getResources().getDimensionPixelSize(R.dimen.drm_icon_width));
            }
            if (DrmManager.mCurrentDrm == 10) {
                bitmap = drawLockIcon(context, bitmap, isRightsStatus, mimeType);
            }
        } else {
            if (mimeType == null) {
                return null;
            }
            if (mimeType.contains(CommonIdentity.MIME_HAED_IMAGE)) {
                bitmap = getImageThumbnail(context, fileInfo);
            } else if (mimeType.contains(CommonIdentity.MIME_HEAD_VIDEO) ||
                    mimeType.contains(CommonIdentity.MIMETYPE_3GPP_VIDEO) ||
                    mimeType.contains(CommonIdentity.MIMETYPE_3GPP2_VIDEO) ||
                    mimeType.contains(CommonIdentity.MIMETYPE_3GPP_UNKONW)) {
                bitmap = getVideoThumbnail(fileInfo);
            } else if (mimeType.contains(CommonIdentity.MIME_HEAD_AUDIO)) {
                bitmap = getAudioThumbnail(context, fileInfo);
            } else if (mimeType.contains(CommonIdentity.MIMETYPE_APK)) {
                bitmap = getAPKThumbnail(context, fileInfo);
            }
        }
        Drawable drawable = centerSquareScaleBitmap(context.getResources(), bitmap, 135);
        return drawable;

    }

    private static Bitmap readBitmap(Context context, int resId) {
        Options opt = new Options();
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private static Bitmap drawLockIcon(Context context, Bitmap sourceBitmap, boolean isRightValid, String mimeType) {
        Bitmap lockBitmap = null;
        if (sourceBitmap == null) {
            if (isRightValid) {
                sourceBitmap = readBitmap(context, R.drawable.ic_drm_black);
            } else {
                sourceBitmap = readBitmap(context, R.drawable.ic_drm_red);
            }
        }
        float outSideSize = context.getResources().getDimension(R.dimen.nail_size);
        int lockiconleft = context.getResources().getDimensionPixelSize(R.dimen.drm_lock_icon_left);
        int sourceW = sourceBitmap.getWidth();
        int sourceH = sourceBitmap.getHeight();
        Bitmap bitmap = null;
        Bitmap newLock = null;
        try{
            /*
            * When the original image is too large,
            * the original image to zoom in, zoom ratio
            * is compared with the size of the imageview.
            * */
            Matrix matrix = new Matrix();
            matrix.postScale(outSideSize/((float)sourceW),outSideSize/((float)sourceH));
            bitmap = Bitmap.createBitmap(sourceBitmap,0,0,sourceW,sourceH,matrix,true);
            if (isRightValid) {
                lockBitmap = readBitmap(context, R.drawable.drm_green_lock);
            } else {
                lockBitmap = readBitmap(context, R.drawable.drm_red_lock);
            }
//            int lockW = lockBitmap.getWidth();
//            int lockH = lockBitmap.getHeight();
            newLock = Bitmap.createBitmap((int)outSideSize, (int)outSideSize, Config.ARGB_8888);
            Canvas canvas = new Canvas(newLock);
            canvas.drawBitmap(bitmap, 0, 0, null);
            canvas.drawBitmap(lockBitmap, lockiconleft, lockiconleft, null);

        } catch(Exception e){

        }

        return newLock;
    }

    /**
     * @param bitmap     bitmap
     * @param edgeLength szie
     * @return BitmapDrawable
     */
    public static BitmapDrawable centerSquareScaleBitmap(Resources resources, Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        BitmapDrawable bitmapDrawable = new BitmapDrawable(resources, bitmap);
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;
            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;

            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                bitmapDrawable = new BitmapDrawable(resources, result);
                scaledBitmap.recycle();
            } catch (Exception e) {
                if (result != null) result.recycle();
                return null;
            }
        }
        return bitmapDrawable;
    }

    public static Bitmap getImageThumbnail(Context context, FileInfo fileInfo) {
        ContentResolver contentResolver = context.getContentResolver();
        Bitmap bitmap = null;
        String filePath = fileInfo.getFile().getPath();
        String[] projection = {Media.DATA, Media._ID,};
        String whereClause = Media.DATA + " = '" + filePath + "'";
        Cursor cursor = contentResolver.query(Media.EXTERNAL_CONTENT_URI, projection, whereClause,
                null, null);
        try {
            int _id = 0;
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    int _idColumn = cursor.getColumnIndex(Media._ID);
                    _id = cursor.getInt(_idColumn);
                }

                Options options = new Options();
                options.inDither = false;
                options.inPreferredConfig = Config.RGB_565;
                bitmap = Thumbnails.getThumbnail(contentResolver, _id, Thumbnails.MINI_KIND,
                        options);
                if (bitmap == null) {
                    File file = new File(filePath);
                    bitmap = makeBitmapByStream(CommonIdentity.MINI_THUMB_TARGET_SIZE, CommonIdentity.MINI_THUMB_MAX_NUM_PIXELS,
                            file);
                }

            } else {
                File file = new File(filePath);
                bitmap = makeBitmapByStream(CommonIdentity.MINI_THUMB_TARGET_SIZE, CommonIdentity.MINI_THUMB_MAX_NUM_PIXELS,
                        file);
            }

        } catch (Exception e) {
            LogUtils.d(TAG, "Exception occured when getImageThumbnail():", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bitmap;
    }

    private static ParcelFileDescriptor makeInputStream(Uri uri, ContentResolver cr) {
        try {
            return cr.openFileDescriptor(uri, "r");
        } catch (IOException ex) {
            return null;
        }
    }

    private static Bitmap makeBitmapByStream(int minSideLength, int maxNumOfPixels, File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        Bitmap bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            Options options = new Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, options);
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                return null;
            }
            options.inSampleSize = computeSampleSize(
                    options, minSideLength, maxNumOfPixels);
            options.inJustDecodeBounds = false;

            options.inDither = false;
            options.inPreferredConfig = Config.ARGB_8888;
            stream = new FileInputStream(file);
            bm = BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception e) {
            //do nothing.
            //If the exception happened on open, bm will be null.
            Log.e(TAG, "Unable to decode stream: " + e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // do nothing here
                }
            }
        }
        return bm;
    }


    private static void closeSilently(ParcelFileDescriptor c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    private static int computeSampleSize(Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == CommonIdentity.UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == CommonIdentity.UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == CommonIdentity.UNCONSTRAINED) &&
                (minSideLength == CommonIdentity.UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == CommonIdentity.UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap getVideoThumbnail(FileInfo fileInfo) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(fileInfo.getFile().getPath());
            synchronized (MediaMetadataRetriever.class) {//add for PR997015 by yane.wang@jrdcom.com 20150508
                bitmap = retriever.getFrameAtTime();
                bitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false);//add for PR824285 by yane.wang@jrdcom.com 20141106
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } catch(Exception ex){
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }


    private static Bitmap getAPKThumbnail(Context context, FileInfo fileInfo) {
        String apkPath = fileInfo.getFile().getPath();
        File file = fileInfo.getFile();
        if (!file.exists()) {
            return null;
        }
        Bitmap icon = null;
        final String PATH_PackageParser = "android.content.pm.PackageParser";
        Method assetMag_close = null;
        Object assetMag = null;
        try {
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = null;
            Object pkgParser = null;
            Object[] valueArgs = new Object[1];
            try {
                pkgParserCt = pkgParserCls.getConstructor();
                pkgParser = pkgParserCt.newInstance();
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                try {
                    pkgParserCt = pkgParserCls.getConstructor(typeArgs);
                    valueArgs[0] = apkPath;
                    pkgParser = pkgParserCt.newInstance(valueArgs);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            if (pkgParserCt == null || pkgParser == null) {
                return null;
            }
            Method pkgParser_parsePackageMtd = null;

            try {
                typeArgs = new Class[2];
                typeArgs[0] = File.class;
                typeArgs[1] = Integer.TYPE;
                pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
                valueArgs = new Object[2];
                valueArgs[0] = file;
                valueArgs[1] = 0;
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
                try {
                    typeArgs = new Class[4];
                    typeArgs[0] = File.class;
                    typeArgs[1] = String.class;
                    typeArgs[2] = DisplayMetrics.class;
                    typeArgs[3] = Integer.TYPE;
                    pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
                    DisplayMetrics metrics = new DisplayMetrics();
                    metrics.setToDefaults();
                    valueArgs = new Object[4];
                    valueArgs[0] = new File(apkPath);
                    valueArgs[1] = apkPath;
                    valueArgs[2] = metrics;
                    valueArgs[3] = 0;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            if (pkgParser_parsePackageMtd == null) {
                return null;
            }

            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
            Class assetMagCls = Class.forName("android.content.res.AssetManager");
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            assetMag = assetMagCt.newInstance((Object[]) null);
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            assetMag_close = assetMagCls.getDeclaredMethod("close");
            Resources res = context.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            if (info != null && info.icon != 0) {
            /* MODIFIED-END by wenjing.ni,BUG-2011884*/
                Options opts = new Options();
                opts.inPreferredConfig = Config.ARGB_8888;
                try {
                    icon = BitmapFactory.decodeResource(res, info.icon, opts);
                } catch (OutOfMemoryError e) {
                    opts.inSampleSize = 2;
                    icon = BitmapFactory.decodeResource(res, info.icon, opts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assetMag_close.invoke(assetMag);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return icon;
    }

    /**
     * Add an additional set of assets to the asset manager. This can be either
     * a directory or ZIP file. Not for use by applications. Returns the cookie
     * of the added asset, or 0 on failure. {@hide}
     */
    private final int addAssetPath(String path) {
        int res = addAssetPathNative(path);
        return res;
    }

    private native final int addAssetPathNative(String path);

    private static Bitmap getAudioThumbnail(Context context, FileInfo fileInfo) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(fileInfo.getFile().getPath());
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null && art.length > 0) {
                Options op = new Options();
                op.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(art, 0, art.length, op);
                float scale = calculateScale(128, 128, op.outWidth, op.outHeight);//add for PR824285 by yane.wang@jrdcom.com 20141106
                int inSampleSize = Math.round((1 / scale));
                if (inSampleSize > scale) {
                    inSampleSize -= 1;
                }
                op.inSampleSize = inSampleSize;
                op.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length, op);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private static float calculateScale(int destWidth, int destHeight,
                                        int originWidth, int originHeight) {
        float scale = 1.0f;
        if (destHeight * originWidth <= originHeight * destWidth) {
            scale = (float) destHeight / originHeight;
        } else {
            scale = (float) destWidth / originWidth;
        }
        return scale;
    }

    public static String getMimeTypeByExt(String fileName) {
        String mExtensionName = getFileExtension(fileName);
        if (TextUtils.isEmpty(mExtensionName)) {
            return CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
        }
        if(multiMIMETypeList.contains(mExtensionName) && canUseMediaMetadataRetriever(fileName,mExtensionName)){
             String mMime = getMediaMetadataMIME(fileName);
             if(mMime != null){
                 return mMime;
             }
        }
        return mimeTypeMap.get(mExtensionName);
    }

    public static String getSelectMimeTypeByExt(String fileName) {
        String mExtensionName = getFileExtension(fileName);
        if (TextUtils.isEmpty(mExtensionName)) {
            return CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
        }
        if(multiSelectMIMETypeList.contains(mExtensionName) && canUseMediaMetadataRetriever(fileName,mExtensionName)){
            String mMime = getMediaMetadataMIME(fileName);
            if(mMime != null){
                return mMime;
            }
        }
        return mimeTypeMap.get(mExtensionName);
    }

    public static String getMimeTypeByExtensionName(String fileName) {
        String mExtensionName = getFileExtension(fileName);
        return mimeTypeMap.get(mExtensionName);
    }

    public static String getMimeTypeByExtension(String mExtension) {
        return mimeTypeMap.get(mExtension);
    }

    public static String getAudioMimeType(String mime) {
        if (mime.equals("mp3")) {
            return "audio/mp3";
        } else if (mime.equals("ogg")) {
            return "audio/ogg";
        } else if (mime.equals("wma")) {
            return "audio/x-ms-wma";
        } else if (mime.equals("awb")) {
            return "audio/amr-wb";
        } else if (mime.equals("aac") || mime.equals("m4a")) {
            return "audio/*";
        } else {
            return mime;
        }
    }

    /**
     * This method converts a size to a string
     *
     * @param size the size of a file
     * @return the string represents the size
     */
    public static String safeFileSizeToString(Context context, long progress, long max) {


        String maxInfo = sizeToString(context, max);


        String UNIT_B = context.getString(R.string.unit_B);
        String UNIT_KB = context.getString(R.string.unit_KB);
        String UNIT_MB = context.getString(R.string.unit_MB);
        String UNIT_GB = context.getString(R.string.unit_GB);
        String UNIT_TB = context.getString(R.string.unit_TB);
        String unit = UNIT_B;
        if (max < DECIMAL_NUMBER) {
            unit = UNIT_B;
        } else {
            unit = UNIT_KB;
            double sizeDouble = (double) max / (double) UNIT_INTERVAL;
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_MB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_GB;
            }
            if (sizeDouble > UNIT_INTERVAL) {
                sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
                unit = UNIT_TB;
            }
        }

        if (unit.equals(UNIT_B)) {

            return Long.toString(progress) + UNIT_B + "/" + maxInfo;

        } else {
            double sizeDouble = 0d;
            if (unit.equals(UNIT_KB)) {
                unit = UNIT_KB;
                sizeDouble = (double) progress / (double) UNIT_INTERVAL;
            } else if (unit.equals(UNIT_MB)) {
                unit = UNIT_MB;
                sizeDouble = (double) progress / ((double) UNIT_INTERVAL * (double) UNIT_INTERVAL);
            } else if (unit.equals(UNIT_GB)) {
                unit = UNIT_GB;
                sizeDouble = (double) progress / ((double) UNIT_INTERVAL * (double) UNIT_INTERVAL * (double) UNIT_INTERVAL);
            } else if (unit.equals(UNIT_TB)) {
                unit = UNIT_TB;
                sizeDouble = (double) progress / ((double) UNIT_INTERVAL * (double) UNIT_INTERVAL * (double) UNIT_INTERVAL * (double) UNIT_INTERVAL);
            }
            long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);

            double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

            if (formatedSize == 0) {
                return "0" + " " + unit;
            } else {
                return Double.toString(formatedSize) + unit + "/" + maxInfo;
            }
        }


    }

    public static String getSafeMIME(String mFileName) {
        try {
            String extendsName = mFileName.substring(mFileName.lastIndexOf(".") + 1, mFileName.length());
            return mimeTypeMap.get(extendsName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "unknown";
    }

    public static FileInfo createFileInfo(Context mContext, File file, String internalPath, String originPath, String mFileMime, boolean isHideFile, boolean isDRM) {
        FileInfo fileInfo = new FileInfo(mContext, file.isDirectory(), originPath, file.getAbsolutePath().replace(internalPath, originPath));
        fileInfo.updateSizeAndLastModifiedTime(file);
        fileInfo.setFileMime(mFileMime);
        fileInfo.setDrm(isDRM);
        fileInfo.setHideFile(isHideFile);
        return fileInfo;
    }

    public static FileInfo createFileInfo(Context mContext, File file, String internalPath, String originPath, String mFileMime, boolean isHideFile, boolean isDRM,boolean isPrivate) {
        FileInfo fileInfo = new FileInfo(mContext, file.isDirectory(), originPath, file.getAbsolutePath().replace(internalPath, originPath));
        fileInfo.updateSizeAndLastModifiedTime(file);
        fileInfo.setFileMime(mFileMime);
        fileInfo.setDrm(isDRM);
        fileInfo.setHideFile(isHideFile);
        fileInfo.setPrivateFile(isPrivate);
        return fileInfo;
    }

    /**
     * To get the lage files.
     *
     * @param context the environment.
     * @return the info to show.
     */
    public static long getFilesCount(Context context, String dirPath) {

        long count = -1;
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = {"count(*)"};

        StringBuilder sb = new StringBuilder();
        sb.append(MediaStore.Files.FileColumns.DATA + " like " + "\"" + dirPath + "/%\" ");
        sb.append("and " + MediaStore.Files.FileColumns.DATA + " not like " + "\"" + dirPath + "/%/%\"");

        String selection = sb.toString();
        Log.e(TAG, "SQL selection is: " + selection);

        try {
            Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null);
            count = cursor.getCount();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    count = cursor.getLong(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "getFilesCount Failed: " + e);
        }
        return count;
    }

    public static Uri getContentUri(File mFile, FileInfoManager mFileInfoManager, String fileType,boolean isDrm) {

        Uri uri = Uri.fromFile(mFile);
        /**
         * Due to the copyright and limits to DRM files need to look at, support the application of DRM need to file the absolute path.
         */
        if (CommonUtils.hasN() && !isDrm) {
            try {
                uri = FileProvider.getUriForFile(mFileInfoManager.mContext, "com.jrdcom.filemanager.fileProvider", mFile);
            }catch(Exception e){
                e.printStackTrace();
            }
            if (uri != null) {
                return uri;
            }
        }

        if ("file".equals(uri.getScheme()) && !TextUtils.isEmpty(fileType)) {
            if (fileType.startsWith("video/")) {
                uri = queryMedia(
                        uri.toString().substring(7, uri.toString().length()), "video", mFileInfoManager);
            } else if (fileType.startsWith("audio/")) {
                uri = queryMedia(
                        uri.toString().substring(7, uri.toString().length()), "audio", mFileInfoManager);
            } else if (fileType.startsWith("image/")) {
                uri = queryMedia(
                        uri.toString().substring(7, uri.toString().length()), "images", mFileInfoManager);
            } else if (!CommonUtils.hasBelowN()) {
                uri = queryFile(
                        uri.toString().substring(7, uri.toString().length()), mFileInfoManager);
            }
        }

        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            uri = Uri.fromFile(mFile);
        }
        return uri;
    }

    public static Uri getMediaContentUri(File mFile, FileInfoManager mFileInfoManager, String fileType) {

        Uri uri = Uri.fromFile(mFile);
        if ("file".equals(uri.getScheme()) && !TextUtils.isEmpty(fileType)) {
            if (fileType.startsWith("video/")) {
                uri = queryMedia(
                        uri.toString().substring(7, uri.toString().length()), "video", mFileInfoManager);
            } else if (fileType.startsWith("audio/")) {
                uri = queryMedia(
                        uri.toString().substring(7, uri.toString().length()), "audio", mFileInfoManager);
            } else if (fileType.startsWith("image/")) {
                uri = queryMedia(
                        uri.toString().substring(7, uri.toString().length()), "images", mFileInfoManager);
            } else if (!CommonUtils.hasBelowN()) {
                uri = queryFile(
                        uri.toString().substring(7, uri.toString().length()), mFileInfoManager);
            }
        }

        if(uri == null || TextUtils.isEmpty(uri.toString())){
            try {
                uri = FileProvider.getUriForFile(mFileInfoManager.mContext, "com.jrdcom.filemanager.fileProvider", mFile);
            }catch(Exception e){
                e.printStackTrace();
            }
            if (uri != null) {
                return uri;
            }
        }

        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            uri = Uri.fromFile(mFile);
        }
        return uri;
    }

    public static Uri queryFile(String mediaPath, FileInfoManager mFileInfoManager) {
        String StringUri = null;
        ContentResolver mContentResolver = mFileInfoManager.mContext.getContentResolver();
        StringUri = "content://media/external/file";
        String searchString = mediaPath;
        Cursor c = null;
        try {
            searchString = Uri.decode(searchString).trim().replace("'", "''");

            Uri uri = Uri.parse(StringUri);
            c = mContentResolver.query(uri, new String[]{
                    "_id"
            }, "_data='" + searchString + "'", null, null);
            if (c != null && c.moveToNext()) {
                int id = c.getInt(0);
                return Uri.withAppendedPath(uri, String.valueOf(id));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    public static Uri queryMedia(String mediaPath, String path, FileInfoManager mFileInfoManager) {
        String StringUri = null;
        ContentResolver mContentResolver = mFileInfoManager.mContext.getContentResolver();
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        StringUri = "content://media/external/" + path + "/media";
        String searchString = mediaPath;
        Cursor c = null;
        try {
            searchString = Uri.decode(searchString).trim().replace("'", "''");

            Uri uri = Uri.parse(StringUri);
            c = mContentResolver.query(uri, new String[]{
                    "_id"
            }, "_data='" + searchString + "'", null, null);
            if (c != null && c.moveToNext()) {
                int id = c.getInt(0);
                return Uri.withAppendedPath(uri, String.valueOf(id));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }


    public static String getShareMimeType(Context mContext, String mMimeType, boolean isDir, boolean isdrm, String path) {

        if (TextUtils.isEmpty(mMimeType) && !isDir) {
            if (isdrm) {
                mMimeType = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                if (!TextUtils.isEmpty(mMimeType)) {
                    return mMimeType;
                }
            }

            ContentResolver resolver = mContext.getContentResolver();
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[]{
                    MediaStore.Files.FileColumns.MIME_TYPE
            };
            final String selection = MediaStore.Files.FileColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{
                    path
            };
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mMimeType = cursor.getString(0);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        return mMimeType;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            mMimeType = FileUtils.getMimeTypeByExt(path);
            if (TextUtils.isEmpty(mMimeType)) {
                mMimeType = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
            }

            if (mMimeType.equals("application/ogg") || mMimeType.equals("application/x-ogg")) {
                mMimeType = "audio/ogg";
            }
        } else if (!TextUtils.isEmpty(mMimeType) && !isDir) {
            if (mMimeType.equals("application/ogg") || mMimeType.equals("application/x-ogg")) {
                mMimeType = "audio/ogg";
            }
        }
        return mMimeType;
    }

    public static String getSingleFileMIME(Context mContext, FileInfo fileInfo,boolean isNormalName){
        String mMimeType=null;
        String path = fileInfo.getFileAbsolutePath();
        if (!fileInfo.isDirectory()) {
            if (fileInfo.isDrm()) {
                mMimeType = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                if (!TextUtils.isEmpty(mMimeType)) {
                    return mMimeType;
                }
            }
            ContentResolver resolver = mContext.getContentResolver();
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[]{MediaStore.Files.FileColumns.MIME_TYPE};
            final String selection = MediaStore.Files.FileColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{path};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mMimeType = cursor.getString(0);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        return mMimeType;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (isNormalName && TextUtils.isEmpty(mMimeType)&&canUseMediaMetadataRetriever(path)) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(path);
                    mMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                } catch (Exception e) {
                    LogUtils.d(TAG, "Exception occured when getMIME():"+path, e);
                }
                if(!TextUtils.isEmpty(mMimeType))return mMimeType;
            }
            mMimeType = FileUtils.getSelectMimeTypeByExt(path);
            if (TextUtils.isEmpty(mMimeType)) {
                mMimeType = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
            }
        }
        return mMimeType;
    }

   public static String getMediaMetadataMIME(String path){
       String mMimeType = null;
       MediaMetadataRetriever mmr = new MediaMetadataRetriever();
       try {
           mmr.setDataSource(path);
           mMimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
       } catch (Exception e) {
           LogUtils.d(TAG, "Exception occured when getMIME():"+path, e);
       }
       return mMimeType;
   }

    public static String getMIME(Context mContext, String mMimeType, String path, boolean isdir, boolean isDrm) {
        if (TextUtils.isEmpty(mMimeType) && !isdir) {
            if (isDrm) {
                mMimeType = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                if (!TextUtils.isEmpty(mMimeType)) {
                    return mMimeType;
                }
            }

            ContentResolver resolver = mContext.getContentResolver();
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[]{MediaStore.Files.FileColumns.MIME_TYPE};
            final String selection = MediaStore.Files.FileColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{path};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mMimeType = cursor.getString(0);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        return mMimeType;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            mMimeType = FileUtils.getMimeTypeByExt(path);
            if (TextUtils.isEmpty(mMimeType)) {
                mMimeType = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
            }
        }
        return mMimeType;
    }

    public static String getCategoryMIME(Context mContext, String mMimeType, String path, boolean isdir, boolean isDrm) {
        if (TextUtils.isEmpty(mMimeType) && !isdir) {
            if (isDrm) {
                mMimeType = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                if (!TextUtils.isEmpty(mMimeType)) {
                    return mMimeType;
                }
            }

            ContentResolver resolver = mContext.getContentResolver();
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[]{MediaStore.Files.FileColumns.MIME_TYPE};
            final String selection = MediaStore.Files.FileColumns.DATA + "=?";
            final String[] selectionArgs = new String[]{path};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    mMimeType = cursor.getString(0);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        return mMimeType;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            mMimeType = FileUtils.getMimeTypeByExt(path);
            if (TextUtils.isEmpty(mMimeType)) {
                mMimeType = CommonIdentity.MIMETYPE_EXTENSION_UNKONW;
            }
        }
        return mMimeType;
    }


    /**
     * get mimetypes of children of directory
     *
     * @param mContext
     * @param path
     * @return
     */
    public static Map<String, String> getMimeTypeMap(Context mContext, String path) {
        if (TextUtils.isEmpty(path)) return null;
        final Uri uri = MediaStore.Files.getContentUri("external");
        final String[] projection = new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE};
        final String selection = MediaStore.Files.FileColumns.DATA + " like" + " \'" + path + "%\'";
        final String[] selectionArgs = new String[]{path};
        Map<String, String> map = new HashMap<String, String>();
        Cursor cursor = null;
        try {
            ContentResolver resolver = mContext.getContentResolver();
            cursor = resolver.query(uri, projection, selection, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    String pathData = cursor.getString(0);
                    String mMimeType = cursor.getString(1);
                    if (!TextUtils.isEmpty(mMimeType)) {
                        map.put(pathData, mMimeType);
                    }
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return map;
    }

    public static String getMIMEType(boolean isDrm, Context mContext, String path, String mName) {
        String type = "*/*";
        String fName = mName;
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex > 0) {
            if (isDrm) {
                type = DrmManager.getInstance(mContext.getApplicationContext()).getOriginalMimeType(path);
                if (!TextUtils.isEmpty(type)) {
                    return type;
                }
            } else {
                ContentResolver resolver = mContext.getContentResolver();
                final Uri uri = MediaStore.Files.getContentUri("external");
                final String[] projection = new String[]{MediaStore.Files.FileColumns.MIME_TYPE};
                final String selection = MediaStore.Files.FileColumns.DATA + "=?";
                final String[] selectionArgs = new String[]{path};
                Cursor cursor = null;
                try {
                    cursor = resolver.query(uri, projection, selection, selectionArgs, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        type = cursor.getString(0);
                        if (!TextUtils.isEmpty(type)) {
                            return type;
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        return type;
    }

    /**
     * The method check the file is DRM file, or not.
     *
     * @return true for DRM file, false for not DRM file.
     */
    public static boolean isDrmFiles(Context mContext, String mAbsolutePath, boolean directory) {

        if (directory) {
            return false;
        }
        if (TextUtils.isEmpty(mAbsolutePath)) {
            return false;
        }
        boolean isDrm = false;
        String[] projection = {};
        ContentResolver resolver = mContext.getContentResolver();
        final Uri uri = MediaStore.Files.getContentUri("external");
        if (CommonUtils.isDRMColumn(mContext)) {
            if (isQcomDrm()) {
                projection = new String[]{"tct_is_drm", "tct_drm_type", "tct_drm_right_type"};
            } else if (isMTKDrm()) {
                projection = new String[]{"is_drm"};
            }
        } else {
            return false;
        }
        final String selection = MediaStore.Files.FileColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{mAbsolutePath};
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                if (isQcomDrm()) {
                    int Drm = cursor.getInt(cursor.getColumnIndex("tct_is_drm"));
                    int DrmType = cursor.getInt(cursor.getColumnIndex("tct_drm_type"));
                    int Drm_right_type = cursor.getInt(cursor.getColumnIndex("tct_drm_right_type"));
                    if (Drm == 0) {
                        isDrm = false;
                    } else if (Drm == 1) {
                        isDrm = true;
                    }
                } else if (isMTKDrm()) {
                    int mtkDrm = cursor.getInt(cursor.getColumnIndex("is_drm"));
                    if (mtkDrm == 0) {
                        isDrm = false;
                    } else if (mtkDrm == 1) {
                        isDrm = true;
                    }
                }
//                switch (DrmType){
//                    case 0:
//                        mDrmType =null;
//                        break;
//                    case 1:
//                        mDrmType ="tct fl drm-protected";
//                        break;
//                    case 2:
//                        mDrmType = "tct cd drm-protected";
//                        break;
//                    case 3:
//                        mDrmType = "tct sd drm-protected";
//                        break;
//                }
//                switch (Drm_right_type){
//                    case 0:
//                        mDrm_right_type = true;
//                        break;
//                    case 1:
//                        mDrm_right_type = false;
//                        break;
//                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isDrm;
    }

    private static boolean isQcomDrm() {
        try {
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

    private static boolean isMTKDrm() {
        try {
            Class<?> managerClass = Class.forName("com.mediatek.drm.OmaDrmClient");
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

    /**
     * The method check the file count when is_hidden or not.
     *
     * @return flies count
     */
    public static int isShowHideCount(File[] files) {
        if (files == null) return 0;

        int count = 0;
        if (!mApplication.isShowHidden) {
            for (int i = 0; i < files.length; i++) {
                if (!getFileName(files[i].getAbsolutePath()).startsWith(".") && files.length > 0) {
                    count = count + 1;
                }
            }
        } else {
            count = files.length;
        }
        return count;
    }

    /**
     * The method is to remove the file name suffix.
     *
     * @return Original flies name
     */
    public static String getOriginalFileName(String mFileName) {
        try {
            String mOriginalFileName = mFileName.substring(0, mFileName.lastIndexOf("."));
            return mOriginalFileName;
        } catch (Exception e) {

        }
        return System.currentTimeMillis() + "";
    }

    public static boolean canUseMediaMetadataRetriever(String fileName){
        String extension=getFileExtension(fileName);
        return extension!=null&&!"rm".equalsIgnoreCase(extension)&&!"rmvb".equalsIgnoreCase(extension);
    }

    public static boolean canUseMediaMetadataRetriever(String fileName,String extension){
        return extension!=null&&!"rm".equalsIgnoreCase(extension)&&!"rmvb".equalsIgnoreCase(extension);
    }

}
