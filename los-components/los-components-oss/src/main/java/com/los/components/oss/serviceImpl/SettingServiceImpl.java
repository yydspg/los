package com.los.components.oss.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.components.oss.mapper.SettingMapper;
import com.los.components.oss.service.SettingService;
import com.los.core.model.dos.Setting;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/2/21
 */
@Service
public class SettingServiceImpl  extends ServiceImpl<SettingMapper, Setting> implements SettingService {
    @Override
    public Setting get(String ossName) {
        return null;
    }
}
