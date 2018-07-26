package com.jrdcom.filemanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.fragment.SafeCategoryFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 16-10-19.
 */
public class SafeCategoryAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<SafeCategoryFragment.PrivateItem> mList;
    public SafeCategoryAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        initData();
    }

    public void setList(ArrayList<SafeCategoryFragment.PrivateItem> list) {
        this.mList = list;
    }

    private void initData() {

    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final MyViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new MyViewHolder();
            convertView = mInflater.inflate(R.layout.safe_list_item, null);
            viewHolder.nameView = (TextView) convertView.findViewById(R.id.safe_list_name);
            viewHolder.iconView =(ImageView) convertView.findViewById(R.id.safe_list_img);
            viewHolder.countView = (TextView) convertView.findViewById(R.id.safe_list_size);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }
        SafeCategoryFragment.PrivateItem item = mList.get(position);
        viewHolder.nameView.setText(item.getName());
        viewHolder.iconView.setImageDrawable(item.getIcon());

        viewHolder.countView.setText(item.getCount() + " "+mContext.getString(R.string.items));
        return convertView;
    }


    public class MyViewHolder {
        ImageView iconView;
        TextView nameView;
        TextView countView;
    }
}

