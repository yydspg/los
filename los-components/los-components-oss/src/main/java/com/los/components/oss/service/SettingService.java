package com.los.components.oss.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.model.dos.Setting;


/**
 * @author paul 2024/2/21
 */

public interface SettingService extends IService<Setting> {
    Setting get(String ossName);
}
