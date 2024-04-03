package com.los.merchant.ctrl;


import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.aop.MethodLog;
import com.los.core.cache.ITokenService;
import com.los.core.constants.CS;
import com.los.core.entity.SysEntitlement;
import com.los.core.entity.SysUser;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.model.security.LosUserDetails;
import com.los.core.utils.StringKit;
import com.los.core.utils.TreeDataBuilder;
import com.los.service.SysEntitlementService;
import com.los.service.SysUserAuthService;
import com.los.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author paul 2024/3/25
 */

@Slf4j
@Tag(name = "登录信息",description = "当前登录者信息接口")
@RestController
@RequestMapping("/api/current")
public class CurrentUserController extends CommonCtrl {
    @Autowired private SysEntitlementService sysEntitlementService;
    @Autowired private SysUserService sysUserService;
    @Autowired private SysUserAuthService sysUserAuthService;



    @Operation(summary = "查询当前登录者的用户信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证",required = true,in = ParameterIn.HEADER)
    })
    @RequestMapping(value = "/user",method = RequestMethod.GET)
    public ApiRes getCurrentUserInfo() {

        // 当前用户信息, 从spring-security context holder中得到数据
        LosUserDetails currentUser = super.getCurrentUser();

        if (currentUser == null) {
            log.error("noSuchUser");
            return ApiRes.customFail("noSuchUser");
        }
        // 权限集合
        List<String> entIdList = new ArrayList<>();
        // 菜单集合
        List<SysEntitlement> menuList = new ArrayList<>();
        currentUser.getAuthorities().forEach(r->entIdList.add(r.getAuthority()));
        // 查询菜单集合
        if(!entIdList.isEmpty()) {
            menuList = sysEntitlementService.list(SysEntitlement.gw()
                    .in(SysEntitlement::getEntId,entIdList)
                    .in(SysEntitlement::getEntType, CS.ENT_TYPE.MENU_LEFT,CS.ENT_TYPE.MENU_OTHER)
                    .eq(SysEntitlement::getState,CS.PUB_USABLE)
                    .eq(SysEntitlement::getSysType,CS.SYS_TYPE.MGR)
            );
        }
        // 转换为 json 树状结构
        // TODO 2024/3/29 : 此方法目前未测试
        JSONArray jsonArray = JSONArray.from(menuList);
        List<JSONObject> menuRouteTree = new TreeDataBuilder(jsonArray,
                "entId", "pid", "children", "endSort", true)
                .buildTreeObject();

        // 同步权限集合
        currentUser.getSysUser().addExt("entIdList",entIdList);
        // TODO 2024/3/29 :  此处key 改动 前端的代码需要修改
        currentUser.getSysUser().addExt("menuRouteTree",menuRouteTree);
        // TODO 2024/3/29 : 思考 --> 为何不直接将程序中的改变量返回
        return ApiRes.success(super.getCurrentUser().getSysUser());
    }

    @Operation(summary = "修改个人信息")
    @Parameters({
            @Parameter(name = "iToken",description = "用户身份凭证",required = true,in = ParameterIn.HEADER),
            @Parameter(name = "avatarUrl",description = "头像地址"),
            @Parameter(name = "realname",description = "真实姓名"),
            @Parameter(name = "sex",description = "性别 0-未知, 1-男, 2-女")
    })
    @PostMapping("/user")
    @MethodLog(remark = "updateInfo")
    public ApiRes modifyCurrentUserInfo() {
        // TODO 2024/3/29 : 总结 更新的一般思路 : 查询-->创建updateRecord-->保存
        String avatarUrl = super.getValString("avatarUrl");
        String realName = super.getValString("realname");
        Byte sex = super.getValByte("sex");
        Long sysUserId = super.getCurrentUser().getSysUser().getSysUserId();
        SysUser updateRecord = new SysUser();
        updateRecord.setSysUserId(sysUserId);

        if(StringKit.isNotEmpty(avatarUrl)) updateRecord.setAvatarUrl(avatarUrl);
        if(StringKit.isNotEmpty(realName)) updateRecord.setRealname(realName);
        if(sex != null) updateRecord.setSex(sex);

        //update error
        if(!sysUserService.updateById(updateRecord)){
            log.error("[{}]updateError",sysUserId);
            return ApiRes.customFail("updateFail");
        }

        // 保存redis 数据
        LosUserDetails currentUser = super.getCurrentUser();
        currentUser.setSysUser(sysUserService.getById(sysUserId));
        ITokenService.refData(currentUser);

        return ApiRes.success();
    }
    @Operation(summary = "修改个人密码")
    @Parameters({
            @Parameter(name = "iToken",description = "用户身份凭证",required = true,in = ParameterIn.HEADER),
            @Parameter(name = "confirmPwd",description = "新密码"),
            @Parameter(name = "originalPwd",description = "原密码"),
    })
    @PostMapping("/modifyPwd")
    public ApiRes modifyCurrentUserPwd() {

        //更改密码，验证当前用户信息
        String currentUserPwd = Base64.decodeStr(getValStringRequired("originalPwd")); //当前用户登录密码
        //验证当前密码是否正确
        if(!sysUserAuthService.validateCurrentUserPwd(currentUserPwd)){
            throw new BizException("The original password verification failed");
        }

        String opUserPwd = Base64.decodeStr(getValStringRequired("confirmPwd"));

        // 验证原密码与新密码是否相同
        if (opUserPwd.equals(currentUserPwd)) {
            throw new BizException("The new password cannot be the same as the original password");
        }

        sysUserAuthService.resetPwd(getCurrentUser().getSysUser().getSysUserId(), opUserPwd, CS.SYS_TYPE.MGR);
        //调用登出接口
        return logout();
    }
    @PostMapping("/logout")
    @MethodLog(remark = "登出")
    public ApiRes logout() throws BizException{
        ITokenService.removeIToken(super.getCurrentUser().getCacheKey(),super.getCurrentUser().getSysUser().getSysUserId());
        return ApiRes.success();
    }
}
