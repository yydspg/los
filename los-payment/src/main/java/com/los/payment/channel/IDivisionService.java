package com.los.payment.channel;

import com.los.core.entity.MchDivisionReceiver;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayOrderDivisionRecord;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;

import java.util.HashMap;
import java.util.List;

/**
 * 分账接口
 * @author paul 2024/2/28
 */

public interface IDivisionService {
    /** 获取到接口code **/
    String getIfCode();

    /** 是否支持该分账 */
    boolean isSupport();

    /** 绑定关系 **/
    ChannelRetMsg bind(MchDivisionReceiver mchDivisionReceiver, MchAppConfigContext mchAppConfigContext);

    /** 单次分账 （无需调用完结接口，或自动解冻商户资金)  **/
    ChannelRetMsg singleDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

    /** 查询分账结果  **/
    HashMap<Long, ChannelRetMsg> queryDivision(PayOrder payOrder, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext);

}
