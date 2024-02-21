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

/**
* <p>
    * 系统角色表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_role")
@Schema(name = "SysRole", description = "$!{table.comment}")
public class SysRole extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysRole> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "角色ID, ROLE_开头")
    private String roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;

    @Schema(description = "所属商户ID / 0(平台)")
    private String belongInfoId;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
