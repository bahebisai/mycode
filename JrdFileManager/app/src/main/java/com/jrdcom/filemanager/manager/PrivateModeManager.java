package com.jrdcom.filemanager.manager;

import android.content.Context;

import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.FileInfo;

import java.util.ArrayList;
import java.util.List;

import tct.util.privacymode.TctPrivacyModeHelper;

/**
 * Created by user on 16-10-13.
 */
public class PrivateModeManager {

    private TctPrivacyModeHelper mTctPrivacyModeHelper;
    private Context mContext;

    public PrivateModeManager(Context context){
        mContext = context;
    }
    public TctPrivacyModeHelper getInstance(){
        if(mTctPrivacyModeHelper == null) {
            mTctPrivacyModeHelper = TctPrivacyModeHelper.createHelper(mContext);
        }
        return mTctPrivacyModeHelper;
    }

    public void addPrivateModeFile(List<FileInfo> mSelectList){
        ArrayList<String> mSelectPathList = new ArrayList<String>();
        if(mSelectList!= null && mSelectList.size() >0){
          for(int i= 0 ;i<mSelectList.size();i++){
              mSelectPathList.add(mSelectList.get(i).getFileAbsolutePath());
          }
            mTctPrivacyModeHelper.setFilePrivateFlag(CommonIdentity.FILES_PACKAGE_NAME,mSelectPathList,true);
        }

    }

    public void addPrivateModeFile(ArrayList<String> mSelectList){
        if(mSelectList!= null && mSelectList.size() >0){
            mTctPrivacyModeHelper.setFilePrivateFlag(CommonIdentity.FILES_PACKAGE_NAME,mSelectList,true);
        }
    }

    public void removePrivateModeFile(List<FileInfo> mSelectList){
        ArrayList<String> mSelectPathList = new ArrayList<String>();
        if(mSelectList!= null && mSelectList.size() >0){
            for(int i= 0 ;i<mSelectList.size();i++){
                mSelectPathList.add(mSelectList.get(i).getFileAbsolutePath());
            }
            mTctPrivacyModeHelper.setFilePrivateFlag(CommonIdentity.FILES_PACKAGE_NAME,mSelectPathList,false);
        }

    }

    public void removePrivateModeFile(ArrayList<String> mSelectList){
        if(mSelectList!= null && mSelectList.size() >0){
            mTctPrivacyModeHelper.setFilePrivateFlag(CommonIdentity.FILES_PACKAGE_NAME,mSelectList,false);
        }

    }

    public static boolean isPrivateFile(TctPrivacyModeHelper mPrivacyModeHelper,String mAbsolutePath){
        if(mPrivacyModeHelper != null && mAbsolutePath != null){
            return mPrivacyModeHelper.isPrivateFile(CommonIdentity.FILES_PACKAGE_NAME, mAbsolutePath);
        }
        return false;
    }
}
