package com.someoneman.youliveonstream.activities.broadcast.chatmessagelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.ChatMessage;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.IChatObserver;
import com.someoneman.youliveonstream.utils.ImageCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ultra on 06.07.2016.
 */
public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessageAdapterViewHolder> implements IChatObserver {

    private static final int ITEMS_SIZE_LIMIT = 250;

    private RecyclerView mRecyclerView;
    private ArrayList<ChatMessage> mItems = new ArrayList<>();

    public ChatMessagesAdapter(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Override
    public ChatMessageAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View chatMessageView = layoutInflater.inflate(R.layout.adapter_chatmessage, parent, false);

        return new ChatMessageAdapterViewHolder(chatMessageView);
    }

    @Override
    public void onBindViewHolder(ChatMessageAdapterViewHolder holder, int position) {
        ChatMessage chatMessage = mItems.get(position);

        holder.textViewAuthor.setText(chatMessage.getAuthorDisplayName());
        holder.textViewMessage.setText(chatMessage.getMessageText());

        ImageCache.getInstance().getImageView(chatMessage.getAuthorProfileImageUrl(), holder.imageViewAvatar);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void OnChatMessagesReceived(List<ChatMessage> chatMessageList) {
        addItems(chatMessageList);
    }

    public void addItems(List<ChatMessage> chatMessageList) {
        int start = getItemCount();
        mItems.addAll(chatMessageList);

        notifyItemRangeInserted(start, chatMessageList.size());
        scrollToLastItem();

        if (getItemCount() > ITEMS_SIZE_LIMIT)
            remove(0, getItemCount() - ITEMS_SIZE_LIMIT);
    }

    public void remove(int startPosition, int size) {
        for (int i = startPosition; i < startPosition + size; i++) {
            mItems.remove(i);
        }

        notifyItemRangeRemoved(startPosition, size);
    }

    public void scrollToLastItem() {
        mRecyclerView.smoothScrollToPosition(getItemCount() - 1);
    }
}
