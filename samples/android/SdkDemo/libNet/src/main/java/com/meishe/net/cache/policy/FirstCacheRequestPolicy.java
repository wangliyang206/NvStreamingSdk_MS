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
package com.meishe.net.cache.policy;

import com.meishe.net.cache.CacheEntity;
import com.meishe.net.callback.Callback;
import com.meishe.net.model.Response;
import com.meishe.net.request.base.Request;

/**
 * ================================================
 * 作    者：jeasonlzy.Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2017/5/25
 * 描    述：
 * 修订历史：
 * ================================================
 *
 * @param <T> the type parameter
 * 第一缓存请求策略类
 * The first cache is the request policy class
 */
public class FirstCacheRequestPolicy<T> extends BaseCachePolicy<T> {
    public FirstCacheRequestPolicy(Request<T, ? extends Request> request) {
        super(request);
    }

    @Override
    public void onSuccess(final Response<T> success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(success);
                mCallback.onFinish();
            }
        });
    }

    @Override
    public void onError(final Response<T> error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(error);
                mCallback.onFinish();
            }
        });
    }

    @Override
    public Response<T> requestSync(CacheEntity<T> cacheEntity) {
        try {
            prepareRawCall();
        } catch (Throwable throwable) {
            return Response.error(false, rawCall, null, throwable);
        }
        /*
        * 同步请求，不能返回两次，只返回正确的数据
        * Synchronous request, cannot return twice, returns only the correct data
        * */
        Response<T> response;
        if (cacheEntity != null) {
            response = Response.success(true, cacheEntity.getData(), rawCall, null);
        }
        response = requestNetworkSync();
        if (!response.isSuccessful() && cacheEntity != null) {
            response = Response.success(true, cacheEntity.getData(), rawCall, response.getRawResponse());
        }
        return response;
    }

    @Override
    public void requestAsync(final CacheEntity<T> cacheEntity, Callback<T> callback) {
        mCallback = callback;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onStart(request);

                try {
                    prepareRawCall();
                } catch (Throwable throwable) {
                    Response<T> error = Response.error(false, rawCall, null, throwable);
                    mCallback.onError(error);
                    return;
                }
                if (cacheEntity != null) {
                    Response<T> success = Response.success(true, cacheEntity.getData(), rawCall, null);
                    mCallback.onCacheSuccess(success);
                }
                requestNetworkAsync();
            }
        });
    }
}
