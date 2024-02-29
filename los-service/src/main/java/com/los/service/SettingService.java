package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.model.dos.Setting;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/*
 * @author paul 2024/2/4
 */

@CacheConfig(cacheNames = "{setting}")
public interface SettingService extends IService<Setting> {

    /*
     * 通过key获取
     *
     * @param key
     * @return
     */
    @Cacheable(key = "#key")
    Setting get(String key);

    /*
     * 修改
     *
     * @param setting
     * @return
     */
    @CacheEvict(key = "#setting.id")
    boolean saveUpdate(Setting setting);
}