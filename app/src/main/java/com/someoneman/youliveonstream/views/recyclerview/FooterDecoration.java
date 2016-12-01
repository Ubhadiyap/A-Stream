package com.someoneman.youliveonstream.views.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.someoneman.youliveonstream.utils.Log;

/**
 * Created by ultra on 26.06.2016.
 */
public class FooterDecoration extends RecyclerView.ItemDecoration {

    private View mLayout;

    public FooterDecoration(Context context, RecyclerView parent, @LayoutRes int resId) {
        mLayout = LayoutInflater.from(context).inflate(resId, parent, false);
        mLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        mLayout.layout(parent.getLeft(), 0, parent.getRight(), mLayout.getMeasuredHeight());
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                c.save();
                final int top = view.getBottom();
                c.translate(0, top);
                mLayout.draw(c);
                c.restore();
                break;
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        Log.I("%d == %d", parent.getChildAdapterPosition(view), parent.getAdapter().getItemCount());
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.set(0, 0, 0, mLayout.getMeasuredHeight());
        } else {
            outRect.setEmpty();
        }
    }
}
