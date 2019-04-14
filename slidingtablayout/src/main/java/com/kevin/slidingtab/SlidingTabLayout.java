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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
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

    private float mLeftPadding;
    private float mRightPadding;
    private int mTabLayoutRes;
    private boolean mIsTabTextBold;
    private boolean mIsTabHorizontalAverage;
    private float mTabHorizontalPadding;
    private int mTabVerticalGravity;
    private int mDefaultTabTextColor;
    private int mSelectedTabTextColor;
    private int mDefaultTabTextSize;
    private int mSelectedTabTextSize;
    private boolean mSmoothScroll;

    private ViewPager mViewPager;
    private SlidingTabStrip mSlidingTabStrip;

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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);
        this.mLeftPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_leftPadding, 0);
        this.mRightPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_rightPadding, 0);
        this.mSmoothScroll = a.getBoolean(R.styleable.SlidingTabLayout_stl_smoothScroll, true);
        this.mTabLayoutRes = a.getResourceId(R.styleable.SlidingTabLayout_stl_tabLayout, 0);
        this.mSmoothScroll = a.getBoolean(R.styleable.SlidingTabLayout_stl_smoothScroll, true);
        this.mIsTabHorizontalAverage = a.getBoolean(R.styleable.SlidingTabLayout_stl_tabHorizontalAverage, false);
        this.mTabHorizontalPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_tabHorizontalPadding, Util.dp2px(context, 8));
        this.mTabVerticalGravity = a.getInt(R.styleable.SlidingTabLayout_stl_tabVerticalGravity, Gravity.CENTER_VERTICAL);
        this.mDefaultTabTextSize = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabTextSizeDefault, Util.sp2px(context, 16));
        this.mSelectedTabTextSize = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabTextSizeSelected, mDefaultTabTextSize);
        this.mDefaultTabTextColor = a.getColor(R.styleable.SlidingTabLayout_stl_tabTextColorDefault, Color.GRAY);
        this.mSelectedTabTextColor = a.getColor(R.styleable.SlidingTabLayout_stl_tabTextColorSelected, Color.DKGRAY);
        this.mIsTabTextBold = a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextBold, false);
        a.recycle();

        this.mSlidingTabStrip = new SlidingTabStrip(context, attrs);
        this.mSlidingTabStrip.setGravity(mTabVerticalGravity);
        this.mSlidingTabStrip.setLeftPadding(mLeftPadding);
        this.mSlidingTabStrip.setRightPadding(mRightPadding);
        this.mSlidingTabStrip.setTabTextBold(mIsTabTextBold);
        this.mSlidingTabStrip.setDefaultTabText(mDefaultTabTextSize, mDefaultTabTextColor);
        this.mSlidingTabStrip.setSelectedTabText(mSelectedTabTextSize, mSelectedTabTextColor);
        this.addView(mSlidingTabStrip, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setViewPager(@NonNull ViewPager viewPager) {
        if (viewPager == null) {
            throw new IllegalStateException("The viewPager is null!");
        }

        this.mViewPager = viewPager;
        if (mIsTabHorizontalAverage) {
            getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        mViewPager.addOnPageChangeListener(new InternalViewPagerListener(this));
        initTabLayout();
        if (mOnTabCreateListener != null) {
            mOnTabCreateListener.onCreated();
        }
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    private void initTabLayout() {
        mSlidingTabStrip.removeAllViews();
        PagerAdapter adapter = mViewPager.getAdapter();
        TabClickListener listener = new TabClickListener(this);

        for (int i = 0; i < adapter.getCount(); i++) {
            View view = null;
            TextView text = null;
            ImageView image = null;

            if (mTabLayoutRes != 0) {
                view = LayoutInflater.from(this.getContext()).inflate(mTabLayoutRes, mSlidingTabStrip, false);
                text = view.findViewById(R.id.tab_text_id);
                image = view.findViewById(R.id.tab_img_id);

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

        int width = getWidth() / mViewPager.getAdapter().getCount();
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        for (int i = 0; i < mSlidingTabStrip.getChildCount(); i++) {
            mSlidingTabStrip.getChildAt(i).setLayoutParams(new LinearLayout.LayoutParams(width, height));
        }
    }

    public void setSmoothScroll(boolean smoothScroll) {
        this.mSmoothScroll = smoothScroll;
    }

    public void setDefaultTextColor(@ColorInt int color) {
        this.mDefaultTabTextColor = color;
        mSlidingTabStrip.setDefaultTabText(mDefaultTabTextSize, color);
        mSlidingTabStrip.invalidate();
    }

    public void setSelectedTextColor(@ColorInt int color) {
        this.mSelectedTabTextColor = color;
        mSlidingTabStrip.setSelectedTabText(mSelectedTabTextSize, color);
    }

    public void setSelectedTextColors(@ColorInt int... colors) {
        mSlidingTabStrip.setSelectedTabText(((float) mSelectedTabTextSize), colors);
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
        view.setPadding((int) mTabHorizontalPadding, 0, (int) mTabHorizontalPadding, 0);
        TextView text;
        if (view instanceof TextView) {
            text = (TextView) view;
        } else {
            text = view.findViewById(R.id.tab_text_id);
        }
        text.setSingleLine(true);
        text.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDefaultTabTextSize);
        text.setTextColor(mDefaultTabTextColor);
        text.setTypeface(Typeface.defaultFromStyle(mIsTabTextBold ? Typeface.BOLD : Typeface.NORMAL));
        int tabWidth = mIsTabHorizontalAverage ? getWidth() / mViewPager.getAdapter().getCount() : LinearLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(new LinearLayout.LayoutParams(tabWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

        int horizontalPadding = (int) mTabHorizontalPadding;
        if (position == 0 && mLeftPadding > 0) {
            view.setPadding((int) mLeftPadding + horizontalPadding, 0, horizontalPadding, 0);
        }
        if (position == mViewPager.getAdapter().getCount() - 1 && mRightPadding > 0) {
            view.setPadding(horizontalPadding, 0, (int) mRightPadding + horizontalPadding, 0);
        }
    }

    static class InternalViewPagerListener implements ViewPager.OnPageChangeListener {

        private final SlidingTabLayout mTabLayout;

        public InternalViewPagerListener(SlidingTabLayout tabLayout) {
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
            if (childCount != 0 && position >= 0 && position < childCount) {
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
        if (firstPagePosition < childCount - 1) { // Sliding the page.
            View secondPageTabView = mSlidingTabStrip.getChildAt(firstPagePosition + 1);
            left = firstPageTabView.getLeft() + positionOffset * (secondPageTabView.getLeft() - firstPageTabView.getLeft());
            right = firstPageTabView.getRight() + positionOffset * (secondPageTabView.getRight() - firstPageTabView.getRight());
        } else if (firstPagePosition == childCount - 1) { // After selected the last page.
            left = firstPageTabView.getLeft();
            right = firstPageTabView.getRight();
        }
        scrollX += (right - left) / 2;
        scrollTo((int) scrollX, 0);
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
