package com.los.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

/**
* <p>
    * 系统用户认证表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_user_auth")
@Schema(name = "SysUserAuth", description = "$!{table.comment}")
public class SysUserAuth extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<SysUserAuth> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "ID")
    @TableId(value = "auth_id", type = IdType.AUTO)
    private Long authId;

    @Schema(description = "user_id")
    private Long userId;

    @Schema(description = "登录类型  1-登录账号 2-手机号 3-邮箱  10-微信  11-QQ 12-支付宝 13-微博")
    private Byte identityType;

    @Schema(description = "认证标识 ( 用户名 | open_id )")
    private String identifier;

    @Schema(description = "密码凭证")
    private String credential;

    @Schema(description = "salt")
    private String salt;

    @Schema(description = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;
}
