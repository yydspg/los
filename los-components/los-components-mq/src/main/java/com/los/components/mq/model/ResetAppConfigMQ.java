
package com.los.components.mq.model;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.components.mq.constant.MQSendTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
*
* 定义MQ消息格式
* 业务场景： [ 更新系统配置参数 ]
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/22 15:25
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetAppConfigMQ extends AbstractMQ {

    /* 【！重要配置项！】 定义MQ名称 **/
    public static final String MQ_NAME = "BROADCAST_RESET_APP_CONFIG";

    /* 内置msg 消息体定义 **/
    private MsgPayload payload;

    /*  【！重要配置项！】 定义Msg消息载体 **/
    @Data
    @AllArgsConstructor
    public static class MsgPayload {

        private String groupKey;

    }

    @Override
    public String getMQName() {
        return MQ_NAME;
    }

    /*  【！重要配置项！】 **/
    @Override
    public MQSendTypeEnum getMQType(){
        return MQSendTypeEnum.BROADCAST;  // QUEUE - 点对点 、 BROADCAST - 广播模式
    }

    @Override
    public String toMessage() {
        return JSONObject.toJSONString(payload);
    }

    /*  【！重要配置项！】 构造MQModel , 一般用于发送MQ时 **/
    public static ResetAppConfigMQ build(String groupKey){
        return new ResetAppConfigMQ(new MsgPayload(groupKey));
    }

    /* 解析MQ消息， 一般用于接收MQ消息时 **/
    public static MsgPayload parse(String msg){
        return JSON.parseObject(msg, MsgPayload.class);
    }

    /* 定义 IMQReceiver 接口： 项目实现该接口则可接收到对应的业务消息  **/
    public interface IMQReceiver{
        void receive(MsgPayload payload);
    }

}
