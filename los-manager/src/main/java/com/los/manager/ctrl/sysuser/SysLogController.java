package com.los.manager.ctrl.sysuser;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.SysLog;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "系统管理(系统日志)")
@RestController
@RequestMapping("api/sysLog")
public class SysLogController extends CommonCtrl {
    @Resource private SysLogService sysLogService;
    @Operation(summary = "系统日志列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "userId", description = "系统用户ID"),
            @Parameter(name = "userName", description = "用户姓名"),
            @Parameter(name = "sysType", description = "所属系统： MGR-运营平台, MCH-商户中心")
    })
    @PreAuthorize("hasAuthority('ENT_LOG_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<SysLog> page() {
        SysLog sysLog = super.getObject(SysLog.class);
        JSONObject paramJSON = super.getParams();

        LambdaQueryWrapper<SysLog> condition = SysLog.gw();
        if(sysLog.getUserId() != null) condition.eq(SysLog::getUserId,sysLog.getUserId());
        if(StringKit.isNotEmpty(sysLog.getUserName())) condition.eq(SysLog::getUserName,sysLog.getUserName());
        if(StringKit.isNotEmpty(sysLog.getSysType())) condition.eq(SysLog::getSysType,sysLog.getSysType());
        condition.orderByDesc(SysLog::getCreatedAt);

        if (paramJSON != null) {
            if (StringKit.isNotEmpty(paramJSON.getString("createdStart"))) {
                condition.ge(SysLog::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringKit.isNotEmpty(paramJSON.getString("createdEnd"))) {
                condition.le(SysLog::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        IPage<SysLog> page = sysLogService.page(super.getIPage(), condition);
        return ApiPageRes.pages(page);
    }
    @Operation(summary = "系统日志详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "sysLogId", description = "系统日志ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_LOG_VIEW')")
    @RequestMapping(value="/{sysLogId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("sysLogId") String sysLogId) {
        SysLog sysLog = sysLogService.getById(sysLogId);
        return  sysLog != null ?ApiRes.success(sysLog):  ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
    }
    @Operation(summary = "删除日志信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "selectedIds", description = "系统日志ID（若干个ID用英文逗号拼接）", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_LOG_DEL')")
    @MethodLog(remark = "删除日志信息")
    @RequestMapping(value="/{selectedIds}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("selectedIds") String selectedIds) {
        String[] ids = selectedIds.split(",");
        List<Long> idsList = new ArrayList<>();
        for (String id : ids) {
            idsList.add(Long.valueOf(id));
        }
        boolean isDel = sysLogService.removeByIds(idsList);
        return isDel ? ApiRes.success():ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
    }
}
