package com.los.payment.rqrs.transfer;

import com.los.payment.rqrs.AbstractMchAppRQ;
import lombok.Data;

/*
查询转账单请求参数对象
 * @author paul 2024/2/27
 */
@Data
public class QueryTransferOrderRQ extends AbstractMchAppRQ {
    /* 商户转账单号 **/
    private String mchOrderNo;

    /* 支付系统转账单号 **/
    private String transferId;
}
