package com.meishe.sdkdemo.mimodemo.common.template.model;


import com.meishe.sdkdemo.mimodemo.common.dataInfo.ClipInfo;
import com.meishe.sdkdemo.mimodemo.common.dataInfo.TransitionInfo;
import com.meishe.sdkdemo.mimodemo.common.dataInfo.VideoClipFxInfo;

/**
 * 带特效的Clip
 * Clip with special effects
 */
public class FxClipInfo extends ClipInfo {
    /**
     * 滤镜
     * filter
     */
    private VideoClipFxInfo mVideoClipFxInfo;
    /**
     * 转场
     * Transition
     */
    private TransitionInfo mTransitionInfo;

    public VideoClipFxInfo getVideoClipFxInfo() {
        return mVideoClipFxInfo;
    }

    public void setVideoClipFxInfo(VideoClipFxInfo videoClipFxInfo) {
        this.mVideoClipFxInfo = videoClipFxInfo;
    }

    public TransitionInfo getTransitionInfo() {
        return mTransitionInfo;
    }

    public void setTransitionInfo(TransitionInfo transitionInfo) {
        this.mTransitionInfo = transitionInfo;
    }
}
