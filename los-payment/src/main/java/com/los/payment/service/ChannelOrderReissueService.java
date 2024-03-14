package com.los.payment.service;

import com.google.common.util.concurrent.AtomicDouble;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.core.utils.SpringBeansKit;
import com.los.payment.channel.IPayOrderQueryService;
import com.los.payment.channel.IPaymentService;
import com.los.payment.channel.IRefundService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.service.PayOrderService;
import com.los.service.TransferOrderService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 查询上游订单 , 执行补单操作
 * @author paul 2024/2/28
 */
@Slf4j
@Service
public class ChannelOrderReissueService {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private TransferOrderService transferOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    // TODO 2024/3/6 : 此部分未开发完成
    // TODO 2024/3/14 : 考虑能否使用设计模式优化此处代码
    public ChannelRetMsg processPayOrder(PayOrder payOrder) {
        try {
            String payId = payOrder.getPayOrderId();

            /* 查询转账接口是否存在 [抽象] */
            /* 设计模式为 抽象工厂方法 */
            IPayOrderQueryService queryService = SpringBeansKit.getBean(payOrder.getIfCode() + "PayOrderQueryService", IPayOrderQueryService.class);

            /* 转账接口不存在 */
            if (queryService == null) {
                log.error("{} payInterfaceNotExists", payOrder.getIfCode());
                return null;
            }

            /* 查询此商户下application的配置信息 */
            // TODO: 2024/3/4 考虑是否存在并发问题
            // TODO: 2024/3/4 个人理解是对于某个Service对外提供的方法,应当自身处理并发问题,上游的调用是无感的
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());

            /* 查询渠道接口返回信息 */

            ChannelRetMsg channelRetMsg = queryService.query(payOrder, mchAppConfigContext);

            /* 若 channelMsg 不存在 */

            if(channelRetMsg == null) {
                log.error("[{}]channelRetMsgNotExists",payId);
                return null;
            }


            /* 同步数据 */
            return channelRetMsg;
        } catch (Exception e) {
            log.error("payOrder[{}]ReissueError[{}]",payOrder.getPayOrderId(),e.getMessage());
            return null;
        }
    }
    public ChannelRetMsg processRefundOrder(RefundOrder refundOrder) {
        try {
            String refundId = refundOrder.getRefundOrderId();

            /* 查询转账接口是否存在 [抽象] */
            /* 设计模式为 抽象工厂方法 */
            IRefundService refundService = SpringBeansKit.getBean(refundOrder.getIfCode() + "RefundService", IRefundService.class);

            /* 转账接口不存在 */
            if (refundService == null) {
                log.error("{} refundInterfaceNotExists", refundOrder.getIfCode());
                return null;
            }

            /* 查询此商户下application的配置信息 */
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(refundOrder.getMchNo(), refundOrder.getAppId());

            /* 查询渠道接口返回信息 */

            ChannelRetMsg channelRetMsg = refundService.query(refundOrder, mchAppConfigContext);

            /* 若 channelMsg 不存在 */

            if(channelRetMsg == null) {
                log.error("[{}]channelRetMsgNotExists",refundId);
                return null;
            }

            /* 同步数据 */


            return channelRetMsg;
        } catch (Exception e) {
            log.error("refundOrder[{}]ReissueError[{}]",refundOrder.getRefundOrderId(),e.getMessage());
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
