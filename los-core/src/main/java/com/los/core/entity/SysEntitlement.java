package com.los.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
    * 系统权限表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_entitlement")
@Schema(name = "SysEntitlement", description = "$!{table.comment}")
public class SysEntitlement extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysEntitlement> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD")
    @TableId
    private String entId;

    @Schema(description = "权限名称")
    private String entName;

    @Schema(description = "菜单图标")
    private String menuIcon;

    @Schema(description = "菜单uri/路由地址")
    private String menuUri;

    @Schema(description = "组件Name（前后端分离使用）")
    private String componentName;

    @Schema(description = "权限类型 ML-左侧显示菜单, MO-其他菜单, PB-页面/按钮")
    private String entType;

    @Schema(description = "快速开始菜单 0-否, 1-是")
    private Byte quickJump;

    @Schema(description = "状态 0-停用, 1-启用")
    private Byte state;

    @Schema(description = "父ID")
    private String pid;

    @Schema(description = "排序字段, 规则：正序")
    private Integer entSort;

    @Schema(description = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
