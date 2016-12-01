package com.someoneman.youliveonstream.model.datamodel.broadcast;

/**
 * Created by nik on 21.04.2016.
 */
public interface IBroadcastObservable {
    void AddObserver(IBroadcastObserver observer);
    void RemoveObserver(IBroadcastObserver observer);
}
