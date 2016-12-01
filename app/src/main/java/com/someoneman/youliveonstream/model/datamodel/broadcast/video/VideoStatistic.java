package com.someoneman.youliveonstream.model.datamodel.broadcast.video;

import com.google.api.services.youtube.model.VideoStatistics;

import java.math.BigInteger;

/**
 * Created by nik on 02.05.2016.
 */
public class VideoStatistic {
    private BigInteger commentCount;
    private BigInteger dislikeCount;
    private BigInteger likeCount;
    private BigInteger viewCount;

    VideoStatistic(VideoStatistics videoStatistics){
        commentCount = videoStatistics.getCommentCount();
        dislikeCount = videoStatistics.getDislikeCount();
        likeCount = videoStatistics.getLikeCount();
        viewCount = videoStatistics.getViewCount();
    }

    public BigInteger getCommentCount() {
        return commentCount;
    }

    public BigInteger getDislikeCount() {
        return dislikeCount;
    }

    public BigInteger getLikeCount() {
        return likeCount;
    }

    public BigInteger getViewCount() {
        return viewCount;
    }
}
