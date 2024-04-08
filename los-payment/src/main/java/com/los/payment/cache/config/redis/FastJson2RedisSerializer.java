package com.los.payment.cache.config.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
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
public class FastJson2RedisSerializer<T> implements RedisSerializer<T> {
    @Override
    public byte[] serialize(T value) throws SerializationException {
        return new byte[0];
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}

    // TODO 2024/4/5 : 报错了!!!! Parameter 0 of constructor in com.los.payment.cache.config.redis.FastJson2RedisSerializer required a bean of type 'java.lang.Class' that could not be found.
//    // TODO 2024/4/1 : 详细配置
//    private static final Filter AUTO_TYPE_FILTER = JSONReader.autoTypeFilter(
//            "com.***.***"
//    );
//    private final Class<T> clazz;
//    public FastJson2RedisSerializer(Class<T> clazz){
//        super();
//        this.clazz = clazz;
//    }
//    @Override
//    public byte[] serialize(T value) throws SerializationException {
//        if(Objects.isNull(value)){
//            return new byte[0];
//        }
//        try {
//            return JSON.toJSONBytes(value, JSONWriter.Feature.WriteClassName);
//        } catch (Exception e) {
//            log.error("[{}]serializeFail",e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public T deserialize(byte[] bytes) throws SerializationException {
//        if (bytes == null || bytes.length == 0) {
//            return null;
//        }
//        try {
//            return JSON.parseObject(bytes,clazz,AUTO_TYPE_FILTER);
//        } catch (Exception e) {
//            log.error("[{}]deserializeFail",e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
