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
    * 转账订单表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_transfer_order")
@Schema(name = "TransferOrder", description = "$!{table.comment}")
public class TransferOrder extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;


    /** 入账方式 **/
    public static final String ENTRY_WX_CASH = "WX_CASH";
    public static final String ENTRY_ALIPAY_CASH = "ALIPAY_CASH";
    public static final String ENTRY_BANK_CARD = "BANK_CARD";

    public static final byte STATE_INIT = 0; //订单生成
    public static final byte STATE_ING = 1; //转账中
    public static final byte STATE_SUCCESS = 2; //转账成功
    public static final byte STATE_FAIL = 3; //转账失败
    public static final byte STATE_CLOSED = 4; //转账关闭
    public static  LambdaQueryWrapper<TransferOrder> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "转账订单号")
    private String transferId;

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

    @Schema(description = "入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡")
    private String entryType;

    @Schema(description = "转账金额,单位分")
    private Long amount;

    @Schema(description = "三位货币代码,人民币:cny")
    private String currency;

    @Schema(description = "收款账号")
    private String accountNo;

    @Schema(description = "收款人姓名")
    private String accountName;

    @Schema(description = "收款人开户行名称")
    private String bankName;

    @Schema(description = "转账备注信息")
    private String transferDesc;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭")
    private Byte state;

    @Schema(description = "特定渠道发起额外参数")
    private String channelExtra;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @Schema(description = "渠道支付错误码")
    private String errCode;

    @Schema(description = "渠道支付错误描述")
    private String errMsg;

    @Schema(description = "商户扩展参数")
    private String extParam;

    @Schema(description = "异步通知地址")
    private String notifyUrl;

    @Schema(description = "转账成功时间")
    private Date successTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updatedAt;
}
