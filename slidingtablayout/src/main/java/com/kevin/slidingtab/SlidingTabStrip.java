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

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
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

    private static final int ANIMATION_DURATION = 300;

    private float mLeftPadding;
    private float mRightPadding;
    private boolean mIsTabSelected;
    private boolean mIsTabTextBold;
    private boolean mIsTabTextSelectedBold;
    private boolean mShowTabTextScaleAnim;

    private int mFirstPagePosition;
    private float mFirstPagePositionOffset;
    private int mTabTextColor;
    private float mTabTextSize;
    private float mSelectedTabTextSize;

    private int mLastSelectedPosition;
    private int mSelectedPosition;

    private float mDividerWidth;
    private float mDividerPadding;
    private final Paint mDividerPaint;

    private boolean mIndicatorCreep;
    private float mIndicatorHeight;
    private float mIndicatorWidth;
    private float mIndicatorWidthRatio;
    private int mIndicatorColor;
    private Drawable mIndicatorDrawable;
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

    SlidingTabStrip(Context context) {
        super(context);
        this.setWillNotDraw(false);

        this.mTabTextColor = Color.GRAY;
        this.mLastSelectedPosition = -1;
        this.mSelectedPosition = 0;
        this.mLeftInterpolator = new AccelerateInterpolator();
        this.mRightInterpolator = new DecelerateInterpolator();

        this.mTabPalette = new SimpleTabPalette();
        this.mTabPalette.setTextColors(Color.DKGRAY);

        this.mIndicatorPaint = new Paint();
        this.mDividerPaint = new Paint();
        this.mDividerPaint.setStrokeWidth(mDividerWidth);
        this.mIndicatorRectF = new RectF();
    }

    void setLeftPadding(float leftPadding) {
        this.mLeftPadding = leftPadding;
    }

    void setRightPadding(float rightPadding) {
        this.mRightPadding = rightPadding;
    }

    void setIndicatorCreep(boolean creep) {
        this.mIndicatorCreep = creep;
    }

    void setIndicatorHeight(float height) {
        this.mIndicatorHeight = height;
    }

    void setIndicatorWidth(float width) {
        this.mIndicatorWidth = width;
    }

    void setIndicatorWidthRatio(float widthRatio) {
        this.mIndicatorWidthRatio = widthRatio;
    }

    void setIndicatorColor(int color) {
        this.mIndicatorColor = color;
    }

    void setIndicatorDrawable(Drawable drawable) {
        this.mIndicatorDrawable = drawable;
    }

    void setIndicatorCornerRadius(float cornerRadius) {
        this.mIndicatorCornerRadius = cornerRadius;
    }

    void setIndicatorTopMargin(float topMargin) {
        this.mIndicatorTopMargin = topMargin;
    }

    void setIndicatorBottomMargin(float bottomMargin) {
        this.mIndicatorBottomMargin = bottomMargin;
    }

    void setIndicatorGravity(int gravity) {
        this.mIndicatorGravity = gravity;
    }

    void setTabTextSelectedBold(boolean selectedBold) {
        this.mIsTabTextSelectedBold = selectedBold;
    }

    void setDividerWidth(float width) {
        this.mDividerWidth = width;
    }

    void setDividerPadding(float padding) {
        this.mDividerPadding = padding;
    }

    void setDividerColor(int color) {
        this.mTabPalette.setDividerColors(color);
    }

    void setShowTabTextScaleAnim(boolean scaleAnim) {
        this.mShowTabTextScaleAnim = scaleAnim;
    }

    void setTabSelected(boolean mIsTabSelected) {
        this.mIsTabSelected = mIsTabSelected;
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
            final ValueAnimator animator = ValueAnimator.ofFloat(text.getTextSize(), size);
            animator.setDuration(ANIMATION_DURATION);
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
     * Set the specified position text typeface style.
     *
     * @param index The specified position.
     * @param style
     */
    private void setTabTextTypeface(int index, int style) {
        if (index < 0 || index >= getChildCount()) {
            return;
        }
        TextView text = getTextView(index);
        text.setTypeface(Typeface.create(text.getTypeface(), style));
    }

    private boolean onlySelectedTabBold() {
        return !mIsTabTextBold && mIsTabTextSelectedBold;
    }

    void setTabText(float titleTextSize, @ColorInt int defaultTabTextColor) {
        this.mTabTextSize = titleTextSize;
        this.mTabTextColor = defaultTabTextColor;
        for (int i = 0; i < getChildCount(); i++) {
            if (i != mSelectedPosition) {
                TextView text = getTextView(i);
                text.setTextColor(mTabTextColor);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.NORMAL);
                }

                text.invalidate();
            } else {
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.BOLD);
                }
            }

        }
    }

    void setTabSelectedText(float selectedTabTextSize, @ColorInt int... colors) {
        this.mSelectedTabTextSize = selectedTabTextSize;
        if (mShowTabTextScaleAnim) {
            mShowTabTextScaleAnim = mSelectedTabTextSize != mTabTextSize;
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
                    setTabTextTypeface(mLastSelectedPosition, Typeface.BOLD);
                }

                text.invalidate();
            } else {
                if (onlySelectedTabBold() && mLastSelectedPosition != -1) {
                    setTabTextTypeface(mLastSelectedPosition, Typeface.NORMAL);
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

    void setDividerColors(@ColorInt int... colors) {
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
            text = text.findViewById(R.id.sliding_tab_text);
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
            if (mTabTextSize != mSelectedTabTextSize) {
                setTabTextSize(mSelectedPosition, mSelectedTabTextSize, mShowTabTextScaleAnim);
                setTabTextSize(mLastSelectedPosition, mTabTextSize, mShowTabTextScaleAnim);
            }

            // Set the selected tab to bold.
            if (onlySelectedTabBold()) {
                setTabTextTypeface(mSelectedPosition, Typeface.BOLD);
                setTabTextTypeface(mLastSelectedPosition, Typeface.NORMAL);
            }

            if (mIsTabSelected) {
                setTabTextColor(mSelectedPosition, tabPalette.getTextColor(mSelectedPosition));
                setTabTextColor(mLastSelectedPosition, mTabTextColor);
            }

            mLastSelectedPosition = mSelectedPosition;
        }

        // Change the text color when slipping page.
        int secondPagePosition = mFirstPagePosition + 1;
        if (!mIsTabSelected) {
            setTabTextColor(mFirstPagePosition, mixColor(mTabTextColor, tabPalette.getTextColor(mFirstPagePosition), mFirstPagePositionOffset));
            if (mFirstPagePositionOffset > 0f && mFirstPagePosition < getChildCount() - 1) {
                setTabTextColor(secondPagePosition, mixColor(tabPalette.getTextColor(secondPagePosition), mTabTextColor, mFirstPagePositionOffset));
            }
        }

        // draw divider
        if (mDividerWidth > 0) {
            // If padding is 0, then the divider is half height of the parent control.
            int dividerHeight = mDividerPadding == 0f ? getHeight() / 2 : (int) (getHeight() - 2 * mDividerPadding);
            for (int i = 0; i < childCount - 1; i++) {
                View childView = getChildAt(i);
                mDividerPaint.setColor(tabPalette.getDividerColor(i));
                canvas.drawLine(childView.getRight(), (float) (getHeight() - dividerHeight) / 2, childView.getRight(), (float) (getHeight() + dividerHeight) / 2, mDividerPaint);
            }
        }

        // draw indicator
        if (mIndicatorHeight > 0) {
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
                    mIndicatorRectF.set(indicatorRectFLeft, mIndicatorTopMargin, indicatorRectFRight, mIndicatorTopMargin + mIndicatorHeight);
                    break;
                case Gravity.CENTER:
                    mIndicatorRectF.set(indicatorRectFLeft, (getHeight() - mIndicatorHeight) / 2, indicatorRectFRight, (getHeight() + mIndicatorHeight) / 2);
                    break;
                case Gravity.BOTTOM:
                    mIndicatorRectF.set(indicatorRectFLeft, getHeight() - mIndicatorHeight - mIndicatorBottomMargin, indicatorRectFRight, getHeight() - mIndicatorBottomMargin);
                    break;
                default:
                    // Can`t reach;
                    break;
            }

            if (mIndicatorDrawable != null) {
                mIndicatorDrawable.setBounds((int) mIndicatorRectF.left, (int) mIndicatorRectF.top, (int) mIndicatorRectF.right, (int) mIndicatorRectF.bottom);
                mIndicatorDrawable.draw(canvas);
            } else {
                canvas.drawRoundRect(mIndicatorRectF, mIndicatorCornerRadius, mIndicatorCornerRadius, mIndicatorPaint);
            }
        }

        // Callback the change color.
        if (mOnColorChangeListener != null) {
            int firstPageTextColor = tabPalette.getTextColor(mFirstPagePosition);
            // Sliding the page.
            if (mFirstPagePosition < getChildCount() - 1) {
                int secondPageTextColor = tabPalette.getTextColor(secondPagePosition);
                if (firstPageTextColor != secondPageTextColor) {
                    firstPageTextColor = mixColor(secondPageTextColor, firstPageTextColor, mFirstPagePositionOffset);
                }
            }
            mOnColorChangeListener.onColorChanged(firstPageTextColor);
        }
    }

    class SimpleTabPalette implements SlidingTabLayout.TabPalette {
        private int[] mTextColors;
        private int[] mDividerColors;

        public void setTextColors(@ColorInt int... colors) {
            this.mTextColors = colors;
        }

        @Override
        public final int getTextColor(int position) {
            return mTextColors[position % mTextColors.length];
        }

        public void setDividerColors(@ColorInt int... colors) {
            this.mDividerColors = colors;
        }

        @Override
        public final int getDividerColor(int position) {
            return mDividerColors[position % mDividerColors.length];
        }
    }
}
