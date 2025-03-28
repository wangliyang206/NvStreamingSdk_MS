package com.meishe.base.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author: blankj
 *     blog  : http://blankj.com
 *     time  : 2020/03/19
 *     desc  :
 * </pre>
 * 转场工具类
 * Transition tool class
 */
public class UtilsTransActivity extends AppCompatActivity {

    private static final Map<UtilsTransActivity, TransActivityDelegate> CALLBACK_MAP = new HashMap<>();

    protected static final String EXTRA_DELEGATE = "extra_delegate";

    /**
     * Start.
     * 开始
     * @param delegate the delegate 委托
     */
    public static void start(final TransActivityDelegate delegate) {
        start(null, null, delegate, UtilsTransActivity.class);
    }

    public static void start(final Utils.Consumer<Intent> consumer,
                             final TransActivityDelegate delegate) {
        start(null, consumer, delegate, UtilsTransActivity.class);
    }

    public static void start(final Activity activity,
                             final TransActivityDelegate delegate) {
        start(activity, null, delegate, UtilsTransActivity.class);
    }

    public static void start(final Activity activity,
                             final Utils.Consumer<Intent> consumer,
                             final TransActivityDelegate delegate) {
        start(activity, consumer, delegate, UtilsTransActivity.class);
    }

    protected static void start(final Activity activity,
                                final Utils.Consumer<Intent> consumer,
                                final TransActivityDelegate delegate,
                                final Class<?> cls) {
        if (delegate == null) {
            return;
        }
        Intent starter = new Intent(Utils.getApp(), cls);
        starter.putExtra(EXTRA_DELEGATE, delegate);
        if (consumer != null) {
            consumer.accept(starter);
        }
        if (activity == null) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Utils.getApp().startActivity(starter);
        } else {
            activity.startActivity(starter);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        Serializable extra = getIntent().getSerializableExtra(EXTRA_DELEGATE);
        if (!(extra instanceof TransActivityDelegate)) {
            super.onCreate(savedInstanceState);
            finish();
            return;
        }
        TransActivityDelegate delegate = (TransActivityDelegate) extra;
        CALLBACK_MAP.put(this, delegate);
        delegate.onCreateBefore(this, savedInstanceState);
        super.onCreate(savedInstanceState);
        delegate.onCreated(this, savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onResumed(this);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onStopped(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onSaveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onDestroy(this);
        CALLBACK_MAP.remove(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return;
        }
        callback.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        TransActivityDelegate callback = CALLBACK_MAP.get(this);
        if (callback == null) {
            return super.dispatchTouchEvent(ev);
        }
        if (callback.dispatchTouchEvent(this, ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * The type Trans activity delegate.
     *  转场授权类
     */
    public abstract static class TransActivityDelegate implements Serializable {
        /**
         * On create before.
         * 创建之前
         * @param activity           the activity
         * @param savedInstanceState the saved instance state 保存了实例状态
         */
        public void onCreateBefore(@NonNull UtilsTransActivity activity, @Nullable Bundle savedInstanceState) {/**/}

        /**
         * On created.
         * 创建
         * @param activity           the activity
         * @param savedInstanceState the saved instance state 保存的实例状态
         */
        public void onCreated(@NonNull UtilsTransActivity activity, @Nullable Bundle savedInstanceState) {/**/}

        /**
         * On started.
         * 开始
         * @param activity the activity
         */
        public void onStarted(@NonNull UtilsTransActivity activity) {/**/}

        /**
         * On destroy.
         * 销毁
         * @param activity the activity
         */
        public void onDestroy(@NonNull UtilsTransActivity activity) {/**/}

        /**
         * On resumed.
         * 重新开始
         * @param activity the activity
         */
        public void onResumed(@NonNull UtilsTransActivity activity) {/**/}

        /**
         * On paused.
         * 暂停
         * @param activity the activity
         */
        public void onPaused(@NonNull UtilsTransActivity activity) {/**/}

        /**
         * On stopped.
         * 停止
         * @param activity the activity
         */
        public void onStopped(@NonNull UtilsTransActivity activity) {/**/}

        /**
         * On save instance state.
         * 保存实例状态
         * @param activity the activity
         * @param outState the out state 离开状态
         */
        public void onSaveInstanceState(@NonNull UtilsTransActivity activity, Bundle outState) {/**/}

        /**
         * On request permissions result.
         * 请求权限结果
         * @param activity     the activity
         * @param requestCode  the request code 请求码
         * @param permissions  the permissions 权限
         * @param grantResults the grant results 授予结果
         */
        public void onRequestPermissionsResult(@NonNull UtilsTransActivity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {/**/}

        /**
         * On activity result.
         * activity的结果
         * @param activity    the activity
         * @param requestCode the request code 请求码
         * @param resultCode  the result code 结果码
         * @param data        the data 数据
         */
        public void onActivityResult(@NonNull UtilsTransActivity activity, int requestCode, int resultCode, Intent data) {/**/}

        /**
         * Dispatch touch event boolean.
         * 调度触摸事件
         * @param activity the activity
         * @param ev       the ev
         * @return the boolean
         */
        public boolean dispatchTouchEvent(@NonNull UtilsTransActivity activity, MotionEvent ev) {
            return false;
        }
    }
}
