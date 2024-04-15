
package com.los.payment.channel.wxpay.paywayV3;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.los.core.entity.PayOrder;
import com.los.payment.channel.wxpay.WxpayPaymentService;
import com.los.payment.channel.wxpay.kits.WxpayKit;
import com.los.payment.channel.wxpay.kits.WxpayV3Util;
import com.los.payment.channel.wxpay.model.WxpayV3OrderRequestModel;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxAppOrderRS;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * 微信 app支付
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:08
 */
@Service("wxpayPaymentByAppV3Service") //Service Name需保持全局唯一性
public class WxApp extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();

        // 构造请求数据
        WxpayV3OrderRequestModel wxpayV3OrderRequestModel = buildV3OrderRequestModel(payOrder, mchAppConfigContext);

        // 构造函数响应数据
        WxAppOrderRS res = AbstractRS.build(WxAppOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        try {
            String payInfo = WxpayV3Util.commonReqWx(wxpayV3OrderRequestModel, wxPayService, mchAppConfigContext.isIsvSubMch(), WxPayConstants.TradeType.APP,
                    (JSONObject wxRes) -> {

                        // 普通商户，App支付与公众号支付  同一个应用只能配置其中一个
                        String resultAppId = wxpayV3OrderRequestModel.getNormalAppid();
                        String resultMchId = wxpayV3OrderRequestModel.getNormalMchid();

                        // 特约商户，App支付与公众号支付  同一个应用只能配置其中一个
                        if(mchAppConfigContext.isIsvSubMch()){
                            resultAppId = wxpayV3OrderRequestModel.getSubAppid();
                            resultMchId = wxpayV3OrderRequestModel.getSubMchid();
                        }

                        WxPayUnifiedOrderV3Result wxPayUnifiedOrderV3Result = new WxPayUnifiedOrderV3Result();
                        wxPayUnifiedOrderV3Result.setPrepayId(wxRes.getString("prepay_id"));

                        try {

                            FileInputStream fis = new FileInputStream(wxPayService.getConfig().getPrivateKeyPath());

                            WxPayUnifiedOrderV3Result.AppResult appResult =
                                    wxPayUnifiedOrderV3Result.getPayInfo(TradeTypeEnum.APP, resultAppId, resultMchId,
                                            PemUtils.loadPrivateKey(fis));

                            JSONObject jsonRes = (JSONObject) JSON.toJSON(appResult);
                            jsonRes.put("package", jsonRes.getString("packageValue"));
                            jsonRes.remove("packageValue");

                            try {
                                fis.close();
                            } catch (IOException e) {
                            }

                            return JSON.toJSONString(jsonRes);

                        } catch (FileNotFoundException e) {

                            return null;

                        }
                    }
            );

            res.setPayInfo(payInfo);

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            //明确失败
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
