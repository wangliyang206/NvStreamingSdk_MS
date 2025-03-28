package com.meishe.third.pop.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.meishe.third.pop.interfaces.OnClickOutsideListener;
import com.meishe.third.pop.util.XPopupUtils;


/**
 * Description:
 * Create by dance, at 2019/1/10
 * 阴影部分的容器类
 * The shaded part of the container class
 */
public class PartShadowContainer extends FrameLayout {
    public boolean isDismissOnTouchOutside = true;

    public PartShadowContainer(@NonNull Context context) {
        super(context);
    }

    public PartShadowContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PartShadowContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float x, y;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
        * 计算implView的Rect
        * Calculate the Rect for implView
        * */
        View implView = getChildAt(0);
        int[] location = new int[2];
        implView.getLocationInWindow(location);
        Rect implViewRect = new Rect(location[0], location[1], location[0] + implView.getMeasuredWidth(),
                location[1] + implView.getMeasuredHeight());
        if (!XPopupUtils.isInRect(event.getRawX(), event.getRawY(), implViewRect)) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                x = event.getX();
                y = event.getY();
            } else if (action == MotionEvent.ACTION_UP) {
                float dx = event.getX() - x;
                float dy = event.getY() - y;
                float distance = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                if (distance < ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    if (isDismissOnTouchOutside) {
                        if (listener != null) listener.onClickOutside();
                    }
                }
                x = 0;
                y = 0;
            }
        }
        return true;
    }

    private OnClickOutsideListener listener;

    public void setOnClickOutsideListener(OnClickOutsideListener listener) {
        this.listener = listener;
    }
}
