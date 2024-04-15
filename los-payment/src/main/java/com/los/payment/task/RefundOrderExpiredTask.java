package com.los.payment.task;

import com.los.service.RefundOrderService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/2/28
 */
@Slf4j
@Component
public class RefundOrderExpiredTask {
    @Autowired private RefundOrderService refundOrderService;

    @XxlJob(value = "LosRefundOrderExpired")
    public void scheduleExpired() {
        int updateCount = refundOrderService.updateOrderExpired();
        log.info("处理退款订单超时{}条.", updateCount);
    }
}
