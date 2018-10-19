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
package com.kevin.slidingtablayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * SlidingTabStrip
 *
 * @author zwenkai@foxmail.com, Created on 2018-09-29 14:26:35
 *          Major Function：<b>SlidingTabStrip</b>
 *          <p/>
 *          Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
final class SlidingTabStrip extends LinearLayout {

    private float mLeftPadding;
    private float mRightPadding;
    private boolean mIsTabSelected;
    private boolean mIsTabTextBold;
    private boolean mIsTabTextSelectedBold;
    private boolean mShowTabTextScaleAnim;

    private int mFirstPagePosition;
    private float mFirstPagePositionOffset;
    private int mDefaultTabTextColor;
    private float mDefaultTabTextSize;
    private float mSelectedTabTextSize;

    private int mLastSelectedPosition;
    private int mSelectedPosition;

    private boolean mDividerEnabled;
    private float mDividerPadding;
    private final Paint mDividerPaint;

    private boolean mIndicatorEnabled;
    private boolean mIndicatorCreep;
    private float mIndicatorThickness;
    private float mIndicatorWidth;
    private float mIndicatorWidthRatio;
    private int mIndicatorColor;
    private float mIndicatorCornerRadius;
    private float mIndicatorTopMargin;
    private float mIndicatorBottomMargin;
    private int mIndicatorGravity;
    private final Paint mIndicatorPaint;
    private final RectF mIndicatorRectF;

    private AccelerateInterpolator mLeftInterpolator;
    private DecelerateInterpolator mRightInterpolator;
    private SlidingTabLayout.TabPalette mCustomTabPalette;
    private final SimpleTabPalette mTabPalette;
    private SlidingTabLayout.OnColorChangeListener mOnColorChangeListener;

    SlidingTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);

        this.mDefaultTabTextColor = Color.GRAY;
        this.mLastSelectedPosition = -1;
        this.mSelectedPosition = 0;
        this.mLeftInterpolator = new AccelerateInterpolator();
        this.mRightInterpolator = new DecelerateInterpolator();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);
        this.mIndicatorEnabled = a.getBoolean(R.styleable.SlidingTabLayout_indicator_enabled, true);
        this.mIndicatorCreep = a.getBoolean(R.styleable.SlidingTabLayout_indicator_creep, true);
        this.mIndicatorThickness = a.getDimension(R.styleable.SlidingTabLayout_indicator_thickness, Util.dp2px(context, 4));
        this.mIndicatorWidth = a.getDimension(R.styleable.SlidingTabLayout_indicator_width, 0);
        this.mIndicatorWidthRatio = a.getFloat(R.styleable.SlidingTabLayout_indicator_width_ratio, 1.0f);
        this.mIndicatorColor = a.getColor(R.styleable.SlidingTabLayout_indicator_color, Color.TRANSPARENT);
        this.mIndicatorCornerRadius = a.getDimension(R.styleable.SlidingTabLayout_indicator_corner_radius, mIndicatorThickness / 2);
        this.mIndicatorTopMargin = a.getDimension(R.styleable.SlidingTabLayout_indicator_top_margin, 0f);
        this.mIndicatorBottomMargin = a.getDimension(R.styleable.SlidingTabLayout_indicator_bottom_margin, 0f);
        this.mIndicatorGravity = a.getInt(R.styleable.SlidingTabLayout_indicator_gravity, Gravity.BOTTOM);
        this.mIsTabTextSelectedBold = a.getBoolean(R.styleable.SlidingTabLayout_tab_text_selected_bold, false);
        this.mIsTabTextBold = a.getBoolean(R.styleable.SlidingTabLayout_tab_text_bold, false);
        this.mDividerEnabled = a.getBoolean(R.styleable.SlidingTabLayout_divider_enabled, false);
        float dividerThickness = a.getDimension(R.styleable.SlidingTabLayout_divider_thickness, Util.dp2px(context, 1));
        mDividerPadding = a.getDimension(R.styleable.SlidingTabLayout_divider_padding, 0f);
        int dividerColor = a.getColor(R.styleable.SlidingTabLayout_divider_color, getAlphaColor(Color.BLACK, ((byte) 32)));
        this.mShowTabTextScaleAnim = a.getBoolean(R.styleable.SlidingTabLayout_tab_text_show_scale_anim, true);
        a.recycle();

        this.mTabPalette = new SimpleTabPalette();
        this.mTabPalette.setTextColors(Color.DKGRAY);
        this.mTabPalette.setDividerColors(dividerColor);

        this.mIndicatorPaint = new Paint();
        this.mDividerPaint = new Paint();
        this.mDividerPaint.setStrokeWidth(dividerThickness);
        this.mIndicatorRectF = new RectF();
    }

    public void setLeftPadding(float leftPadding) {
        this.mLeftPadding = leftPadding;
    }

    public void setRightPadding(float rightPadding) {
        this.mRightPadding = rightPadding;
    }

    void setTabSelected(boolean mIsTabSelected) {
        this.mIsTabSelected = mIsTabSelected;
    }

    private int getAlphaColor(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int mixColor(int firstColor, int secondColor, float ratio) {
        return Color.argb(
                (int) (Color.alpha(firstColor) * ratio + Color.alpha(secondColor) * (1f - ratio)),
                (int) (Color.red(firstColor) * ratio + Color.red(secondColor) * (1f - ratio)),
                (int) (Color.green(firstColor) * ratio + Color.green(secondColor) * (1f - ratio)),
                (int) (Color.blue(firstColor) * ratio + Color.blue(secondColor) * (1f - ratio))
        );
    }

    /**
     * Set the specified position text size
     *
     * @param index    The specified position
     * @param size     The scaled pixel size
     * @param showAnim Whether show animation
     */
    private void setTabTextSize(int index, final float size, boolean showAnim) {
        if (index < 0 || index >= getChildCount()) {
            return;
        }
        final TextView text = getTextView(index);
        if (showAnim) {
            final ValueAnimator animator = ValueAnimator.ofFloat(new float[]{text.getTextSize(), size});
            animator.setDuration(300L);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator paramAnonymousValueAnimator) {
                    float f = ((Float) paramAnonymousValueAnimator.getAnimatedValue()).floatValue();
                    text.setTextSize(TypedValue.COMPLEX_UNIT_PX, f);
                    if (f == size) {
                        animator.removeUpdateListener(this);
                    }
                }
            });
            animator.start();
        } else {
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    /**
     * Set the specified position text color.
     *
     * @param index The specified position.
     * @param color A color value in the form 0xAARRGGBB.
     */
    private void setTabTextColor(int index, @ColorInt int color) {
        if (index < 0 || index >= getChildCount()) {
            return;
        }
        getTextView(index).setTextColor(color);
    }

    /**
     * Set the specified position text typeface.
     *
     * @param index The specified position.
     * @param tf
     */
    private void setTabTextTypeface(int index, Typeface tf) {
        if (index < 0 || index >= getChildCount()) {
            return;
        }
        getTextView(index).setTypeface(tf);
    }

    private boolean onlySelectedTabBold() {
        return !mIsTabTextBold && mIsTabTextSelectedBold;
    }

    void setDefaultTabText(float titleTextSize, @ColorInt int defaultTabTextColor) {
        this.mDefaultTabTextSize = titleTextSize;
        this.mDefaultTabTextColor = defaultTabTextColor;
        for (int i = 0; i < getChildCount(); i++) {
            if (i != mSelectedPosition) {
                TextView text = getTextView(i);
                text.setTextColor(mDefaultTabTextColor);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.defaultFromStyle(Typeface.NORMAL));
                }

                text.invalidate();
            } else {
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.defaultFromStyle(Typeface.BOLD));
                }
            }

        }
    }

    void setSelectedTabText(float selectedTabTextSize, @ColorInt int... colors) {
        this.mSelectedTabTextSize = selectedTabTextSize;
        if (mShowTabTextScaleAnim) {
            mShowTabTextScaleAnim = mSelectedTabTextSize != mDefaultTabTextSize;
        }

        this.mCustomTabPalette = null;
        this.mTabPalette.setTextColors(colors);
        this.invalidate();

        for (int i = 0; i < getChildCount(); i++) {
            if (i == mSelectedPosition) {
                TextView text = getTextView(i);
                text.setTextColor(colors[mSelectedPosition % colors.length]);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, selectedTabTextSize);
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.defaultFromStyle(Typeface.BOLD));
                }

                text.invalidate();
            } else {
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.defaultFromStyle(Typeface.NORMAL));
                }
            }
        }
    }

    public void setTabTextBold(boolean tabTextBold) {
        this.mIsTabTextBold = tabTextBold;
    }

    void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        this.invalidate();
    }

    void setFirstPagePosition(int position, float positionOffset) {
        this.mFirstPagePosition = position;
        this.mFirstPagePositionOffset = positionOffset;
        this.invalidate();
    }

    void setCustomTabPalette(SlidingTabLayout.TabPalette tabPalette) {
        this.mCustomTabPalette = tabPalette;
        this.invalidate();
    }

    void setDividerColors(int... colors) {
        this.mCustomTabPalette = null;
        this.mTabPalette.setDividerColors(colors);
        this.invalidate();
    }

    /**
     * Get the TextView in the specified location.
     *
     * @param index
     * @return
     */
    private TextView getTextView(int index) {
        View text = getChildAt(index);
        if (!(text instanceof TextView)) {
            text = text.findViewById(R.id.tab_text_id);
        }

        return (TextView) text;
    }

    void setOnColorChangeListener(SlidingTabLayout.OnColorChangeListener listener) {
        this.mOnColorChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        SlidingTabLayout.TabPalette tabPalette = mCustomTabPalette != null ? mCustomTabPalette : mTabPalette;

        if (mLastSelectedPosition != mSelectedPosition) {

            // Set the selected tab text size.
            if (mDefaultTabTextSize != mSelectedTabTextSize) {
                setTabTextSize(mSelectedPosition, mSelectedTabTextSize, mShowTabTextScaleAnim);
                setTabTextSize(mLastSelectedPosition, mDefaultTabTextSize, mShowTabTextScaleAnim);
            }

            // Set the selected tab to bold.
            if (onlySelectedTabBold()) {
                setTabTextTypeface(mSelectedPosition, Typeface.defaultFromStyle(Typeface.BOLD));
                setTabTextTypeface(mLastSelectedPosition, Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            if (mIsTabSelected) {
                setTabTextColor(mSelectedPosition, tabPalette.getTextColor(mSelectedPosition));
                setTabTextColor(mLastSelectedPosition, mDefaultTabTextColor);
            }

            mLastSelectedPosition = mSelectedPosition;
        }

        // Change the text color when slipping page.
        int secondPagePosition = mFirstPagePosition + 1;
        if (!mIsTabSelected) {
            setTabTextColor(mFirstPagePosition, mixColor(mDefaultTabTextColor, tabPalette.getTextColor(mFirstPagePosition), mFirstPagePositionOffset));
            if (mFirstPagePositionOffset > 0f && mFirstPagePosition < getChildCount() - 1) {
                setTabTextColor(secondPagePosition, mixColor(tabPalette.getTextColor(secondPagePosition), mDefaultTabTextColor, mFirstPagePositionOffset));
            }
        }

        // draw divider
        if (mDividerEnabled) {
            // If padding is 0, then the divider is half height of the parent control.
            int dividerHeight = mDividerPadding == 0f ? getHeight() / 2 : (int) (getHeight() - 2 * mDividerPadding);
            for (int i = 0; i < childCount - 1; i++) {
                View childView = getChildAt(i);
                mDividerPaint.setColor(tabPalette.getDividerColor(i));
                canvas.drawLine(childView.getRight(), (getHeight() - dividerHeight) / 2, childView.getRight(), (getHeight() + dividerHeight) / 2, mDividerPaint);
            }
        }

        // draw indicator
        if (mIndicatorEnabled) {
            View firstPageTabView = getChildAt(mFirstPagePosition);
            float firstPageTabLeft = firstPageTabView.getLeft();
            float firstPageTabRight = firstPageTabView.getRight();
            // The first page.
            if (mFirstPagePosition == 0 && mLeftPadding > 0) {
                firstPageTabLeft += mLeftPadding;
            }

            float indicatorRectFLeft = 0;
            float indicatorRectFRight = 0;
            int firstPageTextColor = tabPalette.getTextColor(mFirstPagePosition);

            // Sliding the page.
            if (mFirstPagePosition < getChildCount() - 1) {
                View secondPageTabView = getChildAt(secondPagePosition);
                int secondPageTextColor = tabPalette.getTextColor(secondPagePosition);
                if (firstPageTextColor != secondPageTextColor) {
                    firstPageTextColor = mixColor(secondPageTextColor, firstPageTextColor, mFirstPagePositionOffset);
                }

                float secondPageTabLeft = secondPageTabView.getLeft();
                float secondPageTabRight = secondPageTabView.getRight();
                // Sliding to the last page.
                if (secondPagePosition == getChildCount() - 1) {
                    secondPageTabRight -= mRightPadding;
                }

                if (mIndicatorWidth != 0) {
                    float firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2;
                    firstPageTabLeft = firstMiddle - mIndicatorWidth / 2;
                    firstPageTabRight = firstMiddle + mIndicatorWidth / 2;
                    float secondMiddle = (secondPageTabLeft + secondPageTabRight) / 2;
                    secondPageTabLeft = secondMiddle - mIndicatorWidth / 2;
                    secondPageTabRight = secondMiddle + mIndicatorWidth / 2;
                } else if (mIndicatorWidthRatio > 0 && mIndicatorWidthRatio < 1) {
                    float firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2;
                    firstPageTabLeft = firstMiddle - (firstPageTabRight - firstPageTabLeft) / 2 * mIndicatorWidthRatio;
                    firstPageTabRight = firstMiddle + (firstPageTabRight - firstPageTabLeft) / 2 * mIndicatorWidthRatio;
                    float secondMiddle = (secondPageTabLeft + secondPageTabRight) / 2;
                    secondPageTabLeft = secondMiddle - (secondPageTabRight - secondPageTabLeft) / 2 * mIndicatorWidthRatio;
                    secondPageTabRight = secondMiddle + (secondPageTabRight - secondPageTabLeft) / 2 * mIndicatorWidthRatio;
                }

                if (!mIndicatorCreep) {
                    indicatorRectFLeft = firstPageTabLeft + mFirstPagePositionOffset * (secondPageTabLeft - firstPageTabLeft);
                    indicatorRectFRight = firstPageTabRight + mFirstPagePositionOffset * (secondPageTabRight - firstPageTabRight);
                } else {
                    indicatorRectFLeft = firstPageTabLeft * (1 - mLeftInterpolator.getInterpolation(mFirstPagePositionOffset)) + secondPageTabLeft * mLeftInterpolator.getInterpolation(mFirstPagePositionOffset);
                    indicatorRectFRight = firstPageTabRight * (1 - mRightInterpolator.getInterpolation(mFirstPagePositionOffset)) + secondPageTabRight * mRightInterpolator.getInterpolation(mFirstPagePositionOffset);
                }
            } else if (mFirstPagePosition == getChildCount() - 1) {
                // After selected the last page.
                firstPageTabRight -= mRightPadding;

                if (mIndicatorWidth != 0) {
                    float firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2;
                    firstPageTabLeft = firstMiddle - mIndicatorWidth / 2;
                    firstPageTabRight = firstMiddle + mIndicatorWidth / 2;
                } else if (mIndicatorWidthRatio > 0 && mIndicatorWidthRatio < 1) {
                    float firstMiddle = (firstPageTabLeft + firstPageTabRight) / 2;
                    firstPageTabLeft = firstMiddle - (firstPageTabRight - firstPageTabLeft) / 2 * mIndicatorWidthRatio;
                    firstPageTabRight = firstMiddle + (firstPageTabRight - firstPageTabLeft) / 2 * mIndicatorWidthRatio;
                }
                indicatorRectFLeft = firstPageTabLeft;
                indicatorRectFRight = firstPageTabRight;
            }

            int indicatorColor = mIndicatorColor == 0 ? firstPageTextColor : mIndicatorColor;
            mIndicatorPaint.setColor(indicatorColor);

            switch (mIndicatorGravity) {
                case Gravity.TOP:
                    mIndicatorRectF.set(indicatorRectFLeft, mIndicatorTopMargin, indicatorRectFRight, mIndicatorTopMargin + mIndicatorThickness);
                    break;
                case Gravity.CENTER:
                    mIndicatorRectF.set(indicatorRectFLeft, (getHeight() - mIndicatorThickness) / 2, indicatorRectFRight, (getHeight() + mIndicatorThickness) / 2);
                    break;
                case Gravity.BOTTOM:
                    mIndicatorRectF.set(indicatorRectFLeft, getHeight() - mIndicatorThickness - mIndicatorBottomMargin, indicatorRectFRight, getHeight() - mIndicatorBottomMargin);
                    break;
            }

            canvas.drawRoundRect(mIndicatorRectF, mIndicatorCornerRadius, mIndicatorCornerRadius, mIndicatorPaint);

            if (mOnColorChangeListener != null) {
                mOnColorChangeListener.onColorChanged(firstPageTextColor);
            }
        }

    }

    class SimpleTabPalette implements SlidingTabLayout.TabPalette {
        private int[] mTextColors;
        private int[] mDividerColors;

        public void setTextColors(@ColorInt int... colors) {
            this.mTextColors = colors;
        }

        @Override
        public final int getTextColor(int index) {
            return mTextColors[index % mTextColors.length];
        }

        public void setDividerColors(@ColorInt int... colors) {
            this.mDividerColors = colors;
        }

        @Override
        public final int getDividerColor(int index) {
            return mDividerColors[index % mDividerColors.length];
        }
    }
}
