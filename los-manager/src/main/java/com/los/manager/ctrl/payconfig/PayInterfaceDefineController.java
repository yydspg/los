package com.los.manager.ctrl.payconfig;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayInterfaceDefine;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.JSONKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.PayInterfaceConfigService;
import com.los.service.PayInterfaceDefineService;
import com.los.service.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author paul 2024/3/25
 */

@Tag(name = "支付接口配置")
@RestController
@RequestMapping("api/payIfDefines")
public class PayInterfaceDefineController extends CommonCtrl {

    @Resource private PayInterfaceDefineService payInterfaceDefineService;
    @Resource private PayOrderService payOrderService;
    @Resource private PayInterfaceConfigService payInterfaceConfigService;

    @Operation(summary = "支付接口--列表")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_LIST')")
    @RequestMapping(value = "",method = RequestMethod.POST)
    public ApiRes list() {
        return ApiRes.success(payInterfaceDefineService.list(PayInterfaceDefine.gw()
                .orderByDesc(PayInterfaceDefine::getCreatedAt)));
    }
    @Operation(summary = "支付接口--详情")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "ifCode", description = " 接口类型代码", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_PC_IF_DEFINE_VIEW', 'ENT_PC_IF_DEFINE_EDIT')")
    @RequestMapping(value = "/{ifCode}",method = RequestMethod.GET)
    public ApiRes detail(@PathVariable(value = "ifCode") String ifCode) {
        return ApiRes.success(payInterfaceDefineService.getById(ifCode));
    }
    @Operation(summary = "支付接口--新增")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "ifCode", description = " 接口类型代码", required = true),
            @Parameter(name = "configPageType", description = " 支付参数配置页面类型:1-JSON渲染,2-自定义", required = true),
            @Parameter(name = "icon", description = " 页面展示：卡片-图标"),
            @Parameter(name = "bgColor", description = " 页面展示：卡片-背景色"),
            @Parameter(name = "ifName", description = " 接口名称", required = true),
            @Parameter(name = "isIsvMode", description = "是否支持服务商子商户模式: 0-不支持, 1-支持", required = true),
            @Parameter(name = "isMchMode", description = "是否支持普通商户模式: 0-不支持, 1-支持", required = true),
            @Parameter(name = "isvParams", description = "ISV接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @Parameter(name = "isvsubMchParams", description = "特约商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @Parameter(name = "normalMchParams", description = "普通商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @Parameter(name = "remark", description = "备注", required = true),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用", required = true),
            @Parameter(name = "wayCodeStrs", description = "接口类型代码（若干个接口类型代码用英文逗号拼接起来）", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_ADD')")
    @RequestMapping(value = "",method = RequestMethod.POST)
    @MethodLog(remark = "新增支付接口")
    public ApiRes add() {
        PayInterfaceDefine payInterfaceDefine = super.getObject(PayInterfaceDefine.class);
        //[{"wayCode": "ALI_APP"}, {"wayCode": "ALI_BAR"}, {"wayCode": "ALI_JSAPI"}]
        String[] wayCodes = super.getValStringRequired("wayCodeStrs").split(",");
        JSONArray jsonArray = JSONKit.convert("wayCode",wayCodes);
        payInterfaceDefine.setWayCodes(jsonArray);
        return payInterfaceDefineService.save(payInterfaceDefine)? ApiRes.success(): ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }
    @Operation(summary = "支付接口--更新")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "ifCode", description = "接口类型代码", required = true),
            @Parameter(name = "configPageType", description = "支付参数配置页面类型:1-JSON渲染,2-自定义", required = true),
            @Parameter(name = "icon", description = "页面展示：卡片-图标"),
            @Parameter(name = "bgColor", description = "页面展示：卡片-背景色"),
            @Parameter(name = "ifName", description = "接口名称", required = true),
            @Parameter(name = "isIsvMode", description = "是否支持服务商子商户模式: 0-不支持, 1-支持", required = true),
            @Parameter(name = "isMchMode", description = "是否支持普通商户模式: 0-不支持, 1-支持", required = true),
            @Parameter(name = "isvParams", description = "ISV接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @Parameter(name = "isvsubMchParams", description = "特约商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @Parameter(name = "normalMchParams", description = "普通商户接口配置定义描述,[{},{}]，当configPageType为1时必填"),
            @Parameter(name = "remark", description = "备注", required = true),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用", required = true),
            @Parameter(name = "wayCodeStrs", description = "接口类型代码（若干个接口类型代码用英文逗号拼接起来）", required = true),
            @Parameter(name = "wayCodes", description = "接口类型代码列表")
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_EDIT')")
    @RequestMapping(value = "/{ifCode}",method = RequestMethod.POST)
    @MethodLog(remark = "更新支付接口")
    public ApiRes update(@PathVariable(value = "ifCode") String ifCode) {
        PayInterfaceDefine updateRecord = super.getObject(PayInterfaceDefine.class);
        updateRecord.setIfCode(ifCode);
        //[{"wayCode": "ALI_APP"}, {"wayCode": "ALI_BAR"}, {"wayCode": "ALI_JSAPI"}]
        String[] wayCodes = super.getValStringRequired("wayCodeStrs").split(",");
        JSONArray updateJsonArray = JSONKit.convert("wayCode",wayCodes);
        updateRecord.setWayCodes(updateJsonArray);
        return payInterfaceDefineService.updateById(updateRecord)? ApiRes.success(): ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }
    @Operation(summary = "支付接口--删除")
    @Parameters({
            @Parameter(name = "iToken",description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "ifCode", description = "接口类型代码", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_DEL')")
    @RequestMapping(value = "/{ifCode}",method = RequestMethod.GET)
    @MethodLog(remark = "删除支付接口")
    public ApiRes del(@PathVariable(value = "ifCode") String ifCode) {
        // 校验该支付方式是否有服务商或商户配置参数或者已有订单
        if (payInterfaceConfigService.count(PayInterfaceConfig.gw().eq(PayInterfaceConfig::getIfCode, ifCode)) > 0
                || payOrderService.count(PayOrder.gw().eq(PayOrder::getIfCode, ifCode)) > 0) {
            throw new BizException("该支付接口已有服务商或商户配置参数或已发生交易，无法删除！");
        }
        return payInterfaceDefineService.removeById(ifCode)? ApiRes.success(): ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
    }
}
