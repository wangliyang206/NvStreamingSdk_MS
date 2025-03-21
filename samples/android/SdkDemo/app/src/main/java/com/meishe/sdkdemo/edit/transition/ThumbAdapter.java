package com.meishe.sdkdemo.edit.transition;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.meishe.base.view.RoundImageView;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.data.FilterItem;
import com.meishe.sdkdemo.utils.dataInfo.TransitionInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ThumbAdapter extends RecyclerView.Adapter<ThumbAdapter.ViewHolder> {
    private Context mContext;
    private OnItemClickListener mClickListener;
    // private FilterItem mFilterItem;
    private List<TransitionInfo> mFilterItems;
    private List<String> fileData = new ArrayList<>();
    private int mSelectedPos = 0;

    public interface OnItemClickListener {
        void onThumbItemClick(View view, int position);

        void onTransitionItemClick(View view, int position);
    }


    public int getSelectPos() {
        return mSelectedPos;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout item_assetLayout;
        private RoundImageView item_thumbView;
        private SimpleDraweeView item_transitionImage;

        public ViewHolder(View view) {
            super(view);
            item_assetLayout = (LinearLayout) view.findViewById(R.id.layoutAsset);
            item_thumbView = (RoundImageView) view.findViewById(R.id.imageThumbView);
            item_transitionImage = (SimpleDraweeView) view.findViewById(R.id.imageTransition);
        }
    }

    public ThumbAdapter(Context context, List<String> fileData, List<TransitionInfo> items) {
        if (fileData != null) {
            this.fileData = fileData;
        }
        if (items != null) {
            this.mFilterItems = items;
        }
        mContext = context;
    }

    public void setTransitionFilterItem(List<TransitionInfo> items) {
        if (items != null) {
            this.mFilterItems = items;
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumb, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String filePath = fileData.get(position);
        Glide.with(mContext).asBitmap().load(filePath).into(holder.item_thumbView);

        if ((mFilterItems != null) && !mFilterItems.isEmpty()) {
            int countFilterItems = mFilterItems.size();
            if (position < countFilterItems) {
                int filterMode = mFilterItems.get(position).getTransitionMode();
                if (filterMode == FilterItem.FILTERMODE_BUILTIN) {
                    int imageId = mFilterItems.get(position).getM_imageId();
                    if (imageId != 0) {
                        holder.item_transitionImage.setImageResource(imageId);
                    }
                }

                String imageUrl = mFilterItems.get(position).getM_imageUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    if (imageUrl.endsWith(".webp")) {
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setUri(imageUrl)
                                .setAutoPlayAnimations(true)
                                .setOldController(holder.item_transitionImage.getController())
                                .build();
                        holder.item_transitionImage.setController(controller);
                    } else if ((filterMode == FilterItem.FILTERMODE_BUNDLE) || (filterMode == FilterItem.FILTERMODE_PACKAGE)) {
                        RequestOptions options = new RequestOptions();
                        options.centerCrop();
                        options.diskCacheStrategy(DiskCacheStrategy.NONE);
                        options.dontAnimate();
                        Glide.with(mContext)
                                .asBitmap()
                                .load(imageUrl)
                                .apply(options)
                                .into(holder.item_transitionImage);
                    }
                }
            }
        }

        if (position == fileData.size() - 1) {
            holder.item_transitionImage.setVisibility(View.INVISIBLE);
        } else {
            holder.item_transitionImage.setVisibility(View.VISIBLE);
        }

        if (mSelectedPos == position) {
            holder.item_transitionImage.setBackground(ContextCompat.getDrawable(mContext, R.drawable.fx_item_radius_border_select2));
        } else {
            holder.item_transitionImage.setBackground(ContextCompat.getDrawable(mContext, R.drawable.fx_item_radius_shape_unselect2));
        }

        holder.item_transitionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onTransitionItemClick(view, position);
                }
                if (mSelectedPos == position) {
                    return;
                }
                notifyItemChanged(mSelectedPos);
                mSelectedPos = position;
                notifyItemChanged(mSelectedPos);
            }
        });

        holder.item_thumbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onThumbItemClick(view, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileData.size();
    }
}
