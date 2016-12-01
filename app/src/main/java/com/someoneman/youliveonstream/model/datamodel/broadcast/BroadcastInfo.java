package com.someoneman.youliveonstream.model.datamodel.broadcast;

import java.math.BigInteger;

/**
 * Created by nik on 21.04.2016.
 */
public class BroadcastInfo {
    final BigInteger likes;
    final BigInteger dislikes;
    final BigInteger viewers;

    public BroadcastInfo(BigInteger likes, BigInteger dislikes, BigInteger viewers) {
        this.likes = likes;
        this.dislikes = dislikes;
        this.viewers = viewers;
    }
    public BigInteger getLikes(){
        return likes;
    }
    public BigInteger getDislikes(){
        return dislikes;
    }
    public BigInteger getViewers(){
        return viewers;
    }
}
