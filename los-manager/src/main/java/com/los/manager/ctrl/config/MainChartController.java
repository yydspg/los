package com.los.manager.ctrl.config;

import com.los.core.model.ApiRes;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "主页统计")
@Slf4j
@RestController
@RequestMapping("/api/mainChart")
public class MainChartController extends CommonCtrl {

    @Autowired private PayOrderService payOrderService;
    @Operation(summary = "周交易金额统计")
    @Parameters(@Parameter(name = "iToken",description = "用户身份凭证",required = true))
    @PreAuthorize("hasAuthority('ENT_C_MAIN_PAY_AMOUNT_WEEK')")
    @GetMapping("/payAmountWeek")
    public ApiRes payAmountWeek() {
        // TODO 2024/3/30 : 升级此接口,参考阿里云
        return ApiRes.success(payOrderService.mainPageWeekCount(null));
    }

}
