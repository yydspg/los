package com.los.manager.ctrl.payconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.MchPayPassage;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayWay;
import com.los.core.exception.BizException;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.manager.ctrl.CommonCtrl;
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
import org.springframework.web.bind.annotation.*;

/**
 * @author paul 2024/3/25
 */

@Tag(name = "支付方式配置")
@RestController
@RequestMapping("api/payWays")
public class PayWayController extends CommonCtrl {

    @Resource PayWayService payWayService;
    @Resource MchPayPassageService mchPayPassageService;
    @Resource PayOrderService payOrderService;

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



    @Operation(summary = "支付方式--详情")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "wayCode", description = " 支付方式代码", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_WAY_VIEW', 'ENT_PC_WAY_EDIT')")
    @GetMapping("/{wayCode}")
    public ApiRes detail(@PathVariable("wayCode") String wayCode) {
        return ApiRes.success(payWayService.getById(wayCode));
    }


    @Operation(summary = "支付方式--新增")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "wayCode", description = " 支付方式代码", required = true),
            @Parameter(name = "wayName", description = " 支付方式名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_WAY_ADD')")
    @PostMapping
    @MethodLog(remark = "新增支付方式")
    public ApiRes add() {
        PayWay payWay = getObject(PayWay.class);

        if (payWayService.count(PayWay.gw().eq(PayWay::getWayCode, payWay.getWayCode())) > 0) {
            throw new BizException("支付方式代码已存在");
        }
        payWay.setWayCode(payWay.getWayCode().toUpperCase());
        return  payWayService.save(payWay) ?
                ApiRes.success():
                ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }


    @Operation(summary = "支付方式--更新")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "wayCode", description = "支付方式代码", required = true),
            @Parameter(name = "wayName", description = "支付方式名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @PutMapping("/{wayCode}")
    @MethodLog(remark = "更新支付方式")
    public ApiRes update(@PathVariable("wayCode") String wayCode) {
        PayWay payWay = getObject(PayWay.class);
        payWay.setWayCode(wayCode);
        return payWayService.updateById(payWay)?
                ApiRes.success():
                ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }


    @Operation(summary = "支付方式--删除")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "wayCode", description = "支付方式代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_WAY_DEL')")
    @DeleteMapping("/{wayCode}")
    @MethodLog(remark = "删除支付方式")
    public ApiRes delete(@PathVariable("wayCode") String wayCode) {

        // 校验该支付方式是否有商户已配置通道或者已有订单
        if (mchPayPassageService.count(MchPayPassage.gw().eq(MchPayPassage::getWayCode, wayCode)) > 0
                || payOrderService.count(PayOrder.gw().eq(PayOrder::getWayCode, wayCode)) > 0) {
            throw new BizException("该支付方式已有商户配置通道或已发生交易，无法删除！");
        }
        return payWayService.removeById(wayCode) ?
                ApiRes.success() :
                ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
    }
}
