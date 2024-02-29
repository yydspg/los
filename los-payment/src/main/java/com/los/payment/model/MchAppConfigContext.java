package com.los.payment.model;

import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.model.params.IsvsubMchParams;
import com.los.core.model.params.NormalMchParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/*
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

    /* 商户支付配置信息缓存,  <接口代码, 支付参数>  */
    private Map<String, NormalMchParams> normalMchParamsMap = new HashMap<>();
    private Map<String, IsvsubMchParams> isvSubMchParamsMap = new HashMap<>();

    /* 服务商信息 */
    private IsvConfigContext isvConfigContext;

    /*缓存支付宝client 对象 */
    private AlipayClientWrapper alipayClientWrapper;

    /* 缓存 wxServiceWrapper 对象 */
    private WxServiceWrapper wxServiceWrapper;

    /* 缓存 Paypal 对象 **/
    private PaypalWrapper paypalWrapper;


    /* 获取普通商户配置信息 **/
    public NormalMchParams getNormalMchParamsByIfCode(String ifCode){
        return normalMchParamsMap.get(ifCode);
    }

    /* 获取isv配置信息 **/
    public <T> T getNormalMchParamsByIfCode(String ifCode, Class<? extends NormalMchParams> cls){
        return (T)normalMchParamsMap.get(ifCode);
    }

    /* 获取特约商户配置信息 **/
    public IsvsubMchParams getIsvSubMchParamsByIfCode(String ifCode){
        return isvSubMchParamsMap.get(ifCode);
    }

    /* 获取isv配置信息 **/
    public <T> T getIsvSubMchParamsByIfCode(String ifCode, Class<? extends IsvsubMchParams> cls){
        return (T)isvSubMchParamsMap.get(ifCode);
    }

    /* 是否为 服务商特约商户 **/
    public boolean isIsvSubMch(){
        return this.mchType == MchInfo.TYPE_ISVSUB;
    }

    public AlipayClientWrapper getAlipayClientWrapper(){
        return isIsvSubMch() ? isvConfigContext.getAlipayClientWrapper(): alipayClientWrapper;
    }

    public WxServiceWrapper getWxServiceWrapper(){
        return isIsvSubMch() ? isvConfigContext.getWxServiceWrapper(): wxServiceWrapper;
    }
}

