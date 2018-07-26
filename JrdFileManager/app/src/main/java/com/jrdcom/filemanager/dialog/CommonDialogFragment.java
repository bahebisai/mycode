package com.jrdcom.filemanager.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jrdcom.filemanager.FileManagerApplication;
import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;

/**
 * Created by user on 16-9-24.
 */
public class CommonDialogFragment extends DialogFragment {
    private TextView mShowMessage;
    public static CommonDialogFragment mShowMessageDialogFragment = null;
    private static String mMessage;
    private static FragmentManager mFragmentManager;
    private String mButtonStr;
    private AlertDialog mDialog;
    private static String dialogTag = CommonIdentity.DELETE_DIALOG_TAG;
    private FileManagerApplication mApplication;
    private TextView mKeepText;
    private TextView mRenameText;
    private static EditText mExtractEditText;
    private static EditText mCompressEditText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.delete_dialog, null);
        mApplication = (FileManagerApplication)FileManagerApplication.getInstance();
        mShowMessage =(TextView) view.findViewById(R.id.delete_dialog_message);
        if(!dialogTag.isEmpty() && dialogTag.equals(CommonIdentity.DELETE_DIALOG_TAG)){
            builder.setTitle(R.string.delete);
            mButtonStr = getString(R.string.delete);
        } else if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXIT_DIALOG_TAG) || dialogTag.equals(CommonIdentity.NO_AVAILABLE_STORAGE_DIALOG_TAG))){
            mButtonStr = getString(R.string.ok);

        } else if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXTRACT_DIALOG_TAG))){
            mButtonStr = getString(R.string.extract);
            builder.setTitle(R.string.extract);
        } else if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXTRACT_NAME_EXIST_DIALOG_TAG))){
            mKeepText = (TextView) view.findViewById(R.id.extract_dialog_keep);
            mRenameText = (TextView) view.findViewById(R.id.extract_dialog_rename);
            mKeepText.setVisibility(View.VISIBLE);
            mRenameText.setVisibility(View.VISIBLE);
            mKeepText.setOnClickListener((View.OnClickListener)getActivity());
            mRenameText.setOnClickListener((View.OnClickListener)getActivity());
            builder.setTitle(R.string.folder_already_exists);
        } else if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG) ||
                dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG))){
            mButtonStr = getString(R.string.ok);
            if(dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG)) {
                mCompressEditText = (EditText) view.findViewById(R.id.compress_edit_text);
                if(!TextUtils.isEmpty(mMessage)){
                    try {
                        /*
                        Since the length of the compression name is 250, the latter four characters will be removed
                        when the folder name exceeds this length.
                         */
                        if (mMessage.length() > 250) {
                            mMessage = mMessage.substring(0, 250);
                        }
                        mCompressEditText.setText(mMessage);
                        mCompressEditText.setSelection(mMessage.length());
                    } catch (Exception e){

                    }
                }
                mCompressEditText.setVisibility(View.VISIBLE);
            } else {
                mExtractEditText = (EditText) view.findViewById(R.id.extract_edit_text);
                mExtractEditText.setVisibility(View.VISIBLE);
            }
            mShowMessage.setVisibility(View.GONE);
        }  else {
            if(mApplication.mCurrentLocation==CommonIdentity.FILE_SAFEBOX_LOCATION ||
                    mApplication.mCurrentLocation == CommonIdentity.FILE_MANAGER_LOCATIONE){
                builder.setTitle(R.string.set_public);
                mButtonStr = getString(R.string.set_public);
            } else {
                builder.setTitle(R.string.remove_private_title);
                mButtonStr = getString(R.string.remove_private_title);
            }
        }
        mShowMessage.setText(mMessage);
        // Add action buttons
        builder.setView(view);
        if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXIT_DIALOG_TAG) ||
                dialogTag.equals(CommonIdentity.EXTRACT_NAME_EXIST_DIALOG_TAG))){
            builder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeDialogFragment(CommonIdentity.EXIT_DIALOG_TAG);
                }
            });
            builder.setNegativeButton(mButtonStr,((DialogInterface.OnClickListener) getActivity()));
        } else if(!dialogTag.isEmpty() && dialogTag.equals(CommonIdentity.NO_AVAILABLE_STORAGE_DIALOG_TAG)){
            builder.setPositiveButton(mButtonStr, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeDialogFragment(CommonIdentity.NO_AVAILABLE_STORAGE_DIALOG_TAG);
                }
            });
        } else {
            builder.setPositiveButton(mButtonStr, ((DialogInterface.OnClickListener) getActivity()));
            builder.setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(dialogTag != null) {
                        closeDialogFragment(dialogTag);
                    } else {
                        closeDialogFragment(CommonIdentity.DELETE_DIALOG_TAG);
                    }
                }
            });
        }

        builder.setCancelable(false);
        mDialog = builder.create();

        if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXIT_DIALOG_TAG) || dialogTag.equals(CommonIdentity.NO_AVAILABLE_STORAGE_DIALOG_TAG)
                || dialogTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG) || dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG))){
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        if(!dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG) || dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG))){
            CommonUtils.fieldDialog(mDialog);
            builder.setCancelable(true);
        }

        return mDialog;
    }

    public static CommonDialogFragment getInstance(FragmentManager fragmentManager, String message, String flag) {
        dialogTag = flag;
        mMessage = message;
        mFragmentManager = fragmentManager;
        mShowMessageDialogFragment = null;
        mShowMessageDialogFragment = new CommonDialogFragment();
        return mShowMessageDialogFragment;
    }

    public static CommonDialogFragment getInstance() {
        return mShowMessageDialogFragment;
    }

    public void showDialog() {
        mShowMessageDialogFragment.setCancelable(true);
        try {
//            FragmentTransaction ft = mFragmentManager.beginTransaction();
//            ft.add(mShowMessageDialogFragment, dialogTag != null ? dialogTag : CommonIdentity.DELETE_DIALOG_TAG);
//            ft.commitAllowingStateLoss();
            mShowMessageDialogFragment.show(mFragmentManager, dialogTag != null ? dialogTag : CommonIdentity.DELETE_DIALOG_TAG);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getDailogTag() {
        return dialogTag;
    }

    public static void clearDailogTag() {
        if(dialogTag != null && !dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG) ||
                dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG))) {
            dialogTag = null;
        }
    }

    public static String getFolderName() {
        if (mCompressEditText != null && !dialogTag.isEmpty() && dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG)) {
            return mCompressEditText.getText().toString();
        } else if (mExtractEditText != null && !dialogTag.isEmpty() && dialogTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG)){
            return mExtractEditText.getText().toString();
        }
        return null;
    }

    public void closeDialogFragment(String mTag){
        CommonDialogFragment df = (CommonDialogFragment) getFragmentManager().findFragmentByTag(mTag);
        if(df != null){
            df.dismissAllowingStateLoss();
        }
        clearDailogTag();
        mShowMessageDialogFragment = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null && dialogTag != null && !dialogTag.isEmpty() && (dialogTag.equals(CommonIdentity.EXTRACT_RENAME_DIALOG_TAG) ||
                dialogTag.equals(CommonIdentity.COMPRESS_RENAME_DIALOG_TAG))){
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
