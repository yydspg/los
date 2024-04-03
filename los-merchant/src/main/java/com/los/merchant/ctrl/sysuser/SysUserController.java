package com.los.merchant.ctrl.sysuser;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.aop.MethodLog;
import com.los.core.constants.CS;
import com.los.core.entity.SysUser;
import com.los.core.exception.BizException;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.merchant.ctrl.CommonCtrl;
import com.los.merchant.service.AuthService;
import com.los.service.SysUserAuthService;
import com.los.service.SysUserService;
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

import java.util.List;

/**
 * @author paul 2024/3/25
 */

@Tag(name = "系统管理（操作员）")
@RestController
@RequestMapping("api/sysUsers")
public class SysUserController extends CommonCtrl {

    @Resource private SysUserService sysUserService;
    @Resource private SysUserAuthService sysUserAuthService;
    @Resource private AuthService authService;

    @Operation(summary ="操作员列表")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description =  "分页页码"),
            @Parameter(name = "pageSize", description =  "分页条数"),
            @Parameter(name = "sysUserId", description =  "用户ID"),
            @Parameter(name = "realname", description =  "用户姓名")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_LIST' )")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<SysUser> list() {

        SysUser queryObject = getObject(SysUser.class);

        LambdaQueryWrapper<SysUser> condition = SysUser.gw();
        condition.eq(SysUser::getSysType, CS.SYS_TYPE.MGR);

        if(StringKit.isNotEmpty(queryObject.getRealname())){
            condition.like(SysUser::getRealname, queryObject.getRealname());
        }

        if(queryObject.getSysUserId() != null){
            condition.eq(SysUser::getSysUserId, queryObject.getSysUserId());
        }

        condition.orderByDesc(SysUser::getCreatedAt); //时间： 降序

        IPage<SysUser> pages = sysUserService.page(getIPage(), condition);

        return ApiPageRes.pages(pages);
    }
    @Operation(summary ="操作员详情")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description =  "用户ID", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_EDIT' )")
    @RequestMapping(value="/{recordId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("recordId") Long recordId) {
        return ApiRes.success(sysUserService.getById(recordId));
    }
    @Operation(summary ="添加操作员")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "isAdmin", description =  "是否超管（超管拥有全部权限） 0-否 1-是", required = true),
            @Parameter(name = "loginUsername", description =  "登录用户名", required = true),
            @Parameter(name = "realname", description =  "真实姓名", required = true),
            @Parameter(name = "sex", description =  "性别 0-未知, 1-男, 2-女", required = true),
            @Parameter(name = "telphone", description =  "手机号", required = true),
            @Parameter(name = "userNo", description =  "员工编号", required = true),
            @Parameter(name = "state", description =  "状态: 0-停用, 1-启用", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_ADD' )")
    @MethodLog(remark = "添加操作员")
    @RequestMapping(value="", method = RequestMethod.POST)
    public ApiRes add() {
        SysUser sysUser = getObject(SysUser.class);
        // TODO 2024/4/3 : setBelongId(0) 是什么鬼
        sysUser.setBelongInfoId("0");

        sysUserService.addSysUser(sysUser, CS.SYS_TYPE.MGR);
        return ApiRes.success();
    }
    /** update */
    @Operation(summary ="修改操作员信息")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description =  "用户ID", required = true),
            @Parameter(name = "isAdmin", description =  "是否超管（超管拥有全部权限） 0-否 1-是", required = true),
            @Parameter(name = "loginUsername", description =  "登录用户名", required = true),
            @Parameter(name = "realname", description =  "真实姓名", required = true),
            @Parameter(name = "sex", description =  "性别 0-未知, 1-男, 2-女", required = true),
            @Parameter(name = "telphone", description =  "手机号", required = true),
            @Parameter(name = "userNo", description =  "员工编号", required = true),
            @Parameter(name = "state", description =  "状态: 0-停用, 1-启用", required = true),
            @Parameter(name = "resetPass", description =  "是否重置密码"),
            @Parameter(name = "confirmPwd", description =  "待更新的密码，base64加密"),
            @Parameter(name = "defaultPass", description =  "是否默认密码")
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_EDIT' )")
    @RequestMapping(value="/{recordId}", method = RequestMethod.PUT)
    @MethodLog(remark = "修改操作员信息")
    public ApiRes update(@PathVariable("recordId") Long recordId) {
        SysUser sysUser = getObject(SysUser.class);
        sysUser.setSysUserId(recordId);
        //判断是否自己禁用自己
        if(recordId.equals(getCurrentUser().getSysUser().getSysUserId()) && sysUser.getState() != null && sysUser.getState() == CS.PUB_DISABLE){
            throw new BizException("系统不允许禁用当前登陆用户！");
        }
        //判断是否重置密码
        Boolean resetPass = getReqParamJSON().getBoolean("resetPass");
        if (resetPass != null && resetPass) {
            String updatePwd = !super.getReqParamJSON().getBoolean("defaultPass") ? Base64.decodeStr(getValStringRequired("confirmPwd")) : CS.DEFAULT_PWD;
            sysUserAuthService.resetPwd(recordId, updatePwd, CS.SYS_TYPE.MGR);
            // 删除用户redis缓存信息
            authService.delAuthentication(List.of(recordId));
        }

        sysUserService.updateSysUser(sysUser);

        //如果用户被禁用，需要更新redis数据
        if(sysUser.getState() != null && sysUser.getState() == CS.PUB_DISABLE){
            authService.refAuthentication(List.of(recordId));
        }
        return ApiRes.success();
    }
    @Operation(summary ="删除操作员信息")
    @Parameters({
            @Parameter(name = "iToken", description =  "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "recordId", description =  "用户ID", required = true)
    })
    @PreAuthorize("hasAuthority( 'ENT_UR_USER_DELETE' )")
    @RequestMapping(value="/{recordId}", method = RequestMethod.DELETE)
    @MethodLog(remark = "删除操作员信息")
    public ApiRes delete(@PathVariable("recordId") Long recordId) {
        //查询该操作员信息
        SysUser sysUser = sysUserService.getById(recordId);
        if (sysUser == null) {
            throw new BizException("该操作员不存在！");
        }

        //判断是否自己删除自己
        if(recordId.equals(getCurrentUser().getSysUser().getSysUserId())){
            throw new BizException("系统不允许删除当前登陆用户！");
        }
        //判断是否删除商户默认超管
        SysUser mchUserDefault = sysUserService.getOne(SysUser.gw()
                .eq(SysUser::getBelongInfoId, getCurrentMchNo())
                .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
                .eq(SysUser::getIsAdmin, CS.YES)
        );

        if (mchUserDefault.getSysUserId().equals(recordId)) {
            throw new BizException("系统不允许删除商户默认用户！");
        }
        // 删除用户
        sysUserService.removeUser(sysUser, CS.SYS_TYPE.MGR);

        //如果用户被删除，需要更新redis数据
        authService.refAuthentication(List.of(recordId));

        return ApiRes.success();
    }
}
