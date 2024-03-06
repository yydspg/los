package com.los.payment.service;

import com.los.core.entity.TransferOrder;
import com.los.core.utils.SpringBeansKit;
import com.los.payment.channel.ITransferService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.service.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/2/28
 */

@Slf4j
@Service
public class TransferOrderReissueService {
    // TODO: 2024/3/4 为什么要将ChannelOrderReissueService中对PayOrder,RefundOrder的补单操作与TransferOrder的补单操作分离?
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private TransferOrderService transferOrderService;
    @Autowired
    private PayMchNotifyService payMchNotifyService;

    public ChannelRetMsg processTransferOrder(TransferOrder transferOrder) {
        try {
            String transferId = transferOrder.getTransferId();

            /* 查询转账接口是否存在 [抽象] */
            /* 设计模式为 抽象工厂方法 */
            ITransferService transferService = SpringBeansKit.getBean(transferOrder.getIfCode() + "TransferService", ITransferService.class);

            /* 转账接口不存在 */
            if (transferService == null) {
                log.error("{} transferInterfaceNotExists", transferOrder.getIfCode());
                return null;
            }

            /* 查询此商户下application的配置信息 */
            // TODO: 2024/3/4 考虑是否存在并发问题
            // TODO: 2024/3/4 个人理解是对于某个Service对外提供的方法,应当自身处理并发问题,上游的调用是无感的
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(transferOrder.getMchNo(), transferOrder.getAppId());

            /* 查询渠道接口返回信息 */

            ChannelRetMsg channelRetMsg = transferService.query(transferOrder, mchAppConfigContext);

            /* 若 channelMsg 不存在 */

            if(channelRetMsg == null) {
                log.error("[{}]channelRetMsgNotExists",transferId);
                return null;
            }
            // TODO: 2024/3/5 为何要在此打印日志?
            log.info("[{}]查询结果:[{}]",transferId,channelRetMsg);

            /* 同步数据 */
            this.synchronizedTransferOrder(channelRetMsg,transferId);

            return channelRetMsg;
        } catch (Exception e) {
            log.error("transferOrder[{}]ReissueError[{}]",transferOrder.getTransferId(),e.getMessage());
            return null;
        }
    }

    public void synchronizedTransferOrder(ChannelRetMsg channelRetMsg,String transferId) {
        boolean isSynchronizedSuccess = switch (channelRetMsg.getChannelState()) {
            /* 确认失败 */
            case CONFIRM_FAIL -> transferOrderService.updateIng2Fail(transferId,channelRetMsg.getChannelOrderId(),channelRetMsg.getChannelErrCode(),channelRetMsg.getChannelErrMsg());
            /* 确认成功 */
            case CONFIRM_SUCCESS -> transferOrderService.updateIng2Success(transferId,channelRetMsg.getChannelOrderId());

            default -> throw new IllegalStateException("Unexpected value: " + channelRetMsg.getChannelState());
        };
        /* 通知商户 */
        if(isSynchronizedSuccess) {
            payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferId));
        }
    }
}
