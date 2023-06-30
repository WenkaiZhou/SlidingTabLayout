package com.kevin.slidingtab;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.ref.WeakReference;

/**
 * SlidingTabLayoutMediator
 *
 * @author zwenkai@foxmail.com, Created on 2023-06-29 13:36:47
 * Major Function：<b>A mediator to link a TabLayout with a ViewPager2.</b>
 * <p/>
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
public class SlidingTabLayoutMediator {

    @NonNull
    private final SlidingTabLayout tabLayout;
    @NonNull
    private final ViewPager2 viewPager;
    @Nullable
    private SlidingTabPageAdapter adapter;
    private boolean attached;

    @Nullable
    private TabLayoutOnPageChangeCallback onPageChangeCallback;
    @Nullable
    private SlidingTabLayout.OnTabSelectedListener onTabSelectedListener;
    @Nullable
    private RecyclerView.AdapterDataObserver pagerAdapterObserver;

    public SlidingTabLayoutMediator(
            @NonNull SlidingTabLayout tabLayout,
            @NonNull ViewPager2 viewPager) {
        this(tabLayout, viewPager, true);
    }

    public SlidingTabLayoutMediator(
            @NonNull SlidingTabLayout tabLayout,
            @NonNull ViewPager2 viewPager,
            boolean smoothScroll) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
        tabLayout.setSmoothScroll(smoothScroll);
    }

    /**
     * Link the TabLayout and the ViewPager2 together. Must be called after ViewPager2 has an adapter
     * set. To be called on a new instance of TabLayoutMediator or if the ViewPager2's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the ViewPager2 has no
     *                               adapter.
     */
    public void attach() {
        if (attached) {
            throw new IllegalStateException("TabLayoutMediator is already attached");
        }
        adapter = (SlidingTabPageAdapter) viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException(
                    "TabLayoutMediator attached before ViewPager2 has an " + "adapter");
        }
        attached = true;

        // Add our custom OnPageChangeCallback to the ViewPager
        onPageChangeCallback = new TabLayoutOnPageChangeCallback(tabLayout);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);

        // Now we'll add a tab selected listener to set ViewPager's current item
        onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager, tabLayout.getSmoothScroll());
        tabLayout.setOnTabSelectedListener(onTabSelectedListener);

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled

        // Register our observer on the new adapter
        pagerAdapterObserver = new PagerAdapterObserver();
        adapter.registerAdapterDataObserver(pagerAdapterObserver);

        populateTabsFromPagerAdapter();

        // Now update the scroll position to match the ViewPager's current item
//        tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    }

    /**
     * Unlink the TabLayout and the ViewPager. To be called on a stale TabLayoutMediator if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before {@link #attach()} when a ViewPager2's adapter is changed.
     */
    public void detach() {
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(pagerAdapterObserver);
            pagerAdapterObserver = null;
        }
        tabLayout.setOnTabSelectedListener(null);
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        onTabSelectedListener = null;
        onPageChangeCallback = null;
        adapter = null;
        attached = false;
    }

    /**
     * Returns whether the {@link SlidingTabLayout} and the {@link ViewPager2} are linked together.
     */
    public boolean isAttached() {
        return attached;
    }

    void populateTabsFromPagerAdapter() {
        if (adapter == null) {
            return;
        }

        tabLayout.getSlidingTabStrip().reset();

        TabClickListener listener = new TabClickListener(tabLayout);

        for (int i = 0; i < adapter.getItemCount(); i++) {
            View view = null;
            TextView text = null;
            ImageView image = null;

            if (tabLayout.getTabLayoutRes() != 0) {
                view = LayoutInflater.from(tabLayout.getContext()).inflate(tabLayout.getTabLayoutRes(), tabLayout.getSlidingTabStrip(), false);
                text = view.findViewById(R.id.sliding_tab_text);
                image = view.findViewById(R.id.sliding_tab_icon);
                if (text != null && text.getTypeface() != null) {
                    boolean isTabTextBold = text.getTypeface().isBold();
                    tabLayout.getSlidingTabStrip().setTabTextBold(isTabTextBold);
                    tabLayout.setTabTextBold(isTabTextBold);
                }

                if (image != null) {
                    Drawable drawable = adapter.getDrawable(i);
                    if (drawable != null) {
                        image.setImageDrawable(drawable);
                    } else {
                        image.setVisibility(View.GONE);
                    }
                }
            }

            if (text == null && view instanceof TextView) {
                text = (TextView) view;
            }
            if (text == null) {
                text = new TextView(tabLayout.getContext());
            }
            if (view == null) {
                view = text;
            }

            text.setText(adapter.getPageTitle(i));
            view.setOnClickListener(listener);
            tabLayout.setLayoutParams(view, i, adapter.getItemCount());
            tabLayout.getSlidingTabStrip().addView(view);
        }

        // Make sure we reflect the currently set ViewPager item
        if (adapter != null && adapter.getItemCount() > 0) {
            final int curItem = viewPager.getCurrentItem();
            if (curItem != tabLayout.getSlidingTabStrip().getSelectedPosition()) {
                tabLayout.getSlidingTabStrip().setTabSelected(true);
                tabLayout.getSlidingTabStrip().setSelectedPosition(curItem);
            }
        }

        if (tabLayout.getOnTabCreatedListener() != null) {
            tabLayout.getOnTabCreatedListener().onCreated();
        }
    }

    /**
     * A {@link ViewPager2.OnPageChangeCallback} class which contains the necessary calls back to the
     * provided {@link SlidingTabLayout} so that the tab position is kept in sync.
     *
     * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
     * ViewPager2#registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback)} without removing the
     * callback and not cause a leak.
     */
    private static class TabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @NonNull
        private final WeakReference<SlidingTabLayout> tabLayoutRef;

        TabLayoutOnPageChangeCallback(SlidingTabLayout tabLayout) {
            tabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            SlidingTabLayout tabLayout = tabLayoutRef.get();
            if (state != SCROLL_STATE_IDLE) {
                tabLayout.getSlidingTabStrip().setTabSelected(false);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            SlidingTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null) {
                int childCount = tabLayout.getSlidingTabStrip().getChildCount();
                if (position >= 0 && position < childCount) {
                    tabLayout.getSlidingTabStrip().setFirstPagePosition(position, positionOffset);
                    tabLayout.scrollToSelectedTab(position, positionOffset);
                }
            }
        }

        @Override
        public void onPageSelected(final int position) {
            SlidingTabLayout tabLayout = tabLayoutRef.get();
            if (tabLayout != null) {
                tabLayout.getSlidingTabStrip().setTabSelected(true);
                tabLayout.getSlidingTabStrip().setSelectedPosition(position);

                if (tabLayout.getOnTabSelectedListener() != null) {
                    tabLayout.getOnTabSelectedListener().onSelected(position);
                }
            }
        }
    }

    /**
     * A {@link SlidingTabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
     * provided {@link ViewPager2} so that the tab position is kept in sync.
     */
    private static class ViewPagerOnTabSelectedListener implements SlidingTabLayout.OnTabSelectedListener {
        private final ViewPager2 viewPager;
        private final boolean smoothScroll;

        ViewPagerOnTabSelectedListener(ViewPager2 viewPager, boolean smoothScroll) {
            this.viewPager = viewPager;
            this.smoothScroll = smoothScroll;
        }

        @Override
        public void onSelected(int position) {
            viewPager.setCurrentItem(position, smoothScroll);
        }
    }

    private class PagerAdapterObserver extends RecyclerView.AdapterDataObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            populateTabsFromPagerAdapter();
        }
    }

    public static abstract class SlidingTabPageAdapter extends FragmentStateAdapter {

        public SlidingTabPageAdapter(@NonNull FragmentActivity fragmentActivity) {
            this(fragmentActivity.getSupportFragmentManager(), fragmentActivity.getLifecycle());
        }

        public SlidingTabPageAdapter(@NonNull Fragment fragment) {
            this(fragment.getChildFragmentManager(), fragment.getLifecycle());
        }

        public SlidingTabPageAdapter(@NonNull FragmentManager fragmentManager,
                                     @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        /**
         * Returns the specified position icon.
         *
         * @param position
         * @return
         */
        @Nullable
        public Drawable getDrawable(int position) {
            return null;
        }

        /**
         * This method may be called by the ViewPager2 to obtain a title string
         * to describe the specified page. This method may return null
         * indicating no title for this page. The default implementation returns
         * null.
         *
         * @param position The position of the title requested
         * @return A title for the requested page
         */
        public abstract CharSequence getPageTitle(int position);
    }

    private class TabClickListener implements View.OnClickListener {
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

                    if (viewPager.getCurrentItem() == i && mTabLayout.getOnSelectedTabClickListener() != null) {
                        mTabLayout.getOnSelectedTabClickListener().onClick(i);
                    }

                    viewPager.setCurrentItem(i, tabLayout.getSmoothScroll());
                    break;
                }
            }
        }
    }
}
