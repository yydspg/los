package com.los.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.los.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/*
* <p>
    * 商户通知记录表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_notify_record")
@Schema(name = "MchNotifyRecord", description = "$!{table.comment}")
public class MchNotifyRecord extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;
    //订单类型:1-支付,2-退款, 3-转账
    public static final byte TYPE_PAY_ORDER = 1;
    public static final byte TYPE_REFUND_ORDER = 2;
    public static final byte TYPE_TRANSFER_ORDER = 3;

    //通知状态
    public static final byte STATE_ING = 1;
    public static final byte STATE_SUCCESS = 2;
    public static final byte STATE_FAIL = 3;
    public static  LambdaQueryWrapper<MchNotifyRecord> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "商户通知记录ID")
    @TableId(value = "notify_id", type = IdType.AUTO)
    private Long notifyId;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "订单类型:1-支付,2-退款")
    private Byte orderType;

    @Schema(description = "商户订单号")
    private String mchOrderNo;

    @Schema(description = "商户号")
    private String mchNo;

    @Schema(description = "服务商号")
    private String isvNo;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "通知地址")
    private String notifyUrl;

    @Schema(description = "通知响应结果")
    private String resResult;

    @Schema(description = "通知次数")
    private Integer notifyCount;

    @Schema(description = "最大通知次数, 默认6次")
    private Integer notifyCountLimit;

    @Schema(description = "通知状态,1-通知中,2-通知成功,3-通知失败")
    private Byte state;

    @Schema(description = "最后一次通知时间")
    private LocalDateTime lastNotifyTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
