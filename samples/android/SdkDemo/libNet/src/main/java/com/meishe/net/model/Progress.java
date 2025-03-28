/*
 * Copyright 2016 jeasonlzy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meishe.net.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.SystemClock;

import com.meishe.net.OkGo;
import com.meishe.net.request.base.Request;
import com.meishe.net.utils.IOUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy.Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2017/6/1
 * 描    述：
 * 修订历史：
 * ================================================
 * 进度条类
 * The progress bar class
 */
public class Progress implements Serializable {
    private static final long serialVersionUID = 6353658567594109891L;

    public static final int NONE = 0;         //无状态 stateless
    public static final int WAITING = 1;      //等待  wait
    public static final int LOADING = 2;      //下载中 be downloading
    public static final int PAUSE = 3;        //暂停 pause
    public static final int ERROR = 4;        //错误 error
    public static final int FINISH = 5;       //完成 finish

    public static final String TAG = "tag";
    public static final String URL = "url";
    public static final String FOLDER = "folder";
    public static final String FILE_PATH = "filePath";
    public static final String FILE_NAME = "fileName";
    public static final String FRACTION = "fraction";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String CURRENT_SIZE = "currentSize";
    public static final String STATUS = "status";
    public static final String PRIORITY = "priority";
    public static final String DATE = "date";
    public static final String REQUEST = "request";
    public static final String EXTRA1 = "extra1";
    public static final String EXTRA2 = "extra2";
    public static final String EXTRA3 = "extra3";

    /**
     * The Tag.
     * 下载的标识键
     */
    public String tag;
    /**
     * The Url.
     * 网址
     */
    public String url;
    /**
     * The Folder.
     * 保存文件夹
     */
    public String folder;
    /**
     * The File path.
     * 保存文件地址
     */
    public String filePath;
    /**
     * The File name.
     * 保存的文件名
     */
    public String fileName;
    /**
     * The Fraction.
     * 下载的进度，0-1
     */
    public float fraction;
    /**
     * The Total size.
     * 总字节长度, byte
     */
    public long totalSize;
    /**
     * The Current size.
     * 本次下载的大小, byte
     */
    public long currentSize;
    /**
     * The Speed.
     * 网速，byte/s
     */
    public transient long speed;
    /**
     * The Status.
     * 当前状态
     */
    public int status;
    /**
     * The Priority.
     * 任务优先级
     */
    public int priority;
    /**
     * The Date.
     * 创建时间
     */
    public long date;
    /**
     * The Request.
     * 网络请求
     */
    public Request<?, ? extends Request> request;
    /**
     * The Extra 1.
     * 额外的数据
     */
    public Serializable extra1;
    /**
     * The Extra 2.
     * 额外的数据
     */
    public Serializable extra2;
    /**
     * The Extra 3.
     * 额外的数据
     */
    public Serializable extra3;
    /**
     * The Exception.
     * 当前进度出现的异常
     */
    public Throwable exception;

    private transient long tempSize;                //每一小段时间间隔的网络流量 Network traffic at short intervals
    private transient long lastRefreshTime;         //最后一次刷新的时间 The time of the last refresh
    private transient List<Long> speedBuffer;       //网速做平滑的缓存，避免抖动过快 Network speed does smooth cache, avoid jitter too fast

    public Progress() {
        lastRefreshTime = SystemClock.elapsedRealtime();
        totalSize = -1;
        priority = Priority.DEFAULT;
        date = System.currentTimeMillis();
        speedBuffer = new ArrayList<>();
    }

    /**
     * Change progress progress.
     * 改变进度
     * @param progress  the progress 进度
     * @param writeSize the write size  写尺寸
     * @param action    the action 功能
     * @return the progress
     */
    public static Progress changeProgress(Progress progress, long writeSize, Action action) {
        return changeProgress(progress, writeSize, progress.totalSize, action);
    }

    /**
     * Change progress progress.
     * 改变进度
     * @param progress  the progress 进度
     * @param writeSize the write size  写尺寸
     * @param action    the action 功能
     * @return the progress
     */
    public static Progress changeProgress(final Progress progress, long writeSize, long totalSize, final Action action) {
        progress.totalSize = totalSize;
        progress.currentSize += writeSize;
        progress.tempSize += writeSize;

        long currentTime = SystemClock.elapsedRealtime();
        boolean isNotify = (currentTime - progress.lastRefreshTime) >= OkGo.REFRESH_TIME;
        if (isNotify || progress.currentSize == totalSize) {
            long diffTime = currentTime - progress.lastRefreshTime;
            if (diffTime == 0) diffTime = 1;
            progress.fraction = progress.currentSize * 1.0f / totalSize;
            progress.speed = progress.bufferSpeed(progress.tempSize * 1000 / diffTime);
            progress.lastRefreshTime = currentTime;
            progress.tempSize = 0;
            if (action != null) {
                action.call(progress);
            }
        }
        return progress;
    }

    /** 平滑网速，避免抖动过大
     * Smooth network speed, avoid too much jitter
     * */
    private long bufferSpeed(long speed) {
        speedBuffer.add(speed);
        if (speedBuffer.size() > 10) {
            speedBuffer.remove(0);
        }
        long sum = 0;
        for (float speedTemp : speedBuffer) {
            sum += speedTemp;
        }
        return sum / speedBuffer.size();
    }

    /**
     * 转换进度信息  @param progress the progress
     * Conversion progress information
     */
    public void from(Progress progress) {
        totalSize = progress.totalSize;
        currentSize = progress.currentSize;
        fraction = progress.fraction;
        speed = progress.speed;
        lastRefreshTime = progress.lastRefreshTime;
        tempSize = progress.tempSize;
    }

    /**
     * The interface Action.
     * 功能接口
     */
    public interface Action {
        /**
         * Call.
         * 调用
         * @param progress the progress
         */
        void call(Progress progress);
    }

    /**
     * Build content values content values.
     * 构建内容值
     * @param progress the progress
     * @return the content values
     */
    public static ContentValues buildContentValues(Progress progress) {
        ContentValues values = new ContentValues();
        values.put(TAG, progress.tag);
        values.put(URL, progress.url);
        values.put(FOLDER, progress.folder);
        values.put(FILE_PATH, progress.filePath);
        values.put(FILE_NAME, progress.fileName);
        values.put(FRACTION, progress.fraction);
        values.put(TOTAL_SIZE, progress.totalSize);
        values.put(CURRENT_SIZE, progress.currentSize);
        values.put(STATUS, progress.status);
        values.put(PRIORITY, progress.priority);
        values.put(DATE, progress.date);
        values.put(REQUEST, IOUtils.toByteArray(progress.request));
        values.put(EXTRA1, IOUtils.toByteArray(progress.extra1));
        values.put(EXTRA2, IOUtils.toByteArray(progress.extra2));
        values.put(EXTRA3, IOUtils.toByteArray(progress.extra3));
        return values;
    }

    /**
     * Build update content values content values.
     * 生成更新内容值
     * @param progress the progress 进度
     * @return the content values 内容值
     */
    public static ContentValues buildUpdateContentValues(Progress progress) {
        ContentValues values = new ContentValues();
        values.put(FRACTION, progress.fraction);
        values.put(TOTAL_SIZE, progress.totalSize);
        values.put(CURRENT_SIZE, progress.currentSize);
        values.put(STATUS, progress.status);
        values.put(PRIORITY, progress.priority);
        values.put(DATE, progress.date);
        return values;
    }

    /**
     * Parse cursor to bean progress.
     * 解析游标到bean进程
     * @param cursor the cursor 游标
     * @return the progress 进度
     */
    public static Progress parseCursorToBean(Cursor cursor) {
        Progress progress = new Progress();
        progress.tag = cursor.getString(cursor.getColumnIndex(Progress.TAG));
        progress.url = cursor.getString(cursor.getColumnIndex(Progress.URL));
        progress.folder = cursor.getString(cursor.getColumnIndex(Progress.FOLDER));
        progress.filePath = cursor.getString(cursor.getColumnIndex(Progress.FILE_PATH));
        progress.fileName = cursor.getString(cursor.getColumnIndex(Progress.FILE_NAME));
        progress.fraction = cursor.getFloat(cursor.getColumnIndex(Progress.FRACTION));
        progress.totalSize = cursor.getLong(cursor.getColumnIndex(Progress.TOTAL_SIZE));
        progress.currentSize = cursor.getLong(cursor.getColumnIndex(Progress.CURRENT_SIZE));
        progress.status = cursor.getInt(cursor.getColumnIndex(Progress.STATUS));
        progress.priority = cursor.getInt(cursor.getColumnIndex(Progress.PRIORITY));
        progress.date = cursor.getLong(cursor.getColumnIndex(Progress.DATE));
        progress.request = (Request<?, ? extends Request>) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.REQUEST)));
        progress.extra1 = (Serializable) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.EXTRA1)));
        progress.extra2 = (Serializable) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.EXTRA2)));
        progress.extra3 = (Serializable) IOUtils.toObject(cursor.getBlob(cursor.getColumnIndex(Progress.EXTRA3)));
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Progress progress = (Progress) o;
        return tag != null ? tag.equals(progress.tag) : progress.tag == null;

    }

    @Override
    public int hashCode() {
        return tag != null ? tag.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Progress{" +//
               "fraction=" + fraction +//
               ", totalSize=" + totalSize +//
               ", currentSize=" + currentSize +//
               ", speed=" + speed +//
               ", status=" + status +//
               ", priority=" + priority +//
               ", folder=" + folder +//
               ", filePath=" + filePath +//
               ", fileName=" + fileName +//
               ", tag=" + tag +//
               ", url=" + url +//
               '}';
    }
}
