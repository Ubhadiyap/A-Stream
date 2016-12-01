package com.someoneman.youliveonstream.views.newbroadcastoptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.utils.Log;

import java.util.ArrayList;

/**
 * Created by ultra on 13.06.2016.
 */
public class NewBroadcastOptionArray extends NewBroadcastOptionLayout implements
        View.OnClickListener,
        DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {

    private int mSelectedIndex = 0;
    private CharSequence[] mArray;
    private ArrayList<OnItemSelectedListener> mOnItemSelectedListeners = new ArrayList<>();

    public NewBroadcastOptionArray(Context context) {
        super(context);

        initViews();
    }

    public NewBroadcastOptionArray(Context context, AttributeSet attrs) {
        super(context, attrs);

        initViews();
        initAttributes(attrs);
    }

    public NewBroadcastOptionArray(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initViews();
        initAttributes(attrs);
    }

    private void initViews() {
        setOnClickListener(this);
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.NewBroadcastOptionArray, 0, 0);

        try {
            mArray = ta.getTextArray(R.styleable.NewBroadcastOptionArray_values);
            setValueText(mArray[mSelectedIndex].toString());
        } finally {
            ta.recycle();
        }
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public String getSelectedValue() {
        return mArray[mSelectedIndex].toString();
    }

    public void selectPosition(int position) {
        if (position >= 0 && position <= mArray.length - 1) {
            mSelectedIndex = position;
            setValueText(mArray[mSelectedIndex].toString());
        }
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getHintText());
        builder.setSingleChoiceItems(mArray, mSelectedIndex, this);
        builder.setOnDismissListener(this);

        builder.create().show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.D("%d", which);
        mSelectedIndex = which;
        setValueText(mArray[mSelectedIndex].toString());

        for (int i = 0; i < mOnItemSelectedListeners.size(); i++)
            mOnItemSelectedListeners.get(0).onItemSelected(mSelectedIndex);

        dialog.cancel();
    }

    public void addOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        if (!mOnItemSelectedListeners.contains(onItemSelectedListener))
            mOnItemSelectedListeners.add(onItemSelectedListener);
    }

    public void removeOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListeners.remove(onItemSelectedListener);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        for (int i = 0; i < mOnItemSelectedListeners.size(); i++)
            mOnItemSelectedListeners.get(0).onNothingSelected();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onNothingSelected();
    }
}
