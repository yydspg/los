package com.los.merchant.ctrl.payconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.entity.PayWay;
import com.los.core.model.ApiPageRes;
import com.los.merchant.ctrl.CommonCtrl;
import com.los.service.MchPayPassageService;
import com.los.service.PayOrderService;
import com.los.service.PayWayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "支付方式配置")
@RestController
@RequestMapping("api/payWays")
public class PayWayController extends CommonCtrl {
    @Resource
    PayWayService payWayService;
    @Resource
    MchPayPassageService mchPayPassageService;
    @Resource
    PayOrderService payOrderService;

    @Operation(summary = "支付方式--列表")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = " 分页页码"),
            @Parameter(name = "pageSize", description = " 分页条数（-1时查全部数据）"),
            @Parameter(name = "wayCode", description = " 支付方式代码"),
            @Parameter(name = "wayName", description = " 支付方式名称")
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_WAY_LIST', 'ENT_PAY_ORDER_SEARCH_PAY_WAY')")
    @GetMapping
    public ApiPageRes<PayWay> list() {

        PayWay queryObject = getObject(PayWay.class);

        LambdaQueryWrapper<PayWay> condition = PayWay.gw();
        if(StringUtils.isNotEmpty(queryObject.getWayCode())){
            condition.like(PayWay::getWayCode, queryObject.getWayCode());
        }
        if(StringUtils.isNotEmpty(queryObject.getWayName())){
            condition.like(PayWay::getWayName, queryObject.getWayName());
        }
        condition.orderByAsc(PayWay::getWayCode);

        IPage<PayWay> pages = payWayService.page(getIPage(true), condition);

        return ApiPageRes.pages(pages);
    }
}
