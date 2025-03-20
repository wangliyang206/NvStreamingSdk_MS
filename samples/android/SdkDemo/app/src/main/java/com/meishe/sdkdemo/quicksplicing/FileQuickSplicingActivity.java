package com.meishe.sdkdemo.quicksplicing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meicam.sdk.NvsMultiThumbnailSequenceView;
import com.meicam.sdk.NvsPassthroughConvertor;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsVideoClip;
import com.meicam.sdk.NvsVideoTrack;
import com.meishe.base.utils.ToastUtils;
import com.meishe.engine.util.PathUtils;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.base.BaseActivity;
import com.meishe.sdkdemo.edit.clipEdit.SingleClipFragment;
import com.meishe.sdkdemo.edit.data.BackupData;
import com.meishe.sdkdemo.edit.interfaces.OnTitleBarClickListener;
import com.meishe.sdkdemo.edit.timelineEditor.NvsTimelineEditor;
import com.meishe.sdkdemo.edit.timelineEditor.NvsTimelineTimeSpanExt;
import com.meishe.sdkdemo.edit.view.CustomTitleBar;
import com.meishe.sdkdemo.utils.AppManager;
import com.meishe.sdkdemo.utils.MediaScannerUtil;
import com.meishe.sdkdemo.utils.ScreenUtils;
import com.meishe.sdkdemo.utils.TimeFormatUtil;
import com.meishe.sdkdemo.utils.TimelineUtil;
import com.meishe.sdkdemo.utils.dataInfo.ClipInfo;
import com.meishe.sdkdemo.utils.dataInfo.TimelineData;

import java.util.ArrayList;
import java.util.List;

public class FileQuickSplicingActivity extends BaseActivity {

    private CustomTitleBar mTitleBar;
    private RelativeLayout mBottomLayout;
    private TextView mTrimDurationVal;
    private NvsTimelineEditor mTimelineEditor;
    private ImageView mTrimFinish;
    private long mTrimInPoint = 0;
    private long mTrimOutPoint = 0;
    private SingleClipFragment mClipFragment;
    NvsTimelineTimeSpanExt mTimlineTimeSpanExt;
    private NvsStreamingContext mStreamingContext;
    private NvsTimeline mTimeline;
    private ClipInfo mClipInfo = new ClipInfo();
    private StringBuilder mTrimDurationText = new StringBuilder();

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, FileQuickSplicingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int initRootView() {
        mStreamingContext = NvsStreamingContext.getInstance();
        mStreamingContext.stop();
        return R.layout.activity_trim;
    }

    @Override
    protected void initViews() {
        mTitleBar = (CustomTitleBar) findViewById(R.id.title_bar);
        mBottomLayout = (RelativeLayout) findViewById(R.id.trim_bottom_layout);
        mTrimDurationVal = (TextView) findViewById(R.id.trimDurationVal);
        mTimelineEditor = (NvsTimelineEditor) findViewById(R.id.timelineEditor);
        mTrimFinish = (ImageView) findViewById(R.id.trimFinish);
    }

    @Override
    protected void initTitle() {
        mTitleBar.setTextCenter(R.string.file_pass_through);
        mTitleBar.setBackImageVisible(View.GONE);
        mTitleBar.setTextRightVisible(View.VISIBLE);
        mTitleBar.setTextRight(R.string.compile);
        mTitleBar.setOnTitleBarClickListener(new OnTitleBarClickListener() {
            @Override
            public void OnBackImageClick() {

            }

            @Override
            public void OnCenterTextClick() {

            }

            @Override
            public void OnRightTextClick() {
                String fileName = "quick_splicing_" + System.currentTimeMillis() + ".mp4";
                String compileVideoPath = PathUtils.getVideoSavePath(fileName);
                mStreamingContext.stop();
                NvsPassthroughConvertor nvsPassthroughConvertor = new NvsPassthroughConvertor();
                List<NvsPassthroughConvertor.NvsPassthroughFileInfo> passthroughFileInfos = new ArrayList<>();
                NvsPassthroughConvertor.NvsPassthroughFileInfo fileInfo = new NvsPassthroughConvertor.NvsPassthroughFileInfo();
                fileInfo.filePath = mClipInfo.getFilePath();
                fileInfo.trimIn = mClipInfo.getTrimIn();
                fileInfo.trimOut = mClipInfo.getTrimOut();
                passthroughFileInfos.add(fileInfo);
                nvsPassthroughConvertor.convertMediaFile(passthroughFileInfos, compileVideoPath, null, 0);
                nvsPassthroughConvertor.setPassthroughConvertorCallback(new FilePassThroughConvertorCallback(), null);
            }
        });

    }


    class FilePassThroughConvertorCallback implements NvsPassthroughConvertor.PassthroughConvertorCallback {

        @Override
        public void onProgress(long l, float v) {

        }

        @Override
        public void onFinish(long l, int i, String s) {
            MediaScannerUtil.scanFile(s, "video/mp4");
            ToastUtils.showShort(getResources().getString(R.string.compile_video_success) + s);
        }

    }

    @Override
    protected void initData() {
        mClipInfo = BackupData.instance().getClipInfo();
        mTimeline = TimelineUtil.createSingleClipTimeline(mClipInfo, false);
        if (mTimeline == null) {
            return;
        }
        updateClipInfo();
        initVideoFragment();
        initMultiSequence();
    }

    @Override
    protected void initListener() {
        mTrimFinish.setOnClickListener(this);
        if (mTimlineTimeSpanExt != null) {
            mTimlineTimeSpanExt.setOnChangeListener(new NvsTimelineTimeSpanExt.OnTrimInChangeListener() {
                @Override
                public void onChange(long timeStamp, boolean isDragEnd) {
                    mTrimInPoint = timeStamp;
                    long totalDuration = mTrimOutPoint - mTrimInPoint;
                    setTrimDurationText(totalDuration);

                    mClipInfo.changeTrimIn(mTrimInPoint);
                    mClipFragment.updateCurPlayTime(timeStamp);
                    mClipFragment.setVideoTrimIn(timeStamp);
                    seekTimeline(timeStamp);
                }
            });
            mTimlineTimeSpanExt.setOnChangeListener(new NvsTimelineTimeSpanExt.OnTrimOutChangeListener() {
                @Override
                public void onChange(long timeStamp, boolean isDragEnd) {
                    mTrimOutPoint = timeStamp;
                    long totalDuration = mTrimOutPoint - mTrimInPoint;
                    setTrimDurationText(totalDuration);
                    mClipInfo.changeTrimOut(mTrimOutPoint);
                    mClipFragment.setVideoTrimOut(timeStamp);
                    seekTimeline(timeStamp);
                }
            });
        }

        mClipFragment.setVideoFragmentCallBack(new SingleClipFragment.VideoFragmentListener() {
            @Override
            public void playBackEOF(NvsTimeline timeline) {
            }

            @Override
            public void playStopped(NvsTimeline timeline) {
            }

            @Override
            public void playbackTimelinePosition(NvsTimeline timeline, long stamp) {
            }

            @Override
            public void streamingEngineStateChanged(int state) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.trimFinish) {
            BackupData.instance().setClipInfo(mClipInfo);
            removeTimeline();
            setResultIntent();
            AppManager.getInstance().finishActivity();
        }
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

    private void updateClipInfo() {
        NvsVideoTrack videoTrack = mTimeline.getVideoTrackByIndex(0);
        if (videoTrack == null) {
            return;
        }
        NvsVideoClip videoClip = videoTrack.getClipByIndex(0);
        if (videoClip == null) {
            return;
        }
        long trimIn = mClipInfo.getTrimIn();
        if (trimIn < 0) {
            mClipInfo.changeTrimIn(videoClip.getTrimIn());
        }
        long trimOut = mClipInfo.getTrimOut();
        if (trimOut < 0) {
            mClipInfo.changeTrimOut(videoClip.getTrimOut());
        }
    }

    private void initMultiSequence() {
        long duration = mTimeline.getDuration();
        ArrayList<NvsMultiThumbnailSequenceView.ThumbnailSequenceDesc> sequenceDescsArray = new ArrayList<>();
        NvsMultiThumbnailSequenceView.ThumbnailSequenceDesc sequenceDescs = new NvsMultiThumbnailSequenceView.ThumbnailSequenceDesc();
        sequenceDescs.mediaFilePath = mClipInfo.getFilePath();
        sequenceDescs.trimIn = 0;
        sequenceDescs.trimOut = duration;
        sequenceDescs.inPoint = 0;
        sequenceDescs.outPoint = duration;
        sequenceDescs.stillImageHint = false;
        sequenceDescsArray.add(sequenceDescs);
        double pixelPerMicrosecond = getPixelMicrosecond(duration);
        mTimelineEditor.setPixelPerMicrosecond(pixelPerMicrosecond);
        int sequenceLeftPadding = ScreenUtils.dip2px(this, 13);
        mTimelineEditor.setSequencLeftPadding(sequenceLeftPadding);
        mTimelineEditor.setSequencRightPadding(sequenceLeftPadding);
        mTimelineEditor.setTimeSpanLeftPadding(sequenceLeftPadding);
        mTimelineEditor.initTimelineEditor(sequenceDescsArray, duration);
        mTimelineEditor.getMultiThumbnailSequenceView().getLayoutParams().height = ScreenUtils.dip2px(this, 64);
        /*
         * warning: 使用addTimeSpanExt()之前必须设置setTimeSpanType()
         * warning: setTimeSpanType () must be set before using addTimeSpanExt ()
         * */
        mTimelineEditor.setTimeSpanType("NvsTimelineTimeSpanExt");
        float speed = mClipInfo.getSpeed();
        speed = speed <= 0 ? 1.0f : speed;
        mTrimInPoint = (long) (mClipInfo.getTrimIn() / speed);
        mTrimOutPoint = (long) (mClipInfo.getTrimOut() / speed);
        mTimlineTimeSpanExt = mTimelineEditor.addTimeSpanExt(mTrimInPoint, mTrimOutPoint);
        setTrimDurationText(mTrimOutPoint - mTrimInPoint);
    }

    private void setTrimDurationText(long duration) {
        mTrimDurationText.setLength(0);
        mTrimDurationText.append(getResources().getString(R.string.trim_duration));
        mTrimDurationText.append(" ");
        mTrimDurationText.append(TimeFormatUtil.formatUsToString1(duration));
        mTrimDurationVal.setText(mTrimDurationText.toString());
    }

    private double getPixelMicrosecond(long duration) {
        int width = ScreenUtils.getScreenWidth(FileQuickSplicingActivity.this);
        int leftPadding = ScreenUtils.dip2px(this, 13);
        int sequenceWidth = width - 2 * leftPadding;
        double pixelMicrosecond = sequenceWidth / (double) duration;
        return pixelMicrosecond;
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

    private void seekTimeline(long stamp) {
        mClipFragment.seekTimeline(stamp, 0);
    }

    private void setResultIntent() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

}
