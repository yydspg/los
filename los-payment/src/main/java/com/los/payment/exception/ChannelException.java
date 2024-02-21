package com.los.payment.exception;

import com.los.payment.rqrs.msg.ChannelRetMsg;
import lombok.Getter;

import java.io.Serial;

/**
 * 请求渠道异常
 * @author paul 2024/2/4
 */
@Getter
public class ChannelException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    private ChannelRetMsg channelRetMsg;
    /** 业务自定义异常 **/
    private ChannelException(ChannelRetMsg channelRetMsg) {
        super(channelRetMsg != null ? channelRetMsg.getChannelErrMsg() : null);
        this.channelRetMsg = channelRetMsg;
    }

    /** 未知状态 **/
    public static ChannelException unknown(String channelErrMsg){
        return new ChannelException(ChannelRetMsg.unknown(channelErrMsg));
    }

    /** 系统内异常 **/
    public static ChannelException sysError(String channelErrMsg){
        return new ChannelException(ChannelRetMsg.sysError(channelErrMsg));
    }
}
