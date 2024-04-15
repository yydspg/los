
package com.los.payment.channel.alipay.payway;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alipay.api.domain.AlipayTradeCreateModel;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.utils.AmountKit;
import com.los.payment.channel.alipay.AlipayKit;
import com.los.payment.channel.alipay.AlipayPaymentService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliJsapiOrderRQ;
import com.los.payment.rqrs.payorder.payway.AliJsapiOrderRS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
 * 支付宝 jsapi支付
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:20
 */
@Service("alipayPaymentByJsapiService") //Service Name需保持全局唯一性
public class AliJsapi extends AlipayPaymentService {

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayOrder payOrder) {

        AliJsapiOrderRQ bizRQ = (AliJsapiOrderRQ) rq;
        if(StringUtils.isEmpty(bizRQ.getBuyerUserId())){
            throw new BizException("[buyerUserId]不可为空");
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception{

        AliJsapiOrderRQ bizRQ = (AliJsapiOrderRQ) rq;

        AlipayTradeCreateRequest req = new AlipayTradeCreateRequest();
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        model.setOutTradeNo(payOrder.getPayOrderId());
        model.setSubject(payOrder.getSubject()); //订单标题
        model.setBody(payOrder.getBody()); //订单描述信息
        model.setTotalAmount(AmountKit.convertCent2Dollar(payOrder.getAmount().toString()));  //支付金额
        model.setTimeExpire(DateUtil.format(payOrder.getExpiredTime(), DatePattern.NORM_DATETIME_FORMAT));  // 订单超时时间
        model.setBuyerId(bizRQ.getBuyerUserId());
        req.setNotifyUrl(getNotifyUrl()); // 设置异步通知地址
        req.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        //调起支付宝 （如果异常， 将直接跑出   ChannelException ）
        AlipayTradeCreateResponse alipayResp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 构造函数响应数据
        AliJsapiOrderRS res = AbstractRS.build(AliJsapiOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelAttach(alipayResp.getBody());

        // ↓↓↓↓↓↓ 调起接口成功后业务判断务必谨慎！！ 避免因代码编写bug，导致不能正确返回订单状态信息  ↓↓↓↓↓↓
        res.setAlipayTradeNo(alipayResp.getTradeNo());

        channelRetMsg.setChannelOrderId(alipayResp.getTradeNo());
        if(alipayResp.isSuccess()){ //业务处理成功
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);

        }else{
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(AlipayKit.appendErrCode(alipayResp.getCode(), alipayResp.getSubCode()));
            channelRetMsg.setChannelErrMsg(AlipayKit.appendErrMsg(alipayResp.getMsg(), alipayResp.getSubMsg()));
        }
        return res;
    }

}
