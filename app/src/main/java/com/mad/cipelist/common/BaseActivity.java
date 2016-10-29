package com.mad.cipelist.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mad.cipelist.R;
import com.mad.cipelist.main.MainActivity;
import com.mad.cipelist.swiper.SwiperActivity;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * Defines the base activity which extends certain activities in the application.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected AVLoadingIndicatorView mAvi;
    protected TextView mLoadTxt;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_drawer);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.nav_swiper:
                        Intent swiperIntent = new Intent(getApplicationContext(), SwiperActivity.class);
                        swiperIntent.putExtra("recipeAmount", 7);
                        startActivity(swiperIntent);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_home:
                        if (!(BaseActivity.this instanceof MainActivity)) {
                            finish();
                        }
                        drawerLayout.closeDrawers();
                        break;


                }
                return false;
            }
        });
    }

    public void startLoadAnim(String msg) {
        mAvi.smoothToShow();
        mLoadTxt.setText(msg);
        mLoadTxt.setVisibility(View.VISIBLE);
    }

    public void stopLoadAnim() {
        mAvi.smoothToHide();
        mLoadTxt.setVisibility(View.GONE);
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        actionBarDrawerToggle.syncState();
    }



}
