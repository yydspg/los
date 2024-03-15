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
    * 系统配置表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_config")
@Schema(name = "SysConfig", description = "$!{table.comment}")
public class SysConfig extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysConfig> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "配置KEY")
    @TableId(value = "config_key",type = IdType.INPUT)
    private String configKey;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "描述信息")
    private String configDesc;

    @Schema(description = "分组key")
    private String groupKey;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "配置内容项")
    private String configVal;

    @Schema(description = "类型: text-输入框, textarea-多行文本, uploadImg-上传图片, switch-开关")
    private String type;

    @Schema(description = "显示顺序")
    private Long sortNum;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
