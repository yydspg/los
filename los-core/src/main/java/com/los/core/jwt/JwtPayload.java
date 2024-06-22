package com.los.core.jwt;

import com.alibaba.fastjson2.JSONObject;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/*
 * JWT payload 载体
 * 格式：
 *     {
 *         "sysUserId": "10001",
 *         "created": "1568250147846",
 *         "cacheKey": "KEYKEYKEYKEY",
 *     }
 * @author paul 2024/1/30
 */
@Setter
@Getter
public class JwtPayload {
    private Long sysUserId;       //登录用户ID
    private Long created;         //创建时间, 格式：13位时间戳
    private String cacheKey;      //redis保存的key

    protected JwtPayload(){}




    /* toMap **/
    public Map<String, Object> toMap(){
        return JSONObject.from(this);
    }
}
