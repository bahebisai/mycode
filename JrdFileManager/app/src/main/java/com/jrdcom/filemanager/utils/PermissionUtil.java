
package com.jrdcom.filemanager.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.PopupWindow;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FileBrowserActivity;
import com.jrdcom.filemanager.manager.SafeManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {


    public static final int CHECK_REQUEST_PERMISSION_RESULT = 3;
    protected static PopupWindow permissionPop;
    private final static String CHECK_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final static String CHECK_PERMISSION2 = "android.permission.READ_PHONE_STATE";
    public static boolean isSecondRequest = false;
    public final static int JUMPTOSETTINGFORSTORAGE = 0X11;
    public static boolean isShowPermissionDialog = false;

    /*
     * Add PermissionLifecycleCallbacks on application.oncreate to check
     * permissions.
     */
    @Deprecated
    public static PermissionLifecycleCallbacks getActivityLifecycleCallbacks(
            String[] permissions) {
        return new PermissionLifecycleCallbacks(permissions);
    }

    /*
     * @param activity The target activity.
     * @param permissions The requested permissions.
     * @param requestCode Application specific request code to match with a
     * result reported to {@link
     * OnRequestPermissionsResultCallback#onRequestPermissionsResult( int,
     * String[], int[])}.
     */
    public static void checkAndRequestPermissions(final @NonNull
                                                  Activity activity,
                                                  final @NonNull
                                                  String[] permissions, final int requestCode) {
        List<String> requestList = new ArrayList<String>();
        for (String perm : permissions) {
            if (PermissionChecker.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(perm);
            }
        }

        if (requestList.size() > 0) {
            isShowPermissionDialog = true;
            ActivityCompat.requestPermissions(activity,
                    requestList.toArray(new String[requestList.size()]), requestCode);
        }
        isSecondRequest = true;
    }

    /*
     * Check all permissions when resume the activity.
     */

    static class PermissionLifecycleCallbacks implements FileManagerApplication.ActivityLifecycleCallbacks {
        public static Activity mActivity;

        String[] permissions;

        public PermissionLifecycleCallbacks(@NonNull
                                            String[] permissions) {
            this.permissions = permissions;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            this.mActivity = activity;
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            //if(activity.getPackageName() !=null &&)

            //checkAndRequestPermissions(activity, permissions, CHECK_REQUEST_PERMISSION_RESULT);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (isAllowPermission(activity)) {
                if (isSecondRequestPermission(activity)) {
                    isShowPermissionDialog = false;
                } else {
                    activity.setResult(Activity.RESULT_CANCELED);
                }
            }

        }
    }

    public static boolean isAllowPermission(Activity activity) {
        // If use mie upgrade sdk, need READ_PHONE_STATE permission
//        if (activity != null && activity.getClass().getName().equals(FileBrowserActivity.class.getName())
//                && CommonUtils.isSupportUpgrade(activity.getApplicationContext())) {
//            return PermissionChecker.checkSelfPermission(activity, CHECK_PERMISSION) != PackageManager.PERMISSION_GRANTED ||
//                    PermissionChecker.checkSelfPermission(activity, CHECK_PERMISSION2) != PackageManager.PERMISSION_GRANTED;
//        }

        return PermissionChecker.checkSelfPermission(activity, CHECK_PERMISSION) != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isDenyPermission(Activity activity) {
        boolean a = PermissionChecker.checkSelfPermission(activity, CHECK_PERMISSION) == PackageManager.PERMISSION_DENIED;
        return a;
    }

    public static boolean isSecondRequestPermission(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                "firstTimeEnterApp", Context.MODE_PRIVATE);
        return sp.getBoolean("secondrequestpermission", false);
    }

    public static void setSecondRequestPermission(Context context) {
        SharedPreferences sp = context.getSharedPreferences("firstTimeEnterApp",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("secondrequestpermission", true);
        editor.commit();
    }

    static Dialog dialog = null;

    public static void popPermissionDialog(String mClassName, Activity mActivity) {
        final Activity activity = mActivity;
        final String className = mClassName;

        if (PermissionUtil.isDenyPermission(activity)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.permission_setting_title);
            builder.setMessage(R.string.permission_setting_content);
            builder.setPositiveButton(R.string.permission_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    boolean isEnterPermission = false;
                    Intent intent;
                    SafeManager.notQuitSafe = true;
                    try {
                        // Goto setting application permission
                        intent = new Intent(CommonIdentity.MANAGE_PERMISSIONS);
                        intent.putExtra(CommonIdentity.PACKAGE_NAME, activity.getPackageName());
                        activity.startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
                    } catch (Exception e) {
                        isEnterPermission = true;
                    }
                    if (isEnterPermission) {
                        // Goto settings details
                        Uri packageURI = Uri.parse("package:" + activity.getPackageName());
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        activity.startActivityForResult(intent, PermissionUtil.JUMPTOSETTINGFORSTORAGE);
                    }
                }


            });
            builder.setNegativeButton(R.string.permission_exit_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (className.equals(activity.getClass().getName())) {
                        activity.finish();
                    }
                }

            });
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        } else {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            PermissionUtil.checkAndRequestPermissions(activity, permissions,
                    PermissionUtil.CHECK_REQUEST_PERMISSION_RESULT);
        }

    }

    public static boolean isPermissionDialogShowning() {
        if (dialog != null && dialog.isShowing()) {
            return true;
        }

        return false;
    }

    public static void closePermissionDialogShowning() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static boolean isUerGrant(Context mContext) {
        int flags = 0;
        PackageManager pkg = mContext.getPackageManager();
        try {
            if (pkg == null) return false;
            Method getPermissionFlags = pkg.getClass().getMethod("getPermissionFlags", String.class, String.class, UserHandle.class);
            flags = (int) getPermissionFlags.invoke(pkg, Manifest.permission.READ_EXTERNAL_STORAGE, mContext.getPackageName(), new UserHandle(UserHandle.getCallingUserId()));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (flags & PackageManager.FLAG_PERMISSION_USER_FIXED) != 0;
    }

    public static boolean checkAppPermission(Context mContext) {
        return PermissionChecker.checkSelfPermission(mContext, CHECK_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
}
