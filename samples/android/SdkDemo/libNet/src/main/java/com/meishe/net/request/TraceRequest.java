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
package com.meishe.net.request;

import com.meishe.net.model.HttpMethod;
import com.meishe.net.request.base.NoBodyRequest;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * ================================================
 * 作    者：jeasonlzy.Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/12
 * 描    述：Trace请求的实现类，注意需要传入本类的泛型
 * Trace the implementation class of the request, note that you need to pass in the generic type of this class
 * 修订历史：
 * ================================================
 *
 * @param <T> the type parameter
 */
public class TraceRequest<T> extends NoBodyRequest<T, TraceRequest<T>> {

    public TraceRequest(String url) {
        super(url);
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.TRACE;
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = generateRequestBuilder(requestBody);
        return requestBuilder.method("TRACE", requestBody).url(url).tag(tag).build();
    }
}
