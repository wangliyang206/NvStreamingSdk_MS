package com.meishe.sdkdemo.edit.Caption;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.data.CaptionColorInfo;
import com.meishe.sdkdemo.edit.interfaces.OnItemClickListener;
import com.meishe.sdkdemo.edit.view.RoundColorView;

import java.util.ArrayList;

/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : jml
 * @CreateDate : 2020/6/22.
 * @Description :视频编辑-字幕-背景色-Adapter
 * @Description :VideoEdit-Caption-background-Adapter
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class CaptionBackgroundRecyclerAdaper extends RecyclerView.Adapter<CaptionBackgroundRecyclerAdaper.ViewHolder>  {
    private ArrayList<CaptionColorInfo> captionBackgroundColorList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener = null;
    public CaptionBackgroundRecyclerAdaper(Context context) {
        mContext = context;
    }
    public void setCaptionBackgroundColorList(ArrayList<CaptionColorInfo> captionBackgroundColorList) {
        this.captionBackgroundColorList = captionBackgroundColorList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_asset_caption_outline, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CaptionBackgroundRecyclerAdaper.ViewHolder holder, final int position) {
        final CaptionColorInfo colorInfo = captionBackgroundColorList.get(position);
        if(0 == position){
            holder.mCaptionOutlineNoColor.setVisibility(View.VISIBLE);
            holder.mCaptionOutlineColor.setVisibility(View.GONE);
        }else {
            holder.mCaptionOutlineNoColor.setVisibility(View.GONE);
            holder.mCaptionOutlineColor.setVisibility(View.VISIBLE);
            holder.mCaptionOutlineColor.setColor(Color.parseColor(colorInfo.mColorValue));
        }
        if(colorInfo.mSelected){
            if (position==0){
                holder.mCaptionOutlineNoColor.setBackgroundResource(R.mipmap.ic_caption_no_select);
            }else{
                holder.mSelecteItem.setVisibility(View.VISIBLE);
            }
        }else {
            holder.mSelecteItem.setVisibility(View.GONE);
            if (position==0){
                holder.mCaptionOutlineNoColor.setBackgroundResource(R.mipmap.ic_caption_no);
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return captionBackgroundColorList.size();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mCaptionOutlineNoColor;
        RoundColorView mCaptionOutlineColor;
        View mSelecteItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mCaptionOutlineNoColor = (ImageView)itemView.findViewById(R.id.captionOutlineNoColor);
            mCaptionOutlineColor = (RoundColorView)itemView.findViewById(R.id.captionOutlineColor);
            mSelecteItem = itemView.findViewById(R.id.selectedItem);
        }
    }
}
