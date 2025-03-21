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
package com.meishe.net.server;

import com.meishe.net.db.UploadManager;
import com.meishe.net.model.Progress;
import com.meishe.net.request.base.Request;
import com.meishe.net.server.task.XExecutor;
import com.meishe.net.server.upload.UploadTask;
import com.meishe.net.server.upload.UploadThreadPool;
import com.meishe.net.utils.OkLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================
 * 作    者：jeasonlzy.Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/26
 * 描    述：全局的上传管理
 * Global upload management
 * 修订历史：
 * ================================================
 */
public class OkUpload {

    private Map<String, UploadTask<?>> taskMap;         //所有任务 All Tasks
    private UploadThreadPool threadPool;                //上传的线程池 Thread pool to upload

    public static OkUpload getInstance() {
        return OkUploadHolder.instance;
    }

    private static class OkUploadHolder {
        private static final OkUpload instance = new OkUpload();
    }

    private OkUpload() {
        threadPool = new UploadThreadPool();
        taskMap = new LinkedHashMap<>();

        //校验数据的有效性，防止下载过程中退出，第二次进入的时候，由于状态没有更新导致的状态错误 Verify the validity of the data to prevent the exit during the download process and the status error caused by not updating the status on the second entry
        List<Progress> taskList = UploadManager.getInstance().getUploading();
        for (Progress info : taskList) {
            if (info.status == Progress.WAITING || info.status == Progress.LOADING || info.status == Progress.PAUSE) {
                info.status = Progress.NONE;
            }
        }
        UploadManager.getInstance().replace(taskList);
    }

    /**
     * Request upload task.
     * 请求上传任务
     * @param <T>     the type parameter 类型参数
     * @param tag     the tag 标签
     * @param request the request 请求
     * @return the upload task 上传任务
     */
    public static <T> UploadTask<T> request(String tag, Request<T, ? extends Request> request) {
        Map<String, UploadTask<?>> taskMap = OkUpload.getInstance().getTaskMap();
        //noinspection unchecked
        UploadTask<T> task = (UploadTask<T>) taskMap.get(tag);
        if (task == null) {
            task = new UploadTask<>(tag, request);
            taskMap.put(tag, task);
        }
        return task;
    }

    /**
     * 从数据库中恢复任务  @param <T>  the type parameter
     * Restore the task from the database
     * @param progress the progress 进度
     * @return the upload task 上传任务
     */
    public static <T> UploadTask<T> restore(Progress progress) {
        Map<String, UploadTask<?>> taskMap = OkUpload.getInstance().getTaskMap();
        //noinspection unchecked
        UploadTask<T> task = (UploadTask<T>) taskMap.get(progress.tag);
        if (task == null) {
            task = new UploadTask<>(progress);
            taskMap.put(progress.tag, task);
        }
        return task;
    }

    /**
     * 从数据库中恢复任务  @param progressList the progress list
     * Restore the task from the database
     * @return the list
     */
    public static List<UploadTask<?>> restore(List<Progress> progressList) {
        Map<String, UploadTask<?>> taskMap = OkUpload.getInstance().getTaskMap();
        List<UploadTask<?>> tasks = new ArrayList<>();
        for (Progress progress : progressList) {
            UploadTask<?> task = taskMap.get(progress.tag);
            if (task == null) {
                task = new UploadTask<>(progress);
                taskMap.put(progress.tag, task);
            }
            tasks.add(task);
        }
        return tasks;
    }

    /**
     * 开始所有任务
     * Start all tasks
     */
    public void startAll() {
        for (Map.Entry<String, UploadTask<?>> entry : taskMap.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                OkLogger.w("can't find task with tag = " + entry.getKey());
                continue;
            }
            task.start();
        }
    }

    /**
     * 暂停全部任务
     * Suspend all tasks
     */
    public void pauseAll() {
        //先停止未开始的任务 Stop unstarted tasks first
        for (Map.Entry<String, UploadTask<?>> entry : taskMap.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                OkLogger.w("can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status != Progress.LOADING) {
                task.pause();
            }
        }
        //再停止进行中的任务 Then stop the task in progress
        for (Map.Entry<String, UploadTask<?>> entry : taskMap.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                OkLogger.w("can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status == Progress.LOADING) {
                task.pause();
            }
        }
    }

    /**
     * 删除所有任务
     * Delete all tasks
     */
    public void removeAll() {
        Map<String, UploadTask<?>> map = new HashMap<>(taskMap);
        //先删除未开始的任务 Start by deleting unstarted tasks
        for (Map.Entry<String, UploadTask<?>> entry : map.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                OkLogger.w("can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status != Progress.LOADING) {
                task.remove();
            }
        }
        //再删除进行中的任务 Delete the task in progress
        for (Map.Entry<String, UploadTask<?>> entry : map.entrySet()) {
            UploadTask<?> task = entry.getValue();
            if (task == null) {
                OkLogger.w("can't find task with tag = " + entry.getKey());
                continue;
            }
            if (task.progress.status == Progress.LOADING) {
                task.remove();
            }
        }
    }

    /**
     * Gets thread pool.
     * 线程池
     * @return the thread pool 线程池
     */
    public UploadThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     * Gets task map.
     * 得到任务地图
     * @return  the task map  任务地图
     */
    public Map<String, UploadTask<?>> getTaskMap() {
        return taskMap;
    }

    /**
     * Gets task.
     * 获得任务
     * @param tag the tag 标签
     * @return the task 任务
     */
    public UploadTask<?> getTask(String tag) {
        return taskMap.get(tag);
    }

    /**
     * Has task boolean.
     * 有任务
     * @param tag the tag 标签
     * @return the boolean
     */
    public boolean hasTask(String tag) {
        return taskMap.containsKey(tag);
    }

    /**
     * Remove task upload task.
     * 删除任务上传任务
     * @param tag the tag 标签
     * @return the upload task 上传任务
     */
    public UploadTask<?> removeTask(String tag) {
        return taskMap.remove(tag);
    }

    /**
     * Add on all task end listener.
     * 添加所有任务结束监听器
     * @param listener the listener 监听
     */
    public void addOnAllTaskEndListener(XExecutor.OnAllTaskEndListener listener) {
        threadPool.getExecutor().addOnAllTaskEndListener(listener);
    }

    /**
     * Remove on all task end listener.
     * 删除所有任务结束监听器
     * @param listener the listener 监听
     */
    public void removeOnAllTaskEndListener(XExecutor.OnAllTaskEndListener listener) {
        threadPool.getExecutor().removeOnAllTaskEndListener(listener);
    }
}
