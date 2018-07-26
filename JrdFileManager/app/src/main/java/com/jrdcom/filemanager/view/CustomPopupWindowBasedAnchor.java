package com.jrdcom.filemanager.view;


import android.app.Activity;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView; // MODIFIED by Chuanzhi.Shao, 2017-07-04,BUG-4974642
import android.widget.PopupWindow;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.DisplayUtil;


public class CustomPopupWindowBasedAnchor extends CustomPopupWindow {

    private PopupWindow mPopupWindow;
    private DisplayUtil mDisplayUtil;
    private int mWidth;
    private int mHeight;
    Resources mRes;

    public CustomPopupWindowBasedAnchor(View contentView, int width, int height,
                                        Activity activity) {
        super(contentView, width, height,activity);
        mRes = activity.getResources();
        mWidth = width;
        mHeight = height;
        mPopupWindow = new PopupWindow(contentView, width, height);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.setAnimationStyle(R.style.AnimationPreview);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(mRes.getDrawable(R.drawable.pop_menu_bg));
        if (android.os.Build.VERSION.SDK_INT > 20) {
            mPopupWindow.setElevation(mRes.getDimension(R.dimen.floating_window_z));
        }
        mDisplayUtil = new DisplayUtil(activity);
        //Log.d("POP","CustomPopupWindowBasedAnchor() find ::::", new Exception());
    }

    public void showForCustomedOptionsMenu(View anchor, int x, int y) {
        mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
    }

    public void showAsDropDownBasedAnchor(View anchor, int offX, int offY) {
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            int xOff = 0;
            int yOff = 0;
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            int width = anchor.getWidth();
            int height = anchor.getHeight();
            int centerX = location[0] + width / 2;
            int centerY = location[1] + height / 2;
            int screenWidth = mDisplayUtil.getScreenWidth();
            int screenHeight = mDisplayUtil.getScreenHeight();
            int targetX = centerX - offX;
            int targetY = centerY - offY;
            int dx = screenWidth - targetX;
            int dy = screenHeight - targetY;
            if (mWidth > 0) {
                if (dx >= mWidth) {
                    xOff = width / 2 - offX;
                } else {
                    xOff = width / 2 - mWidth + (offX * 2);
                }
            } else {
                xOff = width / 2 - offX;
            }
            if (mHeight > 0) {
                if (dy >= mHeight) {
                    yOff = -height / 2 - offY;
                } else {
                    yOff = -height / 2 + (offY * 2);
                }
            } else {
                yOff = -height / 2 - offY;
            }
            mPopupWindow.showAsDropDown(anchor, xOff, yOff);
        }
    }

    public void showAtLocationBasedAnchor(View anchor, int xOff, int yOff) {
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mHeight = mPopupWindow.getHeight();
            mPopupWindow.getMaxAvailableHeight(anchor);
            int xOFF = 0;
            int yOFF = 0;
            int[] location = new int[2];

            // use getLocationInWindow to adapter multi-window mode
            anchor.getLocationInWindow(location);
            int width = anchor.getWidth();
            int height = anchor.getHeight();
            int screenWidth = mDisplayUtil.getScreenWidth();
            int screenHeight = mDisplayUtil.getScreenHeight();
            int centerX = location[0] + width / 2;
            int centerY = location[1] + height / 2;

            // if anchor is partly display in bottom, handle the hidden part
            if ((location[1] + height) > screenHeight) {
                centerY = location[1] + (screenHeight - location[1]) /2;
            }

            int targetX = centerX - xOff;
            int targetY = centerY - yOff;
            int dx = screenWidth - targetX;
            int dy = screenHeight - targetY;
            boolean isLeft=false;
            if (mWidth > 0) {
                if (dx >= mWidth) {
                    xOFF = targetX;
                    isLeft=true;
                } else {
                    xOFF = targetX - mWidth + 2 * xOff;
                }
            } else {
                xOFF = targetX;
            }
            // check PopWindow height with screen height
            int maxHeight = screenHeight - (int)(mDisplayUtil.getScreenScaledDensity() * 2); // margin 2 dp with bottom
            if (mHeight > maxHeight) {
                mHeight = maxHeight;
                mPopupWindow.setHeight(mHeight);
            }

            if (mHeight > 0) {
                if (dy >= mHeight) {
                    yOFF = targetY;
                    if(isLeft)mPopupWindow.setAnimationStyle(R.style.AnimationPreviewLeft);
                } else {
                    if(isLeft){
                        mPopupWindow.setAnimationStyle(R.style.AnimationPreviewLefUp);
                    }else {
                        mPopupWindow.setAnimationStyle(R.style.AnimationPreviewUp);
                    }
                    yOFF = targetY - mHeight + 2 * yOff;
                    // if pop window beyond the display screen, set the value by screenHeight
                    if (yOFF + mHeight > maxHeight) {
                        yOFF = maxHeight - mHeight;
                    }
                }
            } else {
                yOFF = targetY;
            }

            // if yOFF < 0, reset it to avoid some display issues
            if (yOFF < 0) {
                yOFF = 0;
            }

            mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xOFF, yOFF);
        }
    }

    //[BUG-FIX]-BEGIN by NJTS Junyong.Sun 2016/01/22 PR-1445808
    public void update(Activity activity) {
        if (mPopupWindow != null) {
            mDisplayUtil = new DisplayUtil(activity);
            mPopupWindow.update();
        }
    }

    public void update(View view, Activity activity) {
        if (mPopupWindow != null) {
            mPopupWindow.setContentView(view);
            update(activity);
        }
    }

    //[BUG-FIX]-END by NJTS Junyong.Sun 2016/01/22 PR-1445808
    @Override
    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public boolean isShowing() {
        if (mPopupWindow != null) {
            return mPopupWindow.isShowing();
        }

        return false;
    }

    /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-07-04,BUG-4974642*/
    public void showAtLocationBasedAnchorMy(ImageView anchor, int xOff, int yOff) {
        if (mPopupWindow != null && !mPopupWindow.isShowing()) {
            mHeight = mPopupWindow.getHeight();
            mPopupWindow.getMaxAvailableHeight(anchor);
            int xOFF = 0;
            int yOFF = 0;
            int[] location = new int[2];

            // use getLocationInWindow to adapter multi-window mode
            anchor.getLocationInWindow(location);
            int width = anchor.getWidth();
            int height = anchor.getHeight();
            int screenWidth = mDisplayUtil.getScreenWidth();
            int screenHeight = mDisplayUtil.getScreenHeight();
            int centerX = location[0] + width / 2;
            int centerY = location[1] + height / 2;

            // if anchor is partly display in bottom, handle the hidden part
            if ((location[1] + height) > screenHeight) {
                centerY = location[1] + (screenHeight - location[1]) /2;
            }

            int targetX = centerX - xOff;
            int targetY = centerY - yOff;
            int dx = screenWidth - targetX;
            int dy = screenHeight - targetY;
            boolean isLeft=false;
            if (mWidth > 0) {
                if (dx >= mWidth) {
                    xOFF = targetX;
                    isLeft=true;
                } else {
                    xOFF = targetX - mWidth + 2 * xOff;
                }
            } else {
                xOFF = targetX;
            }
            // check PopWindow height with screen height
            int maxHeight = screenHeight - (int)(mDisplayUtil.getScreenScaledDensity() * 2); // margin 2 dp with bottom
            if (mHeight > maxHeight) {
                mHeight = maxHeight;
                mPopupWindow.setHeight(mHeight);
            }

            if (mHeight > 0) {
                if (dy >= mHeight) {
                    yOFF = targetY;
                    if(isLeft)mPopupWindow.setAnimationStyle(R.style.AnimationPreviewLeft);
                } else {
                    if(isLeft){
                        mPopupWindow.setAnimationStyle(R.style.AnimationPreviewLefUp);
                    }else {
                        mPopupWindow.setAnimationStyle(R.style.AnimationPreviewUp);
                    }
                    yOFF = targetY - mHeight + 2 * yOff;
                    // if pop window beyond the display screen, set the value by screenHeight
                    if (yOFF + mHeight > maxHeight) {
                        yOFF = maxHeight - mHeight;
                    }
                }
            } else {
                yOFF = targetY;
            }

            // if yOFF < 0, reset it to avoid some display issues
            if (yOFF < 0) {
                yOFF = 0;
            }

            mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xOFF, yOFF);
        }

    }
    /* MODIFIED-END by Chuanzhi.Shao,BUG-4974642*/
}
