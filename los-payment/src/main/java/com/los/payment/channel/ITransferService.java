package com.los.payment.channel;

import com.los.core.entity.TransferOrder;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.transfer.TransferOrderRQ;

/**
 * 转账接口
 * @author paul 2024/2/28
 */

public interface ITransferService {
    /* 获取到接口code **/
    String getIfCode();

    /** 是否支持该支付入账方式 */
    boolean isSupport(String entryType);

    /** 前置检查如参数等信息是否符合要求， 返回错误信息或直接抛出异常即可  */
    String preCheck(TransferOrderRQ bizRQ, TransferOrder transferOrder);

    /** 调起转账接口，并响应数据；  内部处理普通商户和服务商模式  **/
    ChannelRetMsg transfer(TransferOrderRQ bizRQ, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) throws Exception;

    /** 调起转账查询接口  **/
    ChannelRetMsg query(TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext);

}
