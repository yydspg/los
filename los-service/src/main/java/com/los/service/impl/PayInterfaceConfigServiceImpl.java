package com.los.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayInterfaceDefine;
import com.los.core.exception.BizException;
import com.los.service.MchAppService;
import com.los.service.MchInfoService;
import com.los.service.PayInterfaceDefineService;
import com.los.service.mapper.PayInterfaceConfigMapper;
import com.los.service.PayInterfaceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    /* 通过appId 获取商户所有支付配置 */
    /*
    这段代码是一个Java方法，功能是从数据库中根据appId获取符合条件的支付接口定义列表及其相关配置信息。整个逻辑主要包括以下几个步骤：

    1. **获取商户应用信息**：首先根据传入的appId从mchAppService服务获取MchApp实体对象，检查该对象是否存在且状态为有效（`CS.YES`），否则抛出业务异常。

    2. **获取商户信息**：接着根据MchApp实体中的mchNo属性获取MchInfo实体，同样验证其存在性和状态。

    3. **查询支付接口定义**：
       - 初始化一个LambdaQueryWrapper用于查询有效的PayInterfaceDefine记录（即state为`CS.YES`）。
       - 根据商户类型（普通商户或特约商户）添加额外的查询条件。
       - 如果商户类型为特约商户，还会进一步查询服务商的支付参数配置列表，并将符合条件的配置项存储到HashMap中，同时给每个配置项添加商户类型信息。

    4. **获取支付接口配置**：根据appId和商户应用类型查询支付接口的具体配置列表。

    5. **合并数据**：遍历查询到的支付接口定义列表，为每条记录添加商户类型信息，并查找匹配的支付接口配置。如果找到，则添加配置状态信息；若商户类型为特约商户且服务商没有相关支付参数配置，则添加标记表明未配置。

    6. **返回结果**：最终返回处理过的支付接口定义列表，其中包含了接口定义本身以及与其关联的配置信息。

    这里的类名、方法名及变量名都体现了这是一个与支付相关的业务逻辑，涉及到了对数据库表的操作，如`MchApp`、`MchInfo`、`PayInterfaceDefine`和`PayInterfaceConfig`等实体类，
    以及一些枚举值（如`CS`）来标识状态信息。整个方法主要实现了对不同类型的商户应用，筛选其可用的支付接口及其配置状态的功能。
     */
    /* TODO 理解此接口的实际用途 */
    @Override
    public List<PayInterfaceDefine> selectAllPayIfConfigListByAppId(String appId) {
        /* appId --> MchApp --> mchNo --> MchInfo */
        MchApp mchApp = mchAppService.getById(appId);
        if (mchApp == null || mchApp.getState() != CS.YES) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }
        MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
        if (mchInfo == null || mchInfo.getState() != CS.YES) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }
        /* 构造查询条件 */
        LambdaQueryWrapper<PayInterfaceDefine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayInterfaceDefine::getState,CS.YES);
        /* 缓存服务商支付参数 */
        HashMap<String, PayInterfaceConfig> isvPayConfigMap = new HashMap<>();

        /* 商户是否支持普通商户 */
        if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {
            wrapper.eq(PayInterfaceDefine::getIsMchMode,CS.YES);
        }
        /* 商户是否支持特约商户 */
        if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB) {
            wrapper.eq(PayInterfaceDefine::getIsIsvMode,CS.YES);
            /* 特约商户,需查询支付配置参数 */
            List<PayInterfaceConfig> isvConfigList = this.list(PayInterfaceConfig.gw()
                    .eq(PayInterfaceConfig::getInfoId, mchInfo.getIsvNo())
                    .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
                    .eq(PayInterfaceConfig::getState, CS.YES)
                    .ne(PayInterfaceConfig::getIfParams, "")
                    .isNotNull(PayInterfaceConfig::getIfParams));
            for (PayInterfaceConfig config : isvConfigList) {
                config.addExt("mchType", mchInfo.getType());
                isvPayConfigMap.put(config.getIfCode(), config);
            }
        }
        /* 查询支付接口定义 */
        List<PayInterfaceDefine> defineList = payInterfaceDefineService.list(wrapper);

        // 支付参数列表
        LambdaQueryWrapper<PayInterfaceConfig> queryConfigWrapper = PayInterfaceConfig.gw();
        queryConfigWrapper.eq(PayInterfaceConfig::getInfoId, appId);
        queryConfigWrapper.eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP);
        List<PayInterfaceConfig> configList = this.list(queryConfigWrapper);

        for (PayInterfaceDefine define : defineList) {
            /* 添加所属商户类型*/
            define.addExt("mchType", mchInfo.getType());
            for (PayInterfaceConfig config : configList) {
                if (define.getIfCode().equals(config.getIfCode())) {
                    /* 添加配置状态 */
                    define.addExt("ifConfigState", config.getState());
                }
            }

            if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB && isvPayConfigMap.get(define.getIfCode()) == null) {
                define.addExt("subMchIsvConfig", CS.NO); // 特约商户，服务商支付参数的配置状态，0表示未配置
            }
        }
        return defineList;
    }
}
