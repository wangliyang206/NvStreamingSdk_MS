package com.meishe.sdkdemo.edit.compoundcaption;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.edit.adapter.SpaceItemDecoration;
import com.meishe.sdkdemo.edit.data.CaptionColorInfo;
import com.meishe.sdkdemo.edit.interfaces.OnItemClickListener;
import com.meishe.sdkdemo.utils.ScreenUtils;

import java.util.ArrayList;

/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : jml
 * @CreateDate : 2020/6/22.
 * @Description :视频编辑-字幕-背景色-Fragment
 * @Description :VideoEdit-Caption-background-Fragment
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class CompoundCaptionBackgroundFragment extends Fragment {
    private RecyclerView mCaptionBackgroundRecyclerView;
    private CompoundCaptionBackgroundRecyclerAdaper mCaptionBackgroundRecycleAdapter;
    private SeekBar mCaptonOpacitySeekBar;
    private TextView mSeekBarOpacityValue;
    private OnCaptionBackgroundListener captionBackgroundListener;
    private ArrayList<CaptionColorInfo> mCaptionColorInfolist = new ArrayList<>();

    /**
     * 背景操作回调
     * Background operation callback
     */
    public interface OnCaptionBackgroundListener{
        void onFragmentLoadFinished();
        void onCaptionColor(int pos);
        void onCaptionOpacity(int progress);
        void onCaptionCorner(int progress);
        void onCaptionPadding(int progress);
        void onIsApplyToAll(boolean isApplyToAll);
    }

    public void setCaptionBackgroundListener(OnCaptionBackgroundListener captionBackgroundListener) {
        this.captionBackgroundListener = captionBackgroundListener;
    }
    public void updateCaptionOpacityValue(int progress){
        mSeekBarOpacityValue.setText(String.valueOf(progress));
        mCaptonOpacitySeekBar.setProgress(progress);
    }

    /**
     * 更新设置圆角的进度
     * Update the progress of setting round corners
     * @param progress 获取圆角的值
     *
     */
    public void updateCaptionCornerValue(int progress){

        //mSeekBarCornerValue.setText(String.valueOf(progress));
        //mCaptonCornerSeekBar.setProgress(progress);
    }

    /**
     * 更新设置边距的进度
     * Update the progress of setting round corners
     * @param progress 获取圆角的值
     *
     */
    public void updateCaptionPaddingValue(int progress){

        //mSeekBarMarginValue.setText(String.valueOf(progress/(float)100));
        //mCaptionMarginSeekBar.setProgress(progress);
    }

    /**
     * 设置最大进度
     * Set maximum progress
     *  @param max 最大值(原则上是高度的一半)
     *            Maximum value (in principle, half of the height)
     */
    public void initCaptionMaxCorner(int max){
        //mCaptonCornerSeekBar.setMax(max);
    }

    public void setCaptionBackgroundInfolist(ArrayList<CaptionColorInfo> captionColorInfolist) {
        this.mCaptionColorInfolist = captionColorInfolist;
        if(mCaptionBackgroundRecycleAdapter != null){
            mCaptionBackgroundRecycleAdapter.setCaptionBackgroundColorList(captionColorInfolist);
        }
    }

    /*public void applyToAllCaption(boolean isApplyToAll){
        mApplyToAllImage.setImageResource(isApplyToAll ? R.mipmap.applytoall : R.mipmap.unapplytoall);
        mApplyToAllText.setTextColor(isApplyToAll ? Color.parseColor("#ff4a90e2") : Color.parseColor("#ff909293"));
        mIsApplyToAll = isApplyToAll;
    }*/

    public void notifyDataSetChanged(){
        mCaptionBackgroundRecycleAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootParent = inflater.inflate(R.layout.compound_caption_background_fragment, container, false);
        mCaptionBackgroundRecyclerView = (RecyclerView)rootParent.findViewById(R.id.captionColorRecyerView);
        mCaptonOpacitySeekBar = (SeekBar)rootParent.findViewById(R.id.captonOpacitySeekBar);
        mCaptonOpacitySeekBar.setMax(100);
       /* mCaptonCornerSeekBar = (SeekBar)rootParent.findViewById(R.id.captonCornerSeekBar);
        mCaptonCornerSeekBar.setMax(100);
        mCaptionMarginSeekBar = (SeekBar)rootParent.findViewById(R.id.captionMarginSeekBar);
        mCaptionMarginSeekBar.setMax(100);
*/
        mSeekBarOpacityValue = (TextView) rootParent.findViewById(R.id.seekBarOpacityValue);
        return rootParent;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCaptionBackgroundRecycleAdapter();
        initCaptionBackgroundSeekBar();
        if(captionBackgroundListener != null){
            captionBackgroundListener.onFragmentLoadFinished();
        }
    }

    private void initCaptionBackgroundSeekBar() {
        mCaptonOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updateCaptionOpacityValue(progress);
                    if(captionBackgroundListener != null){
                        captionBackgroundListener.onCaptionOpacity(progress);
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
       /* mCaptonCornerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updateCaptionCornerValue(progress);
                    if(captionBackgroundListener != null){
                        captionBackgroundListener.onCaptionCorner(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

       /* mCaptionMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    updateCaptionPaddingValue(progress);
                    if(captionBackgroundListener != null){
                        captionBackgroundListener.onCaptionPadding(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/
    }

    private void initCaptionBackgroundRecycleAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mCaptionBackgroundRecyclerView.setLayoutManager(layoutManager);
        mCaptionBackgroundRecycleAdapter = new CompoundCaptionBackgroundRecyclerAdaper(getActivity());
        mCaptionBackgroundRecycleAdapter.setCaptionBackgroundColorList(mCaptionColorInfolist);
        mCaptionBackgroundRecyclerView.setAdapter(mCaptionBackgroundRecycleAdapter);
        mCaptionBackgroundRecyclerView.addItemDecoration(new SpaceItemDecoration(0, ScreenUtils.dip2px(getActivity(),16)));
        mCaptionBackgroundRecycleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if(captionBackgroundListener != null){
                    captionBackgroundListener.onCaptionColor(pos);
                }
            }
        });

       /* mApplyToAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsApplyToAll = !mIsApplyToAll;
                applyToAllCaption(mIsApplyToAll);
                if(captionBackgroundListener != null){
                    captionBackgroundListener.onIsApplyToAll(mIsApplyToAll);
                }
            }
        });*/
    }
}
