package com.los.manager.ctrl.isv;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.IsvInfo;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.DateKit;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.IsvInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author paul 2024/3/25
 */
@Tag(name = "服务商管理")
@RestController
@RequestMapping("/api/isvInfo")
public class IsvInfoController extends CommonCtrl {

    @Autowired private IsvInfoService isvInfoService;
    @Autowired private IMQSender mqSender;
    @Operation(summary = "服务商列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数（-1时查全部数据）"),
            @Parameter(name = "isvNo", description = "服务商编号"),
            @Parameter(name = "isvName", description = "服务商名称"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<IsvInfo> list() {
        IsvInfo isvInfo = super.getObject(IsvInfo.class);
        LambdaQueryWrapper<IsvInfo> condition = IsvInfo.gw();
        if(StringKit.isNotEmpty(isvInfo.getIsvNo())) {
            condition.eq(IsvInfo::getIsvNo,isvInfo.getIsvNo());
        }
        if(StringKit.isNotEmpty(isvInfo.getIsvName())) {
            condition.eq(IsvInfo::getIsvName,isvInfo.getIsvName());
        }
        if(isvInfo.getState() != null) {
            condition.eq(IsvInfo::getState,isvInfo.getState());
        }
        condition.orderByDesc(IsvInfo::getCreatedAt);
        IPage<IsvInfo> pages= isvInfoService.page(super.getIPage(true), condition);
        
        return ApiPageRes.pages(pages);
    }

    @Operation(summary = "新增服务商")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号"),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "isvShortName", description = "服务商简称"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_ADD')")
    @MethodLog(remark = "新增服务商")
    @RequestMapping(value="", method = RequestMethod.POST)
    public ApiRes add() {
        IsvInfo isvInfo = super.getObject(IsvInfo.class);
        // TODO 2024/3/31 : id 生成器这么草率吗
        String isvNo = "V" + DateKit.currentTimeMillis();
        isvInfo.setIsvNo(isvNo);
        isvInfo.setCreatedUid(getCurrentUser().getSysUser().getSysUserId());
        isvInfo.setCreatedBy(getCurrentUser().getSysUser().getRealname());
        boolean result = isvInfoService.save(isvInfo);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.success();
    }
    @Operation(summary = "更新服务商")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号"),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "isvShortName", description = "服务商简称"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_EDIT')")
    @MethodLog(remark = "更新服务商信息")
    @RequestMapping(value="/{isvNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("isvNo") String isvNo) {
        IsvInfo isvInfo = getObject(IsvInfo.class);
        isvInfo.setIsvNo(isvNo);
        boolean result = isvInfoService.updateById(isvInfo);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO, isvNo, null, null));

        return ApiRes.success();
    }
    @Operation(summary = "查看服务商")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_ISV_INFO_VIEW', 'ENT_ISV_INFO_EDIT')")
    @RequestMapping(value="/{isvNo}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("isvNo") String isvNo) {
        IsvInfo isvInfo = isvInfoService.getById(isvNo);
        if (isvInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }
        return ApiRes.success(isvInfo);
    }
    @Operation(summary = "删除服务商")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_DEL')")
    @MethodLog(remark = "删除服务商")
    @RequestMapping(value="/{isvNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("isvNo") String isvNo) {
        isvInfoService.removeByIsvNo(isvNo);

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO, isvNo, null, null));
        return ApiRes.success();
    }
}
