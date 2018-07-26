package com.jrdcom.filemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.PlfUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SharedPreferenceUtils {

    private static final String KEY_SHOW_HIDDEN = "key_show_hidden";

    private static final String CURRENT_SAFE_NAME = "boxFolderName";
    private static final String CURRENT_SAFE_ROOT = "cardRootpath";
    private static final String CURRENT_VIEW_STATUS = "viewstatus";
    private static final String FRIST_ENTER_SAFE = "frist_enter_safe";
    public static FileManagerApplication mApplication = (FileManagerApplication)FileManagerApplication.getInstance();

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(CommonIdentity.SP_NAME, Context.MODE_PRIVATE);
    }

    public static void setShowHidden(Context context, boolean showHidden) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(KEY_SHOW_HIDDEN, showHidden).commit();
    }

    public static boolean isShowHidden(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_SHOW_HIDDEN, false);
    }

    public static void removeShowHiddenPref(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().remove(KEY_SHOW_HIDDEN).commit();
    }


    public static void setCurrentSafeName(Context context, String currentSafeName) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(CURRENT_SAFE_NAME, currentSafeName).commit();
    }

    public static String getCurrentSafeName(Context context) {
        return getSharedPreferences(context).getString(CURRENT_SAFE_NAME, null);
    }

    public static void setCurrentSafeRoot(Context context, String currentSafeRoot) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(CURRENT_SAFE_ROOT, currentSafeRoot).commit();
    }

    public static String getCurrentSafeRoot(Context context) {
        return getSharedPreferences(context).getString(CURRENT_SAFE_ROOT, null);
    }

    public static String getCurrentViewMode(Context context) {
        return getSharedPreferences(context).getString(CommonIdentity.PREF_VIEW_BY, null);
    }

    public static boolean isFristEnterSafe(Context context) {
        return getSharedPreferences(context).getBoolean(FRIST_ENTER_SAFE, false);
    }

    public static void setFristEnterSafe(Context context, boolean showHidden) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(FRIST_ENTER_SAFE, true).commit();
    }


    public static void changePrefsSortBy(FileManagerApplication mApplication, int sort) {
        mApplication.setDefaultSortBy(sort);
        SharedPreferences.Editor editor = getSharedPreferences(mApplication).edit();
        editor.putInt(CommonIdentity.PREF_SORT_BY, sort);
        editor.commit();
    }

    public static int getPrefsSortBy(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        if (PlfUtils.getBoolean(context, "def_Sort_type_name")) {
            return prefs.getInt(CommonIdentity.PREF_SORT_BY, 0);
        } else {
            return prefs.getInt(CommonIdentity.PREF_SORT_BY, 1);
        }
    }

    public static boolean changePrefsShowHidenFile(Context context) {
        boolean hide = SharedPreferenceUtils.isShowHidden(context);
        mApplication.isShowHidden = !hide;
        SharedPreferenceUtils.setShowHidden(context, !hide);
        return hide;
    }

    public static int getPrefsShowHidenFile(Context context) {
        return mApplication.isShowHidden ? CommonIdentity.FILE_FILTER_TYPE_DEFAULT
                : CommonIdentity.FILE_FILTER_TYPE_ALL;
    }

    public static void changePrefViewBy(Context context, String viewMode) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(CommonIdentity.PREF_VIEW_BY, viewMode);
        editor.commit();
    }

    public static String getPrefsViewBy(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(CommonIdentity.PREF_VIEW_BY, CommonIdentity.LIST_MODE);
    }

    public static String getPrefCurrTag(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(CommonIdentity.PREF_CURR_TAG, CommonIdentity.CATEGORY_TAG);
    }

    public static void changePrefCurrTag(Context context, String currTag) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(CommonIdentity.PREF_CURR_TAG, currTag);
        editor.commit();
    }

    public static void changeSafePrefCurrTag(Context context, String currTag) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(CommonIdentity.SAFE_PREF_CURR_TAG, currTag);
        editor.commit();
    }

    public static String getSafePrefCurrTag(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(CommonIdentity.SAFE_PREF_CURR_TAG, CommonIdentity.CATEGORY_TAG);
    }


    public static String getPermissionPrefCurrTag(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(CommonIdentity.PREF_CURR_TAG, CommonIdentity.PERMISSION_TAG);
    }


    public static void changePrefsStatus(Context mContext, int viewStatus) {
        SharedPreferences.Editor editor = getSharedPreferences(mContext).edit();
        editor.putInt(CURRENT_VIEW_STATUS, viewStatus);
        editor.commit();
    }

    public static int getPrefsStatus(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(CURRENT_VIEW_STATUS, 1);
    }

    public static void resetPrefsStatus(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        if (prefs.contains(CURRENT_VIEW_STATUS)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(CURRENT_VIEW_STATUS);
            editor.commit();
        }
    }

    /**
     * save category count in to SharedPreferences("category")
     * @param context
     * @param map count info
     */
    public static void saveCategoryCountInfo(Context context, Map<Integer, String> map) {
        Log.d("SharedPreferenceUtils", "saveCategoryCountInfo()");
        if (context == null || map == null || map.size() == 0) {
            return;
        }

        JSONArray jsonArray = new JSONArray();
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        JSONObject object = new JSONObject();
        while (iterator != null && iterator.hasNext()) {
            try {
                Map.Entry<Integer, String> entry = iterator.next();
                object.put(String.valueOf(entry.getKey()), entry.getValue());
            } catch (JSONException e) {

                Log.e("SharedPreferenceUtils", "JSONException occurred when saveCategoryCountInfo !");
            }
        }
        jsonArray.put(object);
        SharedPreferences sp = context.getSharedPreferences("category", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("category_count", jsonArray.toString());
        editor.commit();
    }

    /**
     * get category count info from haredPreferences("category")
     * @param context
     * @return
     */
    public static Map<Integer, String> getCategoryCountInfo(Context context) {
        Log.d("SharedPreferenceUtils", "getCategoryCountInfo()");
        Map<Integer, String> map = new HashMap<Integer, String>();
        if (context == null) {
            return map;
        }
        SharedPreferences sp = context.getSharedPreferences("category", Context.MODE_PRIVATE);
        String result = sp.getString("category_count", "");
        if (TextUtils.isEmpty(result)) {
            return map;
        }

        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject obj = jsonArray.getJSONObject(0);
            JSONArray names = obj.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    String name = names.getString(i);
                    String value = (obj.getString(name));
                    map.put(Integer.valueOf(name), value);
                }
            }
        } catch (JSONException e) {
            Log.e("SharedPreferenceUtils", "JSONException occurred when getCategoryCountInfo !");
        }

        return map;
    }

}
