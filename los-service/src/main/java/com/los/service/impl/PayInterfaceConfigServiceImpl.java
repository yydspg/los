package com.los.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.CS;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayInterfaceDefine;
import com.los.service.MchAppService;
import com.los.service.MchInfoService;
import com.los.service.PayInterfaceDefineService;
import com.los.service.mapper.PayInterfaceConfigMapper;
import com.los.service.PayInterfaceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* <p>
    * 支付接口配置参数表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class PayInterfaceConfigServiceImpl extends ServiceImpl<PayInterfaceConfigMapper, PayInterfaceConfig> implements PayInterfaceConfigService {
    @Autowired
    private PayInterfaceDefineService payInterfaceDefineService;

    @Autowired
    private MchInfoService mchInfoService;

    @Autowired
    private MchAppService mchAppService;
    @Override
    public PayInterfaceConfig getByInfoIdAndIfCode(Byte infoType, String infoId, String ifCode) {
        return this.getOne(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoType, infoType)
                .eq(PayInterfaceConfig::getInfoId, infoId)
                .eq(PayInterfaceConfig::getIfCode, ifCode)
        );
    }

    @Override
    public List<PayInterfaceDefine> selectAllPayIfConfigListByIsvNo(Byte infoType, String infoId) {
        // 支付定义列表
        LambdaQueryWrapper<PayInterfaceDefine> queryWrapper = PayInterfaceDefine.gw();
        queryWrapper.eq(PayInterfaceDefine::getState, CS.YES);
        queryWrapper.eq(PayInterfaceDefine::getIsIsvMode, CS.YES); // 支持服务商模式

        List<PayInterfaceDefine> defineList = payInterfaceDefineService.list(queryWrapper);

        // 支付参数列表
        LambdaQueryWrapper<PayInterfaceConfig> wrapper = PayInterfaceConfig.gw();
        wrapper.eq(PayInterfaceConfig::getInfoId, infoId);
        wrapper.eq(PayInterfaceConfig::getInfoType, infoType);
        List<PayInterfaceConfig> configList = this.list(wrapper);

        for (PayInterfaceDefine define : defineList) {
            for (PayInterfaceConfig config : configList) {
                if (define.getIfCode().equals(config.getIfCode())) {
                    /* 对支付接口的定义添加配置状态 */
                    define.addExt("ifConfigState", config.getState());
                }
            }
        }
        return defineList;
    }

    @Override
    public boolean mchAppHasAvailableIfCode(String appId, String ifCode) {
        /* 查询商户是否正确设置 支付配置 */
        return this.count(
                PayInterfaceConfig.gw()
                        .eq(PayInterfaceConfig::getIfCode, ifCode)
                        .eq(PayInterfaceConfig::getState, CS.PUB_USABLE)
                        .eq(PayInterfaceConfig::getInfoId, appId)
                        .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
        ) > 0;
    }

    @Override
    public List<PayInterfaceDefine> selectAllPayIfConfigListByAppId(String appId) {
        return null;
    }
}
