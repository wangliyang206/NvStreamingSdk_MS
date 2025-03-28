package com.meishe.sdkdemo.edit.Caption;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.adapter.SpaceItemDecoration;
import com.meishe.sdkdemo.edit.data.CaptionColorInfo;
import com.meishe.sdkdemo.edit.interfaces.OnItemClickListener;
import com.meishe.sdkdemo.utils.ScreenUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class CaptionOutlineFragment extends BaseFragment {
    private RecyclerView mCaptionOutlineRecyerView;
    private SeekBar mCaptonOutlineWidthSeekBar;
    private TextView mSeekBarOutlineWidthValue;
    private SeekBar mCaptonOutlineOpacitySeekBar;
    private TextView mSeekBarOutlineOpacityValue;
    private LinearLayout mApplyToAll;
    private ImageView mApplyToAllImage;
    private TextView mApplyToAllText;
    private boolean mIsApplyToAll = false;
    private CaptionOutlineRecyclerAdaper mCaptionOutlineRecycleAdapter;
    private ArrayList<CaptionColorInfo> mCaptionOutlineInfolist = new ArrayList<>();
    private OnCaptionOutlineListener mCaptionOutlineListener;
    private LinearLayout mContainer1;
    private LinearLayout mContainer2;

    public interface OnCaptionOutlineListener{
        void onFragmentLoadFinished();
        void onCaptionOutlineColor(int pos);
        void onCaptionOutlineWidth(int width);
        void onCaptionOutlineOpacity(int opacity);
        void onIsApplyToAll(boolean isApplyToAll);
    }
    public void setCaptionOutlineInfolist(ArrayList<CaptionColorInfo> captionOutlineInfolist) {
        this.mCaptionOutlineInfolist = captionOutlineInfolist;
        if (mCaptionOutlineRecycleAdapter != null)
            mCaptionOutlineRecycleAdapter.setCaptionOutlineColorList(captionOutlineInfolist);
    }
    public void setCaptionOutlineListener(OnCaptionOutlineListener captionOutlineListener) {
        this.mCaptionOutlineListener = captionOutlineListener;
    }

    public void updateCaptionOutlineWidthValue(float progress){
        mSeekBarOutlineWidthValue.setText(String.valueOf(progress));
        mCaptonOutlineWidthSeekBar.setProgress((int) progress);
    }

    public void updateCaptionOutlineOpacityValue(int progress){
        mSeekBarOutlineOpacityValue.setText(String.valueOf(progress));
        mCaptonOutlineOpacitySeekBar.setProgress(progress);
    }

    public void applyToAllCaption(boolean isApplyToAll){
        mApplyToAllImage.setImageResource(isApplyToAll ? R.mipmap.applytoall : R.mipmap.unapplytoall);
        mApplyToAllText.setTextColor(isApplyToAll ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ff909293"));
        mIsApplyToAll = isApplyToAll;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataSetChanged(int selectPosition){
        if (selectPosition > 0) {
            if (mContainer1.getVisibility() != View.VISIBLE) {
                mContainer1.setVisibility(View.VISIBLE);
            }
            if (mContainer2.getVisibility() != View.VISIBLE) {
                mContainer2.setVisibility(View.VISIBLE);
            }
        }
        mCaptionOutlineRecycleAdapter.notifyDataSetChanged();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootParent = inflater.inflate(R.layout.caption_outline_fragment, container, false);
        mCaptionOutlineRecyerView = (RecyclerView)rootParent.findViewById(R.id.captionOutlineRecyerView);
        mCaptonOutlineWidthSeekBar = (SeekBar)rootParent.findViewById(R.id.captonOutlineWidthSeekBar);
        mCaptonOutlineWidthSeekBar.setMax(16);
        mSeekBarOutlineWidthValue = (TextView) rootParent.findViewById(R.id.seekBarOutlineWidthValue);
        mCaptonOutlineOpacitySeekBar = (SeekBar)rootParent.findViewById(R.id.captonOutlineOpacitySeekBar);
        mCaptonOutlineOpacitySeekBar.setMax(100);
        mSeekBarOutlineOpacityValue = (TextView) rootParent.findViewById(R.id.seekBarOutlineOpacityValue);
        mApplyToAll = (LinearLayout)rootParent.findViewById(R.id.applyToAll);
        mApplyToAllImage = (ImageView)rootParent.findViewById(R.id.applyToAllImage);
        mApplyToAllText = (TextView)rootParent.findViewById(R.id.applyToAllText);
        mContainer1 = (LinearLayout)rootParent.findViewById(R.id.ll_seek_1);
        mContainer2 = (LinearLayout)rootParent.findViewById(R.id.ll_seek_2);
        mContainer1.setVisibility(View.GONE);
        mContainer2.setVisibility(View.GONE);
        return rootParent;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCaptionColorRecycleAdapter();
        initCaptionOutlineSeekBar();
        if(mCaptionOutlineListener != null){
            mCaptionOutlineListener.onFragmentLoadFinished();
        }
    }
    private void initCaptionColorRecycleAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mCaptionOutlineRecyerView.setLayoutManager(layoutManager);
        mCaptionOutlineRecycleAdapter = new CaptionOutlineRecyclerAdaper(getActivity());
        mCaptionOutlineRecycleAdapter.setCaptionOutlineColorList(mCaptionOutlineInfolist);
        mCaptionOutlineRecyerView.setAdapter(mCaptionOutlineRecycleAdapter);
        mCaptionOutlineRecyerView.addItemDecoration(new SpaceItemDecoration(0, ScreenUtils.dip2px(getActivity(),16)));
        mCaptionOutlineRecycleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                mIsApplyToAll=false;
                applyToAllCaption(false);
//                if(mCaptionOutlineListener != null){
//                    mCaptionOutlineListener.onIsApplyToAll(mIsApplyToAll);
//                }
                if (pos==0){
                    mContainer1.setVisibility(View.GONE);
                    mContainer2.setVisibility(View.GONE);
                }else{
                    mContainer1.setVisibility(View.VISIBLE);
                    mContainer2.setVisibility(View.VISIBLE);
                }
                if(mCaptionOutlineListener != null){
                    mCaptionOutlineListener.onCaptionOutlineColor(pos);
                }
            }
        });
    }
    private void initCaptionOutlineSeekBar() {
        mCaptonOutlineWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updateCaptionOutlineWidthValue(progress);
                    if(mCaptionOutlineListener != null){
                        mCaptionOutlineListener.onCaptionOutlineWidth(progress);
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
        mCaptonOutlineOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updateCaptionOutlineOpacityValue(progress);
                    if(mCaptionOutlineListener != null){
                        mCaptionOutlineListener.onCaptionOutlineOpacity(progress);
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

        mApplyToAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIsPartEdit()){
                    return;
                }
                mIsApplyToAll = !mIsApplyToAll;
                applyToAllCaption(mIsApplyToAll);
                if(mCaptionOutlineListener != null){
                    mCaptionOutlineListener.onIsApplyToAll(mIsApplyToAll);
                }
            }
        });
    }
}
