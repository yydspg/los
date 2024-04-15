
package com.los.payment.channel.ysfpay;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.payment.channel.AbstractRefundService;
import com.los.payment.channel.ysfpay.utils.YsfHttpUtil;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.refund.RefundOrderRQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class YsfpayRefundService extends AbstractRefundService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Autowired
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public String preCheck(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, RefundOrder refundOrder, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")退款】";
        try {
            reqParams.put("origOrderNo", payOrder.getPayOrderId()); // 原交易订单号
            reqParams.put("origTxnAmt", payOrder.getAmount()); // 原交易金额
            reqParams.put("orderNo", refundOrder.getRefundOrderId()); // 退款订单号
            reqParams.put("txnAmt ", refundOrder.getRefundAmount()); // 退款金额
            reqParams.put("orderType ", orderType); // 订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/refund", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 payorderId:{}, 返回结果:{}", payOrder.getPayOrderId(), resJSON);
            if(resJSON == null){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN); // 状态不明确
            }
            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            channelRetMsg.setChannelOrderId(refundOrder.getRefundOrderId());
            if("00".equals(respCode)){ // 交易成功
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                log.info("{} >>> 退款成功", logPrefix);
            }else{
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
                log.info("{} >>> 退款失败, {}", logPrefix, respMsg);
            }
        }catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(refundOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")退款查询】";
        try {
            reqParams.put("orderNo", refundOrder.getRefundOrderId()); // 退款订单号
            reqParams.put("origOrderNo", refundOrder.getPayOrderId()); // 原交易订单号

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/refundQuery", reqParams, logPrefix, mchAppConfigContext);
            log.info("查询订单 refundOrderId:{}, 返回结果:{}", refundOrder.getRefundOrderId(), resJSON);
            if(resJSON == null){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN); // 状态不明确
            }
            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            String origRespCode = resJSON.getString("origRespCode"); //原交易应答码
            String origRespMsg = resJSON.getString("origRespMsg"); //原交易应答信息
            channelRetMsg.setChannelOrderId(refundOrder.getRefundOrderId());
            if("00".equals(respCode)){ // 请求成功
                if("00".equals(origRespCode)){ //明确退款成功

                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    log.info("{} >>> 退款成功", logPrefix);

                } else if("01".equals(origRespCode)){ //明确退款失败

                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                    channelRetMsg.setChannelErrCode(respCode);
                    channelRetMsg.setChannelErrMsg(respMsg);
                    log.info("{} >>> 退款失败, {}", logPrefix, origRespMsg);

                } else if("02".equals(origRespCode)){ //退款中
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    log.info("{} >>> 退款中", logPrefix);
                }
            }else{
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN);
                channelRetMsg.setChannelErrCode(respCode);
                channelRetMsg.setChannelErrMsg(respMsg);
                log.info("{} >>> 请求失败, {}", logPrefix, respMsg);
            }
        }catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

}
