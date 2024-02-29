package com.los.payment.service;

import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.model.params.IsvParams;
import com.los.core.model.params.IsvsubMchParams;
import com.los.core.model.params.NormalMchParams;
import com.los.core.model.params.alipay.AlipayIsvParams;
import com.los.core.model.params.alipay.AlipayNormalMchParams;
import com.los.core.model.params.pppay.PppayNormalMchParams;
import com.los.core.model.params.wxpay.WxpayIsvParams;
import com.los.core.model.params.wxpay.WxpayNormalMchParams;
import com.los.payment.model.*;
import com.los.service.MchAppService;
import com.los.service.MchInfoService;
import com.los.service.PayInterfaceConfigService;
import com.los.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
    配置信息查询服务 （兼容 缓存 和 直接查询方式）
 * @author paul 2024/2/8
 */
@Slf4j
@Service
public class ConfigContextQueryService {
    @Autowired private ConfigContextService configContextService;
    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;

    private boolean isCache(){
        return SysConfigService.IS_USE_CACHE;
    }

    /* 查询商户应用 */
    public MchApp queryMchApp(String mchNo, String mchAppId){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getMchApp();
        }

        return mchAppService.getOneByMch(mchNo, mchAppId);
    }
    /* 查询商户应用配置上下文 */
    public MchAppConfigContext queryMchInfoAndAppInfo(String mchNo, String mchAppId){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId);
        }

        MchInfo mchInfo = mchInfoService.getById(mchNo);
        MchApp mchApp = queryMchApp(mchNo, mchAppId);

        if(mchInfo == null || mchApp == null){
            return null;
        }

        MchAppConfigContext result = new MchAppConfigContext();
        result.setMchInfo(mchInfo);
        result.setMchNo(mchNo);
        result.setMchType(mchInfo.getType());

        result.setMchApp(mchApp);
        result.setAppId(mchAppId);

        return result;
    }

    /* 构造普通商户参数配置 */
    public NormalMchParams queryNormalMchParams(String mchNo, String mchAppId, String ifCode){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getNormalMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置

        PayInterfaceConfig payInterfaceConfig = this.buildPayInterfaceConfig(CS.YES, CS.INFO_TYPE_ISV, mchAppId, ifCode);

        if(payInterfaceConfig == null){
            return null;
        }

        return NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }

    /* 构造特约服务商户参数配置 */
    public IsvsubMchParams queryIsvSubMchParams(String mchNo, String mchAppId, String ifCode){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchNo, mchAppId).getIsvSubMchParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置;
        PayInterfaceConfig payInterfaceConfig = this.buildPayInterfaceConfig(CS.YES, CS.INFO_TYPE_MCH_APP, mchAppId, ifCode);
        if(payInterfaceConfig == null){
            return null;
        }

        return IsvsubMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());
    }


    /* 构造服务商户参数配置 */
    public IsvParams queryIsvParams(String isvNo, String ifCode){

        if(isCache()){
            IsvConfigContext isvConfigContext = configContextService.getIsvConfigContext(isvNo);
            return isvConfigContext == null ? null : isvConfigContext.getIsvParamsByIfCode(ifCode);
        }

        // 查询商户的所有支持的参数配置

        PayInterfaceConfig payInterfaceConfig = this.buildPayInterfaceConfig(CS.YES, CS.INFO_TYPE_ISV, isvNo, ifCode);

        if(payInterfaceConfig == null){
            return null;
        }

        return IsvParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams());

    }
    /* 对于 buildAlipayClientWrapper() 的抽象 */
    public AlipayClientWrapper getAlipayClientWrapper(MchAppConfigContext mchAppConfigContext){
        /* 是否缓存 */
        if(isCache()){
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getAlipayClientWrapper();
        }
        /* 特约服务商 */
        if(mchAppConfigContext.isIsvSubMch()){

            AlipayIsvParams alipayParams = (AlipayIsvParams)queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.ALIPAY);
            return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
        }else{
        /* 普通商户 */
            AlipayNormalMchParams alipayParams = (AlipayNormalMchParams)queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.ALIPAY);
            return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);
        }

    }
    public WxServiceWrapper getWxServiceWrapper(MchAppConfigContext mchAppConfigContext){

        if(isCache()){
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getWxServiceWrapper();
        }

        if(mchAppConfigContext.isIsvSubMch()){

            WxpayIsvParams wxParams = (WxpayIsvParams)queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.WXPAY);
            return WxServiceWrapper.buildWxServiceWrapper(wxParams);
        }else{

            WxpayNormalMchParams wxParams = (WxpayNormalMchParams)queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);
            return WxServiceWrapper.buildWxServiceWrapper(wxParams);
        }

    }

    public PaypalWrapper getPaypalWrapper(MchAppConfigContext mchAppConfigContext){
        if(isCache()){
            return
                    configContextService.getMchAppConfigContext(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId()).getPaypalWrapper();
        }
        PppayNormalMchParams ppPayNormalMchParams = (PppayNormalMchParams) queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PPPAY);;
        return PaypalWrapper.buildPaypalWrapper(ppPayNormalMchParams);

    }
    private PayInterfaceConfig buildPayInterfaceConfig(byte state,byte infoType,String infoId,String ifCode){
        return payInterfaceConfigService.getOne(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode,PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState,state)
                .eq(PayInterfaceConfig::getInfoType,infoType)
                .eq(PayInterfaceConfig::getIfCode,ifCode)
                .eq(PayInterfaceConfig::getInfoId,infoId));
    }
}
