package com.meishe.third.adpater;

import android.graphics.Canvas;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meishe.base.R;
import com.meishe.third.adpater.listener.OnItemDragListener;
import com.meishe.third.adpater.listener.OnItemSwipeListener;

import java.util.Collections;
import java.util.List;

/**
 * Created by luoxw on 2016/7/13.
 * 可拖动适配器
 * Drag-able adapter
 * @param <T> the type parameter
 * @param <K> the type parameter
 */
public abstract class BaseItemDraggableAdapter<T, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    private static final int NO_TOGGLE_VIEW = 0;
    protected int mToggleViewId = NO_TOGGLE_VIEW;
    protected ItemTouchHelper mItemTouchHelper;
    protected boolean itemDragEnabled = false;
    protected boolean itemSwipeEnabled = false;
    protected OnItemDragListener mOnItemDragListener;
    protected OnItemSwipeListener mOnItemSwipeListener;
    protected boolean mDragOnLongPress = true;

    protected View.OnTouchListener mOnToggleViewTouchListener;
    protected View.OnLongClickListener mOnToggleViewLongClickListener;

    private static final String ERROR_NOT_SAME_ITEMTOUCHHELPER = "Item drag and item swipe should pass the same ItemTouchHelper";


    public BaseItemDraggableAdapter(List<T> data) {
        super(data);
    }

    public BaseItemDraggableAdapter(int layoutResId, List<T> data) {
        super(layoutResId, data);
    }


    /**
     * To bind different types of holder and solve different the bind events
     * 绑定不同类型的持有者，解决不同的绑定事件
     * @param holder
     * @param position
     * @see #getDefItemViewType(int)
     */
    @Override
    public void onBindViewHolder(@NonNull K holder, int position) {
        super.onBindViewHolder(holder, position);
        int viewType = holder.getItemViewType();

        if (mItemTouchHelper != null && itemDragEnabled && viewType != LOADING_VIEW && viewType != HEADER_VIEW
                && viewType != EMPTY_VIEW && viewType != FOOTER_VIEW) {
            if (mToggleViewId != NO_TOGGLE_VIEW) {
                View toggleView = holder.getView(mToggleViewId);
                if (toggleView != null) {
                    toggleView.setTag(R.id.BaseQuickAdapter_view_holder_support, holder);
                    if (mDragOnLongPress) {
                        toggleView.setOnLongClickListener(mOnToggleViewLongClickListener);
                    } else {
                        toggleView.setOnTouchListener(mOnToggleViewTouchListener);
                    }
                }
            } else {
                holder.itemView.setTag(R.id.BaseQuickAdapter_view_holder_support, holder);
                holder.itemView.setOnLongClickListener(mOnToggleViewLongClickListener);
            }
        }
    }


    /**
     * Set the toggle view's id which will trigger drag event.
     * If the toggle view id is not set, drag event will be triggered when the item is long pressed.
     * 设置将触发拖放事件的切换视图的id。
     * 如果切换视图id没有设置，长按项目时将触发拖动事件。
     * @param toggleViewId the toggle view's id 切换视图的id
     */
    public void setToggleViewId(int toggleViewId) {
        mToggleViewId = toggleViewId;
    }

    /**
     * Set the drag event should be trigger on long press.
     * Work when the toggleViewId has been set.
     * 设置拖动事件时应在长按时触发。
     * 当toggleViewId被设置时工作
     * @param longPress by default is true. 默认为true
     */
    public void setToggleDragOnLongPress(boolean longPress) {
        mDragOnLongPress = longPress;
        if (mDragOnLongPress) {
            mOnToggleViewTouchListener = null;
            mOnToggleViewLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemTouchHelper != null && itemDragEnabled) {
                        mItemTouchHelper.startDrag((RecyclerView.ViewHolder) v.getTag(R.id.BaseQuickAdapter_view_holder_support));
                    }
                    return true;
                }
            };
        } else {
            mOnToggleViewTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN
                            && !mDragOnLongPress) {
                        if (mItemTouchHelper != null && itemDragEnabled) {
                            mItemTouchHelper.startDrag((RecyclerView.ViewHolder) v.getTag(R.id.BaseQuickAdapter_view_holder_support));
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            mOnToggleViewLongClickListener = null;
        }
    }

    /**
     * Enable drag items.
     * Use itemView as the toggleView when long pressed.
     *
     * 长按时使用itemView作为toggleView
     * @param itemTouchHelper {@link ItemTouchHelper}
     */
    public void enableDragItem(@NonNull ItemTouchHelper itemTouchHelper) {
        enableDragItem(itemTouchHelper, NO_TOGGLE_VIEW, true);
    }

    /**
     * Enable drag items. Use the specified view as toggle.
     * 使用指定的视图作为切换项
     * @param itemTouchHelper {@link ItemTouchHelper}
     * @param toggleViewId    The toggle view's id. 切换视图的id。
     * @param dragOnLongPress If true the drag event will be trigger on long press, otherwise on touch down.
     *                        如果为真，拖动事件将在长按时触发，否则在触地时触发
     */
    public void enableDragItem(@NonNull ItemTouchHelper itemTouchHelper, int toggleViewId, boolean dragOnLongPress) {
        itemDragEnabled = true;
        mItemTouchHelper = itemTouchHelper;
        setToggleViewId(toggleViewId);
        setToggleDragOnLongPress(dragOnLongPress);
    }

    /**
     * Disable drag items.
     */
    public void disableDragItem() {
        itemDragEnabled = false;
        mItemTouchHelper = null;
    }

    public boolean isItemDraggable() {
        return itemDragEnabled;
    }

    /**
     * <p>Enable swipe items.</p>
     */
    public void enableSwipeItem() {
        itemSwipeEnabled = true;
    }

    public void disableSwipeItem() {
        itemSwipeEnabled = false;
    }

    public boolean isItemSwipeEnable() {
        return itemSwipeEnabled;
    }

    /**
     * @param onItemDragListener Register a callback to be invoked when drag event happen.
     */
    public void setOnItemDragListener(OnItemDragListener onItemDragListener) {
        mOnItemDragListener = onItemDragListener;
    }

    public int getViewHolderPosition(RecyclerView.ViewHolder viewHolder) {
        return viewHolder.getAdapterPosition() - getHeaderLayoutCount();
    }

    /**
     * On item drag start.
     * 拖动开始
     * @param viewHolder the view holder
     */
    public void onItemDragStart(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemDragListener != null && itemDragEnabled) {
            mOnItemDragListener.onItemDragStart(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemDragMoving(RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        int from = getViewHolderPosition(source);
        int to = getViewHolderPosition(target);

        if (inRange(from) && inRange(to)) {
            if (from < to) {
                for (int i = from; i < to; i++) {
                    Collections.swap(mData, i, i + 1);
                }
            } else {
                for (int i = from; i > to; i--) {
                    Collections.swap(mData, i, i - 1);
                }
            }
            notifyItemMoved(source.getAdapterPosition(), target.getAdapterPosition());
        }

        if (mOnItemDragListener != null && itemDragEnabled) {
            mOnItemDragListener.onItemDragMoving(source, from, target, to);
        }
    }

    /**
     * On item drag end.
     * 拖动结束
     * @param viewHolder the view holder
     */
    public void onItemDragEnd(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemDragListener != null && itemDragEnabled) {
            mOnItemDragListener.onItemDragEnd(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void setOnItemSwipeListener(OnItemSwipeListener listener) {
        mOnItemSwipeListener = listener;
    }

    public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemSwipeListener != null && itemSwipeEnabled) {
            mOnItemSwipeListener.onItemSwipeStart(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemSwipeClear(RecyclerView.ViewHolder viewHolder) {
        if (mOnItemSwipeListener != null && itemSwipeEnabled) {
            mOnItemSwipeListener.clearView(viewHolder, getViewHolderPosition(viewHolder));
        }
    }

    public void onItemSwiped(RecyclerView.ViewHolder viewHolder) {
        final int pos = getViewHolderPosition(viewHolder);
        if (inRange(pos)) {
            mData.remove(pos);
            notifyItemRemoved(viewHolder.getAdapterPosition());

            if (mOnItemSwipeListener != null && itemSwipeEnabled) {
                mOnItemSwipeListener.onItemSwiped(viewHolder, pos);
            }
        }
    }

    public void onItemSwiping(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {
        if (mOnItemSwipeListener != null && itemSwipeEnabled) {
            mOnItemSwipeListener.onItemSwipeMoving(canvas, viewHolder, dX, dY, isCurrentlyActive);
        }
    }

    private boolean inRange(int position) {
        return position >= 0 && position < mData.size();
    }
}
