package com.someoneman.youliveonstream.activities.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.someoneman.youliveonstream.R;
import com.someoneman.youliveonstream.activities.auth.AuthActivity;
import com.someoneman.youliveonstream.model.Connector;
import com.someoneman.youliveonstream.model.SettingsManager;
import com.someoneman.youliveonstream.model.datamodel.Channel;
import com.someoneman.youliveonstream.utils.ImageCache;
import com.someoneman.youliveonstream.utils.Log;
import com.someoneman.youliveonstream.utils.Utils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    //region Views

    NavigationView mNavigationView;

    //endregion

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        Log.D("%s onCreate", this.getClass().getName());
        String accessToken = SettingsManager.getInstance().GetAccessToken();

        if (accessToken == null) {
            return; //TODO: Это вообще возможно?
        }
        GoogleCredential googleCredential = new GoogleCredential().setAccessToken(accessToken);
        Connector.Init(googleCredential);


        initViews();
        new InitAccountTask().execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_logout:
                logout();

                break;
            case R.id.nav_change:
                changeChannel();

                break;
        }

        if (item.isCheckable())
            mNavigationView.setCheckedItem(item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return false;
    }

    //endregion

    //region Private

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.all_broadcasts));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_all_broadcasts);

        View headerView = mNavigationView.getHeaderView(0);

        FrameLayout frameLayoutTexts = (FrameLayout)headerView.findViewById(R.id.layout_texts);
        frameLayoutTexts.setOnClickListener(this);
    }

    private void navigateToAuthActivity(boolean changeChannel){
        Intent intent = new Intent(this, AuthActivity.class);

        Bundle b = new Bundle();
        b.putBoolean("changeChannel", changeChannel);
        intent.putExtras(b);

        startActivity(intent);
        finish();
    }

    private void logout() {
        SettingsManager.getInstance().RemoveAuthSettings();
        navigateToAuthActivity(false);
    }

    private void changeChannel(){
        navigateToAuthActivity(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_texts:
                ImageView imageViewIcTriangle = (ImageView)mNavigationView.getHeaderView(0).findViewById(R.id.ic_triangle);
                imageViewIcTriangle.setRotation(imageViewIcTriangle.getRotation() + 180);

                Menu menu = mNavigationView.getMenu();

                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    menuItem.setVisible(!menuItem.isVisible());
                }

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region Async tasks

    private class InitAccountTask extends AsyncTask<Void, Void, Channel> {
        @Override
        protected Channel doInBackground(Void... params) {
            Channel channel = null;

            try {
                channel = new Channel(Connector.GetInstance().GetChannelInfo(), Connector.GetInstance().GetAccount());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return channel;
        }

        @Override
        protected void onPostExecute(Channel channel) {
            if (channel == null)
                return;
            TextView textViewChannelName = (TextView)findViewById(R.id.channel_name);
            TextView textViewAccountName = (TextView)findViewById(R.id.account_name);
            ImageView imageViewChannelBanner = (ImageView)findViewById(R.id.channel_banner);
            ImageView imageViewAvatar = (ImageView)findViewById(R.id.account_avatar);

            textViewChannelName.setText(channel.getTitle());
            textViewAccountName.setText(channel.getEmail());

            String bannerUri = channel.getBannerMobileImageUrl();
            String avatarUri = channel.getAvatarImageUrl(imageViewAvatar.getWidth());

            Utils.tempBannerUri = bannerUri; //Великий временный костыль

            ImageCache.getInstance().getImageView(bannerUri, imageViewChannelBanner);
            ImageCache.getInstance().getImageView(avatarUri, imageViewAvatar);
        }
    }

    //endregion
}
