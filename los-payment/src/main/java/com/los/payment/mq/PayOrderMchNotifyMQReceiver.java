package com.los.payment.mq;

import com.los.components.mq.model.PayOrderMchNotifyMQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */

@Slf4j
@Component
public class PayOrderMchNotifyMQReceiver implements PayOrderMchNotifyMQ.IMQReceiver {

    @Override
    public void receive(PayOrderMchNotifyMQ.MsgPayload payload) {

    }
}
