
package com.los.payment.channel.ysfpay.payway;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.entity.PayOrder;
import com.los.payment.channel.ysfpay.YsfpayPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.YsfJsapiOrderRQ;
import com.los.payment.rqrs.payorder.payway.YsfJsapiOrderRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service("ysfpayPaymentByJsapiService")
public class YsfJsapi extends YsfpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【云闪付(unionpay)jsapi支付】";
        JSONObject reqParams = new JSONObject();
        YsfJsapiOrderRS res = AbstractRS.build(YsfJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        YsfJsapiOrderRQ bizRQ = (YsfJsapiOrderRQ) rq;

        // 请求参数赋值
        jsapiParamsSet(reqParams, payOrder, getNotifyUrl(), getReturnUrl());
        //云闪付扫一扫支付， 需要传入termInfo参数
        reqParams.put("termInfo", "{\"ip\": \""+StringUtils.defaultIfEmpty(payOrder.getClientIp(), "127.0.0.1")+"\"}");

        //客户端IP
        reqParams.put("customerIp", StringUtils.defaultIfEmpty(payOrder.getClientIp(), "127.0.0.1"));
        // 发送请求并返回订单状态
        JSONObject resJSON = packageParamAndReq("/gateway/api/pay/unifiedorder", reqParams, logPrefix, mchAppConfigContext);
        //请求 & 响应成功， 判断业务逻辑
        String respCode = resJSON.getString("respCode"); //应答码
        String respMsg = resJSON.getString("respMsg"); //应答信息

        try {
            //00-交易成功， 02-用户支付中 , 12-交易重复， 需要发起查询处理    其他认为失败
            if("00".equals(respCode)){
                //付款信息
                res.setPayData(resJSON.getString("payData"));
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
            }else{
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
            }
        }catch (Exception e) {
            channelRetMsg.setChannelErrCode(respCode);
            channelRetMsg.setChannelErrMsg(respMsg);
        }
        return res;
    }

}
