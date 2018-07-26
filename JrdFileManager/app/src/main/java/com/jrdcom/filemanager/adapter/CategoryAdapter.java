package com.jrdcom.filemanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.fragment.CategoryFragment;
import com.jrdcom.filemanager.manager.CategoryManager;
import com.jrdcom.filemanager.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 16-8-10.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private Context mmContext;
    private List<CategoryFragment.CategoryItem> mList;
    private LayoutInflater mInflater;
    private OnItemClickLitener mOnItemClickLitener;

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener litener) {
        mOnItemClickLitener = litener;
    }

    public CategoryAdapter(Context context) {
        super();
        mmContext = context;
        mInflater = LayoutInflater.from(mmContext);
    }

    public void setList(ArrayList<CategoryFragment.CategoryItem> list) {
        this.mList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder = new MyViewHolder(mInflater.inflate(R.layout.main_item, parent, false));
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(final CategoryAdapter.MyViewHolder holder, int position) {
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });
        }
        CategoryFragment.CategoryItem item = mList.get(position);
        holder.iconView.setImageDrawable(item.getIcon());
        holder.mainLayout.setBackground(item.getMainDrawable());
        holder.nameView.setText(item.getName());
        holder.countView.setVisibility(View.VISIBLE);
        holder.countView.setTag(String.valueOf(position));
        if (item.getCount() != null && ((CommonUtils.isSupportPrivacyMode(mmContext) && CategoryManager.mCategoryItemMap.size() < 10)
                || CategoryManager.mCategoryItemMap.size() < 9)) {
            holder.countView.setText(item.getCount() + "");
        } else {
            if (position < CategoryManager.mCategoryItemMap.size()) {
                holder.countView.setText(CategoryManager.mCategoryItemMap.get(position));
            }
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView nameView;
        TextView countView;
        LinearLayout mainLayout;
        LinearLayout itemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView
                    .findViewById(R.id.category_img);
            nameView = (TextView) itemView
                    .findViewById(R.id.category_name);
            countView = (TextView) itemView
                    .findViewById(R.id.category_count);
            mainLayout = (LinearLayout) itemView
                    .findViewById(R.id.main_item_bac);
            itemLayout = (LinearLayout) itemView
                    .findViewById(R.id.main_item_mes_bac);
        }
    }
}
