package com.meishe.sdkdemo.edit.Caption;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.adapter.SpaceItemDecoration;
import com.meishe.sdkdemo.edit.data.AssetItem;
import com.meishe.sdkdemo.edit.interfaces.OnItemClickListener;
import com.meishe.sdkdemo.utils.asset.NvAsset;

import java.util.ArrayList;

public class CaptionRichWordFragment extends Fragment {
    private RecyclerView mRvCaptionList;
    private RichWordAdapter mCaptionAdapter;
    private ArrayList<AssetItem> mAssetList;
    private ImageView mIvLoadMore;
    private TextView mTvLoadMore;
    private OnCaptionStateListener mCaptionStateListener;

    public interface OnCaptionStateListener {
        void onFragmentLoadFinished();

        void onLoadMore();

        void onItemClick(int pos);
    }

    public void setCaptionStateListener(OnCaptionStateListener stateListener) {
        this.mCaptionStateListener = stateListener;
    }

    public void setSelectedPos(int selectedPos) {
        if (mCaptionAdapter != null)
            mCaptionAdapter.setSelectedPos(selectedPos);
    }

    public void setAssetInfoList(ArrayList<AssetItem> assetItems) {
        mAssetList = assetItems;
        if (mCaptionAdapter != null){
            mCaptionAdapter.setAssetList(assetItems);
            mCaptionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.caption_rich_word_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        if (mCaptionStateListener != null) {
            mCaptionStateListener.onFragmentLoadFinished();
        }
    }

    private void initView(View view) {
        mIvLoadMore = view.findViewById(R.id.iv_load_more);
        mTvLoadMore = view.findViewById(R.id.tv_load_more);
        mRvCaptionList = view.findViewById(R.id.rv_list);

        mIvLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCaptionStateListener != null){
                    mCaptionStateListener.onLoadMore();
                }
            }
        });
        mTvLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCaptionStateListener != null){
                    mCaptionStateListener.onLoadMore();
                }
            }
        });
    }

    private void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRvCaptionList.setLayoutManager(layoutManager);
        mCaptionAdapter = new RichWordAdapter(getActivity());
        mCaptionAdapter.setAssetList(mAssetList);
        mRvCaptionList.setAdapter(mCaptionAdapter);
        mRvCaptionList.addItemDecoration(new SpaceItemDecoration(0, 15));
        mCaptionAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (mCaptionStateListener != null) {
                    mCaptionStateListener.onItemClick(pos);
                }
            }
        });
    }

    static class RichWordAdapter extends RecyclerView.Adapter<RichWordAdapter.ViewHolder> {
        private ArrayList<AssetItem> mAssetList = new ArrayList<>();
        private Context mContext;
        private OnItemClickListener mOnItemClickListener = null;
        private int mSelectedPos = 0;

        RichWordAdapter(Context context) {
            mContext = context;
        }

        void setAssetList(ArrayList<AssetItem> assetArrayList) {
            this.mAssetList = assetArrayList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_simple_caption, parent, false);
            return new ViewHolder(v);
        }

        public void setSelectedPos(int selectedPos) {
            if (selectedPos >= 0 && mAssetList != null && selectedPos < mAssetList.size()) {
                notifyItemChanged(mSelectedPos);
                this.mSelectedPos = selectedPos;
                notifyItemChanged(mSelectedPos);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            AssetItem assetItem = mAssetList.get(position);
            if (assetItem == null)
                return;
            NvAsset asset = assetItem.getAsset();
            if (asset == null)
                return;
            if (assetItem.getAssetMode() == AssetItem.ASSET_NONE) {
                holder.mIvCover.setImageResource(assetItem.getImageRes());
            } else {
                RequestOptions options = new RequestOptions();
                options.centerCrop();
                options.placeholder(R.mipmap.default_caption);
                Glide.with(mContext)
                        .asBitmap()
                        .load(asset.coverUrl)
                        .apply(options)
                        .into(holder.mIvCover);
            }

            holder.mTvName.setText(asset.name);
            holder.vSelected.setVisibility(mSelectedPos == position ? View.VISIBLE : View.GONE);
            holder.mTvName.setTextColor(mSelectedPos == position ? mContext.getResources().getColor(R.color.red_ff64)
                    : mContext.getResources().getColor(R.color.white));
            holder.mTvName.setSelected(true);
        }

        @Override
        public int getItemCount() {
            return mAssetList.size();
        }

        public void setOnItemClickListener(OnItemClickListener itemClickListener) {
            this.mOnItemClickListener = itemClickListener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mIvCover;
            View vSelected;
            TextView mTvName;

            ViewHolder(View itemView) {
                super(itemView);
                mIvCover = itemView.findViewById(R.id.iv_cover);
                vSelected = itemView.findViewById(R.id.v_select);
                mTvName = itemView.findViewById(R.id.tv_name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemClickListener != null)
                            mOnItemClickListener.onItemClick(v, getAdapterPosition());
                        if (mSelectedPos == getAdapterPosition())
                            return;
                        notifyItemChanged(mSelectedPos);
                        mSelectedPos = getAdapterPosition();
                        notifyItemChanged(mSelectedPos);
                    }
                });
            }
        }
    }
}
