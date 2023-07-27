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

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.kevin.slidingtab.SlidingTabLayout.OnColorChangeListener
import com.kevin.slidingtab.SlidingTabLayout.TabPalette

/**
 * SlidingTabStrip
 *
 * @author zwenkai@foxmail.com, Created on 2018-09-29 14:26:35
 * Major Function：**SlidingTabStrip**
 *
 *
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
internal class SlidingTabStrip(context: Context) : LinearLayout(context) {
    private var leftPadding = 0f
    private var rightPadding = 0f
    private var isTabSelected: Boolean
    private var isTabTextBold = false
    private var isTabTextSelectedBold = false
    private var showTabTextScaleAnim = false
    private var firstPagePosition = 0
    private var firstPagePositionOffset = 0f
    private var tabTextColor: Int
    private var tabTextSize = 0f
    private var selectedTabTextSize = 0f
    private var lastSelectedPosition: Int
    private var selectedPosition: Int
    private var dividerWidth = 0f
    private var dividerPadding = 0f
    private val dividerPaint: Paint
    private var indicatorCreep = false
    private var indicatorHeight = 0f
    private var indicatorWidth = 0f
    private var indicatorWidthRatio = 0f
    private var indicatorColor = 0
    private var indicatorDrawable: Drawable? = null
    private var indicatorCornerRadius = 0f
    private var indicatorTopMargin = 0f
    private var indicatorBottomMargin = 0f
    private var indicatorGravity = 0
    private val indicatorPaint: Paint
    private val indicatorRectF: RectF
    private val leftInterpolator: AccelerateInterpolator
    private val rightInterpolator: DecelerateInterpolator
    private var customTabPalette: TabPalette? = null
    private val tabPalette: SimpleTabPalette
    private var onColorChangeListener: OnColorChangeListener? = null

    init {
        setWillNotDraw(false)
        tabTextColor = Color.GRAY
        lastSelectedPosition = -1
        selectedPosition = 0
        isTabSelected = true
        leftInterpolator = AccelerateInterpolator()
        rightInterpolator = DecelerateInterpolator()
        tabPalette = SimpleTabPalette()
        tabPalette.setTextColors(Color.DKGRAY)
        indicatorPaint = Paint()
        dividerPaint = Paint()
        dividerPaint.strokeWidth = dividerWidth
        indicatorRectF = RectF()
    }

    fun setLeftPadding(leftPadding: Float) {
        this.leftPadding = leftPadding
    }

    fun setRightPadding(rightPadding: Float) {
        this.rightPadding = rightPadding
    }

    fun setIndicatorCreep(creep: Boolean) {
        indicatorCreep = creep
    }

    fun setIndicatorHeight(height: Float) {
        indicatorHeight = height
    }

    fun setIndicatorWidth(width: Float) {
        indicatorWidth = width
    }

    fun setIndicatorWidthRatio(widthRatio: Float) {
        indicatorWidthRatio = widthRatio
    }

    fun setIndicatorColor(color: Int) {
        indicatorColor = color
    }

    fun setIndicatorDrawable(drawable: Drawable?) {
        indicatorDrawable = drawable
    }

    fun setIndicatorCornerRadius(cornerRadius: Float) {
        indicatorCornerRadius = cornerRadius
    }

    fun setIndicatorTopMargin(topMargin: Float) {
        indicatorTopMargin = topMargin
    }

    fun setIndicatorBottomMargin(bottomMargin: Float) {
        indicatorBottomMargin = bottomMargin
    }

    fun setIndicatorGravity(gravity: Int) {
        indicatorGravity = gravity
    }

    fun setTabTextSelectedBold(selectedBold: Boolean) {
        isTabTextSelectedBold = selectedBold
    }

    fun setDividerWidth(width: Float) {
        dividerWidth = width
    }

    fun setDividerPadding(padding: Float) {
        dividerPadding = padding
    }

    fun setDividerColor(color: Int) {
        tabPalette.setDividerColors(color)
    }

    fun setShowTabTextScaleAnim(scaleAnim: Boolean) {
        showTabTextScaleAnim = scaleAnim
    }

    fun setTabSelected(mIsTabSelected: Boolean) {
        this.isTabSelected = mIsTabSelected
    }

    private fun mixColor(firstColor: Int, secondColor: Int, ratio: Float): Int {
        return Color.argb(
            (Color.alpha(firstColor) * ratio + Color.alpha(secondColor) * (1f - ratio)).toInt(),
            (Color.red(firstColor) * ratio + Color.red(secondColor) * (1f - ratio)).toInt(),
            (Color.green(firstColor) * ratio + Color.green(secondColor) * (1f - ratio)).toInt(),
            (Color.blue(firstColor) * ratio + Color.blue(secondColor) * (1f - ratio)).toInt()
        )
    }

    /**
     * Set the specified position text size
     *
     * @param index    The specified position
     * @param size     The scaled pixel size
     * @param showAnim Whether show animation
     */
    private fun setTabTextSize(index: Int, size: Float, showAnim: Boolean) {
        if (index < 0 || index >= childCount) {
            return
        }
        val text = getTextView(index)
        if (showAnim) {
            val animator = ValueAnimator.ofFloat(text.textSize, size)
            animator.duration = ANIMATION_DURATION.toLong()
            animator.addUpdateListener(object : AnimatorUpdateListener {
                override fun onAnimationUpdate(paramAnonymousValueAnimator: ValueAnimator) {
                    val f = paramAnonymousValueAnimator.animatedValue as Float
                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX, f)
                    if (f == size) {
                        animator.removeUpdateListener(this)
                    }
                }
            })
            animator.start()
        } else {
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }
    }

    /**
     * Changes the selection state of the tab view.
     *
     * @param index    The specified position.
     * @param selected true if the view must be selected, false otherwise
     */
    private fun setTabTextSelected(index: Int, selected: Boolean) {
        if (index < 0 || index >= childCount) {
            return
        }
        val text = getTextView(index)
        text.isSelected = selected
    }

    /**
     * Set the specified position text color.
     *
     * @param index The specified position.
     * @param color A color value in the form 0xAARRGGBB.
     */
    private fun setTabTextColor(index: Int, @ColorInt color: Int) {
        if (index < 0 || index >= childCount) {
            return
        }
        getTextView(index).setTextColor(color)
    }

    /**
     * Set the specified position text typeface style.
     *
     * @param index The specified position.
     * @param style
     */
    private fun setTabTextTypeface(index: Int, style: Int) {
        if (index < 0 || index >= childCount) {
            return
        }
        val text = getTextView(index)
        text.typeface = Typeface.create(text.typeface, style)
    }

    private fun onlySelectedTabBold(): Boolean {
        return !isTabTextBold && isTabTextSelectedBold
    }

    fun setTabText(titleTextSize: Float, @ColorInt defaultTabTextColor: Int) {
        tabTextSize = titleTextSize
        tabTextColor = defaultTabTextColor
        for (i in 0 until childCount) {
            if (i != selectedPosition) {
                val text = getTextView(i)
                text.setTextColor(tabTextColor)
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
                if (onlySelectedTabBold() && lastSelectedPosition != -1) {
                    setTabTextTypeface(lastSelectedPosition, Typeface.NORMAL)
                }
                text.invalidate()
            } else {
                if (onlySelectedTabBold() && lastSelectedPosition != -1) {
                    setTabTextTypeface(lastSelectedPosition, Typeface.BOLD)
                }
            }
        }
    }

    fun setTabSelectedText(selectedTabTextSize: Float, @ColorInt vararg colors: Int) {
        this.selectedTabTextSize = selectedTabTextSize
        if (showTabTextScaleAnim) {
            showTabTextScaleAnim = this.selectedTabTextSize != tabTextSize
        }
        customTabPalette = null
        tabPalette.setTextColors(*colors)
        this.invalidate()
        for (i in 0 until childCount) {
            if (i == selectedPosition) {
                val text = getTextView(i)
                text.setTextColor(colors[selectedPosition % colors.size])
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedTabTextSize)
                if (onlySelectedTabBold() && lastSelectedPosition != -1) {
                    setTabTextTypeface(lastSelectedPosition, Typeface.BOLD)
                }
                text.invalidate()
            } else {
                if (onlySelectedTabBold() && lastSelectedPosition != -1) {
                    setTabTextTypeface(lastSelectedPosition, Typeface.NORMAL)
                }
            }
        }
    }

    fun setTabTextBold(tabTextBold: Boolean) {
        isTabTextBold = tabTextBold
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    fun setSelectedPosition(position: Int) {
        this.selectedPosition = position
        this.invalidate()
    }

    fun setFirstPagePosition(position: Int, positionOffset: Float) {
        firstPagePosition = position
        firstPagePositionOffset = positionOffset
        this.invalidate()
    }

    fun setCustomTabPalette(tabPalette: TabPalette) {
        customTabPalette = tabPalette
        this.invalidate()
    }

    fun setDividerColors(@ColorInt vararg colors: Int) {
        customTabPalette = null
        tabPalette.setDividerColors(*colors)
        this.invalidate()
    }

    /**
     * Get the TextView in the specified location.
     *
     * @param index
     * @return
     */
    private fun getTextView(index: Int): TextView {
        var view = getChildAt(index)
        if (view !is TextView) {
            view = view.findViewById(R.id.sliding_tab_text)
        }
        return view as TextView
    }

    fun setOnColorChangeListener(listener: OnColorChangeListener?) {
        onColorChangeListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        val childCount = childCount
        if (childCount == 0) {
            return
        }
        val tabPalette = customTabPalette ?: tabPalette
        if (lastSelectedPosition != selectedPosition) {

            // Set the selected tab text size.
            if (tabTextSize != selectedTabTextSize) {
                setTabTextSize(selectedPosition, selectedTabTextSize, showTabTextScaleAnim)
                setTabTextSize(lastSelectedPosition, tabTextSize, showTabTextScaleAnim)
            }

            // Set the selected tab to bold.
            if (onlySelectedTabBold()) {
                setTabTextTypeface(selectedPosition, Typeface.BOLD)
                setTabTextTypeface(lastSelectedPosition, Typeface.NORMAL)
            }
            if (isTabSelected) {
                setTabTextColor(selectedPosition, tabPalette.getTextColor(selectedPosition))
                setTabTextColor(lastSelectedPosition, tabTextColor)
                setTabTextSelected(selectedPosition, true)
                setTabTextSelected(lastSelectedPosition, false)
            }
            lastSelectedPosition = selectedPosition
        }

        // Change the text color when slipping page.
        val secondPagePosition = firstPagePosition + 1
        if (!isTabSelected) {
            setTabTextColor(
                firstPagePosition,
                mixColor(
                    tabTextColor,
                    tabPalette.getTextColor(firstPagePosition),
                    firstPagePositionOffset
                )
            )
            if (firstPagePositionOffset > 0f && firstPagePosition < getChildCount() - 1) {
                setTabTextColor(
                    secondPagePosition,
                    mixColor(
                        tabPalette.getTextColor(secondPagePosition),
                        tabTextColor,
                        firstPagePositionOffset
                    )
                )
            }
        }

        // draw divider
        if (dividerWidth > 0) {
            // If padding is 0, then the divider is half height of the parent control.
            val dividerHeight =
                if (dividerPadding == 0f) height / 2 else (height - 2 * dividerPadding).toInt()
            for (i in 0 until childCount - 1) {
                val childView = getChildAt(i)
                dividerPaint.color = tabPalette.getDividerColor(i)
                canvas.drawLine(
                    childView.right.toFloat(), (height - dividerHeight).toFloat() / 2,
                    childView.right.toFloat(), (height + dividerHeight).toFloat() / 2,
                    dividerPaint
                )
            }
        }

        // draw indicator
        if (indicatorHeight > 0) {
            val firstPageTabView = getChildAt(firstPagePosition)
            var firstPageTabLeft = firstPageTabView.left.toFloat()
            var firstPageTabRight = firstPageTabView.right.toFloat()
            // The first page.
            if (firstPagePosition == 0 && leftPadding > 0) {
                firstPageTabLeft += leftPadding
            }
            var indicatorRectFLeft = 0f
            var indicatorRectFRight = 0f
            var firstPageTextColor = tabPalette.getTextColor(firstPagePosition)

            // Sliding the page.
            if (firstPagePosition < getChildCount() - 1) {
                val secondPageTabView = getChildAt(secondPagePosition)
                val secondPageTextColor = tabPalette.getTextColor(secondPagePosition)
                if (firstPageTextColor != secondPageTextColor) {
                    firstPageTextColor =
                        mixColor(secondPageTextColor, firstPageTextColor, firstPagePositionOffset)
                }
                var secondPageTabLeft = secondPageTabView.left.toFloat()
                var secondPageTabRight = secondPageTabView.right.toFloat()
                // Sliding to the last page.
                if (secondPagePosition == getChildCount() - 1) {
                    secondPageTabRight -= rightPadding
                }
                if (indicatorWidth != 0f) {
                    val firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2
                    firstPageTabLeft = firstMiddle - indicatorWidth / 2
                    firstPageTabRight = firstMiddle + indicatorWidth / 2
                    val secondMiddle = (secondPageTabLeft + secondPageTabRight) / 2
                    secondPageTabLeft = secondMiddle - indicatorWidth / 2
                    secondPageTabRight = secondMiddle + indicatorWidth / 2
                } else if (indicatorWidthRatio > 0 && indicatorWidthRatio < 1) {
                    val firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2
                    firstPageTabLeft =
                        firstMiddle - (firstPageTabRight - firstPageTabLeft) / 2 * indicatorWidthRatio
                    firstPageTabRight =
                        firstMiddle + (firstPageTabRight - firstPageTabLeft) / 2 * indicatorWidthRatio
                    val secondMiddle = (secondPageTabLeft + secondPageTabRight) / 2
                    secondPageTabLeft =
                        secondMiddle - (secondPageTabRight - secondPageTabLeft) / 2 * indicatorWidthRatio
                    secondPageTabRight =
                        secondMiddle + (secondPageTabRight - secondPageTabLeft) / 2 * indicatorWidthRatio
                }
                if (!indicatorCreep) {
                    indicatorRectFLeft =
                        firstPageTabLeft + firstPagePositionOffset * (secondPageTabLeft - firstPageTabLeft)
                    indicatorRectFRight =
                        firstPageTabRight + firstPagePositionOffset * (secondPageTabRight - firstPageTabRight)
                } else {
                    indicatorRectFLeft = firstPageTabLeft * (1 - leftInterpolator.getInterpolation(
                        firstPagePositionOffset
                    )) + secondPageTabLeft * leftInterpolator.getInterpolation(
                        firstPagePositionOffset
                    )
                    indicatorRectFRight =
                        firstPageTabRight * (1 - rightInterpolator.getInterpolation(
                            firstPagePositionOffset
                        )) + secondPageTabRight * rightInterpolator.getInterpolation(
                            firstPagePositionOffset
                        )
                }
            } else if (firstPagePosition == getChildCount() - 1) {
                // After selected the last page.
                firstPageTabRight -= rightPadding
                if (indicatorWidth != 0f) {
                    val firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2
                    firstPageTabLeft = firstMiddle - indicatorWidth / 2
                    firstPageTabRight = firstMiddle + indicatorWidth / 2
                } else if (indicatorWidthRatio > 0 && indicatorWidthRatio < 1) {
                    val firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2
                    firstPageTabLeft =
                        firstMiddle - (firstPageTabRight - firstPageTabLeft) / 2 * indicatorWidthRatio
                    firstPageTabRight =
                        firstMiddle + (firstPageTabRight - firstPageTabLeft) / 2 * indicatorWidthRatio
                }
                indicatorRectFLeft = firstPageTabLeft
                indicatorRectFRight = firstPageTabRight
            }
            val indicatorColor = if (indicatorColor == 0) firstPageTextColor else indicatorColor
            indicatorPaint.color = indicatorColor
            when (indicatorGravity) {
                Gravity.TOP -> {
                    indicatorRectF[indicatorRectFLeft, indicatorTopMargin, indicatorRectFRight] =
                        indicatorTopMargin + indicatorHeight
                }

                Gravity.CENTER -> {
                    indicatorRectF[indicatorRectFLeft, (height - indicatorHeight) / 2, indicatorRectFRight] =
                        (height + indicatorHeight) / 2
                }

                Gravity.BOTTOM -> {
                    indicatorRectF[indicatorRectFLeft, height - indicatorHeight - indicatorBottomMargin, indicatorRectFRight] =
                        height - indicatorBottomMargin
                }

                else -> {
                    // Can`t reach;
                }
            }

            if (indicatorDrawable != null) {
                indicatorDrawable!!.setBounds(
                    indicatorRectF.left.toInt(),
                    indicatorRectF.top.toInt(),
                    indicatorRectF.right.toInt(),
                    indicatorRectF.bottom.toInt()
                )
                indicatorDrawable!!.draw(canvas)
            } else {
                canvas.drawRoundRect(
                    indicatorRectF,
                    indicatorCornerRadius,
                    indicatorCornerRadius,
                    indicatorPaint
                )
            }
        }

        // Callback the change color.
        onColorChangeListener?.let {
            var firstPageTextColor = tabPalette.getTextColor(firstPagePosition)
            // Sliding the page.
            if (firstPagePosition < getChildCount() - 1) {
                val secondPageTextColor = tabPalette.getTextColor(secondPagePosition)
                if (firstPageTextColor != secondPageTextColor) {
                    firstPageTextColor =
                        mixColor(secondPageTextColor, firstPageTextColor, firstPagePositionOffset)
                }
            }
            it.onColorChanged(firstPageTextColor)
        }
    }

    fun reset() {
        removeAllViews()
        lastSelectedPosition = -1
        selectedPosition = 0
        isTabSelected = true
    }

    private class SimpleTabPalette : TabPalette {
        private lateinit var textColors: IntArray
        private lateinit var dividerColors: IntArray

        fun setTextColors(@ColorInt vararg colors: Int) {
            textColors = colors
        }

        override fun getTextColor(position: Int): Int {
            return textColors[position % textColors.size]
        }

        fun setDividerColors(@ColorInt vararg colors: Int) {
            dividerColors = colors
        }

        override fun getDividerColor(position: Int): Int {
            return dividerColors[position % dividerColors.size]
        }
    }

    companion object {
        private const val ANIMATION_DURATION = 300
    }
}