package com.los.payment.mq;

import com.los.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */

@Slf4j
@Component
public class ResetIsvMchAppInfoMQReceiver implements ResetIsvMchAppInfoConfigMQ.IMQReceiver {
    @Override
    public void receive(ResetIsvMchAppInfoConfigMQ.MsgPayload payload) {

    }
}
