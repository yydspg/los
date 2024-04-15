package com.los.payment.channel;

import com.los.core.entity.PayOrder;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;

/**
 * 关闭订单（渠道侧）接口定义
 * @author paul 2024/2/28
 */

public interface IPayOrderCloseService {
    /** 获取到接口code **/
    String getIfCode();

    /** 关闭订单 **/
    ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
