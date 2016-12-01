package com.someoneman.youliveonstream.model.datamodel;

import android.os.AsyncTask;

import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.someoneman.youliveonstream.model.Connector;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.model.datamodel.broadcast.IBroadcastListObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nik on 27.04.2016.
 */
public class BroadcastList {
    private ArrayList<Broadcast> mBroadcastList;
    String _nextPageToken = null;
    boolean endReached=false;
    IBroadcastListObserver _observer;

    public BroadcastList(IBroadcastListObserver observer) throws IOException {
        mBroadcastList = new ArrayList<>();
        _observer = observer;
        //LoadMore();

    }

    public ArrayList<Broadcast> getItems() {
        return mBroadcastList;
    }

    public void setItems(ArrayList<Broadcast> broadcasts) {
        mBroadcastList.clear();
        mBroadcastList.addAll(broadcasts);
    }

    public void LoadMore() throws IOException {
        if(endReached)
            return;
        new AsyncTask<Void,Void,List<Broadcast>>(){

            @Override
            protected List<Broadcast> doInBackground(Void... params) {
                List<Broadcast> broadcastList=null;
                try {
                    LiveBroadcastListResponse liveBroadcastListResponse = Connector.GetInstance().GetBroadcastList(_nextPageToken);
                    broadcastList = new ArrayList<>();
                    int size = liveBroadcastListResponse.getItems().size();
                    for (int i = 0; i < size; i++) {
                        LiveBroadcast liveBroadcast = liveBroadcastListResponse.getItems().get(i);
                        Broadcast broadcast = null;
                        try {
                            broadcast = new Broadcast(liveBroadcast);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        broadcastList.add(broadcast);
                    }
                    _nextPageToken = liveBroadcastListResponse.getNextPageToken();
                    if(_nextPageToken == null)
                        endReached=true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return broadcastList;
            }

            @Override
            protected void onPostExecute(List<Broadcast> broadcastList) {
                synchronized (mBroadcastList) {
                    mBroadcastList.addAll(broadcastList);
                }
                _observer.onDataSetChanged();
            }
        }.execute();


    }
    public int size(){
        synchronized (mBroadcastList){
            return mBroadcastList.size();
        }
    }

    public Broadcast get(int position){
        synchronized (mBroadcastList){
            return mBroadcastList.get(position);
        }
    }

    public void add(int i, Broadcast broadcast) {
        synchronized (mBroadcastList){
            mBroadcastList.add(i,broadcast);
        }
    }

    public boolean isLastPosition(int position){
        synchronized (mBroadcastList){
            return position==(mBroadcastList.size()-1);
        }
    }
}
