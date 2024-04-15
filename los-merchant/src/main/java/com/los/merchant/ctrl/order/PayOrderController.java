package com.los.merchant.ctrl.order;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.aop.MethodLog;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayWay;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.merchant.ctrl.CommonCtrl;
import com.los.service.MchAppService;
import com.los.service.PayOrderService;
import com.los.service.PayWayService;
import com.los.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author paul 2024/3/25
 */

@Tag(name = "订单管理（支付类）")
@RestController
@RequestMapping("/api/payOrder")
public class PayOrderController extends CommonCtrl {

    @Resource private PayOrderService payOrderService;
    @Resource private PayWayService payWayService;
    @Resource private SysConfigService sysConfigService;
    @Resource private MchAppService mchAppService;

    @Operation(summary = "支付订单信息列表")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = " 分页页码"),
            @Parameter(name = "pageSize", description = " 分页条数"),
            @Parameter(name = "createdStart", description = " 日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = " 日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "mchNo", description = " 商户号"),
            @Parameter(name = "unionOrderId", description = " 支付/商户/渠道订单号"),
            @Parameter(name = "isvNo", description = " 服务商号"),
            @Parameter(name = "appId", description = " 应用ID"),
            @Parameter(name = "wayCode", description = " 支付方式代码"),
            @Parameter(name = "state", description = " 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭"),
            @Parameter(name = "notifyState", description = "向下游回调状态, 0-未发送,  1-已发送"),
            @Parameter(name = "divisionState", description = "0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)")
    })
    @PreAuthorize("hasAuthority('ENT_ORDER_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<PayOrder> list() {

        PayOrder payOrder = getObject(PayOrder.class);
        JSONObject paramJSON = getParams();
        LambdaQueryWrapper<PayOrder> wrapper = PayOrder.gw();

        IPage<PayOrder> pages = payOrderService.listByPage(getIPage(), payOrder, paramJSON, wrapper);
        // 得到所有支付方式
        Map<String, String> payWayNameMap = new HashMap<>();
        List<PayWay> payWayList = payWayService.list();
        for (PayWay payWay:payWayList) {
            payWayNameMap.put(payWay.getWayCode(), payWay.getWayName());
        }
        for (PayOrder order:pages.getRecords()) {
            // 存入支付方式名称
            if (StringKit.isNotEmpty(payWayNameMap.get(order.getWayCode())))
                order.addExt("wayName", payWayNameMap.get(order.getWayCode()));
            else order.addExt("wayName", order.getWayCode());
        }
        return ApiPageRes.pages(pages);
    }
    @Operation(summary = "支付订单信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "payOrderId", description = "支付订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @RequestMapping(value="/{payOrderId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("payOrderId") String payOrderId) {
        return ApiRes.success(payOrderService.getById(payOrderId));
    }

    @Operation(summary = "发起订单退款")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "payOrderId", description = "支付订单号", required = true),
            @Parameter(name = "refundAmount", description = "退款金额", required = true),
            @Parameter(name = "refundReason", description = "退款原因", required = true)
    })
    @MethodLog(remark = "发起订单退款")
    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_REFUND')")
    @PostMapping("/refunds/{payOrderId}")
    public ApiRes refund(@PathVariable("payOrderId") String payOrderId) {
        // TODO 2024/4/3 : 实现 Los支付系统的sdk
        return null;
    }

}
