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
    * 商户信息表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_info")
@Schema(name = "MchInfo", description = "$!{table.comment}")
public class MchInfo extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<MchInfo> gw() {return new LambdaQueryWrapper<>(); }
    public static final byte TYPE_NORMAL = 1; //商户类型： 1-普通商户
    public static final byte TYPE_ISVSUB = 2; //商户类型： 2-特约商户

    @Schema(description = "商户号")
    private String mchNo;

    @Schema(description = "商户名称")
    private String mchName;

    @Schema(description = "商户简称")
    private String mchShortName;

    @Schema(description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    private Byte type;

    @Schema(description = "服务商号")
    private String isvNo;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactTel;

    @Schema(description = "联系人邮箱")
    private String contactEmail;

    @Schema(description = "商户状态: 0-停用, 1-正常")
    private Byte state;

    @Schema(description = "商户备注")
    private String remark;

    @Schema(description = "初始用户ID（创建商户时，允许商户登录的用户）")
    private Long initUserId;

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
