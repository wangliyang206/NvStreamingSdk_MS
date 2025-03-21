package com.meishe.sdkdemo.edit.audio.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.utils.ScreenUtils;

/**
 * 纵向滑动的seekbar
 * Seekbar sliding vertically
 */
public class AudioEqualizerVerticalSeekBar extends View {
    private Context context;
    private int height;
    private int width;
    private Paint paint;
    private int maxProgress = 100;
    private int progress = 50;

    protected Bitmap mThumb;
    private int intrinsicHeight;
    private int intrinsicWidth;
    private boolean isInnerClick;

    private int locationX;
    private int locationY = -1;

    private int mInnerProgressWidth = 4;
    private int mInnerProgressWidthPx;

    private int unSelectColor = 0xff414141;
    private RectF mDestRect;

    /**
     * 设置未选中的颜色
     * Set unchecked colors
     */
    public void setUnSelectColor(int uNSelectColor) {
        this.unSelectColor = uNSelectColor;
    }

    private int selectColor = 0xffffffff;

    /**
     * 设置选中线条的颜色
     * Set the color of selected lines
     */
    public void setSelectColor(int selectColor) {
        this.selectColor = selectColor;
    }

    /**
     * 设置进度条的宽度 单位是px
     * Set the width of the progress bar in px
     */
    public void setmInnerProgressWidthPx(int mInnerProgressWidthPx) {
        this.mInnerProgressWidthPx = mInnerProgressWidthPx;
    }

    /**
     * 设置进度条的宽度 ，单位是dp;默认是4dp
     * Set the width of the progress bar, the unit is dp; the default is 4dp
     */
    public void setmInnerProgressWidthDp(int mInnerProgressWidth) {
        this.mInnerProgressWidth = mInnerProgressWidth;
        mInnerProgressWidthPx = ScreenUtils.dip2px(context, mInnerProgressWidth);
    }

    /**
     * 设置图片
     * Set picture
     */
    public void setThumb(int id) {

        mThumb = BitmapFactory.decodeResource(getResources(), id);
        intrinsicHeight = mThumb.getHeight();
        intrinsicWidth = mThumb.getWidth();
        mDestRect.set(0, 0, intrinsicWidth, intrinsicHeight);
        invalidate();
    }

    /**
     * 设置滑动图片的大小 单位是dp
     * Set the size of the sliding image. The unit is dp.
     */
    public void setThumbSizeDp(int width, int height) {
        setThumbSizePx(ScreenUtils.dip2px(context, width), ScreenUtils.dip2px(context, height));
    }

    /**
     * 设置滑动图片的大小 单位是px
     * Set the size of the sliding image. The unit is px.
     */
    public void setThumbSizePx(int width, int height) {
        intrinsicHeight = width;
        intrinsicWidth = height;
        mDestRect.set(0, 0, width, height);
        invalidate();
    }

    public AudioEqualizerVerticalSeekBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AudioEqualizerVerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AudioEqualizerVerticalSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        paint = new Paint();
        mThumb = BitmapFactory.decodeResource(getResources(), R.drawable.acitivity_audio_equlaizer_adjust);
        intrinsicHeight = mThumb.getHeight();
        intrinsicWidth = mThumb.getWidth();
        mDestRect = new RectF(0, 0, intrinsicWidth, intrinsicHeight);
        mInnerProgressWidthPx = ScreenUtils.dip2px(context, mInnerProgressWidth);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        if (locationY == -1) {
            locationX = width / 2;
            locationY = height / 2;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断点击点是否在圈圈上
                //Judge whether the click point is on the circle
                isInnerClick = isInnerMthum(event);
                if (isInnerClick) {
                    if (listener != null) {
                        listener.onStart(this, progress);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isInnerClick) {
                    locationY = (int) event.getY();//int) (locationY + event.getY() - downY);
                    fixLocationY();
                    int curProgress = (int) (maxProgress - (locationY - intrinsicHeight * 0.5) / (height - intrinsicHeight) * maxProgress);
                    if(progress != curProgress){
                        progress = curProgress;
                        if (listener != null) {
                            listener.onProgress(this, progress);
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isInnerClick) {
                    if (listener != null) {
                        listener.onStop(this, progress);
                    }
                }
                break;
        }
        return true;
    }

    private void fixLocationY() {
        if (locationY <= intrinsicHeight / 2) {
            locationY = intrinsicHeight / 2;
        } else if (locationY >= height - intrinsicHeight / 2) {
            locationY = height - intrinsicHeight / 2;
        }
    }

    /**
     * 是否点击了图片
     * Whether the picture was clicked
     */
    private boolean isInnerMthum(MotionEvent event) {
        return event.getX() >= width / 2 - intrinsicWidth / 2 && event.getX() <= width / 2 + intrinsicWidth / 2 && event.getY() >= locationY - intrinsicHeight / 2 && event.getY() <= locationY + intrinsicHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        locationY = (int) (intrinsicHeight * 0.5f + (maxProgress - progress) * (height - intrinsicHeight) / maxProgress);
        if (locationY > mDestRect.height()) {
            paint.setColor(unSelectColor);
            canvas.drawRect(width / 2 - mInnerProgressWidthPx / 2, mDestRect.height() / 2, width / 2 + mInnerProgressWidthPx / 2, locationY - mDestRect.height() / 2, paint);
        }
        if (locationY + mDestRect.height() < height) {
            paint.setColor(selectColor);
            canvas.drawRect(width / 2 - mInnerProgressWidthPx / 2, locationY + mDestRect.height() / 2, width / 2 + mInnerProgressWidthPx / 2, height - mDestRect.height() / 2, paint);
        }
        canvas.save();
        canvas.translate(width * 1f / 2 - mDestRect.width() / 2, locationY - mDestRect.height() / 2);
        paint.reset();
        canvas.drawBitmap(mThumb, null, mDestRect, paint);
        canvas.restore();
        super.onDraw(canvas);
    }

    public void setProgress(int progress) {
        if (height == 0) {
            height = getMeasuredHeight();
        }
        this.progress = progress;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected void onDetachedFromWindow() {
        /*if (mThumb != null) {
            mThumb.recycle();
        }*/
        super.onDetachedFromWindow();
    }


    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    private SlideChangeListener listener;

    public void setOnSlideChangeListener(SlideChangeListener l) {
        this.listener = l;
    }

    public int getLocationY() {
        return locationY;
    }

    /*
     * 添加监听接口
     * Add listening interface
     * */
    public interface SlideChangeListener {
        void onStart(AudioEqualizerVerticalSeekBar slideView, int progress);

        void onProgress(AudioEqualizerVerticalSeekBar slideView, int progress);

        void onStop(AudioEqualizerVerticalSeekBar slideView, int progress);
    }
}
