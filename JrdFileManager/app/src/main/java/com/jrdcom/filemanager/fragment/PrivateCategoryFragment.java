package com.jrdcom.filemanager.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.ISafeCategoryListener;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.activity.FilePrivateModeActivity;
import com.jrdcom.filemanager.adapter.PrivateCategoryAdapter;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.utils.CommonIdentity;

/**
 * Created by user on 16-10-12.
 */
public class PrivateCategoryFragment extends FileBrowserFragment implements AdapterView.OnItemClickListener, ISafeCategoryListener {

    protected FileManagerApplication mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
    private PrivateFragmentListener mCategoryFragmentListener;
    private static final int DATA_UPDATED = 100;
    private String mMode = CommonIdentity.CATEGORY_TAG;

    private Activity mActivity;

    public void setMode(String mMode) {
        this.mMode = mMode;
    }

    private Context mContext;
    private RelativeLayout mNoPrivateFileLay;
    private TextView mNoPrivateContent;

    private ListView mListView;
    private PrivateCategoryAdapter mAdapter;
    private boolean mFirstSafebox = false;
    private Resources mResources;

    @Override
    public void refreshSafeCategory() {
        refreshCategoryAdapter();
    }

    @Override
    public void clickAddFileBtn(boolean isback) {
        if(isback){
            mNoPrivateFileLay.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        } else {
            mNoPrivateFileLay.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(mAdapter);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_private_category, container, false);
    }


    public interface PrivateFragmentListener {
        public void switchContentByViewMode(boolean isCategory);

        public void updateCategoryNormalBarView();

    }


    @Override
    public void onAttach(Activity activity) {
        try {
            if (activity instanceof PrivateFragmentListener) {
                mCategoryFragmentListener = (PrivateFragmentListener) activity;
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

        mApplication.mCurrentLocation = CommonIdentity.FILE_PRIVATE_LOCATION;
        mContext = this.getActivity().getApplicationContext();
        mResources = mContext.getResources();
        setHasOptionsMenu(true);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.select_type_list);
        mListView.setOnItemClickListener(this);
        mResources = mContext.getResources();
        mAdapter = new PrivateCategoryAdapter(mActivity);
        mNoPrivateFileLay=(RelativeLayout) view.findViewById(R.id.no_private_lay);
        mNoPrivateContent = (TextView) view.findViewById(R.id.no_private_content);
        if(SafeManager.isPrivateFileTypeInterface || SafeManager.getPrivateFileCount(mActivity) >0 ||
                SafeManager.mSafeCurrentOperration == CommonIdentity.FILE_MOVE_IN_MODE) {
            SafeManager.isPrivateFileTypeInterface= false;
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(mAdapter);
            mNoPrivateFileLay.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.GONE);
            mNoPrivateFileLay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void refreshCategoryAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

            case CommonIdentity.PRIVATE_CATEGORY_VEDIO:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_VEDIOS;
                SafeManager.mCurrentSafeCategory = CommonIdentity.PRIVATE_CATEGORY_VEDIO;
                break;

            case CommonIdentity.PRIVATE_CATEGORY_ARCHIVES:
                CategoryManager.mCurrentCagegory = CommonIdentity.CATEGORY_ARCHIVES;
                SafeManager.mCurrentSafeCategory = CommonIdentity.PRIVATE_CATEGORY_ARCHIVES;
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
}
