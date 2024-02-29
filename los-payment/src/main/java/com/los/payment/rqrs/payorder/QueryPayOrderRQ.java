package com.los.payment.rqrs.payorder;

import com.los.payment.rqrs.AbstractMchAppRQ;
import lombok.Data;

/*
查询订单请求参数对象
*@author paul 2024/2/27
*/    
@Data
public class QueryPayOrderRQ extends AbstractMchAppRQ {
    /* 商户订单号 **/
    private String mchOrderNo;

    /* 支付系统订单号 **/
    private String payOrderId;
}
