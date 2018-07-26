package com.jrdcom.filemanager.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.adapter.FileShowAdapter;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.SharedPreferenceUtils;
import com.jrdcom.filemanager.view.FloatingActionButton;
import com.jrdcom.filemanager.view.FloatingActionsMenu;


public class ListsFragment extends FileBrowserFragment {

    private View rootView;
    private LinearLayoutManager lm = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(R.layout.fragment_list, container, false);
            initView(rootView);
        }
        return rootView;
//        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    private void initView(View view){
        mRecyclerView = null;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_view);
        mNoSearchView = (LinearLayout) view.findViewById(R.id.list_no_search_result);
        mNoFolderView = (LinearLayout) view.findViewById(R.id.list_no_folder);
        noSearchText = (TextView) view.findViewById(R.id.list_no_result_text);
        mNo_messageView = (TextView) view.findViewById(R.id.list_no_folder_text);
        mNo_ImageView = (ImageView) view.findViewById(R.id.list_no_folder_img);

        menuMultipleActions = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions);
        setPrivate_btn = (FloatingActionButton) view.findViewById(R.id.float_action_private);
        archives_btn = (FloatingActionButton) view.findViewById(R.id.float_action_archive);
        share_btn = (FloatingActionButton) view.findViewById(R.id.float_action_share);
        delete_btn = (FloatingActionButton) view.findViewById(R.id.float_action_delete);
        copy_btn = (FloatingActionButton) view.findViewById(R.id.float_action_copy);
        cut_btn = (FloatingActionButton) view.findViewById(R.id.float_action_cut);
        extract_btn = (FloatingActionButton) view.findViewById(R.id.float_action_extract);
        paste_btn = (FloatingActionButton) view.findViewById(R.id.float_action_paste);

        menuMultipleActionsLeft = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions_left);
        setPrivate_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_private_left);
        archives_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_archive_left);
        share_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_share_left);
        delete_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_delete_left);
        copy_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_copy_left);
        cut_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_cut_left);
        extract_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_extract_left);
        paste_btnLeft = (FloatingActionButton) view.findViewById(R.id.float_action_paste_left);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter == null) {
            mAdapter = new FileShowAdapter(getActivity(), mApplication.mFileInfoManager, mRecyclerView);
        }
        mApplication.mViewMode = SharedPreferenceUtils.getPrefsViewBy(getActivity());
        int column = CommonUtils.getGridColumn(mApplication);
        if (mApplication.mViewMode.equals(CommonIdentity.GRID_MODE)) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), column));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(mAdapter);
        }

        mAdapter.setOnItemClickLitener(this);
        setPrivate_btn.setOnClickListener((View.OnClickListener) getActivity());
        archives_btn.setOnClickListener((View.OnClickListener) getActivity());
        share_btn.setOnClickListener((View.OnClickListener) getActivity());
        delete_btn.setOnClickListener((View.OnClickListener) getActivity());
        copy_btn.setOnClickListener((View.OnClickListener) getActivity());
        cut_btn.setOnClickListener((View.OnClickListener) getActivity());
        extract_btn.setOnClickListener((View.OnClickListener) getActivity());
        paste_btn.setOnClickListener((View.OnClickListener) getActivity());

        setPrivate_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        archives_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        share_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        delete_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        copy_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        cut_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        extract_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        paste_btnLeft.setOnClickListener((View.OnClickListener) getActivity());
        lm =  (LinearLayoutManager) mRecyclerView.getLayoutManager();
    }

    @Override
    protected void restoreListPosition() {

    }

    @Override
    protected void switchToEditView(int position, int top,boolean flag) {
        super.switchToEditView(position, top,flag);
        switchToEditView();
    }

    @Override
    public void updateActionMode(int mode) {
        super.updateActionMode(mode);
        mApplication.mCurrentStatus = mode;
        SharedPreferenceUtils.changePrefsStatus(mApplication,mode);
    }

    @Override
    public void setViewPostion(final int position,boolean isBackPosition) {
        /*
           Grid mode supports folder positioning when clicked back, creating folders is not supported.
         */
        if(lm == null || (CommonUtils.isGridMode(mApplication) && !isBackPosition)){
            return;
        }
        try {
            int total = mAdapter.getItemCount();
            int firstItem = lm.findFirstVisibleItemPosition();
            int lastItem = lm.findLastVisibleItemPosition();
            if (position <= firstItem) {
                if (isBackPosition) {
                    mRecyclerView.scrollToPosition(position); // MODIFIED by caiminjie, 2017-09-21,BUG-5346088
                } else {
                    mRecyclerView.scrollToPosition(position);
                }
            } else if (position <= lastItem) {
                int top = mRecyclerView.getChildAt(position - firstItem).getTop();
                mRecyclerView.scrollBy(0, top);
            } else {
                if (position == total - 1) {
                    if (isBackPosition) {
                        /* MODIFIED-BEGIN by caiminjie, 2017-09-21,BUG-5346088*/
                        mRecyclerView.scrollToPosition(position);
                    } else {
                        mRecyclerView.scrollToPosition(position);
                    }
                } else {
                    if (isBackPosition) {
                        mRecyclerView.scrollToPosition(position + 1);
                        /* MODIFIED-END by caiminjie,BUG-5346088*/
                    } else {
                        mRecyclerView.scrollToPosition(position + 1);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
