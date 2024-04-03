package com.los.merchant.ctrl.sysuser;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.SysRole;
import com.los.core.entity.SysUserRoleRela;
import com.los.core.exception.BizException;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.merchant.ctrl.CommonCtrl;
import com.los.merchant.service.AuthService;
import com.los.service.SysRoleEntRelaService;
import com.los.service.SysRoleService;
import com.los.service.SysUserRoleRelaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author paul 2024/3/25
 */
@Tag(name = "系统管理（用户角色）")
@RestController
@RequestMapping("api/sysRoles")
public class SysRoleController  extends CommonCtrl {
    @Resource SysRoleService sysRoleService;
    @Resource SysUserRoleRelaService sysUserRoleRelaService;
    @Resource private AuthService authService;
    @Resource private SysRoleEntRelaService sysRoleEntRelaService;
    @Operation(summary = "角色列表")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description =  "分页页码"),
            @Parameter(name = "pageSize", description =  "分页条数（-1时查全部数据）"),
            @Parameter(name = "roleId", description =  "角色ID, ROLE_开头"),
            @Parameter(name = "roleName", description =  "角色名称")
    })
    @PreAuthorize("hasAnyAuthority( 'ENT_UR_ROLE_LIST', 'ENT_UR_USER_UPD_ROLE' )")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<SysRole> list() throws NoSuchMethodException {
        SysRole queryObject = getObject(SysRole.class);

        QueryWrapper<SysRole> condition = new QueryWrapper<>();
        LambdaQueryWrapper<SysRole> lambdaCondition = condition.lambda();
        lambdaCondition.eq(SysRole::getSysType, CS.SYS_TYPE.MGR);
        lambdaCondition.eq(SysRole::getBelongInfoId, 0);

        if(StringKit.isNotEmpty(queryObject.getRoleName())){
            lambdaCondition.like(SysRole::getRoleName, queryObject.getRoleName());
        }

        if(StringKit.isNotEmpty(queryObject.getRoleId())){
            lambdaCondition.like(SysRole::getRoleId, queryObject.getRoleId());
        }

        //是否有排序字段
        MutablePair<Boolean, String> orderInfo = getSortInfo();
        if(orderInfo != null){
            condition.orderBy(true, orderInfo.getLeft(), orderInfo.getRight());
        }else{
            lambdaCondition.orderByDesc(SysRole::getUpdatedAt);
        }

        IPage<SysRole> pages = sysRoleService.page(getIPage(true), condition);
        return ApiPageRes.pages(pages);
    }
    @Operation(summary ="角色详情")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description =  "角色ID, ROLE_开头", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
    @RequestMapping(value="/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") String recordId) {
        SysRole sysRole = sysRoleService.getById(recordId);
        return  sysRole != null ?ApiRes.success(sysRole):  ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);

    }
    @Operation(summary ="添加角色信息")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "roleName", description =  "角色名称", required = true),
            @Parameter(name = "entIdListStr", description =  "权限信息集合，eg：[str1,str2]，字符串列表转成json字符串，若为空，则创建的角色无任何权限")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_ADD' )")
    @MethodLog(remark = "添加角色信息")
    @RequestMapping(value="", method = RequestMethod.POST)  
    public ApiRes add() {
        SysRole sysRole = super.getObject(SysRole.class);
        String roleId = "ROLE_" + StringKit.getUUID(6);
        sysRole.setRoleId(roleId);
        sysRole.setSysType(CS.SYS_TYPE.MGR);

        if(!sysRoleService.save(sysRole)) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        String entIdListStr = super.getValStringRequired("entIdListStr");

        // 若 当前用户可分配权限 && 权限集合不为空
        if(super.getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority("ENT_UR_ROLE_DIST"))
                &&StringKit.isNotEmpty(entIdListStr)) {
            List<String> entIdList = JSONArray.parseArray(entIdListStr, String.class);
            sysRoleEntRelaService.resetRela(roleId,entIdList);
        }
        return ApiRes.success();
    }
    @Operation(summary ="更新角色信息")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description =  "角色ID, ROLE_开头", required = true),
            @Parameter(name = "roleName", description =  "角色名称", required = true),
            @Parameter(name = "entIdListStr", description =  "权限信息集合，eg：[str1,str2]，字符串列表转成json字符串，若为空，则创建的角色无任何权限")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_ROLE_EDIT' )")
    @RequestMapping(value="/{recordId}", method = RequestMethod.POST)
    @MethodLog(remark = "更新角色信息")
    public ApiRes update(@PathVariable(value = "recordId") String recordId) {
        SysRole SysRole = getObject(SysRole.class);
        SysRole.setRoleId(recordId);
        if (!sysRoleService.updateById(SysRole)) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        String entIdListStr = super.getValStringRequired("entIdListStr");
        // 若 当前用户可分配权限 && 权限集合不为空
        if(super.getCurrentUser().getAuthorities().contains(new SimpleGrantedAuthority("ENT_UR_ROLE_DIST"))
                &&StringKit.isNotEmpty(entIdListStr)) {
            List<String> entIdList = JSONArray.parseArray(entIdListStr, String.class);

            sysRoleEntRelaService.resetRela(recordId, entIdList);

            List<Long> sysUserIdList = new ArrayList<>();
            sysUserRoleRelaService.list(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)).forEach(item -> sysUserIdList.add(item.getUserId()));

            //查询到该角色的人员， 将redis更新
            authService.refAuthentication(sysUserIdList);
        }
        return ApiRes.success();
    }
    @Operation(summary ="删除角色")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description =  "角色ID, ROLE_开头", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_UR_ROLE_DEL')")
    @MethodLog(remark = "删除角色")
    @RequestMapping(value="/{recordId}", method = RequestMethod.DELETE)
    public ApiRes del(@PathVariable("recordId") String recordId) {

        if(sysUserRoleRelaService.count(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, recordId)) > 0){
            throw new BizException("TheCurrentRoleHasBeenAssignedToUserAndCannotBeDeleted!");
        }
        sysRoleService.removeRole(recordId);
        return ApiRes.success();
    }

}