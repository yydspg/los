package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.model.dos.Setting;
import com.los.service.mapper.SettingMapper;
import com.los.service.SettingService;
import org.springframework.stereotype.Service;

/*
 * @author paul 2024/2/4
 */

@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

    @Override
    public Setting get(String key) {
        return this.getById(key);
    }

    @Override
    public boolean saveUpdate(Setting setting) {
        return this.saveOrUpdate(setting);
    }
}