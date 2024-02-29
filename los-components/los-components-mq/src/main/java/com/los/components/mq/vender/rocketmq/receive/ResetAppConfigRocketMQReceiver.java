
package com.los.components.mq.vender.rocketmq.receive;

import com.los.components.mq.constant.MQVenderCS;
import com.los.components.mq.model.ResetAppConfigMQ;
import com.los.components.mq.vender.IMQMsgReceiver;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/*
* rocketMQ消息接收器：仅在vender=rocketMQ时 && 项目实现IMQReceiver接口时 进行实例化
* 业务：  更新系统配置参数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/22 17:06
*/
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
@ConditionalOnBean(ResetAppConfigMQ.IMQReceiver.class)
@RocketMQMessageListener(topic = ResetAppConfigMQ.MQ_NAME, consumerGroup = ResetAppConfigMQ.MQ_NAME, messageModel = MessageModel.BROADCASTING)
public class ResetAppConfigRocketMQReceiver implements IMQMsgReceiver, RocketMQListener<String> {

    @Autowired
    private ResetAppConfigMQ.IMQReceiver mqReceiver;

    /* 接收 【 MQSendTypeEnum.BROADCAST  】 广播类型的消息 **/
    @Override
    public void receiveMsg(String msg){
        mqReceiver.receive(ResetAppConfigMQ.parse(msg));
    }

    @Override
    public void onMessage(String message) {
        this.receiveMsg(message);
    }

}
