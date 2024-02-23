package com.los.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.los.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;

/**
* <p>
    * 退款订单表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_refund_order")
@Schema(name = "RefundOrder", description = "$!{table.comment}")
public class RefundOrder extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<RefundOrder> gw() {return new LambdaQueryWrapper<>(); }

    public static final byte STATE_INIT = 0; //订单生成
    public static final byte STATE_ING = 1; //退款中
    public static final byte STATE_SUCCESS = 2; //退款成功
    public static final byte STATE_FAIL = 3; //退款失败
    public static final byte STATE_CLOSED = 4; //退款任务关闭

    @Schema(description = "退款订单号（支付系统生成订单号）")
    private String refundOrderId;

    @Schema(description = "支付订单号（与t_pay_order对应）")
    private String payOrderId;

    @Schema(description = "渠道支付单号（与t_pay_order channel_order_no对应）")
    private String channelPayOrderNo;

    @Schema(description = "商户号")
    private String mchNo;

    @Schema(description = "服务商号")
    private String isvNo;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "商户名称")
    private String mchName;

    @Schema(description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    private Byte mchType;

    @Schema(description = "商户退款单号（商户系统的订单号）")
    private String mchRefundNo;

    @Schema(description = "支付方式代码")
    private String wayCode;

    @Schema(description = "支付接口代码")
    private String ifCode;

    @Schema(description = "支付金额,单位分")
    private Long payAmount;

    @Schema(description = "退款金额,单位分")
    private Long refundAmount;

    @Schema(description = "三位货币代码,人民币:cny")
    private String currency;

    @Schema(description = "退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭")
    private Byte state;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @Schema(description = "渠道错误码")
    private String errCode;

    @Schema(description = "渠道错误描述")
    private String errMsg;

    @Schema(description = "特定渠道发起时额外参数")
    private String channelExtra;

    @Schema(description = "通知地址")
    private String notifyUrl;

    @Schema(description = "扩展参数")
    private String extParam;

    @Schema(description = "订单退款成功时间")
    private Date successTime;

    @Schema(description = "退款失效时间（失效后系统更改为退款任务关闭状态）")
    private Date expiredTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updatedAt;
}
