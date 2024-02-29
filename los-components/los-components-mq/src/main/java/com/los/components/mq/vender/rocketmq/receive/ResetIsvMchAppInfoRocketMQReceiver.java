
package com.los.components.mq.vender.rocketmq.receive;

import com.los.components.mq.constant.MQVenderCS;
import com.los.components.mq.model.ResetIsvMchAppInfoConfigMQ;
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
* 业务：  更新服务商/商户/商户应用配置信息
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/22 17:06
*/
@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
@ConditionalOnBean(ResetIsvMchAppInfoConfigMQ.IMQReceiver.class)
@RocketMQMessageListener(topic = ResetIsvMchAppInfoConfigMQ.MQ_NAME, consumerGroup = ResetIsvMchAppInfoConfigMQ.MQ_NAME, messageModel = MessageModel.BROADCASTING)
public class ResetIsvMchAppInfoRocketMQReceiver implements IMQMsgReceiver, RocketMQListener<String> {

    @Autowired
    private ResetIsvMchAppInfoConfigMQ.IMQReceiver mqReceiver;

    /* 接收 【 MQSendTypeEnum.BROADCAST  】 广播类型的消息 **/
    @Override
    public void receiveMsg(String msg){
        mqReceiver.receive(ResetIsvMchAppInfoConfigMQ.parse(msg));
    }

    @Override
    public void onMessage(String message) {
        this.receiveMsg(message);
    }

}
