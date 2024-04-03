package com.los.manager.ctrl.sysuser;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.SysEntitlement;
import com.los.core.model.ApiRes;
import com.los.core.utils.TreeDataBuilder;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.SysEntitlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "系统管理(用户权限)")
@RestController
// TODO 2024/4/3 : 修改了请求地址
@RequestMapping("/api/sysEnt")
public class SysEntController extends CommonCtrl {
    @Resource SysEntitlementService sysEntitlementService;
    @Operation(summary = "查询菜单权限详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "entId", description = "权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD", required = true),
            @Parameter(name = "sysType", description = "所属系统： MGR-运营平台, MCH-商户中心", required = true)
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST' )")
    @RequestMapping(value="/bySysType", method = RequestMethod.GET)
    public ApiRes getEntInfo() {
        return ApiRes.success(sysEntitlementService.getOne(SysEntitlement.gw()
                .eq(SysEntitlement::getEntId,super.getValStringRequired("entId"))
                .eq(SysEntitlement::getSysType,super.getValStringRequired("sysType"))));
    }
    @Operation(summary = "更新权限资源")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "entId", description = "权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD", required = true),
            @Parameter(name = "entName", description = "权限名称", required = true),
            @Parameter(name = "menuUri", description = "菜单uri/路由地址"),
            @Parameter(name = "entSort", description = "排序字段, 规则：正序"),
            @Parameter(name = "quickJump", description = "快速开始菜单 0-否, 1-是"),
            @Parameter(name = "state", description = "状态 0-停用, 1-启用")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ENT_EDIT')")
    @MethodLog(remark = "更新资源权限")
    @RequestMapping(value="", method = RequestMethod.POST)
    public ApiRes updateEntInfoById () {
        SysEntitlement updateRecord = super.getObject(SysEntitlement.class);
        boolean isUpdateSuccess = sysEntitlementService.update(updateRecord, SysEntitlement.gw().eq(SysEntitlement::getEntId, updateRecord.getEntId()).eq(SysEntitlement::getEntType, updateRecord.getSysType()));
        if(isUpdateSuccess) return ApiRes.success();
        else return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE,"updateEntInfo");
    }
    @Operation(summary = "查询权限集合")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "sysType", description = "所属系统： MGR-运营平台, MCH-商户中心", required = true)
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_ENT_LIST', 'ENT_UR_ROLE_DIST' )")
    @RequestMapping(value="/showTree", method = RequestMethod.GET)
    public ApiRes getEntTree () {
        List<SysEntitlement> sysType = sysEntitlementService.list(SysEntitlement.gw().eq(SysEntitlement::getEntType, super.getValStringRequired("sysType")));

        //转换为json树状结构
        JSONArray jsonArray = JSONArray.from(sysType);
        List<JSONObject> leftMenuTree = new TreeDataBuilder(jsonArray,
                "entId", "pid", "children", "entSort", true)
                .buildTreeObject();
        return ApiRes.success(leftMenuTree);
    }
}
