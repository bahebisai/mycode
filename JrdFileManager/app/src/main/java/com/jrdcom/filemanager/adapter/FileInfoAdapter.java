package com.jrdcom.filemanager.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.manager.FileInfoComparator;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.manager.IconManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.view.FileItemView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileInfoAdapter extends BaseAdapter {


    protected final List<FileInfo> mFileInfoList = new ArrayList<FileInfo>();
    protected final FileInfoManager mFileInfoManager;
    protected final Context mContext;
    protected IconManager mIconManager;
    private SimpleDateFormat mDateFormat;
    private ListView mListView;
    protected FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();

    /**
     * The constructor to construct a FileInfoAdapter.
     *
     * @param context            the context of FileManagerActivity
     * @param fileManagerService the service binded with FileManagerActivity
     * @param fileInfoManager    a instance of FileInfoManager, which manages all
     *                           files.
     */
    public FileInfoAdapter(Context context, FileInfoManager fileInfoManager, ListView listView) {
        mFileInfoManager = fileInfoManager;
        if (fileInfoManager != null) {
            mFileInfoList.addAll(fileInfoManager.getShowFileList());
        }
        mContext = context;
        mIconManager = IconManager.getInstance();
        mListView = listView;
    }

    public void refresh() {
        mFileInfoList.clear();
        mFileInfoList.addAll(mFileInfoManager.getShowFileList());
    }

    /**
     * return the list in the adapters.
     *
     * @return return the list.
     */
    public List<FileInfo> getList() {
        return mFileInfoList;
    }


    /**
     * This method gets index of certain fileInfo in fileInfoList
     *
     * @param fileInfo the fileInfo which wants to be located.
     * @return the index of the item in the listView.
     */
    public int getPosition(FileInfo fileInfo) {
        return mFileInfoList.indexOf(fileInfo);
    }

    public boolean isHasDrm(int position) {
        try {
            if (mFileInfoList.size() <= position || position < 0) {
                return false;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);
            return itemEditInfo.isDrmFile();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method gets the count of the items in the name list
     *
     * @return the number of the items
     */
    @Override
    public int getCount() {
        return mFileInfoList.size();
    }

    /**
     * This method gets the name of the item at the specified position
     *
     * @param pos the position of item
     * @return the name of the item
     */
    @Override
    public FileInfo getItem(int pos) {
        // Because mFileInfoList is used to be async by other software engineers
        // I have to make a judge due to the IndexOutOfBoundsException
        if (pos < mFileInfoList.size()) {
            return mFileInfoList.get(pos);
        } else {
            return null;
        }
    }

    /**
     * This method gets the item id at the specified position
     *
     * @param pos the position of item
     * @return the id of the item
     */
    @Override
    public long getItemId(int pos) {
        return pos;
    }

    /**
     * This method gets the view to be displayed
     *
     * @param pos         the position of the item
     * @param convertView the view to be shown
     * @param parent      the parent view
     * @return the view to be shown
     */
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        return getListView(pos, convertView, parent);

    }

    /**
     * This method gets the list view to be desplayed in list view.
     *
     * @param pos         the position of the item
     * @param convertView convertView the view to be shown
     * @param parent      parent the parent view
     * @return the list view to be shown
     */
    public View getListView(int pos, View convertView, ViewGroup parent) {
        final FileItemView viewHolder;
        final int position = pos;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_list_item, null);
            viewHolder = new FileItemView(
                    (TextView) convertView.findViewById(R.id.edit_adapter_name),
                    (TextView) convertView.findViewById(R.id.edit_adapter_time),
                    (TextView) convertView.findViewById(R.id.edit_adapter_size),
                    (ImageView) convertView.findViewById(R.id.edit_adapter_img),
                    (ImageView) convertView.findViewById(R.id.edit_moreMenu),
                    (TextView) convertView.findViewById(R.id.search_line));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FileItemView) convertView.getTag();
        }

        FileInfo currentItem = getItem(pos);
        if (currentItem != null) {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.list_corners_bg));
            setNameText(viewHolder.getName(), currentItem);
            TextView mSize = viewHolder.getSize();
            ImageView mIcon = viewHolder.getIcon();
            String filePath = currentItem.getFileAbsolutePath();
            mIcon.setTag("icon" + filePath);
            mSize.setTag("size" + filePath);
            switch (SharedPreferenceUtils.getPrefsStatus(mContext)) {
                case CommonIdentity.FILE_STATUS_EDIT:
                    viewHolder.getMoreMenu().setVisibility(View.GONE);
                    viewHolder.getLineview().setVisibility(View.VISIBLE);
                    setTimeSizeText(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                case CommonIdentity.FILE_STATUS_NORMAL:
                    viewHolder.getMoreMenu().setVisibility(View.GONE);
                    viewHolder.getLineview().setVisibility(View.VISIBLE);
                    setTimeSizeText(viewHolder.getTime(), viewHolder.getSize(), currentItem);
                    break;
                default:
                    break;
            }
            setIcon(pos, viewHolder, currentItem, IconManager.LIST_ITEM);
        }
        return convertView;
    }

    private void setIcon(final int pos, final FileItemView viewHolder, final FileInfo fileInfo, int mode) {
        int iconId = mIconManager.getIcon(fileInfo, mode);
        String filePath = fileInfo.getFileAbsolutePath();
        viewHolder.getIcon().setScaleType(ImageView.ScaleType.CENTER);
        viewHolder.getIcon().setImageResource(iconId);
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        Drawable drawable = mIconManager.getImageCacheDrawable(filePath);
        if (drawable != null) {
            viewHolder.getIcon().setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.getIcon().setImageDrawable(drawable);
        } else {
            loadImage(fileInfo, viewHolder);
        }


        if (fileInfo.isHideFile()) {
            viewHolder.getIcon().setAlpha(CommonIdentity.HIDE_ICON_ALPHA);
        } else if (mFileInfoManager.getPasteType() == CommonIdentity.PASTE_MODE_CUT
                && mFileInfoManager.isPasteItem(fileInfo)) {
            viewHolder.getIcon().setAlpha(CommonIdentity.CUT_ICON_ALPHA);
        } else {
            viewHolder.getIcon().setAlpha(CommonIdentity.DEFAULT_ICON_ALPHA);
        }

    }

    protected void loadImage(final FileInfo fileInfo, final FileItemView viewHolder) {
        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ImageView iconByTag = viewHolder.getIcon();
                if (iconByTag != null && iconByTag.getTag().equals("icon" + fileInfo.getFileAbsolutePath())) {
                    iconByTag.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iconByTag.setImageDrawable((Drawable) msg.obj);
                }
            }
        };
        mIconManager.loadImage(mContext, fileInfo,
                new IconManager.IconCallback() {
                    public void iconLoaded(Drawable iconDrawable) {
                        if (iconDrawable != null) {
                            Message message = mHandler.obtainMessage(0, 1, 1, iconDrawable);
                            mHandler.sendMessage(message);
                        }
                    }
                });
    }

    /**
     * This method gets current mode of the adapter.
     *
     * @return current display mode of adapter
     */
    public int getMode() {
        return SharedPreferenceUtils.getPrefsStatus(mContext);
    }

    /**
     * This method checks that current mode equals to certain mode, or not.
     *
     * @param mode the display mode of adapter
     * @return true for equal, and false for not equal
     */
    public boolean isMode(int mode) {
        return mApplication.mCurrentStatus == mode;
    }


    public void setTimeSizeText(TextView timeView, TextView sizeView, final FileInfo fileInfo) {
        long time = fileInfo.getFileLastModifiedTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        if (mDateFormat == null) {
            try {
                //read data format from System Setting
                ContentResolver cv = mContext.getContentResolver();
                String strDateFormat = android.provider.Settings.System.getString(cv,
                        android.provider.Settings.System.DATE_FORMAT);
                if (TextUtils.isEmpty(strDateFormat)) {
                    strDateFormat = "yyyy-MM-dd HH:mm";
                    mDateFormat = new SimpleDateFormat(strDateFormat);
                } else {
                    mDateFormat = new SimpleDateFormat(strDateFormat+" HH:mm");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (mDateFormat != null) {
            String mModifiedTime = mDateFormat.format(new Date(time)).toString();
            timeView.setText(mModifiedTime);
        }

        if (!fileInfo.isDirectory()) {
            if (mApplication.mSortType == FileInfoComparator.SORT_BY_TIME) {
                sizeView.setVisibility(View.GONE);
                timeView.setVisibility(View.VISIBLE);
            } else {
                sizeView.setVisibility(View.VISIBLE);
                timeView.setVisibility(View.GONE);
                sizeView.setText(fileInfo.getFileSizeStr());
            }
        } else {
            sizeView.setVisibility(View.GONE);
            timeView.setVisibility(View.VISIBLE);

        }
    }

    protected void setNameText(TextView textView, FileInfo fileInfo) {

        if (fileInfo.isDirectory()
                && !MountManager.getInstance().isMountPoint(fileInfo.getFileAbsolutePath())) {
            textView.setText(fileInfo.getShowName());
        } else {
            textView.setText(fileInfo.getShowName());
        }
    }


    protected List<FileInfo> mCheckedFileList = new ArrayList<FileInfo>();
    protected List<FileInfo> mItemEditFileList = new ArrayList<FileInfo>();
}
