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
package com.kevin.slidingtab

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import kotlin.math.roundToInt

/**
 * SlidingTabLayout
 *
 * @author zwenkai@foxmail.com, Created on 2018-09-29 14:24:57
 * Major Function：<b>SlidingTabLayout</b>
 *
 *
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class SlidingTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {
    @IntDef(MODE_SCROLLABLE, MODE_FIXED)
    annotation class TabMode

    @TabMode
    private var mode: Int
    private val leftPadding: Float
    private val rightPadding: Float
    private val tabLayoutRes: Int
    private var isTabTextBold: Boolean
    private var tabPaddingStart: Int
    private var tabPaddingTop: Int
    private var tabPaddingEnd: Int
    private var tabPaddingBottom: Int
    private var tabTextColor: Int
    private var selectedTabTextColor: Int
    private val tabTextSize: Int
    private val tabSelectedTextSize: Int
    private var smoothScroll: Boolean

    private var viewPager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null
    private val slidingTabStrip: SlidingTabStrip

    private var pageChangeListener: TabLayoutOnPageChangeListener? = null
    private var adapterChangeListener: AdapterChangeListener? = null
    private var pagerAdapterObserver: DataSetObserver? = null

    private var onTabCreateListener: OnTabCreateListener? = null
    private var onTabClickListener: OnTabClickListener? = null
    private var onSelectedTabClickListener: OnSelectedTabClickListener? = null
    private var onTabSelectedListener: OnTabSelectedListener? = null

    init {
        this.isHorizontalScrollBarEnabled = false
        this.isFillViewport = true
        slidingTabStrip = SlidingTabStrip(context)
        val a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout)
        mode = a.getInt(R.styleable.SlidingTabLayout_stl_tabMode, MODE_FIXED)
        leftPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_leftPadding, 0f)
        rightPadding = a.getDimension(R.styleable.SlidingTabLayout_stl_rightPadding, 0f)
        smoothScroll = a.getBoolean(R.styleable.SlidingTabLayout_stl_smoothScroll, true)
        tabLayoutRes = a.getResourceId(R.styleable.SlidingTabLayout_stl_tabLayout, 0)
        tabPaddingBottom = a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPadding, 0)
        tabPaddingEnd = tabPaddingBottom
        tabPaddingTop = tabPaddingEnd
        tabPaddingStart = tabPaddingTop
        tabPaddingStart = a.getDimensionPixelSize(
            R.styleable.SlidingTabLayout_stl_tabPaddingStart, tabPaddingStart
        )
        tabPaddingTop =
            a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPaddingTop, tabPaddingTop)
        tabPaddingEnd =
            a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabPaddingEnd, tabPaddingEnd)
        tabPaddingBottom = a.getDimensionPixelSize(
            R.styleable.SlidingTabLayout_stl_tabPaddingBottom, tabPaddingBottom
        )
        tabTextSize =
            a.getDimensionPixelSize(R.styleable.SlidingTabLayout_stl_tabTextSize, dpToPx(16))
        tabSelectedTextSize = a.getDimensionPixelSize(
            R.styleable.SlidingTabLayout_stl_tabSelectedTextSize, tabTextSize
        )
        tabTextColor = a.getColor(R.styleable.SlidingTabLayout_stl_tabTextColor, Color.GRAY)
        selectedTabTextColor =
            a.getColor(R.styleable.SlidingTabLayout_stl_tabSelectedTextColor, Color.DKGRAY)
        isTabTextBold = a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextBold, false)
        slidingTabStrip.gravity =
            a.getInt(R.styleable.SlidingTabLayout_stl_tabGravity, Gravity.CENTER_VERTICAL)
        slidingTabStrip.setLeftPadding(leftPadding)
        slidingTabStrip.setRightPadding(rightPadding)
        slidingTabStrip.setTabText(tabTextSize.toFloat(), tabTextColor)
        slidingTabStrip.setTabSelectedText(tabSelectedTextSize.toFloat(), selectedTabTextColor)
        slidingTabStrip.setIndicatorCreep(
            a.getBoolean(R.styleable.SlidingTabLayout_stl_tabIndicatorCreep, false)
        )
        slidingTabStrip.setIndicatorHeight(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorHeight, 0f)
        )
        slidingTabStrip.setIndicatorWidth(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorWidth, 0f)
        )
        slidingTabStrip.setIndicatorWidthRatio(
            a.getFloat(R.styleable.SlidingTabLayout_stl_tabIndicatorWidthRatio, 1.0f)
        )
        slidingTabStrip.setIndicatorColor(
            a.getColor(R.styleable.SlidingTabLayout_stl_tabIndicatorColor, Color.TRANSPARENT)
        )
        slidingTabStrip.setIndicatorDrawable(a.getDrawable(R.styleable.SlidingTabLayout_stl_tabIndicator))
        slidingTabStrip.setIndicatorCornerRadius(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorCornerRadius, 0f)
        )
        slidingTabStrip.setIndicatorTopMargin(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorMarginTop, 0f)
        )
        slidingTabStrip.setIndicatorBottomMargin(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabIndicatorMarginBottom, 0f)
        )
        slidingTabStrip.setIndicatorGravity(
            a.getInt(R.styleable.SlidingTabLayout_stl_tabIndicatorGravity, Gravity.BOTTOM)
        )
        slidingTabStrip.setTabTextSelectedBold(
            a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextSelectedBold, false)
        )
        slidingTabStrip.setTabTextBold(isTabTextBold)
        slidingTabStrip.setDividerWidth(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabDividerWidth, 0f)
        )
        slidingTabStrip.setDividerPadding(
            a.getDimension(R.styleable.SlidingTabLayout_stl_tabDividerPadding, 0f)
        )
        slidingTabStrip.setDividerColor(
            a.getColor(
                R.styleable.SlidingTabLayout_stl_tabDividerColor,
                getAlphaColor(Color.BLACK, 32.toByte())
            )
        )
        slidingTabStrip.setShowTabTextScaleAnim(
            a.getBoolean(R.styleable.SlidingTabLayout_stl_tabTextShowScaleAnim, true)
        )
        a.recycle()
        this.addView(
            slidingTabStrip,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun setupWithViewPager(viewPager: ViewPager?) {
        this.viewPager?.let {
            // If we've already been setup with a ViewPager, remove us from it
            pageChangeListener?.apply {
                it.removeOnPageChangeListener(this)
            }
            adapterChangeListener?.apply {
                it.removeOnAdapterChangeListener(this)
            }
        }

        if (viewPager != null) {
            this.viewPager = viewPager

            // Add our custom OnPageChangeListener to the ViewPager
            if (pageChangeListener == null) {
                pageChangeListener = TabLayoutOnPageChangeListener(this)
            }
            viewPager.addOnPageChangeListener(pageChangeListener!!)

            val  adapter = viewPager.adapter
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter)
            }

            // Add a listener so that we're notified of any adapter changes
            if (adapterChangeListener == null) {
                adapterChangeListener = AdapterChangeListener()
            }
            viewPager.addOnAdapterChangeListener(adapterChangeListener!!)
        }
    }

    fun getViewPager(): ViewPager? {
        return viewPager
    }

    fun setTabMode(@TabMode mode: Int) {
        if (this.mode != mode) {
            this.mode = mode
            setupWithViewPager(viewPager)
        }
    }

    @TabMode
    fun getTabMode(): Int {
        return mode
    }

    fun getTabLayoutRes(): Int {
        return tabLayoutRes
    }

    fun setTabTextBold(mIsTabTextBold: Boolean) {
        this.isTabTextBold = mIsTabTextBold
    }

    private fun setPagerAdapter(adapter: PagerAdapter?) {
        if (pagerAdapter != null && pagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            pagerAdapter!!.unregisterDataSetObserver(pagerAdapterObserver!!)
        }
        pagerAdapter = adapter
        if (adapter != null) {
            // Register our observer on the new adapter
            if (pagerAdapterObserver == null) {
                pagerAdapterObserver = PagerAdapterObserver()
            }
            adapter.registerDataSetObserver(pagerAdapterObserver!!)
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter()
    }

    private fun populateFromPagerAdapter() {
        if (viewPager == null) {
            return
        }
        val adapter = viewPager!!.adapter ?: return
        slidingTabStrip.reset()
        val listener: TabClickListener = TabClickListener(this)
        for (i in 0 until adapter.count) {
            var view: View? = null
            var textView: TextView? = null
            var iconView: ImageView? = null
            if (tabLayoutRes != 0) {
                view =
                    LayoutInflater.from(this.context).inflate(tabLayoutRes, slidingTabStrip, false)
                textView = view.findViewById(R.id.sliding_tab_text)
                iconView = view.findViewById(R.id.sliding_tab_icon)
                if (textView != null && textView.typeface != null) {
                    isTabTextBold = textView.typeface.isBold
                    slidingTabStrip.setTabTextBold(isTabTextBold)
                }
                if (adapter is SlidingTabPageAdapter && iconView != null) {
                    val drawable = adapter.getDrawable(i)
                    if (drawable != null) {
                        iconView.setImageDrawable(drawable)
                    } else {
                        iconView.visibility = GONE
                    }
                }
            }
            if (textView == null && view is TextView) {
                textView = view
            }
            if (textView == null) {
                textView = TextView(context)
            }
            if (view == null) {
                view = textView
            }
            textView.text = adapter.getPageTitle(i)
            view.setOnClickListener(listener)
            setLayoutParams(view, i, adapter.count)
            slidingTabStrip.addView(view)
        }

        // Make sure we reflect the currently set ViewPager item
        if (viewPager != null && pagerAdapter != null && pagerAdapter!!.count > 0) {
            val curItem = viewPager!!.currentItem
            if (curItem != slidingTabStrip.getSelectedPosition()) {
                slidingTabStrip.setTabSelected(true)
                slidingTabStrip.setSelectedPosition(curItem)
            }
        }
        onTabCreateListener?.onCreated()
    }

    private fun getAlphaColor(color: Int, alpha: Byte): Int {
        return Color.argb(alpha.toInt(), Color.red(color), Color.green(color), Color.blue(color))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (viewPager != null) {
            scrollToSelectedTab(viewPager!!.currentItem, 0f)
            if (onTabSelectedListener != null) {
                onTabSelectedListener!!.onSelected(viewPager!!.currentItem)
            }
        }
    }

    fun setSmoothScroll(smoothScroll: Boolean) {
        this.smoothScroll = smoothScroll
    }

    fun getSmoothScroll(): Boolean {
        return smoothScroll
    }

    fun setTextColor(@ColorInt color: Int) {
        tabTextColor = color
        slidingTabStrip.setTabText(tabTextSize.toFloat(), color)
        slidingTabStrip.invalidate()
    }

    fun setSelectedTextColor(@ColorInt color: Int) {
        selectedTabTextColor = color
        slidingTabStrip.setTabSelectedText(tabSelectedTextSize.toFloat(), color)
    }

    fun setSelectedTextColors(@ColorInt vararg colors: Int) {
        slidingTabStrip.setTabSelectedText(tabSelectedTextSize.toFloat(), *colors)
    }

    fun setDividerColors(@ColorInt vararg colors: Int) {
        slidingTabStrip.setDividerColors(*colors)
    }

    fun setCustomTabPalette(tabPalette: TabPalette) {
        slidingTabStrip.setCustomTabPalette(tabPalette)
    }

    internal fun getSlidingTabStrip(): SlidingTabStrip {
        return slidingTabStrip
    }

    fun getTabAt(index: Int): View {
        return slidingTabStrip.getChildAt(index)
    }

    fun setOnTabCreatedListener(listener: OnTabCreateListener) {
        this.onTabCreateListener = listener
    }

    fun getOnTabCreatedListener(): OnTabCreateListener? {
        return onTabCreateListener
    }

    /**
     * Register a callback to be invoked when selected tab item view is clicked.
     *
     * @param listener The callback that will run
     */
    fun setOnSelectedTabClickListener(listener: OnSelectedTabClickListener) {
        this.onSelectedTabClickListener = listener
    }

    fun getOnSelectedTabClickListener(): OnSelectedTabClickListener? {
        return onSelectedTabClickListener
    }

    /**
     * Register a callback to be invoked when tab item view is clicked.
     *
     * @param listener The callback that will run
     */
    fun setOnTabClickListener(listener: OnTabClickListener) {
        this.onTabClickListener = listener
    }

    fun getOnTabClickListener(): OnTabClickListener? {
        return onTabClickListener
    }

    fun setOnTabSelectedListener(listener: OnTabSelectedListener?) {
        this.onTabSelectedListener = listener
    }

    fun getOnTabSelectedListener(): OnTabSelectedListener? {
        return onTabSelectedListener
    }

    fun setOnColorChangedListener(listener: OnColorChangeListener?) {
        slidingTabStrip.setOnColorChangeListener(listener)
    }

    fun setLayoutParams(view: View, position: Int, count: Int) {
        view.setPadding(tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom)
        val text: TextView = if (view is TextView) {
            view
        } else {
            view.findViewById(R.id.sliding_tab_text)
        }
        text.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize.toFloat())
        text.setTextColor(tabTextColor)
        text.typeface =
            Typeface.create(text.typeface, if (isTabTextBold) Typeface.BOLD else Typeface.NORMAL)
        val layoutParams = if (mode == MODE_FIXED) {
            LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        } else {
            LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.0f)
        }
        view.layoutParams = layoutParams
        if (position == 0 && leftPadding > 0) {
            view.setPadding(
                leftPadding.toInt() + tabPaddingStart,
                tabPaddingTop,
                tabPaddingEnd,
                tabPaddingBottom
            )
        }
        if (position == count - 1 && rightPadding > 0) {
            view.setPadding(
                tabPaddingStart,
                tabPaddingTop,
                rightPadding.toInt() + tabPaddingEnd,
                tabPaddingBottom
            )
        }
    }

    internal class TabLayoutOnPageChangeListener(private val mTabLayout: SlidingTabLayout) :
        OnPageChangeListener {
        /**
         * Called when the scroll state changes. Useful for discovering when the user
         * begins dragging, when the pager is automatically settling to the current page,
         * or when it is fully stopped/idle.
         *
         * @param state The new scroll state.
         * @see ViewPager.SCROLL_STATE_IDLE
         *
         * @see ViewPager.SCROLL_STATE_DRAGGING
         *
         * @see ViewPager.SCROLL_STATE_SETTLING
         */
        override fun onPageScrollStateChanged(state: Int) {
            mTabLayout.slidingTabStrip.setTabSelected(state == ViewPager.SCROLL_STATE_IDLE)
        }

        /**
         * This method will be invoked when the current page is scrolled, either as part
         * of a programmatically initiated smooth scroll or a user initiated touch scroll.
         *
         * @param position             Position index of the first page currently being displayed.
         * Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            @Px positionOffsetPixels: Int
        ) {
            val childCount = mTabLayout.slidingTabStrip.childCount
            if (position in 0 until childCount) {
                mTabLayout.slidingTabStrip.setFirstPagePosition(position, positionOffset)
                mTabLayout.scrollToSelectedTab(position, positionOffset)
            }
        }

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        override fun onPageSelected(position: Int) {
            mTabLayout.slidingTabStrip.setTabSelected(true)
            mTabLayout.slidingTabStrip.setSelectedPosition(position)
            mTabLayout.onTabSelectedListener?.onSelected(position)
        }
    }

    private inner class AdapterChangeListener : OnAdapterChangeListener {
        override fun onAdapterChanged(
            viewPager: ViewPager,
            oldAdapter: PagerAdapter?,
            newAdapter: PagerAdapter?
        ) {
            if (this@SlidingTabLayout.viewPager === viewPager) {
                setPagerAdapter(newAdapter)
            }
        }
    }

    /**
     * Rolling around, to guarantee the selected tab item in the middle as far as possible.
     *
     * @param firstPagePosition the position of the relative item.
     * If the position of the currently selected is 1,
     * When sliding to the left, the index is 0;
     * When sliding to the right, the index is 1;
     * @param positionOffset    the position offset
     */
    fun scrollToSelectedTab(firstPagePosition: Int, positionOffset: Float) {
        val childCount = slidingTabStrip.childCount
        if (childCount == 0) {
            return
        }
        val firstPageTabView = slidingTabStrip.getChildAt(firstPagePosition)
        val offset = firstPageTabView.width * positionOffset
        var scrollX = paddingLeft + firstPageTabView.left + offset - width / 2
        var left = 0f
        var right = 0f
        if (firstPagePosition < childCount - 1) {
            // Sliding the page.
            val secondPageTabView = slidingTabStrip.getChildAt(firstPagePosition + 1)
            left =
                firstPageTabView.left + positionOffset * (secondPageTabView.left - firstPageTabView.left)
            right =
                firstPageTabView.right + positionOffset * (secondPageTabView.right - firstPageTabView.right)
        } else if (firstPagePosition == childCount - 1) {
            // After selected the last page.
            left = firstPageTabView.left.toFloat()
            right = firstPageTabView.right.toFloat()
        }
        scrollX += (right - left) / 2
        scrollTo(scrollX.toInt(), 0)
    }

    private fun dpToPx(dps: Int): Int {
        return (resources.displayMetrics.density * dps).roundToInt()
    }

    private inner class TabClickListener constructor(private val mTabLayout: SlidingTabLayout) :
        OnClickListener {
        override fun onClick(view: View) {
            for (i in 0 until mTabLayout.slidingTabStrip.childCount) {
                if (view === mTabLayout.slidingTabStrip.getChildAt(i)) {
                    mTabLayout.slidingTabStrip.setTabSelected(true)
                    mTabLayout.onTabClickListener?.onClick(i)
                    val viewPager = mTabLayout.viewPager
                    if (viewPager != null && viewPager.currentItem == i) {
                        mTabLayout.onSelectedTabClickListener?.onClick(i)
                    }
                    viewPager?.setCurrentItem(i, smoothScroll)
                    break
                }
            }
        }
    }

    private inner class PagerAdapterObserver : DataSetObserver() {
        override fun onChanged() {
            populateFromPagerAdapter()
        }

        override fun onInvalidated() {
            populateFromPagerAdapter()
        }
    }

    abstract class SlidingTabPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        /**
         * Returns the specified position icon.
         *
         * @param position
         * @return
         */
        abstract fun getDrawable(position: Int): Drawable?
    }

    interface TabPalette {
        /**
         * Return the specified position text color.
         *
         * @param position
         * @return
         */
        fun getTextColor(position: Int): Int

        /**
         * Return the specified position divider color.
         *
         * @param position
         * @return
         */
        fun getDividerColor(position: Int): Int
    }

    /**
     * Interface definition for a callback to be invoked when tabs created.
     */
    interface OnTabCreateListener {
        /**
         * Called when tabs created.
         */
        fun onCreated()
    }

    interface OnColorChangeListener {
        /**
         * Callback when the color changes.
         *
         * @param color
         */
        fun onColorChanged(@ColorInt color: Int)
    }

    /**
     * Interface definition for a callback to be invoked when a tab view is clicked.
     */
    interface OnTabClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param position The position of the view that was clicked.
         */
        fun onClick(position: Int)
    }

    /**
     * Interface definition for a callback to be invoked when a selected tab view is clicked.
     */
    interface OnSelectedTabClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param position The position of the view that was clicked.
         */
        fun onClick(position: Int)
    }

    /**
     * Interface definition for a callback to be invoked when a selected tab view is clicked.
     */
    interface OnTabSelectedListener {
        /**
         * This method will be invoked when a new tab becomes selected.
         *
         * @param position Position index of the new selected tab.
         */
        fun onSelected(position: Int)
    }

    companion object {
        private const val MODE_SCROLLABLE = 0
        private const val MODE_FIXED = 1
    }
}