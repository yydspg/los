package com.los.components.mq.vender.rocketmq;

/*
 * @author paul 2024/2/1
 */

import com.los.components.mq.constant.MQVenderCS;
import com.los.components.mq.model.AbstractMQ;
import com.los.components.mq.vender.IMQSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
public class RocketMQSender implements IMQSender {
    @Override
    public void send(AbstractMQ mqModel) {

    }

    @Override
    public void send(AbstractMQ mqModel, int delay) {

    }
}
