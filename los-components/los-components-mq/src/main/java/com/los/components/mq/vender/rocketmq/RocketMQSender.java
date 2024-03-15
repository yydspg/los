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
/*
    `@ConditionalOnProperty` 是 Spring Boot 中的一个条件注解，用于根据配置文件中的属性值来决定是否应该实例化一个 Bean 或加载某个配置类。在您提供的代码片段中：

    ```java
    @ConditionalOnProperty(
        name = MQVenderCS.YML_VENDER_KEY,
        havingValue = MQVenderCS.ROCKET_MQ
    )
    ```

    这个注解表示：

    - `name` 属性指定了配置文件（通常如 application.properties 或 application.yml）中的属性名，这里的值是从 `MQVenderCS` 类中引用的静态常量 `YML_VENDER_KEY`。
    这意味着 Spring Boot 将查找配置文件中以 `MQVenderCS.YML_VENDER_KEY` 表示的属性值。

    - `havingValue` 属性则是检查上述属性所对应的值是否等于 `MQVenderCS.ROCKET_MQ` 这个常量值。
    如果配置文件中 `MQVenderCS.YML_VENDER_KEY` 对应的值正好等于 `ROCKET_MQ`，那么被此注解修饰的 Bean 或配置类才会被加载和实例化。

    例如，在配置文件中可能有这样的设置：
    ```yaml
    # application.yml
    mqvender: rocket_mq
    ```
    若 `MQVenderCS.YML_VENDER_KEY` 的值是 `"mqvender"`，
    那么由于 `rocket_mq` 与 `MQVenderCS.ROCKET_MQ` 值匹配，带有此 `@ConditionalOnProperty` 注解的类或方法会被启用。

    总结来说，这段注解的作用是：只有当应用程序的配置属性中 `MQVenderCS.YML_VENDER_KEY` 对应的值为 `MQVenderCS.ROCKET_MQ` 时，相关的配置或 Bean 才会生效。
 */
public class RocketMQSender implements IMQSender {
    @Override
    public void send(AbstractMQ mqModel) {

    }

    @Override
    public void send(AbstractMQ mqModel, int delay) {

    }
}
