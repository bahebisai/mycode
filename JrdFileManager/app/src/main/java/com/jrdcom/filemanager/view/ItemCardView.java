package com.jrdcom.filemanager.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;


/**
 * Created by user on 16-3-9.
 */
public class ItemCardView extends CardView {

    public ItemCardView(Context context) {
        super(context);
    }

    public ItemCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
