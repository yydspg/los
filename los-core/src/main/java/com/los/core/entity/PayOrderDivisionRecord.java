package com.los.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/*
 * <p>
 * 分账记录表
 * </p>
 *
 * @author [paul]
 */
@Schema(description= "分账记录表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_order_division_record")
public class PayOrderDivisionRecord implements Serializable {

    public static final byte STATE_WAIT = 0; // 待分账
    public static final byte STATE_SUCCESS = 1; // 分账成功（明确成功）
    public static final byte STATE_FAIL = 2; // 分账失败（明确失败）
    public static final byte STATE_ACCEPT = 3; // 分账已受理（上游受理）
    @Serial
    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<PayOrderDivisionRecord> gw(){
        return new LambdaQueryWrapper<>();
    }

    /*
     * 分账记录ID
     */
    @Schema(description = "分账记录ID")
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    /*
     * 商户号
     */
    @Schema(description = "商户号")
    private String mchNo;

    /*
     * 服务商号
     */
    @Schema(description = "服务商号")
    private String isvNo;

    /*
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;

    /*
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String mchName;

    /*
     * 类型: 1-普通商户, 2-特约商户(服务商模式)
     */
    @Schema(description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    private Byte mchType;

    /*
     * 支付接口代码
     */
    @Schema(description = "支付接口代码")
    private String ifCode;

    /*
     * 系统支付订单号
     */
    @Schema(description = "系统支付订单号")
    private String payOrderId;

    /*
     * 支付订单渠道支付订单号
     */
    @Schema(description = "支付订单渠道支付订单号")
    private String payOrderChannelOrderNo;

    /*
     * 订单金额,单位分
     */
    @Schema(description = "订单金额,单位分")
    private Long payOrderAmount;

    /*
     * 订单实际分账金额, 单位：分（订单金额 - 商户手续费 - 已退款金额）
     */
    @Schema(description = "订单实际分账金额, 单位：分（订单金额 - 商户手续费 - 已退款金额）")
    private Long payOrderDivisionAmount;

    /*
     * 系统分账批次号
     */
    /*
    在后端支付系统中，**系统分账批次号（BatchOrderId或Batch No.）**是用来标识一组分账操作集合的唯一编号。
    在进行分账处理时，特别是在大型电商平台、金融服务系统等场景中，往往涉及到多个收款方对一笔或多笔交易款项进行分配的过程。
    当一笔交易产生后，系统可能需要按照预先设定的规则，将这笔交易的收入按照一定比例分配给不同的商户、代理商、平台方或其他参与方。

    系统分账批次号就是针对一次批量分账操作产生的标识，
    它关联了这次分账的所有详细信息，包括但不限于参与分账的各方、各自应得份额、原始交易信息等。
    通过批次号，可以追踪和管理整个分账批次的生命周期，包括创建、执行、查询、对账、清算等各个环节。

    举个例子，假设一家电商平台上有一个商家完成了订单交易，该笔订单的总收入需要分给商家自身、平台抽成以及物流合作伙伴。
    系统在处理这笔交易的分账时，会创建一个分账批次，并为其生成一个唯一的批次号，然后在这个批次下记录每一个分账明细，最终根据批次号统一进行财务操作和核对。
     */
    // TODO: 2024/3/4 此处记录了同一批分账的唯一标识
    @Schema(description = "系统分账批次号")
    private String batchOrderId;

    /*
     * 上游分账批次号
     */
    @Schema(description = "上游分账批次号")
    private String channelBatchOrderId;

    /*
     * 状态: 0-待分账 1-分账成功, 2-分账失败
     */
    @Schema(description = "状态: 0-待分账 1-分账成功, 2-分账失败")
    private Byte state;

    /*
     * 上游返回数据包
     */
    @Schema(description = "上游返回数据包")
    private String channelRespResult;

    /*
     * 账号快照》 分账接收者ID
     */
    @Schema(description = "账号快照 > 分账接收者ID")
    private Long receiverId;

    /*
     * 账号快照》 组ID（便于商户接口使用）
     */
    @Schema(description = "账号快照 >  组ID（便于商户接口使用）")
    private Long receiverGroupId;

    /*
     * 账号快照》 分账接收者别名
     */
    @Schema(description = "账号快照 >  分账接收者别名")
    private String receiverAlias;

    /*
     * 账号快照》 分账接收账号类型: 0-个人 1-商户
     */
    @Schema(description = "账号快照 >  分账接收账号类型: 0-个人 1-商户")
    private Byte accType;

    /*
     * 账号快照》 分账接收账号
     */
    @Schema(description = "账号快照 > 分账接收账号")
    private String accNo;

    /*
     * 账号快照》 分账接收账号名称
     */
    @Schema(description = "账号快照 > 分账接收账号名称")
    private String accName;

    /*
     * 账号快照》 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等
     */
    @Schema(description = "账号快照 > 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等")
    private String relationType;

    /*
     * 账号快照》 当选择自定义时，需要录入该字段。 否则为对应的名称
     */
    @Schema(description = "账号快照 >  当选择自定义时，需要录入该字段。 否则为对应的名称")
    private String relationTypeName;

    /*
     * 账号快照》 配置的实际分账比例
     */
    @Schema(description = "账号快照 >  配置的实际分账比例")
    private BigDecimal divisionProfit;

    /*
     * 计算该接收方的分账金额,单位分
     */
    @Schema(description = "计算该接收方的分账金额,单位分")
    private Long calDivisionAmount;

    /*
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createdAt;

    /*
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updatedAt;


}
