package com.meishe.sdkdemo.photoalbum.grallyRecyclerView;

import android.content.Context;
import android.view.View;

import com.meishe.sdkdemo.utils.ScreenUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * recyclerView实现画廊帮助类
 * recyclerView implements gallery helper class
 */
public class CardScaleHelper {
    private RecyclerView mRecyclerView;
    private Context mContext;
    private LinearLayoutManager layoutManager;
    private GalleryAdapter galleryAdapter;

    /*
    *  两边视图scale
    * View on both sides scale
    * */
    private float mScale = 0.8f;
    /*
    * 卡片的padding, 卡片间的距离等于2倍的mPagePadding
    * Padding of cards, the distance between cards is equal to 2 times mPagePadding
    * */
    private int mPagePadding = 15;
    /*
    * 左边卡片显示大小
    * Left card display size
    * */
    public int mShowLeftCardWidth = 60;

    /*
    * 卡片宽度
    * Card width
    * */
    private int mCardWidth;
    /*
    * 滑动一页的距离
    * Distance by one page
    * */
    private int mOnePageWidth;
    private int mCardGalleryWidth;

    private int mCurrentItemPos;
    private int mCurrentItemOffset;

    private OnGrallyItemSelectListener mSelectListener;
    public interface OnGrallyItemSelectListener {
        void onItemSelect(int pos);
        void onScrolling();
    }
    public void setOnGrallyItemSelectListener(OnGrallyItemSelectListener listener) {
        mSelectListener = listener;
    }

    private CardLinearSnapHelper mLinearSnapHelper = new CardLinearSnapHelper();

    public void attachToRecyclerView(final RecyclerView mRecyclerView) {
        /*
        * 开启log会影响滑动体验, 调试时才开启
        * Enabling the log will affect the sliding experience, which is only enabled when debugging
        * */
//        LogUtils.mLogEnable = false;
        this.mRecyclerView = mRecyclerView;
        layoutManager = (LinearLayoutManager) this.mRecyclerView.getLayoutManager();
        galleryAdapter = (GalleryAdapter) this.mRecyclerView.getAdapter();
        mContext = mRecyclerView.getContext();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mLinearSnapHelper.mNoNeedToScroll = mCurrentItemOffset == 0 || mCurrentItemOffset == getDestItemOffset(mRecyclerView.getAdapter().getItemCount() - 1);

                    int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                    galleryAdapter.setSelectPos(position);
                    if (mSelectListener != null) {
                        mSelectListener.onItemSelect(position);
                    }

                } else {
                    mLinearSnapHelper.mNoNeedToScroll = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                /*
                * dx>0则表示右滑, dx<0表示左滑, dy<0表示上滑, dy>0表示下滑
                * dx> 0 means sliding right, dx <0 means sliding left, dy <0 means sliding up, dy> 0 means sliding
                * */
                if(dx != 0){
                    mCurrentItemOffset += dx;
                    computeCurrentItemPos();
//                    LogUtils.v(String.format("dx=%s, dy=%s, mScrolledX=%s", dx, dy, mCurrentItemOffset));
                    onScrolledChangedCallback();


                    if (mSelectListener != null) {
                        mSelectListener.onScrolling();
                    }
                }
            }
        });

        initWidth();
        mRecyclerView.setOnFlingListener(null);
        mLinearSnapHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * 初始化卡片宽度
     * Initialize the card width
     */
    private void initWidth() {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mCardGalleryWidth = mRecyclerView.getWidth();
                mCardWidth = mCardGalleryWidth - ScreenUtils.dip2px(mContext, 2 * (mPagePadding + mShowLeftCardWidth));
                mOnePageWidth = mCardWidth;
                mRecyclerView.smoothScrollToPosition(mCurrentItemPos);
                onScrolledChangedCallback();
            }
        });
    }

    public void setCurrentItemPos(int currentItemPos) {
        this.mCurrentItemPos = currentItemPos;
    }

    public int getCurrentItemPos() {
        return mCurrentItemPos;
    }

    private int getDestItemOffset(int destPos) {
        return mOnePageWidth * destPos;
    }

    /**
     * 计算mCurrentItemPos
     * Calculate mCurrentItemPos
     */
    private void computeCurrentItemPos() {
        if (mOnePageWidth <= 0) return;
        boolean pageChanged = false;
        /*
        * 滑动超过一页说明已翻页
        * Slide more than one page to indicate that the page has been turned
        * */
        if (Math.abs(mCurrentItemOffset - mCurrentItemPos * mOnePageWidth) >= mOnePageWidth) {
            pageChanged = true;
        }
        if (pageChanged) {
            int tempPos = mCurrentItemPos;

            mCurrentItemPos = mCurrentItemOffset / mOnePageWidth;
//            LogUtils.d(String.format("=======onCurrentItemPos Changed======= tempPos=%s, mCurrentItemPos=%s", tempPos, mCurrentItemPos));
        }
    }

    /**
     * RecyclerView位移事件监听, view大小随位移事件变化
     * RecyclerView displacement event listener, view size changes with displacement event
     */
    private void onScrolledChangedCallback() {
        int offset = mCurrentItemOffset - mCurrentItemPos * mOnePageWidth;
        float percent = (float) Math.max(Math.abs(offset) * 1.0 / mOnePageWidth, 0.0001);

        int height = mOnePageWidth * 16 / 9;

//        LogUtils.d(String.format("offset=%s, percent=%s", offset, percent));
        View leftView = null;
        View currentView;
        View rightView = null;
        if (mCurrentItemPos > 0) {
            leftView = mRecyclerView.getLayoutManager().findViewByPosition(mCurrentItemPos - 1);
        }
        currentView = mRecyclerView.getLayoutManager().findViewByPosition(mCurrentItemPos);
        if (mCurrentItemPos < mRecyclerView.getAdapter().getItemCount() - 1) {
            rightView = mRecyclerView.getLayoutManager().findViewByPosition(mCurrentItemPos + 1);
        }

        if (leftView != null) {
            // y = (1 - mScale)x + mScale
            leftView.setScaleX((1 - mScale) * percent + mScale);
            leftView.setScaleY((1 - mScale) * percent + mScale);
        }
        if (currentView != null) {
            // y = (mScale - 1)x + 1
            currentView.setScaleX((mScale - 1) * percent + 1);
            currentView.setScaleY((mScale - 1) * percent + 1);
        }
        if (rightView != null) {
            // y = (1 - mScale)x + mScale
            rightView.setScaleX((1 - mScale) * percent + mScale);
            rightView.setScaleY((1 - mScale) * percent + mScale);
        }
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void setPagePadding(int pagePadding) {
        mPagePadding = pagePadding;
    }

    public void setShowLeftCardWidth(int showLeftCardWidth) {
        mShowLeftCardWidth = showLeftCardWidth;
    }
}
