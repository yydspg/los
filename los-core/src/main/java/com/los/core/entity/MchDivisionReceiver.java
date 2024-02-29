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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
* <p>
    * 商户分账接收者账号绑定关系表
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_division_receiver")
@Schema(name = "MchDivisionReceiver", description = "$!{table.comment}")
public class MchDivisionReceiver extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<MchDivisionReceiver> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "分账接收者ID")
    @TableId(value = "receiver_id", type = IdType.AUTO)
    private Long receiverId;

    @Schema(description = "接收者账号别名")
    private String receiverAlias;

    @Schema(description = "组ID（便于商户接口使用）")
    private Long receiverGroupId;

    @Schema(description = "组名称")
    private String receiverGroupName;

    @Schema(description = "商户号")
    private String mchNo;

    @Schema(description = "服务商号")
    private String isvNo;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "支付接口代码")
    private String ifCode;

    @Schema(description = "分账接收账号类型: 0-个人(对私) 1-商户(对公)")
    private Byte accType;

    @Schema(description = "分账接收账号")
    private String accNo;

    @Schema(description = "分账接收账号名称")
    private String accName;

    @Schema(description = "分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等")
    private String relationType;

    @Schema(description = "当选择自定义时，需要录入该字段。 否则为对应的名称")
    private String relationTypeName;

    @Schema(description = "分账比例")
    private BigDecimal divisionProfit;

    @Schema(description = "分账状态（本系统状态，并不调用上游关联关系）: 1-正常分账, 0-暂停分账")
    private Byte state;

    @Schema(description = "上游绑定返回信息，一般用作查询账号异常时的记录")
    private String channelBindResult;

    @Schema(description = "渠道特殊信息")
    private String channelExtInfo;

    @Schema(description = "绑定成功时间")
    private LocalDateTime bindSuccessTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
