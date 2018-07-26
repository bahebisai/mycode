package com.jrdcom.filemanager.utils;

import android.media.DecoderCapabilities;
import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.mtp.MtpConstants;

import java.util.HashMap;
import java.util.List;

/**
 * MediaScanner helper class. Extend the android.media.MediaFile
 */
public class MediaFile {

    // Audio file types
    public static final int FILE_TYPE_MP3 = 1;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_MKA = 9;
    public static final int FILE_TYPE_FLAC = 10;
    public static final int FILE_TYPE_3GPA = 11;
    public static final int FILE_TYPE_AC3 = 12;
    public static final int FILE_TYPE_QCP = 13;
    public static final int FILE_TYPE_WEBMA = 14;
    public static final int FILE_TYPE_PCM = 15;
    public static final int FILE_TYPE_EC3 = 16;
    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_MP3;
    private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_EC3;

    // More audio file types
    public static final int FILE_TYPE_DTS = 300;
    private static final int FIRST_AUDIO_FILE_TYPE2 = FILE_TYPE_DTS;
    private static final int LAST_AUDIO_FILE_TYPE2 = FILE_TYPE_DTS;

    // MIDI file types
    public static final int FILE_TYPE_MID = 16;
    public static final int FILE_TYPE_SMF = 17;
    public static final int FILE_TYPE_IMY = 18;
    private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
    private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY;

    // Video file types
    public static final int FILE_TYPE_MP4 = 21;
    public static final int FILE_TYPE_M4V = 22;
    public static final int FILE_TYPE_3GPP = 23;
    public static final int FILE_TYPE_3GPP2 = 24;
    public static final int FILE_TYPE_WMV = 25;
    public static final int FILE_TYPE_ASF = 26;
    public static final int FILE_TYPE_MKV = 27;
    public static final int FILE_TYPE_MP2TS = 28;
    public static final int FILE_TYPE_AVI = 29;
    public static final int FILE_TYPE_WEBM = 30;
    public static final int FILE_TYPE_DIVX = 31;
    private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_MP4;
    private static final int LAST_VIDEO_FILE_TYPE = FILE_TYPE_DIVX;

    // More video file types
    public static final int FILE_TYPE_MP2PS = 200;
    private static final int FIRST_VIDEO_FILE_TYPE2 = FILE_TYPE_MP2PS;
    private static final int LAST_VIDEO_FILE_TYPE2 = FILE_TYPE_MP2PS;

    // Image file types
    public static final int FILE_TYPE_JPEG = 32;
    public static final int FILE_TYPE_GIF = 33;
    public static final int FILE_TYPE_PNG = 34;
    public static final int FILE_TYPE_BMP = 35;
    public static final int FILE_TYPE_WBMP = 36;
    public static final int FILE_TYPE_WEBP = 37;
    private static final int FIRST_IMAGE_FILE_TYPE = FILE_TYPE_JPEG;
    private static final int LAST_IMAGE_FILE_TYPE = FILE_TYPE_WEBP;

    // Playlist file types
    public static final int FILE_TYPE_M3U = 41;
    public static final int FILE_TYPE_PLS = 42;
    public static final int FILE_TYPE_WPL = 43;
    public static final int FILE_TYPE_HTTPLIVE = 44;

    private static final int FIRST_PLAYLIST_FILE_TYPE = FILE_TYPE_M3U;
    private static final int LAST_PLAYLIST_FILE_TYPE = FILE_TYPE_HTTPLIVE;

    // Drm file types
    public static final int FILE_TYPE_FL = 51;
    private static final int FIRST_DRM_FILE_TYPE = FILE_TYPE_FL;
    private static final int LAST_DRM_FILE_TYPE = FILE_TYPE_FL;

    // Other popular file types
    public static final int FILE_TYPE_TEXT = 100;
    public static final int FILE_TYPE_HTML = 101;
    public static final int FILE_TYPE_PDF = 102;
    public static final int FILE_TYPE_XML = 103;
    public static final int FILE_TYPE_MS_WORD = 104;
    public static final int FILE_TYPE_MS_EXCEL = 105;
    public static final int FILE_TYPE_MS_POWERPOINT = 106;
    public static final int FILE_TYPE_ZIP = 107;
    public static final int FILE_TYPE_EML = 108;//add for PR915950 by yane.wang@jrdcom.com 20150128
    public static final int FILE_TYPE_RAR = 109;
    public static final int FILE_TYPE_JAR = 110;
    public static final int FILE_TYPE_SDP = 111;
    public static final int FILE_TYPE_JAD = 112;

    // FileManager support file type
    public static final int FILE_TYPE_ICS = 190;
    public static final int FILE_TYPE_ICZ = 191;
    public static final int FILE_TYPE_VCF = 192;
    public static final int FILE_TYPE_VCS = 193;
    public static final int FILE_TYPE_APK = 194;
    public static final int FILE_TYPE_INI = 195;
    public static final int FILE_TYPE_DAT = 196;
    public static final int FILE_TYPE_XMIND = 197;
    public static final int FILE_TYPE_LOG = 198;
    public static final int FILE_TYPE_RM = 199;
    public static final int FILE_TYPE_TIF = 200;

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap<String, MediaFileType>();
    private static final HashMap<String, Integer> sMimeTypeMap = new HashMap<String, Integer>();
    // maps file extension to MTP format code
    private static final HashMap<String, Integer> sFileTypeToFormatMap = new HashMap<String, Integer>();
    // maps mime type to MTP format code
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = new HashMap<String, Integer>();
    // maps MTP format code to mime type
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = new HashMap<Integer, String>();

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    static void addFileType(String extension, int fileType, String mimeType, int mtpFormatCode) {
        addFileType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, Integer.valueOf(mtpFormatCode));
        sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        sFormatToMimeTypeMap.put(mtpFormatCode, mimeType);
    }

    private static boolean isWMAEnabled() {
        List<AudioDecoder> decoders = DecoderCapabilities.getAudioDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i++) {
            AudioDecoder decoder = decoders.get(i);
            if (decoder == AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWMVEnabled() {
        List<VideoDecoder> decoders = DecoderCapabilities.getVideoDecoders();
        int count = decoders.size();
        for (int i = 0; i < count; i++) {
            VideoDecoder decoder = decoders.get(i);
            if (decoder == VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }

    static {
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3);
        addFileType("MPGA", FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3);
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav", MtpConstants.FORMAT_WAV);
        addFileType("WAV", FILE_TYPE_PCM, "audio/wav");
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr");
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb");
        addFileType("DIVX", FILE_TYPE_DIVX, "video/flv");
        if (isWMAEnabled()) {
            addFileType("WMA", FILE_TYPE_WMA, "audio/x-ms-wma", MtpConstants.FORMAT_WMA);
        }
        addFileType("QCP", FILE_TYPE_QCP, "audio/qcelp");
        addFileType("OGG", FILE_TYPE_OGG, "audio/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGG", FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGA", FILE_TYPE_OGG, "audio/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGA", FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG);
        addFileType("AAC", FILE_TYPE_AAC, "audio/aac", MtpConstants.FORMAT_AAC);
        addFileType("AAC", FILE_TYPE_AAC, "audio/aac-adts", MtpConstants.FORMAT_AAC);
        // [Dev]-Add by TCTNB hanling.zhang Dec/19/2013 575237
        addFileType("ADTS", FILE_TYPE_AAC, "audio/x-hx-aac-adts", MtpConstants.FORMAT_AAC);
        addFileType("MKA", FILE_TYPE_MKA, "audio/x-matroska");

        addFileType("MID", FILE_TYPE_MID, "audio/midi");
        addFileType("MIDI", FILE_TYPE_MID, "audio/midi");
        addFileType("XMF", FILE_TYPE_MID, "audio/midi");
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi");
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi");
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody");
        addFileType("RTX", FILE_TYPE_MID, "audio/midi");
        addFileType("OTA", FILE_TYPE_MID, "audio/midi");
        addFileType("MXMF", FILE_TYPE_MID, "audio/midi");

        addFileType("MPEG", FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("MPG", FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("MP4", FILE_TYPE_MP4, "video/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("M4V", FILE_TYPE_M4V, "video/x-m4v", MtpConstants.FORMAT_MPEG);
        addFileType("3GP", FILE_TYPE_3GPP, "video/*", MtpConstants.FORMAT_3GP_CONTAINER);//video/3gpp
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp", MtpConstants.FORMAT_3GP_CONTAINER);//video/3gpp
        addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER);//video/3gpp2
        addFileType("3GPP2", FILE_TYPE_3GPP2, "video/*", MtpConstants.FORMAT_3GP_CONTAINER);//video/3gpp2
        addFileType("MKV", FILE_TYPE_MKV, "video/x-matroska");
        addFileType("WEBM", FILE_TYPE_WEBM, "video/x-matroska");
        addFileType("TS", FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("MPG", FILE_TYPE_MP2TS, "video/mpeg");

        addFileType("AVI", FILE_TYPE_AVI, "video/avi");

        if (isWMVEnabled()) {
            addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv", MtpConstants.FORMAT_WMV);
            addFileType("ASF", FILE_TYPE_ASF, "video/x-ms-asf");
        }

        addFileType("JPG", FILE_TYPE_JPEG, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG);
        addFileType("JPEG", FILE_TYPE_JPEG, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG);
        addFileType("GIF", FILE_TYPE_GIF, "image/gif", MtpConstants.FORMAT_GIF);
        addFileType("PNG", FILE_TYPE_PNG, "image/png", MtpConstants.FORMAT_PNG);
        addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp", MtpConstants.FORMAT_BMP);
        addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp");
        addFileType("WEBP", FILE_TYPE_WEBP, "image/webp");

        addFileType("M3U", FILE_TYPE_M3U, "audio/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST);
        addFileType("M3U", FILE_TYPE_M3U, "application/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST);
        addFileType("PLS", FILE_TYPE_PLS, "audio/x-scpls", MtpConstants.FORMAT_PLS_PLAYLIST);
        addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl",
                MtpConstants.FORMAT_WPL_PLAYLIST);
        addFileType("M3U8", FILE_TYPE_HTTPLIVE, "application/vnd.apple.mpegurl");
        addFileType("M3U8", FILE_TYPE_HTTPLIVE, "audio/mpegurl");
        addFileType("M3U8", FILE_TYPE_HTTPLIVE, "audio/x-mpegurl");

        addFileType("FL", FILE_TYPE_FL, "application/x-android-drm-fl");

        addFileType("TXT", FILE_TYPE_TEXT, "text/plain", MtpConstants.FORMAT_TEXT);
        addFileType("HTM", FILE_TYPE_HTML, "text/html", MtpConstants.FORMAT_HTML);
        addFileType("HTML", FILE_TYPE_HTML, "text/html", MtpConstants.FORMAT_HTML);
        addFileType("PDF", FILE_TYPE_PDF, "application/pdf");
        addFileType("DOC", FILE_TYPE_MS_WORD, "application/msword",
                MtpConstants.FORMAT_MS_WORD_DOCUMENT);
        //add for PR910274 by yane.wang@jrdcom.com 20150121 begin
        addFileType("DOCX", FILE_TYPE_MS_WORD, "application/msword",
                MtpConstants.FORMAT_MS_WORD_DOCUMENT);
        addFileType("XLSX", FILE_TYPE_MS_EXCEL, "application/vnd.ms-excel",
                MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET);
        //add for PR910274 by yane.wang@jrdcom.com 20150121 end
        addFileType("XLS", FILE_TYPE_MS_EXCEL, "application/vnd.ms-excel",
                MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET);
        // ADD START FOR PR1047782 BY HONGBIN.CHEN 20150811
        addFileType("XLSM", FILE_TYPE_MS_EXCEL, "application/vnd.ms-excel",
                MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET);
        // ADD END FOR PR1047782 BY HONGBIN.CHEN 20150811
        addFileType("PPT", FILE_TYPE_MS_POWERPOINT, "application/vnd.ms-powerpoint",
                MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION);
        addFileType("PPTX", FILE_TYPE_MS_POWERPOINT, "application/vnd.ms-powerpoint",
                MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION);//add for PR910274 by yane.wang@jrdcom.com 20150121
        addFileType("FLAC", FILE_TYPE_FLAC, "audio/flac", MtpConstants.FORMAT_FLAC);
        addFileType("ZIP", FILE_TYPE_ZIP, "application/zip");
        addFileType("EML", FILE_TYPE_EML, "application/eml");//add for PR915950 by yane.wang@jrdcom.com 20150128
        addFileType("RAR", FILE_TYPE_RAR, "application/x-rar-compressed");
        addFileType("JAR", FILE_TYPE_JAR, "application/java-archive");
        addFileType("SDP", FILE_TYPE_SDP, "application/sdp");
        addFileType("JAD", FILE_TYPE_JAD, "application/java-archive");
        addFileType("MPG", FILE_TYPE_MP2PS, "video/mp2p");
        addFileType("MPEG", FILE_TYPE_MP2PS, "video/mp2p");

        // FileManager support file type
        addFileType("ICS", FILE_TYPE_ICS, "text/calendar");
        addFileType("ICZ", FILE_TYPE_ICZ, "text/calendar");
        addFileType("VCF", FILE_TYPE_VCF, "text/x-vcard");
        addFileType("VCS", FILE_TYPE_VCS, "text/x-vcalendar");
        addFileType("APK", FILE_TYPE_APK, "application/vnd.android.package-archive");
        addFileType("INI", FILE_TYPE_INI, "application/octet-stream");
        addFileType("DAT", FILE_TYPE_DAT, "text/plain");
        addFileType("LOG", FILE_TYPE_LOG, "text/plain");
        addFileType("XMIND", FILE_TYPE_XMIND, "application/xmind");
        addFileType("RM", FILE_TYPE_XMIND, "audio/mpeg");
        addFileType("TIF", FILE_TYPE_TIF, "image/tiff");

    }

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE &&
                fileType <= LAST_AUDIO_FILE_TYPE) ||
                (fileType >= FIRST_MIDI_FILE_TYPE &&
                        fileType <= LAST_MIDI_FILE_TYPE) || (fileType >= FIRST_AUDIO_FILE_TYPE2 && fileType <= LAST_AUDIO_FILE_TYPE2));
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE &&
                fileType <= LAST_VIDEO_FILE_TYPE)
                || (fileType >= FIRST_VIDEO_FILE_TYPE2 &&
                fileType <= LAST_VIDEO_FILE_TYPE2);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE && fileType <= LAST_IMAGE_FILE_TYPE);
    }

    public static boolean isPlayListFileType(int fileType) {
        return (fileType >= FIRST_PLAYLIST_FILE_TYPE && fileType <= LAST_PLAYLIST_FILE_TYPE);
    }

    public static boolean isDrmFileType(int fileType) {
        return (fileType >= FIRST_DRM_FILE_TYPE && fileType <= LAST_DRM_FILE_TYPE);
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0)
            return null;
        return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase());
    }

    public static boolean isMimeTypeMedia(String mimeType) {
        int fileType = getFileTypeForMimeType(mimeType);
        return isAudioFileType(fileType) || isVideoFileType(fileType)
                || isImageFileType(fileType) || isPlayListFileType(fileType);
    }

    // generates a title based on file name
    public static String getFileTitle(String path) {
        // extract file name after last slash
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            lastSlash++;
            if (lastSlash < path.length()) {
                path = path.substring(lastSlash);
            }
        }
        // truncate the file extension (if any)
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            path = path.substring(0, lastDot);
        }
        return path;
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = sMimeTypeMap.get(mimeType);
        return (value == null ? 0 : value.intValue());
    }

    public static String getMimeTypeForFile(String path) {
        MediaFileType mediaFileType = getFileType(path);
        return (mediaFileType == null ? null : mediaFileType.mimeType);
    }

    public static int getFormatCode(String fileName, String mimeType) {
        if (mimeType != null) {
            Integer value = sMimeTypeToFormatMap.get(mimeType);
            if (value != null) {
                return value.intValue();
            }
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            String extension = fileName.substring(lastDot + 1).toUpperCase();
            Integer value = sFileTypeToFormatMap.get(extension);
            if (value != null) {
                return value.intValue();
            }
        }
        return MtpConstants.FORMAT_UNDEFINED;
    }

    public static String getMimeTypeForFormatCode(int formatCode) {
        return sFormatToMimeTypeMap.get(formatCode);
    }
}
