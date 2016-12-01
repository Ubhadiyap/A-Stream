package com.someoneman.youliveonstream.views.newbroadcastoptions;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.utils.Log;

import java.util.ArrayList;

/**
 * Created by ultra on 13.06.2016.
 */
public class NewBroadcastOptionLayout extends RelativeLayout {

    private TextView mTextViewHint, mTextViewValue;
    private ImageView mImageViewIcon;

    public NewBroadcastOptionLayout(Context context) {
        super(context);

        initView();
    }

    public NewBroadcastOptionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
        initAttributes(attrs);
    }

    public NewBroadcastOptionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
        initAttributes(attrs);
    }

    private void initView() {
        inflate(getContext(), R.layout.newbroadcast_option_layout, this);

        mTextViewValue = (TextView)findViewById(R.id.newBroadcastOptionLayoutValue);
        mTextViewHint = (TextView)findViewById(R.id.newBroadcastOptionLayoutHint);
        mImageViewIcon = (ImageView)findViewById(R.id.newBroadcastOptionLayoutIcon);
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.NewBroadcastOptionLayout, 0, 0);

        try {
            mTextViewValue.setText(ta.getString(R.styleable.NewBroadcastOptionLayout_value));
            mTextViewHint.setText(ta.getString(R.styleable.NewBroadcastOptionLayout_hint));
            mImageViewIcon.setImageDrawable(ta.getDrawable(R.styleable.NewBroadcastOptionLayout_image));
        } finally {
            ta.recycle();
        }
    }

    public void setHintText(String text) {
        mTextViewHint.setText(text);
    }

    public void setValueText(String text) {
        mTextViewValue.setText(text);
    }

    public String getHintText() {
        return mTextViewHint.getText().toString();
    }

    public String getValueText() {
        return mTextViewValue.getText().toString();
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }
}
