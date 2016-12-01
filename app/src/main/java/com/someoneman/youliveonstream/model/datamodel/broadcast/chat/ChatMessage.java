package com.someoneman.youliveonstream.model.datamodel.broadcast.chat;

import com.google.api.services.youtube.model.LiveChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksander on 23.02.2016.
 */
public class ChatMessage {

    private String authorDisplayName;
    private String messageText;
    private String authorProfileImageUrl;
    private ChatMessageAuthorStatus authorStatus;

    public ChatMessage(LiveChatMessage liveChatMessage) {

        authorDisplayName=liveChatMessage.getAuthorDetails().getDisplayName();
        messageText=liveChatMessage.getSnippet().getTextMessageDetails().getMessageText();
        authorProfileImageUrl=liveChatMessage.getAuthorDetails().getProfileImageUrl();

        if (liveChatMessage.getAuthorDetails().getIsChatOwner()) {
             authorStatus= ChatMessageAuthorStatus.Owner;
        } else if (liveChatMessage.getAuthorDetails().getIsChatSponsor()) {
            authorStatus=ChatMessageAuthorStatus.Sponsor;
        } else if (liveChatMessage.getAuthorDetails().getIsChatModerator()) {
            authorStatus=ChatMessageAuthorStatus.Moderator;
        }
    }

    public static List<ChatMessage> FromArray(List<LiveChatMessage> liveChatMessageList) {
        List<ChatMessage> result = new ArrayList<>();

        for (LiveChatMessage liveChatMessage : liveChatMessageList)
            result.add(new ChatMessage(liveChatMessage));

        return result;
    }

    public String getAuthorDisplayName() {
        return  authorDisplayName;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl;
    }

    public ChatMessageAuthorStatus getAuthorStatus() {
        return authorStatus;
    }

}
