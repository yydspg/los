package com.los.payment.service;

import com.los.core.constants.CS;
import com.los.core.entity.IsvInfo;
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
import com.los.service.IsvInfoService;
import com.los.service.MchAppService;
import com.los.service.MchInfoService;
import com.los.service.PayInterfaceConfigService;
import com.los.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    商户/服务商 配置信息上下文服务
    此模块只专注于提供配置信息的上下文缓存,不管理是否缓存而直接进行数据库查询的功能
    上层的两种模式(本地缓存/数据库直接查询),交于QueryConfigContextService实现
 * @author paul 2024/2/8
 */
@Slf4j
@Service
public class ConfigContextService {

    //todo 思考此处为何要设置concurrentHashMap,在何时会存在并发安全问题
    /** <商户ID, 商户配置项>  **/
    private static final Map<String, MchInfoConfigContext> mchInfoConfigContextMap = new ConcurrentHashMap<>();

    /** <应用ID, 商户配置上下文>  **/
    private static final Map<String, MchAppConfigContext> mchAppConfigContextMap = new ConcurrentHashMap<>();

    /** <服务商号, 服务商配置上下文>  **/
    private static final Map<String, IsvConfigContext> isvConfigContextMap = new ConcurrentHashMap<>();

    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private IsvInfoService isvInfoService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;



    /* 获取 [商户配置信息]
     若存在数据 ,保证此接口查询有效

     */
    public MchInfoConfigContext getMchInfoConfigContext(String mchNo){

        MchInfoConfigContext mchInfoConfigContext = mchInfoConfigContextMap.get(mchNo);
        boolean isInit = false;
        /* 无此数据,初始化 */
        if(mchInfoConfigContext == null){
            initMchInfoConfigContext(mchNo);
            isInit = true;
        }

        return isInit ? mchInfoConfigContextMap.get(mchNo):mchInfoConfigContext;
    }

    /** 获取 [商户应用支付参数配置信息] **/
    public MchAppConfigContext getMchAppConfigContext(String mchNo, String appId){

        MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(appId);
        boolean isInit = false;
        /* 无此数据,初始化 */
        if(mchAppConfigContext == null){
            initMchAppConfigContext(mchNo, appId);
            isInit = true;
        }

        return isInit?mchAppConfigContextMap.get(appId):mchAppConfigContext;
    }

    /** 获取 [ISV支付参数配置信息] **/
    public IsvConfigContext getIsvConfigContext(String isvNo){

        IsvConfigContext isvConfigContext = isvConfigContextMap.get(isvNo);
        boolean isInit = false;
        /* 无此数据,初始化 */
        if(isvConfigContext == null){
            initIsvConfigContext(isvNo);
            isInit = true;
        }

        return isInit?isvConfigContextMap.get(isvNo):isvConfigContext;
    }
    public void initMchInfoConfigContext(String mchNo) {

        /*  若系统不采用本地缓存 */

        if(!isCache()){
            return ;
        }

        /* 获取商户主体信息 */

        MchInfo mchInfo = mchInfoService.getById(mchNo);

        /* 商户主体信息可能被删除 */

        if (mchInfo == null) {

            /* 保持数据一致 */

            /* 获取缓存 */
            MchInfoConfigContext mchInfoConfigContext = mchInfoConfigContextMap.get(mchNo);

            /* 删除商户应用 */
            if (mchInfoConfigContext != null) {
                mchInfoConfigContext.deleteAllMchApp();
            }
            /* 删除商户本体 */
            mchInfoConfigContextMap.remove(mchNo);
            return;
        }
        /* 重新加载商户信息 */

        MchInfoConfigContext mchInfoConfigContextRecord = new MchInfoConfigContext();
        mchInfoConfigContextRecord.setMchNo(mchNo);
        mchInfoConfigContextRecord.setMchInfo(mchInfo);
        mchInfoConfigContextRecord.setMchType(mchInfo.getType());
        mchAppService.list(MchApp.gw().eq(MchApp::getMchNo,mchNo)).forEach(t->{
            /* 更新商户内部 application */
            mchInfoConfigContextRecord.putMchApp(t);

            /* 同步更新商户所属 application */
            MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(t.getAppId());
            if(mchAppConfigContext != null) {
                mchAppConfigContext.setMchApp(t);
                mchAppConfigContext.setMchNo(mchNo);
                mchAppConfigContext.setMchType(mchInfo.getType());
                mchAppConfigContext.setMchInfo(mchInfo);
            }
        });
        /* 添加 mchInfoConfigContextMap */
        mchInfoConfigContextMap.put(mchNo,mchInfoConfigContextRecord);
    }
    /* 初始化 商户应用配置,配置中无数据走此接口 */
    public void initMchAppConfigContext(String mchNo,String appId) {
        /*
        在支付系统中，特别是像微信支付、支付宝等第三方支付平台，服务商模式和服务商下的普通商户模式是两种不同的合作类型和角色定位：

        ### 普通商户模式：
        1. **定义**：
           普通商户是指直接在支付平台上注册并通过审核的商户，拥有自己的商户账号（商户号）。他们可以直接与支付平台签订协议，接入支付接口，完成收款、退款等操作。

        2. **特点**：
           - 自主申请和管理：普通商户需要自行在支付平台提交相关资料申请，经过平台审核通过后获得商户资格。
           - 直接结算：普通商户产生的交易款项将直接打入商户自身的银行账户。
           - 开发集成：普通商户需要按照支付平台提供的API文档，自行开发对接支付功能到自己的应用或网站上。

        ### 服务商模式：
        1. **定义**：
           服务商是一种高级的合作形态，它具备一定的技术开发能力和市场拓展能力，能够为众多普通商户提供支付解决方案。服务商需向支付平台申请成为合作伙伴，并获得服务商户号。

        2. **特点**：
           - 帮助商户入驻：服务商可以协助其他商户入驻支付平台，帮助不具备开发能力的商户快速接入支付功能。
           - 间接结算：服务商并不能直接接收交易款项，而是通过自己的服务商户号为下辖的特约商户（也就是服务商模式下的普通商户）提供支付服务，交易款项会直接进入特约商户的银行账户。
           - 多商户管理：服务商可以管理和维护多个特约商户的账户信息，集中处理多个商户的交易数据和技术支持。
           - 扩展接口：服务商通常可以获得更丰富的API权限，以便为其商户提供更多的定制化服务，比如批量操作、分账、账单管理等。

        ### 差异总结：
        - 普通商户是直接与支付平台打交道的单一实体，服务商则扮演着桥梁的角色，为多个普通商户提供支付服务支持。
        - 普通商户直接处理自己的交易资金流，服务商则是帮助其下属商户处理资金流，本身并不直接参与资金结算环节。
        - 服务商模式适用于有实力进行技术支持和客户服务的大规模平台，比如电商平台、SaaS服务商等，他们能够为大量小型商户提供一站式支付解决方案。
                 */

        /*  若系统不采用本地缓存 */

        if(!isCache()){
            return ;
        }
        /* 检查商户主体是否存在 */
        MchInfoConfigContext mchInfoConfigContext = this.getMchInfoConfigContext(mchNo);
        /* 若商户信息不存在 */
        if(mchInfoConfigContext == null) {
            //TODO 逻辑存在问题
            return;
        }

        /* 查询商户应用信息 */

        MchApp dbMchApp = mchAppService.getById(appId);

        /* 信息不存在 */

        if (dbMchApp == null) {
            /* 清除缓存信息 */
            mchAppConfigContextMap.remove(appId);
            /* 保证其他接口不会继续查询此删除 application */
            mchInfoConfigContext.getAppMap().remove(appId);
            return ;
        }

        // 商户应用mchNo 与参数不匹配

        if(!dbMchApp.getMchNo().equals(mchNo)){
            return;
        }

        //更新商户信息主体中的商户应用
        mchInfoConfigContext.putMchApp(dbMchApp);

        //商户主体信息
        MchInfo mchInfo = mchInfoConfigContext.getMchInfo();
        MchAppConfigContext mchAppConfigContext = new MchAppConfigContext();

        // 设置商户信息
        mchAppConfigContext.setAppId(appId);
        mchAppConfigContext.setMchNo(mchInfo.getMchNo());
        mchAppConfigContext.setMchType(mchInfo.getType());
        mchAppConfigContext.setMchInfo(mchInfo);
        mchAppConfigContext.setMchApp(dbMchApp);

        // 查询商户的所有支持的参数配置
        List<PayInterfaceConfig> allConfigList = payInterfaceConfigService.list(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_MCH_APP)
                .eq(PayInterfaceConfig::getInfoId, appId)
        );

        /* 若为普通商户 */
        if (mchInfo.getType() == CS.MCH_TYPE_NORMAL) {
            for (PayInterfaceConfig payInterfaceConfig : allConfigList) {
                mchAppConfigContext.getNormalMchParamsMap().put(
                        payInterfaceConfig.getIfCode(),
                        NormalMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams())
                );
            }

            //放置alipay client

            AlipayNormalMchParams alipayParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.ALIPAY, AlipayNormalMchParams.class);
            if(alipayParams != null){
                mchAppConfigContext.setAlipayClientWrapper(AlipayClientWrapper.buildAlipayClientWrapper(alipayParams));
            }

            //放置 wxJavaService
            WxpayNormalMchParams wxpayParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.WXPAY, WxpayNormalMchParams.class);
            if(wxpayParams != null){
                mchAppConfigContext.setWxServiceWrapper(WxServiceWrapper.buildWxServiceWrapper(wxpayParams));
            }

            //放置 paypal client
            PppayNormalMchParams ppPayMchParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.PPPAY, PppayNormalMchParams.class);
            if (ppPayMchParams != null) {
                mchAppConfigContext.setPaypalWrapper(PaypalWrapper.buildPaypalWrapper(ppPayMchParams));
            }
            /* 服务商模式商户 */
        } else {
            for (PayInterfaceConfig payInterfaceConfig : allConfigList) {
                mchAppConfigContext.getIsvSubMchParamsMap().put(
                        payInterfaceConfig.getIfCode(),
                        IsvsubMchParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams())
                );
            }

            //放置 当前商户的 服务商信息
            mchAppConfigContext.setIsvConfigContext(getIsvConfigContext(mchInfo.getIsvNo()));
        }
        /* 执行缓存 */
        mchAppConfigContextMap.put(appId, mchAppConfigContext);
    }
    /* TODO 理解更新思路 */
    public void initIsvConfigContext(String isvNo) {

        /*  若系统不采用本地缓存 */

        if(!isCache()){
            return ;
        }
        //查询出所有商户的配置信息并更新
        List<String> mchNoList = new ArrayList<>();
        mchInfoService.list(MchInfo.gw().select(MchInfo::getMchNo).eq(MchInfo::getIsvNo, isvNo)).forEach(r -> mchNoList.add(r.getMchNo()));

        // 查询出所有 所属当前服务商的所有应用集合
        List<String> mchAppIdList = new ArrayList<>();
        if(!mchNoList.isEmpty()){
            mchAppService.list(MchApp.gw().select(MchApp::getAppId).in(MchApp::getMchNo, mchNoList)).forEach(r -> mchAppIdList.add(r.getAppId()));
        }
        IsvConfigContext isvConfigContext = new IsvConfigContext();
        IsvInfo isvInfo = isvInfoService.getById(isvNo);
        /* 若服务商信息不存在 */
        if(isvInfo == null){

            for (String appId : mchAppIdList) {
                //将更新已存在缓存的商户配置信息 （每个商户下存储的为同一个 服务商配置的对象指针）
                MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(appId);
                if(mchAppConfigContext != null){
                    mchAppConfigContext.setIsvConfigContext(null);
                }
            }
            isvConfigContextMap.remove(isvNo); // 服务商有商户不可删除， 此处不再更新商户下的配置信息
            return ;
        }
        // 设置商户信息
        isvConfigContext.setIsvNo(isvInfo.getIsvNo());
        isvConfigContext.setIsvInfo(isvInfo);

        // 查询商户的所有支持的参数配置
        List<PayInterfaceConfig> allConfigList = payInterfaceConfigService.list(PayInterfaceConfig.gw()
                .select(PayInterfaceConfig::getIfCode, PayInterfaceConfig::getIfParams)
                .eq(PayInterfaceConfig::getState, CS.YES)
                .eq(PayInterfaceConfig::getInfoType, CS.INFO_TYPE_ISV)
                .eq(PayInterfaceConfig::getInfoId, isvNo)
        );

        for (PayInterfaceConfig payInterfaceConfig : allConfigList) {
            isvConfigContext.getIsvParamsMap().put(
                    payInterfaceConfig.getIfCode(),
                    IsvParams.factory(payInterfaceConfig.getIfCode(), payInterfaceConfig.getIfParams())
            );
        }

        //放置alipay client
        AlipayIsvParams alipayParams = isvConfigContext.getIsvParamsByIfCode(CS.IF_CODE.ALIPAY, AlipayIsvParams.class);
        if(alipayParams != null){
            isvConfigContext.setAlipayClientWrapper(AlipayClientWrapper.buildAlipayClientWrapper(alipayParams));
        }

        //放置 wxJavaService
        WxpayIsvParams wxpayParams = isvConfigContext.getIsvParamsByIfCode(CS.IF_CODE.WXPAY, WxpayIsvParams.class);
        if(wxpayParams != null){
            isvConfigContext.setWxServiceWrapper(WxServiceWrapper.buildWxServiceWrapper(wxpayParams));
        }
        /* 缓存 isvConfigContext */
        isvConfigContextMap.put(isvNo, isvConfigContext);

        //查询出所有商户的配置信息并更新
        for (String appId : mchAppIdList) {
            //将更新已存在缓存的商户配置信息 （每个商户下存储的为同一个 服务商配置的对象指针）
            MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(appId);
            if(mchAppConfigContext != null){
                mchAppConfigContext.setIsvConfigContext(isvConfigContext);
            }
        }
    }
    private boolean isCache(){
        return SysConfigService.IS_USE_CACHE;
    }
}
