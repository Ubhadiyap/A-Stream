package com.someoneman.youliveonstream.model.datamodel.broadcast.chat;

/**
 * Created by nik on 27.04.2016.
 */
public interface IChatObservable {
    void AddObserver(IChatObserver observer);
    void RemoveObserver(IChatObserver observer);
}
