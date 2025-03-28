package com.meishe.sdkdemo.utils;

import android.text.TextUtils;

import com.meishe.sdkdemo.mimodemo.bean.MiMoLocalData;
import com.meishe.sdkdemo.mimodemo.common.Constants;
import com.meishe.sdkdemo.mimodemo.common.template.model.RepeatInfo;
import com.meishe.sdkdemo.mimodemo.common.template.model.ShotDataInfo;
import com.meishe.sdkdemo.mimodemo.common.template.model.ShotInfo;
import com.meishe.sdkdemo.mimodemo.common.template.model.ShotVideoInfo;
import com.meishe.sdkdemo.mimodemo.common.template.model.SpeedInfo;
import com.meishe.sdkdemo.mimodemo.common.template.model.SubTrackFilterInfo;
import com.meishe.sdkdemo.mimodemo.common.template.model.TrackClipInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MimoFileDataUtil {

    public static final float DIFF_TIME_VALUE = 5f;

    public static void updateShotClipInfos(MiMoLocalData templateInfo) {
        if (templateInfo == null) {
            return;
        }
        //设置时间线扫换默认素材源路径
        //Set the default material source path for time line scanning
        String timeLineTransSource = templateInfo.getTimeLineTransSource();
        if (!TextUtils.isEmpty(timeLineTransSource)) {
            templateInfo.setTransSourcePath(templateInfo.getSourceDir() + File.separator + timeLineTransSource);
        }
        List<ShotInfo> shotInfos = templateInfo.getShotInfos();
        if (shotInfos == null || shotInfos.isEmpty()) {
            return;
        }
        boolean isTimelineTrans = templateInfo.isTimelineTrans();
        String transSourcePath = templateInfo.getTransSourcePath();
        //存储当前镜头需要的所有视频片段信息，trackTotalClipInfos.size()表示当前镜头需要的源视频个数
        //trackTotalClipInfos.size() represents the number of source videos required by the current lens
        List<ShotDataInfo> totalShotDataInfos = new ArrayList<>();
        for (ShotInfo shotInfo : shotInfos) {
            if (shotInfo == null) {
                continue;
            }
            String mainTrackFilter = shotInfo.getMainTrackFilter();
            handleSpeedInfoDisconnect(shotInfo);
            ShotDataInfo shotDataInfo = new ShotDataInfo();
            ShotVideoInfo shotVideoInfo = getRepeatClipInfos(shotInfo, mainTrackFilter, 0);
            if (shotVideoInfo != null && !shotVideoInfo.getTrackClipInfos().isEmpty()) {
                //作反复动作 repeat
                shotDataInfo.setMainTrackVideoInfo(shotVideoInfo);
            } else {
                //没有反复动作 No repetition
                ShotVideoInfo mainTrackVideoInfo = appendTrackClipInfos(shotInfo, mainTrackFilter, 0);
                if (mainTrackVideoInfo != null && !mainTrackVideoInfo.getTrackClipInfos().isEmpty()) {
                    shotDataInfo.setMainTrackVideoInfo(mainTrackVideoInfo);
                }
            }

            //子轨道视频列表 Subtrack video list
            List<SubTrackFilterInfo> subTrackFilter = shotInfo.getSubTrackFilter();
            if (subTrackFilter != null && !subTrackFilter.isEmpty()) {
                int subTrackCount = subTrackFilter.size();
                List<ShotVideoInfo> subTrackVideoInfos = new ArrayList<>();
                for (int subTrackIndex = 0; subTrackIndex < subTrackCount; subTrackIndex++) {
                    SubTrackFilterInfo subTrackFilterInfo = subTrackFilter.get(subTrackIndex);
                    if (subTrackFilterInfo == null) {
                        continue;
                    }
                    String subTrackFilterName = subTrackFilterInfo.getFilterName();
                    ShotVideoInfo subTrackVideoInfo = appendTrackClipInfos(shotInfo, subTrackFilterName, subTrackIndex + 1);
                    if (subTrackVideoInfo != null && !subTrackVideoInfo.getTrackClipInfos().isEmpty()) {
                        subTrackVideoInfos.add(subTrackVideoInfo);
                    }
                }
                shotDataInfo.setSubTrackVideoInfos(subTrackVideoInfos);
            } else {
                //没有子轨道，如果是时间线扫换模板，需要添加上默认扫换素材源路径
                //There is no subtrack. If it is a timeline scan template, you need to add the default scan material source path
                if (isTimelineTrans && !TextUtils.isEmpty(transSourcePath)) {
                    ShotVideoInfo subTrackVideoInfo = appendTrackClipInfos(shotInfo, null, 1);
                    subTrackVideoInfo.setVideoClipPath(transSourcePath);
                    List<ShotVideoInfo> subTrackVideoInfos = new ArrayList<>();
                    subTrackVideoInfos.add(subTrackVideoInfo);
                    shotDataInfo.setSubTrackVideoInfos(subTrackVideoInfos);
                }
            }
            totalShotDataInfos.add(shotDataInfo);
        }
        templateInfo.setShotDataInfos(totalShotDataInfos);
    }

    //处理速度start值与上一个速度end不衔接的问题
    //Handle the problem that the speed start value does not correspond to the previous speed end value
    private static void handleSpeedInfoDisconnect(ShotInfo shotInfo) {
        if (shotInfo == null) {
            return;
        }
        List<SpeedInfo> speedInfos = shotInfo.getSpeed();
        if (speedInfos == null || speedInfos.isEmpty()) {
            return;
        }
        List<SpeedInfo> insertSpeedInfos = new ArrayList<>();
        List<Integer> insertIndexList = new ArrayList<>();
        int speedCpunt = speedInfos.size();
        float prevSpeedEnd = 0f;
        for (int index = 0; index < speedCpunt; index++) {
            SpeedInfo speedInfo = speedInfos.get(index);
            if (speedInfo == null) {
                continue;
            }
            float start = speedInfo.getStart();
            if (index > 0 && prevSpeedEnd > 0f && !isEqualSpeedStartWithEnd(prevSpeedEnd, start)) {
                SpeedInfo newSpeedInfo = new SpeedInfo(prevSpeedEnd, start, 1f, 1f);
                insertSpeedInfos.add(newSpeedInfo);
                insertIndexList.add(index);
            }
            prevSpeedEnd = speedInfo.getEnd();
        }
        int insertCount = insertSpeedInfos.size();
        if (!insertSpeedInfos.isEmpty()) {
            for (int insertIndex = 0; insertIndex < insertCount; insertIndex++) {
                SpeedInfo newSpeedInfo = insertSpeedInfos.get(insertIndex);
                int insertPosition = insertIndexList.get(insertIndex);
                speedInfos.add(insertPosition, newSpeedInfo);
            }
        }
    }

    //判断上一个速度信息的end值与下一个速度信息的start值是否相同,返回true,表示衔接，false则相反
    //Check whether the end value of the previous speed information is the same as the start value of the next speed information. Return true, indicating cohesion; false is the opposite
    private static boolean isEqualSpeedStartWithEnd(float end, float start) {
        float timeDiff = start - end;
        if (timeDiff >= -DIFF_TIME_VALUE && timeDiff <= DIFF_TIME_VALUE) {
            return true;
        }
        return false;
    }

    private static ShotVideoInfo getRepeatClipInfos(ShotInfo shotInfo, String trackFilter, int trackIndex) {
        List<RepeatInfo> repeatInfos = shotInfo.getRepeat();
        if (repeatInfos == null || repeatInfos.isEmpty()) {
            return null;
        }

        int shot = shotInfo.getShot();
        String filter = shotInfo.getFilter();
        String transition = shotInfo.getTrans();
        List<TrackClipInfo> videoClipInfos = new ArrayList<>();
        int repeatCount = repeatInfos.size();

        long totalRealNeedDuration = 0;
        long trimIn = 0;
        float prevEnd = 0f;
        for (int repeatIndex = 0; repeatIndex < repeatCount; repeatIndex++) {
            RepeatInfo repeatInfo = repeatInfos.get(repeatIndex);
            if (repeatInfo == null) {
                continue;
            }
            float start = repeatInfo.getStart();
            long startTimeInterval = millisecondToMicrosecond(start - prevEnd);
            long originDuration = millisecondToMicrosecond(repeatInfo.getOriginDuration());
            prevEnd = repeatInfo.getEnd();
            int repeat = repeatInfo.getCount();
            for (int index = 0; index < repeat; index++) {
                //正放视频片段 We're playing video clips now
                long clipRealDuration = originDuration + startTimeInterval;
                TrackClipInfo videoClipInfo = new TrackClipInfo(
                        trackIndex, 1f, 1f, null, filter, trackFilter,
                        false, 1, trimIn, clipRealDuration);
                //倒放视频片段 Play the video clip backwards
                TrackClipInfo oppositeVideoClipInfo = new TrackClipInfo(
                        trackIndex, 1f, 1f, null, filter, trackFilter,
                        true, 2, 0, originDuration);
                videoClipInfos.add(videoClipInfo);
                videoClipInfos.add(oppositeVideoClipInfo);
            }
            //第二个正放视频的裁剪入点  Cropping entry point for the second playing video
            long secondClipTrimIn = trimIn + startTimeInterval;
            //正放视频片段 We're playing video clips now
            TrackClipInfo secondVideoClipInfo = new TrackClipInfo(
                    0, 1f, 1f, transition, filter, trackFilter,
                    false, 3, secondClipTrimIn, originDuration);
            videoClipInfos.add(secondVideoClipInfo);
            trimIn += (startTimeInterval + originDuration);
            totalRealNeedDuration = trimIn;
        }

        //根据speedInfo查找片段信息，设置片段速度
        //Find the fragment information according to the speedInfo and set the fragment speed
        List<SpeedInfo> speedInfos = shotInfo.getSpeed();
        if (speedInfos != null && !speedInfos.isEmpty()) {
            int speedCount = speedInfos.size();
            int clipInfoCount = videoClipInfos.size();
            long clipTrimIn = 0, clipTrimOut = 0;
            for (int clipIndex = 0; clipIndex < clipInfoCount; clipIndex++) {
                TrackClipInfo clipInfo = videoClipInfos.get(clipIndex);
                if (clipInfo == null) {
                    continue;
                }
                long clipNeedDuration = clipInfo.getRealNeedDuration();
                clipTrimOut += clipNeedDuration;
                long speedTrimIn = 0, speedTrimOut = 0;
                for (int speedIndex = 0; speedIndex < speedCount; speedIndex++) {
                    SpeedInfo speedInfo = speedInfos.get(speedIndex);
                    if (speedInfo == null) {
                        continue;
                    }
                    //计算所需要的原始视频的时长
                    //Calculate the length of the original video required
                    float speed0 = speedInfo.getSpeed0();
                    float speed1 = speedInfo.getSpeed1();
                    float diffDuration = speedInfo.getEnd() - speedInfo.getStart();
                    float speedValue = (speed0 + speed1) / 2;
                    long needDuration = millisecondToMicrosecond(diffDuration * speedValue);
                    speedTrimOut += needDuration;
                    if (clipTrimIn >= speedTrimIn && clipTrimOut <= speedTrimOut) {
                        clipInfo.setSpeed0(speed0);
                        clipInfo.setSpeed1(speed1);
                    }
                    speedTrimIn += needDuration;
                }
                clipTrimIn += clipNeedDuration;
            }
        }
        long durationBySpeed = millisecondToMicrosecond(shotInfo.getDuration());
        ShotVideoInfo shotVideoInfo = new ShotVideoInfo(videoClipInfos,
                totalRealNeedDuration, durationBySpeed, shot, trackIndex, true,
                shotInfo.getFilter(), (long) (shotInfo.getDuration() * 1000));
        return shotVideoInfo;
    }

    //获取变速的视频片段信息
    //Get video footage of the speed change
    private static List<TrackClipInfo> getVideoClipInfosBySpeed(List<SpeedInfo> speedInfos,
                                                                int trachIndex,
                                                                String trans,
                                                                String filter,
                                                                String trackFilter,
                                                                boolean isReverse) {
        if (speedInfos == null || speedInfos.isEmpty()) {
            return null;
        }
        long videoTrimIn = 0;
        int speedCount = speedInfos.size();
        List<TrackClipInfo> shotClipInfos = new ArrayList<>();
        for (int speedIndex = 0; speedIndex < speedCount; speedIndex++) {
            SpeedInfo speedInfo = speedInfos.get(speedIndex);
            if (speedInfo == null) {
                continue;
            }
            //计算所需要的原始视频的时长
            //Calculate the length of the original video required
            float diffDuration = speedInfo.getEnd() - speedInfo.getStart();
            float speedValue = (speedInfo.getSpeed0() + speedInfo.getSpeed1()) / 2;
            long realNeedDuration = millisecondToMicrosecond(diffDuration * speedValue);
            TrackClipInfo videoClipInfo = new TrackClipInfo(
                    trachIndex, speedInfo.getSpeed0(), speedInfo.getSpeed1(),
                    null, filter, trackFilter, isReverse,
                    0, videoTrimIn, realNeedDuration);
            if (speedIndex == speedCount - 1) {
                videoClipInfo.setTrans(trans);
            }
            shotClipInfos.add(videoClipInfo);
            videoTrimIn += realNeedDuration;
        }
        return shotClipInfos;
    }

    public static long millisecondToMicrosecond(float millisecond) {
        return (long) (millisecond * Constants.BASE_MILLISECOND);
    }

    private static ShotVideoInfo appendTrackClipInfos(ShotInfo shotInfo, String trackFilter, int trackIndex) {
        if (shotInfo == null) {
            return null;
        }
        int shot = shotInfo.getShot();
        String filter = shotInfo.getFilter();
        String transition = shotInfo.getTrans();
        boolean isReverse = shotInfo.isReverse();
        List<TrackClipInfo> videoClipInfos = new ArrayList<>();
        List<TrackClipInfo> trackClipInfoBySpeed = getVideoClipInfosBySpeed(shotInfo.getSpeed(),
                trackIndex,
                transition,
                filter,
                trackFilter,
                isReverse);
        if (trackClipInfoBySpeed != null && !trackClipInfoBySpeed.isEmpty()) {
            videoClipInfos.addAll(trackClipInfoBySpeed);
        } else {
            //没有变速列表 No variable speed list
            long realNeedDuration = millisecondToMicrosecond(shotInfo.getDuration());
            TrackClipInfo videoClipInfo = new TrackClipInfo(
                    trackIndex, 1f, 1f,
                    transition, filter, trackFilter, isReverse,
                    0, 0, realNeedDuration);
            videoClipInfos.add(videoClipInfo);
        }
        long realNeedDuration = 0;
        int clipCount = videoClipInfos.size();
        //计算实际需要的视频视频时长
        //Calculate the required video duration
        for (int clipIndex = 0; clipIndex < clipCount; clipIndex++) {
            TrackClipInfo clipInfo = videoClipInfos.get(clipIndex);
            if (clipInfo == null) {
                continue;
            }
            realNeedDuration += clipInfo.getRealNeedDuration();
        }

        long durationBySpeed = millisecondToMicrosecond(shotInfo.getDuration());
        ShotVideoInfo shotVideoInfo = new ShotVideoInfo(videoClipInfos,
                realNeedDuration, durationBySpeed, shot, trackIndex, isReverse,
                shotInfo.getFilter(), (long) (shotInfo.getDuration() * 1000));
        return shotVideoInfo;
    }
}
