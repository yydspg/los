package com.los.core.model;

import com.alibaba.fastjson2.JSONObject;

import java.io.Serializable;

/**
 * 封装公共处理函数
 * @author paul 2024/1/30
 */

public class BaseModel implements Serializable {

    /* ext参数, 用作扩展参数， 会在转换为api数据时自动将ext全部属性放置在对象的主属性上, 并且不包含ext属性   **/

    /* api接口扩展字段， 当包含该字段时 将自动填充到实体对象属性中如{id:1, ext:{abc:222}}  则自动转换为： {id:1, abc:222}，
      需配合ResponseBodyAdvice使用 **/
    private JSONObject ext;
    /* 获取 **/
    public JSONObject getExt() {
        return ext;}
    /* 设置扩展字段 **/
    public BaseModel setExt (String k,Object v){
        if (ext == null) {
            ext = new JSONObject();
        }
        ext.put(k,v);
        return this;
    }
    /** get ext value  可直接使用JSONObject对象的函数 **/
    public JSONObject extV() {
        return ext == null ? new JSONObject(): ext ;}
}
