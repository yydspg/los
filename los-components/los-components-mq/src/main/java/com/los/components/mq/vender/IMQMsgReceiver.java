package com.los.components.mq.vender;

/**
 * MQ 消息接收器 接口定义
 * @author paul 2024/2/1
 */

public interface IMQMsgReceiver {

    void receiveMsg(String msg);
}
