package com.los.core.cache;

import com.alibaba.fastjson2.JSON;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author paul 2024/3/24
 */

public class RedisKit {
    private static final StringRedisTemplate srt = new StringRedisTemplate();


    public static String getString(@NotNull String key) {
        return key == null ? null : srt.opsForValue().get(key);
    }
    public static <T> T getObject(@NotNull String  key, Class<T> cls) {
        String val = getString(key);
        return JSON.parseObject(val, cls);
    }
    public static void setString(@NotNull String key,@NotNull String value){
        srt.opsForValue().set(key,value);
    }


    /** 普通缓存放入并设置时间, 默认单位：秒 */
    public static void setString(String key, String value, long time) {
        srt.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }
    /** 普通缓存放入并设置时间 */
    public static void set(String key, Object value, long time, TimeUnit timeUnit) {
        setString(key, JSON.toJSONString(value), time, timeUnit);
    }
    /** 普通缓存放入并设置时间, 默认单位：秒 */
    public static void set(String key, Object value, long time) {
        setString(key, JSON.toJSONString(value), time);
    }
    /** 普通缓存放入并设置时间 */
    public static void setString(String key, String value, long time, TimeUnit timeUnit) {
        srt.opsForValue().set(key, value, time, timeUnit);
    }
    /** 指定缓存失效时间 */
    public static void expire(String key, long time) {
        srt.expire(key, time, TimeUnit.SECONDS);
    }

    /** 指定缓存失效时间 */
    public static void expire(String key, long time, TimeUnit timeUnit) {
        srt.expire(key, time, timeUnit);
    }
    /** 删除缓存 **/
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                srt.delete(key[0]);
            } else {
                srt.delete(List.of(key));
            }
        }
    }
}
