package com.los.payment.cache.config.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author paul 2024/4/1
 */

@Slf4j
@Component
@Data
public class SysRedisSerializer<T> implements RedisSerializer<T> {

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (null == value) {
            return new byte[0];
        }
        return JSON.toJSONBytes(value, JSONWriter.Feature.WriteClassName);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return (T) JSON.parseObject(bytes);
    }
}