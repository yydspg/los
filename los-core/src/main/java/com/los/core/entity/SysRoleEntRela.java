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

/*
* <p>
    * 系统角色权限关联表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_role_ent_rela")
@Schema(name = "SysRoleEntRela", description = "$!{table.comment}")
public class SysRoleEntRela extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysRoleEntRela> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "权限ID")
    private String entId;
}
