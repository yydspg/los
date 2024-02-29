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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/*
* <p>
    * 支付订单表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_order")
@Schema(name = "PayOrder", description = "$!{table.comment}")
public class PayOrder extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<PayOrder> gw() {return new LambdaQueryWrapper<>(); }

    public static final byte STATE_INIT = 0; //订单生成
    public static final byte STATE_ING = 1; //支付中
    public static final byte STATE_SUCCESS = 2; //支付成功
    public static final byte STATE_FAIL = 3; //支付失败
    public static final byte STATE_CANCEL = 4; //已撤销
    public static final byte STATE_REFUND = 5; //已退款
    public static final byte STATE_CLOSED = 6; //订单关闭

    public static final byte REFUND_STATE_NONE = 0; //未发生实际退款
    public static final byte REFUND_STATE_SUB = 1; //部分退款
    public static final byte REFUND_STATE_ALL = 2; //全额退款


    public static final byte DIVISION_MODE_FORBID = 0; //该笔订单不允许分账
    public static final byte DIVISION_MODE_AUTO = 1; //支付成功按配置自动完成分账
    public static final byte DIVISION_MODE_MANUAL = 2; //商户手动分账(解冻商户金额)

    public static final byte DIVISION_STATE_UNHAPPEN = 0; //未发生分账
    public static final byte DIVISION_STATE_WAIT_TASK = 1; //等待分账任务处理
    public static final byte DIVISION_STATE_ING = 2; //分账处理中
    public static final byte DIVISION_STATE_FINISH = 3; //分账任务已结束(不体现状态)

    @Schema(description = "支付订单号")
    private String payOrderId;

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

    @Schema(description = "商户订单号")
    private String mchOrderNo;

    @Schema(description = "支付接口代码")
    private String ifCode;

    @Schema(description = "支付方式代码")
    private String wayCode;

    @Schema(description = "支付金额,单位分")
    private Long amount;

    @Schema(description = "商户手续费费率快照")
    private BigDecimal mchFeeRate;

    @Schema(description = "商户手续费,单位分")
    private Long mchFeeAmount;

    @Schema(description = "三位货币代码,人民币:cny")
    private String currency;

    @Schema(description = "支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭")
    private Byte state;

    @Schema(description = "向下游回调状态, 0-未发送,  1-已发送")
    private Byte notifyState;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "商品标题")
    private String subject;

    @Schema(description = "商品描述信息")
    private String body;

    @Schema(description = "特定渠道发起额外参数")
    private String channelExtra;

    @Schema(description = "渠道用户标识,如微信openId,支付宝账号")
    private String channelUser;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @Schema(description = "退款状态: 0-未发生实际退款, 1-部分退款, 2-全额退款")
    private Byte refundState;

    @Schema(description = "退款次数")
    private Integer refundTimes;

    @Schema(description = "退款总金额,单位分")
    private Long refundAmount;

    @Schema(description = "订单分账模式：0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)")
    private Byte divisionMode;

    @Schema(description = "订单分账状态：0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)")
    private Byte divisionState;

    @Schema(description = "最新分账时间")
    private LocalDateTime divisionLastTime;

    @Schema(description = "渠道支付错误码")
    private String errCode;

    @Schema(description = "渠道支付错误描述")
    private String errMsg;

    @Schema(description = "商户扩展参数")
    private String extParam;

    @Schema(description = "异步通知地址")
    private String notifyUrl;

    @Schema(description = "页面跳转地址")
    private String returnUrl;
    //localDateTime和Date的区别
    //TODO 考虑是否需要进行LocalDateTime的升级
    @Schema(description = "订单失效时间")
    private Date expiredTime;

    @Schema(description = "订单支付成功时间")
    private Date successTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updatedAt;
}
