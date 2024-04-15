
package com.los.payment.channel.wxpay.paywayV3;

import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.los.core.constants.CS;
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
import com.los.payment.rqrs.payorder.payway.WxNativeOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxNativeOrderRS;
import org.springframework.stereotype.Service;

/*
 * 微信 native支付
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:08
 */
@Service("wxpayPaymentByNativeV3Service") //Service Name需保持全局唯一性
public class WxNative extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {

        WxNativeOrderRQ bizRQ = (WxNativeOrderRQ) rq;

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        WxPayService wxPayService = wxServiceWrapper.getWxPayService();

        // 构造请求数据
        WxpayV3OrderRequestModel wxpayV3OrderRequestModel = buildV3OrderRequestModel(payOrder, mchAppConfigContext);

        // 构造函数响应数据
        WxNativeOrderRS res = AbstractRS.build(WxNativeOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        try {
            String payInfo = WxpayV3Util.commonReqWx(wxpayV3OrderRequestModel, wxPayService, mchAppConfigContext.isIsvSubMch(), WxPayConstants.TradeType.NATIVE, null);

            JSONObject resJSON = JSONObject.parseObject(payInfo);

            String codeUrl = resJSON.getString("code_url");
            if (CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())){ //二维码图片地址

                res.setCodeImgUrl(sysConfigService.getDBApplicationConfig().genScanImgUrl(codeUrl));
            }else{

                res.setCodeUrl(codeUrl);
            }

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
