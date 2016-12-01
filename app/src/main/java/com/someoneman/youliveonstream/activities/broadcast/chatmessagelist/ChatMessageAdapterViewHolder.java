package com.someoneman.youliveonstream.activities.broadcast.chatmessagelist;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.someoneman.youliveonstream.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ultra on 06.07.2016.
 */
public class ChatMessageAdapterViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewAuthor, textViewMessage;
    public CircleImageView imageViewAvatar;

    public ChatMessageAdapterViewHolder(View itemView) {
        super(itemView);

        textViewAuthor = (TextView)itemView.findViewById(R.id.author);
        textViewMessage = (TextView)itemView.findViewById(R.id.message);
        imageViewAvatar = (CircleImageView)itemView.findViewById(R.id.avatar);
    }
}
