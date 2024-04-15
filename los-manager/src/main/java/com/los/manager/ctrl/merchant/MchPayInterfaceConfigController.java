package com.los.manager.ctrl.merchant;

import com.alibaba.fastjson2.JSONObject;
import com.los.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayInterfaceConfig;
import com.los.core.entity.PayInterfaceDefine;
import com.los.core.model.ApiRes;
import com.los.core.model.DBApplicationConfig;
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
        String infoId = getValStringRequired("infoId");
        String ifCode = getValStringRequired("ifCode");

        MchApp mchApp = mchAppService.getById(infoId);
        if (mchApp == null || mchApp.getState() != CS.YES) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }

        PayInterfaceConfig payInterfaceConfig = getObject(PayInterfaceConfig.class);
        payInterfaceConfig.setInfoType(CS.INFO_TYPE_MCH_APP);
        payInterfaceConfig.setInfoId(infoId);

        // 存入真实费率
        if (payInterfaceConfig.getIfRate() != null) {
            payInterfaceConfig.setIfRate(payInterfaceConfig.getIfRate().divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP));
        }

        //添加更新者信息
        Long userId = getCurrentUser().getSysUser().getSysUserId();
        String realName = getCurrentUser().getSysUser().getRealname();
        payInterfaceConfig.setUpdatedUid(userId);
        payInterfaceConfig.setUpdatedBy(realName);

        //根据 商户号、接口类型 获取商户参数配置
        PayInterfaceConfig dbRecoed = payInterfaceConfigService.getByInfoIdAndIfCode(CS.INFO_TYPE_MCH_APP, infoId, ifCode);
        //若配置存在，为saveOrUpdate添加ID，第一次配置添加创建者
        if (dbRecoed != null) {
            payInterfaceConfig.setId(dbRecoed.getId());
            // 合并支付参数
            payInterfaceConfig.setIfParams(StringKit.merge(dbRecoed.getIfParams(), payInterfaceConfig.getIfParams()));
        }else {
            payInterfaceConfig.setCreatedUid(userId);
            payInterfaceConfig.setCreatedBy(realName);
        }

        boolean result = payInterfaceConfigService.saveOrUpdate(payInterfaceConfig);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "configureFail");
        }

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP, null, mchApp.getMchNo(), infoId));

        return ApiRes.success();
    }
    @Operation(summary = "查询商户授权URL")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchAppId", description = "应用ID", required = true)
    })
    @GetMapping("/alipayIsvsubMchAuthUrls/{mchAppId}")
    public ApiRes queryAlipayIsvSubMchAuthUrl(@PathVariable String mchAppId) {

        MchApp mchApp = mchAppService.getById(mchAppId);
        MchInfo mchInfo = mchInfoService.getById(mchApp.getMchNo());
        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();
        String authUrl = dbApplicationConfig.genAlipayIsvsubMchAuthUrl(mchInfo.getIsvNo(), mchAppId);
        String authQrImgUrl = dbApplicationConfig.genScanImgUrl(authUrl);

        JSONObject result = new JSONObject();
        result.put("authUrl", authUrl);
        result.put("authQrImgUrl", authQrImgUrl);
        return ApiRes.success(result);
    }

}
