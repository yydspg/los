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
    * 订单接口数据快照
    * </p>
*
* @author paul
* @since 2024-01-31
*/
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_order_snapshot")
@Schema(name = "OrderSnapshot", description = "$!{table.comment}")
public class OrderSnapshot extends BaseModel {
    @Serial
    private static final long serialVersionUID = 1L;

    public static  LambdaQueryWrapper<OrderSnapshot> gw() {return new LambdaQueryWrapper<>(); }


    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "订单类型: 1-支付, 2-退款")
    private Byte orderType;

    @Schema(description = "下游请求数据")
    private String mchReqData;

    @Schema(description = "下游请求时间")
    private LocalDateTime mchReqTime;

    @Schema(description = "向下游响应数据")
    private String mchRespData;

    @Schema(description = "向下游响应时间")
    private LocalDateTime mchRespTime;

    @Schema(description = "向上游请求数据")
    private String channelReqData;

    @Schema(description = "向上游请求时间")
    private LocalDateTime channelReqTime;

    @Schema(description = "上游响应数据")
    private String channelRespData;

    @Schema(description = "上游响应时间")
    private LocalDateTime channelRespTime;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;
}
