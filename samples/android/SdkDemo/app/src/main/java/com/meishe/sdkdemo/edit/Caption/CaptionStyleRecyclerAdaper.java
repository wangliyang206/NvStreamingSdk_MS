package com.meishe.sdkdemo.edit.Caption;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.data.AssetItem;
import com.meishe.sdkdemo.edit.interfaces.OnItemClickListener;
import com.meishe.sdkdemo.utils.asset.NvAsset;

import java.util.ArrayList;

/**
 * Created by admin on 2018/5/25.
 */

public class CaptionStyleRecyclerAdaper extends RecyclerView.Adapter<CaptionStyleRecyclerAdaper.ViewHolder>  {
    private ArrayList<AssetItem> mAssetList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener = null;
    private int mSelectedPos = 0;
    public CaptionStyleRecyclerAdaper(Context context) {
        mContext = context;
    }

    public void setAssetList(ArrayList<AssetItem> assetArrayList) {
        this.mAssetList = assetArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_asset_caption_style, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }
    public void setSelectedPos(int selectedPos) {
        this.mSelectedPos = selectedPos;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        AssetItem assetItem = mAssetList.get(position);
        if(assetItem == null)
            return;
        NvAsset asset = assetItem.getAsset();
        if(asset == null)
            return;
        if(assetItem.getAssetMode() == AssetItem.ASSET_NONE){
            holder.mCaptionAssetCover.setImageResource(assetItem.getImageRes());
        }else {
            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.placeholder(R.mipmap.default_caption);
            if (!TextUtils.isEmpty(asset.coverUrl) && asset.coverUrl.endsWith(".webp")) {
                String imageUrl = asset.coverUrl.replace("file:///android_asset/", "asset://android_asset/");
                Uri uri = Uri.parse(imageUrl);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .setOldController(holder.mCaptionAssetCover.getController())
                        .build();
                holder.mCaptionAssetCover.setController(controller);
            } else {
                Glide.with(mContext)
                        .asBitmap()
                        .load(asset.coverUrl)
                        .apply(options)
                        .into(holder.mCaptionAssetCover);
            }
        }

        holder.mCaptionStyleName.setText(asset.name);
        holder.mSelecteItem.setVisibility(mSelectedPos == position ? View.VISIBLE : View.GONE);
        holder.mCaptionStyleName.setTextColor(mSelectedPos == position ? Color.parseColor("#ff4a90e2")
                : Color.parseColor("#ffffffff"));

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v,position);
                if(mSelectedPos == position)
                    return;
                notifyItemChanged(mSelectedPos);
                mSelectedPos = position;
                notifyItemChanged(mSelectedPos);

            }
        });
        holder.mCaptionStyleName.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return mAssetList.size();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView mCaptionAssetCover;
        View mSelecteItem;
        TextView mCaptionStyleName;
        public ViewHolder(View itemView) {
            super(itemView);
            mCaptionAssetCover = itemView.findViewById(R.id.captionStyleAssetCover);
            mSelecteItem = itemView.findViewById(R.id.selectedItem);
            mCaptionStyleName = (TextView)itemView.findViewById(R.id.captionStyleName);
        }
    }
}
