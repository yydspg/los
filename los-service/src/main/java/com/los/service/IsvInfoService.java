package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.IsvInfo;

/*
 * <p>
 * 服务商信息表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface IsvInfoService extends IService<IsvInfo> {
    public void removeByIsvNo(String isvNo);
}
