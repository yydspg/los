package com.los.payment.rqrs.refund;

import com.los.core.entity.RefundOrder;
import com.los.core.utils.BeanKit;
import com.los.payment.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
   退款响应参数
 * @author paul 2024/2/27
 */
@Data
public class RefundOrderRS extends AbstractRS {
    /* 支付系统退款订单号 **/
    private String refundOrderId;

    /* 商户发起的退款订单号 **/
    private String mchRefundNo;

    /* 订单支付金额 **/
    private Long payAmount;

    /* 申请退款金额 **/
    private Long refundAmount;

    /* 退款状态 **/
    private Byte state;

    /* 渠道退款单号   **/
    private String channelOrderNo;

    /* 渠道返回错误代码 **/
    private String errCode;

    /* 渠道返回错误信息 **/
    private String errMsg;


    public static RefundOrderRS buildByRefundOrder(RefundOrder refundOrder){

        if(refundOrder == null){
            return null;
        }

        RefundOrderRS result = new RefundOrderRS();
        BeanKit.copyProperties(refundOrder, result);

        return result;
    }

}
