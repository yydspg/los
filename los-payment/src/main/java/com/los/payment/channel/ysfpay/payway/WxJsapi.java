
package com.los.payment.channel.ysfpay.payway;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.params.wxpay.WxpayIsvParams;
import com.los.payment.channel.ysfpay.YsfpayPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxJsapiOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxJsapiOrderRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service("ysfpayPaymentByWxJsapiService")
public class WxJsapi extends YsfpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getOpenid())){
            throw new BizException("[openId]不可为空");
        }
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【云闪付(wechatJs)jsapi支付】";
        JSONObject reqParams = new JSONObject();
        WxJsapiOrderRS res = AbstractRS.build(WxJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        // 请求参数赋值
        jsapiParamsSet(reqParams, payOrder, getNotifyUrl(), getReturnUrl());

        WxJsapiOrderRQ bizRQ = (WxJsapiOrderRQ) rq;
        //云闪付扫一扫支付， 需要传入openId参数
        reqParams.put("userId", bizRQ.getOpenid()); // openId

        //客户端IP
        reqParams.put("customerIp", StringUtils.defaultIfEmpty(payOrder.getClientIp(), "127.0.0.1"));

        // 获取微信官方配置 的appId
        WxpayIsvParams wxpayIsvParams = (WxpayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), CS.IF_CODE.WXPAY);
        reqParams.put("subAppId", wxpayIsvParams.getAppId()); //用户ID

        // 发送请求并返回订单状态
        JSONObject resJSON = packageParamAndReq("/gateway/api/pay/unifiedorder", reqParams, logPrefix, mchAppConfigContext);
        //请求 & 响应成功， 判断业务逻辑
        String respCode = resJSON.getString("respCode"); //应答码
        String respMsg = resJSON.getString("respMsg"); //应答信息

        try {
            //00-交易成功， 02-用户支付中 , 12-交易重复， 需要发起查询处理    其他认为失败
            if("00".equals(respCode)){
                //付款信息
                res.setPayInfo(resJSON.getString("payData"));
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
