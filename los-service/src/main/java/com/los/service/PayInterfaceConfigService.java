package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayInterfaceDefine;

import java.util.List;

/*
 * <p>
 * 支付接口配置参数表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface PayInterfaceConfigService extends IService<PayInterfaceConfig> {
    /* 根据 账户类型、账户号、接口类型 获取支付参数配置 */
    public PayInterfaceConfig getByInfoIdAndIfCode(Byte infoType, String infoId, String ifCode);
    /* 根据 账户类型、账户号 获取支付参数配置列表 */
    public List<PayInterfaceDefine> selectAllPayIfConfigListByIsvNo(Byte infoType, String infoId);
    /* 查询商户app使用已正确配置了通道信息 */
    public boolean mchAppHasAvailableIfCode(String appId, String ifCode);
    public List<PayInterfaceDefine> selectAllPayIfConfigListByAppId(String appId);
}
