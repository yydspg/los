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

/*
* <p>
    * 商户支付通道表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_pay_passage")
@Schema(name = "MchPayPassage", description = "$!{table.comment}")
public class MchPayPassage extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<MchPayPassage> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户号")
    private String mchNo;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "支付接口")
    private String ifCode;

    @Schema(description = "支付方式")
    private String wayCode;

    @Schema(description = "支付方式费率")
    private BigDecimal rate;

    @Schema(description = "风控数据")
    private String riskConfig;

    @Schema(description = "状态: 0-停用, 1-启用")
    private Byte state;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
