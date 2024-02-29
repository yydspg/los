package com.los.payment.rqrs.payorder;

import lombok.Data;

/*
 * 通用支付数据RQ
 * @author paul 2024/2/27
 */
@Data
public class CommonPayDataRQ extends UnifiedOrderRQ{
    /* 请求参数： 支付数据包类型 */
    private String payDataType;
}
