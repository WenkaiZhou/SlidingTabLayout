package com.kevin.slidingtab

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kevin.slidingtab.SlidingTabLayout.OnTabSelectedListener
import java.lang.ref.WeakReference

/**
 * SlidingTabLayoutMediator
 *
 * @author zwenkai@foxmail.com, Created on 2023-06-29 13:36:47
 * Major Function：**A mediator to link a TabLayout with a ViewPager2.**
 *
 *
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class SlidingTabLayoutMediator @JvmOverloads constructor(
    private val tabLayout: SlidingTabLayout,
    private val viewPager: ViewPager2,
    smoothScroll: Boolean = true
) {
    private var adapter: SlidingTabPageAdapter? = null

    /**
     * Returns whether the [SlidingTabLayout] and the [ViewPager2] are linked together.
     */
    private var isAttached = false

    private var onPageChangeCallback: TabLayoutOnPageChangeCallback? = null
    private var onTabSelectedListener: OnTabSelectedListener? = null
    private var pagerAdapterObserver: RecyclerView.AdapterDataObserver? = null

    init {
        tabLayout.setSmoothScroll(smoothScroll)
    }

    /**
     * Link the TabLayout and the ViewPager2 together. Must be called after ViewPager2 has an adapter
     * set. To be called on a new instance of TabLayoutMediator or if the ViewPager2's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the ViewPager2 has no
     * adapter.
     */
    fun attach() {
        check(!isAttached) { "TabLayoutMediator is already attached" }
        adapter = viewPager.adapter as SlidingTabPageAdapter?
        checkNotNull(adapter) { "TabLayoutMediator attached before ViewPager2 has an " + "adapter" }
        isAttached = true

        // Add our custom OnPageChangeCallback to the ViewPager
        onPageChangeCallback = TabLayoutOnPageChangeCallback(tabLayout)
        viewPager.registerOnPageChangeCallback(onPageChangeCallback!!)

        // Now we'll add a tab selected listener to set ViewPager's current item
        onTabSelectedListener =
            ViewPagerOnTabSelectedListener(viewPager, tabLayout.getSmoothScroll())
        tabLayout.setOnTabSelectedListener(onTabSelectedListener)

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled

        // Register our observer on the new adapter
        pagerAdapterObserver = PagerAdapterObserver()
        adapter!!.registerAdapterDataObserver(pagerAdapterObserver!!)
        populateTabsFromPagerAdapter()

        // Now update the scroll position to match the ViewPager's current item
//        tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    }

    /**
     * Unlink the TabLayout and the ViewPager. To be called on a stale TabLayoutMediator if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before [.attach] when a ViewPager2's adapter is changed.
     */
    fun detach() {
        if (adapter != null) {
            adapter!!.unregisterAdapterDataObserver(pagerAdapterObserver!!)
            pagerAdapterObserver = null
        }
        tabLayout.setOnTabSelectedListener(null)
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback!!)
        onTabSelectedListener = null
        onPageChangeCallback = null
        adapter = null
        isAttached = false
    }

    fun populateTabsFromPagerAdapter() {
        if (adapter == null) {
            return
        }
        tabLayout.getSlidingTabStrip().reset()
        val listener = TabClickListener(tabLayout)
        for (i in 0 until adapter!!.itemCount) {
            var view: View? = null
            var text: TextView? = null
            var image: ImageView? = null
            if (tabLayout.getTabLayoutRes() != 0) {
                view = LayoutInflater.from(tabLayout.context)
                    .inflate(tabLayout.getTabLayoutRes(), tabLayout.getSlidingTabStrip(), false)
                text = view.findViewById(R.id.sliding_tab_text)
                image = view.findViewById(R.id.sliding_tab_icon)
                if (text != null && text.typeface != null) {
                    val isTabTextBold = text.typeface.isBold
                    tabLayout.getSlidingTabStrip().setTabTextBold(isTabTextBold)
                    tabLayout.setTabTextBold(isTabTextBold)
                }
                if (image != null) {
                    val drawable = adapter!!.getDrawable(i)
                    if (drawable != null) {
                        image.setImageDrawable(drawable)
                    } else {
                        image.visibility = View.GONE
                    }
                }
            }
            if (text == null && view is TextView) {
                text = view
            }
            if (text == null) {
                text = TextView(tabLayout.context)
            }
            if (view == null) {
                view = text
            }
            text.text = adapter!!.getPageTitle(i)
            view.setOnClickListener(listener)
            tabLayout.setLayoutParams(view, i, adapter!!.itemCount)
            tabLayout.getSlidingTabStrip().addView(view)
        }

        // Make sure we reflect the currently set ViewPager item
        if (adapter != null && adapter!!.itemCount > 0) {
            val curItem = viewPager.currentItem
            if (curItem != tabLayout.getSlidingTabStrip().getSelectedPosition()) {
                tabLayout.getSlidingTabStrip().setTabSelected(true)
                tabLayout.getSlidingTabStrip().setSelectedPosition(curItem)
            }
        }
        tabLayout.getOnTabCreatedListener()?.onCreated()
    }

    /**
     * A [ViewPager2.OnPageChangeCallback] class which contains the necessary calls back to the
     * provided [SlidingTabLayout] so that the tab position is kept in sync.
     *
     *
     * This class stores the provided TabLayout weakly, meaning that you can use [ ][ViewPager2.registerOnPageChangeCallback] without removing the
     * callback and not cause a leak.
     */
    private class TabLayoutOnPageChangeCallback internal constructor(tabLayout: SlidingTabLayout) :
        OnPageChangeCallback() {
        private val tabLayoutRef: WeakReference<SlidingTabLayout>

        init {
            tabLayoutRef = WeakReference(tabLayout)
        }

        override fun onPageScrollStateChanged(state: Int) {
            val tabLayout = tabLayoutRef.get()
            tabLayout?.getSlidingTabStrip()?.setTabSelected(state == ViewPager2.SCROLL_STATE_IDLE)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            val tabLayout = tabLayoutRef.get()
            if (tabLayout != null) {
                val childCount: Int = tabLayout.getSlidingTabStrip().childCount
                if (position in 0 until childCount) {
                    tabLayout.getSlidingTabStrip().setFirstPagePosition(position, positionOffset)
                    tabLayout.scrollToSelectedTab(position, positionOffset)
                }
            }
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = tabLayoutRef.get()
            if (tabLayout != null) {
                tabLayout.getSlidingTabStrip().setTabSelected(true)
                tabLayout.getSlidingTabStrip().setSelectedPosition(position)
                tabLayout.getOnTabSelectedListener()?.onSelected(position)
            }
        }
    }

    /**
     * A [SlidingTabLayout.OnTabSelectedListener] class which contains the necessary calls back to the
     * provided [ViewPager2] so that the tab position is kept in sync.
     */
    private class ViewPagerOnTabSelectedListener internal constructor(
        private val viewPager: ViewPager2,
        private val smoothScroll: Boolean
    ) : OnTabSelectedListener {
        override fun onSelected(position: Int) {
            viewPager.setCurrentItem(position, smoothScroll)
        }
    }

    private inner class PagerAdapterObserver internal constructor() :
        RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }
    }

    abstract class SlidingTabPageAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        constructor(fragmentActivity: FragmentActivity) : this(
            fragmentActivity.supportFragmentManager,
            fragmentActivity.lifecycle
        )

        constructor(fragment: Fragment) : this(fragment.childFragmentManager, fragment.lifecycle)

        /**
         * Returns the specified position icon.
         *
         * @param position
         * @return
         */
        fun getDrawable(position: Int): Drawable? {
            return null
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
        abstract fun getPageTitle(position: Int): CharSequence?
    }

    private inner class TabClickListener constructor(private val mTabLayout: SlidingTabLayout) :
        View.OnClickListener {
        override fun onClick(view: View) {
            for (i in 0 until mTabLayout.getSlidingTabStrip().childCount) {
                if (view === mTabLayout.getSlidingTabStrip().getChildAt(i)) {
                    mTabLayout.getSlidingTabStrip().setTabSelected(true)
                    mTabLayout.getOnTabClickListener()?.onClick(i)
                    if (viewPager.currentItem == i) {
                        mTabLayout.getOnSelectedTabClickListener()?.onClick(i)
                    }
                    viewPager.setCurrentItem(i, tabLayout.getSmoothScroll())
                    break
                }
            }
        }
    }
}