package com.someoneman.youliveonstream.model.datamodel.broadcast;


/**
 * Created by nik on 21.04.2016.
 */
public interface IBroadcastObserver {
    void OnStateChanged(BroadcastStatus status);
    void OnInfoChanged(BroadcastInfo broadcastInfo);
}
