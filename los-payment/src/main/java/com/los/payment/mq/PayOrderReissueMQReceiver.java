package com.los.payment.mq;

import com.los.components.mq.model.PayOrderReissueMQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */

@Slf4j
@Component
public class PayOrderReissueMQReceiver implements PayOrderReissueMQ.IMQReceiver {

    @Override
    public void receive(PayOrderReissueMQ.MsgPayload payload) {

    }
}
