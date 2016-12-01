package com.someoneman.youliveonstream.activities.main.broadcastslist;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.Connector;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.model.datamodel.broadcast.video.VideoStatistic;
import com.someoneman.youliveonstream.utils.ImageCache;
import com.someoneman.youliveonstream.utils.Utils;
import com.someoneman.youliveonstream.views.OnCardClickListener;
import com.someoneman.youliveonstream.views.recyclerview.FooterDecoration;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aleksander on 06.02.2016.
 */
public class BroadcastsAdapter extends RecyclerView.Adapter<BroadcastAdapterViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    public final static String STATE_ITEMS = BroadcastsAdapter.class.getName() + "Items";
    public final static String STATE_NEXTPAGETOKEN = BroadcastsAdapter.class.getName() + "NextPageToken";

    private Context mContext;
    private ArrayList<Broadcast> mItems = new ArrayList<>();
    private OnCardClickListener mOnCardClickListener;
    private String mNextPageToken = "";
    private FooterDecoration mFooterDecoration;
    private RecyclerView mRecyclerView;

    public BroadcastsAdapter(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
        mFooterDecoration = new FooterDecoration(context, recyclerView, R.layout.adapted_broadcast_loader);
    }

    public void loadMore() {
        new LoadMoreAsync().execute();
    }

    @Override
    public BroadcastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View broadcastView = layoutInflater.inflate(R.layout.adapter_broadcast, parent, false);

        return new BroadcastAdapterViewHolder(broadcastView);
    }

    @Override
    public void onBindViewHolder(BroadcastAdapterViewHolder viewHolder, int position) {
        Broadcast broadcast = mItems.get(position);

        viewHolder.mCardView.setTag(position);
        viewHolder.mCardView.setOnClickListener(this);
        viewHolder.mCardView.setOnLongClickListener(this);
        viewHolder.mTextViewTitle.setText(broadcast.getTitle());
        viewHolder.mTextViewStatus.setText(Utils.stringFromResourceByName(mContext, broadcast.getLiveStatus()));
        viewHolder.mImageViewBroadcastThumbnail.setImageResource(0);
        viewHolder.mImageViewBroadcastThumbnail.setImageDrawable(null);

        VideoStatistic videoStatistic = broadcast.getVideoStatistic();

        if (videoStatistic != null) {
            viewHolder.mTextViewInfo.setText(String.format(
                    mContext.getString(R.string.broadcast_adapter_info),

                    Utils.intToTextFormat(mContext, videoStatistic.getViewCount().doubleValue()),
                    Utils.intToTextFormat(mContext, videoStatistic.getCommentCount().doubleValue()),
                    Utils.intToTextFormat(mContext, videoStatistic.getLikeCount().doubleValue()),
                    Utils.intToTextFormat(mContext, videoStatistic.getDislikeCount().doubleValue()),
                    Utils.stringFromResourceByName(mContext, "privacy_" + broadcast.getPrivacy()))
            );
        }

        ImageCache.getInstance().getImageView(broadcast.getThumbnailUrl(), viewHolder.mImageViewBroadcastThumbnail);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void add(Broadcast broadcast) {
        mItems.add(0, broadcast);

        notifyItemChanged(0);
    }

    public ArrayList<Broadcast> getItems() {
        return mItems;
    }

    public void setItems(ArrayList<Broadcast> broadcasts) {
        if (broadcasts != null) {
            mItems.clear();
            mItems.addAll(broadcasts);

            notifyDataSetChanged();
        }
    }

    public String getNextPageToken() {
        return mNextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        mNextPageToken = nextPageToken;
    }

    public Broadcast get(int position) {
        return mItems.get(position);
    }

    public void setOnCardClickListener(OnCardClickListener onCardClickListener) {
        mOnCardClickListener = onCardClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnCardClickListener != null)
            mOnCardClickListener.onCardClick(v, (int)v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnCardClickListener != null) {
            mOnCardClickListener.onCardLongClick(v, (int)v.getTag());

            return true;
        }

        return false;
    }

    public void addItems(ArrayList<Broadcast> broadcasts) {
        int size = mItems.size();
        mItems.addAll(broadcasts);

        notifyItemRangeInserted(size, broadcasts.size());
        //notifyDataSetChanged();
    }

    public boolean isEnded() {
        return mNextPageToken == null;
    }

    //region Async tasks

    private class LoadMoreAsync extends AsyncTask<Void, Void, ArrayList<Broadcast>> {

        @Override
        protected void onPreExecute() {
            mRecyclerView.addItemDecoration(mFooterDecoration);
        }

        @Override
        protected ArrayList<Broadcast> doInBackground(Void... params) {
            if (mNextPageToken != null) {
                try {
                    ArrayList<Broadcast> broadcasts = new ArrayList<>();
                    LiveBroadcastListResponse liveBroadcastListResponse = Connector.GetInstance().GetBroadcastList(mNextPageToken);

                    for (int i = 0; i < liveBroadcastListResponse.getItems().size(); i++) {
                        LiveBroadcast liveBroadcast = liveBroadcastListResponse.getItems().get(i);
                        broadcasts.add(new Broadcast(liveBroadcast));
                    }

                    mNextPageToken = liveBroadcastListResponse.getNextPageToken();

                    return broadcasts;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Broadcast> broadcasts) {
            mRecyclerView.removeItemDecoration(mFooterDecoration);
            if (broadcasts != null)
                addItems(broadcasts);
        }
    }

    //endregion
}
