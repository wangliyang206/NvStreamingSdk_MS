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
package com.meishe.net.server.download;

import com.meishe.net.server.task.PriorityBlockingQueue;
import com.meishe.net.server.task.XExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ================================================
 * 作    者：jeasonlzy.Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/19
 * 描    述：下载管理的线程池
 * Download the managed thread pool
 * 修订历史：
 * ================================================
 */
public class DownloadThreadPool {
    private static final int MAX_POOL_SIZE = 5;          //最大线程池的数量 Maximum number of thread pools
    private static final int KEEP_ALIVE_TIME = 1;        //存活的时间 Survival time
    private static final TimeUnit UNIT = TimeUnit.HOURS; //时间单位  unit of time
    private int corePoolSize = 3;                        //核心线程池的数量，同时能执行的线程数量，默认3个 The number of core thread pools, and the number of threads that can execute at the same time, defaults to 3
    private XExecutor executor;               //线程池执行器 Thread pool executor

    /**
     * Gets executor.
     * 获取执行器
     * @return the executor
     */
    public XExecutor getExecutor() {
        if (executor == null) {
            synchronized (DownloadThreadPool.class) {
                if (executor == null) {
                    executor = new XExecutor(corePoolSize, MAX_POOL_SIZE, KEEP_ALIVE_TIME, UNIT, //
                                             new PriorityBlockingQueue<Runnable>(),   //无限容量的缓冲队列  Unlimited capacity buffer queue
                                             Executors.defaultThreadFactory(),        //线程创建工厂  Thread creation factory
                                             new ThreadPoolExecutor.AbortPolicy());   //继续超出上限的策略，阻止 Continue to exceed the cap policy, block
                }
            }
        }
        return executor;
    }

    /**
     * 必须在首次执行前设置，否者无效 ,范围1-5之间  @param corePoolSize the core pool size
     * It must be set before the first execution, or it will be invalid. The range is 1-5
     */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize <= 0) corePoolSize = 1;
        if (corePoolSize > MAX_POOL_SIZE) corePoolSize = MAX_POOL_SIZE;
        this.corePoolSize = corePoolSize;
    }

    /**
     * 执行任务  @param runnable the runnable
     * execute the task
     */
    public void execute(Runnable runnable) {
        if (runnable != null) {
            getExecutor().execute(runnable);
        }
    }

    /**
     * 移除线程  @param runnable the runnable
     * Remove the thread
     */
    public void remove(Runnable runnable) {
        if (runnable != null) {
            getExecutor().remove(runnable);
        }
    }
}
