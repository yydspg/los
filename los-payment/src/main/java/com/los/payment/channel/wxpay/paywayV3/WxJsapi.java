
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
import com.los.payment.rqrs.payorder.payway.WxJsapiOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxJsapiOrderRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * 微信 jsapi支付
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:08
 */
@Service("wxpayPaymentByJsapiV3Service") //Service Name需保持全局唯一性
public class WxJsapi extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        // 使用的是V2接口的预先校验
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();

        // 构造请求数据
        WxpayV3OrderRequestModel wxpayV3OrderRequestModel = buildV3OrderRequestModel(payOrder, mchAppConfigContext);

        // 特约商户
        if(mchAppConfigContext.isIsvSubMch()) {

            // 子商户subAppId不为空
            if(StringUtils.isNotBlank(wxpayV3OrderRequestModel.getSubAppid())){
                wxpayV3OrderRequestModel.setPayer(new WxpayV3OrderRequestModel.Payer().setSubOpenid(bizRQ.getOpenid()));

            }else{ // 使用的服务商配置的公众号appId获取
                wxpayV3OrderRequestModel.setPayer(new WxpayV3OrderRequestModel.Payer().setSpOpenid(bizRQ.getOpenid()));
            }

        }else{ //普通商户 设置openId即可。
            wxpayV3OrderRequestModel.setPayer(new WxpayV3OrderRequestModel.Payer().setNormalOpenId(bizRQ.getOpenid()));

        }

        // 构造函数响应数据
        WxJsapiOrderRS res = AbstractRS.build(WxJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        try {
            // 调起接口  modify 20220425 by terrfly : 改为统一调用的方式， 将返回数据包放置在业务层进行处理 。
            String payInfo = WxpayV3Util.commonReqWx(wxpayV3OrderRequestModel, wxPayService, mchAppConfigContext.isIsvSubMch(), WxPayConstants.TradeType.JSAPI,
                    (JSONObject wxRes) -> {

                        // 普通商户
                        String resultAppId = wxpayV3OrderRequestModel.getNormalAppid();

                        // 特约商户
                        if(mchAppConfigContext.isIsvSubMch()){
                            resultAppId = StringUtils.defaultIfEmpty(wxpayV3OrderRequestModel.getSubAppid(), wxpayV3OrderRequestModel.getSpAppid());
                        }

                        WxPayUnifiedOrderV3Result wxPayUnifiedOrderV3Result = new WxPayUnifiedOrderV3Result();
                        wxPayUnifiedOrderV3Result.setPrepayId(wxRes.getString("prepay_id"));
                        try {

                            FileInputStream fis = new FileInputStream(wxPayService.getConfig().getPrivateKeyPath());

                            WxPayUnifiedOrderV3Result.JsapiResult jsapiResult =
                                    wxPayUnifiedOrderV3Result.getPayInfo(TradeTypeEnum.JSAPI, resultAppId, null,
                                            PemUtils.loadPrivateKey(fis));

                            JSONObject jsonRes = (JSONObject)JSON.toJSON(jsapiResult);
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
