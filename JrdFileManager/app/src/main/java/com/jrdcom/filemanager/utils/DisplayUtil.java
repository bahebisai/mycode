/*****************************************************************************/

/*                                                            Date : 06/2013 */

/*                            PRESENTATION                                   */

/*              Copyright (c) 2010 JRD Communications, Inc.                  */

/*****************************************************************************/
/*                                                                           */
/*    This material is company confidential, cannot be reproduced in any     */
/*    form without the written permission of JRD Communications, Inc.        */
/*                                                                           */
/*---------------------------------------------------------------------------*/

/*   Author :  Yaping.Liu                                                    */

/*   Role   :                                                                */

/*   Reference documents :                                                   */

/*---------------------------------------------------------------------------*/

package com.jrdcom.filemanager.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jrdcom.filemanager.FileManagerApplication;

/**
 * @author yaping.liu
 * @company TCL Communication Technology Holdings Ltd. Shanghai R&D center
 * @date Apr 25, 2013
 * @description DisplayUtil.java
 */
public class DisplayUtil {
    private Activity mActivity;
    private FileManagerApplication mApplication;
    private Context mContext;

    private int width;
    private int height;
    private float density;
    private int densityDpi;
    private float fontScale;


    /**
     * @param activity
     */
    public DisplayUtil(Activity activity) {
        super();
        this.mActivity = activity;
        mApplication = (FileManagerApplication)FileManagerApplication.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        this.width = metric.widthPixels;
        this.height = metric.heightPixels;
        this.density = metric.density;
        this.densityDpi = metric.densityDpi;
        this.fontScale = metric.scaledDensity;
    }

    /**
     * @param Context
     */
    public DisplayUtil(Context context) {
        super();
        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        this.mContext = context;
        mApplication = (FileManagerApplication)FileManagerApplication.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        this.width = metric.widthPixels;
        this.height = metric.heightPixels;
        this.density = metric.density;
        this.densityDpi = metric.densityDpi;
        this.fontScale = metric.scaledDensity;
    }

    /**
     * get screen width
     *
     * @return
     */
    public int getScreenWidth() {
        return width;
    }

    /**
     * get screen height
     *
     * @return
     */
    public int getScreenHeight() {
        return height;
    }

    /**
     * get screen density
     *
     * @return
     */
    public float getScreenDensity() {
        return density;
    }

    /**
     * get screen DensityDpi
     *
     * @return
     */
    public int getScreenDensityDpi() {
        return densityDpi;
    }

    /**
     * get screen scaledDensity
     *
     * @return
     */
    public float getScreenScaledDensity() {
        return fontScale;
    }

    /**
     * change px to dip or dp
     *
     * @param pxValue
     * @param scale
     * @return
     */
    public int pxToDip(float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * change dip or dp to px
     *
     * @param dipValue
     * @param scale
     * @return
     */
    public int dipToPx(float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }

    /**
     * change px to sp
     *
     * @param pxValue
     * @param fontScale
     * @return
     */
    public int pxToSp(float pxValue) {
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * change sp to px
     *
     * @param spValue
     * @param fontScale
     * @return
     */
    public int spToPx(float spValue) {
        return (int) (spValue * fontScale + 0.5f);
    }
}
