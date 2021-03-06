package com.xxun.xungallery.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xxun.xungallery.adapter.DirectionalViewPager;

/**
 * 自定义ViewPager类，此类只是为了解决双指缩放过小可能会出现异常的问题
 *
 * @author ghc
 */
public class ViewPagerFixed extends DirectionalViewPager {
    public ViewPagerFixed(Context context) {
        super(context);
    }

    public ViewPagerFixed(Context context, AttributeSet att) {
        super(context, att);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        try {
            return super.onInterceptTouchEvent(arg0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        try {
            return super.onTouchEvent(arg0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
