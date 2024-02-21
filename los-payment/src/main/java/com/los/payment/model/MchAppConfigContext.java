package com.los.payment.model;

import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.model.params.IsvsubMchParams;
import com.los.core.model.params.NormalMchParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author paul 2024/2/8
 */
@Data
public class MchAppConfigContext {
    /* 商户信息缓存 */
    private String mchNo;
    //TODO 哪个关联字段
    private String appId;

    private Byte mchType;
    /* 商户基本信息 */
    private MchInfo mchInfo;
    private MchApp mchApp;
    /** 商户支付配置信息缓存,  <接口代码, 支付参数>  */
    private Map<String, NormalMchParams> normalMchParamsMap = new HashMap<>();
    private Map<String, IsvsubMchParams> isvsubMchParamsMap = new HashMap<>();

    /* 服务商信息 */
    private IsvConfigContext isvConfigContext;

    /** 缓存支付宝client 对象 **/
    private AlipayClientWrapper alipayClientWrapper;

    /** 缓存 wxServiceWrapper 对象 **/
    private WxServiceWrapper wxServiceWrapper;

    /** 获取普通商户配置信息 **/
    public NormalMchParams getNormalMchParamsByIfCode(String ifCode){
        return normalMchParamsMap.get(ifCode);
    }

    /** 获取isv配置信息 **/
    public <T> T getNormalMchParamsByIfCode(String ifCode, Class<? extends NormalMchParams> cls){
        return (T)normalMchParamsMap.get(ifCode);
    }

    /** 获取特约商户配置信息 **/
    public IsvsubMchParams getIsvsubMchParamsByIfCode(String ifCode){
        return isvsubMchParamsMap.get(ifCode);
    }

    /** 获取isv配置信息 **/
    public <T> T getIsvsubMchParamsByIfCode(String ifCode, Class<? extends IsvsubMchParams> cls){
        return (T)isvsubMchParamsMap.get(ifCode);
    }

    /** 是否为 服务商特约商户 **/
    public boolean isIsvsubMch(){
        return this.mchType == MchInfo.TYPE_ISVSUB;
    }

    public AlipayClientWrapper getAlipayClientWrapper(){
        return isIsvsubMch() ? isvConfigContext.getAlipayClientWrapper(): alipayClientWrapper;
    }

    public WxServiceWrapper getWxServiceWrapper(){
        return isIsvsubMch() ? isvConfigContext.getWxServiceWrapper(): wxServiceWrapper;
    }
}

