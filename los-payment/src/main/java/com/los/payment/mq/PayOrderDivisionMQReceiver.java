package com.los.payment.mq;

import com.los.components.mq.model.PayOrderDivisionMQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */
@Slf4j
@Component
public class PayOrderDivisionMQReceiver implements PayOrderDivisionMQ.IMQReceiver {

    @Override
    public void receive(PayOrderDivisionMQ.MsgPayload payload) {

    }
}
