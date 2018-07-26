/* Copyright (C) 2016 Tcl Corporation Limited */
package com.jrdcom.filemanager.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FileBrowserActivity;
import com.jrdcom.filemanager.activity.FileSafeBrowserActivity;
import com.jrdcom.filemanager.drm.DrmManager;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.FileInfoComparator;
import com.jrdcom.filemanager.manager.FileInfoManager;
import com.jrdcom.filemanager.manager.FolderCountManager;
import com.jrdcom.filemanager.manager.IconManager;
import com.jrdcom.filemanager.manager.MountManager;
import com.jrdcom.filemanager.manager.PrivateModeManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.task.FolderCountTask;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.FileInfo;
import com.jrdcom.filemanager.utils.FileUtils;
import com.jrdcom.filemanager.utils.SafeUtils;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.utils.TaskInfo;
import com.jrdcom.filemanager.view.CustomPopupWindowBasedAnchor;
import com.jrdcom.filemanager.view.FileItemView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import tct.util.privacymode.TctPrivacyModeHelper;


/**
 * Created by user on 16-7-20.
 */
public class FileShowAdapter extends RecyclerView.Adapter<FileShowAdapter.MyViewHolder> {

    public static final String TAG = FileShowAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private RecyclerView mListView;
    private boolean isThirdAPP = false;
    private boolean isPathLocation = false;
    private boolean isPrivateLocation = false;
    private boolean isSafeboxLocation = false;
    private boolean isInPrivacyMode = false;
    private Context mContext;
    private SimpleDateFormat mDateFormat;
    protected FileInfoManager mFileInfoManager;
    protected final List<FileInfo> mFileInfoList = new ArrayList<FileInfo>();
    private FileManagerApplication mApplication;
    protected List<FileInfo> mCheckedFileList = new ArrayList<FileInfo>();
    protected List<FileInfo> mItemEditFileList = new ArrayList<FileInfo>();
    protected IconManager mIconManager;
    private OnItemClickLitener mOnItemClickLitener;
    protected int selectedPosition = -1;
    public CustomPopupWindowBasedAnchor mItemMorePop;
    protected int mItemMorePopWidth;
    protected int mItemMorePopHeight;
    protected int mMultiScreenModeHeight;
    private int xoff;
    private int mFileShowListCount;
    private MountManager mMountManager;

    private int yoff;
    private long mCurrentTime;
    private TctPrivacyModeHelper mPrivacyModeHelper;
    private int mPosition; // MODIFIED by Chuanzhi.Shao, 2017-07-04,BUG-4974642

    public FileShowAdapter(Context context, FileInfoManager fileInfoManager, RecyclerView listView) {
        mContext = context;
        mFileInfoManager = fileInfoManager;
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        mInflater = LayoutInflater.from(context);
        mFileInfoList.addAll(getShowOrHideFileList(mFileInfoManager));
        mIconManager = IconManager.getInstance();
        mPrivacyModeHelper = TctPrivacyModeHelper.createHelper(mContext);
        mMountManager = MountManager.getInstance();
        isPathLocation = CommonUtils.isFilePathLocation(mApplication);
        mListView = listView;
        mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(mContext);
        mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.more_menu_land_pop_multishare_height);
        mMultiScreenModeHeight = mContext.getResources().getDimensionPixelSize(R.dimen.more_menu_multiscreen_pop_height);
        mItemMorePopWidth = context.getResources().getDimensionPixelSize(R.dimen.sort_menu_width);
        if (CommonUtils.isListMode(mApplication)) {
            xoff = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listxoff);
            yoff = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_listyoff);
        } else {
            xoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_gridxoff);
            yoff = context.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_gridyoff);
        }
        initMorePopWindow(false, false,false,false,false,false,0,null,false);
    }

    /**
     * change mode by List or Grid
     **/
    public void setViewMode(String ViewMode) {
        mApplication.mViewMode = ViewMode;
        SharedPreferenceUtils.changePrefViewBy(mContext, ViewMode);
        notifyDataSetChanged();
    }

    /**
     * Can directly pass the list for update.
     *
     * @param list update list.
     */
    public void refresh(List<FileInfo> list) {
        mFileInfoList.clear();
        mFileInfoList.addAll(list);
    }

    /**
     * return the list in the adapters.
     *
     * @return return the list.
     */
    public List<FileInfo> getList() {
        return mFileInfoList;
    }

    /* MODIFIED-BEGIN by Chuanzhi.Shao, 2017-07-04,BUG-4974642*/
    public void afreshPopWindow(RecyclerView mRecyclerView) {
        if (mPosition == -1)return;
        if (mItemMorePop.isShowing()){
            FileShowAdapter.MyViewHolder holder = (MyViewHolder) mRecyclerView.findViewHolderForLayoutPosition(mPosition);
            if (holder != null){
                afreshPop(holder,mPosition);
            }
        }
    }

    private void afreshPop(MyViewHolder holder, int position) {
        try {
            CommonUtils.hideSoftInput((Activity) mContext);
            setItemEditSelect(position);
            if(getItemEditFileInfoList().size() <= 0) return;
            FileInfo info = getItemEditFileInfoList().get(0);
            boolean isDir = info.isDirectory();
            boolean isZip = false;
            boolean isPrivate = false;
            boolean isDrm = false;
            boolean isSDDrm = false;
            boolean isCanShare = false;
            boolean isSupportPrivacyMode = CommonUtils.isSupportPrivacyMode(mContext);
            if (!isDir) {
                isDrm = isHasDrm(position);
                if (isDrm) {
                    isSDDrm = isHasSDDrm(position);
                }
                isCanShare = isCanShare(position);
            }
            String mAbsolutePath = info.getFileAbsolutePath();
            if (!isDir && isInPrivacyMode && !CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage)) {
                isPrivate = info.isPrivateFile();
            }
            mPosition = position;
            if (isSupportPrivacyMode && isSafeboxLocation) {
                mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.more_menu_land_pop_multishare_height);
                showPopWindowMy(isZip, isDir, isPrivate, isDrm, isSDDrm, isCanShare, position, mItemMorePopWidth, mItemMorePopHeight, holder,mAbsolutePath,isSupportPrivacyMode);
                return;
            } else if (CommonUtils.isCategoryMode()) {
                mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_category_height);
            } else {
                mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_path_height);
            }
            if (isDir) {
                isZip = false;
            } else {
                if (info.getFileName() == null) {
                    isZip = false;
                } else {
                    String fileName = FileUtils.getFileExtension(info.getFileName());
                    if (TextUtils.isEmpty(fileName)) {
                        isZip = false;
                    } else {
                        isZip = fileName.equalsIgnoreCase("zip") ||
                                fileName.equalsIgnoreCase("tar")
                                || fileName.equalsIgnoreCase("rar");
                    }
                }
                if (isDrm && isSDDrm && !isCanShare) {
                    mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_sd_drm_height);
                } else if (isDrm && !isCanShare) {
                    mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_drm_height);
                } else if (isDrm && isCanShare) {
                    mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_share_height);
                } else if (!isDrm && isCanShare && isSupportPrivacyMode) {
                    if (!CommonUtils.isCategoryMode() && !CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage)) {
                        mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_path_private_height);
                    } else if(CommonUtils.isCategoryMode() && CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage)){
                        mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_category_height);
                    } else {
                        mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_private_height);
                    }
                }
            }
            showPopWindowMy(isZip, isDir, isPrivate, isDrm, isSDDrm, isCanShare, position, mItemMorePopWidth, mItemMorePopHeight, holder,mAbsolutePath,isSupportPrivacyMode);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPopWindowMy(boolean isZip, boolean isDir, boolean isPrivate, boolean isDrm, boolean isSDDrm, boolean isCanShare, int position, int mItemMorePopWidth, int mItemMorePopHeight, MyViewHolder holder, String mAbsolutePath, boolean isSupportPrivacyMode) {
        mItemMorePop.dismiss();
        mItemMorePop = new CustomPopupWindowBasedAnchor(
                initMorePopWindow(isZip,isDir,isPrivate,isDrm,isSDDrm,isCanShare,position,mAbsolutePath,isSupportPrivacyMode),
                mItemMorePopWidth, mItemMorePopHeight,
                (Activity) mContext);

        if (mItemMorePop != null)
            mItemMorePop.showAtLocationBasedAnchorMy(holder.fileitemView.getMoreMenu(), xoff, yoff);
    }
    /* MODIFIED-END by Chuanzhi.Shao,BUG-4974642*/

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        boolean onItemLongClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder;
        mCurrentTime = System.currentTimeMillis();
        mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(mContext);
        mDateFormat = CommonUtils.getSystemDateFormat(mContext);
        isPrivateLocation = mApplication.mCurrentLocation == CommonIdentity.FILE_PRIVATE_LOCATION;
        isSafeboxLocation = mApplication.mCurrentLocation == CommonIdentity.FILE_SAFEBOX_LOCATION;
        isInPrivacyMode = CommonUtils.isInPrivacyMode(mApplication);
        if (CommonUtils.isGridMode(mApplication)) {
            viewHolder = new MyViewHolder(mInflater.inflate(R.layout.file_grid_item, parent, false));
        } else {
            viewHolder = new MyViewHolder(mInflater.inflate(R.layout.file_list_item, parent, false));
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (holder == null || holder.fileitemView == null) {
            return;
        }

        FileInfo currentItem = mFileInfoList.get(position);
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return true;
                }
            });
        }
        mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(mContext);
        mApplication.mCurrentStatus = SharedPreferenceUtils.getPrefsStatus(mContext);
        if (currentItem != null && CommonUtils.isGridMode(mApplication)) {
            ImageView mIcon = holder.fileitemView.getIcon();
            TextView mNameTextView = holder.fileitemView.getName();
            TextView mFileSize = holder.fileitemView.getSize();
            TextView girdLineView = holder.fileitemView.getLineview();
            ImageView moreMenu = holder.fileitemView.getMoreMenu();
            LinearLayout mesbackLayout = holder.fileitemView.getMesbackLayout();
            LinearLayout gridLayout = holder.fileitemView.getGridMesLayout();
            String filePath = currentItem.getFileAbsolutePath();
            setNameText(mNameTextView, currentItem);
            mIcon.setTag("icon" + filePath);
            mesbackLayout.setTag("mesbac" + filePath);
            gridLayout.setTag("gridMes" + filePath);
            mNameTextView.setTag("mName" + filePath);
            girdLineView.setTag("mLine" + filePath);
            mFileSize.setTag("mSize" + filePath);
            moreMenu.setTag("moreMenu" + filePath);
            switch (mApplication.mCurrentStatus) {
                case CommonIdentity.FILE_STATUS_EDIT:
                    moreMenu.setVisibility(View.GONE);
                    setTimeSizeText(holder.fileitemView.getTime(), mFileSize, currentItem);
                    break;
                case CommonIdentity.FILE_STATUS_NORMAL:
                    moreMenu.setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), mFileSize, currentItem);
                    break;
                case CommonIdentity.FILE_STATUS_SEARCH:
                    moreMenu.setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), mFileSize, currentItem);
                    if (isSafeboxLocation) {
                        setSearchHighLight(mNameTextView, ((FileSafeBrowserActivity) mContext).getQueryText());
                    } else {
                        setSearchHighLight(mNameTextView, ((FileBrowserActivity) mContext).getQueryText());
                    }
                    break;
                case CommonIdentity.FILE_STATUS_GLOBALSEARCH:
                    setTimeSizeText(holder.fileitemView.getTime(), mFileSize, currentItem);
                    moreMenu.setVisibility(View.GONE);
                    setSearchHighLight(mNameTextView, ((FileBrowserActivity) mContext).getQueryText());
                    break;
                case CommonIdentity.FILE_COPY_NORMAL:
                    moreMenu.setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), mFileSize, currentItem);
                    break;
                default:
                    break;
            }
            if(isPathLocation && currentItem.isPrivateFile()){
                holder.fileitemView.getPrivateIcon().setVisibility(View.VISIBLE);
            } else {
                holder.fileitemView.getPrivateIcon().setVisibility(View.GONE);
            }
            if (mApplication.mCurrentStatus != CommonIdentity.FILE_STATUS_EDIT && mApplication.mCurrentStatus != CommonIdentity.FILE_STATUS_GLOBALSEARCH) {
                if (isInPrivacyMode && isPrivateLocation) {
                    moreMenu.setVisibility(View.GONE);
                } else {
                    moreMenu.setVisibility(View.VISIBLE);
                }
            }
            // set icon
            setIconGird(position, holder.fileitemView, currentItem, IconManager.GRID_ITEM);
        } else {
            if (mCheckedFileList.contains(currentItem)) {
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.gird_item_name_bg));
                holder.fileitemView.getSelectedIcon().setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setBackground(mContext.getResources().getDrawable(R.drawable.list_corners_bg));
                holder.fileitemView.getSelectedIcon().setVisibility(View.GONE);
            }
            if(isPathLocation && currentItem.isPrivateFile()){
                holder.fileitemView.getPrivateIcon().setVisibility(View.VISIBLE);
            } else {
                holder.fileitemView.getPrivateIcon().setVisibility(View.GONE);
            }
            ImageView mIcon = holder.fileitemView.getIcon();
            TextView mNameTextView = holder.fileitemView.getName();
            ImageView moreMenu = holder.fileitemView.getMoreMenu();
            String filePath = currentItem.getFileAbsolutePath();
            setNameText(mNameTextView, currentItem);
            mIcon.setTag("icon" + filePath);
            switch (mApplication.mCurrentStatus) {
                case CommonIdentity.FILE_STATUS_EDIT:
                    moreMenu.setVisibility(View.GONE);
                    holder.fileitemView.getLineview().setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), holder.fileitemView.getSize(), currentItem);
                    break;
                case CommonIdentity.FILE_STATUS_NORMAL:
                    if (isThirdAPP || isPrivateLocation) {
                        moreMenu.setVisibility(View.GONE);
                    } else {
                        moreMenu.setVisibility(View.VISIBLE);
                    }
                    holder.fileitemView.getLineview().setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), holder.fileitemView.getSize(), currentItem);
                    break;
                case CommonIdentity.FILE_STATUS_SEARCH:
                    moreMenu.setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), holder.fileitemView.getSize(), currentItem);
                    if (isSafeboxLocation) {
                        setSearchHighLight(mNameTextView, ((FileSafeBrowserActivity) mContext).getQueryText());
                    } else {
                        setSearchHighLight(mNameTextView, ((FileBrowserActivity) mContext).getQueryText());
                    }
                    break;
                case CommonIdentity.FILE_STATUS_GLOBALSEARCH:
                    setTimeSizeText(holder.fileitemView.getTime(), holder.fileitemView.getSize(), currentItem);
                    holder.fileitemView.getLineview().setVisibility(View.GONE);
                    moreMenu.setVisibility(View.GONE);
                    setSearchHighLight(mNameTextView, ((FileBrowserActivity) mContext).getQueryText());
                    break;
                case CommonIdentity.FILE_COPY_NORMAL:
                    moreMenu.setVisibility(View.VISIBLE);
                    setTimeSizeText(holder.fileitemView.getTime(), holder.fileitemView.getSize(), currentItem);
                    break;
                default:
                    break;
            }
            if (position >= getItemCount()-1 && !mCheckedFileList.contains(currentItem)) {
                holder.fileitemView.getLineview().setVisibility(View.INVISIBLE);
                holder.itemView.setBackground(mContext.getDrawable(R.drawable.list_view_shadow));
            } else {
                holder.fileitemView.getLineview().setVisibility(View.VISIBLE);
            }
            setIcon(position, holder.fileitemView, currentItem, IconManager.LIST_ITEM);
        }
        holder.fileitemView.getMoreMenu().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    CommonUtils.hideSoftInput((Activity) mContext);
                    if (!FileBrowserActivity.isMorePopShow()) {
                        setItemEditSelect(position);
                        if(getItemEditFileInfoList().size() <= 0) return;
                        FileInfo info = getItemEditFileInfoList().get(0);
                        boolean isDir = info.isDirectory();
                        boolean isZip = false;
                        boolean isPrivate = false;
                        boolean isDrm = false;
                        boolean isSDDrm = false;
                        boolean isCanShare = false;
                        boolean isSupportPrivacyMode = CommonUtils.isSupportPrivacyMode(mContext);
                        if (!isDir) {
                            isDrm = isHasDrm(position);
                            if (isDrm) {
                                isSDDrm = isHasSDDrm(position);
                            }
                            isCanShare = isCanShare(position);
                        }
                        String mAbsolutePath = info.getFileAbsolutePath();
                        if (!isDir && isInPrivacyMode && !CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage)) {
                            isPrivate = info.isPrivateFile();
                        }
                        mPosition = position; // MODIFIED by Chuanzhi.Shao, 2017-07-04,BUG-4974642
                        if (isSupportPrivacyMode && isSafeboxLocation) {
                            mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.more_menu_land_pop_multishare_height);
                            showPopWindow(isZip, isDir, isPrivate, isDrm, isSDDrm, isCanShare, position, mItemMorePopWidth, mItemMorePopHeight, holder,mAbsolutePath,isSupportPrivacyMode);
                            return;
                        } else if (CommonUtils.isCategoryMode()) {
                            mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_category_height);
                        } else {
                            mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_path_height);
                        }
                        if (isDir) {
                            isZip = false;
                        } else {
                            if (info.getFileName() == null) {
                                isZip = false;
                            } else {
                                String fileName = FileUtils.getFileExtension(info.getFileName());
                                if (TextUtils.isEmpty(fileName)) {
                                    isZip = false;
                                } else {
                                    isZip = fileName.equalsIgnoreCase("zip") ||
                                            fileName.equalsIgnoreCase("tar")
                                            || fileName.equalsIgnoreCase("rar");
                                }
                            }
                            if (isDrm && isSDDrm && !isCanShare) {
                                mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_sd_drm_height);
                            } else if (isDrm && !isCanShare) {
                                mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_drm_height);
                            } else if (isDrm && isCanShare) {
                                mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_share_height);
                            } else if (!isDrm && isCanShare && isSupportPrivacyMode) {
                                if (!CommonUtils.isCategoryMode() && !CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage)) {
                                    mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_path_private_height);
                                } else if(CommonUtils.isCategoryMode() && CommonUtils.isExternalStorage(mAbsolutePath, mMountManager,mApplication.isBuiltInStorage)){
                                    mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_category_height);
                                } else {
                                    mItemMorePopHeight = mContext.getResources().getDimensionPixelSize(R.dimen.item_more_menu_pop_private_height);
                                }
                            }
                        }
                        showPopWindow(isZip, isDir, isPrivate, isDrm, isSDDrm, isCanShare, position, mItemMorePopWidth, mItemMorePopHeight, holder,mAbsolutePath,isSupportPrivacyMode);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         *Support multi screen drag files, DRM and can not share files do not support drag.
         */
        if (CommonUtils.isListMode(mApplication) && mApplication.isInMultiWindowMode) {
            holder.fileitemView.getIcon().setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        FileInfo info = mFileInfoList.get(position);
                        if (!info.isDirectory() && isCanShare(position)) {
                            String mMime = info.getMime();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType(mMime);
                            Uri uri = info.getUri(false);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            ClipData.Item item = new ClipData.Item(null,intent,uri);
                            ClipData dragData = new ClipData(view.getTag().toString(),new String[]{mMime},item);
                            View.DragShadowBuilder shadow = new View.DragShadowBuilder(holder.fileitemView.getIcon());
                            view.startDragAndDrop(dragData, shadow, null, View.DRAG_FLAG_GLOBAL|
                                    View.DRAG_FLAG_GLOBAL_URI_WRITE|
                                    View.DRAG_FLAG_GLOBAL_URI_READ);
                            return true;
                        }
                        return false;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    private void showPopWindow(boolean isZip,boolean isDir,boolean isPrivate,boolean isDrm,boolean isSDDrm,boolean isCanshare,int position,int width,int height,MyViewHolder holder,String mAbsolutePath,boolean isSupportPrivacyMode){
        mItemMorePop = new CustomPopupWindowBasedAnchor(
                initMorePopWindow(isZip,isDir,isPrivate,isDrm,isSDDrm,isCanshare,position,mAbsolutePath,isSupportPrivacyMode),
                mItemMorePopWidth, mItemMorePopHeight,
                (Activity) mContext);

        if (mItemMorePop != null)
            mItemMorePop.showAtLocationBasedAnchor(holder.fileitemView.getMoreMenu(), xoff, yoff);
    }


    @Override
    public int getItemCount() {
        return mFileInfoList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        FileItemView fileitemView;

        public MyViewHolder(View view) {
            super(view);
            if (CommonUtils.isGridMode(mApplication)) {
                fileitemView = new FileItemView((TextView) view
                        .findViewById(R.id.edit_adapter_name),
                        (ImageView) view.findViewById(R.id.edit_adapter_img),
                        (ImageView) view.findViewById(R.id.edit_show_img),
                        (TextView) view.findViewById(R.id.edit_adapter_time),
                        (TextView) view.findViewById(R.id.edit_adapter_size),
                        (ImageView) view.findViewById(R.id.edit_moreMenu),
                        (LinearLayout) view.findViewById(R.id.file_grid_item_name_layout),
                        (LinearLayout) view.findViewById(R.id.file_grid_item_mes_layout),
                        (LinearLayout) view.findViewById(R.id.file_grid_item_botton_view),
                        (TextView) view.findViewById(R.id.grid_item_line),
                        (ImageView) view.findViewById(R.id.edit_private_img));
            } else {
                fileitemView = new FileItemView(
                        (TextView) view.findViewById(R.id.edit_adapter_name),
                        (TextView) view.findViewById(R.id.edit_adapter_time),
                        (TextView) view.findViewById(R.id.edit_adapter_size),
                        (ImageView) view.findViewById(R.id.edit_adapter_img),
                        (ImageView) view.findViewById(R.id.edit_moreMenu),
                        (TextView) view.findViewById(R.id.search_line),
                        (ImageView) view.findViewById(R.id.ic_selected),
                        (ImageView) view.findViewById(R.id.edit_private_img));
            }
        }
    }

    public void setTimeSizeText(TextView timeView, TextView sizeView, final FileInfo fileInfo) {
        long time = fileInfo.getFileLastModifiedTime();
        if (time == 0) {
            time = System.currentTimeMillis();
        }
        if (CommonUtils.isRecentCategoryMode()) {
            long mFileTimeInstanse = (mCurrentTime - time) / 1000l;
            String mTimeText = "";
            /**
             * 60 1 minutes ago is just
             * 3600 1 hour
             * 86400 24 hour
             */
            if (mFileTimeInstanse < 60) {
                mTimeText = mApplication.getString(R.string.just_now);
            } else if (mFileTimeInstanse >= 60 && mFileTimeInstanse < 3600) {
                if (mFileTimeInstanse < 120) {
                    mTimeText = mFileTimeInstanse / 60l + " " + mApplication.getString(R.string.minute_ago);
                } else {
                    mTimeText = mFileTimeInstanse / 60l + " " + mApplication.getString(R.string.minutes_ago);
                }
            } else if (mFileTimeInstanse >= 3600 && mFileTimeInstanse < 86400) {
                if (mFileTimeInstanse < 7200) {
                    mTimeText = mFileTimeInstanse / 3600l + " " + mApplication.getString(R.string.hour_ago);
                } else {
                    mTimeText = mFileTimeInstanse / 3600l + " " + mApplication.getString(R.string.hours_ago);
                }
            } else if (mFileTimeInstanse >= 86400) {
                mTimeText = mFileTimeInstanse / 86400l + " " + mApplication.getString(R.string.day_ago);
            }
            sizeView.setText(mTimeText + " • ");
        } else if (mDateFormat != null) {
            String mModifiedTime = mDateFormat.format(new Date(time)).toString();
            timeView.setText(mModifiedTime);
        }
        boolean isDir = fileInfo.isDirectory();
        if (CommonUtils.isListMode(mApplication)) {
            //showTextSizeByThread(sizeView, fileInfo);
            if (!isDir) {
                if (CommonUtils.isRecentCategoryMode()) {
                    timeView.setText(fileInfo.getFileSizeStr());
                } else {
                    sizeView.setText(fileInfo.getFileSizeStr() + " • ");
                }
            } else if (mApplication.mCurrentStatus != CommonIdentity.FILE_STATUS_GLOBALSEARCH) {
                sizeView.setText(mContext.getResources().getString(R.string.folder_items, fileInfo.getFolderCount()+"")+ " • ");
            }
        } else {
            if (!isDir) {
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
    }

    public void setSearchHighLight(TextView textView, String searchText) {
        if (!TextUtils.isEmpty(searchText)) {
            try {
                Spanned finalText = setHighLight(textView.getText().toString(), searchText);
                textView.setText(finalText);
            } catch (PatternSyntaxException e){
                e.printStackTrace();
            }
        }
    }

    public Spanned setHighLight(String allText, String searchText) {
        if (!TextUtils.isEmpty(searchText)) {
            try {
                StringBuilder search = new StringBuilder();
                int len = searchText.length();
                for (int i = 0; i < len; i++) {
                    char c = searchText.charAt(i);
                    if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}'
                            || c == '+' || c == '.' || c == '$' || c == '^') {
                        search.append('\\');
                        search.append(c);
                    } else {
                        search.append(c);
                    }
                }
                String searchReg = "(?i)" + search;
                Matcher matcher = Pattern.compile(searchReg).matcher(allText);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    String matchText = matcher.group(0);
                    StringBuilder text = new StringBuilder();
                    int len2 = matchText.length();
                    for (int i = 0; i < len2; i++) {
                        char c = matchText.charAt(i);
                        if (c == '$') {
                            text.append('\\');
                            text.append(c);
                        } else {
                            text.append(c);
                        }
                    }
                    matcher.appendReplacement(sb, "<font color='#FF5722'>" + text + "</font>");
                }
                matcher.appendTail(sb);
                Spanned finalText = Html.fromHtml(sb.toString());
                return finalText;
            } catch(PatternSyntaxException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void setNameText(TextView textView, FileInfo fileInfo) {

        if (fileInfo.isDirectory()
                && !MountManager.getInstance().isMountPoint(fileInfo.getFileAbsolutePath())) {
            textView.setText(fileInfo.getShowName());
        } else {
            textView.setText(fileInfo.getShowName());
        }
    }

    public List<FileInfo> getItemEditSelect() {
        return mItemEditFileList;
    }

    /**
     * This method sets all items' check boxes
     *
     * @param checked the checked state
     */
    public void setAllItemChecked(boolean checked) {
        mCheckedFileList.clear();
        if (checked) {
            mCheckedFileList.addAll(mFileInfoList);
        }
        notifyDataSetChanged();
    }

    public boolean isAllItemChecked() {
        return mFileInfoList.size() > 0 && mFileInfoList.size() == mCheckedFileList.size();
    }

    /**
     * This method gets the number of the checked items
     *
     * @return the number of the checked items
     */
    public int getCheckedItemsCount() {
        return mCheckedFileList.size();
    }

    /**
     * This method gets the list of the checked items
     *
     * @return the list of the checked items
     */
    public List<FileInfo> getCheckedFileInfoItemsList() {
        /**
         * We return the copy of mCheckedFileList
         *Because the mCheckedFileList will be cleared async by other software engineer before
         */
        List<FileInfo> list = new CopyOnWriteArrayList<FileInfo>();
        list.addAll(mCheckedFileList);
        return list;
    }


    /**
     * We return the copy of mCheckedFileList
     * Because the mCheckedFileList will be cleared async by other software engineer before
     */
    public List<FileInfo> getItemEditFileInfoList() {
        List<FileInfo> list = new CopyOnWriteArrayList<FileInfo>();
        if (mCheckedFileList.size() > 0) {
            list.addAll(mCheckedFileList);
        } else {
            list.addAll(mItemEditFileList);
        }
        return list;
    }

    /**
     * This method gets the first item in the list of the checked items
     *
     * @return the first item in the list of the checked items
     */
    public FileInfo getFirstCheckedFileInfoItem() {
        if (mCheckedFileList.size() > 0) {
            return mCheckedFileList.get(0);
        } else {
            return null;
        }
    }

    /**
     * This method change all checked items to be unchecked state
     */

    public void clearSelected(int mBaseType) {
        if (mBaseType == CommonIdentity.PASTE_CUT_TASK || mBaseType == CommonIdentity.PASTE_COPY_TASK) {
            mApplication.mFileInfoManager.clearPasteList();
        }
        mCheckedFileList.clear();
        mItemEditFileList.clear();
        notifyDataSetChanged();
    }

    /**
     * This method change all checked items to be unchecked state
     */
    public void clearChecked() {
        mCheckedFileList.clear();
        notifyDataSetChanged();
    }

    private void setIcon(final int pos, final FileItemView viewHolder, final FileInfo fileInfo, int mode) {
        int iconId = mIconManager.getIcon(fileInfo, mode);
        viewHolder.getIcon().setScaleType(ImageView.ScaleType.CENTER);
        viewHolder.getIcon().setImageResource(iconId);
        String filePath = fileInfo.getFileAbsolutePath();

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
        } else if (mFileInfoManager.getPasteType() == CommonIdentity.PASTE_MODE_CUT && mFileInfoManager.isPasteItem(fileInfo)) {
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

    public FileInfo getItem(int pos) {
        // Because mFileInfoList is used to be async by other software engineers
        // I have to make a judge due to the IndexOutOfBoundsException
        if (pos < mFileInfoList.size()) {
            return mFileInfoList.get(pos);
        } else {
            return null;
        }
    }

    public boolean isMode(int mode) {
        return SharedPreferenceUtils.getPrefsStatus(mApplication) == mode;
    }

    public int getMode() {
        return SharedPreferenceUtils.getPrefsStatus(mApplication);
    }

    public void clearList() {
        mFileInfoList.clear();
        mCheckedFileList.clear();
        notifyDataSetChanged();
    }

    public void refresh() {
        mFileInfoList.clear();
        mFileInfoList.addAll(getShowOrHideFileList(mFileInfoManager));
        if (FolderCountTask.getInstance(null, false) != null) {
            FolderCountTask.getInstance(null, false).cancel();
        }
        mFileShowListCount = mFileInfoList.size();
        if (mFileShowListCount > 0 && CommonUtils.isListMode(mApplication) && CommonUtils.isPathMode()) {
            loadCountText();
        }
        notifyDataSetChanged();
    }

    public void refreshSortAdapter() {
        mFileInfoList.clear();
        mFileInfoList.addAll(mFileInfoManager.getShowFileList());
        notifyDataSetChanged();
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

    /**
     * This method sets the item's check boxes
     *
     * @param position      the position of the item
     * @param checked the checked state
     */
    public void setChecked(int position, boolean checked) {
        if (position >= mFileInfoList.size()) {
            return;
        }
        FileInfo checkInfo = mFileInfoList.get(position);
        if (checked) {
            if (!mCheckedFileList.contains(checkInfo)) {
                mCheckedFileList.add(checkInfo);
            }
        } else {
            mCheckedFileList.remove(checkInfo);
        }
        notifyDataSetChanged();
    }

    /**
     * This method sets the item's check boxes
     *
     * @param position      the id of the item
     */
    public void setSelect(int position) {
        FileInfo checkInfo = mFileInfoList.get(position);
        if (!mCheckedFileList.contains(checkInfo)) {
            mCheckedFileList.add(checkInfo);
            selectedPosition = position;
        } else {
            mCheckedFileList.remove(checkInfo);
        }
        notifyDataSetChanged();
    }

    public void setItemEditSelect(int position) {
        try {
            if (mFileInfoList.size() <= position || position < 0) {
                return;
            }

            FileInfo itemEditInfo = mFileInfoList.get(position);
            if (mItemEditFileList.size() != 0) {
                mItemEditFileList.clear();
            }
            mItemEditFileList.add(itemEditInfo);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isHasDrm(int position) {
        try {
            if (mFileInfoList.size() <= position || position < 0) {
                return false;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);
            return itemEditInfo.isDrmFile() || itemEditInfo.isDrm();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isHasSDDrm(int position) {
        try {
            if (mFileInfoList.size() <= position || position < 0) {
                return false;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);
            return DrmManager.getInstance(mContext).isDrmSDFile(itemEditInfo.getFileAbsolutePath());
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean CanCompress(int pos) {
        if (mFileInfoList.size() <= pos || pos < 0) {
            return false;
        }
        FileInfo info = mFileInfoList.get(pos);
        if (info != null && info.getMime() != null && info.getMime().equals("application/zip")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCanShare(int position) {
        try {
            if (mFileInfoList.size() <= position || position < 0) {
                return false;
            }
            FileInfo itemEditInfo = mFileInfoList.get(position);
            if (itemEditInfo.isDrmFile() || itemEditInfo.isDrm()) {
                return false;
            } else {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public View initMorePopWindow(boolean isZip, boolean isDirectory, boolean isPrivate,boolean isDrm,boolean isSDDrm,boolean isCanshare,int position,String mAbsolutePath,boolean isSupportPrivacyMode) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customMoreView = inflater.inflate(R.layout.item_more_menu, null);

        RelativeLayout mRelCut = (RelativeLayout) customMoreView.findViewById(R.id.cut_item);
        RelativeLayout mRelCopy = (RelativeLayout) customMoreView.findViewById(R.id.copy_item);
        RelativeLayout mRelPaste = (RelativeLayout) customMoreView.findViewById(R.id.paste_item);
        RelativeLayout mRelDelete = (RelativeLayout) customMoreView.findViewById(R.id.delete_item);
        RelativeLayout mRelDetail = (RelativeLayout) customMoreView.findViewById(R.id.detail_item);
        RelativeLayout mRelRename = (RelativeLayout) customMoreView.findViewById(R.id.rename_item);
        RelativeLayout mRelExtract = (RelativeLayout) customMoreView.findViewById(R.id.extract_item);
        RelativeLayout mRelCompress = (RelativeLayout) customMoreView.findViewById(R.id.compress_item);
        RelativeLayout mRelShare = (RelativeLayout) customMoreView.findViewById(R.id.share_item);
        RelativeLayout mRelShortcut = (RelativeLayout) customMoreView.findViewById(R.id.shortcut_item);
        RelativeLayout mRelMoveToSafe = (RelativeLayout) customMoreView.findViewById(R.id.move_to_safe_item);
        RelativeLayout mRelSetPublic = (RelativeLayout) customMoreView.findViewById(R.id.set_public_safe_item);
        mRelExtract.setOnClickListener((View.OnClickListener) mContext);
        mRelCompress.setOnClickListener((View.OnClickListener) mContext);
        mRelCut.setOnClickListener((View.OnClickListener) mContext);
        mRelCopy.setOnClickListener((View.OnClickListener) mContext);
        mRelPaste.setOnClickListener((View.OnClickListener) mContext);
        mRelDelete.setOnClickListener((View.OnClickListener) mContext);
        mRelDetail.setOnClickListener((View.OnClickListener) mContext);
        mRelRename.setOnClickListener((View.OnClickListener) mContext);
        mRelShare.setOnClickListener((View.OnClickListener) mContext);
        mRelShortcut.setOnClickListener((View.OnClickListener) mContext);
        mRelSetPublic.setOnClickListener((View.OnClickListener) mContext);
        if (CommonUtils.isInPrivacyMode(mContext)) {
            mRelMoveToSafe.setVisibility(View.VISIBLE);
            mRelMoveToSafe.setOnClickListener((View.OnClickListener) mContext);
        } else {
            mRelMoveToSafe.setVisibility(View.GONE);
        }
        if (isInPrivacyMode && isSafeboxLocation) {
            mRelMoveToSafe.setVisibility(View.GONE);
            mRelCompress.setVisibility(View.GONE);
            mRelCopy.setVisibility(View.GONE);
            mRelCut.setVisibility(View.GONE);
            mRelExtract.setVisibility(View.GONE);
            mRelPaste.setVisibility(View.GONE);
            mRelShortcut.setVisibility(View.GONE);
            return customMoreView;
        }
        mRelSetPublic.setVisibility(View.GONE);
        mRelPaste.setVisibility(View.GONE);
        mRelCut.setVisibility(View.VISIBLE);
        mRelCopy.setVisibility(View.VISIBLE);
        mRelDelete.setVisibility(View.VISIBLE);
        mRelDetail.setVisibility(View.VISIBLE);
        mRelRename.setVisibility(View.VISIBLE);
        mRelCompress.setVisibility(View.GONE);
        mRelExtract.setVisibility(View.GONE);
        mRelMoveToSafe.setVisibility(View.GONE);
        mRelSetPublic.setVisibility(View.GONE);
        if (CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE) {
            mRelCompress.setVisibility(View.GONE);
            mRelExtract.setVisibility(View.GONE);
        } else if (isZip) {
            mRelCompress.setVisibility(View.GONE);
            mRelExtract.setVisibility(View.VISIBLE);
        } else {
            mRelCompress.setVisibility(View.VISIBLE);
            mRelExtract.setVisibility(View.GONE);
        }
        if (isDirectory) {
            mRelShare.setVisibility(View.GONE);
            mRelMoveToSafe.setVisibility(View.GONE);
            mRelShortcut.setVisibility(View.VISIBLE);
            mRelSetPublic.setVisibility(View.GONE);
        } else {
            if (isDrm && isSDDrm && !isCanshare) {
                mRelShare.setVisibility(View.VISIBLE);
                mRelCopy.setVisibility(View.GONE);
                mRelCompress.setVisibility(View.GONE);
                mRelMoveToSafe.setVisibility(View.GONE);
                mRelSetPublic.setVisibility(View.GONE);
            } else if (isDrm && !isCanshare) {
                mRelShare.setVisibility(View.GONE);
                mRelCopy.setVisibility(View.GONE);
                mRelCompress.setVisibility(View.GONE);
                mRelMoveToSafe.setVisibility(View.GONE);
                mRelSetPublic.setVisibility(View.GONE);
            } else if (isDrm && isCanshare) {
                mRelCopy.setVisibility(View.GONE);
                mRelShare.setVisibility(View.VISIBLE);

                if (isSupportPrivacyMode && !CommonUtils.isExternalStorage(mAbsolutePath,mMountManager,mApplication.isBuiltInStorage)) {
                    if(!isPrivate) {
                        mRelMoveToSafe.setVisibility(View.VISIBLE);
                        mRelSetPublic.setVisibility(View.GONE);
                    } else {
                        mRelMoveToSafe.setVisibility(View.GONE);
                        mRelSetPublic.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (isSupportPrivacyMode && !CommonUtils.isExternalStorage(mAbsolutePath,mMountManager,mApplication.isBuiltInStorage)) {
                    if(!isPrivate) {
                        mRelMoveToSafe.setVisibility(View.VISIBLE);
                        mRelSetPublic.setVisibility(View.GONE);
                    } else {
                        mRelMoveToSafe.setVisibility(View.GONE);
                        mRelSetPublic.setVisibility(View.VISIBLE);
                    }
                }
                mRelShare.setVisibility(View.VISIBLE);
            }
            mRelShortcut.setVisibility(View.GONE);
        }
        return customMoreView;
    }

    /**
     * This method changes the mode of adapter between CommonIdentity.MODE_NORMAL, MODE_EDIT,
     * and MODE_SEARCH
     *
     * @param mode the mode which will be changed to be.
     */
    public boolean changeMode(int mode) {
        if (mApplication.mCurrentStatus != mode) {
            switch (mode) {
                case CommonIdentity.FILE_STATUS_NORMAL:
                    clearChecked();
                    break;
                default:
                    break;
            }
            mApplication.mCurrentStatus = mode;
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    /**
     * This method changes the mode of MODE_SEARCH to CommonIdentity.MODE_NORMAL
     */
    public void changeModeFromSearchToNormal() {
        if (isMode(CommonIdentity.FILE_STATUS_SEARCH) || isMode(CommonIdentity.FILE_STATUS_GLOBALSEARCH)) {
            if (!changeMode(CommonIdentity.FILE_STATUS_NORMAL)) {
                notifyDataSetChanged();
            }
        }
    }

    private void setIconGird(final int pos, final FileItemView viewHolder, final FileInfo fileInfo, int mode) {
        int iconId = mIconManager.getIcon(fileInfo, mode);
        String filePath = fileInfo.getFileAbsolutePath();
        Drawable drawable = mIconManager.getImageCacheDrawable(filePath);
        if (drawable != null && viewHolder != null) {
            if (fileInfo.getMime() != null && fileInfo.getMime().startsWith("application/vnd.android.package-archive")) {
                viewHolder.getIcon().setScaleType(ImageView.ScaleType.CENTER);
                viewHolder.getIcon().setImageDrawable(drawable);
                viewHolder.getIcon().setBackground(null);
                viewHolder.getMesbackLayout().setBackground(null);
                viewHolder.getLineview().setVisibility(View.VISIBLE);
                viewHolder.getName().setTextColor(mContext.getResources().getColor(R.color.list_title_text_color));
                viewHolder.getSize().setTextColor(mContext.getResources().getColor(R.color.grid_time_text_color));
                viewHolder.getGridMesLayout().setBackground(mContext.getResources().getDrawable(R.drawable.list_corners_bg));
                viewHolder.getMoreMenu().setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file_overflow, null));
            } else if (viewHolder != null) {
                viewHolder.getGridMesLayout().setBackgroundResource(iconId);
                viewHolder.getIcon().setImageDrawable(null);
                viewHolder.getIcon().setBackground(null);
                viewHolder.getLineview().setVisibility(View.GONE);
                viewHolder.getGridMesLayout().setBackground(drawable);
                viewHolder.getMesbackLayout().setBackgroundColor(mContext.getResources().getColor(R.color.main_item_mes_color));
                viewHolder.getName().setTextColor(mContext.getResources().getColor(R.color.main_bac_color));
                viewHolder.getSize().setTextColor(mContext.getResources().getColor(R.color.main_bac_color));
                viewHolder.getMoreMenu().setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_menu));
                viewHolder.getSize().setAlpha(0.7f);
            }
        } else if (viewHolder != null) {
            loadGridImage(fileInfo);
            viewHolder.getMesbackLayout().setBackgroundColor(mContext.getResources().getColor(R.color.transparent_background));
            viewHolder.getIcon().setScaleType(ImageView.ScaleType.CENTER);
            viewHolder.getIcon().setImageResource(iconId);
            viewHolder.getLineview().setVisibility(View.VISIBLE);
            viewHolder.getMesbackLayout().setBackground(null);
            viewHolder.getName().setTextColor(mContext.getResources().getColor(R.color.list_title_text_color));
            viewHolder.getSize().setTextColor(mContext.getResources().getColor(R.color.grid_time_text_color));
            viewHolder.getGridMesLayout().setBackground(mContext.getResources().getDrawable(R.drawable.list_corners_bg));
            viewHolder.getMoreMenu().setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file_overflow));
        }
        if (viewHolder != null && mCheckedFileList.contains(fileInfo) == true) {
            viewHolder.getBottomLayout().setVisibility(View.VISIBLE);
            viewHolder.getGridMesLayout().setAlpha(0.5f);
        } else {
            if (viewHolder != null && fileInfo.isHideFile()) {
                viewHolder.getBottomLayout().setVisibility(View.GONE);
                viewHolder.getGridMesLayout().setAlpha(CommonIdentity.HIDE_ICON_ALPHA);
            } else if (viewHolder != null && mFileInfoManager.getPasteType() == CommonIdentity.PASTE_MODE_CUT && mFileInfoManager.isPasteItem(fileInfo)) {
                viewHolder.getBottomLayout().setVisibility(View.GONE);
                viewHolder.getGridMesLayout().setAlpha(CommonIdentity.CUT_ICON_ALPHA);
            } else if (viewHolder != null) {
                viewHolder.getGridMesLayout().setAlpha(1f);
            }
        }
    }

    protected void loadGridImage(final FileInfo fileInfo) {
        final String filePath = fileInfo.getFileAbsolutePath();

        final Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                ImageView iconByTag;
                LinearLayout backTag;
                LinearLayout mesbackTag;
                TextView nameTextView;
                TextView sizeColor;
                TextView lineView;
                ImageView moreMenu;
                nameTextView = (TextView) mListView.findViewWithTag("mName" + filePath);
                iconByTag = (ImageView) mListView.findViewWithTag("icon"
                        + filePath);
                backTag = (LinearLayout) mListView.findViewWithTag("gridMes" + filePath);
                mesbackTag = (LinearLayout) mListView.findViewWithTag("mesbac" + filePath);
                sizeColor = (TextView) mListView.findViewWithTag("mSize" + filePath);
                lineView = (TextView) mListView.findViewWithTag("mLine" + filePath);
                moreMenu = (ImageView) mListView.findViewWithTag("moreMenu" + filePath);
                if (backTag != null) {
                    if (fileInfo.getMime() != null && fileInfo.getMime().startsWith("application/vnd.android.package-archive")) {
                        iconByTag.setScaleType(ImageView.ScaleType.CENTER);
                        iconByTag.setImageDrawable((Drawable) msg.obj);
                        iconByTag.setBackground(null);
                        lineView.setVisibility(View.VISIBLE);
                        mesbackTag.setBackground(null);
                        nameTextView.setTextColor(mContext.getResources().getColor(R.color.list_title_text_color));
                        sizeColor.setTextColor(mContext.getResources().getColor(R.color.grid_time_text_color));
                        moreMenu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file_overflow, null));
                    } else {
                        iconByTag.setImageDrawable(null);
                        iconByTag.setBackground(null);
                        lineView.setVisibility(View.GONE);
                        backTag.setBackground((Drawable) msg.obj);
                        mesbackTag.setBackgroundColor(mContext.getResources().getColor(R.color.main_item_mes_color));
                        nameTextView.setTextColor(mContext.getResources().getColor(R.color.main_bac_color));
                        sizeColor.setTextColor(mContext.getResources().getColor(R.color.main_bac_color));
                        moreMenu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_menu, null));
                        sizeColor.setAlpha(0.7f);
                    }
                }
            }
        };

        mIconManager.loadImage(mContext, fileInfo,
                new IconManager.IconCallback() {
                    public void iconLoaded(Drawable iconDrawable) {
                        if (iconDrawable != null) {
                            Message message = mHandler.obtainMessage(0, 1, 1,
                                    iconDrawable);
                            mHandler.sendMessage(message);
                        }
                    }
                });
    }

    private List<FileInfo> getShowOrHideFileList(FileInfoManager mFileManager) {
        if (mFileManager == null) {
            return new ArrayList<FileInfo>();
        }
        List<FileInfo> mSourceFile = mFileManager.getShowFileList();
        List<FileInfo> mSourceTempFile = new ArrayList<FileInfo>();
        if (mSourceFile == null) {
            return new ArrayList<FileInfo>();
        }
        // Classification or directory not added cache
        if (mApplication != null && !CommonUtils.isPrivateLocation(mApplication) && ((CategoryManager.mCurrentMode == CommonIdentity.CATEGORY_MODE &&
                CategoryManager.mCurrentCagegory >= 0 && CategoryManager.mCurrentCagegory < 9 &&
                !mApplication.mCache.hasCachedPath(String.valueOf(CategoryManager.mCurrentCagegory)) ||
                (CategoryManager.mCurrentMode == CommonIdentity.PATH_MODE && mApplication.mCurrentPath != null && !mApplication.mCache.hasCachedPath(mApplication.mCurrentPath))))) {
            Log.d(TAG, "removeAndShowHideFile ==》 current key no exist cache==>" + mApplication.mCurrentPath + " ==> category key ==>" + CategoryManager.mCurrentCagegory);
            mApplication.mCache.setAllFileList(mSourceFile);
        }
        if (mApplication.isShowHidden) {
            return mSourceFile;
        } else {
            for (int i = 0; i < mSourceFile.size(); i++) {
                FileInfo info = mSourceFile.get(i);
                if (info != null) {
                    boolean hide = info.isHideFile();
                    if (!hide) {
                        mSourceTempFile.add(info);
                    }
                }
            }
            return mSourceTempFile;
        }
    }

    private void loadCountText() {
        final Handler mFolderHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                try {
                    String count = (String) msg.obj;
                    int what = msg.what;
                    if (what >= mFileShowListCount || mFileShowListCount < 1) {
                        return;
                    }
                    // get item count
                    int itemCount;
                    if (!TextUtils.isEmpty(count)) {
                        itemCount = Integer.parseInt(count);
                    } else {
                        itemCount = 0;
                    }
                    mFileInfoList.get(what).setFolderCount(itemCount);
                    notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred when loadCountText():" + e);
                }
            }
        };

        FolderCountManager.getInstance().loadFolderCountText(new FolderCountManager.FolderCountTextCallback() {
            @Override
            public void folderCountTextCallback(TaskInfo mTaskInfo) {
                Message message = mFolderHandler.obtainMessage(mTaskInfo.getCategoryIndex(), 1, 1, mTaskInfo.getSearchContent());
                mFolderHandler.sendMessage(message);
            }
        }, mFileInfoList);
    }


    public void setChecked(List<FileInfo> mCheckedList, boolean isClickItem) {
        if (isClickItem && mCheckedList.size() == 1) {
            if (mItemEditFileList.size() != 0) {
                mItemEditFileList.clear();
            }
            mItemEditFileList.addAll(mCheckedList);
        } else {
            mCheckedFileList.clear();
            mCheckedFileList.addAll(mCheckedList);
        }
    }
}
