
package com.meishe.sdkdemo.utils;


import android.os.AsyncTask;

import java.util.concurrent.Executor;


/**
 * 线程池辅助类，整个应用程序就只有一个线程池去管理线程。 可以设置核心线程数、最大线程数、
 * 额外线程空状态生存时间，阻塞队列长度来优化线程池。下面的数据都是参考Android的AsynTask里的数据。
 *
 *
 * Thread pool auxiliary class, the entire application has only one thread pool to manage threads. You can set the number of core threads, the maximum number of threads, the empty thread lifetime,
 * and the blocking queue length to optimize the thread pool. The following data refer to the data in Android's AsynTask.
 */
public class ThreadPoolUtils {

    /*
    * 线程池
    * Thread Pool
    * */
    private static Executor threadPool;

    static {
        threadPool = AsyncTask.THREAD_POOL_EXECUTOR;
    }

    /**
     * 从线程池中抽取线程，执行指定的Runnable对象
     * Extract threads from the thread pool and execute the specified Runnable object
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public static Executor getExecutor(){
        return threadPool;
    }

}