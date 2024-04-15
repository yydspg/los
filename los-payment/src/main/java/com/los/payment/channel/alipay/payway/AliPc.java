
package com.los.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.utils.AmountKit;
import com.los.payment.channel.alipay.AlipayKit;
import com.los.payment.channel.alipay.AlipayPaymentService;
import com.los.payment.exception.ChannelException;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliPcOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliPcOrderRS;
import org.springframework.stereotype.Service;

/*
* 支付宝 PC支付
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:21
*/
@Service("alipayPaymentByAliPcService") //Service Name需保持全局唯一性
public class AliPc extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext){

        AliPcOrderRQ bizRQ = (AliPcOrderRQ) rq;

        AlipayTradePagePayRequest req = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountKit.convertCent2Dollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setQrPayMode("2"); //订单码-跳转模式
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setReturnUrl(getReturnUrl()); // 同步跳转地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        // 构造函数响应数据
        AliPcOrderRS res = AbstractRS.build(AliPcOrderRS.class);

        try {
            if(CS.PAY_DATA_TYPE.FORM.equals(bizRQ.getPayDataType())){
                res.setFormContent(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req).getBody());
            }else{
                res.setPayUrl(configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).getAlipayClient().pageExecute(req, "GET").getBody());
            }
        }catch (AlipayApiException e) {
            throw ChannelException.sysError(e.getMessage());
        }

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        return res;
    }

}
