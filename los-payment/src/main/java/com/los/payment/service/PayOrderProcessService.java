package com.los.payment.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.los.components.mq.model.PayOrderDivisionMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.constants.CS;
import com.los.core.entity.PayOrder;
import com.los.service.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
    订单处理通用逻辑
 * @author paul 2024/2/28
 */
@Slf4j
@Service
public class PayOrderProcessService {

    @Autowired private PayOrderService payOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private IMQSender mqSender;

    /* 明确订单成功 */

    public void confirmSuccess(PayOrder payOrder) {

        /* 查询订单详情 */

        payOrder = payOrderService.getById(payOrder.getPayOrderId());

        /* 设置订单成功状态 */
        payOrder.setState(PayOrder.STATE_SUCCESS);

        /* 自动分账 */
        this.updatePayOrderAutoDivision(payOrder);

        /* 发送商户通知 */
        payMchNotifyService.payOrderNotify(payOrder);
    }
    /* 自动分账业务 */
    private void updatePayOrderAutoDivision(PayOrder payOrder) {
        //TODO 分布式时此段代码如何处理

        try {
            /* 分账模式不为自动model ,不处理*/

            if(payOrder == null || payOrder.getDivisionState() == null || payOrder.getDivisionMode() != PayOrder.DIVISION_MODE_AUTO) {
                return ;
            }

            /* 更新订单状态 */

            boolean isUpdateDivisionState = payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                    .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_WAIT_TASK)
                    .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId())
                    .eq(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_UNHAPPEN));

            /* 依据订单状态推送消息至分账 MQ */

            if (isUpdateDivisionState) {
                mqSender.send(PayOrderDivisionMQ.build(payOrder.getPayOrderId(), CS.YES,null),80);
            }
        } catch (Exception e) {
            log.error("订单[{}]自动分账异常:{}",payOrder.getPayOrderId(),e);
        }
    }
}
