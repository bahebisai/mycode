package com.jrdcom.filemanager.listener;

import android.view.View;

/**
 * Created by slzhou on 16-12-7.
 */

public interface CategoryFragmentListener {
    void switchContentByViewMode(boolean isCategory);

    void updateCategoryNormalBarView();

    void notifyCategoryDone(boolean isDone);

    void refreshStorageInfoUiForLand(View progressBarAdapterView);
}
