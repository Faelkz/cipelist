package com.mad.cipelist.main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mad.cipelist.R;
import com.mad.cipelist.common.BaseActivity;
import com.mad.cipelist.common.LocalSearch;
import com.mad.cipelist.common.Utils;
import com.mad.cipelist.login.LoginActivity;
import com.mad.cipelist.main.adapter.MainRecyclerViewAdapter;
import com.mad.cipelist.result.ResultActivity;
import com.mad.cipelist.settings.SettingsActivity;
import com.mad.cipelist.swiper.SwiperActivity;

import java.util.Date;
import java.util.List;

/**
 * Displays the initial landing page with previous searches.
 * This method needs to load all searches associated with the
 * user and display them in the recycler view. Currently the
 * view is populated with default items.
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static String LOG_TAG = "MainActivity";

    private RecyclerView mSearchRecyclerView;

    private MainRecyclerViewAdapter mAdapter;

    private List<LocalSearch> mLocalSearches;
    private String mCurrentUserId;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!= null) {
                    // User is signed in
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        if (mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
        } else {
            mCurrentUserId = "default";
        }

        // View that holds the current searches that are unique to the user
        mSearchRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mSearchRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        if (mAdapter == null) {
            mAdapter = new MainRecyclerViewAdapter(this, mLocalSearches, mCurrentUserId, getUserEmail());
            mSearchRecyclerView.setAdapter(mAdapter);
        }

        FloatingActionButton addRecipeFab = (FloatingActionButton) findViewById(R.id.addRecipeFab);
        addRecipeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewSwiper(7);
            }
        });
    }

    public void startNewSwiper(int amount) {
        Intent intent = new Intent(this, SwiperActivity.class);
        Log.d("NewActivity", "Starting swiper for " + amount + " recipes");
        // The amount sets the number of recipes the swiper needs to find
        intent.putExtra("recipeAmount", amount);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                showToast("About Selected");
                return true;
            case R.id.action_swipe:
                startNewSwiper(7);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                return true;
            case R.id.action_logout:
                mAuth.signOut();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                return true;
            case R.id.action_reset_db:
                Utils.resetDatabase(this.getApplicationContext());

                mLocalSearches = null;
                mAdapter = new MainRecyclerViewAdapter(this, mLocalSearches, mCurrentUserId, getUserEmail());
                mSearchRecyclerView.setAdapter(mAdapter);
                return true;
        }

        try {
            mLocalSearches = getLocalSearches(mCurrentUserId);
        } catch (NullPointerException e) {
            Log.d("ERROR", "Couldn't load local searches, could be empty");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.setOnItemClickListener(new MainRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + (position+1));
                createDialog(v, position);

            }
        });

        try {
            mLocalSearches = getLocalSearches(mCurrentUserId);
            Log.d("Success!", "Loaded a local search into memory with " + mLocalSearches.size() + " items");
        } catch (NullPointerException e) {
            Log.d("ERROR", "Could not load local searches, could be empty");
        }

        if (mLocalSearches != null) {
            mAdapter = new MainRecyclerViewAdapter(this, mLocalSearches, mCurrentUserId, getUserEmail());
            mSearchRecyclerView.setAdapter(mAdapter);
        }

    }

    public void createDialog(View v, int position) {

        final Dialog dialog = new Dialog(v.getContext());
        dialog.setContentView(R.layout.search_detail_dialog);
        dialog.setTitle("Search " + (position + 1));

        TextView searchQuery = (TextView) dialog.findViewById(R.id.searchQueryTv);
        TextView searchDate = (TextView) dialog.findViewById(R.id.searchDateTv);
        TextView searchMatches = (TextView) dialog.findViewById(R.id.matchesCountTv);
        Button viewRecipeBtn = (Button) dialog.findViewById(R.id.viewSearchBtn);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancelBtn);


        searchQuery.setText("Burgers");
        searchDate.setText(new Date().toString());
        searchMatches.setText("7");

        viewRecipeBtn.setOnClickListener(new ViewButtonOnClickListener(dialog, position));
        assert cancelBtn != null;
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public String getUserEmail() {
        if (mAuth.getCurrentUser() == null) {
            return "Anonymous";
        } else {
            return mAuth.getCurrentUser().getEmail();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public List<LocalSearch> getLocalSearches(String id) {
        Log.d(LOG_TAG, "Current UserID: " + id);
        return LocalSearch.find(LocalSearch.class, "user_id = ?", id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }

    public class ViewButtonOnClickListener implements View.OnClickListener {

        int position;
        Dialog dialog;

        public ViewButtonOnClickListener(Dialog dialog, int position) {
            this.position = position;
            this.dialog = dialog;
        }

        @Override
        public void onClick(View view) {
            Intent shoppingListIntent = new Intent(getApplicationContext(), ResultActivity.class);
            shoppingListIntent.putExtra(SwiperActivity.RECIPE_AMOUNT, 7);
            shoppingListIntent.putExtra(SwiperActivity.SEARCH_ID, mAdapter.getSearchId(position));
            startActivity(shoppingListIntent);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            dialog.dismiss();
        }
    }
}
