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
    * 服务商信息表
    * </p>
* @author paul
* @since 2024-01-31
*/
/*
@EqualsAndHashCode(callSuper = false)
`@EqualsAndHashCode(callSuper = false)` 是Lombok库中的一个注解，它用于在Java类上自动生成 `equals()` 和 `hashCode()` 方法的实现。这两个方法是Java中用于判断对象相等性和支持集合（如HashMap和HashSet）正确工作的核心方法。
- `equals()`：此方法定义了对象间的相等性逻辑。如果两个对象相等，它们的 `equals()` 方法应该返回 `true`，否则返回 `false`。
- `hashCode()`：每个对象都应该有一个稳定的、与 `equals()` 逻辑一致的哈希码。这意味着如果两个对象根据 `equals()` 判断为相等，则它们必须拥有相同的哈希码。
`callSuper = false` 参数是一个标志，用于指示生成的 `equals()` 和 `hashCode()` 方法是否需要考虑从父类继承下来的属性。当设置为 `false` 时：
1. Lombok生成的 `equals()` 方法仅基于当前类（即注解所在类）的字段来判断相等性，而不会包括任何超类的字段。
2. 同样地，生成的 `hashCode()` 方法只计算当前类字段的贡献值，不包含超类的字段对哈希码的影响。
这样做的主要原因是避免由于父类行为未知或者有意忽略父类状态而导致的潜在问题，尤其是在设计复合对象或实体类时，通常希望子类实例的相等性仅由子类自身的属性决定。
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_isv_info")
@Schema(name = "IsvInfo", description = "$!{table.comment}")
public class IsvInfo extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<IsvInfo> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "服务商号")
    private String isvNo;

    @Schema(description = "服务商名称")
    private String isvName;

    @Schema(description = "服务商简称")
    private String isvShortName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactTel;

    @Schema(description = "联系人邮箱")
    private String contactEmail;

    @Schema(description = "状态: 0-停用, 1-正常")
    private Byte state;

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
