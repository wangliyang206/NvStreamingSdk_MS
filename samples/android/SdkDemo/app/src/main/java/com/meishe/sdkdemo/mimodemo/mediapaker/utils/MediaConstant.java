package com.meishe.sdkdemo.mimodemo.mediapaker.utils;

/**
 * Created by ms on 2018/5/29.
 */

public class MediaConstant {

    /**
     * 媒体类型
     * Media type
     */
    public static final String MEDIA_TYPE = "media_type";
    /**
     * 多选限制
     * Multiple choice restriction
     */
    public static final String LIMIT_COUNT = "limitMediaCount";

    /**
     * 音乐和视频类型
     * Music and video genres
     */
    public static final int ALL_MEDIA = 0;
    public static final int VIDEO = 1;
    public static final int IMAGE = 2;
    public static final int[] MEDIATYPECOUNT = {ALL_MEDIA, VIDEO, IMAGE};
    /**
     * 单选和多选
     * Single and multiple options
     */
    public static final String KEY_CLICK_TYPE = "clickType";
    public static final int TYPE_ITEMCLICK_SINGLE = 0;
    public static final int TYPE_ITEMCLICK_MULTIPLE = 1;

}
