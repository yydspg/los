package com.los.payment.service;

import com.los.core.entity.RefundOrder;
import com.los.core.exception.BizException;
import com.los.core.utils.StringKit;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.service.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
    退款处理通用逻辑
 * @author paul 2024/2/28
 */
@Slf4j
@Service
public class RefundOrderProcessService {

    @Autowired private RefundOrderService refundOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;

    /* 根据channel的ChannelRetMsg,同步本系统的退款数据,返回是否synchronized成功 */

    public boolean handleRefundOrder4Channel(ChannelRetMsg channelRetMsg, RefundOrder refundOrder) {
        /* 默认同步数据成功 */
        String currentRefundOrderId = refundOrder.getRefundOrderId();
        boolean isSynchronizeRefundSuccess = switch (channelRetMsg.getChannelState()) {
            case CONFIRM_FAIL -> refundOrderService.updateIng2Fail(currentRefundOrderId,refundOrder.getChannelOrderNo(),refundOrder.getErrCode(),refundOrder.getErrMsg());
            case CONFIRM_SUCCESS -> refundOrderService.updateIng2Success(currentRefundOrderId,refundOrder.getChannelOrderNo());
            default -> throw new BizException("Unexpected value: " + channelRetMsg.getChannelState());
        };
        /* 异步通知商户 */
        if (isSynchronizeRefundSuccess) {
            payMchNotifyService.refundOrderNotify(refundOrderService.getById(currentRefundOrderId));
        }
        return isSynchronizeRefundSuccess;
    }
}
