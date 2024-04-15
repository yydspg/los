package com.los.core.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

/*
 * @author paul 2024/1/30
 */

public class JSONKit {

    private static final JSONWriter.Feature[] WRITER_FEATURES = {
            WriteNulls,
            WriteNullListAsEmpty,
            WriteNullStringAsEmpty,
            WriteNullBooleanAsFalse,
            NotWriteEmptyArray
    };

    public static String toString(Object o){
        return JSON.toJSONString(o);
    }
    public static String toFeatureString(Object o){
        return JSONObject.toJSONString(o,WRITER_FEATURES);
    }

    public static <T> T toObject(String json,Class<T> t){
        return  JSON.parseObject(json,t);
    }
    public static Object toObject(String json){
        return JSONObject.parse(json);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> toCollect(String s) {
        return (Map<K, V>) JSONObject.parseObject(s);
    }

    public static <K, V> String collectToString(Map<K, V> m) {
        return JSONObject.toJSONString(m);
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    public static JSONArray listToArray(String json){
        return JSON.parseArray(json);
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    public static JSONObject parseObj(String str) {
        return JSONObject.parseObject(str);
    }
    public static JSONArray convert(String key,String ... values) {
        JSONArray jsonArray = new JSONArray();
        for (String value : values) {
            JSONObject entry = new JSONObject();
            entry.put(key,value);
            jsonArray.add(entry);
        }
        return jsonArray;
    }
    public static JSONObject build() {
        return new JSONObject();
    }
    public static JSONObject convert(String k,Object v) {
        return JSONObject.of(k,v);
    }
}
