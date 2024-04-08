package com.los.payment.cache.impl;


import com.los.payment.cache.Cache;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author paul 2024/4/1
 */

public class RedisCache implements Cache {

    @Resource private RedisTemplate<String,Object> redisTemplate;
    @Override
    public String getString(String k) {
        return (String) redisTemplate.opsForValue().get(k);
    }

    @Override
    public void setString(String k, String v) {
        redisTemplate.opsForValue().set(k,v);
    }

    @Override
    public void setString(String k, String v, Long exp) {
        redisTemplate.opsForValue().set(k,v,exp);
    }

    @Override
    public void setString(String k, String v, Long exp, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(k,v,exp,timeUnit);
    }

    @Override
    public void setString(Map<String, String> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    @Override
    public List<String> getString(Collection<String> keys) {
        List<String> res = new ArrayList<>();
        Objects.requireNonNull(redisTemplate.opsForValue().multiGet(keys)).forEach(t->res.add((String)t));
        return res;
    }

    @Override
    public void setHash(String k, String f, String v) {
        redisTemplate.opsForHash().put(k,f,v);
    }

    @Override
    public void setHash(String k, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(k,map);
    }

    @Override
    public String getHash(String k, String f) {
        return (String) redisTemplate.opsForHash().get(k,f);
    }

    @Override
    public Map<Object, Object> getHash(String k) {
        return redisTemplate.opsForHash().entries(k);
    }

    @Override
    public Collection<String> getHashFieldNames(String k) {
        List<String> res = new ArrayList<>();
        redisTemplate.opsForHash().keys(k).forEach(t->res.add((String) t));
        return res;
    }

    @Override
    public Collection<String> getHashFieldValues(String k) {
        List<String> res = new ArrayList<>();
        redisTemplate.opsForHash().values(k).forEach(t->res.add((String) t));
        return res;
    }

    @Override
    public void delHashFields(String k, Collection<String> fields) {
        redisTemplate.opsForHash().delete(k,fields);
    }
}
