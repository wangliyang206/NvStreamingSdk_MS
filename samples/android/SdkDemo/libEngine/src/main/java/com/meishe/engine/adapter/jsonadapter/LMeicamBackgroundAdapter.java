package com.meishe.engine.adapter.jsonadapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.meishe.base.utils.LogUtils;
import com.meishe.engine.adapter.LGsonContext;
import com.meishe.engine.local.LMeicamFxParam;
import com.meishe.engine.local.background.LMeicamBackgroundStory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * author：yangtailin on 2020/8/15 15:44
 * Meicam背景适配器
 * Meicam background adapter
 */
public class LMeicamBackgroundAdapter extends BaseTypeAdapter<LMeicamBackgroundStory> {
    private final static  String TAG = "LMeicamBackgroundAdapter";

    @Override
    public Class<LMeicamBackgroundStory> getClassOfT() {
        return LMeicamBackgroundStory.class;
    }

    @Override
    protected JsonElement parseReadData(JsonElement jsonObject) {
       LogUtils.d( "parseReadData before " + jsonObject);
        JsonObject rootJson = jsonObject.getAsJsonObject();
        JsonElement fxParams = rootJson.get("fxParams");
        if (fxParams != null && !fxParams.isJsonArray()) {
            fxParams = rootJson.remove("fxParams");
            Gson mapGson = new GsonBuilder().enableComplexMapKeySerialization().create();
            Type type = new TypeToken<Map<String, LMeicamFxParam>>() {
            }.getType();
            Map<String, LMeicamFxParam> mapData = mapGson.fromJson(fxParams, type);
            List<LMeicamFxParam> listData = new ArrayList<>();
            if (mapData != null && !mapData.isEmpty()) {
                Set<String> keySet = mapData.keySet();
                for (String key : keySet) {
                    listData.add(mapData.get(key));
                }
            }
            Type listType = new TypeToken<List<LMeicamFxParam>>() {
            }.getType();
            JsonElement jsonElement = LGsonContext.getInstance().getCommonGson().toJsonTree(listData, listType);
            rootJson.add("fxParams", jsonElement);
        }
       LogUtils.d( "parseReadData after " + rootJson);
        return rootJson;
    }

    @Override
    protected LMeicamBackgroundStory parseToObject(JsonElement jsonObject) {
        return LGsonContext.getInstance().getCommonGson().fromJson(jsonObject, getClassOfT());
    }
}
