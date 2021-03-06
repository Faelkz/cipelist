package com.mad.cipelist.swiper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mad.cipelist.R;
import com.mad.cipelist.common.BaseActivity;
import com.mad.cipelist.common.Utils;
import com.mad.cipelist.search_result.SearchResultActivity;
import com.mad.cipelist.services.ApiRecipeLoader;
import com.mad.cipelist.services.MockRecipeLoader;
import com.mad.cipelist.services.RecipeLoader;
import com.mad.cipelist.services.model.LocalRecipe;
import com.mad.cipelist.services.model.LocalSearch;
import com.mad.cipelist.swiper.model.SearchFilter;
import com.mad.cipelist.swiper.widget.RecipeCard;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Displays a swiper that the used can use to select recipes they like.
 * Searches are only saved if the swiping session completes.
 */
public class SwiperActivity extends BaseActivity {

    public static final String SWIPER_LOGTAG = "Swiper";
    public static final String SEARCH_ID = "searchId";

    @BindView(R.id.swiper_avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.swiper_load_text)
    TextView loadText;
    @BindView(R.id.swipe_view)
    SwipePlaceHolderView swipeView;
    @BindView(R.id.swiper_button_holder)
    LinearLayout swipeButtonHolder;
    @BindView(R.id.swiper_progress_bar)
    ProgressBar swipeProgressBar;
    @BindView(R.id.swiper_error)
    TextView errorText;
    private int mRecipeAmount;
    private int mRecipeLoadCount;
    private int mSwipeCount;
    private List<LocalRecipe> mSelectedRecipes;
    private Context mContext;
    private SearchFilter mFilter;
    private ArrayList<String> mQueries;
    private int mQueriesAmount;

    @OnClick(R.id.swiper_error)
    public void onClick() {
        Intent intent = new Intent(this, SearchFilterActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_swiper, contentFrameLayout);
        ButterKnife.bind(this);

        mContext = this.getApplicationContext();
        mRecipeLoadCount = 0;
        mSelectedRecipes = new ArrayList<>();

        // Getting things passed by the searchfilteractivity
        mRecipeAmount = getIntent().getIntExtra(SearchFilterActivity.RECIPE_AMOUNT, 0);
        swipeProgressBar.setMax(mRecipeAmount);

        // Creates a search filter using passed parameters
        mFilter = createSearchFilter();
        mQueries = getIntent().getExtras().getStringArrayList(SearchFilterActivity.QUERY);
        mQueriesAmount = (mQueries != null && !mQueries.isEmpty()) ? mQueries.size() : 1;

        // Creates a swipe view with specified layout
        swipeView.getBuilder()
                .setDisplayViewCount(3)
                .setSwipeDecor(new SwipeDecor()
                        .setPaddingTop(20)
                        .setRelativeScale(0.01f)
                        .setSwipeInMsgLayoutId(R.layout.swiper_in_msg)
                        .setSwipeOutMsgLayoutId(R.layout.swiper_out_msg));


        // Programatically call the doSwipe function on reject button click
        findViewById(R.id.reject_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeView.doSwipe(false);
            }
        });

        // Programatically call the doSwipe function on accept button click
        findViewById(R.id.accept_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeView.doSwipe(true);
            }
        });

        // Initiate a new call to the yummly api to get recipes based on search filter parameters
        AsyncRecipeLoader loader = new AsyncRecipeLoader(mFilter);
        loader.execute("");
    }

    /**
     * Creates a filter object that uses the values passed from the search filter activity.
     *
     * @return a search filter
     */
    public SearchFilter createSearchFilter() {

        int maxTime = getIntent().getIntExtra(SearchFilterActivity.MAX_TIME, -1);
        ArrayList<String> diets = getIntent().getExtras().getStringArrayList(SearchFilterActivity.DIET);
        ArrayList<String> cuisines = getIntent().getExtras().getStringArrayList(SearchFilterActivity.CUISINE);
        ArrayList<String> allergies = getIntent().getExtras().getStringArrayList(SearchFilterActivity.ALLERGY);
        ArrayList<String> courses = getIntent().getExtras().getStringArrayList(SearchFilterActivity.COURSE);

        return new SearchFilter(maxTime, diets, cuisines, allergies, courses, getSearchId());
    }

    /**
     * Retrieves the current user id to generate a unique id for the search.
     */
    private String getSearchId() {
        String id = getUserId() + System.currentTimeMillis();
        Log.d(SWIPER_LOGTAG, "Current search id is: " + id);
        return id;
    }

    /**
     * Stores the selected recipes and starts the result activity.
     */
    private void onSwipeLimitReached() {

        LocalSearch search = new LocalSearch();
        search.userId = getUserId();
        search.searchId = mFilter.getSearchId();
        search.searchTimeStamp = Utils.getCurrentDate();
        search.title = getSearchTitle();
        search.save();
        startLoadAnim(avi, loadText, getString(R.string.saving_recipes));
        new AsyncRecipeUpdate(mSelectedRecipes).execute();

    }

    /**
     * Define the title for the search based on search parameters
     *
     * @return title
     */
    private String getSearchTitle() {
        String title = "";
        if (mQueries != null && !mQueries.isEmpty()) {
            for (String query : mQueries) {
                title += query + ", ";
            }
            // Capitalize first word
            title = Character.toString(title.charAt(0)).toUpperCase() + title.substring(1);
            // To remove the trailing comma
            title = title.substring(0, title.length() - 2);
            return title;
        }
        return null;
    }

    /**
     * Start the result activity with an animation and passes a search id.
     */
    public void startResultActivity() {
        Intent shoppingListIntent = new Intent(getApplicationContext(), SearchResultActivity.class);
        shoppingListIntent.putExtra(SEARCH_ID, mFilter.getSearchId());
        startActivity(shoppingListIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Adds cards from the global variable mRecipes to the SwipeView.
     */
    public void addCards(List<LocalRecipe> recipes) {
        stopLoadAnim(avi, loadText);
        mRecipeLoadCount += 10;

        try {
            for (final LocalRecipe recipe : recipes) {
                swipeView.addView(new RecipeCard(mContext, recipe, new RecipeCard.SwipeHandler() {
                    @Override
                    public void onSwipeIn() {

                        mSwipeCount++;
                        // Set the search id of the recipe so that is is associated with the current search
                        recipe.setSearchId(mFilter.getSearchId());
                        mSelectedRecipes.add(recipe);
                        swipeProgressBar.setProgress(mSelectedRecipes.size());

                        handleSwipeCount();
                        Log.d("EVENT", "onSwipedIn");
                    }

                    @Override
                    public void onSwipedOut() {
                        mSwipeCount++;
                        handleSwipeCount();
                    }
                }));
            }
        } catch (NullPointerException n) {
            Log.d(SWIPER_LOGTAG, "SwiperActivity could not retrieve json data, a null pointer exception was thrown");
        }
    }

    /**
     * Calculates the amount of recipes selected and the recipes left in the stack and acts accordingly.
     */
    private void handleSwipeCount() {
        if (mSelectedRecipes.size() >= mRecipeAmount) {
            onSwipeLimitReached();
        } else if (mSwipeCount >= (mRecipeLoadCount * mQueriesAmount - 5)) {
            new AsyncRecipeLoader(mFilter).execute();
        }
        if (mSwipeCount >= mRecipeLoadCount) {
            startLoadAnim(avi, loadText, getString(R.string.loading_more_recipes));
        }
    }

    /**
     * Initiates an asynchronous call to the yummly api.
     */
    private class AsyncRecipeLoader extends AsyncTask<String, Integer, List<LocalRecipe>> {

        private SearchFilter mFilter;

        AsyncRecipeLoader(SearchFilter filter) {
            this.mFilter = filter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mRecipeLoadCount == 0) {
                startLoadAnim(avi, loadText, getString(R.string.loading_recipes));
            }
        }

        @Override
        protected List<LocalRecipe> doInBackground(String... strings) {
            // MockLoader that retrieves recipes from a locally saved search in case it matches the criteria
            // If recipes are processed and stored locally this could reduce network usage and speed up searchess
            if (mQueries != null && mQueries.size() >= 3 && mQueries.get(0).equals(getString(R.string.pasta)) && mQueries.get(1).equals(getString(R.string.pizza)) && mQueries.get(2).equals(getString(R.string.burger))) {
                RecipeLoader loader = new MockRecipeLoader(getApplicationContext());
                try {
                    // Sleep so that animation is shown and views flow better
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return loader.getRecipes(null);
            }
            List<LocalRecipe> response = new ArrayList<>();
            RecipeLoader mLoader = new ApiRecipeLoader(mContext, mFilter, mRecipeLoadCount);
            if (mQueries != null && !mQueries.isEmpty()) {
                for (String query : mQueries) {
                    response.addAll(mLoader.getRecipes(query));
                }
            }
            List<LocalRecipe> filler = mLoader.getRecipes(null);
            if (filler != null) {
                response.addAll(filler);
            }
            Collections.shuffle(response);
            return new ArrayList<>(new LinkedHashSet<>(response));
        }

        @Override
        protected void onPostExecute(List<LocalRecipe> localRecipes) {
            super.onPostExecute(localRecipes);
            // Stop the loading animation
            // Add the loaded cards to the SwiperView in the Main Thread.
            if (localRecipes.size() == 0) {
                errorText.setVisibility(View.VISIBLE);
                errorText.setClickable(true);
            }
            addCards(localRecipes);
            stopLoadAnim(avi, loadText);
        }
    }

    /**
     * Asynchronous call to update the selected recipes with information from the api.
     * The GET requests provide more information about the recipes which is necessary for the next
     * step in the application
     */
    public class AsyncRecipeUpdate extends AsyncTask<Void, Void, Void> {

        private final List<LocalRecipe> recipes;

        /**
         * Default constructor that can store the selected recipes
         * @param recipes selected recipes
         */
        AsyncRecipeUpdate(List<LocalRecipe> recipes) {
            this.recipes = recipes;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            RecipeLoader loader = new ApiRecipeLoader(mContext);

            for (LocalRecipe r : recipes) {
                try {
                    // Check if the file exists in the assets folder already
                    if (Arrays.asList(mContext.getResources().getAssets().list("")).contains(r.getmId() + getString(R.string._json))) {
                        loader = new MockRecipeLoader(mContext);
                        loader.updateRecipe(r);
                    } else {
                        loader.updateRecipe(r);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            startResultActivity();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeButtonHolder.setVisibility(View.INVISIBLE);
            swipeView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}
