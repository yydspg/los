package com.los.payment.rqrs.payorder;

import com.los.core.entity.PayOrder;
import com.los.payment.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
 *  查询订单 响应参数
 * @author paul 2024/2/27
 */
@Data
public class QueryPayOrderRS extends AbstractRS {
    /*
     * 支付订单号
     */
    private String payOrderId;


    /*
     * 商户号
     */
    private String mchNo;

    /*
     * 商户应用ID
     */
    private String appId;

    /*
     * 商户订单号
     */
    private String mchOrderNo;

    /*
     * 支付接口代码
     */
    private String ifCode;

    /*
     * 支付方式代码
     */
    private String wayCode;

    /*
     * 支付金额,单位分
     */
    private Long amount;

    /*
     * 三位货币代码,人民币:cny
     */
    private String currency;

    /*
     * 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭
     */
    private Byte state;

    /*
     * 客户端IP
     */
    private String clientIp;

    /*
     * 商品标题
     */
    private String subject;

    /*
     * 商品描述信息
     */
    private String body;

    /*
     * 渠道订单号
     */
    private String channelOrderNo;

    /*
     * 渠道支付错误码
     */
    private String errCode;

    /*
     * 渠道支付错误描述
     */
    private String errMsg;

    /*
     * 商户扩展参数
     */
    private String extParam;

    /*
     * 订单支付成功时间
     */
    private Long successTime;

    /*
     * 创建时间
     */
    private Long createdAt;


    public static QueryPayOrderRS buildByPayOrder(PayOrder payOrder){

        if(payOrder == null){
            return null;
        }

        QueryPayOrderRS result = new QueryPayOrderRS();
        BeanUtils.copyProperties(payOrder, result);
        result.setSuccessTime(payOrder.getSuccessTime() == null ? null : payOrder.getSuccessTime().getTime());
        result.setCreatedAt(payOrder.getCreatedAt() == null ? null : payOrder.getCreatedAt().getTime());

        return result;
    }
}
