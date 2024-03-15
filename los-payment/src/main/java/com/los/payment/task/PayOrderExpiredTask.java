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
// TODO 2024/3/15 : 改造点, 支付系统分布式部署,多个服务之间的并发问题
// TODO 2024/3/15 : 改造点, 多个支付实例,如此简单的定时任务执行会造成数据多次读写,如果回调数据失败,如何重试
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
