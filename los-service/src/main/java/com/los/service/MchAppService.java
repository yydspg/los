package com.los.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.MchApp;

/*
 * <p>
 * 商户应用表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface MchAppService extends IService<MchApp> {
    public void removeByAppId(String appId);
    public MchApp selectById(String appId);
    public IPage<MchApp> selectPage(IPage iPage, MchApp mchApp);
    public MchApp getOneByMch(String mchNo, String appId);
}
