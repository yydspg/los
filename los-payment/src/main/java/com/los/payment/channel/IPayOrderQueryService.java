package com.los.payment.channel;

import com.los.core.entity.PayOrder;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;

/**
 * 订单查询接口
 * @author paul 2024/2/28
 */

public interface IPayOrderQueryService {
    /** 获取到接口code **/
    String getIfCode();

    /** 查询订单 **/
    ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

}
