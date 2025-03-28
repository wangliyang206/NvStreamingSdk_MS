package com.meishe.sdkdemo.edit.Caption;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.adapter.SpaceItemDecoration;
import com.meishe.sdkdemo.edit.data.AssetItem;

import java.util.ArrayList;

/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : yyj
 * @CreateDate : 2019/6/22.
 * @Description :视频编辑-字幕-字体-Fragment
 * @Description :VideoEdit-Caption-Font-Fragment
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class CaptionFontFragment extends BaseFragment {
    private RecyclerView mCaptionFontRecycleView;
    private CaptionFontRecyclerAdaper mCaptionFontRecycleAdapter;
    private Button mBoldButton;
    private Button mItalicButton;
    private Button mShadowButton;
    private LinearLayout mApplyToAll;
    private ImageView mApplyToAllImage;
    private TextView mApplyToAllText;
    private boolean mIsBold = false;
    private boolean mIsItalic = false;
    private boolean mIsShadow = false;
    private boolean mIsUnderline = false;
    private boolean mIsApplyToAll = false;
    private ArrayList<AssetItem> mCaptionFontInfolist = new ArrayList<>();
    //private RelativeLayout mDownloadMoreCapionSytle;
    private OnCaptionFontListener mCaptionFontListener;
    private Button mUnderlineButton;
    private SeekBar mCaptonOpacitySeekBar;
    private TextView mSeekBarOpacityValue;
    private View mLlCaptionSeekViewContainer;

    public interface OnCaptionFontListener{
        void onFragmentLoadFinished();
        void onItemClick(int pos);
        void onBold(boolean mIsBold);
        void onItalic(boolean mIsItalic);
        void onShadow();
        void onUnderline(boolean underline);
        void onIsApplyToAll(boolean isApplyToAll);
        void onFontDownload(int pos);
        void onChangeOpacity(int progress);

    }
    public void setCaptionFontListener(OnCaptionFontListener captionFontListener) {
        this.mCaptionFontListener = captionFontListener;
    }

    public void setSelectedPos(int selectedPos){
        if (mCaptionFontRecycleAdapter != null)
            mCaptionFontRecycleAdapter.setSelectedPos(selectedPos);
    }
    public void setFontInfolist(ArrayList<AssetItem> fontInfolist) {
        mCaptionFontInfolist = fontInfolist;
        if (mCaptionFontRecycleAdapter != null)
            mCaptionFontRecycleAdapter.setAssetInfoList(fontInfolist);
    }

    public void applyToAllCaption(boolean isApplyToAll){
        mApplyToAllImage.setImageResource(isApplyToAll ? R.mipmap.applytoall : R.mipmap.unapplytoall);
        mApplyToAllText.setTextColor(isApplyToAll ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ff909293"));
        mIsApplyToAll = isApplyToAll;
    }
    public void updateBoldButton(boolean isBold){
        mBoldButton.setBackgroundResource(isBold ? R.drawable.shape_caption_font_corner_button_selected : R.drawable.shape_caption_font_corner_button);
        mBoldButton.setTextColor(isBold ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ffffffff"));
        mIsBold = isBold;
    }
    public void updateItalicButton(boolean isItalic){
        mItalicButton.setBackgroundResource(isItalic ? R.drawable.shape_caption_font_corner_button_selected : R.drawable.shape_caption_font_corner_button);
        mItalicButton.setTextColor(isItalic ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ffffffff"));
        mIsItalic = isItalic;
    }
    public void updateShadowButton(boolean isShadow){
        mShadowButton.setBackgroundResource(isShadow ? R.drawable.shape_caption_font_corner_button_selected : R.drawable.shape_caption_font_corner_button);
        mShadowButton.setTextColor(isShadow ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ffffffff"));
        mIsShadow = isShadow;
    }
    public void updateUnderlineButton(boolean isUnderline){
        mUnderlineButton.setBackgroundResource(isUnderline ? R.drawable.shape_caption_font_corner_button_selected : R.drawable.shape_caption_font_corner_button);
        mUnderlineButton.setTextColor(isUnderline ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ffffffff"));
        mIsUnderline = isUnderline;
    }

    public void notifyDataSetChanged(){
        if(mCaptionFontRecycleAdapter != null)
            mCaptionFontRecycleAdapter.notifyDataSetChanged();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootParent = inflater.inflate(R.layout.caption_font_list_fragment, container, false);
        //mDownloadMoreCapionSytle = (RelativeLayout) rootParent.findViewById(R.id.download_more);
        mCaptionFontRecycleView = (RecyclerView) rootParent.findViewById(R.id.captionFontRecycleView);
        mBoldButton = (Button) rootParent.findViewById(R.id.boldButton);
        mItalicButton = (Button) rootParent.findViewById(R.id.italicButton);
        mShadowButton = (Button) rootParent.findViewById(R.id.shadowButton);
        mUnderlineButton = (Button) rootParent.findViewById(R.id.underlineButton);
        mApplyToAll = (LinearLayout)rootParent.findViewById(R.id.applyToAll);
        mApplyToAllImage = (ImageView)rootParent.findViewById(R.id.applyToAllImage);
        mApplyToAllText = (TextView)rootParent.findViewById(R.id.applyToAllText);


        mCaptonOpacitySeekBar = (SeekBar)rootParent.findViewById(R.id.captonOpacitySeekBar);
        mCaptonOpacitySeekBar.setMax(100);
        mSeekBarOpacityValue = (TextView) rootParent.findViewById(R.id.seekBarOpacityValue);
        mLlCaptionSeekViewContainer =  rootParent.findViewById(R.id.ll_caption_sub);
        updateCaptionOpacityValue(100);
        return rootParent;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAssetRecycleAdapter();
        if(mCaptionFontListener != null){
            mCaptionFontListener.onFragmentLoadFinished();
        }
    }

    private void initAssetRecycleAdapter() {
        mBoldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsApplyToAll=false;
                applyToAllCaption(false);
//                if(mCaptionFontListener != null){
//                    mCaptionFontListener.onIsApplyToAll(mIsApplyToAll);
//                }

                mIsBold = !mIsBold;
                updateBoldButton(mIsBold);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onBold(mIsBold);
                }
            }
        });
        mItalicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsApplyToAll=false;
                applyToAllCaption(false);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onIsApplyToAll(mIsApplyToAll);
                }

                mIsItalic = !mIsItalic;
                updateItalicButton(mIsItalic);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onItalic(mIsItalic);
                }
            }
        });
        mShadowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsApplyToAll=false;
                applyToAllCaption(false);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onIsApplyToAll(mIsApplyToAll);
                }

                mIsShadow = !mIsShadow;
                updateShadowButton(mIsShadow);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onShadow();
                }
            }
        });
        mUnderlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsApplyToAll=false;
                applyToAllCaption(false);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onIsApplyToAll(mIsApplyToAll);
                }

                mIsUnderline = !mIsUnderline;
                updateUnderlineButton(mIsUnderline);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onUnderline(mIsUnderline);
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mCaptionFontRecycleView.setLayoutManager(layoutManager);
        mCaptionFontRecycleAdapter = new CaptionFontRecyclerAdaper(getActivity());
        mCaptionFontRecycleAdapter.setAssetInfoList(mCaptionFontInfolist);
        mCaptionFontRecycleView.setAdapter(mCaptionFontRecycleAdapter);
        mCaptionFontRecycleView.addItemDecoration(new SpaceItemDecoration(0, 8));
        mCaptionFontRecycleAdapter.setOnItemClickListener(new CaptionFontRecyclerAdaper.OnFontItemClickListener() {
            @Override
            public void onItemDownload(View view, int position) {
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onFontDownload(position);
                }
            }

            @Override
            public void onItemClick(View view, int pos) {
                mIsApplyToAll=false;
                applyToAllCaption(false);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onIsApplyToAll(mIsApplyToAll);
                }
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onItemClick(pos);
                }
            }
        });

        mApplyToAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIsPartEdit()){
                    return;
                }
                mIsApplyToAll = !mIsApplyToAll;
                applyToAllCaption(mIsApplyToAll);
                if(mCaptionFontListener != null){
                    mCaptionFontListener.onIsApplyToAll(mIsApplyToAll);
                }
            }
        });

        mCaptonOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updateCaptionOpacityValue(progress);
                    if(mCaptionFontListener != null){
                        mCaptionFontListener.onChangeOpacity(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void updateCaptionOpacityValue(int progress){
        mSeekBarOpacityValue.setText(String.valueOf(progress));
        mCaptonOpacitySeekBar.setProgress(progress);
    }

    public void showSeekContainerVisible(int visible){
        if (mLlCaptionSeekViewContainer!=null){
            mLlCaptionSeekViewContainer.setVisibility(visible);
        }
    }
}
