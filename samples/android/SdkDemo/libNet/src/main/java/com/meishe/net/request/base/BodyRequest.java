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
package com.meishe.net.request.base;

import android.text.TextUtils;

import com.meishe.net.model.HttpHeaders;
import com.meishe.net.model.HttpParams;
import com.meishe.net.utils.HttpUtils;
import com.meishe.net.utils.OkLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * ================================================
 * 作    者：jeasonlzy.Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：16/8/9
 * 描    述：
 * 修订历史：
 * ================================================
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 * 请求体类
 * Request body class
 */
public abstract class BodyRequest<T, R extends BodyRequest> extends Request<T, R> implements HasBody<R> {
    private static final long serialVersionUID = -6459175248476927501L;

    /**
     * The Media type.
     * 上传的MIME类型
     */
    protected transient MediaType mediaType;
    /**
     * The Content.
     * 上传的文本内容
     */
    protected String content;
    /**
     * The Bs.
     * 上传的字节数据
     */
    protected byte[] bs;
    /**
     * The File.
     * 单纯的上传一个文件
     */
    protected transient File file;

    /**
     * The Is multipart.
     * 是否强制使用 multipart/form-data 表单上传
     */
    protected boolean isMultipart = false;
    /**
     * The Is splice url.
     * 是否拼接url参数
     */
    protected boolean isSpliceUrl = false;
    /**
     * The Request body.
     * 请求体
     */
    protected RequestBody requestBody;

    public BodyRequest(String url) {
        super(url);
    }

    @SuppressWarnings("unchecked")
    @Override
    public R isMultipart(boolean isMultipart) {
        this.isMultipart = isMultipart;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R isSpliceUrl(boolean isSpliceUrl) {
        this.isSpliceUrl = isSpliceUrl;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R params(String key, File file) {
        params.put(key, file);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R addFileParams(String key, List<File> files) {
        params.putFileParams(key, files);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R addFileWrapperParams(String key, List<HttpParams.FileWrapper> fileWrappers) {
        params.putFileWrapperParams(key, fileWrappers);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R params(String key, File file, String fileName) {
        params.put(key, file, fileName);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R params(String key, File file, String fileName, MediaType contentType) {
        params.put(key, file, fileName, contentType);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R upRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return (R) this;
    }

    /**
     *  注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upString(String string) {
        this.content = string;
        this.mediaType = HttpParams.MEDIA_TYPE_PLAIN;
        return (R) this;
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * 该方法用于定制请求content-type
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * This method is used to customize the request Content-Type
     */
    @SuppressWarnings("unchecked")
    @Override
    public R upString(String string, MediaType mediaType) {
        this.content = string;
        this.mediaType = mediaType;
        return (R) this;
    }

    /** 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upJson(String json) {
        this.content = json;
        this.mediaType = HttpParams.MEDIA_TYPE_JSON;
        return (R) this;
    }

    /** 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upJson(JSONObject jsonObject) {
        this.content = jsonObject.toString();
        this.mediaType = HttpParams.MEDIA_TYPE_JSON;
        return (R) this;
    }

    /** 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upJson(JSONArray jsonArray) {
        this.content = jsonArray.toString();
        this.mediaType = HttpParams.MEDIA_TYPE_JSON;
        return (R) this;
    }

    /** 注意使用该方法上传字 符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upBytes(byte[] bs) {
        this.bs = bs;
        this.mediaType = HttpParams.MEDIA_TYPE_STREAM;
        return (R) this;
    }

    /** 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upBytes(byte[] bs, MediaType mediaType) {
        this.bs = bs;
        this.mediaType = mediaType;
        return (R) this;
    }

    /** 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upFile(File file) {
        this.file = file;
        this.mediaType = HttpUtils.guessMimeType(file.getName());
        return (R) this;
    }

    /** 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     * Note that uploading the string using this method clears out all other parameters in the entity and does not clear the header information
     * */
    @SuppressWarnings("unchecked")
    @Override
    public R upFile(File file, MediaType mediaType) {
        this.file = file;
        this.mediaType = mediaType;
        return (R) this;
    }

    @Override
    public RequestBody generateRequestBody() {
        if (isSpliceUrl) url = HttpUtils.createUrlFromParams(baseUrl, params.urlParamsMap);

        if (requestBody != null) return requestBody;                                                //自定义的请求体 Custom request body
        if (content != null && mediaType != null) return RequestBody.create(mediaType, content);    //上传字符串数据 Upload string data
        if (bs != null && mediaType != null) return RequestBody.create(mediaType, bs);              //上传字节数组 Upload byte array
        if (file != null && mediaType != null) return RequestBody.create(mediaType, file);          //上传一个文件 Upload a file
        return HttpUtils.generateMultipartRequestBody(params, isMultipart);
    }

    /**
     * Generate request builder okhttp 3 . request . builder.
     * 生成请求生成器okhttp请求。构建器
     * @param requestBody the request body
     * @return the okhttp 3 . request . builder
     */
    protected okhttp3.Request.Builder generateRequestBuilder(RequestBody requestBody) {
        try {
            headers(HttpHeaders.HEAD_KEY_CONTENT_LENGTH, String.valueOf(requestBody.contentLength()));
        } catch (IOException e) {
            OkLogger.printStackTrace(e);
        }
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        return HttpUtils.appendHeaders(requestBuilder, headers);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(mediaType == null ? "" : mediaType.toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String mediaTypeString = (String) in.readObject();
        if (!TextUtils.isEmpty(mediaTypeString)) {
            mediaType = MediaType.parse(mediaTypeString);
        }
    }
}
