package com.meishe.sdkdemo.edit.watermark;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by CaoZhiChao on 2018/10/16 15:28
 */
public interface OnItemClickListener {
    void onItemClick(RecyclerView.ViewHolder holder, int position, int pictureType, String picturePath, int waterMarkType, String waterMarkPicture);

    void onEffectClick(RecyclerView.ViewHolder holder, int position, EffectItemData effectItemData);
}
