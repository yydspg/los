package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.CS;
import com.los.core.entity.IsvInfo;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.exception.BizException;
import com.los.service.MchInfoService;
import com.los.service.PayInterfaceConfigService;
import com.los.service.mapper.IsvInfoMapper;
import com.los.service.IsvInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* <p>
    * 服务商信息表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class IsvInfoServiceImpl extends ServiceImpl<IsvInfoMapper, IsvInfo> implements IsvInfoService {
    @Autowired private MchInfoService mchInfoService;

    @Autowired private IsvInfoService isvInfoService;

    @Autowired private PayInterfaceConfigService payInterfaceConfigService;


    @Override
    public void removeByIsvNo(String isvNo) {
        /* 检查此服务商是否存在 */
        IsvInfo isvInfo = this.getById(isvNo);
        if (isvInfo == null) {
            throw new BizException("服务商不存在");
        }
        /* 检查此服务商下是否存在商户 */
        long count = mchInfoService.count(MchInfo.gw()
                .eq(MchInfo::getIsvNo, isvNo)
                .eq(MchInfo::getType, CS.MCH_TYPE_ISVSUB));
        if(count > 0) {
            throw new BizException("服务商存在商户");
        }
        /* 删除服务商配置信息 */
        payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoId,isvNo)
                .eq(PayInterfaceConfig::getInfoType,CS.INFO_TYPE_ISV));
        /* 删除服务商 */
        boolean res = isvInfoService.removeById(isvNo);
    }
}
