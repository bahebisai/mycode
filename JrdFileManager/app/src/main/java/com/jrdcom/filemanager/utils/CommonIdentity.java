package com.jrdcom.filemanager.utils;


import android.drm.DrmStore;
import android.net.Uri;
import android.provider.MediaStore;

import com.tct.drm.api.TctDrmManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommonIdentity {
    public static List<String> audioMimeTypeList;
    public static List<String> imageMimeTypeList;
    public static List<String> videoMimeTypeList;
    public static List<String> achiveMimeTypeList;
    public static List<String> docMimeTypeList;
    static {
        audioMimeTypeList = new ArrayList<String>();
        audioMimeTypeList.add("audio/mp3");
        audioMimeTypeList.add("audio/*");
        audioMimeTypeList.add("audio/ogg");
        audioMimeTypeList.add("audio/mp4");
        audioMimeTypeList.add("audio/x-ms-wma");
        audioMimeTypeList.add("audio/amr-wb");
        audioMimeTypeList.add("audio/mpeg");
        audioMimeTypeList.add("audio/xmf");
        audioMimeTypeList.add("audio/x-ape");
        audioMimeTypeList.add("audio/aac");
        audioMimeTypeList.add("audio/x-wav");
        audioMimeTypeList.add("audio/wav");
        audioMimeTypeList.add("audio/amr");
        audioMimeTypeList.add("audio/midi");
        audioMimeTypeList.add("audio/imelody");
        audioMimeTypeList.add("audio/qcelp");
        audioMimeTypeList.add("audio/aac-adts");
        audioMimeTypeList.add("audio/x-hx-aac-adts");
        audioMimeTypeList.add("audio/x-matroska");
        audioMimeTypeList.add("audio/x-mpegurl");
        audioMimeTypeList.add("audio/x-scpls");

        videoMimeTypeList = new ArrayList<String>();
        videoMimeTypeList.add("video/*");
        videoMimeTypeList.add("video/divx");
        videoMimeTypeList.add("video/mpeg");
        videoMimeTypeList.add("video/mp4");
        videoMimeTypeList.add("video/x-matroska");
        videoMimeTypeList.add("video/webm");
        videoMimeTypeList.add("video/mp2ts");
        videoMimeTypeList.add("video/avi");
        videoMimeTypeList.add("video/x-ms-wmv");
        videoMimeTypeList.add("video/x-ms-asf");

        imageMimeTypeList = new ArrayList<String>();
        imageMimeTypeList.add("image/*");
        imageMimeTypeList.add("image/jpeg");
        imageMimeTypeList.add("image/gif");
        imageMimeTypeList.add("image/png");
        imageMimeTypeList.add("image/x-ms-bmp");
        imageMimeTypeList.add("image/vnd.wap.wbmp");
        imageMimeTypeList.add("image/webp");

        achiveMimeTypeList = new ArrayList<String>();
        achiveMimeTypeList.add("application/sdp");
        achiveMimeTypeList.add("application/java-archive");
        achiveMimeTypeList.add("application/java-archive");
        achiveMimeTypeList.add("application/zip");
        achiveMimeTypeList.add("application/x-rar-compressed");
        achiveMimeTypeList.add("application/x-tar");
        achiveMimeTypeList.add("application/x-7z-compressed");
        achiveMimeTypeList.add("application/x-gzip");

        docMimeTypeList = new ArrayList<String>();
        docMimeTypeList.add("application/pdf");
        docMimeTypeList.add("application/msword");
        docMimeTypeList.add("application/vnd.ms-excel");
        docMimeTypeList.add("application/vnd.ms-powerpoint");
        docMimeTypeList.add("application/msword");
        docMimeTypeList.add("application/vnd.ms-excel");
        docMimeTypeList.add("application/vnd.ms-powerpoint");
        docMimeTypeList.add("application/vnd.ms-excel");
        docMimeTypeList.add("text/x-vcalendar");
        docMimeTypeList.add("text/calendar");

    }

    public static final String FILEMANAGER_SHARE_NUMBER = "share_num";
    public static final String FILEMANAGER_SEARCH_NUMBER = "search_num";
    public static final String FILEMANAGER_GLOBAL_SEARCH_NUMBER = "global_search_num";
    public static final String FILEMANAGER_PRIVATE_MODE_NUMBER = "private_mode_num";
    public static final String FILEMANAGER_VIEW_KEY = "view_key";

    public static final String FILEMANAGER_LAUNCH_TIME = "launch_time";
    public static final String FILEMANAGER_LISTFILE_TIME = "listfile_time";
    public static final String FILEMANAGER_DELETE_TIME = "delete_time";
    public static final long LOW_MEMORY_TAG = 50*1024*1024;
    public static final int HAWKEYE_LISTFILE_COUNT = 1000;
    public static final int MSG_HAWKEYE_RESUME = 0X101;
    public static final int MSG_BASE_ACTIVITY_RESTART = 0X102;

    public static final String FILEMANAGER_GRID_VIEW_STATUS = "grid_view_sta";
    public static final String FILEMANAGER_LIST_VIEW_STATUS = "list_view_sta";
    public static final String FILEMANAGER_SORT_KEY = "sort_key";
    public static final String FILEMANAGER_NAME_SORT_STATUS = "name_sort_sta";
    public static final String FILEMANAGER_TIME_SORT_STATUS = "time_sort_sta";
    public static final String FILEMANAGER_SIZE_SORT_STATUS = "size_sort_sta";
    public static final String FILEMANAGER_TYPE_SORT_STATUS = "type_sort_sta";
    public static final String FILEMANAGER_ACTIONBAR_HOME_CLICK_NUMBER = "actionbar_home_click_num";
    public static final int HAWKEYE_FEEDBACK_INFO = 0X01;

    public static final int INIT_ACTIVITY_ONCREATE = 0X01;
    public static final int INIT_ACTIVITY_MAINCONTENTVIEW = 0X02;
    public static final int INIT_ACTIVITY_ACTIONBAR = 0X03;
    public static final int NO_AVAILABLE_STORAGE = 0X04;
    public static final int RESTORE_INSTANCE_STATE = 0X05;

    public static final int EXTRACT_NORMAL_MODE = 1;
    public static final int EXTRACT_RENAME_MODE = 2;
    public static final int EXTRACT_REPEAT_NAME_MODE = 3;
    public static final int EXTRACT_RUNNING_MODE = 4;
    public static final int EXTRACT_RENAME_RUNNING_MODE = 5;

    public static final int COMPRESS_NORMAL_MODE = 1;
    public static final int COMPRESS_RENAME_RUNNING_MODE = 2;

    public static final int DRM_SCHEME_OMA1_FL = 1;
    public static final int DRM_SCHEME_OMA1_CD = 2;
    public static final int DRM_SCHEME_OMA1_SD = 3;
    public static final String RIGHTS_ISSUER = "drm_rights_issuer";
    public static final String CONSTRAINT_TYPE = "constraint_type";
    public static final String CONTENT_VENDOR = "content_vendor";
    public static final String WALLPAPER_FILEPATH = "drm_wallpaper_filepath";
    public static final String COMPRESS_DEFAUT_NAME = "Archive";


    public static final String TCT_IS_PRIVATE = "tct_is_private";
    public static final String FILES_PACKAGE_NAME = "com.jrdcom.filemanager";
    public static final String SP_NAME = "filemanager_sp";

    public static final Uri MEDIA_URI =MediaStore.Files.getContentUri("external");
    public static final Uri IMAGE_MEDIA_URI =MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final Uri AUDIO_MEDIA_URI =MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final Uri VEDIO_MEDIA_URI =MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    public static final String RESET_DEVICE_ACTION ="android.intent.action.LAUNCH_DEVICE_RESET";
    public static final String SET_SYSTEM_TIME_ACTION ="android.intent.action.TIME_SET";

    public static final long SAFE_SIZE_LIMITED = 2147483648l;
    public static final int RUNNING_TASK_MAX_LIMITE = 0X02;
    public static final int WAITTING_TASK_MAX_LIMITE = 0X02;
    public static final String RESULT_TASK_KEY = "TASKRESULT";
    public static final int BUFFER_SIZE = 256 * 1024;
    public static final int FILENAME_MAX_LENGTH = 254;
    public static final String MANAGE_PERMISSIONS = "android.intent.action.tct.MANAGE_PERMISSIONS";
    public static final String PACKAGE_NAME = "android.intent.extra.tct.PACKAGE_NAME";
    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String FILE_BROWSER_ACTIVITY_NAME = "com.jrdcom.filemanager.activity.FileBrowserActivity";
    public static final String FILE_CATEGORY_FRAGMENT_NAME = "com.jrdcom.filemanager.fragment.CategoryFragment";
    public static final String FILE_LIST_FRAGMENT_NAME = "com.jrdcom.filemanager.fragment.ListsFragment";
    public static final int FILE_MANAGER_LOCATIONE = 1;
    public static final int FILE_SAFEBOX_LOCATION = 2;
    public static final int FILE_PRIVATE_LOCATION = 3;

    public static final int SCROLL_FLAG_SCROLL_CANCEL = 0; // close Scroll
    public static final int SCROLL_FLAG_SCROLL_START = 5; //Can Scroll


    public static final int REFRESH_FILE_PATH_MODE = 1;
    public static final int REFRESH_FILE_CATEGORY_MODE = 2;
    public static final int REFRESH_SAFE_CATEGORY_MODE = 3;
    public static final int REFRESH_FILE_NOTIFICATION_MODE = 4;
    public static final int REFRESH_PRIVATE_CATEGORY_MODE = 5;

    public static final int NEED_UPDATE_TIME = 200;

    public static final int STORAGE_INFO_CATEGORY = 1;
    public static final int STORAGE_INFO_SAFEBOX = 2;

    public static final int LOADING_REFRESH_MODE = 0x01;
    public static final int NOTIFICATION_REFRESH_MODE = 0x02;


    public static final int FILE_STATUS_NORMAL = 0x01;
    public static final int FILE_STATUS_EDIT = 0x02;
    public static final int FILE_STATUS_SEARCH = 0x03;
    public static final int FILE_STATUS_GLOBALSEARCH = 0x04;
    public static final int FILE_STATUS_CATEGORY_SEARCH = 0X05;
    public static final int FILE_STATUS_SELECT = 0x07;
    public static final int FILE_STATUS_FOLDER_SELECT = 0x08;

    public static final int FILE_COPY_NORMAL = 0x06;


    public static final int SAFE_CATEGORY_INSTALLERS = 0;
    public static final int SAFE_CATEGORY_DOCS = 1;
    public static final int SAFE_CATEGORY_MUISC = 2;
    public static final int SAFE_CATEGORY_PICTURES = 3;
    public static final int PRIVATE_CATEGORY_VEDIO = 3;
    public static final int PRIVATE_CATEGORY_ARCHIVES = 4;
    public static final int SAFE_CATEGORY_VEDIO = 4;
    public static final int SAFE_CATEGORY_ARCHIVES = 5;
    public static final int SAFE_CATEGORY_PRIVATE = 12;
    public static final int FILE_SAFE_VIEW_MODE = 1;
    public static final int FILE_MOVE_IN_MODE = 2;


    public static final int FILE_FILTER_TYPE_UNKOWN = -1;
    public static final int FILE_FILTER_TYPE_DEFAULT = 0;
    public static final int FILE_FILTER_TYPE_FOLDER = 1;
    public static final int FILE_FILTER_TYPE_ALL = 2;


    public static final float CUT_ICON_ALPHA = 0.6f;
    public static final float HIDE_ICON_ALPHA = 0.3f;
    public static final float DEFAULT_ICON_ALPHA = 1f;


    public static final String LIST_MODE = "listMode";
    public static final String GRID_MODE = "gridMode";

    public static final String CATEGORY_TAG = "category";
    public static final String PERMISSION_TAG = "permissions";
    public static final String PHONE_TAG = "phone";
    public static final String SDCARD_TAG = "sdcard";
    public static final String USBOTG_TAG = "usbotg";

    public static final int CATEGORY_RECENT = 0;
    public static final int CATEGORY_APKS = 1;
    public static final int CATEGORY_BLUETOOTH = 2;
    public static final int CATEGORY_DOCS = 3;
    public static final int CATEGORY_DOWNLOAD = 4;
    public static final int CATEGORY_MUSIC = 5;

    public static final int CATEGORY_PICTURES = 6;
    public static final int CATEGORY_VEDIOS = 7;
    public static final int CATEGORY_ARCHIVES = 8;
    public static final int CATEGORY_SAFE = 9;

    public static final int CATEGORY_MODE = 0x01;
    public static final int PATH_MODE = 0X02;


    public static final int MSG_FINGER_AUTH_SUCCESS = 1001;
    public static final int MSG_FINGER_AUTH_FAIL = 1002;
    public static final int MSG_FINGER_AUTH_ERROR = 1003;
    public static final int MSG_FINGER_AUTH_HELP = 1004;
    public static final int MSG_SAFE_NO_EXISTS = 1005;
    public static final int MSG_DIALOG_SHOW=1011;
    public static final int MSG_DIALOG_DISMISS=1012;
    public static final int MSG_REFRESH_UI=1101;

    public static final int CANCEL_ALL_TASK = -3;
    public static final int CANCEL_TASK = -2;
    public static final int UNKNOWN_TASK = -1;
    public static final int PASTE_CUT_TASK = 0x01;
    public static final int PASTE_COPY_TASK = 0x02;
    public static final int CREATE_FOLDER_TASK = 0X03;
    public static final int RENAME_FILE_TASK = 0X04;
    public static final int NORMAL_DELETE_TASK = 0X05;
    public static final int LIST_INFO_TASK = 0X11;
    public static final int SEARCH_INFO_TASK = 0X12;
    public static final int DETAIL_FILE_TASK = 0X13;
    public static final int STORAGE_SPACE_TASK = 0X14;
    public static final int UPDATE_PERCENTAGEBAR_TASK = 0X15;
    public static final int PROGRESS_DIALOG_TASK = 0X17;
    public static final int PROGRESS_SHOW_TASK = 0X18;
    public static final int PROGRESS_COMPLETE_TASK = 0X19;
    public static final int PROGRESS_NOTIFICATION_TASK = 0X20;
    public static final int OBSERVER_UPDATE_TASK = 0X21;
    public static final int FILE_COMPRESSION_TASK = 0X22;
    public static final int FILE_UNCOMPRESSION_TASK = 0X23;
    public static final int CATEGORY_COUNT_TASK = 0X24;
    public static final int FOLDER_COUNT_TASK = 0X25;
    public static final int ADD_PRIVATE_FILE_TASK = 0X26;
    public static final int SAFE_CATEGORY_COUNT_TASK = 0X27;
    public static final int REMOVE_PRIVATE_FILE_TASK = 0X28;

    public static final int MINI_THUMB_TARGET_SIZE = 256;
    public static final int MINI_THUMB_MAX_NUM_PIXELS = 512 * 512;

    public static final int UNCONSTRAINED = -1;
    public static final String DESTORY_TAG = "destory";


    public static final int DETETE = 10001;
    public static final int RENAME = 10002;
    public static final int OTHER = 10003;
    public static final int PASTE = 10004;
    public static final int SELECT_ALL = 10005;
    public static final int PASTE_MODE_CUT = 1;

    public static final String PREF_SORT_BY = "pref_sort_by";
    public static final String PREF_VIEW_BY = "pref_view_by";
    public static final String PREF_CURR_TAG = "curr_tag";
    public static final String SAFE_PREF_CURR_TAG = "safe_curr_tag";

    public static final String GLOBAL_SEARCH = "global_search";

    public static final String TCT_HDCP_DRM_NOTIFY = "hdcp_drm_notify";

    public static final String CREATE_FOLDER_DIALOG_TAG = "CreateFolderDialogTag";

    public static final String DETAIL_DIALOG_TAG = "detailDialogTag";

    public static final String RENAME_EXTENSION_DIALOG_TAG = "RenameExtensionDialogTag";

    public static final String DELETE_DIALOG_TAG = "DeleteDialogTag";

    public static final String EXTRACT_DIALOG_TAG = "ExtractDialogTag";
    public static final String EXTRACT_NAME_EXIST_DIALOG_TAG = "ExtractNameExistDialogTag";
    public static final String EXTRACT_RENAME_DIALOG_TAG = "ExtractRenameDialogTag";
    public static final String COMPRESS_RENAME_DIALOG_TAG = "CompressRenameDialogTag";

    public static final String NEW_FILE_PATH_KEY = "newFilePathKey";

    public static final String REMOVE_PRIVATE_DIALOG_TAG = "removeprivateDialogTag";

    public static final String EXIT_DIALOG_TAG = "exitDialogTag";

    public static final String NO_AVAILABLE_STORAGE_DIALOG_TAG = "availablestorageDialogTag";

    public static final int CATEGROY_DELETE_NOT_EXIT = 105;

    public static final int UPDATE_ADAPTER_SORT_NOTIFICATION = 188;
    public static final int SORT_UPDATE_ADAPTER_NOTIFICATION = 189;

    public static final int SORT_MODE = 2;


    public static final String ALL = "allSpace";
    public static final String PHONE = "phoneSpace";
    public static final String SDCARD = "sdcardSpace";

    public static final int DATA_UPDATED = 100;

    public static final int MAX_CACHE_SIZE = 10;
    public static final long EXPIRE_TIME = 60 * 1000;

    public static final int TOTAL = 100;

    public static final String FONT_ROBOTO_MEDIUM_FILE = "/system/fonts/Roboto-Medium.ttf";

    public static final String MIMETYPE_EXTENSION_NULL = "unknown_ext_null_mimeType";
    public static final String MIMETYPE_EXTENSION_UNKONW = "unknown_ext_mimeType";
    public static final String MIMETYPE_EXTENSION_BAD = "bad mime type";
    public static final String MIMETYPE_3GPP_VIDEO = "video/3gpp";
    public static final String MIMETYPE_3GPP2_VIDEO = "video/3gpp2";
    public static final String MIMETYPE_3GPP_UNKONW = "unknown_3gpp_mimeType";
    public static final String MIMETYPE_APK = "application/vnd.android.package-archive";
    public static final String MIMETYPE_UNRECOGNIZED = "application/zip";

    public static final String MIME_HAED_IMAGE = "image/";
    public static final String MIME_HEAD_VIDEO = "video/";
    public static final String MIME_HEAD_AUDIO = "audio/";
    public static final String PREF_BY = "sort_item";

    public static final int MODE_DRM_DETAIL = 2;
    public static final int MODE_NORMAL_DETAIL = 1;

    public static final int MODE_EXTEND_RENAME = 2;
    public static final int MODE_NORMAL_RENAME = 1;

    public static final int MODE_CATEGORY_FINGER = 2;
    public static final int MODE_SAFE_FINGER = 1;

    public static final int PROGRESS_DIALOG_MODE = 0X01;
    public static final int PROGRESS_NOTIFICATION_MODE = 0X02;
    public static final int PROGRESS_ALL_SHOW_MODE = 0X03;

    public static final int PROGRESS_NOTIFICATION_ID = 0X1008;

    public static final int FILE_OPERATION_NORMAL =0X01;

    public static final int DIR_ANIM_LEFT = 1;
    public static final int DIR_ANIM_RIGHT = 2;


    //DRM file
    public static final int NO_DRM = -1;
    public static final int MTK_DRM = 10;
    public static final int QCOM_DRM = 20;
    public static final String EXT_DRM_CONTENT = "dcf";
    public static final int DRM_THUMBNAIL_WITH = 72;


    public static String TCT_IS_DRM = TctDrmManager.TCT_IS_DRM;
    public static final String REMAINING_REPEAT_COUNT = DrmStore.ConstraintsColumns.REMAINING_REPEAT_COUNT;
    public static final String LICENSE_START_TIME = DrmStore.ConstraintsColumns.LICENSE_START_TIME;
    public static final String LICENSE_EXPIRY_TIME = DrmStore.ConstraintsColumns.LICENSE_EXPIRY_TIME;
    public static final String LICENSE_AVAILABLE_TIME = DrmStore.ConstraintsColumns.LICENSE_AVAILABLE_TIME;

    public static final int SORT_NAME = 0;
    public static final int SORT_TIME = 1;
    public static final int SORT_SIZE = 2;
    public static final int SORT_TYPE = 3;

}
