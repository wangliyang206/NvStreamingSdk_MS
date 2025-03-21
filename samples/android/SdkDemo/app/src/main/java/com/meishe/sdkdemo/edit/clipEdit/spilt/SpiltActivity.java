package com.meishe.sdkdemo.edit.clipEdit.spilt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meicam.sdk.NvsMultiThumbnailSequenceView;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsVideoClip;
import com.meicam.sdk.NvsVideoTrack;
import com.meishe.base.utils.ThreadUtils;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BaseActivity;
import com.meishe.sdkdemo.dialog.LoadingPop;
import com.meishe.sdkdemo.edit.clipEdit.SingleClipFragment;
import com.meishe.sdkdemo.edit.data.BackupData;
import com.meishe.sdkdemo.edit.data.BitmapData;
import com.meishe.sdkdemo.edit.view.CustomTitleBar;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.Constants;
import com.meishe.sdkdemo.utils.ScreenUtils;
import com.meishe.sdkdemo.utils.TimeFormatUtil;
import com.meishe.sdkdemo.utils.TimelineUtil;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.dataInfo.ClipInfo;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;

import java.util.ArrayList;

;

/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : yyj
 * @CreateDate : 2019/6/28.
 * @Description :视频编辑-编辑-分割-Activity
 * @Description :VideoEdit-edit-split-Activity
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class SpiltActivity extends BaseActivity {
    private static final String TAG = "SpiltActivity";
    private CustomTitleBar mTitleBar;

    private RelativeLayout mBottomLayout;
    private TextView mTimeSpanValue;
    private NvsMultiThumbnailSequenceView mMultiThumbnailSequenceView;
    private SpiltTimeSpan mSpiltTimeSpan;
    private ImageView mSplitCancel;
    private ImageView mSplitFinish;
    private String mClipDurationStr;
    private NvsStreamingContext mStreamingContext;
    private NvsTimeline mTimeline;
    private SingleClipFragment mClipFragment;
    ArrayList<ClipInfo> mClipArrayList;
    private int mCurClipIndex = 0;
    private long mSpiltPoint = 0;
    private LoadingPop mLoadingPop;

    @Override
    protected int initRootView() {
        mStreamingContext = NvsStreamingContext.getInstance();
        mStreamingContext.stop();
        return R.layout.activity_spilt;
    }

    @Override
    protected void initViews() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottomLayout);
        mTimeSpanValue = (TextView) findViewById(R.id.timeSpanValue);
        mMultiThumbnailSequenceView = (NvsMultiThumbnailSequenceView) findViewById(R.id.mutilSequenceView);
        mSpiltTimeSpan = (SpiltTimeSpan) findViewById(R.id.spiltTimeSpan);
        mSplitCancel = (ImageView) findViewById(R.id.splitCancel);
        mSplitFinish = (ImageView) findViewById(R.id.spiltFinish);
    }

    @Override
    protected void initTitle() {
        mTitleBar.setTextCenter(R.string.split);
        mTitleBar.setBackImageVisible(View.GONE);
    }

    @Override
    protected void initData() {
        mLoadingPop = LoadingPop.create(this);
        mClipArrayList = BackupData.instance().cloneClipInfoData();
        mCurClipIndex = BackupData.instance().getClipIndex();
        if (mCurClipIndex < 0 || mCurClipIndex >= mClipArrayList.size())
            return;
        mTimeline = TimelineUtil.createSingleClipTimeline(mClipArrayList.get(mCurClipIndex), true);
        if (mTimeline == null)
            return;
        updateClipInfo();
        initVideoFragment();
        initMultiSequence();
    }

    private void updateClipInfo() {
        NvsVideoTrack videoTrack = mTimeline.getVideoTrackByIndex(0);
        if (videoTrack == null)
            return;
        NvsVideoClip videoClip = videoTrack.getClipByIndex(0);
        if (videoClip == null)
            return;

        long trimIn = mClipArrayList.get(mCurClipIndex).getTrimIn();
        if (trimIn < 0)
            mClipArrayList.get(mCurClipIndex).changeTrimIn(videoClip.getTrimIn());
        long trimOut = mClipArrayList.get(mCurClipIndex).getTrimOut();
        if (trimOut < 0)
            mClipArrayList.get(mCurClipIndex).changeTrimOut(videoClip.getTrimOut());
        mClipArrayList.get(mCurClipIndex).setTrackIndex(videoClip.getIndex());
    }

    private void initMultiSequence() {
        long duration = mTimeline.getDuration();
        ArrayList<NvsMultiThumbnailSequenceView.ThumbnailSequenceDesc> sequenceDescsArray = new ArrayList<>();
        NvsMultiThumbnailSequenceView.ThumbnailSequenceDesc sequenceDescs = new NvsMultiThumbnailSequenceView.ThumbnailSequenceDesc();
        sequenceDescs.mediaFilePath = mClipArrayList.get(mCurClipIndex).getFilePath();
        sequenceDescs.trimIn = mClipArrayList.get(mCurClipIndex).getTrimIn();
        sequenceDescs.trimOut = mClipArrayList.get(mCurClipIndex).getTrimOut();
        sequenceDescs.inPoint = 0;
        sequenceDescs.outPoint = duration;
        sequenceDescs.stillImageHint = false;
        sequenceDescsArray.add(sequenceDescs);
        mMultiThumbnailSequenceView.setThumbnailSequenceDescArray(sequenceDescsArray);
        double pixelPerMicrosecond = getPixelMicrosecond(duration);
        mMultiThumbnailSequenceView.setPixelPerMicrosecond(pixelPerMicrosecond);
        mSpiltTimeSpan.setMultiThumbnailSequenceView(mMultiThumbnailSequenceView);
        mSpiltTimeSpan.setPixelPerMicrosecond(pixelPerMicrosecond);
        mClipDurationStr = TimeFormatUtil.formatUsToString1(duration);
        mSplitFinish.postDelayed(new Runnable() {
            @Override
            public void run() {
                int sreenWidth = ScreenUtils.getScreenWidth(SpiltActivity.this);
                int halfScreenWid = (int) Math.floor(sreenWidth / 2 + 0.5D);
                mSpiltTimeSpan.updateSpiltTimeSpan(halfScreenWid);
            }
        }, 100);
    }

    private double getPixelMicrosecond(long duration) {
        int width = ScreenUtils.getScreenWidth(SpiltActivity.this);
        double pixelMicrosecond = width / (double) duration;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMultiThumbnailSequenceView.getLayoutParams();
        if (layoutParams != null) {
            int sequenceWidth = width - layoutParams.leftMargin - layoutParams.rightMargin;
            pixelMicrosecond = sequenceWidth / (double) duration;
        }
        return pixelMicrosecond;
    }


    private void setTimeSpanText(long timeStamp) {
        String timeStr = TimeFormatUtil.formatUsToString1(timeStamp);
        timeStr += "/";
        timeStr += mClipDurationStr;
        mTimeSpanValue.setText(timeStr);
    }

    @Override
    protected void initListener() {
        mSplitFinish.setOnClickListener(this);
        mSplitCancel.setOnClickListener(this);
        mSpiltTimeSpan.setOnSpiltPointChangeListener(new SpiltTimeSpan.OnSpiltPointChangeListener() {
            @Override
            public void onChange(long timeStamp) {
                mSpiltPoint = timeStamp;
                setTimeSpanText(timeStamp);
                updateSeekBarValue(timeStamp);
                seekTimeline(timeStamp);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.splitCancel) {
            removeTimeline();
            AppManager.getInstance().finishActivity();
        } else if (id == R.id.spiltFinish) {
            if (isFinishing()) {
                return;
            }
            if (mSpiltPoint < Constants.NS_TIME_BASE) {
                String[] spiltVideoTips = getResources().getStringArray(R.array.spilt_video_tips);
                Util.showDialog(SpiltActivity.this, spiltVideoTips[0], spiltVideoTips[1]);
                return;
            }
            ClipInfo currentClipInfo = mClipArrayList.get(mCurClipIndex);
            float speed = currentClipInfo.getSpeed();
            speed = speed <= 0 ? 1.0f : speed;
            long newSpiltPoint = (long) (mSpiltPoint * speed) + currentClipInfo.getTrimIn();
            ClipInfo clipInfoFst = currentClipInfo.clone();
            clipInfoFst.changeTrimOut(newSpiltPoint);
            clipInfoFst.setTrackIndex(currentClipInfo.getTrackIndex());
            ClipInfo clipInfoSec = currentClipInfo.clone();
            clipInfoSec.changeTrimIn(newSpiltPoint);
            clipInfoSec.setTrackIndex(currentClipInfo.getTrackIndex() + 1);
            mClipArrayList.remove(mCurClipIndex);
            mClipArrayList.add(mCurClipIndex, clipInfoSec);
            mClipArrayList.add(mCurClipIndex, clipInfoFst);
            mLoadingPop.show();
            boolean success = Util.getBitmapFromClipInfo(this, clipInfoSec, new Util.OnBitmapFromClipInfo() {
                @Override
                public void onBitmapFromClipInfo(Bitmap bitmap) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finishSpilt(bitmap);
                        }
                    });
                }
            });
            if (!success) {
                finishSpilt(null);
            }
        }

    }

    private void finishSpilt(Bitmap bitmap) {
        if (null != mLoadingPop && !mLoadingPop.isDismiss()) {
            mLoadingPop.dismiss();
        }
        BitmapData.instance().insertBitmap(mCurClipIndex + 1, bitmap);
        BackupData.instance().setClipInfoData(mClipArrayList);
        removeTimeline();
        Intent intent = new Intent();
        intent.putExtra("spiltPosition", mCurClipIndex);
        setResult(RESULT_OK, intent);
        AppManager.getInstance().finishActivity();
    }

    private void updateSeekBarValue(long timeStamp) {
        mClipFragment.updateCurPlayTime(timeStamp);
    }

    private void seekTimeline(long timeStamp) {
        mClipFragment.seekTimeline(timeStamp, 0);
    }

    @Override
    public void onBackPressed() {
        removeTimeline();
        AppManager.getInstance().finishActivity();
        super.onBackPressed();
    }

    private void removeTimeline() {
        mClipFragment.stopEngine();
        TimelineUtil.removeTimeline(mTimeline);
        mTimeline = null;
    }

    private void initVideoFragment() {
        mClipFragment = new SingleClipFragment();
        mClipFragment.setFragmentLoadFinisedListener(new SingleClipFragment.OnFragmentLoadFinisedListener() {
            @Override
            public void onLoadFinished() {
                seekTimeline(mStreamingContext.getTimelineCurrentPosition(mTimeline));
            }
        });
        mClipFragment.setTimeline(mTimeline);
        Bundle bundle = new Bundle();
        bundle.putInt("titleHeight", mTitleBar.getLayoutParams().height);
        bundle.putInt("bottomHeight", mBottomLayout.getLayoutParams().height);
        bundle.putInt("ratio", TimelineData.instance().getMakeRatio());
        mClipFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.spaceLayout, mClipFragment)
                .commit();
        getFragmentManager().beginTransaction().show(mClipFragment);
    }
}
