package com.los.payment.rqrs.division;

import com.los.payment.rqrs.AbstractMchAppRQ;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/*
 * 发起订单分账 请求参数
 * @author paul 2024/2/27
 */

@Data
public class PayOrderDivisionExecRQ extends AbstractMchAppRQ {

    /* 商户订单号 **/
    private String mchOrderNo;

    /* 支付系统订单号 **/
    private String payOrderId;

    /*
     * 是否使用系统配置的自动分账组： 0-否 1-是
     **/
    @NotNull(message = "是否使用系统配置的自动分账组不能为空")
    private Byte useSysAutoDivisionReceivers;

    /* 接收者账号列表（JSONArray 转换为字符串类型）
     * 仅当useSysAutoDivisionReceivers=0 时有效。
     *
     * 参考：
     *
     * 方式1： 按账号纬度
     * [{
     *     receiverId: 800001,
     *     divisionProfit: 0.1 (若不填入则使用系统默认配置值)
     * }]
     *
     * 方式2： 按组纬度
     * [{
     *     receiverGroupId: 100001, (该组所有 当前订单的渠道账号并且可用状态的全部参与分账)
     *     divisionProfit: 0.1 (每个账号的分账比例， 若不填入则使用系统默认配置值， 建议不填写)
     * }]
     *
     * **/
    private String receivers;

}