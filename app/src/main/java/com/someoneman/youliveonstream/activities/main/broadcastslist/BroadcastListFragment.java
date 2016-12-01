package com.someoneman.youliveonstream.activities.main.broadcastslist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.someoneman.youliveonstream.activities.broadcast.BroadcastActivity;
import com.someoneman.youliveonstream.activities.newbroadcast.NewBroadcastActivity;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.utils.Utils;
import com.someoneman.youliveonstream.views.recyclerview.HorizontalItemDecoration;
import com.someoneman.youliveonstream.views.OnCardClickListener;

import java.util.ArrayList;

public class BroadcastListFragment extends Fragment
    implements View.OnClickListener {

    private View fragmentView;

    private BroadcastsAdapter mBroadcastAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private LinearLayoutManager mLayoutManager;
    private RelativeLayout mRelativeLayoutMain;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NewBroadcastActivity.REQUEST_CODE_NEW_BROADCAST:
                if (resultCode == Activity.RESULT_OK) {
                    Broadcast broadcast = (Broadcast) data.getParcelableExtra(NewBroadcastActivity.EXTRA_BROADCAST);

                    mBroadcastAdapter.add(broadcast);
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onCreateNewBroadcastClicked(){
        startActivityForResult(new Intent(getActivity(), NewBroadcastActivity.class), NewBroadcastActivity.REQUEST_CODE_NEW_BROADCAST);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mRecyclerView.getAdapter() == null) {
            mBroadcastAdapter = new BroadcastsAdapter(getContext(), mRecyclerView);
            mRecyclerView.setAdapter(mBroadcastAdapter);
        } else {
            mBroadcastAdapter = (BroadcastsAdapter)mRecyclerView.getAdapter();
        }

        if (savedInstanceState == null) {
            mBroadcastAdapter.loadMore();
        }

        mBroadcastAdapter.setOnCardClickListener(new OnCardClickListener() {
            @Override
            public void onCardClick(View view, int position) {
                startSteamerActivity(mBroadcastAdapter.get(position));
            }

            @Override
            public void onCardLongClick(View view, int position) {
                startContextDialog(mBroadcastAdapter.get(position));
            }
        });

        mFab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        //TODO: create broadcast info listener
        fragmentView = inflater.inflate(R.layout.main_activity_broadcast_list_fragment, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout)fragmentView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = (RecyclerView)fragmentView.findViewById(R.id.recycleView);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new HorizontalItemDecoration(getContext()));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    mFab.hide();
                } else {
                    mFab.show();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int visibleItemCount = mRecyclerView.getChildCount();
                int totalItemCount = mRecyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                if (visibleItemCount + firstVisibleItemPosition == totalItemCount && !mBroadcastAdapter.isEnded())
                    mBroadcastAdapter.loadMore();
            }
        });

        return fragmentView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            ArrayList<Broadcast> broadcastList = savedInstanceState.getParcelableArrayList(BroadcastsAdapter.STATE_ITEMS);
            String nextPageToken = savedInstanceState.getString(BroadcastsAdapter.STATE_NEXTPAGETOKEN);

            mBroadcastAdapter.setItems(broadcastList);
            mBroadcastAdapter.setNextPageToken(nextPageToken);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //mBroadcastAdapter.saveInstanceState(outState);
        outState.putParcelableArrayList(BroadcastsAdapter.STATE_ITEMS, mBroadcastAdapter.getItems());
        outState.putString(BroadcastsAdapter.STATE_NEXTPAGETOKEN, mBroadcastAdapter.getNextPageToken());
    }

    private void startContextDialog(final Broadcast broadcast){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(broadcast.getTitle())
                .setItems(R.array.broadcast_action_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                Utils.shareBroadcast(getActivity(), broadcast);

                                break;
                            }

                            case 1: {
                                //TODO: delete broadcast

                                break;
                            }
                        }
                    }
                })
                .create()
                .show();
    }

    private void startSteamerActivity(Broadcast broadcast) {
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(getActivity(), BroadcastActivityV21.class);
            intent.putExtra(Broadcast.EXTRA_KEY, broadcast);
            startActivity(intent);
        } else {*/
            Intent intent = new Intent(getActivity(), BroadcastActivity.class);
            intent.putExtra(Broadcast.EXTRA_KEY, broadcast);
            startActivity(intent);
        //}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                startActivityForResult(new Intent(getActivity(), NewBroadcastActivity.class),
                        NewBroadcastActivity.REQUEST_CODE_NEW_BROADCAST);

                break;
        }
    }
}

