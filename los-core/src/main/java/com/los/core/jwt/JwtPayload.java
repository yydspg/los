package com.los.core.jwt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.model.security.LosUserDetails;
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
public class JwtPayload {
    private Long sysUserId;       //登录用户ID
    private Long created;         //创建时间, 格式：13位时间戳
    private String cacheKey;      //redis保存的key

    protected JwtPayload(){}

    public JwtPayload(LosUserDetails losUserDetails){
        this.setSysUserId(losUserDetails.getSysUser().getSysUserId());
        this.setCreated(System.currentTimeMillis());
        this.setCacheKey(losUserDetails.getCacheKey());
    }


    /* toMap **/
    public Map<String, Object> toMap(){
        return JSONObject.from(this);
    }
}
