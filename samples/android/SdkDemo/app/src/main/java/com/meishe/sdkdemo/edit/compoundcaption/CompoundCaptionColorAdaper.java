package com.meishe.sdkdemo.edit.compoundcaption;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.interfaces.OnItemClickListener;
import com.meishe.sdkdemo.edit.view.RoundColorView;

import java.util.ArrayList;

/**
 * Created by admin on 2018/5/25.
 */

public class CompoundCaptionColorAdaper extends RecyclerView.Adapter<CompoundCaptionColorAdaper.ViewHolder>  {
    private ArrayList<String> mCaptionColorList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener = null;

    private int mSelectedPos = -1;

    public CompoundCaptionColorAdaper(Context context) {
        mContext = context;
    }

    public void setSelectedPos(int selectedPos) {
        this.mSelectedPos = selectedPos;
    }

    public void setCaptionColorList(ArrayList<String> captionColorList) {
        this.mCaptionColorList = captionColorList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_asset_caption_color, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String colorInfo = mCaptionColorList.get(position);
        holder.mCaptionColor.setColor(Color.parseColor(colorInfo));
        if(mSelectedPos == position){
            holder.mSelecteItem.setVisibility(View.VISIBLE);
        }else {
            holder.mSelecteItem.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if( mSelectedPos == position){
                    return;
                }
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(v,position);
                }
                if(mSelectedPos >= 0){
                    notifyItemChanged(mSelectedPos);
                }
                mSelectedPos = position;
                notifyItemChanged(mSelectedPos);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mCaptionColorList.size();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RoundColorView mCaptionColor;
        View mSelecteItem;
        public ViewHolder(View itemView) {
            super(itemView);
            mCaptionColor = (RoundColorView)itemView.findViewById(R.id.captionColor);
            mSelecteItem = itemView.findViewById(R.id.selectedItem);
        }
    }
}
