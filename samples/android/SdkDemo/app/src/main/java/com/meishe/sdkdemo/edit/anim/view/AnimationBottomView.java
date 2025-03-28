package com.meishe.sdkdemo.edit.anim.view;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.meishe.base.view.MagicSeekBar;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.anim.AnimationAdapter;
import com.meishe.sdkdemo.edit.data.FilterItem;
import com.meishe.utils.ToastUtil;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.asset.NvAsset;
import com.meishe.sdkdemo.utils.asset.NvAssetManager;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author :Jml
 * @date :2020/8/25 10:37
 * @des :
 */
public class AnimationBottomView extends LinearLayout {
    private RecyclerView mAnimationRecyclerView;
    private ImageView mAnimationAssetFinish;
    private RelativeLayout mLoadMore;
    private LinearLayout mProgressLayout;
    private MagicSeekBar mAnimationSeekBar;
    private AnimationAdapter mAnimationAdapter;
    private ArrayList<FilterItem> mAnimationDataList;
    private NvAssetManager mAssetManager;
    private int mAssetType = NvAsset.ASSET_ANIMATION_IN;
    private OnFunctionListener mOnFunctionListener;
    private int mSelectedClipPosition = 0;

    public AnimationBottomView(Context context) {
        this(context,null);

    }

    public AnimationBottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initData(context);
        initAnimationViewList(context);
    }

    private void initData(Context context) {
        initAnimationDataList(context);
    }

    private void initView(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.activity_animation_bottom,this);
        mAnimationRecyclerView = root.findViewById(R.id.animation_list);
        mAnimationAssetFinish = root.findViewById(R.id.animationAssetFinish);
        mLoadMore = root.findViewById(R.id.download_more_btn);
        mProgressLayout = root.findViewById(R.id.ll_progress);
        mAnimationSeekBar = root.findViewById(R.id.animation_seekBar);
        mAnimationAssetFinish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mOnFunctionListener){
                    mOnFunctionListener.onConfirm();
                }
            }
        });
        mLoadMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mOnFunctionListener){
                    int categoryId = getCategoryId();
                    mOnFunctionListener.onLoadMore(categoryId);
                }
            }
        });
       /* mAnimationSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                if(null == seekParams ){
                    return;
                }
                boolean fromUser = seekParams.fromUser;
                if(fromUser && null != mOnFunctionListener){
                    String packageId = mAnimationDataList.get(mAnimationAdapter.getSelectedPosition()).getPackageId();
                    mOnFunctionListener.onSeekChanged(packageId,seekParams.progressFloat);
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });*/
       mAnimationSeekBar.setOnProgressChangeListener(new MagicSeekBar.OnProgressChangeListener() {
           @Override
           public void onProgressChange(long progress, boolean fromUser) {
               if(!fromUser && null != mOnFunctionListener){
                   mOnFunctionListener.onSeekChanged(mAnimationDataList.get(mAnimationAdapter.getSelectedPosition()),progress);
               }
           }
       });
    }

    public void initAnimationDataList(Context context) {
        FilterItem selectFilterItem =null;
        if (mAnimationAdapter != null) {
            selectFilterItem = mAnimationAdapter.getSelectFilterItem();
        }
        mAnimationDataList = new ArrayList<>();

        FilterItem filterItem = new FilterItem();
        filterItem.setFilterMode(FilterItem.FILTERMODE_BUILTIN);

//        if (isZh(context)){
//            filterItem.setFilterName("无");
//        }else {
//            filterItem.setFilterName("None");
//        }
        filterItem.setFilterName(context.getResources().getString(R.string.NO_FX));
        filterItem.setImageId(R.mipmap.none);
        mAnimationDataList.add(filterItem);

        ArrayList<NvAsset> animationList = getLocalData();
        if (isZh(context)){
            String bundlePath = "animation/in/info_Zh.txt";
            if(mAssetType == NvAsset.ASSET_ANIMATION_OUT){
                bundlePath = "animation/out/info_Zh.txt";
            }else if(mAssetType == NvAsset.ASSET_ANIMATION_COMPANY){
                bundlePath = "animation/company/info_Zh.txt";
            }
            Util.getBundleFilterInfo(context, animationList, bundlePath);
        }else {
            String bundlePath = "animation/in/info.txt";
            if(mAssetType == NvAsset.ASSET_ANIMATION_OUT){
                bundlePath = "animation/out/info.txt";
            }else if(mAssetType == NvAsset.ASSET_ANIMATION_COMPANY){
                bundlePath = "animation/company/info.txt";
            }
            Util.getBundleFilterInfo(context, animationList, bundlePath);
        }
        int ratio = TimelineData.instance().getMakeRatio();
        for (NvAsset asset : animationList) {
            if ((ratio & asset.aspectRatio) == 0)
                continue;

            FilterItem newFilterItem = new FilterItem();
            if (asset.isReserved()) {
                /*
                 * 加载assets/animation文件夹下的图片
                 * Load images in the assets / animation folder
                 * */
                String coverPath = "animation/in/";
                if(mAssetType == NvAsset.ASSET_ANIMATION_OUT){
                    coverPath = "animation/out/";
                }else if(mAssetType == NvAsset.ASSET_ANIMATION_COMPANY){
                    coverPath = "animation/company/";
                }
                coverPath += asset.uuid;
                coverPath = com.meishe.utils.PathUtils.getAssetFileBySuffixPic(coverPath);
                if (coverPath.endsWith("webp")) {
                    asset.coverUrl = "asset://android_asset/"+coverPath;
                } else{
                    asset.coverUrl = "file:///android_asset/" + coverPath;
                }
            }
            newFilterItem.setFilterMode(FilterItem.FILTERMODE_PACKAGE);
            newFilterItem.setFilterName(asset.name);
            newFilterItem.setPackageId(asset.uuid);
            newFilterItem.setImageUrl(asset.coverUrl);
            newFilterItem.setPostPackage(asset.isPostPackage == 1);
            mAnimationDataList.add(newFilterItem);
        }
        if(null != mAnimationAdapter){
            mAnimationAdapter.setAnimationDataList(mAnimationDataList);
            mAnimationAdapter.setSelectItem(selectFilterItem);
            mAnimationAdapter.notifyDataSetChanged();
        }
    }

    private void initAnimationViewList(final Context context) {
        mAnimationAdapter = new AnimationAdapter(context);
        mAnimationAdapter.setAnimationDataList(mAnimationDataList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mAnimationRecyclerView.setLayoutManager(linearLayoutManager);
        mAnimationRecyclerView.setAdapter(mAnimationAdapter);

        mAnimationAdapter.setOnItemClickListener(new AnimationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(null != mOnFunctionListener){
                    if(mSelectedClipPosition == -1){
                        ToastUtil.showToast(context,context.getString(R.string.select_videoClip_first));
                        return;
                    }
                    mAnimationAdapter.setSelectPos(position);
                    if(position >=0 && position< mAnimationDataList.size()){
                        FilterItem filterItem = mAnimationDataList.get(position);
                        mOnFunctionListener.onItemClick(filterItem,position,mAnimationSeekBar.getProgress(),mAssetType);
                    }
                    if(position == 0){
                        mProgressLayout.setVisibility(INVISIBLE);
                    }else {
                        mProgressLayout.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    private ArrayList<NvAsset> getLocalData() {
        mAssetManager = NvAssetManager.sharedInstance();
        mAssetManager.searchLocalAssets(mAssetType);
        String bundlePath = "animation/in";
        if(mAssetType == NvAsset.ASSET_ANIMATION_OUT){
            bundlePath = "animation/out";
        }else if(mAssetType == NvAsset.ASSET_ANIMATION_COMPANY){
            bundlePath = "animation/company";
        }
        mAssetManager.searchReservedAssets(mAssetType, bundlePath);
        return mAssetManager.getUsableAssets(mAssetType, NvAsset.AspectRatio_All, 0);
    }

    private int getCategoryId() {
        int categoryId = NvAsset.NV_CATEGORY_ID_ANIMATION_IN;
        if(mAssetType == NvAsset.ASSET_ANIMATION_OUT){
            categoryId = NvAsset.NV_CATEGORY_ID_ANIMATION_OUT;
        }else if(mAssetType == NvAsset.ASSET_ANIMATION_COMPANY){
            categoryId = NvAsset.NV_CATEGORY_ID_ANIMATION_COMPANY;
        }
        return categoryId;
    }


    public void setFunctionListener(OnFunctionListener onFunctionListener){
        this.mOnFunctionListener = onFunctionListener;
    }

    public void setAssetType(int type){
        this.mAssetType = type;
        initAnimationDataList(getContext());

    }

    /**
     * 设置选中的packageID, 设置选中样式
     * Set the selected packageID, set the selected style
     * @param packageId
     */
    public void setSelectedPackageId(String packageId) {
        int selectedPosition = 0;
        if(!TextUtils.isEmpty(packageId)){
            for(int i = 0;i <mAnimationDataList.size();i++){
                FilterItem filterItem = mAnimationDataList.get(i);
                String itemId = filterItem.getPackageId();
                if(!TextUtils.isEmpty(itemId) && itemId.equals(packageId)){
                    selectedPosition = i;
                    break;
                }
            }
        }
        if(null != mAnimationAdapter){
            mAnimationAdapter.setSelectPos(selectedPosition);
            if(selectedPosition == 0){
                mProgressLayout.setVisibility(INVISIBLE);
            }else{
                mProgressLayout.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 设置片段的时间最大值
     * Set the maximum time of the clip
     * @param clipDuration
     */
    public void setMaxProgress(long clipDuration) {
        mAnimationSeekBar.setMax(clipDuration);
    }

    public void setSelectedProgress(long value) {
        mAnimationSeekBar.setProgress(value);
    }

    public void setSelectedClipPosition(int mSelectedClipPosition) {
        this.mSelectedClipPosition = mSelectedClipPosition ;
        if(mSelectedClipPosition == -1){
            mAnimationSeekBar.setEnabled(false);
        }else{
            mAnimationSeekBar.setEnabled(true);
        }
    }

    public ArrayList<FilterItem> getCurrentAnimationList() {
        return mAnimationDataList;
    }

    public long getMaxProgress() {
        return mAnimationSeekBar.getMax() ;
    }

    public interface OnFunctionListener{
        void onItemClick(FilterItem filterItem, int position,float duration,int animationType);
        void onConfirm();
        void onLoadMore(int categoryId);
        void onSeekChanged(FilterItem filterItem,long progress);
    }
}
