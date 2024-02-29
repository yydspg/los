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

/*
* <p>
    * 商户应用表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_app")
@Schema(name = "MchApp", description = "$!{table.comment}")
public class MchApp extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<MchApp> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "商户号")
    private String mchNo;

    @Schema(description = "应用状态: 0-停用, 1-正常")
    private Byte state;

    @Schema(description = "应用私钥")
    private String appSecret;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建者用户ID")
    private Long createdUid;

    @Schema(description = "创建者姓名")
    private String createdBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
