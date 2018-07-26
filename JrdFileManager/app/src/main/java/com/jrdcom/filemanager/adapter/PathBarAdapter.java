package com.jrdcom.filemanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.LogUtils;

public class PathBarAdapter extends RecyclerView.Adapter<PathBarAdapter.MyViewHolder> {
    private Context mmContext;
    private String[] paths;
    private LayoutInflater mInflater;
    private OnItemClickLitener mOnItemClickLitener;

    public interface OnItemClickLitener {
        void onItemClick(int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener litener) {
        mOnItemClickLitener = litener;
    }

    public PathBarAdapter(Context context, String[] dataList) {
        super();
        mmContext = context;
        mInflater = LayoutInflater.from(mmContext);
        this.paths = dataList;
    }

    public void setList(String[] paths) {
        this.paths = paths;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder = new MyViewHolder(mInflater.inflate(R.layout.horizontallist_item, parent, false));
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return paths.length;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(position);
                }
            });
        }
        holder.file_name.setText(paths[position].toUpperCase());
        if (position == 0) {
            // position is 0 means root path, use icon instead in main.xml
            holder.file_name.setVisibility(View.GONE);
            holder.path_icon.setVisibility(View.GONE);
        } else if (position != paths.length - 1) {
            // if it is not focus directory, make it not be highlight state
            holder.path_icon.setVisibility(View.VISIBLE);
            holder.file_name.setVisibility(View.VISIBLE);
            holder.file_name.setAlpha(0.5f);
        } else {
            // if it is focus directory, make it be highlight state
            holder.path_icon.setVisibility(View.VISIBLE);
            holder.file_name.setVisibility(View.VISIBLE);
            holder.file_name.setAlpha(1.0f);
        }
    }

    public void notifyAndScroll(RecyclerView listView){
        notifyDataSetChanged();
        if(listView!=null){
            try {
                listView.scrollToPosition(getItemCount() - 1); // MODIFIED by caiminjie, 2017-09-21,BUG-5346088
            } catch (Exception e){
                LogUtils.e("PathBarAdapter", "Exception occured when smoothScrollToPosition():", e);
            }
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView path_icon;
        TextView file_name;
        public MyViewHolder(View itemView) {
            super(itemView);
            file_name = (TextView) itemView.findViewById(R.id.horizontallist_item_path);
            path_icon = (ImageView) itemView.findViewById(R.id.horizontallist_item_icon);
        }
    }
}
