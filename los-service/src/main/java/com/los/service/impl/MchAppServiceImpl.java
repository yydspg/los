package com.los.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchPayPassage;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.utils.StringKit;
import com.los.service.*;
import com.los.service.mapper.MchAppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
* <p>
    * 商户应用表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class MchAppServiceImpl extends ServiceImpl<MchAppMapper, MchApp> implements MchAppService {

    @Autowired private PayOrderService payOrderService;
    @Autowired private MchPayPassageService mchPayPassageService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByAppId(String appId) {
        /* 检查当前应用是否存在交易数据 */
        long count = payOrderService.count(PayOrder.gw()
                .eq(PayOrder::getAppId, appId));
        if(count > 0) {
            throw new BizException("应用存在交易数据");
        }
        /* 删除与应用关联的支付通道 */
        mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getAppId,appId));
        /* 删除应用配置的支付参数 */
        payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                .eq(PayInterfaceConfig::getInfoId, appId)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
        );
        /* 删除当前应用 */
        if (!this.removeById(appId)) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
    }

    @Override
    public MchApp selectById(String appId) {
        MchApp mchApp = this.getById(appId);
        if (mchApp == null) {
            return null;
        }
        mchApp.setAppSecret(StringKit.str2Star(mchApp.getAppSecret(), 6, 6, 6));

        return mchApp;
    }

    @Override
    public IPage<MchApp> selectPage(IPage iPage, MchApp mchApp) {
        LambdaQueryWrapper<MchApp> wrapper = MchApp.gw();
        if (StringKit.isNotBlank(mchApp.getMchNo())) {
            wrapper.eq(MchApp::getMchNo, mchApp.getMchNo());
        }
        if (StringKit.isNotEmpty(mchApp.getAppId())) {
            wrapper.eq(MchApp::getAppId, mchApp.getAppId());
        }
        if (StringKit.isNotEmpty(mchApp.getAppName())) {
            wrapper.eq(MchApp::getAppName, mchApp.getAppName());
        }
        if (mchApp.getState() != null) {
            wrapper.eq(MchApp::getState, mchApp.getState());
        }
        wrapper.orderByDesc(MchApp::getCreatedAt);

        IPage<MchApp> pages = this.page(iPage, wrapper);

        pages.getRecords().forEach(item -> item.setAppSecret(StringKit.str2Star(item.getAppSecret(), 6, 6, 6)));

        return pages;
    }
    //TODO 是否类型检查
    @Override
    public MchApp getOneByMch(String mchNo, String appId) {
        return this.getOne(MchApp.gw().eq(MchApp::getMchNo, mchNo).eq(MchApp::getAppId, appId));
    }
}
