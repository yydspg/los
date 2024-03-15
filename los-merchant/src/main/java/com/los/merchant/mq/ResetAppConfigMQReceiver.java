package com.los.merchant.mq;

import com.los.components.mq.model.ResetAppConfigMQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */
@Slf4j
@Component
public class ResetAppConfigMQReceiver implements ResetAppConfigMQ.IMQReceiver {


    @Override
    public void receive(ResetAppConfigMQ.MsgPayload payload) {

    }
}
