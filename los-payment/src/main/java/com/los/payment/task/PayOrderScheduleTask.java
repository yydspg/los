package com.los.payment.task;

import com.los.service.PayOrderService;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/2/28
 */
@Slf4j
@Component
public class PayOrderScheduleTask {
    @Resource private PayOrderService payOrderService;
    @XxlJob(value = "LosPayOrderExpired")
    public void scheduleExpired() {
        int updateCount = payOrderService.updateOrderExpired();
        log.info("处理订单超时{}条.", updateCount);
    }

}
