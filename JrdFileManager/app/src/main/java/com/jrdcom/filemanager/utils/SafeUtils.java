/* Copyright (C) 2016 Tcl Corporation Limited */
package com.jrdcom.filemanager.utils;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FileSafeBrowserActivity;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.SafeManager;


import java.io.File;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 16-2-26.
 */
public class SafeUtils {

    public static String getMD5String(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void fieldDialog(AlertDialog dialog){
        try {
            Field field = dialog.getClass().getDeclaredField("mAlert");
            field.setAccessible(true);
            Object obj = field.get(dialog);
            field = obj.getClass().getDeclaredField("mHandler");
            field.setAccessible(true);
            field.set(obj, new ButtonHandler(dialog));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isActivityRunning(Context mContext, String ActivityName) {
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = mActivityManager.getRunningTasks(1);
        if (info != null && info.size() > 0) {
            ComponentName component = info.get(0).topActivity;
            if (ActivityName.equals(component.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isQuitSafe(Context context) {
        SafeManager.notQuitSafe = false;
        if (CommonUtils.isInPrivacyMode(context) &&
                !SafeUtils.isActivityRunning(context, "com.jrdcom.filemanager.activity.FileSafeBrowserActivity") && context
                .getClass().getName().equals("com.jrdcom.filemanager.activity.FileSafeBrowserActivity")) {
            return true;
        }
        return false;
    }

}
