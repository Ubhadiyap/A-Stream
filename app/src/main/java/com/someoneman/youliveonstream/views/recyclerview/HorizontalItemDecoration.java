package com.someoneman.youliveonstream.views.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.someoneman.youliveonstream.R;

/**
 * Created by ultra on 23.06.2016.
 */
public class HorizontalItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDrawable;

    public HorizontalItemDecoration(Context context) {
        mDrawable = context.getResources().getDrawable(R.drawable.horizontal_item_decoration);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDrawable.getIntrinsicHeight();

            mDrawable.setBounds(left, top, right, bottom);
            mDrawable.draw(c);
        }
    }
}
