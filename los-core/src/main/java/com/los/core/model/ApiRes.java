package com.los.core.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.utils.DateKit;
import com.los.core.utils.JSONKit;
import com.los.core.utils.SecKit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 * 接口返回对象,并提供对应操作接口
 * @author paul 2024/1/30
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRes implements Serializable {

    private Integer code;
    private String msg;
    private Object data;
    private String sign;
    private final long timestamp = DateKit.currentTimeMillis();

    public String toJSONString() {return JSON.toJSONString(this);}

    public static <T> ApiRes success(T data) {
        return new ApiRes(ApiCodeEnum.SUCCESS.getCode(),ApiCodeEnum.SUCCESS.getMsg(),data,null);
    }
    public static ApiRes success() {
        return success(null);
    }
    public static ApiRes successWithJSON(String k,Object v) {
        return success(JSONKit.convert(k,v));
    }

    public static ApiRes customFail(String customMsg) {
        return new ApiRes(ApiCodeEnum.CUSTOM_FAIL.getCode(), ApiCodeEnum.CUSTOM_FAIL.getMsg(), null,null);
    }
    public static ApiRes fail(ApiCodeEnum ace,String... params) {
        return new ApiRes(ace.getCode(),(params == null || params.length == 0) ? ace.getMsg():String.format(ace.getMsg(),params),null,null);
    }
    public static ApiRes successWithSign(Object data,String mchKey) {
        if(data == null){
            return success();
        }
        JSONObject o = JSONObject.from(data);
        String sign = SecKit.getSign(o, mchKey);
        return  new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), data, sign);
    }
}
