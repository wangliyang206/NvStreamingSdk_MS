package com.meishe.sdkdemo.capturescene.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.meishe.sdkdemo.R;

/**
 * Created by CaoZhiChao on 2019/1/3 16:23
 */
public class CircleBarView extends View {
    int progressColor;
    int noneProgressColor;
    private Paint rPaint;
    private Paint progressPaint;
    private RectF rectF;
    private int CBProgress = 0;

    public void setProgress(int progress) {
        this.CBProgress = progress * 360 / 100;
        invalidate();
    }

    public void setCBProgress(int CBProgress) {
        this.CBProgress = CBProgress * 360 / 100;
        invalidate();
    }

    public CircleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleBarView);
        progressColor = mTypedArray.getColor(R.styleable.CircleBarView_CBProgressColor, Color.BLUE);
        noneProgressColor = mTypedArray.getColor(R.styleable.CircleBarView_CBNoneProgressColor, Color.WHITE);
        CBProgress = mTypedArray.getInt(R.styleable.CircleBarView_CBProgress, 0);
        mTypedArray.recycle();

        rPaint = new Paint();
        rPaint.setStyle(Paint.Style.FILL);
        rPaint.setColor(noneProgressColor);
        rPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (CBProgress > 0) {
            setBackground(null);
            canvas.drawArc(rectF, -90, CBProgress, true, progressPaint);
            canvas.drawArc(rectF, -90 + CBProgress, 360 - CBProgress, true, rPaint);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                - getPaddingRight();
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingLeft()
                - getPaddingRight();
        /*
         * viewHeight与viewWidth必须相等，不等取最大值
         * viewHeight must be equal to viewWidth，if not equal, take the maximum
         * */
        if (viewHeight != viewWidth) {
            viewWidth = Math.max(viewWidth, viewHeight);
            viewHeight = Math.max(viewWidth, viewHeight);
        }
        rectF = new RectF(0, 0, viewWidth, viewHeight);
    }
}
