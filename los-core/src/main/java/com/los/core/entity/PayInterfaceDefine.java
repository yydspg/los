package com.los.core.entity;

import com.alibaba.fastjson2.JSONArray;
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
import java.io.Serializable;
import java.time.LocalDateTime;

/*
* <p>
    * 支付接口定义表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_interface_define")
@Schema(name = "PayInterfaceDefine", description = "$!{table.comment}")
public class PayInterfaceDefine extends BaseModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<PayInterfaceDefine> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "接口代码 全小写  wxpay alipay ")
    @TableId
    private String ifCode;

    @Schema(description = "接口名称")
    private String ifName;

    @Schema(description = "是否支持普通商户模式: 0-不支持, 1-支持")
    private Byte isMchMode;

    @Schema(description = "是否支持服务商子商户模式: 0-不支持, 1-支持")
    private Byte isIsvMode;

    @Schema(description = "支付参数配置页面类型:1-JSON渲染,2-自定义")
    private Byte configPageType;

    @Schema(description = "ISV接口配置定义描述,json字符串")
    private String isvParams;

    @Schema(description = "特约商户接口配置定义描述,json字符串")
    private String isvsubMchParams;

    @Schema(description = "普通商户接口配置定义描述,json字符串")
    private String normalMchParams;

    @Schema(description = "支持的支付方式")
    private JSONArray wayCodes;

    @Schema(description = "页面展示：卡片-图标")
    private String icon;

    @Schema(description = "页面展示：卡片-背景色")
    private String bgColor;

    @Schema(description = "状态: 0-停用, 1-启用")
    private Byte state;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
