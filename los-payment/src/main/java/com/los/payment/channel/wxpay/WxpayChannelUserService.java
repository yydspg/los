
package com.los.payment.channel.wxpay;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.exception.BizException;
import com.los.core.model.params.wxpay.WxpayIsvParams;
import com.los.core.model.params.wxpay.WxpayNormalMchParams;
import com.los.payment.channel.IChannelUserService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;

import com.los.payment.service.ConfigContextQueryService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 微信支付 获取微信openID实现类
*/
@Service
@Slf4j
public class WxpayChannelUserService implements IChannelUserService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    /** 默认官方跳转地址 **/
    private static final String DEFAULT_OAUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public String buildUserRedirectUrl(String callbackUrlEncode, MchAppConfigContext mchAppConfigContext) {

        String appId = null;
        String oauth2Url = "";
        if(mchAppConfigContext.isIsvSubMch()){
            WxpayIsvParams wxpayIsvParams = (WxpayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.WXPAY);
            if(wxpayIsvParams == null) {
                throw new BizException("服务商微信支付接口没有配置！");
            }
            appId = wxpayIsvParams.getAppId();
            oauth2Url = wxpayIsvParams.getOauth2Url();
        }else{
            //获取商户配置信息
            WxpayNormalMchParams normalMchParams = (WxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.WXPAY);
            if(normalMchParams == null) {
                throw new BizException("商户微信支付接口没有配置！");
            }
            appId = normalMchParams.getAppId();
            oauth2Url = normalMchParams.getOauth2Url();
        }

        if(StringUtils.isBlank(oauth2Url)){
            oauth2Url = DEFAULT_OAUTH_URL;
        }
        String wxUserRedirectUrl = String.format(oauth2Url + "?appid=%s&scope=snsapi_base&state=&redirect_uri=%s&response_type=code#wechat_redirect", appId, callbackUrlEncode);
        log.info("wxUserRedirectUrl={}", wxUserRedirectUrl);
        return wxUserRedirectUrl;
    }

    @Override
    public String getChannelUserId(JSONObject reqParams, MchAppConfigContext mchAppConfigContext) {
        String code = reqParams.getString("code");
        try {

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
            return wxServiceWrapper.getWxMpService().getOAuth2Service().getAccessToken(code).getOpenId();
        } catch (WxErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

}
