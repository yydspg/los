package com.los.components.mq.constant;

/**
 * 定义mq消费类类型
 * @author paul 2024/2/1
 */

public enum  MQSendTypeEnum {
    /** QUEUE - 点对点 （只有1个消费者可消费。 ActiveMQ的queue模式 ） **/
    QUEUE,
    /** BROADCAST - 订阅模式 (所有接收者都可接收到。 ActiveMQ的topic模式, RabbitMQ的fanout类型的交换机, RocketMQ的广播模式  ) **/
    BROADCAST
}
