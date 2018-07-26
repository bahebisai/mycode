package com.jrdcom.filemanager.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;

import java.util.List;

public class DetailDialogFragment extends DialogFragment{

    private static Context mContext;
    private static String mTitle;
    private static List<String> mTitleAdapter;
    private static List<String> mValueAdapter;
    private static DetailDialogFragment mShowDetailDialogFragment = null;
    private static FragmentManager mFragmentManager;
    public Dialog mDialog;
    private ListView mDetailListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_details, null);
        mDetailListView = (ListView)view.findViewById(R.id.dialog_details_listview);
        SimPickerAdapter simAdapter = new SimPickerAdapter(getActivity(), mTitleAdapter, mValueAdapter);
        mDetailListView.setAdapter(simAdapter);
        builder.setView(view);
        //builder.setSingleChoiceItems(simAdapter, -1, null)
                builder.setTitle(mTitle)
                .setNegativeButton(
                        getResources().getString(R.string.dialog_close_btn), new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DetailDialogFragment df = (DetailDialogFragment) getFragmentManager().findFragmentByTag(CommonIdentity.DETAIL_DIALOG_TAG);
                                if(df != null){
                                    df.dismissAllowingStateLoss();
                                }
                                mShowDetailDialogFragment = null;
                            }
                        });
        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static DetailDialogFragment createDetailDialog(Context context, FragmentManager fragmentManager,String title,
                                                          List<String> titleAdapter, List<String> valueAdapter) {
        mContext = context;
        mFragmentManager = fragmentManager;
        mTitle = title;
        mTitleAdapter = titleAdapter;
        mValueAdapter = valueAdapter;
        mShowDetailDialogFragment = null;
        mShowDetailDialogFragment = new DetailDialogFragment();
        return mShowDetailDialogFragment;
    }

    private static class SimPickerAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<String> mTitleAdapter;
        private List<String> mNameAdapter;

        public SimPickerAdapter(Context context, List<String> titleAdapter, List<String> valueAdapter) {
            mInflater = LayoutInflater.from(context);
            mTitleAdapter = titleAdapter;
            mNameAdapter = valueAdapter;
        }

        public int getCount() {
            if(mTitleAdapter == null){
                return 0;
            }
            return mTitleAdapter.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            SingleHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.detail_dialog, null);

                holder = new SingleHolder();
                holder.detail_title = (TextView) convertView.findViewById(R.id.detail_title);
                holder.detail_value = (TextView) convertView.findViewById(R.id.detail_value);
                holder.divider = (TextView) convertView.findViewById(R.id.detail_divider);
                convertView.setTag(holder);
            } else {
                holder = (SingleHolder) convertView.getTag();
            }
            if(mTitleAdapter == null){
                return convertView;
            }
            if (position == mTitleAdapter.size()) {
                holder.divider.setVisibility(View.GONE);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }
            if (CommonUtils.getRobotoMedium() != null) {
                holder.detail_value.setTypeface(CommonUtils.getRobotoMedium());
            }
            holder.detail_title.setText(mTitleAdapter.get(position));
            holder.detail_value.setText(mNameAdapter.get(position));
            return convertView;
        }

    }

    private static class SingleHolder {
        TextView detail_title;
        TextView detail_value;
        TextView divider;
    }

    public void showDialog() {
        mShowDetailDialogFragment.show(mFragmentManager, CommonIdentity.DETAIL_DIALOG_TAG);
    }
}
