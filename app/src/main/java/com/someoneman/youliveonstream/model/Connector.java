package com.someoneman.youliveonstream.model;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveChatMessageListResponse;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamListResponse;
import com.google.api.services.youtube.model.LiveStreamSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.someoneman.youliveonstream.App;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.Chat;
import com.someoneman.youliveonstream.model.datamodel.broadcast.chat.ChatMessage;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by nik on 25.04.2016.
 */
public class Connector {
    private static Connector mInstance;

    private YouTube mYouTube;
    private Plus mPlus;

    private Connector(GoogleCredential googleCredential){
        YouTube.Builder youtubeBuilder = new YouTube.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), googleCredential);
        youtubeBuilder.setApplicationName("you-live-on-stream-android");

        mYouTube = youtubeBuilder.build();

        mPlus = new Plus.Builder(AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), googleCredential)
                .setApplicationName("you-live-on-stream-android")
                .build();
    }

    public static void Init(GoogleCredential googleCredential){
        mInstance = new Connector(googleCredential);
    }

    public static Connector GetInstance(){
        return mInstance;
    }

    public Channel GetChannelInfo() throws IOException {
        YouTube.Channels.List channelsList = mYouTube.channels().list("id,brandingSettings");
        channelsList.setMine(true);
        ChannelListResponse channelListResponse = channelsList.execute();
        return channelListResponse.getItems().get(0);
    }

    public Person GetAccount() throws IOException {
        return mPlus.people().get("me").execute();
    }

    //region Broadcast

    public LiveBroadcast CreateBroadcast(String title, int access) throws IOException {
        LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
        broadcastSnippet.setTitle(title);
        broadcastSnippet.setScheduledStartTime(new DateTime(new Date(), TimeZone.getDefault()));

        LiveBroadcastStatus broadcastStatus = new LiveBroadcastStatus();
        broadcastStatus.setPrivacyStatus(App.getContext().getResources().getStringArray(R.array.youtubeapi_access_to_stream)[access]);

        LiveBroadcast broadcast = new LiveBroadcast();
        broadcast.setKind("youtube#liveBroadcast");
        broadcast.setSnippet(broadcastSnippet);
        broadcast.setStatus(broadcastStatus);

        YouTube.LiveBroadcasts.Insert liveBroadcastInsert = mYouTube.liveBroadcasts().insert("snippet,status,contentDetails", broadcast);

        return liveBroadcastInsert.execute();
    }

    public void DeleteBroadcast(String broadcastId) throws IOException {
        mYouTube.liveBroadcasts().delete(broadcastId).execute();
    }

    public String SetBroadcastStatus(String broadcastId, String status) throws IOException {
        LiveBroadcast liveBroadcast =  mYouTube.liveBroadcasts().transition(status, broadcastId, "id,status").execute();
        return liveBroadcast.getStatus().getLifeCycleStatus();
    }

    public String GetBroadcastStatus(String broadcastId) throws IOException {
        YouTube.LiveBroadcasts.List broadcastList = mYouTube.liveBroadcasts().list("id,status");
        broadcastList.setId(broadcastId);
        LiveBroadcastListResponse liveBroadcastListResponse = broadcastList.execute();

        LiveBroadcast liveBroadcast = liveBroadcastListResponse.getItems().get(0);

        return liveBroadcast.getStatus().getLifeCycleStatus();
    }

    public LiveBroadcast BindStream(String broadcastId, String streamId) throws IOException {
        YouTube.LiveBroadcasts.Bind liveBroadcastBind = mYouTube.liveBroadcasts().bind(broadcastId, "id,snippet,contentDetails,status");
        liveBroadcastBind.setStreamId(streamId);

        return liveBroadcastBind.execute();
    }

    //endregion

    //region Stream

    public LiveStream CreateStream(String title, int quality) throws IOException {
        String cdnFormat = App.getContext().getResources().getStringArray(R.array.youtubeapi_cdn_format)[quality];

        LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
        streamSnippet.setTitle(String.format("%s - %s", title, cdnFormat));

        CdnSettings cdnSettings = new CdnSettings();
        cdnSettings.setFormat(cdnFormat);
        cdnSettings.setIngestionType("rtmp");

        LiveStream stream = new LiveStream();
        stream.setKind("youtube#liveStream");
        stream.setSnippet(streamSnippet);
        stream.setCdn(cdnSettings);

        YouTube.LiveStreams.Insert liveStreamInsert = mYouTube.liveStreams().insert("snippet,cdn,status,id", stream);

        return liveStreamInsert.execute();
    }

    public LiveStream GetStream(String boundStreamId) throws IOException {
        LiveStreamListResponse liveStreamListResponse = mYouTube.liveStreams().list("id,cdn")
                .setId(boundStreamId)
                .execute();

        return liveStreamListResponse.getItems().get(0);
    }

    public VideoListResponse GetVideoStats(String id) throws IOException {
        YouTube.Videos.List list = mYouTube.videos().list("id,statistics,liveStreamingDetails");
        list.setId(id);

        return list.execute();
    }

    public void DeleteStream(String streamId) throws IOException {
        mYouTube.liveStreams().delete(streamId).execute();
    }

    public String GetStreamStatus(String streamId) throws IOException {
        LiveStreamListResponse liveStreamListResponse = mYouTube.liveStreams().list("id,status")
                .setId(streamId)
                .execute();

        LiveStream liveStream = liveStreamListResponse.getItems().get(0);

        return liveStream.getStatus().getStreamStatus();
    }

    public String GetLiveChatId(String broadcastId) throws IOException {
        LiveBroadcastListResponse liveBroadcastListResponse = mYouTube.liveBroadcasts().list("id,snippet")
                .setId(broadcastId)
                .execute();

        LiveBroadcast liveBroadcast = liveBroadcastListResponse.getItems().get(0);

        return liveBroadcast.getSnippet().getLiveChatId();
    }

    public List<ChatMessage> GetChatMessages(Chat chat) throws IOException {
        LiveChatMessageListResponse liveChatMessageListResponse = mYouTube.liveChatMessages()
                .list(chat.getId(), "id,snippet,authorDetails")
                .setPageToken(chat.getPageToken())
                .execute();

        chat.setPageToken(liveChatMessageListResponse.getNextPageToken());

        return ChatMessage.FromArray(liveChatMessageListResponse.getItems());
    }

    public LiveBroadcastListResponse GetBroadcastList(String nextPageToken) throws IOException {
        YouTube.LiveBroadcasts.List liveBroadcastList = mYouTube.liveBroadcasts().list("id,snippet,status,contentDetails");
        liveBroadcastList.setMine(true);
        liveBroadcastList.setPageToken(nextPageToken);

        return liveBroadcastList.execute();
    }

    /*
    public LiveChatMessageListResponse GetChatMessages(String liveChatId, String pageToken) throws IOException {
        YouTube.LiveChatMessages.List liveChatMessagesList = mYouTube.liveChatMessages().list(liveChatId, "id,snippet,authorDetails");
        liveChatMessagesList.setPageToken(pageToken);

        return liveChatMessagesList.execute();
    }*/

    public LiveBroadcast GetBroadcast(String id) throws IOException {
        YouTube.LiveBroadcasts.List broadcastList = mYouTube.liveBroadcasts().list("id,snippet");
        broadcastList.setId(id);
        LiveBroadcastListResponse liveBroadcastListResponse = broadcastList.execute();

        return liveBroadcastListResponse.getItems().get(0);
    }

    public Video getVideo(String id) throws IOException {
        YouTube.Videos.List videoList = mYouTube.videos().list("id,statistics,liveStreamingDetails");
        videoList.setId(id);
        VideoListResponse videoListResponse = videoList.execute();
        return videoListResponse.getItems().get(0);
    }

    //endregion
}
