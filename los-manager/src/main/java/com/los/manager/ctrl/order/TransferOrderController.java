package com.los.manager.ctrl.order;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.entity.TransferOrder;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.TransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/25
 */

@Tag(name = "订单管理（转账类）")
@RestController
@RequestMapping("/api/transferOrders")
public class TransferOrderController extends CommonCtrl {

    @Resource
    private TransferOrderService transferOrderService;

    /**
     * list
     **/
    @Operation(summary = "转账订单信息列表")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = " 分页页码"),
            @Parameter(name = "pageSize", description = " 分页条数"),
            @Parameter(name = "createdStart", description = " 日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = " 日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "mchNo", description = " 商户号"),
            @Parameter(name = "unionOrderId", description = " 转账/商户/渠道订单号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "state", description = "支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭")
    })
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_LIST')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ApiPageRes<TransferOrder> list() {

        TransferOrder transferOrder = getObject(TransferOrder.class);
        JSONObject paramJSON = super.getParams();
        LambdaQueryWrapper<TransferOrder> wrapper = TransferOrder.gw();
        IPage<TransferOrder> pages = transferOrderService.pageList(getIPage(), wrapper, transferOrder, paramJSON);

        return ApiPageRes.pages(pages);
    }

    /**
     * detail
     **/
    @Operation(summary = "转账订单信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description = "转账订单号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_TRANSFER_ORDER_VIEW')")
    @RequestMapping(value = "/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") String transferId) {
        return ApiRes.success(transferOrderService.getById(transferId));
    }
}
