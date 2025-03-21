package com.meishe.sdkdemo.glitter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.meishe.sdkdemo.utils.ScreenUtils;


/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : malijia
 * @CreateDate : 2019/10/17.
 * @Description : 闪光特效录制按钮 * Flash effect shooting Record Button
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class NvRecordButton extends View {
    private Context mContext;

    private Paint mRectPaint;

    private Paint mCirclePaint;

    private float corner;
    private float circleRadius;
    private float circleStrokeWidth;
    private float rectWidth;

    private float mMinCircleRadius;
    private float mMaxCircleRadius;
    private float mMinRectWidth;
    private float mMaxRectWidth;
    private float mMinCorner;
    private float mMaxCorner;
    private float mMinCircleStrokeWidth;
    private float mMaxCircleStrokeWidth;


    private RectF mRectF = new RectF( );

    private RecordMode mRecordMode = RecordMode.ORIGIN;

    private AnimatorSet mBeginAnimatorSet = new AnimatorSet( );

    private AnimatorSet mEndAnimatorSet = new AnimatorSet( );

    private Xfermode mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    private Handler mHandler = new Handler( );

    private ClickRunnable mClickRunnable = new ClickRunnable( );

    private OnRecordStateChangedListener mOnRecordStateChangedListener;

    private float mInitX;

    private float mInitY;

    private float mDownRawX;

    private float mDownRawY;

    private float mInfectionPoint;

    private ScrollDirection mScrollDirection;

    private boolean mHasCancel = false;

    public NvRecordButton(Context context) {
        this(context, null);
    }

    public NvRecordButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NvRecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init( );
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setColor(Color.parseColor("#FC3E3E"));

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.parseColor("#7FFC3E3E"));

        mMinCircleStrokeWidth = ScreenUtils.dip2px(mContext, 5);
        mMaxCircleStrokeWidth = ScreenUtils.dip2px(mContext, 8);
        circleStrokeWidth = mMinCircleStrokeWidth;
        mCirclePaint.setStrokeWidth(circleStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth( );
        int height = getMeasuredHeight( );

        int centerX = width / 2;
        int centerY = height / 2;

        mMaxRectWidth = width / 1.6f;
        mMinRectWidth = mMaxRectWidth * 0.5f;

        mMinCircleRadius = mMaxRectWidth / 2 + mMinCircleStrokeWidth + ScreenUtils.dip2px(mContext, 2);
        mMaxCircleRadius = width / 1.8f - mMaxCircleStrokeWidth;

        mMinCorner = ScreenUtils.dip2px(mContext, 5);
        mMaxCorner = mMaxRectWidth / 2;

        if (rectWidth == 0) {
            rectWidth = mMaxRectWidth;
        }
        if (circleRadius == 0) {
            circleRadius = mMinCircleRadius;
        }
        if (corner == 0) {
            corner = rectWidth / 2;
        }

        mCirclePaint.setColor(Color.parseColor("#33ff0000"));
        canvas.drawCircle(centerX, centerY, circleRadius, mCirclePaint);
        mCirclePaint.setXfermode(mXfermode);

        mCirclePaint.setColor(Color.parseColor("#000000"));
        canvas.drawCircle(centerX, centerY, circleRadius - circleStrokeWidth, mCirclePaint);
        mCirclePaint.setXfermode(null);

        mRectF.left = centerX - rectWidth / 2;
        mRectF.right = centerX + rectWidth / 2;
        mRectF.top = centerY - rectWidth / 2;
        mRectF.bottom = centerY + rectWidth / 2;
        canvas.drawRoundRect(mRectF, corner, corner, mRectPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction( )) {
            case MotionEvent.ACTION_DOWN:
                if (mRecordMode == RecordMode.ORIGIN && inBeginRange(event)) {
                    mDownRawX = event.getRawX( );
                    mDownRawY = event.getRawY( );
                    startBeginAnimation( );
                    mHandler.postDelayed(mClickRunnable, 200);
                    mOnRecordStateChangedListener.onRecordStart( );
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mHasCancel) {
                    if (mRecordMode == RecordMode.LONG_CLICK) {
                        /*
                        ScrollDirection mOldDirection = mScrollDirection;
                        float oldY = getY();
                        setX(mInitX+event.getRawX()-mDownRawX);
                        setY(mInitY+event.getRawY()-mDownRawY);
                        float newY = getY();

                        if (newY <= oldY) {
                            mScrollDirection = ScrollDirection.UP;
                        } else {
                            mScrollDirection = ScrollDirection.DOWN;
                        }

                        if (mOldDirection != mScrollDirection) {
                            mInfectionPoint = oldY;
                        }
                        float zoomPercentage = (mInfectionPoint - getY()) / mInitY;
                        mOnRecordStateChangedListener.onZoom(zoomPercentage);
                        */
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mHasCancel) {
                    if (mRecordMode == RecordMode.LONG_CLICK) {
                        mOnRecordStateChangedListener.onRecordStop( );
                        resetLongClick( );
                    } else if (mRecordMode == RecordMode.ORIGIN && inBeginRange(event)) {
                        mHandler.removeCallbacks(mClickRunnable);
                        mRecordMode = RecordMode.SINGLE_CLICK;
                    } else if (mRecordMode == RecordMode.SINGLE_CLICK && inEndRange(event)) {
                        mOnRecordStateChangedListener.onRecordStop( );
                        resetSingleClick( );
                    }
                } else {
                    mHasCancel = false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean inBeginRange(MotionEvent event) {
        int centerX = getMeasuredWidth( ) / 2;
        int centerY = getMeasuredHeight( ) / 2;
        int minX = (int) (centerX - mMinCircleRadius);
        int maxX = (int) (centerX + mMinCircleRadius);
        int minY = (int) (centerY - mMinCircleRadius);
        int maxY = (int) (centerY + mMinCircleRadius);
        boolean isXInRange = event.getX( ) >= minX && event.getX( ) <= maxX;
        boolean isYInRange = event.getY( ) >= minY && event.getY( ) <= maxY;
        return isXInRange && isYInRange;
    }

    private boolean inEndRange(MotionEvent event) {
        int minX = 0;
        int maxX = getMeasuredWidth( );
        int minY = 0;
        int maxY = getMeasuredHeight( );
        boolean isXInRange = event.getX( ) >= minX && event.getX( ) <= maxX;
        boolean isYInRange = event.getY( ) >= minY && event.getY( ) <= maxY;
        return isXInRange && isYInRange;
    }

    private void resetLongClick() {
        mRecordMode = RecordMode.ORIGIN;
        mBeginAnimatorSet.cancel( );
        startEndAnimation( );
        setX(mInitX);
        setY(mInitY);
    }

    private void resetSingleClick() {
        mRecordMode = RecordMode.ORIGIN;
        mBeginAnimatorSet.cancel( );
        startEndAnimation( );
    }

    public void reset() {
        if (mRecordMode == RecordMode.LONG_CLICK) {
            resetLongClick( );
        } else if (mRecordMode == RecordMode.SINGLE_CLICK) {
            resetSingleClick( );
        } else if (mRecordMode == RecordMode.ORIGIN) {
            if (mBeginAnimatorSet.isRunning( )) {
                mHasCancel = true;
                mBeginAnimatorSet.cancel( );
                startEndAnimation( );
                mHandler.removeCallbacks(mClickRunnable);
                mRecordMode = RecordMode.ORIGIN;
            }
        }
    }

    public void startClockRecord() {
        if (mRecordMode == RecordMode.ORIGIN) {
            startBeginAnimation( );
            mRecordMode = RecordMode.SINGLE_CLICK;
        }
    }

    private void startBeginAnimation() {
        AnimatorSet startAnimatorSet = new AnimatorSet( );
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                mMaxCorner, mMinCorner)
                .setDuration(500);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                mMaxRectWidth, mMinRectWidth)
                .setDuration(500);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "circleRadius",
                mMinCircleRadius, mMaxCircleRadius)
                .setDuration(500);
        startAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, radiusAnimator);

        ObjectAnimator circleWidthAnimator = ObjectAnimator.ofFloat(this, "circleStrokeWidth",
                mMinCircleStrokeWidth, mMaxCircleStrokeWidth, mMinCircleStrokeWidth)
                .setDuration(1500);
        circleWidthAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        mBeginAnimatorSet.playSequentially(startAnimatorSet, circleWidthAnimator);
        mBeginAnimatorSet.start( );
    }

    private void startEndAnimation() {
        ObjectAnimator cornerAnimator = ObjectAnimator.ofFloat(this, "corner",
                mMinCorner, mMaxCorner)
                .setDuration(500);
        ObjectAnimator rectSizeAnimator = ObjectAnimator.ofFloat(this, "rectWidth",
                mMinRectWidth, mMaxRectWidth)
                .setDuration(500);
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(this, "circleRadius",
                mMaxCircleRadius, mMinCircleRadius)
                .setDuration(500);
        ObjectAnimator circleWidthAnimator = ObjectAnimator.ofFloat(this, "circleStrokeWidth",
                mMaxCircleStrokeWidth, mMinCircleStrokeWidth)
                .setDuration(500);

        mEndAnimatorSet.playTogether(cornerAnimator, rectSizeAnimator, radiusAnimator, circleWidthAnimator);
        mEndAnimatorSet.start( );
    }

    public void setCorner(float corner) {
        this.corner = corner;
        invalidate( );
    }

    public void setCircleRadius(float circleRadius) {
        this.circleRadius = circleRadius;
    }

    public void setCircleStrokeWidth(float circleStrokeWidth) {
        this.circleStrokeWidth = circleStrokeWidth;
        invalidate( );
    }

    public void setRectWidth(float rectWidth) {
        this.rectWidth = rectWidth;
    }

    class ClickRunnable implements Runnable {

        @Override
        public void run() {
            if (!mHasCancel) {
                mRecordMode = RecordMode.LONG_CLICK;
                mInitX = getX( );
                mInitY = getY( );
                mInfectionPoint = mInitY;
                mScrollDirection = ScrollDirection.UP;
            }
        }
    }

    public void setOnRecordStateChangedListener(OnRecordStateChangedListener listener) {
        this.mOnRecordStateChangedListener = listener;
    }

    public interface OnRecordStateChangedListener {

        /**
         * 开始录制
         * Start recording
         */
        void onRecordStart();

        /**
         * 结束录制
         * End recording
         */
        void onRecordStop();
    }

    private enum RecordMode {
        /**
         * 单击录制模式
         * One-click recording mode
         */
        SINGLE_CLICK,
        /**
         * 长按录制模式
         * Long press recording mode
         */
        LONG_CLICK,
        /**
         * 初始化
         * initialization
         */
        ORIGIN;

        RecordMode() {

        }
    }

    private enum ScrollDirection {
        /**
         * 滑动方向，上
         * Sliding direction, up
         */
        UP,
        /**
         * 滑动方向，下
         * Slide direction, down
         */
        DOWN;

        ScrollDirection() {

        }
    }
}
