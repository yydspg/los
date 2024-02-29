package com.los.payment.rqrs.payorder;

import com.los.payment.rqrs.AbstractMchAppRQ;
import lombok.Data;

/*
 * @author paul 2024/2/27
 */
@Data
public class ClosePayOrderRQ extends AbstractMchAppRQ {
    /* 商户订单号 **/
    private String mchOrderNo;

    /* 支付系统订单号 **/
    private String payOrderId;
}
