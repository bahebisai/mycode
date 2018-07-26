package com.jrdcom.filemanager.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.manager.SafeManager;
import com.jrdcom.filemanager.utils.CommonUtils;

/**
 * Created by user on 16-10-12.
 */
public class PrivateCategoryAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private TextView mTypeTextview;


    private String[] mTypeArray = new String[5];

    public PrivateCategoryAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        initData();
    }

    private void initData() {

        mTypeArray[0] = mContext.getString(R.string.main_installers);
        mTypeArray[1] = mContext.getString(R.string.main_document);
        mTypeArray[2] = mContext.getString(R.string.category_music);
        mTypeArray[3] = mContext.getString(R.string.category_vedios);
        mTypeArray[4] = mContext.getString(R.string.category_archives);

    }


    @Override
    public int getCount() {
        return mTypeArray.length;
    }

    @Override
    public Object getItem(int position) {
        return mTypeArray[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = mInflater.inflate(R.layout.private_type_item, null);
        mTypeTextview = (TextView) view.findViewById(R.id.private_item_name);
        mTypeTextview.setText(mTypeArray[position]);
        return view;
    }
}
