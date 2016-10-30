package com.mad.cipelist.result;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mad.cipelist.R;
import com.mad.cipelist.common.BaseActivity;
import com.mad.cipelist.result.adapter.ResultAdapter;
import com.mad.cipelist.swiper.SwiperActivity;
import com.viewpagerindicator.TitlePageIndicator;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * Displays two fragments that contain the general recipes and the grocerylist of the loaded search.
 */
public class ResultActivity extends BaseActivity {

    //public static String RESULT_LOGTAG = "ShoppingList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_result, contentFrameLayout);

        Context mContext = this.getApplicationContext();

        mAvi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        mLoadTxt = (TextView) findViewById(R.id.loadText);
        String mSearchId = getIntent().getStringExtra(SwiperActivity.SEARCH_ID);

        FragmentPagerAdapter adapterViewPager = new ResultAdapter(getSupportFragmentManager(), mContext, mSearchId);

        ViewPager mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(adapterViewPager);

        TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
        titleIndicator.setViewPager(mPager);

        // Customises the title indicator
        final float density = getResources().getDisplayMetrics().density;
        titleIndicator.setFooterColor(R.color.colorPrimary);
        titleIndicator.setFooterLineHeight(2 * density); //1dp
        titleIndicator.setFooterIndicatorHeight(3 * density); //3dp
        titleIndicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Underline);
        titleIndicator.setSelectedBold(true);


        stopLoadAnim();

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                showToast("Selected page position: " + position);
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void startLoadAnim() {
        mAvi.smoothToShow();
        mLoadTxt.setVisibility(View.VISIBLE);
    }
    public void stopLoadAnim() {
        mAvi.smoothToHide();
        mLoadTxt.setVisibility(View.GONE);
    }
}
