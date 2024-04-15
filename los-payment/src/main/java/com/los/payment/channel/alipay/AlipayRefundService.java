
package com.los.payment.channel.alipay;

import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.core.utils.AmountKit;
import com.los.payment.channel.AbstractRefundService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.refund.RefundOrderRQ;
import org.springframework.stereotype.Service;

/*
* 退款接口： 支付宝官方
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/17 9:38
*/
@Service
public class AlipayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrder.getRefundOrderId());
        model.setRefundAmount(AmountKit.convertCent2Dollar(refundOrder.getRefundAmount().toString()));
        model.setRefundReason(refundOrder.getRefundReason());
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        AlipayTradeRefundResponse response = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功
        if(response.isSuccess()){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else{

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
            channelRetMsg.setChannelErrCode(response.getSubCode());
            channelRetMsg.setChannelErrMsg(response.getSubMsg());
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setOutRequestNo(refundOrder.getRefundOrderId());
        request.setBizModel(model);

        //统一放置 isv接口必传信息
        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);

        AlipayTradeFastpayRefundQueryResponse response = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        channelRetMsg.setChannelAttach(response.getBody());

        // 调用成功 & 金额相等  （传入不存在的outRequestNo支付宝仍然返回响应成功只是数据不存在， 调用isSuccess() 仍是成功, 此处需判断金额是否相等）
        Long channelRefundAmount = response.getRefundAmount() == null ? null : Long.parseLong(AmountKit.convertDollar2Cent(response.getRefundAmount()));
        if(response.isSuccess() && refundOrder.getRefundAmount().equals(channelRefundAmount)){
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        }else{

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING); //认为是处理中

        }

        return channelRetMsg;
    }


}
