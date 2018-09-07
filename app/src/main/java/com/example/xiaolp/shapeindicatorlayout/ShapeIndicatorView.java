package com.example.xiaolp.shapeindicatorlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


/**
 * Created by xiaolp on 2018/8/23
 * description:
 */
public class ShapeIndicatorView extends View implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private Paint mShapePaint;
    private Path mShapePath;
    private int mShapeHorizontalSpace = 0;
    private int mShapeColor = Color.GREEN;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;


    public ShapeIndicatorView(Context context) {
        this(context, null);
    }

    public ShapeIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs, defStyleAttr, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShapeIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initViews(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
        mShapePaint.setDither(true);
        mShapePaint.setColor(Color.RED);
        mShapePaint.setStyle(Paint.Style.FILL);
        mShapePaint.setPathEffect(new CornerPathEffect(66));
        mShapePaint.setStrokeCap(Paint.Cap.ROUND);

    }


    public void setupWithTabLayout(final TabLayout tableLayout) {
        mTabLayout = tableLayout;
        tableLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        tableLayout.addOnTabSelectedListener(this);
        tableLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (mTabLayout.getScaleX() != getScaleX()) {
                    scrollTo(mTabLayout.getScrollX(), mTabLayout.getScrollY());
                }
            }
        });
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mTabLayout.getTabCount() > 0)
                    onTabSelected(mTabLayout.getTabAt(0));
            }
        });
        ViewCompat.setElevation(this, ViewCompat.getElevation(mTabLayout));

        //清除Tab background
        for (int tab = 0; tab < tableLayout.getTabCount(); tab++) {
            View tabView = getTabViewByPosition(tab);
            assert tabView != null;
            tabView.setBackgroundResource(0);
        }
    }

    public void setupWithViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPath(canvas);
    }

    private void drawPath(Canvas canvas) {
        if (mShapePath == null || mShapePath.isEmpty())
            return;
        int savePos = canvas.save();
        canvas.drawPath(mShapePath, mShapePaint);
        canvas.restoreToCount(savePos);
    }

    private void generatePath(int position, float positionOffset) {
        RectF range = new RectF();
        View tabView = getTabViewByPosition(position);
        if (tabView == null)
            return;
        int left, top, right, bottom;
        left = top = right = bottom = 0;
        int paddingTop = getPaddingTop() + 20;
        int paddingBottom = getPaddingBottom() + 20;
        if (positionOffset > 0.f && position < mTabLayout.getTabCount() - 1) {
            View nextTabView = getTabViewByPosition(position + 1);
            assert nextTabView != null;
            left += (int) (nextTabView.getLeft() * positionOffset + tabView.getLeft() * (1.f - positionOffset));
            right += (int) (nextTabView.getRight() * positionOffset + tabView.getRight() * (1.f - positionOffset));
            left += mShapeHorizontalSpace;
            right -= mShapeHorizontalSpace;
            top = tabView.getTop() + paddingTop;
            bottom = tabView.getBottom() - paddingBottom;
            range.set(left, top, right, bottom);
        } else {
            left = tabView.getLeft() + mShapeHorizontalSpace;
            right = tabView.getRight() - mShapeHorizontalSpace;
            top = tabView.getTop() + paddingTop;
            bottom = tabView.getBottom() - paddingBottom;
            range.set(left, top, right, bottom);
            if (range.isEmpty())
                return;
        }

        if (mShapePath == null)
            mShapePath = new Path();

        mShapePath.reset();
        mShapePath.moveTo(range.right, range.bottom);
        mShapePath.lineTo(range.left, range.bottom);
        mShapePath.lineTo(range.left, range.top);
        mShapePath.lineTo(range.right, range.top);
        mShapePath.lineTo(range.right, range.bottom);
        mShapePath.close();

    }

    private Rect getTabArea() {
        Rect rect = null;
        if (mTabLayout != null) {
            View view = mTabLayout.getChildAt(0);
            rect = new Rect();
            view.getHitRect(rect);
        }

        return rect;
    }

    private View getTabViewByPosition(int position) {
        if (mTabLayout != null && mTabLayout.getTabCount() > 0) {
            ViewGroup tabStrip = (ViewGroup) mTabLayout.getChildAt(0);
            return tabStrip != null ? tabStrip.getChildAt(position) : null;
        }

        return null;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        generatePath(position, positionOffset);
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        if (mTabLayout.getSelectedTabPosition() != position && mTabLayout.getTabAt(position) != null)
            mTabLayout.getTabAt(position).select();

    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 当已经有一个ViewPager后，当TabLayout的tab改变的时候在onTabSelected方法直接调用ViewPager的
     * setCurrentItem方法调用这个方法后会触发ViewPager的scroll事件也就是在onPageScrolled方法中调用
     * generatePath方法来更新Path，如果没有ViewPager的话直接在onTabSelected的方法中调用generatePath
     * 方法。
     **/
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (mViewPager != null) {
            if (tab.getPosition() != mViewPager.getCurrentItem())
                mViewPager.setCurrentItem(tab.getPosition());
        } else {
            generatePath(tab.getPosition(), 0);
            invalidate();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}