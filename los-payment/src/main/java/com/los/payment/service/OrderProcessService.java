package com.los.payment.service;

import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.RefundOrder;
import com.los.core.entity.TransferOrder;
import com.los.core.exception.BizException;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.service.RefundOrderService;
import com.los.service.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/3/14
 */
@Slf4j
@Service
public class OrderProcessService {

    @Autowired private RefundOrderService refundOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private TransferOrderService transferOrderService;


    /* 根据channel的ChannelRetMsg,同步本系统的退款数据,返回是否synchronized成功 */

    public boolean syncRefundChannel(ChannelRetMsg channelRetMsg, RefundOrder refundOrder) {

        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return false ;
        }

        String currentRefundOrderId = refundOrder.getRefundOrderId();

        /* 是否同步成功 */

        boolean isSyncRefundSuccess = switch (channelRetMsg.getChannelState()) {
            case CONFIRM_FAIL -> refundOrderService.updateIng2Fail(currentRefundOrderId,channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
            case CONFIRM_SUCCESS -> refundOrderService.updateIng2Success(currentRefundOrderId,refundOrder.getChannelOrderNo());
            default -> throw new BizException("ErrorRefundChannelState:" + channelRetMsg.getChannelState());
        };

        /* 异步通知商户 */
        if (isSyncRefundSuccess) {
            payMchNotifyService.refundOrderNotify(refundOrderService.getById(currentRefundOrderId));
        }
        return isSyncRefundSuccess;
    }


    // 同步上游转账渠道结果至本系统
    public boolean syncTransferChannel(ChannelRetMsg channelRetMsg, TransferOrder transferOrder) {

        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return false ;
        }
        String currentTransferOrderId = transferOrder.getTransferId();

        
        boolean isSyncTransferSuccess = switch (channelRetMsg.getChannelState()) {
                case CONFIRM_SUCCESS -> transferOrderService.updateIng2Success(currentTransferOrderId,channelRetMsg.getChannelOrderId());
                case CONFIRM_FAIL -> transferOrderService.updateIng2Fail(currentTransferOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
                default -> throw new BizException("ErrorTransferChannelState:" + channelRetMsg.getChannelState());
            };

        if(isSyncTransferSuccess) {
            payMchNotifyService.transferOrderNotify(transferOrderService.getById(currentTransferOrderId));
        }
        return isSyncTransferSuccess;
    }
}
