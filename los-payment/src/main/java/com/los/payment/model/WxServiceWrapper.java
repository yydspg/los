package com.los.payment.model;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.los.core.constants.CS;
import com.los.core.model.params.wxpay.WxpayIsvParams;
import com.los.core.model.params.wxpay.WxpayNormalMchParams;
import com.los.core.utils.SpringBeansUtil;
import com.los.payment.utils.ChannelCertConfigKitBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.apache.commons.lang3.StringUtils;

/*
 * @author paul 2024/2/4
 */
@Data
@AllArgsConstructor
public class WxServiceWrapper {
    /** 缓存微信API版本 **/
    private String apiVersion;

    /** 缓存 wxPayService 对象 **/
    private WxPayService wxPayService;

    /** 缓存 wxJavaService 对象 **/
    private WxMpService wxMpService;

    public static WxServiceWrapper buildWxServiceWrapper(String mchId, String appId, String appSecret, String mchKey, String apiVersion, String apiV3Key,
                                                         String serialNo, String cert, String apiClientCert, String apiClientKey){

        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setMchId(mchId);
        wxPayConfig.setAppId(appId);
        wxPayConfig.setMchKey(mchKey);

        if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)) { // 微信API  V2
            wxPayConfig.setSignType(WxPayConstants.SignType.MD5);
        }

        ChannelCertConfigKitBean channelCertConfigKitBean = SpringBeansUtil.getBean(ChannelCertConfigKitBean.class);

        if(StringUtils.isNotBlank(apiV3Key)) {
            wxPayConfig.setApiV3Key(apiV3Key);
        }
        if(StringUtils.isNotBlank(serialNo)) {
            wxPayConfig.setCertSerialNo(serialNo);
        }
        if(StringUtils.isNotBlank(cert)){
            wxPayConfig.setKeyPath(channelCertConfigKitBean.getCertFilePath(cert));
        }
        if(StringUtils.isNotBlank(apiClientCert)){
            wxPayConfig.setPrivateCertPath(channelCertConfigKitBean.getCertFilePath(apiClientCert));
        }
        if(StringUtils.isNotBlank(apiClientKey)) {
            wxPayConfig.setPrivateKeyPath(channelCertConfigKitBean.getCertFilePath(apiClientKey));
        }

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(wxPayConfig); //微信配置信息

        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(appId);
        wxMpConfigStorage.setSecret(appSecret);

        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage); //微信配置信息

        return new WxServiceWrapper(apiVersion, wxPayService, wxMpService);
    }


    public static WxServiceWrapper buildWxServiceWrapper(WxpayIsvParams wxpayParams){
        //放置 wxJavaService
        return buildWxServiceWrapper(wxpayParams.getMchId(), wxpayParams.getAppId(),
                wxpayParams.getAppSecret(), wxpayParams.getKey(), wxpayParams.getApiVersion(), wxpayParams.getApiV3Key(),
                wxpayParams.getSerialNo(), wxpayParams.getCert(), wxpayParams.getApiClientCert(), wxpayParams.getApiClientKey());
    }

    public static WxServiceWrapper buildWxServiceWrapper(WxpayNormalMchParams wxpayParams){
        //放置 wxJavaService
        return buildWxServiceWrapper(wxpayParams.getMchId(), wxpayParams.getAppId(),
                wxpayParams.getAppSecret(), wxpayParams.getKey(), wxpayParams.getApiVersion(), wxpayParams.getApiV3Key(),
                wxpayParams.getSerialNo(), wxpayParams.getCert(), wxpayParams.getApiClientCert(), wxpayParams.getApiClientKey());
    }
}
