package com.meishe.sdkdemo.edit;

import android.app.Fragment;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.meicam.sdk.NvsAssetPackageManager;
import com.meicam.sdk.NvsStreamingContext;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsTimelineAnimatedSticker;
import com.meishe.base.view.CompileProgress;
import com.meishe.sdkdemo.MSApplication;
import com.meishe.sdkdemo.R;
import com.meishe.sdkdemo.utils.Logger;
import com.meishe.sdkdemo.utils.MediaScannerUtil;
import com.meishe.sdkdemo.utils.ScreenUtils;
import com.meishe.sdkdemo.utils.Util;
import com.meishe.sdkdemo.utils.VideoCompileUtil;
import com.meishe.utils.ToastUtil;

import java.util.Collections;
import java.util.List;

public class CompileVideoFragment extends Fragment {
    private final String TAG = "CompileVideoFragment";
    private RelativeLayout mCompileVideoRect;
    private TextView mCompileVideoFinished;
    private String mCompileVideoPath;
    private NvsStreamingContext mStreamingContext = NvsStreamingContext.getInstance();
    private NvsTimeline mTimeline;
    private OnCompileVideoListener mCompileVideoListener;
    private NvsTimelineAnimatedSticker mLogoSticker;

    private TextView mCutCompileProgressText;
    private CompileProgress mCutCompileProgress;
    private View mBtnCancel;

    private long costTime = 0;

    public void setCompileVideoListener(OnCompileVideoListener compileVideoListener) {
        mCompileVideoListener = compileVideoListener;
    }

    /**
     * 视频播放相关回调
     * Video playback related callbacks
     */
    public interface OnCompileVideoListener {
        //video compile
        void compileProgress(NvsTimeline timeline, int progress);

        void compileFinished(NvsTimeline timeline);

        void compileFailed(NvsTimeline timeline);

        void compileCompleted(NvsTimeline nvsTimeline, boolean isCanceled);

        void compileVideoCancel();
    }

    public void setTimeline(NvsTimeline timeline) {
        mTimeline = timeline;
    }

    /**
     * 生成视频
     * Compile video
     */
    public void compileVideo() {
        Logger.d(TAG, "compileVideo..");
        initCompileCallBack();
        mCompileVideoRect.setVisibility(View.VISIBLE);
        mCompileVideoFinished.setVisibility(View.GONE);
        mCompileVideoPath = VideoCompileUtil.getCompileVideoPath(MSApplication.getContext());
        if (mCompileVideoPath == null)
            return;
        /*
         * 添加美摄logo
         * Add Meishe logo
         * */
//        addLogoWaterMark();
        costTime = System.currentTimeMillis();
        VideoCompileUtil.compileVideo(mStreamingContext,
                mTimeline,
                mCompileVideoPath,
                0,
                mTimeline.getDuration()
        );
    }

    private void addLogoWaterMark() {
        String logoTemplatePath = "assets:/E14FEE65-71A0-4717-9D66-3397B6C11223/E14FEE65-71A0-4717-9D66-3397B6C11223.5.animatedsticker";
        String imagePath = "assets:/logo.png";
        StringBuilder packageId = new StringBuilder();
        int error = mStreamingContext.getAssetPackageManager().installAssetPackage(logoTemplatePath, null, NvsAssetPackageManager.ASSET_PACKAGE_TYPE_ANIMATEDSTICKER, true, packageId);
        if (error == NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_NO_ERROR
                || error == NvsAssetPackageManager.ASSET_PACKAGE_MANAGER_ERROR_ALREADY_INSTALLED) {
            mLogoSticker = mTimeline.addCustomAnimatedSticker(0, mTimeline.getDuration(), packageId.toString(), imagePath);
            if (mLogoSticker == null)
                return;
            /*
             * 必须先缩放，然后再平移，否则计算平移结果错误
             * You must zoom before panning, otherwise the translation result is incorrect
             * */
            mLogoSticker.setScale(0.3f);
            List<PointF> list = mLogoSticker.getBoundingRectangleVertices();
            if (list == null || list.size() < 4)
                return;
            Collections.sort(list, new Util.PointXComparator());
            int offset = ScreenUtils.dip2px(getActivity(), 10);
            float xPos = -(mTimeline.getVideoRes().imageWidth / 2 + list.get(0).x - offset);

            Collections.sort(list, new Util.PointYComparator());
            float y_dis = list.get(3).y - list.get(0).y;
            float yPos = mTimeline.getVideoRes().imageHeight / 2 - list.get(0).y - y_dis - offset;

            PointF logoPos = new PointF(xPos, yPos);
            mLogoSticker.translateAnimatedSticker(logoPos);
        }
    }

    private void removeLogoSticker() {
        if (mLogoSticker != null) {
            mTimeline.removeAnimatedSticker(mLogoSticker);
            mLogoSticker = null;
        }
    }

    /**
     * 停止引擎
     * Stop the engine
     */
    public void stopEngine() {
        if (mStreamingContext.getStreamingEngineState() != NvsStreamingContext.STREAMING_ENGINE_STATE_COMPILE) {
            if (mStreamingContext != null) {
                mStreamingContext.stop();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_compile_video, container, false);
        mCompileVideoRect = rootView.findViewById(R.id.compileVideoRect);
        mCutCompileProgressText = rootView.findViewById(R.id.cut_compile_progress_text);
        mCutCompileProgress = rootView.findViewById(R.id.cut_compile_progress);
        mBtnCancel = rootView.findViewById(R.id.btn_cancel);

        mCompileVideoFinished = (TextView) rootView.findViewById(R.id.compileVideoFinished);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStreamingContext != null) {
                    mStreamingContext.stop();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopEngine();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    public void initCompileCallBack() {
        mStreamingContext.setCompileCallback(new NvsStreamingContext.CompileCallback() {
            @Override
            public void onCompileProgress(NvsTimeline nvsTimeline, int i) {
                setCenterProgress(i);
            }

            @Override
            public void onCompileFinished(NvsTimeline nvsTimeline) {
//                Toast.makeText(getActivity().getApplicationContext(),
//                        "生成耗时： " + (System.currentTimeMillis() - costTime), Toast.LENGTH_LONG).show();
                Log.e(TAG, "onCompileFinished: 生成耗时： " + (System.currentTimeMillis() - costTime));
                mStreamingContext.setCompileConfigurations(null);
                /*
                 * 加入到媒体库
                 * Add to media library
                 * */
                MediaScannerUtil.scanFile(mCompileVideoPath, "video/mp4");
                if (mCompileVideoListener != null)
                    mCompileVideoListener.compileFinished(nvsTimeline);
            }

            @Override
            public void onCompileFailed(NvsTimeline nvsTimeline) {
                onCompileFiled();
                String[] tipsName = getResources().getStringArray(R.array.compile_video_failed_tips);
                Util.showDialog(getActivity(), tipsName[0], tipsName[1]);
                if (mCompileVideoListener != null)
                    mCompileVideoListener.compileFailed(nvsTimeline);
                removeLogoSticker();
            }
        });
        mStreamingContext.setCompileCallback2(new NvsStreamingContext.CompileCallback2() {
            @Override
            public void onCompileCompleted(NvsTimeline nvsTimeline, boolean isCanceled) {
                if (!isCanceled) {
                    mStreamingContext.setCompileConfigurations(null);
                    mCompileVideoRect.setVisibility(View.GONE);
                    mCompileVideoFinished.setVisibility(View.VISIBLE);

                    /*
                     * 加入到媒体库
                     * Add to media library
                     * */
                    MediaScannerUtil.scanFile(mCompileVideoPath, "video/mp4");

                    String[] tipsName = getResources().getStringArray(R.array.compile_video_success_tips);
                    StringBuilder successTips = new StringBuilder();
                    successTips.append(tipsName[0]);
                    successTips.append("\n");
                    successTips.append(mCompileVideoPath);
                    Logger.e(TAG, "successTips" + successTips.toString());
                    ToastUtil.showToastCenter(getActivity(), successTips.toString());
                }
                removeLogoSticker();
                if (mCompileVideoListener != null)
                    mCompileVideoListener.compileCompleted(nvsTimeline, isCanceled);

            }
        });
    }


    private void onCompileFiled() {
        mCutCompileProgress.setVisibility(View.GONE);
        mCutCompileProgressText.setVisibility(View.GONE);
    }

    private void setCenterProgress(int i) {
        mCutCompileProgress.setProgress(i);
        mCutCompileProgressText.setText(i + "%");
    }

}