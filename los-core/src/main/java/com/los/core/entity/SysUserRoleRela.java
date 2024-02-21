package com.los.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.los.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
* <p>
    * 操作员<->角色 关联表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_user_role_rela")
@Schema(name = "SysUserRoleRela", description = "$!{table.comment}")
public class SysUserRoleRela extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysUserRoleRela> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色ID")
    private String roleId;
}
