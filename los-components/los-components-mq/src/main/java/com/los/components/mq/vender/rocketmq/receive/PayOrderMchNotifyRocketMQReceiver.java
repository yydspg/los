
package com.los.components.mq.vender.rocketmq.receive;

import com.los.components.mq.constant.MQVenderCS;
import com.los.components.mq.executor.MQThreadExecutor;
import com.los.components.mq.model.PayOrderMchNotifyMQ;
import com.los.components.mq.vender.IMQMsgReceiver;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/*
 * rocketMQ消息接收器：仅在vender=rocketMQ时 && 项目实现IMQReceiver接口时 进行实例化
 * 业务：  支付订单商户通知
 *
 */
/*
@ConditionalOnBean 是 Spring Boot 中的一个条件注解，它用于控制自动配置类或@Bean方法的加载和实例化时机。当这个注解被应用到一个配置类或者@Bean方法上时，
Spring 容器在启动过程中会检查指定的 bean 是否已经存在。如果指定类型的 bean 已经被创建并注册到了 Spring 容器中，则满足条件，该注解修饰的配置类或 Bean 将会被实例化和添加到容器中；反之，如果没有找到匹配的 bean，则不会实例化这个 Bean。
 */

/*
@ConditionalOnProperty 是 Spring Boot 中的一个条件注解，用于根据配置文件中的属性值来决定是否包含某个 Bean 的实例化和加载。这个注解允许开发人员基于应用的外部属性配置来有条件地启用或禁用特定的自动配置类或自定义的 @Bean 方法
具体使用时，可以通过以下属性进行控制：
value 或 name：这两个属性用来指定要检查的属性名称。如果指定了 name，那么它会与 prefix 属性结合形成完整的属性名；若使用 value，则直接对应属性全名或者数组形式的多个属性名。
prefix：属性前缀，默认为空字符串。当不指定完整属性名时，Spring 会自动加上 "."（点）作为前缀，以便能够匹配到类似 spring.myfeature.enabled 这样的属性。
havingValue：指定一个值，只有当所检查的属性的值与 havingValue 指定的值相匹配时，条件才满足。
matchIfMissing：布尔类型属性，默认为 false。如果设置为 true，则表示如果在配置文件中没有找到该属性，则条件视为满足。这意味着即使在配置文件中没有对应的属性键值对，也会加载被注解的 Bean。
 */

@Component
@ConditionalOnProperty(name = MQVenderCS.YML_VENDER_KEY, havingValue = MQVenderCS.ROCKET_MQ)
@ConditionalOnBean(PayOrderMchNotifyMQ.IMQReceiver.class)
@RocketMQMessageListener(topic = PayOrderMchNotifyMQ.MQ_NAME, consumerGroup = PayOrderMchNotifyMQ.MQ_NAME)
public class PayOrderMchNotifyRocketMQReceiver implements IMQMsgReceiver, RocketMQListener<String> {

    @Autowired
    private PayOrderMchNotifyMQ.IMQReceiver mqReceiver;

    /* 接收 【 queue 】 类型的消息 **/
    @Override
    public void receiveMsg(String msg){
        mqReceiver.receive(PayOrderMchNotifyMQ.parse(msg));
    }

    @Override
    @Async(MQThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    public void onMessage(String message) {
        this.receiveMsg(message);
    }

}
