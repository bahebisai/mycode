
package com.jrdcom.filemanager.view;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileItemView {

    private View rootView;
    private TextView mName;
    private TextView mTime;
    private TextView mSize;
    private ImageView mIcon;
    private ImageView mShowIcon;
    private CheckBox mCheckBox;
    private Bitmap mThumbnail;
    private ImageView moreMenu;
    private FrameLayout imageFrameLayout;
    private LinearLayout gridMesLayout;
    private LinearLayout mesbackLayout;
    private LinearLayout bottomLayout;
    private TextView lineview;
    private ImageView selectedIcon;
    private ImageView privateIcon;

    /**
     * The constructor to construct an edit view tag
     *
     * @param name the name view of the item
     * @param size the size view of the item
     * @param icon the icon view of the item
     * @param box  the check box view of the item
     */
    public FileItemView(TextView name, TextView time, TextView size, ImageView icon, ImageView menu, TextView line) {
        //, CheckBox box) {
        mName = name;
        mTime = time;
        mSize = size;
        mIcon = icon;
        moreMenu = menu;
        lineview = line;
        // mCheckBox = box;
    }

    public FileItemView(TextView name, ImageView icon, ImageView showIcon, TextView time, TextView size, ImageView menu, LinearLayout layout, LinearLayout mesbLayout, LinearLayout bottomview, TextView gridlineView,ImageView mPrivateIcon) {
        //, CheckBox box) {
        mName = name;
        mIcon = icon;
        mShowIcon = showIcon;
        moreMenu = menu;
        mTime = time;
        mSize = size;
        mesbackLayout = layout;
        gridMesLayout = mesbLayout;
        bottomLayout = bottomview;
        lineview = gridlineView;
        privateIcon = mPrivateIcon;
        //mCheckBox = box;
    }

    public FileItemView(TextView name, TextView time, TextView size, ImageView icon, ImageView menu,
                        TextView line, ImageView selected,ImageView mPrivateIcon) {
        mName = name;
        mTime = time;
        mSize = size;
        mIcon = icon;
        moreMenu = menu;
        lineview = line;
        selectedIcon = selected;
        privateIcon = mPrivateIcon;
    }

    public ImageView getSelectedIcon() {
        return selectedIcon;
    }

    public void setName(ImageView icon) {
        selectedIcon = icon;
    }

    public FrameLayout getImageFrameLayout() {
        return imageFrameLayout;
    }

    public void setImageFrameLayout(FrameLayout imageFrameLayout) {
        this.imageFrameLayout = imageFrameLayout;
    }

    public TextView getName() {
        return mName;
    }

    public void setName(TextView name) {
        mName = name;
    }

    public void setTime(TextView time) {
        mTime = time;
    }

    public TextView getTime() {
        return mTime;
    }

    public TextView getSize() {
        return mSize;
    }

    public void setSize(TextView size) {
        mSize = size;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public void setIcon(ImageView icon) {
        mIcon = icon;
    }

    public ImageView getPrivateIcon() {
        return privateIcon;
    }

    public void setPrivateIcon(ImageView privateIcon) {
        this.privateIcon = privateIcon;
    }

    public ImageView getShowIcon() {
        return mShowIcon;
    }

    public void setShowIcon(ImageView icon) {
        mShowIcon = icon;
    }

//    public CheckBox getCheckBox() {
//        return mCheckBox;
//    }
//
//    public void setCheckBox(CheckBox checkBox) {
//        mCheckBox = checkBox;
//    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public ImageView getMoreMenu() {
        return moreMenu;
    }

    public void setMoreMenu(ImageView moreMenu) {
        this.moreMenu = moreMenu;
    }

    public void setThumbnail(Bitmap thumbnail) {
        mThumbnail = thumbnail;
    }

    public LinearLayout getMesbackLayout() {
        return mesbackLayout;
    }

    public void setMesbackLayout(LinearLayout mesbackLayout) {
        this.mesbackLayout = mesbackLayout;
    }

    public LinearLayout getGridMesLayout() {
        return gridMesLayout;
    }

    public void setGridMesLayout(LinearLayout gridMesLayout) {
        this.gridMesLayout = gridMesLayout;
    }

    public LinearLayout getBottomLayout() {
        return bottomLayout;
    }

    public void setBottomLayout(LinearLayout bottomLayout) {
        this.bottomLayout = bottomLayout;
    }

    public TextView getLineview() {
        return lineview;
    }

    public void setLineview(TextView lineview) {
        this.lineview = lineview;
    }

}
