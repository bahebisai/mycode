package com.jrdcom.filemanager.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.utils.TaskInfo;


public class ProgressPopupWindow {
    public static final String TAG = "ProgressPopupWindow";
    private View.OnClickListener mCancelListener;
    public static String mTitle;
    private static Context mContext;
    public static LinearLayout mMultiTaskLay;
    public static LinearLayout mSingleTaskLay;
    public static RelativeLayout mFirstTaskLayout;
    public static RelativeLayout mTotalProgressLayout;
    public static ProgressBar mFirstProgressbar;
    public static TextView mFirstTaskProgress;
    public static TextView mTotalProgress;
    private static TextView mSingleTextView;
    private static AlertDialog dialog;
    private static TaskInfo mListenerInfo;
    private static int mProgressIndex = -1;
    private static int mBaseTaskType = -1;
    private static FileManagerApplication mApplication;
    private static ImageButton mFirstCancel;

    public static AlertDialog newInstance(Context context, TaskInfo listenerInfo) {
        mContext = context;
        mListenerInfo = listenerInfo;
        mTitle = listenerInfo.getTitleStr();
        if (mTitle == null) {
           mTitle = mContext.getString(R.string.app_name_new); // MODIFIED by Chuanzhi.Shao, 2017-10-11,BUG-5395138
        }
        mBaseTaskType = mListenerInfo.getFileFilter();
        AlertDialog.Builder passwordBuilder = new AlertDialog.Builder(mContext);
        passwordBuilder.setView(onCreateView());
        if(CommonUtils.isShowHorizontalProgressBar(mBaseTaskType)) {
            CommonDialogFragment.clearDailogTag();
            passwordBuilder.setTitle(mTitle);
            passwordBuilder.setPositiveButton(R.string.hide_btn, (DialogInterface.OnClickListener) mContext);
            passwordBuilder.setNegativeButton(R.string.cancel, (DialogInterface.OnClickListener) mContext);
        }
        dialog = passwordBuilder.create();

        return dialog;
    }

    /**
     * This method sets cancel listener to cancel button
     *
     * @param listener clickListener, which will do proper things when touch
     *                 cancel button
     */
    public void setCancelListener(View.OnClickListener listener) {
        mCancelListener = listener;
    }

    public static View onCreateView() {
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        View mProgressView = mInflater.inflate(R.layout.progressdialog_layout, null);
        mApplication = (FileManagerApplication) FileManagerApplication.getInstance();
        mMultiTaskLay = (LinearLayout) mProgressView.findViewById(R.id.multi_task_lay);
        mSingleTaskLay = (LinearLayout) mProgressView.findViewById(R.id.single_task_lay);
        if (CommonUtils.isShowCircularProgressBar(mBaseTaskType)) {
            mSingleTextView = (TextView) mProgressView.findViewById(R.id.list_progress_tile);
            mMultiTaskLay.setVisibility(View.GONE);
            mSingleTaskLay.setVisibility(View.VISIBLE);

        } else {
            mMultiTaskLay.setVisibility(View.VISIBLE);
            mSingleTaskLay.setVisibility(View.GONE);
            mFirstTaskLayout = (RelativeLayout) mProgressView.findViewById(R.id.first_task_lay);
            mTotalProgressLayout = (RelativeLayout) mProgressView.findViewById(R.id.total_task_lay);
            mFirstProgressbar = (ProgressBar) mProgressView.findViewById(R.id.first_task_progressbar);
            mFirstTaskProgress = (TextView) mProgressView.findViewById(R.id.first_task_text);
            mTotalProgress = (TextView) mProgressView.findViewById(R.id.total_progress_content);
        }
        return mProgressView;
    }

    public static long getCreateTaskTime(){
        if(mListenerInfo == null) return -1l;
        return mListenerInfo.getCreateTaskTime();
    }

}
