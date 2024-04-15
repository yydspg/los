package com.los.manager.ctrl.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.los.components.mq.model.ResetAppConfigMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.SysConfig;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "系统配置管理")
@Slf4j
@RestController
@RequestMapping("/api/sysConfigs")
public class SysConfigController extends CommonCtrl {
    @Autowired private SysConfigService sysConfigService;
    @Autowired private IMQSender mqSender;

    @Operation(summary = "")
    @Parameters({
            @Parameter(name = "iToken",description = "用户身份凭证",required = true,in = ParameterIn.HEADER),
            @Parameter(name = "groupKey",description = "分组key",required = true),
    })
    @PreAuthorize("hasAuthority('ENT_SYS_CONFIG_INFO')")
    @GetMapping(value="/{groupKey}")
    public ApiRes getConfigs(@PathVariable("groupKey") String groupKey) {
        LambdaQueryWrapper<SysConfig> condition = SysConfig.gw();
        condition.orderByAsc(SysConfig::getSortNum);
        if(StringKit.isNotEmpty(groupKey)) {
            condition.eq(SysConfig::getGroupKey,groupKey);
        }
        return ApiRes.success(sysConfigService.list(condition));
    }

    @Operation(summary = "修改分组下的系统配置")
    @Parameters({
            @Parameter(name = "iToken",description = "用户身份凭证",required = true,in = ParameterIn.HEADER),
            @Parameter(name = "groupKey" ,description = "分组key",required = true),
            @Parameter(name = "mchSiteUrl",description = "商户平台网址(不包含结尾/)",required = true),
            @Parameter(name = "mgrSiteUrl",description = "运营平台网址(不包含结尾/)",required = true),
            @Parameter(name = "ossPublicSiteUrl",description = "公共oss访问地址(不包含结尾/)",required = true),
            @Parameter(name = "paySiteUrl",description = "支付网关地址(不包含结尾/)",required = true),
    })
    @PreAuthorize("hasAuthority('ENT_SYS_CONFIG_EDIT')")
    @MethodLog(remark = "系统配置修改")
    @RequestMapping(value="/{groupKey}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("groupKey") String groupKey) {
        JSONObject reqParamJSON = super.getParams();
        // TODO 2024/3/31 : 将对于jsonObject的操作赋予对象
        Map<String, String> updateMap = reqParamJSON.toJavaObject(Map.class);
        if(sysConfigService.updateByConfigKey(updateMap) <= 0) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR,"updateFail");
        }
        // async
        this.updateSysConfigMQ(groupKey);
        return ApiRes.success();
    }
    @Async
    public void updateSysConfigMQ(String groupKey){
        mqSender.send(ResetAppConfigMQ.build(groupKey));
    }

}
