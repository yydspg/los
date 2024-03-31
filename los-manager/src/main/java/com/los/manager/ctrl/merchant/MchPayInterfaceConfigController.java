package com.los.manager.ctrl.merchant;

import com.los.components.mq.vender.IMQSender;
import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayInterfaceDefine;
import com.los.core.model.ApiRes;
import com.los.core.model.params.NormalMchParams;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.MchAppService;
import com.los.service.MchInfoService;
import com.los.service.PayInterfaceConfigService;
import com.los.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author paul 2024/3/25
 */
@Tag(name="商户支付接口管理")
@RestController
@RequestMapping("/api/mch/payConfigs")
public class MchPayInterfaceConfigController extends CommonCtrl {

    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private MchAppService mchAppService;
    @Autowired private IMQSender mqSender;
    @Autowired private MchInfoService mchInfoService;
    @Autowired private SysConfigService sysConfigService;

    @Operation(summary = "查询应用详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID",required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_LIST')")
    @RequestMapping(value = "",method = RequestMethod.GET)
    public ApiRes list() {
        List<PayInterfaceDefine> list = payInterfaceConfigService.selectAllPayIfConfigListByAppId(getValStringRequired("appId"));
        return ApiRes.success(list);
    }

    @Operation(summary = "查询应用详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID",required = true),
            @Parameter(name = "ifCode", description = "接口类型代码",required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_VIEW')")
    @RequestMapping(value = "/{appId}/{ifCode}",method = RequestMethod.GET)
    public ApiRes getByAppIdAndIfCode(@PathVariable(value = "appId") String appId,@PathVariable(value = "ifCode") String ifCode) {
        PayInterfaceConfig payInterfaceConfig = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, appId, ifCode);
        // 数据处理
        if(payInterfaceConfig != null) {
            // 费率转化为百分比数值
            if(payInterfaceConfig.getIfRate() != null) {
                payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().multiply(new BigDecimal("100")));
            }
            // 脱敏
            // TODO 2024/3/31 : 此处数据变更结果
            if(StringKit.isNotBlank(payInterfaceConfig.getIfParams())) {
                MchApp mchApp = mchAppService.getById(appId);
                MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());

                // 普通商户支付参数执行数据脱敏
                if(mchInfo.getType() == CS.MCH_TYPE_NORMAL){
                    NormalMchParams mchParams = NormalMchParams.factory(ifCode, payInterfaceConfig.getIfParams());
                    if(mchParams != null) {
                        payInterfaceConfig.setIfParams(mchParams.deSenData());
                    }
                }
            }
        }
        return ApiRes.success(payInterfaceConfig);
    }
    @Operation(summary = "更新应用支付参数")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "infoId", description = "应用AppID",required = true),
            @Parameter(name = "ifCode", description = "接口类型代码",required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_CONFIG_VIEW')")
    @RequestMapping(value = "/{appId}/{ifCode}",method = RequestMethod.POST)
    public ApiRes saveOrUpdate() {
        return null;
    }
}
