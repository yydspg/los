package com.los.manager.ctrl.merchant;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.MchApp;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.MchAppService;
import com.los.service.MchInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "商户应用管理")
@RestController
@RequestMapping("/api/mchApps")
public class MchAppController extends CommonCtrl {
    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private IMQSender mqSender;

    @Operation(summary = "查询应用列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "mchNo", description = "商户号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "appName", description = "应用名称"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_APP_LIST')")
    @RequestMapping(value = "",method = RequestMethod.GET)
    public ApiPageRes<MchApp> page() {
        MchApp mchApp = super.getObject(MchApp.class);

        IPage<MchApp> pages = mchAppService.selectPage(getIPage(), mchApp);
        return ApiPageRes.pages(pages);
    }


    @Operation(summary = "查询应用详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID")
    })
    @PreAuthorize("hasAnyAuthority('ENT_MCH_APP_VIEW', 'ENT_MCH_APP_EDIT')")
    @RequestMapping(value = "/{appId}",method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("appId") String appId) {
        MchApp mchApp = mchAppService.selectById(appId);
        if (mchApp == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }

        return ApiRes.success(mchApp);
    }


    @Operation(summary = "更新应用信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID", required = true),
            @Parameter(name = "appName", description = "应用名称", required = true),
            @Parameter(name = "appSecret", description = "应用私钥", required = true),
            @Parameter(name = "mchNo", description = "商户号", required = true),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "更新应用信息")
    @RequestMapping(value = "/{appId}",method = RequestMethod.POST)
    public ApiRes update(@PathVariable("appId") String appId) {
        MchApp mchApp = getObject(MchApp.class);
        mchApp.setAppId(appId);
        boolean result = mchAppService.updateById(mchApp);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        // 推送修改应用消息
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP, null, mchApp.getMchNo(), appId));
        return ApiRes.success();
    }


    @Operation(summary = "删除应用")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appId", description = "应用ID")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_APP_DEL')")
    @MethodLog(remark = "删除应用")
    @RequestMapping(value = "/{appId}",method = RequestMethod.GET)
    public ApiRes delete(@PathVariable("appId") String appId) {

        MchApp mchApp = mchAppService.getById(appId);
        mchAppService.removeByAppId(appId);

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP, null, mchApp.getMchNo(), appId));
        return ApiRes.success();
    }

    @Operation(summary = "新增应用")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "appName", description = "应用名称", required = true),
            @Parameter(name = "appSecret", description = "应用私钥", required = true),
            @Parameter(name = "mchNo", description = "商户号", required = true),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_APP_ADD')")
    @MethodLog(remark = "新建应用")
    @PostMapping
    public ApiRes add() {
        MchApp mchApp = super.getObject(MchApp.class);
        // TODO 2024/3/31 : id生成
        mchApp.setAppId(IdUtil.objectId());

        if(mchInfoService.getById(mchApp.getMchNo()) == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }

        boolean result = mchAppService.save(mchApp);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.success();
    }
}
