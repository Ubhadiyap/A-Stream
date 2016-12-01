package com.someoneman.youliveonstream.model.datamodel;

import com.google.api.services.plus.model.Person;
import com.google.api.services.youtube.model.ChannelBrandingSettings;
import com.google.api.services.youtube.model.ChannelSettings;
import com.google.api.services.youtube.model.ImageSettings;

import java.io.IOException;
import java.util.List;

/**
 * Created by nik on 25.04.2016.
 */
public class Channel {
    private String _bannerMobileImageUrl;
    private String mTitle;
    private String mEmail;
    private String mAvatarImageUrl;


    public Channel(com.google.api.services.youtube.model.Channel channel, com.google.api.services.plus.model.Person person) throws IOException {
        ChannelBrandingSettings channelBrandingSettings = channel.getBrandingSettings();

        if (channelBrandingSettings != null) {
            ImageSettings imageSettings = channelBrandingSettings.getImage();
            if (imageSettings != null) {
                _bannerMobileImageUrl = imageSettings.getBannerMobileImageUrl();
            }
            ChannelSettings channelSettings = channelBrandingSettings.getChannel();
            if(channelSettings!=null) {
                mTitle = channelSettings.getTitle();
            }
        }

        List<Person.Emails> emailsList = person.getEmails();

        if (emailsList != null && !emailsList.isEmpty()) {
            mEmail = emailsList.get(0).getValue();
        }

        Person.Image image = person.getImage();

        if (image != null) {
            mAvatarImageUrl = image.getUrl();
        }

        //TODO: add more to Channel
    }

    //region Getters

    public String getBannerMobileImageUrl() {
        return _bannerMobileImageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getAvatarImageUrl(int size) {
        return mAvatarImageUrl.replace("sz=50", "sz=" + size);
    }

    //endregion
}
