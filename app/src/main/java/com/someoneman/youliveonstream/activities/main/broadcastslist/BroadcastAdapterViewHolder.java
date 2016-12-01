package com.someoneman.youliveonstream.activities.main.broadcastslist;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.someoneman.youliveonstream.R;

/**
 * Created by nik on 16.04.2016.
 */


public class BroadcastAdapterViewHolder extends RecyclerView.ViewHolder {

    public CardView mCardView;
    public TextView mTextViewTitle, mTextViewInfo, mTextViewStatus;
    public ImageView mImageViewBroadcastThumbnail;

    public BroadcastAdapterViewHolder(View itemView) {
        super(itemView);

        mCardView = (CardView)itemView.findViewById(R.id.card_view);
        mTextViewTitle = (TextView)itemView.findViewById(R.id.textViewBroadcastTitle);
        mTextViewInfo = (TextView)itemView.findViewById(R.id.textViewInfo);
        mTextViewStatus = (TextView)itemView.findViewById(R.id.textViewStatus);
        mImageViewBroadcastThumbnail = (ImageView)itemView.findViewById(R.id.imageViewBroadcastThumbnail);
    }
}