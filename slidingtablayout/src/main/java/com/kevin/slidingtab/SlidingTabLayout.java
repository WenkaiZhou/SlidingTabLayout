/*
 * Copyright (c) 2018 Kevin zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevin.slidingtab;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * SlidingTabLayout
 *
 * @author zwenkai@foxmail.com, Created on 2018-09-29 14:24:57
 *          Major Function：<b>SlidingTabLayout</b>
 *          <p/>
 *          Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
public class SlidingTabLayout extends HorizontalScrollView implements ViewTreeObserver.OnGlobalLayoutListener {

    public static final int MODE_SCROLLABLE = 0;
    public static final int MODE_FIXED = 1;

    @IntDef({MODE_SCROLLABLE, MODE_FIXED})
    public @interface TabMode {
    }

    @TabMode
    private int mMode;
    private float mLeftPadding;
    private float mRightPadding;
    private int mTabLayoutRes;
    private boolean mIsTabTextBold;
    int mTabPaddingStart;
    int mTabPaddingTop;
    int mTabPaddingEnd;
    int mTabPaddingBottom;
    private int mTabTextColor;
    private int mSelectedTabTextColor;
    private int mTabTextSize;
    private int mTabSelectedTextSize;
    private boolean mSmoothScroll;

    private ViewPager mViewPager;
    private SlidingTabStrip mSlidingTabStrip;

    private TabLayoutOnPageChangeListener mPageChangeListener;
    private AdapterChangeListener mAdapterChangeListener;
    private DataSetObserver mPagerAdapterObserver;

    private OnTabCreateListener mOnTabCreateListener;
    private OnTabClickListener mOnTabClickListener;
    private OnSelectedTabClickListener mOnSelectedTabClickListener;
    private OnTabSelectedListener mOnTabSelectedListener;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setHorizontalScrollBarEnabled(false);
        this.setFillViewport(true);

        this.mSlidingTabStrip = new SlidingTabStrip(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);
        this.mMode = a.getInt(R.styleable.SlidingTabLayout_stl_tabMode, MODE_FIXED);
        this.mLeftPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_leftPadding, 0);
        this.mRightPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_rightPadding, 0);
        this.mSmoothScroll = a.getBoolean(R.styleable.SlidingTabLayout_stl_smoothScroll, true);
        this.mTabLayoutRes = a.getResourceId(R.styleable.SlidingTabLayout_stl_tabLayout, 0);
        this.mSmoothScroll = a.getBoolean(R.styleable.SlidingTabLayout_stl_smoothScroll, true);
        mTabPaddingStart = mTabPaddingTop = mTabPaddingEnd = mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPadding, 0);
        this.mTabPaddingStart = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPaddingStart, mTabPaddingStart);
        this.mTabPaddingTop = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPaddingTop, mTabPaddingTop);
        this.mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPaddingEnd, mTabPaddingEnd);
        this.mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPaddingBottom, mTabPaddingBottom);
        this.mTabTextSize = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabTextSize, dpToPx(16));
        this.mTabSelectedTextSize = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabSelectedTextSize, mTabTextSize);
        this.mTabTextColor = a.getColor(R.styleable.SlidingTabLayout_stl_tabTextColor, Color.GRAY);
        this.mSelectedTabTextColor = a.getColor(R.styleable.SlidingTabLayout_stl_tabSelectedTextColor, Color.DKGRAY);
        this.mIsTabTextBold = a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextBold, false);

        this.mSlidingTabStrip.setGravity(a.getInt(R.styleable.SlidingTabLayout_stl_tabGravity, Gravity.CENTER_VERTICAL));
        this.mSlidingTabStrip.setLeftPadding(mLeftPadding);
        this.mSlidingTabStrip.setRightPadding(mRightPadding);

        this.mSlidingTabStrip.setTabTextBold(mIsTabTextBold);
        this.mSlidingTabStrip.setTabText(mTabTextSize, mTabTextColor);
        this.mSlidingTabStrip.setTabSelectedText(mTabSelectedTextSize, mSelectedTabTextColor);

        this.mSlidingTabStrip.setIndicatorCreep(a.getBoolean(R.styleable.SlidingTabLayout_stl_tabIndicatorCreep, false));
        this.mSlidingTabStrip.setIndicatorHeight(a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorHeight, 0));
        this.mSlidingTabStrip.setIndicatorWidth(a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorWidth, 0));
        this.mSlidingTabStrip.setIndicatorCornerRadius(a.getFloat(R.styleable.SlidingTabLayout_stl_tabIndicatorWidthRatio, 1.0f));
        this.mSlidingTabStrip.setIndicatorColor(a.getColor(R.styleable.SlidingTabLayout_stl_tabIndicatorColor, Color.TRANSPARENT));
        this.mSlidingTabStrip.setIndicatorDrawable(a.getDrawable(R.styleable.SlidingTabLayout_stl_tabIndicator));
        this.mSlidingTabStrip.setIndicatorCornerRadius(a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorCornerRadius, 0));
        this.mSlidingTabStrip.setIndicatorTopMargin(a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorMarginTop, 0f));
        this.mSlidingTabStrip.setIndicatorBottomMargin(a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorMarginBottom, 0f));
        this.mSlidingTabStrip.setIndicatorGravity(a.getInt(R.styleable.SlidingTabLayout_stl_tabIndicatorGravity, Gravity.BOTTOM));
        this.mSlidingTabStrip.setIsTabTextSelectedBold(a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextSelectedBold, false));
        this.mSlidingTabStrip.setIsTabTextBold(mIsTabTextBold);

        this.mSlidingTabStrip.setmDividerWidth(a.getDimension(R.styleable.SlidingTabLayout_stl_tabDividerWidth, 0));
        this.mSlidingTabStrip.setDividerPadding(a.getDimension(R.styleable.SlidingTabLayout_stl_tabDividerPadding, 0f));
        this.mSlidingTabStrip.setDividerColor(a.getColor(R.styleable.SlidingTabLayout_stl_tabDividerColor, getAlphaColor(Color.BLACK, ((byte) 32))));
        this.mSlidingTabStrip.setShowTabTextScaleAnim(a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextShowScaleAnim, true));
        a.recycle();

        this.addView(mSlidingTabStrip, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mPageChangeListener != null) {
                mViewPager.removeOnPageChangeListener(mPageChangeListener);
            }
            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }

        if (viewPager != null) {
            mViewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            viewPager.addOnPageChangeListener(mPageChangeListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter);
            }

            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener();
            }
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);
        }
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setTabMode(@TabMode int mode) {
        if (mMode != mode) {
            this.mMode = mode;
            setupWithViewPager(mViewPager);
        }
    }

    private void setPagerAdapter(PagerAdapter adapter) {
        if (mViewPager.getAdapter() != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mViewPager.getAdapter().unregisterDataSetObserver(mPagerAdapterObserver);
        }

        if (adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    private void populateFromPagerAdapter() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter == null) {
            return;
        }

        if (mMode == MODE_FIXED) {
            getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        mSlidingTabStrip.removeAllViews();
        TabClickListener listener = new TabClickListener(this);

        for (int i = 0; i < adapter.getCount(); i++) {
            View view = null;
            TextView text = null;
            ImageView image = null;

            if (mTabLayoutRes != 0) {
                view = LayoutInflater.from(this.getContext()).inflate(mTabLayoutRes, mSlidingTabStrip, false);
                text = view.findViewById(R.id.sliding_tab_text);
                image = view.findViewById(R.id.sliding_tab_image);

                if (adapter instanceof SlidingTabPageAdapter && image != null) {
                    Drawable drawable = ((SlidingTabPageAdapter) adapter).getDrawable(i);
                    if (drawable != null) {
                        image.setImageDrawable(drawable);
                    } else {
                        image.setVisibility(GONE);
                    }
                }
            }

            if (text == null && view instanceof TextView) {
                text = (TextView) view;
            }
            if (text == null) {
                text = new TextView(getContext());
            }
            if (view == null) {
                view = text;
            }

            text.setText(adapter.getPageTitle(i));
            view.setOnClickListener(listener);
            setLayoutParams(view, i);
            mSlidingTabStrip.addView(view);
        }

        if (mOnTabCreateListener != null) {
            mOnTabCreateListener.onCreated();
        }
    }

    private int getAlphaColor(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewPager != null) {
            scrollToSelectedTab(mViewPager.getCurrentItem(), 0);

            if (getOnTabSelectedListener() != null) {
                getOnTabSelectedListener().onSelected(mViewPager.getCurrentItem());
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        for (int i = 0; i < mSlidingTabStrip.getChildCount(); i++) {
            setLayoutParams(mSlidingTabStrip.getChildAt(i), i);
        }
    }

    public void setSmoothScroll(boolean smoothScroll) {
        this.mSmoothScroll = smoothScroll;
    }

    public void setTextColor(@ColorInt int color) {
        this.mTabTextColor = color;
        mSlidingTabStrip.setTabText(mTabTextSize, color);
        mSlidingTabStrip.invalidate();
    }

    public void setSelectedTextColor(@ColorInt int color) {
        this.mSelectedTabTextColor = color;
        mSlidingTabStrip.setTabSelectedText(mTabSelectedTextSize, color);
    }

    public void setSelectedTextColors(@ColorInt int... colors) {
        mSlidingTabStrip.setTabSelectedText(mTabSelectedTextSize, colors);
    }

    public void setDividerColors(@ColorInt int... colors) {
        mSlidingTabStrip.setDividerColors(colors);
    }

    public void setCustomTabPalette(TabPalette tabPalette) {
        mSlidingTabStrip.setCustomTabPalette(tabPalette);
    }

    public SlidingTabStrip getSlidingTabStrip() {
        return mSlidingTabStrip;
    }

    public void setOnTabCreatedListener(OnTabCreateListener listener) {
        this.mOnTabCreateListener = listener;
    }

    /**
     * Register a callback to be invoked when selected tab item view is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnSelectedTabClickListener(@Nullable OnSelectedTabClickListener listener) {
        this.mOnSelectedTabClickListener = listener;
    }

    public OnSelectedTabClickListener getOnSelectedTabClickListener() {
        return mOnSelectedTabClickListener;
    }

    /**
     * Register a callback to be invoked when tab item view is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnTabClickListener(@Nullable OnTabClickListener listener) {
        this.mOnTabClickListener = listener;
    }

    public OnTabClickListener getOnTabClickListener() {
        return mOnTabClickListener;
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.mOnTabSelectedListener = listener;
    }

    public OnTabSelectedListener getOnTabSelectedListener() {
        return mOnTabSelectedListener;
    }

    public void setOnColorChangedListener(OnColorChangeListener listener) {
        mSlidingTabStrip.setOnColorChangeListener(listener);
    }

    private void setLayoutParams(View view, int position) {
        view.setPadding(mTabPaddingStart, mTabPaddingTop, mTabPaddingEnd, mTabPaddingBottom);
        TextView text;
        if (view instanceof TextView) {
            text = (TextView) view;
        } else {
            text = view.findViewById(R.id.sliding_tab_text);
        }
        text.setSingleLine(true);
        text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
        text.setTextColor(mTabTextColor);
        text.setTypeface(Typeface.defaultFromStyle(mIsTabTextBold ? Typeface.BOLD : Typeface.NORMAL));
        int tabWidth = mMode == MODE_FIXED ? getWidth() / mViewPager.getAdapter().getCount() : LinearLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(new LinearLayout.LayoutParams(tabWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (position == 0 && mLeftPadding > 0) {
            view.setPadding((int) mLeftPadding + mTabPaddingStart, mTabPaddingTop, mTabPaddingEnd, mTabPaddingBottom);
        }
        if (position == mViewPager.getAdapter().getCount() - 1 && mRightPadding > 0) {
            view.setPadding(mTabPaddingStart, mTabPaddingTop, (int) mRightPadding + mTabPaddingEnd, mTabPaddingBottom);
        }
    }

    static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {

        private final SlidingTabLayout mTabLayout;

        public TabLayoutOnPageChangeListener(SlidingTabLayout tabLayout) {
            this.mTabLayout = tabLayout;
        }

        /**
         * Called when the scroll state changes. Useful for discovering when the user
         * begins dragging, when the pager is automatically settling to the current page,
         * or when it is fully stopped/idle.
         *
         * @param state The new scroll state.
         * @see ViewPager#SCROLL_STATE_IDLE
         * @see ViewPager#SCROLL_STATE_DRAGGING
         * @see ViewPager#SCROLL_STATE_SETTLING
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                mTabLayout.getSlidingTabStrip().setTabSelected(false);
            }
        }

        /**
         * This method will be invoked when the current page is scrolled, either as part
         * of a programmatically initiated smooth scroll or a user initiated touch scroll.
         *
         * @param position             Position index of the first page currently being displayed.
         *                             Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {
            int childCount = mTabLayout.getSlidingTabStrip().getChildCount();
            if (position >= 0 && position < childCount) {
                mTabLayout.getSlidingTabStrip().setFirstPagePosition(position, positionOffset);
                mTabLayout.scrollToSelectedTab(position, positionOffset);
            }
        }

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        @Override
        public void onPageSelected(int position) {
            mTabLayout.getSlidingTabStrip().setTabSelected(true);
            mTabLayout.getSlidingTabStrip().setSelectedPosition(position);

            if (mTabLayout.getOnTabSelectedListener() != null) {
                mTabLayout.getOnTabSelectedListener().onSelected(position);
            }
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter);
            }
        }
    }

    /**
     * Rolling around, to guarantee the selected tab item in the middle as far as possible.
     *
     * @param firstPagePosition the position of the relative item.
     *                          If the position of the currently selected is 1,
     *                          When sliding to the left, the index is 0;
     *                          When sliding to the right, the index is 1;
     * @param positionOffset    the position offset
     */
    void scrollToSelectedTab(int firstPagePosition, float positionOffset) {
        int childCount = mSlidingTabStrip.getChildCount();
        if (childCount == 0) {
            return;
        }

        View firstPageTabView = mSlidingTabStrip.getChildAt(firstPagePosition);
        float offset = firstPageTabView.getWidth() * positionOffset;
        float scrollX = getPaddingLeft() + firstPageTabView.getLeft() + offset - getWidth() / 2;

        float left = 0;
        float right = 0;
        if (firstPagePosition < childCount - 1) {
            // Sliding the page.
            View secondPageTabView = mSlidingTabStrip.getChildAt(firstPagePosition + 1);
            left = firstPageTabView.getLeft() + positionOffset * (secondPageTabView.getLeft() - firstPageTabView.getLeft());
            right = firstPageTabView.getRight() + positionOffset * (secondPageTabView.getRight() - firstPageTabView.getRight());
        } else if (firstPagePosition == childCount - 1) {
            // After selected the last page.
            left = firstPageTabView.getLeft();
            right = firstPageTabView.getRight();
        }
        scrollX += (right - left) / 2;
        scrollTo((int) scrollX, 0);
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }

    class TabClickListener implements View.OnClickListener {
        private final SlidingTabLayout mTabLayout;

        TabClickListener(SlidingTabLayout tabLayout) {
            this.mTabLayout = tabLayout;
        }

        @Override
        public void onClick(View view) {
            for (int i = 0; i < mTabLayout.getSlidingTabStrip().getChildCount(); i++) {
                if (view == mTabLayout.getSlidingTabStrip().getChildAt(i)) {
                    mTabLayout.getSlidingTabStrip().setTabSelected(true);

                    if (mTabLayout.getOnTabClickListener() != null) {
                        mTabLayout.getOnTabClickListener().onClick(i);
                    }

                    if (mTabLayout.getViewPager().getCurrentItem() == i && mTabLayout.getOnSelectedTabClickListener() != null) {
                        mTabLayout.getOnSelectedTabClickListener().onClick(i);
                    }

                    mTabLayout.getViewPager().setCurrentItem(i, mSmoothScroll);
                    break;
                }
            }
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    public static abstract class SlidingTabPageAdapter extends FragmentPagerAdapter {

        public SlidingTabPageAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Returns the specified position icon.
         *
         * @param position
         * @return
         */
        public abstract Drawable getDrawable(int position);
    }

    public interface TabPalette {

        /**
         * Return the specified position text color.
         *
         * @param position
         * @return
         */
        int getTextColor(int position);

        /**
         * Return the specified position divider color.
         *
         * @param position
         * @return
         */
        int getDividerColor(int position);
    }

    /**
     * Interface definition for a callback to be invoked when tabs created.
     */
    public interface OnTabCreateListener {

        /**
         * Called when tabs created.
         */
        void onCreated();
    }

    public interface OnColorChangeListener {

        /**
         * Callback when the color changes.
         *
         * @param color
         */
        void onColorChanged(@ColorInt int color);
    }

    /**
     * Interface definition for a callback to be invoked when a tab view is clicked.
     */
    public interface OnTabClickListener {

        /**
         * Called when a view has been clicked.
         *
         * @param position The position of the view that was clicked.
         */
        void onClick(int position);
    }

    /**
     * Interface definition for a callback to be invoked when a selected tab view is clicked.
     */
    public interface OnSelectedTabClickListener {

        /**
         * Called when a view has been clicked.
         *
         * @param position The position of the view that was clicked.
         */
        void onClick(int position);
    }

    /**
     * Interface definition for a callback to be invoked when a selected tab view is clicked.
     */
    public interface OnTabSelectedListener {

        /**
         * This method will be invoked when a new tab becomes selected.
         *
         * @param position Position index of the new selected tab.
         */
        void onSelected(int position);
    }
}
