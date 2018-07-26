package com.jrdcom.filemanager.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.PermissionUtil;

public class PermissionFragment extends Fragment {

    private Context mContext;

    private TextView mWelcomeTitle;
    private TextView mDenyTitle;
    private TextView mDenyContent;

    private ImageView indicatorImage;

    private View mBtnSettingExit;
    private View mSettingOptionBtn;
    private View permission_option_btn_container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        return getContentView(inflater, container, savedInstanceState);
    }

    protected View getContentView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permission, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWelcomeTitle = (TextView) view.findViewById(R.id.permission_welcome_title);
        mDenyTitle = (TextView) view.findViewById(R.id.permission_deny_title);
        indicatorImage = (ImageView) view.findViewById(R.id.permission_deny_img);
        mDenyContent = (TextView) view.findViewById(R.id.permission_deny_content);

        indicatorImage = (ImageView) view.findViewById(R.id.permission_deny_img);

        permission_option_btn_container = view.findViewById(R.id.permission_option_btn_container);
        mSettingOptionBtn = view.findViewById(R.id.permission_setting_layout);
        mBtnSettingExit = view.findViewById(R.id.permission_setting_exit);
        mBtnSettingExit.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mSettingOptionBtn.setOnClickListener((OnClickListener) getActivity());

        if (PermissionUtil.isSecondRequestPermission(mContext)) {
            updateView(1);
        } else {
            mWelcomeTitle.setVisibility(View.VISIBLE);
            mDenyTitle.setVisibility(View.GONE);
            mDenyContent.setVisibility(View.GONE);
            indicatorImage.setImageResource(R.drawable.permission_welcome);
            permission_option_btn_container.setVisibility(View.GONE);

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updateView(int type) {
        switch (type) {
            case 1:
                if (mWelcomeTitle != null) {
                    mWelcomeTitle.setVisibility(View.GONE);
                }
                if (mDenyTitle != null) {
                    mDenyTitle.setVisibility(View.VISIBLE);
                }
                if (mDenyContent != null) {
                    mDenyContent.setVisibility(View.VISIBLE);
                }
                if (indicatorImage != null) {
                    indicatorImage.setImageResource(R.drawable.permission_deny);
                }
                if (permission_option_btn_container != null) {
                    permission_option_btn_container.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

}
