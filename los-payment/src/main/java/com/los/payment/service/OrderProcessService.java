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


    // 此接口 目的是同步 渠道返回的信息 ,与本系统保持一致,并且参数 needInitToIng 表示不同使用场景
    //1. 无需将 refundOrder.state 从 Init --> Ing; 2. 需要从 Init --> Ing

    public boolean  syncRefundChannel(ChannelRetMsg channelRetMsg, RefundOrder refundOrder,boolean needInitToIng) {

        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            log.error("syncRefundParamsError");
            return false;
        }

        String currentRefundOrderId = refundOrder.getRefundOrderId();

        // 需要将refundOrder.getState() 由INIT-->ING 并且 更新失败
        if(needInitToIng && !refundOrderService.updateInit2Ing(currentRefundOrderId, channelRetMsg.getChannelOrderId())) {
            throw new BizException("updateRefundStatus[Init->Ing]Error");
        }
        /* 是否同步成功 */

        boolean isSyncRefundSuccess = switch (channelRetMsg.getChannelState()) {
            case CONFIRM_FAIL -> refundOrderService.updateIng2Fail(currentRefundOrderId,channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
            case CONFIRM_SUCCESS -> refundOrderService.updateIng2Success(currentRefundOrderId,refundOrder.getChannelOrderNo());
            default -> throw new BizException("ErrorRefundChannelState:" + channelRetMsg.getChannelState());
        };

        /* 异步通知商户 */
        if (isSyncRefundSuccess) {
            payMchNotifyService.refundOrderNotify(refundOrderService.getById(currentRefundOrderId));
        }else {
            throw new BizException("updateRefundStatus[Ing->Success/Fail]Error");
        }
        return true;
    }


    // 此接口 目的是同步 渠道返回的信息 ,与本系统保持一致,并且参数 needInitToIng 表示不同使用场景
    //1. 无需将 transferOrder.state 从 Init --> Ing; 2. 需要从 Init --> Ing
    // 同步上游转账渠道结果至本系统
    // 调用本接口的前提是保证transferOrder.getState() == STATE_ING
    public boolean syncTransferChannel(ChannelRetMsg channelRetMsg, TransferOrder transferOrder,boolean needInitToIng) {

        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            log.error("syncTransferParamsError");
            return false;
        }
        String currentTransferOrderId = transferOrder.getTransferId();

        // 需要将transferOrder.getState() 由INIT-->ING 并且 更新失败
        if(needInitToIng && !transferOrderService.updateInit2Ing(transferOrder.getTransferId())) {
            throw new BizException("updateRefundStatus[Init->Ing]Error");
        }
        
        boolean isSyncTransferSuccess = switch (channelRetMsg.getChannelState()) {
                case CONFIRM_SUCCESS -> transferOrderService.updateIng2Success(currentTransferOrderId,channelRetMsg.getChannelOrderId());
                case CONFIRM_FAIL -> transferOrderService.updateIng2Fail(currentTransferOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
                default -> throw new BizException("ErrorTransferChannelState:" + channelRetMsg.getChannelState());
            };

        if(isSyncTransferSuccess) {
            payMchNotifyService.transferOrderNotify(transferOrderService.getById(currentTransferOrderId));
        }else {
            throw new BizException("updateRefundStatus[Ing->Success/Fail]Error");
        }
        return true;
    }

}
