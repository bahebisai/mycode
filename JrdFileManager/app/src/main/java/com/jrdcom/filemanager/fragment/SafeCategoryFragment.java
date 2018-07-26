/* Copyright (C) 2016 Tcl Corporation Limited */
package com.jrdcom.filemanager.fragment;

import android.app.Activity;
import android.content.Context;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.ISafeCategoryListener;
import com.jrdcom.filemanager.R;

import com.jrdcom.filemanager.adapter.SafeCategoryAdapter;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.TaskInfo;

import java.util.ArrayList;

/**
 * Created by user on 16-3-10.
 */
public class SafeCategoryFragment extends FileBrowserFragment implements AdapterView.OnItemClickListener,
        ISafeCategoryListener,View.OnClickListener {

    protected FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    private CategoryFragmentListener mCategoryFragmentListener;
    private String mMode = CommonIdentity.CATEGORY_TAG;
    private Resources mResources;
    private Button mAddFileBtn;

    private Activity mActivity;


    public void setMode(String mMode) {
        this.mMode = mMode;
    }

    private Context mContext;
    private RelativeLayout mNoPrivateFileLay;
    private TextView mNoPrivateContent;
    private ScrollView mScrollView;
    private ArrayList<PrivateItem> mList;

    private ListView mListView;
    private SafeCategoryAdapter mAdapter;

    @Override
    public void refreshSafeCategory() {
        refreshCategoryAdapter();
    }

    @Override
    public void clickAddFileBtn(boolean isBack) {
        if(isBack) {
            mListView.setVisibility(View.GONE);
            mNoPrivateFileLay.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
        } else {
            mNoPrivateFileLay.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mAdapter.setList(mList);
            mListView.setAdapter(mAdapter);
            if(mCategoryFragmentListener != null){
                mCategoryFragmentListener.hideMoreMenu();
            }
            refreshAdapter();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_safe_category, container, false);
    }

    @Override
    public void onClick(View view) {
        clickAddFileBtn(false);
    }

    public interface CategoryFragmentListener {
        public void switchContentByViewMode(boolean isCategory);

        public void updateCategoryNormalBarView();

        public void hideMoreMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            if (activity instanceof CategoryFragmentListener) {
                mCategoryFragmentListener = (CategoryFragmentListener) activity;
            }
            mActivity = getActivity();
        } catch (Exception e) {
            throw new ClassCastException(activity.toString()
                    + "must implement CategoryFragmentListener");
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication.mCurrentLocation = CommonIdentity.FILE_SAFEBOX_LOCATION;
        mContext = this.getActivity().getApplicationContext();
        setHasOptionsMenu(true);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.select_type_list);
        mAddFileBtn = (Button) view.findViewById(R.id.safe_add_file_btn);
        mScrollView =(ScrollView) view.findViewById(R.id.no_private_scroll);
        mListView.setOnItemClickListener(this);
        mAddFileBtn.setOnClickListener(this);
        mAdapter = new SafeCategoryAdapter(mActivity);
        mNoPrivateFileLay=(RelativeLayout) view.findViewById(R.id.no_private_lay);
        mNoPrivateContent = (TextView) view.findViewById(R.id.no_private_content);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mListView !=null && mListView.getVisibility() == View.VISIBLE) {
            refreshAdapter();
        }
    }

    private void refreshAdapter() {
        if (mAdapter != null) {
            loadCountText(null);
        }
    }


    private void refreshCategoryAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void loadCountText(final TextView textview) {
        final Handler mmHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                mList.get(msg.what).updateCount((String) msg.obj);
                mAdapter.notifyDataSetChanged();
            }
        };

        SafeManager.getInstance(mContext).loadCategoryCountText(new SafeManager.CountTextCallback() {
            @Override
            public void countTextCallback(TaskInfo mTaskInfo) {
                Message message = mmHandler.obtainMessage(mTaskInfo.getCategoryIndex(), 1, 1, mTaskInfo.getSearchContent());
                mmHandler.sendMessage(message);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mList = new ArrayList<PrivateItem>();
        mResources = mContext.getResources();
        mList.ensureCapacity(6);

        mList.add(new PrivateItem(mResources
                .getDrawable(R.drawable.ic_type_installer, null),
                mResources.getString(R.string.main_installers)));
        mList.add(new PrivateItem(mResources
                .getDrawable(R.drawable.ic_type_doc, null),
                mResources.getString(R.string.main_document)));
        mList.add(new PrivateItem(mResources
                .getDrawable(R.drawable.ic_type_audio, null),
                mResources.getString(R.string.category_music)));
        mList.add(new PrivateItem(mResources
                .getDrawable(R.drawable.ic_type_image, null),
                mResources.getString(R.string.category_pictures)));
        mList.add(new PrivateItem(mResources
                .getDrawable(R.drawable.ic_type_video, null),
                mResources.getString(R.string.category_vedios)));
        mList.add(new PrivateItem(mResources
                .getDrawable(R.drawable.ic_type_archive, null),
                mResources.getString(R.string.category_archives)));

        if(SafeManager.isFileTypeInterface || SafeManager.getPrivateFileCount(mActivity) >0 ||
                SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
            SafeManager.isFileTypeInterface = false;
            mListView.setVisibility(View.VISIBLE);
            mAdapter.setList(mList);
            mListView.setAdapter(mAdapter);
            mNoPrivateFileLay.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.GONE);
            mNoPrivateFileLay.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        SafeManager.mSafeCurrentOperration = CommonIdentity.FILE_MOVE_IN_MODE;

        switch (position) {
            case CommonIdentity.SAFE_CATEGORY_INSTALLERS:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_APKS;
                SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_INSTALLERS;
                break;

            case CommonIdentity.SAFE_CATEGORY_DOCS:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_DOCS;
                SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_DOCS;
                break;

            case CommonIdentity.SAFE_CATEGORY_MUISC:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_MUSIC;
                SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_MUISC;
                break;

            case CommonIdentity.SAFE_CATEGORY_PICTURES:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_PICTURES;
                SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_PICTURES;
                break;

            case CommonIdentity.SAFE_CATEGORY_VEDIO:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_VEDIOS;
                SafeManager.mCurrentSafeCategory = CommonIdentity.SAFE_CATEGORY_VEDIO;
                break;

            case CommonIdentity.SAFE_CATEGORY_ARCHIVES:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_ARCHIVES;
                SafeManager.mCurrentSafeCategory = CommonIdentity.CATEGORY_ARCHIVES;
                break;

        }
        mCategoryFragmentListener.updateCategoryNormalBarView();
        mCategoryFragmentListener.switchContentByViewMode(false);

    }

    @Override
    public boolean isShowFileTypeInterface() {
        if(mListView != null && mListView.getVisibility() == View.VISIBLE){
            return true;
        }
        return false;
    }

    public class PrivateItem {
        private Drawable icon;

        private String name;



        private String count = "";


        public PrivateItem(Drawable icon, String name) {
            this.icon = icon;
            this.name = name;
        }

        public Drawable getIcon() {
            return icon;
        }


        public String getName() {
            return name;
        }

        public String getCount() {
            return count;
        }

        public void updateCount(String c) {
            count = c;
        }

    }

}