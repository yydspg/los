package com.los.core.service;

import com.los.core.model.DBApplicationConfig;

/**
 * @author paul 2024/1/31
 */

public interface ISysConfigService {
    /** 获取应用的配置参数 **/
    DBApplicationConfig getDBApplicationConfig();
}
