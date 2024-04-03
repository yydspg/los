package com.los.merchant.mq;

import com.los.components.mq.model.ResetAppConfigMQ;
import com.los.service.impl.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */
@Slf4j
@Component
public class ResetAppConfigMQReceiver implements ResetAppConfigMQ.IMQReceiver {
    @Resource private SysConfigService sysConfigService;

    @Override
    public void receive(ResetAppConfigMQ.MsgPayload payload) {
        log.info("成功接收更新系统配置的订阅通知, msg={}", payload);
        sysConfigService.initDBConfig(payload.getGroupKey());
        log.info("系统配置静态属性已重置");
    }
}
