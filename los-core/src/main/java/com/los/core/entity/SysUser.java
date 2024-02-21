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

/**
* <p>
    * 系统用户表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_user")
@Schema(name = "SysUser", description = "$!{table.comment}")
public class SysUser extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysUser> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "系统用户ID")
    @TableId(value = "sys_user_id", type = IdType.AUTO)
    private Long sysUserId;

    @Schema(description = "登录用户名")
    private String loginUsername;

    @Schema(description = "真实姓名")
    private String realname;

    @Schema(description = "手机号")
    private String telphone;

    @Schema(description = "性别 0-未知, 1-男, 2-女")
    private Byte sex;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "员工编号")
    private String userNo;

    @Schema(description = "是否超管（超管拥有全部权限） 0-否 1-是")
    private Byte isAdmin;

    @Schema(description = "状态 0-停用 1-启用")
    private Byte state;

    @Schema(description = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;

    @Schema(description = "所属商户ID / 0(平台)")
    private String belongInfoId;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
