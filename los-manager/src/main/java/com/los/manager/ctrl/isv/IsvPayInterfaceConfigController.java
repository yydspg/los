package com.los.manager.ctrl.isv;

import com.los.components.mq.vender.IMQSender;
import com.los.core.constants.CS;
import com.los.core.entity.PayInterfaceDefine;
import com.los.core.model.ApiRes;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.PayInterfaceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "服务商支付接口管理")
@RestController
@RequestMapping("/api/isv/payConfigs")
public class IsvPayInterfaceConfigController  extends CommonCtrl {
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private IMQSender mqSender;
    @Operation(summary = "查询服务商支付接口配置列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_ISV_PAY_CONFIG_LIST')")
    @RequestMapping(value = "",method = RequestMethod.GET)
    public ApiRes list() {
        String isvNo = super.getValStringRequired("isvNo");
        List<PayInterfaceDefine> payInterfaceDefines = payInterfaceConfigService.selectAllPayIfConfigListByIsvNo(CS.INFO_TYPE_ISV, isvNo);
        return ApiRes.success(payInterfaceDefines);
    }

    @Operation(summary = "根据[服务商号]、[接口类型]获取商户参数配置")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_ISV_PAY_CONFIG_VIEW')")
    @GetMapping("/{isvNo}/{ifCode}")
    public ApiRes getByMchNo(@PathVariable(value = "isvNo") String isvNo, @PathVariable(value = "ifCode") String ifCode) {
        return null;
    }
}
