package com.los.payment.rqrs.payorder;

import com.alibaba.fastjson2.annotation.JSONField;
import com.los.payment.rqrs.AbstractRS;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import lombok.Data;

/*
 * @author paul 2024/2/27
 */
@Data
public class ClosePayOrderRS extends AbstractRS {
    /* 上游渠道返回数据包 (无需JSON序列化) **/
    @JSONField(serialize = false)
    private ChannelRetMsg channelRetMsg;
}
