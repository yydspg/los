package com.los.payment.task;

import com.los.service.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/2/28
 */
@Slf4j
@Component
public class PayOrderExpiredTask {
    @Autowired
    private PayOrderService payOrderService;
    //TODO 抽象cron表达式管理
    @Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
    public void start() {
        int updateCount = payOrderService.updateOrderExpired();
        log.info("处理订单超时{}条.", updateCount);
    }

}
