
package com.los.payment.channel.wxpay.payway;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.payment.channel.wxpay.WxpayPaymentService;
import com.los.payment.channel.wxpay.kits.WxpayKit;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.model.WxServiceWrapper;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxLiteOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxLiteOrderRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service("wxpayPaymentByLiteService") //Service Name需保持全局唯一性
@Slf4j
public class WxLite extends WxpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        WxLiteOrderRQ bizRQ = (WxLiteOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getOpenid())){
            throw new BizException("[openid]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception{

        WxLiteOrderRQ bizRQ = (WxLiteOrderRQ) rq;

        WxPayUnifiedOrderRequest req = buildUnifiedOrderRequest(payOrder, mchAppConfigContext);
        req.setTradeType(WxPayConstants.TradeType.JSAPI);
        if(mchAppConfigContext.isIsvSubMch() && StringUtils.isNotBlank(req.getSubAppId())){ // 特约商户 && 传了子商户appId
            req.setSubOpenid(bizRQ.getOpenid()); // 用户在子商户appid下的唯一标识
        }else {
            req.setOpenid(bizRQ.getOpenid());
        }

        // 构造函数响应数据
        WxLiteOrderRS res = AbstractRS.build(WxLiteOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 调起上游接口：
        // 1. 如果抛异常，则订单状态为： 生成状态，此时没有查单处理操作。 订单将超时关闭
        // 2. 接口调用成功， 后续异常需进行捕捉， 如果 逻辑代码出现异常则需要走完正常流程，此时订单状态为： 支付中， 需要查单处理。
        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);
        WxPayService wxPayService = wxServiceWrapper.getWxPayService();
        try {
            WxPayMpOrderResult payResult = wxPayService.createOrder(req);
            JSONObject resJSON = (JSONObject) JSON.toJSON(payResult);
            resJSON.put("package", payResult.getPackageValue());

            res.setPayInfo(resJSON.toJSONString());

            // 支付中
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        } catch (WxPayException e) {
            log.error("WxPayException:", e);
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            WxpayKit.commonSetErrInfo(channelRetMsg, e);
        }

        return res;
    }

}
