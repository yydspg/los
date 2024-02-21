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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* <p>
    * 支付接口配置参数表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_interface_config")
@Schema(name = "PayInterfaceConfig", description = "$!{table.comment}")
public class PayInterfaceConfig extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<PayInterfaceConfig> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "账号类型:1-服务商 2-商户 3-商户应用")
    private Byte infoType;

    @Schema(description = "服务商号/商户号/应用ID")
    private String infoId;

    @Schema(description = "支付接口代码")
    private String ifCode;

    @Schema(description = "接口配置参数,json字符串")
    private String ifParams;

    @Schema(description = "支付接口费率")
    private BigDecimal ifRate;

    @Schema(description = "状态: 0-停用, 1-启用")
    private Byte state;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建者用户ID")
    private Long createdUid;

    @Schema(description = "创建者姓名")
    private String createdBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新者用户ID")
    private Long updatedUid;

    @Schema(description = "更新者姓名")
    private String updatedBy;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
