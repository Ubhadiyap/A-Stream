package com.someoneman.youliveonstream.model.datamodel.broadcast.chat;

import java.util.List;

/**
 * Created by nik on 27.04.2016.
 */
public interface IChatObserver {
    void OnChatMessagesReceived(List<ChatMessage> chatMessageList);
}
