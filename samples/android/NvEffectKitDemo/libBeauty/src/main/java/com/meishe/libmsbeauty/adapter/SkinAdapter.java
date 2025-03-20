package com.meishe.libmsbeauty.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.meishe.libbase.util.CommonUtils;
import com.meishe.libmsbeauty.R;
import com.meishe.libmsbeauty.bean.BaseFxInfo;
import com.meishe.libmsbeauty.bean.IFxInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 美颜
 */
public class SkinAdapter extends RecyclerView.Adapter<SkinAdapter.ViewHolder> {

    /**
     * data item , controlled by ui
     * 真实的数据实体类，控制UI显示
     */
    private List<IFxInfo> mDataList;
    private int mSelectedPos = Integer.MAX_VALUE;
    private int mStrengthSelectPos = 2;
    private Context mContext;
    private OnItemClickListener mClickListener;
    private boolean mIsEnable = true;
    // if is beauty shape
    //判断是否是美型
    private boolean isBeautyShape = false;
    private boolean needFirstBack = false;

    /**
     * two types
     * 两种类型
     * 1.是类型选项，点击返回选择类型的列表
     * is type class ,click back to type item list
     * 2.美型的item操作，点击设置值
     * is type item ,clic to apply value
     */
    public static final int TYPE_KIND_BACK = 1;
    public static final int TYPE_KIND_ITEM = 2;

    /**
     * type item point
     * 点类型
     */
    public static final int TYPE_POINT = 3;

    private boolean isHandClick = false;

    /**
     * if is extended
     * 是否处于扩展状态
     */
    private boolean isExtendState = false;

    /**
     * temp beauty fx data
     * 只是存放磨皮的数据
     */
    private List<IFxInfo> mTmpBeautyData;
    private Switch mSwitchView;

    public SkinAdapter(Context context, List<IFxInfo> skinDataList, List<IFxInfo> dataList) {
        mContext = context;
        mDataList = dataList;
        mTmpBeautyData = skinDataList;
    }

    public void setIsBeautyShape(boolean isBeautyShape) {
        this.isBeautyShape = isBeautyShape;
    }

    public void setDataList(List<IFxInfo> data) {

        this.mDataList = data;
    }

    public void setEnable(boolean enable) {
        mIsEnable = enable;
        if (!mIsEnable) {
            hideStrength();
        }
        mSelectedPos = Integer.MAX_VALUE;
        notifyDataSetChanged();
    }


    public void setSwitch(Switch smallShapeSwitch) {
        mSwitchView = smallShapeSwitch;
    }

    public void setSelectPos(int pos) {
        mSelectedPos = pos;
        notifyDataSetChanged();
    }

    public int getSelectPos() {
        return mSelectedPos;
    }

    public IFxInfo getSelectItem() {
        if (mDataList != null && mSelectedPos >= 0 && mSelectedPos < mDataList.size()) {
            return mDataList.get(mSelectedPos);
        }
        return null;
    }

    public IFxInfo getItem(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        if (mDataList == null) {
            return null;
        }
        for (int i = 0; i < mDataList.size(); i++) {
            IFxInfo beautyInfo = mDataList.get(i);
            if (beautyInfo == null) {
                continue;
            }
            String name = beautyInfo.getName();
            if (tag.equals(name)) {
                return beautyInfo;
            }
        }
        return null;
    }

    public List<IFxInfo> getItems() {
        return mDataList;
    }


    @Override
    public int getItemViewType(int position) {
        IFxInfo info = mDataList.get(position);
        if (TextUtils.equals(info.getType(), BaseFxInfo.TYPE_PLACE_HOLDER)) {
            return TYPE_POINT;
        }
        return TYPE_KIND_ITEM;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void updateDataList(ArrayList dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
    }

    public void updateItem(int position, IFxInfo fxInfo) {
        if (position >= 0 && position < mDataList.size()) {
            IFxInfo iFxInfo = mDataList.get(position);
            iFxInfo.copy(fxInfo);
            notifyItemChanged(position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        View view;
        switch (viewType) {
            case TYPE_POINT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beauty_shape_point_item, parent, false);
                holder = new ViewHolder(view);
                break;
            case TYPE_KIND_BACK:
            case TYPE_KIND_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beauty_shape_item, parent, false);
                holder = new ViewHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final int itemType = getItemViewType(position);
        if (itemType == TYPE_POINT) {
            if (mIsEnable) {
                holder.itemView.setAlpha(1f);
            } else {
                holder.itemView.setAlpha(0.5f);
            }
        } else {
            final IFxInfo item = mDataList.get(position);
            if (item.getStrength() != 0) {
                holder.blue_point.setVisibility(View.VISIBLE);
            } else {
                holder.blue_point.setVisibility(View.GONE);
            }
            if (position == 0) {
                if (isHandClick) {
                    isHandClick = false;
                    if (item.getName().equals(mContext.getResources().getString(R.string.strength))) {
                        holder.shape_icon.setImageResource(R.mipmap.icon_fx_back);
                        item.setName(mContext.getString(R.string.back));
                    } else {
                        holder.shape_icon.setImageResource(R.mipmap.ic_buffing);
                        item.setName(mContext.getString(R.string.strength));
                    }
                    holder.shape_name.setText(item.getName());
                } else {
                    if (!isExtendState) {
                        holder.shape_icon.setImageResource(R.mipmap.ic_buffing);
                        item.setName(mContext.getString(R.string.strength));
                        holder.shape_name.setText(item.getName());
                        boolean hasSkin = false;
                        for (int i = 0; i < mTmpBeautyData.size(); i++) {
                            IFxInfo itemInfo = mTmpBeautyData.get(i);
                            if (itemInfo == null) {
                                continue;
                            }
                            if (itemInfo.getStrength() != 0) {
                                hasSkin = true;
                                break;
                            }
                        }
                        if (hasSkin) {
                            holder.blue_point.setVisibility(View.VISIBLE);
                        } else {
                            holder.blue_point.setVisibility(View.GONE);
                        }
                    } else {
                        holder.shape_icon.setImageResource(R.mipmap.icon_fx_back);
                        item.setName(mContext.getString(R.string.back));
                    }

                }

                if (mIsEnable) {
                    holder.shape_icon_layout.setAlpha(1f);
                    holder.shape_name.setAlpha(1f);
                } else {
                    holder.shape_icon_layout.setAlpha(0.5f);
                    holder.shape_name.setAlpha(0.5f);
                }
                holder.shape_name.setTextColor(mContext.getResources().getColor(R.color.ms_disable_color));
            } else {
                holder.shape_icon.setImageResource(item.getResourceId());
                holder.shape_name.setText(item.getName());
                if (mIsEnable) {
                    holder.shape_name.setTextColor(mContext.getResources().getColor(R.color.black_alfph));
                } else {
                    holder.shape_name.setTextColor(mContext.getResources().getColor(R.color.ms_disable_color));
                }


                if (mIsEnable && (mSelectedPos == position)) {
                    holder.shape_icon.setSelected(true);
                    holder.shape_name.setTextColor(Color.parseColor("#CC4A90E2"));
                    holder.shape_icon_layout.setAlpha(1.0f);
                    holder.shape_name.setAlpha(1.0f);
                } else {
                    holder.shape_icon.setSelected(false);
                    if (mIsEnable && mSelectedPos != position) {
                        if (isBeautyShape) {
                            holder.shape_name.setTextColor(mContext.getResources().getColor(R.color.black_alfph));
                        } else {
                            holder.shape_name.setTextColor(Color.BLACK);
                        }
                        holder.shape_icon_layout.setAlpha(1.0f);
                        holder.shape_name.setAlpha(0.8f);

                    } else if (!mIsEnable) {
                        holder.shape_name.setTextColor(Color.BLACK);
                        holder.shape_icon_layout.setAlpha(0.5f);
                        holder.shape_name.setAlpha(0.5f);
                    }
                }

            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSwitchView.isChecked()) {
                        return;
                    }
                    if (!mIsEnable) {
                        return;
                    }
                    int tmpPosition = position;
                    if (position == 0) {
                        isHandClick = true;
                        if (item.getName().equals(mContext.getResources().getString(R.string.strength))) {
                            showStrength();
                        } else {
                            hideStrength();
                        }
                        notifyDataSetChanged();
                        return;
                    } else {
                        //position 不是0
                        if (isExtendState) {//展开状态
                            //最后一个磨皮的位置
                            int skinLastIndex = 5;
                            if (position > skinLastIndex) { //收起磨皮
                                hideStrength();
                                tmpPosition -= skinLastIndex;
                                mSelectedPos = tmpPosition;
                                mClickListener.onItemClick(v, mSelectedPos, mDataList.get(mSelectedPos).getName());
                                notifyDataSetChanged();
                                return;
                            } else {
                                mStrengthSelectPos = position;
                            }
                        }
                    }

                    if (mClickListener != null) {
                        notifyItemChanged(mSelectedPos);
                        mSelectedPos = tmpPosition;
                        notifyItemChanged(mSelectedPos);
                        mClickListener.onItemClick(v, tmpPosition, item.getName());
                    }
                }
            });
        }
    }

    private void showStrength() {
        if (mTmpBeautyData != null && mTmpBeautyData.size() > 0) {
            for (int i = mTmpBeautyData.size() - 1; i >= 0; i--) {
                IFxInfo beautyInfo = mTmpBeautyData.get(i);
                if (beautyInfo == null) {
                    continue;
                }
                mDataList.add(1, beautyInfo);
            }
        }
        isExtendState = true;
        mSelectedPos = 0;
        mClickListener.onItemClick(null, mSelectedPos, mDataList.get(mSelectedPos).getName());
        mSelectedPos = mStrengthSelectPos;
        mClickListener.onItemClick(null, mSelectedPos, mDataList.get(mSelectedPos).getName());
        notifyDataSetChanged();
    }

    private void hideStrength() {
        for (int i = 0; i < mDataList.size(); i++) {
            IFxInfo beautyInfo = mDataList.get(1);
            if (beautyInfo.getName().equals(mContext.getResources().getString(R.string.strength_1))
                    || beautyInfo.getName().equals(mContext.getResources().getString(R.string.advanced_strength_1))
                    || beautyInfo.getName().equals(mContext.getResources().getString(R.string.advanced_strength_2))
                    || beautyInfo.getName().equals(mContext.getResources().getString(R.string.advanced_strength_3))
                    || beautyInfo.getName().equals(mContext.getResources().getString(R.string.blackPoint))) {
                IFxInfo remove = mDataList.remove(1);
                i--;
            }
        }
        mSelectedPos = 0;
        if (mClickListener != null) {
            mClickListener.onItemClick(null, mSelectedPos, mDataList.get(mSelectedPos).getName());
        }
        isExtendState = false;
    }

    public IFxInfo getSelectedBeautyTempData() {
        if (CommonUtils.isIndexAvailable(mStrengthSelectPos - 1, mTmpBeautyData)) {
            return mTmpBeautyData.get(mStrengthSelectPos - 1);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String name);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout shape_icon_layout;
        private ImageView shape_icon;
        private TextView shape_name;
        private View blue_point;


        public ViewHolder(View view) {
            super(view);
            shape_icon_layout = (RelativeLayout) view.findViewById(R.id.shape_icon_layout);
            shape_icon = (ImageView) view.findViewById(R.id.shape_icon);
            shape_name = (TextView) view.findViewById(R.id.shape_txt);
            blue_point = (View) view.findViewById(R.id.blue_point);
        }
    }


    public int getStrengthSelectPos() {
        return mStrengthSelectPos;
    }
}
