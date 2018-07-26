package com.jrdcom.filemanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;

/**
 * Created by user on 16-9-28.
 */
public class FilesReceiver extends BroadcastReceiver {

    private static final String TAG = FilesReceiver.class.getSimpleName();
    private FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    public static final String PREF_BY = "sort_item";
    private String prefsNames = "com.jrdcom.filemanager_preferences";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(CommonIdentity.RESET_DEVICE_ACTION)) {
            Log.d("TAG","Receive "+CommonIdentity.RESET_DEVICE_ACTION);
            SharedPreferences prefss = context.getSharedPreferences(prefsNames, Context.MODE_PRIVATE);
            SharedPreferences.Editor comeditor = prefss.edit();
            comeditor.putInt(PREF_BY, 0);
            comeditor.commit();
            SharedPreferences prefs = context.getSharedPreferences(CommonIdentity.SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(CommonIdentity.PREF_SORT_BY, 0);
            editor.putString(CommonIdentity.PREF_VIEW_BY, CommonIdentity.LIST_MODE);
            editor.commit();
           SharedPreferenceUtils.removeShowHiddenPref(context);
        } else if (action.equals(CommonIdentity.SET_SYSTEM_TIME_ACTION)) {
            if (mApplication != null && mApplication.mCache != null) {
                // remove recent category cache
                mApplication.mCache.removeCache("0");
            }
        }
    }
}
