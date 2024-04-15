
package com.los.payment.channel.ysfpay.payway;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.payment.channel.ysfpay.YsfpayPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxBarOrderRQ;
import com.los.payment.rqrs.payorder.payway.WxBarOrderRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Service("ysfPaymentByWxBarService")
public class WxBar extends YsfpayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getAuthCode())){
            throw new BizException("用户支付条码[authCode]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【云闪付条码(wechat)支付】";

        WxBarOrderRQ bizRQ = (WxBarOrderRQ) rq;
        WxBarOrderRS res = AbstractRS.build(WxBarOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        JSONObject reqParams = new JSONObject();
        reqParams.put("authCode", bizRQ.getAuthCode().trim()); //付款码： 用户 APP 展示的付款条码或二维码

        // 云闪付 bar 统一参数赋值
        barParamsSet(reqParams, payOrder);

        //客户端IP
        reqParams.put("termInfo", "{\"ip\": \""+StringUtils.defaultIfEmpty(payOrder.getClientIp(), "127.0.0.1")+"\"}"); //终端信息

        // 发送请求
        JSONObject resJSON = packageParamAndReq("/gateway/api/pay/micropay", reqParams, logPrefix, mchAppConfigContext);
        //请求 & 响应成功， 判断业务逻辑
        String respCode = resJSON.getString("respCode"); //应答码
        String respMsg = resJSON.getString("respMsg"); //应答信息
        try {

            //00-交易成功， 02-用户支付中 , 12-交易重复， 需要发起查询处理    其他认为失败
            if("00".equals(respCode)){
                res.setPayData(resJSON.getString("payData"));
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }else if("02".equals(respCode) || "12".equals(respCode) || "99".equals(respCode)){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                channelRetMsg.setNeedQuery(true); // 开启轮询查单
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
