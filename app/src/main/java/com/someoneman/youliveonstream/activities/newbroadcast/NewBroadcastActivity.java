package com.someoneman.youliveonstream.activities.newbroadcast;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.jakewharton.disklrucache.Util;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.model.datamodel.broadcast.Broadcast;
import com.someoneman.youliveonstream.utils.ImageCache;
import com.someoneman.youliveonstream.utils.Log;
import com.someoneman.youliveonstream.utils.Utils;
import com.someoneman.youliveonstream.views.AAppBarLayout;
import com.someoneman.youliveonstream.views.newbroadcastoptions.NewBroadcastOptionArray;
import com.someoneman.youliveonstream.views.newbroadcastoptions.NewBroadcastOptionLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ultra on 03.06.2016.
 */
public class NewBroadcastActivity extends AppCompatActivity implements
        AAppBarLayout.OnStateChangeListener,
        View.OnClickListener {

    //region Consts

    public static final int REQUEST_CODE_NEW_BROADCAST = 0;
    public static final String EXTRA_BROADCAST = "NewBroadcastActivityExtraBroadcast";

    //endregion

    //region Views

    private ActionBar mActionBar;
    private AAppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollTollbarLayout;
    private TextInputEditText mTextInputBroadcastTitle;
    private FloatingActionButton mFabStartBroadcast;
    private NewBroadcastOptionArray mOptionVideoQuality, mOptionAccessType;
    private AAppBarLayout.State mAppBarLayoutLastState;

    //endregion

    //region Vars

    private Context mContext;

    //endregion

    //region Overrides

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbroadcast_activity);

        mContext = this;

        initWindow();
        initViews();
    }

    @Override
    protected void onDestroy() {
        mAppBarLayout.removeOnStateChangedListener(this);
        mFabStartBroadcast.setOnClickListener(null);

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    @Override
    public void onStateChanged(AAppBarLayout.State state) {
        switch (state) {
            case Collapsed:
                if (mTextInputBroadcastTitle.getText().toString().isEmpty())
                    mCollTollbarLayout.setTitle(String.format(getString(R.string.broadcast_by), new SimpleDateFormat(getString(R.string.broadcast_by_datemaket)).format(new Date())));
                else
                    mCollTollbarLayout.setTitle(mTextInputBroadcastTitle.getText());

                if (mAppBarLayoutLastState != AAppBarLayout.State.Collapsed) {
                    mFabStartBroadcast.animate()
                            .translationXBy(getWindow().getDecorView().getWidth())
                            .setDuration(500)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(mFabDownAnimatorListener);

                    mAppBarLayoutLastState = AAppBarLayout.State.Collapsed;
                }

                break;

            case Expended:
                if (mAppBarLayoutLastState != AAppBarLayout.State.Expended) {
                    mFabStartBroadcast.animate()
                            .translationXBy(getWindow().getDecorView().getWidth())
                            .setDuration(500)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(mFabUpAnimatorListener);

                    mAppBarLayoutLastState = AAppBarLayout.State.Expended;
                }

                break;

            case Idle:
                mCollTollbarLayout.setTitle("");

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_startbroadcast:
                createBroadcast();

                break;
        }
    }

    //endregion

    //region Private

    private void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(android.R.color.holo_red_light);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle("");
        }

        ImageCache.getInstance().getImageView(Utils.tempBannerUri, (ImageView)findViewById(R.id.imageBanner));

        mAppBarLayout = (AAppBarLayout)findViewById(R.id.app_bar_layout);
        mCollTollbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        mTextInputBroadcastTitle = (TextInputEditText)findViewById(R.id.broadcastTitle);
        mFabStartBroadcast = (FloatingActionButton)findViewById(R.id.fab_startbroadcast);
        mOptionVideoQuality = (NewBroadcastOptionArray)findViewById(R.id.optionVideoQuality);
        mOptionAccessType = (NewBroadcastOptionArray)findViewById(R.id.optionAccessType);

        mFabStartBroadcast.setOnClickListener(this);

        final NewBroadcastOptionLayout optionUserList = (NewBroadcastOptionLayout)findViewById(R.id.optionUsersList);

        mOptionAccessType.addOnItemSelectedListener(new NewBroadcastOptionArray.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                if (position == 2) {
                    optionUserList.show();
                } else {
                    optionUserList.hide();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mAppBarLayout.addOnStateChangedListener(this);
    }

    //endregion

    private void createBroadcast() {
        final String title;

        if (mTextInputBroadcastTitle.getText().toString().isEmpty())
            title = String.format(getString(R.string.broadcast_by), new SimpleDateFormat(getString(R.string.broadcast_by_datemaket)).format(new Date()));
        else
            title = mTextInputBroadcastTitle.getText().toString();

        int videoQuality = mOptionVideoQuality.getSelectedIndex();
        final int accessType = mOptionAccessType.getSelectedIndex();

        new CreateBroadcastTask().execute(title, videoQuality, accessType);
    }

    //region listeners

    private Animator.AnimatorListener mFabDownAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mFabStartBroadcast.getLayoutParams();

            params.setAnchorId(View.NO_ID);
            params.gravity = Gravity.BOTTOM | Gravity.END;
            mFabStartBroadcast.setLayoutParams(params);
            mFabStartBroadcast.animate().translationX(0).setDuration(500).start();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    private Animator.AnimatorListener mFabUpAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mFabStartBroadcast.getLayoutParams();

            params.setAnchorId(R.id.collapsing_toolbar);
            params.gravity = Gravity.NO_GRAVITY;
            mFabStartBroadcast.setLayoutParams(params);
            mFabStartBroadcast.animate().translationX(0).setDuration(500).start();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    //endregion

    //region Async tasks

    public class CreateBroadcastTask extends AsyncTask<Object, Void, Broadcast> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Создание трансляции");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Broadcast doInBackground(Object... params) {
            String title = (String) params[0];
            int videoQuality = (int) params[1];
            int accessType = (int) params[2];

            Broadcast broadcast = Broadcast.createNewBroadcast(title, accessType);

            return broadcast;
        }

        @Override
        protected void onPostExecute(Broadcast broadcast) {
            mProgressDialog.cancel();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_BROADCAST, broadcast);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    //endregion

}
