package com.meishe.sdkdemo.themeshoot.utlils;

import android.text.TextUtils;
import android.util.Log;

import com.meicam.sdk.NvsAudioClip;
import com.meicam.sdk.NvsAudioTrack;
import com.meicam.sdk.NvsTimeline;
import com.meicam.sdk.NvsTimelineVideoFx;
import com.meicam.sdk.NvsVideoClip;
import com.meicam.sdk.NvsVideoTrack;
import com.meicam.sdk.NvsVideoTransition;
import com.meishe.sdkdemo.themeshoot.bean.ThemePreviewBean;
import com.meishe.sdkdemo.themeshoot.model.ThemeModel;
import com.meishe.sdkdemo.utils.Constants;
import com.meishe.sdkdemo.utils.NumberUtils;

import java.io.File;
import java.util.List;

/**
 * All rights reserved,Designed by www.meishesdk.com
 *
 * @Author : zcy
 * @CreateDate : 2020/8/5.
 * @Description :主题拍摄TimelineUtil。ThemeModel TimelineUtils
 * @Copyright :www.meishesdk.com Inc.All rights reserved.
 */
public class ThemeTimelineUtils {
    private static String TAG = "ThemeTimelineUtils";

    /**
     * 添加时间线滤镜
     * Add a timeline filter
     * @param mTimeline
     * @param filterPackageId
     */
    public static void addTimelineFilter(NvsTimeline mTimeline, String filterPackageId) {
        if (!TextUtils.isEmpty(filterPackageId)) {
            NvsTimelineVideoFx nvsTimelineVideoFx = mTimeline.addPackagedTimelineVideoFx(0, mTimeline.getDuration(), filterPackageId);
            if (nvsTimelineVideoFx != null) {
                nvsTimelineVideoFx.setFilterIntensity(1.0f);
            }
        }
    }

    /**
     * 添加背景音乐
     * Add background music
     * @param mTimeline
     * @param mThemeModel
     */
    public static void addMusic(NvsTimeline mTimeline, ThemeModel mThemeModel) {
        //添加音乐 Add music
        String music = mThemeModel.getMusic();
        if (!TextUtils.isEmpty(music)) {
            NvsAudioTrack nvsAudioTrack = mTimeline.appendAudioTrack();
            if (nvsAudioTrack == null) {
                Log.e(TAG, "mTimeline.appendAudioTrack failed");
            }
//            NvsAudioClip nvsAudioClip = nvsAudioTrack.addClip(mThemeModel.getFolderPath() + File.separator + music, 0, 0, mThemeModel.getMusicDuration());
            NvsAudioClip nvsAudioClip = nvsAudioTrack.addClip(mThemeModel.getFolderPath() + File.separator + music, 0);
            if (nvsAudioClip == null) {
                Log.e(TAG, "AudioTrack.addClip failed nvsAudioClip==null!!!");
            }
            if ("1".equals(mThemeModel.getNeedControlMusicFading()+"")) {
                long musicFadingTime = mThemeModel.getMusicFadingTime();
                nvsAudioClip.setFadeOutDuration(musicFadingTime * Constants.US_TIME_BASE);
            }
        }
    }

    /**
     * 设置转场
     * Set up a transition
     * @param nvsVideoTrack
     * @param shotInfos
     */
    public static void setVideoClipTrans(NvsVideoTrack nvsVideoTrack, List<ThemeModel.ShotInfo> shotInfos) {
        if (nvsVideoTrack == null || shotInfos == null) {
            Log.e(TAG, "setVideoClipTrans failed nvsVideoTrack == null || shotInfos == null!!!");
            return;
        }
        int currentClip = 0;
        int addClip = 0;
        for (int i = 0; i < shotInfos.size(); i++) {
            ThemeModel.ShotInfo shotInfo = shotInfos.get(i);

            if (shotInfo != null) {
                List<ThemeModel.ShotInfo.SpeedInfo> speed = shotInfo.getSpeed();
                if (speed == null || speed.isEmpty()) {
                } else {
                    int clipCount = ThemeShootUtil.getClipCountFromSpeed(speed, shotInfo.getDuration());
                    //分成多段添加 Add in sections
                    if (clipCount > 1) {
                        for (int j = 0; j < clipCount -1; j++) {
                            nvsVideoTrack.setPackagedTransition(currentClip + addClip, null);
                            addClip++;
                        }
                    }
                }
                String trans = shotInfo.getTrans();
                if (!TextUtils.isEmpty(trans)) {
                    // 内建与包裹转场(未加) Built in and Parcel transfer (not added)
                    NvsVideoTransition nvsVideoTransition = nvsVideoTrack.setPackagedTransition(currentClip + addClip, trans);
                    if(shotInfo.getTransDuration() !=0){
                        nvsVideoTransition.setVideoTransitionDuration(shotInfo.getTransDuration()*1000, NvsVideoTransition.VIDEO_TRANSITION_DURATION_MATCH_MODE_STRETCH);
                    }
                } else {
                    nvsVideoTrack.setBuiltinTransition(currentClip + addClip, null);
                }
                currentClip++;
            }
        }
        //合成时间线转场 Synthesize the timeline transition
        int clipCount = nvsVideoTrack.getClipCount();
        for (int index = 0;index < clipCount;index++){
            NvsVideoTransition videoTransition = nvsVideoTrack.getTransitionBySourceClipIndex(index);
            if (videoTransition != null){
                videoTransition.enableTimelineTransition(true);
            }
        }
    }

    /**
     * 添加视频片段（包含变速）
     * Add video footage (including speed changes)
     * @param mTimeline
     * @param nvsVideoTrack
     * @param previewBean
     * @param shotInfo
     * @param mThemeModel
     */
    public static void appendVideoClip(NvsTimeline mTimeline, NvsVideoTrack nvsVideoTrack, ThemePreviewBean previewBean, ThemeModel.ShotInfo shotInfo, ThemeModel mThemeModel) {
        String shotFilePath = shotInfo.canPlaced() ? shotInfo.getSource() : mThemeModel.getFolderPath() + File.separator + shotInfo.getSource();
        // 如果有变速 添加片段时 选取时长，循环添加
        //If there is a variable speed to add the segment when selected time, add cycle
        List<ThemeModel.ShotInfo.SpeedInfo> speeds = shotInfo.getSpeed();
        if (speeds != null && speeds.size() > 0) {
            long startSpeed = 0;//当前追加位置 Current append position
            for (int j = 0; j < speeds.size(); j++) {
                ThemeModel.ShotInfo.SpeedInfo speedInfo = speeds.get(j);
                if (speedInfo != null) {
                    //变速开始位置 Shift starting position
                    long speedStart = NumberUtils.parseString2Long(speedInfo.getStart());
                    long totalSpeedTime = (long) ((NumberUtils.parseString2Long(speedInfo.getEnd()) -
                            NumberUtils.parseString2Long(speedInfo.getStart())) *
                            (speedInfo.getSpeed1()
                                    + speedInfo.getSpeed0()) / 2);
                    if (mTimeline.getDuration() - previewBean.getStartDuration() < speedStart) {
                        //追加原视频 Append original video
                        long addDuration = speedStart - (mTimeline.getDuration() - previewBean.getStartDuration());
                        NvsVideoClip nvsVideoClip = nvsVideoTrack.appendClip(shotFilePath,
                                startSpeed * Constants.US_TIME_BASE, (startSpeed + addDuration) * Constants.US_TIME_BASE);
                        if (nvsVideoClip == null) {
                            Log.e(TAG, "createTimeline VideoTrack.appendClip changeSpeed failed!");
                            continue;
                        }
                        if (!TextUtils.isEmpty(mThemeModel.getMusic())) {
                            nvsVideoClip.setVolumeGain(0, 0);
                        }
                        long duration = mTimeline.getDuration();
                        startSpeed += addDuration;
                        Log.d(TAG, "duration:" + startSpeed);
                    }
                    //追加变速视频 Additional variable speed video
                    NvsVideoClip nvsVideoClip = nvsVideoTrack.appendClip(shotFilePath,
                            startSpeed * Constants.US_TIME_BASE, (startSpeed + totalSpeedTime) * Constants.US_TIME_BASE);
                    if (nvsVideoClip == null) {
                        Log.e(TAG, "createTimeline VideoTrack.appendClip changeSpeed failed!");
                        continue;
                    }
                    if (!TextUtils.isEmpty(mThemeModel.getMusic())) {
                        nvsVideoClip.setVolumeGain(0, 0);
                    }
                    nvsVideoClip.changeVariableSpeed(speedInfo.getSpeed0()
                            , speedInfo.getSpeed1(), true);
                    long duration = mTimeline.getDuration() - previewBean.getStartDuration();
                    startSpeed += totalSpeedTime;
                    Log.d(TAG, "duration:" + startSpeed);
                }
            }
            if (startSpeed < shotInfo.getNeedDuration()) {
                //追加原视频 Append original video
                NvsVideoClip nvsVideoClip = nvsVideoTrack.appendClip(shotFilePath,
                        startSpeed * Constants.US_TIME_BASE, shotInfo.getNeedDuration() * Constants.US_TIME_BASE);
                if (nvsVideoClip == null) {
                    Log.e(TAG, "createTimeline VideoTrack.appendClip changeSpeed failed!");
                    return;
                }
                if (!TextUtils.isEmpty(mThemeModel.getMusic())) {
                    nvsVideoClip.setVolumeGain(0, 0);
                }
                long duration = mTimeline.getDuration() - previewBean.getStartDuration();
                Log.d(TAG, "duration:" + duration);
            }
        } else {
            NvsVideoClip nvsVideoClip = nvsVideoTrack.appendClip(shotFilePath,
                    0, shotInfo.getNeedDuration() * Constants.US_TIME_BASE);
            if (nvsVideoClip == null) {
                Log.e(TAG, "createTimeline VideoTrack.appendClip failed!");
                return;
            }
            if (!TextUtils.isEmpty(mThemeModel.getMusic())) {
                nvsVideoClip.setVolumeGain(0, 0);
            }
        }
    }
}
